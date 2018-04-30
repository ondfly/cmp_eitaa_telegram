package ir.eitaa.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.eitaa.messenger.query.StickersQuery;
import ir.eitaa.messenger.support.widget.LinearLayoutManager;
import ir.eitaa.messenger.support.widget.RecyclerView;
import ir.eitaa.messenger.support.widget.RecyclerView.Adapter;
import ir.eitaa.messenger.support.widget.RecyclerView.LayoutParams;
import ir.eitaa.messenger.support.widget.RecyclerView.OnScrollListener;
import ir.eitaa.messenger.support.widget.RecyclerView.ViewHolder;
import ir.eitaa.tgnet.ConnectionsManager;
import ir.eitaa.tgnet.RequestDelegate;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.InputStickerSet;
import ir.eitaa.tgnet.TLRPC.StickerSet;
import ir.eitaa.tgnet.TLRPC.StickerSetCovered;
import ir.eitaa.tgnet.TLRPC.TL_error;
import ir.eitaa.tgnet.TLRPC.TL_inputStickerSetID;
import ir.eitaa.tgnet.TLRPC.TL_inputStickerSetShortName;
import ir.eitaa.tgnet.TLRPC.TL_messages_archivedStickers;
import ir.eitaa.tgnet.TLRPC.TL_messages_getArchivedStickers;
import ir.eitaa.ui.ActionBar.ActionBar;
import ir.eitaa.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.eitaa.ui.ActionBar.BaseFragment;
import ir.eitaa.ui.Cells.ArchivedStickerSetCell;
import ir.eitaa.ui.Cells.LoadingCell;
import ir.eitaa.ui.Cells.TextInfoPrivacyCell;
import ir.eitaa.ui.Components.EmptyTextProgressView;
import ir.eitaa.ui.Components.LayoutHelper;
import ir.eitaa.ui.Components.RecyclerListView;
import ir.eitaa.ui.Components.RecyclerListView.OnItemClickListener;
import ir.eitaa.ui.Components.StickersAlert;
import ir.eitaa.ui.Components.StickersAlert.StickersAlertInstallDelegate;
import java.util.ArrayList;

public class ArchivedStickersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private int currentType;
  private EmptyTextProgressView emptyView;
  private boolean endReached;
  private boolean firstLoaded;
  private LinearLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private boolean loadingStickers;
  private int rowCount;
  private ArrayList<TLRPC.StickerSetCovered> sets = new ArrayList();
  private int stickersEndRow;
  private int stickersLoadingRow;
  private int stickersShadowRow;
  private int stickersStartRow;
  
  public ArchivedStickersActivity(int paramInt)
  {
    this.currentType = paramInt;
  }
  
  private void getStickers()
  {
    if ((this.loadingStickers) || (this.endReached)) {
      return;
    }
    this.loadingStickers = true;
    if ((this.emptyView != null) && (!this.firstLoaded)) {
      this.emptyView.showProgress();
    }
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    TLRPC.TL_messages_getArchivedStickers localTL_messages_getArchivedStickers = new TLRPC.TL_messages_getArchivedStickers();
    long l;
    if (this.sets.isEmpty())
    {
      l = 0L;
      localTL_messages_getArchivedStickers.offset_id = l;
      localTL_messages_getArchivedStickers.limit = 15;
      if (this.currentType != 1) {
        break label165;
      }
    }
    label165:
    for (boolean bool = true;; bool = false)
    {
      localTL_messages_getArchivedStickers.masks = bool;
      int i = ConnectionsManager.getInstance().sendRequest(localTL_messages_getArchivedStickers, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              ArchivedStickersActivity localArchivedStickersActivity;
              if (paramAnonymousTL_error == null)
              {
                TLRPC.TL_messages_archivedStickers localTL_messages_archivedStickers = (TLRPC.TL_messages_archivedStickers)paramAnonymousTLObject;
                ArchivedStickersActivity.this.sets.addAll(localTL_messages_archivedStickers.sets);
                localArchivedStickersActivity = ArchivedStickersActivity.this;
                if (localTL_messages_archivedStickers.sets.size() == 15) {
                  break label122;
                }
              }
              label122:
              for (boolean bool = true;; bool = false)
              {
                ArchivedStickersActivity.access$402(localArchivedStickersActivity, bool);
                ArchivedStickersActivity.access$302(ArchivedStickersActivity.this, false);
                ArchivedStickersActivity.access$802(ArchivedStickersActivity.this, true);
                if (ArchivedStickersActivity.this.emptyView != null) {
                  ArchivedStickersActivity.this.emptyView.showTextView();
                }
                ArchivedStickersActivity.this.updateRows();
                return;
              }
            }
          });
        }
      });
      ConnectionsManager.getInstance().bindRequestToGuid(i, this.classGuid);
      return;
      l = ((TLRPC.StickerSetCovered)this.sets.get(this.sets.size() - 1)).set.id;
      break;
    }
  }
  
  private void updateRows()
  {
    this.rowCount = 0;
    int i;
    if (!this.sets.isEmpty())
    {
      this.stickersStartRow = this.rowCount;
      this.stickersEndRow = (this.rowCount + this.sets.size());
      this.rowCount += this.sets.size();
      if (!this.endReached)
      {
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.stickersLoadingRow = i;
        this.stickersShadowRow = -1;
      }
    }
    for (;;)
    {
      if (this.listAdapter != null) {
        this.listAdapter.notifyDataSetChanged();
      }
      return;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.stickersShadowRow = i;
      this.stickersLoadingRow = -1;
      continue;
      this.stickersStartRow = -1;
      this.stickersEndRow = -1;
      this.stickersLoadingRow = -1;
      this.stickersShadowRow = -1;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackButtonImage(2130837705);
    this.actionBar.setAllowOverlayTitle(true);
    FrameLayout localFrameLayout;
    if (this.currentType == 0)
    {
      this.actionBar.setTitle(LocaleController.getString("ArchivedStickers", 2131165307));
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            ArchivedStickersActivity.this.finishFragment();
          }
        }
      });
      this.listAdapter = new ListAdapter(paramContext);
      this.fragmentView = new FrameLayout(paramContext);
      localFrameLayout = (FrameLayout)this.fragmentView;
      localFrameLayout.setBackgroundColor(-986896);
      this.emptyView = new EmptyTextProgressView(paramContext);
      if (this.currentType != 0) {
        break label263;
      }
      this.emptyView.setText(LocaleController.getString("ArchivedStickersEmpty", 2131165310));
      label125:
      localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
      if (!this.loadingStickers) {
        break label282;
      }
      this.emptyView.showProgress();
    }
    for (;;)
    {
      RecyclerListView localRecyclerListView = new RecyclerListView(paramContext);
      localRecyclerListView.setFocusable(true);
      localRecyclerListView.setEmptyView(this.emptyView);
      paramContext = new LinearLayoutManager(paramContext, 1, false);
      this.layoutManager = paramContext;
      localRecyclerListView.setLayoutManager(paramContext);
      localFrameLayout.addView(localRecyclerListView, LayoutHelper.createFrame(-1, -1.0F));
      localRecyclerListView.setAdapter(this.listAdapter);
      localRecyclerListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(final View paramAnonymousView, int paramAnonymousInt)
        {
          TLRPC.StickerSetCovered localStickerSetCovered;
          Object localObject;
          if ((paramAnonymousInt >= ArchivedStickersActivity.this.stickersStartRow) && (paramAnonymousInt < ArchivedStickersActivity.this.stickersEndRow) && (ArchivedStickersActivity.this.getParentActivity() != null))
          {
            localStickerSetCovered = (TLRPC.StickerSetCovered)ArchivedStickersActivity.this.sets.get(paramAnonymousInt);
            if (localStickerSetCovered.set.id == 0L) {
              break label138;
            }
            localObject = new TLRPC.TL_inputStickerSetID();
            ((TLRPC.InputStickerSet)localObject).id = localStickerSetCovered.set.id;
          }
          for (;;)
          {
            ((TLRPC.InputStickerSet)localObject).access_hash = localStickerSetCovered.set.access_hash;
            localObject = new StickersAlert(ArchivedStickersActivity.this.getParentActivity(), ArchivedStickersActivity.this, (TLRPC.InputStickerSet)localObject, null, null);
            ((StickersAlert)localObject).setInstallDelegate(new StickersAlert.StickersAlertInstallDelegate()
            {
              public void onStickerSetInstalled()
              {
                ((ArchivedStickerSetCell)paramAnonymousView).setChecked(true);
              }
              
              public void onStickerSetUninstalled()
              {
                ((ArchivedStickerSetCell)paramAnonymousView).setChecked(false);
              }
            });
            ArchivedStickersActivity.this.showDialog((Dialog)localObject);
            return;
            label138:
            localObject = new TLRPC.TL_inputStickerSetShortName();
            ((TLRPC.InputStickerSet)localObject).short_name = localStickerSetCovered.set.short_name;
          }
        }
      });
      localRecyclerListView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          if ((!ArchivedStickersActivity.this.loadingStickers) && (!ArchivedStickersActivity.this.endReached) && (ArchivedStickersActivity.this.layoutManager.findLastVisibleItemPosition() > ArchivedStickersActivity.this.stickersLoadingRow - 2)) {
            ArchivedStickersActivity.this.getStickers();
          }
        }
      });
      return this.fragmentView;
      this.actionBar.setTitle(LocaleController.getString("ArchivedMasks", 2131165302));
      break;
      label263:
      this.emptyView.setText(LocaleController.getString("ArchivedMasksEmpty", 2131165305));
      break label125;
      label282:
      this.emptyView.showTextView();
    }
  }
  
  public void didReceivedNotification(int paramInt, Object... paramVarArgs)
  {
    if (paramInt == NotificationCenter.needReloadArchivedStickers)
    {
      this.firstLoaded = false;
      this.endReached = false;
      this.sets.clear();
      updateRows();
      if (this.emptyView != null) {
        this.emptyView.showProgress();
      }
      getStickers();
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    getStickers();
    updateRows();
    NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadArchivedStickers);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadArchivedStickers);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
  }
  
  private class ListAdapter
    extends RecyclerView.Adapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getItemCount()
    {
      return ArchivedStickersActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt >= ArchivedStickersActivity.this.stickersStartRow) && (paramInt < ArchivedStickersActivity.this.stickersEndRow)) {}
      do
      {
        return 0;
        if (paramInt == ArchivedStickersActivity.this.stickersLoadingRow) {
          return 1;
        }
      } while (paramInt != ArchivedStickersActivity.this.stickersShadowRow);
      return 2;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      TLRPC.StickerSetCovered localStickerSetCovered;
      if (getItemViewType(paramInt) == 0)
      {
        paramViewHolder = (ArchivedStickerSetCell)paramViewHolder.itemView;
        paramViewHolder.setTag(Integer.valueOf(paramInt));
        localStickerSetCovered = (TLRPC.StickerSetCovered)ArchivedStickersActivity.this.sets.get(paramInt);
        if (paramInt == ArchivedStickersActivity.this.sets.size() - 1) {
          break label82;
        }
      }
      label82:
      for (boolean bool = true;; bool = false)
      {
        paramViewHolder.setStickersSet(localStickerSetCovered, bool, false);
        paramViewHolder.setChecked(StickersQuery.isStickerPackInstalled(localStickerSetCovered.set.id));
        return;
      }
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
        return new Holder(paramViewGroup);
        paramViewGroup = new ArchivedStickerSetCell(this.mContext, true);
        paramViewGroup.setBackgroundResource(2130837801);
        ((ArchivedStickerSetCell)paramViewGroup).setOnCheckClick(new CompoundButton.OnCheckedChangeListener()
        {
          public void onCheckedChanged(CompoundButton paramAnonymousCompoundButton, boolean paramAnonymousBoolean)
          {
            paramAnonymousCompoundButton = (ArchivedStickerSetCell)paramAnonymousCompoundButton.getParent();
            Object localObject = (TLRPC.StickerSetCovered)ArchivedStickersActivity.this.sets.get(((Integer)paramAnonymousCompoundButton.getTag()).intValue());
            paramAnonymousCompoundButton = ArchivedStickersActivity.this.getParentActivity();
            localObject = ((TLRPC.StickerSetCovered)localObject).set;
            if (!paramAnonymousBoolean) {}
            for (int i = 1;; i = 2)
            {
              StickersQuery.removeStickersSet(paramAnonymousCompoundButton, (TLRPC.StickerSet)localObject, i, ArchivedStickersActivity.this, false);
              return;
            }
          }
        });
        continue;
        paramViewGroup = new LoadingCell(this.mContext);
        paramViewGroup.setBackgroundResource(2130837695);
        continue;
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundResource(2130837695);
      }
    }
    
    private class Holder
      extends RecyclerView.ViewHolder
    {
      public Holder(View paramView)
      {
        super();
      }
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/ArchivedStickersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */