package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
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
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.GeoPoint;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonCallback;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatActivityEnterView.ChatActivityEnterViewDelegate;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PlayingGameDrawable;
import org.telegram.ui.Components.PopupAudioView;
import org.telegram.ui.Components.RecordStatusDrawable;
import org.telegram.ui.Components.RoundStatusDrawable;
import org.telegram.ui.Components.SendingFileDrawable;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StatusDrawable;
import org.telegram.ui.Components.TypingDotsDrawable;

public class PopupNotificationActivity
  extends Activity
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int id_chat_compose_panel = 1000;
  private ActionBar actionBar;
  private boolean animationInProgress = false;
  private long animationStartTime = 0L;
  private ArrayList<ViewGroup> audioViews = new ArrayList();
  private FrameLayout avatarContainer;
  private BackupImageView avatarImageView;
  private ViewGroup centerButtonsView;
  private ViewGroup centerView;
  private ChatActivityEnterView chatActivityEnterView;
  private int classGuid;
  private TextView countText;
  private TLRPC.Chat currentChat;
  private int currentMessageNum = 0;
  private MessageObject currentMessageObject = null;
  private TLRPC.User currentUser;
  private boolean finished = false;
  private ArrayList<ViewGroup> imageViews = new ArrayList();
  private boolean isReply;
  private CharSequence lastPrintString;
  private int lastResumedAccount = -1;
  private ViewGroup leftButtonsView;
  private ViewGroup leftView;
  private ViewGroup messageContainer;
  private float moveStartX = -1.0F;
  private TextView nameTextView;
  private Runnable onAnimationEndRunnable = null;
  private TextView onlineTextView;
  private RelativeLayout popupContainer;
  private ArrayList<MessageObject> popupMessages = new ArrayList();
  private ViewGroup rightButtonsView;
  private ViewGroup rightView;
  private boolean startedMoving = false;
  private StatusDrawable[] statusDrawables = new StatusDrawable[5];
  private ArrayList<ViewGroup> textViews = new ArrayList();
  private VelocityTracker velocityTracker = null;
  private PowerManager.WakeLock wakeLock = null;
  
  private void applyViewsLayoutParams(int paramInt)
  {
    int i = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0F);
    FrameLayout.LayoutParams localLayoutParams;
    if (this.leftView != null)
    {
      localLayoutParams = (FrameLayout.LayoutParams)this.leftView.getLayoutParams();
      if (localLayoutParams.width != i)
      {
        localLayoutParams.width = i;
        this.leftView.setLayoutParams(localLayoutParams);
      }
      this.leftView.setTranslationX(-i + paramInt);
    }
    if (this.leftButtonsView != null) {
      this.leftButtonsView.setTranslationX(-i + paramInt);
    }
    if (this.centerView != null)
    {
      localLayoutParams = (FrameLayout.LayoutParams)this.centerView.getLayoutParams();
      if (localLayoutParams.width != i)
      {
        localLayoutParams.width = i;
        this.centerView.setLayoutParams(localLayoutParams);
      }
      this.centerView.setTranslationX(paramInt);
    }
    if (this.centerButtonsView != null) {
      this.centerButtonsView.setTranslationX(paramInt);
    }
    if (this.rightView != null)
    {
      localLayoutParams = (FrameLayout.LayoutParams)this.rightView.getLayoutParams();
      if (localLayoutParams.width != i)
      {
        localLayoutParams.width = i;
        this.rightView.setLayoutParams(localLayoutParams);
      }
      this.rightView.setTranslationX(i + paramInt);
    }
    if (this.rightButtonsView != null) {
      this.rightButtonsView.setTranslationX(i + paramInt);
    }
    this.messageContainer.invalidate();
  }
  
  private void checkAndUpdateAvatar()
  {
    if (this.currentMessageObject == null) {}
    label189:
    for (;;)
    {
      return;
      Object localObject4 = null;
      Object localObject3 = null;
      Object localObject2 = null;
      Object localObject1 = null;
      if (this.currentChat != null)
      {
        localObject1 = MessagesController.getInstance(this.currentMessageObject.currentAccount).getChat(Integer.valueOf(this.currentChat.id));
        if (localObject1 != null)
        {
          this.currentChat = ((TLRPC.Chat)localObject1);
          if (this.currentChat.photo != null) {
            localObject2 = this.currentChat.photo.photo_small;
          }
          localObject1 = new AvatarDrawable(this.currentChat);
        }
      }
      else
      {
        for (;;)
        {
          if (this.avatarImageView == null) {
            break label189;
          }
          this.avatarImageView.setImage((TLObject)localObject2, "50_50", (Drawable)localObject1);
          return;
          localObject2 = localObject4;
          if (this.currentUser != null)
          {
            localObject1 = MessagesController.getInstance(this.currentMessageObject.currentAccount).getUser(Integer.valueOf(this.currentUser.id));
            if (localObject1 == null) {
              break;
            }
            this.currentUser = ((TLRPC.User)localObject1);
            localObject2 = localObject3;
            if (this.currentUser.photo != null) {
              localObject2 = this.currentUser.photo.photo_small;
            }
            localObject1 = new AvatarDrawable(this.currentUser);
          }
        }
      }
    }
  }
  
  private void fixLayout()
  {
    if (this.avatarContainer != null) {
      this.avatarContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          if (PopupNotificationActivity.this.avatarContainer != null) {
            PopupNotificationActivity.this.avatarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
          }
          int i = (ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(48.0F)) / 2;
          PopupNotificationActivity.this.avatarContainer.setPadding(PopupNotificationActivity.this.avatarContainer.getPaddingLeft(), i, PopupNotificationActivity.this.avatarContainer.getPaddingRight(), i);
          return true;
        }
      });
    }
    if (this.messageContainer != null) {
      this.messageContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          PopupNotificationActivity.this.messageContainer.getViewTreeObserver().removeOnPreDrawListener(this);
          if ((!PopupNotificationActivity.this.checkTransitionAnimation()) && (!PopupNotificationActivity.this.startedMoving))
          {
            ViewGroup.MarginLayoutParams localMarginLayoutParams = (ViewGroup.MarginLayoutParams)PopupNotificationActivity.this.messageContainer.getLayoutParams();
            localMarginLayoutParams.topMargin = ActionBar.getCurrentActionBarHeight();
            localMarginLayoutParams.bottomMargin = AndroidUtilities.dp(48.0F);
            localMarginLayoutParams.width = -1;
            localMarginLayoutParams.height = -1;
            PopupNotificationActivity.this.messageContainer.setLayoutParams(localMarginLayoutParams);
            PopupNotificationActivity.this.applyViewsLayoutParams(0);
          }
          return true;
        }
      });
    }
  }
  
  private LinearLayout getButtonsViewForMessage(int paramInt, boolean paramBoolean)
  {
    if ((this.popupMessages.size() == 1) && ((paramInt < 0) || (paramInt >= this.popupMessages.size()))) {
      localObject2 = null;
    }
    int i;
    Object localObject1;
    label231:
    do
    {
      return (LinearLayout)localObject2;
      final MessageObject localMessageObject;
      int k;
      Object localObject3;
      int j;
      Object localObject4;
      int n;
      Object localObject5;
      int i1;
      if (paramInt == -1)
      {
        i = this.popupMessages.size() - 1;
        localObject1 = null;
        localObject2 = null;
        localMessageObject = (MessageObject)this.popupMessages.get(i);
        k = 0;
        paramInt = 0;
        localObject3 = localMessageObject.messageOwner.reply_markup;
        j = k;
        if (localMessageObject.getDialogId() == 777000L)
        {
          j = k;
          if (localObject3 != null)
          {
            localObject4 = ((TLRPC.ReplyMarkup)localObject3).rows;
            k = 0;
            n = ((ArrayList)localObject4).size();
          }
        }
      }
      else
      {
        for (;;)
        {
          j = paramInt;
          if (k >= n) {
            break label231;
          }
          localObject5 = (TLRPC.TL_keyboardButtonRow)((ArrayList)localObject4).get(k);
          j = 0;
          i1 = ((TLRPC.TL_keyboardButtonRow)localObject5).buttons.size();
          for (;;)
          {
            if (j < i1)
            {
              m = paramInt;
              if (((TLRPC.KeyboardButton)((TLRPC.TL_keyboardButtonRow)localObject5).buttons.get(j) instanceof TLRPC.TL_keyboardButtonCallback)) {
                m = paramInt + 1;
              }
              j += 1;
              paramInt = m;
              continue;
              i = paramInt;
              if (paramInt != this.popupMessages.size()) {
                break;
              }
              i = 0;
              break;
            }
          }
          k += 1;
        }
      }
      final int m = localMessageObject.currentAccount;
      if (j > 0)
      {
        localObject3 = ((TLRPC.ReplyMarkup)localObject3).rows;
        paramInt = 0;
        n = ((ArrayList)localObject3).size();
        for (;;)
        {
          localObject1 = localObject2;
          if (paramInt >= n) {
            break;
          }
          localObject4 = (TLRPC.TL_keyboardButtonRow)((ArrayList)localObject3).get(paramInt);
          k = 0;
          i1 = ((TLRPC.TL_keyboardButtonRow)localObject4).buttons.size();
          for (localObject1 = localObject2; k < i1; localObject1 = localObject2)
          {
            localObject5 = (TLRPC.KeyboardButton)((TLRPC.TL_keyboardButtonRow)localObject4).buttons.get(k);
            localObject2 = localObject1;
            if ((localObject5 instanceof TLRPC.TL_keyboardButtonCallback))
            {
              localObject2 = localObject1;
              if (localObject1 == null)
              {
                localObject2 = new LinearLayout(this);
                ((LinearLayout)localObject2).setOrientation(0);
                ((LinearLayout)localObject2).setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
                ((LinearLayout)localObject2).setWeightSum(100.0F);
                ((LinearLayout)localObject2).setTag("b");
                ((LinearLayout)localObject2).setOnTouchListener(new View.OnTouchListener()
                {
                  public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
                  {
                    return true;
                  }
                });
              }
              localObject1 = new TextView(this);
              ((TextView)localObject1).setTextSize(1, 16.0F);
              ((TextView)localObject1).setTextColor(Theme.getColor("windowBackgroundWhiteBlueText"));
              ((TextView)localObject1).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
              ((TextView)localObject1).setText(((TLRPC.KeyboardButton)localObject5).text.toUpperCase());
              ((TextView)localObject1).setTag(localObject5);
              ((TextView)localObject1).setGravity(17);
              ((TextView)localObject1).setBackgroundDrawable(Theme.getSelectorDrawable(true));
              ((LinearLayout)localObject2).addView((View)localObject1, LayoutHelper.createLinear(-1, -1, 100.0F / j));
              ((TextView)localObject1).setOnClickListener(new View.OnClickListener()
              {
                public void onClick(View paramAnonymousView)
                {
                  paramAnonymousView = (TLRPC.KeyboardButton)paramAnonymousView.getTag();
                  if (paramAnonymousView != null) {
                    SendMessagesHelper.getInstance(m).sendNotificationCallback(localMessageObject.getDialogId(), localMessageObject.getId(), paramAnonymousView.data);
                  }
                }
              });
            }
            k += 1;
          }
          paramInt += 1;
          localObject2 = localObject1;
        }
      }
      localObject2 = localObject1;
    } while (localObject1 == null);
    paramInt = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0F);
    Object localObject2 = new RelativeLayout.LayoutParams(-1, -2);
    ((RelativeLayout.LayoutParams)localObject2).addRule(12);
    if (paramBoolean)
    {
      if (i != this.currentMessageNum) {
        break label606;
      }
      ((LinearLayout)localObject1).setTranslationX(0.0F);
    }
    for (;;)
    {
      this.popupContainer.addView((View)localObject1, (ViewGroup.LayoutParams)localObject2);
      return (LinearLayout)localObject1;
      label606:
      if (i == this.currentMessageNum - 1) {
        ((LinearLayout)localObject1).setTranslationX(-paramInt);
      } else if (i == this.currentMessageNum + 1) {
        ((LinearLayout)localObject1).setTranslationX(paramInt);
      }
    }
  }
  
  private void getNewMessage()
  {
    if (this.popupMessages.isEmpty())
    {
      onFinish();
      finish();
      return;
    }
    int k = 0;
    int j;
    int i;
    if ((this.currentMessageNum == 0) && (!this.chatActivityEnterView.hasText()))
    {
      j = k;
      if (!this.startedMoving) {}
    }
    else
    {
      j = k;
      if (this.currentMessageObject != null)
      {
        i = 0;
        int m = this.popupMessages.size();
        j = k;
        if (i < m)
        {
          MessageObject localMessageObject = (MessageObject)this.popupMessages.get(i);
          if ((localMessageObject.currentAccount != this.currentMessageObject.currentAccount) || (localMessageObject.getDialogId() != this.currentMessageObject.getDialogId()) || (localMessageObject.getId() != this.currentMessageObject.getId())) {
            break label213;
          }
          this.currentMessageNum = i;
          j = 1;
        }
      }
    }
    if (j == 0)
    {
      this.currentMessageNum = 0;
      this.currentMessageObject = ((MessageObject)this.popupMessages.get(0));
      updateInterfaceForCurrentMessage(0);
    }
    for (;;)
    {
      this.countText.setText(String.format("%d/%d", new Object[] { Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(this.popupMessages.size()) }));
      return;
      label213:
      i += 1;
      break;
      if (this.startedMoving) {
        if (this.currentMessageNum == this.popupMessages.size() - 1) {
          prepareLayouts(3);
        } else if (this.currentMessageNum == 1) {
          prepareLayouts(4);
        }
      }
    }
  }
  
  private ViewGroup getViewForMessage(int paramInt, boolean paramBoolean)
  {
    if ((this.popupMessages.size() == 1) && ((paramInt < 0) || (paramInt >= this.popupMessages.size()))) {
      localObject2 = null;
    }
    int i;
    MessageObject localMessageObject;
    label112:
    Object localObject3;
    BackupImageView localBackupImageView;
    Object localObject1;
    TLRPC.PhotoSize localPhotoSize;
    int j;
    label274:
    label315:
    do
    {
      return (ViewGroup)localObject2;
      if (paramInt != -1) {
        break;
      }
      i = this.popupMessages.size() - 1;
      localMessageObject = (MessageObject)this.popupMessages.get(i);
      if ((localMessageObject.type != 1) && (localMessageObject.type != 4)) {
        break label821;
      }
      if (this.imageViews.size() <= 0) {
        break label438;
      }
      localObject2 = (ViewGroup)this.imageViews.get(0);
      this.imageViews.remove(0);
      localObject3 = (TextView)((ViewGroup)localObject2).findViewWithTag(Integer.valueOf(312));
      localBackupImageView = (BackupImageView)((ViewGroup)localObject2).findViewWithTag(Integer.valueOf(311));
      localBackupImageView.setAspectFit(true);
      if (localMessageObject.type != 1) {
        break label679;
      }
      localObject1 = FileLoader.getClosestPhotoSizeWithSize(localMessageObject.photoThumbs, AndroidUtilities.getPhotoSize());
      localPhotoSize = FileLoader.getClosestPhotoSizeWithSize(localMessageObject.photoThumbs, 100);
      j = 0;
      paramInt = j;
      if (localObject1 != null)
      {
        int k = 1;
        paramInt = k;
        if (localMessageObject.type == 1)
        {
          paramInt = k;
          if (!FileLoader.getPathToMessage(localMessageObject.messageOwner).exists()) {
            paramInt = 0;
          }
        }
        if ((paramInt == 0) && (!DownloadController.getInstance(localMessageObject.currentAccount).canDownloadMedia(localMessageObject))) {
          break label631;
        }
        localBackupImageView.setImage(((TLRPC.PhotoSize)localObject1).location, "100_100", localPhotoSize.location, ((TLRPC.PhotoSize)localObject1).size);
        paramInt = 1;
      }
      if (paramInt != 0) {
        break label659;
      }
      localBackupImageView.setVisibility(8);
      ((TextView)localObject3).setVisibility(0);
      ((TextView)localObject3).setTextSize(2, SharedConfig.fontSize);
      ((TextView)localObject3).setText(localMessageObject.messageText);
      localObject1 = localObject2;
      if (((ViewGroup)localObject1).getParent() == null) {
        this.messageContainer.addView((View)localObject1);
      }
      ((ViewGroup)localObject1).setVisibility(0);
      localObject2 = localObject1;
    } while (!paramBoolean);
    paramInt = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0F);
    Object localObject2 = (FrameLayout.LayoutParams)((ViewGroup)localObject1).getLayoutParams();
    ((FrameLayout.LayoutParams)localObject2).gravity = 51;
    ((FrameLayout.LayoutParams)localObject2).height = -1;
    ((FrameLayout.LayoutParams)localObject2).width = paramInt;
    if (i == this.currentMessageNum) {
      ((ViewGroup)localObject1).setTranslationX(0.0F);
    }
    for (;;)
    {
      ((ViewGroup)localObject1).setLayoutParams((ViewGroup.LayoutParams)localObject2);
      ((ViewGroup)localObject1).invalidate();
      return (ViewGroup)localObject1;
      i = paramInt;
      if (paramInt != this.popupMessages.size()) {
        break;
      }
      i = 0;
      break;
      label438:
      localObject2 = new FrameLayout(this);
      localObject1 = new FrameLayout(this);
      ((FrameLayout)localObject1).setPadding(AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F));
      ((FrameLayout)localObject1).setBackgroundDrawable(Theme.getSelectorDrawable(false));
      ((ViewGroup)localObject2).addView((View)localObject1, LayoutHelper.createFrame(-1, -1.0F));
      localObject3 = new BackupImageView(this);
      ((BackupImageView)localObject3).setTag(Integer.valueOf(311));
      ((FrameLayout)localObject1).addView((View)localObject3, LayoutHelper.createFrame(-1, -1.0F));
      localObject3 = new TextView(this);
      ((TextView)localObject3).setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      ((TextView)localObject3).setTextSize(1, 16.0F);
      ((TextView)localObject3).setGravity(17);
      ((TextView)localObject3).setTag(Integer.valueOf(312));
      ((FrameLayout)localObject1).addView((View)localObject3, LayoutHelper.createFrame(-1, -2, 17));
      ((ViewGroup)localObject2).setTag(Integer.valueOf(2));
      ((ViewGroup)localObject2).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PopupNotificationActivity.this.openCurrentMessage();
        }
      });
      break label112;
      label631:
      paramInt = j;
      if (localPhotoSize == null) {
        break label274;
      }
      localBackupImageView.setImage(localPhotoSize.location, null, (Drawable)null);
      paramInt = 1;
      break label274;
      label659:
      localBackupImageView.setVisibility(0);
      ((TextView)localObject3).setVisibility(8);
      localObject1 = localObject2;
      break label315;
      label679:
      localObject1 = localObject2;
      if (localMessageObject.type != 4) {
        break label315;
      }
      ((TextView)localObject3).setVisibility(8);
      ((TextView)localObject3).setText(localMessageObject.messageText);
      localBackupImageView.setVisibility(0);
      double d1 = localMessageObject.messageOwner.media.geo.lat;
      double d2 = localMessageObject.messageOwner.media.geo._long;
      localBackupImageView.setImage(String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=13&size=100x100&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", new Object[] { Double.valueOf(d1), Double.valueOf(d2), Integer.valueOf(Math.min(2, (int)Math.ceil(AndroidUtilities.density))), Double.valueOf(d1), Double.valueOf(d2) }), null, null);
      localObject1 = localObject2;
      break label315;
      label821:
      if (localMessageObject.type == 2)
      {
        if (this.audioViews.size() > 0)
        {
          localObject2 = (ViewGroup)this.audioViews.get(0);
          this.audioViews.remove(0);
          localObject3 = (PopupAudioView)((ViewGroup)localObject2).findViewWithTag(Integer.valueOf(300));
        }
        for (;;)
        {
          ((PopupAudioView)localObject3).setMessageObject(localMessageObject);
          localObject1 = localObject2;
          if (!DownloadController.getInstance(localMessageObject.currentAccount).canDownloadMedia(localMessageObject)) {
            break;
          }
          ((PopupAudioView)localObject3).downloadAudioIfNeed();
          localObject1 = localObject2;
          break;
          localObject2 = new FrameLayout(this);
          localObject3 = new FrameLayout(this);
          ((FrameLayout)localObject3).setPadding(AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F));
          ((FrameLayout)localObject3).setBackgroundDrawable(Theme.getSelectorDrawable(false));
          ((ViewGroup)localObject2).addView((View)localObject3, LayoutHelper.createFrame(-1, -1.0F));
          localObject1 = new FrameLayout(this);
          ((FrameLayout)localObject3).addView((View)localObject1, LayoutHelper.createFrame(-1, -2.0F, 17, 20.0F, 0.0F, 20.0F, 0.0F));
          localObject3 = new PopupAudioView(this);
          ((PopupAudioView)localObject3).setTag(Integer.valueOf(300));
          ((FrameLayout)localObject1).addView((View)localObject3);
          ((ViewGroup)localObject2).setTag(Integer.valueOf(3));
          ((ViewGroup)localObject2).setOnClickListener(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              PopupNotificationActivity.this.openCurrentMessage();
            }
          });
        }
      }
      if (this.textViews.size() > 0)
      {
        localObject1 = (ViewGroup)this.textViews.get(0);
        this.textViews.remove(0);
      }
      for (;;)
      {
        localObject2 = (TextView)((ViewGroup)localObject1).findViewWithTag(Integer.valueOf(301));
        ((TextView)localObject2).setTextSize(2, SharedConfig.fontSize);
        ((TextView)localObject2).setText(localMessageObject.messageText);
        break;
        localObject1 = new FrameLayout(this);
        localObject3 = new ScrollView(this);
        ((ScrollView)localObject3).setFillViewport(true);
        ((ViewGroup)localObject1).addView((View)localObject3, LayoutHelper.createFrame(-1, -1.0F));
        localObject2 = new LinearLayout(this);
        ((LinearLayout)localObject2).setOrientation(0);
        ((LinearLayout)localObject2).setBackgroundDrawable(Theme.getSelectorDrawable(false));
        ((ScrollView)localObject3).addView((View)localObject2, LayoutHelper.createScroll(-1, -2, 1));
        ((LinearLayout)localObject2).setPadding(AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(10.0F));
        ((LinearLayout)localObject2).setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            PopupNotificationActivity.this.openCurrentMessage();
          }
        });
        localObject3 = new TextView(this);
        ((TextView)localObject3).setTextSize(1, 16.0F);
        ((TextView)localObject3).setTag(Integer.valueOf(301));
        ((TextView)localObject3).setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        ((TextView)localObject3).setLinkTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        ((TextView)localObject3).setGravity(17);
        ((LinearLayout)localObject2).addView((View)localObject3, LayoutHelper.createLinear(-1, -2, 17));
        ((ViewGroup)localObject1).setTag(Integer.valueOf(1));
      }
      if (i == this.currentMessageNum - 1) {
        ((ViewGroup)localObject1).setTranslationX(-paramInt);
      } else if (i == this.currentMessageNum + 1) {
        ((ViewGroup)localObject1).setTranslationX(paramInt);
      }
    }
  }
  
  private void handleIntent(Intent paramIntent)
  {
    boolean bool;
    int i;
    if ((paramIntent != null) && (paramIntent.getBooleanExtra("force", false)))
    {
      bool = true;
      this.isReply = bool;
      this.popupMessages.clear();
      if (!this.isReply) {
        break label127;
      }
      if (paramIntent == null) {
        break label120;
      }
      i = paramIntent.getIntExtra("currentAccount", UserConfig.selectedAccount);
      label51:
      this.popupMessages.addAll(NotificationsController.getInstance(i).popupReplyMessages);
      label66:
      if ((!((KeyguardManager)getSystemService("keyguard")).inKeyguardRestrictedInputMode()) && (ApplicationLoader.isScreenOn)) {
        break label166;
      }
      getWindow().addFlags(2623490);
    }
    for (;;)
    {
      if (this.currentMessageObject == null) {
        this.currentMessageNum = 0;
      }
      getNewMessage();
      return;
      bool = false;
      break;
      label120:
      i = UserConfig.selectedAccount;
      break label51;
      label127:
      i = 0;
      while (i < 3)
      {
        if (UserConfig.getInstance(i).isClientActivated()) {
          this.popupMessages.addAll(NotificationsController.getInstance(i).popupMessages);
        }
        i += 1;
      }
      break label66;
      label166:
      getWindow().addFlags(2623488);
      getWindow().clearFlags(2);
    }
  }
  
  private void openCurrentMessage()
  {
    if (this.currentMessageObject == null) {
      return;
    }
    Intent localIntent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
    long l = this.currentMessageObject.getDialogId();
    int i;
    if ((int)l != 0)
    {
      i = (int)l;
      if (i < 0) {
        localIntent.putExtra("chatId", -i);
      }
    }
    for (;;)
    {
      localIntent.putExtra("currentAccount", this.currentMessageObject.currentAccount);
      localIntent.setAction("com.tmessages.openchat" + Math.random() + Integer.MAX_VALUE);
      localIntent.setFlags(32768);
      startActivity(localIntent);
      onFinish();
      finish();
      return;
      localIntent.putExtra("userId", i);
      continue;
      localIntent.putExtra("encId", (int)(l >> 32));
    }
  }
  
  private void prepareLayouts(int paramInt)
  {
    int i = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0F);
    if (paramInt == 0)
    {
      reuseView(this.centerView);
      reuseView(this.leftView);
      reuseView(this.rightView);
      reuseButtonsView(this.centerButtonsView);
      reuseButtonsView(this.leftButtonsView);
      reuseButtonsView(this.rightButtonsView);
      paramInt = this.currentMessageNum - 1;
      if (paramInt < this.currentMessageNum + 2)
      {
        if (paramInt == this.currentMessageNum - 1)
        {
          this.leftView = getViewForMessage(paramInt, true);
          this.leftButtonsView = getButtonsViewForMessage(paramInt, true);
        }
        for (;;)
        {
          paramInt += 1;
          break;
          if (paramInt == this.currentMessageNum)
          {
            this.centerView = getViewForMessage(paramInt, true);
            this.centerButtonsView = getButtonsViewForMessage(paramInt, true);
          }
          else if (paramInt == this.currentMessageNum + 1)
          {
            this.rightView = getViewForMessage(paramInt, true);
            this.rightButtonsView = getButtonsViewForMessage(paramInt, true);
          }
        }
      }
    }
    else
    {
      if (paramInt != 1) {
        break label267;
      }
      reuseView(this.rightView);
      reuseButtonsView(this.rightButtonsView);
      this.rightView = this.centerView;
      this.centerView = this.leftView;
      this.leftView = getViewForMessage(this.currentMessageNum - 1, true);
      this.rightButtonsView = this.centerButtonsView;
      this.centerButtonsView = this.leftButtonsView;
      this.leftButtonsView = getButtonsViewForMessage(this.currentMessageNum - 1, true);
    }
    label267:
    float f;
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
              return;
              if (paramInt == 2)
              {
                reuseView(this.leftView);
                reuseButtonsView(this.leftButtonsView);
                this.leftView = this.centerView;
                this.centerView = this.rightView;
                this.rightView = getViewForMessage(this.currentMessageNum + 1, true);
                this.leftButtonsView = this.centerButtonsView;
                this.centerButtonsView = this.rightButtonsView;
                this.rightButtonsView = getButtonsViewForMessage(this.currentMessageNum + 1, true);
                return;
              }
              if (paramInt != 3) {
                break;
              }
              if (this.rightView != null)
              {
                f = this.rightView.getTranslationX();
                reuseView(this.rightView);
                localObject = getViewForMessage(this.currentMessageNum + 1, false);
                this.rightView = ((ViewGroup)localObject);
                if (localObject != null)
                {
                  localObject = (FrameLayout.LayoutParams)this.rightView.getLayoutParams();
                  ((FrameLayout.LayoutParams)localObject).width = i;
                  this.rightView.setLayoutParams((ViewGroup.LayoutParams)localObject);
                  this.rightView.setTranslationX(f);
                  this.rightView.invalidate();
                }
              }
            } while (this.rightButtonsView == null);
            f = this.rightButtonsView.getTranslationX();
            reuseButtonsView(this.rightButtonsView);
            localObject = getButtonsViewForMessage(this.currentMessageNum + 1, false);
            this.rightButtonsView = ((ViewGroup)localObject);
          } while (localObject == null);
          this.rightButtonsView.setTranslationX(f);
          return;
        } while (paramInt != 4);
        if (this.leftView != null)
        {
          f = this.leftView.getTranslationX();
          reuseView(this.leftView);
          localObject = getViewForMessage(0, false);
          this.leftView = ((ViewGroup)localObject);
          if (localObject != null)
          {
            localObject = (FrameLayout.LayoutParams)this.leftView.getLayoutParams();
            ((FrameLayout.LayoutParams)localObject).width = i;
            this.leftView.setLayoutParams((ViewGroup.LayoutParams)localObject);
            this.leftView.setTranslationX(f);
            this.leftView.invalidate();
          }
        }
      } while (this.leftButtonsView == null);
      f = this.leftButtonsView.getTranslationX();
      reuseButtonsView(this.leftButtonsView);
      localObject = getButtonsViewForMessage(0, false);
      this.leftButtonsView = ((ViewGroup)localObject);
    } while (localObject == null);
    this.leftButtonsView.setTranslationX(f);
  }
  
  private void reuseButtonsView(ViewGroup paramViewGroup)
  {
    if (paramViewGroup == null) {
      return;
    }
    this.popupContainer.removeView(paramViewGroup);
  }
  
  private void reuseView(ViewGroup paramViewGroup)
  {
    if (paramViewGroup == null) {}
    int i;
    do
    {
      return;
      i = ((Integer)paramViewGroup.getTag()).intValue();
      paramViewGroup.setVisibility(8);
      if (i == 1)
      {
        this.textViews.add(paramViewGroup);
        return;
      }
      if (i == 2)
      {
        this.imageViews.add(paramViewGroup);
        return;
      }
    } while (i != 3);
    this.audioViews.add(paramViewGroup);
  }
  
  private void setTypingAnimation(boolean paramBoolean)
  {
    if (this.actionBar == null) {
      return;
    }
    if (paramBoolean) {}
    for (;;)
    {
      try
      {
        Integer localInteger = (Integer)MessagesController.getInstance(this.currentMessageObject.currentAccount).printingStringsTypes.get(this.currentMessageObject.getDialogId());
        this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(this.statusDrawables[localInteger.intValue()], null, null, null);
        this.onlineTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0F));
        i = 0;
        if (i >= this.statusDrawables.length) {
          break;
        }
        if (i == localInteger.intValue()) {
          this.statusDrawables[i].start();
        } else {
          this.statusDrawables[i].stop();
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        return;
      }
      this.onlineTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
      this.onlineTextView.setCompoundDrawablePadding(0);
      int i = 0;
      while (i < this.statusDrawables.length)
      {
        this.statusDrawables[i].stop();
        i += 1;
      }
      break;
      i += 1;
    }
  }
  
  private void switchToNextMessage()
  {
    if (this.popupMessages.size() > 1) {
      if (this.currentMessageNum >= this.popupMessages.size() - 1) {
        break label103;
      }
    }
    label103:
    for (this.currentMessageNum += 1;; this.currentMessageNum = 0)
    {
      this.currentMessageObject = ((MessageObject)this.popupMessages.get(this.currentMessageNum));
      updateInterfaceForCurrentMessage(2);
      this.countText.setText(String.format("%d/%d", new Object[] { Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(this.popupMessages.size()) }));
      return;
    }
  }
  
  private void switchToPreviousMessage()
  {
    if (this.popupMessages.size() > 1) {
      if (this.currentMessageNum <= 0) {
        break label94;
      }
    }
    label94:
    for (this.currentMessageNum -= 1;; this.currentMessageNum = (this.popupMessages.size() - 1))
    {
      this.currentMessageObject = ((MessageObject)this.popupMessages.get(this.currentMessageNum));
      updateInterfaceForCurrentMessage(1);
      this.countText.setText(String.format("%d/%d", new Object[] { Integer.valueOf(this.currentMessageNum + 1), Integer.valueOf(this.popupMessages.size()) }));
      return;
    }
  }
  
  private void updateInterfaceForCurrentMessage(int paramInt)
  {
    if (this.actionBar == null) {
      return;
    }
    if (this.lastResumedAccount != this.currentMessageObject.currentAccount)
    {
      if (this.lastResumedAccount >= 0) {
        ConnectionsManager.getInstance(this.lastResumedAccount).setAppPaused(true, false);
      }
      this.lastResumedAccount = this.currentMessageObject.currentAccount;
      ConnectionsManager.getInstance(this.lastResumedAccount).setAppPaused(false, false);
    }
    this.currentChat = null;
    this.currentUser = null;
    long l = this.currentMessageObject.getDialogId();
    this.chatActivityEnterView.setDialogId(l, this.currentMessageObject.currentAccount);
    int i;
    if ((int)l != 0)
    {
      i = (int)l;
      if (i > 0)
      {
        this.currentUser = MessagesController.getInstance(this.currentMessageObject.currentAccount).getUser(Integer.valueOf(i));
        if ((this.currentChat == null) || (this.currentUser == null)) {
          break label316;
        }
        this.nameTextView.setText(this.currentChat.title);
        this.onlineTextView.setText(UserObject.getUserName(this.currentUser));
        this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        this.nameTextView.setCompoundDrawablePadding(0);
      }
    }
    for (;;)
    {
      prepareLayouts(paramInt);
      updateSubtitle();
      checkAndUpdateAvatar();
      applyViewsLayoutParams(0);
      return;
      this.currentChat = MessagesController.getInstance(this.currentMessageObject.currentAccount).getChat(Integer.valueOf(-i));
      this.currentUser = MessagesController.getInstance(this.currentMessageObject.currentAccount).getUser(Integer.valueOf(this.currentMessageObject.messageOwner.from_id));
      break;
      TLRPC.EncryptedChat localEncryptedChat = MessagesController.getInstance(this.currentMessageObject.currentAccount).getEncryptedChat(Integer.valueOf((int)(l >> 32)));
      this.currentUser = MessagesController.getInstance(this.currentMessageObject.currentAccount).getUser(Integer.valueOf(localEncryptedChat.user_id));
      break;
      label316:
      if (this.currentUser != null)
      {
        this.nameTextView.setText(UserObject.getUserName(this.currentUser));
        if ((int)l == 0)
        {
          this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(2131165386, 0, 0, 0);
          this.nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4.0F));
        }
        else
        {
          this.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          this.nameTextView.setCompoundDrawablePadding(0);
        }
      }
    }
  }
  
  private void updateSubtitle()
  {
    if ((this.actionBar == null) || (this.currentMessageObject == null)) {}
    while ((this.currentChat != null) || (this.currentUser == null)) {
      return;
    }
    if ((this.currentUser.id / 1000 != 777) && (this.currentUser.id / 1000 != 333) && (ContactsController.getInstance(this.currentMessageObject.currentAccount).contactsDict.get(Integer.valueOf(this.currentUser.id)) == null) && ((ContactsController.getInstance(this.currentMessageObject.currentAccount).contactsDict.size() != 0) || (!ContactsController.getInstance(this.currentMessageObject.currentAccount).isLoadingContacts()))) {
      if ((this.currentUser.phone != null) && (this.currentUser.phone.length() != 0)) {
        this.nameTextView.setText(PhoneFormat.getInstance().format("+" + this.currentUser.phone));
      }
    }
    while ((this.currentUser != null) && (this.currentUser.id == 777000))
    {
      this.onlineTextView.setText(LocaleController.getString("ServiceNotifications", 2131494365));
      return;
      this.nameTextView.setText(UserObject.getUserName(this.currentUser));
      continue;
      this.nameTextView.setText(UserObject.getUserName(this.currentUser));
    }
    Object localObject = (CharSequence)MessagesController.getInstance(this.currentMessageObject.currentAccount).printingStrings.get(this.currentMessageObject.getDialogId());
    if ((localObject == null) || (((CharSequence)localObject).length() == 0))
    {
      this.lastPrintString = null;
      setTypingAnimation(false);
      localObject = MessagesController.getInstance(this.currentMessageObject.currentAccount).getUser(Integer.valueOf(this.currentUser.id));
      if (localObject != null) {
        this.currentUser = ((TLRPC.User)localObject);
      }
      this.onlineTextView.setText(LocaleController.formatUserStatus(this.currentMessageObject.currentAccount, this.currentUser));
      return;
    }
    this.lastPrintString = ((CharSequence)localObject);
    this.onlineTextView.setText((CharSequence)localObject);
    setTypingAnimation(true);
  }
  
  public boolean checkTransitionAnimation()
  {
    if ((this.animationInProgress) && (this.animationStartTime < System.currentTimeMillis() - 400L))
    {
      this.animationInProgress = false;
      if (this.onAnimationEndRunnable != null)
      {
        this.onAnimationEndRunnable.run();
        this.onAnimationEndRunnable = null;
      }
    }
    return this.animationInProgress;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.appDidLogout) {
      if (paramInt2 == this.lastResumedAccount)
      {
        onFinish();
        finish();
      }
    }
    do
    {
      for (;;)
      {
        return;
        if (paramInt1 == NotificationCenter.pushMessagesUpdated)
        {
          if (!this.isReply)
          {
            this.popupMessages.clear();
            paramInt1 = 0;
            while (paramInt1 < 3)
            {
              if (UserConfig.getInstance(paramInt1).isClientActivated()) {
                this.popupMessages.addAll(NotificationsController.getInstance(paramInt1).popupMessages);
              }
              paramInt1 += 1;
            }
            getNewMessage();
          }
        }
        else if (paramInt1 == NotificationCenter.updateInterfaces)
        {
          if ((this.currentMessageObject != null) && (paramInt2 == this.lastResumedAccount))
          {
            paramInt1 = ((Integer)paramVarArgs[0]).intValue();
            if (((paramInt1 & 0x1) != 0) || ((paramInt1 & 0x4) != 0) || ((paramInt1 & 0x10) != 0) || ((paramInt1 & 0x20) != 0)) {
              updateSubtitle();
            }
            if (((paramInt1 & 0x2) != 0) || ((paramInt1 & 0x8) != 0)) {
              checkAndUpdateAvatar();
            }
            if ((paramInt1 & 0x40) != 0)
            {
              paramVarArgs = (CharSequence)MessagesController.getInstance(this.currentMessageObject.currentAccount).printingStrings.get(this.currentMessageObject.getDialogId());
              if (((this.lastPrintString != null) && (paramVarArgs == null)) || ((this.lastPrintString == null) && (paramVarArgs != null)) || ((this.lastPrintString != null) && (paramVarArgs != null) && (!this.lastPrintString.equals(paramVarArgs)))) {
                updateSubtitle();
              }
            }
          }
        }
        else
        {
          int i;
          Object localObject;
          MessageObject localMessageObject;
          if (paramInt1 == NotificationCenter.messagePlayingDidReset)
          {
            paramVarArgs = (Integer)paramVarArgs[0];
            if (this.messageContainer != null)
            {
              i = this.messageContainer.getChildCount();
              paramInt1 = 0;
              while (paramInt1 < i)
              {
                localObject = this.messageContainer.getChildAt(paramInt1);
                if (((Integer)((View)localObject).getTag()).intValue() == 3)
                {
                  localObject = (PopupAudioView)((View)localObject).findViewWithTag(Integer.valueOf(300));
                  localMessageObject = ((PopupAudioView)localObject).getMessageObject();
                  if ((localMessageObject != null) && (localMessageObject.currentAccount == paramInt2) && (localMessageObject.getId() == paramVarArgs.intValue()))
                  {
                    ((PopupAudioView)localObject).updateButtonState();
                    return;
                  }
                }
                paramInt1 += 1;
              }
            }
          }
          else if (paramInt1 == NotificationCenter.messagePlayingProgressDidChanged)
          {
            paramVarArgs = (Integer)paramVarArgs[0];
            if (this.messageContainer != null)
            {
              i = this.messageContainer.getChildCount();
              paramInt1 = 0;
              while (paramInt1 < i)
              {
                localObject = this.messageContainer.getChildAt(paramInt1);
                if (((Integer)((View)localObject).getTag()).intValue() == 3)
                {
                  localObject = (PopupAudioView)((View)localObject).findViewWithTag(Integer.valueOf(300));
                  localMessageObject = ((PopupAudioView)localObject).getMessageObject();
                  if ((localMessageObject != null) && (localMessageObject.currentAccount == paramInt2) && (localMessageObject.getId() == paramVarArgs.intValue()))
                  {
                    ((PopupAudioView)localObject).updateProgress();
                    return;
                  }
                }
                paramInt1 += 1;
              }
            }
          }
          else
          {
            if (paramInt1 != NotificationCenter.emojiDidLoaded) {
              break;
            }
            if (this.messageContainer != null)
            {
              paramInt2 = this.messageContainer.getChildCount();
              paramInt1 = 0;
              while (paramInt1 < paramInt2)
              {
                paramVarArgs = this.messageContainer.getChildAt(paramInt1);
                if (((Integer)paramVarArgs.getTag()).intValue() == 1)
                {
                  paramVarArgs = (TextView)paramVarArgs.findViewWithTag(Integer.valueOf(301));
                  if (paramVarArgs != null) {
                    paramVarArgs.invalidate();
                  }
                }
                paramInt1 += 1;
              }
            }
          }
        }
      }
    } while ((paramInt1 != NotificationCenter.contactsDidLoaded) || (paramInt2 != this.lastResumedAccount));
    updateSubtitle();
  }
  
  public void onBackPressed()
  {
    if (this.chatActivityEnterView.isPopupShowing())
    {
      this.chatActivityEnterView.hidePopup(true);
      return;
    }
    super.onBackPressed();
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    AndroidUtilities.checkDisplaySize(this, paramConfiguration);
    fixLayout();
  }
  
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    Theme.createChatResources(this, false);
    int i = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (i > 0) {
      AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(i);
    }
    i = 0;
    while (i < 3)
    {
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.appDidLogout);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.updateInterfaces);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.contactsDidLoaded);
      i += 1;
    }
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.pushMessagesUpdated);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    this.classGuid = ConnectionsManager.generateClassGuid();
    this.statusDrawables[0] = new TypingDotsDrawable();
    this.statusDrawables[1] = new RecordStatusDrawable();
    this.statusDrawables[2] = new SendingFileDrawable();
    this.statusDrawables[3] = new PlayingGameDrawable();
    this.statusDrawables[4] = new RoundStatusDrawable();
    paramBundle = new SizeNotifierFrameLayout(this)
    {
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        int n = getChildCount();
        if (getKeyboardHeight() <= AndroidUtilities.dp(20.0F)) {}
        View localView;
        for (int k = PopupNotificationActivity.this.chatActivityEnterView.getEmojiPadding();; k = 0)
        {
          int m = 0;
          for (;;)
          {
            if (m >= n) {
              break label457;
            }
            localView = getChildAt(m);
            if (localView.getVisibility() != 8) {
              break;
            }
            m += 1;
          }
        }
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)localView.getLayoutParams();
        int i1 = localView.getMeasuredWidth();
        int i2 = localView.getMeasuredHeight();
        int j = localLayoutParams.gravity;
        int i = j;
        if (j == -1) {
          i = 51;
        }
        switch (i & 0x7 & 0x7)
        {
        default: 
          j = localLayoutParams.leftMargin;
          label159:
          switch (i & 0x70)
          {
          default: 
            i = localLayoutParams.topMargin;
            label207:
            if (PopupNotificationActivity.this.chatActivityEnterView.isPopupView(localView)) {
              if (k != 0) {
                i = getMeasuredHeight() - k;
              }
            }
            break;
          }
          break;
        }
        for (;;)
        {
          localView.layout(j, i, j + i1, i + i2);
          break;
          j = (paramAnonymousInt3 - paramAnonymousInt1 - i1) / 2 + localLayoutParams.leftMargin - localLayoutParams.rightMargin;
          break label159;
          j = paramAnonymousInt3 - i1 - localLayoutParams.rightMargin;
          break label159;
          i = localLayoutParams.topMargin;
          break label207;
          i = (paramAnonymousInt4 - k - paramAnonymousInt2 - i2) / 2 + localLayoutParams.topMargin - localLayoutParams.bottomMargin;
          break label207;
          i = paramAnonymousInt4 - k - paramAnonymousInt2 - i2 - localLayoutParams.bottomMargin;
          break label207;
          i = getMeasuredHeight();
          continue;
          if (PopupNotificationActivity.this.chatActivityEnterView.isRecordCircle(localView))
          {
            i = PopupNotificationActivity.this.popupContainer.getTop() + PopupNotificationActivity.this.popupContainer.getMeasuredHeight() - localView.getMeasuredHeight() - localLayoutParams.bottomMargin;
            j = PopupNotificationActivity.this.popupContainer.getLeft() + PopupNotificationActivity.this.popupContainer.getMeasuredWidth() - localView.getMeasuredWidth() - localLayoutParams.rightMargin;
          }
        }
        label457:
        notifyHeightChanged();
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        View.MeasureSpec.getMode(paramAnonymousInt1);
        View.MeasureSpec.getMode(paramAnonymousInt2);
        int k = View.MeasureSpec.getSize(paramAnonymousInt1);
        int j = View.MeasureSpec.getSize(paramAnonymousInt2);
        setMeasuredDimension(k, j);
        int i = j;
        if (getKeyboardHeight() <= AndroidUtilities.dp(20.0F)) {
          i = j - PopupNotificationActivity.this.chatActivityEnterView.getEmojiPadding();
        }
        int m = getChildCount();
        j = 0;
        if (j < m)
        {
          View localView = getChildAt(j);
          if (localView.getVisibility() == 8) {}
          for (;;)
          {
            j += 1;
            break;
            if (PopupNotificationActivity.this.chatActivityEnterView.isPopupView(localView)) {
              localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(localView.getLayoutParams().height, 1073741824));
            } else if (PopupNotificationActivity.this.chatActivityEnterView.isRecordCircle(localView)) {
              measureChildWithMargins(localView, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
            } else {
              localView.measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0F), AndroidUtilities.dp(2.0F) + i), 1073741824));
            }
          }
        }
      }
    };
    setContentView(paramBundle);
    paramBundle.setBackgroundColor(-1728053248);
    RelativeLayout localRelativeLayout = new RelativeLayout(this);
    paramBundle.addView(localRelativeLayout, LayoutHelper.createFrame(-1, -1.0F));
    this.popupContainer = new RelativeLayout(this)
    {
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        paramAnonymousInt1 = 0;
        while (paramAnonymousInt1 < getChildCount())
        {
          View localView = getChildAt(paramAnonymousInt1);
          if ((localView.getTag() instanceof String)) {
            localView.layout(localView.getLeft(), PopupNotificationActivity.this.chatActivityEnterView.getTop() + AndroidUtilities.dp(3.0F), localView.getRight(), PopupNotificationActivity.this.chatActivityEnterView.getBottom());
          }
          paramAnonymousInt1 += 1;
        }
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        super.onMeasure(paramAnonymousInt1, paramAnonymousInt2);
        paramAnonymousInt2 = PopupNotificationActivity.this.chatActivityEnterView.getMeasuredWidth();
        int i = PopupNotificationActivity.this.chatActivityEnterView.getMeasuredHeight();
        paramAnonymousInt1 = 0;
        while (paramAnonymousInt1 < getChildCount())
        {
          View localView = getChildAt(paramAnonymousInt1);
          if ((localView.getTag() instanceof String)) {
            localView.measure(View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2, 1073741824), View.MeasureSpec.makeMeasureSpec(i - AndroidUtilities.dp(3.0F), 1073741824));
          }
          paramAnonymousInt1 += 1;
        }
      }
    };
    this.popupContainer.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
    localRelativeLayout.addView(this.popupContainer, LayoutHelper.createRelative(-1, 240, 12, 0, 12, 0, 13));
    if (this.chatActivityEnterView != null) {
      this.chatActivityEnterView.onDestroy();
    }
    this.chatActivityEnterView = new ChatActivityEnterView(this, paramBundle, null, false);
    this.chatActivityEnterView.setId(1000);
    this.popupContainer.addView(this.chatActivityEnterView, LayoutHelper.createRelative(-1, -2, 12));
    this.chatActivityEnterView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate()
    {
      public void didPressedAttachButton() {}
      
      public void needChangeVideoPreviewState(int paramAnonymousInt, float paramAnonymousFloat) {}
      
      public void needSendTyping()
      {
        if (PopupNotificationActivity.this.currentMessageObject != null) {
          MessagesController.getInstance(PopupNotificationActivity.this.currentMessageObject.currentAccount).sendTyping(PopupNotificationActivity.this.currentMessageObject.getDialogId(), 0, PopupNotificationActivity.this.classGuid);
        }
      }
      
      public void needShowMediaBanHint() {}
      
      public void needStartRecordAudio(int paramAnonymousInt) {}
      
      public void needStartRecordVideo(int paramAnonymousInt) {}
      
      public void onAttachButtonHidden() {}
      
      public void onAttachButtonShow() {}
      
      public void onMessageEditEnd(boolean paramAnonymousBoolean) {}
      
      public void onMessageSend(CharSequence paramAnonymousCharSequence)
      {
        if (PopupNotificationActivity.this.currentMessageObject == null) {
          return;
        }
        if ((PopupNotificationActivity.this.currentMessageNum >= 0) && (PopupNotificationActivity.this.currentMessageNum < PopupNotificationActivity.this.popupMessages.size())) {
          PopupNotificationActivity.this.popupMessages.remove(PopupNotificationActivity.this.currentMessageNum);
        }
        MessagesController.getInstance(PopupNotificationActivity.this.currentMessageObject.currentAccount).markDialogAsRead(PopupNotificationActivity.this.currentMessageObject.getDialogId(), PopupNotificationActivity.this.currentMessageObject.getId(), Math.max(0, PopupNotificationActivity.this.currentMessageObject.getId()), PopupNotificationActivity.this.currentMessageObject.messageOwner.date, true, 0, true);
        PopupNotificationActivity.access$202(PopupNotificationActivity.this, null);
        PopupNotificationActivity.this.getNewMessage();
      }
      
      public void onPreAudioVideoRecord() {}
      
      public void onStickersExpandedChange() {}
      
      public void onStickersTab(boolean paramAnonymousBoolean) {}
      
      public void onSwitchRecordMode(boolean paramAnonymousBoolean) {}
      
      public void onTextChanged(CharSequence paramAnonymousCharSequence, boolean paramAnonymousBoolean) {}
      
      public void onWindowSizeChanged(int paramAnonymousInt) {}
    });
    this.messageContainer = new FrameLayoutTouch(this);
    this.popupContainer.addView(this.messageContainer, 0);
    this.actionBar = new ActionBar(this);
    this.actionBar.setOccupyStatusBar(false);
    this.actionBar.setBackButtonImage(2131165374);
    this.actionBar.setBackgroundColor(Theme.getColor("actionBarDefault"));
    this.actionBar.setItemsBackgroundColor(Theme.getColor("actionBarDefaultSelector"), false);
    this.popupContainer.addView(this.actionBar);
    paramBundle = this.actionBar.getLayoutParams();
    paramBundle.width = -1;
    this.actionBar.setLayoutParams(paramBundle);
    paramBundle = this.actionBar.createMenu().addItemWithWidth(2, 0, AndroidUtilities.dp(56.0F));
    this.countText = new TextView(this);
    this.countText.setTextColor(Theme.getColor("actionBarDefaultSubtitle"));
    this.countText.setTextSize(1, 14.0F);
    this.countText.setGravity(17);
    paramBundle.addView(this.countText, LayoutHelper.createFrame(56, -1.0F));
    this.avatarContainer = new FrameLayout(this);
    this.avatarContainer.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
    this.actionBar.addView(this.avatarContainer);
    paramBundle = (FrameLayout.LayoutParams)this.avatarContainer.getLayoutParams();
    paramBundle.height = -1;
    paramBundle.width = -2;
    paramBundle.rightMargin = AndroidUtilities.dp(48.0F);
    paramBundle.leftMargin = AndroidUtilities.dp(60.0F);
    paramBundle.gravity = 51;
    this.avatarContainer.setLayoutParams(paramBundle);
    this.avatarImageView = new BackupImageView(this);
    this.avatarImageView.setRoundRadius(AndroidUtilities.dp(21.0F));
    this.avatarContainer.addView(this.avatarImageView);
    paramBundle = (FrameLayout.LayoutParams)this.avatarImageView.getLayoutParams();
    paramBundle.width = AndroidUtilities.dp(42.0F);
    paramBundle.height = AndroidUtilities.dp(42.0F);
    paramBundle.topMargin = AndroidUtilities.dp(3.0F);
    this.avatarImageView.setLayoutParams(paramBundle);
    this.nameTextView = new TextView(this);
    this.nameTextView.setTextColor(Theme.getColor("actionBarDefaultTitle"));
    this.nameTextView.setTextSize(1, 18.0F);
    this.nameTextView.setLines(1);
    this.nameTextView.setMaxLines(1);
    this.nameTextView.setSingleLine(true);
    this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.nameTextView.setGravity(3);
    this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.avatarContainer.addView(this.nameTextView);
    paramBundle = (FrameLayout.LayoutParams)this.nameTextView.getLayoutParams();
    paramBundle.width = -2;
    paramBundle.height = -2;
    paramBundle.leftMargin = AndroidUtilities.dp(54.0F);
    paramBundle.bottomMargin = AndroidUtilities.dp(22.0F);
    paramBundle.gravity = 80;
    this.nameTextView.setLayoutParams(paramBundle);
    this.onlineTextView = new TextView(this);
    this.onlineTextView.setTextColor(Theme.getColor("actionBarDefaultSubtitle"));
    this.onlineTextView.setTextSize(1, 14.0F);
    this.onlineTextView.setLines(1);
    this.onlineTextView.setMaxLines(1);
    this.onlineTextView.setSingleLine(true);
    this.onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.onlineTextView.setGravity(3);
    this.avatarContainer.addView(this.onlineTextView);
    paramBundle = (FrameLayout.LayoutParams)this.onlineTextView.getLayoutParams();
    paramBundle.width = -2;
    paramBundle.height = -2;
    paramBundle.leftMargin = AndroidUtilities.dp(54.0F);
    paramBundle.bottomMargin = AndroidUtilities.dp(4.0F);
    paramBundle.gravity = 80;
    this.onlineTextView.setLayoutParams(paramBundle);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1)
        {
          PopupNotificationActivity.this.onFinish();
          PopupNotificationActivity.this.finish();
        }
        do
        {
          return;
          if (paramAnonymousInt == 1)
          {
            PopupNotificationActivity.this.openCurrentMessage();
            return;
          }
        } while (paramAnonymousInt != 2);
        PopupNotificationActivity.this.switchToNextMessage();
      }
    });
    this.wakeLock = ((PowerManager)ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(268435462, "screen");
    this.wakeLock.setReferenceCounted(false);
    handleIntent(getIntent());
  }
  
  protected void onDestroy()
  {
    super.onDestroy();
    onFinish();
    MediaController.getInstance().setFeedbackView(this.chatActivityEnterView, false);
    if (this.wakeLock.isHeld()) {
      this.wakeLock.release();
    }
    if (this.avatarImageView != null) {
      this.avatarImageView.setImageDrawable(null);
    }
  }
  
  protected void onFinish()
  {
    if (this.finished) {}
    do
    {
      return;
      this.finished = true;
      if (this.isReply) {
        this.popupMessages.clear();
      }
      int i = 0;
      while (i < 3)
      {
        NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.contactsDidLoaded);
        i += 1;
      }
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.pushMessagesUpdated);
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
      if (this.chatActivityEnterView != null) {
        this.chatActivityEnterView.onDestroy();
      }
    } while (!this.wakeLock.isHeld());
    this.wakeLock.release();
  }
  
  protected void onNewIntent(Intent paramIntent)
  {
    super.onNewIntent(paramIntent);
    handleIntent(paramIntent);
  }
  
  protected void onPause()
  {
    super.onPause();
    overridePendingTransition(0, 0);
    if (this.chatActivityEnterView != null)
    {
      this.chatActivityEnterView.hidePopup(false);
      this.chatActivityEnterView.setFieldFocused(false);
    }
    if (this.lastResumedAccount >= 0) {
      ConnectionsManager.getInstance(this.lastResumedAccount).setAppPaused(true, false);
    }
  }
  
  public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    super.onRequestPermissionsResult(paramInt, paramArrayOfString, paramArrayOfInt);
    if ((paramInt != 3) || (paramArrayOfInt[0] == 0)) {
      return;
    }
    paramArrayOfString = new AlertDialog.Builder(this);
    paramArrayOfString.setTitle(LocaleController.getString("AppName", 2131492981));
    paramArrayOfString.setMessage(LocaleController.getString("PermissionNoAudio", 2131494142));
    paramArrayOfString.setNegativeButton(LocaleController.getString("PermissionOpenSettings", 2131494147), new DialogInterface.OnClickListener()
    {
      @TargetApi(9)
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        try
        {
          paramAnonymousDialogInterface = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
          paramAnonymousDialogInterface.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
          PopupNotificationActivity.this.startActivity(paramAnonymousDialogInterface);
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
        }
      }
    });
    paramArrayOfString.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    paramArrayOfString.show();
  }
  
  protected void onResume()
  {
    super.onResume();
    MediaController.getInstance().setFeedbackView(this.chatActivityEnterView, true);
    if (this.chatActivityEnterView != null) {
      this.chatActivityEnterView.setFieldFocused(true);
    }
    fixLayout();
    checkAndUpdateAvatar();
    this.wakeLock.acquire(7000L);
  }
  
  public boolean onTouchEventMy(MotionEvent paramMotionEvent)
  {
    if (checkTransitionAnimation()) {
      return false;
    }
    if ((paramMotionEvent != null) && (paramMotionEvent.getAction() == 0)) {
      this.moveStartX = paramMotionEvent.getX();
    }
    float f;
    int j;
    int i;
    label199:
    label207:
    do
    {
      for (;;)
      {
        return this.startedMoving;
        if ((paramMotionEvent == null) || (paramMotionEvent.getAction() != 2)) {
          break;
        }
        f = paramMotionEvent.getX();
        j = (int)(f - this.moveStartX);
        i = j;
        if (this.moveStartX != -1.0F)
        {
          i = j;
          if (!this.startedMoving)
          {
            i = j;
            if (Math.abs(j) > AndroidUtilities.dp(10.0F))
            {
              this.startedMoving = true;
              this.moveStartX = f;
              AndroidUtilities.lockOrientation(this);
              i = 0;
              if (this.velocityTracker != null) {
                break label199;
              }
              this.velocityTracker = VelocityTracker.obtain();
            }
          }
        }
        for (;;)
        {
          if (!this.startedMoving) {
            break label207;
          }
          j = i;
          if (this.leftView == null)
          {
            j = i;
            if (i > 0) {
              j = 0;
            }
          }
          i = j;
          if (this.rightView == null)
          {
            i = j;
            if (j < 0) {
              i = 0;
            }
          }
          if (this.velocityTracker != null) {
            this.velocityTracker.addMovement(paramMotionEvent);
          }
          applyViewsLayoutParams(i);
          break;
          this.velocityTracker.clear();
        }
      }
    } while ((paramMotionEvent != null) && (paramMotionEvent.getAction() != 1) && (paramMotionEvent.getAction() != 3));
    int k;
    int m;
    ViewGroup localViewGroup;
    if ((paramMotionEvent != null) && (this.startedMoving))
    {
      k = (int)(paramMotionEvent.getX() - this.moveStartX);
      m = AndroidUtilities.displaySize.x - AndroidUtilities.dp(24.0F);
      f = 0.0F;
      j = 0;
      localViewGroup = null;
      paramMotionEvent = null;
      i = j;
      if (this.velocityTracker != null)
      {
        this.velocityTracker.computeCurrentVelocity(1000);
        if (this.velocityTracker.getXVelocity() >= 3500.0F) {
          i = 1;
        }
      }
      else
      {
        label312:
        if (((i != 1) && (k <= m / 3)) || (this.leftView == null)) {
          break label631;
        }
        f = m - this.centerView.getTranslationX();
        localViewGroup = this.leftView;
        paramMotionEvent = this.leftButtonsView;
        this.onAnimationEndRunnable = new Runnable()
        {
          public void run()
          {
            PopupNotificationActivity.access$902(PopupNotificationActivity.this, false);
            PopupNotificationActivity.this.switchToPreviousMessage();
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
          }
        };
        label368:
        if (f != 0.0F)
        {
          i = (int)(Math.abs(f / m) * 200.0F);
          ArrayList localArrayList = new ArrayList();
          localArrayList.add(ObjectAnimator.ofFloat(this.centerView, "translationX", new float[] { this.centerView.getTranslationX() + f }));
          if (this.centerButtonsView != null) {
            localArrayList.add(ObjectAnimator.ofFloat(this.centerButtonsView, "translationX", new float[] { this.centerButtonsView.getTranslationX() + f }));
          }
          if (localViewGroup != null) {
            localArrayList.add(ObjectAnimator.ofFloat(localViewGroup, "translationX", new float[] { localViewGroup.getTranslationX() + f }));
          }
          if (paramMotionEvent != null) {
            localArrayList.add(ObjectAnimator.ofFloat(paramMotionEvent, "translationX", new float[] { paramMotionEvent.getTranslationX() + f }));
          }
          paramMotionEvent = new AnimatorSet();
          paramMotionEvent.playTogether(localArrayList);
          paramMotionEvent.setDuration(i);
          paramMotionEvent.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if (PopupNotificationActivity.this.onAnimationEndRunnable != null)
              {
                PopupNotificationActivity.this.onAnimationEndRunnable.run();
                PopupNotificationActivity.access$1202(PopupNotificationActivity.this, null);
              }
            }
          });
          paramMotionEvent.start();
          this.animationInProgress = true;
          this.animationStartTime = System.currentTimeMillis();
        }
      }
    }
    for (;;)
    {
      if (this.velocityTracker != null)
      {
        this.velocityTracker.recycle();
        this.velocityTracker = null;
      }
      this.startedMoving = false;
      this.moveStartX = -1.0F;
      break;
      i = j;
      if (this.velocityTracker.getXVelocity() > -3500.0F) {
        break label312;
      }
      i = 2;
      break label312;
      label631:
      if (((i == 2) || (k < -m / 3)) && (this.rightView != null))
      {
        f = -m - this.centerView.getTranslationX();
        localViewGroup = this.rightView;
        paramMotionEvent = this.rightButtonsView;
        this.onAnimationEndRunnable = new Runnable()
        {
          public void run()
          {
            PopupNotificationActivity.access$902(PopupNotificationActivity.this, false);
            PopupNotificationActivity.this.switchToNextMessage();
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
          }
        };
        break label368;
      }
      if (this.centerView.getTranslationX() == 0.0F) {
        break label368;
      }
      f = -this.centerView.getTranslationX();
      if (k > 0)
      {
        localViewGroup = this.leftView;
        label724:
        if (k <= 0) {
          break label758;
        }
      }
      label758:
      for (paramMotionEvent = this.leftButtonsView;; paramMotionEvent = this.rightButtonsView)
      {
        this.onAnimationEndRunnable = new Runnable()
        {
          public void run()
          {
            PopupNotificationActivity.access$902(PopupNotificationActivity.this, false);
            PopupNotificationActivity.this.applyViewsLayoutParams(0);
            AndroidUtilities.unlockOrientation(PopupNotificationActivity.this);
          }
        };
        break;
        localViewGroup = this.rightView;
        break label724;
      }
      applyViewsLayoutParams(0);
    }
  }
  
  private class FrameLayoutTouch
    extends FrameLayout
  {
    public FrameLayoutTouch(Context paramContext)
    {
      super();
    }
    
    public FrameLayoutTouch(Context paramContext, AttributeSet paramAttributeSet)
    {
      super(paramAttributeSet);
    }
    
    public FrameLayoutTouch(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
      super(paramAttributeSet, paramInt);
    }
    
    public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
    {
      return (PopupNotificationActivity.this.checkTransitionAnimation()) || (((PopupNotificationActivity)getContext()).onTouchEventMy(paramMotionEvent));
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      return (PopupNotificationActivity.this.checkTransitionAnimation()) || (((PopupNotificationActivity)getContext()).onTouchEventMy(paramMotionEvent));
    }
    
    public void requestDisallowInterceptTouchEvent(boolean paramBoolean)
    {
      ((PopupNotificationActivity)getContext()).onTouchEventMy(null);
      super.requestDisallowInterceptTouchEvent(paramBoolean);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/PopupNotificationActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */