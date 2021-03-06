package ir.eitaa.messenger.support.widget.util;

import ir.eitaa.messenger.support.util.SortedList.Callback;
import ir.eitaa.messenger.support.widget.RecyclerView.Adapter;

public abstract class SortedListAdapterCallback<T2>
  extends SortedList.Callback<T2>
{
  final RecyclerView.Adapter mAdapter;
  
  public SortedListAdapterCallback(RecyclerView.Adapter paramAdapter)
  {
    this.mAdapter = paramAdapter;
  }
  
  public void onChanged(int paramInt1, int paramInt2)
  {
    this.mAdapter.notifyItemRangeChanged(paramInt1, paramInt2);
  }
  
  public void onInserted(int paramInt1, int paramInt2)
  {
    this.mAdapter.notifyItemRangeInserted(paramInt1, paramInt2);
  }
  
  public void onMoved(int paramInt1, int paramInt2)
  {
    this.mAdapter.notifyItemMoved(paramInt1, paramInt2);
  }
  
  public void onRemoved(int paramInt1, int paramInt2)
  {
    this.mAdapter.notifyItemRangeRemoved(paramInt1, paramInt2);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/support/widget/util/SortedListAdapterCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */