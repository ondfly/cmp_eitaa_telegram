package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_contacts_importedContacts;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPhoneContact;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;

public class NewContactActivity
  extends BaseFragment
  implements AdapterView.OnItemSelectedListener
{
  private static final int done_button = 1;
  private AvatarDrawable avatarDrawable;
  private BackupImageView avatarImage;
  private EditTextBoldCursor codeField;
  private HashMap<String, String> codesMap = new HashMap();
  private ArrayList<String> countriesArray = new ArrayList();
  private HashMap<String, String> countriesMap = new HashMap();
  private TextView countryButton;
  private int countryState;
  private boolean donePressed;
  private ActionBarMenuItem editDoneItem;
  private AnimatorSet editDoneItemAnimation;
  private ContextProgressView editDoneItemProgress;
  private EditTextBoldCursor firstNameField;
  private boolean ignoreOnPhoneChange;
  private boolean ignoreOnTextChange;
  private boolean ignoreSelection;
  private EditTextBoldCursor lastNameField;
  private View lineView;
  private HintEditText phoneField;
  private HashMap<String, String> phoneFormatMap = new HashMap();
  private TextView textView;
  
  private void showEditDoneProgress(final boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.editDoneItemAnimation != null) {
      this.editDoneItemAnimation.cancel();
    }
    if (!paramBoolean2)
    {
      if (paramBoolean1)
      {
        this.editDoneItem.getImageView().setScaleX(0.1F);
        this.editDoneItem.getImageView().setScaleY(0.1F);
        this.editDoneItem.getImageView().setAlpha(0.0F);
        this.editDoneItemProgress.setScaleX(1.0F);
        this.editDoneItemProgress.setScaleY(1.0F);
        this.editDoneItemProgress.setAlpha(1.0F);
        this.editDoneItem.getImageView().setVisibility(4);
        this.editDoneItemProgress.setVisibility(0);
        this.editDoneItem.setEnabled(false);
        return;
      }
      this.editDoneItemProgress.setScaleX(0.1F);
      this.editDoneItemProgress.setScaleY(0.1F);
      this.editDoneItemProgress.setAlpha(0.0F);
      this.editDoneItem.getImageView().setScaleX(1.0F);
      this.editDoneItem.getImageView().setScaleY(1.0F);
      this.editDoneItem.getImageView().setAlpha(1.0F);
      this.editDoneItem.getImageView().setVisibility(0);
      this.editDoneItemProgress.setVisibility(4);
      this.editDoneItem.setEnabled(true);
      return;
    }
    this.editDoneItemAnimation = new AnimatorSet();
    if (paramBoolean1)
    {
      this.editDoneItemProgress.setVisibility(0);
      this.editDoneItem.setEnabled(false);
      this.editDoneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.editDoneItemProgress, "alpha", new float[] { 1.0F }) });
    }
    for (;;)
    {
      this.editDoneItemAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if ((NewContactActivity.this.editDoneItemAnimation != null) && (NewContactActivity.this.editDoneItemAnimation.equals(paramAnonymousAnimator))) {
            NewContactActivity.access$2302(NewContactActivity.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((NewContactActivity.this.editDoneItemAnimation != null) && (NewContactActivity.this.editDoneItemAnimation.equals(paramAnonymousAnimator)))
          {
            if (!paramBoolean1) {
              NewContactActivity.this.editDoneItemProgress.setVisibility(4);
            }
          }
          else {
            return;
          }
          NewContactActivity.this.editDoneItem.getImageView().setVisibility(4);
        }
      });
      this.editDoneItemAnimation.setDuration(150L);
      this.editDoneItemAnimation.start();
      return;
      this.editDoneItem.getImageView().setVisibility(0);
      this.editDoneItem.setEnabled(true);
      this.editDoneItemAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleX", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.editDoneItemProgress, "scaleY", new float[] { 0.1F }), ObjectAnimator.ofFloat(this.editDoneItemProgress, "alpha", new float[] { 0.0F }), ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.editDoneItem.getImageView(), "alpha", new float[] { 1.0F }) });
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("AddContactTitle", 2131492928));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          NewContactActivity.this.finishFragment();
        }
        while ((paramAnonymousInt != 1) || (NewContactActivity.this.donePressed)) {
          return;
        }
        if (NewContactActivity.this.firstNameField.length() == 0)
        {
          localObject = (Vibrator)NewContactActivity.this.getParentActivity().getSystemService("vibrator");
          if (localObject != null) {
            ((Vibrator)localObject).vibrate(200L);
          }
          AndroidUtilities.shakeView(NewContactActivity.this.firstNameField, 2.0F, 0);
          return;
        }
        if (NewContactActivity.this.codeField.length() == 0)
        {
          localObject = (Vibrator)NewContactActivity.this.getParentActivity().getSystemService("vibrator");
          if (localObject != null) {
            ((Vibrator)localObject).vibrate(200L);
          }
          AndroidUtilities.shakeView(NewContactActivity.this.codeField, 2.0F, 0);
          return;
        }
        if (NewContactActivity.this.phoneField.length() == 0)
        {
          localObject = (Vibrator)NewContactActivity.this.getParentActivity().getSystemService("vibrator");
          if (localObject != null) {
            ((Vibrator)localObject).vibrate(200L);
          }
          AndroidUtilities.shakeView(NewContactActivity.this.phoneField, 2.0F, 0);
          return;
        }
        NewContactActivity.access$002(NewContactActivity.this, true);
        NewContactActivity.this.showEditDoneProgress(true, true);
        final Object localObject = new TLRPC.TL_contacts_importContacts();
        final TLRPC.TL_inputPhoneContact localTL_inputPhoneContact = new TLRPC.TL_inputPhoneContact();
        localTL_inputPhoneContact.first_name = NewContactActivity.this.firstNameField.getText().toString();
        localTL_inputPhoneContact.last_name = NewContactActivity.this.lastNameField.getText().toString();
        localTL_inputPhoneContact.phone = ("+" + NewContactActivity.this.codeField.getText().toString() + NewContactActivity.this.phoneField.getText().toString());
        ((TLRPC.TL_contacts_importContacts)localObject).contacts.add(localTL_inputPhoneContact);
        paramAnonymousInt = ConnectionsManager.getInstance(NewContactActivity.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NewContactActivity.access$002(NewContactActivity.this, false);
                if (paramAnonymous2TLObject != null)
                {
                  if (!paramAnonymous2TLObject.users.isEmpty())
                  {
                    MessagesController.getInstance(NewContactActivity.this.currentAccount).putUsers(paramAnonymous2TLObject.users, false);
                    MessagesController.openChatOrProfileWith((TLRPC.User)paramAnonymous2TLObject.users.get(0), null, NewContactActivity.this, 1, true);
                  }
                  while (NewContactActivity.this.getParentActivity() == null) {
                    return;
                  }
                  NewContactActivity.this.showEditDoneProgress(false, true);
                  AlertDialog.Builder localBuilder = new AlertDialog.Builder(NewContactActivity.this.getParentActivity());
                  localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                  localBuilder.setMessage(LocaleController.formatString("ContactNotRegistered", 2131493289, new Object[] { ContactsController.formatName(NewContactActivity.1.1.this.val$inputPhoneContact.first_name, NewContactActivity.1.1.this.val$inputPhoneContact.last_name) }));
                  localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                  localBuilder.setPositiveButton(LocaleController.getString("Invite", 2131493684), new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous4DialogInterface, int paramAnonymous4Int)
                    {
                      try
                      {
                        paramAnonymous4DialogInterface = new Intent("android.intent.action.VIEW", Uri.fromParts("sms", NewContactActivity.1.1.this.val$inputPhoneContact.phone, null));
                        paramAnonymous4DialogInterface.putExtra("sms_body", ContactsController.getInstance(NewContactActivity.this.currentAccount).getInviteText(1));
                        NewContactActivity.this.getParentActivity().startActivityForResult(paramAnonymous4DialogInterface, 500);
                        return;
                      }
                      catch (Exception paramAnonymous4DialogInterface)
                      {
                        FileLog.e(paramAnonymous4DialogInterface);
                      }
                    }
                  });
                  NewContactActivity.this.showDialog(localBuilder.create());
                  return;
                }
                NewContactActivity.this.showEditDoneProgress(false, true);
                AlertsCreator.processError(NewContactActivity.this.currentAccount, paramAnonymous2TL_error, NewContactActivity.this, NewContactActivity.1.1.this.val$req, new Object[0]);
              }
            });
          }
        }, 2);
        ConnectionsManager.getInstance(NewContactActivity.this.currentAccount).bindRequestToGuid(paramAnonymousInt, NewContactActivity.this.classGuid);
      }
    });
    this.avatarDrawable = new AvatarDrawable();
    this.avatarDrawable.setInfo(5, "", "", false);
    this.editDoneItem = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    this.editDoneItemProgress = new ContextProgressView(paramContext, 1);
    this.editDoneItem.addView(this.editDoneItemProgress, LayoutHelper.createFrame(-1, -1.0F));
    this.editDoneItemProgress.setVisibility(4);
    this.fragmentView = new ScrollView(paramContext);
    localObject1 = new LinearLayout(paramContext);
    ((LinearLayout)localObject1).setPadding(AndroidUtilities.dp(24.0F), 0, AndroidUtilities.dp(24.0F), 0);
    ((LinearLayout)localObject1).setOrientation(1);
    ((ScrollView)this.fragmentView).addView((View)localObject1, LayoutHelper.createScroll(-1, -2, 51));
    ((LinearLayout)localObject1).setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    Object localObject2 = new FrameLayout(paramContext);
    ((LinearLayout)localObject1).addView((View)localObject2, LayoutHelper.createLinear(-1, -2, 0.0F, 24.0F, 0.0F, 0.0F));
    this.avatarImage = new BackupImageView(paramContext);
    this.avatarImage.setImageDrawable(this.avatarDrawable);
    ((FrameLayout)localObject2).addView(this.avatarImage, LayoutHelper.createFrame(60, 60.0F, 51, 0.0F, 9.0F, 0.0F, 0.0F));
    this.firstNameField = new EditTextBoldCursor(paramContext);
    this.firstNameField.setTextSize(1, 18.0F);
    this.firstNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.firstNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.firstNameField.setMaxLines(1);
    this.firstNameField.setLines(1);
    this.firstNameField.setSingleLine(true);
    this.firstNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
    this.firstNameField.setGravity(3);
    this.firstNameField.setInputType(49152);
    this.firstNameField.setImeOptions(5);
    this.firstNameField.setHint(LocaleController.getString("FirstName", 2131493542));
    this.firstNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.firstNameField.setCursorSize(AndroidUtilities.dp(20.0F));
    this.firstNameField.setCursorWidth(1.5F);
    ((FrameLayout)localObject2).addView(this.firstNameField, LayoutHelper.createFrame(-1, 34.0F, 51, 84.0F, 0.0F, 0.0F, 0.0F));
    this.firstNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
    {
      public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
      {
        if (paramAnonymousInt == 5)
        {
          NewContactActivity.this.lastNameField.requestFocus();
          NewContactActivity.this.lastNameField.setSelection(NewContactActivity.this.lastNameField.length());
          return true;
        }
        return false;
      }
    });
    this.firstNameField.addTextChangedListener(new TextWatcher()
    {
      public void afterTextChanged(Editable paramAnonymousEditable)
      {
        NewContactActivity.this.avatarDrawable.setInfo(5, NewContactActivity.this.firstNameField.getText().toString(), NewContactActivity.this.lastNameField.getText().toString(), false);
        NewContactActivity.this.avatarImage.invalidate();
      }
      
      public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      
      public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
    });
    this.lastNameField = new EditTextBoldCursor(paramContext);
    this.lastNameField.setTextSize(1, 18.0F);
    this.lastNameField.setHintTextColor(Theme.getColor("windowBackgroundWhiteHintText"));
    this.lastNameField.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.lastNameField.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, false));
    this.lastNameField.setMaxLines(1);
    this.lastNameField.setLines(1);
    this.lastNameField.setSingleLine(true);
    this.lastNameField.setGravity(3);
    this.lastNameField.setInputType(49152);
    this.lastNameField.setImeOptions(5);
    this.lastNameField.setHint(LocaleController.getString("LastName", 2131493726));
    this.lastNameField.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.lastNameField.setCursorSize(AndroidUtilities.dp(20.0F));
    this.lastNameField.setCursorWidth(1.5F);
    ((FrameLayout)localObject2).addView(this.lastNameField, LayoutHelper.createFrame(-1, 34.0F, 51, 84.0F, 44.0F, 0.0F, 0.0F));
    this.lastNameField.setOnEditorActionListener(new TextView.OnEditorActionListener()
    {
      public boolean onEditorAction(TextView paramAnonymousTextView, int paramAnonymousInt, KeyEvent paramAnonymousKeyEvent)
      {
        if (paramAnonymousInt == 5)
        {
          NewContactActivity.this.phoneField.requestFocus();
          NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
          return true;
        }
        return false;
      }
    });
    this.lastNameField.addTextChangedListener(new TextWatcher()
    {
      public void afterTextChanged(Editable paramAnonymousEditable)
      {
        NewContactActivity.this.avatarDrawable.setInfo(5, NewContactActivity.this.firstNameField.getText().toString(), NewContactActivity.this.lastNameField.getText().toString(), false);
        NewContactActivity.this.avatarImage.invalidate();
      }
      
      public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
      
      public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}
    });
    this.countryButton = new TextView(paramContext);
    this.countryButton.setTextSize(1, 18.0F);
    this.countryButton.setPadding(AndroidUtilities.dp(6.0F), AndroidUtilities.dp(10.0F), AndroidUtilities.dp(6.0F), 0);
    this.countryButton.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.countryButton.setMaxLines(1);
    this.countryButton.setSingleLine(true);
    this.countryButton.setEllipsize(TextUtils.TruncateAt.END);
    this.countryButton.setGravity(3);
    this.countryButton.setBackgroundResource(2131165650);
    ((LinearLayout)localObject1).addView(this.countryButton, LayoutHelper.createLinear(-1, 36, 0.0F, 24.0F, 0.0F, 14.0F));
    this.countryButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        paramAnonymousView = new CountrySelectActivity(true);
        paramAnonymousView.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate()
        {
          public void didSelectCountry(String paramAnonymous2String1, String paramAnonymous2String2)
          {
            NewContactActivity.this.selectCountry(paramAnonymous2String1);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                AndroidUtilities.showKeyboard(NewContactActivity.this.phoneField);
              }
            }, 300L);
            NewContactActivity.this.phoneField.requestFocus();
            NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
          }
        });
        NewContactActivity.this.presentFragment(paramAnonymousView);
      }
    });
    this.lineView = new View(paramContext);
    this.lineView.setPadding(AndroidUtilities.dp(8.0F), 0, AndroidUtilities.dp(8.0F), 0);
    this.lineView.setBackgroundColor(Theme.getColor("windowBackgroundWhiteGrayLine"));
    ((LinearLayout)localObject1).addView(this.lineView, LayoutHelper.createLinear(-1, 1, 0.0F, -17.5F, 0.0F, 0.0F));
    localObject2 = new LinearLayout(paramContext);
    ((LinearLayout)localObject2).setOrientation(0);
    ((LinearLayout)localObject1).addView((View)localObject2, LayoutHelper.createLinear(-1, -2, 0.0F, 20.0F, 0.0F, 0.0F));
    this.textView = new TextView(paramContext);
    this.textView.setText("+");
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 18.0F);
    ((LinearLayout)localObject2).addView(this.textView, LayoutHelper.createLinear(-2, -2));
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
    localObject1 = new InputFilter.LengthFilter(5);
    this.codeField.setFilters(new InputFilter[] { localObject1 });
    ((LinearLayout)localObject2).addView(this.codeField, LayoutHelper.createLinear(55, 36, -9.0F, 0.0F, 16.0F, 0.0F));
    this.codeField.addTextChangedListener(new TextWatcher()
    {
      public void afterTextChanged(Editable paramAnonymousEditable)
      {
        if (NewContactActivity.this.ignoreOnTextChange) {
          return;
        }
        NewContactActivity.access$1402(NewContactActivity.this, true);
        Object localObject3 = PhoneFormat.stripExceptNumbers(NewContactActivity.this.codeField.getText().toString());
        NewContactActivity.this.codeField.setText((CharSequence)localObject3);
        if (((String)localObject3).length() == 0)
        {
          NewContactActivity.this.countryButton.setText(LocaleController.getString("ChooseCountry", 2131493246));
          NewContactActivity.this.phoneField.setHintText(null);
          NewContactActivity.access$1602(NewContactActivity.this, 1);
          NewContactActivity.access$1402(NewContactActivity.this, false);
          return;
        }
        int j = 0;
        int k = 0;
        paramAnonymousEditable = null;
        Object localObject4 = null;
        Object localObject2 = localObject3;
        label139:
        int i;
        if (((String)localObject3).length() > 4)
        {
          NewContactActivity.access$1402(NewContactActivity.this, true);
          j = 4;
          i = k;
          localObject1 = localObject3;
          paramAnonymousEditable = (Editable)localObject4;
          if (j >= 1)
          {
            paramAnonymousEditable = ((String)localObject3).substring(0, j);
            if ((String)NewContactActivity.this.codesMap.get(paramAnonymousEditable) == null) {
              break label541;
            }
            i = 1;
            localObject2 = ((String)localObject3).substring(j, ((String)localObject3).length()) + NewContactActivity.this.phoneField.getText().toString();
            localObject3 = NewContactActivity.this.codeField;
            localObject1 = paramAnonymousEditable;
            ((EditTextBoldCursor)localObject3).setText(paramAnonymousEditable);
            paramAnonymousEditable = (Editable)localObject2;
          }
          j = i;
          localObject2 = localObject1;
          if (i == 0)
          {
            NewContactActivity.access$1402(NewContactActivity.this, true);
            paramAnonymousEditable = ((String)localObject1).substring(1, ((String)localObject1).length()) + NewContactActivity.this.phoneField.getText().toString();
            localObject3 = NewContactActivity.this.codeField;
            localObject2 = ((String)localObject1).substring(0, 1);
            ((EditTextBoldCursor)localObject3).setText((CharSequence)localObject2);
            j = i;
          }
        }
        Object localObject1 = (String)NewContactActivity.this.codesMap.get(localObject2);
        if (localObject1 != null)
        {
          i = NewContactActivity.this.countriesArray.indexOf(localObject1);
          if (i != -1)
          {
            NewContactActivity.access$1902(NewContactActivity.this, true);
            NewContactActivity.this.countryButton.setText((CharSequence)NewContactActivity.this.countriesArray.get(i));
            localObject1 = (String)NewContactActivity.this.phoneFormatMap.get(localObject2);
            localObject2 = NewContactActivity.this.phoneField;
            if (localObject1 != null)
            {
              localObject1 = ((String)localObject1).replace('X', '–');
              label447:
              ((HintEditText)localObject2).setHintText((String)localObject1);
              NewContactActivity.access$1602(NewContactActivity.this, 0);
            }
          }
        }
        for (;;)
        {
          if (j == 0) {
            NewContactActivity.this.codeField.setSelection(NewContactActivity.this.codeField.getText().length());
          }
          if (paramAnonymousEditable == null) {
            break;
          }
          NewContactActivity.this.phoneField.requestFocus();
          NewContactActivity.this.phoneField.setText(paramAnonymousEditable);
          NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
          break;
          label541:
          j -= 1;
          break label139;
          localObject1 = null;
          break label447;
          NewContactActivity.this.countryButton.setText(LocaleController.getString("WrongCountry", 2131494644));
          NewContactActivity.this.phoneField.setHintText(null);
          NewContactActivity.access$1602(NewContactActivity.this, 2);
          continue;
          NewContactActivity.this.countryButton.setText(LocaleController.getString("WrongCountry", 2131494644));
          NewContactActivity.this.phoneField.setHintText(null);
          NewContactActivity.access$1602(NewContactActivity.this, 2);
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
          NewContactActivity.this.phoneField.requestFocus();
          NewContactActivity.this.phoneField.setSelection(NewContactActivity.this.phoneField.length());
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
    this.phoneField.setImeOptions(268435462);
    ((LinearLayout)localObject2).addView(this.phoneField, LayoutHelper.createFrame(-1, 36.0F));
    this.phoneField.addTextChangedListener(new TextWatcher()
    {
      private int actionPosition;
      private int characterAction = -1;
      
      public void afterTextChanged(Editable paramAnonymousEditable)
      {
        if (NewContactActivity.this.ignoreOnPhoneChange) {
          return;
        }
        int j = NewContactActivity.this.phoneField.getSelectionStart();
        Object localObject = NewContactActivity.this.phoneField.getText().toString();
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
        NewContactActivity.access$2102(NewContactActivity.this, true);
        paramAnonymousEditable = NewContactActivity.this.phoneField.getHintText();
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
        NewContactActivity.this.phoneField.setText((CharSequence)localObject);
        if (j >= 0)
        {
          paramAnonymousEditable = NewContactActivity.this.phoneField;
          if (j > NewContactActivity.this.phoneField.length()) {
            break label404;
          }
        }
        for (;;)
        {
          paramAnonymousEditable.setSelection(j);
          NewContactActivity.this.phoneField.onTextChange();
          NewContactActivity.access$2102(NewContactActivity.this, false);
          return;
          label404:
          j = NewContactActivity.this.phoneField.length();
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
        if (paramAnonymousInt == 6)
        {
          NewContactActivity.this.editDoneItem.performClick();
          return true;
        }
        return false;
      }
    });
    localObject2 = new HashMap();
    try
    {
      paramContext = new BufferedReader(new InputStreamReader(paramContext.getResources().getAssets().open("countries.txt")));
      for (;;)
      {
        localObject1 = paramContext.readLine();
        if (localObject1 == null) {
          break;
        }
        localObject1 = ((String)localObject1).split(";");
        this.countriesArray.add(0, localObject1[2]);
        this.countriesMap.put(localObject1[2], localObject1[0]);
        this.codesMap.put(localObject1[0], localObject1[2]);
        if (localObject1.length > 3) {
          this.phoneFormatMap.put(localObject1[0], localObject1[3]);
        }
        ((HashMap)localObject2).put(localObject1[1], localObject1[2]);
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
    for (;;)
    {
      localObject1 = null;
      try
      {
        TelephonyManager localTelephonyManager = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
        paramContext = (Context)localObject1;
        if (localTelephonyManager != null) {
          paramContext = localTelephonyManager.getSimCountryIso().toUpperCase();
        }
      }
      catch (Exception paramContext)
      {
        for (;;)
        {
          FileLog.e(paramContext);
          paramContext = (Context)localObject1;
        }
      }
      if (paramContext != null)
      {
        paramContext = (String)((HashMap)localObject2).get(paramContext);
        if ((paramContext != null) && (this.countriesArray.indexOf(paramContext) != -1))
        {
          this.codeField.setText((CharSequence)this.countriesMap.get(paramContext));
          this.countryState = 0;
        }
      }
      if (this.codeField.length() == 0)
      {
        this.countryButton.setText(LocaleController.getString("ChooseCountry", 2131493246));
        this.phoneField.setHintText(null);
        this.countryState = 1;
      }
      return this.fragmentView;
      paramContext.close();
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local14 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (NewContactActivity.this.avatarImage != null)
        {
          NewContactActivity.this.avatarDrawable.setInfo(5, NewContactActivity.this.firstNameField.getText().toString(), NewContactActivity.this.lastNameField.getText().toString(), false);
          NewContactActivity.this.avatarImage.invalidate();
        }
      }
    };
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.firstNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.lastNameField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.codeField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.codeField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.phoneField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.phoneField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, "windowBackgroundWhiteHintText"), new ThemeDescription(this.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "windowBackgroundWhiteInputField"), new ThemeDescription(this.phoneField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "windowBackgroundWhiteInputFieldActivated"), new ThemeDescription(this.textView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.lineView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhiteGrayLine"), new ThemeDescription(this.countryButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.editDoneItemProgress, 0, null, null, null, null, "contextProgressInner2"), new ThemeDescription(this.editDoneItemProgress, 0, null, null, null, null, "contextProgressOuter2"), new ThemeDescription(null, 0, null, null, new Drawable[] { Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable }, local14, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local14, "avatar_backgroundPink") };
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
  
  public void onNothingSelected(AdapterView<?> paramAdapterView) {}
  
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


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/NewContactActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */