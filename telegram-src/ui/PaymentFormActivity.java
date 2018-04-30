package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.Cart.Builder;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.FullWalletRequest.Builder;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.LineItem.Builder;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.MaskedWalletRequest.Builder;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters.Builder;
import com.google.android.gms.wallet.Payments;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.Wallet.WalletOptions.Builder;
import com.google.android.gms.wallet.fragment.WalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams.Builder;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions.Builder;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.APIConnectionException;
import com.stripe.android.exception.APIException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.net.TokenParser;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputPaymentCredentials;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_getTmpPassword;
import org.telegram.tgnet.TLRPC.TL_account_noPassword;
import org.telegram.tgnet.TLRPC.TL_account_password;
import org.telegram.tgnet.TLRPC.TL_account_passwordInputSettings;
import org.telegram.tgnet.TLRPC.TL_account_tmpPassword;
import org.telegram.tgnet.TLRPC.TL_account_updatePasswordSettings;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_dataJSON;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPaymentCredentials;
import org.telegram.tgnet.TLRPC.TL_inputPaymentCredentialsAndroidPay;
import org.telegram.tgnet.TLRPC.TL_inputPaymentCredentialsSaved;
import org.telegram.tgnet.TLRPC.TL_invoice;
import org.telegram.tgnet.TLRPC.TL_labeledPrice;
import org.telegram.tgnet.TLRPC.TL_paymentRequestedInfo;
import org.telegram.tgnet.TLRPC.TL_paymentSavedCredentialsCard;
import org.telegram.tgnet.TLRPC.TL_payments_clearSavedInfo;
import org.telegram.tgnet.TLRPC.TL_payments_paymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_paymentReceipt;
import org.telegram.tgnet.TLRPC.TL_payments_paymentResult;
import org.telegram.tgnet.TLRPC.TL_payments_paymentVerficationNeeded;
import org.telegram.tgnet.TLRPC.TL_payments_sendPaymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_validateRequestedInfo;
import org.telegram.tgnet.TLRPC.TL_payments_validatedRequestedInfo;
import org.telegram.tgnet.TLRPC.TL_postAddress;
import org.telegram.tgnet.TLRPC.TL_shippingOption;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.account_Password;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PaymentInfoCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextPriceCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintEditText;

public class PaymentFormActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int FIELDS_COUNT_ADDRESS = 10;
  private static final int FIELDS_COUNT_CARD = 6;
  private static final int FIELDS_COUNT_PASSWORD = 3;
  private static final int FIELDS_COUNT_SAVEDCARD = 2;
  private static final int FIELD_CARD = 0;
  private static final int FIELD_CARDNAME = 2;
  private static final int FIELD_CARD_COUNTRY = 4;
  private static final int FIELD_CARD_POSTCODE = 5;
  private static final int FIELD_CITY = 2;
  private static final int FIELD_COUNTRY = 4;
  private static final int FIELD_CVV = 3;
  private static final int FIELD_EMAIL = 7;
  private static final int FIELD_ENTERPASSWORD = 0;
  private static final int FIELD_ENTERPASSWORDEMAIL = 2;
  private static final int FIELD_EXPIRE_DATE = 1;
  private static final int FIELD_NAME = 6;
  private static final int FIELD_PHONE = 9;
  private static final int FIELD_PHONECODE = 8;
  private static final int FIELD_POSTCODE = 5;
  private static final int FIELD_REENTERPASSWORD = 1;
  private static final int FIELD_SAVEDCARD = 0;
  private static final int FIELD_SAVEDPASSWORD = 1;
  private static final int FIELD_STATE = 3;
  private static final int FIELD_STREET1 = 0;
  private static final int FIELD_STREET2 = 1;
  private static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;
  private static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
  private static final int done_button = 1;
  private static final int fragment_container_id = 4000;
  private int androidPayBackgroundColor;
  private boolean androidPayBlackTheme;
  private FrameLayout androidPayContainer;
  private TLRPC.TL_inputPaymentCredentialsAndroidPay androidPayCredentials;
  private String androidPayPublicKey;
  private TLRPC.User botUser;
  private TextInfoPrivacyCell[] bottomCell = new TextInfoPrivacyCell[3];
  private FrameLayout bottomLayout;
  private boolean canceled;
  private String cardName;
  private TextCheckCell checkCell1;
  private HashMap<String, String> codesMap = new HashMap();
  private ArrayList<String> countriesArray = new ArrayList();
  private HashMap<String, String> countriesMap = new HashMap();
  private String countryName;
  private String currentBotName;
  private String currentItemName;
  private TLRPC.account_Password currentPassword;
  private int currentStep;
  private PaymentFormActivityDelegate delegate;
  private TextDetailSettingsCell[] detailSettingsCell = new TextDetailSettingsCell[7];
  private ArrayList<View> dividers = new ArrayList();
  private ActionBarMenuItem doneItem;
  private AnimatorSet doneItemAnimation;
  private boolean donePressed;
  private GoogleApiClient googleApiClient;
  private HeaderCell[] headerCell = new HeaderCell[3];
  private boolean ignoreOnCardChange;
  private boolean ignoreOnPhoneChange;
  private boolean ignoreOnTextChange;
  private EditTextBoldCursor[] inputFields;
  private boolean isWebView;
  private LinearLayout linearLayout2;
  private boolean loadingPasswordInfo;
  private MessageObject messageObject;
  private boolean need_card_country;
  private boolean need_card_name;
  private boolean need_card_postcode;
  private PaymentFormActivity passwordFragment;
  private boolean passwordOk;
  private TextView payTextView;
  private TLRPC.TL_payments_paymentForm paymentForm;
  private PaymentInfoCell paymentInfoCell;
  private String paymentJson;
  private HashMap<String, String> phoneFormatMap = new HashMap();
  private ContextProgressView progressView;
  private ContextProgressView progressViewButton;
  private RadioCell[] radioCells;
  private TLRPC.TL_payments_validatedRequestedInfo requestedInfo;
  private boolean saveCardInfo;
  private boolean saveShippingInfo;
  private ScrollView scrollView;
  private ShadowSectionCell[] sectionCell = new ShadowSectionCell[3];
  private TextSettingsCell settingsCell1;
  private TLRPC.TL_shippingOption shippingOption;
  private Runnable shortPollRunnable;
  private String stripeApiKey;
  private TextView textView;
  private String totalPriceDecimal;
  private TLRPC.TL_payments_validateRequestedInfo validateRequest;
  private boolean waitingForEmail;
  private WebView webView;
  private boolean webviewLoading;
  
  public PaymentFormActivity(MessageObject paramMessageObject, TLRPC.TL_payments_paymentReceipt paramTL_payments_paymentReceipt)
  {
    this.currentStep = 5;
    this.paymentForm = new TLRPC.TL_payments_paymentForm();
    this.paymentForm.bot_id = paramTL_payments_paymentReceipt.bot_id;
    this.paymentForm.invoice = paramTL_payments_paymentReceipt.invoice;
    this.paymentForm.provider_id = paramTL_payments_paymentReceipt.provider_id;
    this.paymentForm.users = paramTL_payments_paymentReceipt.users;
    this.shippingOption = paramTL_payments_paymentReceipt.shipping;
    this.messageObject = paramMessageObject;
    this.botUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramTL_payments_paymentReceipt.bot_id));
    if (this.botUser != null) {}
    for (this.currentBotName = this.botUser.first_name;; this.currentBotName = "")
    {
      this.currentItemName = paramMessageObject.messageOwner.media.title;
      if (paramTL_payments_paymentReceipt.info != null)
      {
        this.validateRequest = new TLRPC.TL_payments_validateRequestedInfo();
        this.validateRequest.info = paramTL_payments_paymentReceipt.info;
      }
      this.cardName = paramTL_payments_paymentReceipt.credentials_title;
      return;
    }
  }
  
  public PaymentFormActivity(TLRPC.TL_payments_paymentForm paramTL_payments_paymentForm, MessageObject paramMessageObject)
  {
    int i;
    if ((paramTL_payments_paymentForm.invoice.shipping_address_requested) || (paramTL_payments_paymentForm.invoice.email_requested) || (paramTL_payments_paymentForm.invoice.name_requested) || (paramTL_payments_paymentForm.invoice.phone_requested)) {
      i = 0;
    }
    for (;;)
    {
      init(paramTL_payments_paymentForm, paramMessageObject, i, null, null, null, null, null, false, null);
      return;
      if (paramTL_payments_paymentForm.saved_credentials != null)
      {
        if ((UserConfig.getInstance(this.currentAccount).tmpPassword != null) && (UserConfig.getInstance(this.currentAccount).tmpPassword.valid_until < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 60))
        {
          UserConfig.getInstance(this.currentAccount).tmpPassword = null;
          UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        if (UserConfig.getInstance(this.currentAccount).tmpPassword != null) {
          i = 4;
        } else {
          i = 3;
        }
      }
      else
      {
        i = 2;
      }
    }
  }
  
  private PaymentFormActivity(TLRPC.TL_payments_paymentForm paramTL_payments_paymentForm, MessageObject paramMessageObject, int paramInt, TLRPC.TL_payments_validatedRequestedInfo paramTL_payments_validatedRequestedInfo, TLRPC.TL_shippingOption paramTL_shippingOption, String paramString1, String paramString2, TLRPC.TL_payments_validateRequestedInfo paramTL_payments_validateRequestedInfo, boolean paramBoolean, TLRPC.TL_inputPaymentCredentialsAndroidPay paramTL_inputPaymentCredentialsAndroidPay)
  {
    init(paramTL_payments_paymentForm, paramMessageObject, paramInt, paramTL_payments_validatedRequestedInfo, paramTL_shippingOption, paramString1, paramString2, paramTL_payments_validateRequestedInfo, paramBoolean, paramTL_inputPaymentCredentialsAndroidPay);
  }
  
  private void checkPassword()
  {
    if ((UserConfig.getInstance(this.currentAccount).tmpPassword != null) && (UserConfig.getInstance(this.currentAccount).tmpPassword.valid_until < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 60))
    {
      UserConfig.getInstance(this.currentAccount).tmpPassword = null;
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
    }
    if (UserConfig.getInstance(this.currentAccount).tmpPassword != null)
    {
      sendData();
      return;
    }
    if (this.inputFields[1].length() == 0)
    {
      localObject = (Vibrator)ApplicationLoader.applicationContext.getSystemService("vibrator");
      if (localObject != null) {
        ((Vibrator)localObject).vibrate(200L);
      }
      AndroidUtilities.shakeView(this.inputFields[1], 2.0F, 0);
      return;
    }
    final Object localObject = this.inputFields[1].getText().toString();
    showEditDoneProgress(true, true);
    setDonePressed(true);
    final TLRPC.TL_account_getPassword localTL_account_getPassword = new TLRPC.TL_account_getPassword();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_getPassword, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (paramAnonymousTL_error == null)
            {
              if ((paramAnonymousTLObject instanceof TLRPC.TL_account_noPassword))
              {
                PaymentFormActivity.access$3002(PaymentFormActivity.this, false);
                PaymentFormActivity.this.goToNextStep();
                return;
              }
              TLRPC.TL_account_password localTL_account_password = (TLRPC.TL_account_password)paramAnonymousTLObject;
              final Object localObject = null;
              try
              {
                arrayOfByte = PaymentFormActivity.36.this.val$password.getBytes("UTF-8");
                localObject = arrayOfByte;
              }
              catch (Exception localException)
              {
                for (;;)
                {
                  byte[] arrayOfByte;
                  FileLog.e(localException);
                }
              }
              arrayOfByte = new byte[localTL_account_password.current_salt.length * 2 + localObject.length];
              System.arraycopy(localTL_account_password.current_salt, 0, arrayOfByte, 0, localTL_account_password.current_salt.length);
              System.arraycopy(localObject, 0, arrayOfByte, localTL_account_password.current_salt.length, localObject.length);
              System.arraycopy(localTL_account_password.current_salt, 0, arrayOfByte, arrayOfByte.length - localTL_account_password.current_salt.length, localTL_account_password.current_salt.length);
              localObject = new TLRPC.TL_account_getTmpPassword();
              ((TLRPC.TL_account_getTmpPassword)localObject).password_hash = Utilities.computeSHA256(arrayOfByte, 0, arrayOfByte.length);
              ((TLRPC.TL_account_getTmpPassword)localObject).period = 1800;
              ConnectionsManager.getInstance(PaymentFormActivity.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymous3TLObject, final TLRPC.TL_error paramAnonymous3TL_error)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      PaymentFormActivity.this.showEditDoneProgress(true, false);
                      PaymentFormActivity.this.setDonePressed(false);
                      if (paramAnonymous3TLObject != null)
                      {
                        PaymentFormActivity.access$3002(PaymentFormActivity.this, true);
                        UserConfig.getInstance(PaymentFormActivity.this.currentAccount).tmpPassword = ((TLRPC.TL_account_tmpPassword)paramAnonymous3TLObject);
                        UserConfig.getInstance(PaymentFormActivity.this.currentAccount).saveConfig(false);
                        PaymentFormActivity.this.goToNextStep();
                        return;
                      }
                      if (paramAnonymous3TL_error.text.equals("PASSWORD_HASH_INVALID"))
                      {
                        Vibrator localVibrator = (Vibrator)ApplicationLoader.applicationContext.getSystemService("vibrator");
                        if (localVibrator != null) {
                          localVibrator.vibrate(200L);
                        }
                        AndroidUtilities.shakeView(PaymentFormActivity.this.inputFields[1], 2.0F, 0);
                        PaymentFormActivity.this.inputFields[1].setText("");
                        return;
                      }
                      AlertsCreator.processError(PaymentFormActivity.this.currentAccount, paramAnonymous3TL_error, PaymentFormActivity.this, PaymentFormActivity.36.1.1.this.val$req, new Object[0]);
                    }
                  });
                }
              }, 2);
              return;
            }
            AlertsCreator.processError(PaymentFormActivity.this.currentAccount, paramAnonymousTL_error, PaymentFormActivity.this, PaymentFormActivity.36.this.val$req, new Object[0]);
            PaymentFormActivity.this.showEditDoneProgress(true, false);
            PaymentFormActivity.this.setDonePressed(false);
          }
        });
      }
    }, 2);
  }
  
  private TLRPC.TL_paymentRequestedInfo getRequestInfo()
  {
    TLRPC.TL_paymentRequestedInfo localTL_paymentRequestedInfo = new TLRPC.TL_paymentRequestedInfo();
    if (this.paymentForm.invoice.name_requested)
    {
      localTL_paymentRequestedInfo.name = this.inputFields[6].getText().toString();
      localTL_paymentRequestedInfo.flags |= 0x1;
    }
    if (this.paymentForm.invoice.phone_requested)
    {
      localTL_paymentRequestedInfo.phone = ("+" + this.inputFields[8].getText().toString() + this.inputFields[9].getText().toString());
      localTL_paymentRequestedInfo.flags |= 0x2;
    }
    if (this.paymentForm.invoice.email_requested)
    {
      localTL_paymentRequestedInfo.email = this.inputFields[7].getText().toString().trim();
      localTL_paymentRequestedInfo.flags |= 0x4;
    }
    TLRPC.TL_postAddress localTL_postAddress;
    if (this.paymentForm.invoice.shipping_address_requested)
    {
      localTL_paymentRequestedInfo.shipping_address = new TLRPC.TL_postAddress();
      localTL_paymentRequestedInfo.shipping_address.street_line1 = this.inputFields[0].getText().toString();
      localTL_paymentRequestedInfo.shipping_address.street_line2 = this.inputFields[1].getText().toString();
      localTL_paymentRequestedInfo.shipping_address.city = this.inputFields[2].getText().toString();
      localTL_paymentRequestedInfo.shipping_address.state = this.inputFields[3].getText().toString();
      localTL_postAddress = localTL_paymentRequestedInfo.shipping_address;
      if (this.countryName == null) {
        break label320;
      }
    }
    label320:
    for (String str = this.countryName;; str = "")
    {
      localTL_postAddress.country_iso2 = str;
      localTL_paymentRequestedInfo.shipping_address.post_code = this.inputFields[5].getText().toString();
      localTL_paymentRequestedInfo.flags |= 0x8;
      return localTL_paymentRequestedInfo;
    }
  }
  
  private String getTotalPriceDecimalString(ArrayList<TLRPC.TL_labeledPrice> paramArrayList)
  {
    long l = 0L;
    int i = 0;
    while (i < paramArrayList.size())
    {
      l += ((TLRPC.TL_labeledPrice)paramArrayList.get(i)).amount;
      i += 1;
    }
    return LocaleController.getInstance().formatCurrencyDecimalString(l, this.paymentForm.invoice.currency, false);
  }
  
  private String getTotalPriceString(ArrayList<TLRPC.TL_labeledPrice> paramArrayList)
  {
    long l = 0L;
    int i = 0;
    while (i < paramArrayList.size())
    {
      l += ((TLRPC.TL_labeledPrice)paramArrayList.get(i)).amount;
      i += 1;
    }
    return LocaleController.getInstance().formatCurrencyString(l, this.paymentForm.invoice.currency);
  }
  
  private void goToNextStep()
  {
    int i;
    if (this.currentStep == 0) {
      if (this.paymentForm.invoice.flexible)
      {
        i = 1;
        presentFragment(new PaymentFormActivity(this.paymentForm, this.messageObject, i, this.requestedInfo, null, null, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials), this.isWebView);
      }
    }
    label620:
    do
    {
      return;
      if (this.paymentForm.saved_credentials != null)
      {
        if ((UserConfig.getInstance(this.currentAccount).tmpPassword != null) && (UserConfig.getInstance(this.currentAccount).tmpPassword.valid_until < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 60))
        {
          UserConfig.getInstance(this.currentAccount).tmpPassword = null;
          UserConfig.getInstance(this.currentAccount).saveConfig(false);
        }
        if (UserConfig.getInstance(this.currentAccount).tmpPassword != null)
        {
          i = 4;
          break;
        }
        i = 3;
        break;
      }
      i = 2;
      break;
      if (this.currentStep == 1)
      {
        if (this.paymentForm.saved_credentials != null)
        {
          if ((UserConfig.getInstance(this.currentAccount).tmpPassword != null) && (UserConfig.getInstance(this.currentAccount).tmpPassword.valid_until < ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 60))
          {
            UserConfig.getInstance(this.currentAccount).tmpPassword = null;
            UserConfig.getInstance(this.currentAccount).saveConfig(false);
          }
          if (UserConfig.getInstance(this.currentAccount).tmpPassword != null) {
            i = 4;
          }
        }
        for (;;)
        {
          presentFragment(new PaymentFormActivity(this.paymentForm, this.messageObject, i, this.requestedInfo, this.shippingOption, null, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials), this.isWebView);
          return;
          i = 3;
          continue;
          i = 2;
        }
      }
      if (this.currentStep == 2)
      {
        if ((this.paymentForm.password_missing) && (this.saveCardInfo))
        {
          this.passwordFragment = new PaymentFormActivity(this.paymentForm, this.messageObject, 6, this.requestedInfo, this.shippingOption, this.paymentJson, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials);
          this.passwordFragment.setCurrentPassword(this.currentPassword);
          this.passwordFragment.setDelegate(new PaymentFormActivityDelegate()
          {
            public void currentPasswordUpdated(TLRPC.account_Password paramAnonymousaccount_Password)
            {
              PaymentFormActivity.access$4302(PaymentFormActivity.this, paramAnonymousaccount_Password);
            }
            
            public boolean didSelectNewCard(String paramAnonymousString1, String paramAnonymousString2, boolean paramAnonymousBoolean, TLRPC.TL_inputPaymentCredentialsAndroidPay paramAnonymousTL_inputPaymentCredentialsAndroidPay)
            {
              if (PaymentFormActivity.this.delegate != null) {
                PaymentFormActivity.this.delegate.didSelectNewCard(paramAnonymousString1, paramAnonymousString2, paramAnonymousBoolean, paramAnonymousTL_inputPaymentCredentialsAndroidPay);
              }
              if (PaymentFormActivity.this.isWebView) {
                PaymentFormActivity.this.removeSelfFromStack();
              }
              return PaymentFormActivity.this.delegate != null;
            }
            
            public void onFragmentDestroyed()
            {
              PaymentFormActivity.access$4402(PaymentFormActivity.this, null);
            }
          });
          presentFragment(this.passwordFragment, this.isWebView);
          return;
        }
        if (this.delegate != null)
        {
          this.delegate.didSelectNewCard(this.paymentJson, this.cardName, this.saveCardInfo, this.androidPayCredentials);
          finishFragment();
          return;
        }
        presentFragment(new PaymentFormActivity(this.paymentForm, this.messageObject, 4, this.requestedInfo, this.shippingOption, this.paymentJson, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials), this.isWebView);
        return;
      }
      if (this.currentStep == 3)
      {
        PaymentFormActivity localPaymentFormActivity;
        if (this.passwordOk)
        {
          i = 4;
          localPaymentFormActivity = new PaymentFormActivity(this.paymentForm, this.messageObject, i, this.requestedInfo, this.shippingOption, this.paymentJson, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials);
          if (this.passwordOk) {
            break label620;
          }
        }
        for (boolean bool = true;; bool = false)
        {
          presentFragment(localPaymentFormActivity, bool);
          return;
          i = 2;
          break;
        }
      }
      if (this.currentStep == 4)
      {
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.paymentFinished, new Object[0]);
        finishFragment();
        return;
      }
    } while (this.currentStep != 6);
    if (!this.delegate.didSelectNewCard(this.paymentJson, this.cardName, this.saveCardInfo, this.androidPayCredentials))
    {
      presentFragment(new PaymentFormActivity(this.paymentForm, this.messageObject, 4, this.requestedInfo, this.shippingOption, this.paymentJson, this.cardName, this.validateRequest, this.saveCardInfo, this.androidPayCredentials), true);
      return;
    }
    finishFragment();
  }
  
  private void init(TLRPC.TL_payments_paymentForm paramTL_payments_paymentForm, MessageObject paramMessageObject, int paramInt, TLRPC.TL_payments_validatedRequestedInfo paramTL_payments_validatedRequestedInfo, TLRPC.TL_shippingOption paramTL_shippingOption, String paramString1, String paramString2, TLRPC.TL_payments_validateRequestedInfo paramTL_payments_validateRequestedInfo, boolean paramBoolean, TLRPC.TL_inputPaymentCredentialsAndroidPay paramTL_inputPaymentCredentialsAndroidPay)
  {
    boolean bool2 = true;
    this.currentStep = paramInt;
    this.paymentJson = paramString1;
    this.androidPayCredentials = paramTL_inputPaymentCredentialsAndroidPay;
    this.requestedInfo = paramTL_payments_validatedRequestedInfo;
    this.paymentForm = paramTL_payments_paymentForm;
    this.shippingOption = paramTL_shippingOption;
    this.messageObject = paramMessageObject;
    this.saveCardInfo = paramBoolean;
    boolean bool1;
    if (!"stripe".equals(this.paymentForm.native_provider))
    {
      bool1 = true;
      this.isWebView = bool1;
      this.botUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramTL_payments_paymentForm.bot_id));
      if (this.botUser == null) {
        break label178;
      }
    }
    label178:
    for (this.currentBotName = this.botUser.first_name;; this.currentBotName = "")
    {
      this.currentItemName = paramMessageObject.messageOwner.media.title;
      this.validateRequest = paramTL_payments_validateRequestedInfo;
      this.saveShippingInfo = true;
      if (!paramBoolean) {
        break label188;
      }
      this.saveCardInfo = paramBoolean;
      if (paramString2 != null) {
        break label217;
      }
      if (paramTL_payments_paymentForm.saved_credentials != null) {
        this.cardName = paramTL_payments_paymentForm.saved_credentials.title;
      }
      return;
      bool1 = false;
      break;
    }
    label188:
    if (this.paymentForm.saved_credentials != null) {}
    for (paramBoolean = bool2;; paramBoolean = false)
    {
      this.saveCardInfo = paramBoolean;
      break;
    }
    label217:
    this.cardName = paramString2;
  }
  
  private void initAndroidPay(Context paramContext)
  {
    if (Build.VERSION.SDK_INT >= 0) {
      return;
    }
    paramContext = new GoogleApiClient.Builder(paramContext).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
    {
      public void onConnected(Bundle paramAnonymousBundle) {}
      
      public void onConnectionSuspended(int paramAnonymousInt) {}
    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
    {
      public void onConnectionFailed(ConnectionResult paramAnonymousConnectionResult) {}
    });
    Api localApi = Wallet.API;
    Wallet.WalletOptions.Builder localBuilder = new Wallet.WalletOptions.Builder();
    if (this.paymentForm.invoice.test) {}
    for (int i = 3;; i = 1)
    {
      this.googleApiClient = paramContext.addApi(localApi, localBuilder.setEnvironment(i).setTheme(1).build()).build();
      Wallet.Payments.isReadyToPay(this.googleApiClient).setResultCallback(new ResultCallback()
      {
        public void onResult(BooleanResult paramAnonymousBooleanResult)
        {
          if ((paramAnonymousBooleanResult.getStatus().isSuccess()) && (paramAnonymousBooleanResult.getValue())) {
            PaymentFormActivity.this.showAndroidPay();
          }
        }
      });
      this.googleApiClient.connect();
      return;
    }
  }
  
  private void loadPasswordInfo()
  {
    if (this.loadingPasswordInfo) {
      return;
    }
    this.loadingPasswordInfo = true;
    TLRPC.TL_account_getPassword localTL_account_getPassword = new TLRPC.TL_account_getPassword();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_getPassword, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            PaymentFormActivity.access$4202(PaymentFormActivity.this, false);
            if (paramAnonymousTL_error == null)
            {
              PaymentFormActivity.access$4302(PaymentFormActivity.this, (TLRPC.account_Password)paramAnonymousTLObject);
              if ((PaymentFormActivity.this.paymentForm != null) && ((PaymentFormActivity.this.currentPassword instanceof TLRPC.TL_account_password)))
              {
                PaymentFormActivity.this.paymentForm.password_missing = false;
                PaymentFormActivity.this.paymentForm.can_save_credentials = true;
                PaymentFormActivity.this.updateSavePaymentField();
              }
              byte[] arrayOfByte = new byte[PaymentFormActivity.this.currentPassword.new_salt.length + 8];
              Utilities.random.nextBytes(arrayOfByte);
              System.arraycopy(PaymentFormActivity.this.currentPassword.new_salt, 0, arrayOfByte, 0, PaymentFormActivity.this.currentPassword.new_salt.length);
              PaymentFormActivity.this.currentPassword.new_salt = arrayOfByte;
              if (PaymentFormActivity.this.passwordFragment != null) {
                PaymentFormActivity.this.passwordFragment.setCurrentPassword(PaymentFormActivity.this.currentPassword);
              }
            }
            if (((paramAnonymousTLObject instanceof TLRPC.TL_account_noPassword)) && (PaymentFormActivity.this.shortPollRunnable == null))
            {
              PaymentFormActivity.access$4602(PaymentFormActivity.this, new Runnable()
              {
                public void run()
                {
                  if (PaymentFormActivity.this.shortPollRunnable == null) {
                    return;
                  }
                  PaymentFormActivity.this.loadPasswordInfo();
                  PaymentFormActivity.access$4602(PaymentFormActivity.this, null);
                }
              });
              AndroidUtilities.runOnUIThread(PaymentFormActivity.this.shortPollRunnable, 5000L);
            }
          }
        });
      }
    }, 10);
  }
  
  private boolean sendCardData()
  {
    Object localObject2 = this.inputFields[1].getText().toString().split("/");
    Object localObject1;
    if (localObject2.length == 2) {
      localObject1 = Utilities.parseInt(localObject2[0]);
    }
    for (localObject2 = Utilities.parseInt(localObject2[1]);; localObject2 = null)
    {
      localObject1 = new Card(this.inputFields[0].getText().toString(), (Integer)localObject1, (Integer)localObject2, this.inputFields[3].getText().toString(), this.inputFields[2].getText().toString(), null, null, null, null, this.inputFields[5].getText().toString(), this.inputFields[4].getText().toString(), null);
      this.cardName = (((Card)localObject1).getType() + " *" + ((Card)localObject1).getLast4());
      if (((Card)localObject1).validateNumber()) {
        break;
      }
      shakeField(0);
      return false;
      localObject1 = null;
    }
    if ((!((Card)localObject1).validateExpMonth()) || (!((Card)localObject1).validateExpYear()) || (!((Card)localObject1).validateExpiryDate()))
    {
      shakeField(1);
      return false;
    }
    if ((this.need_card_name) && (this.inputFields[2].length() == 0))
    {
      shakeField(2);
      return false;
    }
    if (!((Card)localObject1).validateCVC())
    {
      shakeField(3);
      return false;
    }
    if ((this.need_card_country) && (this.inputFields[4].length() == 0))
    {
      shakeField(4);
      return false;
    }
    if ((this.need_card_postcode) && (this.inputFields[5].length() == 0))
    {
      shakeField(5);
      return false;
    }
    showEditDoneProgress(true, true);
    try
    {
      new Stripe(this.stripeApiKey).createToken((Card)localObject1, new TokenCallback()
      {
        public void onError(Exception paramAnonymousException)
        {
          if (PaymentFormActivity.this.canceled) {
            return;
          }
          PaymentFormActivity.this.showEditDoneProgress(true, false);
          PaymentFormActivity.this.setDonePressed(false);
          if (((paramAnonymousException instanceof APIConnectionException)) || ((paramAnonymousException instanceof APIException)))
          {
            AlertsCreator.showSimpleToast(PaymentFormActivity.this, LocaleController.getString("PaymentConnectionFailed", 2131494099));
            return;
          }
          AlertsCreator.showSimpleToast(PaymentFormActivity.this, paramAnonymousException.getMessage());
        }
        
        public void onSuccess(Token paramAnonymousToken)
        {
          if (PaymentFormActivity.this.canceled) {
            return;
          }
          PaymentFormActivity.access$002(PaymentFormActivity.this, String.format(Locale.US, "{\"type\":\"%1$s\", \"id\":\"%2$s\"}", new Object[] { paramAnonymousToken.getType(), paramAnonymousToken.getId() }));
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              PaymentFormActivity.this.goToNextStep();
              PaymentFormActivity.this.showEditDoneProgress(true, false);
              PaymentFormActivity.this.setDonePressed(false);
            }
          });
        }
      });
      return true;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  private void sendData()
  {
    if (this.canceled) {
      return;
    }
    showEditDoneProgress(false, true);
    final TLRPC.TL_payments_sendPaymentForm localTL_payments_sendPaymentForm = new TLRPC.TL_payments_sendPaymentForm();
    localTL_payments_sendPaymentForm.msg_id = this.messageObject.getId();
    if ((UserConfig.getInstance(this.currentAccount).tmpPassword != null) && (this.paymentForm.saved_credentials != null))
    {
      localTL_payments_sendPaymentForm.credentials = new TLRPC.TL_inputPaymentCredentialsSaved();
      localTL_payments_sendPaymentForm.credentials.id = this.paymentForm.saved_credentials.id;
      localTL_payments_sendPaymentForm.credentials.tmp_password = UserConfig.getInstance(this.currentAccount).tmpPassword.tmp_password;
    }
    for (;;)
    {
      if ((this.requestedInfo != null) && (this.requestedInfo.id != null))
      {
        localTL_payments_sendPaymentForm.requested_info_id = this.requestedInfo.id;
        localTL_payments_sendPaymentForm.flags |= 0x1;
      }
      if (this.shippingOption != null)
      {
        localTL_payments_sendPaymentForm.shipping_option_id = this.shippingOption.id;
        localTL_payments_sendPaymentForm.flags |= 0x2;
      }
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_payments_sendPaymentForm, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null)
          {
            if ((paramAnonymousTLObject instanceof TLRPC.TL_payments_paymentResult))
            {
              MessagesController.getInstance(PaymentFormActivity.this.currentAccount).processUpdates(((TLRPC.TL_payments_paymentResult)paramAnonymousTLObject).updates, false);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  PaymentFormActivity.this.goToNextStep();
                }
              });
            }
            while (!(paramAnonymousTLObject instanceof TLRPC.TL_payments_paymentVerficationNeeded)) {
              return;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(PaymentFormActivity.this.currentAccount).postNotificationName(NotificationCenter.paymentFinished, new Object[0]);
                PaymentFormActivity.this.setDonePressed(false);
                PaymentFormActivity.this.webView.setVisibility(0);
                PaymentFormActivity.access$2402(PaymentFormActivity.this, true);
                PaymentFormActivity.this.showEditDoneProgress(true, true);
                PaymentFormActivity.this.progressView.setVisibility(0);
                PaymentFormActivity.this.doneItem.setEnabled(false);
                PaymentFormActivity.this.doneItem.getImageView().setVisibility(4);
                PaymentFormActivity.this.webView.loadUrl(((TLRPC.TL_payments_paymentVerficationNeeded)paramAnonymousTLObject).url);
              }
            });
            return;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              AlertsCreator.processError(PaymentFormActivity.this.currentAccount, paramAnonymousTL_error, PaymentFormActivity.this, PaymentFormActivity.35.this.val$req, new Object[0]);
              PaymentFormActivity.this.setDonePressed(false);
              PaymentFormActivity.this.showEditDoneProgress(false, false);
            }
          });
        }
      }, 2);
      return;
      if (this.androidPayCredentials != null)
      {
        localTL_payments_sendPaymentForm.credentials = this.androidPayCredentials;
      }
      else
      {
        localTL_payments_sendPaymentForm.credentials = new TLRPC.TL_inputPaymentCredentials();
        localTL_payments_sendPaymentForm.credentials.save = this.saveCardInfo;
        localTL_payments_sendPaymentForm.credentials.data = new TLRPC.TL_dataJSON();
        localTL_payments_sendPaymentForm.credentials.data.data = this.paymentJson;
      }
    }
  }
  
  private void sendForm()
  {
    if (this.canceled) {
      return;
    }
    showEditDoneProgress(true, true);
    this.validateRequest = new TLRPC.TL_payments_validateRequestedInfo();
    this.validateRequest.save = this.saveShippingInfo;
    this.validateRequest.msg_id = this.messageObject.getId();
    this.validateRequest.info = new TLRPC.TL_paymentRequestedInfo();
    if (this.paymentForm.invoice.name_requested)
    {
      this.validateRequest.info.name = this.inputFields[6].getText().toString();
      localObject = this.validateRequest.info;
      ((TLRPC.TL_paymentRequestedInfo)localObject).flags |= 0x1;
    }
    if (this.paymentForm.invoice.phone_requested)
    {
      this.validateRequest.info.phone = ("+" + this.inputFields[8].getText().toString() + this.inputFields[9].getText().toString());
      localObject = this.validateRequest.info;
      ((TLRPC.TL_paymentRequestedInfo)localObject).flags |= 0x2;
    }
    if (this.paymentForm.invoice.email_requested)
    {
      this.validateRequest.info.email = this.inputFields[7].getText().toString().trim();
      localObject = this.validateRequest.info;
      ((TLRPC.TL_paymentRequestedInfo)localObject).flags |= 0x4;
    }
    TLRPC.TL_postAddress localTL_postAddress;
    if (this.paymentForm.invoice.shipping_address_requested)
    {
      this.validateRequest.info.shipping_address = new TLRPC.TL_postAddress();
      this.validateRequest.info.shipping_address.street_line1 = this.inputFields[0].getText().toString();
      this.validateRequest.info.shipping_address.street_line2 = this.inputFields[1].getText().toString();
      this.validateRequest.info.shipping_address.city = this.inputFields[2].getText().toString();
      this.validateRequest.info.shipping_address.state = this.inputFields[3].getText().toString();
      localTL_postAddress = this.validateRequest.info.shipping_address;
      if (this.countryName == null) {
        break label497;
      }
    }
    label497:
    for (final Object localObject = this.countryName;; localObject = "")
    {
      localTL_postAddress.country_iso2 = ((String)localObject);
      this.validateRequest.info.shipping_address.post_code = this.inputFields[5].getText().toString();
      localObject = this.validateRequest.info;
      ((TLRPC.TL_paymentRequestedInfo)localObject).flags |= 0x8;
      localObject = this.validateRequest;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(this.validateRequest, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          if ((paramAnonymousTLObject instanceof TLRPC.TL_payments_validatedRequestedInfo))
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                PaymentFormActivity.access$902(PaymentFormActivity.this, (TLRPC.TL_payments_validatedRequestedInfo)paramAnonymousTLObject);
                if ((PaymentFormActivity.this.paymentForm.saved_info != null) && (!PaymentFormActivity.this.saveShippingInfo))
                {
                  TLRPC.TL_payments_clearSavedInfo localTL_payments_clearSavedInfo = new TLRPC.TL_payments_clearSavedInfo();
                  localTL_payments_clearSavedInfo.info = true;
                  ConnectionsManager.getInstance(PaymentFormActivity.this.currentAccount).sendRequest(localTL_payments_clearSavedInfo, new RequestDelegate()
                  {
                    public void run(TLObject paramAnonymous3TLObject, TLRPC.TL_error paramAnonymous3TL_error) {}
                  });
                }
                PaymentFormActivity.this.goToNextStep();
                PaymentFormActivity.this.setDonePressed(false);
                PaymentFormActivity.this.showEditDoneProgress(true, false);
              }
            });
            return;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              PaymentFormActivity.this.setDonePressed(false);
              PaymentFormActivity.this.showEditDoneProgress(true, false);
              String str;
              int i;
              if (paramAnonymousTL_error != null)
              {
                str = paramAnonymousTL_error.text;
                i = -1;
                switch (str.hashCode())
                {
                }
              }
              for (;;)
              {
                switch (i)
                {
                default: 
                  AlertsCreator.processError(PaymentFormActivity.this.currentAccount, paramAnonymousTL_error, PaymentFormActivity.this, PaymentFormActivity.34.this.val$req, new Object[0]);
                  return;
                  if (str.equals("REQ_INFO_NAME_INVALID"))
                  {
                    i = 0;
                    continue;
                    if (str.equals("REQ_INFO_PHONE_INVALID"))
                    {
                      i = 1;
                      continue;
                      if (str.equals("REQ_INFO_EMAIL_INVALID"))
                      {
                        i = 2;
                        continue;
                        if (str.equals("ADDRESS_COUNTRY_INVALID"))
                        {
                          i = 3;
                          continue;
                          if (str.equals("ADDRESS_CITY_INVALID"))
                          {
                            i = 4;
                            continue;
                            if (str.equals("ADDRESS_POSTCODE_INVALID"))
                            {
                              i = 5;
                              continue;
                              if (str.equals("ADDRESS_STATE_INVALID"))
                              {
                                i = 6;
                                continue;
                                if (str.equals("ADDRESS_STREET_LINE1_INVALID"))
                                {
                                  i = 7;
                                  continue;
                                  if (str.equals("ADDRESS_STREET_LINE2_INVALID")) {
                                    i = 8;
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                  break;
                }
              }
              PaymentFormActivity.this.shakeField(6);
              return;
              PaymentFormActivity.this.shakeField(9);
              return;
              PaymentFormActivity.this.shakeField(7);
              return;
              PaymentFormActivity.this.shakeField(4);
              return;
              PaymentFormActivity.this.shakeField(2);
              return;
              PaymentFormActivity.this.shakeField(5);
              return;
              PaymentFormActivity.this.shakeField(3);
              return;
              PaymentFormActivity.this.shakeField(0);
              return;
              PaymentFormActivity.this.shakeField(1);
            }
          });
        }
      }, 2);
      return;
    }
  }
  
  private void sendSavePassword(final boolean paramBoolean)
  {
    TLRPC.TL_account_updatePasswordSettings localTL_account_updatePasswordSettings = new TLRPC.TL_account_updatePasswordSettings();
    final String str1;
    if (paramBoolean)
    {
      this.doneItem.setVisibility(0);
      str1 = null;
      localTL_account_updatePasswordSettings.new_settings = new TLRPC.TL_account_passwordInputSettings();
      localTL_account_updatePasswordSettings.new_settings.flags = 2;
      localTL_account_updatePasswordSettings.new_settings.email = "";
      localTL_account_updatePasswordSettings.current_password_hash = new byte[0];
    }
    for (;;)
    {
      showEditDoneProgress(true, true);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_updatePasswordSettings, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              PaymentFormActivity.this.showEditDoneProgress(true, false);
              if (PaymentFormActivity.32.this.val$clear)
              {
                PaymentFormActivity.access$4302(PaymentFormActivity.this, new TLRPC.TL_account_noPassword());
                PaymentFormActivity.this.delegate.currentPasswordUpdated(PaymentFormActivity.this.currentPassword);
                PaymentFormActivity.this.finishFragment();
              }
              Object localObject;
              do
              {
                do
                {
                  do
                  {
                    return;
                    if ((paramAnonymousTL_error != null) || (!(paramAnonymousTLObject instanceof TLRPC.TL_boolTrue))) {
                      break;
                    }
                  } while (PaymentFormActivity.this.getParentActivity() == null);
                  PaymentFormActivity.this.goToNextStep();
                  return;
                } while (paramAnonymousTL_error == null);
                if (!paramAnonymousTL_error.text.equals("EMAIL_UNCONFIRMED")) {
                  break;
                }
                localObject = new AlertDialog.Builder(PaymentFormActivity.this.getParentActivity());
                ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                  {
                    PaymentFormActivity.access$5302(PaymentFormActivity.this, true);
                    PaymentFormActivity.this.currentPassword.email_unconfirmed_pattern = PaymentFormActivity.32.this.val$email;
                    PaymentFormActivity.this.updatePasswordFields();
                  }
                });
                ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("YourEmailAlmostThereText", 2131494663));
                ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("YourEmailAlmostThere", 2131494662));
                localObject = PaymentFormActivity.this.showDialog(((AlertDialog.Builder)localObject).create());
              } while (localObject == null);
              ((Dialog)localObject).setCanceledOnTouchOutside(false);
              ((Dialog)localObject).setCancelable(false);
              return;
              if (paramAnonymousTL_error.text.equals("EMAIL_INVALID"))
              {
                PaymentFormActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.getString("PasswordEmailInvalid", 2131494074));
                return;
              }
              if (paramAnonymousTL_error.text.startsWith("FLOOD_WAIT"))
              {
                int i = Utilities.parseInt(paramAnonymousTL_error.text).intValue();
                if (i < 60) {}
                for (localObject = LocaleController.formatPluralString("Seconds", i);; localObject = LocaleController.formatPluralString("Minutes", i / 60))
                {
                  PaymentFormActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { localObject }));
                  return;
                }
              }
              PaymentFormActivity.this.showAlertWithText(LocaleController.getString("AppName", 2131492981), paramAnonymousTL_error.text);
            }
          });
        }
      }, 10);
      return;
      Object localObject2 = this.inputFields[0].getText().toString();
      if (TextUtils.isEmpty((CharSequence)localObject2))
      {
        shakeField(0);
        return;
      }
      if (!((String)localObject2).equals(this.inputFields[1].getText().toString())) {
        try
        {
          Toast.makeText(getParentActivity(), LocaleController.getString("PasswordDoNotMatch", 2131494073), 0).show();
          shakeField(1);
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
      String str2 = this.inputFields[2].getText().toString();
      if (str2.length() < 3)
      {
        shakeField(2);
        return;
      }
      int i = str2.lastIndexOf('.');
      int j = str2.lastIndexOf('@');
      if ((i < 0) || (j < 0) || (i < j))
      {
        shakeField(2);
        return;
      }
      localTL_account_updatePasswordSettings.current_password_hash = new byte[0];
      localTL_account_updatePasswordSettings.new_settings = new TLRPC.TL_account_passwordInputSettings();
      Object localObject1 = null;
      try
      {
        localObject2 = ((String)localObject2).getBytes("UTF-8");
        localObject1 = localObject2;
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          byte[] arrayOfByte;
          FileLog.e(localException2);
        }
      }
      localObject2 = this.currentPassword.new_salt;
      arrayOfByte = new byte[localObject2.length * 2 + localObject1.length];
      System.arraycopy(localObject2, 0, arrayOfByte, 0, localObject2.length);
      System.arraycopy(localObject1, 0, arrayOfByte, localObject2.length, localObject1.length);
      System.arraycopy(localObject2, 0, arrayOfByte, arrayOfByte.length - localObject2.length, localObject2.length);
      localObject1 = localTL_account_updatePasswordSettings.new_settings;
      ((TLRPC.TL_account_passwordInputSettings)localObject1).flags |= 0x1;
      localTL_account_updatePasswordSettings.new_settings.hint = "";
      localTL_account_updatePasswordSettings.new_settings.new_password_hash = Utilities.computeSHA256(arrayOfByte, 0, arrayOfByte.length);
      localTL_account_updatePasswordSettings.new_settings.new_salt = ((byte[])localObject2);
      localObject1 = str2;
      if (str2.length() > 0)
      {
        localObject1 = localTL_account_updatePasswordSettings.new_settings;
        ((TLRPC.TL_account_passwordInputSettings)localObject1).flags |= 0x2;
        localTL_account_updatePasswordSettings.new_settings.email = str2.trim();
        localObject1 = str2;
      }
    }
  }
  
  private void setCurrentPassword(TLRPC.account_Password paramaccount_Password)
  {
    if ((paramaccount_Password instanceof TLRPC.TL_account_password))
    {
      if (getParentActivity() == null) {
        return;
      }
      goToNextStep();
      return;
    }
    this.currentPassword = paramaccount_Password;
    if (this.currentPassword != null) {
      if (this.currentPassword.email_unconfirmed_pattern.length() <= 0) {
        break label57;
      }
    }
    label57:
    for (boolean bool = true;; bool = false)
    {
      this.waitingForEmail = bool;
      updatePasswordFields();
      return;
    }
  }
  
  private void setDelegate(PaymentFormActivityDelegate paramPaymentFormActivityDelegate)
  {
    this.delegate = paramPaymentFormActivityDelegate;
  }
  
  private void setDonePressed(boolean paramBoolean)
  {
    boolean bool = true;
    this.donePressed = paramBoolean;
    Object localObject;
    if (!paramBoolean)
    {
      paramBoolean = true;
      this.swipeBackEnabled = paramBoolean;
      localObject = this.actionBar.getBackButton();
      if (this.donePressed) {
        break label76;
      }
      paramBoolean = true;
      label35:
      ((View)localObject).setEnabled(paramBoolean);
      if (this.detailSettingsCell[0] != null)
      {
        localObject = this.detailSettingsCell[0];
        if (this.donePressed) {
          break label81;
        }
      }
    }
    label76:
    label81:
    for (paramBoolean = bool;; paramBoolean = false)
    {
      ((TextDetailSettingsCell)localObject).setEnabled(paramBoolean);
      return;
      paramBoolean = false;
      break;
      paramBoolean = false;
      break label35;
    }
  }
  
  private void shakeField(int paramInt)
  {
    Vibrator localVibrator = (Vibrator)getParentActivity().getSystemService("vibrator");
    if (localVibrator != null) {
      localVibrator.vibrate(200L);
    }
    AndroidUtilities.shakeView(this.inputFields[paramInt], 2.0F, 0);
  }
  
  private void showAlertWithText(String paramString1, String paramString2)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    localBuilder.setTitle(paramString1);
    localBuilder.setMessage(paramString2);
    showDialog(localBuilder.create());
  }
  
  private void showAndroidPay()
  {
    if ((getParentActivity() == null) || (this.androidPayContainer == null)) {
      return;
    }
    Object localObject2 = WalletFragmentOptions.newBuilder();
    int i;
    if (this.paymentForm.invoice.test)
    {
      i = 3;
      ((WalletFragmentOptions.Builder)localObject2).setEnvironment(i);
      ((WalletFragmentOptions.Builder)localObject2).setMode(1);
      if (this.androidPayPublicKey == null) {
        break label347;
      }
      this.androidPayContainer.setBackgroundColor(this.androidPayBackgroundColor);
      localObject1 = new WalletFragmentStyle().setBuyButtonText(5);
      if (!this.androidPayBlackTheme) {
        break label342;
      }
      i = 6;
      label86:
      localObject1 = ((WalletFragmentStyle)localObject1).setBuyButtonAppearance(i).setBuyButtonWidth(-1);
      label96:
      ((WalletFragmentOptions.Builder)localObject2).setFragmentStyle((WalletFragmentStyle)localObject1);
      localObject2 = WalletFragment.newInstance(((WalletFragmentOptions.Builder)localObject2).build());
      localObject1 = getParentActivity().getFragmentManager().beginTransaction();
      ((FragmentTransaction)localObject1).replace(4000, (Fragment)localObject2);
      ((FragmentTransaction)localObject1).commit();
      localObject1 = new ArrayList();
      ((ArrayList)localObject1).addAll(this.paymentForm.invoice.prices);
      if (this.shippingOption != null) {
        ((ArrayList)localObject1).addAll(this.shippingOption.prices);
      }
      this.totalPriceDecimal = getTotalPriceDecimalString((ArrayList)localObject1);
      if (this.androidPayPublicKey == null) {
        break label373;
      }
    }
    label342:
    label347:
    label373:
    for (Object localObject1 = PaymentMethodTokenizationParameters.newBuilder().setPaymentMethodTokenizationType(2).addParameter("publicKey", this.androidPayPublicKey).build();; localObject1 = PaymentMethodTokenizationParameters.newBuilder().setPaymentMethodTokenizationType(1).addParameter("gateway", "stripe").addParameter("stripe:publishableKey", this.stripeApiKey).addParameter("stripe:version", "3.5.0").build())
    {
      localObject1 = MaskedWalletRequest.newBuilder().setPaymentMethodTokenizationParameters((PaymentMethodTokenizationParameters)localObject1).setEstimatedTotalPrice(this.totalPriceDecimal).setCurrencyCode(this.paymentForm.invoice.currency).build();
      ((WalletFragment)localObject2).initialize(WalletFragmentInitParams.newBuilder().setMaskedWalletRequest((MaskedWalletRequest)localObject1).setMaskedWalletRequestCode(1000).build());
      this.androidPayContainer.setVisibility(0);
      localObject1 = new AnimatorSet();
      ((AnimatorSet)localObject1).playTogether(new Animator[] { ObjectAnimator.ofFloat(this.androidPayContainer, "alpha", new float[] { 0.0F, 1.0F }) });
      ((AnimatorSet)localObject1).setInterpolator(new DecelerateInterpolator());
      ((AnimatorSet)localObject1).setDuration(180L);
      ((AnimatorSet)localObject1).start();
      return;
      i = 1;
      break;
      i = 4;
      break label86;
      localObject1 = new WalletFragmentStyle().setBuyButtonText(6).setBuyButtonAppearance(6).setBuyButtonWidth(-2);
      break label96;
    }
  }
  
  private void showEditDoneProgress(boolean paramBoolean1, final boolean paramBoolean2)
  {
    if (this.doneItemAnimation != null) {
      this.doneItemAnimation.cancel();
    }
    if ((paramBoolean1) && (this.doneItem != null))
    {
      this.doneItemAnimation = new AnimatorSet();
      if (paramBoolean2)
      {
        this.progressView.setVisibility(0);
        this.doneItem.setEnabled(false);
        this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressView, "alpha", new float[] { 1.0F }) });
        this.doneItemAnimation.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationCancel(Animator paramAnonymousAnimator)
          {
            if ((PaymentFormActivity.this.doneItemAnimation != null) && (PaymentFormActivity.this.doneItemAnimation.equals(paramAnonymousAnimator))) {
              PaymentFormActivity.access$7002(PaymentFormActivity.this, null);
            }
          }
          
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if ((PaymentFormActivity.this.doneItemAnimation != null) && (PaymentFormActivity.this.doneItemAnimation.equals(paramAnonymousAnimator)))
            {
              if (!paramBoolean2) {
                PaymentFormActivity.this.progressView.setVisibility(4);
              }
            }
            else {
              return;
            }
            PaymentFormActivity.this.doneItem.getImageView().setVisibility(4);
          }
        });
        this.doneItemAnimation.setDuration(150L);
        this.doneItemAnimation.start();
      }
    }
    while (this.payTextView == null) {
      for (;;)
      {
        return;
        if (this.webView != null)
        {
          this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "alpha", new float[] { 0.0F }) });
        }
        else
        {
          this.doneItem.getImageView().setVisibility(0);
          this.doneItem.setEnabled(true);
          this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.progressView, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressView, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.doneItem.getImageView(), "alpha", new float[] { 1.0F }) });
        }
      }
    }
    this.doneItemAnimation = new AnimatorSet();
    if (paramBoolean2)
    {
      this.progressViewButton.setVisibility(0);
      this.bottomLayout.setEnabled(false);
      this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.payTextView, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.payTextView, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.payTextView, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.progressViewButton, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressViewButton, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.progressViewButton, "alpha", new float[] { 1.0F }) });
    }
    for (;;)
    {
      this.doneItemAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((PaymentFormActivity.this.doneItemAnimation != null) && (PaymentFormActivity.this.doneItemAnimation.equals(paramAnonymousAnimator))) {
            PaymentFormActivity.access$7002(PaymentFormActivity.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((PaymentFormActivity.this.doneItemAnimation != null) && (PaymentFormActivity.this.doneItemAnimation.equals(paramAnonymousAnimator)))
          {
            if (!paramBoolean2) {
              PaymentFormActivity.this.progressViewButton.setVisibility(4);
            }
          }
          else {
            return;
          }
          PaymentFormActivity.this.payTextView.setVisibility(4);
        }
      });
      this.doneItemAnimation.setDuration(150L);
      this.doneItemAnimation.start();
      return;
      this.payTextView.setVisibility(0);
      this.bottomLayout.setEnabled(true);
      this.doneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.progressViewButton, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressViewButton, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.progressViewButton, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.payTextView, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.payTextView, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.payTextView, "alpha", new float[] { 1.0F }) });
    }
  }
  
  private void showPayAlert(String paramString)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("PaymentTransactionReview", 2131494135));
    localBuilder.setMessage(LocaleController.formatString("PaymentTransactionMessage", 2131494134, new Object[] { paramString, this.currentBotName, this.currentItemName }));
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        PaymentFormActivity.this.setDonePressed(true);
        PaymentFormActivity.this.sendData();
      }
    });
    localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
    showDialog(localBuilder.create());
  }
  
  private void updatePasswordFields()
  {
    if ((this.currentStep != 6) || (this.bottomCell[2] == null)) {}
    for (;;)
    {
      return;
      int i;
      if (this.currentPassword == null)
      {
        this.doneItem.setVisibility(0);
        showEditDoneProgress(true, true);
        this.bottomCell[2].setVisibility(8);
        this.settingsCell1.setVisibility(8);
        this.headerCell[0].setVisibility(8);
        this.headerCell[1].setVisibility(8);
        this.bottomCell[0].setVisibility(8);
        i = 0;
        while (i < 3)
        {
          ((View)this.inputFields[i].getParent()).setVisibility(8);
          i += 1;
        }
        i = 0;
        while (i < this.dividers.size())
        {
          ((View)this.dividers.get(i)).setVisibility(8);
          i += 1;
        }
      }
      else
      {
        showEditDoneProgress(true, false);
        if (this.waitingForEmail)
        {
          if (getParentActivity() != null) {
            AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
          }
          this.doneItem.setVisibility(8);
          this.bottomCell[2].setText(LocaleController.formatString("EmailPasswordConfirmText", 2131493418, new Object[] { this.currentPassword.email_unconfirmed_pattern }));
          this.bottomCell[2].setVisibility(0);
          this.settingsCell1.setVisibility(0);
          this.bottomCell[1].setText("");
          this.headerCell[0].setVisibility(8);
          this.headerCell[1].setVisibility(8);
          this.bottomCell[0].setVisibility(8);
          i = 0;
          while (i < 3)
          {
            ((View)this.inputFields[i].getParent()).setVisibility(8);
            i += 1;
          }
          i = 0;
          while (i < this.dividers.size())
          {
            ((View)this.dividers.get(i)).setVisibility(8);
            i += 1;
          }
        }
        else
        {
          this.doneItem.setVisibility(0);
          this.bottomCell[2].setVisibility(8);
          this.settingsCell1.setVisibility(8);
          this.bottomCell[1].setText(LocaleController.getString("PaymentPasswordEmailInfo", 2131494106));
          this.headerCell[0].setVisibility(0);
          this.headerCell[1].setVisibility(0);
          this.bottomCell[0].setVisibility(0);
          i = 0;
          while (i < 3)
          {
            ((View)this.inputFields[i].getParent()).setVisibility(0);
            i += 1;
          }
          i = 0;
          while (i < this.dividers.size())
          {
            ((View)this.dividers.get(i)).setVisibility(0);
            i += 1;
          }
        }
      }
    }
  }
  
  private void updateSavePaymentField()
  {
    if ((this.bottomCell[0] == null) || (this.sectionCell[2] == null)) {
      return;
    }
    if (((this.paymentForm.password_missing) || (this.paymentForm.can_save_credentials)) && ((this.webView == null) || ((this.webView != null) && (!this.webviewLoading))))
    {
      SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder(LocaleController.getString("PaymentCardSavePaymentInformationInfoLine1", 2131494086));
      if (this.paymentForm.password_missing)
      {
        loadPasswordInfo();
        localSpannableStringBuilder.append("\n");
        int i = localSpannableStringBuilder.length();
        String str = LocaleController.getString("PaymentCardSavePaymentInformationInfoLine2", 2131494087);
        int k = str.indexOf('*');
        int j = str.lastIndexOf('*');
        localSpannableStringBuilder.append(str);
        if ((k != -1) && (j != -1))
        {
          k += i;
          i = j + i;
          this.bottomCell[0].getTextView().setMovementMethod(new LinkMovementMethodMy(null));
          localSpannableStringBuilder.replace(i, i + 1, "");
          localSpannableStringBuilder.replace(k, k + 1, "");
          localSpannableStringBuilder.setSpan(new LinkSpan(), k, i - 1, 33);
        }
      }
      this.checkCell1.setEnabled(true);
      this.bottomCell[0].setText(localSpannableStringBuilder);
      this.checkCell1.setVisibility(0);
      this.bottomCell[0].setVisibility(0);
      this.sectionCell[2].setBackgroundDrawable(Theme.getThemedDrawable(this.sectionCell[2].getContext(), 2131165331, "windowBackgroundGrayShadow"));
      return;
    }
    this.checkCell1.setVisibility(8);
    this.bottomCell[0].setVisibility(8);
    this.sectionCell[2].setBackgroundDrawable(Theme.getThemedDrawable(this.sectionCell[2].getContext(), 2131165332, "windowBackgroundGrayShadow"));
  }
  
  /* Error */
  @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
  public View createView(Context paramContext)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   4: ifne +1230 -> 1234
    //   7: aload_0
    //   8: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   11: ldc_w 1569
    //   14: ldc_w 1570
    //   17: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   20: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   23: aload_0
    //   24: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   27: ldc_w 1573
    //   30: invokevirtual 1576	org/telegram/ui/ActionBar/ActionBar:setBackButtonImage	(I)V
    //   33: aload_0
    //   34: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   37: iconst_1
    //   38: invokevirtual 1579	org/telegram/ui/ActionBar/ActionBar:setAllowOverlayTitle	(Z)V
    //   41: aload_0
    //   42: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   45: new 8	org/telegram/ui/PaymentFormActivity$1
    //   48: dup
    //   49: aload_0
    //   50: invokespecial 1580	org/telegram/ui/PaymentFormActivity$1:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   53: invokevirtual 1584	org/telegram/ui/ActionBar/ActionBar:setActionBarMenuOnItemClick	(Lorg/telegram/ui/ActionBar/ActionBar$ActionBarMenuOnItemClick;)V
    //   56: aload_0
    //   57: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   60: invokevirtual 1588	org/telegram/ui/ActionBar/ActionBar:createMenu	()Lorg/telegram/ui/ActionBar/ActionBarMenu;
    //   63: astore 8
    //   65: aload_0
    //   66: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   69: ifeq +44 -> 113
    //   72: aload_0
    //   73: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   76: iconst_1
    //   77: if_icmpeq +36 -> 113
    //   80: aload_0
    //   81: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   84: iconst_2
    //   85: if_icmpeq +28 -> 113
    //   88: aload_0
    //   89: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   92: iconst_3
    //   93: if_icmpeq +20 -> 113
    //   96: aload_0
    //   97: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   100: iconst_4
    //   101: if_icmpeq +12 -> 113
    //   104: aload_0
    //   105: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   108: bipush 6
    //   110: if_icmpne +61 -> 171
    //   113: aload_0
    //   114: aload 8
    //   116: iconst_1
    //   117: ldc_w 1589
    //   120: ldc_w 1590
    //   123: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   126: invokevirtual 1600	org/telegram/ui/ActionBar/ActionBarMenu:addItemWithWidth	(III)Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   129: putfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   132: aload_0
    //   133: new 1423	org/telegram/ui/Components/ContextProgressView
    //   136: dup
    //   137: aload_1
    //   138: iconst_1
    //   139: invokespecial 1603	org/telegram/ui/Components/ContextProgressView:<init>	(Landroid/content/Context;I)V
    //   142: putfield 673	org/telegram/ui/PaymentFormActivity:progressView	Lorg/telegram/ui/Components/ContextProgressView;
    //   145: aload_0
    //   146: getfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   149: aload_0
    //   150: getfield 673	org/telegram/ui/PaymentFormActivity:progressView	Lorg/telegram/ui/Components/ContextProgressView;
    //   153: iconst_m1
    //   154: ldc_w 1604
    //   157: invokestatic 1610	org/telegram/ui/Components/LayoutHelper:createFrame	(IF)Landroid/widget/FrameLayout$LayoutParams;
    //   160: invokevirtual 1614	org/telegram/ui/ActionBar/ActionBarMenuItem:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   163: aload_0
    //   164: getfield 673	org/telegram/ui/PaymentFormActivity:progressView	Lorg/telegram/ui/Components/ContextProgressView;
    //   167: iconst_4
    //   168: invokevirtual 1424	org/telegram/ui/Components/ContextProgressView:setVisibility	(I)V
    //   171: aload_0
    //   172: new 1251	android/widget/FrameLayout
    //   175: dup
    //   176: aload_1
    //   177: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   180: putfield 1619	org/telegram/ui/PaymentFormActivity:fragmentView	Landroid/view/View;
    //   183: aload_0
    //   184: getfield 1619	org/telegram/ui/PaymentFormActivity:fragmentView	Landroid/view/View;
    //   187: checkcast 1251	android/widget/FrameLayout
    //   190: astore 10
    //   192: aload_0
    //   193: getfield 1619	org/telegram/ui/PaymentFormActivity:fragmentView	Landroid/view/View;
    //   196: ldc_w 1621
    //   199: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   202: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   205: aload_0
    //   206: new 1628	android/widget/ScrollView
    //   209: dup
    //   210: aload_1
    //   211: invokespecial 1629	android/widget/ScrollView:<init>	(Landroid/content/Context;)V
    //   214: putfield 1631	org/telegram/ui/PaymentFormActivity:scrollView	Landroid/widget/ScrollView;
    //   217: aload_0
    //   218: getfield 1631	org/telegram/ui/PaymentFormActivity:scrollView	Landroid/widget/ScrollView;
    //   221: iconst_1
    //   222: invokevirtual 1634	android/widget/ScrollView:setFillViewport	(Z)V
    //   225: aload_0
    //   226: getfield 1631	org/telegram/ui/PaymentFormActivity:scrollView	Landroid/widget/ScrollView;
    //   229: ldc_w 1636
    //   232: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   235: invokestatic 1640	org/telegram/messenger/AndroidUtilities:setScrollViewEdgeEffectColor	(Landroid/widget/ScrollView;I)V
    //   238: aload_0
    //   239: getfield 1631	org/telegram/ui/PaymentFormActivity:scrollView	Landroid/widget/ScrollView;
    //   242: astore 8
    //   244: aload_0
    //   245: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   248: iconst_4
    //   249: if_icmpne +1250 -> 1499
    //   252: ldc_w 1641
    //   255: fstore_2
    //   256: aload 10
    //   258: aload 8
    //   260: iconst_m1
    //   261: ldc_w 1604
    //   264: bipush 51
    //   266: fconst_0
    //   267: fconst_0
    //   268: fconst_0
    //   269: fload_2
    //   270: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   273: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   276: aload_0
    //   277: new 1647	android/widget/LinearLayout
    //   280: dup
    //   281: aload_1
    //   282: invokespecial 1648	android/widget/LinearLayout:<init>	(Landroid/content/Context;)V
    //   285: putfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   288: aload_0
    //   289: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   292: iconst_1
    //   293: invokevirtual 1653	android/widget/LinearLayout:setOrientation	(I)V
    //   296: aload_0
    //   297: getfield 1631	org/telegram/ui/PaymentFormActivity:scrollView	Landroid/widget/ScrollView;
    //   300: aload_0
    //   301: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   304: new 1655	android/widget/FrameLayout$LayoutParams
    //   307: dup
    //   308: iconst_m1
    //   309: bipush -2
    //   311: invokespecial 1658	android/widget/FrameLayout$LayoutParams:<init>	(II)V
    //   314: invokevirtual 1659	android/widget/ScrollView:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   317: aload_0
    //   318: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   321: ifne +3454 -> 3775
    //   324: new 289	java/util/HashMap
    //   327: dup
    //   328: invokespecial 290	java/util/HashMap:<init>	()V
    //   331: astore 10
    //   333: new 289	java/util/HashMap
    //   336: dup
    //   337: invokespecial 290	java/util/HashMap:<init>	()V
    //   340: astore 11
    //   342: new 1661	java/io/BufferedReader
    //   345: dup
    //   346: new 1663	java/io/InputStreamReader
    //   349: dup
    //   350: aload_1
    //   351: invokevirtual 1667	android/content/Context:getResources	()Landroid/content/res/Resources;
    //   354: invokevirtual 1673	android/content/res/Resources:getAssets	()Landroid/content/res/AssetManager;
    //   357: ldc_w 1675
    //   360: invokevirtual 1681	android/content/res/AssetManager:open	(Ljava/lang/String;)Ljava/io/InputStream;
    //   363: invokespecial 1684	java/io/InputStreamReader:<init>	(Ljava/io/InputStream;)V
    //   366: invokespecial 1687	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
    //   369: astore 8
    //   371: aload 8
    //   373: invokevirtual 1690	java/io/BufferedReader:readLine	()Ljava/lang/String;
    //   376: astore 9
    //   378: aload 9
    //   380: ifnull +1124 -> 1504
    //   383: aload 9
    //   385: ldc_w 1692
    //   388: invokevirtual 977	java/lang/String:split	(Ljava/lang/String;)[Ljava/lang/String;
    //   391: astore 9
    //   393: aload_0
    //   394: getfield 287	org/telegram/ui/PaymentFormActivity:countriesArray	Ljava/util/ArrayList;
    //   397: iconst_0
    //   398: aload 9
    //   400: iconst_2
    //   401: aaload
    //   402: invokevirtual 1696	java/util/ArrayList:add	(ILjava/lang/Object;)V
    //   405: aload_0
    //   406: getfield 292	org/telegram/ui/PaymentFormActivity:countriesMap	Ljava/util/HashMap;
    //   409: aload 9
    //   411: iconst_2
    //   412: aaload
    //   413: aload 9
    //   415: iconst_0
    //   416: aaload
    //   417: invokevirtual 1700	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   420: pop
    //   421: aload_0
    //   422: getfield 294	org/telegram/ui/PaymentFormActivity:codesMap	Ljava/util/HashMap;
    //   425: aload 9
    //   427: iconst_0
    //   428: aaload
    //   429: aload 9
    //   431: iconst_2
    //   432: aaload
    //   433: invokevirtual 1700	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   436: pop
    //   437: aload 11
    //   439: aload 9
    //   441: iconst_1
    //   442: aaload
    //   443: aload 9
    //   445: iconst_2
    //   446: aaload
    //   447: invokevirtual 1700	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   450: pop
    //   451: aload 9
    //   453: arraylength
    //   454: iconst_3
    //   455: if_icmple +19 -> 474
    //   458: aload_0
    //   459: getfield 296	org/telegram/ui/PaymentFormActivity:phoneFormatMap	Ljava/util/HashMap;
    //   462: aload 9
    //   464: iconst_0
    //   465: aaload
    //   466: aload 9
    //   468: iconst_3
    //   469: aaload
    //   470: invokevirtual 1700	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   473: pop
    //   474: aload 10
    //   476: aload 9
    //   478: iconst_1
    //   479: aaload
    //   480: aload 9
    //   482: iconst_2
    //   483: aaload
    //   484: invokevirtual 1700	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   487: pop
    //   488: goto -117 -> 371
    //   491: astore 8
    //   493: aload 8
    //   495: invokestatic 1032	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   498: aload_0
    //   499: getfield 287	org/telegram/ui/PaymentFormActivity:countriesArray	Ljava/util/ArrayList;
    //   502: new 32	org/telegram/ui/PaymentFormActivity$2
    //   505: dup
    //   506: aload_0
    //   507: invokespecial 1701	org/telegram/ui/PaymentFormActivity$2:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   510: invokestatic 1707	java/util/Collections:sort	(Ljava/util/List;Ljava/util/Comparator;)V
    //   513: aload_0
    //   514: bipush 10
    //   516: anewarray 708	org/telegram/ui/Components/EditTextBoldCursor
    //   519: putfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   522: iconst_0
    //   523: istore 5
    //   525: iload 5
    //   527: bipush 10
    //   529: if_icmpge +2518 -> 3047
    //   532: iload 5
    //   534: ifne +978 -> 1512
    //   537: aload_0
    //   538: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   541: iconst_0
    //   542: new 298	org/telegram/ui/Cells/HeaderCell
    //   545: dup
    //   546: aload_1
    //   547: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   550: aastore
    //   551: aload_0
    //   552: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   555: iconst_0
    //   556: aaload
    //   557: ldc_w 1710
    //   560: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   563: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   566: aload_0
    //   567: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   570: iconst_0
    //   571: aaload
    //   572: ldc_w 1713
    //   575: ldc_w 1714
    //   578: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   581: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   584: aload_0
    //   585: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   588: aload_0
    //   589: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   592: iconst_0
    //   593: aaload
    //   594: iconst_m1
    //   595: bipush -2
    //   597: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   600: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   603: iload 5
    //   605: bipush 8
    //   607: if_icmpne +1014 -> 1621
    //   610: new 1647	android/widget/LinearLayout
    //   613: dup
    //   614: aload_1
    //   615: invokespecial 1648	android/widget/LinearLayout:<init>	(Landroid/content/Context;)V
    //   618: astore 8
    //   620: aload 8
    //   622: checkcast 1647	android/widget/LinearLayout
    //   625: iconst_0
    //   626: invokevirtual 1653	android/widget/LinearLayout:setOrientation	(I)V
    //   629: aload_0
    //   630: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   633: aload 8
    //   635: iconst_m1
    //   636: bipush 48
    //   638: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   641: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   644: aload 8
    //   646: ldc_w 1710
    //   649: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   652: invokevirtual 1724	android/view/ViewGroup:setBackgroundColor	(I)V
    //   655: iload 5
    //   657: bipush 9
    //   659: if_icmpne +1186 -> 1845
    //   662: aload_0
    //   663: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   666: iload 5
    //   668: new 1726	org/telegram/ui/Components/HintEditText
    //   671: dup
    //   672: aload_1
    //   673: invokespecial 1727	org/telegram/ui/Components/HintEditText:<init>	(Landroid/content/Context;)V
    //   676: aastore
    //   677: aload_0
    //   678: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   681: iload 5
    //   683: aaload
    //   684: iload 5
    //   686: invokestatic 362	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   689: invokevirtual 1731	org/telegram/ui/Components/EditTextBoldCursor:setTag	(Ljava/lang/Object;)V
    //   692: aload_0
    //   693: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   696: iload 5
    //   698: aaload
    //   699: iconst_1
    //   700: ldc_w 1732
    //   703: invokevirtual 1736	org/telegram/ui/Components/EditTextBoldCursor:setTextSize	(IF)V
    //   706: aload_0
    //   707: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   710: iload 5
    //   712: aaload
    //   713: ldc_w 1738
    //   716: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   719: invokevirtual 1741	org/telegram/ui/Components/EditTextBoldCursor:setHintTextColor	(I)V
    //   722: aload_0
    //   723: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   726: iload 5
    //   728: aaload
    //   729: ldc_w 1743
    //   732: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   735: invokevirtual 1746	org/telegram/ui/Components/EditTextBoldCursor:setTextColor	(I)V
    //   738: aload_0
    //   739: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   742: iload 5
    //   744: aaload
    //   745: aconst_null
    //   746: invokevirtual 1747	org/telegram/ui/Components/EditTextBoldCursor:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   749: aload_0
    //   750: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   753: iload 5
    //   755: aaload
    //   756: ldc_w 1743
    //   759: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   762: invokevirtual 1750	org/telegram/ui/Components/EditTextBoldCursor:setCursorColor	(I)V
    //   765: aload_0
    //   766: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   769: iload 5
    //   771: aaload
    //   772: ldc_w 1751
    //   775: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   778: invokevirtual 1754	org/telegram/ui/Components/EditTextBoldCursor:setCursorSize	(I)V
    //   781: aload_0
    //   782: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   785: iload 5
    //   787: aaload
    //   788: ldc_w 1755
    //   791: invokevirtual 1759	org/telegram/ui/Components/EditTextBoldCursor:setCursorWidth	(F)V
    //   794: iload 5
    //   796: iconst_4
    //   797: if_icmpne +32 -> 829
    //   800: aload_0
    //   801: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   804: iload 5
    //   806: aaload
    //   807: new 64	org/telegram/ui/PaymentFormActivity$3
    //   810: dup
    //   811: aload_0
    //   812: invokespecial 1760	org/telegram/ui/PaymentFormActivity$3:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   815: invokevirtual 1764	org/telegram/ui/Components/EditTextBoldCursor:setOnTouchListener	(Landroid/view/View$OnTouchListener;)V
    //   818: aload_0
    //   819: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   822: iload 5
    //   824: aaload
    //   825: iconst_0
    //   826: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   829: iload 5
    //   831: bipush 9
    //   833: if_icmpeq +10 -> 843
    //   836: iload 5
    //   838: bipush 8
    //   840: if_icmpne +1023 -> 1863
    //   843: aload_0
    //   844: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   847: iload 5
    //   849: aaload
    //   850: iconst_3
    //   851: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   854: aload_0
    //   855: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   858: iload 5
    //   860: aaload
    //   861: ldc_w 1768
    //   864: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   867: iload 5
    //   869: tableswitch	default:+47->916, 0:+1161->2030, 1:+1229->2098, 2:+1297->2166, 3:+1365->2234, 4:+1433->2302, 5:+1548->2417, 6:+1031->1900, 7:+1096->1965
    //   916: aload_0
    //   917: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   920: iload 5
    //   922: aaload
    //   923: aload_0
    //   924: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   927: iload 5
    //   929: aaload
    //   930: invokevirtual 711	org/telegram/ui/Components/EditTextBoldCursor:length	()I
    //   933: invokevirtual 1774	org/telegram/ui/Components/EditTextBoldCursor:setSelection	(I)V
    //   936: iload 5
    //   938: bipush 8
    //   940: if_icmpne +1545 -> 2485
    //   943: aload_0
    //   944: new 1451	android/widget/TextView
    //   947: dup
    //   948: aload_1
    //   949: invokespecial 1775	android/widget/TextView:<init>	(Landroid/content/Context;)V
    //   952: putfield 1777	org/telegram/ui/PaymentFormActivity:textView	Landroid/widget/TextView;
    //   955: aload_0
    //   956: getfield 1777	org/telegram/ui/PaymentFormActivity:textView	Landroid/widget/TextView;
    //   959: ldc_w 775
    //   962: invokevirtual 1778	android/widget/TextView:setText	(Ljava/lang/CharSequence;)V
    //   965: aload_0
    //   966: getfield 1777	org/telegram/ui/PaymentFormActivity:textView	Landroid/widget/TextView;
    //   969: ldc_w 1743
    //   972: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   975: invokevirtual 1779	android/widget/TextView:setTextColor	(I)V
    //   978: aload_0
    //   979: getfield 1777	org/telegram/ui/PaymentFormActivity:textView	Landroid/widget/TextView;
    //   982: iconst_1
    //   983: ldc_w 1732
    //   986: invokevirtual 1780	android/widget/TextView:setTextSize	(IF)V
    //   989: aload 8
    //   991: aload_0
    //   992: getfield 1777	org/telegram/ui/PaymentFormActivity:textView	Landroid/widget/TextView;
    //   995: bipush -2
    //   997: bipush -2
    //   999: ldc_w 1781
    //   1002: ldc_w 1782
    //   1005: fconst_0
    //   1006: ldc_w 1783
    //   1009: invokestatic 1786	org/telegram/ui/Components/LayoutHelper:createLinear	(IIFFFF)Landroid/widget/LinearLayout$LayoutParams;
    //   1012: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1015: aload_0
    //   1016: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1019: iload 5
    //   1021: aaload
    //   1022: ldc_w 1788
    //   1025: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1028: iconst_0
    //   1029: iconst_0
    //   1030: iconst_0
    //   1031: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   1034: aload_0
    //   1035: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1038: iload 5
    //   1040: aaload
    //   1041: bipush 19
    //   1043: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   1046: new 1797	android/text/InputFilter$LengthFilter
    //   1049: dup
    //   1050: iconst_5
    //   1051: invokespecial 1799	android/text/InputFilter$LengthFilter:<init>	(I)V
    //   1054: astore 9
    //   1056: aload_0
    //   1057: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1060: iload 5
    //   1062: aaload
    //   1063: iconst_1
    //   1064: anewarray 1801	android/text/InputFilter
    //   1067: dup
    //   1068: iconst_0
    //   1069: aload 9
    //   1071: aastore
    //   1072: invokevirtual 1805	org/telegram/ui/Components/EditTextBoldCursor:setFilters	([Landroid/text/InputFilter;)V
    //   1075: aload 8
    //   1077: aload_0
    //   1078: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1081: iload 5
    //   1083: aaload
    //   1084: bipush 55
    //   1086: bipush -2
    //   1088: fconst_0
    //   1089: ldc_w 1782
    //   1092: ldc_w 1732
    //   1095: ldc_w 1783
    //   1098: invokestatic 1786	org/telegram/ui/Components/LayoutHelper:createLinear	(IIFFFF)Landroid/widget/LinearLayout$LayoutParams;
    //   1101: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1104: aload_0
    //   1105: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1108: iload 5
    //   1110: aaload
    //   1111: new 110	org/telegram/ui/PaymentFormActivity$4
    //   1114: dup
    //   1115: aload_0
    //   1116: invokespecial 1806	org/telegram/ui/PaymentFormActivity$4:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   1119: invokevirtual 1810	org/telegram/ui/Components/EditTextBoldCursor:addTextChangedListener	(Landroid/text/TextWatcher;)V
    //   1122: aload_0
    //   1123: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1126: iload 5
    //   1128: aaload
    //   1129: new 114	org/telegram/ui/PaymentFormActivity$6
    //   1132: dup
    //   1133: aload_0
    //   1134: invokespecial 1811	org/telegram/ui/PaymentFormActivity$6:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   1137: invokevirtual 1815	org/telegram/ui/Components/EditTextBoldCursor:setOnEditorActionListener	(Landroid/widget/TextView$OnEditorActionListener;)V
    //   1140: iload 5
    //   1142: bipush 9
    //   1144: if_icmpne +1777 -> 2921
    //   1147: aload_0
    //   1148: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1151: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1154: getfield 1818	org/telegram/tgnet/TLRPC$TL_invoice:email_to_provider	Z
    //   1157: ifne +16 -> 1173
    //   1160: aload_0
    //   1161: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1164: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1167: getfield 1821	org/telegram/tgnet/TLRPC$TL_invoice:phone_to_provider	Z
    //   1170: ifeq +1841 -> 3011
    //   1173: aconst_null
    //   1174: astore 8
    //   1176: iconst_0
    //   1177: istore_3
    //   1178: iload_3
    //   1179: aload_0
    //   1180: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1183: getfield 340	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:users	Ljava/util/ArrayList;
    //   1186: invokevirtual 821	java/util/ArrayList:size	()I
    //   1189: if_icmpge +1461 -> 2650
    //   1192: aload_0
    //   1193: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1196: getfield 340	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:users	Ljava/util/ArrayList;
    //   1199: iload_3
    //   1200: invokevirtual 825	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   1203: checkcast 370	org/telegram/tgnet/TLRPC$User
    //   1206: astore 9
    //   1208: aload 9
    //   1210: getfield 1823	org/telegram/tgnet/TLRPC$User:id	I
    //   1213: aload_0
    //   1214: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1217: getfield 336	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:provider_id	I
    //   1220: if_icmpne +7 -> 1227
    //   1223: aload 9
    //   1225: astore 8
    //   1227: iload_3
    //   1228: iconst_1
    //   1229: iadd
    //   1230: istore_3
    //   1231: goto -53 -> 1178
    //   1234: aload_0
    //   1235: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1238: iconst_1
    //   1239: if_icmpne +22 -> 1261
    //   1242: aload_0
    //   1243: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1246: ldc_w 1825
    //   1249: ldc_w 1826
    //   1252: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1255: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1258: goto -1235 -> 23
    //   1261: aload_0
    //   1262: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1265: iconst_2
    //   1266: if_icmpne +22 -> 1288
    //   1269: aload_0
    //   1270: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1273: ldc_w 1828
    //   1276: ldc_w 1829
    //   1279: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1282: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1285: goto -1262 -> 23
    //   1288: aload_0
    //   1289: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1292: iconst_3
    //   1293: if_icmpne +22 -> 1315
    //   1296: aload_0
    //   1297: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1300: ldc_w 1828
    //   1303: ldc_w 1829
    //   1306: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1309: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1312: goto -1289 -> 23
    //   1315: aload_0
    //   1316: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1319: iconst_4
    //   1320: if_icmpne +73 -> 1393
    //   1323: aload_0
    //   1324: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1327: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1330: getfield 926	org/telegram/tgnet/TLRPC$TL_invoice:test	Z
    //   1333: ifeq +41 -> 1374
    //   1336: aload_0
    //   1337: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1340: new 772	java/lang/StringBuilder
    //   1343: dup
    //   1344: invokespecial 773	java/lang/StringBuilder:<init>	()V
    //   1347: ldc_w 1831
    //   1350: invokevirtual 779	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1353: ldc_w 1833
    //   1356: ldc_w 1834
    //   1359: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1362: invokevirtual 779	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1365: invokevirtual 780	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1368: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1371: goto -1348 -> 23
    //   1374: aload_0
    //   1375: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1378: ldc_w 1833
    //   1381: ldc_w 1834
    //   1384: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1387: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1390: goto -1367 -> 23
    //   1393: aload_0
    //   1394: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1397: iconst_5
    //   1398: if_icmpne +73 -> 1471
    //   1401: aload_0
    //   1402: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1405: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1408: getfield 926	org/telegram/tgnet/TLRPC$TL_invoice:test	Z
    //   1411: ifeq +41 -> 1452
    //   1414: aload_0
    //   1415: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1418: new 772	java/lang/StringBuilder
    //   1421: dup
    //   1422: invokespecial 773	java/lang/StringBuilder:<init>	()V
    //   1425: ldc_w 1831
    //   1428: invokevirtual 779	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1431: ldc_w 1836
    //   1434: ldc_w 1837
    //   1437: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1440: invokevirtual 779	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   1443: invokevirtual 780	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   1446: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1449: goto -1426 -> 23
    //   1452: aload_0
    //   1453: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1456: ldc_w 1836
    //   1459: ldc_w 1837
    //   1462: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1465: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1468: goto -1445 -> 23
    //   1471: aload_0
    //   1472: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   1475: bipush 6
    //   1477: if_icmpne -1454 -> 23
    //   1480: aload_0
    //   1481: getfield 1189	org/telegram/ui/PaymentFormActivity:actionBar	Lorg/telegram/ui/ActionBar/ActionBar;
    //   1484: ldc_w 1839
    //   1487: ldc_w 1840
    //   1490: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1493: invokevirtual 1572	org/telegram/ui/ActionBar/ActionBar:setTitle	(Ljava/lang/CharSequence;)V
    //   1496: goto -1473 -> 23
    //   1499: fconst_0
    //   1500: fstore_2
    //   1501: goto -1245 -> 256
    //   1504: aload 8
    //   1506: invokevirtual 1843	java/io/BufferedReader:close	()V
    //   1509: goto -1011 -> 498
    //   1512: iload 5
    //   1514: bipush 6
    //   1516: if_icmpne -913 -> 603
    //   1519: aload_0
    //   1520: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   1523: iconst_0
    //   1524: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   1527: dup
    //   1528: aload_1
    //   1529: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   1532: aastore
    //   1533: aload_0
    //   1534: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   1537: aload_0
    //   1538: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   1541: iconst_0
    //   1542: aaload
    //   1543: iconst_m1
    //   1544: bipush -2
    //   1546: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   1549: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1552: aload_0
    //   1553: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   1556: iconst_1
    //   1557: new 298	org/telegram/ui/Cells/HeaderCell
    //   1560: dup
    //   1561: aload_1
    //   1562: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   1565: aastore
    //   1566: aload_0
    //   1567: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   1570: iconst_1
    //   1571: aaload
    //   1572: ldc_w 1710
    //   1575: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   1578: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   1581: aload_0
    //   1582: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   1585: iconst_1
    //   1586: aaload
    //   1587: ldc_w 1846
    //   1590: ldc_w 1847
    //   1593: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1596: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   1599: aload_0
    //   1600: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   1603: aload_0
    //   1604: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   1607: iconst_1
    //   1608: aaload
    //   1609: iconst_m1
    //   1610: bipush -2
    //   1612: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   1615: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1618: goto -1015 -> 603
    //   1621: iload 5
    //   1623: bipush 9
    //   1625: if_icmpne +21 -> 1646
    //   1628: aload_0
    //   1629: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1632: bipush 8
    //   1634: aaload
    //   1635: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   1638: checkcast 1723	android/view/ViewGroup
    //   1641: astore 8
    //   1643: goto -988 -> 655
    //   1646: new 1251	android/widget/FrameLayout
    //   1649: dup
    //   1650: aload_1
    //   1651: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   1654: astore 9
    //   1656: aload_0
    //   1657: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   1660: aload 9
    //   1662: iconst_m1
    //   1663: bipush 48
    //   1665: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   1668: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1671: aload 9
    //   1673: ldc_w 1710
    //   1676: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   1679: invokevirtual 1724	android/view/ViewGroup:setBackgroundColor	(I)V
    //   1682: iload 5
    //   1684: iconst_5
    //   1685: if_icmpeq +107 -> 1792
    //   1688: iload 5
    //   1690: bipush 9
    //   1692: if_icmpeq +100 -> 1792
    //   1695: iconst_1
    //   1696: istore_3
    //   1697: iload_3
    //   1698: istore 4
    //   1700: iload_3
    //   1701: ifeq +26 -> 1727
    //   1704: iload 5
    //   1706: bipush 7
    //   1708: if_icmpne +89 -> 1797
    //   1711: aload_0
    //   1712: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1715: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1718: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   1721: ifne +76 -> 1797
    //   1724: iconst_0
    //   1725: istore 4
    //   1727: aload 9
    //   1729: astore 8
    //   1731: iload 4
    //   1733: ifeq -1078 -> 655
    //   1736: new 1197	android/view/View
    //   1739: dup
    //   1740: aload_1
    //   1741: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   1744: astore 8
    //   1746: aload_0
    //   1747: getfield 302	org/telegram/ui/PaymentFormActivity:dividers	Ljava/util/ArrayList;
    //   1750: aload 8
    //   1752: invokevirtual 1850	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   1755: pop
    //   1756: aload 8
    //   1758: ldc_w 1852
    //   1761: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   1764: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   1767: aload 9
    //   1769: aload 8
    //   1771: new 1655	android/widget/FrameLayout$LayoutParams
    //   1774: dup
    //   1775: iconst_m1
    //   1776: iconst_1
    //   1777: bipush 83
    //   1779: invokespecial 1855	android/widget/FrameLayout$LayoutParams:<init>	(III)V
    //   1782: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   1785: aload 9
    //   1787: astore 8
    //   1789: goto -1134 -> 655
    //   1792: iconst_0
    //   1793: istore_3
    //   1794: goto -97 -> 1697
    //   1797: iload_3
    //   1798: istore 4
    //   1800: iload 5
    //   1802: bipush 6
    //   1804: if_icmpne -77 -> 1727
    //   1807: iload_3
    //   1808: istore 4
    //   1810: aload_0
    //   1811: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1814: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1817: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   1820: ifne -93 -> 1727
    //   1823: iload_3
    //   1824: istore 4
    //   1826: aload_0
    //   1827: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1830: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   1833: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   1836: ifne -109 -> 1727
    //   1839: iconst_0
    //   1840: istore 4
    //   1842: goto -115 -> 1727
    //   1845: aload_0
    //   1846: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1849: iload 5
    //   1851: new 708	org/telegram/ui/Components/EditTextBoldCursor
    //   1854: dup
    //   1855: aload_1
    //   1856: invokespecial 1856	org/telegram/ui/Components/EditTextBoldCursor:<init>	(Landroid/content/Context;)V
    //   1859: aastore
    //   1860: goto -1183 -> 677
    //   1863: iload 5
    //   1865: bipush 7
    //   1867: if_icmpne +17 -> 1884
    //   1870: aload_0
    //   1871: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1874: iload 5
    //   1876: aaload
    //   1877: iconst_1
    //   1878: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   1881: goto -1027 -> 854
    //   1884: aload_0
    //   1885: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1888: iload 5
    //   1890: aaload
    //   1891: sipush 16385
    //   1894: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   1897: goto -1043 -> 854
    //   1900: aload_0
    //   1901: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1904: iload 5
    //   1906: aaload
    //   1907: ldc_w 1858
    //   1910: ldc_w 1859
    //   1913: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1916: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   1919: aload_0
    //   1920: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1923: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   1926: ifnull -1010 -> 916
    //   1929: aload_0
    //   1930: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1933: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   1936: getfield 767	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:name	Ljava/lang/String;
    //   1939: ifnull -1023 -> 916
    //   1942: aload_0
    //   1943: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1946: iload 5
    //   1948: aaload
    //   1949: aload_0
    //   1950: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1953: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   1956: getfield 767	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:name	Ljava/lang/String;
    //   1959: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   1962: goto -1046 -> 916
    //   1965: aload_0
    //   1966: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   1969: iload 5
    //   1971: aaload
    //   1972: ldc_w 1868
    //   1975: ldc_w 1869
    //   1978: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   1981: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   1984: aload_0
    //   1985: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1988: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   1991: ifnull -1075 -> 916
    //   1994: aload_0
    //   1995: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   1998: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2001: getfield 791	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:email	Ljava/lang/String;
    //   2004: ifnull -1088 -> 916
    //   2007: aload_0
    //   2008: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2011: iload 5
    //   2013: aaload
    //   2014: aload_0
    //   2015: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2018: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2021: getfield 791	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:email	Ljava/lang/String;
    //   2024: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2027: goto -1111 -> 916
    //   2030: aload_0
    //   2031: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2034: iload 5
    //   2036: aaload
    //   2037: ldc_w 1871
    //   2040: ldc_w 1872
    //   2043: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2046: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2049: aload_0
    //   2050: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2053: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2056: ifnull -1140 -> 916
    //   2059: aload_0
    //   2060: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2063: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2066: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2069: ifnull -1153 -> 916
    //   2072: aload_0
    //   2073: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2076: iload 5
    //   2078: aaload
    //   2079: aload_0
    //   2080: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2083: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2086: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2089: getfield 801	org/telegram/tgnet/TLRPC$TL_postAddress:street_line1	Ljava/lang/String;
    //   2092: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2095: goto -1179 -> 916
    //   2098: aload_0
    //   2099: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2102: iload 5
    //   2104: aaload
    //   2105: ldc_w 1874
    //   2108: ldc_w 1875
    //   2111: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2114: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2117: aload_0
    //   2118: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2121: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2124: ifnull -1208 -> 916
    //   2127: aload_0
    //   2128: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2131: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2134: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2137: ifnull -1221 -> 916
    //   2140: aload_0
    //   2141: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2144: iload 5
    //   2146: aaload
    //   2147: aload_0
    //   2148: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2151: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2154: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2157: getfield 804	org/telegram/tgnet/TLRPC$TL_postAddress:street_line2	Ljava/lang/String;
    //   2160: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2163: goto -1247 -> 916
    //   2166: aload_0
    //   2167: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2170: iload 5
    //   2172: aaload
    //   2173: ldc_w 1877
    //   2176: ldc_w 1878
    //   2179: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2182: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2185: aload_0
    //   2186: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2189: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2192: ifnull -1276 -> 916
    //   2195: aload_0
    //   2196: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2199: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2202: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2205: ifnull -1289 -> 916
    //   2208: aload_0
    //   2209: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2212: iload 5
    //   2214: aaload
    //   2215: aload_0
    //   2216: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2219: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2222: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2225: getfield 807	org/telegram/tgnet/TLRPC$TL_postAddress:city	Ljava/lang/String;
    //   2228: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2231: goto -1315 -> 916
    //   2234: aload_0
    //   2235: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2238: iload 5
    //   2240: aaload
    //   2241: ldc_w 1880
    //   2244: ldc_w 1881
    //   2247: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2250: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2253: aload_0
    //   2254: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2257: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2260: ifnull -1344 -> 916
    //   2263: aload_0
    //   2264: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2267: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2270: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2273: ifnull -1357 -> 916
    //   2276: aload_0
    //   2277: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2280: iload 5
    //   2282: aaload
    //   2283: aload_0
    //   2284: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2287: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2290: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2293: getfield 810	org/telegram/tgnet/TLRPC$TL_postAddress:state	Ljava/lang/String;
    //   2296: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2299: goto -1383 -> 916
    //   2302: aload_0
    //   2303: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2306: iload 5
    //   2308: aaload
    //   2309: ldc_w 1883
    //   2312: ldc_w 1884
    //   2315: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2318: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2321: aload_0
    //   2322: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2325: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2328: ifnull -1412 -> 916
    //   2331: aload_0
    //   2332: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2335: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2338: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2341: ifnull -1425 -> 916
    //   2344: aload 11
    //   2346: aload_0
    //   2347: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2350: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2353: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2356: getfield 813	org/telegram/tgnet/TLRPC$TL_postAddress:country_iso2	Ljava/lang/String;
    //   2359: invokevirtual 1887	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   2362: checkcast 785	java/lang/String
    //   2365: astore 9
    //   2367: aload_0
    //   2368: aload_0
    //   2369: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2372: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2375: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2378: getfield 813	org/telegram/tgnet/TLRPC$TL_postAddress:country_iso2	Ljava/lang/String;
    //   2381: putfield 495	org/telegram/ui/PaymentFormActivity:countryName	Ljava/lang/String;
    //   2384: aload_0
    //   2385: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2388: iload 5
    //   2390: aaload
    //   2391: astore 12
    //   2393: aload 9
    //   2395: ifnull +13 -> 2408
    //   2398: aload 12
    //   2400: aload 9
    //   2402: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2405: goto -1489 -> 916
    //   2408: aload_0
    //   2409: getfield 495	org/telegram/ui/PaymentFormActivity:countryName	Ljava/lang/String;
    //   2412: astore 9
    //   2414: goto -16 -> 2398
    //   2417: aload_0
    //   2418: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2421: iload 5
    //   2423: aaload
    //   2424: ldc_w 1889
    //   2427: ldc_w 1890
    //   2430: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2433: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   2436: aload_0
    //   2437: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2440: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2443: ifnull -1527 -> 916
    //   2446: aload_0
    //   2447: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2450: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2453: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2456: ifnull -1540 -> 916
    //   2459: aload_0
    //   2460: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2463: iload 5
    //   2465: aaload
    //   2466: aload_0
    //   2467: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2470: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   2473: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   2476: getfield 816	org/telegram/tgnet/TLRPC$TL_postAddress:post_code	Ljava/lang/String;
    //   2479: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   2482: goto -1566 -> 916
    //   2485: iload 5
    //   2487: bipush 9
    //   2489: if_icmpne +78 -> 2567
    //   2492: aload_0
    //   2493: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2496: iload 5
    //   2498: aaload
    //   2499: iconst_0
    //   2500: iconst_0
    //   2501: iconst_0
    //   2502: iconst_0
    //   2503: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   2506: aload_0
    //   2507: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2510: iload 5
    //   2512: aaload
    //   2513: bipush 19
    //   2515: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   2518: aload 8
    //   2520: aload_0
    //   2521: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2524: iload 5
    //   2526: aaload
    //   2527: iconst_m1
    //   2528: bipush -2
    //   2530: fconst_0
    //   2531: ldc_w 1782
    //   2534: ldc_w 1781
    //   2537: ldc_w 1783
    //   2540: invokestatic 1786	org/telegram/ui/Components/LayoutHelper:createLinear	(IIFFFF)Landroid/widget/LinearLayout$LayoutParams;
    //   2543: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   2546: aload_0
    //   2547: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2550: iload 5
    //   2552: aaload
    //   2553: new 112	org/telegram/ui/PaymentFormActivity$5
    //   2556: dup
    //   2557: aload_0
    //   2558: invokespecial 1891	org/telegram/ui/PaymentFormActivity$5:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   2561: invokevirtual 1810	org/telegram/ui/Components/EditTextBoldCursor:addTextChangedListener	(Landroid/text/TextWatcher;)V
    //   2564: goto -1442 -> 1122
    //   2567: aload_0
    //   2568: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2571: iload 5
    //   2573: aaload
    //   2574: iconst_0
    //   2575: iconst_0
    //   2576: iconst_0
    //   2577: ldc_w 1783
    //   2580: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2583: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   2586: aload_0
    //   2587: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2590: iload 5
    //   2592: aaload
    //   2593: astore 9
    //   2595: getstatic 1894	org/telegram/messenger/LocaleController:isRTL	Z
    //   2598: ifeq +47 -> 2645
    //   2601: iconst_5
    //   2602: istore_3
    //   2603: aload 9
    //   2605: iload_3
    //   2606: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   2609: aload 8
    //   2611: aload_0
    //   2612: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   2615: iload 5
    //   2617: aaload
    //   2618: iconst_m1
    //   2619: ldc_w 1895
    //   2622: bipush 51
    //   2624: ldc_w 1781
    //   2627: ldc_w 1782
    //   2630: ldc_w 1781
    //   2633: ldc_w 1783
    //   2636: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   2639: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   2642: goto -1520 -> 1122
    //   2645: iconst_3
    //   2646: istore_3
    //   2647: goto -44 -> 2603
    //   2650: aload 8
    //   2652: ifnull +278 -> 2930
    //   2655: aload 8
    //   2657: getfield 373	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   2660: aload 8
    //   2662: getfield 1898	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   2665: invokestatic 1904	org/telegram/messenger/ContactsController:formatName	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   2668: astore 8
    //   2670: aload_0
    //   2671: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2674: iconst_1
    //   2675: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   2678: dup
    //   2679: aload_1
    //   2680: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   2683: aastore
    //   2684: aload_0
    //   2685: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2688: iconst_1
    //   2689: aaload
    //   2690: aload_1
    //   2691: ldc_w 1561
    //   2694: ldc_w 1550
    //   2697: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   2700: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   2703: aload_0
    //   2704: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   2707: aload_0
    //   2708: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2711: iconst_1
    //   2712: aaload
    //   2713: iconst_m1
    //   2714: bipush -2
    //   2716: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   2719: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   2722: aload_0
    //   2723: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2726: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   2729: getfield 1818	org/telegram/tgnet/TLRPC$TL_invoice:email_to_provider	Z
    //   2732: ifeq +206 -> 2938
    //   2735: aload_0
    //   2736: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2739: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   2742: getfield 1821	org/telegram/tgnet/TLRPC$TL_invoice:phone_to_provider	Z
    //   2745: ifeq +193 -> 2938
    //   2748: aload_0
    //   2749: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2752: iconst_1
    //   2753: aaload
    //   2754: ldc_w 1908
    //   2757: ldc_w 1909
    //   2760: iconst_1
    //   2761: anewarray 745	java/lang/Object
    //   2764: dup
    //   2765: iconst_0
    //   2766: aload 8
    //   2768: aastore
    //   2769: invokestatic 1462	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   2772: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   2775: aload_0
    //   2776: new 1541	org/telegram/ui/Cells/TextCheckCell
    //   2779: dup
    //   2780: aload_1
    //   2781: invokespecial 1910	org/telegram/ui/Cells/TextCheckCell:<init>	(Landroid/content/Context;)V
    //   2784: putfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   2787: aload_0
    //   2788: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   2791: iconst_1
    //   2792: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   2795: invokevirtual 1915	org/telegram/ui/Cells/TextCheckCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   2798: aload_0
    //   2799: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   2802: ldc_w 1917
    //   2805: ldc_w 1918
    //   2808: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2811: aload_0
    //   2812: getfield 520	org/telegram/ui/PaymentFormActivity:saveShippingInfo	Z
    //   2815: iconst_0
    //   2816: invokevirtual 1922	org/telegram/ui/Cells/TextCheckCell:setTextAndCheck	(Ljava/lang/String;ZZ)V
    //   2819: aload_0
    //   2820: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   2823: aload_0
    //   2824: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   2827: iconst_m1
    //   2828: bipush -2
    //   2830: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   2833: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   2836: aload_0
    //   2837: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   2840: new 116	org/telegram/ui/PaymentFormActivity$7
    //   2843: dup
    //   2844: aload_0
    //   2845: invokespecial 1923	org/telegram/ui/PaymentFormActivity$7:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   2848: invokevirtual 1927	org/telegram/ui/Cells/TextCheckCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   2851: aload_0
    //   2852: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2855: iconst_0
    //   2856: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   2859: dup
    //   2860: aload_1
    //   2861: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   2864: aastore
    //   2865: aload_0
    //   2866: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2869: iconst_0
    //   2870: aaload
    //   2871: aload_1
    //   2872: ldc_w 1561
    //   2875: ldc_w 1550
    //   2878: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   2881: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   2884: aload_0
    //   2885: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2888: iconst_0
    //   2889: aaload
    //   2890: ldc_w 1929
    //   2893: ldc_w 1930
    //   2896: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2899: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   2902: aload_0
    //   2903: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   2906: aload_0
    //   2907: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2910: iconst_0
    //   2911: aaload
    //   2912: iconst_m1
    //   2913: bipush -2
    //   2915: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   2918: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   2921: iload 5
    //   2923: iconst_1
    //   2924: iadd
    //   2925: istore 5
    //   2927: goto -2402 -> 525
    //   2930: ldc_w 411
    //   2933: astore 8
    //   2935: goto -265 -> 2670
    //   2938: aload_0
    //   2939: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   2942: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   2945: getfield 1818	org/telegram/tgnet/TLRPC$TL_invoice:email_to_provider	Z
    //   2948: ifeq +33 -> 2981
    //   2951: aload_0
    //   2952: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2955: iconst_1
    //   2956: aaload
    //   2957: ldc_w 1932
    //   2960: ldc_w 1933
    //   2963: iconst_1
    //   2964: anewarray 745	java/lang/Object
    //   2967: dup
    //   2968: iconst_0
    //   2969: aload 8
    //   2971: aastore
    //   2972: invokestatic 1462	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   2975: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   2978: goto -203 -> 2775
    //   2981: aload_0
    //   2982: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   2985: iconst_1
    //   2986: aaload
    //   2987: ldc_w 1935
    //   2990: ldc_w 1936
    //   2993: iconst_1
    //   2994: anewarray 745	java/lang/Object
    //   2997: dup
    //   2998: iconst_0
    //   2999: aload 8
    //   3001: aastore
    //   3002: invokestatic 1462	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   3005: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   3008: goto -233 -> 2775
    //   3011: aload_0
    //   3012: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   3015: iconst_1
    //   3016: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   3019: dup
    //   3020: aload_1
    //   3021: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   3024: aastore
    //   3025: aload_0
    //   3026: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   3029: aload_0
    //   3030: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   3033: iconst_1
    //   3034: aaload
    //   3035: iconst_m1
    //   3036: bipush -2
    //   3038: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   3041: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   3044: goto -269 -> 2775
    //   3047: aload_0
    //   3048: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3051: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3054: getfield 424	org/telegram/tgnet/TLRPC$TL_invoice:name_requested	Z
    //   3057: ifne +21 -> 3078
    //   3060: aload_0
    //   3061: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3064: bipush 6
    //   3066: aaload
    //   3067: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3070: checkcast 1723	android/view/ViewGroup
    //   3073: bipush 8
    //   3075: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3078: aload_0
    //   3079: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3082: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3085: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3088: ifne +21 -> 3109
    //   3091: aload_0
    //   3092: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3095: bipush 8
    //   3097: aaload
    //   3098: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3101: checkcast 1723	android/view/ViewGroup
    //   3104: bipush 8
    //   3106: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3109: aload_0
    //   3110: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3113: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3116: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   3119: ifne +21 -> 3140
    //   3122: aload_0
    //   3123: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3126: bipush 7
    //   3128: aaload
    //   3129: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3132: checkcast 1723	android/view/ViewGroup
    //   3135: bipush 8
    //   3137: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3140: aload_0
    //   3141: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3144: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3147: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3150: ifeq +450 -> 3600
    //   3153: aload_0
    //   3154: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3157: bipush 9
    //   3159: aaload
    //   3160: ldc_w 1938
    //   3163: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   3166: aload_0
    //   3167: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   3170: iconst_1
    //   3171: aaload
    //   3172: ifnull +507 -> 3679
    //   3175: aload_0
    //   3176: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   3179: iconst_1
    //   3180: aaload
    //   3181: astore_1
    //   3182: aload_0
    //   3183: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3186: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3189: getfield 424	org/telegram/tgnet/TLRPC$TL_invoice:name_requested	Z
    //   3192: ifne +29 -> 3221
    //   3195: aload_0
    //   3196: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3199: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3202: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3205: ifne +16 -> 3221
    //   3208: aload_0
    //   3209: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3212: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3215: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   3218: ifeq +455 -> 3673
    //   3221: iconst_0
    //   3222: istore_3
    //   3223: aload_1
    //   3224: iload_3
    //   3225: invokevirtual 1939	org/telegram/ui/Cells/ShadowSectionCell:setVisibility	(I)V
    //   3228: aload_0
    //   3229: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   3232: iconst_1
    //   3233: aaload
    //   3234: astore_1
    //   3235: aload_0
    //   3236: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3239: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3242: getfield 424	org/telegram/tgnet/TLRPC$TL_invoice:name_requested	Z
    //   3245: ifne +29 -> 3274
    //   3248: aload_0
    //   3249: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3252: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3255: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3258: ifne +16 -> 3274
    //   3261: aload_0
    //   3262: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3265: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3268: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   3271: ifeq +479 -> 3750
    //   3274: iconst_0
    //   3275: istore_3
    //   3276: aload_1
    //   3277: iload_3
    //   3278: invokevirtual 1476	org/telegram/ui/Cells/HeaderCell:setVisibility	(I)V
    //   3281: aload_0
    //   3282: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3285: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3288: getfield 418	org/telegram/tgnet/TLRPC$TL_invoice:shipping_address_requested	Z
    //   3291: ifne +127 -> 3418
    //   3294: aload_0
    //   3295: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   3298: iconst_0
    //   3299: aaload
    //   3300: bipush 8
    //   3302: invokevirtual 1476	org/telegram/ui/Cells/HeaderCell:setVisibility	(I)V
    //   3305: aload_0
    //   3306: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   3309: iconst_0
    //   3310: aaload
    //   3311: bipush 8
    //   3313: invokevirtual 1939	org/telegram/ui/Cells/ShadowSectionCell:setVisibility	(I)V
    //   3316: aload_0
    //   3317: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3320: iconst_0
    //   3321: aaload
    //   3322: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3325: checkcast 1723	android/view/ViewGroup
    //   3328: bipush 8
    //   3330: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3333: aload_0
    //   3334: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3337: iconst_1
    //   3338: aaload
    //   3339: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3342: checkcast 1723	android/view/ViewGroup
    //   3345: bipush 8
    //   3347: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3350: aload_0
    //   3351: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3354: iconst_2
    //   3355: aaload
    //   3356: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3359: checkcast 1723	android/view/ViewGroup
    //   3362: bipush 8
    //   3364: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3367: aload_0
    //   3368: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3371: iconst_3
    //   3372: aaload
    //   3373: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3376: checkcast 1723	android/view/ViewGroup
    //   3379: bipush 8
    //   3381: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3384: aload_0
    //   3385: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3388: iconst_4
    //   3389: aaload
    //   3390: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3393: checkcast 1723	android/view/ViewGroup
    //   3396: bipush 8
    //   3398: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3401: aload_0
    //   3402: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3405: iconst_5
    //   3406: aaload
    //   3407: invokevirtual 1480	org/telegram/ui/Components/EditTextBoldCursor:getParent	()Landroid/view/ViewParent;
    //   3410: checkcast 1723	android/view/ViewGroup
    //   3413: bipush 8
    //   3415: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   3418: aload_0
    //   3419: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3422: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   3425: ifnull +331 -> 3756
    //   3428: aload_0
    //   3429: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3432: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   3435: getfield 783	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:phone	Ljava/lang/String;
    //   3438: invokestatic 1124	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   3441: ifne +315 -> 3756
    //   3444: aload_0
    //   3445: aload_0
    //   3446: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3449: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   3452: getfield 783	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:phone	Ljava/lang/String;
    //   3455: invokevirtual 1942	org/telegram/ui/PaymentFormActivity:fillNumber	(Ljava/lang/String;)V
    //   3458: aload_0
    //   3459: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3462: bipush 8
    //   3464: aaload
    //   3465: invokevirtual 711	org/telegram/ui/Components/EditTextBoldCursor:length	()I
    //   3468: ifne +127 -> 3595
    //   3471: aload_0
    //   3472: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3475: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3478: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3481: ifeq +114 -> 3595
    //   3484: aload_0
    //   3485: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3488: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   3491: ifnull +19 -> 3510
    //   3494: aload_0
    //   3495: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3498: getfield 1865	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   3501: getfield 783	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:phone	Ljava/lang/String;
    //   3504: invokestatic 1124	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   3507: ifeq +88 -> 3595
    //   3510: aconst_null
    //   3511: astore 8
    //   3513: getstatic 717	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   3516: ldc_w 1943
    //   3519: invokevirtual 725	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
    //   3522: checkcast 1945	android/telephony/TelephonyManager
    //   3525: astore 9
    //   3527: aload 8
    //   3529: astore_1
    //   3530: aload 9
    //   3532: ifnull +12 -> 3544
    //   3535: aload 9
    //   3537: invokevirtual 1948	android/telephony/TelephonyManager:getSimCountryIso	()Ljava/lang/String;
    //   3540: invokevirtual 1951	java/lang/String:toUpperCase	()Ljava/lang/String;
    //   3543: astore_1
    //   3544: aload_1
    //   3545: ifnull +50 -> 3595
    //   3548: aload 10
    //   3550: aload_1
    //   3551: invokevirtual 1887	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   3554: checkcast 785	java/lang/String
    //   3557: astore_1
    //   3558: aload_1
    //   3559: ifnull +36 -> 3595
    //   3562: aload_0
    //   3563: getfield 287	org/telegram/ui/PaymentFormActivity:countriesArray	Ljava/util/ArrayList;
    //   3566: aload_1
    //   3567: invokevirtual 1954	java/util/ArrayList:indexOf	(Ljava/lang/Object;)I
    //   3570: iconst_m1
    //   3571: if_icmpeq +24 -> 3595
    //   3574: aload_0
    //   3575: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3578: bipush 8
    //   3580: aaload
    //   3581: aload_0
    //   3582: getfield 292	org/telegram/ui/PaymentFormActivity:countriesMap	Ljava/util/HashMap;
    //   3585: aload_1
    //   3586: invokevirtual 1887	java/util/HashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   3589: checkcast 1956	java/lang/CharSequence
    //   3592: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   3595: aload_0
    //   3596: getfield 1619	org/telegram/ui/PaymentFormActivity:fragmentView	Landroid/view/View;
    //   3599: areturn
    //   3600: aload_0
    //   3601: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3604: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3607: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   3610: ifeq +19 -> 3629
    //   3613: aload_0
    //   3614: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3617: bipush 7
    //   3619: aaload
    //   3620: ldc_w 1938
    //   3623: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   3626: goto -460 -> 3166
    //   3629: aload_0
    //   3630: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3633: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3636: getfield 424	org/telegram/tgnet/TLRPC$TL_invoice:name_requested	Z
    //   3639: ifeq +19 -> 3658
    //   3642: aload_0
    //   3643: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3646: bipush 6
    //   3648: aaload
    //   3649: ldc_w 1938
    //   3652: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   3655: goto -489 -> 3166
    //   3658: aload_0
    //   3659: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   3662: iconst_5
    //   3663: aaload
    //   3664: ldc_w 1938
    //   3667: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   3670: goto -504 -> 3166
    //   3673: bipush 8
    //   3675: istore_3
    //   3676: goto -453 -> 3223
    //   3679: aload_0
    //   3680: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   3683: iconst_1
    //   3684: aaload
    //   3685: ifnull -457 -> 3228
    //   3688: aload_0
    //   3689: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   3692: iconst_1
    //   3693: aaload
    //   3694: astore_1
    //   3695: aload_0
    //   3696: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3699: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3702: getfield 424	org/telegram/tgnet/TLRPC$TL_invoice:name_requested	Z
    //   3705: ifne +29 -> 3734
    //   3708: aload_0
    //   3709: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3712: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3715: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   3718: ifne +16 -> 3734
    //   3721: aload_0
    //   3722: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3725: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   3728: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   3731: ifeq +13 -> 3744
    //   3734: iconst_0
    //   3735: istore_3
    //   3736: aload_1
    //   3737: iload_3
    //   3738: invokevirtual 1470	org/telegram/ui/Cells/TextInfoPrivacyCell:setVisibility	(I)V
    //   3741: goto -513 -> 3228
    //   3744: bipush 8
    //   3746: istore_3
    //   3747: goto -11 -> 3736
    //   3750: bipush 8
    //   3752: istore_3
    //   3753: goto -477 -> 3276
    //   3756: aload_0
    //   3757: aconst_null
    //   3758: invokevirtual 1942	org/telegram/ui/PaymentFormActivity:fillNumber	(Ljava/lang/String;)V
    //   3761: goto -303 -> 3458
    //   3764: astore_1
    //   3765: aload_1
    //   3766: invokestatic 1032	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   3769: aload 8
    //   3771: astore_1
    //   3772: goto -228 -> 3544
    //   3775: aload_0
    //   3776: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   3779: iconst_2
    //   3780: if_icmpne +1966 -> 5746
    //   3783: aload_0
    //   3784: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3787: getfield 1959	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:native_params	Lorg/telegram/tgnet/TLRPC$TL_dataJSON;
    //   3790: ifnull +74 -> 3864
    //   3793: new 1961	org/json/JSONObject
    //   3796: dup
    //   3797: aload_0
    //   3798: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   3801: getfield 1959	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:native_params	Lorg/telegram/tgnet/TLRPC$TL_dataJSON;
    //   3804: getfield 1090	org/telegram/tgnet/TLRPC$TL_dataJSON:data	Ljava/lang/String;
    //   3807: invokespecial 1962	org/json/JSONObject:<init>	(Ljava/lang/String;)V
    //   3810: astore 8
    //   3812: aload 8
    //   3814: ldc_w 1964
    //   3817: invokevirtual 1967	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   3820: astore 9
    //   3822: aload 9
    //   3824: invokestatic 1124	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   3827: ifne +9 -> 3836
    //   3830: aload_0
    //   3831: aload 9
    //   3833: putfield 1247	org/telegram/ui/PaymentFormActivity:androidPayPublicKey	Ljava/lang/String;
    //   3836: aload_0
    //   3837: aload 8
    //   3839: ldc_w 1969
    //   3842: invokevirtual 1972	org/json/JSONObject:getInt	(Ljava/lang/String;)I
    //   3845: ldc_w 1973
    //   3848: ior
    //   3849: putfield 1249	org/telegram/ui/PaymentFormActivity:androidPayBackgroundColor	I
    //   3852: aload_0
    //   3853: aload 8
    //   3855: ldc_w 1975
    //   3858: invokevirtual 1979	org/json/JSONObject:getBoolean	(Ljava/lang/String;)Z
    //   3861: putfield 1263	org/telegram/ui/PaymentFormActivity:androidPayBlackTheme	Z
    //   3864: aload_0
    //   3865: getfield 635	org/telegram/ui/PaymentFormActivity:isWebView	Z
    //   3868: ifeq +437 -> 4305
    //   3871: aload_0
    //   3872: getfield 1247	org/telegram/ui/PaymentFormActivity:androidPayPublicKey	Ljava/lang/String;
    //   3875: ifnull +8 -> 3883
    //   3878: aload_0
    //   3879: aload_1
    //   3880: invokespecial 1981	org/telegram/ui/PaymentFormActivity:initAndroidPay	(Landroid/content/Context;)V
    //   3883: aload_0
    //   3884: new 1251	android/widget/FrameLayout
    //   3887: dup
    //   3888: aload_1
    //   3889: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   3892: putfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   3895: aload_0
    //   3896: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   3899: sipush 4000
    //   3902: invokevirtual 1984	android/widget/FrameLayout:setId	(I)V
    //   3905: aload_0
    //   3906: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   3909: iconst_1
    //   3910: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   3913: invokevirtual 1985	android/widget/FrameLayout:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   3916: aload_0
    //   3917: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   3920: bipush 8
    //   3922: invokevirtual 1377	android/widget/FrameLayout:setVisibility	(I)V
    //   3925: aload_0
    //   3926: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   3929: aload_0
    //   3930: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   3933: iconst_m1
    //   3934: bipush 48
    //   3936: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   3939: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   3942: aload_0
    //   3943: iconst_1
    //   3944: putfield 528	org/telegram/ui/PaymentFormActivity:webviewLoading	Z
    //   3947: aload_0
    //   3948: iconst_1
    //   3949: iconst_1
    //   3950: invokespecial 534	org/telegram/ui/PaymentFormActivity:showEditDoneProgress	(ZZ)V
    //   3953: aload_0
    //   3954: getfield 673	org/telegram/ui/PaymentFormActivity:progressView	Lorg/telegram/ui/Components/ContextProgressView;
    //   3957: iconst_0
    //   3958: invokevirtual 1424	org/telegram/ui/Components/ContextProgressView:setVisibility	(I)V
    //   3961: aload_0
    //   3962: getfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   3965: iconst_0
    //   3966: invokevirtual 1425	org/telegram/ui/ActionBar/ActionBarMenuItem:setEnabled	(Z)V
    //   3969: aload_0
    //   3970: getfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   3973: invokevirtual 1429	org/telegram/ui/ActionBar/ActionBarMenuItem:getImageView	()Landroid/widget/ImageView;
    //   3976: iconst_4
    //   3977: invokevirtual 1445	android/widget/ImageView:setVisibility	(I)V
    //   3980: aload_0
    //   3981: new 118	org/telegram/ui/PaymentFormActivity$8
    //   3984: dup
    //   3985: aload_0
    //   3986: aload_1
    //   3987: invokespecial 1988	org/telegram/ui/PaymentFormActivity$8:<init>	(Lorg/telegram/ui/PaymentFormActivity;Landroid/content/Context;)V
    //   3990: putfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   3993: aload_0
    //   3994: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   3997: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   4000: iconst_1
    //   4001: invokevirtual 1999	android/webkit/WebSettings:setJavaScriptEnabled	(Z)V
    //   4004: aload_0
    //   4005: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4008: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   4011: iconst_1
    //   4012: invokevirtual 2002	android/webkit/WebSettings:setDomStorageEnabled	(Z)V
    //   4015: getstatic 900	android/os/Build$VERSION:SDK_INT	I
    //   4018: bipush 21
    //   4020: if_icmplt +25 -> 4045
    //   4023: aload_0
    //   4024: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4027: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   4030: iconst_0
    //   4031: invokevirtual 2005	android/webkit/WebSettings:setMixedContentMode	(I)V
    //   4034: invokestatic 2010	android/webkit/CookieManager:getInstance	()Landroid/webkit/CookieManager;
    //   4037: aload_0
    //   4038: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4041: iconst_1
    //   4042: invokevirtual 2014	android/webkit/CookieManager:setAcceptThirdPartyCookies	(Landroid/webkit/WebView;Z)V
    //   4045: aload_0
    //   4046: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4049: new 131	org/telegram/ui/PaymentFormActivity$TelegramWebviewProxy
    //   4052: dup
    //   4053: aload_0
    //   4054: aconst_null
    //   4055: invokespecial 2017	org/telegram/ui/PaymentFormActivity$TelegramWebviewProxy:<init>	(Lorg/telegram/ui/PaymentFormActivity;Lorg/telegram/ui/PaymentFormActivity$1;)V
    //   4058: ldc_w 2018
    //   4061: invokevirtual 2022	android/webkit/WebView:addJavascriptInterface	(Ljava/lang/Object;Ljava/lang/String;)V
    //   4064: aload_0
    //   4065: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4068: new 120	org/telegram/ui/PaymentFormActivity$9
    //   4071: dup
    //   4072: aload_0
    //   4073: invokespecial 2023	org/telegram/ui/PaymentFormActivity$9:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   4076: invokevirtual 2027	android/webkit/WebView:setWebViewClient	(Landroid/webkit/WebViewClient;)V
    //   4079: aload_0
    //   4080: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4083: aload_0
    //   4084: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   4087: iconst_m1
    //   4088: ldc_w 1895
    //   4091: invokestatic 1610	org/telegram/ui/Components/LayoutHelper:createFrame	(IF)Landroid/widget/FrameLayout$LayoutParams;
    //   4094: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4097: aload_0
    //   4098: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   4101: iconst_2
    //   4102: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   4105: dup
    //   4106: aload_1
    //   4107: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   4110: aastore
    //   4111: aload_0
    //   4112: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4115: aload_0
    //   4116: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   4119: iconst_2
    //   4120: aaload
    //   4121: iconst_m1
    //   4122: bipush -2
    //   4124: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4127: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4130: aload_0
    //   4131: new 1541	org/telegram/ui/Cells/TextCheckCell
    //   4134: dup
    //   4135: aload_1
    //   4136: invokespecial 1910	org/telegram/ui/Cells/TextCheckCell:<init>	(Landroid/content/Context;)V
    //   4139: putfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   4142: aload_0
    //   4143: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   4146: iconst_1
    //   4147: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   4150: invokevirtual 1915	org/telegram/ui/Cells/TextCheckCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   4153: aload_0
    //   4154: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   4157: ldc_w 2029
    //   4160: ldc_w 2030
    //   4163: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   4166: aload_0
    //   4167: getfield 541	org/telegram/ui/PaymentFormActivity:saveCardInfo	Z
    //   4170: iconst_0
    //   4171: invokevirtual 1922	org/telegram/ui/Cells/TextCheckCell:setTextAndCheck	(Ljava/lang/String;ZZ)V
    //   4174: aload_0
    //   4175: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4178: aload_0
    //   4179: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   4182: iconst_m1
    //   4183: bipush -2
    //   4185: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4188: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4191: aload_0
    //   4192: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   4195: new 10	org/telegram/ui/PaymentFormActivity$10
    //   4198: dup
    //   4199: aload_0
    //   4200: invokespecial 2031	org/telegram/ui/PaymentFormActivity$10:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   4203: invokevirtual 1927	org/telegram/ui/Cells/TextCheckCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   4206: aload_0
    //   4207: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   4210: iconst_0
    //   4211: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   4214: dup
    //   4215: aload_1
    //   4216: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   4219: aastore
    //   4220: aload_0
    //   4221: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   4224: iconst_0
    //   4225: aaload
    //   4226: aload_1
    //   4227: ldc_w 1561
    //   4230: ldc_w 1550
    //   4233: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   4236: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   4239: aload_0
    //   4240: invokespecial 538	org/telegram/ui/PaymentFormActivity:updateSavePaymentField	()V
    //   4243: aload_0
    //   4244: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4247: aload_0
    //   4248: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   4251: iconst_0
    //   4252: aaload
    //   4253: iconst_m1
    //   4254: bipush -2
    //   4256: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4259: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4262: goto -667 -> 3595
    //   4265: astore 9
    //   4267: aload_0
    //   4268: aconst_null
    //   4269: putfield 1247	org/telegram/ui/PaymentFormActivity:androidPayPublicKey	Ljava/lang/String;
    //   4272: goto -436 -> 3836
    //   4275: astore 8
    //   4277: aload 8
    //   4279: invokestatic 1032	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   4282: goto -418 -> 3864
    //   4285: astore 9
    //   4287: aload_0
    //   4288: iconst_m1
    //   4289: putfield 1249	org/telegram/ui/PaymentFormActivity:androidPayBackgroundColor	I
    //   4292: goto -440 -> 3852
    //   4295: astore 8
    //   4297: aload_0
    //   4298: iconst_0
    //   4299: putfield 1263	org/telegram/ui/PaymentFormActivity:androidPayBlackTheme	Z
    //   4302: goto -438 -> 3864
    //   4305: aload_0
    //   4306: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   4309: getfield 1959	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:native_params	Lorg/telegram/tgnet/TLRPC$TL_dataJSON;
    //   4312: ifnull +70 -> 4382
    //   4315: new 1961	org/json/JSONObject
    //   4318: dup
    //   4319: aload_0
    //   4320: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   4323: getfield 1959	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:native_params	Lorg/telegram/tgnet/TLRPC$TL_dataJSON;
    //   4326: getfield 1090	org/telegram/tgnet/TLRPC$TL_dataJSON:data	Ljava/lang/String;
    //   4329: invokespecial 1962	org/json/JSONObject:<init>	(Ljava/lang/String;)V
    //   4332: astore 8
    //   4334: aload_0
    //   4335: aload 8
    //   4337: ldc_w 2033
    //   4340: invokevirtual 1979	org/json/JSONObject:getBoolean	(Ljava/lang/String;)Z
    //   4343: putfield 1013	org/telegram/ui/PaymentFormActivity:need_card_country	Z
    //   4346: aload_0
    //   4347: aload 8
    //   4349: ldc_w 2035
    //   4352: invokevirtual 1979	org/json/JSONObject:getBoolean	(Ljava/lang/String;)Z
    //   4355: putfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   4358: aload_0
    //   4359: aload 8
    //   4361: ldc_w 2037
    //   4364: invokevirtual 1979	org/json/JSONObject:getBoolean	(Ljava/lang/String;)Z
    //   4367: putfield 549	org/telegram/ui/PaymentFormActivity:need_card_name	Z
    //   4370: aload_0
    //   4371: aload 8
    //   4373: ldc_w 2039
    //   4376: invokevirtual 1967	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
    //   4379: putfield 1019	org/telegram/ui/PaymentFormActivity:stripeApiKey	Ljava/lang/String;
    //   4382: aload_0
    //   4383: aload_1
    //   4384: invokespecial 1981	org/telegram/ui/PaymentFormActivity:initAndroidPay	(Landroid/content/Context;)V
    //   4387: aload_0
    //   4388: bipush 6
    //   4390: anewarray 708	org/telegram/ui/Components/EditTextBoldCursor
    //   4393: putfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4396: iconst_0
    //   4397: istore_3
    //   4398: iload_3
    //   4399: bipush 6
    //   4401: if_icmpge +1272 -> 5673
    //   4404: iload_3
    //   4405: ifne +677 -> 5082
    //   4408: aload_0
    //   4409: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   4412: iconst_0
    //   4413: new 298	org/telegram/ui/Cells/HeaderCell
    //   4416: dup
    //   4417: aload_1
    //   4418: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   4421: aastore
    //   4422: aload_0
    //   4423: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   4426: iconst_0
    //   4427: aaload
    //   4428: ldc_w 1710
    //   4431: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4434: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   4437: aload_0
    //   4438: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   4441: iconst_0
    //   4442: aaload
    //   4443: ldc_w 2041
    //   4446: ldc_w 2042
    //   4449: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   4452: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   4455: aload_0
    //   4456: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4459: aload_0
    //   4460: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   4463: iconst_0
    //   4464: aaload
    //   4465: iconst_m1
    //   4466: bipush -2
    //   4468: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4471: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4474: iload_3
    //   4475: iconst_3
    //   4476: if_icmpeq +680 -> 5156
    //   4479: iload_3
    //   4480: iconst_5
    //   4481: if_icmpeq +675 -> 5156
    //   4484: iload_3
    //   4485: iconst_4
    //   4486: if_icmpne +10 -> 4496
    //   4489: aload_0
    //   4490: getfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   4493: ifeq +663 -> 5156
    //   4496: iconst_1
    //   4497: istore 4
    //   4499: new 1251	android/widget/FrameLayout
    //   4502: dup
    //   4503: aload_1
    //   4504: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   4507: astore 8
    //   4509: aload_0
    //   4510: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4513: aload 8
    //   4515: iconst_m1
    //   4516: bipush 48
    //   4518: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4521: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4524: aload 8
    //   4526: ldc_w 1710
    //   4529: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4532: invokevirtual 1724	android/view/ViewGroup:setBackgroundColor	(I)V
    //   4535: aload_0
    //   4536: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4539: iload_3
    //   4540: new 708	org/telegram/ui/Components/EditTextBoldCursor
    //   4543: dup
    //   4544: aload_1
    //   4545: invokespecial 1856	org/telegram/ui/Components/EditTextBoldCursor:<init>	(Landroid/content/Context;)V
    //   4548: aastore
    //   4549: aload_0
    //   4550: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4553: iload_3
    //   4554: aaload
    //   4555: iload_3
    //   4556: invokestatic 362	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   4559: invokevirtual 1731	org/telegram/ui/Components/EditTextBoldCursor:setTag	(Ljava/lang/Object;)V
    //   4562: aload_0
    //   4563: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4566: iload_3
    //   4567: aaload
    //   4568: iconst_1
    //   4569: ldc_w 1732
    //   4572: invokevirtual 1736	org/telegram/ui/Components/EditTextBoldCursor:setTextSize	(IF)V
    //   4575: aload_0
    //   4576: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4579: iload_3
    //   4580: aaload
    //   4581: ldc_w 1738
    //   4584: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4587: invokevirtual 1741	org/telegram/ui/Components/EditTextBoldCursor:setHintTextColor	(I)V
    //   4590: aload_0
    //   4591: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4594: iload_3
    //   4595: aaload
    //   4596: ldc_w 1743
    //   4599: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4602: invokevirtual 1746	org/telegram/ui/Components/EditTextBoldCursor:setTextColor	(I)V
    //   4605: aload_0
    //   4606: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4609: iload_3
    //   4610: aaload
    //   4611: aconst_null
    //   4612: invokevirtual 1747	org/telegram/ui/Components/EditTextBoldCursor:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   4615: aload_0
    //   4616: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4619: iload_3
    //   4620: aaload
    //   4621: ldc_w 1743
    //   4624: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4627: invokevirtual 1750	org/telegram/ui/Components/EditTextBoldCursor:setCursorColor	(I)V
    //   4630: aload_0
    //   4631: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4634: iload_3
    //   4635: aaload
    //   4636: ldc_w 1751
    //   4639: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4642: invokevirtual 1754	org/telegram/ui/Components/EditTextBoldCursor:setCursorSize	(I)V
    //   4645: aload_0
    //   4646: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4649: iload_3
    //   4650: aaload
    //   4651: ldc_w 1755
    //   4654: invokevirtual 1759	org/telegram/ui/Components/EditTextBoldCursor:setCursorWidth	(F)V
    //   4657: iload_3
    //   4658: iconst_3
    //   4659: if_icmpne +503 -> 5162
    //   4662: new 1797	android/text/InputFilter$LengthFilter
    //   4665: dup
    //   4666: iconst_3
    //   4667: invokespecial 1799	android/text/InputFilter$LengthFilter:<init>	(I)V
    //   4670: astore 9
    //   4672: aload_0
    //   4673: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4676: iload_3
    //   4677: aaload
    //   4678: iconst_1
    //   4679: anewarray 1801	android/text/InputFilter
    //   4682: dup
    //   4683: iconst_0
    //   4684: aload 9
    //   4686: aastore
    //   4687: invokevirtual 1805	org/telegram/ui/Components/EditTextBoldCursor:setFilters	([Landroid/text/InputFilter;)V
    //   4690: aload_0
    //   4691: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4694: iload_3
    //   4695: aaload
    //   4696: sipush 130
    //   4699: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   4702: aload_0
    //   4703: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4706: iload_3
    //   4707: aaload
    //   4708: getstatic 2048	android/graphics/Typeface:DEFAULT	Landroid/graphics/Typeface;
    //   4711: invokevirtual 2052	org/telegram/ui/Components/EditTextBoldCursor:setTypeface	(Landroid/graphics/Typeface;)V
    //   4714: aload_0
    //   4715: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4718: iload_3
    //   4719: aaload
    //   4720: invokestatic 2057	android/text/method/PasswordTransformationMethod:getInstance	()Landroid/text/method/PasswordTransformationMethod;
    //   4723: invokevirtual 2061	org/telegram/ui/Components/EditTextBoldCursor:setTransformationMethod	(Landroid/text/method/TransformationMethod;)V
    //   4726: aload_0
    //   4727: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4730: iload_3
    //   4731: aaload
    //   4732: ldc_w 1768
    //   4735: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   4738: iload_3
    //   4739: tableswitch	default:+37->4776, 0:+530->5269, 1:+572->5311, 2:+593->5332, 3:+551->5290, 4:+635->5374, 5:+614->5353
    //   4776: iload_3
    //   4777: ifne +618 -> 5395
    //   4780: aload_0
    //   4781: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4784: iload_3
    //   4785: aaload
    //   4786: new 16	org/telegram/ui/PaymentFormActivity$12
    //   4789: dup
    //   4790: aload_0
    //   4791: invokespecial 2062	org/telegram/ui/PaymentFormActivity$12:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   4794: invokevirtual 1810	org/telegram/ui/Components/EditTextBoldCursor:addTextChangedListener	(Landroid/text/TextWatcher;)V
    //   4797: aload_0
    //   4798: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4801: iload_3
    //   4802: aaload
    //   4803: iconst_0
    //   4804: iconst_0
    //   4805: iconst_0
    //   4806: ldc_w 1783
    //   4809: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   4812: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   4815: aload_0
    //   4816: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4819: iload_3
    //   4820: aaload
    //   4821: astore 9
    //   4823: getstatic 1894	org/telegram/messenger/LocaleController:isRTL	Z
    //   4826: ifeq +594 -> 5420
    //   4829: iconst_5
    //   4830: istore 5
    //   4832: aload 9
    //   4834: iload 5
    //   4836: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   4839: aload 8
    //   4841: aload_0
    //   4842: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4845: iload_3
    //   4846: aaload
    //   4847: iconst_m1
    //   4848: ldc_w 1895
    //   4851: bipush 51
    //   4853: ldc_w 1781
    //   4856: ldc_w 1782
    //   4859: ldc_w 1781
    //   4862: ldc_w 1783
    //   4865: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   4868: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4871: aload_0
    //   4872: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   4875: iload_3
    //   4876: aaload
    //   4877: new 20	org/telegram/ui/PaymentFormActivity$14
    //   4880: dup
    //   4881: aload_0
    //   4882: invokespecial 2063	org/telegram/ui/PaymentFormActivity$14:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   4885: invokevirtual 1815	org/telegram/ui/Components/EditTextBoldCursor:setOnEditorActionListener	(Landroid/widget/TextView$OnEditorActionListener;)V
    //   4888: iload_3
    //   4889: iconst_3
    //   4890: if_icmpne +536 -> 5426
    //   4893: aload_0
    //   4894: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   4897: iconst_0
    //   4898: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   4901: dup
    //   4902: aload_1
    //   4903: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   4906: aastore
    //   4907: aload_0
    //   4908: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   4911: aload_0
    //   4912: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   4915: iconst_0
    //   4916: aaload
    //   4917: iconst_m1
    //   4918: bipush -2
    //   4920: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   4923: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4926: iload 4
    //   4928: ifeq +52 -> 4980
    //   4931: new 1197	android/view/View
    //   4934: dup
    //   4935: aload_1
    //   4936: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   4939: astore 9
    //   4941: aload_0
    //   4942: getfield 302	org/telegram/ui/PaymentFormActivity:dividers	Ljava/util/ArrayList;
    //   4945: aload 9
    //   4947: invokevirtual 1850	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   4950: pop
    //   4951: aload 9
    //   4953: ldc_w 1852
    //   4956: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   4959: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   4962: aload 8
    //   4964: aload 9
    //   4966: new 1655	android/widget/FrameLayout$LayoutParams
    //   4969: dup
    //   4970: iconst_m1
    //   4971: iconst_1
    //   4972: bipush 83
    //   4974: invokespecial 1855	android/widget/FrameLayout$LayoutParams:<init>	(III)V
    //   4977: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   4980: iload_3
    //   4981: iconst_4
    //   4982: if_icmpne +10 -> 4992
    //   4985: aload_0
    //   4986: getfield 1013	org/telegram/ui/PaymentFormActivity:need_card_country	Z
    //   4989: ifeq +27 -> 5016
    //   4992: iload_3
    //   4993: iconst_5
    //   4994: if_icmpne +10 -> 5004
    //   4997: aload_0
    //   4998: getfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   5001: ifeq +15 -> 5016
    //   5004: iload_3
    //   5005: iconst_2
    //   5006: if_icmpne +17 -> 5023
    //   5009: aload_0
    //   5010: getfield 549	org/telegram/ui/PaymentFormActivity:need_card_name	Z
    //   5013: ifne +10 -> 5023
    //   5016: aload 8
    //   5018: bipush 8
    //   5020: invokevirtual 1937	android/view/ViewGroup:setVisibility	(I)V
    //   5023: iload_3
    //   5024: iconst_1
    //   5025: iadd
    //   5026: istore_3
    //   5027: goto -629 -> 4398
    //   5030: astore 9
    //   5032: aload_0
    //   5033: iconst_0
    //   5034: putfield 1013	org/telegram/ui/PaymentFormActivity:need_card_country	Z
    //   5037: goto -691 -> 4346
    //   5040: astore 8
    //   5042: aload 8
    //   5044: invokestatic 1032	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   5047: goto -665 -> 4382
    //   5050: astore 9
    //   5052: aload_0
    //   5053: iconst_0
    //   5054: putfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   5057: goto -699 -> 4358
    //   5060: astore 9
    //   5062: aload_0
    //   5063: iconst_0
    //   5064: putfield 549	org/telegram/ui/PaymentFormActivity:need_card_name	Z
    //   5067: goto -697 -> 4370
    //   5070: astore 8
    //   5072: aload_0
    //   5073: ldc_w 411
    //   5076: putfield 1019	org/telegram/ui/PaymentFormActivity:stripeApiKey	Ljava/lang/String;
    //   5079: goto -697 -> 4382
    //   5082: iload_3
    //   5083: iconst_4
    //   5084: if_icmpne -610 -> 4474
    //   5087: aload_0
    //   5088: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   5091: iconst_1
    //   5092: new 298	org/telegram/ui/Cells/HeaderCell
    //   5095: dup
    //   5096: aload_1
    //   5097: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   5100: aastore
    //   5101: aload_0
    //   5102: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   5105: iconst_1
    //   5106: aaload
    //   5107: ldc_w 1710
    //   5110: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   5113: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   5116: aload_0
    //   5117: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   5120: iconst_1
    //   5121: aaload
    //   5122: ldc_w 2065
    //   5125: ldc_w 2066
    //   5128: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5131: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   5134: aload_0
    //   5135: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5138: aload_0
    //   5139: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   5142: iconst_1
    //   5143: aaload
    //   5144: iconst_m1
    //   5145: bipush -2
    //   5147: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   5150: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   5153: goto -679 -> 4474
    //   5156: iconst_0
    //   5157: istore 4
    //   5159: goto -660 -> 4499
    //   5162: iload_3
    //   5163: ifne +16 -> 5179
    //   5166: aload_0
    //   5167: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5170: iload_3
    //   5171: aaload
    //   5172: iconst_2
    //   5173: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   5176: goto -450 -> 4726
    //   5179: iload_3
    //   5180: iconst_4
    //   5181: if_icmpne +33 -> 5214
    //   5184: aload_0
    //   5185: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5188: iload_3
    //   5189: aaload
    //   5190: new 12	org/telegram/ui/PaymentFormActivity$11
    //   5193: dup
    //   5194: aload_0
    //   5195: invokespecial 2067	org/telegram/ui/PaymentFormActivity$11:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   5198: invokevirtual 1764	org/telegram/ui/Components/EditTextBoldCursor:setOnTouchListener	(Landroid/view/View$OnTouchListener;)V
    //   5201: aload_0
    //   5202: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5205: iload_3
    //   5206: aaload
    //   5207: iconst_0
    //   5208: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   5211: goto -485 -> 4726
    //   5214: iload_3
    //   5215: iconst_1
    //   5216: if_icmpne +18 -> 5234
    //   5219: aload_0
    //   5220: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5223: iload_3
    //   5224: aaload
    //   5225: sipush 16386
    //   5228: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   5231: goto -505 -> 4726
    //   5234: iload_3
    //   5235: iconst_2
    //   5236: if_icmpne +18 -> 5254
    //   5239: aload_0
    //   5240: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5243: iload_3
    //   5244: aaload
    //   5245: sipush 4097
    //   5248: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   5251: goto -525 -> 4726
    //   5254: aload_0
    //   5255: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5258: iload_3
    //   5259: aaload
    //   5260: sipush 16385
    //   5263: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   5266: goto -540 -> 4726
    //   5269: aload_0
    //   5270: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5273: iload_3
    //   5274: aaload
    //   5275: ldc_w 2069
    //   5278: ldc_w 2070
    //   5281: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5284: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5287: goto -511 -> 4776
    //   5290: aload_0
    //   5291: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5294: iload_3
    //   5295: aaload
    //   5296: ldc_w 2072
    //   5299: ldc_w 2073
    //   5302: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5305: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5308: goto -532 -> 4776
    //   5311: aload_0
    //   5312: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5315: iload_3
    //   5316: aaload
    //   5317: ldc_w 2075
    //   5320: ldc_w 2076
    //   5323: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5326: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5329: goto -553 -> 4776
    //   5332: aload_0
    //   5333: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5336: iload_3
    //   5337: aaload
    //   5338: ldc_w 2078
    //   5341: ldc_w 2079
    //   5344: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5347: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5350: goto -574 -> 4776
    //   5353: aload_0
    //   5354: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5357: iload_3
    //   5358: aaload
    //   5359: ldc_w 1889
    //   5362: ldc_w 1890
    //   5365: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5368: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5371: goto -595 -> 4776
    //   5374: aload_0
    //   5375: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5378: iload_3
    //   5379: aaload
    //   5380: ldc_w 1883
    //   5383: ldc_w 1884
    //   5386: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5389: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   5392: goto -616 -> 4776
    //   5395: iload_3
    //   5396: iconst_1
    //   5397: if_icmpne -600 -> 4797
    //   5400: aload_0
    //   5401: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5404: iload_3
    //   5405: aaload
    //   5406: new 18	org/telegram/ui/PaymentFormActivity$13
    //   5409: dup
    //   5410: aload_0
    //   5411: invokespecial 2080	org/telegram/ui/PaymentFormActivity$13:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   5414: invokevirtual 1810	org/telegram/ui/Components/EditTextBoldCursor:addTextChangedListener	(Landroid/text/TextWatcher;)V
    //   5417: goto -620 -> 4797
    //   5420: iconst_3
    //   5421: istore 5
    //   5423: goto -591 -> 4832
    //   5426: iload_3
    //   5427: iconst_5
    //   5428: if_icmpne +171 -> 5599
    //   5431: aload_0
    //   5432: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   5435: iconst_2
    //   5436: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   5439: dup
    //   5440: aload_1
    //   5441: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   5444: aastore
    //   5445: aload_0
    //   5446: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5449: aload_0
    //   5450: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   5453: iconst_2
    //   5454: aaload
    //   5455: iconst_m1
    //   5456: bipush -2
    //   5458: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   5461: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   5464: aload_0
    //   5465: new 1541	org/telegram/ui/Cells/TextCheckCell
    //   5468: dup
    //   5469: aload_1
    //   5470: invokespecial 1910	org/telegram/ui/Cells/TextCheckCell:<init>	(Landroid/content/Context;)V
    //   5473: putfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   5476: aload_0
    //   5477: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   5480: iconst_1
    //   5481: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   5484: invokevirtual 1915	org/telegram/ui/Cells/TextCheckCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   5487: aload_0
    //   5488: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   5491: ldc_w 2029
    //   5494: ldc_w 2030
    //   5497: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   5500: aload_0
    //   5501: getfield 541	org/telegram/ui/PaymentFormActivity:saveCardInfo	Z
    //   5504: iconst_0
    //   5505: invokevirtual 1922	org/telegram/ui/Cells/TextCheckCell:setTextAndCheck	(Ljava/lang/String;ZZ)V
    //   5508: aload_0
    //   5509: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5512: aload_0
    //   5513: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   5516: iconst_m1
    //   5517: bipush -2
    //   5519: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   5522: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   5525: aload_0
    //   5526: getfield 525	org/telegram/ui/PaymentFormActivity:checkCell1	Lorg/telegram/ui/Cells/TextCheckCell;
    //   5529: new 22	org/telegram/ui/PaymentFormActivity$15
    //   5532: dup
    //   5533: aload_0
    //   5534: invokespecial 2081	org/telegram/ui/PaymentFormActivity$15:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   5537: invokevirtual 1927	org/telegram/ui/Cells/TextCheckCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   5540: aload_0
    //   5541: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5544: iconst_0
    //   5545: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   5548: dup
    //   5549: aload_1
    //   5550: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   5553: aastore
    //   5554: aload_0
    //   5555: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5558: iconst_0
    //   5559: aaload
    //   5560: aload_1
    //   5561: ldc_w 1561
    //   5564: ldc_w 1550
    //   5567: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   5570: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   5573: aload_0
    //   5574: invokespecial 538	org/telegram/ui/PaymentFormActivity:updateSavePaymentField	()V
    //   5577: aload_0
    //   5578: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5581: aload_0
    //   5582: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5585: iconst_0
    //   5586: aaload
    //   5587: iconst_m1
    //   5588: bipush -2
    //   5590: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   5593: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   5596: goto -670 -> 4926
    //   5599: iload_3
    //   5600: ifne -674 -> 4926
    //   5603: aload_0
    //   5604: new 1251	android/widget/FrameLayout
    //   5607: dup
    //   5608: aload_1
    //   5609: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   5612: putfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   5615: aload_0
    //   5616: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   5619: sipush 4000
    //   5622: invokevirtual 1984	android/widget/FrameLayout:setId	(I)V
    //   5625: aload_0
    //   5626: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   5629: iconst_1
    //   5630: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   5633: invokevirtual 1985	android/widget/FrameLayout:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   5636: aload_0
    //   5637: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   5640: bipush 8
    //   5642: invokevirtual 1377	android/widget/FrameLayout:setVisibility	(I)V
    //   5645: aload 8
    //   5647: aload_0
    //   5648: getfield 1231	org/telegram/ui/PaymentFormActivity:androidPayContainer	Landroid/widget/FrameLayout;
    //   5651: bipush -2
    //   5653: ldc_w 1895
    //   5656: bipush 21
    //   5658: fconst_0
    //   5659: fconst_0
    //   5660: ldc_w 2082
    //   5663: fconst_0
    //   5664: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   5667: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   5670: goto -744 -> 4926
    //   5673: aload_0
    //   5674: getfield 1013	org/telegram/ui/PaymentFormActivity:need_card_country	Z
    //   5677: ifne +32 -> 5709
    //   5680: aload_0
    //   5681: getfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   5684: ifne +25 -> 5709
    //   5687: aload_0
    //   5688: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   5691: iconst_1
    //   5692: aaload
    //   5693: bipush 8
    //   5695: invokevirtual 1476	org/telegram/ui/Cells/HeaderCell:setVisibility	(I)V
    //   5698: aload_0
    //   5699: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   5702: iconst_0
    //   5703: aaload
    //   5704: bipush 8
    //   5706: invokevirtual 1939	org/telegram/ui/Cells/ShadowSectionCell:setVisibility	(I)V
    //   5709: aload_0
    //   5710: getfield 1015	org/telegram/ui/PaymentFormActivity:need_card_postcode	Z
    //   5713: ifeq +18 -> 5731
    //   5716: aload_0
    //   5717: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5720: iconst_5
    //   5721: aaload
    //   5722: ldc_w 1938
    //   5725: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   5728: goto -2133 -> 3595
    //   5731: aload_0
    //   5732: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   5735: iconst_3
    //   5736: aaload
    //   5737: ldc_w 1938
    //   5740: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   5743: goto -2148 -> 3595
    //   5746: aload_0
    //   5747: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   5750: iconst_1
    //   5751: if_icmpne +261 -> 6012
    //   5754: aload_0
    //   5755: getfield 704	org/telegram/ui/PaymentFormActivity:requestedInfo	Lorg/telegram/tgnet/TLRPC$TL_payments_validatedRequestedInfo;
    //   5758: getfield 2085	org/telegram/tgnet/TLRPC$TL_payments_validatedRequestedInfo:shipping_options	Ljava/util/ArrayList;
    //   5761: invokevirtual 821	java/util/ArrayList:size	()I
    //   5764: istore 4
    //   5766: aload_0
    //   5767: iload 4
    //   5769: anewarray 2087	org/telegram/ui/Cells/RadioCell
    //   5772: putfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5775: iconst_0
    //   5776: istore_3
    //   5777: iload_3
    //   5778: iload 4
    //   5780: if_icmpge +177 -> 5957
    //   5783: aload_0
    //   5784: getfield 704	org/telegram/ui/PaymentFormActivity:requestedInfo	Lorg/telegram/tgnet/TLRPC$TL_payments_validatedRequestedInfo;
    //   5787: getfield 2085	org/telegram/tgnet/TLRPC$TL_payments_validatedRequestedInfo:shipping_options	Ljava/util/ArrayList;
    //   5790: iload_3
    //   5791: invokevirtual 825	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   5794: checkcast 1068	org/telegram/tgnet/TLRPC$TL_shippingOption
    //   5797: astore 9
    //   5799: aload_0
    //   5800: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5803: iload_3
    //   5804: new 2087	org/telegram/ui/Cells/RadioCell
    //   5807: dup
    //   5808: aload_1
    //   5809: invokespecial 2088	org/telegram/ui/Cells/RadioCell:<init>	(Landroid/content/Context;)V
    //   5812: aastore
    //   5813: aload_0
    //   5814: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5817: iload_3
    //   5818: aaload
    //   5819: iload_3
    //   5820: invokestatic 362	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   5823: invokevirtual 2089	org/telegram/ui/Cells/RadioCell:setTag	(Ljava/lang/Object;)V
    //   5826: aload_0
    //   5827: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5830: iload_3
    //   5831: aaload
    //   5832: iconst_1
    //   5833: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   5836: invokevirtual 2090	org/telegram/ui/Cells/RadioCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   5839: aload_0
    //   5840: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5843: iload_3
    //   5844: aaload
    //   5845: astore 8
    //   5847: ldc_w 2092
    //   5850: iconst_2
    //   5851: anewarray 745	java/lang/Object
    //   5854: dup
    //   5855: iconst_0
    //   5856: aload_0
    //   5857: aload 9
    //   5859: getfield 1309	org/telegram/tgnet/TLRPC$TL_shippingOption:prices	Ljava/util/ArrayList;
    //   5862: invokespecial 2094	org/telegram/ui/PaymentFormActivity:getTotalPriceString	(Ljava/util/ArrayList;)Ljava/lang/String;
    //   5865: aastore
    //   5866: dup
    //   5867: iconst_1
    //   5868: aload 9
    //   5870: getfield 2095	org/telegram/tgnet/TLRPC$TL_shippingOption:title	Ljava/lang/String;
    //   5873: aastore
    //   5874: invokestatic 2099	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   5877: astore 9
    //   5879: iload_3
    //   5880: ifne +65 -> 5945
    //   5883: iconst_1
    //   5884: istore 6
    //   5886: iload_3
    //   5887: iload 4
    //   5889: iconst_1
    //   5890: isub
    //   5891: if_icmpeq +60 -> 5951
    //   5894: iconst_1
    //   5895: istore 7
    //   5897: aload 8
    //   5899: aload 9
    //   5901: iload 6
    //   5903: iload 7
    //   5905: invokevirtual 2101	org/telegram/ui/Cells/RadioCell:setText	(Ljava/lang/String;ZZ)V
    //   5908: aload_0
    //   5909: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5912: iload_3
    //   5913: aaload
    //   5914: new 24	org/telegram/ui/PaymentFormActivity$16
    //   5917: dup
    //   5918: aload_0
    //   5919: invokespecial 2102	org/telegram/ui/PaymentFormActivity$16:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   5922: invokevirtual 2103	org/telegram/ui/Cells/RadioCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   5925: aload_0
    //   5926: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5929: aload_0
    //   5930: getfield 683	org/telegram/ui/PaymentFormActivity:radioCells	[Lorg/telegram/ui/Cells/RadioCell;
    //   5933: iload_3
    //   5934: aaload
    //   5935: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   5938: iload_3
    //   5939: iconst_1
    //   5940: iadd
    //   5941: istore_3
    //   5942: goto -165 -> 5777
    //   5945: iconst_0
    //   5946: istore 6
    //   5948: goto -62 -> 5886
    //   5951: iconst_0
    //   5952: istore 7
    //   5954: goto -57 -> 5897
    //   5957: aload_0
    //   5958: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5961: iconst_0
    //   5962: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   5965: dup
    //   5966: aload_1
    //   5967: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   5970: aastore
    //   5971: aload_0
    //   5972: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5975: iconst_0
    //   5976: aaload
    //   5977: aload_1
    //   5978: ldc_w 1561
    //   5981: ldc_w 1550
    //   5984: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   5987: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   5990: aload_0
    //   5991: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   5994: aload_0
    //   5995: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   5998: iconst_0
    //   5999: aaload
    //   6000: iconst_m1
    //   6001: bipush -2
    //   6003: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6006: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6009: goto -2414 -> 3595
    //   6012: aload_0
    //   6013: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   6016: iconst_3
    //   6017: if_icmpne +885 -> 6902
    //   6020: aload_0
    //   6021: iconst_2
    //   6022: anewarray 708	org/telegram/ui/Components/EditTextBoldCursor
    //   6025: putfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6028: iconst_0
    //   6029: istore 5
    //   6031: iload 5
    //   6033: iconst_2
    //   6034: if_icmpge -2439 -> 3595
    //   6037: iload 5
    //   6039: ifne +69 -> 6108
    //   6042: aload_0
    //   6043: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   6046: iconst_0
    //   6047: new 298	org/telegram/ui/Cells/HeaderCell
    //   6050: dup
    //   6051: aload_1
    //   6052: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   6055: aastore
    //   6056: aload_0
    //   6057: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   6060: iconst_0
    //   6061: aaload
    //   6062: ldc_w 1710
    //   6065: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6068: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   6071: aload_0
    //   6072: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   6075: iconst_0
    //   6076: aaload
    //   6077: ldc_w 2041
    //   6080: ldc_w 2042
    //   6083: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   6086: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   6089: aload_0
    //   6090: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6093: aload_0
    //   6094: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   6097: iconst_0
    //   6098: aaload
    //   6099: iconst_m1
    //   6100: bipush -2
    //   6102: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6105: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6108: new 1251	android/widget/FrameLayout
    //   6111: dup
    //   6112: aload_1
    //   6113: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   6116: astore 8
    //   6118: aload_0
    //   6119: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6122: aload 8
    //   6124: iconst_m1
    //   6125: bipush 48
    //   6127: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6130: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6133: aload 8
    //   6135: ldc_w 1710
    //   6138: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6141: invokevirtual 1724	android/view/ViewGroup:setBackgroundColor	(I)V
    //   6144: iload 5
    //   6146: iconst_1
    //   6147: if_icmpeq +612 -> 6759
    //   6150: iconst_1
    //   6151: istore_3
    //   6152: iload_3
    //   6153: istore 4
    //   6155: iload_3
    //   6156: ifeq +26 -> 6182
    //   6159: iload 5
    //   6161: bipush 7
    //   6163: if_icmpne +601 -> 6764
    //   6166: aload_0
    //   6167: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   6170: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   6173: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   6176: ifne +588 -> 6764
    //   6179: iconst_0
    //   6180: istore 4
    //   6182: iload 4
    //   6184: ifeq +52 -> 6236
    //   6187: new 1197	android/view/View
    //   6190: dup
    //   6191: aload_1
    //   6192: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   6195: astore 9
    //   6197: aload_0
    //   6198: getfield 302	org/telegram/ui/PaymentFormActivity:dividers	Ljava/util/ArrayList;
    //   6201: aload 9
    //   6203: invokevirtual 1850	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   6206: pop
    //   6207: aload 9
    //   6209: ldc_w 1852
    //   6212: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6215: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   6218: aload 8
    //   6220: aload 9
    //   6222: new 1655	android/widget/FrameLayout$LayoutParams
    //   6225: dup
    //   6226: iconst_m1
    //   6227: iconst_1
    //   6228: bipush 83
    //   6230: invokespecial 1855	android/widget/FrameLayout$LayoutParams:<init>	(III)V
    //   6233: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6236: aload_0
    //   6237: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6240: iload 5
    //   6242: new 708	org/telegram/ui/Components/EditTextBoldCursor
    //   6245: dup
    //   6246: aload_1
    //   6247: invokespecial 1856	org/telegram/ui/Components/EditTextBoldCursor:<init>	(Landroid/content/Context;)V
    //   6250: aastore
    //   6251: aload_0
    //   6252: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6255: iload 5
    //   6257: aaload
    //   6258: iload 5
    //   6260: invokestatic 362	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   6263: invokevirtual 1731	org/telegram/ui/Components/EditTextBoldCursor:setTag	(Ljava/lang/Object;)V
    //   6266: aload_0
    //   6267: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6270: iload 5
    //   6272: aaload
    //   6273: iconst_1
    //   6274: ldc_w 1732
    //   6277: invokevirtual 1736	org/telegram/ui/Components/EditTextBoldCursor:setTextSize	(IF)V
    //   6280: aload_0
    //   6281: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6284: iload 5
    //   6286: aaload
    //   6287: ldc_w 1738
    //   6290: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6293: invokevirtual 1741	org/telegram/ui/Components/EditTextBoldCursor:setHintTextColor	(I)V
    //   6296: aload_0
    //   6297: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6300: iload 5
    //   6302: aaload
    //   6303: ldc_w 1743
    //   6306: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6309: invokevirtual 1746	org/telegram/ui/Components/EditTextBoldCursor:setTextColor	(I)V
    //   6312: aload_0
    //   6313: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6316: iload 5
    //   6318: aaload
    //   6319: aconst_null
    //   6320: invokevirtual 1747	org/telegram/ui/Components/EditTextBoldCursor:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   6323: aload_0
    //   6324: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6327: iload 5
    //   6329: aaload
    //   6330: ldc_w 1743
    //   6333: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6336: invokevirtual 1750	org/telegram/ui/Components/EditTextBoldCursor:setCursorColor	(I)V
    //   6339: aload_0
    //   6340: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6343: iload 5
    //   6345: aaload
    //   6346: ldc_w 1751
    //   6349: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6352: invokevirtual 1754	org/telegram/ui/Components/EditTextBoldCursor:setCursorSize	(I)V
    //   6355: aload_0
    //   6356: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6359: iload 5
    //   6361: aaload
    //   6362: ldc_w 1755
    //   6365: invokevirtual 1759	org/telegram/ui/Components/EditTextBoldCursor:setCursorWidth	(F)V
    //   6368: iload 5
    //   6370: ifne +442 -> 6812
    //   6373: aload_0
    //   6374: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6377: iload 5
    //   6379: aaload
    //   6380: new 26	org/telegram/ui/PaymentFormActivity$17
    //   6383: dup
    //   6384: aload_0
    //   6385: invokespecial 2106	org/telegram/ui/PaymentFormActivity$17:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   6388: invokevirtual 1764	org/telegram/ui/Components/EditTextBoldCursor:setOnTouchListener	(Landroid/view/View$OnTouchListener;)V
    //   6391: aload_0
    //   6392: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6395: iload 5
    //   6397: aaload
    //   6398: iconst_0
    //   6399: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   6402: aload_0
    //   6403: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6406: iload 5
    //   6408: aaload
    //   6409: ldc_w 1938
    //   6412: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   6415: iload 5
    //   6417: tableswitch	default:+23->6440, 0:+424->6841, 1:+447->6864
    //   6440: aload_0
    //   6441: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6444: iload 5
    //   6446: aaload
    //   6447: iconst_0
    //   6448: iconst_0
    //   6449: iconst_0
    //   6450: ldc_w 1783
    //   6453: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6456: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   6459: aload_0
    //   6460: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6463: iload 5
    //   6465: aaload
    //   6466: astore 9
    //   6468: getstatic 1894	org/telegram/messenger/LocaleController:isRTL	Z
    //   6471: ifeq +426 -> 6897
    //   6474: iconst_5
    //   6475: istore_3
    //   6476: aload 9
    //   6478: iload_3
    //   6479: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   6482: aload 8
    //   6484: aload_0
    //   6485: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6488: iload 5
    //   6490: aaload
    //   6491: iconst_m1
    //   6492: ldc_w 1895
    //   6495: bipush 51
    //   6497: ldc_w 1781
    //   6500: ldc_w 1782
    //   6503: ldc_w 1781
    //   6506: ldc_w 1783
    //   6509: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   6512: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6515: aload_0
    //   6516: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6519: iload 5
    //   6521: aaload
    //   6522: new 28	org/telegram/ui/PaymentFormActivity$18
    //   6525: dup
    //   6526: aload_0
    //   6527: invokespecial 2107	org/telegram/ui/PaymentFormActivity$18:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   6530: invokevirtual 1815	org/telegram/ui/Components/EditTextBoldCursor:setOnEditorActionListener	(Landroid/widget/TextView$OnEditorActionListener;)V
    //   6533: iload 5
    //   6535: iconst_1
    //   6536: if_icmpne +214 -> 6750
    //   6539: aload_0
    //   6540: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6543: iconst_0
    //   6544: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   6547: dup
    //   6548: aload_1
    //   6549: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   6552: aastore
    //   6553: aload_0
    //   6554: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6557: iconst_0
    //   6558: aaload
    //   6559: ldc_w 2109
    //   6562: ldc_w 2110
    //   6565: iconst_1
    //   6566: anewarray 745	java/lang/Object
    //   6569: dup
    //   6570: iconst_0
    //   6571: aload_0
    //   6572: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   6575: getfield 435	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_credentials	Lorg/telegram/tgnet/TLRPC$TL_paymentSavedCredentialsCard;
    //   6578: getfield 893	org/telegram/tgnet/TLRPC$TL_paymentSavedCredentialsCard:title	Ljava/lang/String;
    //   6581: aastore
    //   6582: invokestatic 1462	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   6585: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   6588: aload_0
    //   6589: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6592: iconst_0
    //   6593: aaload
    //   6594: aload_1
    //   6595: ldc_w 1548
    //   6598: ldc_w 1550
    //   6601: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   6604: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   6607: aload_0
    //   6608: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6611: aload_0
    //   6612: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6615: iconst_0
    //   6616: aaload
    //   6617: iconst_m1
    //   6618: bipush -2
    //   6620: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6623: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6626: aload_0
    //   6627: new 1474	org/telegram/ui/Cells/TextSettingsCell
    //   6630: dup
    //   6631: aload_1
    //   6632: invokespecial 2111	org/telegram/ui/Cells/TextSettingsCell:<init>	(Landroid/content/Context;)V
    //   6635: putfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   6638: aload_0
    //   6639: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   6642: iconst_1
    //   6643: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   6646: invokevirtual 2112	org/telegram/ui/Cells/TextSettingsCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   6649: aload_0
    //   6650: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   6653: ldc_w 2114
    //   6656: ldc_w 2115
    //   6659: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   6662: iconst_0
    //   6663: invokevirtual 2118	org/telegram/ui/Cells/TextSettingsCell:setText	(Ljava/lang/String;Z)V
    //   6666: aload_0
    //   6667: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6670: aload_0
    //   6671: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   6674: iconst_m1
    //   6675: bipush -2
    //   6677: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6680: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6683: aload_0
    //   6684: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   6687: new 30	org/telegram/ui/PaymentFormActivity$19
    //   6690: dup
    //   6691: aload_0
    //   6692: invokespecial 2119	org/telegram/ui/PaymentFormActivity$19:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   6695: invokevirtual 2120	org/telegram/ui/Cells/TextSettingsCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   6698: aload_0
    //   6699: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6702: iconst_1
    //   6703: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   6706: dup
    //   6707: aload_1
    //   6708: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   6711: aastore
    //   6712: aload_0
    //   6713: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6716: iconst_1
    //   6717: aaload
    //   6718: aload_1
    //   6719: ldc_w 1561
    //   6722: ldc_w 1550
    //   6725: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   6728: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   6731: aload_0
    //   6732: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6735: aload_0
    //   6736: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   6739: iconst_1
    //   6740: aaload
    //   6741: iconst_m1
    //   6742: bipush -2
    //   6744: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6747: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6750: iload 5
    //   6752: iconst_1
    //   6753: iadd
    //   6754: istore 5
    //   6756: goto -725 -> 6031
    //   6759: iconst_0
    //   6760: istore_3
    //   6761: goto -609 -> 6152
    //   6764: iload_3
    //   6765: istore 4
    //   6767: iload 5
    //   6769: bipush 6
    //   6771: if_icmpne -589 -> 6182
    //   6774: iload_3
    //   6775: istore 4
    //   6777: aload_0
    //   6778: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   6781: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   6784: getfield 427	org/telegram/tgnet/TLRPC$TL_invoice:phone_requested	Z
    //   6787: ifne -605 -> 6182
    //   6790: iload_3
    //   6791: istore 4
    //   6793: aload_0
    //   6794: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   6797: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   6800: getfield 421	org/telegram/tgnet/TLRPC$TL_invoice:email_requested	Z
    //   6803: ifne -621 -> 6182
    //   6806: iconst_0
    //   6807: istore 4
    //   6809: goto -627 -> 6182
    //   6812: aload_0
    //   6813: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6816: iload 5
    //   6818: aaload
    //   6819: sipush 129
    //   6822: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   6825: aload_0
    //   6826: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6829: iload 5
    //   6831: aaload
    //   6832: getstatic 2048	android/graphics/Typeface:DEFAULT	Landroid/graphics/Typeface;
    //   6835: invokevirtual 2052	org/telegram/ui/Components/EditTextBoldCursor:setTypeface	(Landroid/graphics/Typeface;)V
    //   6838: goto -436 -> 6402
    //   6841: aload_0
    //   6842: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6845: iload 5
    //   6847: aaload
    //   6848: aload_0
    //   6849: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   6852: getfield 435	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:saved_credentials	Lorg/telegram/tgnet/TLRPC$TL_paymentSavedCredentialsCard;
    //   6855: getfield 893	org/telegram/tgnet/TLRPC$TL_paymentSavedCredentialsCard:title	Ljava/lang/String;
    //   6858: invokevirtual 1866	org/telegram/ui/Components/EditTextBoldCursor:setText	(Ljava/lang/CharSequence;)V
    //   6861: goto -421 -> 6440
    //   6864: aload_0
    //   6865: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6868: iload 5
    //   6870: aaload
    //   6871: ldc_w 2122
    //   6874: ldc_w 2123
    //   6877: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   6880: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   6883: aload_0
    //   6884: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   6887: iload 5
    //   6889: aaload
    //   6890: invokevirtual 2126	org/telegram/ui/Components/EditTextBoldCursor:requestFocus	()Z
    //   6893: pop
    //   6894: goto -454 -> 6440
    //   6897: iconst_3
    //   6898: istore_3
    //   6899: goto -423 -> 6476
    //   6902: aload_0
    //   6903: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   6906: iconst_4
    //   6907: if_icmpeq +11 -> 6918
    //   6910: aload_0
    //   6911: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   6914: iconst_5
    //   6915: if_icmpne +1549 -> 8464
    //   6918: aload_0
    //   6919: new 2128	org/telegram/ui/Cells/PaymentInfoCell
    //   6922: dup
    //   6923: aload_1
    //   6924: invokespecial 2129	org/telegram/ui/Cells/PaymentInfoCell:<init>	(Landroid/content/Context;)V
    //   6927: putfield 2131	org/telegram/ui/PaymentFormActivity:paymentInfoCell	Lorg/telegram/ui/Cells/PaymentInfoCell;
    //   6930: aload_0
    //   6931: getfield 2131	org/telegram/ui/PaymentFormActivity:paymentInfoCell	Lorg/telegram/ui/Cells/PaymentInfoCell;
    //   6934: ldc_w 1710
    //   6937: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   6940: invokevirtual 2132	org/telegram/ui/Cells/PaymentInfoCell:setBackgroundColor	(I)V
    //   6943: aload_0
    //   6944: getfield 2131	org/telegram/ui/PaymentFormActivity:paymentInfoCell	Lorg/telegram/ui/Cells/PaymentInfoCell;
    //   6947: aload_0
    //   6948: getfield 347	org/telegram/ui/PaymentFormActivity:messageObject	Lorg/telegram/messenger/MessageObject;
    //   6951: getfield 381	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   6954: getfield 387	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   6957: checkcast 2134	org/telegram/tgnet/TLRPC$TL_messageMediaInvoice
    //   6960: aload_0
    //   6961: getfield 375	org/telegram/ui/PaymentFormActivity:currentBotName	Ljava/lang/String;
    //   6964: invokevirtual 2138	org/telegram/ui/Cells/PaymentInfoCell:setInvoice	(Lorg/telegram/tgnet/TLRPC$TL_messageMediaInvoice;Ljava/lang/String;)V
    //   6967: aload_0
    //   6968: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   6971: aload_0
    //   6972: getfield 2131	org/telegram/ui/PaymentFormActivity:paymentInfoCell	Lorg/telegram/ui/Cells/PaymentInfoCell;
    //   6975: iconst_m1
    //   6976: bipush -2
    //   6978: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   6981: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   6984: aload_0
    //   6985: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   6988: iconst_0
    //   6989: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   6992: dup
    //   6993: aload_1
    //   6994: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   6997: aastore
    //   6998: aload_0
    //   6999: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7002: aload_0
    //   7003: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   7006: iconst_0
    //   7007: aaload
    //   7008: iconst_m1
    //   7009: bipush -2
    //   7011: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   7014: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   7017: new 284	java/util/ArrayList
    //   7020: dup
    //   7021: invokespecial 285	java/util/ArrayList:<init>	()V
    //   7024: astore 8
    //   7026: aload 8
    //   7028: aload_0
    //   7029: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   7032: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   7035: getfield 1304	org/telegram/tgnet/TLRPC$TL_invoice:prices	Ljava/util/ArrayList;
    //   7038: invokevirtual 1308	java/util/ArrayList:addAll	(Ljava/util/Collection;)Z
    //   7041: pop
    //   7042: aload_0
    //   7043: getfield 345	org/telegram/ui/PaymentFormActivity:shippingOption	Lorg/telegram/tgnet/TLRPC$TL_shippingOption;
    //   7046: ifnull +16 -> 7062
    //   7049: aload 8
    //   7051: aload_0
    //   7052: getfield 345	org/telegram/ui/PaymentFormActivity:shippingOption	Lorg/telegram/tgnet/TLRPC$TL_shippingOption;
    //   7055: getfield 1309	org/telegram/tgnet/TLRPC$TL_shippingOption:prices	Ljava/util/ArrayList;
    //   7058: invokevirtual 1308	java/util/ArrayList:addAll	(Ljava/util/Collection;)Z
    //   7061: pop
    //   7062: aload_0
    //   7063: aload 8
    //   7065: invokespecial 2094	org/telegram/ui/PaymentFormActivity:getTotalPriceString	(Ljava/util/ArrayList;)Ljava/lang/String;
    //   7068: astore 11
    //   7070: iconst_0
    //   7071: istore_3
    //   7072: iload_3
    //   7073: aload 8
    //   7075: invokevirtual 821	java/util/ArrayList:size	()I
    //   7078: if_icmpge +83 -> 7161
    //   7081: aload 8
    //   7083: iload_3
    //   7084: invokevirtual 825	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7087: checkcast 827	org/telegram/tgnet/TLRPC$TL_labeledPrice
    //   7090: astore 9
    //   7092: new 2140	org/telegram/ui/Cells/TextPriceCell
    //   7095: dup
    //   7096: aload_1
    //   7097: invokespecial 2141	org/telegram/ui/Cells/TextPriceCell:<init>	(Landroid/content/Context;)V
    //   7100: astore 12
    //   7102: aload 12
    //   7104: ldc_w 1710
    //   7107: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7110: invokevirtual 2142	org/telegram/ui/Cells/TextPriceCell:setBackgroundColor	(I)V
    //   7113: aload 12
    //   7115: aload 9
    //   7117: getfield 2145	org/telegram/tgnet/TLRPC$TL_labeledPrice:label	Ljava/lang/String;
    //   7120: invokestatic 836	org/telegram/messenger/LocaleController:getInstance	()Lorg/telegram/messenger/LocaleController;
    //   7123: aload 9
    //   7125: getfield 831	org/telegram/tgnet/TLRPC$TL_labeledPrice:amount	J
    //   7128: aload_0
    //   7129: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   7132: getfield 332	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:invoice	Lorg/telegram/tgnet/TLRPC$TL_invoice;
    //   7135: getfield 839	org/telegram/tgnet/TLRPC$TL_invoice:currency	Ljava/lang/String;
    //   7138: invokevirtual 850	org/telegram/messenger/LocaleController:formatCurrencyString	(JLjava/lang/String;)Ljava/lang/String;
    //   7141: iconst_0
    //   7142: invokevirtual 2149	org/telegram/ui/Cells/TextPriceCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/String;Z)V
    //   7145: aload_0
    //   7146: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7149: aload 12
    //   7151: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7154: iload_3
    //   7155: iconst_1
    //   7156: iadd
    //   7157: istore_3
    //   7158: goto -86 -> 7072
    //   7161: new 2140	org/telegram/ui/Cells/TextPriceCell
    //   7164: dup
    //   7165: aload_1
    //   7166: invokespecial 2141	org/telegram/ui/Cells/TextPriceCell:<init>	(Landroid/content/Context;)V
    //   7169: astore 8
    //   7171: aload 8
    //   7173: ldc_w 1710
    //   7176: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7179: invokevirtual 2142	org/telegram/ui/Cells/TextPriceCell:setBackgroundColor	(I)V
    //   7182: aload 8
    //   7184: ldc_w 2151
    //   7187: ldc_w 2152
    //   7190: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7193: aload 11
    //   7195: iconst_1
    //   7196: invokevirtual 2149	org/telegram/ui/Cells/TextPriceCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/String;Z)V
    //   7199: aload_0
    //   7200: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7203: aload 8
    //   7205: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7208: new 1197	android/view/View
    //   7211: dup
    //   7212: aload_1
    //   7213: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   7216: astore 8
    //   7218: aload_0
    //   7219: getfield 302	org/telegram/ui/PaymentFormActivity:dividers	Ljava/util/ArrayList;
    //   7222: aload 8
    //   7224: invokevirtual 1850	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   7227: pop
    //   7228: aload 8
    //   7230: ldc_w 1852
    //   7233: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7236: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   7239: aload_0
    //   7240: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7243: aload 8
    //   7245: new 1655	android/widget/FrameLayout$LayoutParams
    //   7248: dup
    //   7249: iconst_m1
    //   7250: iconst_1
    //   7251: bipush 83
    //   7253: invokespecial 1855	android/widget/FrameLayout$LayoutParams:<init>	(III)V
    //   7256: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   7259: aload_0
    //   7260: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7263: iconst_0
    //   7264: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7267: dup
    //   7268: aload_1
    //   7269: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7272: aastore
    //   7273: aload_0
    //   7274: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7277: iconst_0
    //   7278: aaload
    //   7279: iconst_1
    //   7280: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   7283: invokevirtual 2154	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   7286: aload_0
    //   7287: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7290: iconst_0
    //   7291: aaload
    //   7292: aload_0
    //   7293: getfield 409	org/telegram/ui/PaymentFormActivity:cardName	Ljava/lang/String;
    //   7296: ldc_w 2156
    //   7299: ldc_w 2157
    //   7302: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7305: iconst_1
    //   7306: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7309: aload_0
    //   7310: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7313: aload_0
    //   7314: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7317: iconst_0
    //   7318: aaload
    //   7319: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7322: aload_0
    //   7323: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   7326: iconst_4
    //   7327: if_icmpne +20 -> 7347
    //   7330: aload_0
    //   7331: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7334: iconst_0
    //   7335: aaload
    //   7336: new 34	org/telegram/ui/PaymentFormActivity$20
    //   7339: dup
    //   7340: aload_0
    //   7341: invokespecial 2161	org/telegram/ui/PaymentFormActivity$20:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   7344: invokevirtual 2162	org/telegram/ui/Cells/TextDetailSettingsCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   7347: aconst_null
    //   7348: astore 8
    //   7350: iconst_0
    //   7351: istore_3
    //   7352: iload_3
    //   7353: aload_0
    //   7354: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   7357: getfield 340	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:users	Ljava/util/ArrayList;
    //   7360: invokevirtual 821	java/util/ArrayList:size	()I
    //   7363: if_icmpge +45 -> 7408
    //   7366: aload_0
    //   7367: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   7370: getfield 340	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:users	Ljava/util/ArrayList;
    //   7373: iload_3
    //   7374: invokevirtual 825	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   7377: checkcast 370	org/telegram/tgnet/TLRPC$User
    //   7380: astore 9
    //   7382: aload 9
    //   7384: getfield 1823	org/telegram/tgnet/TLRPC$User:id	I
    //   7387: aload_0
    //   7388: getfield 321	org/telegram/ui/PaymentFormActivity:paymentForm	Lorg/telegram/tgnet/TLRPC$TL_payments_paymentForm;
    //   7391: getfield 336	org/telegram/tgnet/TLRPC$TL_payments_paymentForm:provider_id	I
    //   7394: if_icmpne +7 -> 7401
    //   7397: aload 9
    //   7399: astore 8
    //   7401: iload_3
    //   7402: iconst_1
    //   7403: iadd
    //   7404: istore_3
    //   7405: goto -53 -> 7352
    //   7408: aload 8
    //   7410: ifnull +1046 -> 8456
    //   7413: aload_0
    //   7414: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7417: iconst_1
    //   7418: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7421: dup
    //   7422: aload_1
    //   7423: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7426: aastore
    //   7427: aload_0
    //   7428: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7431: iconst_1
    //   7432: aaload
    //   7433: iconst_1
    //   7434: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   7437: invokevirtual 2154	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   7440: aload_0
    //   7441: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7444: iconst_1
    //   7445: aaload
    //   7446: astore 9
    //   7448: aload 8
    //   7450: getfield 373	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   7453: aload 8
    //   7455: getfield 1898	org/telegram/tgnet/TLRPC$User:last_name	Ljava/lang/String;
    //   7458: invokestatic 1904	org/telegram/messenger/ContactsController:formatName	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    //   7461: astore 8
    //   7463: aload 9
    //   7465: aload 8
    //   7467: ldc_w 2164
    //   7470: ldc_w 2165
    //   7473: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7476: iconst_1
    //   7477: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7480: aload_0
    //   7481: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7484: aload_0
    //   7485: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7488: iconst_1
    //   7489: aaload
    //   7490: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7493: aload_0
    //   7494: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7497: ifnull +525 -> 8022
    //   7500: aload_0
    //   7501: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7504: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7507: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7510: ifnull +175 -> 7685
    //   7513: ldc_w 2167
    //   7516: bipush 6
    //   7518: anewarray 745	java/lang/Object
    //   7521: dup
    //   7522: iconst_0
    //   7523: aload_0
    //   7524: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7527: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7530: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7533: getfield 801	org/telegram/tgnet/TLRPC$TL_postAddress:street_line1	Ljava/lang/String;
    //   7536: aastore
    //   7537: dup
    //   7538: iconst_1
    //   7539: aload_0
    //   7540: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7543: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7546: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7549: getfield 804	org/telegram/tgnet/TLRPC$TL_postAddress:street_line2	Ljava/lang/String;
    //   7552: aastore
    //   7553: dup
    //   7554: iconst_2
    //   7555: aload_0
    //   7556: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7559: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7562: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7565: getfield 807	org/telegram/tgnet/TLRPC$TL_postAddress:city	Ljava/lang/String;
    //   7568: aastore
    //   7569: dup
    //   7570: iconst_3
    //   7571: aload_0
    //   7572: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7575: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7578: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7581: getfield 810	org/telegram/tgnet/TLRPC$TL_postAddress:state	Ljava/lang/String;
    //   7584: aastore
    //   7585: dup
    //   7586: iconst_4
    //   7587: aload_0
    //   7588: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7591: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7594: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7597: getfield 813	org/telegram/tgnet/TLRPC$TL_postAddress:country_iso2	Ljava/lang/String;
    //   7600: aastore
    //   7601: dup
    //   7602: iconst_5
    //   7603: aload_0
    //   7604: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7607: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7610: getfield 798	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:shipping_address	Lorg/telegram/tgnet/TLRPC$TL_postAddress;
    //   7613: getfield 816	org/telegram/tgnet/TLRPC$TL_postAddress:post_code	Ljava/lang/String;
    //   7616: aastore
    //   7617: invokestatic 2099	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   7620: astore 9
    //   7622: aload_0
    //   7623: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7626: iconst_2
    //   7627: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7630: dup
    //   7631: aload_1
    //   7632: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7635: aastore
    //   7636: aload_0
    //   7637: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7640: iconst_2
    //   7641: aaload
    //   7642: ldc_w 1710
    //   7645: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7648: invokevirtual 2168	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundColor	(I)V
    //   7651: aload_0
    //   7652: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7655: iconst_2
    //   7656: aaload
    //   7657: aload 9
    //   7659: ldc_w 1713
    //   7662: ldc_w 1714
    //   7665: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7668: iconst_1
    //   7669: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7672: aload_0
    //   7673: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7676: aload_0
    //   7677: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7680: iconst_2
    //   7681: aaload
    //   7682: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7685: aload_0
    //   7686: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7689: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7692: getfield 767	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:name	Ljava/lang/String;
    //   7695: ifnull +74 -> 7769
    //   7698: aload_0
    //   7699: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7702: iconst_3
    //   7703: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7706: dup
    //   7707: aload_1
    //   7708: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7711: aastore
    //   7712: aload_0
    //   7713: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7716: iconst_3
    //   7717: aaload
    //   7718: ldc_w 1710
    //   7721: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7724: invokevirtual 2168	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundColor	(I)V
    //   7727: aload_0
    //   7728: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7731: iconst_3
    //   7732: aaload
    //   7733: aload_0
    //   7734: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7737: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7740: getfield 767	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:name	Ljava/lang/String;
    //   7743: ldc_w 2170
    //   7746: ldc_w 2171
    //   7749: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7752: iconst_1
    //   7753: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7756: aload_0
    //   7757: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7760: aload_0
    //   7761: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7764: iconst_3
    //   7765: aaload
    //   7766: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7769: aload_0
    //   7770: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7773: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7776: getfield 783	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:phone	Ljava/lang/String;
    //   7779: ifnull +80 -> 7859
    //   7782: aload_0
    //   7783: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7786: iconst_4
    //   7787: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7790: dup
    //   7791: aload_1
    //   7792: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7795: aastore
    //   7796: aload_0
    //   7797: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7800: iconst_4
    //   7801: aaload
    //   7802: ldc_w 1710
    //   7805: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7808: invokevirtual 2168	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundColor	(I)V
    //   7811: aload_0
    //   7812: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7815: iconst_4
    //   7816: aaload
    //   7817: invokestatic 2176	org/telegram/PhoneFormat/PhoneFormat:getInstance	()Lorg/telegram/PhoneFormat/PhoneFormat;
    //   7820: aload_0
    //   7821: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7824: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7827: getfield 783	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:phone	Ljava/lang/String;
    //   7830: invokevirtual 2178	org/telegram/PhoneFormat/PhoneFormat:format	(Ljava/lang/String;)Ljava/lang/String;
    //   7833: ldc_w 2180
    //   7836: ldc_w 2181
    //   7839: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7842: iconst_1
    //   7843: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7846: aload_0
    //   7847: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7850: aload_0
    //   7851: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7854: iconst_4
    //   7855: aaload
    //   7856: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7859: aload_0
    //   7860: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7863: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7866: getfield 791	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:email	Ljava/lang/String;
    //   7869: ifnull +74 -> 7943
    //   7872: aload_0
    //   7873: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7876: iconst_5
    //   7877: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7880: dup
    //   7881: aload_1
    //   7882: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7885: aastore
    //   7886: aload_0
    //   7887: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7890: iconst_5
    //   7891: aaload
    //   7892: ldc_w 1710
    //   7895: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7898: invokevirtual 2168	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundColor	(I)V
    //   7901: aload_0
    //   7902: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7905: iconst_5
    //   7906: aaload
    //   7907: aload_0
    //   7908: getfield 403	org/telegram/ui/PaymentFormActivity:validateRequest	Lorg/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo;
    //   7911: getfield 404	org/telegram/tgnet/TLRPC$TL_payments_validateRequestedInfo:info	Lorg/telegram/tgnet/TLRPC$TL_paymentRequestedInfo;
    //   7914: getfield 791	org/telegram/tgnet/TLRPC$TL_paymentRequestedInfo:email	Ljava/lang/String;
    //   7917: ldc_w 2183
    //   7920: ldc_w 2184
    //   7923: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   7926: iconst_1
    //   7927: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   7930: aload_0
    //   7931: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   7934: aload_0
    //   7935: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7938: iconst_5
    //   7939: aaload
    //   7940: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   7943: aload_0
    //   7944: getfield 345	org/telegram/ui/PaymentFormActivity:shippingOption	Lorg/telegram/tgnet/TLRPC$TL_shippingOption;
    //   7947: ifnull +75 -> 8022
    //   7950: aload_0
    //   7951: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7954: bipush 6
    //   7956: new 312	org/telegram/ui/Cells/TextDetailSettingsCell
    //   7959: dup
    //   7960: aload_1
    //   7961: invokespecial 2153	org/telegram/ui/Cells/TextDetailSettingsCell:<init>	(Landroid/content/Context;)V
    //   7964: aastore
    //   7965: aload_0
    //   7966: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7969: bipush 6
    //   7971: aaload
    //   7972: ldc_w 1710
    //   7975: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   7978: invokevirtual 2168	org/telegram/ui/Cells/TextDetailSettingsCell:setBackgroundColor	(I)V
    //   7981: aload_0
    //   7982: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   7985: bipush 6
    //   7987: aaload
    //   7988: aload_0
    //   7989: getfield 345	org/telegram/ui/PaymentFormActivity:shippingOption	Lorg/telegram/tgnet/TLRPC$TL_shippingOption;
    //   7992: getfield 2095	org/telegram/tgnet/TLRPC$TL_shippingOption:title	Ljava/lang/String;
    //   7995: ldc_w 2186
    //   7998: ldc_w 2187
    //   8001: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   8004: iconst_0
    //   8005: invokevirtual 2160	org/telegram/ui/Cells/TextDetailSettingsCell:setTextAndValue	(Ljava/lang/String;Ljava/lang/CharSequence;Z)V
    //   8008: aload_0
    //   8009: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8012: aload_0
    //   8013: getfield 314	org/telegram/ui/PaymentFormActivity:detailSettingsCell	[Lorg/telegram/ui/Cells/TextDetailSettingsCell;
    //   8016: bipush 6
    //   8018: aaload
    //   8019: invokevirtual 2105	android/widget/LinearLayout:addView	(Landroid/view/View;)V
    //   8022: aload_0
    //   8023: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   8026: iconst_4
    //   8027: if_icmpne +374 -> 8401
    //   8030: aload_0
    //   8031: new 1251	android/widget/FrameLayout
    //   8034: dup
    //   8035: aload_1
    //   8036: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   8039: putfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8042: aload_0
    //   8043: getfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8046: iconst_1
    //   8047: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   8050: invokevirtual 1985	android/widget/FrameLayout:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   8053: aload 10
    //   8055: aload_0
    //   8056: getfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8059: iconst_m1
    //   8060: bipush 48
    //   8062: bipush 80
    //   8064: invokestatic 2190	org/telegram/ui/Components/LayoutHelper:createFrame	(III)Landroid/widget/FrameLayout$LayoutParams;
    //   8067: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8070: aload_0
    //   8071: getfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8074: new 38	org/telegram/ui/PaymentFormActivity$21
    //   8077: dup
    //   8078: aload_0
    //   8079: aload 8
    //   8081: aload 11
    //   8083: invokespecial 2192	org/telegram/ui/PaymentFormActivity$21:<init>	(Lorg/telegram/ui/PaymentFormActivity;Ljava/lang/String;Ljava/lang/String;)V
    //   8086: invokevirtual 2193	android/widget/FrameLayout:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   8089: aload_0
    //   8090: new 1451	android/widget/TextView
    //   8093: dup
    //   8094: aload_1
    //   8095: invokespecial 1775	android/widget/TextView:<init>	(Landroid/content/Context;)V
    //   8098: putfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8101: aload_0
    //   8102: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8105: ldc_w 2195
    //   8108: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8111: invokevirtual 1779	android/widget/TextView:setTextColor	(I)V
    //   8114: aload_0
    //   8115: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8118: ldc_w 2197
    //   8121: ldc_w 2198
    //   8124: iconst_1
    //   8125: anewarray 745	java/lang/Object
    //   8128: dup
    //   8129: iconst_0
    //   8130: aload 11
    //   8132: aastore
    //   8133: invokestatic 1462	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   8136: invokevirtual 1778	android/widget/TextView:setText	(Ljava/lang/CharSequence;)V
    //   8139: aload_0
    //   8140: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8143: iconst_1
    //   8144: ldc_w 2199
    //   8147: invokevirtual 1780	android/widget/TextView:setTextSize	(IF)V
    //   8150: aload_0
    //   8151: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8154: bipush 17
    //   8156: invokevirtual 2200	android/widget/TextView:setGravity	(I)V
    //   8159: aload_0
    //   8160: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8163: ldc_w 2202
    //   8166: invokestatic 2206	org/telegram/messenger/AndroidUtilities:getTypeface	(Ljava/lang/String;)Landroid/graphics/Typeface;
    //   8169: invokevirtual 2207	android/widget/TextView:setTypeface	(Landroid/graphics/Typeface;)V
    //   8172: aload_0
    //   8173: getfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8176: aload_0
    //   8177: getfield 696	org/telegram/ui/PaymentFormActivity:payTextView	Landroid/widget/TextView;
    //   8180: iconst_m1
    //   8181: ldc_w 1604
    //   8184: invokestatic 1610	org/telegram/ui/Components/LayoutHelper:createFrame	(IF)Landroid/widget/FrameLayout$LayoutParams;
    //   8187: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8190: aload_0
    //   8191: new 1423	org/telegram/ui/Components/ContextProgressView
    //   8194: dup
    //   8195: aload_1
    //   8196: iconst_0
    //   8197: invokespecial 1603	org/telegram/ui/Components/ContextProgressView:<init>	(Landroid/content/Context;I)V
    //   8200: putfield 692	org/telegram/ui/PaymentFormActivity:progressViewButton	Lorg/telegram/ui/Components/ContextProgressView;
    //   8203: aload_0
    //   8204: getfield 692	org/telegram/ui/PaymentFormActivity:progressViewButton	Lorg/telegram/ui/Components/ContextProgressView;
    //   8207: iconst_4
    //   8208: invokevirtual 1424	org/telegram/ui/Components/ContextProgressView:setVisibility	(I)V
    //   8211: aload_0
    //   8212: getfield 1447	org/telegram/ui/PaymentFormActivity:bottomLayout	Landroid/widget/FrameLayout;
    //   8215: aload_0
    //   8216: getfield 692	org/telegram/ui/PaymentFormActivity:progressViewButton	Lorg/telegram/ui/Components/ContextProgressView;
    //   8219: iconst_m1
    //   8220: ldc_w 1604
    //   8223: invokestatic 1610	org/telegram/ui/Components/LayoutHelper:createFrame	(IF)Landroid/widget/FrameLayout$LayoutParams;
    //   8226: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8229: new 1197	android/view/View
    //   8232: dup
    //   8233: aload_1
    //   8234: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   8237: astore 8
    //   8239: aload 8
    //   8241: ldc_w 2208
    //   8244: invokevirtual 2211	android/view/View:setBackgroundResource	(I)V
    //   8247: aload 10
    //   8249: aload 8
    //   8251: iconst_m1
    //   8252: ldc_w 2212
    //   8255: bipush 83
    //   8257: fconst_0
    //   8258: fconst_0
    //   8259: fconst_0
    //   8260: ldc_w 1641
    //   8263: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   8266: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8269: aload_0
    //   8270: getfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   8273: iconst_0
    //   8274: invokevirtual 1425	org/telegram/ui/ActionBar/ActionBarMenuItem:setEnabled	(Z)V
    //   8277: aload_0
    //   8278: getfield 517	org/telegram/ui/PaymentFormActivity:doneItem	Lorg/telegram/ui/ActionBar/ActionBarMenuItem;
    //   8281: invokevirtual 1429	org/telegram/ui/ActionBar/ActionBarMenuItem:getImageView	()Landroid/widget/ImageView;
    //   8284: iconst_4
    //   8285: invokevirtual 1445	android/widget/ImageView:setVisibility	(I)V
    //   8288: aload_0
    //   8289: new 42	org/telegram/ui/PaymentFormActivity$22
    //   8292: dup
    //   8293: aload_0
    //   8294: aload_1
    //   8295: invokespecial 2213	org/telegram/ui/PaymentFormActivity$22:<init>	(Lorg/telegram/ui/PaymentFormActivity;Landroid/content/Context;)V
    //   8298: putfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8301: aload_0
    //   8302: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8305: iconst_m1
    //   8306: invokevirtual 2214	android/webkit/WebView:setBackgroundColor	(I)V
    //   8309: aload_0
    //   8310: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8313: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   8316: iconst_1
    //   8317: invokevirtual 1999	android/webkit/WebSettings:setJavaScriptEnabled	(Z)V
    //   8320: aload_0
    //   8321: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8324: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   8327: iconst_1
    //   8328: invokevirtual 2002	android/webkit/WebSettings:setDomStorageEnabled	(Z)V
    //   8331: getstatic 900	android/os/Build$VERSION:SDK_INT	I
    //   8334: bipush 21
    //   8336: if_icmplt +25 -> 8361
    //   8339: aload_0
    //   8340: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8343: invokevirtual 1994	android/webkit/WebView:getSettings	()Landroid/webkit/WebSettings;
    //   8346: iconst_0
    //   8347: invokevirtual 2005	android/webkit/WebSettings:setMixedContentMode	(I)V
    //   8350: invokestatic 2010	android/webkit/CookieManager:getInstance	()Landroid/webkit/CookieManager;
    //   8353: aload_0
    //   8354: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8357: iconst_1
    //   8358: invokevirtual 2014	android/webkit/CookieManager:setAcceptThirdPartyCookies	(Landroid/webkit/WebView;Z)V
    //   8361: aload_0
    //   8362: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8365: new 44	org/telegram/ui/PaymentFormActivity$23
    //   8368: dup
    //   8369: aload_0
    //   8370: invokespecial 2215	org/telegram/ui/PaymentFormActivity$23:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   8373: invokevirtual 2027	android/webkit/WebView:setWebViewClient	(Landroid/webkit/WebViewClient;)V
    //   8376: aload 10
    //   8378: aload_0
    //   8379: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8382: iconst_m1
    //   8383: ldc_w 1604
    //   8386: invokestatic 1610	org/telegram/ui/Components/LayoutHelper:createFrame	(IF)Landroid/widget/FrameLayout$LayoutParams;
    //   8389: invokevirtual 1645	android/widget/FrameLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8392: aload_0
    //   8393: getfield 669	org/telegram/ui/PaymentFormActivity:webView	Landroid/webkit/WebView;
    //   8396: bipush 8
    //   8398: invokevirtual 2216	android/webkit/WebView:setVisibility	(I)V
    //   8401: aload_0
    //   8402: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   8405: iconst_1
    //   8406: new 304	org/telegram/ui/Cells/ShadowSectionCell
    //   8409: dup
    //   8410: aload_1
    //   8411: invokespecial 1844	org/telegram/ui/Cells/ShadowSectionCell:<init>	(Landroid/content/Context;)V
    //   8414: aastore
    //   8415: aload_0
    //   8416: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   8419: iconst_1
    //   8420: aaload
    //   8421: aload_1
    //   8422: ldc_w 1561
    //   8425: ldc_w 1550
    //   8428: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   8431: invokevirtual 1560	org/telegram/ui/Cells/ShadowSectionCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   8434: aload_0
    //   8435: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8438: aload_0
    //   8439: getfield 306	org/telegram/ui/PaymentFormActivity:sectionCell	[Lorg/telegram/ui/Cells/ShadowSectionCell;
    //   8442: iconst_1
    //   8443: aaload
    //   8444: iconst_m1
    //   8445: bipush -2
    //   8447: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   8450: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8453: goto -4858 -> 3595
    //   8456: ldc_w 411
    //   8459: astore 8
    //   8461: goto -968 -> 7493
    //   8464: aload_0
    //   8465: getfield 316	org/telegram/ui/PaymentFormActivity:currentStep	I
    //   8468: bipush 6
    //   8470: if_icmpne -4875 -> 3595
    //   8473: aload_0
    //   8474: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   8477: iconst_2
    //   8478: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   8481: dup
    //   8482: aload_1
    //   8483: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   8486: aastore
    //   8487: aload_0
    //   8488: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   8491: iconst_2
    //   8492: aaload
    //   8493: aload_1
    //   8494: ldc_w 1548
    //   8497: ldc_w 1550
    //   8500: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   8503: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   8506: aload_0
    //   8507: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8510: aload_0
    //   8511: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   8514: iconst_2
    //   8515: aaload
    //   8516: iconst_m1
    //   8517: bipush -2
    //   8519: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   8522: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8525: aload_0
    //   8526: new 1474	org/telegram/ui/Cells/TextSettingsCell
    //   8529: dup
    //   8530: aload_1
    //   8531: invokespecial 2111	org/telegram/ui/Cells/TextSettingsCell:<init>	(Landroid/content/Context;)V
    //   8534: putfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8537: aload_0
    //   8538: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8541: iconst_1
    //   8542: invokestatic 1914	org/telegram/ui/ActionBar/Theme:getSelectorDrawable	(Z)Landroid/graphics/drawable/Drawable;
    //   8545: invokevirtual 2112	org/telegram/ui/Cells/TextSettingsCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   8548: aload_0
    //   8549: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8552: ldc_w 2218
    //   8555: invokevirtual 2219	org/telegram/ui/Cells/TextSettingsCell:setTag	(Ljava/lang/Object;)V
    //   8558: aload_0
    //   8559: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8562: ldc_w 2218
    //   8565: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8568: invokevirtual 2220	org/telegram/ui/Cells/TextSettingsCell:setTextColor	(I)V
    //   8571: aload_0
    //   8572: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8575: ldc_w 2222
    //   8578: ldc_w 2223
    //   8581: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   8584: iconst_0
    //   8585: invokevirtual 2118	org/telegram/ui/Cells/TextSettingsCell:setText	(Ljava/lang/String;Z)V
    //   8588: aload_0
    //   8589: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8592: aload_0
    //   8593: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8596: iconst_m1
    //   8597: bipush -2
    //   8599: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   8602: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8605: aload_0
    //   8606: getfield 1472	org/telegram/ui/PaymentFormActivity:settingsCell1	Lorg/telegram/ui/Cells/TextSettingsCell;
    //   8609: new 46	org/telegram/ui/PaymentFormActivity$24
    //   8612: dup
    //   8613: aload_0
    //   8614: invokespecial 2224	org/telegram/ui/PaymentFormActivity$24:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   8617: invokevirtual 2120	org/telegram/ui/Cells/TextSettingsCell:setOnClickListener	(Landroid/view/View$OnClickListener;)V
    //   8620: aload_0
    //   8621: iconst_3
    //   8622: anewarray 708	org/telegram/ui/Components/EditTextBoldCursor
    //   8625: putfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8628: iconst_0
    //   8629: istore_3
    //   8630: iload_3
    //   8631: iconst_3
    //   8632: if_icmpge +775 -> 9407
    //   8635: iload_3
    //   8636: ifne +525 -> 9161
    //   8639: aload_0
    //   8640: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   8643: iconst_0
    //   8644: new 298	org/telegram/ui/Cells/HeaderCell
    //   8647: dup
    //   8648: aload_1
    //   8649: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   8652: aastore
    //   8653: aload_0
    //   8654: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   8657: iconst_0
    //   8658: aaload
    //   8659: ldc_w 1710
    //   8662: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8665: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   8668: aload_0
    //   8669: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   8672: iconst_0
    //   8673: aaload
    //   8674: ldc_w 2226
    //   8677: ldc_w 2227
    //   8680: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   8683: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   8686: aload_0
    //   8687: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8690: aload_0
    //   8691: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   8694: iconst_0
    //   8695: aaload
    //   8696: iconst_m1
    //   8697: bipush -2
    //   8699: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   8702: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8705: new 1251	android/widget/FrameLayout
    //   8708: dup
    //   8709: aload_1
    //   8710: invokespecial 1615	android/widget/FrameLayout:<init>	(Landroid/content/Context;)V
    //   8713: astore 8
    //   8715: aload_0
    //   8716: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   8719: aload 8
    //   8721: iconst_m1
    //   8722: bipush 48
    //   8724: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   8727: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8730: aload 8
    //   8732: ldc_w 1710
    //   8735: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8738: invokevirtual 1724	android/view/ViewGroup:setBackgroundColor	(I)V
    //   8741: iload_3
    //   8742: ifne +52 -> 8794
    //   8745: new 1197	android/view/View
    //   8748: dup
    //   8749: aload_1
    //   8750: invokespecial 1848	android/view/View:<init>	(Landroid/content/Context;)V
    //   8753: astore 9
    //   8755: aload_0
    //   8756: getfield 302	org/telegram/ui/PaymentFormActivity:dividers	Ljava/util/ArrayList;
    //   8759: aload 9
    //   8761: invokevirtual 1850	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   8764: pop
    //   8765: aload 9
    //   8767: ldc_w 1852
    //   8770: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8773: invokevirtual 1626	android/view/View:setBackgroundColor	(I)V
    //   8776: aload 8
    //   8778: aload 9
    //   8780: new 1655	android/widget/FrameLayout$LayoutParams
    //   8783: dup
    //   8784: iconst_m1
    //   8785: iconst_1
    //   8786: bipush 83
    //   8788: invokespecial 1855	android/widget/FrameLayout$LayoutParams:<init>	(III)V
    //   8791: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   8794: aload_0
    //   8795: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8798: iload_3
    //   8799: new 708	org/telegram/ui/Components/EditTextBoldCursor
    //   8802: dup
    //   8803: aload_1
    //   8804: invokespecial 1856	org/telegram/ui/Components/EditTextBoldCursor:<init>	(Landroid/content/Context;)V
    //   8807: aastore
    //   8808: aload_0
    //   8809: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8812: iload_3
    //   8813: aaload
    //   8814: iload_3
    //   8815: invokestatic 362	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   8818: invokevirtual 1731	org/telegram/ui/Components/EditTextBoldCursor:setTag	(Ljava/lang/Object;)V
    //   8821: aload_0
    //   8822: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8825: iload_3
    //   8826: aaload
    //   8827: iconst_1
    //   8828: ldc_w 1732
    //   8831: invokevirtual 1736	org/telegram/ui/Components/EditTextBoldCursor:setTextSize	(IF)V
    //   8834: aload_0
    //   8835: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8838: iload_3
    //   8839: aaload
    //   8840: ldc_w 1738
    //   8843: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8846: invokevirtual 1741	org/telegram/ui/Components/EditTextBoldCursor:setHintTextColor	(I)V
    //   8849: aload_0
    //   8850: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8853: iload_3
    //   8854: aaload
    //   8855: ldc_w 1743
    //   8858: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8861: invokevirtual 1746	org/telegram/ui/Components/EditTextBoldCursor:setTextColor	(I)V
    //   8864: aload_0
    //   8865: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8868: iload_3
    //   8869: aaload
    //   8870: aconst_null
    //   8871: invokevirtual 1747	org/telegram/ui/Components/EditTextBoldCursor:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   8874: aload_0
    //   8875: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8878: iload_3
    //   8879: aaload
    //   8880: ldc_w 1743
    //   8883: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   8886: invokevirtual 1750	org/telegram/ui/Components/EditTextBoldCursor:setCursorColor	(I)V
    //   8889: aload_0
    //   8890: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8893: iload_3
    //   8894: aaload
    //   8895: ldc_w 1751
    //   8898: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   8901: invokevirtual 1754	org/telegram/ui/Components/EditTextBoldCursor:setCursorSize	(I)V
    //   8904: aload_0
    //   8905: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8908: iload_3
    //   8909: aaload
    //   8910: ldc_w 1755
    //   8913: invokevirtual 1759	org/telegram/ui/Components/EditTextBoldCursor:setCursorWidth	(F)V
    //   8916: iload_3
    //   8917: ifeq +8 -> 8925
    //   8920: iload_3
    //   8921: iconst_1
    //   8922: if_icmpne +313 -> 9235
    //   8925: aload_0
    //   8926: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8929: iload_3
    //   8930: aaload
    //   8931: sipush 129
    //   8934: invokevirtual 1767	org/telegram/ui/Components/EditTextBoldCursor:setInputType	(I)V
    //   8937: aload_0
    //   8938: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8941: iload_3
    //   8942: aaload
    //   8943: getstatic 2048	android/graphics/Typeface:DEFAULT	Landroid/graphics/Typeface;
    //   8946: invokevirtual 2052	org/telegram/ui/Components/EditTextBoldCursor:setTypeface	(Landroid/graphics/Typeface;)V
    //   8949: aload_0
    //   8950: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8953: iload_3
    //   8954: aaload
    //   8955: ldc_w 1768
    //   8958: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   8961: iload_3
    //   8962: tableswitch	default:+26->8988, 0:+288->9250, 1:+319->9281, 2:+340->9302
    //   8988: aload_0
    //   8989: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   8992: iload_3
    //   8993: aaload
    //   8994: iconst_0
    //   8995: iconst_0
    //   8996: iconst_0
    //   8997: ldc_w 1783
    //   9000: invokestatic 1594	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   9003: invokevirtual 1792	org/telegram/ui/Components/EditTextBoldCursor:setPadding	(IIII)V
    //   9006: aload_0
    //   9007: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9010: iload_3
    //   9011: aaload
    //   9012: astore 9
    //   9014: getstatic 1894	org/telegram/messenger/LocaleController:isRTL	Z
    //   9017: ifeq +306 -> 9323
    //   9020: iconst_5
    //   9021: istore 4
    //   9023: aload 9
    //   9025: iload 4
    //   9027: invokevirtual 1795	org/telegram/ui/Components/EditTextBoldCursor:setGravity	(I)V
    //   9030: aload 8
    //   9032: aload_0
    //   9033: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9036: iload_3
    //   9037: aaload
    //   9038: iconst_m1
    //   9039: ldc_w 1895
    //   9042: bipush 51
    //   9044: ldc_w 1781
    //   9047: ldc_w 1782
    //   9050: ldc_w 1781
    //   9053: ldc_w 1783
    //   9056: invokestatic 1644	org/telegram/ui/Components/LayoutHelper:createFrame	(IFIFFFF)Landroid/widget/FrameLayout$LayoutParams;
    //   9059: invokevirtual 1787	android/view/ViewGroup:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   9062: aload_0
    //   9063: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9066: iload_3
    //   9067: aaload
    //   9068: new 50	org/telegram/ui/PaymentFormActivity$25
    //   9071: dup
    //   9072: aload_0
    //   9073: invokespecial 2228	org/telegram/ui/PaymentFormActivity$25:<init>	(Lorg/telegram/ui/PaymentFormActivity;)V
    //   9076: invokevirtual 1815	org/telegram/ui/Components/EditTextBoldCursor:setOnEditorActionListener	(Landroid/widget/TextView$OnEditorActionListener;)V
    //   9079: iload_3
    //   9080: iconst_1
    //   9081: if_icmpne +248 -> 9329
    //   9084: aload_0
    //   9085: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9088: iconst_0
    //   9089: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   9092: dup
    //   9093: aload_1
    //   9094: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   9097: aastore
    //   9098: aload_0
    //   9099: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9102: iconst_0
    //   9103: aaload
    //   9104: ldc_w 2230
    //   9107: ldc_w 2231
    //   9110: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9113: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   9116: aload_0
    //   9117: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9120: iconst_0
    //   9121: aaload
    //   9122: aload_1
    //   9123: ldc_w 1548
    //   9126: ldc_w 1550
    //   9129: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   9132: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   9135: aload_0
    //   9136: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   9139: aload_0
    //   9140: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9143: iconst_0
    //   9144: aaload
    //   9145: iconst_m1
    //   9146: bipush -2
    //   9148: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   9151: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   9154: iload_3
    //   9155: iconst_1
    //   9156: iadd
    //   9157: istore_3
    //   9158: goto -528 -> 8630
    //   9161: iload_3
    //   9162: iconst_2
    //   9163: if_icmpne -458 -> 8705
    //   9166: aload_0
    //   9167: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   9170: iconst_1
    //   9171: new 298	org/telegram/ui/Cells/HeaderCell
    //   9174: dup
    //   9175: aload_1
    //   9176: invokespecial 1708	org/telegram/ui/Cells/HeaderCell:<init>	(Landroid/content/Context;)V
    //   9179: aastore
    //   9180: aload_0
    //   9181: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   9184: iconst_1
    //   9185: aaload
    //   9186: ldc_w 1710
    //   9189: invokestatic 1625	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   9192: invokevirtual 1711	org/telegram/ui/Cells/HeaderCell:setBackgroundColor	(I)V
    //   9195: aload_0
    //   9196: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   9199: iconst_1
    //   9200: aaload
    //   9201: ldc_w 2233
    //   9204: ldc_w 2234
    //   9207: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9210: invokevirtual 1716	org/telegram/ui/Cells/HeaderCell:setText	(Ljava/lang/String;)V
    //   9213: aload_0
    //   9214: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   9217: aload_0
    //   9218: getfield 300	org/telegram/ui/PaymentFormActivity:headerCell	[Lorg/telegram/ui/Cells/HeaderCell;
    //   9221: iconst_1
    //   9222: aaload
    //   9223: iconst_m1
    //   9224: bipush -2
    //   9226: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   9229: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   9232: goto -527 -> 8705
    //   9235: aload_0
    //   9236: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9239: iload_3
    //   9240: aaload
    //   9241: ldc_w 1938
    //   9244: invokevirtual 1771	org/telegram/ui/Components/EditTextBoldCursor:setImeOptions	(I)V
    //   9247: goto -286 -> 8961
    //   9250: aload_0
    //   9251: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9254: iload_3
    //   9255: aaload
    //   9256: ldc_w 2236
    //   9259: ldc_w 2237
    //   9262: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9265: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   9268: aload_0
    //   9269: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9272: iload_3
    //   9273: aaload
    //   9274: invokevirtual 2126	org/telegram/ui/Components/EditTextBoldCursor:requestFocus	()Z
    //   9277: pop
    //   9278: goto -290 -> 8988
    //   9281: aload_0
    //   9282: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9285: iload_3
    //   9286: aaload
    //   9287: ldc_w 2239
    //   9290: ldc_w 2240
    //   9293: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9296: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   9299: goto -311 -> 8988
    //   9302: aload_0
    //   9303: getfield 492	org/telegram/ui/PaymentFormActivity:inputFields	[Lorg/telegram/ui/Components/EditTextBoldCursor;
    //   9306: iload_3
    //   9307: aaload
    //   9308: ldc_w 2242
    //   9311: ldc_w 2243
    //   9314: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9317: invokevirtual 1862	org/telegram/ui/Components/EditTextBoldCursor:setHint	(Ljava/lang/CharSequence;)V
    //   9320: goto -332 -> 8988
    //   9323: iconst_3
    //   9324: istore 4
    //   9326: goto -303 -> 9023
    //   9329: iload_3
    //   9330: iconst_2
    //   9331: if_icmpne -177 -> 9154
    //   9334: aload_0
    //   9335: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9338: iconst_1
    //   9339: new 308	org/telegram/ui/Cells/TextInfoPrivacyCell
    //   9342: dup
    //   9343: aload_1
    //   9344: invokespecial 1905	org/telegram/ui/Cells/TextInfoPrivacyCell:<init>	(Landroid/content/Context;)V
    //   9347: aastore
    //   9348: aload_0
    //   9349: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9352: iconst_1
    //   9353: aaload
    //   9354: ldc_w 1497
    //   9357: ldc_w 1498
    //   9360: invokestatic 1135	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   9363: invokevirtual 1495	org/telegram/ui/Cells/TextInfoPrivacyCell:setText	(Ljava/lang/CharSequence;)V
    //   9366: aload_0
    //   9367: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9370: iconst_1
    //   9371: aaload
    //   9372: aload_1
    //   9373: ldc_w 1561
    //   9376: ldc_w 1550
    //   9379: invokestatic 1556	org/telegram/ui/ActionBar/Theme:getThemedDrawable	(Landroid/content/Context;ILjava/lang/String;)Landroid/graphics/drawable/Drawable;
    //   9382: invokevirtual 1906	org/telegram/ui/Cells/TextInfoPrivacyCell:setBackgroundDrawable	(Landroid/graphics/drawable/Drawable;)V
    //   9385: aload_0
    //   9386: getfield 1650	org/telegram/ui/PaymentFormActivity:linearLayout2	Landroid/widget/LinearLayout;
    //   9389: aload_0
    //   9390: getfield 310	org/telegram/ui/PaymentFormActivity:bottomCell	[Lorg/telegram/ui/Cells/TextInfoPrivacyCell;
    //   9393: iconst_1
    //   9394: aaload
    //   9395: iconst_m1
    //   9396: bipush -2
    //   9398: invokestatic 1720	org/telegram/ui/Components/LayoutHelper:createLinear	(II)Landroid/widget/LinearLayout$LayoutParams;
    //   9401: invokevirtual 1721	android/widget/LinearLayout:addView	(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V
    //   9404: goto -250 -> 9154
    //   9407: aload_0
    //   9408: invokespecial 642	org/telegram/ui/PaymentFormActivity:updatePasswordFields	()V
    //   9411: goto -5816 -> 3595
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	9414	0	this	PaymentFormActivity
    //   0	9414	1	paramContext	Context
    //   255	1246	2	f	float
    //   1177	8155	3	i	int
    //   1698	7627	4	j	int
    //   523	6365	5	k	int
    //   5884	63	6	bool1	boolean
    //   5895	58	7	bool2	boolean
    //   63	309	8	localObject1	Object
    //   491	3	8	localException1	Exception
    //   618	3236	8	localObject2	Object
    //   4275	3	8	localException2	Exception
    //   4295	1	8	localException3	Exception
    //   4332	685	8	localObject3	Object
    //   5040	3	8	localException4	Exception
    //   5070	576	8	localException5	Exception
    //   5845	3186	8	localObject4	Object
    //   376	3456	9	localObject5	Object
    //   4265	1	9	localException6	Exception
    //   4285	1	9	localException7	Exception
    //   4670	295	9	localObject6	Object
    //   5030	1	9	localException8	Exception
    //   5050	1	9	localException9	Exception
    //   5060	1	9	localException10	Exception
    //   5797	3227	9	localObject7	Object
    //   190	8187	10	localObject8	Object
    //   340	7791	11	localObject9	Object
    //   2391	4759	12	localObject10	Object
    // Exception table:
    //   from	to	target	type
    //   342	371	491	java/lang/Exception
    //   371	378	491	java/lang/Exception
    //   383	474	491	java/lang/Exception
    //   474	488	491	java/lang/Exception
    //   1504	1509	491	java/lang/Exception
    //   3513	3527	3764	java/lang/Exception
    //   3535	3544	3764	java/lang/Exception
    //   3812	3836	4265	java/lang/Exception
    //   3793	3812	4275	java/lang/Exception
    //   4267	4272	4275	java/lang/Exception
    //   4287	4292	4275	java/lang/Exception
    //   4297	4302	4275	java/lang/Exception
    //   3836	3852	4285	java/lang/Exception
    //   3852	3864	4295	java/lang/Exception
    //   4334	4346	5030	java/lang/Exception
    //   4315	4334	5040	java/lang/Exception
    //   5032	5037	5040	java/lang/Exception
    //   5052	5057	5040	java/lang/Exception
    //   5062	5067	5040	java/lang/Exception
    //   5072	5079	5040	java/lang/Exception
    //   4346	4358	5050	java/lang/Exception
    //   4358	4370	5060	java/lang/Exception
    //   4370	4382	5070	java/lang/Exception
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.didSetTwoStepPassword)
    {
      this.paymentForm.password_missing = false;
      this.paymentForm.can_save_credentials = true;
      updateSavePaymentField();
    }
    do
    {
      return;
      if (paramInt1 == NotificationCenter.didRemovedTwoStepPassword)
      {
        this.paymentForm.password_missing = true;
        this.paymentForm.can_save_credentials = false;
        updateSavePaymentField();
        return;
      }
    } while (paramInt1 != NotificationCenter.paymentFinished);
    removeSelfFromStack();
  }
  
  @SuppressLint({"HardwareIds"})
  public void fillNumber(String paramString)
  {
    for (;;)
    {
      int j;
      int i;
      try
      {
        TelephonyManager localTelephonyManager = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
        j = 1;
        i = 1;
        if ((paramString != null) || ((localTelephonyManager.getSimState() != 1) && (localTelephonyManager.getPhoneType() != 0)))
        {
          if (Build.VERSION.SDK_INT < 23) {
            break label289;
          }
          if (getParentActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            continue;
          }
          i = 1;
          if (getParentActivity().checkSelfPermission("android.permission.RECEIVE_SMS") != 0) {
            continue;
          }
          k = 1;
          j = i;
          i = k;
          break label289;
          String str1 = paramString;
          if (paramString == null) {
            str1 = PhoneFormat.stripExceptNumbers(localTelephonyManager.getLine1Number());
          }
          paramString = null;
          localTelephonyManager = null;
          k = 0;
          if (!TextUtils.isEmpty(str1))
          {
            if (str1.length() > 4)
            {
              i = 4;
              j = k;
              paramString = localTelephonyManager;
              if (i >= 1)
              {
                String str2 = str1.substring(0, i);
                if ((String)this.codesMap.get(str2) == null) {
                  continue;
                }
                j = 1;
                paramString = str1.substring(i, str1.length());
                this.inputFields[8].setText(str2);
              }
              if (j == 0)
              {
                paramString = str1.substring(1, str1.length());
                this.inputFields[8].setText(str1.substring(0, 1));
              }
            }
            if (paramString != null)
            {
              this.inputFields[9].setText(paramString);
              this.inputFields[9].setSelection(this.inputFields[9].length());
            }
          }
        }
        return;
        i = 0;
        continue;
        int k = 0;
        j = i;
        i = k;
        break label289;
        i -= 1;
        continue;
        if (paramString != null) {
          continue;
        }
      }
      catch (Exception paramString)
      {
        FileLog.e(paramString);
        return;
      }
      label289:
      if (j == 0) {
        if (i == 0) {}
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"));
    localArrayList.add(new ThemeDescription(this.scrollView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch"));
    localArrayList.add(new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder"));
    LinearLayout localLinearLayout = this.linearLayout2;
    Paint localPaint = Theme.dividerPaint;
    localArrayList.add(new ThemeDescription(localLinearLayout, 0, new Class[] { View.class }, localPaint, null, null, "divider"));
    localArrayList.add(new ThemeDescription(this.progressView, 0, null, null, null, null, "contextProgressInner2"));
    localArrayList.add(new ThemeDescription(this.progressView, 0, null, null, null, null, "contextProgressOuter2"));
    localArrayList.add(new ThemeDescription(this.progressViewButton, 0, null, null, null, null, "contextProgressInner2"));
    localArrayList.add(new ThemeDescription(this.progressViewButton, 0, null, null, null, null, "contextProgressOuter2"));
    if (this.inputFields != null)
    {
      i = 0;
      while (i < this.inputFields.length)
      {
        localArrayList.add(new ThemeDescription((View)this.inputFields[i].getParent(), ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"));
        localArrayList.add(new ThemeDescription(this.inputFields[i], ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
        localArrayList.add(new ThemeDescription(this.inputFields[i], ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"));
        i += 1;
      }
    }
    localArrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"));
    if (this.radioCells != null)
    {
      i = 0;
      while (i < this.radioCells.length)
      {
        localArrayList.add(new ThemeDescription(this.radioCells[i], ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "windowBackgroundWhite"));
        localArrayList.add(new ThemeDescription(this.radioCells[i], ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "listSelectorSDK21"));
        localArrayList.add(new ThemeDescription(this.radioCells[i], 0, new Class[] { RadioCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
        localArrayList.add(new ThemeDescription(this.radioCells[i], ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"));
        localArrayList.add(new ThemeDescription(this.radioCells[i], ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"));
        i += 1;
      }
    }
    localArrayList.add(new ThemeDescription(null, 0, new Class[] { RadioCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"));
    localArrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"));
    int i = 0;
    while (i < this.headerCell.length)
    {
      localArrayList.add(new ThemeDescription(this.headerCell[i], ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"));
      localArrayList.add(new ThemeDescription(this.headerCell[i], 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"));
      i += 1;
    }
    i = 0;
    while (i < this.sectionCell.length)
    {
      localArrayList.add(new ThemeDescription(this.sectionCell[i], ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"));
      i += 1;
    }
    i = 0;
    while (i < this.bottomCell.length)
    {
      localArrayList.add(new ThemeDescription(this.bottomCell[i], ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"));
      localArrayList.add(new ThemeDescription(this.bottomCell[i], 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"));
      localArrayList.add(new ThemeDescription(this.bottomCell[i], ThemeDescription.FLAG_LINKCOLOR, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteLinkText"));
      i += 1;
    }
    i = 0;
    while (i < this.dividers.size())
    {
      localArrayList.add(new ThemeDescription((View)this.dividers.get(i), ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "divider"));
      i += 1;
    }
    localArrayList.add(new ThemeDescription(this.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.checkCell1, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.checkCell1, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"));
    localArrayList.add(new ThemeDescription(this.checkCell1, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"));
    localArrayList.add(new ThemeDescription(this.checkCell1, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"));
    localArrayList.add(new ThemeDescription(this.checkCell1, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"));
    localArrayList.add(new ThemeDescription(this.checkCell1, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.checkCell1, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "listSelectorSDK21"));
    localArrayList.add(new ThemeDescription(this.settingsCell1, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.settingsCell1, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "listSelectorSDK21"));
    localArrayList.add(new ThemeDescription(this.settingsCell1, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.payTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlueText6"));
    localArrayList.add(new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextPriceCell.class }, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextPriceCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextPriceCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextPriceCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2"));
    localArrayList.add(new ThemeDescription(this.linearLayout2, ThemeDescription.FLAG_CHECKTAG, new Class[] { TextPriceCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"));
    localArrayList.add(new ThemeDescription(this.detailSettingsCell[0], ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.detailSettingsCell[0], ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "listSelectorSDK21"));
    i = 1;
    while (i < this.detailSettingsCell.length)
    {
      localArrayList.add(new ThemeDescription(this.detailSettingsCell[i], ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"));
      localArrayList.add(new ThemeDescription(this.detailSettingsCell[i], 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"));
      localArrayList.add(new ThemeDescription(this.detailSettingsCell[i], 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"));
      i += 1;
    }
    localArrayList.add(new ThemeDescription(this.paymentInfoCell, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.paymentInfoCell, 0, new Class[] { PaymentInfoCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.paymentInfoCell, 0, new Class[] { PaymentInfoCell.class }, new String[] { "detailTextView" }, null, null, null, "windowBackgroundWhiteBlackText"));
    localArrayList.add(new ThemeDescription(this.paymentInfoCell, 0, new Class[] { PaymentInfoCell.class }, new String[] { "detailExTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"));
    localArrayList.add(new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "windowBackgroundWhite"));
    localArrayList.add(new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_SELECTORWHITE, null, null, null, null, "listSelectorSDK21"));
    return (ThemeDescription[])localArrayList.toArray(new ThemeDescription[localArrayList.size()]);
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    Object localObject;
    if (paramInt1 == 1000) {
      if (paramInt2 == -1)
      {
        showEditDoneProgress(true, true);
        setDonePressed(true);
        paramIntent = (MaskedWallet)paramIntent.getParcelableExtra("com.google.android.gms.wallet.EXTRA_MASKED_WALLET");
        localObject = Cart.newBuilder().setCurrencyCode(this.paymentForm.invoice.currency).setTotalPrice(this.totalPriceDecimal);
        localArrayList = new ArrayList();
        localArrayList.addAll(this.paymentForm.invoice.prices);
        if (this.shippingOption != null) {
          localArrayList.addAll(this.shippingOption.prices);
        }
        paramInt1 = 0;
        while (paramInt1 < localArrayList.size())
        {
          localTL_labeledPrice = (TLRPC.TL_labeledPrice)localArrayList.get(paramInt1);
          str = LocaleController.getInstance().formatCurrencyDecimalString(localTL_labeledPrice.amount, this.paymentForm.invoice.currency, false);
          ((Cart.Builder)localObject).addLineItem(LineItem.newBuilder().setCurrencyCode(this.paymentForm.invoice.currency).setQuantity("1").setDescription(localTL_labeledPrice.label).setTotalPrice(str).setUnitPrice(str).build());
          paramInt1 += 1;
        }
        paramIntent = FullWalletRequest.newBuilder().setCart(((Cart.Builder)localObject).build()).setGoogleTransactionId(paramIntent.getGoogleTransactionId()).build();
        Wallet.Payments.loadFullWallet(this.googleApiClient, paramIntent, 1001);
      }
    }
    while (paramInt1 != 1001)
    {
      ArrayList localArrayList;
      TLRPC.TL_labeledPrice localTL_labeledPrice;
      String str;
      return;
      showEditDoneProgress(true, false);
      setDonePressed(false);
      return;
    }
    if (paramInt2 == -1)
    {
      paramIntent = (FullWallet)paramIntent.getParcelableExtra("com.google.android.gms.wallet.EXTRA_FULL_WALLET");
      localObject = paramIntent.getPaymentMethodToken().getToken();
      for (;;)
      {
        try
        {
          if (this.androidPayPublicKey == null) {
            break label400;
          }
          this.androidPayCredentials = new TLRPC.TL_inputPaymentCredentialsAndroidPay();
          this.androidPayCredentials.payment_token = new TLRPC.TL_dataJSON();
          this.androidPayCredentials.payment_token.data = ((String)localObject);
          this.androidPayCredentials.google_transaction_id = paramIntent.getGoogleTransactionId();
          paramIntent = paramIntent.getPaymentDescriptions();
          if (paramIntent.length > 0)
          {
            this.cardName = paramIntent[0];
            goToNextStep();
            showEditDoneProgress(true, false);
            setDonePressed(false);
            return;
          }
        }
        catch (JSONException paramIntent)
        {
          showEditDoneProgress(true, false);
          setDonePressed(false);
          return;
        }
        this.cardName = "Android Pay";
        continue;
        label400:
        paramIntent = TokenParser.parseToken((String)localObject);
        this.paymentJson = String.format(Locale.US, "{\"type\":\"%1$s\", \"id\":\"%2$s\"}", new Object[] { paramIntent.getType(), paramIntent.getId() });
        paramIntent = paramIntent.getCard();
        this.cardName = (paramIntent.getType() + " *" + paramIntent.getLast4());
      }
    }
    showEditDoneProgress(true, false);
    setDonePressed(false);
  }
  
  public boolean onBackPressed()
  {
    return !this.donePressed;
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didSetTwoStepPassword);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didRemovedTwoStepPassword);
    if (this.currentStep != 4) {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.paymentFinished);
    }
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    if (this.delegate != null) {
      this.delegate.onFragmentDestroyed();
    }
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didSetTwoStepPassword);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didRemovedTwoStepPassword);
    if (this.currentStep != 4) {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.paymentFinished);
    }
    if (this.webView != null) {}
    for (;;)
    {
      try
      {
        ViewParent localViewParent = this.webView.getParent();
        if (localViewParent != null) {
          ((FrameLayout)localViewParent).removeView(this.webView);
        }
        this.webView.stopLoading();
        this.webView.loadUrl("about:blank");
        this.webView.destroy();
        this.webView = null;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        continue;
      }
      try
      {
        if (((this.currentStep == 2) || (this.currentStep == 6)) && (Build.VERSION.SDK_INT >= 23) && ((SharedConfig.passcodeHash.length() == 0) || (SharedConfig.allowScreenCapture))) {
          getParentActivity().getWindow().clearFlags(8192);
        }
      }
      catch (Throwable localThrowable)
      {
        FileLog.e(localThrowable);
      }
    }
    super.onFragmentDestroy();
    this.canceled = true;
  }
  
  public void onPause()
  {
    if (this.googleApiClient != null) {
      this.googleApiClient.disconnect();
    }
  }
  
  public void onResume()
  {
    super.onResume();
    AndroidUtilities.requestAdjustResize(getParentActivity(), this.classGuid);
    if (Build.VERSION.SDK_INT >= 23) {}
    for (;;)
    {
      try
      {
        if (((this.currentStep != 2) && (this.currentStep != 6)) || (this.paymentForm.invoice.test)) {
          continue;
        }
        getParentActivity().getWindow().setFlags(8192, 8192);
      }
      catch (Throwable localThrowable)
      {
        FileLog.e(localThrowable);
        continue;
      }
      if (this.googleApiClient != null) {
        this.googleApiClient.connect();
      }
      return;
      if ((SharedConfig.passcodeHash.length() == 0) || (SharedConfig.allowScreenCapture)) {
        getParentActivity().getWindow().clearFlags(8192);
      }
    }
  }
  
  protected void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (!paramBoolean2))
    {
      if (this.webView == null) {
        break label38;
      }
      if (this.currentStep != 4) {
        this.webView.loadUrl(this.paymentForm.url);
      }
    }
    label38:
    do
    {
      return;
      if (this.currentStep == 2)
      {
        this.inputFields[0].requestFocus();
        AndroidUtilities.showKeyboard(this.inputFields[0]);
        return;
      }
      if (this.currentStep == 3)
      {
        this.inputFields[1].requestFocus();
        AndroidUtilities.showKeyboard(this.inputFields[1]);
        return;
      }
    } while ((this.currentStep != 6) || (this.waitingForEmail));
    this.inputFields[0].requestFocus();
    AndroidUtilities.showKeyboard(this.inputFields[0]);
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
    public LinkSpan() {}
    
    public void onClick(View paramView)
    {
      PaymentFormActivity.this.presentFragment(new TwoStepVerificationActivity(0));
    }
    
    public void updateDrawState(TextPaint paramTextPaint)
    {
      super.updateDrawState(paramTextPaint);
      paramTextPaint.setUnderlineText(false);
    }
  }
  
  private static abstract interface PaymentFormActivityDelegate
  {
    public abstract void currentPasswordUpdated(TLRPC.account_Password paramaccount_Password);
    
    public abstract boolean didSelectNewCard(String paramString1, String paramString2, boolean paramBoolean, TLRPC.TL_inputPaymentCredentialsAndroidPay paramTL_inputPaymentCredentialsAndroidPay);
    
    public abstract void onFragmentDestroyed();
  }
  
  private class TelegramWebviewProxy
  {
    private TelegramWebviewProxy() {}
    
    @JavascriptInterface
    public void postEvent(final String paramString1, final String paramString2)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (PaymentFormActivity.this.getParentActivity() == null) {}
          while (!paramString1.equals("payment_form_submit")) {
            return;
          }
          try
          {
            JSONObject localJSONObject1 = new JSONObject(paramString2);
            JSONObject localJSONObject2 = localJSONObject1.getJSONObject("credentials");
            PaymentFormActivity.access$002(PaymentFormActivity.this, localJSONObject2.toString());
            PaymentFormActivity.access$102(PaymentFormActivity.this, localJSONObject1.getString("title"));
            PaymentFormActivity.this.goToNextStep();
            return;
          }
          catch (Throwable localThrowable)
          {
            for (;;)
            {
              PaymentFormActivity.access$002(PaymentFormActivity.this, paramString2);
              FileLog.e(localThrowable);
            }
          }
        }
      });
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/PaymentFormActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */