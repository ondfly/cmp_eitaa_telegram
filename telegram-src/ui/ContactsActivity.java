package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ContactsController.Contact;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.ContactsAdapter;
import org.telegram.ui.Adapters.SearchAdapter;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;

public class ContactsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int add_button = 1;
  private static final int search_button = 0;
  private ActionBarMenuItem addItem;
  private boolean addingToChannel;
  private boolean allowBots = true;
  private boolean allowUsernameSearch = true;
  private int chat_id;
  private boolean checkPermission = true;
  private boolean createSecretChat;
  private boolean creatingChat;
  private ContactsActivityDelegate delegate;
  private boolean destroyAfterSelect;
  private EmptyTextProgressView emptyView;
  private SparseArray<TLRPC.User> ignoreUsers;
  private RecyclerListView listView;
  private ContactsAdapter listViewAdapter;
  private boolean needFinishFragment = true;
  private boolean needForwardCount = true;
  private boolean needPhonebook;
  private boolean onlyUsers;
  private AlertDialog permissionDialog;
  private boolean returnAsResult;
  private SearchAdapter searchListViewAdapter;
  private boolean searchWas;
  private boolean searching;
  private String selectAlertString = null;
  
  public ContactsActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  @TargetApi(23)
  private void askForPermissons()
  {
    Activity localActivity = getParentActivity();
    if ((localActivity == null) || (localActivity.checkSelfPermission("android.permission.READ_CONTACTS") == 0)) {
      return;
    }
    ArrayList localArrayList = new ArrayList();
    localArrayList.add("android.permission.READ_CONTACTS");
    localArrayList.add("android.permission.WRITE_CONTACTS");
    localArrayList.add("android.permission.GET_ACCOUNTS");
    localActivity.requestPermissions((String[])localArrayList.toArray(new String[localArrayList.size()]), 1);
  }
  
  private void didSelectResult(final TLRPC.User paramUser, boolean paramBoolean, final String paramString)
  {
    if ((paramBoolean) && (this.selectAlertString != null)) {
      if (getParentActivity() != null) {}
    }
    do
    {
      do
      {
        return;
        if ((paramUser.bot) && (paramUser.bot_nochats) && (!this.addingToChannel)) {
          try
          {
            Toast.makeText(getParentActivity(), LocaleController.getString("BotCantJoinGroups", 2131493087), 0).show();
            return;
          }
          catch (Exception paramUser)
          {
            FileLog.e(paramUser);
            return;
          }
        }
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
        localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
        String str2 = LocaleController.formatStringSimple(this.selectAlertString, new Object[] { UserObject.getUserName(paramUser) });
        Object localObject = null;
        paramString = (String)localObject;
        String str1 = str2;
        if (!paramUser.bot)
        {
          paramString = (String)localObject;
          str1 = str2;
          if (this.needForwardCount)
          {
            str1 = String.format("%s\n\n%s", new Object[] { str2, LocaleController.getString("AddToTheGroupForwardCount", 2131492945) });
            paramString = new EditText(getParentActivity());
            paramString.setTextSize(1, 18.0F);
            paramString.setText("50");
            paramString.setTextColor(Theme.getColor("dialogTextBlack"));
            paramString.setGravity(17);
            paramString.setInputType(2);
            paramString.setImeOptions(6);
            paramString.setBackgroundDrawable(Theme.createEditTextDrawable(getParentActivity(), true));
            paramString.addTextChangedListener(new TextWatcher()
            {
              public void afterTextChanged(Editable paramAnonymousEditable)
              {
                int i;
                try
                {
                  paramAnonymousEditable = paramAnonymousEditable.toString();
                  if (paramAnonymousEditable.length() == 0) {
                    return;
                  }
                  i = Utilities.parseInt(paramAnonymousEditable).intValue();
                  if (i < 0)
                  {
                    paramString.setText("0");
                    paramString.setSelection(paramString.length());
                    return;
                  }
                  if (i > 300)
                  {
                    paramString.setText("300");
                    paramString.setSelection(paramString.length());
                    return;
                  }
                }
                catch (Exception paramAnonymousEditable)
                {
                  FileLog.e(paramAnonymousEditable);
                  return;
                }
                if (!paramAnonymousEditable.equals("" + i))
                {
                  paramString.setText("" + i);
                  paramString.setSelection(paramString.length());
                }
              }
              
              public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
              
              public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
            });
            localBuilder.setView(paramString);
          }
        }
        localBuilder.setMessage(str1);
        localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            ContactsActivity localContactsActivity = ContactsActivity.this;
            TLRPC.User localUser = paramUser;
            if (paramString != null) {}
            for (paramAnonymousDialogInterface = paramString.getText().toString();; paramAnonymousDialogInterface = "0")
            {
              localContactsActivity.didSelectResult(localUser, false, paramAnonymousDialogInterface);
              return;
            }
          }
        });
        localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        showDialog(localBuilder.create());
      } while (paramString == null);
      paramUser = (ViewGroup.MarginLayoutParams)paramString.getLayoutParams();
      if (paramUser != null)
      {
        if ((paramUser instanceof FrameLayout.LayoutParams)) {
          ((FrameLayout.LayoutParams)paramUser).gravity = 1;
        }
        int i = AndroidUtilities.dp(24.0F);
        paramUser.leftMargin = i;
        paramUser.rightMargin = i;
        paramUser.height = AndroidUtilities.dp(36.0F);
        paramString.setLayoutParams(paramUser);
      }
      paramString.setSelection(paramString.getText().length());
      return;
      if (this.delegate != null)
      {
        this.delegate.didSelectContact(paramUser, paramString, this);
        this.delegate = null;
      }
    } while (!this.needFinishFragment);
    finishFragment();
  }
  
  private void updateVisibleRows(int paramInt)
  {
    if (this.listView != null)
    {
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
    this.searching = false;
    this.searchWas = false;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    Object localObject;
    int i;
    label184:
    boolean bool2;
    if (this.destroyAfterSelect) {
      if (this.returnAsResult)
      {
        this.actionBar.setTitle(LocaleController.getString("SelectContact", 2131494329));
        this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
        {
          public void onItemClick(int paramAnonymousInt)
          {
            if (paramAnonymousInt == -1) {
              ContactsActivity.this.finishFragment();
            }
            while (paramAnonymousInt != 1) {
              return;
            }
            ContactsActivity.this.presentFragment(new NewContactActivity());
          }
        });
        localObject = this.actionBar.createMenu();
        ((ActionBarMenu)localObject).addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
        {
          public void onSearchCollapse()
          {
            if (ContactsActivity.this.addItem != null) {
              ContactsActivity.this.addItem.setVisibility(0);
            }
            ContactsActivity.this.searchListViewAdapter.searchDialogs(null);
            ContactsActivity.access$002(ContactsActivity.this, false);
            ContactsActivity.access$302(ContactsActivity.this, false);
            ContactsActivity.this.listView.setAdapter(ContactsActivity.this.listViewAdapter);
            ContactsActivity.this.listViewAdapter.notifyDataSetChanged();
            ContactsActivity.this.listView.setFastScrollVisible(true);
            ContactsActivity.this.listView.setVerticalScrollBarEnabled(false);
            ContactsActivity.this.emptyView.setText(LocaleController.getString("NoContacts", 2131493887));
          }
          
          public void onSearchExpand()
          {
            ContactsActivity.access$002(ContactsActivity.this, true);
            if (ContactsActivity.this.addItem != null) {
              ContactsActivity.this.addItem.setVisibility(8);
            }
          }
          
          public void onTextChanged(EditText paramAnonymousEditText)
          {
            if (ContactsActivity.this.searchListViewAdapter == null) {
              return;
            }
            paramAnonymousEditText = paramAnonymousEditText.getText().toString();
            if (paramAnonymousEditText.length() != 0)
            {
              ContactsActivity.access$302(ContactsActivity.this, true);
              if (ContactsActivity.this.listView != null)
              {
                ContactsActivity.this.listView.setAdapter(ContactsActivity.this.searchListViewAdapter);
                ContactsActivity.this.searchListViewAdapter.notifyDataSetChanged();
                ContactsActivity.this.listView.setFastScrollVisible(false);
                ContactsActivity.this.listView.setVerticalScrollBarEnabled(true);
              }
              if (ContactsActivity.this.emptyView != null) {
                ContactsActivity.this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
              }
            }
            ContactsActivity.this.searchListViewAdapter.searchDialogs(paramAnonymousEditText);
          }
        }).getSearchField().setHint(LocaleController.getString("Search", 2131494298));
        if ((!this.createSecretChat) && (!this.returnAsResult)) {
          this.addItem = ((ActionBarMenu)localObject).addItem(1, 2131165188);
        }
        this.searchListViewAdapter = new SearchAdapter(paramContext, this.ignoreUsers, this.allowUsernameSearch, false, false, this.allowBots, 0);
        if (!this.onlyUsers) {
          break label476;
        }
        i = 1;
        bool2 = this.needPhonebook;
        localObject = this.ignoreUsers;
        if (this.chat_id == 0) {
          break label481;
        }
      }
    }
    label476:
    label481:
    for (boolean bool1 = true;; bool1 = false)
    {
      this.listViewAdapter = new ContactsAdapter(paramContext, i, bool2, (SparseArray)localObject, bool1);
      this.fragmentView = new FrameLayout(paramContext);
      localObject = (FrameLayout)this.fragmentView;
      this.emptyView = new EmptyTextProgressView(paramContext);
      this.emptyView.setShowAtCenter(true);
      this.emptyView.showTextView();
      ((FrameLayout)localObject).addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView = new RecyclerListView(paramContext);
      this.listView.setEmptyView(this.emptyView);
      this.listView.setSectionsType(1);
      this.listView.setVerticalScrollBarEnabled(false);
      this.listView.setFastScrollEnabled();
      this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
      this.listView.setAdapter(this.listViewAdapter);
      ((FrameLayout)localObject).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(final View paramAnonymousView, int paramAnonymousInt)
        {
          if ((ContactsActivity.this.searching) && (ContactsActivity.this.searchWas))
          {
            paramAnonymousView = (TLRPC.User)ContactsActivity.this.searchListViewAdapter.getItem(paramAnonymousInt);
            if (paramAnonymousView != null) {}
          }
          label572:
          label747:
          do
          {
            do
            {
              do
              {
                do
                {
                  int i;
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
                            do
                            {
                              do
                              {
                                return;
                                if (ContactsActivity.this.searchListViewAdapter.isGlobalSearch(paramAnonymousInt))
                                {
                                  localObject = new ArrayList();
                                  ((ArrayList)localObject).add(paramAnonymousView);
                                  MessagesController.getInstance(ContactsActivity.this.currentAccount).putUsers((ArrayList)localObject, false);
                                  MessagesStorage.getInstance(ContactsActivity.this.currentAccount).putUsersAndChats((ArrayList)localObject, null, false, true);
                                }
                                if (!ContactsActivity.this.returnAsResult) {
                                  break;
                                }
                              } while ((ContactsActivity.this.ignoreUsers != null) && (ContactsActivity.this.ignoreUsers.indexOfKey(paramAnonymousView.id) >= 0));
                              ContactsActivity.this.didSelectResult(paramAnonymousView, true, null);
                              return;
                              if (!ContactsActivity.this.createSecretChat) {
                                break;
                              }
                            } while (paramAnonymousView.id == UserConfig.getInstance(ContactsActivity.this.currentAccount).getClientUserId());
                            ContactsActivity.access$1402(ContactsActivity.this, true);
                            SecretChatHelper.getInstance(ContactsActivity.this.currentAccount).startSecretChat(ContactsActivity.this.getParentActivity(), paramAnonymousView);
                            return;
                            localObject = new Bundle();
                            ((Bundle)localObject).putInt("user_id", paramAnonymousView.id);
                          } while (!MessagesController.getInstance(ContactsActivity.this.currentAccount).checkCanOpenChat((Bundle)localObject, ContactsActivity.this));
                          ContactsActivity.this.presentFragment(new ChatActivity((Bundle)localObject), true);
                          return;
                          i = ContactsActivity.this.listViewAdapter.getSectionForPosition(paramAnonymousInt);
                          paramAnonymousInt = ContactsActivity.this.listViewAdapter.getPositionInSectionForPosition(paramAnonymousInt);
                        } while ((paramAnonymousInt < 0) || (i < 0));
                        if (((ContactsActivity.this.onlyUsers) && (ContactsActivity.this.chat_id == 0)) || (i != 0)) {
                          break label572;
                        }
                        if (!ContactsActivity.this.needPhonebook) {
                          break;
                        }
                      } while (paramAnonymousInt != 0);
                      ContactsActivity.this.presentFragment(new InviteContactsActivity());
                      return;
                      if (ContactsActivity.this.chat_id == 0) {
                        break;
                      }
                    } while (paramAnonymousInt != 0);
                    ContactsActivity.this.presentFragment(new GroupInviteActivity(ContactsActivity.this.chat_id));
                    return;
                    if (paramAnonymousInt == 0)
                    {
                      ContactsActivity.this.presentFragment(new GroupCreateActivity(), false);
                      return;
                    }
                    if (paramAnonymousInt == 1)
                    {
                      paramAnonymousView = new Bundle();
                      paramAnonymousView.putBoolean("onlyUsers", true);
                      paramAnonymousView.putBoolean("destroyAfterSelect", true);
                      paramAnonymousView.putBoolean("createSecretChat", true);
                      paramAnonymousView.putBoolean("allowBots", false);
                      ContactsActivity.this.presentFragment(new ContactsActivity(paramAnonymousView), false);
                      return;
                    }
                  } while (paramAnonymousInt != 2);
                  paramAnonymousView = MessagesController.getGlobalMainSettings();
                  if ((!BuildVars.DEBUG_VERSION) && (paramAnonymousView.getBoolean("channel_intro", false)))
                  {
                    paramAnonymousView = new Bundle();
                    paramAnonymousView.putInt("step", 0);
                    ContactsActivity.this.presentFragment(new ChannelCreateActivity(paramAnonymousView));
                    return;
                  }
                  ContactsActivity.this.presentFragment(new ChannelIntroActivity());
                  paramAnonymousView.edit().putBoolean("channel_intro", true).commit();
                  return;
                  paramAnonymousView = ContactsActivity.this.listViewAdapter.getItem(i, paramAnonymousInt);
                  if (!(paramAnonymousView instanceof TLRPC.User)) {
                    break label747;
                  }
                  paramAnonymousView = (TLRPC.User)paramAnonymousView;
                  if (!ContactsActivity.this.returnAsResult) {
                    break;
                  }
                } while ((ContactsActivity.this.ignoreUsers != null) && (ContactsActivity.this.ignoreUsers.indexOfKey(paramAnonymousView.id) >= 0));
                ContactsActivity.this.didSelectResult(paramAnonymousView, true, null);
                return;
                if (ContactsActivity.this.createSecretChat)
                {
                  ContactsActivity.access$1402(ContactsActivity.this, true);
                  SecretChatHelper.getInstance(ContactsActivity.this.currentAccount).startSecretChat(ContactsActivity.this.getParentActivity(), paramAnonymousView);
                  return;
                }
                localObject = new Bundle();
                ((Bundle)localObject).putInt("user_id", paramAnonymousView.id);
              } while (!MessagesController.getInstance(ContactsActivity.this.currentAccount).checkCanOpenChat((Bundle)localObject, ContactsActivity.this));
              ContactsActivity.this.presentFragment(new ChatActivity((Bundle)localObject), true);
              return;
            } while (!(paramAnonymousView instanceof ContactsController.Contact));
            localObject = (ContactsController.Contact)paramAnonymousView;
            paramAnonymousView = null;
            if (!((ContactsController.Contact)localObject).phones.isEmpty()) {
              paramAnonymousView = (String)((ContactsController.Contact)localObject).phones.get(0);
            }
          } while ((paramAnonymousView == null) || (ContactsActivity.this.getParentActivity() == null));
          Object localObject = new AlertDialog.Builder(ContactsActivity.this.getParentActivity());
          ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("InviteUser", 2131493699));
          ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
          ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              try
              {
                paramAnonymous2DialogInterface = new Intent("android.intent.action.VIEW", Uri.fromParts("sms", paramAnonymousView, null));
                paramAnonymous2DialogInterface.putExtra("sms_body", ContactsController.getInstance(ContactsActivity.this.currentAccount).getInviteText(1));
                ContactsActivity.this.getParentActivity().startActivityForResult(paramAnonymous2DialogInterface, 500);
                return;
              }
              catch (Exception paramAnonymous2DialogInterface)
              {
                FileLog.e(paramAnonymous2DialogInterface);
              }
            }
          });
          ((AlertDialog.Builder)localObject).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          ContactsActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (ContactsActivity.this.searching) && (ContactsActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(ContactsActivity.this.getParentActivity().getCurrentFocus());
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          super.onScrolled(paramAnonymousRecyclerView, paramAnonymousInt1, paramAnonymousInt2);
        }
      });
      return this.fragmentView;
      if (this.createSecretChat)
      {
        this.actionBar.setTitle(LocaleController.getString("NewSecretChat", 2131493877));
        break;
      }
      this.actionBar.setTitle(LocaleController.getString("NewMessageTitle", 2131493870));
      break;
      this.actionBar.setTitle(LocaleController.getString("Contacts", 2131493290));
      break;
      i = 0;
      break label184;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.contactsDidLoaded) {
      if (this.listViewAdapter != null) {
        this.listViewAdapter.notifyDataSetChanged();
      }
    }
    do
    {
      do
      {
        do
        {
          return;
          if (paramInt1 != NotificationCenter.updateInterfaces) {
            break;
          }
          paramInt1 = ((Integer)paramVarArgs[0]).intValue();
        } while (((paramInt1 & 0x2) == 0) && ((paramInt1 & 0x1) == 0) && ((paramInt1 & 0x4) == 0));
        updateVisibleRows(paramInt1);
        return;
        if (paramInt1 != NotificationCenter.encryptedChatCreated) {
          break;
        }
      } while ((!this.createSecretChat) || (!this.creatingChat));
      paramVarArgs = (TLRPC.EncryptedChat)paramVarArgs[0];
      Bundle localBundle = new Bundle();
      localBundle.putInt("enc_id", paramVarArgs.id);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
      presentFragment(new ChatActivity(localBundle), true);
      return;
    } while ((paramInt1 != NotificationCenter.closeChats) || (this.creatingChat));
    removeSelfFromStack();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    Object localObject7 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (ContactsActivity.this.listView != null)
        {
          int j = ContactsActivity.this.listView.getChildCount();
          int i = 0;
          if (i < j)
          {
            View localView = ContactsActivity.this.listView.getChildAt(i);
            if ((localView instanceof UserCell)) {
              ((UserCell)localView).update(0);
            }
            for (;;)
            {
              i += 1;
              break;
              if ((localView instanceof ProfileSearchCell)) {
                ((ProfileSearchCell)localView).update(0);
              }
            }
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SECTIONS, new Class[] { LetterSectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollActive");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollInactive");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.listView, ThemeDescription.FLAG_FASTSCROLL, null, null, null, null, "fastScrollText");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusColor" }, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "windowBackgroundWhiteGrayText");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.listView, 0, new Class[] { UserCell.class }, new String[] { "statusOnlineColor" }, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "windowBackgroundWhiteBlueText");
    Object localObject3 = this.listView;
    Object localObject4 = Theme.avatar_photoDrawable;
    Object localObject5 = Theme.avatar_broadcastDrawable;
    Object localObject6 = Theme.avatar_savedDrawable;
    localObject3 = new ThemeDescription((View)localObject3, 0, new Class[] { UserCell.class }, null, new Drawable[] { localObject4, localObject5, localObject6 }, null, "avatar_text");
    localObject4 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundRed");
    localObject5 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundOrange");
    localObject6 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundViolet");
    ThemeDescription localThemeDescription17 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundGreen");
    ThemeDescription localThemeDescription18 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundCyan");
    ThemeDescription localThemeDescription19 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundBlue");
    localObject7 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundPink");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.listView, 0, new Class[] { TextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.listView, 0, new Class[] { TextCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection");
    Object localObject8 = this.listView;
    Object localObject9 = Theme.dialogs_groupDrawable;
    Object localObject10 = Theme.dialogs_broadcastDrawable;
    Object localObject11 = Theme.dialogs_botDrawable;
    localObject8 = new ThemeDescription((View)localObject8, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject9, localObject10, localObject11 }, null, "chats_nameIcon");
    localObject9 = this.listView;
    localObject10 = Theme.dialogs_verifiedCheckDrawable;
    localObject9 = new ThemeDescription((View)localObject9, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject10 }, null, "chats_verifiedCheck");
    localObject10 = this.listView;
    localObject11 = Theme.dialogs_verifiedDrawable;
    localObject10 = new ThemeDescription((View)localObject10, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject11 }, null, "chats_verifiedBackground");
    localObject11 = this.listView;
    Object localObject12 = Theme.dialogs_offlinePaint;
    localObject11 = new ThemeDescription((View)localObject11, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject12, null, null, "windowBackgroundWhiteGrayText3");
    localObject12 = this.listView;
    Object localObject13 = Theme.dialogs_onlinePaint;
    localObject12 = new ThemeDescription((View)localObject12, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject13, null, null, "windowBackgroundWhiteBlueText3");
    localObject13 = this.listView;
    TextPaint localTextPaint = Theme.dialogs_namePaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localObject1, localObject2, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localObject3, localObject4, localObject5, localObject6, localThemeDescription17, localThemeDescription18, localThemeDescription19, localObject7, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, localObject8, localObject9, localObject10, localObject11, localObject12, new ThemeDescription((View)localObject13, 0, new Class[] { ProfileSearchCell.class }, localTextPaint, null, null, "chats_name") };
  }
  
  protected void onDialogDismiss(Dialog paramDialog)
  {
    super.onDialogDismiss(paramDialog);
    if ((this.permissionDialog != null) && (paramDialog == this.permissionDialog) && (getParentActivity() != null)) {
      askForPermissons();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatCreated);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
    if (this.arguments != null)
    {
      this.onlyUsers = getArguments().getBoolean("onlyUsers", false);
      this.destroyAfterSelect = this.arguments.getBoolean("destroyAfterSelect", false);
      this.returnAsResult = this.arguments.getBoolean("returnAsResult", false);
      this.createSecretChat = this.arguments.getBoolean("createSecretChat", false);
      this.selectAlertString = this.arguments.getString("selectAlertString");
      this.allowUsernameSearch = this.arguments.getBoolean("allowUsernameSearch", true);
      this.needForwardCount = this.arguments.getBoolean("needForwardCount", true);
      this.allowBots = this.arguments.getBoolean("allowBots", true);
      this.addingToChannel = this.arguments.getBoolean("addingToChannel", false);
      this.needFinishFragment = this.arguments.getBoolean("needFinishFragment", true);
      this.chat_id = this.arguments.getInt("chat_id", 0);
    }
    for (;;)
    {
      ContactsController.getInstance(this.currentAccount).checkInviteText();
      return true;
      this.needPhonebook = true;
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatCreated);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    this.delegate = null;
  }
  
  public void onPause()
  {
    super.onPause();
    if (this.actionBar != null) {
      this.actionBar.closeSearchField();
    }
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 1)
    {
      paramInt = 0;
      while (paramInt < paramArrayOfString.length) {
        if ((paramArrayOfInt.length <= paramInt) || (paramArrayOfInt[paramInt] != 0))
        {
          paramInt += 1;
        }
        else
        {
          String str = paramArrayOfString[paramInt];
          int i = -1;
          switch (str.hashCode())
          {
          }
          for (;;)
          {
            switch (i)
            {
            default: 
              break;
            case 0: 
              ContactsController.getInstance(this.currentAccount).forceImportContacts();
              break;
              if (str.equals("android.permission.READ_CONTACTS")) {
                i = 0;
              }
              break;
            }
          }
        }
      }
    }
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
    if ((this.checkPermission) && (Build.VERSION.SDK_INT >= 23))
    {
      Object localObject = getParentActivity();
      if (localObject != null)
      {
        this.checkPermission = false;
        if (((Activity)localObject).checkSelfPermission("android.permission.READ_CONTACTS") != 0)
        {
          if (!((Activity)localObject).shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
            break label132;
          }
          localObject = new AlertDialog.Builder((Context)localObject);
          ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
          ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("PermissionContacts", 2131494140));
          ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
          localObject = ((AlertDialog.Builder)localObject).create();
          this.permissionDialog = ((AlertDialog)localObject);
          showDialog((Dialog)localObject);
        }
      }
    }
    return;
    label132:
    askForPermissons();
  }
  
  public void setDelegate(ContactsActivityDelegate paramContactsActivityDelegate)
  {
    this.delegate = paramContactsActivityDelegate;
  }
  
  public void setIgnoreUsers(SparseArray<TLRPC.User> paramSparseArray)
  {
    this.ignoreUsers = paramSparseArray;
  }
  
  public static abstract interface ContactsActivityDelegate
  {
    public abstract void didSelectContact(TLRPC.User paramUser, String paramString, ContactsActivity paramContactsActivity);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ContactsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */