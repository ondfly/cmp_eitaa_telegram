package ir.eitaa.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.ContactsController;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.messenger.UserConfig;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.PrivacyRule;
import ir.eitaa.tgnet.TLRPC.TL_accountDaysTTL;
import ir.eitaa.tgnet.TLRPC.TL_account_setAccountTTL;
import ir.eitaa.tgnet.TLRPC.TL_boolTrue;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueAllowAll;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueAllowUsers;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueDisallowAll;
import ir.eitaa.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Adapters.BaseFragmentAdapter;
import ir.eitaa.ui.Cells.HeaderCell;
import ir.eitaa.ui.Cells.TextCheckCell;
import ir.eitaa.ui.Cells.TextInfoPrivacyCell;
import ir.eitaa.ui.Cells.TextSettingsCell;
import ir.eitaa.ui.Components.LayoutHelper;
import java.util.ArrayList;

public class PrivacySettingsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private int blockedRow;
  private int deleteAccountDetailRow;
  private int deleteAccountRow;
  private int deleteAccountSectionRow;
  private int groupsDetailRow;
  private int groupsRow;
  private int lastSeenRow;
  private ListAdapter listAdapter;
  private int passcodeRow;
  private int passwordRow;
  private int privacySectionRow;
  private int rowCount;
  private int secretDetailRow;
  private int secretSectionRow;
  private int secretWebpageRow;
  private int securitySectionRow;
  private int sessionsDetailRow;
  private int sessionsRow;
  
  private String formatRulesString(boolean paramBoolean)
  {
    ArrayList localArrayList = ContactsController.getInstance().getPrivacyRules(paramBoolean);
    if (localArrayList.size() == 0) {
      return LocaleController.getString("LastSeenNobody", 2131165813);
    }
    int i = -1;
    int k = 0;
    int m = 0;
    int j = 0;
    if (j < localArrayList.size())
    {
      TLRPC.PrivacyRule localPrivacyRule = (TLRPC.PrivacyRule)localArrayList.get(j);
      if ((localPrivacyRule instanceof TLRPC.TL_privacyValueAllowUsers)) {
        k += localPrivacyRule.users.size();
      }
      for (;;)
      {
        j += 1;
        break;
        if ((localPrivacyRule instanceof TLRPC.TL_privacyValueDisallowUsers)) {
          m += localPrivacyRule.users.size();
        } else if ((localPrivacyRule instanceof TLRPC.TL_privacyValueAllowAll)) {
          i = 0;
        } else if ((localPrivacyRule instanceof TLRPC.TL_privacyValueDisallowAll)) {
          i = 1;
        } else {
          i = 2;
        }
      }
    }
    if ((i == 0) || ((i == -1) && (m > 0)))
    {
      if (m == 0) {
        return LocaleController.getString("LastSeenEverybody", 2131165798);
      }
      return LocaleController.formatString("LastSeenEverybodyMinus", 2131165799, new Object[] { Integer.valueOf(m) });
    }
    if ((i == 2) || ((i == -1) && (m > 0) && (k > 0)))
    {
      if ((k == 0) && (m == 0)) {
        return LocaleController.getString("LastSeenContacts", 2131165793);
      }
      if ((k != 0) && (m != 0)) {
        return LocaleController.formatString("LastSeenContactsMinusPlus", 2131165795, new Object[] { Integer.valueOf(m), Integer.valueOf(k) });
      }
      if (m != 0) {
        return LocaleController.formatString("LastSeenContactsMinus", 2131165794, new Object[] { Integer.valueOf(m) });
      }
      return LocaleController.formatString("LastSeenContactsPlus", 2131165796, new Object[] { Integer.valueOf(k) });
    }
    if ((i == 1) || (k > 0))
    {
      if (k == 0) {
        return LocaleController.getString("LastSeenNobody", 2131165813);
      }
      return LocaleController.formatString("LastSeenNobodyPlus", 2131165814, new Object[] { Integer.valueOf(k) });
    }
    return "unknown";
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("PrivacySettings", 2131166146));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          PrivacySettingsActivity.this.finishFragment();
        }
      }
    });
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    localFrameLayout.setBackgroundColor(-986896);
    paramContext = new ListView(paramContext);
    paramContext.setDivider(null);
    paramContext.setDividerHeight(0);
    paramContext.setVerticalScrollBarEnabled(false);
    paramContext.setDrawSelectorOnTop(true);
    localFrameLayout.addView(paramContext, LayoutHelper.createFrame(-1, -1.0F));
    paramContext.setAdapter(this.listAdapter);
    paramContext.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
      {
        boolean bool = true;
        if (paramAnonymousInt == PrivacySettingsActivity.this.blockedRow) {
          PrivacySettingsActivity.this.presentFragment(new BlockedUsersActivity());
        }
        do
        {
          do
          {
            do
            {
              return;
              if (paramAnonymousInt == PrivacySettingsActivity.this.sessionsRow)
              {
                PrivacySettingsActivity.this.presentFragment(new SessionsActivity());
                return;
              }
              if (paramAnonymousInt != PrivacySettingsActivity.this.deleteAccountRow) {
                break;
              }
            } while (PrivacySettingsActivity.this.getParentActivity() == null);
            paramAnonymousAdapterView = new AlertDialog.Builder(PrivacySettingsActivity.this.getParentActivity());
            paramAnonymousAdapterView.setTitle(LocaleController.getString("DeleteAccountTitle", 2131165564));
            paramAnonymousView = LocaleController.formatPluralString("Months", 1);
            String str1 = LocaleController.formatPluralString("Months", 3);
            String str2 = LocaleController.formatPluralString("Months", 6);
            String str3 = LocaleController.formatPluralString("Years", 1);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(final DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = 0;
                if (paramAnonymous2Int == 0) {
                  i = 30;
                }
                for (;;)
                {
                  paramAnonymous2DialogInterface = new ProgressDialog(PrivacySettingsActivity.this.getParentActivity());
                  paramAnonymous2DialogInterface.setMessage(LocaleController.getString("Loading", 2131165837));
                  paramAnonymous2DialogInterface.setCanceledOnTouchOutside(false);
                  paramAnonymous2DialogInterface.setCancelable(false);
                  paramAnonymous2DialogInterface.show();
                  final TLRPC.TL_account_setAccountTTL localTL_account_setAccountTTL = new TLRPC.TL_account_setAccountTTL();
                  localTL_account_setAccountTTL.ttl = new TLRPC.TL_accountDaysTTL();
                  localTL_account_setAccountTTL.ttl.days = i;
                  ConnectionsManager.getInstance().sendRequest(localTL_account_setAccountTTL, new RequestDelegate()
                  {
                    public void run(final TLObject paramAnonymous3TLObject, TLRPC.TL_error paramAnonymous3TL_error)
                    {
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          try
                          {
                            PrivacySettingsActivity.2.1.1.this.val$progressDialog.dismiss();
                            if ((paramAnonymous3TLObject instanceof TLRPC.TL_boolTrue))
                            {
                              ContactsController.getInstance().setDeleteAccountTTL(PrivacySettingsActivity.2.1.1.this.val$req.ttl.days);
                              PrivacySettingsActivity.this.listAdapter.notifyDataSetChanged();
                            }
                            return;
                          }
                          catch (Exception localException)
                          {
                            for (;;)
                            {
                              FileLog.e("TSMS", localException);
                            }
                          }
                        }
                      });
                    }
                  });
                  return;
                  if (paramAnonymous2Int == 1) {
                    i = 90;
                  } else if (paramAnonymous2Int == 2) {
                    i = 182;
                  } else if (paramAnonymous2Int == 3) {
                    i = 365;
                  }
                }
              }
            };
            paramAnonymousAdapterView.setItems(new CharSequence[] { paramAnonymousView, str1, str2, str3 }, local1);
            paramAnonymousAdapterView.setNegativeButton(LocaleController.getString("Cancel", 2131165386), null);
            PrivacySettingsActivity.this.showDialog(paramAnonymousAdapterView.create());
            return;
            if (paramAnonymousInt == PrivacySettingsActivity.this.lastSeenRow)
            {
              PrivacySettingsActivity.this.presentFragment(new PrivacyControlActivity(false));
              return;
            }
            if (paramAnonymousInt == PrivacySettingsActivity.this.groupsRow)
            {
              PrivacySettingsActivity.this.presentFragment(new PrivacyControlActivity(true));
              return;
            }
            if (paramAnonymousInt == PrivacySettingsActivity.this.passwordRow)
            {
              PrivacySettingsActivity.this.presentFragment(new TwoStepVerificationActivity(0));
              return;
            }
            if (paramAnonymousInt == PrivacySettingsActivity.this.passcodeRow)
            {
              if (UserConfig.passcodeHash.length() > 0)
              {
                PrivacySettingsActivity.this.presentFragment(new PasscodeActivity(2));
                return;
              }
              PrivacySettingsActivity.this.presentFragment(new PasscodeActivity(0));
              return;
            }
          } while (paramAnonymousInt != PrivacySettingsActivity.this.secretWebpageRow);
          if (MessagesController.getInstance().secretWebpagePreview != 1) {
            break;
          }
          MessagesController.getInstance().secretWebpagePreview = 0;
          ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit().putInt("secretWebpage2", MessagesController.getInstance().secretWebpagePreview).commit();
        } while (!(paramAnonymousView instanceof TextCheckCell));
        paramAnonymousAdapterView = (TextCheckCell)paramAnonymousView;
        if (MessagesController.getInstance().secretWebpagePreview == 1) {}
        for (;;)
        {
          paramAnonymousAdapterView.setChecked(bool);
          return;
          MessagesController.getInstance().secretWebpagePreview = 1;
          break;
          bool = false;
        }
      }
    });
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if ((paramInt == NotificationCenter.privacyRulesUpdated) && (this.listAdapter != null)) {
      this.listAdapter.notifyDataSetChanged();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    ContactsController.getInstance().loadPrivacySettings();
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.privacySectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.blockedRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.lastSeenRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.groupsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.groupsDetailRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.securitySectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.passcodeRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.passwordRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.sessionsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.sessionsDetailRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.deleteAccountSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.deleteAccountRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.deleteAccountDetailRow = i;
    if (MessagesController.getInstance().secretWebpagePreview != 1)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.secretSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.secretWebpageRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    for (this.secretDetailRow = i;; this.secretDetailRow = -1)
    {
      NotificationCenter.getInstance().addObserver(this, NotificationCenter.privacyRulesUpdated);
      return true;
      this.secretSectionRow = -1;
      this.secretWebpageRow = -1;
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.privacyRulesUpdated);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
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
      return PrivacySettingsActivity.this.rowCount;
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
      if ((paramInt == PrivacySettingsActivity.this.lastSeenRow) || (paramInt == PrivacySettingsActivity.this.blockedRow) || (paramInt == PrivacySettingsActivity.this.deleteAccountRow) || (paramInt == PrivacySettingsActivity.this.sessionsRow) || (paramInt == PrivacySettingsActivity.this.passwordRow) || (paramInt == PrivacySettingsActivity.this.passcodeRow) || (paramInt == PrivacySettingsActivity.this.groupsRow)) {}
      do
      {
        return 0;
        if ((paramInt == PrivacySettingsActivity.this.deleteAccountDetailRow) || (paramInt == PrivacySettingsActivity.this.groupsDetailRow) || (paramInt == PrivacySettingsActivity.this.sessionsDetailRow) || (paramInt == PrivacySettingsActivity.this.secretDetailRow)) {
          return 1;
        }
        if ((paramInt == PrivacySettingsActivity.this.securitySectionRow) || (paramInt == PrivacySettingsActivity.this.deleteAccountSectionRow) || (paramInt == PrivacySettingsActivity.this.privacySectionRow) || (paramInt == PrivacySettingsActivity.this.secretSectionRow)) {
          return 2;
        }
      } while (paramInt != PrivacySettingsActivity.this.secretWebpageRow);
      return 3;
    }
    
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
      boolean bool = false;
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
        if (paramInt == PrivacySettingsActivity.this.blockedRow)
        {
          localTextSettingsCell.setText(LocaleController.getString("BlockedUsers", 2131165360), true);
          paramViewGroup = (ViewGroup)localObject;
        }
      }
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
                if (paramInt == PrivacySettingsActivity.this.sessionsRow)
                {
                  localTextSettingsCell.setText(LocaleController.getString("SessionsTitle", 2131166261), false);
                  return (View)localObject;
                }
                if (paramInt == PrivacySettingsActivity.this.passwordRow)
                {
                  localTextSettingsCell.setText(LocaleController.getString("TwoStepVerification", 2131166349), true);
                  return (View)localObject;
                }
                if (paramInt == PrivacySettingsActivity.this.passcodeRow)
                {
                  localTextSettingsCell.setText(LocaleController.getString("Passcode", 2131166086), true);
                  return (View)localObject;
                }
                if (paramInt == PrivacySettingsActivity.this.lastSeenRow)
                {
                  if (ContactsController.getInstance().getLoadingLastSeenInfo()) {}
                  for (paramView = LocaleController.getString("Loading", 2131165837);; paramView = PrivacySettingsActivity.this.formatRulesString(false))
                  {
                    localTextSettingsCell.setTextAndValue(LocaleController.getString("PrivacyLastSeen", 2131166143), paramView, true);
                    return (View)localObject;
                  }
                }
                if (paramInt == PrivacySettingsActivity.this.groupsRow)
                {
                  if (ContactsController.getInstance().getLoadingGroupInfo()) {}
                  for (paramView = LocaleController.getString("Loading", 2131165837);; paramView = PrivacySettingsActivity.this.formatRulesString(true))
                  {
                    localTextSettingsCell.setTextAndValue(LocaleController.getString("GroupsAndChannels", 2131165729), paramView, false);
                    return (View)localObject;
                  }
                }
                paramViewGroup = (ViewGroup)localObject;
              } while (paramInt != PrivacySettingsActivity.this.deleteAccountRow);
              if (ContactsController.getInstance().getLoadingDeleteInfo()) {
                paramView = LocaleController.getString("Loading", 2131165837);
              }
              for (;;)
              {
                localTextSettingsCell.setTextAndValue(LocaleController.getString("DeleteAccountIfAwayFor", 2131165562), paramView, false);
                return (View)localObject;
                paramInt = ContactsController.getInstance().getDeleteAccountTTL();
                if (paramInt <= 182) {
                  paramView = LocaleController.formatPluralString("Months", paramInt / 30);
                } else if (paramInt == 365) {
                  paramView = LocaleController.formatPluralString("Years", paramInt / 365);
                } else {
                  paramView = LocaleController.formatPluralString("Days", paramInt);
                }
              }
              if (i != 1) {
                break;
              }
              localObject = paramView;
              if (paramView == null) {
                localObject = new TextInfoPrivacyCell(this.mContext);
              }
              if (paramInt == PrivacySettingsActivity.this.deleteAccountDetailRow)
              {
                ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("DeleteAccountHelp", 2131165561));
                if (PrivacySettingsActivity.this.secretSectionRow == -1) {}
                for (paramInt = 2130837695;; paramInt = 2130837694)
                {
                  ((View)localObject).setBackgroundResource(paramInt);
                  return (View)localObject;
                }
              }
              if (paramInt == PrivacySettingsActivity.this.groupsDetailRow)
              {
                ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("GroupsAndChannelsHelp", 2131165730));
                ((View)localObject).setBackgroundResource(2130837694);
                return (View)localObject;
              }
              if (paramInt == PrivacySettingsActivity.this.sessionsDetailRow)
              {
                ((TextInfoPrivacyCell)localObject).setText(LocaleController.getString("SessionsInfo", 2131166260));
                ((View)localObject).setBackgroundResource(2130837694);
                return (View)localObject;
              }
              paramViewGroup = (ViewGroup)localObject;
            } while (paramInt != PrivacySettingsActivity.this.secretDetailRow);
            ((TextInfoPrivacyCell)localObject).setText("");
            ((View)localObject).setBackgroundResource(2130837695);
            return (View)localObject;
            if (i != 2) {
              break;
            }
            localObject = paramView;
            if (paramView == null)
            {
              localObject = new HeaderCell(this.mContext);
              ((View)localObject).setBackgroundColor(-1);
            }
            if (paramInt == PrivacySettingsActivity.this.privacySectionRow)
            {
              ((HeaderCell)localObject).setText(LocaleController.getString("PrivacyTitle", 2131166147));
              return (View)localObject;
            }
            if (paramInt == PrivacySettingsActivity.this.securitySectionRow)
            {
              ((HeaderCell)localObject).setText(LocaleController.getString("SecurityTitle", 2131166232));
              return (View)localObject;
            }
            if (paramInt == PrivacySettingsActivity.this.deleteAccountSectionRow)
            {
              ((HeaderCell)localObject).setText(LocaleController.getString("DeleteAccountTitle", 2131165564));
              return (View)localObject;
            }
            paramViewGroup = (ViewGroup)localObject;
          } while (paramInt != PrivacySettingsActivity.this.secretSectionRow);
          ((HeaderCell)localObject).setText(LocaleController.getString("SecretChat", 2131166228));
          return (View)localObject;
          paramViewGroup = paramView;
        } while (i != 3);
        localObject = paramView;
        if (paramView == null)
        {
          localObject = new TextCheckCell(this.mContext);
          ((View)localObject).setBackgroundColor(-1);
        }
        paramView = (TextCheckCell)localObject;
        paramViewGroup = (ViewGroup)localObject;
      } while (paramInt != PrivacySettingsActivity.this.secretWebpageRow);
      paramViewGroup = LocaleController.getString("SecretWebPage", 2131166231);
      if (MessagesController.getInstance().secretWebpagePreview == 1) {
        bool = true;
      }
      paramView.setTextAndCheck(paramViewGroup, bool, true);
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
      return (paramInt == PrivacySettingsActivity.this.passcodeRow) || (paramInt == PrivacySettingsActivity.this.passwordRow) || (paramInt == PrivacySettingsActivity.this.blockedRow) || (paramInt == PrivacySettingsActivity.this.sessionsRow) || (paramInt == PrivacySettingsActivity.this.secretWebpageRow) || ((paramInt == PrivacySettingsActivity.this.groupsRow) && (!ContactsController.getInstance().getLoadingGroupInfo())) || ((paramInt == PrivacySettingsActivity.this.lastSeenRow) && (!ContactsController.getInstance().getLoadingLastSeenInfo())) || ((paramInt == PrivacySettingsActivity.this.deleteAccountRow) && (!ContactsController.getInstance().getLoadingDeleteInfo()));
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/PrivacySettingsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */