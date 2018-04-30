package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.exoplayer2.ui.AspectRatioFrameLayout;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.LinearSmoothScrollerMiddle;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelAdminLogEventAction;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEvent;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangeStickerSet;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventsFilter;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channels_adminLogResults;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_getAdminLog;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.BotHelpCell;
import org.telegram.ui.Cells.BotHelpCell.BotHelpCellDelegate;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatActionCell.ChatActionCellDelegate;
import org.telegram.ui.Cells.ChatLoadingCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.ChatMessageCell.ChatMessageCellDelegate;
import org.telegram.ui.Cells.ChatUnreadCell;
import org.telegram.ui.Components.AdminLogFilterAlert;
import org.telegram.ui.Components.AdminLogFilterAlert.AdminLogFilterAlertDelegate;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

public class ChannelAdminLogActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private ArrayList<TLRPC.ChannelParticipant> admins;
  private Paint aspectPaint;
  private Path aspectPath;
  private AspectRatioFrameLayout aspectRatioFrameLayout;
  private ChatAvatarContainer avatarContainer;
  private FrameLayout bottomOverlayChat;
  private TextView bottomOverlayChatText;
  private ImageView bottomOverlayImage;
  private ChatActivityAdapter chatAdapter;
  private LinearLayoutManager chatLayoutManager;
  private RecyclerListView chatListView;
  private ArrayList<ChatMessageCell> chatMessageCellsCache = new ArrayList();
  private boolean checkTextureViewPosition;
  private SizeNotifierFrameLayout contentView;
  protected TLRPC.Chat currentChat;
  private TLRPC.TL_channelAdminLogEventsFilter currentFilter = null;
  private boolean currentFloatingDateOnScreen;
  private boolean currentFloatingTopIsNotMessage;
  private TextView emptyView;
  private FrameLayout emptyViewContainer;
  private boolean endReached;
  private AnimatorSet floatingDateAnimation;
  private ChatActionCell floatingDateView;
  private boolean loading;
  private int loadsCount;
  protected ArrayList<MessageObject> messages = new ArrayList();
  private HashMap<String, ArrayList<MessageObject>> messagesByDays = new HashMap();
  private LongSparseArray<MessageObject> messagesDict = new LongSparseArray();
  private int[] mid = { 2 };
  private int minDate;
  private long minEventId;
  private boolean openAnimationEnded;
  private boolean paused = true;
  private RadialProgressView progressBar;
  private FrameLayout progressView;
  private View progressView2;
  private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider()
  {
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      int j = ChannelAdminLogActivity.this.chatListView.getChildCount();
      paramAnonymousInt = 0;
      while (paramAnonymousInt < j)
      {
        Object localObject2 = null;
        View localView = ChannelAdminLogActivity.this.chatListView.getChildAt(paramAnonymousInt);
        Object localObject1;
        Object localObject3;
        MessageObject localMessageObject;
        int i;
        if ((localView instanceof ChatMessageCell))
        {
          localObject1 = localObject2;
          if (paramAnonymousMessageObject != null)
          {
            localObject3 = (ChatMessageCell)localView;
            localMessageObject = ((ChatMessageCell)localObject3).getMessageObject();
            localObject1 = localObject2;
            if (localMessageObject != null)
            {
              localObject1 = localObject2;
              if (localMessageObject.getId() == paramAnonymousMessageObject.getId()) {
                localObject1 = ((ChatMessageCell)localObject3).getPhotoImage();
              }
            }
          }
          if (localObject1 == null) {
            break label370;
          }
          paramAnonymousMessageObject = new int[2];
          localView.getLocationInWindow(paramAnonymousMessageObject);
          paramAnonymousFileLocation = new PhotoViewer.PlaceProviderObject();
          paramAnonymousFileLocation.viewX = paramAnonymousMessageObject[0];
          i = paramAnonymousMessageObject[1];
          if (Build.VERSION.SDK_INT < 21) {
            break label363;
          }
        }
        label363:
        for (paramAnonymousInt = 0;; paramAnonymousInt = AndroidUtilities.statusBarHeight)
        {
          paramAnonymousFileLocation.viewY = (i - paramAnonymousInt);
          paramAnonymousFileLocation.parentView = ChannelAdminLogActivity.this.chatListView;
          paramAnonymousFileLocation.imageReceiver = ((ImageReceiver)localObject1);
          paramAnonymousFileLocation.thumb = ((ImageReceiver)localObject1).getBitmapSafe();
          paramAnonymousFileLocation.radius = ((ImageReceiver)localObject1).getRoundRadius();
          paramAnonymousFileLocation.isEvent = true;
          return paramAnonymousFileLocation;
          localObject1 = localObject2;
          if (!(localView instanceof ChatActionCell)) {
            break;
          }
          localObject3 = (ChatActionCell)localView;
          localMessageObject = ((ChatActionCell)localObject3).getMessageObject();
          localObject1 = localObject2;
          if (localMessageObject == null) {
            break;
          }
          if (paramAnonymousMessageObject != null)
          {
            localObject1 = localObject2;
            if (localMessageObject.getId() != paramAnonymousMessageObject.getId()) {
              break;
            }
            localObject1 = ((ChatActionCell)localObject3).getPhotoImage();
            break;
          }
          localObject1 = localObject2;
          if (paramAnonymousFileLocation == null) {
            break;
          }
          localObject1 = localObject2;
          if (localMessageObject.photoThumbs == null) {
            break;
          }
          i = 0;
          for (;;)
          {
            localObject1 = localObject2;
            if (i >= localMessageObject.photoThumbs.size()) {
              break;
            }
            localObject1 = (TLRPC.PhotoSize)localMessageObject.photoThumbs.get(i);
            if ((((TLRPC.PhotoSize)localObject1).location.volume_id == paramAnonymousFileLocation.volume_id) && (((TLRPC.PhotoSize)localObject1).location.local_id == paramAnonymousFileLocation.local_id))
            {
              localObject1 = ((ChatActionCell)localObject3).getPhotoImage();
              break;
            }
            i += 1;
          }
        }
        label370:
        paramAnonymousInt += 1;
      }
      return null;
    }
  };
  private FrameLayout roundVideoContainer;
  private MessageObject scrollToMessage;
  private int scrollToOffsetOnRecreate = 0;
  private int scrollToPositionOnRecreate = -1;
  private boolean scrollingFloatingDate;
  private ImageView searchCalendarButton;
  private FrameLayout searchContainer;
  private SimpleTextView searchCountText;
  private ImageView searchDownButton;
  private ActionBarMenuItem searchItem;
  private String searchQuery = "";
  private ImageView searchUpButton;
  private boolean searchWas;
  private SparseArray<TLRPC.User> selectedAdmins;
  private MessageObject selectedObject;
  private TextureView videoTextureView;
  private boolean wasPaused = false;
  
  public ChannelAdminLogActivity(TLRPC.Chat paramChat)
  {
    this.currentChat = paramChat;
  }
  
  private void addCanBanUser(Bundle paramBundle, int paramInt)
  {
    if ((!this.currentChat.megagroup) || (this.admins == null) || (!ChatObject.canBlockUsers(this.currentChat))) {
      return;
    }
    int i = 0;
    for (;;)
    {
      if (i < this.admins.size())
      {
        TLRPC.ChannelParticipant localChannelParticipant = (TLRPC.ChannelParticipant)this.admins.get(i);
        if (localChannelParticipant.user_id != paramInt) {
          break label86;
        }
        if (!localChannelParticipant.can_edit) {
          break;
        }
      }
      paramBundle.putInt("ban_chat_id", this.currentChat.id);
      return;
      label86:
      i += 1;
    }
  }
  
  private void alertUserOpenError(MessageObject paramMessageObject)
  {
    if (getParentActivity() == null) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    if (paramMessageObject.type == 3) {
      localBuilder.setMessage(LocaleController.getString("NoPlayerInstalled", 2131493901));
    }
    for (;;)
    {
      showDialog(localBuilder.create());
      return;
      localBuilder.setMessage(LocaleController.formatString("NoHandleAppInstalled", 2131493889, new Object[] { paramMessageObject.getDocument().mime_type }));
    }
  }
  
  private void checkScrollForLoad(boolean paramBoolean)
  {
    if ((this.chatLayoutManager == null) || (this.paused)) {}
    label30:
    label90:
    label92:
    label95:
    for (;;)
    {
      return;
      int j = this.chatLayoutManager.findFirstVisibleItemPosition();
      if (j == -1)
      {
        i = 0;
        if (i <= 0) {
          break label90;
        }
        this.chatAdapter.getItemCount();
        if (!paramBoolean) {
          break label92;
        }
      }
      for (int i = 25;; i = 5)
      {
        if ((j > i) || (this.loading) || (this.endReached)) {
          break label95;
        }
        loadMessages(false);
        return;
        i = Math.abs(this.chatLayoutManager.findLastVisibleItemPosition() - j) + 1;
        break label30;
        break;
      }
    }
  }
  
  private void createMenu(View paramView)
  {
    Object localObject = null;
    if ((paramView instanceof ChatMessageCell))
    {
      localObject = ((ChatMessageCell)paramView).getMessageObject();
      break label21;
      label17:
      if (localObject != null) {
        break label40;
      }
    }
    for (;;)
    {
      label21:
      return;
      if (!(paramView instanceof ChatActionCell)) {
        break label17;
      }
      localObject = ((ChatActionCell)paramView).getMessageObject();
      break label17;
      label40:
      int i = getMessageType((MessageObject)localObject);
      this.selectedObject = ((MessageObject)localObject);
      if (getParentActivity() == null) {
        break;
      }
      AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
      ArrayList localArrayList1 = new ArrayList();
      final ArrayList localArrayList2 = new ArrayList();
      if ((this.selectedObject.type == 0) || (this.selectedObject.caption != null))
      {
        localArrayList1.add(LocaleController.getString("Copy", 2131493303));
        localArrayList2.add(Integer.valueOf(3));
      }
      if (i == 1)
      {
        if ((this.selectedObject.currentEvent != null) && ((this.selectedObject.currentEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeStickerSet)))
        {
          localObject = this.selectedObject.currentEvent.action.new_stickerset;
          if (localObject != null)
          {
            paramView = (View)localObject;
            if (!(localObject instanceof TLRPC.TL_inputStickerSetEmpty)) {}
          }
          else
          {
            paramView = this.selectedObject.currentEvent.action.prev_stickerset;
          }
          if (paramView != null) {
            showDialog(new StickersAlert(getParentActivity(), this, paramView, null, null));
          }
        }
      }
      else
      {
        if (i != 3) {
          break label366;
        }
        if (((this.selectedObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (MessageObject.isNewGifDocument(this.selectedObject.messageOwner.media.webpage.document)))
        {
          localArrayList1.add(LocaleController.getString("SaveToGIFs", 2131494289));
          localArrayList2.add(Integer.valueOf(11));
        }
      }
      while (!localArrayList2.isEmpty())
      {
        localBuilder.setItems((CharSequence[])localArrayList1.toArray(new CharSequence[localArrayList1.size()]), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            if ((ChannelAdminLogActivity.this.selectedObject == null) || (paramAnonymousInt < 0) || (paramAnonymousInt >= localArrayList2.size())) {
              return;
            }
            ChannelAdminLogActivity.this.processSelectedOption(((Integer)localArrayList2.get(paramAnonymousInt)).intValue());
          }
        });
        localBuilder.setTitle(LocaleController.getString("Message", 2131493817));
        showDialog(localBuilder.create());
        return;
        label366:
        if (i == 4)
        {
          if (this.selectedObject.isVideo())
          {
            localArrayList1.add(LocaleController.getString("SaveToGallery", 2131494290));
            localArrayList2.add(Integer.valueOf(4));
            localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
            localArrayList2.add(Integer.valueOf(6));
          }
          else if (this.selectedObject.isMusic())
          {
            localArrayList1.add(LocaleController.getString("SaveToMusic", 2131494292));
            localArrayList2.add(Integer.valueOf(10));
            localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
            localArrayList2.add(Integer.valueOf(6));
          }
          else if (this.selectedObject.getDocument() != null)
          {
            if (MessageObject.isNewGifDocument(this.selectedObject.getDocument()))
            {
              localArrayList1.add(LocaleController.getString("SaveToGIFs", 2131494289));
              localArrayList2.add(Integer.valueOf(11));
            }
            localArrayList1.add(LocaleController.getString("SaveToDownloads", 2131494288));
            localArrayList2.add(Integer.valueOf(10));
            localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
            localArrayList2.add(Integer.valueOf(6));
          }
          else
          {
            localArrayList1.add(LocaleController.getString("SaveToGallery", 2131494290));
            localArrayList2.add(Integer.valueOf(4));
          }
        }
        else if (i == 5)
        {
          localArrayList1.add(LocaleController.getString("ApplyLocalizationFile", 2131492983));
          localArrayList2.add(Integer.valueOf(5));
          localArrayList1.add(LocaleController.getString("SaveToDownloads", 2131494288));
          localArrayList2.add(Integer.valueOf(10));
          localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
          localArrayList2.add(Integer.valueOf(6));
        }
        else if (i == 10)
        {
          localArrayList1.add(LocaleController.getString("ApplyThemeFile", 2131492985));
          localArrayList2.add(Integer.valueOf(5));
          localArrayList1.add(LocaleController.getString("SaveToDownloads", 2131494288));
          localArrayList2.add(Integer.valueOf(10));
          localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
          localArrayList2.add(Integer.valueOf(6));
        }
        else if (i == 6)
        {
          localArrayList1.add(LocaleController.getString("SaveToGallery", 2131494290));
          localArrayList2.add(Integer.valueOf(7));
          localArrayList1.add(LocaleController.getString("SaveToDownloads", 2131494288));
          localArrayList2.add(Integer.valueOf(10));
          localArrayList1.add(LocaleController.getString("ShareFile", 2131494383));
          localArrayList2.add(Integer.valueOf(6));
        }
        else
        {
          if (i == 7)
          {
            if (this.selectedObject.isMask()) {
              localArrayList1.add(LocaleController.getString("AddToMasks", 2131492942));
            }
            for (;;)
            {
              localArrayList2.add(Integer.valueOf(9));
              break;
              localArrayList1.add(LocaleController.getString("AddToStickers", 2131492943));
            }
          }
          if (i == 8)
          {
            paramView = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.selectedObject.messageOwner.media.user_id));
            if ((paramView != null) && (paramView.id != UserConfig.getInstance(this.currentAccount).getClientUserId()) && (ContactsController.getInstance(this.currentAccount).contactsDict.get(Integer.valueOf(paramView.id)) == null))
            {
              localArrayList1.add(LocaleController.getString("AddContactTitle", 2131492928));
              localArrayList2.add(Integer.valueOf(15));
            }
            if ((this.selectedObject.messageOwner.media.phone_number != null) || (this.selectedObject.messageOwner.media.phone_number.length() != 0))
            {
              localArrayList1.add(LocaleController.getString("Copy", 2131493303));
              localArrayList2.add(Integer.valueOf(16));
              localArrayList1.add(LocaleController.getString("Call", 2131493105));
              localArrayList2.add(Integer.valueOf(17));
            }
          }
        }
      }
    }
  }
  
  private TextureView createTextureView(boolean paramBoolean)
  {
    if (this.parentLayout == null) {
      return null;
    }
    if (this.roundVideoContainer == null)
    {
      if (Build.VERSION.SDK_INT < 21) {
        break label221;
      }
      this.roundVideoContainer = new FrameLayout(getParentActivity())
      {
        public void setTranslationY(float paramAnonymousFloat)
        {
          super.setTranslationY(paramAnonymousFloat);
          ChannelAdminLogActivity.this.contentView.invalidate();
        }
      };
      this.roundVideoContainer.setOutlineProvider(new ViewOutlineProvider()
      {
        @TargetApi(21)
        public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
        {
          paramAnonymousOutline.setOval(0, 0, AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize);
        }
      });
      this.roundVideoContainer.setClipToOutline(true);
    }
    for (;;)
    {
      this.roundVideoContainer.setWillNotDraw(false);
      this.roundVideoContainer.setVisibility(4);
      this.aspectRatioFrameLayout = new AspectRatioFrameLayout(getParentActivity());
      this.aspectRatioFrameLayout.setBackgroundColor(0);
      if (paramBoolean) {
        this.roundVideoContainer.addView(this.aspectRatioFrameLayout, LayoutHelper.createFrame(-1, -1.0F));
      }
      this.videoTextureView = new TextureView(getParentActivity());
      this.videoTextureView.setOpaque(false);
      this.aspectRatioFrameLayout.addView(this.videoTextureView, LayoutHelper.createFrame(-1, -1.0F));
      if (this.roundVideoContainer.getParent() == null) {
        this.contentView.addView(this.roundVideoContainer, 1, new FrameLayout.LayoutParams(AndroidUtilities.roundMessageSize, AndroidUtilities.roundMessageSize));
      }
      this.roundVideoContainer.setVisibility(4);
      this.aspectRatioFrameLayout.setDrawingReady(false);
      return this.videoTextureView;
      label221:
      this.roundVideoContainer = new FrameLayout(getParentActivity())
      {
        protected void dispatchDraw(Canvas paramAnonymousCanvas)
        {
          super.dispatchDraw(paramAnonymousCanvas);
          paramAnonymousCanvas.drawPath(ChannelAdminLogActivity.this.aspectPath, ChannelAdminLogActivity.this.aspectPaint);
        }
        
        protected void onSizeChanged(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onSizeChanged(paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          ChannelAdminLogActivity.this.aspectPath.reset();
          ChannelAdminLogActivity.this.aspectPath.addCircle(paramAnonymousInt1 / 2, paramAnonymousInt2 / 2, paramAnonymousInt1 / 2, Path.Direction.CW);
          ChannelAdminLogActivity.this.aspectPath.toggleInverseFillType();
        }
        
        public void setTranslationY(float paramAnonymousFloat)
        {
          super.setTranslationY(paramAnonymousFloat);
          ChannelAdminLogActivity.this.contentView.invalidate();
        }
        
        public void setVisibility(int paramAnonymousInt)
        {
          super.setVisibility(paramAnonymousInt);
          if (paramAnonymousInt == 0) {
            setLayerType(2, null);
          }
        }
      };
      this.aspectPath = new Path();
      this.aspectPaint = new Paint(1);
      this.aspectPaint.setColor(-16777216);
      this.aspectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
  }
  
  private void destroyTextureView()
  {
    if ((this.roundVideoContainer == null) || (this.roundVideoContainer.getParent() == null)) {}
    do
    {
      return;
      this.contentView.removeView(this.roundVideoContainer);
      this.aspectRatioFrameLayout.setDrawingReady(false);
      this.roundVideoContainer.setVisibility(4);
    } while (Build.VERSION.SDK_INT >= 21);
    this.roundVideoContainer.setLayerType(0, null);
  }
  
  private void fixLayout()
  {
    if (this.avatarContainer != null) {
      this.avatarContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          if (ChannelAdminLogActivity.this.avatarContainer != null) {
            ChannelAdminLogActivity.this.avatarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
          }
          return true;
        }
      });
    }
  }
  
  private String getMessageContent(MessageObject paramMessageObject, int paramInt, boolean paramBoolean)
  {
    String str2 = "";
    String str1 = str2;
    Object localObject;
    if (paramBoolean)
    {
      str1 = str2;
      if (paramInt != paramMessageObject.messageOwner.from_id)
      {
        if (paramMessageObject.messageOwner.from_id <= 0) {
          break label145;
        }
        localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramMessageObject.messageOwner.from_id));
        str1 = str2;
        if (localObject != null) {
          str1 = ContactsController.formatName(((TLRPC.User)localObject).first_name, ((TLRPC.User)localObject).last_name) + ":\n";
        }
      }
    }
    while ((paramMessageObject.type == 0) && (paramMessageObject.messageOwner.message != null))
    {
      return str1 + paramMessageObject.messageOwner.message;
      label145:
      str1 = str2;
      if (paramMessageObject.messageOwner.from_id < 0)
      {
        localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-paramMessageObject.messageOwner.from_id));
        str1 = str2;
        if (localObject != null) {
          str1 = ((TLRPC.Chat)localObject).title + ":\n";
        }
      }
    }
    if ((paramMessageObject.messageOwner.media != null) && (paramMessageObject.messageOwner.message != null)) {
      return str1 + paramMessageObject.messageOwner.message;
    }
    return str1 + paramMessageObject.messageText;
  }
  
  private int getMessageType(MessageObject paramMessageObject)
  {
    if (paramMessageObject == null) {}
    do
    {
      do
      {
        return -1;
      } while (paramMessageObject.type == 6);
      if ((paramMessageObject.type != 10) && (paramMessageObject.type != 11) && (paramMessageObject.type != 16)) {
        break;
      }
    } while (paramMessageObject.getId() == 0);
    return 1;
    if (paramMessageObject.isVoice()) {
      return 2;
    }
    if (paramMessageObject.isSticker())
    {
      paramMessageObject = paramMessageObject.getInputStickerSet();
      if ((paramMessageObject instanceof TLRPC.TL_inputStickerSetID))
      {
        if (!DataQuery.getInstance(this.currentAccount).isStickerPackInstalled(paramMessageObject.id)) {
          return 7;
        }
      }
      else if (((paramMessageObject instanceof TLRPC.TL_inputStickerSetShortName)) && (!DataQuery.getInstance(this.currentAccount).isStickerPackInstalled(paramMessageObject.short_name))) {
        return 7;
      }
    }
    else if (((!paramMessageObject.isRoundVideo()) || ((paramMessageObject.isRoundVideo()) && (BuildVars.DEBUG_VERSION))) && (((paramMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) || (paramMessageObject.getDocument() != null) || (paramMessageObject.isMusic()) || (paramMessageObject.isVideo())))
    {
      int j = 0;
      int i = j;
      if (paramMessageObject.messageOwner.attachPath != null)
      {
        i = j;
        if (paramMessageObject.messageOwner.attachPath.length() != 0)
        {
          i = j;
          if (new File(paramMessageObject.messageOwner.attachPath).exists()) {
            i = 1;
          }
        }
      }
      j = i;
      if (i == 0)
      {
        j = i;
        if (FileLoader.getPathToMessage(paramMessageObject.messageOwner).exists()) {
          j = 1;
        }
      }
      if (j != 0)
      {
        if (paramMessageObject.getDocument() != null)
        {
          String str = paramMessageObject.getDocument().mime_type;
          if (str != null)
          {
            if (paramMessageObject.getDocumentName().toLowerCase().endsWith("attheme")) {
              return 10;
            }
            if (str.endsWith("/xml")) {
              return 5;
            }
            if ((str.endsWith("/png")) || (str.endsWith("/jpg")) || (str.endsWith("/jpeg"))) {
              return 6;
            }
          }
        }
        return 4;
      }
    }
    else
    {
      if (paramMessageObject.type == 12) {
        return 8;
      }
      if (paramMessageObject.isMediaEmpty()) {
        return 3;
      }
    }
    return 2;
  }
  
  private void hideFloatingDateView(boolean paramBoolean)
  {
    if ((this.floatingDateView.getTag() != null) && (!this.currentFloatingDateOnScreen) && ((!this.scrollingFloatingDate) || (this.currentFloatingTopIsNotMessage)))
    {
      this.floatingDateView.setTag(null);
      if (paramBoolean)
      {
        this.floatingDateAnimation = new AnimatorSet();
        this.floatingDateAnimation.setDuration(150L);
        this.floatingDateAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.floatingDateView, "alpha", new float[] { 0.0F }) });
        this.floatingDateAnimation.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if (paramAnonymousAnimator.equals(ChannelAdminLogActivity.this.floatingDateAnimation)) {
              ChannelAdminLogActivity.access$4502(ChannelAdminLogActivity.this, null);
            }
          }
        });
        this.floatingDateAnimation.setStartDelay(500L);
        this.floatingDateAnimation.start();
      }
    }
    else
    {
      return;
    }
    if (this.floatingDateAnimation != null)
    {
      this.floatingDateAnimation.cancel();
      this.floatingDateAnimation = null;
    }
    this.floatingDateView.setAlpha(0.0F);
  }
  
  private void loadAdmins()
  {
    TLRPC.TL_channels_getParticipants localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
    localTL_channels_getParticipants.channel = MessagesController.getInputChannel(this.currentChat);
    localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsAdmins();
    localTL_channels_getParticipants.offset = 0;
    localTL_channels_getParticipants.limit = 200;
    int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (paramAnonymousTL_error == null)
            {
              TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
              MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).putUsers(localTL_channels_channelParticipants.users, false);
              ChannelAdminLogActivity.access$4902(ChannelAdminLogActivity.this, localTL_channels_channelParticipants.participants);
              if ((ChannelAdminLogActivity.this.visibleDialog instanceof AdminLogFilterAlert)) {
                ((AdminLogFilterAlert)ChannelAdminLogActivity.this.visibleDialog).setCurrentAdmins(ChannelAdminLogActivity.this.admins);
              }
            }
          }
        });
      }
    });
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, this.classGuid);
  }
  
  private void loadMessages(boolean paramBoolean)
  {
    if (this.loading) {}
    do
    {
      return;
      if (paramBoolean)
      {
        this.minEventId = Long.MAX_VALUE;
        if (this.progressView != null)
        {
          this.progressView.setVisibility(0);
          this.emptyViewContainer.setVisibility(4);
          this.chatListView.setEmptyView(null);
        }
        this.messagesDict.clear();
        this.messages.clear();
        this.messagesByDays.clear();
      }
      this.loading = true;
      TLRPC.TL_channels_getAdminLog localTL_channels_getAdminLog = new TLRPC.TL_channels_getAdminLog();
      localTL_channels_getAdminLog.channel = MessagesController.getInputChannel(this.currentChat);
      localTL_channels_getAdminLog.q = this.searchQuery;
      localTL_channels_getAdminLog.limit = 50;
      if ((!paramBoolean) && (!this.messages.isEmpty())) {}
      for (localTL_channels_getAdminLog.max_id = this.minEventId;; localTL_channels_getAdminLog.max_id = 0L)
      {
        localTL_channels_getAdminLog.min_id = 0L;
        if (this.currentFilter != null)
        {
          localTL_channels_getAdminLog.flags |= 0x1;
          localTL_channels_getAdminLog.events_filter = this.currentFilter;
        }
        if (this.selectedAdmins == null) {
          break;
        }
        localTL_channels_getAdminLog.flags |= 0x2;
        int i = 0;
        while (i < this.selectedAdmins.size())
        {
          localTL_channels_getAdminLog.admins.add(MessagesController.getInstance(this.currentAccount).getInputUser((TLRPC.User)this.selectedAdmins.valueAt(i)));
          i += 1;
        }
      }
      updateEmptyPlaceholder();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getAdminLog, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).putUsers(paramAnonymousTLObject.users, false);
                MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).putChats(paramAnonymousTLObject.chats, false);
                int j = 0;
                int m = ChannelAdminLogActivity.this.messages.size();
                int i = 0;
                Object localObject;
                int k;
                if (i < paramAnonymousTLObject.events.size())
                {
                  localObject = (TLRPC.TL_channelAdminLogEvent)paramAnonymousTLObject.events.get(i);
                  if (ChannelAdminLogActivity.this.messagesDict.indexOfKey(((TLRPC.TL_channelAdminLogEvent)localObject).id) >= 0) {}
                  for (;;)
                  {
                    i += 1;
                    break;
                    ChannelAdminLogActivity.access$402(ChannelAdminLogActivity.this, Math.min(ChannelAdminLogActivity.this.minEventId, ((TLRPC.TL_channelAdminLogEvent)localObject).id));
                    k = 1;
                    MessageObject localMessageObject = new MessageObject(ChannelAdminLogActivity.this.currentAccount, (TLRPC.TL_channelAdminLogEvent)localObject, ChannelAdminLogActivity.this.messages, ChannelAdminLogActivity.this.messagesByDays, ChannelAdminLogActivity.this.currentChat, ChannelAdminLogActivity.this.mid);
                    j = k;
                    if (localMessageObject.contentType >= 0)
                    {
                      ChannelAdminLogActivity.this.messagesDict.put(((TLRPC.TL_channelAdminLogEvent)localObject).id, localMessageObject);
                      j = k;
                    }
                  }
                }
                m = ChannelAdminLogActivity.this.messages.size() - m;
                ChannelAdminLogActivity.access$802(ChannelAdminLogActivity.this, false);
                if (j == 0) {
                  ChannelAdminLogActivity.access$902(ChannelAdminLogActivity.this, true);
                }
                ChannelAdminLogActivity.this.progressView.setVisibility(4);
                ChannelAdminLogActivity.this.chatListView.setEmptyView(ChannelAdminLogActivity.this.emptyViewContainer);
                if (m != 0)
                {
                  j = 0;
                  if (ChannelAdminLogActivity.this.endReached)
                  {
                    j = 1;
                    ChannelAdminLogActivity.this.chatAdapter.notifyItemRangeChanged(0, 2);
                  }
                  n = ChannelAdminLogActivity.this.chatLayoutManager.findLastVisibleItemPosition();
                  localObject = ChannelAdminLogActivity.this.chatLayoutManager.findViewByPosition(n);
                  if (localObject == null)
                  {
                    i = 0;
                    i1 = ChannelAdminLogActivity.this.chatListView.getPaddingTop();
                    if (j == 0) {
                      break label544;
                    }
                    k = 1;
                    if (m - k > 0)
                    {
                      if (j == 0) {
                        break label549;
                      }
                      k = 0;
                      i2 = k + 1;
                      ChannelAdminLogActivity.this.chatAdapter.notifyItemChanged(i2);
                      localObject = ChannelAdminLogActivity.this.chatAdapter;
                      if (j == 0) {
                        break label554;
                      }
                      k = 1;
                      ((ChannelAdminLogActivity.ChatActivityAdapter)localObject).notifyItemRangeInserted(i2, m - k);
                    }
                    if (n != -1)
                    {
                      localObject = ChannelAdminLogActivity.this.chatLayoutManager;
                      if (j == 0) {
                        break label559;
                      }
                      j = 1;
                      ((LinearLayoutManager)localObject).scrollToPositionWithOffset(n + m - j, i - i1);
                    }
                  }
                }
                label544:
                label549:
                label554:
                label559:
                while (!ChannelAdminLogActivity.this.endReached) {
                  for (;;)
                  {
                    int n;
                    int i1;
                    int i2;
                    return;
                    i = ((View)localObject).getTop();
                    continue;
                    k = 0;
                    continue;
                    k = 1;
                    continue;
                    k = 0;
                    continue;
                    j = 0;
                  }
                }
                ChannelAdminLogActivity.this.chatAdapter.notifyItemRemoved(0);
              }
            });
          }
        }
      });
    } while ((!paramBoolean) || (this.chatAdapter == null));
    this.chatAdapter.notifyDataSetChanged();
  }
  
  private void moveScrollToLastMessage()
  {
    if ((this.chatListView != null) && (!this.messages.isEmpty())) {
      this.chatLayoutManager.scrollToPositionWithOffset(this.messages.size() - 1, -100000 - this.chatListView.getPaddingTop());
    }
  }
  
  private void processSelectedOption(int paramInt)
  {
    if (this.selectedObject == null) {
      return;
    }
    switch (paramInt)
    {
    }
    for (;;)
    {
      this.selectedObject = null;
      return;
      AndroidUtilities.addToClipboard(getMessageContent(this.selectedObject, 0, true));
      continue;
      Object localObject2 = this.selectedObject.messageOwner.attachPath;
      Object localObject1 = localObject2;
      if (localObject2 != null)
      {
        localObject1 = localObject2;
        if (((String)localObject2).length() > 0)
        {
          localObject1 = localObject2;
          if (!new File((String)localObject2).exists()) {
            localObject1 = null;
          }
        }
      }
      if (localObject1 != null)
      {
        localObject2 = localObject1;
        if (((String)localObject1).length() != 0) {}
      }
      else
      {
        localObject2 = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
      }
      if ((this.selectedObject.type == 3) || (this.selectedObject.type == 1))
      {
        if ((Build.VERSION.SDK_INT >= 23) && (getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
        {
          getParentActivity().requestPermissions(new String[] { "android.permission.WRITE_EXTERNAL_STORAGE" }, 4);
          this.selectedObject = null;
          return;
        }
        localObject1 = getParentActivity();
        if (this.selectedObject.type == 3) {}
        for (paramInt = 1;; paramInt = 0)
        {
          MediaController.saveFile((String)localObject2, (Context)localObject1, paramInt, null, null);
          break;
        }
        localObject2 = null;
        localObject1 = localObject2;
        File localFile;
        if (this.selectedObject.messageOwner.attachPath != null)
        {
          localObject1 = localObject2;
          if (this.selectedObject.messageOwner.attachPath.length() != 0)
          {
            localFile = new File(this.selectedObject.messageOwner.attachPath);
            localObject1 = localObject2;
            if (localFile.exists()) {
              localObject1 = localFile;
            }
          }
        }
        localObject2 = localObject1;
        if (localObject1 == null)
        {
          localFile = FileLoader.getPathToMessage(this.selectedObject.messageOwner);
          localObject2 = localObject1;
          if (localFile.exists()) {
            localObject2 = localFile;
          }
        }
        if (localObject2 != null) {
          if (((File)localObject2).getName().toLowerCase().endsWith("attheme"))
          {
            if (this.chatLayoutManager != null)
            {
              if (this.chatLayoutManager.findLastVisibleItemPosition() >= this.chatLayoutManager.getItemCount() - 1) {
                break label505;
              }
              this.scrollToPositionOnRecreate = this.chatLayoutManager.findFirstVisibleItemPosition();
              localObject1 = (RecyclerListView.Holder)this.chatListView.findViewHolderForAdapterPosition(this.scrollToPositionOnRecreate);
              if (localObject1 == null) {
                break label497;
              }
              this.scrollToOffsetOnRecreate = ((RecyclerListView.Holder)localObject1).itemView.getTop();
            }
            for (;;)
            {
              localObject1 = Theme.applyThemeFile((File)localObject2, this.selectedObject.getDocumentName(), true);
              if (localObject1 == null) {
                break label513;
              }
              presentFragment(new ThemePreviewActivity((File)localObject2, (Theme.ThemeInfo)localObject1));
              break;
              label497:
              this.scrollToPositionOnRecreate = -1;
              continue;
              label505:
              this.scrollToPositionOnRecreate = -1;
            }
            label513:
            this.scrollToPositionOnRecreate = -1;
            if (getParentActivity() == null)
            {
              this.selectedObject = null;
              return;
            }
            localObject1 = new AlertDialog.Builder(getParentActivity());
            ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("IncorrectTheme", 2131493674));
            ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
            showDialog(((AlertDialog.Builder)localObject1).create());
          }
          else if (LocaleController.getInstance().applyLanguageFile((File)localObject2, this.currentAccount))
          {
            presentFragment(new LanguageSelectActivity());
          }
          else
          {
            if (getParentActivity() == null)
            {
              this.selectedObject = null;
              return;
            }
            localObject1 = new AlertDialog.Builder(getParentActivity());
            ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("IncorrectLocalization", 2131493673));
            ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
            showDialog(((AlertDialog.Builder)localObject1).create());
            continue;
            localObject2 = this.selectedObject.messageOwner.attachPath;
            localObject1 = localObject2;
            if (localObject2 != null)
            {
              localObject1 = localObject2;
              if (((String)localObject2).length() > 0)
              {
                localObject1 = localObject2;
                if (!new File((String)localObject2).exists()) {
                  localObject1 = null;
                }
              }
            }
            if (localObject1 != null)
            {
              localObject2 = localObject1;
              if (((String)localObject1).length() != 0) {}
            }
            else
            {
              localObject2 = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
            }
            localObject1 = new Intent("android.intent.action.SEND");
            ((Intent)localObject1).setType(this.selectedObject.getDocument().mime_type);
            if (Build.VERSION.SDK_INT >= 24) {}
            for (;;)
            {
              try
              {
                ((Intent)localObject1).putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(getParentActivity(), "org.telegram.messenger.provider", new File((String)localObject2)));
                ((Intent)localObject1).setFlags(1);
                getParentActivity().startActivityForResult(Intent.createChooser((Intent)localObject1, LocaleController.getString("ShareFile", 2131494383)), 500);
              }
              catch (Exception localException2)
              {
                ((Intent)localObject1).putExtra("android.intent.extra.STREAM", Uri.fromFile(new File((String)localObject2)));
                continue;
              }
              ((Intent)localObject1).putExtra("android.intent.extra.STREAM", Uri.fromFile(new File((String)localObject2)));
            }
            localObject2 = this.selectedObject.messageOwner.attachPath;
            localObject1 = localObject2;
            if (localObject2 != null)
            {
              localObject1 = localObject2;
              if (((String)localObject2).length() > 0)
              {
                localObject1 = localObject2;
                if (!new File((String)localObject2).exists()) {
                  localObject1 = null;
                }
              }
            }
            if (localObject1 != null)
            {
              localObject2 = localObject1;
              if (((String)localObject1).length() != 0) {}
            }
            else
            {
              localObject2 = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
            }
            if ((Build.VERSION.SDK_INT >= 23) && (getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
            {
              getParentActivity().requestPermissions(new String[] { "android.permission.WRITE_EXTERNAL_STORAGE" }, 4);
              this.selectedObject = null;
              return;
            }
            MediaController.saveFile((String)localObject2, getParentActivity(), 0, null, null);
            continue;
            showDialog(new StickersAlert(getParentActivity(), this, this.selectedObject.getInputStickerSet(), null, null));
            continue;
            if ((Build.VERSION.SDK_INT >= 23) && (getParentActivity().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
            {
              getParentActivity().requestPermissions(new String[] { "android.permission.WRITE_EXTERNAL_STORAGE" }, 4);
              this.selectedObject = null;
              return;
            }
            localObject1 = FileLoader.getDocumentFileName(this.selectedObject.getDocument());
            localObject2 = localObject1;
            if (TextUtils.isEmpty((CharSequence)localObject1)) {
              localObject2 = this.selectedObject.getFileName();
            }
            Object localObject3 = this.selectedObject.messageOwner.attachPath;
            localObject1 = localObject3;
            if (localObject3 != null)
            {
              localObject1 = localObject3;
              if (((String)localObject3).length() > 0)
              {
                localObject1 = localObject3;
                if (!new File((String)localObject3).exists()) {
                  localObject1 = null;
                }
              }
            }
            if (localObject1 != null)
            {
              localObject3 = localObject1;
              if (((String)localObject1).length() != 0) {}
            }
            else
            {
              localObject3 = FileLoader.getPathToMessage(this.selectedObject.messageOwner).toString();
            }
            Activity localActivity = getParentActivity();
            if (this.selectedObject.isMusic())
            {
              paramInt = 3;
              label1246:
              if (this.selectedObject.getDocument() == null) {
                break label1285;
              }
            }
            label1285:
            for (localObject1 = this.selectedObject.getDocument().mime_type;; localObject1 = "")
            {
              MediaController.saveFile((String)localObject3, localActivity, paramInt, (String)localObject2, (String)localObject1);
              break;
              paramInt = 2;
              break label1246;
            }
            localObject1 = this.selectedObject.getDocument();
            MessagesController.getInstance(this.currentAccount).saveGif((TLRPC.Document)localObject1);
            continue;
            localObject1 = new Bundle();
            ((Bundle)localObject1).putInt("user_id", this.selectedObject.messageOwner.media.user_id);
            ((Bundle)localObject1).putString("phone", this.selectedObject.messageOwner.media.phone_number);
            ((Bundle)localObject1).putBoolean("addContact", true);
            presentFragment(new ContactAddActivity((Bundle)localObject1));
            continue;
            AndroidUtilities.addToClipboard(this.selectedObject.messageOwner.media.phone_number);
            continue;
            try
            {
              localObject1 = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + this.selectedObject.messageOwner.media.phone_number));
              ((Intent)localObject1).addFlags(268435456);
              getParentActivity().startActivityForResult((Intent)localObject1, 500);
            }
            catch (Exception localException1)
            {
              FileLog.e(localException1);
            }
          }
        }
      }
    }
  }
  
  private void removeMessageObject(MessageObject paramMessageObject)
  {
    int i = this.messages.indexOf(paramMessageObject);
    if (i == -1) {}
    do
    {
      return;
      this.messages.remove(i);
    } while (this.chatAdapter == null);
    this.chatAdapter.notifyItemRemoved(this.chatAdapter.messagesStartRow + this.messages.size() - i - 1);
  }
  
  private void updateBottomOverlay() {}
  
  private void updateEmptyPlaceholder()
  {
    if (this.emptyView == null) {
      return;
    }
    if (!TextUtils.isEmpty(this.searchQuery))
    {
      this.emptyView.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(5.0F));
      this.emptyView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("EventLogEmptyTextSearch", 2131493478, new Object[] { this.searchQuery })));
      return;
    }
    if ((this.selectedAdmins != null) || (this.currentFilter != null))
    {
      this.emptyView.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(5.0F));
      this.emptyView.setText(AndroidUtilities.replaceTags(LocaleController.getString("EventLogEmptySearch", 2131493477)));
      return;
    }
    this.emptyView.setPadding(AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F));
    if (this.currentChat.megagroup)
    {
      this.emptyView.setText(AndroidUtilities.replaceTags(LocaleController.getString("EventLogEmpty", 2131493475)));
      return;
    }
    this.emptyView.setText(AndroidUtilities.replaceTags(LocaleController.getString("EventLogEmptyChannel", 2131493476)));
  }
  
  private void updateMessagesVisisblePart()
  {
    if (this.chatListView == null) {
      return;
    }
    int i3 = this.chatListView.getChildCount();
    int i4 = this.chatListView.getMeasuredHeight();
    int i = Integer.MAX_VALUE;
    int k = Integer.MAX_VALUE;
    Object localObject5 = null;
    Object localObject3 = null;
    Object localObject1 = null;
    int j = 0;
    int m = 0;
    Object localObject2;
    if (m < i3)
    {
      localObject2 = this.chatListView.getChildAt(m);
      int n = j;
      Object localObject4;
      int i5;
      label105:
      int i2;
      int i1;
      Object localObject6;
      if ((localObject2 instanceof ChatMessageCell))
      {
        localObject4 = (ChatMessageCell)localObject2;
        i5 = ((ChatMessageCell)localObject4).getTop();
        ((ChatMessageCell)localObject4).getBottom();
        if (i5 < 0) {
          break label306;
        }
        n = 0;
        i2 = ((ChatMessageCell)localObject4).getMeasuredHeight();
        i1 = i2;
        if (i2 > i4) {
          i1 = n + i4;
        }
        ((ChatMessageCell)localObject4).setVisiblePart(n, i1 - n);
        localObject6 = ((ChatMessageCell)localObject4).getMessageObject();
        n = j;
        if (this.roundVideoContainer != null)
        {
          n = j;
          if (((MessageObject)localObject6).isRoundVideo())
          {
            n = j;
            if (MediaController.getInstance().isPlayingMessage((MessageObject)localObject6))
            {
              localObject4 = ((ChatMessageCell)localObject4).getPhotoImage();
              this.roundVideoContainer.setTranslationX(((ImageReceiver)localObject4).getImageX());
              this.roundVideoContainer.setTranslationY(this.fragmentView.getPaddingTop() + i5 + ((ImageReceiver)localObject4).getImageY());
              this.fragmentView.invalidate();
              this.roundVideoContainer.invalidate();
              n = 1;
            }
          }
        }
      }
      Object localObject7;
      if (((View)localObject2).getBottom() <= this.chatListView.getPaddingTop())
      {
        i1 = i;
        i = k;
        localObject7 = localObject1;
        localObject6 = localObject5;
        localObject1 = localObject3;
      }
      for (;;)
      {
        m += 1;
        j = n;
        localObject3 = localObject1;
        localObject5 = localObject6;
        localObject1 = localObject7;
        k = i;
        i = i1;
        break;
        label306:
        n = -i5;
        break label105;
        i2 = ((View)localObject2).getBottom();
        localObject4 = localObject3;
        localObject3 = localObject1;
        j = i;
        if (i2 < i)
        {
          j = i2;
          if (((localObject2 instanceof ChatMessageCell)) || ((localObject2 instanceof ChatActionCell))) {
            localObject1 = localObject2;
          }
          localObject4 = localObject2;
          localObject3 = localObject1;
        }
        localObject1 = localObject4;
        localObject6 = localObject5;
        localObject7 = localObject3;
        i = k;
        i1 = j;
        if ((localObject2 instanceof ChatActionCell))
        {
          localObject1 = localObject4;
          localObject6 = localObject5;
          localObject7 = localObject3;
          i = k;
          i1 = j;
          if (((ChatActionCell)localObject2).getMessageObject().isDateObject)
          {
            if (((View)localObject2).getAlpha() != 1.0F) {
              ((View)localObject2).setAlpha(1.0F);
            }
            localObject1 = localObject4;
            localObject6 = localObject5;
            localObject7 = localObject3;
            i = k;
            i1 = j;
            if (i2 < k)
            {
              i = i2;
              localObject1 = localObject4;
              localObject6 = localObject2;
              localObject7 = localObject3;
              i1 = j;
            }
          }
        }
      }
    }
    if (this.roundVideoContainer != null)
    {
      if (j != 0) {
        break label738;
      }
      this.roundVideoContainer.setTranslationY(-AndroidUtilities.roundMessageSize - 100);
      this.fragmentView.invalidate();
      localObject2 = MediaController.getInstance().getPlayingMessageObject();
      if ((localObject2 != null) && (((MessageObject)localObject2).isRoundVideo()) && (this.checkTextureViewPosition)) {
        MediaController.getInstance().setCurrentRoundVisible(false);
      }
    }
    label575:
    boolean bool;
    if (localObject1 != null)
    {
      if ((localObject1 instanceof ChatMessageCell))
      {
        localObject1 = ((ChatMessageCell)localObject1).getMessageObject();
        this.floatingDateView.setCustomDate(((MessageObject)localObject1).messageOwner.date);
      }
    }
    else
    {
      this.currentFloatingDateOnScreen = false;
      if (((localObject3 instanceof ChatMessageCell)) || ((localObject3 instanceof ChatActionCell))) {
        break label761;
      }
      bool = true;
      label614:
      this.currentFloatingTopIsNotMessage = bool;
      if (localObject5 == null) {
        break label866;
      }
      if ((((View)localObject5).getTop() <= this.chatListView.getPaddingTop()) && (!this.currentFloatingTopIsNotMessage)) {
        break label773;
      }
      if (((View)localObject5).getAlpha() != 1.0F) {
        ((View)localObject5).setAlpha(1.0F);
      }
      if (this.currentFloatingTopIsNotMessage) {
        break label767;
      }
      bool = true;
      label673:
      hideFloatingDateView(bool);
    }
    for (;;)
    {
      i = ((View)localObject5).getBottom() - this.chatListView.getPaddingTop();
      if ((i <= this.floatingDateView.getMeasuredHeight()) || (i >= this.floatingDateView.getMeasuredHeight() * 2)) {
        break label857;
      }
      this.floatingDateView.setTranslationY(-this.floatingDateView.getMeasuredHeight() * 2 + i);
      return;
      label738:
      MediaController.getInstance().setCurrentRoundVisible(true);
      break;
      localObject1 = ((ChatActionCell)localObject1).getMessageObject();
      break label575;
      label761:
      bool = false;
      break label614;
      label767:
      bool = false;
      break label673;
      label773:
      if (((View)localObject5).getAlpha() != 0.0F) {
        ((View)localObject5).setAlpha(0.0F);
      }
      if (this.floatingDateAnimation != null)
      {
        this.floatingDateAnimation.cancel();
        this.floatingDateAnimation = null;
      }
      if (this.floatingDateView.getTag() == null) {
        this.floatingDateView.setTag(Integer.valueOf(1));
      }
      if (this.floatingDateView.getAlpha() != 1.0F) {
        this.floatingDateView.setAlpha(1.0F);
      }
      this.currentFloatingDateOnScreen = true;
    }
    label857:
    this.floatingDateView.setTranslationY(0.0F);
    return;
    label866:
    hideFloatingDateView(true);
    this.floatingDateView.setTranslationY(0.0F);
  }
  
  private void updateTextureViewPosition()
  {
    int k = 0;
    int m = this.chatListView.getChildCount();
    int i = 0;
    for (;;)
    {
      int j = k;
      Object localObject1;
      if (i < m)
      {
        localObject1 = this.chatListView.getChildAt(i);
        if ((localObject1 instanceof ChatMessageCell))
        {
          localObject1 = (ChatMessageCell)localObject1;
          Object localObject2 = ((ChatMessageCell)localObject1).getMessageObject();
          if ((this.roundVideoContainer != null) && (((MessageObject)localObject2).isRoundVideo()) && (MediaController.getInstance().isPlayingMessage((MessageObject)localObject2)))
          {
            localObject2 = ((ChatMessageCell)localObject1).getPhotoImage();
            this.roundVideoContainer.setTranslationX(((ImageReceiver)localObject2).getImageX());
            this.roundVideoContainer.setTranslationY(this.fragmentView.getPaddingTop() + ((ChatMessageCell)localObject1).getTop() + ((ImageReceiver)localObject2).getImageY());
            this.fragmentView.invalidate();
            this.roundVideoContainer.invalidate();
            j = 1;
          }
        }
      }
      else
      {
        if (this.roundVideoContainer != null)
        {
          localObject1 = MediaController.getInstance().getPlayingMessageObject();
          if (j != 0) {
            break;
          }
          this.roundVideoContainer.setTranslationY(-AndroidUtilities.roundMessageSize - 100);
          this.fragmentView.invalidate();
          if ((localObject1 != null) && (((MessageObject)localObject1).isRoundVideo()) && ((this.checkTextureViewPosition) || (PipRoundVideoView.getInstance() != null))) {
            MediaController.getInstance().setCurrentRoundVisible(false);
          }
        }
        return;
      }
      i += 1;
    }
    MediaController.getInstance().setCurrentRoundVisible(true);
  }
  
  public View createView(Context paramContext)
  {
    if (this.chatMessageCellsCache.isEmpty())
    {
      int i = 0;
      while (i < 8)
      {
        this.chatMessageCellsCache.add(new ChatMessageCell(paramContext));
        i += 1;
      }
    }
    this.searchWas = false;
    this.hasOwnBackground = true;
    Theme.createChatResources(paramContext, false);
    this.actionBar.setAddToContainer(false);
    Object localObject = this.actionBar;
    boolean bool;
    if ((Build.VERSION.SDK_INT >= 21) && (!AndroidUtilities.isTablet()))
    {
      bool = true;
      ((ActionBar)localObject).setOccupyStatusBar(bool);
      this.actionBar.setBackButtonDrawable(new BackDrawable(false));
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            ChannelAdminLogActivity.this.finishFragment();
          }
        }
      });
      this.avatarContainer = new ChatAvatarContainer(paramContext, null, false);
      localObject = this.avatarContainer;
      if (AndroidUtilities.isTablet()) {
        break label1512;
      }
      bool = true;
      label150:
      ((ChatAvatarContainer)localObject).setOccupyStatusBar(bool);
      this.actionBar.addView(this.avatarContainer, 0, LayoutHelper.createFrame(-2, -1.0F, 51, 56.0F, 0.0F, 40.0F, 0.0F));
      this.searchItem = this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
      {
        public void onSearchCollapse()
        {
          ChannelAdminLogActivity.access$1402(ChannelAdminLogActivity.this, "");
          ChannelAdminLogActivity.this.avatarContainer.setVisibility(0);
          if (ChannelAdminLogActivity.this.searchWas)
          {
            ChannelAdminLogActivity.access$1602(ChannelAdminLogActivity.this, false);
            ChannelAdminLogActivity.this.loadMessages(true);
          }
          ChannelAdminLogActivity.this.updateBottomOverlay();
        }
        
        public void onSearchExpand()
        {
          ChannelAdminLogActivity.this.avatarContainer.setVisibility(8);
          ChannelAdminLogActivity.this.updateBottomOverlay();
        }
        
        public void onSearchPressed(EditText paramAnonymousEditText)
        {
          ChannelAdminLogActivity.access$1602(ChannelAdminLogActivity.this, true);
          ChannelAdminLogActivity.access$1402(ChannelAdminLogActivity.this, paramAnonymousEditText.getText().toString());
          ChannelAdminLogActivity.this.loadMessages(true);
        }
      });
      this.searchItem.getSearchField().setHint(LocaleController.getString("Search", 2131494298));
      this.avatarContainer.setEnabled(false);
      this.avatarContainer.setTitle(this.currentChat.title);
      this.avatarContainer.setSubtitle(LocaleController.getString("EventLogAllEvents", 2131493459));
      this.avatarContainer.setChatAvatar(this.currentChat);
      this.fragmentView = new SizeNotifierFrameLayout(paramContext)
      {
        protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
          if ((paramAnonymousView == ChannelAdminLogActivity.this.actionBar) && (ChannelAdminLogActivity.this.parentLayout != null))
          {
            paramAnonymousView = ChannelAdminLogActivity.this.parentLayout;
            if (ChannelAdminLogActivity.this.actionBar.getVisibility() != 0) {
              break label73;
            }
          }
          label73:
          for (int i = ChannelAdminLogActivity.this.actionBar.getMeasuredHeight();; i = 0)
          {
            paramAnonymousView.drawHeaderShadow(paramAnonymousCanvas, i);
            return bool;
          }
        }
        
        protected boolean isActionBarVisible()
        {
          return ChannelAdminLogActivity.this.actionBar.getVisibility() == 0;
        }
        
        protected void onAttachedToWindow()
        {
          super.onAttachedToWindow();
          MessageObject localMessageObject = MediaController.getInstance().getPlayingMessageObject();
          if ((localMessageObject != null) && (localMessageObject.isRoundVideo()) && (localMessageObject.eventId != 0L) && (localMessageObject.getDialogId() == -ChannelAdminLogActivity.this.currentChat.id)) {
            MediaController.getInstance().setTextureView(ChannelAdminLogActivity.this.createTextureView(false), ChannelAdminLogActivity.this.aspectRatioFrameLayout, ChannelAdminLogActivity.this.roundVideoContainer, true);
          }
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          int n = getChildCount();
          int m = 0;
          while (m < n)
          {
            View localView = getChildAt(m);
            if (localView.getVisibility() == 8)
            {
              m += 1;
            }
            else
            {
              FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)localView.getLayoutParams();
              int i1 = localView.getMeasuredWidth();
              int i2 = localView.getMeasuredHeight();
              int j = localLayoutParams.gravity;
              int i = j;
              if (j == -1) {
                i = 51;
              }
              label131:
              label179:
              int k;
              switch (i & 0x7 & 0x7)
              {
              default: 
                j = localLayoutParams.leftMargin;
                switch (i & 0x70)
                {
                default: 
                  i = localLayoutParams.topMargin;
                  if (localView == ChannelAdminLogActivity.this.emptyViewContainer)
                  {
                    int i3 = AndroidUtilities.dp(24.0F);
                    if (ChannelAdminLogActivity.this.actionBar.getVisibility() == 0)
                    {
                      k = ChannelAdminLogActivity.this.actionBar.getMeasuredHeight() / 2;
                      label225:
                      k = i - (i3 - k);
                    }
                  }
                  break;
                }
                break;
              }
              for (;;)
              {
                localView.layout(j, k, j + i1, k + i2);
                break;
                j = (paramAnonymousInt3 - paramAnonymousInt1 - i1) / 2 + localLayoutParams.leftMargin - localLayoutParams.rightMargin;
                break label131;
                j = paramAnonymousInt3 - i1 - localLayoutParams.rightMargin;
                break label131;
                k = localLayoutParams.topMargin + getPaddingTop();
                i = k;
                if (localView == ChannelAdminLogActivity.this.actionBar) {
                  break label179;
                }
                i = k;
                if (ChannelAdminLogActivity.this.actionBar.getVisibility() != 0) {
                  break label179;
                }
                i = k + ChannelAdminLogActivity.this.actionBar.getMeasuredHeight();
                break label179;
                i = (paramAnonymousInt4 - paramAnonymousInt2 - i2) / 2 + localLayoutParams.topMargin - localLayoutParams.bottomMargin;
                break label179;
                i = paramAnonymousInt4 - paramAnonymousInt2 - i2 - localLayoutParams.bottomMargin;
                break label179;
                k = 0;
                break label225;
                k = i;
                if (localView == ChannelAdminLogActivity.this.actionBar) {
                  k = i - getPaddingTop();
                }
              }
            }
          }
          ChannelAdminLogActivity.this.updateMessagesVisisblePart();
          notifyHeightChanged();
        }
        
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int k = View.MeasureSpec.getSize(paramAnonymousInt1);
          int i = View.MeasureSpec.getSize(paramAnonymousInt2);
          setMeasuredDimension(k, i);
          int j = i - getPaddingTop();
          measureChildWithMargins(ChannelAdminLogActivity.this.actionBar, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
          int m = ChannelAdminLogActivity.this.actionBar.getMeasuredHeight();
          i = j;
          if (ChannelAdminLogActivity.this.actionBar.getVisibility() == 0) {
            i = j - m;
          }
          getKeyboardHeight();
          m = getChildCount();
          j = 0;
          if (j < m)
          {
            View localView = getChildAt(j);
            if ((localView == null) || (localView.getVisibility() == 8) || (localView == ChannelAdminLogActivity.this.actionBar)) {}
            for (;;)
            {
              j += 1;
              break;
              if ((localView == ChannelAdminLogActivity.this.chatListView) || (localView == ChannelAdminLogActivity.this.progressView)) {
                localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0F), i - AndroidUtilities.dp(50.0F)), 1073741824));
              } else if (localView == ChannelAdminLogActivity.this.emptyViewContainer) {
                localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(i, 1073741824));
              } else {
                measureChildWithMargins(localView, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
              }
            }
          }
        }
      };
      this.contentView = ((SizeNotifierFrameLayout)this.fragmentView);
      localObject = this.contentView;
      if (AndroidUtilities.isTablet()) {
        break label1517;
      }
      bool = true;
      label325:
      ((SizeNotifierFrameLayout)localObject).setOccupyStatusBar(bool);
      this.contentView.setBackgroundImage(Theme.getCachedWallpaper());
      this.emptyViewContainer = new FrameLayout(paramContext);
      this.emptyViewContainer.setVisibility(4);
      this.contentView.addView(this.emptyViewContainer, LayoutHelper.createFrame(-1, -2, 17));
      this.emptyViewContainer.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          return true;
        }
      });
      this.emptyView = new TextView(paramContext);
      this.emptyView.setTextSize(1, 14.0F);
      this.emptyView.setGravity(17);
      this.emptyView.setTextColor(Theme.getColor("chat_serviceText"));
      this.emptyView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(10.0F), Theme.getServiceMessageColor()));
      this.emptyView.setPadding(AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F));
      this.emptyViewContainer.addView(this.emptyView, LayoutHelper.createFrame(-2, -2.0F, 17, 16.0F, 0.0F, 16.0F, 0.0F));
      this.chatListView = new RecyclerListView(paramContext)
      {
        public boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
          ChatMessageCell localChatMessageCell;
          ImageReceiver localImageReceiver;
          Object localObject;
          if ((paramAnonymousView instanceof ChatMessageCell))
          {
            localChatMessageCell = (ChatMessageCell)paramAnonymousView;
            localImageReceiver = localChatMessageCell.getAvatarImage();
            if (localImageReceiver != null)
            {
              j = paramAnonymousView.getTop();
              if (!localChatMessageCell.isPinnedBottom()) {
                break label107;
              }
              localObject = ChannelAdminLogActivity.this.chatListView.getChildViewHolder(paramAnonymousView);
              if ((localObject == null) || (ChannelAdminLogActivity.this.chatListView.findViewHolderForAdapterPosition(((RecyclerView.ViewHolder)localObject).getAdapterPosition() + 1) == null)) {
                break label107;
              }
              localImageReceiver.setImageY(-AndroidUtilities.dp(1000.0F));
              localImageReceiver.draw(paramAnonymousCanvas);
            }
          }
          return bool;
          label107:
          int i = j;
          if (localChatMessageCell.isPinnedTop())
          {
            localObject = ChannelAdminLogActivity.this.chatListView.getChildViewHolder(paramAnonymousView);
            i = j;
            if (localObject != null)
            {
              i = j;
              RecyclerView.ViewHolder localViewHolder;
              do
              {
                localViewHolder = ChannelAdminLogActivity.this.chatListView.findViewHolderForAdapterPosition(((RecyclerView.ViewHolder)localObject).getAdapterPosition() - 1);
                if (localViewHolder == null) {
                  break;
                }
                j = localViewHolder.itemView.getTop();
                i = j;
                if (!(localViewHolder.itemView instanceof ChatMessageCell)) {
                  break;
                }
                localObject = localViewHolder;
                i = j;
              } while (((ChatMessageCell)localViewHolder.itemView).isPinnedTop());
              i = j;
            }
          }
          int k = paramAnonymousView.getTop() + localChatMessageCell.getLayoutHeight();
          int m = ChannelAdminLogActivity.this.chatListView.getHeight() - ChannelAdminLogActivity.this.chatListView.getPaddingBottom();
          int j = k;
          if (k > m) {
            j = m;
          }
          k = j;
          if (j - AndroidUtilities.dp(48.0F) < i) {
            k = i + AndroidUtilities.dp(48.0F);
          }
          localImageReceiver.setImageY(k - AndroidUtilities.dp(44.0F));
          localImageReceiver.draw(paramAnonymousCanvas);
          return bool;
        }
      };
      this.chatListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          ChannelAdminLogActivity.this.createMenu(paramAnonymousView);
        }
      });
      this.chatListView.setTag(Integer.valueOf(1));
      this.chatListView.setVerticalScrollBarEnabled(true);
      localObject = this.chatListView;
      ChatActivityAdapter localChatActivityAdapter = new ChatActivityAdapter(paramContext);
      this.chatAdapter = localChatActivityAdapter;
      ((RecyclerListView)localObject).setAdapter(localChatActivityAdapter);
      this.chatListView.setClipToPadding(false);
      this.chatListView.setPadding(0, AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(3.0F));
      this.chatListView.setItemAnimator(null);
      this.chatListView.setLayoutAnimation(null);
      this.chatLayoutManager = new LinearLayoutManager(paramContext)
      {
        public void smoothScrollToPosition(RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState, int paramAnonymousInt)
        {
          paramAnonymousRecyclerView = new LinearSmoothScrollerMiddle(paramAnonymousRecyclerView.getContext());
          paramAnonymousRecyclerView.setTargetPosition(paramAnonymousInt);
          startSmoothScroll(paramAnonymousRecyclerView);
        }
        
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      this.chatLayoutManager.setOrientation(1);
      this.chatLayoutManager.setStackFromEnd(true);
      this.chatListView.setLayoutManager(this.chatLayoutManager);
      this.contentView.addView(this.chatListView, LayoutHelper.createFrame(-1, -1.0F));
      this.chatListView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        private final int scrollValue = AndroidUtilities.dp(100.0F);
        private float totalDy = 0.0F;
        
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if (paramAnonymousInt == 1)
          {
            ChannelAdminLogActivity.access$4002(ChannelAdminLogActivity.this, true);
            ChannelAdminLogActivity.access$4102(ChannelAdminLogActivity.this, true);
          }
          while (paramAnonymousInt != 0) {
            return;
          }
          ChannelAdminLogActivity.access$4002(ChannelAdminLogActivity.this, false);
          ChannelAdminLogActivity.access$4102(ChannelAdminLogActivity.this, false);
          ChannelAdminLogActivity.this.hideFloatingDateView(true);
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          ChannelAdminLogActivity.this.chatListView.invalidate();
          if ((paramAnonymousInt2 != 0) && (ChannelAdminLogActivity.this.scrollingFloatingDate) && (!ChannelAdminLogActivity.this.currentFloatingTopIsNotMessage) && (ChannelAdminLogActivity.this.floatingDateView.getTag() == null))
          {
            if (ChannelAdminLogActivity.this.floatingDateAnimation != null) {
              ChannelAdminLogActivity.this.floatingDateAnimation.cancel();
            }
            ChannelAdminLogActivity.this.floatingDateView.setTag(Integer.valueOf(1));
            ChannelAdminLogActivity.access$4502(ChannelAdminLogActivity.this, new AnimatorSet());
            ChannelAdminLogActivity.this.floatingDateAnimation.setDuration(150L);
            ChannelAdminLogActivity.this.floatingDateAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(ChannelAdminLogActivity.this.floatingDateView, "alpha", new float[] { 1.0F }) });
            ChannelAdminLogActivity.this.floatingDateAnimation.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if (paramAnonymous2Animator.equals(ChannelAdminLogActivity.this.floatingDateAnimation)) {
                  ChannelAdminLogActivity.access$4502(ChannelAdminLogActivity.this, null);
                }
              }
            });
            ChannelAdminLogActivity.this.floatingDateAnimation.start();
          }
          ChannelAdminLogActivity.this.checkScrollForLoad(true);
          ChannelAdminLogActivity.this.updateMessagesVisisblePart();
        }
      });
      if (this.scrollToPositionOnRecreate != -1)
      {
        this.chatLayoutManager.scrollToPositionWithOffset(this.scrollToPositionOnRecreate, this.scrollToOffsetOnRecreate);
        this.scrollToPositionOnRecreate = -1;
      }
      this.progressView = new FrameLayout(paramContext);
      this.progressView.setVisibility(4);
      this.contentView.addView(this.progressView, LayoutHelper.createFrame(-1, -1, 51));
      this.progressView2 = new View(paramContext);
      this.progressView2.setBackgroundResource(2131165672);
      this.progressView2.getBackground().setColorFilter(Theme.colorFilter);
      this.progressView.addView(this.progressView2, LayoutHelper.createFrame(36, 36, 17));
      this.progressBar = new RadialProgressView(paramContext);
      this.progressBar.setSize(AndroidUtilities.dp(28.0F));
      this.progressBar.setProgressColor(Theme.getColor("chat_serviceText"));
      this.progressView.addView(this.progressBar, LayoutHelper.createFrame(32, 32, 17));
      this.floatingDateView = new ChatActionCell(paramContext);
      this.floatingDateView.setAlpha(0.0F);
      this.contentView.addView(this.floatingDateView, LayoutHelper.createFrame(-2, -2.0F, 49, 0.0F, 4.0F, 0.0F, 0.0F));
      this.contentView.addView(this.actionBar);
      this.bottomOverlayChat = new FrameLayout(paramContext)
      {
        public void onDraw(Canvas paramAnonymousCanvas)
        {
          int i = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
          Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), i);
          Theme.chat_composeShadowDrawable.draw(paramAnonymousCanvas);
          paramAnonymousCanvas.drawRect(0.0F, i, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
        }
      };
      this.bottomOverlayChat.setWillNotDraw(false);
      this.bottomOverlayChat.setPadding(0, AndroidUtilities.dp(3.0F), 0, 0);
      this.contentView.addView(this.bottomOverlayChat, LayoutHelper.createFrame(-1, 51, 80));
      this.bottomOverlayChat.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChannelAdminLogActivity.this.getParentActivity() == null) {
            return;
          }
          paramAnonymousView = new AdminLogFilterAlert(ChannelAdminLogActivity.this.getParentActivity(), ChannelAdminLogActivity.this.currentFilter, ChannelAdminLogActivity.this.selectedAdmins, ChannelAdminLogActivity.this.currentChat.megagroup);
          paramAnonymousView.setCurrentAdmins(ChannelAdminLogActivity.this.admins);
          paramAnonymousView.setAdminLogFilterAlertDelegate(new AdminLogFilterAlert.AdminLogFilterAlertDelegate()
          {
            public void didSelectRights(TLRPC.TL_channelAdminLogEventsFilter paramAnonymous2TL_channelAdminLogEventsFilter, SparseArray<TLRPC.User> paramAnonymous2SparseArray)
            {
              ChannelAdminLogActivity.access$4702(ChannelAdminLogActivity.this, paramAnonymous2TL_channelAdminLogEventsFilter);
              ChannelAdminLogActivity.access$4802(ChannelAdminLogActivity.this, paramAnonymous2SparseArray);
              if ((ChannelAdminLogActivity.this.currentFilter != null) || (ChannelAdminLogActivity.this.selectedAdmins != null)) {
                ChannelAdminLogActivity.this.avatarContainer.setSubtitle(LocaleController.getString("EventLogSelectedEvents", 2131493522));
              }
              for (;;)
              {
                ChannelAdminLogActivity.this.loadMessages(true);
                return;
                ChannelAdminLogActivity.this.avatarContainer.setSubtitle(LocaleController.getString("EventLogAllEvents", 2131493459));
              }
            }
          });
          ChannelAdminLogActivity.this.showDialog(paramAnonymousView);
        }
      });
      this.bottomOverlayChatText = new TextView(paramContext);
      this.bottomOverlayChatText.setTextSize(1, 15.0F);
      this.bottomOverlayChatText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.bottomOverlayChatText.setTextColor(Theme.getColor("chat_fieldOverlayText"));
      this.bottomOverlayChatText.setText(LocaleController.getString("SETTINGS", 2131494283).toUpperCase());
      this.bottomOverlayChat.addView(this.bottomOverlayChatText, LayoutHelper.createFrame(-2, -2, 17));
      this.bottomOverlayImage = new ImageView(paramContext);
      this.bottomOverlayImage.setImageResource(2131165480);
      this.bottomOverlayImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_fieldOverlayText"), PorterDuff.Mode.MULTIPLY));
      this.bottomOverlayImage.setScaleType(ImageView.ScaleType.CENTER);
      this.bottomOverlayChat.addView(this.bottomOverlayImage, LayoutHelper.createFrame(48, 48.0F, 53, 3.0F, 0.0F, 0.0F, 0.0F));
      this.bottomOverlayImage.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = new AlertDialog.Builder(ChannelAdminLogActivity.this.getParentActivity());
          if (ChannelAdminLogActivity.this.currentChat.megagroup) {
            paramAnonymousView.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("EventLogInfoDetail", 2131493490)));
          }
          for (;;)
          {
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
            paramAnonymousView.setTitle(LocaleController.getString("EventLogInfoTitle", 2131493492));
            ChannelAdminLogActivity.this.showDialog(paramAnonymousView.create());
            return;
            paramAnonymousView.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("EventLogInfoDetailChannel", 2131493491)));
          }
        }
      });
      this.searchContainer = new FrameLayout(paramContext)
      {
        public void onDraw(Canvas paramAnonymousCanvas)
        {
          int i = Theme.chat_composeShadowDrawable.getIntrinsicHeight();
          Theme.chat_composeShadowDrawable.setBounds(0, 0, getMeasuredWidth(), i);
          Theme.chat_composeShadowDrawable.draw(paramAnonymousCanvas);
          paramAnonymousCanvas.drawRect(0.0F, i, getMeasuredWidth(), getMeasuredHeight(), Theme.chat_composeBackgroundPaint);
        }
      };
      this.searchContainer.setWillNotDraw(false);
      this.searchContainer.setVisibility(4);
      this.searchContainer.setFocusable(true);
      this.searchContainer.setFocusableInTouchMode(true);
      this.searchContainer.setClickable(true);
      this.searchContainer.setPadding(0, AndroidUtilities.dp(3.0F), 0, 0);
      this.contentView.addView(this.searchContainer, LayoutHelper.createFrame(-1, 51, 80));
      this.searchCalendarButton = new ImageView(paramContext);
      this.searchCalendarButton.setScaleType(ImageView.ScaleType.CENTER);
      this.searchCalendarButton.setImageResource(2131165628);
      this.searchCalendarButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_searchPanelIcons"), PorterDuff.Mode.MULTIPLY));
      this.searchContainer.addView(this.searchCalendarButton, LayoutHelper.createFrame(48, 48, 53));
      this.searchCalendarButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ChannelAdminLogActivity.this.getParentActivity() == null) {
            return;
          }
          AndroidUtilities.hideKeyboard(ChannelAdminLogActivity.this.searchItem.getSearchField());
          paramAnonymousView = Calendar.getInstance();
          int i = paramAnonymousView.get(1);
          int j = paramAnonymousView.get(2);
          int k = paramAnonymousView.get(5);
          try
          {
            paramAnonymousView = new DatePickerDialog(ChannelAdminLogActivity.this.getParentActivity(), new DatePickerDialog.OnDateSetListener()
            {
              public void onDateSet(DatePicker paramAnonymous2DatePicker, int paramAnonymous2Int1, int paramAnonymous2Int2, int paramAnonymous2Int3)
              {
                paramAnonymous2DatePicker = Calendar.getInstance();
                paramAnonymous2DatePicker.clear();
                paramAnonymous2DatePicker.set(paramAnonymous2Int1, paramAnonymous2Int2, paramAnonymous2Int3);
                paramAnonymous2Int1 = (int)(paramAnonymous2DatePicker.getTime().getTime() / 1000L);
                ChannelAdminLogActivity.this.loadMessages(true);
              }
            }, i, j, k);
            final DatePicker localDatePicker = paramAnonymousView.getDatePicker();
            localDatePicker.setMinDate(1375315200000L);
            localDatePicker.setMaxDate(System.currentTimeMillis());
            paramAnonymousView.setButton(-1, LocaleController.getString("JumpToDate", 2131493713), paramAnonymousView);
            paramAnonymousView.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int) {}
            });
            if (Build.VERSION.SDK_INT >= 21) {
              paramAnonymousView.setOnShowListener(new DialogInterface.OnShowListener()
              {
                public void onShow(DialogInterface paramAnonymous2DialogInterface)
                {
                  int j = localDatePicker.getChildCount();
                  int i = 0;
                  while (i < j)
                  {
                    paramAnonymous2DialogInterface = localDatePicker.getChildAt(i);
                    ViewGroup.LayoutParams localLayoutParams = paramAnonymous2DialogInterface.getLayoutParams();
                    localLayoutParams.width = -1;
                    paramAnonymous2DialogInterface.setLayoutParams(localLayoutParams);
                    i += 1;
                  }
                }
              });
            }
            ChannelAdminLogActivity.this.showDialog(paramAnonymousView);
            return;
          }
          catch (Exception paramAnonymousView)
          {
            FileLog.e(paramAnonymousView);
          }
        }
      });
      this.searchCountText = new SimpleTextView(paramContext);
      this.searchCountText.setTextColor(Theme.getColor("chat_searchPanelText"));
      this.searchCountText.setTextSize(15);
      this.searchCountText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.searchContainer.addView(this.searchCountText, LayoutHelper.createFrame(-1, -2.0F, 19, 108.0F, 0.0F, 0.0F, 0.0F));
      this.chatAdapter.updateRows();
      if ((!this.loading) || (!this.messages.isEmpty())) {
        break label1522;
      }
      this.progressView.setVisibility(0);
      this.chatListView.setEmptyView(null);
    }
    for (;;)
    {
      updateEmptyPlaceholder();
      return this.fragmentView;
      bool = false;
      break;
      label1512:
      bool = false;
      break label150;
      label1517:
      bool = false;
      break label325;
      label1522:
      this.progressView.setVisibility(4);
      this.chatListView.setEmptyView(this.emptyViewContainer);
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.emojiDidLoaded) {
      if (this.chatListView != null) {
        this.chatListView.invalidateViews();
      }
    }
    label82:
    label147:
    label224:
    label289:
    label450:
    do
    {
      for (;;)
      {
        return;
        Object localObject;
        if (paramInt1 == NotificationCenter.messagePlayingDidStarted)
        {
          if (((MessageObject)paramVarArgs[0]).isRoundVideo())
          {
            MediaController.getInstance().setTextureView(createTextureView(true), this.aspectRatioFrameLayout, this.roundVideoContainer, true);
            updateTextureViewPosition();
          }
          if (this.chatListView != null)
          {
            paramInt2 = this.chatListView.getChildCount();
            paramInt1 = 0;
            if (paramInt1 < paramInt2)
            {
              paramVarArgs = this.chatListView.getChildAt(paramInt1);
              if ((paramVarArgs instanceof ChatMessageCell))
              {
                paramVarArgs = (ChatMessageCell)paramVarArgs;
                localObject = paramVarArgs.getMessageObject();
                if (localObject != null)
                {
                  if ((!((MessageObject)localObject).isVoice()) && (!((MessageObject)localObject).isMusic())) {
                    break label147;
                  }
                  paramVarArgs.updateButtonState(false);
                }
              }
            }
            for (;;)
            {
              paramInt1 += 1;
              break label82;
              break;
              if (((MessageObject)localObject).isRoundVideo())
              {
                paramVarArgs.checkRoundVideoPlayback(false);
                if ((!MediaController.getInstance().isPlayingMessage((MessageObject)localObject)) && (((MessageObject)localObject).audioProgress != 0.0F))
                {
                  ((MessageObject)localObject).resetPlayingProgress();
                  paramVarArgs.invalidate();
                }
              }
            }
          }
        }
        else if ((paramInt1 == NotificationCenter.messagePlayingDidReset) || (paramInt1 == NotificationCenter.messagePlayingPlayStateChanged))
        {
          if (this.chatListView != null)
          {
            paramInt2 = this.chatListView.getChildCount();
            paramInt1 = 0;
            if (paramInt1 < paramInt2)
            {
              paramVarArgs = this.chatListView.getChildAt(paramInt1);
              if ((paramVarArgs instanceof ChatMessageCell))
              {
                paramVarArgs = (ChatMessageCell)paramVarArgs;
                localObject = paramVarArgs.getMessageObject();
                if (localObject != null)
                {
                  if ((!((MessageObject)localObject).isVoice()) && (!((MessageObject)localObject).isMusic())) {
                    break label289;
                  }
                  paramVarArgs.updateButtonState(false);
                }
              }
            }
            for (;;)
            {
              paramInt1 += 1;
              break label224;
              break;
              if ((((MessageObject)localObject).isRoundVideo()) && (!MediaController.getInstance().isPlayingMessage((MessageObject)localObject))) {
                paramVarArgs.checkRoundVideoPlayback(true);
              }
            }
          }
        }
        else
        {
          if (paramInt1 != NotificationCenter.messagePlayingProgressDidChanged) {
            break;
          }
          paramVarArgs = (Integer)paramVarArgs[0];
          if (this.chatListView != null)
          {
            paramInt2 = this.chatListView.getChildCount();
            paramInt1 = 0;
            for (;;)
            {
              if (paramInt1 >= paramInt2) {
                break label450;
              }
              localObject = this.chatListView.getChildAt(paramInt1);
              if ((localObject instanceof ChatMessageCell))
              {
                localObject = (ChatMessageCell)localObject;
                MessageObject localMessageObject = ((ChatMessageCell)localObject).getMessageObject();
                if ((localMessageObject != null) && (localMessageObject.getId() == paramVarArgs.intValue()))
                {
                  paramVarArgs = MediaController.getInstance().getPlayingMessageObject();
                  if (paramVarArgs == null) {
                    break;
                  }
                  localMessageObject.audioProgress = paramVarArgs.audioProgress;
                  localMessageObject.audioProgressSec = paramVarArgs.audioProgressSec;
                  localMessageObject.audioPlayerDuration = paramVarArgs.audioPlayerDuration;
                  ((ChatMessageCell)localObject).updatePlayingMessageProgress();
                  return;
                }
              }
              paramInt1 += 1;
            }
          }
        }
      }
    } while ((paramInt1 != NotificationCenter.didSetNewWallpapper) || (this.fragmentView == null));
    ((SizeNotifierFrameLayout)this.fragmentView).setBackgroundImage(Theme.getCachedWallpaper());
    this.progressView2.getBackground().setColorFilter(Theme.colorFilter);
    if (this.emptyView != null) {
      this.emptyView.getBackground().setColorFilter(Theme.colorFilter);
    }
    this.chatListView.invalidateViews();
  }
  
  public TLRPC.Chat getCurrentChat()
  {
    return this.currentChat;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, 0, null, null, null, null, "chat_wallpaper");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, "actionBarDefaultSubmenuBackground");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, "actionBarDefaultSubmenuItem");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.avatarContainer.getTitleTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.avatarContainer.getSubtitleTextView(), ThemeDescription.FLAG_TEXTCOLOR, null, new Paint[] { Theme.chat_statusPaint, Theme.chat_statusRecordPaint }, null, null, "actionBarDefaultSubtitle", null);
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    Object localObject1 = this.chatListView;
    Object localObject2 = Theme.avatar_photoDrawable;
    Object localObject3 = Theme.avatar_broadcastDrawable;
    Object localObject4 = Theme.avatar_savedDrawable;
    localObject2 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject2, localObject3, localObject4 }, null, "avatar_text");
    localObject3 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundRed");
    localObject4 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundOrange");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundViolet");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundGreen");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundCyan");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundBlue");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_backgroundPink");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageRed");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageOrange");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageViolet");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageGreen");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageCyan");
    ThemeDescription localThemeDescription24 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessageBlue");
    ThemeDescription localThemeDescription25 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "avatar_nameInMessagePink");
    localObject1 = this.chatListView;
    Object localObject5 = Theme.chat_msgInDrawable;
    Object localObject6 = Theme.chat_msgInMediaDrawable;
    localObject5 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject5, localObject6 }, null, "chat_inBubble");
    localObject1 = this.chatListView;
    localObject6 = Theme.chat_msgInSelectedDrawable;
    Object localObject7 = Theme.chat_msgInMediaSelectedDrawable;
    localObject6 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject6, localObject7 }, null, "chat_inBubbleSelected");
    localObject1 = this.chatListView;
    localObject7 = Theme.chat_msgInShadowDrawable;
    Object localObject8 = Theme.chat_msgInMediaShadowDrawable;
    localObject7 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject7, localObject8 }, null, "chat_inBubbleShadow");
    localObject1 = this.chatListView;
    localObject8 = Theme.chat_msgOutDrawable;
    Object localObject9 = Theme.chat_msgOutMediaDrawable;
    localObject8 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject8, localObject9 }, null, "chat_outBubble");
    localObject1 = this.chatListView;
    localObject9 = Theme.chat_msgOutSelectedDrawable;
    Object localObject10 = Theme.chat_msgOutMediaSelectedDrawable;
    localObject9 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject9, localObject10 }, null, "chat_outBubbleSelected");
    localObject1 = this.chatListView;
    localObject10 = Theme.chat_msgOutShadowDrawable;
    Object localObject11 = Theme.chat_msgOutMediaShadowDrawable;
    localObject10 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject10, localObject11 }, null, "chat_outBubbleShadow");
    localObject1 = this.chatListView;
    int i = ThemeDescription.FLAG_TEXTCOLOR;
    localObject11 = Theme.chat_actionTextPaint;
    localObject11 = new ThemeDescription((View)localObject1, i, new Class[] { ChatActionCell.class }, (Paint)localObject11, null, null, "chat_serviceText");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_LINKCOLOR;
    Object localObject12 = Theme.chat_actionTextPaint;
    localObject12 = new ThemeDescription((View)localObject1, i, new Class[] { ChatActionCell.class }, (Paint)localObject12, null, null, "chat_serviceLink");
    localObject1 = this.chatListView;
    Object localObject13 = Theme.chat_shareIconDrawable;
    Object localObject14 = Theme.chat_botInlineDrawable;
    Object localObject15 = Theme.chat_botLinkDrawalbe;
    Object localObject16 = Theme.chat_goIconDrawable;
    localObject13 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject13, localObject14, localObject15, localObject16 }, null, "chat_serviceIcon");
    localObject14 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class, ChatActionCell.class }, null, null, null, "chat_serviceBackground");
    localObject15 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class, ChatActionCell.class }, null, null, null, "chat_serviceBackgroundSelected");
    localObject16 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_messageTextIn");
    ThemeDescription localThemeDescription26 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_messageTextOut");
    ThemeDescription localThemeDescription27 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_LINKCOLOR, new Class[] { ChatMessageCell.class }, null, null, null, "chat_messageLinkIn", null);
    ThemeDescription localThemeDescription28 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_LINKCOLOR, new Class[] { ChatMessageCell.class }, null, null, null, "chat_messageLinkOut", null);
    localObject1 = this.chatListView;
    Object localObject17 = Theme.chat_msgOutCheckDrawable;
    Object localObject18 = Theme.chat_msgOutHalfCheckDrawable;
    localObject17 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject17, localObject18 }, null, "chat_outSentCheck");
    localObject1 = this.chatListView;
    localObject18 = Theme.chat_msgOutCheckSelectedDrawable;
    Object localObject19 = Theme.chat_msgOutHalfCheckSelectedDrawable;
    localObject18 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject18, localObject19 }, null, "chat_outSentCheckSelected");
    localObject1 = this.chatListView;
    localObject19 = Theme.chat_msgOutClockDrawable;
    localObject19 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject19 }, null, "chat_outSentClock");
    localObject1 = this.chatListView;
    Object localObject20 = Theme.chat_msgOutSelectedClockDrawable;
    localObject20 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject20 }, null, "chat_outSentClockSelected");
    localObject1 = this.chatListView;
    Object localObject21 = Theme.chat_msgInClockDrawable;
    localObject21 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject21 }, null, "chat_inSentClock");
    localObject1 = this.chatListView;
    Object localObject22 = Theme.chat_msgInSelectedClockDrawable;
    localObject22 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject22 }, null, "chat_inSentClockSelected");
    localObject1 = this.chatListView;
    Object localObject23 = Theme.chat_msgMediaCheckDrawable;
    Object localObject24 = Theme.chat_msgMediaHalfCheckDrawable;
    localObject23 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject23, localObject24 }, null, "chat_mediaSentCheck");
    localObject1 = this.chatListView;
    localObject24 = Theme.chat_msgStickerHalfCheckDrawable;
    Object localObject25 = Theme.chat_msgStickerCheckDrawable;
    Object localObject26 = Theme.chat_msgStickerClockDrawable;
    Object localObject27 = Theme.chat_msgStickerViewsDrawable;
    localObject24 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject24, localObject25, localObject26, localObject27 }, null, "chat_serviceText");
    localObject1 = this.chatListView;
    localObject25 = Theme.chat_msgMediaClockDrawable;
    localObject25 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject25 }, null, "chat_mediaSentClock");
    localObject1 = this.chatListView;
    localObject26 = Theme.chat_msgOutViewsDrawable;
    localObject26 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject26 }, null, "chat_outViews");
    localObject1 = this.chatListView;
    localObject27 = Theme.chat_msgOutViewsSelectedDrawable;
    localObject27 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject27 }, null, "chat_outViewsSelected");
    localObject1 = this.chatListView;
    Object localObject28 = Theme.chat_msgInViewsDrawable;
    localObject28 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject28 }, null, "chat_inViews");
    localObject1 = this.chatListView;
    Object localObject29 = Theme.chat_msgInViewsSelectedDrawable;
    localObject29 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject29 }, null, "chat_inViewsSelected");
    localObject1 = this.chatListView;
    Object localObject30 = Theme.chat_msgMediaViewsDrawable;
    localObject30 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject30 }, null, "chat_mediaViews");
    localObject1 = this.chatListView;
    Object localObject31 = Theme.chat_msgOutMenuDrawable;
    localObject31 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject31 }, null, "chat_outMenu");
    localObject1 = this.chatListView;
    Object localObject32 = Theme.chat_msgOutMenuSelectedDrawable;
    localObject32 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject32 }, null, "chat_outMenuSelected");
    localObject1 = this.chatListView;
    Object localObject33 = Theme.chat_msgInMenuDrawable;
    localObject33 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject33 }, null, "chat_inMenu");
    localObject1 = this.chatListView;
    Object localObject34 = Theme.chat_msgInMenuSelectedDrawable;
    localObject34 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject34 }, null, "chat_inMenuSelected");
    localObject1 = this.chatListView;
    Object localObject35 = Theme.chat_msgMediaMenuDrawable;
    localObject35 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject35 }, null, "chat_mediaMenu");
    localObject1 = this.chatListView;
    Object localObject36 = Theme.chat_msgOutInstantDrawable;
    Object localObject37 = Theme.chat_msgOutCallDrawable;
    localObject36 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject36, localObject37 }, null, "chat_outInstant");
    localObject1 = this.chatListView;
    localObject37 = Theme.chat_msgOutCallSelectedDrawable;
    localObject37 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject37 }, null, "chat_outInstantSelected");
    localObject1 = this.chatListView;
    Object localObject38 = Theme.chat_msgInInstantDrawable;
    Object localObject39 = Theme.chat_msgInCallDrawable;
    localObject38 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject38, localObject39 }, null, "chat_inInstant");
    localObject1 = this.chatListView;
    localObject39 = Theme.chat_msgInCallSelectedDrawable;
    localObject39 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject39 }, null, "chat_inInstantSelected");
    localObject1 = this.chatListView;
    Object localObject40 = Theme.chat_msgCallUpRedDrawable;
    Object localObject41 = Theme.chat_msgCallDownRedDrawable;
    localObject40 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject40, localObject41 }, null, "calls_callReceivedRedIcon");
    localObject1 = this.chatListView;
    localObject41 = Theme.chat_msgCallUpGreenDrawable;
    Object localObject42 = Theme.chat_msgCallDownGreenDrawable;
    localObject41 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject41, localObject42 }, null, "calls_callReceivedGreenIcon");
    localObject1 = this.chatListView;
    localObject42 = Theme.chat_msgErrorPaint;
    localObject42 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject42, null, null, "chat_sentError");
    localObject1 = this.chatListView;
    Object localObject43 = Theme.chat_msgErrorDrawable;
    localObject43 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject43 }, null, "chat_sentErrorIcon");
    localObject1 = this.chatListView;
    Object localObject44 = Theme.chat_durationPaint;
    localObject44 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject44, null, null, "chat_previewDurationText");
    localObject1 = this.chatListView;
    Object localObject45 = Theme.chat_gamePaint;
    localObject45 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject45, null, null, "chat_previewGameText");
    ThemeDescription localThemeDescription29 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inPreviewInstantText");
    ThemeDescription localThemeDescription30 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outPreviewInstantText");
    ThemeDescription localThemeDescription31 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inPreviewInstantSelectedText");
    ThemeDescription localThemeDescription32 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outPreviewInstantSelectedText");
    localObject1 = this.chatListView;
    Object localObject46 = Theme.chat_deleteProgressPaint;
    localObject46 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject46, null, null, "chat_secretTimeText");
    ThemeDescription localThemeDescription33 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_stickerNameText");
    localObject1 = this.chatListView;
    Object localObject47 = Theme.chat_botButtonPaint;
    localObject47 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject47, null, null, "chat_botButtonText");
    localObject1 = this.chatListView;
    Object localObject48 = Theme.chat_botProgressPaint;
    localObject48 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject48, null, null, "chat_botProgress");
    ThemeDescription localThemeDescription34 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inForwardedNameText");
    ThemeDescription localThemeDescription35 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outForwardedNameText");
    ThemeDescription localThemeDescription36 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inViaBotNameText");
    ThemeDescription localThemeDescription37 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outViaBotNameText");
    ThemeDescription localThemeDescription38 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_stickerViaBotNameText");
    ThemeDescription localThemeDescription39 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inReplyLine");
    ThemeDescription localThemeDescription40 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outReplyLine");
    ThemeDescription localThemeDescription41 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_stickerReplyLine");
    ThemeDescription localThemeDescription42 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inReplyNameText");
    ThemeDescription localThemeDescription43 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outReplyNameText");
    ThemeDescription localThemeDescription44 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_stickerReplyNameText");
    ThemeDescription localThemeDescription45 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inReplyMessageText");
    ThemeDescription localThemeDescription46 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outReplyMessageText");
    ThemeDescription localThemeDescription47 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inReplyMediaMessageText");
    ThemeDescription localThemeDescription48 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outReplyMediaMessageText");
    ThemeDescription localThemeDescription49 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inReplyMediaMessageSelectedText");
    ThemeDescription localThemeDescription50 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outReplyMediaMessageSelectedText");
    ThemeDescription localThemeDescription51 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_stickerReplyMessageText");
    ThemeDescription localThemeDescription52 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inPreviewLine");
    ThemeDescription localThemeDescription53 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outPreviewLine");
    ThemeDescription localThemeDescription54 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inSiteNameText");
    ThemeDescription localThemeDescription55 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outSiteNameText");
    ThemeDescription localThemeDescription56 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inContactNameText");
    ThemeDescription localThemeDescription57 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outContactNameText");
    ThemeDescription localThemeDescription58 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inContactPhoneText");
    ThemeDescription localThemeDescription59 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outContactPhoneText");
    ThemeDescription localThemeDescription60 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_mediaProgress");
    ThemeDescription localThemeDescription61 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioProgress");
    ThemeDescription localThemeDescription62 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioProgress");
    ThemeDescription localThemeDescription63 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioSelectedProgress");
    ThemeDescription localThemeDescription64 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioSelectedProgress");
    ThemeDescription localThemeDescription65 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_mediaTimeText");
    ThemeDescription localThemeDescription66 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inTimeText");
    ThemeDescription localThemeDescription67 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outTimeText");
    ThemeDescription localThemeDescription68 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inTimeSelectedText");
    ThemeDescription localThemeDescription69 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outTimeSelectedText");
    ThemeDescription localThemeDescription70 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioPerfomerText");
    ThemeDescription localThemeDescription71 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioPerfomerText");
    ThemeDescription localThemeDescription72 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioTitleText");
    ThemeDescription localThemeDescription73 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioTitleText");
    ThemeDescription localThemeDescription74 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioDurationText");
    ThemeDescription localThemeDescription75 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioDurationText");
    ThemeDescription localThemeDescription76 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioDurationSelectedText");
    ThemeDescription localThemeDescription77 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioDurationSelectedText");
    ThemeDescription localThemeDescription78 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioSeekbar");
    ThemeDescription localThemeDescription79 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioSeekbar");
    ThemeDescription localThemeDescription80 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioSeekbarSelected");
    ThemeDescription localThemeDescription81 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioSeekbarSelected");
    ThemeDescription localThemeDescription82 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioSeekbarFill");
    ThemeDescription localThemeDescription83 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inAudioCacheSeekbar");
    ThemeDescription localThemeDescription84 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioSeekbarFill");
    ThemeDescription localThemeDescription85 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outAudioCacheSeekbar");
    ThemeDescription localThemeDescription86 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVoiceSeekbar");
    ThemeDescription localThemeDescription87 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVoiceSeekbar");
    ThemeDescription localThemeDescription88 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVoiceSeekbarSelected");
    ThemeDescription localThemeDescription89 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVoiceSeekbarSelected");
    ThemeDescription localThemeDescription90 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVoiceSeekbarFill");
    ThemeDescription localThemeDescription91 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVoiceSeekbarFill");
    ThemeDescription localThemeDescription92 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileProgress");
    ThemeDescription localThemeDescription93 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileProgress");
    ThemeDescription localThemeDescription94 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileProgressSelected");
    ThemeDescription localThemeDescription95 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileProgressSelected");
    ThemeDescription localThemeDescription96 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileNameText");
    ThemeDescription localThemeDescription97 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileNameText");
    ThemeDescription localThemeDescription98 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileInfoText");
    ThemeDescription localThemeDescription99 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileInfoText");
    ThemeDescription localThemeDescription100 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileInfoSelectedText");
    ThemeDescription localThemeDescription101 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileInfoSelectedText");
    ThemeDescription localThemeDescription102 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileBackground");
    ThemeDescription localThemeDescription103 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileBackground");
    ThemeDescription localThemeDescription104 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inFileBackgroundSelected");
    ThemeDescription localThemeDescription105 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outFileBackgroundSelected");
    ThemeDescription localThemeDescription106 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVenueNameText");
    ThemeDescription localThemeDescription107 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVenueNameText");
    ThemeDescription localThemeDescription108 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVenueInfoText");
    ThemeDescription localThemeDescription109 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVenueInfoText");
    ThemeDescription localThemeDescription110 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_inVenueInfoSelectedText");
    ThemeDescription localThemeDescription111 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_outVenueInfoSelectedText");
    ThemeDescription localThemeDescription112 = new ThemeDescription(this.chatListView, 0, new Class[] { ChatMessageCell.class }, null, null, null, "chat_mediaInfoText");
    localObject1 = this.chatListView;
    Object localObject49 = Theme.chat_urlPaint;
    localObject49 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject49, null, null, "chat_linkSelectBackground");
    localObject1 = this.chatListView;
    Object localObject50 = Theme.chat_textSearchSelectionPaint;
    localObject50 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, (Paint)localObject50, null, null, "chat_textSelectBackground");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    Object localObject51 = Theme.chat_fileStatesDrawable[0][0];
    Object localObject52 = Theme.chat_fileStatesDrawable[1][0];
    Object localObject53 = Theme.chat_fileStatesDrawable[2][0];
    Object localObject54 = Theme.chat_fileStatesDrawable[3][0];
    Object localObject55 = Theme.chat_fileStatesDrawable[4][0];
    localObject51 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject51, localObject52, localObject53, localObject54, localObject55 }, null, "chat_outLoader");
    localObject1 = this.chatListView;
    localObject52 = Theme.chat_fileStatesDrawable[0][0];
    localObject53 = Theme.chat_fileStatesDrawable[1][0];
    localObject54 = Theme.chat_fileStatesDrawable[2][0];
    localObject55 = Theme.chat_fileStatesDrawable[3][0];
    Object localObject56 = Theme.chat_fileStatesDrawable[4][0];
    localObject52 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject52, localObject53, localObject54, localObject55, localObject56 }, null, "chat_outBubble");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject53 = Theme.chat_fileStatesDrawable[0][1];
    localObject54 = Theme.chat_fileStatesDrawable[1][1];
    localObject55 = Theme.chat_fileStatesDrawable[2][1];
    localObject56 = Theme.chat_fileStatesDrawable[3][1];
    Object localObject57 = Theme.chat_fileStatesDrawable[4][1];
    localObject53 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject53, localObject54, localObject55, localObject56, localObject57 }, null, "chat_outLoaderSelected");
    localObject1 = this.chatListView;
    localObject54 = Theme.chat_fileStatesDrawable[0][1];
    localObject55 = Theme.chat_fileStatesDrawable[1][1];
    localObject56 = Theme.chat_fileStatesDrawable[2][1];
    localObject57 = Theme.chat_fileStatesDrawable[3][1];
    Object localObject58 = Theme.chat_fileStatesDrawable[4][1];
    localObject54 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject54, localObject55, localObject56, localObject57, localObject58 }, null, "chat_outBubbleSelected");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject55 = Theme.chat_fileStatesDrawable[5][0];
    localObject56 = Theme.chat_fileStatesDrawable[6][0];
    localObject57 = Theme.chat_fileStatesDrawable[7][0];
    localObject58 = Theme.chat_fileStatesDrawable[8][0];
    Object localObject59 = Theme.chat_fileStatesDrawable[9][0];
    localObject55 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject55, localObject56, localObject57, localObject58, localObject59 }, null, "chat_inLoader");
    localObject1 = this.chatListView;
    localObject56 = Theme.chat_fileStatesDrawable[5][0];
    localObject57 = Theme.chat_fileStatesDrawable[6][0];
    localObject58 = Theme.chat_fileStatesDrawable[7][0];
    localObject59 = Theme.chat_fileStatesDrawable[8][0];
    Object localObject60 = Theme.chat_fileStatesDrawable[9][0];
    localObject56 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject56, localObject57, localObject58, localObject59, localObject60 }, null, "chat_inBubble");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject57 = Theme.chat_fileStatesDrawable[5][1];
    localObject58 = Theme.chat_fileStatesDrawable[6][1];
    localObject59 = Theme.chat_fileStatesDrawable[7][1];
    localObject60 = Theme.chat_fileStatesDrawable[8][1];
    Object localObject61 = Theme.chat_fileStatesDrawable[9][1];
    localObject57 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject57, localObject58, localObject59, localObject60, localObject61 }, null, "chat_inLoaderSelected");
    localObject1 = this.chatListView;
    localObject58 = Theme.chat_fileStatesDrawable[5][1];
    localObject59 = Theme.chat_fileStatesDrawable[6][1];
    localObject60 = Theme.chat_fileStatesDrawable[7][1];
    localObject61 = Theme.chat_fileStatesDrawable[8][1];
    Object localObject62 = Theme.chat_fileStatesDrawable[9][1];
    localObject58 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject58, localObject59, localObject60, localObject61, localObject62 }, null, "chat_inBubbleSelected");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject59 = Theme.chat_photoStatesDrawables[0][0];
    localObject60 = Theme.chat_photoStatesDrawables[1][0];
    localObject61 = Theme.chat_photoStatesDrawables[2][0];
    localObject62 = Theme.chat_photoStatesDrawables[3][0];
    localObject59 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject59, localObject60, localObject61, localObject62 }, null, "chat_mediaLoaderPhoto");
    localObject1 = this.chatListView;
    localObject60 = Theme.chat_photoStatesDrawables[0][0];
    localObject61 = Theme.chat_photoStatesDrawables[1][0];
    localObject62 = Theme.chat_photoStatesDrawables[2][0];
    Object localObject63 = Theme.chat_photoStatesDrawables[3][0];
    localObject60 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject60, localObject61, localObject62, localObject63 }, null, "chat_mediaLoaderPhotoIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject61 = Theme.chat_photoStatesDrawables[0][1];
    localObject62 = Theme.chat_photoStatesDrawables[1][1];
    localObject63 = Theme.chat_photoStatesDrawables[2][1];
    Object localObject64 = Theme.chat_photoStatesDrawables[3][1];
    localObject61 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject61, localObject62, localObject63, localObject64 }, null, "chat_mediaLoaderPhotoSelected");
    localObject1 = this.chatListView;
    localObject62 = Theme.chat_photoStatesDrawables[0][1];
    localObject63 = Theme.chat_photoStatesDrawables[1][1];
    localObject64 = Theme.chat_photoStatesDrawables[2][1];
    Object localObject65 = Theme.chat_photoStatesDrawables[3][1];
    localObject62 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject62, localObject63, localObject64, localObject65 }, null, "chat_mediaLoaderPhotoIconSelected");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject63 = Theme.chat_photoStatesDrawables[7][0];
    localObject64 = Theme.chat_photoStatesDrawables[8][0];
    localObject63 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject63, localObject64 }, null, "chat_outLoaderPhoto");
    localObject1 = this.chatListView;
    localObject64 = Theme.chat_photoStatesDrawables[7][0];
    localObject65 = Theme.chat_photoStatesDrawables[8][0];
    localObject64 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject64, localObject65 }, null, "chat_outLoaderPhotoIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject65 = Theme.chat_photoStatesDrawables[7][1];
    Object localObject66 = Theme.chat_photoStatesDrawables[8][1];
    localObject65 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject65, localObject66 }, null, "chat_outLoaderPhotoSelected");
    localObject1 = this.chatListView;
    localObject66 = Theme.chat_photoStatesDrawables[7][1];
    Object localObject67 = Theme.chat_photoStatesDrawables[8][1];
    localObject66 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject66, localObject67 }, null, "chat_outLoaderPhotoIconSelected");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject67 = Theme.chat_photoStatesDrawables[10][0];
    Object localObject68 = Theme.chat_photoStatesDrawables[11][0];
    localObject67 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject67, localObject68 }, null, "chat_inLoaderPhoto");
    localObject1 = this.chatListView;
    localObject68 = Theme.chat_photoStatesDrawables[10][0];
    Object localObject69 = Theme.chat_photoStatesDrawables[11][0];
    localObject68 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject68, localObject69 }, null, "chat_inLoaderPhotoIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    localObject69 = Theme.chat_photoStatesDrawables[10][1];
    Object localObject70 = Theme.chat_photoStatesDrawables[11][1];
    localObject69 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject69, localObject70 }, null, "chat_inLoaderPhotoSelected");
    localObject1 = this.chatListView;
    localObject70 = Theme.chat_photoStatesDrawables[10][1];
    Object localObject71 = Theme.chat_photoStatesDrawables[11][1];
    localObject70 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject70, localObject71 }, null, "chat_inLoaderPhotoIconSelected");
    localObject1 = this.chatListView;
    localObject71 = Theme.chat_photoStatesDrawables[9][0];
    localObject71 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject71 }, null, "chat_outFileIcon");
    localObject1 = this.chatListView;
    Object localObject72 = Theme.chat_photoStatesDrawables[9][1];
    localObject72 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject72 }, null, "chat_outFileSelectedIcon");
    localObject1 = this.chatListView;
    Object localObject73 = Theme.chat_photoStatesDrawables[12][0];
    localObject73 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject73 }, null, "chat_inFileIcon");
    localObject1 = this.chatListView;
    Object localObject74 = Theme.chat_photoStatesDrawables[12][1];
    localObject74 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject74 }, null, "chat_inFileSelectedIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    Object localObject75 = Theme.chat_contactDrawable[0];
    localObject75 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject75 }, null, "chat_inContactBackground");
    localObject1 = this.chatListView;
    Object localObject76 = Theme.chat_contactDrawable[0];
    localObject76 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject76 }, null, "chat_inContactIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    Object localObject77 = Theme.chat_contactDrawable[1];
    localObject77 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject77 }, null, "chat_outContactBackground");
    localObject1 = this.chatListView;
    Object localObject78 = Theme.chat_contactDrawable[1];
    localObject78 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject78 }, null, "chat_outContactIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    Object localObject79 = Theme.chat_locationDrawable[0];
    localObject79 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject79 }, null, "chat_inLocationBackground");
    localObject1 = this.chatListView;
    Object localObject80 = Theme.chat_locationDrawable[0];
    localObject80 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject80 }, null, "chat_inLocationIcon");
    localObject1 = this.chatListView;
    i = ThemeDescription.FLAG_BACKGROUNDFILTER;
    Object localObject81 = Theme.chat_locationDrawable[1];
    localObject81 = new ThemeDescription((View)localObject1, i, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject81 }, null, "chat_outLocationBackground");
    localObject1 = this.chatListView;
    Object localObject82 = Theme.chat_locationDrawable[1];
    localObject82 = new ThemeDescription((View)localObject1, 0, new Class[] { ChatMessageCell.class }, null, new Drawable[] { localObject82 }, null, "chat_outLocationIcon");
    ThemeDescription localThemeDescription113 = new ThemeDescription(this.bottomOverlayChat, 0, null, Theme.chat_composeBackgroundPaint, null, null, "chat_messagePanelBackground");
    ThemeDescription localThemeDescription114 = new ThemeDescription(this.bottomOverlayChat, 0, null, null, new Drawable[] { Theme.chat_composeShadowDrawable }, null, "chat_messagePanelShadow");
    ThemeDescription localThemeDescription115 = new ThemeDescription(this.bottomOverlayChatText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "chat_fieldOverlayText");
    ThemeDescription localThemeDescription116 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "chat_serviceText");
    ThemeDescription localThemeDescription117 = new ThemeDescription(this.progressBar, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "chat_serviceText");
    ThemeDescription localThemeDescription118 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { ChatUnreadCell.class }, new String[] { "backgroundLayout" }, null, null, null, "chat_unreadMessagesStartBackground");
    ThemeDescription localThemeDescription119 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { ChatUnreadCell.class }, new String[] { "imageView" }, null, null, null, "chat_unreadMessagesStartArrowIcon");
    ThemeDescription localThemeDescription120 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { ChatUnreadCell.class }, new String[] { "textView" }, null, null, null, "chat_unreadMessagesStartText");
    ThemeDescription localThemeDescription121 = new ThemeDescription(this.progressView2, ThemeDescription.FLAG_SERVICEBACKGROUND, null, null, null, null, "chat_serviceBackground");
    ThemeDescription localThemeDescription122 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_SERVICEBACKGROUND, null, null, null, null, "chat_serviceBackground");
    ThemeDescription localThemeDescription123 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_SERVICEBACKGROUND, new Class[] { ChatLoadingCell.class }, new String[] { "textView" }, null, null, null, "chat_serviceBackground");
    ThemeDescription localThemeDescription124 = new ThemeDescription(this.chatListView, ThemeDescription.FLAG_PROGRESSBAR, new Class[] { ChatLoadingCell.class }, new String[] { "textView" }, null, null, null, "chat_serviceText");
    ThemeDescription localThemeDescription125;
    if (this.avatarContainer != null)
    {
      localObject1 = this.avatarContainer.getTimeItem();
      localThemeDescription125 = new ThemeDescription((View)localObject1, 0, null, null, null, null, "chat_secretTimerBackground");
      if (this.avatarContainer == null) {
        break label9722;
      }
    }
    label9722:
    for (localObject1 = this.avatarContainer.getTimeItem();; localObject1 = null)
    {
      return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localObject2, localObject3, localObject4, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, localThemeDescription24, localThemeDescription25, localObject5, localObject6, localObject7, localObject8, localObject9, localObject10, localObject11, localObject12, localObject13, localObject14, localObject15, localObject16, localThemeDescription26, localThemeDescription27, localThemeDescription28, localObject17, localObject18, localObject19, localObject20, localObject21, localObject22, localObject23, localObject24, localObject25, localObject26, localObject27, localObject28, localObject29, localObject30, localObject31, localObject32, localObject33, localObject34, localObject35, localObject36, localObject37, localObject38, localObject39, localObject40, localObject41, localObject42, localObject43, localObject44, localObject45, localThemeDescription29, localThemeDescription30, localThemeDescription31, localThemeDescription32, localObject46, localThemeDescription33, localObject47, localObject48, localThemeDescription34, localThemeDescription35, localThemeDescription36, localThemeDescription37, localThemeDescription38, localThemeDescription39, localThemeDescription40, localThemeDescription41, localThemeDescription42, localThemeDescription43, localThemeDescription44, localThemeDescription45, localThemeDescription46, localThemeDescription47, localThemeDescription48, localThemeDescription49, localThemeDescription50, localThemeDescription51, localThemeDescription52, localThemeDescription53, localThemeDescription54, localThemeDescription55, localThemeDescription56, localThemeDescription57, localThemeDescription58, localThemeDescription59, localThemeDescription60, localThemeDescription61, localThemeDescription62, localThemeDescription63, localThemeDescription64, localThemeDescription65, localThemeDescription66, localThemeDescription67, localThemeDescription68, localThemeDescription69, localThemeDescription70, localThemeDescription71, localThemeDescription72, localThemeDescription73, localThemeDescription74, localThemeDescription75, localThemeDescription76, localThemeDescription77, localThemeDescription78, localThemeDescription79, localThemeDescription80, localThemeDescription81, localThemeDescription82, localThemeDescription83, localThemeDescription84, localThemeDescription85, localThemeDescription86, localThemeDescription87, localThemeDescription88, localThemeDescription89, localThemeDescription90, localThemeDescription91, localThemeDescription92, localThemeDescription93, localThemeDescription94, localThemeDescription95, localThemeDescription96, localThemeDescription97, localThemeDescription98, localThemeDescription99, localThemeDescription100, localThemeDescription101, localThemeDescription102, localThemeDescription103, localThemeDescription104, localThemeDescription105, localThemeDescription106, localThemeDescription107, localThemeDescription108, localThemeDescription109, localThemeDescription110, localThemeDescription111, localThemeDescription112, localObject49, localObject50, localObject51, localObject52, localObject53, localObject54, localObject55, localObject56, localObject57, localObject58, localObject59, localObject60, localObject61, localObject62, localObject63, localObject64, localObject65, localObject66, localObject67, localObject68, localObject69, localObject70, localObject71, localObject72, localObject73, localObject74, localObject75, localObject76, localObject77, localObject78, localObject79, localObject80, localObject81, localObject82, localThemeDescription113, localThemeDescription114, localThemeDescription115, localThemeDescription116, localThemeDescription117, localThemeDescription118, localThemeDescription119, localThemeDescription120, localThemeDescription121, localThemeDescription122, localThemeDescription123, localThemeDescription124, localThemeDescription125, new ThemeDescription((View)localObject1, 0, null, null, null, null, "chat_secretTimerText") };
      localObject1 = null;
      break;
    }
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    fixLayout();
    if ((this.visibleDialog instanceof DatePickerDialog)) {
      this.visibleDialog.dismiss();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidStarted);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewWallpapper);
    loadMessages(true);
    loadAdmins();
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidStarted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
  }
  
  public void onPause()
  {
    super.onPause();
    this.paused = true;
    this.wasPaused = true;
  }
  
  protected void onRemoveFromParent()
  {
    MediaController.getInstance().setTextureView(this.videoTextureView, null, null, false);
  }
  
  public void onResume()
  {
    super.onResume();
    this.paused = false;
    checkScrollForLoad(false);
    if (this.wasPaused)
    {
      this.wasPaused = false;
      if (this.chatAdapter != null) {
        this.chatAdapter.notifyDataSetChanged();
      }
    }
    fixLayout();
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(false);
    if (paramBoolean1) {
      this.openAnimationEnded = true;
    }
  }
  
  public void onTransitionAnimationStart(boolean paramBoolean1, boolean paramBoolean2)
  {
    NotificationCenter.getInstance(this.currentAccount).setAllowedNotificationsDutingAnimation(new int[] { NotificationCenter.chatInfoDidLoaded, NotificationCenter.dialogsNeedReload, NotificationCenter.closeChats, NotificationCenter.messagesDidLoaded, NotificationCenter.botKeyboardDidLoaded });
    NotificationCenter.getInstance(this.currentAccount).setAnimationInProgress(true);
    if (paramBoolean1) {
      this.openAnimationEnded = false;
    }
  }
  
  public void showOpenUrlAlert(final String paramString, boolean paramBoolean)
  {
    if ((Browser.isInternalUrl(paramString, null)) || (!paramBoolean))
    {
      Browser.openUrl(getParentActivity(), paramString, true);
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    localBuilder.setMessage(LocaleController.formatString("OpenUrlAlert", 2131494046, new Object[] { paramString }));
    localBuilder.setPositiveButton(LocaleController.getString("Open", 2131494040), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        Browser.openUrl(ChannelAdminLogActivity.this.getParentActivity(), paramString, true);
      }
    });
    localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
    showDialog(localBuilder.create());
  }
  
  public class ChatActivityAdapter
    extends RecyclerView.Adapter
  {
    private int loadingUpRow;
    private Context mContext;
    private int messagesEndRow;
    private int messagesStartRow;
    private int rowCount;
    
    public ChatActivityAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      return this.rowCount;
    }
    
    public long getItemId(int paramInt)
    {
      return -1L;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt >= this.messagesStartRow) && (paramInt < this.messagesEndRow)) {
        return ((MessageObject)ChannelAdminLogActivity.this.messages.get(ChannelAdminLogActivity.this.messages.size() - (paramInt - this.messagesStartRow) - 1)).contentType;
      }
      return 4;
    }
    
    public void notifyDataSetChanged()
    {
      updateRows();
      try
      {
        super.notifyDataSetChanged();
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemChanged(int paramInt)
    {
      updateRows();
      try
      {
        super.notifyItemChanged(paramInt);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemInserted(int paramInt)
    {
      updateRows();
      try
      {
        super.notifyItemInserted(paramInt);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemMoved(int paramInt1, int paramInt2)
    {
      updateRows();
      try
      {
        super.notifyItemMoved(paramInt1, paramInt2);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemRangeChanged(int paramInt1, int paramInt2)
    {
      updateRows();
      try
      {
        super.notifyItemRangeChanged(paramInt1, paramInt2);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemRangeInserted(int paramInt1, int paramInt2)
    {
      updateRows();
      try
      {
        super.notifyItemRangeInserted(paramInt1, paramInt2);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemRangeRemoved(int paramInt1, int paramInt2)
    {
      updateRows();
      try
      {
        super.notifyItemRangeRemoved(paramInt1, paramInt2);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void notifyItemRemoved(int paramInt)
    {
      updateRows();
      try
      {
        super.notifyItemRemoved(paramInt);
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool1;
      if (paramInt == this.loadingUpRow)
      {
        paramViewHolder = (ChatLoadingCell)paramViewHolder.itemView;
        if (ChannelAdminLogActivity.this.loadsCount > 1)
        {
          bool1 = true;
          paramViewHolder.setProgressVisible(bool1);
        }
      }
      MessageObject localMessageObject1;
      View localView;
      label438:
      label444:
      do
      {
        do
        {
          return;
          bool1 = false;
          break;
        } while ((paramInt < this.messagesStartRow) || (paramInt >= this.messagesEndRow));
        localMessageObject1 = (MessageObject)ChannelAdminLogActivity.this.messages.get(ChannelAdminLogActivity.this.messages.size() - (paramInt - this.messagesStartRow) - 1);
        localView = paramViewHolder.itemView;
        if ((localView instanceof ChatMessageCell))
        {
          ChatMessageCell localChatMessageCell = (ChatMessageCell)localView;
          localChatMessageCell.isChat = true;
          int i = getItemViewType(paramInt + 1);
          int j = getItemViewType(paramInt - 1);
          boolean bool2;
          if ((!(localMessageObject1.messageOwner.reply_markup instanceof TLRPC.TL_replyInlineMarkup)) && (i == paramViewHolder.getItemViewType()))
          {
            MessageObject localMessageObject2 = (MessageObject)ChannelAdminLogActivity.this.messages.get(ChannelAdminLogActivity.this.messages.size() - (paramInt + 1 - this.messagesStartRow) - 1);
            if ((localMessageObject2.isOutOwner() == localMessageObject1.isOutOwner()) && (localMessageObject2.messageOwner.from_id == localMessageObject1.messageOwner.from_id) && (Math.abs(localMessageObject2.messageOwner.date - localMessageObject1.messageOwner.date) <= 300))
            {
              bool1 = true;
              if (j != paramViewHolder.getItemViewType()) {
                break label444;
              }
              paramViewHolder = (MessageObject)ChannelAdminLogActivity.this.messages.get(ChannelAdminLogActivity.this.messages.size() - (paramInt - this.messagesStartRow));
              if (((paramViewHolder.messageOwner.reply_markup instanceof TLRPC.TL_replyInlineMarkup)) || (paramViewHolder.isOutOwner() != localMessageObject1.isOutOwner()) || (paramViewHolder.messageOwner.from_id != localMessageObject1.messageOwner.from_id) || (Math.abs(paramViewHolder.messageOwner.date - localMessageObject1.messageOwner.date) > 300)) {
                break label438;
              }
              bool2 = true;
            }
          }
          for (;;)
          {
            localChatMessageCell.setMessageObject(localMessageObject1, null, bool1, bool2);
            if (((localView instanceof ChatMessageCell)) && (DownloadController.getInstance(ChannelAdminLogActivity.this.currentAccount).canDownloadMedia(localMessageObject1))) {
              ((ChatMessageCell)localView).downloadAudioIfNeed();
            }
            localChatMessageCell.setHighlighted(false);
            localChatMessageCell.setHighlightedText(null);
            return;
            bool1 = false;
            break;
            bool1 = false;
            break;
            bool2 = false;
            continue;
            bool2 = false;
          }
        }
      } while (!(localView instanceof ChatActionCell));
      paramViewHolder = (ChatActionCell)localView;
      paramViewHolder.setMessageObject(localMessageObject1);
      paramViewHolder.setAlpha(1.0F);
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      if (paramInt == 0) {
        if (!ChannelAdminLogActivity.this.chatMessageCellsCache.isEmpty())
        {
          paramViewGroup = (View)ChannelAdminLogActivity.this.chatMessageCellsCache.get(0);
          ChannelAdminLogActivity.this.chatMessageCellsCache.remove(0);
          ChatMessageCell localChatMessageCell = (ChatMessageCell)paramViewGroup;
          localChatMessageCell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate()
          {
            public boolean canPerformActions()
            {
              return true;
            }
            
            public void didLongPressed(ChatMessageCell paramAnonymousChatMessageCell)
            {
              ChannelAdminLogActivity.this.createMenu(paramAnonymousChatMessageCell);
            }
            
            public void didPressedBotButton(ChatMessageCell paramAnonymousChatMessageCell, TLRPC.KeyboardButton paramAnonymousKeyboardButton) {}
            
            public void didPressedCancelSendButton(ChatMessageCell paramAnonymousChatMessageCell) {}
            
            public void didPressedChannelAvatar(ChatMessageCell paramAnonymousChatMessageCell, TLRPC.Chat paramAnonymousChat, int paramAnonymousInt)
            {
              if ((paramAnonymousChat != null) && (paramAnonymousChat != ChannelAdminLogActivity.this.currentChat))
              {
                paramAnonymousChatMessageCell = new Bundle();
                paramAnonymousChatMessageCell.putInt("chat_id", paramAnonymousChat.id);
                if (paramAnonymousInt != 0) {
                  paramAnonymousChatMessageCell.putInt("message_id", paramAnonymousInt);
                }
                if (MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).checkCanOpenChat(paramAnonymousChatMessageCell, ChannelAdminLogActivity.this)) {
                  ChannelAdminLogActivity.this.presentFragment(new ChatActivity(paramAnonymousChatMessageCell), true);
                }
              }
            }
            
            public void didPressedImage(ChatMessageCell paramAnonymousChatMessageCell)
            {
              MessageObject localMessageObject = paramAnonymousChatMessageCell.getMessageObject();
              if (localMessageObject.type == 13) {
                ChannelAdminLogActivity.this.showDialog(new StickersAlert(ChannelAdminLogActivity.this.getParentActivity(), ChannelAdminLogActivity.this, localMessageObject.getInputStickerSet(), null, null));
              }
              Object localObject;
              do
              {
                do
                {
                  return;
                  if ((localMessageObject.isVideo()) || (localMessageObject.type == 1) || ((localMessageObject.type == 0) && (!localMessageObject.isWebpageDocument())) || (localMessageObject.isGif()))
                  {
                    PhotoViewer.getInstance().setParentActivity(ChannelAdminLogActivity.this.getParentActivity());
                    PhotoViewer.getInstance().openPhoto(localMessageObject, 0L, 0L, ChannelAdminLogActivity.this.provider);
                    return;
                  }
                  if (localMessageObject.type == 3)
                  {
                    localObject = null;
                    paramAnonymousChatMessageCell = (ChatMessageCell)localObject;
                    for (;;)
                    {
                      try
                      {
                        if (localMessageObject.messageOwner.attachPath != null)
                        {
                          paramAnonymousChatMessageCell = (ChatMessageCell)localObject;
                          if (localMessageObject.messageOwner.attachPath.length() != 0) {
                            paramAnonymousChatMessageCell = new File(localMessageObject.messageOwner.attachPath);
                          }
                        }
                        if (paramAnonymousChatMessageCell != null)
                        {
                          localObject = paramAnonymousChatMessageCell;
                          if (paramAnonymousChatMessageCell.exists()) {}
                        }
                        else
                        {
                          localObject = FileLoader.getPathToMessage(localMessageObject.messageOwner);
                        }
                        paramAnonymousChatMessageCell = new Intent("android.intent.action.VIEW");
                        if (Build.VERSION.SDK_INT >= 24)
                        {
                          paramAnonymousChatMessageCell.setFlags(1);
                          paramAnonymousChatMessageCell.setDataAndType(FileProvider.getUriForFile(ChannelAdminLogActivity.this.getParentActivity(), "org.telegram.messenger.provider", (File)localObject), "video/mp4");
                          ChannelAdminLogActivity.this.getParentActivity().startActivityForResult(paramAnonymousChatMessageCell, 500);
                          return;
                        }
                      }
                      catch (Exception paramAnonymousChatMessageCell)
                      {
                        ChannelAdminLogActivity.this.alertUserOpenError(localMessageObject);
                        return;
                      }
                      paramAnonymousChatMessageCell.setDataAndType(Uri.fromFile((File)localObject), "video/mp4");
                    }
                  }
                  if (localMessageObject.type != 4) {
                    break;
                  }
                } while (!AndroidUtilities.isGoogleMapsInstalled(ChannelAdminLogActivity.this));
                paramAnonymousChatMessageCell = new LocationActivity(0);
                paramAnonymousChatMessageCell.setMessageObject(localMessageObject);
                ChannelAdminLogActivity.this.presentFragment(paramAnonymousChatMessageCell);
                return;
              } while ((localMessageObject.type != 9) && (localMessageObject.type != 0));
              if (localMessageObject.getDocumentName().toLowerCase().endsWith("attheme"))
              {
                localObject = null;
                paramAnonymousChatMessageCell = (ChatMessageCell)localObject;
                File localFile;
                if (localMessageObject.messageOwner.attachPath != null)
                {
                  paramAnonymousChatMessageCell = (ChatMessageCell)localObject;
                  if (localMessageObject.messageOwner.attachPath.length() != 0)
                  {
                    localFile = new File(localMessageObject.messageOwner.attachPath);
                    paramAnonymousChatMessageCell = (ChatMessageCell)localObject;
                    if (localFile.exists()) {
                      paramAnonymousChatMessageCell = localFile;
                    }
                  }
                }
                localObject = paramAnonymousChatMessageCell;
                if (paramAnonymousChatMessageCell == null)
                {
                  localFile = FileLoader.getPathToMessage(localMessageObject.messageOwner);
                  localObject = paramAnonymousChatMessageCell;
                  if (localFile.exists()) {
                    localObject = localFile;
                  }
                }
                if (ChannelAdminLogActivity.this.chatLayoutManager != null)
                {
                  if (ChannelAdminLogActivity.this.chatLayoutManager.findLastVisibleItemPosition() >= ChannelAdminLogActivity.this.chatLayoutManager.getItemCount() - 1) {
                    break label645;
                  }
                  ChannelAdminLogActivity.access$7002(ChannelAdminLogActivity.this, ChannelAdminLogActivity.this.chatLayoutManager.findFirstVisibleItemPosition());
                  paramAnonymousChatMessageCell = (RecyclerListView.Holder)ChannelAdminLogActivity.this.chatListView.findViewHolderForAdapterPosition(ChannelAdminLogActivity.this.scrollToPositionOnRecreate);
                  if (paramAnonymousChatMessageCell == null) {
                    break label630;
                  }
                  ChannelAdminLogActivity.access$7102(ChannelAdminLogActivity.this, paramAnonymousChatMessageCell.itemView.getTop());
                }
                for (;;)
                {
                  paramAnonymousChatMessageCell = Theme.applyThemeFile((File)localObject, localMessageObject.getDocumentName(), true);
                  if (paramAnonymousChatMessageCell == null) {
                    break;
                  }
                  ChannelAdminLogActivity.this.presentFragment(new ThemePreviewActivity((File)localObject, paramAnonymousChatMessageCell));
                  return;
                  label630:
                  ChannelAdminLogActivity.access$7002(ChannelAdminLogActivity.this, -1);
                  continue;
                  label645:
                  ChannelAdminLogActivity.access$7002(ChannelAdminLogActivity.this, -1);
                }
                ChannelAdminLogActivity.access$7002(ChannelAdminLogActivity.this, -1);
              }
              try
              {
                AndroidUtilities.openForView(localMessageObject, ChannelAdminLogActivity.this.getParentActivity());
                return;
              }
              catch (Exception paramAnonymousChatMessageCell)
              {
                ChannelAdminLogActivity.this.alertUserOpenError(localMessageObject);
              }
            }
            
            public void didPressedInstantButton(ChatMessageCell paramAnonymousChatMessageCell, int paramAnonymousInt)
            {
              paramAnonymousChatMessageCell = paramAnonymousChatMessageCell.getMessageObject();
              if (paramAnonymousInt == 0)
              {
                if ((paramAnonymousChatMessageCell.messageOwner.media != null) && (paramAnonymousChatMessageCell.messageOwner.media.webpage != null) && (paramAnonymousChatMessageCell.messageOwner.media.webpage.cached_page != null))
                {
                  ArticleViewer.getInstance().setParentActivity(ChannelAdminLogActivity.this.getParentActivity(), ChannelAdminLogActivity.this);
                  ArticleViewer.getInstance().open(paramAnonymousChatMessageCell);
                }
                return;
              }
              Browser.openUrl(ChannelAdminLogActivity.this.getParentActivity(), paramAnonymousChatMessageCell.messageOwner.media.webpage.url);
            }
            
            public void didPressedOther(ChatMessageCell paramAnonymousChatMessageCell)
            {
              ChannelAdminLogActivity.this.createMenu(paramAnonymousChatMessageCell);
            }
            
            public void didPressedReplyMessage(ChatMessageCell paramAnonymousChatMessageCell, int paramAnonymousInt) {}
            
            public void didPressedShare(ChatMessageCell paramAnonymousChatMessageCell)
            {
              if (ChannelAdminLogActivity.this.getParentActivity() == null) {
                return;
              }
              ChannelAdminLogActivity localChannelAdminLogActivity = ChannelAdminLogActivity.this;
              Context localContext = ChannelAdminLogActivity.ChatActivityAdapter.this.mContext;
              paramAnonymousChatMessageCell = paramAnonymousChatMessageCell.getMessageObject();
              if ((ChatObject.isChannel(ChannelAdminLogActivity.this.currentChat)) && (!ChannelAdminLogActivity.this.currentChat.megagroup) && (ChannelAdminLogActivity.this.currentChat.username != null) && (ChannelAdminLogActivity.this.currentChat.username.length() > 0)) {}
              for (boolean bool = true;; bool = false)
              {
                localChannelAdminLogActivity.showDialog(ShareAlert.createShareAlert(localContext, paramAnonymousChatMessageCell, null, bool, null, false));
                return;
              }
            }
            
            public void didPressedUrl(MessageObject paramAnonymousMessageObject, CharacterStyle paramAnonymousCharacterStyle, boolean paramAnonymousBoolean)
            {
              if (paramAnonymousCharacterStyle == null) {}
              do
              {
                do
                {
                  do
                  {
                    return;
                    if ((paramAnonymousCharacterStyle instanceof URLSpanMono))
                    {
                      ((URLSpanMono)paramAnonymousCharacterStyle).copyToClipboard();
                      Toast.makeText(ChannelAdminLogActivity.this.getParentActivity(), LocaleController.getString("TextCopied", 2131494480), 0).show();
                      return;
                    }
                    if (!(paramAnonymousCharacterStyle instanceof URLSpanUserMention)) {
                      break;
                    }
                    paramAnonymousMessageObject = MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).getUser(Utilities.parseInt(((URLSpanUserMention)paramAnonymousCharacterStyle).getURL()));
                  } while (paramAnonymousMessageObject == null);
                  MessagesController.openChatOrProfileWith(paramAnonymousMessageObject, null, ChannelAdminLogActivity.this, 0, false);
                  return;
                  if (!(paramAnonymousCharacterStyle instanceof URLSpanNoUnderline)) {
                    break;
                  }
                  paramAnonymousMessageObject = ((URLSpanNoUnderline)paramAnonymousCharacterStyle).getURL();
                  if (paramAnonymousMessageObject.startsWith("@"))
                  {
                    MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).openByUserName(paramAnonymousMessageObject.substring(1), ChannelAdminLogActivity.this, 0);
                    return;
                  }
                } while (!paramAnonymousMessageObject.startsWith("#"));
                paramAnonymousCharacterStyle = new DialogsActivity(null);
                paramAnonymousCharacterStyle.setSearchString(paramAnonymousMessageObject);
                ChannelAdminLogActivity.this.presentFragment(paramAnonymousCharacterStyle);
                return;
                final Object localObject = ((URLSpan)paramAnonymousCharacterStyle).getURL();
                String str;
                if (paramAnonymousBoolean)
                {
                  paramAnonymousMessageObject = new BottomSheet.Builder(ChannelAdminLogActivity.this.getParentActivity());
                  paramAnonymousMessageObject.setTitle((CharSequence)localObject);
                  paramAnonymousCharacterStyle = LocaleController.getString("Open", 2131494040);
                  str = LocaleController.getString("Copy", 2131493303);
                  localObject = new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                    {
                      if (paramAnonymous2Int == 0) {
                        Browser.openUrl(ChannelAdminLogActivity.this.getParentActivity(), localObject, true);
                      }
                      while (paramAnonymous2Int != 1) {
                        return;
                      }
                      String str = localObject;
                      if (str.startsWith("mailto:")) {
                        paramAnonymous2DialogInterface = str.substring(7);
                      }
                      for (;;)
                      {
                        AndroidUtilities.addToClipboard(paramAnonymous2DialogInterface);
                        return;
                        paramAnonymous2DialogInterface = str;
                        if (str.startsWith("tel:")) {
                          paramAnonymous2DialogInterface = str.substring(4);
                        }
                      }
                    }
                  };
                  paramAnonymousMessageObject.setItems(new CharSequence[] { paramAnonymousCharacterStyle, str }, (DialogInterface.OnClickListener)localObject);
                  ChannelAdminLogActivity.this.showDialog(paramAnonymousMessageObject.create());
                  return;
                }
                if ((paramAnonymousCharacterStyle instanceof URLSpanReplacement))
                {
                  ChannelAdminLogActivity.this.showOpenUrlAlert(((URLSpanReplacement)paramAnonymousCharacterStyle).getURL(), true);
                  return;
                }
                if ((paramAnonymousCharacterStyle instanceof URLSpan))
                {
                  if (((paramAnonymousMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (paramAnonymousMessageObject.messageOwner.media.webpage != null) && (paramAnonymousMessageObject.messageOwner.media.webpage.cached_page != null))
                  {
                    paramAnonymousCharacterStyle = ((String)localObject).toLowerCase();
                    str = paramAnonymousMessageObject.messageOwner.media.webpage.url.toLowerCase();
                    if (((paramAnonymousCharacterStyle.contains("telegra.ph")) || (paramAnonymousCharacterStyle.contains("t.me/iv"))) && ((paramAnonymousCharacterStyle.contains(str)) || (str.contains(paramAnonymousCharacterStyle))))
                    {
                      ArticleViewer.getInstance().setParentActivity(ChannelAdminLogActivity.this.getParentActivity(), ChannelAdminLogActivity.this);
                      ArticleViewer.getInstance().open(paramAnonymousMessageObject);
                      return;
                    }
                  }
                  Browser.openUrl(ChannelAdminLogActivity.this.getParentActivity(), (String)localObject, true);
                  return;
                }
              } while (!(paramAnonymousCharacterStyle instanceof ClickableSpan));
              ((ClickableSpan)paramAnonymousCharacterStyle).onClick(ChannelAdminLogActivity.this.fragmentView);
            }
            
            public void didPressedUserAvatar(ChatMessageCell paramAnonymousChatMessageCell, TLRPC.User paramAnonymousUser)
            {
              if ((paramAnonymousUser != null) && (paramAnonymousUser.id != UserConfig.getInstance(ChannelAdminLogActivity.this.currentAccount).getClientUserId()))
              {
                paramAnonymousChatMessageCell = new Bundle();
                paramAnonymousChatMessageCell.putInt("user_id", paramAnonymousUser.id);
                ChannelAdminLogActivity.this.addCanBanUser(paramAnonymousChatMessageCell, paramAnonymousUser.id);
                paramAnonymousChatMessageCell = new ProfileActivity(paramAnonymousChatMessageCell);
                paramAnonymousChatMessageCell.setPlayProfileAnimation(false);
                ChannelAdminLogActivity.this.presentFragment(paramAnonymousChatMessageCell);
              }
            }
            
            public void didPressedViaBot(ChatMessageCell paramAnonymousChatMessageCell, String paramAnonymousString) {}
            
            public boolean isChatAdminCell(int paramAnonymousInt)
            {
              return false;
            }
            
            public void needOpenWebView(String paramAnonymousString1, String paramAnonymousString2, String paramAnonymousString3, String paramAnonymousString4, int paramAnonymousInt1, int paramAnonymousInt2)
            {
              EmbedBottomSheet.show(ChannelAdminLogActivity.ChatActivityAdapter.this.mContext, paramAnonymousString2, paramAnonymousString3, paramAnonymousString4, paramAnonymousString1, paramAnonymousInt1, paramAnonymousInt2);
            }
            
            public boolean needPlayMessage(MessageObject paramAnonymousMessageObject)
            {
              if ((paramAnonymousMessageObject.isVoice()) || (paramAnonymousMessageObject.isRoundVideo()))
              {
                boolean bool = MediaController.getInstance().playMessage(paramAnonymousMessageObject);
                MediaController.getInstance().setVoiceMessagesPlaylist(null, false);
                return bool;
              }
              if (paramAnonymousMessageObject.isMusic()) {
                return MediaController.getInstance().setPlaylist(ChannelAdminLogActivity.this.messages, paramAnonymousMessageObject);
              }
              return false;
            }
          });
          localChatMessageCell.setAllowAssistant(true);
        }
      }
      for (;;)
      {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new ChatMessageCell(this.mContext);
        break;
        if (paramInt == 1)
        {
          paramViewGroup = new ChatActionCell(this.mContext);
          ((ChatActionCell)paramViewGroup).setDelegate(new ChatActionCell.ChatActionCellDelegate()
          {
            public void didClickedImage(ChatActionCell paramAnonymousChatActionCell)
            {
              paramAnonymousChatActionCell = paramAnonymousChatActionCell.getMessageObject();
              PhotoViewer.getInstance().setParentActivity(ChannelAdminLogActivity.this.getParentActivity());
              TLRPC.PhotoSize localPhotoSize = FileLoader.getClosestPhotoSizeWithSize(paramAnonymousChatActionCell.photoThumbs, 640);
              if (localPhotoSize != null)
              {
                PhotoViewer.getInstance().openPhoto(localPhotoSize.location, ChannelAdminLogActivity.this.provider);
                return;
              }
              PhotoViewer.getInstance().openPhoto(paramAnonymousChatActionCell, 0L, 0L, ChannelAdminLogActivity.this.provider);
            }
            
            public void didLongPressed(ChatActionCell paramAnonymousChatActionCell)
            {
              ChannelAdminLogActivity.this.createMenu(paramAnonymousChatActionCell);
            }
            
            public void didPressedBotButton(MessageObject paramAnonymousMessageObject, TLRPC.KeyboardButton paramAnonymousKeyboardButton) {}
            
            public void didPressedReplyMessage(ChatActionCell paramAnonymousChatActionCell, int paramAnonymousInt) {}
            
            public void needOpenUserProfile(int paramAnonymousInt)
            {
              if (paramAnonymousInt < 0)
              {
                localObject = new Bundle();
                ((Bundle)localObject).putInt("chat_id", -paramAnonymousInt);
                if (MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).checkCanOpenChat((Bundle)localObject, ChannelAdminLogActivity.this)) {
                  ChannelAdminLogActivity.this.presentFragment(new ChatActivity((Bundle)localObject), true);
                }
              }
              while (paramAnonymousInt == UserConfig.getInstance(ChannelAdminLogActivity.this.currentAccount).getClientUserId()) {
                return;
              }
              Object localObject = new Bundle();
              ((Bundle)localObject).putInt("user_id", paramAnonymousInt);
              ChannelAdminLogActivity.this.addCanBanUser((Bundle)localObject, paramAnonymousInt);
              localObject = new ProfileActivity((Bundle)localObject);
              ((ProfileActivity)localObject).setPlayProfileAnimation(false);
              ChannelAdminLogActivity.this.presentFragment((BaseFragment)localObject);
            }
          });
        }
        else if (paramInt == 2)
        {
          paramViewGroup = new ChatUnreadCell(this.mContext);
        }
        else if (paramInt == 3)
        {
          paramViewGroup = new BotHelpCell(this.mContext);
          ((BotHelpCell)paramViewGroup).setDelegate(new BotHelpCell.BotHelpCellDelegate()
          {
            public void didPressUrl(String paramAnonymousString)
            {
              if (paramAnonymousString.startsWith("@")) {
                MessagesController.getInstance(ChannelAdminLogActivity.this.currentAccount).openByUserName(paramAnonymousString.substring(1), ChannelAdminLogActivity.this, 0);
              }
              while (!paramAnonymousString.startsWith("#")) {
                return;
              }
              DialogsActivity localDialogsActivity = new DialogsActivity(null);
              localDialogsActivity.setSearchString(paramAnonymousString);
              ChannelAdminLogActivity.this.presentFragment(localDialogsActivity);
            }
          });
        }
        else if (paramInt == 4)
        {
          paramViewGroup = new ChatLoadingCell(this.mContext);
        }
      }
    }
    
    public void onViewAttachedToWindow(final RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool2 = true;
      boolean bool1;
      if ((paramViewHolder.itemView instanceof ChatMessageCell))
      {
        paramViewHolder = (ChatMessageCell)paramViewHolder.itemView;
        paramViewHolder.getMessageObject();
        paramViewHolder.setBackgroundDrawable(null);
        if (0 != 0) {
          break label72;
        }
        bool1 = true;
        if ((0 == 0) || (0 == 0)) {
          break label77;
        }
      }
      for (;;)
      {
        paramViewHolder.setCheckPressed(bool1, bool2);
        paramViewHolder.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
          public boolean onPreDraw()
          {
            paramViewHolder.getViewTreeObserver().removeOnPreDrawListener(this);
            int m = ChannelAdminLogActivity.this.chatListView.getMeasuredHeight();
            int i = paramViewHolder.getTop();
            paramViewHolder.getBottom();
            if (i >= 0) {}
            for (i = 0;; i = -i)
            {
              int k = paramViewHolder.getMeasuredHeight();
              int j = k;
              if (k > m) {
                j = i + m;
              }
              paramViewHolder.setVisiblePart(i, j - i);
              return true;
            }
          }
        });
        paramViewHolder.setHighlighted(false);
        return;
        label72:
        bool1 = false;
        break;
        label77:
        bool2 = false;
      }
    }
    
    public void updateRowWithMessageObject(MessageObject paramMessageObject)
    {
      int i = ChannelAdminLogActivity.this.messages.indexOf(paramMessageObject);
      if (i == -1) {
        return;
      }
      notifyItemChanged(this.messagesStartRow + ChannelAdminLogActivity.this.messages.size() - i - 1);
    }
    
    public void updateRows()
    {
      this.rowCount = 0;
      if (!ChannelAdminLogActivity.this.messages.isEmpty())
      {
        int i;
        if (!ChannelAdminLogActivity.this.endReached)
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
        }
        for (this.loadingUpRow = i;; this.loadingUpRow = -1)
        {
          this.messagesStartRow = this.rowCount;
          this.rowCount += ChannelAdminLogActivity.this.messages.size();
          this.messagesEndRow = this.rowCount;
          return;
        }
      }
      this.loadingUpRow = -1;
      this.messagesStartRow = -1;
      this.messagesEndRow = -1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChannelAdminLogActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */