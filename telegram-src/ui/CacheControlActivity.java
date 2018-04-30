package org.telegram.ui;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.io.File;
import java.util.ArrayList;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.SQLite.SQLitePreparedStatement;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ClearCacheService;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class CacheControlActivity
  extends BaseFragment
{
  private long audioSize = -1L;
  private int cacheInfoRow;
  private int cacheRow;
  private long cacheSize = -1L;
  private boolean calculating = true;
  private volatile boolean canceled = false;
  private boolean[] clear = new boolean[6];
  private int databaseInfoRow;
  private int databaseRow;
  private long databaseSize = -1L;
  private long documentsSize = -1L;
  private int keepMediaInfoRow;
  private int keepMediaRow;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private long musicSize = -1L;
  private long photoSize = -1L;
  private int rowCount;
  private long totalSize = -1L;
  private long videoSize = -1L;
  
  private void cleanupFolders()
  {
    final AlertDialog localAlertDialog = new AlertDialog(getParentActivity(), 1);
    localAlertDialog.setMessage(LocaleController.getString("Loading", 2131493762));
    localAlertDialog.setCanceledOnTouchOutside(false);
    localAlertDialog.setCancelable(false);
    localAlertDialog.show();
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final boolean bool2 = false;
        int k = 0;
        if (k < 6)
        {
          boolean bool1;
          if (CacheControlActivity.this.clear[k] == 0) {
            bool1 = bool2;
          }
          for (;;)
          {
            k += 1;
            bool2 = bool1;
            break;
            int i = -1;
            int m = 0;
            int j;
            if (k == 0)
            {
              i = 0;
              j = m;
            }
            for (;;)
            {
              bool1 = bool2;
              if (i == -1) {
                break;
              }
              File localFile = FileLoader.checkDirectory(i);
              if (localFile != null) {
                Utilities.clearDir(localFile.getAbsolutePath(), j, Long.MAX_VALUE);
              }
              if (i != 4) {
                break label181;
              }
              CacheControlActivity.access$002(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(4), j));
              bool1 = true;
              break;
              if (k == 1)
              {
                i = 2;
                j = m;
              }
              else if (k == 2)
              {
                i = 3;
                j = 1;
              }
              else if (k == 3)
              {
                i = 3;
                j = 2;
              }
              else if (k == 4)
              {
                i = 1;
                j = m;
              }
              else
              {
                j = m;
                if (k == 5)
                {
                  i = 4;
                  j = m;
                }
              }
            }
            label181:
            if (i == 1)
            {
              CacheControlActivity.access$702(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(1), j));
              bool1 = bool2;
            }
            else if (i == 3)
            {
              if (j == 1)
              {
                CacheControlActivity.access$502(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), j));
                bool1 = bool2;
              }
              else
              {
                CacheControlActivity.access$602(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), j));
                bool1 = bool2;
              }
            }
            else if (i == 0)
            {
              bool1 = true;
              CacheControlActivity.access$302(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(0), j));
            }
            else
            {
              bool1 = bool2;
              if (i == 2)
              {
                CacheControlActivity.access$402(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(2), j));
                bool1 = bool2;
              }
            }
          }
        }
        CacheControlActivity.access$802(CacheControlActivity.this, CacheControlActivity.this.cacheSize + CacheControlActivity.this.videoSize + CacheControlActivity.this.audioSize + CacheControlActivity.this.photoSize + CacheControlActivity.this.documentsSize + CacheControlActivity.this.musicSize);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (bool2) {
              ImageLoader.getInstance().clearMemory();
            }
            if (CacheControlActivity.this.listAdapter != null) {
              CacheControlActivity.this.listAdapter.notifyDataSetChanged();
            }
            try
            {
              CacheControlActivity.2.this.val$progressDialog.dismiss();
              return;
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
            }
          }
        });
      }
    });
  }
  
  private long getDirectorySize(File paramFile, int paramInt)
  {
    if ((paramFile == null) || (this.canceled)) {}
    do
    {
      return 0L;
      if (paramFile.isDirectory()) {
        return Utilities.getDirSize(paramFile.getAbsolutePath(), paramInt);
      }
    } while (!paramFile.isFile());
    return 0L + paramFile.length();
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("StorageUsage", 2131494442));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          CacheControlActivity.this.finishFragment();
        }
      }
    });
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    localFrameLayout.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        if (CacheControlActivity.this.getParentActivity() == null) {}
        Object localObject3;
        do
        {
          return;
          if (paramAnonymousInt == CacheControlActivity.this.keepMediaRow)
          {
            paramAnonymousView = new BottomSheet.Builder(CacheControlActivity.this.getParentActivity());
            localObject1 = LocaleController.formatPluralString("Days", 3);
            localObject2 = LocaleController.formatPluralString("Weeks", 1);
            localObject3 = LocaleController.formatPluralString("Months", 1);
            String str = LocaleController.getString("KeepMediaForever", 2131493716);
            DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                paramAnonymous2DialogInterface = MessagesController.getGlobalMainSettings().edit();
                if (paramAnonymous2Int == 0) {
                  paramAnonymous2DialogInterface.putInt("keep_media", 3).commit();
                }
                AlarmManager localAlarmManager;
                for (;;)
                {
                  if (CacheControlActivity.this.listAdapter != null) {
                    CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                  }
                  paramAnonymous2DialogInterface = PendingIntent.getService(ApplicationLoader.applicationContext, 1, new Intent(ApplicationLoader.applicationContext, ClearCacheService.class), 0);
                  localAlarmManager = (AlarmManager)ApplicationLoader.applicationContext.getSystemService("alarm");
                  if (paramAnonymous2Int != 2) {
                    break;
                  }
                  localAlarmManager.cancel(paramAnonymous2DialogInterface);
                  return;
                  if (paramAnonymous2Int == 1) {
                    paramAnonymous2DialogInterface.putInt("keep_media", 0).commit();
                  } else if (paramAnonymous2Int == 2) {
                    paramAnonymous2DialogInterface.putInt("keep_media", 1).commit();
                  } else if (paramAnonymous2Int == 3) {
                    paramAnonymous2DialogInterface.putInt("keep_media", 2).commit();
                  }
                }
                localAlarmManager.setInexactRepeating(2, 86400000L, 86400000L, paramAnonymous2DialogInterface);
              }
            };
            paramAnonymousView.setItems(new CharSequence[] { localObject1, localObject2, localObject3, str }, local1);
            CacheControlActivity.this.showDialog(paramAnonymousView.create());
            return;
          }
          if (paramAnonymousInt == CacheControlActivity.this.databaseRow)
          {
            paramAnonymousView = new AlertDialog.Builder(CacheControlActivity.this.getParentActivity());
            paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            paramAnonymousView.setMessage(LocaleController.getString("LocalDatabaseClear", 2131493766));
            paramAnonymousView.setPositiveButton(LocaleController.getString("CacheClear", 2131493102), new DialogInterface.OnClickListener()
            {
              public void onClick(final DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                paramAnonymous2DialogInterface = new AlertDialog(CacheControlActivity.this.getParentActivity(), 1);
                paramAnonymous2DialogInterface.setMessage(LocaleController.getString("Loading", 2131493762));
                paramAnonymous2DialogInterface.setCanceledOnTouchOutside(false);
                paramAnonymous2DialogInterface.setCancelable(false);
                paramAnonymous2DialogInterface.show();
                MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                {
                  public void run()
                  {
                    for (;;)
                    {
                      Object localObject2;
                      int j;
                      SQLitePreparedStatement localSQLitePreparedStatement;
                      try
                      {
                        SQLiteDatabase localSQLiteDatabase = MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getDatabase();
                        ArrayList localArrayList = new ArrayList();
                        localObject2 = localSQLiteDatabase.queryFinalized("SELECT did FROM dialogs WHERE 1", new Object[0]);
                        new StringBuilder();
                        long l1;
                        int i;
                        if (((SQLiteCursor)localObject2).next())
                        {
                          l1 = ((SQLiteCursor)localObject2).longValue(0);
                          i = (int)l1;
                          j = (int)(l1 >> 32);
                          if ((i == 0) || (j == 1)) {
                            continue;
                          }
                          localArrayList.add(Long.valueOf(l1));
                          continue;
                        }
                        Long localLong;
                        SQLiteCursor localSQLiteCursor1;
                        long l2;
                        SQLiteCursor localSQLiteCursor2;
                        int k;
                        NativeByteBuffer localNativeByteBuffer;
                        TLRPC.Message localMessage;
                        ((SQLiteDatabase)localObject1).executeFast("DELETE FROM messages WHERE uid = " + -1117744619L).stepThis().dispose();
                      }
                      catch (Exception localException1)
                      {
                        FileLog.e(localException1);
                        return;
                        ((SQLiteCursor)localObject2).dispose();
                        localObject2 = localException1.executeFast("REPLACE INTO messages_holes VALUES(?, ?, ?)");
                        localSQLitePreparedStatement = localException1.executeFast("REPLACE INTO media_holes_v2 VALUES(?, ?, ?, ?)");
                        localException1.beginTransaction();
                        j = 0;
                        if (j < localArrayList.size())
                        {
                          localLong = (Long)localArrayList.get(j);
                          i = 0;
                          localSQLiteCursor1 = localException1.queryFinalized("SELECT COUNT(mid) FROM messages WHERE uid = " + localLong, new Object[0]);
                          if (localSQLiteCursor1.next()) {
                            i = localSQLiteCursor1.intValue(0);
                          }
                          localSQLiteCursor1.dispose();
                          if (i <= 2) {
                            break label973;
                          }
                          localSQLiteCursor1 = localException1.queryFinalized("SELECT last_mid_i, last_mid FROM dialogs WHERE did = " + localLong, new Object[0]);
                          i = -1;
                          if (localSQLiteCursor1.next())
                          {
                            l1 = localSQLiteCursor1.longValue(0);
                            l2 = localSQLiteCursor1.longValue(1);
                            localSQLiteCursor2 = localException1.queryFinalized("SELECT data FROM messages WHERE uid = " + localLong + " AND mid IN (" + l1 + "," + l2 + ")", new Object[0]);
                            k = i;
                            try
                            {
                              if (localSQLiteCursor2.next())
                              {
                                localNativeByteBuffer = localSQLiteCursor2.byteBufferValue(0);
                                i = k;
                                if (localNativeByteBuffer == null) {
                                  continue;
                                }
                                localMessage = TLRPC.Message.TLdeserialize(localNativeByteBuffer, localNativeByteBuffer.readInt32(false), false);
                                localMessage.readAttachPath(localNativeByteBuffer, UserConfig.getInstance(CacheControlActivity.this.currentAccount).clientUserId);
                                localNativeByteBuffer.reuse();
                                i = k;
                                if (localMessage == null) {
                                  continue;
                                }
                                i = localMessage.id;
                              }
                            }
                            catch (Exception localException2)
                            {
                              FileLog.e(localException2);
                              localSQLiteCursor2.dispose();
                              localException1.executeFast("DELETE FROM messages WHERE uid = " + localLong + " AND mid != " + l1 + " AND mid != " + l2).stepThis().dispose();
                              localException1.executeFast("DELETE FROM messages_holes WHERE uid = " + localLong).stepThis().dispose();
                              localException1.executeFast("DELETE FROM bot_keyboard WHERE uid = " + localLong).stepThis().dispose();
                              localException1.executeFast("DELETE FROM media_counts_v2 WHERE uid = " + localLong).stepThis().dispose();
                              localException1.executeFast("DELETE FROM media_v2 WHERE uid = " + localLong).stepThis().dispose();
                              localException1.executeFast("DELETE FROM media_holes_v2 WHERE uid = " + localLong).stepThis().dispose();
                              DataQuery.getInstance(CacheControlActivity.this.currentAccount).clearBotKeyboard(localLong.longValue(), null);
                              if (k != -1) {
                                MessagesStorage.createFirstHoles(localLong.longValue(), (SQLitePreparedStatement)localObject2, localSQLitePreparedStatement, k);
                              }
                            }
                          }
                          localSQLiteCursor1.dispose();
                        }
                      }
                      finally
                      {
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            try
                            {
                              CacheControlActivity.4.2.1.this.val$progressDialog.dismiss();
                              if (CacheControlActivity.this.listAdapter != null)
                              {
                                CacheControlActivity.access$1702(CacheControlActivity.this, MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getDatabaseSize());
                                CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                              }
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
                        });
                      }
                      ((SQLiteDatabase)localObject1).executeFast("DELETE FROM messages_holes WHERE uid = " + -1117744619L).stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("DELETE FROM bot_keyboard WHERE uid = " + -1117744619L).stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("DELETE FROM media_counts_v2 WHERE uid = " + -1117744619L).stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("DELETE FROM media_v2 WHERE uid = " + -1117744619L).stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("DELETE FROM media_holes_v2 WHERE uid = " + -1117744619L).stepThis().dispose();
                      ((SQLitePreparedStatement)localObject2).dispose();
                      localSQLitePreparedStatement.dispose();
                      ((SQLiteDatabase)localObject1).commitTransaction();
                      ((SQLiteDatabase)localObject1).executeFast("PRAGMA journal_size_limit = 0").stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("VACUUM").stepThis().dispose();
                      ((SQLiteDatabase)localObject1).executeFast("PRAGMA journal_size_limit = -1").stepThis().dispose();
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          try
                          {
                            CacheControlActivity.4.2.1.this.val$progressDialog.dismiss();
                            if (CacheControlActivity.this.listAdapter != null)
                            {
                              CacheControlActivity.access$1702(CacheControlActivity.this, MessagesStorage.getInstance(CacheControlActivity.this.currentAccount).getDatabaseSize());
                              CacheControlActivity.this.listAdapter.notifyDataSetChanged();
                            }
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
                      });
                      return;
                      label973:
                      j += 1;
                    }
                  }
                });
              }
            });
            CacheControlActivity.this.showDialog(paramAnonymousView.create());
            return;
          }
        } while ((paramAnonymousInt != CacheControlActivity.this.cacheRow) || (CacheControlActivity.this.totalSize <= 0L) || (CacheControlActivity.this.getParentActivity() == null));
        Object localObject1 = new BottomSheet.Builder(CacheControlActivity.this.getParentActivity());
        ((BottomSheet.Builder)localObject1).setApplyTopPadding(false);
        ((BottomSheet.Builder)localObject1).setApplyBottomPadding(false);
        Object localObject2 = new LinearLayout(CacheControlActivity.this.getParentActivity());
        ((LinearLayout)localObject2).setOrientation(1);
        paramAnonymousInt = 0;
        if (paramAnonymousInt < 6)
        {
          long l = 0L;
          paramAnonymousView = null;
          if (paramAnonymousInt == 0)
          {
            l = CacheControlActivity.this.photoSize;
            paramAnonymousView = LocaleController.getString("LocalPhotoCache", 2131493771);
            label337:
            if (l <= 0L) {
              break label563;
            }
            CacheControlActivity.this.clear[paramAnonymousInt] = 1;
            localObject3 = new CheckBoxCell(CacheControlActivity.this.getParentActivity(), 1);
            ((CheckBoxCell)localObject3).setTag(Integer.valueOf(paramAnonymousInt));
            ((CheckBoxCell)localObject3).setBackgroundDrawable(Theme.getSelectorDrawable(false));
            ((LinearLayout)localObject2).addView((View)localObject3, LayoutHelper.createLinear(-1, 48));
            ((CheckBoxCell)localObject3).setText(paramAnonymousView, AndroidUtilities.formatFileSize(l), true, true);
            ((CheckBoxCell)localObject3).setTextColor(Theme.getColor("dialogTextBlack"));
            ((CheckBoxCell)localObject3).setOnClickListener(new View.OnClickListener()
            {
              public void onClick(View paramAnonymous2View)
              {
                paramAnonymous2View = (CheckBoxCell)paramAnonymous2View;
                int i = ((Integer)paramAnonymous2View.getTag()).intValue();
                boolean[] arrayOfBoolean = CacheControlActivity.this.clear;
                if (CacheControlActivity.this.clear[i] == 0) {}
                for (int j = 1;; j = 0)
                {
                  arrayOfBoolean[i] = j;
                  paramAnonymous2View.setChecked(CacheControlActivity.this.clear[i], true);
                  return;
                }
              }
            });
          }
          for (;;)
          {
            paramAnonymousInt += 1;
            break;
            if (paramAnonymousInt == 1)
            {
              l = CacheControlActivity.this.videoSize;
              paramAnonymousView = LocaleController.getString("LocalVideoCache", 2131493772);
              break label337;
            }
            if (paramAnonymousInt == 2)
            {
              l = CacheControlActivity.this.documentsSize;
              paramAnonymousView = LocaleController.getString("LocalDocumentCache", 2131493768);
              break label337;
            }
            if (paramAnonymousInt == 3)
            {
              l = CacheControlActivity.this.musicSize;
              paramAnonymousView = LocaleController.getString("LocalMusicCache", 2131493770);
              break label337;
            }
            if (paramAnonymousInt == 4)
            {
              l = CacheControlActivity.this.audioSize;
              paramAnonymousView = LocaleController.getString("LocalAudioCache", 2131493763);
              break label337;
            }
            if (paramAnonymousInt != 5) {
              break label337;
            }
            l = CacheControlActivity.this.cacheSize;
            paramAnonymousView = LocaleController.getString("LocalCache", 2131493764);
            break label337;
            label563:
            CacheControlActivity.this.clear[paramAnonymousInt] = 0;
          }
        }
        paramAnonymousView = new BottomSheet.BottomSheetCell(CacheControlActivity.this.getParentActivity(), 1);
        paramAnonymousView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        paramAnonymousView.setTextAndIcon(LocaleController.getString("ClearMediaCache", 2131493260).toUpperCase(), 0);
        paramAnonymousView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText"));
        paramAnonymousView.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymous2View)
          {
            try
            {
              if (CacheControlActivity.this.visibleDialog != null) {
                CacheControlActivity.this.visibleDialog.dismiss();
              }
              CacheControlActivity.this.cleanupFolders();
              return;
            }
            catch (Exception paramAnonymous2View)
            {
              for (;;)
              {
                FileLog.e(paramAnonymous2View);
              }
            }
          }
        });
        ((LinearLayout)localObject2).addView(paramAnonymousView, LayoutHelper.createLinear(-1, 48));
        ((BottomSheet.Builder)localObject1).setCustomView((View)localObject2);
        CacheControlActivity.this.showDialog(((BottomSheet.Builder)localObject1).create());
      }
    });
    return this.fragmentView;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { TextSettingsCell.class }, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.keepMediaRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.keepMediaInfoRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.cacheRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.cacheInfoRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.databaseRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.databaseInfoRow = i;
    this.databaseSize = MessagesStorage.getInstance(this.currentAccount).getDatabaseSize();
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        CacheControlActivity.access$002(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(4), 0));
        if (CacheControlActivity.this.canceled) {}
        do
        {
          do
          {
            do
            {
              do
              {
                return;
                CacheControlActivity.access$302(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(0), 0));
              } while (CacheControlActivity.this.canceled);
              CacheControlActivity.access$402(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(2), 0));
            } while (CacheControlActivity.this.canceled);
            CacheControlActivity.access$502(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), 1));
          } while (CacheControlActivity.this.canceled);
          CacheControlActivity.access$602(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(3), 2));
        } while (CacheControlActivity.this.canceled);
        CacheControlActivity.access$702(CacheControlActivity.this, CacheControlActivity.this.getDirectorySize(FileLoader.checkDirectory(1), 0));
        CacheControlActivity.access$802(CacheControlActivity.this, CacheControlActivity.this.cacheSize + CacheControlActivity.this.videoSize + CacheControlActivity.this.audioSize + CacheControlActivity.this.photoSize + CacheControlActivity.this.documentsSize + CacheControlActivity.this.musicSize);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            CacheControlActivity.access$902(CacheControlActivity.this, false);
            if (CacheControlActivity.this.listAdapter != null) {
              CacheControlActivity.this.listAdapter.notifyDataSetChanged();
            }
          }
        });
      }
    });
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    this.canceled = true;
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
      return CacheControlActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == CacheControlActivity.this.databaseInfoRow) || (paramInt == CacheControlActivity.this.cacheInfoRow) || (paramInt == CacheControlActivity.this.keepMediaInfoRow)) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      return (i == CacheControlActivity.this.databaseRow) || ((i == CacheControlActivity.this.cacheRow) && (CacheControlActivity.this.totalSize > 0L)) || (i == CacheControlActivity.this.keepMediaRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      do
      {
        TextSettingsCell localTextSettingsCell;
        do
        {
          return;
          localTextSettingsCell = (TextSettingsCell)paramViewHolder.itemView;
          if (paramInt == CacheControlActivity.this.databaseRow)
          {
            localTextSettingsCell.setTextAndValue(LocaleController.getString("LocalDatabase", 2131493765), AndroidUtilities.formatFileSize(CacheControlActivity.this.databaseSize), false);
            return;
          }
          if (paramInt == CacheControlActivity.this.cacheRow)
          {
            if (CacheControlActivity.this.calculating)
            {
              localTextSettingsCell.setTextAndValue(LocaleController.getString("ClearMediaCache", 2131493260), LocaleController.getString("CalculatingSize", 2131493104), false);
              return;
            }
            String str = LocaleController.getString("ClearMediaCache", 2131493260);
            if (CacheControlActivity.this.totalSize == 0L) {}
            for (paramViewHolder = LocaleController.getString("CacheEmpty", 2131493103);; paramViewHolder = AndroidUtilities.formatFileSize(CacheControlActivity.this.totalSize))
            {
              localTextSettingsCell.setTextAndValue(str, paramViewHolder, false);
              return;
            }
          }
        } while (paramInt != CacheControlActivity.this.keepMediaRow);
        paramInt = MessagesController.getGlobalMainSettings().getInt("keep_media", 2);
        if (paramInt == 0) {
          paramViewHolder = LocaleController.formatPluralString("Weeks", 1);
        }
        for (;;)
        {
          localTextSettingsCell.setTextAndValue(LocaleController.getString("KeepMedia", 2131493715), paramViewHolder, false);
          return;
          if (paramInt == 1) {
            paramViewHolder = LocaleController.formatPluralString("Months", 1);
          } else if (paramInt == 3) {
            paramViewHolder = LocaleController.formatPluralString("Days", 3);
          } else {
            paramViewHolder = LocaleController.getString("KeepMediaForever", 2131493716);
          }
        }
        paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
        if (paramInt == CacheControlActivity.this.databaseInfoRow)
        {
          paramViewHolder.setText(LocaleController.getString("LocalDatabaseInfo", 2131493767));
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
          return;
        }
        if (paramInt == CacheControlActivity.this.cacheInfoRow)
        {
          paramViewHolder.setText("");
          paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
          return;
        }
      } while (paramInt != CacheControlActivity.this.keepMediaInfoRow);
      paramViewHolder.setText(AndroidUtilities.replaceTags(LocaleController.getString("KeepMediaInfo", 2131493717)));
      paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165331, "windowBackgroundGrayShadow"));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new TextSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/CacheControlActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */