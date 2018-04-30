package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
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
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.lang.reflect.Method;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.CloseProgressDrawable2;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class ActionBarMenuItem
  extends FrameLayout
{
  private static Method layoutInScreenMethod;
  private int additionalOffset;
  private boolean allowCloseAnimation = true;
  private boolean animationEnabled = true;
  private ImageView clearButton;
  private ActionBarMenuItemDelegate delegate;
  protected ImageView iconView;
  private boolean ignoreOnTextChange;
  private boolean isSearchField;
  private boolean layoutInScreen;
  private ActionBarMenuItemSearchListener listener;
  private int[] location;
  private boolean longClickEnabled = true;
  protected boolean overrideMenuClick;
  private ActionBarMenu parentMenu;
  private ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout;
  private ActionBarPopupWindow popupWindow;
  private boolean processedPopupClick;
  private CloseProgressDrawable2 progressDrawable;
  private Rect rect;
  private FrameLayout searchContainer;
  private EditTextBoldCursor searchField;
  private TextView searchFieldCaption;
  private View selectedMenuView;
  private Runnable showMenuRunnable;
  private int subMenuOpenSide;
  
  public ActionBarMenuItem(Context paramContext, ActionBarMenu paramActionBarMenu, int paramInt1, int paramInt2)
  {
    super(paramContext);
    if (paramInt1 != 0) {
      setBackgroundDrawable(Theme.createSelectorDrawable(paramInt1));
    }
    this.parentMenu = paramActionBarMenu;
    this.iconView = new ImageView(paramContext);
    this.iconView.setScaleType(ImageView.ScaleType.CENTER);
    addView(this.iconView, LayoutHelper.createFrame(-1, -1.0F));
    if (paramInt2 != 0) {
      this.iconView.setColorFilter(new PorterDuffColorFilter(paramInt2, PorterDuff.Mode.MULTIPLY));
    }
  }
  
  private void createPopupLayout()
  {
    if (this.popupLayout != null) {
      return;
    }
    this.rect = new Rect();
    this.location = new int[2];
    this.popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext());
    this.popupLayout.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        if ((paramAnonymousMotionEvent.getActionMasked() == 0) && (ActionBarMenuItem.this.popupWindow != null) && (ActionBarMenuItem.this.popupWindow.isShowing()))
        {
          paramAnonymousView.getHitRect(ActionBarMenuItem.this.rect);
          if (!ActionBarMenuItem.this.rect.contains((int)paramAnonymousMotionEvent.getX(), (int)paramAnonymousMotionEvent.getY())) {
            ActionBarMenuItem.this.popupWindow.dismiss();
          }
        }
        return false;
      }
    });
    this.popupLayout.setDispatchKeyEventListener(new ActionBarPopupWindow.OnDispatchKeyEventListener()
    {
      public void onDispatchKeyEvent(KeyEvent paramAnonymousKeyEvent)
      {
        if ((paramAnonymousKeyEvent.getKeyCode() == 4) && (paramAnonymousKeyEvent.getRepeatCount() == 0) && (ActionBarMenuItem.this.popupWindow != null) && (ActionBarMenuItem.this.popupWindow.isShowing())) {
          ActionBarMenuItem.this.popupWindow.dismiss();
        }
      }
    });
  }
  
  private void updateOrShowPopup(boolean paramBoolean1, boolean paramBoolean2)
  {
    int i;
    Object localObject;
    if (this.parentMenu != null)
    {
      i = -this.parentMenu.parentActionBar.getMeasuredHeight() + this.parentMenu.getTop();
      if (paramBoolean1) {
        this.popupLayout.scrollToTop();
      }
      if (this.parentMenu == null) {
        break label255;
      }
      localObject = this.parentMenu.parentActionBar;
      if (this.subMenuOpenSide != 0) {
        break label188;
      }
      if (paramBoolean1) {
        this.popupWindow.showAsDropDown((View)localObject, getLeft() + this.parentMenu.getLeft() + getMeasuredWidth() - this.popupLayout.getMeasuredWidth() + (int)getTranslationX(), i);
      }
      if (paramBoolean2) {
        this.popupWindow.update((View)localObject, getLeft() + this.parentMenu.getLeft() + getMeasuredWidth() - this.popupLayout.getMeasuredWidth() + (int)getTranslationX(), i, -1, -1);
      }
    }
    label188:
    label255:
    label345:
    do
    {
      do
      {
        do
        {
          do
          {
            return;
            float f = getScaleY();
            i = -(int)(getMeasuredHeight() * f - getTranslationY() / f) + this.additionalOffset;
            break;
            if (paramBoolean1) {
              this.popupWindow.showAsDropDown((View)localObject, getLeft() - AndroidUtilities.dp(8.0F) + (int)getTranslationX(), i);
            }
          } while (!paramBoolean2);
          this.popupWindow.update((View)localObject, getLeft() - AndroidUtilities.dp(8.0F) + (int)getTranslationX(), i, -1, -1);
          return;
          if (this.subMenuOpenSide != 0) {
            break label345;
          }
        } while (getParent() == null);
        localObject = (View)getParent();
        if (paramBoolean1) {
          this.popupWindow.showAsDropDown((View)localObject, getLeft() + getMeasuredWidth() - this.popupLayout.getMeasuredWidth(), i);
        }
      } while (!paramBoolean2);
      this.popupWindow.update((View)localObject, getLeft() + getMeasuredWidth() - this.popupLayout.getMeasuredWidth(), i, -1, -1);
      return;
      if (paramBoolean1) {
        this.popupWindow.showAsDropDown(this, -AndroidUtilities.dp(8.0F), i);
      }
    } while (!paramBoolean2);
    this.popupWindow.update(this, -AndroidUtilities.dp(8.0F), i, -1, -1);
  }
  
  public TextView addSubItem(int paramInt, String paramString)
  {
    createPopupLayout();
    TextView localTextView = new TextView(getContext());
    localTextView.setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
    localTextView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
    if (!LocaleController.isRTL) {
      localTextView.setGravity(16);
    }
    for (;;)
    {
      localTextView.setPadding(AndroidUtilities.dp(16.0F), 0, AndroidUtilities.dp(16.0F), 0);
      localTextView.setTextSize(1, 16.0F);
      localTextView.setMinWidth(AndroidUtilities.dp(196.0F));
      localTextView.setTag(Integer.valueOf(paramInt));
      localTextView.setText(paramString);
      this.popupLayout.addView(localTextView);
      paramString = (LinearLayout.LayoutParams)localTextView.getLayoutParams();
      if (LocaleController.isRTL) {
        paramString.gravity = 5;
      }
      paramString.width = -1;
      paramString.height = AndroidUtilities.dp(48.0F);
      localTextView.setLayoutParams(paramString);
      localTextView.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if ((ActionBarMenuItem.this.popupWindow != null) && (ActionBarMenuItem.this.popupWindow.isShowing())) {
            if (!ActionBarMenuItem.this.processedPopupClick) {}
          }
          do
          {
            return;
            ActionBarMenuItem.access$202(ActionBarMenuItem.this, true);
            ActionBarMenuItem.this.popupWindow.dismiss(ActionBarMenuItem.this.allowCloseAnimation);
            if (ActionBarMenuItem.this.parentMenu != null)
            {
              ActionBarMenuItem.this.parentMenu.onItemClick(((Integer)paramAnonymousView.getTag()).intValue());
              return;
            }
          } while (ActionBarMenuItem.this.delegate == null);
          ActionBarMenuItem.this.delegate.onItemClick(((Integer)paramAnonymousView.getTag()).intValue());
        }
      });
      return localTextView;
      localTextView.setGravity(21);
    }
  }
  
  public void addSubItem(int paramInt1, View paramView, int paramInt2, int paramInt3)
  {
    createPopupLayout();
    paramView.setLayoutParams(new LinearLayout.LayoutParams(paramInt2, paramInt3));
    this.popupLayout.addView(paramView);
    paramView.setTag(Integer.valueOf(paramInt1));
    paramView.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if ((ActionBarMenuItem.this.popupWindow != null) && (ActionBarMenuItem.this.popupWindow.isShowing())) {
          if (!ActionBarMenuItem.this.processedPopupClick) {}
        }
        do
        {
          return;
          ActionBarMenuItem.access$202(ActionBarMenuItem.this, true);
          ActionBarMenuItem.this.popupWindow.dismiss(ActionBarMenuItem.this.allowCloseAnimation);
          if (ActionBarMenuItem.this.parentMenu != null)
          {
            ActionBarMenuItem.this.parentMenu.onItemClick(((Integer)paramAnonymousView.getTag()).intValue());
            return;
          }
        } while (ActionBarMenuItem.this.delegate == null);
        ActionBarMenuItem.this.delegate.onItemClick(((Integer)paramAnonymousView.getTag()).intValue());
      }
    });
    paramView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
  }
  
  public void addSubItem(View paramView, int paramInt1, int paramInt2)
  {
    createPopupLayout();
    this.popupLayout.addView(paramView, new LinearLayout.LayoutParams(paramInt1, paramInt2));
  }
  
  public void clearSearchText()
  {
    if (this.searchField == null) {
      return;
    }
    this.searchField.setText("");
  }
  
  public void closeSubMenu()
  {
    if ((this.popupWindow != null) && (this.popupWindow.isShowing())) {
      this.popupWindow.dismiss();
    }
  }
  
  public ImageView getImageView()
  {
    return this.iconView;
  }
  
  public EditTextBoldCursor getSearchField()
  {
    return this.searchField;
  }
  
  public boolean hasSubMenu()
  {
    return this.popupLayout != null;
  }
  
  public void hideSubItem(int paramInt)
  {
    View localView = this.popupLayout.findViewWithTag(Integer.valueOf(paramInt));
    if ((localView != null) && (localView.getVisibility() != 8)) {
      localView.setVisibility(8);
    }
  }
  
  public boolean isSearchField()
  {
    return this.isSearchField;
  }
  
  public boolean isSubItemVisible(int paramInt)
  {
    View localView = this.popupLayout.findViewWithTag(Integer.valueOf(paramInt));
    return (localView != null) && (localView.getVisibility() == 0);
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    if ((this.popupWindow != null) && (this.popupWindow.isShowing())) {
      updateOrShowPopup(false, true);
    }
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if (paramMotionEvent.getActionMasked() == 0) {
      if ((this.longClickEnabled) && (hasSubMenu()) && ((this.popupWindow == null) || ((this.popupWindow != null) && (!this.popupWindow.isShowing()))))
      {
        this.showMenuRunnable = new Runnable()
        {
          public void run()
          {
            if (ActionBarMenuItem.this.getParent() != null) {
              ActionBarMenuItem.this.getParent().requestDisallowInterceptTouchEvent(true);
            }
            ActionBarMenuItem.this.toggleSubMenu();
          }
        };
        AndroidUtilities.runOnUIThread(this.showMenuRunnable, 200L);
      }
    }
    for (;;)
    {
      return super.onTouchEvent(paramMotionEvent);
      if (paramMotionEvent.getActionMasked() == 2)
      {
        if ((hasSubMenu()) && ((this.popupWindow == null) || ((this.popupWindow != null) && (!this.popupWindow.isShowing()))))
        {
          if (paramMotionEvent.getY() > getHeight())
          {
            if (getParent() != null) {
              getParent().requestDisallowInterceptTouchEvent(true);
            }
            toggleSubMenu();
            return true;
          }
        }
        else if ((this.popupWindow != null) && (this.popupWindow.isShowing()))
        {
          getLocationOnScreen(this.location);
          float f3 = paramMotionEvent.getX();
          float f4 = this.location[0];
          float f1 = paramMotionEvent.getY();
          float f2 = this.location[1];
          this.popupLayout.getLocationOnScreen(this.location);
          f3 = f3 + f4 - this.location[0];
          f1 = f1 + f2 - this.location[1];
          this.selectedMenuView = null;
          int i = 0;
          label247:
          View localView;
          if (i < this.popupLayout.getItemsCount())
          {
            localView = this.popupLayout.getItemAt(i);
            localView.getHitRect(this.rect);
            if (((Integer)localView.getTag()).intValue() < 100)
            {
              if (this.rect.contains((int)f3, (int)f1)) {
                break label350;
              }
              localView.setPressed(false);
              localView.setSelected(false);
              if (Build.VERSION.SDK_INT == 21) {
                localView.getBackground().setVisible(false, false);
              }
            }
          }
          for (;;)
          {
            i += 1;
            break label247;
            break;
            label350:
            localView.setPressed(true);
            localView.setSelected(true);
            if (Build.VERSION.SDK_INT >= 21)
            {
              if (Build.VERSION.SDK_INT == 21) {
                localView.getBackground().setVisible(true, false);
              }
              localView.drawableHotspotChanged(f3, f1 - localView.getTop());
            }
            this.selectedMenuView = localView;
          }
        }
      }
      else if ((this.popupWindow != null) && (this.popupWindow.isShowing()) && (paramMotionEvent.getActionMasked() == 1))
      {
        if (this.selectedMenuView != null)
        {
          this.selectedMenuView.setSelected(false);
          if (this.parentMenu != null) {
            this.parentMenu.onItemClick(((Integer)this.selectedMenuView.getTag()).intValue());
          }
          for (;;)
          {
            this.popupWindow.dismiss(this.allowCloseAnimation);
            break;
            if (this.delegate != null) {
              this.delegate.onItemClick(((Integer)this.selectedMenuView.getTag()).intValue());
            }
          }
        }
        this.popupWindow.dismiss();
      }
      else if (this.selectedMenuView != null)
      {
        this.selectedMenuView.setSelected(false);
        this.selectedMenuView = null;
      }
    }
  }
  
  public void openSearch(boolean paramBoolean)
  {
    if ((this.searchContainer == null) || (this.searchContainer.getVisibility() == 0) || (this.parentMenu == null)) {
      return;
    }
    this.parentMenu.parentActionBar.onSearchFieldVisibilityChanged(toggleSearch(paramBoolean));
  }
  
  public void redrawPopup(int paramInt)
  {
    if (this.popupLayout != null)
    {
      this.popupLayout.backgroundDrawable.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      this.popupLayout.invalidate();
    }
  }
  
  public ActionBarMenuItem setActionBarMenuItemSearchListener(ActionBarMenuItemSearchListener paramActionBarMenuItemSearchListener)
  {
    this.listener = paramActionBarMenuItemSearchListener;
    return this;
  }
  
  public void setAdditionalOffset(int paramInt)
  {
    this.additionalOffset = paramInt;
  }
  
  public ActionBarMenuItem setAllowCloseAnimation(boolean paramBoolean)
  {
    this.allowCloseAnimation = paramBoolean;
    return this;
  }
  
  public void setDelegate(ActionBarMenuItemDelegate paramActionBarMenuItemDelegate)
  {
    this.delegate = paramActionBarMenuItemDelegate;
  }
  
  public void setIcon(int paramInt)
  {
    this.iconView.setImageResource(paramInt);
  }
  
  public void setIcon(Drawable paramDrawable)
  {
    this.iconView.setImageDrawable(paramDrawable);
  }
  
  public void setIconColor(int paramInt)
  {
    this.iconView.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
    if (this.clearButton != null) {
      this.clearButton.setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
    }
  }
  
  public void setIgnoreOnTextChange()
  {
    this.ignoreOnTextChange = true;
  }
  
  public ActionBarMenuItem setIsSearchField(boolean paramBoolean)
  {
    if (this.parentMenu == null) {
      return this;
    }
    Object localObject;
    int i;
    if ((paramBoolean) && (this.searchContainer == null))
    {
      this.searchContainer = new FrameLayout(getContext())
      {
        protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
        {
          super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
          if (LocaleController.isRTL) {
            paramAnonymousInt1 = 0;
          }
          for (;;)
          {
            ActionBarMenuItem.this.searchField.layout(paramAnonymousInt1, ActionBarMenuItem.this.searchField.getTop(), ActionBarMenuItem.this.searchField.getMeasuredWidth() + paramAnonymousInt1, ActionBarMenuItem.this.searchField.getBottom());
            return;
            if (ActionBarMenuItem.this.searchFieldCaption.getVisibility() == 0) {
              paramAnonymousInt1 = ActionBarMenuItem.this.searchFieldCaption.getMeasuredWidth() + AndroidUtilities.dp(4.0F);
            } else {
              paramAnonymousInt1 = 0;
            }
          }
        }
        
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          measureChildWithMargins(ActionBarMenuItem.this.clearButton, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
          if (ActionBarMenuItem.this.searchFieldCaption.getVisibility() == 0) {
            measureChildWithMargins(ActionBarMenuItem.this.searchFieldCaption, paramAnonymousInt1, View.MeasureSpec.getSize(paramAnonymousInt1) / 2, paramAnonymousInt2, 0);
          }
          for (int i = ActionBarMenuItem.this.searchFieldCaption.getMeasuredWidth() + AndroidUtilities.dp(4.0F);; i = 0)
          {
            measureChildWithMargins(ActionBarMenuItem.this.searchField, paramAnonymousInt1, i, paramAnonymousInt2, 0);
            View.MeasureSpec.getSize(paramAnonymousInt1);
            View.MeasureSpec.getSize(paramAnonymousInt2);
            setMeasuredDimension(View.MeasureSpec.getSize(paramAnonymousInt1), View.MeasureSpec.getSize(paramAnonymousInt2));
            return;
          }
        }
      };
      this.parentMenu.addView(this.searchContainer, 0, LayoutHelper.createLinear(0, -1, 1.0F, 6, 0, 0, 0));
      this.searchContainer.setVisibility(8);
      this.searchFieldCaption = new TextView(getContext());
      this.searchFieldCaption.setTextSize(1, 18.0F);
      this.searchFieldCaption.setTextColor(Theme.getColor("actionBarDefaultSearch"));
      this.searchFieldCaption.setSingleLine(true);
      this.searchFieldCaption.setEllipsize(TextUtils.TruncateAt.END);
      this.searchFieldCaption.setVisibility(8);
      localObject = this.searchFieldCaption;
      if (!LocaleController.isRTL) {
        break label519;
      }
      i = 5;
      ((TextView)localObject).setGravity(i);
      this.searchField = new EditTextBoldCursor(getContext())
      {
        public boolean dispatchKeyEvent(KeyEvent paramAnonymousKeyEvent)
        {
          return super.dispatchKeyEvent(paramAnonymousKeyEvent);
        }
        
        public boolean onKeyDown(int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousInt == 67) && (ActionBarMenuItem.this.searchField.length() == 0) && (ActionBarMenuItem.this.searchFieldCaption.getVisibility() == 0) && (ActionBarMenuItem.this.searchFieldCaption.length() > 0))
          {
            ActionBarMenuItem.this.clearButton.callOnClick();
            return true;
          }
          return super.onKeyDown(paramAnonymousInt, paramAnonymousKeyEvent);
        }
      };
      this.searchField.setCursorWidth(1.5F);
      this.searchField.setCursorColor(-1);
      this.searchField.setTextSize(1, 18.0F);
      this.searchField.setHintTextColor(Theme.getColor("actionBarDefaultSearchPlaceholder"));
      this.searchField.setTextColor(Theme.getColor("actionBarDefaultSearch"));
      this.searchField.setSingleLine(true);
      this.searchField.setBackgroundResource(0);
      this.searchField.setPadding(0, 0, 0, 0);
      i = this.searchField.getInputType();
      this.searchField.setInputType(i | 0x80000);
      if (Build.VERSION.SDK_INT < 23) {
        this.searchField.setCustomSelectionActionModeCallback(new ActionMode.Callback()
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
      }
      this.searchField.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousKeyEvent != null) && (((paramAnonymousKeyEvent.getAction() == 1) && (paramAnonymousKeyEvent.getKeyCode() == 84)) || ((paramAnonymousKeyEvent.getAction() == 0) && (paramAnonymousKeyEvent.getKeyCode() == 66))))
          {
            AndroidUtilities.hideKeyboard(ActionBarMenuItem.this.searchField);
            if (ActionBarMenuItem.this.listener != null) {
              ActionBarMenuItem.this.listener.onSearchPressed(ActionBarMenuItem.this.searchField);
            }
          }
          return false;
        }
      });
      this.searchField.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable) {}
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
        {
          if (ActionBarMenuItem.this.ignoreOnTextChange) {
            ActionBarMenuItem.access$1002(ActionBarMenuItem.this, false);
          }
          do
          {
            return;
            if (ActionBarMenuItem.this.listener != null) {
              ActionBarMenuItem.this.listener.onTextChanged(ActionBarMenuItem.this.searchField);
            }
          } while (ActionBarMenuItem.this.clearButton == null);
        }
      });
      this.searchField.setImeOptions(33554435);
      this.searchField.setTextIsSelectable(false);
      if (LocaleController.isRTL) {
        break label524;
      }
      this.searchContainer.addView(this.searchFieldCaption, LayoutHelper.createFrame(-2, 36.0F, 19, 0.0F, 5.5F, 0.0F, 0.0F));
      this.searchContainer.addView(this.searchField, LayoutHelper.createFrame(-1, 36.0F, 16, 0.0F, 0.0F, 48.0F, 0.0F));
    }
    for (;;)
    {
      this.clearButton = new ImageView(getContext());
      localObject = this.clearButton;
      CloseProgressDrawable2 localCloseProgressDrawable2 = new CloseProgressDrawable2();
      this.progressDrawable = localCloseProgressDrawable2;
      ((ImageView)localObject).setImageDrawable(localCloseProgressDrawable2);
      this.clearButton.setColorFilter(new PorterDuffColorFilter(this.parentMenu.parentActionBar.itemsColor, PorterDuff.Mode.MULTIPLY));
      this.clearButton.setScaleType(ImageView.ScaleType.CENTER);
      this.clearButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (ActionBarMenuItem.this.searchField.length() != 0) {
            ActionBarMenuItem.this.searchField.setText("");
          }
          for (;;)
          {
            ActionBarMenuItem.this.searchField.requestFocus();
            AndroidUtilities.showKeyboard(ActionBarMenuItem.this.searchField);
            return;
            if ((ActionBarMenuItem.this.searchFieldCaption != null) && (ActionBarMenuItem.this.searchFieldCaption.getVisibility() == 0))
            {
              ActionBarMenuItem.this.searchFieldCaption.setVisibility(8);
              if (ActionBarMenuItem.this.listener != null) {
                ActionBarMenuItem.this.listener.onCaptionCleared();
              }
            }
          }
        }
      });
      this.searchContainer.addView(this.clearButton, LayoutHelper.createFrame(48, -1, 21));
      this.isSearchField = paramBoolean;
      return this;
      label519:
      i = 3;
      break;
      label524:
      this.searchContainer.addView(this.searchField, LayoutHelper.createFrame(-1, 36.0F, 16, 0.0F, 0.0F, 48.0F, 0.0F));
      this.searchContainer.addView(this.searchFieldCaption, LayoutHelper.createFrame(-2, 36.0F, 21, 0.0F, 5.5F, 48.0F, 0.0F));
    }
  }
  
  public void setLayoutInScreen(boolean paramBoolean)
  {
    this.layoutInScreen = paramBoolean;
  }
  
  public void setLongClickEnabled(boolean paramBoolean)
  {
    this.longClickEnabled = paramBoolean;
  }
  
  public ActionBarMenuItem setOverrideMenuClick(boolean paramBoolean)
  {
    this.overrideMenuClick = paramBoolean;
    return this;
  }
  
  public void setPopupAnimationEnabled(boolean paramBoolean)
  {
    if (this.popupWindow != null) {
      this.popupWindow.setAnimationEnabled(paramBoolean);
    }
    this.animationEnabled = paramBoolean;
  }
  
  public void setPopupItemsColor(int paramInt)
  {
    if (this.popupLayout == null) {}
    for (;;)
    {
      return;
      int j = this.popupLayout.linearLayout.getChildCount();
      int i = 0;
      while (i < j)
      {
        View localView = this.popupLayout.linearLayout.getChildAt(i);
        if ((localView instanceof TextView)) {
          ((TextView)localView).setTextColor(paramInt);
        }
        i += 1;
      }
    }
  }
  
  public void setSearchFieldCaption(CharSequence paramCharSequence)
  {
    if (TextUtils.isEmpty(paramCharSequence)) {
      this.searchFieldCaption.setVisibility(8);
    }
    for (;;)
    {
      if (this.clearButton != null) {}
      return;
      this.searchFieldCaption.setVisibility(0);
      this.searchFieldCaption.setText(paramCharSequence);
    }
  }
  
  public void setShowSearchProgress(boolean paramBoolean)
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
  
  public void setSubMenuOpenSide(int paramInt)
  {
    this.subMenuOpenSide = paramInt;
  }
  
  public void showSubItem(int paramInt)
  {
    View localView = this.popupLayout.findViewWithTag(Integer.valueOf(paramInt));
    if ((localView != null) && (localView.getVisibility() != 0)) {
      localView.setVisibility(0);
    }
  }
  
  public boolean toggleSearch(boolean paramBoolean)
  {
    if (this.searchContainer == null) {}
    do
    {
      do
      {
        return false;
        if (this.searchContainer.getVisibility() != 0) {
          break;
        }
      } while ((this.listener != null) && ((this.listener == null) || (!this.listener.canCollapseSearch())));
      this.searchContainer.setVisibility(8);
      this.searchField.clearFocus();
      setVisibility(0);
      if (paramBoolean) {
        AndroidUtilities.hideKeyboard(this.searchField);
      }
    } while (this.listener == null);
    this.listener.onSearchCollapse();
    return false;
    this.searchContainer.setVisibility(0);
    setVisibility(8);
    this.searchField.setText("");
    this.searchField.requestFocus();
    if (paramBoolean) {
      AndroidUtilities.showKeyboard(this.searchField);
    }
    if (this.listener != null) {
      this.listener.onSearchExpand();
    }
    return true;
  }
  
  public void toggleSubMenu()
  {
    if (this.popupLayout == null) {
      return;
    }
    if (this.showMenuRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.showMenuRunnable);
      this.showMenuRunnable = null;
    }
    if ((this.popupWindow != null) && (this.popupWindow.isShowing()))
    {
      this.popupWindow.dismiss();
      return;
    }
    if (this.popupWindow == null)
    {
      this.popupWindow = new ActionBarPopupWindow(this.popupLayout, -2, -2);
      if ((!this.animationEnabled) || (Build.VERSION.SDK_INT < 19)) {
        break label312;
      }
      this.popupWindow.setAnimationStyle(0);
    }
    for (;;)
    {
      if (!this.animationEnabled) {
        this.popupWindow.setAnimationEnabled(this.animationEnabled);
      }
      this.popupWindow.setOutsideTouchable(true);
      this.popupWindow.setClippingEnabled(true);
      if (this.layoutInScreen) {}
      try
      {
        if (layoutInScreenMethod == null)
        {
          layoutInScreenMethod = PopupWindow.class.getDeclaredMethod("setLayoutInScreenEnabled", new Class[] { Boolean.TYPE });
          layoutInScreenMethod.setAccessible(true);
        }
        layoutInScreenMethod.invoke(this.popupWindow, new Object[] { Boolean.valueOf(true) });
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          updateOrShowPopup(true, false);
        }
      }
      this.popupWindow.setInputMethodMode(2);
      this.popupWindow.setSoftInputMode(0);
      this.popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0F), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0F), Integer.MIN_VALUE));
      this.popupWindow.getContentView().setFocusableInTouchMode(true);
      this.popupWindow.getContentView().setOnKeyListener(new View.OnKeyListener()
      {
        public boolean onKey(View paramAnonymousView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousInt == 82) && (paramAnonymousKeyEvent.getRepeatCount() == 0) && (paramAnonymousKeyEvent.getAction() == 1) && (ActionBarMenuItem.this.popupWindow != null) && (ActionBarMenuItem.this.popupWindow.isShowing()))
          {
            ActionBarMenuItem.this.popupWindow.dismiss();
            return true;
          }
          return false;
        }
      });
      this.processedPopupClick = false;
      this.popupWindow.setFocusable(true);
      if (this.popupLayout.getMeasuredWidth() != 0) {
        break;
      }
      updateOrShowPopup(true, true);
      this.popupWindow.startAnimation();
      return;
      label312:
      this.popupWindow.setAnimationStyle(2131558406);
    }
  }
  
  public static abstract interface ActionBarMenuItemDelegate
  {
    public abstract void onItemClick(int paramInt);
  }
  
  public static class ActionBarMenuItemSearchListener
  {
    public boolean canCollapseSearch()
    {
      return true;
    }
    
    public void onCaptionCleared() {}
    
    public void onSearchCollapse() {}
    
    public void onSearchExpand() {}
    
    public void onSearchPressed(EditText paramEditText) {}
    
    public void onTextChanged(EditText paramEditText) {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/ActionBarMenuItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */