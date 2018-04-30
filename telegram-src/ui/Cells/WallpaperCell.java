package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_wallPaperSolid;
import org.telegram.tgnet.TLRPC.WallPaper;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class WallpaperCell
  extends FrameLayout
{
  private BackupImageView imageView;
  private ImageView imageView2;
  private View selectionView;
  
  public WallpaperCell(Context paramContext)
  {
    super(paramContext);
    this.imageView = new BackupImageView(paramContext);
    addView(this.imageView, LayoutHelper.createFrame(100, 100, 83));
    this.imageView2 = new ImageView(paramContext);
    this.imageView2.setImageResource(2131165380);
    this.imageView2.setScaleType(ImageView.ScaleType.CENTER);
    addView(this.imageView2, LayoutHelper.createFrame(100, 100, 83));
    this.selectionView = new View(paramContext);
    this.selectionView.setBackgroundResource(2131165703);
    addView(this.selectionView, LayoutHelper.createFrame(100, 102.0F));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102.0F), 1073741824));
  }
  
  public void setWallpaper(TLRPC.WallPaper paramWallPaper, int paramInt, Drawable paramDrawable, boolean paramBoolean)
  {
    int j = 0;
    int i = 0;
    if (paramWallPaper == null)
    {
      this.imageView.setVisibility(4);
      this.imageView2.setVisibility(0);
      if (paramBoolean)
      {
        paramWallPaper = this.selectionView;
        if (paramInt == -2) {}
        for (paramInt = 0;; paramInt = 4)
        {
          paramWallPaper.setVisibility(paramInt);
          this.imageView2.setImageDrawable(paramDrawable);
          this.imageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);
          return;
        }
      }
      paramWallPaper = this.selectionView;
      if (paramInt == -1)
      {
        paramWallPaper.setVisibility(i);
        paramWallPaper = this.imageView2;
        if ((paramInt != -1) && (paramInt != 1000001)) {
          break label139;
        }
      }
      label139:
      for (paramInt = 1514625126;; paramInt = 1509949440)
      {
        paramWallPaper.setBackgroundColor(paramInt);
        this.imageView2.setScaleType(ImageView.ScaleType.CENTER);
        this.imageView2.setImageResource(2131165380);
        return;
        i = 4;
        break;
      }
    }
    this.imageView.setVisibility(0);
    this.imageView2.setVisibility(4);
    paramDrawable = this.selectionView;
    if (paramInt == paramWallPaper.id) {}
    for (paramInt = j;; paramInt = 4)
    {
      paramDrawable.setVisibility(paramInt);
      if (!(paramWallPaper instanceof TLRPC.TL_wallPaperSolid)) {
        break;
      }
      this.imageView.setImageBitmap(null);
      this.imageView.setBackgroundColor(0xFF000000 | paramWallPaper.bg_color);
      return;
    }
    j = AndroidUtilities.dp(100.0F);
    paramDrawable = null;
    paramInt = 0;
    if (paramInt < paramWallPaper.sizes.size())
    {
      TLRPC.PhotoSize localPhotoSize = (TLRPC.PhotoSize)paramWallPaper.sizes.get(paramInt);
      if (localPhotoSize == null) {}
      label343:
      for (;;)
      {
        paramInt += 1;
        break;
        if (localPhotoSize.w >= localPhotoSize.h) {}
        for (i = localPhotoSize.w;; i = localPhotoSize.h)
        {
          if ((paramDrawable != null) && ((j <= 100) || (paramDrawable.location == null) || (paramDrawable.location.dc_id != Integer.MIN_VALUE)) && (!(localPhotoSize instanceof TLRPC.TL_photoCachedSize)) && (i > j)) {
            break label343;
          }
          paramDrawable = localPhotoSize;
          break;
        }
      }
    }
    if ((paramDrawable != null) && (paramDrawable.location != null)) {
      this.imageView.setImage(paramDrawable.location, "100_100", (Drawable)null);
    }
    this.imageView.setBackgroundColor(1514625126);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/WallpaperCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */