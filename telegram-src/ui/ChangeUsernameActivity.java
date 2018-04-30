package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_checkUsername;
import org.telegram.tgnet.TLRPC.TL_account_updateUsername;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class ChangeUsernameActivity
  extends BaseFragment
{
  private static final int done_button = 1;
  private int checkReqId;
  private Runnable checkRunnable;
  private TextView checkTextView;
  private View doneButton;
  private EditTextBoldCursor firstNameField;
  private TextView helpTextView;
  private boolean ignoreCheck;
  private CharSequence infoText;
  private String lastCheckName;
  private boolean lastNameAvailable;
  
  private boolean checkUserName(final String paramString, boolean paramBoolean)
  {
    if ((paramString != null) && (paramString.length() > 0))
    {
      this.checkTextView.setVisibility(0);
      if ((!paramBoolean) || (paramString.length() != 0)) {
        break label44;
      }
    }
    label44:
    do
    {
      return true;
      this.checkTextView.setVisibility(8);
      break;
      if (this.checkRunnable != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.checkRunnable);
        this.checkRunnable = null;
        this.lastCheckName = null;
        if (this.checkReqId != 0) {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.checkReqId, true);
        }
      }
      this.lastNameAvailable = false;
      if (paramString != null)
      {
        if ((paramString.startsWith("_")) || (paramString.endsWith("_")))
        {
          this.checkTextView.setText(LocaleController.getString("UsernameInvalid", 2131494563));
          this.checkTextView.setTag("windowBackgroundWhiteRedText4");
          this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
          return false;
        }
        int i = 0;
        while (i < paramString.length())
        {
          int j = paramString.charAt(i);
          if ((i == 0) && (j >= 48) && (j <= 57))
          {
            if (paramBoolean) {
              AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidStartNumber", 2131494566));
            }
            for (;;)
            {
              return false;
              this.checkTextView.setText(LocaleController.getString("UsernameInvalidStartNumber", 2131494566));
              this.checkTextView.setTag("windowBackgroundWhiteRedText4");
              this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
            }
          }
          if (((j < 48) || (j > 57)) && ((j < 97) || (j > 122)) && ((j < 65) || (j > 90)) && (j != 95))
          {
            if (paramBoolean) {
              AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalid", 2131494563));
            }
            for (;;)
            {
              return false;
              this.checkTextView.setText(LocaleController.getString("UsernameInvalid", 2131494563));
              this.checkTextView.setTag("windowBackgroundWhiteRedText4");
              this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
            }
          }
          i += 1;
        }
      }
      if ((paramString == null) || (paramString.length() < 5))
      {
        if (paramBoolean) {
          AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidShort", 2131494565));
        }
        for (;;)
        {
          return false;
          this.checkTextView.setText(LocaleController.getString("UsernameInvalidShort", 2131494565));
          this.checkTextView.setTag("windowBackgroundWhiteRedText4");
          this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
        }
      }
      if (paramString.length() > 32)
      {
        if (paramBoolean) {
          AlertsCreator.showSimpleAlert(this, LocaleController.getString("UsernameInvalidLong", 2131494564));
        }
        for (;;)
        {
          return false;
          this.checkTextView.setText(LocaleController.getString("UsernameInvalidLong", 2131494564));
          this.checkTextView.setTag("windowBackgroundWhiteRedText4");
          this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
        }
      }
    } while (paramBoolean);
    String str2 = UserConfig.getInstance(this.currentAccount).getCurrentUser().username;
    String str1 = str2;
    if (str2 == null) {
      str1 = "";
    }
    if (paramString.equals(str1))
    {
      this.checkTextView.setText(LocaleController.formatString("UsernameAvailable", 2131494557, new Object[] { paramString }));
      this.checkTextView.setTag("windowBackgroundWhiteGreenText");
      this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGreenText"));
      return true;
    }
    this.checkTextView.setText(LocaleController.getString("UsernameChecking", 2131494558));
    this.checkTextView.setTag("windowBackgroundWhiteGrayText8");
    this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText8"));
    this.lastCheckName = paramString;
    this.checkRunnable = new Runnable()
    {
      public void run()
      {
        TLRPC.TL_account_checkUsername localTL_account_checkUsername = new TLRPC.TL_account_checkUsername();
        localTL_account_checkUsername.username = paramString;
        ChangeUsernameActivity.access$902(ChangeUsernameActivity.this, ConnectionsManager.getInstance(ChangeUsernameActivity.this.currentAccount).sendRequest(localTL_account_checkUsername, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                ChangeUsernameActivity.access$902(ChangeUsernameActivity.this, 0);
                if ((ChangeUsernameActivity.this.lastCheckName != null) && (ChangeUsernameActivity.this.lastCheckName.equals(ChangeUsernameActivity.5.this.val$name)))
                {
                  if ((paramAnonymous2TL_error == null) && ((paramAnonymous2TLObject instanceof TLRPC.TL_boolTrue)))
                  {
                    ChangeUsernameActivity.this.checkTextView.setText(LocaleController.formatString("UsernameAvailable", 2131494557, new Object[] { ChangeUsernameActivity.5.this.val$name }));
                    ChangeUsernameActivity.this.checkTextView.setTag("windowBackgroundWhiteGreenText");
                    ChangeUsernameActivity.this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGreenText"));
                    ChangeUsernameActivity.access$1202(ChangeUsernameActivity.this, true);
                  }
                }
                else {
                  return;
                }
                ChangeUsernameActivity.this.checkTextView.setText(LocaleController.getString("UsernameInUse", 2131494562));
                ChangeUsernameActivity.this.checkTextView.setTag("windowBackgroundWhiteRedText4");
                ChangeUsernameActivity.this.checkTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText4"));
                ChangeUsernameActivity.access$1202(ChangeUsernameActivity.this, false);
              }
            });
          }
        }, 2));
      }
    };
    AndroidUtilities.runOnUIThread(this.checkRunnable, 300L);
    return true;
  }
  
  private void saveName()
  {
    if (!checkUserName(this.firstNameField.getText().toString(), true)) {}
    do
    {
      return;
      localObject = UserConfig.getInstance(this.currentAccount).getCurrentUser();
    } while ((getParentActivity() == null) || (localObject == null));
    String str = ((TLRPC.User)localObject).username;
    final Object localObject = str;
    if (str == null) {
      localObject = "";
    }
    str = this.firstNameField.getText().toString();
    if (((String)localObject).equals(str))
    {
      finishFragment();
      return;
    }
    localObject = new AlertDialog(getParentActivity(), 1);
    ((AlertDialog)localObject).setMessage(LocaleController.getString("Loading", 2131493762));
    ((AlertDialog)localObject).setCanceledOnTouchOutside(false);
    ((AlertDialog)localObject).setCancelable(false);
    final TLRPC.TL_account_updateUsername localTL_account_updateUsername = new TLRPC.TL_account_updateUsername();
    localTL_account_updateUsername.username = str;
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1) });
    final int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_updateUsername, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              try
              {
                ChangeUsernameActivity.6.this.val$progressDialog.dismiss();
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(paramAnonymousTLObject);
                MessagesController.getInstance(ChangeUsernameActivity.this.currentAccount).putUsers(localArrayList, false);
                MessagesStorage.getInstance(ChangeUsernameActivity.this.currentAccount).putUsersAndChats(localArrayList, null, false, true);
                UserConfig.getInstance(ChangeUsernameActivity.this.currentAccount).saveConfig(true);
                ChangeUsernameActivity.this.finishFragment();
                return;
              }
              catch (Exception localException)
              {
                for (;;)
                {
                  FileLog.e(localException);
                }
              }
            }
          });
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            try
            {
              ChangeUsernameActivity.6.this.val$progressDialog.dismiss();
              AlertsCreator.processError(ChangeUsernameActivity.this.currentAccount, paramAnonymousTL_error, ChangeUsernameActivity.this, ChangeUsernameActivity.6.this.val$req, new Object[0]);
              return;
            }
            catch (Exception localException)
            {
              for (;;)
              {
                FileLog.e(localException);
              }
            }
          }
        });
      }
    }, 2);
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, this.classGuid);
    ((AlertDialog)localObject).setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        ConnectionsManager.getInstance(ChangeUsernameActivity.this.currentAccount).cancelRequest(i, true);
        try
        {
          paramAnonymousDialogInterface.dismiss();
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
        }
      }
    });
    ((AlertDialog)localObject).show();
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("Username", 2131494556));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          ChangeUsernameActivity.this.finishFragment();
        }
        while (paramAnonymousInt != 1) {
          return;
        }
        ChangeUsernameActivity.this.saveName();
      }
    });
    this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    Object localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
    Object localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = UserConfig.getInstance(this.currentAccount).getCurrentUser();
    }
    this.fragmentView = new LinearLayout(paramContext);
    localObject2 = (LinearLayout)this.fragmentView;
    ((LinearLayout)localObject2).setOrientation(1);
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
    this.firstNameField.setPadding(0, 0, 0, 0);
    this.firstNameField.setSingleLine(true);
    Object localObject3 = this.firstNameField;
    if (LocaleController.isRTL)
    {
      i = 5;
      ((EditTextBoldCursor)localObject3).setGravity(i);
      this.firstNameField.setInputType(180224);
      this.firstNameField.setImeOptions(6);
      this.firstNameField.setHint(LocaleController.getString("UsernamePlaceholder", 2131494567));
      this.firstNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.firstNameField.setCursorSize(AndroidUtilities.dp(20.0F));
      this.firstNameField.setCursorWidth(1.5F);
      this.firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
      {
        public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousInt == 6) && (ChangeUsernameActivity.this.doneButton != null))
          {
            ChangeUsernameActivity.this.doneButton.performClick();
            return true;
          }
          return false;
        }
      });
      this.firstNameField.addTextChangedListener(new TextWatcher()
      {
        public void afterTextChanged(Editable paramAnonymousEditable)
        {
          if (ChangeUsernameActivity.this.firstNameField.length() > 0)
          {
            paramAnonymousEditable = "https://" + MessagesController.getInstance(ChangeUsernameActivity.this.currentAccount).linkPrefix + "/" + ChangeUsernameActivity.this.firstNameField.getText();
            Object localObject = LocaleController.formatString("UsernameHelpLink", 2131494561, new Object[] { paramAnonymousEditable });
            int i = ((String)localObject).indexOf(paramAnonymousEditable);
            localObject = new SpannableStringBuilder((CharSequence)localObject);
            if (i >= 0) {
              ((SpannableStringBuilder)localObject).setSpan(new ChangeUsernameActivity.LinkSpan(ChangeUsernameActivity.this, paramAnonymousEditable), i, paramAnonymousEditable.length() + i, 33);
            }
            ChangeUsernameActivity.this.helpTextView.setText(TextUtils.concat(new CharSequence[] { ChangeUsernameActivity.this.infoText, "\n\n", localObject }));
            return;
          }
          ChangeUsernameActivity.this.helpTextView.setText(ChangeUsernameActivity.this.infoText);
        }
        
        public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        
        public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
        {
          if (ChangeUsernameActivity.this.ignoreCheck) {
            return;
          }
          ChangeUsernameActivity.this.checkUserName(ChangeUsernameActivity.this.firstNameField.getText().toString(), false);
        }
      });
      ((LinearLayout)localObject2).addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 24.0F, 24.0F, 24.0F, 0.0F));
      this.checkTextView = new TextView(paramContext);
      this.checkTextView.setTextSize(1, 15.0F);
      localObject3 = this.checkTextView;
      if (!LocaleController.isRTL) {
        break label714;
      }
      i = 5;
      label438:
      ((TextView)localObject3).setGravity(i);
      localObject3 = this.checkTextView;
      if (!LocaleController.isRTL) {
        break label719;
      }
      i = 5;
      label458:
      ((LinearLayout)localObject2).addView((View)localObject3, LayoutHelper.createLinear(-2, -2, i, 24, 12, 24, 0));
      this.helpTextView = new TextView(paramContext);
      this.helpTextView.setTextSize(1, 15.0F);
      this.helpTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText8"));
      paramContext = this.helpTextView;
      if (!LocaleController.isRTL) {
        break label724;
      }
      i = 5;
      label528:
      paramContext.setGravity(i);
      paramContext = this.helpTextView;
      localObject3 = AndroidUtilities.replaceTags(LocaleController.getString("UsernameHelp", 2131494560));
      this.infoText = ((CharSequence)localObject3);
      paramContext.setText((CharSequence)localObject3);
      this.helpTextView.setLinkTextColor(Theme.getColor("windowBackgroundWhiteLinkText"));
      this.helpTextView.setHighlightColor(Theme.getColor("windowBackgroundWhiteLinkSelection"));
      this.helpTextView.setMovementMethod(new LinkMovementMethodMy(null));
      paramContext = this.helpTextView;
      if (!LocaleController.isRTL) {
        break label729;
      }
    }
    label714:
    label719:
    label724:
    label729:
    for (int i = 5;; i = 3)
    {
      ((LinearLayout)localObject2).addView(paramContext, LayoutHelper.createLinear(-2, -2, i, 24, 10, 24, 0));
      this.checkTextView.setVisibility(8);
      if ((localObject1 != null) && (((TLRPC.User)localObject1).username != null) && (((TLRPC.User)localObject1).username.length() > 0))
      {
        this.ignoreCheck = true;
        this.firstNameField.setText(((TLRPC.User)localObject1).username);
        this.firstNameField.setSelection(this.firstNameField.length());
        this.ignoreCheck = false;
      }
      return this.fragmentView;
      i = 3;
      break;
      i = 3;
      break label438;
      i = 3;
      break label458;
      i = 3;
      break label528;
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.helpTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText8"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteRedText4"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteGreenText"), new ThemeDescription(this.checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteGrayText8") };
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
    if (paramBoolean1)
    {
      this.firstNameField.requestFocus();
      AndroidUtilities.showKeyboard(this.firstNameField);
    }
  }
  
  private static class LinkMovementMethodMy
    extends LinkMovementMethod
  {
    public boolean onTouchEvent(TextView paramTextView, Spannable paramSpannable, MotionEvent paramMotionEvent)
    {
      try
      {
        boolean bool = super.onTouchEvent(paramTextView, paramSpannable, paramMotionEvent);
        if ((paramMotionEvent.getAction() == 1) || (paramMotionEvent.getAction() == 3)) {
          Selection.removeSelection(paramSpannable);
        }
        return bool;
      }
      catch (Exception paramTextView)
      {
        FileLog.e(paramTextView);
      }
      return false;
    }
  }
  
  public class LinkSpan
    extends ClickableSpan
  {
    private String url;
    
    public LinkSpan(String paramString)
    {
      this.url = paramString;
    }
    
    public void onClick(View paramView)
    {
      try
      {
        ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", this.url));
        Toast.makeText(ChangeUsernameActivity.this.getParentActivity(), LocaleController.getString("LinkCopied", 2131493748), 0).show();
        return;
      }
      catch (Exception paramView)
      {
        FileLog.e(paramView);
      }
    }
    
    public void updateDrawState(TextPaint paramTextPaint)
    {
      super.updateDrawState(paramTextPaint);
      paramTextPaint.setUnderlineText(false);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChangeUsernameActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */