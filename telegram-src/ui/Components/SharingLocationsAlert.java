package org.telegram.ui.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.SharingLiveLocationCell;
import org.telegram.ui.StickerPreviewViewer;

public class SharingLocationsAlert
  extends BottomSheet
  implements NotificationCenter.NotificationCenterDelegate
{
  private ListAdapter adapter;
  private SharingLocationsAlertDelegate delegate;
  private boolean ignoreLayout;
  private RecyclerListView listView;
  private int reqId;
  private int scrollOffsetY;
  private Drawable shadowDrawable;
  private TextView textView;
  private Pattern urlPattern;
  
  public SharingLocationsAlert(Context paramContext, SharingLocationsAlertDelegate paramSharingLocationsAlertDelegate)
  {
    super(paramContext, false);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
    this.delegate = paramSharingLocationsAlertDelegate;
    this.shadowDrawable = paramContext.getResources().getDrawable(2131165640).mutate();
    this.shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("dialogBackground"), PorterDuff.Mode.MULTIPLY));
    this.containerView = new FrameLayout(paramContext)
    {
      protected void onDraw(Canvas paramAnonymousCanvas)
      {
        SharingLocationsAlert.this.shadowDrawable.setBounds(0, SharingLocationsAlert.this.scrollOffsetY - SharingLocationsAlert.backgroundPaddingTop, getMeasuredWidth(), getMeasuredHeight());
        SharingLocationsAlert.this.shadowDrawable.draw(paramAnonymousCanvas);
      }
      
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if ((paramAnonymousMotionEvent.getAction() == 0) && (SharingLocationsAlert.this.scrollOffsetY != 0) && (paramAnonymousMotionEvent.getY() < SharingLocationsAlert.this.scrollOffsetY))
        {
          SharingLocationsAlert.this.dismiss();
          return true;
        }
        return super.onInterceptTouchEvent(paramAnonymousMotionEvent);
      }
      
      protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
      {
        super.onLayout(paramAnonymousBoolean, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3, paramAnonymousInt4);
        SharingLocationsAlert.this.updateLayout();
      }
      
      protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
      {
        paramAnonymousInt2 = View.MeasureSpec.getSize(paramAnonymousInt2);
        int i = paramAnonymousInt2;
        if (Build.VERSION.SDK_INT >= 21) {
          i = paramAnonymousInt2 - AndroidUtilities.statusBarHeight;
        }
        getMeasuredWidth();
        int k = AndroidUtilities.dp(56.0F) + AndroidUtilities.dp(56.0F) + 1 + LocationController.getLocationsCount() * AndroidUtilities.dp(54.0F);
        if (k < i / 5 * 3) {
          paramAnonymousInt2 = AndroidUtilities.dp(8.0F);
        }
        for (;;)
        {
          if (SharingLocationsAlert.this.listView.getPaddingTop() != paramAnonymousInt2)
          {
            SharingLocationsAlert.access$202(SharingLocationsAlert.this, true);
            SharingLocationsAlert.this.listView.setPadding(0, paramAnonymousInt2, 0, AndroidUtilities.dp(8.0F));
            SharingLocationsAlert.access$202(SharingLocationsAlert.this, false);
          }
          super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(Math.min(k, i), 1073741824));
          return;
          int j = i / 5 * 2;
          paramAnonymousInt2 = j;
          if (k < i) {
            paramAnonymousInt2 = j - (i - k);
          }
        }
      }
      
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return (!SharingLocationsAlert.this.isDismissed()) && (super.onTouchEvent(paramAnonymousMotionEvent));
      }
      
      public void requestLayout()
      {
        if (SharingLocationsAlert.this.ignoreLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    this.containerView.setWillNotDraw(false);
    this.containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
    this.listView = new RecyclerListView(paramContext)
    {
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        boolean bool1 = false;
        boolean bool2 = StickerPreviewViewer.getInstance().onInterceptTouchEvent(paramAnonymousMotionEvent, SharingLocationsAlert.this.listView, 0, null);
        if ((super.onInterceptTouchEvent(paramAnonymousMotionEvent)) || (bool2)) {
          bool1 = true;
        }
        return bool1;
      }
      
      public void requestLayout()
      {
        if (SharingLocationsAlert.this.ignoreLayout) {
          return;
        }
        super.requestLayout();
      }
    };
    this.listView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
    paramSharingLocationsAlertDelegate = this.listView;
    ListAdapter localListAdapter = new ListAdapter(paramContext);
    this.adapter = localListAdapter;
    paramSharingLocationsAlertDelegate.setAdapter(localListAdapter);
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setClipToPadding(false);
    this.listView.setEnabled(true);
    this.listView.setGlowColor(Theme.getColor("dialogScrollGlow"));
    this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
    {
      public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
      {
        SharingLocationsAlert.this.updateLayout();
      }
    });
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        paramAnonymousInt -= 1;
        if ((paramAnonymousInt < 0) || (paramAnonymousInt >= LocationController.getLocationsCount())) {
          return;
        }
        SharingLocationsAlert.this.delegate.didSelectLocation(SharingLocationsAlert.this.getLocation(paramAnonymousInt));
        SharingLocationsAlert.this.dismiss();
      }
    });
    this.containerView.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
    paramSharingLocationsAlertDelegate = new View(paramContext);
    paramSharingLocationsAlertDelegate.setBackgroundResource(2131165343);
    this.containerView.addView(paramSharingLocationsAlertDelegate, LayoutHelper.createFrame(-1, 3.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
    paramContext = new PickerBottomLayout(paramContext, false);
    paramContext.setBackgroundColor(Theme.getColor("dialogBackground"));
    this.containerView.addView(paramContext, LayoutHelper.createFrame(-1, 48, 83));
    paramContext.cancelButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
    paramContext.cancelButton.setTextColor(Theme.getColor("dialogTextRed"));
    paramContext.cancelButton.setText(LocaleController.getString("StopAllLocationSharings", 2131494436));
    paramContext.cancelButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        int i = 0;
        while (i < 3)
        {
          LocationController.getInstance(i).removeAllLocationSharings();
          i += 1;
        }
        SharingLocationsAlert.this.dismiss();
      }
    });
    paramContext.doneButtonTextView.setTextColor(Theme.getColor("dialogTextBlue2"));
    paramContext.doneButtonTextView.setText(LocaleController.getString("Close", 2131493265).toUpperCase());
    paramContext.doneButton.setPadding(AndroidUtilities.dp(18.0F), 0, AndroidUtilities.dp(18.0F), 0);
    paramContext.doneButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        SharingLocationsAlert.this.dismiss();
      }
    });
    paramContext.doneButtonBadgeTextView.setVisibility(8);
    this.adapter.notifyDataSetChanged();
  }
  
  private LocationController.SharingLocationInfo getLocation(int paramInt)
  {
    int j = 0;
    int i = paramInt;
    paramInt = j;
    while (paramInt < 3)
    {
      ArrayList localArrayList = LocationController.getInstance(paramInt).sharingLocationsUI;
      if (i >= localArrayList.size())
      {
        i -= localArrayList.size();
        paramInt += 1;
      }
      else
      {
        return (LocationController.SharingLocationInfo)localArrayList.get(i);
      }
    }
    return null;
  }
  
  @SuppressLint({"NewApi"})
  private void updateLayout()
  {
    int j = 0;
    int i;
    if (this.listView.getChildCount() <= 0)
    {
      localObject = this.listView;
      i = this.listView.getPaddingTop();
      this.scrollOffsetY = i;
      ((RecyclerListView)localObject).setTopGlowOffset(i);
      this.containerView.invalidate();
    }
    do
    {
      return;
      localObject = this.listView.getChildAt(0);
      RecyclerListView.Holder localHolder = (RecyclerListView.Holder)this.listView.findContainingViewHolder((View)localObject);
      int k = ((View)localObject).getTop() - AndroidUtilities.dp(8.0F);
      i = j;
      if (k > 0)
      {
        i = j;
        if (localHolder != null)
        {
          i = j;
          if (localHolder.getAdapterPosition() == 0) {
            i = k;
          }
        }
      }
    } while (this.scrollOffsetY == i);
    Object localObject = this.listView;
    this.scrollOffsetY = i;
    ((RecyclerListView)localObject).setTopGlowOffset(i);
    this.containerView.invalidate();
  }
  
  protected boolean canDismissWithSwipe()
  {
    return false;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.liveLocationsChanged)
    {
      if (LocationController.getLocationsCount() == 0) {
        dismiss();
      }
    }
    else {
      return;
    }
    this.adapter.notifyDataSetChanged();
  }
  
  public void dismiss()
  {
    super.dismiss();
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context context;
    
    public ListAdapter(Context paramContext)
    {
      this.context = paramContext;
    }
    
    public int getItemCount()
    {
      return LocationController.getLocationsCount() + 1;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (paramInt == 0) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() == 0;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      do
      {
        return;
        ((SharingLiveLocationCell)paramViewHolder.itemView).setDialog(SharingLocationsAlert.this.getLocation(paramInt - 1));
        return;
      } while (SharingLocationsAlert.this.textView == null);
      SharingLocationsAlert.this.textView.setText(LocaleController.formatString("SharingLiveLocationTitle", 2131494398, new Object[] { LocaleController.formatPluralString("Chats", LocationController.getLocationsCount()) }));
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new FrameLayout(this.context)
        {
          protected void onDraw(Canvas paramAnonymousCanvas)
          {
            paramAnonymousCanvas.drawLine(0.0F, AndroidUtilities.dp(40.0F), getMeasuredWidth(), AndroidUtilities.dp(40.0F), Theme.dividerPaint);
          }
          
          protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramAnonymousInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F) + 1, 1073741824));
          }
        };
        paramViewGroup.setWillNotDraw(false);
        SharingLocationsAlert.access$802(SharingLocationsAlert.this, new TextView(this.context));
        SharingLocationsAlert.this.textView.setTextColor(Theme.getColor("dialogIcon"));
        SharingLocationsAlert.this.textView.setTextSize(1, 14.0F);
        SharingLocationsAlert.this.textView.setGravity(17);
        SharingLocationsAlert.this.textView.setPadding(0, 0, 0, AndroidUtilities.dp(8.0F));
        paramViewGroup.addView(SharingLocationsAlert.this.textView, LayoutHelper.createFrame(-1, 40.0F));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new SharingLiveLocationCell(this.context, false);
      }
    }
  }
  
  public static abstract interface SharingLocationsAlertDelegate
  {
    public abstract void didSelectLocation(LocationController.SharingLocationInfo paramSharingLocationInfo);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/SharingLocationsAlert.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */