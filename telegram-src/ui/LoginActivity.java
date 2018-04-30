package org.telegram.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_deleteAccount;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_password;
import org.telegram.tgnet.TLRPC.TL_auth_authorization;
import org.telegram.tgnet.TLRPC.TL_auth_cancelCode;
import org.telegram.tgnet.TLRPC.TL_auth_checkPassword;
import org.telegram.tgnet.TLRPC.TL_auth_codeTypeCall;
import org.telegram.tgnet.TLRPC.TL_auth_codeTypeFlashCall;
import org.telegram.tgnet.TLRPC.TL_auth_codeTypeSms;
import org.telegram.tgnet.TLRPC.TL_auth_passwordRecovery;
import org.telegram.tgnet.TLRPC.TL_auth_recoverPassword;
import org.telegram.tgnet.TLRPC.TL_auth_requestPasswordRecovery;
import org.telegram.tgnet.TLRPC.TL_auth_resendCode;
import org.telegram.tgnet.TLRPC.TL_auth_sendCode;
import org.telegram.tgnet.TLRPC.TL_auth_sentCode;
import org.telegram.tgnet.TLRPC.TL_auth_sentCodeTypeApp;
import org.telegram.tgnet.TLRPC.TL_auth_sentCodeTypeCall;
import org.telegram.tgnet.TLRPC.TL_auth_sentCodeTypeFlashCall;
import org.telegram.tgnet.TLRPC.TL_auth_sentCodeTypeSms;
import org.telegram.tgnet.TLRPC.TL_auth_signIn;
import org.telegram.tgnet.TLRPC.TL_auth_signUp;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.auth_SentCodeType;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SlideView;

@SuppressLint({"HardwareIds"})
public class LoginActivity
  extends BaseFragment
{
  private static final int done_button = 1;
  private boolean checkPermissions = true;
  private boolean checkShowPermissions = true;
  private int currentViewNum;
  private View doneButton;
  private boolean newAccount;
  private Dialog permissionsDialog;
  private ArrayList<String> permissionsItems = new ArrayList();
  private Dialog permissionsShowDialog;
  private ArrayList<String> permissionsShowItems = new ArrayList();
  private AlertDialog progressDialog;
  private boolean syncContacts = true;
  private SlideView[] views = new SlideView[9];
  
  public LoginActivity() {}
  
  public LoginActivity(int paramInt)
  {
    this.currentAccount = paramInt;
    this.newAccount = true;
  }
  
  private void clearCurrentState()
  {
    SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", 0).edit();
    localEditor.clear();
    localEditor.commit();
  }
  
  private void fillNextCodeParams(Bundle paramBundle, TLRPC.TL_auth_sentCode paramTL_auth_sentCode)
  {
    paramBundle.putString("phoneHash", paramTL_auth_sentCode.phone_code_hash);
    if ((paramTL_auth_sentCode.next_type instanceof TLRPC.TL_auth_codeTypeCall))
    {
      paramBundle.putInt("nextType", 4);
      if (!(paramTL_auth_sentCode.type instanceof TLRPC.TL_auth_sentCodeTypeApp)) {
        break label112;
      }
      paramBundle.putInt("type", 1);
      paramBundle.putInt("length", paramTL_auth_sentCode.type.length);
      setPage(1, true, paramBundle, false);
    }
    label112:
    do
    {
      return;
      if ((paramTL_auth_sentCode.next_type instanceof TLRPC.TL_auth_codeTypeFlashCall))
      {
        paramBundle.putInt("nextType", 3);
        break;
      }
      if (!(paramTL_auth_sentCode.next_type instanceof TLRPC.TL_auth_codeTypeSms)) {
        break;
      }
      paramBundle.putInt("nextType", 2);
      break;
      if (paramTL_auth_sentCode.timeout == 0) {
        paramTL_auth_sentCode.timeout = 60;
      }
      paramBundle.putInt("timeout", paramTL_auth_sentCode.timeout * 1000);
      if ((paramTL_auth_sentCode.type instanceof TLRPC.TL_auth_sentCodeTypeCall))
      {
        paramBundle.putInt("type", 4);
        paramBundle.putInt("length", paramTL_auth_sentCode.type.length);
        setPage(4, true, paramBundle, false);
        return;
      }
      if ((paramTL_auth_sentCode.type instanceof TLRPC.TL_auth_sentCodeTypeFlashCall))
      {
        paramBundle.putInt("type", 3);
        paramBundle.putString("pattern", paramTL_auth_sentCode.type.pattern);
        setPage(3, true, paramBundle, false);
        return;
      }
    } while (!(paramTL_auth_sentCode.type instanceof TLRPC.TL_auth_sentCodeTypeSms));
    paramBundle.putInt("type", 2);
    paramBundle.putInt("length", paramTL_auth_sentCode.type.length);
    setPage(2, true, paramBundle, false);
  }
  
  private Bundle loadCurrentState()
  {
    for (;;)
    {
      Bundle localBundle;
      Object localObject3;
      Object localObject4;
      String[] arrayOfString;
      Object localObject2;
      try
      {
        localBundle = new Bundle();
        Iterator localIterator = ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", 0).getAll().entrySet().iterator();
        Object localObject1 = localBundle;
        if (localIterator.hasNext())
        {
          localObject3 = (Map.Entry)localIterator.next();
          localObject1 = (String)((Map.Entry)localObject3).getKey();
          localObject4 = ((Map.Entry)localObject3).getValue();
          arrayOfString = ((String)localObject1).split("_\\|_");
          if (arrayOfString.length != 1) {
            break label146;
          }
          if (!(localObject4 instanceof String)) {
            break label122;
          }
          localBundle.putString((String)localObject1, (String)localObject4);
        }
        if (!(localObject4 instanceof Integer)) {
          continue;
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        localObject2 = null;
        return (Bundle)localObject2;
      }
      label122:
      localBundle.putInt((String)localObject2, ((Integer)localObject4).intValue());
      continue;
      label146:
      if (arrayOfString.length == 2)
      {
        localObject3 = localBundle.getBundle(arrayOfString[0]);
        localObject2 = localObject3;
        if (localObject3 == null)
        {
          localObject2 = new Bundle();
          localBundle.putBundle(arrayOfString[0], (Bundle)localObject2);
        }
        if ((localObject4 instanceof String)) {
          ((Bundle)localObject2).putString(arrayOfString[1], (String)localObject4);
        } else if ((localObject4 instanceof Integer)) {
          ((Bundle)localObject2).putInt(arrayOfString[1], ((Integer)localObject4).intValue());
        }
      }
    }
  }
  
  private void needFinishActivity()
  {
    clearCurrentState();
    if (this.newAccount)
    {
      if (getParentActivity() != null)
      {
        this.newAccount = false;
        ((LaunchActivity)getParentActivity()).switchToAccount(this.currentAccount, false);
        finishFragment();
      }
      return;
    }
    presentFragment(new DialogsActivity(null), true);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
  }
  
  private void needShowAlert(String paramString1, String paramString2)
  {
    if ((paramString2 == null) || (getParentActivity() == null)) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(paramString1);
    localBuilder.setMessage(paramString2);
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    showDialog(localBuilder.create());
  }
  
  private void needShowInvalidAlert(final String paramString, final boolean paramBoolean)
  {
    if (getParentActivity() == null) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    if (paramBoolean) {
      localBuilder.setMessage(LocaleController.getString("BannedPhoneNumber", 2131493079));
    }
    for (;;)
    {
      localBuilder.setNeutralButton(LocaleController.getString("BotHelp", 2131493088), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          try
          {
            paramAnonymousDialogInterface = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            paramAnonymousDialogInterface = String.format(Locale.US, "%s (%d)", new Object[] { paramAnonymousDialogInterface.versionName, Integer.valueOf(paramAnonymousDialogInterface.versionCode) });
            Intent localIntent = new Intent("android.intent.action.SEND");
            localIntent.setType("message/rfc822");
            localIntent.putExtra("android.intent.extra.EMAIL", new String[] { "login@stel.com" });
            if (paramBoolean)
            {
              localIntent.putExtra("android.intent.extra.SUBJECT", "Banned phone number: " + paramString);
              localIntent.putExtra("android.intent.extra.TEXT", "I'm trying to use my mobile phone number: " + paramString + "\nBut Telegram says it's banned. Please help.\n\nApp version: " + paramAnonymousDialogInterface + "\nOS version: SDK " + Build.VERSION.SDK_INT + "\nDevice Name: " + Build.MANUFACTURER + Build.MODEL + "\nLocale: " + Locale.getDefault());
            }
            for (;;)
            {
              LoginActivity.this.getParentActivity().startActivity(Intent.createChooser(localIntent, "Send email..."));
              return;
              localIntent.putExtra("android.intent.extra.SUBJECT", "Invalid phone number: " + paramString);
              localIntent.putExtra("android.intent.extra.TEXT", "I'm trying to use my mobile phone number: " + paramString + "\nBut Telegram says it's invalid. Please help.\n\nApp version: " + paramAnonymousDialogInterface + "\nOS version: SDK " + Build.VERSION.SDK_INT + "\nDevice Name: " + Build.MANUFACTURER + Build.MODEL + "\nLocale: " + Locale.getDefault());
            }
            return;
          }
          catch (Exception paramAnonymousDialogInterface)
          {
            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("NoMailInstalled", 2131493890));
          }
        }
      });
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      showDialog(localBuilder.create());
      return;
      localBuilder.setMessage(LocaleController.getString("InvalidPhoneNumber", 2131493682));
    }
  }
  
  private void needShowProgress(final int paramInt)
  {
    if ((getParentActivity() == null) || (getParentActivity().isFinishing()) || (this.progressDialog != null)) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity(), 1);
    localBuilder.setMessage(LocaleController.getString("Loading", 2131493762));
    if (paramInt != 0) {
      localBuilder.setPositiveButton(LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          LoginActivity.this.views[LoginActivity.this.currentViewNum].onCancelPressed();
          ConnectionsManager.getInstance(LoginActivity.this.currentAccount).cancelRequest(paramInt, true);
          LoginActivity.access$702(LoginActivity.this, null);
        }
      });
    }
    this.progressDialog = localBuilder.show();
    this.progressDialog.setCanceledOnTouchOutside(false);
    this.progressDialog.setCancelable(false);
  }
  
  private void putBundleToEditor(Bundle paramBundle, SharedPreferences.Editor paramEditor, String paramString)
  {
    Iterator localIterator = paramBundle.keySet().iterator();
    while (localIterator.hasNext())
    {
      String str = (String)localIterator.next();
      Object localObject = paramBundle.get(str);
      if ((localObject instanceof String))
      {
        if (paramString != null) {
          paramEditor.putString(paramString + "_|_" + str, (String)localObject);
        } else {
          paramEditor.putString(str, (String)localObject);
        }
      }
      else if ((localObject instanceof Integer))
      {
        if (paramString != null) {
          paramEditor.putInt(paramString + "_|_" + str, ((Integer)localObject).intValue());
        } else {
          paramEditor.putInt(str, ((Integer)localObject).intValue());
        }
      }
      else if ((localObject instanceof Bundle)) {
        putBundleToEditor((Bundle)localObject, paramEditor, str);
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setTitle(LocaleController.getString("AppName", 2131492981));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == 1) {
          LoginActivity.this.views[LoginActivity.this.currentViewNum].onNextPressed();
        }
        while (paramAnonymousInt != -1) {
          return;
        }
        LoginActivity.this.onBackPressed();
      }
    });
    Object localObject = this.actionBar.createMenu();
    this.actionBar.setAllowOverlayTitle(true);
    this.doneButton = ((ActionBarMenu)localObject).addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.fragmentView = new ScrollView(paramContext);
    ScrollView localScrollView = (ScrollView)this.fragmentView;
    localScrollView.setFillViewport(true);
    localObject = new FrameLayout(paramContext);
    localScrollView.addView((View)localObject, LayoutHelper.createScroll(-1, -2, 51));
    this.views[0] = new PhoneView(paramContext);
    this.views[1] = new LoginActivitySmsView(paramContext, 1);
    this.views[2] = new LoginActivitySmsView(paramContext, 2);
    this.views[3] = new LoginActivitySmsView(paramContext, 3);
    this.views[4] = new LoginActivitySmsView(paramContext, 4);
    this.views[5] = new LoginActivityRegisterView(paramContext);
    this.views[6] = new LoginActivityPasswordView(paramContext);
    this.views[7] = new LoginActivityRecoverView(paramContext);
    this.views[8] = new LoginActivityResetWaitView(paramContext);
    int i = 0;
    int j;
    if (i < this.views.length)
    {
      paramContext = this.views[i];
      label290:
      float f1;
      label313:
      float f2;
      if (i == 0)
      {
        j = 0;
        paramContext.setVisibility(j);
        paramContext = this.views[i];
        if (i != 0) {
          break label370;
        }
        f1 = -2.0F;
        if (!AndroidUtilities.isTablet()) {
          break label377;
        }
        f2 = 26.0F;
        label323:
        if (!AndroidUtilities.isTablet()) {
          break label384;
        }
      }
      label370:
      label377:
      label384:
      for (float f3 = 26.0F;; f3 = 18.0F)
      {
        ((FrameLayout)localObject).addView(paramContext, LayoutHelper.createFrame(-1, f1, 51, f2, 30.0F, f3, 0.0F));
        i += 1;
        break;
        j = 8;
        break label290;
        f1 = -1.0F;
        break label313;
        f2 = 18.0F;
        break label323;
      }
    }
    localObject = loadCurrentState();
    paramContext = (Context)localObject;
    boolean bool;
    if (localObject != null)
    {
      this.currentViewNum = ((Bundle)localObject).getInt("currentViewNum", 0);
      if (((Bundle)localObject).getInt("syncContacts", 1) != 1) {
        break label683;
      }
      bool = true;
      this.syncContacts = bool;
      paramContext = (Context)localObject;
      if (this.currentViewNum >= 1)
      {
        paramContext = (Context)localObject;
        if (this.currentViewNum <= 4)
        {
          i = ((Bundle)localObject).getInt("open");
          paramContext = (Context)localObject;
          if (i != 0)
          {
            paramContext = (Context)localObject;
            if (Math.abs(System.currentTimeMillis() / 1000L - i) >= 86400L)
            {
              this.currentViewNum = 0;
              paramContext = null;
              clearCurrentState();
            }
          }
        }
      }
    }
    this.actionBar.setTitle(this.views[this.currentViewNum].getHeaderName());
    i = 0;
    label538:
    if (i < this.views.length)
    {
      if (paramContext != null)
      {
        if ((i < 1) || (i > 4)) {
          break label689;
        }
        if (i == this.currentViewNum) {
          this.views[i].restoreStateParams(paramContext);
        }
      }
      label584:
      if (this.currentViewNum == i)
      {
        localObject = this.actionBar;
        if ((this.views[i].needBackButton()) || (this.newAccount))
        {
          j = 2131165346;
          label624:
          ((ActionBar)localObject).setBackButtonImage(j);
          this.views[i].setVisibility(0);
          this.views[i].onShow();
          if ((i == 3) || (i == 8)) {
            this.doneButton.setVisibility(8);
          }
        }
      }
      for (;;)
      {
        i += 1;
        break label538;
        label683:
        bool = false;
        break;
        label689:
        this.views[i].restoreStateParams(paramContext);
        break label584;
        j = 0;
        break label624;
        this.views[i].setVisibility(8);
      }
    }
    return this.fragmentView;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    PhoneView localPhoneView = (PhoneView)this.views[0];
    LoginActivitySmsView localLoginActivitySmsView1 = (LoginActivitySmsView)this.views[1];
    LoginActivitySmsView localLoginActivitySmsView2 = (LoginActivitySmsView)this.views[2];
    LoginActivitySmsView localLoginActivitySmsView3 = (LoginActivitySmsView)this.views[3];
    LoginActivitySmsView localLoginActivitySmsView4 = (LoginActivitySmsView)this.views[4];
    LoginActivityRegisterView localLoginActivityRegisterView = (LoginActivityRegisterView)this.views[5];
    LoginActivityPasswordView localLoginActivityPasswordView = (LoginActivityPasswordView)this.views[6];
    LoginActivityRecoverView localLoginActivityRecoverView = (LoginActivityRecoverView)this.views[7];
    LoginActivityResetWaitView localLoginActivityResetWaitView = (LoginActivityResetWaitView)this.views[8];
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(localPhoneView.countryButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localPhoneView.view, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhiteGrayLine"), new ThemeDescription(localPhoneView.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localPhoneView.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localPhoneView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localPhoneView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localPhoneView.phoneField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localPhoneView.phoneField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localPhoneView.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localPhoneView.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localPhoneView.textView2, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityPasswordView.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityPasswordView.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivityPasswordView.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivityPasswordView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivityPasswordView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivityPasswordView.cancelButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivityPasswordView.resetAccountButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteRedText6"), new ThemeDescription(localLoginActivityPasswordView.resetAccountText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityRegisterView.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityRegisterView.firstNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivityRegisterView.firstNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivityRegisterView.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivityRegisterView.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivityRegisterView.lastNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivityRegisterView.lastNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivityRegisterView.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivityRegisterView.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivityRegisterView.wrongNumber, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivityRecoverView.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityRecoverView.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivityRecoverView.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivityRecoverView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivityRecoverView.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivityRecoverView.cancelButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivityResetWaitView.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityResetWaitView.resetAccountText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityResetWaitView.resetAccountTime, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityResetWaitView.resetAccountButton, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivityResetWaitView.resetAccountButton, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, "windowBackgroundWhiteRedText6"), new ThemeDescription(localLoginActivitySmsView1.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView1.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivitySmsView1.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivitySmsView1.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivitySmsView1.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivitySmsView1.timeText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView1.problemText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView1.wrongNumber, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView1.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressInner"), new ThemeDescription(localLoginActivitySmsView1.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressOuter"), new ThemeDescription(localLoginActivitySmsView2.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView2.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivitySmsView2.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivitySmsView2.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivitySmsView2.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivitySmsView2.timeText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView2.problemText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView2.wrongNumber, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView2.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressInner"), new ThemeDescription(localLoginActivitySmsView2.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressOuter"), new ThemeDescription(localLoginActivitySmsView3.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView3.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivitySmsView3.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivitySmsView3.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivitySmsView3.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivitySmsView3.timeText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView3.problemText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView3.wrongNumber, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView3.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressInner"), new ThemeDescription(localLoginActivitySmsView3.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressOuter"), new ThemeDescription(localLoginActivitySmsView4.confirmTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView4.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(localLoginActivitySmsView4.codeField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(localLoginActivitySmsView4.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(localLoginActivitySmsView4.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(localLoginActivitySmsView4.timeText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText6"), new ThemeDescription(localLoginActivitySmsView4.problemText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView4.wrongNumber, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText4"), new ThemeDescription(localLoginActivitySmsView4.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressInner"), new ThemeDescription(localLoginActivitySmsView4.progressView, 0, new Class[] { ProgressView.class }, new String[] { "paint" }, null, null, null, "login_progressOuter") };
  }
  
  public void needHideProgress()
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
  
  public boolean onBackPressed()
  {
    if (this.currentViewNum == 0)
    {
      int i = 0;
      while (i < this.views.length)
      {
        if (this.views[i] != null) {
          this.views[i].onDestroyActivity();
        }
        i += 1;
      }
      clearCurrentState();
      if (this.newAccount) {
        finishFragment();
      }
      return true;
    }
    if (this.currentViewNum == 6)
    {
      this.views[this.currentViewNum].onBackPressed();
      setPage(0, true, null, true);
    }
    for (;;)
    {
      return false;
      if ((this.currentViewNum == 7) || (this.currentViewNum == 8))
      {
        this.views[this.currentViewNum].onBackPressed();
        setPage(6, true, null, true);
      }
      else if (this.newAccount)
      {
        if ((this.currentAccount >= 1) && (this.currentAccount <= 4)) {
          ((LoginActivitySmsView)this.views[this.currentAccount]).wrongNumber.callOnClick();
        } else if (this.currentAccount == 5) {
          ((LoginActivityRegisterView)this.views[this.currentAccount]).wrongNumber.callOnClick();
        }
      }
    }
  }
  
  protected void onDialogDismiss(Dialog paramDialog)
  {
    if ((Build.VERSION.SDK_INT < 23) || ((paramDialog == this.permissionsDialog) && (!this.permissionsItems.isEmpty()) && (getParentActivity() != null))) {}
    for (;;)
    {
      try
      {
        getParentActivity().requestPermissions((String[])this.permissionsItems.toArray(new String[this.permissionsItems.size()]), 6);
        return;
      }
      catch (Exception paramDialog) {}
      if ((paramDialog == this.permissionsShowDialog) && (!this.permissionsShowItems.isEmpty()) && (getParentActivity() != null)) {
        try
        {
          getParentActivity().requestPermissions((String[])this.permissionsShowItems.toArray(new String[this.permissionsShowItems.size()]), 7);
          return;
        }
        catch (Exception paramDialog) {}
      }
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    int i = 0;
    while (i < this.views.length)
    {
      if (this.views[i] != null) {
        this.views[i].onDestroyActivity();
      }
      i += 1;
    }
    if (this.progressDialog != null) {}
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
  
  public void onPause()
  {
    super.onPause();
    AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
    if (this.newAccount) {
      ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
    }
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 6)
    {
      this.checkPermissions = false;
      if (this.currentViewNum == 0) {
        this.views[this.currentViewNum].onNextPressed();
      }
    }
    do
    {
      do
      {
        return;
      } while (paramInt != 7);
      this.checkShowPermissions = false;
    } while (this.currentViewNum != 0);
    ((PhoneView)this.views[this.currentViewNum]).fillNumber();
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.newAccount) {
      ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
    }
    AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
    try
    {
      if ((this.currentViewNum >= 1) && (this.currentViewNum <= 4) && ((this.views[this.currentViewNum] instanceof LoginActivitySmsView)))
      {
        int i = ((LoginActivitySmsView)this.views[this.currentViewNum]).openTime;
        if ((i != 0) && (Math.abs(System.currentTimeMillis() / 1000L - i) >= 86400L))
        {
          this.views[this.currentViewNum].onBackPressed();
          setPage(0, false, null, true);
        }
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public void saveSelfArgs(Bundle paramBundle)
  {
    int i = 0;
    for (;;)
    {
      try
      {
        paramBundle = new Bundle();
        paramBundle.putInt("currentViewNum", this.currentViewNum);
        if (this.syncContacts) {
          i = 1;
        }
        paramBundle.putInt("syncContacts", i);
        i = 0;
        Object localObject;
        if (i <= this.currentViewNum)
        {
          localObject = this.views[i];
          if (localObject != null) {
            ((SlideView)localObject).saveStateParams(paramBundle);
          }
        }
        else
        {
          localObject = ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", 0).edit();
          ((SharedPreferences.Editor)localObject).clear();
          putBundleToEditor(paramBundle, (SharedPreferences.Editor)localObject, null);
          ((SharedPreferences.Editor)localObject).commit();
          return;
        }
      }
      catch (Exception paramBundle)
      {
        FileLog.e(paramBundle);
        return;
      }
      i += 1;
    }
  }
  
  public void setPage(int paramInt, boolean paramBoolean1, Bundle paramBundle, boolean paramBoolean2)
  {
    int i = 2131165346;
    if ((paramInt == 3) || (paramInt == 8))
    {
      this.doneButton.setVisibility(8);
      if (!paramBoolean1) {
        break label287;
      }
      final SlideView localSlideView = this.views[this.currentViewNum];
      localObject = this.views[paramInt];
      this.currentViewNum = paramInt;
      ActionBar localActionBar = this.actionBar;
      paramInt = i;
      if (!((SlideView)localObject).needBackButton())
      {
        if (!this.newAccount) {
          break label257;
        }
        paramInt = i;
      }
      label80:
      localActionBar.setBackButtonImage(paramInt);
      ((SlideView)localObject).setParams(paramBundle, false);
      this.actionBar.setTitle(((SlideView)localObject).getHeaderName());
      ((SlideView)localObject).onShow();
      if (!paramBoolean2) {
        break label262;
      }
      f = -AndroidUtilities.displaySize.x;
      label125:
      ((SlideView)localObject).setX(f);
      paramBundle = localSlideView.animate().setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator) {}
        
        @SuppressLint({"NewApi"})
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          localSlideView.setVisibility(8);
          localSlideView.setX(0.0F);
        }
        
        public void onAnimationRepeat(Animator paramAnonymousAnimator) {}
        
        public void onAnimationStart(Animator paramAnonymousAnimator) {}
      }).setDuration(300L);
      if (!paramBoolean2) {
        break label274;
      }
    }
    label257:
    label262:
    label274:
    for (float f = AndroidUtilities.displaySize.x;; f = -AndroidUtilities.displaySize.x)
    {
      paramBundle.translationX(f).start();
      ((SlideView)localObject).animate().setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator) {}
        
        public void onAnimationEnd(Animator paramAnonymousAnimator) {}
        
        public void onAnimationRepeat(Animator paramAnonymousAnimator) {}
        
        public void onAnimationStart(Animator paramAnonymousAnimator)
        {
          localObject.setVisibility(0);
        }
      }).setDuration(300L).translationX(0.0F).start();
      return;
      if (paramInt == 0)
      {
        this.checkPermissions = true;
        this.checkShowPermissions = true;
      }
      this.doneButton.setVisibility(0);
      break;
      paramInt = 0;
      break label80;
      f = AndroidUtilities.displaySize.x;
      break label125;
    }
    label287:
    final Object localObject = this.actionBar;
    int j = i;
    if (!this.views[paramInt].needBackButton()) {
      if (!this.newAccount) {
        break label393;
      }
    }
    label393:
    for (j = i;; j = 0)
    {
      ((ActionBar)localObject).setBackButtonImage(j);
      this.views[this.currentViewNum].setVisibility(8);
      this.currentViewNum = paramInt;
      this.views[paramInt].setParams(paramBundle, false);
      this.views[paramInt].setVisibility(0);
      this.actionBar.setTitle(this.views[paramInt].getHeaderName());
      this.views[paramInt].onShow();
      return;
    }
  }
  
  public class LoginActivityPasswordView
    extends SlideView
  {
    private TextView cancelButton;
    private EditTextBoldCursor codeField;
    private TextView confirmTextView;
    private Bundle currentParams;
    private byte[] current_salt;
    private String email_unconfirmed_pattern;
    private boolean has_recovery;
    private String hint;
    private boolean nextPressed;
    private String phoneCode;
    private String phoneHash;
    private String requestPhone;
    private TextView resetAccountButton;
    private TextView resetAccountText;
    
    public LoginActivityPasswordView(Context paramContext)
    {
      super();
      setOrientation(1);
      this.confirmTextView = new TextView(paramContext);
      this.confirmTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
      this.confirmTextView.setTextSize(1, 14.0F);
      Object localObject = this.confirmTextView;
      if (LocaleController.isRTL)
      {
        i = 5;
        ((TextView)localObject).setGravity(i);
        this.confirmTextView.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.confirmTextView.setText(LocaleController.getString("LoginPasswordText", 2131493778));
        localObject = this.confirmTextView;
        if (!LocaleController.isRTL) {
          break label792;
        }
        i = 5;
        label110:
        addView((View)localObject, LayoutHelper.createLinear(-2, -2, i));
        this.codeField = new EditTextBoldCursor(paramContext);
        this.codeField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.codeField.setCursorWidth(1.5F);
        this.codeField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.codeField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.codeField.setHint(LocaleController.getString("LoginPassword", 2131493777));
        this.codeField.setImeOptions(268435461);
        this.codeField.setTextSize(1, 18.0F);
        this.codeField.setMaxLines(1);
        this.codeField.setPadding(0, 0, 0, 0);
        this.codeField.setInputType(129);
        this.codeField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        this.codeField.setTypeface(Typeface.DEFAULT);
        localObject = this.codeField;
        if (!LocaleController.isRTL) {
          break label797;
        }
        i = 5;
        label301:
        ((EditTextBoldCursor)localObject).setGravity(i);
        addView(this.codeField, LayoutHelper.createLinear(-1, 36, 1, 0, 20, 0, 0));
        this.codeField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.LoginActivityPasswordView.this.onNextPressed();
              return true;
            }
            return false;
          }
        });
        this.cancelButton = new TextView(paramContext);
        localObject = this.cancelButton;
        if (!LocaleController.isRTL) {
          break label802;
        }
        i = 5;
        label369:
        ((TextView)localObject).setGravity(i | 0x30);
        this.cancelButton.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
        this.cancelButton.setText(LocaleController.getString("ForgotPassword", 2131493547));
        this.cancelButton.setTextSize(1, 14.0F);
        this.cancelButton.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.cancelButton.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
        localObject = this.cancelButton;
        if (!LocaleController.isRTL) {
          break label807;
        }
        i = 5;
        label456:
        addView((View)localObject, LayoutHelper.createLinear(-1, -2, i | 0x30));
        this.cancelButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (LoginActivity.LoginActivityPasswordView.this.has_recovery)
            {
              LoginActivity.this.needShowProgress(0);
              paramAnonymousView = new TLRPC.TL_auth_requestPasswordRecovery();
              ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(paramAnonymousView, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      LoginActivity.this.needHideProgress();
                      final Object localObject;
                      if (paramAnonymous2TL_error == null)
                      {
                        localObject = (TLRPC.TL_auth_passwordRecovery)paramAnonymous2TLObject;
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
                        localBuilder.setMessage(LocaleController.formatString("RestoreEmailSent", 2131494267, new Object[] { ((TLRPC.TL_auth_passwordRecovery)localObject).email_pattern }));
                        localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                        localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                        {
                          public void onClick(DialogInterface paramAnonymous4DialogInterface, int paramAnonymous4Int)
                          {
                            paramAnonymous4DialogInterface = new Bundle();
                            paramAnonymous4DialogInterface.putString("email_unconfirmed_pattern", localObject.email_pattern);
                            LoginActivity.this.setPage(7, true, paramAnonymous4DialogInterface, false);
                          }
                        });
                        localObject = LoginActivity.this.showDialog(localBuilder.create());
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
                          LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                          return;
                        }
                      }
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymous2TL_error.text);
                    }
                  });
                }
              }, 10);
              return;
            }
            LoginActivity.LoginActivityPasswordView.this.resetAccountText.setVisibility(0);
            LoginActivity.LoginActivityPasswordView.this.resetAccountButton.setVisibility(0);
            AndroidUtilities.hideKeyboard(LoginActivity.LoginActivityPasswordView.this.codeField);
            LoginActivity.this.needShowAlert(LocaleController.getString("RestorePasswordNoEmailTitle", 2131494272), LocaleController.getString("RestorePasswordNoEmailText", 2131494271));
          }
        });
        this.resetAccountButton = new TextView(paramContext);
        localObject = this.resetAccountButton;
        if (!LocaleController.isRTL) {
          break label812;
        }
        i = 5;
        label514:
        ((TextView)localObject).setGravity(i | 0x30);
        this.resetAccountButton.setTextColor(Theme.getColor("windowBackgroundWhiteRedText6"));
        this.resetAccountButton.setVisibility(8);
        this.resetAccountButton.setText(LocaleController.getString("ResetMyAccount", 2131494259));
        this.resetAccountButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.resetAccountButton.setTextSize(1, 14.0F);
        this.resetAccountButton.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.resetAccountButton.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
        localObject = this.resetAccountButton;
        if (!LocaleController.isRTL) {
          break label817;
        }
        i = 5;
        label622:
        addView((View)localObject, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 34, 0, 0));
        this.resetAccountButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
            paramAnonymousView.setMessage(LocaleController.getString("ResetMyAccountWarningText", 2131494263));
            paramAnonymousView.setTitle(LocaleController.getString("ResetMyAccountWarning", 2131494261));
            paramAnonymousView.setPositiveButton(LocaleController.getString("ResetMyAccountWarningReset", 2131494262), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                LoginActivity.this.needShowProgress(0);
                paramAnonymous2DialogInterface = new TLRPC.TL_account_deleteAccount();
                paramAnonymous2DialogInterface.reason = "Forgot password";
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(paramAnonymous2DialogInterface, new RequestDelegate()
                {
                  public void run(TLObject paramAnonymous3TLObject, final TLRPC.TL_error paramAnonymous3TL_error)
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        LoginActivity.this.needHideProgress();
                        Bundle localBundle;
                        if (paramAnonymous3TL_error == null)
                        {
                          localBundle = new Bundle();
                          localBundle.putString("phoneFormated", LoginActivity.LoginActivityPasswordView.this.requestPhone);
                          localBundle.putString("phoneHash", LoginActivity.LoginActivityPasswordView.this.phoneHash);
                          localBundle.putString("code", LoginActivity.LoginActivityPasswordView.this.phoneCode);
                          LoginActivity.this.setPage(5, true, localBundle, false);
                          return;
                        }
                        if (paramAnonymous3TL_error.text.equals("2FA_RECENT_CONFIRM"))
                        {
                          LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("ResetAccountCancelledAlert", 2131494253));
                          return;
                        }
                        if (paramAnonymous3TL_error.text.startsWith("2FA_CONFIRM_WAIT_"))
                        {
                          localBundle = new Bundle();
                          localBundle.putString("phoneFormated", LoginActivity.LoginActivityPasswordView.this.requestPhone);
                          localBundle.putString("phoneHash", LoginActivity.LoginActivityPasswordView.this.phoneHash);
                          localBundle.putString("code", LoginActivity.LoginActivityPasswordView.this.phoneCode);
                          localBundle.putInt("startTime", ConnectionsManager.getInstance(LoginActivity.this.currentAccount).getCurrentTime());
                          localBundle.putInt("waitTime", Utilities.parseInt(paramAnonymous3TL_error.text.replace("2FA_CONFIRM_WAIT_", "")).intValue());
                          LoginActivity.this.setPage(8, true, localBundle, false);
                          return;
                        }
                        LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymous3TL_error.text);
                      }
                    });
                  }
                }, 10);
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            LoginActivity.this.showDialog(paramAnonymousView.create());
          }
        });
        this.resetAccountText = new TextView(paramContext);
        this$1 = this.resetAccountText;
        if (!LocaleController.isRTL) {
          break label822;
        }
        i = 5;
        label685:
        LoginActivity.this.setGravity(i | 0x30);
        this.resetAccountText.setVisibility(8);
        this.resetAccountText.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        this.resetAccountText.setText(LocaleController.getString("ResetMyAccountText", 2131494260));
        this.resetAccountText.setTextSize(1, 14.0F);
        this.resetAccountText.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this$1 = this.resetAccountText;
        if (!LocaleController.isRTL) {
          break label827;
        }
      }
      label792:
      label797:
      label802:
      label807:
      label812:
      label817:
      label822:
      label827:
      for (int i = 5;; i = 3)
      {
        addView(LoginActivity.this, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 7, 0, 14));
        return;
        i = 3;
        break;
        i = 3;
        break label110;
        i = 3;
        break label301;
        i = 3;
        break label369;
        i = 3;
        break label456;
        i = 3;
        break label514;
        i = 3;
        break label622;
        i = 3;
        break label685;
      }
    }
    
    private void onPasscodeError(boolean paramBoolean)
    {
      if (LoginActivity.this.getParentActivity() == null) {
        return;
      }
      Vibrator localVibrator = (Vibrator)LoginActivity.this.getParentActivity().getSystemService("vibrator");
      if (localVibrator != null) {
        localVibrator.vibrate(200L);
      }
      if (paramBoolean) {
        this.codeField.setText("");
      }
      AndroidUtilities.shakeView(this.confirmTextView, 2.0F, 0);
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("LoginPassword", 2131493777);
    }
    
    public boolean needBackButton()
    {
      return true;
    }
    
    public void onBackPressed()
    {
      this.currentParams = null;
    }
    
    public void onCancelPressed()
    {
      this.nextPressed = false;
    }
    
    public void onNextPressed()
    {
      if (this.nextPressed) {
        return;
      }
      Object localObject2 = this.codeField.getText().toString();
      if (((String)localObject2).length() == 0)
      {
        onPasscodeError(false);
        return;
      }
      this.nextPressed = true;
      Object localObject1 = null;
      try
      {
        localObject2 = ((String)localObject2).getBytes("UTF-8");
        localObject1 = localObject2;
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
      LoginActivity.this.needShowProgress(0);
      localObject2 = new byte[this.current_salt.length * 2 + localObject1.length];
      System.arraycopy(this.current_salt, 0, localObject2, 0, this.current_salt.length);
      System.arraycopy(localObject1, 0, localObject2, this.current_salt.length, localObject1.length);
      System.arraycopy(this.current_salt, 0, localObject2, localObject2.length - this.current_salt.length, this.current_salt.length);
      localObject1 = new TLRPC.TL_auth_checkPassword();
      ((TLRPC.TL_auth_checkPassword)localObject1).password_hash = Utilities.computeSHA256((byte[])localObject2, 0, localObject2.length);
      ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LoginActivity.this.needHideProgress();
              LoginActivity.LoginActivityPasswordView.access$8502(LoginActivity.LoginActivityPasswordView.this, false);
              Object localObject;
              if (paramAnonymousTL_error == null)
              {
                localObject = (TLRPC.TL_auth_authorization)paramAnonymousTLObject;
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).setUserId(((TLRPC.TL_auth_authorization)localObject).user.id);
                UserConfig.getInstance(LoginActivity.this.currentAccount).clearConfig();
                MessagesController.getInstance(LoginActivity.this.currentAccount).cleanup();
                UserConfig.getInstance(LoginActivity.this.currentAccount).setCurrentUser(((TLRPC.TL_auth_authorization)localObject).user);
                UserConfig.getInstance(LoginActivity.this.currentAccount).syncContacts = LoginActivity.this.syncContacts;
                UserConfig.getInstance(LoginActivity.this.currentAccount).saveConfig(true);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).cleanup(true);
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(((TLRPC.TL_auth_authorization)localObject).user);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).putUsersAndChats(localArrayList, null, true, true);
                MessagesController.getInstance(LoginActivity.this.currentAccount).putUser(((TLRPC.TL_auth_authorization)localObject).user, false);
                ContactsController.getInstance(LoginActivity.this.currentAccount).checkAppAccount();
                MessagesController.getInstance(LoginActivity.this.currentAccount).getBlockedUsers(true);
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).updateDcSettings();
                LoginActivity.this.needFinishActivity();
                return;
              }
              if (paramAnonymousTL_error.text.equals("PASSWORD_HASH_INVALID"))
              {
                LoginActivity.LoginActivityPasswordView.this.onPasscodeError(true);
                return;
              }
              if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
              {
                int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
                if (i < 60) {}
                for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
                {
                  LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                  return;
                }
              }
              LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
            }
          });
        }
      }, 10);
    }
    
    public void onShow()
    {
      super.onShow();
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (LoginActivity.LoginActivityPasswordView.this.codeField != null)
          {
            LoginActivity.LoginActivityPasswordView.this.codeField.requestFocus();
            LoginActivity.LoginActivityPasswordView.this.codeField.setSelection(LoginActivity.LoginActivityPasswordView.this.codeField.length());
            AndroidUtilities.showKeyboard(LoginActivity.LoginActivityPasswordView.this.codeField);
          }
        }
      }, 100L);
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      this.currentParams = paramBundle.getBundle("passview_params");
      if (this.currentParams != null) {
        setParams(this.currentParams, true);
      }
      paramBundle = paramBundle.getString("passview_code");
      if (paramBundle != null) {
        this.codeField.setText(paramBundle);
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      String str = this.codeField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("passview_code", str);
      }
      if (this.currentParams != null) {
        paramBundle.putBundle("passview_params", this.currentParams);
      }
    }
    
    public void setParams(Bundle paramBundle, boolean paramBoolean)
    {
      paramBoolean = true;
      if (paramBundle == null) {
        return;
      }
      if (paramBundle.isEmpty())
      {
        this.resetAccountButton.setVisibility(0);
        this.resetAccountText.setVisibility(0);
        AndroidUtilities.hideKeyboard(this.codeField);
        return;
      }
      this.resetAccountButton.setVisibility(8);
      this.resetAccountText.setVisibility(8);
      this.codeField.setText("");
      this.currentParams = paramBundle;
      this.current_salt = Utilities.hexToBytes(this.currentParams.getString("current_salt"));
      this.hint = this.currentParams.getString("hint");
      if (this.currentParams.getInt("has_recovery") == 1) {}
      for (;;)
      {
        this.has_recovery = paramBoolean;
        this.email_unconfirmed_pattern = this.currentParams.getString("email_unconfirmed_pattern");
        this.requestPhone = paramBundle.getString("phoneFormated");
        this.phoneHash = paramBundle.getString("phoneHash");
        this.phoneCode = paramBundle.getString("code");
        if ((this.hint == null) || (this.hint.length() <= 0)) {
          break;
        }
        this.codeField.setHint(this.hint);
        return;
        paramBoolean = false;
      }
      this.codeField.setHint(LocaleController.getString("LoginPassword", 2131493777));
    }
  }
  
  public class LoginActivityRecoverView
    extends SlideView
  {
    private TextView cancelButton;
    private EditTextBoldCursor codeField;
    private TextView confirmTextView;
    private Bundle currentParams;
    private String email_unconfirmed_pattern;
    private boolean nextPressed;
    
    public LoginActivityRecoverView(Context paramContext)
    {
      super();
      setOrientation(1);
      this.confirmTextView = new TextView(paramContext);
      this.confirmTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
      this.confirmTextView.setTextSize(1, 14.0F);
      Object localObject = this.confirmTextView;
      if (LocaleController.isRTL)
      {
        i = 5;
        ((TextView)localObject).setGravity(i);
        this.confirmTextView.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.confirmTextView.setText(LocaleController.getString("RestoreEmailSentInfo", 2131494268));
        localObject = this.confirmTextView;
        if (!LocaleController.isRTL) {
          break label484;
        }
        i = 5;
        label113:
        addView((View)localObject, LayoutHelper.createLinear(-2, -2, i));
        this.codeField = new EditTextBoldCursor(paramContext);
        this.codeField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.codeField.setCursorWidth(1.5F);
        this.codeField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.codeField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.codeField.setHint(LocaleController.getString("PasswordCode", 2131494072));
        this.codeField.setImeOptions(268435461);
        this.codeField.setTextSize(1, 18.0F);
        this.codeField.setMaxLines(1);
        this.codeField.setPadding(0, 0, 0, 0);
        this.codeField.setInputType(3);
        this.codeField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        this.codeField.setTypeface(Typeface.DEFAULT);
        localObject = this.codeField;
        if (!LocaleController.isRTL) {
          break label489;
        }
        i = 5;
        label302:
        ((EditTextBoldCursor)localObject).setGravity(i);
        addView(this.codeField, LayoutHelper.createLinear(-1, 36, 1, 0, 20, 0, 0));
        this.codeField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.LoginActivityRecoverView.this.onNextPressed();
              return true;
            }
            return false;
          }
        });
        this.cancelButton = new TextView(paramContext);
        paramContext = this.cancelButton;
        if (!LocaleController.isRTL) {
          break label494;
        }
        i = 5;
        label369:
        paramContext.setGravity(i | 0x50);
        this.cancelButton.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
        this.cancelButton.setTextSize(1, 14.0F);
        this.cancelButton.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.cancelButton.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
        paramContext = this.cancelButton;
        if (!LocaleController.isRTL) {
          break label499;
        }
      }
      label484:
      label489:
      label494:
      label499:
      for (int i = j;; i = 3)
      {
        addView(paramContext, LayoutHelper.createLinear(-2, -2, i | 0x50, 0, 0, 0, 14));
        this.cancelButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
            paramAnonymousView.setMessage(LocaleController.getString("RestoreEmailTroubleText", 2131494270));
            paramAnonymousView.setTitle(LocaleController.getString("RestorePasswordNoEmailTitle", 2131494272));
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                LoginActivity.this.setPage(6, true, new Bundle(), true);
              }
            });
            paramAnonymousView = LoginActivity.this.showDialog(paramAnonymousView.create());
            if (paramAnonymousView != null)
            {
              paramAnonymousView.setCanceledOnTouchOutside(false);
              paramAnonymousView.setCancelable(false);
            }
          }
        });
        return;
        i = 3;
        break;
        i = 3;
        break label113;
        i = 3;
        break label302;
        i = 3;
        break label369;
      }
    }
    
    private void onPasscodeError(boolean paramBoolean)
    {
      if (LoginActivity.this.getParentActivity() == null) {
        return;
      }
      Vibrator localVibrator = (Vibrator)LoginActivity.this.getParentActivity().getSystemService("vibrator");
      if (localVibrator != null) {
        localVibrator.vibrate(200L);
      }
      if (paramBoolean) {
        this.codeField.setText("");
      }
      AndroidUtilities.shakeView(this.confirmTextView, 2.0F, 0);
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("LoginPassword", 2131493777);
    }
    
    public boolean needBackButton()
    {
      return true;
    }
    
    public void onBackPressed()
    {
      this.currentParams = null;
    }
    
    public void onCancelPressed()
    {
      this.nextPressed = false;
    }
    
    public void onNextPressed()
    {
      if (this.nextPressed) {
        return;
      }
      if (this.codeField.getText().toString().length() == 0)
      {
        onPasscodeError(false);
        return;
      }
      this.nextPressed = true;
      String str = this.codeField.getText().toString();
      if (str.length() == 0)
      {
        onPasscodeError(false);
        return;
      }
      LoginActivity.this.needShowProgress(0);
      TLRPC.TL_auth_recoverPassword localTL_auth_recoverPassword = new TLRPC.TL_auth_recoverPassword();
      localTL_auth_recoverPassword.code = str;
      ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(localTL_auth_recoverPassword, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LoginActivity.this.needHideProgress();
              LoginActivity.LoginActivityRecoverView.access$11002(LoginActivity.LoginActivityRecoverView.this, false);
              Object localObject;
              if (paramAnonymousTL_error == null)
              {
                localObject = (TLRPC.TL_auth_authorization)paramAnonymousTLObject;
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).setUserId(((TLRPC.TL_auth_authorization)localObject).user.id);
                UserConfig.getInstance(LoginActivity.this.currentAccount).clearConfig();
                MessagesController.getInstance(LoginActivity.this.currentAccount).cleanup();
                UserConfig.getInstance(LoginActivity.this.currentAccount).setCurrentUser(((TLRPC.TL_auth_authorization)localObject).user);
                UserConfig.getInstance(LoginActivity.this.currentAccount).syncContacts = LoginActivity.this.syncContacts;
                UserConfig.getInstance(LoginActivity.this.currentAccount).saveConfig(true);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).cleanup(true);
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(((TLRPC.TL_auth_authorization)localObject).user);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).putUsersAndChats(localArrayList, null, true, true);
                MessagesController.getInstance(LoginActivity.this.currentAccount).putUser(((TLRPC.TL_auth_authorization)localObject).user, false);
                ContactsController.getInstance(LoginActivity.this.currentAccount).checkAppAccount();
                MessagesController.getInstance(LoginActivity.this.currentAccount).getBlockedUsers(true);
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).updateDcSettings();
                LoginActivity.this.needFinishActivity();
                return;
              }
              if (paramAnonymousTL_error.text.startsWith("CODE_INVALID"))
              {
                LoginActivity.LoginActivityRecoverView.this.onPasscodeError(true);
                return;
              }
              if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
              {
                int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
                if (i < 60) {}
                for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
                {
                  LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                  return;
                }
              }
              LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
            }
          });
        }
      }, 10);
    }
    
    public void onShow()
    {
      super.onShow();
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (LoginActivity.LoginActivityRecoverView.this.codeField != null)
          {
            LoginActivity.LoginActivityRecoverView.this.codeField.requestFocus();
            LoginActivity.LoginActivityRecoverView.this.codeField.setSelection(LoginActivity.LoginActivityRecoverView.this.codeField.length());
          }
        }
      }, 100L);
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      this.currentParams = paramBundle.getBundle("recoveryview_params");
      if (this.currentParams != null) {
        setParams(this.currentParams, true);
      }
      paramBundle = paramBundle.getString("recoveryview_code");
      if (paramBundle != null) {
        this.codeField.setText(paramBundle);
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      String str = this.codeField.getText().toString();
      if ((str != null) && (str.length() != 0)) {
        paramBundle.putString("recoveryview_code", str);
      }
      if (this.currentParams != null) {
        paramBundle.putBundle("recoveryview_params", this.currentParams);
      }
    }
    
    public void setParams(Bundle paramBundle, boolean paramBoolean)
    {
      if (paramBundle == null) {
        return;
      }
      this.codeField.setText("");
      this.currentParams = paramBundle;
      this.email_unconfirmed_pattern = this.currentParams.getString("email_unconfirmed_pattern");
      this.cancelButton.setText(LocaleController.formatString("RestoreEmailTrouble", 2131494269, new Object[] { this.email_unconfirmed_pattern }));
      AndroidUtilities.showKeyboard(this.codeField);
      this.codeField.requestFocus();
    }
  }
  
  public class LoginActivityRegisterView
    extends SlideView
  {
    private Bundle currentParams;
    private EditTextBoldCursor firstNameField;
    private EditTextBoldCursor lastNameField;
    private boolean nextPressed = false;
    private String phoneCode;
    private String phoneHash;
    private String requestPhone;
    private TextView textView;
    private TextView wrongNumber;
    
    public LoginActivityRegisterView(Context paramContext)
    {
      super();
      setOrientation(1);
      this.textView = new TextView(paramContext);
      this.textView.setText(LocaleController.getString("RegisterText", 2131494228));
      this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
      Object localObject = this.textView;
      if (LocaleController.isRTL)
      {
        i = 5;
        ((TextView)localObject).setGravity(i);
        this.textView.setTextSize(1, 14.0F);
        localObject = this.textView;
        if (!LocaleController.isRTL) {
          break label636;
        }
        i = 5;
        label102:
        addView((View)localObject, LayoutHelper.createLinear(-2, -2, i, 0, 8, 0, 0));
        this.firstNameField = new EditTextBoldCursor(paramContext);
        this.firstNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.firstNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.firstNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.firstNameField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.firstNameField.setCursorWidth(1.5F);
        this.firstNameField.setHint(LocaleController.getString("FirstName", 2131493542));
        this.firstNameField.setImeOptions(268435461);
        this.firstNameField.setTextSize(1, 18.0F);
        this.firstNameField.setMaxLines(1);
        this.firstNameField.setInputType(8192);
        addView(this.firstNameField, LayoutHelper.createLinear(-1, 36, 0.0F, 26.0F, 0.0F, 0.0F));
        this.firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.LoginActivityRegisterView.this.lastNameField.requestFocus();
              return true;
            }
            return false;
          }
        });
        this.lastNameField = new EditTextBoldCursor(paramContext);
        this.lastNameField.setHint(LocaleController.getString("LastName", 2131493726));
        this.lastNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.lastNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.lastNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.lastNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.lastNameField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.lastNameField.setCursorWidth(1.5F);
        this.lastNameField.setImeOptions(268435462);
        this.lastNameField.setTextSize(1, 18.0F);
        this.lastNameField.setMaxLines(1);
        this.lastNameField.setInputType(8192);
        addView(this.lastNameField, LayoutHelper.createLinear(-1, 36, 0.0F, 10.0F, 0.0F, 0.0F));
        this.lastNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if ((paramAnonymousInt == 6) || (paramAnonymousInt == 5))
            {
              LoginActivity.LoginActivityRegisterView.this.onNextPressed();
              return true;
            }
            return false;
          }
        });
        localObject = new LinearLayout(paramContext);
        ((LinearLayout)localObject).setGravity(80);
        addView((View)localObject, LayoutHelper.createLinear(-1, -1));
        this.wrongNumber = new TextView(paramContext);
        this.wrongNumber.setText(LocaleController.getString("CancelRegistration", 2131493132));
        paramContext = this.wrongNumber;
        if (!LocaleController.isRTL) {
          break label641;
        }
        i = 5;
        label522:
        paramContext.setGravity(i | 0x1);
        this.wrongNumber.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
        this.wrongNumber.setTextSize(1, 14.0F);
        this.wrongNumber.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.wrongNumber.setPadding(0, AndroidUtilities.dp(24.0F), 0, 0);
        paramContext = this.wrongNumber;
        if (!LocaleController.isRTL) {
          break label646;
        }
      }
      label636:
      label641:
      label646:
      for (int i = 5;; i = 3)
      {
        ((LinearLayout)localObject).addView(paramContext, LayoutHelper.createLinear(-2, -2, i | 0x50, 0, 0, 0, 10));
        this.wrongNumber.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
            paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
            paramAnonymousView.setMessage(LocaleController.getString("AreYouSureRegistration", 2131493010));
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                LoginActivity.LoginActivityRegisterView.this.onBackPressed();
                LoginActivity.this.setPage(0, true, null, true);
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            LoginActivity.this.showDialog(paramAnonymousView.create());
          }
        });
        return;
        i = 3;
        break;
        i = 3;
        break label102;
        i = 3;
        break label522;
      }
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("YourName", 2131494668);
    }
    
    public void onBackPressed()
    {
      this.currentParams = null;
    }
    
    public void onCancelPressed()
    {
      this.nextPressed = false;
    }
    
    public void onNextPressed()
    {
      if (this.nextPressed) {
        return;
      }
      this.nextPressed = true;
      TLRPC.TL_auth_signUp localTL_auth_signUp = new TLRPC.TL_auth_signUp();
      localTL_auth_signUp.phone_code = this.phoneCode;
      localTL_auth_signUp.phone_code_hash = this.phoneHash;
      localTL_auth_signUp.phone_number = this.requestPhone;
      localTL_auth_signUp.first_name = this.firstNameField.getText().toString();
      localTL_auth_signUp.last_name = this.lastNameField.getText().toString();
      LoginActivity.this.needShowProgress(0);
      ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(localTL_auth_signUp, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LoginActivity.LoginActivityRegisterView.access$12802(LoginActivity.LoginActivityRegisterView.this, false);
              LoginActivity.this.needHideProgress();
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_auth_authorization localTL_auth_authorization = (TLRPC.TL_auth_authorization)paramAnonymousTLObject;
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).setUserId(localTL_auth_authorization.user.id);
                UserConfig.getInstance(LoginActivity.this.currentAccount).clearConfig();
                MessagesController.getInstance(LoginActivity.this.currentAccount).cleanup();
                UserConfig.getInstance(LoginActivity.this.currentAccount).setCurrentUser(localTL_auth_authorization.user);
                UserConfig.getInstance(LoginActivity.this.currentAccount).syncContacts = LoginActivity.this.syncContacts;
                UserConfig.getInstance(LoginActivity.this.currentAccount).saveConfig(true);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).cleanup(true);
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(localTL_auth_authorization.user);
                MessagesStorage.getInstance(LoginActivity.this.currentAccount).putUsersAndChats(localArrayList, null, true, true);
                MessagesController.getInstance(LoginActivity.this.currentAccount).putUser(localTL_auth_authorization.user, false);
                ContactsController.getInstance(LoginActivity.this.currentAccount).checkAppAccount();
                MessagesController.getInstance(LoginActivity.this.currentAccount).getBlockedUsers(true);
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).updateDcSettings();
                LoginActivity.this.needFinishActivity();
                return;
              }
              if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_INVALID"))
              {
                LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidPhoneNumber", 2131493682));
                return;
              }
              if ((paramAnonymousTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramAnonymousTL_error.text.contains("PHONE_CODE_INVALID")))
              {
                LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidCode", 2131493679));
                return;
              }
              if (paramAnonymousTL_error.text.contains("PHONE_CODE_EXPIRED"))
              {
                LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("CodeExpired", 2131493268));
                return;
              }
              if (paramAnonymousTL_error.text.contains("FIRSTNAME_INVALID"))
              {
                LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidFirstName", 2131493680));
                return;
              }
              if (paramAnonymousTL_error.text.contains("LASTNAME_INVALID"))
              {
                LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidLastName", 2131493681));
                return;
              }
              LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
            }
          });
        }
      }, 10);
    }
    
    public void onShow()
    {
      super.onShow();
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (LoginActivity.LoginActivityRegisterView.this.firstNameField != null)
          {
            LoginActivity.LoginActivityRegisterView.this.firstNameField.requestFocus();
            LoginActivity.LoginActivityRegisterView.this.firstNameField.setSelection(LoginActivity.LoginActivityRegisterView.this.firstNameField.length());
          }
        }
      }, 100L);
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      this.currentParams = paramBundle.getBundle("registerview_params");
      if (this.currentParams != null) {
        setParams(this.currentParams, true);
      }
      String str = paramBundle.getString("registerview_first");
      if (str != null) {
        this.firstNameField.setText(str);
      }
      paramBundle = paramBundle.getString("registerview_last");
      if (paramBundle != null) {
        this.lastNameField.setText(paramBundle);
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      String str = this.firstNameField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("registerview_first", str);
      }
      str = this.lastNameField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("registerview_last", str);
      }
      if (this.currentParams != null) {
        paramBundle.putBundle("registerview_params", this.currentParams);
      }
    }
    
    public void setParams(Bundle paramBundle, boolean paramBoolean)
    {
      if (paramBundle == null) {
        return;
      }
      this.firstNameField.setText("");
      this.lastNameField.setText("");
      this.requestPhone = paramBundle.getString("phoneFormated");
      this.phoneHash = paramBundle.getString("phoneHash");
      this.phoneCode = paramBundle.getString("code");
      this.currentParams = paramBundle;
    }
  }
  
  public class LoginActivityResetWaitView
    extends SlideView
  {
    private TextView confirmTextView;
    private Bundle currentParams;
    private String phoneCode;
    private String phoneHash;
    private String requestPhone;
    private TextView resetAccountButton;
    private TextView resetAccountText;
    private TextView resetAccountTime;
    private int startTime;
    private Runnable timeRunnable;
    private int waitTime;
    
    public LoginActivityResetWaitView(Context paramContext)
    {
      super();
      setOrientation(1);
      this.confirmTextView = new TextView(paramContext);
      this.confirmTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
      this.confirmTextView.setTextSize(1, 14.0F);
      TextView localTextView = this.confirmTextView;
      if (LocaleController.isRTL)
      {
        i = 5;
        localTextView.setGravity(i);
        this.confirmTextView.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        localTextView = this.confirmTextView;
        if (!LocaleController.isRTL) {
          break label492;
        }
        i = 5;
        label99:
        addView(localTextView, LayoutHelper.createLinear(-2, -2, i));
        this.resetAccountText = new TextView(paramContext);
        localTextView = this.resetAccountText;
        if (!LocaleController.isRTL) {
          break label497;
        }
        i = 5;
        label139:
        localTextView.setGravity(i | 0x30);
        this.resetAccountText.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        this.resetAccountText.setText(LocaleController.getString("ResetAccountStatus", 2131494255));
        this.resetAccountText.setTextSize(1, 14.0F);
        this.resetAccountText.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        localTextView = this.resetAccountText;
        if (!LocaleController.isRTL) {
          break label502;
        }
        i = 5;
        label211:
        addView(localTextView, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 24, 0, 0));
        this.resetAccountTime = new TextView(paramContext);
        localTextView = this.resetAccountTime;
        if (!LocaleController.isRTL) {
          break label507;
        }
        i = 5;
        label259:
        localTextView.setGravity(i | 0x30);
        this.resetAccountTime.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        this.resetAccountTime.setTextSize(1, 14.0F);
        this.resetAccountTime.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        localTextView = this.resetAccountTime;
        if (!LocaleController.isRTL) {
          break label512;
        }
        i = 5;
        label317:
        addView(localTextView, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 2, 0, 0));
        this.resetAccountButton = new TextView(paramContext);
        paramContext = this.resetAccountButton;
        if (!LocaleController.isRTL) {
          break label517;
        }
        i = 5;
        label363:
        paramContext.setGravity(i | 0x30);
        this.resetAccountButton.setText(LocaleController.getString("ResetAccountButton", 2131494252));
        this.resetAccountButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.resetAccountButton.setTextSize(1, 14.0F);
        this.resetAccountButton.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.resetAccountButton.setPadding(0, AndroidUtilities.dp(14.0F), 0, 0);
        paramContext = this.resetAccountButton;
        if (!LocaleController.isRTL) {
          break label522;
        }
      }
      label492:
      label497:
      label502:
      label507:
      label512:
      label517:
      label522:
      for (int i = j;; i = 3)
      {
        addView(paramContext, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 7, 0, 0));
        this.resetAccountButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (Math.abs(ConnectionsManager.getInstance(LoginActivity.this.currentAccount).getCurrentTime() - LoginActivity.LoginActivityResetWaitView.this.startTime) < LoginActivity.LoginActivityResetWaitView.this.waitTime) {
              return;
            }
            paramAnonymousView = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
            paramAnonymousView.setMessage(LocaleController.getString("ResetMyAccountWarningText", 2131494263));
            paramAnonymousView.setTitle(LocaleController.getString("ResetMyAccountWarning", 2131494261));
            paramAnonymousView.setPositiveButton(LocaleController.getString("ResetMyAccountWarningReset", 2131494262), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                LoginActivity.this.needShowProgress(0);
                paramAnonymous2DialogInterface = new TLRPC.TL_account_deleteAccount();
                paramAnonymous2DialogInterface.reason = "Forgot password";
                ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(paramAnonymous2DialogInterface, new RequestDelegate()
                {
                  public void run(TLObject paramAnonymous3TLObject, final TLRPC.TL_error paramAnonymous3TL_error)
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        LoginActivity.this.needHideProgress();
                        if (paramAnonymous3TL_error == null)
                        {
                          Bundle localBundle = new Bundle();
                          localBundle.putString("phoneFormated", LoginActivity.LoginActivityResetWaitView.this.requestPhone);
                          localBundle.putString("phoneHash", LoginActivity.LoginActivityResetWaitView.this.phoneHash);
                          localBundle.putString("code", LoginActivity.LoginActivityResetWaitView.this.phoneCode);
                          LoginActivity.this.setPage(5, true, localBundle, false);
                          return;
                        }
                        if (paramAnonymous3TL_error.text.equals("2FA_RECENT_CONFIRM"))
                        {
                          LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("ResetAccountCancelledAlert", 2131494253));
                          return;
                        }
                        LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymous3TL_error.text);
                      }
                    });
                  }
                }, 10);
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            LoginActivity.this.showDialog(paramAnonymousView.create());
          }
        });
        return;
        i = 3;
        break;
        i = 3;
        break label99;
        i = 3;
        break label139;
        i = 3;
        break label211;
        i = 3;
        break label259;
        i = 3;
        break label317;
        i = 3;
        break label363;
      }
    }
    
    private void updateTimeText()
    {
      int i = Math.max(0, this.waitTime - (ConnectionsManager.getInstance(LoginActivity.this.currentAccount).getCurrentTime() - this.startTime));
      int j = i / 86400;
      int k = (i - j * 86400) / 3600;
      int m = (i - j * 86400 - k * 3600) / 60;
      if (j != 0) {
        this.resetAccountTime.setText(AndroidUtilities.replaceTags(LocaleController.formatPluralString("DaysBold", j) + " " + LocaleController.formatPluralString("HoursBold", k) + " " + LocaleController.formatPluralString("MinutesBold", m)));
      }
      while (i > 0)
      {
        this.resetAccountButton.setTag("windowBackgroundWhiteGrayText6");
        this.resetAccountButton.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        return;
        this.resetAccountTime.setText(AndroidUtilities.replaceTags(LocaleController.formatPluralString("HoursBold", k) + " " + LocaleController.formatPluralString("MinutesBold", m) + " " + LocaleController.formatPluralString("SecondsBold", i % 60)));
      }
      this.resetAccountButton.setTag("windowBackgroundWhiteRedText6");
      this.resetAccountButton.setTextColor(Theme.getColor("windowBackgroundWhiteRedText6"));
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("ResetAccount", 2131494251);
    }
    
    public boolean needBackButton()
    {
      return true;
    }
    
    public void onBackPressed()
    {
      AndroidUtilities.cancelRunOnUIThread(this.timeRunnable);
      this.timeRunnable = null;
      this.currentParams = null;
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      this.currentParams = paramBundle.getBundle("resetview_params");
      if (this.currentParams != null) {
        setParams(this.currentParams, true);
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      if (this.currentParams != null) {
        paramBundle.putBundle("resetview_params", this.currentParams);
      }
    }
    
    public void setParams(Bundle paramBundle, boolean paramBoolean)
    {
      if (paramBundle == null) {
        return;
      }
      this.currentParams = paramBundle;
      this.requestPhone = paramBundle.getString("phoneFormated");
      this.phoneHash = paramBundle.getString("phoneHash");
      this.phoneCode = paramBundle.getString("code");
      this.startTime = paramBundle.getInt("startTime");
      this.waitTime = paramBundle.getInt("waitTime");
      this.confirmTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("ResetAccountInfo", 2131494254, new Object[] { LocaleController.addNbsp(PhoneFormat.getInstance().format("+" + this.requestPhone)) })));
      updateTimeText();
      this.timeRunnable = new Runnable()
      {
        public void run()
        {
          if (LoginActivity.LoginActivityResetWaitView.this.timeRunnable != this) {
            return;
          }
          LoginActivity.LoginActivityResetWaitView.this.updateTimeText();
          AndroidUtilities.runOnUIThread(LoginActivity.LoginActivityResetWaitView.this.timeRunnable, 1000L);
        }
      };
      AndroidUtilities.runOnUIThread(this.timeRunnable, 1000L);
    }
  }
  
  public class LoginActivitySmsView
    extends SlideView
    implements NotificationCenter.NotificationCenterDelegate
  {
    private String catchedPhone;
    private EditTextBoldCursor codeField;
    private volatile int codeTime = 15000;
    private Timer codeTimer;
    private TextView confirmTextView;
    private Bundle currentParams;
    private int currentType;
    private String emailPhone;
    private boolean ignoreOnTextChange;
    private boolean isRestored;
    private double lastCodeTime;
    private double lastCurrentTime;
    private String lastError = "";
    private int length;
    private boolean nextPressed;
    private int nextType;
    private int openTime;
    private String pattern = "*";
    private String phone;
    private String phoneHash;
    private TextView problemText;
    private LoginActivity.ProgressView progressView;
    private String requestPhone;
    private volatile int time = 60000;
    private TextView timeText;
    private Timer timeTimer;
    private int timeout;
    private final Object timerSync = new Object();
    private boolean waitingForEvent;
    private TextView wrongNumber;
    
    public LoginActivitySmsView(Context paramContext, int paramInt)
    {
      super();
      this.currentType = paramInt;
      setOrientation(1);
      this.confirmTextView = new TextView(paramContext);
      this.confirmTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
      this.confirmTextView.setTextSize(1, 14.0F);
      Object localObject1 = this.confirmTextView;
      Object localObject2;
      if (LocaleController.isRTL)
      {
        paramInt = 5;
        ((TextView)localObject1).setGravity(paramInt);
        this.confirmTextView.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        if (this.currentType != 3) {
          break label1032;
        }
        localObject1 = new FrameLayout(paramContext);
        localObject2 = new ImageView(paramContext);
        ((ImageView)localObject2).setImageResource(2131165575);
        if (!LocaleController.isRTL) {
          break label966;
        }
        ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(64, 76.0F, 19, 2.0F, 2.0F, 0.0F, 0.0F));
        localObject2 = this.confirmTextView;
        if (!LocaleController.isRTL) {
          break label961;
        }
        paramInt = 5;
        label198:
        ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-1, -2.0F, paramInt, 82.0F, 0.0F, 0.0F, 0.0F));
        if (!LocaleController.isRTL) {
          break label1027;
        }
        paramInt = 5;
        label225:
        addView((View)localObject1, LayoutHelper.createLinear(-2, -2, paramInt));
        this.codeField = new EditTextBoldCursor(paramContext);
        this.codeField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setHint(LocaleController.getString("Code", 2131493267));
        this.codeField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.codeField.setCursorWidth(1.5F);
        this.codeField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.codeField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.codeField.setImeOptions(268435461);
        this.codeField.setTextSize(1, 18.0F);
        this.codeField.setInputType(3);
        this.codeField.setMaxLines(1);
        this.codeField.setPadding(0, 0, 0, 0);
        addView(this.codeField, LayoutHelper.createLinear(-1, 36, 1, 0, 20, 0, 0));
        this.codeField.addTextChangedListener(new TextWatcher()
        {
          public void afterTextChanged(Editable paramAnonymousEditable)
          {
            if (LoginActivity.LoginActivitySmsView.this.ignoreOnTextChange) {}
            while ((LoginActivity.LoginActivitySmsView.this.length == 0) || (LoginActivity.LoginActivitySmsView.this.codeField.length() != LoginActivity.LoginActivitySmsView.this.length)) {
              return;
            }
            LoginActivity.LoginActivitySmsView.this.onNextPressed();
          }
          
          public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        });
        this.codeField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.LoginActivitySmsView.this.onNextPressed();
              return true;
            }
            return false;
          }
        });
        if (this.currentType == 3)
        {
          this.codeField.setEnabled(false);
          this.codeField.setInputType(0);
          this.codeField.setVisibility(8);
        }
        this.timeText = new TextView(paramContext);
        this.timeText.setTextSize(1, 14.0F);
        this.timeText.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        this.timeText.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        localObject1 = this.timeText;
        if (!LocaleController.isRTL) {
          break label1068;
        }
        paramInt = 5;
        label526:
        ((TextView)localObject1).setGravity(paramInt);
        localObject1 = this.timeText;
        if (!LocaleController.isRTL) {
          break label1073;
        }
        paramInt = 5;
        label546:
        addView((View)localObject1, LayoutHelper.createLinear(-2, -2, paramInt, 0, 30, 0, 0));
        if (this.currentType == 3)
        {
          this.progressView = new LoginActivity.ProgressView(LoginActivity.this, paramContext);
          addView(this.progressView, LayoutHelper.createLinear(-1, 3, 0.0F, 12.0F, 0.0F, 0.0F));
        }
        this.problemText = new TextView(paramContext);
        this.problemText.setText(LocaleController.getString("DidNotGetTheCode", 2131493382));
        localObject1 = this.problemText;
        if (!LocaleController.isRTL) {
          break label1078;
        }
        paramInt = 5;
        label647:
        ((TextView)localObject1).setGravity(paramInt);
        this.problemText.setTextSize(1, 14.0F);
        this.problemText.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
        this.problemText.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.problemText.setPadding(0, AndroidUtilities.dp(2.0F), 0, AndroidUtilities.dp(12.0F));
        localObject1 = this.problemText;
        if (!LocaleController.isRTL) {
          break label1083;
        }
        paramInt = 5;
        label722:
        addView((View)localObject1, LayoutHelper.createLinear(-2, -2, paramInt, 0, 20, 0, 0));
        this.problemText.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (LoginActivity.LoginActivitySmsView.this.nextPressed) {
              return;
            }
            if ((LoginActivity.LoginActivitySmsView.this.nextType != 0) && (LoginActivity.LoginActivitySmsView.this.nextType != 4))
            {
              LoginActivity.LoginActivitySmsView.this.resendCode();
              return;
            }
            try
            {
              paramAnonymousView = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
              paramAnonymousView = String.format(Locale.US, "%s (%d)", new Object[] { paramAnonymousView.versionName, Integer.valueOf(paramAnonymousView.versionCode) });
              Intent localIntent = new Intent("android.intent.action.SEND");
              localIntent.setType("message/rfc822");
              localIntent.putExtra("android.intent.extra.EMAIL", new String[] { "sms@stel.com" });
              localIntent.putExtra("android.intent.extra.SUBJECT", "Android registration/login issue " + paramAnonymousView + " " + LoginActivity.LoginActivitySmsView.this.emailPhone);
              localIntent.putExtra("android.intent.extra.TEXT", "Phone: " + LoginActivity.LoginActivitySmsView.this.requestPhone + "\nApp version: " + paramAnonymousView + "\nOS version: SDK " + Build.VERSION.SDK_INT + "\nDevice Name: " + Build.MANUFACTURER + Build.MODEL + "\nLocale: " + Locale.getDefault() + "\nError: " + LoginActivity.LoginActivitySmsView.this.lastError);
              LoginActivity.LoginActivitySmsView.this.getContext().startActivity(Intent.createChooser(localIntent, "Send email..."));
              return;
            }
            catch (Exception paramAnonymousView)
            {
              LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("NoMailInstalled", 2131493890));
            }
          }
        });
        localObject1 = new LinearLayout(paramContext);
        if (!LocaleController.isRTL) {
          break label1088;
        }
        paramInt = 5;
        label775:
        ((LinearLayout)localObject1).setGravity(paramInt | 0x10);
        if (!LocaleController.isRTL) {
          break label1093;
        }
        paramInt = 5;
        label792:
        addView((View)localObject1, LayoutHelper.createLinear(-1, -1, paramInt));
        this.wrongNumber = new TextView(paramContext);
        paramContext = this.wrongNumber;
        if (!LocaleController.isRTL) {
          break label1098;
        }
        paramInt = 5;
        label829:
        paramContext.setGravity(paramInt | 0x1);
        this.wrongNumber.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText4"));
        this.wrongNumber.setTextSize(1, 14.0F);
        this.wrongNumber.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        this.wrongNumber.setPadding(0, AndroidUtilities.dp(24.0F), 0, 0);
        paramContext = this.wrongNumber;
        if (!LocaleController.isRTL) {
          break label1103;
        }
      }
      label961:
      label966:
      label1027:
      label1032:
      label1068:
      label1073:
      label1078:
      label1083:
      label1088:
      label1093:
      label1098:
      label1103:
      for (paramInt = 5;; paramInt = 3)
      {
        ((LinearLayout)localObject1).addView(paramContext, LayoutHelper.createLinear(-2, -2, paramInt | 0x50, 0, 0, 0, 10));
        this.wrongNumber.setText(LocaleController.getString("WrongNumber", 2131494645));
        this.wrongNumber.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = new TLRPC.TL_auth_cancelCode();
            paramAnonymousView.phone_number = LoginActivity.LoginActivitySmsView.this.requestPhone;
            paramAnonymousView.phone_code_hash = LoginActivity.LoginActivitySmsView.this.phoneHash;
            ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(paramAnonymousView, new RequestDelegate()
            {
              public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error) {}
            }, 10);
            LoginActivity.LoginActivitySmsView.this.onBackPressed();
            LoginActivity.this.setPage(0, true, null, true);
          }
        });
        return;
        paramInt = 3;
        break;
        paramInt = 3;
        break label198;
        TextView localTextView = this.confirmTextView;
        if (LocaleController.isRTL) {}
        for (paramInt = 5;; paramInt = 3)
        {
          ((FrameLayout)localObject1).addView(localTextView, LayoutHelper.createFrame(-1, -2.0F, paramInt, 0.0F, 0.0F, 82.0F, 0.0F));
          ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(64, 76.0F, 21, 0.0F, 2.0F, 0.0F, 2.0F));
          break;
        }
        paramInt = 3;
        break label225;
        localObject1 = this.confirmTextView;
        if (LocaleController.isRTL) {}
        for (paramInt = 5;; paramInt = 3)
        {
          addView((View)localObject1, LayoutHelper.createLinear(-2, -2, paramInt));
          break;
        }
        paramInt = 3;
        break label526;
        paramInt = 3;
        break label546;
        paramInt = 3;
        break label647;
        paramInt = 3;
        break label722;
        paramInt = 3;
        break label775;
        paramInt = 3;
        break label792;
        paramInt = 3;
        break label829;
      }
    }
    
    private void createCodeTimer()
    {
      if (this.codeTimer != null) {
        return;
      }
      this.codeTime = 15000;
      this.codeTimer = new Timer();
      this.lastCodeTime = System.currentTimeMillis();
      this.codeTimer.schedule(new TimerTask()
      {
        public void run()
        {
          double d1 = System.currentTimeMillis();
          double d2 = LoginActivity.LoginActivitySmsView.this.lastCodeTime;
          LoginActivity.LoginActivitySmsView.access$4502(LoginActivity.LoginActivitySmsView.this, (int)(LoginActivity.LoginActivitySmsView.this.codeTime - (d1 - d2)));
          LoginActivity.LoginActivitySmsView.access$4402(LoginActivity.LoginActivitySmsView.this, d1);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (LoginActivity.LoginActivitySmsView.this.codeTime <= 1000)
              {
                LoginActivity.LoginActivitySmsView.this.problemText.setVisibility(0);
                LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
              }
            }
          });
        }
      }, 0L, 1000L);
    }
    
    private void createTimer()
    {
      if (this.timeTimer != null) {
        return;
      }
      this.timeTimer = new Timer();
      this.timeTimer.schedule(new TimerTask()
      {
        public void run()
        {
          if (LoginActivity.LoginActivitySmsView.this.timeTimer == null) {
            return;
          }
          double d1 = System.currentTimeMillis();
          double d2 = LoginActivity.LoginActivitySmsView.this.lastCurrentTime;
          LoginActivity.LoginActivitySmsView.access$5002(LoginActivity.LoginActivitySmsView.this, (int)(LoginActivity.LoginActivitySmsView.this.time - (d1 - d2)));
          LoginActivity.LoginActivitySmsView.access$4902(LoginActivity.LoginActivitySmsView.this, d1);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i;
              int j;
              if (LoginActivity.LoginActivitySmsView.this.time >= 1000)
              {
                i = LoginActivity.LoginActivitySmsView.this.time / 1000 / 60;
                j = LoginActivity.LoginActivitySmsView.this.time / 1000 - i * 60;
                if ((LoginActivity.LoginActivitySmsView.this.nextType == 4) || (LoginActivity.LoginActivitySmsView.this.nextType == 3)) {
                  LoginActivity.LoginActivitySmsView.this.timeText.setText(LocaleController.formatString("CallText", 2131493121, new Object[] { Integer.valueOf(i), Integer.valueOf(j) }));
                }
              }
              do
              {
                do
                {
                  for (;;)
                  {
                    if (LoginActivity.LoginActivitySmsView.this.progressView != null) {
                      LoginActivity.LoginActivitySmsView.this.progressView.setProgress(1.0F - LoginActivity.LoginActivitySmsView.this.time / LoginActivity.LoginActivitySmsView.this.timeout);
                    }
                    return;
                    if (LoginActivity.LoginActivitySmsView.this.nextType == 2) {
                      LoginActivity.LoginActivitySmsView.this.timeText.setText(LocaleController.formatString("SmsText", 2131494414, new Object[] { Integer.valueOf(i), Integer.valueOf(j) }));
                    }
                  }
                  if (LoginActivity.LoginActivitySmsView.this.progressView != null) {
                    LoginActivity.LoginActivitySmsView.this.progressView.setProgress(1.0F);
                  }
                  LoginActivity.LoginActivitySmsView.this.destroyTimer();
                  if (LoginActivity.LoginActivitySmsView.this.currentType == 3)
                  {
                    AndroidUtilities.setWaitingForCall(false);
                    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
                    LoginActivity.LoginActivitySmsView.access$5602(LoginActivity.LoginActivitySmsView.this, false);
                    LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
                    LoginActivity.LoginActivitySmsView.this.resendCode();
                    return;
                  }
                } while (LoginActivity.LoginActivitySmsView.this.currentType != 2);
                if (LoginActivity.LoginActivitySmsView.this.nextType == 4)
                {
                  LoginActivity.LoginActivitySmsView.this.timeText.setText(LocaleController.getString("Calling", 2131493123));
                  LoginActivity.LoginActivitySmsView.this.createCodeTimer();
                  TLRPC.TL_auth_resendCode localTL_auth_resendCode = new TLRPC.TL_auth_resendCode();
                  localTL_auth_resendCode.phone_number = LoginActivity.LoginActivitySmsView.this.requestPhone;
                  localTL_auth_resendCode.phone_code_hash = LoginActivity.LoginActivitySmsView.this.phoneHash;
                  ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(localTL_auth_resendCode, new RequestDelegate()
                  {
                    public void run(TLObject paramAnonymous3TLObject, final TLRPC.TL_error paramAnonymous3TL_error)
                    {
                      if ((paramAnonymous3TL_error != null) && (paramAnonymous3TL_error.text != null)) {
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            LoginActivity.LoginActivitySmsView.access$4002(LoginActivity.LoginActivitySmsView.this, paramAnonymous3TL_error.text);
                          }
                        });
                      }
                    }
                  }, 10);
                  return;
                }
              } while (LoginActivity.LoginActivitySmsView.this.nextType != 3);
              AndroidUtilities.setWaitingForSms(false);
              NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
              LoginActivity.LoginActivitySmsView.access$5602(LoginActivity.LoginActivitySmsView.this, false);
              LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
              LoginActivity.LoginActivitySmsView.this.resendCode();
            }
          });
        }
      }, 0L, 1000L);
    }
    
    private void destroyCodeTimer()
    {
      try
      {
        synchronized (this.timerSync)
        {
          if (this.codeTimer != null)
          {
            this.codeTimer.cancel();
            this.codeTimer = null;
          }
          return;
        }
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    private void destroyTimer()
    {
      try
      {
        synchronized (this.timerSync)
        {
          if (this.timeTimer != null)
          {
            this.timeTimer.cancel();
            this.timeTimer = null;
          }
          return;
        }
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    
    private void resendCode()
    {
      final Bundle localBundle = new Bundle();
      localBundle.putString("phone", this.phone);
      localBundle.putString("ephone", this.emailPhone);
      localBundle.putString("phoneFormated", this.requestPhone);
      this.nextPressed = true;
      TLRPC.TL_auth_resendCode localTL_auth_resendCode = new TLRPC.TL_auth_resendCode();
      localTL_auth_resendCode.phone_number = this.requestPhone;
      localTL_auth_resendCode.phone_code_hash = this.phoneHash;
      ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(localTL_auth_resendCode, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LoginActivity.LoginActivitySmsView.access$3502(LoginActivity.LoginActivitySmsView.this, false);
              if (paramAnonymousTL_error == null) {
                LoginActivity.this.fillNextCodeParams(LoginActivity.LoginActivitySmsView.5.this.val$params, (TLRPC.TL_auth_sentCode)paramAnonymousTLObject);
              }
              for (;;)
              {
                LoginActivity.this.needHideProgress();
                return;
                if (paramAnonymousTL_error.text != null) {
                  if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_INVALID"))
                  {
                    LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidPhoneNumber", 2131493682));
                  }
                  else if ((paramAnonymousTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramAnonymousTL_error.text.contains("PHONE_CODE_INVALID")))
                  {
                    LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidCode", 2131493679));
                  }
                  else if (paramAnonymousTL_error.text.contains("PHONE_CODE_EXPIRED"))
                  {
                    LoginActivity.LoginActivitySmsView.this.onBackPressed();
                    LoginActivity.this.setPage(0, true, null, true);
                    LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("CodeExpired", 2131493268));
                  }
                  else if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
                  {
                    LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("FloodWait", 2131493543));
                  }
                  else if (paramAnonymousTL_error.code != 64536)
                  {
                    LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramAnonymousTL_error.text);
                  }
                }
              }
            }
          });
        }
      }, 10);
      LoginActivity.this.needShowProgress(0);
    }
    
    public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
    {
      if ((!this.waitingForEvent) || (this.codeField == null)) {}
      do
      {
        do
        {
          return;
          if (paramInt1 == NotificationCenter.didReceiveSmsCode)
          {
            this.ignoreOnTextChange = true;
            this.codeField.setText("" + paramVarArgs[0]);
            this.ignoreOnTextChange = false;
            onNextPressed();
            return;
          }
        } while (paramInt1 != NotificationCenter.didReceiveCall);
        paramVarArgs = "" + paramVarArgs[0];
      } while (!AndroidUtilities.checkPhonePattern(this.pattern, paramVarArgs));
      if (!this.pattern.equals("*"))
      {
        this.catchedPhone = paramVarArgs;
        AndroidUtilities.endIncomingCall();
        AndroidUtilities.removeLoginPhoneCall(paramVarArgs, true);
      }
      this.ignoreOnTextChange = true;
      this.codeField.setText(paramVarArgs);
      this.ignoreOnTextChange = false;
      onNextPressed();
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("YourCode", 2131494660);
    }
    
    public void onBackPressed()
    {
      destroyTimer();
      destroyCodeTimer();
      this.currentParams = null;
      if (this.currentType == 2)
      {
        AndroidUtilities.setWaitingForSms(false);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
      }
      for (;;)
      {
        this.waitingForEvent = false;
        return;
        if (this.currentType == 3)
        {
          AndroidUtilities.setWaitingForCall(false);
          NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
        }
      }
    }
    
    public void onCancelPressed()
    {
      this.nextPressed = false;
    }
    
    public void onDestroyActivity()
    {
      super.onDestroyActivity();
      if (this.currentType == 2)
      {
        AndroidUtilities.setWaitingForSms(false);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
      }
      for (;;)
      {
        this.waitingForEvent = false;
        destroyTimer();
        destroyCodeTimer();
        return;
        if (this.currentType == 3)
        {
          AndroidUtilities.setWaitingForCall(false);
          NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
        }
      }
    }
    
    public void onNextPressed()
    {
      if (this.nextPressed) {
        return;
      }
      this.nextPressed = true;
      if (this.currentType == 2)
      {
        AndroidUtilities.setWaitingForSms(false);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
      }
      for (;;)
      {
        this.waitingForEvent = false;
        final String str = this.codeField.getText().toString();
        final TLRPC.TL_auth_signIn localTL_auth_signIn = new TLRPC.TL_auth_signIn();
        localTL_auth_signIn.phone_number = this.requestPhone;
        localTL_auth_signIn.phone_code = str;
        localTL_auth_signIn.phone_code_hash = this.phoneHash;
        destroyTimer();
        ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest(localTL_auth_signIn, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                LoginActivity.LoginActivitySmsView.access$3502(LoginActivity.LoginActivitySmsView.this, false);
                int j = 0;
                int i;
                Object localObject;
                if (paramAnonymousTL_error == null)
                {
                  i = 1;
                  LoginActivity.this.needHideProgress();
                  localObject = (TLRPC.TL_auth_authorization)paramAnonymousTLObject;
                  ConnectionsManager.getInstance(LoginActivity.this.currentAccount).setUserId(((TLRPC.TL_auth_authorization)localObject).user.id);
                  LoginActivity.LoginActivitySmsView.this.destroyTimer();
                  LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
                  UserConfig.getInstance(LoginActivity.this.currentAccount).clearConfig();
                  MessagesController.getInstance(LoginActivity.this.currentAccount).cleanup();
                  UserConfig.getInstance(LoginActivity.this.currentAccount).syncContacts = LoginActivity.this.syncContacts;
                  UserConfig.getInstance(LoginActivity.this.currentAccount).setCurrentUser(((TLRPC.TL_auth_authorization)localObject).user);
                  UserConfig.getInstance(LoginActivity.this.currentAccount).saveConfig(true);
                  MessagesStorage.getInstance(LoginActivity.this.currentAccount).cleanup(true);
                  ArrayList localArrayList = new ArrayList();
                  localArrayList.add(((TLRPC.TL_auth_authorization)localObject).user);
                  MessagesStorage.getInstance(LoginActivity.this.currentAccount).putUsersAndChats(localArrayList, null, true, true);
                  MessagesController.getInstance(LoginActivity.this.currentAccount).putUser(((TLRPC.TL_auth_authorization)localObject).user, false);
                  ContactsController.getInstance(LoginActivity.this.currentAccount).checkAppAccount();
                  MessagesController.getInstance(LoginActivity.this.currentAccount).getBlockedUsers(true);
                  ConnectionsManager.getInstance(LoginActivity.this.currentAccount).updateDcSettings();
                  LoginActivity.this.needFinishActivity();
                }
                for (;;)
                {
                  if ((i != 0) && (LoginActivity.LoginActivitySmsView.this.currentType == 3))
                  {
                    AndroidUtilities.endIncomingCall();
                    AndroidUtilities.removeLoginPhoneCall(LoginActivity.LoginActivitySmsView.8.this.val$code, true);
                  }
                  return;
                  LoginActivity.LoginActivitySmsView.access$4002(LoginActivity.LoginActivitySmsView.this, paramAnonymousTL_error.text);
                  if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_UNOCCUPIED"))
                  {
                    i = 1;
                    LoginActivity.this.needHideProgress();
                    localObject = new Bundle();
                    ((Bundle)localObject).putString("phoneFormated", LoginActivity.LoginActivitySmsView.this.requestPhone);
                    ((Bundle)localObject).putString("phoneHash", LoginActivity.LoginActivitySmsView.this.phoneHash);
                    ((Bundle)localObject).putString("code", LoginActivity.LoginActivitySmsView.8.this.val$req.phone_code);
                    LoginActivity.this.setPage(5, true, (Bundle)localObject, false);
                    LoginActivity.LoginActivitySmsView.this.destroyTimer();
                    LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
                  }
                  else if (paramAnonymousTL_error.text.contains("SESSION_PASSWORD_NEEDED"))
                  {
                    i = 1;
                    localObject = new TLRPC.TL_account_getPassword();
                    ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
                    {
                      public void run(final TLObject paramAnonymous3TLObject, final TLRPC.TL_error paramAnonymous3TL_error)
                      {
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            LoginActivity.this.needHideProgress();
                            if (paramAnonymous3TL_error == null)
                            {
                              TLRPC.TL_account_password localTL_account_password = (TLRPC.TL_account_password)paramAnonymous3TLObject;
                              Bundle localBundle = new Bundle();
                              localBundle.putString("current_salt", Utilities.bytesToHex(localTL_account_password.current_salt));
                              localBundle.putString("hint", localTL_account_password.hint);
                              localBundle.putString("email_unconfirmed_pattern", localTL_account_password.email_unconfirmed_pattern);
                              localBundle.putString("phoneFormated", LoginActivity.LoginActivitySmsView.this.requestPhone);
                              localBundle.putString("phoneHash", LoginActivity.LoginActivitySmsView.this.phoneHash);
                              localBundle.putString("code", LoginActivity.LoginActivitySmsView.8.this.val$req.phone_code);
                              if (localTL_account_password.has_recovery) {}
                              for (int i = 1;; i = 0)
                              {
                                localBundle.putInt("has_recovery", i);
                                LoginActivity.this.setPage(6, true, localBundle, false);
                                return;
                              }
                            }
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymous3TL_error.text);
                          }
                        });
                      }
                    }, 10);
                    LoginActivity.LoginActivitySmsView.this.destroyTimer();
                    LoginActivity.LoginActivitySmsView.this.destroyCodeTimer();
                  }
                  else
                  {
                    LoginActivity.this.needHideProgress();
                    if (((LoginActivity.LoginActivitySmsView.this.currentType == 3) && ((LoginActivity.LoginActivitySmsView.this.nextType == 4) || (LoginActivity.LoginActivitySmsView.this.nextType == 2))) || ((LoginActivity.LoginActivitySmsView.this.currentType == 2) && ((LoginActivity.LoginActivitySmsView.this.nextType == 4) || (LoginActivity.LoginActivitySmsView.this.nextType == 3)))) {
                      LoginActivity.LoginActivitySmsView.this.createTimer();
                    }
                    if (LoginActivity.LoginActivitySmsView.this.currentType == 2)
                    {
                      AndroidUtilities.setWaitingForSms(true);
                      NotificationCenter.getGlobalInstance().addObserver(LoginActivity.LoginActivitySmsView.this, NotificationCenter.didReceiveSmsCode);
                    }
                    for (;;)
                    {
                      LoginActivity.LoginActivitySmsView.access$5602(LoginActivity.LoginActivitySmsView.this, true);
                      i = j;
                      if (LoginActivity.LoginActivitySmsView.this.currentType == 3) {
                        break;
                      }
                      if (!paramAnonymousTL_error.text.contains("PHONE_NUMBER_INVALID")) {
                        break label877;
                      }
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidPhoneNumber", 2131493682));
                      i = j;
                      break;
                      if (LoginActivity.LoginActivitySmsView.this.currentType == 3)
                      {
                        AndroidUtilities.setWaitingForCall(true);
                        NotificationCenter.getGlobalInstance().addObserver(LoginActivity.LoginActivitySmsView.this, NotificationCenter.didReceiveCall);
                      }
                    }
                    label877:
                    if ((paramAnonymousTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramAnonymousTL_error.text.contains("PHONE_CODE_INVALID")))
                    {
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidCode", 2131493679));
                      i = j;
                    }
                    else if (paramAnonymousTL_error.text.contains("PHONE_CODE_EXPIRED"))
                    {
                      LoginActivity.LoginActivitySmsView.this.onBackPressed();
                      LoginActivity.this.setPage(0, true, null, true);
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("CodeExpired", 2131493268));
                      i = j;
                    }
                    else if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
                    {
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("FloodWait", 2131493543));
                      i = j;
                    }
                    else
                    {
                      LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramAnonymousTL_error.text);
                      i = j;
                    }
                  }
                }
              }
            });
          }
        }, 10);
        LoginActivity.this.needShowProgress(0);
        return;
        if (this.currentType == 3)
        {
          AndroidUtilities.setWaitingForCall(false);
          NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
        }
      }
    }
    
    public void onShow()
    {
      super.onShow();
      if (this.currentType == 3) {
        return;
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (LoginActivity.LoginActivitySmsView.this.codeField != null)
          {
            LoginActivity.LoginActivitySmsView.this.codeField.requestFocus();
            LoginActivity.LoginActivitySmsView.this.codeField.setSelection(LoginActivity.LoginActivitySmsView.this.codeField.length());
          }
        }
      }, 100L);
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      this.currentParams = paramBundle.getBundle("smsview_params_" + this.currentType);
      if (this.currentParams != null) {
        setParams(this.currentParams, true);
      }
      String str = paramBundle.getString("catchedPhone");
      if (str != null) {
        this.catchedPhone = str;
      }
      str = paramBundle.getString("smsview_code_" + this.currentType);
      if (str != null) {
        this.codeField.setText(str);
      }
      int i = paramBundle.getInt("time");
      if (i != 0) {
        this.time = i;
      }
      i = paramBundle.getInt("open");
      if (i != 0) {
        this.openTime = i;
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      String str = this.codeField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("smsview_code_" + this.currentType, str);
      }
      if (this.catchedPhone != null) {
        paramBundle.putString("catchedPhone", this.catchedPhone);
      }
      if (this.currentParams != null) {
        paramBundle.putBundle("smsview_params_" + this.currentType, this.currentParams);
      }
      if (this.time != 0) {
        paramBundle.putInt("time", this.time);
      }
      if (this.openTime != 0) {
        paramBundle.putInt("open", this.openTime);
      }
    }
    
    public void setParams(Bundle paramBundle, boolean paramBoolean)
    {
      if (paramBundle == null) {}
      int i;
      label192:
      label213:
      do
      {
        return;
        this.isRestored = paramBoolean;
        this.codeField.setText("");
        this.waitingForEvent = true;
        if (this.currentType != 2) {
          break;
        }
        AndroidUtilities.setWaitingForSms(true);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveSmsCode);
        this.currentParams = paramBundle;
        this.phone = paramBundle.getString("phone");
        this.emailPhone = paramBundle.getString("ephone");
        this.requestPhone = paramBundle.getString("phoneFormated");
        this.phoneHash = paramBundle.getString("phoneHash");
        i = paramBundle.getInt("timeout");
        this.time = i;
        this.timeout = i;
        this.openTime = ((int)(System.currentTimeMillis() / 1000L));
        this.nextType = paramBundle.getInt("nextType");
        this.pattern = paramBundle.getString("pattern");
        this.length = paramBundle.getInt("length");
        if (this.length == 0) {
          break label359;
        }
        paramBundle = new InputFilter.LengthFilter(this.length);
        this.codeField.setFilters(new InputFilter[] { paramBundle });
        if (this.progressView != null)
        {
          paramBundle = this.progressView;
          if (this.nextType == 0) {
            break label373;
          }
          i = 0;
          paramBundle.setVisibility(i);
        }
      } while (this.phone == null);
      String str = PhoneFormat.getInstance().format(this.phone);
      paramBundle = "";
      if (this.currentType == 1)
      {
        paramBundle = AndroidUtilities.replaceTags(LocaleController.getString("SentAppCode", 2131494361));
        label261:
        this.confirmTextView.setText(paramBundle);
        if (this.currentType == 3) {
          break label487;
        }
        AndroidUtilities.showKeyboard(this.codeField);
        this.codeField.requestFocus();
      }
      for (;;)
      {
        destroyTimer();
        destroyCodeTimer();
        this.lastCurrentTime = System.currentTimeMillis();
        if (this.currentType != 1) {
          break label497;
        }
        this.problemText.setVisibility(0);
        this.timeText.setVisibility(8);
        return;
        if (this.currentType != 3) {
          break;
        }
        AndroidUtilities.setWaitingForCall(true);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveCall);
        break;
        label359:
        this.codeField.setFilters(new InputFilter[0]);
        break label192;
        label373:
        i = 8;
        break label213;
        if (this.currentType == 2)
        {
          paramBundle = AndroidUtilities.replaceTags(LocaleController.formatString("SentSmsCode", 2131494364, new Object[] { LocaleController.addNbsp(str) }));
          break label261;
        }
        if (this.currentType == 3)
        {
          paramBundle = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallCode", 2131494362, new Object[] { LocaleController.addNbsp(str) }));
          break label261;
        }
        if (this.currentType != 4) {
          break label261;
        }
        paramBundle = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallOnly", 2131494363, new Object[] { LocaleController.addNbsp(str) }));
        break label261;
        label487:
        AndroidUtilities.hideKeyboard(this.codeField);
      }
      label497:
      if ((this.currentType == 3) && ((this.nextType == 4) || (this.nextType == 2)))
      {
        this.problemText.setVisibility(8);
        this.timeText.setVisibility(0);
        if (this.nextType == 4)
        {
          this.timeText.setText(LocaleController.formatString("CallText", 2131493121, new Object[] { Integer.valueOf(1), Integer.valueOf(0) }));
          if (!this.isRestored) {
            break label667;
          }
        }
        label667:
        for (paramBundle = AndroidUtilities.obtainLoginPhoneCall(this.pattern);; paramBundle = null)
        {
          if (paramBundle == null) {
            break label672;
          }
          this.ignoreOnTextChange = true;
          this.codeField.setText(paramBundle);
          this.ignoreOnTextChange = false;
          onNextPressed();
          return;
          if (this.nextType != 2) {
            break;
          }
          this.timeText.setText(LocaleController.formatString("SmsText", 2131494414, new Object[] { Integer.valueOf(1), Integer.valueOf(0) }));
          break;
        }
        label672:
        if (this.catchedPhone != null)
        {
          this.ignoreOnTextChange = true;
          this.codeField.setText(this.catchedPhone);
          this.ignoreOnTextChange = false;
          onNextPressed();
          return;
        }
        createTimer();
        return;
      }
      if ((this.currentType == 2) && ((this.nextType == 4) || (this.nextType == 3)))
      {
        this.timeText.setVisibility(0);
        this.timeText.setText(LocaleController.formatString("CallText", 2131493121, new Object[] { Integer.valueOf(2), Integer.valueOf(0) }));
        paramBundle = this.problemText;
        if (this.time < 1000) {}
        for (i = 0;; i = 8)
        {
          paramBundle.setVisibility(i);
          createTimer();
          return;
        }
      }
      this.timeText.setVisibility(8);
      this.problemText.setVisibility(8);
      createCodeTimer();
    }
  }
  
  public class PhoneView
    extends SlideView
    implements AdapterView.OnItemSelectedListener
  {
    private CheckBoxCell checkBoxCell;
    private EditTextBoldCursor codeField;
    private HashMap<String, String> codesMap = new HashMap();
    private ArrayList<String> countriesArray = new ArrayList();
    private HashMap<String, String> countriesMap = new HashMap();
    private TextView countryButton;
    private int countryState = 0;
    private boolean ignoreOnPhoneChange = false;
    private boolean ignoreOnTextChange = false;
    private boolean ignoreSelection = false;
    private boolean nextPressed = false;
    private HintEditText phoneField;
    private HashMap<String, String> phoneFormatMap = new HashMap();
    private TextView textView;
    private TextView textView2;
    private View view;
    
    public PhoneView(Context paramContext)
    {
      super();
      setOrientation(1);
      this.countryButton = new TextView(paramContext);
      this.countryButton.setTextSize(1, 18.0F);
      this.countryButton.setPadding(AndroidUtilities.dp(12.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(12.0F), 0);
      this.countryButton.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.countryButton.setMaxLines(1);
      this.countryButton.setSingleLine(true);
      this.countryButton.setEllipsize(TextUtils.TruncateAt.END);
      Object localObject1 = this.countryButton;
      int i;
      Object localObject2;
      if (LocaleController.isRTL)
      {
        i = 5;
        ((TextView)localObject1).setGravity(i | 0x1);
        this.countryButton.setBackgroundResource(2131165650);
        addView(this.countryButton, LayoutHelper.createLinear(-1, 36, 0.0F, 0.0F, 0.0F, 14.0F));
        this.countryButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            paramAnonymousView = new CountrySelectActivity(true);
            paramAnonymousView.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate()
            {
              public void didSelectCountry(String paramAnonymous2String1, String paramAnonymous2String2)
              {
                LoginActivity.PhoneView.this.selectCountry(paramAnonymous2String1);
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    AndroidUtilities.showKeyboard(LoginActivity.PhoneView.this.phoneField);
                  }
                }, 300L);
                LoginActivity.PhoneView.this.phoneField.requestFocus();
                LoginActivity.PhoneView.this.phoneField.setSelection(LoginActivity.PhoneView.this.phoneField.length());
              }
            });
            LoginActivity.this.presentFragment(paramAnonymousView);
          }
        });
        this.view = new View(paramContext);
        this.view.setPadding(AndroidUtilities.dp(12.0F), 0, AndroidUtilities.dp(12.0F), 0);
        this.view.setBackgroundColor(Theme.getColor("windowBackgroundWhiteGrayLine"));
        addView(this.view, LayoutHelper.createLinear(-1, 1, 4.0F, -17.5F, 4.0F, 0.0F));
        localObject1 = new LinearLayout(paramContext);
        ((LinearLayout)localObject1).setOrientation(0);
        addView((View)localObject1, LayoutHelper.createLinear(-1, -2, 0.0F, 20.0F, 0.0F, 0.0F));
        this.textView = new TextView(paramContext);
        this.textView.setText("+");
        this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.textView.setTextSize(1, 18.0F);
        ((LinearLayout)localObject1).addView(this.textView, LayoutHelper.createLinear(-2, -2));
        this.codeField = new EditTextBoldCursor(paramContext);
        this.codeField.setInputType(3);
        this.codeField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.codeField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.codeField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.codeField.setCursorWidth(1.5F);
        this.codeField.setPadding(AndroidUtilities.dp(10.0F), 0, 0, 0);
        this.codeField.setTextSize(1, 18.0F);
        this.codeField.setMaxLines(1);
        this.codeField.setGravity(19);
        this.codeField.setImeOptions(268435461);
        localObject2 = new InputFilter.LengthFilter(5);
        this.codeField.setFilters(new InputFilter[] { localObject2 });
        ((LinearLayout)localObject1).addView(this.codeField, LayoutHelper.createLinear(55, 36, -9.0F, 0.0F, 16.0F, 0.0F));
        this.codeField.addTextChangedListener(new TextWatcher()
        {
          public void afterTextChanged(Editable paramAnonymousEditable)
          {
            if (LoginActivity.PhoneView.this.ignoreOnTextChange) {
              return;
            }
            LoginActivity.PhoneView.access$902(LoginActivity.PhoneView.this, true);
            Object localObject3 = PhoneFormat.stripExceptNumbers(LoginActivity.PhoneView.this.codeField.getText().toString());
            LoginActivity.PhoneView.this.codeField.setText((CharSequence)localObject3);
            if (((String)localObject3).length() == 0)
            {
              LoginActivity.PhoneView.this.countryButton.setText(LocaleController.getString("ChooseCountry", 2131493246));
              LoginActivity.PhoneView.this.phoneField.setHintText(null);
              LoginActivity.PhoneView.access$1202(LoginActivity.PhoneView.this, 1);
              LoginActivity.PhoneView.access$902(LoginActivity.PhoneView.this, false);
              return;
            }
            int j = 0;
            int k = 0;
            paramAnonymousEditable = null;
            Object localObject4 = null;
            Object localObject2 = localObject3;
            label130:
            int i;
            if (((String)localObject3).length() > 4)
            {
              j = 4;
              i = k;
              localObject1 = localObject3;
              paramAnonymousEditable = (Editable)localObject4;
              if (j >= 1)
              {
                paramAnonymousEditable = ((String)localObject3).substring(0, j);
                if ((String)LoginActivity.PhoneView.this.codesMap.get(paramAnonymousEditable) == null) {
                  break label523;
                }
                i = 1;
                localObject2 = ((String)localObject3).substring(j, ((String)localObject3).length()) + LoginActivity.PhoneView.this.phoneField.getText().toString();
                localObject3 = LoginActivity.PhoneView.this.codeField;
                localObject1 = paramAnonymousEditable;
                ((EditTextBoldCursor)localObject3).setText(paramAnonymousEditable);
                paramAnonymousEditable = (Editable)localObject2;
              }
              j = i;
              localObject2 = localObject1;
              if (i == 0)
              {
                paramAnonymousEditable = ((String)localObject1).substring(1, ((String)localObject1).length()) + LoginActivity.PhoneView.this.phoneField.getText().toString();
                localObject3 = LoginActivity.PhoneView.this.codeField;
                localObject2 = ((String)localObject1).substring(0, 1);
                ((EditTextBoldCursor)localObject3).setText((CharSequence)localObject2);
                j = i;
              }
            }
            Object localObject1 = (String)LoginActivity.PhoneView.this.codesMap.get(localObject2);
            if (localObject1 != null)
            {
              i = LoginActivity.PhoneView.this.countriesArray.indexOf(localObject1);
              if (i != -1)
              {
                LoginActivity.PhoneView.access$1502(LoginActivity.PhoneView.this, true);
                LoginActivity.PhoneView.this.countryButton.setText((CharSequence)LoginActivity.PhoneView.this.countriesArray.get(i));
                localObject1 = (String)LoginActivity.PhoneView.this.phoneFormatMap.get(localObject2);
                localObject2 = LoginActivity.PhoneView.this.phoneField;
                if (localObject1 != null)
                {
                  localObject1 = ((String)localObject1).replace('X', '–');
                  label429:
                  ((HintEditText)localObject2).setHintText((String)localObject1);
                  LoginActivity.PhoneView.access$1202(LoginActivity.PhoneView.this, 0);
                }
              }
            }
            for (;;)
            {
              if (j == 0) {
                LoginActivity.PhoneView.this.codeField.setSelection(LoginActivity.PhoneView.this.codeField.getText().length());
              }
              if (paramAnonymousEditable == null) {
                break;
              }
              LoginActivity.PhoneView.this.phoneField.requestFocus();
              LoginActivity.PhoneView.this.phoneField.setText(paramAnonymousEditable);
              LoginActivity.PhoneView.this.phoneField.setSelection(LoginActivity.PhoneView.this.phoneField.length());
              break;
              label523:
              j -= 1;
              break label130;
              localObject1 = null;
              break label429;
              LoginActivity.PhoneView.this.countryButton.setText(LocaleController.getString("WrongCountry", 2131494644));
              LoginActivity.PhoneView.this.phoneField.setHintText(null);
              LoginActivity.PhoneView.access$1202(LoginActivity.PhoneView.this, 2);
              continue;
              LoginActivity.PhoneView.this.countryButton.setText(LocaleController.getString("WrongCountry", 2131494644));
              LoginActivity.PhoneView.this.phoneField.setHintText(null);
              LoginActivity.PhoneView.access$1202(LoginActivity.PhoneView.this, 2);
            }
          }
          
          public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        });
        this.codeField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.PhoneView.this.phoneField.requestFocus();
              LoginActivity.PhoneView.this.phoneField.setSelection(LoginActivity.PhoneView.this.phoneField.length());
              return true;
            }
            return false;
          }
        });
        this.phoneField = new HintEditText(paramContext);
        this.phoneField.setInputType(3);
        this.phoneField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.phoneField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
        this.phoneField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
        this.phoneField.setPadding(0, 0, 0, 0);
        this.phoneField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.phoneField.setCursorSize(AndroidUtilities.dp(20.0F));
        this.phoneField.setCursorWidth(1.5F);
        this.phoneField.setTextSize(1, 18.0F);
        this.phoneField.setMaxLines(1);
        this.phoneField.setGravity(19);
        this.phoneField.setImeOptions(268435461);
        ((LinearLayout)localObject1).addView(this.phoneField, LayoutHelper.createFrame(-1, 36.0F));
        this.phoneField.addTextChangedListener(new TextWatcher()
        {
          private int actionPosition;
          private int characterAction = -1;
          
          public void afterTextChanged(Editable paramAnonymousEditable)
          {
            if (LoginActivity.PhoneView.this.ignoreOnPhoneChange) {
              return;
            }
            int j = LoginActivity.PhoneView.this.phoneField.getSelectionStart();
            Object localObject = LoginActivity.PhoneView.this.phoneField.getText().toString();
            int i = j;
            paramAnonymousEditable = (Editable)localObject;
            if (this.characterAction == 3)
            {
              paramAnonymousEditable = ((String)localObject).substring(0, this.actionPosition) + ((String)localObject).substring(this.actionPosition + 1, ((String)localObject).length());
              i = j - 1;
            }
            localObject = new StringBuilder(paramAnonymousEditable.length());
            j = 0;
            while (j < paramAnonymousEditable.length())
            {
              String str = paramAnonymousEditable.substring(j, j + 1);
              if ("0123456789".contains(str)) {
                ((StringBuilder)localObject).append(str);
              }
              j += 1;
            }
            LoginActivity.PhoneView.access$1702(LoginActivity.PhoneView.this, true);
            paramAnonymousEditable = LoginActivity.PhoneView.this.phoneField.getHintText();
            j = i;
            if (paramAnonymousEditable != null)
            {
              int k = 0;
              for (;;)
              {
                j = i;
                if (k >= ((StringBuilder)localObject).length()) {
                  break label341;
                }
                if (k >= paramAnonymousEditable.length()) {
                  break;
                }
                int m = k;
                j = i;
                if (paramAnonymousEditable.charAt(k) == ' ')
                {
                  ((StringBuilder)localObject).insert(k, ' ');
                  k += 1;
                  m = k;
                  j = i;
                  if (i == k)
                  {
                    m = k;
                    j = i;
                    if (this.characterAction != 2)
                    {
                      m = k;
                      j = i;
                      if (this.characterAction != 3)
                      {
                        j = i + 1;
                        m = k;
                      }
                    }
                  }
                }
                k = m + 1;
                i = j;
              }
              ((StringBuilder)localObject).insert(k, ' ');
              j = i;
              if (i == k + 1)
              {
                j = i;
                if (this.characterAction != 2)
                {
                  j = i;
                  if (this.characterAction != 3) {
                    j = i + 1;
                  }
                }
              }
            }
            label341:
            LoginActivity.PhoneView.this.phoneField.setText((CharSequence)localObject);
            if (j >= 0)
            {
              paramAnonymousEditable = LoginActivity.PhoneView.this.phoneField;
              if (j > LoginActivity.PhoneView.this.phoneField.length()) {
                break label404;
              }
            }
            for (;;)
            {
              paramAnonymousEditable.setSelection(j);
              LoginActivity.PhoneView.this.phoneField.onTextChange();
              LoginActivity.PhoneView.access$1702(LoginActivity.PhoneView.this, false);
              return;
              label404:
              j = LoginActivity.PhoneView.this.phoneField.length();
            }
          }
          
          public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
          {
            if ((paramAnonymousInt2 == 0) && (paramAnonymousInt3 == 1))
            {
              this.characterAction = 1;
              return;
            }
            if ((paramAnonymousInt2 == 1) && (paramAnonymousInt3 == 0))
            {
              if ((paramAnonymousCharSequence.charAt(paramAnonymousInt1) == ' ') && (paramAnonymousInt1 > 0))
              {
                this.characterAction = 3;
                this.actionPosition = (paramAnonymousInt1 - 1);
                return;
              }
              this.characterAction = 2;
              return;
            }
            this.characterAction = -1;
          }
          
          public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
        });
        this.phoneField.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
          public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
          {
            if (paramAnonymousInt == 5)
            {
              LoginActivity.PhoneView.this.onNextPressed();
              return true;
            }
            return false;
          }
        });
        this.textView2 = new TextView(paramContext);
        this.textView2.setText(LocaleController.getString("StartText", 2131494423));
        this.textView2.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText6"));
        this.textView2.setTextSize(1, 14.0F);
        localObject1 = this.textView2;
        if (!LocaleController.isRTL) {
          break label1306;
        }
        i = 5;
        label848:
        ((TextView)localObject1).setGravity(i);
        this.textView2.setLineSpacing(AndroidUtilities.dp(2.0F), 1.0F);
        localObject1 = this.textView2;
        if (!LocaleController.isRTL) {
          break label1311;
        }
        i = 5;
        label881:
        addView((View)localObject1, LayoutHelper.createLinear(-2, -2, i, 0, 28, 0, 10));
        if (LoginActivity.this.newAccount)
        {
          this.checkBoxCell = new CheckBoxCell(paramContext, 2);
          this.checkBoxCell.setText(LocaleController.getString("SyncContacts", 2131494455), "", LoginActivity.this.syncContacts, false);
          addView(this.checkBoxCell, LayoutHelper.createLinear(-2, -1, 51, 0, 0, 0, 0));
          this.checkBoxCell.setOnClickListener(new View.OnClickListener()
          {
            private Toast visibleToast;
            
            public void onClick(View paramAnonymousView)
            {
              if (LoginActivity.this.getParentActivity() == null) {
                return;
              }
              paramAnonymousView = (CheckBoxCell)paramAnonymousView;
              LoginActivity localLoginActivity = LoginActivity.this;
              boolean bool;
              if (!LoginActivity.this.syncContacts) {
                bool = true;
              }
              for (;;)
              {
                LoginActivity.access$1902(localLoginActivity, bool);
                paramAnonymousView.setChecked(LoginActivity.this.syncContacts, true);
                try
                {
                  if (this.visibleToast != null) {
                    this.visibleToast.cancel();
                  }
                  if (LoginActivity.this.syncContacts)
                  {
                    this.visibleToast = Toast.makeText(LoginActivity.this.getParentActivity(), LocaleController.getString("SyncContactsOn", 2131494460), 0);
                    this.visibleToast.show();
                    return;
                    bool = false;
                  }
                }
                catch (Exception paramAnonymousView)
                {
                  for (;;)
                  {
                    FileLog.e(paramAnonymousView);
                  }
                  this.visibleToast = Toast.makeText(LoginActivity.this.getParentActivity(), LocaleController.getString("SyncContactsOff", 2131494459), 0);
                  this.visibleToast.show();
                }
              }
            }
          });
        }
        localObject1 = new HashMap();
        try
        {
          paramContext = new BufferedReader(new InputStreamReader(getResources().getAssets().open("countries.txt")));
          for (;;)
          {
            localObject2 = paramContext.readLine();
            if (localObject2 == null) {
              break;
            }
            localObject2 = ((String)localObject2).split(";");
            this.countriesArray.add(0, localObject2[2]);
            this.countriesMap.put(localObject2[2], localObject2[0]);
            this.codesMap.put(localObject2[0], localObject2[2]);
            if (localObject2.length > 3) {
              this.phoneFormatMap.put(localObject2[0], localObject2[3]);
            }
            ((HashMap)localObject1).put(localObject2[1], localObject2[2]);
          }
          Collections.sort(this.countriesArray, new Comparator()
          {
            public int compare(String paramAnonymousString1, String paramAnonymousString2)
            {
              return paramAnonymousString1.compareTo(paramAnonymousString2);
            }
          });
        }
        catch (Exception paramContext)
        {
          FileLog.e(paramContext);
        }
      }
      for (;;)
      {
        paramContext = null;
        try
        {
          localObject2 = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
          this$1 = paramContext;
          if (localObject2 != null) {
            this$1 = ((TelephonyManager)localObject2).getSimCountryIso().toUpperCase();
          }
        }
        catch (Exception this$1)
        {
          for (;;)
          {
            FileLog.e(LoginActivity.this);
            this$1 = paramContext;
          }
          this.codeField.requestFocus();
        }
        if (LoginActivity.this != null)
        {
          this$1 = (String)((HashMap)localObject1).get(LoginActivity.this);
          if ((LoginActivity.this != null) && (this.countriesArray.indexOf(LoginActivity.this) != -1))
          {
            this.codeField.setText((CharSequence)this.countriesMap.get(LoginActivity.this));
            this.countryState = 0;
          }
        }
        if (this.codeField.length() == 0)
        {
          this.countryButton.setText(LocaleController.getString("ChooseCountry", 2131493246));
          this.phoneField.setHintText(null);
          this.countryState = 1;
        }
        if (this.codeField.length() == 0) {
          break label1333;
        }
        this.phoneField.requestFocus();
        this.phoneField.setSelection(this.phoneField.length());
        return;
        i = 3;
        break;
        label1306:
        i = 3;
        break label848;
        label1311:
        i = 3;
        break label881;
        paramContext.close();
      }
      label1333:
    }
    
    public void fillNumber()
    {
      for (;;)
      {
        int k;
        int m;
        try
        {
          Object localObject1 = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
          if ((((TelephonyManager)localObject1).getSimState() == 1) || (((TelephonyManager)localObject1).getPhoneType() == 0)) {
            break label587;
          }
          k = 1;
          m = 1;
          if (Build.VERSION.SDK_INT >= 23)
          {
            if (LoginActivity.this.getParentActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
              break label588;
            }
            i = 1;
            if (LoginActivity.this.getParentActivity().checkSelfPermission("android.permission.RECEIVE_SMS") != 0) {
              break label593;
            }
            j = 1;
            k = i;
            m = j;
            if (LoginActivity.this.checkShowPermissions)
            {
              k = i;
              m = j;
              if (i == 0)
              {
                k = i;
                m = j;
                if (j == 0)
                {
                  LoginActivity.this.permissionsShowItems.clear();
                  if (i == 0) {
                    LoginActivity.this.permissionsShowItems.add("android.permission.READ_PHONE_STATE");
                  }
                  if (j == 0)
                  {
                    LoginActivity.this.permissionsShowItems.add("android.permission.RECEIVE_SMS");
                    if (Build.VERSION.SDK_INT >= 23) {
                      LoginActivity.this.permissionsShowItems.add("android.permission.READ_SMS");
                    }
                  }
                  if (LoginActivity.this.permissionsShowItems.isEmpty()) {
                    break label587;
                  }
                  localObject1 = MessagesController.getGlobalMainSettings();
                  if ((((SharedPreferences)localObject1).getBoolean("firstloginshow", true)) || (LoginActivity.this.getParentActivity().shouldShowRequestPermissionRationale("android.permission.READ_PHONE_STATE")) || (LoginActivity.this.getParentActivity().shouldShowRequestPermissionRationale("android.permission.RECEIVE_SMS")))
                  {
                    ((SharedPreferences)localObject1).edit().putBoolean("firstloginshow", false).commit();
                    localObject1 = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
                    ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
                    ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AllowFillNumber", 2131492952));
                    LoginActivity.access$3102(LoginActivity.this, LoginActivity.this.showDialog(((AlertDialog.Builder)localObject1).create()));
                    return;
                  }
                  LoginActivity.this.getParentActivity().requestPermissions((String[])LoginActivity.this.permissionsShowItems.toArray(new String[LoginActivity.this.permissionsShowItems.size()]), 7);
                  return;
                }
              }
            }
          }
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
          return;
        }
        if ((!LoginActivity.this.newAccount) && ((k != 0) || (m != 0)))
        {
          String str1 = PhoneFormat.stripExceptNumbers(localException.getLine1Number());
          Object localObject2 = null;
          Object localObject3 = null;
          k = 0;
          if (!TextUtils.isEmpty(str1))
          {
            if (str1.length() > 4) {
              i = 4;
            }
            for (;;)
            {
              j = k;
              localObject2 = localObject3;
              if (i >= 1)
              {
                String str2 = str1.substring(0, i);
                if ((String)this.codesMap.get(str2) != null)
                {
                  j = 1;
                  localObject2 = str1.substring(i, str1.length());
                  this.codeField.setText(str2);
                }
              }
              else
              {
                if (j == 0)
                {
                  localObject2 = str1.substring(1, str1.length());
                  this.codeField.setText(str1.substring(0, 1));
                }
                if (localObject2 == null) {
                  break;
                }
                this.phoneField.requestFocus();
                this.phoneField.setText((CharSequence)localObject2);
                this.phoneField.setSelection(this.phoneField.length());
                return;
              }
              i -= 1;
            }
          }
        }
        label587:
        return;
        label588:
        int i = 0;
        continue;
        label593:
        int j = 0;
      }
    }
    
    public String getHeaderName()
    {
      return LocaleController.getString("YourPhone", 2131494672);
    }
    
    public void onCancelPressed()
    {
      this.nextPressed = false;
    }
    
    public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong)
    {
      if (this.ignoreSelection)
      {
        this.ignoreSelection = false;
        return;
      }
      this.ignoreOnTextChange = true;
      paramAdapterView = (String)this.countriesArray.get(paramInt);
      this.codeField.setText((CharSequence)this.countriesMap.get(paramAdapterView));
      this.ignoreOnTextChange = false;
    }
    
    public void onNextPressed()
    {
      if ((LoginActivity.this.getParentActivity() == null) || (this.nextPressed)) {}
      final Object localObject3;
      int j;
      int k;
      label87:
      label106:
      label125:
      label371:
      label376:
      label382:
      label388:
      label648:
      for (;;)
      {
        return;
        localObject3 = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
        int m;
        int n;
        int i1;
        Object localObject1;
        if ((((TelephonyManager)localObject3).getSimState() != 1) && (((TelephonyManager)localObject3).getPhoneType() != 0))
        {
          j = 1;
          i = 1;
          k = i;
          if (Build.VERSION.SDK_INT >= 23)
          {
            k = i;
            if (j != 0)
            {
              if (LoginActivity.this.getParentActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
                break label371;
              }
              i = 1;
              if (LoginActivity.this.getParentActivity().checkSelfPermission("android.permission.RECEIVE_SMS") != 0) {
                break label376;
              }
              m = 1;
              if (LoginActivity.this.getParentActivity().checkSelfPermission("android.permission.CALL_PHONE") != 0) {
                break label382;
              }
              n = 1;
              k = i;
              if (LoginActivity.this.checkPermissions)
              {
                LoginActivity.this.permissionsItems.clear();
                if (i == 0) {
                  LoginActivity.this.permissionsItems.add("android.permission.READ_PHONE_STATE");
                }
                if (m == 0)
                {
                  LoginActivity.this.permissionsItems.add("android.permission.RECEIVE_SMS");
                  if (Build.VERSION.SDK_INT >= 23) {
                    LoginActivity.this.permissionsItems.add("android.permission.READ_SMS");
                  }
                }
                if (n == 0)
                {
                  LoginActivity.this.permissionsItems.add("android.permission.CALL_PHONE");
                  LoginActivity.this.permissionsItems.add("android.permission.WRITE_CALL_LOG");
                  LoginActivity.this.permissionsItems.add("android.permission.READ_CALL_LOG");
                }
                i1 = 1;
                k = i;
                if (!LoginActivity.this.permissionsItems.isEmpty())
                {
                  localObject1 = MessagesController.getGlobalMainSettings();
                  if ((n != 0) || (i == 0)) {
                    break label388;
                  }
                  LoginActivity.this.getParentActivity().requestPermissions((String[])LoginActivity.this.permissionsItems.toArray(new String[LoginActivity.this.permissionsItems.size()]), 6);
                  k = i1;
                }
              }
            }
          }
        }
        for (;;)
        {
          if (k != 0) {
            break label648;
          }
          k = i;
          if (this.countryState != 1) {
            break label650;
          }
          LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("ChooseCountry", 2131493246));
          return;
          j = 0;
          break;
          i = 0;
          break label87;
          m = 0;
          break label106;
          n = 0;
          break label125;
          if ((((SharedPreferences)localObject1).getBoolean("firstlogin", true)) || (LoginActivity.this.getParentActivity().shouldShowRequestPermissionRationale("android.permission.READ_PHONE_STATE")) || (LoginActivity.this.getParentActivity().shouldShowRequestPermissionRationale("android.permission.RECEIVE_SMS")))
          {
            ((SharedPreferences)localObject1).edit().putBoolean("firstlogin", false).commit();
            localObject1 = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
            ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
            ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
            if (LoginActivity.this.permissionsItems.size() >= 2) {
              ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AllowReadCallAndSms", 2131492954));
            }
            for (;;)
            {
              LoginActivity.access$2202(LoginActivity.this, LoginActivity.this.showDialog(((AlertDialog.Builder)localObject1).create()));
              k = i1;
              break;
              if (m == 0) {
                ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AllowReadSms", 2131492955));
              } else {
                ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("AllowReadCall", 2131492953));
              }
            }
          }
          try
          {
            LoginActivity.this.getParentActivity().requestPermissions((String[])LoginActivity.this.permissionsItems.toArray(new String[LoginActivity.this.permissionsItems.size()]), 6);
            k = i1;
          }
          catch (Exception localException1)
          {
            k = 0;
          }
        }
      }
      label650:
      if (this.codeField.length() == 0)
      {
        LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidPhoneNumber", 2131493682));
        return;
      }
      Object localObject2 = PhoneFormat.stripExceptNumbers("" + this.codeField.getText() + this.phoneField.getText());
      final int i = 0;
      if (i < 3)
      {
        localObject4 = UserConfig.getInstance(i);
        if (!((UserConfig)localObject4).isClientActivated()) {}
        do
        {
          i += 1;
          break;
          localObject4 = ((UserConfig)localObject4).getCurrentUser().phone;
        } while ((!((String)localObject4).contains((CharSequence)localObject2)) && (!((String)localObject2).contains((CharSequence)localObject4)));
        localObject2 = new AlertDialog.Builder(LoginActivity.this.getParentActivity());
        ((AlertDialog.Builder)localObject2).setTitle(LocaleController.getString("AppName", 2131492981));
        ((AlertDialog.Builder)localObject2).setMessage(LocaleController.getString("AccountAlreadyLoggedIn", 2131492867));
        ((AlertDialog.Builder)localObject2).setPositiveButton(LocaleController.getString("AccountSwitch", 2131492869), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            if (UserConfig.selectedAccount != i) {
              ((LaunchActivity)LoginActivity.this.getParentActivity()).switchToAccount(i, false);
            }
            LoginActivity.this.finishFragment();
          }
        });
        ((AlertDialog.Builder)localObject2).setNegativeButton(LocaleController.getString("OK", 2131494028), null);
        LoginActivity.this.showDialog(((AlertDialog.Builder)localObject2).create());
        return;
      }
      ConnectionsManager.getInstance(LoginActivity.this.currentAccount).cleanup();
      final Object localObject4 = new TLRPC.TL_auth_sendCode();
      ((TLRPC.TL_auth_sendCode)localObject4).api_hash = BuildVars.APP_HASH;
      ((TLRPC.TL_auth_sendCode)localObject4).api_id = BuildVars.APP_ID;
      ((TLRPC.TL_auth_sendCode)localObject4).phone_number = ((String)localObject2);
      boolean bool;
      if ((j != 0) && (k != 0)) {
        bool = true;
      }
      for (;;)
      {
        ((TLRPC.TL_auth_sendCode)localObject4).allow_flashcall = bool;
        if (((TLRPC.TL_auth_sendCode)localObject4).allow_flashcall) {}
        try
        {
          localObject3 = ((TelephonyManager)localObject3).getLine1Number();
          if (!TextUtils.isEmpty((CharSequence)localObject3))
          {
            if (((String)localObject2).contains((CharSequence)localObject3)) {
              break label1272;
            }
            if (((String)localObject3).contains((CharSequence)localObject2))
            {
              break label1272;
              ((TLRPC.TL_auth_sendCode)localObject4).current_number = bool;
              if (!((TLRPC.TL_auth_sendCode)localObject4).current_number) {
                ((TLRPC.TL_auth_sendCode)localObject4).allow_flashcall = false;
              }
              localObject3 = new Bundle();
              ((Bundle)localObject3).putString("phone", "+" + this.codeField.getText() + this.phoneField.getText());
            }
          }
        }
        catch (Exception localException2)
        {
          for (;;)
          {
            try
            {
              ((Bundle)localObject3).putString("ephone", "+" + PhoneFormat.stripExceptNumbers(this.codeField.getText().toString()) + " " + PhoneFormat.stripExceptNumbers(this.phoneField.getText().toString()));
              ((Bundle)localObject3).putString("phoneFormated", (String)localObject2);
              this.nextPressed = true;
              i = ConnectionsManager.getInstance(LoginActivity.this.currentAccount).sendRequest((TLObject)localObject4, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      LoginActivity.PhoneView.access$2402(LoginActivity.PhoneView.this, false);
                      if (paramAnonymousTL_error == null) {
                        LoginActivity.this.fillNextCodeParams(LoginActivity.PhoneView.9.this.val$params, (TLRPC.TL_auth_sentCode)paramAnonymousTLObject);
                      }
                      for (;;)
                      {
                        LoginActivity.this.needHideProgress();
                        return;
                        if (paramAnonymousTL_error.text != null) {
                          if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_INVALID")) {
                            LoginActivity.this.needShowInvalidAlert(LoginActivity.PhoneView.9.this.val$req.phone_number, false);
                          } else if (paramAnonymousTL_error.text.contains("PHONE_PASSWORD_FLOOD")) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("FloodWait", 2131493543));
                          } else if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_FLOOD")) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("PhoneNumberFlood", 2131494155));
                          } else if (paramAnonymousTL_error.text.contains("PHONE_NUMBER_BANNED")) {
                            LoginActivity.this.needShowInvalidAlert(LoginActivity.PhoneView.9.this.val$req.phone_number, true);
                          } else if ((paramAnonymousTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramAnonymousTL_error.text.contains("PHONE_CODE_INVALID"))) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("InvalidCode", 2131493679));
                          } else if (paramAnonymousTL_error.text.contains("PHONE_CODE_EXPIRED")) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("CodeExpired", 2131493268));
                          } else if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT")) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), LocaleController.getString("FloodWait", 2131493543));
                          } else if (paramAnonymousTL_error.code != 64536) {
                            LoginActivity.this.needShowAlert(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
                          }
                        }
                      }
                    }
                  });
                }
              }, 27);
              LoginActivity.this.needShowProgress(i);
              return;
              bool = false;
              break;
              bool = false;
              continue;
              if (UserConfig.getActivatedAccountsCount() > 0)
              {
                ((TLRPC.TL_auth_sendCode)localObject4).allow_flashcall = false;
                continue;
                localException2 = localException2;
                ((TLRPC.TL_auth_sendCode)localObject4).allow_flashcall = false;
                FileLog.e(localException2);
                continue;
              }
              ((TLRPC.TL_auth_sendCode)localObject4).current_number = false;
            }
            catch (Exception localException3)
            {
              FileLog.e(localException3);
              localException2.putString("ephone", "+" + (String)localObject2);
              continue;
            }
            label1272:
            bool = true;
          }
        }
      }
    }
    
    public void onNothingSelected(AdapterView<?> paramAdapterView) {}
    
    public void onShow()
    {
      super.onShow();
      fillNumber();
      if (this.checkBoxCell != null) {
        this.checkBoxCell.setChecked(LoginActivity.this.syncContacts, false);
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (LoginActivity.PhoneView.this.phoneField != null)
          {
            if (LoginActivity.PhoneView.this.codeField.length() != 0)
            {
              AndroidUtilities.showKeyboard(LoginActivity.PhoneView.this.phoneField);
              LoginActivity.PhoneView.this.phoneField.requestFocus();
              LoginActivity.PhoneView.this.phoneField.setSelection(LoginActivity.PhoneView.this.phoneField.length());
            }
          }
          else {
            return;
          }
          AndroidUtilities.showKeyboard(LoginActivity.PhoneView.this.codeField);
          LoginActivity.PhoneView.this.codeField.requestFocus();
        }
      }, 100L);
    }
    
    public void restoreStateParams(Bundle paramBundle)
    {
      String str = paramBundle.getString("phoneview_code");
      if (str != null) {
        this.codeField.setText(str);
      }
      paramBundle = paramBundle.getString("phoneview_phone");
      if (paramBundle != null) {
        this.phoneField.setText(paramBundle);
      }
    }
    
    public void saveStateParams(Bundle paramBundle)
    {
      String str = this.codeField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("phoneview_code", str);
      }
      str = this.phoneField.getText().toString();
      if (str.length() != 0) {
        paramBundle.putString("phoneview_phone", str);
      }
    }
    
    public void selectCountry(String paramString)
    {
      Object localObject;
      if (this.countriesArray.indexOf(paramString) != -1)
      {
        this.ignoreOnTextChange = true;
        localObject = (String)this.countriesMap.get(paramString);
        this.codeField.setText((CharSequence)localObject);
        this.countryButton.setText(paramString);
        paramString = (String)this.phoneFormatMap.get(localObject);
        localObject = this.phoneField;
        if (paramString == null) {
          break label92;
        }
      }
      label92:
      for (paramString = paramString.replace('X', '–');; paramString = null)
      {
        ((HintEditText)localObject).setHintText(paramString);
        this.countryState = 0;
        this.ignoreOnTextChange = false;
        return;
      }
    }
  }
  
  private class ProgressView
    extends View
  {
    private Paint paint = new Paint();
    private Paint paint2 = new Paint();
    private float progress;
    
    public ProgressView(Context paramContext)
    {
      super();
      this.paint.setColor(Theme.getColor("login_progressInner"));
      this.paint2.setColor(Theme.getColor("login_progressOuter"));
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      int i = (int)(getMeasuredWidth() * this.progress);
      paramCanvas.drawRect(0.0F, 0.0F, i, getMeasuredHeight(), this.paint2);
      paramCanvas.drawRect(i, 0.0F, getMeasuredWidth(), getMeasuredHeight(), this.paint);
    }
    
    public void setProgress(float paramFloat)
    {
      this.progress = paramFloat;
      invalidate();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/LoginActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */