package ir.eitaa.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.ContactsController;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.PrivacyRule;
import ir.eitaa.tgnet.TLRPC.TL_account_privacyRules;
import ir.eitaa.tgnet.TLRPC.TL_account_setPrivacy;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyKeyChatInvite;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyValueAllowAll;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyValueAllowContacts;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyValueAllowUsers;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyValueDisallowAll;
import ir.eitaa.tgnet.TLRPC.TL_inputPrivacyValueDisallowUsers;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueAllowAll;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueAllowUsers;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueDisallowAll;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.ActionBarMenu;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Adapters.BaseFragmentAdapter;
import ir.eitaa.ui.Cells.HeaderCell;
import ir.eitaa.ui.Cells.RadioCell;
import ir.eitaa.ui.Cells.TextInfoPrivacyCell;
import ir.eitaa.ui.Cells.TextSettingsCell;
import java.util.ArrayList;

public class PrivacyControlActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private int alwaysShareRow;
  private ArrayList<Integer> currentMinus;
  private ArrayList<Integer> currentPlus;
  private int currentType = 0;
  private int detailRow;
  private View doneButton;
  private boolean enableAnimation;
  private int everybodyRow;
  private boolean isGroup;
  private int lastCheckedType = -1;
  private ListAdapter listAdapter;
  private int myContactsRow;
  private int neverShareRow;
  private int nobodyRow;
  private int rowCount;
  private int sectionRow;
  private int shareDetailRow;
  private int shareSectionRow;
  
  public PrivacyControlActivity(boolean paramBoolean)
  {
    this.isGroup = paramBoolean;
  }
  
  private void applyCurrentPrivacySettings()
  {
    TLRPC.TL_account_setPrivacy localTL_account_setPrivacy = new TLRPC.TL_account_setPrivacy();
    if (this.isGroup) {
      localTL_account_setPrivacy.key = new TLRPC.TL_inputPrivacyKeyChatInvite();
    }
    final Object localObject1;
    int i;
    Object localObject2;
    while ((this.currentType != 0) && (this.currentPlus.size() > 0))
    {
      localObject1 = new TLRPC.TL_inputPrivacyValueAllowUsers();
      i = 0;
      for (;;)
      {
        if (i < this.currentPlus.size())
        {
          localObject2 = MessagesController.getInstance().getUser((Integer)this.currentPlus.get(i));
          if (localObject2 != null)
          {
            localObject2 = MessagesController.getInputUser((TLRPC.User)localObject2);
            if (localObject2 != null) {
              ((TLRPC.TL_inputPrivacyValueAllowUsers)localObject1).users.add(localObject2);
            }
          }
          i += 1;
          continue;
          localTL_account_setPrivacy.key = new TLRPC.TL_inputPrivacyKeyStatusTimestamp();
          break;
        }
      }
      localTL_account_setPrivacy.rules.add(localObject1);
    }
    if ((this.currentType != 1) && (this.currentMinus.size() > 0))
    {
      localObject1 = new TLRPC.TL_inputPrivacyValueDisallowUsers();
      i = 0;
      while (i < this.currentMinus.size())
      {
        localObject2 = MessagesController.getInstance().getUser((Integer)this.currentMinus.get(i));
        if (localObject2 != null)
        {
          localObject2 = MessagesController.getInputUser((TLRPC.User)localObject2);
          if (localObject2 != null) {
            ((TLRPC.TL_inputPrivacyValueDisallowUsers)localObject1).users.add(localObject2);
          }
        }
        i += 1;
      }
      localTL_account_setPrivacy.rules.add(localObject1);
    }
    if (this.currentType == 0) {
      localTL_account_setPrivacy.rules.add(new TLRPC.TL_inputPrivacyValueAllowAll());
    }
    for (;;)
    {
      localObject1 = null;
      if (getParentActivity() != null)
      {
        localObject1 = new ProgressDialog(getParentActivity());
        ((ProgressDialog)localObject1).setMessage(LocaleController.getString("Loading", 2131165837));
        ((ProgressDialog)localObject1).setCanceledOnTouchOutside(false);
        ((ProgressDialog)localObject1).setCancelable(false);
        ((ProgressDialog)localObject1).show();
      }
      ConnectionsManager.getInstance().sendRequest(localTL_account_setPrivacy, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              try
              {
                if (PrivacyControlActivity.3.this.val$progressDialogFinal != null) {
                  PrivacyControlActivity.3.this.val$progressDialogFinal.dismiss();
                }
                if (paramAnonymousTL_error == null)
                {
                  PrivacyControlActivity.this.finishFragment();
                  TLRPC.TL_account_privacyRules localTL_account_privacyRules = (TLRPC.TL_account_privacyRules)paramAnonymousTLObject;
                  MessagesController.getInstance().putUsers(localTL_account_privacyRules.users, false);
                  ContactsController.getInstance().setPrivacyRules(localTL_account_privacyRules.rules, PrivacyControlActivity.this.isGroup);
                  return;
                }
              }
              catch (Exception localException)
              {
                for (;;)
                {
                  FileLog.e("TSMS", localException);
                }
                PrivacyControlActivity.this.showErrorAlert();
              }
            }
          });
        }
      }, 2);
      return;
      if (this.currentType == 1) {
        localTL_account_setPrivacy.rules.add(new TLRPC.TL_inputPrivacyValueDisallowAll());
      } else if (this.currentType == 2) {
        localTL_account_setPrivacy.rules.add(new TLRPC.TL_inputPrivacyValueAllowContacts());
      }
    }
  }
  
  private void checkPrivacy()
  {
    this.currentPlus = new ArrayList();
    this.currentMinus = new ArrayList();
    ArrayList localArrayList = ContactsController.getInstance().getPrivacyRules(this.isGroup);
    if (localArrayList.size() == 0)
    {
      this.currentType = 1;
      return;
    }
    int i = -1;
    int j = 0;
    if (j < localArrayList.size())
    {
      TLRPC.PrivacyRule localPrivacyRule = (TLRPC.PrivacyRule)localArrayList.get(j);
      if ((localPrivacyRule instanceof TLRPC.TL_privacyValueAllowUsers)) {
        this.currentPlus.addAll(localPrivacyRule.users);
      }
      for (;;)
      {
        j += 1;
        break;
        if ((localPrivacyRule instanceof TLRPC.TL_privacyValueDisallowUsers)) {
          this.currentMinus.addAll(localPrivacyRule.users);
        } else if ((localPrivacyRule instanceof TLRPC.TL_privacyValueAllowAll)) {
          i = 0;
        } else if ((localPrivacyRule instanceof TLRPC.TL_privacyValueDisallowAll)) {
          i = 1;
        } else {
          i = 2;
        }
      }
    }
    if ((i == 0) || ((i == -1) && (this.currentMinus.size() > 0))) {
      this.currentType = 0;
    }
    for (;;)
    {
      if (this.doneButton != null) {
        this.doneButton.setVisibility(8);
      }
      updateRows();
      return;
      if ((i == 2) || ((i == -1) && (this.currentMinus.size() > 0) && (this.currentPlus.size() > 0))) {
        this.currentType = 2;
      } else if ((i == 1) || ((i == -1) && (this.currentPlus.size() > 0))) {
        this.currentType = 1;
      }
    }
  }
  
  private void showErrorAlert()
  {
    if (getParentActivity() == null) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131165299));
    localBuilder.setMessage(LocaleController.getString("PrivacyFloodControlError", 2131166142));
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131166047), null);
    showDialog(localBuilder.create());
  }
  
  private void updateRows()
  {
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.sectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.everybodyRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.myContactsRow = i;
    if (this.isGroup)
    {
      this.nobodyRow = -1;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.detailRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.shareSectionRow = i;
      if ((this.currentType != 1) && (this.currentType != 2)) {
        break label219;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.alwaysShareRow = i;
      label135:
      if ((this.currentType != 0) && (this.currentType != 2)) {
        break label227;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    label219:
    label227:
    for (this.neverShareRow = i;; this.neverShareRow = -1)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.shareDetailRow = i;
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      return;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.nobodyRow = i;
      break;
      this.alwaysShareRow = -1;
      break label135;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    if (this.isGroup) {
      this.actionBar.setTitle(LocaleController.getString("GroupsAndChannels", 2131165729));
    }
    for (;;)
    {
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            PrivacyControlActivity.this.finishFragment();
          }
          while ((paramAnonymousInt != 1) || (PrivacyControlActivity.this.getParentActivity() == null)) {
            return;
          }
          if ((PrivacyControlActivity.this.currentType != 0) && (!PrivacyControlActivity.this.isGroup))
          {
            final SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
            if (!localSharedPreferences.getBoolean("privacyAlertShowed", false))
            {
              AlertDialog.Builder localBuilder = new AlertDialog.Builder(PrivacyControlActivity.this.getParentActivity());
              if (PrivacyControlActivity.this.isGroup) {
                localBuilder.setMessage(LocaleController.getString("WhoCanAddMeInfo", 2131166402));
              }
              for (;;)
              {
                localBuilder.setTitle(LocaleController.getString("AppName", 2131165299));
                localBuilder.setPositiveButton(LocaleController.getString("OK", 2131166047), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                  {
                    PrivacyControlActivity.this.applyCurrentPrivacySettings();
                    localSharedPreferences.edit().putBoolean("privacyAlertShowed", true).commit();
                  }
                });
                localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131165386), null);
                PrivacyControlActivity.this.showDialog(localBuilder.create());
                return;
                localBuilder.setMessage(LocaleController.getString("CustomHelp", 2131165544));
              }
            }
          }
          PrivacyControlActivity.this.applyCurrentPrivacySettings();
        }
      });
      this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2130837725, AndroidUtilities.dp(56.0F));
      this.doneButton.setVisibility(8);
      this.listAdapter = new ListAdapter(paramContext);
      this.fragmentView = new FrameLayout(paramContext);
      Object localObject = (FrameLayout)this.fragmentView;
      ((FrameLayout)localObject).setBackgroundColor(-986896);
      paramContext = new ListView(paramContext);
      paramContext.setDivider(null);
      paramContext.setDividerHeight(0);
      paramContext.setVerticalScrollBarEnabled(false);
      paramContext.setDrawSelectorOnTop(true);
      ((FrameLayout)localObject).addView(paramContext);
      localObject = (FrameLayout.LayoutParams)paramContext.getLayoutParams();
      ((FrameLayout.LayoutParams)localObject).width = -1;
      ((FrameLayout.LayoutParams)localObject).height = -1;
      ((FrameLayout.LayoutParams)localObject).gravity = 48;
      paramContext.setLayoutParams((ViewGroup.LayoutParams)localObject);
      paramContext.setAdapter(this.listAdapter);
      paramContext.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
        public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, final int paramAnonymousInt, long paramAnonymousLong)
        {
          boolean bool1 = false;
          if ((paramAnonymousInt == PrivacyControlActivity.this.nobodyRow) || (paramAnonymousInt == PrivacyControlActivity.this.everybodyRow) || (paramAnonymousInt == PrivacyControlActivity.this.myContactsRow))
          {
            i = PrivacyControlActivity.this.currentType;
            if (paramAnonymousInt == PrivacyControlActivity.this.nobodyRow)
            {
              i = 1;
              if (i != PrivacyControlActivity.this.currentType) {
                break label106;
              }
            }
          }
          label106:
          while ((paramAnonymousInt != PrivacyControlActivity.this.neverShareRow) && (paramAnonymousInt != PrivacyControlActivity.this.alwaysShareRow))
          {
            int i;
            for (;;)
            {
              return;
              if (paramAnonymousInt == PrivacyControlActivity.this.everybodyRow) {
                i = 0;
              } else if (paramAnonymousInt == PrivacyControlActivity.this.myContactsRow) {
                i = 2;
              }
            }
            PrivacyControlActivity.access$602(PrivacyControlActivity.this, true);
            PrivacyControlActivity.this.doneButton.setVisibility(0);
            PrivacyControlActivity.access$802(PrivacyControlActivity.this, PrivacyControlActivity.this.currentType);
            PrivacyControlActivity.access$002(PrivacyControlActivity.this, i);
            PrivacyControlActivity.this.updateRows();
            return;
          }
          if (paramAnonymousInt == PrivacyControlActivity.this.neverShareRow)
          {
            paramAnonymousAdapterView = PrivacyControlActivity.this.currentMinus;
            if (!paramAnonymousAdapterView.isEmpty()) {
              break label297;
            }
            paramAnonymousView = new Bundle();
            if (paramAnonymousInt != PrivacyControlActivity.this.neverShareRow) {
              break label291;
            }
          }
          label291:
          for (paramAnonymousAdapterView = "isNeverShare";; paramAnonymousAdapterView = "isAlwaysShare")
          {
            paramAnonymousView.putBoolean(paramAnonymousAdapterView, true);
            paramAnonymousView.putBoolean("isGroup", PrivacyControlActivity.this.isGroup);
            paramAnonymousAdapterView = new GroupCreateActivity(paramAnonymousView);
            paramAnonymousAdapterView.setDelegate(new GroupCreateActivity.GroupCreateActivityDelegate()
            {
              public void didSelectUsers(ArrayList<Integer> paramAnonymous2ArrayList)
              {
                if (paramAnonymousInt == PrivacyControlActivity.this.neverShareRow)
                {
                  PrivacyControlActivity.access$1202(PrivacyControlActivity.this, paramAnonymous2ArrayList);
                  i = 0;
                  while (i < PrivacyControlActivity.this.currentMinus.size())
                  {
                    PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(i));
                    i += 1;
                  }
                }
                PrivacyControlActivity.access$1302(PrivacyControlActivity.this, paramAnonymous2ArrayList);
                int i = 0;
                while (i < PrivacyControlActivity.this.currentPlus.size())
                {
                  PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(i));
                  i += 1;
                }
                PrivacyControlActivity.this.doneButton.setVisibility(0);
                PrivacyControlActivity.access$802(PrivacyControlActivity.this, -1);
                PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
              }
            });
            PrivacyControlActivity.this.presentFragment(paramAnonymousAdapterView);
            return;
            paramAnonymousAdapterView = PrivacyControlActivity.this.currentPlus;
            break;
          }
          label297:
          boolean bool2 = PrivacyControlActivity.this.isGroup;
          if (paramAnonymousInt == PrivacyControlActivity.this.alwaysShareRow) {
            bool1 = true;
          }
          paramAnonymousAdapterView = new PrivacyUsersActivity(paramAnonymousAdapterView, bool2, bool1);
          paramAnonymousAdapterView.setDelegate(new PrivacyUsersActivity.PrivacyActivityDelegate()
          {
            public void didUpdatedUserList(ArrayList<Integer> paramAnonymous2ArrayList, boolean paramAnonymous2Boolean)
            {
              int i;
              if (paramAnonymousInt == PrivacyControlActivity.this.neverShareRow)
              {
                PrivacyControlActivity.access$1202(PrivacyControlActivity.this, paramAnonymous2ArrayList);
                if (paramAnonymous2Boolean)
                {
                  i = 0;
                  while (i < PrivacyControlActivity.this.currentMinus.size())
                  {
                    PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(i));
                    i += 1;
                  }
                }
              }
              else
              {
                PrivacyControlActivity.access$1302(PrivacyControlActivity.this, paramAnonymous2ArrayList);
                if (paramAnonymous2Boolean)
                {
                  i = 0;
                  while (i < PrivacyControlActivity.this.currentPlus.size())
                  {
                    PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(i));
                    i += 1;
                  }
                }
              }
              PrivacyControlActivity.this.doneButton.setVisibility(0);
              PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
            }
          });
          PrivacyControlActivity.this.presentFragment(paramAnonymousAdapterView);
        }
      });
      return this.fragmentView;
      this.actionBar.setTitle(LocaleController.getString("PrivacyLastSeen", 2131166143));
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.privacyRulesUpdated) {
      checkPrivacy();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    checkPrivacy();
    updateRows();
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.privacyRulesUpdated);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.privacyRulesUpdated);
  }
  
  public void onResume()
  {
    super.onResume();
    this.lastCheckedType = -1;
    this.enableAnimation = false;
  }
  
  private static class LinkMovementMethodMy
    extends LinkMovementMethod
  {
    public boolean onTouchEvent(TextView paramTextView, Spannable paramSpannable, MotionEvent paramMotionEvent)
    {
      try
      {
        boolean bool = super.onTouchEvent(paramTextView, paramSpannable, paramMotionEvent);
        return bool;
      }
      catch (Exception paramTextView)
      {
        FileLog.e("TSMS", paramTextView);
      }
      return false;
    }
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
      return PrivacyControlActivity.this.rowCount;
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
      if ((paramInt == PrivacyControlActivity.this.alwaysShareRow) || (paramInt == PrivacyControlActivity.this.neverShareRow)) {}
      do
      {
        return 0;
        if ((paramInt == PrivacyControlActivity.this.shareDetailRow) || (paramInt == PrivacyControlActivity.this.detailRow)) {
          return 1;
        }
        if ((paramInt == PrivacyControlActivity.this.sectionRow) || (paramInt == PrivacyControlActivity.this.shareSectionRow)) {
          return 2;
        }
      } while ((paramInt != PrivacyControlActivity.this.everybodyRow) && (paramInt != PrivacyControlActivity.this.myContactsRow) && (paramInt != PrivacyControlActivity.this.nobodyRow));
      return 3;
    }
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      boolean bool2 = true;
      boolean bool1 = true;
      int i = getItemViewType(paramInt);
      Object localObject;
      TextSettingsCell localTextSettingsCell;
      if (i == 0)
      {
        localObject = paramView;
        if (paramView == null)
        {
          localObject = new TextSettingsCell(this.mContext);
          ((View)localObject).setBackgroundColor(-1);
        }
        localTextSettingsCell = (TextSettingsCell)localObject;
        if (paramInt == PrivacyControlActivity.this.alwaysShareRow) {
          if (PrivacyControlActivity.this.currentPlus.size() != 0)
          {
            paramView = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentPlus.size());
            if (!PrivacyControlActivity.this.isGroup) {
              break label151;
            }
            paramViewGroup = LocaleController.getString("AlwaysAllow", 2131165281);
            if (PrivacyControlActivity.this.neverShareRow == -1) {
              break label145;
            }
            label120:
            localTextSettingsCell.setTextAndValue(paramViewGroup, paramView, bool1);
            paramViewGroup = (ViewGroup)localObject;
          }
        }
      }
      label145:
      label151:
      label453:
      label574:
      label726:
      label761:
      do
      {
        do
        {
          do
          {
            do
            {
              do
              {
                return paramViewGroup;
                paramView = LocaleController.getString("EmpryUsersPlaceholder", 2131165603);
                break;
                bool1 = false;
                break label120;
                paramViewGroup = LocaleController.getString("AlwaysShareWith", 2131165283);
                if (PrivacyControlActivity.this.neverShareRow != -1) {}
                for (bool1 = bool2;; bool1 = false)
                {
                  localTextSettingsCell.setTextAndValue(paramViewGroup, paramView, bool1);
                  return (View)localObject;
                }
                paramViewGroup = (ViewGroup)localObject;
              } while (paramInt != PrivacyControlActivity.this.neverShareRow);
              if (PrivacyControlActivity.this.currentMinus.size() != 0) {}
              for (paramView = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentMinus.size()); PrivacyControlActivity.this.isGroup; paramView = LocaleController.getString("EmpryUsersPlaceholder", 2131165603))
              {
                localTextSettingsCell.setTextAndValue(LocaleController.getString("NeverAllow", 2131165913), paramView, false);
                return (View)localObject;
              }
              localTextSettingsCell.setTextAndValue(LocaleController.getString("NeverShareWith", 2131165915), paramView, false);
              return (View)localObject;
              if (i != 1) {
                break label453;
              }
              localObject = paramView;
              if (paramView == null)
              {
                localObject = new TextInfoPrivacyCell(this.mContext);
                ((View)localObject).setBackgroundColor(-1);
              }
              if (paramInt == PrivacyControlActivity.this.detailRow)
              {
                if (PrivacyControlActivity.this.isGroup) {
                  ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("WhoCanAddMeInfo", 2131166402));
                }
                for (;;)
                {
                  ((View)localObject).setBackgroundResource(2130837694);
                  return (View)localObject;
                  ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("CustomHelp", 2131165544));
                }
              }
              paramViewGroup = (ViewGroup)localObject;
            } while (paramInt != PrivacyControlActivity.this.shareDetailRow);
            if (PrivacyControlActivity.this.isGroup) {
              ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("CustomShareInfo", 2131165545));
            }
            for (;;)
            {
              ((View)localObject).setBackgroundResource(2130837695);
              return (View)localObject;
              ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("CustomShareSettingsHelp", 2131165546));
            }
            if (i != 2) {
              break label574;
            }
            localObject = paramView;
            if (paramView == null)
            {
              localObject = new HeaderCell(this.mContext);
              ((View)localObject).setBackgroundColor(-1);
            }
            if (paramInt == PrivacyControlActivity.this.sectionRow)
            {
              if (PrivacyControlActivity.this.isGroup)
              {
                ((HeaderCell)localObject).setText(LocaleController.getString("WhoCanAddMe", 2131166401));
                return (View)localObject;
              }
              ((HeaderCell)localObject).setText(LocaleController.getString("LastSeenTitle", 2131165816));
              return (View)localObject;
            }
            paramViewGroup = (ViewGroup)localObject;
          } while (paramInt != PrivacyControlActivity.this.shareSectionRow);
          ((HeaderCell)localObject).setText(LocaleController.getString("AddExceptions", 2131165259));
          return (View)localObject;
          paramViewGroup = paramView;
        } while (i != 3);
        localObject = paramView;
        if (paramView == null)
        {
          localObject = new RadioCell(this.mContext);
          ((View)localObject).setBackgroundColor(-1);
        }
        paramView = (RadioCell)localObject;
        i = 0;
        if (paramInt == PrivacyControlActivity.this.everybodyRow)
        {
          paramViewGroup = LocaleController.getString("LastSeenEverybody", 2131165798);
          if (PrivacyControlActivity.this.lastCheckedType == 0)
          {
            bool1 = true;
            paramView.setText(paramViewGroup, bool1, true);
            i = 0;
          }
        }
        for (;;)
        {
          if (PrivacyControlActivity.this.lastCheckedType == i)
          {
            paramView.setChecked(false, PrivacyControlActivity.this.enableAnimation);
            return (View)localObject;
            bool1 = false;
            break;
            if (paramInt == PrivacyControlActivity.this.myContactsRow)
            {
              paramViewGroup = LocaleController.getString("LastSeenContacts", 2131165793);
              if (PrivacyControlActivity.this.lastCheckedType == 2)
              {
                bool1 = true;
                if (PrivacyControlActivity.this.nobodyRow == -1) {
                  break label761;
                }
              }
              for (bool2 = true;; bool2 = false)
              {
                paramView.setText(paramViewGroup, bool1, bool2);
                i = 2;
                break;
                bool1 = false;
                break label726;
              }
            }
            if (paramInt == PrivacyControlActivity.this.nobodyRow)
            {
              paramViewGroup = LocaleController.getString("LastSeenNobody", 2131165813);
              if (PrivacyControlActivity.this.lastCheckedType == 1) {}
              for (bool1 = true;; bool1 = false)
              {
                paramView.setText(paramViewGroup, bool1, false);
                i = 1;
                break;
              }
            }
          }
        }
        paramViewGroup = (ViewGroup)localObject;
      } while (PrivacyControlActivity.this.currentType != i);
      paramView.setChecked(true, PrivacyControlActivity.this.enableAnimation);
      return (View)localObject;
    }
    
    public int getViewTypeCount()
    {
      return 4;
    }
    
    public boolean hasStableIds()
    {
      return false;
    }
    
    public boolean isEmpty()
    {
      return false;
    }
    
    public boolean isEnabled(int paramInt)
    {
      return (paramInt == PrivacyControlActivity.this.nobodyRow) || (paramInt == PrivacyControlActivity.this.everybodyRow) || (paramInt == PrivacyControlActivity.this.myContactsRow) || (paramInt == PrivacyControlActivity.this.neverShareRow) || (paramInt == PrivacyControlActivity.this.alwaysShareRow);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/PrivacyControlActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */