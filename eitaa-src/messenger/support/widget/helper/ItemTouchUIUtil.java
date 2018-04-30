package ir.eitaa.messenger.support.widget.helper;

import android.graphics.Canvas;
import android.view.View;
import ir.eitaa.messenger.support.widget.RecyclerView;

public abstract interface ItemTouchUIUtil
{
  public abstract void clearView(View paramView);
  
  public abstract void onDraw(Canvas paramCanvas, RecyclerView paramRecyclerView, View paramView, float paramFloat1, float paramFloat2, int paramInt, boolean paramBoolean);
  
  public abstract void onDrawOver(Canvas paramCanvas, RecyclerView paramRecyclerView, View paramView, float paramFloat1, float paramFloat2, int paramInt, boolean paramBoolean);
  
  public abstract void onSelected(View paramView);
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/support/widget/helper/ItemTouchUIUtil.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */