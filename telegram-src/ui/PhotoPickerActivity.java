package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.text.Editable;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.BitmapHolder;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController.AlbumEntry;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.support.widget.GridLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.FoundGif;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_foundGifs;
import org.telegram.tgnet.TLRPC.TL_messages_searchGifs;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.PhotoPickerPhotoCell;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PickerBottomLayout;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class PhotoPickerActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private boolean allowCaption = true;
  private boolean allowIndices;
  private boolean bingSearchEndReached = true;
  private ChatActivity chatActivity;
  private AsyncTask<Void, Void, JSONObject> currentBingTask;
  private PhotoPickerActivityDelegate delegate;
  private EmptyTextProgressView emptyView;
  private FrameLayout frameLayout;
  private int giphyReqId;
  private boolean giphySearchEndReached = true;
  private AnimatorSet hintAnimation;
  private Runnable hintHideRunnable;
  private TextView hintTextView;
  private ImageView imageOrderToggleButton;
  private int itemWidth = 100;
  private String lastSearchString;
  private int lastSearchToken;
  private GridLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private boolean loadingRecent;
  private int nextGiphySearchOffset;
  private PickerBottomLayout pickerBottomLayout;
  private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider()
  {
    public boolean allowCaption()
    {
      return PhotoPickerActivity.this.allowCaption;
    }
    
    public boolean allowGroupPhotos()
    {
      return PhotoPickerActivity.this.imageOrderToggleButton != null;
    }
    
    public boolean cancelButtonPressed()
    {
      PhotoPickerActivity.this.delegate.actionButtonPressed(true);
      PhotoPickerActivity.this.finishFragment();
      return true;
    }
    
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      paramAnonymousMessageObject = PhotoPickerActivity.this.getCellForIndex(paramAnonymousInt);
      if (paramAnonymousMessageObject != null)
      {
        paramAnonymousFileLocation = new int[2];
        paramAnonymousMessageObject.photoImage.getLocationInWindow(paramAnonymousFileLocation);
        PhotoViewer.PlaceProviderObject localPlaceProviderObject = new PhotoViewer.PlaceProviderObject();
        localPlaceProviderObject.viewX = paramAnonymousFileLocation[0];
        int i = paramAnonymousFileLocation[1];
        if (Build.VERSION.SDK_INT >= 21) {}
        for (paramAnonymousInt = 0;; paramAnonymousInt = AndroidUtilities.statusBarHeight)
        {
          localPlaceProviderObject.viewY = (i - paramAnonymousInt);
          localPlaceProviderObject.parentView = PhotoPickerActivity.this.listView;
          localPlaceProviderObject.imageReceiver = paramAnonymousMessageObject.photoImage.getImageReceiver();
          localPlaceProviderObject.thumb = localPlaceProviderObject.imageReceiver.getBitmapSafe();
          localPlaceProviderObject.scale = paramAnonymousMessageObject.photoImage.getScaleX();
          paramAnonymousMessageObject.showCheck(false);
          return localPlaceProviderObject;
        }
      }
      return null;
    }
    
    public int getSelectedCount()
    {
      return PhotoPickerActivity.this.selectedPhotos.size();
    }
    
    public HashMap<Object, Object> getSelectedPhotos()
    {
      return PhotoPickerActivity.this.selectedPhotos;
    }
    
    public ArrayList<Object> getSelectedPhotosOrder()
    {
      return PhotoPickerActivity.this.selectedPhotosOrder;
    }
    
    public ImageReceiver.BitmapHolder getThumbForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      paramAnonymousMessageObject = PhotoPickerActivity.this.getCellForIndex(paramAnonymousInt);
      if (paramAnonymousMessageObject != null) {
        return paramAnonymousMessageObject.photoImage.getImageReceiver().getBitmapSafe();
      }
      return null;
    }
    
    public boolean isPhotoChecked(int paramAnonymousInt)
    {
      boolean bool = true;
      if (PhotoPickerActivity.this.selectedAlbum != null) {
        return (paramAnonymousInt >= 0) && (paramAnonymousInt < PhotoPickerActivity.this.selectedAlbum.photos.size()) && (PhotoPickerActivity.this.selectedPhotos.containsKey(Integer.valueOf(((MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(paramAnonymousInt)).imageId)));
      }
      ArrayList localArrayList;
      if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null))
      {
        localArrayList = PhotoPickerActivity.this.recentImages;
        if ((paramAnonymousInt < 0) || (paramAnonymousInt >= localArrayList.size()) || (!PhotoPickerActivity.this.selectedPhotos.containsKey(((MediaController.SearchImage)localArrayList.get(paramAnonymousInt)).id))) {
          break label153;
        }
      }
      for (;;)
      {
        return bool;
        localArrayList = PhotoPickerActivity.this.searchResult;
        break;
        label153:
        bool = false;
      }
    }
    
    public boolean scaleToFill()
    {
      return false;
    }
    
    public void sendButtonPressed(int paramAnonymousInt, VideoEditedInfo paramAnonymousVideoEditedInfo)
    {
      if (PhotoPickerActivity.this.selectedPhotos.isEmpty())
      {
        if (PhotoPickerActivity.this.selectedAlbum == null) {
          break label86;
        }
        if ((paramAnonymousInt >= 0) && (paramAnonymousInt < PhotoPickerActivity.this.selectedAlbum.photos.size())) {}
      }
      label86:
      label155:
      for (;;)
      {
        return;
        MediaController.PhotoEntry localPhotoEntry = (MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(paramAnonymousInt);
        localPhotoEntry.editedInfo = paramAnonymousVideoEditedInfo;
        PhotoPickerActivity.this.addToSelectedPhotos(localPhotoEntry, -1);
        PhotoPickerActivity.this.sendSelectedPhotos();
        return;
        if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {}
        for (paramAnonymousVideoEditedInfo = PhotoPickerActivity.this.recentImages;; paramAnonymousVideoEditedInfo = PhotoPickerActivity.this.searchResult)
        {
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= paramAnonymousVideoEditedInfo.size())) {
            break label155;
          }
          PhotoPickerActivity.this.addToSelectedPhotos(paramAnonymousVideoEditedInfo.get(paramAnonymousInt), -1);
          break;
        }
      }
    }
    
    public int setPhotoChecked(int paramAnonymousInt, VideoEditedInfo paramAnonymousVideoEditedInfo)
    {
      boolean bool = true;
      MediaController.PhotoEntry localPhotoEntry;
      int i;
      label96:
      int k;
      int j;
      if (PhotoPickerActivity.this.selectedAlbum != null)
      {
        if ((paramAnonymousInt < 0) || (paramAnonymousInt >= PhotoPickerActivity.this.selectedAlbum.photos.size())) {
          return -1;
        }
        localPhotoEntry = (MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(paramAnonymousInt);
        i = PhotoPickerActivity.this.addToSelectedPhotos(localPhotoEntry, -1);
        if (i == -1)
        {
          localPhotoEntry.editedInfo = paramAnonymousVideoEditedInfo;
          i = PhotoPickerActivity.this.selectedPhotosOrder.indexOf(Integer.valueOf(localPhotoEntry.imageId));
          k = PhotoPickerActivity.this.listView.getChildCount();
          j = 0;
        }
      }
      for (;;)
      {
        if (j < k)
        {
          paramAnonymousVideoEditedInfo = PhotoPickerActivity.this.listView.getChildAt(j);
          if (((Integer)paramAnonymousVideoEditedInfo.getTag()).intValue() != paramAnonymousInt) {
            break label324;
          }
          paramAnonymousVideoEditedInfo = (PhotoPickerPhotoCell)paramAnonymousVideoEditedInfo;
          if (!PhotoPickerActivity.this.allowIndices) {
            break label319;
          }
        }
        label311:
        label313:
        label319:
        for (paramAnonymousInt = i;; paramAnonymousInt = -1)
        {
          paramAnonymousVideoEditedInfo.setChecked(paramAnonymousInt, bool, false);
          PhotoPickerActivity.this.pickerBottomLayout.updateSelectedCount(PhotoPickerActivity.this.selectedPhotos.size(), true);
          PhotoPickerActivity.this.delegate.selectedPhotosChanged();
          return i;
          bool = false;
          localPhotoEntry.editedInfo = null;
          break label96;
          if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {}
          for (paramAnonymousVideoEditedInfo = PhotoPickerActivity.this.recentImages;; paramAnonymousVideoEditedInfo = PhotoPickerActivity.this.searchResult)
          {
            if ((paramAnonymousInt < 0) || (paramAnonymousInt >= paramAnonymousVideoEditedInfo.size())) {
              break label311;
            }
            paramAnonymousVideoEditedInfo = (MediaController.SearchImage)paramAnonymousVideoEditedInfo.get(paramAnonymousInt);
            i = PhotoPickerActivity.this.addToSelectedPhotos(paramAnonymousVideoEditedInfo, -1);
            if (i != -1) {
              break label313;
            }
            i = PhotoPickerActivity.this.selectedPhotosOrder.indexOf(paramAnonymousVideoEditedInfo.id);
            break;
          }
          break;
          bool = false;
          break label96;
        }
        label324:
        j += 1;
      }
    }
    
    public void toggleGroupPhotosEnabled()
    {
      ImageView localImageView;
      if (PhotoPickerActivity.this.imageOrderToggleButton != null)
      {
        localImageView = PhotoPickerActivity.this.imageOrderToggleButton;
        if (!SharedConfig.groupPhotosEnabled) {
          break label44;
        }
      }
      label44:
      for (PorterDuffColorFilter localPorterDuffColorFilter = new PorterDuffColorFilter(-10043398, PorterDuff.Mode.MULTIPLY);; localPorterDuffColorFilter = null)
      {
        localImageView.setColorFilter(localPorterDuffColorFilter);
        return;
      }
    }
    
    public void updatePhotoAtIndex(int paramAnonymousInt)
    {
      PhotoPickerPhotoCell localPhotoPickerPhotoCell = PhotoPickerActivity.this.getCellForIndex(paramAnonymousInt);
      if (localPhotoPickerPhotoCell != null)
      {
        if (PhotoPickerActivity.this.selectedAlbum == null) {
          break label236;
        }
        localPhotoPickerPhotoCell.photoImage.setOrientation(0, true);
        localObject = (MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(paramAnonymousInt);
        if (((MediaController.PhotoEntry)localObject).thumbPath != null) {
          localPhotoPickerPhotoCell.photoImage.setImage(((MediaController.PhotoEntry)localObject).thumbPath, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
        }
      }
      else
      {
        return;
      }
      if (((MediaController.PhotoEntry)localObject).path != null)
      {
        localPhotoPickerPhotoCell.photoImage.setOrientation(((MediaController.PhotoEntry)localObject).orientation, true);
        if (((MediaController.PhotoEntry)localObject).isVideo)
        {
          localPhotoPickerPhotoCell.photoImage.setImage("vthumb://" + ((MediaController.PhotoEntry)localObject).imageId + ":" + ((MediaController.PhotoEntry)localObject).path, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
          return;
        }
        localPhotoPickerPhotoCell.photoImage.setImage("thumb://" + ((MediaController.PhotoEntry)localObject).imageId + ":" + ((MediaController.PhotoEntry)localObject).path, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
        return;
      }
      localPhotoPickerPhotoCell.photoImage.setImageResource(2131165542);
      return;
      label236:
      if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {}
      for (Object localObject = PhotoPickerActivity.this.recentImages;; localObject = PhotoPickerActivity.this.searchResult)
      {
        localObject = (MediaController.SearchImage)((ArrayList)localObject).get(paramAnonymousInt);
        if ((((MediaController.SearchImage)localObject).document == null) || (((MediaController.SearchImage)localObject).document.thumb == null)) {
          break;
        }
        localPhotoPickerPhotoCell.photoImage.setImage(((MediaController.SearchImage)localObject).document.thumb.location, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
        return;
      }
      if (((MediaController.SearchImage)localObject).thumbPath != null)
      {
        localPhotoPickerPhotoCell.photoImage.setImage(((MediaController.SearchImage)localObject).thumbPath, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
        return;
      }
      if ((((MediaController.SearchImage)localObject).thumbUrl != null) && (((MediaController.SearchImage)localObject).thumbUrl.length() > 0))
      {
        localPhotoPickerPhotoCell.photoImage.setImage(((MediaController.SearchImage)localObject).thumbUrl, null, localPhotoPickerPhotoCell.getContext().getResources().getDrawable(2131165542));
        return;
      }
      localPhotoPickerPhotoCell.photoImage.setImageResource(2131165542);
    }
    
    public void willHidePhotoViewer()
    {
      int j = PhotoPickerActivity.this.listView.getChildCount();
      int i = 0;
      while (i < j)
      {
        View localView = PhotoPickerActivity.this.listView.getChildAt(i);
        if ((localView instanceof PhotoPickerPhotoCell)) {
          ((PhotoPickerPhotoCell)localView).showCheck(true);
        }
        i += 1;
      }
    }
    
    public void willSwitchFromPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      int j = PhotoPickerActivity.this.listView.getChildCount();
      int i = 0;
      if (i < j)
      {
        paramAnonymousMessageObject = PhotoPickerActivity.this.listView.getChildAt(i);
        if (paramAnonymousMessageObject.getTag() == null) {
          break label101;
        }
      }
      label42:
      label101:
      label170:
      for (;;)
      {
        i += 1;
        break;
        paramAnonymousFileLocation = (PhotoPickerPhotoCell)paramAnonymousMessageObject;
        int k = ((Integer)paramAnonymousMessageObject.getTag()).intValue();
        if (PhotoPickerActivity.this.selectedAlbum != null)
        {
          if ((k >= 0) && (k < PhotoPickerActivity.this.selectedAlbum.photos.size())) {
            if (k == paramAnonymousInt) {
              paramAnonymousFileLocation.showCheck(true);
            }
          }
        }
        else
        {
          if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {}
          for (paramAnonymousMessageObject = PhotoPickerActivity.this.recentImages;; paramAnonymousMessageObject = PhotoPickerActivity.this.searchResult)
          {
            if (k < 0) {
              break label170;
            }
            if (k < paramAnonymousMessageObject.size()) {
              break;
            }
            break label42;
          }
        }
      }
    }
  };
  private ArrayList<MediaController.SearchImage> recentImages;
  private ActionBarMenuItem searchItem;
  private ArrayList<MediaController.SearchImage> searchResult = new ArrayList();
  private HashMap<String, MediaController.SearchImage> searchResultKeys = new HashMap();
  private HashMap<String, MediaController.SearchImage> searchResultUrls = new HashMap();
  private boolean searching;
  private MediaController.AlbumEntry selectedAlbum;
  private HashMap<Object, Object> selectedPhotos;
  private ArrayList<Object> selectedPhotosOrder;
  private boolean sendPressed;
  private boolean singlePhoto;
  private int type;
  
  public PhotoPickerActivity(int paramInt, MediaController.AlbumEntry paramAlbumEntry, HashMap<Object, Object> paramHashMap, ArrayList<Object> paramArrayList, ArrayList<MediaController.SearchImage> paramArrayList1, boolean paramBoolean1, boolean paramBoolean2, ChatActivity paramChatActivity)
  {
    this.selectedAlbum = paramAlbumEntry;
    this.selectedPhotos = paramHashMap;
    this.selectedPhotosOrder = paramArrayList;
    this.type = paramInt;
    this.recentImages = paramArrayList1;
    this.singlePhoto = paramBoolean1;
    this.chatActivity = paramChatActivity;
    this.allowCaption = paramBoolean2;
  }
  
  private int addToSelectedPhotos(Object paramObject, int paramInt)
  {
    int i = -1;
    Object localObject = null;
    if ((paramObject instanceof MediaController.PhotoEntry))
    {
      localObject = Integer.valueOf(((MediaController.PhotoEntry)paramObject).imageId);
      if (localObject != null) {
        break label50;
      }
    }
    label50:
    int j;
    do
    {
      return i;
      if (!(paramObject instanceof MediaController.SearchImage)) {
        break;
      }
      localObject = ((MediaController.SearchImage)paramObject).id;
      break;
      if (!this.selectedPhotos.containsKey(localObject)) {
        break label160;
      }
      this.selectedPhotos.remove(localObject);
      j = this.selectedPhotosOrder.indexOf(localObject);
      if (j >= 0) {
        this.selectedPhotosOrder.remove(j);
      }
      if (this.allowIndices) {
        updateCheckedPhotoIndices();
      }
      i = j;
    } while (paramInt < 0);
    if ((paramObject instanceof MediaController.PhotoEntry)) {
      ((MediaController.PhotoEntry)paramObject).reset();
    }
    for (;;)
    {
      this.provider.updatePhotoAtIndex(paramInt);
      return j;
      if ((paramObject instanceof MediaController.SearchImage)) {
        ((MediaController.SearchImage)paramObject).reset();
      }
    }
    label160:
    this.selectedPhotos.put(localObject, paramObject);
    this.selectedPhotosOrder.add(localObject);
    return -1;
  }
  
  private void fixLayout()
  {
    if (this.listView != null) {
      this.listView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          PhotoPickerActivity.this.fixLayoutInternal();
          if (PhotoPickerActivity.this.listView != null) {
            PhotoPickerActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
          }
          return true;
        }
      });
    }
  }
  
  private void fixLayoutInternal()
  {
    if (getParentActivity() == null) {
      return;
    }
    int j = this.layoutManager.findFirstVisibleItemPosition();
    int i = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
    if (AndroidUtilities.isTablet())
    {
      i = 3;
      label45:
      this.layoutManager.setSpanCount(i);
      if (!AndroidUtilities.isTablet()) {
        break label151;
      }
    }
    label151:
    for (this.itemWidth = ((AndroidUtilities.dp(490.0F) - (i + 1) * AndroidUtilities.dp(4.0F)) / i);; this.itemWidth = ((AndroidUtilities.displaySize.x - (i + 1) * AndroidUtilities.dp(4.0F)) / i))
    {
      this.listAdapter.notifyDataSetChanged();
      this.layoutManager.scrollToPosition(j);
      if (this.selectedAlbum != null) {
        break;
      }
      this.emptyView.setPadding(0, 0, 0, (int)((AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight()) * 0.4F));
      return;
      if ((i == 3) || (i == 1))
      {
        i = 5;
        break label45;
      }
      i = 3;
      break label45;
    }
  }
  
  private PhotoPickerPhotoCell getCellForIndex(int paramInt)
  {
    int j = this.listView.getChildCount();
    int i = 0;
    if (i < j)
    {
      Object localObject = this.listView.getChildAt(i);
      PhotoPickerPhotoCell localPhotoPickerPhotoCell;
      int k;
      if ((localObject instanceof PhotoPickerPhotoCell))
      {
        localPhotoPickerPhotoCell = (PhotoPickerPhotoCell)localObject;
        k = ((Integer)localPhotoPickerPhotoCell.photoImage.getTag()).intValue();
        if (this.selectedAlbum == null) {
          break label90;
        }
        if ((k >= 0) && (k < this.selectedAlbum.photos.size())) {
          break label128;
        }
      }
      label90:
      label128:
      label144:
      for (;;)
      {
        i += 1;
        break;
        if ((this.searchResult.isEmpty()) && (this.lastSearchString == null)) {}
        for (localObject = this.recentImages;; localObject = this.searchResult)
        {
          if ((k < 0) || (k >= ((ArrayList)localObject).size())) {
            break label144;
          }
          if (k != paramInt) {
            break;
          }
          return localPhotoPickerPhotoCell;
        }
      }
    }
    return null;
  }
  
  private void hideHint()
  {
    this.hintAnimation = new AnimatorSet();
    this.hintAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.hintTextView, "alpha", new float[] { 0.0F }) });
    this.hintAnimation.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationCancel(Animator paramAnonymousAnimator)
      {
        if (paramAnonymousAnimator.equals(PhotoPickerActivity.this.hintAnimation))
        {
          PhotoPickerActivity.access$3802(PhotoPickerActivity.this, null);
          PhotoPickerActivity.access$3802(PhotoPickerActivity.this, null);
        }
      }
      
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        if (paramAnonymousAnimator.equals(PhotoPickerActivity.this.hintAnimation))
        {
          PhotoPickerActivity.access$3702(PhotoPickerActivity.this, null);
          PhotoPickerActivity.access$3802(PhotoPickerActivity.this, null);
          if (PhotoPickerActivity.this.hintTextView != null) {
            PhotoPickerActivity.this.hintTextView.setVisibility(8);
          }
        }
      }
    });
    this.hintAnimation.setDuration(300L);
    this.hintAnimation.start();
  }
  
  private void searchBingImages(String paramString, int paramInt1, int paramInt2)
  {
    if (this.searching)
    {
      this.searching = false;
      if (this.giphyReqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.giphyReqId, true);
        this.giphyReqId = 0;
      }
      if (this.currentBingTask != null)
      {
        this.currentBingTask.cancel(true);
        this.currentBingTask = null;
      }
    }
    for (;;)
    {
      try
      {
        this.searching = true;
        localObject = UserConfig.getInstance(this.currentAccount).getCurrentUser().phone;
        if ((((String)localObject).startsWith("44")) || (((String)localObject).startsWith("49")) || (((String)localObject).startsWith("43")) || (((String)localObject).startsWith("31"))) {
          break label321;
        }
        if (!((String)localObject).startsWith("1")) {
          break label327;
        }
      }
      catch (Exception paramString)
      {
        Object localObject;
        String str;
        FileLog.e(paramString);
        this.bingSearchEndReached = true;
        this.searching = false;
        this.listAdapter.notifyItemRemoved(this.searchResult.size() - 1);
        if (((!this.searching) || (!this.searchResult.isEmpty())) && ((!this.loadingRecent) || (this.lastSearchString != null))) {
          continue;
        }
        this.emptyView.showProgress();
        return;
        this.emptyView.showTextView();
        return;
      }
      localObject = Locale.US;
      str = URLEncoder.encode(paramString, "UTF-8");
      if (i != 0)
      {
        paramString = "Strict";
        this.currentBingTask = new AsyncTask()
        {
          private boolean canRetry = true;
          
          private String downloadUrlContent(String paramAnonymousString)
          {
            int m = 1;
            int i = 1;
            Object localObject5 = null;
            int j = 0;
            int k = 0;
            Object localObject4 = null;
            byte[] arrayOfByte = null;
            Object localObject3 = null;
            Object localObject1 = null;
            Object localObject2;
            try
            {
              localObject2 = new URL(paramAnonymousString).openConnection();
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Ocp-Apim-Subscription-Key", BuildVars.BING_SEARCH_KEY);
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept-Language", "en-us,en;q=0.5");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
              localObject1 = localObject2;
              ((URLConnection)localObject2).setConnectTimeout(5000);
              localObject1 = localObject2;
              ((URLConnection)localObject2).setReadTimeout(5000);
              paramAnonymousString = (String)localObject2;
              localObject1 = localObject2;
              if ((localObject2 instanceof HttpURLConnection))
              {
                localObject1 = localObject2;
                Object localObject6 = (HttpURLConnection)localObject2;
                localObject1 = localObject2;
                ((HttpURLConnection)localObject6).setInstanceFollowRedirects(true);
                localObject1 = localObject2;
                int n = ((HttpURLConnection)localObject6).getResponseCode();
                if ((n != 302) && (n != 301))
                {
                  paramAnonymousString = (String)localObject2;
                  if (n != 303) {}
                }
                else
                {
                  localObject1 = localObject2;
                  paramAnonymousString = ((HttpURLConnection)localObject6).getHeaderField("Location");
                  localObject1 = localObject2;
                  localObject6 = ((HttpURLConnection)localObject6).getHeaderField("Set-Cookie");
                  localObject1 = localObject2;
                  paramAnonymousString = new URL(paramAnonymousString).openConnection();
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.setRequestProperty("Cookie", (String)localObject6);
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Ocp-Apim-Subscription-Key", BuildVars.BING_SEARCH_KEY);
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
                }
              }
              localObject1 = paramAnonymousString;
              paramAnonymousString.connect();
              localObject1 = paramAnonymousString;
              localObject2 = paramAnonymousString.getInputStream();
            }
            catch (Throwable paramAnonymousString)
            {
              if (!(paramAnonymousString instanceof SocketTimeoutException)) {
                break label474;
              }
              i = m;
              if (!ConnectionsManager.isNetworkOnline()) {
                break label460;
              }
              i = 0;
              for (;;)
              {
                FileLog.e(paramAnonymousString);
                paramAnonymousString = (String)localObject1;
                localObject2 = localObject5;
                break;
                if ((paramAnonymousString instanceof UnknownHostException))
                {
                  i = 0;
                }
                else if ((paramAnonymousString instanceof SocketException))
                {
                  i = m;
                  if (paramAnonymousString.getMessage() != null)
                  {
                    i = m;
                    if (paramAnonymousString.getMessage().contains("ECONNRESET")) {
                      i = 0;
                    }
                  }
                }
                else
                {
                  i = m;
                  if ((paramAnonymousString instanceof FileNotFoundException)) {
                    i = 0;
                  }
                }
              }
            }
            localObject1 = localObject4;
            if ((i == 0) || (paramAnonymousString != null)) {}
            try
            {
              if ((paramAnonymousString instanceof HttpURLConnection))
              {
                i = ((HttpURLConnection)paramAnonymousString).getResponseCode();
                if ((i == 200) || (i == 202) || (i == 304)) {}
              }
            }
            catch (Exception paramAnonymousString)
            {
              for (;;)
              {
                FileLog.e(paramAnonymousString);
              }
            }
            i = k;
            paramAnonymousString = (String)localObject3;
            if (localObject2 != null) {
              localObject1 = arrayOfByte;
            }
            for (;;)
            {
              try
              {
                arrayOfByte = new byte[32768];
                paramAnonymousString = null;
              }
              catch (Throwable localThrowable3)
              {
                boolean bool;
                label460:
                label474:
                label574:
                paramAnonymousString = (String)localObject1;
                localObject1 = localThrowable3;
                FileLog.e((Throwable)localObject1);
                i = k;
                continue;
              }
              for (;;)
              {
                try
                {
                  bool = isCancelled();
                  if (bool)
                  {
                    i = k;
                    j = i;
                    localObject1 = paramAnonymousString;
                    if (localObject2 == null) {
                      break;
                    }
                  }
                }
                catch (Throwable localThrowable2)
                {
                  String str;
                  break label639;
                  break label574;
                }
                try
                {
                  ((InputStream)localObject2).close();
                  localObject1 = paramAnonymousString;
                  j = i;
                }
                catch (Throwable localThrowable1)
                {
                  FileLog.e(localThrowable1);
                  j = i;
                  str = paramAnonymousString;
                  break;
                }
              }
              if (j == 0) {
                break label665;
              }
              return ((StringBuilder)localObject1).toString();
              try
              {
                i = ((InputStream)localObject2).read(arrayOfByte);
                if (i > 0)
                {
                  if (paramAnonymousString != null) {
                    break label677;
                  }
                  localObject1 = new StringBuilder();
                  paramAnonymousString = (String)localObject1;
                  localObject1 = paramAnonymousString;
                }
              }
              catch (Exception localException1) {}
              try
              {
                paramAnonymousString.append(new String(arrayOfByte, 0, i, "UTF-8"));
              }
              catch (Exception localException2)
              {
                continue;
              }
              if (i == -1)
              {
                i = 1;
              }
              else
              {
                i = k;
                continue;
                localObject1 = paramAnonymousString;
                FileLog.e(localException1);
                i = k;
              }
            }
            label639:
            label665:
            return null;
          }
          
          protected JSONObject doInBackground(Void... paramAnonymousVarArgs)
          {
            paramAnonymousVarArgs = downloadUrlContent(this.val$url);
            if (isCancelled()) {
              return null;
            }
            try
            {
              paramAnonymousVarArgs = new JSONObject(paramAnonymousVarArgs);
              return paramAnonymousVarArgs;
            }
            catch (Exception paramAnonymousVarArgs)
            {
              FileLog.e(paramAnonymousVarArgs);
            }
            return null;
          }
          
          protected void onPostExecute(JSONObject paramAnonymousJSONObject)
          {
            boolean bool = true;
            int j = 0;
            int k = 0;
            int i = 0;
            if (paramAnonymousJSONObject != null) {}
            for (;;)
            {
              int m;
              try
              {
                paramAnonymousJSONObject = paramAnonymousJSONObject.getJSONArray("value");
                m = 0;
                k = 0;
                j = i;
                int n = paramAnonymousJSONObject.length();
                if (k >= n) {
                  break label308;
                }
                try
                {
                  JSONObject localJSONObject = paramAnonymousJSONObject.getJSONObject(k);
                  String str = Utilities.MD5(localJSONObject.getString("contentUrl"));
                  if (PhotoPickerActivity.this.searchResultKeys.containsKey(str)) {
                    break label408;
                  }
                  MediaController.SearchImage localSearchImage = new MediaController.SearchImage();
                  localSearchImage.id = str;
                  localSearchImage.width = localJSONObject.getInt("width");
                  localSearchImage.height = localJSONObject.getInt("height");
                  localSearchImage.size = Utilities.parseInt(localJSONObject.getString("contentSize")).intValue();
                  localSearchImage.imageUrl = localJSONObject.getString("contentUrl");
                  localSearchImage.thumbUrl = localJSONObject.getString("thumbnailUrl");
                  PhotoPickerActivity.this.searchResult.add(localSearchImage);
                  PhotoPickerActivity.this.searchResultKeys.put(str, localSearchImage);
                  i += 1;
                  m = 1;
                }
                catch (Exception localException)
                {
                  j = i;
                  FileLog.e(localException);
                }
                PhotoPickerActivity.access$1802(PhotoPickerActivity.this, false);
              }
              catch (Exception paramAnonymousJSONObject)
              {
                FileLog.e(paramAnonymousJSONObject);
                i = j;
              }
              label229:
              if (i != 0) {
                PhotoPickerActivity.this.listAdapter.notifyItemRangeInserted(PhotoPickerActivity.this.searchResult.size(), i);
              }
              for (;;)
              {
                if (((!PhotoPickerActivity.this.searching) || (!PhotoPickerActivity.this.searchResult.isEmpty())) && ((!PhotoPickerActivity.this.loadingRecent) || (PhotoPickerActivity.this.lastSearchString != null))) {
                  break label397;
                }
                PhotoPickerActivity.this.emptyView.showProgress();
                return;
                label308:
                j = i;
                paramAnonymousJSONObject = PhotoPickerActivity.this;
                if (m == 0) {}
                for (;;)
                {
                  j = i;
                  PhotoPickerActivity.access$1602(paramAnonymousJSONObject, bool);
                  break;
                  bool = false;
                }
                PhotoPickerActivity.access$1602(PhotoPickerActivity.this, true);
                PhotoPickerActivity.access$1802(PhotoPickerActivity.this, false);
                i = k;
                break label229;
                if (PhotoPickerActivity.this.giphySearchEndReached) {
                  PhotoPickerActivity.this.listAdapter.notifyItemRemoved(PhotoPickerActivity.this.searchResult.size() - 1);
                }
              }
              label397:
              PhotoPickerActivity.this.emptyView.showTextView();
              return;
              label408:
              k += 1;
            }
          }
        };
        this.currentBingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
        return;
      }
      paramString = "Off";
      continue;
      label321:
      int i = 1;
      continue;
      label327:
      i = 0;
    }
  }
  
  private void searchGiphyImages(final String paramString, final int paramInt)
  {
    if (this.searching)
    {
      this.searching = false;
      if (this.giphyReqId != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.giphyReqId, true);
        this.giphyReqId = 0;
      }
      if (this.currentBingTask != null)
      {
        this.currentBingTask.cancel(true);
        this.currentBingTask = null;
      }
    }
    this.searching = true;
    TLRPC.TL_messages_searchGifs localTL_messages_searchGifs = new TLRPC.TL_messages_searchGifs();
    localTL_messages_searchGifs.q = paramString;
    localTL_messages_searchGifs.offset = paramInt;
    paramInt = this.lastSearchToken + 1;
    this.lastSearchToken = paramInt;
    this.giphyReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_searchGifs, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            boolean bool = true;
            if (PhotoPickerActivity.15.this.val$token != PhotoPickerActivity.this.lastSearchToken) {
              return;
            }
            int i = 0;
            int j = 0;
            if (paramAnonymousTLObject != null)
            {
              int k = 0;
              Object localObject1 = (TLRPC.TL_messages_foundGifs)paramAnonymousTLObject;
              PhotoPickerActivity.access$2602(PhotoPickerActivity.this, ((TLRPC.TL_messages_foundGifs)localObject1).next_offset);
              int m = 0;
              i = j;
              j = m;
              while (j < ((TLRPC.TL_messages_foundGifs)localObject1).results.size())
              {
                TLRPC.FoundGif localFoundGif = (TLRPC.FoundGif)((TLRPC.TL_messages_foundGifs)localObject1).results.get(j);
                if (PhotoPickerActivity.this.searchResultKeys.containsKey(localFoundGif.url))
                {
                  j += 1;
                }
                else
                {
                  m = 1;
                  MediaController.SearchImage localSearchImage = new MediaController.SearchImage();
                  localSearchImage.id = localFoundGif.url;
                  label156:
                  Object localObject2;
                  if (localFoundGif.document != null)
                  {
                    k = 0;
                    if (k < localFoundGif.document.attributes.size())
                    {
                      localObject2 = (TLRPC.DocumentAttribute)localFoundGif.document.attributes.get(k);
                      if ((!(localObject2 instanceof TLRPC.TL_documentAttributeImageSize)) && (!(localObject2 instanceof TLRPC.TL_documentAttributeVideo))) {
                        break label406;
                      }
                      localSearchImage.width = ((TLRPC.DocumentAttribute)localObject2).w;
                    }
                  }
                  for (localSearchImage.height = ((TLRPC.DocumentAttribute)localObject2).h;; localSearchImage.height = localFoundGif.h)
                  {
                    localSearchImage.size = 0;
                    localSearchImage.imageUrl = localFoundGif.content_url;
                    localSearchImage.thumbUrl = localFoundGif.thumb_url;
                    localSearchImage.localUrl = (localFoundGif.url + "|" + PhotoPickerActivity.15.this.val$query);
                    localSearchImage.document = localFoundGif.document;
                    if ((localFoundGif.photo != null) && (localFoundGif.document != null))
                    {
                      localObject2 = FileLoader.getClosestPhotoSizeWithSize(localFoundGif.photo.sizes, PhotoPickerActivity.this.itemWidth, true);
                      if (localObject2 != null) {
                        localFoundGif.document.thumb = ((TLRPC.PhotoSize)localObject2);
                      }
                    }
                    localSearchImage.type = 1;
                    PhotoPickerActivity.this.searchResult.add(localSearchImage);
                    i += 1;
                    PhotoPickerActivity.this.searchResultKeys.put(localSearchImage.id, localSearchImage);
                    k = m;
                    break;
                    label406:
                    k += 1;
                    break label156;
                    localSearchImage.width = localFoundGif.w;
                  }
                }
              }
              localObject1 = PhotoPickerActivity.this;
              if (k == 0) {
                PhotoPickerActivity.access$1702((PhotoPickerActivity)localObject1, bool);
              }
            }
            else
            {
              PhotoPickerActivity.access$1802(PhotoPickerActivity.this, false);
              if (i == 0) {
                break label575;
              }
              PhotoPickerActivity.this.listAdapter.notifyItemRangeInserted(PhotoPickerActivity.this.searchResult.size(), i);
            }
            for (;;)
            {
              if (((!PhotoPickerActivity.this.searching) || (!PhotoPickerActivity.this.searchResult.isEmpty())) && ((!PhotoPickerActivity.this.loadingRecent) || (PhotoPickerActivity.this.lastSearchString != null))) {
                break label619;
              }
              PhotoPickerActivity.this.emptyView.showProgress();
              return;
              bool = false;
              break;
              label575:
              if (PhotoPickerActivity.this.giphySearchEndReached) {
                PhotoPickerActivity.this.listAdapter.notifyItemRemoved(PhotoPickerActivity.this.searchResult.size() - 1);
              }
            }
            label619:
            PhotoPickerActivity.this.emptyView.showTextView();
          }
        });
      }
    });
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(this.giphyReqId, this.classGuid);
  }
  
  private void sendSelectedPhotos()
  {
    if ((this.selectedPhotos.isEmpty()) || (this.delegate == null) || (this.sendPressed)) {
      return;
    }
    this.sendPressed = true;
    this.delegate.actionButtonPressed(false);
    finishFragment();
  }
  
  private void showHint(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((getParentActivity() == null) || (this.fragmentView == null) || ((paramBoolean1) && (this.hintTextView == null))) {
      return;
    }
    if (this.hintTextView == null)
    {
      this.hintTextView = new TextView(getParentActivity());
      this.hintTextView.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(3.0F), Theme.getColor("chat_gifSaveHintBackground")));
      this.hintTextView.setTextColor(Theme.getColor("chat_gifSaveHintText"));
      this.hintTextView.setTextSize(1, 14.0F);
      this.hintTextView.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(7.0F));
      this.hintTextView.setGravity(16);
      this.hintTextView.setAlpha(0.0F);
      this.frameLayout.addView(this.hintTextView, LayoutHelper.createFrame(-2, -2.0F, 81, 5.0F, 0.0F, 5.0F, 51.0F));
    }
    if (paramBoolean1)
    {
      if (this.hintAnimation != null)
      {
        this.hintAnimation.cancel();
        this.hintAnimation = null;
      }
      AndroidUtilities.cancelRunOnUIThread(this.hintHideRunnable);
      this.hintHideRunnable = null;
      hideHint();
      return;
    }
    TextView localTextView = this.hintTextView;
    Object localObject;
    if (paramBoolean2)
    {
      localObject = LocaleController.getString("GroupPhotosHelp", 2131493635);
      localTextView.setText((CharSequence)localObject);
      if (this.hintHideRunnable == null) {
        break label391;
      }
      if (this.hintAnimation == null) {
        break label362;
      }
      this.hintAnimation.cancel();
      this.hintAnimation = null;
    }
    label362:
    label391:
    while (this.hintAnimation == null)
    {
      this.hintTextView.setVisibility(0);
      this.hintAnimation = new AnimatorSet();
      this.hintAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.hintTextView, "alpha", new float[] { 1.0F }) });
      this.hintAnimation.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoPickerActivity.this.hintAnimation)) {
            PhotoPickerActivity.access$3702(PhotoPickerActivity.this, null);
          }
        }
        
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (paramAnonymousAnimator.equals(PhotoPickerActivity.this.hintAnimation))
          {
            PhotoPickerActivity.access$3702(PhotoPickerActivity.this, null);
            AndroidUtilities.runOnUIThread(PhotoPickerActivity.access$3802(PhotoPickerActivity.this, new Runnable()
            {
              public void run()
              {
                PhotoPickerActivity.this.hideHint();
              }
            }), 2000L);
          }
        }
      });
      this.hintAnimation.setDuration(300L);
      this.hintAnimation.start();
      return;
      localObject = LocaleController.getString("SinglePhotosHelp", 2131494407);
      break;
      AndroidUtilities.cancelRunOnUIThread(this.hintHideRunnable);
      localObject = new Runnable()
      {
        public void run()
        {
          PhotoPickerActivity.this.hideHint();
        }
      };
      this.hintHideRunnable = ((Runnable)localObject);
      AndroidUtilities.runOnUIThread((Runnable)localObject, 2000L);
      return;
    }
  }
  
  private void updateCheckedPhotoIndices()
  {
    if (!this.allowIndices) {
      return;
    }
    int k = this.listView.getChildCount();
    int i = 0;
    label18:
    Object localObject;
    PhotoPickerPhotoCell localPhotoPickerPhotoCell;
    if (i < k)
    {
      localObject = this.listView.getChildAt(i);
      if ((localObject instanceof PhotoPickerPhotoCell))
      {
        localPhotoPickerPhotoCell = (PhotoPickerPhotoCell)localObject;
        localObject = (Integer)localPhotoPickerPhotoCell.getTag();
        if (this.selectedAlbum == null) {
          break label126;
        }
        localObject = (MediaController.PhotoEntry)this.selectedAlbum.photos.get(((Integer)localObject).intValue());
        if (!this.allowIndices) {
          break label121;
        }
      }
    }
    label121:
    for (int j = this.selectedPhotosOrder.indexOf(Integer.valueOf(((MediaController.PhotoEntry)localObject).imageId));; j = -1)
    {
      localPhotoPickerPhotoCell.setNum(j);
      i += 1;
      break label18;
      break;
    }
    label126:
    if ((this.searchResult.isEmpty()) && (this.lastSearchString == null))
    {
      localObject = (MediaController.SearchImage)this.recentImages.get(((Integer)localObject).intValue());
      label160:
      if (!this.allowIndices) {
        break label209;
      }
    }
    label209:
    for (j = this.selectedPhotosOrder.indexOf(((MediaController.SearchImage)localObject).id);; j = -1)
    {
      localPhotoPickerPhotoCell.setNum(j);
      break;
      localObject = (MediaController.SearchImage)this.searchResult.get(((Integer)localObject).intValue());
      break label160;
    }
  }
  
  private void updateSearchInterface()
  {
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    if (((this.searching) && (this.searchResult.isEmpty())) || ((this.loadingRecent) && (this.lastSearchString == null)))
    {
      this.emptyView.showProgress();
      return;
    }
    this.emptyView.showTextView();
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackgroundColor(-13421773);
    this.actionBar.setItemsBackgroundColor(-12763843, false);
    this.actionBar.setTitleColor(-1);
    this.actionBar.setBackButtonImage(2131165346);
    label148:
    Object localObject1;
    float f;
    if (this.selectedAlbum != null)
    {
      this.actionBar.setTitle(this.selectedAlbum.bucketName);
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            PhotoPickerActivity.this.finishFragment();
          }
        }
      });
      if (this.selectedAlbum == null) {
        this.searchItem = this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
        {
          public boolean canCollapseSearch()
          {
            PhotoPickerActivity.this.finishFragment();
            return false;
          }
          
          public void onSearchExpand() {}
          
          public void onSearchPressed(EditText paramAnonymousEditText)
          {
            if (paramAnonymousEditText.getText().toString().length() == 0) {
              return;
            }
            PhotoPickerActivity.this.searchResult.clear();
            PhotoPickerActivity.this.searchResultKeys.clear();
            PhotoPickerActivity.access$1602(PhotoPickerActivity.this, true);
            PhotoPickerActivity.access$1702(PhotoPickerActivity.this, true);
            if (PhotoPickerActivity.this.type == 0)
            {
              PhotoPickerActivity.this.searchBingImages(paramAnonymousEditText.getText().toString(), 0, 53);
              PhotoPickerActivity.access$402(PhotoPickerActivity.this, paramAnonymousEditText.getText().toString());
              if (PhotoPickerActivity.this.lastSearchString.length() != 0) {
                break label220;
              }
              PhotoPickerActivity.access$402(PhotoPickerActivity.this, null);
              if (PhotoPickerActivity.this.type != 0) {
                break label189;
              }
              PhotoPickerActivity.this.emptyView.setText(LocaleController.getString("NoRecentPhotos", 2131493905));
            }
            for (;;)
            {
              PhotoPickerActivity.this.updateSearchInterface();
              return;
              if (PhotoPickerActivity.this.type != 1) {
                break;
              }
              PhotoPickerActivity.access$2602(PhotoPickerActivity.this, 0);
              PhotoPickerActivity.this.searchGiphyImages(paramAnonymousEditText.getText().toString(), 0);
              break;
              label189:
              if (PhotoPickerActivity.this.type == 1)
              {
                PhotoPickerActivity.this.emptyView.setText(LocaleController.getString("NoRecentGIFs", 2131493904));
                continue;
                label220:
                PhotoPickerActivity.this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
              }
            }
          }
          
          public void onTextChanged(EditText paramAnonymousEditText)
          {
            if (paramAnonymousEditText.getText().length() == 0)
            {
              PhotoPickerActivity.this.searchResult.clear();
              PhotoPickerActivity.this.searchResultKeys.clear();
              PhotoPickerActivity.access$402(PhotoPickerActivity.this, null);
              PhotoPickerActivity.access$1602(PhotoPickerActivity.this, true);
              PhotoPickerActivity.access$1702(PhotoPickerActivity.this, true);
              PhotoPickerActivity.access$1802(PhotoPickerActivity.this, false);
              if (PhotoPickerActivity.this.currentBingTask != null)
              {
                PhotoPickerActivity.this.currentBingTask.cancel(true);
                PhotoPickerActivity.access$1902(PhotoPickerActivity.this, null);
              }
              if (PhotoPickerActivity.this.giphyReqId != 0)
              {
                ConnectionsManager.getInstance(PhotoPickerActivity.this.currentAccount).cancelRequest(PhotoPickerActivity.this.giphyReqId, true);
                PhotoPickerActivity.access$2002(PhotoPickerActivity.this, 0);
              }
              if (PhotoPickerActivity.this.type != 0) {
                break label174;
              }
              PhotoPickerActivity.this.emptyView.setText(LocaleController.getString("NoRecentPhotos", 2131493905));
            }
            for (;;)
            {
              PhotoPickerActivity.this.updateSearchInterface();
              return;
              label174:
              if (PhotoPickerActivity.this.type == 1) {
                PhotoPickerActivity.this.emptyView.setText(LocaleController.getString("NoRecentGIFs", 2131493904));
              }
            }
          }
        });
      }
      if (this.selectedAlbum == null)
      {
        if (this.type != 0) {
          break label744;
        }
        this.searchItem.getSearchField().setHint(LocaleController.getString("SearchImagesTitle", 2131494305));
      }
      this.fragmentView = new FrameLayout(paramContext);
      this.frameLayout = ((FrameLayout)this.fragmentView);
      this.frameLayout.setBackgroundColor(-16777216);
      this.listView = new RecyclerListView(paramContext);
      this.listView.setPadding(AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F), AndroidUtilities.dp(4.0F));
      this.listView.setClipToPadding(false);
      this.listView.setHorizontalScrollBarEnabled(false);
      this.listView.setVerticalScrollBarEnabled(false);
      this.listView.setItemAnimator(null);
      this.listView.setLayoutAnimation(null);
      localObject1 = this.listView;
      Object localObject2 = new GridLayoutManager(paramContext, 4)
      {
        public boolean supportsPredictiveItemAnimations()
        {
          return false;
        }
      };
      this.layoutManager = ((GridLayoutManager)localObject2);
      ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
      this.listView.addItemDecoration(new RecyclerView.ItemDecoration()
      {
        public void getItemOffsets(Rect paramAnonymousRect, View paramAnonymousView, RecyclerView paramAnonymousRecyclerView, RecyclerView.State paramAnonymousState)
        {
          int j = 0;
          super.getItemOffsets(paramAnonymousRect, paramAnonymousView, paramAnonymousRecyclerView, paramAnonymousState);
          int k = paramAnonymousState.getItemCount();
          int i = paramAnonymousRecyclerView.getChildAdapterPosition(paramAnonymousView);
          int n = PhotoPickerActivity.this.layoutManager.getSpanCount();
          k = (int)Math.ceil(k / n);
          int m = i / n;
          if (i % n != n - 1) {}
          for (i = AndroidUtilities.dp(4.0F);; i = 0)
          {
            paramAnonymousRect.right = i;
            i = j;
            if (m != k - 1) {
              i = AndroidUtilities.dp(4.0F);
            }
            paramAnonymousRect.bottom = i;
            return;
          }
        }
      });
      localObject1 = this.frameLayout;
      localObject2 = this.listView;
      if (!this.singlePhoto) {
        break label774;
      }
      f = 0.0F;
      label331:
      ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, f));
      localObject1 = this.listView;
      localObject2 = new ListAdapter(paramContext);
      this.listAdapter = ((ListAdapter)localObject2);
      ((RecyclerListView)localObject1).setAdapter((RecyclerView.Adapter)localObject2);
      this.listView.setGlowColor(-13421773);
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (PhotoPickerActivity.this.selectedAlbum != null) {
            paramAnonymousView = PhotoPickerActivity.this.selectedAlbum.photos;
          }
          while ((paramAnonymousInt < 0) || (paramAnonymousInt >= paramAnonymousView.size()))
          {
            return;
            if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {
              paramAnonymousView = PhotoPickerActivity.this.recentImages;
            } else {
              paramAnonymousView = PhotoPickerActivity.this.searchResult;
            }
          }
          if (PhotoPickerActivity.this.searchItem != null) {
            AndroidUtilities.hideKeyboard(PhotoPickerActivity.this.searchItem.getSearchField());
          }
          PhotoViewer.getInstance().setParentActivity(PhotoPickerActivity.this.getParentActivity());
          PhotoViewer localPhotoViewer = PhotoViewer.getInstance();
          if (PhotoPickerActivity.this.singlePhoto) {}
          for (int i = 1;; i = 0)
          {
            localPhotoViewer.openPhotoForSelect(paramAnonymousView, paramAnonymousInt, i, PhotoPickerActivity.this.provider, PhotoPickerActivity.this.chatActivity);
            return;
          }
        }
      });
      if (this.selectedAlbum == null) {
        this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
        {
          public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
          {
            if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null))
            {
              paramAnonymousView = new AlertDialog.Builder(PhotoPickerActivity.this.getParentActivity());
              paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
              paramAnonymousView.setMessage(LocaleController.getString("ClearSearch", 2131493264));
              paramAnonymousView.setPositiveButton(LocaleController.getString("ClearButton", 2131493257).toUpperCase(), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                {
                  PhotoPickerActivity.this.recentImages.clear();
                  if (PhotoPickerActivity.this.listAdapter != null) {
                    PhotoPickerActivity.this.listAdapter.notifyDataSetChanged();
                  }
                  MessagesStorage.getInstance(PhotoPickerActivity.this.currentAccount).clearWebRecent(PhotoPickerActivity.this.type);
                }
              });
              paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
              PhotoPickerActivity.this.showDialog(paramAnonymousView.create());
              return true;
            }
            return false;
          }
        });
      }
      this.emptyView = new EmptyTextProgressView(paramContext);
      this.emptyView.setTextColor(-8355712);
      this.emptyView.setProgressBarColor(-1);
      this.emptyView.setShowAtCenter(true);
      if (this.selectedAlbum == null) {
        break label781;
      }
      this.emptyView.setText(LocaleController.getString("NoPhotos", 2131493900));
      label489:
      localObject1 = this.frameLayout;
      localObject2 = this.emptyView;
      if (!this.singlePhoto) {
        break label834;
      }
      f = 0.0F;
      label510:
      ((FrameLayout)localObject1).addView((View)localObject2, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, f));
      if (this.selectedAlbum == null)
      {
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
        {
          public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
          {
            if (paramAnonymousInt == 1) {
              AndroidUtilities.hideKeyboard(PhotoPickerActivity.this.getParentActivity().getCurrentFocus());
            }
          }
          
          public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
          {
            paramAnonymousInt2 = PhotoPickerActivity.this.layoutManager.findFirstVisibleItemPosition();
            if (paramAnonymousInt2 == -1)
            {
              paramAnonymousInt1 = 0;
              if (paramAnonymousInt1 > 0)
              {
                int i = PhotoPickerActivity.this.layoutManager.getItemCount();
                if ((paramAnonymousInt1 != 0) && (paramAnonymousInt2 + paramAnonymousInt1 > i - 2) && (!PhotoPickerActivity.this.searching))
                {
                  if ((PhotoPickerActivity.this.type != 0) || (PhotoPickerActivity.this.bingSearchEndReached)) {
                    break label126;
                  }
                  PhotoPickerActivity.this.searchBingImages(PhotoPickerActivity.this.lastSearchString, PhotoPickerActivity.this.searchResult.size(), 54);
                }
              }
            }
            label126:
            while ((PhotoPickerActivity.this.type != 1) || (PhotoPickerActivity.this.giphySearchEndReached))
            {
              return;
              paramAnonymousInt1 = Math.abs(PhotoPickerActivity.this.layoutManager.findLastVisibleItemPosition() - paramAnonymousInt2) + 1;
              break;
            }
            PhotoPickerActivity.this.searchGiphyImages(PhotoPickerActivity.this.searchItem.getSearchField().getText().toString(), PhotoPickerActivity.this.nextGiphySearchOffset);
          }
        });
        updateSearchInterface();
      }
      this.pickerBottomLayout = new PickerBottomLayout(paramContext);
      this.frameLayout.addView(this.pickerBottomLayout, LayoutHelper.createFrame(-1, 48, 80));
      this.pickerBottomLayout.cancelButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PhotoPickerActivity.this.delegate.actionButtonPressed(true);
          PhotoPickerActivity.this.finishFragment();
        }
      });
      this.pickerBottomLayout.doneButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          PhotoPickerActivity.this.sendSelectedPhotos();
        }
      });
      if (!this.singlePhoto) {
        break label841;
      }
      this.pickerBottomLayout.setVisibility(8);
      label639:
      if ((this.selectedAlbum == null) && (this.type != 0)) {
        break label978;
      }
    }
    label744:
    label774:
    label781:
    label834:
    label841:
    label978:
    for (boolean bool = true;; bool = false)
    {
      this.allowIndices = bool;
      this.listView.setEmptyView(this.emptyView);
      this.pickerBottomLayout.updateSelectedCount(this.selectedPhotos.size(), true);
      return this.fragmentView;
      if (this.type == 0)
      {
        this.actionBar.setTitle(LocaleController.getString("SearchImagesTitle", 2131494305));
        break;
      }
      if (this.type != 1) {
        break;
      }
      this.actionBar.setTitle(LocaleController.getString("SearchGifsTitle", 2131494302));
      break;
      if (this.type != 1) {
        break label148;
      }
      this.searchItem.getSearchField().setHint(LocaleController.getString("SearchGifsTitle", 2131494302));
      break label148;
      f = 48.0F;
      break label331;
      if (this.type == 0)
      {
        this.emptyView.setText(LocaleController.getString("NoRecentPhotos", 2131493905));
        break label489;
      }
      if (this.type != 1) {
        break label489;
      }
      this.emptyView.setText(LocaleController.getString("NoRecentGIFs", 2131493904));
      break label489;
      f = 48.0F;
      break label510;
      if (((this.selectedAlbum == null) && (this.type != 0)) || (this.chatActivity == null) || (!this.chatActivity.allowGroupPhotos())) {
        break label639;
      }
      this.imageOrderToggleButton = new ImageView(paramContext);
      this.imageOrderToggleButton.setScaleType(ImageView.ScaleType.CENTER);
      this.imageOrderToggleButton.setImageResource(2131165593);
      this.pickerBottomLayout.addView(this.imageOrderToggleButton, LayoutHelper.createFrame(48, -1, 17));
      this.imageOrderToggleButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          SharedConfig.toggleGroupPhotosEnabled();
          ImageView localImageView = PhotoPickerActivity.this.imageOrderToggleButton;
          if (SharedConfig.groupPhotosEnabled) {}
          for (paramAnonymousView = new PorterDuffColorFilter(-10043398, PorterDuff.Mode.MULTIPLY);; paramAnonymousView = null)
          {
            localImageView.setColorFilter(paramAnonymousView);
            PhotoPickerActivity.this.showHint(false, SharedConfig.groupPhotosEnabled);
            PhotoPickerActivity.this.updateCheckedPhotoIndices();
            return;
          }
        }
      });
      localObject1 = this.imageOrderToggleButton;
      if (SharedConfig.groupPhotosEnabled) {}
      for (paramContext = new PorterDuffColorFilter(-10043398, PorterDuff.Mode.MULTIPLY);; paramContext = null)
      {
        ((ImageView)localObject1).setColorFilter(paramContext);
        break;
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.closeChats) {
      removeSelfFromStack();
    }
    while ((paramInt1 != NotificationCenter.recentImagesDidLoaded) || (this.selectedAlbum != null) || (this.type != ((Integer)paramVarArgs[0]).intValue())) {
      return;
    }
    this.recentImages = ((ArrayList)paramVarArgs[1]);
    this.loadingRecent = false;
    updateSearchInterface();
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    fixLayout();
  }
  
  public boolean onFragmentCreate()
  {
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.recentImagesDidLoaded);
    if ((this.selectedAlbum == null) && (this.recentImages.isEmpty()))
    {
      MessagesStorage.getInstance(this.currentAccount).loadWebRecent(this.type);
      this.loadingRecent = true;
    }
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.recentImagesDidLoaded);
    if (this.currentBingTask != null)
    {
      this.currentBingTask.cancel(true);
      this.currentBingTask = null;
    }
    if (this.giphyReqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.giphyReqId, true);
      this.giphyReqId = 0;
    }
    super.onFragmentDestroy();
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    if (this.searchItem != null)
    {
      this.searchItem.openSearch(true);
      getParentActivity().getWindow().setSoftInputMode(32);
    }
    fixLayout();
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (this.searchItem != null)) {
      AndroidUtilities.showKeyboard(this.searchItem.getSearchField());
    }
  }
  
  public void setDelegate(PhotoPickerActivityDelegate paramPhotoPickerActivityDelegate)
  {
    this.delegate = paramPhotoPickerActivityDelegate;
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
      int j = 0;
      int i = 0;
      if (PhotoPickerActivity.this.selectedAlbum == null)
      {
        if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {
          return PhotoPickerActivity.this.recentImages.size();
        }
        if (PhotoPickerActivity.this.type == 0)
        {
          j = PhotoPickerActivity.this.searchResult.size();
          if (PhotoPickerActivity.this.bingSearchEndReached) {}
          for (;;)
          {
            return i + j;
            i = 1;
          }
        }
        if (PhotoPickerActivity.this.type == 1)
        {
          int k = PhotoPickerActivity.this.searchResult.size();
          if (PhotoPickerActivity.this.giphySearchEndReached) {}
          for (i = j;; i = 1) {
            return i + k;
          }
        }
      }
      return PhotoPickerActivity.this.selectedAlbum.photos.size();
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((PhotoPickerActivity.this.selectedAlbum != null) || ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null) && (paramInt < PhotoPickerActivity.this.recentImages.size())) || (paramInt < PhotoPickerActivity.this.searchResult.size())) {
        return 0;
      }
      return 1;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i;
      if (PhotoPickerActivity.this.selectedAlbum == null)
      {
        i = paramViewHolder.getAdapterPosition();
        if ((!PhotoPickerActivity.this.searchResult.isEmpty()) || (PhotoPickerActivity.this.lastSearchString != null)) {
          break label56;
        }
        if (i >= PhotoPickerActivity.this.recentImages.size()) {
          break label54;
        }
      }
      label54:
      label56:
      while (i < PhotoPickerActivity.this.searchResult.size())
      {
        return true;
        return false;
      }
      return false;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      Object localObject;
      label167:
      label200:
      label214:
      label463:
      label506:
      label535:
      label634:
      label734:
      label739:
      label751:
      label757:
      do
      {
        return;
        localObject = (PhotoPickerPhotoCell)paramViewHolder.itemView;
        ((PhotoPickerPhotoCell)localObject).itemWidth = PhotoPickerActivity.this.itemWidth;
        BackupImageView localBackupImageView = ((PhotoPickerPhotoCell)localObject).photoImage;
        localBackupImageView.setTag(Integer.valueOf(paramInt));
        ((PhotoPickerPhotoCell)localObject).setTag(Integer.valueOf(paramInt));
        localBackupImageView.setOrientation(0, true);
        boolean bool1;
        boolean bool2;
        if (PhotoPickerActivity.this.selectedAlbum != null)
        {
          paramViewHolder = (MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(paramInt);
          if (paramViewHolder.thumbPath != null)
          {
            localBackupImageView.setImage(paramViewHolder.thumbPath, null, this.mContext.getResources().getDrawable(2131165542));
            if (!PhotoPickerActivity.this.allowIndices) {
              break label463;
            }
            paramInt = PhotoPickerActivity.this.selectedPhotosOrder.indexOf(Integer.valueOf(paramViewHolder.imageId));
            ((PhotoPickerPhotoCell)localObject).setChecked(paramInt, PhotoPickerActivity.this.selectedPhotos.containsKey(Integer.valueOf(paramViewHolder.imageId)), false);
            bool1 = PhotoViewer.isShowingImage(paramViewHolder.path);
            paramViewHolder = localBackupImageView.getImageReceiver();
            if (bool1) {
              break label751;
            }
            bool2 = true;
            paramViewHolder.setVisible(bool2, true);
            paramViewHolder = ((PhotoPickerPhotoCell)localObject).checkBox;
            if ((!PhotoPickerActivity.this.singlePhoto) && (!bool1)) {
              break label757;
            }
          }
        }
        for (paramInt = 8;; paramInt = 0)
        {
          paramViewHolder.setVisibility(paramInt);
          return;
          if (paramViewHolder.path != null)
          {
            localBackupImageView.setOrientation(paramViewHolder.orientation, true);
            if (paramViewHolder.isVideo)
            {
              ((PhotoPickerPhotoCell)localObject).videoInfoContainer.setVisibility(0);
              paramInt = paramViewHolder.duration / 60;
              int i = paramViewHolder.duration;
              ((PhotoPickerPhotoCell)localObject).videoTextView.setText(String.format("%d:%02d", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i - paramInt * 60) }));
              localBackupImageView.setImage("vthumb://" + paramViewHolder.imageId + ":" + paramViewHolder.path, null, this.mContext.getResources().getDrawable(2131165542));
              break;
            }
            ((PhotoPickerPhotoCell)localObject).videoInfoContainer.setVisibility(4);
            localBackupImageView.setImage("thumb://" + paramViewHolder.imageId + ":" + paramViewHolder.path, null, this.mContext.getResources().getDrawable(2131165542));
            break;
          }
          localBackupImageView.setImageResource(2131165542);
          break;
          paramInt = -1;
          break label167;
          if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null))
          {
            paramViewHolder = (MediaController.SearchImage)PhotoPickerActivity.this.recentImages.get(paramInt);
            if (paramViewHolder.thumbPath == null) {
              break label634;
            }
            localBackupImageView.setImage(paramViewHolder.thumbPath, null, this.mContext.getResources().getDrawable(2131165542));
            ((PhotoPickerPhotoCell)localObject).videoInfoContainer.setVisibility(4);
            if (!PhotoPickerActivity.this.allowIndices) {
              break label734;
            }
          }
          for (paramInt = PhotoPickerActivity.this.selectedPhotosOrder.indexOf(paramViewHolder.id);; paramInt = -1)
          {
            ((PhotoPickerPhotoCell)localObject).setChecked(paramInt, PhotoPickerActivity.this.selectedPhotos.containsKey(paramViewHolder.id), false);
            if (paramViewHolder.document == null) {
              break label739;
            }
            bool1 = PhotoViewer.isShowingImage(FileLoader.getPathToAttach(paramViewHolder.document, true).getAbsolutePath());
            break;
            paramViewHolder = (MediaController.SearchImage)PhotoPickerActivity.this.searchResult.get(paramInt);
            break label506;
            if ((paramViewHolder.thumbUrl != null) && (paramViewHolder.thumbUrl.length() > 0))
            {
              localBackupImageView.setImage(paramViewHolder.thumbUrl, null, this.mContext.getResources().getDrawable(2131165542));
              break label535;
            }
            if ((paramViewHolder.document != null) && (paramViewHolder.document.thumb != null))
            {
              localBackupImageView.setImage(paramViewHolder.document.thumb.location, null, this.mContext.getResources().getDrawable(2131165542));
              break label535;
            }
            localBackupImageView.setImageResource(2131165542);
            break label535;
          }
          bool1 = PhotoViewer.isShowingImage(paramViewHolder.imageUrl);
          break label200;
          bool2 = false;
          break label214;
        }
        localObject = paramViewHolder.itemView.getLayoutParams();
      } while (localObject == null);
      ((ViewGroup.LayoutParams)localObject).width = PhotoPickerActivity.this.itemWidth;
      ((ViewGroup.LayoutParams)localObject).height = PhotoPickerActivity.this.itemWidth;
      paramViewHolder.itemView.setLayoutParams((ViewGroup.LayoutParams)localObject);
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        localFrameLayout = new FrameLayout(this.mContext);
        paramViewGroup = localFrameLayout;
        RadialProgressView localRadialProgressView = new RadialProgressView(this.mContext);
        localRadialProgressView.setProgressColor(-1);
        localFrameLayout.addView(localRadialProgressView, LayoutHelper.createFrame(-1, -1.0F));
        return new RecyclerListView.Holder(paramViewGroup);
      }
      paramViewGroup = new PhotoPickerPhotoCell(this.mContext, true);
      paramViewGroup.checkFrame.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          boolean bool2 = false;
          boolean bool1 = false;
          int i = -1;
          int k = ((Integer)((View)paramAnonymousView.getParent()).getTag()).intValue();
          int j;
          if (PhotoPickerActivity.this.selectedAlbum != null)
          {
            localObject = (MediaController.PhotoEntry)PhotoPickerActivity.this.selectedAlbum.photos.get(k);
            if (!PhotoPickerActivity.this.selectedPhotos.containsKey(Integer.valueOf(((MediaController.PhotoEntry)localObject).imageId))) {
              bool1 = true;
            }
            j = i;
            if (PhotoPickerActivity.this.allowIndices)
            {
              j = i;
              if (bool1) {
                j = PhotoPickerActivity.this.selectedPhotosOrder.size();
              }
            }
            ((PhotoPickerPhotoCell)paramAnonymousView.getParent()).setChecked(j, bool1, true);
            PhotoPickerActivity.this.addToSelectedPhotos(localObject, k);
            PhotoPickerActivity.this.pickerBottomLayout.updateSelectedCount(PhotoPickerActivity.this.selectedPhotos.size(), true);
            PhotoPickerActivity.this.delegate.selectedPhotosChanged();
            return;
          }
          AndroidUtilities.hideKeyboard(PhotoPickerActivity.this.getParentActivity().getCurrentFocus());
          if ((PhotoPickerActivity.this.searchResult.isEmpty()) && (PhotoPickerActivity.this.lastSearchString == null)) {}
          for (Object localObject = (MediaController.SearchImage)PhotoPickerActivity.this.recentImages.get(((Integer)((View)paramAnonymousView.getParent()).getTag()).intValue());; localObject = (MediaController.SearchImage)PhotoPickerActivity.this.searchResult.get(((Integer)((View)paramAnonymousView.getParent()).getTag()).intValue()))
          {
            bool1 = bool2;
            if (!PhotoPickerActivity.this.selectedPhotos.containsKey(((MediaController.SearchImage)localObject).id)) {
              bool1 = true;
            }
            j = i;
            if (PhotoPickerActivity.this.allowIndices)
            {
              j = i;
              if (bool1) {
                j = PhotoPickerActivity.this.selectedPhotosOrder.size();
              }
            }
            ((PhotoPickerPhotoCell)paramAnonymousView.getParent()).setChecked(j, bool1, true);
            PhotoPickerActivity.this.addToSelectedPhotos(localObject, k);
            break;
          }
        }
      });
      FrameLayout localFrameLayout = paramViewGroup.checkFrame;
      if (PhotoPickerActivity.this.singlePhoto) {}
      for (paramInt = 8;; paramInt = 0)
      {
        localFrameLayout.setVisibility(paramInt);
        break;
      }
    }
  }
  
  public static abstract interface PhotoPickerActivityDelegate
  {
    public abstract void actionButtonPressed(boolean paramBoolean);
    
    public abstract void selectedPhotosChanged();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/PhotoPickerActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */