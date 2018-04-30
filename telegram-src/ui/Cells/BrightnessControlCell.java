package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.SeekBarView.SeekBarViewDelegate;

public class BrightnessControlCell
  extends FrameLayout
{
  private ImageView leftImageView;
  private ImageView rightImageView;
  private SeekBarView seekBarView;
  
  public BrightnessControlCell(Context paramContext)
  {
    super(paramContext);
    this.leftImageView = new ImageView(paramContext);
    this.leftImageView.setImageResource(2131165253);
    addView(this.leftImageView, LayoutHelper.createFrame(24, 24.0F, 51, 17.0F, 12.0F, 0.0F, 0.0F));
    this.seekBarView = new SeekBarView(paramContext)
    {
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        if (paramAnonymousMotionEvent.getAction() == 0) {
          getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(paramAnonymousMotionEvent);
      }
    };
    this.seekBarView.setReportChanges(true);
    this.seekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate()
    {
      public void onSeekBarDrag(float paramAnonymousFloat)
      {
        BrightnessControlCell.this.didChangedValue(paramAnonymousFloat);
      }
    });
    addView(this.seekBarView, LayoutHelper.createFrame(-1, 30.0F, 51, 58.0F, 9.0F, 58.0F, 0.0F));
    this.rightImageView = new ImageView(paramContext);
    this.rightImageView.setImageResource(2131165252);
    addView(this.rightImageView, LayoutHelper.createFrame(24, 24.0F, 53, 0.0F, 12.0F, 17.0F, 0.0F));
  }
  
  protected void didChangedValue(float paramFloat) {}
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.leftImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("profile_actionIcon"), PorterDuff.Mode.MULTIPLY));
    this.rightImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("profile_actionIcon"), PorterDuff.Mode.MULTIPLY));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
  }
  
  public void setProgress(float paramFloat)
  {
    this.seekBarView.setProgress(paramFloat);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/BrightnessControlCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */