package org.telegram.ui.Components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.MessagesStorage.IntCallback;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.SecretChatHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.TL_account_changePhone;
import org.telegram.tgnet.TLRPC.TL_account_confirmPhone;
import org.telegram.tgnet.TLRPC.TL_account_getPassword;
import org.telegram.tgnet.TLRPC.TL_account_getTmpPassword;
import org.telegram.tgnet.TLRPC.TL_account_reportPeer;
import org.telegram.tgnet.TLRPC.TL_account_sendChangePhoneCode;
import org.telegram.tgnet.TLRPC.TL_account_sendConfirmPhoneCode;
import org.telegram.tgnet.TLRPC.TL_auth_resendCode;
import org.telegram.tgnet.TLRPC.TL_channels_createChannel;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC.TL_channels_joinChannel;
import org.telegram.tgnet.TLRPC.TL_contacts_importContacts;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonPornography;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonSpam;
import org.telegram.tgnet.TLRPC.TL_inputReportReasonViolence;
import org.telegram.tgnet.TLRPC.TL_messages_addChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_createChat;
import org.telegram.tgnet.TLRPC.TL_messages_editMessage;
import org.telegram.tgnet.TLRPC.TL_messages_forwardMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getAttachedStickers;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_report;
import org.telegram.tgnet.TLRPC.TL_messages_sendBroadcast;
import org.telegram.tgnet.TLRPC.TL_messages_sendInlineBotResult;
import org.telegram.tgnet.TLRPC.TL_messages_sendMedia;
import org.telegram.tgnet.TLRPC.TL_messages_sendMessage;
import org.telegram.tgnet.TLRPC.TL_messages_startBot;
import org.telegram.tgnet.TLRPC.TL_payments_sendPaymentForm;
import org.telegram.tgnet.TLRPC.TL_payments_validateRequestedInfo;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_updateUserName;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.CacheControlActivity;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ReportOtherActivity;

public class AlertsCreator
{
  public static Dialog createColorSelectDialog(Activity paramActivity, final long paramLong, final boolean paramBoolean1, boolean paramBoolean2, Runnable paramRunnable)
  {
    Object localObject = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
    int i;
    final int[] arrayOfInt;
    int j;
    label136:
    RadioColorCell localRadioColorCell;
    String str10;
    if (paramBoolean1)
    {
      i = ((SharedPreferences)localObject).getInt("GroupLed", -16776961);
      localObject = new LinearLayout(paramActivity);
      ((LinearLayout)localObject).setOrientation(1);
      String str1 = LocaleController.getString("ColorRed", 2131493275);
      String str2 = LocaleController.getString("ColorOrange", 2131493273);
      String str3 = LocaleController.getString("ColorYellow", 2131493279);
      String str4 = LocaleController.getString("ColorGreen", 2131493272);
      String str5 = LocaleController.getString("ColorCyan", 2131493270);
      String str6 = LocaleController.getString("ColorBlue", 2131493269);
      String str7 = LocaleController.getString("ColorViolet", 2131493277);
      String str8 = LocaleController.getString("ColorPink", 2131493274);
      String str9 = LocaleController.getString("ColorWhite", 2131493278);
      arrayOfInt = new int[1];
      arrayOfInt[0] = i;
      j = 0;
      if (j >= 9) {
        break label436;
      }
      localRadioColorCell = new RadioColorCell(paramActivity);
      localRadioColorCell.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
      localRadioColorCell.setTag(Integer.valueOf(j));
      localRadioColorCell.setCheckColor(org.telegram.ui.Cells.TextColorCell.colors[j], org.telegram.ui.Cells.TextColorCell.colors[j]);
      str10 = new String[] { str1, str2, str3, str4, str5, str6, str7, str8, str9 }[j];
      if (i != org.telegram.ui.Cells.TextColorCell.colorsToSave[j]) {
        break label430;
      }
    }
    label430:
    for (boolean bool = true;; bool = false)
    {
      localRadioColorCell.setTextAndValue(str10, bool);
      ((LinearLayout)localObject).addView(localRadioColorCell);
      localRadioColorCell.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          int j = this.val$linearLayout.getChildCount();
          int i = 0;
          if (i < j)
          {
            RadioColorCell localRadioColorCell = (RadioColorCell)this.val$linearLayout.getChildAt(i);
            if (localRadioColorCell == paramAnonymousView) {}
            for (boolean bool = true;; bool = false)
            {
              localRadioColorCell.setChecked(bool, true);
              i += 1;
              break;
            }
          }
          arrayOfInt[0] = org.telegram.ui.Cells.TextColorCell.colorsToSave[((Integer)paramAnonymousView.getTag()).intValue()];
        }
      });
      j += 1;
      break label136;
      if (paramBoolean2)
      {
        i = ((SharedPreferences)localObject).getInt("MessagesLed", -16776961);
        break;
      }
      if (((SharedPreferences)localObject).contains("color_" + paramLong))
      {
        i = ((SharedPreferences)localObject).getInt("color_" + paramLong, -16776961);
        break;
      }
      if ((int)paramLong < 0)
      {
        i = ((SharedPreferences)localObject).getInt("GroupLed", -16776961);
        break;
      }
      i = ((SharedPreferences)localObject).getInt("MessagesLed", -16776961);
      break;
    }
    label436:
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(LocaleController.getString("LedColor", 2131493744));
    paramActivity.setView((View)localObject);
    paramActivity.setPositiveButton(LocaleController.getString("Set", 2131494368), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        paramAnonymousDialogInterface = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (this.val$globalAll) {
          paramAnonymousDialogInterface.putInt("MessagesLed", arrayOfInt[0]);
        }
        for (;;)
        {
          paramAnonymousDialogInterface.commit();
          if (this.val$onSelect != null) {
            this.val$onSelect.run();
          }
          return;
          if (paramBoolean1) {
            paramAnonymousDialogInterface.putInt("GroupLed", arrayOfInt[0]);
          } else {
            paramAnonymousDialogInterface.putInt("color_" + paramLong, arrayOfInt[0]);
          }
        }
      }
    });
    paramActivity.setNeutralButton(LocaleController.getString("LedDisabled", 2131493745), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        paramAnonymousDialogInterface = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
        if (this.val$globalAll) {
          paramAnonymousDialogInterface.putInt("MessagesLed", 0);
        }
        for (;;)
        {
          paramAnonymousDialogInterface.commit();
          if (this.val$onSelect != null) {
            this.val$onSelect.run();
          }
          return;
          if (paramBoolean1) {
            paramAnonymousDialogInterface.putInt("GroupLed", 0);
          } else {
            paramAnonymousDialogInterface.putInt("color_" + paramLong, 0);
          }
        }
      }
    });
    if ((!paramBoolean2) && (!paramBoolean1)) {
      paramActivity.setNegativeButton(LocaleController.getString("Default", 2131493354), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          paramAnonymousDialogInterface = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
          paramAnonymousDialogInterface.remove("color_" + this.val$dialog_id);
          paramAnonymousDialogInterface.commit();
          if (this.val$onSelect != null) {
            this.val$onSelect.run();
          }
        }
      });
    }
    return paramActivity.create();
  }
  
  public static Dialog createFreeSpaceDialog(LaunchActivity paramLaunchActivity)
  {
    int[] arrayOfInt = new int[1];
    int i = MessagesController.getGlobalMainSettings().getInt("keep_media", 2);
    final LinearLayout localLinearLayout;
    Object localObject2;
    label153:
    label170:
    label197:
    String str;
    if (i == 2)
    {
      arrayOfInt[0] = 3;
      localObject1 = new String[4];
      localObject1[0] = LocaleController.formatPluralString("Days", 3);
      localObject1[1] = LocaleController.formatPluralString("Weeks", 1);
      localObject1[2] = LocaleController.formatPluralString("Months", 1);
      localObject1[3] = LocaleController.getString("LowDiskSpaceNeverRemove", 2131493781);
      localLinearLayout = new LinearLayout(paramLaunchActivity);
      localLinearLayout.setOrientation(1);
      localObject2 = new TextView(paramLaunchActivity);
      ((TextView)localObject2).setText(LocaleController.getString("LowDiskSpaceTitle2", 2131493783));
      ((TextView)localObject2).setTextColor(Theme.getColor("dialogTextBlack"));
      ((TextView)localObject2).setTextSize(1, 16.0F);
      ((TextView)localObject2).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      if (!LocaleController.isRTL) {
        break label344;
      }
      i = 5;
      ((TextView)localObject2).setGravity(i | 0x30);
      if (!LocaleController.isRTL) {
        break label349;
      }
      i = 5;
      localLinearLayout.addView((View)localObject2, LayoutHelper.createLinear(-2, -2, i | 0x30, 24, 0, 24, 8));
      i = 0;
      if (i >= localObject1.length) {
        break label359;
      }
      localObject2 = new RadioColorCell(paramLaunchActivity);
      ((RadioColorCell)localObject2).setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
      ((RadioColorCell)localObject2).setTag(Integer.valueOf(i));
      ((RadioColorCell)localObject2).setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
      str = localObject1[i];
      if (arrayOfInt[0] != i) {
        break label354;
      }
    }
    label344:
    label349:
    label354:
    for (boolean bool = true;; bool = false)
    {
      ((RadioColorCell)localObject2).setTextAndValue(str, bool);
      localLinearLayout.addView((View)localObject2);
      ((RadioColorCell)localObject2).setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          int i = ((Integer)paramAnonymousView.getTag()).intValue();
          label32:
          RadioColorCell localRadioColorCell;
          if (i == 0)
          {
            this.val$selected[0] = 3;
            int j = localLinearLayout.getChildCount();
            i = 0;
            if (i >= j) {
              return;
            }
            View localView = localLinearLayout.getChildAt(i);
            if ((localView instanceof RadioColorCell))
            {
              localRadioColorCell = (RadioColorCell)localView;
              if (localView != paramAnonymousView) {
                break label131;
              }
            }
          }
          label131:
          for (boolean bool = true;; bool = false)
          {
            localRadioColorCell.setChecked(bool, true);
            i += 1;
            break label32;
            if (i == 1)
            {
              this.val$selected[0] = 0;
              break;
            }
            if (i == 2)
            {
              this.val$selected[0] = 1;
              break;
            }
            if (i != 3) {
              break;
            }
            this.val$selected[0] = 2;
            break;
          }
        }
      });
      i += 1;
      break label197;
      if (i == 0)
      {
        arrayOfInt[0] = 1;
        break;
      }
      if (i == 1)
      {
        arrayOfInt[0] = 2;
        break;
      }
      if (i != 3) {
        break;
      }
      arrayOfInt[0] = 0;
      break;
      i = 3;
      break label153;
      i = 3;
      break label170;
    }
    label359:
    Object localObject1 = new AlertDialog.Builder(paramLaunchActivity);
    ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("LowDiskSpaceTitle", 2131493782));
    ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("LowDiskSpaceMessage", 2131493780));
    ((AlertDialog.Builder)localObject1).setView(localLinearLayout);
    ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        MessagesController.getGlobalMainSettings().edit().putInt("keep_media", this.val$selected[0]).commit();
      }
    });
    ((AlertDialog.Builder)localObject1).setNeutralButton(LocaleController.getString("ClearMediaCache", 2131493260), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        this.val$parentActivity.presentFragment(new CacheControlActivity());
      }
    });
    return ((AlertDialog.Builder)localObject1).create();
  }
  
  public static Dialog createLocationUpdateDialog(Activity paramActivity, TLRPC.User paramUser, final MessagesStorage.IntCallback paramIntCallback)
  {
    int[] arrayOfInt = new int[1];
    String[] arrayOfString = new String[3];
    arrayOfString[0] = LocaleController.getString("SendLiveLocationFor15m", 2131494341);
    arrayOfString[1] = LocaleController.getString("SendLiveLocationFor1h", 2131494342);
    arrayOfString[2] = LocaleController.getString("SendLiveLocationFor8h", 2131494343);
    final LinearLayout localLinearLayout = new LinearLayout(paramActivity);
    localLinearLayout.setOrientation(1);
    Object localObject = new TextView(paramActivity);
    int i;
    if (paramUser != null)
    {
      ((TextView)localObject).setText(LocaleController.formatString("LiveLocationAlertPrivate", 2131493760, new Object[] { UserObject.getFirstName(paramUser) }));
      ((TextView)localObject).setTextColor(Theme.getColor("dialogTextBlack"));
      ((TextView)localObject).setTextSize(1, 16.0F);
      if (!LocaleController.isRTL) {
        break label303;
      }
      i = 5;
      label133:
      ((TextView)localObject).setGravity(i | 0x30);
      if (!LocaleController.isRTL) {
        break label308;
      }
      i = 5;
      label150:
      localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, -2, i | 0x30, 24, 0, 24, 8));
      i = 0;
      label177:
      if (i >= arrayOfString.length) {
        break label319;
      }
      paramUser = new RadioColorCell(paramActivity);
      paramUser.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
      paramUser.setTag(Integer.valueOf(i));
      paramUser.setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
      localObject = arrayOfString[i];
      if (arrayOfInt[0] != i) {
        break label313;
      }
    }
    label303:
    label308:
    label313:
    for (boolean bool = true;; bool = false)
    {
      paramUser.setTextAndValue((String)localObject, bool);
      localLinearLayout.addView(paramUser);
      paramUser.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          int i = ((Integer)paramAnonymousView.getTag()).intValue();
          this.val$selected[0] = i;
          int j = localLinearLayout.getChildCount();
          i = 0;
          if (i < j)
          {
            View localView = localLinearLayout.getChildAt(i);
            RadioColorCell localRadioColorCell;
            if ((localView instanceof RadioColorCell))
            {
              localRadioColorCell = (RadioColorCell)localView;
              if (localView != paramAnonymousView) {
                break label82;
              }
            }
            label82:
            for (boolean bool = true;; bool = false)
            {
              localRadioColorCell.setChecked(bool, true);
              i += 1;
              break;
            }
          }
        }
      });
      i += 1;
      break label177;
      ((TextView)localObject).setText(LocaleController.getString("LiveLocationAlertGroup", 2131493759));
      break;
      i = 3;
      break label133;
      i = 3;
      break label150;
    }
    label319:
    paramUser = new AlertDialog.Builder(paramActivity);
    paramUser.setTopImage(new ShareLocationDrawable(paramActivity, false), Theme.getColor("dialogTopBackground"));
    paramUser.setView(localLinearLayout);
    paramUser.setPositiveButton(LocaleController.getString("ShareFile", 2131494383), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (this.val$selected[0] == 0) {
          paramAnonymousInt = 900;
        }
        for (;;)
        {
          paramIntCallback.run(paramAnonymousInt);
          return;
          if (this.val$selected[0] == 1) {
            paramAnonymousInt = 3600;
          } else {
            paramAnonymousInt = 28800;
          }
        }
      }
    });
    paramUser.setNeutralButton(LocaleController.getString("Cancel", 2131493127), null);
    return paramUser.create();
  }
  
  public static Dialog createMuteAlert(Context paramContext, long paramLong)
  {
    if (paramContext == null) {
      return null;
    }
    paramContext = new BottomSheet.Builder(paramContext);
    paramContext.setTitle(LocaleController.getString("Notifications", 2131494004));
    String str1 = LocaleController.formatString("MuteFor", 2131493857, new Object[] { LocaleController.formatPluralString("Hours", 1) });
    String str2 = LocaleController.formatString("MuteFor", 2131493857, new Object[] { LocaleController.formatPluralString("Hours", 8) });
    String str3 = LocaleController.formatString("MuteFor", 2131493857, new Object[] { LocaleController.formatPluralString("Days", 2) });
    String str4 = LocaleController.getString("MuteDisable", 2131493856);
    DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        int i = ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime();
        if (paramAnonymousInt == 0)
        {
          i += 3600;
          paramAnonymousDialogInterface = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
          if (paramAnonymousInt != 3) {
            break label200;
          }
          paramAnonymousDialogInterface.putInt("notify2_" + this.val$dialog_id, 2);
        }
        for (long l = 1L;; l = i << 32 | 1L)
        {
          NotificationsController.getInstance(UserConfig.selectedAccount).removeNotificationsForDialog(this.val$dialog_id);
          MessagesStorage.getInstance(UserConfig.selectedAccount).setDialogFlags(this.val$dialog_id, l);
          paramAnonymousDialogInterface.commit();
          paramAnonymousDialogInterface = (TLRPC.TL_dialog)MessagesController.getInstance(UserConfig.selectedAccount).dialogs_dict.get(this.val$dialog_id);
          if (paramAnonymousDialogInterface != null)
          {
            paramAnonymousDialogInterface.notify_settings = new TLRPC.TL_peerNotifySettings();
            paramAnonymousDialogInterface.notify_settings.mute_until = i;
          }
          NotificationsController.getInstance(UserConfig.selectedAccount).updateServerNotificationsSettings(this.val$dialog_id);
          return;
          if (paramAnonymousInt == 1)
          {
            i += 28800;
            break;
          }
          if (paramAnonymousInt == 2)
          {
            i += 172800;
            break;
          }
          if (paramAnonymousInt != 3) {
            break;
          }
          i = Integer.MAX_VALUE;
          break;
          label200:
          paramAnonymousDialogInterface.putInt("notify2_" + this.val$dialog_id, 3);
          paramAnonymousDialogInterface.putInt("notifyuntil_" + this.val$dialog_id, i);
        }
      }
    };
    paramContext.setItems(new CharSequence[] { str1, str2, str3, str4 }, local1);
    return paramContext.create();
  }
  
  public static Dialog createPopupSelectDialog(Activity paramActivity, final BaseFragment paramBaseFragment, final boolean paramBoolean1, boolean paramBoolean2, final Runnable paramRunnable)
  {
    Object localObject = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
    int[] arrayOfInt = new int[1];
    int i;
    label109:
    RadioColorCell localRadioColorCell;
    String str;
    if (paramBoolean2)
    {
      arrayOfInt[0] = ((SharedPreferences)localObject).getInt("popupAll", 0);
      String[] arrayOfString = new String[4];
      arrayOfString[0] = LocaleController.getString("NoPopup", 2131493902);
      arrayOfString[1] = LocaleController.getString("OnlyWhenScreenOn", 2131494039);
      arrayOfString[2] = LocaleController.getString("OnlyWhenScreenOff", 2131494038);
      arrayOfString[3] = LocaleController.getString("AlwaysShowPopup", 2131492961);
      localObject = new LinearLayout(paramActivity);
      ((LinearLayout)localObject).setOrientation(1);
      i = 0;
      if (i >= arrayOfString.length) {
        break label258;
      }
      localRadioColorCell = new RadioColorCell(paramActivity);
      localRadioColorCell.setTag(Integer.valueOf(i));
      localRadioColorCell.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
      localRadioColorCell.setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
      str = arrayOfString[i];
      if (arrayOfInt[0] != i) {
        break label253;
      }
    }
    label253:
    for (paramBoolean2 = true;; paramBoolean2 = false)
    {
      localRadioColorCell.setTextAndValue(str, paramBoolean2);
      ((LinearLayout)localObject).addView(localRadioColorCell);
      localRadioColorCell.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          this.val$selected[0] = ((Integer)paramAnonymousView.getTag()).intValue();
          SharedPreferences.Editor localEditor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
          if (paramBoolean1) {}
          for (paramAnonymousView = "popupGroup";; paramAnonymousView = "popupAll")
          {
            localEditor.putInt(paramAnonymousView, this.val$selected[0]);
            localEditor.commit();
            if (paramBaseFragment != null) {
              paramBaseFragment.dismissCurrentDialig();
            }
            if (paramRunnable != null) {
              paramRunnable.run();
            }
            return;
          }
        }
      });
      i += 1;
      break label109;
      if (!paramBoolean1) {
        break;
      }
      arrayOfInt[0] = ((SharedPreferences)localObject).getInt("popupGroup", 0);
      break;
    }
    label258:
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(LocaleController.getString("PopupNotification", 2131494187));
    paramActivity.setView((View)localObject);
    paramActivity.setPositiveButton(LocaleController.getString("Cancel", 2131493127), null);
    return paramActivity.create();
  }
  
  public static Dialog createPrioritySelectDialog(Activity paramActivity, final BaseFragment paramBaseFragment, final long paramLong, boolean paramBoolean1, boolean paramBoolean2, final Runnable paramRunnable)
  {
    Object localObject = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
    int[] arrayOfInt = new int[1];
    LinearLayout localLinearLayout;
    int i;
    label154:
    RadioColorCell localRadioColorCell;
    String str;
    if (paramLong != 0L)
    {
      arrayOfInt[0] = ((SharedPreferences)localObject).getInt("priority_" + paramLong, 3);
      if (arrayOfInt[0] == 3)
      {
        arrayOfInt[0] = 0;
        localObject = new String[5];
        localObject[0] = LocaleController.getString("NotificationsPrioritySettings", 2131494019);
        localObject[1] = LocaleController.getString("NotificationsPriorityLow", 2131494017);
        localObject[2] = LocaleController.getString("NotificationsPriorityMedium", 2131494018);
        localObject[3] = LocaleController.getString("NotificationsPriorityHigh", 2131494016);
        localObject[4] = LocaleController.getString("NotificationsPriorityUrgent", 2131494020);
        localLinearLayout = new LinearLayout(paramActivity);
        localLinearLayout.setOrientation(1);
        i = 0;
        if (i >= localObject.length) {
          break label497;
        }
        localRadioColorCell = new RadioColorCell(paramActivity);
        localRadioColorCell.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
        localRadioColorCell.setTag(Integer.valueOf(i));
        localRadioColorCell.setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
        str = localObject[i];
        if (arrayOfInt[0] != i) {
          break label491;
        }
      }
    }
    label355:
    label452:
    label491:
    for (paramBoolean2 = true;; paramBoolean2 = false)
    {
      localRadioColorCell.setTextAndValue(str, paramBoolean2);
      localLinearLayout.addView(localRadioColorCell);
      localRadioColorCell.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          this.val$selected[0] = ((Integer)paramAnonymousView.getTag()).intValue();
          SharedPreferences.Editor localEditor = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
          int i;
          if (paramLong != 0L)
          {
            if (this.val$selected[0] == 0) {
              i = 3;
            }
            for (;;)
            {
              localEditor.putInt("priority_" + paramLong, i);
              localEditor.commit();
              if (paramRunnable != null) {
                paramRunnable.dismissCurrentDialig();
              }
              if (this.val$onSelect != null) {
                this.val$onSelect.run();
              }
              return;
              if (this.val$selected[0] == 1) {
                i = 4;
              } else if (this.val$selected[0] == 2) {
                i = 5;
              } else if (this.val$selected[0] == 3) {
                i = 0;
              } else {
                i = 1;
              }
            }
          }
          if (this.val$selected[0] == 0)
          {
            i = 4;
            label177:
            if (!paramBaseFragment) {
              break label234;
            }
          }
          label234:
          for (paramAnonymousView = "priority_group";; paramAnonymousView = "priority_messages")
          {
            localEditor.putInt(paramAnonymousView, i);
            break;
            if (this.val$selected[0] == 1)
            {
              i = 5;
              break label177;
            }
            if (this.val$selected[0] == 2)
            {
              i = 0;
              break label177;
            }
            i = 1;
            break label177;
          }
        }
      });
      i += 1;
      break label154;
      if (arrayOfInt[0] == 4)
      {
        arrayOfInt[0] = 1;
        break;
      }
      if (arrayOfInt[0] == 5)
      {
        arrayOfInt[0] = 2;
        break;
      }
      if (arrayOfInt[0] == 0)
      {
        arrayOfInt[0] = 3;
        break;
      }
      arrayOfInt[0] = 4;
      break;
      if (paramBoolean2)
      {
        arrayOfInt[0] = ((SharedPreferences)localObject).getInt("priority_messages", 1);
        if (arrayOfInt[0] != 4) {
          break label452;
        }
        arrayOfInt[0] = 0;
      }
      for (;;)
      {
        localObject = new String[4];
        localObject[0] = LocaleController.getString("NotificationsPriorityLow", 2131494017);
        localObject[1] = LocaleController.getString("NotificationsPriorityMedium", 2131494018);
        localObject[2] = LocaleController.getString("NotificationsPriorityHigh", 2131494016);
        localObject[3] = LocaleController.getString("NotificationsPriorityUrgent", 2131494020);
        break;
        if (!paramBoolean1) {
          break label355;
        }
        arrayOfInt[0] = ((SharedPreferences)localObject).getInt("priority_group", 1);
        break label355;
        if (arrayOfInt[0] == 5) {
          arrayOfInt[0] = 1;
        } else if (arrayOfInt[0] == 0) {
          arrayOfInt[0] = 2;
        } else {
          arrayOfInt[0] = 3;
        }
      }
    }
    label497:
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(LocaleController.getString("NotificationsImportance", 2131494009));
    paramActivity.setView(localLinearLayout);
    paramActivity.setPositiveButton(LocaleController.getString("Cancel", 2131493127), null);
    return paramActivity.create();
  }
  
  public static Dialog createReportAlert(final Context paramContext, long paramLong, int paramInt, final BaseFragment paramBaseFragment)
  {
    if ((paramContext == null) || (paramBaseFragment == null)) {
      return null;
    }
    BottomSheet.Builder localBuilder = new BottomSheet.Builder(paramContext);
    localBuilder.setTitle(LocaleController.getString("ReportChat", 2131494238));
    String str1 = LocaleController.getString("ReportChatSpam", 2131494243);
    String str2 = LocaleController.getString("ReportChatViolence", 2131494244);
    String str3 = LocaleController.getString("ReportChatPornography", 2131494241);
    String str4 = LocaleController.getString("ReportChatOther", 2131494240);
    paramContext = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (paramAnonymousInt == 3)
        {
          paramAnonymousDialogInterface = new Bundle();
          paramAnonymousDialogInterface.putLong("dialog_id", this.val$dialog_id);
          paramAnonymousDialogInterface.putLong("message_id", paramBaseFragment);
          paramContext.presentFragment(new ReportOtherActivity(paramAnonymousDialogInterface));
          return;
        }
        TLRPC.InputPeer localInputPeer = MessagesController.getInstance(UserConfig.selectedAccount).getInputPeer((int)this.val$dialog_id);
        if (paramBaseFragment != 0)
        {
          paramAnonymousDialogInterface = new TLRPC.TL_messages_report();
          paramAnonymousDialogInterface.peer = localInputPeer;
          paramAnonymousDialogInterface.id.add(Integer.valueOf(paramBaseFragment));
          if (paramAnonymousInt == 0) {
            paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonSpam();
          }
          for (;;)
          {
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(paramAnonymousDialogInterface, new RequestDelegate()
            {
              public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error) {}
            });
            Toast.makeText(this.val$context, LocaleController.getString("ReportChatSent", 2131494242), 0).show();
            return;
            if (paramAnonymousInt == 1) {
              paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonViolence();
            } else if (paramAnonymousInt == 2) {
              paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonPornography();
            }
          }
        }
        paramAnonymousDialogInterface = new TLRPC.TL_account_reportPeer();
        paramAnonymousDialogInterface.peer = localInputPeer;
        if (paramAnonymousInt == 0) {
          paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonSpam();
        }
        for (;;)
        {
          break;
          if (paramAnonymousInt == 1) {
            paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonViolence();
          } else if (paramAnonymousInt == 2) {
            paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonPornography();
          }
        }
      }
    };
    localBuilder.setItems(new CharSequence[] { str1, str2, str3, str4 }, paramContext);
    return localBuilder.create();
  }
  
  public static Dialog createSingleChoiceDialog(Activity paramActivity, BaseFragment paramBaseFragment, String[] paramArrayOfString, String paramString, int paramInt, final DialogInterface.OnClickListener paramOnClickListener)
  {
    LinearLayout localLinearLayout = new LinearLayout(paramActivity);
    localLinearLayout.setOrientation(1);
    int i = 0;
    if (i < paramArrayOfString.length)
    {
      RadioColorCell localRadioColorCell = new RadioColorCell(paramActivity);
      localRadioColorCell.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
      localRadioColorCell.setTag(Integer.valueOf(i));
      localRadioColorCell.setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
      String str = paramArrayOfString[i];
      if (paramInt == i) {}
      for (boolean bool = true;; bool = false)
      {
        localRadioColorCell.setTextAndValue(str, bool);
        localLinearLayout.addView(localRadioColorCell);
        localRadioColorCell.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            int i = ((Integer)paramAnonymousView.getTag()).intValue();
            if (this.val$parentFragment != null) {
              this.val$parentFragment.dismissCurrentDialig();
            }
            paramOnClickListener.onClick(null, i);
          }
        });
        i += 1;
        break;
      }
    }
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(paramString);
    paramActivity.setView(localLinearLayout);
    paramActivity.setPositiveButton(LocaleController.getString("Cancel", 2131493127), null);
    return paramActivity.create();
  }
  
  public static AlertDialog.Builder createTTLAlert(final Context paramContext, TLRPC.EncryptedChat paramEncryptedChat)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramContext);
    localBuilder.setTitle(LocaleController.getString("MessageLifetime", 2131493818));
    paramContext = new NumberPicker(paramContext);
    paramContext.setMinValue(0);
    paramContext.setMaxValue(20);
    if ((paramEncryptedChat.ttl > 0) && (paramEncryptedChat.ttl < 16)) {
      paramContext.setValue(paramEncryptedChat.ttl);
    }
    for (;;)
    {
      paramContext.setFormatter(new NumberPicker.Formatter()
      {
        public String format(int paramAnonymousInt)
        {
          if (paramAnonymousInt == 0) {
            return LocaleController.getString("ShortMessageLifetimeForever", 2131494402);
          }
          if ((paramAnonymousInt >= 1) && (paramAnonymousInt < 16)) {
            return LocaleController.formatTTLString(paramAnonymousInt);
          }
          if (paramAnonymousInt == 16) {
            return LocaleController.formatTTLString(30);
          }
          if (paramAnonymousInt == 17) {
            return LocaleController.formatTTLString(60);
          }
          if (paramAnonymousInt == 18) {
            return LocaleController.formatTTLString(3600);
          }
          if (paramAnonymousInt == 19) {
            return LocaleController.formatTTLString(86400);
          }
          if (paramAnonymousInt == 20) {
            return LocaleController.formatTTLString(604800);
          }
          return "";
        }
      });
      localBuilder.setView(paramContext);
      localBuilder.setNegativeButton(LocaleController.getString("Done", 2131493395), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          paramAnonymousInt = this.val$encryptedChat.ttl;
          int i = paramContext.getValue();
          if ((i >= 0) && (i < 16)) {
            this.val$encryptedChat.ttl = i;
          }
          for (;;)
          {
            if (paramAnonymousInt != this.val$encryptedChat.ttl)
            {
              SecretChatHelper.getInstance(UserConfig.selectedAccount).sendTTLMessage(this.val$encryptedChat, null);
              MessagesStorage.getInstance(UserConfig.selectedAccount).updateEncryptedChatTTL(this.val$encryptedChat);
            }
            return;
            if (i == 16) {
              this.val$encryptedChat.ttl = 30;
            } else if (i == 17) {
              this.val$encryptedChat.ttl = 60;
            } else if (i == 18) {
              this.val$encryptedChat.ttl = 3600;
            } else if (i == 19) {
              this.val$encryptedChat.ttl = 86400;
            } else if (i == 20) {
              this.val$encryptedChat.ttl = 604800;
            }
          }
        }
      });
      return localBuilder;
      if (paramEncryptedChat.ttl == 30) {
        paramContext.setValue(16);
      } else if (paramEncryptedChat.ttl == 60) {
        paramContext.setValue(17);
      } else if (paramEncryptedChat.ttl == 3600) {
        paramContext.setValue(18);
      } else if (paramEncryptedChat.ttl == 86400) {
        paramContext.setValue(19);
      } else if (paramEncryptedChat.ttl == 604800) {
        paramContext.setValue(20);
      } else if (paramEncryptedChat.ttl == 0) {
        paramContext.setValue(0);
      }
    }
  }
  
  public static Dialog createVibrationSelectDialog(Activity paramActivity, final BaseFragment paramBaseFragment, final long paramLong, String paramString, final Runnable paramRunnable)
  {
    Object localObject = MessagesController.getNotificationsSettings(UserConfig.selectedAccount);
    int[] arrayOfInt = new int[1];
    LinearLayout localLinearLayout;
    int i;
    label140:
    RadioColorCell localRadioColorCell;
    String str;
    if (paramLong != 0L)
    {
      arrayOfInt[0] = ((SharedPreferences)localObject).getInt(paramString + paramLong, 0);
      if (arrayOfInt[0] == 3)
      {
        arrayOfInt[0] = 2;
        localObject = new String[4];
        localObject[0] = LocaleController.getString("VibrationDefault", 2131494575);
        localObject[1] = LocaleController.getString("Short", 2131494401);
        localObject[2] = LocaleController.getString("Long", 2131493779);
        localObject[3] = LocaleController.getString("VibrationDisabled", 2131494576);
        localLinearLayout = new LinearLayout(paramActivity);
        localLinearLayout.setOrientation(1);
        i = 0;
        if (i >= localObject.length) {
          break label420;
        }
        localRadioColorCell = new RadioColorCell(paramActivity);
        localRadioColorCell.setPadding(AndroidUtilities.dp(4.0F), 0, AndroidUtilities.dp(4.0F), 0);
        localRadioColorCell.setTag(Integer.valueOf(i));
        localRadioColorCell.setCheckColor(Theme.getColor("radioBackground"), Theme.getColor("dialogRadioBackgroundChecked"));
        str = localObject[i];
        if (arrayOfInt[0] != i) {
          break label414;
        }
      }
    }
    label414:
    for (boolean bool = true;; bool = false)
    {
      localRadioColorCell.setTextAndValue(str, bool);
      localLinearLayout.addView(localRadioColorCell);
      localRadioColorCell.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          this.val$selected[0] = ((Integer)paramAnonymousView.getTag()).intValue();
          paramAnonymousView = MessagesController.getNotificationsSettings(UserConfig.selectedAccount).edit();
          if (paramLong != 0L) {
            if (this.val$selected[0] == 0) {
              paramAnonymousView.putInt(paramBaseFragment + paramLong, 0);
            }
          }
          for (;;)
          {
            paramAnonymousView.commit();
            if (paramRunnable != null) {
              paramRunnable.dismissCurrentDialig();
            }
            if (this.val$onSelect != null) {
              this.val$onSelect.run();
            }
            return;
            if (this.val$selected[0] == 1)
            {
              paramAnonymousView.putInt(paramBaseFragment + paramLong, 1);
            }
            else if (this.val$selected[0] == 2)
            {
              paramAnonymousView.putInt(paramBaseFragment + paramLong, 3);
            }
            else if (this.val$selected[0] == 3)
            {
              paramAnonymousView.putInt(paramBaseFragment + paramLong, 2);
              continue;
              if (this.val$selected[0] == 0) {
                paramAnonymousView.putInt(paramBaseFragment, 2);
              } else if (this.val$selected[0] == 1) {
                paramAnonymousView.putInt(paramBaseFragment, 0);
              } else if (this.val$selected[0] == 2) {
                paramAnonymousView.putInt(paramBaseFragment, 1);
              } else if (this.val$selected[0] == 3) {
                paramAnonymousView.putInt(paramBaseFragment, 3);
              } else if (this.val$selected[0] == 4) {
                paramAnonymousView.putInt(paramBaseFragment, 4);
              }
            }
          }
        }
      });
      i += 1;
      break label140;
      if (arrayOfInt[0] != 2) {
        break;
      }
      arrayOfInt[0] = 3;
      break;
      arrayOfInt[0] = ((SharedPreferences)localObject).getInt(paramString, 0);
      if (arrayOfInt[0] == 0) {
        arrayOfInt[0] = 1;
      }
      for (;;)
      {
        localObject = new String[5];
        localObject[0] = LocaleController.getString("VibrationDisabled", 2131494576);
        localObject[1] = LocaleController.getString("VibrationDefault", 2131494575);
        localObject[2] = LocaleController.getString("Short", 2131494401);
        localObject[3] = LocaleController.getString("Long", 2131493779);
        localObject[4] = LocaleController.getString("OnlyIfSilent", 2131494037);
        break;
        if (arrayOfInt[0] == 1) {
          arrayOfInt[0] = 2;
        } else if (arrayOfInt[0] == 2) {
          arrayOfInt[0] = 0;
        }
      }
    }
    label420:
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(LocaleController.getString("Vibrate", 2131494574));
    paramActivity.setView(localLinearLayout);
    paramActivity.setPositiveButton(LocaleController.getString("Cancel", 2131493127), null);
    return paramActivity.create();
  }
  
  public static Dialog createVibrationSelectDialog(Activity paramActivity, BaseFragment paramBaseFragment, long paramLong, boolean paramBoolean1, boolean paramBoolean2, Runnable paramRunnable)
  {
    if (paramLong != 0L)
    {
      str = "vibrate_";
      return createVibrationSelectDialog(paramActivity, paramBaseFragment, paramLong, str, paramRunnable);
    }
    if (paramBoolean1) {}
    for (String str = "vibrate_group";; str = "vibrate_messages") {
      break;
    }
  }
  
  private static String getFloodWaitString(String paramString)
  {
    int i = Utilities.parseInt(paramString).intValue();
    if (i < 60) {}
    for (paramString = LocaleController.formatPluralString("Seconds", i);; paramString = LocaleController.formatPluralString("Minutes", i / 60)) {
      return LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { paramString });
    }
  }
  
  public static Dialog processError(int paramInt, TLRPC.TL_error paramTL_error, BaseFragment paramBaseFragment, TLObject paramTLObject, Object... paramVarArgs)
  {
    int j = 0;
    int i = 0;
    if ((paramTL_error.code == 406) || (paramTL_error.text == null)) {
      return null;
    }
    if (((paramTLObject instanceof TLRPC.TL_channels_joinChannel)) || ((paramTLObject instanceof TLRPC.TL_channels_editAdmin)) || ((paramTLObject instanceof TLRPC.TL_channels_inviteToChannel)) || ((paramTLObject instanceof TLRPC.TL_messages_addChatUser)) || ((paramTLObject instanceof TLRPC.TL_messages_startBot)) || ((paramTLObject instanceof TLRPC.TL_channels_editBanned))) {
      if (paramBaseFragment != null) {
        showAddUserAlert(paramTL_error.text, paramBaseFragment, ((Boolean)paramVarArgs[0]).booleanValue());
      }
    }
    for (;;)
    {
      label89:
      return null;
      if (paramTL_error.text.equals("PEER_FLOOD"))
      {
        NotificationCenter.getInstance(paramInt).postNotificationName(NotificationCenter.needShowAlert, new Object[] { Integer.valueOf(1) });
        continue;
        if ((paramTLObject instanceof TLRPC.TL_messages_createChat))
        {
          if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showFloodWaitAlert(paramTL_error.text, paramBaseFragment);
          } else {
            showAddUserAlert(paramTL_error.text, paramBaseFragment, false);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_channels_createChannel))
        {
          if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showFloodWaitAlert(paramTL_error.text, paramBaseFragment);
          } else {
            showAddUserAlert(paramTL_error.text, paramBaseFragment, false);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_messages_editMessage))
        {
          if (!paramTL_error.text.equals("MESSAGE_NOT_MODIFIED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("EditMessageError", 2131493415));
          }
        }
        else if (((paramTLObject instanceof TLRPC.TL_messages_sendMessage)) || ((paramTLObject instanceof TLRPC.TL_messages_sendMedia)) || ((paramTLObject instanceof TLRPC.TL_messages_sendBroadcast)) || ((paramTLObject instanceof TLRPC.TL_messages_sendInlineBotResult)) || ((paramTLObject instanceof TLRPC.TL_messages_forwardMessages)))
        {
          if (paramTL_error.text.equals("PEER_FLOOD")) {
            NotificationCenter.getInstance(paramInt).postNotificationName(NotificationCenter.needShowAlert, new Object[] { Integer.valueOf(0) });
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_messages_importChatInvite))
        {
          if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else if (paramTL_error.text.equals("USERS_TOO_MUCH")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("JoinToGroupErrorFull", 2131493711));
          } else {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("JoinToGroupErrorNotExist", 2131493712));
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_messages_getAttachedStickers))
        {
          if ((paramBaseFragment != null) && (paramBaseFragment.getParentActivity() != null)) {
            Toast.makeText(paramBaseFragment.getParentActivity(), LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramTL_error.text, 0).show();
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_account_confirmPhone))
        {
          if ((paramTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramTL_error.text.contains("PHONE_CODE_INVALID"))) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidCode", 2131493679));
          } else if (paramTL_error.text.contains("PHONE_CODE_EXPIRED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("CodeExpired", 2131493268));
          } else if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else {
            showSimpleAlert(paramBaseFragment, paramTL_error.text);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_auth_resendCode))
        {
          if (paramTL_error.text.contains("PHONE_NUMBER_INVALID")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidPhoneNumber", 2131493682));
          } else if ((paramTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramTL_error.text.contains("PHONE_CODE_INVALID"))) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidCode", 2131493679));
          } else if (paramTL_error.text.contains("PHONE_CODE_EXPIRED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("CodeExpired", 2131493268));
          } else if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else if (paramTL_error.code != 64536) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramTL_error.text);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_account_sendConfirmPhoneCode))
        {
          if (paramTL_error.code == 400) {
            return showSimpleAlert(paramBaseFragment, LocaleController.getString("CancelLinkExpired", 2131493130));
          }
          if (paramTL_error.text != null)
          {
            if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
              return showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
            }
            return showSimpleAlert(paramBaseFragment, LocaleController.getString("ErrorOccurred", 2131493453));
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_account_changePhone))
        {
          if (paramTL_error.text.contains("PHONE_NUMBER_INVALID")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidPhoneNumber", 2131493682));
          } else if ((paramTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramTL_error.text.contains("PHONE_CODE_INVALID"))) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidCode", 2131493679));
          } else if (paramTL_error.text.contains("PHONE_CODE_EXPIRED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("CodeExpired", 2131493268));
          } else if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else {
            showSimpleAlert(paramBaseFragment, paramTL_error.text);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_account_sendChangePhoneCode))
        {
          if (paramTL_error.text.contains("PHONE_NUMBER_INVALID")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidPhoneNumber", 2131493682));
          } else if ((paramTL_error.text.contains("PHONE_CODE_EMPTY")) || (paramTL_error.text.contains("PHONE_CODE_INVALID"))) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("InvalidCode", 2131493679));
          } else if (paramTL_error.text.contains("PHONE_CODE_EXPIRED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("CodeExpired", 2131493268));
          } else if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else if (paramTL_error.text.startsWith("PHONE_NUMBER_OCCUPIED")) {
            showSimpleAlert(paramBaseFragment, LocaleController.formatString("ChangePhoneNumberOccupied", 2131493141, new Object[] { (String)paramVarArgs[0] }));
          } else {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("ErrorOccurred", 2131493453));
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_updateUserName))
        {
          paramTL_error = paramTL_error.text;
          switch (paramTL_error.hashCode())
          {
          default: 
            label1264:
            paramInt = -1;
          }
          for (;;)
          {
            switch (paramInt)
            {
            default: 
              showSimpleAlert(paramBaseFragment, LocaleController.getString("ErrorOccurred", 2131493453));
              break label89;
              if (!paramTL_error.equals("USERNAME_INVALID")) {
                break label1264;
              }
              paramInt = i;
              continue;
              if (!paramTL_error.equals("USERNAME_OCCUPIED")) {
                break label1264;
              }
              paramInt = 1;
            }
          }
          showSimpleAlert(paramBaseFragment, LocaleController.getString("UsernameInvalid", 2131494563));
          continue;
          showSimpleAlert(paramBaseFragment, LocaleController.getString("UsernameInUse", 2131494562));
        }
        else if ((paramTLObject instanceof TLRPC.TL_contacts_importContacts))
        {
          if ((paramTL_error == null) || (paramTL_error.text.startsWith("FLOOD_WAIT"))) {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("FloodWait", 2131493543));
          } else {
            showSimpleAlert(paramBaseFragment, LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramTL_error.text);
          }
        }
        else if (((paramTLObject instanceof TLRPC.TL_account_getPassword)) || ((paramTLObject instanceof TLRPC.TL_account_getTmpPassword)))
        {
          if (paramTL_error.text.startsWith("FLOOD_WAIT")) {
            showSimpleToast(paramBaseFragment, getFloodWaitString(paramTL_error.text));
          } else {
            showSimpleToast(paramBaseFragment, paramTL_error.text);
          }
        }
        else if ((paramTLObject instanceof TLRPC.TL_payments_sendPaymentForm))
        {
          paramTLObject = paramTL_error.text;
          switch (paramTLObject.hashCode())
          {
          default: 
            label1552:
            paramInt = -1;
          }
          for (;;)
          {
            switch (paramInt)
            {
            default: 
              showSimpleToast(paramBaseFragment, paramTL_error.text);
              break label89;
              if (!paramTLObject.equals("BOT_PRECHECKOUT_FAILED")) {
                break label1552;
              }
              paramInt = j;
              continue;
              if (!paramTLObject.equals("PAYMENT_FAILED")) {
                break label1552;
              }
              paramInt = 1;
            }
          }
          showSimpleToast(paramBaseFragment, LocaleController.getString("PaymentPrecheckoutFailed", 2131494114));
          continue;
          showSimpleToast(paramBaseFragment, LocaleController.getString("PaymentFailed", 2131494101));
        }
        else if ((paramTLObject instanceof TLRPC.TL_payments_validateRequestedInfo))
        {
          paramTLObject = paramTL_error.text;
          paramInt = -1;
          switch (paramTLObject.hashCode())
          {
          }
          for (;;)
          {
            switch (paramInt)
            {
            default: 
              showSimpleToast(paramBaseFragment, paramTL_error.text);
              break label89;
              if (paramTLObject.equals("SHIPPING_NOT_AVAILABLE")) {
                paramInt = 0;
              }
              break;
            }
          }
          showSimpleToast(paramBaseFragment, LocaleController.getString("PaymentNoShippingMethod", 2131494103));
        }
      }
    }
  }
  
  public static void showAddUserAlert(String paramString, BaseFragment paramBaseFragment, boolean paramBoolean)
  {
    if ((paramString == null) || (paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    int i = -1;
    switch (paramString.hashCode())
    {
    default: 
      switch (i)
      {
      default: 
        localBuilder.setMessage(LocaleController.getString("ErrorOccurred", 2131493453) + "\n" + paramString);
      }
      break;
    }
    for (;;)
    {
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      paramBaseFragment.showDialog(localBuilder.create(), true, null);
      return;
      if (!paramString.equals("PEER_FLOOD")) {
        break;
      }
      i = 0;
      break;
      if (!paramString.equals("USER_BLOCKED")) {
        break;
      }
      i = 1;
      break;
      if (!paramString.equals("USER_BOT")) {
        break;
      }
      i = 2;
      break;
      if (!paramString.equals("USER_ID_INVALID")) {
        break;
      }
      i = 3;
      break;
      if (!paramString.equals("USERS_TOO_MUCH")) {
        break;
      }
      i = 4;
      break;
      if (!paramString.equals("USER_NOT_MUTUAL_CONTACT")) {
        break;
      }
      i = 5;
      break;
      if (!paramString.equals("ADMINS_TOO_MUCH")) {
        break;
      }
      i = 6;
      break;
      if (!paramString.equals("BOTS_TOO_MUCH")) {
        break;
      }
      i = 7;
      break;
      if (!paramString.equals("USER_PRIVACY_RESTRICTED")) {
        break;
      }
      i = 8;
      break;
      if (!paramString.equals("USERS_TOO_FEW")) {
        break;
      }
      i = 9;
      break;
      if (!paramString.equals("USER_RESTRICTED")) {
        break;
      }
      i = 10;
      break;
      if (!paramString.equals("YOU_BLOCKED_USER")) {
        break;
      }
      i = 11;
      break;
      if (!paramString.equals("CHAT_ADMIN_BAN_REQUIRED")) {
        break;
      }
      i = 12;
      break;
      if (!paramString.equals("USER_KICKED")) {
        break;
      }
      i = 13;
      break;
      if (!paramString.equals("CHAT_ADMIN_INVITE_REQUIRED")) {
        break;
      }
      i = 14;
      break;
      if (!paramString.equals("USER_ADMIN_INVALID")) {
        break;
      }
      i = 15;
      break;
      localBuilder.setMessage(LocaleController.getString("NobodyLikesSpam2", 2131493918));
      localBuilder.setNegativeButton(LocaleController.getString("MoreInfo", 2131493855), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          MessagesController.getInstance(this.val$fragment.getCurrentAccount()).openByUserName("spambot", this.val$fragment, 1);
        }
      });
      continue;
      if (paramBoolean)
      {
        localBuilder.setMessage(LocaleController.getString("ChannelUserCantAdd", 2131493213));
      }
      else
      {
        localBuilder.setMessage(LocaleController.getString("GroupUserCantAdd", 2131493639));
        continue;
        if (paramBoolean)
        {
          localBuilder.setMessage(LocaleController.getString("ChannelUserAddLimit", 2131493212));
        }
        else
        {
          localBuilder.setMessage(LocaleController.getString("GroupUserAddLimit", 2131493638));
          continue;
          if (paramBoolean)
          {
            localBuilder.setMessage(LocaleController.getString("ChannelUserLeftError", 2131493216));
          }
          else
          {
            localBuilder.setMessage(LocaleController.getString("GroupUserLeftError", 2131493642));
            continue;
            if (paramBoolean)
            {
              localBuilder.setMessage(LocaleController.getString("ChannelUserCantAdmin", 2131493214));
            }
            else
            {
              localBuilder.setMessage(LocaleController.getString("GroupUserCantAdmin", 2131493640));
              continue;
              if (paramBoolean)
              {
                localBuilder.setMessage(LocaleController.getString("ChannelUserCantBot", 2131493215));
              }
              else
              {
                localBuilder.setMessage(LocaleController.getString("GroupUserCantBot", 2131493641));
                continue;
                if (paramBoolean)
                {
                  localBuilder.setMessage(LocaleController.getString("InviteToChannelError", 2131493695));
                }
                else
                {
                  localBuilder.setMessage(LocaleController.getString("InviteToGroupError", 2131493697));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("CreateGroupError", 2131493308));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("UserRestricted", 2131494544));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("YouBlockedUser", 2131494655));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("AddAdminErrorBlacklisted", 2131492922));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("AddAdminErrorNotAMember", 2131492923));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("AddBannedErrorAdmin", 2131492924));
                }
              }
            }
          }
        }
      }
    }
  }
  
  public static void showFloodWaitAlert(String paramString, BaseFragment paramBaseFragment)
  {
    if ((paramString == null) || (!paramString.startsWith("FLOOD_WAIT")) || (paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return;
    }
    int i = Utilities.parseInt(paramString).intValue();
    if (i < 60) {}
    for (paramString = LocaleController.formatPluralString("Seconds", i);; paramString = LocaleController.formatPluralString("Minutes", i / 60))
    {
      AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
      localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
      localBuilder.setMessage(LocaleController.formatString("FloodWaitTime", 2131493544, new Object[] { paramString }));
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      paramBaseFragment.showDialog(localBuilder.create(), true, null);
      return;
    }
  }
  
  public static void showSendMediaAlert(int paramInt, BaseFragment paramBaseFragment)
  {
    if (paramInt == 0) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    if (paramInt == 1) {
      localBuilder.setMessage(LocaleController.getString("ErrorSendRestrictedStickers", 2131493455));
    }
    for (;;)
    {
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      paramBaseFragment.showDialog(localBuilder.create(), true, null);
      return;
      if (paramInt == 2) {
        localBuilder.setMessage(LocaleController.getString("ErrorSendRestrictedMedia", 2131493454));
      }
    }
  }
  
  public static Dialog showSimpleAlert(BaseFragment paramBaseFragment, String paramString)
  {
    if ((paramString == null) || (paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return null;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    localBuilder.setMessage(paramString);
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    paramString = localBuilder.create();
    paramBaseFragment.showDialog(paramString);
    return paramString;
  }
  
  public static Toast showSimpleToast(BaseFragment paramBaseFragment, String paramString)
  {
    if ((paramString == null) || (paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return null;
    }
    paramBaseFragment = Toast.makeText(paramBaseFragment.getParentActivity(), paramString, 1);
    paramBaseFragment.show();
    return paramBaseFragment;
  }
  
  public static abstract interface PaymentAlertDelegate
  {
    public abstract void didPressedNewCard();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/AlertsCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */