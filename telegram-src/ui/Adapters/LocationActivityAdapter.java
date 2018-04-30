package org.telegram.ui.Adapters;

import android.content.Context;
import android.location.Location;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.LocationLoadingCell;
import org.telegram.ui.Cells.LocationPoweredCell;
import org.telegram.ui.Cells.SendLocationCell;
import org.telegram.ui.Cells.SharingLiveLocationCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.LocationActivity.LiveLocation;

public class LocationActivityAdapter
  extends BaseLocationAdapter
{
  private int currentAccount = UserConfig.selectedAccount;
  private ArrayList<LocationActivity.LiveLocation> currentLiveLocations = new ArrayList();
  private MessageObject currentMessageObject;
  private Location customLocation;
  private long dialogId;
  private Location gpsLocation;
  private int liveLocationType;
  private Context mContext;
  private int overScrollHeight;
  private boolean pulledUp;
  private SendLocationCell sendLocationCell;
  private int shareLiveLocationPotistion = -1;
  
  public LocationActivityAdapter(Context paramContext, int paramInt, long paramLong)
  {
    this.mContext = paramContext;
    this.liveLocationType = paramInt;
    this.dialogId = paramLong;
  }
  
  private void updateCell()
  {
    if (this.sendLocationCell != null)
    {
      if (this.customLocation != null) {
        this.sendLocationCell.setText(LocaleController.getString("SendSelectedLocation", 2131494351), String.format(Locale.US, "(%f,%f)", new Object[] { Double.valueOf(this.customLocation.getLatitude()), Double.valueOf(this.customLocation.getLongitude()) }));
      }
    }
    else {
      return;
    }
    if (this.gpsLocation != null)
    {
      this.sendLocationCell.setText(LocaleController.getString("SendLocation", 2131494345), LocaleController.formatString("AccurateTo", 2131492870, new Object[] { LocaleController.formatPluralString("Meters", (int)this.gpsLocation.getAccuracy()) }));
      return;
    }
    this.sendLocationCell.setText(LocaleController.getString("SendLocation", 2131494345), LocaleController.getString("Loading", 2131493762));
  }
  
  public Object getItem(int paramInt)
  {
    Object localObject2 = null;
    Object localObject1;
    if (this.currentMessageObject != null) {
      if (paramInt == 1) {
        localObject1 = this.currentMessageObject;
      }
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
              do
              {
                do
                {
                  return localObject1;
                  localObject1 = localObject2;
                } while (paramInt <= 3);
                localObject1 = localObject2;
              } while (paramInt >= this.places.size() + 3);
              return this.currentLiveLocations.get(paramInt - 4);
              if (this.liveLocationType != 2) {
                break;
              }
              localObject1 = localObject2;
            } while (paramInt < 2);
            return this.currentLiveLocations.get(paramInt - 2);
            if (this.liveLocationType != 1) {
              break;
            }
            localObject1 = localObject2;
          } while (paramInt <= 3);
          localObject1 = localObject2;
        } while (paramInt >= this.places.size() + 3);
        return this.places.get(paramInt - 4);
        localObject1 = localObject2;
      } while (paramInt <= 2);
      localObject1 = localObject2;
    } while (paramInt >= this.places.size() + 2);
    return this.places.get(paramInt - 3);
  }
  
  public int getItemCount()
  {
    int i = 0;
    int j = 0;
    int k = 0;
    if (this.currentMessageObject != null)
    {
      if (this.currentLiveLocations.isEmpty()) {}
      for (i = k;; i = this.currentLiveLocations.size() + 2) {
        return i + 2;
      }
    }
    if (this.liveLocationType == 2) {
      return this.currentLiveLocations.size() + 2;
    }
    if ((this.searching) || ((!this.searching) && (this.places.isEmpty())))
    {
      if (this.liveLocationType != 0) {
        return 5;
      }
      return 4;
    }
    if (this.liveLocationType == 1)
    {
      j = this.places.size();
      if (this.places.isEmpty()) {}
      for (;;)
      {
        return i + (j + 4);
        i = 1;
      }
    }
    k = this.places.size();
    if (this.places.isEmpty()) {}
    for (i = j;; i = 1) {
      return i + (k + 3);
    }
  }
  
  public int getItemViewType(int paramInt)
  {
    int i = 2;
    if (paramInt == 0) {
      i = 0;
    }
    do
    {
      do
      {
        do
        {
          return i;
          if (this.currentMessageObject == null) {
            break;
          }
        } while (paramInt == 2);
        if (paramInt == 3)
        {
          this.shareLiveLocationPotistion = paramInt;
          return 6;
        }
        return 7;
        if (this.liveLocationType == 2)
        {
          if (paramInt == 1)
          {
            this.shareLiveLocationPotistion = paramInt;
            return 6;
          }
          return 7;
        }
        if (this.liveLocationType != 1) {
          break;
        }
        if (paramInt == 1) {
          return 1;
        }
        if (paramInt == 2)
        {
          this.shareLiveLocationPotistion = paramInt;
          return 6;
        }
      } while (paramInt == 3);
      if ((this.searching) || ((!this.searching) && (this.places.isEmpty()))) {
        return 4;
      }
      if (paramInt != this.places.size() + 4) {
        break;
      }
      return 5;
      if (paramInt == 1) {
        return 1;
      }
    } while (paramInt == 2);
    if ((this.searching) || ((!this.searching) && (this.places.isEmpty()))) {
      return 4;
    }
    if (paramInt == this.places.size() + 3) {
      return 5;
    }
    return 3;
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    boolean bool = false;
    int i = paramViewHolder.getItemViewType();
    if (i == 6) {
      if ((LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId) != null) || (this.gpsLocation != null)) {
        bool = true;
      }
    }
    while ((i != 1) && (i != 3) && (i != 7)) {
      return bool;
    }
    return true;
  }
  
  public boolean isPulledUp()
  {
    return this.pulledUp;
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    switch (paramViewHolder.getItemViewType())
    {
    case 5: 
    default: 
      return;
    case 0: 
      ((EmptyCell)paramViewHolder.itemView).setHeight(this.overScrollHeight);
      return;
    case 1: 
      this.sendLocationCell = ((SendLocationCell)paramViewHolder.itemView);
      updateCell();
      return;
    case 2: 
      if (this.currentMessageObject != null)
      {
        ((GraySectionCell)paramViewHolder.itemView).setText(LocaleController.getString("LiveLocations", 2131493761));
        return;
      }
      if (this.pulledUp)
      {
        ((GraySectionCell)paramViewHolder.itemView).setText(LocaleController.getString("NearbyPlaces", 2131493859));
        return;
      }
      ((GraySectionCell)paramViewHolder.itemView).setText(LocaleController.getString("ShowNearbyPlaces", 2131494405));
      return;
    case 3: 
      if (this.liveLocationType == 0)
      {
        ((LocationCell)paramViewHolder.itemView).setLocation((TLRPC.TL_messageMediaVenue)this.places.get(paramInt - 3), (String)this.iconUrls.get(paramInt - 3), true);
        return;
      }
      ((LocationCell)paramViewHolder.itemView).setLocation((TLRPC.TL_messageMediaVenue)this.places.get(paramInt - 4), (String)this.iconUrls.get(paramInt - 4), true);
      return;
    case 4: 
      ((LocationLoadingCell)paramViewHolder.itemView).setLoading(this.searching);
      return;
    case 6: 
      paramViewHolder = (SendLocationCell)paramViewHolder.itemView;
      if (this.gpsLocation != null) {}
      for (boolean bool = true;; bool = false)
      {
        paramViewHolder.setHasLocation(bool);
        return;
      }
    }
    if ((this.currentMessageObject != null) && (paramInt == 1))
    {
      ((SharingLiveLocationCell)paramViewHolder.itemView).setDialog(this.currentMessageObject, this.gpsLocation);
      return;
    }
    paramViewHolder = (SharingLiveLocationCell)paramViewHolder.itemView;
    ArrayList localArrayList = this.currentLiveLocations;
    if (this.currentMessageObject != null) {}
    for (int i = 4;; i = 2)
    {
      paramViewHolder.setDialog((LocationActivity.LiveLocation)localArrayList.get(paramInt - i), this.gpsLocation);
      return;
    }
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    switch (paramInt)
    {
    default: 
      paramViewGroup = new SharingLiveLocationCell(this.mContext, true);
    }
    for (;;)
    {
      return new RecyclerListView.Holder(paramViewGroup);
      paramViewGroup = new EmptyCell(this.mContext);
      continue;
      paramViewGroup = new SendLocationCell(this.mContext, false);
      continue;
      paramViewGroup = new GraySectionCell(this.mContext);
      continue;
      paramViewGroup = new LocationCell(this.mContext);
      continue;
      paramViewGroup = new LocationLoadingCell(this.mContext);
      continue;
      paramViewGroup = new LocationPoweredCell(this.mContext);
      continue;
      paramViewGroup = new SendLocationCell(this.mContext, true);
      paramViewGroup.setDialogId(this.dialogId);
    }
  }
  
  public void setCustomLocation(Location paramLocation)
  {
    this.customLocation = paramLocation;
    updateCell();
  }
  
  public void setGpsLocation(Location paramLocation)
  {
    if (this.gpsLocation == null) {}
    for (int i = 1;; i = 0)
    {
      this.gpsLocation = paramLocation;
      if ((i != 0) && (this.shareLiveLocationPotistion > 0)) {
        notifyItemChanged(this.shareLiveLocationPotistion);
      }
      if (this.currentMessageObject == null) {
        break;
      }
      notifyItemChanged(1);
      updateLiveLocations();
      return;
    }
    if (this.liveLocationType != 2)
    {
      updateCell();
      return;
    }
    updateLiveLocations();
  }
  
  public void setLiveLocations(ArrayList<LocationActivity.LiveLocation> paramArrayList)
  {
    this.currentLiveLocations = new ArrayList(paramArrayList);
    int j = UserConfig.getInstance(this.currentAccount).getClientUserId();
    int i = 0;
    for (;;)
    {
      if (i < this.currentLiveLocations.size())
      {
        if (((LocationActivity.LiveLocation)this.currentLiveLocations.get(i)).id == j) {
          this.currentLiveLocations.remove(i);
        }
      }
      else
      {
        notifyDataSetChanged();
        return;
      }
      i += 1;
    }
  }
  
  public void setMessageObject(MessageObject paramMessageObject)
  {
    this.currentMessageObject = paramMessageObject;
    notifyDataSetChanged();
  }
  
  public void setOverScrollHeight(int paramInt)
  {
    this.overScrollHeight = paramInt;
  }
  
  public void setPulledUp()
  {
    if (this.pulledUp) {
      return;
    }
    this.pulledUp = true;
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        LocationActivityAdapter localLocationActivityAdapter = LocationActivityAdapter.this;
        if (LocationActivityAdapter.this.liveLocationType == 0) {}
        for (int i = 2;; i = 3)
        {
          localLocationActivityAdapter.notifyItemChanged(i);
          return;
        }
      }
    });
  }
  
  public void updateLiveLocations()
  {
    if (!this.currentLiveLocations.isEmpty()) {
      notifyItemRangeChanged(2, this.currentLiveLocations.size());
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/LocationActivityAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */