package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.DefaultItemAnimator;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.time.SunDate;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.BrightnessControlCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.ThemeCell;
import org.telegram.ui.Cells.ThemeTypeCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.ThemeEditorView;

public class ThemeActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  public static final int THEME_TYPE_BASIC = 0;
  public static final int THEME_TYPE_NIGHT = 1;
  private int automaticBrightnessInfoRow;
  private int automaticBrightnessRow;
  private int automaticHeaderRow;
  private int currentType;
  private GpsLocationListener gpsLocationListener = new GpsLocationListener(null);
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private GpsLocationListener networkLocationListener = new GpsLocationListener(null);
  private int newThemeInfoRow;
  private int newThemeRow;
  private int nightAutomaticRow;
  private int nightDisabledRow;
  private int nightScheduledRow;
  private int nightThemeRow;
  private int nightTypeInfoRow;
  private int preferedHeaderRow;
  private boolean previousByLocation;
  private int previousUpdatedType;
  private int rowCount;
  private int scheduleFromRow;
  private int scheduleFromToInfoRow;
  private int scheduleHeaderRow;
  private int scheduleLocationInfoRow;
  private int scheduleLocationRow;
  private int scheduleToRow;
  private int scheduleUpdateLocationRow;
  private int themeEndRow;
  private int themeInfoRow;
  private int themeStartRow;
  private boolean updatingLocation;
  
  public ThemeActivity(int paramInt)
  {
    this.currentType = paramInt;
    updateRows();
  }
  
  private String getLocationSunString()
  {
    int i = Theme.autoNightSunriseTime / 60;
    String str = String.format("%02d:%02d", new Object[] { Integer.valueOf(i), Integer.valueOf(Theme.autoNightSunriseTime - i * 60) });
    i = Theme.autoNightSunsetTime / 60;
    return LocaleController.formatString("AutoNightUpdateLocationInfo", 2131493066, new Object[] { String.format("%02d:%02d", new Object[] { Integer.valueOf(i), Integer.valueOf(Theme.autoNightSunsetTime - i * 60) }), str });
  }
  
  private void showPermissionAlert(boolean paramBoolean)
  {
    if (getParentActivity() == null) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    if (paramBoolean) {
      localBuilder.setMessage(LocaleController.getString("PermissionNoLocationPosition", 2131494146));
    }
    for (;;)
    {
      localBuilder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", 2131494147), new DialogInterface.OnClickListener()
      {
        @TargetApi(9)
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          if (ThemeActivity.this.getParentActivity() == null) {
            return;
          }
          try
          {
            paramAnonymousDialogInterface = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            paramAnonymousDialogInterface.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
            ThemeActivity.this.getParentActivity().startActivity(paramAnonymousDialogInterface);
            return;
          }
          catch (Exception paramAnonymousDialogInterface)
          {
            FileLog.e(paramAnonymousDialogInterface);
          }
        }
      });
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      showDialog(localBuilder.create());
      return;
      localBuilder.setMessage(LocaleController.getString("PermissionNoLocation", 2131494145));
    }
  }
  
  private void startLocationUpdate()
  {
    if (this.updatingLocation) {
      return;
    }
    this.updatingLocation = true;
    LocationManager localLocationManager = (LocationManager)ApplicationLoader.applicationContext.getSystemService("location");
    try
    {
      localLocationManager.requestLocationUpdates("gps", 1L, 0.0F, this.gpsLocationListener);
      try
      {
        localLocationManager.requestLocationUpdates("network", 1L, 0.0F, this.networkLocationListener);
        return;
      }
      catch (Exception localException1)
      {
        FileLog.e(localException1);
        return;
      }
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        FileLog.e(localException2);
      }
    }
  }
  
  private void stopLocationUpdate()
  {
    this.updatingLocation = false;
    LocationManager localLocationManager = (LocationManager)ApplicationLoader.applicationContext.getSystemService("location");
    localLocationManager.removeUpdates(this.gpsLocationListener);
    localLocationManager.removeUpdates(this.networkLocationListener);
  }
  
  private void updateRows()
  {
    int j = 2;
    int k = this.rowCount;
    this.rowCount = 0;
    this.scheduleLocationRow = -1;
    this.scheduleUpdateLocationRow = -1;
    this.scheduleLocationInfoRow = -1;
    this.nightDisabledRow = -1;
    this.nightScheduledRow = -1;
    this.nightAutomaticRow = -1;
    this.nightTypeInfoRow = -1;
    this.scheduleHeaderRow = -1;
    this.nightThemeRow = -1;
    this.newThemeRow = -1;
    this.newThemeInfoRow = -1;
    this.scheduleFromRow = -1;
    this.scheduleToRow = -1;
    this.scheduleFromToInfoRow = -1;
    this.themeStartRow = -1;
    this.themeEndRow = -1;
    this.themeInfoRow = -1;
    this.preferedHeaderRow = -1;
    this.automaticHeaderRow = -1;
    this.automaticBrightnessRow = -1;
    this.automaticBrightnessInfoRow = -1;
    if (this.currentType == 0)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.nightThemeRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.newThemeRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.newThemeInfoRow = i;
      this.themeStartRow = this.rowCount;
      this.rowCount += Theme.themes.size();
      this.themeEndRow = this.rowCount;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.themeInfoRow = i;
      if (this.listAdapter != null)
      {
        if ((this.currentType != 0) && (this.previousUpdatedType != -1)) {
          break label613;
        }
        this.listAdapter.notifyDataSetChanged();
      }
    }
    label611:
    label613:
    int m;
    label835:
    label883:
    do
    {
      do
      {
        for (;;)
        {
          if (this.currentType == 1)
          {
            this.previousByLocation = Theme.autoNightScheduleByLocation;
            this.previousUpdatedType = Theme.selectedAutoNightType;
          }
          return;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.nightDisabledRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.nightScheduledRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.nightAutomaticRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.nightTypeInfoRow = i;
          if (Theme.selectedAutoNightType == 1)
          {
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.scheduleHeaderRow = i;
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.scheduleLocationRow = i;
            if (Theme.autoNightScheduleByLocation)
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.scheduleUpdateLocationRow = i;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.scheduleLocationInfoRow = i;
            }
          }
          for (;;)
          {
            if (Theme.selectedAutoNightType == 0) {
              break label611;
            }
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.preferedHeaderRow = i;
            this.themeStartRow = this.rowCount;
            this.rowCount += Theme.themes.size();
            this.themeEndRow = this.rowCount;
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.themeInfoRow = i;
            break;
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.scheduleFromRow = i;
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.scheduleToRow = i;
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.scheduleFromToInfoRow = i;
            continue;
            if (Theme.selectedAutoNightType == 2)
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.automaticHeaderRow = i;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.automaticBrightnessRow = i;
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.automaticBrightnessInfoRow = i;
            }
          }
          break;
          m = this.nightTypeInfoRow + 1;
          if (this.previousUpdatedType == Theme.selectedAutoNightType) {
            break label883;
          }
          i = 0;
          while (i < 3)
          {
            localObject = (RecyclerListView.Holder)this.listView.findViewHolderForAdapterPosition(i);
            if (localObject == null)
            {
              i += 1;
            }
            else
            {
              localObject = (ThemeTypeCell)((RecyclerListView.Holder)localObject).itemView;
              if (i == Theme.selectedAutoNightType) {}
              for (boolean bool = true;; bool = false)
              {
                ((ThemeTypeCell)localObject).setTypeChecked(bool);
                break;
              }
            }
          }
          if (Theme.selectedAutoNightType == 0)
          {
            this.listAdapter.notifyItemRangeRemoved(m, k - m);
          }
          else if (Theme.selectedAutoNightType == 1)
          {
            if (this.previousUpdatedType == 0)
            {
              this.listAdapter.notifyItemRangeInserted(m, this.rowCount - m);
            }
            else if (this.previousUpdatedType == 2)
            {
              this.listAdapter.notifyItemRangeRemoved(m, 3);
              localObject = this.listAdapter;
              if (Theme.autoNightScheduleByLocation) {}
              for (i = 4;; i = 5)
              {
                ((ListAdapter)localObject).notifyItemRangeInserted(m, i);
                break;
              }
            }
          }
          else if (Theme.selectedAutoNightType == 2)
          {
            if (this.previousUpdatedType != 0) {
              break label835;
            }
            this.listAdapter.notifyItemRangeInserted(m, this.rowCount - m);
          }
        }
      } while (this.previousUpdatedType != 1);
      localObject = this.listAdapter;
      if (Theme.autoNightScheduleByLocation) {}
      for (i = 4;; i = 5)
      {
        ((ListAdapter)localObject).notifyItemRangeRemoved(m, i);
        this.listAdapter.notifyItemRangeInserted(m, 3);
        break;
      }
    } while (this.previousByLocation == Theme.autoNightScheduleByLocation);
    Object localObject = this.listAdapter;
    if (Theme.autoNightScheduleByLocation)
    {
      i = 3;
      label907:
      ((ListAdapter)localObject).notifyItemRangeRemoved(m + 2, i);
      localObject = this.listAdapter;
      if (!Theme.autoNightScheduleByLocation) {
        break label949;
      }
    }
    label949:
    for (int i = j;; i = 3)
    {
      ((ListAdapter)localObject).notifyItemRangeInserted(m + 2, i);
      break;
      i = 2;
      break label907;
    }
  }
  
  private void updateSunTime(Location paramLocation, boolean paramBoolean)
  {
    LocationManager localLocationManager = (LocationManager)ApplicationLoader.applicationContext.getSystemService("location");
    Object localObject;
    if (Build.VERSION.SDK_INT >= 23)
    {
      localObject = getParentActivity();
      if ((localObject != null) && (((Activity)localObject).checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0)) {
        ((Activity)localObject).requestPermissions(new String[] { "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, 2);
      }
    }
    do
    {
      return;
      if (getParentActivity() == null) {
        break;
      }
    } while (!getParentActivity().getPackageManager().hasSystemFeature("android.hardware.location.gps"));
    try
    {
      if (!((LocationManager)ApplicationLoader.applicationContext.getSystemService("location")).isProviderEnabled("gps"))
      {
        localObject = new AlertDialog.Builder(getParentActivity());
        ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
        ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("GpsDisabledAlert", 2131493630));
        ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("ConnectingToProxyEnable", 2131493286), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            if (ThemeActivity.this.getParentActivity() == null) {
              return;
            }
            try
            {
              ThemeActivity.this.getParentActivity().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
              return;
            }
            catch (Exception paramAnonymousDialogInterface) {}
          }
        });
        ((AlertDialog.Builder)localObject).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        showDialog(((AlertDialog.Builder)localObject).create());
        return;
      }
    }
    catch (Exception localException1)
    {
      FileLog.e(localException1);
    }
    for (;;)
    {
      try
      {
        localLocation = localLocationManager.getLastKnownLocation("gps");
        if (localLocation != null) {
          continue;
        }
        paramLocation = localLocation;
        localLocation = localLocationManager.getLastKnownLocation("network");
        paramLocation = localLocation;
      }
      catch (Exception localException2)
      {
        Location localLocation;
        FileLog.e(localException2);
        continue;
      }
      if ((paramLocation == null) || (paramBoolean))
      {
        startLocationUpdate();
        if (paramLocation == null) {
          break;
        }
      }
      Theme.autoNightLocationLatitude = paramLocation.getLatitude();
      Theme.autoNightLocationLongitude = paramLocation.getLongitude();
      paramLocation = SunDate.calculateSunriseSunset(Theme.autoNightLocationLatitude, Theme.autoNightLocationLongitude);
      Theme.autoNightSunriseTime = paramLocation[0];
      Theme.autoNightSunsetTime = paramLocation[1];
      Theme.autoNightCityName = null;
      paramLocation = Calendar.getInstance();
      paramLocation.setTimeInMillis(System.currentTimeMillis());
      Theme.autoNightLastSunCheckDay = paramLocation.get(5);
      Utilities.globalQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          try
          {
            final Object localObject1 = new Geocoder(ApplicationLoader.applicationContext, Locale.getDefault()).getFromLocation(Theme.autoNightLocationLatitude, Theme.autoNightLocationLongitude, 1);
            if (((List)localObject1).size() > 0) {}
            for (localObject1 = ((Address)((List)localObject1).get(0)).getLocality();; localObject1 = null)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  Theme.autoNightCityName = localObject1;
                  if (Theme.autoNightCityName == null) {
                    Theme.autoNightCityName = String.format("(%.06f, %.06f)", new Object[] { Double.valueOf(Theme.autoNightLocationLatitude), Double.valueOf(Theme.autoNightLocationLongitude) });
                  }
                  Theme.saveAutoNightThemeConfig();
                  if (ThemeActivity.this.listView != null)
                  {
                    RecyclerListView.Holder localHolder = (RecyclerListView.Holder)ThemeActivity.this.listView.findViewHolderForAdapterPosition(ThemeActivity.this.scheduleUpdateLocationRow);
                    if ((localHolder != null) && ((localHolder.itemView instanceof TextSettingsCell))) {
                      ((TextSettingsCell)localHolder.itemView).setTextAndValue(LocaleController.getString("AutoNightUpdateLocation", 2131493065), Theme.autoNightCityName, false);
                    }
                  }
                }
              });
              return;
            }
          }
          catch (Exception localException)
          {
            for (;;)
            {
              Object localObject2 = null;
            }
          }
        }
      });
      paramLocation = (RecyclerListView.Holder)this.listView.findViewHolderForAdapterPosition(this.scheduleLocationInfoRow);
      if ((paramLocation != null) && ((paramLocation.itemView instanceof TextInfoPrivacyCell))) {
        ((TextInfoPrivacyCell)paramLocation.itemView).setText(getLocationSunString());
      }
      if ((!Theme.autoNightScheduleByLocation) || (Theme.selectedAutoNightType != 1)) {
        break;
      }
      Theme.checkAutoNightThemeConditions();
      return;
      paramLocation = localLocation;
      if (localLocation == null)
      {
        paramLocation = localLocation;
        localLocation = localLocationManager.getLastKnownLocation("passive");
        paramLocation = localLocation;
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(false);
    if (this.currentType == 0) {
      this.actionBar.setTitle(LocaleController.getString("Theme", 2131494482));
    }
    for (;;)
    {
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            ThemeActivity.this.finishFragment();
          }
        }
      });
      this.listAdapter = new ListAdapter(paramContext);
      FrameLayout localFrameLayout = new FrameLayout(paramContext);
      localFrameLayout.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
      this.fragmentView = localFrameLayout;
      this.listView = new RecyclerListView(paramContext);
      this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
      this.listView.setVerticalScrollBarEnabled(false);
      this.listView.setAdapter(this.listAdapter);
      ((DefaultItemAnimator)this.listView.getItemAnimator()).setDelayAnimations(false);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(final View paramAnonymousView, final int paramAnonymousInt)
        {
          if (paramAnonymousInt == ThemeActivity.this.newThemeRow) {
            if (ThemeActivity.this.getParentActivity() != null) {}
          }
          do
          {
            int i;
            do
            {
              for (;;)
              {
                return;
                paramAnonymousView = new EditTextBoldCursor(ThemeActivity.this.getParentActivity());
                paramAnonymousView.setBackgroundDrawable(Theme.createEditTextDrawable(ThemeActivity.this.getParentActivity(), true));
                final Object localObject = new AlertDialog.Builder(ThemeActivity.this.getParentActivity());
                ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("NewTheme", 2131493878));
                ((AlertDialog.Builder)localObject).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int) {}
                });
                LinearLayout localLinearLayout = new LinearLayout(ThemeActivity.this.getParentActivity());
                localLinearLayout.setOrientation(1);
                ((AlertDialog.Builder)localObject).setView(localLinearLayout);
                TextView localTextView = new TextView(ThemeActivity.this.getParentActivity());
                localTextView.setText(LocaleController.formatString("EnterThemeName", 2131493451, new Object[0]));
                localTextView.setTextSize(16.0F);
                localTextView.setPadding(AndroidUtilities.dp(23.0F), AndroidUtilities.dp(12.0F), AndroidUtilities.dp(23.0F), AndroidUtilities.dp(6.0F));
                localTextView.setTextColor(Theme.getColor("dialogTextBlack"));
                localLinearLayout.addView(localTextView, LayoutHelper.createLinear(-1, -2));
                paramAnonymousView.setTextSize(1, 16.0F);
                paramAnonymousView.setTextColor(Theme.getColor("dialogTextBlack"));
                paramAnonymousView.setMaxLines(1);
                paramAnonymousView.setLines(1);
                paramAnonymousView.setInputType(16385);
                paramAnonymousView.setGravity(51);
                paramAnonymousView.setSingleLine(true);
                paramAnonymousView.setImeOptions(6);
                paramAnonymousView.setCursorColor(Theme.getColor("windowBackgroundWhiteBlackText"));
                paramAnonymousView.setCursorSize(AndroidUtilities.dp(20.0F));
                paramAnonymousView.setCursorWidth(1.5F);
                paramAnonymousView.setPadding(0, AndroidUtilities.dp(4.0F), 0, 0);
                localLinearLayout.addView(paramAnonymousView, LayoutHelper.createLinear(-1, 36, 51, 24, 6, 24, 0));
                paramAnonymousView.setOnEditorActionListener(new TextView.OnEditorActionListener()
                {
                  public boolean onEditorAction(TextView paramAnonymous2TextView, int paramAnonymous2Int, KeyEvent paramAnonymous2KeyEvent)
                  {
                    AndroidUtilities.hideKeyboard(paramAnonymous2TextView);
                    return false;
                  }
                });
                localObject = ((AlertDialog.Builder)localObject).create();
                ((AlertDialog)localObject).setOnShowListener(new DialogInterface.OnShowListener()
                {
                  public void onShow(DialogInterface paramAnonymous2DialogInterface)
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        ThemeActivity.2.3.this.val$editText.requestFocus();
                        AndroidUtilities.showKeyboard(ThemeActivity.2.3.this.val$editText);
                      }
                    });
                  }
                });
                ThemeActivity.this.showDialog((Dialog)localObject);
                ((AlertDialog)localObject).getButton(-1).setOnClickListener(new View.OnClickListener()
                {
                  public void onClick(View paramAnonymous2View)
                  {
                    if (paramAnonymousView.length() == 0)
                    {
                      paramAnonymous2View = (Vibrator)ApplicationLoader.applicationContext.getSystemService("vibrator");
                      if (paramAnonymous2View != null) {
                        paramAnonymous2View.vibrate(200L);
                      }
                      AndroidUtilities.shakeView(paramAnonymousView, 2.0F, 0);
                    }
                    do
                    {
                      return;
                      paramAnonymous2View = new ThemeEditorView();
                      String str = paramAnonymousView.getText().toString() + ".attheme";
                      paramAnonymous2View.show(ThemeActivity.this.getParentActivity(), str);
                      Theme.saveCurrentTheme(str, true);
                      ThemeActivity.this.updateRows();
                      localObject.dismiss();
                      paramAnonymous2View = MessagesController.getGlobalMainSettings();
                    } while (paramAnonymous2View.getBoolean("themehint", false));
                    paramAnonymous2View.edit().putBoolean("themehint", true).commit();
                    try
                    {
                      Toast.makeText(ThemeActivity.this.getParentActivity(), LocaleController.getString("CreateNewThemeHelp", 2131493310), 1).show();
                      return;
                    }
                    catch (Exception paramAnonymous2View)
                    {
                      FileLog.e(paramAnonymous2View);
                    }
                  }
                });
                return;
                if ((paramAnonymousInt < ThemeActivity.this.themeStartRow) || (paramAnonymousInt >= ThemeActivity.this.themeEndRow)) {
                  break;
                }
                paramAnonymousInt -= ThemeActivity.this.themeStartRow;
                if ((paramAnonymousInt >= 0) && (paramAnonymousInt < Theme.themes.size()))
                {
                  paramAnonymousView = (Theme.ThemeInfo)Theme.themes.get(paramAnonymousInt);
                  if (ThemeActivity.this.currentType == 0)
                  {
                    Theme.applyTheme(paramAnonymousView);
                    if (ThemeActivity.this.parentLayout != null) {
                      ThemeActivity.this.parentLayout.rebuildAllFragmentViews(false, false);
                    }
                    ThemeActivity.this.finishFragment();
                    return;
                  }
                  Theme.setCurrentNightTheme(paramAnonymousView);
                  i = ThemeActivity.this.listView.getChildCount();
                  paramAnonymousInt = 0;
                  while (paramAnonymousInt < i)
                  {
                    paramAnonymousView = ThemeActivity.this.listView.getChildAt(paramAnonymousInt);
                    if ((paramAnonymousView instanceof ThemeCell)) {
                      ((ThemeCell)paramAnonymousView).updateCurrentThemeCheck();
                    }
                    paramAnonymousInt += 1;
                  }
                }
              }
              if (paramAnonymousInt == ThemeActivity.this.nightThemeRow)
              {
                ThemeActivity.this.presentFragment(new ThemeActivity(1));
                return;
              }
              if (paramAnonymousInt == ThemeActivity.this.nightDisabledRow)
              {
                Theme.selectedAutoNightType = 0;
                ThemeActivity.this.updateRows();
                Theme.checkAutoNightThemeConditions();
                return;
              }
              if (paramAnonymousInt == ThemeActivity.this.nightScheduledRow)
              {
                Theme.selectedAutoNightType = 1;
                if (Theme.autoNightScheduleByLocation) {
                  ThemeActivity.this.updateSunTime(null, true);
                }
                ThemeActivity.this.updateRows();
                Theme.checkAutoNightThemeConditions();
                return;
              }
              if (paramAnonymousInt == ThemeActivity.this.nightAutomaticRow)
              {
                Theme.selectedAutoNightType = 2;
                ThemeActivity.this.updateRows();
                Theme.checkAutoNightThemeConditions();
                return;
              }
              if (paramAnonymousInt == ThemeActivity.this.scheduleLocationRow)
              {
                if (!Theme.autoNightScheduleByLocation) {}
                for (boolean bool = true;; bool = false)
                {
                  Theme.autoNightScheduleByLocation = bool;
                  ((TextCheckCell)paramAnonymousView).setChecked(Theme.autoNightScheduleByLocation);
                  ThemeActivity.this.updateRows();
                  if (Theme.autoNightScheduleByLocation) {
                    ThemeActivity.this.updateSunTime(null, true);
                  }
                  Theme.checkAutoNightThemeConditions();
                  return;
                }
              }
              if ((paramAnonymousInt != ThemeActivity.this.scheduleFromRow) && (paramAnonymousInt != ThemeActivity.this.scheduleToRow)) {
                break;
              }
            } while (ThemeActivity.this.getParentActivity() == null);
            if (paramAnonymousInt == ThemeActivity.this.scheduleFromRow) {
              i = Theme.autoNightDayStartTime / 60;
            }
            for (int j = Theme.autoNightDayStartTime - i * 60;; j = Theme.autoNightDayEndTime - i * 60)
            {
              paramAnonymousView = (TextSettingsCell)paramAnonymousView;
              paramAnonymousView = new TimePickerDialog(ThemeActivity.this.getParentActivity(), new TimePickerDialog.OnTimeSetListener()
              {
                public void onTimeSet(TimePicker paramAnonymous2TimePicker, int paramAnonymous2Int1, int paramAnonymous2Int2)
                {
                  int i = paramAnonymous2Int1 * 60 + paramAnonymous2Int2;
                  if (paramAnonymousInt == ThemeActivity.this.scheduleFromRow)
                  {
                    Theme.autoNightDayStartTime = i;
                    paramAnonymousView.setTextAndValue(LocaleController.getString("AutoNightFrom", 2131493058), String.format("%02d:%02d", new Object[] { Integer.valueOf(paramAnonymous2Int1), Integer.valueOf(paramAnonymous2Int2) }), true);
                    return;
                  }
                  Theme.autoNightDayEndTime = i;
                  paramAnonymousView.setTextAndValue(LocaleController.getString("AutoNightTo", 2131493064), String.format("%02d:%02d", new Object[] { Integer.valueOf(paramAnonymous2Int1), Integer.valueOf(paramAnonymous2Int2) }), true);
                }
              }, i, j, true);
              ThemeActivity.this.showDialog(paramAnonymousView);
              return;
              i = Theme.autoNightDayEndTime / 60;
            }
          } while (paramAnonymousInt != ThemeActivity.this.scheduleUpdateLocationRow);
          ThemeActivity.this.updateSunTime(null, true);
        }
      });
      return this.fragmentView;
      this.actionBar.setTitle(LocaleController.getString("AutoNightTheme", 2131493063));
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.locationPermissionGranted) {
      updateSunTime(null, true);
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, BrightnessControlCell.class, ThemeTypeCell.class, ThemeCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, 0, new Class[] { ThemeCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { ThemeCell.class }, new String[] { "checkImage" }, null, null, null, "featuredStickers_addedIcon"), new ThemeDescription(this.listView, 0, new Class[] { ThemeCell.class }, new String[] { "optionsButton" }, null, null, null, "stickers_menu"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"), new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { BrightnessControlCell.class }, new String[] { "leftImageView" }, null, null, null, "profile_actionIcon"), new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { BrightnessControlCell.class }, new String[] { "rightImageView" }, null, null, null, "profile_actionIcon"), new ThemeDescription(this.listView, 0, new Class[] { BrightnessControlCell.class }, new String[] { "seekBarView" }, null, null, null, "player_progressBackground"), new ThemeDescription(this.listView, ThemeDescription.FLAG_PROGRESSBAR, new Class[] { BrightnessControlCell.class }, new String[] { "seekBarView" }, null, null, null, "player_progress"), new ThemeDescription(this.listView, 0, new Class[] { ThemeTypeCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { ThemeTypeCell.class }, new String[] { "checkImage" }, null, null, null, "featuredStickers_addedIcon") };
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    stopLocationUpdate();
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.locationPermissionGranted);
    Theme.saveAutoNightThemeConfig();
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
  }
  
  private class GpsLocationListener
    implements LocationListener
  {
    private GpsLocationListener() {}
    
    public void onLocationChanged(Location paramLocation)
    {
      if (paramLocation == null) {
        return;
      }
      ThemeActivity.this.stopLocationUpdate();
      ThemeActivity.this.updateSunTime(paramLocation, false);
    }
    
    public void onProviderDisabled(String paramString) {}
    
    public void onProviderEnabled(String paramString) {}
    
    public void onStatusChanged(String paramString, int paramInt, Bundle paramBundle) {}
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
      return ThemeActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == ThemeActivity.this.newThemeRow) || (paramInt == ThemeActivity.this.nightThemeRow) || (paramInt == ThemeActivity.this.scheduleFromRow) || (paramInt == ThemeActivity.this.scheduleToRow) || (paramInt == ThemeActivity.this.scheduleUpdateLocationRow)) {
        return 1;
      }
      if ((paramInt == ThemeActivity.this.newThemeInfoRow) || (paramInt == ThemeActivity.this.automaticBrightnessInfoRow) || (paramInt == ThemeActivity.this.scheduleLocationInfoRow)) {
        return 2;
      }
      if ((paramInt == ThemeActivity.this.themeInfoRow) || (paramInt == ThemeActivity.this.nightTypeInfoRow) || (paramInt == ThemeActivity.this.scheduleFromToInfoRow)) {
        return 3;
      }
      if ((paramInt == ThemeActivity.this.nightDisabledRow) || (paramInt == ThemeActivity.this.nightScheduledRow) || (paramInt == ThemeActivity.this.nightAutomaticRow)) {
        return 4;
      }
      if ((paramInt == ThemeActivity.this.scheduleHeaderRow) || (paramInt == ThemeActivity.this.automaticHeaderRow) || (paramInt == ThemeActivity.this.preferedHeaderRow)) {
        return 5;
      }
      if (paramInt == ThemeActivity.this.automaticBrightnessRow) {
        return 6;
      }
      if (paramInt == ThemeActivity.this.scheduleLocationRow) {
        return 7;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      return (i == 0) || (i == 1) || (i == 4) || (i == 7);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool3 = false;
      boolean bool1 = false;
      boolean bool4 = true;
      boolean bool2 = true;
      switch (paramViewHolder.getItemViewType())
      {
      }
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
                paramInt -= ThemeActivity.this.themeStartRow;
                localObject = (Theme.ThemeInfo)Theme.themes.get(paramInt);
                paramViewHolder = (ThemeCell)paramViewHolder.itemView;
                if (paramInt != Theme.themes.size() - 1) {}
                for (bool1 = bool2;; bool1 = false)
                {
                  paramViewHolder.setTheme((Theme.ThemeInfo)localObject, bool1);
                  return;
                }
                paramViewHolder = (TextSettingsCell)paramViewHolder.itemView;
                if (paramInt == ThemeActivity.this.newThemeRow)
                {
                  paramViewHolder.setText(LocaleController.getString("CreateNewTheme", 2131493309), false);
                  return;
                }
                if (paramInt == ThemeActivity.this.nightThemeRow)
                {
                  if ((Theme.selectedAutoNightType == 0) || (Theme.getCurrentNightTheme() == null))
                  {
                    paramViewHolder.setText(LocaleController.getString("AutoNightTheme", 2131493063), true);
                    return;
                  }
                  paramViewHolder.setTextAndValue(LocaleController.getString("AutoNightTheme", 2131493063), Theme.getCurrentNightThemeName(), true);
                  return;
                }
                int i;
                if (paramInt == ThemeActivity.this.scheduleFromRow)
                {
                  paramInt = Theme.autoNightDayStartTime / 60;
                  i = Theme.autoNightDayStartTime;
                  paramViewHolder.setTextAndValue(LocaleController.getString("AutoNightFrom", 2131493058), String.format("%02d:%02d", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i - paramInt * 60) }), true);
                  return;
                }
                if (paramInt == ThemeActivity.this.scheduleToRow)
                {
                  paramInt = Theme.autoNightDayEndTime / 60;
                  i = Theme.autoNightDayEndTime;
                  paramViewHolder.setTextAndValue(LocaleController.getString("AutoNightTo", 2131493064), String.format("%02d:%02d", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i - paramInt * 60) }), false);
                  return;
                }
              } while (paramInt != ThemeActivity.this.scheduleUpdateLocationRow);
              paramViewHolder.setTextAndValue(LocaleController.getString("AutoNightUpdateLocation", 2131493065), Theme.autoNightCityName, false);
              return;
              paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
              if (paramInt == ThemeActivity.this.newThemeInfoRow)
              {
                paramViewHolder.setText(LocaleController.getString("CreateNewThemeInfo", 2131493311));
                return;
              }
              if (paramInt == ThemeActivity.this.automaticBrightnessInfoRow)
              {
                paramViewHolder.setText(LocaleController.formatString("AutoNightBrightnessInfo", 2131493056, new Object[] { Integer.valueOf((int)(100.0F * Theme.autoNightBrighnessThreshold)) }));
                return;
              }
            } while (paramInt != ThemeActivity.this.scheduleLocationInfoRow);
            paramViewHolder.setText(ThemeActivity.this.getLocationSunString());
            return;
            paramViewHolder = (ThemeTypeCell)paramViewHolder.itemView;
            if (paramInt == ThemeActivity.this.nightDisabledRow)
            {
              localObject = LocaleController.getString("AutoNightDisabled", 2131493057);
              if (Theme.selectedAutoNightType == 0) {
                bool1 = true;
              }
              paramViewHolder.setValue((String)localObject, bool1, true);
              return;
            }
            if (paramInt == ThemeActivity.this.nightScheduledRow)
            {
              localObject = LocaleController.getString("AutoNightScheduled", 2131493062);
              bool1 = bool3;
              if (Theme.selectedAutoNightType == 1) {
                bool1 = true;
              }
              paramViewHolder.setValue((String)localObject, bool1, true);
              return;
            }
          } while (paramInt != ThemeActivity.this.nightAutomaticRow);
          Object localObject = LocaleController.getString("AutoNightAutomatic", 2131493054);
          if (Theme.selectedAutoNightType == 2) {}
          for (bool1 = bool4;; bool1 = false)
          {
            paramViewHolder.setValue((String)localObject, bool1, false);
            return;
          }
          paramViewHolder = (HeaderCell)paramViewHolder.itemView;
          if (paramInt == ThemeActivity.this.scheduleHeaderRow)
          {
            paramViewHolder.setText(LocaleController.getString("AutoNightSchedule", 2131493061));
            return;
          }
          if (paramInt == ThemeActivity.this.automaticHeaderRow)
          {
            paramViewHolder.setText(LocaleController.getString("AutoNightBrightness", 2131493055));
            return;
          }
        } while (paramInt != ThemeActivity.this.preferedHeaderRow);
        paramViewHolder.setText(LocaleController.getString("AutoNightPreferred", 2131493060));
        return;
        ((BrightnessControlCell)paramViewHolder.itemView).setProgress(Theme.autoNightBrighnessThreshold);
        return;
        paramViewHolder = (TextCheckCell)paramViewHolder.itemView;
      } while (paramInt != ThemeActivity.this.scheduleLocationRow);
      paramViewHolder.setTextAndCheck(LocaleController.getString("AutoNightLocation", 2131493059), Theme.autoNightScheduleByLocation, true);
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      boolean bool = true;
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextCheckCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = this.mContext;
        if (ThemeActivity.this.currentType == 1) {}
        for (;;)
        {
          ThemeCell localThemeCell = new ThemeCell(paramViewGroup, bool);
          localThemeCell.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          paramViewGroup = localThemeCell;
          if (ThemeActivity.this.currentType != 0) {
            break;
          }
          ((ThemeCell)localThemeCell).setOnOptionsClick(new View.OnClickListener()
          {
            public void onClick(View paramAnonymousView)
            {
              final Theme.ThemeInfo localThemeInfo = ((ThemeCell)paramAnonymousView.getParent()).getCurrentThemeInfo();
              if (ThemeActivity.this.getParentActivity() == null) {
                return;
              }
              BottomSheet.Builder localBuilder = new BottomSheet.Builder(ThemeActivity.this.getParentActivity());
              if (localThemeInfo.pathToFile == null)
              {
                paramAnonymousView = new CharSequence[1];
                paramAnonymousView[0] = LocaleController.getString("ShareFile", 2131494383);
              }
              for (;;)
              {
                localBuilder.setItems(paramAnonymousView, new DialogInterface.OnClickListener()
                {
                  /* Error */
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                  {
                    // Byte code:
                    //   0: iload_2
                    //   1: ifne +434 -> 435
                    //   4: aload_0
                    //   5: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   8: getfield 41	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
                    //   11: ifnonnull +256 -> 267
                    //   14: aload_0
                    //   15: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   18: getfield 44	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
                    //   21: ifnonnull +246 -> 267
                    //   24: new 46	java/lang/StringBuilder
                    //   27: dup
                    //   28: invokespecial 47	java/lang/StringBuilder:<init>	()V
                    //   31: astore 7
                    //   33: invokestatic 53	org/telegram/ui/ActionBar/Theme:getDefaultColors	()Ljava/util/HashMap;
                    //   36: invokevirtual 59	java/util/HashMap:entrySet	()Ljava/util/Set;
                    //   39: invokeinterface 65 1 0
                    //   44: astore_1
                    //   45: aload_1
                    //   46: invokeinterface 71 1 0
                    //   51: ifeq +53 -> 104
                    //   54: aload_1
                    //   55: invokeinterface 75 1 0
                    //   60: checkcast 77	java/util/Map$Entry
                    //   63: astore 4
                    //   65: aload 7
                    //   67: aload 4
                    //   69: invokeinterface 80 1 0
                    //   74: checkcast 82	java/lang/String
                    //   77: invokevirtual 86	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    //   80: ldc 88
                    //   82: invokevirtual 86	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    //   85: aload 4
                    //   87: invokeinterface 91 1 0
                    //   92: invokevirtual 94	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
                    //   95: ldc 96
                    //   97: invokevirtual 86	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    //   100: pop
                    //   101: goto -56 -> 45
                    //   104: new 98	java/io/File
                    //   107: dup
                    //   108: invokestatic 104	org/telegram/messenger/ApplicationLoader:getFilesDirFixed	()Ljava/io/File;
                    //   111: ldc 106
                    //   113: invokespecial 109	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
                    //   116: astore 6
                    //   118: aconst_null
                    //   119: astore_1
                    //   120: aconst_null
                    //   121: astore 5
                    //   123: new 111	java/io/FileOutputStream
                    //   126: dup
                    //   127: aload 6
                    //   129: invokespecial 114	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
                    //   132: astore 4
                    //   134: aload 4
                    //   136: aload 7
                    //   138: invokevirtual 118	java/lang/StringBuilder:toString	()Ljava/lang/String;
                    //   141: invokevirtual 122	java/lang/String:getBytes	()[B
                    //   144: invokevirtual 126	java/io/FileOutputStream:write	([B)V
                    //   147: aload 4
                    //   149: ifnull +8 -> 157
                    //   152: aload 4
                    //   154: invokevirtual 129	java/io/FileOutputStream:close	()V
                    //   157: aload 6
                    //   159: astore_1
                    //   160: new 98	java/io/File
                    //   163: dup
                    //   164: iconst_4
                    //   165: invokestatic 135	org/telegram/messenger/FileLoader:getDirectory	(I)Ljava/io/File;
                    //   168: aload_1
                    //   169: invokevirtual 138	java/io/File:getName	()Ljava/lang/String;
                    //   172: invokespecial 109	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
                    //   175: astore 4
                    //   177: aload_1
                    //   178: aload 4
                    //   180: invokestatic 144	org/telegram/messenger/AndroidUtilities:copyFile	(Ljava/io/File;Ljava/io/File;)Z
                    //   183: istore_3
                    //   184: iload_3
                    //   185: ifne +124 -> 309
                    //   188: return
                    //   189: astore_1
                    //   190: aload_1
                    //   191: invokestatic 150	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
                    //   194: aload 6
                    //   196: astore_1
                    //   197: goto -37 -> 160
                    //   200: astore_1
                    //   201: aload 5
                    //   203: astore 4
                    //   205: aload_1
                    //   206: astore 5
                    //   208: aload 4
                    //   210: astore_1
                    //   211: aload 5
                    //   213: invokestatic 150	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
                    //   216: aload 6
                    //   218: astore_1
                    //   219: aload 4
                    //   221: ifnull -61 -> 160
                    //   224: aload 4
                    //   226: invokevirtual 129	java/io/FileOutputStream:close	()V
                    //   229: aload 6
                    //   231: astore_1
                    //   232: goto -72 -> 160
                    //   235: astore_1
                    //   236: aload_1
                    //   237: invokestatic 150	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
                    //   240: aload 6
                    //   242: astore_1
                    //   243: goto -83 -> 160
                    //   246: astore 4
                    //   248: aload_1
                    //   249: ifnull +7 -> 256
                    //   252: aload_1
                    //   253: invokevirtual 129	java/io/FileOutputStream:close	()V
                    //   256: aload 4
                    //   258: athrow
                    //   259: astore_1
                    //   260: aload_1
                    //   261: invokestatic 150	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
                    //   264: goto -8 -> 256
                    //   267: aload_0
                    //   268: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   271: getfield 44	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
                    //   274: ifnull +17 -> 291
                    //   277: aload_0
                    //   278: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   281: getfield 44	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
                    //   284: invokestatic 154	org/telegram/ui/ActionBar/Theme:getAssetFile	(Ljava/lang/String;)Ljava/io/File;
                    //   287: astore_1
                    //   288: goto -128 -> 160
                    //   291: new 98	java/io/File
                    //   294: dup
                    //   295: aload_0
                    //   296: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   299: getfield 41	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
                    //   302: invokespecial 157	java/io/File:<init>	(Ljava/lang/String;)V
                    //   305: astore_1
                    //   306: goto -146 -> 160
                    //   309: new 159	android/content/Intent
                    //   312: dup
                    //   313: ldc -95
                    //   315: invokespecial 162	android/content/Intent:<init>	(Ljava/lang/String;)V
                    //   318: astore_1
                    //   319: aload_1
                    //   320: ldc -92
                    //   322: invokevirtual 168	android/content/Intent:setType	(Ljava/lang/String;)Landroid/content/Intent;
                    //   325: pop
                    //   326: getstatic 174	android/os/Build$VERSION:SDK_INT	I
                    //   329: istore_2
                    //   330: iload_2
                    //   331: bipush 24
                    //   333: if_icmplt +87 -> 420
                    //   336: aload_1
                    //   337: ldc -80
                    //   339: aload_0
                    //   340: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   343: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   346: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   349: invokevirtual 188	org/telegram/ui/ThemeActivity:getParentActivity	()Landroid/app/Activity;
                    //   352: ldc -66
                    //   354: aload 4
                    //   356: invokestatic 196	android/support/v4/content/FileProvider:getUriForFile	(Landroid/content/Context;Ljava/lang/String;Ljava/io/File;)Landroid/net/Uri;
                    //   359: invokevirtual 200	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
                    //   362: pop
                    //   363: aload_1
                    //   364: iconst_1
                    //   365: invokevirtual 204	android/content/Intent:setFlags	(I)Landroid/content/Intent;
                    //   368: pop
                    //   369: aload_0
                    //   370: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   373: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   376: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   379: aload_1
                    //   380: ldc -50
                    //   382: ldc -49
                    //   384: invokestatic 213	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
                    //   387: invokestatic 217	android/content/Intent:createChooser	(Landroid/content/Intent;Ljava/lang/CharSequence;)Landroid/content/Intent;
                    //   390: sipush 500
                    //   393: invokevirtual 221	org/telegram/ui/ThemeActivity:startActivityForResult	(Landroid/content/Intent;I)V
                    //   396: return
                    //   397: astore_1
                    //   398: aload_1
                    //   399: invokestatic 150	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
                    //   402: return
                    //   403: astore 5
                    //   405: aload_1
                    //   406: ldc -80
                    //   408: aload 4
                    //   410: invokestatic 227	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
                    //   413: invokevirtual 200	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
                    //   416: pop
                    //   417: goto -48 -> 369
                    //   420: aload_1
                    //   421: ldc -80
                    //   423: aload 4
                    //   425: invokestatic 227	android/net/Uri:fromFile	(Ljava/io/File;)Landroid/net/Uri;
                    //   428: invokevirtual 200	android/content/Intent:putExtra	(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
                    //   431: pop
                    //   432: goto -63 -> 369
                    //   435: iload_2
                    //   436: iconst_1
                    //   437: if_icmpne +75 -> 512
                    //   440: aload_0
                    //   441: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   444: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   447: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   450: invokestatic 231	org/telegram/ui/ThemeActivity:access$2000	(Lorg/telegram/ui/ThemeActivity;)Lorg/telegram/ui/ActionBar/ActionBarLayout;
                    //   453: ifnull -265 -> 188
                    //   456: aload_0
                    //   457: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   460: invokestatic 235	org/telegram/ui/ActionBar/Theme:applyTheme	(Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;)V
                    //   463: aload_0
                    //   464: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   467: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   470: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   473: invokestatic 238	org/telegram/ui/ThemeActivity:access$2100	(Lorg/telegram/ui/ThemeActivity;)Lorg/telegram/ui/ActionBar/ActionBarLayout;
                    //   476: iconst_1
                    //   477: iconst_1
                    //   478: invokevirtual 244	org/telegram/ui/ActionBar/ActionBarLayout:rebuildAllFragmentViews	(ZZ)V
                    //   481: new 246	org/telegram/ui/Components/ThemeEditorView
                    //   484: dup
                    //   485: invokespecial 247	org/telegram/ui/Components/ThemeEditorView:<init>	()V
                    //   488: aload_0
                    //   489: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   492: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   495: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   498: invokevirtual 188	org/telegram/ui/ThemeActivity:getParentActivity	()Landroid/app/Activity;
                    //   501: aload_0
                    //   502: getfield 28	org/telegram/ui/ThemeActivity$ListAdapter$1$1:val$themeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
                    //   505: getfield 250	org/telegram/ui/ActionBar/Theme$ThemeInfo:name	Ljava/lang/String;
                    //   508: invokevirtual 254	org/telegram/ui/Components/ThemeEditorView:show	(Landroid/app/Activity;Ljava/lang/String;)V
                    //   511: return
                    //   512: aload_0
                    //   513: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   516: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   519: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   522: invokevirtual 188	org/telegram/ui/ThemeActivity:getParentActivity	()Landroid/app/Activity;
                    //   525: ifnull -337 -> 188
                    //   528: new 256	org/telegram/ui/ActionBar/AlertDialog$Builder
                    //   531: dup
                    //   532: aload_0
                    //   533: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   536: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   539: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   542: invokevirtual 188	org/telegram/ui/ThemeActivity:getParentActivity	()Landroid/app/Activity;
                    //   545: invokespecial 259	org/telegram/ui/ActionBar/AlertDialog$Builder:<init>	(Landroid/content/Context;)V
                    //   548: astore_1
                    //   549: aload_1
                    //   550: ldc_w 261
                    //   553: ldc_w 262
                    //   556: invokestatic 213	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
                    //   559: invokevirtual 266	org/telegram/ui/ActionBar/AlertDialog$Builder:setMessage	(Ljava/lang/CharSequence;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
                    //   562: pop
                    //   563: aload_1
                    //   564: ldc_w 268
                    //   567: ldc_w 269
                    //   570: invokestatic 213	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
                    //   573: invokevirtual 272	org/telegram/ui/ActionBar/AlertDialog$Builder:setTitle	(Ljava/lang/CharSequence;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
                    //   576: pop
                    //   577: aload_1
                    //   578: ldc_w 274
                    //   581: ldc_w 275
                    //   584: invokestatic 213	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
                    //   587: new 18	org/telegram/ui/ThemeActivity$ListAdapter$1$1$1
                    //   590: dup
                    //   591: aload_0
                    //   592: invokespecial 278	org/telegram/ui/ThemeActivity$ListAdapter$1$1$1:<init>	(Lorg/telegram/ui/ThemeActivity$ListAdapter$1$1;)V
                    //   595: invokevirtual 282	org/telegram/ui/ActionBar/AlertDialog$Builder:setPositiveButton	(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
                    //   598: pop
                    //   599: aload_1
                    //   600: ldc_w 284
                    //   603: ldc_w 285
                    //   606: invokestatic 213	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
                    //   609: aconst_null
                    //   610: invokevirtual 288	org/telegram/ui/ActionBar/AlertDialog$Builder:setNegativeButton	(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Lorg/telegram/ui/ActionBar/AlertDialog$Builder;
                    //   613: pop
                    //   614: aload_0
                    //   615: getfield 26	org/telegram/ui/ThemeActivity$ListAdapter$1$1:this$2	Lorg/telegram/ui/ThemeActivity$ListAdapter$1;
                    //   618: getfield 180	org/telegram/ui/ThemeActivity$ListAdapter$1:this$1	Lorg/telegram/ui/ThemeActivity$ListAdapter;
                    //   621: getfield 184	org/telegram/ui/ThemeActivity$ListAdapter:this$0	Lorg/telegram/ui/ThemeActivity;
                    //   624: aload_1
                    //   625: invokevirtual 292	org/telegram/ui/ActionBar/AlertDialog$Builder:create	()Lorg/telegram/ui/ActionBar/AlertDialog;
                    //   628: invokevirtual 296	org/telegram/ui/ThemeActivity:showDialog	(Landroid/app/Dialog;)Landroid/app/Dialog;
                    //   631: pop
                    //   632: return
                    //   633: astore 5
                    //   635: aload 4
                    //   637: astore_1
                    //   638: aload 5
                    //   640: astore 4
                    //   642: goto -394 -> 248
                    //   645: astore 5
                    //   647: goto -439 -> 208
                    // Local variable table:
                    //   start	length	slot	name	signature
                    //   0	650	0	this	1
                    //   0	650	1	paramAnonymous2DialogInterface	DialogInterface
                    //   0	650	2	paramAnonymous2Int	int
                    //   183	2	3	bool	boolean
                    //   63	162	4	localObject1	Object
                    //   246	390	4	localFile1	java.io.File
                    //   640	1	4	localObject2	Object
                    //   121	91	5	localDialogInterface	DialogInterface
                    //   403	1	5	localException1	Exception
                    //   633	6	5	localObject3	Object
                    //   645	1	5	localException2	Exception
                    //   116	125	6	localFile2	java.io.File
                    //   31	106	7	localStringBuilder	StringBuilder
                    // Exception table:
                    //   from	to	target	type
                    //   152	157	189	java/lang/Exception
                    //   123	134	200	java/lang/Exception
                    //   224	229	235	java/lang/Exception
                    //   123	134	246	finally
                    //   211	216	246	finally
                    //   252	256	259	java/lang/Exception
                    //   177	184	397	java/lang/Exception
                    //   309	330	397	java/lang/Exception
                    //   369	396	397	java/lang/Exception
                    //   405	417	397	java/lang/Exception
                    //   420	432	397	java/lang/Exception
                    //   336	369	403	java/lang/Exception
                    //   134	147	633	finally
                    //   134	147	645	java/lang/Exception
                  }
                });
                ThemeActivity.this.showDialog(localBuilder.create());
                return;
                paramAnonymousView = new CharSequence[3];
                paramAnonymousView[0] = LocaleController.getString("ShareFile", 2131494383);
                paramAnonymousView[1] = LocaleController.getString("Edit", 2131493397);
                paramAnonymousView[2] = LocaleController.getString("Delete", 2131493356);
              }
            }
          });
          paramViewGroup = localThemeCell;
          break;
          bool = false;
        }
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
        continue;
        paramViewGroup = new ThemeTypeCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new BrightnessControlCell(this.mContext)
        {
          protected void didChangedValue(float paramAnonymousFloat)
          {
            int i = (int)(Theme.autoNightBrighnessThreshold * 100.0F);
            int j = (int)(paramAnonymousFloat * 100.0F);
            Theme.autoNightBrighnessThreshold = paramAnonymousFloat;
            if (i != j)
            {
              RecyclerListView.Holder localHolder = (RecyclerListView.Holder)ThemeActivity.this.listView.findViewHolderForAdapterPosition(ThemeActivity.this.automaticBrightnessInfoRow);
              if (localHolder != null) {
                ((TextInfoPrivacyCell)localHolder.itemView).setText(LocaleController.formatString("AutoNightBrightnessInfo", 2131493056, new Object[] { Integer.valueOf((int)(Theme.autoNightBrighnessThreshold * 100.0F)) }));
              }
              Theme.checkAutoNightThemeConditions(true);
            }
          }
        };
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
    
    public void onViewAttachedToWindow(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      boolean bool;
      if (i == 4)
      {
        ThemeTypeCell localThemeTypeCell = (ThemeTypeCell)paramViewHolder.itemView;
        if (paramViewHolder.getAdapterPosition() == Theme.selectedAutoNightType)
        {
          bool = true;
          localThemeTypeCell.setTypeChecked(bool);
        }
      }
      for (;;)
      {
        if ((i != 2) && (i != 3)) {
          paramViewHolder.itemView.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        }
        return;
        bool = false;
        break;
        if (i == 0) {
          ((ThemeCell)paramViewHolder.itemView).updateCurrentThemeCheck();
        }
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ThemeActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */