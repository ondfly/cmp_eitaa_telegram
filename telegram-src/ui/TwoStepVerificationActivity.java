package org.telegram.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.security.SecureRandom;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_getPasswordSettings;
import org.telegram.tgnet.TLRPC.TL_account_noPassword;
import org.telegram.tgnet.TLRPC.TL_account_password;
import org.telegram.tgnet.TLRPC.TL_account_passwordInputSettings;
import org.telegram.tgnet.TLRPC.TL_account_updatePasswordSettings;
import org.telegram.tgnet.TLRPC.TL_auth_passwordRecovery;
import org.telegram.tgnet.TLRPC.TL_auth_recoverPassword;
import org.telegram.tgnet.TLRPC.TL_auth_requestPasswordRecovery;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.account_Password;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class TwoStepVerificationActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private int abortPasswordRow;
  private TextView bottomButton;
  private TextView bottomTextView;
  private int changePasswordRow;
  private int changeRecoveryEmailRow;
  private TLRPC.account_Password currentPassword;
  private byte[] currentPasswordHash = new byte[0];
  private boolean destroyed;
  private ActionBarMenuItem doneItem;
  private String email;
  private boolean emailOnly;
  private EmptyTextProgressView emptyView;
  private String firstPassword;
  private String hint;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private boolean loading;
  private EditTextBoldCursor passwordEditText;
  private int passwordEmailVerifyDetailRow;
  private int passwordEnabledDetailRow;
  private boolean passwordEntered = true;
  private int passwordSetState;
  private int passwordSetupDetailRow;
  private AlertDialog progressDialog;
  private int rowCount;
  private ScrollView scrollView;
  private int setPasswordDetailRow;
  private int setPasswordRow;
  private int setRecoveryEmailRow;
  private int shadowRow;
  private Runnable shortPollRunnable;
  private TextView titleTextView;
  private int turnPasswordOffRow;
  private int type;
  private boolean waitingForEmail;
  
  public TwoStepVerificationActivity(int paramInt)
  {
    this.type = paramInt;
    if (paramInt == 0) {
      loadPasswordInfo(false);
    }
  }
  
  private boolean isValidEmail(String paramString)
  {
    if ((paramString == null) || (paramString.length() < 3)) {}
    int i;
    int j;
    do
    {
      return false;
      i = paramString.lastIndexOf('.');
      j = paramString.lastIndexOf('@');
    } while ((i < 0) || (j < 0) || (i < j));
    return true;
  }
  
  private void loadPasswordInfo(final boolean paramBoolean)
  {
    if (!paramBoolean)
    {
      this.loading = true;
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
    }
    TLRPC.TL_account_getPassword localTL_account_getPassword = new TLRPC.TL_account_getPassword();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_getPassword, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            boolean bool2 = true;
            TwoStepVerificationActivity.access$1902(TwoStepVerificationActivity.this, false);
            Object localObject;
            if (paramAnonymousTL_error == null)
            {
              if (!TwoStepVerificationActivity.7.this.val$silent)
              {
                localObject = TwoStepVerificationActivity.this;
                if ((TwoStepVerificationActivity.this.currentPassword == null) && (!(paramAnonymousTLObject instanceof TLRPC.TL_account_noPassword))) {
                  break label282;
                }
                bool1 = true;
                TwoStepVerificationActivity.access$2002((TwoStepVerificationActivity)localObject, bool1);
              }
              TwoStepVerificationActivity.access$202(TwoStepVerificationActivity.this, (TLRPC.account_Password)paramAnonymousTLObject);
              localObject = TwoStepVerificationActivity.this;
              if (TwoStepVerificationActivity.this.currentPassword.email_unconfirmed_pattern.length() <= 0) {
                break label287;
              }
            }
            label282:
            label287:
            for (boolean bool1 = bool2;; bool1 = false)
            {
              TwoStepVerificationActivity.access$2102((TwoStepVerificationActivity)localObject, bool1);
              localObject = new byte[TwoStepVerificationActivity.this.currentPassword.new_salt.length + 8];
              Utilities.random.nextBytes((byte[])localObject);
              System.arraycopy(TwoStepVerificationActivity.this.currentPassword.new_salt, 0, localObject, 0, TwoStepVerificationActivity.this.currentPassword.new_salt.length);
              TwoStepVerificationActivity.this.currentPassword.new_salt = ((byte[])localObject);
              if ((TwoStepVerificationActivity.this.type == 0) && (!TwoStepVerificationActivity.this.destroyed) && (TwoStepVerificationActivity.this.shortPollRunnable == null))
              {
                TwoStepVerificationActivity.access$2302(TwoStepVerificationActivity.this, new Runnable()
                {
                  public void run()
                  {
                    if (TwoStepVerificationActivity.this.shortPollRunnable == null) {
                      return;
                    }
                    TwoStepVerificationActivity.this.loadPasswordInfo(true);
                    TwoStepVerificationActivity.access$2302(TwoStepVerificationActivity.this, null);
                  }
                });
                AndroidUtilities.runOnUIThread(TwoStepVerificationActivity.this.shortPollRunnable, 5000L);
              }
              TwoStepVerificationActivity.this.updateRows();
              return;
              bool1 = false;
              break;
            }
          }
        });
      }
    }, 10);
  }
  
  private void needHideProgress()
  {
    if (this.progressDialog == null) {
      return;
    }
    try
    {
      this.progressDialog.dismiss();
      this.progressDialog = null;
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
  
  private void needShowProgress()
  {
    if ((getParentActivity() == null) || (getParentActivity().isFinishing()) || (this.progressDialog != null)) {
      return;
    }
    this.progressDialog = new AlertDialog(getParentActivity(), 1);
    this.progressDialog.setMessage(LocaleController.getString("Loading", 2131493762));
    this.progressDialog.setCanceledOnTouchOutside(false);
    this.progressDialog.setCancelable(false);
    this.progressDialog.show();
  }
  
  private void onPasscodeError(boolean paramBoolean)
  {
    if (getParentActivity() == null) {
      return;
    }
    Vibrator localVibrator = (Vibrator)getParentActivity().getSystemService("vibrator");
    if (localVibrator != null) {
      localVibrator.vibrate(200L);
    }
    if (paramBoolean) {
      this.passwordEditText.setText("");
    }
    AndroidUtilities.shakeView(this.titleTextView, 2.0F, 0);
  }
  
  private void processDone()
  {
    Object localObject2;
    if (this.type == 0) {
      if (!this.passwordEntered)
      {
        localObject2 = this.passwordEditText.getText().toString();
        if (((String)localObject2).length() != 0) {
          break label38;
        }
        onPasscodeError(false);
      }
    }
    label38:
    do
    {
      do
      {
        return;
        final Object localObject1 = null;
        try
        {
          localObject2 = ((String)localObject2).getBytes("UTF-8");
          localObject1 = localObject2;
        }
        catch (Exception localException3)
        {
          for (;;)
          {
            FileLog.e(localException3);
          }
        }
        needShowProgress();
        localObject2 = new byte[this.currentPassword.current_salt.length * 2 + localObject1.length];
        System.arraycopy(this.currentPassword.current_salt, 0, localObject2, 0, this.currentPassword.current_salt.length);
        System.arraycopy(localObject1, 0, localObject2, this.currentPassword.current_salt.length, localObject1.length);
        System.arraycopy(this.currentPassword.current_salt, 0, localObject2, localObject2.length - this.currentPassword.current_salt.length, this.currentPassword.current_salt.length);
        localObject1 = new TLRPC.TL_account_getPasswordSettings();
        ((TLRPC.TL_account_getPasswordSettings)localObject1).current_password_hash = Utilities.computeSHA256((byte[])localObject2, 0, localObject2.length);
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                TwoStepVerificationActivity.this.needHideProgress();
                if (paramAnonymousTL_error == null)
                {
                  TwoStepVerificationActivity.access$1202(TwoStepVerificationActivity.this, TwoStepVerificationActivity.10.this.val$req.current_password_hash);
                  TwoStepVerificationActivity.access$2002(TwoStepVerificationActivity.this, true);
                  AndroidUtilities.hideKeyboard(TwoStepVerificationActivity.this.passwordEditText);
                  TwoStepVerificationActivity.this.updateRows();
                  return;
                }
                if (paramAnonymousTL_error.text.equals("PASSWORD_HASH_INVALID"))
                {
                  TwoStepVerificationActivity.this.onPasscodeError(true);
                  return;
                }
                if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
                {
                  int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
                  if (i < 60) {}
                  for (String str = LocaleController.formatPluralString("Seconds", i);; str = LocaleController.formatPluralString("Minutes", i / 60))
                  {
                    TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { str }));
                    return;
                  }
                }
                TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
              }
            });
          }
        }, 10);
        return;
      } while (this.type != 1);
      if (this.passwordSetState == 0)
      {
        if (this.passwordEditText.getText().length() == 0)
        {
          onPasscodeError(false);
          return;
        }
        this.titleTextView.setText(LocaleController.getString("ReEnterYourPasscode", 2131494214));
        this.firstPassword = this.passwordEditText.getText().toString();
        setPasswordSetState(1);
        return;
      }
      if (this.passwordSetState == 1)
      {
        if (!this.firstPassword.equals(this.passwordEditText.getText().toString())) {
          try
          {
            Toast.makeText(getParentActivity(), LocaleController.getString("PasswordDoNotMatch", 2131494073), 0).show();
            onPasscodeError(true);
            return;
          }
          catch (Exception localException1)
          {
            for (;;)
            {
              FileLog.e(localException1);
            }
          }
        }
        setPasswordSetState(2);
        return;
      }
      if (this.passwordSetState == 2)
      {
        this.hint = this.passwordEditText.getText().toString();
        if (this.hint.toLowerCase().equals(this.firstPassword.toLowerCase())) {
          try
          {
            Toast.makeText(getParentActivity(), LocaleController.getString("PasswordAsHintError", 2131494071), 0).show();
            onPasscodeError(false);
            return;
          }
          catch (Exception localException2)
          {
            for (;;)
            {
              FileLog.e(localException2);
            }
          }
        }
        if (!this.currentPassword.has_recovery)
        {
          setPasswordSetState(3);
          return;
        }
        this.email = "";
        setNewPassword(false);
        return;
      }
      if (this.passwordSetState == 3)
      {
        this.email = this.passwordEditText.getText().toString();
        if (!isValidEmail(this.email))
        {
          onPasscodeError(false);
          return;
        }
        setNewPassword(false);
        return;
      }
    } while (this.passwordSetState != 4);
    String str = this.passwordEditText.getText().toString();
    if (str.length() == 0)
    {
      onPasscodeError(false);
      return;
    }
    TLRPC.TL_auth_recoverPassword localTL_auth_recoverPassword = new TLRPC.TL_auth_recoverPassword();
    localTL_auth_recoverPassword.code = str;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_auth_recoverPassword, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            Object localObject;
            if (paramAnonymousTL_error == null)
            {
              localObject = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
              ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                {
                  NotificationCenter.getInstance(TwoStepVerificationActivity.this.currentAccount).postNotificationName(NotificationCenter.didSetTwoStepPassword, new Object[0]);
                  TwoStepVerificationActivity.this.finishFragment();
                }
              });
              ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("PasswordReset", 2131494078));
              ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
              localObject = TwoStepVerificationActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
              if (localObject != null)
              {
                ((Dialog)localObject).setCanceledOnTouchOutside(false);
                ((Dialog)localObject).setCancelable(false);
              }
              return;
            }
            if (paramAnonymousTL_error.text.startsWith("CODE_INVALID"))
            {
              TwoStepVerificationActivity.this.onPasscodeError(true);
              return;
            }
            if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
            {
              int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
              if (i < 60) {}
              for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
              {
                TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                return;
              }
            }
            TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
          }
        });
      }
    }, 10);
  }
  
  private void setNewPassword(final boolean paramBoolean)
  {
    final TLRPC.TL_account_updatePasswordSettings localTL_account_updatePasswordSettings = new TLRPC.TL_account_updatePasswordSettings();
    localTL_account_updatePasswordSettings.current_password_hash = this.currentPasswordHash;
    localTL_account_updatePasswordSettings.new_settings = new TLRPC.TL_account_passwordInputSettings();
    if (paramBoolean) {
      if ((this.waitingForEmail) && ((this.currentPassword instanceof TLRPC.TL_account_noPassword)))
      {
        localTL_account_updatePasswordSettings.new_settings.flags = 2;
        localTL_account_updatePasswordSettings.new_settings.email = "";
        localTL_account_updatePasswordSettings.current_password_hash = new byte[0];
      }
    }
    for (;;)
    {
      needShowProgress();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_updatePasswordSettings, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TwoStepVerificationActivity.this.needHideProgress();
              if ((paramAnonymousTL_error == null) && ((paramAnonymousTLObject instanceof TLRPC.TL_boolTrue))) {
                if (TwoStepVerificationActivity.9.this.val$clear)
                {
                  TwoStepVerificationActivity.access$202(TwoStepVerificationActivity.this, null);
                  TwoStepVerificationActivity.access$1202(TwoStepVerificationActivity.this, new byte[0]);
                  TwoStepVerificationActivity.this.loadPasswordInfo(false);
                  NotificationCenter.getInstance(TwoStepVerificationActivity.this.currentAccount).postNotificationName(NotificationCenter.didRemovedTwoStepPassword, new Object[0]);
                  TwoStepVerificationActivity.this.updateRows();
                }
              }
              label107:
              Object localObject;
              do
              {
                do
                {
                  do
                  {
                    break label107;
                    do
                    {
                      return;
                    } while (TwoStepVerificationActivity.this.getParentActivity() == null);
                    localObject = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
                    ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                      {
                        NotificationCenter.getInstance(TwoStepVerificationActivity.this.currentAccount).postNotificationName(NotificationCenter.didSetTwoStepPassword, new Object[] { TwoStepVerificationActivity.9.this.val$req.new_settings.new_password_hash });
                        TwoStepVerificationActivity.this.finishFragment();
                      }
                    });
                    ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("YourPasswordSuccessText", 2131494671));
                    ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("YourPasswordSuccess", 2131494670));
                    localObject = TwoStepVerificationActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
                  } while (localObject == null);
                  ((Dialog)localObject).setCanceledOnTouchOutside(false);
                  ((Dialog)localObject).setCancelable(false);
                  return;
                } while (paramAnonymousTL_error == null);
                if (!paramAnonymousTL_error.text.equals("EMAIL_UNCONFIRMED")) {
                  break;
                }
                localObject = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
                ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                  {
                    NotificationCenter.getInstance(TwoStepVerificationActivity.this.currentAccount).postNotificationName(NotificationCenter.didSetTwoStepPassword, new Object[] { TwoStepVerificationActivity.9.this.val$req.new_settings.new_password_hash });
                    TwoStepVerificationActivity.this.finishFragment();
                  }
                });
                ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("YourEmailAlmostThereText", 2131494663));
                ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("YourEmailAlmostThere", 2131494662));
                localObject = TwoStepVerificationActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
              } while (localObject == null);
              ((Dialog)localObject).setCanceledOnTouchOutside(false);
              ((Dialog)localObject).setCancelable(false);
              return;
              if (paramAnonymousTL_error.text.equals("EMAIL_INVALID"))
              {
                TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.getString("PasswordEmailInvalid", 2131494074));
                return;
              }
              if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
              {
                int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
                if (i < 60) {}
                for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
                {
                  TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                  return;
                }
              }
              TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
            }
          });
        }
      }, 10);
      return;
      localTL_account_updatePasswordSettings.new_settings.flags = 3;
      localTL_account_updatePasswordSettings.new_settings.hint = "";
      localTL_account_updatePasswordSettings.new_settings.new_password_hash = new byte[0];
      localTL_account_updatePasswordSettings.new_settings.new_salt = new byte[0];
      localTL_account_updatePasswordSettings.new_settings.email = "";
      continue;
      if ((this.firstPassword != null) && (this.firstPassword.length() > 0)) {
        localObject = null;
      }
      try
      {
        arrayOfByte1 = this.firstPassword.getBytes("UTF-8");
        localObject = arrayOfByte1;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          byte[] arrayOfByte1;
          byte[] arrayOfByte2;
          FileLog.e(localException);
        }
      }
      arrayOfByte1 = this.currentPassword.new_salt;
      arrayOfByte2 = new byte[arrayOfByte1.length * 2 + localObject.length];
      System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
      System.arraycopy(localObject, 0, arrayOfByte2, arrayOfByte1.length, localObject.length);
      System.arraycopy(arrayOfByte1, 0, arrayOfByte2, arrayOfByte2.length - arrayOfByte1.length, arrayOfByte1.length);
      Object localObject = localTL_account_updatePasswordSettings.new_settings;
      ((TLRPC.TL_account_passwordInputSettings)localObject).flags |= 0x1;
      localTL_account_updatePasswordSettings.new_settings.hint = this.hint;
      localTL_account_updatePasswordSettings.new_settings.new_password_hash = Utilities.computeSHA256(arrayOfByte2, 0, arrayOfByte2.length);
      localTL_account_updatePasswordSettings.new_settings.new_salt = arrayOfByte1;
      if (this.email.length() > 0)
      {
        localObject = localTL_account_updatePasswordSettings.new_settings;
        ((TLRPC.TL_account_passwordInputSettings)localObject).flags |= 0x2;
        localTL_account_updatePasswordSettings.new_settings.email = this.email.trim();
      }
    }
  }
  
  private void setPasswordSetState(int paramInt)
  {
    int i = 4;
    if (this.passwordEditText == null) {
      return;
    }
    this.passwordSetState = paramInt;
    if (this.passwordSetState == 0)
    {
      this.actionBar.setTitle(LocaleController.getString("YourPassword", 2131494669));
      if ((this.currentPassword instanceof TLRPC.TL_account_noPassword))
      {
        this.titleTextView.setText(LocaleController.getString("PleaseEnterFirstPassword", 2131494175));
        this.passwordEditText.setImeOptions(5);
        this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        this.bottomTextView.setVisibility(4);
        this.bottomButton.setVisibility(4);
      }
    }
    for (;;)
    {
      this.passwordEditText.setText("");
      return;
      this.titleTextView.setText(LocaleController.getString("PleaseEnterPassword", 2131494176));
      break;
      if (this.passwordSetState == 1)
      {
        this.actionBar.setTitle(LocaleController.getString("YourPassword", 2131494669));
        this.titleTextView.setText(LocaleController.getString("PleaseReEnterPassword", 2131494177));
        this.passwordEditText.setImeOptions(5);
        this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        this.bottomTextView.setVisibility(4);
        this.bottomButton.setVisibility(4);
      }
      else if (this.passwordSetState == 2)
      {
        this.actionBar.setTitle(LocaleController.getString("PasswordHint", 2131494075));
        this.titleTextView.setText(LocaleController.getString("PasswordHintText", 2131494076));
        this.passwordEditText.setImeOptions(5);
        this.passwordEditText.setTransformationMethod(null);
        this.bottomTextView.setVisibility(4);
        this.bottomButton.setVisibility(4);
      }
      else
      {
        if (this.passwordSetState == 3)
        {
          this.actionBar.setTitle(LocaleController.getString("RecoveryEmail", 2131494227));
          this.titleTextView.setText(LocaleController.getString("YourEmail", 2131494661));
          this.passwordEditText.setImeOptions(6);
          this.passwordEditText.setTransformationMethod(null);
          this.passwordEditText.setInputType(33);
          this.bottomTextView.setVisibility(0);
          TextView localTextView = this.bottomButton;
          if (this.emailOnly) {}
          for (paramInt = i;; paramInt = 0)
          {
            localTextView.setVisibility(paramInt);
            break;
          }
        }
        if (this.passwordSetState == 4)
        {
          this.actionBar.setTitle(LocaleController.getString("PasswordRecovery", 2131494077));
          this.titleTextView.setText(LocaleController.getString("PasswordCode", 2131494072));
          this.bottomTextView.setText(LocaleController.getString("RestoreEmailSentInfo", 2131494268));
          this.bottomButton.setText(LocaleController.formatString("RestoreEmailTrouble", 2131494269, new Object[] { this.currentPassword.email_unconfirmed_pattern }));
          this.passwordEditText.setImeOptions(6);
          this.passwordEditText.setTransformationMethod(null);
          this.passwordEditText.setInputType(3);
          this.bottomTextView.setVisibility(0);
          this.bottomButton.setVisibility(0);
        }
      }
    }
  }
  
  private void showAlertWithText(String paramString1, String paramString2)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    localBuilder.setTitle(paramString1);
    localBuilder.setMessage(paramString2);
    showDialog(localBuilder.create());
  }
  
  private void updateRows()
  {
    this.rowCount = 0;
    this.setPasswordRow = -1;
    this.setPasswordDetailRow = -1;
    this.changePasswordRow = -1;
    this.turnPasswordOffRow = -1;
    this.setRecoveryEmailRow = -1;
    this.changeRecoveryEmailRow = -1;
    this.abortPasswordRow = -1;
    this.passwordSetupDetailRow = -1;
    this.passwordEnabledDetailRow = -1;
    this.passwordEmailVerifyDetailRow = -1;
    this.shadowRow = -1;
    int i;
    if ((!this.loading) && (this.currentPassword != null))
    {
      if (!(this.currentPassword instanceof TLRPC.TL_account_noPassword)) {
        break label291;
      }
      if (this.waitingForEmail)
      {
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.passwordSetupDetailRow = i;
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.abortPasswordRow = i;
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.shadowRow = i;
      }
    }
    else
    {
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      if (!this.passwordEntered) {
        break label429;
      }
      if (this.listView != null)
      {
        this.listView.setVisibility(0);
        this.scrollView.setVisibility(4);
        this.emptyView.setVisibility(0);
        this.listView.setEmptyView(this.emptyView);
      }
      if (this.passwordEditText != null)
      {
        this.doneItem.setVisibility(8);
        this.passwordEditText.setVisibility(4);
        this.titleTextView.setVisibility(4);
        this.bottomTextView.setVisibility(4);
        this.bottomButton.setVisibility(4);
      }
    }
    label291:
    label409:
    label429:
    do
    {
      return;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.setPasswordRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.setPasswordDetailRow = i;
      break;
      if (!(this.currentPassword instanceof TLRPC.TL_account_password)) {
        break;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.changePasswordRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.turnPasswordOffRow = i;
      if (this.currentPassword.has_recovery)
      {
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.changeRecoveryEmailRow = i;
      }
      for (;;)
      {
        if (!this.waitingForEmail) {
          break label409;
        }
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.passwordEmailVerifyDetailRow = i;
        break;
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.setRecoveryEmailRow = i;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.passwordEnabledDetailRow = i;
      break;
      if (this.listView != null)
      {
        this.listView.setEmptyView(null);
        this.listView.setVisibility(4);
        this.scrollView.setVisibility(0);
        this.emptyView.setVisibility(4);
      }
    } while (this.passwordEditText == null);
    this.doneItem.setVisibility(0);
    this.passwordEditText.setVisibility(0);
    this.titleTextView.setVisibility(0);
    this.bottomButton.setVisibility(0);
    this.bottomTextView.setVisibility(4);
    this.bottomButton.setText(LocaleController.getString("ForgotPassword", 2131493547));
    if ((this.currentPassword.hint != null) && (this.currentPassword.hint.length() > 0)) {
      this.passwordEditText.setHint(this.currentPassword.hint);
    }
    for (;;)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (TwoStepVerificationActivity.this.passwordEditText != null)
          {
            TwoStepVerificationActivity.this.passwordEditText.requestFocus();
            AndroidUtilities.showKeyboard(TwoStepVerificationActivity.this.passwordEditText);
          }
        }
      }, 200L);
      return;
      this.passwordEditText.setHint("");
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(false);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          TwoStepVerificationActivity.this.finishFragment();
        }
        while (paramAnonymousInt != 1) {
          return;
        }
        TwoStepVerificationActivity.this.processDone();
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    Object localObject1 = (FrameLayout)this.fragmentView;
    ((FrameLayout)localObject1).setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.doneItem = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.scrollView = new ScrollView(paramContext);
    this.scrollView.setFillViewport(true);
    ((FrameLayout)localObject1).addView(this.scrollView, LayoutHelper.createFrame(-1, -1.0F));
    Object localObject2 = new LinearLayout(paramContext);
    ((LinearLayout)localObject2).setOrientation(1);
    this.scrollView.addView((View)localObject2, LayoutHelper.createScroll(-1, -2, 51));
    this.titleTextView = new TextView(paramContext);
    this.titleTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
    this.titleTextView.setTextSize(1, 18.0F);
    this.titleTextView.setGravity(1);
    ((LinearLayout)localObject2).addView(this.titleTextView, LayoutHelper.createLinear(-2, -2, 1, 0, 38, 0, 0));
    this.passwordEditText = new EditTextBoldCursor(paramContext);
    this.passwordEditText.setTextSize(1, 20.0F);
    this.passwordEditText.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.passwordEditText.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.passwordEditText.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
    this.passwordEditText.setMaxLines(1);
    this.passwordEditText.setLines(1);
    this.passwordEditText.setGravity(1);
    this.passwordEditText.setSingleLine(true);
    this.passwordEditText.setInputType(129);
    this.passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    this.passwordEditText.setTypeface(Typeface.DEFAULT);
    this.passwordEditText.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.passwordEditText.setCursorSize(AndroidUtilities.dp(20.0F));
    this.passwordEditText.setCursorWidth(1.5F);
    ((LinearLayout)localObject2).addView(this.passwordEditText, LayoutHelper.createLinear(-1, 36, 51, 40, 32, 40, 0));
    this.passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
    {
      public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
      {
        if ((paramAnonymousInt == 5) || (paramAnonymousInt == 6))
        {
          TwoStepVerificationActivity.this.processDone();
          return true;
        }
        return false;
      }
    });
    this.passwordEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback()
    {
      public boolean onActionItemClicked(ActionMode paramAnonymousActionMode, MenuItem paramAnonymousMenuItem)
      {
        return false;
      }
      
      public boolean onCreateActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
      {
        return false;
      }
      
      public void onDestroyActionMode(ActionMode paramAnonymousActionMode) {}
      
      public boolean onPrepareActionMode(ActionMode paramAnonymousActionMode, Menu paramAnonymousMenu)
      {
        return false;
      }
    });
    this.bottomTextView = new TextView(paramContext);
    this.bottomTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
    this.bottomTextView.setTextSize(1, 14.0F);
    Object localObject3 = this.bottomTextView;
    int i;
    if (LocaleController.isRTL)
    {
      i = 5;
      ((TextView)localObject3).setGravity(i | 0x30);
      this.bottomTextView.setText(LocaleController.getString("YourEmailInfo", 2131494664));
      localObject3 = this.bottomTextView;
      if (!LocaleController.isRTL) {
        break label896;
      }
      i = 5;
      label523:
      ((LinearLayout)localObject2).addView((View)localObject3, LayoutHelper.createLinear(-2, -2, i | 0x30, 40, 30, 40, 0));
      localObject3 = new LinearLayout(paramContext);
      ((LinearLayout)localObject3).setGravity(80);
      ((LinearLayout)localObject2).addView((View)localObject3, LayoutHelper.createLinear(-1, -1));
      this.bottomButton = new TextView(paramContext);
      this.bottomButton.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
      this.bottomButton.setTextSize(1, 14.0F);
      localObject2 = this.bottomButton;
      if (!LocaleController.isRTL) {
        break label901;
      }
      i = 5;
      label627:
      ((TextView)localObject2).setGravity(i | 0x50);
      this.bottomButton.setText(LocaleController.getString("YourEmailSkip", 2131494665));
      this.bottomButton.setPadding(0, AndroidUtilities.dp(10.0F), 0, 0);
      localObject2 = this.bottomButton;
      if (!LocaleController.isRTL) {
        break label906;
      }
      i = 5;
      label682:
      ((LinearLayout)localObject3).addView((View)localObject2, LayoutHelper.createLinear(-1, -2, i | 0x50, 40, 0, 40, 14));
      this.bottomButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (TwoStepVerificationActivity.this.type == 0)
          {
            if (TwoStepVerificationActivity.this.currentPassword.has_recovery)
            {
              TwoStepVerificationActivity.this.needShowProgress();
              paramAnonymousView = new TLRPC.TL_auth_requestPasswordRecovery();
              ConnectionsManager.getInstance(TwoStepVerificationActivity.this.currentAccount).sendRequest(paramAnonymousView, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      TwoStepVerificationActivity.this.needHideProgress();
                      final Object localObject;
                      if (paramAnonymous2TL_error == null)
                      {
                        localObject = (TLRPC.TL_auth_passwordRecovery)paramAnonymous2TLObject;
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
                        localBuilder.setMessage(LocaleController.formatString("RestoreEmailSent", 2131494267, new Object[] { ((TLRPC.TL_auth_passwordRecovery)localObject).email_pattern }));
                        localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                        localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                        {
                          public void onClick(DialogInterface paramAnonymous4DialogInterface, int paramAnonymous4Int)
                          {
                            paramAnonymous4DialogInterface = new TwoStepVerificationActivity(1);
                            TwoStepVerificationActivity.access$202(paramAnonymous4DialogInterface, TwoStepVerificationActivity.this.currentPassword);
                            paramAnonymous4DialogInterface.currentPassword.email_unconfirmed_pattern = localObject.email_pattern;
                            TwoStepVerificationActivity.access$502(paramAnonymous4DialogInterface, 4);
                            TwoStepVerificationActivity.this.presentFragment(paramAnonymous4DialogInterface);
                          }
                        });
                        localObject = TwoStepVerificationActivity.this.showDialog(localBuilder.create());
                        if (localObject != null)
                        {
                          ((Dialog)localObject).setCanceledOnTouchOutside(false);
                          ((Dialog)localObject).setCancelable(false);
                        }
                        return;
                      }
                      if (paramAnonymous2TL_error.text.startsWith("FLOOD_WAIT"))
                      {
                        int i = Utilities.parseInt(paramAnonymous2TL_error.text).intValue();
                        if (i < 60) {}
                        for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
                        {
                          TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                          return;
                        }
                      }
                      TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), paramAnonymous2TL_error.text);
                    }
                  });
                }
              }, 10);
              return;
            }
            TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("RestorePasswordNoEmailTitle", 2131494272), LocaleController.getString("RestorePasswordNoEmailText", 2131494271));
            return;
          }
          if (TwoStepVerificationActivity.this.passwordSetState == 4)
          {
            TwoStepVerificationActivity.this.showAlertWithText(LocaleController.getString("RestorePasswordNoEmailTitle", 2131494272), LocaleController.getString("RestoreEmailTroubleText", 2131494270));
            return;
          }
          paramAnonymousView = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
          paramAnonymousView.setMessage(LocaleController.getString("YourEmailSkipWarningText", 2131494667));
          paramAnonymousView.setTitle(LocaleController.getString("YourEmailSkipWarning", 2131494666));
          paramAnonymousView.setPositiveButton(LocaleController.getString("YourEmailSkip", 2131494665), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              TwoStepVerificationActivity.access$802(TwoStepVerificationActivity.this, "");
              TwoStepVerificationActivity.this.setNewPassword(false);
            }
          });
          paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          TwoStepVerificationActivity.this.showDialog(paramAnonymousView.create());
        }
      });
      if (this.type != 0) {
        break label911;
      }
      this.emptyView = new EmptyTextProgressView(paramContext);
      this.emptyView.showProgress();
      this.listView = new RecyclerListView(paramContext);
      this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
      this.listView.setEmptyView(this.emptyView);
      this.listView.setVerticalScrollBarEnabled(false);
      ((FrameLayout)localObject1).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      localObject1 = this.listView;
      paramContext = new ListAdapter(paramContext);
      this.listAdapter = paramContext;
      ((RecyclerListView)localObject1).setAdapter(paramContext);
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt == TwoStepVerificationActivity.this.setPasswordRow) || (paramAnonymousInt == TwoStepVerificationActivity.this.changePasswordRow))
          {
            paramAnonymousView = new TwoStepVerificationActivity(1);
            TwoStepVerificationActivity.access$1202(paramAnonymousView, TwoStepVerificationActivity.this.currentPasswordHash);
            TwoStepVerificationActivity.access$202(paramAnonymousView, TwoStepVerificationActivity.this.currentPassword);
            TwoStepVerificationActivity.this.presentFragment(paramAnonymousView);
          }
          do
          {
            return;
            if ((paramAnonymousInt == TwoStepVerificationActivity.this.setRecoveryEmailRow) || (paramAnonymousInt == TwoStepVerificationActivity.this.changeRecoveryEmailRow))
            {
              paramAnonymousView = new TwoStepVerificationActivity(1);
              TwoStepVerificationActivity.access$1202(paramAnonymousView, TwoStepVerificationActivity.this.currentPasswordHash);
              TwoStepVerificationActivity.access$202(paramAnonymousView, TwoStepVerificationActivity.this.currentPassword);
              TwoStepVerificationActivity.access$1502(paramAnonymousView, true);
              TwoStepVerificationActivity.access$502(paramAnonymousView, 3);
              TwoStepVerificationActivity.this.presentFragment(paramAnonymousView);
              return;
            }
          } while ((paramAnonymousInt != TwoStepVerificationActivity.this.turnPasswordOffRow) && (paramAnonymousInt != TwoStepVerificationActivity.this.abortPasswordRow));
          paramAnonymousView = new AlertDialog.Builder(TwoStepVerificationActivity.this.getParentActivity());
          paramAnonymousView.setMessage(LocaleController.getString("TurnPasswordOffQuestion", 2131494500));
          paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
          paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              TwoStepVerificationActivity.this.setNewPassword(true);
            }
          });
          paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          TwoStepVerificationActivity.this.showDialog(paramAnonymousView.create());
        }
      });
      updateRows();
      this.actionBar.setTitle(LocaleController.getString("TwoStepVerification", 2131494501));
      this.titleTextView.setText(LocaleController.getString("PleaseEnterCurrentPassword", 2131494174));
    }
    for (;;)
    {
      return this.fragmentView;
      i = 3;
      break;
      label896:
      i = 3;
      break label523;
      label901:
      i = 3;
      break label627;
      label906:
      i = 3;
      break label682;
      label911:
      if (this.type == 1) {
        setPasswordSetState(this.passwordSetState);
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.didSetTwoStepPassword)
    {
      if ((paramVarArgs != null) && (paramVarArgs.length > 0) && (paramVarArgs[0] != null)) {
        this.currentPasswordHash = ((byte[])paramVarArgs[0]);
      }
      loadPasswordInfo(false);
      updateRows();
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextSettingsCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteRedText3"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.titleTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(this.bottomTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(this.bottomButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(this.passwordEditText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.passwordEditText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.passwordEditText, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.passwordEditText, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    updateRows();
    if (this.type == 0) {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didSetTwoStepPassword);
    }
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    if (this.type == 0)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didSetTwoStepPassword);
      if (this.shortPollRunnable != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.shortPollRunnable);
        this.shortPollRunnable = null;
      }
      this.destroyed = true;
    }
    if (this.progressDialog != null) {}
    try
    {
      this.progressDialog.dismiss();
      this.progressDialog = null;
      AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
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
  
  public void onResume()
  {
    super.onResume();
    if (this.type == 1) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (TwoStepVerificationActivity.this.passwordEditText != null)
          {
            TwoStepVerificationActivity.this.passwordEditText.requestFocus();
            AndroidUtilities.showKeyboard(TwoStepVerificationActivity.this.passwordEditText);
          }
        }
      }, 200L);
    }
    AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (this.type == 1)) {
      AndroidUtilities.showKeyboard(this.passwordEditText);
    }
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      if ((TwoStepVerificationActivity.this.loading) || (TwoStepVerificationActivity.this.currentPassword == null)) {
        return 0;
      }
      return TwoStepVerificationActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == TwoStepVerificationActivity.this.setPasswordDetailRow) || (paramInt == TwoStepVerificationActivity.this.shadowRow) || (paramInt == TwoStepVerificationActivity.this.passwordSetupDetailRow) || (paramInt == TwoStepVerificationActivity.this.passwordEnabledDetailRow) || (paramInt == TwoStepVerificationActivity.this.passwordEmailVerifyDetailRow)) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      return (i != TwoStepVerificationActivity.this.setPasswordDetailRow) && (i != TwoStepVerificationActivity.this.shadowRow) && (i != TwoStepVerificationActivity.this.passwordSetupDetailRow) && (i != TwoStepVerificationActivity.this.passwordEmailVerifyDetailRow) && (i != TwoStepVerificationActivity.this.passwordEnabledDetailRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool = true;
      switch (paramViewHolder.getItemViewType())
      {
      }
      do
      {
        do
        {
          return;
          paramViewHolder = (TextSettingsCell)paramViewHolder.itemView;
          paramViewHolder.setTag("windowBackgroundWhiteBlackText");
          paramViewHolder.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
          if (paramInt == TwoStepVerificationActivity.this.changePasswordRow)
          {
            paramViewHolder.setText(LocaleController.getString("ChangePassword", 2131493138), true);
            return;
          }
          if (paramInt == TwoStepVerificationActivity.this.setPasswordRow)
          {
            paramViewHolder.setText(LocaleController.getString("SetAdditionalPassword", 2131494369), true);
            return;
          }
          if (paramInt == TwoStepVerificationActivity.this.turnPasswordOffRow)
          {
            paramViewHolder.setText(LocaleController.getString("TurnPasswordOff", 2131494499), true);
            return;
          }
          if (paramInt == TwoStepVerificationActivity.this.changeRecoveryEmailRow)
          {
            String str = LocaleController.getString("ChangeRecoveryEmail", 2131493143);
            if (TwoStepVerificationActivity.this.abortPasswordRow != -1) {}
            for (;;)
            {
              paramViewHolder.setText(str, bool);
              return;
              bool = false;
            }
          }
          if (paramInt == TwoStepVerificationActivity.this.setRecoveryEmailRow)
          {
            paramViewHolder.setText(LocaleController.getString("SetRecoveryEmail", 2131494377), false);
            return;
          }
        } while (paramInt != TwoStepVerificationActivity.this.abortPasswordRow);
        paramViewHolder.setTag("windowBackgroundWhiteRedText3");
        paramViewHolder.setTextColor(Theme.getColor("windowBackgroundWhiteRedText3"));
        paramViewHolder.setText(LocaleController.getString("AbortPassword", 2131492865), false);
        return;
        paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
        if (paramInt == TwoStepVerificationActivity.this.setPasswordDetailRow)
        {
          paramViewHolder.setText(LocaleController.getString("SetAdditionalPasswordInfo", 2131494370));
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
          return;
        }
        if (paramInt == TwoStepVerificationActivity.this.shadowRow)
        {
          paramViewHolder.setText("");
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
          return;
        }
        if (paramInt == TwoStepVerificationActivity.this.passwordSetupDetailRow)
        {
          paramViewHolder.setText(LocaleController.formatString("EmailPasswordConfirmText", 2131493418, new Object[] { TwoStepVerificationActivity.this.currentPassword.email_unconfirmed_pattern }));
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165333, "windowBackgroundGrayShadow"));
          return;
        }
        if (paramInt == TwoStepVerificationActivity.this.passwordEnabledDetailRow)
        {
          paramViewHolder.setText(LocaleController.getString("EnabledPasswordText", 2131493428));
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
          return;
        }
      } while (paramInt != TwoStepVerificationActivity.this.passwordEmailVerifyDetailRow);
      paramViewHolder.setText(LocaleController.formatString("PendingEmailText", 2131494139, new Object[] { TwoStepVerificationActivity.this.currentPassword.email_unconfirmed_pattern }));
      paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/TwoStepVerificationActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */