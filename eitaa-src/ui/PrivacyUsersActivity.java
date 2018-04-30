package ir.eitaa.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import ir.eitaa.PhoneFormat.PhoneFormat;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.ActionBarMenu;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Adapters.BaseFragmentAdapter;
import ir.eitaa.ui.Cells.TextInfoCell;
import ir.eitaa.ui.Cells.UserCell;
import java.util.ArrayList;
import java.util.Iterator;

public class PrivacyUsersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int block_user = 1;
  private PrivacyActivityDelegate delegate;
  private boolean isAlwaysShare;
  private boolean isGroup;
  private ListView listView;
  private ListAdapter listViewAdapter;
  private int selectedUserId;
  private ArrayList<Integer> uidArray;
  
  public PrivacyUsersActivity(ArrayList<Integer> paramArrayList, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.uidArray = paramArrayList;
    this.isAlwaysShare = paramBoolean2;
    this.isGroup = paramBoolean1;
  }
  
  private void updateVisibleRows(int paramInt)
  {
    if (this.listView == null) {}
    for (;;)
    {
      return;
      int j = this.listView.getChildCount();
      int i = 0;
      while (i < j)
      {
        View localView = this.listView.getChildAt(i);
        if ((localView instanceof UserCell)) {
          ((UserCell)localView).update(paramInt);
        }
        i += 1;
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    int i = 1;
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    FrameLayout localFrameLayout;
    if (this.isGroup) {
      if (this.isAlwaysShare)
      {
        this.actionBar.setTitle(LocaleController.getString("AlwaysAllow", 2131165281));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
        {
          public void onItemClick(int paramAnonymousInt)
          {
            if (paramAnonymousInt == -1) {
              PrivacyUsersActivity.this.finishFragment();
            }
            while (paramAnonymousInt != 1) {
              return;
            }
            Bundle localBundle = new Bundle();
            if (PrivacyUsersActivity.this.isAlwaysShare) {}
            for (Object localObject = "isAlwaysShare";; localObject = "isNeverShare")
            {
              localBundle.putBoolean((String)localObject, true);
              localBundle.putBoolean("isGroup", PrivacyUsersActivity.this.isGroup);
              localObject = new GroupCreateActivity(localBundle);
              ((GroupCreateActivity)localObject).setDelegate(new GroupCreateActivity.GroupCreateActivityDelegate()
              {
                public void didSelectUsers(ArrayList<Integer> paramAnonymous2ArrayList)
                {
                  paramAnonymous2ArrayList = paramAnonymous2ArrayList.iterator();
                  while (paramAnonymous2ArrayList.hasNext())
                  {
                    Integer localInteger = (Integer)paramAnonymous2ArrayList.next();
                    if (!PrivacyUsersActivity.this.uidArray.contains(localInteger)) {
                      PrivacyUsersActivity.this.uidArray.add(localInteger);
                    }
                  }
                  PrivacyUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                  if (PrivacyUsersActivity.this.delegate != null) {
                    PrivacyUsersActivity.this.delegate.didUpdatedUserList(PrivacyUsersActivity.this.uidArray, true);
                  }
                }
              });
              PrivacyUsersActivity.this.presentFragment((BaseFragment)localObject);
              return;
            }
          }
        });
        this.actionBar.createMenu().addItem(1, 2130837944);
        this.fragmentView = new FrameLayout(paramContext);
        localFrameLayout = (FrameLayout)this.fragmentView;
        Object localObject = new TextView(paramContext);
        ((TextView)localObject).setTextColor(-8355712);
        ((TextView)localObject).setTextSize(20.0F);
        ((TextView)localObject).setGravity(17);
        ((TextView)localObject).setVisibility(4);
        ((TextView)localObject).setText(LocaleController.getString("NoContacts", 2131165935));
        localFrameLayout.addView((View)localObject);
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)((TextView)localObject).getLayoutParams();
        localLayoutParams.width = -1;
        localLayoutParams.height = -1;
        localLayoutParams.gravity = 48;
        ((TextView)localObject).setLayoutParams(localLayoutParams);
        ((TextView)localObject).setOnTouchListener(new View.OnTouchListener()
        {
          public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
          {
            return true;
          }
        });
        this.listView = new ListView(paramContext);
        this.listView.setEmptyView((View)localObject);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        localObject = this.listView;
        paramContext = new ListAdapter(paramContext);
        this.listViewAdapter = paramContext;
        ((ListView)localObject).setAdapter(paramContext);
        paramContext = this.listView;
        if (!LocaleController.isRTL) {
          break label418;
        }
      }
    }
    for (;;)
    {
      paramContext.setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView);
      paramContext = (FrameLayout.LayoutParams)this.listView.getLayoutParams();
      paramContext.width = -1;
      paramContext.height = -1;
      this.listView.setLayoutParams(paramContext);
      this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
        public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
        {
          if (paramAnonymousInt < PrivacyUsersActivity.this.uidArray.size())
          {
            paramAnonymousAdapterView = new Bundle();
            paramAnonymousAdapterView.putInt("user_id", ((Integer)PrivacyUsersActivity.this.uidArray.get(paramAnonymousInt)).intValue());
            PrivacyUsersActivity.this.presentFragment(new ProfileActivity(paramAnonymousAdapterView));
          }
        }
      });
      this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
      {
        public boolean onItemLongClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
        {
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= PrivacyUsersActivity.this.uidArray.size()) || (PrivacyUsersActivity.this.getParentActivity() == null)) {
            return true;
          }
          PrivacyUsersActivity.access$502(PrivacyUsersActivity.this, ((Integer)PrivacyUsersActivity.this.uidArray.get(paramAnonymousInt)).intValue());
          paramAnonymousAdapterView = new AlertDialog.Builder(PrivacyUsersActivity.this.getParentActivity());
          paramAnonymousView = LocaleController.getString("Delete", 2131165560);
          DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              if (paramAnonymous2Int == 0)
              {
                PrivacyUsersActivity.this.uidArray.remove(Integer.valueOf(PrivacyUsersActivity.this.selectedUserId));
                PrivacyUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                if (PrivacyUsersActivity.this.delegate != null) {
                  PrivacyUsersActivity.this.delegate.didUpdatedUserList(PrivacyUsersActivity.this.uidArray, false);
                }
              }
            }
          };
          paramAnonymousAdapterView.setItems(new CharSequence[] { paramAnonymousView }, local1);
          PrivacyUsersActivity.this.showDialog(paramAnonymousAdapterView.create());
          return true;
        }
      });
      return this.fragmentView;
      this.actionBar.setTitle(LocaleController.getString("NeverAllow", 2131165913));
      break;
      if (this.isAlwaysShare)
      {
        this.actionBar.setTitle(LocaleController.getString("AlwaysShareWithTitle", 2131165285));
        break;
      }
      this.actionBar.setTitle(LocaleController.getString("NeverShareWithTitle", 2131165917));
      break;
      label418:
      i = 2;
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.updateInterfaces)
    {
      paramInt = ((Integer)paramVarArgs[0]).intValue();
      if (((paramInt & 0x2) != 0) || ((paramInt & 0x1) != 0)) {
        updateVisibleRows(paramInt);
      }
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
  }
  
  public void setDelegate(PrivacyActivityDelegate paramPrivacyActivityDelegate)
  {
    this.delegate = paramPrivacyActivityDelegate;
  }
  
  private class ListAdapter
    extends BaseFragmentAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public boolean areAllItemsEnabled()
    {
      return false;
    }
    
    public int getCount()
    {
      if (PrivacyUsersActivity.this.uidArray.isEmpty()) {
        return 0;
      }
      return PrivacyUsersActivity.this.uidArray.size() + 1;
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
      if (paramInt == PrivacyUsersActivity.this.uidArray.size()) {
        return 1;
      }
      return 0;
    }
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      int i = getItemViewType(paramInt);
      if (i == 0)
      {
        paramViewGroup = paramView;
        if (paramView == null) {
          paramViewGroup = new UserCell(this.mContext, 1, 0, false);
        }
        TLRPC.User localUser = MessagesController.getInstance().getUser((Integer)PrivacyUsersActivity.this.uidArray.get(paramInt));
        UserCell localUserCell = (UserCell)paramViewGroup;
        if ((localUser.phone != null) && (localUser.phone.length() != 0))
        {
          paramView = PhoneFormat.getInstance().format("+" + localUser.phone);
          localUserCell.setData(localUser, null, paramView, 0);
        }
      }
      do
      {
        do
        {
          return paramViewGroup;
          paramView = LocaleController.getString("NumberUnknown", 2131166046);
          break;
          paramViewGroup = paramView;
        } while (i != 1);
        paramViewGroup = paramView;
      } while (paramView != null);
      paramView = new TextInfoCell(this.mContext);
      ((TextInfoCell)paramView).setText(LocaleController.getString("RemoveFromListText", 2131166155));
      return paramView;
    }
    
    public int getViewTypeCount()
    {
      return 2;
    }
    
    public boolean hasStableIds()
    {
      return false;
    }
    
    public boolean isEmpty()
    {
      return PrivacyUsersActivity.this.uidArray.isEmpty();
    }
    
    public boolean isEnabled(int paramInt)
    {
      return paramInt != PrivacyUsersActivity.this.uidArray.size();
    }
  }
  
  public static abstract interface PrivacyActivityDelegate
  {
    public abstract void didUpdatedUserList(ArrayList<Integer> paramArrayList, boolean paramBoolean);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/PrivacyUsersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */