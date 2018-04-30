package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.GridLayoutManager;
import org.telegram.messenger.support.widget.GridLayoutManager.SpanSizeLookup;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPhoto;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_inputStickeredMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messages_getAttachedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_getStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_installStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSetInstallResultArchive;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.FeaturedStickerSetInfoCell;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.StickerPreviewViewer;

public class StickersAlert
  extends BottomSheet
  implements NotificationCenter.NotificationCenterDelegate
{
  private GridAdapter adapter;
  private StickersAlertDelegate delegate;
  private FrameLayout emptyView;
  private RecyclerListView gridView;
  private boolean ignoreLayout;
  private TLRPC.InputStickerSet inputStickerSet;
  private StickersAlertInstallDelegate installDelegate;
  private int itemSize;
  private GridLayoutManager layoutManager;
  private Activity parentActivity;
  private BaseFragment parentFragment;
  private PickerBottomLayout pickerBottomLayout;
  private ImageView previewFavButton;
  private TextView previewSendButton;
  private View previewSendButtonShadow;
  private int reqId;
  private int scrollOffsetY;
  private TLRPC.Document selectedSticker;
  private View[] shadow = new View[2];
  private AnimatorSet[] shadowAnimation = new AnimatorSet[2];
  private Drawable shadowDrawable;
  private boolean showEmoji;
  private TextView stickerEmojiTextView;
  private BackupImageView stickerImageView;
  private FrameLayout stickerPreviewLayout;
  private TLRPC.TL_messages_stickerSet stickerSet;
  private ArrayList<TLRPC.StickerSetCovered> stickerSetCovereds;
  private RecyclerListView.OnItemClickListener stickersOnItemClickListener;
  private TextView titleTextView;
  private Pattern urlPattern;
  
  public StickersAlert(Context paramContext, TLRPC.Photo paramPhoto)
  {
    super(paramContext, false);
    this.parentActivity = ((Activity)paramContext);
    final TLRPC.TL_messages_getAttachedStickers localTL_messages_getAttachedStickers = new TLRPC.TL_messages_getAttachedStickers();
    TLRPC.TL_inputStickeredMediaPhoto localTL_inputStickeredMediaPhoto = new TLRPC.TL_inputStickeredMediaPhoto();
    localTL_inputStickeredMediaPhoto.id = new TLRPC.TL_inputPhoto();
    localTL_inputStickeredMediaPhoto.id.id = paramPhoto.id;
    localTL_inputStickeredMediaPhoto.id.access_hash = paramPhoto.access_hash;
    localTL_messages_getAttachedStickers.media = localTL_inputStickeredMediaPhoto;
    this.reqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getAttachedStickers, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            StickersAlert.access$002(StickersAlert.this, 0);
            if (paramAnonymousTL_error == null)
            {
              Object localObject = (TLRPC.Vector)paramAnonymousTLObject;
              if (((TLRPC.Vector)localObject).objects.isEmpty())
              {
                StickersAlert.this.dismiss();
                return;
              }
              if (((TLRPC.Vector)localObject).objects.size() == 1)
              {
                localObject = (TLRPC.StickerSetCovered)((TLRPC.Vector)localObject).objects.get(0);
                StickersAlert.access$102(StickersAlert.this, new TLRPC.TL_inputStickerSetID());
                StickersAlert.this.inputStickerSet.id = ((TLRPC.StickerSetCovered)localObject).set.id;
                StickersAlert.this.inputStickerSet.access_hash = ((TLRPC.StickerSetCovered)localObject).set.access_hash;
                StickersAlert.this.loadStickerSet();
                return;
              }
              StickersAlert.access$302(StickersAlert.this, new ArrayList());
              int i = 0;
              while (i < ((TLRPC.Vector)localObject).objects.size())
              {
                StickersAlert.this.stickerSetCovereds.add((TLRPC.StickerSetCovered)((TLRPC.Vector)localObject).objects.get(i));
                i += 1;
              }
              StickersAlert.this.gridView.setLayoutParams(LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
              StickersAlert.this.titleTextView.setVisibility(8);
              StickersAlert.this.shadow[0].setVisibility(8);
              StickersAlert.this.adapter.notifyDataSetChanged();
              return;
            }
            AlertsCreator.processError(StickersAlert.this.currentAccount, paramAnonymousTL_error, StickersAlert.this.parentFragment, StickersAlert.1.this.val$req, new Object[0]);
            StickersAlert.this.dismiss();
          }
        });
      }
    });
    init(paramContext);
  }
  
  public StickersAlert(Context paramContext, BaseFragment paramBaseFragment, TLRPC.InputStickerSet paramInputStickerSet, TLRPC.TL_messages_stickerSet paramTL_messages_stickerSet, StickersAlertDelegate paramStickersAlertDelegate)
  {
    super(paramContext, false);
    this.delegate = paramStickersAlertDelegate;
    this.inputStickerSet = paramInputStickerSet;
    this.stickerSet = paramTL_messages_stickerSet;
    this.parentFragment = paramBaseFragment;
    loadStickerSet();
    init(paramContext);
  }
  
  private void hidePreview()
  {
    AnimatorSet localAnimatorSet = new AnimatorSet();
    localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.stickerPreviewLayout, "alpha", new float[] { 0.0F }) });
    localAnimatorSet.setDuration(200L);
    localAnimatorSet.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        StickersAlert.this.stickerPreviewLayout.setVisibility(8);
      }
    });
    localAnimatorSet.start();
  }
  
  private void init(Context paramContext)
  {
    this.shadowDrawable = paramContext.getResources().getDrawable(2131165640).mutate();
    this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogBackground"), PorterDuff.Mode.MULTIPLY));
    this.containerView = new FrameLayout(paramContext)
    {
      private int lastNotifyWidth;
      
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        StickersAlert.this.shadowDrawable.setBounds(0, StickersAlert.this.scrollOffsetY - StickersAlert.backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
        StickersAlert.this.shadowDrawable.draw(paramAnonymousCanvas);
      }
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if ((paramAnonymousMotionEvent.getAction() == 0) && (StickersAlert.this.scrollOffsetY != 0) && (paramAnonymousMotionEvent.getY() < StickersAlert.this.scrollOffsetY))
        {
          StickersAlert.this.dismiss();
          return true;
        }
        return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        if (this.lastNotifyWidth != paramAnonymousInt3 - paramAnonymousInt1)
        {
          this.lastNotifyWidth = (paramAnonymousInt3 - paramAnonymousInt1);
          if ((StickersAlert.this.adapter != null) && (StickersAlert.this.stickerSetCovereds != null)) {
            StickersAlert.this.adapter.notifyDataSetChanged();
          }
        }
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        StickersAlert.this.updateLayout();
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        paramAnonymousInt2 = View.MeasureSpec.getSize(paramAnonymousInt2);
        int j = paramAnonymousInt2;
        if (Build.VERSION.SDK_INT >= 21) {
          j = paramAnonymousInt2 - AndroidUtilities.statusBarHeight;
        }
        getMeasuredWidth();
        StickersAlert.access$1502(StickersAlert.this, (View.MeasureSpec.getSize(paramAnonymousInt1) - AndroidUtilities.dp(36.0F)) / 5);
        int k;
        if (StickersAlert.this.stickerSetCovereds != null)
        {
          k = AndroidUtilities.dp(56.0F) + AndroidUtilities.dp(60.0F) * StickersAlert.this.stickerSetCovereds.size() + StickersAlert.GridAdapter.access$1600(StickersAlert.this.adapter) * AndroidUtilities.dp(82.0F);
          if (k >= j / 5 * 3.2D) {
            break label322;
          }
        }
        label322:
        for (int i = 0;; i = j / 5 * 2)
        {
          paramAnonymousInt2 = i;
          if (i != 0)
          {
            paramAnonymousInt2 = i;
            if (k < j) {
              paramAnonymousInt2 = i - (j - k);
            }
          }
          i = paramAnonymousInt2;
          if (paramAnonymousInt2 == 0) {
            i = StickersAlert.backgroundPaddingTop;
          }
          paramAnonymousInt2 = i;
          if (StickersAlert.this.stickerSetCovereds != null) {
            paramAnonymousInt2 = i + AndroidUtilities.dp(8.0F);
          }
          if (StickersAlert.this.gridView.getPaddingTop() != paramAnonymousInt2)
          {
            StickersAlert.access$1902(StickersAlert.this, true);
            StickersAlert.this.gridView.setPadding(AndroidUtilities.dp(10.0F), paramAnonymousInt2, AndroidUtilities.dp(10.0F), 0);
            StickersAlert.this.emptyView.setPadding(0, paramAnonymousInt2, 0, 0);
            StickersAlert.access$1902(StickersAlert.this, false);
          }
          super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(Math.min(k, j), 1073741824));
          return;
          i = AndroidUtilities.dp(96.0F);
          if (StickersAlert.this.stickerSet != null) {}
          for (paramAnonymousInt2 = (int)Math.ceil(StickersAlert.this.stickerSet.documents.size() / 5.0F);; paramAnonymousInt2 = 0)
          {
            k = Math.max(3, paramAnonymousInt2) * AndroidUtilities.dp(82.0F) + i + StickersAlert.backgroundPaddingTop;
            break;
          }
        }
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (!StickersAlert.this.isDismissed()) && (super.onTouchEvent(paramAnonymousMotionEvent));
      }
      
      public void requestLayout()
      {
        if (StickersAlert.this.ignoreLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    this.containerView.setWillNotDraw(false);
    this.containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
    this.shadow[0] = new View(paramContext);
    this.shadow[0].setBackgroundResource(2131165342);
    this.shadow[0].setAlpha(0.0F);
    this.shadow[0].setVisibility(4);
    this.shadow[0].setTag(Integer.valueOf(1));
    this.containerView.addView(this.shadow[0], LayoutHelper.createFrame(-1, 3.0F, 51, 0.0F, 48.0F, 0.0F, 0.0F));
    this.gridView = new RecyclerListView(paramContext)
    {
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        boolean bool1 = false;
        boolean bool2 = StickerPreviewViewer.getInstance().onInterceptTouchEvent(paramAnonymousMotionEvent, StickersAlert.this.gridView, 0, null);
        if ((super.onInterceptTouchEvent(paramAnonymousMotionEvent)) || (bool2)) {
          bool1 = true;
        }
        return bool1;
      }
      
      public void requestLayout()
      {
        if (StickersAlert.this.ignoreLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    this.gridView.setTag(Integer.valueOf(14));
    Object localObject1 = this.gridView;
    Object localObject2 = new GridLayoutManager(getContext(), 5);
    this.layoutManager = ((GridLayoutManager)localObject2);
    ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
    this.layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
    {
      public int getSpanSize(int paramAnonymousInt)
      {
        if (((StickersAlert.this.stickerSetCovereds != null) && ((StickersAlert.GridAdapter.access$2400(StickersAlert.this.adapter).get(paramAnonymousInt) instanceof Integer))) || (paramAnonymousInt == StickersAlert.GridAdapter.access$2500(StickersAlert.this.adapter))) {
          return StickersAlert.GridAdapter.access$2600(StickersAlert.this.adapter);
        }
        return 1;
      }
    });
    localObject1 = this.gridView;
    localObject2 = new GridAdapter(paramContext);
    this.adapter = ((GridAdapter)localObject2);
    ((RecyclerListView)localObject1).setAdapter((RecyclerView.Adapter)localObject2);
    this.gridView.setVerticalScrollBarEnabled(false);
    this.gridView.addItemDecoration(new RecyclerView.ItemDecoration()
    {
      public void getItemOffsets(Rect paramAnonymousRect, View paramAnonymousView, RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState)
      {
        paramAnonymousRect.left = 0;
        paramAnonymousRect.right = 0;
        paramAnonymousRect.bottom = 0;
        paramAnonymousRect.top = 0;
      }
    });
    this.gridView.setPadding(AndroidUtilities.dp(10.0F), 0, AndroidUtilities.dp(10.0F), 0);
    this.gridView.setClipToPadding(false);
    this.gridView.setEnabled(true);
    this.gridView.setGlowColor(Theme.getColor("dialogScrollGlow"));
    this.gridView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return StickerPreviewViewer.getInstance().onTouch(paramAnonymousMotionEvent, StickersAlert.this.gridView, 0, StickersAlert.this.stickersOnItemClickListener, null);
      }
    });
    this.gridView.setOnScrollListener(new RecyclerView.OnScrollListener()
    {
      public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
      {
        StickersAlert.this.updateLayout();
      }
    });
    this.stickersOnItemClickListener = new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        Object localObject;
        if (StickersAlert.this.stickerSetCovereds != null)
        {
          paramAnonymousView = (TLRPC.StickerSetCovered)StickersAlert.GridAdapter.access$2800(StickersAlert.this.adapter).get(paramAnonymousInt);
          if (paramAnonymousView != null)
          {
            StickersAlert.this.dismiss();
            localObject = new TLRPC.TL_inputStickerSetID();
            ((TLRPC.TL_inputStickerSetID)localObject).access_hash = paramAnonymousView.set.access_hash;
            ((TLRPC.TL_inputStickerSetID)localObject).id = paramAnonymousView.set.id;
            new StickersAlert(StickersAlert.this.parentActivity, StickersAlert.this.parentFragment, (TLRPC.InputStickerSet)localObject, null, null).show();
          }
        }
        while ((StickersAlert.this.stickerSet == null) || (paramAnonymousInt < 0) || (paramAnonymousInt >= StickersAlert.this.stickerSet.documents.size())) {
          return;
        }
        StickersAlert.access$3002(StickersAlert.this, (TLRPC.Document)StickersAlert.this.stickerSet.documents.get(paramAnonymousInt));
        int j = 0;
        paramAnonymousInt = 0;
        int i = j;
        if (paramAnonymousInt < StickersAlert.this.selectedSticker.attributes.size())
        {
          paramAnonymousView = (TLRPC.DocumentAttribute)StickersAlert.this.selectedSticker.attributes.get(paramAnonymousInt);
          if (!(paramAnonymousView instanceof TLRPC.TL_documentAttributeSticker)) {
            break label610;
          }
          i = j;
          if (paramAnonymousView.alt != null)
          {
            i = j;
            if (paramAnonymousView.alt.length() > 0)
            {
              StickersAlert.this.stickerEmojiTextView.setText(Emoji.replaceEmoji(paramAnonymousView.alt, StickersAlert.this.stickerEmojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(30.0F), false));
              i = 1;
            }
          }
        }
        if (i == 0) {
          StickersAlert.this.stickerEmojiTextView.setText(Emoji.replaceEmoji(DataQuery.getInstance(StickersAlert.this.currentAccount).getEmojiForSticker(StickersAlert.this.selectedSticker.id), StickersAlert.this.stickerEmojiTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(30.0F), false));
        }
        boolean bool = DataQuery.getInstance(StickersAlert.this.currentAccount).isStickerInFavorites(StickersAlert.this.selectedSticker);
        paramAnonymousView = StickersAlert.this.previewFavButton;
        label365:
        label389:
        label441:
        TLRPC.Document localDocument;
        if (bool)
        {
          paramAnonymousInt = 2131165664;
          paramAnonymousView.setImageResource(paramAnonymousInt);
          localObject = StickersAlert.this.previewFavButton;
          if (!bool) {
            break label624;
          }
          paramAnonymousView = Integer.valueOf(1);
          ((ImageView)localObject).setTag(paramAnonymousView);
          if (StickersAlert.this.previewFavButton.getVisibility() != 8)
          {
            paramAnonymousView = StickersAlert.this.previewFavButton;
            if ((!bool) && (!DataQuery.getInstance(StickersAlert.this.currentAccount).canAddStickerToFavorites())) {
              break label629;
            }
            paramAnonymousInt = 0;
            paramAnonymousView.setVisibility(paramAnonymousInt);
          }
          localObject = StickersAlert.this.stickerImageView.getImageReceiver();
          localDocument = StickersAlert.this.selectedSticker;
          if (StickersAlert.this.selectedSticker.thumb == null) {
            break label634;
          }
        }
        label610:
        label624:
        label629:
        label634:
        for (paramAnonymousView = StickersAlert.this.selectedSticker.thumb.location;; paramAnonymousView = null)
        {
          ((ImageReceiver)localObject).setImage(localDocument, null, paramAnonymousView, null, "webp", 1);
          paramAnonymousView = (FrameLayout.LayoutParams)StickersAlert.this.stickerPreviewLayout.getLayoutParams();
          paramAnonymousView.topMargin = StickersAlert.this.scrollOffsetY;
          StickersAlert.this.stickerPreviewLayout.setLayoutParams(paramAnonymousView);
          StickersAlert.this.stickerPreviewLayout.setVisibility(0);
          paramAnonymousView = new AnimatorSet();
          paramAnonymousView.playTogether(new Animator[] { ObjectAnimator.ofFloat(StickersAlert.this.stickerPreviewLayout, "alpha", new float[] { 0.0F, 1.0F }) });
          paramAnonymousView.setDuration(200L);
          paramAnonymousView.start();
          return;
          paramAnonymousInt += 1;
          break;
          paramAnonymousInt = 2131165660;
          break label365;
          paramAnonymousView = null;
          break label389;
          paramAnonymousInt = 4;
          break label441;
        }
      }
    };
    this.gridView.setOnItemClickListener(this.stickersOnItemClickListener);
    this.containerView.addView(this.gridView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 48.0F, 0.0F, 48.0F));
    this.emptyView = new FrameLayout(paramContext)
    {
      public void requestLayout()
      {
        if (StickersAlert.this.ignoreLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    this.containerView.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
    this.gridView.setEmptyView(this.emptyView);
    this.emptyView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.titleTextView = new TextView(paramContext);
    this.titleTextView.setLines(1);
    this.titleTextView.setSingleLine(true);
    this.titleTextView.setTextColor(Theme.getColor("dialogTextBlack"));
    this.titleTextView.setTextSize(1, 20.0F);
    this.titleTextView.setLinkTextColor(Theme.getColor("dialogTextLink"));
    this.titleTextView.setHighlightColor(Theme.getColor("dialogLinkSelection"));
    this.titleTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
    this.titleTextView.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
    this.titleTextView.setGravity(16);
    this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.titleTextView.setMovementMethod(new LinkMovementMethodMy(null));
    this.containerView.addView(this.titleTextView, LayoutHelper.createLinear(-1, 48));
    localObject1 = new RadialProgressView(paramContext);
    this.emptyView.addView((View)localObject1, LayoutHelper.createFrame(-2, -2, 17));
    this.shadow[1] = new View(paramContext);
    this.shadow[1].setBackgroundResource(2131165343);
    this.containerView.addView(this.shadow[1], LayoutHelper.createFrame(-1, 3.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
    this.pickerBottomLayout = new PickerBottomLayout(paramContext, false);
    this.pickerBottomLayout.setBackgroundColor(Theme.getColor("dialogBackground"));
    this.containerView.addView(this.pickerBottomLayout, LayoutHelper.createFrame(-1, 48, 83));
    this.pickerBottomLayout.cancelButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
    this.pickerBottomLayout.cancelButton.setTextColor(Theme.getColor("dialogTextBlue2"));
    this.pickerBottomLayout.cancelButton.setText(LocaleController.getString("Close", 2131493265).toUpperCase());
    this.pickerBottomLayout.cancelButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        StickersAlert.this.dismiss();
      }
    });
    this.pickerBottomLayout.doneButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
    this.pickerBottomLayout.doneButtonBadgeTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(12.5F), Theme.getColor("dialogBadgeBackground")));
    this.stickerPreviewLayout = new FrameLayout(paramContext);
    this.stickerPreviewLayout.setBackgroundColor(Theme.getColor("dialogBackground") & 0xDFFFFFFF);
    this.stickerPreviewLayout.setVisibility(8);
    this.stickerPreviewLayout.setSoundEffectsEnabled(false);
    this.containerView.addView(this.stickerPreviewLayout, LayoutHelper.createFrame(-1, -1.0F));
    this.stickerPreviewLayout.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        StickersAlert.this.hidePreview();
      }
    });
    localObject1 = new ImageView(paramContext);
    ((ImageView)localObject1).setImageResource(2131165519);
    ((ImageView)localObject1).setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogTextGray3"), PorterDuff.Mode.MULTIPLY));
    ((ImageView)localObject1).setScaleType(ImageView.ScaleType.CENTER);
    this.stickerPreviewLayout.addView((View)localObject1, LayoutHelper.createFrame(48, 48, 53));
    ((ImageView)localObject1).setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        StickersAlert.this.hidePreview();
      }
    });
    this.stickerImageView = new BackupImageView(paramContext);
    this.stickerImageView.setAspectFit(true);
    this.stickerPreviewLayout.addView(this.stickerImageView);
    this.stickerEmojiTextView = new TextView(paramContext);
    this.stickerEmojiTextView.setTextSize(1, 30.0F);
    this.stickerEmojiTextView.setGravity(85);
    this.stickerPreviewLayout.addView(this.stickerEmojiTextView);
    this.previewSendButton = new TextView(paramContext);
    this.previewSendButton.setTextSize(1, 14.0F);
    this.previewSendButton.setTextColor(Theme.getColor("dialogTextBlue2"));
    this.previewSendButton.setGravity(17);
    this.previewSendButton.setBackgroundColor(Theme.getColor("dialogBackground"));
    this.previewSendButton.setPadding(AndroidUtilities.dp(29.0F), 0, AndroidUtilities.dp(29.0F), 0);
    this.previewSendButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.stickerPreviewLayout.addView(this.previewSendButton, LayoutHelper.createFrame(-1, 48, 83));
    this.previewSendButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        StickersAlert.this.delegate.onStickerSelected(StickersAlert.this.selectedSticker);
        StickersAlert.this.dismiss();
      }
    });
    this.previewFavButton = new ImageView(paramContext);
    this.previewFavButton.setScaleType(ImageView.ScaleType.CENTER);
    this.stickerPreviewLayout.addView(this.previewFavButton, LayoutHelper.createFrame(48, 48.0F, 85, 0.0F, 0.0F, 4.0F, 0.0F));
    this.previewFavButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogIcon"), PorterDuff.Mode.MULTIPLY));
    this.previewFavButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        paramAnonymousView = DataQuery.getInstance(StickersAlert.this.currentAccount);
        TLRPC.Document localDocument = StickersAlert.this.selectedSticker;
        int i = (int)(System.currentTimeMillis() / 1000L);
        if (StickersAlert.this.previewFavButton.getTag() != null) {}
        for (boolean bool = true;; bool = false)
        {
          paramAnonymousView.addRecentSticker(2, localDocument, i, bool);
          if (StickersAlert.this.previewFavButton.getTag() != null) {
            break;
          }
          StickersAlert.this.previewFavButton.setTag(Integer.valueOf(1));
          StickersAlert.this.previewFavButton.setImageResource(2131165664);
          return;
        }
        StickersAlert.this.previewFavButton.setTag(null);
        StickersAlert.this.previewFavButton.setImageResource(2131165660);
      }
    });
    this.previewSendButtonShadow = new View(paramContext);
    this.previewSendButtonShadow.setBackgroundResource(2131165343);
    this.stickerPreviewLayout.addView(this.previewSendButtonShadow, LayoutHelper.createFrame(-1, 3.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    updateFields();
    updateSendButton();
    this.adapter.notifyDataSetChanged();
  }
  
  private void loadStickerSet()
  {
    if (this.inputStickerSet != null)
    {
      if ((this.stickerSet == null) && (this.inputStickerSet.short_name != null)) {
        this.stickerSet = DataQuery.getInstance(this.currentAccount).getStickerSetByName(this.inputStickerSet.short_name);
      }
      if (this.stickerSet == null) {
        this.stickerSet = DataQuery.getInstance(this.currentAccount).getStickerSetById(this.inputStickerSet.id);
      }
      if (this.stickerSet == null)
      {
        TLRPC.TL_messages_getStickerSet localTL_messages_getStickerSet = new TLRPC.TL_messages_getStickerSet();
        localTL_messages_getStickerSet.stickerset = this.inputStickerSet;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getStickerSet, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                StickersAlert.access$002(StickersAlert.this, 0);
                if (paramAnonymousTL_error == null)
                {
                  StickersAlert.access$1002(StickersAlert.this, (TLRPC.TL_messages_stickerSet)paramAnonymousTLObject);
                  StickersAlert localStickersAlert = StickersAlert.this;
                  if (!StickersAlert.this.stickerSet.set.masks) {}
                  for (boolean bool = true;; bool = false)
                  {
                    StickersAlert.access$1102(localStickersAlert, bool);
                    StickersAlert.this.updateSendButton();
                    StickersAlert.this.updateFields();
                    StickersAlert.this.adapter.notifyDataSetChanged();
                    return;
                  }
                }
                Toast.makeText(StickersAlert.this.getContext(), LocaleController.getString("AddStickersNotFound", 2131492938), 0).show();
                StickersAlert.this.dismiss();
              }
            });
          }
        });
      }
    }
    else if (this.stickerSet != null)
    {
      if (this.stickerSet.set.masks) {
        break label169;
      }
    }
    label169:
    for (boolean bool = true;; bool = false)
    {
      this.showEmoji = bool;
      return;
      if (this.adapter == null) {
        break;
      }
      updateSendButton();
      updateFields();
      this.adapter.notifyDataSetChanged();
      break;
    }
  }
  
  private void runShadowAnimation(final int paramInt, final boolean paramBoolean)
  {
    if (this.stickerSetCovereds != null) {}
    while (((!paramBoolean) || (this.shadow[paramInt].getTag() == null)) && ((paramBoolean) || (this.shadow[paramInt].getTag() != null))) {
      return;
    }
    View localView = this.shadow[paramInt];
    Object localObject;
    if (paramBoolean)
    {
      localObject = null;
      localView.setTag(localObject);
      if (paramBoolean) {
        this.shadow[paramInt].setVisibility(0);
      }
      if (this.shadowAnimation[paramInt] != null) {
        this.shadowAnimation[paramInt].cancel();
      }
      this.shadowAnimation[paramInt] = new AnimatorSet();
      localObject = this.shadowAnimation[paramInt];
      localView = this.shadow[paramInt];
      if (!paramBoolean) {
        break label207;
      }
    }
    label207:
    for (float f = 1.0F;; f = 0.0F)
    {
      ((AnimatorSet)localObject).playTogether(new Animator[] { ObjectAnimator.ofFloat(localView, "alpha", new float[] { f }) });
      this.shadowAnimation[paramInt].setDuration(150L);
      this.shadowAnimation[paramInt].addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((StickersAlert.this.shadowAnimation[paramInt] != null) && (StickersAlert.this.shadowAnimation[paramInt].equals(paramAnonymousAnimator))) {
            StickersAlert.this.shadowAnimation[paramInt] = null;
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((StickersAlert.this.shadowAnimation[paramInt] != null) && (StickersAlert.this.shadowAnimation[paramInt].equals(paramAnonymousAnimator)))
          {
            if (!paramBoolean) {
              StickersAlert.this.shadow[paramInt].setVisibility(4);
            }
            StickersAlert.this.shadowAnimation[paramInt] = null;
          }
        }
      });
      this.shadowAnimation[paramInt].start();
      return;
      localObject = Integer.valueOf(1);
      break;
    }
  }
  
  private void setRightButton(View.OnClickListener paramOnClickListener, String paramString, int paramInt, boolean paramBoolean)
  {
    if (paramString == null)
    {
      this.pickerBottomLayout.doneButton.setVisibility(8);
      return;
    }
    this.pickerBottomLayout.doneButton.setVisibility(0);
    if (paramBoolean)
    {
      this.pickerBottomLayout.doneButtonBadgeTextView.setVisibility(0);
      this.pickerBottomLayout.doneButtonBadgeTextView.setText(String.format("%d", new Object[] { Integer.valueOf(this.stickerSet.documents.size()) }));
    }
    for (;;)
    {
      this.pickerBottomLayout.doneButtonTextView.setTextColor(paramInt);
      this.pickerBottomLayout.doneButtonTextView.setText(paramString.toUpperCase());
      this.pickerBottomLayout.doneButton.setOnClickListener(paramOnClickListener);
      return;
      this.pickerBottomLayout.doneButtonBadgeTextView.setVisibility(8);
    }
  }
  
  private void updateFields()
  {
    if (this.titleTextView == null) {
      return;
    }
    if (this.stickerSet != null)
    {
      Object localObject1 = null;
      localObject3 = localObject1;
      for (;;)
      {
        Matcher localMatcher;
        try
        {
          if (this.urlPattern == null)
          {
            localObject3 = localObject1;
            this.urlPattern = Pattern.compile("@[a-zA-Z\\d_]{1,32}");
          }
          localObject3 = localObject1;
          localMatcher = this.urlPattern.matcher(this.stickerSet.set.title);
          localObject1 = null;
        }
        catch (Exception localException1) {}
        try
        {
          if (!localMatcher.find()) {
            continue;
          }
          if (localObject1 != null) {
            break label472;
          }
          localObject3 = new SpannableStringBuilder(this.stickerSet.set.title);
          localObject1 = localObject3;
        }
        catch (Exception localException2)
        {
          for (;;)
          {
            localObject3 = localObject2;
            Object localObject2 = localException2;
          }
          continue;
        }
        localObject3 = localObject1;
        int j = localMatcher.start();
        localObject3 = localObject1;
        int k = localMatcher.end();
        int i = j;
        localObject3 = localObject1;
        if (this.stickerSet.set.title.charAt(j) != '@') {
          i = j + 1;
        }
        localObject3 = localObject1;
        URLSpanNoUnderline local17 = new URLSpanNoUnderline(this.stickerSet.set.title.subSequence(i + 1, k).toString())
        {
          public void onClick(View paramAnonymousView)
          {
            MessagesController.getInstance(StickersAlert.this.currentAccount).openByUserName(getURL(), StickersAlert.this.parentFragment, 1);
            StickersAlert.this.dismiss();
          }
        };
        if (local17 != null)
        {
          localObject3 = localObject1;
          ((SpannableStringBuilder)localObject1).setSpan(local17, i, k, 0);
        }
      }
      localObject3 = localObject1;
      localObject1 = this.titleTextView;
      if (localObject3 != null)
      {
        label227:
        ((TextView)localObject1).setText((CharSequence)localObject3);
        if ((this.stickerSet.set != null) && (DataQuery.getInstance(this.currentAccount).isStickerPackInstalled(this.stickerSet.set.id))) {
          break label370;
        }
        localObject3 = new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            StickersAlert.this.dismiss();
            if (StickersAlert.this.installDelegate != null) {
              StickersAlert.this.installDelegate.onStickerSetInstalled();
            }
            paramAnonymousView = new TLRPC.TL_messages_installStickerSet();
            paramAnonymousView.stickerset = StickersAlert.this.inputStickerSet;
            ConnectionsManager.getInstance(StickersAlert.this.currentAccount).sendRequest(paramAnonymousView, new RequestDelegate()
            {
              public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    for (;;)
                    {
                      try
                      {
                        if (paramAnonymous2TL_error != null) {
                          continue;
                        }
                        if (!StickersAlert.this.stickerSet.set.masks) {
                          continue;
                        }
                        Toast.makeText(StickersAlert.this.getContext(), LocaleController.getString("AddMasksInstalled", 2131492931), 0).show();
                        if ((paramAnonymous2TLObject instanceof TLRPC.TL_messages_stickerSetInstallResultArchive))
                        {
                          NotificationCenter.getInstance(StickersAlert.this.currentAccount).postNotificationName(NotificationCenter.needReloadArchivedStickers, new Object[0]);
                          if ((StickersAlert.this.parentFragment != null) && (StickersAlert.this.parentFragment.getParentActivity() != null))
                          {
                            localObject = new StickersArchiveAlert(StickersAlert.this.parentFragment.getParentActivity(), StickersAlert.this.parentFragment, ((TLRPC.TL_messages_stickerSetInstallResultArchive)paramAnonymous2TLObject).sets);
                            StickersAlert.this.parentFragment.showDialog(((StickersArchiveAlert)localObject).create());
                          }
                        }
                      }
                      catch (Exception localException)
                      {
                        Object localObject;
                        FileLog.e(localException);
                        continue;
                        Toast.makeText(StickersAlert.this.getContext(), LocaleController.getString("ErrorOccurred", 2131493453), 0).show();
                        continue;
                        int i = 0;
                        continue;
                      }
                      localObject = DataQuery.getInstance(StickersAlert.this.currentAccount);
                      if (!StickersAlert.this.stickerSet.set.masks) {
                        continue;
                      }
                      i = 1;
                      ((DataQuery)localObject).loadStickers(i, false, true);
                      return;
                      Toast.makeText(StickersAlert.this.getContext(), LocaleController.getString("AddStickersInstalled", 2131492937), 0).show();
                    }
                  }
                });
              }
            });
          }
        };
        if ((this.stickerSet == null) || (!this.stickerSet.set.masks)) {
          break label356;
        }
        localObject1 = LocaleController.getString("AddMasks", 2131492930);
        label308:
        setRightButton((View.OnClickListener)localObject3, (String)localObject1, Theme.getColor("dialogTextBlue2"), true);
      }
      for (;;)
      {
        this.adapter.notifyDataSetChanged();
        return;
        FileLog.e(localException1);
        break;
        localObject3 = this.stickerSet.set.title;
        break label227;
        label356:
        localObject2 = LocaleController.getString("AddStickers", 2131492936);
        break label308;
        label370:
        if (this.stickerSet.set.official) {
          setRightButton(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              if (StickersAlert.this.installDelegate != null) {
                StickersAlert.this.installDelegate.onStickerSetUninstalled();
              }
              StickersAlert.this.dismiss();
              DataQuery.getInstance(StickersAlert.this.currentAccount).removeStickersSet(StickersAlert.this.getContext(), StickersAlert.this.stickerSet.set, 1, StickersAlert.this.parentFragment, true);
            }
          }, LocaleController.getString("StickersRemove", 2131494425), Theme.getColor("dialogTextRed"), false);
        } else {
          setRightButton(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              if (StickersAlert.this.installDelegate != null) {
                StickersAlert.this.installDelegate.onStickerSetUninstalled();
              }
              StickersAlert.this.dismiss();
              DataQuery.getInstance(StickersAlert.this.currentAccount).removeStickersSet(StickersAlert.this.getContext(), StickersAlert.this.stickerSet.set, 0, StickersAlert.this.parentFragment, true);
            }
          }, LocaleController.getString("StickersRemove", 2131494427), Theme.getColor("dialogTextRed"), false);
        }
      }
    }
    setRightButton(null, null, Theme.getColor("dialogTextRed"), false);
  }
  
  @SuppressLint({"NewApi"})
  private void updateLayout()
  {
    Object localObject;
    int i;
    if (this.gridView.getChildCount() <= 0)
    {
      localObject = this.gridView;
      i = this.gridView.getPaddingTop();
      this.scrollOffsetY = i;
      ((RecyclerListView)localObject).setTopGlowOffset(i);
      if (this.stickerSetCovereds == null)
      {
        this.titleTextView.setTranslationY(this.scrollOffsetY);
        this.shadow[0].setTranslationY(this.scrollOffsetY);
      }
      this.containerView.invalidate();
    }
    for (;;)
    {
      return;
      localObject = this.gridView.getChildAt(0);
      RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.gridView.findContainingViewHolder((View)localObject);
      i = ((View)localObject).getTop();
      int j = 0;
      if ((i >= 0) && (localHolder != null) && (localHolder.getAdapterPosition() == 0)) {
        runShadowAnimation(0, false);
      }
      while (this.scrollOffsetY != i)
      {
        localObject = this.gridView;
        this.scrollOffsetY = i;
        ((RecyclerListView)localObject).setTopGlowOffset(i);
        if (this.stickerSetCovereds == null)
        {
          this.titleTextView.setTranslationY(this.scrollOffsetY);
          this.shadow[0].setTranslationY(this.scrollOffsetY);
        }
        this.containerView.invalidate();
        return;
        runShadowAnimation(0, true);
        i = j;
      }
    }
  }
  
  private void updateSendButton()
  {
    int i = (int)(Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / 2 / AndroidUtilities.density);
    if ((this.delegate != null) && ((this.stickerSet == null) || (!this.stickerSet.set.masks)))
    {
      this.previewSendButton.setText(LocaleController.getString("SendSticker", 2131494352).toUpperCase());
      this.stickerImageView.setLayoutParams(LayoutHelper.createFrame(i, i, 17, 0.0F, 0.0F, 0.0F, 30.0F));
      this.stickerEmojiTextView.setLayoutParams(LayoutHelper.createFrame(i, i, 17, 0.0F, 0.0F, 0.0F, 30.0F));
      this.previewSendButton.setVisibility(0);
      this.previewFavButton.setVisibility(0);
      this.previewSendButtonShadow.setVisibility(0);
      return;
    }
    this.previewSendButton.setText(LocaleController.getString("Close", 2131493265).toUpperCase());
    this.stickerImageView.setLayoutParams(LayoutHelper.createFrame(i, i, 17));
    this.stickerEmojiTextView.setLayoutParams(LayoutHelper.createFrame(i, i, 17));
    this.previewSendButton.setVisibility(8);
    this.previewFavButton.setVisibility(8);
    this.previewSendButtonShadow.setVisibility(8);
  }
  
  protected boolean canDismissWithSwipe()
  {
    return false;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.emojiDidLoaded)
    {
      if (this.gridView != null)
      {
        paramInt2 = this.gridView.getChildCount();
        paramInt1 = 0;
        while (paramInt1 < paramInt2)
        {
          this.gridView.getChildAt(paramInt1).invalidate();
          paramInt1 += 1;
        }
      }
      if (StickerPreviewViewer.getInstance().isVisible()) {
        StickerPreviewViewer.getInstance().close();
      }
      StickerPreviewViewer.getInstance().reset();
    }
  }
  
  public void dismiss()
  {
    super.dismiss();
    if (this.reqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
      this.reqId = 0;
    }
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
  }
  
  public void setInstallDelegate(StickersAlertInstallDelegate paramStickersAlertInstallDelegate)
  {
    this.installDelegate = paramStickersAlertInstallDelegate;
  }
  
  private class GridAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private SparseArray<Object> cache = new SparseArray();
    private Context context;
    private SparseArray<TLRPC.StickerSetCovered> positionsToSets = new SparseArray();
    private int stickersPerRow;
    private int stickersRowCount;
    private int totalItems;
    
    public GridAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public int getItemCount()
    {
      return this.totalItems;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (StickersAlert.this.stickerSetCovereds != null)
      {
        Object localObject = this.cache.get(paramInt);
        if (localObject == null) {
          break label34;
        }
        if (!(localObject instanceof TLRPC.Document)) {}
      }
      else
      {
        return 0;
      }
      return 2;
      label34:
      return 1;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return false;
    }
    
    public void notifyDataSetChanged()
    {
      int i = 0;
      if (StickersAlert.this.stickerSetCovereds != null)
      {
        int j = StickersAlert.this.gridView.getMeasuredWidth();
        i = j;
        if (j == 0) {
          i = AndroidUtilities.displaySize.x;
        }
        this.stickersPerRow = (i / AndroidUtilities.dp(72.0F));
        StickersAlert.this.layoutManager.setSpanCount(this.stickersPerRow);
        this.cache.clear();
        this.positionsToSets.clear();
        this.totalItems = 0;
        this.stickersRowCount = 0;
        i = 0;
        if (i < StickersAlert.this.stickerSetCovereds.size())
        {
          TLRPC.StickerSetCovered localStickerSetCovered = (TLRPC.StickerSetCovered)StickersAlert.this.stickerSetCovereds.get(i);
          if ((localStickerSetCovered.covers.isEmpty()) && (localStickerSetCovered.cover == null)) {}
          for (;;)
          {
            i += 1;
            break;
            this.stickersRowCount = ((int)(this.stickersRowCount + Math.ceil(StickersAlert.this.stickerSetCovereds.size() / this.stickersPerRow)));
            this.positionsToSets.put(this.totalItems, localStickerSetCovered);
            SparseArray localSparseArray = this.cache;
            j = this.totalItems;
            this.totalItems = (j + 1);
            localSparseArray.put(j, Integer.valueOf(i));
            j = this.totalItems / this.stickersPerRow;
            if (!localStickerSetCovered.covers.isEmpty())
            {
              int m = (int)Math.ceil(localStickerSetCovered.covers.size() / this.stickersPerRow);
              k = 0;
              for (;;)
              {
                j = m;
                if (k >= localStickerSetCovered.covers.size()) {
                  break;
                }
                this.cache.put(this.totalItems + k, localStickerSetCovered.covers.get(k));
                k += 1;
              }
            }
            j = 1;
            this.cache.put(this.totalItems, localStickerSetCovered.cover);
            int k = 0;
            while (k < this.stickersPerRow * j)
            {
              this.positionsToSets.put(this.totalItems + k, localStickerSetCovered);
              k += 1;
            }
            this.totalItems += this.stickersPerRow * j;
          }
        }
      }
      else
      {
        if (StickersAlert.this.stickerSet != null) {
          i = StickersAlert.this.stickerSet.documents.size();
        }
        this.totalItems = i;
      }
      super.notifyDataSetChanged();
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      if (StickersAlert.this.stickerSetCovereds != null)
      {
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
        Object localObject = (TLRPC.StickerSetCovered)StickersAlert.this.stickerSetCovereds.get(((Integer)this.cache.get(paramInt)).intValue());
        ((FeaturedStickerSetInfoCell)paramViewHolder.itemView).setStickerSet((TLRPC.StickerSetCovered)localObject, false);
        return;
      }
      ((StickerEmojiCell)paramViewHolder.itemView).setSticker((TLRPC.Document)StickersAlert.this.stickerSet.documents.get(paramInt), StickersAlert.this.showEmoji);
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
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(StickersAlert.this.itemSize, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82.0F), 1073741824));
          }
        };
        continue;
        paramViewGroup = new EmptyCell(this.context);
        continue;
        paramViewGroup = new FeaturedStickerSetInfoCell(this.context, 8);
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
  
  public static abstract interface StickersAlertDelegate
  {
    public abstract void onStickerSelected(TLRPC.Document paramDocument);
  }
  
  public static abstract interface StickersAlertInstallDelegate
  {
    public abstract void onStickerSetInstalled();
    
    public abstract void onStickerSetUninstalled();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/StickersAlert.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */