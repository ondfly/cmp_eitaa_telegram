package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Cells.GroupCreateSectionCell;
import org.telegram.ui.Cells.GroupCreateUserCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.GroupCreateDividerItemDecoration;
import org.telegram.ui.Components.GroupCreateSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.FastScrollAdapter;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;

public class GroupCreateActivity
  extends BaseFragment
  implements View.OnClickListener, NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private GroupCreateAdapter adapter;
  private ArrayList<GroupCreateSpan> allSpans = new ArrayList();
  private int chatId;
  private int chatType = 0;
  private int containerHeight;
  private GroupCreateSpan currentDeletingSpan;
  private AnimatorSet currentDoneButtonAnimation;
  private GroupCreateActivityDelegate delegate;
  private View doneButton;
  private boolean doneButtonVisible;
  private EditTextBoldCursor editText;
  private EmptyTextProgressView emptyView;
  private int fieldY;
  private boolean ignoreScrollEvent;
  private boolean isAlwaysShare;
  private boolean isGroup;
  private boolean isNeverShare;
  private GroupCreateDividerItemDecoration itemDecoration;
  private RecyclerListView listView;
  private int maxCount = MessagesController.getInstance(this.currentAccount).maxMegagroupCount;
  private ScrollView scrollView;
  private boolean searchWas;
  private boolean searching;
  private SparseArray<GroupCreateSpan> selectedContacts = new SparseArray();
  private SpansContainer spansContainer;
  
  public GroupCreateActivity() {}
  
  public GroupCreateActivity(Bundle paramBundle)
  {
    super(paramBundle);
    this.chatType = paramBundle.getInt("chatType", 0);
    this.isAlwaysShare = paramBundle.getBoolean("isAlwaysShare", false);
    this.isNeverShare = paramBundle.getBoolean("isNeverShare", false);
    this.isGroup = paramBundle.getBoolean("isGroup", false);
    this.chatId = paramBundle.getInt("chatId");
    if (this.chatType == 0) {}
    for (int i = MessagesController.getInstance(this.currentAccount).maxMegagroupCount;; i = MessagesController.getInstance(this.currentAccount).maxBroadcastCount)
    {
      this.maxCount = i;
      return;
    }
  }
  
  private void checkVisibleRows()
  {
    int j = this.listView.getChildCount();
    int i = 0;
    if (i < j)
    {
      Object localObject = this.listView.getChildAt(i);
      if ((localObject instanceof GroupCreateUserCell))
      {
        localObject = (GroupCreateUserCell)localObject;
        TLRPC.User localUser = ((GroupCreateUserCell)localObject).getUser();
        if (localUser != null) {
          if (this.selectedContacts.indexOfKey(localUser.id) < 0) {
            break label83;
          }
        }
      }
      label83:
      for (boolean bool = true;; bool = false)
      {
        ((GroupCreateUserCell)localObject).setChecked(bool, true);
        i += 1;
        break;
      }
    }
  }
  
  private void closeSearch()
  {
    this.searching = false;
    this.searchWas = false;
    this.itemDecoration.setSearching(false);
    this.adapter.setSearching(false);
    this.adapter.searchDialogs(null);
    this.listView.setFastScrollVisible(true);
    this.listView.setVerticalScrollBarEnabled(false);
    this.emptyView.setText(LocaleController.getString("NoContacts", 2131493887));
  }
  
  private boolean onDonePressed()
  {
    boolean bool2 = false;
    Object localObject1;
    int i;
    Object localObject2;
    if (this.chatType == 2)
    {
      localObject1 = new ArrayList();
      i = 0;
      while (i < this.selectedContacts.size())
      {
        localObject2 = MessagesController.getInstance(this.currentAccount).getInputUser(MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.selectedContacts.keyAt(i))));
        if (localObject2 != null) {
          ((ArrayList)localObject1).add(localObject2);
        }
        i += 1;
      }
      MessagesController.getInstance(this.currentAccount).addUsersToChannel(this.chatId, (ArrayList)localObject1, null);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
      localObject1 = new Bundle();
      ((Bundle)localObject1).putInt("chat_id", this.chatId);
      presentFragment(new ChatActivity((Bundle)localObject1), true);
    }
    for (;;)
    {
      boolean bool1 = true;
      do
      {
        do
        {
          return bool1;
          bool1 = bool2;
        } while (!this.doneButtonVisible);
        bool1 = bool2;
      } while (this.selectedContacts.size() == 0);
      localObject1 = new ArrayList();
      i = 0;
      while (i < this.selectedContacts.size())
      {
        ((ArrayList)localObject1).add(Integer.valueOf(this.selectedContacts.keyAt(i)));
        i += 1;
      }
      if ((this.isAlwaysShare) || (this.isNeverShare))
      {
        if (this.delegate != null) {
          this.delegate.didSelectUsers((ArrayList)localObject1);
        }
        finishFragment();
      }
      else
      {
        localObject2 = new Bundle();
        ((Bundle)localObject2).putIntegerArrayList("result", (ArrayList)localObject1);
        ((Bundle)localObject2).putInt("chatType", this.chatType);
        presentFragment(new GroupCreateFinalActivity((Bundle)localObject2));
      }
    }
  }
  
  private void updateHint()
  {
    if ((!this.isAlwaysShare) && (!this.isNeverShare))
    {
      if (this.chatType == 2) {
        this.actionBar.setSubtitle(LocaleController.formatPluralString("Members", this.selectedContacts.size()));
      }
    }
    else if (this.chatType != 2)
    {
      if ((!this.doneButtonVisible) || (!this.allSpans.isEmpty())) {
        break label279;
      }
      if (this.currentDoneButtonAnimation != null) {
        this.currentDoneButtonAnimation.cancel();
      }
      this.currentDoneButtonAnimation = new AnimatorSet();
      this.currentDoneButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneButton, "scaleX", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneButton, "scaleY", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneButton, "alpha", new float[] { 0.0F }) });
      this.currentDoneButtonAnimation.setDuration(180L);
      this.currentDoneButtonAnimation.start();
      this.doneButtonVisible = false;
    }
    label279:
    while ((this.doneButtonVisible) || (this.allSpans.isEmpty()))
    {
      return;
      if (this.selectedContacts.size() == 0)
      {
        this.actionBar.setSubtitle(LocaleController.formatString("MembersCountZero", 2131493810, new Object[] { LocaleController.formatPluralString("Members", this.maxCount) }));
        break;
      }
      this.actionBar.setSubtitle(LocaleController.formatString("MembersCount", 2131493809, new Object[] { Integer.valueOf(this.selectedContacts.size()), Integer.valueOf(this.maxCount) }));
      break;
    }
    if (this.currentDoneButtonAnimation != null) {
      this.currentDoneButtonAnimation.cancel();
    }
    this.currentDoneButtonAnimation = new AnimatorSet();
    this.currentDoneButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneButton, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButton, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneButton, "alpha", new float[] { 1.0F }) });
    this.currentDoneButtonAnimation.setDuration(180L);
    this.currentDoneButtonAnimation.start();
    this.doneButtonVisible = true;
  }
  
  public View createView(Context paramContext)
  {
    int j = 1;
    this.searching = false;
    this.searchWas = false;
    this.allSpans.clear();
    this.selectedContacts.clear();
    this.currentDeletingSpan = null;
    boolean bool;
    label90:
    Object localObject1;
    Object localObject2;
    if (this.chatType == 2)
    {
      bool = true;
      this.doneButtonVisible = bool;
      this.actionBar.setBackButtonImage(2131165346);
      this.actionBar.setAllowOverlayTitle(true);
      if (this.chatType != 2) {
        break label768;
      }
      this.actionBar.setTitle(LocaleController.getString("ChannelAddMembers", 2131493145));
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            GroupCreateActivity.this.finishFragment();
          }
          while (paramAnonymousInt != 1) {
            return;
          }
          GroupCreateActivity.this.onDonePressed();
        }
      });
      this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
      if (this.chatType != 2)
      {
        this.doneButton.setScaleX(0.0F);
        this.doneButton.setScaleY(0.0F);
        this.doneButton.setAlpha(0.0F);
      }
      this.fragmentView = new ViewGroup(paramContext)
      {
        protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
        {
          boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
          if ((paramAnonymousView == GroupCreateActivity.this.listView) || (paramAnonymousView == GroupCreateActivity.this.emptyView)) {
            GroupCreateActivity.this.parentLayout.drawHeaderShadow(paramAnonymousCanvas, GroupCreateActivity.this.scrollView.getMeasuredHeight());
          }
          return bool;
        }
        
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          GroupCreateActivity.this.scrollView.layout(0, 0, GroupCreateActivity.this.scrollView.getMeasuredWidth(), GroupCreateActivity.this.scrollView.getMeasuredHeight());
          GroupCreateActivity.this.listView.layout(0, GroupCreateActivity.this.scrollView.getMeasuredHeight(), GroupCreateActivity.this.listView.getMeasuredWidth(), GroupCreateActivity.this.scrollView.getMeasuredHeight() + GroupCreateActivity.this.listView.getMeasuredHeight());
          GroupCreateActivity.this.emptyView.layout(0, GroupCreateActivity.this.scrollView.getMeasuredHeight(), GroupCreateActivity.this.emptyView.getMeasuredWidth(), GroupCreateActivity.this.scrollView.getMeasuredHeight() + GroupCreateActivity.this.emptyView.getMeasuredHeight());
        }
        
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int i = View.MeasureSpec.getSize(paramAnonymousInt1);
          paramAnonymousInt2 = View.MeasureSpec.getSize(paramAnonymousInt2);
          setMeasuredDimension(i, paramAnonymousInt2);
          if ((AndroidUtilities.isTablet()) || (paramAnonymousInt2 > i)) {}
          for (paramAnonymousInt1 = AndroidUtilities.dp(144.0F);; paramAnonymousInt1 = AndroidUtilities.dp(56.0F))
          {
            GroupCreateActivity.this.scrollView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, Integer.MIN_VALUE));
            GroupCreateActivity.this.listView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2 - GroupCreateActivity.this.scrollView.getMeasuredHeight(), 1073741824));
            GroupCreateActivity.this.emptyView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt2 - GroupCreateActivity.this.scrollView.getMeasuredHeight(), 1073741824));
            return;
          }
        }
      };
      localObject1 = (ViewGroup)this.fragmentView;
      this.scrollView = new ScrollView(paramContext)
      {
        public boolean requestChildRectangleOnScreen(View paramAnonymousView, Rect paramAnonymousRect, boolean paramAnonymousBoolean)
        {
          if (GroupCreateActivity.this.ignoreScrollEvent)
          {
            GroupCreateActivity.access$302(GroupCreateActivity.this, false);
            return false;
          }
          paramAnonymousRect.offset(paramAnonymousView.getLeft() - paramAnonymousView.getScrollX(), paramAnonymousView.getTop() - paramAnonymousView.getScrollY());
          paramAnonymousRect.top += GroupCreateActivity.this.fieldY + AndroidUtilities.dp(20.0F);
          paramAnonymousRect.bottom += GroupCreateActivity.this.fieldY + AndroidUtilities.dp(50.0F);
          return super.requestChildRectangleOnScreen(paramAnonymousView, paramAnonymousRect, paramAnonymousBoolean);
        }
      };
      this.scrollView.setVerticalScrollBarEnabled(false);
      AndroidUtilities.setScrollViewEdgeEffectColor(this.scrollView, Theme.getColor("windowBackgroundWhite"));
      ((ViewGroup)localObject1).addView(this.scrollView);
      this.spansContainer = new SpansContainer(paramContext);
      this.scrollView.addView(this.spansContainer, LayoutHelper.createFrame(-1, -2.0F));
      this.editText = new EditTextBoldCursor(paramContext)
      {
        public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
        {
          if (GroupCreateActivity.this.currentDeletingSpan != null)
          {
            GroupCreateActivity.this.currentDeletingSpan.cancelDeleteAnimation();
            GroupCreateActivity.access$1502(GroupCreateActivity.this, null);
          }
          return super.onTouchEvent(paramAnonymousMotionEvent);
        }
      };
      this.editText.setTextSize(1, 18.0F);
      this.editText.setHintColor(Theme.getColor("groupcreate_hintText"));
      this.editText.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.editText.setCursorColor(Theme.getColor("groupcreate_cursor"));
      this.editText.setCursorWidth(1.5F);
      this.editText.setInputType(655536);
      this.editText.setSingleLine(true);
      this.editText.setBackgroundDrawable(null);
      this.editText.setVerticalScrollBarEnabled(false);
      this.editText.setHorizontalScrollBarEnabled(false);
      this.editText.setTextIsSelectable(false);
      this.editText.setPadding(0, 0, 0, 0);
      this.editText.setImeOptions(268435462);
      localObject2 = this.editText;
      if (!LocaleController.isRTL) {
        break label920;
      }
      i = 5;
      label415:
      ((EditTextBoldCursor)localObject2).setGravity(i | 0x10);
      this.spansContainer.addView(this.editText);
      if (this.chatType != 2) {
        break label925;
      }
      this.editText.setHintText(LocaleController.getString("AddMutual", 2131492933));
      label459:
      this.editText.setCustomSelectionActionModeCallback(new ActionMode.Callback()
      {
        public boolean onActionItemClicked(ActionMode paramAnonymousActionMode, MenuItem paramAnonymousMenuItem)
        {
          return false;
        }
        
        public boolean onCreateActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
        {
          return false;
        }
        
        public void onDestroyActionMode(ActionMode paramAnonymousActionMode) {}
        
        public boolean onPrepareActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
        {
          return false;
        }
      });
      this.editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          return (paramAnonymousInt == 6) && (GroupCreateActivity.this.onDonePressed());
        }
      });
      this.editText.setOnKeyListener(new View.OnKeyListener()
      {
        private boolean wasEmpty;
        
        public boolean onKey(View paramAnonymousView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if (paramAnonymousInt == 67)
          {
            if (paramAnonymousKeyEvent.getAction() != 0) {
              break label43;
            }
            if (GroupCreateActivity.this.editText.length() != 0) {
              break label37;
            }
            bool = true;
            this.wasEmpty = bool;
          }
          label37:
          label43:
          while ((paramAnonymousKeyEvent.getAction() != 1) || (!this.wasEmpty) || (GroupCreateActivity.this.allSpans.isEmpty())) {
            for (;;)
            {
              return false;
              boolean bool = false;
            }
          }
          GroupCreateActivity.this.spansContainer.removeSpan((GroupCreateSpan)GroupCreateActivity.this.allSpans.get(GroupCreateActivity.this.allSpans.size() - 1));
          GroupCreateActivity.this.updateHint();
          GroupCreateActivity.this.checkVisibleRows();
          return true;
        }
      });
      this.editText.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable)
        {
          if (GroupCreateActivity.this.editText.length() != 0)
          {
            GroupCreateActivity.access$1902(GroupCreateActivity.this, true);
            GroupCreateActivity.access$2002(GroupCreateActivity.this, true);
            GroupCreateActivity.this.adapter.setSearching(true);
            GroupCreateActivity.this.itemDecoration.setSearching(true);
            GroupCreateActivity.this.adapter.searchDialogs(GroupCreateActivity.this.editText.getText().toString());
            GroupCreateActivity.this.listView.setFastScrollVisible(false);
            GroupCreateActivity.this.listView.setVerticalScrollBarEnabled(true);
            GroupCreateActivity.this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
            return;
          }
          GroupCreateActivity.this.closeSearch();
        }
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      });
      this.emptyView = new EmptyTextProgressView(paramContext);
      if (!ContactsController.getInstance(this.currentAccount).isLoadingContacts()) {
        break label1048;
      }
      this.emptyView.showProgress();
      label551:
      this.emptyView.setShowAtCenter(true);
      this.emptyView.setText(LocaleController.getString("NoContacts", 2131493887));
      ((ViewGroup)localObject1).addView(this.emptyView);
      localObject2 = new LinearLayoutManager(paramContext, 1, false);
      this.listView = new RecyclerListView(paramContext);
      this.listView.setFastScrollEnabled();
      this.listView.setEmptyView(this.emptyView);
      RecyclerListView localRecyclerListView = this.listView;
      paramContext = new GroupCreateAdapter(paramContext);
      this.adapter = paramContext;
      localRecyclerListView.setAdapter(paramContext);
      this.listView.setLayoutManager((RecyclerView.LayoutManager)localObject2);
      this.listView.setVerticalScrollBarEnabled(false);
      paramContext = this.listView;
      if (!LocaleController.isRTL) {
        break label1058;
      }
    }
    label768:
    label920:
    label925:
    label1048:
    label1058:
    for (int i = j;; i = 2)
    {
      paramContext.setVerticalScrollbarPosition(i);
      paramContext = this.listView;
      localObject2 = new GroupCreateDividerItemDecoration();
      this.itemDecoration = ((GroupCreateDividerItemDecoration)localObject2);
      paramContext.addItemDecoration((RecyclerView.ItemDecoration)localObject2);
      ((ViewGroup)localObject1).addView(this.listView);
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool2 = false;
          if (!(paramAnonymousView instanceof GroupCreateUserCell)) {}
          label47:
          label150:
          label364:
          label380:
          for (;;)
          {
            return;
            paramAnonymousView = (GroupCreateUserCell)paramAnonymousView;
            Object localObject = paramAnonymousView.getUser();
            if (localObject != null)
            {
              if (GroupCreateActivity.this.selectedContacts.indexOfKey(((TLRPC.User)localObject).id) >= 0)
              {
                paramAnonymousInt = 1;
                if (paramAnonymousInt == 0) {
                  break label150;
                }
                localObject = (GroupCreateSpan)GroupCreateActivity.this.selectedContacts.get(((TLRPC.User)localObject).id);
                GroupCreateActivity.this.spansContainer.removeSpan((GroupCreateSpan)localObject);
                GroupCreateActivity.this.updateHint();
                if ((!GroupCreateActivity.this.searching) && (!GroupCreateActivity.this.searchWas)) {
                  break label364;
                }
                AndroidUtilities.showKeyboard(GroupCreateActivity.this.editText);
              }
              for (;;)
              {
                if (GroupCreateActivity.this.editText.length() <= 0) {
                  break label380;
                }
                GroupCreateActivity.this.editText.setText(null);
                return;
                paramAnonymousInt = 0;
                break label47;
                if ((GroupCreateActivity.this.maxCount != 0) && (GroupCreateActivity.this.selectedContacts.size() == GroupCreateActivity.this.maxCount)) {
                  break;
                }
                if ((GroupCreateActivity.this.chatType == 0) && (GroupCreateActivity.this.selectedContacts.size() == MessagesController.getInstance(GroupCreateActivity.this.currentAccount).maxGroupCount))
                {
                  paramAnonymousView = new AlertDialog.Builder(GroupCreateActivity.this.getParentActivity());
                  paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
                  paramAnonymousView.setMessage(LocaleController.getString("SoftUserLimitAlert", 2131494415));
                  paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                  GroupCreateActivity.this.showDialog(paramAnonymousView.create());
                  return;
                }
                MessagesController localMessagesController = MessagesController.getInstance(GroupCreateActivity.this.currentAccount);
                if (!GroupCreateActivity.this.searching) {}
                for (boolean bool1 = true;; bool1 = false)
                {
                  localMessagesController.putUser((TLRPC.User)localObject, bool1);
                  localObject = new GroupCreateSpan(GroupCreateActivity.this.editText.getContext(), (TLRPC.User)localObject);
                  GroupCreateActivity.this.spansContainer.addSpan((GroupCreateSpan)localObject);
                  ((GroupCreateSpan)localObject).setOnClickListener(GroupCreateActivity.this);
                  break;
                }
                bool1 = bool2;
                if (paramAnonymousInt == 0) {
                  bool1 = true;
                }
                paramAnonymousView.setChecked(bool1, true);
              }
            }
          }
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if (paramAnonymousInt == 1) {
            AndroidUtilities.hideKeyboard(GroupCreateActivity.this.editText);
          }
        }
      });
      updateHint();
      return this.fragmentView;
      bool = false;
      break;
      if (this.isAlwaysShare)
      {
        if (this.isGroup)
        {
          this.actionBar.setTitle(LocaleController.getString("AlwaysAllow", 2131492956));
          break label90;
        }
        this.actionBar.setTitle(LocaleController.getString("AlwaysShareWithTitle", 2131492960));
        break label90;
      }
      if (this.isNeverShare)
      {
        if (this.isGroup)
        {
          this.actionBar.setTitle(LocaleController.getString("NeverAllow", 2131493861));
          break label90;
        }
        this.actionBar.setTitle(LocaleController.getString("NeverShareWithTitle", 2131493865));
        break label90;
      }
      localObject2 = this.actionBar;
      if (this.chatType == 0) {}
      for (localObject1 = LocaleController.getString("NewGroup", 2131493869);; localObject1 = LocaleController.getString("NewBroadcastList", 2131493866))
      {
        ((ActionBar)localObject2).setTitle((CharSequence)localObject1);
        break;
      }
      i = 3;
      break label415;
      if (this.isAlwaysShare)
      {
        if (this.isGroup)
        {
          this.editText.setHintText(LocaleController.getString("AlwaysAllowPlaceholder", 2131492957));
          break label459;
        }
        this.editText.setHintText(LocaleController.getString("AlwaysShareWithPlaceholder", 2131492959));
        break label459;
      }
      if (this.isNeverShare)
      {
        if (this.isGroup)
        {
          this.editText.setHintText(LocaleController.getString("NeverAllowPlaceholder", 2131493862));
          break label459;
        }
        this.editText.setHintText(LocaleController.getString("NeverShareWithPlaceholder", 2131493864));
        break label459;
      }
      this.editText.setHintText(LocaleController.getString("SendMessageTo", 2131494348));
      break label459;
      this.emptyView.showTextView();
      break label551;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.contactsDidLoaded)
    {
      if (this.emptyView != null) {
        this.emptyView.showTextView();
      }
      if (this.adapter != null) {
        this.adapter.notifyDataSetChanged();
      }
    }
    do
    {
      for (;;)
      {
        return;
        if (paramInt1 != NotificationCenter.updateInterfaces) {
          break;
        }
        if (this.listView != null)
        {
          paramInt2 = ((Integer)paramVarArgs[0]).intValue();
          int i = this.listView.getChildCount();
          if (((paramInt2 & 0x2) != 0) || ((paramInt2 & 0x1) != 0) || ((paramInt2 & 0x4) != 0))
          {
            paramInt1 = 0;
            while (paramInt1 < i)
            {
              paramVarArgs = this.listView.getChildAt(paramInt1);
              if ((paramVarArgs instanceof GroupCreateUserCell)) {
                ((GroupCreateUserCell)paramVarArgs).update(paramInt2);
              }
              paramInt1 += 1;
            }
          }
        }
      }
    } while (paramInt1 != NotificationCenter.chatDidCreated);
    removeSelfFromStack();
  }
  
  public int getContainerHeight()
  {
    return this.containerHeight;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local11 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (GroupCreateActivity.this.listView != null)
        {
          int j = GroupCreateActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = GroupCreateActivity.this.listView.getChildAt(i);
            if ((localView instanceof GroupCreateUserCell)) {
              ((GroupCreateUserCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.scrollView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollActive");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollInactive");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollText");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "groupcreate_hintText");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.editText, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, "groupcreate_cursor");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GroupCreateSectionCell.class }, null, null, null, "graySection");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.listView, 0, new Class[] { GroupCreateSectionCell.class }, new String[] { "drawable" }, null, null, null, "groupcreate_sectionShadow");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { GroupCreateSectionCell.class }, new String[] { "textView" }, null, null, null, "groupcreate_sectionText");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { GroupCreateUserCell.class }, new String[] { "textView" }, null, null, null, "groupcreate_sectionText");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { GroupCreateUserCell.class }, new String[] { "checkBox" }, null, null, null, "groupcreate_checkbox");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { GroupCreateUserCell.class }, new String[] { "checkBox" }, null, null, null, "groupcreate_checkboxCheck");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { GroupCreateUserCell.class }, new String[] { "statusTextView" }, null, null, null, "groupcreate_onlineText");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { GroupCreateUserCell.class }, new String[] { "statusTextView" }, null, null, null, "groupcreate_offlineText");
    RecyclerListView localRecyclerListView = this.listView;
    Drawable localDrawable1 = Theme.avatar_photoDrawable;
    Drawable localDrawable2 = Theme.avatar_broadcastDrawable;
    Drawable localDrawable3 = Theme.avatar_savedDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localObject1, localObject2, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, new ThemeDescription(localRecyclerListView, 0, new Class[] { GroupCreateUserCell.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local11, "avatar_backgroundPink"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "avatar_backgroundGroupCreateSpanBlue"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "groupcreate_spanBackground"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "groupcreate_spanText"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "avatar_backgroundBlue") };
  }
  
  public void onClick(View paramView)
  {
    paramView = (GroupCreateSpan)paramView;
    if (paramView.isDeleting())
    {
      this.currentDeletingSpan = null;
      this.spansContainer.removeSpan(paramView);
      updateHint();
      checkVisibleRows();
      return;
    }
    if (this.currentDeletingSpan != null) {
      this.currentDeletingSpan.cancelDeleteAnimation();
    }
    this.currentDeletingSpan = paramView;
    paramView.startDeleteAnimation();
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatDidCreated);
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatDidCreated);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.editText != null) {
      this.editText.requestFocus();
    }
  }
  
  @Keep
  public void setContainerHeight(int paramInt)
  {
    this.containerHeight = paramInt;
    if (this.spansContainer != null) {
      this.spansContainer.requestLayout();
    }
  }
  
  public void setDelegate(GroupCreateActivityDelegate paramGroupCreateActivityDelegate)
  {
    this.delegate = paramGroupCreateActivityDelegate;
  }
  
  public static abstract interface GroupCreateActivityDelegate
  {
    public abstract void didSelectUsers(ArrayList<Integer> paramArrayList);
  }
  
  public class GroupCreateAdapter
    extends RecyclerListView.FastScrollAdapter
  {
    private ArrayList<TLRPC.User> contacts = new ArrayList();
    private Context context;
    private SearchAdapterHelper searchAdapterHelper;
    private ArrayList<TLRPC.User> searchResult = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Timer searchTimer;
    private boolean searching;
    
    public GroupCreateAdapter(Context paramContext)
    {
      this.context = paramContext;
      paramContext = ContactsController.getInstance(GroupCreateActivity.this.currentAccount).contacts;
      int i = 0;
      if (i < paramContext.size())
      {
        TLRPC.User localUser = MessagesController.getInstance(GroupCreateActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)paramContext.get(i)).user_id));
        if ((localUser == null) || (localUser.self) || (localUser.deleted)) {}
        for (;;)
        {
          i += 1;
          break;
          this.contacts.add(localUser);
        }
      }
      this.searchAdapterHelper = new SearchAdapterHelper(true);
      this.searchAdapterHelper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate()
      {
        public void onDataSetChanged()
        {
          GroupCreateActivity.GroupCreateAdapter.this.notifyDataSetChanged();
        }
        
        public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramAnonymousArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramAnonymousHashMap) {}
      });
    }
    
    private void updateSearchResults(final ArrayList<TLRPC.User> paramArrayList, final ArrayList<CharSequence> paramArrayList1)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          GroupCreateActivity.GroupCreateAdapter.access$3402(GroupCreateActivity.GroupCreateAdapter.this, paramArrayList);
          GroupCreateActivity.GroupCreateAdapter.access$3502(GroupCreateActivity.GroupCreateAdapter.this, paramArrayList1);
          GroupCreateActivity.GroupCreateAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public int getItemCount()
    {
      if (this.searching)
      {
        int j = this.searchResult.size();
        int k = this.searchAdapterHelper.getGlobalSearch().size();
        int i = j;
        if (k != 0) {
          i = j + (k + 1);
        }
        return i;
      }
      return this.contacts.size();
    }
    
    public int getItemViewType(int paramInt)
    {
      int j = 1;
      int i = j;
      if (this.searching)
      {
        i = j;
        if (paramInt == this.searchResult.size()) {
          i = 0;
        }
      }
      return i;
    }
    
    public String getLetter(int paramInt)
    {
      if ((paramInt < 0) || (paramInt >= this.contacts.size())) {}
      TLRPC.User localUser;
      do
      {
        return null;
        localUser = (TLRPC.User)this.contacts.get(paramInt);
      } while (localUser == null);
      if (LocaleController.nameDisplayOrder == 1)
      {
        if (!TextUtils.isEmpty(localUser.first_name)) {
          return localUser.first_name.substring(0, 1).toUpperCase();
        }
        if (!TextUtils.isEmpty(localUser.last_name)) {
          return localUser.last_name.substring(0, 1).toUpperCase();
        }
      }
      else
      {
        if (!TextUtils.isEmpty(localUser.last_name)) {
          return localUser.last_name.substring(0, 1).toUpperCase();
        }
        if (!TextUtils.isEmpty(localUser.first_name)) {
          return localUser.first_name.substring(0, 1).toUpperCase();
        }
      }
      return "";
    }
    
    public int getPositionForScrollProgress(float paramFloat)
    {
      return (int)(getItemCount() * paramFloat);
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      GroupCreateUserCell localGroupCreateUserCell;
      Object localObject5;
      CharSequence localCharSequence;
      int i;
      int j;
      Object localObject3;
      Object localObject4;
      Object localObject1;
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        localGroupCreateUserCell = (GroupCreateUserCell)paramViewHolder.itemView;
        localObject5 = null;
        localCharSequence = null;
        if (this.searching)
        {
          i = this.searchResult.size();
          j = this.searchAdapterHelper.getGlobalSearch().size();
          if ((paramInt >= 0) && (paramInt < i))
          {
            paramViewHolder = (TLRPC.User)this.searchResult.get(paramInt);
            localObject3 = localCharSequence;
            localObject4 = paramViewHolder;
            localObject1 = localObject5;
            if (paramViewHolder != null)
            {
              if (paramInt >= i) {
                break label323;
              }
              localCharSequence = (CharSequence)this.searchResultNames.get(paramInt);
              localObject3 = localCharSequence;
              localObject4 = paramViewHolder;
              localObject1 = localObject5;
              if (localCharSequence != null)
              {
                localObject3 = localCharSequence;
                localObject4 = paramViewHolder;
                localObject1 = localObject5;
                if (!TextUtils.isEmpty(paramViewHolder.username))
                {
                  localObject3 = localCharSequence;
                  localObject4 = paramViewHolder;
                  localObject1 = localObject5;
                  if (localCharSequence.toString().startsWith("@" + paramViewHolder.username))
                  {
                    localObject1 = localCharSequence;
                    localObject3 = null;
                    localObject4 = paramViewHolder;
                  }
                }
              }
            }
            label213:
            localGroupCreateUserCell.setUser((TLRPC.User)localObject4, (CharSequence)localObject3, (CharSequence)localObject1);
            if (GroupCreateActivity.this.selectedContacts.indexOfKey(((TLRPC.User)localObject4).id) < 0) {
              break label537;
            }
          }
        }
        break;
      }
      label323:
      label488:
      label537:
      for (boolean bool = true;; bool = false)
      {
        localGroupCreateUserCell.setChecked(bool, false);
        do
        {
          return;
          paramViewHolder = (GroupCreateSectionCell)paramViewHolder.itemView;
        } while (!this.searching);
        paramViewHolder.setText(LocaleController.getString("GlobalSearch", 2131493628));
        return;
        if ((paramInt > i) && (paramInt <= j + i))
        {
          paramViewHolder = (TLRPC.User)this.searchAdapterHelper.getGlobalSearch().get(paramInt - i - 1);
          break;
        }
        paramViewHolder = null;
        break;
        localObject3 = localCharSequence;
        localObject4 = paramViewHolder;
        localObject1 = localObject5;
        if (paramInt <= i) {
          break label213;
        }
        localObject3 = localCharSequence;
        localObject4 = paramViewHolder;
        localObject1 = localObject5;
        if (TextUtils.isEmpty(paramViewHolder.username)) {
          break label213;
        }
        localObject3 = this.searchAdapterHelper.getLastFoundUsername();
        localObject1 = localObject3;
        if (((String)localObject3).startsWith("@")) {
          localObject1 = ((String)localObject3).substring(1);
        }
        try
        {
          localObject3 = new SpannableStringBuilder();
          ((SpannableStringBuilder)localObject3).append("@");
          ((SpannableStringBuilder)localObject3).append(paramViewHolder.username);
          paramInt = paramViewHolder.username.toLowerCase().indexOf((String)localObject1);
          if (paramInt != -1)
          {
            i = ((String)localObject1).length();
            if (paramInt != 0) {
              break label488;
            }
            i += 1;
          }
          for (;;)
          {
            ((SpannableStringBuilder)localObject3).setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt, paramInt + i, 33);
            localObject1 = localObject3;
            localObject3 = localCharSequence;
            localObject4 = paramViewHolder;
            break;
            paramInt += 1;
          }
        }
        catch (Exception localException)
        {
          localObject2 = paramViewHolder.username;
          localObject3 = localCharSequence;
          localObject4 = paramViewHolder;
        }
        localObject4 = (TLRPC.User)this.contacts.get(paramInt);
        localObject3 = localCharSequence;
        Object localObject2 = localObject5;
        break label213;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      }
      for (paramViewGroup = new GroupCreateUserCell(this.context, true);; paramViewGroup = new GroupCreateSectionCell(this.context)) {
        return new RecyclerListView.Holder(paramViewGroup);
      }
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof GroupCreateUserCell)) {
        ((GroupCreateUserCell)paramViewHolder.itemView).recycle();
      }
    }
    
    public void searchDialogs(final String paramString)
    {
      try
      {
        if (this.searchTimer != null) {
          this.searchTimer.cancel();
        }
        if (paramString == null)
        {
          this.searchResult.clear();
          this.searchResultNames.clear();
          this.searchAdapterHelper.queryServerSearch(null, true, false, false, false, 0, false);
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
              GroupCreateActivity.GroupCreateAdapter.this.searchTimer.cancel();
              GroupCreateActivity.GroupCreateAdapter.access$3002(GroupCreateActivity.GroupCreateAdapter.this, null);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  GroupCreateActivity.GroupCreateAdapter.this.searchAdapterHelper.queryServerSearch(GroupCreateActivity.GroupCreateAdapter.2.this.val$query, true, false, false, false, 0, false);
                  Utilities.searchQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      Object localObject = GroupCreateActivity.GroupCreateAdapter.2.this.val$query.trim().toLowerCase();
                      if (((String)localObject).length() == 0)
                      {
                        GroupCreateActivity.GroupCreateAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
                        return;
                      }
                      String str2 = LocaleController.getInstance().getTranslitString((String)localObject);
                      String str1;
                      if (!((String)localObject).equals(str2))
                      {
                        str1 = str2;
                        if (str2.length() != 0) {}
                      }
                      else
                      {
                        str1 = null;
                      }
                      int i;
                      String[] arrayOfString;
                      ArrayList localArrayList;
                      int j;
                      label141:
                      TLRPC.User localUser;
                      String str3;
                      int m;
                      int n;
                      int k;
                      if (str1 != null)
                      {
                        i = 1;
                        arrayOfString = new String[i + 1];
                        arrayOfString[0] = localObject;
                        if (str1 != null) {
                          arrayOfString[1] = str1;
                        }
                        localObject = new ArrayList();
                        localArrayList = new ArrayList();
                        j = 0;
                        if (j < GroupCreateActivity.GroupCreateAdapter.this.contacts.size())
                        {
                          localUser = (TLRPC.User)GroupCreateActivity.GroupCreateAdapter.this.contacts.get(j);
                          str3 = ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase();
                          str2 = LocaleController.getInstance().getTranslitString(str3);
                          str1 = str2;
                          if (str3.equals(str2)) {
                            str1 = null;
                          }
                          m = 0;
                          n = arrayOfString.length;
                          k = 0;
                        }
                      }
                      else
                      {
                        for (;;)
                        {
                          if (k < n)
                          {
                            str2 = arrayOfString[k];
                            if ((!str3.startsWith(str2)) && (!str3.contains(" " + str2)) && ((str1 == null) || ((!str1.startsWith(str2)) && (!str1.contains(" " + str2))))) {
                              break label383;
                            }
                            i = 1;
                            label333:
                            if (i == 0) {
                              break label471;
                            }
                            if (i != 1) {
                              break label415;
                            }
                            localArrayList.add(AndroidUtilities.generateSearchName(localUser.first_name, localUser.last_name, str2));
                          }
                          for (;;)
                          {
                            ((ArrayList)localObject).add(localUser);
                            j += 1;
                            break label141;
                            i = 0;
                            break;
                            label383:
                            i = m;
                            if (localUser.username == null) {
                              break label333;
                            }
                            i = m;
                            if (!localUser.username.startsWith(str2)) {
                              break label333;
                            }
                            i = 2;
                            break label333;
                            label415:
                            localArrayList.add(AndroidUtilities.generateSearchName("@" + localUser.username, null, "@" + str2));
                          }
                          label471:
                          k += 1;
                          m = i;
                        }
                      }
                      GroupCreateActivity.GroupCreateAdapter.this.updateSearchResults((ArrayList)localObject, localArrayList);
                    }
                  });
                }
              });
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
    
    public void setSearching(boolean paramBoolean)
    {
      if (this.searching == paramBoolean) {
        return;
      }
      this.searching = paramBoolean;
      notifyDataSetChanged();
    }
  }
  
  private class SpansContainer
    extends ViewGroup
  {
    private View addingSpan;
    private boolean animationStarted;
    private ArrayList<Animator> animators = new ArrayList();
    private AnimatorSet currentAnimation;
    private View removingSpan;
    
    public SpansContainer(Context paramContext)
    {
      super();
    }
    
    public void addSpan(GroupCreateSpan paramGroupCreateSpan)
    {
      GroupCreateActivity.this.allSpans.add(paramGroupCreateSpan);
      GroupCreateActivity.this.selectedContacts.put(paramGroupCreateSpan.getUid(), paramGroupCreateSpan);
      GroupCreateActivity.this.editText.setHintVisible(false);
      if (this.currentAnimation != null)
      {
        this.currentAnimation.setupEndValues();
        this.currentAnimation.cancel();
      }
      this.animationStarted = false;
      this.currentAnimation = new AnimatorSet();
      this.currentAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          GroupCreateActivity.SpansContainer.access$602(GroupCreateActivity.SpansContainer.this, null);
          GroupCreateActivity.SpansContainer.access$702(GroupCreateActivity.SpansContainer.this, null);
          GroupCreateActivity.SpansContainer.access$802(GroupCreateActivity.SpansContainer.this, false);
          GroupCreateActivity.this.editText.setAllowDrawCursor(true);
        }
      });
      this.currentAnimation.setDuration(150L);
      this.addingSpan = paramGroupCreateSpan;
      this.animators.clear();
      this.animators.add(ObjectAnimator.ofFloat(this.addingSpan, "scaleX", new float[] { 0.01F, 1.0F }));
      this.animators.add(ObjectAnimator.ofFloat(this.addingSpan, "scaleY", new float[] { 0.01F, 1.0F }));
      this.animators.add(ObjectAnimator.ofFloat(this.addingSpan, "alpha", new float[] { 0.0F, 1.0F }));
      addView(paramGroupCreateSpan);
    }
    
    protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      paramInt2 = getChildCount();
      paramInt1 = 0;
      while (paramInt1 < paramInt2)
      {
        View localView = getChildAt(paramInt1);
        localView.layout(0, 0, localView.getMeasuredWidth(), localView.getMeasuredHeight());
        paramInt1 += 1;
      }
    }
    
    protected void onMeasure(int paramInt1, int paramInt2)
    {
      int i3 = getChildCount();
      int i1 = View.MeasureSpec.getSize(paramInt1);
      int i2 = i1 - AndroidUtilities.dp(32.0F);
      int i = 0;
      paramInt2 = AndroidUtilities.dp(12.0F);
      int j = 0;
      paramInt1 = AndroidUtilities.dp(12.0F);
      int n = 0;
      int k;
      int m;
      while (n < i3)
      {
        View localView = getChildAt(n);
        if (!(localView instanceof GroupCreateSpan))
        {
          k = paramInt2;
          paramInt2 = paramInt1;
          n += 1;
          paramInt1 = paramInt2;
          paramInt2 = k;
        }
        else
        {
          localView.measure(View.MeasureSpec.makeMeasureSpec(i1, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32.0F), 1073741824));
          m = i;
          k = paramInt2;
          if (localView != this.removingSpan)
          {
            m = i;
            k = paramInt2;
            if (localView.getMeasuredWidth() + i > i2)
            {
              k = paramInt2 + (localView.getMeasuredHeight() + AndroidUtilities.dp(12.0F));
              m = 0;
            }
          }
          i = j;
          paramInt2 = paramInt1;
          if (localView.getMeasuredWidth() + j > i2)
          {
            paramInt2 = paramInt1 + (localView.getMeasuredHeight() + AndroidUtilities.dp(12.0F));
            i = 0;
          }
          paramInt1 = AndroidUtilities.dp(16.0F) + m;
          if (!this.animationStarted)
          {
            if (localView != this.removingSpan) {
              break label283;
            }
            localView.setTranslationX(AndroidUtilities.dp(16.0F) + i);
            localView.setTranslationY(paramInt2);
          }
          for (;;)
          {
            paramInt1 = m;
            if (localView != this.removingSpan) {
              paramInt1 = m + (localView.getMeasuredWidth() + AndroidUtilities.dp(9.0F));
            }
            j = i + (localView.getMeasuredWidth() + AndroidUtilities.dp(9.0F));
            i = paramInt1;
            break;
            label283:
            if (this.removingSpan != null)
            {
              if (localView.getTranslationX() != paramInt1) {
                this.animators.add(ObjectAnimator.ofFloat(localView, "translationX", new float[] { paramInt1 }));
              }
              if (localView.getTranslationY() != k) {
                this.animators.add(ObjectAnimator.ofFloat(localView, "translationY", new float[] { k }));
              }
            }
            else
            {
              localView.setTranslationX(paramInt1);
              localView.setTranslationY(k);
            }
          }
        }
      }
      if (AndroidUtilities.isTablet())
      {
        m = AndroidUtilities.dp(366.0F) / 3;
        n = i;
        k = paramInt2;
        if (i2 - i < m)
        {
          n = 0;
          k = paramInt2 + AndroidUtilities.dp(44.0F);
        }
        paramInt2 = paramInt1;
        if (i2 - j < m) {
          paramInt2 = paramInt1 + AndroidUtilities.dp(44.0F);
        }
        GroupCreateActivity.this.editText.measure(View.MeasureSpec.makeMeasureSpec(i2 - n, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32.0F), 1073741824));
        if (this.animationStarted) {
          break label776;
        }
        paramInt1 = AndroidUtilities.dp(44.0F);
        i = n + AndroidUtilities.dp(16.0F);
        GroupCreateActivity.access$102(GroupCreateActivity.this, k);
        if (this.currentAnimation == null) {
          break label732;
        }
        paramInt1 = k + AndroidUtilities.dp(44.0F);
        if (GroupCreateActivity.this.containerHeight != paramInt1) {
          this.animators.add(ObjectAnimator.ofInt(GroupCreateActivity.this, "containerHeight", new int[] { paramInt1 }));
        }
        if (GroupCreateActivity.this.editText.getTranslationX() != i) {
          this.animators.add(ObjectAnimator.ofFloat(GroupCreateActivity.this.editText, "translationX", new float[] { i }));
        }
        if (GroupCreateActivity.this.editText.getTranslationY() != GroupCreateActivity.this.fieldY) {
          this.animators.add(ObjectAnimator.ofFloat(GroupCreateActivity.this.editText, "translationY", new float[] { GroupCreateActivity.this.fieldY }));
        }
        GroupCreateActivity.this.editText.setAllowDrawCursor(false);
        this.currentAnimation.playTogether(this.animators);
        this.currentAnimation.start();
        this.animationStarted = true;
      }
      for (;;)
      {
        setMeasuredDimension(i1, GroupCreateActivity.this.containerHeight);
        return;
        m = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(164.0F)) / 3;
        break;
        label732:
        GroupCreateActivity.access$202(GroupCreateActivity.this, paramInt2 + paramInt1);
        GroupCreateActivity.this.editText.setTranslationX(i);
        GroupCreateActivity.this.editText.setTranslationY(GroupCreateActivity.this.fieldY);
        continue;
        label776:
        if ((this.currentAnimation != null) && (!GroupCreateActivity.this.ignoreScrollEvent) && (this.removingSpan == null)) {
          GroupCreateActivity.this.editText.bringPointIntoView(GroupCreateActivity.this.editText.getSelectionStart());
        }
      }
    }
    
    public void removeSpan(final GroupCreateSpan paramGroupCreateSpan)
    {
      GroupCreateActivity.access$302(GroupCreateActivity.this, true);
      GroupCreateActivity.this.selectedContacts.remove(paramGroupCreateSpan.getUid());
      GroupCreateActivity.this.allSpans.remove(paramGroupCreateSpan);
      paramGroupCreateSpan.setOnClickListener(null);
      if (this.currentAnimation != null)
      {
        this.currentAnimation.setupEndValues();
        this.currentAnimation.cancel();
      }
      this.animationStarted = false;
      this.currentAnimation = new AnimatorSet();
      this.currentAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          GroupCreateActivity.SpansContainer.this.removeView(paramGroupCreateSpan);
          GroupCreateActivity.SpansContainer.access$902(GroupCreateActivity.SpansContainer.this, null);
          GroupCreateActivity.SpansContainer.access$702(GroupCreateActivity.SpansContainer.this, null);
          GroupCreateActivity.SpansContainer.access$802(GroupCreateActivity.SpansContainer.this, false);
          GroupCreateActivity.this.editText.setAllowDrawCursor(true);
          if (GroupCreateActivity.this.allSpans.isEmpty()) {
            GroupCreateActivity.this.editText.setHintVisible(true);
          }
        }
      });
      this.currentAnimation.setDuration(150L);
      this.removingSpan = paramGroupCreateSpan;
      this.animators.clear();
      this.animators.add(ObjectAnimator.ofFloat(this.removingSpan, "scaleX", new float[] { 1.0F, 0.01F }));
      this.animators.add(ObjectAnimator.ofFloat(this.removingSpan, "scaleY", new float[] { 1.0F, 0.01F }));
      this.animators.add(ObjectAnimator.ofFloat(this.removingSpan, "alpha", new float[] { 1.0F, 0.0F }));
      requestLayout();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/GroupCreateActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */