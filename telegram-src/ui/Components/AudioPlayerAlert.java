package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemDelegate;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.AudioPlayerCell;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.DialogsActivity.DialogsActivityDelegate;
import org.telegram.ui.LaunchActivity;

public class AudioPlayerAlert
  extends BottomSheet
  implements DownloadController.FileDownloadProgressListener, NotificationCenter.NotificationCenterDelegate
{
  private int TAG;
  private ActionBar actionBar;
  private AnimatorSet actionBarAnimation;
  private AnimatorSet animatorSet;
  private TextView authorTextView;
  private ChatAvatarContainer avatarContainer;
  private View[] buttons = new View[5];
  private TextView durationTextView;
  private float endTranslation;
  private float fullAnimationProgress;
  private boolean hasNoCover;
  private boolean hasOptions = true;
  private boolean inFullSize;
  private boolean isInFullMode;
  private int lastTime;
  private LinearLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private ActionBarMenuItem menuItem;
  private Drawable noCoverDrawable;
  private ActionBarMenuItem optionsButton;
  private Paint paint = new Paint(1);
  private float panelEndTranslation;
  private float panelStartTranslation;
  private LaunchActivity parentActivity;
  private BackupImageView placeholderImageView;
  private ImageView playButton;
  private Drawable[] playOrderButtons = new Drawable[2];
  private FrameLayout playerLayout;
  private ArrayList<MessageObject> playlist = new ArrayList();
  private LineProgressView progressView;
  private ImageView repeatButton;
  private int scrollOffsetY = Integer.MAX_VALUE;
  private boolean scrollToSong = true;
  private ActionBarMenuItem searchItem;
  private int searchOpenOffset;
  private int searchOpenPosition = -1;
  private boolean searchWas;
  private boolean searching;
  private SeekBarView seekBarView;
  private View shadow;
  private View shadow2;
  private Drawable shadowDrawable;
  private ActionBarMenuItem shuffleButton;
  private float startTranslation;
  private float thumbMaxScale;
  private int thumbMaxX;
  private int thumbMaxY;
  private SimpleTextView timeTextView;
  private TextView titleTextView;
  private int topBeforeSwitch;
  
  public AudioPlayerAlert(Context paramContext)
  {
    super(paramContext, true);
    Object localObject1 = MediaController.getInstance().getPlayingMessageObject();
    int i;
    int j;
    if (localObject1 != null)
    {
      this.currentAccount = ((MessageObject)localObject1).currentAccount;
      this.parentActivity = ((LaunchActivity)paramContext);
      this.noCoverDrawable = paramContext.getResources().getDrawable(2131165539).mutate();
      this.noCoverDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_placeholder"), PorterDuff.Mode.MULTIPLY));
      this.TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidStarted);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.musicDidLoaded);
      this.shadowDrawable = paramContext.getResources().getDrawable(2131165640).mutate();
      this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_background"), PorterDuff.Mode.MULTIPLY));
      this.paint.setColor(Theme.getColor("player_placeholderBackground"));
      this.containerView = new FrameLayout(paramContext)
      {
        private boolean ignoreLayout = false;
        
        protected void onDraw(Canvas paramAnonymousCanvas)
        {
          AudioPlayerAlert.this.shadowDrawable.setBounds(0, Math.max(AudioPlayerAlert.this.actionBar.getMeasuredHeight(), AudioPlayerAlert.this.scrollOffsetY) - AudioPlayerAlert.backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
          AudioPlayerAlert.this.shadowDrawable.draw(paramAnonymousCanvas);
        }
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          if ((paramAnonymousMotionEvent.getAction() == 0) && (AudioPlayerAlert.this.scrollOffsetY != 0) && (paramAnonymousMotionEvent.getY() < AudioPlayerAlert.this.scrollOffsetY) && (AudioPlayerAlert.this.placeholderImageView.getTranslationX() == 0.0F))
          {
            AudioPlayerAlert.this.dismiss();
            return true;
          }
          return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          paramAnonymousInt1 = AudioPlayerAlert.this.actionBar.getMeasuredHeight();
          AudioPlayerAlert.this.shadow.layout(AudioPlayerAlert.this.shadow.getLeft(), paramAnonymousInt1, AudioPlayerAlert.this.shadow.getRight(), AudioPlayerAlert.this.shadow.getMeasuredHeight() + paramAnonymousInt1);
          AudioPlayerAlert.this.updateLayout();
          AudioPlayerAlert.this.setFullAnimationProgress(AudioPlayerAlert.this.fullAnimationProgress);
        }
        
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int j = View.MeasureSpec.getSize(paramAnonymousInt2);
          paramAnonymousInt2 = AndroidUtilities.dp(178.0F) + AudioPlayerAlert.this.playlist.size() * AndroidUtilities.dp(56.0F) + AudioPlayerAlert.backgroundPaddingTop + ActionBar.getCurrentActionBarHeight() + AndroidUtilities.statusBarHeight;
          int k = View.MeasureSpec.makeMeasureSpec(j, 1073741824);
          int i;
          int m;
          boolean bool;
          if (AudioPlayerAlert.this.searching)
          {
            i = AndroidUtilities.dp(178.0F);
            m = ActionBar.getCurrentActionBarHeight();
            if (Build.VERSION.SDK_INT >= 21)
            {
              paramAnonymousInt2 = AndroidUtilities.statusBarHeight;
              paramAnonymousInt2 = m + i + paramAnonymousInt2;
              if (AudioPlayerAlert.this.listView.getPaddingTop() != paramAnonymousInt2)
              {
                this.ignoreLayout = true;
                AudioPlayerAlert.this.listView.setPadding(0, paramAnonymousInt2, 0, AndroidUtilities.dp(8.0F));
                this.ignoreLayout = false;
              }
              super.onMeasure(paramAnonymousInt1, k);
              AudioPlayerAlert localAudioPlayerAlert = AudioPlayerAlert.this;
              if (getMeasuredHeight() < j) {
                break label429;
              }
              bool = true;
              label157:
              AudioPlayerAlert.access$602(localAudioPlayerAlert, bool);
              paramAnonymousInt2 = ActionBar.getCurrentActionBarHeight();
              if (Build.VERSION.SDK_INT < 21) {
                break label435;
              }
            }
          }
          label424:
          label429:
          label435:
          for (paramAnonymousInt1 = AndroidUtilities.statusBarHeight;; paramAnonymousInt1 = 0)
          {
            paramAnonymousInt1 = j - paramAnonymousInt2 - paramAnonymousInt1 - AndroidUtilities.dp(120.0F);
            paramAnonymousInt2 = Math.max(paramAnonymousInt1, getMeasuredWidth());
            AudioPlayerAlert.access$702(AudioPlayerAlert.this, (getMeasuredWidth() - paramAnonymousInt2) / 2 - AndroidUtilities.dp(17.0F));
            AudioPlayerAlert.access$802(AudioPlayerAlert.this, AndroidUtilities.dp(19.0F));
            AudioPlayerAlert.access$902(AudioPlayerAlert.this, getMeasuredHeight() - AudioPlayerAlert.this.playerLayout.getMeasuredHeight());
            AudioPlayerAlert.access$1102(AudioPlayerAlert.this, paramAnonymousInt2 / AudioPlayerAlert.this.placeholderImageView.getMeasuredWidth() - 1.0F);
            AudioPlayerAlert.access$1202(AudioPlayerAlert.this, ActionBar.getCurrentActionBarHeight() + AndroidUtilities.dp(5.0F));
            paramAnonymousInt2 = (int)Math.ceil(AudioPlayerAlert.this.placeholderImageView.getMeasuredHeight() * (1.0F + AudioPlayerAlert.this.thumbMaxScale));
            if (paramAnonymousInt2 > paramAnonymousInt1) {
              AudioPlayerAlert.access$1202(AudioPlayerAlert.this, AudioPlayerAlert.this.endTranslation - (paramAnonymousInt2 - paramAnonymousInt1));
            }
            return;
            paramAnonymousInt2 = 0;
            break;
            if (paramAnonymousInt2 < j)
            {
              paramAnonymousInt2 = j - paramAnonymousInt2;
              m = ActionBar.getCurrentActionBarHeight();
              if (Build.VERSION.SDK_INT < 21) {
                break label424;
              }
            }
            for (i = AndroidUtilities.statusBarHeight;; i = 0)
            {
              paramAnonymousInt2 += i + m;
              break;
              if (paramAnonymousInt2 < j) {}
              for (paramAnonymousInt2 = 0;; paramAnonymousInt2 = j - j / 5 * 3) {
                break;
              }
            }
            bool = false;
            break label157;
          }
        }
        
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          return (!AudioPlayerAlert.this.isDismissed()) && (super.onTouchEvent(paramAnonymousMotionEvent));
        }
        
        public void requestLayout()
        {
          if (this.ignoreLayout) {
            return;
          }
          super.requestLayout();
        }
      };
      this.containerView.setWillNotDraw(false);
      this.containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
      this.actionBar = new ActionBar(paramContext);
      this.actionBar.setBackgroundColor(Theme.getColor("player_actionBar"));
      this.actionBar.setBackButtonImage(2131165346);
      this.actionBar.setItemsColor(Theme.getColor("player_actionBarItems"), false);
      this.actionBar.setItemsBackgroundColor(Theme.getColor("player_actionBarSelector"), false);
      this.actionBar.setTitleColor(Theme.getColor("player_actionBarTitle"));
      this.actionBar.setSubtitleColor(Theme.getColor("player_actionBarSubtitle"));
      this.actionBar.setAlpha(0.0F);
      this.actionBar.setTitle("1");
      this.actionBar.setSubtitle("1");
      this.actionBar.getTitleTextView().setAlpha(0.0F);
      this.actionBar.getSubtitleTextView().setAlpha(0.0F);
      this.avatarContainer = new ChatAvatarContainer(paramContext, null, false);
      this.avatarContainer.setEnabled(false);
      this.avatarContainer.setTitleColors(Theme.getColor("player_actionBarTitle"), Theme.getColor("player_actionBarSubtitle"));
      if (localObject1 != null)
      {
        long l = ((MessageObject)localObject1).getDialogId();
        i = (int)l;
        j = (int)(l >> 32);
        if (i == 0) {
          break label2599;
        }
        if (i <= 0) {
          break label2553;
        }
        localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
        if (localObject1 != null)
        {
          this.avatarContainer.setTitle(ContactsController.formatName(((TLRPC.User)localObject1).first_name, ((TLRPC.User)localObject1).last_name));
          this.avatarContainer.setUserAvatar((TLRPC.User)localObject1);
        }
      }
    }
    for (;;)
    {
      this.avatarContainer.setSubtitle(LocaleController.getString("AudioTitle", 2131493046));
      this.actionBar.addView(this.avatarContainer, 0, LayoutHelper.createFrame(-2, -1.0F, 51, 56.0F, 0.0F, 40.0F, 0.0F));
      localObject1 = this.actionBar.createMenu();
      this.menuItem = ((ActionBarMenu)localObject1).addItem(0, 2131165353);
      this.menuItem.addSubItem(1, LocaleController.getString("Forward", 2131493548));
      this.menuItem.addSubItem(2, LocaleController.getString("ShareFile", 2131494383));
      this.menuItem.addSubItem(4, LocaleController.getString("ShowInChat", 2131494404));
      this.menuItem.setTranslationX(AndroidUtilities.dp(48.0F));
      this.menuItem.setAlpha(0.0F);
      this.searchItem = ((ActionBarMenu)localObject1).addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
      {
        public void onSearchCollapse()
        {
          AudioPlayerAlert.this.avatarContainer.setVisibility(0);
          if (AudioPlayerAlert.this.hasOptions) {
            AudioPlayerAlert.this.menuItem.setVisibility(4);
          }
          if (AudioPlayerAlert.this.searching)
          {
            AudioPlayerAlert.access$2202(AudioPlayerAlert.this, false);
            AudioPlayerAlert.access$402(AudioPlayerAlert.this, false);
            AudioPlayerAlert.this.setAllowNestedScroll(true);
            AudioPlayerAlert.this.listAdapter.search(null);
          }
        }
        
        public void onSearchExpand()
        {
          AudioPlayerAlert.access$2402(AudioPlayerAlert.this, AudioPlayerAlert.this.layoutManager.findLastVisibleItemPosition());
          View localView = AudioPlayerAlert.this.layoutManager.findViewByPosition(AudioPlayerAlert.this.searchOpenPosition);
          AudioPlayerAlert localAudioPlayerAlert = AudioPlayerAlert.this;
          if (localView == null) {}
          for (int i = 0;; i = localView.getTop())
          {
            AudioPlayerAlert.access$2602(localAudioPlayerAlert, i - AudioPlayerAlert.this.listView.getPaddingTop());
            AudioPlayerAlert.this.avatarContainer.setVisibility(8);
            if (AudioPlayerAlert.this.hasOptions) {
              AudioPlayerAlert.this.menuItem.setVisibility(8);
            }
            AudioPlayerAlert.access$402(AudioPlayerAlert.this, true);
            AudioPlayerAlert.this.setAllowNestedScroll(false);
            AudioPlayerAlert.this.listAdapter.notifyDataSetChanged();
            return;
          }
        }
        
        public void onTextChanged(EditText paramAnonymousEditText)
        {
          if (paramAnonymousEditText.length() > 0)
          {
            AudioPlayerAlert.this.listAdapter.search(paramAnonymousEditText.getText().toString());
            return;
          }
          AudioPlayerAlert.access$2202(AudioPlayerAlert.this, false);
          AudioPlayerAlert.this.listAdapter.search(null);
        }
      });
      localObject1 = this.searchItem.getSearchField();
      ((EditTextBoldCursor)localObject1).setHint(LocaleController.getString("Search", 2131494298));
      ((EditTextBoldCursor)localObject1).setTextColor(Theme.getColor("player_actionBarTitle"));
      ((EditTextBoldCursor)localObject1).setHintTextColor(Theme.getColor("player_time"));
      ((EditTextBoldCursor)localObject1).setCursorColor(Theme.getColor("player_actionBarTitle"));
      if (!AndroidUtilities.isTablet())
      {
        this.actionBar.showActionModeTop();
        this.actionBar.setActionModeTopColor(Theme.getColor("player_actionBarTop"));
      }
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1)
          {
            AudioPlayerAlert.this.dismiss();
            return;
          }
          AudioPlayerAlert.this.onSubItemClick(paramAnonymousInt);
        }
      });
      this.shadow = new View(paramContext);
      this.shadow.setAlpha(0.0F);
      this.shadow.setBackgroundResource(2131165342);
      this.shadow2 = new View(paramContext);
      this.shadow2.setAlpha(0.0F);
      this.shadow2.setBackgroundResource(2131165342);
      this.playerLayout = new FrameLayout(paramContext);
      this.playerLayout.setBackgroundColor(Theme.getColor("player_background"));
      this.placeholderImageView = new BackupImageView(paramContext)
      {
        private RectF rect = new RectF();
        
        protected void onDraw(Canvas paramAnonymousCanvas)
        {
          if (AudioPlayerAlert.this.hasNoCover)
          {
            this.rect.set(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight());
            paramAnonymousCanvas.drawRoundRect(this.rect, getRoundRadius(), getRoundRadius(), AudioPlayerAlert.this.paint);
            float f = AudioPlayerAlert.this.thumbMaxScale / getScaleX() / 3.0F;
            int i = (int)(AndroidUtilities.dp(63.0F) * Math.max(f / AudioPlayerAlert.this.thumbMaxScale, 1.0F / AudioPlayerAlert.this.thumbMaxScale));
            int j = (int)(this.rect.centerX() - i / 2);
            int k = (int)(this.rect.centerY() - i / 2);
            AudioPlayerAlert.this.noCoverDrawable.setBounds(j, k, j + i, k + i);
            AudioPlayerAlert.this.noCoverDrawable.draw(paramAnonymousCanvas);
            return;
          }
          super.onDraw(paramAnonymousCanvas);
        }
      };
      this.placeholderImageView.setRoundRadius(AndroidUtilities.dp(20.0F));
      this.placeholderImageView.setPivotX(0.0F);
      this.placeholderImageView.setPivotY(0.0F);
      this.placeholderImageView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          float f2 = 0.0F;
          float f1 = 0.0F;
          if (AudioPlayerAlert.this.animatorSet != null)
          {
            AudioPlayerAlert.this.animatorSet.cancel();
            AudioPlayerAlert.access$3102(AudioPlayerAlert.this, null);
          }
          AudioPlayerAlert.access$3102(AudioPlayerAlert.this, new AnimatorSet());
          Object localObject1;
          if (AudioPlayerAlert.this.scrollOffsetY <= AudioPlayerAlert.this.actionBar.getMeasuredHeight())
          {
            paramAnonymousView = AudioPlayerAlert.this.animatorSet;
            localObject1 = AudioPlayerAlert.this;
            if (AudioPlayerAlert.this.isInFullMode)
            {
              paramAnonymousView.playTogether(new Animator[] { ObjectAnimator.ofFloat(localObject1, "fullAnimationProgress", new float[] { f1 }) });
              AudioPlayerAlert.this.animatorSet.setInterpolator(new DecelerateInterpolator());
              AudioPlayerAlert.this.animatorSet.setDuration(250L);
              AudioPlayerAlert.this.animatorSet.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  if (paramAnonymous2Animator.equals(AudioPlayerAlert.this.animatorSet))
                  {
                    if (AudioPlayerAlert.this.isInFullMode) {
                      break label98;
                    }
                    AudioPlayerAlert.this.listView.setScrollEnabled(true);
                    if (AudioPlayerAlert.this.hasOptions) {
                      AudioPlayerAlert.this.menuItem.setVisibility(4);
                    }
                    AudioPlayerAlert.this.searchItem.setVisibility(0);
                  }
                  for (;;)
                  {
                    AudioPlayerAlert.access$3102(AudioPlayerAlert.this, null);
                    return;
                    label98:
                    if (AudioPlayerAlert.this.hasOptions) {
                      AudioPlayerAlert.this.menuItem.setVisibility(0);
                    }
                    AudioPlayerAlert.this.searchItem.setVisibility(4);
                  }
                }
              });
              AudioPlayerAlert.this.animatorSet.start();
              if (AudioPlayerAlert.this.hasOptions) {
                AudioPlayerAlert.this.menuItem.setVisibility(0);
              }
              AudioPlayerAlert.this.searchItem.setVisibility(0);
              paramAnonymousView = AudioPlayerAlert.this;
              if (AudioPlayerAlert.this.isInFullMode) {
                break label476;
              }
            }
          }
          label302:
          label339:
          label376:
          label461:
          label466:
          label471:
          label476:
          for (boolean bool = true;; bool = false)
          {
            AudioPlayerAlert.access$3202(paramAnonymousView, bool);
            AudioPlayerAlert.this.listView.setScrollEnabled(false);
            if (!AudioPlayerAlert.this.isInFullMode) {
              break label482;
            }
            AudioPlayerAlert.this.shuffleButton.setAdditionalOffset(-AndroidUtilities.dp(68.0F));
            return;
            f1 = 1.0F;
            break;
            paramAnonymousView = AudioPlayerAlert.this.animatorSet;
            localObject1 = AudioPlayerAlert.this;
            Object localObject2;
            Object localObject3;
            View localView;
            if (AudioPlayerAlert.this.isInFullMode)
            {
              f1 = 0.0F;
              localObject1 = ObjectAnimator.ofFloat(localObject1, "fullAnimationProgress", new float[] { f1 });
              localObject2 = AudioPlayerAlert.this.actionBar;
              if (!AudioPlayerAlert.this.isInFullMode) {
                break label461;
              }
              f1 = 0.0F;
              localObject2 = ObjectAnimator.ofFloat(localObject2, "alpha", new float[] { f1 });
              localObject3 = AudioPlayerAlert.this.shadow;
              if (!AudioPlayerAlert.this.isInFullMode) {
                break label466;
              }
              f1 = 0.0F;
              localObject3 = ObjectAnimator.ofFloat(localObject3, "alpha", new float[] { f1 });
              localView = AudioPlayerAlert.this.shadow2;
              if (!AudioPlayerAlert.this.isInFullMode) {
                break label471;
              }
            }
            for (f1 = f2;; f1 = 1.0F)
            {
              paramAnonymousView.playTogether(new Animator[] { localObject1, localObject2, localObject3, ObjectAnimator.ofFloat(localView, "alpha", new float[] { f1 }) });
              break;
              f1 = 1.0F;
              break label302;
              f1 = 1.0F;
              break label339;
              f1 = 1.0F;
              break label376;
            }
          }
          label482:
          AudioPlayerAlert.this.shuffleButton.setAdditionalOffset(-AndroidUtilities.dp(10.0F));
        }
      });
      this.titleTextView = new TextView(paramContext);
      this.titleTextView.setTextColor(Theme.getColor("player_actionBarTitle"));
      this.titleTextView.setTextSize(1, 15.0F);
      this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
      this.titleTextView.setSingleLine(true);
      this.playerLayout.addView(this.titleTextView, LayoutHelper.createFrame(-1, -2.0F, 51, 72.0F, 18.0F, 60.0F, 0.0F));
      this.authorTextView = new TextView(paramContext);
      this.authorTextView.setTextColor(Theme.getColor("player_time"));
      this.authorTextView.setTextSize(1, 14.0F);
      this.authorTextView.setEllipsize(TextUtils.TruncateAt.END);
      this.authorTextView.setSingleLine(true);
      this.playerLayout.addView(this.authorTextView, LayoutHelper.createFrame(-1, -2.0F, 51, 72.0F, 40.0F, 60.0F, 0.0F));
      this.optionsButton = new ActionBarMenuItem(paramContext, null, 0, Theme.getColor("player_actionBarItems"));
      this.optionsButton.setLongClickEnabled(false);
      this.optionsButton.setIcon(2131165353);
      this.optionsButton.setAdditionalOffset(-AndroidUtilities.dp(120.0F));
      this.playerLayout.addView(this.optionsButton, LayoutHelper.createFrame(40, 40.0F, 53, 0.0F, 19.0F, 10.0F, 0.0F));
      this.optionsButton.addSubItem(1, LocaleController.getString("Forward", 2131493548));
      this.optionsButton.addSubItem(2, LocaleController.getString("ShareFile", 2131494383));
      this.optionsButton.addSubItem(4, LocaleController.getString("ShowInChat", 2131494404));
      this.optionsButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          AudioPlayerAlert.this.optionsButton.toggleSubMenu();
        }
      });
      this.optionsButton.setDelegate(new ActionBarMenuItem.ActionBarMenuItemDelegate()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          AudioPlayerAlert.this.onSubItemClick(paramAnonymousInt);
        }
      });
      this.seekBarView = new SeekBarView(paramContext);
      this.seekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate()
      {
        public void onSeekBarDrag(float paramAnonymousFloat)
        {
          MediaController.getInstance().seekToProgress(MediaController.getInstance().getPlayingMessageObject(), paramAnonymousFloat);
        }
      });
      this.playerLayout.addView(this.seekBarView, LayoutHelper.createFrame(-1, 30.0F, 51, 8.0F, 62.0F, 8.0F, 0.0F));
      this.progressView = new LineProgressView(paramContext);
      this.progressView.setVisibility(4);
      this.progressView.setBackgroundColor(Theme.getColor("player_progressBackground"));
      this.progressView.setProgressColor(Theme.getColor("player_progress"));
      this.playerLayout.addView(this.progressView, LayoutHelper.createFrame(-1, 2.0F, 51, 20.0F, 78.0F, 20.0F, 0.0F));
      this.timeTextView = new SimpleTextView(paramContext);
      this.timeTextView.setTextSize(12);
      this.timeTextView.setTextColor(Theme.getColor("player_time"));
      this.playerLayout.addView(this.timeTextView, LayoutHelper.createFrame(100, -2.0F, 51, 20.0F, 92.0F, 0.0F, 0.0F));
      this.durationTextView = new TextView(paramContext);
      this.durationTextView.setTextSize(1, 12.0F);
      this.durationTextView.setTextColor(Theme.getColor("player_time"));
      this.durationTextView.setGravity(17);
      this.playerLayout.addView(this.durationTextView, LayoutHelper.createFrame(-2, -2.0F, 53, 0.0F, 90.0F, 20.0F, 0.0F));
      localObject1 = new FrameLayout(paramContext)
      {
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          paramAnonymousInt2 = (paramAnonymousInt3 - paramAnonymousInt1 - AndroidUtilities.dp(248.0F)) / 4;
          paramAnonymousInt1 = 0;
          while (paramAnonymousInt1 < 5)
          {
            paramAnonymousInt3 = AndroidUtilities.dp(paramAnonymousInt1 * 48 + 4) + paramAnonymousInt2 * paramAnonymousInt1;
            paramAnonymousInt4 = AndroidUtilities.dp(9.0F);
            AudioPlayerAlert.this.buttons[paramAnonymousInt1].layout(paramAnonymousInt3, paramAnonymousInt4, AudioPlayerAlert.this.buttons[paramAnonymousInt1].getMeasuredWidth() + paramAnonymousInt3, AudioPlayerAlert.this.buttons[paramAnonymousInt1].getMeasuredHeight() + paramAnonymousInt4);
            paramAnonymousInt1 += 1;
          }
        }
      };
      this.playerLayout.addView((View)localObject1, LayoutHelper.createFrame(-1, 66.0F, 51, 0.0F, 106.0F, 0.0F, 0.0F));
      Object localObject2 = this.buttons;
      Object localObject3 = new ActionBarMenuItem(paramContext, null, 0, 0);
      this.shuffleButton = ((ActionBarMenuItem)localObject3);
      localObject2[0] = localObject3;
      this.shuffleButton.setLongClickEnabled(false);
      this.shuffleButton.setAdditionalOffset(-AndroidUtilities.dp(10.0F));
      ((FrameLayout)localObject1).addView(this.shuffleButton, LayoutHelper.createFrame(48, 48, 51));
      this.shuffleButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          AudioPlayerAlert.this.shuffleButton.toggleSubMenu();
        }
      });
      localObject2 = this.shuffleButton.addSubItem(1, LocaleController.getString("ReverseOrder", 2131494275));
      ((TextView)localObject2).setPadding(AndroidUtilities.dp(8.0F), 0, AndroidUtilities.dp(16.0F), 0);
      this.playOrderButtons[0] = paramContext.getResources().getDrawable(2131165533).mutate();
      ((TextView)localObject2).setCompoundDrawablePadding(AndroidUtilities.dp(8.0F));
      ((TextView)localObject2).setCompoundDrawablesWithIntrinsicBounds(this.playOrderButtons[0], null, null, null);
      localObject2 = this.shuffleButton.addSubItem(2, LocaleController.getString("Shuffle", 2131494406));
      ((TextView)localObject2).setPadding(AndroidUtilities.dp(8.0F), 0, AndroidUtilities.dp(16.0F), 0);
      this.playOrderButtons[1] = paramContext.getResources().getDrawable(2131165604).mutate();
      ((TextView)localObject2).setCompoundDrawablePadding(AndroidUtilities.dp(8.0F));
      ((TextView)localObject2).setCompoundDrawablesWithIntrinsicBounds(this.playOrderButtons[1], null, null, null);
      this.shuffleButton.setDelegate(new ActionBarMenuItem.ActionBarMenuItemDelegate()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          MediaController.getInstance().toggleShuffleMusic(paramAnonymousInt);
          AudioPlayerAlert.this.updateShuffleButton();
          AudioPlayerAlert.this.listAdapter.notifyDataSetChanged();
        }
      });
      localObject2 = this.buttons;
      localObject3 = new ImageView(paramContext);
      localObject2[1] = localObject3;
      ((ImageView)localObject3).setScaleType(ImageView.ScaleType.CENTER);
      ((ImageView)localObject3).setImageDrawable(Theme.createSimpleSelectorDrawable(paramContext, 2131165601, Theme.getColor("player_button"), Theme.getColor("player_buttonActive")));
      ((FrameLayout)localObject1).addView((View)localObject3, LayoutHelper.createFrame(48, 48, 51));
      ((ImageView)localObject3).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          MediaController.getInstance().playPreviousMessage();
        }
      });
      localObject2 = this.buttons;
      localObject3 = new ImageView(paramContext);
      this.playButton = ((ImageView)localObject3);
      localObject2[2] = localObject3;
      this.playButton.setScaleType(ImageView.ScaleType.CENTER);
      this.playButton.setImageDrawable(Theme.createSimpleSelectorDrawable(paramContext, 2131165600, Theme.getColor("player_button"), Theme.getColor("player_buttonActive")));
      ((FrameLayout)localObject1).addView(this.playButton, LayoutHelper.createFrame(48, 48, 51));
      this.playButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (MediaController.getInstance().isDownloadingCurrentMessage()) {
            return;
          }
          if (MediaController.getInstance().isMessagePaused())
          {
            MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
            return;
          }
          MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
        }
      });
      localObject2 = this.buttons;
      localObject3 = new ImageView(paramContext);
      localObject2[3] = localObject3;
      ((ImageView)localObject3).setScaleType(ImageView.ScaleType.CENTER);
      ((ImageView)localObject3).setImageDrawable(Theme.createSimpleSelectorDrawable(paramContext, 2131165598, Theme.getColor("player_button"), Theme.getColor("player_buttonActive")));
      ((FrameLayout)localObject1).addView((View)localObject3, LayoutHelper.createFrame(48, 48, 51));
      ((ImageView)localObject3).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          MediaController.getInstance().playNextMessage();
        }
      });
      localObject2 = this.buttons;
      localObject3 = new ImageView(paramContext);
      this.repeatButton = ((ImageView)localObject3);
      localObject2[4] = localObject3;
      this.repeatButton.setScaleType(ImageView.ScaleType.CENTER);
      this.repeatButton.setPadding(0, 0, AndroidUtilities.dp(8.0F), 0);
      ((FrameLayout)localObject1).addView(this.repeatButton, LayoutHelper.createFrame(50, 48, 51));
      this.repeatButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          SharedConfig.toggleRepeatMode();
          AudioPlayerAlert.this.updateRepeatButton();
        }
      });
      this.listView = new RecyclerListView(paramContext)
      {
        boolean ignoreLayout;
        
        protected boolean allowSelectChildAtPosition(float paramAnonymousFloat1, float paramAnonymousFloat2)
        {
          AudioPlayerAlert.this.playerLayout.getY();
          paramAnonymousFloat1 = AudioPlayerAlert.this.playerLayout.getMeasuredHeight();
          return (AudioPlayerAlert.this.playerLayout == null) || (paramAnonymousFloat2 > AudioPlayerAlert.this.playerLayout.getY() + AudioPlayerAlert.this.playerLayout.getMeasuredHeight());
        }
        
        public boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          paramAnonymousCanvas.save();
          if (AudioPlayerAlert.this.actionBar != null) {}
          for (int i = AudioPlayerAlert.this.actionBar.getMeasuredHeight();; i = 0)
          {
            paramAnonymousCanvas.clipRect(0, i + AndroidUtilities.dp(50.0F), getMeasuredWidth(), getMeasuredHeight());
            boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
            paramAnonymousCanvas.restore();
            return bool;
          }
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          if ((AudioPlayerAlert.this.searchOpenPosition != -1) && (!AudioPlayerAlert.this.actionBar.isSearchFieldVisible()))
          {
            this.ignoreLayout = true;
            AudioPlayerAlert.this.layoutManager.scrollToPositionWithOffset(AudioPlayerAlert.this.searchOpenPosition, AudioPlayerAlert.this.searchOpenOffset);
            super.onLayout(false, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
            this.ignoreLayout = false;
          }
          label89:
          int i;
          do
          {
            MessageObject localMessageObject;
            int j;
            do
            {
              int k;
              do
              {
                AudioPlayerAlert.access$2402(AudioPlayerAlert.this, -1);
                break label89;
                do
                {
                  return;
                } while (!AudioPlayerAlert.this.scrollToSong);
                AudioPlayerAlert.access$4002(AudioPlayerAlert.this, false);
                k = 0;
                localMessageObject = MediaController.getInstance().getPlayingMessageObject();
              } while (localMessageObject == null);
              int m = AudioPlayerAlert.this.listView.getChildCount();
              i = 0;
              j = k;
              if (i < m)
              {
                View localView = AudioPlayerAlert.this.listView.getChildAt(i);
                if ((!(localView instanceof AudioPlayerCell)) || (((AudioPlayerCell)localView).getMessageObject() != localMessageObject)) {
                  break;
                }
                j = k;
                if (localView.getBottom() <= getMeasuredHeight()) {
                  j = 1;
                }
              }
            } while (j != 0);
            i = AudioPlayerAlert.this.playlist.indexOf(localMessageObject);
          } while (i < 0);
          this.ignoreLayout = true;
          if (SharedConfig.playOrderReversed) {
            AudioPlayerAlert.this.layoutManager.scrollToPosition(i);
          }
          for (;;)
          {
            super.onLayout(false, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
            this.ignoreLayout = false;
            return;
            i += 1;
            break;
            AudioPlayerAlert.this.layoutManager.scrollToPosition(AudioPlayerAlert.this.playlist.size() - i);
          }
        }
        
        public void requestLayout()
        {
          if (this.ignoreLayout) {
            return;
          }
          super.requestLayout();
        }
      };
      this.listView.setPadding(0, 0, 0, AndroidUtilities.dp(8.0F));
      this.listView.setClipToPadding(false);
      localObject1 = this.listView;
      localObject2 = new LinearLayoutManager(getContext(), 1, false);
      this.layoutManager = ((LinearLayoutManager)localObject2);
      ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
      this.listView.setHorizontalScrollBarEnabled(false);
      this.listView.setVerticalScrollBarEnabled(false);
      this.containerView.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
      localObject1 = this.listView;
      paramContext = new ListAdapter(paramContext);
      this.listAdapter = paramContext;
      ((RecyclerListView)localObject1).setAdapter(paramContext);
      this.listView.setGlowColor(Theme.getColor("dialogScrollGlow"));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousView instanceof AudioPlayerCell)) {
            ((AudioPlayerCell)paramAnonymousView).didPressedButton();
          }
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (AudioPlayerAlert.this.searching) && (AudioPlayerAlert.this.searchWas)) {
            AndroidUtilities.hideKeyboard(AudioPlayerAlert.this.getCurrentFocus());
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          AudioPlayerAlert.this.updateLayout();
        }
      });
      this.playlist = MediaController.getInstance().getPlaylist();
      this.listAdapter.notifyDataSetChanged();
      this.containerView.addView(this.playerLayout, LayoutHelper.createFrame(-1, 178.0F));
      this.containerView.addView(this.shadow2, LayoutHelper.createFrame(-1, 3.0F));
      this.containerView.addView(this.placeholderImageView, LayoutHelper.createFrame(40, 40.0F, 51, 17.0F, 19.0F, 0.0F, 0.0F));
      this.containerView.addView(this.shadow, LayoutHelper.createFrame(-1, 3.0F));
      this.containerView.addView(this.actionBar);
      updateTitle(false);
      updateRepeatButton();
      updateShuffleButton();
      return;
      this.currentAccount = UserConfig.selectedAccount;
      break;
      label2553:
      localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      if (localObject1 != null)
      {
        this.avatarContainer.setTitle(((TLRPC.Chat)localObject1).title);
        this.avatarContainer.setChatAvatar((TLRPC.Chat)localObject1);
        continue;
        label2599:
        localObject1 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(j));
        if (localObject1 != null)
        {
          localObject1 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.EncryptedChat)localObject1).user_id));
          if (localObject1 != null)
          {
            this.avatarContainer.setTitle(ContactsController.formatName(((TLRPC.User)localObject1).first_name, ((TLRPC.User)localObject1).last_name));
            this.avatarContainer.setUserAvatar((TLRPC.User)localObject1);
          }
        }
      }
    }
  }
  
  private void checkIfMusicDownloaded(MessageObject paramMessageObject)
  {
    Object localObject2 = null;
    Object localObject1 = localObject2;
    if (paramMessageObject.messageOwner.attachPath != null)
    {
      localObject1 = localObject2;
      if (paramMessageObject.messageOwner.attachPath.length() > 0)
      {
        localObject2 = new File(paramMessageObject.messageOwner.attachPath);
        localObject1 = localObject2;
        if (!((File)localObject2).exists()) {
          localObject1 = null;
        }
      }
    }
    localObject2 = localObject1;
    if (localObject1 == null) {
      localObject2 = FileLoader.getPathToMessage(paramMessageObject.messageOwner);
    }
    int i;
    if ((SharedConfig.streamMedia) && ((int)paramMessageObject.getDialogId() != 0) && (paramMessageObject.isMusic()))
    {
      i = 1;
      if ((((File)localObject2).exists()) || (i != 0)) {
        break label200;
      }
      paramMessageObject = paramMessageObject.getFileName();
      DownloadController.getInstance(this.currentAccount).addLoadingFileObserver(paramMessageObject, this);
      paramMessageObject = ImageLoader.getInstance().getFileProgress(paramMessageObject);
      localObject1 = this.progressView;
      if (paramMessageObject == null) {
        break label195;
      }
    }
    label195:
    for (float f = paramMessageObject.floatValue();; f = 0.0F)
    {
      ((LineProgressView)localObject1).setProgress(f, false);
      this.progressView.setVisibility(0);
      this.seekBarView.setVisibility(4);
      this.playButton.setEnabled(false);
      return;
      i = 0;
      break;
    }
    label200:
    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
    this.progressView.setVisibility(4);
    this.seekBarView.setVisibility(0);
    this.playButton.setEnabled(true);
  }
  
  private int getCurrentTop()
  {
    int j = 0;
    if (this.listView.getChildCount() != 0)
    {
      View localView = this.listView.getChildAt(0);
      RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.listView.findContainingViewHolder(localView);
      if (localHolder != null)
      {
        int k = this.listView.getPaddingTop();
        int i = j;
        if (localHolder.getAdapterPosition() == 0)
        {
          i = j;
          if (localView.getTop() >= 0) {
            i = localView.getTop();
          }
        }
        return k - i;
      }
    }
    return 64536;
  }
  
  /* Error */
  private void onSubItemClick(int paramInt)
  {
    // Byte code:
    //   0: invokestatic 181	org/telegram/messenger/MediaController:getInstance	()Lorg/telegram/messenger/MediaController;
    //   3: invokevirtual 185	org/telegram/messenger/MediaController:getPlayingMessageObject	()Lorg/telegram/messenger/MessageObject;
    //   6: astore 8
    //   8: aload 8
    //   10: ifnull +10 -> 20
    //   13: aload_0
    //   14: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   17: ifnonnull +4 -> 21
    //   20: return
    //   21: iload_1
    //   22: iconst_1
    //   23: if_icmpne +109 -> 132
    //   26: getstatic 825	org/telegram/messenger/UserConfig:selectedAccount	I
    //   29: aload_0
    //   30: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   33: if_icmpeq +15 -> 48
    //   36: aload_0
    //   37: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   40: aload_0
    //   41: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   44: iconst_1
    //   45: invokevirtual 1062	org/telegram/ui/LaunchActivity:switchToAccount	(IZ)V
    //   48: new 1064	android/os/Bundle
    //   51: dup
    //   52: invokespecial 1065	android/os/Bundle:<init>	()V
    //   55: astore 6
    //   57: aload 6
    //   59: ldc_w 1067
    //   62: iconst_1
    //   63: invokevirtual 1071	android/os/Bundle:putBoolean	(Ljava/lang/String;Z)V
    //   66: aload 6
    //   68: ldc_w 1073
    //   71: iconst_3
    //   72: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   75: new 1079	org/telegram/ui/DialogsActivity
    //   78: dup
    //   79: aload 6
    //   81: invokespecial 1082	org/telegram/ui/DialogsActivity:<init>	(Landroid/os/Bundle;)V
    //   84: astore 6
    //   86: new 167	java/util/ArrayList
    //   89: dup
    //   90: invokespecial 170	java/util/ArrayList:<init>	()V
    //   93: astore 7
    //   95: aload 7
    //   97: aload 8
    //   99: invokevirtual 1086	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   102: pop
    //   103: aload 6
    //   105: new 30	org/telegram/ui/Components/AudioPlayerAlert$19
    //   108: dup
    //   109: aload_0
    //   110: aload 7
    //   112: invokespecial 1089	org/telegram/ui/Components/AudioPlayerAlert$19:<init>	(Lorg/telegram/ui/Components/AudioPlayerAlert;Ljava/util/ArrayList;)V
    //   115: invokevirtual 1092	org/telegram/ui/DialogsActivity:setDelegate	(Lorg/telegram/ui/DialogsActivity$DialogsActivityDelegate;)V
    //   118: aload_0
    //   119: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   122: aload 6
    //   124: invokevirtual 1096	org/telegram/ui/LaunchActivity:presentFragment	(Lorg/telegram/ui/ActionBar/BaseFragment;)V
    //   127: aload_0
    //   128: invokevirtual 1099	org/telegram/ui/Components/AudioPlayerAlert:dismiss	()V
    //   131: return
    //   132: iload_1
    //   133: iconst_2
    //   134: if_icmpne +289 -> 423
    //   137: aconst_null
    //   138: astore 6
    //   140: aload 8
    //   142: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   145: getfield 974	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   148: invokestatic 1105	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   151: ifne +33 -> 184
    //   154: new 981	java/io/File
    //   157: dup
    //   158: aload 8
    //   160: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   163: getfield 974	org/telegram/tgnet/TLRPC$Message:attachPath	Ljava/lang/String;
    //   166: invokespecial 984	java/io/File:<init>	(Ljava/lang/String;)V
    //   169: astore 6
    //   171: aload 6
    //   173: invokevirtual 987	java/io/File:exists	()Z
    //   176: istore_3
    //   177: iload_3
    //   178: ifne +912 -> 1090
    //   181: aconst_null
    //   182: astore 6
    //   184: aload 6
    //   186: astore 7
    //   188: aload 6
    //   190: ifnonnull +13 -> 203
    //   193: aload 8
    //   195: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   198: invokestatic 993	org/telegram/messenger/FileLoader:getPathToMessage	(Lorg/telegram/tgnet/TLRPC$Message;)Ljava/io/File;
    //   201: astore 7
    //   203: aload 7
    //   205: invokevirtual 987	java/io/File:exists	()Z
    //   208: ifeq +149 -> 357
    //   211: new 1107	android/content/Intent
    //   214: dup
    //   215: ldc_w 1109
    //   218: invokespecial 1110	android/content/Intent:<init>	(Ljava/lang/String;)V
    //   221: astore 6
    //   223: aload 8
    //   225: ifnull +84 -> 309
    //   228: aload 6
    //   230: aload 8
    //   232: invokevirtual 1113	org/telegram/messenger/MessageObject:getMimeType	()Ljava/lang/String;
    //   235: invokevirtual 1117	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
    //   238: pop
    //   239: getstatic 1122	android/os/Build$VERSION:SDK_INT	I
    //   242: istore_1
    //   243: iload_1
    //   244: bipush 24
    //   246: if_icmplt +94 -> 340
    //   249: aload 6
    //   251: ldc_w 1124
    //   254: getstatic 1130	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   257: ldc_w 1132
    //   260: aload 7
    //   262: invokestatic 1138	android/support/v4/content/FileProvider:getUriForFile	(Landroid/content/Context;Ljava/lang/String;Ljava/io/File;)Landroid/net/Uri;
    //   265: invokevirtual 1142	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   268: pop
    //   269: aload 6
    //   271: iconst_1
    //   272: invokevirtual 1146	android/content/Intent:setFlags	(I)Landroid/content/Intent;
    //   275: pop
    //   276: aload_0
    //   277: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   280: aload 6
    //   282: ldc_w 461
    //   285: ldc_w 462
    //   288: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   291: invokestatic 1150	android/content/Intent:createChooser	(Landroid/content/Intent;Ljava/lang/CharSequence;)Landroid/content/Intent;
    //   294: sipush 500
    //   297: invokevirtual 1154	org/telegram/ui/LaunchActivity:startActivityForResult	(Landroid/content/Intent;I)V
    //   300: return
    //   301: astore 6
    //   303: aload 6
    //   305: invokestatic 1160	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   308: return
    //   309: aload 6
    //   311: ldc_w 1162
    //   314: invokevirtual 1117	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
    //   317: pop
    //   318: goto -79 -> 239
    //   321: astore 8
    //   323: aload 6
    //   325: ldc_w 1124
    //   328: aload 7
    //   330: invokestatic 1168	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   333: invokevirtual 1142	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   336: pop
    //   337: goto -61 -> 276
    //   340: aload 6
    //   342: ldc_w 1124
    //   345: aload 7
    //   347: invokestatic 1168	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
    //   350: invokevirtual 1142	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    //   353: pop
    //   354: goto -78 -> 276
    //   357: new 1170	org/telegram/ui/ActionBar/AlertDialog$Builder
    //   360: dup
    //   361: aload_0
    //   362: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   365: invokespecial 1171	org/telegram/ui/ActionBar/AlertDialog$Builder:<init>	(Landroid/content/Context;)V
    //   368: astore 6
    //   370: aload 6
    //   372: ldc_w 1173
    //   375: ldc_w 1174
    //   378: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   381: invokevirtual 1177	org/telegram/ui/ActionBar/AlertDialog$Builder:setTitle	(Ljava/lang/CharSequence;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   384: pop
    //   385: aload 6
    //   387: ldc_w 1179
    //   390: ldc_w 1180
    //   393: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   396: aconst_null
    //   397: invokevirtual 1184	org/telegram/ui/ActionBar/AlertDialog$Builder:setPositiveButton	(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   400: pop
    //   401: aload 6
    //   403: ldc_w 1186
    //   406: ldc_w 1187
    //   409: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   412: invokevirtual 1190	org/telegram/ui/ActionBar/AlertDialog$Builder:setMessage	(Ljava/lang/CharSequence;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   415: pop
    //   416: aload 6
    //   418: invokevirtual 1194	org/telegram/ui/ActionBar/AlertDialog$Builder:show	()Lorg/telegram/ui/ActionBar/AlertDialog;
    //   421: pop
    //   422: return
    //   423: iload_1
    //   424: iconst_3
    //   425: if_icmpne +436 -> 861
    //   428: new 1170	org/telegram/ui/ActionBar/AlertDialog$Builder
    //   431: dup
    //   432: aload_0
    //   433: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   436: invokespecial 1171	org/telegram/ui/ActionBar/AlertDialog$Builder:<init>	(Landroid/content/Context;)V
    //   439: astore 9
    //   441: aload 9
    //   443: ldc_w 1173
    //   446: ldc_w 1174
    //   449: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   452: invokevirtual 1177	org/telegram/ui/ActionBar/AlertDialog$Builder:setTitle	(Ljava/lang/CharSequence;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   455: pop
    //   456: iconst_1
    //   457: newarray <illegal type>
    //   459: astore 10
    //   461: aload 8
    //   463: invokevirtual 379	org/telegram/messenger/MessageObject:getDialogId	()J
    //   466: l2i
    //   467: istore_1
    //   468: iload_1
    //   469: ifeq +265 -> 734
    //   472: iload_1
    //   473: ifle +311 -> 784
    //   476: aload_0
    //   477: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   480: invokestatic 384	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   483: iload_1
    //   484: invokestatic 390	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   487: invokevirtual 394	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   490: astore 7
    //   492: aconst_null
    //   493: astore 6
    //   495: aload 7
    //   497: ifnonnull +11 -> 508
    //   500: aload 6
    //   502: invokestatic 1200	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   505: ifne +229 -> 734
    //   508: aload_0
    //   509: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   512: invokestatic 1205	org/telegram/tgnet/ConnectionsManager:getInstance	(I)Lorg/telegram/tgnet/ConnectionsManager;
    //   515: invokevirtual 1208	org/telegram/tgnet/ConnectionsManager:getCurrentTime	()I
    //   518: istore_1
    //   519: aload 7
    //   521: ifnull +21 -> 542
    //   524: aload 7
    //   526: getfield 1211	org/telegram/tgnet/TLRPC$User:id	I
    //   529: aload_0
    //   530: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   533: invokestatic 1214	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   536: invokevirtual 1217	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   539: if_icmpne +8 -> 547
    //   542: aload 6
    //   544: ifnull +190 -> 734
    //   547: aload 8
    //   549: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   552: getfield 1221	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
    //   555: ifnull +17 -> 572
    //   558: aload 8
    //   560: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   563: getfield 1221	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
    //   566: instanceof 1223
    //   569: ifeq +165 -> 734
    //   572: aload 8
    //   574: invokevirtual 1226	org/telegram/messenger/MessageObject:isOut	()Z
    //   577: ifeq +157 -> 734
    //   580: iload_1
    //   581: aload 8
    //   583: getfield 969	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   586: getfield 1229	org/telegram/tgnet/TLRPC$Message:date	I
    //   589: isub
    //   590: ldc_w 1230
    //   593: if_icmpgt +141 -> 734
    //   596: new 542	android/widget/FrameLayout
    //   599: dup
    //   600: aload_0
    //   601: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   604: invokespecial 543	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   607: astore 11
    //   609: new 1232	org/telegram/ui/Cells/CheckBoxCell
    //   612: dup
    //   613: aload_0
    //   614: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   617: iconst_1
    //   618: invokespecial 1235	org/telegram/ui/Cells/CheckBoxCell:<init>	(Landroid/content/Context;I)V
    //   621: astore 12
    //   623: aload 12
    //   625: iconst_0
    //   626: invokestatic 1239	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   629: invokevirtual 1242	org/telegram/ui/Cells/CheckBoxCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   632: aload 6
    //   634: ifnull +173 -> 807
    //   637: aload 12
    //   639: ldc_w 1244
    //   642: ldc_w 1245
    //   645: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   648: ldc_w 1247
    //   651: iconst_0
    //   652: iconst_0
    //   653: invokevirtual 1251	org/telegram/ui/Cells/CheckBoxCell:setText	(Ljava/lang/String;Ljava/lang/String;ZZ)V
    //   656: getstatic 1254	org/telegram/messenger/LocaleController:isRTL	Z
    //   659: ifeq +182 -> 841
    //   662: ldc_w 690
    //   665: invokestatic 472	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   668: istore_1
    //   669: getstatic 1254	org/telegram/messenger/LocaleController:isRTL	Z
    //   672: ifeq +179 -> 851
    //   675: ldc_w 645
    //   678: invokestatic 472	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   681: istore_2
    //   682: aload 12
    //   684: iload_1
    //   685: iconst_0
    //   686: iload_2
    //   687: iconst_0
    //   688: invokevirtual 1255	org/telegram/ui/Cells/CheckBoxCell:setPadding	(IIII)V
    //   691: aload 11
    //   693: aload 12
    //   695: iconst_m1
    //   696: ldc_w 466
    //   699: bipush 51
    //   701: fconst_0
    //   702: fconst_0
    //   703: fconst_0
    //   704: fconst_0
    //   705: invokestatic 433	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   708: invokevirtual 607	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   711: aload 12
    //   713: new 34	org/telegram/ui/Components/AudioPlayerAlert$20
    //   716: dup
    //   717: aload_0
    //   718: aload 10
    //   720: invokespecial 1258	org/telegram/ui/Components/AudioPlayerAlert$20:<init>	(Lorg/telegram/ui/Components/AudioPlayerAlert;[Z)V
    //   723: invokevirtual 1259	org/telegram/ui/Cells/CheckBoxCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   726: aload 9
    //   728: aload 11
    //   730: invokevirtual 1263	org/telegram/ui/ActionBar/AlertDialog$Builder:setView	(Landroid/view/View;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   733: pop
    //   734: aload 9
    //   736: ldc_w 1179
    //   739: ldc_w 1180
    //   742: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   745: new 36	org/telegram/ui/Components/AudioPlayerAlert$21
    //   748: dup
    //   749: aload_0
    //   750: aload 8
    //   752: aload 10
    //   754: invokespecial 1266	org/telegram/ui/Components/AudioPlayerAlert$21:<init>	(Lorg/telegram/ui/Components/AudioPlayerAlert;Lorg/telegram/messenger/MessageObject;[Z)V
    //   757: invokevirtual 1184	org/telegram/ui/ActionBar/AlertDialog$Builder:setPositiveButton	(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   760: pop
    //   761: aload 9
    //   763: ldc_w 1268
    //   766: ldc_w 1269
    //   769: invokestatic 423	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   772: aconst_null
    //   773: invokevirtual 1272	org/telegram/ui/ActionBar/AlertDialog$Builder:setNegativeButton	(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
    //   776: pop
    //   777: aload 9
    //   779: invokevirtual 1194	org/telegram/ui/ActionBar/AlertDialog$Builder:show	()Lorg/telegram/ui/ActionBar/AlertDialog;
    //   782: pop
    //   783: return
    //   784: aconst_null
    //   785: astore 7
    //   787: aload_0
    //   788: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   791: invokestatic 384	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   794: iload_1
    //   795: ineg
    //   796: invokestatic 390	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   799: invokevirtual 829	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   802: astore 6
    //   804: goto -309 -> 495
    //   807: aload 12
    //   809: ldc_w 1274
    //   812: ldc_w 1275
    //   815: iconst_1
    //   816: anewarray 1277	java/lang/Object
    //   819: dup
    //   820: iconst_0
    //   821: aload 7
    //   823: invokestatic 1283	org/telegram/messenger/UserObject:getFirstName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   826: aastore
    //   827: invokestatic 1287	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   830: ldc_w 1247
    //   833: iconst_0
    //   834: iconst_0
    //   835: invokevirtual 1251	org/telegram/ui/Cells/CheckBoxCell:setText	(Ljava/lang/String;Ljava/lang/String;ZZ)V
    //   838: goto -182 -> 656
    //   841: ldc_w 645
    //   844: invokestatic 472	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   847: istore_1
    //   848: goto -179 -> 669
    //   851: ldc_w 690
    //   854: invokestatic 472	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   857: istore_2
    //   858: goto -176 -> 682
    //   861: iload_1
    //   862: iconst_4
    //   863: if_icmpne -843 -> 20
    //   866: getstatic 825	org/telegram/messenger/UserConfig:selectedAccount	I
    //   869: aload_0
    //   870: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   873: if_icmpeq +15 -> 888
    //   876: aload_0
    //   877: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   880: aload_0
    //   881: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   884: iconst_1
    //   885: invokevirtual 1062	org/telegram/ui/LaunchActivity:switchToAccount	(IZ)V
    //   888: new 1064	android/os/Bundle
    //   891: dup
    //   892: invokespecial 1065	android/os/Bundle:<init>	()V
    //   895: astore 6
    //   897: aload 8
    //   899: invokevirtual 379	org/telegram/messenger/MessageObject:getDialogId	()J
    //   902: lstore 4
    //   904: lload 4
    //   906: l2i
    //   907: istore_2
    //   908: lload 4
    //   910: bipush 32
    //   912: lshr
    //   913: l2i
    //   914: istore_1
    //   915: iload_2
    //   916: ifeq +157 -> 1073
    //   919: iload_1
    //   920: iconst_1
    //   921: if_icmpne +66 -> 987
    //   924: aload 6
    //   926: ldc_w 1289
    //   929: iload_2
    //   930: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   933: aload 6
    //   935: ldc_w 1291
    //   938: aload 8
    //   940: invokevirtual 1294	org/telegram/messenger/MessageObject:getId	()I
    //   943: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   946: aload_0
    //   947: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   950: invokestatic 253	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   953: getstatic 1297	org/telegram/messenger/NotificationCenter:closeChats	I
    //   956: iconst_0
    //   957: anewarray 1277	java/lang/Object
    //   960: invokevirtual 1301	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   963: aload_0
    //   964: getfield 195	org/telegram/ui/Components/AudioPlayerAlert:parentActivity	Lorg/telegram/ui/LaunchActivity;
    //   967: new 1303	org/telegram/ui/ChatActivity
    //   970: dup
    //   971: aload 6
    //   973: invokespecial 1304	org/telegram/ui/ChatActivity:<init>	(Landroid/os/Bundle;)V
    //   976: iconst_0
    //   977: iconst_0
    //   978: invokevirtual 1307	org/telegram/ui/LaunchActivity:presentFragment	(Lorg/telegram/ui/ActionBar/BaseFragment;ZZ)Z
    //   981: pop
    //   982: aload_0
    //   983: invokevirtual 1099	org/telegram/ui/Components/AudioPlayerAlert:dismiss	()V
    //   986: return
    //   987: iload_2
    //   988: ifle +15 -> 1003
    //   991: aload 6
    //   993: ldc_w 1308
    //   996: iload_2
    //   997: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   1000: goto -67 -> 933
    //   1003: iload_2
    //   1004: ifge -71 -> 933
    //   1007: aload_0
    //   1008: getfield 191	org/telegram/ui/Components/AudioPlayerAlert:currentAccount	I
    //   1011: invokestatic 384	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   1014: iload_2
    //   1015: ineg
    //   1016: invokestatic 390	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   1019: invokevirtual 829	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   1022: astore 7
    //   1024: iload_2
    //   1025: istore_1
    //   1026: aload 7
    //   1028: ifnull +32 -> 1060
    //   1031: iload_2
    //   1032: istore_1
    //   1033: aload 7
    //   1035: getfield 1312	org/telegram/tgnet/TLRPC$Chat:migrated_to	Lorg/telegram/tgnet/TLRPC$InputChannel;
    //   1038: ifnull +22 -> 1060
    //   1041: aload 6
    //   1043: ldc_w 1313
    //   1046: iload_2
    //   1047: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   1050: aload 7
    //   1052: getfield 1312	org/telegram/tgnet/TLRPC$Chat:migrated_to	Lorg/telegram/tgnet/TLRPC$InputChannel;
    //   1055: getfield 1318	org/telegram/tgnet/TLRPC$InputChannel:channel_id	I
    //   1058: ineg
    //   1059: istore_1
    //   1060: aload 6
    //   1062: ldc_w 1289
    //   1065: iload_1
    //   1066: ineg
    //   1067: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   1070: goto -137 -> 933
    //   1073: aload 6
    //   1075: ldc_w 1320
    //   1078: iload_1
    //   1079: invokevirtual 1077	android/os/Bundle:putInt	(Ljava/lang/String;I)V
    //   1082: goto -149 -> 933
    //   1085: astore 6
    //   1087: goto -784 -> 303
    //   1090: goto -906 -> 184
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1093	0	this	AudioPlayerAlert
    //   0	1093	1	paramInt	int
    //   681	366	2	i	int
    //   176	2	3	bool	boolean
    //   902	7	4	l	long
    //   55	226	6	localObject1	Object
    //   301	40	6	localException1	Exception
    //   368	706	6	localObject2	Object
    //   1085	1	6	localException2	Exception
    //   93	958	7	localObject3	Object
    //   6	225	8	localMessageObject	MessageObject
    //   321	618	8	localException3	Exception
    //   439	339	9	localBuilder	org.telegram.ui.ActionBar.AlertDialog.Builder
    //   459	294	10	arrayOfBoolean	boolean[]
    //   607	122	11	localFrameLayout	FrameLayout
    //   621	187	12	localCheckBoxCell	CheckBoxCell
    // Exception table:
    //   from	to	target	type
    //   140	171	301	java/lang/Exception
    //   193	203	301	java/lang/Exception
    //   203	223	301	java/lang/Exception
    //   228	239	301	java/lang/Exception
    //   239	243	301	java/lang/Exception
    //   276	300	301	java/lang/Exception
    //   309	318	301	java/lang/Exception
    //   323	337	301	java/lang/Exception
    //   340	354	301	java/lang/Exception
    //   357	422	301	java/lang/Exception
    //   249	276	321	java/lang/Exception
    //   171	177	1085	java/lang/Exception
  }
  
  private void updateLayout()
  {
    if (this.listView.getChildCount() <= 0) {
      return;
    }
    Object localObject = this.listView.getChildAt(0);
    RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.listView.findContainingViewHolder((View)localObject);
    int i = ((View)localObject).getTop();
    if ((i > 0) && (localHolder != null) && (localHolder.getAdapterPosition() == 0))
    {
      if ((this.searchWas) || (this.searching)) {
        i = 0;
      }
      if (this.scrollOffsetY != i)
      {
        localObject = this.listView;
        this.scrollOffsetY = i;
        ((RecyclerListView)localObject).setTopGlowOffset(i);
        this.playerLayout.setTranslationY(Math.max(this.actionBar.getMeasuredHeight(), this.scrollOffsetY));
        this.placeholderImageView.setTranslationY(Math.max(this.actionBar.getMeasuredHeight(), this.scrollOffsetY));
        this.shadow2.setTranslationY(Math.max(this.actionBar.getMeasuredHeight(), this.scrollOffsetY) + this.playerLayout.getMeasuredHeight());
        this.containerView.invalidate();
        if (((!this.inFullSize) || (this.scrollOffsetY > this.actionBar.getMeasuredHeight())) && (!this.searchWas)) {
          break label379;
        }
        if (this.actionBar.getTag() == null)
        {
          if (this.actionBarAnimation != null) {
            this.actionBarAnimation.cancel();
          }
          this.actionBar.setTag(Integer.valueOf(1));
          this.actionBarAnimation = new AnimatorSet();
          this.actionBarAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.actionBar, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.shadow, "alpha", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.shadow2, "alpha", new float[] { 1.0F }) });
          this.actionBarAnimation.setDuration(180L);
          this.actionBarAnimation.start();
        }
      }
    }
    for (;;)
    {
      this.startTranslation = Math.max(this.actionBar.getMeasuredHeight(), this.scrollOffsetY);
      this.panelStartTranslation = Math.max(this.actionBar.getMeasuredHeight(), this.scrollOffsetY);
      return;
      i = 0;
      break;
      label379:
      if (this.actionBar.getTag() != null)
      {
        if (this.actionBarAnimation != null) {
          this.actionBarAnimation.cancel();
        }
        this.actionBar.setTag(null);
        this.actionBarAnimation = new AnimatorSet();
        this.actionBarAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.actionBar, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.shadow, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.shadow2, "alpha", new float[] { 0.0F }) });
        this.actionBarAnimation.setDuration(180L);
        this.actionBarAnimation.start();
      }
    }
  }
  
  private void updateProgress(MessageObject paramMessageObject)
  {
    if (this.seekBarView != null)
    {
      if (!this.seekBarView.isDragging())
      {
        this.seekBarView.setProgress(paramMessageObject.audioProgress);
        this.seekBarView.setBufferedProgress(paramMessageObject.bufferedProgress);
      }
      if (this.lastTime != paramMessageObject.audioProgressSec)
      {
        this.lastTime = paramMessageObject.audioProgressSec;
        this.timeTextView.setText(String.format("%d:%02d", new Object[] { Integer.valueOf(paramMessageObject.audioProgressSec / 60), Integer.valueOf(paramMessageObject.audioProgressSec % 60) }));
      }
    }
  }
  
  private void updateRepeatButton()
  {
    int i = SharedConfig.repeatMode;
    if (i == 0)
    {
      this.repeatButton.setImageResource(2131165602);
      this.repeatButton.setTag("player_button");
      this.repeatButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_button"), PorterDuff.Mode.MULTIPLY));
    }
    do
    {
      return;
      if (i == 1)
      {
        this.repeatButton.setImageResource(2131165602);
        this.repeatButton.setTag("player_buttonActive");
        this.repeatButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_buttonActive"), PorterDuff.Mode.MULTIPLY));
        return;
      }
    } while (i != 2);
    this.repeatButton.setImageResource(2131165603);
    this.repeatButton.setTag("player_buttonActive");
    this.repeatButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_buttonActive"), PorterDuff.Mode.MULTIPLY));
  }
  
  private void updateShuffleButton()
  {
    Drawable localDrawable;
    if (SharedConfig.shuffleMusic)
    {
      localObject = getContext().getResources().getDrawable(2131165604).mutate();
      ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_buttonActive"), PorterDuff.Mode.MULTIPLY));
      this.shuffleButton.setIcon((Drawable)localObject);
      localDrawable = this.playOrderButtons[0];
      if (!SharedConfig.playOrderReversed) {
        break label199;
      }
      localObject = "player_buttonActive";
      label68:
      localDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor((String)localObject), PorterDuff.Mode.MULTIPLY));
      localDrawable = this.playOrderButtons[1];
      if (!SharedConfig.shuffleMusic) {
        break label206;
      }
    }
    label199:
    label206:
    for (Object localObject = "player_buttonActive";; localObject = "player_button")
    {
      localDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor((String)localObject), PorterDuff.Mode.MULTIPLY));
      return;
      localObject = getContext().getResources().getDrawable(2131165533).mutate();
      if (SharedConfig.playOrderReversed) {
        ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_buttonActive"), PorterDuff.Mode.MULTIPLY));
      }
      for (;;)
      {
        this.shuffleButton.setIcon((Drawable)localObject);
        break;
        ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("player_button"), PorterDuff.Mode.MULTIPLY));
      }
      localObject = "player_button";
      break label68;
    }
  }
  
  private void updateTitle(boolean paramBoolean)
  {
    Object localObject1 = MediaController.getInstance().getPlayingMessageObject();
    if (((localObject1 == null) && (paramBoolean)) || ((localObject1 != null) && (!((MessageObject)localObject1).isMusic()))) {
      dismiss();
    }
    while (localObject1 == null) {
      return;
    }
    label65:
    label116:
    Object localObject2;
    label202:
    int i;
    if (((MessageObject)localObject1).eventId != 0L)
    {
      this.hasOptions = false;
      this.menuItem.setVisibility(4);
      this.optionsButton.setVisibility(4);
      checkIfMusicDownloaded((MessageObject)localObject1);
      updateProgress((MessageObject)localObject1);
      if (!MediaController.getInstance().isMessagePaused()) {
        break label296;
      }
      this.playButton.setImageDrawable(Theme.createSimpleSelectorDrawable(this.playButton.getContext(), 2131165600, Theme.getColor("player_button"), Theme.getColor("player_buttonActive")));
      localObject2 = ((MessageObject)localObject1).getMusicTitle();
      String str = ((MessageObject)localObject1).getMusicAuthor();
      this.titleTextView.setText((CharSequence)localObject2);
      this.authorTextView.setText(str);
      this.actionBar.setTitle((CharSequence)localObject2);
      this.actionBar.setSubtitle(str);
      localObject2 = MediaController.getInstance().getAudioInfo();
      if ((localObject2 == null) || (((AudioInfo)localObject2).getCover() == null)) {
        break label331;
      }
      this.hasNoCover = false;
      this.placeholderImageView.setImageBitmap(((AudioInfo)localObject2).getCover());
      if (this.durationTextView == null) {
        break label352;
      }
      i = ((MessageObject)localObject1).getDuration();
      localObject2 = this.durationTextView;
      if (i == 0) {
        break label354;
      }
    }
    label296:
    label331:
    label352:
    label354:
    for (localObject1 = String.format("%d:%02d", new Object[] { Integer.valueOf(i / 60), Integer.valueOf(i % 60) });; localObject1 = "-:--")
    {
      ((TextView)localObject2).setText((CharSequence)localObject1);
      return;
      this.hasOptions = true;
      if (!this.actionBar.isSearchFieldVisible()) {
        this.menuItem.setVisibility(0);
      }
      this.optionsButton.setVisibility(0);
      break label65;
      this.playButton.setImageDrawable(Theme.createSimpleSelectorDrawable(this.playButton.getContext(), 2131165599, Theme.getColor("player_button"), Theme.getColor("player_buttonActive")));
      break label116;
      this.hasNoCover = true;
      this.placeholderImageView.invalidate();
      this.placeholderImageView.setImageDrawable(null);
      break label202;
      break;
    }
  }
  
  protected boolean canDismissWithSwipe()
  {
    return false;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    MessageObject localMessageObject;
    if ((paramInt1 == NotificationCenter.messagePlayingDidStarted) || (paramInt1 == NotificationCenter.messagePlayingPlayStateChanged) || (paramInt1 == NotificationCenter.messagePlayingDidReset))
    {
      if ((paramInt1 == NotificationCenter.messagePlayingDidReset) && (((Boolean)paramVarArgs[1]).booleanValue())) {}
      for (boolean bool = true;; bool = false)
      {
        updateTitle(bool);
        if ((paramInt1 != NotificationCenter.messagePlayingDidReset) && (paramInt1 != NotificationCenter.messagePlayingPlayStateChanged)) {
          break;
        }
        paramInt2 = this.listView.getChildCount();
        paramInt1 = 0;
        while (paramInt1 < paramInt2)
        {
          paramVarArgs = this.listView.getChildAt(paramInt1);
          if ((paramVarArgs instanceof AudioPlayerCell))
          {
            paramVarArgs = (AudioPlayerCell)paramVarArgs;
            localMessageObject = paramVarArgs.getMessageObject();
            if ((localMessageObject != null) && ((localMessageObject.isVoice()) || (localMessageObject.isMusic()))) {
              paramVarArgs.updateButtonState(false);
            }
          }
          paramInt1 += 1;
        }
      }
      if ((paramInt1 == NotificationCenter.messagePlayingDidStarted) && (((MessageObject)paramVarArgs[0]).eventId == 0L)) {}
    }
    do
    {
      do
      {
        for (;;)
        {
          return;
          paramInt2 = this.listView.getChildCount();
          paramInt1 = 0;
          while (paramInt1 < paramInt2)
          {
            paramVarArgs = this.listView.getChildAt(paramInt1);
            if ((paramVarArgs instanceof AudioPlayerCell))
            {
              paramVarArgs = (AudioPlayerCell)paramVarArgs;
              localMessageObject = paramVarArgs.getMessageObject();
              if ((localMessageObject != null) && ((localMessageObject.isVoice()) || (localMessageObject.isMusic()))) {
                paramVarArgs.updateButtonState(false);
              }
            }
            paramInt1 += 1;
          }
        }
        if (paramInt1 != NotificationCenter.messagePlayingProgressDidChanged) {
          break;
        }
        paramVarArgs = MediaController.getInstance().getPlayingMessageObject();
      } while ((paramVarArgs == null) || (!paramVarArgs.isMusic()));
      updateProgress(paramVarArgs);
      return;
    } while (paramInt1 != NotificationCenter.musicDidLoaded);
    this.playlist = MediaController.getInstance().getPlaylist();
    this.listAdapter.notifyDataSetChanged();
  }
  
  public void dismiss()
  {
    super.dismiss();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidStarted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.musicDidLoaded);
    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
  }
  
  @Keep
  public float getFullAnimationProgress()
  {
    return this.fullAnimationProgress;
  }
  
  public int getObserverTag()
  {
    return this.TAG;
  }
  
  public void onBackPressed()
  {
    if ((this.actionBar != null) && (this.actionBar.isSearchFieldVisible()))
    {
      this.actionBar.closeSearchField();
      return;
    }
    super.onBackPressed();
  }
  
  public void onFailedDownload(String paramString) {}
  
  public void onProgressDownload(String paramString, float paramFloat)
  {
    this.progressView.setProgress(paramFloat, true);
  }
  
  public void onProgressUpload(String paramString, float paramFloat, boolean paramBoolean) {}
  
  public void onSuccessDownload(String paramString) {}
  
  @Keep
  public void setFullAnimationProgress(float paramFloat)
  {
    this.fullAnimationProgress = paramFloat;
    this.placeholderImageView.setRoundRadius(AndroidUtilities.dp(20.0F * (1.0F - this.fullAnimationProgress)));
    paramFloat = 1.0F + this.thumbMaxScale * this.fullAnimationProgress;
    this.placeholderImageView.setScaleX(paramFloat);
    this.placeholderImageView.setScaleY(paramFloat);
    this.placeholderImageView.getTranslationY();
    this.placeholderImageView.setTranslationX(this.thumbMaxX * this.fullAnimationProgress);
    this.placeholderImageView.setTranslationY(this.startTranslation + (this.endTranslation - this.startTranslation) * this.fullAnimationProgress);
    this.playerLayout.setTranslationY(this.panelStartTranslation + (this.panelEndTranslation - this.panelStartTranslation) * this.fullAnimationProgress);
    this.shadow2.setTranslationY(this.panelStartTranslation + (this.panelEndTranslation - this.panelStartTranslation) * this.fullAnimationProgress + this.playerLayout.getMeasuredHeight());
    this.menuItem.setAlpha(this.fullAnimationProgress);
    this.searchItem.setAlpha(1.0F - this.fullAnimationProgress);
    this.avatarContainer.setAlpha(1.0F - this.fullAnimationProgress);
    this.actionBar.getTitleTextView().setAlpha(this.fullAnimationProgress);
    this.actionBar.getSubtitleTextView().setAlpha(this.fullAnimationProgress);
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context context;
    private ArrayList<MessageObject> searchResult = new ArrayList();
    private Timer searchTimer;
    
    public ListAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    private void processSearch(final String paramString)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          final ArrayList localArrayList = new ArrayList(AudioPlayerAlert.this.playlist);
          Utilities.searchQueue.postRunnable(new Runnable()
          {
            public void run()
            {
              Object localObject3 = AudioPlayerAlert.ListAdapter.2.this.val$query.trim().toLowerCase();
              if (((String)localObject3).length() == 0)
              {
                AudioPlayerAlert.ListAdapter.this.updateSearchResults(new ArrayList());
                return;
              }
              Object localObject2 = LocaleController.getInstance().getTranslitString((String)localObject3);
              Object localObject1;
              if (!((String)localObject3).equals(localObject2))
              {
                localObject1 = localObject2;
                if (((String)localObject2).length() != 0) {}
              }
              else
              {
                localObject1 = null;
              }
              int i;
              if (localObject1 != null)
              {
                i = 1;
                localObject2 = new String[i + 1];
                localObject2[0] = localObject3;
                if (localObject1 != null) {
                  localObject2[1] = localObject1;
                }
                localObject3 = new ArrayList();
                i = 0;
              }
              for (;;)
              {
                if (i < localArrayList.size())
                {
                  MessageObject localMessageObject = (MessageObject)localArrayList.get(i);
                  int j = 0;
                  CharSequence localCharSequence;
                  for (;;)
                  {
                    if (j < localObject2.length)
                    {
                      localCharSequence = localObject2[j];
                      localObject1 = localMessageObject.getDocumentName();
                      if ((localObject1 == null) || (((String)localObject1).length() == 0))
                      {
                        j += 1;
                        continue;
                        i = 0;
                        break;
                      }
                      if (!((String)localObject1).toLowerCase().contains(localCharSequence)) {
                        break label218;
                      }
                      ((ArrayList)localObject3).add(localMessageObject);
                    }
                  }
                  label211:
                  i += 1;
                  continue;
                  label218:
                  label242:
                  boolean bool3;
                  boolean bool2;
                  int k;
                  if (localMessageObject.type == 0)
                  {
                    localObject1 = localMessageObject.messageOwner.media.webpage.document;
                    bool3 = false;
                    bool2 = false;
                    k = 0;
                  }
                  for (;;)
                  {
                    boolean bool1 = bool3;
                    if (k < ((TLRPC.Document)localObject1).attributes.size())
                    {
                      TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)((TLRPC.Document)localObject1).attributes.get(k);
                      if (!(localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio)) {
                        break label379;
                      }
                      if (localDocumentAttribute.performer != null) {
                        bool2 = localDocumentAttribute.performer.toLowerCase().contains(localCharSequence);
                      }
                      bool1 = bool2;
                      if (!bool2)
                      {
                        bool1 = bool2;
                        if (localDocumentAttribute.title != null) {
                          bool1 = localDocumentAttribute.title.toLowerCase().contains(localCharSequence);
                        }
                      }
                    }
                    if (!bool1) {
                      break;
                    }
                    ((ArrayList)localObject3).add(localMessageObject);
                    break label211;
                    localObject1 = localMessageObject.messageOwner.media.document;
                    break label242;
                    label379:
                    k += 1;
                  }
                }
              }
              AudioPlayerAlert.ListAdapter.this.updateSearchResults((ArrayList)localObject3);
            }
          });
        }
      });
    }
    
    private void updateSearchResults(final ArrayList<MessageObject> paramArrayList)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          AudioPlayerAlert.access$2202(AudioPlayerAlert.this, true);
          AudioPlayerAlert.ListAdapter.access$5102(AudioPlayerAlert.ListAdapter.this, paramArrayList);
          AudioPlayerAlert.ListAdapter.this.notifyDataSetChanged();
          AudioPlayerAlert.this.layoutManager.scrollToPosition(0);
        }
      });
    }
    
    public int getItemCount()
    {
      if (AudioPlayerAlert.this.searchWas) {
        return this.searchResult.size();
      }
      if (AudioPlayerAlert.this.searching) {
        return AudioPlayerAlert.this.playlist.size();
      }
      return AudioPlayerAlert.this.playlist.size() + 1;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((AudioPlayerAlert.this.searchWas) || (AudioPlayerAlert.this.searching)) {}
      while (paramInt != 0) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return (AudioPlayerAlert.this.searchWas) || (paramViewHolder.getAdapterPosition() > 0);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      if (paramViewHolder.getItemViewType() == 1)
      {
        paramViewHolder = (AudioPlayerCell)paramViewHolder.itemView;
        if (!AudioPlayerAlert.this.searchWas) {
          break label42;
        }
        paramViewHolder.setMessageObject((MessageObject)this.searchResult.get(paramInt));
      }
      label42:
      do
      {
        return;
        if (AudioPlayerAlert.this.searching)
        {
          if (SharedConfig.playOrderReversed)
          {
            paramViewHolder.setMessageObject((MessageObject)AudioPlayerAlert.this.playlist.get(paramInt));
            return;
          }
          paramViewHolder.setMessageObject((MessageObject)AudioPlayerAlert.this.playlist.get(AudioPlayerAlert.this.playlist.size() - paramInt - 1));
          return;
        }
      } while (paramInt <= 0);
      if (SharedConfig.playOrderReversed)
      {
        paramViewHolder.setMessageObject((MessageObject)AudioPlayerAlert.this.playlist.get(paramInt - 1));
        return;
      }
      paramViewHolder.setMessageObject((MessageObject)AudioPlayerAlert.this.playlist.get(AudioPlayerAlert.this.playlist.size() - paramInt));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new AudioPlayerCell(this.context);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new View(this.context);
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, AndroidUtilities.dp(178.0F)));
      }
    }
    
    public void search(final String paramString)
    {
      try
      {
        if (this.searchTimer != null) {
          this.searchTimer.cancel();
        }
        if (paramString == null)
        {
          this.searchResult.clear();
          notifyDataSetChanged();
          return;
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
        this.searchTimer = new Timer();
        this.searchTimer.schedule(new TimerTask()
        {
          public void run()
          {
            try
            {
              AudioPlayerAlert.ListAdapter.this.searchTimer.cancel();
              AudioPlayerAlert.ListAdapter.access$4802(AudioPlayerAlert.ListAdapter.this, null);
              AudioPlayerAlert.ListAdapter.this.processSearch(paramString);
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
        }, 200L, 300L);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/AudioPlayerAlert.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */