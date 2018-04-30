package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DrawerLayoutAdapter
  extends RecyclerListView.SelectionAdapter
{
  private ArrayList<Integer> accountNumbers = new ArrayList();
  private boolean accountsShowed;
  private ArrayList<Item> items = new ArrayList(11);
  private Context mContext;
  private DrawerProfileCell profileCell;
  
  public DrawerLayoutAdapter(Context paramContext)
  {
    this.mContext = paramContext;
    if ((UserConfig.getActivatedAccountsCount() > 1) && (MessagesController.getGlobalMainSettings().getBoolean("accountsShowed", true))) {}
    for (;;)
    {
      this.accountsShowed = bool;
      Theme.createDialogsResources(paramContext);
      resetItems();
      return;
      bool = false;
    }
  }
  
  private int getAccountRowsCount()
  {
    int j = this.accountNumbers.size() + 1;
    int i = j;
    if (this.accountNumbers.size() < 3) {
      i = j + 1;
    }
    return i;
  }
  
  private void resetItems()
  {
    this.accountNumbers.clear();
    int i = 0;
    while (i < 3)
    {
      if (UserConfig.getInstance(i).isClientActivated()) {
        this.accountNumbers.add(Integer.valueOf(i));
      }
      i += 1;
    }
    Collections.sort(this.accountNumbers, new Comparator()
    {
      public int compare(Integer paramAnonymousInteger1, Integer paramAnonymousInteger2)
      {
        long l1 = UserConfig.getInstance(paramAnonymousInteger1.intValue()).loginTime;
        long l2 = UserConfig.getInstance(paramAnonymousInteger2.intValue()).loginTime;
        if (l1 > l2) {
          return 1;
        }
        if (l1 < l2) {
          return -1;
        }
        return 0;
      }
    });
    this.items.clear();
    if (!UserConfig.getInstance(UserConfig.selectedAccount).isClientActivated()) {
      return;
    }
    this.items.add(new Item(2, LocaleController.getString("NewGroup", 2131493869), 2131165497));
    this.items.add(new Item(3, LocaleController.getString("NewSecretChat", 2131493877), 2131165499));
    this.items.add(new Item(4, LocaleController.getString("NewChannel", 2131493867), 2131165491));
    this.items.add(null);
    this.items.add(new Item(6, LocaleController.getString("Contacts", 2131493290), 2131165493));
    this.items.add(new Item(11, LocaleController.getString("SavedMessages", 2131494293), 2131165498));
    this.items.add(new Item(10, LocaleController.getString("Calls", 2131493124), 2131165492));
    this.items.add(new Item(7, LocaleController.getString("InviteFriends", 2131493685), 2131165496));
    this.items.add(new Item(8, LocaleController.getString("Settings", 2131494379), 2131165500));
    this.items.add(new Item(9, LocaleController.getString("TelegramFAQ", 2131494469), 2131165495));
  }
  
  public int getId(int paramInt)
  {
    int i = paramInt - 2;
    paramInt = i;
    if (this.accountsShowed) {
      paramInt = i - getAccountRowsCount();
    }
    if ((paramInt < 0) || (paramInt >= this.items.size())) {}
    Item localItem;
    do
    {
      return -1;
      localItem = (Item)this.items.get(paramInt);
    } while (localItem == null);
    return localItem.id;
  }
  
  public int getItemCount()
  {
    int j = this.items.size() + 2;
    int i = j;
    if (this.accountsShowed) {
      i = j + getAccountRowsCount();
    }
    return i;
  }
  
  public int getItemViewType(int paramInt)
  {
    int i = 1;
    if (paramInt == 0) {
      i = 0;
    }
    while (paramInt == 1) {
      return i;
    }
    i = paramInt - 2;
    paramInt = i;
    if (this.accountsShowed)
    {
      if (i < this.accountNumbers.size()) {
        return 4;
      }
      if (this.accountNumbers.size() < 3)
      {
        if (i == this.accountNumbers.size()) {
          return 5;
        }
        if (i == this.accountNumbers.size() + 1) {
          return 2;
        }
      }
      else if (i == this.accountNumbers.size())
      {
        return 2;
      }
      paramInt = i - getAccountRowsCount();
    }
    if (paramInt == 3) {
      return 2;
    }
    return 3;
  }
  
  public boolean isAccountsShowed()
  {
    return this.accountsShowed;
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    int i = paramViewHolder.getItemViewType();
    return (i == 3) || (i == 4) || (i == 5);
  }
  
  public void notifyDataSetChanged()
  {
    resetItems();
    super.notifyDataSetChanged();
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    switch (paramViewHolder.getItemViewType())
    {
    case 1: 
    case 2: 
    default: 
      return;
    case 0: 
      ((DrawerProfileCell)paramViewHolder.itemView).setUser(MessagesController.getInstance(UserConfig.selectedAccount).getUser(Integer.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())), this.accountsShowed);
      paramViewHolder.itemView.setBackgroundColor(Theme.getColor("avatar_backgroundActionBarBlue"));
      return;
    case 3: 
      int i = paramInt - 2;
      paramInt = i;
      if (this.accountsShowed) {
        paramInt = i - getAccountRowsCount();
      }
      paramViewHolder = (DrawerActionCell)paramViewHolder.itemView;
      ((Item)this.items.get(paramInt)).bind(paramViewHolder);
      paramViewHolder.setPadding(0, 0, 0, 0);
      return;
    }
    ((DrawerUserCell)paramViewHolder.itemView).setAccount(((Integer)this.accountNumbers.get(paramInt - 2)).intValue());
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    switch (paramInt)
    {
    case 1: 
    default: 
      paramViewGroup = new EmptyCell(this.mContext, AndroidUtilities.dp(8.0F));
    }
    for (;;)
    {
      paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
      return new RecyclerListView.Holder(paramViewGroup);
      this.profileCell = new DrawerProfileCell(this.mContext);
      this.profileCell.setOnArrowClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = (DrawerProfileCell)paramAnonymousView;
          DrawerLayoutAdapter.this.setAccountsShowed(paramAnonymousView.isAccountsShowed(), true);
        }
      });
      paramViewGroup = this.profileCell;
      continue;
      paramViewGroup = new DividerCell(this.mContext);
      continue;
      paramViewGroup = new DrawerActionCell(this.mContext);
      continue;
      paramViewGroup = new DrawerUserCell(this.mContext);
      continue;
      paramViewGroup = new DrawerAddCell(this.mContext);
    }
  }
  
  public void setAccountsShowed(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.accountsShowed == paramBoolean1) {
      return;
    }
    this.accountsShowed = paramBoolean1;
    if (this.profileCell != null) {
      this.profileCell.setAccountsShowed(this.accountsShowed);
    }
    MessagesController.getGlobalMainSettings().edit().putBoolean("accountsShowed", this.accountsShowed).commit();
    if (paramBoolean2)
    {
      if (this.accountsShowed)
      {
        notifyItemRangeInserted(2, getAccountRowsCount());
        return;
      }
      notifyItemRangeRemoved(2, getAccountRowsCount());
      return;
    }
    notifyDataSetChanged();
  }
  
  private class Item
  {
    public int icon;
    public int id;
    public String text;
    
    public Item(int paramInt1, String paramString, int paramInt2)
    {
      this.icon = paramInt2;
      this.id = paramInt1;
      this.text = paramString;
    }
    
    public void bind(DrawerActionCell paramDrawerActionCell)
    {
      paramDrawerActionCell.setTextAndIcon(this.text, this.icon);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/DrawerLayoutAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */