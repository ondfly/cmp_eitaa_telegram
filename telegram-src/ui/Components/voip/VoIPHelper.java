package org.telegram.ui.Components.voip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.Settings.System;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.TL_phone_setCallRating;
import org.telegram.tgnet.TLRPC.TL_updates;
import org.telegram.tgnet.TLRPC.TL_userFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.BetterRatingView.OnRatingChangeListener;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.VoIPActivity;

public class VoIPHelper
{
  private static final int VOIP_SUPPORT_ID = 4244000;
  public static long lastCallTime = 0L;
  
  public static boolean canRateCall(TLRPC.TL_messageActionPhoneCall paramTL_messageActionPhoneCall)
  {
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (!(paramTL_messageActionPhoneCall.reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy))
    {
      bool1 = bool2;
      if (!(paramTL_messageActionPhoneCall.reason instanceof TLRPC.TL_phoneCallDiscardReasonMissed))
      {
        Iterator localIterator = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET).iterator();
        String[] arrayOfString;
        do
        {
          bool1 = bool2;
          if (!localIterator.hasNext()) {
            break;
          }
          arrayOfString = ((String)localIterator.next()).split(" ");
        } while ((arrayOfString.length < 2) || (!arrayOfString[0].equals(paramTL_messageActionPhoneCall.call_id + "")));
        bool1 = true;
      }
    }
    return bool1;
  }
  
  private static void doInitiateCall(TLRPC.User paramUser, Activity paramActivity)
  {
    if ((paramActivity == null) || (paramUser == null)) {}
    while (System.currentTimeMillis() - lastCallTime < 2000L) {
      return;
    }
    lastCallTime = System.currentTimeMillis();
    Intent localIntent = new Intent(paramActivity, VoIPService.class);
    localIntent.putExtra("user_id", paramUser.id);
    localIntent.putExtra("is_outgoing", true);
    localIntent.putExtra("start_incall_activity", true);
    localIntent.putExtra("account", UserConfig.selectedAccount);
    try
    {
      paramActivity.startService(localIntent);
      return;
    }
    catch (Throwable paramUser)
    {
      FileLog.e(paramUser);
    }
  }
  
  public static File getLogsDir()
  {
    File localFile = new File(ApplicationLoader.applicationContext.getCacheDir(), "voip_logs");
    if (!localFile.exists()) {
      localFile.mkdirs();
    }
    return localFile;
  }
  
  private static void initiateCall(TLRPC.User paramUser, final Activity paramActivity)
  {
    if ((paramActivity == null) || (paramUser == null)) {}
    do
    {
      return;
      if (VoIPService.getSharedInstance() != null)
      {
        TLRPC.User localUser = VoIPService.getSharedInstance().getUser();
        if (localUser.id != paramUser.id)
        {
          new AlertDialog.Builder(paramActivity).setTitle(LocaleController.getString("VoipOngoingAlertTitle", 2131494608)).setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("VoipOngoingAlert", 2131494607, new Object[] { ContactsController.formatName(localUser.first_name, localUser.last_name), ContactsController.formatName(paramUser.first_name, paramUser.last_name) }))).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              if (VoIPService.getSharedInstance() != null)
              {
                VoIPService.getSharedInstance().hangUp(new Runnable()
                {
                  public void run()
                  {
                    VoIPHelper.doInitiateCall(VoIPHelper.2.this.val$user, VoIPHelper.2.this.val$activity);
                  }
                });
                return;
              }
              VoIPHelper.doInitiateCall(this.val$user, paramActivity);
            }
          }).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null).show();
          return;
        }
        paramActivity.startActivity(new Intent(paramActivity, VoIPActivity.class).addFlags(268435456));
        return;
      }
    } while (VoIPService.callIShouldHavePutIntoIntent != null);
    doInitiateCall(paramUser, paramActivity);
  }
  
  @TargetApi(23)
  public static void permissionDenied(Activity paramActivity, Runnable paramRunnable)
  {
    if (!paramActivity.shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO")) {
      new AlertDialog.Builder(paramActivity).setTitle(LocaleController.getString("AppName", 2131492981)).setMessage(LocaleController.getString("VoipNeedMicPermission", 2131494600)).setPositiveButton(LocaleController.getString("OK", 2131494028), null).setNegativeButton(LocaleController.getString("Settings", 2131494379), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          paramAnonymousDialogInterface = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
          paramAnonymousDialogInterface.setData(Uri.fromParts("package", this.val$activity.getPackageName(), null));
          this.val$activity.startActivity(paramAnonymousDialogInterface);
        }
      }).show().setOnDismissListener(new DialogInterface.OnDismissListener()
      {
        public void onDismiss(DialogInterface paramAnonymousDialogInterface)
        {
          if (this.val$onFinish != null) {
            this.val$onFinish.run();
          }
        }
      });
    }
  }
  
  public static void showRateAlert(final Context paramContext, Runnable paramRunnable, long paramLong1, final long paramLong2, final int paramInt)
  {
    final File localFile = new File(getLogsDir(), paramLong1 + ".log");
    LinearLayout localLinearLayout = new LinearLayout(paramContext);
    localLinearLayout.setOrientation(1);
    int i = AndroidUtilities.dp(16.0F);
    localLinearLayout.setPadding(i, i, i, 0);
    Object localObject = new TextView(paramContext);
    ((TextView)localObject).setTextSize(2, 16.0F);
    ((TextView)localObject).setTextColor(Theme.getColor("dialogTextBlack"));
    ((TextView)localObject).setGravity(17);
    ((TextView)localObject).setText(LocaleController.getString("VoipRateCallAlert", 2131494614));
    localLinearLayout.addView((View)localObject);
    localObject = new BetterRatingView(paramContext);
    localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, -2, 1, 0, 16, 0, 0));
    final EditText localEditText = new EditText(paramContext);
    localEditText.setHint(LocaleController.getString("CallReportHint", 2131493117));
    localEditText.setInputType(147457);
    localEditText.setTextColor(Theme.getColor("dialogTextBlack"));
    localEditText.setHintTextColor(Theme.getColor("dialogTextHint"));
    localEditText.setBackgroundDrawable(Theme.createEditTextDrawable(paramContext, true));
    localEditText.setPadding(0, AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F));
    localEditText.setTextSize(18.0F);
    localEditText.setVisibility(8);
    localLinearLayout.addView(localEditText, LayoutHelper.createLinear(-1, -2, 8.0F, 8.0F, 8.0F, 0.0F));
    boolean[] arrayOfBoolean = new boolean[1];
    arrayOfBoolean[0] = true;
    final CheckBoxCell localCheckBoxCell = new CheckBoxCell(paramContext, 1);
    View.OnClickListener local5 = new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        paramAnonymousView = this.val$includeLogs;
        if (this.val$includeLogs[0] == 0) {}
        for (int i = 1;; i = 0)
        {
          paramAnonymousView[0] = i;
          localCheckBoxCell.setChecked(this.val$includeLogs[0], true);
          return;
        }
      }
    };
    localCheckBoxCell.setText(LocaleController.getString("CallReportIncludeLogs", 2131493118), null, true, false);
    localCheckBoxCell.setClipToPadding(false);
    localCheckBoxCell.setOnClickListener(local5);
    localLinearLayout.addView(localCheckBoxCell, LayoutHelper.createLinear(-1, -2, -8.0F, 0.0F, -8.0F, 0.0F));
    final TextView localTextView = new TextView(paramContext);
    localTextView.setTextSize(2, 14.0F);
    localTextView.setTextColor(Theme.getColor("dialogTextGray3"));
    localTextView.setText(LocaleController.getString("CallReportLogsExplain", 2131493119));
    localTextView.setPadding(AndroidUtilities.dp(8.0F), 0, AndroidUtilities.dp(8.0F), 0);
    localTextView.setOnClickListener(local5);
    localLinearLayout.addView(localTextView);
    localCheckBoxCell.setVisibility(8);
    localTextView.setVisibility(8);
    if (!localFile.exists()) {
      arrayOfBoolean[0] = false;
    }
    paramRunnable = new AlertDialog.Builder(paramContext).setTitle(LocaleController.getString("CallMessageReportProblem", 2131493114)).setView(localLinearLayout).setPositiveButton(LocaleController.getString("Send", 2131494331), new DialogInterface.OnClickListener()
    {
      public void onClick(final DialogInterface paramAnonymousDialogInterface, final int paramAnonymousInt)
      {
        paramAnonymousInt = UserConfig.selectedAccount;
        paramAnonymousDialogInterface = new TLRPC.TL_phone_setCallRating();
        paramAnonymousDialogInterface.rating = this.val$bar.getRating();
        if (paramAnonymousDialogInterface.rating < 5) {}
        for (paramAnonymousDialogInterface.comment = localEditText.getText().toString();; paramAnonymousDialogInterface.comment = "")
        {
          paramAnonymousDialogInterface.peer = new TLRPC.TL_inputPhoneCall();
          paramAnonymousDialogInterface.peer.access_hash = paramLong2;
          paramAnonymousDialogInterface.peer.id = paramInt;
          ConnectionsManager.getInstance(localFile).sendRequest(paramAnonymousDialogInterface, new RequestDelegate()
          {
            public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              if ((paramAnonymous2TLObject instanceof TLRPC.TL_updates))
              {
                paramAnonymous2TLObject = (TLRPC.TL_updates)paramAnonymous2TLObject;
                MessagesController.getInstance(paramAnonymousInt).processUpdates(paramAnonymous2TLObject, false);
                if ((VoIPHelper.7.this.val$includeLogs[0] != 0) && (VoIPHelper.7.this.val$log.exists()) && (paramAnonymousDialogInterface.rating < 4))
                {
                  SendMessagesHelper.prepareSendingDocument(VoIPHelper.7.this.val$log.getAbsolutePath(), VoIPHelper.7.this.val$log.getAbsolutePath(), null, "text/plain", 4244000L, null, null);
                  Toast.makeText(VoIPHelper.7.this.val$context, LocaleController.getString("CallReportSent", 2131493120), 1).show();
                }
              }
            }
          });
          return;
        }
      }
    }).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null).setOnDismissListener(new DialogInterface.OnDismissListener()
    {
      public void onDismiss(DialogInterface paramAnonymousDialogInterface)
      {
        if (this.val$onDismiss != null) {
          this.val$onDismiss.run();
        }
      }
    }).show().getButton(-1);
    paramRunnable.setEnabled(false);
    ((BetterRatingView)localObject).setOnRatingChangeListener(new BetterRatingView.OnRatingChangeListener()
    {
      public void onRatingChanged(int paramAnonymousInt)
      {
        int j = 0;
        Object localObject = this.val$btn;
        boolean bool;
        label42:
        int i;
        if (paramAnonymousInt > 0)
        {
          bool = true;
          ((View)localObject).setEnabled(bool);
          EditText localEditText = localEditText;
          if (paramAnonymousInt >= 4) {
            break label163;
          }
          localObject = LocaleController.getString("CallReportHint", 2131493117);
          localEditText.setHint((CharSequence)localObject);
          localObject = localEditText;
          if ((paramAnonymousInt >= 5) || (paramAnonymousInt <= 0)) {
            break label175;
          }
          i = 0;
          label66:
          ((EditText)localObject).setVisibility(i);
          if (localEditText.getVisibility() == 8) {
            ((InputMethodManager)paramContext.getSystemService("input_method")).hideSoftInputFromWindow(localEditText.getWindowToken(), 0);
          }
          if (localFile.exists())
          {
            localObject = localCheckBoxCell;
            if (paramAnonymousInt >= 4) {
              break label181;
            }
            i = 0;
            label131:
            ((CheckBoxCell)localObject).setVisibility(i);
            localObject = localTextView;
            if (paramAnonymousInt >= 4) {
              break label187;
            }
          }
        }
        label163:
        label175:
        label181:
        label187:
        for (paramAnonymousInt = j;; paramAnonymousInt = 8)
        {
          ((TextView)localObject).setVisibility(paramAnonymousInt);
          return;
          bool = false;
          break;
          localObject = LocaleController.getString("VoipFeedbackCommentHint", 2131494595);
          break label42;
          i = 8;
          break label66;
          i = 8;
          break label131;
        }
      }
    });
  }
  
  public static void showRateAlert(Context paramContext, TLRPC.TL_messageActionPhoneCall paramTL_messageActionPhoneCall)
  {
    Iterator localIterator = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).getStringSet("calls_access_hashes", Collections.EMPTY_SET).iterator();
    String[] arrayOfString;
    do
    {
      if (!localIterator.hasNext()) {
        break;
      }
      arrayOfString = ((String)localIterator.next()).split(" ");
    } while ((arrayOfString.length < 2) || (!arrayOfString[0].equals(paramTL_messageActionPhoneCall.call_id + "")));
    try
    {
      long l = Long.parseLong(arrayOfString[1]);
      showRateAlert(paramContext, null, paramTL_messageActionPhoneCall.call_id, l, UserConfig.selectedAccount);
      return;
    }
    catch (Exception paramContext) {}
  }
  
  public static void startCall(TLRPC.User paramUser, Activity paramActivity, final TLRPC.TL_userFull paramTL_userFull)
  {
    int i = 1;
    if ((paramTL_userFull != null) && (paramTL_userFull.phone_calls_private))
    {
      new AlertDialog.Builder(paramActivity).setTitle(LocaleController.getString("VoipFailed", 2131494594)).setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("CallNotAvailable", 2131493116, new Object[] { ContactsController.formatName(paramUser.first_name, paramUser.last_name) }))).setPositiveButton(LocaleController.getString("OK", 2131494028), null).show();
      return;
    }
    if (ConnectionsManager.getInstance(UserConfig.selectedAccount).getConnectionState() != 3)
    {
      if (Settings.System.getInt(paramActivity.getContentResolver(), "airplane_mode_on", 0) != 0)
      {
        paramTL_userFull = new AlertDialog.Builder(paramActivity);
        if (i == 0) {
          break label233;
        }
        paramUser = LocaleController.getString("VoipOfflineAirplaneTitle", 2131494604);
        label134:
        paramTL_userFull = paramTL_userFull.setTitle(paramUser);
        if (i == 0) {
          break label246;
        }
      }
      label233:
      label246:
      for (paramUser = LocaleController.getString("VoipOfflineAirplane", 2131494603);; paramUser = LocaleController.getString("VoipOffline", 2131494602))
      {
        paramUser = paramTL_userFull.setMessage(paramUser).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
        if (i != 0)
        {
          paramTL_userFull = new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
          if (paramTL_userFull.resolveActivity(paramActivity.getPackageManager()) != null) {
            paramUser.setNeutralButton(LocaleController.getString("VoipOfflineOpenSettings", 2131494605), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
              {
                this.val$activity.startActivity(paramTL_userFull);
              }
            });
          }
        }
        paramUser.show();
        return;
        i = 0;
        break;
        paramUser = LocaleController.getString("VoipOfflineTitle", 2131494606);
        break label134;
      }
    }
    if ((Build.VERSION.SDK_INT >= 23) && (paramActivity.checkSelfPermission("android.permission.RECORD_AUDIO") != 0))
    {
      paramActivity.requestPermissions(new String[] { "android.permission.RECORD_AUDIO" }, 101);
      return;
    }
    initiateCall(paramUser, paramActivity);
  }
  
  public static void upgradeP2pSetting(int paramInt)
  {
    SharedPreferences localSharedPreferences = MessagesController.getMainSettings(paramInt);
    if (localSharedPreferences.contains("calls_p2p"))
    {
      SharedPreferences.Editor localEditor = localSharedPreferences.edit();
      if (!localSharedPreferences.getBoolean("calls_p2p", true)) {
        localEditor.putInt("calls_p2p_new", 2);
      }
      localEditor.remove("calls_p2p").commit();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/voip/VoIPHelper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */