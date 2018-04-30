package org.telegram.ui.Cells;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadioButton;

public class PhotoEditRadioCell
  extends FrameLayout
{
  private int currentColor;
  private int currentType;
  private TextView nameTextView;
  private View.OnClickListener onClickListener;
  private LinearLayout tintButtonsContainer;
  private final int[] tintHighlighsColors = { 0, -1076602, -1388894, -859780, -5968466, -7742235, -13726776, -3303195 };
  private final int[] tintShadowColors = { 0, -45747, -753630, 52480, -8269183, -9321002, -16747844, -10080879 };
  
  public PhotoEditRadioCell(Context paramContext)
  {
    super(paramContext);
    this.nameTextView = new TextView(paramContext);
    this.nameTextView.setGravity(5);
    this.nameTextView.setTextColor(-1);
    this.nameTextView.setTextSize(1, 12.0F);
    this.nameTextView.setMaxLines(1);
    this.nameTextView.setSingleLine(true);
    this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
    addView(this.nameTextView, LayoutHelper.createFrame(80, -2.0F, 19, 0.0F, 0.0F, 0.0F, 0.0F));
    this.tintButtonsContainer = new LinearLayout(paramContext);
    this.tintButtonsContainer.setOrientation(0);
    int i = 0;
    while (i < this.tintShadowColors.length)
    {
      RadioButton localRadioButton = new RadioButton(paramContext);
      localRadioButton.setSize(AndroidUtilities.dp(20.0F));
      localRadioButton.setTag(Integer.valueOf(i));
      this.tintButtonsContainer.addView(localRadioButton, LayoutHelper.createLinear(0, -1, 1.0F / this.tintShadowColors.length));
      localRadioButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = (RadioButton)paramAnonymousView;
          if (PhotoEditRadioCell.this.currentType == 0) {
            PhotoEditRadioCell.access$102(PhotoEditRadioCell.this, PhotoEditRadioCell.this.tintShadowColors[((Integer)paramAnonymousView.getTag()).intValue()]);
          }
          for (;;)
          {
            PhotoEditRadioCell.this.updateSelectedTintButton(true);
            PhotoEditRadioCell.this.onClickListener.onClick(PhotoEditRadioCell.this);
            return;
            PhotoEditRadioCell.access$102(PhotoEditRadioCell.this, PhotoEditRadioCell.this.tintHighlighsColors[((Integer)paramAnonymousView.getTag()).intValue()]);
          }
        }
      });
      i += 1;
    }
    addView(this.tintButtonsContainer, LayoutHelper.createFrame(-1, 40.0F, 51, 96.0F, 0.0F, 24.0F, 0.0F));
  }
  
  private void updateSelectedTintButton(boolean paramBoolean)
  {
    int m = this.tintButtonsContainer.getChildCount();
    int k = 0;
    if (k < m)
    {
      Object localObject = this.tintButtonsContainer.getChildAt(k);
      int j;
      int i;
      label71:
      boolean bool;
      if ((localObject instanceof RadioButton))
      {
        localObject = (RadioButton)localObject;
        j = ((Integer)((RadioButton)localObject).getTag()).intValue();
        if (this.currentType != 0) {
          break label118;
        }
        i = this.tintShadowColors[j];
        if (this.currentColor != i) {
          break label128;
        }
        bool = true;
        label82:
        ((RadioButton)localObject).setChecked(bool, paramBoolean);
        if (j != 0) {
          break label134;
        }
        i = -1;
        label96:
        if (j != 0) {
          break label161;
        }
        j = -1;
      }
      for (;;)
      {
        ((RadioButton)localObject).setColor(i, j);
        k += 1;
        break;
        label118:
        i = this.tintHighlighsColors[j];
        break label71;
        label128:
        bool = false;
        break label82;
        label134:
        if (this.currentType == 0)
        {
          i = this.tintShadowColors[j];
          break label96;
        }
        i = this.tintHighlighsColors[j];
        break label96;
        label161:
        if (this.currentType == 0) {
          j = this.tintShadowColors[j];
        } else {
          j = this.tintHighlighsColors[j];
        }
      }
    }
  }
  
  public int getCurrentColor()
  {
    return this.currentColor;
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40.0F), 1073741824));
  }
  
  public void setIconAndTextAndValue(String paramString, int paramInt1, int paramInt2)
  {
    this.currentType = paramInt1;
    this.currentColor = paramInt2;
    this.nameTextView.setText(paramString.substring(0, 1).toUpperCase() + paramString.substring(1).toLowerCase());
    updateSelectedTintButton(false);
  }
  
  public void setOnClickListener(View.OnClickListener paramOnClickListener)
  {
    this.onClickListener = paramOnClickListener;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/PhotoEditRadioCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */