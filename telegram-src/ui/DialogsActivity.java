package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetID;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlChat;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlChatInvite;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlStickerSet;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlUnknown;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlUser;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter.DialogsSearchAdapterDelegate;
import org.telegram.ui.Cells.AccountSelectCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatActivityEnterView.ChatActivityEnterViewDelegate;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickersAlert;

public class DialogsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  public static boolean[] dialogsLoaded = new boolean[3];
  private String addToGroupAlertString;
  private boolean allowSwitchAccount;
  private boolean cantSendToChannels;
  private boolean checkPermission = true;
  private ChatActivityEnterView commentView;
  private DialogsActivityDelegate delegate;
  private DialogsAdapter dialogsAdapter;
  private DialogsSearchAdapter dialogsSearchAdapter;
  private int dialogsType;
  private ImageView floatingButton;
  private boolean floatingHidden;
  private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
  private LinearLayoutManager layoutManager;
  private RecyclerListView listView;
  private boolean onlySelect;
  private long openedDialogId;
  private ActionBarMenuItem passcodeItem;
  private AlertDialog permissionDialog;
  private int prevPosition;
  private int prevTop;
  private RadialProgressView progressView;
  private boolean scrollUpdated;
  private EmptyTextProgressView searchEmptyView;
  private String searchString;
  private boolean searchWas;
  private boolean searching;
  private String selectAlertString;
  private String selectAlertStringGroup;
  private long selectedDialog;
  private RecyclerView sideMenu;
  private ActionBarMenuItem switchItem;
  
  public DialogsActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  @TargetApi(23)
  private void askForPermissons()
  {
    Activity localActivity = getParentActivity();
    if (localActivity == null) {
      return;
    }
    Object localObject = new ArrayList();
    if (localActivity.checkSelfPermission("android.permission.READ_CONTACTS") != 0)
    {
      ((ArrayList)localObject).add("android.permission.READ_CONTACTS");
      ((ArrayList)localObject).add("android.permission.WRITE_CONTACTS");
      ((ArrayList)localObject).add("android.permission.GET_ACCOUNTS");
    }
    if (localActivity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0)
    {
      ((ArrayList)localObject).add("android.permission.READ_EXTERNAL_STORAGE");
      ((ArrayList)localObject).add("android.permission.WRITE_EXTERNAL_STORAGE");
    }
    localObject = (String[])((ArrayList)localObject).toArray(new String[((ArrayList)localObject).size()]);
    try
    {
      localActivity.requestPermissions((String[])localObject, 1);
      return;
    }
    catch (Exception localException) {}
  }
  
  private void didSelectResult(final long paramLong, boolean paramBoolean1, boolean paramBoolean2)
  {
    Object localObject1;
    if ((this.addToGroupAlertString == null) && ((int)paramLong < 0))
    {
      localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)paramLong));
      if ((ChatObject.isChannel((TLRPC.Chat)localObject1)) && (!((TLRPC.Chat)localObject1).megagroup) && ((this.cantSendToChannels) || (!ChatObject.isCanWriteToChannel(-(int)paramLong, this.currentAccount))))
      {
        localObject1 = new AlertDialog.Builder(getParentActivity());
        ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
        ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("ChannelCantSendMessage", 2131493161));
        ((AlertDialog.Builder)localObject1).setNegativeButton(LocaleController.getString("OK", 2131494028), null);
        showDialog(((AlertDialog.Builder)localObject1).create());
      }
    }
    int i;
    int j;
    Object localObject2;
    do
    {
      do
      {
        return;
        if ((!paramBoolean1) || (((this.selectAlertString == null) || (this.selectAlertStringGroup == null)) && (this.addToGroupAlertString == null))) {
          break;
        }
      } while (getParentActivity() == null);
      localObject1 = new AlertDialog.Builder(getParentActivity());
      ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
      i = (int)paramLong;
      j = (int)(paramLong >> 32);
      if (i == 0) {
        break label509;
      }
      if (j != 1) {
        break;
      }
      localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(i));
    } while (localObject2 == null);
    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, new Object[] { ((TLRPC.Chat)localObject2).title }));
    for (;;)
    {
      ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          DialogsActivity.this.didSelectResult(paramLong, false, false);
        }
      });
      ((AlertDialog.Builder)localObject1).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
      showDialog(((AlertDialog.Builder)localObject1).create());
      return;
      if (i == UserConfig.getInstance(this.currentAccount).getClientUserId())
      {
        ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, new Object[] { LocaleController.getString("SavedMessages", 2131494293) }));
      }
      else
      {
        if (i > 0)
        {
          localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
          if (localObject2 == null) {
            break;
          }
          ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.selectAlertString, new Object[] { UserObject.getUserName((TLRPC.User)localObject2) }));
          continue;
        }
        if (i < 0)
        {
          localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
          if (localObject2 == null) {
            break;
          }
          if (this.addToGroupAlertString != null)
          {
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.addToGroupAlertString, new Object[] { ((TLRPC.Chat)localObject2).title }));
          }
          else
          {
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.selectAlertStringGroup, new Object[] { ((TLRPC.Chat)localObject2).title }));
            continue;
            label509:
            localObject2 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(j));
            localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(((TLRPC.EncryptedChat)localObject2).user_id));
            if (localObject2 == null) {
              break;
            }
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.formatStringSimple(this.selectAlertString, new Object[] { UserObject.getUserName((TLRPC.User)localObject2) }));
          }
        }
      }
    }
    if (this.delegate != null)
    {
      localObject1 = new ArrayList();
      ((ArrayList)localObject1).add(Long.valueOf(paramLong));
      this.delegate.didSelectDialogs(this, (ArrayList)localObject1, null, paramBoolean2);
      this.delegate = null;
      return;
    }
    finishFragment();
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
  
  private void hideFloatingButton(boolean paramBoolean)
  {
    if (this.floatingHidden == paramBoolean) {
      return;
    }
    this.floatingHidden = paramBoolean;
    Object localObject = this.floatingButton;
    float f;
    ImageView localImageView;
    if (this.floatingHidden)
    {
      f = AndroidUtilities.dp(100.0F);
      localObject = ObjectAnimator.ofFloat(localObject, "translationY", new float[] { f }).setDuration(300L);
      ((ObjectAnimator)localObject).setInterpolator(this.floatingInterpolator);
      localImageView = this.floatingButton;
      if (paramBoolean) {
        break label91;
      }
    }
    label91:
    for (paramBoolean = true;; paramBoolean = false)
    {
      localImageView.setClickable(paramBoolean);
      ((ObjectAnimator)localObject).start();
      return;
      f = 0.0F;
      break;
    }
  }
  
  private void updatePasscodeButton()
  {
    if (this.passcodeItem == null) {
      return;
    }
    if ((SharedConfig.passcodeHash.length() != 0) && (!this.searching))
    {
      this.passcodeItem.setVisibility(0);
      if (SharedConfig.appLocked)
      {
        this.passcodeItem.setIcon(2131165474);
        return;
      }
      this.passcodeItem.setIcon(2131165476);
      return;
    }
    this.passcodeItem.setVisibility(8);
  }
  
  private void updateSelectedCount()
  {
    if (this.commentView == null) {}
    AnimatorSet localAnimatorSet;
    for (;;)
    {
      return;
      if (this.dialogsAdapter.hasSelectedDialogs()) {
        break;
      }
      if ((this.dialogsType == 3) && (this.selectAlertString == null)) {
        this.actionBar.setTitle(LocaleController.getString("ForwardTo", 2131493549));
      }
      while (this.commentView.getTag() != null)
      {
        this.commentView.hidePopup(false);
        this.commentView.closeKeyboard();
        localAnimatorSet = new AnimatorSet();
        localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.commentView, "translationY", new float[] { 0.0F, this.commentView.getMeasuredHeight() }) });
        localAnimatorSet.setDuration(180L);
        localAnimatorSet.setInterpolator(new DecelerateInterpolator());
        localAnimatorSet.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            DialogsActivity.this.commentView.setVisibility(8);
          }
        });
        localAnimatorSet.start();
        this.commentView.setTag(null);
        this.listView.requestLayout();
        return;
        this.actionBar.setTitle(LocaleController.getString("SelectChat", 2131494327));
      }
    }
    if (this.commentView.getTag() == null)
    {
      this.commentView.setFieldText("");
      this.commentView.setVisibility(0);
      localAnimatorSet = new AnimatorSet();
      localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.commentView, "translationY", new float[] { this.commentView.getMeasuredHeight(), 0.0F }) });
      localAnimatorSet.setDuration(180L);
      localAnimatorSet.setInterpolator(new DecelerateInterpolator());
      localAnimatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          DialogsActivity.this.commentView.setTag(Integer.valueOf(2));
        }
      });
      localAnimatorSet.start();
      this.commentView.setTag(Integer.valueOf(1));
    }
    this.actionBar.setTitle(LocaleController.formatPluralString("Recipient", this.dialogsAdapter.getSelectedDialogs().size()));
  }
  
  private void updateVisibleRows(int paramInt)
  {
    if (this.listView == null) {
      return;
    }
    int k = this.listView.getChildCount();
    int i = 0;
    label19:
    Object localObject;
    boolean bool;
    if (i < k)
    {
      localObject = this.listView.getChildAt(i);
      if (!(localObject instanceof DialogCell)) {
        break label188;
      }
      if (this.listView.getAdapter() != this.dialogsSearchAdapter)
      {
        localObject = (DialogCell)localObject;
        if ((paramInt & 0x800) == 0) {
          break label126;
        }
        ((DialogCell)localObject).checkCurrentDialogIndex();
        if ((this.dialogsType == 0) && (AndroidUtilities.isTablet()))
        {
          if (((DialogCell)localObject).getDialogId() != this.openedDialogId) {
            break label120;
          }
          bool = true;
          label106:
          ((DialogCell)localObject).setDialogSelected(bool);
        }
      }
    }
    for (;;)
    {
      i += 1;
      break label19;
      break;
      label120:
      bool = false;
      break label106;
      label126:
      if ((paramInt & 0x200) != 0)
      {
        if ((this.dialogsType == 0) && (AndroidUtilities.isTablet()))
        {
          if (((DialogCell)localObject).getDialogId() == this.openedDialogId) {}
          for (bool = true;; bool = false)
          {
            ((DialogCell)localObject).setDialogSelected(bool);
            break;
          }
        }
      }
      else
      {
        ((DialogCell)localObject).update(paramInt);
        continue;
        label188:
        if ((localObject instanceof UserCell))
        {
          ((UserCell)localObject).update(paramInt);
        }
        else if ((localObject instanceof ProfileSearchCell))
        {
          ((ProfileSearchCell)localObject).update(paramInt);
        }
        else if ((localObject instanceof RecyclerListView))
        {
          localObject = (RecyclerListView)localObject;
          int m = ((RecyclerListView)localObject).getChildCount();
          int j = 0;
          while (j < m)
          {
            View localView = ((RecyclerListView)localObject).getChildAt(j);
            if ((localView instanceof HintDialogCell)) {
              ((HintDialogCell)localView).checkUnreadCounter(paramInt);
            }
            j += 1;
          }
        }
      }
    }
  }
  
  public View createView(final Context paramContext)
  {
    this.searching = false;
    this.searchWas = false;
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        Theme.createChatResources(paramContext, false);
      }
    });
    Object localObject1 = this.actionBar.createMenu();
    if ((!this.onlySelect) && (this.searchString == null))
    {
      this.passcodeItem = ((ActionBarMenu)localObject1).addItem(1, 2131165474);
      updatePasscodeButton();
    }
    ((ActionBarMenu)localObject1).addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
    {
      public boolean canCollapseSearch()
      {
        if (DialogsActivity.this.switchItem != null) {
          DialogsActivity.this.switchItem.setVisibility(0);
        }
        if (DialogsActivity.this.searchString != null)
        {
          DialogsActivity.this.finishFragment();
          return false;
        }
        return true;
      }
      
      public void onSearchCollapse()
      {
        DialogsActivity.access$002(DialogsActivity.this, false);
        DialogsActivity.access$902(DialogsActivity.this, false);
        if (DialogsActivity.this.listView != null)
        {
          if ((!MessagesController.getInstance(DialogsActivity.this.currentAccount).loadingDialogs) || (!MessagesController.getInstance(DialogsActivity.this.currentAccount).dialogs.isEmpty())) {
            break label222;
          }
          DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.progressView);
        }
        for (;;)
        {
          DialogsActivity.this.searchEmptyView.setVisibility(8);
          if (!DialogsActivity.this.onlySelect)
          {
            DialogsActivity.this.floatingButton.setVisibility(0);
            DialogsActivity.access$1202(DialogsActivity.this, true);
            DialogsActivity.this.floatingButton.setTranslationY(AndroidUtilities.dp(100.0F));
            DialogsActivity.this.hideFloatingButton(false);
          }
          if (DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsAdapter)
          {
            DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsAdapter);
            DialogsActivity.this.dialogsAdapter.notifyDataSetChanged();
          }
          if (DialogsActivity.this.dialogsSearchAdapter != null) {
            DialogsActivity.this.dialogsSearchAdapter.searchDialogs(null);
          }
          DialogsActivity.this.updatePasscodeButton();
          return;
          label222:
          DialogsActivity.this.progressView.setVisibility(8);
          DialogsActivity.this.listView.setEmptyView(null);
        }
      }
      
      public void onSearchExpand()
      {
        DialogsActivity.access$002(DialogsActivity.this, true);
        if (DialogsActivity.this.switchItem != null) {
          DialogsActivity.this.switchItem.setVisibility(8);
        }
        if (DialogsActivity.this.listView != null)
        {
          if (DialogsActivity.this.searchString != null)
          {
            DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
            DialogsActivity.this.progressView.setVisibility(8);
          }
          if (!DialogsActivity.this.onlySelect) {
            DialogsActivity.this.floatingButton.setVisibility(8);
          }
        }
        DialogsActivity.this.updatePasscodeButton();
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        if ((paramAnonymousEditText.length() != 0) || ((DialogsActivity.this.dialogsSearchAdapter != null) && (DialogsActivity.this.dialogsSearchAdapter.hasRecentRearch())))
        {
          DialogsActivity.access$902(DialogsActivity.this, true);
          if ((DialogsActivity.this.dialogsSearchAdapter != null) && (DialogsActivity.this.listView.getAdapter() != DialogsActivity.this.dialogsSearchAdapter))
          {
            DialogsActivity.this.listView.setAdapter(DialogsActivity.this.dialogsSearchAdapter);
            DialogsActivity.this.dialogsSearchAdapter.notifyDataSetChanged();
          }
          if ((DialogsActivity.this.searchEmptyView != null) && (DialogsActivity.this.listView.getEmptyView() != DialogsActivity.this.searchEmptyView))
          {
            DialogsActivity.this.progressView.setVisibility(8);
            DialogsActivity.this.searchEmptyView.showTextView();
            DialogsActivity.this.listView.setEmptyView(DialogsActivity.this.searchEmptyView);
          }
        }
        if (DialogsActivity.this.dialogsSearchAdapter != null) {
          DialogsActivity.this.dialogsSearchAdapter.searchDialogs(paramAnonymousEditText);
        }
      }
    }).getSearchField().setHint(LocaleController.getString("Search", 2131494298));
    Object localObject2;
    if (this.onlySelect)
    {
      this.actionBar.setBackButtonImage(2131165346);
      if ((this.dialogsType == 3) && (this.selectAlertString == null))
      {
        this.actionBar.setTitle(LocaleController.getString("ForwardTo", 2131493549));
        if ((!this.allowSwitchAccount) || (UserConfig.getActivatedAccountsCount() <= 1)) {
          break label518;
        }
        this.switchItem = ((ActionBarMenu)localObject1).addItemWithWidth(1, 0, AndroidUtilities.dp(56.0F));
        localObject2 = new AvatarDrawable();
        ((AvatarDrawable)localObject2).setTextSize(AndroidUtilities.dp(12.0F));
        localObject3 = new BackupImageView(paramContext);
        ((BackupImageView)localObject3).setRoundRadius(AndroidUtilities.dp(18.0F));
        this.switchItem.addView((View)localObject3, LayoutHelper.createFrame(36, 36, 17));
        localObject1 = UserConfig.getInstance(this.currentAccount).getCurrentUser();
        ((AvatarDrawable)localObject2).setInfo((TLRPC.User)localObject1);
        if ((((TLRPC.User)localObject1).photo == null) || (((TLRPC.User)localObject1).photo.photo_small == null) || (((TLRPC.User)localObject1).photo.photo_small.volume_id == 0L) || (((TLRPC.User)localObject1).photo.photo_small.local_id == 0)) {
          break label512;
        }
      }
    }
    int i;
    label449:
    label493:
    label512:
    for (localObject1 = ((TLRPC.User)localObject1).photo.photo_small;; localObject1 = null)
    {
      ((BackupImageView)localObject3).getImageReceiver().setCurrentAccount(this.currentAccount);
      ((BackupImageView)localObject3).setImage((TLObject)localObject1, "50_50", (Drawable)localObject2);
      i = 0;
      while (i < 3)
      {
        if (UserConfig.getInstance(i).getCurrentUser() != null)
        {
          localObject1 = new AccountSelectCell(paramContext);
          ((AccountSelectCell)localObject1).setAccount(i);
          this.switchItem.addSubItem(i + 10, (View)localObject1, AndroidUtilities.dp(230.0F), AndroidUtilities.dp(48.0F));
        }
        i += 1;
      }
      this.actionBar.setTitle(LocaleController.getString("SelectChat", 2131494327));
      break;
      if (this.searchString != null)
      {
        this.actionBar.setBackButtonImage(2131165346);
        if (!BuildVars.DEBUG_VERSION) {
          break label493;
        }
        this.actionBar.setTitle("Telegram Beta");
      }
      for (;;)
      {
        this.actionBar.setSupportsHolidayImage(true);
        break;
        this.actionBar.setBackButtonDrawable(new MenuDrawable());
        break label449;
        this.actionBar.setTitle(LocaleController.getString("AppName", 2131492981));
      }
    }
    label518:
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        boolean bool = true;
        if (paramAnonymousInt == -1) {
          if (DialogsActivity.this.onlySelect) {
            DialogsActivity.this.finishFragment();
          }
        }
        do
        {
          do
          {
            return;
          } while (DialogsActivity.this.parentLayout == null);
          DialogsActivity.this.parentLayout.getDrawerLayoutContainer().openDrawer(false);
          return;
          if (paramAnonymousInt == 1)
          {
            if (!SharedConfig.appLocked) {}
            for (;;)
            {
              SharedConfig.appLocked = bool;
              SharedConfig.saveConfig();
              DialogsActivity.this.updatePasscodeButton();
              return;
              bool = false;
            }
          }
        } while ((paramAnonymousInt < 10) || (paramAnonymousInt >= 13) || (DialogsActivity.this.getParentActivity() == null));
        DialogsActivity.DialogsActivityDelegate localDialogsActivityDelegate = DialogsActivity.this.delegate;
        LaunchActivity localLaunchActivity = (LaunchActivity)DialogsActivity.this.getParentActivity();
        localLaunchActivity.switchToAccount(paramAnonymousInt - 10, true);
        DialogsActivity localDialogsActivity = new DialogsActivity(DialogsActivity.this.arguments);
        localDialogsActivity.setDelegate(localDialogsActivityDelegate);
        localLaunchActivity.presentFragment(localDialogsActivity, false, true);
      }
    });
    if (this.sideMenu != null)
    {
      this.sideMenu.setBackgroundColor(Theme.getColor("chats_menuBackground"));
      this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
      this.sideMenu.getAdapter().notifyDataSetChanged();
    }
    Object localObject3 = new SizeNotifierFrameLayout(paramContext)
    {
      int inputFieldHeight = 0;
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        int i1 = getChildCount();
        Object localObject;
        int j;
        if (DialogsActivity.this.commentView != null)
        {
          localObject = DialogsActivity.this.commentView.getTag();
          if ((localObject == null) || (!localObject.equals(Integer.valueOf(2)))) {
            break label130;
          }
          if ((getKeyboardHeight() > AndroidUtilities.dp(20.0F)) || (AndroidUtilities.isInMultiwindow)) {
            break label124;
          }
          j = DialogsActivity.this.commentView.getEmojiPadding();
        }
        for (;;)
        {
          setBottomClip(j);
          int n = 0;
          for (;;)
          {
            if (n >= i1) {
              break label477;
            }
            localObject = getChildAt(n);
            if (((View)localObject).getVisibility() != 8) {
              break;
            }
            n += 1;
          }
          localObject = null;
          break;
          label124:
          j = 0;
          continue;
          label130:
          j = 0;
        }
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)((View)localObject).getLayoutParams();
        int i2 = ((View)localObject).getMeasuredWidth();
        int i3 = ((View)localObject).getMeasuredHeight();
        int k = localLayoutParams.gravity;
        int i = k;
        if (k == -1) {
          i = 51;
        }
        switch (i & 0x7 & 0x7)
        {
        default: 
          k = localLayoutParams.leftMargin;
          label223:
          switch (i & 0x70)
          {
          default: 
            i = localLayoutParams.topMargin;
            label271:
            m = i;
            if (DialogsActivity.this.commentView != null)
            {
              m = i;
              if (DialogsActivity.this.commentView.isPopupView((View)localObject)) {
                if (!AndroidUtilities.isInMultiwindow) {
                  break label462;
                }
              }
            }
            break;
          }
          break;
        }
        label462:
        for (int m = DialogsActivity.this.commentView.getTop() - ((View)localObject).getMeasuredHeight() + AndroidUtilities.dp(1.0F);; m = DialogsActivity.this.commentView.getBottom())
        {
          ((View)localObject).layout(k, m, k + i2, m + i3);
          break;
          k = (paramAnonymousInt3 - paramAnonymousInt1 - i2) / 2 + localLayoutParams.leftMargin - localLayoutParams.rightMargin;
          break label223;
          k = paramAnonymousInt3 - i2 - localLayoutParams.rightMargin;
          break label223;
          i = localLayoutParams.topMargin + getPaddingTop();
          break label271;
          i = (paramAnonymousInt4 - j - paramAnonymousInt2 - i3) / 2 + localLayoutParams.topMargin - localLayoutParams.bottomMargin;
          break label271;
          i = paramAnonymousInt4 - j - paramAnonymousInt2 - i3 - localLayoutParams.bottomMargin;
          break label271;
        }
        label477:
        notifyHeightChanged();
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        int k = View.MeasureSpec.getSize(paramAnonymousInt1);
        int i = View.MeasureSpec.getSize(paramAnonymousInt2);
        setMeasuredDimension(k, i);
        int j = i - getPaddingTop();
        measureChildWithMargins(DialogsActivity.this.actionBar, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
        int n = getKeyboardHeight();
        int m = getChildCount();
        i = j;
        Object localObject;
        if (DialogsActivity.this.commentView != null)
        {
          measureChildWithMargins(DialogsActivity.this.commentView, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
          localObject = DialogsActivity.this.commentView.getTag();
          if ((localObject != null) && (localObject.equals(Integer.valueOf(2))))
          {
            i = j;
            if (n <= AndroidUtilities.dp(20.0F))
            {
              i = j;
              if (!AndroidUtilities.isInMultiwindow) {
                i = j - DialogsActivity.this.commentView.getEmojiPadding();
              }
            }
            this.inputFieldHeight = DialogsActivity.this.commentView.getMeasuredHeight();
          }
        }
        else
        {
          j = 0;
          label163:
          if (j >= m) {
            return;
          }
          localObject = getChildAt(j);
          if ((localObject != null) && (((View)localObject).getVisibility() != 8) && (localObject != DialogsActivity.this.commentView) && (localObject != DialogsActivity.this.actionBar)) {
            break label237;
          }
        }
        for (;;)
        {
          j += 1;
          break label163;
          this.inputFieldHeight = 0;
          i = j;
          break;
          label237:
          if ((localObject == DialogsActivity.this.listView) || (localObject == DialogsActivity.this.progressView) || (localObject == DialogsActivity.this.searchEmptyView)) {
            ((View)localObject).measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10.0F), i - this.inputFieldHeight + AndroidUtilities.dp(2.0F)), 1073741824));
          } else if ((DialogsActivity.this.commentView != null) && (DialogsActivity.this.commentView.isPopupView((View)localObject)))
          {
            if (AndroidUtilities.isInMultiwindow)
            {
              if (AndroidUtilities.isTablet()) {
                ((View)localObject).measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320.0F), i - this.inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), 1073741824));
              } else {
                ((View)localObject).measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(i - this.inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), 1073741824));
              }
            }
            else {
              ((View)localObject).measure(View.MeasureSpec.makeMeasureSpec(k, 1073741824), View.MeasureSpec.makeMeasureSpec(((View)localObject).getLayoutParams().height, 1073741824));
            }
          }
          else {
            measureChildWithMargins((View)localObject, paramAnonymousInt1, 0, paramAnonymousInt2, 0);
          }
        }
      }
    };
    this.fragmentView = ((View)localObject3);
    this.listView = new RecyclerListView(paramContext);
    this.listView.setVerticalScrollBarEnabled(true);
    this.listView.setItemAnimator(null);
    this.listView.setInstantClick(true);
    this.listView.setLayoutAnimation(null);
    this.listView.setTag(Integer.valueOf(4));
    this.layoutManager = new LinearLayoutManager(paramContext)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    };
    this.layoutManager.setOrientation(1);
    this.listView.setLayoutManager(this.layoutManager);
    localObject1 = this.listView;
    label885:
    label1207:
    float f1;
    label1219:
    int j;
    label1228:
    float f2;
    label1238:
    float f3;
    if (LocaleController.isRTL)
    {
      i = 1;
      ((RecyclerListView)localObject1).setVerticalScrollbarPosition(i);
      ((SizeNotifierFrameLayout)localObject3).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((DialogsActivity.this.listView == null) || (DialogsActivity.this.listView.getAdapter() == null) || (DialogsActivity.this.getParentActivity() == null)) {}
          label791:
          label793:
          label805:
          label843:
          label1102:
          label1125:
          do
          {
            Object localObject1;
            Object localObject2;
            long l1;
            int i;
            for (;;)
            {
              return;
              long l2 = 0L;
              j = 0;
              localObject1 = DialogsActivity.this.listView.getAdapter();
              if (localObject1 == DialogsActivity.this.dialogsAdapter)
              {
                localObject2 = DialogsActivity.this.dialogsAdapter.getItem(paramAnonymousInt);
                if ((localObject2 instanceof TLRPC.TL_dialog))
                {
                  l1 = ((TLRPC.TL_dialog)localObject2).id;
                  i = j;
                }
              }
              for (;;)
              {
                if (l1 == 0L) {
                  break label791;
                }
                if (!DialogsActivity.this.onlySelect) {
                  break label805;
                }
                if (!DialogsActivity.this.dialogsAdapter.hasSelectedDialogs()) {
                  break label793;
                }
                DialogsActivity.this.dialogsAdapter.addOrRemoveSelectedDialog(l1, paramAnonymousView);
                DialogsActivity.this.updateSelectedCount();
                return;
                if ((localObject2 instanceof TLRPC.TL_recentMeUrlChat))
                {
                  l1 = -((TLRPC.TL_recentMeUrlChat)localObject2).chat_id;
                  i = j;
                }
                else if ((localObject2 instanceof TLRPC.TL_recentMeUrlUser))
                {
                  l1 = ((TLRPC.TL_recentMeUrlUser)localObject2).user_id;
                  i = j;
                }
                else
                {
                  if ((localObject2 instanceof TLRPC.TL_recentMeUrlChatInvite))
                  {
                    TLRPC.TL_recentMeUrlChatInvite localTL_recentMeUrlChatInvite = (TLRPC.TL_recentMeUrlChatInvite)localObject2;
                    localObject2 = localTL_recentMeUrlChatInvite.chat_invite;
                    if (((((TLRPC.ChatInvite)localObject2).chat == null) && ((!((TLRPC.ChatInvite)localObject2).channel) || (((TLRPC.ChatInvite)localObject2).megagroup))) || ((((TLRPC.ChatInvite)localObject2).chat != null) && ((!ChatObject.isChannel(((TLRPC.ChatInvite)localObject2).chat)) || (((TLRPC.ChatInvite)localObject2).chat.megagroup))))
                    {
                      localObject1 = localTL_recentMeUrlChatInvite.url;
                      paramAnonymousInt = ((String)localObject1).indexOf('/');
                      paramAnonymousView = (View)localObject1;
                      if (paramAnonymousInt > 0) {
                        paramAnonymousView = ((String)localObject1).substring(paramAnonymousInt + 1);
                      }
                      DialogsActivity.this.showDialog(new JoinGroupAlert(DialogsActivity.this.getParentActivity(), (TLRPC.ChatInvite)localObject2, paramAnonymousView, DialogsActivity.this));
                      return;
                    }
                    if (((TLRPC.ChatInvite)localObject2).chat == null) {
                      break;
                    }
                    l1 = -((TLRPC.ChatInvite)localObject2).chat.id;
                    i = j;
                    continue;
                  }
                  if ((localObject2 instanceof TLRPC.TL_recentMeUrlStickerSet))
                  {
                    paramAnonymousView = ((TLRPC.TL_recentMeUrlStickerSet)localObject2).set.set;
                    localObject1 = new TLRPC.TL_inputStickerSetID();
                    ((TLRPC.TL_inputStickerSetID)localObject1).id = paramAnonymousView.id;
                    ((TLRPC.TL_inputStickerSetID)localObject1).access_hash = paramAnonymousView.access_hash;
                    DialogsActivity.this.showDialog(new StickersAlert(DialogsActivity.this.getParentActivity(), DialogsActivity.this, (TLRPC.InputStickerSet)localObject1, null, null));
                    return;
                  }
                  if (!(localObject2 instanceof TLRPC.TL_recentMeUrlUnknown)) {
                    break;
                  }
                  return;
                  l1 = l2;
                  i = j;
                  if (localObject1 == DialogsActivity.this.dialogsSearchAdapter)
                  {
                    localObject2 = DialogsActivity.this.dialogsSearchAdapter.getItem(paramAnonymousInt);
                    if ((localObject2 instanceof TLRPC.User))
                    {
                      l2 = ((TLRPC.User)localObject2).id;
                      l1 = l2;
                      i = j;
                      if (!DialogsActivity.this.onlySelect)
                      {
                        DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.User)localObject2);
                        l1 = l2;
                        i = j;
                      }
                    }
                    else
                    {
                      if ((localObject2 instanceof TLRPC.Chat))
                      {
                        if (((TLRPC.Chat)localObject2).id > 0) {}
                        for (l2 = -((TLRPC.Chat)localObject2).id;; l2 = AndroidUtilities.makeBroadcastId(((TLRPC.Chat)localObject2).id))
                        {
                          l1 = l2;
                          i = j;
                          if (DialogsActivity.this.onlySelect) {
                            break;
                          }
                          DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.Chat)localObject2);
                          l1 = l2;
                          i = j;
                          break;
                        }
                      }
                      if ((localObject2 instanceof TLRPC.EncryptedChat))
                      {
                        l2 = ((TLRPC.EncryptedChat)localObject2).id << 32;
                        l1 = l2;
                        i = j;
                        if (!DialogsActivity.this.onlySelect)
                        {
                          DialogsActivity.this.dialogsSearchAdapter.putRecentSearch(l2, (TLRPC.EncryptedChat)localObject2);
                          l1 = l2;
                          i = j;
                        }
                      }
                      else if ((localObject2 instanceof MessageObject))
                      {
                        localObject2 = (MessageObject)localObject2;
                        l1 = ((MessageObject)localObject2).getDialogId();
                        i = ((MessageObject)localObject2).getId();
                        DialogsActivity.this.dialogsSearchAdapter.addHashtagsFromMessage(DialogsActivity.this.dialogsSearchAdapter.getLastSearchString());
                      }
                      else
                      {
                        l1 = l2;
                        i = j;
                        if ((localObject2 instanceof String))
                        {
                          DialogsActivity.this.actionBar.openSearchField((String)localObject2);
                          l1 = l2;
                          i = j;
                        }
                      }
                    }
                  }
                }
              }
            }
            DialogsActivity.this.didSelectResult(l1, true, false);
            return;
            paramAnonymousView = new Bundle();
            int j = (int)l1;
            paramAnonymousInt = (int)(l1 >> 32);
            if (j != 0) {
              if (paramAnonymousInt == 1)
              {
                paramAnonymousView.putInt("chat_id", j);
                if (i == 0) {
                  break label1102;
                }
                paramAnonymousView.putInt("message_id", i);
              }
            }
            for (;;)
            {
              if (AndroidUtilities.isTablet())
              {
                if ((DialogsActivity.this.openedDialogId == l1) && (localObject1 != DialogsActivity.this.dialogsSearchAdapter)) {
                  break;
                }
                if (DialogsActivity.this.dialogsAdapter != null)
                {
                  DialogsActivity.this.dialogsAdapter.setOpenedDialogId(DialogsActivity.access$2902(DialogsActivity.this, l1));
                  DialogsActivity.this.updateVisibleRows(512);
                }
              }
              if (DialogsActivity.this.searchString == null) {
                break label1125;
              }
              if (!MessagesController.getInstance(DialogsActivity.this.currentAccount).checkCanOpenChat(paramAnonymousView, DialogsActivity.this)) {
                break;
              }
              NotificationCenter.getInstance(DialogsActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              DialogsActivity.this.presentFragment(new ChatActivity(paramAnonymousView));
              return;
              if (j > 0)
              {
                paramAnonymousView.putInt("user_id", j);
                break label843;
              }
              if (j >= 0) {
                break label843;
              }
              paramAnonymousInt = j;
              if (i != 0)
              {
                localObject2 = MessagesController.getInstance(DialogsActivity.this.currentAccount).getChat(Integer.valueOf(-j));
                paramAnonymousInt = j;
                if (localObject2 != null)
                {
                  paramAnonymousInt = j;
                  if (((TLRPC.Chat)localObject2).migrated_to != null)
                  {
                    paramAnonymousView.putInt("migrated_to", j);
                    paramAnonymousInt = -((TLRPC.Chat)localObject2).migrated_to.channel_id;
                  }
                }
              }
              paramAnonymousView.putInt("chat_id", -paramAnonymousInt);
              break label843;
              paramAnonymousView.putInt("enc_id", paramAnonymousInt);
              break label843;
              if (DialogsActivity.this.actionBar != null) {
                DialogsActivity.this.actionBar.closeSearchField();
              }
            }
          } while (!MessagesController.getInstance(DialogsActivity.this.currentAccount).checkCanOpenChat(paramAnonymousView, DialogsActivity.this));
          DialogsActivity.this.presentFragment(new ChatActivity(paramAnonymousView));
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (DialogsActivity.this.getParentActivity() == null) {
            return false;
          }
          if (DialogsActivity.this.listView.getAdapter() == DialogsActivity.this.dialogsSearchAdapter)
          {
            if (((DialogsActivity.this.dialogsSearchAdapter.getItem(paramAnonymousInt) instanceof String)) || (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed()))
            {
              paramAnonymousView = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
              paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
              paramAnonymousView.setMessage(LocaleController.getString("ClearSearch", 2131493264));
              paramAnonymousView.setPositiveButton(LocaleController.getString("ClearButton", 2131493257).toUpperCase(), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  if (DialogsActivity.this.dialogsSearchAdapter.isRecentSearchDisplayed())
                  {
                    DialogsActivity.this.dialogsSearchAdapter.clearRecentSearch();
                    return;
                  }
                  DialogsActivity.this.dialogsSearchAdapter.clearRecentHashtags();
                }
              });
              paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
              DialogsActivity.this.showDialog(paramAnonymousView.create());
              return true;
            }
            return false;
          }
          Object localObject1 = DialogsActivity.this.getDialogsArray();
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= ((ArrayList)localObject1).size())) {
            return false;
          }
          Object localObject2 = (TLRPC.TL_dialog)((ArrayList)localObject1).get(paramAnonymousInt);
          if (DialogsActivity.this.onlySelect)
          {
            if ((DialogsActivity.this.dialogsType != 3) || (DialogsActivity.this.selectAlertString != null)) {
              return false;
            }
            DialogsActivity.this.dialogsAdapter.addOrRemoveSelectedDialog(((TLRPC.TL_dialog)localObject2).id, paramAnonymousView);
            DialogsActivity.this.updateSelectedCount();
            return true;
          }
          DialogsActivity.access$3702(DialogsActivity.this, ((TLRPC.TL_dialog)localObject2).id);
          final boolean bool4 = ((TLRPC.TL_dialog)localObject2).pinned;
          BottomSheet.Builder localBuilder = new BottomSheet.Builder(DialogsActivity.this.getParentActivity());
          paramAnonymousInt = (int)DialogsActivity.this.selectedDialog;
          int i = (int)(DialogsActivity.this.selectedDialog >> 32);
          final Object localObject3;
          if (DialogObject.isChannel((TLRPC.TL_dialog)localObject2))
          {
            localObject3 = MessagesController.getInstance(DialogsActivity.this.currentAccount).getChat(Integer.valueOf(-paramAnonymousInt));
            if (((TLRPC.TL_dialog)localObject2).pinned)
            {
              paramAnonymousInt = 2131165272;
              label342:
              if ((localObject3 == null) || (!((TLRPC.Chat)localObject3).megagroup)) {
                break label534;
              }
              localObject1 = new CharSequence[3];
              if ((!((TLRPC.TL_dialog)localObject2).pinned) && (!MessagesController.getInstance(DialogsActivity.this.currentAccount).canPinDialog(false))) {
                break label518;
              }
              if (!((TLRPC.TL_dialog)localObject2).pinned) {
                break label507;
              }
              paramAnonymousView = LocaleController.getString("UnpinFromTop", 2131494513);
              label402:
              localObject1[0] = paramAnonymousView;
              if (!TextUtils.isEmpty(((TLRPC.Chat)localObject3).username)) {
                break label523;
              }
            }
            label507:
            label518:
            label523:
            for (paramAnonymousView = LocaleController.getString("ClearHistory", 2131493258);; paramAnonymousView = LocaleController.getString("ClearHistoryCache", 2131493259))
            {
              localObject1[1] = paramAnonymousView;
              localObject1[2] = LocaleController.getString("LeaveMegaMenu", 2131493743);
              paramAnonymousView = (View)localObject1;
              localObject1 = new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  boolean bool = true;
                  if (paramAnonymous2Int == 0)
                  {
                    paramAnonymous2DialogInterface = MessagesController.getInstance(DialogsActivity.this.currentAccount);
                    long l = DialogsActivity.this.selectedDialog;
                    if (!bool4) {}
                    for (;;)
                    {
                      if ((paramAnonymous2DialogInterface.pinDialog(l, bool, null, 0L)) && (!bool4)) {
                        DialogsActivity.this.listView.smoothScrollToPosition(0);
                      }
                      return;
                      bool = false;
                    }
                  }
                  paramAnonymous2DialogInterface = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                  paramAnonymous2DialogInterface.setTitle(LocaleController.getString("AppName", 2131492981));
                  if (paramAnonymous2Int == 1)
                  {
                    if ((localObject3 != null) && (localObject3.megagroup)) {
                      if (TextUtils.isEmpty(localObject3.username)) {
                        paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistory", 2131493000));
                      }
                    }
                    for (;;)
                    {
                      paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                      {
                        public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                        {
                          if ((DialogsActivity.7.2.this.val$chat != null) && (DialogsActivity.7.2.this.val$chat.megagroup) && (TextUtils.isEmpty(DialogsActivity.7.2.this.val$chat.username)))
                          {
                            MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteDialog(DialogsActivity.this.selectedDialog, 1);
                            return;
                          }
                          MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteDialog(DialogsActivity.this.selectedDialog, 2);
                        }
                      });
                      paramAnonymous2DialogInterface.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                      DialogsActivity.this.showDialog(paramAnonymous2DialogInterface.create());
                      return;
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistoryGroup", 2131493002));
                      continue;
                      paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", 2131493001));
                    }
                  }
                  if ((localObject3 != null) && (localObject3.megagroup)) {
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("MegaLeaveAlert", 2131493802));
                  }
                  for (;;)
                  {
                    paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                      {
                        MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteUserFromChat((int)-DialogsActivity.this.selectedDialog, UserConfig.getInstance(DialogsActivity.this.currentAccount).getCurrentUser(), null);
                        if (AndroidUtilities.isTablet()) {
                          NotificationCenter.getInstance(DialogsActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[] { Long.valueOf(DialogsActivity.this.selectedDialog) });
                        }
                      }
                    });
                    break;
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("ChannelLeaveAlert", 2131493173));
                  }
                }
              };
              localBuilder.setItems(paramAnonymousView, new int[] { paramAnonymousInt, 2131165268, 2131165270 }, (DialogInterface.OnClickListener)localObject1);
              DialogsActivity.this.showDialog(localBuilder.create());
              break;
              paramAnonymousInt = 2131165271;
              break label342;
              paramAnonymousView = LocaleController.getString("PinToTop", 2131494170);
              break label402;
              paramAnonymousView = null;
              break label402;
            }
            label534:
            localObject1 = new CharSequence[3];
            if ((((TLRPC.TL_dialog)localObject2).pinned) || (MessagesController.getInstance(DialogsActivity.this.currentAccount).canPinDialog(false))) {
              if (((TLRPC.TL_dialog)localObject2).pinned) {
                paramAnonymousView = LocaleController.getString("UnpinFromTop", 2131494513);
              }
            }
            for (;;)
            {
              localObject1[0] = paramAnonymousView;
              localObject1[1] = LocaleController.getString("ClearHistoryCache", 2131493259);
              localObject1[2] = LocaleController.getString("LeaveChannelMenu", 2131493742);
              paramAnonymousView = (View)localObject1;
              break;
              paramAnonymousView = LocaleController.getString("PinToTop", 2131494170);
              continue;
              paramAnonymousView = null;
            }
          }
          final boolean bool1;
          label644:
          final boolean bool2;
          label702:
          boolean bool3;
          if ((paramAnonymousInt < 0) && (i != 1))
          {
            bool1 = true;
            localObject1 = null;
            paramAnonymousView = (View)localObject1;
            if (!bool1)
            {
              paramAnonymousView = (View)localObject1;
              if (paramAnonymousInt > 0)
              {
                paramAnonymousView = (View)localObject1;
                if (i != 1) {
                  paramAnonymousView = MessagesController.getInstance(DialogsActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousInt));
                }
              }
            }
            if ((paramAnonymousView == null) || (!paramAnonymousView.bot)) {
              break label877;
            }
            bool2 = true;
            if (!((TLRPC.TL_dialog)localObject2).pinned)
            {
              paramAnonymousView = MessagesController.getInstance(DialogsActivity.this.currentAccount);
              if (paramAnonymousInt != 0) {
                break label883;
              }
              bool3 = true;
              label728:
              if (!paramAnonymousView.canPinDialog(bool3)) {
                break label900;
              }
            }
            if (!((TLRPC.TL_dialog)localObject2).pinned) {
              break label889;
            }
            paramAnonymousView = LocaleController.getString("UnpinFromTop", 2131494513);
            label753:
            localObject3 = LocaleController.getString("ClearHistory", 2131493258);
            if (!bool1) {
              break label905;
            }
            localObject1 = LocaleController.getString("DeleteChat", 2131493364);
            label778:
            if (!((TLRPC.TL_dialog)localObject2).pinned) {
              break label938;
            }
            paramAnonymousInt = 2131165272;
            label789:
            if (!bool1) {
              break label944;
            }
          }
          label877:
          label883:
          label889:
          label900:
          label905:
          label938:
          label944:
          for (i = 2131165270;; i = 2131165269)
          {
            localObject2 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, final int paramAnonymous2Int)
              {
                boolean bool = true;
                if (paramAnonymous2Int == 0)
                {
                  paramAnonymous2DialogInterface = MessagesController.getInstance(DialogsActivity.this.currentAccount);
                  long l = DialogsActivity.this.selectedDialog;
                  if (!bool4) {}
                  for (;;)
                  {
                    if ((paramAnonymous2DialogInterface.pinDialog(l, bool, null, 0L)) && (!bool4)) {
                      DialogsActivity.this.listView.smoothScrollToPosition(0);
                    }
                    return;
                    bool = false;
                  }
                }
                paramAnonymous2DialogInterface = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
                paramAnonymous2DialogInterface.setTitle(LocaleController.getString("AppName", 2131492981));
                if (paramAnonymous2Int == 1) {
                  paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureClearHistory", 2131493000));
                }
                for (;;)
                {
                  paramAnonymous2DialogInterface.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                    {
                      if (paramAnonymous2Int != 1)
                      {
                        if (DialogsActivity.7.3.this.val$isChat)
                        {
                          paramAnonymous3DialogInterface = MessagesController.getInstance(DialogsActivity.this.currentAccount).getChat(Integer.valueOf((int)-DialogsActivity.this.selectedDialog));
                          if ((paramAnonymous3DialogInterface != null) && (ChatObject.isNotInChat(paramAnonymous3DialogInterface))) {
                            MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteDialog(DialogsActivity.this.selectedDialog, 0);
                          }
                        }
                        for (;;)
                        {
                          if (DialogsActivity.7.3.this.val$isBot) {
                            MessagesController.getInstance(DialogsActivity.this.currentAccount).blockUser((int)DialogsActivity.this.selectedDialog);
                          }
                          if (AndroidUtilities.isTablet()) {
                            NotificationCenter.getInstance(DialogsActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[] { Long.valueOf(DialogsActivity.this.selectedDialog) });
                          }
                          return;
                          MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteUserFromChat((int)-DialogsActivity.this.selectedDialog, MessagesController.getInstance(DialogsActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(DialogsActivity.this.currentAccount).getClientUserId())), null);
                          continue;
                          MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteDialog(DialogsActivity.this.selectedDialog, 0);
                        }
                      }
                      MessagesController.getInstance(DialogsActivity.this.currentAccount).deleteDialog(DialogsActivity.this.selectedDialog, 1);
                    }
                  });
                  paramAnonymous2DialogInterface.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                  DialogsActivity.this.showDialog(paramAnonymous2DialogInterface.create());
                  return;
                  if (bool1) {
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", 2131493003));
                  } else {
                    paramAnonymous2DialogInterface.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", 2131493007));
                  }
                }
              }
            };
            localBuilder.setItems(new CharSequence[] { paramAnonymousView, localObject3, localObject1 }, new int[] { paramAnonymousInt, 2131165268, i }, (DialogInterface.OnClickListener)localObject2);
            DialogsActivity.this.showDialog(localBuilder.create());
            break;
            bool1 = false;
            break label644;
            bool2 = false;
            break label702;
            bool3 = false;
            break label728;
            paramAnonymousView = LocaleController.getString("PinToTop", 2131494170);
            break label753;
            paramAnonymousView = null;
            break label753;
            if (bool2)
            {
              localObject1 = LocaleController.getString("DeleteAndStop", 2131493362);
              break label778;
            }
            localObject1 = LocaleController.getString("Delete", 2131493356);
            break label778;
            paramAnonymousInt = 2131165271;
            break label789;
          }
        }
      });
      this.searchEmptyView = new EmptyTextProgressView(paramContext);
      this.searchEmptyView.setVisibility(8);
      this.searchEmptyView.setShowAtCenter(true);
      this.searchEmptyView.setText(LocaleController.getString("NoResult", 2131493906));
      ((SizeNotifierFrameLayout)localObject3).addView(this.searchEmptyView, LayoutHelper.createFrame(-1, -1.0F));
      this.progressView = new RadialProgressView(paramContext);
      this.progressView.setVisibility(8);
      ((SizeNotifierFrameLayout)localObject3).addView(this.progressView, LayoutHelper.createFrame(-2, -2, 17));
      this.floatingButton = new ImageView(paramContext);
      localObject1 = this.floatingButton;
      if (!this.onlySelect) {
        break label1582;
      }
      i = 8;
      ((ImageView)localObject1).setVisibility(i);
      this.floatingButton.setScaleType(ImageView.ScaleType.CENTER);
      localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("chats_actionBackground"), Theme.getColor("chats_actionPressedBackground"));
      localObject1 = localObject2;
      if (Build.VERSION.SDK_INT < 21)
      {
        localObject1 = paramContext.getResources().getDrawable(2131165322).mutate();
        ((Drawable)localObject1).setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
        localObject1 = new CombinedDrawable((Drawable)localObject1, (Drawable)localObject2, 0, 0);
        ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
      }
      this.floatingButton.setBackgroundDrawable((Drawable)localObject1);
      this.floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_actionIcon"), PorterDuff.Mode.MULTIPLY));
      this.floatingButton.setImageResource(2131165321);
      if (Build.VERSION.SDK_INT >= 21)
      {
        localObject1 = new StateListAnimator();
        localObject2 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        ((StateListAnimator)localObject1).addState(new int[] { 16842919 }, (Animator)localObject2);
        localObject2 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        ((StateListAnimator)localObject1).addState(new int[0], (Animator)localObject2);
        this.floatingButton.setStateListAnimator((StateListAnimator)localObject1);
        this.floatingButton.setOutlineProvider(new ViewOutlineProvider()
        {
          @SuppressLint({"NewApi"})
          public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
          {
            paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
          }
        });
      }
      localObject1 = this.floatingButton;
      if (Build.VERSION.SDK_INT < 21) {
        break label1588;
      }
      i = 56;
      if (Build.VERSION.SDK_INT < 21) {
        break label1595;
      }
      f1 = 56.0F;
      if (!LocaleController.isRTL) {
        break label1602;
      }
      j = 3;
      if (!LocaleController.isRTL) {
        break label1608;
      }
      f2 = 14.0F;
      if (!LocaleController.isRTL) {
        break label1613;
      }
      f3 = 0.0F;
      label1247:
      ((SizeNotifierFrameLayout)localObject3).addView((View)localObject1, LayoutHelper.createFrame(i, f1, j | 0x50, f2, 0.0F, f3, 14.0F));
      this.floatingButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = new Bundle();
          paramAnonymousView.putBoolean("destroyAfterSelect", true);
          DialogsActivity.this.presentFragment(new ContactsActivity(paramAnonymousView));
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == 1) && (DialogsActivity.this.searching) && (DialogsActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(DialogsActivity.this.getParentActivity().getCurrentFocus());
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int i = DialogsActivity.this.layoutManager.findFirstVisibleItemPosition();
          paramAnonymousInt1 = Math.abs(DialogsActivity.this.layoutManager.findLastVisibleItemPosition() - i) + 1;
          paramAnonymousInt2 = paramAnonymousRecyclerView.getAdapter().getItemCount();
          if ((DialogsActivity.this.searching) && (DialogsActivity.this.searchWas)) {
            if ((paramAnonymousInt1 > 0) && (DialogsActivity.this.layoutManager.findLastVisibleItemPosition() == paramAnonymousInt2 - 1) && (!DialogsActivity.this.dialogsSearchAdapter.isMessagesSearchEndReached())) {
              DialogsActivity.this.dialogsSearchAdapter.loadMoreSearchMessages();
            }
          }
          do
          {
            return;
            if ((paramAnonymousInt1 > 0) && (DialogsActivity.this.layoutManager.findLastVisibleItemPosition() >= DialogsActivity.this.getDialogsArray().size() - 10))
            {
              if (MessagesController.getInstance(DialogsActivity.this.currentAccount).dialogsEndReached) {
                break;
              }
              bool = true;
              if ((bool) || (!MessagesController.getInstance(DialogsActivity.this.currentAccount).serverDialogsEndReached)) {
                MessagesController.getInstance(DialogsActivity.this.currentAccount).loadDialogs(-1, 100, bool);
              }
            }
          } while (DialogsActivity.this.floatingButton.getVisibility() == 8);
          paramAnonymousRecyclerView = paramAnonymousRecyclerView.getChildAt(0);
          paramAnonymousInt2 = 0;
          if (paramAnonymousRecyclerView != null) {
            paramAnonymousInt2 = paramAnonymousRecyclerView.getTop();
          }
          paramAnonymousInt1 = 1;
          if (DialogsActivity.this.prevPosition == i)
          {
            paramAnonymousInt1 = DialogsActivity.this.prevTop;
            if (paramAnonymousInt2 < DialogsActivity.this.prevTop)
            {
              bool = true;
              label259:
              if (Math.abs(paramAnonymousInt1 - paramAnonymousInt2) <= 1) {
                break label335;
              }
            }
            label335:
            for (paramAnonymousInt1 = 1;; paramAnonymousInt1 = 0)
            {
              if ((paramAnonymousInt1 != 0) && (DialogsActivity.this.scrollUpdated)) {
                DialogsActivity.this.hideFloatingButton(bool);
              }
              DialogsActivity.access$6302(DialogsActivity.this, i);
              DialogsActivity.access$6402(DialogsActivity.this, paramAnonymousInt2);
              DialogsActivity.access$6502(DialogsActivity.this, true);
              return;
              bool = false;
              break;
              bool = false;
              break label259;
            }
          }
          if (i > DialogsActivity.this.prevPosition) {}
          for (boolean bool = true;; bool = false) {
            break;
          }
        }
      });
      if (this.searchString == null)
      {
        this.dialogsAdapter = new DialogsAdapter(paramContext, this.dialogsType, this.onlySelect);
        if ((AndroidUtilities.isTablet()) && (this.openedDialogId != 0L)) {
          this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
        }
        this.listView.setAdapter(this.dialogsAdapter);
      }
      i = 0;
      if (this.searchString == null) {
        break label1621;
      }
      i = 2;
      label1379:
      this.dialogsSearchAdapter = new DialogsSearchAdapter(paramContext, i, this.dialogsType);
      this.dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate()
      {
        public void didPressedOnSubDialog(long paramAnonymousLong)
        {
          if (DialogsActivity.this.onlySelect) {
            if (DialogsActivity.this.dialogsAdapter.hasSelectedDialogs())
            {
              DialogsActivity.this.dialogsAdapter.addOrRemoveSelectedDialog(paramAnonymousLong, null);
              DialogsActivity.this.updateSelectedCount();
              DialogsActivity.this.actionBar.closeSearchField();
            }
          }
          Bundle localBundle;
          label234:
          do
          {
            return;
            DialogsActivity.this.didSelectResult(paramAnonymousLong, true, false);
            return;
            int i = (int)paramAnonymousLong;
            localBundle = new Bundle();
            if (i > 0) {
              localBundle.putInt("user_id", i);
            }
            for (;;)
            {
              if (DialogsActivity.this.actionBar != null) {
                DialogsActivity.this.actionBar.closeSearchField();
              }
              if ((AndroidUtilities.isTablet()) && (DialogsActivity.this.dialogsAdapter != null))
              {
                DialogsActivity.this.dialogsAdapter.setOpenedDialogId(DialogsActivity.access$2902(DialogsActivity.this, paramAnonymousLong));
                DialogsActivity.this.updateVisibleRows(512);
              }
              if (DialogsActivity.this.searchString == null) {
                break label234;
              }
              if (!MessagesController.getInstance(DialogsActivity.this.currentAccount).checkCanOpenChat(localBundle, DialogsActivity.this)) {
                break;
              }
              NotificationCenter.getInstance(DialogsActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              DialogsActivity.this.presentFragment(new ChatActivity(localBundle));
              return;
              localBundle.putInt("chat_id", -i);
            }
          } while (!MessagesController.getInstance(DialogsActivity.this.currentAccount).checkCanOpenChat(localBundle, DialogsActivity.this));
          DialogsActivity.this.presentFragment(new ChatActivity(localBundle));
        }
        
        public void needRemoveHint(final int paramAnonymousInt)
        {
          if (DialogsActivity.this.getParentActivity() == null) {}
          TLRPC.User localUser;
          do
          {
            return;
            localUser = MessagesController.getInstance(DialogsActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousInt));
          } while (localUser == null);
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(DialogsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
          localBuilder.setMessage(LocaleController.formatString("ChatHintsDelete", 2131493225, new Object[] { ContactsController.formatName(localUser.first_name, localUser.last_name) }));
          localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              DataQuery.getInstance(DialogsActivity.this.currentAccount).removePeer(paramAnonymousInt);
            }
          });
          localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          DialogsActivity.this.showDialog(localBuilder.create());
        }
        
        public void searchStateChanged(boolean paramAnonymousBoolean)
        {
          if ((DialogsActivity.this.searching) && (DialogsActivity.this.searchWas) && (DialogsActivity.this.searchEmptyView != null))
          {
            if (paramAnonymousBoolean) {
              DialogsActivity.this.searchEmptyView.showProgress();
            }
          }
          else {
            return;
          }
          DialogsActivity.this.searchEmptyView.showTextView();
        }
      });
      if ((!MessagesController.getInstance(this.currentAccount).loadingDialogs) || (!MessagesController.getInstance(this.currentAccount).dialogs.isEmpty())) {
        break label1634;
      }
      this.searchEmptyView.setVisibility(8);
      this.listView.setEmptyView(this.progressView);
      label1461:
      if (this.searchString != null) {
        this.actionBar.openSearchField(this.searchString);
      }
      if ((this.onlySelect) || (this.dialogsType != 0)) {
        break label1663;
      }
      localObject1 = new FragmentContextView(paramContext, this, true);
      ((SizeNotifierFrameLayout)localObject3).addView((View)localObject1, LayoutHelper.createFrame(-1, 39.0F, 51, 0.0F, -36.0F, 0.0F, 0.0F));
      paramContext = new FragmentContextView(paramContext, this, false);
      ((SizeNotifierFrameLayout)localObject3).addView(paramContext, LayoutHelper.createFrame(-1, 39.0F, 51, 0.0F, -36.0F, 0.0F, 0.0F));
      paramContext.setAdditionalContextView((FragmentContextView)localObject1);
      ((FragmentContextView)localObject1).setAdditionalContextView(paramContext);
    }
    for (;;)
    {
      return this.fragmentView;
      i = 2;
      break;
      label1582:
      i = 0;
      break label885;
      label1588:
      i = 60;
      break label1207;
      label1595:
      f1 = 60.0F;
      break label1219;
      label1602:
      j = 5;
      break label1228;
      label1608:
      f2 = 0.0F;
      break label1238;
      label1613:
      f3 = 14.0F;
      break label1247;
      label1621:
      if (this.onlySelect) {
        break label1379;
      }
      i = 1;
      break label1379;
      label1634:
      this.searchEmptyView.setVisibility(8);
      this.progressView.setVisibility(8);
      this.listView.setEmptyView(null);
      break label1461;
      label1663:
      if ((this.dialogsType == 3) && (this.selectAlertString == null))
      {
        if (this.commentView != null) {
          this.commentView.onDestroy();
        }
        this.commentView = new ChatActivityEnterView(getParentActivity(), (SizeNotifierFrameLayout)localObject3, null, false);
        this.commentView.setAllowStickersAndGifs(false, false);
        this.commentView.setForceShowSendButton(true, false);
        this.commentView.setVisibility(8);
        ((SizeNotifierFrameLayout)localObject3).addView(this.commentView, LayoutHelper.createFrame(-1, -2, 83));
        this.commentView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate()
        {
          public void didPressedAttachButton() {}
          
          public void needChangeVideoPreviewState(int paramAnonymousInt, float paramAnonymousFloat) {}
          
          public void needSendTyping() {}
          
          public void needShowMediaBanHint() {}
          
          public void needStartRecordAudio(int paramAnonymousInt) {}
          
          public void needStartRecordVideo(int paramAnonymousInt) {}
          
          public void onAttachButtonHidden() {}
          
          public void onAttachButtonShow() {}
          
          public void onMessageEditEnd(boolean paramAnonymousBoolean) {}
          
          public void onMessageSend(CharSequence paramAnonymousCharSequence)
          {
            if (DialogsActivity.this.delegate == null) {}
            ArrayList localArrayList;
            do
            {
              return;
              localArrayList = DialogsActivity.this.dialogsAdapter.getSelectedDialogs();
            } while (localArrayList.isEmpty());
            DialogsActivity.this.delegate.didSelectDialogs(DialogsActivity.this, localArrayList, paramAnonymousCharSequence, false);
          }
          
          public void onPreAudioVideoRecord() {}
          
          public void onStickersExpandedChange() {}
          
          public void onStickersTab(boolean paramAnonymousBoolean) {}
          
          public void onSwitchRecordMode(boolean paramAnonymousBoolean) {}
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, boolean paramAnonymousBoolean) {}
          
          public void onWindowSizeChanged(int paramAnonymousInt) {}
        });
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.dialogsNeedReload) {
      if (this.dialogsAdapter != null)
      {
        if (this.dialogsAdapter.isDataSetChanged()) {
          this.dialogsAdapter.notifyDataSetChanged();
        }
      }
      else if (this.listView == null) {}
    }
    label331:
    label407:
    do
    {
      do
      {
        do
        {
          try
          {
            if ((MessagesController.getInstance(this.currentAccount).loadingDialogs) && (MessagesController.getInstance(this.currentAccount).dialogs.isEmpty()))
            {
              this.searchEmptyView.setVisibility(8);
              this.listView.setEmptyView(this.progressView);
              return;
              updateVisibleRows(2048);
              break;
            }
            this.progressView.setVisibility(8);
            if ((this.searching) && (this.searchWas))
            {
              this.listView.setEmptyView(this.searchEmptyView);
              return;
            }
          }
          catch (Exception paramVarArgs)
          {
            FileLog.e(paramVarArgs);
            return;
          }
          this.searchEmptyView.setVisibility(8);
          this.listView.setEmptyView(null);
          return;
          if (paramInt1 == NotificationCenter.emojiDidLoaded)
          {
            updateVisibleRows(0);
            return;
          }
          if (paramInt1 == NotificationCenter.updateInterfaces)
          {
            updateVisibleRows(((Integer)paramVarArgs[0]).intValue());
            return;
          }
          if (paramInt1 == NotificationCenter.appDidLogout)
          {
            dialogsLoaded[this.currentAccount] = false;
            return;
          }
          if (paramInt1 == NotificationCenter.encryptedChatUpdated)
          {
            updateVisibleRows(0);
            return;
          }
          if (paramInt1 == NotificationCenter.contactsDidLoaded)
          {
            updateVisibleRows(0);
            return;
          }
          if (paramInt1 != NotificationCenter.openedChatChanged) {
            break label331;
          }
        } while ((this.dialogsType != 0) || (!AndroidUtilities.isTablet()));
        boolean bool = ((Boolean)paramVarArgs[1]).booleanValue();
        long l = ((Long)paramVarArgs[0]).longValue();
        if (bool) {
          if (l != this.openedDialogId) {}
        }
        for (this.openedDialogId = 0L;; this.openedDialogId = l)
        {
          if (this.dialogsAdapter != null) {
            this.dialogsAdapter.setOpenedDialogId(this.openedDialogId);
          }
          updateVisibleRows(512);
          return;
        }
        if (paramInt1 == NotificationCenter.notificationsSettingsUpdated)
        {
          updateVisibleRows(0);
          return;
        }
        if ((paramInt1 == NotificationCenter.messageReceivedByAck) || (paramInt1 == NotificationCenter.messageReceivedByServer) || (paramInt1 == NotificationCenter.messageSendError))
        {
          updateVisibleRows(4096);
          return;
        }
        if (paramInt1 == NotificationCenter.didSetPasscode)
        {
          updatePasscodeButton();
          return;
        }
        if (paramInt1 != NotificationCenter.needReloadRecentDialogsSearch) {
          break label407;
        }
      } while (this.dialogsSearchAdapter == null);
      this.dialogsSearchAdapter.loadRecentSearch();
      return;
      if (paramInt1 == NotificationCenter.didLoadedReplyMessages)
      {
        updateVisibleRows(32768);
        return;
      }
    } while ((paramInt1 != NotificationCenter.reloadHints) || (this.dialogsSearchAdapter == null));
    this.dialogsSearchAdapter.notifyDataSetChanged();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    Object localObject1 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        int j;
        int i;
        Object localObject;
        if (DialogsActivity.this.listView != null)
        {
          j = DialogsActivity.this.listView.getChildCount();
          i = 0;
          if (i < j)
          {
            localObject = DialogsActivity.this.listView.getChildAt(i);
            if ((localObject instanceof ProfileSearchCell)) {
              ((ProfileSearchCell)localObject).update(0);
            }
            for (;;)
            {
              i += 1;
              break;
              if ((localObject instanceof DialogCell)) {
                ((DialogCell)localObject).update(0);
              }
            }
          }
        }
        if (DialogsActivity.this.dialogsSearchAdapter != null)
        {
          localObject = DialogsActivity.this.dialogsSearchAdapter.getInnerListView();
          if (localObject != null)
          {
            j = ((RecyclerListView)localObject).getChildCount();
            i = 0;
            while (i < j)
            {
              View localView = ((RecyclerListView)localObject).getChildAt(i);
              if ((localView instanceof HintDialogCell)) {
                ((HintDialogCell)localView).update();
              }
              i += 1;
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
    Object localObject2 = this.listView;
    Object localObject3 = Theme.dividerPaint;
    localObject2 = new ThemeDescription((View)localObject2, 0, new Class[] { View.class }, (Paint)localObject3, null, null, "divider");
    localObject3 = new ThemeDescription(this.searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { DialogsEmptyCell.class }, new String[] { "emptyTextView1" }, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { DialogsEmptyCell.class }, new String[] { "emptyTextView2" }, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "chats_actionIcon");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "chats_actionBackground");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "chats_actionPressedBackground");
    Object localObject4 = this.listView;
    Object localObject5 = Theme.avatar_photoDrawable;
    Object localObject6 = Theme.avatar_broadcastDrawable;
    Object localObject7 = Theme.avatar_savedDrawable;
    localObject4 = new ThemeDescription((View)localObject4, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, null, new Drawable[] { localObject5, localObject6, localObject7 }, null, "avatar_text");
    localObject5 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundRed");
    localObject6 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundOrange");
    localObject7 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundViolet");
    ThemeDescription localThemeDescription16 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundGreen");
    ThemeDescription localThemeDescription17 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundCyan");
    ThemeDescription localThemeDescription18 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundBlue");
    ThemeDescription localThemeDescription19 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundPink");
    ThemeDescription localThemeDescription20 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "avatar_backgroundSaved");
    Object localObject8 = this.listView;
    Object localObject9 = Theme.dialogs_countPaint;
    localObject8 = new ThemeDescription((View)localObject8, 0, new Class[] { DialogCell.class }, (Paint)localObject9, null, null, "chats_unreadCounter");
    localObject9 = this.listView;
    Object localObject10 = Theme.dialogs_countGrayPaint;
    localObject9 = new ThemeDescription((View)localObject9, 0, new Class[] { DialogCell.class }, (Paint)localObject10, null, null, "chats_unreadCounterMuted");
    localObject10 = this.listView;
    Object localObject11 = Theme.dialogs_countTextPaint;
    localObject10 = new ThemeDescription((View)localObject10, 0, new Class[] { DialogCell.class }, (Paint)localObject11, null, null, "chats_unreadCounterText");
    localObject11 = this.listView;
    Object localObject12 = Theme.dialogs_namePaint;
    localObject11 = new ThemeDescription((View)localObject11, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, (Paint)localObject12, null, null, "chats_name");
    localObject12 = this.listView;
    Object localObject13 = Theme.dialogs_nameEncryptedPaint;
    localObject12 = new ThemeDescription((View)localObject12, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, (Paint)localObject13, null, null, "chats_secretName");
    localObject13 = this.listView;
    Object localObject14 = Theme.dialogs_lockDrawable;
    localObject13 = new ThemeDescription((View)localObject13, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, null, new Drawable[] { localObject14 }, null, "chats_secretIcon");
    localObject14 = this.listView;
    Object localObject15 = Theme.dialogs_groupDrawable;
    Object localObject16 = Theme.dialogs_broadcastDrawable;
    Object localObject17 = Theme.dialogs_botDrawable;
    localObject14 = new ThemeDescription((View)localObject14, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, null, new Drawable[] { localObject15, localObject16, localObject17 }, null, "chats_nameIcon");
    localObject15 = this.listView;
    localObject16 = Theme.dialogs_pinnedDrawable;
    localObject15 = new ThemeDescription((View)localObject15, 0, new Class[] { DialogCell.class }, null, new Drawable[] { localObject16 }, null, "chats_pinnedIcon");
    localObject16 = this.listView;
    localObject17 = Theme.dialogs_messagePaint;
    localObject16 = new ThemeDescription((View)localObject16, 0, new Class[] { DialogCell.class }, (Paint)localObject17, null, null, "chats_message");
    localObject17 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "chats_nameMessage");
    ThemeDescription localThemeDescription21 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "chats_draft");
    ThemeDescription localThemeDescription22 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject1, "chats_attachMessage");
    localObject1 = this.listView;
    Object localObject18 = Theme.dialogs_messagePrintingPaint;
    localObject18 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, (Paint)localObject18, null, null, "chats_actionMessage");
    localObject1 = this.listView;
    Object localObject19 = Theme.dialogs_timePaint;
    localObject19 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, (Paint)localObject19, null, null, "chats_date");
    localObject1 = this.listView;
    Object localObject20 = Theme.dialogs_pinnedPaint;
    localObject20 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, (Paint)localObject20, null, null, "chats_pinnedOverlay");
    localObject1 = this.listView;
    Object localObject21 = Theme.dialogs_tabletSeletedPaint;
    localObject21 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, (Paint)localObject21, null, null, "chats_tabletSelectedOverlay");
    localObject1 = this.listView;
    Object localObject22 = Theme.dialogs_checkDrawable;
    Object localObject23 = Theme.dialogs_halfCheckDrawable;
    localObject22 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, null, new Drawable[] { localObject22, localObject23 }, null, "chats_sentCheck");
    localObject1 = this.listView;
    localObject23 = Theme.dialogs_clockDrawable;
    localObject23 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, null, new Drawable[] { localObject23 }, null, "chats_sentClock");
    localObject1 = this.listView;
    Object localObject24 = Theme.dialogs_errorPaint;
    localObject24 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, (Paint)localObject24, null, null, "chats_sentError");
    localObject1 = this.listView;
    Object localObject25 = Theme.dialogs_errorDrawable;
    localObject25 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, null, new Drawable[] { localObject25 }, null, "chats_sentErrorIcon");
    localObject1 = this.listView;
    Object localObject26 = Theme.dialogs_verifiedCheckDrawable;
    localObject26 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, null, new Drawable[] { localObject26 }, null, "chats_verifiedCheck");
    localObject1 = this.listView;
    Object localObject27 = Theme.dialogs_verifiedDrawable;
    localObject27 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class, ProfileSearchCell.class }, null, new Drawable[] { localObject27 }, null, "chats_verifiedBackground");
    localObject1 = this.listView;
    Object localObject28 = Theme.dialogs_muteDrawable;
    localObject28 = new ThemeDescription((View)localObject1, 0, new Class[] { DialogCell.class }, null, new Drawable[] { localObject28 }, null, "chats_muteIcon");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "chats_menuBackground");
    ThemeDescription localThemeDescription24 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chats_menuName");
    ThemeDescription localThemeDescription25 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chats_menuPhone");
    ThemeDescription localThemeDescription26 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chats_menuPhoneCats");
    ThemeDescription localThemeDescription27 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chats_menuCloudBackgroundCats");
    ThemeDescription localThemeDescription28 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chat_serviceBackground");
    ThemeDescription localThemeDescription29 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerProfileCell.class }, null, null, null, "chats_menuTopShadow");
    ThemeDescription localThemeDescription30 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { DrawerProfileCell.class }, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription31 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { DrawerActionCell.class }, new String[] { "textView" }, null, null, null, "chats_menuItemIcon");
    ThemeDescription localThemeDescription32 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerActionCell.class }, new String[] { "textView" }, null, null, null, "chats_menuItemText");
    ThemeDescription localThemeDescription33 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerUserCell.class }, new String[] { "textView" }, null, null, null, "chats_menuItemText");
    ThemeDescription localThemeDescription34 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { DrawerUserCell.class }, new String[] { "checkBox" }, null, null, null, "chats_unreadCounterText");
    ThemeDescription localThemeDescription35 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { DrawerUserCell.class }, new String[] { "checkBox" }, null, null, null, "chats_unreadCounter");
    ThemeDescription localThemeDescription36 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { DrawerUserCell.class }, new String[] { "checkBox" }, null, null, null, "chats_menuBackground");
    ThemeDescription localThemeDescription37 = new ThemeDescription(this.sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { DrawerAddCell.class }, new String[] { "textView" }, null, null, null, "chats_menuItemIcon");
    ThemeDescription localThemeDescription38 = new ThemeDescription(this.sideMenu, 0, new Class[] { DrawerAddCell.class }, new String[] { "textView" }, null, null, null, "chats_menuItemText");
    localObject1 = this.sideMenu;
    Object localObject29 = Theme.dividerPaint;
    localObject29 = new ThemeDescription((View)localObject1, 0, new Class[] { DividerCell.class }, (Paint)localObject29, null, null, "divider");
    ThemeDescription localThemeDescription39 = new ThemeDescription(this.listView, 0, new Class[] { LoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle");
    localObject1 = this.listView;
    Object localObject30 = Theme.dialogs_offlinePaint;
    localObject30 = new ThemeDescription((View)localObject1, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject30, null, null, "windowBackgroundWhiteGrayText3");
    localObject1 = this.listView;
    Object localObject31 = Theme.dialogs_onlinePaint;
    localObject31 = new ThemeDescription((View)localObject1, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject31, null, null, "windowBackgroundWhiteBlueText3");
    ThemeDescription localThemeDescription40 = new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2");
    ThemeDescription localThemeDescription41 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection");
    ThemeDescription localThemeDescription42 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { HashtagSearchCell.class }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription43 = new ThemeDescription(this.progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle");
    Object localObject32;
    label2544:
    Object localObject33;
    label2592:
    Object localObject34;
    if (this.dialogsSearchAdapter != null)
    {
      localObject1 = this.dialogsSearchAdapter.getInnerListView();
      localObject32 = Theme.dialogs_countPaint;
      localObject32 = new ThemeDescription((View)localObject1, 0, new Class[] { HintDialogCell.class }, (Paint)localObject32, null, null, "chats_unreadCounter");
      if (this.dialogsSearchAdapter == null) {
        break label4473;
      }
      localObject1 = this.dialogsSearchAdapter.getInnerListView();
      localObject33 = Theme.dialogs_countGrayPaint;
      localObject33 = new ThemeDescription((View)localObject1, 0, new Class[] { HintDialogCell.class }, (Paint)localObject33, null, null, "chats_unreadCounterMuted");
      if (this.dialogsSearchAdapter == null) {
        break label4478;
      }
      localObject1 = this.dialogsSearchAdapter.getInnerListView();
      localObject34 = Theme.dialogs_countTextPaint;
      localObject34 = new ThemeDescription((View)localObject1, 0, new Class[] { HintDialogCell.class }, (Paint)localObject34, null, null, "chats_unreadCounterText");
      if (this.dialogsSearchAdapter == null) {
        break label4483;
      }
    }
    label4473:
    label4478:
    label4483:
    for (localObject1 = this.dialogsSearchAdapter.getInnerListView();; localObject1 = null)
    {
      return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localObject2, localObject3, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localObject4, localObject5, localObject6, localObject7, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localObject8, localObject9, localObject10, localObject11, localObject12, localObject13, localObject14, localObject15, localObject16, localObject17, localThemeDescription21, localThemeDescription22, localObject18, localObject19, localObject20, localObject21, localObject22, localObject23, localObject24, localObject25, localObject26, localObject27, localObject28, localThemeDescription23, localThemeDescription24, localThemeDescription25, localThemeDescription26, localThemeDescription27, localThemeDescription28, localThemeDescription29, localThemeDescription30, localThemeDescription31, localThemeDescription32, localThemeDescription33, localThemeDescription34, localThemeDescription35, localThemeDescription36, localThemeDescription37, localThemeDescription38, localObject29, localThemeDescription39, localObject30, localObject31, localThemeDescription40, localThemeDescription41, localThemeDescription42, localThemeDescription43, localObject32, localObject33, localObject34, new ThemeDescription((View)localObject1, 0, new Class[] { HintDialogCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[] { FragmentContextView.class }, new String[] { "frameLayout" }, null, null, null, "inappPlayerBackground"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { FragmentContextView.class }, new String[] { "playButton" }, null, null, null, "inappPlayerPlayPause"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { FragmentContextView.class }, new String[] { "titleTextView" }, null, null, null, "inappPlayerTitle"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_FASTSCROLL, new Class[] { FragmentContextView.class }, new String[] { "titleTextView" }, null, null, null, "inappPlayerPerformer"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { FragmentContextView.class }, new String[] { "closeButton" }, null, null, null, "inappPlayerClose"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[] { FragmentContextView.class }, new String[] { "frameLayout" }, null, null, null, "returnToCallBackground"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { FragmentContextView.class }, new String[] { "titleTextView" }, null, null, null, "returnToCallText"), new ThemeDescription(null, 0, null, null, null, null, "dialogBackground"), new ThemeDescription(null, 0, null, null, null, null, "dialogBackgroundGray"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextBlack"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextLink"), new ThemeDescription(null, 0, null, null, null, null, "dialogLinkSelection"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextBlue"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextBlue2"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextBlue3"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextBlue4"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextRed"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextGray"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextGray2"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextGray3"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextGray4"), new ThemeDescription(null, 0, null, null, null, null, "dialogIcon"), new ThemeDescription(null, 0, null, null, null, null, "dialogTextHint"), new ThemeDescription(null, 0, null, null, null, null, "dialogInputField"), new ThemeDescription(null, 0, null, null, null, null, "dialogInputFieldActivated"), new ThemeDescription(null, 0, null, null, null, null, "dialogCheckboxSquareBackground"), new ThemeDescription(null, 0, null, null, null, null, "dialogCheckboxSquareCheck"), new ThemeDescription(null, 0, null, null, null, null, "dialogCheckboxSquareUnchecked"), new ThemeDescription(null, 0, null, null, null, null, "dialogCheckboxSquareDisabled"), new ThemeDescription(null, 0, null, null, null, null, "dialogRadioBackground"), new ThemeDescription(null, 0, null, null, null, null, "dialogRadioBackgroundChecked"), new ThemeDescription(null, 0, null, null, null, null, "dialogProgressCircle"), new ThemeDescription(null, 0, null, null, null, null, "dialogButton"), new ThemeDescription(null, 0, null, null, null, null, "dialogButtonSelector"), new ThemeDescription(null, 0, null, null, null, null, "dialogScrollGlow"), new ThemeDescription(null, 0, null, null, null, null, "dialogRoundCheckBox"), new ThemeDescription(null, 0, null, null, null, null, "dialogRoundCheckBoxCheck"), new ThemeDescription(null, 0, null, null, null, null, "dialogBadgeBackground"), new ThemeDescription(null, 0, null, null, null, null, "dialogBadgeText"), new ThemeDescription(null, 0, null, null, null, null, "dialogLineProgress"), new ThemeDescription(null, 0, null, null, null, null, "dialogLineProgressBackground"), new ThemeDescription(null, 0, null, null, null, null, "dialogGrayLine"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBar"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBarSelector"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBarTitle"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBarTop"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBarSubtitle"), new ThemeDescription(null, 0, null, null, null, null, "player_actionBarItems"), new ThemeDescription(null, 0, null, null, null, null, "player_background"), new ThemeDescription(null, 0, null, null, null, null, "player_time"), new ThemeDescription(null, 0, null, null, null, null, "player_progressBackground"), new ThemeDescription(null, 0, null, null, null, null, "key_player_progressCachedBackground"), new ThemeDescription(null, 0, null, null, null, null, "player_progress"), new ThemeDescription(null, 0, null, null, null, null, "player_placeholder"), new ThemeDescription(null, 0, null, null, null, null, "player_placeholderBackground"), new ThemeDescription(null, 0, null, null, null, null, "player_button"), new ThemeDescription(null, 0, null, null, null, null, "player_buttonActive") };
      localObject1 = null;
      break;
      localObject1 = null;
      break label2544;
      localObject1 = null;
      break label2592;
    }
  }
  
  public boolean isMainDialogList()
  {
    return (this.delegate == null) && (this.searchString == null);
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if ((!this.onlySelect) && (this.floatingButton != null)) {
      this.floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
      {
        public void onGlobalLayout()
        {
          ImageView localImageView = DialogsActivity.this.floatingButton;
          float f;
          if (DialogsActivity.this.floatingHidden)
          {
            f = AndroidUtilities.dp(100.0F);
            localImageView.setTranslationY(f);
            localImageView = DialogsActivity.this.floatingButton;
            if (DialogsActivity.this.floatingHidden) {
              break label85;
            }
          }
          label85:
          for (boolean bool = true;; bool = false)
          {
            localImageView.setClickable(bool);
            if (DialogsActivity.this.floatingButton != null) {
              DialogsActivity.this.floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            return;
            f = 0.0F;
            break;
          }
        }
      });
    }
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
    if (getArguments() != null)
    {
      this.onlySelect = this.arguments.getBoolean("onlySelect", false);
      this.cantSendToChannels = this.arguments.getBoolean("cantSendToChannels", false);
      this.dialogsType = this.arguments.getInt("dialogsType", 0);
      this.selectAlertString = this.arguments.getString("selectAlertString");
      this.selectAlertStringGroup = this.arguments.getString("selectAlertStringGroup");
      this.addToGroupAlertString = this.arguments.getString("addToGroupAlertString");
      this.allowSwitchAccount = this.arguments.getBoolean("allowSwitchAccount");
    }
    if (this.searchString == null)
    {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.dialogsNeedReload);
      NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.encryptedChatUpdated);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.openedChatChanged);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.notificationsSettingsUpdated);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messageReceivedByAck);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messageReceivedByServer);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messageSendError);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didLoadedReplyMessages);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.reloadHints);
      NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
    }
    if (dialogsLoaded[this.currentAccount] == 0)
    {
      MessagesController.getInstance(this.currentAccount).loadDialogs(0, 100, true);
      MessagesController.getInstance(this.currentAccount).loadHintDialogs();
      ContactsController.getInstance(this.currentAccount).checkInviteText();
      MessagesController.getInstance(this.currentAccount).loadPinnedDialogs(0L, null);
      DataQuery.getInstance(this.currentAccount).loadRecents(2, false, true, false);
      DataQuery.getInstance(this.currentAccount).checkFeaturedStickers();
      dialogsLoaded[this.currentAccount] = true;
    }
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    if (this.searchString == null)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.dialogsNeedReload);
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.encryptedChatUpdated);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openedChatChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messageReceivedByAck);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messageReceivedByServer);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messageSendError);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didLoadedReplyMessages);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.reloadHints);
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
    }
    if (this.commentView != null) {
      this.commentView.onDestroy();
    }
    this.delegate = null;
  }
  
  public void onPause()
  {
    super.onPause();
    if (this.commentView != null) {
      this.commentView.onResume();
    }
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 1)
    {
      int i = 0;
      if (i < paramArrayOfString.length)
      {
        if ((paramArrayOfInt.length <= i) || (paramArrayOfInt[i] != 0)) {}
        for (;;)
        {
          i += 1;
          break;
          String str = paramArrayOfString[i];
          paramInt = -1;
          switch (str.hashCode())
          {
          }
          for (;;)
          {
            switch (paramInt)
            {
            default: 
              break;
            case 0: 
              ContactsController.getInstance(this.currentAccount).forceImportContacts();
              break;
              if (str.equals("android.permission.READ_CONTACTS"))
              {
                paramInt = 0;
                continue;
                if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                  paramInt = 1;
                }
              }
              break;
            }
          }
          ImageLoader.getInstance().checkMediaPaths();
        }
      }
    }
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.dialogsAdapter != null) {
      this.dialogsAdapter.notifyDataSetChanged();
    }
    if (this.commentView != null) {
      this.commentView.onResume();
    }
    if (this.dialogsSearchAdapter != null) {
      this.dialogsSearchAdapter.notifyDataSetChanged();
    }
    Object localObject;
    if ((this.checkPermission) && (!this.onlySelect) && (Build.VERSION.SDK_INT >= 23))
    {
      localObject = getParentActivity();
      if (localObject != null)
      {
        this.checkPermission = false;
        if ((((Activity)localObject).checkSelfPermission("android.permission.READ_CONTACTS") != 0) || (((Activity)localObject).checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0))
        {
          if (!((Activity)localObject).shouldShowRequestPermissionRationale("android.permission.READ_CONTACTS")) {
            break label181;
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
    label181:
    if (((Activity)localObject).shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE"))
    {
      localObject = new AlertDialog.Builder((Context)localObject);
      ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
      ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("PermissionStorage", 2131494148));
      ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      localObject = ((AlertDialog.Builder)localObject).create();
      this.permissionDialog = ((AlertDialog)localObject);
      showDialog((Dialog)localObject);
      return;
    }
    askForPermissons();
  }
  
  public void setDelegate(DialogsActivityDelegate paramDialogsActivityDelegate)
  {
    this.delegate = paramDialogsActivityDelegate;
  }
  
  public void setSearchString(String paramString)
  {
    this.searchString = paramString;
  }
  
  public void setSideMenu(RecyclerView paramRecyclerView)
  {
    this.sideMenu = paramRecyclerView;
    this.sideMenu.setBackgroundColor(Theme.getColor("chats_menuBackground"));
    this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
  }
  
  public static abstract interface DialogsActivityDelegate
  {
    public abstract void didSelectDialogs(DialogsActivity paramDialogsActivity, ArrayList<Long> paramArrayList, CharSequence paramCharSequence, boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/DialogsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */