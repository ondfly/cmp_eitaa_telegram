package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DataSettingsActivity
  extends BaseFragment
{
  private AnimatorSet animatorSet;
  private int autoDownloadMediaRow;
  private int callsSection2Row;
  private int callsSectionRow;
  private int enableAllStreamInfoRow;
  private int enableAllStreamRow;
  private int enableCacheStreamRow;
  private int enableStreamRow;
  private int filesRow;
  private int gifsRow;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private int mediaDownloadSection2Row;
  private int mediaDownloadSectionRow;
  private int mobileUsageRow;
  private int musicRow;
  private int photosRow;
  private int proxyRow;
  private int proxySection2Row;
  private int proxySectionRow;
  private int quickRepliesRow;
  private int resetDownloadRow;
  private int roamingUsageRow;
  private int rowCount;
  private int storageUsageRow;
  private int streamSectionRow;
  private int usageSection2Row;
  private int usageSectionRow;
  private int useLessDataForCallsRow;
  private int videoMessagesRow;
  private int videosRow;
  private int voiceMessagesRow;
  private int wifiUsageRow;
  
  private void updateAutodownloadRows(boolean paramBoolean)
  {
    int j = this.listView.getChildCount();
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    if (i < j)
    {
      Object localObject = this.listView.getChildAt(i);
      localObject = (RecyclerListView.Holder)this.listView.getChildViewHolder((View)localObject);
      ((RecyclerListView.Holder)localObject).getItemViewType();
      int k = ((RecyclerListView.Holder)localObject).getAdapterPosition();
      if ((k >= this.photosRow) && (k <= this.gifsRow)) {
        ((TextSettingsCell)((RecyclerListView.Holder)localObject).itemView).setEnabled(DownloadController.getInstance(this.currentAccount).globalAutodownloadEnabled, localArrayList);
      }
      for (;;)
      {
        i += 1;
        break;
        if ((paramBoolean) && (k == this.autoDownloadMediaRow)) {
          ((TextCheckCell)((RecyclerListView.Holder)localObject).itemView).setChecked(true);
        }
      }
    }
    if (!localArrayList.isEmpty())
    {
      if (this.animatorSet != null) {
        this.animatorSet.cancel();
      }
      this.animatorSet = new AnimatorSet();
      this.animatorSet.playTogether(localArrayList);
      this.animatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(DataSettingsActivity.this.animatorSet)) {
            DataSettingsActivity.access$3302(DataSettingsActivity.this, null);
          }
        }
      });
      this.animatorSet.setDuration(150L);
      this.animatorSet.start();
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setTitle(LocaleController.getString("DataSettings", 2131493328));
    if (AndroidUtilities.isTablet()) {
      this.actionBar.setOccupyStatusBar(false);
    }
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          DataSettingsActivity.this.finishFragment();
        }
      }
    });
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.listView = new RecyclerListView(paramContext);
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, final int paramAnonymousInt)
      {
        if ((paramAnonymousInt == DataSettingsActivity.this.photosRow) || (paramAnonymousInt == DataSettingsActivity.this.voiceMessagesRow) || (paramAnonymousInt == DataSettingsActivity.this.videoMessagesRow) || (paramAnonymousInt == DataSettingsActivity.this.videosRow) || (paramAnonymousInt == DataSettingsActivity.this.filesRow) || (paramAnonymousInt == DataSettingsActivity.this.musicRow) || (paramAnonymousInt == DataSettingsActivity.this.gifsRow)) {
          if (DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled) {}
        }
        do
        {
          do
          {
            do
            {
              return;
              if (paramAnonymousInt == DataSettingsActivity.this.photosRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(1));
                return;
              }
              if (paramAnonymousInt == DataSettingsActivity.this.voiceMessagesRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(2));
                return;
              }
              if (paramAnonymousInt == DataSettingsActivity.this.videoMessagesRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(64));
                return;
              }
              if (paramAnonymousInt == DataSettingsActivity.this.videosRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(4));
                return;
              }
              if (paramAnonymousInt == DataSettingsActivity.this.filesRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(8));
                return;
              }
              if (paramAnonymousInt == DataSettingsActivity.this.musicRow)
              {
                DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(16));
                return;
              }
            } while (paramAnonymousInt != DataSettingsActivity.this.gifsRow);
            DataSettingsActivity.this.presentFragment(new DataAutoDownloadActivity(32));
            return;
            if (paramAnonymousInt != DataSettingsActivity.this.resetDownloadRow) {
              break;
            }
          } while (DataSettingsActivity.this.getParentActivity() == null);
          paramAnonymousView = new AlertDialog.Builder(DataSettingsActivity.this.getParentActivity());
          paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
          paramAnonymousView.setMessage(LocaleController.getString("ResetAutomaticMediaDownloadAlert", 2131494258));
          paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              SharedPreferences.Editor localEditor = MessagesController.getMainSettings(DataSettingsActivity.this.currentAccount).edit();
              DownloadController localDownloadController = DownloadController.getInstance(DataSettingsActivity.this.currentAccount);
              paramAnonymous2Int = 0;
              if (paramAnonymous2Int < 4)
              {
                localDownloadController.mobileDataDownloadMask[paramAnonymous2Int] = 115;
                localDownloadController.wifiDownloadMask[paramAnonymous2Int] = 115;
                localDownloadController.roamingDownloadMask[paramAnonymous2Int] = 0;
                StringBuilder localStringBuilder = new StringBuilder().append("mobileDataDownloadMask");
                if (paramAnonymous2Int != 0)
                {
                  paramAnonymous2DialogInterface = Integer.valueOf(paramAnonymous2Int);
                  label91:
                  localEditor.putInt(paramAnonymous2DialogInterface, localDownloadController.mobileDataDownloadMask[paramAnonymous2Int]);
                  localStringBuilder = new StringBuilder().append("wifiDownloadMask");
                  if (paramAnonymous2Int == 0) {
                    break label222;
                  }
                  paramAnonymous2DialogInterface = Integer.valueOf(paramAnonymous2Int);
                  label138:
                  localEditor.putInt(paramAnonymous2DialogInterface, localDownloadController.wifiDownloadMask[paramAnonymous2Int]);
                  localStringBuilder = new StringBuilder().append("roamingDownloadMask");
                  if (paramAnonymous2Int == 0) {
                    break label228;
                  }
                }
                label222:
                label228:
                for (paramAnonymous2DialogInterface = Integer.valueOf(paramAnonymous2Int);; paramAnonymous2DialogInterface = "")
                {
                  localEditor.putInt(paramAnonymous2DialogInterface, localDownloadController.roamingDownloadMask[paramAnonymous2Int]);
                  paramAnonymous2Int += 1;
                  break;
                  paramAnonymous2DialogInterface = "";
                  break label91;
                  paramAnonymous2DialogInterface = "";
                  break label138;
                }
              }
              int i = 0;
              if (i < 7)
              {
                if (i == 1) {
                  paramAnonymous2Int = 2097152;
                }
                for (;;)
                {
                  localDownloadController.mobileMaxFileSize[i] = paramAnonymous2Int;
                  localDownloadController.wifiMaxFileSize[i] = paramAnonymous2Int;
                  localDownloadController.roamingMaxFileSize[i] = paramAnonymous2Int;
                  localEditor.putInt("mobileMaxDownloadSize" + i, paramAnonymous2Int);
                  localEditor.putInt("wifiMaxDownloadSize" + i, paramAnonymous2Int);
                  localEditor.putInt("roamingMaxDownloadSize" + i, paramAnonymous2Int);
                  i += 1;
                  break;
                  if (i == 6) {
                    paramAnonymous2Int = 5242880;
                  } else {
                    paramAnonymous2Int = 10485760;
                  }
                }
              }
              if (!DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled)
              {
                DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled = true;
                localEditor.putBoolean("globalAutodownloadEnabled", DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled);
                DataSettingsActivity.this.updateAutodownloadRows(true);
              }
              localEditor.commit();
              DownloadController.getInstance(DataSettingsActivity.this.currentAccount).checkAutodownloadSettings();
            }
          });
          paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          paramAnonymousView.show();
          return;
          Object localObject1;
          if (paramAnonymousInt == DataSettingsActivity.this.autoDownloadMediaRow)
          {
            localObject1 = DownloadController.getInstance(DataSettingsActivity.this.currentAccount);
            if (!DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled) {}
            for (boolean bool = true;; bool = false)
            {
              ((DownloadController)localObject1).globalAutodownloadEnabled = bool;
              MessagesController.getMainSettings(DataSettingsActivity.this.currentAccount).edit().putBoolean("globalAutodownloadEnabled", DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled).commit();
              ((TextCheckCell)paramAnonymousView).setChecked(DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled);
              DataSettingsActivity.this.updateAutodownloadRows(false);
              return;
            }
          }
          if (paramAnonymousInt == DataSettingsActivity.this.storageUsageRow)
          {
            DataSettingsActivity.this.presentFragment(new CacheControlActivity());
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.useLessDataForCallsRow)
          {
            final Object localObject2 = MessagesController.getGlobalMainSettings();
            paramAnonymousView = DataSettingsActivity.this.getParentActivity();
            localObject1 = DataSettingsActivity.this;
            String str1 = LocaleController.getString("UseLessDataNever", 2131494531);
            String str2 = LocaleController.getString("UseLessDataOnMobile", 2131494532);
            String str3 = LocaleController.getString("UseLessDataAlways", 2131494530);
            String str4 = LocaleController.getString("VoipUseLessData", 2131494619);
            int i = ((SharedPreferences)localObject2).getInt("VoipDataSaving", 0);
            localObject2 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                int i = -1;
                switch (paramAnonymous2Int)
                {
                default: 
                  paramAnonymous2Int = i;
                }
                for (;;)
                {
                  if (paramAnonymous2Int != -1) {
                    localObject2.edit().putInt("VoipDataSaving", paramAnonymous2Int).commit();
                  }
                  if (DataSettingsActivity.this.listAdapter != null) {
                    DataSettingsActivity.this.listAdapter.notifyItemChanged(paramAnonymousInt);
                  }
                  return;
                  paramAnonymous2Int = 0;
                  continue;
                  paramAnonymous2Int = 1;
                  continue;
                  paramAnonymous2Int = 2;
                }
              }
            };
            paramAnonymousView = AlertsCreator.createSingleChoiceDialog(paramAnonymousView, (BaseFragment)localObject1, new String[] { str1, str2, str3 }, str4, i, (DialogInterface.OnClickListener)localObject2);
            DataSettingsActivity.this.setVisibleDialog(paramAnonymousView);
            paramAnonymousView.show();
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.mobileUsageRow)
          {
            DataSettingsActivity.this.presentFragment(new DataUsageActivity(0));
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.roamingUsageRow)
          {
            DataSettingsActivity.this.presentFragment(new DataUsageActivity(2));
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.wifiUsageRow)
          {
            DataSettingsActivity.this.presentFragment(new DataUsageActivity(1));
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.proxyRow)
          {
            DataSettingsActivity.this.presentFragment(new ProxySettingsActivity());
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.enableStreamRow)
          {
            SharedConfig.toggleStreamMedia();
            ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.streamMedia);
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.enableAllStreamRow)
          {
            SharedConfig.toggleStreamAllVideo();
            ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.streamAllVideo);
            return;
          }
          if (paramAnonymousInt == DataSettingsActivity.this.enableCacheStreamRow)
          {
            SharedConfig.toggleSaveStreamMedia();
            ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.saveStreamMedia);
            return;
          }
        } while (paramAnonymousInt != DataSettingsActivity.this.quickRepliesRow);
        DataSettingsActivity.this.presentFragment(new QuickRepliesSettingsActivity());
      }
    });
    localFrameLayout.addView(this.actionBar);
    return this.fragmentView;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextSettingsCell.class, TextCheckCell.class, HeaderCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4") };
  }
  
  protected void onDialogDismiss(Dialog paramDialog)
  {
    DownloadController.getInstance(this.currentAccount).checkAutodownloadSettings();
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.usageSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.storageUsageRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.mobileUsageRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.wifiUsageRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.roamingUsageRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.usageSection2Row = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.mediaDownloadSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.autoDownloadMediaRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.photosRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.voiceMessagesRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.videoMessagesRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.videosRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.filesRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.musicRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.gifsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.resetDownloadRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.mediaDownloadSection2Row = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.streamSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.enableStreamRow = i;
    if (BuildVars.DEBUG_VERSION)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    for (this.enableAllStreamRow = i;; this.enableAllStreamRow = -1)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.enableAllStreamInfoRow = i;
      this.enableCacheStreamRow = -1;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsSectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.useLessDataForCallsRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.quickRepliesRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.callsSection2Row = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.proxySectionRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.proxyRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.proxySection2Row = i;
      return true;
    }
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
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
      return DataSettingsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == DataSettingsActivity.this.mediaDownloadSection2Row) || (paramInt == DataSettingsActivity.this.usageSection2Row) || (paramInt == DataSettingsActivity.this.callsSection2Row) || (paramInt == DataSettingsActivity.this.proxySection2Row)) {
        return 0;
      }
      if ((paramInt == DataSettingsActivity.this.mediaDownloadSectionRow) || (paramInt == DataSettingsActivity.this.streamSectionRow) || (paramInt == DataSettingsActivity.this.callsSectionRow) || (paramInt == DataSettingsActivity.this.usageSectionRow) || (paramInt == DataSettingsActivity.this.proxySectionRow)) {
        return 2;
      }
      if ((paramInt == DataSettingsActivity.this.autoDownloadMediaRow) || (paramInt == DataSettingsActivity.this.enableCacheStreamRow) || (paramInt == DataSettingsActivity.this.enableStreamRow) || (paramInt == DataSettingsActivity.this.enableAllStreamRow)) {
        return 3;
      }
      if (paramInt == DataSettingsActivity.this.enableAllStreamInfoRow) {
        return 4;
      }
      return 1;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      if ((i == DataSettingsActivity.this.photosRow) || (i == DataSettingsActivity.this.voiceMessagesRow) || (i == DataSettingsActivity.this.videoMessagesRow) || (i == DataSettingsActivity.this.videosRow) || (i == DataSettingsActivity.this.filesRow) || (i == DataSettingsActivity.this.musicRow) || (i == DataSettingsActivity.this.gifsRow)) {
        return DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled;
      }
      return (i == DataSettingsActivity.this.storageUsageRow) || (i == DataSettingsActivity.this.useLessDataForCallsRow) || (i == DataSettingsActivity.this.mobileUsageRow) || (i == DataSettingsActivity.this.roamingUsageRow) || (i == DataSettingsActivity.this.wifiUsageRow) || (i == DataSettingsActivity.this.proxyRow) || (i == DataSettingsActivity.this.resetDownloadRow) || (i == DataSettingsActivity.this.autoDownloadMediaRow) || (i == DataSettingsActivity.this.enableCacheStreamRow) || (i == DataSettingsActivity.this.enableStreamRow) || (i == DataSettingsActivity.this.enableAllStreamRow) || (i == DataSettingsActivity.this.quickRepliesRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool1 = true;
      switch (paramViewHolder.getItemViewType())
      {
      }
      do
      {
        do
        {
          Object localObject;
          do
          {
            do
            {
              return;
              if (paramInt == DataSettingsActivity.this.proxySection2Row)
              {
                paramViewHolder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
                return;
              }
              paramViewHolder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
              return;
              localObject = (TextSettingsCell)paramViewHolder.itemView;
              ((TextSettingsCell)localObject).setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
              if (paramInt == DataSettingsActivity.this.storageUsageRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("StorageUsage", 2131494442), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.useLessDataForCallsRow)
              {
                SharedPreferences localSharedPreferences = MessagesController.getGlobalMainSettings();
                paramViewHolder = null;
                switch (localSharedPreferences.getInt("VoipDataSaving", 0))
                {
                }
                for (;;)
                {
                  ((TextSettingsCell)localObject).setTextAndValue(LocaleController.getString("VoipUseLessData", 2131494619), paramViewHolder, true);
                  return;
                  paramViewHolder = LocaleController.getString("UseLessDataNever", 2131494531);
                  continue;
                  paramViewHolder = LocaleController.getString("UseLessDataOnMobile", 2131494532);
                  continue;
                  paramViewHolder = LocaleController.getString("UseLessDataAlways", 2131494530);
                }
              }
              if (paramInt == DataSettingsActivity.this.mobileUsageRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("MobileUsage", 2131493848), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.roamingUsageRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("RoamingUsage", 2131494282), false);
                return;
              }
              if (paramInt == DataSettingsActivity.this.wifiUsageRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("WiFiUsage", 2131494640), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.proxyRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("ProxySettings", 2131494207), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.resetDownloadRow)
              {
                ((TextSettingsCell)localObject).setTextColor(Theme.getColor("windowBackgroundWhiteRedText"));
                ((TextSettingsCell)localObject).setText(LocaleController.getString("ResetAutomaticMediaDownload", 2131494257), false);
                return;
              }
              if (paramInt == DataSettingsActivity.this.photosRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("LocalPhotoCache", 2131493771), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.voiceMessagesRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("AudioAutodownload", 2131493045), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.videoMessagesRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("VideoMessagesAutodownload", 2131494579), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.videosRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("LocalVideoCache", 2131493772), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.filesRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("FilesDataUsage", 2131493538), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.musicRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("AttachMusic", 2131493036), true);
                return;
              }
              if (paramInt == DataSettingsActivity.this.gifsRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("LocalGifCache", 2131493769), true);
                return;
              }
            } while (paramInt != DataSettingsActivity.this.quickRepliesRow);
            ((TextSettingsCell)localObject).setText(LocaleController.getString("VoipQuickReplies", 2131494612), false);
            return;
            paramViewHolder = (HeaderCell)paramViewHolder.itemView;
            if (paramInt == DataSettingsActivity.this.mediaDownloadSectionRow)
            {
              paramViewHolder.setText(LocaleController.getString("AutomaticMediaDownload", 2131493073));
              return;
            }
            if (paramInt == DataSettingsActivity.this.usageSectionRow)
            {
              paramViewHolder.setText(LocaleController.getString("DataUsage", 2131493329));
              return;
            }
            if (paramInt == DataSettingsActivity.this.callsSectionRow)
            {
              paramViewHolder.setText(LocaleController.getString("Calls", 2131493124));
              return;
            }
            if (paramInt == DataSettingsActivity.this.proxySectionRow)
            {
              paramViewHolder.setText(LocaleController.getString("Proxy", 2131494206));
              return;
            }
          } while (paramInt != DataSettingsActivity.this.streamSectionRow);
          paramViewHolder.setText(LocaleController.getString("Streaming", 2131494443));
          return;
          paramViewHolder = (TextCheckCell)paramViewHolder.itemView;
          if (paramInt == DataSettingsActivity.this.autoDownloadMediaRow)
          {
            paramViewHolder.setTextAndCheck(LocaleController.getString("AutoDownloadMedia", 2131493049), DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled, true);
            return;
          }
          if (paramInt == DataSettingsActivity.this.enableStreamRow)
          {
            localObject = LocaleController.getString("EnableStreaming", 2131493427);
            boolean bool2 = SharedConfig.streamMedia;
            if (DataSettingsActivity.this.enableAllStreamRow != -1) {}
            for (;;)
            {
              paramViewHolder.setTextAndCheck((String)localObject, bool2, bool1);
              return;
              bool1 = false;
            }
          }
        } while ((paramInt == DataSettingsActivity.this.enableCacheStreamRow) || (paramInt != DataSettingsActivity.this.enableAllStreamRow));
        paramViewHolder.setTextAndCheck("Try to Stream All Videos", SharedConfig.streamAllVideo, false);
        return;
        paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
      } while (paramInt != DataSettingsActivity.this.enableAllStreamInfoRow);
      paramViewHolder.setText(LocaleController.getString("EnableAllStreamingInfo", 2131493423));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      switch (paramInt)
      {
      }
      for (;;)
      {
        paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new ShadowSectionCell(this.mContext);
        continue;
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextCheckCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
      }
    }
    
    public void onViewAttachedToWindow(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      if (i == 1)
      {
        i = paramViewHolder.getAdapterPosition();
        paramViewHolder = (TextSettingsCell)paramViewHolder.itemView;
        if ((i >= DataSettingsActivity.this.photosRow) && (i <= DataSettingsActivity.this.gifsRow)) {
          paramViewHolder.setEnabled(DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled, null);
        }
      }
      TextCheckCell localTextCheckCell;
      do
      {
        do
        {
          return;
          paramViewHolder.setEnabled(true, null);
          return;
        } while (i != 3);
        localTextCheckCell = (TextCheckCell)paramViewHolder.itemView;
        i = paramViewHolder.getAdapterPosition();
        if (i == DataSettingsActivity.this.enableCacheStreamRow)
        {
          localTextCheckCell.setChecked(SharedConfig.saveStreamMedia);
          return;
        }
        if (i == DataSettingsActivity.this.enableStreamRow)
        {
          localTextCheckCell.setChecked(SharedConfig.streamMedia);
          return;
        }
        if (i == DataSettingsActivity.this.autoDownloadMediaRow)
        {
          localTextCheckCell.setChecked(DownloadController.getInstance(DataSettingsActivity.this.currentAccount).globalAutodownloadEnabled);
          return;
        }
      } while (i != DataSettingsActivity.this.enableAllStreamRow);
      localTextCheckCell.setChecked(SharedConfig.streamAllVideo);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/DataSettingsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */