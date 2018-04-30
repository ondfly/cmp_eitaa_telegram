package org.telegram.ui.Adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SectionsAdapter;

public class ContactsAdapter
  extends RecyclerListView.SectionsAdapter
{
  private SparseArray<?> checkedMap;
  private int currentAccount = UserConfig.selectedAccount;
  private SparseArray<TLRPC.User> ignoreUsers;
  private boolean isAdmin;
  private Context mContext;
  private boolean needPhonebook;
  private int onlyUsers;
  private boolean scrolling;
  
  public ContactsAdapter(Context paramContext, int paramInt, boolean paramBoolean1, SparseArray<TLRPC.User> paramSparseArray, boolean paramBoolean2)
  {
    this.mContext = paramContext;
    this.onlyUsers = paramInt;
    this.needPhonebook = paramBoolean1;
    this.ignoreUsers = paramSparseArray;
    this.isAdmin = paramBoolean2;
  }
  
  public int getCountForSection(int paramInt)
  {
    int i = 2;
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label121;
      }
      localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;
      label41:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label136;
      }
      if (paramInt >= localArrayList.size()) {
        break label216;
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
    label121:
    label136:
    label160:
    do
    {
      do
      {
        do
        {
          return paramInt;
          localHashMap = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray;
          break label41;
          if (paramInt != 0) {
            break label160;
          }
          paramInt = i;
        } while (this.needPhonebook);
        paramInt = i;
      } while (this.isAdmin);
      return 4;
      if (paramInt - 1 >= localArrayList.size()) {
        break label216;
      }
      i = ((ArrayList)localHashMap.get(localArrayList.get(paramInt - 1))).size();
      if (paramInt - 1 != localArrayList.size() - 1) {
        break label212;
      }
      paramInt = i;
    } while (!this.needPhonebook);
    label212:
    return i + 1;
    label216:
    if (this.needPhonebook) {
      return ContactsController.getInstance(this.currentAccount).phoneBookContacts.size();
    }
    return 0;
  }
  
  public Object getItem(int paramInt1, int paramInt2)
  {
    Object localObject3 = null;
    Object localObject1;
    ArrayList localArrayList;
    label42:
    Object localObject2;
    if (this.onlyUsers == 2)
    {
      localObject1 = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label138;
      }
      localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label153;
      }
      localObject2 = localObject3;
      if (paramInt1 < localArrayList.size())
      {
        localObject1 = (ArrayList)((HashMap)localObject1).get(localArrayList.get(paramInt1));
        localObject2 = localObject3;
        if (paramInt2 < ((ArrayList)localObject1).size()) {
          localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)((ArrayList)localObject1).get(paramInt2)).user_id));
        }
      }
    }
    label138:
    label153:
    label225:
    do
    {
      do
      {
        do
        {
          return localObject2;
          localObject1 = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray;
          break label42;
          localObject2 = localObject3;
        } while (paramInt1 == 0);
        if (paramInt1 - 1 >= localArrayList.size()) {
          break label225;
        }
        localObject1 = (ArrayList)((HashMap)localObject1).get(localArrayList.get(paramInt1 - 1));
        localObject2 = localObject3;
      } while (paramInt2 >= ((ArrayList)localObject1).size());
      return MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)((ArrayList)localObject1).get(paramInt2)).user_id));
      localObject2 = localObject3;
    } while (!this.needPhonebook);
    return ContactsController.getInstance(this.currentAccount).phoneBookContacts.get(paramInt2);
  }
  
  public int getItemViewType(int paramInt1, int paramInt2)
  {
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label89;
      }
      localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;
      label39:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label106;
      }
      if (paramInt2 >= ((ArrayList)localHashMap.get(localArrayList.get(paramInt1))).size()) {
        break label104;
      }
    }
    label89:
    label104:
    label106:
    do
    {
      return 0;
      localHashMap = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
      break;
      localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray;
      break label39;
      return 3;
      if (paramInt1 == 0)
      {
        if (((this.needPhonebook) || (this.isAdmin)) && ((paramInt2 != 1) && (paramInt2 != 3))) {
          break label171;
        }
        return 2;
      }
      if (paramInt1 - 1 >= localArrayList.size()) {
        break label171;
      }
    } while (paramInt2 < ((ArrayList)localHashMap.get(localArrayList.get(paramInt1 - 1))).size());
    return 3;
    label171:
    return 1;
  }
  
  public String getLetter(int paramInt)
  {
    if (this.onlyUsers == 2) {}
    for (ArrayList localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;; localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray)
    {
      int i = getSectionForPosition(paramInt);
      paramInt = i;
      if (i == -1) {
        paramInt = localArrayList.size() - 1;
      }
      if ((paramInt <= 0) || (paramInt > localArrayList.size())) {
        break;
      }
      return (String)localArrayList.get(paramInt - 1);
    }
    return null;
  }
  
  public int getPositionForScrollProgress(float paramFloat)
  {
    return (int)(getItemCount() * paramFloat);
  }
  
  public int getSectionCount()
  {
    if (this.onlyUsers == 2) {}
    for (ArrayList localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;; localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray)
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
  
  public View getSectionHeaderView(int paramInt, View paramView)
  {
    if (this.onlyUsers == 2)
    {
      localObject1 = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label115;
      }
    }
    Object localObject2;
    label115:
    for (Object localObject1 = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;; localObject1 = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray)
    {
      localObject2 = paramView;
      if (paramView == null) {
        localObject2 = new LetterSectionCell(this.mContext);
      }
      paramView = (LetterSectionCell)localObject2;
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label138;
      }
      if (paramInt >= ((ArrayList)localObject1).size()) {
        break label129;
      }
      paramView.setLetter((String)((ArrayList)localObject1).get(paramInt));
      return (View)localObject2;
      localObject1 = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
      break;
    }
    label129:
    paramView.setLetter("");
    return (View)localObject2;
    label138:
    if (paramInt == 0)
    {
      paramView.setLetter("");
      return (View)localObject2;
    }
    if (paramInt - 1 < ((ArrayList)localObject1).size())
    {
      paramView.setLetter((String)((ArrayList)localObject1).get(paramInt - 1));
      return (View)localObject2;
    }
    paramView.setLetter("");
    return (View)localObject2;
  }
  
  public boolean isEnabled(int paramInt1, int paramInt2)
  {
    HashMap localHashMap;
    ArrayList localArrayList;
    if (this.onlyUsers == 2)
    {
      localHashMap = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
      if (this.onlyUsers != 2) {
        break label89;
      }
      localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;
      label39:
      if ((this.onlyUsers == 0) || (this.isAdmin)) {
        break label106;
      }
      if (paramInt2 >= ((ArrayList)localHashMap.get(localArrayList.get(paramInt1))).size()) {
        break label104;
      }
    }
    label89:
    label104:
    label106:
    label131:
    label138:
    do
    {
      do
      {
        do
        {
          return true;
          localHashMap = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
          break;
          localArrayList = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray;
          break label39;
          return false;
          if (paramInt1 != 0) {
            break label138;
          }
          if ((!this.needPhonebook) && (!this.isAdmin)) {
            break label131;
          }
        } while (paramInt2 != 1);
        return false;
      } while (paramInt2 != 3);
      return false;
    } while ((paramInt1 - 1 >= localArrayList.size()) || (paramInt2 < ((ArrayList)localHashMap.get(localArrayList.get(paramInt1 - 1))).size()));
    return false;
  }
  
  public void onBindViewHolder(int paramInt1, int paramInt2, RecyclerView.ViewHolder paramViewHolder)
  {
    switch (paramViewHolder.getItemViewType())
    {
    }
    label57:
    label77:
    label94:
    label169:
    label231:
    label246:
    label252:
    label258:
    label264:
    do
    {
      return;
      UserCell localUserCell = (UserCell)paramViewHolder.itemView;
      int i;
      boolean bool1;
      if (this.onlyUsers == 2)
      {
        paramViewHolder = ContactsController.getInstance(this.currentAccount).usersMutualSectionsDict;
        if (this.onlyUsers != 2) {
          break label231;
        }
        localObject = ContactsController.getInstance(this.currentAccount).sortedUsersMutualSectionsArray;
        if ((this.onlyUsers == 0) || (this.isAdmin)) {
          break label246;
        }
        i = 0;
        paramViewHolder = (ArrayList)paramViewHolder.get(((ArrayList)localObject).get(paramInt1 - i));
        paramViewHolder = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)paramViewHolder.get(paramInt2)).user_id));
        localUserCell.setData(paramViewHolder, null, null, 0);
        if (this.checkedMap != null)
        {
          if (this.checkedMap.indexOfKey(paramViewHolder.id) < 0) {
            break label252;
          }
          bool1 = true;
          if (this.scrolling) {
            break label258;
          }
        }
      }
      for (boolean bool2 = true;; bool2 = false)
      {
        localUserCell.setChecked(bool1, bool2);
        if (this.ignoreUsers == null) {
          break;
        }
        if (this.ignoreUsers.indexOfKey(paramViewHolder.id) < 0) {
          break label264;
        }
        localUserCell.setAlpha(0.5F);
        return;
        paramViewHolder = ContactsController.getInstance(this.currentAccount).usersSectionsDict;
        break label57;
        localObject = ContactsController.getInstance(this.currentAccount).sortedUsersSectionsArray;
        break label77;
        i = 1;
        break label94;
        bool1 = false;
        break label169;
      }
      localUserCell.setAlpha(1.0F);
      return;
      paramViewHolder = (TextCell)paramViewHolder.itemView;
      if (paramInt1 != 0) {
        break;
      }
      if (this.needPhonebook)
      {
        paramViewHolder.setTextAndIcon(LocaleController.getString("InviteFriends", 2131493685), 2131165496);
        return;
      }
      if (this.isAdmin)
      {
        paramViewHolder.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", 2131493696), 2131165496);
        return;
      }
      if (paramInt2 == 0)
      {
        paramViewHolder.setTextAndIcon(LocaleController.getString("NewGroup", 2131493869), 2131165497);
        return;
      }
      if (paramInt2 == 1)
      {
        paramViewHolder.setTextAndIcon(LocaleController.getString("NewSecretChat", 2131493877), 2131165499);
        return;
      }
    } while (paramInt2 != 2);
    paramViewHolder.setTextAndIcon(LocaleController.getString("NewChannel", 2131493867), 2131165491);
    return;
    Object localObject = (ContactsController.Contact)ContactsController.getInstance(this.currentAccount).phoneBookContacts.get(paramInt2);
    if ((((ContactsController.Contact)localObject).first_name != null) && (((ContactsController.Contact)localObject).last_name != null))
    {
      paramViewHolder.setText(((ContactsController.Contact)localObject).first_name + " " + ((ContactsController.Contact)localObject).last_name);
      return;
    }
    if ((((ContactsController.Contact)localObject).first_name != null) && (((ContactsController.Contact)localObject).last_name == null))
    {
      paramViewHolder.setText(((ContactsController.Contact)localObject).first_name);
      return;
    }
    paramViewHolder.setText(((ContactsController.Contact)localObject).last_name);
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    float f2 = 72.0F;
    switch (paramInt)
    {
    default: 
      paramViewGroup = new DividerCell(this.mContext);
      if (LocaleController.isRTL)
      {
        f1 = 28.0F;
        paramInt = AndroidUtilities.dp(f1);
        if (!LocaleController.isRTL) {
          break label161;
        }
      }
      break;
    }
    label161:
    for (float f1 = f2;; f1 = 28.0F)
    {
      paramViewGroup.setPadding(paramInt, 0, AndroidUtilities.dp(f1), 0);
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new UserCell(this.mContext, 58, 1, false);
        continue;
        paramViewGroup = new TextCell(this.mContext);
        continue;
        paramViewGroup = new GraySectionCell(this.mContext);
        ((GraySectionCell)paramViewGroup).setText(LocaleController.getString("Contacts", 2131493290).toUpperCase());
      }
      f1 = 72.0F;
      break;
    }
  }
  
  public void setCheckedMap(SparseArray<?> paramSparseArray)
  {
    this.checkedMap = paramSparseArray;
  }
  
  public void setIsScrolling(boolean paramBoolean)
  {
    this.scrolling = paramBoolean;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/ContactsAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */