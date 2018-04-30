package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.EditTextBoldCursor;

public class ActionBarMenu
  extends LinearLayout
{
  protected boolean isActionMode;
  protected ActionBar parentActionBar;
  
  public ActionBarMenu(Context paramContext)
  {
    super(paramContext);
  }
  
  public ActionBarMenu(Context paramContext, ActionBar paramActionBar)
  {
    super(paramContext);
    setOrientation(0);
    this.parentActionBar = paramActionBar;
  }
  
  public ActionBarMenuItem addItem(int paramInt1, int paramInt2)
  {
    if (this.isActionMode) {}
    for (int i = this.parentActionBar.itemsActionModeBackgroundColor;; i = this.parentActionBar.itemsBackgroundColor) {
      return addItem(paramInt1, paramInt2, i);
    }
  }
  
  public ActionBarMenuItem addItem(int paramInt1, int paramInt2, int paramInt3)
  {
    return addItem(paramInt1, paramInt2, paramInt3, null, AndroidUtilities.dp(48.0F));
  }
  
  public ActionBarMenuItem addItem(int paramInt1, int paramInt2, int paramInt3, Drawable paramDrawable, int paramInt4)
  {
    Object localObject = getContext();
    int i;
    if (this.isActionMode)
    {
      i = this.parentActionBar.itemsActionModeColor;
      localObject = new ActionBarMenuItem((Context)localObject, this, paramInt3, i);
      ((ActionBarMenuItem)localObject).setTag(Integer.valueOf(paramInt1));
      if (paramDrawable == null) {
        break label105;
      }
      ((ActionBarMenuItem)localObject).iconView.setImageDrawable(paramDrawable);
    }
    for (;;)
    {
      addView((View)localObject, new LinearLayout.LayoutParams(paramInt4, -1));
      ((ActionBarMenuItem)localObject).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          ActionBarMenuItem localActionBarMenuItem = (ActionBarMenuItem)paramAnonymousView;
          if (localActionBarMenuItem.hasSubMenu())
          {
            if (ActionBarMenu.this.parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
              localActionBarMenuItem.toggleSubMenu();
            }
            return;
          }
          if (localActionBarMenuItem.isSearchField())
          {
            ActionBarMenu.this.parentActionBar.onSearchFieldVisibilityChanged(localActionBarMenuItem.toggleSearch(true));
            return;
          }
          ActionBarMenu.this.onItemClick(((Integer)paramAnonymousView.getTag()).intValue());
        }
      });
      return (ActionBarMenuItem)localObject;
      i = this.parentActionBar.itemsColor;
      break;
      label105:
      if (paramInt2 != 0) {
        ((ActionBarMenuItem)localObject).iconView.setImageResource(paramInt2);
      }
    }
  }
  
  public ActionBarMenuItem addItem(int paramInt, Drawable paramDrawable)
  {
    if (this.isActionMode) {}
    for (int i = this.parentActionBar.itemsActionModeBackgroundColor;; i = this.parentActionBar.itemsBackgroundColor) {
      return addItem(paramInt, 0, i, paramDrawable, AndroidUtilities.dp(48.0F));
    }
  }
  
  public ActionBarMenuItem addItemWithWidth(int paramInt1, int paramInt2, int paramInt3)
  {
    if (this.isActionMode) {}
    for (int i = this.parentActionBar.itemsActionModeBackgroundColor;; i = this.parentActionBar.itemsBackgroundColor) {
      return addItem(paramInt1, paramInt2, i, null, paramInt3);
    }
  }
  
  public void clearItems()
  {
    removeAllViews();
  }
  
  public void closeSearchField(boolean paramBoolean)
  {
    int j = getChildCount();
    int i = 0;
    for (;;)
    {
      if (i < j)
      {
        Object localObject = getChildAt(i);
        if ((localObject instanceof ActionBarMenuItem))
        {
          localObject = (ActionBarMenuItem)localObject;
          if (((ActionBarMenuItem)localObject).isSearchField())
          {
            this.parentActionBar.onSearchFieldVisibilityChanged(false);
            ((ActionBarMenuItem)localObject).toggleSearch(paramBoolean);
          }
        }
      }
      else
      {
        return;
      }
      i += 1;
    }
  }
  
  public ActionBarMenuItem getItem(int paramInt)
  {
    View localView = findViewWithTag(Integer.valueOf(paramInt));
    if ((localView instanceof ActionBarMenuItem)) {
      return (ActionBarMenuItem)localView;
    }
    return null;
  }
  
  public void hideAllPopupMenus()
  {
    int j = getChildCount();
    int i = 0;
    while (i < j)
    {
      View localView = getChildAt(i);
      if ((localView instanceof ActionBarMenuItem)) {
        ((ActionBarMenuItem)localView).closeSubMenu();
      }
      i += 1;
    }
  }
  
  public void onItemClick(int paramInt)
  {
    if (this.parentActionBar.actionBarMenuOnItemClick != null) {
      this.parentActionBar.actionBarMenuOnItemClick.onItemClick(paramInt);
    }
  }
  
  public void onMenuButtonPressed()
  {
    int j = getChildCount();
    int i = 0;
    Object localObject;
    if (i < j)
    {
      localObject = getChildAt(i);
      if ((localObject instanceof ActionBarMenuItem))
      {
        localObject = (ActionBarMenuItem)localObject;
        if (((ActionBarMenuItem)localObject).getVisibility() == 0) {
          break label44;
        }
      }
    }
    label44:
    do
    {
      i += 1;
      break;
      if (((ActionBarMenuItem)localObject).hasSubMenu())
      {
        ((ActionBarMenuItem)localObject).toggleSubMenu();
        return;
      }
    } while (!((ActionBarMenuItem)localObject).overrideMenuClick);
    onItemClick(((Integer)((ActionBarMenuItem)localObject).getTag()).intValue());
  }
  
  public void openSearchField(boolean paramBoolean, String paramString)
  {
    int j = getChildCount();
    int i = 0;
    for (;;)
    {
      if (i < j)
      {
        Object localObject = getChildAt(i);
        if ((localObject instanceof ActionBarMenuItem))
        {
          localObject = (ActionBarMenuItem)localObject;
          if (((ActionBarMenuItem)localObject).isSearchField())
          {
            if (paramBoolean) {
              this.parentActionBar.onSearchFieldVisibilityChanged(((ActionBarMenuItem)localObject).toggleSearch(true));
            }
            ((ActionBarMenuItem)localObject).getSearchField().setText(paramString);
            ((ActionBarMenuItem)localObject).getSearchField().setSelection(paramString.length());
          }
        }
      }
      else
      {
        return;
      }
      i += 1;
    }
  }
  
  protected void redrawPopup(int paramInt)
  {
    int j = getChildCount();
    int i = 0;
    while (i < j)
    {
      View localView = getChildAt(i);
      if ((localView instanceof ActionBarMenuItem)) {
        ((ActionBarMenuItem)localView).redrawPopup(paramInt);
      }
      i += 1;
    }
  }
  
  protected void setPopupItemsColor(int paramInt)
  {
    int j = getChildCount();
    int i = 0;
    while (i < j)
    {
      View localView = getChildAt(i);
      if ((localView instanceof ActionBarMenuItem)) {
        ((ActionBarMenuItem)localView).setPopupItemsColor(paramInt);
      }
      i += 1;
    }
  }
  
  public void setSearchTextColor(int paramInt, boolean paramBoolean)
  {
    int j = getChildCount();
    int i = 0;
    for (;;)
    {
      Object localObject;
      if (i < j)
      {
        localObject = getChildAt(i);
        if (!(localObject instanceof ActionBarMenuItem)) {
          break label68;
        }
        localObject = (ActionBarMenuItem)localObject;
        if (!((ActionBarMenuItem)localObject).isSearchField()) {
          break label68;
        }
        if (paramBoolean) {
          ((ActionBarMenuItem)localObject).getSearchField().setHintTextColor(paramInt);
        }
      }
      else
      {
        return;
      }
      ((ActionBarMenuItem)localObject).getSearchField().setTextColor(paramInt);
      return;
      label68:
      i += 1;
    }
  }
  
  protected void updateItemsBackgroundColor()
  {
    int k = getChildCount();
    int i = 0;
    if (i < k)
    {
      View localView = getChildAt(i);
      if ((localView instanceof ActionBarMenuItem)) {
        if (!this.isActionMode) {
          break label58;
        }
      }
      label58:
      for (int j = this.parentActionBar.itemsActionModeBackgroundColor;; j = this.parentActionBar.itemsBackgroundColor)
      {
        localView.setBackgroundDrawable(Theme.createSelectorDrawable(j));
        i += 1;
        break;
      }
    }
  }
  
  protected void updateItemsColor()
  {
    int k = getChildCount();
    int i = 0;
    if (i < k)
    {
      Object localObject = getChildAt(i);
      if ((localObject instanceof ActionBarMenuItem))
      {
        localObject = (ActionBarMenuItem)localObject;
        if (!this.isActionMode) {
          break label62;
        }
      }
      label62:
      for (int j = this.parentActionBar.itemsActionModeColor;; j = this.parentActionBar.itemsColor)
      {
        ((ActionBarMenuItem)localObject).setIconColor(j);
        i += 1;
        break;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/ActionBarMenu.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */