package ir.eitaa.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ContactsController;
import ir.eitaa.messenger.ContactsController.Contact;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.tgnet.TLRPC.TL_contact;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.ui.Cells.DividerCell;
import ir.eitaa.ui.Cells.GreySectionCell;
import ir.eitaa.ui.Cells.LetterSectionCell;
import ir.eitaa.ui.Cells.TextCell;
import ir.eitaa.ui.Cells.UserCell;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactsAdapter
  extends BaseSectionsAdapter
{
  private HashMap<Integer, ?> checkedMap;
  private HashMap<Integer, TLRPC.User> ignoreUsers;
  private boolean isAdmin;
  private Context mContext;
  private boolean needPhonebook;
  private int onlyUsers;
  private boolean scrolling;
  
  public ContactsAdapter(Context paramContext, int paramInt, boolean paramBoolean1, HashMap<Integer, TLRPC.User> paramHashMap, boolean paramBoolean2)
  {
    this.mContext = paramContext;
    this.onlyUsers = paramInt;
    this.needPhonebook = paramBoolean1;
    this.ignoreUsers = paramHashMap;
    this.isAdmin = paramBoolean2;
  }
  
  public int getCountForSection(int paramInt)
  {
    int i = 2;
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label109;
      }
      localArrayList = ContactsController.getInstance().sortedUsersMutualSectionsArray;
      label33:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label120;
      }
      if (paramInt >= localArrayList.size()) {
        break label200;
      }
      i = ((ArrayList)localHashMap.get(localArrayList.get(paramInt))).size();
      if (paramInt == localArrayList.size() - 1)
      {
        paramInt = i;
        if (!this.needPhonebook) {}
      }
      else
      {
        paramInt = i + 1;
      }
    }
    label109:
    label120:
    label144:
    do
    {
      do
      {
        do
        {
          return paramInt;
          localHashMap = ContactsController.getInstance().usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance().sortedUsersSectionsArray;
          break label33;
          if (paramInt != 0) {
            break label144;
          }
          paramInt = i;
        } while (this.needPhonebook);
        paramInt = i;
      } while (this.isAdmin);
      return 3;
      if (paramInt - 1 >= localArrayList.size()) {
        break label200;
      }
      i = ((ArrayList)localHashMap.get(localArrayList.get(paramInt - 1))).size();
      if (paramInt - 1 != localArrayList.size() - 1) {
        break label196;
      }
      paramInt = i;
    } while (!this.needPhonebook);
    label196:
    return i + 1;
    label200:
    if (this.needPhonebook) {
      return ContactsController.getInstance().phoneBookContacts.size();
    }
    return 0;
  }
  
  public Object getItem(int paramInt1, int paramInt2)
  {
    Object localObject3 = null;
    Object localObject1;
    ArrayList localArrayList;
    label34:
    Object localObject2;
    if (this.onlyUsers == 2)
    {
      localObject1 = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label122;
      }
      localArrayList = ContactsController.getInstance().sortedUsersMutualSectionsArray;
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label133;
      }
      localObject2 = localObject3;
      if (paramInt1 < localArrayList.size())
      {
        localObject1 = (ArrayList)((HashMap)localObject1).get(localArrayList.get(paramInt1));
        localObject2 = localObject3;
        if (paramInt2 < ((ArrayList)localObject1).size()) {
          localObject2 = MessagesController.getInstance().getUser(Integer.valueOf(((TLRPC.TL_contact)((ArrayList)localObject1).get(paramInt2)).user_id));
        }
      }
    }
    label122:
    label133:
    label201:
    do
    {
      do
      {
        do
        {
          return localObject2;
          localObject1 = ContactsController.getInstance().usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance().sortedUsersSectionsArray;
          break label34;
          localObject2 = localObject3;
        } while (paramInt1 == 0);
        if (paramInt1 - 1 >= localArrayList.size()) {
          break label201;
        }
        localObject1 = (ArrayList)((HashMap)localObject1).get(localArrayList.get(paramInt1 - 1));
        localObject2 = localObject3;
      } while (paramInt2 >= ((ArrayList)localObject1).size());
      return MessagesController.getInstance().getUser(Integer.valueOf(((TLRPC.TL_contact)((ArrayList)localObject1).get(paramInt2)).user_id));
      localObject2 = localObject3;
    } while (!this.needPhonebook);
    return ContactsController.getInstance().phoneBookContacts.get(paramInt2);
  }
  
  public View getItemView(int paramInt1, int paramInt2, View paramView, ViewGroup paramViewGroup)
  {
    int i = getItemViewType(paramInt1, paramInt2);
    float f;
    if (i == 4)
    {
      paramViewGroup = paramView;
      if (paramView == null)
      {
        paramViewGroup = new DividerCell(this.mContext);
        if (!LocaleController.isRTL) {
          break label76;
        }
        f = 28.0F;
        paramInt1 = AndroidUtilities.dp(f);
        if (!LocaleController.isRTL) {
          break label83;
        }
        f = 72.0F;
        label60:
        paramViewGroup.setPadding(paramInt1, 0, AndroidUtilities.dp(f), 0);
      }
    }
    label76:
    label83:
    label134:
    label257:
    do
    {
      do
      {
        do
        {
          return paramViewGroup;
          f = 72.0F;
          break;
          f = 28.0F;
          break label60;
          if (i != 3) {
            break label134;
          }
          paramViewGroup = paramView;
        } while (paramView != null);
        paramView = new GreySectionCell(this.mContext);
        ((GreySectionCell)paramView).setText(LocaleController.getString("Contacts", 2131165522).toUpperCase());
        return paramView;
        if (i != 2) {
          break label257;
        }
        localObject = paramView;
        if (paramView == null) {
          localObject = new TextCell(this.mContext);
        }
        paramView = (TextCell)localObject;
        if (this.needPhonebook)
        {
          paramView.setTextAndIcon(LocaleController.getString("InviteFriends", 2131165764), 2130837823);
          return (View)localObject;
        }
        if (this.isAdmin)
        {
          paramView.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", 2131165768), 2130837823);
          return (View)localObject;
        }
        if (paramInt2 == 0)
        {
          paramView.setTextAndIcon(LocaleController.getString("NewGroup", 2131165920), 2130837824);
          return (View)localObject;
        }
        paramViewGroup = (ViewGroup)localObject;
      } while (paramInt2 != 1);
      paramView.setTextAndIcon(LocaleController.getString("NewChannel", 2131165919), 2130837820);
      return (View)localObject;
      if (i == 1)
      {
        paramViewGroup = paramView;
        if (paramView == null) {
          paramViewGroup = new TextCell(this.mContext);
        }
        paramView = (ContactsController.Contact)ContactsController.getInstance().phoneBookContacts.get(paramInt2);
        localObject = (TextCell)paramViewGroup;
        if ((paramView.first_name != null) && (paramView.last_name != null))
        {
          ((TextCell)localObject).setText(paramView.first_name + " " + paramView.last_name);
          return paramViewGroup;
        }
        if ((paramView.first_name != null) && (paramView.last_name == null))
        {
          ((TextCell)localObject).setText(paramView.first_name);
          return paramViewGroup;
        }
        ((TextCell)localObject).setText(paramView.last_name);
        return paramViewGroup;
      }
      paramViewGroup = paramView;
    } while (i != 0);
    Object localObject = paramView;
    if (paramView == null)
    {
      localObject = new UserCell(this.mContext, 58, 1, false);
      ((UserCell)localObject).setStatusColors(-5723992, -12876608);
    }
    label452:
    label468:
    label485:
    boolean bool2;
    if (this.onlyUsers == 2)
    {
      paramView = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label632;
      }
      paramViewGroup = ContactsController.getInstance().sortedUsersMutualSectionsArray;
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label643;
      }
      i = 0;
      paramView = (ArrayList)paramView.get(paramViewGroup.get(paramInt1 - i));
      paramView = MessagesController.getInstance().getUser(Integer.valueOf(((TLRPC.TL_contact)paramView.get(paramInt2)).user_id));
      ((UserCell)localObject).setData(paramView, null, null, 0);
      if (this.checkedMap != null)
      {
        paramViewGroup = (UserCell)localObject;
        bool2 = this.checkedMap.containsKey(Integer.valueOf(paramView.id));
        if (this.scrolling) {
          break label649;
        }
      }
    }
    label632:
    label643:
    label649:
    for (boolean bool1 = true;; bool1 = false)
    {
      paramViewGroup.setChecked(bool2, bool1);
      paramViewGroup = (ViewGroup)localObject;
      if (this.ignoreUsers == null) {
        break;
      }
      if (!this.ignoreUsers.containsKey(Integer.valueOf(paramView.id))) {
        break label655;
      }
      ((View)localObject).setAlpha(0.5F);
      return (View)localObject;
      paramView = ContactsController.getInstance().usersSectionsDict;
      break label452;
      paramViewGroup = ContactsController.getInstance().sortedUsersSectionsArray;
      break label468;
      i = 1;
      break label485;
    }
    label655:
    ((View)localObject).setAlpha(1.0F);
    return (View)localObject;
  }
  
  public int getItemViewType(int paramInt1, int paramInt2)
  {
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label77;
      }
      localArrayList = ContactsController.getInstance().sortedUsersMutualSectionsArray;
      label31:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label90;
      }
      if (paramInt2 >= ((ArrayList)localHashMap.get(localArrayList.get(paramInt1))).size()) {
        break label88;
      }
    }
    label77:
    label88:
    label90:
    do
    {
      return 0;
      localHashMap = ContactsController.getInstance().usersSectionsDict;
      break;
      localArrayList = ContactsController.getInstance().sortedUsersSectionsArray;
      break label31;
      return 4;
      if (paramInt1 == 0)
      {
        if ((this.needPhonebook) || (this.isAdmin))
        {
          if (paramInt2 == 1) {
            return 3;
          }
        }
        else if (paramInt2 == 2) {
          return 3;
        }
        return 2;
      }
      if (paramInt1 - 1 >= localArrayList.size()) {
        break label159;
      }
    } while (paramInt2 < ((ArrayList)localHashMap.get(localArrayList.get(paramInt1 - 1))).size());
    return 4;
    label159:
    return 1;
  }
  
  public int getSectionCount()
  {
    if (this.onlyUsers == 2) {}
    for (ArrayList localArrayList = ContactsController.getInstance().sortedUsersMutualSectionsArray;; localArrayList = ContactsController.getInstance().sortedUsersSectionsArray)
    {
      int j = localArrayList.size();
      int i = j;
      if (this.onlyUsers == 0) {
        i = j + 1;
      }
      j = i;
      if (this.isAdmin) {
        j = i + 1;
      }
      i = j;
      if (this.needPhonebook) {
        i = j + 1;
      }
      return i;
    }
  }
  
  public View getSectionHeaderView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    if (this.onlyUsers == 2)
    {
      paramViewGroup = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label101;
      }
    }
    Object localObject;
    label101:
    for (paramViewGroup = ContactsController.getInstance().sortedUsersMutualSectionsArray;; paramViewGroup = ContactsController.getInstance().sortedUsersSectionsArray)
    {
      localObject = paramView;
      if (paramView == null) {
        localObject = new LetterSectionCell(this.mContext);
      }
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label124;
      }
      if (paramInt >= paramViewGroup.size()) {
        break label111;
      }
      ((LetterSectionCell)localObject).setLetter((String)paramViewGroup.get(paramInt));
      return (View)localObject;
      paramViewGroup = ContactsController.getInstance().usersSectionsDict;
      break;
    }
    label111:
    ((LetterSectionCell)localObject).setLetter("");
    return (View)localObject;
    label124:
    if (paramInt == 0)
    {
      ((LetterSectionCell)localObject).setLetter("");
      return (View)localObject;
    }
    if (paramInt - 1 < paramViewGroup.size())
    {
      ((LetterSectionCell)localObject).setLetter((String)paramViewGroup.get(paramInt - 1));
      return (View)localObject;
    }
    ((LetterSectionCell)localObject).setLetter("");
    return (View)localObject;
  }
  
  public int getViewTypeCount()
  {
    return 5;
  }
  
  public boolean isRowEnabled(int paramInt1, int paramInt2)
  {
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance().usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label77;
      }
      localArrayList = ContactsController.getInstance().sortedUsersMutualSectionsArray;
      label31:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label90;
      }
      if (paramInt2 >= ((ArrayList)localHashMap.get(localArrayList.get(paramInt1))).size()) {
        break label88;
      }
    }
    label77:
    label88:
    label90:
    label115:
    label122:
    do
    {
      do
      {
        do
        {
          return true;
          localHashMap = ContactsController.getInstance().usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance().sortedUsersSectionsArray;
          break label31;
          return false;
          if (paramInt1 != 0) {
            break label122;
          }
          if ((!this.needPhonebook) && (!this.isAdmin)) {
            break label115;
          }
        } while (paramInt2 != 1);
        return false;
      } while (paramInt2 != 3);
      return false;
    } while ((paramInt1 - 1 >= localArrayList.size()) || (paramInt2 < ((ArrayList)localHashMap.get(localArrayList.get(paramInt1 - 1))).size()));
    return false;
  }
  
  public void setCheckedMap(HashMap<Integer, ?> paramHashMap)
  {
    this.checkedMap = paramHashMap;
  }
  
  public void setIsScrolling(boolean paramBoolean)
  {
    this.scrolling = paramBoolean;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Adapters/ContactsAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */