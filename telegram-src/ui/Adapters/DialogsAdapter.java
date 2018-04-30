package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.RecentMeUrl;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogMeUrlCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DialogsAdapter
  extends RecyclerListView.SelectionAdapter
{
  private int currentAccount = UserConfig.selectedAccount;
  private int currentCount;
  private int dialogsType;
  private boolean hasHints;
  private boolean isOnlySelect;
  private Context mContext;
  private long openedDialogId;
  private ArrayList<Long> selectedDialogs;
  
  public DialogsAdapter(Context paramContext, int paramInt, boolean paramBoolean)
  {
    this.mContext = paramContext;
    this.dialogsType = paramInt;
    this.isOnlySelect = paramBoolean;
    if ((paramInt == 0) && (!paramBoolean)) {}
    for (boolean bool = true;; bool = false)
    {
      this.hasHints = bool;
      if (paramBoolean) {
        this.selectedDialogs = new ArrayList();
      }
      return;
    }
  }
  
  private ArrayList<TLRPC.TL_dialog> getDialogsArray()
  {
    if (this.dialogsType == 0) {
      return MessagesController.getInstance(this.currentAccount).dialogs;
    }
    if (this.dialogsType == 1) {
      return MessagesController.getInstance(this.currentAccount).dialogsServerOnly;
    }
    if (this.dialogsType == 2) {
      return MessagesController.getInstance(this.currentAccount).dialogsGroupsOnly;
    }
    if (this.dialogsType == 3) {
      return MessagesController.getInstance(this.currentAccount).dialogsForward;
    }
    return null;
  }
  
  public void addOrRemoveSelectedDialog(long paramLong, View paramView)
  {
    if (this.selectedDialogs.contains(Long.valueOf(paramLong)))
    {
      this.selectedDialogs.remove(Long.valueOf(paramLong));
      if ((paramView instanceof DialogCell)) {
        ((DialogCell)paramView).setChecked(false, true);
      }
    }
    do
    {
      return;
      this.selectedDialogs.add(Long.valueOf(paramLong));
    } while (!(paramView instanceof DialogCell));
    ((DialogCell)paramView).setChecked(true, true);
  }
  
  public TLObject getItem(int paramInt)
  {
    ArrayList localArrayList = getDialogsArray();
    int i = paramInt;
    if (this.hasHints)
    {
      i = MessagesController.getInstance(this.currentAccount).hintDialogs.size();
      if (paramInt < i + 2) {
        return (TLObject)MessagesController.getInstance(this.currentAccount).hintDialogs.get(paramInt - 1);
      }
      i = paramInt - (i + 2);
    }
    if ((i < 0) || (i >= localArrayList.size())) {
      return null;
    }
    return (TLObject)localArrayList.get(i);
  }
  
  public int getItemCount()
  {
    int j = getDialogsArray().size();
    if ((j == 0) && (MessagesController.getInstance(this.currentAccount).loadingDialogs)) {
      return 0;
    }
    int i;
    if (MessagesController.getInstance(this.currentAccount).dialogsEndReached)
    {
      i = j;
      if (j != 0) {}
    }
    else
    {
      i = j + 1;
    }
    j = i;
    if (this.hasHints) {
      j = i + (MessagesController.getInstance(this.currentAccount).hintDialogs.size() + 2);
    }
    this.currentCount = j;
    return j;
  }
  
  public int getItemViewType(int paramInt)
  {
    int i = paramInt;
    if (this.hasHints)
    {
      i = MessagesController.getInstance(this.currentAccount).hintDialogs.size();
      if (paramInt < i + 2)
      {
        if (paramInt == 0) {
          return 2;
        }
        if (paramInt == i + 1) {
          return 3;
        }
        return 4;
      }
      i = paramInt - (i + 2);
    }
    if (i == getDialogsArray().size())
    {
      if (!MessagesController.getInstance(this.currentAccount).dialogsEndReached) {
        return 1;
      }
      return 5;
    }
    return 0;
  }
  
  public ArrayList<Long> getSelectedDialogs()
  {
    return this.selectedDialogs;
  }
  
  public boolean hasSelectedDialogs()
  {
    return (this.selectedDialogs != null) && (!this.selectedDialogs.isEmpty());
  }
  
  public boolean isDataSetChanged()
  {
    int i = this.currentCount;
    return (i != getItemCount()) || (i == 1);
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    int i = paramViewHolder.getItemViewType();
    return (i != 1) && (i != 5) && (i != 3);
  }
  
  public void notifyDataSetChanged()
  {
    if ((this.dialogsType == 0) && (!this.isOnlySelect) && (!MessagesController.getInstance(this.currentAccount).hintDialogs.isEmpty())) {}
    for (boolean bool = true;; bool = false)
    {
      this.hasHints = bool;
      super.notifyDataSetChanged();
      return;
    }
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    boolean bool2 = true;
    switch (paramViewHolder.getItemViewType())
    {
    default: 
      return;
    case 0: 
      paramViewHolder = (DialogCell)paramViewHolder.itemView;
      TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)getItem(paramInt);
      int i = paramInt;
      if (this.hasHints) {
        i = paramInt - (MessagesController.getInstance(this.currentAccount).hintDialogs.size() + 2);
      }
      if (i != getItemCount() - 1)
      {
        bool1 = true;
        paramViewHolder.useSeparator = bool1;
        if ((this.dialogsType == 0) && (AndroidUtilities.isTablet())) {
          if (localTL_dialog.id != this.openedDialogId) {
            break label178;
          }
        }
      }
      label178:
      for (boolean bool1 = bool2;; bool1 = false)
      {
        paramViewHolder.setDialogSelected(bool1);
        if (this.selectedDialogs != null) {
          paramViewHolder.setChecked(this.selectedDialogs.contains(Long.valueOf(localTL_dialog.id)), false);
        }
        paramViewHolder.setDialog(localTL_dialog, i, this.dialogsType);
        return;
        bool1 = false;
        break;
      }
    }
    ((DialogMeUrlCell)paramViewHolder.itemView).setRecentMeUrl((TLRPC.RecentMeUrl)getItem(paramInt));
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    switch (paramInt)
    {
    default: 
      paramViewGroup = new DialogsEmptyCell(this.mContext);
      if (paramInt != 5) {
        break;
      }
    }
    for (paramInt = -1;; paramInt = -2)
    {
      paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, paramInt));
      return new RecyclerListView.Holder(paramViewGroup);
      paramViewGroup = new DialogCell(this.mContext, this.isOnlySelect);
      break;
      paramViewGroup = new LoadingCell(this.mContext);
      break;
      paramViewGroup = new HeaderCell(this.mContext);
      paramViewGroup.setText(LocaleController.getString("RecentlyViewed", 2131494217));
      Object localObject = new TextView(this.mContext);
      ((TextView)localObject).setTextSize(1, 15.0F);
      ((TextView)localObject).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      ((TextView)localObject).setTextColor(Theme.getColor("windowBackgroundWhiteBlueHeader"));
      ((TextView)localObject).setText(LocaleController.getString("RecentlyViewedHide", 2131494218));
      if (LocaleController.isRTL)
      {
        i = 3;
        label197:
        ((TextView)localObject).setGravity(i | 0x10);
        if (!LocaleController.isRTL) {
          break label261;
        }
      }
      label261:
      for (int i = 3;; i = 5)
      {
        paramViewGroup.addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, i | 0x30, 17.0F, 15.0F, 17.0F, 0.0F));
        ((TextView)localObject).setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            MessagesController.getInstance(DialogsAdapter.this.currentAccount).hintDialogs.clear();
            MessagesController.getGlobalMainSettings().edit().remove("installReferer").commit();
            DialogsAdapter.this.notifyDataSetChanged();
          }
        });
        break;
        i = 5;
        break label197;
      }
      paramViewGroup = new FrameLayout(this.mContext)
      {
        protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
        {
          super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramAnonymousInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(12.0F), 1073741824));
        }
      };
      paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
      localObject = new View(this.mContext);
      ((View)localObject).setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
      paramViewGroup.addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F));
      break;
      paramViewGroup = new DialogMeUrlCell(this.mContext);
      break;
    }
  }
  
  public void onViewAttachedToWindow(RecyclerView.ViewHolder paramViewHolder)
  {
    if ((paramViewHolder.itemView instanceof DialogCell)) {
      ((DialogCell)paramViewHolder.itemView).checkCurrentDialogIndex();
    }
  }
  
  public void setOpenedDialogId(long paramLong)
  {
    this.openedDialogId = paramLong;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/DialogsAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */