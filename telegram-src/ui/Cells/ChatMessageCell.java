package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStructure;
import android.view.animation.DecelerateInterpolator;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessageObject.GroupedMessagePosition;
import org.telegram.messenger.MessageObject.GroupedMessages;
import org.telegram.messenger.MessageObject.TextLayoutBlock;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.GeoPoint;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonGame;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRequestGeoLocation;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonSwitchInline;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonUrl;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.RoundVideoPlayingDrawable;
import org.telegram.ui.Components.SeekBar;
import org.telegram.ui.Components.SeekBar.SeekBarDelegate;
import org.telegram.ui.Components.SeekBarWaveform;
import org.telegram.ui.Components.StaticLayoutEx;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.SecretMediaViewer;

public class ChatMessageCell
  extends BaseCell
  implements DownloadController.FileDownloadProgressListener, ImageReceiver.ImageReceiverDelegate, SeekBar.SeekBarDelegate
{
  private static final int DOCUMENT_ATTACH_TYPE_AUDIO = 3;
  private static final int DOCUMENT_ATTACH_TYPE_DOCUMENT = 1;
  private static final int DOCUMENT_ATTACH_TYPE_GIF = 2;
  private static final int DOCUMENT_ATTACH_TYPE_MUSIC = 5;
  private static final int DOCUMENT_ATTACH_TYPE_NONE = 0;
  private static final int DOCUMENT_ATTACH_TYPE_ROUND = 7;
  private static final int DOCUMENT_ATTACH_TYPE_STICKER = 6;
  private static final int DOCUMENT_ATTACH_TYPE_VIDEO = 4;
  private int TAG;
  private int addedCaptionHeight;
  private StaticLayout adminLayout;
  private boolean allowAssistant;
  private StaticLayout authorLayout;
  private int authorX;
  private int availableTimeWidth;
  private AvatarDrawable avatarDrawable;
  private ImageReceiver avatarImage = new ImageReceiver();
  private boolean avatarPressed;
  private int backgroundDrawableLeft;
  private int backgroundDrawableRight;
  private int backgroundWidth = 100;
  private ArrayList<BotButton> botButtons = new ArrayList();
  private HashMap<String, BotButton> botButtonsByData = new HashMap();
  private HashMap<String, BotButton> botButtonsByPosition = new HashMap();
  private String botButtonsLayout;
  private int buttonPressed;
  private int buttonState;
  private int buttonX;
  private int buttonY;
  private boolean cancelLoading;
  private int captionHeight;
  private StaticLayout captionLayout;
  private int captionOffsetX;
  private int captionWidth;
  private int captionX;
  private int captionY;
  private AvatarDrawable contactAvatarDrawable;
  private float controlsAlpha = 1.0F;
  private int currentAccount = UserConfig.selectedAccount;
  private Drawable currentBackgroundDrawable;
  private CharSequence currentCaption;
  private TLRPC.Chat currentChat;
  private TLRPC.Chat currentForwardChannel;
  private String currentForwardNameString;
  private TLRPC.User currentForwardUser;
  private MessageObject currentMessageObject;
  private MessageObject.GroupedMessages currentMessagesGroup;
  private String currentNameString;
  private TLRPC.FileLocation currentPhoto;
  private String currentPhotoFilter;
  private String currentPhotoFilterThumb;
  private TLRPC.PhotoSize currentPhotoObject;
  private TLRPC.PhotoSize currentPhotoObjectThumb;
  private MessageObject.GroupedMessagePosition currentPosition;
  private TLRPC.FileLocation currentReplyPhoto;
  private String currentTimeString;
  private String currentUrl;
  private TLRPC.User currentUser;
  private TLRPC.User currentViaBotUser;
  private String currentViewsString;
  private ChatMessageCellDelegate delegate;
  private RectF deleteProgressRect = new RectF();
  private StaticLayout descriptionLayout;
  private int descriptionX;
  private int descriptionY;
  private boolean disallowLongPress;
  private StaticLayout docTitleLayout;
  private int docTitleOffsetX;
  private TLRPC.Document documentAttach;
  private int documentAttachType;
  private boolean drawBackground = true;
  private boolean drawForwardedName;
  private boolean drawImageButton;
  private boolean drawInstantView;
  private int drawInstantViewType;
  private boolean drawJoinChannelView;
  private boolean drawJoinGroupView;
  private boolean drawName;
  private boolean drawNameLayout;
  private boolean drawPhotoImage;
  private boolean drawPinnedBottom;
  private boolean drawPinnedTop;
  private boolean drawRadialCheckBackground;
  private boolean drawShareButton;
  private boolean drawTime = true;
  private boolean drwaShareGoIcon;
  private StaticLayout durationLayout;
  private int durationWidth;
  private int firstVisibleBlockNum;
  private boolean forceNotDrawTime;
  private boolean forwardBotPressed;
  private boolean forwardName;
  private float[] forwardNameOffsetX = new float[2];
  private boolean forwardNamePressed;
  private int forwardNameX;
  private int forwardNameY;
  private StaticLayout[] forwardedNameLayout = new StaticLayout[2];
  private int forwardedNameWidth;
  private boolean fullyDraw;
  private boolean gamePreviewPressed;
  private boolean groupPhotoInvisible;
  private boolean hasGamePreview;
  private boolean hasInvoicePreview;
  private boolean hasLinkPreview;
  private int hasMiniProgress;
  private boolean hasNewLineForTime;
  private boolean hasOldCaptionPreview;
  private int highlightProgress;
  private boolean imagePressed;
  private boolean inLayout;
  private StaticLayout infoLayout;
  private int infoWidth;
  private boolean instantButtonPressed;
  private boolean instantPressed;
  private int instantTextLeftX;
  private int instantTextX;
  private StaticLayout instantViewLayout;
  private Drawable instantViewSelectorDrawable;
  private int instantWidth;
  private Runnable invalidateRunnable = new Runnable()
  {
    public void run()
    {
      ChatMessageCell.this.checkLocationExpired();
      if (ChatMessageCell.this.locationExpired)
      {
        ChatMessageCell.this.invalidate();
        ChatMessageCell.access$202(ChatMessageCell.this, false);
      }
      do
      {
        return;
        ChatMessageCell.this.invalidate((int)ChatMessageCell.this.rect.left - 5, (int)ChatMessageCell.this.rect.top - 5, (int)ChatMessageCell.this.rect.right + 5, (int)ChatMessageCell.this.rect.bottom + 5);
      } while (!ChatMessageCell.this.scheduledInvalidate);
      AndroidUtilities.runOnUIThread(ChatMessageCell.this.invalidateRunnable, 1000L);
    }
  };
  private boolean isAvatarVisible;
  public boolean isChat;
  private boolean isCheckPressed = true;
  private boolean isHighlighted;
  private boolean isHighlightedAnimated;
  private boolean isPressed;
  private boolean isSmallImage;
  private int keyboardHeight;
  private long lastControlsAlphaChangeTime;
  private int lastDeleteDate;
  private int lastHeight;
  private long lastHighlightProgressTime;
  private int lastSendState;
  private int lastTime;
  private int lastViewsCount;
  private int lastVisibleBlockNum;
  private int layoutHeight;
  private int layoutWidth;
  private int linkBlockNum;
  private int linkPreviewHeight;
  private boolean linkPreviewPressed;
  private int linkSelectionBlockNum;
  private boolean locationExpired;
  private ImageReceiver locationImageReceiver;
  private boolean mediaBackground;
  private int mediaOffsetY;
  private boolean mediaWasInvisible;
  private int miniButtonPressed;
  private int miniButtonState;
  private StaticLayout nameLayout;
  private float nameOffsetX;
  private int nameWidth;
  private float nameX;
  private float nameY;
  private int namesOffset;
  private boolean needNewVisiblePart;
  private boolean needReplyImage;
  private boolean otherPressed;
  private int otherX;
  private int otherY;
  private StaticLayout performerLayout;
  private int performerX;
  private ImageReceiver photoImage;
  private boolean photoNotSet;
  private StaticLayout photosCountLayout;
  private int photosCountWidth;
  private boolean pinnedBottom;
  private boolean pinnedTop;
  private int pressedBotButton;
  private CharacterStyle pressedLink;
  private int pressedLinkType;
  private int[] pressedState = { 16842910, 16842919 };
  private RadialProgress radialProgress;
  private RectF rect = new RectF();
  private ImageReceiver replyImageReceiver;
  private StaticLayout replyNameLayout;
  private float replyNameOffset;
  private int replyNameWidth;
  private boolean replyPressed;
  private int replyStartX;
  private int replyStartY;
  private StaticLayout replyTextLayout;
  private float replyTextOffset;
  private int replyTextWidth;
  private RoundVideoPlayingDrawable roundVideoPlayingDrawable;
  private boolean scheduledInvalidate;
  private Rect scrollRect = new Rect();
  private SeekBar seekBar;
  private SeekBarWaveform seekBarWaveform;
  private int seekBarX;
  private int seekBarY;
  private boolean sharePressed;
  private int shareStartX;
  private int shareStartY;
  private StaticLayout siteNameLayout;
  private boolean siteNameRtl;
  private int siteNameWidth;
  private StaticLayout songLayout;
  private int songX;
  private int substractBackgroundHeight;
  private int textX;
  private int textY;
  private float timeAlpha = 1.0F;
  private int timeAudioX;
  private StaticLayout timeLayout;
  private int timeTextWidth;
  private boolean timeWasInvisible;
  private int timeWidth;
  private int timeWidthAudio;
  private int timeX;
  private StaticLayout titleLayout;
  private int titleX;
  private long totalChangeTime;
  private int totalHeight;
  private int totalVisibleBlocksCount;
  private int unmovedTextX;
  private ArrayList<LinkPath> urlPath = new ArrayList();
  private ArrayList<LinkPath> urlPathCache = new ArrayList();
  private ArrayList<LinkPath> urlPathSelection = new ArrayList();
  private boolean useSeekBarWaweform;
  private int viaNameWidth;
  private int viaWidth;
  private StaticLayout videoInfoLayout;
  private StaticLayout viewsLayout;
  private int viewsTextWidth;
  private boolean wasLayout;
  private int widthBeforeNewTimeLine;
  private int widthForButtons;
  
  public ChatMessageCell(Context paramContext)
  {
    super(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0F));
    this.avatarDrawable = new AvatarDrawable();
    this.replyImageReceiver = new ImageReceiver(this);
    this.locationImageReceiver = new ImageReceiver(this);
    this.locationImageReceiver.setRoundRadius(AndroidUtilities.dp(26.1F));
    this.TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
    this.contactAvatarDrawable = new AvatarDrawable();
    this.photoImage = new ImageReceiver(this);
    this.photoImage.setDelegate(this);
    this.radialProgress = new RadialProgress(this);
    this.seekBar = new SeekBar(paramContext);
    this.seekBar.setDelegate(this);
    this.seekBarWaveform = new SeekBarWaveform(paramContext);
    this.seekBarWaveform.setDelegate(this);
    this.seekBarWaveform.setParentView(this);
    this.roundVideoPlayingDrawable = new RoundVideoPlayingDrawable(this);
  }
  
  private void calcBackgroundWidth(int paramInt1, int paramInt2, int paramInt3)
  {
    if ((this.hasLinkPreview) || (this.hasOldCaptionPreview) || (this.hasGamePreview) || (this.hasInvoicePreview) || (paramInt1 - this.currentMessageObject.lastLineWidth < paramInt2) || (this.currentMessageObject.hasRtl))
    {
      this.totalHeight += AndroidUtilities.dp(14.0F);
      this.hasNewLineForTime = true;
      this.backgroundWidth = (Math.max(paramInt3, this.currentMessageObject.lastLineWidth) + AndroidUtilities.dp(31.0F));
      paramInt2 = this.backgroundWidth;
      if (this.currentMessageObject.isOutOwner()) {}
      for (paramInt1 = this.timeWidth + AndroidUtilities.dp(17.0F);; paramInt1 = this.timeWidth)
      {
        this.backgroundWidth = Math.max(paramInt2, paramInt1 + AndroidUtilities.dp(31.0F));
        return;
      }
    }
    paramInt1 = paramInt3 - this.currentMessageObject.lastLineWidth;
    if ((paramInt1 >= 0) && (paramInt1 <= paramInt2))
    {
      this.backgroundWidth = (paramInt3 + paramInt2 - paramInt1 + AndroidUtilities.dp(31.0F));
      return;
    }
    this.backgroundWidth = (Math.max(paramInt3, this.currentMessageObject.lastLineWidth + paramInt2) + AndroidUtilities.dp(31.0F));
  }
  
  private boolean checkAudioMotionEvent(MotionEvent paramMotionEvent)
  {
    boolean bool2;
    if ((this.documentAttachType != 3) && (this.documentAttachType != 5)) {
      bool2 = false;
    }
    boolean bool1;
    label161:
    label194:
    int j;
    label348:
    label369:
    label377:
    label415:
    label420:
    label433:
    label453:
    label458:
    label518:
    do
    {
      int n;
      int k;
      int m;
      int i;
      do
      {
        return bool2;
        n = (int)paramMotionEvent.getX();
        k = (int)paramMotionEvent.getY();
        if (this.useSeekBarWaweform)
        {
          bool1 = this.seekBarWaveform.onTouch(paramMotionEvent.getAction(), paramMotionEvent.getX() - this.seekBarX - AndroidUtilities.dp(13.0F), paramMotionEvent.getY() - this.seekBarY);
          if (!bool1) {
            break label194;
          }
          if ((this.useSeekBarWaweform) || (paramMotionEvent.getAction() != 0)) {
            break label161;
          }
          getParent().requestDisallowInterceptTouchEvent(true);
        }
        for (;;)
        {
          this.disallowLongPress = true;
          invalidate();
          return bool1;
          bool1 = this.seekBar.onTouch(paramMotionEvent.getAction(), paramMotionEvent.getX() - this.seekBarX, paramMotionEvent.getY() - this.seekBarY);
          break;
          if ((this.useSeekBarWaweform) && (!this.seekBarWaveform.isStartDraging()) && (paramMotionEvent.getAction() == 1)) {
            didPressedButton(true);
          }
        }
        m = AndroidUtilities.dp(36.0F);
        i = 0;
        j = 0;
        if (this.miniButtonState >= 0)
        {
          j = AndroidUtilities.dp(27.0F);
          if ((n < this.buttonX + j) || (n > this.buttonX + j + m) || (k < this.buttonY + j) || (k > this.buttonY + j + m)) {
            break label415;
          }
          j = 1;
        }
        if (j == 0)
        {
          if ((this.buttonState != 0) && (this.buttonState != 1) && (this.buttonState != 2)) {
            break label458;
          }
          if ((n < this.buttonX - AndroidUtilities.dp(12.0F)) || (n > this.buttonX - AndroidUtilities.dp(12.0F) + this.backgroundWidth)) {
            break label453;
          }
          if (!this.drawInstantView) {
            break label420;
          }
          i = this.buttonY;
          if (k < i) {
            break label453;
          }
          if (!this.drawInstantView) {
            break label433;
          }
          i = this.buttonY + m;
          if (k > i) {
            break label453;
          }
          i = 1;
        }
        if (paramMotionEvent.getAction() != 0) {
          break label518;
        }
        if (i != 0) {
          break;
        }
        bool2 = bool1;
      } while (j == 0);
      if (i != 0) {
        this.buttonPressed = 1;
      }
      for (;;)
      {
        invalidate();
        updateRadialProgressBackground();
        return true;
        j = 0;
        break;
        i = this.namesOffset + this.mediaOffsetY;
        break label348;
        i = this.namesOffset + this.mediaOffsetY + AndroidUtilities.dp(82.0F);
        break label369;
        i = 0;
        break label377;
        if ((n >= this.buttonX) && (n <= this.buttonX + m) && (k >= this.buttonY) && (k <= this.buttonY + m)) {}
        for (i = 1;; i = 0) {
          break;
        }
        this.miniButtonPressed = 1;
      }
      if (this.buttonPressed != 0)
      {
        if (paramMotionEvent.getAction() == 1)
        {
          this.buttonPressed = 0;
          playSoundEffect(0);
          didPressedButton(true);
          invalidate();
        }
        for (;;)
        {
          updateRadialProgressBackground();
          return bool1;
          if (paramMotionEvent.getAction() == 3)
          {
            this.buttonPressed = 0;
            invalidate();
          }
          else if ((paramMotionEvent.getAction() == 2) && (i == 0))
          {
            this.buttonPressed = 0;
            invalidate();
          }
        }
      }
      bool2 = bool1;
    } while (this.miniButtonPressed == 0);
    if (paramMotionEvent.getAction() == 1)
    {
      this.miniButtonPressed = 0;
      playSoundEffect(0);
      didPressedMiniButton(true);
      invalidate();
    }
    for (;;)
    {
      updateRadialProgressBackground();
      return bool1;
      if (paramMotionEvent.getAction() == 3)
      {
        this.miniButtonPressed = 0;
        invalidate();
      }
      else if ((paramMotionEvent.getAction() == 2) && (j == 0))
      {
        this.miniButtonPressed = 0;
        invalidate();
      }
    }
  }
  
  private boolean checkBotButtonMotionEvent(MotionEvent paramMotionEvent)
  {
    if ((this.botButtons.isEmpty()) || (this.currentMessageObject.eventId != 0L)) {}
    label212:
    do
    {
      for (;;)
      {
        return false;
        int k = (int)paramMotionEvent.getX();
        int m = (int)paramMotionEvent.getY();
        if (paramMotionEvent.getAction() != 0) {
          break;
        }
        int i;
        int j;
        if (this.currentMessageObject.isOutOwner())
        {
          i = getMeasuredWidth() - this.widthForButtons - AndroidUtilities.dp(10.0F);
          j = 0;
        }
        for (;;)
        {
          if (j >= this.botButtons.size()) {
            break label212;
          }
          paramMotionEvent = (BotButton)this.botButtons.get(j);
          int n = paramMotionEvent.y + this.layoutHeight - AndroidUtilities.dp(2.0F);
          if ((k >= paramMotionEvent.x + i) && (k <= paramMotionEvent.x + i + paramMotionEvent.width) && (m >= n) && (m <= paramMotionEvent.height + n))
          {
            this.pressedBotButton = j;
            invalidate();
            return true;
            i = this.backgroundDrawableLeft;
            if (this.mediaBackground) {}
            for (float f = 1.0F;; f = 7.0F)
            {
              i += AndroidUtilities.dp(f);
              break;
            }
          }
          j += 1;
        }
      }
    } while ((paramMotionEvent.getAction() != 1) || (this.pressedBotButton == -1));
    playSoundEffect(0);
    this.delegate.didPressedBotButton(this, ((BotButton)this.botButtons.get(this.pressedBotButton)).button);
    this.pressedBotButton = -1;
    invalidate();
    return false;
  }
  
  private boolean checkCaptionMotionEvent(MotionEvent paramMotionEvent)
  {
    if ((!(this.currentCaption instanceof Spannable)) || (this.captionLayout == null)) {
      return false;
    }
    int i;
    int j;
    if ((paramMotionEvent.getAction() == 0) || (((this.linkPreviewPressed) || (this.pressedLink != null)) && (paramMotionEvent.getAction() == 1)))
    {
      i = (int)paramMotionEvent.getX();
      j = (int)paramMotionEvent.getY();
      if ((i < this.captionX) || (i > this.captionX + this.captionWidth) || (j < this.captionY) || (j > this.captionY + this.captionHeight)) {
        break label433;
      }
      if (paramMotionEvent.getAction() != 0) {
        break label400;
      }
    }
    for (;;)
    {
      try
      {
        i -= this.captionX;
        int k = this.captionY;
        j = this.captionLayout.getLineForVertical(j - k);
        k = this.captionLayout.getOffsetForHorizontal(j, i);
        float f = this.captionLayout.getLineLeft(j);
        if ((f <= i) && (this.captionLayout.getLineWidth(j) + f >= i))
        {
          Spannable localSpannable = (Spannable)this.currentCaption;
          CharacterStyle[] arrayOfCharacterStyle = (CharacterStyle[])localSpannable.getSpans(k, k, ClickableSpan.class);
          if (arrayOfCharacterStyle != null)
          {
            paramMotionEvent = arrayOfCharacterStyle;
            if (arrayOfCharacterStyle.length != 0) {}
          }
          else
          {
            paramMotionEvent = (CharacterStyle[])localSpannable.getSpans(k, k, URLSpanMono.class);
          }
          j = 0;
          if (paramMotionEvent.length == 0) {
            break label441;
          }
          i = j;
          if (paramMotionEvent.length != 0)
          {
            i = j;
            if ((paramMotionEvent[0] instanceof URLSpanBotCommand))
            {
              i = j;
              if (!URLSpanBotCommand.enabled) {
                break label441;
              }
            }
          }
          if (i == 0)
          {
            this.pressedLink = paramMotionEvent[0];
            this.pressedLinkType = 3;
            resetUrlPaths(false);
            try
            {
              paramMotionEvent = obtainNewUrlPath(false);
              i = localSpannable.getSpanStart(this.pressedLink);
              paramMotionEvent.setCurrentLayout(this.captionLayout, i, 0.0F);
              this.captionLayout.getSelectionPath(i, localSpannable.getSpanEnd(this.pressedLink), paramMotionEvent);
              if ((this.currentMessagesGroup != null) && (getParent() != null)) {
                ((ViewGroup)getParent()).invalidate();
              }
              invalidate();
              return true;
            }
            catch (Exception paramMotionEvent)
            {
              FileLog.e(paramMotionEvent);
              continue;
            }
          }
        }
        return false;
      }
      catch (Exception paramMotionEvent)
      {
        FileLog.e(paramMotionEvent);
      }
      for (;;)
      {
        label400:
        if (this.pressedLinkType == 3)
        {
          this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
          resetPressedLink(3);
          return true;
          label433:
          resetPressedLink(3);
        }
      }
      label441:
      i = 1;
    }
  }
  
  private boolean checkGameMotionEvent(MotionEvent paramMotionEvent)
  {
    if (!this.hasGamePreview) {
      return false;
    }
    int i = (int)paramMotionEvent.getX();
    int j = (int)paramMotionEvent.getY();
    if (paramMotionEvent.getAction() == 0)
    {
      if ((this.drawPhotoImage) && (this.photoImage.isInsideImage(i, j)))
      {
        this.gamePreviewPressed = true;
        return true;
      }
      if ((this.descriptionLayout == null) || (j < this.descriptionY)) {}
    }
    for (;;)
    {
      try
      {
        i -= this.unmovedTextX + AndroidUtilities.dp(10.0F) + this.descriptionX;
        int k = this.descriptionY;
        j = this.descriptionLayout.getLineForVertical(j - k);
        k = this.descriptionLayout.getOffsetForHorizontal(j, i);
        float f = this.descriptionLayout.getLineLeft(j);
        if ((f <= i) && (this.descriptionLayout.getLineWidth(j) + f >= i))
        {
          paramMotionEvent = (Spannable)this.currentMessageObject.linkDescription;
          Object localObject = (ClickableSpan[])paramMotionEvent.getSpans(k, k, ClickableSpan.class);
          j = 0;
          if (localObject.length == 0) {
            break label504;
          }
          i = j;
          if (localObject.length != 0)
          {
            i = j;
            if ((localObject[0] instanceof URLSpanBotCommand))
            {
              i = j;
              if (!URLSpanBotCommand.enabled) {
                break label504;
              }
            }
          }
          if (i == 0)
          {
            this.pressedLink = localObject[0];
            this.linkBlockNum = -10;
            this.pressedLinkType = 2;
            resetUrlPaths(false);
            try
            {
              localObject = obtainNewUrlPath(false);
              i = paramMotionEvent.getSpanStart(this.pressedLink);
              ((LinkPath)localObject).setCurrentLayout(this.descriptionLayout, i, 0.0F);
              this.descriptionLayout.getSelectionPath(i, paramMotionEvent.getSpanEnd(this.pressedLink), (Path)localObject);
              invalidate();
              return true;
            }
            catch (Exception paramMotionEvent)
            {
              FileLog.e(paramMotionEvent);
              continue;
            }
          }
        }
        return false;
      }
      catch (Exception paramMotionEvent)
      {
        FileLog.e(paramMotionEvent);
      }
      for (;;)
      {
        if (paramMotionEvent.getAction() == 1)
        {
          if ((this.pressedLinkType == 2) || (this.gamePreviewPressed))
          {
            if (this.pressedLink != null)
            {
              if ((this.pressedLink instanceof URLSpan)) {
                Browser.openUrl(getContext(), ((URLSpan)this.pressedLink).getURL());
              }
              for (;;)
              {
                resetPressedLink(2);
                break;
                if ((this.pressedLink instanceof ClickableSpan)) {
                  ((ClickableSpan)this.pressedLink).onClick(this);
                }
              }
            }
            this.gamePreviewPressed = false;
            i = 0;
            for (;;)
            {
              if (i < this.botButtons.size())
              {
                paramMotionEvent = (BotButton)this.botButtons.get(i);
                if ((paramMotionEvent.button instanceof TLRPC.TL_keyboardButtonGame))
                {
                  playSoundEffect(0);
                  this.delegate.didPressedBotButton(this, paramMotionEvent.button);
                  invalidate();
                }
              }
              else
              {
                resetPressedLink(2);
                return true;
              }
              i += 1;
            }
          }
          resetPressedLink(2);
        }
      }
      label504:
      i = 1;
    }
  }
  
  private boolean checkLinkPreviewMotionEvent(MotionEvent paramMotionEvent)
  {
    if ((this.currentMessageObject.type != 0) || (!this.hasLinkPreview)) {
      return false;
    }
    int k = (int)paramMotionEvent.getX();
    int m = (int)paramMotionEvent.getY();
    int j;
    int n;
    int i1;
    if ((k >= this.unmovedTextX) && (k <= this.unmovedTextX + this.backgroundWidth) && (m >= this.textY + this.currentMessageObject.textHeight))
    {
      j = this.textY;
      n = this.currentMessageObject.textHeight;
      i1 = this.linkPreviewHeight;
      if (!this.drawInstantView) {
        break label1379;
      }
    }
    label514:
    label860:
    label862:
    label1328:
    label1374:
    label1379:
    for (int i = 46;; i = 0)
    {
      if (m <= AndroidUtilities.dp(i + 8) + (i1 + (j + n))) {
        if (paramMotionEvent.getAction() == 0) {
          if ((this.descriptionLayout == null) || (m < this.descriptionY)) {}
        }
      }
      for (;;)
      {
        try
        {
          i = k - (this.unmovedTextX + AndroidUtilities.dp(10.0F) + this.descriptionX);
          j = m - this.descriptionY;
          if (j <= this.descriptionLayout.getHeight())
          {
            j = this.descriptionLayout.getLineForVertical(j);
            n = this.descriptionLayout.getOffsetForHorizontal(j, i);
            float f = this.descriptionLayout.getLineLeft(j);
            if ((f <= i) && (this.descriptionLayout.getLineWidth(j) + f >= i))
            {
              paramMotionEvent = (Spannable)this.currentMessageObject.linkDescription;
              Object localObject = (ClickableSpan[])paramMotionEvent.getSpans(n, n, ClickableSpan.class);
              j = 0;
              if (localObject.length == 0) {
                break label1374;
              }
              i = j;
              if (localObject.length != 0)
              {
                i = j;
                if ((localObject[0] instanceof URLSpanBotCommand))
                {
                  i = j;
                  if (!URLSpanBotCommand.enabled) {
                    break label1374;
                  }
                }
              }
              if (i == 0)
              {
                this.pressedLink = localObject[0];
                this.linkBlockNum = -10;
                this.pressedLinkType = 2;
                resetUrlPaths(false);
                try
                {
                  localObject = obtainNewUrlPath(false);
                  i = paramMotionEvent.getSpanStart(this.pressedLink);
                  ((LinkPath)localObject).setCurrentLayout(this.descriptionLayout, i, 0.0F);
                  this.descriptionLayout.getSelectionPath(i, paramMotionEvent.getSpanEnd(this.pressedLink), (Path)localObject);
                  invalidate();
                  return true;
                }
                catch (Exception paramMotionEvent)
                {
                  FileLog.e(paramMotionEvent);
                  continue;
                }
              }
            }
          }
          if (this.pressedLink != null) {
            break label860;
          }
        }
        catch (Exception paramMotionEvent)
        {
          FileLog.e(paramMotionEvent);
        }
        j = AndroidUtilities.dp(48.0F);
        i = 0;
        if (this.miniButtonState >= 0)
        {
          i = AndroidUtilities.dp(27.0F);
          if ((k < this.buttonX + i) || (k > this.buttonX + i + j) || (m < this.buttonY + i) || (m > this.buttonY + i + j)) {
            break label514;
          }
        }
        for (i = 1; i != 0; i = 0)
        {
          this.miniButtonPressed = 1;
          invalidate();
          return true;
        }
        if ((this.drawPhotoImage) && (this.drawImageButton) && (this.buttonState != -1) && (k >= this.buttonX) && (k <= this.buttonX + AndroidUtilities.dp(48.0F)) && (m >= this.buttonY) && (m <= this.buttonY + AndroidUtilities.dp(48.0F)))
        {
          this.buttonPressed = 1;
          return true;
        }
        if (this.drawInstantView)
        {
          this.instantPressed = true;
          if ((Build.VERSION.SDK_INT >= 21) && (this.instantViewSelectorDrawable != null) && (this.instantViewSelectorDrawable.getBounds().contains(k, m)))
          {
            this.instantViewSelectorDrawable.setState(this.pressedState);
            this.instantViewSelectorDrawable.setHotspot(k, m);
            this.instantButtonPressed = true;
          }
          invalidate();
          return true;
        }
        if ((this.documentAttachType != 1) && (this.drawPhotoImage) && (this.photoImage.isInsideImage(k, m)))
        {
          this.linkPreviewPressed = true;
          paramMotionEvent = this.currentMessageObject.messageOwner.media.webpage;
          if ((this.documentAttachType == 2) && (this.buttonState == -1) && (SharedConfig.autoplayGifs) && ((this.photoImage.getAnimation() == null) || (!TextUtils.isEmpty(paramMotionEvent.embed_url))))
          {
            this.linkPreviewPressed = false;
            return false;
          }
          return true;
          if (paramMotionEvent.getAction() != 1) {
            break label1328;
          }
          if (!this.instantPressed) {
            break label862;
          }
          if (this.delegate != null) {
            this.delegate.didPressedInstantButton(this, this.drawInstantViewType);
          }
          playSoundEffect(0);
          if ((Build.VERSION.SDK_INT >= 21) && (this.instantViewSelectorDrawable != null)) {
            this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
          }
          this.instantButtonPressed = false;
          this.instantPressed = false;
          invalidate();
        }
        for (;;)
        {
          return false;
          if ((this.pressedLinkType == 2) || (this.buttonPressed != 0) || (this.miniButtonPressed != 0) || (this.linkPreviewPressed))
          {
            if (this.buttonPressed != 0)
            {
              this.buttonPressed = 0;
              playSoundEffect(0);
              didPressedButton(false);
              invalidate();
            }
            else if (this.miniButtonPressed != 0)
            {
              this.miniButtonPressed = 0;
              playSoundEffect(0);
              didPressedMiniButton(false);
              invalidate();
            }
            else
            {
              if (this.pressedLink != null)
              {
                if ((this.pressedLink instanceof URLSpan)) {
                  Browser.openUrl(getContext(), ((URLSpan)this.pressedLink).getURL());
                }
                for (;;)
                {
                  resetPressedLink(2);
                  break;
                  if ((this.pressedLink instanceof ClickableSpan)) {
                    ((ClickableSpan)this.pressedLink).onClick(this);
                  }
                }
              }
              if (this.documentAttachType == 7) {
                if ((!MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) || (MediaController.getInstance().isMessagePaused())) {
                  this.delegate.needPlayMessage(this.currentMessageObject);
                }
              }
              for (;;)
              {
                resetPressedLink(2);
                return true;
                MediaController.getInstance().pauseMessage(this.currentMessageObject);
                continue;
                if ((this.documentAttachType == 2) && (this.drawImageButton))
                {
                  if (this.buttonState == -1)
                  {
                    if (SharedConfig.autoplayGifs)
                    {
                      this.delegate.didPressedImage(this);
                    }
                    else
                    {
                      this.buttonState = 2;
                      this.currentMessageObject.gifState = 1.0F;
                      this.photoImage.setAllowStartAnimation(false);
                      this.photoImage.stopAnimation();
                      this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                      invalidate();
                      playSoundEffect(0);
                    }
                  }
                  else if ((this.buttonState == 2) || (this.buttonState == 0))
                  {
                    didPressedButton(false);
                    playSoundEffect(0);
                  }
                }
                else
                {
                  paramMotionEvent = this.currentMessageObject.messageOwner.media.webpage;
                  if ((paramMotionEvent != null) && (!TextUtils.isEmpty(paramMotionEvent.embed_url)))
                  {
                    this.delegate.needOpenWebView(paramMotionEvent.embed_url, paramMotionEvent.site_name, paramMotionEvent.title, paramMotionEvent.url, paramMotionEvent.embed_width, paramMotionEvent.embed_height);
                  }
                  else if ((this.buttonState == -1) || (this.buttonState == 3))
                  {
                    this.delegate.didPressedImage(this);
                    playSoundEffect(0);
                  }
                  else if (paramMotionEvent != null)
                  {
                    Browser.openUrl(getContext(), paramMotionEvent.url);
                  }
                }
              }
            }
          }
          else
          {
            resetPressedLink(2);
            continue;
            if ((paramMotionEvent.getAction() == 2) && (this.instantButtonPressed) && (Build.VERSION.SDK_INT >= 21) && (this.instantViewSelectorDrawable != null)) {
              this.instantViewSelectorDrawable.setHotspot(k, m);
            }
          }
        }
        i = 1;
      }
    }
  }
  
  private void checkLocationExpired()
  {
    if (this.currentMessageObject == null) {}
    boolean bool;
    do
    {
      return;
      bool = isCurrentLocationTimeExpired(this.currentMessageObject);
    } while (bool == this.locationExpired);
    this.locationExpired = bool;
    if (!this.locationExpired)
    {
      AndroidUtilities.runOnUIThread(this.invalidateRunnable, 1000L);
      this.scheduledInvalidate = true;
      int i = this.backgroundWidth;
      int j = AndroidUtilities.dp(91.0F);
      this.docTitleLayout = new StaticLayout(LocaleController.getString("AttachLiveLocation", 2131493031), Theme.chat_locationTitlePaint, i - j, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      return;
    }
    MessageObject localMessageObject = this.currentMessageObject;
    this.currentMessageObject = null;
    setMessageObject(localMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
  }
  
  private boolean checkNeedDrawShareButton(MessageObject paramMessageObject)
  {
    boolean bool2 = true;
    boolean bool1;
    if ((this.currentPosition != null) && (!this.currentPosition.last)) {
      bool1 = false;
    }
    label354:
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
                  return bool1;
                  if (paramMessageObject.eventId != 0L) {
                    return false;
                  }
                  if ((paramMessageObject.messageOwner.fwd_from != null) && (!paramMessageObject.isOutOwner()) && (paramMessageObject.messageOwner.fwd_from.saved_from_peer != null) && (paramMessageObject.getDialogId() == UserConfig.getInstance(this.currentAccount).getClientUserId()))
                  {
                    this.drwaShareGoIcon = true;
                    return true;
                  }
                  if (paramMessageObject.type == 13) {
                    return false;
                  }
                  if ((paramMessageObject.messageOwner.fwd_from == null) || (paramMessageObject.messageOwner.fwd_from.channel_id == 0)) {
                    break;
                  }
                  bool1 = bool2;
                } while (!paramMessageObject.isOutOwner());
                if (!paramMessageObject.isFromUser()) {
                  break label354;
                }
                if (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty)) || (paramMessageObject.messageOwner.media == null) || (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (!(paramMessageObject.messageOwner.media.webpage instanceof TLRPC.TL_webPage)))) {
                  return false;
                }
                localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id));
                if (localObject == null) {
                  break;
                }
                bool1 = bool2;
              } while (((TLRPC.User)localObject).bot);
              if (paramMessageObject.isOut()) {
                break label420;
              }
              bool1 = bool2;
            } while ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame));
            bool1 = bool2;
          } while ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice));
          if (!paramMessageObject.isMegagroup()) {
            break label420;
          }
          Object localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(paramMessageObject.messageOwner.to_id.channel_id));
          if ((localObject == null) || (((TLRPC.Chat)localObject).username == null) || (((TLRPC.Chat)localObject).username.length() <= 0) || ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))) {
            break;
          }
          bool1 = bool2;
        } while (!(paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo));
        return false;
        if (((paramMessageObject.messageOwner.from_id >= 0) && (!paramMessageObject.messageOwner.post)) || (paramMessageObject.messageOwner.to_id.channel_id == 0)) {
          break label420;
        }
        if (paramMessageObject.messageOwner.via_bot_id != 0) {
          break;
        }
        bool1 = bool2;
      } while (paramMessageObject.messageOwner.reply_to_msg_id == 0);
      bool1 = bool2;
    } while (paramMessageObject.type != 13);
    label420:
    return false;
  }
  
  private boolean checkOtherButtonMotionEvent(MotionEvent paramMotionEvent)
  {
    if (this.currentMessageObject.type == 16)
    {
      i = 1;
      j = i;
      if (i == 0) {
        if (((this.documentAttachType != 1) && (this.currentMessageObject.type != 12) && (this.documentAttachType != 5) && (this.documentAttachType != 4) && (this.documentAttachType != 2) && (this.currentMessageObject.type != 8)) || (this.hasGamePreview) || (this.hasInvoicePreview)) {
          break label103;
        }
      }
    }
    label103:
    for (int j = 1;; j = 0)
    {
      if (j != 0) {
        break label108;
      }
      return false;
      i = 0;
      break;
    }
    label108:
    int i = (int)paramMotionEvent.getX();
    j = (int)paramMotionEvent.getY();
    boolean bool2 = false;
    boolean bool1;
    if (paramMotionEvent.getAction() == 0) {
      if (this.currentMessageObject.type == 16)
      {
        bool1 = bool2;
        if (i >= this.otherX)
        {
          bool1 = bool2;
          if (i <= this.otherX + AndroidUtilities.dp(235.0F))
          {
            bool1 = bool2;
            if (j >= this.otherY - AndroidUtilities.dp(14.0F))
            {
              bool1 = bool2;
              if (j <= this.otherY + AndroidUtilities.dp(50.0F))
              {
                this.otherPressed = true;
                bool1 = true;
                invalidate();
              }
            }
          }
        }
      }
    }
    for (;;)
    {
      return bool1;
      bool1 = bool2;
      if (i >= this.otherX - AndroidUtilities.dp(20.0F))
      {
        bool1 = bool2;
        if (i <= this.otherX + AndroidUtilities.dp(20.0F))
        {
          bool1 = bool2;
          if (j >= this.otherY - AndroidUtilities.dp(4.0F))
          {
            bool1 = bool2;
            if (j <= this.otherY + AndroidUtilities.dp(30.0F))
            {
              this.otherPressed = true;
              bool1 = true;
              invalidate();
              continue;
              bool1 = bool2;
              if (paramMotionEvent.getAction() == 1)
              {
                bool1 = bool2;
                if (this.otherPressed)
                {
                  this.otherPressed = false;
                  playSoundEffect(0);
                  this.delegate.didPressedOther(this);
                  invalidate();
                  bool1 = true;
                }
              }
            }
          }
        }
      }
    }
  }
  
  private boolean checkPhotoImageMotionEvent(MotionEvent paramMotionEvent)
  {
    if ((!this.drawPhotoImage) && (this.documentAttachType != 1)) {
      return false;
    }
    int j = (int)paramMotionEvent.getX();
    int k = (int)paramMotionEvent.getY();
    boolean bool1 = false;
    boolean bool3 = false;
    boolean bool2 = false;
    int i;
    int m;
    if (paramMotionEvent.getAction() == 0)
    {
      i = 0;
      m = AndroidUtilities.dp(48.0F);
      if (this.miniButtonState >= 0)
      {
        i = AndroidUtilities.dp(27.0F);
        if ((j >= this.buttonX + i) && (j <= this.buttonX + i + m) && (k >= this.buttonY + i) && (k <= this.buttonY + i + m)) {
          i = 1;
        }
      }
      else
      {
        if (i == 0) {
          break label173;
        }
        this.miniButtonPressed = 1;
        invalidate();
        bool1 = true;
        label136:
        bool2 = bool1;
        if (this.imagePressed)
        {
          if (!this.currentMessageObject.isSendError()) {
            break label506;
          }
          this.imagePressed = false;
          bool2 = false;
        }
      }
    }
    label173:
    label506:
    label675:
    do
    {
      for (;;)
      {
        return bool2;
        i = 0;
        break;
        if ((this.buttonState != -1) && (j >= this.buttonX) && (j <= this.buttonX + m) && (k >= this.buttonY) && (k <= this.buttonY + m))
        {
          this.buttonPressed = 1;
          invalidate();
          bool1 = true;
          break label136;
        }
        if (this.documentAttachType == 1)
        {
          bool1 = bool2;
          if (j < this.photoImage.getImageX()) {
            break label136;
          }
          bool1 = bool2;
          if (j > this.photoImage.getImageX() + this.backgroundWidth - AndroidUtilities.dp(50.0F)) {
            break label136;
          }
          bool1 = bool2;
          if (k < this.photoImage.getImageY()) {
            break label136;
          }
          bool1 = bool2;
          if (k > this.photoImage.getImageY() + this.photoImage.getImageHeight()) {
            break label136;
          }
          this.imagePressed = true;
          bool1 = true;
          break label136;
        }
        if (this.currentMessageObject.type == 13)
        {
          bool1 = bool2;
          if (this.currentMessageObject.getInputStickerSet() == null) {
            break label136;
          }
        }
        bool2 = bool3;
        if (j >= this.photoImage.getImageX())
        {
          bool2 = bool3;
          if (j <= this.photoImage.getImageX() + this.backgroundWidth)
          {
            bool2 = bool3;
            if (k >= this.photoImage.getImageY())
            {
              bool2 = bool3;
              if (k <= this.photoImage.getImageY() + this.photoImage.getImageHeight())
              {
                this.imagePressed = true;
                bool2 = true;
              }
            }
          }
        }
        bool1 = bool2;
        if (this.currentMessageObject.type != 12) {
          break label136;
        }
        bool1 = bool2;
        if (MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id)) != null) {
          break label136;
        }
        this.imagePressed = false;
        bool1 = false;
        break label136;
        if ((this.currentMessageObject.type == 8) && (this.buttonState == -1) && (SharedConfig.autoplayGifs) && (this.photoImage.getAnimation() == null))
        {
          this.imagePressed = false;
          bool2 = false;
        }
        else
        {
          bool2 = bool1;
          if (this.currentMessageObject.type == 5)
          {
            bool2 = bool1;
            if (this.buttonState != -1)
            {
              this.imagePressed = false;
              bool2 = false;
              continue;
              bool2 = bool1;
              if (paramMotionEvent.getAction() == 1) {
                if (this.buttonPressed == 1)
                {
                  this.buttonPressed = 0;
                  playSoundEffect(0);
                  didPressedButton(false);
                  updateRadialProgressBackground();
                  invalidate();
                  bool2 = bool1;
                }
                else
                {
                  if (this.miniButtonPressed != 1) {
                    break label675;
                  }
                  this.miniButtonPressed = 0;
                  playSoundEffect(0);
                  didPressedMiniButton(false);
                  invalidate();
                  bool2 = bool1;
                }
              }
            }
          }
        }
      }
      bool2 = bool1;
    } while (!this.imagePressed);
    this.imagePressed = false;
    if ((this.buttonState == -1) || (this.buttonState == 2) || (this.buttonState == 3))
    {
      playSoundEffect(0);
      didClickedImage();
    }
    for (;;)
    {
      invalidate();
      bool2 = bool1;
      break;
      if ((this.buttonState == 0) && (this.documentAttachType == 1))
      {
        playSoundEffect(0);
        didPressedButton(false);
      }
    }
  }
  
  private boolean checkTextBlockMotionEvent(MotionEvent paramMotionEvent)
  {
    if ((this.currentMessageObject.type != 0) || (this.currentMessageObject.textLayoutBlocks == null) || (this.currentMessageObject.textLayoutBlocks.isEmpty()) || (!(this.currentMessageObject.messageText instanceof Spannable))) {
      return false;
    }
    int k;
    int i;
    int m;
    int j;
    if ((paramMotionEvent.getAction() == 0) || ((paramMotionEvent.getAction() == 1) && (this.pressedLinkType == 1)))
    {
      k = (int)paramMotionEvent.getX();
      i = (int)paramMotionEvent.getY();
      if ((k < this.textX) || (i < this.textY) || (k > this.textX + this.currentMessageObject.textWidth) || (i > this.textY + this.currentMessageObject.textHeight)) {
        break label977;
      }
      m = i - this.textY;
      j = 0;
      i = 0;
      if ((i < this.currentMessageObject.textLayoutBlocks.size()) && (((MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(i)).textYOffset <= m)) {}
    }
    for (;;)
    {
      Object localObject1;
      try
      {
        MessageObject.TextLayoutBlock localTextLayoutBlock = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(j);
        float f2 = k;
        float f3 = this.textX;
        float f1;
        Spannable localSpannable;
        Object localObject2;
        if (localTextLayoutBlock.isRtl())
        {
          f1 = this.currentMessageObject.textXOffset;
          i = (int)(f2 - (f3 - f1));
          k = (int)(m - localTextLayoutBlock.textYOffset);
          m = localTextLayoutBlock.textLayout.getLineForVertical(k);
          k = localTextLayoutBlock.textLayout.getOffsetForHorizontal(m, i);
          f1 = localTextLayoutBlock.textLayout.getLineLeft(m);
          if ((f1 <= i) && (localTextLayoutBlock.textLayout.getLineWidth(m) + f1 >= i))
          {
            localSpannable = (Spannable)this.currentMessageObject.messageText;
            localObject2 = (CharacterStyle[])localSpannable.getSpans(k, k, ClickableSpan.class);
            i = 0;
            if (localObject2 != null)
            {
              localObject1 = localObject2;
              if (localObject2.length != 0) {}
            }
            else
            {
              localObject1 = (CharacterStyle[])localSpannable.getSpans(k, k, URLSpanMono.class);
              i = 1;
            }
            m = 0;
            if (localObject1.length == 0) {
              break label985;
            }
            k = m;
            if (localObject1.length != 0)
            {
              k = m;
              if ((localObject1[0] instanceof URLSpanBotCommand))
              {
                k = m;
                if (!URLSpanBotCommand.enabled) {
                  break label985;
                }
              }
            }
            if (k == 0)
            {
              if (paramMotionEvent.getAction() != 0) {
                break label941;
              }
              this.pressedLink = localObject1[0];
              this.linkBlockNum = j;
              this.pressedLinkType = 1;
              resetUrlPaths(false);
            }
          }
        }
        else
        {
          try
          {
            paramMotionEvent = obtainNewUrlPath(false);
            m = localSpannable.getSpanStart(this.pressedLink);
            n = localSpannable.getSpanEnd(this.pressedLink);
            paramMotionEvent.setCurrentLayout(localTextLayoutBlock.textLayout, m, 0.0F);
            localTextLayoutBlock.textLayout.getSelectionPath(m, n, paramMotionEvent);
            if (n >= localTextLayoutBlock.charactersEnd)
            {
              k = j + 1;
              if (k < this.currentMessageObject.textLayoutBlocks.size())
              {
                localObject1 = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(k);
                int i1 = ((MessageObject.TextLayoutBlock)localObject1).charactersOffset;
                int i2 = ((MessageObject.TextLayoutBlock)localObject1).charactersOffset;
                if (i == 0) {
                  continue;
                }
                paramMotionEvent = URLSpanMono.class;
                paramMotionEvent = (CharacterStyle[])localSpannable.getSpans(i1, i2, paramMotionEvent);
                if ((paramMotionEvent != null) && (paramMotionEvent.length != 0) && (paramMotionEvent[0] == this.pressedLink)) {
                  continue;
                }
              }
            }
            if (m <= localTextLayoutBlock.charactersOffset)
            {
              k = 0;
              j -= 1;
              if (j >= 0)
              {
                localObject1 = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(j);
                m = ((MessageObject.TextLayoutBlock)localObject1).charactersEnd;
                n = ((MessageObject.TextLayoutBlock)localObject1).charactersEnd;
                if (i == 0) {
                  break label991;
                }
                paramMotionEvent = URLSpanMono.class;
                paramMotionEvent = (CharacterStyle[])localSpannable.getSpans(m - 1, n - 1, paramMotionEvent);
                if ((paramMotionEvent != null) && (paramMotionEvent.length != 0))
                {
                  paramMotionEvent = paramMotionEvent[0];
                  localObject2 = this.pressedLink;
                  if (paramMotionEvent == localObject2) {
                    continue;
                  }
                }
              }
            }
          }
          catch (Exception paramMotionEvent)
          {
            int n;
            FileLog.e(paramMotionEvent);
            continue;
          }
          invalidate();
          return true;
          j = i;
          i += 1;
          break;
          f1 = 0.0F;
          continue;
          paramMotionEvent = ClickableSpan.class;
          continue;
          paramMotionEvent = obtainNewUrlPath(false);
          paramMotionEvent.setCurrentLayout(((MessageObject.TextLayoutBlock)localObject1).textLayout, 0, ((MessageObject.TextLayoutBlock)localObject1).textYOffset - localTextLayoutBlock.textYOffset);
          ((MessageObject.TextLayoutBlock)localObject1).textLayout.getSelectionPath(0, n, paramMotionEvent);
          if (n < ((MessageObject.TextLayoutBlock)localObject1).charactersEnd - 1) {
            continue;
          }
          k += 1;
          continue;
          paramMotionEvent = obtainNewUrlPath(false);
          m = localSpannable.getSpanStart(this.pressedLink);
          k -= ((MessageObject.TextLayoutBlock)localObject1).height;
          paramMotionEvent.setCurrentLayout(((MessageObject.TextLayoutBlock)localObject1).textLayout, m, k);
          ((MessageObject.TextLayoutBlock)localObject1).textLayout.getSelectionPath(m, localSpannable.getSpanEnd(this.pressedLink), paramMotionEvent);
          n = ((MessageObject.TextLayoutBlock)localObject1).charactersOffset;
          if (m > n) {
            continue;
          }
          j -= 1;
          continue;
        }
        return false;
      }
      catch (Exception paramMotionEvent)
      {
        FileLog.e(paramMotionEvent);
      }
      for (;;)
      {
        label941:
        if (localObject1[0] == this.pressedLink)
        {
          this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, false);
          resetPressedLink(1);
          return true;
          label977:
          resetPressedLink(1);
        }
      }
      label985:
      k = 1;
      continue;
      label991:
      paramMotionEvent = ClickableSpan.class;
    }
  }
  
  private int createDocumentLayout(int paramInt, MessageObject paramMessageObject)
  {
    if (paramMessageObject.type == 0) {}
    for (this.documentAttach = paramMessageObject.messageOwner.media.webpage.document; this.documentAttach == null; this.documentAttach = paramMessageObject.messageOwner.media.document) {
      return 0;
    }
    int j;
    int i;
    Object localObject1;
    if (MessageObject.isVoiceDocument(this.documentAttach))
    {
      this.documentAttachType = 3;
      int k = 0;
      j = 0;
      for (;;)
      {
        i = k;
        if (j < this.documentAttach.attributes.size())
        {
          localObject1 = (TLRPC.DocumentAttribute)this.documentAttach.attributes.get(j);
          if ((localObject1 instanceof TLRPC.TL_documentAttributeAudio)) {
            i = ((TLRPC.DocumentAttribute)localObject1).duration;
          }
        }
        else
        {
          this.widthBeforeNewTimeLine = (paramInt - AndroidUtilities.dp(94.0F) - (int)Math.ceil(Theme.chat_audioTimePaint.measureText("00:00")));
          this.availableTimeWidth = (paramInt - AndroidUtilities.dp(18.0F));
          measureTime(paramMessageObject);
          j = AndroidUtilities.dp(174.0F);
          k = this.timeWidth;
          if (!this.hasLinkPreview) {
            this.backgroundWidth = Math.min(paramInt, AndroidUtilities.dp(10.0F) * i + (j + k));
          }
          this.seekBarWaveform.setMessageObject(paramMessageObject);
          return 0;
        }
        j += 1;
      }
    }
    if (MessageObject.isMusicDocument(this.documentAttach))
    {
      this.documentAttachType = 5;
      paramInt -= AndroidUtilities.dp(86.0F);
      this.songLayout = new StaticLayout(TextUtils.ellipsize(paramMessageObject.getMusicTitle().replace('\n', ' '), Theme.chat_audioTitlePaint, paramInt - AndroidUtilities.dp(12.0F), TextUtils.TruncateAt.END), Theme.chat_audioTitlePaint, paramInt, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      if (this.songLayout.getLineCount() > 0) {
        this.songX = (-(int)Math.ceil(this.songLayout.getLineLeft(0)));
      }
      this.performerLayout = new StaticLayout(TextUtils.ellipsize(paramMessageObject.getMusicAuthor().replace('\n', ' '), Theme.chat_audioPerformerPaint, paramInt, TextUtils.TruncateAt.END), Theme.chat_audioPerformerPaint, paramInt, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      if (this.performerLayout.getLineCount() > 0) {
        this.performerX = (-(int)Math.ceil(this.performerLayout.getLineLeft(0)));
      }
      j = 0;
      paramInt = 0;
      for (;;)
      {
        i = j;
        if (paramInt < this.documentAttach.attributes.size())
        {
          paramMessageObject = (TLRPC.DocumentAttribute)this.documentAttach.attributes.get(paramInt);
          if ((paramMessageObject instanceof TLRPC.TL_documentAttributeAudio)) {
            i = paramMessageObject.duration;
          }
        }
        else
        {
          paramInt = (int)Math.ceil(Theme.chat_audioTimePaint.measureText(String.format("%d:%02d / %d:%02d", new Object[] { Integer.valueOf(i / 60), Integer.valueOf(i % 60), Integer.valueOf(i / 60), Integer.valueOf(i % 60) })));
          this.widthBeforeNewTimeLine = (this.backgroundWidth - AndroidUtilities.dp(86.0F) - paramInt);
          this.availableTimeWidth = (this.backgroundWidth - AndroidUtilities.dp(28.0F));
          return paramInt;
        }
        paramInt += 1;
      }
    }
    if (MessageObject.isVideoDocument(this.documentAttach))
    {
      this.documentAttachType = 4;
      if (!paramMessageObject.needDrawBluredPreview())
      {
        j = 0;
        i = 0;
      }
      for (;;)
      {
        paramInt = j;
        if (i < this.documentAttach.attributes.size())
        {
          paramMessageObject = (TLRPC.DocumentAttribute)this.documentAttach.attributes.get(i);
          if ((paramMessageObject instanceof TLRPC.TL_documentAttributeVideo)) {
            paramInt = paramMessageObject.duration;
          }
        }
        else
        {
          i = paramInt / 60;
          paramMessageObject = String.format("%d:%02d, %s", new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt - i * 60), AndroidUtilities.formatFileSize(this.documentAttach.size) });
          this.infoWidth = ((int)Math.ceil(Theme.chat_infoPaint.measureText(paramMessageObject)));
          this.infoLayout = new StaticLayout(paramMessageObject, Theme.chat_infoPaint, this.infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
          return 0;
        }
        i += 1;
      }
    }
    boolean bool;
    Object localObject2;
    Layout.Alignment localAlignment;
    TextUtils.TruncateAt localTruncateAt;
    if (((this.documentAttach.mime_type != null) && (this.documentAttach.mime_type.toLowerCase().startsWith("image/"))) || ((this.documentAttach.thumb != null) && (!(this.documentAttach.thumb instanceof TLRPC.TL_photoSizeEmpty)) && (!(this.documentAttach.thumb.location instanceof TLRPC.TL_fileLocationUnavailable))))
    {
      bool = true;
      this.drawPhotoImage = bool;
      i = paramInt;
      if (!this.drawPhotoImage) {
        i = paramInt + AndroidUtilities.dp(30.0F);
      }
      this.documentAttachType = 1;
      localObject2 = FileLoader.getDocumentFileName(this.documentAttach);
      if (localObject2 != null)
      {
        localObject1 = localObject2;
        if (((String)localObject2).length() != 0) {}
      }
      else
      {
        localObject1 = LocaleController.getString("AttachDocument", 2131493026);
      }
      localObject2 = Theme.chat_docNamePaint;
      localAlignment = Layout.Alignment.ALIGN_NORMAL;
      localTruncateAt = TextUtils.TruncateAt.MIDDLE;
      if (!this.drawPhotoImage) {
        break label1001;
      }
    }
    label1001:
    for (paramInt = 2;; paramInt = 1)
    {
      this.docTitleLayout = StaticLayoutEx.createStaticLayout((CharSequence)localObject1, (TextPaint)localObject2, i, localAlignment, 1.0F, 0.0F, false, localTruncateAt, i, paramInt);
      this.docTitleOffsetX = Integer.MIN_VALUE;
      if ((this.docTitleLayout == null) || (this.docTitleLayout.getLineCount() <= 0)) {
        break label1230;
      }
      j = 0;
      paramInt = 0;
      while (paramInt < this.docTitleLayout.getLineCount())
      {
        j = Math.max(j, (int)Math.ceil(this.docTitleLayout.getLineWidth(paramInt)));
        this.docTitleOffsetX = Math.max(this.docTitleOffsetX, (int)Math.ceil(-this.docTitleLayout.getLineLeft(paramInt)));
        paramInt += 1;
      }
      bool = false;
      break;
    }
    paramInt = Math.min(i, j);
    for (;;)
    {
      localObject1 = AndroidUtilities.formatFileSize(this.documentAttach.size) + " " + FileLoader.getDocumentExtension(this.documentAttach);
      this.infoWidth = Math.min(i - AndroidUtilities.dp(30.0F), (int)Math.ceil(Theme.chat_infoPaint.measureText((String)localObject1)));
      localObject1 = TextUtils.ellipsize((CharSequence)localObject1, Theme.chat_infoPaint, this.infoWidth, TextUtils.TruncateAt.END);
      try
      {
        if (this.infoWidth < 0) {
          this.infoWidth = AndroidUtilities.dp(10.0F);
        }
        this.infoLayout = new StaticLayout((CharSequence)localObject1, Theme.chat_infoPaint, this.infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      }
      catch (Exception localException)
      {
        for (;;)
        {
          label1230:
          FileLog.e(localException);
          continue;
          this.photoImage.setImageBitmap((BitmapDrawable)null);
        }
      }
      if (this.drawPhotoImage)
      {
        this.currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(paramMessageObject.photoThumbs, AndroidUtilities.getPhotoSize());
        this.photoImage.setNeedsQualityThumb(true);
        this.photoImage.setShouldGenerateQualityThumb(true);
        this.photoImage.setParentMessageObject(paramMessageObject);
        if (this.currentPhotoObject == null) {
          break;
        }
        this.currentPhotoFilter = "86_86_b";
        this.photoImage.setImage(null, null, null, null, this.currentPhotoObject.location, this.currentPhotoFilter, 0, null, 1);
      }
      return paramInt;
      paramInt = i;
      this.docTitleOffsetX = 0;
    }
  }
  
  private void didClickedImage()
  {
    if ((this.currentMessageObject.type == 1) || (this.currentMessageObject.type == 13)) {
      if (this.buttonState == -1) {
        this.delegate.didPressedImage(this);
      }
    }
    label41:
    do
    {
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
                break label41;
                break label41;
                break label41;
                break label41;
                do
                {
                  return;
                } while (this.buttonState != 0);
                didPressedButton(false);
                return;
                if (this.currentMessageObject.type == 12)
                {
                  localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.media.user_id));
                  this.delegate.didPressedUserAvatar(this, (TLRPC.User)localObject);
                  return;
                }
                if (this.currentMessageObject.type == 5)
                {
                  if ((!MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) || (MediaController.getInstance().isMessagePaused()))
                  {
                    this.delegate.needPlayMessage(this.currentMessageObject);
                    return;
                  }
                  MediaController.getInstance().pauseMessage(this.currentMessageObject);
                  return;
                }
                if (this.currentMessageObject.type != 8) {
                  break;
                }
                if (this.buttonState == -1)
                {
                  if (SharedConfig.autoplayGifs)
                  {
                    this.delegate.didPressedImage(this);
                    return;
                  }
                  this.buttonState = 2;
                  this.currentMessageObject.gifState = 1.0F;
                  this.photoImage.setAllowStartAnimation(false);
                  this.photoImage.stopAnimation();
                  this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
                  invalidate();
                  return;
                }
              } while ((this.buttonState != 2) && (this.buttonState != 0));
              didPressedButton(false);
              return;
              if (this.documentAttachType != 4) {
                break;
              }
              if (this.buttonState == -1)
              {
                this.delegate.didPressedImage(this);
                return;
              }
            } while ((this.buttonState != 0) && (this.buttonState != 3));
            didPressedButton(false);
            return;
            if (this.currentMessageObject.type == 4)
            {
              this.delegate.didPressedImage(this);
              return;
            }
            if (this.documentAttachType != 1) {
              break;
            }
          } while (this.buttonState != -1);
          this.delegate.didPressedImage(this);
          return;
          if (this.documentAttachType != 2) {
            break;
          }
        } while (this.buttonState != -1);
        localObject = this.currentMessageObject.messageOwner.media.webpage;
      } while (localObject == null);
      if ((((TLRPC.WebPage)localObject).embed_url != null) && (((TLRPC.WebPage)localObject).embed_url.length() != 0))
      {
        this.delegate.needOpenWebView(((TLRPC.WebPage)localObject).embed_url, ((TLRPC.WebPage)localObject).site_name, ((TLRPC.WebPage)localObject).description, ((TLRPC.WebPage)localObject).url, ((TLRPC.WebPage)localObject).embed_width, ((TLRPC.WebPage)localObject).embed_height);
        return;
      }
      Browser.openUrl(getContext(), ((TLRPC.WebPage)localObject).url);
      return;
    } while ((!this.hasInvoicePreview) || (this.buttonState != -1));
    this.delegate.didPressedImage(this);
  }
  
  private void didPressedButton(boolean paramBoolean)
  {
    if (this.buttonState == 0) {
      if ((this.documentAttachType == 3) || (this.documentAttachType == 5))
      {
        if (this.miniButtonState == 0) {
          FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
        }
        if (this.delegate.needPlayMessage(this.currentMessageObject))
        {
          if ((this.hasMiniProgress == 2) && (this.miniButtonState != 1))
          {
            this.miniButtonState = 1;
            this.radialProgress.setProgress(0.0F, false);
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
          }
          updatePlayingMessageProgress();
          this.buttonState = 1;
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
          invalidate();
        }
      }
    }
    label228:
    label273:
    label734:
    label1067:
    do
    {
      do
      {
        do
        {
          do
          {
            return;
            this.cancelLoading = false;
            this.radialProgress.setProgress(0.0F, false);
            int i;
            if (this.currentMessageObject.type == 1)
            {
              this.photoImage.setForceLoading(true);
              localObject2 = this.photoImage;
              localObject3 = this.currentPhotoObject.location;
              str1 = this.currentPhotoFilter;
              if (this.currentPhotoObjectThumb != null)
              {
                localObject1 = this.currentPhotoObjectThumb.location;
                String str2 = this.currentPhotoFilterThumb;
                int j = this.currentPhotoObject.size;
                if (!this.currentMessageObject.shouldEncryptPhotoOrVideo()) {
                  break label273;
                }
                i = 2;
                ((ImageReceiver)localObject2).setImage((TLObject)localObject3, str1, (TLRPC.FileLocation)localObject1, str2, j, null, i);
              }
            }
            for (;;)
            {
              this.buttonState = 1;
              this.radialProgress.setBackground(getDrawableForCurrentState(), true, paramBoolean);
              invalidate();
              return;
              localObject1 = null;
              break;
              i = 0;
              break label228;
              if (this.currentMessageObject.type == 8)
              {
                this.currentMessageObject.gifState = 2.0F;
                this.photoImage.setForceLoading(true);
                localObject2 = this.photoImage;
                localObject3 = this.currentMessageObject.messageOwner.media.document;
                if (this.currentPhotoObject != null) {}
                for (localObject1 = this.currentPhotoObject.location;; localObject1 = null)
                {
                  ((ImageReceiver)localObject2).setImage((TLObject)localObject3, null, (TLRPC.FileLocation)localObject1, this.currentPhotoFilterThumb, this.currentMessageObject.messageOwner.media.document.size, null, 0);
                  break;
                }
              }
              if (this.currentMessageObject.isRoundVideo())
              {
                if (this.currentMessageObject.isSecretMedia())
                {
                  FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.getDocument(), true, 1);
                }
                else
                {
                  this.currentMessageObject.gifState = 2.0F;
                  localObject2 = this.currentMessageObject.getDocument();
                  this.photoImage.setForceLoading(true);
                  localObject3 = this.photoImage;
                  if (this.currentPhotoObject != null) {}
                  for (localObject1 = this.currentPhotoObject.location;; localObject1 = null)
                  {
                    ((ImageReceiver)localObject3).setImage((TLObject)localObject2, null, (TLRPC.FileLocation)localObject1, this.currentPhotoFilterThumb, ((TLRPC.Document)localObject2).size, null, 0);
                    break;
                  }
                }
              }
              else if (this.currentMessageObject.type == 9)
              {
                FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.messageOwner.media.document, false, 0);
              }
              else
              {
                if (this.documentAttachType == 4)
                {
                  localObject1 = FileLoader.getInstance(this.currentAccount);
                  localObject2 = this.documentAttach;
                  if (this.currentMessageObject.shouldEncryptPhotoOrVideo()) {}
                  for (i = 2;; i = 0)
                  {
                    ((FileLoader)localObject1).loadFile((TLRPC.Document)localObject2, true, i);
                    break;
                  }
                }
                if ((this.currentMessageObject.type != 0) || (this.documentAttachType == 0)) {
                  break label734;
                }
                if (this.documentAttachType == 2)
                {
                  this.photoImage.setForceLoading(true);
                  this.photoImage.setImage(this.currentMessageObject.messageOwner.media.webpage.document, null, this.currentPhotoObject.location, this.currentPhotoFilterThumb, this.currentMessageObject.messageOwner.media.webpage.document.size, null, 0);
                  this.currentMessageObject.gifState = 2.0F;
                }
                else if (this.documentAttachType == 1)
                {
                  FileLoader.getInstance(this.currentAccount).loadFile(this.currentMessageObject.messageOwner.media.webpage.document, false, 0);
                }
              }
            }
            this.photoImage.setForceLoading(true);
            Object localObject2 = this.photoImage;
            Object localObject3 = this.currentPhotoObject.location;
            String str1 = this.currentPhotoFilter;
            if (this.currentPhotoObjectThumb != null) {}
            for (Object localObject1 = this.currentPhotoObjectThumb.location;; localObject1 = null)
            {
              ((ImageReceiver)localObject2).setImage((TLObject)localObject3, str1, (TLRPC.FileLocation)localObject1, this.currentPhotoFilterThumb, 0, null, 0);
              break;
            }
            if (this.buttonState != 1) {
              break label1067;
            }
            if ((this.documentAttachType != 3) && (this.documentAttachType != 5)) {
              break;
            }
          } while (!MediaController.getInstance().pauseMessage(this.currentMessageObject));
          this.buttonState = 0;
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
          invalidate();
          return;
          if ((!this.currentMessageObject.isOut()) || (!this.currentMessageObject.isSending())) {
            break;
          }
        } while (this.radialProgress.isDrawCheckDrawable());
        this.delegate.didPressedCancelSendButton(this);
        return;
        this.cancelLoading = true;
        if ((this.documentAttachType == 4) || (this.documentAttachType == 1)) {
          FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
        }
        for (;;)
        {
          this.buttonState = 0;
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
          invalidate();
          return;
          if ((this.currentMessageObject.type == 0) || (this.currentMessageObject.type == 1) || (this.currentMessageObject.type == 8) || (this.currentMessageObject.type == 5))
          {
            ImageLoader.getInstance().cancelForceLoadingForImageReceiver(this.photoImage);
            this.photoImage.cancelLoadImage();
          }
          else if (this.currentMessageObject.type == 9)
          {
            FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.currentMessageObject.messageOwner.media.document);
          }
        }
        if (this.buttonState == 2)
        {
          if ((this.documentAttachType == 3) || (this.documentAttachType == 5))
          {
            this.radialProgress.setProgress(0.0F, false);
            FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
            this.buttonState = 4;
            this.radialProgress.setBackground(getDrawableForCurrentState(), true, false);
            invalidate();
            return;
          }
          this.photoImage.setAllowStartAnimation(true);
          this.photoImage.startAnimation();
          this.currentMessageObject.gifState = 0.0F;
          this.buttonState = -1;
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
          return;
        }
        if (this.buttonState == 3)
        {
          if ((this.hasMiniProgress == 2) && (this.miniButtonState != 1))
          {
            this.miniButtonState = 1;
            this.radialProgress.setProgress(0.0F, false);
            this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
          }
          this.delegate.didPressedImage(this);
          return;
        }
      } while ((this.buttonState != 4) || ((this.documentAttachType != 3) && (this.documentAttachType != 5)));
      if (((!this.currentMessageObject.isOut()) || (!this.currentMessageObject.isSending())) && (!this.currentMessageObject.isSendError())) {
        break;
      }
    } while (this.delegate == null);
    this.delegate.didPressedCancelSendButton(this);
    return;
    FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
    this.buttonState = 2;
    this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
    invalidate();
  }
  
  private void didPressedMiniButton(boolean paramBoolean)
  {
    if (this.miniButtonState == 0)
    {
      this.miniButtonState = 1;
      this.radialProgress.setProgress(0.0F, false);
      if ((this.documentAttachType == 3) || (this.documentAttachType == 5)) {
        FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
      }
    }
    while (this.miniButtonState != 1)
    {
      do
      {
        this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
        invalidate();
        return;
      } while (this.documentAttachType != 4);
      FileLoader localFileLoader = FileLoader.getInstance(this.currentAccount);
      TLRPC.Document localDocument = this.documentAttach;
      if (this.currentMessageObject.shouldEncryptPhotoOrVideo()) {}
      for (int i = 2;; i = 0)
      {
        localFileLoader.loadFile(localDocument, true, i);
        break;
      }
    }
    if (((this.documentAttachType == 3) || (this.documentAttachType == 5)) && (MediaController.getInstance().isPlayingMessage(this.currentMessageObject))) {
      MediaController.getInstance().cleanupPlayer(true, true);
    }
    this.miniButtonState = 0;
    FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.documentAttach);
    this.radialProgress.setMiniBackground(getMiniDrawableForCurrentState(), true, false);
    invalidate();
  }
  
  private void drawContent(Canvas paramCanvas)
  {
    if ((this.needNewVisiblePart) && (this.currentMessageObject.type == 0))
    {
      getLocalVisibleRect(this.scrollRect);
      setVisiblePart(this.scrollRect.top, this.scrollRect.bottom - this.scrollRect.top);
      this.needNewVisiblePart = false;
    }
    Object localObject1;
    label95:
    label133:
    label161:
    boolean bool2;
    boolean bool3;
    label301:
    int j;
    label378:
    label542:
    int n;
    int i2;
    Object localObject3;
    label575:
    label659:
    int i1;
    if (this.currentMessagesGroup != null)
    {
      bool1 = true;
      this.forceNotDrawTime = bool1;
      localObject1 = this.photoImage;
      if (!isDrawSelectedBackground()) {
        break label1450;
      }
      if (this.currentPosition == null) {
        break label1445;
      }
      i = 2;
      ((ImageReceiver)localObject1).setPressed(i);
      localObject1 = this.photoImage;
      if ((PhotoViewer.isShowingImage(this.currentMessageObject)) || (SecretMediaViewer.getInstance().isShowingImage(this.currentMessageObject))) {
        break label1455;
      }
      bool1 = true;
      ((ImageReceiver)localObject1).setVisible(bool1, false);
      if (this.photoImage.getVisible()) {
        break label1461;
      }
      this.mediaWasInvisible = true;
      this.timeWasInvisible = true;
      this.radialProgress.setHideCurrentDrawable(false);
      this.radialProgress.setProgressColor(Theme.getColor("chat_mediaProgress"));
      bool2 = false;
      bool3 = false;
      bool1 = false;
      if (this.currentMessageObject.type != 0) {
        break label5560;
      }
      if (!this.currentMessageObject.isOutOwner()) {
        break label1539;
      }
      this.textX = (this.currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11.0F));
      if (!this.hasGamePreview) {
        break label1588;
      }
      this.textX += AndroidUtilities.dp(11.0F);
      this.textY = (AndroidUtilities.dp(14.0F) + this.namesOffset);
      if (this.siteNameLayout != null) {
        this.textY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
      }
      this.unmovedTextX = this.textX;
      if ((this.currentMessageObject.textXOffset != 0.0F) && (this.replyNameLayout != null))
      {
        j = this.backgroundWidth - AndroidUtilities.dp(31.0F) - this.currentMessageObject.textWidth;
        i = j;
        if (!this.hasNewLineForTime)
        {
          k = this.timeWidth;
          if (!this.currentMessageObject.isOutOwner()) {
            break label1663;
          }
          i = 20;
          i = j - (AndroidUtilities.dp(i + 4) + k);
        }
        if (i > 0) {
          this.textX += i;
        }
      }
      if ((this.currentMessageObject.textLayoutBlocks != null) && (!this.currentMessageObject.textLayoutBlocks.isEmpty()))
      {
        if (this.fullyDraw)
        {
          this.firstVisibleBlockNum = 0;
          this.lastVisibleBlockNum = this.currentMessageObject.textLayoutBlocks.size();
        }
        if (this.firstVisibleBlockNum >= 0)
        {
          i = this.firstVisibleBlockNum;
          if ((i <= this.lastVisibleBlockNum) && (i < this.currentMessageObject.textLayoutBlocks.size())) {
            break label1668;
          }
        }
      }
      if ((!this.hasLinkPreview) && (!this.hasGamePreview) && (!this.hasInvoicePreview)) {
        break label2911;
      }
      if (!this.hasGamePreview) {
        break label1893;
      }
      i = AndroidUtilities.dp(14.0F) + this.namesOffset;
      j = this.unmovedTextX - AndroidUtilities.dp(10.0F);
      n = i;
      i2 = 0;
      if (!this.hasInvoicePreview)
      {
        localObject3 = Theme.chat_replyLinePaint;
        if (!this.currentMessageObject.isOutOwner()) {
          break label1960;
        }
        localObject1 = "chat_outPreviewLine";
        ((Paint)localObject3).setColor(Theme.getColor((String)localObject1));
        paramCanvas.drawRect(j, n - AndroidUtilities.dp(3.0F), AndroidUtilities.dp(2.0F) + j, this.linkPreviewHeight + n + AndroidUtilities.dp(3.0F), Theme.chat_replyLinePaint);
      }
      m = n;
      if (this.siteNameLayout != null)
      {
        localObject3 = Theme.chat_replyNamePaint;
        if (!this.currentMessageObject.isOutOwner()) {
          break label1968;
        }
        localObject1 = "chat_outSiteNameText";
        ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject1));
        paramCanvas.save();
        if (!this.siteNameRtl) {
          break label1976;
        }
        k = this.backgroundWidth - this.siteNameWidth - AndroidUtilities.dp(32.0F);
        paramCanvas.translate(j + k, n - AndroidUtilities.dp(3.0F));
        this.siteNameLayout.draw(paramCanvas);
        paramCanvas.restore();
        m = n + this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
      }
      if (!this.hasGamePreview)
      {
        k = m;
        i1 = i;
        if (!this.hasInvoicePreview) {}
      }
      else
      {
        k = m;
        i1 = i;
        if (this.currentMessageObject.textHeight != 0)
        {
          i1 = i + (this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0F));
          k = m + (this.currentMessageObject.textHeight + AndroidUtilities.dp(4.0F));
        }
      }
      bool2 = bool1;
      i = k;
      if (this.drawPhotoImage)
      {
        bool2 = bool1;
        i = k;
        if (this.drawInstantView)
        {
          i = k;
          if (k != i1) {
            i = k + AndroidUtilities.dp(2.0F);
          }
          this.photoImage.setImageCoords(AndroidUtilities.dp(10.0F) + j, i, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
          if (this.drawImageButton)
          {
            k = AndroidUtilities.dp(48.0F);
            this.buttonX = ((int)(this.photoImage.getImageX() + (this.photoImage.getImageWidth() - k) / 2.0F));
            this.buttonY = ((int)(this.photoImage.getImageY() + (this.photoImage.getImageHeight() - k) / 2.0F));
            this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + k, this.buttonY + k);
          }
          bool2 = this.photoImage.draw(paramCanvas);
          i += this.photoImage.getImageHeight() + AndroidUtilities.dp(6.0F);
        }
      }
      if (!this.currentMessageObject.isOutOwner()) {
        break label2000;
      }
      Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_messageTextOut"));
      Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
      label1063:
      m = i;
      k = i2;
      if (this.titleLayout != null)
      {
        k = i;
        if (i != i1) {
          k = i + AndroidUtilities.dp(2.0F);
        }
        i = k - AndroidUtilities.dp(1.0F);
        paramCanvas.save();
        paramCanvas.translate(AndroidUtilities.dp(10.0F) + j + this.titleX, k - AndroidUtilities.dp(3.0F));
        this.titleLayout.draw(paramCanvas);
        paramCanvas.restore();
        m = k + this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
        k = i;
      }
      n = m;
      i = k;
      if (this.authorLayout != null)
      {
        n = m;
        if (m != i1) {
          n = m + AndroidUtilities.dp(2.0F);
        }
        i = k;
        if (k == 0) {
          i = n - AndroidUtilities.dp(1.0F);
        }
        paramCanvas.save();
        paramCanvas.translate(AndroidUtilities.dp(10.0F) + j + this.authorX, n - AndroidUtilities.dp(3.0F));
        this.authorLayout.draw(paramCanvas);
        paramCanvas.restore();
        n += this.authorLayout.getLineBottom(this.authorLayout.getLineCount() - 1);
      }
      k = n;
      m = i;
      if (this.descriptionLayout == null) {
        break label2076;
      }
      m = n;
      if (n != i1) {
        m = n + AndroidUtilities.dp(2.0F);
      }
      k = i;
      if (i == 0) {
        k = m - AndroidUtilities.dp(1.0F);
      }
      this.descriptionY = (m - AndroidUtilities.dp(3.0F));
      paramCanvas.save();
      if (!this.hasInvoicePreview) {
        break label2027;
      }
    }
    label1445:
    label1450:
    label1455:
    label1461:
    label1539:
    float f;
    label1588:
    label1663:
    label1668:
    label1893:
    label1960:
    Object localObject2;
    label1968:
    label1976:
    label2000:
    label2027:
    for (int i = 0;; i = AndroidUtilities.dp(10.0F))
    {
      paramCanvas.translate(i + j + this.descriptionX, this.descriptionY);
      if ((this.pressedLink == null) || (this.linkBlockNum != -10)) {
        break label2037;
      }
      i = 0;
      while (i < this.urlPath.size())
      {
        paramCanvas.drawPath((Path)this.urlPath.get(i), Theme.chat_urlPaint);
        i += 1;
      }
      bool1 = false;
      break;
      i = 1;
      break label95;
      i = 0;
      break label95;
      bool1 = false;
      break label133;
      if (this.groupPhotoInvisible)
      {
        this.timeWasInvisible = true;
        break label161;
      }
      if ((!this.mediaWasInvisible) && (!this.timeWasInvisible)) {
        break label161;
      }
      if (this.mediaWasInvisible)
      {
        this.controlsAlpha = 0.0F;
        this.mediaWasInvisible = false;
      }
      if (this.timeWasInvisible)
      {
        this.timeAlpha = 0.0F;
        this.timeWasInvisible = false;
      }
      this.lastControlsAlphaChangeTime = System.currentTimeMillis();
      this.totalChangeTime = 0L;
      break label161;
      i = this.currentBackgroundDrawable.getBounds().left;
      if ((!this.mediaBackground) && (this.drawPinnedBottom)) {}
      for (f = 11.0F;; f = 17.0F)
      {
        this.textX = (AndroidUtilities.dp(f) + i);
        break;
      }
      if (this.hasInvoicePreview)
      {
        this.textY = (AndroidUtilities.dp(14.0F) + this.namesOffset);
        if (this.siteNameLayout == null) {
          break label301;
        }
        this.textY += this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
        break label301;
      }
      this.textY = (AndroidUtilities.dp(10.0F) + this.namesOffset);
      break label301;
      i = 0;
      break label378;
      localObject1 = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(i);
      paramCanvas.save();
      k = this.textX;
      if (((MessageObject.TextLayoutBlock)localObject1).isRtl()) {}
      for (j = (int)Math.ceil(this.currentMessageObject.textXOffset);; j = 0)
      {
        paramCanvas.translate(k - j, this.textY + ((MessageObject.TextLayoutBlock)localObject1).textYOffset);
        if ((this.pressedLink == null) || (i != this.linkBlockNum)) {
          break;
        }
        j = 0;
        while (j < this.urlPath.size())
        {
          paramCanvas.drawPath((Path)this.urlPath.get(j), Theme.chat_urlPaint);
          j += 1;
        }
      }
      if ((i == this.linkSelectionBlockNum) && (!this.urlPathSelection.isEmpty()))
      {
        j = 0;
        while (j < this.urlPathSelection.size())
        {
          paramCanvas.drawPath((Path)this.urlPathSelection.get(j), Theme.chat_textSearchSelectionPaint);
          j += 1;
        }
      }
      try
      {
        ((MessageObject.TextLayoutBlock)localObject1).textLayout.draw(paramCanvas);
        paramCanvas.restore();
        i += 1;
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          FileLog.e(localException1);
        }
      }
      if (this.hasInvoicePreview)
      {
        i = AndroidUtilities.dp(14.0F) + this.namesOffset;
        j = this.unmovedTextX + AndroidUtilities.dp(1.0F);
        break label542;
      }
      i = this.textY + this.currentMessageObject.textHeight + AndroidUtilities.dp(8.0F);
      j = this.unmovedTextX + AndroidUtilities.dp(1.0F);
      break label542;
      localObject2 = "chat_inPreviewLine";
      break label575;
      localObject2 = "chat_inSiteNameText";
      break label659;
      if (this.hasInvoicePreview) {}
      for (k = 0;; k = AndroidUtilities.dp(10.0F)) {
        break;
      }
      Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_messageTextIn"));
      Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
      break label1063;
    }
    label2037:
    this.descriptionLayout.draw(paramCanvas);
    paramCanvas.restore();
    i = m + this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
    int m = k;
    int k = i;
    label2076:
    boolean bool1 = bool2;
    i = k;
    if (this.drawPhotoImage)
    {
      bool1 = bool2;
      i = k;
      if (!this.drawInstantView)
      {
        i = k;
        if (k != i1) {
          i = k + AndroidUtilities.dp(2.0F);
        }
        if (!this.isSmallImage) {
          break label5170;
        }
        this.photoImage.setImageCoords(this.backgroundWidth + j - AndroidUtilities.dp(81.0F), m, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
      }
    }
    for (;;)
    {
      label2206:
      label2586:
      label2626:
      label2707:
      label2911:
      label2920:
      label2989:
      long l1;
      long l2;
      label3400:
      label3808:
      label3978:
      label4022:
      label4098:
      label4210:
      label4340:
      label4367:
      label4389:
      label4459:
      label4577:
      Object localObject4;
      if ((this.currentMessageObject.isRoundVideo()) && (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) && (MediaController.getInstance().isRoundVideoDrawingReady()))
      {
        bool1 = true;
        this.drawTime = true;
        if ((this.photosCountLayout != null) && (this.photoImage.getVisible()))
        {
          k = this.photoImage.getImageX() + this.photoImage.getImageWidth() - AndroidUtilities.dp(8.0F) - this.photosCountWidth;
          m = this.photoImage.getImageY() + this.photoImage.getImageHeight() - AndroidUtilities.dp(19.0F);
          this.rect.set(k - AndroidUtilities.dp(4.0F), m - AndroidUtilities.dp(1.5F), this.photosCountWidth + k + AndroidUtilities.dp(4.0F), AndroidUtilities.dp(14.5F) + m);
          n = Theme.chat_timeBackgroundPaint.getAlpha();
          Theme.chat_timeBackgroundPaint.setAlpha((int)(n * this.controlsAlpha));
          Theme.chat_durationPaint.setAlpha((int)(255.0F * this.controlsAlpha));
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), Theme.chat_timeBackgroundPaint);
          Theme.chat_timeBackgroundPaint.setAlpha(n);
          paramCanvas.save();
          paramCanvas.translate(k, m);
          this.photosCountLayout.draw(paramCanvas);
          paramCanvas.restore();
          Theme.chat_durationPaint.setAlpha(255);
        }
        if ((this.videoInfoLayout != null) && ((!this.drawPhotoImage) || (this.photoImage.getVisible())))
        {
          if ((!this.hasGamePreview) && (!this.hasInvoicePreview)) {
            break label5359;
          }
          if (!this.drawPhotoImage) {
            break label5342;
          }
          i = this.photoImage.getImageX() + AndroidUtilities.dp(8.5F);
          k = this.photoImage.getImageY() + AndroidUtilities.dp(6.0F);
          this.rect.set(i - AndroidUtilities.dp(4.0F), k - AndroidUtilities.dp(1.5F), this.durationWidth + i + AndroidUtilities.dp(4.0F), AndroidUtilities.dp(16.5F) + k);
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), Theme.chat_timeBackgroundPaint);
          paramCanvas.save();
          paramCanvas.translate(i, k);
          if (this.hasInvoicePreview)
          {
            if (!this.drawPhotoImage) {
              break label5489;
            }
            Theme.chat_shipmentPaint.setColor(Theme.getColor("chat_previewGameText"));
          }
          this.videoInfoLayout.draw(paramCanvas);
          paramCanvas.restore();
        }
        bool2 = bool1;
        if (this.drawInstantView)
        {
          i = this.linkPreviewHeight + i1 + AndroidUtilities.dp(10.0F);
          localObject3 = Theme.chat_instantViewRectPaint;
          if (!this.currentMessageObject.isOutOwner()) {
            break label5529;
          }
          localObject2 = Theme.chat_msgOutInstantDrawable;
          Theme.chat_instantViewPaint.setColor(Theme.getColor("chat_outPreviewInstantText"));
          ((Paint)localObject3).setColor(Theme.getColor("chat_outPreviewInstantText"));
          if (Build.VERSION.SDK_INT >= 21)
          {
            this.instantViewSelectorDrawable.setBounds(j, i, this.instantWidth + j, AndroidUtilities.dp(36.0F) + i);
            this.instantViewSelectorDrawable.draw(paramCanvas);
          }
          this.rect.set(j, i, this.instantWidth + j, AndroidUtilities.dp(36.0F) + i);
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(6.0F), AndroidUtilities.dp(6.0F), (Paint)localObject3);
          if (this.drawInstantViewType == 0)
          {
            setDrawableBounds((Drawable)localObject2, this.instantTextLeftX + this.instantTextX + j - AndroidUtilities.dp(15.0F), AndroidUtilities.dp(11.5F) + i, AndroidUtilities.dp(9.0F), AndroidUtilities.dp(13.0F));
            ((Drawable)localObject2).draw(paramCanvas);
          }
          bool2 = bool1;
          if (this.instantViewLayout != null)
          {
            paramCanvas.save();
            paramCanvas.translate(this.instantTextX + j, AndroidUtilities.dp(10.5F) + i);
            this.instantViewLayout.draw(paramCanvas);
            paramCanvas.restore();
            bool2 = bool1;
          }
        }
        this.drawTime = true;
        bool1 = bool2;
        if ((this.buttonState == -1) && (this.currentMessageObject.needDrawBluredPreview()) && (!MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) && (this.photoImage.getVisible()))
        {
          i = 4;
          if (this.currentMessageObject.messageOwner.destroyTime != 0)
          {
            if (!this.currentMessageObject.isOutOwner()) {
              break label5968;
            }
            i = 6;
          }
          setDrawableBounds(Theme.chat_photoStatesDrawables[i][this.buttonPressed], this.buttonX, this.buttonY);
          Theme.chat_photoStatesDrawables[i][this.buttonPressed].setAlpha((int)(255.0F * (1.0F - this.radialProgress.getAlpha()) * this.controlsAlpha));
          Theme.chat_photoStatesDrawables[i][this.buttonPressed].draw(paramCanvas);
          if (this.currentMessageObject.messageOwner.destroyTime != 0)
          {
            if (!this.currentMessageObject.isOutOwner())
            {
              l1 = System.currentTimeMillis();
              l2 = ConnectionsManager.getInstance(this.currentAccount).getTimeDifference() * 1000;
              f = (float)Math.max(0L, this.currentMessageObject.messageOwner.destroyTime * 1000L - (l1 + l2)) / (this.currentMessageObject.messageOwner.ttl * 1000.0F);
              Theme.chat_deleteProgressPaint.setAlpha((int)(255.0F * this.controlsAlpha));
              paramCanvas.drawArc(this.deleteProgressRect, -90.0F, -360.0F * f, true, Theme.chat_deleteProgressPaint);
              if (f != 0.0F)
              {
                i = AndroidUtilities.dp(2.0F);
                invalidate((int)this.deleteProgressRect.left - i, (int)this.deleteProgressRect.top - i, (int)this.deleteProgressRect.right + i * 2, (int)this.deleteProgressRect.bottom + i * 2);
              }
            }
            updateSecretTimeText(this.currentMessageObject);
          }
        }
        if ((this.documentAttachType != 2) && (this.currentMessageObject.type != 8)) {
          break label5973;
        }
        if ((this.photoImage.getVisible()) && (!this.hasGamePreview) && (!this.currentMessageObject.needDrawBluredPreview()))
        {
          i = ((BitmapDrawable)Theme.chat_msgMediaMenuDrawable).getPaint().getAlpha();
          Theme.chat_msgMediaMenuDrawable.setAlpha((int)(i * this.controlsAlpha));
          localObject2 = Theme.chat_msgMediaMenuDrawable;
          j = this.photoImage.getImageX() + this.photoImage.getImageWidth() - AndroidUtilities.dp(14.0F);
          this.otherX = j;
          k = this.photoImage.getImageY() + AndroidUtilities.dp(8.1F);
          this.otherY = k;
          setDrawableBounds((Drawable)localObject2, j, k);
          Theme.chat_msgMediaMenuDrawable.draw(paramCanvas);
          Theme.chat_msgMediaMenuDrawable.setAlpha(i);
        }
        if ((this.currentMessageObject.type != 1) && (this.documentAttachType != 4)) {
          break label7335;
        }
        if (this.photoImage.getVisible())
        {
          if ((!this.currentMessageObject.needDrawBluredPreview()) && (this.documentAttachType == 4))
          {
            i = ((BitmapDrawable)Theme.chat_msgMediaMenuDrawable).getPaint().getAlpha();
            Theme.chat_msgMediaMenuDrawable.setAlpha((int)(i * this.controlsAlpha));
            localObject2 = Theme.chat_msgMediaMenuDrawable;
            j = this.photoImage.getImageX() + this.photoImage.getImageWidth() - AndroidUtilities.dp(14.0F);
            this.otherX = j;
            k = this.photoImage.getImageY() + AndroidUtilities.dp(8.1F);
            this.otherY = k;
            setDrawableBounds((Drawable)localObject2, j, k);
            Theme.chat_msgMediaMenuDrawable.draw(paramCanvas);
            Theme.chat_msgMediaMenuDrawable.setAlpha(i);
          }
          if ((!this.forceNotDrawTime) && (this.infoLayout != null) && ((this.buttonState == 1) || (this.buttonState == 0) || (this.buttonState == 3) || (this.currentMessageObject.needDrawBluredPreview())))
          {
            Theme.chat_infoPaint.setColor(Theme.getColor("chat_mediaInfoText"));
            i = this.photoImage.getImageX() + AndroidUtilities.dp(4.0F);
            j = this.photoImage.getImageY() + AndroidUtilities.dp(4.0F);
            this.rect.set(i, j, this.infoWidth + i + AndroidUtilities.dp(8.0F), AndroidUtilities.dp(16.5F) + j);
            i = Theme.chat_timeBackgroundPaint.getAlpha();
            Theme.chat_timeBackgroundPaint.setAlpha((int)(i * this.controlsAlpha));
            paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), Theme.chat_timeBackgroundPaint);
            Theme.chat_timeBackgroundPaint.setAlpha(i);
            paramCanvas.save();
            paramCanvas.translate(this.photoImage.getImageX() + AndroidUtilities.dp(8.0F), this.photoImage.getImageY() + AndroidUtilities.dp(5.5F));
            Theme.chat_infoPaint.setAlpha((int)(255.0F * this.controlsAlpha));
            this.infoLayout.draw(paramCanvas);
            paramCanvas.restore();
            Theme.chat_infoPaint.setAlpha(255);
          }
        }
        if (this.captionLayout != null)
        {
          if ((this.currentMessageObject.type != 1) && (this.documentAttachType != 4) && (this.currentMessageObject.type != 8)) {
            break label9017;
          }
          this.captionX = (this.photoImage.getImageX() + AndroidUtilities.dp(5.0F) + this.captionOffsetX);
          this.captionY = (this.photoImage.getImageY() + this.photoImage.getImageHeight() + AndroidUtilities.dp(6.0F));
        }
        if (this.currentPosition == null) {
          drawCaptionLayout(paramCanvas, false);
        }
        if (this.hasOldCaptionPreview)
        {
          if ((this.currentMessageObject.type != 1) && (this.documentAttachType != 4) && (this.currentMessageObject.type != 8)) {
            break label9208;
          }
          j = this.photoImage.getImageX() + AndroidUtilities.dp(5.0F);
          i = this.totalHeight;
          if (!this.drawPinnedTop) {
            break label9245;
          }
          f = 9.0F;
          m = i - AndroidUtilities.dp(f) - this.linkPreviewHeight - AndroidUtilities.dp(8.0F);
          k = m;
          localObject3 = Theme.chat_replyLinePaint;
          if (!this.currentMessageObject.isOutOwner()) {
            break label9252;
          }
          localObject2 = "chat_outPreviewLine";
          ((Paint)localObject3).setColor(Theme.getColor((String)localObject2));
          paramCanvas.drawRect(j, k - AndroidUtilities.dp(3.0F), AndroidUtilities.dp(2.0F) + j, this.linkPreviewHeight + k, Theme.chat_replyLinePaint);
          i = k;
          if (this.siteNameLayout != null)
          {
            localObject3 = Theme.chat_replyNamePaint;
            if (!this.currentMessageObject.isOutOwner()) {
              break label9260;
            }
            localObject2 = "chat_outSiteNameText";
            ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
            paramCanvas.save();
            if (!this.siteNameRtl) {
              break label9268;
            }
            i = this.backgroundWidth - this.siteNameWidth - AndroidUtilities.dp(32.0F);
            paramCanvas.translate(j + i, k - AndroidUtilities.dp(3.0F));
            this.siteNameLayout.draw(paramCanvas);
            paramCanvas.restore();
            i = k + this.siteNameLayout.getLineBottom(this.siteNameLayout.getLineCount() - 1);
          }
          if (!this.currentMessageObject.isOutOwner()) {
            break label9290;
          }
          Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
          if (this.descriptionLayout != null)
          {
            k = i;
            if (i != m) {
              k = i + AndroidUtilities.dp(2.0F);
            }
            this.descriptionY = (k - AndroidUtilities.dp(3.0F));
            paramCanvas.save();
            paramCanvas.translate(AndroidUtilities.dp(10.0F) + j + this.descriptionX, this.descriptionY);
            this.descriptionLayout.draw(paramCanvas);
            paramCanvas.restore();
          }
          this.drawTime = true;
        }
        if (this.documentAttachType == 1)
        {
          if (!this.currentMessageObject.isOutOwner()) {
            break label9329;
          }
          Theme.chat_docNamePaint.setColor(Theme.getColor("chat_outFileNameText"));
          localObject3 = Theme.chat_infoPaint;
          if (!isDrawSelectedBackground()) {
            break label9305;
          }
          localObject2 = "chat_outFileInfoSelectedText";
          ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
          localObject3 = Theme.chat_docBackPaint;
          if (!isDrawSelectedBackground()) {
            break label9313;
          }
          localObject2 = "chat_outFileBackgroundSelected";
          ((Paint)localObject3).setColor(Theme.getColor((String)localObject2));
          if (!isDrawSelectedBackground()) {
            break label9321;
          }
          localObject2 = Theme.chat_msgOutMenuSelectedDrawable;
          if (!this.drawPhotoImage) {
            break label9640;
          }
          if (this.currentMessageObject.type != 0) {
            break label9434;
          }
          i = this.photoImage.getImageX() + this.backgroundWidth - AndroidUtilities.dp(56.0F);
          this.otherX = i;
          j = this.photoImage.getImageY() + AndroidUtilities.dp(1.0F);
          this.otherY = j;
          setDrawableBounds((Drawable)localObject2, i, j);
          k = this.photoImage.getImageX() + this.photoImage.getImageWidth() + AndroidUtilities.dp(10.0F);
          j = this.photoImage.getImageY() + AndroidUtilities.dp(8.0F);
          m = this.photoImage.getImageY() + this.docTitleLayout.getLineBottom(this.docTitleLayout.getLineCount() - 1) + AndroidUtilities.dp(13.0F);
          if ((this.buttonState >= 0) && (this.buttonState < 4))
          {
            if (bool1) {
              break label9531;
            }
            i = this.buttonState;
            if (this.buttonState != 0) {
              break label9496;
            }
            if (!this.currentMessageObject.isOutOwner()) {
              break label9490;
            }
            i = 7;
            localObject3 = this.radialProgress;
            localObject4 = Theme.chat_photoStatesDrawables[i];
            if ((!isDrawSelectedBackground()) && (this.buttonPressed == 0)) {
              break label9526;
            }
            i = 1;
            label4606:
            ((RadialProgress)localObject3).swapBackground(localObject4[i]);
          }
          label4616:
          if (bool1) {
            break label9605;
          }
          this.rect.set(this.photoImage.getImageX(), this.photoImage.getImageY(), this.photoImage.getImageX() + this.photoImage.getImageWidth(), this.photoImage.getImageY() + this.photoImage.getImageHeight());
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(3.0F), AndroidUtilities.dp(3.0F), Theme.chat_docBackPaint);
          if (!this.currentMessageObject.isOutOwner()) {
            break label9563;
          }
          localObject4 = this.radialProgress;
          if (!isDrawSelectedBackground()) {
            break label9555;
          }
          localObject3 = "chat_outFileProgressSelected";
          label4729:
          ((RadialProgress)localObject4).setProgressColor(Theme.getColor((String)localObject3));
          i = m;
          label4742:
          ((Drawable)localObject2).draw(paramCanvas);
        }
      }
      try
      {
        if (this.docTitleLayout != null)
        {
          paramCanvas.save();
          paramCanvas.translate(this.docTitleOffsetX + k, j);
          this.docTitleLayout.draw(paramCanvas);
          paramCanvas.restore();
        }
      }
      catch (Exception localException2)
      {
        try
        {
          for (;;)
          {
            if (this.infoLayout != null)
            {
              paramCanvas.save();
              paramCanvas.translate(k, i);
              this.infoLayout.draw(paramCanvas);
              paramCanvas.restore();
            }
            if ((this.drawImageButton) && (this.photoImage.getVisible()))
            {
              if (this.controlsAlpha != 1.0F) {
                this.radialProgress.setOverrideAlpha(this.controlsAlpha);
              }
              this.radialProgress.draw(paramCanvas);
            }
            if (this.botButtons.isEmpty()) {
              return;
            }
            if (!this.currentMessageObject.isOutOwner()) {
              break label9874;
            }
            i = getMeasuredWidth() - this.widthForButtons - AndroidUtilities.dp(10.0F);
            j = 0;
            for (;;)
            {
              if (j >= this.botButtons.size()) {
                return;
              }
              localObject3 = (BotButton)this.botButtons.get(j);
              m = ((BotButton)localObject3).y + this.layoutHeight - AndroidUtilities.dp(2.0F);
              localObject4 = Theme.chat_systemDrawable;
              if (j != this.pressedBotButton) {
                break;
              }
              localObject2 = Theme.colorPressedFilter;
              ((Drawable)localObject4).setColorFilter((ColorFilter)localObject2);
              Theme.chat_systemDrawable.setBounds(((BotButton)localObject3).x + i, m, ((BotButton)localObject3).x + i + ((BotButton)localObject3).width, ((BotButton)localObject3).height + m);
              Theme.chat_systemDrawable.draw(paramCanvas);
              paramCanvas.save();
              paramCanvas.translate(((BotButton)localObject3).x + i + AndroidUtilities.dp(5.0F), (AndroidUtilities.dp(44.0F) - ((BotButton)localObject3).title.getLineBottom(((BotButton)localObject3).title.getLineCount() - 1)) / 2 + m);
              ((BotButton)localObject3).title.draw(paramCanvas);
              paramCanvas.restore();
              if (!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonUrl)) {
                break label9913;
              }
              k = ((BotButton)localObject3).x;
              n = ((BotButton)localObject3).width;
              i1 = AndroidUtilities.dp(3.0F);
              i2 = Theme.chat_botLinkDrawalbe.getIntrinsicWidth();
              setDrawableBounds(Theme.chat_botLinkDrawalbe, k + n - i1 - i2 + i, AndroidUtilities.dp(3.0F) + m);
              Theme.chat_botLinkDrawalbe.draw(paramCanvas);
              j += 1;
            }
            label5170:
            localObject2 = this.photoImage;
            if (this.hasInvoicePreview) {}
            for (k = -AndroidUtilities.dp(6.3F);; k = AndroidUtilities.dp(10.0F))
            {
              ((ImageReceiver)localObject2).setImageCoords(k + j, i, this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
              if (!this.drawImageButton) {
                break;
              }
              k = AndroidUtilities.dp(48.0F);
              this.buttonX = ((int)(this.photoImage.getImageX() + (this.photoImage.getImageWidth() - k) / 2.0F));
              this.buttonY = ((int)(this.photoImage.getImageY() + (this.photoImage.getImageHeight() - k) / 2.0F));
              this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + k, this.buttonY + k);
              break;
            }
            bool1 = this.photoImage.draw(paramCanvas);
            break label2206;
            label5342:
            k = j;
            m = i;
            i = k;
            k = m;
            break label2586;
            label5359:
            i = this.photoImage.getImageX() + this.photoImage.getImageWidth() - AndroidUtilities.dp(8.0F) - this.durationWidth;
            k = this.photoImage.getImageY() + this.photoImage.getImageHeight() - AndroidUtilities.dp(19.0F);
            this.rect.set(i - AndroidUtilities.dp(4.0F), k - AndroidUtilities.dp(1.5F), this.durationWidth + i + AndroidUtilities.dp(4.0F), AndroidUtilities.dp(14.5F) + k);
            paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), Theme.chat_timeBackgroundPaint);
            break label2586;
            label5489:
            if (this.currentMessageObject.isOutOwner())
            {
              Theme.chat_shipmentPaint.setColor(Theme.getColor("chat_messageTextOut"));
              break label2626;
            }
            Theme.chat_shipmentPaint.setColor(Theme.getColor("chat_messageTextIn"));
            break label2626;
            label5529:
            localObject2 = Theme.chat_msgInInstantDrawable;
            Theme.chat_instantViewPaint.setColor(Theme.getColor("chat_inPreviewInstantText"));
            ((Paint)localObject3).setColor(Theme.getColor("chat_inPreviewInstantText"));
            break label2707;
            label5560:
            bool1 = bool3;
            if (!this.drawPhotoImage) {
              break label2920;
            }
            if ((this.currentMessageObject.isRoundVideo()) && (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) && (MediaController.getInstance().isRoundVideoDrawingReady()))
            {
              bool1 = true;
              this.drawTime = true;
              break label2920;
            }
            if ((this.currentMessageObject.type == 5) && (Theme.chat_roundVideoShadow != null))
            {
              i = this.photoImage.getImageX() - AndroidUtilities.dp(3.0F);
              j = this.photoImage.getImageY() - AndroidUtilities.dp(2.0F);
              Theme.chat_roundVideoShadow.setAlpha((int)(this.photoImage.getCurrentAlpha() * 255.0F));
              Theme.chat_roundVideoShadow.setBounds(i, j, AndroidUtilities.roundMessageSize + i + AndroidUtilities.dp(6.0F), AndroidUtilities.roundMessageSize + j + AndroidUtilities.dp(6.0F));
              Theme.chat_roundVideoShadow.draw(paramCanvas);
            }
            bool2 = this.photoImage.draw(paramCanvas);
            bool3 = this.drawTime;
            this.drawTime = this.photoImage.getVisible();
            bool1 = bool2;
            if (this.currentPosition == null) {
              break label2920;
            }
            bool1 = bool2;
            if (bool3 == this.drawTime) {
              break label2920;
            }
            localObject2 = (ViewGroup)getParent();
            bool1 = bool2;
            if (localObject2 == null) {
              break label2920;
            }
            if (!this.currentPosition.last)
            {
              j = ((ViewGroup)localObject2).getChildCount();
              i = 0;
              bool1 = bool2;
              if (i >= j) {
                break label2920;
              }
              localObject3 = ((ViewGroup)localObject2).getChildAt(i);
              if ((localObject3 == this) || (!(localObject3 instanceof ChatMessageCell))) {}
              do
              {
                do
                {
                  i += 1;
                  break;
                  localObject3 = (ChatMessageCell)localObject3;
                } while (((ChatMessageCell)localObject3).getCurrentMessagesGroup() != this.currentMessagesGroup);
                localObject4 = ((ChatMessageCell)localObject3).getCurrentPosition();
              } while ((!((MessageObject.GroupedMessagePosition)localObject4).last) || (((MessageObject.GroupedMessagePosition)localObject4).maxY != this.currentPosition.maxY) || (((ChatMessageCell)localObject3).timeX - AndroidUtilities.dp(4.0F) + ((ChatMessageCell)localObject3).getLeft() >= getRight()));
              if (!this.drawTime) {}
              for (bool1 = true;; bool1 = false)
              {
                ((ChatMessageCell)localObject3).groupPhotoInvisible = bool1;
                ((ChatMessageCell)localObject3).invalidate();
                ((ViewGroup)localObject2).invalidate();
                break;
              }
            }
            ((ViewGroup)localObject2).invalidate();
            bool1 = bool2;
            break label2920;
            label5968:
            i = 5;
            break label2989;
            label5973:
            if ((this.documentAttachType == 7) || (this.currentMessageObject.type == 5))
            {
              if (this.durationLayout == null) {
                break label3400;
              }
              bool2 = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
              if ((bool2) && (this.currentMessageObject.type == 5)) {
                drawRoundProgress(paramCanvas);
              }
              if (this.documentAttachType == 7)
              {
                i = this.backgroundDrawableLeft;
                if ((this.currentMessageObject.isOutOwner()) || (this.drawPinnedBottom))
                {
                  f = 12.0F;
                  label6068:
                  k = i + AndroidUtilities.dp(f);
                  j = this.layoutHeight;
                  if (!this.drawPinnedBottom) {
                    break label6152;
                  }
                }
                label6152:
                for (i = 2;; i = 0)
                {
                  j = j - AndroidUtilities.dp(6.3F - i) - this.timeLayout.getHeight();
                  i = k;
                  paramCanvas.save();
                  paramCanvas.translate(i, j);
                  this.durationLayout.draw(paramCanvas);
                  paramCanvas.restore();
                  break;
                  f = 18.0F;
                  break label6068;
                }
              }
              j = this.backgroundDrawableLeft + AndroidUtilities.dp(8.0F);
              k = this.layoutHeight;
              if (this.drawPinnedBottom) {}
              for (i = 2;; i = 0)
              {
                k -= AndroidUtilities.dp(28 - i);
                this.rect.set(j, k, this.timeWidthAudio + j + AndroidUtilities.dp(22.0F), AndroidUtilities.dp(17.0F) + k);
                paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), Theme.chat_actionBackgroundPaint);
                if ((bool2) || (!this.currentMessageObject.isContentUnread())) {
                  break label6356;
                }
                Theme.chat_docBackPaint.setColor(Theme.getColor("chat_mediaTimeText"));
                paramCanvas.drawCircle(this.timeWidthAudio + j + AndroidUtilities.dp(12.0F), AndroidUtilities.dp(8.3F) + k, AndroidUtilities.dp(3.0F), Theme.chat_docBackPaint);
                i = j + AndroidUtilities.dp(4.0F);
                j = k + AndroidUtilities.dp(1.7F);
                break;
              }
              label6356:
              if ((bool2) && (!MediaController.getInstance().isMessagePaused())) {
                this.roundVideoPlayingDrawable.start();
              }
              for (;;)
              {
                setDrawableBounds(this.roundVideoPlayingDrawable, this.timeWidthAudio + j + AndroidUtilities.dp(6.0F), AndroidUtilities.dp(2.3F) + k);
                this.roundVideoPlayingDrawable.draw(paramCanvas);
                break;
                this.roundVideoPlayingDrawable.stop();
              }
            }
            if (this.documentAttachType == 5)
            {
              if (this.currentMessageObject.isOutOwner())
              {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor("chat_outAudioTitleText"));
                Theme.chat_audioPerformerPaint.setColor(Theme.getColor("chat_outAudioPerfomerText"));
                Theme.chat_audioTimePaint.setColor(Theme.getColor("chat_outAudioDurationText"));
                localObject3 = this.radialProgress;
                if ((isDrawSelectedBackground()) || (this.buttonPressed != 0))
                {
                  localObject2 = "chat_outAudioSelectedProgress";
                  label6507:
                  ((RadialProgress)localObject3).setProgressColor(Theme.getColor((String)localObject2));
                  this.radialProgress.draw(paramCanvas);
                  paramCanvas.save();
                  paramCanvas.translate(this.timeAudioX + this.songX, AndroidUtilities.dp(13.0F) + this.namesOffset + this.mediaOffsetY);
                  this.songLayout.draw(paramCanvas);
                  paramCanvas.restore();
                  paramCanvas.save();
                  if (!MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
                    break label6848;
                  }
                  paramCanvas.translate(this.seekBarX, this.seekBarY);
                  this.seekBar.draw(paramCanvas);
                  label6613:
                  paramCanvas.restore();
                  paramCanvas.save();
                  paramCanvas.translate(this.timeAudioX, AndroidUtilities.dp(57.0F) + this.namesOffset + this.mediaOffsetY);
                  this.durationLayout.draw(paramCanvas);
                  paramCanvas.restore();
                  if (!this.currentMessageObject.isOutOwner()) {
                    break label6898;
                  }
                  if (!isDrawSelectedBackground()) {
                    break label6890;
                  }
                  localObject2 = Theme.chat_msgOutMenuSelectedDrawable;
                  label6682:
                  i = this.buttonX;
                  j = this.backgroundWidth;
                  if (this.currentMessageObject.type != 0) {
                    break label6921;
                  }
                }
              }
              label6848:
              label6890:
              label6898:
              label6921:
              for (f = 58.0F;; f = 48.0F)
              {
                i = j + i - AndroidUtilities.dp(f);
                this.otherX = i;
                j = this.buttonY - AndroidUtilities.dp(5.0F);
                this.otherY = j;
                setDrawableBounds((Drawable)localObject2, i, j);
                ((Drawable)localObject2).draw(paramCanvas);
                break;
                localObject2 = "chat_outAudioProgress";
                break label6507;
                Theme.chat_audioTitlePaint.setColor(Theme.getColor("chat_inAudioTitleText"));
                Theme.chat_audioPerformerPaint.setColor(Theme.getColor("chat_inAudioPerfomerText"));
                Theme.chat_audioTimePaint.setColor(Theme.getColor("chat_inAudioDurationText"));
                localObject3 = this.radialProgress;
                if ((isDrawSelectedBackground()) || (this.buttonPressed != 0)) {}
                for (localObject2 = "chat_inAudioSelectedProgress";; localObject2 = "chat_inAudioProgress")
                {
                  ((RadialProgress)localObject3).setProgressColor(Theme.getColor((String)localObject2));
                  break;
                }
                paramCanvas.translate(this.timeAudioX + this.performerX, AndroidUtilities.dp(35.0F) + this.namesOffset + this.mediaOffsetY);
                this.performerLayout.draw(paramCanvas);
                break label6613;
                localObject2 = Theme.chat_msgOutMenuDrawable;
                break label6682;
                if (isDrawSelectedBackground()) {}
                for (localObject2 = Theme.chat_msgInMenuSelectedDrawable;; localObject2 = Theme.chat_msgInMenuDrawable) {
                  break;
                }
              }
            }
            if (this.documentAttachType != 3) {
              break label3400;
            }
            if (this.currentMessageObject.isOutOwner())
            {
              localObject3 = Theme.chat_audioTimePaint;
              if (isDrawSelectedBackground())
              {
                localObject2 = "chat_outAudioDurationSelectedText";
                label6963:
                ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                localObject3 = this.radialProgress;
                if ((!isDrawSelectedBackground()) && (this.buttonPressed == 0)) {
                  break label7213;
                }
                localObject2 = "chat_outAudioSelectedProgress";
                label6998:
                ((RadialProgress)localObject3).setProgressColor(Theme.getColor((String)localObject2));
                this.radialProgress.draw(paramCanvas);
                paramCanvas.save();
                if (!this.useSeekBarWaweform) {
                  break label7302;
                }
                paramCanvas.translate(this.seekBarX + AndroidUtilities.dp(13.0F), this.seekBarY);
                this.seekBarWaveform.draw(paramCanvas);
                label7057:
                paramCanvas.restore();
                paramCanvas.save();
                paramCanvas.translate(this.timeAudioX, AndroidUtilities.dp(44.0F) + this.namesOffset + this.mediaOffsetY);
                this.durationLayout.draw(paramCanvas);
                paramCanvas.restore();
                if ((this.currentMessageObject.type == 0) || (!this.currentMessageObject.isContentUnread())) {
                  break label3400;
                }
                localObject3 = Theme.chat_docBackPaint;
                if (!this.currentMessageObject.isOutOwner()) {
                  break label7327;
                }
              }
            }
            label7213:
            label7238:
            label7294:
            label7302:
            label7327:
            for (localObject2 = "chat_outVoiceSeekbarFill";; localObject2 = "chat_inVoiceSeekbarFill")
            {
              ((Paint)localObject3).setColor(Theme.getColor((String)localObject2));
              paramCanvas.drawCircle(this.timeAudioX + this.timeWidthAudio + AndroidUtilities.dp(6.0F), AndroidUtilities.dp(51.0F) + this.namesOffset + this.mediaOffsetY, AndroidUtilities.dp(3.0F), Theme.chat_docBackPaint);
              break;
              localObject2 = "chat_outAudioDurationText";
              break label6963;
              localObject2 = "chat_outAudioProgress";
              break label6998;
              localObject3 = Theme.chat_audioTimePaint;
              if (isDrawSelectedBackground())
              {
                localObject2 = "chat_inAudioDurationSelectedText";
                ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                localObject3 = this.radialProgress;
                if ((!isDrawSelectedBackground()) && (this.buttonPressed == 0)) {
                  break label7294;
                }
              }
              for (localObject2 = "chat_inAudioSelectedProgress";; localObject2 = "chat_inAudioProgress")
              {
                ((RadialProgress)localObject3).setProgressColor(Theme.getColor((String)localObject2));
                break;
                localObject2 = "chat_inAudioDurationText";
                break label7238;
              }
              paramCanvas.translate(this.seekBarX, this.seekBarY);
              this.seekBar.draw(paramCanvas);
              break label7057;
            }
            label7335:
            if (this.currentMessageObject.type == 4)
            {
              if (this.docTitleLayout == null) {
                break label3808;
              }
              if (this.currentMessageObject.isOutOwner()) {
                if ((this.currentMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
                {
                  Theme.chat_locationTitlePaint.setColor(Theme.getColor("chat_messageTextOut"));
                  label7391:
                  localObject3 = Theme.chat_locationAddressPaint;
                  if (!isDrawSelectedBackground()) {
                    break label7940;
                  }
                  localObject2 = "chat_outVenueInfoSelectedText";
                  label7408:
                  ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                  if (!(this.currentMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive)) {
                    break label8056;
                  }
                  i = this.photoImage.getImageY2() + AndroidUtilities.dp(30.0F);
                  if (!this.locationExpired)
                  {
                    this.forceNotDrawTime = true;
                    f = Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - this.currentMessageObject.messageOwner.date) / this.currentMessageObject.messageOwner.media.period;
                    this.rect.set(this.photoImage.getImageX2() - AndroidUtilities.dp(43.0F), i - AndroidUtilities.dp(15.0F), this.photoImage.getImageX2() - AndroidUtilities.dp(13.0F), AndroidUtilities.dp(15.0F) + i);
                    if (!this.currentMessageObject.isOutOwner()) {
                      break label8029;
                    }
                    Theme.chat_radialProgress2Paint.setColor(Theme.getColor("chat_outInstant"));
                    Theme.chat_livePaint.setColor(Theme.getColor("chat_outInstant"));
                  }
                }
              }
              for (;;)
              {
                Theme.chat_radialProgress2Paint.setAlpha(50);
                paramCanvas.drawCircle(this.rect.centerX(), this.rect.centerY(), AndroidUtilities.dp(15.0F), Theme.chat_radialProgress2Paint);
                Theme.chat_radialProgress2Paint.setAlpha(255);
                paramCanvas.drawArc(this.rect, -90.0F, -360.0F * (1.0F - f), false, Theme.chat_radialProgress2Paint);
                localObject2 = LocaleController.formatLocationLeftTime(Math.abs(this.currentMessageObject.messageOwner.media.period - (ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - this.currentMessageObject.messageOwner.date)));
                f = Theme.chat_livePaint.measureText((String)localObject2);
                paramCanvas.drawText((String)localObject2, this.rect.centerX() - f / 2.0F, AndroidUtilities.dp(4.0F) + i, Theme.chat_livePaint);
                paramCanvas.save();
                paramCanvas.translate(this.photoImage.getImageX() + AndroidUtilities.dp(10.0F), this.photoImage.getImageY2() + AndroidUtilities.dp(10.0F));
                this.docTitleLayout.draw(paramCanvas);
                paramCanvas.translate(0.0F, AndroidUtilities.dp(23.0F));
                this.infoLayout.draw(paramCanvas);
                paramCanvas.restore();
                i = this.photoImage.getImageX() + this.photoImage.getImageWidth() / 2 - AndroidUtilities.dp(31.0F);
                j = this.photoImage.getImageY() + this.photoImage.getImageHeight() / 2 - AndroidUtilities.dp(38.0F);
                setDrawableBounds(Theme.chat_msgAvatarLiveLocationDrawable, i, j);
                Theme.chat_msgAvatarLiveLocationDrawable.draw(paramCanvas);
                this.locationImageReceiver.setImageCoords(AndroidUtilities.dp(5.0F) + i, AndroidUtilities.dp(5.0F) + j, AndroidUtilities.dp(52.0F), AndroidUtilities.dp(52.0F));
                this.locationImageReceiver.draw(paramCanvas);
                break;
                Theme.chat_locationTitlePaint.setColor(Theme.getColor("chat_outVenueNameText"));
                break label7391;
                label7940:
                localObject2 = "chat_outVenueInfoText";
                break label7408;
                if ((this.currentMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
                {
                  Theme.chat_locationTitlePaint.setColor(Theme.getColor("chat_messageTextIn"));
                  label7976:
                  localObject3 = Theme.chat_locationAddressPaint;
                  if (!isDrawSelectedBackground()) {
                    break label8021;
                  }
                }
                label8021:
                for (localObject2 = "chat_inVenueInfoSelectedText";; localObject2 = "chat_inVenueInfoText")
                {
                  ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                  break;
                  Theme.chat_locationTitlePaint.setColor(Theme.getColor("chat_inVenueNameText"));
                  break label7976;
                }
                label8029:
                Theme.chat_radialProgress2Paint.setColor(Theme.getColor("chat_inInstant"));
                Theme.chat_livePaint.setColor(Theme.getColor("chat_inInstant"));
              }
              label8056:
              paramCanvas.save();
              paramCanvas.translate(this.docTitleOffsetX + this.photoImage.getImageX() + this.photoImage.getImageWidth() + AndroidUtilities.dp(10.0F), this.photoImage.getImageY() + AndroidUtilities.dp(8.0F));
              this.docTitleLayout.draw(paramCanvas);
              paramCanvas.restore();
              if (this.infoLayout == null) {
                break label3808;
              }
              paramCanvas.save();
              paramCanvas.translate(this.photoImage.getImageX() + this.photoImage.getImageWidth() + AndroidUtilities.dp(10.0F), this.photoImage.getImageY() + this.docTitleLayout.getLineBottom(this.docTitleLayout.getLineCount() - 1) + AndroidUtilities.dp(13.0F));
              this.infoLayout.draw(paramCanvas);
              paramCanvas.restore();
              break label3808;
            }
            if (this.currentMessageObject.type == 16)
            {
              if (this.currentMessageObject.isOutOwner())
              {
                Theme.chat_audioTitlePaint.setColor(Theme.getColor("chat_messageTextOut"));
                localObject3 = Theme.chat_contactPhonePaint;
                if (isDrawSelectedBackground())
                {
                  localObject2 = "chat_outTimeSelectedText";
                  label8257:
                  ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                  this.forceNotDrawTime = true;
                  if (!this.currentMessageObject.isOutOwner()) {
                    break label8559;
                  }
                  i = this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(16.0F);
                  label8299:
                  this.otherX = i;
                  if (this.titleLayout != null)
                  {
                    paramCanvas.save();
                    paramCanvas.translate(i, AndroidUtilities.dp(12.0F) + this.namesOffset);
                    this.titleLayout.draw(paramCanvas);
                    paramCanvas.restore();
                  }
                  if (this.docTitleLayout != null)
                  {
                    paramCanvas.save();
                    paramCanvas.translate(AndroidUtilities.dp(19.0F) + i, AndroidUtilities.dp(37.0F) + this.namesOffset);
                    this.docTitleLayout.draw(paramCanvas);
                    paramCanvas.restore();
                  }
                  if (!this.currentMessageObject.isOutOwner()) {
                    break label8604;
                  }
                  localObject4 = Theme.chat_msgCallUpGreenDrawable;
                  if ((!isDrawSelectedBackground()) && (!this.otherPressed)) {
                    break label8596;
                  }
                }
              }
              label8559:
              label8596:
              for (localObject2 = Theme.chat_msgOutCallSelectedDrawable;; localObject2 = Theme.chat_msgOutCallDrawable)
              {
                setDrawableBounds((Drawable)localObject4, i - AndroidUtilities.dp(3.0F), AndroidUtilities.dp(36.0F) + this.namesOffset);
                ((Drawable)localObject4).draw(paramCanvas);
                j = AndroidUtilities.dp(205.0F);
                k = AndroidUtilities.dp(22.0F);
                this.otherY = k;
                setDrawableBounds((Drawable)localObject2, j + i, k);
                ((Drawable)localObject2).draw(paramCanvas);
                break;
                localObject2 = "chat_outTimeText";
                break label8257;
                Theme.chat_audioTitlePaint.setColor(Theme.getColor("chat_messageTextIn"));
                localObject3 = Theme.chat_contactPhonePaint;
                if (isDrawSelectedBackground()) {}
                for (localObject2 = "chat_inTimeSelectedText";; localObject2 = "chat_inTimeText")
                {
                  ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
                  break;
                }
                if ((this.isChat) && (this.currentMessageObject.needDrawAvatar()))
                {
                  i = AndroidUtilities.dp(74.0F);
                  break label8299;
                }
                i = AndroidUtilities.dp(25.0F);
                break label8299;
              }
              label8604:
              localObject2 = this.currentMessageObject.messageOwner.action.reason;
              if (((localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonMissed)) || ((localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonBusy)))
              {
                localObject2 = Theme.chat_msgCallDownRedDrawable;
                label8640:
                if ((!isDrawSelectedBackground()) && (!this.otherPressed)) {
                  break label8678;
                }
              }
              label8678:
              for (localObject3 = Theme.chat_msgInCallSelectedDrawable;; localObject3 = Theme.chat_msgInCallDrawable)
              {
                localObject4 = localObject2;
                localObject2 = localObject3;
                break;
                localObject2 = Theme.chat_msgCallDownGreenDrawable;
                break label8640;
              }
            }
            if (this.currentMessageObject.type != 12) {
              break label3808;
            }
            localObject3 = Theme.chat_contactNamePaint;
            if (this.currentMessageObject.isOutOwner())
            {
              localObject2 = "chat_outContactNameText";
              label8718:
              ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
              localObject3 = Theme.chat_contactPhonePaint;
              if (!this.currentMessageObject.isOutOwner()) {
                break label8978;
              }
              localObject2 = "chat_outContactPhoneText";
              label8748:
              ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
              if (this.titleLayout != null)
              {
                paramCanvas.save();
                paramCanvas.translate(this.photoImage.getImageX() + this.photoImage.getImageWidth() + AndroidUtilities.dp(9.0F), AndroidUtilities.dp(16.0F) + this.namesOffset);
                this.titleLayout.draw(paramCanvas);
                paramCanvas.restore();
              }
              if (this.docTitleLayout != null)
              {
                paramCanvas.save();
                paramCanvas.translate(this.photoImage.getImageX() + this.photoImage.getImageWidth() + AndroidUtilities.dp(9.0F), AndroidUtilities.dp(39.0F) + this.namesOffset);
                this.docTitleLayout.draw(paramCanvas);
                paramCanvas.restore();
              }
              if (!this.currentMessageObject.isOutOwner()) {
                break label8994;
              }
              if (!isDrawSelectedBackground()) {
                break label8986;
              }
            }
            label8978:
            label8986:
            for (localObject2 = Theme.chat_msgOutMenuSelectedDrawable;; localObject2 = Theme.chat_msgOutMenuDrawable)
            {
              i = this.photoImage.getImageX() + this.backgroundWidth - AndroidUtilities.dp(48.0F);
              this.otherX = i;
              j = this.photoImage.getImageY() - AndroidUtilities.dp(5.0F);
              this.otherY = j;
              setDrawableBounds((Drawable)localObject2, i, j);
              ((Drawable)localObject2).draw(paramCanvas);
              break;
              localObject2 = "chat_inContactNameText";
              break label8718;
              localObject2 = "chat_inContactPhoneText";
              break label8748;
            }
            label8994:
            if (isDrawSelectedBackground()) {}
            for (localObject2 = Theme.chat_msgInMenuSelectedDrawable;; localObject2 = Theme.chat_msgInMenuDrawable) {
              break;
            }
            label9017:
            if (this.hasOldCaptionPreview)
            {
              i = this.backgroundDrawableLeft;
              if (this.currentMessageObject.isOutOwner())
              {
                f = 11.0F;
                label9043:
                this.captionX = (AndroidUtilities.dp(f) + i + this.captionOffsetX);
                i = this.totalHeight;
                j = this.captionHeight;
                if (!this.drawPinnedTop) {
                  break label9115;
                }
              }
              label9115:
              for (f = 9.0F;; f = 10.0F)
              {
                this.captionY = (i - j - AndroidUtilities.dp(f) - this.linkPreviewHeight - AndroidUtilities.dp(17.0F));
                break;
                f = 17.0F;
                break label9043;
              }
            }
            i = this.backgroundDrawableLeft;
            if (this.currentMessageObject.isOutOwner())
            {
              f = 11.0F;
              label9141:
              this.captionX = (AndroidUtilities.dp(f) + i + this.captionOffsetX);
              i = this.totalHeight;
              j = this.captionHeight;
              if (!this.drawPinnedTop) {
                break label9201;
              }
            }
            label9201:
            for (f = 9.0F;; f = 10.0F)
            {
              this.captionY = (i - j - AndroidUtilities.dp(f));
              break;
              f = 17.0F;
              break label9141;
            }
            label9208:
            i = this.backgroundDrawableLeft;
            if (this.currentMessageObject.isOutOwner()) {}
            for (f = 11.0F;; f = 17.0F)
            {
              j = i + AndroidUtilities.dp(f);
              break;
            }
            label9245:
            f = 10.0F;
            break label3978;
            label9252:
            localObject2 = "chat_inPreviewLine";
            break label4022;
            label9260:
            localObject2 = "chat_inSiteNameText";
            break label4098;
            label9268:
            if (this.hasInvoicePreview) {}
            for (i = 0;; i = AndroidUtilities.dp(10.0F)) {
              break;
            }
            label9290:
            Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
            break label4210;
            label9305:
            localObject2 = "chat_outFileInfoText";
            break label4340;
            label9313:
            localObject2 = "chat_outFileBackground";
            break label4367;
            label9321:
            localObject2 = Theme.chat_msgOutMenuDrawable;
            break label4389;
            label9329:
            Theme.chat_docNamePaint.setColor(Theme.getColor("chat_inFileNameText"));
            localObject3 = Theme.chat_infoPaint;
            if (isDrawSelectedBackground())
            {
              localObject2 = "chat_inFileInfoSelectedText";
              label9358:
              ((TextPaint)localObject3).setColor(Theme.getColor((String)localObject2));
              localObject3 = Theme.chat_docBackPaint;
              if (!isDrawSelectedBackground()) {
                break label9418;
              }
              localObject2 = "chat_inFileBackgroundSelected";
              label9385:
              ((Paint)localObject3).setColor(Theme.getColor((String)localObject2));
              if (!isDrawSelectedBackground()) {
                break label9426;
              }
            }
            label9418:
            label9426:
            for (localObject2 = Theme.chat_msgInMenuSelectedDrawable;; localObject2 = Theme.chat_msgInMenuDrawable)
            {
              break;
              localObject2 = "chat_inFileInfoText";
              break label9358;
              localObject2 = "chat_inFileBackground";
              break label9385;
            }
            label9434:
            i = this.photoImage.getImageX() + this.backgroundWidth - AndroidUtilities.dp(40.0F);
            this.otherX = i;
            j = this.photoImage.getImageY() + AndroidUtilities.dp(1.0F);
            this.otherY = j;
            setDrawableBounds((Drawable)localObject2, i, j);
            break label4459;
            label9490:
            i = 10;
            break label4577;
            label9496:
            if (this.buttonState != 1) {
              break label4577;
            }
            if (this.currentMessageObject.isOutOwner()) {}
            for (i = 8;; i = 11) {
              break;
            }
            label9526:
            i = 0;
            break label4606;
            label9531:
            this.radialProgress.swapBackground(Theme.chat_photoStatesDrawables[this.buttonState][this.buttonPressed]);
            break label4616;
            label9555:
            localObject3 = "chat_outFileProgress";
            break label4729;
            label9563:
            localObject4 = this.radialProgress;
            if (isDrawSelectedBackground()) {}
            for (localObject3 = "chat_inFileProgressSelected";; localObject3 = "chat_inFileProgress")
            {
              ((RadialProgress)localObject4).setProgressColor(Theme.getColor((String)localObject3));
              i = m;
              break;
            }
            label9605:
            if (this.buttonState == -1) {
              this.radialProgress.setHideCurrentDrawable(true);
            }
            this.radialProgress.setProgressColor(Theme.getColor("chat_mediaProgress"));
            i = m;
            break label4742;
            label9640:
            i = this.buttonX;
            j = this.backgroundWidth;
            if (this.currentMessageObject.type == 0)
            {
              f = 58.0F;
              label9665:
              i = j + i - AndroidUtilities.dp(f);
              this.otherX = i;
              j = this.buttonY - AndroidUtilities.dp(5.0F);
              this.otherY = j;
              setDrawableBounds((Drawable)localObject2, i, j);
              k = this.buttonX + AndroidUtilities.dp(53.0F);
              j = this.buttonY + AndroidUtilities.dp(4.0F);
              i = this.buttonY + AndroidUtilities.dp(27.0F);
              if (!this.currentMessageObject.isOutOwner()) {
                break label9808;
              }
              localObject4 = this.radialProgress;
              if ((!isDrawSelectedBackground()) && (this.buttonPressed == 0)) {
                break label9800;
              }
            }
            label9800:
            for (localObject3 = "chat_outAudioSelectedProgress";; localObject3 = "chat_outAudioProgress")
            {
              ((RadialProgress)localObject4).setProgressColor(Theme.getColor((String)localObject3));
              break;
              f = 48.0F;
              break label9665;
            }
            label9808:
            localObject4 = this.radialProgress;
            if ((isDrawSelectedBackground()) || (this.buttonPressed != 0)) {}
            for (localObject3 = "chat_inAudioSelectedProgress";; localObject3 = "chat_inAudioProgress")
            {
              ((RadialProgress)localObject4).setProgressColor(Theme.getColor((String)localObject3));
              break;
            }
            localException2 = localException2;
            FileLog.e(localException2);
          }
        }
        catch (Exception localException3)
        {
          label9874:
          label9913:
          label10127:
          do
          {
            do
            {
              for (;;)
              {
                FileLog.e(localException3);
                continue;
                i = this.backgroundDrawableLeft;
                if (this.mediaBackground) {}
                for (f = 1.0F;; f = 7.0F)
                {
                  i += AndroidUtilities.dp(f);
                  break;
                }
                PorterDuffColorFilter localPorterDuffColorFilter = Theme.colorFilter;
                continue;
                if (!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonSwitchInline)) {
                  break;
                }
                k = ((BotButton)localObject3).x;
                n = ((BotButton)localObject3).width;
                i1 = AndroidUtilities.dp(3.0F);
                i2 = Theme.chat_botInlineDrawable.getIntrinsicWidth();
                setDrawableBounds(Theme.chat_botInlineDrawable, k + n - i1 - i2 + i, AndroidUtilities.dp(3.0F) + m);
                Theme.chat_botInlineDrawable.draw(paramCanvas);
              }
            } while ((!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonCallback)) && (!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonRequestGeoLocation)) && (!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonGame)) && (!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonBuy)));
            if ((((((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonCallback)) || ((((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonGame)) || ((((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonBuy))) && ((!SendMessagesHelper.getInstance(this.currentAccount).isSendingCallback(this.currentMessageObject, ((BotButton)localObject3).button)) && ((!(((BotButton)localObject3).button instanceof TLRPC.TL_keyboardButtonRequestGeoLocation)) || (!SendMessagesHelper.getInstance(this.currentAccount).isSendingCurrentLocation(this.currentMessageObject, ((BotButton)localObject3).button))))) {
              break;
            }
            k = 1;
          } while ((k == 0) && ((k != 0) || (((BotButton)localObject3).progressAlpha == 0.0F)));
          Theme.chat_botProgressPaint.setAlpha(Math.min(255, (int)(((BotButton)localObject3).progressAlpha * 255.0F)));
          n = ((BotButton)localObject3).x + ((BotButton)localObject3).width - AndroidUtilities.dp(12.0F) + i;
          this.rect.set(n, AndroidUtilities.dp(4.0F) + m, AndroidUtilities.dp(8.0F) + n, AndroidUtilities.dp(12.0F) + m);
          paramCanvas.drawArc(this.rect, ((BotButton)localObject3).angle, 220.0F, false, Theme.chat_botProgressPaint);
          invalidate((int)this.rect.left - AndroidUtilities.dp(2.0F), (int)this.rect.top - AndroidUtilities.dp(2.0F), (int)this.rect.right + AndroidUtilities.dp(2.0F), (int)this.rect.bottom + AndroidUtilities.dp(2.0F));
          l1 = System.currentTimeMillis();
          if (Math.abs(((BotButton)localObject3).lastUpdateTime - System.currentTimeMillis()) < 1000L)
          {
            l2 = l1 - ((BotButton)localObject3).lastUpdateTime;
            f = (float)(360L * l2) / 2000.0F;
            BotButton.access$1202((BotButton)localObject3, (int)(((BotButton)localObject3).angle + f));
            BotButton.access$1202((BotButton)localObject3, ((BotButton)localObject3).angle - ((BotButton)localObject3).angle / 360 * 360);
            if (k == 0) {
              break label10462;
            }
            if (((BotButton)localObject3).progressAlpha < 1.0F)
            {
              BotButton.access$1102((BotButton)localObject3, ((BotButton)localObject3).progressAlpha + (float)l2 / 200.0F);
              if (((BotButton)localObject3).progressAlpha > 1.0F) {
                BotButton.access$1102((BotButton)localObject3, 1.0F);
              }
            }
          }
          for (;;)
          {
            BotButton.access$1302((BotButton)localObject3, l1);
            break;
            k = 0;
            break label10127;
            label10462:
            if (((BotButton)localObject3).progressAlpha > 0.0F)
            {
              BotButton.access$1102((BotButton)localObject3, ((BotButton)localObject3).progressAlpha - (float)l2 / 200.0F);
              if (((BotButton)localObject3).progressAlpha < 0.0F) {
                BotButton.access$1102((BotButton)localObject3, 0.0F);
              }
            }
          }
        }
      }
    }
  }
  
  public static StaticLayout generateStaticLayout(CharSequence paramCharSequence, TextPaint paramTextPaint, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder(paramCharSequence);
    int j = 0;
    StaticLayout localStaticLayout = new StaticLayout(paramCharSequence, paramTextPaint, paramInt2, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
    int i = 0;
    int m = paramInt1;
    int k;
    if (i < paramInt3)
    {
      localStaticLayout.getLineDirections(i);
      if ((localStaticLayout.getLineLeft(i) != 0.0F) || (localStaticLayout.isRtlCharAt(localStaticLayout.getLineStart(i))) || (localStaticLayout.isRtlCharAt(localStaticLayout.getLineEnd(i)))) {
        paramInt1 = paramInt2;
      }
      k = localStaticLayout.getLineEnd(i);
      if (k == paramCharSequence.length()) {
        m = paramInt1;
      }
    }
    else
    {
      label119:
      return StaticLayoutEx.createStaticLayout(localSpannableStringBuilder, paramTextPaint, m, Layout.Alignment.ALIGN_NORMAL, 1.0F, AndroidUtilities.dp(1.0F), false, TextUtils.TruncateAt.END, m, paramInt4);
    }
    m = k - 1;
    if (localSpannableStringBuilder.charAt(m + j) == ' ')
    {
      localSpannableStringBuilder.replace(m + j, m + j + 1, "\n");
      k = j;
    }
    for (;;)
    {
      m = paramInt1;
      if (i == localStaticLayout.getLineCount() - 1) {
        break label119;
      }
      m = paramInt1;
      if (i == paramInt4 - 1) {
        break label119;
      }
      i += 1;
      j = k;
      break;
      k = j;
      if (localSpannableStringBuilder.charAt(m + j) != '\n')
      {
        localSpannableStringBuilder.insert(m + j, "\n");
        k = j + 1;
      }
    }
  }
  
  private int getAdditionalWidthForPosition(MessageObject.GroupedMessagePosition paramGroupedMessagePosition)
  {
    int i = 0;
    int j = 0;
    if (paramGroupedMessagePosition != null)
    {
      if ((paramGroupedMessagePosition.flags & 0x2) == 0) {
        j = 0 + AndroidUtilities.dp(4.0F);
      }
      i = j;
      if ((paramGroupedMessagePosition.flags & 0x1) == 0) {
        i = j + AndroidUtilities.dp(4.0F);
      }
    }
    return i;
  }
  
  private Drawable getDrawableForCurrentState()
  {
    int i = 3;
    int j = 0;
    int k = 1;
    int m = 1;
    int n = 1;
    int i1 = 1;
    Object localObject;
    if ((this.documentAttachType == 3) || (this.documentAttachType == 5))
    {
      if (this.buttonState == -1) {
        return null;
      }
      this.radialProgress.setAlphaForPrevious(false);
      this.radialProgress.setAlphaForMiniPrevious(true);
      localObject = Theme.chat_fileStatesDrawable;
      if (this.currentMessageObject.isOutOwner())
      {
        i = this.buttonState;
        localObject = localObject[i];
        if ((!isDrawSelectedBackground()) && (this.buttonPressed == 0)) {
          break label114;
        }
      }
      label114:
      for (i = 1;; i = 0)
      {
        return localObject[i];
        i = this.buttonState + 5;
        break;
      }
    }
    if ((this.documentAttachType == 1) && (!this.drawPhotoImage))
    {
      this.radialProgress.setAlphaForPrevious(false);
      if (this.buttonState == -1)
      {
        localObject = Theme.chat_fileStatesDrawable;
        if (this.currentMessageObject.isOutOwner())
        {
          localObject = localObject[i];
          if (!isDrawSelectedBackground()) {
            break label192;
          }
        }
        label192:
        for (i = i1;; i = 0)
        {
          return localObject[i];
          i = 8;
          break;
        }
      }
      if (this.buttonState == 0)
      {
        localObject = Theme.chat_fileStatesDrawable;
        if (this.currentMessageObject.isOutOwner())
        {
          i = 2;
          localObject = localObject[i];
          if (!isDrawSelectedBackground()) {
            break label247;
          }
        }
        label247:
        for (i = k;; i = 0)
        {
          return localObject[i];
          i = 7;
          break;
        }
      }
      if (this.buttonState == 1)
      {
        localObject = Theme.chat_fileStatesDrawable;
        if (this.currentMessageObject.isOutOwner())
        {
          i = 4;
          localObject = localObject[i];
          if (!isDrawSelectedBackground()) {
            break label304;
          }
        }
        label304:
        for (i = m;; i = 0)
        {
          return localObject[i];
          i = 9;
          break;
        }
      }
    }
    else
    {
      this.radialProgress.setAlphaForPrevious(true);
      if ((this.buttonState >= 0) && (this.buttonState < 4))
      {
        if (this.documentAttachType == 1)
        {
          i = this.buttonState;
          if (this.buttonState == 0) {
            if (this.currentMessageObject.isOutOwner()) {
              i = 7;
            }
          }
          while (this.buttonState != 1) {
            for (;;)
            {
              localObject = Theme.chat_photoStatesDrawables[i];
              if (!isDrawSelectedBackground())
              {
                i = j;
                if (this.buttonPressed == 0) {}
              }
              else
              {
                i = 1;
              }
              return localObject[i];
              i = 10;
            }
          }
          if (this.currentMessageObject.isOutOwner()) {}
          for (i = 8;; i = 11) {
            break;
          }
        }
        return Theme.chat_photoStatesDrawables[this.buttonState][this.buttonPressed];
      }
      if ((this.buttonState == -1) && (this.documentAttachType == 1))
      {
        localObject = Theme.chat_photoStatesDrawables;
        if (this.currentMessageObject.isOutOwner())
        {
          i = 9;
          localObject = localObject[i];
          if (!isDrawSelectedBackground()) {
            break label506;
          }
        }
        label506:
        for (i = n;; i = 0)
        {
          return localObject[i];
          i = 12;
          break;
        }
      }
    }
    return null;
  }
  
  private int getGroupPhotosWidth()
  {
    if ((!AndroidUtilities.isInMultiwindow) && (AndroidUtilities.isTablet()) && ((!AndroidUtilities.isSmallTablet()) || (getResources().getConfiguration().orientation == 2)))
    {
      int j = AndroidUtilities.displaySize.x / 100 * 35;
      int i = j;
      if (j < AndroidUtilities.dp(320.0F)) {
        i = AndroidUtilities.dp(320.0F);
      }
      return AndroidUtilities.displaySize.x - i;
    }
    return AndroidUtilities.displaySize.x;
  }
  
  private int getMaxNameWidth()
  {
    if ((this.documentAttachType == 6) || (this.currentMessageObject.type == 5))
    {
      if (AndroidUtilities.isTablet()) {
        if ((this.isChat) && (!this.currentMessageObject.isOutOwner()) && (this.currentMessageObject.needDrawAvatar())) {
          i = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(42.0F);
        }
      }
      for (;;)
      {
        return i - this.backgroundWidth - AndroidUtilities.dp(57.0F);
        i = AndroidUtilities.getMinTabletSide();
        continue;
        if ((this.isChat) && (!this.currentMessageObject.isOutOwner()) && (this.currentMessageObject.needDrawAvatar())) {
          i = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(42.0F);
        } else {
          i = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
        }
      }
    }
    if (this.currentMessagesGroup != null)
    {
      if (AndroidUtilities.isTablet()) {}
      int j;
      for (i = AndroidUtilities.getMinTabletSide();; i = AndroidUtilities.displaySize.x)
      {
        j = 0;
        int k = 0;
        while (k < this.currentMessagesGroup.posArray.size())
        {
          MessageObject.GroupedMessagePosition localGroupedMessagePosition = (MessageObject.GroupedMessagePosition)this.currentMessagesGroup.posArray.get(k);
          if (localGroupedMessagePosition.minY != 0) {
            break;
          }
          j = (int)(j + Math.ceil((localGroupedMessagePosition.pw + localGroupedMessagePosition.leftSpanOffset) / 1000.0F * i));
          k += 1;
        }
      }
      if (this.isAvatarVisible) {}
      for (i = 48;; i = 0) {
        return j - AndroidUtilities.dp(i + 31);
      }
    }
    int i = this.backgroundWidth;
    if (this.mediaBackground) {}
    for (float f = 22.0F;; f = 31.0F) {
      return i - AndroidUtilities.dp(f);
    }
  }
  
  private Drawable getMiniDrawableForCurrentState()
  {
    int i = 1;
    if (this.miniButtonState < 0) {}
    label88:
    do
    {
      return null;
      if ((this.documentAttachType == 3) || (this.documentAttachType == 5))
      {
        this.radialProgress.setAlphaForPrevious(false);
        localObject = Theme.chat_fileMiniStatesDrawable;
        if (this.currentMessageObject.isOutOwner())
        {
          i = this.miniButtonState;
          localObject = localObject[i];
          if ((!isDrawSelectedBackground()) && (this.miniButtonPressed == 0)) {
            break label88;
          }
        }
        for (i = 1;; i = 0)
        {
          return localObject[i];
          i = this.miniButtonState + 2;
          break;
        }
      }
    } while (this.documentAttachType != 4);
    Object localObject = Theme.chat_fileMiniStatesDrawable[(this.miniButtonState + 4)];
    if (this.miniButtonPressed != 0) {}
    for (;;)
    {
      return localObject[i];
      i = 0;
    }
  }
  
  private boolean intersect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    if (paramFloat1 <= paramFloat3) {
      if (paramFloat2 < paramFloat3) {}
    }
    while (paramFloat1 <= paramFloat4)
    {
      return true;
      return false;
    }
    return false;
  }
  
  private boolean isCurrentLocationTimeExpired(MessageObject paramMessageObject)
  {
    if (this.currentMessageObject.messageOwner.media.period % 60 == 0) {
      if (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - paramMessageObject.messageOwner.date) <= paramMessageObject.messageOwner.media.period) {}
    }
    while (Math.abs(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() - paramMessageObject.messageOwner.date) > paramMessageObject.messageOwner.media.period - 5)
    {
      return true;
      return false;
    }
    return false;
  }
  
  private boolean isDrawSelectedBackground()
  {
    return ((isPressed()) && (this.isCheckPressed)) || ((!this.isCheckPressed) && (this.isPressed)) || (this.isHighlighted);
  }
  
  private boolean isOpenChatByShare(MessageObject paramMessageObject)
  {
    return (paramMessageObject.messageOwner.fwd_from != null) && (paramMessageObject.messageOwner.fwd_from.saved_from_peer != null);
  }
  
  private boolean isPhotoDataChanged(MessageObject paramMessageObject)
  {
    if ((paramMessageObject.type == 0) || (paramMessageObject.type == 14)) {
      return false;
    }
    if (paramMessageObject.type == 4)
    {
      if (this.currentUrl == null) {
        return true;
      }
      double d2 = paramMessageObject.messageOwner.media.geo.lat;
      double d1 = paramMessageObject.messageOwner.media.geo._long;
      if ((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
      {
        int i = this.backgroundWidth;
        int j = AndroidUtilities.dp(21.0F);
        int k = AndroidUtilities.dp(195.0F);
        double d3 = 268435456 / 3.141592653589793D;
        d2 = (1.5707963267948966D - 2.0D * Math.atan(Math.exp((Math.round(268435456 - Math.log((1.0D + Math.sin(3.141592653589793D * d2 / 180.0D)) / (1.0D - Math.sin(3.141592653589793D * d2 / 180.0D))) * d3 / 2.0D) - (AndroidUtilities.dp(10.3F) << 6) - 268435456) / d3))) * 180.0D / 3.141592653589793D;
        paramMessageObject = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=%dx%d&maptype=roadmap&scale=%d&sensor=false", new Object[] { Double.valueOf(d2), Double.valueOf(d1), Integer.valueOf((int)((i - j) / AndroidUtilities.density)), Integer.valueOf((int)(k / AndroidUtilities.density)), Integer.valueOf(Math.min(2, (int)Math.ceil(AndroidUtilities.density))) });
      }
      while (!paramMessageObject.equals(this.currentUrl))
      {
        return true;
        if (!TextUtils.isEmpty(paramMessageObject.messageOwner.media.title)) {
          paramMessageObject = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=72x72&maptype=roadmap&scale=%d&markers=color:red|size:mid|%f,%f&sensor=false", new Object[] { Double.valueOf(d2), Double.valueOf(d1), Integer.valueOf(Math.min(2, (int)Math.ceil(AndroidUtilities.density))), Double.valueOf(d2), Double.valueOf(d1) });
        } else {
          paramMessageObject = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=15&size=200x100&maptype=roadmap&scale=%d&markers=color:red|size:mid|%f,%f&sensor=false", new Object[] { Double.valueOf(d2), Double.valueOf(d1), Integer.valueOf(Math.min(2, (int)Math.ceil(AndroidUtilities.density))), Double.valueOf(d2), Double.valueOf(d1) });
        }
      }
    }
    if ((this.currentPhotoObject == null) || ((this.currentPhotoObject.location instanceof TLRPC.TL_fileLocationUnavailable)))
    {
      if ((paramMessageObject.type == 1) || (paramMessageObject.type == 5) || (paramMessageObject.type == 3) || (paramMessageObject.type == 8) || (paramMessageObject.type == 13)) {
        return true;
      }
    }
    else if ((this.currentMessageObject != null) && (this.photoNotSet) && (FileLoader.getPathToMessage(this.currentMessageObject.messageOwner).exists())) {
      return true;
    }
    return false;
  }
  
  private boolean isUserDataChanged()
  {
    boolean bool2 = false;
    if ((this.currentMessageObject != null) && (!this.hasLinkPreview) && (this.currentMessageObject.messageOwner.media != null) && ((this.currentMessageObject.messageOwner.media.webpage instanceof TLRPC.TL_webPage))) {}
    label169:
    label520:
    label522:
    label541:
    for (;;)
    {
      return true;
      if ((this.currentMessageObject == null) || ((this.currentUser == null) && (this.currentChat == null))) {
        return false;
      }
      if ((this.lastSendState == this.currentMessageObject.messageOwner.send_state) && (this.lastDeleteDate == this.currentMessageObject.messageOwner.destroyTime) && (this.lastViewsCount == this.currentMessageObject.messageOwner.views))
      {
        updateCurrentUserAndChat();
        Object localObject2 = null;
        Object localObject1 = localObject2;
        if (this.isAvatarVisible)
        {
          if ((this.currentUser != null) && (this.currentUser.photo != null)) {
            localObject1 = this.currentUser.photo.photo_small;
          }
        }
        else
        {
          if (((this.replyTextLayout == null) && (this.currentMessageObject.replyMessageObject != null)) || ((this.currentPhoto == null) && (localObject1 != null)) || ((this.currentPhoto != null) && (localObject1 == null)) || ((this.currentPhoto != null) && (localObject1 != null) && ((this.currentPhoto.local_id != ((TLRPC.FileLocation)localObject1).local_id) || (this.currentPhoto.volume_id != ((TLRPC.FileLocation)localObject1).volume_id)))) {
            break label520;
          }
          localObject2 = null;
          localObject1 = localObject2;
          if (this.replyNameLayout != null)
          {
            TLRPC.PhotoSize localPhotoSize = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.replyMessageObject.photoThumbs, 80);
            localObject1 = localObject2;
            if (localPhotoSize != null)
            {
              localObject1 = localObject2;
              if (this.currentMessageObject.replyMessageObject.type != 13) {
                localObject1 = localPhotoSize.location;
              }
            }
          }
          if ((this.currentReplyPhoto == null) && (localObject1 != null)) {
            continue;
          }
          localObject2 = null;
          localObject1 = localObject2;
          if (this.drawName)
          {
            localObject1 = localObject2;
            if (this.isChat)
            {
              localObject1 = localObject2;
              if (!this.currentMessageObject.isOutOwner())
              {
                if (this.currentUser == null) {
                  break label522;
                }
                localObject1 = UserObject.getUserName(this.currentUser);
              }
            }
          }
        }
        for (;;)
        {
          if (((this.currentNameString == null) && (localObject1 != null)) || ((this.currentNameString != null) && (localObject1 == null)) || ((this.currentNameString != null) && (localObject1 != null) && (!this.currentNameString.equals(localObject1)))) {
            break label541;
          }
          if (!this.drawForwardedName) {
            break label543;
          }
          localObject1 = this.currentMessageObject.getForwardedName();
          boolean bool1;
          if (((this.currentForwardNameString != null) || (localObject1 == null)) && ((this.currentForwardNameString == null) || (localObject1 != null)))
          {
            bool1 = bool2;
            if (this.currentForwardNameString != null)
            {
              bool1 = bool2;
              if (localObject1 != null)
              {
                bool1 = bool2;
                if (this.currentForwardNameString.equals(localObject1)) {}
              }
            }
          }
          else
          {
            bool1 = true;
          }
          return bool1;
          localObject1 = localObject2;
          if (this.currentChat == null) {
            break label169;
          }
          localObject1 = localObject2;
          if (this.currentChat.photo == null) {
            break label169;
          }
          localObject1 = this.currentChat.photo.photo_small;
          break label169;
          break;
          localObject1 = localObject2;
          if (this.currentChat != null) {
            localObject1 = this.currentChat.title;
          }
        }
      }
    }
    label543:
    return false;
  }
  
  private void measureTime(MessageObject paramMessageObject)
  {
    Object localObject1;
    Object localObject2;
    label189:
    label219:
    int i;
    if (paramMessageObject.messageOwner.post_author != null)
    {
      localObject1 = paramMessageObject.messageOwner.post_author.replace("\n", "");
      localObject2 = null;
      if (this.currentMessageObject.isFromUser()) {
        localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id));
      }
      if ((paramMessageObject.isLiveLocation()) || (paramMessageObject.getDialogId() == 777000L) || (paramMessageObject.messageOwner.via_bot_id != 0) || (paramMessageObject.messageOwner.via_bot_name != null) || ((localObject2 != null) && (((TLRPC.User)localObject2).bot)) || ((paramMessageObject.messageOwner.flags & 0x8000) == 0) || (this.currentPosition != null)) {
        break label624;
      }
      localObject2 = LocaleController.getString("EditedMessage", 2131493417) + " " + LocaleController.getInstance().formatterDay.format(paramMessageObject.messageOwner.date * 1000L);
      if (localObject1 == null) {
        break label650;
      }
      this.currentTimeString = (", " + (String)localObject2);
      i = (int)Math.ceil(Theme.chat_timePaint.measureText(this.currentTimeString));
      this.timeWidth = i;
      this.timeTextWidth = i;
      if ((paramMessageObject.messageOwner.flags & 0x400) != 0)
      {
        this.currentViewsString = String.format("%s", new Object[] { LocaleController.formatShortNumber(Math.max(1, paramMessageObject.messageOwner.views), null) });
        this.viewsTextWidth = ((int)Math.ceil(Theme.chat_timePaint.measureText(this.currentViewsString)));
        this.timeWidth += this.viewsTextWidth + Theme.chat_msgInViewsDrawable.getIntrinsicWidth() + AndroidUtilities.dp(10.0F);
      }
      if (localObject1 != null)
      {
        if (this.availableTimeWidth == 0) {
          this.availableTimeWidth = AndroidUtilities.dp(1000.0F);
        }
        j = this.availableTimeWidth - this.timeWidth;
        i = j;
        if (paramMessageObject.isOutOwner())
        {
          if (paramMessageObject.type != 5) {
            break label659;
          }
          i = j - AndroidUtilities.dp(20.0F);
        }
        label395:
        int k = (int)Math.ceil(Theme.chat_timePaint.measureText((CharSequence)localObject1, 0, ((CharSequence)localObject1).length()));
        paramMessageObject = (MessageObject)localObject1;
        j = k;
        if (k > i)
        {
          if (i > 0) {
            break label671;
          }
          paramMessageObject = "";
        }
      }
    }
    for (int j = 0;; j = i)
    {
      this.currentTimeString = (paramMessageObject + this.currentTimeString);
      this.timeTextWidth += j;
      this.timeWidth += j;
      return;
      if ((paramMessageObject.messageOwner.fwd_from != null) && (paramMessageObject.messageOwner.fwd_from.post_author != null))
      {
        localObject1 = paramMessageObject.messageOwner.fwd_from.post_author.replace("\n", "");
        break;
      }
      if ((!paramMessageObject.isOutOwner()) && (paramMessageObject.messageOwner.from_id > 0) && (paramMessageObject.messageOwner.post))
      {
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id));
        if (localObject1 != null)
        {
          localObject1 = ContactsController.formatName(((TLRPC.User)localObject1).first_name, ((TLRPC.User)localObject1).last_name).replace('\n', ' ');
          break;
        }
        localObject1 = null;
        break;
      }
      localObject1 = null;
      break;
      label624:
      localObject2 = LocaleController.getInstance().formatterDay.format(paramMessageObject.messageOwner.date * 1000L);
      break label189;
      label650:
      this.currentTimeString = ((String)localObject2);
      break label219;
      label659:
      i = j - AndroidUtilities.dp(96.0F);
      break label395;
      label671:
      paramMessageObject = TextUtils.ellipsize((CharSequence)localObject1, Theme.chat_timePaint, i, TextUtils.TruncateAt.END);
    }
  }
  
  private LinkPath obtainNewUrlPath(boolean paramBoolean)
  {
    LinkPath localLinkPath;
    if (!this.urlPathCache.isEmpty())
    {
      localLinkPath = (LinkPath)this.urlPathCache.get(0);
      this.urlPathCache.remove(0);
    }
    while (paramBoolean)
    {
      this.urlPathSelection.add(localLinkPath);
      return localLinkPath;
      localLinkPath = new LinkPath();
    }
    this.urlPath.add(localLinkPath);
    return localLinkPath;
  }
  
  private void resetPressedLink(int paramInt)
  {
    if ((this.pressedLink == null) || ((this.pressedLinkType != paramInt) && (paramInt != -1))) {
      return;
    }
    resetUrlPaths(false);
    this.pressedLink = null;
    this.pressedLinkType = -1;
    invalidate();
  }
  
  private void resetUrlPaths(boolean paramBoolean)
  {
    if (paramBoolean) {
      if (!this.urlPathSelection.isEmpty()) {}
    }
    while (this.urlPath.isEmpty())
    {
      return;
      this.urlPathCache.addAll(this.urlPathSelection);
      this.urlPathSelection.clear();
      return;
    }
    this.urlPathCache.addAll(this.urlPath);
    this.urlPath.clear();
  }
  
  /* Error */
  private void setMessageObjectInternal(MessageObject paramMessageObject)
  {
    // Byte code:
    //   0: aload_1
    //   1: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4: getfield 2460	org/telegram/tgnet/TLRPC$Message:flags	I
    //   7: sipush 1024
    //   10: iand
    //   11: ifeq +38 -> 49
    //   14: aload_0
    //   15: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   18: getfield 2535	org/telegram/messenger/MessageObject:viewsReloaded	Z
    //   21: ifne +28 -> 49
    //   24: aload_0
    //   25: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   28: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   31: aload_0
    //   32: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   35: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   38: invokevirtual 2539	org/telegram/messenger/MessagesController:addToViewsQueue	(Lorg/telegram/tgnet/TLRPC$Message;)V
    //   41: aload_0
    //   42: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   45: iconst_1
    //   46: putfield 2535	org/telegram/messenger/MessageObject:viewsReloaded	Z
    //   49: aload_0
    //   50: invokespecial 2393	org/telegram/ui/Cells/ChatMessageCell:updateCurrentUserAndChat	()V
    //   53: aload_0
    //   54: getfield 2278	org/telegram/ui/Cells/ChatMessageCell:isAvatarVisible	Z
    //   57: ifeq +65 -> 122
    //   60: aload_0
    //   61: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   64: ifnull +2100 -> 2164
    //   67: aload_0
    //   68: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   71: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   74: ifnull +2082 -> 2156
    //   77: aload_0
    //   78: aload_0
    //   79: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   82: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   85: getfield 2402	org/telegram/tgnet/TLRPC$UserProfilePhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   88: putfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   91: aload_0
    //   92: getfield 382	org/telegram/ui/Cells/ChatMessageCell:avatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   95: aload_0
    //   96: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   99: invokevirtual 2543	org/telegram/ui/Components/AvatarDrawable:setInfo	(Lorg/telegram/tgnet/TLRPC$User;)V
    //   102: aload_0
    //   103: getfield 366	org/telegram/ui/Cells/ChatMessageCell:avatarImage	Lorg/telegram/messenger/ImageReceiver;
    //   106: aload_0
    //   107: getfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   110: ldc_w 2545
    //   113: aload_0
    //   114: getfield 382	org/telegram/ui/Cells/ChatMessageCell:avatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   117: aconst_null
    //   118: iconst_0
    //   119: invokevirtual 2548	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Ljava/lang/String;I)V
    //   122: aload_0
    //   123: aload_1
    //   124: invokespecial 1191	org/telegram/ui/Cells/ChatMessageCell:measureTime	(Lorg/telegram/messenger/MessageObject;)V
    //   127: aload_0
    //   128: iconst_0
    //   129: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   132: aconst_null
    //   133: astore 8
    //   135: aconst_null
    //   136: astore 9
    //   138: aload_1
    //   139: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   142: getfield 1064	org/telegram/tgnet/TLRPC$Message:via_bot_id	I
    //   145: ifeq +2097 -> 2242
    //   148: aload_0
    //   149: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   152: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   155: aload_1
    //   156: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   159: getfield 1064	org/telegram/tgnet/TLRPC$Message:via_bot_id	I
    //   162: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   165: invokevirtual 1019	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   168: astore 10
    //   170: aload 9
    //   172: astore 7
    //   174: aload 8
    //   176: astore 6
    //   178: aload 10
    //   180: ifnull +115 -> 295
    //   183: aload 9
    //   185: astore 7
    //   187: aload 8
    //   189: astore 6
    //   191: aload 10
    //   193: getfield 2549	org/telegram/tgnet/TLRPC$User:username	Ljava/lang/String;
    //   196: ifnull +99 -> 295
    //   199: aload 9
    //   201: astore 7
    //   203: aload 8
    //   205: astore 6
    //   207: aload 10
    //   209: getfield 2549	org/telegram/tgnet/TLRPC$User:username	Ljava/lang/String;
    //   212: invokevirtual 1054	java/lang/String:length	()I
    //   215: ifle +80 -> 295
    //   218: new 1320	java/lang/StringBuilder
    //   221: dup
    //   222: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   225: ldc_w 2551
    //   228: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   231: aload 10
    //   233: getfield 2549	org/telegram/tgnet/TLRPC$User:username	Ljava/lang/String;
    //   236: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   239: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   242: astore 6
    //   244: ldc_w 2553
    //   247: iconst_1
    //   248: anewarray 1242	java/lang/Object
    //   251: dup
    //   252: iconst_0
    //   253: aload 6
    //   255: aastore
    //   256: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   259: invokestatic 2557	org/telegram/messenger/AndroidUtilities:replaceTags	(Ljava/lang/String;)Landroid/text/SpannableStringBuilder;
    //   262: astore 7
    //   264: aload_0
    //   265: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   268: aload 7
    //   270: iconst_0
    //   271: aload 7
    //   273: invokeinterface 2192 1 0
    //   278: invokevirtual 2500	android/text/TextPaint:measureText	(Ljava/lang/CharSequence;II)F
    //   281: f2d
    //   282: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   285: d2i
    //   286: putfield 2559	org/telegram/ui/Cells/ChatMessageCell:viaWidth	I
    //   289: aload_0
    //   290: aload 10
    //   292: putfield 2561	org/telegram/ui/Cells/ChatMessageCell:currentViaBotUser	Lorg/telegram/tgnet/TLRPC$User;
    //   295: aload_0
    //   296: getfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   299: ifeq +2058 -> 2357
    //   302: aload_0
    //   303: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   306: ifeq +2051 -> 2357
    //   309: aload_0
    //   310: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   313: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   316: ifne +2041 -> 2357
    //   319: iconst_1
    //   320: istore 4
    //   322: aload_1
    //   323: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   326: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   329: ifnull +12 -> 341
    //   332: aload_1
    //   333: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   336: bipush 14
    //   338: if_icmpne +2025 -> 2363
    //   341: aload 6
    //   343: ifnull +2020 -> 2363
    //   346: iconst_1
    //   347: istore_3
    //   348: iload 4
    //   350: ifne +7 -> 357
    //   353: iload_3
    //   354: ifeq +2203 -> 2557
    //   357: aload_0
    //   358: iconst_1
    //   359: putfield 2563	org/telegram/ui/Cells/ChatMessageCell:drawNameLayout	Z
    //   362: aload_0
    //   363: aload_0
    //   364: invokespecial 2565	org/telegram/ui/Cells/ChatMessageCell:getMaxNameWidth	()I
    //   367: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   370: aload_0
    //   371: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   374: ifge +13 -> 387
    //   377: aload_0
    //   378: ldc_w 2568
    //   381: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   384: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   387: aload_0
    //   388: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   391: ifnull +1977 -> 2368
    //   394: aload_0
    //   395: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   398: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   401: ifne +1967 -> 2368
    //   404: aload_0
    //   405: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   408: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   411: bipush 13
    //   413: if_icmpeq +1955 -> 2368
    //   416: aload_0
    //   417: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   420: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   423: iconst_5
    //   424: if_icmpeq +1944 -> 2368
    //   427: aload_0
    //   428: getfield 618	org/telegram/ui/Cells/ChatMessageCell:delegate	Lorg/telegram/ui/Cells/ChatMessageCell$ChatMessageCellDelegate;
    //   431: aload_0
    //   432: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   435: getfield 2571	org/telegram/tgnet/TLRPC$User:id	I
    //   438: invokeinterface 2574 2 0
    //   443: ifeq +1925 -> 2368
    //   446: ldc_w 2576
    //   449: ldc_w 2577
    //   452: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   455: astore 8
    //   457: getstatic 2580	org/telegram/ui/ActionBar/Theme:chat_adminPaint	Landroid/text/TextPaint;
    //   460: aload 8
    //   462: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   465: f2d
    //   466: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   469: d2i
    //   470: istore_2
    //   471: aload_0
    //   472: aload_0
    //   473: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   476: iload_2
    //   477: isub
    //   478: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   481: iload 4
    //   483: ifeq +1924 -> 2407
    //   486: aload_0
    //   487: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   490: ifnull +1886 -> 2376
    //   493: aload_0
    //   494: aload_0
    //   495: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   498: invokestatic 2427	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   501: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   504: aload_0
    //   505: getfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   508: bipush 10
    //   510: bipush 32
    //   512: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   515: astore 9
    //   517: getstatic 2583	org/telegram/ui/ActionBar/Theme:chat_namePaint	Landroid/text/TextPaint;
    //   520: astore 10
    //   522: aload_0
    //   523: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   526: istore 5
    //   528: iload_3
    //   529: ifeq +1888 -> 2417
    //   532: aload_0
    //   533: getfield 2559	org/telegram/ui/Cells/ChatMessageCell:viaWidth	I
    //   536: istore 4
    //   538: aload 9
    //   540: aload 10
    //   542: iload 5
    //   544: iload 4
    //   546: isub
    //   547: i2f
    //   548: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   551: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   554: astore 10
    //   556: aload 10
    //   558: astore 9
    //   560: iload_3
    //   561: ifeq +210 -> 771
    //   564: aload_0
    //   565: getstatic 2583	org/telegram/ui/ActionBar/Theme:chat_namePaint	Landroid/text/TextPaint;
    //   568: aload 10
    //   570: iconst_0
    //   571: aload 10
    //   573: invokeinterface 2192 1 0
    //   578: invokevirtual 2500	android/text/TextPaint:measureText	(Ljava/lang/CharSequence;II)F
    //   581: f2d
    //   582: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   585: d2i
    //   586: putfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   589: aload_0
    //   590: getfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   593: ifeq +18 -> 611
    //   596: aload_0
    //   597: aload_0
    //   598: getfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   601: ldc_w 1078
    //   604: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   607: iadd
    //   608: putfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   611: aload_0
    //   612: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   615: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   618: bipush 13
    //   620: if_icmpeq +14 -> 634
    //   623: aload_0
    //   624: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   627: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   630: iconst_5
    //   631: if_icmpne +1792 -> 2423
    //   634: ldc_w 2587
    //   637: invokestatic 1511	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   640: istore_3
    //   641: aload_0
    //   642: getfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   645: invokevirtual 1054	java/lang/String:length	()I
    //   648: ifle +1807 -> 2455
    //   651: new 2172	android/text/SpannableStringBuilder
    //   654: dup
    //   655: ldc_w 2589
    //   658: iconst_2
    //   659: anewarray 1242	java/lang/Object
    //   662: dup
    //   663: iconst_0
    //   664: aload 10
    //   666: aastore
    //   667: dup
    //   668: iconst_1
    //   669: aload 6
    //   671: aastore
    //   672: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   675: invokespecial 2175	android/text/SpannableStringBuilder:<init>	(Ljava/lang/CharSequence;)V
    //   678: astore 9
    //   680: aload 9
    //   682: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   685: dup
    //   686: getstatic 2597	android/graphics/Typeface:DEFAULT	Landroid/graphics/Typeface;
    //   689: iconst_0
    //   690: iload_3
    //   691: invokespecial 2600	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;II)V
    //   694: aload 10
    //   696: invokeinterface 2192 1 0
    //   701: iconst_1
    //   702: iadd
    //   703: aload 10
    //   705: invokeinterface 2192 1 0
    //   710: iconst_4
    //   711: iadd
    //   712: bipush 33
    //   714: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   717: aload 9
    //   719: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   722: dup
    //   723: ldc_w 2606
    //   726: invokestatic 2610	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   729: iconst_0
    //   730: iload_3
    //   731: invokespecial 2600	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;II)V
    //   734: aload 10
    //   736: invokeinterface 2192 1 0
    //   741: iconst_5
    //   742: iadd
    //   743: aload 9
    //   745: invokevirtual 2611	android/text/SpannableStringBuilder:length	()I
    //   748: bipush 33
    //   750: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   753: aload 9
    //   755: getstatic 2583	org/telegram/ui/ActionBar/Theme:chat_namePaint	Landroid/text/TextPaint;
    //   758: aload_0
    //   759: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   762: i2f
    //   763: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   766: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   769: astore 9
    //   771: aload_0
    //   772: new 350	android/text/StaticLayout
    //   775: dup
    //   776: aload 9
    //   778: getstatic 2583	org/telegram/ui/ActionBar/Theme:chat_namePaint	Landroid/text/TextPaint;
    //   781: aload_0
    //   782: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   785: fconst_2
    //   786: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   789: iadd
    //   790: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   793: fconst_1
    //   794: fconst_0
    //   795: iconst_0
    //   796: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   799: putfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   802: aload_0
    //   803: getfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   806: ifnull +1725 -> 2531
    //   809: aload_0
    //   810: getfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   813: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   816: ifle +1715 -> 2531
    //   819: aload_0
    //   820: aload_0
    //   821: getfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   824: iconst_0
    //   825: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   828: f2d
    //   829: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   832: d2i
    //   833: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   836: aload_1
    //   837: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   840: bipush 13
    //   842: if_icmpeq +18 -> 860
    //   845: aload_0
    //   846: aload_0
    //   847: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   850: ldc_w 1646
    //   853: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   856: iadd
    //   857: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   860: aload_0
    //   861: aload_0
    //   862: getfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   865: iconst_0
    //   866: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   869: putfield 2615	org/telegram/ui/Cells/ChatMessageCell:nameOffsetX	F
    //   872: aload 8
    //   874: ifnull +1675 -> 2549
    //   877: aload_0
    //   878: new 350	android/text/StaticLayout
    //   881: dup
    //   882: aload 8
    //   884: getstatic 2580	org/telegram/ui/ActionBar/Theme:chat_adminPaint	Landroid/text/TextPaint;
    //   887: iload_2
    //   888: fconst_2
    //   889: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   892: iadd
    //   893: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   896: fconst_1
    //   897: fconst_0
    //   898: iconst_0
    //   899: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   902: putfield 2617	org/telegram/ui/Cells/ChatMessageCell:adminLayout	Landroid/text/StaticLayout;
    //   905: aload_0
    //   906: aload_0
    //   907: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   910: i2f
    //   911: aload_0
    //   912: getfield 2617	org/telegram/ui/Cells/ChatMessageCell:adminLayout	Landroid/text/StaticLayout;
    //   915: iconst_0
    //   916: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   919: ldc_w 1629
    //   922: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   925: i2f
    //   926: fadd
    //   927: fadd
    //   928: f2i
    //   929: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   932: aload_0
    //   933: getfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   936: invokevirtual 1054	java/lang/String:length	()I
    //   939: ifne +8 -> 947
    //   942: aload_0
    //   943: aconst_null
    //   944: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   947: aload_0
    //   948: aconst_null
    //   949: putfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   952: aload_0
    //   953: aconst_null
    //   954: putfield 2436	org/telegram/ui/Cells/ChatMessageCell:currentForwardNameString	Ljava/lang/String;
    //   957: aload_0
    //   958: aconst_null
    //   959: putfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   962: aload_0
    //   963: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   966: iconst_0
    //   967: aconst_null
    //   968: aastore
    //   969: aload_0
    //   970: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   973: iconst_1
    //   974: aconst_null
    //   975: aastore
    //   976: aload_0
    //   977: iconst_0
    //   978: putfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   981: aload_0
    //   982: getfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   985: ifeq +622 -> 1607
    //   988: aload_1
    //   989: invokevirtual 2626	org/telegram/messenger/MessageObject:needDrawForwarded	()Z
    //   992: ifeq +615 -> 1607
    //   995: aload_0
    //   996: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   999: ifnull +13 -> 1012
    //   1002: aload_0
    //   1003: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   1006: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   1009: ifne +598 -> 1607
    //   1012: aload_1
    //   1013: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1016: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   1019: getfield 992	org/telegram/tgnet/TLRPC$MessageFwdHeader:channel_id	I
    //   1022: ifeq +30 -> 1052
    //   1025: aload_0
    //   1026: aload_0
    //   1027: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   1030: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   1033: aload_1
    //   1034: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1037: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   1040: getfield 992	org/telegram/tgnet/TLRPC$MessageFwdHeader:channel_id	I
    //   1043: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1046: invokevirtual 1044	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   1049: putfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1052: aload_1
    //   1053: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1056: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   1059: getfield 2627	org/telegram/tgnet/TLRPC$MessageFwdHeader:from_id	I
    //   1062: ifeq +30 -> 1092
    //   1065: aload_0
    //   1066: aload_0
    //   1067: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   1070: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   1073: aload_1
    //   1074: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1077: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   1080: getfield 2627	org/telegram/tgnet/TLRPC$MessageFwdHeader:from_id	I
    //   1083: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1086: invokevirtual 1019	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   1089: putfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   1092: aload_0
    //   1093: getfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   1096: ifnonnull +10 -> 1106
    //   1099: aload_0
    //   1100: getfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1103: ifnull +504 -> 1607
    //   1106: aload_0
    //   1107: getfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1110: ifnull +1479 -> 2589
    //   1113: aload_0
    //   1114: getfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   1117: ifnull +1458 -> 2575
    //   1120: aload_0
    //   1121: ldc_w 2629
    //   1124: iconst_2
    //   1125: anewarray 1242	java/lang/Object
    //   1128: dup
    //   1129: iconst_0
    //   1130: aload_0
    //   1131: getfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1134: getfield 2443	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   1137: aastore
    //   1138: dup
    //   1139: iconst_1
    //   1140: aload_0
    //   1141: getfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   1144: invokestatic 2427	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   1147: aastore
    //   1148: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   1151: putfield 2436	org/telegram/ui/Cells/ChatMessageCell:currentForwardNameString	Ljava/lang/String;
    //   1154: aload_0
    //   1155: aload_0
    //   1156: invokespecial 2565	org/telegram/ui/Cells/ChatMessageCell:getMaxNameWidth	()I
    //   1159: putfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1162: ldc_w 2631
    //   1165: ldc_w 2632
    //   1168: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1171: astore 8
    //   1173: ldc_w 2634
    //   1176: ldc_w 2635
    //   1179: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1182: astore 10
    //   1184: aload 10
    //   1186: ldc_w 2637
    //   1189: invokevirtual 2640	java/lang/String:indexOf	(Ljava/lang/String;)I
    //   1192: istore_2
    //   1193: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1196: new 1320	java/lang/StringBuilder
    //   1199: dup
    //   1200: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   1203: aload 8
    //   1205: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1208: ldc_w 1327
    //   1211: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1214: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1217: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   1220: f2d
    //   1221: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1224: d2i
    //   1225: istore_3
    //   1226: aload_0
    //   1227: getfield 2436	org/telegram/ui/Cells/ChatMessageCell:currentForwardNameString	Ljava/lang/String;
    //   1230: bipush 10
    //   1232: bipush 32
    //   1234: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   1237: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   1240: aload_0
    //   1241: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1244: iload_3
    //   1245: isub
    //   1246: aload_0
    //   1247: getfield 2559	org/telegram/ui/Cells/ChatMessageCell:viaWidth	I
    //   1250: isub
    //   1251: i2f
    //   1252: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1255: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1258: astore 9
    //   1260: aload 10
    //   1262: iconst_1
    //   1263: anewarray 1242	java/lang/Object
    //   1266: dup
    //   1267: iconst_0
    //   1268: aload 9
    //   1270: aastore
    //   1271: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   1274: astore 8
    //   1276: aload 7
    //   1278: ifnull +1346 -> 2624
    //   1281: new 2172	android/text/SpannableStringBuilder
    //   1284: dup
    //   1285: ldc_w 2589
    //   1288: iconst_2
    //   1289: anewarray 1242	java/lang/Object
    //   1292: dup
    //   1293: iconst_0
    //   1294: aload 8
    //   1296: aastore
    //   1297: dup
    //   1298: iconst_1
    //   1299: aload 6
    //   1301: aastore
    //   1302: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   1305: invokespecial 2175	android/text/SpannableStringBuilder:<init>	(Ljava/lang/CharSequence;)V
    //   1308: astore 7
    //   1310: aload_0
    //   1311: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1314: aload 8
    //   1316: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   1319: f2d
    //   1320: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1323: d2i
    //   1324: putfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   1327: aload 7
    //   1329: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   1332: dup
    //   1333: ldc_w 2606
    //   1336: invokestatic 2610	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   1339: invokespecial 2646	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;)V
    //   1342: aload 7
    //   1344: invokevirtual 2611	android/text/SpannableStringBuilder:length	()I
    //   1347: aload 6
    //   1349: invokevirtual 1054	java/lang/String:length	()I
    //   1352: isub
    //   1353: iconst_1
    //   1354: isub
    //   1355: aload 7
    //   1357: invokevirtual 2611	android/text/SpannableStringBuilder:length	()I
    //   1360: bipush 33
    //   1362: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   1365: aload 7
    //   1367: astore 6
    //   1369: iload_2
    //   1370: iflt +33 -> 1403
    //   1373: aload 6
    //   1375: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   1378: dup
    //   1379: ldc_w 2606
    //   1382: invokestatic 2610	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   1385: invokespecial 2646	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;)V
    //   1388: iload_2
    //   1389: aload 9
    //   1391: invokeinterface 2192 1 0
    //   1396: iload_2
    //   1397: iadd
    //   1398: bipush 33
    //   1400: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   1403: aload 6
    //   1405: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1408: aload_0
    //   1409: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1412: i2f
    //   1413: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1416: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1419: astore 6
    //   1421: aload_0
    //   1422: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1425: iconst_1
    //   1426: new 350	android/text/StaticLayout
    //   1429: dup
    //   1430: aload 6
    //   1432: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1435: aload_0
    //   1436: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1439: fconst_2
    //   1440: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1443: iadd
    //   1444: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1447: fconst_1
    //   1448: fconst_0
    //   1449: iconst_0
    //   1450: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1453: aastore
    //   1454: ldc_w 2648
    //   1457: ldc_w 2649
    //   1460: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1463: invokestatic 2557	org/telegram/messenger/AndroidUtilities:replaceTags	(Ljava/lang/String;)Landroid/text/SpannableStringBuilder;
    //   1466: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1469: aload_0
    //   1470: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1473: i2f
    //   1474: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1477: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1480: astore 6
    //   1482: aload_0
    //   1483: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1486: iconst_0
    //   1487: new 350	android/text/StaticLayout
    //   1490: dup
    //   1491: aload 6
    //   1493: getstatic 2643	org/telegram/ui/ActionBar/Theme:chat_forwardNamePaint	Landroid/text/TextPaint;
    //   1496: aload_0
    //   1497: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1500: fconst_2
    //   1501: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1504: iadd
    //   1505: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1508: fconst_1
    //   1509: fconst_0
    //   1510: iconst_0
    //   1511: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1514: aastore
    //   1515: aload_0
    //   1516: aload_0
    //   1517: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1520: iconst_0
    //   1521: aaload
    //   1522: iconst_0
    //   1523: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   1526: f2d
    //   1527: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1530: d2i
    //   1531: aload_0
    //   1532: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1535: iconst_1
    //   1536: aaload
    //   1537: iconst_0
    //   1538: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   1541: f2d
    //   1542: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1545: d2i
    //   1546: invokestatic 486	java/lang/Math:max	(II)I
    //   1549: putfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1552: aload_0
    //   1553: getfield 354	org/telegram/ui/Cells/ChatMessageCell:forwardNameOffsetX	[F
    //   1556: iconst_0
    //   1557: aload_0
    //   1558: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1561: iconst_0
    //   1562: aaload
    //   1563: iconst_0
    //   1564: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   1567: fastore
    //   1568: aload_0
    //   1569: getfield 354	org/telegram/ui/Cells/ChatMessageCell:forwardNameOffsetX	[F
    //   1572: iconst_1
    //   1573: aload_0
    //   1574: getfield 352	org/telegram/ui/Cells/ChatMessageCell:forwardedNameLayout	[Landroid/text/StaticLayout;
    //   1577: iconst_1
    //   1578: aaload
    //   1579: iconst_0
    //   1580: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   1583: fastore
    //   1584: aload_1
    //   1585: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1588: iconst_5
    //   1589: if_icmpeq +18 -> 1607
    //   1592: aload_0
    //   1593: aload_0
    //   1594: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1597: ldc_w 544
    //   1600: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1603: iadd
    //   1604: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1607: aload_1
    //   1608: invokevirtual 2652	org/telegram/messenger/MessageObject:hasValidReplyMessageObject	()Z
    //   1611: ifeq +540 -> 2151
    //   1614: aload_0
    //   1615: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   1618: ifnull +13 -> 1631
    //   1621: aload_0
    //   1622: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   1625: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   1628: ifne +523 -> 2151
    //   1631: aload_1
    //   1632: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1635: bipush 13
    //   1637: if_icmpeq +48 -> 1685
    //   1640: aload_1
    //   1641: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1644: iconst_5
    //   1645: if_icmpeq +40 -> 1685
    //   1648: aload_0
    //   1649: aload_0
    //   1650: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1653: ldc_w 2259
    //   1656: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1659: iadd
    //   1660: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1663: aload_1
    //   1664: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1667: ifeq +18 -> 1685
    //   1670: aload_0
    //   1671: aload_0
    //   1672: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1675: ldc_w 1775
    //   1678: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1681: iadd
    //   1682: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1685: aload_0
    //   1686: invokespecial 2565	org/telegram/ui/Cells/ChatMessageCell:getMaxNameWidth	()I
    //   1689: istore_3
    //   1690: iload_3
    //   1691: istore_2
    //   1692: aload_1
    //   1693: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1696: bipush 13
    //   1698: if_icmpeq +22 -> 1720
    //   1701: iload_3
    //   1702: istore_2
    //   1703: aload_1
    //   1704: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1707: iconst_5
    //   1708: if_icmpeq +12 -> 1720
    //   1711: iload_3
    //   1712: ldc_w 587
    //   1715: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1718: isub
    //   1719: istore_2
    //   1720: aconst_null
    //   1721: astore 8
    //   1723: aload_1
    //   1724: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1727: getfield 2655	org/telegram/messenger/MessageObject:photoThumbs2	Ljava/util/ArrayList;
    //   1730: bipush 80
    //   1732: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   1735: astore 7
    //   1737: aload 7
    //   1739: astore 6
    //   1741: aload 7
    //   1743: ifnonnull +17 -> 1760
    //   1746: aload_1
    //   1747: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1750: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   1753: bipush 80
    //   1755: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   1758: astore 6
    //   1760: aload 6
    //   1762: ifnull +40 -> 1802
    //   1765: aload_1
    //   1766: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1769: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1772: bipush 13
    //   1774: if_icmpeq +28 -> 1802
    //   1777: aload_1
    //   1778: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   1781: bipush 13
    //   1783: if_icmpne +9 -> 1792
    //   1786: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   1789: ifeq +13 -> 1802
    //   1792: aload_1
    //   1793: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1796: invokevirtual 1419	org/telegram/messenger/MessageObject:isSecretMedia	()Z
    //   1799: ifeq +861 -> 2660
    //   1802: aload_0
    //   1803: getfield 387	org/telegram/ui/Cells/ChatMessageCell:replyImageReceiver	Lorg/telegram/messenger/ImageReceiver;
    //   1806: aconst_null
    //   1807: checkcast 794	android/graphics/drawable/Drawable
    //   1810: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   1813: aload_0
    //   1814: iconst_0
    //   1815: putfield 2657	org/telegram/ui/Cells/ChatMessageCell:needReplyImage	Z
    //   1818: aconst_null
    //   1819: astore 6
    //   1821: aload_1
    //   1822: getfield 2660	org/telegram/messenger/MessageObject:customReplyName	Ljava/lang/String;
    //   1825: ifnull +913 -> 2738
    //   1828: aload_1
    //   1829: getfield 2660	org/telegram/messenger/MessageObject:customReplyName	Ljava/lang/String;
    //   1832: astore 6
    //   1834: aload 6
    //   1836: astore 7
    //   1838: aload 6
    //   1840: ifnonnull +14 -> 1854
    //   1843: ldc_w 2662
    //   1846: ldc_w 2663
    //   1849: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1852: astore 7
    //   1854: aload 7
    //   1856: bipush 10
    //   1858: bipush 32
    //   1860: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   1863: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   1866: iload_2
    //   1867: i2f
    //   1868: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1871: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1874: astore 7
    //   1876: aload_1
    //   1877: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1880: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1883: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1886: instanceof 1029
    //   1889: ifeq +996 -> 2885
    //   1892: aload_1
    //   1893: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1896: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1899: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1902: getfield 2667	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   1905: getfield 2670	org/telegram/tgnet/TLRPC$TL_game:title	Ljava/lang/String;
    //   1908: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   1911: invokevirtual 2674	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   1914: ldc_w 478
    //   1917: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1920: iconst_0
    //   1921: invokestatic 2680	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   1924: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   1927: iload_2
    //   1928: i2f
    //   1929: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1932: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1935: astore 6
    //   1937: aload_0
    //   1938: getfield 2657	org/telegram/ui/Cells/ChatMessageCell:needReplyImage	Z
    //   1941: ifeq +1116 -> 3057
    //   1944: bipush 44
    //   1946: istore_3
    //   1947: aload_0
    //   1948: iload_3
    //   1949: iconst_4
    //   1950: iadd
    //   1951: i2f
    //   1952: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1955: putfield 2682	org/telegram/ui/Cells/ChatMessageCell:replyNameWidth	I
    //   1958: aload 7
    //   1960: ifnull +84 -> 2044
    //   1963: aload_0
    //   1964: new 350	android/text/StaticLayout
    //   1967: dup
    //   1968: aload 7
    //   1970: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   1973: iload_2
    //   1974: ldc_w 1588
    //   1977: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1980: iadd
    //   1981: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1984: fconst_1
    //   1985: fconst_0
    //   1986: iconst_0
    //   1987: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1990: putfield 1527	org/telegram/ui/Cells/ChatMessageCell:replyNameLayout	Landroid/text/StaticLayout;
    //   1993: aload_0
    //   1994: getfield 1527	org/telegram/ui/Cells/ChatMessageCell:replyNameLayout	Landroid/text/StaticLayout;
    //   1997: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   2000: ifle +44 -> 2044
    //   2003: aload_0
    //   2004: aload_0
    //   2005: getfield 2682	org/telegram/ui/Cells/ChatMessageCell:replyNameWidth	I
    //   2008: aload_0
    //   2009: getfield 1527	org/telegram/ui/Cells/ChatMessageCell:replyNameLayout	Landroid/text/StaticLayout;
    //   2012: iconst_0
    //   2013: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   2016: f2d
    //   2017: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   2020: d2i
    //   2021: ldc_w 1629
    //   2024: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2027: iadd
    //   2028: iadd
    //   2029: putfield 2682	org/telegram/ui/Cells/ChatMessageCell:replyNameWidth	I
    //   2032: aload_0
    //   2033: aload_0
    //   2034: getfield 1527	org/telegram/ui/Cells/ChatMessageCell:replyNameLayout	Landroid/text/StaticLayout;
    //   2037: iconst_0
    //   2038: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   2041: putfield 2684	org/telegram/ui/Cells/ChatMessageCell:replyNameOffset	F
    //   2044: aload_0
    //   2045: getfield 2657	org/telegram/ui/Cells/ChatMessageCell:needReplyImage	Z
    //   2048: ifeq +1022 -> 3070
    //   2051: bipush 44
    //   2053: istore_3
    //   2054: aload_0
    //   2055: iload_3
    //   2056: iconst_4
    //   2057: iadd
    //   2058: i2f
    //   2059: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2062: putfield 2686	org/telegram/ui/Cells/ChatMessageCell:replyTextWidth	I
    //   2065: aload 6
    //   2067: ifnull +84 -> 2151
    //   2070: aload_0
    //   2071: new 350	android/text/StaticLayout
    //   2074: dup
    //   2075: aload 6
    //   2077: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   2080: iload_2
    //   2081: ldc_w 1588
    //   2084: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2087: iadd
    //   2088: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   2091: fconst_1
    //   2092: fconst_0
    //   2093: iconst_0
    //   2094: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   2097: putfield 2404	org/telegram/ui/Cells/ChatMessageCell:replyTextLayout	Landroid/text/StaticLayout;
    //   2100: aload_0
    //   2101: getfield 2404	org/telegram/ui/Cells/ChatMessageCell:replyTextLayout	Landroid/text/StaticLayout;
    //   2104: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   2107: ifle +44 -> 2151
    //   2110: aload_0
    //   2111: aload_0
    //   2112: getfield 2686	org/telegram/ui/Cells/ChatMessageCell:replyTextWidth	I
    //   2115: aload_0
    //   2116: getfield 2404	org/telegram/ui/Cells/ChatMessageCell:replyTextLayout	Landroid/text/StaticLayout;
    //   2119: iconst_0
    //   2120: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   2123: f2d
    //   2124: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   2127: d2i
    //   2128: ldc_w 1629
    //   2131: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2134: iadd
    //   2135: iadd
    //   2136: putfield 2686	org/telegram/ui/Cells/ChatMessageCell:replyTextWidth	I
    //   2139: aload_0
    //   2140: aload_0
    //   2141: getfield 2404	org/telegram/ui/Cells/ChatMessageCell:replyTextLayout	Landroid/text/StaticLayout;
    //   2144: iconst_0
    //   2145: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   2148: putfield 2688	org/telegram/ui/Cells/ChatMessageCell:replyTextOffset	F
    //   2151: aload_0
    //   2152: invokevirtual 2691	org/telegram/ui/Cells/ChatMessageCell:requestLayout	()V
    //   2155: return
    //   2156: aload_0
    //   2157: aconst_null
    //   2158: putfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2161: goto -2070 -> 91
    //   2164: aload_0
    //   2165: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2168: ifnull +49 -> 2217
    //   2171: aload_0
    //   2172: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2175: getfield 2439	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   2178: ifnull +31 -> 2209
    //   2181: aload_0
    //   2182: aload_0
    //   2183: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2186: getfield 2439	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   2189: getfield 2442	org/telegram/tgnet/TLRPC$ChatPhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2192: putfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2195: aload_0
    //   2196: getfield 382	org/telegram/ui/Cells/ChatMessageCell:avatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   2199: aload_0
    //   2200: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2203: invokevirtual 2694	org/telegram/ui/Components/AvatarDrawable:setInfo	(Lorg/telegram/tgnet/TLRPC$Chat;)V
    //   2206: goto -2104 -> 102
    //   2209: aload_0
    //   2210: aconst_null
    //   2211: putfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2214: goto -19 -> 2195
    //   2217: aload_0
    //   2218: aconst_null
    //   2219: putfield 2409	org/telegram/ui/Cells/ChatMessageCell:currentPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2222: aload_0
    //   2223: getfield 382	org/telegram/ui/Cells/ChatMessageCell:avatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   2226: aload_1
    //   2227: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2230: getfield 1009	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   2233: aconst_null
    //   2234: aconst_null
    //   2235: iconst_0
    //   2236: invokevirtual 2697	org/telegram/ui/Components/AvatarDrawable:setInfo	(ILjava/lang/String;Ljava/lang/String;Z)V
    //   2239: goto -2137 -> 102
    //   2242: aload 9
    //   2244: astore 7
    //   2246: aload 8
    //   2248: astore 6
    //   2250: aload_1
    //   2251: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2254: getfield 2459	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   2257: ifnull -1962 -> 295
    //   2260: aload 9
    //   2262: astore 7
    //   2264: aload 8
    //   2266: astore 6
    //   2268: aload_1
    //   2269: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2272: getfield 2459	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   2275: invokevirtual 1054	java/lang/String:length	()I
    //   2278: ifle -1983 -> 295
    //   2281: new 1320	java/lang/StringBuilder
    //   2284: dup
    //   2285: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   2288: ldc_w 2551
    //   2291: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2294: aload_1
    //   2295: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2298: getfield 2459	org/telegram/tgnet/TLRPC$Message:via_bot_name	Ljava/lang/String;
    //   2301: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   2304: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   2307: astore 6
    //   2309: ldc_w 2553
    //   2312: iconst_1
    //   2313: anewarray 1242	java/lang/Object
    //   2316: dup
    //   2317: iconst_0
    //   2318: aload 6
    //   2320: aastore
    //   2321: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   2324: invokestatic 2557	org/telegram/messenger/AndroidUtilities:replaceTags	(Ljava/lang/String;)Landroid/text/SpannableStringBuilder;
    //   2327: astore 7
    //   2329: aload_0
    //   2330: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   2333: aload 7
    //   2335: iconst_0
    //   2336: aload 7
    //   2338: invokeinterface 2192 1 0
    //   2343: invokevirtual 2500	android/text/TextPaint:measureText	(Ljava/lang/CharSequence;II)F
    //   2346: f2d
    //   2347: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   2350: d2i
    //   2351: putfield 2559	org/telegram/ui/Cells/ChatMessageCell:viaWidth	I
    //   2354: goto -2059 -> 295
    //   2357: iconst_0
    //   2358: istore 4
    //   2360: goto -2038 -> 322
    //   2363: iconst_0
    //   2364: istore_3
    //   2365: goto -2017 -> 348
    //   2368: aconst_null
    //   2369: astore 8
    //   2371: iconst_0
    //   2372: istore_2
    //   2373: goto -1892 -> 481
    //   2376: aload_0
    //   2377: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2380: ifnull +17 -> 2397
    //   2383: aload_0
    //   2384: aload_0
    //   2385: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2388: getfield 2443	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   2391: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   2394: goto -1890 -> 504
    //   2397: aload_0
    //   2398: ldc_w 2699
    //   2401: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   2404: goto -1900 -> 504
    //   2407: aload_0
    //   2408: ldc_w 2448
    //   2411: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   2414: goto -1910 -> 504
    //   2417: iconst_0
    //   2418: istore 4
    //   2420: goto -1882 -> 538
    //   2423: aload_0
    //   2424: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2427: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   2430: ifeq +17 -> 2447
    //   2433: ldc_w 2701
    //   2436: astore 9
    //   2438: aload 9
    //   2440: invokestatic 1511	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   2443: istore_3
    //   2444: goto -1803 -> 641
    //   2447: ldc_w 2703
    //   2450: astore 9
    //   2452: goto -14 -> 2438
    //   2455: new 2172	android/text/SpannableStringBuilder
    //   2458: dup
    //   2459: ldc_w 2705
    //   2462: iconst_1
    //   2463: anewarray 1242	java/lang/Object
    //   2466: dup
    //   2467: iconst_0
    //   2468: aload 6
    //   2470: aastore
    //   2471: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   2474: invokespecial 2175	android/text/SpannableStringBuilder:<init>	(Ljava/lang/CharSequence;)V
    //   2477: astore 9
    //   2479: aload 9
    //   2481: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   2484: dup
    //   2485: getstatic 2597	android/graphics/Typeface:DEFAULT	Landroid/graphics/Typeface;
    //   2488: iconst_0
    //   2489: iload_3
    //   2490: invokespecial 2600	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;II)V
    //   2493: iconst_0
    //   2494: iconst_4
    //   2495: bipush 33
    //   2497: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   2500: aload 9
    //   2502: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   2505: dup
    //   2506: ldc_w 2606
    //   2509: invokestatic 2610	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   2512: iconst_0
    //   2513: iload_3
    //   2514: invokespecial 2600	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;II)V
    //   2517: iconst_4
    //   2518: aload 9
    //   2520: invokevirtual 2611	android/text/SpannableStringBuilder:length	()I
    //   2523: bipush 33
    //   2525: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   2528: goto -1775 -> 753
    //   2531: aload_0
    //   2532: iconst_0
    //   2533: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   2536: goto -1664 -> 872
    //   2539: astore 8
    //   2541: aload 8
    //   2543: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   2546: goto -1614 -> 932
    //   2549: aload_0
    //   2550: aconst_null
    //   2551: putfield 2617	org/telegram/ui/Cells/ChatMessageCell:adminLayout	Landroid/text/StaticLayout;
    //   2554: goto -1622 -> 932
    //   2557: aload_0
    //   2558: aconst_null
    //   2559: putfield 2429	org/telegram/ui/Cells/ChatMessageCell:currentNameString	Ljava/lang/String;
    //   2562: aload_0
    //   2563: aconst_null
    //   2564: putfield 2613	org/telegram/ui/Cells/ChatMessageCell:nameLayout	Landroid/text/StaticLayout;
    //   2567: aload_0
    //   2568: iconst_0
    //   2569: putfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   2572: goto -1625 -> 947
    //   2575: aload_0
    //   2576: aload_0
    //   2577: getfield 2621	org/telegram/ui/Cells/ChatMessageCell:currentForwardChannel	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2580: getfield 2443	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   2583: putfield 2436	org/telegram/ui/Cells/ChatMessageCell:currentForwardNameString	Ljava/lang/String;
    //   2586: goto -1432 -> 1154
    //   2589: aload_0
    //   2590: getfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   2593: ifnull -1439 -> 1154
    //   2596: aload_0
    //   2597: aload_0
    //   2598: getfield 2619	org/telegram/ui/Cells/ChatMessageCell:currentForwardUser	Lorg/telegram/tgnet/TLRPC$User;
    //   2601: invokestatic 2427	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   2604: putfield 2436	org/telegram/ui/Cells/ChatMessageCell:currentForwardNameString	Ljava/lang/String;
    //   2607: goto -1453 -> 1154
    //   2610: astore 8
    //   2612: aload 9
    //   2614: invokeinterface 2706 1 0
    //   2619: astore 8
    //   2621: goto -1345 -> 1276
    //   2624: new 2172	android/text/SpannableStringBuilder
    //   2627: dup
    //   2628: aload 10
    //   2630: iconst_1
    //   2631: anewarray 1242	java/lang/Object
    //   2634: dup
    //   2635: iconst_0
    //   2636: aload 9
    //   2638: aastore
    //   2639: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   2642: invokespecial 2175	android/text/SpannableStringBuilder:<init>	(Ljava/lang/CharSequence;)V
    //   2645: astore 6
    //   2647: goto -1278 -> 1369
    //   2650: astore 6
    //   2652: aload 6
    //   2654: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   2657: goto -1050 -> 1607
    //   2660: aload_1
    //   2661: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2664: invokevirtual 1416	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   2667: ifeq +60 -> 2727
    //   2670: aload_0
    //   2671: getfield 387	org/telegram/ui/Cells/ChatMessageCell:replyImageReceiver	Lorg/telegram/messenger/ImageReceiver;
    //   2674: ldc_w 1883
    //   2677: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2680: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   2683: aload_0
    //   2684: aload 6
    //   2686: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2689: putfield 2419	org/telegram/ui/Cells/ChatMessageCell:currentReplyPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2692: aload_0
    //   2693: getfield 387	org/telegram/ui/Cells/ChatMessageCell:replyImageReceiver	Lorg/telegram/messenger/ImageReceiver;
    //   2696: aload 6
    //   2698: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   2701: ldc_w 2545
    //   2704: aconst_null
    //   2705: aconst_null
    //   2706: iconst_1
    //   2707: invokevirtual 2548	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Ljava/lang/String;I)V
    //   2710: aload_0
    //   2711: iconst_1
    //   2712: putfield 2657	org/telegram/ui/Cells/ChatMessageCell:needReplyImage	Z
    //   2715: iload_2
    //   2716: ldc_w 1819
    //   2719: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2722: isub
    //   2723: istore_2
    //   2724: goto -906 -> 1818
    //   2727: aload_0
    //   2728: getfield 387	org/telegram/ui/Cells/ChatMessageCell:replyImageReceiver	Lorg/telegram/messenger/ImageReceiver;
    //   2731: iconst_0
    //   2732: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   2735: goto -52 -> 2683
    //   2738: aload_1
    //   2739: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2742: invokevirtual 995	org/telegram/messenger/MessageObject:isFromUser	()Z
    //   2745: ifeq +43 -> 2788
    //   2748: aload_0
    //   2749: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   2752: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   2755: aload_1
    //   2756: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2759: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2762: getfield 1009	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   2765: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   2768: invokevirtual 1019	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   2771: astore 7
    //   2773: aload 7
    //   2775: ifnull -941 -> 1834
    //   2778: aload 7
    //   2780: invokestatic 2427	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   2783: astore 6
    //   2785: goto -951 -> 1834
    //   2788: aload_1
    //   2789: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2792: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2795: getfield 1009	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   2798: ifge +44 -> 2842
    //   2801: aload_0
    //   2802: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   2805: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   2808: aload_1
    //   2809: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2812: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2815: getfield 1009	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   2818: ineg
    //   2819: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   2822: invokevirtual 1044	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   2825: astore 7
    //   2827: aload 7
    //   2829: ifnull -995 -> 1834
    //   2832: aload 7
    //   2834: getfield 2443	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   2837: astore 6
    //   2839: goto -1005 -> 1834
    //   2842: aload_0
    //   2843: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   2846: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   2849: aload_1
    //   2850: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2853: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2856: getfield 1037	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   2859: getfield 1040	org/telegram/tgnet/TLRPC$Peer:channel_id	I
    //   2862: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   2865: invokevirtual 1044	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   2868: astore 7
    //   2870: aload 7
    //   2872: ifnull -1038 -> 1834
    //   2875: aload 7
    //   2877: getfield 2443	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   2880: astore 6
    //   2882: goto -1048 -> 1834
    //   2885: aload_1
    //   2886: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2889: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2892: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2895: instanceof 1031
    //   2898: ifeq +48 -> 2946
    //   2901: aload_1
    //   2902: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2905: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2908: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   2911: getfield 2358	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   2914: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   2917: invokevirtual 2674	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   2920: ldc_w 478
    //   2923: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2926: iconst_0
    //   2927: invokestatic 2680	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   2930: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   2933: iload_2
    //   2934: i2f
    //   2935: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   2938: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   2941: astore 6
    //   2943: goto -1006 -> 1937
    //   2946: aload 8
    //   2948: astore 6
    //   2950: aload_1
    //   2951: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2954: getfield 1114	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   2957: ifnull -1020 -> 1937
    //   2960: aload 8
    //   2962: astore 6
    //   2964: aload_1
    //   2965: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2968: getfield 1114	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   2971: invokeinterface 2192 1 0
    //   2976: ifle -1039 -> 1937
    //   2979: aload_1
    //   2980: getfield 2407	org/telegram/messenger/MessageObject:replyMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2983: getfield 1114	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   2986: invokeinterface 2706 1 0
    //   2991: astore 6
    //   2993: aload 6
    //   2995: astore_1
    //   2996: aload 6
    //   2998: invokevirtual 1054	java/lang/String:length	()I
    //   3001: sipush 150
    //   3004: if_icmple +13 -> 3017
    //   3007: aload 6
    //   3009: iconst_0
    //   3010: sipush 150
    //   3013: invokevirtual 2710	java/lang/String:substring	(II)Ljava/lang/String;
    //   3016: astore_1
    //   3017: aload_1
    //   3018: bipush 10
    //   3020: bipush 32
    //   3022: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   3025: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   3028: invokevirtual 2674	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   3031: ldc_w 478
    //   3034: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3037: iconst_0
    //   3038: invokestatic 2680	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   3041: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   3044: iload_2
    //   3045: i2f
    //   3046: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   3049: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   3052: astore 6
    //   3054: goto -1117 -> 1937
    //   3057: iconst_0
    //   3058: istore_3
    //   3059: goto -1112 -> 1947
    //   3062: astore_1
    //   3063: aload_1
    //   3064: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   3067: goto -1023 -> 2044
    //   3070: iconst_0
    //   3071: istore_3
    //   3072: goto -1018 -> 2054
    //   3075: astore_1
    //   3076: aload_1
    //   3077: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   3080: goto -929 -> 2151
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	3083	0	this	ChatMessageCell
    //   0	3083	1	paramMessageObject	MessageObject
    //   470	2575	2	i	int
    //   347	2725	3	j	int
    //   320	2099	4	k	int
    //   526	21	5	m	int
    //   176	2470	6	localObject1	Object
    //   2650	47	6	localException1	Exception
    //   2783	270	6	localObject2	Object
    //   172	2704	7	localObject3	Object
    //   133	2237	8	str1	String
    //   2539	3	8	localException2	Exception
    //   2610	1	8	localException3	Exception
    //   2619	342	8	str2	String
    //   136	2501	9	localObject4	Object
    //   168	2461	10	localObject5	Object
    // Exception table:
    //   from	to	target	type
    //   771	860	2539	java/lang/Exception
    //   860	872	2539	java/lang/Exception
    //   877	932	2539	java/lang/Exception
    //   2531	2536	2539	java/lang/Exception
    //   2549	2554	2539	java/lang/Exception
    //   1260	1276	2610	java/lang/Exception
    //   1421	1607	2650	java/lang/Exception
    //   1937	1944	3062	java/lang/Exception
    //   1947	1958	3062	java/lang/Exception
    //   1963	2044	3062	java/lang/Exception
    //   2044	2051	3075	java/lang/Exception
    //   2054	2065	3075	java/lang/Exception
    //   2070	2151	3075	java/lang/Exception
  }
  
  private void updateCurrentUserAndChat()
  {
    MessagesController localMessagesController = MessagesController.getInstance(this.currentAccount);
    TLRPC.MessageFwdHeader localMessageFwdHeader = this.currentMessageObject.messageOwner.fwd_from;
    int i = UserConfig.getInstance(this.currentAccount).getClientUserId();
    if ((localMessageFwdHeader != null) && (localMessageFwdHeader.channel_id != 0) && (this.currentMessageObject.getDialogId() == i)) {
      this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(localMessageFwdHeader.channel_id));
    }
    do
    {
      do
      {
        return;
        if ((localMessageFwdHeader == null) || (localMessageFwdHeader.saved_from_peer == null)) {
          break;
        }
        if (localMessageFwdHeader.saved_from_peer.user_id != 0)
        {
          if (localMessageFwdHeader.from_id != 0)
          {
            this.currentUser = localMessagesController.getUser(Integer.valueOf(localMessageFwdHeader.from_id));
            return;
          }
          this.currentUser = localMessagesController.getUser(Integer.valueOf(localMessageFwdHeader.saved_from_peer.user_id));
          return;
        }
        if (localMessageFwdHeader.saved_from_peer.channel_id != 0)
        {
          if ((this.currentMessageObject.isSavedFromMegagroup()) && (localMessageFwdHeader.from_id != 0))
          {
            this.currentUser = localMessagesController.getUser(Integer.valueOf(localMessageFwdHeader.from_id));
            return;
          }
          this.currentChat = localMessagesController.getChat(Integer.valueOf(localMessageFwdHeader.saved_from_peer.channel_id));
          return;
        }
      } while (localMessageFwdHeader.saved_from_peer.chat_id == 0);
      if (localMessageFwdHeader.from_id != 0)
      {
        this.currentUser = localMessagesController.getUser(Integer.valueOf(localMessageFwdHeader.from_id));
        return;
      }
      this.currentChat = localMessagesController.getChat(Integer.valueOf(localMessageFwdHeader.saved_from_peer.chat_id));
      return;
      if ((localMessageFwdHeader != null) && (localMessageFwdHeader.from_id != 0) && (localMessageFwdHeader.channel_id == 0) && (this.currentMessageObject.getDialogId() == i))
      {
        this.currentUser = localMessagesController.getUser(Integer.valueOf(localMessageFwdHeader.from_id));
        return;
      }
      if (this.currentMessageObject.isFromUser())
      {
        this.currentUser = localMessagesController.getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
        return;
      }
      if (this.currentMessageObject.messageOwner.from_id < 0)
      {
        this.currentChat = localMessagesController.getChat(Integer.valueOf(-this.currentMessageObject.messageOwner.from_id));
        return;
      }
    } while (!this.currentMessageObject.messageOwner.post);
    this.currentChat = localMessagesController.getChat(Integer.valueOf(this.currentMessageObject.messageOwner.to_id.channel_id));
  }
  
  private void updateRadialProgressBackground()
  {
    if (this.drawRadialCheckBackground) {}
    do
    {
      return;
      this.radialProgress.swapBackground(getDrawableForCurrentState());
    } while (this.hasMiniProgress == 0);
    this.radialProgress.swapMiniBackground(getMiniDrawableForCurrentState());
  }
  
  private void updateSecretTimeText(MessageObject paramMessageObject)
  {
    if ((paramMessageObject == null) || (!paramMessageObject.needDrawBluredPreview())) {}
    do
    {
      return;
      paramMessageObject = paramMessageObject.getSecretTimeString();
    } while (paramMessageObject == null);
    this.infoWidth = ((int)Math.ceil(Theme.chat_infoPaint.measureText(paramMessageObject)));
    this.infoLayout = new StaticLayout(TextUtils.ellipsize(paramMessageObject, Theme.chat_infoPaint, this.infoWidth, TextUtils.TruncateAt.END), Theme.chat_infoPaint, this.infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
    invalidate();
  }
  
  private void updateWaveform()
  {
    if ((this.currentMessageObject == null) || (this.documentAttachType != 3)) {}
    for (;;)
    {
      return;
      int i = 0;
      while (i < this.documentAttach.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)this.documentAttach.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio))
        {
          if ((localDocumentAttribute.waveform == null) || (localDocumentAttribute.waveform.length == 0)) {
            MediaController.getInstance().generateWaveform(this.currentMessageObject);
          }
          if (localDocumentAttribute.waveform != null) {}
          for (boolean bool = true;; bool = false)
          {
            this.useSeekBarWaweform = bool;
            this.seekBarWaveform.setWaveform(localDocumentAttribute.waveform);
            return;
          }
        }
        i += 1;
      }
    }
  }
  
  public void checkRoundVideoPlayback(boolean paramBoolean)
  {
    boolean bool = paramBoolean;
    if (paramBoolean) {
      if (MediaController.getInstance().getPlayingMessageObject() != null) {
        break label37;
      }
    }
    label37:
    for (bool = true;; bool = false)
    {
      this.photoImage.setAllowStartAnimation(bool);
      if (!bool) {
        break;
      }
      this.photoImage.startAnimation();
      return;
    }
    this.photoImage.stopAnimation();
  }
  
  public void didSetImage(ImageReceiver paramImageReceiver, boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((this.currentMessageObject != null) && (paramBoolean1) && (!paramBoolean2) && (!this.currentMessageObject.mediaExists) && (!this.currentMessageObject.attachPathExists))
    {
      this.currentMessageObject.mediaExists = true;
      updateButtonState(true);
    }
  }
  
  public void downloadAudioIfNeed()
  {
    if (this.documentAttachType != 3) {}
    while (this.buttonState != 2) {
      return;
    }
    FileLoader.getInstance(this.currentAccount).loadFile(this.documentAttach, true, 0);
    this.buttonState = 4;
    this.radialProgress.setBackground(getDrawableForCurrentState(), false, false);
  }
  
  public void drawCaptionLayout(Canvas paramCanvas, boolean paramBoolean)
  {
    if ((this.captionLayout == null) || ((paramBoolean) && (this.pressedLink == null))) {
      return;
    }
    paramCanvas.save();
    paramCanvas.translate(this.captionX, this.captionY);
    if (this.pressedLink != null)
    {
      int i = 0;
      while (i < this.urlPath.size())
      {
        paramCanvas.drawPath((Path)this.urlPath.get(i), Theme.chat_urlPaint);
        i += 1;
      }
    }
    if (!paramBoolean) {}
    try
    {
      this.captionLayout.draw(paramCanvas);
      paramCanvas.restore();
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
  
  public void drawNamesLayout(Canvas paramCanvas)
  {
    float f2 = 11.0F;
    int k = 0;
    TextPaint localTextPaint;
    String str;
    if ((this.drawNameLayout) && (this.nameLayout != null))
    {
      paramCanvas.save();
      if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
        break label575;
      }
      Theme.chat_namePaint.setColor(Theme.getColor("chat_stickerNameText"));
      if (!this.currentMessageObject.isOutOwner()) {
        break label551;
      }
      this.nameX = AndroidUtilities.dp(28.0F);
      this.nameY = (this.layoutHeight - AndroidUtilities.dp(38.0F));
      Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
      Theme.chat_systemDrawable.setBounds((int)this.nameX - AndroidUtilities.dp(12.0F), (int)this.nameY - AndroidUtilities.dp(5.0F), (int)this.nameX + AndroidUtilities.dp(12.0F) + this.nameWidth, (int)this.nameY + AndroidUtilities.dp(22.0F));
      Theme.chat_systemDrawable.draw(paramCanvas);
      paramCanvas.translate(this.nameX, this.nameY);
      this.nameLayout.draw(paramCanvas);
      paramCanvas.restore();
      if (this.adminLayout != null)
      {
        localTextPaint = Theme.chat_adminPaint;
        if (!isDrawSelectedBackground()) {
          break label789;
        }
        str = "chat_adminSelectedText";
        label221:
        localTextPaint.setColor(Theme.getColor(str));
        paramCanvas.save();
        paramCanvas.translate(this.backgroundDrawableLeft + this.backgroundDrawableRight - AndroidUtilities.dp(11.0F) - this.adminLayout.getLineWidth(0), this.nameY + AndroidUtilities.dp(0.5F));
        this.adminLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
    }
    label385:
    int j;
    label551:
    label575:
    label636:
    float f1;
    if ((this.drawForwardedName) && (this.forwardedNameLayout[0] != null) && (this.forwardedNameLayout[1] != null) && ((this.currentPosition == null) || ((this.currentPosition.minY == 0) && (this.currentPosition.minX == 0))))
    {
      if (this.currentMessageObject.type == 5)
      {
        Theme.chat_forwardNamePaint.setColor(Theme.getColor("chat_stickerReplyNameText"));
        if (this.currentMessageObject.isOutOwner())
        {
          this.forwardNameX = AndroidUtilities.dp(23.0F);
          this.forwardNameY = AndroidUtilities.dp(12.0F);
          i = this.forwardedNameWidth;
          j = AndroidUtilities.dp(14.0F);
          Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
          Theme.chat_systemDrawable.setBounds(this.forwardNameX - AndroidUtilities.dp(7.0F), this.forwardNameY - AndroidUtilities.dp(6.0F), this.forwardNameX - AndroidUtilities.dp(7.0F) + (i + j), this.forwardNameY + AndroidUtilities.dp(38.0F));
          Theme.chat_systemDrawable.draw(paramCanvas);
        }
      }
      for (;;)
      {
        i = 0;
        while (i < 2)
        {
          paramCanvas.save();
          paramCanvas.translate(this.forwardNameX - this.forwardNameOffsetX[i], this.forwardNameY + AndroidUtilities.dp(16.0F) * i);
          this.forwardedNameLayout[i].draw(paramCanvas);
          paramCanvas.restore();
          i += 1;
        }
        this.nameX = (this.backgroundDrawableLeft + this.backgroundDrawableRight + AndroidUtilities.dp(22.0F));
        break;
        if ((this.mediaBackground) || (this.currentMessageObject.isOutOwner()))
        {
          this.nameX = (this.backgroundDrawableLeft + AndroidUtilities.dp(11.0F) - this.nameOffsetX);
          if (this.currentUser == null) {
            break label710;
          }
          Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentUser.id));
          if (!this.drawPinnedTop) {
            break label782;
          }
        }
        label710:
        label782:
        for (f1 = 9.0F;; f1 = 10.0F)
        {
          this.nameY = AndroidUtilities.dp(f1);
          break;
          i = this.backgroundDrawableLeft;
          if ((!this.mediaBackground) && (this.drawPinnedBottom)) {}
          for (f1 = 11.0F;; f1 = 17.0F)
          {
            this.nameX = (AndroidUtilities.dp(f1) + i - this.nameOffsetX);
            break;
          }
          if (this.currentChat != null)
          {
            if ((ChatObject.isChannel(this.currentChat)) && (!this.currentChat.megagroup))
            {
              Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(5));
              break label636;
            }
            Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(this.currentChat.id));
            break label636;
          }
          Theme.chat_namePaint.setColor(AvatarDrawable.getNameColorForId(0));
          break label636;
        }
        label789:
        str = "chat_adminText";
        break label221;
        this.forwardNameX = (this.backgroundDrawableLeft + this.backgroundDrawableRight + AndroidUtilities.dp(17.0F));
        break label385;
        if (this.drawNameLayout) {}
        for (i = 19;; i = 0)
        {
          this.forwardNameY = AndroidUtilities.dp(i + 10);
          if (!this.currentMessageObject.isOutOwner()) {
            break label890;
          }
          Theme.chat_forwardNamePaint.setColor(Theme.getColor("chat_outForwardedNameText"));
          this.forwardNameX = (this.backgroundDrawableLeft + AndroidUtilities.dp(11.0F));
          break;
        }
        label890:
        Theme.chat_forwardNamePaint.setColor(Theme.getColor("chat_inForwardedNameText"));
        if (!this.mediaBackground) {
          break label927;
        }
        this.forwardNameX = (this.backgroundDrawableLeft + AndroidUtilities.dp(11.0F));
      }
      label927:
      i = this.backgroundDrawableLeft;
      if ((!this.mediaBackground) && (this.drawPinnedBottom)) {}
      for (f1 = f2;; f1 = 17.0F)
      {
        this.forwardNameX = (i + AndroidUtilities.dp(f1));
        break;
      }
    }
    if (this.replyNameLayout != null)
    {
      if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
        break label1448;
      }
      Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_stickerReplyLine"));
      Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_stickerReplyNameText"));
      Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_stickerReplyMessageText"));
      if (!this.currentMessageObject.isOutOwner()) {
        break label1425;
      }
      this.replyStartX = AndroidUtilities.dp(23.0F);
      this.replyStartY = AndroidUtilities.dp(12.0F);
      i = Math.max(this.replyNameWidth, this.replyTextWidth);
      j = AndroidUtilities.dp(14.0F);
      Theme.chat_systemDrawable.setColorFilter(Theme.colorFilter);
      Theme.chat_systemDrawable.setBounds(this.replyStartX - AndroidUtilities.dp(7.0F), this.replyStartY - AndroidUtilities.dp(6.0F), this.replyStartX - AndroidUtilities.dp(7.0F) + (i + j), this.replyStartY + AndroidUtilities.dp(41.0F));
      Theme.chat_systemDrawable.draw(paramCanvas);
      if ((this.currentPosition == null) || ((this.currentPosition.minY == 0) && (this.currentPosition.minX == 0)))
      {
        paramCanvas.drawRect(this.replyStartX, this.replyStartY, this.replyStartX + AndroidUtilities.dp(2.0F), this.replyStartY + AndroidUtilities.dp(35.0F), Theme.chat_replyLinePaint);
        if (this.needReplyImage)
        {
          this.replyImageReceiver.setImageCoords(this.replyStartX + AndroidUtilities.dp(10.0F), this.replyStartY, AndroidUtilities.dp(35.0F), AndroidUtilities.dp(35.0F));
          this.replyImageReceiver.draw(paramCanvas);
        }
        if (this.replyNameLayout != null)
        {
          paramCanvas.save();
          f1 = this.replyStartX;
          f2 = this.replyNameOffset;
          if (!this.needReplyImage) {
            break label1882;
          }
        }
      }
    }
    label1425:
    label1448:
    label1570:
    label1590:
    label1825:
    label1870:
    label1876:
    label1882:
    for (int i = 44;; i = 0)
    {
      paramCanvas.translate(AndroidUtilities.dp(i + 10) + (f1 - f2), this.replyStartY);
      this.replyNameLayout.draw(paramCanvas);
      paramCanvas.restore();
      if (this.replyTextLayout != null)
      {
        paramCanvas.save();
        f1 = this.replyStartX;
        f2 = this.replyTextOffset;
        i = k;
        if (this.needReplyImage) {
          i = 44;
        }
        paramCanvas.translate(f1 - f2 + AndroidUtilities.dp(i + 10), this.replyStartY + AndroidUtilities.dp(19.0F));
        this.replyTextLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
      return;
      this.replyStartX = (this.backgroundDrawableLeft + this.backgroundDrawableRight + AndroidUtilities.dp(17.0F));
      break;
      if (this.currentMessageObject.isOutOwner())
      {
        Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_outReplyLine"));
        Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_outReplyNameText"));
        if ((this.currentMessageObject.hasValidReplyMessageObject()) && (this.currentMessageObject.replyMessageObject.type == 0) && (!(this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) && (!(this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)))
        {
          Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_outReplyMessageText"));
          this.replyStartX = (this.backgroundDrawableLeft + AndroidUtilities.dp(12.0F));
          if ((!this.drawForwardedName) || (this.forwardedNameLayout[0] == null)) {
            break label1870;
          }
          i = 36;
          if ((!this.drawNameLayout) || (this.nameLayout == null)) {
            break label1876;
          }
        }
      }
      for (j = 20;; j = 0)
      {
        this.replyStartY = AndroidUtilities.dp(j + (i + 12));
        break;
        localTextPaint = Theme.chat_replyTextPaint;
        if (isDrawSelectedBackground()) {}
        for (str = "chat_outReplyMediaMessageSelectedText";; str = "chat_outReplyMediaMessageText")
        {
          localTextPaint.setColor(Theme.getColor(str));
          break;
        }
        Theme.chat_replyLinePaint.setColor(Theme.getColor("chat_inReplyLine"));
        Theme.chat_replyNamePaint.setColor(Theme.getColor("chat_inReplyNameText"));
        if ((this.currentMessageObject.hasValidReplyMessageObject()) && (this.currentMessageObject.replyMessageObject.type == 0) && (!(this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) && (!(this.currentMessageObject.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)))
        {
          Theme.chat_replyTextPaint.setColor(Theme.getColor("chat_inReplyMessageText"));
          if (!this.mediaBackground) {
            break label1825;
          }
          this.replyStartX = (this.backgroundDrawableLeft + AndroidUtilities.dp(12.0F));
          break label1570;
        }
        localTextPaint = Theme.chat_replyTextPaint;
        if (isDrawSelectedBackground()) {}
        for (str = "chat_inReplyMediaMessageSelectedText";; str = "chat_inReplyMediaMessageText")
        {
          localTextPaint.setColor(Theme.getColor(str));
          break;
        }
        i = this.backgroundDrawableLeft;
        if ((!this.mediaBackground) && (this.drawPinnedBottom)) {}
        for (f1 = 12.0F;; f1 = 18.0F)
        {
          this.replyStartX = (AndroidUtilities.dp(f1) + i);
          break;
        }
        i = 0;
        break label1590;
      }
    }
  }
  
  public void drawRoundProgress(Canvas paramCanvas)
  {
    this.rect.set(this.photoImage.getImageX() + AndroidUtilities.dpf2(1.5F), this.photoImage.getImageY() + AndroidUtilities.dpf2(1.5F), this.photoImage.getImageX2() - AndroidUtilities.dpf2(1.5F), this.photoImage.getImageY2() - AndroidUtilities.dpf2(1.5F));
    paramCanvas.drawArc(this.rect, -90.0F, this.currentMessageObject.audioProgress * 360.0F, false, Theme.chat_radialProgressPaint);
  }
  
  public void drawTimeLayout(Canvas paramCanvas)
  {
    if (((this.drawTime) && (!this.groupPhotoInvisible)) || (((this.mediaBackground) && (this.captionLayout == null)) || (this.timeLayout == null))) {
      return;
    }
    label59:
    Object localObject1;
    label118:
    int k;
    int m;
    Object localObject2;
    int n;
    int i;
    if (this.currentMessageObject.type == 5)
    {
      Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
      if (this.drawPinnedBottom) {
        paramCanvas.translate(0.0F, AndroidUtilities.dp(2.0F));
      }
      if ((!this.mediaBackground) || (this.captionLayout != null)) {
        break label1307;
      }
      if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
        break label972;
      }
      localObject1 = Theme.chat_actionBackgroundPaint;
      j = ((Paint)localObject1).getAlpha();
      ((Paint)localObject1).setAlpha((int)(j * this.timeAlpha));
      Theme.chat_timePaint.setAlpha((int)(255.0F * this.timeAlpha));
      k = this.timeX - AndroidUtilities.dp(4.0F);
      m = this.layoutHeight - AndroidUtilities.dp(28.0F);
      localObject2 = this.rect;
      float f1 = k;
      float f2 = m;
      n = this.timeWidth;
      if (!this.currentMessageObject.isOutOwner()) {
        break label980;
      }
      i = 20;
      label214:
      ((RectF)localObject2).set(f1, f2, AndroidUtilities.dp(i + 8) + (k + n), AndroidUtilities.dp(17.0F) + m);
      paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), (Paint)localObject1);
      ((Paint)localObject1).setAlpha(j);
      j = (int)-this.timeLayout.getLineLeft(0);
      i = j;
      if ((this.currentMessageObject.messageOwner.flags & 0x400) != 0)
      {
        j += (int)(this.timeWidth - this.timeLayout.getLineWidth(0));
        if (!this.currentMessageObject.isSending()) {
          break label986;
        }
        i = j;
        if (!this.currentMessageObject.isOutOwner())
        {
          setDrawableBounds(Theme.chat_msgMediaClockDrawable, this.timeX + AndroidUtilities.dp(11.0F), this.layoutHeight - AndroidUtilities.dp(14.0F) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
          Theme.chat_msgMediaClockDrawable.draw(paramCanvas);
          i = j;
        }
      }
      label401:
      paramCanvas.save();
      paramCanvas.translate(this.timeX + i, this.layoutHeight - AndroidUtilities.dp(12.3F) - this.timeLayout.getHeight());
      this.timeLayout.draw(paramCanvas);
      paramCanvas.restore();
      Theme.chat_timePaint.setAlpha(255);
      label459:
      if (!this.currentMessageObject.isOutOwner()) {
        break label1489;
      }
      i = 0;
      j = 0;
      k = 0;
      m = 0;
      if ((int)(this.currentMessageObject.getDialogId() >> 32) != 1) {
        break label1838;
      }
      n = 1;
      label499:
      if (!this.currentMessageObject.isSending()) {
        break label1844;
      }
      i = 0;
      j = 0;
      k = 1;
      m = 0;
      label521:
      if (k != 0)
      {
        if ((!this.mediaBackground) || (this.captionLayout != null)) {
          break label1962;
        }
        if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
          break label1910;
        }
        setDrawableBounds(Theme.chat_msgStickerClockDrawable, this.layoutWidth - AndroidUtilities.dp(22.0F) - Theme.chat_msgStickerClockDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgStickerClockDrawable.getIntrinsicHeight());
        Theme.chat_msgStickerClockDrawable.draw(paramCanvas);
      }
      label612:
      if (n == 0) {
        break label2066;
      }
      if ((i != 0) || (j != 0))
      {
        if ((!this.mediaBackground) || (this.captionLayout != null)) {
          break label2014;
        }
        setDrawableBounds(Theme.chat_msgBroadcastMediaDrawable, this.layoutWidth - AndroidUtilities.dp(24.0F) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(14.0F) - Theme.chat_msgBroadcastMediaDrawable.getIntrinsicHeight());
        Theme.chat_msgBroadcastMediaDrawable.draw(paramCanvas);
      }
      label690:
      if (m == 0) {
        break label2676;
      }
      if ((!this.mediaBackground) || (this.captionLayout != null)) {
        break label2686;
      }
      i = this.layoutWidth - AndroidUtilities.dp(34.5F);
    }
    for (int j = this.layoutHeight - AndroidUtilities.dp(26.5F);; j = this.layoutHeight - AndroidUtilities.dp(21.0F))
    {
      this.rect.set(i, j, AndroidUtilities.dp(14.0F) + i, AndroidUtilities.dp(14.0F) + j);
      paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(1.0F), AndroidUtilities.dp(1.0F), Theme.chat_msgErrorPaint);
      setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0F) + i, AndroidUtilities.dp(2.0F) + j);
      Theme.chat_msgErrorDrawable.draw(paramCanvas);
      return;
      if ((this.mediaBackground) && (this.captionLayout == null))
      {
        if ((this.currentMessageObject.type == 13) || (this.currentMessageObject.type == 5))
        {
          Theme.chat_timePaint.setColor(Theme.getColor("chat_serviceText"));
          break label59;
        }
        Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
        break label59;
      }
      if (this.currentMessageObject.isOutOwner())
      {
        localObject2 = Theme.chat_timePaint;
        if (isDrawSelectedBackground()) {}
        for (localObject1 = "chat_outTimeSelectedText";; localObject1 = "chat_outTimeText")
        {
          ((TextPaint)localObject2).setColor(Theme.getColor((String)localObject1));
          break;
        }
      }
      localObject2 = Theme.chat_timePaint;
      if (isDrawSelectedBackground()) {}
      for (localObject1 = "chat_inTimeSelectedText";; localObject1 = "chat_inTimeText")
      {
        ((TextPaint)localObject2).setColor(Theme.getColor((String)localObject1));
        break;
      }
      label972:
      localObject1 = Theme.chat_timeBackgroundPaint;
      break label118;
      label980:
      i = 0;
      break label214;
      label986:
      if (this.currentMessageObject.isSendError())
      {
        i = j;
        if (this.currentMessageObject.isOutOwner()) {
          break label401;
        }
        i = this.timeX + AndroidUtilities.dp(11.0F);
        k = this.layoutHeight - AndroidUtilities.dp(27.5F);
        this.rect.set(i, k, AndroidUtilities.dp(14.0F) + i, AndroidUtilities.dp(14.0F) + k);
        paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(1.0F), AndroidUtilities.dp(1.0F), Theme.chat_msgErrorPaint);
        setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0F) + i, AndroidUtilities.dp(2.0F) + k);
        Theme.chat_msgErrorDrawable.draw(paramCanvas);
        i = j;
        break label401;
      }
      if ((this.currentMessageObject.type == 13) || (this.currentMessageObject.type == 5)) {}
      for (localObject1 = Theme.chat_msgStickerViewsDrawable;; localObject1 = Theme.chat_msgMediaViewsDrawable)
      {
        i = ((BitmapDrawable)localObject1).getPaint().getAlpha();
        ((Drawable)localObject1).setAlpha((int)(this.timeAlpha * i));
        setDrawableBounds((Drawable)localObject1, this.timeX, this.layoutHeight - AndroidUtilities.dp(10.5F) - this.timeLayout.getHeight());
        ((Drawable)localObject1).draw(paramCanvas);
        ((Drawable)localObject1).setAlpha(i);
        i = j;
        if (this.viewsLayout == null) {
          break;
        }
        paramCanvas.save();
        paramCanvas.translate(this.timeX + ((Drawable)localObject1).getIntrinsicWidth() + AndroidUtilities.dp(3.0F), this.layoutHeight - AndroidUtilities.dp(12.3F) - this.timeLayout.getHeight());
        this.viewsLayout.draw(paramCanvas);
        paramCanvas.restore();
        i = j;
        break;
      }
      label1307:
      j = (int)-this.timeLayout.getLineLeft(0);
      i = j;
      if ((this.currentMessageObject.messageOwner.flags & 0x400) != 0)
      {
        j += (int)(this.timeWidth - this.timeLayout.getLineWidth(0));
        if (!this.currentMessageObject.isSending()) {
          break label1499;
        }
        i = j;
        if (!this.currentMessageObject.isOutOwner())
        {
          if (!isDrawSelectedBackground()) {
            break label1491;
          }
          localObject1 = Theme.chat_msgInSelectedClockDrawable;
          label1396:
          setDrawableBounds((Drawable)localObject1, this.timeX + AndroidUtilities.dp(11.0F), this.layoutHeight - AndroidUtilities.dp(8.5F) - ((Drawable)localObject1).getIntrinsicHeight());
          ((Drawable)localObject1).draw(paramCanvas);
          i = j;
        }
      }
      for (;;)
      {
        paramCanvas.save();
        paramCanvas.translate(this.timeX + i, this.layoutHeight - AndroidUtilities.dp(6.5F) - this.timeLayout.getHeight());
        this.timeLayout.draw(paramCanvas);
        paramCanvas.restore();
        break label459;
        label1489:
        break;
        label1491:
        localObject1 = Theme.chat_msgInClockDrawable;
        break label1396;
        label1499:
        if (!this.currentMessageObject.isSendError()) {
          break label1639;
        }
        i = j;
        if (!this.currentMessageObject.isOutOwner())
        {
          i = this.timeX + AndroidUtilities.dp(11.0F);
          k = this.layoutHeight - AndroidUtilities.dp(20.5F);
          this.rect.set(i, k, AndroidUtilities.dp(14.0F) + i, AndroidUtilities.dp(14.0F) + k);
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(1.0F), AndroidUtilities.dp(1.0F), Theme.chat_msgErrorPaint);
          setDrawableBounds(Theme.chat_msgErrorDrawable, AndroidUtilities.dp(6.0F) + i, AndroidUtilities.dp(2.0F) + k);
          Theme.chat_msgErrorDrawable.draw(paramCanvas);
          i = j;
        }
      }
      label1639:
      if (!this.currentMessageObject.isOutOwner())
      {
        if (isDrawSelectedBackground()) {}
        for (localObject1 = Theme.chat_msgInViewsSelectedDrawable;; localObject1 = Theme.chat_msgInViewsDrawable)
        {
          setDrawableBounds((Drawable)localObject1, this.timeX, this.layoutHeight - AndroidUtilities.dp(4.5F) - this.timeLayout.getHeight());
          ((Drawable)localObject1).draw(paramCanvas);
          i = j;
          if (this.viewsLayout == null) {
            break;
          }
          paramCanvas.save();
          paramCanvas.translate(this.timeX + Theme.chat_msgInViewsDrawable.getIntrinsicWidth() + AndroidUtilities.dp(3.0F), this.layoutHeight - AndroidUtilities.dp(6.5F) - this.timeLayout.getHeight());
          this.viewsLayout.draw(paramCanvas);
          paramCanvas.restore();
          i = j;
          break;
        }
      }
      if (isDrawSelectedBackground()) {}
      for (localObject1 = Theme.chat_msgOutViewsSelectedDrawable;; localObject1 = Theme.chat_msgOutViewsDrawable)
      {
        setDrawableBounds((Drawable)localObject1, this.timeX, this.layoutHeight - AndroidUtilities.dp(4.5F) - this.timeLayout.getHeight());
        ((Drawable)localObject1).draw(paramCanvas);
        break;
      }
      label1838:
      n = 0;
      break label499;
      label1844:
      if (this.currentMessageObject.isSendError())
      {
        i = 0;
        j = 0;
        k = 0;
        m = 1;
        break label521;
      }
      if (!this.currentMessageObject.isSent()) {
        break label521;
      }
      if (!this.currentMessageObject.isUnread()) {}
      for (i = 1;; i = 0)
      {
        j = 1;
        k = 0;
        m = 0;
        break;
      }
      label1910:
      setDrawableBounds(Theme.chat_msgMediaClockDrawable, this.layoutWidth - AndroidUtilities.dp(22.0F) - Theme.chat_msgMediaClockDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgMediaClockDrawable.getIntrinsicHeight());
      Theme.chat_msgMediaClockDrawable.draw(paramCanvas);
      break label612;
      label1962:
      setDrawableBounds(Theme.chat_msgOutClockDrawable, this.layoutWidth - AndroidUtilities.dp(18.5F) - Theme.chat_msgOutClockDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(8.5F) - Theme.chat_msgOutClockDrawable.getIntrinsicHeight());
      Theme.chat_msgOutClockDrawable.draw(paramCanvas);
      break label612;
      label2014:
      setDrawableBounds(Theme.chat_msgBroadcastDrawable, this.layoutWidth - AndroidUtilities.dp(20.5F) - Theme.chat_msgBroadcastDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(8.0F) - Theme.chat_msgBroadcastDrawable.getIntrinsicHeight());
      Theme.chat_msgBroadcastDrawable.draw(paramCanvas);
      break label690;
      label2066:
      if (j != 0)
      {
        if ((!this.mediaBackground) || (this.captionLayout != null)) {
          break label2427;
        }
        if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
          break label2301;
        }
        if (i == 0) {
          break label2256;
        }
        setDrawableBounds(Theme.chat_msgStickerCheckDrawable, this.layoutWidth - AndroidUtilities.dp(26.3F) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
      }
      for (;;)
      {
        Theme.chat_msgStickerCheckDrawable.draw(paramCanvas);
        label2162:
        if (i == 0) {
          break label2490;
        }
        if ((!this.mediaBackground) || (this.captionLayout != null)) {
          break label2618;
        }
        if ((this.currentMessageObject.type != 13) && (this.currentMessageObject.type != 5)) {
          break label2542;
        }
        setDrawableBounds(Theme.chat_msgStickerHalfCheckDrawable, this.layoutWidth - AndroidUtilities.dp(21.5F) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgStickerHalfCheckDrawable.getIntrinsicHeight());
        Theme.chat_msgStickerHalfCheckDrawable.draw(paramCanvas);
        break;
        label2256:
        setDrawableBounds(Theme.chat_msgStickerCheckDrawable, this.layoutWidth - AndroidUtilities.dp(21.5F) - Theme.chat_msgStickerCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgStickerCheckDrawable.getIntrinsicHeight());
      }
      label2301:
      if (i != 0) {
        setDrawableBounds(Theme.chat_msgMediaCheckDrawable, this.layoutWidth - AndroidUtilities.dp(26.3F) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
      }
      for (;;)
      {
        Theme.chat_msgMediaCheckDrawable.setAlpha((int)(255.0F * this.timeAlpha));
        Theme.chat_msgMediaCheckDrawable.draw(paramCanvas);
        Theme.chat_msgMediaCheckDrawable.setAlpha(255);
        break;
        setDrawableBounds(Theme.chat_msgMediaCheckDrawable, this.layoutWidth - AndroidUtilities.dp(21.5F) - Theme.chat_msgMediaCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgMediaCheckDrawable.getIntrinsicHeight());
      }
      label2427:
      if (isDrawSelectedBackground())
      {
        localObject1 = Theme.chat_msgOutCheckSelectedDrawable;
        label2439:
        if (i == 0) {
          break label2500;
        }
        setDrawableBounds((Drawable)localObject1, this.layoutWidth - AndroidUtilities.dp(22.5F) - ((Drawable)localObject1).getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(8.0F) - ((Drawable)localObject1).getIntrinsicHeight());
      }
      for (;;)
      {
        ((Drawable)localObject1).draw(paramCanvas);
        break label2162;
        label2490:
        break;
        localObject1 = Theme.chat_msgOutCheckDrawable;
        break label2439;
        label2500:
        setDrawableBounds((Drawable)localObject1, this.layoutWidth - AndroidUtilities.dp(18.5F) - ((Drawable)localObject1).getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(8.0F) - ((Drawable)localObject1).getIntrinsicHeight());
      }
      label2542:
      setDrawableBounds(Theme.chat_msgMediaHalfCheckDrawable, this.layoutWidth - AndroidUtilities.dp(21.5F) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(13.5F) - Theme.chat_msgMediaHalfCheckDrawable.getIntrinsicHeight());
      Theme.chat_msgMediaHalfCheckDrawable.setAlpha((int)(255.0F * this.timeAlpha));
      Theme.chat_msgMediaHalfCheckDrawable.draw(paramCanvas);
      Theme.chat_msgMediaHalfCheckDrawable.setAlpha(255);
      break label690;
      label2618:
      if (isDrawSelectedBackground()) {}
      for (localObject1 = Theme.chat_msgOutHalfCheckSelectedDrawable;; localObject1 = Theme.chat_msgOutHalfCheckDrawable)
      {
        setDrawableBounds((Drawable)localObject1, this.layoutWidth - AndroidUtilities.dp(18.0F) - ((Drawable)localObject1).getIntrinsicWidth(), this.layoutHeight - AndroidUtilities.dp(8.0F) - ((Drawable)localObject1).getIntrinsicHeight());
        ((Drawable)localObject1).draw(paramCanvas);
        break label690;
        label2676:
        break;
      }
      label2686:
      i = this.layoutWidth - AndroidUtilities.dp(32.0F);
    }
  }
  
  public ImageReceiver getAvatarImage()
  {
    if (this.isAvatarVisible) {
      return this.avatarImage;
    }
    return null;
  }
  
  public int getBackgroundDrawableLeft()
  {
    int j = 0;
    int i = 0;
    if (this.currentMessageObject.isOutOwner())
    {
      j = this.layoutWidth;
      int k = this.backgroundWidth;
      if (!this.mediaBackground) {}
      for (;;)
      {
        return j - k - i;
        i = AndroidUtilities.dp(9.0F);
      }
    }
    i = j;
    if (this.isChat)
    {
      i = j;
      if (this.isAvatarVisible) {
        i = 48;
      }
    }
    if (!this.mediaBackground) {}
    for (j = 3;; j = 9) {
      return AndroidUtilities.dp(i + j);
    }
  }
  
  public int getCaptionHeight()
  {
    return this.addedCaptionHeight;
  }
  
  public MessageObject.GroupedMessages getCurrentMessagesGroup()
  {
    return this.currentMessagesGroup;
  }
  
  public MessageObject.GroupedMessagePosition getCurrentPosition()
  {
    return this.currentPosition;
  }
  
  public int getLayoutHeight()
  {
    return this.layoutHeight;
  }
  
  public MessageObject getMessageObject()
  {
    return this.currentMessageObject;
  }
  
  public int getObserverTag()
  {
    return this.TAG;
  }
  
  public ImageReceiver getPhotoImage()
  {
    return this.photoImage;
  }
  
  public boolean hasCaptionLayout()
  {
    return this.captionLayout != null;
  }
  
  public boolean hasNameLayout()
  {
    boolean bool = false;
    if (((this.drawNameLayout) && (this.nameLayout != null)) || ((this.drawForwardedName) && (this.forwardedNameLayout[0] != null) && (this.forwardedNameLayout[1] != null) && ((this.currentPosition == null) || ((this.currentPosition.minY == 0) && (this.currentPosition.minX == 0)))) || (this.replyNameLayout != null)) {
      bool = true;
    }
    return bool;
  }
  
  public boolean isInsideBackground(float paramFloat1, float paramFloat2)
  {
    return (this.currentBackgroundDrawable != null) && (paramFloat1 >= getLeft() + this.backgroundDrawableLeft) && (paramFloat1 <= getLeft() + this.backgroundDrawableLeft + this.backgroundDrawableRight);
  }
  
  public boolean isPinnedBottom()
  {
    return this.pinnedBottom;
  }
  
  public boolean isPinnedTop()
  {
    return this.pinnedTop;
  }
  
  public boolean needDelayRoundProgressDraw()
  {
    return (this.documentAttachType == 7) && (this.currentMessageObject.type != 5) && (MediaController.getInstance().isPlayingMessage(this.currentMessageObject));
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    setTranslationX(0.0F);
    this.avatarImage.onAttachedToWindow();
    this.avatarImage.setParentView((View)getParent());
    this.replyImageReceiver.onAttachedToWindow();
    this.locationImageReceiver.onAttachedToWindow();
    if (this.drawPhotoImage) {
      if (this.photoImage.onAttachedToWindow()) {
        updateButtonState(false);
      }
    }
    for (;;)
    {
      if ((this.currentMessageObject != null) && (this.currentMessageObject.isRoundVideo())) {
        checkRoundVideoPlayback(true);
      }
      return;
      updateButtonState(false);
    }
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.avatarImage.onDetachedFromWindow();
    this.replyImageReceiver.onDetachedFromWindow();
    this.locationImageReceiver.onDetachedFromWindow();
    this.photoImage.onDetachedFromWindow();
    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.currentMessageObject == null) {
      return;
    }
    if (!this.wasLayout)
    {
      requestLayout();
      return;
    }
    label90:
    label177:
    label200:
    int n;
    int i3;
    int j;
    int k;
    int i4;
    int m;
    Object localObject1;
    Object localObject2;
    label265:
    int i;
    label286:
    label312:
    label565:
    label589:
    Object localObject3;
    float f;
    label713:
    long l3;
    long l2;
    long l1;
    if (this.currentMessageObject.isOutOwner())
    {
      Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
      Theme.chat_msgTextPaint.linkColor = Theme.getColor("chat_messageLinkOut");
      Theme.chat_msgGameTextPaint.setColor(Theme.getColor("chat_messageTextOut"));
      Theme.chat_msgGameTextPaint.linkColor = Theme.getColor("chat_messageLinkOut");
      Theme.chat_replyTextPaint.linkColor = Theme.getColor("chat_messageLinkOut");
      if (this.documentAttach != null)
      {
        if (this.documentAttachType != 3) {
          break label1351;
        }
        if (!this.currentMessageObject.isOutOwner()) {
          break label1286;
        }
        this.seekBarWaveform.setColors(Theme.getColor("chat_outVoiceSeekbar"), Theme.getColor("chat_outVoiceSeekbarFill"), Theme.getColor("chat_outVoiceSeekbarSelected"));
        this.seekBar.setColors(Theme.getColor("chat_outAudioSeekbar"), Theme.getColor("chat_outAudioCacheSeekbar"), Theme.getColor("chat_outAudioSeekbarFill"), Theme.getColor("chat_outAudioSeekbarFill"), Theme.getColor("chat_outAudioSeekbarSelected"));
      }
      if (this.currentMessageObject.type != 5) {
        break label1454;
      }
      Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
      int i1 = 0;
      n = 0;
      i3 = 0;
      int i2 = 0;
      j = 0;
      k = 0;
      i4 = 0;
      m = 0;
      if (!this.currentMessageObject.isOutOwner()) {
        break label1674;
      }
      if ((this.mediaBackground) || (this.drawPinnedBottom)) {
        break label1600;
      }
      this.currentBackgroundDrawable = Theme.chat_msgOutDrawable;
      localObject1 = Theme.chat_msgOutSelectedDrawable;
      localObject2 = Theme.chat_msgOutShadowDrawable;
      k = this.layoutWidth;
      n = this.backgroundWidth;
      if (this.mediaBackground) {
        break label1620;
      }
      i = 0;
      this.backgroundDrawableLeft = (k - n - i);
      k = this.backgroundWidth;
      if (!this.mediaBackground) {
        break label1630;
      }
      i = 0;
      this.backgroundDrawableRight = (k - i);
      if ((this.currentMessagesGroup != null) && (!this.currentPosition.edge)) {
        this.backgroundDrawableRight += AndroidUtilities.dp(10.0F);
      }
      k = this.backgroundDrawableLeft;
      if ((!this.mediaBackground) && (this.drawPinnedBottom)) {
        this.backgroundDrawableRight -= AndroidUtilities.dp(6.0F);
      }
      i = j;
      n = k;
      if (this.currentPosition != null)
      {
        if ((this.currentPosition.flags & 0x2) == 0) {
          this.backgroundDrawableRight += AndroidUtilities.dp(8.0F);
        }
        j = k;
        if ((this.currentPosition.flags & 0x1) == 0)
        {
          j = k - AndroidUtilities.dp(8.0F);
          this.backgroundDrawableRight += AndroidUtilities.dp(8.0F);
        }
        k = i2;
        if ((this.currentPosition.flags & 0x4) == 0)
        {
          k = 0 - AndroidUtilities.dp(9.0F);
          m = 0 + AndroidUtilities.dp(9.0F);
        }
        i = m;
        i1 = k;
        n = j;
        if ((this.currentPosition.flags & 0x8) == 0)
        {
          i = m + AndroidUtilities.dp(9.0F);
          n = j;
          i1 = k;
        }
      }
      if ((!this.drawPinnedBottom) || (!this.drawPinnedTop)) {
        break label1640;
      }
      j = 0;
      if ((!this.drawPinnedTop) && ((!this.drawPinnedTop) || (!this.drawPinnedBottom))) {
        break label1665;
      }
      k = 0;
      k = i1 + k;
      setDrawableBounds(this.currentBackgroundDrawable, n, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
      setDrawableBounds((Drawable)localObject1, n, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
      setDrawableBounds((Drawable)localObject2, n, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
      localObject3 = localObject2;
      localObject2 = localObject1;
      if ((this.drawBackground) && (this.currentBackgroundDrawable != null))
      {
        if (!this.isHighlightedAnimated) {
          break label2271;
        }
        this.currentBackgroundDrawable.draw(paramCanvas);
        if (this.highlightProgress < 300) {
          break label2258;
        }
        f = 1.0F;
        if (this.currentPosition == null)
        {
          ((Drawable)localObject2).setAlpha((int)(255.0F * f));
          ((Drawable)localObject2).draw(paramCanvas);
        }
        l3 = System.currentTimeMillis();
        l2 = Math.abs(l3 - this.lastHighlightProgressTime);
        l1 = l2;
        if (l2 > 17L) {
          l1 = 17L;
        }
        this.highlightProgress = ((int)(this.highlightProgress - l1));
        this.lastHighlightProgressTime = l3;
        if (this.highlightProgress <= 0)
        {
          this.highlightProgress = 0;
          this.isHighlightedAnimated = false;
        }
        invalidate();
        label812:
        if ((this.currentPosition == null) || (this.currentPosition.flags != 0)) {
          ((Drawable)localObject3).draw(paramCanvas);
        }
      }
      drawContent(paramCanvas);
      if (this.drawShareButton)
      {
        localObject2 = Theme.chat_shareDrawable;
        if (!this.sharePressed) {
          break label2320;
        }
        localObject1 = Theme.colorPressedFilter;
        label864:
        ((Drawable)localObject2).setColorFilter((ColorFilter)localObject1);
        if (!this.currentMessageObject.isOutOwner()) {
          break label2328;
        }
        this.shareStartX = (this.currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(8.0F) - Theme.chat_shareDrawable.getIntrinsicWidth());
        label909:
        localObject1 = Theme.chat_shareDrawable;
        i = this.shareStartX;
        j = this.layoutHeight - AndroidUtilities.dp(41.0F);
        this.shareStartY = j;
        setDrawableBounds((Drawable)localObject1, i, j);
        Theme.chat_shareDrawable.draw(paramCanvas);
        if (!this.drwaShareGoIcon) {
          break label2352;
        }
        setDrawableBounds(Theme.chat_goIconDrawable, this.shareStartX + AndroidUtilities.dp(12.0F), this.shareStartY + AndroidUtilities.dp(9.0F));
        Theme.chat_goIconDrawable.draw(paramCanvas);
      }
    }
    for (;;)
    {
      if (this.currentPosition == null) {
        drawNamesLayout(paramCanvas);
      }
      if (((this.drawTime) || (!this.mediaBackground)) && (!this.forceNotDrawTime)) {
        drawTimeLayout(paramCanvas);
      }
      if ((this.controlsAlpha == 1.0F) && (this.timeAlpha == 1.0F)) {
        break;
      }
      l3 = System.currentTimeMillis();
      l2 = Math.abs(this.lastControlsAlphaChangeTime - l3);
      l1 = l2;
      if (l2 > 17L) {
        l1 = 17L;
      }
      this.totalChangeTime += l1;
      if (this.totalChangeTime > 100L) {
        this.totalChangeTime = 100L;
      }
      this.lastControlsAlphaChangeTime = l3;
      if (this.controlsAlpha != 1.0F) {
        this.controlsAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation((float)this.totalChangeTime / 100.0F);
      }
      if (this.timeAlpha != 1.0F) {
        this.timeAlpha = AndroidUtilities.decelerateInterpolator.getInterpolation((float)this.totalChangeTime / 100.0F);
      }
      invalidate();
      if ((!this.forceNotDrawTime) || (this.currentPosition == null) || (!this.currentPosition.last) || (getParent() == null)) {
        break;
      }
      ((View)getParent()).invalidate();
      return;
      Theme.chat_msgTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
      Theme.chat_msgTextPaint.linkColor = Theme.getColor("chat_messageLinkIn");
      Theme.chat_msgGameTextPaint.setColor(Theme.getColor("chat_messageTextIn"));
      Theme.chat_msgGameTextPaint.linkColor = Theme.getColor("chat_messageLinkIn");
      Theme.chat_replyTextPaint.linkColor = Theme.getColor("chat_messageLinkIn");
      break label90;
      label1286:
      this.seekBarWaveform.setColors(Theme.getColor("chat_inVoiceSeekbar"), Theme.getColor("chat_inVoiceSeekbarFill"), Theme.getColor("chat_inVoiceSeekbarSelected"));
      this.seekBar.setColors(Theme.getColor("chat_inAudioSeekbar"), Theme.getColor("chat_inAudioCacheSeekbar"), Theme.getColor("chat_inAudioSeekbarFill"), Theme.getColor("chat_inAudioSeekbarFill"), Theme.getColor("chat_inAudioSeekbarSelected"));
      break label177;
      label1351:
      if (this.documentAttachType != 5) {
        break label177;
      }
      this.documentAttachType = 5;
      if (this.currentMessageObject.isOutOwner())
      {
        this.seekBar.setColors(Theme.getColor("chat_outAudioSeekbar"), Theme.getColor("chat_outAudioCacheSeekbar"), Theme.getColor("chat_outAudioSeekbarFill"), Theme.getColor("chat_outAudioSeekbarFill"), Theme.getColor("chat_outAudioSeekbarSelected"));
        break label177;
      }
      this.seekBar.setColors(Theme.getColor("chat_inAudioSeekbar"), Theme.getColor("chat_inAudioCacheSeekbar"), Theme.getColor("chat_inAudioSeekbarFill"), Theme.getColor("chat_inAudioSeekbarFill"), Theme.getColor("chat_inAudioSeekbarSelected"));
      break label177;
      label1454:
      if (this.mediaBackground)
      {
        if ((this.currentMessageObject.type == 13) || (this.currentMessageObject.type == 5))
        {
          Theme.chat_timePaint.setColor(Theme.getColor("chat_serviceText"));
          break label200;
        }
        Theme.chat_timePaint.setColor(Theme.getColor("chat_mediaTimeText"));
        break label200;
      }
      if (this.currentMessageObject.isOutOwner())
      {
        localObject2 = Theme.chat_timePaint;
        if (isDrawSelectedBackground()) {}
        for (localObject1 = "chat_outTimeSelectedText";; localObject1 = "chat_outTimeText")
        {
          ((TextPaint)localObject2).setColor(Theme.getColor((String)localObject1));
          break;
        }
      }
      localObject2 = Theme.chat_timePaint;
      if (isDrawSelectedBackground()) {}
      for (localObject1 = "chat_inTimeSelectedText";; localObject1 = "chat_inTimeText")
      {
        ((TextPaint)localObject2).setColor(Theme.getColor((String)localObject1));
        break;
      }
      label1600:
      this.currentBackgroundDrawable = Theme.chat_msgOutMediaDrawable;
      localObject1 = Theme.chat_msgOutMediaSelectedDrawable;
      localObject2 = Theme.chat_msgOutMediaShadowDrawable;
      break label265;
      label1620:
      i = AndroidUtilities.dp(9.0F);
      break label286;
      label1630:
      i = AndroidUtilities.dp(3.0F);
      break label312;
      label1640:
      if (this.drawPinnedBottom)
      {
        j = AndroidUtilities.dp(1.0F);
        break label565;
      }
      j = AndroidUtilities.dp(2.0F);
      break label565;
      label1665:
      k = AndroidUtilities.dp(1.0F);
      break label589;
      label1674:
      if ((!this.mediaBackground) && (!this.drawPinnedBottom))
      {
        this.currentBackgroundDrawable = Theme.chat_msgInDrawable;
        localObject2 = Theme.chat_msgInSelectedDrawable;
        localObject1 = Theme.chat_msgInShadowDrawable;
        label1705:
        if ((!this.isChat) || (!this.isAvatarVisible)) {
          break label2202;
        }
        i = 48;
        label1722:
        if (this.mediaBackground) {
          break label2207;
        }
        j = 3;
        label1732:
        this.backgroundDrawableLeft = AndroidUtilities.dp(i + j);
        j = this.backgroundWidth;
        if (!this.mediaBackground) {
          break label2214;
        }
        i = 0;
        label1759:
        this.backgroundDrawableRight = (j - i);
        if (this.currentMessagesGroup != null)
        {
          if (!this.currentPosition.edge)
          {
            this.backgroundDrawableLeft -= AndroidUtilities.dp(10.0F);
            this.backgroundDrawableRight += AndroidUtilities.dp(10.0F);
          }
          if (this.currentPosition.leftSpanOffset != 0) {
            this.backgroundDrawableLeft += (int)Math.ceil(this.currentPosition.leftSpanOffset / 1000.0F * getGroupPhotosWidth());
          }
        }
        if ((!this.mediaBackground) && (this.drawPinnedBottom))
        {
          this.backgroundDrawableRight -= AndroidUtilities.dp(6.0F);
          this.backgroundDrawableLeft += AndroidUtilities.dp(6.0F);
        }
        i = i4;
        m = i3;
        if (this.currentPosition != null)
        {
          if ((this.currentPosition.flags & 0x2) == 0) {
            this.backgroundDrawableRight += AndroidUtilities.dp(8.0F);
          }
          if ((this.currentPosition.flags & 0x1) == 0)
          {
            this.backgroundDrawableLeft -= AndroidUtilities.dp(8.0F);
            this.backgroundDrawableRight += AndroidUtilities.dp(8.0F);
          }
          j = n;
          if ((this.currentPosition.flags & 0x4) == 0)
          {
            j = 0 - AndroidUtilities.dp(9.0F);
            k = 0 + AndroidUtilities.dp(9.0F);
          }
          i = k;
          m = j;
          if ((this.currentPosition.flags & 0x8) == 0)
          {
            i = k + AndroidUtilities.dp(10.0F);
            m = j;
          }
        }
        if ((!this.drawPinnedBottom) || (!this.drawPinnedTop)) {
          break label2224;
        }
        j = 0;
        label2070:
        if ((!this.drawPinnedTop) && ((!this.drawPinnedTop) || (!this.drawPinnedBottom))) {
          break label2249;
        }
      }
      label2202:
      label2207:
      label2214:
      label2224:
      label2249:
      for (k = 0;; k = AndroidUtilities.dp(1.0F))
      {
        k = m + k;
        setDrawableBounds(this.currentBackgroundDrawable, this.backgroundDrawableLeft, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
        setDrawableBounds((Drawable)localObject2, this.backgroundDrawableLeft, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
        setDrawableBounds((Drawable)localObject1, this.backgroundDrawableLeft, k, this.backgroundDrawableRight, this.layoutHeight - j + i);
        localObject3 = localObject1;
        break;
        this.currentBackgroundDrawable = Theme.chat_msgInMediaDrawable;
        localObject2 = Theme.chat_msgInMediaSelectedDrawable;
        localObject1 = Theme.chat_msgInMediaShadowDrawable;
        break label1705;
        i = 0;
        break label1722;
        j = 9;
        break label1732;
        i = AndroidUtilities.dp(3.0F);
        break label1759;
        if (this.drawPinnedBottom)
        {
          j = AndroidUtilities.dp(1.0F);
          break label2070;
        }
        j = AndroidUtilities.dp(2.0F);
        break label2070;
      }
      label2258:
      f = this.highlightProgress / 300.0F;
      break label713;
      label2271:
      if ((isDrawSelectedBackground()) && ((this.currentPosition == null) || (getBackground() != null)))
      {
        ((Drawable)localObject2).setAlpha(255);
        ((Drawable)localObject2).draw(paramCanvas);
        break label812;
      }
      this.currentBackgroundDrawable.draw(paramCanvas);
      break label812;
      label2320:
      localObject1 = Theme.colorFilter;
      break label864;
      label2328:
      this.shareStartX = (this.currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(8.0F));
      break label909;
      label2352:
      setDrawableBounds(Theme.chat_shareIconDrawable, this.shareStartX + AndroidUtilities.dp(9.0F), this.shareStartY + AndroidUtilities.dp(9.0F));
      Theme.chat_shareIconDrawable.draw(paramCanvas);
    }
  }
  
  public void onFailedDownload(String paramString)
  {
    if ((this.documentAttachType == 3) || (this.documentAttachType == 5)) {}
    for (boolean bool = true;; bool = false)
    {
      updateButtonState(bool);
      return;
    }
  }
  
  @SuppressLint({"DrawAllocation"})
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.currentMessageObject == null) {
      return;
    }
    label155:
    label200:
    label361:
    Object localObject;
    if ((paramBoolean) || (!this.wasLayout))
    {
      this.layoutWidth = getMeasuredWidth();
      this.layoutHeight = (getMeasuredHeight() - this.substractBackgroundHeight);
      if (this.timeTextWidth < 0) {
        this.timeTextWidth = AndroidUtilities.dp(10.0F);
      }
      this.timeLayout = new StaticLayout(this.currentTimeString, Theme.chat_timePaint, this.timeTextWidth + AndroidUtilities.dp(100.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      if (this.mediaBackground) {
        break label607;
      }
      if (this.currentMessageObject.isOutOwner()) {
        break label584;
      }
      paramInt2 = this.backgroundWidth;
      paramInt3 = AndroidUtilities.dp(9.0F);
      paramInt4 = this.timeWidth;
      if (this.isAvatarVisible)
      {
        paramInt1 = AndroidUtilities.dp(48.0F);
        this.timeX = (paramInt1 + (paramInt2 - paramInt3 - paramInt4));
        if ((this.currentMessageObject.messageOwner.flags & 0x400) == 0) {
          break label743;
        }
        this.viewsLayout = new StaticLayout(this.currentViewsString, Theme.chat_timePaint, this.viewsTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        if (this.isAvatarVisible) {
          this.avatarImage.setImageCoords(AndroidUtilities.dp(6.0F), this.avatarImage.getImageY(), AndroidUtilities.dp(42.0F), AndroidUtilities.dp(42.0F));
        }
        this.wasLayout = true;
      }
    }
    else
    {
      if (this.currentMessageObject.type == 0) {
        this.textY = (AndroidUtilities.dp(10.0F) + this.namesOffset);
      }
      if (this.currentMessageObject.isRoundVideo()) {
        updatePlayingMessageProgress();
      }
      if (this.documentAttachType != 3) {
        break label844;
      }
      if (!this.currentMessageObject.isOutOwner()) {
        break label751;
      }
      this.seekBarX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(57.0F));
      this.buttonX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(14.0F));
      this.timeAudioX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(67.0F));
      if (this.hasLinkPreview)
      {
        this.seekBarX += AndroidUtilities.dp(10.0F);
        this.buttonX += AndroidUtilities.dp(10.0F);
        this.timeAudioX += AndroidUtilities.dp(10.0F);
      }
      localObject = this.seekBarWaveform;
      paramInt2 = this.backgroundWidth;
      if (!this.hasLinkPreview) {
        break label834;
      }
      paramInt1 = 10;
      label434:
      ((SeekBarWaveform)localObject).setSize(paramInt2 - AndroidUtilities.dp(paramInt1 + 92), AndroidUtilities.dp(30.0F));
      localObject = this.seekBar;
      paramInt2 = this.backgroundWidth;
      if (!this.hasLinkPreview) {
        break label839;
      }
    }
    label584:
    label607:
    label743:
    label751:
    label834:
    label839:
    for (paramInt1 = 10;; paramInt1 = 0)
    {
      ((SeekBar)localObject).setSize(paramInt2 - AndroidUtilities.dp(paramInt1 + 72), AndroidUtilities.dp(30.0F));
      this.seekBarY = (AndroidUtilities.dp(13.0F) + this.namesOffset + this.mediaOffsetY);
      this.buttonY = (AndroidUtilities.dp(13.0F) + this.namesOffset + this.mediaOffsetY);
      this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0F), this.buttonY + AndroidUtilities.dp(44.0F));
      updatePlayingMessageProgress();
      return;
      paramInt1 = 0;
      break;
      this.timeX = (this.layoutWidth - this.timeWidth - AndroidUtilities.dp(38.5F));
      break label155;
      if (!this.currentMessageObject.isOutOwner())
      {
        paramInt2 = this.backgroundWidth;
        paramInt3 = AndroidUtilities.dp(4.0F);
        paramInt4 = this.timeWidth;
        if (this.isAvatarVisible) {}
        for (paramInt1 = AndroidUtilities.dp(48.0F);; paramInt1 = 0)
        {
          this.timeX = (paramInt1 + (paramInt2 - paramInt3 - paramInt4));
          if ((this.currentPosition == null) || (this.currentPosition.leftSpanOffset == 0)) {
            break;
          }
          this.timeX += (int)Math.ceil(this.currentPosition.leftSpanOffset / 1000.0F * getGroupPhotosWidth());
          break;
        }
      }
      this.timeX = (this.layoutWidth - this.timeWidth - AndroidUtilities.dp(42.0F));
      break label155;
      this.viewsLayout = null;
      break label200;
      if ((this.isChat) && (this.currentMessageObject.needDrawAvatar()))
      {
        this.seekBarX = AndroidUtilities.dp(114.0F);
        this.buttonX = AndroidUtilities.dp(71.0F);
        this.timeAudioX = AndroidUtilities.dp(124.0F);
        break label361;
      }
      this.seekBarX = AndroidUtilities.dp(66.0F);
      this.buttonX = AndroidUtilities.dp(23.0F);
      this.timeAudioX = AndroidUtilities.dp(76.0F);
      break label361;
      paramInt1 = 0;
      break label434;
    }
    label844:
    if (this.documentAttachType == 5)
    {
      if (this.currentMessageObject.isOutOwner())
      {
        this.seekBarX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(56.0F));
        this.buttonX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(14.0F));
        this.timeAudioX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(67.0F));
        if (this.hasLinkPreview)
        {
          this.seekBarX += AndroidUtilities.dp(10.0F);
          this.buttonX += AndroidUtilities.dp(10.0F);
          this.timeAudioX += AndroidUtilities.dp(10.0F);
        }
        localObject = this.seekBar;
        paramInt2 = this.backgroundWidth;
        if (!this.hasLinkPreview) {
          break label1181;
        }
      }
      label1181:
      for (paramInt1 = 10;; paramInt1 = 0)
      {
        ((SeekBar)localObject).setSize(paramInt2 - AndroidUtilities.dp(paramInt1 + 65), AndroidUtilities.dp(30.0F));
        this.seekBarY = (AndroidUtilities.dp(29.0F) + this.namesOffset + this.mediaOffsetY);
        this.buttonY = (AndroidUtilities.dp(13.0F) + this.namesOffset + this.mediaOffsetY);
        this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0F), this.buttonY + AndroidUtilities.dp(44.0F));
        updatePlayingMessageProgress();
        return;
        if ((this.isChat) && (this.currentMessageObject.needDrawAvatar()))
        {
          this.seekBarX = AndroidUtilities.dp(113.0F);
          this.buttonX = AndroidUtilities.dp(71.0F);
          this.timeAudioX = AndroidUtilities.dp(124.0F);
          break;
        }
        this.seekBarX = AndroidUtilities.dp(65.0F);
        this.buttonX = AndroidUtilities.dp(23.0F);
        this.timeAudioX = AndroidUtilities.dp(76.0F);
        break;
      }
    }
    if ((this.documentAttachType == 1) && (!this.drawPhotoImage))
    {
      if (this.currentMessageObject.isOutOwner()) {
        this.buttonX = (this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(14.0F));
      }
      for (;;)
      {
        if (this.hasLinkPreview) {
          this.buttonX += AndroidUtilities.dp(10.0F);
        }
        this.buttonY = (AndroidUtilities.dp(13.0F) + this.namesOffset + this.mediaOffsetY);
        this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(44.0F), this.buttonY + AndroidUtilities.dp(44.0F));
        this.photoImage.setImageCoords(this.buttonX - AndroidUtilities.dp(10.0F), this.buttonY - AndroidUtilities.dp(10.0F), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
        return;
        if ((this.isChat) && (this.currentMessageObject.needDrawAvatar())) {
          this.buttonX = AndroidUtilities.dp(71.0F);
        } else {
          this.buttonX = AndroidUtilities.dp(23.0F);
        }
      }
    }
    if (this.currentMessageObject.type == 12)
    {
      if (this.currentMessageObject.isOutOwner()) {
        paramInt1 = this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(14.0F);
      }
      for (;;)
      {
        this.photoImage.setImageCoords(paramInt1, AndroidUtilities.dp(13.0F) + this.namesOffset, AndroidUtilities.dp(44.0F), AndroidUtilities.dp(44.0F));
        return;
        if ((this.isChat) && (this.currentMessageObject.needDrawAvatar())) {
          paramInt1 = AndroidUtilities.dp(72.0F);
        } else {
          paramInt1 = AndroidUtilities.dp(23.0F);
        }
      }
    }
    if ((this.currentMessageObject.type == 0) && ((this.hasLinkPreview) || (this.hasGamePreview) || (this.hasInvoicePreview))) {
      if (this.hasGamePreview)
      {
        paramInt1 = this.unmovedTextX - AndroidUtilities.dp(10.0F);
        if (!this.isSmallImage) {
          break label1859;
        }
        paramInt1 = this.backgroundWidth + paramInt1 - AndroidUtilities.dp(81.0F);
      }
    }
    for (;;)
    {
      paramInt3 = paramInt1;
      if (this.currentPosition != null)
      {
        paramInt2 = paramInt1;
        if ((this.currentPosition.flags & 0x1) == 0) {
          paramInt2 = paramInt1 - AndroidUtilities.dp(4.0F);
        }
        paramInt3 = paramInt2;
        if (this.currentPosition.leftSpanOffset != 0) {
          paramInt3 = paramInt2 + (int)Math.ceil(this.currentPosition.leftSpanOffset / 1000.0F * getGroupPhotosWidth());
        }
      }
      this.photoImage.setImageCoords(paramInt3, this.photoImage.getImageY(), this.photoImage.getImageWidth(), this.photoImage.getImageHeight());
      this.buttonX = ((int)(paramInt3 + (this.photoImage.getImageWidth() - AndroidUtilities.dp(48.0F)) / 2.0F));
      this.buttonY = (this.photoImage.getImageY() + (this.photoImage.getImageHeight() - AndroidUtilities.dp(48.0F)) / 2);
      this.radialProgress.setProgressRect(this.buttonX, this.buttonY, this.buttonX + AndroidUtilities.dp(48.0F), this.buttonY + AndroidUtilities.dp(48.0F));
      this.deleteProgressRect.set(this.buttonX + AndroidUtilities.dp(3.0F), this.buttonY + AndroidUtilities.dp(3.0F), this.buttonX + AndroidUtilities.dp(45.0F), this.buttonY + AndroidUtilities.dp(45.0F));
      return;
      if (this.hasInvoicePreview)
      {
        paramInt1 = this.unmovedTextX + AndroidUtilities.dp(1.0F);
        break;
      }
      paramInt1 = this.unmovedTextX + AndroidUtilities.dp(1.0F);
      break;
      label1859:
      if (this.hasInvoicePreview) {}
      for (paramInt2 = -AndroidUtilities.dp(6.3F);; paramInt2 = AndroidUtilities.dp(10.0F))
      {
        paramInt1 += paramInt2;
        break;
      }
      if (!this.currentMessageObject.isOutOwner()) {
        break label1948;
      }
      if (this.mediaBackground) {
        paramInt1 = this.layoutWidth - this.backgroundWidth - AndroidUtilities.dp(3.0F);
      } else {
        paramInt1 = this.layoutWidth - this.backgroundWidth + AndroidUtilities.dp(6.0F);
      }
    }
    label1948:
    if ((this.isChat) && (this.isAvatarVisible)) {}
    for (paramInt2 = AndroidUtilities.dp(63.0F);; paramInt2 = AndroidUtilities.dp(15.0F))
    {
      paramInt1 = paramInt2;
      if (this.currentPosition == null) {
        break;
      }
      paramInt1 = paramInt2;
      if (this.currentPosition.edge) {
        break;
      }
      paramInt1 = paramInt2 - AndroidUtilities.dp(10.0F);
      break;
    }
  }
  
  protected void onLongPress()
  {
    if ((this.pressedLink instanceof URLSpanMono)) {
      this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
    }
    do
    {
      return;
      if ((this.pressedLink instanceof URLSpanNoUnderline))
      {
        if (((URLSpanNoUnderline)this.pressedLink).getURL().startsWith("/")) {
          this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
        }
      }
      else if ((this.pressedLink instanceof URLSpan))
      {
        this.delegate.didPressedUrl(this.currentMessageObject, this.pressedLink, true);
        return;
      }
      resetPressedLink(-1);
      if ((this.buttonPressed != 0) || (this.miniButtonPressed != 0) || (this.pressedBotButton != -1))
      {
        this.buttonPressed = 0;
        this.miniButtonState = 0;
        this.pressedBotButton = -1;
        invalidate();
      }
      if (this.instantPressed)
      {
        this.instantButtonPressed = false;
        this.instantPressed = false;
        if ((Build.VERSION.SDK_INT >= 21) && (this.instantViewSelectorDrawable != null)) {
          this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
        }
        invalidate();
      }
    } while (this.delegate == null);
    this.delegate.didLongPressed(this);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    if ((this.currentMessageObject != null) && ((this.currentMessageObject.checkLayout()) || ((this.currentPosition != null) && (this.lastHeight != AndroidUtilities.displaySize.y))))
    {
      this.inLayout = true;
      MessageObject localMessageObject = this.currentMessageObject;
      this.currentMessageObject = null;
      setMessageObject(localMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
      this.inLayout = false;
    }
    setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), this.totalHeight + this.keyboardHeight);
    this.lastHeight = AndroidUtilities.displaySize.y;
  }
  
  public void onProgressDownload(String paramString, float paramFloat)
  {
    this.radialProgress.setProgress(paramFloat, true);
    if ((this.documentAttachType == 3) || (this.documentAttachType == 5)) {
      if (this.hasMiniProgress != 0) {
        if (this.miniButtonState != 1) {
          updateButtonState(false);
        }
      }
    }
    do
    {
      do
      {
        do
        {
          return;
        } while (this.buttonState == 4);
        updateButtonState(false);
        return;
        if (this.hasMiniProgress == 0) {
          break;
        }
      } while (this.miniButtonState == 1);
      updateButtonState(false);
      return;
    } while (this.buttonState == 1);
    updateButtonState(false);
  }
  
  public void onProgressUpload(String paramString, float paramFloat, boolean paramBoolean)
  {
    this.radialProgress.setProgress(paramFloat, true);
    if ((paramFloat == 1.0F) && (this.currentPosition != null) && (SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId())) && (this.buttonState == 1))
    {
      this.drawRadialCheckBackground = true;
      this.radialProgress.setCheckBackground(false, true);
    }
  }
  
  public void onProvideStructure(ViewStructure paramViewStructure)
  {
    super.onProvideStructure(paramViewStructure);
    if ((this.allowAssistant) && (Build.VERSION.SDK_INT >= 23))
    {
      if ((this.currentMessageObject.messageText == null) || (this.currentMessageObject.messageText.length() <= 0)) {
        break label57;
      }
      paramViewStructure.setText(this.currentMessageObject.messageText);
    }
    label57:
    while ((this.currentMessageObject.caption == null) || (this.currentMessageObject.caption.length() <= 0)) {
      return;
    }
    paramViewStructure.setText(this.currentMessageObject.caption);
  }
  
  public void onSeekBarDrag(float paramFloat)
  {
    if (this.currentMessageObject == null) {
      return;
    }
    this.currentMessageObject.audioProgress = paramFloat;
    MediaController.getInstance().seekToProgress(this.currentMessageObject, paramFloat);
  }
  
  public void onSuccessDownload(String paramString)
  {
    if ((this.documentAttachType == 3) || (this.documentAttachType == 5))
    {
      updateButtonState(true);
      updateWaveform();
    }
    for (;;)
    {
      return;
      this.radialProgress.setProgress(1.0F, true);
      if (this.currentMessageObject.type == 0)
      {
        if ((this.documentAttachType == 2) && (this.currentMessageObject.gifState != 1.0F))
        {
          this.buttonState = 2;
          didPressedButton(true);
          return;
        }
        if (!this.photoNotSet)
        {
          updateButtonState(true);
          return;
        }
        setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
        return;
      }
      if ((!this.photoNotSet) || (((this.currentMessageObject.type == 8) || (this.currentMessageObject.type == 5)) && (this.currentMessageObject.gifState != 1.0F)))
      {
        if (((this.currentMessageObject.type != 8) && (this.currentMessageObject.type != 5)) || (this.currentMessageObject.gifState == 1.0F)) {
          break label230;
        }
        this.photoNotSet = false;
        this.buttonState = 2;
        didPressedButton(true);
      }
      while (this.photoNotSet)
      {
        setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
        return;
        label230:
        updateButtonState(true);
      }
    }
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    boolean bool3;
    if ((this.currentMessageObject == null) || (!this.delegate.canPerformActions())) {
      bool3 = super.onTouchEvent(paramMotionEvent);
    }
    boolean bool2;
    float f1;
    float f2;
    label678:
    label816:
    label986:
    label1179:
    label1370:
    label1452:
    label1651:
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
                                    boolean bool1;
                                    do
                                    {
                                      do
                                      {
                                        return bool3;
                                        this.disallowLongPress = false;
                                        bool2 = checkTextBlockMotionEvent(paramMotionEvent);
                                        bool1 = bool2;
                                        if (!bool2) {
                                          bool1 = checkOtherButtonMotionEvent(paramMotionEvent);
                                        }
                                        bool2 = bool1;
                                        if (!bool1) {
                                          bool2 = checkCaptionMotionEvent(paramMotionEvent);
                                        }
                                        bool1 = bool2;
                                        if (!bool2) {
                                          bool1 = checkAudioMotionEvent(paramMotionEvent);
                                        }
                                        bool2 = bool1;
                                        if (!bool1) {
                                          bool2 = checkLinkPreviewMotionEvent(paramMotionEvent);
                                        }
                                        bool1 = bool2;
                                        if (!bool2) {
                                          bool1 = checkGameMotionEvent(paramMotionEvent);
                                        }
                                        bool2 = bool1;
                                        if (!bool1) {
                                          bool2 = checkPhotoImageMotionEvent(paramMotionEvent);
                                        }
                                        bool1 = bool2;
                                        if (!bool2) {
                                          bool1 = checkBotButtonMotionEvent(paramMotionEvent);
                                        }
                                        bool2 = bool1;
                                        if (paramMotionEvent.getAction() == 3)
                                        {
                                          this.buttonPressed = 0;
                                          this.miniButtonPressed = 0;
                                          this.pressedBotButton = -1;
                                          this.linkPreviewPressed = false;
                                          this.otherPressed = false;
                                          this.imagePressed = false;
                                          this.gamePreviewPressed = false;
                                          this.instantButtonPressed = false;
                                          this.instantPressed = false;
                                          if ((Build.VERSION.SDK_INT >= 21) && (this.instantViewSelectorDrawable != null)) {
                                            this.instantViewSelectorDrawable.setState(StateSet.NOTHING);
                                          }
                                          bool2 = false;
                                          resetPressedLink(-1);
                                        }
                                        if ((!this.disallowLongPress) && (bool2) && (paramMotionEvent.getAction() == 0)) {
                                          startCheckLongPress();
                                        }
                                        if ((paramMotionEvent.getAction() != 0) && (paramMotionEvent.getAction() != 2)) {
                                          cancelCheckLongPress();
                                        }
                                        bool3 = bool2;
                                      } while (bool2);
                                      f1 = paramMotionEvent.getX();
                                      f2 = paramMotionEvent.getY();
                                      if (paramMotionEvent.getAction() != 0) {
                                        break label816;
                                      }
                                      if (this.delegate == null) {
                                        break;
                                      }
                                      bool3 = bool2;
                                    } while (!this.delegate.canPerformActions());
                                    if ((this.isAvatarVisible) && (this.avatarImage.isInsideImage(f1, getTop() + f2)))
                                    {
                                      this.avatarPressed = true;
                                      bool1 = true;
                                    }
                                    do
                                    {
                                      for (;;)
                                      {
                                        bool3 = bool1;
                                        if (!bool1) {
                                          break;
                                        }
                                        startCheckLongPress();
                                        return bool1;
                                        if ((this.drawForwardedName) && (this.forwardedNameLayout[0] != null) && (f1 >= this.forwardNameX) && (f1 <= this.forwardNameX + this.forwardedNameWidth) && (f2 >= this.forwardNameY) && (f2 <= this.forwardNameY + AndroidUtilities.dp(32.0F)))
                                        {
                                          if ((this.viaWidth != 0) && (f1 >= this.forwardNameX + this.viaNameWidth + AndroidUtilities.dp(4.0F))) {
                                            this.forwardBotPressed = true;
                                          }
                                          for (;;)
                                          {
                                            bool1 = true;
                                            break;
                                            this.forwardNamePressed = true;
                                          }
                                        }
                                        if ((this.drawNameLayout) && (this.nameLayout != null) && (this.viaWidth != 0) && (f1 >= this.nameX + this.viaNameWidth) && (f1 <= this.nameX + this.viaNameWidth + this.viaWidth) && (f2 >= this.nameY - AndroidUtilities.dp(4.0F)) && (f2 <= this.nameY + AndroidUtilities.dp(20.0F)))
                                        {
                                          this.forwardBotPressed = true;
                                          bool1 = true;
                                        }
                                        else
                                        {
                                          if ((!this.drawShareButton) || (f1 < this.shareStartX) || (f1 > this.shareStartX + AndroidUtilities.dp(40.0F)) || (f2 < this.shareStartY) || (f2 > this.shareStartY + AndroidUtilities.dp(32.0F))) {
                                            break label678;
                                          }
                                          this.sharePressed = true;
                                          bool1 = true;
                                          invalidate();
                                        }
                                      }
                                      bool1 = bool2;
                                    } while (this.replyNameLayout == null);
                                    if ((this.currentMessageObject.type == 13) || (this.currentMessageObject.type == 5)) {}
                                    for (i = this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth);; i = this.replyStartX + this.backgroundDrawableRight)
                                    {
                                      bool1 = bool2;
                                      if (f1 < this.replyStartX) {
                                        break;
                                      }
                                      bool1 = bool2;
                                      if (f1 > i) {
                                        break;
                                      }
                                      bool1 = bool2;
                                      if (f2 < this.replyStartY) {
                                        break;
                                      }
                                      bool1 = bool2;
                                      if (f2 > this.replyStartY + AndroidUtilities.dp(35.0F)) {
                                        break;
                                      }
                                      this.replyPressed = true;
                                      bool1 = true;
                                      break;
                                    }
                                    if (paramMotionEvent.getAction() != 2) {
                                      cancelCheckLongPress();
                                    }
                                    if (!this.avatarPressed) {
                                      break label986;
                                    }
                                    if (paramMotionEvent.getAction() != 1) {
                                      break;
                                    }
                                    this.avatarPressed = false;
                                    playSoundEffect(0);
                                    bool3 = bool2;
                                  } while (this.delegate == null);
                                  if (this.currentUser != null)
                                  {
                                    this.delegate.didPressedUserAvatar(this, this.currentUser);
                                    return bool2;
                                  }
                                  bool3 = bool2;
                                } while (this.currentChat == null);
                                this.delegate.didPressedChannelAvatar(this, this.currentChat, 0);
                                return bool2;
                                if (paramMotionEvent.getAction() == 3)
                                {
                                  this.avatarPressed = false;
                                  return bool2;
                                }
                                bool3 = bool2;
                              } while (paramMotionEvent.getAction() != 2);
                              bool3 = bool2;
                            } while (!this.isAvatarVisible);
                            bool3 = bool2;
                          } while (this.avatarImage.isInsideImage(f1, getTop() + f2));
                          this.avatarPressed = false;
                          return bool2;
                          if (!this.forwardNamePressed) {
                            break label1179;
                          }
                          if (paramMotionEvent.getAction() != 1) {
                            break;
                          }
                          this.forwardNamePressed = false;
                          playSoundEffect(0);
                          bool3 = bool2;
                        } while (this.delegate == null);
                        if (this.currentForwardChannel != null)
                        {
                          this.delegate.didPressedChannelAvatar(this, this.currentForwardChannel, this.currentMessageObject.messageOwner.fwd_from.channel_post);
                          return bool2;
                        }
                        bool3 = bool2;
                      } while (this.currentForwardUser == null);
                      this.delegate.didPressedUserAvatar(this, this.currentForwardUser);
                      return bool2;
                      if (paramMotionEvent.getAction() == 3)
                      {
                        this.forwardNamePressed = false;
                        return bool2;
                      }
                      bool3 = bool2;
                    } while (paramMotionEvent.getAction() != 2);
                    if ((f1 < this.forwardNameX) || (f1 > this.forwardNameX + this.forwardedNameWidth) || (f2 < this.forwardNameY)) {
                      break;
                    }
                    bool3 = bool2;
                  } while (f2 <= this.forwardNameY + AndroidUtilities.dp(32.0F));
                  this.forwardNamePressed = false;
                  return bool2;
                  if (!this.forwardBotPressed) {
                    break label1452;
                  }
                  if (paramMotionEvent.getAction() != 1) {
                    break;
                  }
                  this.forwardBotPressed = false;
                  playSoundEffect(0);
                  bool3 = bool2;
                } while (this.delegate == null);
                ChatMessageCellDelegate localChatMessageCellDelegate = this.delegate;
                if (this.currentViaBotUser != null) {}
                for (paramMotionEvent = this.currentViaBotUser.username;; paramMotionEvent = this.currentMessageObject.messageOwner.via_bot_name)
                {
                  localChatMessageCellDelegate.didPressedViaBot(this, paramMotionEvent);
                  return bool2;
                }
                if (paramMotionEvent.getAction() == 3)
                {
                  this.forwardBotPressed = false;
                  return bool2;
                }
                bool3 = bool2;
              } while (paramMotionEvent.getAction() != 2);
              if ((!this.drawForwardedName) || (this.forwardedNameLayout[0] == null)) {
                break label1370;
              }
              if ((f1 < this.forwardNameX) || (f1 > this.forwardNameX + this.forwardedNameWidth) || (f2 < this.forwardNameY)) {
                break;
              }
              bool3 = bool2;
            } while (f2 <= this.forwardNameY + AndroidUtilities.dp(32.0F));
            this.forwardBotPressed = false;
            return bool2;
            if ((f1 < this.nameX + this.viaNameWidth) || (f1 > this.nameX + this.viaNameWidth + this.viaWidth) || (f2 < this.nameY - AndroidUtilities.dp(4.0F))) {
              break;
            }
            bool3 = bool2;
          } while (f2 <= this.nameY + AndroidUtilities.dp(20.0F));
          this.forwardBotPressed = false;
          return bool2;
          if (!this.replyPressed) {
            break label1651;
          }
          if (paramMotionEvent.getAction() != 1) {
            break;
          }
          this.replyPressed = false;
          playSoundEffect(0);
          bool3 = bool2;
        } while (this.delegate == null);
        this.delegate.didPressedReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
        return bool2;
        if (paramMotionEvent.getAction() == 3)
        {
          this.replyPressed = false;
          return bool2;
        }
        bool3 = bool2;
      } while (paramMotionEvent.getAction() != 2);
      if ((this.currentMessageObject.type == 13) || (this.currentMessageObject.type == 5)) {}
      for (int i = this.replyStartX + Math.max(this.replyNameWidth, this.replyTextWidth);; i = this.replyStartX + this.backgroundDrawableRight)
      {
        if ((f1 >= this.replyStartX) && (f1 <= i) && (f2 >= this.replyStartY))
        {
          bool3 = bool2;
          if (f2 <= this.replyStartY + AndroidUtilities.dp(35.0F)) {
            break;
          }
        }
        this.replyPressed = false;
        return bool2;
      }
      bool3 = bool2;
    } while (!this.sharePressed);
    if (paramMotionEvent.getAction() == 1)
    {
      this.sharePressed = false;
      playSoundEffect(0);
      if (this.delegate != null) {
        this.delegate.didPressedShare(this);
      }
    }
    for (;;)
    {
      invalidate();
      return bool2;
      if (paramMotionEvent.getAction() == 3) {
        this.sharePressed = false;
      } else if ((paramMotionEvent.getAction() == 2) && ((f1 < this.shareStartX) || (f1 > this.shareStartX + AndroidUtilities.dp(40.0F)) || (f2 < this.shareStartY) || (f2 > this.shareStartY + AndroidUtilities.dp(32.0F)))) {
        this.sharePressed = false;
      }
    }
  }
  
  public void requestLayout()
  {
    if (this.inLayout) {
      return;
    }
    super.requestLayout();
  }
  
  public void setAllowAssistant(boolean paramBoolean)
  {
    this.allowAssistant = paramBoolean;
  }
  
  public void setCheckPressed(boolean paramBoolean1, boolean paramBoolean2)
  {
    this.isCheckPressed = paramBoolean1;
    this.isPressed = paramBoolean2;
    updateRadialProgressBackground();
    if (this.useSeekBarWaweform) {
      this.seekBarWaveform.setSelected(isDrawSelectedBackground());
    }
    for (;;)
    {
      invalidate();
      return;
      this.seekBar.setSelected(isDrawSelectedBackground());
    }
  }
  
  public void setDelegate(ChatMessageCellDelegate paramChatMessageCellDelegate)
  {
    this.delegate = paramChatMessageCellDelegate;
  }
  
  public void setFullyDraw(boolean paramBoolean)
  {
    this.fullyDraw = paramBoolean;
  }
  
  public void setHighlighted(boolean paramBoolean)
  {
    if (this.isHighlighted == paramBoolean) {
      return;
    }
    this.isHighlighted = paramBoolean;
    if (!this.isHighlighted)
    {
      this.lastHighlightProgressTime = System.currentTimeMillis();
      this.isHighlightedAnimated = true;
      this.highlightProgress = 300;
      updateRadialProgressBackground();
      if (!this.useSeekBarWaweform) {
        break label80;
      }
      this.seekBarWaveform.setSelected(isDrawSelectedBackground());
    }
    for (;;)
    {
      invalidate();
      return;
      this.isHighlightedAnimated = false;
      this.highlightProgress = 0;
      break;
      label80:
      this.seekBar.setSelected(isDrawSelectedBackground());
    }
  }
  
  public void setHighlightedAnimated()
  {
    this.isHighlightedAnimated = true;
    this.highlightProgress = 1000;
    this.lastHighlightProgressTime = System.currentTimeMillis();
    invalidate();
  }
  
  public void setHighlightedText(String paramString)
  {
    if ((this.currentMessageObject.messageOwner.message == null) || (this.currentMessageObject == null) || (this.currentMessageObject.type != 0) || (TextUtils.isEmpty(this.currentMessageObject.messageText)) || (paramString == null)) {
      if (!this.urlPathSelection.isEmpty())
      {
        this.linkSelectionBlockNum = -1;
        resetUrlPaths(true);
        invalidate();
      }
    }
    for (;;)
    {
      return;
      int k = TextUtils.indexOf(this.currentMessageObject.messageOwner.message.toLowerCase(), paramString.toLowerCase());
      if (k == -1)
      {
        if (!this.urlPathSelection.isEmpty())
        {
          this.linkSelectionBlockNum = -1;
          resetUrlPaths(true);
          invalidate();
        }
      }
      else
      {
        int j = k + paramString.length();
        int i = 0;
        while (i < this.currentMessageObject.textLayoutBlocks.size())
        {
          paramString = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(i);
          if ((k >= paramString.charactersOffset) && (k < paramString.charactersOffset + paramString.textLayout.getText().length()))
          {
            this.linkSelectionBlockNum = i;
            resetUrlPaths(true);
            for (;;)
            {
              try
              {
                Object localObject = obtainNewUrlPath(true);
                int m = paramString.textLayout.getText().length();
                ((LinkPath)localObject).setCurrentLayout(paramString.textLayout, k, 0.0F);
                paramString.textLayout.getSelectionPath(k, j - paramString.charactersOffset, (Path)localObject);
                if (j >= paramString.charactersOffset + m)
                {
                  i += 1;
                  if (i < this.currentMessageObject.textLayoutBlocks.size())
                  {
                    localObject = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(i);
                    k = ((MessageObject.TextLayoutBlock)localObject).textLayout.getText().length();
                    LinkPath localLinkPath = obtainNewUrlPath(true);
                    localLinkPath.setCurrentLayout(((MessageObject.TextLayoutBlock)localObject).textLayout, 0, ((MessageObject.TextLayoutBlock)localObject).height);
                    ((MessageObject.TextLayoutBlock)localObject).textLayout.getSelectionPath(0, j - ((MessageObject.TextLayoutBlock)localObject).charactersOffset, localLinkPath);
                    m = paramString.charactersOffset;
                    if (j >= m + k - 1) {
                      continue;
                    }
                  }
                }
              }
              catch (Exception paramString)
              {
                FileLog.e(paramString);
                continue;
              }
              invalidate();
              return;
              i += 1;
            }
          }
          i += 1;
        }
      }
    }
  }
  
  /* Error */
  public void setMessageObject(MessageObject paramMessageObject, final MessageObject.GroupedMessages paramGroupedMessages, boolean paramBoolean1, boolean paramBoolean2)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 3143	org/telegram/messenger/MessageObject:checkLayout	()Z
    //   4: ifne +23 -> 27
    //   7: aload_0
    //   8: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11: ifnull +21 -> 32
    //   14: aload_0
    //   15: getfield 3145	org/telegram/ui/Cells/ChatMessageCell:lastHeight	I
    //   18: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   21: getfield 2262	android/graphics/Point:y	I
    //   24: if_icmpeq +8 -> 32
    //   27: aload_0
    //   28: aconst_null
    //   29: putfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   32: aload_0
    //   33: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   36: ifnull +17 -> 53
    //   39: aload_0
    //   40: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   43: invokevirtual 3164	org/telegram/messenger/MessageObject:getId	()I
    //   46: aload_1
    //   47: invokevirtual 3164	org/telegram/messenger/MessageObject:getId	()I
    //   50: if_icmpeq +2734 -> 2784
    //   53: iconst_1
    //   54: istore 25
    //   56: aload_0
    //   57: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   60: aload_1
    //   61: if_acmpne +10 -> 71
    //   64: aload_1
    //   65: getfield 3273	org/telegram/messenger/MessageObject:forceUpdate	Z
    //   68: ifeq +2722 -> 2790
    //   71: iconst_1
    //   72: istore 15
    //   74: aload_0
    //   75: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   78: aload_1
    //   79: if_acmpne +2717 -> 2796
    //   82: aload_0
    //   83: invokespecial 3275	org/telegram/ui/Cells/ChatMessageCell:isUserDataChanged	()Z
    //   86: ifne +10 -> 96
    //   89: aload_0
    //   90: getfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   93: ifeq +2703 -> 2796
    //   96: iconst_1
    //   97: istore 34
    //   99: aload_2
    //   100: aload_0
    //   101: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   104: if_acmpeq +2698 -> 2802
    //   107: iconst_1
    //   108: istore 13
    //   110: iload 13
    //   112: istore 14
    //   114: iload 13
    //   116: ifne +53 -> 169
    //   119: iload 13
    //   121: istore 14
    //   123: aload_2
    //   124: ifnull +45 -> 169
    //   127: aload_2
    //   128: getfield 3278	org/telegram/messenger/MessageObject$GroupedMessages:messages	Ljava/util/ArrayList;
    //   131: invokevirtual 590	java/util/ArrayList:size	()I
    //   134: iconst_1
    //   135: if_icmple +2673 -> 2808
    //   138: aload_0
    //   139: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   142: getfield 3281	org/telegram/messenger/MessageObject$GroupedMessages:positions	Ljava/util/HashMap;
    //   145: aload_0
    //   146: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   149: invokevirtual 3284	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   152: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   155: astore 37
    //   157: aload 37
    //   159: aload_0
    //   160: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   163: if_acmpeq +2651 -> 2814
    //   166: iconst_1
    //   167: istore 14
    //   169: iload 15
    //   171: ifne +38 -> 209
    //   174: iload 34
    //   176: ifne +33 -> 209
    //   179: iload 14
    //   181: ifne +28 -> 209
    //   184: aload_0
    //   185: aload_1
    //   186: invokespecial 3286	org/telegram/ui/Cells/ChatMessageCell:isPhotoDataChanged	(Lorg/telegram/messenger/MessageObject;)Z
    //   189: ifne +20 -> 209
    //   192: aload_0
    //   193: getfield 953	org/telegram/ui/Cells/ChatMessageCell:pinnedBottom	Z
    //   196: iload_3
    //   197: if_icmpne +12 -> 209
    //   200: aload_0
    //   201: getfield 955	org/telegram/ui/Cells/ChatMessageCell:pinnedTop	Z
    //   204: iload 4
    //   206: if_icmpeq +19049 -> 19255
    //   209: aload_0
    //   210: iload_3
    //   211: putfield 953	org/telegram/ui/Cells/ChatMessageCell:pinnedBottom	Z
    //   214: aload_0
    //   215: iload 4
    //   217: putfield 955	org/telegram/ui/Cells/ChatMessageCell:pinnedTop	Z
    //   220: aload_0
    //   221: bipush -2
    //   223: putfield 3288	org/telegram/ui/Cells/ChatMessageCell:lastTime	I
    //   226: aload_0
    //   227: iconst_0
    //   228: putfield 3010	org/telegram/ui/Cells/ChatMessageCell:isHighlightedAnimated	Z
    //   231: aload_0
    //   232: iconst_m1
    //   233: putfield 1184	org/telegram/ui/Cells/ChatMessageCell:widthBeforeNewTimeLine	I
    //   236: aload_0
    //   237: aload_1
    //   238: putfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   241: aload_0
    //   242: aload_2
    //   243: putfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   246: aload_0
    //   247: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   250: ifnull +2570 -> 2820
    //   253: aload_0
    //   254: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   257: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   260: invokevirtual 590	java/util/ArrayList:size	()I
    //   263: iconst_1
    //   264: if_icmple +2556 -> 2820
    //   267: aload_0
    //   268: aload_0
    //   269: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   272: getfield 3281	org/telegram/messenger/MessageObject$GroupedMessages:positions	Ljava/util/HashMap;
    //   275: aload_0
    //   276: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   279: invokevirtual 3284	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   282: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   285: putfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   288: aload_0
    //   289: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   292: ifnonnull +8 -> 300
    //   295: aload_0
    //   296: aconst_null
    //   297: putfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   300: aload_0
    //   301: getfield 955	org/telegram/ui/Cells/ChatMessageCell:pinnedTop	Z
    //   304: ifeq +2529 -> 2833
    //   307: aload_0
    //   308: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   311: ifnull +15 -> 326
    //   314: aload_0
    //   315: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   318: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   321: iconst_4
    //   322: iand
    //   323: ifeq +2510 -> 2833
    //   326: iconst_1
    //   327: istore_3
    //   328: aload_0
    //   329: iload_3
    //   330: putfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   333: aload_0
    //   334: getfield 953	org/telegram/ui/Cells/ChatMessageCell:pinnedBottom	Z
    //   337: ifeq +2501 -> 2838
    //   340: aload_0
    //   341: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   344: ifnull +16 -> 360
    //   347: aload_0
    //   348: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   351: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   354: bipush 8
    //   356: iand
    //   357: ifeq +2481 -> 2838
    //   360: iconst_1
    //   361: istore_3
    //   362: aload_0
    //   363: iload_3
    //   364: putfield 1623	org/telegram/ui/Cells/ChatMessageCell:drawPinnedBottom	Z
    //   367: aload_0
    //   368: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   371: iconst_0
    //   372: invokevirtual 3291	org/telegram/messenger/ImageReceiver:setCrossfadeWithOldImage	(Z)V
    //   375: aload_0
    //   376: aload_1
    //   377: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   380: getfield 2383	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   383: putfield 2380	org/telegram/ui/Cells/ChatMessageCell:lastSendState	I
    //   386: aload_0
    //   387: aload_1
    //   388: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   391: getfield 1715	org/telegram/tgnet/TLRPC$Message:destroyTime	I
    //   394: putfield 2385	org/telegram/ui/Cells/ChatMessageCell:lastDeleteDate	I
    //   397: aload_0
    //   398: aload_1
    //   399: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   402: getfield 2390	org/telegram/tgnet/TLRPC$Message:views	I
    //   405: putfield 2387	org/telegram/ui/Cells/ChatMessageCell:lastViewsCount	I
    //   408: aload_0
    //   409: iconst_0
    //   410: putfield 2288	org/telegram/ui/Cells/ChatMessageCell:isPressed	Z
    //   413: aload_0
    //   414: iconst_0
    //   415: putfield 730	org/telegram/ui/Cells/ChatMessageCell:gamePreviewPressed	Z
    //   418: aload_0
    //   419: iconst_1
    //   420: putfield 344	org/telegram/ui/Cells/ChatMessageCell:isCheckPressed	Z
    //   423: aload_0
    //   424: iconst_0
    //   425: putfield 480	org/telegram/ui/Cells/ChatMessageCell:hasNewLineForTime	Z
    //   428: aload_0
    //   429: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   432: ifeq +2411 -> 2843
    //   435: aload_1
    //   436: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   439: ifne +2404 -> 2843
    //   442: aload_1
    //   443: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   446: ifeq +2397 -> 2843
    //   449: aload_0
    //   450: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   453: ifnull +13 -> 466
    //   456: aload_0
    //   457: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   460: getfield 3008	org/telegram/messenger/MessageObject$GroupedMessagePosition:edge	Z
    //   463: ifeq +2380 -> 2843
    //   466: iconst_1
    //   467: istore_3
    //   468: aload_0
    //   469: iload_3
    //   470: putfield 2278	org/telegram/ui/Cells/ChatMessageCell:isAvatarVisible	Z
    //   473: aload_0
    //   474: iconst_0
    //   475: putfield 2967	org/telegram/ui/Cells/ChatMessageCell:wasLayout	Z
    //   478: aload_0
    //   479: iconst_0
    //   480: putfield 989	org/telegram/ui/Cells/ChatMessageCell:drwaShareGoIcon	Z
    //   483: aload_0
    //   484: iconst_0
    //   485: putfield 1612	org/telegram/ui/Cells/ChatMessageCell:groupPhotoInvisible	Z
    //   488: aload_0
    //   489: aload_0
    //   490: aload_1
    //   491: invokespecial 3293	org/telegram/ui/Cells/ChatMessageCell:checkNeedDrawShareButton	(Lorg/telegram/messenger/MessageObject;)Z
    //   494: putfield 3020	org/telegram/ui/Cells/ChatMessageCell:drawShareButton	Z
    //   497: aload_0
    //   498: aconst_null
    //   499: putfield 1527	org/telegram/ui/Cells/ChatMessageCell:replyNameLayout	Landroid/text/StaticLayout;
    //   502: aload_0
    //   503: aconst_null
    //   504: putfield 2617	org/telegram/ui/Cells/ChatMessageCell:adminLayout	Landroid/text/StaticLayout;
    //   507: aload_0
    //   508: aconst_null
    //   509: putfield 2404	org/telegram/ui/Cells/ChatMessageCell:replyTextLayout	Landroid/text/StaticLayout;
    //   512: aload_0
    //   513: iconst_0
    //   514: putfield 2682	org/telegram/ui/Cells/ChatMessageCell:replyNameWidth	I
    //   517: aload_0
    //   518: iconst_0
    //   519: putfield 2686	org/telegram/ui/Cells/ChatMessageCell:replyTextWidth	I
    //   522: aload_0
    //   523: iconst_0
    //   524: putfield 2559	org/telegram/ui/Cells/ChatMessageCell:viaWidth	I
    //   527: aload_0
    //   528: iconst_0
    //   529: putfield 2585	org/telegram/ui/Cells/ChatMessageCell:viaNameWidth	I
    //   532: aload_0
    //   533: iconst_0
    //   534: putfield 2933	org/telegram/ui/Cells/ChatMessageCell:addedCaptionHeight	I
    //   537: aload_0
    //   538: aconst_null
    //   539: putfield 2419	org/telegram/ui/Cells/ChatMessageCell:currentReplyPhoto	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   542: aload_0
    //   543: aconst_null
    //   544: putfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   547: aload_0
    //   548: aconst_null
    //   549: putfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   552: aload_0
    //   553: aconst_null
    //   554: putfield 2561	org/telegram/ui/Cells/ChatMessageCell:currentViaBotUser	Lorg/telegram/tgnet/TLRPC$User;
    //   557: aload_0
    //   558: iconst_0
    //   559: putfield 2563	org/telegram/ui/Cells/ChatMessageCell:drawNameLayout	Z
    //   562: aload_0
    //   563: getfield 449	org/telegram/ui/Cells/ChatMessageCell:scheduledInvalidate	Z
    //   566: ifeq +15 -> 581
    //   569: aload_0
    //   570: getfield 361	org/telegram/ui/Cells/ChatMessageCell:invalidateRunnable	Ljava/lang/Runnable;
    //   573: invokestatic 3297	org/telegram/messenger/AndroidUtilities:cancelRunOnUIThread	(Ljava/lang/Runnable;)V
    //   576: aload_0
    //   577: iconst_0
    //   578: putfield 449	org/telegram/ui/Cells/ChatMessageCell:scheduledInvalidate	Z
    //   581: aload_0
    //   582: iconst_m1
    //   583: invokespecial 721	org/telegram/ui/Cells/ChatMessageCell:resetPressedLink	(I)V
    //   586: aload_1
    //   587: iconst_0
    //   588: putfield 3273	org/telegram/messenger/MessageObject:forceUpdate	Z
    //   591: aload_0
    //   592: iconst_0
    //   593: putfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   596: aload_0
    //   597: iconst_0
    //   598: putfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   601: aload_0
    //   602: iconst_0
    //   603: putfield 461	org/telegram/ui/Cells/ChatMessageCell:hasOldCaptionPreview	Z
    //   606: aload_0
    //   607: iconst_0
    //   608: putfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   611: aload_0
    //   612: iconst_0
    //   613: putfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   616: aload_0
    //   617: iconst_0
    //   618: putfield 812	org/telegram/ui/Cells/ChatMessageCell:instantButtonPressed	Z
    //   621: aload_0
    //   622: iconst_0
    //   623: putfield 785	org/telegram/ui/Cells/ChatMessageCell:instantPressed	Z
    //   626: getstatic 790	android/os/Build$VERSION:SDK_INT	I
    //   629: bipush 21
    //   631: if_icmplt +31 -> 662
    //   634: aload_0
    //   635: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   638: ifnull +24 -> 662
    //   641: aload_0
    //   642: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   645: iconst_0
    //   646: iconst_0
    //   647: invokevirtual 3300	android/graphics/drawable/Drawable:setVisible	(ZZ)Z
    //   650: pop
    //   651: aload_0
    //   652: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   655: getstatic 858	android/util/StateSet:NOTHING	[I
    //   658: invokevirtual 806	android/graphics/drawable/Drawable:setState	([I)Z
    //   661: pop
    //   662: aload_0
    //   663: iconst_0
    //   664: putfield 637	org/telegram/ui/Cells/ChatMessageCell:linkPreviewPressed	Z
    //   667: aload_0
    //   668: iconst_0
    //   669: putfield 558	org/telegram/ui/Cells/ChatMessageCell:buttonPressed	I
    //   672: aload_0
    //   673: iconst_0
    //   674: putfield 568	org/telegram/ui/Cells/ChatMessageCell:miniButtonPressed	I
    //   677: aload_0
    //   678: iconst_m1
    //   679: putfield 611	org/telegram/ui/Cells/ChatMessageCell:pressedBotButton	I
    //   682: aload_0
    //   683: iconst_0
    //   684: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   687: aload_0
    //   688: iconst_0
    //   689: putfield 565	org/telegram/ui/Cells/ChatMessageCell:mediaOffsetY	I
    //   692: aload_0
    //   693: iconst_0
    //   694: putfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   697: aload_0
    //   698: aconst_null
    //   699: putfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   702: aload_0
    //   703: aconst_null
    //   704: putfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   707: aload_0
    //   708: aconst_null
    //   709: putfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   712: aload_0
    //   713: aconst_null
    //   714: putfield 1672	org/telegram/ui/Cells/ChatMessageCell:videoInfoLayout	Landroid/text/StaticLayout;
    //   717: aload_0
    //   718: aconst_null
    //   719: putfield 1643	org/telegram/ui/Cells/ChatMessageCell:photosCountLayout	Landroid/text/StaticLayout;
    //   722: aload_0
    //   723: aconst_null
    //   724: putfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   727: aload_0
    //   728: aconst_null
    //   729: putfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   732: aload_0
    //   733: aconst_null
    //   734: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   737: aload_0
    //   738: iconst_0
    //   739: putfield 1777	org/telegram/ui/Cells/ChatMessageCell:captionOffsetX	I
    //   742: aload_0
    //   743: aconst_null
    //   744: putfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   747: aload_0
    //   748: aconst_null
    //   749: putfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   752: aload_0
    //   753: iconst_0
    //   754: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   757: aload_0
    //   758: aconst_null
    //   759: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   762: aload_0
    //   763: aconst_null
    //   764: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   767: aload_0
    //   768: aconst_null
    //   769: putfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   772: aload_0
    //   773: aconst_null
    //   774: putfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   777: aload_0
    //   778: iconst_0
    //   779: putfield 1399	org/telegram/ui/Cells/ChatMessageCell:cancelLoading	Z
    //   782: aload_0
    //   783: iconst_m1
    //   784: putfield 553	org/telegram/ui/Cells/ChatMessageCell:buttonState	I
    //   787: aload_0
    //   788: iconst_m1
    //   789: putfield 546	org/telegram/ui/Cells/ChatMessageCell:miniButtonState	I
    //   792: aload_0
    //   793: iconst_0
    //   794: putfield 1384	org/telegram/ui/Cells/ChatMessageCell:hasMiniProgress	I
    //   797: aload_0
    //   798: aconst_null
    //   799: putfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   802: aload_0
    //   803: iconst_0
    //   804: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   807: aload_0
    //   808: iconst_1
    //   809: putfield 346	org/telegram/ui/Cells/ChatMessageCell:drawBackground	Z
    //   812: aload_0
    //   813: iconst_0
    //   814: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   817: aload_0
    //   818: iconst_0
    //   819: putfield 509	org/telegram/ui/Cells/ChatMessageCell:useSeekBarWaweform	Z
    //   822: aload_0
    //   823: iconst_0
    //   824: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   827: aload_0
    //   828: iconst_0
    //   829: putfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   832: aload_0
    //   833: iconst_0
    //   834: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   837: aload_0
    //   838: iconst_0
    //   839: putfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   842: iconst_0
    //   843: istore 18
    //   845: iconst_0
    //   846: istore 24
    //   848: iconst_0
    //   849: istore 31
    //   851: aload_0
    //   852: iconst_0
    //   853: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   856: aload_0
    //   857: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   860: iconst_0
    //   861: invokevirtual 1402	org/telegram/messenger/ImageReceiver:setForceLoading	(Z)V
    //   864: aload_0
    //   865: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   868: iconst_0
    //   869: invokevirtual 1348	org/telegram/messenger/ImageReceiver:setNeedsQualityThumb	(Z)V
    //   872: aload_0
    //   873: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   876: iconst_0
    //   877: invokevirtual 1351	org/telegram/messenger/ImageReceiver:setShouldGenerateQualityThumb	(Z)V
    //   880: aload_0
    //   881: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   884: iconst_0
    //   885: invokevirtual 3303	org/telegram/messenger/ImageReceiver:setAllowDecodeSingleFrame	(Z)V
    //   888: aload_0
    //   889: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   892: aconst_null
    //   893: invokevirtual 1354	org/telegram/messenger/ImageReceiver:setParentMessageObject	(Lorg/telegram/messenger/MessageObject;)V
    //   896: aload_0
    //   897: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   900: ldc_w 1545
    //   903: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   906: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   909: iload 15
    //   911: ifeq +18 -> 929
    //   914: aload_0
    //   915: iconst_0
    //   916: putfield 1531	org/telegram/ui/Cells/ChatMessageCell:firstVisibleBlockNum	I
    //   919: aload_0
    //   920: iconst_0
    //   921: putfield 1533	org/telegram/ui/Cells/ChatMessageCell:lastVisibleBlockNum	I
    //   924: aload_0
    //   925: iconst_1
    //   926: putfield 1459	org/telegram/ui/Cells/ChatMessageCell:needNewVisiblePart	Z
    //   929: aload_1
    //   930: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   933: ifne +8513 -> 9446
    //   936: aload_0
    //   937: iconst_1
    //   938: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   941: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   944: ifeq +1951 -> 2895
    //   947: aload_0
    //   948: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   951: ifeq +1897 -> 2848
    //   954: aload_1
    //   955: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   958: ifne +1890 -> 2848
    //   961: aload_1
    //   962: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   965: ifeq +1883 -> 2848
    //   968: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   971: ldc_w 3304
    //   974: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   977: isub
    //   978: istore 17
    //   980: aload_0
    //   981: iconst_1
    //   982: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   985: aload_0
    //   986: iload 17
    //   988: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   991: aload_1
    //   992: invokevirtual 1416	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   995: ifeq +49 -> 1044
    //   998: aload_0
    //   999: getfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   1002: i2d
    //   1003: dstore 5
    //   1005: getstatic 1170	org/telegram/ui/ActionBar/Theme:chat_audioTimePaint	Landroid/text/TextPaint;
    //   1008: ldc_w 1172
    //   1011: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   1014: f2d
    //   1015: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1018: dstore 7
    //   1020: aload_1
    //   1021: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   1024: ifeq +1983 -> 3007
    //   1027: iconst_0
    //   1028: istore 13
    //   1030: aload_0
    //   1031: dload 5
    //   1033: dload 7
    //   1035: iload 13
    //   1037: i2d
    //   1038: dadd
    //   1039: dsub
    //   1040: d2i
    //   1041: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   1044: aload_0
    //   1045: aload_1
    //   1046: invokespecial 1191	org/telegram/ui/Cells/ChatMessageCell:measureTime	(Lorg/telegram/messenger/MessageObject;)V
    //   1049: aload_0
    //   1050: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   1053: ldc_w 1588
    //   1056: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1059: iadd
    //   1060: istore 13
    //   1062: iload 13
    //   1064: istore 26
    //   1066: aload_1
    //   1067: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   1070: ifeq +14 -> 1084
    //   1073: iload 13
    //   1075: ldc_w 2877
    //   1078: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1081: iadd
    //   1082: istore 26
    //   1084: aload_1
    //   1085: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1088: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1091: instanceof 1029
    //   1094: ifeq +1924 -> 3018
    //   1097: aload_1
    //   1098: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1101: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1104: getfield 2667	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   1107: instanceof 2669
    //   1110: ifeq +1908 -> 3018
    //   1113: iconst_1
    //   1114: istore_3
    //   1115: aload_0
    //   1116: iload_3
    //   1117: putfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   1120: aload_0
    //   1121: aload_1
    //   1122: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1125: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1128: instanceof 1031
    //   1131: putfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   1134: aload_1
    //   1135: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1138: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1141: instanceof 999
    //   1144: ifeq +1879 -> 3023
    //   1147: aload_1
    //   1148: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1151: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1154: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1157: instanceof 1001
    //   1160: ifeq +1863 -> 3023
    //   1163: iconst_1
    //   1164: istore_3
    //   1165: aload_0
    //   1166: iload_3
    //   1167: putfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1170: aload_0
    //   1171: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1174: ifeq +1854 -> 3028
    //   1177: aload_1
    //   1178: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1181: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1184: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1187: getfield 3308	org/telegram/tgnet/TLRPC$WebPage:cached_page	Lorg/telegram/tgnet/TLRPC$Page;
    //   1190: ifnull +1838 -> 3028
    //   1193: iconst_1
    //   1194: istore_3
    //   1195: aload_0
    //   1196: iload_3
    //   1197: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1200: iconst_0
    //   1201: istore 13
    //   1203: aload_0
    //   1204: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1207: ifeq +1826 -> 3033
    //   1210: aload_1
    //   1211: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1214: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1217: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1220: getfield 899	org/telegram/tgnet/TLRPC$WebPage:site_name	Ljava/lang/String;
    //   1223: astore_2
    //   1224: aload_0
    //   1225: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1228: ifeq +1810 -> 3038
    //   1231: aload_1
    //   1232: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1235: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1238: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1241: getfield 3310	org/telegram/tgnet/TLRPC$WebPage:type	Ljava/lang/String;
    //   1244: astore 37
    //   1246: aload_0
    //   1247: getfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1250: ifne +1854 -> 3104
    //   1253: ldc_w 3312
    //   1256: aload 37
    //   1258: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1261: ifeq +1783 -> 3044
    //   1264: aload_0
    //   1265: iconst_1
    //   1266: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1269: aload_0
    //   1270: iconst_1
    //   1271: putfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   1274: iload 13
    //   1276: istore 15
    //   1278: getstatic 790	android/os/Build$VERSION:SDK_INT	I
    //   1281: bipush 21
    //   1283: if_icmplt +127 -> 1410
    //   1286: aload_0
    //   1287: getfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1290: ifeq +120 -> 1410
    //   1293: aload_0
    //   1294: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   1297: ifnonnull +2105 -> 3402
    //   1300: new 1541	android/graphics/Paint
    //   1303: dup
    //   1304: iconst_1
    //   1305: invokespecial 3314	android/graphics/Paint:<init>	(I)V
    //   1308: astore_2
    //   1309: aload_2
    //   1310: iconst_m1
    //   1311: invokevirtual 1544	android/graphics/Paint:setColor	(I)V
    //   1314: new 14	org/telegram/ui/Cells/ChatMessageCell$2
    //   1317: dup
    //   1318: aload_0
    //   1319: aload_2
    //   1320: invokespecial 3317	org/telegram/ui/Cells/ChatMessageCell$2:<init>	(Lorg/telegram/ui/Cells/ChatMessageCell;Landroid/graphics/Paint;)V
    //   1323: astore 37
    //   1325: getstatic 3320	android/util/StateSet:WILD_CARD	[I
    //   1328: astore 38
    //   1330: aload_0
    //   1331: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1334: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   1337: ifeq +2058 -> 3395
    //   1340: ldc_w 1692
    //   1343: astore_2
    //   1344: aload_2
    //   1345: invokestatic 1511	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   1348: istore 13
    //   1350: aload_0
    //   1351: new 3322	android/graphics/drawable/RippleDrawable
    //   1354: dup
    //   1355: new 3324	android/content/res/ColorStateList
    //   1358: dup
    //   1359: iconst_1
    //   1360: anewarray 3325	[I
    //   1363: dup
    //   1364: iconst_0
    //   1365: aload 38
    //   1367: aastore
    //   1368: iconst_1
    //   1369: newarray <illegal type>
    //   1371: dup
    //   1372: iconst_0
    //   1373: iload 13
    //   1375: ldc_w 3326
    //   1378: iand
    //   1379: iastore
    //   1380: invokespecial 3329	android/content/res/ColorStateList:<init>	([[I[I)V
    //   1383: aconst_null
    //   1384: aload 37
    //   1386: invokespecial 3332	android/graphics/drawable/RippleDrawable:<init>	(Landroid/content/res/ColorStateList;Landroid/graphics/drawable/Drawable;Landroid/graphics/drawable/Drawable;)V
    //   1389: putfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   1392: aload_0
    //   1393: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   1396: aload_0
    //   1397: invokevirtual 3336	android/graphics/drawable/Drawable:setCallback	(Landroid/graphics/drawable/Drawable$Callback;)V
    //   1400: aload_0
    //   1401: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   1404: iconst_1
    //   1405: iconst_0
    //   1406: invokevirtual 3300	android/graphics/drawable/Drawable:setVisible	(ZZ)Z
    //   1409: pop
    //   1410: aload_0
    //   1411: iload 17
    //   1413: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1416: aload_0
    //   1417: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1420: ifne +29 -> 1449
    //   1423: aload_0
    //   1424: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   1427: ifne +22 -> 1449
    //   1430: aload_0
    //   1431: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   1434: ifne +15 -> 1449
    //   1437: iload 17
    //   1439: aload_1
    //   1440: getfield 472	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   1443: isub
    //   1444: iload 26
    //   1446: if_icmpge +2000 -> 3446
    //   1449: aload_0
    //   1450: aload_0
    //   1451: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1454: aload_1
    //   1455: getfield 472	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   1458: invokestatic 486	java/lang/Math:max	(II)I
    //   1461: ldc_w 487
    //   1464: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1467: iadd
    //   1468: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1471: aload_0
    //   1472: aload_0
    //   1473: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1476: aload_0
    //   1477: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   1480: ldc_w 487
    //   1483: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1486: iadd
    //   1487: invokestatic 486	java/lang/Math:max	(II)I
    //   1490: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1493: aload_0
    //   1494: aload_0
    //   1495: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1498: ldc_w 487
    //   1501: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1504: isub
    //   1505: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   1508: aload_1
    //   1509: invokevirtual 1416	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   1512: ifeq +49 -> 1561
    //   1515: aload_0
    //   1516: getfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   1519: i2d
    //   1520: dstore 5
    //   1522: getstatic 1170	org/telegram/ui/ActionBar/Theme:chat_audioTimePaint	Landroid/text/TextPaint;
    //   1525: ldc_w 1172
    //   1528: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   1531: f2d
    //   1532: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   1535: dstore 7
    //   1537: aload_1
    //   1538: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   1541: ifeq +1980 -> 3521
    //   1544: iconst_0
    //   1545: istore 13
    //   1547: aload_0
    //   1548: dload 5
    //   1550: dload 7
    //   1552: iload 13
    //   1554: i2d
    //   1555: dadd
    //   1556: dsub
    //   1557: d2i
    //   1558: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   1561: aload_0
    //   1562: aload_1
    //   1563: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   1566: aload_1
    //   1567: getfield 1119	org/telegram/messenger/MessageObject:textWidth	I
    //   1570: istore 14
    //   1572: aload_0
    //   1573: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   1576: ifne +10 -> 1586
    //   1579: aload_0
    //   1580: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   1583: ifeq +1949 -> 3532
    //   1586: ldc_w 587
    //   1589: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1592: istore 13
    //   1594: aload_0
    //   1595: iload 13
    //   1597: iload 14
    //   1599: iadd
    //   1600: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1603: aload_0
    //   1604: aload_1
    //   1605: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   1608: ldc_w 3339
    //   1611: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1614: iadd
    //   1615: aload_0
    //   1616: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1619: iadd
    //   1620: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   1623: aload_0
    //   1624: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   1627: ifeq +16 -> 1643
    //   1630: aload_0
    //   1631: aload_0
    //   1632: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1635: fconst_1
    //   1636: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1639: isub
    //   1640: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   1643: aload_0
    //   1644: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   1647: aload_0
    //   1648: getfield 2567	org/telegram/ui/Cells/ChatMessageCell:nameWidth	I
    //   1651: invokestatic 486	java/lang/Math:max	(II)I
    //   1654: aload_0
    //   1655: getfield 2623	org/telegram/ui/Cells/ChatMessageCell:forwardedNameWidth	I
    //   1658: invokestatic 486	java/lang/Math:max	(II)I
    //   1661: aload_0
    //   1662: getfield 2682	org/telegram/ui/Cells/ChatMessageCell:replyNameWidth	I
    //   1665: invokestatic 486	java/lang/Math:max	(II)I
    //   1668: aload_0
    //   1669: getfield 2686	org/telegram/ui/Cells/ChatMessageCell:replyTextWidth	I
    //   1672: invokestatic 486	java/lang/Math:max	(II)I
    //   1675: istore 16
    //   1677: iconst_0
    //   1678: istore 19
    //   1680: aload_0
    //   1681: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1684: ifne +17 -> 1701
    //   1687: aload_0
    //   1688: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   1691: ifne +10 -> 1701
    //   1694: aload_0
    //   1695: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   1698: ifeq +7720 -> 9418
    //   1701: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   1704: ifeq +1849 -> 3553
    //   1707: aload_0
    //   1708: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   1711: ifeq +1827 -> 3538
    //   1714: aload_1
    //   1715: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   1718: ifeq +1820 -> 3538
    //   1721: aload_0
    //   1722: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1725: invokevirtual 1027	org/telegram/messenger/MessageObject:isOut	()Z
    //   1728: ifne +1810 -> 3538
    //   1731: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   1734: ldc_w 3340
    //   1737: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1740: isub
    //   1741: istore 13
    //   1743: iload 13
    //   1745: istore 14
    //   1747: aload_0
    //   1748: getfield 3020	org/telegram/ui/Cells/ChatMessageCell:drawShareButton	Z
    //   1751: ifeq +14 -> 1765
    //   1754: iload 13
    //   1756: ldc_w 1077
    //   1759: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1762: isub
    //   1763: istore 14
    //   1765: aload_0
    //   1766: getfield 459	org/telegram/ui/Cells/ChatMessageCell:hasLinkPreview	Z
    //   1769: ifeq +1873 -> 3642
    //   1772: aload_1
    //   1773: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1776: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   1779: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   1782: checkcast 1001	org/telegram/tgnet/TLRPC$TL_webPage
    //   1785: astore_2
    //   1786: aload_2
    //   1787: getfield 3341	org/telegram/tgnet/TLRPC$TL_webPage:site_name	Ljava/lang/String;
    //   1790: astore 39
    //   1792: aload_2
    //   1793: getfield 3342	org/telegram/tgnet/TLRPC$TL_webPage:title	Ljava/lang/String;
    //   1796: astore 43
    //   1798: aload_2
    //   1799: getfield 3345	org/telegram/tgnet/TLRPC$TL_webPage:author	Ljava/lang/String;
    //   1802: astore 42
    //   1804: aload_2
    //   1805: getfield 3346	org/telegram/tgnet/TLRPC$TL_webPage:description	Ljava/lang/String;
    //   1808: astore 40
    //   1810: aload_2
    //   1811: getfield 3349	org/telegram/tgnet/TLRPC$TL_webPage:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   1814: astore 41
    //   1816: aload_2
    //   1817: getfield 3350	org/telegram/tgnet/TLRPC$TL_webPage:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   1820: astore 38
    //   1822: aload_2
    //   1823: getfield 3351	org/telegram/tgnet/TLRPC$TL_webPage:type	Ljava/lang/String;
    //   1826: astore 37
    //   1828: aload_2
    //   1829: getfield 3352	org/telegram/tgnet/TLRPC$TL_webPage:duration	I
    //   1832: istore 21
    //   1834: iload 14
    //   1836: istore 13
    //   1838: aload 39
    //   1840: ifnull +50 -> 1890
    //   1843: iload 14
    //   1845: istore 13
    //   1847: aload 41
    //   1849: ifnull +41 -> 1890
    //   1852: iload 14
    //   1854: istore 13
    //   1856: aload 39
    //   1858: invokevirtual 1276	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   1861: ldc_w 3354
    //   1864: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1867: ifeq +23 -> 1890
    //   1870: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   1873: getfield 2262	android/graphics/Point:y	I
    //   1876: iconst_3
    //   1877: idiv
    //   1878: aload_0
    //   1879: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   1882: getfield 1119	org/telegram/messenger/MessageObject:textWidth	I
    //   1885: invokestatic 486	java/lang/Math:max	(II)I
    //   1888: istore 13
    //   1890: iload 15
    //   1892: ifne +1739 -> 3631
    //   1895: aload_0
    //   1896: getfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1899: ifne +1732 -> 3631
    //   1902: aload 38
    //   1904: ifnonnull +1727 -> 3631
    //   1907: aload 37
    //   1909: ifnull +1722 -> 3631
    //   1912: aload 37
    //   1914: ldc_w 3356
    //   1917: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1920: ifne +25 -> 1945
    //   1923: aload 37
    //   1925: ldc_w 3358
    //   1928: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1931: ifne +14 -> 1945
    //   1934: aload 37
    //   1936: ldc_w 3360
    //   1939: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1942: ifeq +1689 -> 3631
    //   1945: iconst_1
    //   1946: istore 18
    //   1948: iload 15
    //   1950: ifne +1687 -> 3637
    //   1953: aload_0
    //   1954: getfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   1957: ifne +1680 -> 3637
    //   1960: aload 38
    //   1962: ifnonnull +1675 -> 3637
    //   1965: aload 40
    //   1967: ifnull +1670 -> 3637
    //   1970: aload 37
    //   1972: ifnull +1665 -> 3637
    //   1975: aload 37
    //   1977: ldc_w 3356
    //   1980: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1983: ifne +25 -> 2008
    //   1986: aload 37
    //   1988: ldc_w 3358
    //   1991: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   1994: ifne +14 -> 2008
    //   1997: aload 37
    //   1999: ldc_w 3360
    //   2002: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   2005: ifeq +1632 -> 3637
    //   2008: aload_0
    //   2009: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2012: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   2015: ifnull +1622 -> 3637
    //   2018: iconst_1
    //   2019: istore_3
    //   2020: aload_0
    //   2021: iload_3
    //   2022: putfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   2025: aconst_null
    //   2026: astore_2
    //   2027: aload_0
    //   2028: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   2031: ifeq +1784 -> 3815
    //   2034: iconst_0
    //   2035: istore 27
    //   2037: iconst_3
    //   2038: istore 20
    //   2040: iconst_0
    //   2041: istore 14
    //   2043: iconst_0
    //   2044: istore 22
    //   2046: iload 13
    //   2048: iload 27
    //   2050: isub
    //   2051: istore 32
    //   2053: aload_0
    //   2054: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2057: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   2060: ifnonnull +16 -> 2076
    //   2063: aload 41
    //   2065: ifnull +11 -> 2076
    //   2068: aload_0
    //   2069: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   2072: iconst_1
    //   2073: invokevirtual 3363	org/telegram/messenger/MessageObject:generateThumbs	(Z)V
    //   2076: iload 16
    //   2078: istore 13
    //   2080: iload 19
    //   2082: istore 15
    //   2084: aload 39
    //   2086: ifnull +249 -> 2335
    //   2089: iload 14
    //   2091: istore 22
    //   2093: iload 16
    //   2095: istore 13
    //   2097: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   2100: aload 39
    //   2102: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   2105: fconst_1
    //   2106: fadd
    //   2107: f2d
    //   2108: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   2111: d2i
    //   2112: istore 15
    //   2114: iload 14
    //   2116: istore 22
    //   2118: iload 16
    //   2120: istore 13
    //   2122: aload_0
    //   2123: new 350	android/text/StaticLayout
    //   2126: dup
    //   2127: aload 39
    //   2129: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   2132: iload 15
    //   2134: iload 32
    //   2136: invokestatic 1195	java/lang/Math:min	(II)I
    //   2139: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   2142: fconst_1
    //   2143: fconst_0
    //   2144: iconst_0
    //   2145: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   2148: putfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   2151: iload 14
    //   2153: istore 22
    //   2155: iload 16
    //   2157: istore 13
    //   2159: aload_0
    //   2160: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   2163: iconst_0
    //   2164: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   2167: fconst_0
    //   2168: fcmpl
    //   2169: ifeq +1657 -> 3826
    //   2172: iconst_1
    //   2173: istore_3
    //   2174: iload 14
    //   2176: istore 22
    //   2178: iload 16
    //   2180: istore 13
    //   2182: aload_0
    //   2183: iload_3
    //   2184: putfield 1562	org/telegram/ui/Cells/ChatMessageCell:siteNameRtl	Z
    //   2187: iload 14
    //   2189: istore 22
    //   2191: iload 16
    //   2193: istore 13
    //   2195: aload_0
    //   2196: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   2199: aload_0
    //   2200: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   2203: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   2206: iconst_1
    //   2207: isub
    //   2208: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   2211: istore 15
    //   2213: iload 14
    //   2215: istore 22
    //   2217: iload 16
    //   2219: istore 13
    //   2221: aload_0
    //   2222: aload_0
    //   2223: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2226: iload 15
    //   2228: iadd
    //   2229: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2232: iload 14
    //   2234: istore 22
    //   2236: iload 16
    //   2238: istore 13
    //   2240: aload_0
    //   2241: aload_0
    //   2242: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2245: iload 15
    //   2247: iadd
    //   2248: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2251: iconst_0
    //   2252: iload 15
    //   2254: iadd
    //   2255: istore 14
    //   2257: iload 14
    //   2259: istore 22
    //   2261: iload 16
    //   2263: istore 13
    //   2265: aload_0
    //   2266: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   2269: invokevirtual 3366	android/text/StaticLayout:getWidth	()I
    //   2272: istore 15
    //   2274: iload 14
    //   2276: istore 22
    //   2278: iload 16
    //   2280: istore 13
    //   2282: aload_0
    //   2283: iload 15
    //   2285: putfield 1564	org/telegram/ui/Cells/ChatMessageCell:siteNameWidth	I
    //   2288: iload 14
    //   2290: istore 22
    //   2292: iload 16
    //   2294: istore 13
    //   2296: iload 16
    //   2298: iload 15
    //   2300: iload 27
    //   2302: iadd
    //   2303: invokestatic 486	java/lang/Math:max	(II)I
    //   2306: istore 16
    //   2308: iload 14
    //   2310: istore 22
    //   2312: iload 16
    //   2314: istore 13
    //   2316: iconst_0
    //   2317: iload 15
    //   2319: iload 27
    //   2321: iadd
    //   2322: invokestatic 486	java/lang/Math:max	(II)I
    //   2325: istore 15
    //   2327: iload 16
    //   2329: istore 13
    //   2331: iload 14
    //   2333: istore 22
    //   2335: iconst_0
    //   2336: istore 29
    //   2338: iconst_0
    //   2339: istore 23
    //   2341: iconst_0
    //   2342: istore 28
    //   2344: iconst_0
    //   2345: istore 24
    //   2347: iload 13
    //   2349: istore 14
    //   2351: iload 15
    //   2353: istore 19
    //   2355: iload 20
    //   2357: istore 16
    //   2359: iload 28
    //   2361: istore 20
    //   2363: aload 43
    //   2365: ifnull +1586 -> 3951
    //   2368: aload_0
    //   2369: ldc_w 3367
    //   2372: putfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   2375: aload_0
    //   2376: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2379: ifeq +29 -> 2408
    //   2382: aload_0
    //   2383: aload_0
    //   2384: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2387: fconst_2
    //   2388: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2391: iadd
    //   2392: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2395: aload_0
    //   2396: aload_0
    //   2397: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2400: fconst_2
    //   2401: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2404: iadd
    //   2405: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2408: iconst_0
    //   2409: istore 28
    //   2411: aload_0
    //   2412: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   2415: ifeq +8 -> 2423
    //   2418: aload 40
    //   2420: ifnonnull +1425 -> 3845
    //   2423: aload_0
    //   2424: aload 43
    //   2426: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   2429: iload 32
    //   2431: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   2434: fconst_1
    //   2435: fconst_1
    //   2436: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2439: i2f
    //   2440: iconst_0
    //   2441: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   2444: iload 32
    //   2446: iconst_4
    //   2447: invokestatic 1315	org/telegram/ui/Components/StaticLayoutEx:createStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZLandroid/text/TextUtils$TruncateAt;II)Landroid/text/StaticLayout;
    //   2450: putfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2453: iconst_3
    //   2454: istore 14
    //   2456: iload 13
    //   2458: istore 16
    //   2460: iload 15
    //   2462: istore 23
    //   2464: iload 29
    //   2466: istore 19
    //   2468: aload_0
    //   2469: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2472: aload_0
    //   2473: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2476: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   2479: iconst_1
    //   2480: isub
    //   2481: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   2484: istore 20
    //   2486: iload 13
    //   2488: istore 16
    //   2490: iload 15
    //   2492: istore 23
    //   2494: iload 29
    //   2496: istore 19
    //   2498: aload_0
    //   2499: aload_0
    //   2500: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2503: iload 20
    //   2505: iadd
    //   2506: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   2509: iload 13
    //   2511: istore 16
    //   2513: iload 15
    //   2515: istore 23
    //   2517: iload 29
    //   2519: istore 19
    //   2521: aload_0
    //   2522: aload_0
    //   2523: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2526: iload 20
    //   2528: iadd
    //   2529: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   2532: iconst_0
    //   2533: istore 29
    //   2535: iload 24
    //   2537: istore 20
    //   2539: iload 13
    //   2541: istore 16
    //   2543: iload 15
    //   2545: istore 23
    //   2547: iload 20
    //   2549: istore 19
    //   2551: iload 29
    //   2553: aload_0
    //   2554: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2557: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   2560: if_icmpge +1973 -> 4533
    //   2563: iload 13
    //   2565: istore 16
    //   2567: iload 15
    //   2569: istore 23
    //   2571: iload 20
    //   2573: istore 19
    //   2575: aload_0
    //   2576: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2579: iload 29
    //   2581: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   2584: f2i
    //   2585: istore 33
    //   2587: iload 33
    //   2589: ifeq +6 -> 2595
    //   2592: iconst_1
    //   2593: istore 20
    //   2595: iload 13
    //   2597: istore 16
    //   2599: iload 15
    //   2601: istore 23
    //   2603: iload 20
    //   2605: istore 19
    //   2607: aload_0
    //   2608: getfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   2611: ldc_w 3367
    //   2614: if_icmpne +1277 -> 3891
    //   2617: iload 13
    //   2619: istore 16
    //   2621: iload 15
    //   2623: istore 23
    //   2625: iload 20
    //   2627: istore 19
    //   2629: aload_0
    //   2630: iload 33
    //   2632: ineg
    //   2633: putfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   2636: iload 33
    //   2638: ifeq +1860 -> 4498
    //   2641: iload 13
    //   2643: istore 16
    //   2645: iload 15
    //   2647: istore 23
    //   2649: iload 20
    //   2651: istore 19
    //   2653: aload_0
    //   2654: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   2657: invokevirtual 3366	android/text/StaticLayout:getWidth	()I
    //   2660: iload 33
    //   2662: isub
    //   2663: istore 24
    //   2665: iload 29
    //   2667: iload 28
    //   2669: if_icmplt +35 -> 2704
    //   2672: iload 24
    //   2674: istore 30
    //   2676: iload 33
    //   2678: ifeq +49 -> 2727
    //   2681: iload 24
    //   2683: istore 30
    //   2685: iload 13
    //   2687: istore 16
    //   2689: iload 15
    //   2691: istore 23
    //   2693: iload 20
    //   2695: istore 19
    //   2697: aload_0
    //   2698: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   2701: ifeq +26 -> 2727
    //   2704: iload 13
    //   2706: istore 16
    //   2708: iload 15
    //   2710: istore 23
    //   2712: iload 20
    //   2714: istore 19
    //   2716: iload 24
    //   2718: ldc_w 2004
    //   2721: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2724: iadd
    //   2725: istore 30
    //   2727: iload 13
    //   2729: istore 16
    //   2731: iload 15
    //   2733: istore 23
    //   2735: iload 20
    //   2737: istore 19
    //   2739: iload 13
    //   2741: iload 30
    //   2743: iload 27
    //   2745: iadd
    //   2746: invokestatic 486	java/lang/Math:max	(II)I
    //   2749: istore 13
    //   2751: iload 13
    //   2753: istore 16
    //   2755: iload 15
    //   2757: istore 23
    //   2759: iload 20
    //   2761: istore 19
    //   2763: iload 15
    //   2765: iload 30
    //   2767: iload 27
    //   2769: iadd
    //   2770: invokestatic 486	java/lang/Math:max	(II)I
    //   2773: istore 15
    //   2775: iload 29
    //   2777: iconst_1
    //   2778: iadd
    //   2779: istore 29
    //   2781: goto -242 -> 2539
    //   2784: iconst_0
    //   2785: istore 25
    //   2787: goto -2731 -> 56
    //   2790: iconst_0
    //   2791: istore 15
    //   2793: goto -2719 -> 74
    //   2796: iconst_0
    //   2797: istore 34
    //   2799: goto -2700 -> 99
    //   2802: iconst_0
    //   2803: istore 13
    //   2805: goto -2695 -> 110
    //   2808: aconst_null
    //   2809: astore 37
    //   2811: goto -2654 -> 157
    //   2814: iconst_0
    //   2815: istore 14
    //   2817: goto -2648 -> 169
    //   2820: aload_0
    //   2821: aconst_null
    //   2822: putfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   2825: aload_0
    //   2826: aconst_null
    //   2827: putfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   2830: goto -2530 -> 300
    //   2833: iconst_0
    //   2834: istore_3
    //   2835: goto -2507 -> 328
    //   2838: iconst_0
    //   2839: istore_3
    //   2840: goto -2478 -> 362
    //   2843: iconst_0
    //   2844: istore_3
    //   2845: goto -2377 -> 468
    //   2848: aload_1
    //   2849: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2852: getfield 1037	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   2855: getfield 1040	org/telegram/tgnet/TLRPC$Peer:channel_id	I
    //   2858: ifeq +32 -> 2890
    //   2861: aload_1
    //   2862: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   2865: ifne +25 -> 2890
    //   2868: iconst_1
    //   2869: istore_3
    //   2870: aload_0
    //   2871: iload_3
    //   2872: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   2875: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   2878: ldc_w 3368
    //   2881: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2884: isub
    //   2885: istore 17
    //   2887: goto -1902 -> 985
    //   2890: iconst_0
    //   2891: istore_3
    //   2892: goto -22 -> 2870
    //   2895: aload_0
    //   2896: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   2899: ifeq +49 -> 2948
    //   2902: aload_1
    //   2903: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   2906: ifne +42 -> 2948
    //   2909: aload_1
    //   2910: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   2913: ifeq +35 -> 2948
    //   2916: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   2919: getfield 2253	android/graphics/Point:x	I
    //   2922: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   2925: getfield 2262	android/graphics/Point:y	I
    //   2928: invokestatic 1195	java/lang/Math:min	(II)I
    //   2931: ldc_w 3304
    //   2934: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2937: isub
    //   2938: istore 17
    //   2940: aload_0
    //   2941: iconst_1
    //   2942: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   2945: goto -1960 -> 985
    //   2948: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   2951: getfield 2253	android/graphics/Point:x	I
    //   2954: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   2957: getfield 2262	android/graphics/Point:y	I
    //   2960: invokestatic 1195	java/lang/Math:min	(II)I
    //   2963: ldc_w 3368
    //   2966: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2969: isub
    //   2970: istore 17
    //   2972: aload_1
    //   2973: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   2976: getfield 1037	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   2979: getfield 1040	org/telegram/tgnet/TLRPC$Peer:channel_id	I
    //   2982: ifeq +20 -> 3002
    //   2985: aload_1
    //   2986: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   2989: ifne +13 -> 3002
    //   2992: iconst_1
    //   2993: istore_3
    //   2994: aload_0
    //   2995: iload_3
    //   2996: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   2999: goto -2014 -> 985
    //   3002: iconst_0
    //   3003: istore_3
    //   3004: goto -10 -> 2994
    //   3007: ldc_w 3369
    //   3010: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3013: istore 13
    //   3015: goto -1985 -> 1030
    //   3018: iconst_0
    //   3019: istore_3
    //   3020: goto -1905 -> 1115
    //   3023: iconst_0
    //   3024: istore_3
    //   3025: goto -1860 -> 1165
    //   3028: iconst_0
    //   3029: istore_3
    //   3030: goto -1835 -> 1195
    //   3033: aconst_null
    //   3034: astore_2
    //   3035: goto -1811 -> 1224
    //   3038: aconst_null
    //   3039: astore 37
    //   3041: goto -1795 -> 1246
    //   3044: ldc_w 3371
    //   3047: aload 37
    //   3049: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3052: ifeq +20 -> 3072
    //   3055: aload_0
    //   3056: iconst_1
    //   3057: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   3060: aload_0
    //   3061: iconst_2
    //   3062: putfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   3065: iload 13
    //   3067: istore 15
    //   3069: goto -1791 -> 1278
    //   3072: iload 13
    //   3074: istore 15
    //   3076: ldc_w 3373
    //   3079: aload 37
    //   3081: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3084: ifeq -1806 -> 1278
    //   3087: aload_0
    //   3088: iconst_1
    //   3089: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   3092: aload_0
    //   3093: iconst_3
    //   3094: putfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   3097: iload 13
    //   3099: istore 15
    //   3101: goto -1823 -> 1278
    //   3104: iload 13
    //   3106: istore 15
    //   3108: aload_2
    //   3109: ifnull -1831 -> 1278
    //   3112: aload_2
    //   3113: invokevirtual 1276	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   3116: astore_2
    //   3117: aload_2
    //   3118: ldc_w 3354
    //   3121: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3124: ifne +28 -> 3152
    //   3127: aload_2
    //   3128: ldc_w 3375
    //   3131: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3134: ifne +18 -> 3152
    //   3137: iload 13
    //   3139: istore 15
    //   3141: ldc_w 3377
    //   3144: aload 37
    //   3146: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   3149: ifeq -1871 -> 1278
    //   3152: iload 13
    //   3154: istore 15
    //   3156: aload_1
    //   3157: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3160: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3163: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   3166: getfield 3308	org/telegram/tgnet/TLRPC$WebPage:cached_page	Lorg/telegram/tgnet/TLRPC$Page;
    //   3169: instanceof 3379
    //   3172: ifeq -1894 -> 1278
    //   3175: aload_1
    //   3176: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3179: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3182: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   3185: getfield 3380	org/telegram/tgnet/TLRPC$WebPage:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   3188: instanceof 3382
    //   3191: ifne +26 -> 3217
    //   3194: iload 13
    //   3196: istore 15
    //   3198: aload_1
    //   3199: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3202: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3205: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   3208: getfield 1147	org/telegram/tgnet/TLRPC$WebPage:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   3211: invokestatic 1250	org/telegram/messenger/MessageObject:isVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   3214: ifeq -1936 -> 1278
    //   3217: aload_0
    //   3218: iconst_0
    //   3219: putfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   3222: iconst_1
    //   3223: istore 15
    //   3225: aload_1
    //   3226: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3229: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3232: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   3235: getfield 3308	org/telegram/tgnet/TLRPC$WebPage:cached_page	Lorg/telegram/tgnet/TLRPC$Page;
    //   3238: getfield 3387	org/telegram/tgnet/TLRPC$Page:blocks	Ljava/util/ArrayList;
    //   3241: astore_2
    //   3242: iconst_1
    //   3243: istore 13
    //   3245: iconst_0
    //   3246: istore 14
    //   3248: iload 14
    //   3250: aload_2
    //   3251: invokevirtual 590	java/util/ArrayList:size	()I
    //   3254: if_icmpge +68 -> 3322
    //   3257: aload_2
    //   3258: iload 14
    //   3260: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   3263: checkcast 3389	org/telegram/tgnet/TLRPC$PageBlock
    //   3266: astore 37
    //   3268: aload 37
    //   3270: instanceof 3391
    //   3273: ifeq +25 -> 3298
    //   3276: aload 37
    //   3278: checkcast 3391	org/telegram/tgnet/TLRPC$TL_pageBlockSlideshow
    //   3281: getfield 3394	org/telegram/tgnet/TLRPC$TL_pageBlockSlideshow:items	Ljava/util/ArrayList;
    //   3284: invokevirtual 590	java/util/ArrayList:size	()I
    //   3287: istore 13
    //   3289: iload 14
    //   3291: iconst_1
    //   3292: iadd
    //   3293: istore 14
    //   3295: goto -47 -> 3248
    //   3298: aload 37
    //   3300: instanceof 3396
    //   3303: ifeq -14 -> 3289
    //   3306: aload 37
    //   3308: checkcast 3396	org/telegram/tgnet/TLRPC$TL_pageBlockCollage
    //   3311: getfield 3397	org/telegram/tgnet/TLRPC$TL_pageBlockCollage:items	Ljava/util/ArrayList;
    //   3314: invokevirtual 590	java/util/ArrayList:size	()I
    //   3317: istore 13
    //   3319: goto -30 -> 3289
    //   3322: ldc_w 3399
    //   3325: ldc_w 3400
    //   3328: iconst_2
    //   3329: anewarray 1242	java/lang/Object
    //   3332: dup
    //   3333: iconst_0
    //   3334: iconst_1
    //   3335: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3338: aastore
    //   3339: dup
    //   3340: iconst_1
    //   3341: iload 13
    //   3343: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3346: aastore
    //   3347: invokestatic 3404	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   3350: astore_2
    //   3351: aload_0
    //   3352: getstatic 1664	org/telegram/ui/ActionBar/Theme:chat_durationPaint	Landroid/text/TextPaint;
    //   3355: aload_2
    //   3356: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   3359: f2d
    //   3360: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   3363: d2i
    //   3364: putfield 1645	org/telegram/ui/Cells/ChatMessageCell:photosCountWidth	I
    //   3367: aload_0
    //   3368: new 350	android/text/StaticLayout
    //   3371: dup
    //   3372: aload_2
    //   3373: getstatic 1664	org/telegram/ui/ActionBar/Theme:chat_durationPaint	Landroid/text/TextPaint;
    //   3376: aload_0
    //   3377: getfield 1645	org/telegram/ui/Cells/ChatMessageCell:photosCountWidth	I
    //   3380: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   3383: fconst_1
    //   3384: fconst_0
    //   3385: iconst_0
    //   3386: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   3389: putfield 1643	org/telegram/ui/Cells/ChatMessageCell:photosCountLayout	Landroid/text/StaticLayout;
    //   3392: goto -2114 -> 1278
    //   3395: ldc_w 1837
    //   3398: astore_2
    //   3399: goto -2055 -> 1344
    //   3402: aload_0
    //   3403: getfield 792	org/telegram/ui/Cells/ChatMessageCell:instantViewSelectorDrawable	Landroid/graphics/drawable/Drawable;
    //   3406: astore 37
    //   3408: aload_0
    //   3409: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   3412: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   3415: ifeq +24 -> 3439
    //   3418: ldc_w 1692
    //   3421: astore_2
    //   3422: aload 37
    //   3424: aload_2
    //   3425: invokestatic 1511	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   3428: ldc_w 3326
    //   3431: iand
    //   3432: iconst_1
    //   3433: invokestatic 3408	org/telegram/ui/ActionBar/Theme:setSelectorDrawableColor	(Landroid/graphics/drawable/Drawable;IZ)V
    //   3436: goto -2036 -> 1400
    //   3439: ldc_w 1837
    //   3442: astore_2
    //   3443: goto -21 -> 3422
    //   3446: aload_0
    //   3447: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   3450: aload_1
    //   3451: getfield 472	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   3454: isub
    //   3455: istore 13
    //   3457: iload 13
    //   3459: iflt +34 -> 3493
    //   3462: iload 13
    //   3464: iload 26
    //   3466: if_icmpgt +27 -> 3493
    //   3469: aload_0
    //   3470: aload_0
    //   3471: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   3474: iload 26
    //   3476: iadd
    //   3477: iload 13
    //   3479: isub
    //   3480: ldc_w 487
    //   3483: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3486: iadd
    //   3487: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   3490: goto -1997 -> 1493
    //   3493: aload_0
    //   3494: aload_0
    //   3495: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   3498: aload_1
    //   3499: getfield 472	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   3502: iload 26
    //   3504: iadd
    //   3505: invokestatic 486	java/lang/Math:max	(II)I
    //   3508: ldc_w 487
    //   3511: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3514: iadd
    //   3515: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   3518: goto -2025 -> 1493
    //   3521: ldc_w 3369
    //   3524: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3527: istore 13
    //   3529: goto -1982 -> 1547
    //   3532: iconst_0
    //   3533: istore 13
    //   3535: goto -1941 -> 1594
    //   3538: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   3541: ldc_w 3368
    //   3544: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3547: isub
    //   3548: istore 13
    //   3550: goto -1807 -> 1743
    //   3553: aload_0
    //   3554: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   3557: ifeq +47 -> 3604
    //   3560: aload_1
    //   3561: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   3564: ifeq +40 -> 3604
    //   3567: aload_0
    //   3568: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   3571: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   3574: ifne +30 -> 3604
    //   3577: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   3580: getfield 2253	android/graphics/Point:x	I
    //   3583: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   3586: getfield 2262	android/graphics/Point:y	I
    //   3589: invokestatic 1195	java/lang/Math:min	(II)I
    //   3592: ldc_w 3340
    //   3595: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3598: isub
    //   3599: istore 13
    //   3601: goto -1858 -> 1743
    //   3604: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   3607: getfield 2253	android/graphics/Point:x	I
    //   3610: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   3613: getfield 2262	android/graphics/Point:y	I
    //   3616: invokestatic 1195	java/lang/Math:min	(II)I
    //   3619: ldc_w 3368
    //   3622: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3625: isub
    //   3626: istore 13
    //   3628: goto -1885 -> 1743
    //   3631: iconst_0
    //   3632: istore 18
    //   3634: goto -1686 -> 1948
    //   3637: iconst_0
    //   3638: istore_3
    //   3639: goto -1619 -> 2020
    //   3642: aload_0
    //   3643: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   3646: ifeq +87 -> 3733
    //   3649: aload_1
    //   3650: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3653: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3656: checkcast 1031	org/telegram/tgnet/TLRPC$TL_messageMediaInvoice
    //   3659: astore_2
    //   3660: aload_1
    //   3661: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3664: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3667: getfield 2358	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   3670: astore 39
    //   3672: aconst_null
    //   3673: astore 43
    //   3675: aconst_null
    //   3676: astore 40
    //   3678: aconst_null
    //   3679: astore 41
    //   3681: aconst_null
    //   3682: astore 42
    //   3684: aconst_null
    //   3685: astore 38
    //   3687: aload_2
    //   3688: getfield 3411	org/telegram/tgnet/TLRPC$TL_messageMediaInvoice:photo	Lorg/telegram/tgnet/TLRPC$WebDocument;
    //   3691: instanceof 3413
    //   3694: ifeq +34 -> 3728
    //   3697: aload_2
    //   3698: getfield 3411	org/telegram/tgnet/TLRPC$TL_messageMediaInvoice:photo	Lorg/telegram/tgnet/TLRPC$WebDocument;
    //   3701: checkcast 3413	org/telegram/tgnet/TLRPC$TL_webDocument
    //   3704: astore_2
    //   3705: iconst_0
    //   3706: istore 21
    //   3708: ldc_w 3415
    //   3711: astore 37
    //   3713: aload_0
    //   3714: iconst_0
    //   3715: putfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   3718: iconst_0
    //   3719: istore 18
    //   3721: iload 14
    //   3723: istore 13
    //   3725: goto -1698 -> 2027
    //   3728: aconst_null
    //   3729: astore_2
    //   3730: goto -25 -> 3705
    //   3733: aload_1
    //   3734: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3737: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3740: getfield 2667	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   3743: astore_2
    //   3744: aload_2
    //   3745: getfield 2670	org/telegram/tgnet/TLRPC$TL_game:title	Ljava/lang/String;
    //   3748: astore 39
    //   3750: aconst_null
    //   3751: astore 43
    //   3753: aload_1
    //   3754: getfield 1114	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   3757: invokestatic 847	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   3760: ifeq +49 -> 3809
    //   3763: aload_2
    //   3764: getfield 3416	org/telegram/tgnet/TLRPC$TL_game:description	Ljava/lang/String;
    //   3767: astore 40
    //   3769: aload_2
    //   3770: getfield 3417	org/telegram/tgnet/TLRPC$TL_game:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   3773: astore 41
    //   3775: aconst_null
    //   3776: astore 42
    //   3778: aload_2
    //   3779: getfield 3418	org/telegram/tgnet/TLRPC$TL_game:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   3782: astore 38
    //   3784: iconst_0
    //   3785: istore 21
    //   3787: ldc_w 3419
    //   3790: astore 37
    //   3792: aload_0
    //   3793: iconst_0
    //   3794: putfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   3797: iconst_0
    //   3798: istore 18
    //   3800: aconst_null
    //   3801: astore_2
    //   3802: iload 14
    //   3804: istore 13
    //   3806: goto -1779 -> 2027
    //   3809: aconst_null
    //   3810: astore 40
    //   3812: goto -43 -> 3769
    //   3815: ldc_w 587
    //   3818: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3821: istore 27
    //   3823: goto -1786 -> 2037
    //   3826: iconst_0
    //   3827: istore_3
    //   3828: goto -1654 -> 2174
    //   3831: astore 44
    //   3833: aload 44
    //   3835: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   3838: iload 19
    //   3840: istore 15
    //   3842: goto -1507 -> 2335
    //   3845: iconst_3
    //   3846: istore 28
    //   3848: aload_0
    //   3849: aload 43
    //   3851: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   3854: iload 32
    //   3856: iload 32
    //   3858: ldc_w 2004
    //   3861: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3864: isub
    //   3865: iconst_3
    //   3866: iconst_4
    //   3867: invokestatic 3421	org/telegram/ui/Cells/ChatMessageCell:generateStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;IIII)Landroid/text/StaticLayout;
    //   3870: putfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   3873: aload_0
    //   3874: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   3877: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   3880: istore 14
    //   3882: iconst_3
    //   3883: iload 14
    //   3885: isub
    //   3886: istore 14
    //   3888: goto -1432 -> 2456
    //   3891: iload 13
    //   3893: istore 16
    //   3895: iload 15
    //   3897: istore 23
    //   3899: iload 20
    //   3901: istore 19
    //   3903: aload_0
    //   3904: aload_0
    //   3905: getfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   3908: iload 33
    //   3910: ineg
    //   3911: invokestatic 486	java/lang/Math:max	(II)I
    //   3914: putfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   3917: goto -1281 -> 2636
    //   3920: astore 44
    //   3922: iload 23
    //   3924: istore 15
    //   3926: iload 16
    //   3928: istore 13
    //   3930: aload 44
    //   3932: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   3935: iload 19
    //   3937: istore 20
    //   3939: iload 14
    //   3941: istore 16
    //   3943: iload 15
    //   3945: istore 19
    //   3947: iload 13
    //   3949: istore 14
    //   3951: iconst_0
    //   3952: istore 13
    //   3954: iconst_0
    //   3955: istore 23
    //   3957: iconst_0
    //   3958: istore 24
    //   3960: iconst_0
    //   3961: istore 15
    //   3963: aload 42
    //   3965: ifnull +15421 -> 19386
    //   3968: aload 43
    //   3970: ifnonnull +15416 -> 19386
    //   3973: aload_0
    //   3974: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   3977: ifeq +29 -> 4006
    //   3980: aload_0
    //   3981: aload_0
    //   3982: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   3985: fconst_2
    //   3986: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3989: iadd
    //   3990: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   3993: aload_0
    //   3994: aload_0
    //   3995: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   3998: fconst_2
    //   3999: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4002: iadd
    //   4003: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4006: iload 16
    //   4008: iconst_3
    //   4009: if_icmpne +539 -> 4548
    //   4012: aload_0
    //   4013: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   4016: ifeq +8 -> 4024
    //   4019: aload 40
    //   4021: ifnonnull +527 -> 4548
    //   4024: aload_0
    //   4025: new 350	android/text/StaticLayout
    //   4028: dup
    //   4029: aload 42
    //   4031: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   4034: iload 32
    //   4036: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   4039: fconst_1
    //   4040: fconst_0
    //   4041: iconst_0
    //   4042: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   4045: putfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4048: iload 16
    //   4050: istore 13
    //   4052: iload 24
    //   4054: istore 23
    //   4056: iload 14
    //   4058: istore 16
    //   4060: aload_0
    //   4061: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4064: aload_0
    //   4065: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4068: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   4071: iconst_1
    //   4072: isub
    //   4073: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   4076: istore 28
    //   4078: iload 24
    //   4080: istore 23
    //   4082: iload 14
    //   4084: istore 16
    //   4086: aload_0
    //   4087: aload_0
    //   4088: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4091: iload 28
    //   4093: iadd
    //   4094: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4097: iload 24
    //   4099: istore 23
    //   4101: iload 14
    //   4103: istore 16
    //   4105: aload_0
    //   4106: aload_0
    //   4107: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4110: iload 28
    //   4112: iadd
    //   4113: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4116: iload 24
    //   4118: istore 23
    //   4120: iload 14
    //   4122: istore 16
    //   4124: aload_0
    //   4125: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4128: iconst_0
    //   4129: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   4132: f2i
    //   4133: istore 28
    //   4135: iload 24
    //   4137: istore 23
    //   4139: iload 14
    //   4141: istore 16
    //   4143: aload_0
    //   4144: iload 28
    //   4146: ineg
    //   4147: putfield 1601	org/telegram/ui/Cells/ChatMessageCell:authorX	I
    //   4150: iload 28
    //   4152: ifeq +441 -> 4593
    //   4155: iload 24
    //   4157: istore 23
    //   4159: iload 14
    //   4161: istore 16
    //   4163: aload_0
    //   4164: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4167: invokevirtual 3366	android/text/StaticLayout:getWidth	()I
    //   4170: iload 28
    //   4172: isub
    //   4173: istore 24
    //   4175: iconst_1
    //   4176: istore 15
    //   4178: iload 15
    //   4180: istore 23
    //   4182: iload 14
    //   4184: istore 16
    //   4186: iload 14
    //   4188: iload 24
    //   4190: iload 27
    //   4192: iadd
    //   4193: invokestatic 486	java/lang/Math:max	(II)I
    //   4196: istore 14
    //   4198: iload 15
    //   4200: istore 23
    //   4202: iload 14
    //   4204: istore 16
    //   4206: iload 19
    //   4208: iload 24
    //   4210: iload 27
    //   4212: iadd
    //   4213: invokestatic 486	java/lang/Math:max	(II)I
    //   4216: istore 24
    //   4218: iload 24
    //   4220: istore 23
    //   4222: iload 15
    //   4224: istore 16
    //   4226: iload 13
    //   4228: istore 15
    //   4230: iload 14
    //   4232: istore 13
    //   4234: aload 40
    //   4236: ifnull +468 -> 4704
    //   4239: iload 14
    //   4241: istore 19
    //   4243: aload_0
    //   4244: iconst_0
    //   4245: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   4248: iload 14
    //   4250: istore 19
    //   4252: aload_0
    //   4253: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   4256: invokevirtual 3424	org/telegram/messenger/MessageObject:generateLinkDescription	()V
    //   4259: iload 14
    //   4261: istore 19
    //   4263: aload_0
    //   4264: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4267: ifeq +37 -> 4304
    //   4270: iload 14
    //   4272: istore 19
    //   4274: aload_0
    //   4275: aload_0
    //   4276: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4279: fconst_2
    //   4280: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4283: iadd
    //   4284: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4287: iload 14
    //   4289: istore 19
    //   4291: aload_0
    //   4292: aload_0
    //   4293: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4296: fconst_2
    //   4297: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4300: iadd
    //   4301: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4304: iconst_0
    //   4305: istore 13
    //   4307: iload 15
    //   4309: iconst_3
    //   4310: if_icmpne +339 -> 4649
    //   4313: iload 14
    //   4315: istore 19
    //   4317: aload_0
    //   4318: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   4321: ifne +328 -> 4649
    //   4324: iload 14
    //   4326: istore 19
    //   4328: aload_0
    //   4329: aload_1
    //   4330: getfield 741	org/telegram/messenger/MessageObject:linkDescription	Ljava/lang/CharSequence;
    //   4333: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   4336: iload 32
    //   4338: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   4341: fconst_1
    //   4342: fconst_1
    //   4343: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4346: i2f
    //   4347: iconst_0
    //   4348: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   4351: iload 32
    //   4353: bipush 6
    //   4355: invokestatic 1315	org/telegram/ui/Components/StaticLayoutEx:createStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZLandroid/text/TextUtils$TruncateAt;II)Landroid/text/StaticLayout;
    //   4358: putfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4361: iload 13
    //   4363: istore 15
    //   4365: iload 14
    //   4367: istore 19
    //   4369: aload_0
    //   4370: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4373: aload_0
    //   4374: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4377: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   4380: iconst_1
    //   4381: isub
    //   4382: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   4385: istore 13
    //   4387: iload 14
    //   4389: istore 19
    //   4391: aload_0
    //   4392: aload_0
    //   4393: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4396: iload 13
    //   4398: iadd
    //   4399: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4402: iload 14
    //   4404: istore 19
    //   4406: aload_0
    //   4407: aload_0
    //   4408: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4411: iload 13
    //   4413: iadd
    //   4414: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4417: iconst_0
    //   4418: istore 24
    //   4420: iconst_0
    //   4421: istore 13
    //   4423: iload 14
    //   4425: istore 19
    //   4427: iload 13
    //   4429: aload_0
    //   4430: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4433: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   4436: if_icmpge +2115 -> 6551
    //   4439: iload 14
    //   4441: istore 19
    //   4443: aload_0
    //   4444: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4447: iload 13
    //   4449: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   4452: f2d
    //   4453: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   4456: d2i
    //   4457: istore 28
    //   4459: iload 28
    //   4461: ifeq +28 -> 4489
    //   4464: iconst_1
    //   4465: istore 24
    //   4467: iload 14
    //   4469: istore 19
    //   4471: aload_0
    //   4472: getfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   4475: ifne +2055 -> 6530
    //   4478: iload 14
    //   4480: istore 19
    //   4482: aload_0
    //   4483: iload 28
    //   4485: ineg
    //   4486: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   4489: iload 13
    //   4491: iconst_1
    //   4492: iadd
    //   4493: istore 13
    //   4495: goto -72 -> 4423
    //   4498: iload 13
    //   4500: istore 16
    //   4502: iload 15
    //   4504: istore 23
    //   4506: iload 20
    //   4508: istore 19
    //   4510: aload_0
    //   4511: getfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   4514: iload 29
    //   4516: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   4519: f2d
    //   4520: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   4523: dstore 5
    //   4525: dload 5
    //   4527: d2i
    //   4528: istore 24
    //   4530: goto -1865 -> 2665
    //   4533: iload 14
    //   4535: istore 16
    //   4537: iload 13
    //   4539: istore 14
    //   4541: iload 15
    //   4543: istore 19
    //   4545: goto -594 -> 3951
    //   4548: aload_0
    //   4549: aload 42
    //   4551: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   4554: iload 32
    //   4556: iload 32
    //   4558: ldc_w 2004
    //   4561: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4564: isub
    //   4565: iload 16
    //   4567: iconst_1
    //   4568: invokestatic 3421	org/telegram/ui/Cells/ChatMessageCell:generateStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;IIII)Landroid/text/StaticLayout;
    //   4571: putfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4574: aload_0
    //   4575: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4578: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   4581: istore 13
    //   4583: iload 16
    //   4585: iload 13
    //   4587: isub
    //   4588: istore 13
    //   4590: goto -538 -> 4052
    //   4593: iload 24
    //   4595: istore 23
    //   4597: iload 14
    //   4599: istore 16
    //   4601: aload_0
    //   4602: getfield 1599	org/telegram/ui/Cells/ChatMessageCell:authorLayout	Landroid/text/StaticLayout;
    //   4605: iconst_0
    //   4606: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   4609: f2d
    //   4610: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   4613: dstore 5
    //   4615: dload 5
    //   4617: d2i
    //   4618: istore 24
    //   4620: goto -442 -> 4178
    //   4623: astore 42
    //   4625: iload 16
    //   4627: istore 13
    //   4629: aload 42
    //   4631: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   4634: iload 13
    //   4636: istore 15
    //   4638: iload 23
    //   4640: istore 16
    //   4642: iload 19
    //   4644: istore 23
    //   4646: goto -416 -> 4230
    //   4649: iload 15
    //   4651: istore 13
    //   4653: iload 14
    //   4655: istore 19
    //   4657: aload_0
    //   4658: aload_1
    //   4659: getfield 741	org/telegram/messenger/MessageObject:linkDescription	Ljava/lang/CharSequence;
    //   4662: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   4665: iload 32
    //   4667: iload 32
    //   4669: ldc_w 2004
    //   4672: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4675: isub
    //   4676: iload 15
    //   4678: bipush 6
    //   4680: invokestatic 3421	org/telegram/ui/Cells/ChatMessageCell:generateStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;IIII)Landroid/text/StaticLayout;
    //   4683: putfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4686: iload 13
    //   4688: istore 15
    //   4690: goto -325 -> 4365
    //   4693: astore 40
    //   4695: aload 40
    //   4697: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   4700: iload 19
    //   4702: istore 13
    //   4704: iload 18
    //   4706: istore 16
    //   4708: iload 18
    //   4710: ifeq +44 -> 4754
    //   4713: aload_0
    //   4714: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4717: ifnull +29 -> 4746
    //   4720: iload 18
    //   4722: istore 16
    //   4724: aload_0
    //   4725: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4728: ifnull +26 -> 4754
    //   4731: iload 18
    //   4733: istore 16
    //   4735: aload_0
    //   4736: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   4739: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   4742: iconst_1
    //   4743: if_icmpne +11 -> 4754
    //   4746: iconst_0
    //   4747: istore 16
    //   4749: aload_0
    //   4750: iconst_0
    //   4751: putfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   4754: iload 16
    //   4756: ifeq +2068 -> 6824
    //   4759: ldc_w 781
    //   4762: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4765: istore 14
    //   4767: aload 38
    //   4769: ifnull +3394 -> 8163
    //   4772: aload 38
    //   4774: invokestatic 3427	org/telegram/messenger/MessageObject:isRoundVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   4777: ifeq +2054 -> 6831
    //   4780: aload_0
    //   4781: aload 38
    //   4783: getfield 1285	org/telegram/tgnet/TLRPC$Document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   4786: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   4789: aload_0
    //   4790: aload 38
    //   4792: putfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   4795: aload_0
    //   4796: bipush 7
    //   4798: putfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4801: iload 13
    //   4803: istore 15
    //   4805: aload_0
    //   4806: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4809: iconst_5
    //   4810: if_icmpeq +868 -> 5678
    //   4813: aload_0
    //   4814: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4817: iconst_3
    //   4818: if_icmpeq +860 -> 5678
    //   4821: aload_0
    //   4822: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4825: iconst_1
    //   4826: if_icmpeq +852 -> 5678
    //   4829: aload_0
    //   4830: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   4833: ifnonnull +7 -> 4840
    //   4836: aload_2
    //   4837: ifnull +4408 -> 9245
    //   4840: aload 37
    //   4842: ifnull +3472 -> 8314
    //   4845: aload 37
    //   4847: ldc_w 3428
    //   4850: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   4853: ifne +42 -> 4895
    //   4856: aload 37
    //   4858: ldc_w 3429
    //   4861: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   4864: ifeq +12 -> 4876
    //   4867: aload_0
    //   4868: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4871: bipush 6
    //   4873: if_icmpne +22 -> 4895
    //   4876: aload 37
    //   4878: ldc_w 3431
    //   4881: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   4884: ifne +11 -> 4895
    //   4887: aload_0
    //   4888: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4891: iconst_4
    //   4892: if_icmpne +3422 -> 8314
    //   4895: iconst_1
    //   4896: istore_3
    //   4897: aload_0
    //   4898: iload_3
    //   4899: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   4902: aload_0
    //   4903: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4906: ifeq +29 -> 4935
    //   4909: aload_0
    //   4910: aload_0
    //   4911: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4914: fconst_2
    //   4915: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4918: iadd
    //   4919: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   4922: aload_0
    //   4923: aload_0
    //   4924: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4927: fconst_2
    //   4928: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4931: iadd
    //   4932: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   4935: aload_0
    //   4936: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   4939: bipush 6
    //   4941: if_icmpne +3395 -> 8336
    //   4944: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   4947: ifeq +3372 -> 8319
    //   4950: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   4953: i2f
    //   4954: ldc_w 2766
    //   4957: fmul
    //   4958: f2i
    //   4959: istore 13
    //   4961: aload_0
    //   4962: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   4965: ifeq +3400 -> 8365
    //   4968: ldc_w 554
    //   4971: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4974: istore 14
    //   4976: iload 15
    //   4978: iload 13
    //   4980: iload 14
    //   4982: isub
    //   4983: iload 27
    //   4985: iadd
    //   4986: invokestatic 486	java/lang/Math:max	(II)I
    //   4989: istore 18
    //   4991: aload_0
    //   4992: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   4995: ifnull +3376 -> 8371
    //   4998: aload_0
    //   4999: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   5002: iconst_m1
    //   5003: putfield 1407	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   5006: aload_0
    //   5007: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   5010: ifnull +11 -> 5021
    //   5013: aload_0
    //   5014: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   5017: iconst_m1
    //   5018: putfield 1407	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   5021: iload 16
    //   5023: ifne +12 -> 5035
    //   5026: aload_0
    //   5027: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   5030: bipush 7
    //   5032: if_icmpne +3347 -> 8379
    //   5035: iload 13
    //   5037: istore 15
    //   5039: iload 13
    //   5041: istore 14
    //   5043: iload 15
    //   5045: istore 13
    //   5047: aload_0
    //   5048: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   5051: ifeq +3537 -> 8588
    //   5054: ldc_w 1074
    //   5057: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5060: iload 22
    //   5062: iadd
    //   5063: aload_0
    //   5064: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5067: if_icmple +46 -> 5113
    //   5070: aload_0
    //   5071: aload_0
    //   5072: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5075: ldc_w 1074
    //   5078: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5081: iload 22
    //   5083: iadd
    //   5084: aload_0
    //   5085: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5088: isub
    //   5089: ldc_w 1629
    //   5092: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5095: iadd
    //   5096: iadd
    //   5097: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5100: aload_0
    //   5101: ldc_w 1074
    //   5104: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5107: iload 22
    //   5109: iadd
    //   5110: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5113: aload_0
    //   5114: aload_0
    //   5115: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5118: ldc_w 1629
    //   5121: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5124: isub
    //   5125: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5128: aload_0
    //   5129: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   5132: iconst_0
    //   5133: iconst_0
    //   5134: iload 14
    //   5136: iload 13
    //   5138: invokevirtual 1581	org/telegram/messenger/ImageReceiver:setImageCoords	(IIII)V
    //   5141: aload_0
    //   5142: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   5145: ldc_w 3433
    //   5148: iconst_2
    //   5149: anewarray 1242	java/lang/Object
    //   5152: dup
    //   5153: iconst_0
    //   5154: iload 14
    //   5156: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5159: aastore
    //   5160: dup
    //   5161: iconst_1
    //   5162: iload 13
    //   5164: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5167: aastore
    //   5168: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   5171: putfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   5174: aload_0
    //   5175: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   5178: ldc_w 3435
    //   5181: iconst_2
    //   5182: anewarray 1242	java/lang/Object
    //   5185: dup
    //   5186: iconst_0
    //   5187: iload 14
    //   5189: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5192: aastore
    //   5193: dup
    //   5194: iconst_1
    //   5195: iload 13
    //   5197: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5200: aastore
    //   5201: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   5204: putfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   5207: aload_2
    //   5208: ifnull +3412 -> 8620
    //   5211: aload_0
    //   5212: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   5215: aload_2
    //   5216: aconst_null
    //   5217: aload_0
    //   5218: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   5221: aconst_null
    //   5222: aconst_null
    //   5223: ldc_w 3437
    //   5226: aload_2
    //   5227: getfield 3438	org/telegram/tgnet/TLRPC$TL_webDocument:size	I
    //   5230: aconst_null
    //   5231: iconst_1
    //   5232: invokevirtual 1362	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   5235: aload_0
    //   5236: iconst_1
    //   5237: putfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   5240: aload 37
    //   5242: ifnull +3931 -> 9173
    //   5245: aload 37
    //   5247: ldc_w 3440
    //   5250: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   5253: ifeq +3920 -> 9173
    //   5256: iload 21
    //   5258: ifeq +3915 -> 9173
    //   5261: iload 21
    //   5263: bipush 60
    //   5265: idiv
    //   5266: istore 13
    //   5268: ldc_w 3442
    //   5271: iconst_2
    //   5272: anewarray 1242	java/lang/Object
    //   5275: dup
    //   5276: iconst_0
    //   5277: iload 13
    //   5279: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5282: aastore
    //   5283: dup
    //   5284: iconst_1
    //   5285: iload 21
    //   5287: iload 13
    //   5289: bipush 60
    //   5291: imul
    //   5292: isub
    //   5293: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5296: aastore
    //   5297: invokestatic 1246	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   5300: astore_2
    //   5301: aload_0
    //   5302: getstatic 1664	org/telegram/ui/ActionBar/Theme:chat_durationPaint	Landroid/text/TextPaint;
    //   5305: aload_2
    //   5306: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   5309: f2d
    //   5310: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   5313: d2i
    //   5314: putfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5317: aload_0
    //   5318: new 350	android/text/StaticLayout
    //   5321: dup
    //   5322: aload_2
    //   5323: getstatic 1664	org/telegram/ui/ActionBar/Theme:chat_durationPaint	Landroid/text/TextPaint;
    //   5326: aload_0
    //   5327: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5330: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   5333: fconst_1
    //   5334: fconst_0
    //   5335: iconst_0
    //   5336: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   5339: putfield 1672	org/telegram/ui/Cells/ChatMessageCell:videoInfoLayout	Landroid/text/StaticLayout;
    //   5342: iload 18
    //   5344: istore 15
    //   5346: iload 15
    //   5348: istore 13
    //   5350: aload_0
    //   5351: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   5354: ifeq +265 -> 5619
    //   5357: aload_1
    //   5358: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   5361: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   5364: getfield 3443	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   5367: iconst_4
    //   5368: iand
    //   5369: ifeq +3920 -> 9289
    //   5372: ldc_w 3445
    //   5375: ldc_w 3446
    //   5378: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5381: invokevirtual 3449	java/lang/String:toUpperCase	()Ljava/lang/String;
    //   5384: astore_2
    //   5385: invokestatic 2467	org/telegram/messenger/LocaleController:getInstance	()Lorg/telegram/messenger/LocaleController;
    //   5388: aload_1
    //   5389: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   5392: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   5395: getfield 3452	org/telegram/tgnet/TLRPC$MessageMedia:total_amount	J
    //   5398: aload_1
    //   5399: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   5402: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   5405: getfield 3455	org/telegram/tgnet/TLRPC$MessageMedia:currency	Ljava/lang/String;
    //   5408: invokevirtual 3459	org/telegram/messenger/LocaleController:formatCurrencyString	(JLjava/lang/String;)Ljava/lang/String;
    //   5411: astore 37
    //   5413: new 2172	android/text/SpannableStringBuilder
    //   5416: dup
    //   5417: new 1320	java/lang/StringBuilder
    //   5420: dup
    //   5421: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   5424: aload 37
    //   5426: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5429: ldc_w 1327
    //   5432: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5435: aload_2
    //   5436: invokevirtual 2503	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
    //   5439: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   5442: invokespecial 2175	android/text/SpannableStringBuilder:<init>	(Ljava/lang/CharSequence;)V
    //   5445: astore_2
    //   5446: aload_2
    //   5447: new 2591	org/telegram/ui/Components/TypefaceSpan
    //   5450: dup
    //   5451: ldc_w 2606
    //   5454: invokestatic 2610	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   5457: invokespecial 2646	org/telegram/ui/Components/TypefaceSpan:<init>	(Landroid/graphics/Typeface;)V
    //   5460: iconst_0
    //   5461: aload 37
    //   5463: invokevirtual 1054	java/lang/String:length	()I
    //   5466: bipush 33
    //   5468: invokevirtual 2604	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   5471: aload_0
    //   5472: getstatic 1679	org/telegram/ui/ActionBar/Theme:chat_shipmentPaint	Landroid/text/TextPaint;
    //   5475: aload_2
    //   5476: iconst_0
    //   5477: aload_2
    //   5478: invokevirtual 2611	android/text/SpannableStringBuilder:length	()I
    //   5481: invokevirtual 2500	android/text/TextPaint:measureText	(Ljava/lang/CharSequence;II)F
    //   5484: f2d
    //   5485: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   5488: d2i
    //   5489: putfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5492: aload_0
    //   5493: new 350	android/text/StaticLayout
    //   5496: dup
    //   5497: aload_2
    //   5498: getstatic 1679	org/telegram/ui/ActionBar/Theme:chat_shipmentPaint	Landroid/text/TextPaint;
    //   5501: aload_0
    //   5502: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5505: ldc_w 587
    //   5508: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5511: iadd
    //   5512: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   5515: fconst_1
    //   5516: fconst_0
    //   5517: iconst_0
    //   5518: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   5521: putfield 1672	org/telegram/ui/Cells/ChatMessageCell:videoInfoLayout	Landroid/text/StaticLayout;
    //   5524: iload 15
    //   5526: istore 13
    //   5528: aload_0
    //   5529: getfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   5532: ifne +87 -> 5619
    //   5535: aload_0
    //   5536: aload_0
    //   5537: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5540: ldc_w 1588
    //   5543: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5546: iadd
    //   5547: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5550: aload_0
    //   5551: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   5554: istore 14
    //   5556: aload_1
    //   5557: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   5560: ifeq +3774 -> 9334
    //   5563: bipush 20
    //   5565: istore 13
    //   5567: iload 14
    //   5569: iload 13
    //   5571: bipush 14
    //   5573: iadd
    //   5574: i2f
    //   5575: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5578: iadd
    //   5579: istore 13
    //   5581: aload_0
    //   5582: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5585: iload 13
    //   5587: iadd
    //   5588: iload 17
    //   5590: if_icmple +3750 -> 9340
    //   5593: aload_0
    //   5594: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   5597: iload 15
    //   5599: invokestatic 486	java/lang/Math:max	(II)I
    //   5602: istore 13
    //   5604: aload_0
    //   5605: aload_0
    //   5606: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5609: ldc_w 554
    //   5612: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5615: iadd
    //   5616: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5619: aload_0
    //   5620: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   5623: ifeq +45 -> 5668
    //   5626: aload_1
    //   5627: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   5630: ifeq +38 -> 5668
    //   5633: aload_0
    //   5634: aload_0
    //   5635: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5638: aload_1
    //   5639: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   5642: ldc_w 1588
    //   5645: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5648: iadd
    //   5649: iadd
    //   5650: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   5653: aload_0
    //   5654: aload_0
    //   5655: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5658: ldc_w 1078
    //   5661: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5664: iadd
    //   5665: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5668: aload_0
    //   5669: iload 17
    //   5671: iload 26
    //   5673: iload 13
    //   5675: invokespecial 3461	org/telegram/ui/Cells/ChatMessageCell:calcBackgroundWidth	(III)V
    //   5678: iload 31
    //   5680: istore 19
    //   5682: aload_0
    //   5683: getfield 556	org/telegram/ui/Cells/ChatMessageCell:drawInstantView	Z
    //   5686: ifeq +212 -> 5898
    //   5689: aload_0
    //   5690: ldc_w 3462
    //   5693: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5696: putfield 1694	org/telegram/ui/Cells/ChatMessageCell:instantWidth	I
    //   5699: aload_0
    //   5700: getfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   5703: iconst_1
    //   5704: if_icmpne +3653 -> 9357
    //   5707: ldc_w 3464
    //   5710: ldc_w 3465
    //   5713: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5716: astore_2
    //   5717: aload_0
    //   5718: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   5721: ldc_w 3466
    //   5724: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5727: isub
    //   5728: istore 13
    //   5730: aload_0
    //   5731: new 350	android/text/StaticLayout
    //   5734: dup
    //   5735: aload_2
    //   5736: getstatic 1690	org/telegram/ui/ActionBar/Theme:chat_instantViewPaint	Landroid/text/TextPaint;
    //   5739: iload 13
    //   5741: i2f
    //   5742: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   5745: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   5748: getstatic 1690	org/telegram/ui/ActionBar/Theme:chat_instantViewPaint	Landroid/text/TextPaint;
    //   5751: iload 13
    //   5753: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   5756: fconst_1
    //   5757: fconst_0
    //   5758: iconst_0
    //   5759: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   5762: putfield 1711	org/telegram/ui/Cells/ChatMessageCell:instantViewLayout	Landroid/text/StaticLayout;
    //   5765: aload_0
    //   5766: aload_0
    //   5767: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   5770: ldc_w 3467
    //   5773: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5776: isub
    //   5777: putfield 1694	org/telegram/ui/Cells/ChatMessageCell:instantWidth	I
    //   5780: aload_0
    //   5781: aload_0
    //   5782: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5785: ldc_w 3468
    //   5788: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5791: iadd
    //   5792: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   5795: iload 31
    //   5797: istore 19
    //   5799: aload_0
    //   5800: getfield 1711	org/telegram/ui/Cells/ChatMessageCell:instantViewLayout	Landroid/text/StaticLayout;
    //   5803: ifnull +95 -> 5898
    //   5806: iload 31
    //   5808: istore 19
    //   5810: aload_0
    //   5811: getfield 1711	org/telegram/ui/Cells/ChatMessageCell:instantViewLayout	Landroid/text/StaticLayout;
    //   5814: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   5817: ifle +81 -> 5898
    //   5820: aload_0
    //   5821: getfield 1694	org/telegram/ui/Cells/ChatMessageCell:instantWidth	I
    //   5824: i2d
    //   5825: aload_0
    //   5826: getfield 1711	org/telegram/ui/Cells/ChatMessageCell:instantViewLayout	Landroid/text/StaticLayout;
    //   5829: iconst_0
    //   5830: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   5833: f2d
    //   5834: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   5837: dsub
    //   5838: d2i
    //   5839: iconst_2
    //   5840: idiv
    //   5841: istore 14
    //   5843: aload_0
    //   5844: getfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   5847: ifne +3565 -> 9412
    //   5850: ldc_w 1629
    //   5853: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5856: istore 13
    //   5858: aload_0
    //   5859: iload 13
    //   5861: iload 14
    //   5863: iadd
    //   5864: putfield 1702	org/telegram/ui/Cells/ChatMessageCell:instantTextX	I
    //   5867: aload_0
    //   5868: aload_0
    //   5869: getfield 1711	org/telegram/ui/Cells/ChatMessageCell:instantViewLayout	Landroid/text/StaticLayout;
    //   5872: iconst_0
    //   5873: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   5876: f2i
    //   5877: putfield 1700	org/telegram/ui/Cells/ChatMessageCell:instantTextLeftX	I
    //   5880: aload_0
    //   5881: aload_0
    //   5882: getfield 1702	org/telegram/ui/Cells/ChatMessageCell:instantTextX	I
    //   5885: aload_0
    //   5886: getfield 1700	org/telegram/ui/Cells/ChatMessageCell:instantTextLeftX	I
    //   5889: ineg
    //   5890: iadd
    //   5891: putfield 1702	org/telegram/ui/Cells/ChatMessageCell:instantTextX	I
    //   5894: iload 31
    //   5896: istore 19
    //   5898: aload_0
    //   5899: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   5902: ifnonnull +12276 -> 18178
    //   5905: aload_0
    //   5906: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   5909: ifnonnull +12269 -> 18178
    //   5912: aload_1
    //   5913: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   5916: ifnull +12262 -> 18178
    //   5919: aload_1
    //   5920: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   5923: bipush 13
    //   5925: if_icmpeq +12253 -> 18178
    //   5928: aload_0
    //   5929: aload_1
    //   5930: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   5933: putfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   5936: aload_0
    //   5937: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   5940: ldc_w 487
    //   5943: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5946: isub
    //   5947: istore 15
    //   5949: iload 15
    //   5951: ldc_w 587
    //   5954: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5957: isub
    //   5958: istore 14
    //   5960: getstatic 790	android/os/Build$VERSION:SDK_INT	I
    //   5963: bipush 24
    //   5965: if_icmplt +12166 -> 18131
    //   5968: aload_0
    //   5969: aload_1
    //   5970: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   5973: iconst_0
    //   5974: aload_1
    //   5975: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   5978: invokeinterface 2192 1 0
    //   5983: getstatic 2970	org/telegram/ui/ActionBar/Theme:chat_msgTextPaint	Landroid/text/TextPaint;
    //   5986: iload 14
    //   5988: invokestatic 3474	android/text/StaticLayout$Builder:obtain	(Ljava/lang/CharSequence;IILandroid/text/TextPaint;I)Landroid/text/StaticLayout$Builder;
    //   5991: iconst_1
    //   5992: invokevirtual 3478	android/text/StaticLayout$Builder:setBreakStrategy	(I)Landroid/text/StaticLayout$Builder;
    //   5995: iconst_0
    //   5996: invokevirtual 3481	android/text/StaticLayout$Builder:setHyphenationFrequency	(I)Landroid/text/StaticLayout$Builder;
    //   5999: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   6002: invokevirtual 3485	android/text/StaticLayout$Builder:setAlignment	(Landroid/text/Layout$Alignment;)Landroid/text/StaticLayout$Builder;
    //   6005: invokevirtual 3489	android/text/StaticLayout$Builder:build	()Landroid/text/StaticLayout;
    //   6008: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6011: iload 19
    //   6013: istore 13
    //   6015: aload_0
    //   6016: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6019: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6022: ifle +160 -> 6182
    //   6025: aload_0
    //   6026: iload 14
    //   6028: putfield 643	org/telegram/ui/Cells/ChatMessageCell:captionWidth	I
    //   6031: aload_0
    //   6032: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   6035: istore 16
    //   6037: aload_1
    //   6038: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   6041: ifeq +12131 -> 18172
    //   6044: ldc_w 1077
    //   6047: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6050: istore 14
    //   6052: aload_0
    //   6053: aload_0
    //   6054: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6057: invokevirtual 780	android/text/StaticLayout:getHeight	()I
    //   6060: putfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   6063: aload_0
    //   6064: aload_0
    //   6065: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6068: aload_0
    //   6069: getfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   6072: ldc_w 1705
    //   6075: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6078: iadd
    //   6079: iadd
    //   6080: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6083: aload_0
    //   6084: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6087: aload_0
    //   6088: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6091: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6094: iconst_1
    //   6095: isub
    //   6096: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   6099: fstore 11
    //   6101: aload_0
    //   6102: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6105: aload_0
    //   6106: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   6109: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6112: iconst_1
    //   6113: isub
    //   6114: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   6117: fstore 12
    //   6119: iload 19
    //   6121: istore 13
    //   6123: iload 15
    //   6125: ldc_w 1629
    //   6128: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6131: isub
    //   6132: i2f
    //   6133: fload 11
    //   6135: fload 12
    //   6137: fadd
    //   6138: fsub
    //   6139: iload 16
    //   6141: iload 14
    //   6143: iadd
    //   6144: i2f
    //   6145: fcmpg
    //   6146: ifge +36 -> 6182
    //   6149: aload_0
    //   6150: aload_0
    //   6151: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6154: ldc_w 478
    //   6157: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6160: iadd
    //   6161: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6164: aload_0
    //   6165: aload_0
    //   6166: getfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   6169: ldc_w 478
    //   6172: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6175: iadd
    //   6176: putfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   6179: iconst_2
    //   6180: istore 13
    //   6182: aload_0
    //   6183: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   6186: getfield 581	org/telegram/messenger/MessageObject:eventId	J
    //   6189: lconst_0
    //   6190: lcmp
    //   6191: ifeq +12134 -> 18325
    //   6194: aload_0
    //   6195: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   6198: invokevirtual 3492	org/telegram/messenger/MessageObject:isMediaEmpty	()Z
    //   6201: ifne +12124 -> 18325
    //   6204: aload_0
    //   6205: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   6208: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   6211: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   6214: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   6217: ifnull +12108 -> 18325
    //   6220: aload_0
    //   6221: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   6224: ldc_w 2803
    //   6227: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6230: isub
    //   6231: istore 14
    //   6233: aload_0
    //   6234: iconst_1
    //   6235: putfield 461	org/telegram/ui/Cells/ChatMessageCell:hasOldCaptionPreview	Z
    //   6238: aload_0
    //   6239: iconst_0
    //   6240: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6243: aload_0
    //   6244: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   6247: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   6250: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   6253: getfield 828	org/telegram/tgnet/TLRPC$MessageMedia:webpage	Lorg/telegram/tgnet/TLRPC$WebPage;
    //   6256: astore_2
    //   6257: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   6260: aload_2
    //   6261: getfield 899	org/telegram/tgnet/TLRPC$WebPage:site_name	Ljava/lang/String;
    //   6264: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   6267: fconst_1
    //   6268: fadd
    //   6269: f2d
    //   6270: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   6273: d2i
    //   6274: istore 15
    //   6276: aload_0
    //   6277: iload 15
    //   6279: putfield 1564	org/telegram/ui/Cells/ChatMessageCell:siteNameWidth	I
    //   6282: aload_0
    //   6283: new 350	android/text/StaticLayout
    //   6286: dup
    //   6287: aload_2
    //   6288: getfield 899	org/telegram/tgnet/TLRPC$WebPage:site_name	Ljava/lang/String;
    //   6291: getstatic 1554	org/telegram/ui/ActionBar/Theme:chat_replyNamePaint	Landroid/text/TextPaint;
    //   6294: iload 15
    //   6296: iload 14
    //   6298: invokestatic 1195	java/lang/Math:min	(II)I
    //   6301: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   6304: fconst_1
    //   6305: fconst_0
    //   6306: iconst_0
    //   6307: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   6310: putfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   6313: aload_0
    //   6314: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   6317: iconst_0
    //   6318: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   6321: fconst_0
    //   6322: fcmpl
    //   6323: ifeq +11909 -> 18232
    //   6326: iconst_1
    //   6327: istore_3
    //   6328: aload_0
    //   6329: iload_3
    //   6330: putfield 1562	org/telegram/ui/Cells/ChatMessageCell:siteNameRtl	Z
    //   6333: aload_0
    //   6334: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   6337: aload_0
    //   6338: getfield 1522	org/telegram/ui/Cells/ChatMessageCell:siteNameLayout	Landroid/text/StaticLayout;
    //   6341: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6344: iconst_1
    //   6345: isub
    //   6346: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   6349: istore 15
    //   6351: aload_0
    //   6352: aload_0
    //   6353: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6356: iload 15
    //   6358: iadd
    //   6359: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6362: aload_0
    //   6363: aload_0
    //   6364: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6367: iload 15
    //   6369: iadd
    //   6370: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6373: aload_0
    //   6374: iconst_0
    //   6375: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6378: aload_0
    //   6379: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6382: ifeq +16 -> 6398
    //   6385: aload_0
    //   6386: aload_0
    //   6387: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6390: fconst_2
    //   6391: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6394: iadd
    //   6395: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6398: aload_0
    //   6399: aload_2
    //   6400: getfield 1375	org/telegram/tgnet/TLRPC$WebPage:description	Ljava/lang/String;
    //   6403: getstatic 1593	org/telegram/ui/ActionBar/Theme:chat_replyTextPaint	Landroid/text/TextPaint;
    //   6406: iload 14
    //   6408: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   6411: fconst_1
    //   6412: fconst_1
    //   6413: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6416: i2f
    //   6417: iconst_0
    //   6418: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   6421: iload 14
    //   6423: bipush 6
    //   6425: invokestatic 1315	org/telegram/ui/Components/StaticLayoutEx:createStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZLandroid/text/TextUtils$TruncateAt;II)Landroid/text/StaticLayout;
    //   6428: putfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6431: aload_0
    //   6432: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6435: aload_0
    //   6436: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6439: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6442: iconst_1
    //   6443: isub
    //   6444: invokevirtual 1525	android/text/StaticLayout:getLineBottom	(I)I
    //   6447: istore 14
    //   6449: aload_0
    //   6450: aload_0
    //   6451: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6454: iload 14
    //   6456: iadd
    //   6457: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   6460: aload_0
    //   6461: aload_0
    //   6462: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6465: iload 14
    //   6467: iadd
    //   6468: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   6471: iconst_0
    //   6472: istore 14
    //   6474: iload 14
    //   6476: aload_0
    //   6477: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6480: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6483: if_icmpge +11786 -> 18269
    //   6486: aload_0
    //   6487: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6490: iload 14
    //   6492: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   6495: f2d
    //   6496: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   6499: d2i
    //   6500: istore 15
    //   6502: iload 15
    //   6504: ifeq +17 -> 6521
    //   6507: aload_0
    //   6508: getfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6511: ifne +11736 -> 18247
    //   6514: aload_0
    //   6515: iload 15
    //   6517: ineg
    //   6518: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6521: iload 14
    //   6523: iconst_1
    //   6524: iadd
    //   6525: istore 14
    //   6527: goto -53 -> 6474
    //   6530: iload 14
    //   6532: istore 19
    //   6534: aload_0
    //   6535: aload_0
    //   6536: getfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6539: iload 28
    //   6541: ineg
    //   6542: invokestatic 486	java/lang/Math:max	(II)I
    //   6545: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6548: goto -2059 -> 4489
    //   6551: iload 14
    //   6553: istore 19
    //   6555: aload_0
    //   6556: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6559: invokevirtual 3366	android/text/StaticLayout:getWidth	()I
    //   6562: istore 30
    //   6564: iconst_0
    //   6565: istore 13
    //   6567: iload 23
    //   6569: istore 28
    //   6571: iload 13
    //   6573: istore 23
    //   6575: iload 14
    //   6577: istore 19
    //   6579: iload 14
    //   6581: istore 13
    //   6583: iload 23
    //   6585: aload_0
    //   6586: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6589: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   6592: if_icmpge -1888 -> 4704
    //   6595: iload 14
    //   6597: istore 19
    //   6599: aload_0
    //   6600: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6603: iload 23
    //   6605: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   6608: f2d
    //   6609: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   6612: d2i
    //   6613: istore 33
    //   6615: iload 33
    //   6617: ifne +12784 -> 19401
    //   6620: iload 14
    //   6622: istore 19
    //   6624: aload_0
    //   6625: getfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6628: ifeq +12773 -> 19401
    //   6631: iload 14
    //   6633: istore 19
    //   6635: aload_0
    //   6636: iconst_0
    //   6637: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   6640: goto +12761 -> 19401
    //   6643: iload 23
    //   6645: iload 15
    //   6647: if_icmplt +36 -> 6683
    //   6650: iload 13
    //   6652: istore 29
    //   6654: iload 15
    //   6656: ifeq +42 -> 6698
    //   6659: iload 13
    //   6661: istore 29
    //   6663: iload 33
    //   6665: ifeq +33 -> 6698
    //   6668: iload 14
    //   6670: istore 19
    //   6672: iload 13
    //   6674: istore 29
    //   6676: aload_0
    //   6677: getfield 1637	org/telegram/ui/Cells/ChatMessageCell:isSmallImage	Z
    //   6680: ifeq +18 -> 6698
    //   6683: iload 14
    //   6685: istore 19
    //   6687: iload 13
    //   6689: ldc_w 2004
    //   6692: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6695: iadd
    //   6696: istore 29
    //   6698: iload 28
    //   6700: istore 13
    //   6702: iload 28
    //   6704: iload 29
    //   6706: iload 27
    //   6708: iadd
    //   6709: if_icmpge +58 -> 6767
    //   6712: iload 20
    //   6714: ifeq +24 -> 6738
    //   6717: iload 14
    //   6719: istore 19
    //   6721: aload_0
    //   6722: aload_0
    //   6723: getfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   6726: iload 29
    //   6728: iload 27
    //   6730: iadd
    //   6731: iload 28
    //   6733: isub
    //   6734: iadd
    //   6735: putfield 1597	org/telegram/ui/Cells/ChatMessageCell:titleX	I
    //   6738: iload 16
    //   6740: ifeq +12676 -> 19416
    //   6743: iload 14
    //   6745: istore 19
    //   6747: aload_0
    //   6748: aload_0
    //   6749: getfield 1601	org/telegram/ui/Cells/ChatMessageCell:authorX	I
    //   6752: iload 29
    //   6754: iload 27
    //   6756: iadd
    //   6757: iload 28
    //   6759: isub
    //   6760: iadd
    //   6761: putfield 1601	org/telegram/ui/Cells/ChatMessageCell:authorX	I
    //   6764: goto +12652 -> 19416
    //   6767: iload 14
    //   6769: istore 19
    //   6771: iload 14
    //   6773: iload 29
    //   6775: iload 27
    //   6777: iadd
    //   6778: invokestatic 486	java/lang/Math:max	(II)I
    //   6781: istore 14
    //   6783: iload 23
    //   6785: iconst_1
    //   6786: iadd
    //   6787: istore 23
    //   6789: iload 13
    //   6791: istore 28
    //   6793: goto -218 -> 6575
    //   6796: iload 14
    //   6798: istore 19
    //   6800: aload_0
    //   6801: getfield 732	org/telegram/ui/Cells/ChatMessageCell:descriptionLayout	Landroid/text/StaticLayout;
    //   6804: iload 23
    //   6806: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   6809: f2d
    //   6810: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   6813: d2i
    //   6814: iload 30
    //   6816: invokestatic 1195	java/lang/Math:min	(II)I
    //   6819: istore 13
    //   6821: goto -178 -> 6643
    //   6824: iload 32
    //   6826: istore 14
    //   6828: goto -2061 -> 4767
    //   6831: aload 38
    //   6833: invokestatic 3495	org/telegram/messenger/MessageObject:isGifDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   6836: ifeq +230 -> 7066
    //   6839: getstatic 833	org/telegram/messenger/SharedConfig:autoplayGifs	Z
    //   6842: ifne +8 -> 6850
    //   6845: aload_1
    //   6846: fconst_1
    //   6847: putfield 882	org/telegram/messenger/MessageObject:gifState	F
    //   6850: aload_0
    //   6851: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   6854: astore 40
    //   6856: aload_1
    //   6857: getfield 882	org/telegram/messenger/MessageObject:gifState	F
    //   6860: fconst_1
    //   6861: fcmpl
    //   6862: ifeq +190 -> 7052
    //   6865: iconst_1
    //   6866: istore_3
    //   6867: aload 40
    //   6869: iload_3
    //   6870: invokevirtual 885	org/telegram/messenger/ImageReceiver:setAllowStartAnimation	(Z)V
    //   6873: aload_0
    //   6874: aload 38
    //   6876: getfield 1285	org/telegram/tgnet/TLRPC$Document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6879: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6882: aload_0
    //   6883: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6886: ifnull +148 -> 7034
    //   6889: aload_0
    //   6890: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6893: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   6896: ifeq +13 -> 6909
    //   6899: aload_0
    //   6900: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6903: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   6906: ifne +128 -> 7034
    //   6909: iconst_0
    //   6910: istore 15
    //   6912: iload 15
    //   6914: aload 38
    //   6916: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   6919: invokevirtual 590	java/util/ArrayList:size	()I
    //   6922: if_icmpge +58 -> 6980
    //   6925: aload 38
    //   6927: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   6930: iload 15
    //   6932: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   6935: checkcast 1161	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   6938: astore 40
    //   6940: aload 40
    //   6942: instanceof 3503
    //   6945: ifne +11 -> 6956
    //   6948: aload 40
    //   6950: instanceof 1255
    //   6953: ifeq +104 -> 7057
    //   6956: aload_0
    //   6957: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6960: aload 40
    //   6962: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   6965: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   6968: aload_0
    //   6969: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6972: aload 40
    //   6974: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   6977: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   6980: aload_0
    //   6981: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6984: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   6987: ifeq +13 -> 7000
    //   6990: aload_0
    //   6991: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   6994: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   6997: ifne +37 -> 7034
    //   7000: aload_0
    //   7001: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7004: astore 40
    //   7006: aload_0
    //   7007: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7010: astore 41
    //   7012: ldc_w 3506
    //   7015: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7018: istore 15
    //   7020: aload 41
    //   7022: iload 15
    //   7024: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7027: aload 40
    //   7029: iload 15
    //   7031: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7034: aload_0
    //   7035: aload 38
    //   7037: putfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   7040: aload_0
    //   7041: iconst_2
    //   7042: putfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   7045: iload 13
    //   7047: istore 15
    //   7049: goto -2244 -> 4805
    //   7052: iconst_0
    //   7053: istore_3
    //   7054: goto -187 -> 6867
    //   7057: iload 15
    //   7059: iconst_1
    //   7060: iadd
    //   7061: istore 15
    //   7063: goto -151 -> 6912
    //   7066: aload 38
    //   7068: invokestatic 1250	org/telegram/messenger/MessageObject:isVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   7071: ifeq +179 -> 7250
    //   7074: aload_0
    //   7075: aload 38
    //   7077: getfield 1285	org/telegram/tgnet/TLRPC$Document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7080: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7083: aload_0
    //   7084: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7087: ifnull +140 -> 7227
    //   7090: aload_0
    //   7091: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7094: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7097: ifeq +13 -> 7110
    //   7100: aload_0
    //   7101: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7104: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7107: ifne +120 -> 7227
    //   7110: iconst_0
    //   7111: istore 15
    //   7113: iload 15
    //   7115: aload 38
    //   7117: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   7120: invokevirtual 590	java/util/ArrayList:size	()I
    //   7123: if_icmpge +50 -> 7173
    //   7126: aload 38
    //   7128: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   7131: iload 15
    //   7133: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7136: checkcast 1161	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   7139: astore 40
    //   7141: aload 40
    //   7143: instanceof 1255
    //   7146: ifeq +95 -> 7241
    //   7149: aload_0
    //   7150: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7153: aload 40
    //   7155: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   7158: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7161: aload_0
    //   7162: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7165: aload 40
    //   7167: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   7170: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7173: aload_0
    //   7174: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7177: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7180: ifeq +13 -> 7193
    //   7183: aload_0
    //   7184: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7187: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7190: ifne +37 -> 7227
    //   7193: aload_0
    //   7194: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7197: astore 40
    //   7199: aload_0
    //   7200: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7203: astore 41
    //   7205: ldc_w 3506
    //   7208: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7211: istore 15
    //   7213: aload 41
    //   7215: iload 15
    //   7217: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7220: aload 40
    //   7222: iload 15
    //   7224: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7227: aload_0
    //   7228: iconst_0
    //   7229: aload_1
    //   7230: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   7233: pop
    //   7234: iload 13
    //   7236: istore 15
    //   7238: goto -2433 -> 4805
    //   7241: iload 15
    //   7243: iconst_1
    //   7244: iadd
    //   7245: istore 15
    //   7247: goto -134 -> 7113
    //   7250: aload 38
    //   7252: invokestatic 3511	org/telegram/messenger/MessageObject:isStickerDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   7255: ifeq +184 -> 7439
    //   7258: aload_0
    //   7259: aload 38
    //   7261: getfield 1285	org/telegram/tgnet/TLRPC$Document:thumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7264: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7267: aload_0
    //   7268: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7271: ifnull +140 -> 7411
    //   7274: aload_0
    //   7275: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7278: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7281: ifeq +13 -> 7294
    //   7284: aload_0
    //   7285: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7288: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7291: ifne +120 -> 7411
    //   7294: iconst_0
    //   7295: istore 15
    //   7297: iload 15
    //   7299: aload 38
    //   7301: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   7304: invokevirtual 590	java/util/ArrayList:size	()I
    //   7307: if_icmpge +50 -> 7357
    //   7310: aload 38
    //   7312: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   7315: iload 15
    //   7317: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7320: checkcast 1161	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   7323: astore 40
    //   7325: aload 40
    //   7327: instanceof 3503
    //   7330: ifeq +100 -> 7430
    //   7333: aload_0
    //   7334: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7337: aload 40
    //   7339: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   7342: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7345: aload_0
    //   7346: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7349: aload 40
    //   7351: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   7354: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7357: aload_0
    //   7358: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7361: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7364: ifeq +13 -> 7377
    //   7367: aload_0
    //   7368: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7371: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7374: ifne +37 -> 7411
    //   7377: aload_0
    //   7378: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7381: astore 40
    //   7383: aload_0
    //   7384: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   7387: astore 41
    //   7389: ldc_w 3506
    //   7392: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7395: istore 15
    //   7397: aload 41
    //   7399: iload 15
    //   7401: putfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   7404: aload 40
    //   7406: iload 15
    //   7408: putfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   7411: aload_0
    //   7412: aload 38
    //   7414: putfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   7417: aload_0
    //   7418: bipush 6
    //   7420: putfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   7423: iload 13
    //   7425: istore 15
    //   7427: goto -2622 -> 4805
    //   7430: iload 15
    //   7432: iconst_1
    //   7433: iadd
    //   7434: istore 15
    //   7436: goto -139 -> 7297
    //   7439: aload_0
    //   7440: iload 17
    //   7442: iload 26
    //   7444: iload 13
    //   7446: invokespecial 3461	org/telegram/ui/Cells/ChatMessageCell:calcBackgroundWidth	(III)V
    //   7449: aload 38
    //   7451: invokestatic 3511	org/telegram/messenger/MessageObject:isStickerDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   7454: ifne +11925 -> 19379
    //   7457: aload_0
    //   7458: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   7461: ldc_w 1077
    //   7464: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7467: iload 17
    //   7469: iadd
    //   7470: if_icmpge +16 -> 7486
    //   7473: aload_0
    //   7474: ldc_w 1077
    //   7477: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7480: iload 17
    //   7482: iadd
    //   7483: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   7486: aload 38
    //   7488: invokestatic 1154	org/telegram/messenger/MessageObject:isVoiceDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   7491: ifeq +251 -> 7742
    //   7494: aload_0
    //   7495: aload_0
    //   7496: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   7499: ldc_w 587
    //   7502: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7505: isub
    //   7506: aload_1
    //   7507: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   7510: pop
    //   7511: aload_0
    //   7512: aload_0
    //   7513: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   7516: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   7519: ldc_w 1629
    //   7522: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7525: iadd
    //   7526: aload_0
    //   7527: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7530: iadd
    //   7531: putfield 565	org/telegram/ui/Cells/ChatMessageCell:mediaOffsetY	I
    //   7534: aload_0
    //   7535: aload_0
    //   7536: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   7539: ldc_w 1819
    //   7542: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7545: iadd
    //   7546: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   7549: aload_0
    //   7550: aload_0
    //   7551: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7554: ldc_w 1819
    //   7557: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7560: iadd
    //   7561: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7564: iload 17
    //   7566: ldc_w 1201
    //   7569: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7572: isub
    //   7573: istore 17
    //   7575: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   7578: ifeq +87 -> 7665
    //   7581: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   7584: istore 15
    //   7586: aload_0
    //   7587: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   7590: ifeq +69 -> 7659
    //   7593: aload_1
    //   7594: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   7597: ifeq +62 -> 7659
    //   7600: aload_1
    //   7601: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   7604: ifne +55 -> 7659
    //   7607: ldc_w 2004
    //   7610: fstore 11
    //   7612: iload 13
    //   7614: iload 15
    //   7616: fload 11
    //   7618: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7621: isub
    //   7622: ldc_w 2145
    //   7625: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7628: invokestatic 1195	java/lang/Math:min	(II)I
    //   7631: ldc_w 1079
    //   7634: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7637: isub
    //   7638: iload 27
    //   7640: iadd
    //   7641: invokestatic 486	java/lang/Math:max	(II)I
    //   7644: istore 15
    //   7646: aload_0
    //   7647: iload 17
    //   7649: iload 26
    //   7651: iload 15
    //   7653: invokespecial 3461	org/telegram/ui/Cells/ChatMessageCell:calcBackgroundWidth	(III)V
    //   7656: goto -2851 -> 4805
    //   7659: fconst_0
    //   7660: fstore 11
    //   7662: goto -50 -> 7612
    //   7665: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   7668: getfield 2253	android/graphics/Point:x	I
    //   7671: istore 15
    //   7673: aload_0
    //   7674: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   7677: ifeq +59 -> 7736
    //   7680: aload_1
    //   7681: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   7684: ifeq +52 -> 7736
    //   7687: aload_1
    //   7688: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   7691: ifne +45 -> 7736
    //   7694: ldc_w 2004
    //   7697: fstore 11
    //   7699: iload 13
    //   7701: iload 15
    //   7703: fload 11
    //   7705: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7708: isub
    //   7709: ldc_w 2145
    //   7712: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7715: invokestatic 1195	java/lang/Math:min	(II)I
    //   7718: ldc_w 1079
    //   7721: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7724: isub
    //   7725: iload 27
    //   7727: iadd
    //   7728: invokestatic 486	java/lang/Math:max	(II)I
    //   7731: istore 15
    //   7733: goto -87 -> 7646
    //   7736: fconst_0
    //   7737: fstore 11
    //   7739: goto -40 -> 7699
    //   7742: aload 38
    //   7744: invokestatic 1200	org/telegram/messenger/MessageObject:isMusicDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   7747: ifeq +225 -> 7972
    //   7750: aload_0
    //   7751: aload_0
    //   7752: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   7755: ldc_w 587
    //   7758: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7761: isub
    //   7762: aload_1
    //   7763: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   7766: istore 15
    //   7768: aload_0
    //   7769: aload_0
    //   7770: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   7773: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   7776: ldc_w 1629
    //   7779: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7782: iadd
    //   7783: aload_0
    //   7784: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7787: iadd
    //   7788: putfield 565	org/telegram/ui/Cells/ChatMessageCell:mediaOffsetY	I
    //   7791: aload_0
    //   7792: aload_0
    //   7793: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   7796: ldc_w 1796
    //   7799: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7802: iadd
    //   7803: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   7806: aload_0
    //   7807: aload_0
    //   7808: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7811: ldc_w 1796
    //   7814: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7817: iadd
    //   7818: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   7821: iload 17
    //   7823: ldc_w 1201
    //   7826: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7829: isub
    //   7830: istore 17
    //   7832: iload 13
    //   7834: iload 15
    //   7836: iload 27
    //   7838: iadd
    //   7839: ldc_w 1167
    //   7842: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7845: iadd
    //   7846: invokestatic 486	java/lang/Math:max	(II)I
    //   7849: istore 15
    //   7851: iload 15
    //   7853: istore 13
    //   7855: aload_0
    //   7856: getfield 1223	org/telegram/ui/Cells/ChatMessageCell:songLayout	Landroid/text/StaticLayout;
    //   7859: ifnull +46 -> 7905
    //   7862: iload 15
    //   7864: istore 13
    //   7866: aload_0
    //   7867: getfield 1223	org/telegram/ui/Cells/ChatMessageCell:songLayout	Landroid/text/StaticLayout;
    //   7870: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   7873: ifle +32 -> 7905
    //   7876: iload 15
    //   7878: i2f
    //   7879: aload_0
    //   7880: getfield 1223	org/telegram/ui/Cells/ChatMessageCell:songLayout	Landroid/text/StaticLayout;
    //   7883: iconst_0
    //   7884: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   7887: iload 27
    //   7889: i2f
    //   7890: fadd
    //   7891: ldc_w 1201
    //   7894: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7897: i2f
    //   7898: fadd
    //   7899: invokestatic 3514	java/lang/Math:max	(FF)F
    //   7902: f2i
    //   7903: istore 13
    //   7905: iload 13
    //   7907: istore 15
    //   7909: aload_0
    //   7910: getfield 1236	org/telegram/ui/Cells/ChatMessageCell:performerLayout	Landroid/text/StaticLayout;
    //   7913: ifnull +46 -> 7959
    //   7916: iload 13
    //   7918: istore 15
    //   7920: aload_0
    //   7921: getfield 1236	org/telegram/ui/Cells/ChatMessageCell:performerLayout	Landroid/text/StaticLayout;
    //   7924: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   7927: ifle +32 -> 7959
    //   7930: iload 13
    //   7932: i2f
    //   7933: aload_0
    //   7934: getfield 1236	org/telegram/ui/Cells/ChatMessageCell:performerLayout	Landroid/text/StaticLayout;
    //   7937: iconst_0
    //   7938: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   7941: iload 27
    //   7943: i2f
    //   7944: fadd
    //   7945: ldc_w 1201
    //   7948: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7951: i2f
    //   7952: fadd
    //   7953: invokestatic 3514	java/lang/Math:max	(FF)F
    //   7956: f2i
    //   7957: istore 15
    //   7959: aload_0
    //   7960: iload 17
    //   7962: iload 26
    //   7964: iload 15
    //   7966: invokespecial 3461	org/telegram/ui/Cells/ChatMessageCell:calcBackgroundWidth	(III)V
    //   7969: goto -3164 -> 4805
    //   7972: aload_0
    //   7973: aload_0
    //   7974: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   7977: ldc_w 3515
    //   7980: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   7983: isub
    //   7984: aload_1
    //   7985: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   7988: pop
    //   7989: aload_0
    //   7990: iconst_1
    //   7991: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   7994: aload_0
    //   7995: getfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   7998: ifeq +69 -> 8067
    //   8001: aload_0
    //   8002: aload_0
    //   8003: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8006: ldc_w 2568
    //   8009: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8012: iadd
    //   8013: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8016: aload_0
    //   8017: aload_0
    //   8018: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8021: ldc_w 1201
    //   8024: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8027: iadd
    //   8028: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8031: aload_0
    //   8032: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8035: iconst_0
    //   8036: aload_0
    //   8037: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8040: aload_0
    //   8041: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   8044: iadd
    //   8045: ldc_w 1201
    //   8048: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8051: ldc_w 1201
    //   8054: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8057: invokevirtual 1581	org/telegram/messenger/ImageReceiver:setImageCoords	(IIII)V
    //   8060: iload 13
    //   8062: istore 15
    //   8064: goto -3259 -> 4805
    //   8067: aload_0
    //   8068: aload_0
    //   8069: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   8072: getfield 775	org/telegram/messenger/MessageObject:textHeight	I
    //   8075: ldc_w 1629
    //   8078: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8081: iadd
    //   8082: aload_0
    //   8083: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8086: iadd
    //   8087: putfield 565	org/telegram/ui/Cells/ChatMessageCell:mediaOffsetY	I
    //   8090: aload_0
    //   8091: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8094: iconst_0
    //   8095: aload_0
    //   8096: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8099: aload_0
    //   8100: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   8103: iadd
    //   8104: ldc_w 478
    //   8107: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8110: isub
    //   8111: ldc_w 1796
    //   8114: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8117: ldc_w 1796
    //   8120: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8123: invokevirtual 1581	org/telegram/messenger/ImageReceiver:setImageCoords	(IIII)V
    //   8126: aload_0
    //   8127: aload_0
    //   8128: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8131: ldc_w 3369
    //   8134: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8137: iadd
    //   8138: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8141: aload_0
    //   8142: aload_0
    //   8143: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8146: ldc_w 1074
    //   8149: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8152: iadd
    //   8153: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8156: iload 13
    //   8158: istore 15
    //   8160: goto -3355 -> 4805
    //   8163: aload 41
    //   8165: ifnull +118 -> 8283
    //   8168: aload 37
    //   8170: ifnull +96 -> 8266
    //   8173: aload 37
    //   8175: ldc_w 3428
    //   8178: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   8181: ifeq +85 -> 8266
    //   8184: iconst_1
    //   8185: istore_3
    //   8186: aload_0
    //   8187: iload_3
    //   8188: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   8191: aload_1
    //   8192: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   8195: astore 40
    //   8197: aload_0
    //   8198: getfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   8201: ifeq +70 -> 8271
    //   8204: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   8207: istore 15
    //   8209: aload_0
    //   8210: getfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   8213: ifne +65 -> 8278
    //   8216: iconst_1
    //   8217: istore_3
    //   8218: aload_0
    //   8219: aload 40
    //   8221: iload 15
    //   8223: iload_3
    //   8224: invokestatic 3518	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;IZ)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8227: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8230: aload_0
    //   8231: aload_1
    //   8232: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   8235: bipush 80
    //   8237: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8240: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8243: aload_0
    //   8244: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8247: aload_0
    //   8248: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8251: if_acmpne +11128 -> 19379
    //   8254: aload_0
    //   8255: aconst_null
    //   8256: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8259: iload 13
    //   8261: istore 15
    //   8263: goto -3458 -> 4805
    //   8266: iconst_0
    //   8267: istore_3
    //   8268: goto -82 -> 8186
    //   8271: iload 14
    //   8273: istore 15
    //   8275: goto -66 -> 8209
    //   8278: iconst_0
    //   8279: istore_3
    //   8280: goto -62 -> 8218
    //   8283: aload_2
    //   8284: ifnull +11095 -> 19379
    //   8287: aload_2
    //   8288: getfield 3519	org/telegram/tgnet/TLRPC$TL_webDocument:mime_type	Ljava/lang/String;
    //   8291: ldc_w 1278
    //   8294: invokevirtual 1282	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   8297: ifne +11079 -> 19376
    //   8300: aconst_null
    //   8301: astore_2
    //   8302: aload_0
    //   8303: iconst_0
    //   8304: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   8307: iload 13
    //   8309: istore 15
    //   8311: goto -3506 -> 4805
    //   8314: iconst_0
    //   8315: istore_3
    //   8316: goto -3419 -> 4897
    //   8319: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   8322: getfield 2253	android/graphics/Point:x	I
    //   8325: i2f
    //   8326: ldc_w 2766
    //   8329: fmul
    //   8330: f2i
    //   8331: istore 13
    //   8333: goto -3372 -> 4961
    //   8336: iload 14
    //   8338: istore 13
    //   8340: aload_0
    //   8341: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8344: bipush 7
    //   8346: if_icmpne -3385 -> 4961
    //   8349: getstatic 1846	org/telegram/messenger/AndroidUtilities:roundMessageSize	I
    //   8352: istore 13
    //   8354: aload_0
    //   8355: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8358: iconst_1
    //   8359: invokevirtual 3303	org/telegram/messenger/ImageReceiver:setAllowDecodeSingleFrame	(Z)V
    //   8362: goto -3401 -> 4961
    //   8365: iconst_0
    //   8366: istore 14
    //   8368: goto -3392 -> 4976
    //   8371: aload_2
    //   8372: iconst_m1
    //   8373: putfield 3438	org/telegram/tgnet/TLRPC$TL_webDocument:size	I
    //   8376: goto -3355 -> 5021
    //   8379: aload_0
    //   8380: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   8383: ifne +10 -> 8393
    //   8386: aload_0
    //   8387: getfield 465	org/telegram/ui/Cells/ChatMessageCell:hasInvoicePreview	Z
    //   8390: ifeq +41 -> 8431
    //   8393: sipush 640
    //   8396: i2f
    //   8397: iload 13
    //   8399: fconst_2
    //   8400: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8403: isub
    //   8404: i2f
    //   8405: fdiv
    //   8406: fstore 11
    //   8408: sipush 640
    //   8411: i2f
    //   8412: fload 11
    //   8414: fdiv
    //   8415: f2i
    //   8416: istore 14
    //   8418: sipush 360
    //   8421: i2f
    //   8422: fload 11
    //   8424: fdiv
    //   8425: f2i
    //   8426: istore 13
    //   8428: goto -3381 -> 5047
    //   8431: aload_0
    //   8432: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8435: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   8438: istore 15
    //   8440: aload_0
    //   8441: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8444: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   8447: istore 14
    //   8449: iload 15
    //   8451: i2f
    //   8452: iload 13
    //   8454: fconst_2
    //   8455: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8458: isub
    //   8459: i2f
    //   8460: fdiv
    //   8461: fstore 11
    //   8463: iload 15
    //   8465: i2f
    //   8466: fload 11
    //   8468: fdiv
    //   8469: f2i
    //   8470: istore 15
    //   8472: iload 14
    //   8474: i2f
    //   8475: fload 11
    //   8477: fdiv
    //   8478: f2i
    //   8479: istore 16
    //   8481: aload 39
    //   8483: ifnull +29 -> 8512
    //   8486: aload 39
    //   8488: ifnull +62 -> 8550
    //   8491: aload 39
    //   8493: invokevirtual 1276	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   8496: ldc_w 3354
    //   8499: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   8502: ifne +48 -> 8550
    //   8505: aload_0
    //   8506: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8509: ifne +41 -> 8550
    //   8512: iload 16
    //   8514: istore 13
    //   8516: iload 15
    //   8518: istore 14
    //   8520: iload 16
    //   8522: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   8525: getfield 2262	android/graphics/Point:y	I
    //   8528: iconst_3
    //   8529: idiv
    //   8530: if_icmple -3483 -> 5047
    //   8533: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   8536: getfield 2262	android/graphics/Point:y	I
    //   8539: iconst_3
    //   8540: idiv
    //   8541: istore 13
    //   8543: iload 15
    //   8545: istore 14
    //   8547: goto -3500 -> 5047
    //   8550: iload 16
    //   8552: istore 13
    //   8554: iload 15
    //   8556: istore 14
    //   8558: iload 16
    //   8560: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   8563: getfield 2262	android/graphics/Point:y	I
    //   8566: iconst_2
    //   8567: idiv
    //   8568: if_icmple -3521 -> 5047
    //   8571: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   8574: getfield 2262	android/graphics/Point:y	I
    //   8577: iconst_2
    //   8578: idiv
    //   8579: istore 13
    //   8581: iload 15
    //   8583: istore 14
    //   8585: goto -3538 -> 5047
    //   8588: aload_0
    //   8589: aload_0
    //   8590: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8593: ldc_w 554
    //   8596: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8599: iload 13
    //   8601: iadd
    //   8602: iadd
    //   8603: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   8606: aload_0
    //   8607: aload_0
    //   8608: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8611: iload 13
    //   8613: iadd
    //   8614: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   8617: goto -3489 -> 5128
    //   8620: aload_0
    //   8621: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8624: bipush 6
    //   8626: if_icmpne +70 -> 8696
    //   8629: aload_0
    //   8630: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8633: astore 38
    //   8635: aload_0
    //   8636: getfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   8639: astore 39
    //   8641: aload_0
    //   8642: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   8645: astore 40
    //   8647: aload_0
    //   8648: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8651: ifnull +40 -> 8691
    //   8654: aload_0
    //   8655: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8658: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8661: astore_2
    //   8662: aload 38
    //   8664: aload 39
    //   8666: aconst_null
    //   8667: aload 40
    //   8669: aconst_null
    //   8670: aload_2
    //   8671: ldc_w 3437
    //   8674: aload_0
    //   8675: getfield 1149	org/telegram/ui/Cells/ChatMessageCell:documentAttach	Lorg/telegram/tgnet/TLRPC$Document;
    //   8678: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   8681: ldc_w 3521
    //   8684: iconst_1
    //   8685: invokevirtual 1362	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   8688: goto -3453 -> 5235
    //   8691: aconst_null
    //   8692: astore_2
    //   8693: goto -31 -> 8662
    //   8696: aload_0
    //   8697: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8700: iconst_4
    //   8701: if_icmpne +53 -> 8754
    //   8704: aload_0
    //   8705: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8708: iconst_1
    //   8709: invokevirtual 1348	org/telegram/messenger/ImageReceiver:setNeedsQualityThumb	(Z)V
    //   8712: aload_0
    //   8713: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8716: iconst_1
    //   8717: invokevirtual 1351	org/telegram/messenger/ImageReceiver:setShouldGenerateQualityThumb	(Z)V
    //   8720: aload_0
    //   8721: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8724: aload_1
    //   8725: invokevirtual 1354	org/telegram/messenger/ImageReceiver:setParentMessageObject	(Lorg/telegram/messenger/MessageObject;)V
    //   8728: aload_0
    //   8729: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8732: aconst_null
    //   8733: aconst_null
    //   8734: aload_0
    //   8735: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8738: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8741: aload_0
    //   8742: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   8745: iconst_0
    //   8746: aconst_null
    //   8747: iconst_0
    //   8748: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   8751: goto -3516 -> 5235
    //   8754: aload_0
    //   8755: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8758: iconst_2
    //   8759: if_icmpeq +12 -> 8771
    //   8762: aload_0
    //   8763: getfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   8766: bipush 7
    //   8768: if_icmpne +207 -> 8975
    //   8771: aload 38
    //   8773: invokestatic 3525	org/telegram/messenger/FileLoader:getAttachFileName	(Lorg/telegram/tgnet/TLObject;)Ljava/lang/String;
    //   8776: astore_2
    //   8777: iconst_0
    //   8778: istore_3
    //   8779: aload 38
    //   8781: invokestatic 3427	org/telegram/messenger/MessageObject:isRoundVideoDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   8784: ifeq +111 -> 8895
    //   8787: aload_0
    //   8788: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8791: getstatic 1846	org/telegram/messenger/AndroidUtilities:roundMessageSize	I
    //   8794: iconst_2
    //   8795: idiv
    //   8796: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   8799: aload_0
    //   8800: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   8803: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   8806: aload_0
    //   8807: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   8810: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   8813: istore_3
    //   8814: aload_1
    //   8815: invokevirtual 1426	org/telegram/messenger/MessageObject:isSending	()Z
    //   8818: ifne +108 -> 8926
    //   8821: aload_1
    //   8822: getfield 2747	org/telegram/messenger/MessageObject:mediaExists	Z
    //   8825: ifne +21 -> 8846
    //   8828: aload_0
    //   8829: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   8832: invokestatic 1378	org/telegram/messenger/FileLoader:getInstance	(I)Lorg/telegram/messenger/FileLoader;
    //   8835: aload_2
    //   8836: invokevirtual 3531	org/telegram/messenger/FileLoader:isLoadingFile	(Ljava/lang/String;)Z
    //   8839: ifne +7 -> 8846
    //   8842: iload_3
    //   8843: ifeq +83 -> 8926
    //   8846: aload_0
    //   8847: iconst_0
    //   8848: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   8851: aload_0
    //   8852: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8855: astore 39
    //   8857: aload_0
    //   8858: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8861: ifnull +60 -> 8921
    //   8864: aload_0
    //   8865: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8868: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8871: astore_2
    //   8872: aload 39
    //   8874: aload 38
    //   8876: aconst_null
    //   8877: aload_2
    //   8878: aload_0
    //   8879: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   8882: aload 38
    //   8884: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   8887: aconst_null
    //   8888: iconst_0
    //   8889: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   8892: goto -3657 -> 5235
    //   8895: aload 38
    //   8897: invokestatic 3534	org/telegram/messenger/MessageObject:isNewGifDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   8900: ifeq -86 -> 8814
    //   8903: aload_0
    //   8904: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   8907: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   8910: aload_0
    //   8911: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   8914: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   8917: istore_3
    //   8918: goto -104 -> 8814
    //   8921: aconst_null
    //   8922: astore_2
    //   8923: goto -51 -> 8872
    //   8926: aload_0
    //   8927: iconst_1
    //   8928: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   8931: aload_0
    //   8932: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   8935: astore 38
    //   8937: aload_0
    //   8938: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8941: ifnull +29 -> 8970
    //   8944: aload_0
    //   8945: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8948: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   8951: astore_2
    //   8952: aload 38
    //   8954: aconst_null
    //   8955: aconst_null
    //   8956: aload_2
    //   8957: aload_0
    //   8958: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   8961: iconst_0
    //   8962: aconst_null
    //   8963: iconst_0
    //   8964: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   8967: goto -3732 -> 5235
    //   8970: aconst_null
    //   8971: astore_2
    //   8972: goto -20 -> 8952
    //   8975: aload_1
    //   8976: getfield 2747	org/telegram/messenger/MessageObject:mediaExists	Z
    //   8979: istore_3
    //   8980: aload_0
    //   8981: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   8984: invokestatic 3525	org/telegram/messenger/FileLoader:getAttachFileName	(Lorg/telegram/tgnet/TLObject;)Ljava/lang/String;
    //   8987: astore_2
    //   8988: aload_0
    //   8989: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   8992: ifne +38 -> 9030
    //   8995: iload_3
    //   8996: ifne +34 -> 9030
    //   8999: aload_0
    //   9000: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   9003: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   9006: aload_0
    //   9007: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   9010: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   9013: ifne +17 -> 9030
    //   9016: aload_0
    //   9017: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   9020: invokestatic 1378	org/telegram/messenger/FileLoader:getInstance	(I)Lorg/telegram/messenger/FileLoader;
    //   9023: aload_2
    //   9024: invokevirtual 3531	org/telegram/messenger/FileLoader:isLoadingFile	(Ljava/lang/String;)Z
    //   9027: ifeq +69 -> 9096
    //   9030: aload_0
    //   9031: iconst_0
    //   9032: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   9035: aload_0
    //   9036: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9039: astore 38
    //   9041: aload_0
    //   9042: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9045: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   9048: astore 39
    //   9050: aload_0
    //   9051: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   9054: astore 40
    //   9056: aload_0
    //   9057: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9060: ifnull +31 -> 9091
    //   9063: aload_0
    //   9064: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9067: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   9070: astore_2
    //   9071: aload 38
    //   9073: aload 39
    //   9075: aload 40
    //   9077: aload_2
    //   9078: aload_0
    //   9079: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   9082: iconst_0
    //   9083: aconst_null
    //   9084: iconst_0
    //   9085: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   9088: goto -3853 -> 5235
    //   9091: aconst_null
    //   9092: astore_2
    //   9093: goto -22 -> 9071
    //   9096: aload_0
    //   9097: iconst_1
    //   9098: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   9101: aload_0
    //   9102: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9105: ifnull +54 -> 9159
    //   9108: aload_0
    //   9109: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9112: aconst_null
    //   9113: aconst_null
    //   9114: aload_0
    //   9115: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   9118: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   9121: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   9124: ldc_w 3435
    //   9127: iconst_2
    //   9128: anewarray 1242	java/lang/Object
    //   9131: dup
    //   9132: iconst_0
    //   9133: iload 14
    //   9135: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   9138: aastore
    //   9139: dup
    //   9140: iconst_1
    //   9141: iload 13
    //   9143: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   9146: aastore
    //   9147: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   9150: iconst_0
    //   9151: aconst_null
    //   9152: iconst_0
    //   9153: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   9156: goto -3921 -> 5235
    //   9159: aload_0
    //   9160: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9163: aconst_null
    //   9164: checkcast 794	android/graphics/drawable/Drawable
    //   9167: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   9170: goto -3935 -> 5235
    //   9173: iload 18
    //   9175: istore 15
    //   9177: aload_0
    //   9178: getfield 463	org/telegram/ui/Cells/ChatMessageCell:hasGamePreview	Z
    //   9181: ifeq -3835 -> 5346
    //   9184: ldc_w 3536
    //   9187: ldc_w 3537
    //   9190: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9193: invokevirtual 3449	java/lang/String:toUpperCase	()Ljava/lang/String;
    //   9196: astore_2
    //   9197: aload_0
    //   9198: getstatic 3540	org/telegram/ui/ActionBar/Theme:chat_gamePaint	Landroid/text/TextPaint;
    //   9201: aload_2
    //   9202: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   9205: f2d
    //   9206: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   9209: d2i
    //   9210: putfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   9213: aload_0
    //   9214: new 350	android/text/StaticLayout
    //   9217: dup
    //   9218: aload_2
    //   9219: getstatic 3540	org/telegram/ui/ActionBar/Theme:chat_gamePaint	Landroid/text/TextPaint;
    //   9222: aload_0
    //   9223: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   9226: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   9229: fconst_1
    //   9230: fconst_0
    //   9231: iconst_0
    //   9232: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   9235: putfield 1672	org/telegram/ui/Cells/ChatMessageCell:videoInfoLayout	Landroid/text/StaticLayout;
    //   9238: iload 18
    //   9240: istore 15
    //   9242: goto -3896 -> 5346
    //   9245: aload_0
    //   9246: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9249: aconst_null
    //   9250: checkcast 794	android/graphics/drawable/Drawable
    //   9253: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   9256: aload_0
    //   9257: aload_0
    //   9258: getfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   9261: ldc_w 1588
    //   9264: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9267: isub
    //   9268: putfield 777	org/telegram/ui/Cells/ChatMessageCell:linkPreviewHeight	I
    //   9271: aload_0
    //   9272: aload_0
    //   9273: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   9276: ldc_w 1078
    //   9279: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9282: iadd
    //   9283: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   9286: goto -3940 -> 5346
    //   9289: aload_1
    //   9290: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9293: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   9296: getfield 3543	org/telegram/tgnet/TLRPC$MessageMedia:test	Z
    //   9299: ifeq +19 -> 9318
    //   9302: ldc_w 3545
    //   9305: ldc_w 3546
    //   9308: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9311: invokevirtual 3449	java/lang/String:toUpperCase	()Ljava/lang/String;
    //   9314: astore_2
    //   9315: goto -3930 -> 5385
    //   9318: ldc_w 3548
    //   9321: ldc_w 3549
    //   9324: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9327: invokevirtual 3449	java/lang/String:toUpperCase	()Ljava/lang/String;
    //   9330: astore_2
    //   9331: goto -3946 -> 5385
    //   9334: iconst_0
    //   9335: istore 13
    //   9337: goto -3770 -> 5567
    //   9340: aload_0
    //   9341: getfield 1675	org/telegram/ui/Cells/ChatMessageCell:durationWidth	I
    //   9344: iload 13
    //   9346: iadd
    //   9347: iload 15
    //   9349: invokestatic 486	java/lang/Math:max	(II)I
    //   9352: istore 13
    //   9354: goto -3735 -> 5619
    //   9357: aload_0
    //   9358: getfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   9361: iconst_2
    //   9362: if_icmpne +16 -> 9378
    //   9365: ldc_w 3551
    //   9368: ldc_w 3552
    //   9371: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9374: astore_2
    //   9375: goto -3658 -> 5717
    //   9378: aload_0
    //   9379: getfield 849	org/telegram/ui/Cells/ChatMessageCell:drawInstantViewType	I
    //   9382: iconst_3
    //   9383: if_icmpne +16 -> 9399
    //   9386: ldc_w 3554
    //   9389: ldc_w 3555
    //   9392: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9395: astore_2
    //   9396: goto -3679 -> 5717
    //   9399: ldc_w 3557
    //   9402: ldc_w 3558
    //   9405: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9408: astore_2
    //   9409: goto -3692 -> 5717
    //   9412: iconst_0
    //   9413: istore 13
    //   9415: goto -3557 -> 5858
    //   9418: aload_0
    //   9419: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9422: aconst_null
    //   9423: checkcast 794	android/graphics/drawable/Drawable
    //   9426: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   9429: aload_0
    //   9430: iload 17
    //   9432: iload 26
    //   9434: iload 16
    //   9436: invokespecial 3461	org/telegram/ui/Cells/ChatMessageCell:calcBackgroundWidth	(III)V
    //   9439: iload 31
    //   9441: istore 19
    //   9443: goto -3545 -> 5898
    //   9446: aload_1
    //   9447: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   9450: bipush 16
    //   9452: if_icmpne +501 -> 9953
    //   9455: aload_0
    //   9456: iconst_0
    //   9457: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   9460: aload_0
    //   9461: iconst_0
    //   9462: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   9465: aload_0
    //   9466: iconst_0
    //   9467: putfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   9470: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   9473: ifeq +347 -> 9820
    //   9476: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   9479: istore 13
    //   9481: aload_0
    //   9482: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   9485: ifeq +327 -> 9812
    //   9488: aload_1
    //   9489: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   9492: ifeq +320 -> 9812
    //   9495: aload_1
    //   9496: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   9499: ifne +313 -> 9812
    //   9502: ldc_w 3559
    //   9505: fstore 11
    //   9507: aload_0
    //   9508: iload 13
    //   9510: fload 11
    //   9512: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9515: isub
    //   9516: ldc_w 3560
    //   9519: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9522: invokestatic 1195	java/lang/Math:min	(II)I
    //   9525: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   9528: aload_0
    //   9529: aload_0
    //   9530: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   9533: ldc_w 487
    //   9536: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9539: isub
    //   9540: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   9543: aload_0
    //   9544: invokespecial 2565	org/telegram/ui/Cells/ChatMessageCell:getMaxNameWidth	()I
    //   9547: ldc_w 1074
    //   9550: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9553: isub
    //   9554: istore 13
    //   9556: iload 13
    //   9558: ifge +9815 -> 19373
    //   9561: ldc_w 587
    //   9564: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9567: istore 13
    //   9569: invokestatic 2467	org/telegram/messenger/LocaleController:getInstance	()Lorg/telegram/messenger/LocaleController;
    //   9572: getfield 2471	org/telegram/messenger/LocaleController:formatterDay	Lorg/telegram/messenger/time/FastDateFormat;
    //   9575: aload_1
    //   9576: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9579: getfield 1966	org/telegram/tgnet/TLRPC$Message:date	I
    //   9582: i2l
    //   9583: ldc2_w 919
    //   9586: lmul
    //   9587: invokevirtual 2475	org/telegram/messenger/time/FastDateFormat:format	(J)Ljava/lang/String;
    //   9590: astore 38
    //   9592: aload_1
    //   9593: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   9596: getfield 2052	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
    //   9599: checkcast 3562	org/telegram/tgnet/TLRPC$TL_messageActionPhoneCall
    //   9602: astore 39
    //   9604: aload 39
    //   9606: getfield 3563	org/telegram/tgnet/TLRPC$TL_messageActionPhoneCall:reason	Lorg/telegram/tgnet/TLRPC$PhoneCallDiscardReason;
    //   9609: instanceof 2060
    //   9612: istore_3
    //   9613: aload_1
    //   9614: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   9617: ifeq +282 -> 9899
    //   9620: iload_3
    //   9621: ifeq +265 -> 9886
    //   9624: ldc_w 3565
    //   9627: ldc_w 3566
    //   9630: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9633: astore_2
    //   9634: aload 38
    //   9636: astore 37
    //   9638: aload 39
    //   9640: getfield 3567	org/telegram/tgnet/TLRPC$TL_messageActionPhoneCall:duration	I
    //   9643: ifle +37 -> 9680
    //   9646: new 1320	java/lang/StringBuilder
    //   9649: dup
    //   9650: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   9653: aload 38
    //   9655: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   9658: ldc_w 2477
    //   9661: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   9664: aload 39
    //   9666: getfield 3567	org/telegram/tgnet/TLRPC$TL_messageActionPhoneCall:duration	I
    //   9669: invokestatic 3570	org/telegram/messenger/LocaleController:formatCallDuration	(I)Ljava/lang/String;
    //   9672: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   9675: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   9678: astore 37
    //   9680: aload_0
    //   9681: new 350	android/text/StaticLayout
    //   9684: dup
    //   9685: aload_2
    //   9686: getstatic 1211	org/telegram/ui/ActionBar/Theme:chat_audioTitlePaint	Landroid/text/TextPaint;
    //   9689: iload 13
    //   9691: i2f
    //   9692: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   9695: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   9698: getstatic 1211	org/telegram/ui/ActionBar/Theme:chat_audioTitlePaint	Landroid/text/TextPaint;
    //   9701: iload 13
    //   9703: fconst_2
    //   9704: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9707: iadd
    //   9708: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   9711: fconst_1
    //   9712: fconst_0
    //   9713: iconst_0
    //   9714: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   9717: putfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   9720: aload_0
    //   9721: new 350	android/text/StaticLayout
    //   9724: dup
    //   9725: aload 37
    //   9727: getstatic 2019	org/telegram/ui/ActionBar/Theme:chat_contactPhonePaint	Landroid/text/TextPaint;
    //   9730: iload 13
    //   9732: i2f
    //   9733: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   9736: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   9739: getstatic 2019	org/telegram/ui/ActionBar/Theme:chat_contactPhonePaint	Landroid/text/TextPaint;
    //   9742: iload 13
    //   9744: fconst_2
    //   9745: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9748: iadd
    //   9749: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   9752: fconst_1
    //   9753: fconst_0
    //   9754: iconst_0
    //   9755: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   9758: putfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   9761: aload_0
    //   9762: aload_1
    //   9763: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   9766: aload_0
    //   9767: ldc_w 3126
    //   9770: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9773: aload_0
    //   9774: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   9777: iadd
    //   9778: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   9781: iload 31
    //   9783: istore 19
    //   9785: aload_0
    //   9786: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   9789: ifeq -3891 -> 5898
    //   9792: aload_0
    //   9793: aload_0
    //   9794: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   9797: fconst_1
    //   9798: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9801: isub
    //   9802: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   9805: iload 31
    //   9807: istore 19
    //   9809: goto -3911 -> 5898
    //   9812: ldc_w 1074
    //   9815: fstore 11
    //   9817: goto -310 -> 9507
    //   9820: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   9823: getfield 2253	android/graphics/Point:x	I
    //   9826: istore 13
    //   9828: aload_0
    //   9829: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   9832: ifeq +46 -> 9878
    //   9835: aload_1
    //   9836: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   9839: ifeq +39 -> 9878
    //   9842: aload_1
    //   9843: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   9846: ifne +32 -> 9878
    //   9849: ldc_w 3559
    //   9852: fstore 11
    //   9854: aload_0
    //   9855: iload 13
    //   9857: fload 11
    //   9859: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9862: isub
    //   9863: ldc_w 3560
    //   9866: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9869: invokestatic 1195	java/lang/Math:min	(II)I
    //   9872: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   9875: goto -347 -> 9528
    //   9878: ldc_w 1074
    //   9881: fstore 11
    //   9883: goto -29 -> 9854
    //   9886: ldc_w 3572
    //   9889: ldc_w 3573
    //   9892: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9895: astore_2
    //   9896: goto -262 -> 9634
    //   9899: iload_3
    //   9900: ifeq +16 -> 9916
    //   9903: ldc_w 3575
    //   9906: ldc_w 3576
    //   9909: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9912: astore_2
    //   9913: goto -279 -> 9634
    //   9916: aload 39
    //   9918: getfield 3563	org/telegram/tgnet/TLRPC$TL_messageActionPhoneCall:reason	Lorg/telegram/tgnet/TLRPC$PhoneCallDiscardReason;
    //   9921: instanceof 2062
    //   9924: ifeq +16 -> 9940
    //   9927: ldc_w 3578
    //   9930: ldc_w 3579
    //   9933: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9936: astore_2
    //   9937: goto -303 -> 9634
    //   9940: ldc_w 3581
    //   9943: ldc_w 3582
    //   9946: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9949: astore_2
    //   9950: goto -316 -> 9634
    //   9953: aload_1
    //   9954: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   9957: bipush 12
    //   9959: if_icmpne +706 -> 10665
    //   9962: aload_0
    //   9963: iconst_0
    //   9964: putfield 2421	org/telegram/ui/Cells/ChatMessageCell:drawName	Z
    //   9967: aload_0
    //   9968: iconst_1
    //   9969: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   9972: aload_0
    //   9973: iconst_1
    //   9974: putfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   9977: aload_0
    //   9978: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   9981: ldc_w 1883
    //   9984: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9987: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   9990: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   9993: ifeq +527 -> 10520
    //   9996: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   9999: istore 13
    //   10001: aload_0
    //   10002: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   10005: ifeq +507 -> 10512
    //   10008: aload_1
    //   10009: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   10012: ifeq +500 -> 10512
    //   10015: aload_1
    //   10016: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10019: ifne +493 -> 10512
    //   10022: ldc_w 3559
    //   10025: fstore 11
    //   10027: aload_0
    //   10028: iload 13
    //   10030: fload 11
    //   10032: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10035: isub
    //   10036: ldc_w 3560
    //   10039: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10042: invokestatic 1195	java/lang/Math:min	(II)I
    //   10045: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10048: aload_0
    //   10049: aload_0
    //   10050: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10053: ldc_w 487
    //   10056: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10059: isub
    //   10060: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   10063: aload_1
    //   10064: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10067: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   10070: getfield 1104	org/telegram/tgnet/TLRPC$MessageMedia:user_id	I
    //   10073: istore 13
    //   10075: aload_0
    //   10076: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   10079: invokestatic 1006	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   10082: iload 13
    //   10084: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   10087: invokevirtual 1019	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   10090: astore 38
    //   10092: aload_0
    //   10093: invokespecial 2565	org/telegram/ui/Cells/ChatMessageCell:getMaxNameWidth	()I
    //   10096: ldc_w 3583
    //   10099: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10102: isub
    //   10103: istore 13
    //   10105: iload 13
    //   10107: ifge +9263 -> 19370
    //   10110: ldc_w 587
    //   10113: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10116: istore 13
    //   10118: aconst_null
    //   10119: astore_2
    //   10120: aconst_null
    //   10121: astore 37
    //   10123: aload 38
    //   10125: ifnull +32 -> 10157
    //   10128: aload 37
    //   10130: astore_2
    //   10131: aload 38
    //   10133: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   10136: ifnull +12 -> 10148
    //   10139: aload 38
    //   10141: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   10144: getfield 2402	org/telegram/tgnet/TLRPC$UserProfilePhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   10147: astore_2
    //   10148: aload_0
    //   10149: getfield 404	org/telegram/ui/Cells/ChatMessageCell:contactAvatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   10152: aload 38
    //   10154: invokevirtual 2543	org/telegram/ui/Components/AvatarDrawable:setInfo	(Lorg/telegram/tgnet/TLRPC$User;)V
    //   10157: aload_0
    //   10158: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   10161: astore 39
    //   10163: aload 38
    //   10165: ifnull +421 -> 10586
    //   10168: aload_0
    //   10169: getfield 404	org/telegram/ui/Cells/ChatMessageCell:contactAvatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   10172: astore 37
    //   10174: aload 39
    //   10176: aload_2
    //   10177: ldc_w 2545
    //   10180: aload 37
    //   10182: aconst_null
    //   10183: iconst_0
    //   10184: invokevirtual 2548	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Ljava/lang/String;I)V
    //   10187: aload_1
    //   10188: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10191: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   10194: getfield 3586	org/telegram/tgnet/TLRPC$MessageMedia:phone_number	Ljava/lang/String;
    //   10197: astore_2
    //   10198: aload_2
    //   10199: ifnull +418 -> 10617
    //   10202: aload_2
    //   10203: invokevirtual 1054	java/lang/String:length	()I
    //   10206: ifeq +411 -> 10617
    //   10209: invokestatic 3591	org/telegram/PhoneFormat/PhoneFormat:getInstance	()Lorg/telegram/PhoneFormat/PhoneFormat;
    //   10212: aload_2
    //   10213: invokevirtual 3594	org/telegram/PhoneFormat/PhoneFormat:format	(Ljava/lang/String;)Ljava/lang/String;
    //   10216: astore_2
    //   10217: aload_1
    //   10218: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10221: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   10224: getfield 3595	org/telegram/tgnet/TLRPC$MessageMedia:first_name	Ljava/lang/String;
    //   10227: aload_1
    //   10228: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10231: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   10234: getfield 3596	org/telegram/tgnet/TLRPC$MessageMedia:last_name	Ljava/lang/String;
    //   10237: invokestatic 2516	org/telegram/messenger/ContactsController:formatName	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   10240: bipush 10
    //   10242: bipush 32
    //   10244: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   10247: astore 38
    //   10249: aload 38
    //   10251: astore 37
    //   10253: aload 38
    //   10255: invokeinterface 2192 1 0
    //   10260: ifne +6 -> 10266
    //   10263: aload_2
    //   10264: astore 37
    //   10266: aload_0
    //   10267: new 350	android/text/StaticLayout
    //   10270: dup
    //   10271: aload 37
    //   10273: getstatic 2077	org/telegram/ui/ActionBar/Theme:chat_contactNamePaint	Landroid/text/TextPaint;
    //   10276: iload 13
    //   10278: i2f
    //   10279: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   10282: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   10285: getstatic 2077	org/telegram/ui/ActionBar/Theme:chat_contactNamePaint	Landroid/text/TextPaint;
    //   10288: iload 13
    //   10290: fconst_2
    //   10291: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10294: iadd
    //   10295: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   10298: fconst_1
    //   10299: fconst_0
    //   10300: iconst_0
    //   10301: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   10304: putfield 1595	org/telegram/ui/Cells/ChatMessageCell:titleLayout	Landroid/text/StaticLayout;
    //   10307: aload_0
    //   10308: new 350	android/text/StaticLayout
    //   10311: dup
    //   10312: aload_2
    //   10313: bipush 10
    //   10315: bipush 32
    //   10317: invokevirtual 1208	java/lang/String:replace	(CC)Ljava/lang/String;
    //   10320: getstatic 2019	org/telegram/ui/ActionBar/Theme:chat_contactPhonePaint	Landroid/text/TextPaint;
    //   10323: iload 13
    //   10325: i2f
    //   10326: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   10329: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   10332: getstatic 2019	org/telegram/ui/ActionBar/Theme:chat_contactPhonePaint	Landroid/text/TextPaint;
    //   10335: iload 13
    //   10337: fconst_2
    //   10338: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10341: iadd
    //   10342: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   10345: fconst_1
    //   10346: fconst_0
    //   10347: iconst_0
    //   10348: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   10351: putfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   10354: aload_0
    //   10355: aload_1
    //   10356: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   10359: aload_0
    //   10360: getfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   10363: ifeq +267 -> 10630
    //   10366: aload_1
    //   10367: invokevirtual 2626	org/telegram/messenger/MessageObject:needDrawForwarded	()Z
    //   10370: ifeq +260 -> 10630
    //   10373: aload_0
    //   10374: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   10377: ifnull +13 -> 10390
    //   10380: aload_0
    //   10381: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   10384: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   10387: ifne +243 -> 10630
    //   10390: aload_0
    //   10391: aload_0
    //   10392: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10395: ldc_w 1775
    //   10398: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10401: iadd
    //   10402: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10405: aload_0
    //   10406: ldc_w 3597
    //   10409: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10412: aload_0
    //   10413: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10416: iadd
    //   10417: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   10420: aload_0
    //   10421: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   10424: ifeq +16 -> 10440
    //   10427: aload_0
    //   10428: aload_0
    //   10429: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10432: fconst_1
    //   10433: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10436: isub
    //   10437: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10440: iload 31
    //   10442: istore 19
    //   10444: aload_0
    //   10445: getfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   10448: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   10451: ifle -4553 -> 5898
    //   10454: iload 31
    //   10456: istore 19
    //   10458: aload_0
    //   10459: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10462: ldc_w 3583
    //   10465: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10468: isub
    //   10469: aload_0
    //   10470: getfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   10473: iconst_0
    //   10474: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   10477: f2d
    //   10478: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   10481: d2i
    //   10482: isub
    //   10483: aload_0
    //   10484: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   10487: if_icmpge -4589 -> 5898
    //   10490: aload_0
    //   10491: aload_0
    //   10492: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   10495: ldc_w 1629
    //   10498: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10501: iadd
    //   10502: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   10505: iload 31
    //   10507: istore 19
    //   10509: goto -4611 -> 5898
    //   10512: ldc_w 1074
    //   10515: fstore 11
    //   10517: goto -490 -> 10027
    //   10520: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   10523: getfield 2253	android/graphics/Point:x	I
    //   10526: istore 13
    //   10528: aload_0
    //   10529: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   10532: ifeq +46 -> 10578
    //   10535: aload_1
    //   10536: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   10539: ifeq +39 -> 10578
    //   10542: aload_1
    //   10543: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10546: ifne +32 -> 10578
    //   10549: ldc_w 3559
    //   10552: fstore 11
    //   10554: aload_0
    //   10555: iload 13
    //   10557: fload 11
    //   10559: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10562: isub
    //   10563: ldc_w 3560
    //   10566: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10569: invokestatic 1195	java/lang/Math:min	(II)I
    //   10572: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10575: goto -527 -> 10048
    //   10578: ldc_w 1074
    //   10581: fstore 11
    //   10583: goto -29 -> 10554
    //   10586: getstatic 3601	org/telegram/ui/ActionBar/Theme:chat_contactDrawable	[Landroid/graphics/drawable/Drawable;
    //   10589: astore 37
    //   10591: aload_1
    //   10592: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10595: ifeq +16 -> 10611
    //   10598: iconst_1
    //   10599: istore 14
    //   10601: aload 37
    //   10603: iload 14
    //   10605: aaload
    //   10606: astore 37
    //   10608: goto -434 -> 10174
    //   10611: iconst_0
    //   10612: istore 14
    //   10614: goto -13 -> 10601
    //   10617: ldc_w 3603
    //   10620: ldc_w 3604
    //   10623: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   10626: astore_2
    //   10627: goto -410 -> 10217
    //   10630: aload_0
    //   10631: getfield 2563	org/telegram/ui/Cells/ChatMessageCell:drawNameLayout	Z
    //   10634: ifeq -229 -> 10405
    //   10637: aload_1
    //   10638: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   10641: getfield 1067	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   10644: ifne -239 -> 10405
    //   10647: aload_0
    //   10648: aload_0
    //   10649: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10652: ldc_w 616
    //   10655: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10658: iadd
    //   10659: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10662: goto -257 -> 10405
    //   10665: aload_1
    //   10666: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   10669: iconst_2
    //   10670: if_icmpne +201 -> 10871
    //   10673: aload_0
    //   10674: iconst_1
    //   10675: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   10678: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   10681: ifeq +124 -> 10805
    //   10684: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   10687: istore 13
    //   10689: aload_0
    //   10690: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   10693: ifeq +104 -> 10797
    //   10696: aload_1
    //   10697: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   10700: ifeq +97 -> 10797
    //   10703: aload_1
    //   10704: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10707: ifne +90 -> 10797
    //   10710: ldc_w 3559
    //   10713: fstore 11
    //   10715: aload_0
    //   10716: iload 13
    //   10718: fload 11
    //   10720: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10723: isub
    //   10724: ldc_w 3560
    //   10727: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10730: invokestatic 1195	java/lang/Math:min	(II)I
    //   10733: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10736: aload_0
    //   10737: aload_0
    //   10738: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10741: aload_1
    //   10742: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   10745: pop
    //   10746: aload_0
    //   10747: aload_1
    //   10748: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   10751: aload_0
    //   10752: ldc_w 3597
    //   10755: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10758: aload_0
    //   10759: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10762: iadd
    //   10763: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   10766: iload 31
    //   10768: istore 19
    //   10770: aload_0
    //   10771: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   10774: ifeq -4876 -> 5898
    //   10777: aload_0
    //   10778: aload_0
    //   10779: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10782: fconst_1
    //   10783: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10786: isub
    //   10787: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10790: iload 31
    //   10792: istore 19
    //   10794: goto -4896 -> 5898
    //   10797: ldc_w 1074
    //   10800: fstore 11
    //   10802: goto -87 -> 10715
    //   10805: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   10808: getfield 2253	android/graphics/Point:x	I
    //   10811: istore 13
    //   10813: aload_0
    //   10814: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   10817: ifeq +46 -> 10863
    //   10820: aload_1
    //   10821: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   10824: ifeq +39 -> 10863
    //   10827: aload_1
    //   10828: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10831: ifne +32 -> 10863
    //   10834: ldc_w 3559
    //   10837: fstore 11
    //   10839: aload_0
    //   10840: iload 13
    //   10842: fload 11
    //   10844: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10847: isub
    //   10848: ldc_w 3560
    //   10851: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10854: invokestatic 1195	java/lang/Math:min	(II)I
    //   10857: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10860: goto -124 -> 10736
    //   10863: ldc_w 1074
    //   10866: fstore 11
    //   10868: goto -29 -> 10839
    //   10871: aload_1
    //   10872: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   10875: bipush 14
    //   10877: if_icmpne +196 -> 11073
    //   10880: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   10883: ifeq +124 -> 11007
    //   10886: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   10889: istore 13
    //   10891: aload_0
    //   10892: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   10895: ifeq +104 -> 10999
    //   10898: aload_1
    //   10899: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   10902: ifeq +97 -> 10999
    //   10905: aload_1
    //   10906: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   10909: ifne +90 -> 10999
    //   10912: ldc_w 3559
    //   10915: fstore 11
    //   10917: aload_0
    //   10918: iload 13
    //   10920: fload 11
    //   10922: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10925: isub
    //   10926: ldc_w 3560
    //   10929: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10932: invokestatic 1195	java/lang/Math:min	(II)I
    //   10935: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10938: aload_0
    //   10939: aload_0
    //   10940: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   10943: aload_1
    //   10944: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   10947: pop
    //   10948: aload_0
    //   10949: aload_1
    //   10950: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   10953: aload_0
    //   10954: ldc_w 566
    //   10957: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10960: aload_0
    //   10961: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10964: iadd
    //   10965: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   10968: iload 31
    //   10970: istore 19
    //   10972: aload_0
    //   10973: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   10976: ifeq -5078 -> 5898
    //   10979: aload_0
    //   10980: aload_0
    //   10981: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10984: fconst_1
    //   10985: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   10988: isub
    //   10989: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   10992: iload 31
    //   10994: istore 19
    //   10996: goto -5098 -> 5898
    //   10999: ldc_w 1074
    //   11002: fstore 11
    //   11004: goto -87 -> 10917
    //   11007: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   11010: getfield 2253	android/graphics/Point:x	I
    //   11013: istore 13
    //   11015: aload_0
    //   11016: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   11019: ifeq +46 -> 11065
    //   11022: aload_1
    //   11023: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   11026: ifeq +39 -> 11065
    //   11029: aload_1
    //   11030: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   11033: ifne +32 -> 11065
    //   11036: ldc_w 3559
    //   11039: fstore 11
    //   11041: aload_0
    //   11042: iload 13
    //   11044: fload 11
    //   11046: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11049: isub
    //   11050: ldc_w 3560
    //   11053: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11056: invokestatic 1195	java/lang/Math:min	(II)I
    //   11059: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11062: goto -124 -> 10938
    //   11065: ldc_w 1074
    //   11068: fstore 11
    //   11070: goto -29 -> 11041
    //   11073: aload_1
    //   11074: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   11077: getfield 971	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   11080: ifnull +753 -> 11833
    //   11083: aload_1
    //   11084: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11087: bipush 13
    //   11089: if_icmpeq +744 -> 11833
    //   11092: iconst_1
    //   11093: istore_3
    //   11094: aload_0
    //   11095: iload_3
    //   11096: putfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   11099: aload_1
    //   11100: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11103: bipush 9
    //   11105: if_icmpeq +733 -> 11838
    //   11108: iconst_1
    //   11109: istore_3
    //   11110: aload_0
    //   11111: iload_3
    //   11112: putfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   11115: aload_0
    //   11116: iconst_1
    //   11117: putfield 783	org/telegram/ui/Cells/ChatMessageCell:drawImageButton	Z
    //   11120: aload_0
    //   11121: iconst_1
    //   11122: putfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   11125: iconst_0
    //   11126: istore 16
    //   11128: iconst_0
    //   11129: istore 19
    //   11131: iconst_0
    //   11132: istore 14
    //   11134: iconst_0
    //   11135: istore 13
    //   11137: iconst_0
    //   11138: istore 26
    //   11140: iconst_0
    //   11141: istore 27
    //   11143: iconst_0
    //   11144: istore 17
    //   11146: aload_1
    //   11147: getfield 882	org/telegram/messenger/MessageObject:gifState	F
    //   11150: fconst_2
    //   11151: fcmpl
    //   11152: ifeq +31 -> 11183
    //   11155: getstatic 833	org/telegram/messenger/SharedConfig:autoplayGifs	Z
    //   11158: ifne +25 -> 11183
    //   11161: aload_1
    //   11162: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11165: bipush 8
    //   11167: if_icmpeq +11 -> 11178
    //   11170: aload_1
    //   11171: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11174: iconst_5
    //   11175: if_icmpne +8 -> 11183
    //   11178: aload_1
    //   11179: fconst_1
    //   11180: putfield 882	org/telegram/messenger/MessageObject:gifState	F
    //   11183: aload_1
    //   11184: invokevirtual 1416	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   11187: ifeq +661 -> 11848
    //   11190: aload_0
    //   11191: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   11194: iconst_1
    //   11195: invokevirtual 3303	org/telegram/messenger/ImageReceiver:setAllowDecodeSingleFrame	(Z)V
    //   11198: aload_0
    //   11199: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   11202: astore_2
    //   11203: invokestatic 863	org/telegram/messenger/MediaController:getInstance	()Lorg/telegram/messenger/MediaController;
    //   11206: invokevirtual 2742	org/telegram/messenger/MediaController:getPlayingMessageObject	()Lorg/telegram/messenger/MessageObject;
    //   11209: ifnonnull +634 -> 11843
    //   11212: iconst_1
    //   11213: istore_3
    //   11214: aload_2
    //   11215: iload_3
    //   11216: invokevirtual 885	org/telegram/messenger/ImageReceiver:setAllowStartAnimation	(Z)V
    //   11219: aload_0
    //   11220: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   11223: aload_1
    //   11224: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   11227: invokevirtual 3607	org/telegram/messenger/ImageReceiver:setForcePreview	(Z)V
    //   11230: aload_1
    //   11231: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11234: bipush 9
    //   11236: if_icmpne +767 -> 12003
    //   11239: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   11242: ifeq +643 -> 11885
    //   11245: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   11248: istore 13
    //   11250: aload_0
    //   11251: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   11254: ifeq +623 -> 11877
    //   11257: aload_1
    //   11258: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   11261: ifeq +616 -> 11877
    //   11264: aload_1
    //   11265: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   11268: ifne +609 -> 11877
    //   11271: ldc_w 3559
    //   11274: fstore 11
    //   11276: aload_0
    //   11277: iload 13
    //   11279: fload 11
    //   11281: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11284: isub
    //   11285: ldc_w 3560
    //   11288: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11291: invokestatic 1195	java/lang/Math:min	(II)I
    //   11294: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11297: aload_0
    //   11298: aload_1
    //   11299: invokespecial 3293	org/telegram/ui/Cells/ChatMessageCell:checkNeedDrawShareButton	(Lorg/telegram/messenger/MessageObject;)Z
    //   11302: ifeq +18 -> 11320
    //   11305: aload_0
    //   11306: aload_0
    //   11307: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11310: ldc_w 1077
    //   11313: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11316: isub
    //   11317: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11320: aload_0
    //   11321: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11324: ldc_w 3608
    //   11327: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11330: isub
    //   11331: istore 14
    //   11333: aload_0
    //   11334: iload 14
    //   11336: aload_1
    //   11337: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   11340: pop
    //   11341: iload 14
    //   11343: istore 13
    //   11345: aload_1
    //   11346: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   11349: invokestatic 847	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   11352: ifne +14 -> 11366
    //   11355: iload 14
    //   11357: ldc_w 1201
    //   11360: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11363: iadd
    //   11364: istore 13
    //   11366: aload_0
    //   11367: getfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   11370: ifeq +581 -> 11951
    //   11373: ldc_w 1201
    //   11376: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11379: istore 15
    //   11381: ldc_w 1201
    //   11384: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11387: istore 16
    //   11389: aload_0
    //   11390: iload 13
    //   11392: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   11395: iload 17
    //   11397: istore 20
    //   11399: iload 18
    //   11401: istore 19
    //   11403: iload 16
    //   11405: istore 13
    //   11407: iload 15
    //   11409: istore 14
    //   11411: aload_0
    //   11412: getfield 724	org/telegram/ui/Cells/ChatMessageCell:drawPhotoImage	Z
    //   11415: ifne +131 -> 11546
    //   11418: iload 17
    //   11420: istore 20
    //   11422: iload 18
    //   11424: istore 19
    //   11426: iload 16
    //   11428: istore 13
    //   11430: iload 15
    //   11432: istore 14
    //   11434: aload_1
    //   11435: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   11438: invokestatic 847	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   11441: ifeq +105 -> 11546
    //   11444: iload 17
    //   11446: istore 20
    //   11448: iload 18
    //   11450: istore 19
    //   11452: iload 16
    //   11454: istore 13
    //   11456: iload 15
    //   11458: istore 14
    //   11460: aload_0
    //   11461: getfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   11464: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   11467: ifle +79 -> 11546
    //   11470: aload_0
    //   11471: aload_1
    //   11472: invokespecial 1191	org/telegram/ui/Cells/ChatMessageCell:measureTime	(Lorg/telegram/messenger/MessageObject;)V
    //   11475: iload 17
    //   11477: istore 20
    //   11479: iload 18
    //   11481: istore 19
    //   11483: iload 16
    //   11485: istore 13
    //   11487: iload 15
    //   11489: istore 14
    //   11491: aload_0
    //   11492: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11495: ldc_w 3304
    //   11498: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11501: isub
    //   11502: aload_0
    //   11503: getfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   11506: iconst_0
    //   11507: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   11510: f2d
    //   11511: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   11514: d2i
    //   11515: isub
    //   11516: aload_0
    //   11517: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   11520: if_icmpge +26 -> 11546
    //   11523: iload 16
    //   11525: ldc_w 1629
    //   11528: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11531: iadd
    //   11532: istore 13
    //   11534: iload 15
    //   11536: istore 14
    //   11538: iload 18
    //   11540: istore 19
    //   11542: iload 17
    //   11544: istore 20
    //   11546: aload_0
    //   11547: aload_1
    //   11548: invokespecial 3338	org/telegram/ui/Cells/ChatMessageCell:setMessageObjectInternal	(Lorg/telegram/messenger/MessageObject;)V
    //   11551: aload_0
    //   11552: getfield 2431	org/telegram/ui/Cells/ChatMessageCell:drawForwardedName	Z
    //   11555: ifeq +6541 -> 18096
    //   11558: aload_1
    //   11559: invokevirtual 2626	org/telegram/messenger/MessageObject:needDrawForwarded	()Z
    //   11562: ifeq +6534 -> 18096
    //   11565: aload_0
    //   11566: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11569: ifnull +13 -> 11582
    //   11572: aload_0
    //   11573: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11576: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   11579: ifne +6517 -> 18096
    //   11582: aload_1
    //   11583: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   11586: iconst_5
    //   11587: if_icmpeq +18 -> 11605
    //   11590: aload_0
    //   11591: aload_0
    //   11592: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11595: ldc_w 1775
    //   11598: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11601: iadd
    //   11602: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11605: aload_0
    //   11606: ldc_w 478
    //   11609: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11612: iload 13
    //   11614: iadd
    //   11615: aload_0
    //   11616: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11619: iadd
    //   11620: iload 20
    //   11622: iadd
    //   11623: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   11626: aload_0
    //   11627: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11630: ifnull +31 -> 11661
    //   11633: aload_0
    //   11634: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11637: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   11640: bipush 8
    //   11642: iand
    //   11643: ifne +18 -> 11661
    //   11646: aload_0
    //   11647: aload_0
    //   11648: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   11651: ldc_w 1545
    //   11654: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11657: isub
    //   11658: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   11661: iconst_0
    //   11662: istore 18
    //   11664: iconst_0
    //   11665: istore 16
    //   11667: iload 13
    //   11669: istore 15
    //   11671: iload 14
    //   11673: istore 17
    //   11675: aload_0
    //   11676: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11679: ifnull +101 -> 11780
    //   11682: iload 14
    //   11684: aload_0
    //   11685: aload_0
    //   11686: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11689: invokespecial 3610	org/telegram/ui/Cells/ChatMessageCell:getAdditionalWidthForPosition	(Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;)I
    //   11692: iadd
    //   11693: istore 20
    //   11695: iload 16
    //   11697: istore 14
    //   11699: iload 13
    //   11701: istore 16
    //   11703: aload_0
    //   11704: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11707: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   11710: iconst_4
    //   11711: iand
    //   11712: ifne +24 -> 11736
    //   11715: iload 13
    //   11717: ldc_w 1078
    //   11720: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11723: iadd
    //   11724: istore 16
    //   11726: iconst_0
    //   11727: ldc_w 1078
    //   11730: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11733: isub
    //   11734: istore 14
    //   11736: iload 14
    //   11738: istore 18
    //   11740: iload 16
    //   11742: istore 15
    //   11744: iload 20
    //   11746: istore 17
    //   11748: aload_0
    //   11749: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   11752: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   11755: bipush 8
    //   11757: iand
    //   11758: ifne +22 -> 11780
    //   11761: iload 16
    //   11763: ldc_w 1078
    //   11766: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11769: iadd
    //   11770: istore 15
    //   11772: iload 20
    //   11774: istore 17
    //   11776: iload 14
    //   11778: istore 18
    //   11780: aload_0
    //   11781: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   11784: ifeq +16 -> 11800
    //   11787: aload_0
    //   11788: aload_0
    //   11789: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11792: fconst_1
    //   11793: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11796: isub
    //   11797: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11800: aload_0
    //   11801: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   11804: iconst_0
    //   11805: ldc_w 616
    //   11808: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11811: aload_0
    //   11812: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   11815: iadd
    //   11816: iload 18
    //   11818: iadd
    //   11819: iload 17
    //   11821: iload 15
    //   11823: invokevirtual 1581	org/telegram/messenger/ImageReceiver:setImageCoords	(IIII)V
    //   11826: aload_0
    //   11827: invokevirtual 536	org/telegram/ui/Cells/ChatMessageCell:invalidate	()V
    //   11830: goto -5932 -> 5898
    //   11833: iconst_0
    //   11834: istore_3
    //   11835: goto -741 -> 11094
    //   11838: iconst_0
    //   11839: istore_3
    //   11840: goto -730 -> 11110
    //   11843: iconst_0
    //   11844: istore_3
    //   11845: goto -631 -> 11214
    //   11848: aload_0
    //   11849: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   11852: astore_2
    //   11853: aload_1
    //   11854: getfield 882	org/telegram/messenger/MessageObject:gifState	F
    //   11857: fconst_0
    //   11858: fcmpl
    //   11859: ifne +13 -> 11872
    //   11862: iconst_1
    //   11863: istore_3
    //   11864: aload_2
    //   11865: iload_3
    //   11866: invokevirtual 885	org/telegram/messenger/ImageReceiver:setAllowStartAnimation	(Z)V
    //   11869: goto -650 -> 11219
    //   11872: iconst_0
    //   11873: istore_3
    //   11874: goto -10 -> 11864
    //   11877: ldc_w 1074
    //   11880: fstore 11
    //   11882: goto -606 -> 11276
    //   11885: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   11888: getfield 2253	android/graphics/Point:x	I
    //   11891: istore 13
    //   11893: aload_0
    //   11894: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   11897: ifeq +46 -> 11943
    //   11900: aload_1
    //   11901: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   11904: ifeq +39 -> 11943
    //   11907: aload_1
    //   11908: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   11911: ifne +32 -> 11943
    //   11914: ldc_w 3559
    //   11917: fstore 11
    //   11919: aload_0
    //   11920: iload 13
    //   11922: fload 11
    //   11924: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11927: isub
    //   11928: ldc_w 3560
    //   11931: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11934: invokestatic 1195	java/lang/Math:min	(II)I
    //   11937: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   11940: goto -643 -> 11297
    //   11943: ldc_w 1074
    //   11946: fstore 11
    //   11948: goto -29 -> 11919
    //   11951: ldc_w 1796
    //   11954: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11957: istore 15
    //   11959: ldc_w 1796
    //   11962: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11965: istore 16
    //   11967: aload_1
    //   11968: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   11971: invokestatic 847	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   11974: ifeq +21 -> 11995
    //   11977: ldc_w 1946
    //   11980: fstore 11
    //   11982: iload 13
    //   11984: fload 11
    //   11986: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   11989: iadd
    //   11990: istore 13
    //   11992: goto -603 -> 11389
    //   11995: ldc_w 367
    //   11998: fstore 11
    //   12000: goto -18 -> 11982
    //   12003: aload_1
    //   12004: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   12007: iconst_4
    //   12008: if_icmpne +1321 -> 13329
    //   12011: aload_1
    //   12012: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12015: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12018: getfield 2298	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   12021: getfield 2304	org/telegram/tgnet/TLRPC$GeoPoint:lat	D
    //   12024: dstore 7
    //   12026: aload_1
    //   12027: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12030: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12033: getfield 2298	org/telegram/tgnet/TLRPC$MessageMedia:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
    //   12036: getfield 2307	org/telegram/tgnet/TLRPC$GeoPoint:_long	D
    //   12039: dstore 5
    //   12041: aload_1
    //   12042: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12045: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12048: instanceof 1952
    //   12051: ifeq +753 -> 12804
    //   12054: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   12057: ifeq +598 -> 12655
    //   12060: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   12063: istore 13
    //   12065: aload_0
    //   12066: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   12069: ifeq +578 -> 12647
    //   12072: aload_1
    //   12073: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   12076: ifeq +571 -> 12647
    //   12079: aload_1
    //   12080: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   12083: ifne +564 -> 12647
    //   12086: ldc_w 3559
    //   12089: fstore 11
    //   12091: aload_0
    //   12092: iload 13
    //   12094: fload 11
    //   12096: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12099: isub
    //   12100: ldc_w 3611
    //   12103: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12106: invokestatic 1195	java/lang/Math:min	(II)I
    //   12109: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12112: aload_0
    //   12113: aload_1
    //   12114: invokespecial 3293	org/telegram/ui/Cells/ChatMessageCell:checkNeedDrawShareButton	(Lorg/telegram/messenger/MessageObject;)Z
    //   12117: ifeq +18 -> 12135
    //   12120: aload_0
    //   12121: aload_0
    //   12122: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12125: ldc_w 1077
    //   12128: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12131: isub
    //   12132: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12135: aload_0
    //   12136: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12139: ldc_w 2025
    //   12142: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12145: isub
    //   12146: istore 13
    //   12148: aload_0
    //   12149: iload 13
    //   12151: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   12154: iload 13
    //   12156: ldc_w 3612
    //   12159: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12162: isub
    //   12163: istore 13
    //   12165: aload_0
    //   12166: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12169: ldc_w 367
    //   12172: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12175: isub
    //   12176: istore 16
    //   12178: ldc_w 2308
    //   12181: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12184: istore 15
    //   12186: ldc_w 2309
    //   12189: i2d
    //   12190: ldc2_w 2310
    //   12193: ddiv
    //   12194: dstore 9
    //   12196: ldc2_w 2312
    //   12199: ldc2_w 2314
    //   12202: ldc_w 2309
    //   12205: i2d
    //   12206: dconst_1
    //   12207: ldc2_w 2310
    //   12210: dload 7
    //   12212: dmul
    //   12213: ldc2_w 2316
    //   12216: ddiv
    //   12217: invokestatic 2320	java/lang/Math:sin	(D)D
    //   12220: dadd
    //   12221: dconst_1
    //   12222: ldc2_w 2310
    //   12225: dload 7
    //   12227: dmul
    //   12228: ldc2_w 2316
    //   12231: ddiv
    //   12232: invokestatic 2320	java/lang/Math:sin	(D)D
    //   12235: dsub
    //   12236: ddiv
    //   12237: invokestatic 2323	java/lang/Math:log	(D)D
    //   12240: dload 9
    //   12242: dmul
    //   12243: ldc2_w 2314
    //   12246: ddiv
    //   12247: dsub
    //   12248: invokestatic 2327	java/lang/Math:round	(D)J
    //   12251: ldc_w 2328
    //   12254: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12257: bipush 6
    //   12259: ishl
    //   12260: i2l
    //   12261: lsub
    //   12262: l2d
    //   12263: ldc_w 2309
    //   12266: i2d
    //   12267: dsub
    //   12268: dload 9
    //   12270: ddiv
    //   12271: invokestatic 2331	java/lang/Math:exp	(D)D
    //   12274: invokestatic 2334	java/lang/Math:atan	(D)D
    //   12277: dmul
    //   12278: dsub
    //   12279: ldc2_w 2316
    //   12282: dmul
    //   12283: ldc2_w 2310
    //   12286: ddiv
    //   12287: dstore 7
    //   12289: aload_0
    //   12290: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   12293: ldc_w 2342
    //   12296: iconst_5
    //   12297: anewarray 1242	java/lang/Object
    //   12300: dup
    //   12301: iconst_0
    //   12302: dload 7
    //   12304: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   12307: aastore
    //   12308: dup
    //   12309: iconst_1
    //   12310: dload 5
    //   12312: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   12315: aastore
    //   12316: dup
    //   12317: iconst_2
    //   12318: iload 16
    //   12320: i2f
    //   12321: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   12324: fdiv
    //   12325: f2i
    //   12326: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   12329: aastore
    //   12330: dup
    //   12331: iconst_3
    //   12332: iload 15
    //   12334: i2f
    //   12335: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   12338: fdiv
    //   12339: f2i
    //   12340: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   12343: aastore
    //   12344: dup
    //   12345: iconst_4
    //   12346: iconst_2
    //   12347: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   12350: f2d
    //   12351: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   12354: d2i
    //   12355: invokestatic 1195	java/lang/Math:min	(II)I
    //   12358: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   12361: aastore
    //   12362: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   12365: putfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   12368: aload_0
    //   12369: aload_1
    //   12370: invokespecial 918	org/telegram/ui/Cells/ChatMessageCell:isCurrentLocationTimeExpired	(Lorg/telegram/messenger/MessageObject;)Z
    //   12373: istore_3
    //   12374: aload_0
    //   12375: iload_3
    //   12376: putfield 446	org/telegram/ui/Cells/ChatMessageCell:locationExpired	Z
    //   12379: iload_3
    //   12380: ifne +341 -> 12721
    //   12383: aload_0
    //   12384: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   12387: iconst_1
    //   12388: invokevirtual 3291	org/telegram/messenger/ImageReceiver:setCrossfadeWithOldImage	(Z)V
    //   12391: aload_0
    //   12392: iconst_0
    //   12393: putfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   12396: ldc_w 1796
    //   12399: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12402: istore 17
    //   12404: aload_0
    //   12405: getfield 361	org/telegram/ui/Cells/ChatMessageCell:invalidateRunnable	Ljava/lang/Runnable;
    //   12408: ldc2_w 919
    //   12411: invokestatic 924	org/telegram/messenger/AndroidUtilities:runOnUIThread	(Ljava/lang/Runnable;J)V
    //   12414: aload_0
    //   12415: iconst_1
    //   12416: putfield 449	org/telegram/ui/Cells/ChatMessageCell:scheduledInvalidate	Z
    //   12419: aload_0
    //   12420: new 350	android/text/StaticLayout
    //   12423: dup
    //   12424: ldc_w 927
    //   12427: ldc_w 928
    //   12430: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   12433: getstatic 940	org/telegram/ui/ActionBar/Theme:chat_locationTitlePaint	Landroid/text/TextPaint;
    //   12436: iload 13
    //   12438: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   12441: fconst_1
    //   12442: fconst_0
    //   12443: iconst_0
    //   12444: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   12447: putfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   12450: aconst_null
    //   12451: astore 38
    //   12453: aconst_null
    //   12454: astore 37
    //   12456: aconst_null
    //   12457: astore_2
    //   12458: aload_0
    //   12459: invokespecial 2393	org/telegram/ui/Cells/ChatMessageCell:updateCurrentUserAndChat	()V
    //   12462: aload_0
    //   12463: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   12466: ifnull +277 -> 12743
    //   12469: aload_0
    //   12470: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   12473: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   12476: ifnull +14 -> 12490
    //   12479: aload_0
    //   12480: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   12483: getfield 2397	org/telegram/tgnet/TLRPC$User:photo	Lorg/telegram/tgnet/TLRPC$UserProfilePhoto;
    //   12486: getfield 2402	org/telegram/tgnet/TLRPC$UserProfilePhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   12489: astore_2
    //   12490: aload_0
    //   12491: getfield 404	org/telegram/ui/Cells/ChatMessageCell:contactAvatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   12494: aload_0
    //   12495: getfield 2376	org/telegram/ui/Cells/ChatMessageCell:currentUser	Lorg/telegram/tgnet/TLRPC$User;
    //   12498: invokevirtual 2543	org/telegram/ui/Components/AvatarDrawable:setInfo	(Lorg/telegram/tgnet/TLRPC$User;)V
    //   12501: aload_0
    //   12502: getfield 389	org/telegram/ui/Cells/ChatMessageCell:locationImageReceiver	Lorg/telegram/messenger/ImageReceiver;
    //   12505: aload_2
    //   12506: ldc_w 2545
    //   12509: aload_0
    //   12510: getfield 404	org/telegram/ui/Cells/ChatMessageCell:contactAvatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   12513: aconst_null
    //   12514: iconst_0
    //   12515: invokevirtual 2548	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Ljava/lang/String;I)V
    //   12518: aload_1
    //   12519: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12522: getfield 3615	org/telegram/tgnet/TLRPC$Message:edit_date	I
    //   12525: ifeq +266 -> 12791
    //   12528: aload_1
    //   12529: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12532: getfield 3615	org/telegram/tgnet/TLRPC$Message:edit_date	I
    //   12535: i2l
    //   12536: lstore 35
    //   12538: aload_0
    //   12539: new 350	android/text/StaticLayout
    //   12542: dup
    //   12543: lload 35
    //   12545: invokestatic 3618	org/telegram/messenger/LocaleController:formatLocationUpdateDate	(J)Ljava/lang/String;
    //   12548: getstatic 1955	org/telegram/ui/ActionBar/Theme:chat_locationAddressPaint	Landroid/text/TextPaint;
    //   12551: iload 13
    //   12553: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   12556: fconst_1
    //   12557: fconst_0
    //   12558: iconst_0
    //   12559: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   12562: putfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   12565: iload 17
    //   12567: istore 20
    //   12569: iload 18
    //   12571: istore 19
    //   12573: iload 15
    //   12575: istore 13
    //   12577: iload 16
    //   12579: istore 14
    //   12581: aload_0
    //   12582: getfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   12585: ifnull -1039 -> 11546
    //   12588: aload_0
    //   12589: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   12592: astore_2
    //   12593: aload_0
    //   12594: getfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   12597: astore 37
    //   12599: getstatic 3621	org/telegram/ui/ActionBar/Theme:chat_locationDrawable	[Landroid/graphics/drawable/Drawable;
    //   12602: astore 38
    //   12604: aload_1
    //   12605: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   12608: ifeq +715 -> 13323
    //   12611: iconst_1
    //   12612: istore 13
    //   12614: aload_2
    //   12615: aload 37
    //   12617: aconst_null
    //   12618: aload 38
    //   12620: iload 13
    //   12622: aaload
    //   12623: aconst_null
    //   12624: iconst_0
    //   12625: invokevirtual 3624	org/telegram/messenger/ImageReceiver:setImage	(Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Ljava/lang/String;I)V
    //   12628: iload 17
    //   12630: istore 20
    //   12632: iload 18
    //   12634: istore 19
    //   12636: iload 15
    //   12638: istore 13
    //   12640: iload 16
    //   12642: istore 14
    //   12644: goto -1098 -> 11546
    //   12647: ldc_w 1074
    //   12650: fstore 11
    //   12652: goto -561 -> 12091
    //   12655: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   12658: getfield 2253	android/graphics/Point:x	I
    //   12661: istore 13
    //   12663: aload_0
    //   12664: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   12667: ifeq +46 -> 12713
    //   12670: aload_1
    //   12671: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   12674: ifeq +39 -> 12713
    //   12677: aload_1
    //   12678: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   12681: ifne +32 -> 12713
    //   12684: ldc_w 3559
    //   12687: fstore 11
    //   12689: aload_0
    //   12690: iload 13
    //   12692: fload 11
    //   12694: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12697: isub
    //   12698: ldc_w 3611
    //   12701: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12704: invokestatic 1195	java/lang/Math:min	(II)I
    //   12707: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12710: goto -598 -> 12112
    //   12713: ldc_w 1074
    //   12716: fstore 11
    //   12718: goto -29 -> 12689
    //   12721: aload_0
    //   12722: aload_0
    //   12723: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12726: ldc_w 1705
    //   12729: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12732: isub
    //   12733: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12736: iload 14
    //   12738: istore 17
    //   12740: goto -321 -> 12419
    //   12743: aload 38
    //   12745: astore_2
    //   12746: aload_0
    //   12747: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   12750: ifnull -249 -> 12501
    //   12753: aload 37
    //   12755: astore_2
    //   12756: aload_0
    //   12757: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   12760: getfield 2439	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   12763: ifnull +14 -> 12777
    //   12766: aload_0
    //   12767: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   12770: getfield 2439	org/telegram/tgnet/TLRPC$Chat:photo	Lorg/telegram/tgnet/TLRPC$ChatPhoto;
    //   12773: getfield 2442	org/telegram/tgnet/TLRPC$ChatPhoto:photo_small	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   12776: astore_2
    //   12777: aload_0
    //   12778: getfield 404	org/telegram/ui/Cells/ChatMessageCell:contactAvatarDrawable	Lorg/telegram/ui/Components/AvatarDrawable;
    //   12781: aload_0
    //   12782: getfield 2378	org/telegram/ui/Cells/ChatMessageCell:currentChat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   12785: invokevirtual 2694	org/telegram/ui/Components/AvatarDrawable:setInfo	(Lorg/telegram/tgnet/TLRPC$Chat;)V
    //   12788: goto -287 -> 12501
    //   12791: aload_1
    //   12792: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12795: getfield 1966	org/telegram/tgnet/TLRPC$Message:date	I
    //   12798: i2l
    //   12799: lstore 35
    //   12801: goto -263 -> 12538
    //   12804: aload_1
    //   12805: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12808: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12811: getfield 2358	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   12814: invokestatic 847	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   12817: ifne +393 -> 13210
    //   12820: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   12823: ifeq +313 -> 13136
    //   12826: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   12829: istore 14
    //   12831: aload_0
    //   12832: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   12835: ifeq +293 -> 13128
    //   12838: aload_1
    //   12839: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   12842: ifeq +286 -> 13128
    //   12845: aload_1
    //   12846: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   12849: ifne +279 -> 13128
    //   12852: ldc_w 3559
    //   12855: fstore 11
    //   12857: aload_0
    //   12858: iload 14
    //   12860: fload 11
    //   12862: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12865: isub
    //   12866: ldc_w 3560
    //   12869: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12872: invokestatic 1195	java/lang/Math:min	(II)I
    //   12875: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12878: aload_0
    //   12879: aload_1
    //   12880: invokespecial 3293	org/telegram/ui/Cells/ChatMessageCell:checkNeedDrawShareButton	(Lorg/telegram/messenger/MessageObject;)Z
    //   12883: ifeq +18 -> 12901
    //   12886: aload_0
    //   12887: aload_0
    //   12888: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12891: ldc_w 1077
    //   12894: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12897: isub
    //   12898: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12901: aload_0
    //   12902: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   12905: ldc_w 3625
    //   12908: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   12911: isub
    //   12912: istore 14
    //   12914: aload_0
    //   12915: aload_1
    //   12916: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12919: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12922: getfield 2358	org/telegram/tgnet/TLRPC$MessageMedia:title	Ljava/lang/String;
    //   12925: getstatic 940	org/telegram/ui/ActionBar/Theme:chat_locationTitlePaint	Landroid/text/TextPaint;
    //   12928: iload 14
    //   12930: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   12933: fconst_1
    //   12934: fconst_0
    //   12935: iconst_0
    //   12936: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   12939: iload 14
    //   12941: iconst_2
    //   12942: invokestatic 1315	org/telegram/ui/Components/StaticLayoutEx:createStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZLandroid/text/TextUtils$TruncateAt;II)Landroid/text/StaticLayout;
    //   12945: putfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   12948: aload_0
    //   12949: getfield 951	org/telegram/ui/Cells/ChatMessageCell:docTitleLayout	Landroid/text/StaticLayout;
    //   12952: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   12955: istore 15
    //   12957: aload_1
    //   12958: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12961: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12964: getfield 3628	org/telegram/tgnet/TLRPC$MessageMedia:address	Ljava/lang/String;
    //   12967: ifnull +235 -> 13202
    //   12970: aload_1
    //   12971: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12974: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12977: getfield 3628	org/telegram/tgnet/TLRPC$MessageMedia:address	Ljava/lang/String;
    //   12980: invokevirtual 1054	java/lang/String:length	()I
    //   12983: ifle +219 -> 13202
    //   12986: aload_0
    //   12987: aload_1
    //   12988: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   12991: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   12994: getfield 3628	org/telegram/tgnet/TLRPC$MessageMedia:address	Ljava/lang/String;
    //   12997: getstatic 1955	org/telegram/ui/ActionBar/Theme:chat_locationAddressPaint	Landroid/text/TextPaint;
    //   13000: iload 14
    //   13002: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   13005: fconst_1
    //   13006: fconst_0
    //   13007: iconst_0
    //   13008: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   13011: iload 14
    //   13013: iconst_3
    //   13014: iconst_3
    //   13015: iload 15
    //   13017: isub
    //   13018: invokestatic 1195	java/lang/Math:min	(II)I
    //   13021: invokestatic 1315	org/telegram/ui/Components/StaticLayoutEx:createStaticLayout	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZLandroid/text/TextUtils$TruncateAt;II)Landroid/text/StaticLayout;
    //   13024: putfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   13027: aload_0
    //   13028: iconst_0
    //   13029: putfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   13032: aload_0
    //   13033: iload 14
    //   13035: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   13038: ldc_w 1201
    //   13041: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13044: istore 16
    //   13046: ldc_w 1201
    //   13049: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13052: istore 15
    //   13054: aload_0
    //   13055: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   13058: ldc_w 2360
    //   13061: iconst_5
    //   13062: anewarray 1242	java/lang/Object
    //   13065: dup
    //   13066: iconst_0
    //   13067: dload 7
    //   13069: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13072: aastore
    //   13073: dup
    //   13074: iconst_1
    //   13075: dload 5
    //   13077: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13080: aastore
    //   13081: dup
    //   13082: iconst_2
    //   13083: iconst_2
    //   13084: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   13087: f2d
    //   13088: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   13091: d2i
    //   13092: invokestatic 1195	java/lang/Math:min	(II)I
    //   13095: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13098: aastore
    //   13099: dup
    //   13100: iconst_3
    //   13101: dload 7
    //   13103: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13106: aastore
    //   13107: dup
    //   13108: iconst_4
    //   13109: dload 5
    //   13111: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13114: aastore
    //   13115: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   13118: putfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   13121: iload 13
    //   13123: istore 17
    //   13125: goto -560 -> 12565
    //   13128: ldc_w 1074
    //   13131: fstore 11
    //   13133: goto -276 -> 12857
    //   13136: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   13139: getfield 2253	android/graphics/Point:x	I
    //   13142: istore 14
    //   13144: aload_0
    //   13145: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   13148: ifeq +46 -> 13194
    //   13151: aload_1
    //   13152: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   13155: ifeq +39 -> 13194
    //   13158: aload_1
    //   13159: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   13162: ifne +32 -> 13194
    //   13165: ldc_w 3559
    //   13168: fstore 11
    //   13170: aload_0
    //   13171: iload 14
    //   13173: fload 11
    //   13175: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13178: isub
    //   13179: ldc_w 3560
    //   13182: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13185: invokestatic 1195	java/lang/Math:min	(II)I
    //   13188: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   13191: goto -313 -> 12878
    //   13194: ldc_w 1074
    //   13197: fstore 11
    //   13199: goto -29 -> 13170
    //   13202: aload_0
    //   13203: aconst_null
    //   13204: putfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   13207: goto -180 -> 13027
    //   13210: aload_0
    //   13211: ldc_w 3629
    //   13214: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13217: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   13220: ldc_w 2160
    //   13223: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13226: istore 16
    //   13228: ldc_w 2568
    //   13231: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13234: istore 15
    //   13236: aload_0
    //   13237: ldc_w 554
    //   13240: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13243: iload 16
    //   13245: iadd
    //   13246: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   13249: aload_0
    //   13250: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   13253: ldc_w 2362
    //   13256: iconst_5
    //   13257: anewarray 1242	java/lang/Object
    //   13260: dup
    //   13261: iconst_0
    //   13262: dload 7
    //   13264: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13267: aastore
    //   13268: dup
    //   13269: iconst_1
    //   13270: dload 5
    //   13272: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13275: aastore
    //   13276: dup
    //   13277: iconst_2
    //   13278: iconst_2
    //   13279: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   13282: f2d
    //   13283: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   13286: d2i
    //   13287: invokestatic 1195	java/lang/Math:min	(II)I
    //   13290: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13293: aastore
    //   13294: dup
    //   13295: iconst_3
    //   13296: dload 7
    //   13298: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13301: aastore
    //   13302: dup
    //   13303: iconst_4
    //   13304: dload 5
    //   13306: invokestatic 2347	java/lang/Double:valueOf	(D)Ljava/lang/Double;
    //   13309: aastore
    //   13310: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   13313: putfield 2294	org/telegram/ui/Cells/ChatMessageCell:currentUrl	Ljava/lang/String;
    //   13316: iload 13
    //   13318: istore 17
    //   13320: goto -755 -> 12565
    //   13323: iconst_0
    //   13324: istore 13
    //   13326: goto -712 -> 12614
    //   13329: aload_1
    //   13330: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   13333: bipush 13
    //   13335: if_icmpne +544 -> 13879
    //   13338: aload_0
    //   13339: iconst_0
    //   13340: putfield 346	org/telegram/ui/Cells/ChatMessageCell:drawBackground	Z
    //   13343: iconst_0
    //   13344: istore 15
    //   13346: iload 19
    //   13348: istore 14
    //   13350: iload 16
    //   13352: istore 13
    //   13354: iload 15
    //   13356: aload_1
    //   13357: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13360: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13363: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13366: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   13369: invokevirtual 590	java/util/ArrayList:size	()I
    //   13372: if_icmpge +44 -> 13416
    //   13375: aload_1
    //   13376: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13379: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13382: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13385: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   13388: iload 15
    //   13390: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   13393: checkcast 1161	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   13396: astore_2
    //   13397: aload_2
    //   13398: instanceof 3503
    //   13401: ifeq +281 -> 13682
    //   13404: aload_2
    //   13405: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   13408: istore 13
    //   13410: aload_2
    //   13411: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   13414: istore 14
    //   13416: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   13419: ifeq +272 -> 13691
    //   13422: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   13425: i2f
    //   13426: ldc_w 3630
    //   13429: fmul
    //   13430: fstore 11
    //   13432: fload 11
    //   13434: fstore 12
    //   13436: iload 14
    //   13438: istore 15
    //   13440: iload 13
    //   13442: istore 14
    //   13444: iload 13
    //   13446: ifne +19 -> 13465
    //   13449: fload 12
    //   13451: f2i
    //   13452: istore 15
    //   13454: iload 15
    //   13456: ldc_w 2568
    //   13459: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13462: iadd
    //   13463: istore 14
    //   13465: iload 15
    //   13467: i2f
    //   13468: fload 11
    //   13470: iload 14
    //   13472: i2f
    //   13473: fdiv
    //   13474: fmul
    //   13475: f2i
    //   13476: istore 13
    //   13478: fload 11
    //   13480: f2i
    //   13481: istore 14
    //   13483: iload 13
    //   13485: istore 16
    //   13487: iload 14
    //   13489: istore 15
    //   13491: iload 13
    //   13493: i2f
    //   13494: fload 12
    //   13496: fcmpl
    //   13497: ifle +21 -> 13518
    //   13500: iload 14
    //   13502: i2f
    //   13503: fload 12
    //   13505: iload 13
    //   13507: i2f
    //   13508: fdiv
    //   13509: fmul
    //   13510: f2i
    //   13511: istore 15
    //   13513: fload 12
    //   13515: f2i
    //   13516: istore 16
    //   13518: aload_0
    //   13519: bipush 6
    //   13521: putfield 498	org/telegram/ui/Cells/ChatMessageCell:documentAttachType	I
    //   13524: aload_0
    //   13525: iload 15
    //   13527: ldc_w 478
    //   13530: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13533: isub
    //   13534: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   13537: aload_0
    //   13538: ldc_w 554
    //   13541: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13544: iload 15
    //   13546: iadd
    //   13547: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   13550: aload_0
    //   13551: aload_1
    //   13552: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   13555: bipush 80
    //   13557: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13560: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13563: aload_1
    //   13564: getfield 2750	org/telegram/messenger/MessageObject:attachPathExists	Z
    //   13567: ifeq +158 -> 13725
    //   13570: aload_0
    //   13571: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   13574: astore 37
    //   13576: aload_1
    //   13577: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13580: getfield 3633	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   13583: astore 38
    //   13585: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   13588: ldc_w 3433
    //   13591: iconst_2
    //   13592: anewarray 1242	java/lang/Object
    //   13595: dup
    //   13596: iconst_0
    //   13597: iload 15
    //   13599: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13602: aastore
    //   13603: dup
    //   13604: iconst_1
    //   13605: iload 16
    //   13607: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13610: aastore
    //   13611: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   13614: astore 39
    //   13616: aload_0
    //   13617: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13620: ifnull +100 -> 13720
    //   13623: aload_0
    //   13624: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13627: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   13630: astore_2
    //   13631: aload 37
    //   13633: aconst_null
    //   13634: aload 38
    //   13636: aload 39
    //   13638: aconst_null
    //   13639: aload_2
    //   13640: ldc_w 3437
    //   13643: aload_1
    //   13644: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13647: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13650: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13653: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   13656: ldc_w 3521
    //   13659: iconst_1
    //   13660: invokevirtual 1362	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   13663: iload 17
    //   13665: istore 20
    //   13667: iload 18
    //   13669: istore 19
    //   13671: iload 16
    //   13673: istore 13
    //   13675: iload 15
    //   13677: istore 14
    //   13679: goto -2133 -> 11546
    //   13682: iload 15
    //   13684: iconst_1
    //   13685: iadd
    //   13686: istore 15
    //   13688: goto -342 -> 13346
    //   13691: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   13694: getfield 2253	android/graphics/Point:x	I
    //   13697: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   13700: getfield 2262	android/graphics/Point:y	I
    //   13703: invokestatic 1195	java/lang/Math:min	(II)I
    //   13706: i2f
    //   13707: ldc_w 2766
    //   13710: fmul
    //   13711: fstore 11
    //   13713: fload 11
    //   13715: fstore 12
    //   13717: goto -281 -> 13436
    //   13720: aconst_null
    //   13721: astore_2
    //   13722: goto -91 -> 13631
    //   13725: iload 17
    //   13727: istore 20
    //   13729: iload 18
    //   13731: istore 19
    //   13733: iload 16
    //   13735: istore 13
    //   13737: iload 15
    //   13739: istore 14
    //   13741: aload_1
    //   13742: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13745: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13748: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13751: getfield 3635	org/telegram/tgnet/TLRPC$Document:id	J
    //   13754: lconst_0
    //   13755: lcmp
    //   13756: ifeq -2210 -> 11546
    //   13759: aload_0
    //   13760: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   13763: astore 37
    //   13765: aload_1
    //   13766: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13769: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13772: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13775: astore 38
    //   13777: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   13780: ldc_w 3433
    //   13783: iconst_2
    //   13784: anewarray 1242	java/lang/Object
    //   13787: dup
    //   13788: iconst_0
    //   13789: iload 15
    //   13791: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13794: aastore
    //   13795: dup
    //   13796: iconst_1
    //   13797: iload 16
    //   13799: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   13802: aastore
    //   13803: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   13806: astore 39
    //   13808: aload_0
    //   13809: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13812: ifnull +62 -> 13874
    //   13815: aload_0
    //   13816: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   13819: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   13822: astore_2
    //   13823: aload 37
    //   13825: aload 38
    //   13827: aconst_null
    //   13828: aload 39
    //   13830: aconst_null
    //   13831: aload_2
    //   13832: ldc_w 3437
    //   13835: aload_1
    //   13836: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   13839: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   13842: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   13845: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   13848: ldc_w 3521
    //   13851: iconst_1
    //   13852: invokevirtual 1362	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   13855: iload 17
    //   13857: istore 20
    //   13859: iload 18
    //   13861: istore 19
    //   13863: iload 16
    //   13865: istore 13
    //   13867: iload 15
    //   13869: istore 14
    //   13871: goto -2325 -> 11546
    //   13874: aconst_null
    //   13875: astore_2
    //   13876: goto -53 -> 13823
    //   13879: aload_1
    //   13880: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   13883: iconst_5
    //   13884: if_icmpne +675 -> 14559
    //   13887: getstatic 1846	org/telegram/messenger/AndroidUtilities:roundMessageSize	I
    //   13890: istore 14
    //   13892: iload 14
    //   13894: istore 13
    //   13896: iload 14
    //   13898: ldc_w 2568
    //   13901: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13904: iadd
    //   13905: istore 16
    //   13907: iload 13
    //   13909: istore 18
    //   13911: iload 14
    //   13913: istore 15
    //   13915: aload_1
    //   13916: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   13919: iconst_5
    //   13920: if_icmpeq +41 -> 13961
    //   13923: iload 13
    //   13925: istore 18
    //   13927: iload 14
    //   13929: istore 15
    //   13931: aload_0
    //   13932: aload_1
    //   13933: invokespecial 3293	org/telegram/ui/Cells/ChatMessageCell:checkNeedDrawShareButton	(Lorg/telegram/messenger/MessageObject;)Z
    //   13936: ifeq +25 -> 13961
    //   13939: iload 13
    //   13941: ldc_w 1077
    //   13944: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13947: isub
    //   13948: istore 18
    //   13950: iload 14
    //   13952: ldc_w 1077
    //   13955: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   13958: isub
    //   13959: istore 15
    //   13961: iload 15
    //   13963: istore 19
    //   13965: iload 15
    //   13967: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   13970: if_icmple +8 -> 13978
    //   13973: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   13976: istore 19
    //   13978: iload 16
    //   13980: istore 15
    //   13982: iload 16
    //   13984: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   13987: if_icmple +8 -> 13995
    //   13990: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   13993: istore 15
    //   13995: aload_1
    //   13996: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   13999: iconst_1
    //   14000: if_icmpne +613 -> 14613
    //   14003: aload_0
    //   14004: aload_1
    //   14005: invokespecial 1763	org/telegram/ui/Cells/ChatMessageCell:updateSecretTimeText	(Lorg/telegram/messenger/MessageObject;)V
    //   14008: aload_0
    //   14009: aload_1
    //   14010: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   14013: bipush 80
    //   14015: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14018: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14021: aload_0
    //   14022: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   14025: ifnonnull +15 -> 14040
    //   14028: aload_1
    //   14029: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   14032: ifnull +8 -> 14040
    //   14035: aload_0
    //   14036: iconst_0
    //   14037: putfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   14040: aload_0
    //   14041: aload_1
    //   14042: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   14045: invokestatic 1339	org/telegram/messenger/AndroidUtilities:getPhotoSize	()I
    //   14048: invokestatic 1343	org/telegram/messenger/FileLoader:getClosestPhotoSizeWithSize	(Ljava/util/ArrayList;I)Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14051: putfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14054: iconst_0
    //   14055: istore 14
    //   14057: iconst_0
    //   14058: istore 13
    //   14060: aload_0
    //   14061: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14064: ifnull +19 -> 14083
    //   14067: aload_0
    //   14068: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14071: aload_0
    //   14072: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14075: if_acmpne +8 -> 14083
    //   14078: aload_0
    //   14079: aconst_null
    //   14080: putfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14083: aload_0
    //   14084: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14087: ifnull +112 -> 14199
    //   14090: aload_0
    //   14091: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14094: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   14097: i2f
    //   14098: iload 19
    //   14100: i2f
    //   14101: fdiv
    //   14102: fstore 11
    //   14104: aload_0
    //   14105: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14108: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   14111: i2f
    //   14112: fload 11
    //   14114: fdiv
    //   14115: f2i
    //   14116: istore 14
    //   14118: aload_0
    //   14119: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14122: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   14125: i2f
    //   14126: fload 11
    //   14128: fdiv
    //   14129: f2i
    //   14130: istore 13
    //   14132: iload 14
    //   14134: istore 16
    //   14136: iload 14
    //   14138: ifne +11 -> 14149
    //   14141: ldc_w 3506
    //   14144: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14147: istore 16
    //   14149: iload 13
    //   14151: istore 17
    //   14153: iload 13
    //   14155: ifne +11 -> 14166
    //   14158: ldc_w 3506
    //   14161: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14164: istore 17
    //   14166: iload 17
    //   14168: iload 15
    //   14170: if_icmple +641 -> 14811
    //   14173: iload 17
    //   14175: i2f
    //   14176: fstore 11
    //   14178: iload 15
    //   14180: istore 13
    //   14182: fload 11
    //   14184: iload 13
    //   14186: i2f
    //   14187: fdiv
    //   14188: fstore 11
    //   14190: iload 16
    //   14192: i2f
    //   14193: fload 11
    //   14195: fdiv
    //   14196: f2i
    //   14197: istore 14
    //   14199: iload 13
    //   14201: istore 16
    //   14203: iload 14
    //   14205: istore 17
    //   14207: aload_1
    //   14208: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14211: iconst_5
    //   14212: if_icmpne +12 -> 14224
    //   14215: getstatic 1846	org/telegram/messenger/AndroidUtilities:roundMessageSize	I
    //   14218: istore 16
    //   14220: iload 16
    //   14222: istore 17
    //   14224: iload 17
    //   14226: ifeq +16 -> 14242
    //   14229: iload 16
    //   14231: istore 13
    //   14233: iload 17
    //   14235: istore 14
    //   14237: iload 16
    //   14239: ifne +154 -> 14393
    //   14242: iload 16
    //   14244: istore 13
    //   14246: iload 17
    //   14248: istore 14
    //   14250: aload_1
    //   14251: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14254: bipush 8
    //   14256: if_icmpne +137 -> 14393
    //   14259: iconst_0
    //   14260: istore 20
    //   14262: iload 16
    //   14264: istore 13
    //   14266: iload 17
    //   14268: istore 14
    //   14270: iload 20
    //   14272: aload_1
    //   14273: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   14276: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   14279: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   14282: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   14285: invokevirtual 590	java/util/ArrayList:size	()I
    //   14288: if_icmpge +105 -> 14393
    //   14291: aload_1
    //   14292: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   14295: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   14298: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   14301: getfield 1159	org/telegram/tgnet/TLRPC$Document:attributes	Ljava/util/ArrayList;
    //   14304: iload 20
    //   14306: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   14309: checkcast 1161	org/telegram/tgnet/TLRPC$DocumentAttribute
    //   14312: astore_2
    //   14313: aload_2
    //   14314: instanceof 3503
    //   14317: ifne +10 -> 14327
    //   14320: aload_2
    //   14321: instanceof 1255
    //   14324: ifeq +654 -> 14978
    //   14327: aload_2
    //   14328: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   14331: i2f
    //   14332: iload 19
    //   14334: i2f
    //   14335: fdiv
    //   14336: fstore 11
    //   14338: aload_2
    //   14339: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   14342: i2f
    //   14343: fload 11
    //   14345: fdiv
    //   14346: f2i
    //   14347: istore 16
    //   14349: aload_2
    //   14350: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   14353: i2f
    //   14354: fload 11
    //   14356: fdiv
    //   14357: f2i
    //   14358: istore 17
    //   14360: iload 17
    //   14362: iload 15
    //   14364: if_icmple +535 -> 14899
    //   14367: iload 17
    //   14369: i2f
    //   14370: fstore 11
    //   14372: iload 15
    //   14374: istore 13
    //   14376: fload 11
    //   14378: iload 13
    //   14380: i2f
    //   14381: fdiv
    //   14382: fstore 11
    //   14384: iload 16
    //   14386: i2f
    //   14387: fload 11
    //   14389: fdiv
    //   14390: f2i
    //   14391: istore 14
    //   14393: iload 14
    //   14395: ifeq +12 -> 14407
    //   14398: iload 13
    //   14400: istore 15
    //   14402: iload 13
    //   14404: ifne +15 -> 14419
    //   14407: ldc_w 3506
    //   14410: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14413: istore 15
    //   14415: iload 15
    //   14417: istore 14
    //   14419: iload 14
    //   14421: istore 13
    //   14423: aload_1
    //   14424: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14427: iconst_3
    //   14428: if_icmpne +36 -> 14464
    //   14431: iload 14
    //   14433: istore 13
    //   14435: iload 14
    //   14437: aload_0
    //   14438: getfield 1268	org/telegram/ui/Cells/ChatMessageCell:infoWidth	I
    //   14441: ldc_w 2101
    //   14444: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14447: iadd
    //   14448: if_icmpge +16 -> 14464
    //   14451: aload_0
    //   14452: getfield 1268	org/telegram/ui/Cells/ChatMessageCell:infoWidth	I
    //   14455: ldc_w 2101
    //   14458: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14461: iadd
    //   14462: istore 13
    //   14464: aload_0
    //   14465: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   14468: ifnull +906 -> 15374
    //   14471: iconst_0
    //   14472: istore 16
    //   14474: aload_0
    //   14475: invokespecial 3082	org/telegram/ui/Cells/ChatMessageCell:getGroupPhotosWidth	()I
    //   14478: istore 17
    //   14480: iconst_0
    //   14481: istore 14
    //   14483: iload 14
    //   14485: aload_0
    //   14486: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   14489: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   14492: invokevirtual 590	java/util/ArrayList:size	()I
    //   14495: if_icmpge +492 -> 14987
    //   14498: aload_0
    //   14499: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   14502: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   14505: iload 14
    //   14507: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   14510: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   14513: astore_2
    //   14514: aload_2
    //   14515: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   14518: ifne +469 -> 14987
    //   14521: iload 16
    //   14523: i2d
    //   14524: aload_2
    //   14525: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   14528: aload_2
    //   14529: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   14532: iadd
    //   14533: i2f
    //   14534: ldc_w 1740
    //   14537: fdiv
    //   14538: iload 17
    //   14540: i2f
    //   14541: fmul
    //   14542: f2d
    //   14543: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   14546: dadd
    //   14547: d2i
    //   14548: istore 16
    //   14550: iload 14
    //   14552: iconst_1
    //   14553: iadd
    //   14554: istore 14
    //   14556: goto -73 -> 14483
    //   14559: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   14562: ifeq +21 -> 14583
    //   14565: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   14568: i2f
    //   14569: ldc_w 3636
    //   14572: fmul
    //   14573: f2i
    //   14574: istore 14
    //   14576: iload 14
    //   14578: istore 13
    //   14580: goto -684 -> 13896
    //   14583: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   14586: getfield 2253	android/graphics/Point:x	I
    //   14589: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   14592: getfield 2262	android/graphics/Point:y	I
    //   14595: invokestatic 1195	java/lang/Math:min	(II)I
    //   14598: i2f
    //   14599: ldc_w 3636
    //   14602: fmul
    //   14603: f2i
    //   14604: istore 14
    //   14606: iload 14
    //   14608: istore 13
    //   14610: goto -714 -> 13896
    //   14613: aload_1
    //   14614: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14617: iconst_3
    //   14618: if_icmpne +49 -> 14667
    //   14621: aload_0
    //   14622: iconst_0
    //   14623: aload_1
    //   14624: invokespecial 3508	org/telegram/ui/Cells/ChatMessageCell:createDocumentLayout	(ILorg/telegram/messenger/MessageObject;)I
    //   14627: pop
    //   14628: aload_0
    //   14629: aload_1
    //   14630: invokespecial 1763	org/telegram/ui/Cells/ChatMessageCell:updateSecretTimeText	(Lorg/telegram/messenger/MessageObject;)V
    //   14633: aload_1
    //   14634: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   14637: ifne +19 -> 14656
    //   14640: aload_0
    //   14641: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14644: iconst_1
    //   14645: invokevirtual 1348	org/telegram/messenger/ImageReceiver:setNeedsQualityThumb	(Z)V
    //   14648: aload_0
    //   14649: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14652: iconst_1
    //   14653: invokevirtual 1351	org/telegram/messenger/ImageReceiver:setShouldGenerateQualityThumb	(Z)V
    //   14656: aload_0
    //   14657: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14660: aload_1
    //   14661: invokevirtual 1354	org/telegram/messenger/ImageReceiver:setParentMessageObject	(Lorg/telegram/messenger/MessageObject;)V
    //   14664: goto -643 -> 14021
    //   14667: aload_1
    //   14668: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14671: iconst_5
    //   14672: if_icmpne +37 -> 14709
    //   14675: aload_1
    //   14676: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   14679: ifne +19 -> 14698
    //   14682: aload_0
    //   14683: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14686: iconst_1
    //   14687: invokevirtual 1348	org/telegram/messenger/ImageReceiver:setNeedsQualityThumb	(Z)V
    //   14690: aload_0
    //   14691: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14694: iconst_1
    //   14695: invokevirtual 1351	org/telegram/messenger/ImageReceiver:setShouldGenerateQualityThumb	(Z)V
    //   14698: aload_0
    //   14699: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14702: aload_1
    //   14703: invokevirtual 1354	org/telegram/messenger/ImageReceiver:setParentMessageObject	(Lorg/telegram/messenger/MessageObject;)V
    //   14706: goto -685 -> 14021
    //   14709: aload_1
    //   14710: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   14713: bipush 8
    //   14715: if_icmpne -694 -> 14021
    //   14718: aload_1
    //   14719: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   14722: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   14725: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   14728: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   14731: i2l
    //   14732: invokestatic 1263	org/telegram/messenger/AndroidUtilities:formatFileSize	(J)Ljava/lang/String;
    //   14735: astore_2
    //   14736: aload_0
    //   14737: getstatic 1266	org/telegram/ui/ActionBar/Theme:chat_infoPaint	Landroid/text/TextPaint;
    //   14740: aload_2
    //   14741: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   14744: f2d
    //   14745: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   14748: d2i
    //   14749: putfield 1268	org/telegram/ui/Cells/ChatMessageCell:infoWidth	I
    //   14752: aload_0
    //   14753: new 350	android/text/StaticLayout
    //   14756: dup
    //   14757: aload_2
    //   14758: getstatic 1266	org/telegram/ui/ActionBar/Theme:chat_infoPaint	Landroid/text/TextPaint;
    //   14761: aload_0
    //   14762: getfield 1268	org/telegram/ui/Cells/ChatMessageCell:infoWidth	I
    //   14765: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   14768: fconst_1
    //   14769: fconst_0
    //   14770: iconst_0
    //   14771: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   14774: putfield 1270	org/telegram/ui/Cells/ChatMessageCell:infoLayout	Landroid/text/StaticLayout;
    //   14777: aload_1
    //   14778: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   14781: ifne +19 -> 14800
    //   14784: aload_0
    //   14785: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14788: iconst_1
    //   14789: invokevirtual 1348	org/telegram/messenger/ImageReceiver:setNeedsQualityThumb	(Z)V
    //   14792: aload_0
    //   14793: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14796: iconst_1
    //   14797: invokevirtual 1351	org/telegram/messenger/ImageReceiver:setShouldGenerateQualityThumb	(Z)V
    //   14800: aload_0
    //   14801: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   14804: aload_1
    //   14805: invokevirtual 1354	org/telegram/messenger/ImageReceiver:setParentMessageObject	(Lorg/telegram/messenger/MessageObject;)V
    //   14808: goto -787 -> 14021
    //   14811: iload 17
    //   14813: istore 13
    //   14815: iload 16
    //   14817: istore 14
    //   14819: iload 17
    //   14821: ldc_w 3637
    //   14824: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14827: if_icmpge -628 -> 14199
    //   14830: ldc_w 3637
    //   14833: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14836: istore 17
    //   14838: aload_0
    //   14839: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14842: getfield 3501	org/telegram/tgnet/TLRPC$PhotoSize:h	I
    //   14845: i2f
    //   14846: iload 17
    //   14848: i2f
    //   14849: fdiv
    //   14850: fstore 11
    //   14852: iload 17
    //   14854: istore 13
    //   14856: iload 16
    //   14858: istore 14
    //   14860: aload_0
    //   14861: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14864: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   14867: i2f
    //   14868: fload 11
    //   14870: fdiv
    //   14871: iload 19
    //   14873: i2f
    //   14874: fcmpg
    //   14875: ifge -676 -> 14199
    //   14878: aload_0
    //   14879: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   14882: getfield 3498	org/telegram/tgnet/TLRPC$PhotoSize:w	I
    //   14885: i2f
    //   14886: fload 11
    //   14888: fdiv
    //   14889: f2i
    //   14890: istore 14
    //   14892: iload 17
    //   14894: istore 13
    //   14896: goto -697 -> 14199
    //   14899: iload 17
    //   14901: istore 13
    //   14903: iload 16
    //   14905: istore 14
    //   14907: iload 17
    //   14909: ldc_w 3637
    //   14912: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14915: if_icmpge -522 -> 14393
    //   14918: ldc_w 3637
    //   14921: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14924: istore 15
    //   14926: aload_2
    //   14927: getfield 3505	org/telegram/tgnet/TLRPC$DocumentAttribute:h	I
    //   14930: i2f
    //   14931: iload 15
    //   14933: i2f
    //   14934: fdiv
    //   14935: fstore 11
    //   14937: iload 15
    //   14939: istore 13
    //   14941: iload 16
    //   14943: istore 14
    //   14945: aload_2
    //   14946: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   14949: i2f
    //   14950: fload 11
    //   14952: fdiv
    //   14953: iload 19
    //   14955: i2f
    //   14956: fcmpg
    //   14957: ifge -564 -> 14393
    //   14960: aload_2
    //   14961: getfield 3504	org/telegram/tgnet/TLRPC$DocumentAttribute:w	I
    //   14964: i2f
    //   14965: fload 11
    //   14967: fdiv
    //   14968: f2i
    //   14969: istore 14
    //   14971: iload 15
    //   14973: istore 13
    //   14975: goto -582 -> 14393
    //   14978: iload 20
    //   14980: iconst_1
    //   14981: iadd
    //   14982: istore 20
    //   14984: goto -722 -> 14262
    //   14987: aload_0
    //   14988: iload 16
    //   14990: ldc_w 1931
    //   14993: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   14996: isub
    //   14997: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   15000: aload_1
    //   15001: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   15004: iconst_5
    //   15005: if_icmpne +35 -> 15040
    //   15008: aload_0
    //   15009: aload_0
    //   15010: getfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   15013: i2d
    //   15014: getstatic 1170	org/telegram/ui/ActionBar/Theme:chat_audioTimePaint	Landroid/text/TextPaint;
    //   15017: ldc_w 1172
    //   15020: invokevirtual 1178	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   15023: f2d
    //   15024: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15027: ldc_w 3638
    //   15030: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15033: i2d
    //   15034: dadd
    //   15035: dsub
    //   15036: d2i
    //   15037: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   15040: aload_0
    //   15041: aload_1
    //   15042: invokespecial 1191	org/telegram/ui/Cells/ChatMessageCell:measureTime	(Lorg/telegram/messenger/MessageObject;)V
    //   15045: aload_0
    //   15046: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   15049: istore 16
    //   15051: aload_1
    //   15052: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   15055: ifeq +335 -> 15390
    //   15058: bipush 20
    //   15060: istore 14
    //   15062: iload 16
    //   15064: iload 14
    //   15066: bipush 14
    //   15068: iadd
    //   15069: i2f
    //   15070: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15073: iadd
    //   15074: istore 28
    //   15076: iload 13
    //   15078: istore 16
    //   15080: iload 13
    //   15082: iload 28
    //   15084: if_icmpge +7 -> 15091
    //   15087: iload 28
    //   15089: istore 16
    //   15091: aload_1
    //   15092: invokevirtual 1416	org/telegram/messenger/MessageObject:isRoundVideo	()Z
    //   15095: ifeq +301 -> 15396
    //   15098: iload 16
    //   15100: iload 15
    //   15102: invokestatic 1195	java/lang/Math:min	(II)I
    //   15105: istore 14
    //   15107: iload 14
    //   15109: istore 13
    //   15111: aload_0
    //   15112: iconst_0
    //   15113: putfield 346	org/telegram/ui/Cells/ChatMessageCell:drawBackground	Z
    //   15116: aload_0
    //   15117: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   15120: iload 13
    //   15122: iconst_2
    //   15123: idiv
    //   15124: invokevirtual 377	org/telegram/messenger/ImageReceiver:setRoundRadius	(I)V
    //   15127: aload_0
    //   15128: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15131: ifnull +1999 -> 17130
    //   15134: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   15137: getfield 2253	android/graphics/Point:x	I
    //   15140: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   15143: getfield 2262	android/graphics/Point:y	I
    //   15146: invokestatic 486	java/lang/Math:max	(II)I
    //   15149: i2f
    //   15150: ldc_w 2766
    //   15153: fmul
    //   15154: fstore 11
    //   15156: aload_0
    //   15157: invokespecial 3082	org/telegram/ui/Cells/ChatMessageCell:getGroupPhotosWidth	()I
    //   15160: istore 29
    //   15162: aload_0
    //   15163: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15166: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   15169: i2f
    //   15170: ldc_w 1740
    //   15173: fdiv
    //   15174: iload 29
    //   15176: i2f
    //   15177: fmul
    //   15178: f2d
    //   15179: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15182: d2i
    //   15183: istore 18
    //   15185: iload 18
    //   15187: istore 13
    //   15189: aload_0
    //   15190: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15193: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15196: ifeq +395 -> 15591
    //   15199: aload_1
    //   15200: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   15203: ifeq +15 -> 15218
    //   15206: aload_0
    //   15207: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15210: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   15213: iconst_1
    //   15214: iand
    //   15215: ifne +30 -> 15245
    //   15218: iload 18
    //   15220: istore 13
    //   15222: aload_1
    //   15223: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   15226: ifne +365 -> 15591
    //   15229: iload 18
    //   15231: istore 13
    //   15233: aload_0
    //   15234: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15237: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   15240: iconst_2
    //   15241: iand
    //   15242: ifeq +349 -> 15591
    //   15245: iconst_0
    //   15246: istore 13
    //   15248: iconst_0
    //   15249: istore 15
    //   15251: iconst_0
    //   15252: istore 14
    //   15254: iload 14
    //   15256: aload_0
    //   15257: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15260: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   15263: invokevirtual 590	java/util/ArrayList:size	()I
    //   15266: if_icmpge +315 -> 15581
    //   15269: aload_0
    //   15270: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15273: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   15276: iload 14
    //   15278: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   15281: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   15284: astore_2
    //   15285: aload_2
    //   15286: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15289: ifne +182 -> 15471
    //   15292: iload 13
    //   15294: i2d
    //   15295: dstore 7
    //   15297: aload_2
    //   15298: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   15301: i2f
    //   15302: ldc_w 1740
    //   15305: fdiv
    //   15306: iload 29
    //   15308: i2f
    //   15309: fmul
    //   15310: f2d
    //   15311: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15314: dstore 9
    //   15316: aload_2
    //   15317: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   15320: ifeq +145 -> 15465
    //   15323: aload_2
    //   15324: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   15327: i2f
    //   15328: ldc_w 1740
    //   15331: fdiv
    //   15332: iload 29
    //   15334: i2f
    //   15335: fmul
    //   15336: f2d
    //   15337: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15340: dstore 5
    //   15342: dload 5
    //   15344: dload 9
    //   15346: dadd
    //   15347: dload 7
    //   15349: dadd
    //   15350: d2i
    //   15351: istore 17
    //   15353: iload 15
    //   15355: istore 16
    //   15357: iload 14
    //   15359: iconst_1
    //   15360: iadd
    //   15361: istore 14
    //   15363: iload 16
    //   15365: istore 15
    //   15367: iload 17
    //   15369: istore 13
    //   15371: goto -117 -> 15254
    //   15374: aload_0
    //   15375: iload 18
    //   15377: ldc_w 478
    //   15380: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15383: isub
    //   15384: putfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   15387: goto -387 -> 15000
    //   15390: iconst_0
    //   15391: istore 14
    //   15393: goto -331 -> 15062
    //   15396: iload 15
    //   15398: istore 14
    //   15400: iload 16
    //   15402: istore 13
    //   15404: aload_1
    //   15405: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   15408: ifeq -281 -> 15127
    //   15411: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   15414: ifeq +21 -> 15435
    //   15417: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   15420: i2f
    //   15421: ldc_w 2766
    //   15424: fmul
    //   15425: f2i
    //   15426: istore 14
    //   15428: iload 14
    //   15430: istore 13
    //   15432: goto -305 -> 15127
    //   15435: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   15438: getfield 2253	android/graphics/Point:x	I
    //   15441: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   15444: getfield 2262	android/graphics/Point:y	I
    //   15447: invokestatic 1195	java/lang/Math:min	(II)I
    //   15450: i2f
    //   15451: ldc_w 2766
    //   15454: fmul
    //   15455: f2i
    //   15456: istore 14
    //   15458: iload 14
    //   15460: istore 13
    //   15462: goto -335 -> 15127
    //   15465: dconst_0
    //   15466: dstore 5
    //   15468: goto -126 -> 15342
    //   15471: aload_2
    //   15472: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15475: aload_0
    //   15476: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15479: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15482: if_icmpne +77 -> 15559
    //   15485: iload 15
    //   15487: i2d
    //   15488: dstore 7
    //   15490: aload_2
    //   15491: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   15494: i2f
    //   15495: ldc_w 1740
    //   15498: fdiv
    //   15499: iload 29
    //   15501: i2f
    //   15502: fmul
    //   15503: f2d
    //   15504: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15507: dstore 9
    //   15509: aload_2
    //   15510: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   15513: ifeq +40 -> 15553
    //   15516: aload_2
    //   15517: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   15520: i2f
    //   15521: ldc_w 1740
    //   15524: fdiv
    //   15525: iload 29
    //   15527: i2f
    //   15528: fmul
    //   15529: f2d
    //   15530: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15533: dstore 5
    //   15535: dload 5
    //   15537: dload 9
    //   15539: dadd
    //   15540: dload 7
    //   15542: dadd
    //   15543: d2i
    //   15544: istore 16
    //   15546: iload 13
    //   15548: istore 17
    //   15550: goto -193 -> 15357
    //   15553: dconst_0
    //   15554: dstore 5
    //   15556: goto -21 -> 15535
    //   15559: iload 15
    //   15561: istore 16
    //   15563: iload 13
    //   15565: istore 17
    //   15567: aload_2
    //   15568: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15571: aload_0
    //   15572: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15575: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15578: if_icmple -221 -> 15357
    //   15581: iload 18
    //   15583: iload 13
    //   15585: iload 15
    //   15587: isub
    //   15588: iadd
    //   15589: istore 13
    //   15591: iload 13
    //   15593: ldc_w 1705
    //   15596: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15599: isub
    //   15600: istore 13
    //   15602: iload 13
    //   15604: istore 14
    //   15606: aload_0
    //   15607: getfield 2278	org/telegram/ui/Cells/ChatMessageCell:isAvatarVisible	Z
    //   15610: ifeq +14 -> 15624
    //   15613: iload 13
    //   15615: ldc_w 781
    //   15618: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15621: isub
    //   15622: istore 14
    //   15624: aload_0
    //   15625: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15628: getfield 3641	org/telegram/messenger/MessageObject$GroupedMessagePosition:siblingHeights	[F
    //   15631: ifnull +552 -> 16183
    //   15634: iconst_0
    //   15635: istore 15
    //   15637: iconst_0
    //   15638: istore 13
    //   15640: iload 13
    //   15642: aload_0
    //   15643: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15646: getfield 3641	org/telegram/messenger/MessageObject$GroupedMessagePosition:siblingHeights	[F
    //   15649: arraylength
    //   15650: if_icmpge +35 -> 15685
    //   15653: iload 15
    //   15655: aload_0
    //   15656: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15659: getfield 3641	org/telegram/messenger/MessageObject$GroupedMessagePosition:siblingHeights	[F
    //   15662: iload 13
    //   15664: faload
    //   15665: fload 11
    //   15667: fmul
    //   15668: f2d
    //   15669: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15672: d2i
    //   15673: iadd
    //   15674: istore 15
    //   15676: iload 13
    //   15678: iconst_1
    //   15679: iadd
    //   15680: istore 13
    //   15682: goto -42 -> 15640
    //   15685: iload 15
    //   15687: aload_0
    //   15688: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15691: getfield 1865	org/telegram/messenger/MessageObject$GroupedMessagePosition:maxY	B
    //   15694: aload_0
    //   15695: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15698: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   15701: isub
    //   15702: ldc_w 1520
    //   15705: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15708: imul
    //   15709: iadd
    //   15710: istore 13
    //   15712: aload_0
    //   15713: iload 14
    //   15715: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   15718: iload 14
    //   15720: ldc_w 554
    //   15723: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15726: isub
    //   15727: istore 20
    //   15729: iload 20
    //   15731: istore 14
    //   15733: iload 14
    //   15735: istore 16
    //   15737: aload_0
    //   15738: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15741: getfield 3008	org/telegram/messenger/MessageObject$GroupedMessagePosition:edge	Z
    //   15744: ifne +14 -> 15758
    //   15747: iload 14
    //   15749: ldc_w 587
    //   15752: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15755: iadd
    //   15756: istore 16
    //   15758: iload 13
    //   15760: istore 21
    //   15762: iconst_0
    //   15763: iload 16
    //   15765: ldc_w 587
    //   15768: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   15771: isub
    //   15772: iadd
    //   15773: istore 19
    //   15775: aload_0
    //   15776: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15779: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   15782: bipush 8
    //   15784: iand
    //   15785: ifne +65 -> 15850
    //   15788: iload 19
    //   15790: istore 15
    //   15792: iload 13
    //   15794: istore 23
    //   15796: iload 21
    //   15798: istore 17
    //   15800: iload 16
    //   15802: istore 18
    //   15804: iload 20
    //   15806: istore 14
    //   15808: aload_0
    //   15809: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15812: getfield 3644	org/telegram/messenger/MessageObject$GroupedMessages:hasSibling	Z
    //   15815: ifeq +732 -> 16547
    //   15818: iload 19
    //   15820: istore 15
    //   15822: iload 13
    //   15824: istore 23
    //   15826: iload 21
    //   15828: istore 17
    //   15830: iload 16
    //   15832: istore 18
    //   15834: iload 20
    //   15836: istore 14
    //   15838: aload_0
    //   15839: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15842: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   15845: iconst_4
    //   15846: iand
    //   15847: ifne +700 -> 16547
    //   15850: iload 19
    //   15852: aload_0
    //   15853: aload_0
    //   15854: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15857: invokespecial 3610	org/telegram/ui/Cells/ChatMessageCell:getAdditionalWidthForPosition	(Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;)I
    //   15860: iadd
    //   15861: istore 19
    //   15863: aload_0
    //   15864: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15867: getfield 3278	org/telegram/messenger/MessageObject$GroupedMessages:messages	Ljava/util/ArrayList;
    //   15870: invokevirtual 590	java/util/ArrayList:size	()I
    //   15873: istore 30
    //   15875: iconst_0
    //   15876: istore 22
    //   15878: iload 19
    //   15880: istore 15
    //   15882: iload 13
    //   15884: istore 23
    //   15886: iload 21
    //   15888: istore 17
    //   15890: iload 16
    //   15892: istore 18
    //   15894: iload 20
    //   15896: istore 14
    //   15898: iload 22
    //   15900: iload 30
    //   15902: if_icmpge +645 -> 16547
    //   15905: aload_0
    //   15906: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15909: getfield 3278	org/telegram/messenger/MessageObject$GroupedMessages:messages	Ljava/util/ArrayList;
    //   15912: iload 22
    //   15914: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   15917: checkcast 469	org/telegram/messenger/MessageObject
    //   15920: astore_2
    //   15921: aload_0
    //   15922: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   15925: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   15928: iload 22
    //   15930: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   15933: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   15936: astore 37
    //   15938: iload 19
    //   15940: istore 15
    //   15942: iload 20
    //   15944: istore 14
    //   15946: aload 37
    //   15948: aload_0
    //   15949: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   15952: if_acmpeq +564 -> 16516
    //   15955: iload 19
    //   15957: istore 15
    //   15959: iload 20
    //   15961: istore 14
    //   15963: aload 37
    //   15965: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   15968: bipush 8
    //   15970: iand
    //   15971: ifeq +545 -> 16516
    //   15974: aload 37
    //   15976: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   15979: i2f
    //   15980: ldc_w 1740
    //   15983: fdiv
    //   15984: iload 29
    //   15986: i2f
    //   15987: fmul
    //   15988: f2d
    //   15989: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   15992: d2i
    //   15993: istore 23
    //   15995: iload 23
    //   15997: istore 14
    //   15999: aload 37
    //   16001: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16004: ifeq +326 -> 16330
    //   16007: aload_1
    //   16008: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   16011: ifeq +13 -> 16024
    //   16014: aload 37
    //   16016: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   16019: iconst_1
    //   16020: iand
    //   16021: ifne +28 -> 16049
    //   16024: iload 23
    //   16026: istore 14
    //   16028: aload_1
    //   16029: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   16032: ifne +298 -> 16330
    //   16035: iload 23
    //   16037: istore 14
    //   16039: aload 37
    //   16041: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   16044: iconst_2
    //   16045: iand
    //   16046: ifeq +284 -> 16330
    //   16049: iconst_0
    //   16050: istore 14
    //   16052: iconst_0
    //   16053: istore 17
    //   16055: iconst_0
    //   16056: istore 15
    //   16058: iload 15
    //   16060: aload_0
    //   16061: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   16064: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   16067: invokevirtual 590	java/util/ArrayList:size	()I
    //   16070: if_icmpge +250 -> 16320
    //   16073: aload_0
    //   16074: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   16077: getfield 2267	org/telegram/messenger/MessageObject$GroupedMessages:posArray	Ljava/util/ArrayList;
    //   16080: iload 15
    //   16082: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   16085: checkcast 964	org/telegram/messenger/MessageObject$GroupedMessagePosition
    //   16088: astore 38
    //   16090: aload 38
    //   16092: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16095: ifne +114 -> 16209
    //   16098: iload 14
    //   16100: i2d
    //   16101: dstore 7
    //   16103: aload 38
    //   16105: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   16108: i2f
    //   16109: ldc_w 1740
    //   16112: fdiv
    //   16113: iload 29
    //   16115: i2f
    //   16116: fmul
    //   16117: f2d
    //   16118: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   16121: dstore 9
    //   16123: aload 38
    //   16125: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   16128: ifeq +75 -> 16203
    //   16131: aload 38
    //   16133: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   16136: i2f
    //   16137: ldc_w 1740
    //   16140: fdiv
    //   16141: iload 29
    //   16143: i2f
    //   16144: fmul
    //   16145: f2d
    //   16146: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   16149: dstore 5
    //   16151: dload 5
    //   16153: dload 9
    //   16155: dadd
    //   16156: dload 7
    //   16158: dadd
    //   16159: d2i
    //   16160: istore 20
    //   16162: iload 17
    //   16164: istore 18
    //   16166: iload 15
    //   16168: iconst_1
    //   16169: iadd
    //   16170: istore 15
    //   16172: iload 18
    //   16174: istore 17
    //   16176: iload 20
    //   16178: istore 14
    //   16180: goto -122 -> 16058
    //   16183: aload_0
    //   16184: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   16187: getfield 3647	org/telegram/messenger/MessageObject$GroupedMessagePosition:ph	F
    //   16190: fload 11
    //   16192: fmul
    //   16193: f2d
    //   16194: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   16197: d2i
    //   16198: istore 13
    //   16200: goto -488 -> 15712
    //   16203: dconst_0
    //   16204: dstore 5
    //   16206: goto -55 -> 16151
    //   16209: aload 38
    //   16211: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16214: aload 37
    //   16216: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16219: if_icmpne +80 -> 16299
    //   16222: iload 17
    //   16224: i2d
    //   16225: dstore 7
    //   16227: aload 38
    //   16229: getfield 2273	org/telegram/messenger/MessageObject$GroupedMessagePosition:pw	I
    //   16232: i2f
    //   16233: ldc_w 1740
    //   16236: fdiv
    //   16237: iload 29
    //   16239: i2f
    //   16240: fmul
    //   16241: f2d
    //   16242: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   16245: dstore 9
    //   16247: aload 38
    //   16249: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   16252: ifeq +41 -> 16293
    //   16255: aload 38
    //   16257: getfield 2276	org/telegram/messenger/MessageObject$GroupedMessagePosition:leftSpanOffset	I
    //   16260: i2f
    //   16261: ldc_w 1740
    //   16264: fdiv
    //   16265: iload 29
    //   16267: i2f
    //   16268: fmul
    //   16269: f2d
    //   16270: invokestatic 1182	java/lang/Math:ceil	(D)D
    //   16273: dstore 5
    //   16275: dload 5
    //   16277: dload 9
    //   16279: dadd
    //   16280: dload 7
    //   16282: dadd
    //   16283: d2i
    //   16284: istore 18
    //   16286: iload 14
    //   16288: istore 20
    //   16290: goto -124 -> 16166
    //   16293: dconst_0
    //   16294: dstore 5
    //   16296: goto -21 -> 16275
    //   16299: iload 17
    //   16301: istore 18
    //   16303: iload 14
    //   16305: istore 20
    //   16307: aload 38
    //   16309: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16312: aload 37
    //   16314: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16317: if_icmple -151 -> 16166
    //   16320: iload 23
    //   16322: iload 14
    //   16324: iload 17
    //   16326: isub
    //   16327: iadd
    //   16328: istore 14
    //   16330: iload 14
    //   16332: ldc_w 1185
    //   16335: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16338: isub
    //   16339: istore 15
    //   16341: iload 15
    //   16343: istore 14
    //   16345: aload_0
    //   16346: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   16349: ifeq +53 -> 16402
    //   16352: iload 15
    //   16354: istore 14
    //   16356: aload_2
    //   16357: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   16360: ifne +42 -> 16402
    //   16363: iload 15
    //   16365: istore 14
    //   16367: aload_2
    //   16368: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   16371: ifeq +31 -> 16402
    //   16374: aload 37
    //   16376: ifnull +15 -> 16391
    //   16379: iload 15
    //   16381: istore 14
    //   16383: aload 37
    //   16385: getfield 3008	org/telegram/messenger/MessageObject$GroupedMessagePosition:edge	Z
    //   16388: ifeq +14 -> 16402
    //   16391: iload 15
    //   16393: ldc_w 781
    //   16396: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16399: isub
    //   16400: istore 14
    //   16402: iload 14
    //   16404: aload_0
    //   16405: aload 37
    //   16407: invokespecial 3610	org/telegram/ui/Cells/ChatMessageCell:getAdditionalWidthForPosition	(Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;)I
    //   16410: iadd
    //   16411: istore 14
    //   16413: iload 14
    //   16415: istore 17
    //   16417: aload 37
    //   16419: getfield 3008	org/telegram/messenger/MessageObject$GroupedMessagePosition:edge	Z
    //   16422: ifne +14 -> 16436
    //   16425: iload 14
    //   16427: ldc_w 587
    //   16430: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16433: iadd
    //   16434: istore 17
    //   16436: iload 19
    //   16438: iload 17
    //   16440: iadd
    //   16441: istore 18
    //   16443: aload 37
    //   16445: getfield 2769	org/telegram/messenger/MessageObject$GroupedMessagePosition:minX	B
    //   16448: aload_0
    //   16449: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   16452: getfield 2769	org/telegram/messenger/MessageObject$GroupedMessagePosition:minX	B
    //   16455: if_icmplt +42 -> 16497
    //   16458: iload 18
    //   16460: istore 15
    //   16462: iload 17
    //   16464: istore 14
    //   16466: aload_0
    //   16467: getfield 705	org/telegram/ui/Cells/ChatMessageCell:currentMessagesGroup	Lorg/telegram/messenger/MessageObject$GroupedMessages;
    //   16470: getfield 3644	org/telegram/messenger/MessageObject$GroupedMessages:hasSibling	Z
    //   16473: ifeq +43 -> 16516
    //   16476: iload 18
    //   16478: istore 15
    //   16480: iload 17
    //   16482: istore 14
    //   16484: aload 37
    //   16486: getfield 2270	org/telegram/messenger/MessageObject$GroupedMessagePosition:minY	B
    //   16489: aload 37
    //   16491: getfield 1865	org/telegram/messenger/MessageObject$GroupedMessagePosition:maxY	B
    //   16494: if_icmpeq +22 -> 16516
    //   16497: aload_0
    //   16498: aload_0
    //   16499: getfield 1777	org/telegram/ui/Cells/ChatMessageCell:captionOffsetX	I
    //   16502: iload 17
    //   16504: isub
    //   16505: putfield 1777	org/telegram/ui/Cells/ChatMessageCell:captionOffsetX	I
    //   16508: iload 17
    //   16510: istore 14
    //   16512: iload 18
    //   16514: istore 15
    //   16516: aload_2
    //   16517: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   16520: ifnull +593 -> 17113
    //   16523: aload_0
    //   16524: getfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   16527: ifnull +578 -> 17105
    //   16530: aload_0
    //   16531: aconst_null
    //   16532: putfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   16535: iload 16
    //   16537: istore 18
    //   16539: iload 21
    //   16541: istore 17
    //   16543: iload 13
    //   16545: istore 23
    //   16547: iload 26
    //   16549: istore 13
    //   16551: iload 24
    //   16553: istore 19
    //   16555: aload_0
    //   16556: getfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   16559: ifnull +282 -> 16841
    //   16562: iload 27
    //   16564: istore 16
    //   16566: getstatic 790	android/os/Build$VERSION:SDK_INT	I
    //   16569: bipush 24
    //   16571: if_icmplt +632 -> 17203
    //   16574: iload 27
    //   16576: istore 16
    //   16578: aload_0
    //   16579: aload_0
    //   16580: getfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   16583: iconst_0
    //   16584: aload_0
    //   16585: getfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   16588: invokeinterface 2192 1 0
    //   16593: getstatic 2970	org/telegram/ui/ActionBar/Theme:chat_msgTextPaint	Landroid/text/TextPaint;
    //   16596: iload 15
    //   16598: invokestatic 3474	android/text/StaticLayout$Builder:obtain	(Ljava/lang/CharSequence;IILandroid/text/TextPaint;I)Landroid/text/StaticLayout$Builder;
    //   16601: iconst_1
    //   16602: invokevirtual 3478	android/text/StaticLayout$Builder:setBreakStrategy	(I)Landroid/text/StaticLayout$Builder;
    //   16605: iconst_0
    //   16606: invokevirtual 3481	android/text/StaticLayout$Builder:setHyphenationFrequency	(I)Landroid/text/StaticLayout$Builder;
    //   16609: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   16612: invokevirtual 3485	android/text/StaticLayout$Builder:setAlignment	(Landroid/text/Layout$Alignment;)Landroid/text/StaticLayout$Builder;
    //   16615: invokevirtual 3489	android/text/StaticLayout$Builder:build	()Landroid/text/StaticLayout;
    //   16618: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16621: iload 26
    //   16623: istore 13
    //   16625: iload 24
    //   16627: istore 19
    //   16629: iload 27
    //   16631: istore 16
    //   16633: aload_0
    //   16634: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16637: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   16640: ifle +201 -> 16841
    //   16643: iload 27
    //   16645: istore 16
    //   16647: aload_0
    //   16648: iload 15
    //   16650: putfield 643	org/telegram/ui/Cells/ChatMessageCell:captionWidth	I
    //   16653: iload 27
    //   16655: istore 16
    //   16657: aload_0
    //   16658: aload_0
    //   16659: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16662: invokevirtual 780	android/text/StaticLayout:getHeight	()I
    //   16665: putfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   16668: iload 27
    //   16670: istore 16
    //   16672: aload_0
    //   16673: aload_0
    //   16674: getfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   16677: ldc_w 1705
    //   16680: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16683: iadd
    //   16684: putfield 2933	org/telegram/ui/Cells/ChatMessageCell:addedCaptionHeight	I
    //   16687: iload 27
    //   16689: istore 16
    //   16691: aload_0
    //   16692: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   16695: ifnull +20 -> 16715
    //   16698: iload 27
    //   16700: istore 16
    //   16702: aload_0
    //   16703: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   16706: getfield 2210	org/telegram/messenger/MessageObject$GroupedMessagePosition:flags	I
    //   16709: bipush 8
    //   16711: iand
    //   16712: ifeq +540 -> 17252
    //   16715: iload 27
    //   16717: istore 16
    //   16719: iconst_0
    //   16720: aload_0
    //   16721: getfield 2933	org/telegram/ui/Cells/ChatMessageCell:addedCaptionHeight	I
    //   16724: iadd
    //   16725: istore 20
    //   16727: iload 20
    //   16729: istore 16
    //   16731: aload_0
    //   16732: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16735: aload_0
    //   16736: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16739: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   16742: iconst_1
    //   16743: isub
    //   16744: invokevirtual 662	android/text/StaticLayout:getLineWidth	(I)F
    //   16747: fstore 11
    //   16749: iload 20
    //   16751: istore 16
    //   16753: aload_0
    //   16754: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16757: aload_0
    //   16758: getfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   16761: invokevirtual 1226	android/text/StaticLayout:getLineCount	()I
    //   16764: iconst_1
    //   16765: isub
    //   16766: invokevirtual 659	android/text/StaticLayout:getLineLeft	(I)F
    //   16769: fstore 12
    //   16771: iload 20
    //   16773: istore 13
    //   16775: iload 24
    //   16777: istore 19
    //   16779: iload 20
    //   16781: istore 16
    //   16783: fconst_2
    //   16784: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16787: iload 15
    //   16789: iadd
    //   16790: i2f
    //   16791: fload 11
    //   16793: fload 12
    //   16795: fadd
    //   16796: fsub
    //   16797: iload 28
    //   16799: i2f
    //   16800: fcmpg
    //   16801: ifge +40 -> 16841
    //   16804: iload 20
    //   16806: istore 16
    //   16808: iload 20
    //   16810: ldc_w 478
    //   16813: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16816: iadd
    //   16817: istore 13
    //   16819: iload 13
    //   16821: istore 16
    //   16823: aload_0
    //   16824: aload_0
    //   16825: getfield 2933	org/telegram/ui/Cells/ChatMessageCell:addedCaptionHeight	I
    //   16828: ldc_w 478
    //   16831: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   16834: iadd
    //   16835: putfield 2933	org/telegram/ui/Cells/ChatMessageCell:addedCaptionHeight	I
    //   16838: iconst_1
    //   16839: istore 19
    //   16841: getstatic 2340	java/util/Locale:US	Ljava/util/Locale;
    //   16844: ldc_w 3433
    //   16847: iconst_2
    //   16848: anewarray 1242	java/lang/Object
    //   16851: dup
    //   16852: iconst_0
    //   16853: iload 14
    //   16855: i2f
    //   16856: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   16859: fdiv
    //   16860: f2i
    //   16861: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   16864: aastore
    //   16865: dup
    //   16866: iconst_1
    //   16867: iload 23
    //   16869: i2f
    //   16870: getstatic 2350	org/telegram/messenger/AndroidUtilities:density	F
    //   16873: fdiv
    //   16874: f2i
    //   16875: invokestatic 1015	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   16878: aastore
    //   16879: invokestatic 2353	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   16882: astore_2
    //   16883: aload_0
    //   16884: aload_2
    //   16885: putfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   16888: aload_0
    //   16889: aload_2
    //   16890: putfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   16893: aload_1
    //   16894: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   16897: ifnull +14 -> 16911
    //   16900: aload_1
    //   16901: getfield 1336	org/telegram/messenger/MessageObject:photoThumbs	Ljava/util/ArrayList;
    //   16904: invokevirtual 590	java/util/ArrayList:size	()I
    //   16907: iconst_1
    //   16908: if_icmpgt +28 -> 16936
    //   16911: aload_1
    //   16912: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   16915: iconst_3
    //   16916: if_icmpeq +20 -> 16936
    //   16919: aload_1
    //   16920: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   16923: bipush 8
    //   16925: if_icmpeq +11 -> 16936
    //   16928: aload_1
    //   16929: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   16932: iconst_5
    //   16933: if_icmpne +64 -> 16997
    //   16936: aload_1
    //   16937: invokevirtual 1253	org/telegram/messenger/MessageObject:needDrawBluredPreview	()Z
    //   16940: ifeq +332 -> 17272
    //   16943: aload_0
    //   16944: new 1320	java/lang/StringBuilder
    //   16947: dup
    //   16948: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   16951: aload_0
    //   16952: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   16955: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   16958: ldc_w 3649
    //   16961: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   16964: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   16967: putfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   16970: aload_0
    //   16971: new 1320	java/lang/StringBuilder
    //   16974: dup
    //   16975: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   16978: aload_0
    //   16979: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   16982: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   16985: ldc_w 3649
    //   16988: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   16991: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   16994: putfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   16997: iconst_0
    //   16998: istore 14
    //   17000: aload_1
    //   17001: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17004: iconst_3
    //   17005: if_icmpeq +20 -> 17025
    //   17008: aload_1
    //   17009: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17012: bipush 8
    //   17014: if_icmpeq +11 -> 17025
    //   17017: aload_1
    //   17018: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17021: iconst_5
    //   17022: if_icmpne +6 -> 17028
    //   17025: iconst_1
    //   17026: istore 14
    //   17028: aload_0
    //   17029: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17032: ifnull +26 -> 17058
    //   17035: iload 14
    //   17037: ifne +21 -> 17058
    //   17040: aload_0
    //   17041: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17044: getfield 1407	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   17047: ifne +11 -> 17058
    //   17050: aload_0
    //   17051: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17054: iconst_m1
    //   17055: putfield 1407	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   17058: aload_1
    //   17059: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17062: iconst_1
    //   17063: if_icmpne +567 -> 17630
    //   17066: aload_1
    //   17067: getfield 3652	org/telegram/messenger/MessageObject:useCustomPhoto	Z
    //   17070: ifeq +232 -> 17302
    //   17073: aload_0
    //   17074: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17077: aload_0
    //   17078: invokevirtual 2233	org/telegram/ui/Cells/ChatMessageCell:getResources	()Landroid/content/res/Resources;
    //   17081: ldc_w 3653
    //   17084: invokevirtual 3657	android/content/res/Resources:getDrawable	(I)Landroid/graphics/drawable/Drawable;
    //   17087: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   17090: iload 13
    //   17092: istore 20
    //   17094: iload 17
    //   17096: istore 13
    //   17098: iload 18
    //   17100: istore 14
    //   17102: goto -5556 -> 11546
    //   17105: aload_0
    //   17106: aload_2
    //   17107: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   17110: putfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   17113: iload 22
    //   17115: iconst_1
    //   17116: iadd
    //   17117: istore 22
    //   17119: iload 15
    //   17121: istore 19
    //   17123: iload 14
    //   17125: istore 20
    //   17127: goto -1249 -> 15878
    //   17130: iload 13
    //   17132: istore 18
    //   17134: iload 14
    //   17136: istore 17
    //   17138: aload_0
    //   17139: ldc_w 554
    //   17142: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   17145: iload 13
    //   17147: iadd
    //   17148: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   17151: aload_0
    //   17152: getfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   17155: ifne +18 -> 17173
    //   17158: aload_0
    //   17159: aload_0
    //   17160: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   17163: ldc_w 1705
    //   17166: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   17169: iadd
    //   17170: putfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   17173: aload_0
    //   17174: aload_1
    //   17175: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   17178: putfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   17181: iload 18
    //   17183: ldc_w 587
    //   17186: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   17189: isub
    //   17190: istore 15
    //   17192: iload 14
    //   17194: istore 23
    //   17196: iload 13
    //   17198: istore 14
    //   17200: goto -653 -> 16547
    //   17203: iload 27
    //   17205: istore 16
    //   17207: aload_0
    //   17208: new 350	android/text/StaticLayout
    //   17211: dup
    //   17212: aload_0
    //   17213: getfield 631	org/telegram/ui/Cells/ChatMessageCell:currentCaption	Ljava/lang/CharSequence;
    //   17216: getstatic 2970	org/telegram/ui/ActionBar/Theme:chat_msgTextPaint	Landroid/text/TextPaint;
    //   17219: iload 15
    //   17221: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   17224: fconst_1
    //   17225: fconst_0
    //   17226: iconst_0
    //   17227: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   17230: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   17233: goto -612 -> 16621
    //   17236: astore_2
    //   17237: aload_2
    //   17238: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   17241: iload 16
    //   17243: istore 13
    //   17245: iload 24
    //   17247: istore 19
    //   17249: goto -408 -> 16841
    //   17252: iload 27
    //   17254: istore 16
    //   17256: aload_0
    //   17257: aconst_null
    //   17258: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   17261: iload 26
    //   17263: istore 13
    //   17265: iload 24
    //   17267: istore 19
    //   17269: goto -428 -> 16841
    //   17272: aload_0
    //   17273: new 1320	java/lang/StringBuilder
    //   17276: dup
    //   17277: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   17280: aload_0
    //   17281: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17284: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   17287: ldc_w 3659
    //   17290: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   17293: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   17296: putfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17299: goto -302 -> 16997
    //   17302: aload_0
    //   17303: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17306: ifnull +298 -> 17604
    //   17309: iconst_1
    //   17310: istore 15
    //   17312: aload_0
    //   17313: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17316: invokestatic 3525	org/telegram/messenger/FileLoader:getAttachFileName	(Lorg/telegram/tgnet/TLObject;)Ljava/lang/String;
    //   17319: astore_2
    //   17320: aload_1
    //   17321: getfield 2747	org/telegram/messenger/MessageObject:mediaExists	Z
    //   17324: ifeq +145 -> 17469
    //   17327: aload_0
    //   17328: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17331: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   17334: aload_0
    //   17335: invokevirtual 2964	org/telegram/messenger/DownloadController:removeLoadingFileObserver	(Lorg/telegram/messenger/DownloadController$FileDownloadProgressListener;)V
    //   17338: iload 15
    //   17340: ifne +34 -> 17374
    //   17343: aload_0
    //   17344: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17347: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   17350: aload_0
    //   17351: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   17354: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   17357: ifne +17 -> 17374
    //   17360: aload_0
    //   17361: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17364: invokestatic 1378	org/telegram/messenger/FileLoader:getInstance	(I)Lorg/telegram/messenger/FileLoader;
    //   17367: aload_2
    //   17368: invokevirtual 3531	org/telegram/messenger/FileLoader:isLoadingFile	(Ljava/lang/String;)Z
    //   17371: ifeq +127 -> 17498
    //   17374: aload_0
    //   17375: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17378: astore 37
    //   17380: aload_0
    //   17381: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17384: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17387: astore 38
    //   17389: aload_0
    //   17390: getfield 1358	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilter	Ljava/lang/String;
    //   17393: astore 39
    //   17395: aload_0
    //   17396: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17399: ifnull +76 -> 17475
    //   17402: aload_0
    //   17403: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17406: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17409: astore_2
    //   17410: aload_0
    //   17411: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17414: astore 40
    //   17416: iload 14
    //   17418: ifeq +62 -> 17480
    //   17421: iconst_0
    //   17422: istore 14
    //   17424: aload_0
    //   17425: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   17428: invokevirtual 1410	org/telegram/messenger/MessageObject:shouldEncryptPhotoOrVideo	()Z
    //   17431: ifeq +61 -> 17492
    //   17434: iconst_2
    //   17435: istore 15
    //   17437: aload 37
    //   17439: aload 38
    //   17441: aload 39
    //   17443: aload_2
    //   17444: aload 40
    //   17446: iload 14
    //   17448: aconst_null
    //   17449: iload 15
    //   17451: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   17454: iload 13
    //   17456: istore 20
    //   17458: iload 17
    //   17460: istore 13
    //   17462: iload 18
    //   17464: istore 14
    //   17466: goto -5920 -> 11546
    //   17469: iconst_0
    //   17470: istore 15
    //   17472: goto -134 -> 17338
    //   17475: aconst_null
    //   17476: astore_2
    //   17477: goto -67 -> 17410
    //   17480: aload_0
    //   17481: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17484: getfield 1407	org/telegram/tgnet/TLRPC$PhotoSize:size	I
    //   17487: istore 14
    //   17489: goto -65 -> 17424
    //   17492: iconst_0
    //   17493: istore 15
    //   17495: goto -58 -> 17437
    //   17498: aload_0
    //   17499: iconst_1
    //   17500: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   17503: aload_0
    //   17504: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17507: ifnull +71 -> 17578
    //   17510: aload_0
    //   17511: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17514: astore_2
    //   17515: aload_0
    //   17516: getfield 1404	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObjectThumb	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17519: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17522: astore 37
    //   17524: aload_0
    //   17525: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17528: astore 38
    //   17530: aload_0
    //   17531: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   17534: invokevirtual 1410	org/telegram/messenger/MessageObject:shouldEncryptPhotoOrVideo	()Z
    //   17537: ifeq +35 -> 17572
    //   17540: iconst_2
    //   17541: istore 14
    //   17543: aload_2
    //   17544: aconst_null
    //   17545: aconst_null
    //   17546: aload 37
    //   17548: aload 38
    //   17550: iconst_0
    //   17551: aconst_null
    //   17552: iload 14
    //   17554: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   17557: iload 13
    //   17559: istore 20
    //   17561: iload 17
    //   17563: istore 13
    //   17565: iload 18
    //   17567: istore 14
    //   17569: goto -6023 -> 11546
    //   17572: iconst_0
    //   17573: istore 14
    //   17575: goto -32 -> 17543
    //   17578: aload_0
    //   17579: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17582: aconst_null
    //   17583: checkcast 794	android/graphics/drawable/Drawable
    //   17586: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   17589: iload 13
    //   17591: istore 20
    //   17593: iload 17
    //   17595: istore 13
    //   17597: iload 18
    //   17599: istore 14
    //   17601: goto -6055 -> 11546
    //   17604: aload_0
    //   17605: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17608: aconst_null
    //   17609: checkcast 1364	android/graphics/drawable/BitmapDrawable
    //   17612: invokevirtual 1368	org/telegram/messenger/ImageReceiver:setImageBitmap	(Landroid/graphics/drawable/Drawable;)V
    //   17615: iload 13
    //   17617: istore 20
    //   17619: iload 17
    //   17621: istore 13
    //   17623: iload 18
    //   17625: istore 14
    //   17627: goto -6081 -> 11546
    //   17630: aload_1
    //   17631: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17634: bipush 8
    //   17636: if_icmpeq +11 -> 17647
    //   17639: aload_1
    //   17640: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17643: iconst_5
    //   17644: if_icmpne +372 -> 18016
    //   17647: aload_1
    //   17648: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   17651: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   17654: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   17657: invokestatic 3525	org/telegram/messenger/FileLoader:getAttachFileName	(Lorg/telegram/tgnet/TLObject;)Ljava/lang/String;
    //   17660: astore_2
    //   17661: iconst_0
    //   17662: istore 14
    //   17664: aload_1
    //   17665: getfield 2750	org/telegram/messenger/MessageObject:attachPathExists	Z
    //   17668: ifeq +150 -> 17818
    //   17671: aload_0
    //   17672: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17675: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   17678: aload_0
    //   17679: invokevirtual 2964	org/telegram/messenger/DownloadController:removeLoadingFileObserver	(Lorg/telegram/messenger/DownloadController$FileDownloadProgressListener;)V
    //   17682: iconst_1
    //   17683: istore 14
    //   17685: iconst_0
    //   17686: istore_3
    //   17687: aload_1
    //   17688: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   17691: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   17694: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   17697: invokestatic 3534	org/telegram/messenger/MessageObject:isNewGifDocument	(Lorg/telegram/tgnet/TLRPC$Document;)Z
    //   17700: ifeq +131 -> 17831
    //   17703: aload_0
    //   17704: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17707: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   17710: aload_0
    //   17711: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   17714: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   17717: istore_3
    //   17718: aload_1
    //   17719: invokevirtual 1426	org/telegram/messenger/MessageObject:isSending	()Z
    //   17722: ifne +233 -> 17955
    //   17725: iload 14
    //   17727: ifne +21 -> 17748
    //   17730: aload_0
    //   17731: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17734: invokestatic 1378	org/telegram/messenger/FileLoader:getInstance	(I)Lorg/telegram/messenger/FileLoader;
    //   17737: aload_2
    //   17738: invokevirtual 3531	org/telegram/messenger/FileLoader:isLoadingFile	(Ljava/lang/String;)Z
    //   17741: ifne +7 -> 17748
    //   17744: iload_3
    //   17745: ifeq +210 -> 17955
    //   17748: iload 14
    //   17750: iconst_1
    //   17751: if_icmpne +123 -> 17874
    //   17754: aload_0
    //   17755: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17758: astore 38
    //   17760: aload_1
    //   17761: invokevirtual 1088	org/telegram/messenger/MessageObject:isSendError	()Z
    //   17764: ifeq +93 -> 17857
    //   17767: aconst_null
    //   17768: astore_2
    //   17769: aload_0
    //   17770: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17773: ifnull +95 -> 17868
    //   17776: aload_0
    //   17777: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17780: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17783: astore 37
    //   17785: aload 38
    //   17787: aconst_null
    //   17788: aload_2
    //   17789: aconst_null
    //   17790: aconst_null
    //   17791: aload 37
    //   17793: aload_0
    //   17794: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17797: iconst_0
    //   17798: aconst_null
    //   17799: iconst_0
    //   17800: invokevirtual 1362	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Ljava/lang/String;Landroid/graphics/drawable/Drawable;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   17803: iload 13
    //   17805: istore 20
    //   17807: iload 17
    //   17809: istore 13
    //   17811: iload 18
    //   17813: istore 14
    //   17815: goto -6269 -> 11546
    //   17818: aload_1
    //   17819: getfield 2747	org/telegram/messenger/MessageObject:mediaExists	Z
    //   17822: ifeq -137 -> 17685
    //   17825: iconst_2
    //   17826: istore 14
    //   17828: goto -143 -> 17685
    //   17831: aload_1
    //   17832: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   17835: iconst_5
    //   17836: if_icmpne -118 -> 17718
    //   17839: aload_0
    //   17840: getfield 342	org/telegram/ui/Cells/ChatMessageCell:currentAccount	I
    //   17843: invokestatic 396	org/telegram/messenger/DownloadController:getInstance	(I)Lorg/telegram/messenger/DownloadController;
    //   17846: aload_0
    //   17847: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   17850: invokevirtual 3528	org/telegram/messenger/DownloadController:canDownloadMedia	(Lorg/telegram/messenger/MessageObject;)Z
    //   17853: istore_3
    //   17854: goto -136 -> 17718
    //   17857: aload_1
    //   17858: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   17861: getfield 3633	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   17864: astore_2
    //   17865: goto -96 -> 17769
    //   17868: aconst_null
    //   17869: astore 37
    //   17871: goto -86 -> 17785
    //   17874: aload_0
    //   17875: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17878: astore 37
    //   17880: aload_1
    //   17881: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   17884: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   17887: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   17890: astore 38
    //   17892: aload_0
    //   17893: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17896: ifnull +54 -> 17950
    //   17899: aload_0
    //   17900: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17903: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17906: astore_2
    //   17907: aload 37
    //   17909: aload 38
    //   17911: aconst_null
    //   17912: aload_2
    //   17913: aload_0
    //   17914: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17917: aload_1
    //   17918: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   17921: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   17924: getfield 1150	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   17927: getfield 1259	org/telegram/tgnet/TLRPC$Document:size	I
    //   17930: aconst_null
    //   17931: iconst_0
    //   17932: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   17935: iload 13
    //   17937: istore 20
    //   17939: iload 17
    //   17941: istore 13
    //   17943: iload 18
    //   17945: istore 14
    //   17947: goto -6401 -> 11546
    //   17950: aconst_null
    //   17951: astore_2
    //   17952: goto -45 -> 17907
    //   17955: aload_0
    //   17956: iconst_1
    //   17957: putfield 2364	org/telegram/ui/Cells/ChatMessageCell:photoNotSet	Z
    //   17960: aload_0
    //   17961: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   17964: astore 37
    //   17966: aload_0
    //   17967: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17970: ifnull +41 -> 18011
    //   17973: aload_0
    //   17974: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   17977: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   17980: astore_2
    //   17981: aload 37
    //   17983: aconst_null
    //   17984: aconst_null
    //   17985: aload_2
    //   17986: aload_0
    //   17987: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   17990: iconst_0
    //   17991: aconst_null
    //   17992: iconst_0
    //   17993: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   17996: iload 13
    //   17998: istore 20
    //   18000: iload 17
    //   18002: istore 13
    //   18004: iload 18
    //   18006: istore 14
    //   18008: goto -6462 -> 11546
    //   18011: aconst_null
    //   18012: astore_2
    //   18013: goto -32 -> 17981
    //   18016: aload_0
    //   18017: getfield 406	org/telegram/ui/Cells/ChatMessageCell:photoImage	Lorg/telegram/messenger/ImageReceiver;
    //   18020: astore 37
    //   18022: aload_0
    //   18023: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   18026: ifnull +59 -> 18085
    //   18029: aload_0
    //   18030: getfield 1345	org/telegram/ui/Cells/ChatMessageCell:currentPhotoObject	Lorg/telegram/tgnet/TLRPC$PhotoSize;
    //   18033: getfield 1292	org/telegram/tgnet/TLRPC$PhotoSize:location	Lorg/telegram/tgnet/TLRPC$FileLocation;
    //   18036: astore_2
    //   18037: aload_0
    //   18038: getfield 1406	org/telegram/ui/Cells/ChatMessageCell:currentPhotoFilterThumb	Ljava/lang/String;
    //   18041: astore 38
    //   18043: aload_0
    //   18044: getfield 467	org/telegram/ui/Cells/ChatMessageCell:currentMessageObject	Lorg/telegram/messenger/MessageObject;
    //   18047: invokevirtual 1410	org/telegram/messenger/MessageObject:shouldEncryptPhotoOrVideo	()Z
    //   18050: ifeq +40 -> 18090
    //   18053: iconst_2
    //   18054: istore 14
    //   18056: aload 37
    //   18058: aconst_null
    //   18059: aconst_null
    //   18060: aload_2
    //   18061: aload 38
    //   18063: iconst_0
    //   18064: aconst_null
    //   18065: iload 14
    //   18067: invokevirtual 1413	org/telegram/messenger/ImageReceiver:setImage	(Lorg/telegram/tgnet/TLObject;Ljava/lang/String;Lorg/telegram/tgnet/TLRPC$FileLocation;Ljava/lang/String;ILjava/lang/String;I)V
    //   18070: iload 13
    //   18072: istore 20
    //   18074: iload 17
    //   18076: istore 13
    //   18078: iload 18
    //   18080: istore 14
    //   18082: goto -6536 -> 11546
    //   18085: aconst_null
    //   18086: astore_2
    //   18087: goto -50 -> 18037
    //   18090: iconst_0
    //   18091: istore 14
    //   18093: goto -37 -> 18056
    //   18096: aload_0
    //   18097: getfield 2563	org/telegram/ui/Cells/ChatMessageCell:drawNameLayout	Z
    //   18100: ifeq -6495 -> 11605
    //   18103: aload_1
    //   18104: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   18107: getfield 1067	org/telegram/tgnet/TLRPC$Message:reply_to_msg_id	I
    //   18110: ifne -6505 -> 11605
    //   18113: aload_0
    //   18114: aload_0
    //   18115: getfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   18118: ldc_w 616
    //   18121: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18124: iadd
    //   18125: putfield 563	org/telegram/ui/Cells/ChatMessageCell:namesOffset	I
    //   18128: goto -6523 -> 11605
    //   18131: aload_0
    //   18132: new 350	android/text/StaticLayout
    //   18135: dup
    //   18136: aload_1
    //   18137: getfield 3184	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   18140: getstatic 2970	org/telegram/ui/ActionBar/Theme:chat_msgTextPaint	Landroid/text/TextPaint;
    //   18143: iload 14
    //   18145: getstatic 946	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   18148: fconst_1
    //   18149: fconst_0
    //   18150: iconst_0
    //   18151: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   18154: putfield 635	org/telegram/ui/Cells/ChatMessageCell:captionLayout	Landroid/text/StaticLayout;
    //   18157: goto -12146 -> 6011
    //   18160: astore_2
    //   18161: aload_2
    //   18162: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   18165: iload 19
    //   18167: istore 13
    //   18169: goto -11987 -> 6182
    //   18172: iconst_0
    //   18173: istore 14
    //   18175: goto -12123 -> 6052
    //   18178: iload 19
    //   18180: istore 13
    //   18182: aload_0
    //   18183: getfield 1184	org/telegram/ui/Cells/ChatMessageCell:widthBeforeNewTimeLine	I
    //   18186: iconst_m1
    //   18187: if_icmpeq -12005 -> 6182
    //   18190: iload 19
    //   18192: istore 13
    //   18194: aload_0
    //   18195: getfield 1187	org/telegram/ui/Cells/ChatMessageCell:availableTimeWidth	I
    //   18198: aload_0
    //   18199: getfield 1184	org/telegram/ui/Cells/ChatMessageCell:widthBeforeNewTimeLine	I
    //   18202: isub
    //   18203: aload_0
    //   18204: getfield 493	org/telegram/ui/Cells/ChatMessageCell:timeWidth	I
    //   18207: if_icmpge -12025 -> 6182
    //   18210: aload_0
    //   18211: aload_0
    //   18212: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18215: ldc_w 478
    //   18218: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18221: iadd
    //   18222: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18225: iload 19
    //   18227: istore 13
    //   18229: goto -12047 -> 6182
    //   18232: iconst_0
    //   18233: istore_3
    //   18234: goto -11906 -> 6328
    //   18237: astore 37
    //   18239: aload 37
    //   18241: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   18244: goto -11871 -> 6373
    //   18247: aload_0
    //   18248: aload_0
    //   18249: getfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   18252: iload 15
    //   18254: ineg
    //   18255: invokestatic 486	java/lang/Math:max	(II)I
    //   18258: putfield 738	org/telegram/ui/Cells/ChatMessageCell:descriptionX	I
    //   18261: goto -11740 -> 6521
    //   18264: astore_2
    //   18265: aload_2
    //   18266: invokestatic 714	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   18269: aload_0
    //   18270: aload_0
    //   18271: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18274: ldc_w 494
    //   18277: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18280: iadd
    //   18281: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18284: iload 13
    //   18286: ifeq +39 -> 18325
    //   18289: aload_0
    //   18290: aload_0
    //   18291: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18294: ldc_w 478
    //   18297: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18300: isub
    //   18301: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   18304: iload 13
    //   18306: iconst_2
    //   18307: if_icmpne +18 -> 18325
    //   18310: aload_0
    //   18311: aload_0
    //   18312: getfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   18315: ldc_w 478
    //   18318: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18321: isub
    //   18322: putfield 647	org/telegram/ui/Cells/ChatMessageCell:captionHeight	I
    //   18325: aload_0
    //   18326: getfield 328	org/telegram/ui/Cells/ChatMessageCell:botButtons	Ljava/util/ArrayList;
    //   18329: invokevirtual 2531	java/util/ArrayList:clear	()V
    //   18332: iload 25
    //   18334: ifeq +22 -> 18356
    //   18337: aload_0
    //   18338: getfield 333	org/telegram/ui/Cells/ChatMessageCell:botButtonsByData	Ljava/util/HashMap;
    //   18341: invokevirtual 3660	java/util/HashMap:clear	()V
    //   18344: aload_0
    //   18345: getfield 335	org/telegram/ui/Cells/ChatMessageCell:botButtonsByPosition	Ljava/util/HashMap;
    //   18348: invokevirtual 3660	java/util/HashMap:clear	()V
    //   18351: aload_0
    //   18352: aconst_null
    //   18353: putfield 3662	org/telegram/ui/Cells/ChatMessageCell:botButtonsLayout	Ljava/lang/String;
    //   18356: aload_0
    //   18357: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   18360: ifnonnull +906 -> 19266
    //   18363: aload_1
    //   18364: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   18367: getfield 3666	org/telegram/tgnet/TLRPC$Message:reply_markup	Lorg/telegram/tgnet/TLRPC$ReplyMarkup;
    //   18370: instanceof 3668
    //   18373: ifeq +893 -> 19266
    //   18376: aload_1
    //   18377: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   18380: getfield 3666	org/telegram/tgnet/TLRPC$Message:reply_markup	Lorg/telegram/tgnet/TLRPC$ReplyMarkup;
    //   18383: getfield 3673	org/telegram/tgnet/TLRPC$ReplyMarkup:rows	Ljava/util/ArrayList;
    //   18386: invokevirtual 590	java/util/ArrayList:size	()I
    //   18389: istore 18
    //   18391: ldc_w 781
    //   18394: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18397: iload 18
    //   18399: imul
    //   18400: fconst_1
    //   18401: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18404: iadd
    //   18405: istore 13
    //   18407: aload_0
    //   18408: iload 13
    //   18410: putfield 3154	org/telegram/ui/Cells/ChatMessageCell:keyboardHeight	I
    //   18413: aload_0
    //   18414: iload 13
    //   18416: putfield 3112	org/telegram/ui/Cells/ChatMessageCell:substractBackgroundHeight	I
    //   18419: aload_0
    //   18420: aload_0
    //   18421: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   18424: putfield 586	org/telegram/ui/Cells/ChatMessageCell:widthForButtons	I
    //   18427: iconst_0
    //   18428: istore 14
    //   18430: aload_1
    //   18431: getfield 3676	org/telegram/messenger/MessageObject:wantedBotKeyboardWidth	I
    //   18434: aload_0
    //   18435: getfield 586	org/telegram/ui/Cells/ChatMessageCell:widthForButtons	I
    //   18438: if_icmple +74 -> 18512
    //   18441: aload_0
    //   18442: getfield 2040	org/telegram/ui/Cells/ChatMessageCell:isChat	Z
    //   18445: ifeq +195 -> 18640
    //   18448: aload_1
    //   18449: invokevirtual 2043	org/telegram/messenger/MessageObject:needDrawAvatar	()Z
    //   18452: ifeq +188 -> 18640
    //   18455: aload_1
    //   18456: invokevirtual 491	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   18459: ifne +181 -> 18640
    //   18462: ldc_w 3677
    //   18465: fstore 11
    //   18467: fload 11
    //   18469: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18472: ineg
    //   18473: istore 13
    //   18475: invokestatic 2226	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   18478: ifeq +170 -> 18648
    //   18481: iload 13
    //   18483: invokestatic 2258	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   18486: iadd
    //   18487: istore 13
    //   18489: aload_0
    //   18490: aload_0
    //   18491: getfield 348	org/telegram/ui/Cells/ChatMessageCell:backgroundWidth	I
    //   18494: aload_1
    //   18495: getfield 3676	org/telegram/messenger/MessageObject:wantedBotKeyboardWidth	I
    //   18498: iload 13
    //   18500: invokestatic 1195	java/lang/Math:min	(II)I
    //   18503: invokestatic 486	java/lang/Math:max	(II)I
    //   18506: putfield 586	org/telegram/ui/Cells/ChatMessageCell:widthForButtons	I
    //   18509: iconst_1
    //   18510: istore 14
    //   18512: iconst_0
    //   18513: istore 13
    //   18515: new 330	java/util/HashMap
    //   18518: dup
    //   18519: aload_0
    //   18520: getfield 333	org/telegram/ui/Cells/ChatMessageCell:botButtonsByData	Ljava/util/HashMap;
    //   18523: invokespecial 3680	java/util/HashMap:<init>	(Ljava/util/Map;)V
    //   18526: astore 38
    //   18528: aload_1
    //   18529: getfield 3683	org/telegram/messenger/MessageObject:botButtonsLayout	Ljava/lang/StringBuilder;
    //   18532: ifnull +139 -> 18671
    //   18535: aload_0
    //   18536: getfield 3662	org/telegram/ui/Cells/ChatMessageCell:botButtonsLayout	Ljava/lang/String;
    //   18539: ifnull +132 -> 18671
    //   18542: aload_0
    //   18543: getfield 3662	org/telegram/ui/Cells/ChatMessageCell:botButtonsLayout	Ljava/lang/String;
    //   18546: aload_1
    //   18547: getfield 3683	org/telegram/messenger/MessageObject:botButtonsLayout	Ljava/lang/StringBuilder;
    //   18550: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   18553: invokevirtual 2357	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   18556: ifeq +115 -> 18671
    //   18559: new 330	java/util/HashMap
    //   18562: dup
    //   18563: aload_0
    //   18564: getfield 335	org/telegram/ui/Cells/ChatMessageCell:botButtonsByPosition	Ljava/util/HashMap;
    //   18567: invokespecial 3680	java/util/HashMap:<init>	(Ljava/util/Map;)V
    //   18570: astore_2
    //   18571: aload_0
    //   18572: getfield 333	org/telegram/ui/Cells/ChatMessageCell:botButtonsByData	Ljava/util/HashMap;
    //   18575: invokevirtual 3660	java/util/HashMap:clear	()V
    //   18578: iconst_0
    //   18579: istore 15
    //   18581: iload 15
    //   18583: iload 18
    //   18585: if_icmpge +605 -> 19190
    //   18588: aload_1
    //   18589: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   18592: getfield 3666	org/telegram/tgnet/TLRPC$Message:reply_markup	Lorg/telegram/tgnet/TLRPC$ReplyMarkup;
    //   18595: getfield 3673	org/telegram/tgnet/TLRPC$ReplyMarkup:rows	Ljava/util/ArrayList;
    //   18598: iload 15
    //   18600: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   18603: checkcast 3685	org/telegram/tgnet/TLRPC$TL_keyboardButtonRow
    //   18606: astore 39
    //   18608: aload 39
    //   18610: getfield 3688	org/telegram/tgnet/TLRPC$TL_keyboardButtonRow:buttons	Ljava/util/ArrayList;
    //   18613: invokevirtual 590	java/util/ArrayList:size	()I
    //   18616: istore 16
    //   18618: iload 16
    //   18620: ifne +74 -> 18694
    //   18623: iload 13
    //   18625: istore 17
    //   18627: iload 15
    //   18629: iconst_1
    //   18630: iadd
    //   18631: istore 15
    //   18633: iload 17
    //   18635: istore 13
    //   18637: goto -56 -> 18581
    //   18640: ldc_w 587
    //   18643: fstore 11
    //   18645: goto -178 -> 18467
    //   18648: iload 13
    //   18650: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   18653: getfield 2253	android/graphics/Point:x	I
    //   18656: getstatic 2248	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   18659: getfield 2262	android/graphics/Point:y	I
    //   18662: invokestatic 1195	java/lang/Math:min	(II)I
    //   18665: iadd
    //   18666: istore 13
    //   18668: goto -179 -> 18489
    //   18671: aload_1
    //   18672: getfield 3683	org/telegram/messenger/MessageObject:botButtonsLayout	Ljava/lang/StringBuilder;
    //   18675: ifnull +14 -> 18689
    //   18678: aload_0
    //   18679: aload_1
    //   18680: getfield 3683	org/telegram/messenger/MessageObject:botButtonsLayout	Ljava/lang/StringBuilder;
    //   18683: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   18686: putfield 3662	org/telegram/ui/Cells/ChatMessageCell:botButtonsLayout	Ljava/lang/String;
    //   18689: aconst_null
    //   18690: astore_2
    //   18691: goto -120 -> 18571
    //   18694: aload_0
    //   18695: getfield 586	org/telegram/ui/Cells/ChatMessageCell:widthForButtons	I
    //   18698: istore 17
    //   18700: ldc_w 1775
    //   18703: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18706: istore 19
    //   18708: iload 14
    //   18710: ifne +397 -> 19107
    //   18713: aload_0
    //   18714: getfield 615	org/telegram/ui/Cells/ChatMessageCell:mediaBackground	Z
    //   18717: ifeq +390 -> 19107
    //   18720: fconst_0
    //   18721: fstore 11
    //   18723: iload 17
    //   18725: iload 19
    //   18727: iload 16
    //   18729: iconst_1
    //   18730: isub
    //   18731: imul
    //   18732: isub
    //   18733: fload 11
    //   18735: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18738: isub
    //   18739: fconst_2
    //   18740: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18743: isub
    //   18744: iload 16
    //   18746: idiv
    //   18747: istore 19
    //   18749: iconst_0
    //   18750: istore 16
    //   18752: iload 13
    //   18754: istore 17
    //   18756: iload 16
    //   18758: aload 39
    //   18760: getfield 3688	org/telegram/tgnet/TLRPC$TL_keyboardButtonRow:buttons	Ljava/util/ArrayList;
    //   18763: invokevirtual 590	java/util/ArrayList:size	()I
    //   18766: if_icmpge -139 -> 18627
    //   18769: new 16	org/telegram/ui/Cells/ChatMessageCell$BotButton
    //   18772: dup
    //   18773: aload_0
    //   18774: aconst_null
    //   18775: invokespecial 3691	org/telegram/ui/Cells/ChatMessageCell$BotButton:<init>	(Lorg/telegram/ui/Cells/ChatMessageCell;Lorg/telegram/ui/Cells/ChatMessageCell$1;)V
    //   18778: astore 40
    //   18780: aload 40
    //   18782: aload 39
    //   18784: getfield 3688	org/telegram/tgnet/TLRPC$TL_keyboardButtonRow:buttons	Ljava/util/ArrayList;
    //   18787: iload 16
    //   18789: invokevirtual 594	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   18792: checkcast 3693	org/telegram/tgnet/TLRPC$KeyboardButton
    //   18795: invokestatic 3697	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$502	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;Lorg/telegram/tgnet/TLRPC$KeyboardButton;)Lorg/telegram/tgnet/TLRPC$KeyboardButton;
    //   18798: pop
    //   18799: aload 40
    //   18801: invokestatic 622	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$500	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)Lorg/telegram/tgnet/TLRPC$KeyboardButton;
    //   18804: getfield 3700	org/telegram/tgnet/TLRPC$KeyboardButton:data	[B
    //   18807: invokestatic 3706	org/telegram/messenger/Utilities:bytesToHex	([B)Ljava/lang/String;
    //   18810: astore 41
    //   18812: new 1320	java/lang/StringBuilder
    //   18815: dup
    //   18816: invokespecial 1321	java/lang/StringBuilder:<init>	()V
    //   18819: iload 15
    //   18821: invokevirtual 3709	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   18824: ldc_w 2448
    //   18827: invokevirtual 1325	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   18830: iload 16
    //   18832: invokevirtual 3709	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
    //   18835: invokevirtual 1333	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   18838: astore 42
    //   18840: aload_2
    //   18841: ifnull +274 -> 19115
    //   18844: aload_2
    //   18845: aload 42
    //   18847: invokevirtual 3284	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   18850: checkcast 16	org/telegram/ui/Cells/ChatMessageCell$BotButton
    //   18853: astore 37
    //   18855: aload 37
    //   18857: ifnull +273 -> 19130
    //   18860: aload 40
    //   18862: aload 37
    //   18864: invokestatic 2138	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1100	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)F
    //   18867: invokestatic 2164	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1102	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;F)F
    //   18870: pop
    //   18871: aload 40
    //   18873: aload 37
    //   18875: invokestatic 2144	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1200	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)I
    //   18878: invokestatic 2159	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1202	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;I)I
    //   18881: pop
    //   18882: aload 40
    //   18884: aload 37
    //   18886: invokestatic 2149	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1300	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)J
    //   18889: invokestatic 2168	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1302	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;J)J
    //   18892: pop2
    //   18893: aload_0
    //   18894: getfield 333	org/telegram/ui/Cells/ChatMessageCell:botButtonsByData	Ljava/util/HashMap;
    //   18897: aload 41
    //   18899: aload 40
    //   18901: invokevirtual 3713	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   18904: pop
    //   18905: aload_0
    //   18906: getfield 335	org/telegram/ui/Cells/ChatMessageCell:botButtonsByPosition	Ljava/util/HashMap;
    //   18909: aload 42
    //   18911: aload 40
    //   18913: invokevirtual 3713	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   18916: pop
    //   18917: aload 40
    //   18919: ldc_w 1775
    //   18922: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18925: iload 19
    //   18927: iadd
    //   18928: iload 16
    //   18930: imul
    //   18931: invokestatic 3716	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$702	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;I)I
    //   18934: pop
    //   18935: aload 40
    //   18937: ldc_w 781
    //   18940: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18943: iload 15
    //   18945: imul
    //   18946: ldc_w 1775
    //   18949: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18952: iadd
    //   18953: invokestatic 3719	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$602	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;I)I
    //   18956: pop
    //   18957: aload 40
    //   18959: iload 19
    //   18961: invokestatic 3722	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$802	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;I)I
    //   18964: pop
    //   18965: aload 40
    //   18967: ldc_w 1819
    //   18970: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   18973: invokestatic 3725	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$902	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;I)I
    //   18976: pop
    //   18977: aload 40
    //   18979: invokestatic 622	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$500	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)Lorg/telegram/tgnet/TLRPC$KeyboardButton;
    //   18982: instanceof 2122
    //   18985: ifeq +157 -> 19142
    //   18988: aload_1
    //   18989: getfield 816	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   18992: getfield 822	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   18995: getfield 3443	org/telegram/tgnet/TLRPC$MessageMedia:flags	I
    //   18998: iconst_4
    //   18999: iand
    //   19000: ifeq +142 -> 19142
    //   19003: ldc_w 3445
    //   19006: ldc_w 3446
    //   19009: invokestatic 934	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   19012: astore 37
    //   19014: aload 40
    //   19016: new 350	android/text/StaticLayout
    //   19019: dup
    //   19020: aload 37
    //   19022: getstatic 3728	org/telegram/ui/ActionBar/Theme:chat_botButtonPaint	Landroid/text/TextPaint;
    //   19025: iload 19
    //   19027: ldc_w 587
    //   19030: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19033: isub
    //   19034: getstatic 3731	android/text/Layout$Alignment:ALIGN_CENTER	Landroid/text/Layout$Alignment;
    //   19037: fconst_1
    //   19038: fconst_0
    //   19039: iconst_0
    //   19040: invokespecial 949	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   19043: invokestatic 3735	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1402	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;Landroid/text/StaticLayout;)Landroid/text/StaticLayout;
    //   19046: pop
    //   19047: aload_0
    //   19048: getfield 328	org/telegram/ui/Cells/ChatMessageCell:botButtons	Ljava/util/ArrayList;
    //   19051: aload 40
    //   19053: invokevirtual 2523	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   19056: pop
    //   19057: iload 13
    //   19059: istore 17
    //   19061: iload 16
    //   19063: aload 39
    //   19065: getfield 3688	org/telegram/tgnet/TLRPC$TL_keyboardButtonRow:buttons	Ljava/util/ArrayList;
    //   19068: invokevirtual 590	java/util/ArrayList:size	()I
    //   19071: iconst_1
    //   19072: isub
    //   19073: if_icmpne +21 -> 19094
    //   19076: iload 13
    //   19078: aload 40
    //   19080: invokestatic 603	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$700	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)I
    //   19083: aload 40
    //   19085: invokestatic 606	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$800	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)I
    //   19088: iadd
    //   19089: invokestatic 486	java/lang/Math:max	(II)I
    //   19092: istore 17
    //   19094: iload 16
    //   19096: iconst_1
    //   19097: iadd
    //   19098: istore 16
    //   19100: iload 17
    //   19102: istore 13
    //   19104: goto -352 -> 18752
    //   19107: ldc_w 1705
    //   19110: fstore 11
    //   19112: goto -389 -> 18723
    //   19115: aload 38
    //   19117: aload 41
    //   19119: invokevirtual 3284	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   19122: checkcast 16	org/telegram/ui/Cells/ChatMessageCell$BotButton
    //   19125: astore 37
    //   19127: goto -272 -> 18855
    //   19130: aload 40
    //   19132: invokestatic 1617	java/lang/System:currentTimeMillis	()J
    //   19135: invokestatic 2168	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$1302	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;J)J
    //   19138: pop2
    //   19139: goto -246 -> 18893
    //   19142: aload 40
    //   19144: invokestatic 622	org/telegram/ui/Cells/ChatMessageCell$BotButton:access$500	(Lorg/telegram/ui/Cells/ChatMessageCell$BotButton;)Lorg/telegram/tgnet/TLRPC$KeyboardButton;
    //   19147: getfield 3738	org/telegram/tgnet/TLRPC$KeyboardButton:text	Ljava/lang/String;
    //   19150: getstatic 3728	org/telegram/ui/ActionBar/Theme:chat_botButtonPaint	Landroid/text/TextPaint;
    //   19153: invokevirtual 2674	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   19156: ldc_w 1703
    //   19159: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19162: iconst_0
    //   19163: invokestatic 2680	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   19166: getstatic 3728	org/telegram/ui/ActionBar/Theme:chat_botButtonPaint	Landroid/text/TextPaint;
    //   19169: iload 19
    //   19171: ldc_w 587
    //   19174: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19177: isub
    //   19178: i2f
    //   19179: getstatic 1217	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   19182: invokestatic 1221	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   19185: astore 37
    //   19187: goto -173 -> 19014
    //   19190: aload_0
    //   19191: iload 13
    //   19193: putfield 586	org/telegram/ui/Cells/ChatMessageCell:widthForButtons	I
    //   19196: aload_0
    //   19197: getfield 1623	org/telegram/ui/Cells/ChatMessageCell:drawPinnedBottom	Z
    //   19200: ifeq +79 -> 19279
    //   19203: aload_0
    //   19204: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   19207: ifeq +72 -> 19279
    //   19210: aload_0
    //   19211: aload_0
    //   19212: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19215: fconst_2
    //   19216: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19219: isub
    //   19220: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19223: aload_1
    //   19224: getfield 770	org/telegram/messenger/MessageObject:type	I
    //   19227: bipush 13
    //   19229: if_icmpne +26 -> 19255
    //   19232: aload_0
    //   19233: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19236: ldc_w 3597
    //   19239: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19242: if_icmpge +13 -> 19255
    //   19245: aload_0
    //   19246: ldc_w 3597
    //   19249: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19252: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19255: aload_0
    //   19256: invokespecial 3192	org/telegram/ui/Cells/ChatMessageCell:updateWaveform	()V
    //   19259: aload_0
    //   19260: iload 34
    //   19262: invokevirtual 2753	org/telegram/ui/Cells/ChatMessageCell:updateButtonState	(Z)V
    //   19265: return
    //   19266: aload_0
    //   19267: iconst_0
    //   19268: putfield 3112	org/telegram/ui/Cells/ChatMessageCell:substractBackgroundHeight	I
    //   19271: aload_0
    //   19272: iconst_0
    //   19273: putfield 3154	org/telegram/ui/Cells/ChatMessageCell:keyboardHeight	I
    //   19276: goto -80 -> 19196
    //   19279: aload_0
    //   19280: getfield 1623	org/telegram/ui/Cells/ChatMessageCell:drawPinnedBottom	Z
    //   19283: ifeq +19 -> 19302
    //   19286: aload_0
    //   19287: aload_0
    //   19288: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19291: fconst_1
    //   19292: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19295: isub
    //   19296: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19299: goto -76 -> 19223
    //   19302: aload_0
    //   19303: getfield 1783	org/telegram/ui/Cells/ChatMessageCell:drawPinnedTop	Z
    //   19306: ifeq -83 -> 19223
    //   19309: aload_0
    //   19310: getfield 953	org/telegram/ui/Cells/ChatMessageCell:pinnedBottom	Z
    //   19313: ifeq -90 -> 19223
    //   19316: aload_0
    //   19317: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   19320: ifnull -97 -> 19223
    //   19323: aload_0
    //   19324: getfield 962	org/telegram/ui/Cells/ChatMessageCell:currentPosition	Lorg/telegram/messenger/MessageObject$GroupedMessagePosition;
    //   19327: getfield 3641	org/telegram/messenger/MessageObject$GroupedMessagePosition:siblingHeights	[F
    //   19330: ifnonnull -107 -> 19223
    //   19333: aload_0
    //   19334: aload_0
    //   19335: getfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19338: fconst_1
    //   19339: invokestatic 373	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   19342: isub
    //   19343: putfield 477	org/telegram/ui/Cells/ChatMessageCell:totalHeight	I
    //   19346: goto -123 -> 19223
    //   19349: astore 42
    //   19351: iload 16
    //   19353: istore 14
    //   19355: goto -14726 -> 4629
    //   19358: astore 44
    //   19360: iconst_3
    //   19361: istore 14
    //   19363: iload 23
    //   19365: istore 19
    //   19367: goto -15437 -> 3930
    //   19370: goto -9252 -> 10118
    //   19373: goto -9804 -> 9569
    //   19376: goto -11074 -> 8302
    //   19379: iload 13
    //   19381: istore 15
    //   19383: goto -14578 -> 4805
    //   19386: iload 16
    //   19388: istore 15
    //   19390: iload 13
    //   19392: istore 16
    //   19394: iload 19
    //   19396: istore 23
    //   19398: goto -15168 -> 4230
    //   19401: iload 33
    //   19403: ifeq +23 -> 19426
    //   19406: iload 30
    //   19408: iload 33
    //   19410: isub
    //   19411: istore 13
    //   19413: goto -12770 -> 6643
    //   19416: iload 29
    //   19418: iload 27
    //   19420: iadd
    //   19421: istore 13
    //   19423: goto -12656 -> 6767
    //   19426: iload 24
    //   19428: ifeq -12632 -> 6796
    //   19431: iload 30
    //   19433: istore 13
    //   19435: goto -12792 -> 6643
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	19438	0	this	ChatMessageCell
    //   0	19438	1	paramMessageObject	MessageObject
    //   0	19438	2	paramGroupedMessages	MessageObject.GroupedMessages
    //   0	19438	3	paramBoolean1	boolean
    //   0	19438	4	paramBoolean2	boolean
    //   1003	15292	5	d1	double
    //   1018	15263	7	d2	double
    //   12194	4084	9	d3	double
    //   6099	13012	11	f1	float
    //   6117	10677	12	f2	float
    //   108	19326	13	i	int
    //   112	19250	14	j	int
    //   72	19317	15	k	int
    //   1675	17718	16	m	int
    //   978	18123	17	n	int
    //   843	17743	18	i1	int
    //   1678	17717	19	i2	int
    //   2038	16035	20	i3	int
    //   1832	14708	21	i4	int
    //   2044	15074	22	i5	int
    //   2339	17058	23	i6	int
    //   846	18581	24	i7	int
    //   54	18279	25	i8	int
    //   1064	16198	26	i9	int
    //   2035	17386	27	i10	int
    //   2342	14456	28	i11	int
    //   2336	17085	29	i12	int
    //   2674	16758	30	i13	int
    //   849	10144	31	i14	int
    //   2051	4774	32	i15	int
    //   2585	16826	33	i16	int
    //   97	19164	34	bool	boolean
    //   12536	264	35	l	long
    //   155	17902	37	localObject1	Object
    //   18237	3	37	localException1	Exception
    //   18853	333	37	localObject2	Object
    //   1328	17788	38	localObject3	Object
    //   1790	17274	39	localObject4	Object
    //   1808	2427	40	str1	String
    //   4693	3	40	localException2	Exception
    //   6854	12289	40	localObject5	Object
    //   1814	17304	41	localObject6	Object
    //   1802	2748	42	str2	String
    //   4623	7	42	localException3	Exception
    //   18838	72	42	str3	String
    //   19349	1	42	localException4	Exception
    //   1796	2173	43	str4	String
    //   3831	3	44	localException5	Exception
    //   3920	11	44	localException6	Exception
    //   19358	1	44	localException7	Exception
    // Exception table:
    //   from	to	target	type
    //   2097	2114	3831	java/lang/Exception
    //   2122	2151	3831	java/lang/Exception
    //   2159	2172	3831	java/lang/Exception
    //   2182	2187	3831	java/lang/Exception
    //   2195	2213	3831	java/lang/Exception
    //   2221	2232	3831	java/lang/Exception
    //   2240	2251	3831	java/lang/Exception
    //   2265	2274	3831	java/lang/Exception
    //   2282	2288	3831	java/lang/Exception
    //   2296	2308	3831	java/lang/Exception
    //   2316	2327	3831	java/lang/Exception
    //   2468	2486	3920	java/lang/Exception
    //   2498	2509	3920	java/lang/Exception
    //   2521	2532	3920	java/lang/Exception
    //   2551	2563	3920	java/lang/Exception
    //   2575	2587	3920	java/lang/Exception
    //   2607	2617	3920	java/lang/Exception
    //   2629	2636	3920	java/lang/Exception
    //   2653	2665	3920	java/lang/Exception
    //   2697	2704	3920	java/lang/Exception
    //   2716	2727	3920	java/lang/Exception
    //   2739	2751	3920	java/lang/Exception
    //   2763	2775	3920	java/lang/Exception
    //   3903	3917	3920	java/lang/Exception
    //   4510	4525	3920	java/lang/Exception
    //   3973	4006	4623	java/lang/Exception
    //   4012	4019	4623	java/lang/Exception
    //   4024	4048	4623	java/lang/Exception
    //   4548	4583	4623	java/lang/Exception
    //   4243	4248	4693	java/lang/Exception
    //   4252	4259	4693	java/lang/Exception
    //   4263	4270	4693	java/lang/Exception
    //   4274	4287	4693	java/lang/Exception
    //   4291	4304	4693	java/lang/Exception
    //   4317	4324	4693	java/lang/Exception
    //   4328	4361	4693	java/lang/Exception
    //   4369	4387	4693	java/lang/Exception
    //   4391	4402	4693	java/lang/Exception
    //   4406	4417	4693	java/lang/Exception
    //   4427	4439	4693	java/lang/Exception
    //   4443	4459	4693	java/lang/Exception
    //   4471	4478	4693	java/lang/Exception
    //   4482	4489	4693	java/lang/Exception
    //   4657	4686	4693	java/lang/Exception
    //   6534	6548	4693	java/lang/Exception
    //   6555	6564	4693	java/lang/Exception
    //   6583	6595	4693	java/lang/Exception
    //   6599	6615	4693	java/lang/Exception
    //   6624	6631	4693	java/lang/Exception
    //   6635	6640	4693	java/lang/Exception
    //   6676	6683	4693	java/lang/Exception
    //   6687	6698	4693	java/lang/Exception
    //   6721	6738	4693	java/lang/Exception
    //   6747	6764	4693	java/lang/Exception
    //   6771	6783	4693	java/lang/Exception
    //   6800	6821	4693	java/lang/Exception
    //   16566	16574	17236	java/lang/Exception
    //   16578	16621	17236	java/lang/Exception
    //   16633	16643	17236	java/lang/Exception
    //   16647	16653	17236	java/lang/Exception
    //   16657	16668	17236	java/lang/Exception
    //   16672	16687	17236	java/lang/Exception
    //   16691	16698	17236	java/lang/Exception
    //   16702	16715	17236	java/lang/Exception
    //   16719	16727	17236	java/lang/Exception
    //   16731	16749	17236	java/lang/Exception
    //   16753	16771	17236	java/lang/Exception
    //   16783	16804	17236	java/lang/Exception
    //   16808	16819	17236	java/lang/Exception
    //   16823	16838	17236	java/lang/Exception
    //   17207	17233	17236	java/lang/Exception
    //   17256	17261	17236	java/lang/Exception
    //   5928	6011	18160	java/lang/Exception
    //   6015	6052	18160	java/lang/Exception
    //   6052	6119	18160	java/lang/Exception
    //   6123	6179	18160	java/lang/Exception
    //   18131	18157	18160	java/lang/Exception
    //   6257	6326	18237	java/lang/Exception
    //   6328	6373	18237	java/lang/Exception
    //   6373	6398	18264	java/lang/Exception
    //   6398	6471	18264	java/lang/Exception
    //   6474	6502	18264	java/lang/Exception
    //   6507	6521	18264	java/lang/Exception
    //   18247	18261	18264	java/lang/Exception
    //   4060	4078	19349	java/lang/Exception
    //   4086	4097	19349	java/lang/Exception
    //   4105	4116	19349	java/lang/Exception
    //   4124	4135	19349	java/lang/Exception
    //   4143	4150	19349	java/lang/Exception
    //   4163	4175	19349	java/lang/Exception
    //   4186	4198	19349	java/lang/Exception
    //   4206	4218	19349	java/lang/Exception
    //   4601	4615	19349	java/lang/Exception
    //   2368	2408	19358	java/lang/Exception
    //   2411	2418	19358	java/lang/Exception
    //   2423	2453	19358	java/lang/Exception
    //   3848	3882	19358	java/lang/Exception
  }
  
  public void setPressed(boolean paramBoolean)
  {
    super.setPressed(paramBoolean);
    updateRadialProgressBackground();
    if (this.useSeekBarWaweform) {
      this.seekBarWaveform.setSelected(isDrawSelectedBackground());
    }
    for (;;)
    {
      invalidate();
      return;
      this.seekBar.setSelected(isDrawSelectedBackground());
    }
  }
  
  public void setVisiblePart(int paramInt1, int paramInt2)
  {
    if ((this.currentMessageObject == null) || (this.currentMessageObject.textLayoutBlocks == null)) {}
    int j;
    int k;
    int i;
    label85:
    label200:
    do
    {
      return;
      int i2 = paramInt1 - this.textY;
      int m = -1;
      j = -1;
      k = 0;
      i = 0;
      paramInt1 = 0;
      float f;
      int i1;
      int n;
      if ((paramInt1 >= this.currentMessageObject.textLayoutBlocks.size()) || (((MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(paramInt1)).textYOffset > i2))
      {
        paramInt1 = i;
        i = m;
        if (paramInt1 >= this.currentMessageObject.textLayoutBlocks.size()) {
          continue;
        }
        MessageObject.TextLayoutBlock localTextLayoutBlock = (MessageObject.TextLayoutBlock)this.currentMessageObject.textLayoutBlocks.get(paramInt1);
        f = localTextLayoutBlock.textYOffset;
        if (!intersect(f, localTextLayoutBlock.height + f, i2, i2 + paramInt2)) {
          break label200;
        }
        j = i;
        if (i == -1) {
          j = paramInt1;
        }
        m = paramInt1;
        i1 = k + 1;
        n = j;
      }
      do
      {
        paramInt1 += 1;
        k = i1;
        i = n;
        j = m;
        break label85;
        i = paramInt1;
        paramInt1 += 1;
        break;
        i1 = k;
        n = i;
        m = j;
      } while (f <= i2);
    } while ((this.lastVisibleBlockNum == j) && (this.firstVisibleBlockNum == i) && (this.totalVisibleBlocksCount == k));
    this.lastVisibleBlockNum = j;
    this.firstVisibleBlockNum = i;
    this.totalVisibleBlocksCount = k;
    invalidate();
  }
  
  public void updateButtonState(boolean paramBoolean)
  {
    this.drawRadialCheckBackground = false;
    Object localObject1 = null;
    boolean bool1 = false;
    boolean bool2;
    if (this.currentMessageObject.type == 1)
    {
      if (this.currentPhotoObject == null) {
        return;
      }
      localObject1 = FileLoader.getAttachFileName(this.currentPhotoObject);
      bool1 = this.currentMessageObject.mediaExists;
      bool2 = bool1;
      if (SharedConfig.streamMedia)
      {
        bool2 = bool1;
        if ((int)this.currentMessageObject.getDialogId() != 0)
        {
          bool2 = bool1;
          if (!this.currentMessageObject.isSecretMedia()) {
            if (this.documentAttachType != 5)
            {
              bool2 = bool1;
              if (this.documentAttachType == 4)
              {
                bool2 = bool1;
                if (!this.currentMessageObject.canStreamVideo()) {}
              }
            }
            else
            {
              if (!bool1) {
                break label393;
              }
            }
          }
        }
      }
    }
    label393:
    for (int i = 1;; i = 2)
    {
      this.hasMiniProgress = i;
      bool2 = true;
      if (!TextUtils.isEmpty((CharSequence)localObject1)) {
        break label398;
      }
      this.radialProgress.setBackground(null, false, false);
      this.radialProgress.setMiniBackground(null, false, false);
      return;
      if ((this.currentMessageObject.type == 8) || (this.currentMessageObject.type == 5) || (this.documentAttachType == 7) || (this.documentAttachType == 4) || (this.currentMessageObject.type == 9) || (this.documentAttachType == 3) || (this.documentAttachType == 5))
      {
        if (this.currentMessageObject.useCustomPhoto)
        {
          this.buttonState = 1;
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
          return;
        }
        if (this.currentMessageObject.attachPathExists)
        {
          localObject1 = this.currentMessageObject.messageOwner.attachPath;
          bool1 = true;
          break;
        }
        if ((this.currentMessageObject.isSendError()) && (this.documentAttachType != 3) && (this.documentAttachType != 5)) {
          break;
        }
        localObject1 = this.currentMessageObject.getFileName();
        bool1 = this.currentMessageObject.mediaExists;
        break;
      }
      if (this.documentAttachType != 0)
      {
        localObject1 = FileLoader.getAttachFileName(this.documentAttach);
        bool1 = this.currentMessageObject.mediaExists;
        break;
      }
      if (this.currentPhotoObject == null) {
        break;
      }
      localObject1 = FileLoader.getAttachFileName(this.currentPhotoObject);
      bool1 = this.currentMessageObject.mediaExists;
      break;
    }
    label398:
    label432:
    Object localObject2;
    if ((this.currentMessageObject.messageOwner.params != null) && (this.currentMessageObject.messageOwner.params.containsKey("query_id")))
    {
      i = 1;
      if ((this.documentAttachType != 3) && (this.documentAttachType != 5)) {
        break label1141;
      }
      if (((!this.currentMessageObject.isOut()) || (!this.currentMessageObject.isSending())) && ((!this.currentMessageObject.isSendError()) || (i == 0))) {
        break label676;
      }
      DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
      this.buttonState = 4;
      localObject1 = this.radialProgress;
      localObject2 = getDrawableForCurrentState();
      if (i != 0) {
        break label653;
      }
      bool1 = true;
      label531:
      ((RadialProgress)localObject1).setBackground((Drawable)localObject2, bool1, paramBoolean);
      if (i != 0) {
        break label664;
      }
      localObject2 = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        localObject1 = localObject2;
        if (SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId())) {
          localObject1 = Float.valueOf(1.0F);
        }
      }
      localObject2 = this.radialProgress;
      if (localObject1 == null) {
        break label659;
      }
      f = ((Float)localObject1).floatValue();
      label619:
      ((RadialProgress)localObject2).setProgress(f, false);
      label626:
      updatePlayingMessageProgress();
    }
    for (;;)
    {
      label630:
      if (this.hasMiniProgress == 0)
      {
        this.radialProgress.setMiniBackground(null, false, paramBoolean);
        return;
        i = 0;
        break label432;
        label653:
        bool1 = false;
        break label531;
        label659:
        f = 0.0F;
        break label619;
        label664:
        this.radialProgress.setProgress(0.0F, false);
        break label626;
        label676:
        if (this.hasMiniProgress != 0)
        {
          RadialProgress localRadialProgress = this.radialProgress;
          if (this.currentMessageObject.isOutOwner())
          {
            localObject2 = "chat_outLoader";
            label704:
            localRadialProgress.setMiniProgressBackgroundColor(Theme.getColor((String)localObject2));
            bool1 = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
            if ((bool1) && ((!bool1) || (!MediaController.getInstance().isMessagePaused()))) {
              break label831;
            }
            this.buttonState = 0;
            label750:
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
            if (this.hasMiniProgress != 1) {
              break label839;
            }
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
            this.miniButtonState = -1;
            label787:
            localObject1 = this.radialProgress;
            localObject2 = getMiniDrawableForCurrentState();
            if (this.miniButtonState != 1) {
              break label936;
            }
          }
          label831:
          label839:
          label936:
          for (bool1 = true;; bool1 = false)
          {
            ((RadialProgress)localObject1).setMiniBackground((Drawable)localObject2, bool1, paramBoolean);
            break;
            localObject2 = "chat_inLoader";
            break label704;
            this.buttonState = 1;
            break label750;
            DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this.currentMessageObject, this);
            if (!FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1))
            {
              this.radialProgress.setProgress(0.0F, paramBoolean);
              this.miniButtonState = 0;
              break label787;
            }
            this.miniButtonState = 1;
            localObject1 = ImageLoader.getInstance().getFileProgress((String)localObject1);
            if (localObject1 != null)
            {
              this.radialProgress.setProgress(((Float)localObject1).floatValue(), paramBoolean);
              break label787;
            }
            this.radialProgress.setProgress(0.0F, paramBoolean);
            break label787;
          }
        }
        if (bool2)
        {
          DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
          bool1 = MediaController.getInstance().isPlayingMessage(this.currentMessageObject);
          if ((!bool1) || ((bool1) && (MediaController.getInstance().isMessagePaused()))) {}
          for (this.buttonState = 0;; this.buttonState = 1)
          {
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
            break;
          }
        }
        DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this.currentMessageObject, this);
        if (!FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1))
        {
          this.buttonState = 2;
          this.radialProgress.setProgress(0.0F, paramBoolean);
          this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
          break label626;
        }
        this.buttonState = 4;
        localObject1 = ImageLoader.getInstance().getFileProgress((String)localObject1);
        if (localObject1 != null) {
          this.radialProgress.setProgress(((Float)localObject1).floatValue(), paramBoolean);
        }
        for (;;)
        {
          this.radialProgress.setBackground(getDrawableForCurrentState(), true, paramBoolean);
          break;
          this.radialProgress.setProgress(0.0F, paramBoolean);
        }
        label1141:
        if ((this.currentMessageObject.type == 0) && (this.documentAttachType != 1) && (this.documentAttachType != 4))
        {
          if ((this.currentPhotoObject == null) || (!this.drawImageButton)) {
            break;
          }
          if (!bool2)
          {
            DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this.currentMessageObject, this);
            f = 0.0F;
            bool1 = false;
            if (!FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1))
            {
              if ((!this.cancelLoading) && (((this.documentAttachType == 0) && (DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject))) || ((this.documentAttachType == 2) && (MessageObject.isNewGifDocument(this.documentAttach)) && (DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject))))) {
                bool1 = true;
              }
              for (this.buttonState = 1;; this.buttonState = 0)
              {
                this.radialProgress.setProgress(f, false);
                this.radialProgress.setBackground(getDrawableForCurrentState(), bool1, paramBoolean);
                invalidate();
                break;
              }
            }
            bool1 = true;
            this.buttonState = 1;
            localObject1 = ImageLoader.getInstance().getFileProgress((String)localObject1);
            if (localObject1 != null) {}
            for (f = ((Float)localObject1).floatValue();; f = 0.0F) {
              break;
            }
          }
          DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
          if ((this.documentAttachType == 2) && (!this.photoImage.isAllowStartAnimation())) {}
          for (this.buttonState = 2;; this.buttonState = -1)
          {
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
            invalidate();
            break;
          }
        }
        if ((this.currentMessageObject.isOut()) && (this.currentMessageObject.isSending()))
        {
          if ((this.currentMessageObject.messageOwner.attachPath != null) && (this.currentMessageObject.messageOwner.attachPath.length() > 0))
          {
            DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(this.currentMessageObject.messageOwner.attachPath, this.currentMessageObject, this);
            if ((this.currentMessageObject.messageOwner.attachPath == null) || (!this.currentMessageObject.messageOwner.attachPath.startsWith("http")))
            {
              bool1 = true;
              label1543:
              localObject1 = this.currentMessageObject.messageOwner.params;
              if ((this.currentMessageObject.messageOwner.message == null) || (localObject1 == null) || ((!((HashMap)localObject1).containsKey("url")) && (!((HashMap)localObject1).containsKey("bot")))) {
                break label1740;
              }
              bool1 = false;
              this.buttonState = -1;
              label1603:
              bool2 = SendMessagesHelper.getInstance(this.currentAccount).isSendingMessage(this.currentMessageObject.getId());
              if ((this.currentPosition == null) || (!bool2) || (this.buttonState != 1)) {
                break label1748;
              }
              this.drawRadialCheckBackground = true;
              this.radialProgress.setCheckBackground(false, paramBoolean);
              label1656:
              if (!bool1) {
                break label1770;
              }
              localObject2 = ImageLoader.getInstance().getFileProgress(this.currentMessageObject.messageOwner.attachPath);
              localObject1 = localObject2;
              if (localObject2 == null)
              {
                localObject1 = localObject2;
                if (bool2) {
                  localObject1 = Float.valueOf(1.0F);
                }
              }
              localObject2 = this.radialProgress;
              if (localObject1 == null) {
                break label1765;
              }
              f = ((Float)localObject1).floatValue();
              label1720:
              ((RadialProgress)localObject2).setProgress(f, false);
            }
            for (;;)
            {
              invalidate();
              break;
              bool1 = false;
              break label1543;
              label1740:
              this.buttonState = 1;
              break label1603;
              label1748:
              this.radialProgress.setBackground(getDrawableForCurrentState(), bool1, paramBoolean);
              break label1656;
              label1765:
              f = 0.0F;
              break label1720;
              label1770:
              this.radialProgress.setProgress(0.0F, false);
            }
          }
        }
        else
        {
          if ((this.currentMessageObject.messageOwner.attachPath != null) && (this.currentMessageObject.messageOwner.attachPath.length() != 0)) {
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
          }
          if (this.hasMiniProgress != 0)
          {
            this.radialProgress.setMiniProgressBackgroundColor(Theme.getColor("chat_inLoaderPhoto"));
            this.buttonState = 3;
            this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
            if (this.hasMiniProgress == 1)
            {
              DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
              this.miniButtonState = -1;
              label1884:
              localObject1 = this.radialProgress;
              localObject2 = getMiniDrawableForCurrentState();
              if (this.miniButtonState != 1) {
                break label2017;
              }
            }
            label2017:
            for (bool1 = true;; bool1 = false)
            {
              ((RadialProgress)localObject1).setMiniBackground((Drawable)localObject2, bool1, paramBoolean);
              break;
              DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this.currentMessageObject, this);
              if (!FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1))
              {
                this.radialProgress.setProgress(0.0F, paramBoolean);
                this.miniButtonState = 0;
                break label1884;
              }
              this.miniButtonState = 1;
              localObject1 = ImageLoader.getInstance().getFileProgress((String)localObject1);
              if (localObject1 != null)
              {
                this.radialProgress.setProgress(((Float)localObject1).floatValue(), paramBoolean);
                break label1884;
              }
              this.radialProgress.setProgress(0.0F, paramBoolean);
              break label1884;
            }
          }
          if (bool2)
          {
            DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
            if (this.currentMessageObject.needDrawBluredPreview()) {
              this.buttonState = -1;
            }
            for (;;)
            {
              this.radialProgress.setBackground(getDrawableForCurrentState(), false, paramBoolean);
              if (this.photoNotSet) {
                setMessageObject(this.currentMessageObject, this.currentMessagesGroup, this.pinnedBottom, this.pinnedTop);
              }
              invalidate();
              break;
              if ((this.currentMessageObject.type == 8) && (!this.photoImage.isAllowStartAnimation())) {
                this.buttonState = 2;
              } else if (this.documentAttachType == 4) {
                this.buttonState = 3;
              } else {
                this.buttonState = -1;
              }
            }
          }
          DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this.currentMessageObject, this);
          f = 0.0F;
          bool2 = false;
          if (FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1)) {
            break label2364;
          }
          bool1 = false;
          if (this.currentMessageObject.type != 1) {
            break label2272;
          }
          bool1 = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
          label2222:
          if ((this.cancelLoading) || (!bool1)) {
            break label2352;
          }
          bool1 = true;
          this.buttonState = 1;
        }
      }
    }
    for (;;)
    {
      this.radialProgress.setBackground(getDrawableForCurrentState(), bool1, paramBoolean);
      this.radialProgress.setProgress(f, false);
      invalidate();
      break label630;
      break;
      label2272:
      if ((this.currentMessageObject.type == 8) && (MessageObject.isNewGifDocument(this.currentMessageObject.messageOwner.media.document)))
      {
        bool1 = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
        break label2222;
      }
      if (this.currentMessageObject.type != 5) {
        break label2222;
      }
      bool1 = DownloadController.getInstance(this.currentAccount).canDownloadMedia(this.currentMessageObject);
      break label2222;
      label2352:
      this.buttonState = 0;
      bool1 = bool2;
    }
    label2364:
    bool1 = true;
    this.buttonState = 1;
    localObject1 = ImageLoader.getInstance().getFileProgress((String)localObject1);
    if (localObject1 != null) {}
    for (float f = ((Float)localObject1).floatValue();; f = 0.0F) {
      break;
    }
  }
  
  public void updatePlayingMessageProgress()
  {
    if (this.currentMessageObject == null) {}
    int k;
    int j;
    int i;
    do
    {
      return;
      if (this.currentMessageObject.isRoundVideo())
      {
        k = 0;
        localObject = this.currentMessageObject.getDocument();
        j = 0;
        for (;;)
        {
          i = k;
          if (j < ((TLRPC.Document)localObject).attributes.size())
          {
            TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)((TLRPC.Document)localObject).attributes.get(j);
            if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
              i = localDocumentAttribute.duration;
            }
          }
          else
          {
            j = i;
            if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
              j = Math.max(0, i - this.currentMessageObject.audioProgressSec);
            }
            if (this.lastTime == j) {
              break;
            }
            this.lastTime = j;
            localObject = String.format("%02d:%02d", new Object[] { Integer.valueOf(j / 60), Integer.valueOf(j % 60) });
            this.timeWidthAudio = ((int)Math.ceil(Theme.chat_timePaint.measureText((String)localObject)));
            this.durationLayout = new StaticLayout((CharSequence)localObject, Theme.chat_timePaint, this.timeWidthAudio, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            invalidate();
            return;
          }
          j += 1;
        }
      }
    } while (this.documentAttach == null);
    if (this.useSeekBarWaweform)
    {
      if (!this.seekBarWaveform.isDragging()) {
        this.seekBarWaveform.setProgress(this.currentMessageObject.audioProgress);
      }
      k = 0;
      if (this.documentAttachType != 3) {
        break label463;
      }
      if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
        break label452;
      }
      j = 0;
      label265:
      i = k;
      if (j < this.documentAttach.attributes.size())
      {
        localObject = (TLRPC.DocumentAttribute)this.documentAttach.attributes.get(j);
        if (!(localObject instanceof TLRPC.TL_documentAttributeAudio)) {
          break label445;
        }
        i = ((TLRPC.DocumentAttribute)localObject).duration;
      }
      label311:
      if (this.lastTime != i)
      {
        this.lastTime = i;
        localObject = String.format("%02d:%02d", new Object[] { Integer.valueOf(i / 60), Integer.valueOf(i % 60) });
        this.timeWidthAudio = ((int)Math.ceil(Theme.chat_audioTimePaint.measureText((String)localObject)));
        this.durationLayout = new StaticLayout((CharSequence)localObject, Theme.chat_audioTimePaint, this.timeWidthAudio, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      }
    }
    label445:
    label452:
    label463:
    do
    {
      invalidate();
      return;
      if (this.seekBar.isDragging()) {
        break;
      }
      this.seekBar.setProgress(this.currentMessageObject.audioProgress);
      this.seekBar.setBufferedProgress(this.currentMessageObject.bufferedProgress);
      break;
      j += 1;
      break label265;
      i = this.currentMessageObject.audioProgressSec;
      break label311;
      i = 0;
      j = this.currentMessageObject.getDuration();
      if (MediaController.getInstance().isPlayingMessage(this.currentMessageObject)) {
        i = this.currentMessageObject.audioProgressSec;
      }
    } while (this.lastTime == i);
    this.lastTime = i;
    if (j == 0) {}
    for (Object localObject = String.format("%d:%02d / -:--", new Object[] { Integer.valueOf(i / 60), Integer.valueOf(i % 60) });; localObject = String.format("%d:%02d / %d:%02d", new Object[] { Integer.valueOf(i / 60), Integer.valueOf(i % 60), Integer.valueOf(j / 60), Integer.valueOf(j % 60) }))
    {
      i = (int)Math.ceil(Theme.chat_audioTimePaint.measureText((String)localObject));
      this.durationLayout = new StaticLayout((CharSequence)localObject, Theme.chat_audioTimePaint, i, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      break;
    }
  }
  
  protected boolean verifyDrawable(Drawable paramDrawable)
  {
    return (super.verifyDrawable(paramDrawable)) || (paramDrawable == this.instantViewSelectorDrawable);
  }
  
  private class BotButton
  {
    private int angle;
    private TLRPC.KeyboardButton button;
    private int height;
    private long lastUpdateTime;
    private float progressAlpha;
    private StaticLayout title;
    private int width;
    private int x;
    private int y;
    
    private BotButton() {}
  }
  
  public static abstract interface ChatMessageCellDelegate
  {
    public abstract boolean canPerformActions();
    
    public abstract void didLongPressed(ChatMessageCell paramChatMessageCell);
    
    public abstract void didPressedBotButton(ChatMessageCell paramChatMessageCell, TLRPC.KeyboardButton paramKeyboardButton);
    
    public abstract void didPressedCancelSendButton(ChatMessageCell paramChatMessageCell);
    
    public abstract void didPressedChannelAvatar(ChatMessageCell paramChatMessageCell, TLRPC.Chat paramChat, int paramInt);
    
    public abstract void didPressedImage(ChatMessageCell paramChatMessageCell);
    
    public abstract void didPressedInstantButton(ChatMessageCell paramChatMessageCell, int paramInt);
    
    public abstract void didPressedOther(ChatMessageCell paramChatMessageCell);
    
    public abstract void didPressedReplyMessage(ChatMessageCell paramChatMessageCell, int paramInt);
    
    public abstract void didPressedShare(ChatMessageCell paramChatMessageCell);
    
    public abstract void didPressedUrl(MessageObject paramMessageObject, CharacterStyle paramCharacterStyle, boolean paramBoolean);
    
    public abstract void didPressedUserAvatar(ChatMessageCell paramChatMessageCell, TLRPC.User paramUser);
    
    public abstract void didPressedViaBot(ChatMessageCell paramChatMessageCell, String paramString);
    
    public abstract boolean isChatAdminCell(int paramInt);
    
    public abstract void needOpenWebView(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt1, int paramInt2);
    
    public abstract boolean needPlayMessage(MessageObject paramMessageObject);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ChatMessageCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */