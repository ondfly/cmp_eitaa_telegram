package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Parcelable;
import android.provider.Settings.System;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_account_resetNotifySettings;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class NotificationsSettingsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private ListAdapter adapter;
  private int androidAutoAlertRow;
  private int badgeNumberRow;
  private int callsRingtoneRow;
  private int callsSectionRow;
  private int callsSectionRow2;
  private int callsVibrateRow;
  private int contactJoinedRow;
  private int eventsSectionRow;
  private int eventsSectionRow2;
  private int groupAlertRow;
  private int groupLedRow;
  private int groupPopupNotificationRow;
  private int groupPreviewRow;
  private int groupPriorityRow;
  private int groupSectionRow;
  private int groupSectionRow2;
  private int groupSoundRow;
  private int groupVibrateRow;
  private int inappPreviewRow;
  private int inappPriorityRow;
  private int inappSectionRow;
  private int inappSectionRow2;
  private int inappSoundRow;
  private int inappVibrateRow;
  private int inchatSoundRow;
  private RecyclerListView listView;
  private int messageAlertRow;
  private int messageLedRow;
  private int messagePopupNotificationRow;
  private int messagePreviewRow;
  private int messagePriorityRow;
  private int messageSectionRow;
  private int messageSoundRow;
  private int messageVibrateRow;
  private int notificationsServiceConnectionRow;
  private int notificationsServiceRow;
  private int otherSectionRow;
  private int otherSectionRow2;
  private int pinnedMessageRow;
  private int repeatRow;
  private int resetNotificationsRow;
  private int resetSectionRow;
  private int resetSectionRow2;
  private boolean reseting = false;
  private int rowCount = 0;
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("NotificationsAndSounds", 2131494005));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          NotificationsSettingsActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    Object localObject = (FrameLayout)this.fragmentView;
    ((FrameLayout)localObject).setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    });
    this.listView.setVerticalScrollBarEnabled(false);
    ((FrameLayout)localObject).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    localObject = this.listView;
    paramContext = new ListAdapter(paramContext);
    this.adapter = paramContext;
    ((RecyclerListView)localObject).setAdapter(paramContext);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, final int paramAnonymousInt)
      {
        boolean bool3 = false;
        boolean bool2 = false;
        boolean bool1 = false;
        Object localObject1;
        Object localObject3;
        if ((paramAnonymousInt == NotificationsSettingsActivity.this.messageAlertRow) || (paramAnonymousInt == NotificationsSettingsActivity.this.groupAlertRow))
        {
          localObject1 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
          localObject3 = ((SharedPreferences)localObject1).edit();
          if (paramAnonymousInt == NotificationsSettingsActivity.this.messageAlertRow)
          {
            bool2 = ((SharedPreferences)localObject1).getBoolean("EnableAll", true);
            if (!bool2)
            {
              bool1 = true;
              ((SharedPreferences.Editor)localObject3).putBoolean("EnableAll", bool1);
              bool1 = bool2;
              label99:
              ((SharedPreferences.Editor)localObject3).commit();
              localObject1 = NotificationsSettingsActivity.this;
              if (paramAnonymousInt != NotificationsSettingsActivity.this.groupAlertRow) {
                break label223;
              }
              bool2 = true;
              label127:
              ((NotificationsSettingsActivity)localObject1).updateServerNotificationsSettings(bool2);
              label134:
              if ((paramAnonymousView instanceof TextCheckCell))
              {
                paramAnonymousView = (TextCheckCell)paramAnonymousView;
                if (bool1) {
                  break label2773;
                }
              }
            }
          }
        }
        label223:
        label303:
        label319:
        label423:
        label677:
        label682:
        label687:
        label695:
        label705:
        label927:
        label1234:
        label1294:
        label1452:
        label1501:
        label1744:
        label1823:
        label1935:
        label2051:
        label2122:
        label2174:
        label2180:
        label2247:
        label2300:
        label2306:
        label2447:
        label2504:
        label2558:
        label2773:
        for (bool1 = true;; bool1 = false)
        {
          paramAnonymousView.setChecked(bool1);
          do
          {
            do
            {
              do
              {
                do
                {
                  return;
                  bool1 = false;
                  break;
                  if (paramAnonymousInt != NotificationsSettingsActivity.this.groupAlertRow) {
                    break label99;
                  }
                  bool2 = ((SharedPreferences)localObject1).getBoolean("EnableGroup", true);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableGroup", bool1);
                    bool1 = bool2;
                    break;
                  }
                  bool2 = false;
                  break label127;
                  if ((paramAnonymousInt == NotificationsSettingsActivity.this.messagePreviewRow) || (paramAnonymousInt == NotificationsSettingsActivity.this.groupPreviewRow))
                  {
                    localObject1 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                    localObject3 = ((SharedPreferences)localObject1).edit();
                    if (paramAnonymousInt == NotificationsSettingsActivity.this.messagePreviewRow)
                    {
                      bool2 = ((SharedPreferences)localObject1).getBoolean("EnablePreviewAll", true);
                      if (!bool2)
                      {
                        bool1 = true;
                        ((SharedPreferences.Editor)localObject3).putBoolean("EnablePreviewAll", bool1);
                        bool1 = bool2;
                        ((SharedPreferences.Editor)localObject3).commit();
                        localObject1 = NotificationsSettingsActivity.this;
                        if (paramAnonymousInt != NotificationsSettingsActivity.this.groupPreviewRow) {
                          break label423;
                        }
                      }
                    }
                    for (bool2 = true;; bool2 = false)
                    {
                      ((NotificationsSettingsActivity)localObject1).updateServerNotificationsSettings(bool2);
                      break;
                      bool1 = false;
                      break label303;
                      bool1 = bool2;
                      if (paramAnonymousInt != NotificationsSettingsActivity.this.groupPreviewRow) {
                        break label319;
                      }
                      bool2 = ((SharedPreferences)localObject1).getBoolean("EnablePreviewGroup", true);
                      if (!bool2) {}
                      for (bool1 = true;; bool1 = false)
                      {
                        ((SharedPreferences.Editor)localObject3).putBoolean("EnablePreviewGroup", bool1);
                        bool1 = bool2;
                        break;
                      }
                    }
                  }
                  if ((paramAnonymousInt == NotificationsSettingsActivity.this.messageSoundRow) || (paramAnonymousInt == NotificationsSettingsActivity.this.groupSoundRow) || (paramAnonymousInt == NotificationsSettingsActivity.this.callsRingtoneRow)) {
                    for (;;)
                    {
                      try
                      {
                        localObject6 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                        localObject5 = new Intent("android.intent.action.RINGTONE_PICKER");
                        if (paramAnonymousInt != NotificationsSettingsActivity.this.callsRingtoneRow) {
                          break label677;
                        }
                        i = 1;
                        ((Intent)localObject5).putExtra("android.intent.extra.ringtone.TYPE", i);
                        ((Intent)localObject5).putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", true);
                        if (paramAnonymousInt != NotificationsSettingsActivity.this.callsRingtoneRow) {
                          break label682;
                        }
                        i = 1;
                        ((Intent)localObject5).putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(i));
                        str1 = null;
                        localObject4 = null;
                        if (paramAnonymousInt != NotificationsSettingsActivity.this.callsRingtoneRow) {
                          break label687;
                        }
                        localObject3 = Settings.System.DEFAULT_RINGTONE_URI;
                        if (localObject3 != null) {
                          localObject4 = ((Uri)localObject3).getPath();
                        }
                        if (paramAnonymousInt != NotificationsSettingsActivity.this.messageSoundRow) {
                          break label705;
                        }
                        localObject6 = ((SharedPreferences)localObject6).getString("GlobalSoundPath", (String)localObject4);
                        localObject1 = str1;
                        if (localObject6 != null)
                        {
                          localObject1 = str1;
                          if (!((String)localObject6).equals("NoSound"))
                          {
                            if (!((String)localObject6).equals(localObject4)) {
                              break label695;
                            }
                            localObject1 = localObject3;
                          }
                        }
                        ((Intent)localObject5).putExtra("android.intent.extra.ringtone.EXISTING_URI", (Parcelable)localObject1);
                        NotificationsSettingsActivity.this.startActivityForResult((Intent)localObject5, paramAnonymousInt);
                        bool1 = bool3;
                      }
                      catch (Exception localException)
                      {
                        FileLog.e(localException);
                        bool1 = bool3;
                      }
                      break;
                      int i = 2;
                      continue;
                      i = 2;
                      continue;
                      localObject3 = Settings.System.DEFAULT_NOTIFICATION_URI;
                      continue;
                      localObject2 = Uri.parse((String)localObject6);
                      continue;
                      if (paramAnonymousInt == NotificationsSettingsActivity.this.groupSoundRow)
                      {
                        localObject6 = ((SharedPreferences)localObject6).getString("GroupSoundPath", (String)localObject4);
                        localObject2 = str1;
                        if (localObject6 != null)
                        {
                          localObject2 = str1;
                          if (!((String)localObject6).equals("NoSound")) {
                            if (((String)localObject6).equals(localObject4)) {
                              localObject2 = localObject3;
                            } else {
                              localObject2 = Uri.parse((String)localObject6);
                            }
                          }
                        }
                      }
                      else
                      {
                        localObject2 = str1;
                        if (paramAnonymousInt == NotificationsSettingsActivity.this.callsRingtoneRow)
                        {
                          localObject6 = ((SharedPreferences)localObject6).getString("CallsRingtonfePath", (String)localObject4);
                          localObject2 = str1;
                          if (localObject6 != null)
                          {
                            localObject2 = str1;
                            if (!((String)localObject6).equals("NoSound")) {
                              if (((String)localObject6).equals(localObject4)) {
                                localObject2 = localObject3;
                              } else {
                                localObject2 = Uri.parse((String)localObject6);
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                  if (paramAnonymousInt != NotificationsSettingsActivity.this.resetNotificationsRow) {
                    break label927;
                  }
                } while (NotificationsSettingsActivity.this.reseting);
                NotificationsSettingsActivity.access$1102(NotificationsSettingsActivity.this, true);
                localObject2 = new TLRPC.TL_account_resetNotifySettings();
                ConnectionsManager.getInstance(NotificationsSettingsActivity.this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
                {
                  public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        MessagesController.getInstance(NotificationsSettingsActivity.this.currentAccount).enableJoined = true;
                        NotificationsSettingsActivity.access$1102(NotificationsSettingsActivity.this, false);
                        SharedPreferences.Editor localEditor = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount).edit();
                        localEditor.clear();
                        localEditor.commit();
                        NotificationsSettingsActivity.this.adapter.notifyDataSetChanged();
                        if (NotificationsSettingsActivity.this.getParentActivity() != null) {
                          Toast.makeText(NotificationsSettingsActivity.this.getParentActivity(), LocaleController.getString("ResetNotificationsText", 2131494264), 0).show();
                        }
                      }
                    });
                  }
                });
                bool1 = bool3;
                break label134;
                if (paramAnonymousInt == NotificationsSettingsActivity.this.inappSoundRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableInAppSounds", true);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableInAppSounds", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.inappVibrateRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableInAppVibrate", true);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableInAppVibrate", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.inappPreviewRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableInAppPreview", true);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableInAppPreview", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.inchatSoundRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableInChatSound", true);
                  if (!bool2)
                  {
                    bool1 = true;
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableInChatSound", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    localObject2 = NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount);
                    if (bool2) {
                      break label1294;
                    }
                  }
                  for (bool1 = true;; bool1 = false)
                  {
                    ((NotificationsController)localObject2).setInChatSoundEnabled(bool1);
                    bool1 = bool2;
                    break;
                    bool1 = false;
                    break label1234;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.inappPriorityRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableInAppPriority", false);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableInAppPriority", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.contactJoinedRow)
                {
                  localObject3 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject2 = ((SharedPreferences)localObject3).edit();
                  bool2 = ((SharedPreferences)localObject3).getBoolean("EnableContactJoined", true);
                  localObject3 = MessagesController.getInstance(NotificationsSettingsActivity.this.currentAccount);
                  if (!bool2)
                  {
                    bool1 = true;
                    ((MessagesController)localObject3).enableJoined = bool1;
                    if (bool2) {
                      break label1501;
                    }
                  }
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject2).putBoolean("EnableContactJoined", bool1);
                    ((SharedPreferences.Editor)localObject2).commit();
                    bool1 = bool2;
                    break;
                    bool1 = false;
                    break label1452;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.pinnedMessageRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("PinnedMessages", true);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("PinnedMessages", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.androidAutoAlertRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  localObject3 = ((SharedPreferences)localObject2).edit();
                  bool2 = ((SharedPreferences)localObject2).getBoolean("EnableAutoNotifications", false);
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject3).putBoolean("EnableAutoNotifications", bool1);
                    ((SharedPreferences.Editor)localObject3).commit();
                    bool1 = bool2;
                    break;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.badgeNumberRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount).edit();
                  bool2 = NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount).showBadgeNumber;
                  localObject3 = NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount);
                  if (!bool2)
                  {
                    bool1 = true;
                    ((NotificationsController)localObject3).showBadgeNumber = bool1;
                    ((SharedPreferences.Editor)localObject2).putBoolean("badgeNumber", NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount).showBadgeNumber);
                    ((SharedPreferences.Editor)localObject2).commit();
                    localObject2 = NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount);
                    if (bool2) {
                      break label1823;
                    }
                  }
                  for (bool1 = true;; bool1 = false)
                  {
                    ((NotificationsController)localObject2).setBadgeEnabled(bool1);
                    bool1 = bool2;
                    break;
                    bool1 = false;
                    break label1744;
                  }
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.notificationsServiceConnectionRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  bool2 = ((SharedPreferences)localObject2).getBoolean("pushConnection", true);
                  localObject2 = ((SharedPreferences)localObject2).edit();
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject2).putBoolean("pushConnection", bool1);
                    ((SharedPreferences.Editor)localObject2).commit();
                    if (bool2) {
                      break label1935;
                    }
                    ConnectionsManager.getInstance(NotificationsSettingsActivity.this.currentAccount).setPushConnectionEnabled(true);
                    bool1 = bool2;
                    break;
                  }
                  ConnectionsManager.getInstance(NotificationsSettingsActivity.this.currentAccount).setPushConnectionEnabled(false);
                  bool1 = bool2;
                  break label134;
                }
                if (paramAnonymousInt == NotificationsSettingsActivity.this.notificationsServiceRow)
                {
                  localObject2 = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
                  bool2 = ((SharedPreferences)localObject2).getBoolean("pushService", true);
                  localObject2 = ((SharedPreferences)localObject2).edit();
                  if (!bool2) {}
                  for (bool1 = true;; bool1 = false)
                  {
                    ((SharedPreferences.Editor)localObject2).putBoolean("pushService", bool1);
                    ((SharedPreferences.Editor)localObject2).commit();
                    if (bool2) {
                      break label2051;
                    }
                    ApplicationLoader.startPushService();
                    bool1 = bool2;
                    break;
                  }
                  ApplicationLoader.stopPushService();
                  bool1 = bool2;
                  break label134;
                }
                if ((paramAnonymousInt != NotificationsSettingsActivity.this.messageLedRow) && (paramAnonymousInt != NotificationsSettingsActivity.this.groupLedRow)) {
                  break label2180;
                }
              } while (NotificationsSettingsActivity.this.getParentActivity() == null);
              localObject2 = NotificationsSettingsActivity.this;
              localObject3 = NotificationsSettingsActivity.this.getParentActivity();
              if (paramAnonymousInt == NotificationsSettingsActivity.this.groupLedRow)
              {
                bool1 = true;
                if (paramAnonymousInt != NotificationsSettingsActivity.this.messageLedRow) {
                  break label2174;
                }
              }
              for (bool2 = true;; bool2 = false)
              {
                ((NotificationsSettingsActivity)localObject2).showDialog(AlertsCreator.createColorSelectDialog((Activity)localObject3, 0L, bool1, bool2, new Runnable()
                {
                  public void run()
                  {
                    NotificationsSettingsActivity.this.adapter.notifyItemChanged(paramAnonymousInt);
                  }
                }));
                bool1 = bool3;
                break;
                bool1 = false;
                break label2122;
              }
              if ((paramAnonymousInt != NotificationsSettingsActivity.this.messagePopupNotificationRow) && (paramAnonymousInt != NotificationsSettingsActivity.this.groupPopupNotificationRow)) {
                break label2306;
              }
            } while (NotificationsSettingsActivity.this.getParentActivity() == null);
            localObject2 = NotificationsSettingsActivity.this;
            localObject3 = NotificationsSettingsActivity.this.getParentActivity();
            localObject4 = NotificationsSettingsActivity.this;
            if (paramAnonymousInt == NotificationsSettingsActivity.this.groupPopupNotificationRow)
            {
              bool1 = true;
              if (paramAnonymousInt != NotificationsSettingsActivity.this.messagePopupNotificationRow) {
                break label2300;
              }
            }
            for (bool2 = true;; bool2 = false)
            {
              ((NotificationsSettingsActivity)localObject2).showDialog(AlertsCreator.createPopupSelectDialog((Activity)localObject3, (BaseFragment)localObject4, bool1, bool2, new Runnable()
              {
                public void run()
                {
                  NotificationsSettingsActivity.this.adapter.notifyItemChanged(paramAnonymousInt);
                }
              }));
              bool1 = bool3;
              break;
              bool1 = false;
              break label2247;
            }
            if ((paramAnonymousInt != NotificationsSettingsActivity.this.messageVibrateRow) && (paramAnonymousInt != NotificationsSettingsActivity.this.groupVibrateRow) && (paramAnonymousInt != NotificationsSettingsActivity.this.callsVibrateRow)) {
              break label2447;
            }
          } while (NotificationsSettingsActivity.this.getParentActivity() == null);
          Object localObject2 = null;
          if (paramAnonymousInt == NotificationsSettingsActivity.this.messageVibrateRow) {
            localObject2 = "vibrate_messages";
          }
          for (;;)
          {
            NotificationsSettingsActivity.this.showDialog(AlertsCreator.createVibrationSelectDialog(NotificationsSettingsActivity.this.getParentActivity(), NotificationsSettingsActivity.this, 0L, (String)localObject2, new Runnable()
            {
              public void run()
              {
                NotificationsSettingsActivity.this.adapter.notifyItemChanged(paramAnonymousInt);
              }
            }));
            bool1 = bool3;
            break;
            if (paramAnonymousInt == NotificationsSettingsActivity.this.groupVibrateRow) {
              localObject2 = "vibrate_group";
            } else if (paramAnonymousInt == NotificationsSettingsActivity.this.callsVibrateRow) {
              localObject2 = "vibrate_calls";
            }
          }
          if ((paramAnonymousInt == NotificationsSettingsActivity.this.messagePriorityRow) || (paramAnonymousInt == NotificationsSettingsActivity.this.groupPriorityRow))
          {
            localObject2 = NotificationsSettingsActivity.this;
            localObject3 = NotificationsSettingsActivity.this.getParentActivity();
            localObject4 = NotificationsSettingsActivity.this;
            if (paramAnonymousInt == NotificationsSettingsActivity.this.groupPriorityRow)
            {
              bool1 = true;
              if (paramAnonymousInt != NotificationsSettingsActivity.this.messagePriorityRow) {
                break label2558;
              }
            }
            for (bool2 = true;; bool2 = false)
            {
              ((NotificationsSettingsActivity)localObject2).showDialog(AlertsCreator.createPrioritySelectDialog((Activity)localObject3, (BaseFragment)localObject4, 0L, bool1, bool2, new Runnable()
              {
                public void run()
                {
                  NotificationsSettingsActivity.this.adapter.notifyItemChanged(paramAnonymousInt);
                }
              }));
              bool1 = bool3;
              break;
              bool1 = false;
              break label2504;
            }
          }
          bool1 = bool3;
          if (paramAnonymousInt != NotificationsSettingsActivity.this.repeatRow) {
            break label134;
          }
          localObject2 = new AlertDialog.Builder(NotificationsSettingsActivity.this.getParentActivity());
          ((AlertDialog.Builder)localObject2).setTitle(LocaleController.getString("RepeatNotifications", 2131494233));
          localObject3 = LocaleController.getString("RepeatDisabled", 2131494232);
          Object localObject4 = LocaleController.formatPluralString("Minutes", 5);
          String str1 = LocaleController.formatPluralString("Minutes", 10);
          Object localObject5 = LocaleController.formatPluralString("Minutes", 30);
          Object localObject6 = LocaleController.formatPluralString("Hours", 1);
          String str2 = LocaleController.formatPluralString("Hours", 2);
          String str3 = LocaleController.formatPluralString("Hours", 4);
          DialogInterface.OnClickListener local6 = new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              int i = 0;
              if (paramAnonymous2Int == 1) {
                i = 5;
              }
              for (;;)
              {
                MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount).edit().putInt("repeat_messages", i).commit();
                NotificationsSettingsActivity.this.adapter.notifyItemChanged(paramAnonymousInt);
                return;
                if (paramAnonymous2Int == 2) {
                  i = 10;
                } else if (paramAnonymous2Int == 3) {
                  i = 30;
                } else if (paramAnonymous2Int == 4) {
                  i = 60;
                } else if (paramAnonymous2Int == 5) {
                  i = 120;
                } else if (paramAnonymous2Int == 6) {
                  i = 240;
                }
              }
            }
          };
          ((AlertDialog.Builder)localObject2).setItems(new CharSequence[] { localObject3, localObject4, str1, localObject5, localObject6, str2, str3 }, local6);
          ((AlertDialog.Builder)localObject2).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          NotificationsSettingsActivity.this.showDialog(((AlertDialog.Builder)localObject2).create());
          bool1 = bool3;
          break label134;
        }
      }
    });
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.notificationsSettingsUpdated) {
      this.adapter.notifyDataSetChanged();
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { HeaderCell.class, TextCheckCell.class, TextDetailSettingsCell.class, TextColorCell.class, TextSettingsCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextColorCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2") };
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    Uri localUri;
    SharedPreferences.Editor localEditor;
    Ringtone localRingtone;
    if (paramInt2 == -1)
    {
      localUri = (Uri)paramIntent.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
      localEditor = null;
      paramIntent = localEditor;
      if (localUri != null)
      {
        localRingtone = RingtoneManager.getRingtone(getParentActivity(), localUri);
        paramIntent = localEditor;
        if (localRingtone != null)
        {
          if (paramInt1 != this.callsRingtoneRow) {
            break label170;
          }
          if (!localUri.equals(Settings.System.DEFAULT_RINGTONE_URI)) {
            break label157;
          }
          paramIntent = LocaleController.getString("DefaultRingtone", 2131493355);
          localRingtone.stop();
        }
      }
      localEditor = MessagesController.getNotificationsSettings(this.currentAccount).edit();
      if (paramInt1 != this.messageSoundRow) {
        break label238;
      }
      if ((paramIntent == null) || (localUri == null)) {
        break label207;
      }
      localEditor.putString("GlobalSound", paramIntent);
      localEditor.putString("GlobalSoundPath", localUri.toString());
    }
    for (;;)
    {
      localEditor.commit();
      this.adapter.notifyItemChanged(paramInt1);
      return;
      label157:
      paramIntent = localRingtone.getTitle(getParentActivity());
      break;
      label170:
      if (localUri.equals(Settings.System.DEFAULT_NOTIFICATION_URI))
      {
        paramIntent = LocaleController.getString("SoundDefault", 2131494420);
        break;
      }
      paramIntent = localRingtone.getTitle(getParentActivity());
      break;
      label207:
      localEditor.putString("GlobalSound", "NoSound");
      localEditor.putString("GlobalSoundPath", "NoSound");
      continue;
      label238:
      if (paramInt1 == this.groupSoundRow)
      {
        if ((paramIntent != null) && (localUri != null))
        {
          localEditor.putString("GroupSound", paramIntent);
          localEditor.putString("GroupSoundPath", localUri.toString());
        }
        else
        {
          localEditor.putString("GroupSound", "NoSound");
          localEditor.putString("GroupSoundPath", "NoSound");
        }
      }
      else if (paramInt1 == this.callsRingtoneRow) {
        if ((paramIntent != null) && (localUri != null))
        {
          localEditor.putString("CallsRingtone", paramIntent);
          localEditor.putString("CallsRingtonePath", localUri.toString());
        }
        else
        {
          localEditor.putString("CallsRingtone", "NoSound");
          localEditor.putString("CallsRingtonePath", "NoSound");
        }
      }
    }
  }
  
  public boolean onFragmentCreate()
  {
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.messageSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messageAlertRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messagePreviewRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messageLedRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messageVibrateRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messagePopupNotificationRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messageSoundRow = i;
    if (Build.VERSION.SDK_INT >= 21)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.messagePriorityRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupAlertRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupPreviewRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupLedRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupVibrateRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupPopupNotificationRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupSoundRow = i;
      if (Build.VERSION.SDK_INT < 21) {
        break label753;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.groupPriorityRow = i;
      label305:
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inappSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inappSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inappSoundRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inappVibrateRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inappPreviewRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.inchatSoundRow = i;
      if (Build.VERSION.SDK_INT < 21) {
        break label761;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    label753:
    label761:
    for (this.inappPriorityRow = i;; this.inappPriorityRow = -1)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsVibrateRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsRingtoneRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.eventsSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.eventsSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.contactJoinedRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.pinnedMessageRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.otherSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.otherSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.notificationsServiceRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.notificationsServiceConnectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.badgeNumberRow = i;
      this.androidAutoAlertRow = -1;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.repeatRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.resetSectionRow2 = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.resetSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.resetNotificationsRow = i;
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.notificationsSettingsUpdated);
      return super.onFragmentCreate();
      this.messagePriorityRow = -1;
      break;
      this.groupPriorityRow = -1;
      break label305;
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
  }
  
  public void updateServerNotificationsSettings(boolean paramBoolean) {}
  
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
      return NotificationsSettingsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == NotificationsSettingsActivity.this.messageSectionRow) || (paramInt == NotificationsSettingsActivity.this.groupSectionRow) || (paramInt == NotificationsSettingsActivity.this.inappSectionRow) || (paramInt == NotificationsSettingsActivity.this.eventsSectionRow) || (paramInt == NotificationsSettingsActivity.this.otherSectionRow) || (paramInt == NotificationsSettingsActivity.this.resetSectionRow) || (paramInt == NotificationsSettingsActivity.this.callsSectionRow)) {
        return 0;
      }
      if ((paramInt == NotificationsSettingsActivity.this.messageAlertRow) || (paramInt == NotificationsSettingsActivity.this.messagePreviewRow) || (paramInt == NotificationsSettingsActivity.this.groupAlertRow) || (paramInt == NotificationsSettingsActivity.this.groupPreviewRow) || (paramInt == NotificationsSettingsActivity.this.inappSoundRow) || (paramInt == NotificationsSettingsActivity.this.inappVibrateRow) || (paramInt == NotificationsSettingsActivity.this.inappPreviewRow) || (paramInt == NotificationsSettingsActivity.this.contactJoinedRow) || (paramInt == NotificationsSettingsActivity.this.pinnedMessageRow) || (paramInt == NotificationsSettingsActivity.this.notificationsServiceRow) || (paramInt == NotificationsSettingsActivity.this.badgeNumberRow) || (paramInt == NotificationsSettingsActivity.this.inappPriorityRow) || (paramInt == NotificationsSettingsActivity.this.inchatSoundRow) || (paramInt == NotificationsSettingsActivity.this.androidAutoAlertRow) || (paramInt == NotificationsSettingsActivity.this.notificationsServiceConnectionRow)) {
        return 1;
      }
      if ((paramInt == NotificationsSettingsActivity.this.messageLedRow) || (paramInt == NotificationsSettingsActivity.this.groupLedRow)) {
        return 3;
      }
      if ((paramInt == NotificationsSettingsActivity.this.eventsSectionRow2) || (paramInt == NotificationsSettingsActivity.this.groupSectionRow2) || (paramInt == NotificationsSettingsActivity.this.inappSectionRow2) || (paramInt == NotificationsSettingsActivity.this.otherSectionRow2) || (paramInt == NotificationsSettingsActivity.this.resetSectionRow2) || (paramInt == NotificationsSettingsActivity.this.callsSectionRow2)) {
        return 4;
      }
      if (paramInt == NotificationsSettingsActivity.this.resetNotificationsRow) {
        return 2;
      }
      return 5;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      return (i != NotificationsSettingsActivity.this.messageSectionRow) && (i != NotificationsSettingsActivity.this.groupSectionRow) && (i != NotificationsSettingsActivity.this.inappSectionRow) && (i != NotificationsSettingsActivity.this.eventsSectionRow) && (i != NotificationsSettingsActivity.this.otherSectionRow) && (i != NotificationsSettingsActivity.this.resetSectionRow) && (i != NotificationsSettingsActivity.this.eventsSectionRow2) && (i != NotificationsSettingsActivity.this.groupSectionRow2) && (i != NotificationsSettingsActivity.this.inappSectionRow2) && (i != NotificationsSettingsActivity.this.otherSectionRow2) && (i != NotificationsSettingsActivity.this.resetSectionRow2) && (i != NotificationsSettingsActivity.this.callsSectionRow2) && (i != NotificationsSettingsActivity.this.callsSectionRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      Object localObject;
      TextSettingsCell localTextSettingsCell;
      do
      {
        do
        {
          do
          {
            do
            {
              do
              {
                return;
                paramViewHolder = (HeaderCell)paramViewHolder.itemView;
                if (paramInt == NotificationsSettingsActivity.this.messageSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("MessageNotifications", 2131493825));
                  return;
                }
                if (paramInt == NotificationsSettingsActivity.this.groupSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("GroupNotifications", 2131493634));
                  return;
                }
                if (paramInt == NotificationsSettingsActivity.this.inappSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("InAppNotifications", 2131493667));
                  return;
                }
                if (paramInt == NotificationsSettingsActivity.this.eventsSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("Events", 2131493530));
                  return;
                }
                if (paramInt == NotificationsSettingsActivity.this.otherSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("NotificationsOther", 2131494015));
                  return;
                }
                if (paramInt == NotificationsSettingsActivity.this.resetSectionRow)
                {
                  paramViewHolder.setText(LocaleController.getString("Reset", 2131494250));
                  return;
                }
              } while (paramInt != NotificationsSettingsActivity.this.callsSectionRow);
              paramViewHolder.setText(LocaleController.getString("VoipNotificationSettings", 2131494601));
              return;
              paramViewHolder = (TextCheckCell)paramViewHolder.itemView;
              localObject = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
              if (paramInt == NotificationsSettingsActivity.this.messageAlertRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("Alert", 2131492948), ((SharedPreferences)localObject).getBoolean("EnableAll", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.groupAlertRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("Alert", 2131492948), ((SharedPreferences)localObject).getBoolean("EnableGroup", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.messagePreviewRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("MessagePreview", 2131493826), ((SharedPreferences)localObject).getBoolean("EnablePreviewAll", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.groupPreviewRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("MessagePreview", 2131493826), ((SharedPreferences)localObject).getBoolean("EnablePreviewGroup", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.inappSoundRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("InAppSounds", 2131493669), ((SharedPreferences)localObject).getBoolean("EnableInAppSounds", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.inappVibrateRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("InAppVibrate", 2131493670), ((SharedPreferences)localObject).getBoolean("EnableInAppVibrate", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.inappPreviewRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("InAppPreview", 2131493668), ((SharedPreferences)localObject).getBoolean("EnableInAppPreview", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.inappPriorityRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("NotificationsImportance", 2131494009), ((SharedPreferences)localObject).getBoolean("EnableInAppPriority", false), false);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.contactJoinedRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("ContactJoined", 2131493288), ((SharedPreferences)localObject).getBoolean("EnableContactJoined", true), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.pinnedMessageRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("PinnedMessages", 2131494172), ((SharedPreferences)localObject).getBoolean("PinnedMessages", true), false);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.androidAutoAlertRow)
              {
                paramViewHolder.setTextAndCheck("Android Auto", ((SharedPreferences)localObject).getBoolean("EnableAutoNotifications", false), true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.notificationsServiceRow)
              {
                paramViewHolder.setTextAndValueAndCheck(LocaleController.getString("NotificationsService", 2131494021), LocaleController.getString("NotificationsServiceInfo", 2131494024), ((SharedPreferences)localObject).getBoolean("pushService", true), true, true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.notificationsServiceConnectionRow)
              {
                paramViewHolder.setTextAndValueAndCheck(LocaleController.getString("NotificationsServiceConnection", 2131494022), LocaleController.getString("NotificationsServiceConnectionInfo", 2131494023), ((SharedPreferences)localObject).getBoolean("pushConnection", true), true, true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.badgeNumberRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("BadgeNumber", 2131493077), NotificationsController.getInstance(NotificationsSettingsActivity.this.currentAccount).showBadgeNumber, true);
                return;
              }
              if (paramInt == NotificationsSettingsActivity.this.inchatSoundRow)
              {
                paramViewHolder.setTextAndCheck(LocaleController.getString("InChatSound", 2131493671), ((SharedPreferences)localObject).getBoolean("EnableInChatSound", true), true);
                return;
              }
            } while (paramInt != NotificationsSettingsActivity.this.callsVibrateRow);
            paramViewHolder.setTextAndCheck(LocaleController.getString("Vibrate", 2131494574), ((SharedPreferences)localObject).getBoolean("EnableCallVibrate", true), true);
            return;
            paramViewHolder = (TextDetailSettingsCell)paramViewHolder.itemView;
            paramViewHolder.setMultilineDetail(true);
            paramViewHolder.setTextAndValue(LocaleController.getString("ResetAllNotifications", 2131494256), LocaleController.getString("UndoAllCustom", 2131494508), false);
            return;
            paramViewHolder = (TextColorCell)paramViewHolder.itemView;
            localObject = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
            if (paramInt == NotificationsSettingsActivity.this.messageLedRow)
            {
              paramInt = ((SharedPreferences)localObject).getInt("MessagesLed", -16776961);
              i = 0;
            }
            for (;;)
            {
              int j = paramInt;
              if (i < 9)
              {
                if (TextColorCell.colorsToSave[i] == paramInt) {
                  j = TextColorCell.colors[i];
                }
              }
              else
              {
                paramViewHolder.setTextAndColor(LocaleController.getString("LedColor", 2131493744), j, true);
                return;
                paramInt = ((SharedPreferences)localObject).getInt("GroupLed", -16776961);
                break;
              }
              i += 1;
            }
            localTextSettingsCell = (TextSettingsCell)paramViewHolder.itemView;
            localObject = MessagesController.getNotificationsSettings(NotificationsSettingsActivity.this.currentAccount);
            if ((paramInt == NotificationsSettingsActivity.this.messageSoundRow) || (paramInt == NotificationsSettingsActivity.this.groupSoundRow) || (paramInt == NotificationsSettingsActivity.this.callsRingtoneRow))
            {
              paramViewHolder = null;
              if (paramInt == NotificationsSettingsActivity.this.messageSoundRow) {
                paramViewHolder = ((SharedPreferences)localObject).getString("GlobalSound", LocaleController.getString("SoundDefault", 2131494420));
              }
              for (;;)
              {
                localObject = paramViewHolder;
                if (paramViewHolder.equals("NoSound")) {
                  localObject = LocaleController.getString("NoSound", 2131493913);
                }
                if (paramInt != NotificationsSettingsActivity.this.callsRingtoneRow) {
                  break;
                }
                localTextSettingsCell.setTextAndValue(LocaleController.getString("VoipSettingsRingtone", 2131494618), (String)localObject, true);
                return;
                if (paramInt == NotificationsSettingsActivity.this.groupSoundRow) {
                  paramViewHolder = ((SharedPreferences)localObject).getString("GroupSound", LocaleController.getString("SoundDefault", 2131494420));
                } else if (paramInt == NotificationsSettingsActivity.this.callsRingtoneRow) {
                  paramViewHolder = ((SharedPreferences)localObject).getString("CallsRingtone", LocaleController.getString("DefaultRingtone", 2131493355));
                }
              }
              localTextSettingsCell.setTextAndValue(LocaleController.getString("Sound", 2131494419), (String)localObject, true);
              return;
            }
            if ((paramInt != NotificationsSettingsActivity.this.messageVibrateRow) && (paramInt != NotificationsSettingsActivity.this.groupVibrateRow) && (paramInt != NotificationsSettingsActivity.this.callsVibrateRow)) {
              break;
            }
            i = 0;
            if (paramInt == NotificationsSettingsActivity.this.messageVibrateRow) {
              i = ((SharedPreferences)localObject).getInt("vibrate_messages", 0);
            }
            while (i == 0)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("VibrationDefault", 2131494575), true);
              return;
              if (paramInt == NotificationsSettingsActivity.this.groupVibrateRow) {
                i = ((SharedPreferences)localObject).getInt("vibrate_group", 0);
              } else if (paramInt == NotificationsSettingsActivity.this.callsVibrateRow) {
                i = ((SharedPreferences)localObject).getInt("vibrate_calls", 0);
              }
            }
            if (i == 1)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("Short", 2131494401), true);
              return;
            }
            if (i == 2)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("VibrationDisabled", 2131494576), true);
              return;
            }
            if (i == 3)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("Long", 2131493779), true);
              return;
            }
          } while (i != 4);
          localTextSettingsCell.setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("OnlyIfSilent", 2131494037), true);
          return;
          if (paramInt == NotificationsSettingsActivity.this.repeatRow)
          {
            paramInt = ((SharedPreferences)localObject).getInt("repeat_messages", 60);
            if (paramInt == 0) {
              paramViewHolder = LocaleController.getString("RepeatNotificationsNever", 2131494234);
            }
            for (;;)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("RepeatNotifications", 2131494233), paramViewHolder, false);
              return;
              if (paramInt < 60) {
                paramViewHolder = LocaleController.formatPluralString("Minutes", paramInt);
              } else {
                paramViewHolder = LocaleController.formatPluralString("Hours", paramInt / 60);
              }
            }
          }
          if ((paramInt != NotificationsSettingsActivity.this.messagePriorityRow) && (paramInt != NotificationsSettingsActivity.this.groupPriorityRow)) {
            break;
          }
          i = 0;
          if (paramInt == NotificationsSettingsActivity.this.messagePriorityRow) {
            i = ((SharedPreferences)localObject).getInt("priority_messages", 1);
          }
          while (i == 0)
          {
            localTextSettingsCell.setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityHigh", 2131494016), false);
            return;
            if (paramInt == NotificationsSettingsActivity.this.groupPriorityRow) {
              i = ((SharedPreferences)localObject).getInt("priority_group", 1);
            }
          }
          if ((i == 1) || (i == 2))
          {
            localTextSettingsCell.setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityUrgent", 2131494020), false);
            return;
          }
          if (i == 4)
          {
            localTextSettingsCell.setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityLow", 2131494017), false);
            return;
          }
        } while (i != 5);
        localTextSettingsCell.setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityMedium", 2131494018), false);
        return;
      } while ((paramInt != NotificationsSettingsActivity.this.messagePopupNotificationRow) && (paramInt != NotificationsSettingsActivity.this.groupPopupNotificationRow));
      int i = 0;
      if (paramInt == NotificationsSettingsActivity.this.messagePopupNotificationRow)
      {
        i = ((SharedPreferences)localObject).getInt("popupAll", 0);
        if (i != 0) {
          break label1815;
        }
        paramViewHolder = LocaleController.getString("NoPopup", 2131493902);
      }
      for (;;)
      {
        localTextSettingsCell.setTextAndValue(LocaleController.getString("PopupNotification", 2131494187), paramViewHolder, true);
        return;
        if (paramInt != NotificationsSettingsActivity.this.groupPopupNotificationRow) {
          break;
        }
        i = ((SharedPreferences)localObject).getInt("popupGroup", 0);
        break;
        label1815:
        if (i == 1) {
          paramViewHolder = LocaleController.getString("OnlyWhenScreenOn", 2131494039);
        } else if (i == 2) {
          paramViewHolder = LocaleController.getString("OnlyWhenScreenOff", 2131494038);
        } else {
          paramViewHolder = LocaleController.getString("AlwaysShowPopup", 2131492961);
        }
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextCheckCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextDetailSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextColorCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/NotificationsSettingsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */