package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.TL_channels_setStickers;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_messages_getStickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.StickerSetCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class GroupStickersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private int chatId;
  private ActionBarMenuItem doneItem;
  private AnimatorSet doneItemAnimation;
  private boolean donePressed;
  private EditText editText;
  private ImageView eraseImageView;
  private int headerRow;
  private boolean ignoreTextChanges;
  private TLRPC.ChatFull info;
  private int infoRow;
  private LinearLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private LinearLayout nameContainer;
  private int nameRow;
  private ContextProgressView progressView;
  private Runnable queryRunnable;
  private int reqId;
  private int rowCount;
  private boolean searchWas;
  private boolean searching;
  private int selectedStickerRow;
  private TLRPC.TL_messages_stickerSet selectedStickerSet;
  private int stickersEndRow;
  private int stickersShadowRow;
  private int stickersStartRow;
  private EditTextBoldCursor usernameTextView;
  
  public GroupStickersActivity(int paramInt)
  {
    this.chatId = paramInt;
  }
  
  private void resolveStickerSet()
  {
    if (this.listAdapter == null) {}
    do
    {
      return;
      if (this.reqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.reqId, true);
        this.reqId = 0;
      }
      if (this.queryRunnable != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.queryRunnable);
        this.queryRunnable = null;
      }
      this.selectedStickerSet = null;
      if (this.usernameTextView.length() > 0) {
        break;
      }
      this.searching = false;
      this.searchWas = false;
    } while (this.selectedStickerRow == -1);
    updateRows();
    return;
    this.searching = true;
    this.searchWas = true;
    final Object localObject = this.usernameTextView.getText().toString();
    TLRPC.TL_messages_stickerSet localTL_messages_stickerSet = DataQuery.getInstance(this.currentAccount).getStickerSetByName((String)localObject);
    if (localTL_messages_stickerSet != null) {
      this.selectedStickerSet = localTL_messages_stickerSet;
    }
    if (this.selectedStickerRow == -1) {
      updateRows();
    }
    while (localTL_messages_stickerSet != null)
    {
      this.searching = false;
      return;
      this.listAdapter.notifyItemChanged(this.selectedStickerRow);
    }
    localObject = new Runnable()
    {
      public void run()
      {
        if (GroupStickersActivity.this.queryRunnable == null) {
          return;
        }
        TLRPC.TL_messages_getStickerSet localTL_messages_getStickerSet = new TLRPC.TL_messages_getStickerSet();
        localTL_messages_getStickerSet.stickerset = new TLRPC.TL_inputStickerSetShortName();
        localTL_messages_getStickerSet.stickerset.short_name = localObject;
        GroupStickersActivity.access$1802(GroupStickersActivity.this, ConnectionsManager.getInstance(GroupStickersActivity.this.currentAccount).sendRequest(localTL_messages_getStickerSet, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                GroupStickersActivity.access$102(GroupStickersActivity.this, false);
                if ((paramAnonymous2TLObject instanceof TLRPC.TL_messages_stickerSet))
                {
                  GroupStickersActivity.access$402(GroupStickersActivity.this, (TLRPC.TL_messages_stickerSet)paramAnonymous2TLObject);
                  if (GroupStickersActivity.this.donePressed) {
                    GroupStickersActivity.this.saveStickerSet();
                  }
                }
                for (;;)
                {
                  GroupStickersActivity.access$1802(GroupStickersActivity.this, 0);
                  return;
                  if (GroupStickersActivity.this.selectedStickerRow != -1)
                  {
                    GroupStickersActivity.this.listAdapter.notifyItemChanged(GroupStickersActivity.this.selectedStickerRow);
                  }
                  else
                  {
                    GroupStickersActivity.this.updateRows();
                    continue;
                    if (GroupStickersActivity.this.selectedStickerRow != -1) {
                      GroupStickersActivity.this.listAdapter.notifyItemChanged(GroupStickersActivity.this.selectedStickerRow);
                    }
                    if (GroupStickersActivity.this.donePressed)
                    {
                      GroupStickersActivity.access$002(GroupStickersActivity.this, false);
                      GroupStickersActivity.this.showEditDoneProgress(false);
                      if (GroupStickersActivity.this.getParentActivity() != null) {
                        Toast.makeText(GroupStickersActivity.this.getParentActivity(), LocaleController.getString("AddStickersNotFound", 2131492938), 0).show();
                      }
                    }
                  }
                }
              }
            });
          }
        }));
      }
    };
    this.queryRunnable = ((Runnable)localObject);
    AndroidUtilities.runOnUIThread((Runnable)localObject, 500L);
  }
  
  private void saveStickerSet()
  {
    if ((this.info == null) || ((this.info.stickerset != null) && (this.selectedStickerSet != null) && (this.selectedStickerSet.set.id == this.info.stickerset.id)) || ((this.info.stickerset == null) && (this.selectedStickerSet == null)))
    {
      finishFragment();
      return;
    }
    showEditDoneProgress(true);
    TLRPC.TL_channels_setStickers localTL_channels_setStickers = new TLRPC.TL_channels_setStickers();
    localTL_channels_setStickers.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
    if (this.selectedStickerSet == null) {
      localTL_channels_setStickers.stickerset = new TLRPC.TL_inputStickerSetEmpty();
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_setStickers, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.ChatFull localChatFull;
                if (GroupStickersActivity.this.selectedStickerSet == null)
                {
                  GroupStickersActivity.this.info.stickerset = null;
                  if (GroupStickersActivity.this.info.stickerset != null) {
                    break label220;
                  }
                  localChatFull = GroupStickersActivity.this.info;
                }
                label220:
                for (localChatFull.flags |= 0x100;; GroupStickersActivity.this.info.flags &= 0xFEFF)
                {
                  MessagesStorage.getInstance(GroupStickersActivity.this.currentAccount).updateChatInfo(GroupStickersActivity.this.info, false);
                  NotificationCenter.getInstance(GroupStickersActivity.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { GroupStickersActivity.this.info, Integer.valueOf(0), Boolean.valueOf(true), null });
                  GroupStickersActivity.this.finishFragment();
                  return;
                  GroupStickersActivity.this.info.stickerset = GroupStickersActivity.this.selectedStickerSet.set;
                  DataQuery.getInstance(GroupStickersActivity.this.currentAccount).putGroupStickerSet(GroupStickersActivity.this.selectedStickerSet);
                  break;
                }
              }
              Toast.makeText(GroupStickersActivity.this.getParentActivity(), LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramAnonymousTL_error.text, 0).show();
              GroupStickersActivity.access$002(GroupStickersActivity.this, false);
              GroupStickersActivity.this.showEditDoneProgress(false);
            }
          });
        }
      });
      return;
      MessagesController.getEmojiSettings(this.currentAccount).edit().remove("group_hide_stickers_" + this.info.id).commit();
      localTL_channels_setStickers.stickerset = new TLRPC.TL_inputStickerSetID();
      localTL_channels_setStickers.stickerset.id = this.selectedStickerSet.set.id;
      localTL_channels_setStickers.stickerset.access_hash = this.selectedStickerSet.set.access_hash;
    }
  }
  
  private void showEditDoneProgress(final boolean paramBoolean)
  {
    if (this.doneItem == null) {
      return;
    }
    if (this.doneItemAnimation != null) {
      this.doneItemAnimation.cancel();
    }
    this.doneItemAnimation = new AnimatorSet();
    if (paramBoolean)
    {
      this.progressView.setVisibility(0);
      this.doneItem.setEnabled(false);
      this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressView, "alpha", new float[] { 1.0F }) });
    }
    for (;;)
    {
      this.doneItemAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((GroupStickersActivity.this.doneItemAnimation != null) && (GroupStickersActivity.this.doneItemAnimation.equals(paramAnonymousAnimator))) {
            GroupStickersActivity.access$2502(GroupStickersActivity.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((GroupStickersActivity.this.doneItemAnimation != null) && (GroupStickersActivity.this.doneItemAnimation.equals(paramAnonymousAnimator)))
          {
            if (!paramBoolean) {
              GroupStickersActivity.this.progressView.setVisibility(4);
            }
          }
          else {
            return;
          }
          GroupStickersActivity.this.doneItem.getImageView().setVisibility(4);
        }
      });
      this.doneItemAnimation.setDuration(150L);
      this.doneItemAnimation.start();
      return;
      this.doneItem.getImageView().setVisibility(0);
      this.doneItem.setEnabled(true);
      this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[] { 1.0F }) });
    }
  }
  
  private void updateRows()
  {
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.nameRow = i;
    if ((this.selectedStickerSet != null) || (this.searchWas))
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.selectedStickerRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.infoRow = i;
      ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getStickerSets(0);
      if (localArrayList.isEmpty()) {
        break label194;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.headerRow = i;
      this.stickersStartRow = this.rowCount;
      this.stickersEndRow = (this.rowCount + localArrayList.size());
      this.rowCount += localArrayList.size();
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    for (this.stickersShadowRow = i;; this.stickersShadowRow = -1)
    {
      if (this.nameContainer != null) {
        this.nameContainer.invalidate();
      }
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      return;
      this.selectedStickerRow = -1;
      break;
      label194:
      this.headerRow = -1;
      this.stickersStartRow = -1;
      this.stickersEndRow = -1;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("GroupStickers", 2131493636));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          GroupStickersActivity.this.finishFragment();
        }
        while ((paramAnonymousInt != 1) || (GroupStickersActivity.this.donePressed)) {
          return;
        }
        GroupStickersActivity.access$002(GroupStickersActivity.this, true);
        if (GroupStickersActivity.this.searching)
        {
          GroupStickersActivity.this.showEditDoneProgress(true);
          return;
        }
        GroupStickersActivity.this.saveStickerSet();
      }
    });
    this.doneItem = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.progressView = new ContextProgressView(paramContext, 1);
    this.doneItem.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0F));
    this.progressView.setVisibility(4);
    this.nameContainer = new LinearLayout(paramContext)
    {
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        if (GroupStickersActivity.this.selectedStickerSet != null) {
          paramAnonymousCanvas.drawLine(0.0F, getHeight() - 1, getWidth(), getHeight() - 1, Theme.dividerPaint);
        }
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramAnonymousInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42.0F), 1073741824));
      }
    };
    this.nameContainer.setWeightSum(1.0F);
    this.nameContainer.setWillNotDraw(false);
    this.nameContainer.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
    this.nameContainer.setOrientation(0);
    this.nameContainer.setPadding(AndroidUtilities.dp(17.0F), 0, AndroidUtilities.dp(14.0F), 0);
    this.editText = new EditText(paramContext);
    this.editText.setText(MessagesController.getInstance(this.currentAccount).linkPrefix + "/addstickers/");
    this.editText.setTextSize(1, 17.0F);
    this.editText.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.editText.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.editText.setMaxLines(1);
    this.editText.setLines(1);
    this.editText.setEnabled(false);
    this.editText.setFocusable(false);
    this.editText.setBackgroundDrawable(null);
    this.editText.setPadding(0, 0, 0, 0);
    this.editText.setGravity(16);
    this.editText.setSingleLine(true);
    this.editText.setInputType(163840);
    this.editText.setImeOptions(6);
    this.nameContainer.addView(this.editText, LayoutHelper.createLinear(-2, 42));
    this.usernameTextView = new EditTextBoldCursor(paramContext);
    this.usernameTextView.setTextSize(1, 17.0F);
    this.usernameTextView.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.usernameTextView.setCursorSize(AndroidUtilities.dp(20.0F));
    this.usernameTextView.setCursorWidth(1.5F);
    this.usernameTextView.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.usernameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.usernameTextView.setMaxLines(1);
    this.usernameTextView.setLines(1);
    this.usernameTextView.setBackgroundDrawable(null);
    this.usernameTextView.setPadding(0, 0, 0, 0);
    this.usernameTextView.setSingleLine(true);
    this.usernameTextView.setGravity(16);
    this.usernameTextView.setInputType(163872);
    this.usernameTextView.setImeOptions(6);
    this.usernameTextView.setHint(LocaleController.getString("ChooseStickerSetPlaceholder", 2131493252));
    this.usernameTextView.addTextChangedListener(new TextWatcher()
    {
      boolean ignoreTextChange;
      
      public void afterTextChanged(Editable paramAnonymousEditable)
      {
        ImageView localImageView;
        if (GroupStickersActivity.this.eraseImageView != null)
        {
          localImageView = GroupStickersActivity.this.eraseImageView;
          if (paramAnonymousEditable.length() <= 0) {
            break label52;
          }
        }
        label52:
        for (int i = 0;; i = 4)
        {
          localImageView.setVisibility(i);
          if ((!this.ignoreTextChange) && (!GroupStickersActivity.this.ignoreTextChanges)) {
            break;
          }
          return;
        }
        if (paramAnonymousEditable.length() > 5) {
          this.ignoreTextChange = true;
        }
        try
        {
          paramAnonymousEditable = Uri.parse(paramAnonymousEditable.toString());
          if (paramAnonymousEditable != null)
          {
            paramAnonymousEditable = paramAnonymousEditable.getPathSegments();
            if ((paramAnonymousEditable.size() == 2) && (((String)paramAnonymousEditable.get(0)).toLowerCase().equals("addstickers")))
            {
              GroupStickersActivity.this.usernameTextView.setText((CharSequence)paramAnonymousEditable.get(1));
              GroupStickersActivity.this.usernameTextView.setSelection(GroupStickersActivity.this.usernameTextView.length());
            }
          }
        }
        catch (Exception paramAnonymousEditable)
        {
          for (;;) {}
        }
        this.ignoreTextChange = false;
        GroupStickersActivity.this.resolveStickerSet();
      }
      
      public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      
      public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
    });
    this.nameContainer.addView(this.usernameTextView, LayoutHelper.createLinear(0, 42, 1.0F));
    this.eraseImageView = new ImageView(paramContext);
    this.eraseImageView.setScaleType(ImageView.ScaleType.CENTER);
    this.eraseImageView.setImageResource(2131165374);
    this.eraseImageView.setPadding(AndroidUtilities.dp(16.0F), 0, 0, 0);
    this.eraseImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteGrayText3"), PorterDuff.Mode.MULTIPLY));
    this.eraseImageView.setVisibility(4);
    this.eraseImageView.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        GroupStickersActivity.access$902(GroupStickersActivity.this, false);
        GroupStickersActivity.access$402(GroupStickersActivity.this, null);
        GroupStickersActivity.this.usernameTextView.setText("");
        GroupStickersActivity.this.updateRows();
      }
    });
    this.nameContainer.addView(this.eraseImageView, LayoutHelper.createLinear(42, 42, 0.0F));
    if ((this.info != null) && (this.info.stickerset != null))
    {
      this.ignoreTextChanges = true;
      this.usernameTextView.setText(this.info.stickerset.short_name);
      this.usernameTextView.setSelection(this.usernameTextView.length());
      this.ignoreTextChanges = false;
    }
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    localFrameLayout.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setFocusable(true);
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.layoutManager = new LinearLayoutManager(paramContext)
    {
      public boolean requestChildRectangleOnScreen(RecyclerView paramAnonymousRecyclerView, View paramAnonymousView, Rect paramAnonymousRect, boolean paramAnonymousBoolean1, boolean paramAnonymousBoolean2)
      {
        return false;
      }
      
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    };
    this.layoutManager.setOrientation(1);
    this.listView.setLayoutManager(this.layoutManager);
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        if (GroupStickersActivity.this.getParentActivity() == null) {}
        do
        {
          do
          {
            return;
            if (paramAnonymousInt != GroupStickersActivity.this.selectedStickerRow) {
              break;
            }
          } while (GroupStickersActivity.this.selectedStickerSet == null);
          GroupStickersActivity.this.showDialog(new StickersAlert(GroupStickersActivity.this.getParentActivity(), GroupStickersActivity.this, null, GroupStickersActivity.this.selectedStickerSet, null));
          return;
        } while ((paramAnonymousInt < GroupStickersActivity.this.stickersStartRow) || (paramAnonymousInt >= GroupStickersActivity.this.stickersEndRow));
        if (GroupStickersActivity.this.selectedStickerRow == -1) {}
        for (int i = 1;; i = 0)
        {
          int k = GroupStickersActivity.this.layoutManager.findFirstVisibleItemPosition();
          int j = Integer.MAX_VALUE;
          paramAnonymousView = (RecyclerListView.Holder)GroupStickersActivity.this.listView.findViewHolderForAdapterPosition(k);
          if (paramAnonymousView != null) {
            j = paramAnonymousView.itemView.getTop();
          }
          GroupStickersActivity.access$402(GroupStickersActivity.this, (TLRPC.TL_messages_stickerSet)DataQuery.getInstance(GroupStickersActivity.this.currentAccount).getStickerSets(0).get(paramAnonymousInt - GroupStickersActivity.this.stickersStartRow));
          GroupStickersActivity.access$602(GroupStickersActivity.this, true);
          GroupStickersActivity.this.usernameTextView.setText(GroupStickersActivity.this.selectedStickerSet.set.short_name);
          GroupStickersActivity.this.usernameTextView.setSelection(GroupStickersActivity.this.usernameTextView.length());
          GroupStickersActivity.access$602(GroupStickersActivity.this, false);
          AndroidUtilities.hideKeyboard(GroupStickersActivity.this.usernameTextView);
          GroupStickersActivity.this.updateRows();
          if ((i == 0) || (j == Integer.MAX_VALUE)) {
            break;
          }
          GroupStickersActivity.this.layoutManager.scrollToPositionWithOffset(k + 1, j);
          return;
        }
      }
    });
    this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
    {
      public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
      {
        if (paramAnonymousInt == 1) {
          AndroidUtilities.hideKeyboard(GroupStickersActivity.this.getParentActivity().getCurrentFocus());
        }
      }
      
      public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2) {}
    });
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.stickersDidLoaded) {
      if (((Integer)paramVarArgs[0]).intValue() == 0) {
        updateRows();
      }
    }
    do
    {
      do
      {
        do
        {
          return;
          if (paramInt1 != NotificationCenter.chatInfoDidLoaded) {
            break;
          }
          paramVarArgs = (TLRPC.ChatFull)paramVarArgs[0];
        } while (paramVarArgs.id != this.chatId);
        if ((this.info == null) && (paramVarArgs.stickerset != null)) {
          this.selectedStickerSet = DataQuery.getInstance(this.currentAccount).getGroupStickerSetById(paramVarArgs.stickerset);
        }
        this.info = paramVarArgs;
        updateRows();
        return;
      } while (paramInt1 != NotificationCenter.groupStickersDidLoaded);
      ((Long)paramVarArgs[0]).longValue();
    } while ((this.info == null) || (this.info.stickerset == null) || (this.info.stickerset.id != paramInt1));
    updateRows();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { StickerSetCell.class, TextSettingsCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.usernameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.usernameTextView, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.listView, ThemeDescription.FLAG_LINKCOLOR, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteLinkText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.nameContainer, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.listView, 0, new Class[] { StickerSetCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { StickerSetCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[] { StickerSetCell.class }, new String[] { "optionsButton" }, null, null, null, "stickers_menuSelector"), new ThemeDescription(this.listView, 0, new Class[] { StickerSetCell.class }, new String[] { "optionsButton" }, null, null, null, "stickers_menu") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    DataQuery.getInstance(this.currentAccount).checkStickers(0);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.stickersDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.groupStickersDidLoaded);
    updateRows();
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.stickersDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.groupStickersDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    if (!MessagesController.getGlobalMainSettings().getBoolean("view_animations", true))
    {
      this.usernameTextView.requestFocus();
      AndroidUtilities.showKeyboard(this.usernameTextView);
    }
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (GroupStickersActivity.this.usernameTextView != null)
          {
            GroupStickersActivity.this.usernameTextView.requestFocus();
            AndroidUtilities.showKeyboard(GroupStickersActivity.this.usernameTextView);
          }
        }
      }, 100L);
    }
  }
  
  public void setInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    if ((this.info != null) && (this.info.stickerset != null)) {
      this.selectedStickerSet = DataQuery.getInstance(this.currentAccount).getGroupStickerSetById(this.info.stickerset);
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
      return GroupStickersActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt >= GroupStickersActivity.this.stickersStartRow) && (paramInt < GroupStickersActivity.this.stickersEndRow)) {}
      do
      {
        return 0;
        if (paramInt == GroupStickersActivity.this.infoRow) {
          return 1;
        }
        if (paramInt == GroupStickersActivity.this.nameRow) {
          return 2;
        }
        if (paramInt == GroupStickersActivity.this.stickersShadowRow) {
          return 3;
        }
        if (paramInt == GroupStickersActivity.this.headerRow) {
          return 4;
        }
      } while (paramInt != GroupStickersActivity.this.selectedStickerRow);
      return 5;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      return (i == 0) || (i == 2) || (i == 5);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      case 2: 
      case 3: 
      default: 
      case 0: 
      case 1: 
        Object localObject2;
        do
        {
          return;
          localObject1 = DataQuery.getInstance(GroupStickersActivity.this.currentAccount).getStickerSets(0);
          paramInt -= GroupStickersActivity.this.stickersStartRow;
          paramViewHolder = (StickerSetCell)paramViewHolder.itemView;
          localObject2 = (TLRPC.TL_messages_stickerSet)((ArrayList)localObject1).get(paramInt);
          TLRPC.TL_messages_stickerSet localTL_messages_stickerSet = (TLRPC.TL_messages_stickerSet)((ArrayList)localObject1).get(paramInt);
          long l;
          if (paramInt != ((ArrayList)localObject1).size() - 1)
          {
            bool = true;
            paramViewHolder.setStickersSet(localTL_messages_stickerSet, bool);
            if (GroupStickersActivity.this.selectedStickerSet == null) {
              break label173;
            }
            l = GroupStickersActivity.this.selectedStickerSet.set.id;
            if (((TLRPC.TL_messages_stickerSet)localObject2).set.id != l) {
              break label220;
            }
          }
          for (boolean bool = true;; bool = false)
          {
            paramViewHolder.setChecked(bool);
            return;
            bool = false;
            break;
            if ((GroupStickersActivity.this.info != null) && (GroupStickersActivity.this.info.stickerset != null))
            {
              l = GroupStickersActivity.this.info.stickerset.id;
              break label146;
            }
            l = 0L;
            break label146;
          }
        } while (paramInt != GroupStickersActivity.this.infoRow);
        Object localObject1 = LocaleController.getString("ChooseStickerSetMy", 2131493249);
        paramInt = ((String)localObject1).indexOf("@stickers");
        if (paramInt != -1) {
          try
          {
            localObject2 = new SpannableStringBuilder((CharSequence)localObject1);
            ((SpannableStringBuilder)localObject2).setSpan(new URLSpanNoUnderline("@stickers")
            {
              public void onClick(View paramAnonymousView)
              {
                MessagesController.getInstance(GroupStickersActivity.this.currentAccount).openByUserName("stickers", GroupStickersActivity.this, 1);
              }
            }, paramInt, "@stickers".length() + paramInt, 18);
            ((TextInfoPrivacyCell)paramViewHolder.itemView).setText((CharSequence)localObject2);
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            ((TextInfoPrivacyCell)paramViewHolder.itemView).setText((CharSequence)localObject1);
            return;
          }
        }
        ((TextInfoPrivacyCell)paramViewHolder.itemView).setText((CharSequence)localObject1);
        return;
      case 4: 
        label146:
        label173:
        label220:
        ((HeaderCell)paramViewHolder.itemView).setText(LocaleController.getString("ChooseFromYourStickers", 2131493247));
        return;
      }
      paramViewHolder = (StickerSetCell)paramViewHolder.itemView;
      if (GroupStickersActivity.this.selectedStickerSet != null)
      {
        paramViewHolder.setStickersSet(GroupStickersActivity.this.selectedStickerSet, false);
        return;
      }
      if (GroupStickersActivity.this.searching)
      {
        paramViewHolder.setText(LocaleController.getString("Loading", 2131493762), null, 0, false);
        return;
      }
      paramViewHolder.setText(LocaleController.getString("ChooseStickerSetNotFound", 2131493250), LocaleController.getString("ChooseStickerSetNotFoundInfo", 2131493251), 2131165416, false);
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = this.mContext;
        if (paramInt == 0) {}
        for (paramInt = 3;; paramInt = 2)
        {
          paramViewGroup = new StickerSetCell(paramViewGroup, paramInt);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          break;
        }
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
        continue;
        paramViewGroup = GroupStickersActivity.this.nameContainer;
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
        continue;
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/GroupStickersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */