package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.GroupCreateSectionCell;
import org.telegram.ui.Cells.InviteTextCell;
import org.telegram.ui.Cells.InviteUserCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.GroupCreateDividerItemDecoration;
import org.telegram.ui.Components.GroupCreateSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class InviteContactsActivity
  extends BaseFragment
  implements View.OnClickListener, NotificationCenter.NotificationCenterDelegate
{
  private InviteAdapter adapter;
  private ArrayList<GroupCreateSpan> allSpans = new ArrayList();
  private int containerHeight;
  private TextView counterTextView;
  private FrameLayout counterView;
  private GroupCreateSpan currentDeletingSpan;
  private GroupCreateDividerItemDecoration decoration;
  private EditTextBoldCursor editText;
  private EmptyTextProgressView emptyView;
  private int fieldY;
  private boolean ignoreScrollEvent;
  private TextView infoTextView;
  private RecyclerListView listView;
  private ArrayList<ContactsController.Contact> phoneBookContacts;
  private ScrollView scrollView;
  private boolean searchWas;
  private boolean searching;
  private HashMap<String, GroupCreateSpan> selectedContacts = new HashMap();
  private SpansContainer spansContainer;
  private TextView textView;
  
  private void checkVisibleRows()
  {
    int j = this.listView.getChildCount();
    int i = 0;
    while (i < j)
    {
      Object localObject = this.listView.getChildAt(i);
      if ((localObject instanceof InviteUserCell))
      {
        localObject = (InviteUserCell)localObject;
        ContactsController.Contact localContact = ((InviteUserCell)localObject).getContact();
        if (localContact != null) {
          ((InviteUserCell)localObject).setChecked(this.selectedContacts.containsKey(localContact.key), true);
        }
      }
      i += 1;
    }
  }
  
  private void closeSearch()
  {
    this.searching = false;
    this.searchWas = false;
    this.adapter.setSearching(false);
    this.adapter.searchDialogs(null);
    this.listView.setFastScrollVisible(true);
    this.listView.setVerticalScrollBarEnabled(false);
    this.emptyView.setText(LocaleController.getString("NoContacts", 2131493887));
  }
  
  private void fetchContacts()
  {
    this.phoneBookContacts = new ArrayList(ContactsController.getInstance(this.currentAccount).phoneBookContacts);
    Collections.sort(this.phoneBookContacts, new Comparator()
    {
      public int compare(ContactsController.Contact paramAnonymousContact1, ContactsController.Contact paramAnonymousContact2)
      {
        if (paramAnonymousContact1.imported > paramAnonymousContact2.imported) {
          return -1;
        }
        if (paramAnonymousContact1.imported < paramAnonymousContact2.imported) {
          return 1;
        }
        return 0;
      }
    });
    if (this.emptyView != null) {
      this.emptyView.showTextView();
    }
    if (this.adapter != null) {
      this.adapter.notifyDataSetChanged();
    }
  }
  
  private void updateHint()
  {
    if (this.selectedContacts.isEmpty())
    {
      this.infoTextView.setVisibility(0);
      this.counterView.setVisibility(4);
      return;
    }
    this.infoTextView.setVisibility(4);
    this.counterView.setVisibility(0);
    this.counterTextView.setText(String.format("%d", new Object[] { Integer.valueOf(this.selectedContacts.size()) }));
  }
  
  public View createView(Context paramContext)
  {
    this.searching = false;
    this.searchWas = false;
    this.allSpans.clear();
    this.selectedContacts.clear();
    this.currentDeletingSpan = null;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("InviteFriends", 2131493685));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          InviteContactsActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new ViewGroup(paramContext)
    {
      protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
      {
        boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
        if ((paramAnonymousView == InviteContactsActivity.this.listView) || (paramAnonymousView == InviteContactsActivity.this.emptyView)) {
          InviteContactsActivity.this.parentLayout.drawHeaderShadow(paramAnonymousCanvas, InviteContactsActivity.this.scrollView.getMeasuredHeight());
        }
        return bool;
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        InviteContactsActivity.this.scrollView.layout(0, 0, InviteContactsActivity.this.scrollView.getMeasuredWidth(), InviteContactsActivity.this.scrollView.getMeasuredHeight());
        InviteContactsActivity.this.listView.layout(0, InviteContactsActivity.this.scrollView.getMeasuredHeight(), InviteContactsActivity.this.listView.getMeasuredWidth(), InviteContactsActivity.this.scrollView.getMeasuredHeight() + InviteContactsActivity.this.listView.getMeasuredHeight());
        InviteContactsActivity.this.emptyView.layout(0, InviteContactsActivity.this.scrollView.getMeasuredHeight() + AndroidUtilities.dp(72.0F), InviteContactsActivity.this.emptyView.getMeasuredWidth(), InviteContactsActivity.this.scrollView.getMeasuredHeight() + InviteContactsActivity.this.emptyView.getMeasuredHeight());
        paramAnonymousInt1 = paramAnonymousInt4 - paramAnonymousInt2 - InviteContactsActivity.this.infoTextView.getMeasuredHeight();
        InviteContactsActivity.this.infoTextView.layout(0, paramAnonymousInt1, InviteContactsActivity.this.infoTextView.getMeasuredWidth(), InviteContactsActivity.this.infoTextView.getMeasuredHeight() + paramAnonymousInt1);
        paramAnonymousInt1 = paramAnonymousInt4 - paramAnonymousInt2 - InviteContactsActivity.this.counterView.getMeasuredHeight();
        InviteContactsActivity.this.counterView.layout(0, paramAnonymousInt1, InviteContactsActivity.this.counterView.getMeasuredWidth(), InviteContactsActivity.this.counterView.getMeasuredHeight() + paramAnonymousInt1);
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        int i = View.MeasureSpec.getSize(paramAnonymousInt1);
        int j = View.MeasureSpec.getSize(paramAnonymousInt2);
        setMeasuredDimension(i, j);
        if ((AndroidUtilities.isTablet()) || (j > i))
        {
          paramAnonymousInt1 = AndroidUtilities.dp(144.0F);
          InviteContactsActivity.this.infoTextView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, Integer.MIN_VALUE));
          InviteContactsActivity.this.counterView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
          if (InviteContactsActivity.this.infoTextView.getVisibility() != 0) {
            break label216;
          }
        }
        label216:
        for (paramAnonymousInt2 = InviteContactsActivity.this.infoTextView.getMeasuredHeight();; paramAnonymousInt2 = InviteContactsActivity.this.counterView.getMeasuredHeight())
        {
          InviteContactsActivity.this.scrollView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, Integer.MIN_VALUE));
          InviteContactsActivity.this.listView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j - InviteContactsActivity.this.scrollView.getMeasuredHeight() - paramAnonymousInt2, 1073741824));
          InviteContactsActivity.this.emptyView.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j - InviteContactsActivity.this.scrollView.getMeasuredHeight() - AndroidUtilities.dp(72.0F), 1073741824));
          return;
          paramAnonymousInt1 = AndroidUtilities.dp(56.0F);
          break;
        }
      }
    };
    Object localObject1 = (ViewGroup)this.fragmentView;
    this.scrollView = new ScrollView(paramContext)
    {
      public boolean requestChildRectangleOnScreen(View paramAnonymousView, Rect paramAnonymousRect, boolean paramAnonymousBoolean)
      {
        if (InviteContactsActivity.this.ignoreScrollEvent)
        {
          InviteContactsActivity.access$302(InviteContactsActivity.this, false);
          return false;
        }
        paramAnonymousRect.offset(paramAnonymousView.getLeft() - paramAnonymousView.getScrollX(), paramAnonymousView.getTop() - paramAnonymousView.getScrollY());
        paramAnonymousRect.top += InviteContactsActivity.this.fieldY + AndroidUtilities.dp(20.0F);
        paramAnonymousRect.bottom += InviteContactsActivity.this.fieldY + AndroidUtilities.dp(50.0F);
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
        if (InviteContactsActivity.this.currentDeletingSpan != null)
        {
          InviteContactsActivity.this.currentDeletingSpan.cancelDeleteAnimation();
          InviteContactsActivity.access$1602(InviteContactsActivity.this, null);
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
    Object localObject2 = this.editText;
    label443:
    Object localObject3;
    if (LocaleController.isRTL)
    {
      i = 5;
      ((EditTextBoldCursor)localObject2).setGravity(i | 0x10);
      this.spansContainer.addView(this.editText);
      this.editText.setHintText(LocaleController.getString("SearchFriends", 2131494299));
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
      this.editText.setOnKeyListener(new View.OnKeyListener()
      {
        private boolean wasEmpty;
        
        public boolean onKey(View paramAnonymousView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if (paramAnonymousKeyEvent.getAction() == 0) {
            if (InviteContactsActivity.this.editText.length() == 0)
            {
              bool = true;
              this.wasEmpty = bool;
            }
          }
          while ((paramAnonymousKeyEvent.getAction() != 1) || (!this.wasEmpty) || (InviteContactsActivity.this.allSpans.isEmpty())) {
            for (;;)
            {
              return false;
              boolean bool = false;
            }
          }
          InviteContactsActivity.this.spansContainer.removeSpan((GroupCreateSpan)InviteContactsActivity.this.allSpans.get(InviteContactsActivity.this.allSpans.size() - 1));
          InviteContactsActivity.this.updateHint();
          InviteContactsActivity.this.checkVisibleRows();
          return true;
        }
      });
      this.editText.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable)
        {
          if (InviteContactsActivity.this.editText.length() != 0)
          {
            InviteContactsActivity.access$2002(InviteContactsActivity.this, true);
            InviteContactsActivity.access$2102(InviteContactsActivity.this, true);
            InviteContactsActivity.this.adapter.setSearching(true);
            InviteContactsActivity.this.adapter.searchDialogs(InviteContactsActivity.this.editText.getText().toString());
            InviteContactsActivity.this.listView.setFastScrollVisible(false);
            InviteContactsActivity.this.listView.setVerticalScrollBarEnabled(true);
            InviteContactsActivity.this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
            return;
          }
          InviteContactsActivity.this.closeSearch();
        }
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      });
      this.emptyView = new EmptyTextProgressView(paramContext);
      if (!ContactsController.getInstance(this.currentAccount).isLoadingContacts()) {
        break label1123;
      }
      this.emptyView.showProgress();
      this.emptyView.setText(LocaleController.getString("NoContacts", 2131493887));
      ((ViewGroup)localObject1).addView(this.emptyView);
      localObject2 = new LinearLayoutManager(paramContext, 1, false);
      this.listView = new RecyclerListView(paramContext);
      this.listView.setEmptyView(this.emptyView);
      localObject3 = this.listView;
      InviteAdapter localInviteAdapter = new InviteAdapter(paramContext);
      this.adapter = localInviteAdapter;
      ((RecyclerListView)localObject3).setAdapter(localInviteAdapter);
      this.listView.setLayoutManager((RecyclerView.LayoutManager)localObject2);
      this.listView.setVerticalScrollBarEnabled(true);
      localObject2 = this.listView;
      if (!LocaleController.isRTL) {
        break label1133;
      }
    }
    label1123:
    label1133:
    for (int i = 1;; i = 2)
    {
      ((RecyclerListView)localObject2).setVerticalScrollbarPosition(i);
      localObject2 = this.listView;
      localObject3 = new GroupCreateDividerItemDecoration();
      this.decoration = ((GroupCreateDividerItemDecoration)localObject3);
      ((RecyclerListView)localObject2).addItemDecoration((RecyclerView.ItemDecoration)localObject3);
      ((ViewGroup)localObject1).addView(this.listView);
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool1 = false;
          if ((paramAnonymousInt == 0) && (!InviteContactsActivity.this.searching)) {}
          label268:
          label282:
          for (;;)
          {
            Object localObject;
            try
            {
              paramAnonymousView = new Intent("android.intent.action.SEND");
              paramAnonymousView.setType("text/plain");
              localObject = ContactsController.getInstance(InviteContactsActivity.this.currentAccount).getInviteText(0);
              paramAnonymousView.putExtra("android.intent.extra.TEXT", (String)localObject);
              InviteContactsActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(paramAnonymousView, (CharSequence)localObject), 500);
              return;
            }
            catch (Exception paramAnonymousView)
            {
              FileLog.e(paramAnonymousView);
              return;
            }
            if ((paramAnonymousView instanceof InviteUserCell))
            {
              paramAnonymousView = (InviteUserCell)paramAnonymousView;
              localObject = paramAnonymousView.getContact();
              if (localObject != null)
              {
                boolean bool2 = InviteContactsActivity.this.selectedContacts.containsKey(((ContactsController.Contact)localObject).key);
                if (bool2)
                {
                  localObject = (GroupCreateSpan)InviteContactsActivity.this.selectedContacts.get(((ContactsController.Contact)localObject).key);
                  InviteContactsActivity.this.spansContainer.removeSpan((GroupCreateSpan)localObject);
                  InviteContactsActivity.this.updateHint();
                  if ((!InviteContactsActivity.this.searching) && (!InviteContactsActivity.this.searchWas)) {
                    break label268;
                  }
                  AndroidUtilities.showKeyboard(InviteContactsActivity.this.editText);
                }
                for (;;)
                {
                  if (InviteContactsActivity.this.editText.length() <= 0) {
                    break label282;
                  }
                  InviteContactsActivity.this.editText.setText(null);
                  return;
                  localObject = new GroupCreateSpan(InviteContactsActivity.this.editText.getContext(), (ContactsController.Contact)localObject);
                  InviteContactsActivity.this.spansContainer.addSpan((GroupCreateSpan)localObject);
                  ((GroupCreateSpan)localObject).setOnClickListener(InviteContactsActivity.this);
                  break;
                  if (!bool2) {
                    bool1 = true;
                  }
                  paramAnonymousView.setChecked(bool1, true);
                }
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
            AndroidUtilities.hideKeyboard(InviteContactsActivity.this.editText);
          }
        }
      });
      this.infoTextView = new TextView(paramContext);
      this.infoTextView.setBackgroundColor(Theme.getColor("contacts_inviteBackground"));
      this.infoTextView.setTextColor(Theme.getColor("contacts_inviteText"));
      this.infoTextView.setGravity(17);
      this.infoTextView.setText(LocaleController.getString("InviteFriendsHelp", 2131493686));
      this.infoTextView.setTextSize(1, 13.0F);
      this.infoTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.infoTextView.setPadding(AndroidUtilities.dp(17.0F), AndroidUtilities.dp(9.0F), AndroidUtilities.dp(17.0F), AndroidUtilities.dp(9.0F));
      ((ViewGroup)localObject1).addView(this.infoTextView, LayoutHelper.createFrame(-1, -2, 83));
      this.counterView = new FrameLayout(paramContext);
      this.counterView.setBackgroundColor(Theme.getColor("contacts_inviteBackground"));
      this.counterView.setVisibility(4);
      ((ViewGroup)localObject1).addView(this.counterView, LayoutHelper.createFrame(-1, 48, 83));
      this.counterView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          try
          {
            paramAnonymousView = new StringBuilder();
            j = 0;
            i = 0;
          }
          catch (Exception paramAnonymousView)
          {
            for (;;)
            {
              int i;
              ContactsController.Contact localContact;
              int k;
              FileLog.e(paramAnonymousView);
              continue;
              i += 1;
              int j = k;
            }
          }
          if (i < InviteContactsActivity.this.allSpans.size())
          {
            localContact = ((GroupCreateSpan)InviteContactsActivity.this.allSpans.get(i)).getContact();
            if (paramAnonymousView.length() != 0) {
              paramAnonymousView.append(';');
            }
            paramAnonymousView.append((String)localContact.phones.get(0));
            k = j;
            if (i == 0)
            {
              k = j;
              if (InviteContactsActivity.this.allSpans.size() == 1) {
                k = localContact.imported;
              }
            }
          }
          else
          {
            paramAnonymousView = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:" + paramAnonymousView.toString()));
            paramAnonymousView.putExtra("sms_body", ContactsController.getInstance(InviteContactsActivity.this.currentAccount).getInviteText(j));
            InviteContactsActivity.this.getParentActivity().startActivityForResult(paramAnonymousView, 500);
            MediaController.getInstance().startSmsObserver();
            InviteContactsActivity.this.finishFragment();
            return;
          }
        }
      });
      localObject1 = new LinearLayout(paramContext);
      ((LinearLayout)localObject1).setOrientation(0);
      this.counterView.addView((View)localObject1, LayoutHelper.createFrame(-2, -1, 17));
      this.counterTextView = new TextView(paramContext);
      this.counterTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.counterTextView.setTextSize(1, 14.0F);
      this.counterTextView.setTextColor(Theme.getColor("contacts_inviteBackground"));
      this.counterTextView.setGravity(17);
      this.counterTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(10.0F), -1));
      this.counterTextView.setMinWidth(AndroidUtilities.dp(20.0F));
      this.counterTextView.setPadding(AndroidUtilities.dp(6.0F), 0, AndroidUtilities.dp(6.0F), AndroidUtilities.dp(1.0F));
      ((LinearLayout)localObject1).addView(this.counterTextView, LayoutHelper.createLinear(-2, 20, 16, 0, 0, 10, 0));
      this.textView = new TextView(paramContext);
      this.textView.setTextSize(1, 14.0F);
      this.textView.setTextColor(Theme.getColor("contacts_inviteText"));
      this.textView.setGravity(17);
      this.textView.setCompoundDrawablePadding(AndroidUtilities.dp(8.0F));
      this.textView.setText(LocaleController.getString("InviteToTelegram", 2131493698).toUpperCase());
      this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      ((LinearLayout)localObject1).addView(this.textView, LayoutHelper.createLinear(-2, -2, 16));
      updateHint();
      this.adapter.notifyDataSetChanged();
      return this.fragmentView;
      i = 3;
      break;
      this.emptyView.showTextView();
      break label443;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.contactsImported) {
      fetchContacts();
    }
  }
  
  public int getContainerHeight()
  {
    return this.containerHeight;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local12 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (InviteContactsActivity.this.listView != null)
        {
          int j = InviteContactsActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = InviteContactsActivity.this.listView.getChildAt(i);
            if ((localView instanceof InviteUserCell)) {
              ((InviteUserCell)localView).update(0);
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
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { InviteUserCell.class }, new String[] { "textView" }, null, null, null, "groupcreate_sectionText");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { InviteUserCell.class }, new String[] { "checkBox" }, null, null, null, "groupcreate_checkbox");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { InviteUserCell.class }, new String[] { "checkBox" }, null, null, null, "groupcreate_checkboxCheck");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { InviteUserCell.class }, new String[] { "statusTextView" }, null, null, null, "groupcreate_onlineText");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { InviteUserCell.class }, new String[] { "statusTextView" }, null, null, null, "groupcreate_offlineText");
    RecyclerListView localRecyclerListView = this.listView;
    Drawable localDrawable1 = Theme.avatar_photoDrawable;
    Drawable localDrawable2 = Theme.avatar_broadcastDrawable;
    Drawable localDrawable3 = Theme.avatar_savedDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localObject1, localObject2, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, new ThemeDescription(localRecyclerListView, 0, new Class[] { InviteUserCell.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local12, "avatar_backgroundPink"), new ThemeDescription(this.listView, 0, new Class[] { InviteTextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { InviteTextCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "avatar_backgroundGroupCreateSpanBlue"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "groupcreate_spanBackground"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "groupcreate_spanText"), new ThemeDescription(this.spansContainer, 0, new Class[] { GroupCreateSpan.class }, null, null, null, "avatar_backgroundBlue"), new ThemeDescription(this.infoTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "contacts_inviteText"), new ThemeDescription(this.infoTextView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "contacts_inviteBackground"), new ThemeDescription(this.counterView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "contacts_inviteBackground"), new ThemeDescription(this.counterTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "contacts_inviteBackground"), new ThemeDescription(this.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "contacts_inviteText") };
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
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsImported);
    fetchContacts();
    if (!UserConfig.getInstance(this.currentAccount).contactsReimported)
    {
      ContactsController.getInstance(this.currentAccount).forceImportContacts();
      UserConfig.getInstance(this.currentAccount).contactsReimported = true;
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
    }
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsImported);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.editText != null) {
      this.editText.requestFocus();
    }
  }
  
  public void setContainerHeight(int paramInt)
  {
    this.containerHeight = paramInt;
    if (this.spansContainer != null) {
      this.spansContainer.requestLayout();
    }
  }
  
  public class InviteAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context context;
    private ArrayList<ContactsController.Contact> searchResult = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Timer searchTimer;
    private boolean searching;
    
    public InviteAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    private void updateSearchResults(final ArrayList<ContactsController.Contact> paramArrayList, final ArrayList<CharSequence> paramArrayList1)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          InviteContactsActivity.InviteAdapter.access$2902(InviteContactsActivity.InviteAdapter.this, paramArrayList);
          InviteContactsActivity.InviteAdapter.access$3002(InviteContactsActivity.InviteAdapter.this, paramArrayList1);
          InviteContactsActivity.InviteAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public int getItemCount()
    {
      if (this.searching) {
        return this.searchResult.size();
      }
      return InviteContactsActivity.this.phoneBookContacts.size() + 1;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((!this.searching) && (paramInt == 0)) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void notifyDataSetChanged()
    {
      boolean bool = false;
      super.notifyDataSetChanged();
      int j = getItemCount();
      Object localObject = InviteContactsActivity.this.emptyView;
      if (j == 1) {}
      for (int i = 0;; i = 4)
      {
        ((EmptyTextProgressView)localObject).setVisibility(i);
        localObject = InviteContactsActivity.this.decoration;
        if (j == 1) {
          bool = true;
        }
        ((GroupCreateDividerItemDecoration)localObject).setSingle(bool);
        return;
      }
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      }
      InviteUserCell localInviteUserCell = (InviteUserCell)paramViewHolder.itemView;
      if (this.searching) {
        paramViewHolder = (ContactsController.Contact)this.searchResult.get(paramInt);
      }
      for (CharSequence localCharSequence = (CharSequence)this.searchResultNames.get(paramInt);; localCharSequence = null)
      {
        localInviteUserCell.setUser(paramViewHolder, localCharSequence);
        localInviteUserCell.setChecked(InviteContactsActivity.this.selectedContacts.containsKey(paramViewHolder.key), false);
        return;
        paramViewHolder = (ContactsController.Contact)InviteContactsActivity.this.phoneBookContacts.get(paramInt - 1);
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new InviteUserCell(this.context, true);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new InviteTextCell(this.context);
        ((InviteTextCell)paramViewGroup).setTextAndIcon(LocaleController.getString("ShareTelegram", 2131494388), 2131165637);
      }
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof InviteUserCell)) {
        ((InviteUserCell)paramViewHolder.itemView).recycle();
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
              InviteContactsActivity.InviteAdapter.this.searchTimer.cancel();
              InviteContactsActivity.InviteAdapter.access$2702(InviteContactsActivity.InviteAdapter.this, null);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  Utilities.searchQueue.postRunnable(new Runnable()
                  {
                    public void run()
                    {
                      Object localObject = InviteContactsActivity.InviteAdapter.1.this.val$query.trim().toLowerCase();
                      if (((String)localObject).length() == 0)
                      {
                        InviteContactsActivity.InviteAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
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
                      label141:
                      ContactsController.Contact localContact;
                      String str3;
                      int k;
                      int n;
                      int j;
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
                        i = 0;
                        if (i < InviteContactsActivity.this.phoneBookContacts.size())
                        {
                          localContact = (ContactsController.Contact)InviteContactsActivity.this.phoneBookContacts.get(i);
                          str3 = ContactsController.formatName(localContact.first_name, localContact.last_name).toLowerCase();
                          str2 = LocaleController.getInstance().getTranslitString(str3);
                          str1 = str2;
                          if (str3.equals(str2)) {
                            str1 = null;
                          }
                          k = 0;
                          n = arrayOfString.length;
                          j = 0;
                        }
                      }
                      else
                      {
                        for (;;)
                        {
                          int m;
                          if (j < n)
                          {
                            str2 = arrayOfString[j];
                            if ((!str3.startsWith(str2)) && (!str3.contains(" " + str2)))
                            {
                              m = k;
                              if (str1 == null) {
                                break label345;
                              }
                              if (!str1.startsWith(str2))
                              {
                                m = k;
                                if (!str1.contains(" " + str2)) {
                                  break label345;
                                }
                              }
                            }
                            m = 1;
                            label345:
                            if (m != 0)
                            {
                              localArrayList.add(AndroidUtilities.generateSearchName(localContact.first_name, localContact.last_name, str2));
                              ((ArrayList)localObject).add(localContact);
                            }
                          }
                          else
                          {
                            i += 1;
                            break label141;
                            i = 0;
                            break;
                          }
                          j += 1;
                          k = m;
                        }
                      }
                      InviteContactsActivity.InviteAdapter.this.updateSearchResults((ArrayList)localObject, localArrayList);
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
      InviteContactsActivity.this.allSpans.add(paramGroupCreateSpan);
      InviteContactsActivity.this.selectedContacts.put(paramGroupCreateSpan.getKey(), paramGroupCreateSpan);
      InviteContactsActivity.this.editText.setHintVisible(false);
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
          InviteContactsActivity.SpansContainer.access$602(InviteContactsActivity.SpansContainer.this, null);
          InviteContactsActivity.SpansContainer.access$702(InviteContactsActivity.SpansContainer.this, null);
          InviteContactsActivity.SpansContainer.access$802(InviteContactsActivity.SpansContainer.this, false);
          InviteContactsActivity.this.editText.setAllowDrawCursor(true);
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
        InviteContactsActivity.this.editText.measure(View.MeasureSpec.makeMeasureSpec(i2 - n, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(32.0F), 1073741824));
        if (this.animationStarted) {
          break label777;
        }
        paramInt1 = AndroidUtilities.dp(44.0F);
        i = n + AndroidUtilities.dp(16.0F);
        InviteContactsActivity.access$102(InviteContactsActivity.this, k);
        if (this.currentAnimation == null) {
          break label733;
        }
        paramInt1 = k + AndroidUtilities.dp(44.0F);
        if (InviteContactsActivity.this.containerHeight != paramInt1) {
          this.animators.add(ObjectAnimator.ofInt(InviteContactsActivity.this, "containerHeight", new int[] { paramInt1 }));
        }
        if (InviteContactsActivity.this.editText.getTranslationX() != i) {
          this.animators.add(ObjectAnimator.ofFloat(InviteContactsActivity.this.editText, "translationX", new float[] { i }));
        }
        if (InviteContactsActivity.this.editText.getTranslationY() != InviteContactsActivity.this.fieldY) {
          this.animators.add(ObjectAnimator.ofFloat(InviteContactsActivity.this.editText, "translationY", new float[] { InviteContactsActivity.this.fieldY }));
        }
        InviteContactsActivity.this.editText.setAllowDrawCursor(false);
        this.currentAnimation.playTogether(this.animators);
        this.currentAnimation.start();
        this.animationStarted = true;
      }
      for (;;)
      {
        setMeasuredDimension(i1, InviteContactsActivity.this.containerHeight);
        return;
        m = (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(164.0F)) / 3;
        break;
        label733:
        InviteContactsActivity.access$202(InviteContactsActivity.this, paramInt2 + paramInt1);
        InviteContactsActivity.this.editText.setTranslationX(i);
        InviteContactsActivity.this.editText.setTranslationY(InviteContactsActivity.this.fieldY);
        continue;
        label777:
        if ((this.currentAnimation != null) && (!InviteContactsActivity.this.ignoreScrollEvent) && (this.removingSpan == null)) {
          InviteContactsActivity.this.editText.bringPointIntoView(InviteContactsActivity.this.editText.getSelectionStart());
        }
      }
    }
    
    public void removeSpan(final GroupCreateSpan paramGroupCreateSpan)
    {
      InviteContactsActivity.access$302(InviteContactsActivity.this, true);
      InviteContactsActivity.this.selectedContacts.remove(paramGroupCreateSpan.getKey());
      InviteContactsActivity.this.allSpans.remove(paramGroupCreateSpan);
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
          InviteContactsActivity.SpansContainer.this.removeView(paramGroupCreateSpan);
          InviteContactsActivity.SpansContainer.access$902(InviteContactsActivity.SpansContainer.this, null);
          InviteContactsActivity.SpansContainer.access$702(InviteContactsActivity.SpansContainer.this, null);
          InviteContactsActivity.SpansContainer.access$802(InviteContactsActivity.SpansContainer.this, false);
          InviteContactsActivity.this.editText.setAllowDrawCursor(true);
          if (InviteContactsActivity.this.allSpans.isEmpty()) {
            InviteContactsActivity.this.editText.setHintVisible(true);
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/InviteContactsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */