package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_account_getWallPapers;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_wallPaper;
import org.telegram.tgnet.TLRPC.TL_wallPaperSolid;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.WallPaper;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.WallpaperCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.WallpaperUpdater;
import org.telegram.ui.Components.WallpaperUpdater.WallpaperUpdaterDelegate;

public class WallpapersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int done_button = 1;
  private ImageView backgroundImage;
  private View doneButton;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private String loadingFile = null;
  private File loadingFileObject = null;
  private TLRPC.PhotoSize loadingSize = null;
  private boolean overrideThemeWallpaper;
  private FrameLayout progressView;
  private View progressViewBackground;
  private int selectedBackground;
  private int selectedColor;
  private Drawable themedWallpaper;
  private WallpaperUpdater updater;
  private ArrayList<TLRPC.WallPaper> wallPapers = new ArrayList();
  private File wallpaperFile;
  private SparseArray<TLRPC.WallPaper> wallpappersByIds = new SparseArray();
  
  private void loadWallpapers()
  {
    TLRPC.TL_account_getWallPapers localTL_account_getWallPapers = new TLRPC.TL_account_getWallPapers();
    int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_getWallPapers, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            WallpapersActivity.this.wallPapers.clear();
            Object localObject1 = (TLRPC.Vector)paramAnonymousTLObject;
            WallpapersActivity.this.wallpappersByIds.clear();
            localObject1 = ((TLRPC.Vector)localObject1).objects.iterator();
            while (((Iterator)localObject1).hasNext())
            {
              Object localObject2 = ((Iterator)localObject1).next();
              WallpapersActivity.this.wallPapers.add((TLRPC.WallPaper)localObject2);
              WallpapersActivity.this.wallpappersByIds.put(((TLRPC.WallPaper)localObject2).id, (TLRPC.WallPaper)localObject2);
            }
            if (WallpapersActivity.this.listAdapter != null) {
              WallpapersActivity.this.listAdapter.notifyDataSetChanged();
            }
            if (WallpapersActivity.this.backgroundImage != null) {
              WallpapersActivity.this.processSelectedBackground();
            }
            MessagesStorage.getInstance(WallpapersActivity.this.currentAccount).putWallpapers(WallpapersActivity.this.wallPapers);
          }
        });
      }
    });
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, this.classGuid);
  }
  
  private void processSelectedBackground()
  {
    if ((Theme.hasWallpaperFromTheme()) && (!this.overrideThemeWallpaper)) {
      this.backgroundImage.setImageDrawable(Theme.getThemedWallpaper(false));
    }
    Object localObject;
    do
    {
      return;
      localObject = (TLRPC.WallPaper)this.wallpappersByIds.get(this.selectedBackground);
      if ((this.selectedBackground == -1) || (this.selectedBackground == 1000001) || (localObject == null) || (!(localObject instanceof TLRPC.TL_wallPaper))) {
        break;
      }
      int i = AndroidUtilities.displaySize.x;
      int k = AndroidUtilities.displaySize.y;
      int m = k;
      int j = i;
      if (i > k)
      {
        j = k;
        m = i;
      }
      localObject = FileLoader.getClosestPhotoSizeWithSize(((TLRPC.WallPaper)localObject).sizes, Math.min(j, m));
    } while (localObject == null);
    String str = ((TLRPC.PhotoSize)localObject).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject).location.local_id + ".jpg";
    File localFile2 = new File(FileLoader.getDirectory(4), str);
    if (!localFile2.exists())
    {
      int[] arrayOfInt = AndroidUtilities.calcDrawableColor(this.backgroundImage.getDrawable());
      this.progressViewBackground.getBackground().setColorFilter(new PorterDuffColorFilter(arrayOfInt[0], PorterDuff.Mode.MULTIPLY));
      this.loadingFile = str;
      this.loadingFileObject = localFile2;
      this.doneButton.setEnabled(false);
      this.progressView.setVisibility(0);
      this.loadingSize = ((TLRPC.PhotoSize)localObject);
      this.selectedColor = 0;
      FileLoader.getInstance(this.currentAccount).loadFile((TLRPC.PhotoSize)localObject, null, 1);
      this.backgroundImage.setBackgroundColor(0);
      return;
    }
    if (this.loadingFile != null) {
      FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.loadingSize);
    }
    this.loadingFileObject = null;
    this.loadingFile = null;
    this.loadingSize = null;
    try
    {
      this.backgroundImage.setImageURI(Uri.fromFile(localFile2));
      this.backgroundImage.setBackgroundColor(0);
      this.selectedColor = 0;
      this.doneButton.setEnabled(true);
      this.progressView.setVisibility(8);
      return;
    }
    catch (Throwable localThrowable)
    {
      for (;;)
      {
        FileLog.e(localThrowable);
      }
    }
    if (this.loadingFile != null) {
      FileLoader.getInstance(this.currentAccount).cancelLoadFile(this.loadingSize);
    }
    if (this.selectedBackground == 1000001)
    {
      this.backgroundImage.setImageResource(2131165226);
      this.backgroundImage.setBackgroundColor(0);
      this.selectedColor = 0;
    }
    for (;;)
    {
      this.loadingFileObject = null;
      this.loadingFile = null;
      this.loadingSize = null;
      this.doneButton.setEnabled(true);
      this.progressView.setVisibility(8);
      return;
      File localFile1;
      if (this.selectedBackground == -1)
      {
        if (this.wallpaperFile != null) {}
        for (localFile1 = this.wallpaperFile;; localFile1 = new File(ApplicationLoader.getFilesDirFixed(), "wallpaper.jpg"))
        {
          if (!localFile1.exists()) {
            break label523;
          }
          this.backgroundImage.setImageURI(Uri.fromFile(localFile1));
          break;
        }
        label523:
        this.selectedBackground = 1000001;
        this.overrideThemeWallpaper = true;
        processSelectedBackground();
      }
      else
      {
        if (localFile1 == null) {
          break;
        }
        if ((localFile1 instanceof TLRPC.TL_wallPaperSolid))
        {
          this.backgroundImage.getDrawable();
          this.backgroundImage.setImageBitmap(null);
          this.selectedColor = (0xFF000000 | localFile1.bg_color);
          this.backgroundImage.setBackgroundColor(this.selectedColor);
        }
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    this.themedWallpaper = Theme.getThemedWallpaper(true);
    this.updater = new WallpaperUpdater(getParentActivity(), new WallpaperUpdater.WallpaperUpdaterDelegate()
    {
      public void didSelectWallpaper(File paramAnonymousFile, Bitmap paramAnonymousBitmap)
      {
        WallpapersActivity.access$002(WallpapersActivity.this, -1);
        WallpapersActivity.access$102(WallpapersActivity.this, true);
        WallpapersActivity.access$202(WallpapersActivity.this, 0);
        WallpapersActivity.access$302(WallpapersActivity.this, paramAnonymousFile);
        WallpapersActivity.this.backgroundImage.getDrawable();
        WallpapersActivity.this.backgroundImage.setImageBitmap(paramAnonymousBitmap);
      }
      
      public void needOpenColorPicker() {}
    });
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("ChatBackground", 2131493220));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          WallpapersActivity.this.finishFragment();
        }
        while (paramAnonymousInt != 1) {
          return;
        }
        Object localObject = (TLRPC.WallPaper)WallpapersActivity.this.wallpappersByIds.get(WallpapersActivity.this.selectedBackground);
        File localFile2;
        if ((localObject != null) && (((TLRPC.WallPaper)localObject).id != 1000001) && ((localObject instanceof TLRPC.TL_wallPaper)))
        {
          paramAnonymousInt = AndroidUtilities.displaySize.x;
          int j = AndroidUtilities.displaySize.y;
          int k = j;
          int i = paramAnonymousInt;
          if (paramAnonymousInt > j)
          {
            i = j;
            k = paramAnonymousInt;
          }
          localObject = FileLoader.getClosestPhotoSizeWithSize(((TLRPC.WallPaper)localObject).sizes, Math.min(i, k));
          localObject = ((TLRPC.PhotoSize)localObject).location.volume_id + "_" + ((TLRPC.PhotoSize)localObject).location.local_id + ".jpg";
          localObject = new File(FileLoader.getDirectory(4), (String)localObject);
          localFile2 = new File(ApplicationLoader.getFilesDirFixed(), "wallpaper.jpg");
        }
        for (;;)
        {
          boolean bool;
          try
          {
            bool = AndroidUtilities.copyFile((File)localObject, localFile2);
            if (bool)
            {
              localObject = MessagesController.getGlobalMainSettings().edit();
              ((SharedPreferences.Editor)localObject).putInt("selectedBackground", WallpapersActivity.this.selectedBackground);
              ((SharedPreferences.Editor)localObject).putInt("selectedColor", WallpapersActivity.this.selectedColor);
              if ((!Theme.hasWallpaperFromTheme()) || (!WallpapersActivity.this.overrideThemeWallpaper)) {
                break label370;
              }
              bool = true;
              ((SharedPreferences.Editor)localObject).putBoolean("overrideThemeWallpaper", bool);
              ((SharedPreferences.Editor)localObject).commit();
              Theme.reloadWallpaper();
            }
            WallpapersActivity.this.finishFragment();
            return;
          }
          catch (Exception localException1)
          {
            bool = false;
            FileLog.e(localException1);
            continue;
          }
          if (WallpapersActivity.this.selectedBackground == -1)
          {
            File localFile1 = WallpapersActivity.this.updater.getCurrentWallpaperPath();
            localFile2 = new File(ApplicationLoader.getFilesDirFixed(), "wallpaper.jpg");
            try
            {
              bool = AndroidUtilities.copyFile(localFile1, localFile2);
            }
            catch (Exception localException2)
            {
              bool = false;
              FileLog.e(localException2);
            }
          }
          else
          {
            bool = true;
            continue;
            label370:
            bool = false;
          }
        }
      }
    });
    this.doneButton = this.actionBar.createMenu().addItemWithWidth(1, 2131165376, AndroidUtilities.dp(56.0F));
    FrameLayout localFrameLayout = new FrameLayout(paramContext);
    this.fragmentView = localFrameLayout;
    this.backgroundImage = new ImageView(paramContext);
    this.backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    localFrameLayout.addView(this.backgroundImage, LayoutHelper.createFrame(-1, -1.0F));
    this.backgroundImage.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.progressView = new FrameLayout(paramContext);
    this.progressView.setVisibility(4);
    localFrameLayout.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 52.0F));
    this.progressViewBackground = new View(paramContext);
    this.progressViewBackground.setBackgroundResource(2131165672);
    this.progressView.addView(this.progressViewBackground, LayoutHelper.createFrame(36, 36, 17));
    Object localObject = new RadialProgressView(paramContext);
    ((RadialProgressView)localObject).setSize(AndroidUtilities.dp(28.0F));
    ((RadialProgressView)localObject).setProgressColor(-1);
    this.progressView.addView((View)localObject, LayoutHelper.createFrame(32, 32, 17));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setClipToPadding(false);
    this.listView.setTag(Integer.valueOf(8));
    this.listView.setPadding(AndroidUtilities.dp(40.0F), 0, AndroidUtilities.dp(40.0F), 0);
    localObject = new LinearLayoutManager(paramContext);
    ((LinearLayoutManager)localObject).setOrientation(0);
    this.listView.setLayoutManager((RecyclerView.LayoutManager)localObject);
    this.listView.setDisallowInterceptTouchEvents(true);
    this.listView.setOverScrollMode(2);
    localObject = this.listView;
    paramContext = new ListAdapter(paramContext);
    this.listAdapter = paramContext;
    ((RecyclerListView)localObject).setAdapter(paramContext);
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, 102, 83));
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        if (paramAnonymousInt == 0)
        {
          WallpapersActivity.this.updater.showAlert(false);
          return;
        }
        if (Theme.hasWallpaperFromTheme())
        {
          if (paramAnonymousInt == 1)
          {
            WallpapersActivity.access$002(WallpapersActivity.this, -2);
            WallpapersActivity.access$102(WallpapersActivity.this, false);
            WallpapersActivity.this.listAdapter.notifyDataSetChanged();
            WallpapersActivity.this.processSelectedBackground();
            return;
          }
          paramAnonymousInt -= 2;
        }
        for (;;)
        {
          paramAnonymousView = (TLRPC.WallPaper)WallpapersActivity.this.wallPapers.get(paramAnonymousInt);
          WallpapersActivity.access$002(WallpapersActivity.this, paramAnonymousView.id);
          WallpapersActivity.access$102(WallpapersActivity.this, true);
          WallpapersActivity.this.listAdapter.notifyDataSetChanged();
          WallpapersActivity.this.processSelectedBackground();
          return;
          paramAnonymousInt -= 1;
        }
      }
    });
    processSelectedBackground();
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.FileDidFailedLoad)
    {
      paramVarArgs = (String)paramVarArgs[0];
      if ((this.loadingFile != null) && (this.loadingFile.equals(paramVarArgs)))
      {
        this.loadingFileObject = null;
        this.loadingFile = null;
        this.loadingSize = null;
        this.progressView.setVisibility(8);
        this.doneButton.setEnabled(false);
      }
    }
    do
    {
      do
      {
        return;
        if (paramInt1 != NotificationCenter.FileDidLoaded) {
          break;
        }
        paramVarArgs = (String)paramVarArgs[0];
      } while ((this.loadingFile == null) || (!this.loadingFile.equals(paramVarArgs)));
      this.backgroundImage.setImageURI(Uri.fromFile(this.loadingFileObject));
      this.progressView.setVisibility(8);
      this.backgroundImage.setBackgroundColor(0);
      this.doneButton.setEnabled(true);
      this.loadingFileObject = null;
      this.loadingFile = null;
      this.loadingSize = null;
      return;
    } while (paramInt1 != NotificationCenter.wallpapersDidLoaded);
    this.wallPapers = ((ArrayList)paramVarArgs[0]);
    this.wallpappersByIds.clear();
    paramVarArgs = this.wallPapers.iterator();
    while (paramVarArgs.hasNext())
    {
      TLRPC.WallPaper localWallPaper = (TLRPC.WallPaper)paramVarArgs.next();
      this.wallpappersByIds.put(localWallPaper.id, localWallPaper);
    }
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    if ((!this.wallPapers.isEmpty()) && (this.backgroundImage != null)) {
      processSelectedBackground();
    }
    loadWallpapers();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21") };
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    this.updater.onActivityResult(paramInt1, paramInt2, paramIntent);
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidFailedLoad);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidLoaded);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.wallpapersDidLoaded);
    SharedPreferences localSharedPreferences = MessagesController.getGlobalMainSettings();
    this.selectedBackground = localSharedPreferences.getInt("selectedBackground", 1000001);
    this.overrideThemeWallpaper = localSharedPreferences.getBoolean("overrideThemeWallpaper", false);
    this.selectedColor = localSharedPreferences.getInt("selectedColor", 0);
    MessagesStorage.getInstance(this.currentAccount).getWallpapers();
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    this.updater.cleanup();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidFailedLoad);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidLoaded);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.wallpapersDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    processSelectedBackground();
  }
  
  public void restoreSelfArgs(Bundle paramBundle)
  {
    this.updater.setCurrentPicturePath(paramBundle.getString("path"));
  }
  
  public void saveSelfArgs(Bundle paramBundle)
  {
    String str = this.updater.getCurrentPicturePath();
    if (str != null) {
      paramBundle.putString("path", str);
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
      int j = WallpapersActivity.this.wallPapers.size() + 1;
      int i = j;
      if (Theme.hasWallpaperFromTheme()) {
        i = j + 1;
      }
      return i;
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      int i = -2;
      paramViewHolder = (WallpaperCell)paramViewHolder.itemView;
      if (paramInt == 0)
      {
        if ((!Theme.hasWallpaperFromTheme()) || (WallpapersActivity.this.overrideThemeWallpaper)) {}
        for (paramInt = WallpapersActivity.this.selectedBackground;; paramInt = -2)
        {
          paramViewHolder.setWallpaper(null, paramInt, null, false);
          return;
        }
      }
      if (Theme.hasWallpaperFromTheme())
      {
        if (paramInt == 1)
        {
          if (WallpapersActivity.this.overrideThemeWallpaper) {
            i = -1;
          }
          paramViewHolder.setWallpaper(null, i, WallpapersActivity.this.themedWallpaper, true);
          return;
        }
        paramInt -= 2;
      }
      for (;;)
      {
        TLRPC.WallPaper localWallPaper = (TLRPC.WallPaper)WallpapersActivity.this.wallPapers.get(paramInt);
        if ((!Theme.hasWallpaperFromTheme()) || (WallpapersActivity.this.overrideThemeWallpaper)) {
          i = WallpapersActivity.this.selectedBackground;
        }
        paramViewHolder.setWallpaper(localWallPaper, i, null, false);
        return;
        paramInt -= 1;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      return new RecyclerListView.Holder(new WallpaperCell(this.mContext));
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/WallpapersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */