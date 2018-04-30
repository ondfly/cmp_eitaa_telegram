package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.ItemDecoration;
import org.telegram.messenger.support.widget.RecyclerView.State;
import org.telegram.ui.ActionBar.Theme;

public class GroupCreateDividerItemDecoration
  extends RecyclerView.ItemDecoration
{
  private boolean searching;
  private boolean single;
  
  public void getItemOffsets(Rect paramRect, View paramView, RecyclerView paramRecyclerView, RecyclerView.State paramState)
  {
    super.getItemOffsets(paramRect, paramView, paramRecyclerView, paramState);
    paramRect.top = 1;
  }
  
  public void onDraw(Canvas paramCanvas, RecyclerView paramRecyclerView, RecyclerView.State paramState)
  {
    int m = paramRecyclerView.getWidth();
    int n = paramRecyclerView.getChildCount();
    int i;
    int j;
    label25:
    int i1;
    float f1;
    label63:
    float f2;
    if (this.single)
    {
      i = 0;
      j = 0;
      if (j >= n - i) {
        return;
      }
      paramState = paramRecyclerView.getChildAt(j);
      paramRecyclerView.getChildAdapterPosition(paramState);
      i1 = paramState.getBottom();
      if (!LocaleController.isRTL) {
        break label116;
      }
      f1 = 0.0F;
      f2 = i1;
      if (!LocaleController.isRTL) {
        break label127;
      }
    }
    label116:
    label127:
    for (int k = AndroidUtilities.dp(72.0F);; k = 0)
    {
      paramCanvas.drawLine(f1, f2, m - k, i1, Theme.dividerPaint);
      j += 1;
      break label25;
      i = 1;
      break;
      f1 = AndroidUtilities.dp(72.0F);
      break label63;
    }
  }
  
  public void setSearching(boolean paramBoolean)
  {
    this.searching = paramBoolean;
  }
  
  public void setSingle(boolean paramBoolean)
  {
    this.single = paramBoolean;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/GroupCreateDividerItemDecoration.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */