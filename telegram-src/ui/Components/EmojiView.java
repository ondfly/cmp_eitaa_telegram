package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.EmojiData;
import org.telegram.messenger.EmojiSuggestion;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.GridLayoutManager;
import org.telegram.messenger.support.widget.GridLayoutManager.SpanSizeLookup;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_foundStickerSets;
import org.telegram.tgnet.TLRPC.TL_messages_getStickers;
import org.telegram.tgnet.TLRPC.TL_messages_searchStickerSets;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickers;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.FeaturedStickerSetInfoCell;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.Cells.StickerSetGroupInfoCell;
import org.telegram.ui.Cells.StickerSetNameCell;
import org.telegram.ui.StickerPreviewViewer;
import org.telegram.ui.StickerPreviewViewer.StickerPreviewViewerDelegate;

public class EmojiView
  extends FrameLayout
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final ViewTreeObserver.OnScrollChangedListener NOP = new ViewTreeObserver.OnScrollChangedListener()
  {
    public void onScrollChanged() {}
  };
  private static final Field superListenerField;
  private ArrayList<EmojiGridAdapter> adapters = new ArrayList();
  private ImageView backspaceButton;
  private boolean backspaceOnce;
  private boolean backspacePressed;
  private ImageView clearSearchImageView;
  private int currentAccount = UserConfig.selectedAccount;
  private int currentBackgroundType = -1;
  private int currentChatId;
  private int currentPage;
  private Paint dotPaint;
  private DragListener dragListener;
  private ArrayList<GridView> emojiGrids = new ArrayList();
  private int emojiSize;
  private LinearLayout emojiTab;
  private int favTabBum = -2;
  private ArrayList<TLRPC.Document> favouriteStickers = new ArrayList();
  private int featuredStickersHash;
  private boolean firstAttach = true;
  private ExtendedGridLayoutManager flowLayoutManager;
  private int gifTabNum = -2;
  private GifsAdapter gifsAdapter;
  private RecyclerListView gifsGridView;
  private int groupStickerPackNum;
  private int groupStickerPackPosition;
  private TLRPC.TL_messages_stickerSet groupStickerSet;
  private boolean groupStickersHidden;
  private Drawable[] icons;
  private TLRPC.ChatFull info;
  private LongSparseArray<TLRPC.StickerSetCovered> installingStickerSets = new LongSparseArray();
  private boolean isLayout;
  private int lastNotifyWidth;
  private Listener listener;
  private int[] location = new int[2];
  private TextView mediaBanTooltip;
  private int minusDy;
  private TextView noRecentTextView;
  private int oldWidth;
  private Object outlineProvider;
  private ViewPager pager;
  private PagerSlidingTabStrip pagerSlidingTabStrip;
  private EmojiColorPickerView pickerView;
  private EmojiPopupWindow pickerViewPopup;
  private int popupHeight;
  private int popupWidth;
  private CloseProgressDrawable2 progressDrawable;
  private ArrayList<TLRPC.Document> recentGifs = new ArrayList();
  private ArrayList<TLRPC.Document> recentStickers = new ArrayList();
  private int recentTabBum = -2;
  private LongSparseArray<TLRPC.StickerSetCovered> removingStickerSets = new LongSparseArray();
  private AnimatorSet searchAnimation;
  private View searchBackground;
  private EditTextBoldCursor searchEditText;
  private FrameLayout searchEditTextContainer;
  private int searchFieldHeight = AndroidUtilities.dp(64.0F);
  private ImageView searchIconImageView;
  private View shadowLine;
  private boolean showGifs;
  private StickerPreviewViewer.StickerPreviewViewerDelegate stickerPreviewViewerDelegate = new StickerPreviewViewer.StickerPreviewViewerDelegate()
  {
    public boolean needSend()
    {
      return true;
    }
    
    public void openSet(TLRPC.InputStickerSet paramAnonymousInputStickerSet)
    {
      if (paramAnonymousInputStickerSet == null) {
        return;
      }
      EmojiView.this.listener.onShowStickerSet(null, paramAnonymousInputStickerSet);
    }
    
    public void sendSticker(TLRPC.Document paramAnonymousDocument)
    {
      EmojiView.this.listener.onStickerSelected(paramAnonymousDocument);
    }
  };
  private ArrayList<TLRPC.TL_messages_stickerSet> stickerSets = new ArrayList();
  private Drawable stickersDrawable;
  private TextView stickersEmptyView;
  private StickersGridAdapter stickersGridAdapter;
  private RecyclerListView stickersGridView;
  private GridLayoutManager stickersLayoutManager;
  private RecyclerListView.OnItemClickListener stickersOnItemClickListener;
  private StickersSearchGridAdapter stickersSearchGridAdapter;
  private ScrollSlidingTabStrip stickersTab;
  private int stickersTabOffset;
  private FrameLayout stickersWrap;
  private boolean switchToGifTab;
  private TrendingGridAdapter trendingGridAdapter;
  private RecyclerListView trendingGridView;
  private GridLayoutManager trendingLayoutManager;
  private boolean trendingLoaded;
  private int trendingTabNum = -2;
  private ArrayList<View> views = new ArrayList();
  
  static
  {
    Object localObject = null;
    try
    {
      Field localField = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
      localObject = localField;
      localField.setAccessible(true);
      localObject = localField;
    }
    catch (NoSuchFieldException localNoSuchFieldException)
    {
      for (;;) {}
    }
    superListenerField = (Field)localObject;
  }
  
  public EmojiView(boolean paramBoolean1, boolean paramBoolean2, Context paramContext, TLRPC.ChatFull paramChatFull)
  {
    super(paramContext);
    this.stickersDrawable = paramContext.getResources().getDrawable(2131165418);
    Theme.setDrawableColorByKey(this.stickersDrawable, "chat_emojiPanelIcon");
    this.icons = new Drawable[] { Theme.createEmojiIconSelectorDrawable(paramContext, 2131165415, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), Theme.createEmojiIconSelectorDrawable(paramContext, 2131165417, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), Theme.createEmojiIconSelectorDrawable(paramContext, 2131165413, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), Theme.createEmojiIconSelectorDrawable(paramContext, 2131165412, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), Theme.createEmojiIconSelectorDrawable(paramContext, 2131165411, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), Theme.createEmojiIconSelectorDrawable(paramContext, 2131165414, Theme.getColor("chat_emojiPanelIcon"), Theme.getColor("chat_emojiPanelIconSelected")), this.stickersDrawable };
    this.showGifs = paramBoolean2;
    this.info = paramChatFull;
    this.dotPaint = new Paint(1);
    this.dotPaint.setColor(Theme.getColor("chat_emojiPanelNewTrending"));
    if (Build.VERSION.SDK_INT >= 21) {
      this.outlineProvider = new ViewOutlineProvider()
      {
        @TargetApi(21)
        public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
        {
          paramAnonymousOutline.setRoundRect(paramAnonymousView.getPaddingLeft(), paramAnonymousView.getPaddingTop(), paramAnonymousView.getMeasuredWidth() - paramAnonymousView.getPaddingRight(), paramAnonymousView.getMeasuredHeight() - paramAnonymousView.getPaddingBottom(), AndroidUtilities.dp(6.0F));
        }
      };
    }
    int i = 0;
    Object localObject;
    if (i < EmojiData.dataColored.length + 1)
    {
      paramChatFull = new GridView(paramContext);
      if (AndroidUtilities.isTablet()) {
        paramChatFull.setColumnWidth(AndroidUtilities.dp(60.0F));
      }
      for (;;)
      {
        paramChatFull.setNumColumns(-1);
        localObject = new EmojiGridAdapter(i - 1);
        paramChatFull.setAdapter((ListAdapter)localObject);
        this.adapters.add(localObject);
        this.emojiGrids.add(paramChatFull);
        localObject = new FrameLayout(paramContext);
        ((FrameLayout)localObject).addView(paramChatFull, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 48.0F, 0.0F, 0.0F));
        this.views.add(localObject);
        i += 1;
        break;
        paramChatFull.setColumnWidth(AndroidUtilities.dp(45.0F));
      }
    }
    if (paramBoolean1)
    {
      this.stickersWrap = new FrameLayout(paramContext);
      DataQuery.getInstance(this.currentAccount).checkStickers(0);
      DataQuery.getInstance(this.currentAccount).checkFeaturedStickers();
      this.stickersGridView = new RecyclerListView(paramContext)
      {
        boolean ignoreLayout;
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool = StickerPreviewViewer.getInstance().onInterceptTouchEvent(paramAnonymousMotionEvent, EmojiView.this.stickersGridView, EmojiView.this.getMeasuredHeight(), EmojiView.this.stickerPreviewViewerDelegate);
          return (super.onInterceptTouchEvent(paramAnonymousMotionEvent)) || (bool);
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          if ((EmojiView.this.firstAttach) && (EmojiView.this.stickersGridAdapter.getItemCount() > 0))
          {
            this.ignoreLayout = true;
            EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
            EmojiView.access$2102(EmojiView.this, false);
            this.ignoreLayout = false;
          }
          super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          EmojiView.this.checkSearchFieldScroll();
        }
        
        public void requestLayout()
        {
          if (this.ignoreLayout) {
            return;
          }
          super.requestLayout();
        }
        
        public void setVisibility(int paramAnonymousInt)
        {
          if (((EmojiView.this.gifsGridView != null) && (EmojiView.this.gifsGridView.getVisibility() == 0)) || ((EmojiView.this.trendingGridView != null) && (EmojiView.this.trendingGridView.getVisibility() == 0)))
          {
            super.setVisibility(8);
            return;
          }
          super.setVisibility(paramAnonymousInt);
        }
      };
      paramChatFull = this.stickersGridView;
      localObject = new GridLayoutManager(paramContext, 5);
      this.stickersLayoutManager = ((GridLayoutManager)localObject);
      paramChatFull.setLayoutManager((RecyclerView.LayoutManager)localObject);
      this.stickersLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
      {
        public int getSpanSize(int paramAnonymousInt)
        {
          int j = 1;
          int i;
          if (EmojiView.this.stickersGridView.getAdapter() == EmojiView.this.stickersGridAdapter) {
            if (paramAnonymousInt == 0) {
              i = EmojiView.StickersGridAdapter.access$2500(EmojiView.this.stickersGridAdapter);
            }
          }
          do
          {
            do
            {
              do
              {
                do
                {
                  return i;
                  if (paramAnonymousInt == EmojiView.StickersGridAdapter.access$2600(EmojiView.this.stickersGridAdapter)) {
                    break;
                  }
                  i = j;
                } while (EmojiView.StickersGridAdapter.access$2700(EmojiView.this.stickersGridAdapter).get(paramAnonymousInt) == null);
                i = j;
              } while ((EmojiView.StickersGridAdapter.access$2700(EmojiView.this.stickersGridAdapter).get(paramAnonymousInt) instanceof TLRPC.Document));
              return EmojiView.StickersGridAdapter.access$2500(EmojiView.this.stickersGridAdapter);
              if (paramAnonymousInt == EmojiView.StickersSearchGridAdapter.access$2900(EmojiView.this.stickersSearchGridAdapter)) {
                break;
              }
              i = j;
            } while (EmojiView.StickersSearchGridAdapter.access$3000(EmojiView.this.stickersSearchGridAdapter).get(paramAnonymousInt) == null);
            i = j;
          } while ((EmojiView.StickersSearchGridAdapter.access$3000(EmojiView.this.stickersSearchGridAdapter).get(paramAnonymousInt) instanceof TLRPC.Document));
          return EmojiView.StickersGridAdapter.access$2500(EmojiView.this.stickersGridAdapter);
        }
      });
      this.stickersGridView.setPadding(0, AndroidUtilities.dp(52.0F), 0, 0);
      this.stickersGridView.setClipToPadding(false);
      this.views.add(this.stickersWrap);
      this.stickersSearchGridAdapter = new StickersSearchGridAdapter(paramContext);
      paramChatFull = this.stickersGridView;
      localObject = new StickersGridAdapter(paramContext);
      this.stickersGridAdapter = ((StickersGridAdapter)localObject);
      paramChatFull.setAdapter((RecyclerView.Adapter)localObject);
      this.stickersGridView.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          return StickerPreviewViewer.getInstance().onTouch(paramAnonymousMotionEvent, EmojiView.this.stickersGridView, EmojiView.this.getMeasuredHeight(), EmojiView.this.stickersOnItemClickListener, EmojiView.this.stickerPreviewViewerDelegate);
        }
      });
      this.stickersOnItemClickListener = new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (EmojiView.this.stickersGridView.getAdapter() == EmojiView.this.stickersSearchGridAdapter)
          {
            TLRPC.StickerSetCovered localStickerSetCovered = (TLRPC.StickerSetCovered)EmojiView.StickersSearchGridAdapter.access$3200(EmojiView.this.stickersSearchGridAdapter).get(paramAnonymousInt);
            if (localStickerSetCovered != null) {
              EmojiView.this.listener.onShowStickerSet(localStickerSetCovered.set, null);
            }
          }
          do
          {
            do
            {
              return;
            } while (!(paramAnonymousView instanceof StickerEmojiCell));
            StickerPreviewViewer.getInstance().reset();
            paramAnonymousView = (StickerEmojiCell)paramAnonymousView;
          } while (paramAnonymousView.isDisabled());
          paramAnonymousView.disable();
          EmojiView.this.listener.onStickerSelected(paramAnonymousView.getSticker());
        }
      };
      this.stickersGridView.setOnItemClickListener(this.stickersOnItemClickListener);
      this.stickersGridView.setGlowColor(Theme.getColor("chat_emojiPanelBackground"));
      this.stickersWrap.addView(this.stickersGridView);
      this.searchEditTextContainer = new FrameLayout(paramContext);
      this.searchEditTextContainer.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
      this.stickersWrap.addView(this.searchEditTextContainer, new RecyclerView.LayoutParams(-1, this.searchFieldHeight));
      this.searchBackground = new View(paramContext);
      this.searchBackground.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(3.0F), Theme.getColor("chat_emojiSearchBackground")));
      this.searchEditTextContainer.addView(this.searchBackground, LayoutHelper.createFrame(-1, 36.0F, 51, 14.0F, 14.0F, 14.0F, 0.0F));
      this.searchIconImageView = new ImageView(paramContext);
      this.searchIconImageView.setScaleType(ImageView.ScaleType.CENTER);
      this.searchIconImageView.setImageResource(2131165654);
      this.searchIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelIcon"), PorterDuff.Mode.MULTIPLY));
      this.searchEditTextContainer.addView(this.searchIconImageView, LayoutHelper.createFrame(36, 36.0F, 51, 14.0F, 14.0F, 0.0F, 0.0F));
      this.clearSearchImageView = new ImageView(paramContext);
      this.clearSearchImageView.setScaleType(ImageView.ScaleType.CENTER);
      paramChatFull = this.clearSearchImageView;
      localObject = new CloseProgressDrawable2();
      this.progressDrawable = ((CloseProgressDrawable2)localObject);
      paramChatFull.setImageDrawable((Drawable)localObject);
      this.clearSearchImageView.setScaleX(0.1F);
      this.clearSearchImageView.setScaleY(0.1F);
      this.clearSearchImageView.setAlpha(0.0F);
      this.clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelIcon"), PorterDuff.Mode.MULTIPLY));
      this.searchEditTextContainer.addView(this.clearSearchImageView, LayoutHelper.createFrame(36, 36.0F, 53, 14.0F, 14.0F, 14.0F, 0.0F));
      this.clearSearchImageView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          EmojiView.this.searchEditText.setText("");
          AndroidUtilities.showKeyboard(EmojiView.this.searchEditText);
        }
      });
      this.searchEditText = new EditTextBoldCursor(paramContext)
      {
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          if (paramAnonymousMotionEvent.getAction() == 0) {
            if (!EmojiView.this.listener.isSearchOpened())
            {
              if (EmojiView.this.searchAnimation != null)
              {
                EmojiView.this.searchAnimation.cancel();
                EmojiView.access$3402(EmojiView.this, null);
              }
              if ((EmojiView.this.listener == null) || (!EmojiView.this.listener.isExpanded())) {
                break label282;
              }
              EmojiView.access$3402(EmojiView.this, new AnimatorSet());
              EmojiView.this.searchAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(EmojiView.this.stickersTab, "translationY", new float[] { -AndroidUtilities.dp(47.0F) }), ObjectAnimator.ofFloat(EmojiView.this.stickersGridView, "translationY", new float[] { -AndroidUtilities.dp(48.0F) }), ObjectAnimator.ofFloat(EmojiView.this.searchEditTextContainer, "translationY", new float[] { AndroidUtilities.dp(0.0F) }) });
              EmojiView.this.searchAnimation.setDuration(200L);
              EmojiView.this.searchAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
              EmojiView.this.searchAnimation.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationCancel(Animator paramAnonymous2Animator)
                {
                  if (paramAnonymous2Animator.equals(EmojiView.this.searchAnimation)) {
                    EmojiView.access$3402(EmojiView.this, null);
                  }
                }
                
                public void onAnimationEnd(Animator paramAnonymous2Animator)
                {
                  if (paramAnonymous2Animator.equals(EmojiView.this.searchAnimation))
                  {
                    EmojiView.this.stickersGridView.setTranslationY(0.0F);
                    EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.dp(4.0F), 0, 0);
                    EmojiView.access$3402(EmojiView.this, null);
                  }
                }
              });
              EmojiView.this.searchAnimation.start();
            }
          }
          for (;;)
          {
            EmojiView.this.listener.onSearchOpenClose(true);
            EmojiView.this.searchEditText.requestFocus();
            AndroidUtilities.showKeyboard(EmojiView.this.searchEditText);
            return super.onTouchEvent(paramAnonymousMotionEvent);
            label282:
            EmojiView.this.searchEditTextContainer.setTranslationY(AndroidUtilities.dp(0.0F));
            EmojiView.this.stickersTab.setTranslationY(-AndroidUtilities.dp(47.0F));
            EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.dp(4.0F), 0, 0);
          }
        }
      };
      this.searchEditText.setTextSize(1, 16.0F);
      this.searchEditText.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
      this.searchEditText.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.searchEditText.setBackgroundDrawable(null);
      this.searchEditText.setPadding(0, 0, 0, 0);
      this.searchEditText.setMaxLines(1);
      this.searchEditText.setLines(1);
      this.searchEditText.setSingleLine(true);
      this.searchEditText.setImeOptions(268435459);
      this.searchEditText.setHint(LocaleController.getString("SearchStickersHint", 2131494308));
      this.searchEditText.setCursorColor(Theme.getColor("featuredStickers_addedIcon"));
      this.searchEditText.setCursorSize(AndroidUtilities.dp(20.0F));
      this.searchEditText.setCursorWidth(1.5F);
      this.searchEditTextContainer.addView(this.searchEditText, LayoutHelper.createFrame(-1, 40.0F, 51, 46.0F, 12.0F, 46.0F, 0.0F));
      this.searchEditText.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable)
        {
          float f2 = 1.0F;
          int i;
          int j;
          if (EmojiView.this.searchEditText.length() > 0)
          {
            i = 1;
            if (EmojiView.this.clearSearchImageView.getAlpha() == 0.0F) {
              break label131;
            }
            j = 1;
            label36:
            if (i != j)
            {
              paramAnonymousEditable = EmojiView.this.clearSearchImageView.animate();
              if (i == 0) {
                break label137;
              }
              f1 = 1.0F;
              label61:
              paramAnonymousEditable = paramAnonymousEditable.alpha(f1).setDuration(150L);
              if (i == 0) {
                break label142;
              }
              f1 = 1.0F;
              label80:
              paramAnonymousEditable = paramAnonymousEditable.scaleX(f1);
              if (i == 0) {
                break label148;
              }
            }
          }
          label131:
          label137:
          label142:
          label148:
          for (float f1 = f2;; f1 = 0.1F)
          {
            paramAnonymousEditable.scaleY(f1).start();
            EmojiView.this.stickersSearchGridAdapter.search(EmojiView.this.searchEditText.getText().toString());
            return;
            i = 0;
            break;
            j = 0;
            break label36;
            f1 = 0.0F;
            break label61;
            f1 = 0.1F;
            break label80;
          }
        }
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      });
      this.trendingGridView = new RecyclerListView(paramContext);
      this.trendingGridView.setItemAnimator(null);
      this.trendingGridView.setLayoutAnimation(null);
      paramChatFull = this.trendingGridView;
      localObject = new GridLayoutManager(paramContext, 5)
      {
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      this.trendingLayoutManager = ((GridLayoutManager)localObject);
      paramChatFull.setLayoutManager((RecyclerView.LayoutManager)localObject);
      this.trendingLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
      {
        public int getSpanSize(int paramAnonymousInt)
        {
          if (((EmojiView.TrendingGridAdapter.access$3900(EmojiView.this.trendingGridAdapter).get(paramAnonymousInt) instanceof Integer)) || (paramAnonymousInt == EmojiView.TrendingGridAdapter.access$4000(EmojiView.this.trendingGridAdapter))) {
            return EmojiView.TrendingGridAdapter.access$4100(EmojiView.this.trendingGridAdapter);
          }
          return 1;
        }
      });
      this.trendingGridView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          EmojiView.this.checkStickersTabY(paramAnonymousRecyclerView, paramAnonymousInt2);
        }
      });
      this.trendingGridView.setClipToPadding(false);
      this.trendingGridView.setPadding(0, AndroidUtilities.dp(48.0F), 0, 0);
      paramChatFull = this.trendingGridView;
      localObject = new TrendingGridAdapter(paramContext);
      this.trendingGridAdapter = ((TrendingGridAdapter)localObject);
      paramChatFull.setAdapter((RecyclerView.Adapter)localObject);
      this.trendingGridView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          paramAnonymousView = (TLRPC.StickerSetCovered)EmojiView.TrendingGridAdapter.access$4300(EmojiView.this.trendingGridAdapter).get(paramAnonymousInt);
          if (paramAnonymousView != null) {
            EmojiView.this.listener.onShowStickerSet(paramAnonymousView.set, null);
          }
        }
      });
      this.trendingGridAdapter.notifyDataSetChanged();
      this.trendingGridView.setGlowColor(Theme.getColor("chat_emojiPanelBackground"));
      this.trendingGridView.setVisibility(8);
      this.stickersWrap.addView(this.trendingGridView);
      if (paramBoolean2)
      {
        this.gifsGridView = new RecyclerListView(paramContext);
        this.gifsGridView.setClipToPadding(false);
        this.gifsGridView.setPadding(0, AndroidUtilities.dp(48.0F), 0, 0);
        paramChatFull = this.gifsGridView;
        localObject = new ExtendedGridLayoutManager(paramContext, 100)
        {
          private Size size = new Size();
          
          protected Size getSizeForItem(int paramAnonymousInt)
          {
            float f2 = 100.0F;
            TLRPC.Document localDocument = (TLRPC.Document)EmojiView.this.recentGifs.get(paramAnonymousInt);
            Object localObject = this.size;
            float f1;
            if ((localDocument.thumb != null) && (localDocument.thumb.w != 0))
            {
              f1 = localDocument.thumb.w;
              ((Size)localObject).width = f1;
              localObject = this.size;
              f1 = f2;
              if (localDocument.thumb != null)
              {
                f1 = f2;
                if (localDocument.thumb.h != 0) {
                  f1 = localDocument.thumb.h;
                }
              }
              ((Size)localObject).height = f1;
              paramAnonymousInt = 0;
            }
            for (;;)
            {
              if (paramAnonymousInt < localDocument.attributes.size())
              {
                localObject = (TLRPC.DocumentAttribute)localDocument.attributes.get(paramAnonymousInt);
                if (((localObject instanceof TLRPC.TL_documentAttributeImageSize)) || ((localObject instanceof TLRPC.TL_documentAttributeVideo)))
                {
                  this.size.width = ((TLRPC.DocumentAttribute)localObject).w;
                  this.size.height = ((TLRPC.DocumentAttribute)localObject).h;
                }
              }
              else
              {
                return this.size;
                f1 = 100.0F;
                break;
              }
              paramAnonymousInt += 1;
            }
          }
        };
        this.flowLayoutManager = ((ExtendedGridLayoutManager)localObject);
        paramChatFull.setLayoutManager((RecyclerView.LayoutManager)localObject);
        this.flowLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
          public int getSpanSize(int paramAnonymousInt)
          {
            return EmojiView.this.flowLayoutManager.getSpanSizeForItem(paramAnonymousInt);
          }
        });
        this.gifsGridView.addItemDecoration(new RecyclerView.ItemDecoration()
        {
          public void getItemOffsets(Rect paramAnonymousRect, View paramAnonymousView, RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState)
          {
            int i = 0;
            paramAnonymousRect.left = 0;
            paramAnonymousRect.top = 0;
            paramAnonymousRect.bottom = 0;
            int j = paramAnonymousRecyclerView.getChildAdapterPosition(paramAnonymousView);
            if (!EmojiView.this.flowLayoutManager.isFirstRow(j)) {
              paramAnonymousRect.top = AndroidUtilities.dp(2.0F);
            }
            if (EmojiView.this.flowLayoutManager.isLastInRow(j)) {}
            for (;;)
            {
              paramAnonymousRect.right = i;
              return;
              i = AndroidUtilities.dp(2.0F);
            }
          }
        });
        this.gifsGridView.setOverScrollMode(2);
        paramChatFull = this.gifsGridView;
        localObject = new GifsAdapter(paramContext);
        this.gifsAdapter = ((GifsAdapter)localObject);
        paramChatFull.setAdapter((RecyclerView.Adapter)localObject);
        this.gifsGridView.setOnScrollListener(new RecyclerView.OnScrollListener()
        {
          public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
          {
            EmojiView.this.checkStickersTabY(paramAnonymousRecyclerView, paramAnonymousInt2);
          }
        });
        this.gifsGridView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
        {
          public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
          {
            if ((paramAnonymousInt < 0) || (paramAnonymousInt >= EmojiView.this.recentGifs.size()) || (EmojiView.this.listener == null)) {
              return;
            }
            EmojiView.this.listener.onGifSelected((TLRPC.Document)EmojiView.this.recentGifs.get(paramAnonymousInt));
          }
        });
        this.gifsGridView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
        {
          public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
          {
            if ((paramAnonymousInt < 0) || (paramAnonymousInt >= EmojiView.this.recentGifs.size())) {
              return false;
            }
            final TLRPC.Document localDocument = (TLRPC.Document)EmojiView.this.recentGifs.get(paramAnonymousInt);
            paramAnonymousView = new AlertDialog.Builder(paramAnonymousView.getContext());
            paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
            paramAnonymousView.setMessage(LocaleController.getString("DeleteGif", 2131493370));
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028).toUpperCase(), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                DataQuery.getInstance(EmojiView.this.currentAccount).removeRecentGif(localDocument);
                EmojiView.access$4402(EmojiView.this, DataQuery.getInstance(EmojiView.this.currentAccount).getRecentGifs());
                if (EmojiView.this.gifsAdapter != null) {
                  EmojiView.this.gifsAdapter.notifyDataSetChanged();
                }
                if (EmojiView.this.recentGifs.isEmpty())
                {
                  EmojiView.this.updateStickerTabs();
                  if (EmojiView.this.stickersGridAdapter != null) {
                    EmojiView.this.stickersGridAdapter.notifyDataSetChanged();
                  }
                }
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            paramAnonymousView.show().setCanceledOnTouchOutside(true);
            return true;
          }
        });
        this.gifsGridView.setVisibility(8);
        this.stickersWrap.addView(this.gifsGridView);
      }
      this.stickersEmptyView = new TextView(paramContext);
      this.stickersEmptyView.setText(LocaleController.getString("NoStickers", 2131493914));
      this.stickersEmptyView.setTextSize(1, 18.0F);
      this.stickersEmptyView.setTextColor(Theme.getColor("chat_emojiPanelEmptyText"));
      this.stickersEmptyView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.stickersWrap.addView(this.stickersEmptyView, LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 48.0F, 0.0F, 0.0F));
      this.stickersGridView.setEmptyView(this.stickersEmptyView);
      this.stickersTab = new ScrollSlidingTabStrip(paramContext)
      {
        float downX;
        float downY;
        boolean draggingHorizontally;
        boolean draggingVertically;
        boolean first = true;
        float lastTranslateX;
        float lastX;
        boolean startedScroll;
        final int touchslop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        VelocityTracker vTracker;
        
        public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool = true;
          if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
          }
          if (paramAnonymousMotionEvent.getAction() == 0)
          {
            this.draggingHorizontally = false;
            this.draggingVertically = false;
            this.downX = paramAnonymousMotionEvent.getRawX();
            this.downY = paramAnonymousMotionEvent.getRawY();
            bool = super.onInterceptTouchEvent(paramAnonymousMotionEvent);
          }
          do
          {
            return bool;
            if ((this.draggingVertically) || (this.draggingHorizontally) || (EmojiView.this.dragListener == null) || (Math.abs(paramAnonymousMotionEvent.getRawY() - this.downY) < this.touchslop)) {
              break;
            }
            this.draggingVertically = true;
            this.downY = paramAnonymousMotionEvent.getRawY();
            EmojiView.this.dragListener.onDragStart();
          } while (!this.startedScroll);
          EmojiView.this.pager.endFakeDrag();
          this.startedScroll = false;
          return true;
        }
        
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          boolean bool = false;
          if (this.first)
          {
            this.first = false;
            this.lastX = paramAnonymousMotionEvent.getX();
          }
          if (paramAnonymousMotionEvent.getAction() == 0)
          {
            this.draggingHorizontally = false;
            this.draggingVertically = false;
            this.downX = paramAnonymousMotionEvent.getRawX();
            this.downY = paramAnonymousMotionEvent.getRawY();
            if (!this.draggingVertically) {
              break label334;
            }
            if (this.vTracker == null) {
              this.vTracker = VelocityTracker.obtain();
            }
            this.vTracker.addMovement(paramAnonymousMotionEvent);
            if ((paramAnonymousMotionEvent.getAction() != 1) && (paramAnonymousMotionEvent.getAction() != 3)) {
              break label308;
            }
            this.vTracker.computeCurrentVelocity(1000);
            f = this.vTracker.getYVelocity();
            this.vTracker.recycle();
            this.vTracker = null;
            if (paramAnonymousMotionEvent.getAction() != 1) {
              break label293;
            }
            EmojiView.this.dragListener.onDragEnd(f);
          }
          for (;;)
          {
            this.first = true;
            this.draggingHorizontally = false;
            this.draggingVertically = false;
            return true;
            if ((this.draggingVertically) || (this.draggingHorizontally) || (EmojiView.this.dragListener == null)) {
              break;
            }
            if (Math.abs(paramAnonymousMotionEvent.getRawX() - this.downX) >= this.touchslop)
            {
              this.draggingHorizontally = true;
              break;
            }
            if (Math.abs(paramAnonymousMotionEvent.getRawY() - this.downY) < this.touchslop) {
              break;
            }
            this.draggingVertically = true;
            this.downY = paramAnonymousMotionEvent.getRawY();
            EmojiView.this.dragListener.onDragStart();
            if (!this.startedScroll) {
              break;
            }
            EmojiView.this.pager.endFakeDrag();
            this.startedScroll = false;
            break;
            label293:
            EmojiView.this.dragListener.onDragCancel();
          }
          label308:
          EmojiView.this.dragListener.onDrag(Math.round(paramAnonymousMotionEvent.getRawY() - this.downY));
          return true;
          label334:
          float f = EmojiView.this.stickersTab.getTranslationX();
          if ((EmojiView.this.stickersTab.getScrollX() == 0) && (f == 0.0F))
          {
            if ((this.startedScroll) || (this.lastX - paramAnonymousMotionEvent.getX() >= 0.0F)) {
              break label541;
            }
            if (EmojiView.this.pager.beginFakeDrag())
            {
              this.startedScroll = true;
              this.lastTranslateX = EmojiView.this.stickersTab.getTranslationX();
            }
          }
          int i;
          if (this.startedScroll) {
            i = (int)(paramAnonymousMotionEvent.getX() - this.lastX + f - this.lastTranslateX);
          }
          for (;;)
          {
            label541:
            try
            {
              EmojiView.this.pager.fakeDragBy(i);
              this.lastTranslateX = f;
              this.lastX = paramAnonymousMotionEvent.getX();
              if ((paramAnonymousMotionEvent.getAction() == 3) || (paramAnonymousMotionEvent.getAction() == 1))
              {
                this.first = true;
                this.draggingHorizontally = false;
                this.draggingVertically = false;
                if (this.startedScroll)
                {
                  EmojiView.this.pager.endFakeDrag();
                  this.startedScroll = false;
                }
              }
              if ((this.startedScroll) || (super.onTouchEvent(paramAnonymousMotionEvent))) {
                bool = true;
              }
              return bool;
            }
            catch (Exception localException1) {}
            if ((!this.startedScroll) || (this.lastX - paramAnonymousMotionEvent.getX() <= 0.0F) || (!EmojiView.this.pager.isFakeDragging())) {
              break;
            }
            EmojiView.this.pager.endFakeDrag();
            this.startedScroll = false;
            break;
            try
            {
              EmojiView.this.pager.endFakeDrag();
              this.startedScroll = false;
              FileLog.e(localException1);
            }
            catch (Exception localException2)
            {
              for (;;) {}
            }
          }
        }
      };
      this.stickersTab.setUnderlineHeight(AndroidUtilities.dp(1.0F));
      this.stickersTab.setIndicatorColor(Theme.getColor("chat_emojiPanelStickerPackSelector"));
      this.stickersTab.setUnderlineColor(Theme.getColor("chat_emojiPanelStickerPackSelector"));
      this.stickersTab.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
      this.stickersTab.setVisibility(4);
      addView(this.stickersTab, LayoutHelper.createFrame(-1, 48, 51));
      this.stickersTab.setTranslationX(AndroidUtilities.displaySize.x);
      updateStickerTabs();
      this.stickersTab.setDelegate(new ScrollSlidingTabStrip.ScrollSlidingTabStripDelegate()
      {
        public void onPageSelected(int paramAnonymousInt)
        {
          int i = 8;
          if (EmojiView.this.gifsGridView != null)
          {
            if (paramAnonymousInt != EmojiView.this.gifTabNum + 1) {
              break label75;
            }
            if (EmojiView.this.gifsGridView.getVisibility() != 0)
            {
              EmojiView.this.listener.onGifTab(true);
              EmojiView.this.showGifTab();
            }
          }
          if (paramAnonymousInt == 0) {
            EmojiView.this.pager.setCurrentItem(0);
          }
          label75:
          do
          {
            Object localObject;
            do
            {
              return;
              if (paramAnonymousInt == EmojiView.this.trendingTabNum + 1)
              {
                if (EmojiView.this.trendingGridView.getVisibility() == 0) {
                  break;
                }
                EmojiView.this.showTrendingTab();
                break;
              }
              if (EmojiView.this.gifsGridView.getVisibility() == 0)
              {
                EmojiView.this.listener.onGifTab(false);
                EmojiView.this.gifsGridView.setVisibility(8);
                EmojiView.this.stickersGridView.setVisibility(0);
                EmojiView.this.searchEditTextContainer.setVisibility(0);
                EmojiView.this.stickersGridView.getVisibility();
                localObject = EmojiView.this.stickersEmptyView;
                if (EmojiView.this.stickersGridAdapter.getItemCount() != 0) {}
                for (;;)
                {
                  ((TextView)localObject).setVisibility(i);
                  EmojiView.this.checkScroll();
                  EmojiView.this.saveNewPage();
                  break;
                  i = 0;
                }
              }
              if (EmojiView.this.trendingGridView.getVisibility() != 0) {
                break;
              }
              EmojiView.this.trendingGridView.setVisibility(8);
              EmojiView.this.stickersGridView.setVisibility(0);
              EmojiView.this.searchEditTextContainer.setVisibility(0);
              localObject = EmojiView.this.stickersEmptyView;
              if (EmojiView.this.stickersGridAdapter.getItemCount() != 0) {}
              for (;;)
              {
                ((TextView)localObject).setVisibility(i);
                EmojiView.this.saveNewPage();
                break;
                i = 0;
              }
            } while ((paramAnonymousInt == EmojiView.this.gifTabNum + 1) || (paramAnonymousInt == EmojiView.this.trendingTabNum + 1));
            if (paramAnonymousInt == EmojiView.this.recentTabBum + 1)
            {
              EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(EmojiView.this.stickersGridAdapter.getPositionForPack("recent"), 0);
              EmojiView.this.checkStickersTabY(null, 0);
              localObject = EmojiView.this.stickersTab;
              i = EmojiView.this.recentTabBum;
              if (EmojiView.this.recentTabBum > 0) {}
              for (paramAnonymousInt = EmojiView.this.recentTabBum;; paramAnonymousInt = EmojiView.this.stickersTabOffset)
              {
                ((ScrollSlidingTabStrip)localObject).onPageScrolled(i + 1, paramAnonymousInt + 1);
                return;
              }
            }
            if (paramAnonymousInt == EmojiView.this.favTabBum + 1)
            {
              EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(EmojiView.this.stickersGridAdapter.getPositionForPack("fav"), 0);
              EmojiView.this.checkStickersTabY(null, 0);
              localObject = EmojiView.this.stickersTab;
              i = EmojiView.this.favTabBum;
              if (EmojiView.this.favTabBum > 0) {}
              for (paramAnonymousInt = EmojiView.this.favTabBum;; paramAnonymousInt = EmojiView.this.stickersTabOffset)
              {
                ((ScrollSlidingTabStrip)localObject).onPageScrolled(i + 1, paramAnonymousInt + 1);
                return;
              }
            }
            i = paramAnonymousInt - 1 - EmojiView.this.stickersTabOffset;
            if (i < EmojiView.this.stickerSets.size()) {
              break label595;
            }
          } while (EmojiView.this.listener == null);
          EmojiView.this.listener.onStickersSettingsClick();
          return;
          label595:
          paramAnonymousInt = i;
          if (i >= EmojiView.this.stickerSets.size()) {
            paramAnonymousInt = EmojiView.this.stickerSets.size() - 1;
          }
          EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(EmojiView.this.stickersGridAdapter.getPositionForPack(EmojiView.this.stickerSets.get(paramAnonymousInt)), 0);
          EmojiView.this.checkStickersTabY(null, 0);
          EmojiView.this.checkScroll();
        }
      });
      this.stickersGridView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if (paramAnonymousInt == 1) {
            AndroidUtilities.hideKeyboard(EmojiView.this.searchEditText);
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          EmojiView.this.checkScroll();
          EmojiView.this.checkStickersTabY(paramAnonymousRecyclerView, paramAnonymousInt2);
          EmojiView.this.checkSearchFieldScroll();
        }
      });
    }
    this.pager = new ViewPager(paramContext)
    {
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (getParent() != null) {
          getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
      }
    };
    this.pager.setAdapter(new EmojiPagesAdapter(null));
    this.emojiTab = new LinearLayout(paramContext)
    {
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (getParent() != null) {
          getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
      }
    };
    this.emojiTab.setOrientation(0);
    addView(this.emojiTab, LayoutHelper.createFrame(-1, 48.0F));
    this.pagerSlidingTabStrip = new PagerSlidingTabStrip(paramContext);
    this.pagerSlidingTabStrip.setViewPager(this.pager);
    this.pagerSlidingTabStrip.setShouldExpand(true);
    this.pagerSlidingTabStrip.setIndicatorHeight(AndroidUtilities.dp(2.0F));
    this.pagerSlidingTabStrip.setUnderlineHeight(AndroidUtilities.dp(1.0F));
    this.pagerSlidingTabStrip.setIndicatorColor(Theme.getColor("chat_emojiPanelIconSelector"));
    this.pagerSlidingTabStrip.setUnderlineColor(Theme.getColor("chat_emojiPanelShadowLine"));
    this.emojiTab.addView(this.pagerSlidingTabStrip, LayoutHelper.createLinear(0, 48, 1.0F));
    this.pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
    {
      public void onPageScrollStateChanged(int paramAnonymousInt) {}
      
      public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2)
      {
        EmojiView.this.onPageScrolled(paramAnonymousInt1, EmojiView.this.getMeasuredWidth() - EmojiView.this.getPaddingLeft() - EmojiView.this.getPaddingRight(), paramAnonymousInt2);
      }
      
      public void onPageSelected(int paramAnonymousInt)
      {
        EmojiView.this.saveNewPage();
      }
    });
    paramChatFull = new FrameLayout(paramContext);
    this.emojiTab.addView(paramChatFull, LayoutHelper.createLinear(52, 48));
    this.backspaceButton = new ImageView(paramContext)
    {
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (paramAnonymousMotionEvent.getAction() == 0)
        {
          EmojiView.access$6302(EmojiView.this, true);
          EmojiView.access$6402(EmojiView.this, false);
          EmojiView.this.postBackspaceRunnable(350);
        }
        for (;;)
        {
          super.onTouchEvent(paramAnonymousMotionEvent);
          return true;
          if ((paramAnonymousMotionEvent.getAction() == 3) || (paramAnonymousMotionEvent.getAction() == 1))
          {
            EmojiView.access$6302(EmojiView.this, false);
            if ((!EmojiView.this.backspaceOnce) && (EmojiView.this.listener != null) && (EmojiView.this.listener.onBackspace())) {
              EmojiView.this.backspaceButton.performHapticFeedback(3);
            }
          }
        }
      }
    };
    this.backspaceButton.setImageResource(2131165419);
    this.backspaceButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelBackspace"), PorterDuff.Mode.MULTIPLY));
    this.backspaceButton.setScaleType(ImageView.ScaleType.CENTER);
    paramChatFull.addView(this.backspaceButton, LayoutHelper.createFrame(52, 48.0F));
    this.shadowLine = new View(paramContext);
    this.shadowLine.setBackgroundColor(Theme.getColor("chat_emojiPanelShadowLine"));
    paramChatFull.addView(this.shadowLine, LayoutHelper.createFrame(52, 1, 83));
    this.noRecentTextView = new TextView(paramContext);
    this.noRecentTextView.setText(LocaleController.getString("NoRecent", 2131493903));
    this.noRecentTextView.setTextSize(1, 18.0F);
    this.noRecentTextView.setTextColor(Theme.getColor("chat_emojiPanelEmptyText"));
    this.noRecentTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.noRecentTextView.setGravity(17);
    this.noRecentTextView.setClickable(false);
    this.noRecentTextView.setFocusable(false);
    ((FrameLayout)this.views.get(0)).addView(this.noRecentTextView, LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 48.0F, 0.0F, 0.0F));
    ((GridView)this.emojiGrids.get(0)).setEmptyView(this.noRecentTextView);
    addView(this.pager, 0, LayoutHelper.createFrame(-1, -1, 51));
    this.mediaBanTooltip = new CorrectlyMeasuringTextView(paramContext);
    this.mediaBanTooltip.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(3.0F), Theme.getColor("chat_gifSaveHintBackground")));
    this.mediaBanTooltip.setTextColor(Theme.getColor("chat_gifSaveHintText"));
    this.mediaBanTooltip.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F));
    this.mediaBanTooltip.setGravity(16);
    this.mediaBanTooltip.setTextSize(1, 14.0F);
    this.mediaBanTooltip.setVisibility(4);
    addView(this.mediaBanTooltip, LayoutHelper.createFrame(-2, -2.0F, 53, 30.0F, 53.0F, 5.0F, 0.0F));
    if (AndroidUtilities.isTablet())
    {
      f = 40.0F;
      this.emojiSize = AndroidUtilities.dp(f);
      this.pickerView = new EmojiColorPickerView(paramContext);
      paramContext = this.pickerView;
      if (!AndroidUtilities.isTablet()) {
        break label2801;
      }
      i = 40;
      label2634:
      i = AndroidUtilities.dp(i * 6 + 10 + 20);
      this.popupWidth = i;
      if (!AndroidUtilities.isTablet()) {
        break label2808;
      }
    }
    label2801:
    label2808:
    for (float f = 64.0F;; f = 56.0F)
    {
      int j = AndroidUtilities.dp(f);
      this.popupHeight = j;
      this.pickerViewPopup = new EmojiPopupWindow(paramContext, i, j);
      this.pickerViewPopup.setOutsideTouchable(true);
      this.pickerViewPopup.setClippingEnabled(true);
      this.pickerViewPopup.setInputMethodMode(2);
      this.pickerViewPopup.setSoftInputMode(0);
      this.pickerViewPopup.getContentView().setFocusableInTouchMode(true);
      this.pickerViewPopup.getContentView().setOnKeyListener(new View.OnKeyListener()
      {
        public boolean onKey(View paramAnonymousView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousInt == 82) && (paramAnonymousKeyEvent.getRepeatCount() == 0) && (paramAnonymousKeyEvent.getAction() == 1) && (EmojiView.this.pickerViewPopup != null) && (EmojiView.this.pickerViewPopup.isShowing()))
          {
            EmojiView.this.pickerViewPopup.dismiss();
            return true;
          }
          return false;
        }
      });
      this.currentPage = MessagesController.getGlobalEmojiSettings().getInt("selected_page", 0);
      Emoji.loadRecentEmoji();
      ((EmojiGridAdapter)this.adapters.get(0)).notifyDataSetChanged();
      return;
      f = 32.0F;
      break;
      i = 32;
      break label2634;
    }
  }
  
  private static String addColorToCode(String paramString1, String paramString2)
  {
    Object localObject2 = null;
    int i = paramString1.length();
    Object localObject1;
    String str;
    if ((i > 2) && (paramString1.charAt(paramString1.length() - 2) == '‍'))
    {
      localObject1 = paramString1.substring(paramString1.length() - 2);
      str = paramString1.substring(0, paramString1.length() - 2);
    }
    for (;;)
    {
      paramString2 = str + paramString2;
      paramString1 = paramString2;
      if (localObject1 != null) {
        paramString1 = paramString2 + (String)localObject1;
      }
      return paramString1;
      localObject1 = localObject2;
      str = paramString1;
      if (i > 3)
      {
        localObject1 = localObject2;
        str = paramString1;
        if (paramString1.charAt(paramString1.length() - 3) == '‍')
        {
          localObject1 = paramString1.substring(paramString1.length() - 3);
          str = paramString1.substring(0, paramString1.length() - 3);
        }
      }
    }
  }
  
  private void checkDocuments(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      i = this.recentGifs.size();
      this.recentGifs = DataQuery.getInstance(this.currentAccount).getRecentGifs();
      if (this.gifsAdapter != null) {
        this.gifsAdapter.notifyDataSetChanged();
      }
      if (i != this.recentGifs.size()) {
        updateStickerTabs();
      }
      if (this.stickersGridAdapter != null) {
        this.stickersGridAdapter.notifyDataSetChanged();
      }
      return;
    }
    int k = this.recentStickers.size();
    int m = this.favouriteStickers.size();
    this.recentStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(0);
    this.favouriteStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(2);
    int i = 0;
    if (i < this.favouriteStickers.size())
    {
      TLRPC.Document localDocument1 = (TLRPC.Document)this.favouriteStickers.get(i);
      int j = 0;
      for (;;)
      {
        if (j < this.recentStickers.size())
        {
          TLRPC.Document localDocument2 = (TLRPC.Document)this.recentStickers.get(j);
          if ((localDocument2.dc_id == localDocument1.dc_id) && (localDocument2.id == localDocument1.id)) {
            this.recentStickers.remove(j);
          }
        }
        else
        {
          i += 1;
          break;
        }
        j += 1;
      }
    }
    if ((k != this.recentStickers.size()) || (m != this.favouriteStickers.size())) {
      updateStickerTabs();
    }
    if (this.stickersGridAdapter != null) {
      this.stickersGridAdapter.notifyDataSetChanged();
    }
    checkPanels();
  }
  
  private void checkPanels()
  {
    int j = 8;
    if (this.stickersTab == null) {}
    int i;
    label192:
    do
    {
      Object localObject;
      do
      {
        return;
        if ((this.trendingTabNum == -2) && (this.trendingGridView != null) && (this.trendingGridView.getVisibility() == 0))
        {
          this.gifsGridView.setVisibility(8);
          this.trendingGridView.setVisibility(8);
          this.stickersGridView.setVisibility(0);
          this.searchEditTextContainer.setVisibility(0);
          localObject = this.stickersEmptyView;
          if (this.stickersGridAdapter.getItemCount() != 0)
          {
            i = 8;
            ((TextView)localObject).setVisibility(i);
          }
        }
        else
        {
          if ((this.gifTabNum != -2) || (this.gifsGridView == null) || (this.gifsGridView.getVisibility() != 0)) {
            continue;
          }
          this.listener.onGifTab(false);
          this.gifsGridView.setVisibility(8);
          this.trendingGridView.setVisibility(8);
          this.stickersGridView.setVisibility(0);
          this.searchEditTextContainer.setVisibility(0);
          localObject = this.stickersEmptyView;
          if (this.stickersGridAdapter.getItemCount() == 0) {
            break label192;
          }
        }
        for (i = j;; i = 0)
        {
          ((TextView)localObject).setVisibility(i);
          return;
          i = 0;
          break;
        }
      } while (this.gifTabNum == -2);
      if ((this.gifsGridView != null) && (this.gifsGridView.getVisibility() == 0))
      {
        localObject = this.stickersTab;
        j = this.gifTabNum;
        if (this.recentTabBum > 0) {}
        for (i = this.recentTabBum;; i = this.stickersTabOffset)
        {
          ((ScrollSlidingTabStrip)localObject).onPageScrolled(j + 1, i + 1);
          return;
        }
      }
      if ((this.trendingGridView != null) && (this.trendingGridView.getVisibility() == 0))
      {
        localObject = this.stickersTab;
        j = this.trendingTabNum;
        if (this.recentTabBum > 0) {}
        for (i = this.recentTabBum;; i = this.stickersTabOffset)
        {
          ((ScrollSlidingTabStrip)localObject).onPageScrolled(j + 1, i + 1);
          return;
        }
      }
      j = this.stickersLayoutManager.findFirstVisibleItemPosition();
    } while (j == -1);
    if (this.favTabBum > 0) {
      i = this.favTabBum;
    }
    for (;;)
    {
      this.stickersTab.onPageScrolled(this.stickersGridAdapter.getTabForPosition(j) + 1, i + 1);
      return;
      if (this.recentTabBum > 0) {
        i = this.recentTabBum;
      } else {
        i = this.stickersTabOffset;
      }
    }
  }
  
  private void checkScroll()
  {
    int j = this.stickersLayoutManager.findFirstVisibleItemPosition();
    if (j == -1) {}
    while (this.stickersGridView == null) {
      return;
    }
    int i;
    if (this.favTabBum > 0) {
      i = this.favTabBum;
    }
    while (this.stickersGridView.getVisibility() != 0)
    {
      if ((this.gifsGridView != null) && (this.gifsGridView.getVisibility() != 0)) {
        this.gifsGridView.setVisibility(0);
      }
      if ((this.stickersEmptyView != null) && (this.stickersEmptyView.getVisibility() == 0)) {
        this.stickersEmptyView.setVisibility(8);
      }
      this.stickersTab.onPageScrolled(this.gifTabNum + 1, i + 1);
      return;
      if (this.recentTabBum > 0) {
        i = this.recentTabBum;
      } else {
        i = this.stickersTabOffset;
      }
    }
    this.stickersTab.onPageScrolled(this.stickersGridAdapter.getTabForPosition(j) + 1, i + 1);
  }
  
  private void checkSearchFieldScroll()
  {
    if ((this.stickersGridView == null) || ((this.listener != null) && (this.listener.isSearchOpened()))) {
      return;
    }
    RecyclerView.ViewHolder localViewHolder = this.stickersGridView.findViewHolderForAdapterPosition(0);
    if (localViewHolder != null)
    {
      this.searchEditTextContainer.setTranslationY(localViewHolder.itemView.getTop());
      return;
    }
    this.searchEditTextContainer.setTranslationY(-this.searchFieldHeight);
  }
  
  private void checkStickersTabY(View paramView, int paramInt)
  {
    if (paramView == null)
    {
      paramView = this.stickersTab;
      this.minusDy = 0;
      paramView.setTranslationY(0);
    }
    do
    {
      do
      {
        return;
      } while ((paramView.getVisibility() != 0) || ((this.listener != null) && (this.listener.isSearchOpened())));
      if ((paramInt <= 0) || (this.stickersGridView == null) || (this.stickersGridView.getVisibility() != 0)) {
        break;
      }
      paramView = this.stickersGridView.findViewHolderForAdapterPosition(0);
    } while ((paramView != null) && (paramView.itemView.getTop() + this.searchFieldHeight >= this.stickersGridView.getPaddingTop()));
    this.minusDy -= paramInt;
    if (this.minusDy > 0) {
      this.minusDy = 0;
    }
    for (;;)
    {
      this.stickersTab.setTranslationY(Math.max(-AndroidUtilities.dp(47.0F), this.minusDy));
      return;
      if (this.minusDy < -AndroidUtilities.dp(288.0F)) {
        this.minusDy = (-AndroidUtilities.dp(288.0F));
      }
    }
  }
  
  private void onPageScrolled(int paramInt1, int paramInt2, int paramInt3)
  {
    boolean bool = true;
    int j = 0;
    if (this.stickersTab == null) {
      return;
    }
    int i = paramInt2;
    if (paramInt2 == 0) {
      i = AndroidUtilities.displaySize.x;
    }
    paramInt2 = 0;
    Object localObject;
    if (paramInt1 == 5)
    {
      paramInt2 = -paramInt3;
      paramInt1 = paramInt2;
      if (this.listener != null)
      {
        localObject = this.listener;
        if (paramInt3 != 0)
        {
          label58:
          ((Listener)localObject).onStickersTab(bool);
          paramInt1 = paramInt2;
        }
      }
      else
      {
        label69:
        if (this.emojiTab.getTranslationX() == paramInt1) {
          break label185;
        }
        this.emojiTab.setTranslationX(paramInt1);
        this.stickersTab.setTranslationX(i + paramInt1);
        localObject = this.stickersTab;
        if (paramInt1 >= 0) {
          break label187;
        }
      }
    }
    label185:
    label187:
    for (paramInt1 = j;; paramInt1 = 4)
    {
      ((ScrollSlidingTabStrip)localObject).setVisibility(paramInt1);
      return;
      bool = false;
      break label58;
      if (paramInt1 == 6)
      {
        paramInt2 = -i;
        paramInt1 = paramInt2;
        if (this.listener == null) {
          break label69;
        }
        this.listener.onStickersTab(true);
        paramInt1 = paramInt2;
        break label69;
      }
      paramInt1 = paramInt2;
      if (this.listener == null) {
        break label69;
      }
      this.listener.onStickersTab(false);
      paramInt1 = paramInt2;
      break label69;
      break;
    }
  }
  
  private void postBackspaceRunnable(final int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (!EmojiView.this.backspacePressed) {
          return;
        }
        if ((EmojiView.this.listener != null) && (EmojiView.this.listener.onBackspace())) {
          EmojiView.this.backspaceButton.performHapticFeedback(3);
        }
        EmojiView.access$6402(EmojiView.this, true);
        EmojiView.this.postBackspaceRunnable(Math.max(50, paramInt - 100));
      }
    }, paramInt);
  }
  
  private void reloadStickersAdapter()
  {
    if (this.stickersGridAdapter != null) {
      this.stickersGridAdapter.notifyDataSetChanged();
    }
    if (this.trendingGridAdapter != null) {
      this.trendingGridAdapter.notifyDataSetChanged();
    }
    if (this.stickersSearchGridAdapter != null) {
      this.stickersSearchGridAdapter.notifyDataSetChanged();
    }
    if (StickerPreviewViewer.getInstance().isVisible()) {
      StickerPreviewViewer.getInstance().close();
    }
    StickerPreviewViewer.getInstance().reset();
  }
  
  private void saveNewPage()
  {
    int i;
    if (this.pager.getCurrentItem() == 6) {
      if ((this.gifsGridView != null) && (this.gifsGridView.getVisibility() == 0)) {
        i = 2;
      }
    }
    for (;;)
    {
      if (this.currentPage != i)
      {
        this.currentPage = i;
        MessagesController.getGlobalEmojiSettings().edit().putInt("selected_page", i).commit();
      }
      return;
      i = 1;
      continue;
      i = 0;
    }
  }
  
  private void showGifTab()
  {
    this.gifsGridView.setVisibility(0);
    this.stickersGridView.setVisibility(8);
    this.firstAttach = true;
    this.searchEditTextContainer.setVisibility(8);
    this.stickersEmptyView.setVisibility(8);
    this.trendingGridView.setVisibility(8);
    ScrollSlidingTabStrip localScrollSlidingTabStrip = this.stickersTab;
    int j = this.gifTabNum;
    if (this.recentTabBum > 0) {}
    for (int i = this.recentTabBum;; i = this.stickersTabOffset)
    {
      localScrollSlidingTabStrip.onPageScrolled(j + 1, i + 1);
      saveNewPage();
      return;
    }
  }
  
  private void showTrendingTab()
  {
    this.trendingGridView.setVisibility(0);
    this.stickersGridView.setVisibility(8);
    this.firstAttach = true;
    this.searchEditTextContainer.setVisibility(8);
    this.stickersEmptyView.setVisibility(8);
    this.gifsGridView.setVisibility(8);
    ScrollSlidingTabStrip localScrollSlidingTabStrip = this.stickersTab;
    int j = this.trendingTabNum;
    if (this.recentTabBum > 0) {}
    for (int i = this.recentTabBum;; i = this.stickersTabOffset)
    {
      localScrollSlidingTabStrip.onPageScrolled(j + 1, i + 1);
      saveNewPage();
      return;
    }
  }
  
  private void updateStickerTabs()
  {
    if (this.stickersTab == null) {
      return;
    }
    this.recentTabBum = -2;
    this.favTabBum = -2;
    this.gifTabNum = -2;
    this.trendingTabNum = -2;
    this.stickersTabOffset = 0;
    int j = this.stickersTab.getCurrentPosition();
    this.stickersTab.removeTabs();
    Object localObject = getContext().getResources().getDrawable(2131165417);
    Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
    this.stickersTab.addIconTab((Drawable)localObject);
    if ((this.showGifs) && (!this.recentGifs.isEmpty()))
    {
      localObject = getContext().getResources().getDrawable(2131165420);
      Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
      this.stickersTab.addIconTab((Drawable)localObject);
      this.gifTabNum = this.stickersTabOffset;
      this.stickersTabOffset += 1;
    }
    ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getUnreadStickerSets();
    if ((this.trendingGridAdapter != null) && (this.trendingGridAdapter.getItemCount() != 0) && (!localArrayList.isEmpty()))
    {
      localObject = getContext().getResources().getDrawable(2131165422);
      Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
      localObject = this.stickersTab.addIconTabWithCounter((Drawable)localObject);
      this.trendingTabNum = this.stickersTabOffset;
      this.stickersTabOffset += 1;
      ((TextView)localObject).setText(String.format("%d", new Object[] { Integer.valueOf(localArrayList.size()) }));
    }
    if (!this.favouriteStickers.isEmpty())
    {
      this.favTabBum = this.stickersTabOffset;
      this.stickersTabOffset += 1;
      localObject = getContext().getResources().getDrawable(2131165651);
      Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
      this.stickersTab.addIconTab((Drawable)localObject);
    }
    if (!this.recentStickers.isEmpty())
    {
      this.recentTabBum = this.stickersTabOffset;
      this.stickersTabOffset += 1;
      localObject = getContext().getResources().getDrawable(2131165415);
      Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
      this.stickersTab.addIconTab((Drawable)localObject);
    }
    this.stickerSets.clear();
    this.groupStickerSet = null;
    this.groupStickerPackPosition = -1;
    this.groupStickerPackNum = -10;
    localObject = DataQuery.getInstance(this.currentAccount).getStickerSets(0);
    int i = 0;
    TLRPC.TL_messages_stickerSet localTL_messages_stickerSet;
    if (i < ((ArrayList)localObject).size())
    {
      localTL_messages_stickerSet = (TLRPC.TL_messages_stickerSet)((ArrayList)localObject).get(i);
      if ((localTL_messages_stickerSet.set.archived) || (localTL_messages_stickerSet.documents == null) || (localTL_messages_stickerSet.documents.isEmpty())) {}
      for (;;)
      {
        i += 1;
        break;
        this.stickerSets.add(localTL_messages_stickerSet);
      }
    }
    long l;
    boolean bool;
    if (this.info != null)
    {
      l = MessagesController.getEmojiSettings(this.currentAccount).getLong("group_hide_stickers_" + this.info.id, -1L);
      localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.info.id));
      if ((localObject != null) && (this.info.stickerset != null) && (ChatObject.hasAdminRights((TLRPC.Chat)localObject))) {
        break label823;
      }
      if (l == -1L) {
        break label817;
      }
      bool = true;
      this.groupStickersHidden = bool;
      label605:
      if (this.info.stickerset == null) {
        break label890;
      }
      localTL_messages_stickerSet = DataQuery.getInstance(this.currentAccount).getGroupStickerSetById(this.info.stickerset);
      if ((localTL_messages_stickerSet != null) && (localTL_messages_stickerSet.documents != null) && (!localTL_messages_stickerSet.documents.isEmpty()) && (localTL_messages_stickerSet.set != null))
      {
        localObject = new TLRPC.TL_messages_stickerSet();
        ((TLRPC.TL_messages_stickerSet)localObject).documents = localTL_messages_stickerSet.documents;
        ((TLRPC.TL_messages_stickerSet)localObject).packs = localTL_messages_stickerSet.packs;
        ((TLRPC.TL_messages_stickerSet)localObject).set = localTL_messages_stickerSet.set;
        if (!this.groupStickersHidden) {
          break label866;
        }
        this.groupStickerPackNum = this.stickerSets.size();
        this.stickerSets.add(localObject);
        label733:
        if (!this.info.can_set_stickers) {
          break label884;
        }
        label743:
        this.groupStickerSet = ((TLRPC.TL_messages_stickerSet)localObject);
      }
    }
    label749:
    i = 0;
    label751:
    if (i < this.stickerSets.size())
    {
      if (i == this.groupStickerPackNum)
      {
        localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.info.id));
        if (localObject == null)
        {
          this.stickerSets.remove(0);
          i -= 1;
        }
      }
      for (;;)
      {
        i += 1;
        break label751;
        label817:
        bool = false;
        break;
        label823:
        if (this.info.stickerset == null) {
          break label605;
        }
        if (l == this.info.stickerset.id) {}
        for (bool = true;; bool = false)
        {
          this.groupStickersHidden = bool;
          break;
        }
        label866:
        this.groupStickerPackNum = 0;
        this.stickerSets.add(0, localObject);
        break label733;
        label884:
        localObject = null;
        break label743;
        label890:
        if (!this.info.can_set_stickers) {
          break label749;
        }
        localObject = new TLRPC.TL_messages_stickerSet();
        if (this.groupStickersHidden)
        {
          this.groupStickerPackNum = this.stickerSets.size();
          this.stickerSets.add(localObject);
          break label749;
        }
        this.groupStickerPackNum = 0;
        this.stickerSets.add(0, localObject);
        break label749;
        this.stickersTab.addStickerTab((TLRPC.Chat)localObject);
        continue;
        this.stickersTab.addStickerTab((TLRPC.Document)((TLRPC.TL_messages_stickerSet)this.stickerSets.get(i)).documents.get(0));
      }
    }
    if ((this.trendingGridAdapter != null) && (this.trendingGridAdapter.getItemCount() != 0) && (localArrayList.isEmpty()))
    {
      localObject = getContext().getResources().getDrawable(2131165422);
      Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
      this.trendingTabNum = (this.stickersTabOffset + this.stickerSets.size());
      this.stickersTab.addIconTab((Drawable)localObject);
    }
    localObject = getContext().getResources().getDrawable(2131165421);
    Theme.setDrawableColorByKey((Drawable)localObject, "chat_emojiPanelIcon");
    this.stickersTab.addIconTab((Drawable)localObject);
    this.stickersTab.updateTabStyles();
    if (j != 0) {
      this.stickersTab.onPageScrolled(j, j);
    }
    if ((this.switchToGifTab) && (this.gifTabNum >= 0) && (this.gifsGridView.getVisibility() != 0))
    {
      showGifTab();
      this.switchToGifTab = false;
    }
    checkPanels();
  }
  
  private void updateVisibleTrendingSets()
  {
    if ((this.trendingGridAdapter == null) || (this.trendingGridAdapter == null)) {}
    int k;
    TLRPC.StickerSetCovered localStickerSetCovered;
    boolean bool;
    label121:
    int i;
    label173:
    int j;
    label196:
    int m;
    int n;
    for (;;)
    {
      return;
      try
      {
        int i1 = this.trendingGridView.getChildCount();
        k = 0;
        if (k < i1)
        {
          localObject = this.trendingGridView.getChildAt(k);
          if ((!(localObject instanceof FeaturedStickerSetInfoCell)) || ((RecyclerListView.Holder)this.trendingGridView.getChildViewHolder((View)localObject) == null)) {
            break label320;
          }
          localObject = (FeaturedStickerSetInfoCell)localObject;
          ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getUnreadStickerSets();
          localStickerSetCovered = ((FeaturedStickerSetInfoCell)localObject).getStickerSet();
          if ((localArrayList == null) || (!localArrayList.contains(Long.valueOf(localStickerSetCovered.set.id)))) {
            break;
          }
          bool = true;
          ((FeaturedStickerSetInfoCell)localObject).setStickerSet(localStickerSetCovered, bool);
          if (bool) {
            DataQuery.getInstance(this.currentAccount).markFaturedStickersByIdAsRead(localStickerSetCovered.set.id);
          }
          if (this.installingStickerSets.indexOfKey(localStickerSetCovered.set.id) < 0) {
            break label256;
          }
          i = 1;
          if (this.removingStickerSets.indexOfKey(localStickerSetCovered.set.id) < 0) {
            break label261;
          }
          j = 1;
        }
      }
      catch (Exception localException)
      {
        Object localObject;
        label232:
        FileLog.e(localException);
        return;
      }
    }
    if ((i != 0) && (((FeaturedStickerSetInfoCell)localObject).isInstalled()))
    {
      this.installingStickerSets.remove(localStickerSetCovered.set.id);
      m = 0;
      n = j;
    }
    for (;;)
    {
      ((FeaturedStickerSetInfoCell)localObject).setDrawProgress(bool);
      break label320;
      bool = false;
      break label121;
      label256:
      i = 0;
      break label173;
      label261:
      j = 0;
      break label327;
      m = i;
      n = j;
      if (j != 0)
      {
        m = i;
        n = j;
        if (!localException.isInstalled())
        {
          this.removingStickerSets.remove(localStickerSetCovered.set.id);
          n = 0;
          m = i;
        }
      }
      label320:
      label327:
      while ((m == 0) && (n == 0))
      {
        bool = false;
        break label232;
        k += 1;
        break;
        if (i != 0) {
          break label196;
        }
        m = i;
        n = j;
        if (j != 0) {
          break label196;
        }
      }
      bool = true;
    }
  }
  
  public void addEmojiToRecent(String paramString)
  {
    if (!Emoji.isValidEmoji(paramString)) {
      return;
    }
    Emoji.addRecentEmoji(paramString);
    if ((getVisibility() != 0) || (this.pager.getCurrentItem() != 0)) {
      Emoji.sortEmoji();
    }
    Emoji.saveRecentEmoji();
    ((EmojiGridAdapter)this.adapters.get(0)).notifyDataSetChanged();
  }
  
  public void addRecentGif(TLRPC.Document paramDocument)
  {
    if (paramDocument == null) {}
    boolean bool;
    do
    {
      return;
      bool = this.recentGifs.isEmpty();
      this.recentGifs = DataQuery.getInstance(this.currentAccount).getRecentGifs();
      if (this.gifsAdapter != null) {
        this.gifsAdapter.notifyDataSetChanged();
      }
    } while (!bool);
    updateStickerTabs();
  }
  
  public void addRecentSticker(TLRPC.Document paramDocument)
  {
    if (paramDocument == null) {}
    boolean bool;
    do
    {
      return;
      DataQuery.getInstance(this.currentAccount).addRecentSticker(0, paramDocument, (int)(System.currentTimeMillis() / 1000L), false);
      bool = this.recentStickers.isEmpty();
      this.recentStickers = DataQuery.getInstance(this.currentAccount).getRecentStickers(0);
      if (this.stickersGridAdapter != null) {
        this.stickersGridAdapter.notifyDataSetChanged();
      }
    } while (!bool);
    updateStickerTabs();
  }
  
  public boolean areThereAnyStickers()
  {
    return (this.stickersGridAdapter != null) && (this.stickersGridAdapter.getItemCount() > 0);
  }
  
  public void clearRecentEmoji()
  {
    Emoji.clearRecentEmoji();
    ((EmojiGridAdapter)this.adapters.get(0)).notifyDataSetChanged();
  }
  
  public void closeSearch(boolean paramBoolean)
  {
    closeSearch(paramBoolean, -1L);
  }
  
  public void closeSearch(boolean paramBoolean, long paramLong)
  {
    if (this.searchAnimation != null)
    {
      this.searchAnimation.cancel();
      this.searchAnimation = null;
    }
    this.searchEditText.setText("");
    if (paramLong != -1L)
    {
      TLRPC.TL_messages_stickerSet localTL_messages_stickerSet = DataQuery.getInstance(this.currentAccount).getStickerSetById(paramLong);
      if (localTL_messages_stickerSet != null)
      {
        int i = this.stickersGridAdapter.getPositionForPack(localTL_messages_stickerSet);
        if (i >= 0) {
          this.stickersLayoutManager.scrollToPositionWithOffset(i, AndroidUtilities.dp(60.0F));
        }
      }
    }
    if (paramBoolean)
    {
      this.searchAnimation = new AnimatorSet();
      this.searchAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.stickersTab, "translationY", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.stickersGridView, "translationY", new float[] { AndroidUtilities.dp(48.0F) - this.searchFieldHeight }), ObjectAnimator.ofFloat(this.searchEditTextContainer, "translationY", new float[] { AndroidUtilities.dp(48.0F) - this.searchFieldHeight }) });
      this.searchAnimation.setDuration(200L);
      this.searchAnimation.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
      this.searchAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(EmojiView.this.searchAnimation)) {
            EmojiView.access$3402(EmojiView.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(EmojiView.this.searchAnimation))
          {
            EmojiView.this.stickersLayoutManager.findFirstVisibleItemPosition();
            int j = EmojiView.this.stickersLayoutManager.findFirstVisibleItemPosition();
            int i = 0;
            if (j != -1) {
              i = (int)(EmojiView.this.stickersLayoutManager.findViewByPosition(j).getTop() + EmojiView.this.stickersGridView.getTranslationY());
            }
            EmojiView.this.stickersGridView.setTranslationY(0.0F);
            EmojiView.this.stickersGridView.setPadding(0, AndroidUtilities.dp(52.0F), 0, 0);
            if (j != -1) {
              EmojiView.this.stickersLayoutManager.scrollToPositionWithOffset(j, i - EmojiView.this.stickersGridView.getPaddingTop());
            }
            EmojiView.access$3402(EmojiView.this, null);
          }
        }
      });
      this.searchAnimation.start();
      return;
    }
    this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
    this.searchEditTextContainer.setTranslationY(AndroidUtilities.dp(48.0F) - this.searchFieldHeight);
    this.stickersTab.setTranslationY(0.0F);
    this.stickersGridView.setPadding(0, AndroidUtilities.dp(52.0F), 0, 0);
    this.listener.onSearchOpenClose(false);
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.stickersDidLoaded) {
      if (((Integer)paramVarArgs[0]).intValue() == 0)
      {
        if (this.trendingGridAdapter != null)
        {
          if (!this.trendingLoaded) {
            break label50;
          }
          updateVisibleTrendingSets();
        }
        updateStickerTabs();
        reloadStickersAdapter();
        checkPanels();
      }
    }
    for (;;)
    {
      return;
      label50:
      this.trendingGridAdapter.notifyDataSetChanged();
      break;
      if (paramInt1 == NotificationCenter.recentDocumentsDidLoaded)
      {
        boolean bool = ((Boolean)paramVarArgs[0]).booleanValue();
        paramInt1 = ((Integer)paramVarArgs[1]).intValue();
        if ((bool) || (paramInt1 == 0) || (paramInt1 == 2)) {
          checkDocuments(bool);
        }
      }
      else
      {
        if (paramInt1 == NotificationCenter.featuredStickersDidLoaded)
        {
          if (this.trendingGridAdapter != null)
          {
            if (this.featuredStickersHash != DataQuery.getInstance(this.currentAccount).getFeaturesStickersHashWithoutUnread()) {
              this.trendingLoaded = false;
            }
            if (!this.trendingLoaded) {
              break label196;
            }
            updateVisibleTrendingSets();
          }
          while (this.pagerSlidingTabStrip != null)
          {
            paramInt2 = this.pagerSlidingTabStrip.getChildCount();
            paramInt1 = 0;
            while (paramInt1 < paramInt2)
            {
              this.pagerSlidingTabStrip.getChildAt(paramInt1).invalidate();
              paramInt1 += 1;
            }
            label196:
            this.trendingGridAdapter.notifyDataSetChanged();
          }
          updateStickerTabs();
          return;
        }
        if (paramInt1 == NotificationCenter.groupStickersDidLoaded)
        {
          if ((this.info != null) && (this.info.stickerset != null) && (this.info.stickerset.id == ((Long)paramVarArgs[0]).longValue())) {
            updateStickerTabs();
          }
        }
        else if ((paramInt1 == NotificationCenter.emojiDidLoaded) && (this.stickersGridView != null))
        {
          paramInt2 = this.stickersGridView.getChildCount();
          paramInt1 = 0;
          while (paramInt1 < paramInt2)
          {
            paramVarArgs = this.stickersGridView.getChildAt(paramInt1);
            if ((paramVarArgs instanceof StickerSetNameCell)) {
              paramVarArgs.invalidate();
            }
            paramInt1 += 1;
          }
        }
      }
    }
  }
  
  public int getCurrentPage()
  {
    return this.currentPage;
  }
  
  public void hideSearchKeyboard()
  {
    AndroidUtilities.hideKeyboard(this.searchEditText);
  }
  
  public void invalidateViews()
  {
    int i = 0;
    while (i < this.emojiGrids.size())
    {
      ((GridView)this.emojiGrids.get(i)).invalidateViews();
      i += 1;
    }
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (this.stickersGridAdapter != null)
    {
      NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.stickersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recentImagesDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.featuredStickersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.groupStickersDidLoaded);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          EmojiView.this.updateStickerTabs();
          EmojiView.this.reloadStickersAdapter();
        }
      });
    }
  }
  
  public void onDestroy()
  {
    if (this.stickersGridAdapter != null)
    {
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.stickersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recentDocumentsDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.featuredStickersDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.groupStickersDidLoaded);
    }
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    if ((this.pickerViewPopup != null) && (this.pickerViewPopup.isShowing())) {
      this.pickerViewPopup.dismiss();
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.lastNotifyWidth != paramInt3 - paramInt1)
    {
      this.lastNotifyWidth = (paramInt3 - paramInt1);
      reloadStickersAdapter();
    }
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
  }
  
  public void onMeasure(int paramInt1, int paramInt2)
  {
    this.isLayout = true;
    if (AndroidUtilities.isInMultiwindow) {
      if (this.currentBackgroundType != 1)
      {
        if (Build.VERSION.SDK_INT >= 21)
        {
          setOutlineProvider((ViewOutlineProvider)this.outlineProvider);
          setClipToOutline(true);
          setElevation(AndroidUtilities.dp(2.0F));
        }
        setBackgroundResource(2131165649);
        getBackground().setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelBackground"), PorterDuff.Mode.MULTIPLY));
        this.emojiTab.setBackgroundDrawable(null);
        this.currentBackgroundType = 1;
      }
    }
    for (;;)
    {
      FrameLayout.LayoutParams localLayoutParams2 = (FrameLayout.LayoutParams)this.emojiTab.getLayoutParams();
      Object localObject = null;
      localLayoutParams2.width = View.MeasureSpec.getSize(paramInt1);
      if (this.stickersTab != null)
      {
        FrameLayout.LayoutParams localLayoutParams1 = (FrameLayout.LayoutParams)this.stickersTab.getLayoutParams();
        localObject = localLayoutParams1;
        if (localLayoutParams1 != null)
        {
          localLayoutParams1.width = localLayoutParams2.width;
          localObject = localLayoutParams1;
        }
      }
      if (localLayoutParams2.width != this.oldWidth)
      {
        if ((this.stickersTab != null) && (localObject != null))
        {
          onPageScrolled(this.pager.getCurrentItem(), localLayoutParams2.width - getPaddingLeft() - getPaddingRight(), 0);
          this.stickersTab.setLayoutParams((ViewGroup.LayoutParams)localObject);
        }
        this.emojiTab.setLayoutParams(localLayoutParams2);
        this.oldWidth = localLayoutParams2.width;
      }
      super.onMeasure(View.MeasureSpec.makeMeasureSpec(localLayoutParams2.width, 1073741824), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt2), 1073741824));
      this.isLayout = false;
      return;
      if (this.currentBackgroundType != 0)
      {
        if (Build.VERSION.SDK_INT >= 21)
        {
          setOutlineProvider(null);
          setClipToOutline(false);
          setElevation(0.0F);
        }
        setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
        this.emojiTab.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
        this.currentBackgroundType = 0;
      }
    }
  }
  
  public void onOpen(boolean paramBoolean)
  {
    boolean bool = true;
    if (this.stickersTab != null)
    {
      if ((this.currentPage != 0) && (this.currentChatId != 0)) {
        this.currentPage = 0;
      }
      if ((this.currentPage != 0) && (!paramBoolean)) {
        break label74;
      }
      if (this.pager.getCurrentItem() == 6)
      {
        ViewPager localViewPager = this.pager;
        if (paramBoolean) {
          break label69;
        }
        paramBoolean = bool;
        localViewPager.setCurrentItem(0, paramBoolean);
      }
    }
    label69:
    label74:
    label191:
    do
    {
      do
      {
        do
        {
          return;
          paramBoolean = false;
          break;
          if (this.currentPage != 1) {
            break label191;
          }
          if (this.pager.getCurrentItem() != 6) {
            this.pager.setCurrentItem(6);
          }
        } while (this.stickersTab.getCurrentPosition() != this.gifTabNum + 1);
        if (this.recentTabBum >= 0)
        {
          this.stickersTab.selectTab(this.recentTabBum + 1);
          return;
        }
        if (this.favTabBum >= 0)
        {
          this.stickersTab.selectTab(this.favTabBum + 1);
          return;
        }
        if (this.gifTabNum >= 0)
        {
          this.stickersTab.selectTab(this.gifTabNum + 2);
          return;
        }
        this.stickersTab.selectTab(1);
        return;
      } while (this.currentPage != 2);
      if (this.pager.getCurrentItem() != 6) {
        this.pager.setCurrentItem(6);
      }
    } while (this.stickersTab.getCurrentPosition() == this.gifTabNum + 1);
    if ((this.gifTabNum >= 0) && (!this.recentGifs.isEmpty()))
    {
      this.stickersTab.selectTab(this.gifTabNum + 1);
      return;
    }
    this.switchToGifTab = true;
  }
  
  public void requestLayout()
  {
    if (this.isLayout) {
      return;
    }
    super.requestLayout();
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    updateStickerTabs();
  }
  
  public void setDragListener(DragListener paramDragListener)
  {
    this.dragListener = paramDragListener;
  }
  
  public void setListener(Listener paramListener)
  {
    this.listener = paramListener;
  }
  
  public void setStickersBanned(boolean paramBoolean, int paramInt)
  {
    View localView;
    if (paramBoolean)
    {
      this.currentChatId = paramInt;
      localView = this.pagerSlidingTabStrip.getTab(6);
      if (localView != null) {
        if (this.currentChatId == 0) {
          break label78;
        }
      }
    }
    label78:
    for (float f = 0.5F;; f = 1.0F)
    {
      localView.setAlpha(f);
      if ((this.currentChatId != 0) && (this.pager.getCurrentItem() == 6)) {
        this.pager.setCurrentItem(0);
      }
      return;
      this.currentChatId = 0;
      break;
    }
  }
  
  public void setVisibility(int paramInt)
  {
    super.setVisibility(paramInt);
    Listener localListener;
    if (paramInt != 8)
    {
      Emoji.sortEmoji();
      ((EmojiGridAdapter)this.adapters.get(0)).notifyDataSetChanged();
      if (this.stickersGridAdapter != null)
      {
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.stickersDidLoaded);
        NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recentDocumentsDidLoaded);
        updateStickerTabs();
        reloadStickersAdapter();
        if ((this.gifsGridView != null) && (this.gifsGridView.getVisibility() == 0) && (this.listener != null))
        {
          localListener = this.listener;
          if ((this.pager == null) || (this.pager.getCurrentItem() < 6)) {
            break label200;
          }
        }
      }
    }
    label200:
    for (boolean bool = true;; bool = false)
    {
      localListener.onGifTab(bool);
      if (this.trendingGridAdapter != null)
      {
        this.trendingLoaded = false;
        this.trendingGridAdapter.notifyDataSetChanged();
      }
      checkDocuments(true);
      checkDocuments(false);
      DataQuery.getInstance(this.currentAccount).loadRecents(0, true, true, false);
      DataQuery.getInstance(this.currentAccount).loadRecents(0, false, true, false);
      DataQuery.getInstance(this.currentAccount).loadRecents(2, false, true, false);
      return;
    }
  }
  
  public void showSearchField(boolean paramBoolean)
  {
    int i = this.stickersLayoutManager.findFirstVisibleItemPosition();
    if (paramBoolean) {
      if ((i == 1) || (i == 2))
      {
        this.stickersLayoutManager.scrollToPosition(0);
        this.stickersTab.setTranslationY(0.0F);
      }
    }
    while (i != 0) {
      return;
    }
    this.stickersLayoutManager.scrollToPositionWithOffset(1, 0);
  }
  
  public void showStickerBanHint()
  {
    if (this.mediaBanTooltip.getVisibility() == 0) {}
    Object localObject;
    do
    {
      return;
      localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.currentChatId));
    } while ((localObject == null) || (((TLRPC.Chat)localObject).banned_rights == null));
    if (AndroidUtilities.isBannedForever(((TLRPC.Chat)localObject).banned_rights.until_date)) {
      this.mediaBanTooltip.setText(LocaleController.getString("AttachStickersRestrictedForever", 2131493042));
    }
    for (;;)
    {
      this.mediaBanTooltip.setVisibility(0);
      localObject = new AnimatorSet();
      ((AnimatorSet)localObject).playTogether(new Animator[] { ObjectAnimator.ofFloat(this.mediaBanTooltip, "alpha", new float[] { 0.0F, 1.0F }) });
      ((AnimatorSet)localObject).addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (EmojiView.this.mediaBanTooltip == null) {
                return;
              }
              AnimatorSet localAnimatorSet = new AnimatorSet();
              localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(EmojiView.this.mediaBanTooltip, "alpha", new float[] { 0.0F }) });
              localAnimatorSet.addListener(new AnimatorListenerAdapter()
              {
                public void onAnimationEnd(Animator paramAnonymous3Animator)
                {
                  if (EmojiView.this.mediaBanTooltip != null) {
                    EmojiView.this.mediaBanTooltip.setVisibility(4);
                  }
                }
              });
              localAnimatorSet.setDuration(300L);
              localAnimatorSet.start();
            }
          }, 5000L);
        }
      });
      ((AnimatorSet)localObject).setDuration(300L);
      ((AnimatorSet)localObject).start();
      return;
      this.mediaBanTooltip.setText(LocaleController.formatString("AttachStickersRestricted", 2131493041, new Object[] { LocaleController.formatDateForBan(((TLRPC.Chat)localObject).banned_rights.until_date) }));
    }
  }
  
  public void switchToGifRecent()
  {
    if ((this.gifTabNum >= 0) && (!this.recentGifs.isEmpty())) {
      this.stickersTab.selectTab(this.gifTabNum + 1);
    }
    for (;;)
    {
      this.pager.setCurrentItem(6);
      return;
      this.switchToGifTab = true;
    }
  }
  
  public void updateUIColors()
  {
    if (AndroidUtilities.isInMultiwindow) {
      getBackground().setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelBackground"), PorterDuff.Mode.MULTIPLY));
    }
    for (;;)
    {
      if (this.searchEditTextContainer != null) {
        this.searchEditTextContainer.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
      }
      if (this.dotPaint != null) {
        this.dotPaint.setColor(Theme.getColor("chat_emojiPanelNewTrending"));
      }
      if (this.stickersGridView != null) {
        this.stickersGridView.setGlowColor(Theme.getColor("chat_emojiPanelBackground"));
      }
      if (this.trendingGridView != null) {
        this.trendingGridView.setGlowColor(Theme.getColor("chat_emojiPanelBackground"));
      }
      if (this.stickersEmptyView != null) {
        this.stickersEmptyView.setTextColor(Theme.getColor("chat_emojiPanelEmptyText"));
      }
      if (this.stickersTab != null)
      {
        this.stickersTab.setIndicatorColor(Theme.getColor("chat_emojiPanelStickerPackSelector"));
        this.stickersTab.setUnderlineColor(Theme.getColor("chat_emojiPanelStickerPackSelector"));
        this.stickersTab.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
      }
      if (this.pagerSlidingTabStrip != null)
      {
        this.pagerSlidingTabStrip.setIndicatorColor(Theme.getColor("chat_emojiPanelIconSelector"));
        this.pagerSlidingTabStrip.setUnderlineColor(Theme.getColor("chat_emojiPanelShadowLine"));
      }
      if (this.backspaceButton != null) {
        this.backspaceButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelBackspace"), PorterDuff.Mode.MULTIPLY));
      }
      if (this.searchIconImageView != null) {
        this.clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelIcon"), PorterDuff.Mode.MULTIPLY));
      }
      if (this.shadowLine != null) {
        this.shadowLine.setBackgroundColor(Theme.getColor("chat_emojiPanelShadowLine"));
      }
      if (this.noRecentTextView != null) {
        this.noRecentTextView.setTextColor(Theme.getColor("chat_emojiPanelEmptyText"));
      }
      if (this.mediaBanTooltip != null)
      {
        ((ShapeDrawable)this.mediaBanTooltip.getBackground()).getPaint().setColor(Theme.getColor("chat_gifSaveHintBackground"));
        this.mediaBanTooltip.setTextColor(Theme.getColor("chat_gifSaveHintText"));
      }
      Theme.setDrawableColorByKey(this.stickersDrawable, "chat_emojiPanelIcon");
      int i = 0;
      while (i < this.icons.length - 1)
      {
        Theme.setEmojiDrawableColor(this.icons[i], Theme.getColor("chat_emojiPanelIcon"), false);
        Theme.setEmojiDrawableColor(this.icons[i], Theme.getColor("chat_emojiPanelIconSelected"), true);
        i += 1;
      }
      setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
      this.emojiTab.setBackgroundColor(Theme.getColor("chat_emojiPanelBackground"));
    }
    if (this.searchBackground != null)
    {
      Theme.setDrawableColorByKey(this.searchBackground.getBackground(), "chat_emojiSearchBackground");
      this.searchBackground.invalidate();
    }
  }
  
  public static abstract interface DragListener
  {
    public abstract void onDrag(int paramInt);
    
    public abstract void onDragCancel();
    
    public abstract void onDragEnd(float paramFloat);
    
    public abstract void onDragStart();
  }
  
  private class EmojiColorPickerView
    extends View
  {
    private Drawable arrowDrawable = getResources().getDrawable(2131165656);
    private int arrowX;
    private Drawable backgroundDrawable = getResources().getDrawable(2131165655);
    private String currentEmoji;
    private RectF rect = new RectF();
    private Paint rectPaint = new Paint(1);
    private int selection;
    
    public EmojiColorPickerView(Context paramContext)
    {
      super();
    }
    
    public String getEmoji()
    {
      return this.currentEmoji;
    }
    
    public int getSelection()
    {
      return this.selection;
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      Object localObject = this.backgroundDrawable;
      int i = getMeasuredWidth();
      float f;
      int j;
      label67:
      int k;
      label95:
      label135:
      String str;
      if (AndroidUtilities.isTablet())
      {
        f = 60.0F;
        ((Drawable)localObject).setBounds(0, 0, i, AndroidUtilities.dp(f));
        this.backgroundDrawable.draw(paramCanvas);
        localObject = this.arrowDrawable;
        i = this.arrowX;
        j = AndroidUtilities.dp(9.0F);
        if (!AndroidUtilities.isTablet()) {
          break label373;
        }
        f = 55.5F;
        k = AndroidUtilities.dp(f);
        int m = this.arrowX;
        int n = AndroidUtilities.dp(9.0F);
        if (!AndroidUtilities.isTablet()) {
          break label379;
        }
        f = 55.5F;
        ((Drawable)localObject).setBounds(i - j, k, n + m, AndroidUtilities.dp(f + 8.0F));
        this.arrowDrawable.draw(paramCanvas);
        if (this.currentEmoji == null) {
          return;
        }
        i = 0;
        if (i >= 6) {
          return;
        }
        j = EmojiView.this.emojiSize * i + AndroidUtilities.dp(i * 4 + 5);
        k = AndroidUtilities.dp(9.0F);
        if (this.selection == i)
        {
          this.rect.set(j, k - (int)AndroidUtilities.dpf2(3.5F), EmojiView.this.emojiSize + j, EmojiView.this.emojiSize + k + AndroidUtilities.dp(3.0F));
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), this.rectPaint);
        }
        str = this.currentEmoji;
        localObject = str;
        if (i != 0) {
          switch (i)
          {
          default: 
            localObject = "";
          }
        }
      }
      for (;;)
      {
        localObject = EmojiView.addColorToCode(str, (String)localObject);
        localObject = Emoji.getEmojiBigDrawable((String)localObject);
        if (localObject != null)
        {
          ((Drawable)localObject).setBounds(j, k, EmojiView.this.emojiSize + j, EmojiView.this.emojiSize + k);
          ((Drawable)localObject).draw(paramCanvas);
        }
        i += 1;
        break label135;
        f = 52.0F;
        break;
        label373:
        f = 47.5F;
        break label67;
        label379:
        f = 47.5F;
        break label95;
        localObject = "🏻";
        continue;
        localObject = "🏼";
        continue;
        localObject = "🏽";
        continue;
        localObject = "🏾";
        continue;
        localObject = "🏿";
      }
    }
    
    public void setEmoji(String paramString, int paramInt)
    {
      this.currentEmoji = paramString;
      this.arrowX = paramInt;
      this.rectPaint.setColor(788529152);
      invalidate();
    }
    
    public void setSelection(int paramInt)
    {
      if (this.selection == paramInt) {
        return;
      }
      this.selection = paramInt;
      invalidate();
    }
  }
  
  private class EmojiGridAdapter
    extends BaseAdapter
  {
    private int emojiPage;
    
    public EmojiGridAdapter(int paramInt)
    {
      this.emojiPage = paramInt;
    }
    
    public int getCount()
    {
      if (this.emojiPage == -1) {
        return Emoji.recentEmoji.size();
      }
      return EmojiData.dataColored[this.emojiPage].length;
    }
    
    public Object getItem(int paramInt)
    {
      return null;
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      paramView = (EmojiView.ImageViewEmoji)paramView;
      paramViewGroup = paramView;
      if (paramView == null) {
        paramViewGroup = new EmojiView.ImageViewEmoji(EmojiView.this, EmojiView.this.getContext());
      }
      Object localObject;
      if (this.emojiPage == -1)
      {
        localObject = (String)Emoji.recentEmoji.get(paramInt);
        paramView = (View)localObject;
      }
      for (;;)
      {
        paramViewGroup.setImageDrawable(Emoji.getEmojiBigDrawable(paramView));
        paramViewGroup.setTag(localObject);
        return paramViewGroup;
        String str1 = EmojiData.dataColored[this.emojiPage][paramInt];
        String str2 = str1;
        String str3 = (String)Emoji.emojiColor.get(str1);
        localObject = str1;
        paramView = str2;
        if (str3 != null)
        {
          paramView = EmojiView.addColorToCode(str2, str3);
          localObject = str1;
        }
      }
    }
    
    public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver)
    {
      if (paramDataSetObserver != null) {
        super.unregisterDataSetObserver(paramDataSetObserver);
      }
    }
  }
  
  private class EmojiPagesAdapter
    extends PagerAdapter
    implements PagerSlidingTabStrip.IconTabProvider
  {
    private EmojiPagesAdapter() {}
    
    public boolean canScrollToTab(int paramInt)
    {
      if ((paramInt == 6) && (EmojiView.this.currentChatId != 0))
      {
        EmojiView.this.showStickerBanHint();
        return false;
      }
      return true;
    }
    
    public void customOnDraw(Canvas paramCanvas, int paramInt)
    {
      if ((paramInt == 6) && (!DataQuery.getInstance(EmojiView.this.currentAccount).getUnreadStickerSets().isEmpty()) && (EmojiView.this.dotPaint != null))
      {
        paramInt = paramCanvas.getWidth() / 2;
        int i = AndroidUtilities.dp(9.0F);
        int j = paramCanvas.getHeight() / 2;
        int k = AndroidUtilities.dp(8.0F);
        paramCanvas.drawCircle(paramInt + i, j - k, AndroidUtilities.dp(5.0F), EmojiView.this.dotPaint);
      }
    }
    
    public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject)
    {
      if (paramInt == 6) {}
      for (paramObject = EmojiView.this.stickersWrap;; paramObject = (View)EmojiView.this.views.get(paramInt))
      {
        paramViewGroup.removeView((View)paramObject);
        return;
      }
    }
    
    public int getCount()
    {
      return EmojiView.this.views.size();
    }
    
    public Drawable getPageIconDrawable(int paramInt)
    {
      return EmojiView.this.icons[paramInt];
    }
    
    public Object instantiateItem(ViewGroup paramViewGroup, int paramInt)
    {
      if (paramInt == 6) {}
      for (Object localObject = EmojiView.this.stickersWrap;; localObject = (View)EmojiView.this.views.get(paramInt))
      {
        paramViewGroup.addView((View)localObject);
        return localObject;
      }
    }
    
    public boolean isViewFromObject(View paramView, Object paramObject)
    {
      return paramView == paramObject;
    }
    
    public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver)
    {
      if (paramDataSetObserver != null) {
        super.unregisterDataSetObserver(paramDataSetObserver);
      }
    }
  }
  
  private class EmojiPopupWindow
    extends PopupWindow
  {
    private ViewTreeObserver.OnScrollChangedListener mSuperScrollListener;
    private ViewTreeObserver mViewTreeObserver;
    
    public EmojiPopupWindow()
    {
      init();
    }
    
    public EmojiPopupWindow(int paramInt1, int paramInt2)
    {
      super(paramInt2);
      init();
    }
    
    public EmojiPopupWindow(Context paramContext)
    {
      super();
      init();
    }
    
    public EmojiPopupWindow(View paramView)
    {
      super();
      init();
    }
    
    public EmojiPopupWindow(View paramView, int paramInt1, int paramInt2)
    {
      super(paramInt1, paramInt2);
      init();
    }
    
    public EmojiPopupWindow(View paramView, int paramInt1, int paramInt2, boolean paramBoolean)
    {
      super(paramInt1, paramInt2, paramBoolean);
      init();
    }
    
    private void init()
    {
      if (EmojiView.superListenerField != null) {}
      try
      {
        this.mSuperScrollListener = ((ViewTreeObserver.OnScrollChangedListener)EmojiView.superListenerField.get(this));
        EmojiView.superListenerField.set(this, EmojiView.NOP);
        return;
      }
      catch (Exception localException)
      {
        this.mSuperScrollListener = null;
      }
    }
    
    private void registerListener(View paramView)
    {
      if (this.mSuperScrollListener != null) {
        if (paramView.getWindowToken() == null) {
          break label73;
        }
      }
      label73:
      for (paramView = paramView.getViewTreeObserver();; paramView = null)
      {
        if (paramView != this.mViewTreeObserver)
        {
          if ((this.mViewTreeObserver != null) && (this.mViewTreeObserver.isAlive())) {
            this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
          }
          this.mViewTreeObserver = paramView;
          if (paramView != null) {
            paramView.addOnScrollChangedListener(this.mSuperScrollListener);
          }
        }
        return;
      }
    }
    
    private void unregisterListener()
    {
      if ((this.mSuperScrollListener != null) && (this.mViewTreeObserver != null))
      {
        if (this.mViewTreeObserver.isAlive()) {
          this.mViewTreeObserver.removeOnScrollChangedListener(this.mSuperScrollListener);
        }
        this.mViewTreeObserver = null;
      }
    }
    
    public void dismiss()
    {
      setFocusable(false);
      try
      {
        super.dismiss();
        unregisterListener();
        return;
      }
      catch (Exception localException)
      {
        for (;;) {}
      }
    }
    
    public void showAsDropDown(View paramView, int paramInt1, int paramInt2)
    {
      try
      {
        super.showAsDropDown(paramView, paramInt1, paramInt2);
        registerListener(paramView);
        return;
      }
      catch (Exception paramView)
      {
        FileLog.e(paramView);
      }
    }
    
    public void showAtLocation(View paramView, int paramInt1, int paramInt2, int paramInt3)
    {
      super.showAtLocation(paramView, paramInt1, paramInt2, paramInt3);
      unregisterListener();
    }
    
    public void update(View paramView, int paramInt1, int paramInt2)
    {
      super.update(paramView, paramInt1, paramInt2);
      registerListener(paramView);
    }
    
    public void update(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      super.update(paramView, paramInt1, paramInt2, paramInt3, paramInt4);
      registerListener(paramView);
    }
  }
  
  private class GifsAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public GifsAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      return EmojiView.this.recentGifs.size();
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return false;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      TLRPC.Document localDocument = (TLRPC.Document)EmojiView.this.recentGifs.get(paramInt);
      if (localDocument != null) {
        ((ContextLinkCell)paramViewHolder.itemView).setGif(localDocument, false);
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      return new RecyclerListView.Holder(new ContextLinkCell(this.mContext));
    }
  }
  
  private class ImageViewEmoji
    extends ImageView
  {
    private float lastX;
    private float lastY;
    private boolean touched;
    private float touchedX;
    private float touchedY;
    
    public ImageViewEmoji(Context paramContext)
    {
      super();
      setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          EmojiView.ImageViewEmoji.this.sendEmoji(null);
        }
      });
      setOnLongClickListener(new View.OnLongClickListener()
      {
        public boolean onLongClick(View paramAnonymousView)
        {
          int j = 0;
          String str = (String)paramAnonymousView.getTag();
          if (EmojiData.emojiColoredMap.containsKey(str))
          {
            EmojiView.ImageViewEmoji.access$202(EmojiView.ImageViewEmoji.this, true);
            EmojiView.ImageViewEmoji.access$302(EmojiView.ImageViewEmoji.this, EmojiView.ImageViewEmoji.this.lastX);
            EmojiView.ImageViewEmoji.access$502(EmojiView.ImageViewEmoji.this, EmojiView.ImageViewEmoji.this.lastY);
            Object localObject = (String)Emoji.emojiColor.get(str);
            int i;
            label156:
            int k;
            if (localObject != null)
            {
              i = -1;
              switch (((String)localObject).hashCode())
              {
              default: 
                switch (i)
                {
                default: 
                  paramAnonymousView.getLocationOnScreen(EmojiView.this.location);
                  k = EmojiView.this.emojiSize;
                  int m = EmojiView.this.pickerView.getSelection();
                  int n = EmojiView.this.pickerView.getSelection();
                  if (AndroidUtilities.isTablet())
                  {
                    i = 5;
                    label220:
                    k = m * k + AndroidUtilities.dp(n * 4 - i);
                    if (EmojiView.this.location[0] - k >= AndroidUtilities.dp(5.0F)) {
                      break label607;
                    }
                    i = k + (EmojiView.this.location[0] - k - AndroidUtilities.dp(5.0F));
                    label286:
                    k = -i;
                    i = j;
                    if (paramAnonymousView.getTop() < 0) {
                      i = paramAnonymousView.getTop();
                    }
                    localObject = EmojiView.this.pickerView;
                    if (!AndroidUtilities.isTablet()) {
                      break label697;
                    }
                  }
                  break;
                }
                break;
              }
            }
            label607:
            label697:
            for (float f = 30.0F;; f = 22.0F)
            {
              ((EmojiView.EmojiColorPickerView)localObject).setEmoji(str, AndroidUtilities.dp(f) - k + (int)AndroidUtilities.dpf2(0.5F));
              EmojiView.this.pickerViewPopup.setFocusable(true);
              EmojiView.this.pickerViewPopup.showAsDropDown(paramAnonymousView, k, -paramAnonymousView.getMeasuredHeight() - EmojiView.this.popupHeight + (paramAnonymousView.getMeasuredHeight() - EmojiView.this.emojiSize) / 2 - i);
              paramAnonymousView.getParent().requestDisallowInterceptTouchEvent(true);
              return true;
              if (!((String)localObject).equals("🏻")) {
                break;
              }
              i = 0;
              break;
              if (!((String)localObject).equals("🏼")) {
                break;
              }
              i = 1;
              break;
              if (!((String)localObject).equals("🏽")) {
                break;
              }
              i = 2;
              break;
              if (!((String)localObject).equals("🏾")) {
                break;
              }
              i = 3;
              break;
              if (!((String)localObject).equals("🏿")) {
                break;
              }
              i = 4;
              break;
              EmojiView.this.pickerView.setSelection(1);
              break label156;
              EmojiView.this.pickerView.setSelection(2);
              break label156;
              EmojiView.this.pickerView.setSelection(3);
              break label156;
              EmojiView.this.pickerView.setSelection(4);
              break label156;
              EmojiView.this.pickerView.setSelection(5);
              break label156;
              EmojiView.this.pickerView.setSelection(0);
              break label156;
              i = 1;
              break label220;
              i = k;
              if (EmojiView.this.location[0] - k + EmojiView.this.popupWidth <= AndroidUtilities.displaySize.x - AndroidUtilities.dp(5.0F)) {
                break label286;
              }
              i = k + (EmojiView.this.location[0] - k + EmojiView.this.popupWidth - (AndroidUtilities.displaySize.x - AndroidUtilities.dp(5.0F)));
              break label286;
            }
          }
          if (EmojiView.this.pager.getCurrentItem() == 0) {
            EmojiView.this.listener.onClearEmojiRecent();
          }
          return false;
        }
      });
      setBackgroundDrawable(Theme.getSelectorDrawable(false));
      setScaleType(ImageView.ScaleType.CENTER);
    }
    
    private void sendEmoji(String paramString)
    {
      String str1;
      if (paramString != null)
      {
        str1 = paramString;
        new SpannableStringBuilder().append(str1);
        if (paramString != null) {
          break label106;
        }
        paramString = str1;
        if (EmojiView.this.pager.getCurrentItem() != 0)
        {
          str2 = (String)Emoji.emojiColor.get(str1);
          paramString = str1;
          if (str2 != null) {
            paramString = EmojiView.addColorToCode(str1, str2);
          }
        }
        EmojiView.this.addEmojiToRecent(paramString);
        if (EmojiView.this.listener != null) {
          EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(paramString));
        }
      }
      label106:
      while (EmojiView.this.listener == null)
      {
        String str2;
        return;
        str1 = (String)getTag();
        break;
      }
      EmojiView.this.listener.onEmojiSelected(Emoji.fixEmoji(paramString));
    }
    
    public void onMeasure(int paramInt1, int paramInt2)
    {
      setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), View.MeasureSpec.getSize(paramInt1));
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent)
    {
      if (this.touched)
      {
        if ((paramMotionEvent.getAction() != 1) && (paramMotionEvent.getAction() != 3)) {
          break label286;
        }
        if ((EmojiView.this.pickerViewPopup != null) && (EmojiView.this.pickerViewPopup.isShowing()))
        {
          EmojiView.this.pickerViewPopup.dismiss();
          localObject = null;
        }
        switch (EmojiView.this.pickerView.getSelection())
        {
        default: 
          str = (String)getTag();
          if (EmojiView.this.pager.getCurrentItem() != 0) {
            if (localObject != null)
            {
              Emoji.emojiColor.put(str, localObject);
              localObject = EmojiView.addColorToCode(str, (String)localObject);
              setImageDrawable(Emoji.getEmojiBigDrawable((String)localObject));
              sendEmoji(null);
              Emoji.saveEmojiColors();
              this.touched = false;
              this.touchedX = -10000.0F;
              this.touchedY = -10000.0F;
            }
          }
          break;
        }
      }
      label286:
      while (paramMotionEvent.getAction() != 2) {
        for (;;)
        {
          String str;
          this.lastX = paramMotionEvent.getX();
          this.lastY = paramMotionEvent.getY();
          return super.onTouchEvent(paramMotionEvent);
          Object localObject = "🏻";
          continue;
          localObject = "🏼";
          continue;
          localObject = "🏽";
          continue;
          localObject = "🏾";
          continue;
          localObject = "🏿";
          continue;
          Emoji.emojiColor.remove(str);
          localObject = str;
          continue;
          if (localObject != null) {
            sendEmoji(EmojiView.addColorToCode(str, (String)localObject));
          } else {
            sendEmoji(str);
          }
        }
      }
      int j = 0;
      int i = j;
      if (this.touchedX != -10000.0F)
      {
        if ((Math.abs(this.touchedX - paramMotionEvent.getX()) > AndroidUtilities.getPixelsInCM(0.2F, true)) || (Math.abs(this.touchedY - paramMotionEvent.getY()) > AndroidUtilities.getPixelsInCM(0.2F, false)))
        {
          this.touchedX = -10000.0F;
          this.touchedY = -10000.0F;
          i = j;
        }
      }
      else
      {
        label371:
        if (i != 0) {
          break label485;
        }
        getLocationOnScreen(EmojiView.this.location);
        float f1 = EmojiView.this.location[0];
        float f2 = paramMotionEvent.getX();
        EmojiView.this.pickerView.getLocationOnScreen(EmojiView.this.location);
        j = (int)((f1 + f2 - (EmojiView.this.location[0] + AndroidUtilities.dp(3.0F))) / (EmojiView.this.emojiSize + AndroidUtilities.dp(4.0F)));
        if (j >= 0) {
          break label487;
        }
        i = 0;
      }
      for (;;)
      {
        EmojiView.this.pickerView.setSelection(i);
        break;
        i = 1;
        break label371;
        label485:
        break;
        label487:
        i = j;
        if (j > 5) {
          i = 5;
        }
      }
    }
  }
  
  public static abstract interface Listener
  {
    public abstract boolean isExpanded();
    
    public abstract boolean isSearchOpened();
    
    public abstract boolean onBackspace();
    
    public abstract void onClearEmojiRecent();
    
    public abstract void onEmojiSelected(String paramString);
    
    public abstract void onGifSelected(TLRPC.Document paramDocument);
    
    public abstract void onGifTab(boolean paramBoolean);
    
    public abstract void onSearchOpenClose(boolean paramBoolean);
    
    public abstract void onShowStickerSet(TLRPC.StickerSet paramStickerSet, TLRPC.InputStickerSet paramInputStickerSet);
    
    public abstract void onStickerSelected(TLRPC.Document paramDocument);
    
    public abstract void onStickerSetAdd(TLRPC.StickerSetCovered paramStickerSetCovered);
    
    public abstract void onStickerSetRemove(TLRPC.StickerSetCovered paramStickerSetCovered);
    
    public abstract void onStickersGroupClick(int paramInt);
    
    public abstract void onStickersSettingsClick();
    
    public abstract void onStickersTab(boolean paramBoolean);
  }
  
  private class StickersGridAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private SparseArray<Object> cache = new SparseArray();
    private Context context;
    private HashMap<Object, Integer> packStartPosition = new HashMap();
    private SparseIntArray positionToRow = new SparseIntArray();
    private SparseArray<Object> rowStartPack = new SparseArray();
    private int stickersPerRow;
    private int totalItems;
    
    public StickersGridAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public Object getItem(int paramInt)
    {
      return this.cache.get(paramInt);
    }
    
    public int getItemCount()
    {
      if (this.totalItems != 0) {
        return this.totalItems + 1;
      }
      return 0;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (paramInt == 0) {
        return 4;
      }
      Object localObject = this.cache.get(paramInt);
      if (localObject != null)
      {
        if ((localObject instanceof TLRPC.Document)) {
          return 0;
        }
        if ((localObject instanceof String)) {
          return 3;
        }
        return 2;
      }
      return 1;
    }
    
    public int getPositionForPack(Object paramObject)
    {
      paramObject = (Integer)this.packStartPosition.get(paramObject);
      if (paramObject == null) {
        return -1;
      }
      return ((Integer)paramObject).intValue();
    }
    
    public int getTabForPosition(int paramInt)
    {
      int i = paramInt;
      if (paramInt == 0) {
        i = 1;
      }
      if (this.stickersPerRow == 0)
      {
        int j = EmojiView.this.getMeasuredWidth();
        paramInt = j;
        if (j == 0) {
          paramInt = AndroidUtilities.displaySize.x;
        }
        this.stickersPerRow = (paramInt / AndroidUtilities.dp(72.0F));
      }
      paramInt = this.positionToRow.get(i, Integer.MIN_VALUE);
      if (paramInt == Integer.MIN_VALUE) {
        return EmojiView.this.stickerSets.size() - 1 + EmojiView.this.stickersTabOffset;
      }
      Object localObject = this.rowStartPack.get(paramInt);
      if ((localObject instanceof String))
      {
        if ("recent".equals(localObject)) {
          return EmojiView.this.recentTabBum;
        }
        return EmojiView.this.favTabBum;
      }
      localObject = (TLRPC.TL_messages_stickerSet)localObject;
      paramInt = EmojiView.this.stickerSets.indexOf(localObject);
      return EmojiView.this.stickersTabOffset + paramInt;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return false;
    }
    
    public void notifyDataSetChanged()
    {
      int j = EmojiView.this.getMeasuredWidth();
      int i = j;
      if (j == 0) {
        i = AndroidUtilities.displaySize.x;
      }
      this.stickersPerRow = (i / AndroidUtilities.dp(72.0F));
      EmojiView.this.stickersLayoutManager.setSpanCount(this.stickersPerRow);
      this.rowStartPack.clear();
      this.packStartPosition.clear();
      this.positionToRow.clear();
      this.cache.clear();
      this.totalItems = 0;
      ArrayList localArrayList = EmojiView.this.stickerSets;
      int k = 0;
      j = -3;
      if (j < localArrayList.size())
      {
        Object localObject2 = null;
        Object localObject1;
        if (j == -3)
        {
          localObject1 = this.cache;
          i = this.totalItems;
          this.totalItems = (i + 1);
          ((SparseArray)localObject1).put(i, "search");
          i = k + 1;
        }
        for (;;)
        {
          j += 1;
          k = i;
          break;
          if (j == -2)
          {
            localObject1 = EmojiView.this.favouriteStickers;
            this.packStartPosition.put("fav", Integer.valueOf(this.totalItems));
          }
          int m;
          for (;;)
          {
            if (j != EmojiView.this.groupStickerPackNum) {
              break label410;
            }
            EmojiView.access$7902(EmojiView.this, this.totalItems);
            if (!((ArrayList)localObject1).isEmpty()) {
              break label410;
            }
            this.rowStartPack.put(k, localObject2);
            localObject1 = this.positionToRow;
            i = this.totalItems;
            m = k + 1;
            ((SparseIntArray)localObject1).put(i, k);
            this.rowStartPack.put(m, localObject2);
            localObject1 = this.positionToRow;
            k = this.totalItems;
            i = m + 1;
            ((SparseIntArray)localObject1).put(k + 1, m);
            localObject1 = this.cache;
            k = this.totalItems;
            this.totalItems = (k + 1);
            ((SparseArray)localObject1).put(k, localObject2);
            localObject1 = this.cache;
            k = this.totalItems;
            this.totalItems = (k + 1);
            ((SparseArray)localObject1).put(k, "group");
            break;
            if (j == -1)
            {
              localObject1 = EmojiView.this.recentStickers;
              this.packStartPosition.put("recent", Integer.valueOf(this.totalItems));
            }
            else
            {
              localObject2 = (TLRPC.TL_messages_stickerSet)localArrayList.get(j);
              localObject1 = ((TLRPC.TL_messages_stickerSet)localObject2).documents;
              this.packStartPosition.put(localObject2, Integer.valueOf(this.totalItems));
            }
          }
          label410:
          i = k;
          if (!((ArrayList)localObject1).isEmpty())
          {
            m = (int)Math.ceil(((ArrayList)localObject1).size() / this.stickersPerRow);
            if (localObject2 != null) {
              this.cache.put(this.totalItems, localObject2);
            }
            for (;;)
            {
              this.positionToRow.put(this.totalItems, k);
              i = 0;
              while (i < ((ArrayList)localObject1).size())
              {
                this.cache.put(i + 1 + this.totalItems, ((ArrayList)localObject1).get(i));
                this.positionToRow.put(i + 1 + this.totalItems, k + 1 + i / this.stickersPerRow);
                i += 1;
              }
              this.cache.put(this.totalItems, localObject1);
            }
            i = 0;
            while (i < m + 1) {
              if (localObject2 != null)
              {
                this.rowStartPack.put(k + i, localObject2);
                i += 1;
              }
              else
              {
                SparseArray localSparseArray = this.rowStartPack;
                if (j == -1) {}
                for (localObject1 = "recent";; localObject1 = "fav")
                {
                  localSparseArray.put(k + i, localObject1);
                  break;
                }
              }
            }
            this.totalItems += this.stickersPerRow * m + 1;
            i = k + (m + 1);
          }
        }
      }
      super.notifyDataSetChanged();
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      default: 
      case 0: 
      case 1: 
      case 2: 
        Object localObject;
        label387:
        label444:
        label449:
        do
        {
          do
          {
            return;
            localObject = (TLRPC.Document)this.cache.get(paramInt);
            paramViewHolder = (StickerEmojiCell)paramViewHolder.itemView;
            paramViewHolder.setSticker((TLRPC.Document)localObject, false);
            if ((EmojiView.this.recentStickers.contains(localObject)) || (EmojiView.this.favouriteStickers.contains(localObject))) {}
            for (bool = true;; bool = false)
            {
              paramViewHolder.setRecent(bool);
              return;
            }
            localObject = (EmptyCell)paramViewHolder.itemView;
            if (paramInt == this.totalItems)
            {
              paramInt = this.positionToRow.get(paramInt - 1, Integer.MIN_VALUE);
              if (paramInt == Integer.MIN_VALUE)
              {
                ((EmptyCell)localObject).setHeight(1);
                return;
              }
              paramViewHolder = this.rowStartPack.get(paramInt);
              if ((paramViewHolder instanceof TLRPC.TL_messages_stickerSet)) {
                paramViewHolder = ((TLRPC.TL_messages_stickerSet)paramViewHolder).documents;
              }
              while (paramViewHolder == null)
              {
                ((EmptyCell)localObject).setHeight(1);
                return;
                if ((paramViewHolder instanceof String))
                {
                  if ("recent".equals(paramViewHolder)) {
                    paramViewHolder = EmojiView.this.recentStickers;
                  } else {
                    paramViewHolder = EmojiView.this.favouriteStickers;
                  }
                }
                else {
                  paramViewHolder = null;
                }
              }
              if (paramViewHolder.isEmpty())
              {
                ((EmptyCell)localObject).setHeight(AndroidUtilities.dp(8.0F));
                return;
              }
              paramInt = EmojiView.this.pager.getHeight() - (int)Math.ceil(paramViewHolder.size() / this.stickersPerRow) * AndroidUtilities.dp(82.0F);
              if (paramInt > 0) {}
              for (;;)
              {
                ((EmptyCell)localObject).setHeight(paramInt);
                return;
                paramInt = 1;
              }
            }
            ((EmptyCell)localObject).setHeight(AndroidUtilities.dp(82.0F));
            return;
            localObject = (StickerSetNameCell)paramViewHolder.itemView;
            if (paramInt == EmojiView.this.groupStickerPackPosition)
            {
              if ((EmojiView.this.groupStickersHidden) && (EmojiView.this.groupStickerSet == null))
              {
                paramInt = 0;
                if (EmojiView.this.info == null) {
                  break label444;
                }
                paramViewHolder = MessagesController.getInstance(EmojiView.this.currentAccount).getChat(Integer.valueOf(EmojiView.this.info.id));
                if (paramViewHolder == null) {
                  break label449;
                }
              }
              for (paramViewHolder = paramViewHolder.title;; paramViewHolder = "Group Stickers")
              {
                ((StickerSetNameCell)localObject).setText(LocaleController.formatString("CurrentGroupStickers", 2131493317, new Object[] { paramViewHolder }), paramInt);
                return;
                if (EmojiView.this.groupStickerSet != null) {}
                for (paramInt = 2131165665;; paramInt = 2131165666) {
                  break;
                }
                paramViewHolder = null;
                break label387;
              }
            }
            paramViewHolder = this.cache.get(paramInt);
            if (!(paramViewHolder instanceof TLRPC.TL_messages_stickerSet)) {
              break;
            }
            paramViewHolder = (TLRPC.TL_messages_stickerSet)paramViewHolder;
          } while (paramViewHolder.set == null);
          ((StickerSetNameCell)localObject).setText(paramViewHolder.set.title, 0);
          return;
          if (paramViewHolder == EmojiView.this.recentStickers)
          {
            ((StickerSetNameCell)localObject).setText(LocaleController.getString("RecentStickers", 2131494216), 0);
            return;
          }
        } while (paramViewHolder != EmojiView.this.favouriteStickers);
        ((StickerSetNameCell)localObject).setText(LocaleController.getString("FavoriteStickers", 2131493534), 0);
        return;
      }
      paramViewHolder = (StickerSetGroupInfoCell)paramViewHolder.itemView;
      if (paramInt == this.totalItems - 1) {}
      for (boolean bool = true;; bool = false)
      {
        paramViewHolder.setIsLast(bool);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new StickerEmojiCell(this.context)
        {
          public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0F), 1073741824));
          }
        };
        continue;
        paramViewGroup = new EmptyCell(this.context);
        continue;
        paramViewGroup = new StickerSetNameCell(this.context);
        ((StickerSetNameCell)paramViewGroup).setOnIconClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (EmojiView.this.groupStickerSet != null)
            {
              if (EmojiView.this.listener != null) {
                EmojiView.this.listener.onStickersGroupClick(EmojiView.this.info.id);
              }
              return;
            }
            paramAnonymousView = MessagesController.getEmojiSettings(EmojiView.this.currentAccount).edit();
            String str = "group_hide_stickers_" + EmojiView.this.info.id;
            if (EmojiView.this.info.stickerset != null) {}
            for (long l = EmojiView.this.info.stickerset.id;; l = 0L)
            {
              paramAnonymousView.putLong(str, l).commit();
              EmojiView.this.updateStickerTabs();
              if (EmojiView.this.stickersGridAdapter == null) {
                break;
              }
              EmojiView.this.stickersGridAdapter.notifyDataSetChanged();
              return;
            }
          }
        });
        continue;
        paramViewGroup = new StickerSetGroupInfoCell(this.context);
        ((StickerSetGroupInfoCell)paramViewGroup).setAddOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (EmojiView.this.listener != null) {
              EmojiView.this.listener.onStickersGroupClick(EmojiView.this.info.id);
            }
          }
        });
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        continue;
        paramViewGroup = new View(this.context);
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, EmojiView.this.searchFieldHeight));
      }
    }
  }
  
  private class StickersSearchGridAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private SparseArray<Object> cache = new SparseArray();
    boolean cleared;
    private Context context;
    private ArrayList<ArrayList<TLRPC.Document>> emojiArrays = new ArrayList();
    private HashMap<ArrayList<TLRPC.Document>, String> emojiStickers = new HashMap();
    private ArrayList<TLRPC.TL_messages_stickerSet> localPacks = new ArrayList();
    private HashMap<TLRPC.TL_messages_stickerSet, Integer> localPacksByName = new HashMap();
    private HashMap<TLRPC.TL_messages_stickerSet, Boolean> localPacksByShortName = new HashMap();
    private SparseIntArray positionToRow = new SparseIntArray();
    private SparseArray<TLRPC.StickerSetCovered> positionsToSets = new SparseArray();
    private int reqId;
    private int reqId2;
    private SparseArray<Object> rowStartPack = new SparseArray();
    private String searchQuery;
    private Runnable searchRunnable = new Runnable()
    {
      private void clear()
      {
        if (EmojiView.StickersSearchGridAdapter.this.cleared) {
          return;
        }
        EmojiView.StickersSearchGridAdapter.this.cleared = true;
        EmojiView.StickersSearchGridAdapter.this.emojiStickers.clear();
        EmojiView.StickersSearchGridAdapter.this.emojiArrays.clear();
        EmojiView.StickersSearchGridAdapter.this.localPacks.clear();
        EmojiView.StickersSearchGridAdapter.this.serverPacks.clear();
        EmojiView.StickersSearchGridAdapter.this.localPacksByShortName.clear();
        EmojiView.StickersSearchGridAdapter.this.localPacksByName.clear();
      }
      
      public void run()
      {
        if (TextUtils.isEmpty(EmojiView.StickersSearchGridAdapter.this.searchQuery)) {
          return;
        }
        EmojiView.this.progressDrawable.startAnimation();
        EmojiView.StickersSearchGridAdapter.this.cleared = false;
        final ArrayList localArrayList = new ArrayList(0);
        final LongSparseArray localLongSparseArray = new LongSparseArray(0);
        HashMap localHashMap = DataQuery.getInstance(EmojiView.this.currentAccount).getAllStickers();
        Object localObject2;
        int k;
        if (EmojiView.StickersSearchGridAdapter.this.searchQuery.length() <= 14)
        {
          localObject2 = EmojiView.StickersSearchGridAdapter.this.searchQuery;
          int m = ((CharSequence)localObject2).length();
          i = 0;
          if (i < m)
          {
            if ((i < m - 1) && (((((CharSequence)localObject2).charAt(i) == 55356) && (((CharSequence)localObject2).charAt(i + 1) >= 57339) && (((CharSequence)localObject2).charAt(i + 1) <= 57343)) || ((((CharSequence)localObject2).charAt(i) == '‍') && ((((CharSequence)localObject2).charAt(i + 1) == '♀') || (((CharSequence)localObject2).charAt(i + 1) == '♂')))))
            {
              localObject1 = TextUtils.concat(new CharSequence[] { ((CharSequence)localObject2).subSequence(0, i), ((CharSequence)localObject2).subSequence(i + 2, ((CharSequence)localObject2).length()) });
              j = m - 2;
              k = i - 1;
            }
            for (;;)
            {
              i = k + 1;
              localObject2 = localObject1;
              m = j;
              break;
              k = i;
              localObject1 = localObject2;
              j = m;
              if (((CharSequence)localObject2).charAt(i) == 65039)
              {
                localObject1 = TextUtils.concat(new CharSequence[] { ((CharSequence)localObject2).subSequence(0, i), ((CharSequence)localObject2).subSequence(i + 1, ((CharSequence)localObject2).length()) });
                j = m - 1;
                k = i - 1;
              }
            }
          }
          if (localHashMap != null) {
            localObject1 = (ArrayList)localHashMap.get(((CharSequence)localObject2).toString());
          }
          while ((localObject1 != null) && (!((ArrayList)localObject1).isEmpty()))
          {
            clear();
            localArrayList.addAll((Collection)localObject1);
            i = 0;
            j = ((ArrayList)localObject1).size();
            for (;;)
            {
              if (i < j)
              {
                localObject2 = (TLRPC.Document)((ArrayList)localObject1).get(i);
                localLongSparseArray.put(((TLRPC.Document)localObject2).id, localObject2);
                i += 1;
                continue;
                localObject1 = null;
                break;
              }
            }
            EmojiView.StickersSearchGridAdapter.this.emojiStickers.put(localArrayList, EmojiView.StickersSearchGridAdapter.this.searchQuery);
            EmojiView.StickersSearchGridAdapter.this.emojiArrays.add(localArrayList);
          }
        }
        if ((localHashMap != null) && (!localHashMap.isEmpty()) && (EmojiView.StickersSearchGridAdapter.this.searchQuery.length() > 1))
        {
          label553:
          EmojiSuggestion localEmojiSuggestion;
          if (EmojiView.StickersSearchGridAdapter.this.searchQuery.startsWith(":"))
          {
            localObject1 = EmojiView.StickersSearchGridAdapter.this.searchQuery;
            localObject2 = Emoji.getSuggestion((String)localObject1);
            if (localObject2 == null) {
              break label710;
            }
            i = 0;
            j = Math.min(10, localObject2.length);
            if (i >= j) {
              break label710;
            }
            localEmojiSuggestion = (EmojiSuggestion)localObject2[i];
            localEmojiSuggestion.emoji = localEmojiSuggestion.emoji.replace("️", "");
            if (localHashMap == null) {
              break label704;
            }
          }
          label704:
          for (localObject1 = (ArrayList)localHashMap.get(localEmojiSuggestion.emoji);; localObject1 = null)
          {
            if ((localObject1 != null) && (!((ArrayList)localObject1).isEmpty()))
            {
              clear();
              if (!EmojiView.StickersSearchGridAdapter.this.emojiStickers.containsKey(localObject1))
              {
                EmojiView.StickersSearchGridAdapter.this.emojiStickers.put(localObject1, localEmojiSuggestion.emoji);
                EmojiView.StickersSearchGridAdapter.this.emojiArrays.add(localObject1);
              }
            }
            i += 1;
            break label553;
            localObject1 = ":" + EmojiView.StickersSearchGridAdapter.this.searchQuery;
            break;
          }
        }
        label710:
        final Object localObject1 = DataQuery.getInstance(EmojiView.this.currentAccount).getStickerSets(0);
        int i = 0;
        int j = ((ArrayList)localObject1).size();
        if (i < j)
        {
          localObject2 = (TLRPC.TL_messages_stickerSet)((ArrayList)localObject1).get(i);
          k = ((TLRPC.TL_messages_stickerSet)localObject2).set.title.toLowerCase().indexOf(EmojiView.StickersSearchGridAdapter.this.searchQuery);
          if (k >= 0) {
            if ((k == 0) || (((TLRPC.TL_messages_stickerSet)localObject2).set.title.charAt(k - 1) == ' '))
            {
              clear();
              EmojiView.StickersSearchGridAdapter.this.localPacks.add(localObject2);
              EmojiView.StickersSearchGridAdapter.this.localPacksByName.put(localObject2, Integer.valueOf(k));
            }
          }
          for (;;)
          {
            i += 1;
            break;
            if (((TLRPC.TL_messages_stickerSet)localObject2).set.short_name != null)
            {
              k = ((TLRPC.TL_messages_stickerSet)localObject2).set.short_name.toLowerCase().indexOf(EmojiView.StickersSearchGridAdapter.this.searchQuery);
              if ((k >= 0) && ((k == 0) || (((TLRPC.TL_messages_stickerSet)localObject2).set.short_name.charAt(k - 1) == ' ')))
              {
                clear();
                EmojiView.StickersSearchGridAdapter.this.localPacks.add(localObject2);
                EmojiView.StickersSearchGridAdapter.this.localPacksByShortName.put(localObject2, Boolean.valueOf(true));
              }
            }
          }
        }
        localObject1 = DataQuery.getInstance(EmojiView.this.currentAccount).getStickerSets(3);
        i = 0;
        j = ((ArrayList)localObject1).size();
        if (i < j)
        {
          localObject2 = (TLRPC.TL_messages_stickerSet)((ArrayList)localObject1).get(i);
          k = ((TLRPC.TL_messages_stickerSet)localObject2).set.title.toLowerCase().indexOf(EmojiView.StickersSearchGridAdapter.this.searchQuery);
          if (k >= 0) {
            if ((k == 0) || (((TLRPC.TL_messages_stickerSet)localObject2).set.title.charAt(k - 1) == ' '))
            {
              clear();
              EmojiView.StickersSearchGridAdapter.this.localPacks.add(localObject2);
              EmojiView.StickersSearchGridAdapter.this.localPacksByName.put(localObject2, Integer.valueOf(k));
            }
          }
          for (;;)
          {
            i += 1;
            break;
            if (((TLRPC.TL_messages_stickerSet)localObject2).set.short_name != null)
            {
              k = ((TLRPC.TL_messages_stickerSet)localObject2).set.short_name.toLowerCase().indexOf(EmojiView.StickersSearchGridAdapter.this.searchQuery);
              if ((k >= 0) && ((k == 0) || (((TLRPC.TL_messages_stickerSet)localObject2).set.short_name.charAt(k - 1) == ' ')))
              {
                clear();
                EmojiView.StickersSearchGridAdapter.this.localPacks.add(localObject2);
                EmojiView.StickersSearchGridAdapter.this.localPacksByShortName.put(localObject2, Boolean.valueOf(true));
              }
            }
          }
        }
        if (((!EmojiView.StickersSearchGridAdapter.this.localPacks.isEmpty()) || (!EmojiView.StickersSearchGridAdapter.this.emojiStickers.isEmpty())) && (EmojiView.this.stickersGridView.getAdapter() != EmojiView.this.stickersSearchGridAdapter)) {
          EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersSearchGridAdapter);
        }
        localObject1 = new TLRPC.TL_messages_searchStickerSets();
        ((TLRPC.TL_messages_searchStickerSets)localObject1).q = EmojiView.StickersSearchGridAdapter.this.searchQuery;
        EmojiView.StickersSearchGridAdapter.access$9502(EmojiView.StickersSearchGridAdapter.this, ConnectionsManager.getInstance(EmojiView.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
          {
            if ((paramAnonymous2TLObject instanceof TLRPC.TL_messages_foundStickerSets)) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (EmojiView.StickersSearchGridAdapter.1.1.this.val$req.q.equals(EmojiView.StickersSearchGridAdapter.this.searchQuery))
                  {
                    EmojiView.StickersSearchGridAdapter.1.this.clear();
                    EmojiView.this.progressDrawable.stopAnimation();
                    EmojiView.StickersSearchGridAdapter.access$9502(EmojiView.StickersSearchGridAdapter.this, 0);
                    if (EmojiView.this.stickersGridView.getAdapter() != EmojiView.this.stickersSearchGridAdapter) {
                      EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersSearchGridAdapter);
                    }
                    TLRPC.TL_messages_foundStickerSets localTL_messages_foundStickerSets = (TLRPC.TL_messages_foundStickerSets)paramAnonymous2TLObject;
                    EmojiView.StickersSearchGridAdapter.this.serverPacks.addAll(localTL_messages_foundStickerSets.sets);
                    EmojiView.StickersSearchGridAdapter.this.notifyDataSetChanged();
                  }
                }
              });
            }
          }
        }));
        if (Emoji.isValidEmoji(EmojiView.StickersSearchGridAdapter.this.searchQuery))
        {
          localObject1 = new TLRPC.TL_messages_getStickers();
          ((TLRPC.TL_messages_getStickers)localObject1).emoticon = EmojiView.StickersSearchGridAdapter.this.searchQuery;
          ((TLRPC.TL_messages_getStickers)localObject1).hash = "";
          ((TLRPC.TL_messages_getStickers)localObject1).exclude_featured = true;
          EmojiView.StickersSearchGridAdapter.access$9702(EmojiView.StickersSearchGridAdapter.this, ConnectionsManager.getInstance(EmojiView.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (EmojiView.StickersSearchGridAdapter.1.2.this.val$req2.emoticon.equals(EmojiView.StickersSearchGridAdapter.this.searchQuery))
                  {
                    EmojiView.StickersSearchGridAdapter.access$9702(EmojiView.StickersSearchGridAdapter.this, 0);
                    if ((paramAnonymous2TLObject instanceof TLRPC.TL_messages_stickers)) {
                      break label55;
                    }
                  }
                  label55:
                  int j;
                  do
                  {
                    return;
                    TLRPC.TL_messages_stickers localTL_messages_stickers = (TLRPC.TL_messages_stickers)paramAnonymous2TLObject;
                    j = EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersArray.size();
                    int i = 0;
                    int k = localTL_messages_stickers.stickers.size();
                    if (i < k)
                    {
                      TLRPC.Document localDocument = (TLRPC.Document)localTL_messages_stickers.stickers.get(i);
                      if (EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersMap.indexOfKey(localDocument.id) >= 0) {}
                      for (;;)
                      {
                        i += 1;
                        break;
                        EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersArray.add(localDocument);
                      }
                    }
                  } while (j == EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersArray.size());
                  EmojiView.StickersSearchGridAdapter.this.emojiStickers.put(EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersArray, EmojiView.StickersSearchGridAdapter.this.searchQuery);
                  if (j == 0) {
                    EmojiView.StickersSearchGridAdapter.this.emojiArrays.add(EmojiView.StickersSearchGridAdapter.1.2.this.val$emojiStickersArray);
                  }
                  EmojiView.StickersSearchGridAdapter.this.notifyDataSetChanged();
                }
              });
            }
          }));
        }
        EmojiView.StickersSearchGridAdapter.this.notifyDataSetChanged();
      }
    };
    private ArrayList<TLRPC.StickerSetCovered> serverPacks = new ArrayList();
    private int totalItems;
    
    public StickersSearchGridAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public Object getItem(int paramInt)
    {
      return this.cache.get(paramInt);
    }
    
    public int getItemCount()
    {
      if (this.totalItems != 1) {
        return this.totalItems + 1;
      }
      return 2;
    }
    
    public int getItemViewType(int paramInt)
    {
      int i = 1;
      if (paramInt == 0) {
        paramInt = 4;
      }
      Object localObject;
      do
      {
        return paramInt;
        if ((paramInt == 1) && (this.totalItems == 1)) {
          return 5;
        }
        localObject = this.cache.get(paramInt);
        paramInt = i;
      } while (localObject == null);
      if ((localObject instanceof TLRPC.Document)) {
        return 0;
      }
      if ((localObject instanceof TLRPC.StickerSetCovered)) {
        return 3;
      }
      return 2;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return false;
    }
    
    public void notifyDataSetChanged()
    {
      this.rowStartPack.clear();
      this.positionToRow.clear();
      this.cache.clear();
      this.positionsToSets.clear();
      this.totalItems = 0;
      int k = 0;
      int j = -1;
      int m = this.serverPacks.size();
      int n = this.localPacks.size();
      int i1 = this.emojiArrays.size();
      if (j < m + n + i1)
      {
        Object localObject2 = null;
        Object localObject1;
        int i;
        if (j == -1)
        {
          localObject1 = this.cache;
          i = this.totalItems;
          this.totalItems = (i + 1);
          ((SparseArray)localObject1).put(i, "search");
          i = k + 1;
        }
        for (;;)
        {
          j += 1;
          k = i;
          break;
          int i2;
          if (j < n)
          {
            localObject2 = (TLRPC.TL_messages_stickerSet)this.localPacks.get(j);
            localObject1 = ((TLRPC.TL_messages_stickerSet)localObject2).documents;
            i = k;
            if (((ArrayList)localObject1).isEmpty()) {
              continue;
            }
            i2 = (int)Math.ceil(((ArrayList)localObject1).size() / EmojiView.access$2200(EmojiView.this).stickersPerRow);
            if (localObject2 == null) {
              break label405;
            }
            this.cache.put(this.totalItems, localObject2);
          }
          for (;;)
          {
            if ((j >= n) && ((localObject2 instanceof TLRPC.StickerSetCovered))) {
              this.positionsToSets.put(this.totalItems, (TLRPC.StickerSetCovered)localObject2);
            }
            this.positionToRow.put(this.totalItems, k);
            i = 0;
            int i3 = ((ArrayList)localObject1).size();
            while (i < i3)
            {
              this.cache.put(i + 1 + this.totalItems, ((ArrayList)localObject1).get(i));
              this.positionToRow.put(i + 1 + this.totalItems, k + 1 + i / EmojiView.access$2200(EmojiView.this).stickersPerRow);
              if ((j >= n) && ((localObject2 instanceof TLRPC.StickerSetCovered))) {
                this.positionsToSets.put(this.totalItems + (i + 1), (TLRPC.StickerSetCovered)localObject2);
              }
              i += 1;
            }
            i = j - n;
            if (i < i1)
            {
              localObject1 = (ArrayList)this.emojiArrays.get(i);
              break;
            }
            localObject2 = (TLRPC.StickerSetCovered)this.serverPacks.get(i - i1);
            localObject1 = ((TLRPC.StickerSetCovered)localObject2).covers;
            break;
            label405:
            this.cache.put(this.totalItems, localObject1);
          }
          i = 0;
          if (i < i2 + 1)
          {
            if (localObject2 != null) {
              this.rowStartPack.put(k + i, localObject2);
            }
            for (;;)
            {
              i += 1;
              break;
              this.rowStartPack.put(k + i, localObject1);
            }
          }
          this.totalItems += EmojiView.access$2200(EmojiView.this).stickersPerRow * i2 + 1;
          i = k + (i2 + 1);
        }
      }
      super.notifyDataSetChanged();
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      Object localObject1;
      label467:
      label571:
      label620:
      label633:
      label676:
      label681:
      label731:
      label737:
      label759:
      do
      {
        boolean bool;
        int i;
        do
        {
          return;
          localObject1 = (TLRPC.Document)this.cache.get(paramInt);
          paramViewHolder = (StickerEmojiCell)paramViewHolder.itemView;
          paramViewHolder.setSticker((TLRPC.Document)localObject1, false);
          if ((EmojiView.this.recentStickers.contains(localObject1)) || (EmojiView.this.favouriteStickers.contains(localObject1))) {}
          for (bool = true;; bool = false)
          {
            paramViewHolder.setRecent(bool);
            return;
          }
          localObject1 = (EmptyCell)paramViewHolder.itemView;
          if (paramInt == this.totalItems)
          {
            paramInt = this.positionToRow.get(paramInt - 1, Integer.MIN_VALUE);
            if (paramInt == Integer.MIN_VALUE)
            {
              ((EmptyCell)localObject1).setHeight(1);
              return;
            }
            paramViewHolder = this.rowStartPack.get(paramInt);
            if ((paramViewHolder instanceof TLRPC.TL_messages_stickerSet)) {
              paramViewHolder = ((TLRPC.TL_messages_stickerSet)paramViewHolder).documents;
            }
            while (paramViewHolder == null)
            {
              ((EmptyCell)localObject1).setHeight(1);
              return;
              if ((paramViewHolder instanceof ArrayList)) {
                paramViewHolder = (ArrayList)paramViewHolder;
              } else {
                paramViewHolder = null;
              }
            }
            if (paramViewHolder.isEmpty())
            {
              ((EmptyCell)localObject1).setHeight(AndroidUtilities.dp(8.0F));
              return;
            }
            paramInt = EmojiView.this.pager.getHeight() - (int)Math.ceil(paramViewHolder.size() / EmojiView.access$2200(EmojiView.this).stickersPerRow) * AndroidUtilities.dp(82.0F);
            if (paramInt > 0) {}
            for (;;)
            {
              ((EmptyCell)localObject1).setHeight(paramInt);
              return;
              paramInt = 1;
            }
          }
          ((EmptyCell)localObject1).setHeight(AndroidUtilities.dp(82.0F));
          return;
          paramViewHolder = (StickerSetNameCell)paramViewHolder.itemView;
          localObject1 = this.cache.get(paramInt);
          if ((localObject1 instanceof TLRPC.TL_messages_stickerSet))
          {
            Object localObject2 = (TLRPC.TL_messages_stickerSet)localObject1;
            if ((!TextUtils.isEmpty(this.searchQuery)) && (this.localPacksByShortName.containsKey(localObject2)))
            {
              if (((TLRPC.TL_messages_stickerSet)localObject2).set != null) {
                paramViewHolder.setText(((TLRPC.TL_messages_stickerSet)localObject2).set.title, 0);
              }
              paramViewHolder.setUrl(((TLRPC.TL_messages_stickerSet)localObject2).set.short_name, this.searchQuery.length());
              return;
            }
            localObject1 = (Integer)this.localPacksByName.get(localObject2);
            if ((((TLRPC.TL_messages_stickerSet)localObject2).set != null) && (localObject1 != null))
            {
              localObject2 = ((TLRPC.TL_messages_stickerSet)localObject2).set.title;
              i = ((Integer)localObject1).intValue();
              if (TextUtils.isEmpty(this.searchQuery)) {
                break label467;
              }
            }
            for (paramInt = this.searchQuery.length();; paramInt = 0)
            {
              paramViewHolder.setText((CharSequence)localObject2, 0, i, paramInt);
              paramViewHolder.setUrl(null, 0);
              return;
            }
          }
        } while (!(localObject1 instanceof ArrayList));
        paramViewHolder.setText((CharSequence)this.emojiStickers.get(localObject1), 0);
        paramViewHolder.setUrl(null, 0);
        return;
        localObject1 = (TLRPC.StickerSetCovered)this.cache.get(paramInt);
        paramViewHolder = (FeaturedStickerSetInfoCell)paramViewHolder.itemView;
        int j;
        int k;
        if (EmojiView.this.installingStickerSets.indexOfKey(((TLRPC.StickerSetCovered)localObject1).set.id) >= 0)
        {
          paramInt = 1;
          if (EmojiView.this.removingStickerSets.indexOfKey(((TLRPC.StickerSetCovered)localObject1).set.id) < 0) {
            break label676;
          }
          i = 1;
          if (paramInt == 0)
          {
            j = paramInt;
            k = i;
            if (i == 0) {}
          }
          else
          {
            if ((paramInt == 0) || (!paramViewHolder.isInstalled())) {
              break label681;
            }
            EmojiView.this.installingStickerSets.remove(((TLRPC.StickerSetCovered)localObject1).set.id);
            j = 0;
            k = i;
          }
          if ((j == 0) && (k == 0)) {
            break label731;
          }
          bool = true;
          paramViewHolder.setDrawProgress(bool);
          if (!TextUtils.isEmpty(this.searchQuery)) {
            break label737;
          }
        }
        for (paramInt = -1;; paramInt = ((TLRPC.StickerSetCovered)localObject1).set.title.toLowerCase().indexOf(this.searchQuery))
        {
          if (paramInt < 0) {
            break label759;
          }
          paramViewHolder.setStickerSet((TLRPC.StickerSetCovered)localObject1, false, paramInt, this.searchQuery.length());
          return;
          paramInt = 0;
          break;
          i = 0;
          break label571;
          j = paramInt;
          k = i;
          if (i == 0) {
            break label620;
          }
          j = paramInt;
          k = i;
          if (paramViewHolder.isInstalled()) {
            break label620;
          }
          EmojiView.this.removingStickerSets.remove(((TLRPC.StickerSetCovered)localObject1).set.id);
          k = 0;
          j = paramInt;
          break label620;
          bool = false;
          break label633;
        }
        paramViewHolder.setStickerSet((TLRPC.StickerSetCovered)localObject1, false);
      } while ((TextUtils.isEmpty(this.searchQuery)) || (!((TLRPC.StickerSetCovered)localObject1).set.short_name.toLowerCase().startsWith(this.searchQuery)));
      paramViewHolder.setUrl(((TLRPC.StickerSetCovered)localObject1).set.short_name, this.searchQuery.length());
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new StickerEmojiCell(this.context)
        {
          public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0F), 1073741824));
          }
        };
        continue;
        paramViewGroup = new EmptyCell(this.context);
        continue;
        paramViewGroup = new StickerSetNameCell(this.context);
        continue;
        paramViewGroup = new FeaturedStickerSetInfoCell(this.context, 17);
        ((FeaturedStickerSetInfoCell)paramViewGroup).setAddOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = (FeaturedStickerSetInfoCell)paramAnonymousView.getParent();
            TLRPC.StickerSetCovered localStickerSetCovered = paramAnonymousView.getStickerSet();
            if ((EmojiView.this.installingStickerSets.indexOfKey(localStickerSetCovered.set.id) >= 0) || (EmojiView.this.removingStickerSets.indexOfKey(localStickerSetCovered.set.id) >= 0)) {
              return;
            }
            if (paramAnonymousView.isInstalled())
            {
              EmojiView.this.removingStickerSets.put(localStickerSetCovered.set.id, localStickerSetCovered);
              EmojiView.this.listener.onStickerSetRemove(paramAnonymousView.getStickerSet());
            }
            for (;;)
            {
              paramAnonymousView.setDrawProgress(true);
              return;
              EmojiView.this.installingStickerSets.put(localStickerSetCovered.set.id, localStickerSetCovered);
              EmojiView.this.listener.onStickerSetAdd(paramAnonymousView.getStickerSet());
            }
          }
        });
        continue;
        paramViewGroup = new View(this.context);
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, EmojiView.this.searchFieldHeight));
        continue;
        paramViewGroup = new FrameLayout(this.context)
        {
          protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec((int)((EmojiView.this.stickersGridView.getMeasuredHeight() - EmojiView.this.searchFieldHeight - AndroidUtilities.dp(8.0F)) / 3 * 1.7F), 1073741824));
          }
        };
        Object localObject = new ImageView(this.context);
        ((ImageView)localObject).setScaleType(ImageView.ScaleType.CENTER);
        ((ImageView)localObject).setImageResource(2131165661);
        ((ImageView)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelEmptyText"), PorterDuff.Mode.MULTIPLY));
        paramViewGroup.addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 0.0F, 0.0F, 48.0F));
        localObject = new TextView(this.context);
        ((TextView)localObject).setText(LocaleController.getString("NoStickersFound", 2131493915));
        ((TextView)localObject).setTextSize(1, 18.0F);
        ((TextView)localObject).setTextColor(Theme.getColor("chat_emojiPanelEmptyText"));
        ((TextView)localObject).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        paramViewGroup.addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 30.0F, 0.0F, 0.0F));
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
      }
    }
    
    public void search(String paramString)
    {
      if (this.reqId != 0)
      {
        ConnectionsManager.getInstance(EmojiView.this.currentAccount).cancelRequest(this.reqId, true);
        this.reqId = 0;
      }
      if (this.reqId2 != 0)
      {
        ConnectionsManager.getInstance(EmojiView.this.currentAccount).cancelRequest(this.reqId2, true);
        this.reqId2 = 0;
      }
      if (TextUtils.isEmpty(paramString))
      {
        this.searchQuery = null;
        this.localPacks.clear();
        this.emojiStickers.clear();
        this.serverPacks.clear();
        if (EmojiView.this.stickersGridView.getAdapter() != EmojiView.this.stickersGridAdapter) {
          EmojiView.this.stickersGridView.setAdapter(EmojiView.this.stickersGridAdapter);
        }
        notifyDataSetChanged();
      }
      for (;;)
      {
        AndroidUtilities.cancelRunOnUIThread(this.searchRunnable);
        AndroidUtilities.runOnUIThread(this.searchRunnable, 300L);
        return;
        this.searchQuery = paramString.toLowerCase();
      }
    }
  }
  
  private class TrendingGridAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private SparseArray<Object> cache = new SparseArray();
    private Context context;
    private SparseArray<TLRPC.StickerSetCovered> positionsToSets = new SparseArray();
    private ArrayList<TLRPC.StickerSetCovered> sets = new ArrayList();
    private int stickersPerRow;
    private int totalItems;
    
    public TrendingGridAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public Object getItem(int paramInt)
    {
      return this.cache.get(paramInt);
    }
    
    public int getItemCount()
    {
      return this.totalItems;
    }
    
    public int getItemViewType(int paramInt)
    {
      Object localObject = this.cache.get(paramInt);
      if (localObject != null)
      {
        if ((localObject instanceof TLRPC.Document)) {
          return 0;
        }
        return 2;
      }
      return 1;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return false;
    }
    
    public void notifyDataSetChanged()
    {
      int j = EmojiView.this.getMeasuredWidth();
      int i = j;
      int k;
      if (j == 0)
      {
        if (!AndroidUtilities.isTablet()) {
          break label106;
        }
        k = AndroidUtilities.displaySize.x;
        j = k * 35 / 100;
        i = j;
        if (j < AndroidUtilities.dp(320.0F)) {
          i = AndroidUtilities.dp(320.0F);
        }
      }
      label106:
      for (j = k - i;; j = AndroidUtilities.displaySize.x)
      {
        i = j;
        if (j == 0) {
          i = 1080;
        }
        this.stickersPerRow = Math.max(1, i / AndroidUtilities.dp(72.0F));
        EmojiView.this.trendingLayoutManager.setSpanCount(this.stickersPerRow);
        if (!EmojiView.this.trendingLoaded) {
          break;
        }
        return;
      }
      this.cache.clear();
      this.positionsToSets.clear();
      this.sets.clear();
      this.totalItems = 0;
      j = 0;
      ArrayList localArrayList = DataQuery.getInstance(EmojiView.this.currentAccount).getFeaturedStickerSets();
      i = 0;
      if (i < localArrayList.size())
      {
        TLRPC.StickerSetCovered localStickerSetCovered = (TLRPC.StickerSetCovered)localArrayList.get(i);
        k = j;
        if (!DataQuery.getInstance(EmojiView.this.currentAccount).isStickerPackInstalled(localStickerSetCovered.set.id)) {
          if ((!localStickerSetCovered.covers.isEmpty()) || (localStickerSetCovered.cover != null)) {
            break label237;
          }
        }
        for (k = j;; k = j + 1)
        {
          i += 1;
          j = k;
          break;
          label237:
          this.sets.add(localStickerSetCovered);
          this.positionsToSets.put(this.totalItems, localStickerSetCovered);
          SparseArray localSparseArray = this.cache;
          k = this.totalItems;
          this.totalItems = (k + 1);
          localSparseArray.put(k, Integer.valueOf(j));
          k = this.totalItems / this.stickersPerRow;
          if (!localStickerSetCovered.covers.isEmpty())
          {
            int n = (int)Math.ceil(localStickerSetCovered.covers.size() / this.stickersPerRow);
            m = 0;
            for (;;)
            {
              k = n;
              if (m >= localStickerSetCovered.covers.size()) {
                break;
              }
              this.cache.put(this.totalItems + m, localStickerSetCovered.covers.get(m));
              m += 1;
            }
          }
          k = 1;
          this.cache.put(this.totalItems, localStickerSetCovered.cover);
          int m = 0;
          while (m < this.stickersPerRow * k)
          {
            this.positionsToSets.put(this.totalItems + m, localStickerSetCovered);
            m += 1;
          }
          this.totalItems += this.stickersPerRow * k;
        }
      }
      if (this.totalItems != 0)
      {
        EmojiView.access$7202(EmojiView.this, true);
        EmojiView.access$7302(EmojiView.this, DataQuery.getInstance(EmojiView.this.currentAccount).getFeaturesStickersHashWithoutUnread());
      }
      super.notifyDataSetChanged();
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool2 = false;
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      case 0: 
        localObject = (TLRPC.Document)this.cache.get(paramInt);
        ((StickerEmojiCell)paramViewHolder.itemView).setSticker((TLRPC.Document)localObject, false);
        return;
      case 1: 
        ((EmptyCell)paramViewHolder.itemView).setHeight(AndroidUtilities.dp(82.0F));
        return;
      }
      ArrayList localArrayList = DataQuery.getInstance(EmojiView.this.currentAccount).getUnreadStickerSets();
      Object localObject = (TLRPC.StickerSetCovered)this.sets.get(((Integer)this.cache.get(paramInt)).intValue());
      boolean bool1;
      label209:
      int i;
      label232:
      int j;
      int k;
      if ((localArrayList != null) && (localArrayList.contains(Long.valueOf(((TLRPC.StickerSetCovered)localObject).set.id))))
      {
        bool1 = true;
        paramViewHolder = (FeaturedStickerSetInfoCell)paramViewHolder.itemView;
        paramViewHolder.setStickerSet((TLRPC.StickerSetCovered)localObject, bool1);
        if (bool1) {
          DataQuery.getInstance(EmojiView.this.currentAccount).markFaturedStickersByIdAsRead(((TLRPC.StickerSetCovered)localObject).set.id);
        }
        if (EmojiView.this.installingStickerSets.indexOfKey(((TLRPC.StickerSetCovered)localObject).set.id) < 0) {
          break label311;
        }
        paramInt = 1;
        if (EmojiView.this.removingStickerSets.indexOfKey(((TLRPC.StickerSetCovered)localObject).set.id) < 0) {
          break label316;
        }
        i = 1;
        if (paramInt == 0)
        {
          j = paramInt;
          k = i;
          if (i == 0) {}
        }
        else
        {
          if ((paramInt == 0) || (!paramViewHolder.isInstalled())) {
            break label321;
          }
          EmojiView.this.installingStickerSets.remove(((TLRPC.StickerSetCovered)localObject).set.id);
          j = 0;
          k = i;
        }
      }
      for (;;)
      {
        if (j == 0)
        {
          bool1 = bool2;
          if (k == 0) {}
        }
        else
        {
          bool1 = true;
        }
        paramViewHolder.setDrawProgress(bool1);
        return;
        bool1 = false;
        break;
        label311:
        paramInt = 0;
        break label209;
        label316:
        i = 0;
        break label232;
        label321:
        j = paramInt;
        k = i;
        if (i != 0)
        {
          j = paramInt;
          k = i;
          if (!paramViewHolder.isInstalled())
          {
            EmojiView.this.removingStickerSets.remove(((TLRPC.StickerSetCovered)localObject).set.id);
            k = 0;
            j = paramInt;
          }
        }
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new StickerEmojiCell(this.context)
        {
          public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0F), 1073741824));
          }
        };
        continue;
        paramViewGroup = new EmptyCell(this.context);
        continue;
        paramViewGroup = new FeaturedStickerSetInfoCell(this.context, 17);
        ((FeaturedStickerSetInfoCell)paramViewGroup).setAddOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = (FeaturedStickerSetInfoCell)paramAnonymousView.getParent();
            TLRPC.StickerSetCovered localStickerSetCovered = paramAnonymousView.getStickerSet();
            if ((EmojiView.this.installingStickerSets.indexOfKey(localStickerSetCovered.set.id) >= 0) || (EmojiView.this.removingStickerSets.indexOfKey(localStickerSetCovered.set.id) >= 0)) {
              return;
            }
            if (paramAnonymousView.isInstalled())
            {
              EmojiView.this.removingStickerSets.put(localStickerSetCovered.set.id, localStickerSetCovered);
              EmojiView.this.listener.onStickerSetRemove(paramAnonymousView.getStickerSet());
            }
            for (;;)
            {
              paramAnonymousView.setDrawProgress(true);
              return;
              EmojiView.this.installingStickerSets.put(localStickerSetCovered.set.id, localStickerSetCovered);
              EmojiView.this.listener.onStickerSetAdd(paramAnonymousView.getStickerSet());
            }
          }
        });
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/EmojiView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */