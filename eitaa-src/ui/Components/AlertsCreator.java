package ir.eitaa.ui.Components;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.MessagesStorage;
import ir.eitaa.messenger.NotificationsController;
import ir.eitaa.messenger.Utilities;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.TL_account_reportPeer;
import ir.eitaa.tgnet.TLRPC.TL_dialog;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_inputReportReasonPornography;
import ir.eitaa.tgnet.TLRPC.TL_inputReportReasonSpam;
import ir.eitaa.tgnet.TLRPC.TL_inputReportReasonViolence;
import ir.eitaa.tgnet.TLRPC.TL_peerNotifySettings;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.ActionBar.BottomSheet.Builder;
import ir.eitaa.ui.ReportOtherActivity;
import java.util.concurrent.ConcurrentHashMap;

public class AlertsCreator
{
  public static Dialog createMuteAlert(Context paramContext, long paramLong)
  {
    if (paramContext == null) {
      return null;
    }
    paramContext = new BottomSheet.Builder(paramContext);
    paramContext.setTitle(LocaleController.getString("Notifications", 2131166033));
    String str1 = LocaleController.formatString("MuteFor", 2131165910, new Object[] { LocaleController.formatPluralString("Hours", 1) });
    String str2 = LocaleController.formatString("MuteFor", 2131165910, new Object[] { LocaleController.formatPluralString("Hours", 8) });
    String str3 = LocaleController.formatString("MuteFor", 2131165910, new Object[] { LocaleController.formatPluralString("Days", 2) });
    String str4 = LocaleController.getString("MuteDisable", 2131165909);
    DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        int i = ConnectionsManager.getInstance().getCurrentTime();
        if (paramAnonymousInt == 0)
        {
          i += 3600;
          paramAnonymousDialogInterface = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0).edit();
          if (paramAnonymousInt != 3) {
            break label188;
          }
          paramAnonymousDialogInterface.putInt("notify2_" + this.val$dialog_id, 2);
        }
        for (long l = 1L;; l = i << 32 | 1L)
        {
          NotificationsController.getInstance().removeNotificationsForDialog(this.val$dialog_id);
          MessagesStorage.getInstance().setDialogFlags(this.val$dialog_id, l);
          paramAnonymousDialogInterface.commit();
          paramAnonymousDialogInterface = (TLRPC.TL_dialog)MessagesController.getInstance().dialogs_dict.get(Long.valueOf(this.val$dialog_id));
          if (paramAnonymousDialogInterface != null)
          {
            paramAnonymousDialogInterface.notify_settings = new TLRPC.TL_peerNotifySettings();
            paramAnonymousDialogInterface.notify_settings.mute_until = i;
          }
          NotificationsController.updateServerNotificationsSettings(this.val$dialog_id);
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
          label188:
          paramAnonymousDialogInterface.putInt("notify2_" + this.val$dialog_id, 3);
          paramAnonymousDialogInterface.putInt("notifyuntil_" + this.val$dialog_id, i);
        }
      }
    };
    paramContext.setItems(new CharSequence[] { str1, str2, str3, str4 }, local1);
    return paramContext.create();
  }
  
  public static Dialog createReportAlert(Context paramContext, long paramLong, BaseFragment paramBaseFragment)
  {
    if ((paramContext == null) || (paramBaseFragment == null)) {
      return null;
    }
    paramContext = new BottomSheet.Builder(paramContext);
    paramContext.setTitle(LocaleController.getString("ReportChat", 2131166162));
    String str1 = LocaleController.getString("ReportChatSpam", 2131166166);
    String str2 = LocaleController.getString("ReportChatViolence", 2131166167);
    String str3 = LocaleController.getString("ReportChatPornography", 2131166165);
    String str4 = LocaleController.getString("ReportChatOther", 2131166164);
    paramBaseFragment = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (paramAnonymousInt == 3)
        {
          paramAnonymousDialogInterface = new Bundle();
          paramAnonymousDialogInterface.putLong("dialog_id", this.val$dialog_id);
          this.val$parentFragment.presentFragment(new ReportOtherActivity(paramAnonymousDialogInterface));
          return;
        }
        paramAnonymousDialogInterface = new TLRPC.TL_account_reportPeer();
        paramAnonymousDialogInterface.peer = MessagesController.getInputPeer((int)this.val$dialog_id);
        if (paramAnonymousInt == 0) {
          paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonSpam();
        }
        for (;;)
        {
          ConnectionsManager.getInstance().sendRequest(paramAnonymousDialogInterface, new RequestDelegate()
          {
            public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error) {}
          });
          return;
          if (paramAnonymousInt == 1) {
            paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonViolence();
          } else if (paramAnonymousInt == 2) {
            paramAnonymousDialogInterface.reason = new TLRPC.TL_inputReportReasonPornography();
          }
        }
      }
    };
    paramContext.setItems(new CharSequence[] { str1, str2, str3, str4 }, paramBaseFragment);
    return paramContext.create();
  }
  
  public static void showAddUserAlert(String paramString, BaseFragment paramBaseFragment, boolean paramBoolean)
  {
    if ((paramString == null) || (paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131165299));
    int i = -1;
    switch (paramString.hashCode())
    {
    default: 
      switch (i)
      {
      default: 
        localBuilder.setMessage(paramString);
      }
      break;
    }
    for (;;)
    {
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131166047), null);
      paramBaseFragment.showDialog(localBuilder.create(), true);
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
      localBuilder.setMessage(LocaleController.getString("NobodyLikesSpam2", 2131165964));
      localBuilder.setNegativeButton(LocaleController.getString("MoreInfo", 2131165908), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          MessagesController.openByUserName("spambot", this.val$fragment, 1);
        }
      });
      continue;
      if (paramBoolean)
      {
        localBuilder.setMessage(LocaleController.getString("ChannelUserCantAdd", 2131165482));
      }
      else
      {
        localBuilder.setMessage(LocaleController.getString("GroupUserCantAdd", 2131165725));
        continue;
        if (paramBoolean)
        {
          localBuilder.setMessage(LocaleController.getString("ChannelUserAddLimit", 2131165481));
        }
        else
        {
          localBuilder.setMessage(LocaleController.getString("GroupUserAddLimit", 2131165724));
          continue;
          if (paramBoolean)
          {
            localBuilder.setMessage(LocaleController.getString("ChannelUserLeftError", 2131165485));
          }
          else
          {
            localBuilder.setMessage(LocaleController.getString("GroupUserLeftError", 2131165728));
            continue;
            if (paramBoolean)
            {
              localBuilder.setMessage(LocaleController.getString("ChannelUserCantAdmin", 2131165483));
            }
            else
            {
              localBuilder.setMessage(LocaleController.getString("GroupUserCantAdmin", 2131165726));
              continue;
              if (paramBoolean)
              {
                localBuilder.setMessage(LocaleController.getString("ChannelUserCantBot", 2131165484));
              }
              else
              {
                localBuilder.setMessage(LocaleController.getString("GroupUserCantBot", 2131165727));
                continue;
                if (paramBoolean)
                {
                  localBuilder.setMessage(LocaleController.getString("InviteToChannelError", 2131165767));
                }
                else
                {
                  localBuilder.setMessage(LocaleController.getString("InviteToGroupError", 2131165769));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("CreateGroupError", 2131165535));
                  continue;
                  localBuilder.setMessage(LocaleController.getString("UserRestricted", 2131166367));
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
      localBuilder.setTitle(LocaleController.getString("AppName", 2131165299));
      localBuilder.setMessage(LocaleController.formatString("FloodWaitTime", 2131165643, new Object[] { paramString }));
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131166047), null);
      paramBaseFragment.showDialog(localBuilder.create(), true);
      return;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/AlertsCreator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */