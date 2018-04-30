package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.MessagesStorage.IntCallback;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.GeoPoint;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_geoPoint;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messages_getRecentLocations;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.BaseLocationAdapter.BaseLocationAdapterDelegate;
import org.telegram.ui.Adapters.LocationActivityAdapter;
import org.telegram.ui.Adapters.LocationActivitySearchAdapter;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.LocationLoadingCell;
import org.telegram.ui.Cells.LocationPoweredCell;
import org.telegram.ui.Cells.SendLocationCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MapPlaceholderDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;

public class LocationActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int map_list_menu_hybrid = 4;
  private static final int map_list_menu_map = 2;
  private static final int map_list_menu_satellite = 3;
  private static final int share = 1;
  private LocationActivityAdapter adapter;
  private AnimatorSet animatorSet;
  private AvatarDrawable avatarDrawable;
  private boolean checkGpsEnabled = true;
  private boolean checkPermission = true;
  private CircleOptions circleOptions;
  private LocationActivityDelegate delegate;
  private long dialogId;
  private EmptyTextProgressView emptyView;
  private boolean firstFocus = true;
  private boolean firstWas = false;
  private GoogleMap googleMap;
  private boolean isFirstLocation = true;
  private LinearLayoutManager layoutManager;
  private RecyclerListView listView;
  private int liveLocationType;
  private ImageView locationButton;
  private MapView mapView;
  private FrameLayout mapViewClip;
  private boolean mapsInitialized;
  private ImageView markerImageView;
  private int markerTop;
  private ImageView markerXImageView;
  private ArrayList<LiveLocation> markers = new ArrayList();
  private SparseArray<LiveLocation> markersMap = new SparseArray();
  private MessageObject messageObject;
  private Location myLocation;
  private boolean onResumeCalled;
  private ActionBarMenuItem otherItem;
  private int overScrollHeight = AndroidUtilities.displaySize.x - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(66.0F);
  private ImageView routeButton;
  private LocationActivitySearchAdapter searchAdapter;
  private RecyclerListView searchListView;
  private boolean searchWas;
  private boolean searching;
  private Runnable updateRunnable;
  private Location userLocation;
  private boolean userLocationMoved = false;
  private boolean wasResults;
  
  public LocationActivity(int paramInt)
  {
    this.liveLocationType = paramInt;
  }
  
  private LiveLocation addUserMarker(TLRPC.Message paramMessage)
  {
    Object localObject = new LatLng(paramMessage.media.geo.lat, paramMessage.media.geo._long);
    LiveLocation localLiveLocation = (LiveLocation)this.markersMap.get(paramMessage.from_id);
    if (localLiveLocation == null)
    {
      localLiveLocation = new LiveLocation();
      localLiveLocation.object = paramMessage;
      if (localLiveLocation.object.from_id != 0)
      {
        localLiveLocation.user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(localLiveLocation.object.from_id));
        localLiveLocation.id = localLiveLocation.object.from_id;
      }
      for (;;)
      {
        try
        {
          paramMessage = new MarkerOptions().position((LatLng)localObject);
          localObject = createUserBitmap(localLiveLocation);
          if (localObject != null)
          {
            paramMessage.icon(BitmapDescriptorFactory.fromBitmap((Bitmap)localObject));
            paramMessage.anchor(0.5F, 0.907F);
            localLiveLocation.marker = this.googleMap.addMarker(paramMessage);
            this.markers.add(localLiveLocation);
            this.markersMap.put(localLiveLocation.id, localLiveLocation);
            paramMessage = LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId);
            if ((localLiveLocation.id == UserConfig.getInstance(this.currentAccount).getClientUserId()) && (paramMessage != null) && (localLiveLocation.object.id == paramMessage.mid) && (this.myLocation != null)) {
              localLiveLocation.marker.setPosition(new LatLng(this.myLocation.getLatitude(), this.myLocation.getLongitude()));
            }
          }
          return localLiveLocation;
        }
        catch (Exception paramMessage)
        {
          int i;
          FileLog.e(paramMessage);
          return localLiveLocation;
        }
        i = (int)MessageObject.getDialogId(paramMessage);
        if (i > 0)
        {
          localLiveLocation.user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
          localLiveLocation.id = i;
        }
        else
        {
          localLiveLocation.chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
          localLiveLocation.id = i;
        }
      }
    }
    localLiveLocation.object = paramMessage;
    localLiveLocation.marker.setPosition((LatLng)localObject);
    return localLiveLocation;
  }
  
  private Bitmap createUserBitmap(LiveLocation paramLiveLocation)
  {
    Bitmap localBitmap2 = null;
    Canvas localCanvas = null;
    Bitmap localBitmap1 = localBitmap2;
    for (;;)
    {
      try
      {
        if (paramLiveLocation.user != null)
        {
          localBitmap1 = localBitmap2;
          if (paramLiveLocation.user.photo != null)
          {
            localBitmap1 = localBitmap2;
            localObject1 = paramLiveLocation.user.photo.photo_small;
            localBitmap1 = localBitmap2;
            localBitmap2 = Bitmap.createBitmap(AndroidUtilities.dp(62.0F), AndroidUtilities.dp(76.0F), Bitmap.Config.ARGB_8888);
            localBitmap1 = localBitmap2;
            localBitmap2.eraseColor(0);
            localBitmap1 = localBitmap2;
            localCanvas = new Canvas(localBitmap2);
            localBitmap1 = localBitmap2;
            Object localObject2 = ApplicationLoader.applicationContext.getResources().getDrawable(2131165468);
            localBitmap1 = localBitmap2;
            ((Drawable)localObject2).setBounds(0, 0, AndroidUtilities.dp(62.0F), AndroidUtilities.dp(76.0F));
            localBitmap1 = localBitmap2;
            ((Drawable)localObject2).draw(localCanvas);
            localBitmap1 = localBitmap2;
            localObject2 = new Paint(1);
            localBitmap1 = localBitmap2;
            RectF localRectF = new RectF();
            localBitmap1 = localBitmap2;
            localCanvas.save();
            if (localObject1 == null) {
              continue;
            }
            localBitmap1 = localBitmap2;
            paramLiveLocation = BitmapFactory.decodeFile(FileLoader.getPathToAttach((TLObject)localObject1, true).toString());
            if (paramLiveLocation != null)
            {
              localBitmap1 = localBitmap2;
              localObject1 = new BitmapShader(paramLiveLocation, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
              localBitmap1 = localBitmap2;
              Matrix localMatrix = new Matrix();
              localBitmap1 = localBitmap2;
              float f = AndroidUtilities.dp(52.0F) / paramLiveLocation.getWidth();
              localBitmap1 = localBitmap2;
              localMatrix.postTranslate(AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F));
              localBitmap1 = localBitmap2;
              localMatrix.postScale(f, f);
              localBitmap1 = localBitmap2;
              ((Paint)localObject2).setShader((Shader)localObject1);
              localBitmap1 = localBitmap2;
              ((BitmapShader)localObject1).setLocalMatrix(localMatrix);
              localBitmap1 = localBitmap2;
              localRectF.set(AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F), AndroidUtilities.dp(57.0F), AndroidUtilities.dp(57.0F));
              localBitmap1 = localBitmap2;
              localCanvas.drawRoundRect(localRectF, AndroidUtilities.dp(26.0F), AndroidUtilities.dp(26.0F), (Paint)localObject2);
            }
            localBitmap1 = localBitmap2;
            localCanvas.restore();
            localBitmap1 = localBitmap2;
          }
        }
      }
      catch (Throwable paramLiveLocation)
      {
        FileLog.e(paramLiveLocation);
        return localBitmap1;
      }
      try
      {
        localCanvas.setBitmap(null);
        return localBitmap2;
      }
      catch (Exception paramLiveLocation) {}
      localObject1 = localCanvas;
      localBitmap1 = localBitmap2;
      if (paramLiveLocation.chat != null)
      {
        localObject1 = localCanvas;
        localBitmap1 = localBitmap2;
        if (paramLiveLocation.chat.photo != null)
        {
          localBitmap1 = localBitmap2;
          localObject1 = paramLiveLocation.chat.photo.photo_small;
        }
      }
    }
    localBitmap1 = localBitmap2;
    Object localObject1 = new AvatarDrawable();
    localBitmap1 = localBitmap2;
    if (paramLiveLocation.user != null)
    {
      localBitmap1 = localBitmap2;
      ((AvatarDrawable)localObject1).setInfo(paramLiveLocation.user);
    }
    for (;;)
    {
      localBitmap1 = localBitmap2;
      localCanvas.translate(AndroidUtilities.dp(5.0F), AndroidUtilities.dp(5.0F));
      localBitmap1 = localBitmap2;
      ((AvatarDrawable)localObject1).setBounds(0, 0, AndroidUtilities.dp(52.2F), AndroidUtilities.dp(52.2F));
      localBitmap1 = localBitmap2;
      ((AvatarDrawable)localObject1).draw(localCanvas);
      break;
      localBitmap1 = localBitmap2;
      if (paramLiveLocation.chat != null)
      {
        localBitmap1 = localBitmap2;
        ((AvatarDrawable)localObject1).setInfo(paramLiveLocation.chat);
      }
    }
    return localBitmap2;
  }
  
  private void fetchRecentLocations(ArrayList<TLRPC.Message> paramArrayList)
  {
    Object localObject = null;
    if (this.firstFocus) {
      localObject = new LatLngBounds.Builder();
    }
    int j = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    int i = 0;
    while (i < paramArrayList.size())
    {
      TLRPC.Message localMessage = (TLRPC.Message)paramArrayList.get(i);
      if (localMessage.date + localMessage.media.period > j)
      {
        if (localObject != null) {
          ((LatLngBounds.Builder)localObject).include(new LatLng(localMessage.media.geo.lat, localMessage.media.geo._long));
        }
        addUserMarker(localMessage);
      }
      i += 1;
    }
    if (localObject != null)
    {
      this.firstFocus = false;
      this.adapter.setLiveLocations(this.markers);
      if (!this.messageObject.isLiveLocation()) {}
    }
    try
    {
      localObject = ((LatLngBounds.Builder)localObject).build();
      i = paramArrayList.size();
      if (i > 1) {}
      try
      {
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds((LatLngBounds)localObject, AndroidUtilities.dp(60.0F)));
        return;
      }
      catch (Exception paramArrayList)
      {
        FileLog.e(paramArrayList);
        return;
      }
      return;
    }
    catch (Exception paramArrayList) {}
  }
  
  private void fixLayoutInternal(boolean paramBoolean)
  {
    if (this.listView != null) {
      if (!this.actionBar.getOccupyStatusBar()) {
        break label40;
      }
    }
    int j;
    label40:
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      i += ActionBar.getCurrentActionBarHeight();
      j = this.fragmentView.getMeasuredHeight();
      if (j != 0) {
        break;
      }
      return;
    }
    this.overScrollHeight = (j - AndroidUtilities.dp(66.0F) - i);
    Object localObject = (FrameLayout.LayoutParams)this.listView.getLayoutParams();
    ((FrameLayout.LayoutParams)localObject).topMargin = i;
    this.listView.setLayoutParams((ViewGroup.LayoutParams)localObject);
    localObject = (FrameLayout.LayoutParams)this.mapViewClip.getLayoutParams();
    ((FrameLayout.LayoutParams)localObject).topMargin = i;
    ((FrameLayout.LayoutParams)localObject).height = this.overScrollHeight;
    this.mapViewClip.setLayoutParams((ViewGroup.LayoutParams)localObject);
    if (this.searchListView != null)
    {
      localObject = (FrameLayout.LayoutParams)this.searchListView.getLayoutParams();
      ((FrameLayout.LayoutParams)localObject).topMargin = i;
      this.searchListView.setLayoutParams((ViewGroup.LayoutParams)localObject);
    }
    this.adapter.setOverScrollHeight(this.overScrollHeight);
    localObject = (FrameLayout.LayoutParams)this.mapView.getLayoutParams();
    if (localObject != null)
    {
      ((FrameLayout.LayoutParams)localObject).height = (this.overScrollHeight + AndroidUtilities.dp(10.0F));
      if (this.googleMap != null) {
        this.googleMap.setPadding(AndroidUtilities.dp(70.0F), 0, AndroidUtilities.dp(70.0F), AndroidUtilities.dp(10.0F));
      }
      this.mapView.setLayoutParams((ViewGroup.LayoutParams)localObject);
    }
    this.adapter.notifyDataSetChanged();
    if (paramBoolean)
    {
      localObject = this.layoutManager;
      if ((this.liveLocationType == 1) || (this.liveLocationType == 2)) {}
      for (i = 66;; i = 0)
      {
        ((LinearLayoutManager)localObject).scrollToPositionWithOffset(0, -AndroidUtilities.dp(i + 32));
        updateClipView(this.layoutManager.findFirstVisibleItemPosition());
        this.listView.post(new Runnable()
        {
          public void run()
          {
            LinearLayoutManager localLinearLayoutManager = LocationActivity.this.layoutManager;
            if ((LocationActivity.this.liveLocationType == 1) || (LocationActivity.this.liveLocationType == 2)) {}
            for (int i = 66;; i = 0)
            {
              localLinearLayoutManager.scrollToPositionWithOffset(0, -AndroidUtilities.dp(i + 32));
              LocationActivity.this.updateClipView(LocationActivity.this.layoutManager.findFirstVisibleItemPosition());
              return;
            }
          }
        });
        return;
      }
    }
    updateClipView(this.layoutManager.findFirstVisibleItemPosition());
  }
  
  private Location getLastLocation()
  {
    LocationManager localLocationManager = (LocationManager)ApplicationLoader.applicationContext.getSystemService("location");
    List localList = localLocationManager.getProviders(true);
    Location localLocation = null;
    int i = localList.size() - 1;
    for (;;)
    {
      if (i >= 0)
      {
        localLocation = localLocationManager.getLastKnownLocation((String)localList.get(i));
        if (localLocation == null) {}
      }
      else
      {
        return localLocation;
      }
      i -= 1;
    }
  }
  
  private int getMessageId(TLRPC.Message paramMessage)
  {
    if (paramMessage.from_id != 0) {
      return paramMessage.from_id;
    }
    return (int)MessageObject.getDialogId(paramMessage);
  }
  
  private boolean getRecentLocations()
  {
    ArrayList localArrayList = (ArrayList)LocationController.getInstance(this.currentAccount).locationsCache.get(this.messageObject.getDialogId());
    Object localObject;
    if ((localArrayList != null) && (localArrayList.isEmpty()))
    {
      fetchRecentLocations(localArrayList);
      int i = (int)this.dialogId;
      if (i >= 0) {
        break label95;
      }
      localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      if ((!ChatObject.isChannel((TLRPC.Chat)localObject)) || (((TLRPC.Chat)localObject).megagroup)) {
        break label95;
      }
    }
    label95:
    do
    {
      return false;
      localArrayList = null;
      break;
      localObject = new TLRPC.TL_messages_getRecentLocations();
      final long l = this.messageObject.getDialogId();
      ((TLRPC.TL_messages_getRecentLocations)localObject).peer = MessagesController.getInstance(this.currentAccount).getInputPeer((int)l);
      ((TLRPC.TL_messages_getRecentLocations)localObject).limit = 100;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (LocationActivity.this.googleMap == null) {
                  return;
                }
                TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
                int j;
                for (int i = 0; i < localmessages_Messages.messages.size(); i = j + 1)
                {
                  j = i;
                  if (!(((TLRPC.Message)localmessages_Messages.messages.get(i)).media instanceof TLRPC.TL_messageMediaGeoLive))
                  {
                    localmessages_Messages.messages.remove(i);
                    j = i - 1;
                  }
                }
                MessagesStorage.getInstance(LocationActivity.this.currentAccount).putUsersAndChats(localmessages_Messages.users, localmessages_Messages.chats, true, true);
                MessagesController.getInstance(LocationActivity.this.currentAccount).putUsers(localmessages_Messages.users, false);
                MessagesController.getInstance(LocationActivity.this.currentAccount).putChats(localmessages_Messages.chats, false);
                LocationController.getInstance(LocationActivity.this.currentAccount).locationsCache.put(LocationActivity.20.this.val$dialog_id, localmessages_Messages.messages);
                NotificationCenter.getInstance(LocationActivity.this.currentAccount).postNotificationName(NotificationCenter.liveLocationsCacheChanged, new Object[] { Long.valueOf(LocationActivity.20.this.val$dialog_id) });
                LocationActivity.this.fetchRecentLocations(localmessages_Messages.messages);
              }
            });
          }
        }
      });
    } while (localArrayList == null);
    return true;
  }
  
  private void onMapInit()
  {
    if (this.googleMap == null) {}
    for (;;)
    {
      return;
      Object localObject1;
      if (this.messageObject != null) {
        if (this.messageObject.isLiveLocation())
        {
          localObject1 = addUserMarker(this.messageObject.messageOwner);
          if (!getRecentLocations()) {
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(((LiveLocation)localObject1).marker.getPosition(), this.googleMap.getMaxZoomLevel() - 4.0F));
          }
        }
      }
      try
      {
        for (;;)
        {
          this.googleMap.setMyLocationEnabled(true);
          this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);
          this.googleMap.getUiSettings().setZoomControlsEnabled(false);
          this.googleMap.getUiSettings().setCompassEnabled(false);
          this.googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
          {
            public void onMyLocationChange(Location paramAnonymousLocation)
            {
              LocationActivity.this.positionMarker(paramAnonymousLocation);
              LocationController.getInstance(LocationActivity.this.currentAccount).setGoogleMapLocation(paramAnonymousLocation, LocationActivity.this.isFirstLocation);
              LocationActivity.access$3502(LocationActivity.this, false);
            }
          });
          localObject1 = getLastLocation();
          this.myLocation = ((Location)localObject1);
          positionMarker((Location)localObject1);
          if ((!this.checkGpsEnabled) || (getParentActivity() == null)) {
            break;
          }
          this.checkGpsEnabled = false;
          if (!getParentActivity().getPackageManager().hasSystemFeature("android.hardware.location.gps")) {
            break;
          }
          try
          {
            if (((LocationManager)ApplicationLoader.applicationContext.getSystemService("location")).isProviderEnabled("gps")) {
              break;
            }
            localObject1 = new AlertDialog.Builder(getParentActivity());
            ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AppName", 2131492981));
            ((AlertDialog.Builder)localObject1).setMessage(LocaleController.getString("GpsDisabledAlert", 2131493630));
            ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("ConnectingToProxyEnable", 2131493286), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
              {
                if (LocationActivity.this.getParentActivity() == null) {
                  return;
                }
                try
                {
                  LocationActivity.this.getParentActivity().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                  return;
                }
                catch (Exception paramAnonymousDialogInterface) {}
              }
            });
            ((AlertDialog.Builder)localObject1).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            showDialog(((AlertDialog.Builder)localObject1).create());
            return;
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            return;
          }
          Object localObject2 = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());
          try
          {
            this.googleMap.addMarker(new MarkerOptions().position((LatLng)localObject2).icon(BitmapDescriptorFactory.fromResource(2131165482)));
            localObject2 = CameraUpdateFactory.newLatLngZoom((LatLng)localObject2, this.googleMap.getMaxZoomLevel() - 4.0F);
            this.googleMap.moveCamera((CameraUpdate)localObject2);
            this.firstFocus = false;
            getRecentLocations();
          }
          catch (Exception localException3)
          {
            for (;;)
            {
              FileLog.e(localException3);
            }
          }
          this.userLocation = new Location("network");
          this.userLocation.setLatitude(20.659322D);
          this.userLocation.setLongitude(-11.40625D);
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
  }
  
  private void positionMarker(Location paramLocation)
  {
    if (paramLocation == null) {}
    Object localObject;
    do
    {
      return;
      this.myLocation = new Location(paramLocation);
      localObject = (LiveLocation)this.markersMap.get(UserConfig.getInstance(this.currentAccount).getClientUserId());
      LocationController.SharingLocationInfo localSharingLocationInfo = LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId);
      if ((localObject != null) && (localSharingLocationInfo != null) && (((LiveLocation)localObject).object.id == localSharingLocationInfo.mid)) {
        ((LiveLocation)localObject).marker.setPosition(new LatLng(paramLocation.getLatitude(), paramLocation.getLongitude()));
      }
      if ((this.messageObject != null) || (this.googleMap == null)) {
        break;
      }
      localObject = new LatLng(paramLocation.getLatitude(), paramLocation.getLongitude());
      if (this.adapter != null)
      {
        if (this.adapter.isPulledUp()) {
          this.adapter.searchGooglePlacesWithQuery(null, this.myLocation);
        }
        this.adapter.setGpsLocation(this.myLocation);
      }
    } while (this.userLocationMoved);
    this.userLocation = new Location(paramLocation);
    if (this.firstWas)
    {
      paramLocation = CameraUpdateFactory.newLatLng((LatLng)localObject);
      this.googleMap.animateCamera(paramLocation);
      return;
    }
    this.firstWas = true;
    paramLocation = CameraUpdateFactory.newLatLngZoom((LatLng)localObject, this.googleMap.getMaxZoomLevel() - 4.0F);
    this.googleMap.moveCamera(paramLocation);
    return;
    this.adapter.setGpsLocation(this.myLocation);
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
          if (LocationActivity.this.getParentActivity() == null) {
            return;
          }
          try
          {
            paramAnonymousDialogInterface = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            paramAnonymousDialogInterface.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
            LocationActivity.this.getParentActivity().startActivity(paramAnonymousDialogInterface);
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
  
  private void updateClipView(int paramInt)
  {
    if (paramInt == -1) {}
    int i;
    int j;
    Object localObject;
    label46:
    do
    {
      do
      {
        return;
        i = 0;
        j = 0;
        localObject = this.listView.getChildAt(0);
      } while (localObject == null);
      if (paramInt == 0)
      {
        paramInt = ((View)localObject).getTop();
        j = this.overScrollHeight;
        if (paramInt >= 0) {
          break;
        }
        i = paramInt;
        i = j + i;
        j = paramInt;
      }
    } while ((FrameLayout.LayoutParams)this.mapViewClip.getLayoutParams() == null);
    if (i <= 0) {
      if (this.mapView.getVisibility() == 0)
      {
        this.mapView.setVisibility(4);
        this.mapViewClip.setVisibility(4);
      }
    }
    for (;;)
    {
      this.mapViewClip.setTranslationY(Math.min(0, j));
      this.mapView.setTranslationY(Math.max(0, -j / 2));
      if (this.markerImageView != null)
      {
        localObject = this.markerImageView;
        paramInt = -j - AndroidUtilities.dp(42.0F) + i / 2;
        this.markerTop = paramInt;
        ((ImageView)localObject).setTranslationY(paramInt);
        this.markerXImageView.setTranslationY(-j - AndroidUtilities.dp(7.0F) + i / 2);
      }
      if (this.routeButton != null) {
        this.routeButton.setTranslationY(j);
      }
      localObject = (FrameLayout.LayoutParams)this.mapView.getLayoutParams();
      if ((localObject == null) || (((FrameLayout.LayoutParams)localObject).height == this.overScrollHeight + AndroidUtilities.dp(10.0F))) {
        break;
      }
      ((FrameLayout.LayoutParams)localObject).height = (this.overScrollHeight + AndroidUtilities.dp(10.0F));
      if (this.googleMap != null) {
        this.googleMap.setPadding(AndroidUtilities.dp(70.0F), 0, AndroidUtilities.dp(70.0F), AndroidUtilities.dp(10.0F));
      }
      this.mapView.setLayoutParams((ViewGroup.LayoutParams)localObject);
      return;
      i = 0;
      break label46;
      if (this.mapView.getVisibility() == 4)
      {
        this.mapView.setVisibility(0);
        this.mapViewClip.setVisibility(0);
      }
    }
  }
  
  private void updateSearchInterface()
  {
    if (this.adapter != null) {
      this.adapter.notifyDataSetChanged();
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    if (AndroidUtilities.isTablet()) {
      this.actionBar.setOccupyStatusBar(false);
    }
    this.actionBar.setAddToContainer(false);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          LocationActivity.this.finishFragment();
        }
        do
        {
          do
          {
            do
            {
              do
              {
                return;
                if (paramAnonymousInt != 2) {
                  break;
                }
              } while (LocationActivity.this.googleMap == null);
              LocationActivity.this.googleMap.setMapType(1);
              return;
              if (paramAnonymousInt != 3) {
                break;
              }
            } while (LocationActivity.this.googleMap == null);
            LocationActivity.this.googleMap.setMapType(2);
            return;
            if (paramAnonymousInt != 4) {
              break;
            }
          } while (LocationActivity.this.googleMap == null);
          LocationActivity.this.googleMap.setMapType(4);
          return;
        } while (paramAnonymousInt != 1);
        try
        {
          double d1 = LocationActivity.this.messageObject.messageOwner.media.geo.lat;
          double d2 = LocationActivity.this.messageObject.messageOwner.media.geo._long;
          LocationActivity.this.getParentActivity().startActivity(new Intent("android.intent.action.VIEW", Uri.parse("geo:" + d1 + "," + d2 + "?q=" + d1 + "," + d2)));
          return;
        }
        catch (Exception localException)
        {
          FileLog.e(localException);
        }
      }
    });
    Object localObject1 = this.actionBar.createMenu();
    FrameLayout localFrameLayout;
    Object localObject2;
    label1181:
    int i;
    label1221:
    float f1;
    label1233:
    int j;
    label1242:
    float f2;
    if (this.messageObject != null) {
      if (this.messageObject.isLiveLocation())
      {
        this.actionBar.setTitle(LocaleController.getString("AttachLiveLocation", 2131493031));
        this.otherItem = ((ActionBarMenu)localObject1).addItem(0, 2131165353);
        this.otherItem.addSubItem(2, LocaleController.getString("Map", 2131493788));
        this.otherItem.addSubItem(3, LocaleController.getString("Satellite", 2131494284));
        this.otherItem.addSubItem(4, LocaleController.getString("Hybrid", 2131493665));
        this.fragmentView = new FrameLayout(paramContext)
        {
          private boolean first = true;
          
          protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
          {
            super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
            if (paramAnonymousBoolean)
            {
              LocationActivity.this.fixLayoutInternal(this.first);
              this.first = false;
            }
          }
        };
        localFrameLayout = (FrameLayout)this.fragmentView;
        this.locationButton = new ImageView(paramContext);
        localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("profile_actionBackground"), Theme.getColor("profile_actionPressedBackground"));
        localObject1 = localObject2;
        if (Build.VERSION.SDK_INT < 21)
        {
          localObject1 = paramContext.getResources().getDrawable(2131165323).mutate();
          ((Drawable)localObject1).setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
          localObject1 = new CombinedDrawable((Drawable)localObject1, (Drawable)localObject2, 0, 0);
          ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
        }
        this.locationButton.setBackgroundDrawable((Drawable)localObject1);
        this.locationButton.setImageResource(2131165534);
        this.locationButton.setScaleType(ImageView.ScaleType.CENTER);
        this.locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("profile_actionIcon"), PorterDuff.Mode.MULTIPLY));
        if (Build.VERSION.SDK_INT >= 21)
        {
          localObject1 = new StateListAnimator();
          localObject2 = ObjectAnimator.ofFloat(this.locationButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
          ((StateListAnimator)localObject1).addState(new int[] { 16842919 }, (Animator)localObject2);
          localObject2 = ObjectAnimator.ofFloat(this.locationButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
          ((StateListAnimator)localObject1).addState(new int[0], (Animator)localObject2);
          this.locationButton.setStateListAnimator((StateListAnimator)localObject1);
          this.locationButton.setOutlineProvider(new ViewOutlineProvider()
          {
            @SuppressLint({"NewApi"})
            public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
            {
              paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
            }
          });
        }
        if (this.messageObject != null)
        {
          this.userLocation = new Location("network");
          this.userLocation.setLatitude(this.messageObject.messageOwner.media.geo.lat);
          this.userLocation.setLongitude(this.messageObject.messageOwner.media.geo._long);
        }
        this.searchWas = false;
        this.searching = false;
        this.mapViewClip = new FrameLayout(paramContext);
        this.mapViewClip.setBackgroundDrawable(new MapPlaceholderDrawable());
        if (this.adapter != null) {
          this.adapter.destroy();
        }
        if (this.searchAdapter != null) {
          this.searchAdapter.destroy();
        }
        this.listView = new RecyclerListView(paramContext);
        this.listView.setItemAnimator(null);
        this.listView.setLayoutAnimation(null);
        localObject1 = this.listView;
        localObject2 = new LocationActivityAdapter(paramContext, this.liveLocationType, this.dialogId);
        this.adapter = ((LocationActivityAdapter)localObject2);
        ((RecyclerListView)localObject1).setAdapter((RecyclerView.Adapter)localObject2);
        this.listView.setVerticalScrollBarEnabled(false);
        localObject1 = this.listView;
        localObject2 = new LinearLayoutManager(paramContext, 1, false)
        {
          public boolean supportsPredictiveItemAnimations()
          {
            return false;
          }
        };
        this.layoutManager = ((LinearLayoutManager)localObject2);
        ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
        localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
        {
          public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
          {
            if (LocationActivity.this.adapter.getItemCount() == 0) {}
            do
            {
              do
              {
                do
                {
                  return;
                  paramAnonymousInt1 = LocationActivity.this.layoutManager.findFirstVisibleItemPosition();
                } while (paramAnonymousInt1 == -1);
                LocationActivity.this.updateClipView(paramAnonymousInt1);
              } while ((paramAnonymousInt2 <= 0) || (LocationActivity.this.adapter.isPulledUp()));
              LocationActivity.this.adapter.setPulledUp();
            } while (LocationActivity.this.myLocation == null);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                LocationActivity.this.adapter.searchGooglePlacesWithQuery(null, LocationActivity.this.myLocation);
              }
            });
          }
        });
        this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
        {
          public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
          {
            if ((paramAnonymousInt == 1) && (LocationActivity.this.messageObject != null) && (!LocationActivity.this.messageObject.isLiveLocation())) {
              if (LocationActivity.this.googleMap != null) {
                LocationActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationActivity.this.messageObject.messageOwner.media.geo.lat, LocationActivity.this.messageObject.messageOwner.media.geo._long), LocationActivity.this.googleMap.getMaxZoomLevel() - 4.0F));
              }
            }
            do
            {
              do
              {
                return;
                if ((paramAnonymousInt == 1) && (LocationActivity.this.liveLocationType != 2))
                {
                  if ((LocationActivity.this.delegate != null) && (LocationActivity.this.userLocation != null))
                  {
                    paramAnonymousView = new TLRPC.TL_messageMediaGeo();
                    paramAnonymousView.geo = new TLRPC.TL_geoPoint();
                    paramAnonymousView.geo.lat = LocationActivity.this.userLocation.getLatitude();
                    paramAnonymousView.geo._long = LocationActivity.this.userLocation.getLongitude();
                    LocationActivity.this.delegate.didSelectLocation(paramAnonymousView, LocationActivity.this.liveLocationType);
                  }
                  LocationActivity.this.finishFragment();
                  return;
                }
                if (((paramAnonymousInt != 2) || (LocationActivity.this.liveLocationType != 1)) && ((paramAnonymousInt != 1) || (LocationActivity.this.liveLocationType != 2)) && ((paramAnonymousInt != 3) || (LocationActivity.this.liveLocationType != 3))) {
                  break;
                }
                if (LocationController.getInstance(LocationActivity.this.currentAccount).isSharingLocation(LocationActivity.this.dialogId))
                {
                  LocationController.getInstance(LocationActivity.this.currentAccount).removeSharingLocation(LocationActivity.this.dialogId);
                  LocationActivity.this.finishFragment();
                  return;
                }
              } while ((LocationActivity.this.delegate == null) || (LocationActivity.this.getParentActivity() == null) || (LocationActivity.this.myLocation == null));
              paramAnonymousView = null;
              if ((int)LocationActivity.this.dialogId > 0) {
                paramAnonymousView = MessagesController.getInstance(LocationActivity.this.currentAccount).getUser(Integer.valueOf((int)LocationActivity.this.dialogId));
              }
              LocationActivity.this.showDialog(AlertsCreator.createLocationUpdateDialog(LocationActivity.this.getParentActivity(), paramAnonymousView, new MessagesStorage.IntCallback()
              {
                public void run(int paramAnonymous2Int)
                {
                  TLRPC.TL_messageMediaGeoLive localTL_messageMediaGeoLive = new TLRPC.TL_messageMediaGeoLive();
                  localTL_messageMediaGeoLive.geo = new TLRPC.TL_geoPoint();
                  localTL_messageMediaGeoLive.geo.lat = LocationActivity.this.myLocation.getLatitude();
                  localTL_messageMediaGeoLive.geo._long = LocationActivity.this.myLocation.getLongitude();
                  localTL_messageMediaGeoLive.period = paramAnonymous2Int;
                  LocationActivity.this.delegate.didSelectLocation(localTL_messageMediaGeoLive, LocationActivity.this.liveLocationType);
                  LocationActivity.this.finishFragment();
                }
              }));
              return;
              paramAnonymousView = LocationActivity.this.adapter.getItem(paramAnonymousInt);
              if ((paramAnonymousView instanceof TLRPC.TL_messageMediaVenue))
              {
                if ((paramAnonymousView != null) && (LocationActivity.this.delegate != null)) {
                  LocationActivity.this.delegate.didSelectLocation((TLRPC.TL_messageMediaVenue)paramAnonymousView, LocationActivity.this.liveLocationType);
                }
                LocationActivity.this.finishFragment();
                return;
              }
            } while (!(paramAnonymousView instanceof LocationActivity.LiveLocation));
            LocationActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(((LocationActivity.LiveLocation)paramAnonymousView).marker.getPosition(), LocationActivity.this.googleMap.getMaxZoomLevel() - 4.0F));
          }
        });
        this.adapter.setDelegate(new BaseLocationAdapter.BaseLocationAdapterDelegate()
        {
          public void didLoadedSearchResult(ArrayList<TLRPC.TL_messageMediaVenue> paramAnonymousArrayList)
          {
            if ((!LocationActivity.this.wasResults) && (!paramAnonymousArrayList.isEmpty())) {
              LocationActivity.access$2202(LocationActivity.this, true);
            }
          }
        });
        this.adapter.setOverScrollHeight(this.overScrollHeight);
        localFrameLayout.addView(this.mapViewClip, LayoutHelper.createFrame(-1, -1, 51));
        this.mapView = new MapView(paramContext)
        {
          public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
          {
            if (LocationActivity.this.messageObject == null)
            {
              if (paramAnonymousMotionEvent.getAction() != 0) {
                break label314;
              }
              if (LocationActivity.this.animatorSet != null) {
                LocationActivity.this.animatorSet.cancel();
              }
              LocationActivity.access$2302(LocationActivity.this, new AnimatorSet());
              LocationActivity.this.animatorSet.setDuration(200L);
              LocationActivity.this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(LocationActivity.this.markerImageView, "translationY", new float[] { LocationActivity.this.markerTop + -AndroidUtilities.dp(10.0F) }), ObjectAnimator.ofFloat(LocationActivity.this.markerXImageView, "alpha", new float[] { 1.0F }) });
              LocationActivity.this.animatorSet.start();
            }
            for (;;)
            {
              if (paramAnonymousMotionEvent.getAction() == 2)
              {
                if (!LocationActivity.this.userLocationMoved)
                {
                  AnimatorSet localAnimatorSet = new AnimatorSet();
                  localAnimatorSet.setDuration(200L);
                  localAnimatorSet.play(ObjectAnimator.ofFloat(LocationActivity.this.locationButton, "alpha", new float[] { 1.0F }));
                  localAnimatorSet.start();
                  LocationActivity.access$2702(LocationActivity.this, true);
                }
                if ((LocationActivity.this.googleMap != null) && (LocationActivity.this.userLocation != null))
                {
                  LocationActivity.this.userLocation.setLatitude(LocationActivity.this.googleMap.getCameraPosition().target.latitude);
                  LocationActivity.this.userLocation.setLongitude(LocationActivity.this.googleMap.getCameraPosition().target.longitude);
                }
                LocationActivity.this.adapter.setCustomLocation(LocationActivity.this.userLocation);
              }
              return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
              label314:
              if (paramAnonymousMotionEvent.getAction() == 1)
              {
                if (LocationActivity.this.animatorSet != null) {
                  LocationActivity.this.animatorSet.cancel();
                }
                LocationActivity.access$2302(LocationActivity.this, new AnimatorSet());
                LocationActivity.this.animatorSet.setDuration(200L);
                LocationActivity.this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(LocationActivity.this.markerImageView, "translationY", new float[] { LocationActivity.this.markerTop }), ObjectAnimator.ofFloat(LocationActivity.this.markerXImageView, "alpha", new float[] { 0.0F }) });
                LocationActivity.this.animatorSet.start();
              }
            }
          }
        };
        new Thread(new Runnable()
        {
          public void run()
          {
            try
            {
              this.val$map.onCreate(null);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if ((LocationActivity.this.mapView != null) && (LocationActivity.this.getParentActivity() != null)) {}
                  try
                  {
                    LocationActivity.10.this.val$map.onCreate(null);
                    MapsInitializer.initialize(LocationActivity.this.getParentActivity());
                    LocationActivity.this.mapView.getMapAsync(new OnMapReadyCallback()
                    {
                      public void onMapReady(GoogleMap paramAnonymous3GoogleMap)
                      {
                        LocationActivity.access$002(LocationActivity.this, paramAnonymous3GoogleMap);
                        LocationActivity.this.googleMap.setPadding(AndroidUtilities.dp(70.0F), 0, AndroidUtilities.dp(70.0F), AndroidUtilities.dp(10.0F));
                        LocationActivity.this.onMapInit();
                      }
                    });
                    LocationActivity.access$3102(LocationActivity.this, true);
                    if (LocationActivity.this.onResumeCalled) {
                      LocationActivity.this.mapView.onResume();
                    }
                    return;
                  }
                  catch (Exception localException)
                  {
                    FileLog.e(localException);
                  }
                }
              });
              return;
            }
            catch (Exception localException)
            {
              for (;;) {}
            }
          }
        }).start();
        localObject1 = new View(paramContext);
        ((View)localObject1).setBackgroundResource(2131165343);
        this.mapViewClip.addView((View)localObject1, LayoutHelper.createFrame(-1, 3, 83));
        if (this.messageObject != null) {
          break label1470;
        }
        this.markerImageView = new ImageView(paramContext);
        this.markerImageView.setImageResource(2131165482);
        this.mapViewClip.addView(this.markerImageView, LayoutHelper.createFrame(24, 42, 49));
        this.markerXImageView = new ImageView(paramContext);
        this.markerXImageView.setAlpha(0.0F);
        this.markerXImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("location_markerX"), PorterDuff.Mode.MULTIPLY));
        this.markerXImageView.setImageResource(2131165605);
        this.mapViewClip.addView(this.markerXImageView, LayoutHelper.createFrame(14, 14, 49));
        this.emptyView = new EmptyTextProgressView(paramContext);
        this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
        this.emptyView.setShowAtCenter(true);
        this.emptyView.setVisibility(8);
        localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
        this.searchListView = new RecyclerListView(paramContext);
        this.searchListView.setVisibility(8);
        this.searchListView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
        localObject1 = this.searchListView;
        paramContext = new LocationActivitySearchAdapter(paramContext);
        this.searchAdapter = paramContext;
        ((RecyclerListView)localObject1).setAdapter(paramContext);
        localFrameLayout.addView(this.searchListView, LayoutHelper.createFrame(-1, -1, 51));
        this.searchListView.setOnScrollListener(new RecyclerView.OnScrollListener()
        {
          public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
          {
            if ((paramAnonymousInt == 1) && (LocationActivity.this.searching) && (LocationActivity.this.searchWas)) {
              AndroidUtilities.hideKeyboard(LocationActivity.this.getParentActivity().getCurrentFocus());
            }
          }
        });
        this.searchListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
        {
          public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
          {
            paramAnonymousView = LocationActivity.this.searchAdapter.getItem(paramAnonymousInt);
            if ((paramAnonymousView != null) && (LocationActivity.this.delegate != null)) {
              LocationActivity.this.delegate.didSelectLocation(paramAnonymousView, LocationActivity.this.liveLocationType);
            }
            LocationActivity.this.finishFragment();
          }
        });
        if ((this.messageObject == null) || (this.messageObject.isLiveLocation())) {
          break label1958;
        }
        paramContext = this.mapViewClip;
        localObject1 = this.locationButton;
        if (Build.VERSION.SDK_INT < 21) {
          break label1925;
        }
        i = 56;
        if (Build.VERSION.SDK_INT < 21) {
          break label1932;
        }
        f1 = 56.0F;
        if (!LocaleController.isRTL) {
          break label1939;
        }
        j = 3;
        if (!LocaleController.isRTL) {
          break label1945;
        }
        f2 = 14.0F;
        label1252:
        if (!LocaleController.isRTL) {
          break label1950;
        }
      }
    }
    label1470:
    label1799:
    label1811:
    label1820:
    label1830:
    label1899:
    label1906:
    label1912:
    label1917:
    label1925:
    label1932:
    label1939:
    label1945:
    label1950:
    for (float f3 = 0.0F;; f3 = 14.0F)
    {
      paramContext.addView((View)localObject1, LayoutHelper.createFrame(i, f1, j | 0x50, f2, 0.0F, f3, 43.0F));
      this.locationButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (Build.VERSION.SDK_INT >= 23)
          {
            paramAnonymousView = LocationActivity.this.getParentActivity();
            if ((paramAnonymousView != null) && (paramAnonymousView.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0)) {
              LocationActivity.this.showPermissionAlert(false);
            }
          }
          do
          {
            do
            {
              return;
              if (LocationActivity.this.messageObject == null) {
                break;
              }
            } while ((LocationActivity.this.myLocation == null) || (LocationActivity.this.googleMap == null));
            LocationActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LocationActivity.this.myLocation.getLatitude(), LocationActivity.this.myLocation.getLongitude()), LocationActivity.this.googleMap.getMaxZoomLevel() - 4.0F));
            return;
          } while ((LocationActivity.this.myLocation == null) || (LocationActivity.this.googleMap == null));
          paramAnonymousView = new AnimatorSet();
          paramAnonymousView.setDuration(200L);
          paramAnonymousView.play(ObjectAnimator.ofFloat(LocationActivity.this.locationButton, "alpha", new float[] { 0.0F }));
          paramAnonymousView.start();
          LocationActivity.this.adapter.setCustomLocation(null);
          LocationActivity.access$2702(LocationActivity.this, false);
          LocationActivity.this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(LocationActivity.this.myLocation.getLatitude(), LocationActivity.this.myLocation.getLongitude())));
        }
      });
      if (this.messageObject == null) {
        this.locationButton.setAlpha(0.0F);
      }
      localFrameLayout.addView(this.actionBar);
      return this.fragmentView;
      if ((this.messageObject.messageOwner.media.title != null) && (this.messageObject.messageOwner.media.title.length() > 0)) {
        this.actionBar.setTitle(LocaleController.getString("SharedPlace", 2131494397));
      }
      for (;;)
      {
        ((ActionBarMenu)localObject1).addItem(1, 2131165637);
        break;
        this.actionBar.setTitle(LocaleController.getString("ChatLocation", 2131493231));
      }
      this.actionBar.setTitle(LocaleController.getString("ShareLocation", 2131494385));
      ((ActionBarMenu)localObject1).addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
      {
        public void onSearchCollapse()
        {
          LocationActivity.access$202(LocationActivity.this, false);
          LocationActivity.access$802(LocationActivity.this, false);
          LocationActivity.this.otherItem.setVisibility(0);
          LocationActivity.this.searchListView.setEmptyView(null);
          LocationActivity.this.listView.setVisibility(0);
          LocationActivity.this.mapViewClip.setVisibility(0);
          LocationActivity.this.searchListView.setVisibility(8);
          LocationActivity.this.emptyView.setVisibility(8);
          LocationActivity.this.searchAdapter.searchDelayed(null, null);
        }
        
        public void onSearchExpand()
        {
          LocationActivity.access$202(LocationActivity.this, true);
          LocationActivity.this.otherItem.setVisibility(8);
          LocationActivity.this.listView.setVisibility(8);
          LocationActivity.this.mapViewClip.setVisibility(8);
          LocationActivity.this.searchListView.setVisibility(0);
          LocationActivity.this.searchListView.setEmptyView(LocationActivity.this.emptyView);
        }
        
        public void onTextChanged(EditText paramAnonymousEditText)
        {
          if (LocationActivity.this.searchAdapter == null) {
            return;
          }
          paramAnonymousEditText = paramAnonymousEditText.getText().toString();
          if (paramAnonymousEditText.length() != 0) {
            LocationActivity.access$802(LocationActivity.this, true);
          }
          LocationActivity.this.searchAdapter.searchDelayed(paramAnonymousEditText, LocationActivity.this.userLocation);
        }
      }).getSearchField().setHint(LocaleController.getString("Search", 2131494298));
      break;
      if (this.messageObject.isLiveLocation()) {
        break label1181;
      }
      this.routeButton = new ImageView(paramContext);
      localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("chats_actionBackground"), Theme.getColor("chats_actionPressedBackground"));
      localObject1 = localObject2;
      if (Build.VERSION.SDK_INT < 21)
      {
        paramContext = paramContext.getResources().getDrawable(2131165322).mutate();
        paramContext.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
        localObject1 = new CombinedDrawable(paramContext, (Drawable)localObject2, 0, 0);
        ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
      }
      this.routeButton.setBackgroundDrawable((Drawable)localObject1);
      this.routeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_actionIcon"), PorterDuff.Mode.MULTIPLY));
      this.routeButton.setImageResource(2131165535);
      this.routeButton.setScaleType(ImageView.ScaleType.CENTER);
      if (Build.VERSION.SDK_INT >= 21)
      {
        paramContext = new StateListAnimator();
        localObject1 = ObjectAnimator.ofFloat(this.routeButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        paramContext.addState(new int[] { 16842919 }, (Animator)localObject1);
        localObject1 = ObjectAnimator.ofFloat(this.routeButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        paramContext.addState(new int[0], (Animator)localObject1);
        this.routeButton.setStateListAnimator(paramContext);
        this.routeButton.setOutlineProvider(new ViewOutlineProvider()
        {
          @SuppressLint({"NewApi"})
          public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
          {
            paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
          }
        });
      }
      paramContext = this.routeButton;
      if (Build.VERSION.SDK_INT >= 21)
      {
        i = 56;
        if (Build.VERSION.SDK_INT < 21) {
          break label1899;
        }
        f1 = 56.0F;
        if (!LocaleController.isRTL) {
          break label1906;
        }
        j = 3;
        if (!LocaleController.isRTL) {
          break label1912;
        }
        f2 = 14.0F;
        if (!LocaleController.isRTL) {
          break label1917;
        }
      }
      for (f3 = 0.0F;; f3 = 14.0F)
      {
        localFrameLayout.addView(paramContext, LayoutHelper.createFrame(i, f1, j | 0x50, f2, 0.0F, f3, 37.0F));
        this.routeButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymousView)
          {
            if (Build.VERSION.SDK_INT >= 23)
            {
              paramAnonymousView = LocationActivity.this.getParentActivity();
              if ((paramAnonymousView != null) && (paramAnonymousView.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0)) {
                LocationActivity.this.showPermissionAlert(true);
              }
            }
            while (LocationActivity.this.myLocation == null) {
              return;
            }
            try
            {
              paramAnonymousView = new Intent("android.intent.action.VIEW", Uri.parse(String.format(Locale.US, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f", new Object[] { Double.valueOf(LocationActivity.this.myLocation.getLatitude()), Double.valueOf(LocationActivity.this.myLocation.getLongitude()), Double.valueOf(LocationActivity.this.messageObject.messageOwner.media.geo.lat), Double.valueOf(LocationActivity.this.messageObject.messageOwner.media.geo._long) })));
              LocationActivity.this.getParentActivity().startActivity(paramAnonymousView);
              return;
            }
            catch (Exception paramAnonymousView)
            {
              FileLog.e(paramAnonymousView);
            }
          }
        });
        this.adapter.setMessageObject(this.messageObject);
        break;
        i = 60;
        break label1799;
        f1 = 60.0F;
        break label1811;
        j = 5;
        break label1820;
        f2 = 0.0F;
        break label1830;
      }
      i = 60;
      break label1221;
      f1 = 60.0F;
      break label1233;
      j = 5;
      break label1242;
      f2 = 0.0F;
      break label1252;
    }
    label1958:
    paramContext = this.mapViewClip;
    localObject1 = this.locationButton;
    if (Build.VERSION.SDK_INT >= 21)
    {
      i = 56;
      label1981:
      if (Build.VERSION.SDK_INT < 21) {
        break label2055;
      }
      f1 = 56.0F;
      label1993:
      if (!LocaleController.isRTL) {
        break label2062;
      }
      j = 3;
      label2002:
      if (!LocaleController.isRTL) {
        break label2068;
      }
      f2 = 14.0F;
      label2012:
      if (!LocaleController.isRTL) {
        break label2073;
      }
    }
    label2055:
    label2062:
    label2068:
    label2073:
    for (f3 = 0.0F;; f3 = 14.0F)
    {
      paramContext.addView((View)localObject1, LayoutHelper.createFrame(i, f1, j | 0x50, f2, 0.0F, f3, 14.0F));
      break;
      i = 60;
      break label1981;
      f1 = 60.0F;
      break label1993;
      j = 5;
      break label2002;
      f2 = 0.0F;
      break label2012;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.closeChats) {
      removeSelfFromStack();
    }
    do
    {
      MessageObject localMessageObject;
      long l;
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
                if (paramInt1 != NotificationCenter.locationPermissionGranted) {
                  break;
                }
              } while (this.googleMap == null);
              try
              {
                this.googleMap.setMyLocationEnabled(true);
                return;
              }
              catch (Exception paramVarArgs)
              {
                FileLog.e(paramVarArgs);
                return;
              }
              if (paramInt1 != NotificationCenter.didReceivedNewMessages) {
                break;
              }
            } while ((((Long)paramVarArgs[0]).longValue() != this.dialogId) || (this.messageObject == null));
            paramVarArgs = (ArrayList)paramVarArgs[1];
            paramInt2 = 0;
            paramInt1 = 0;
            while (paramInt1 < paramVarArgs.size())
            {
              localMessageObject = (MessageObject)paramVarArgs.get(paramInt1);
              if (localMessageObject.isLiveLocation())
              {
                addUserMarker(localMessageObject.messageOwner);
                paramInt2 = 1;
              }
              paramInt1 += 1;
            }
          } while ((paramInt2 == 0) || (this.adapter == null));
          this.adapter.setLiveLocations(this.markers);
          return;
        } while ((paramInt1 == NotificationCenter.messagesDeleted) || (paramInt1 != NotificationCenter.replaceMessagesObjects));
        l = ((Long)paramVarArgs[0]).longValue();
      } while ((l != this.dialogId) || (this.messageObject == null));
      paramInt2 = 0;
      paramVarArgs = (ArrayList)paramVarArgs[1];
      paramInt1 = 0;
      if (paramInt1 < paramVarArgs.size())
      {
        localMessageObject = (MessageObject)paramVarArgs.get(paramInt1);
        if (!localMessageObject.isLiveLocation()) {}
        for (;;)
        {
          paramInt1 += 1;
          break;
          LiveLocation localLiveLocation = (LiveLocation)this.markersMap.get(getMessageId(localMessageObject.messageOwner));
          if (localLiveLocation != null)
          {
            LocationController.SharingLocationInfo localSharingLocationInfo = LocationController.getInstance(this.currentAccount).getSharingLocationInfo(l);
            if ((localSharingLocationInfo == null) || (localSharingLocationInfo.mid != localMessageObject.getId())) {
              localLiveLocation.marker.setPosition(new LatLng(localMessageObject.messageOwner.media.geo.lat, localMessageObject.messageOwner.media.geo._long));
            }
            paramInt2 = 1;
          }
        }
      }
    } while ((paramInt2 == 0) || (this.adapter == null));
    this.adapter.updateLiveLocations();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local21 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor() {}
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, "actionBarDefaultSubmenuBackground");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, "actionBarDefaultSubmenuItem");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle"), new ThemeDescription(this.locationButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "profile_actionIcon"), new ThemeDescription(this.locationButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "profile_actionBackground"), new ThemeDescription(this.locationButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "profile_actionPressedBackground"), new ThemeDescription(this.routeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "chats_actionIcon"), new ThemeDescription(this.routeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "chats_actionBackground"), new ThemeDescription(this.routeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "chats_actionPressedBackground"), new ThemeDescription(this.markerXImageView, 0, null, null, null, null, "location_markerX"), new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection"), new ThemeDescription(null, 0, null, null, new Drawable[] { Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable }, local21, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local21, "avatar_backgroundPink"), new ThemeDescription(null, 0, null, null, null, null, "location_liveLocationProgress"), new ThemeDescription(null, 0, null, null, null, null, "location_placeLocationBackground"), new ThemeDescription(null, 0, null, null, null, null, "location_liveLocationProgress"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { SendLocationCell.class }, new String[] { "imageView" }, null, null, null, "location_sendLocationIcon"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { SendLocationCell.class }, new String[] { "imageView" }, null, null, null, "location_sendLocationBackground"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { SendLocationCell.class }, new String[] { "imageView" }, null, null, null, "location_sendLiveLocationBackground"), new ThemeDescription(this.listView, 0, new Class[] { SendLocationCell.class }, new String[] { "titleTextView" }, null, null, null, "windowBackgroundWhiteBlueText7"), new ThemeDescription(this.listView, 0, new Class[] { SendLocationCell.class }, new String[] { "accurateTextView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { LocationCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, 0, new Class[] { LocationCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { LocationCell.class }, new String[] { "addressTextView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.searchListView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { LocationCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.searchListView, 0, new Class[] { LocationCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.searchListView, 0, new Class[] { LocationCell.class }, new String[] { "addressTextView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, 0, new Class[] { LocationLoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle"), new ThemeDescription(this.listView, 0, new Class[] { LocationLoadingCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, 0, new Class[] { LocationPoweredCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, 0, new Class[] { LocationPoweredCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, 0, new Class[] { LocationPoweredCell.class }, new String[] { "textView2" }, null, null, null, "windowBackgroundWhiteGrayText3") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    this.swipeBackEnabled = false;
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
    if ((this.messageObject != null) && (this.messageObject.isLiveLocation()))
    {
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didReceivedNewMessages);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagesDeleted);
      NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.replaceMessagesObjects);
    }
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.locationPermissionGranted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didReceivedNewMessages);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagesDeleted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.replaceMessagesObjects);
    try
    {
      if (this.mapView != null) {
        this.mapView.onDestroy();
      }
      if (this.adapter != null) {
        this.adapter.destroy();
      }
      if (this.searchAdapter != null) {
        this.searchAdapter.destroy();
      }
      if (this.updateRunnable != null)
      {
        AndroidUtilities.cancelRunOnUIThread(this.updateRunnable);
        this.updateRunnable = null;
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
  
  public void onLowMemory()
  {
    super.onLowMemory();
    if ((this.mapView != null) && (this.mapsInitialized)) {
      this.mapView.onLowMemory();
    }
  }
  
  public void onPause()
  {
    super.onPause();
    if ((this.mapView != null) && (this.mapsInitialized)) {}
    try
    {
      this.mapView.onPause();
      this.onResumeCalled = false;
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
  
  public void onResume()
  {
    super.onResume();
    AndroidUtilities.removeAdjustResize(getParentActivity(), this.classGuid);
    if ((this.mapView != null) && (this.mapsInitialized)) {}
    try
    {
      this.mapView.onResume();
      this.onResumeCalled = true;
      if (this.googleMap == null) {}
    }
    catch (Throwable localThrowable)
    {
      try
      {
        this.googleMap.setMyLocationEnabled(true);
        fixLayoutInternal(true);
        if ((this.checkPermission) && (Build.VERSION.SDK_INT >= 23))
        {
          Activity localActivity = getParentActivity();
          if (localActivity != null)
          {
            this.checkPermission = false;
            if (localActivity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
              localActivity.requestPermissions(new String[] { "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, 2);
            }
          }
        }
        return;
        localThrowable = localThrowable;
        FileLog.e(localThrowable);
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
    }
  }
  
  public void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1) {}
    try
    {
      if ((this.mapView.getParent() instanceof ViewGroup)) {
        ((ViewGroup)this.mapView.getParent()).removeView(this.mapView);
      }
      if (this.mapViewClip != null)
      {
        this.mapViewClip.addView(this.mapView, 0, LayoutHelper.createFrame(-1, this.overScrollHeight + AndroidUtilities.dp(10.0F), 51));
        updateClipView(this.layoutManager.findFirstVisibleItemPosition());
        return;
      }
    }
    catch (Exception localException)
    {
      do
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      } while (this.fragmentView == null);
      ((FrameLayout)this.fragmentView).addView(this.mapView, 0, LayoutHelper.createFrame(-1, -1, 51));
    }
  }
  
  public void setDelegate(LocationActivityDelegate paramLocationActivityDelegate)
  {
    this.delegate = paramLocationActivityDelegate;
  }
  
  public void setDialogId(long paramLong)
  {
    this.dialogId = paramLong;
  }
  
  public void setMessageObject(MessageObject paramMessageObject)
  {
    this.messageObject = paramMessageObject;
    this.dialogId = this.messageObject.getDialogId();
  }
  
  public class LiveLocation
  {
    public TLRPC.Chat chat;
    public int id;
    public Marker marker;
    public TLRPC.Message object;
    public TLRPC.User user;
    
    public LiveLocation() {}
  }
  
  public static abstract interface LocationActivityDelegate
  {
    public abstract void didSelectLocation(TLRPC.MessageMedia paramMessageMedia, int paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/LocationActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */