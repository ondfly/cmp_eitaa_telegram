package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_updateProfile;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class ChangeNameActivity
  extends BaseFragment
{
  private static final int done_button = 1;
  private View doneButton;
  private EditTextBoldCursor firstNameField;
  private View headerLabelView;
  private EditTextBoldCursor lastNameField;
  
  private void saveName()
  {
    TLRPC.User localUser = UserConfig.getInstance(this.currentAccount).getCurrentUser();
    if ((localUser == null) || (this.lastNameField.getText() == null) || (this.firstNameField.getText() == null)) {}
    String str1;
    String str2;
    do
    {
      return;
      str1 = this.firstNameField.getText().toString();
      str2 = this.lastNameField.getText().toString();
    } while ((localUser.first_name != null) && (localUser.first_name.equals(str1)) && (localUser.last_name != null) && (localUser.last_name.equals(str2)));
    TLRPC.TL_account_updateProfile localTL_account_updateProfile = new TLRPC.TL_account_updateProfile();
    localTL_account_updateProfile.flags = 3;
    localTL_account_updateProfile.first_name = str1;
    localUser.first_name = str1;
    localTL_account_updateProfile.last_name = str2;
    localUser.last_name = str2;
    localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
    if (localUser != null)
    {
      localUser.first_name = localTL_account_updateProfile.first_name;
      localUser.last_name = localTL_account_updateProfile.last_name;
    }
    UserConfig.getInstance(this.currentAccount).saveConfig(true);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1) });
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_updateProfile, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public View createView(Context paramContext)
  {
    int j = 5;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("EditName", 2131493416));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChangeNameActivity.this.finishFragment();
        }
        while ((paramAnonymousInt != 1) || (ChangeNameActivity.this.firstNameField.getText().length() == 0)) {
          return;
        }
        ChangeNameActivity.this.saveName();
        ChangeNameActivity.this.finishFragment();
      }
    });
    this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    Object localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
    Object localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = UserConfig.getInstance(this.currentAccount).getCurrentUser();
    }
    localObject2 = new LinearLayout(paramContext);
    this.fragmentView = ((View)localObject2);
    this.fragmentView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
    ((LinearLayout)this.fragmentView).setOrientation(1);
    this.fragmentView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.firstNameField = new EditTextBoldCursor(paramContext);
    this.firstNameField.setTextSize(1, 18.0F);
    this.firstNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.firstNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
    this.firstNameField.setMaxLines(1);
    this.firstNameField.setLines(1);
    this.firstNameField.setSingleLine(true);
    EditTextBoldCursor localEditTextBoldCursor = this.firstNameField;
    if (LocaleController.isRTL)
    {
      i = 5;
      localEditTextBoldCursor.setGravity(i);
      this.firstNameField.setInputType(49152);
      this.firstNameField.setImeOptions(5);
      this.firstNameField.setHint(LocaleController.getString("FirstName", 2131493542));
      this.firstNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.firstNameField.setCursorSize(AndroidUtilities.dp(20.0F));
      this.firstNameField.setCursorWidth(1.5F);
      ((LinearLayout)localObject2).addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 24.0F, 24.0F, 24.0F, 0.0F));
      this.firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if (paramAnonymousInt == 5)
          {
            ChangeNameActivity.this.lastNameField.requestFocus();
            ChangeNameActivity.this.lastNameField.setSelection(ChangeNameActivity.this.lastNameField.length());
            return true;
          }
          return false;
        }
      });
      this.lastNameField = new EditTextBoldCursor(paramContext);
      this.lastNameField.setTextSize(1, 18.0F);
      this.lastNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
      this.lastNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.lastNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
      this.lastNameField.setMaxLines(1);
      this.lastNameField.setLines(1);
      this.lastNameField.setSingleLine(true);
      paramContext = this.lastNameField;
      if (!LocaleController.isRTL) {
        break label648;
      }
    }
    label648:
    for (int i = j;; i = 3)
    {
      paramContext.setGravity(i);
      this.lastNameField.setInputType(49152);
      this.lastNameField.setImeOptions(6);
      this.lastNameField.setHint(LocaleController.getString("LastName", 2131493726));
      this.lastNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.lastNameField.setCursorSize(AndroidUtilities.dp(20.0F));
      this.lastNameField.setCursorWidth(1.5F);
      ((LinearLayout)localObject2).addView(this.lastNameField, LayoutHelper.createLinear(-1, 36, 24.0F, 16.0F, 24.0F, 0.0F));
      this.lastNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if (paramAnonymousInt == 6)
          {
            ChangeNameActivity.this.doneButton.performClick();
            return true;
          }
          return false;
        }
      });
      if (localObject1 != null)
      {
        this.firstNameField.setText(((TLRPC.User)localObject1).first_name);
        this.firstNameField.setSelection(this.firstNameField.length());
        this.lastNameField.setText(((TLRPC.User)localObject1).last_name);
      }
      return this.fragmentView;
      i = 3;
      break;
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated") };
  }
  
  public void onResume()
  {
    super.onResume();
    if (!MessagesController.getGlobalMainSettings().getBoolean("view_animations", true))
    {
      this.firstNameField.requestFocus();
      AndroidUtilities.showKeyboard(this.firstNameField);
    }
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (ChangeNameActivity.this.firstNameField != null)
          {
            ChangeNameActivity.this.firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(ChangeNameActivity.this.firstNameField);
          }
        }
      }, 100L);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChangeNameActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */