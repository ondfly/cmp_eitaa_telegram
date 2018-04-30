package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
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
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextCheckBoxCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ProfileNotificationsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private ListAdapter adapter;
  private AnimatorSet animatorSet;
  private int callsRow;
  private int callsVibrateRow;
  private int colorRow;
  private boolean customEnabled;
  private int customInfoRow;
  private int customRow;
  private long dialog_id;
  private int generalRow;
  private int ledInfoRow;
  private int ledRow;
  private RecyclerListView listView;
  private boolean notificationsEnabled;
  private int popupDisabledRow;
  private int popupEnabledRow;
  private int popupInfoRow;
  private int popupRow;
  private int priorityInfoRow;
  private int priorityRow;
  private int ringtoneInfoRow;
  private int ringtoneRow;
  private int rowCount;
  private int smartRow;
  private int soundRow;
  private int vibrateRow;
  
  public ProfileNotificationsActivity(Bundle paramBundle)
  {
    super(paramBundle);
    this.dialog_id = paramBundle.getLong("dialog_id");
  }
  
  public View createView(final Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("CustomNotifications", 2131493325));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1)
        {
          if ((ProfileNotificationsActivity.this.notificationsEnabled) && (ProfileNotificationsActivity.this.customEnabled)) {
            MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount).edit().putInt("notify2_" + ProfileNotificationsActivity.this.dialog_id, 0).commit();
          }
          ProfileNotificationsActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    Object localObject = (FrameLayout)this.fragmentView;
    ((FrameLayout)localObject).setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.listView = new RecyclerListView(paramContext);
    ((FrameLayout)localObject).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    localObject = this.listView;
    ListAdapter localListAdapter = new ListAdapter(paramContext);
    this.adapter = localListAdapter;
    ((RecyclerListView)localObject).setAdapter(localListAdapter);
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    });
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(final View paramAnonymousView, int paramAnonymousInt)
      {
        Object localObject;
        ProfileNotificationsActivity localProfileNotificationsActivity;
        int i;
        label157:
        int j;
        if ((paramAnonymousInt == ProfileNotificationsActivity.this.customRow) && ((paramAnonymousView instanceof TextCheckBoxCell)))
        {
          localObject = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
          localProfileNotificationsActivity = ProfileNotificationsActivity.this;
          boolean bool;
          if (!ProfileNotificationsActivity.this.customEnabled)
          {
            bool = true;
            ProfileNotificationsActivity.access$102(localProfileNotificationsActivity, bool);
            ProfileNotificationsActivity.access$002(ProfileNotificationsActivity.this, ProfileNotificationsActivity.this.customEnabled);
            ((SharedPreferences)localObject).edit().putBoolean("custom_" + ProfileNotificationsActivity.this.dialog_id, ProfileNotificationsActivity.this.customEnabled).commit();
            ((TextCheckBoxCell)paramAnonymousView).setChecked(ProfileNotificationsActivity.this.customEnabled);
            i = ProfileNotificationsActivity.this.listView.getChildCount();
            paramAnonymousView = new ArrayList();
            paramAnonymousInt = 0;
            if (paramAnonymousInt >= i) {
              break label353;
            }
            localObject = ProfileNotificationsActivity.this.listView.getChildAt(paramAnonymousInt);
            localObject = (RecyclerListView.Holder)ProfileNotificationsActivity.this.listView.getChildViewHolder((View)localObject);
            j = ((RecyclerListView.Holder)localObject).getItemViewType();
            if ((((RecyclerListView.Holder)localObject).getAdapterPosition() != ProfileNotificationsActivity.this.customRow) && (j != 0)) {
              switch (j)
              {
              }
            }
          }
          for (;;)
          {
            paramAnonymousInt += 1;
            break label157;
            bool = false;
            break;
            ((TextSettingsCell)((RecyclerListView.Holder)localObject).itemView).setEnabled(ProfileNotificationsActivity.this.customEnabled, paramAnonymousView);
            continue;
            ((TextInfoPrivacyCell)((RecyclerListView.Holder)localObject).itemView).setEnabled(ProfileNotificationsActivity.this.customEnabled, paramAnonymousView);
            continue;
            ((TextColorCell)((RecyclerListView.Holder)localObject).itemView).setEnabled(ProfileNotificationsActivity.this.customEnabled, paramAnonymousView);
            continue;
            ((RadioCell)((RecyclerListView.Holder)localObject).itemView).setEnabled(ProfileNotificationsActivity.this.customEnabled, paramAnonymousView);
          }
          label353:
          if (!paramAnonymousView.isEmpty())
          {
            if (ProfileNotificationsActivity.this.animatorSet != null) {
              ProfileNotificationsActivity.this.animatorSet.cancel();
            }
            ProfileNotificationsActivity.access$702(ProfileNotificationsActivity.this, new AnimatorSet());
            ProfileNotificationsActivity.this.animatorSet.playTogether(paramAnonymousView);
            ProfileNotificationsActivity.this.animatorSet.addListener(new AnimatorListenerAdapter()
            {
              public void onAnimationEnd(Animator paramAnonymous2Animator)
              {
                if (paramAnonymous2Animator.equals(ProfileNotificationsActivity.this.animatorSet)) {
                  ProfileNotificationsActivity.access$702(ProfileNotificationsActivity.this, null);
                }
              }
            });
            ProfileNotificationsActivity.this.animatorSet.setDuration(150L);
            ProfileNotificationsActivity.this.animatorSet.start();
          }
        }
        label448:
        label642:
        label835:
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
                  break label448;
                  break label448;
                  break label448;
                  do
                  {
                    return;
                  } while (!ProfileNotificationsActivity.this.customEnabled);
                  Intent localIntent;
                  Uri localUri;
                  String str;
                  if (paramAnonymousInt == ProfileNotificationsActivity.this.soundRow) {
                    for (;;)
                    {
                      try
                      {
                        localIntent = new Intent("android.intent.action.RINGTONE_PICKER");
                        localIntent.putExtra("android.intent.extra.ringtone.TYPE", 2);
                        localIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", true);
                        localIntent.putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(2));
                        paramAnonymousView = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
                        localProfileNotificationsActivity = null;
                        localObject = null;
                        localUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                        if (localUri != null) {
                          localObject = localUri.getPath();
                        }
                        str = paramAnonymousView.getString("sound_path_" + ProfileNotificationsActivity.this.dialog_id, (String)localObject);
                        paramAnonymousView = localProfileNotificationsActivity;
                        if (str != null)
                        {
                          paramAnonymousView = localProfileNotificationsActivity;
                          if (!str.equals("NoSound"))
                          {
                            if (!str.equals(localObject)) {
                              break label642;
                            }
                            paramAnonymousView = localUri;
                          }
                        }
                        localIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", paramAnonymousView);
                        ProfileNotificationsActivity.this.startActivityForResult(localIntent, 12);
                        return;
                      }
                      catch (Exception paramAnonymousView)
                      {
                        FileLog.e(paramAnonymousView);
                        return;
                      }
                      paramAnonymousView = Uri.parse(str);
                    }
                  }
                  if (paramAnonymousInt == ProfileNotificationsActivity.this.ringtoneRow) {
                    for (;;)
                    {
                      try
                      {
                        localIntent = new Intent("android.intent.action.RINGTONE_PICKER");
                        localIntent.putExtra("android.intent.extra.ringtone.TYPE", 1);
                        localIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", true);
                        localIntent.putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(1));
                        paramAnonymousView = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
                        localProfileNotificationsActivity = null;
                        localObject = null;
                        localUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                        if (localUri != null) {
                          localObject = localUri.getPath();
                        }
                        str = paramAnonymousView.getString("ringtone_path_" + ProfileNotificationsActivity.this.dialog_id, (String)localObject);
                        paramAnonymousView = localProfileNotificationsActivity;
                        if (str != null)
                        {
                          paramAnonymousView = localProfileNotificationsActivity;
                          if (!str.equals("NoSound"))
                          {
                            if (!str.equals(localObject)) {
                              break label835;
                            }
                            paramAnonymousView = localUri;
                          }
                        }
                        localIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", paramAnonymousView);
                        ProfileNotificationsActivity.this.startActivityForResult(localIntent, 13);
                        return;
                      }
                      catch (Exception paramAnonymousView)
                      {
                        FileLog.e(paramAnonymousView);
                        return;
                      }
                      paramAnonymousView = Uri.parse(str);
                    }
                  }
                  if (paramAnonymousInt == ProfileNotificationsActivity.this.vibrateRow)
                  {
                    ProfileNotificationsActivity.this.showDialog(AlertsCreator.createVibrationSelectDialog(ProfileNotificationsActivity.this.getParentActivity(), ProfileNotificationsActivity.this, ProfileNotificationsActivity.this.dialog_id, false, false, new Runnable()
                    {
                      public void run()
                      {
                        if (ProfileNotificationsActivity.this.adapter != null) {
                          ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.vibrateRow);
                        }
                      }
                    }));
                    return;
                  }
                  if (paramAnonymousInt == ProfileNotificationsActivity.this.callsVibrateRow)
                  {
                    ProfileNotificationsActivity.this.showDialog(AlertsCreator.createVibrationSelectDialog(ProfileNotificationsActivity.this.getParentActivity(), ProfileNotificationsActivity.this, ProfileNotificationsActivity.this.dialog_id, "calls_vibrate_", new Runnable()
                    {
                      public void run()
                      {
                        if (ProfileNotificationsActivity.this.adapter != null) {
                          ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.callsVibrateRow);
                        }
                      }
                    }));
                    return;
                  }
                  if (paramAnonymousInt == ProfileNotificationsActivity.this.priorityRow)
                  {
                    ProfileNotificationsActivity.this.showDialog(AlertsCreator.createPrioritySelectDialog(ProfileNotificationsActivity.this.getParentActivity(), ProfileNotificationsActivity.this, ProfileNotificationsActivity.this.dialog_id, false, false, new Runnable()
                    {
                      public void run()
                      {
                        if (ProfileNotificationsActivity.this.adapter != null) {
                          ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.priorityRow);
                        }
                      }
                    }));
                    return;
                  }
                  if (paramAnonymousInt != ProfileNotificationsActivity.this.smartRow) {
                    break;
                  }
                } while (ProfileNotificationsActivity.this.getParentActivity() == null);
                paramAnonymousView = ProfileNotificationsActivity.this.getParentActivity();
                localObject = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
                i = ((SharedPreferences)localObject).getInt("smart_max_count_" + ProfileNotificationsActivity.this.dialog_id, 2);
                j = ((SharedPreferences)localObject).getInt("smart_delay_" + ProfileNotificationsActivity.this.dialog_id, 180);
                paramAnonymousInt = i;
                if (i == 0) {
                  paramAnonymousInt = 2;
                }
                i = j / 60;
                localObject = new RecyclerListView(ProfileNotificationsActivity.this.getParentActivity());
                ((RecyclerListView)localObject).setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
                ((RecyclerListView)localObject).setClipToPadding(true);
                ((RecyclerListView)localObject).setAdapter(new RecyclerListView.SelectionAdapter()
                {
                  public int getItemCount()
                  {
                    return 100;
                  }
                  
                  public boolean isEnabled(RecyclerView.ViewHolder paramAnonymous2ViewHolder)
                  {
                    return true;
                  }
                  
                  public void onBindViewHolder(RecyclerView.ViewHolder paramAnonymous2ViewHolder, int paramAnonymous2Int)
                  {
                    TextView localTextView = (TextView)paramAnonymous2ViewHolder.itemView;
                    if (paramAnonymous2Int == this.val$selected) {}
                    for (paramAnonymous2ViewHolder = "dialogTextGray";; paramAnonymous2ViewHolder = "dialogTextBlack")
                    {
                      localTextView.setTextColor(Theme.getColor(paramAnonymous2ViewHolder));
                      int i = paramAnonymous2Int / 10;
                      localTextView.setText(LocaleController.formatString("SmartNotificationsDetail", 2131494411, new Object[] { LocaleController.formatPluralString("Times", paramAnonymous2Int % 10 + 1), LocaleController.formatPluralString("Minutes", i + 1) }));
                      return;
                    }
                  }
                  
                  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramAnonymous2ViewGroup, int paramAnonymous2Int)
                  {
                    paramAnonymous2ViewGroup = new TextView(paramAnonymousView)
                    {
                      protected void onMeasure(int paramAnonymous3Int1, int paramAnonymous3Int2)
                      {
                        super.onMeasure(View.MeasureSpec.makeMeasureSpec(paramAnonymous3Int1, 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
                      }
                    };
                    TextView localTextView = (TextView)paramAnonymous2ViewGroup;
                    localTextView.setGravity(17);
                    localTextView.setTextSize(1, 18.0F);
                    localTextView.setSingleLine(true);
                    localTextView.setEllipsize(TextUtils.TruncateAt.END);
                    localTextView.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
                    return new RecyclerListView.Holder(paramAnonymous2ViewGroup);
                  }
                });
                ((RecyclerListView)localObject).setPadding(0, AndroidUtilities.dp(12.0F), 0, AndroidUtilities.dp(8.0F));
                ((RecyclerListView)localObject).setOnItemClickListener(new RecyclerListView.OnItemClickListener()
                {
                  public void onItemClick(View paramAnonymous2View, int paramAnonymous2Int)
                  {
                    if ((paramAnonymous2Int < 0) || (paramAnonymous2Int >= 100)) {
                      return;
                    }
                    int i = paramAnonymous2Int / 10;
                    paramAnonymous2View = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
                    paramAnonymous2View.edit().putInt("smart_max_count_" + ProfileNotificationsActivity.this.dialog_id, paramAnonymous2Int % 10 + 1).commit();
                    paramAnonymous2View.edit().putInt("smart_delay_" + ProfileNotificationsActivity.this.dialog_id, (i + 1) * 60).commit();
                    if (ProfileNotificationsActivity.this.adapter != null) {
                      ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.smartRow);
                    }
                    ProfileNotificationsActivity.this.dismissCurrentDialig();
                  }
                });
                paramAnonymousView = new AlertDialog.Builder(ProfileNotificationsActivity.this.getParentActivity());
                paramAnonymousView.setTitle(LocaleController.getString("SmartNotificationsAlert", 2131494410));
                paramAnonymousView.setView((View)localObject);
                paramAnonymousView.setPositiveButton(LocaleController.getString("Cancel", 2131493127), null);
                paramAnonymousView.setNegativeButton(LocaleController.getString("SmartNotificationsDisabled", 2131494412), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                  {
                    MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount).edit().putInt("smart_max_count_" + ProfileNotificationsActivity.this.dialog_id, 0).commit();
                    if (ProfileNotificationsActivity.this.adapter != null) {
                      ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.smartRow);
                    }
                    ProfileNotificationsActivity.this.dismissCurrentDialig();
                  }
                });
                ProfileNotificationsActivity.this.showDialog(paramAnonymousView.create());
                return;
                if (paramAnonymousInt != ProfileNotificationsActivity.this.colorRow) {
                  break;
                }
              } while (ProfileNotificationsActivity.this.getParentActivity() == null);
              ProfileNotificationsActivity.this.showDialog(AlertsCreator.createColorSelectDialog(ProfileNotificationsActivity.this.getParentActivity(), ProfileNotificationsActivity.this.dialog_id, false, false, new Runnable()
              {
                public void run()
                {
                  if (ProfileNotificationsActivity.this.adapter != null) {
                    ProfileNotificationsActivity.this.adapter.notifyItemChanged(ProfileNotificationsActivity.this.colorRow);
                  }
                }
              }));
              return;
              if (paramAnonymousInt != ProfileNotificationsActivity.this.popupEnabledRow) {
                break;
              }
              MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount).edit().putInt("popup_" + ProfileNotificationsActivity.this.dialog_id, 1).commit();
              ((RadioCell)paramAnonymousView).setChecked(true, true);
              paramAnonymousView = ProfileNotificationsActivity.this.listView.findViewWithTag(Integer.valueOf(2));
            } while (paramAnonymousView == null);
            ((RadioCell)paramAnonymousView).setChecked(false, true);
            return;
          } while (paramAnonymousInt != ProfileNotificationsActivity.this.popupDisabledRow);
          MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount).edit().putInt("popup_" + ProfileNotificationsActivity.this.dialog_id, 2).commit();
          ((RadioCell)paramAnonymousView).setChecked(true, true);
          paramAnonymousView = ProfileNotificationsActivity.this.listView.findViewWithTag(Integer.valueOf(1));
        } while (paramAnonymousView == null);
        ((RadioCell)paramAnonymousView).setChecked(false, true);
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
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { HeaderCell.class, TextSettingsCell.class, TextColorCell.class, RadioCell.class, TextCheckBoxCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.listView, 0, new Class[] { TextColorCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { RadioCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckBoxCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckBoxCell.class }, null, null, null, "checkboxSquareUnchecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckBoxCell.class }, null, null, null, "checkboxSquareDisabled"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckBoxCell.class }, null, null, null, "checkboxSquareBackground"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckBoxCell.class }, null, null, null, "checkboxSquareCheck") };
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    if ((paramInt2 != -1) || (paramIntent == null)) {}
    Uri localUri;
    SharedPreferences.Editor localEditor;
    Ringtone localRingtone;
    label175:
    do
    {
      return;
      localUri = (Uri)paramIntent.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
      localEditor = null;
      paramIntent = localEditor;
      if (localUri != null)
      {
        localRingtone = RingtoneManager.getRingtone(ApplicationLoader.applicationContext, localUri);
        paramIntent = localEditor;
        if (localRingtone != null)
        {
          if (paramInt1 != 13) {
            break label225;
          }
          if (!localUri.equals(Settings.System.DEFAULT_RINGTONE_URI)) {
            break;
          }
          paramIntent = LocaleController.getString("DefaultRingtone", 2131493355);
          localRingtone.stop();
        }
      }
      localEditor = MessagesController.getNotificationsSettings(this.currentAccount).edit();
      if (paramInt1 != 12) {
        break label333;
      }
      if (paramIntent == null) {
        break label262;
      }
      localEditor.putString("sound_" + this.dialog_id, paramIntent);
      localEditor.putString("sound_path_" + this.dialog_id, localUri.toString());
      localEditor.commit();
    } while (this.adapter == null);
    paramIntent = this.adapter;
    if (paramInt1 == 13) {}
    for (paramInt1 = this.ringtoneRow;; paramInt1 = this.soundRow)
    {
      paramIntent.notifyItemChanged(paramInt1);
      return;
      paramIntent = localRingtone.getTitle(getParentActivity());
      break;
      label225:
      if (localUri.equals(Settings.System.DEFAULT_NOTIFICATION_URI))
      {
        paramIntent = LocaleController.getString("SoundDefault", 2131494420);
        break;
      }
      paramIntent = localRingtone.getTitle(getParentActivity());
      break;
      label262:
      localEditor.putString("sound_" + this.dialog_id, "NoSound");
      localEditor.putString("sound_path_" + this.dialog_id, "NoSound");
      break label175;
      label333:
      if (paramInt1 != 13) {
        break label175;
      }
      if (paramIntent != null)
      {
        localEditor.putString("ringtone_" + this.dialog_id, paramIntent);
        localEditor.putString("ringtone_path_" + this.dialog_id, localUri.toString());
        break label175;
      }
      localEditor.putString("ringtone_" + this.dialog_id, "NoSound");
      localEditor.putString("ringtone_path_" + this.dialog_id, "NoSound");
      break label175;
    }
  }
  
  public boolean onFragmentCreate()
  {
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.customRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.customInfoRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.generalRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.soundRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.vibrateRow = i;
    label140:
    Object localObject;
    if ((int)this.dialog_id < 0)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.smartRow = i;
      if (Build.VERSION.SDK_INT < 21) {
        break label553;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.priorityRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.priorityInfoRow = i;
      int j = (int)this.dialog_id;
      if (j >= 0) {
        break label566;
      }
      localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-j));
      if ((localObject == null) || (!ChatObject.isChannel((TLRPC.Chat)localObject)) || (((TLRPC.Chat)localObject).megagroup)) {
        break label561;
      }
      i = 1;
      label207:
      if ((j == 0) || (i != 0)) {
        break label571;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.popupRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.popupEnabledRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.popupDisabledRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.popupInfoRow = i;
      label283:
      if (j <= 0) {
        break label594;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsVibrateRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.ringtoneRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.ringtoneInfoRow = i;
      label355:
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.ledRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.colorRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.ledInfoRow = i;
      localObject = MessagesController.getNotificationsSettings(this.currentAccount);
      this.customEnabled = ((SharedPreferences)localObject).getBoolean("custom_" + this.dialog_id, false);
      boolean bool = ((SharedPreferences)localObject).contains("notify2_" + this.dialog_id);
      i = ((SharedPreferences)localObject).getInt("notify2_" + this.dialog_id, 0);
      if (i != 0) {
        break label661;
      }
      if (!bool) {
        break label617;
      }
      this.notificationsEnabled = true;
    }
    for (;;)
    {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.notificationsSettingsUpdated);
      return super.onFragmentCreate();
      this.smartRow = -1;
      break;
      label553:
      this.priorityRow = -1;
      break label140;
      label561:
      i = 0;
      break label207;
      label566:
      i = 0;
      break label207;
      label571:
      this.popupRow = -1;
      this.popupEnabledRow = -1;
      this.popupDisabledRow = -1;
      this.popupInfoRow = -1;
      break label283;
      label594:
      this.callsRow = -1;
      this.callsVibrateRow = -1;
      this.ringtoneRow = -1;
      this.ringtoneInfoRow = -1;
      break label355;
      label617:
      if ((int)this.dialog_id < 0)
      {
        this.notificationsEnabled = ((SharedPreferences)localObject).getBoolean("EnableGroup", true);
      }
      else
      {
        this.notificationsEnabled = ((SharedPreferences)localObject).getBoolean("EnableAll", true);
        continue;
        label661:
        if (i == 1) {
          this.notificationsEnabled = true;
        } else if (i == 2) {
          this.notificationsEnabled = false;
        } else {
          this.notificationsEnabled = false;
        }
      }
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
  }
  
  private class ListAdapter
    extends RecyclerView.Adapter
  {
    private Context context;
    
    public ListAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public int getItemCount()
    {
      return ProfileNotificationsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == ProfileNotificationsActivity.this.generalRow) || (paramInt == ProfileNotificationsActivity.this.popupRow) || (paramInt == ProfileNotificationsActivity.this.ledRow) || (paramInt == ProfileNotificationsActivity.this.callsRow)) {}
      do
      {
        return 0;
        if ((paramInt == ProfileNotificationsActivity.this.soundRow) || (paramInt == ProfileNotificationsActivity.this.vibrateRow) || (paramInt == ProfileNotificationsActivity.this.priorityRow) || (paramInt == ProfileNotificationsActivity.this.smartRow) || (paramInt == ProfileNotificationsActivity.this.ringtoneRow) || (paramInt == ProfileNotificationsActivity.this.callsVibrateRow)) {
          return 1;
        }
        if ((paramInt == ProfileNotificationsActivity.this.popupInfoRow) || (paramInt == ProfileNotificationsActivity.this.ledInfoRow) || (paramInt == ProfileNotificationsActivity.this.priorityInfoRow) || (paramInt == ProfileNotificationsActivity.this.customInfoRow) || (paramInt == ProfileNotificationsActivity.this.ringtoneInfoRow)) {
          return 2;
        }
        if (paramInt == ProfileNotificationsActivity.this.colorRow) {
          return 3;
        }
        if ((paramInt == ProfileNotificationsActivity.this.popupEnabledRow) || (paramInt == ProfileNotificationsActivity.this.popupDisabledRow)) {
          return 4;
        }
      } while (paramInt != ProfileNotificationsActivity.this.customRow);
      return 5;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      default: 
      case 0: 
      case 1: 
      case 2: 
      case 3: 
      case 4: 
        int i;
        label1694:
        label1749:
        label1754:
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
                  do
                  {
                    do
                    {
                      return;
                      paramViewHolder = (HeaderCell)paramViewHolder.itemView;
                      if (paramInt == ProfileNotificationsActivity.this.generalRow)
                      {
                        paramViewHolder.setText(LocaleController.getString("General", 2131493625));
                        return;
                      }
                      if (paramInt == ProfileNotificationsActivity.this.popupRow)
                      {
                        paramViewHolder.setText(LocaleController.getString("ProfilePopupNotification", 2131494204));
                        return;
                      }
                      if (paramInt == ProfileNotificationsActivity.this.ledRow)
                      {
                        paramViewHolder.setText(LocaleController.getString("NotificationsLed", 2131494010));
                        return;
                      }
                    } while (paramInt != ProfileNotificationsActivity.this.callsRow);
                    paramViewHolder.setText(LocaleController.getString("VoipNotificationSettings", 2131494601));
                    return;
                    localObject2 = (TextSettingsCell)paramViewHolder.itemView;
                    paramViewHolder = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
                    if (paramInt == ProfileNotificationsActivity.this.soundRow)
                    {
                      localObject1 = paramViewHolder.getString("sound_" + ProfileNotificationsActivity.this.dialog_id, LocaleController.getString("SoundDefault", 2131494420));
                      paramViewHolder = (RecyclerView.ViewHolder)localObject1;
                      if (((String)localObject1).equals("NoSound")) {
                        paramViewHolder = LocaleController.getString("NoSound", 2131493913);
                      }
                      ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("Sound", 2131494419), paramViewHolder, true);
                      return;
                    }
                    if (paramInt == ProfileNotificationsActivity.this.ringtoneRow)
                    {
                      localObject1 = paramViewHolder.getString("ringtone_" + ProfileNotificationsActivity.this.dialog_id, LocaleController.getString("DefaultRingtone", 2131493355));
                      paramViewHolder = (RecyclerView.ViewHolder)localObject1;
                      if (((String)localObject1).equals("NoSound")) {
                        paramViewHolder = LocaleController.getString("NoSound", 2131493913);
                      }
                      ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("VoipSettingsRingtone", 2131494618), paramViewHolder, false);
                      return;
                    }
                    if (paramInt != ProfileNotificationsActivity.this.vibrateRow) {
                      break;
                    }
                    paramInt = paramViewHolder.getInt("vibrate_" + ProfileNotificationsActivity.this.dialog_id, 0);
                    if ((paramInt == 0) || (paramInt == 4))
                    {
                      paramViewHolder = LocaleController.getString("Vibrate", 2131494574);
                      localObject1 = LocaleController.getString("VibrationDefault", 2131494575);
                      if ((ProfileNotificationsActivity.this.smartRow != -1) || (ProfileNotificationsActivity.this.priorityRow != -1)) {}
                      for (bool = true;; bool = false)
                      {
                        ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                        return;
                      }
                    }
                    if (paramInt == 1)
                    {
                      paramViewHolder = LocaleController.getString("Vibrate", 2131494574);
                      localObject1 = LocaleController.getString("Short", 2131494401);
                      if ((ProfileNotificationsActivity.this.smartRow != -1) || (ProfileNotificationsActivity.this.priorityRow != -1)) {}
                      for (bool = true;; bool = false)
                      {
                        ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                        return;
                      }
                    }
                    if (paramInt == 2)
                    {
                      paramViewHolder = LocaleController.getString("Vibrate", 2131494574);
                      localObject1 = LocaleController.getString("VibrationDisabled", 2131494576);
                      if ((ProfileNotificationsActivity.this.smartRow != -1) || (ProfileNotificationsActivity.this.priorityRow != -1)) {}
                      for (bool = true;; bool = false)
                      {
                        ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                        return;
                      }
                    }
                  } while (paramInt != 3);
                  paramViewHolder = LocaleController.getString("Vibrate", 2131494574);
                  localObject1 = LocaleController.getString("Long", 2131493779);
                  if ((ProfileNotificationsActivity.this.smartRow != -1) || (ProfileNotificationsActivity.this.priorityRow != -1)) {}
                  for (bool = true;; bool = false)
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                    return;
                  }
                  if (paramInt != ProfileNotificationsActivity.this.priorityRow) {
                    break;
                  }
                  paramInt = paramViewHolder.getInt("priority_" + ProfileNotificationsActivity.this.dialog_id, 3);
                  if (paramInt == 0)
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityHigh", 2131494016), false);
                    return;
                  }
                  if ((paramInt == 1) || (paramInt == 2))
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityUrgent", 2131494020), false);
                    return;
                  }
                  if (paramInt == 3)
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPrioritySettings", 2131494019), false);
                    return;
                  }
                  if (paramInt == 4)
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityLow", 2131494017), false);
                    return;
                  }
                } while (paramInt != 5);
                ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("NotificationsImportance", 2131494009), LocaleController.getString("NotificationsPriorityMedium", 2131494018), false);
                return;
                if (paramInt == ProfileNotificationsActivity.this.smartRow)
                {
                  paramInt = paramViewHolder.getInt("smart_max_count_" + ProfileNotificationsActivity.this.dialog_id, 2);
                  i = paramViewHolder.getInt("smart_delay_" + ProfileNotificationsActivity.this.dialog_id, 180);
                  if (paramInt == 0)
                  {
                    paramViewHolder = LocaleController.getString("SmartNotifications", 2131494409);
                    localObject1 = LocaleController.getString("SmartNotificationsDisabled", 2131494412);
                    if (ProfileNotificationsActivity.this.priorityRow != -1) {}
                    for (bool = true;; bool = false)
                    {
                      ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                      return;
                    }
                  }
                  localObject1 = LocaleController.formatPluralString("Minutes", i / 60);
                  paramViewHolder = LocaleController.getString("SmartNotifications", 2131494409);
                  localObject1 = LocaleController.formatString("SmartNotificationsInfo", 2131494413, new Object[] { Integer.valueOf(paramInt), localObject1 });
                  if (ProfileNotificationsActivity.this.priorityRow != -1) {}
                  for (bool = true;; bool = false)
                  {
                    ((TextSettingsCell)localObject2).setTextAndValue(paramViewHolder, (String)localObject1, bool);
                    return;
                  }
                }
              } while (paramInt != ProfileNotificationsActivity.this.callsVibrateRow);
              paramInt = paramViewHolder.getInt("calls_vibrate_" + ProfileNotificationsActivity.this.dialog_id, 0);
              if ((paramInt == 0) || (paramInt == 4))
              {
                ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("VibrationDefault", 2131494575), true);
                return;
              }
              if (paramInt == 1)
              {
                ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("Short", 2131494401), true);
                return;
              }
              if (paramInt == 2)
              {
                ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("VibrationDisabled", 2131494576), true);
                return;
              }
            } while (paramInt != 3);
            ((TextSettingsCell)localObject2).setTextAndValue(LocaleController.getString("Vibrate", 2131494574), LocaleController.getString("Long", 2131493779), true);
            return;
            paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
            if (paramInt == ProfileNotificationsActivity.this.popupInfoRow)
            {
              paramViewHolder.setText(LocaleController.getString("ProfilePopupNotificationInfo", 2131494205));
              paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.context, 2131165331, "windowBackgroundGrayShadow"));
              return;
            }
            if (paramInt == ProfileNotificationsActivity.this.ledInfoRow)
            {
              paramViewHolder.setText(LocaleController.getString("NotificationsLedInfo", 2131494012));
              paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.context, 2131165332, "windowBackgroundGrayShadow"));
              return;
            }
            if (paramInt == ProfileNotificationsActivity.this.priorityInfoRow)
            {
              if (ProfileNotificationsActivity.this.priorityRow == -1) {
                paramViewHolder.setText("");
              }
              for (;;)
              {
                paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.context, 2131165331, "windowBackgroundGrayShadow"));
                return;
                paramViewHolder.setText(LocaleController.getString("PriorityInfo", 2131494189));
              }
            }
            if (paramInt == ProfileNotificationsActivity.this.customInfoRow)
            {
              paramViewHolder.setText(null);
              paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.context, 2131165331, "windowBackgroundGrayShadow"));
              return;
            }
          } while (paramInt != ProfileNotificationsActivity.this.ringtoneInfoRow);
          paramViewHolder.setText(LocaleController.getString("VoipRingtoneInfo", 2131494617));
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.context, 2131165331, "windowBackgroundGrayShadow"));
          return;
          paramViewHolder = (TextColorCell)paramViewHolder.itemView;
          localObject1 = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
          if (((SharedPreferences)localObject1).contains("color_" + ProfileNotificationsActivity.this.dialog_id))
          {
            paramInt = ((SharedPreferences)localObject1).getInt("color_" + ProfileNotificationsActivity.this.dialog_id, -16776961);
            i = 0;
          }
          for (;;)
          {
            j = paramInt;
            if (i < 9)
            {
              if (TextColorCell.colorsToSave[i] == paramInt) {
                j = TextColorCell.colors[i];
              }
            }
            else
            {
              paramViewHolder.setTextAndColor(LocaleController.getString("NotificationsLedColor", 2131494011), j, false);
              return;
              if ((int)ProfileNotificationsActivity.this.dialog_id < 0)
              {
                paramInt = ((SharedPreferences)localObject1).getInt("GroupLed", -16776961);
                break;
              }
              paramInt = ((SharedPreferences)localObject1).getInt("MessagesLed", -16776961);
              break;
            }
            i += 1;
          }
          localObject1 = (RadioCell)paramViewHolder.itemView;
          Object localObject2 = MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
          int j = ((SharedPreferences)localObject2).getInt("popup_" + ProfileNotificationsActivity.this.dialog_id, 0);
          i = j;
          if (j == 0)
          {
            if ((int)ProfileNotificationsActivity.this.dialog_id < 0)
            {
              paramViewHolder = "popupGroup";
              if (((SharedPreferences)localObject2).getInt(paramViewHolder, 0) == 0) {
                break label1749;
              }
              i = 1;
            }
          }
          else
          {
            if (paramInt != ProfileNotificationsActivity.this.popupEnabledRow) {
              continue;
            }
            paramViewHolder = LocaleController.getString("PopupEnabled", 2131494186);
            if (i != 1) {
              break label1754;
            }
          }
          for (bool = true;; bool = false)
          {
            ((RadioCell)localObject1).setText(paramViewHolder, bool, true);
            ((RadioCell)localObject1).setTag(Integer.valueOf(1));
            return;
            paramViewHolder = "popupAll";
            break;
            i = 2;
            break label1694;
          }
        } while (paramInt != ProfileNotificationsActivity.this.popupDisabledRow);
        paramViewHolder = LocaleController.getString("PopupDisabled", 2131494185);
        if (i == 2) {}
        for (bool = true;; bool = false)
        {
          ((RadioCell)localObject1).setText(paramViewHolder, bool, false);
          ((RadioCell)localObject1).setTag(Integer.valueOf(2));
          return;
        }
      }
      paramViewHolder = (TextCheckBoxCell)paramViewHolder.itemView;
      MessagesController.getNotificationsSettings(ProfileNotificationsActivity.this.currentAccount);
      Object localObject1 = LocaleController.getString("NotificationsEnableCustom", 2131494008);
      if ((ProfileNotificationsActivity.this.customEnabled) && (ProfileNotificationsActivity.this.notificationsEnabled)) {}
      for (boolean bool = true;; bool = false)
      {
        paramViewHolder.setTextAndCheck((String)localObject1, bool, false);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextCheckBoxCell(this.context);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new HeaderCell(this.context);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextSettingsCell(this.context);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.context);
        continue;
        paramViewGroup = new TextColorCell(this.context);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new RadioCell(this.context);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
    
    public void onViewAttachedToWindow(RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool2 = true;
      boolean bool3 = true;
      boolean bool4 = true;
      boolean bool1 = true;
      if (paramViewHolder.getItemViewType() != 0) {}
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      case 1: 
        paramViewHolder = (TextSettingsCell)paramViewHolder.itemView;
        if ((ProfileNotificationsActivity.this.customEnabled) && (ProfileNotificationsActivity.this.notificationsEnabled)) {}
        for (;;)
        {
          paramViewHolder.setEnabled(bool1, null);
          return;
          bool1 = false;
        }
      case 2: 
        paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
        if ((ProfileNotificationsActivity.this.customEnabled) && (ProfileNotificationsActivity.this.notificationsEnabled)) {}
        for (bool1 = bool2;; bool1 = false)
        {
          paramViewHolder.setEnabled(bool1, null);
          return;
        }
      case 3: 
        paramViewHolder = (TextColorCell)paramViewHolder.itemView;
        if ((ProfileNotificationsActivity.this.customEnabled) && (ProfileNotificationsActivity.this.notificationsEnabled)) {}
        for (bool1 = bool3;; bool1 = false)
        {
          paramViewHolder.setEnabled(bool1, null);
          return;
        }
      }
      paramViewHolder = (RadioCell)paramViewHolder.itemView;
      if ((ProfileNotificationsActivity.this.customEnabled) && (ProfileNotificationsActivity.this.notificationsEnabled)) {}
      for (bool1 = bool4;; bool1 = false)
      {
        paramViewHolder.setEnabled(bool1, null);
        return;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ProfileNotificationsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */