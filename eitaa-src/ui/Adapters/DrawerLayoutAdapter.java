package ir.eitaa.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.UserConfig;
import ir.eitaa.ui.Cells.DividerCell;
import ir.eitaa.ui.Cells.DrawerActionCell;
import ir.eitaa.ui.Cells.DrawerProfileCell;
import ir.eitaa.ui.Cells.EmptyCell;

public class DrawerLayoutAdapter
  extends BaseAdapter
{
  private Context mContext;
  
  public DrawerLayoutAdapter(Context paramContext)
  {
    this.mContext = paramContext;
  }
  
  public boolean areAllItemsEnabled()
  {
    return false;
  }
  
  public int getCount()
  {
    if (UserConfig.isClientActivated()) {
      return 9;
    }
    return 0;
  }
  
  public Object getItem(int paramInt)
  {
    return null;
  }
  
  public long getItemId(int paramInt)
  {
    return paramInt;
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
    if (paramInt == 4) {
      return 2;
    }
    return 3;
  }
  
  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    int i = getItemViewType(paramInt);
    if (i == 0) {
      if (paramView == null)
      {
        paramViewGroup = new DrawerProfileCell(this.mContext);
        paramView = paramViewGroup;
        paramViewGroup.setUser(MessagesController.getInstance().getUser(Integer.valueOf(UserConfig.getClientUserId())));
        paramViewGroup = paramView;
      }
    }
    label87:
    label111:
    Object localObject;
    do
    {
      do
      {
        do
        {
          do
          {
            return paramViewGroup;
            paramViewGroup = (DrawerProfileCell)paramView;
            break;
            if (i != 1) {
              break label87;
            }
            paramViewGroup = paramView;
          } while (paramView != null);
          return new EmptyCell(this.mContext, AndroidUtilities.dp(8.0F));
          if (i != 2) {
            break label111;
          }
          paramViewGroup = paramView;
        } while (paramView != null);
        return new DividerCell(this.mContext);
        paramViewGroup = paramView;
      } while (i != 3);
      localObject = paramView;
      if (paramView == null) {
        localObject = new DrawerActionCell(this.mContext);
      }
      paramView = (DrawerActionCell)localObject;
      if (paramInt == 2)
      {
        paramView.setTextAndIcon(LocaleController.getString("NewGroup", 2131165920), 2130837824);
        return (View)localObject;
      }
      if (paramInt == 3)
      {
        paramView.setTextAndIcon(LocaleController.getString("NewChannel", 2131165919), 2130837820);
        return (View)localObject;
      }
      if (paramInt == 5)
      {
        paramView.setTextAndIcon(LocaleController.getString("Contacts", 2131165522), 2130837821);
        return (View)localObject;
      }
      if (paramInt == 6)
      {
        paramView.setTextAndIcon(LocaleController.getString("InviteFriends", 2131165764), 2130837823);
        return (View)localObject;
      }
      if (paramInt == 7)
      {
        paramView.setTextAndIcon(LocaleController.getString("Settings", 2131166273), 2130837826);
        return (View)localObject;
      }
      paramViewGroup = (ViewGroup)localObject;
    } while (paramInt != 8);
    paramView.setTextAndIcon(LocaleController.getString("TelegramFaq", 2131166329), 2130837822);
    return (View)localObject;
  }
  
  public int getViewTypeCount()
  {
    return 4;
  }
  
  public boolean hasStableIds()
  {
    return true;
  }
  
  public boolean isEmpty()
  {
    return !UserConfig.isClientActivated();
  }
  
  public boolean isEnabled(int paramInt)
  {
    return (paramInt != 1) && (paramInt != 4);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Adapters/DrawerLayoutAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */