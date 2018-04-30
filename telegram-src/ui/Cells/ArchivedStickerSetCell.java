package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

public class ArchivedStickerSetCell
  extends FrameLayout
{
  private Switch checkBox;
  private BackupImageView imageView;
  private boolean needDivider;
  private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
  private Rect rect = new Rect();
  private TLRPC.StickerSetCovered stickersSet;
  private TextView textView;
  private TextView valueTextView;
  
  public ArchivedStickerSetCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    Object localObject = this.textView;
    label124:
    float f1;
    label131:
    label225:
    label247:
    label254:
    label310:
    label318:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 5;
      ((TextView)localObject).setGravity(i);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label437;
      }
      i = 5;
      if (!paramBoolean) {
        break label443;
      }
      f1 = 71.0F;
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, i, 71.0F, 10.0F, f1, 0.0F));
      this.valueTextView = new TextView(paramContext);
      this.valueTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.valueTextView.setTextSize(1, 13.0F);
      this.valueTextView.setLines(1);
      this.valueTextView.setMaxLines(1);
      this.valueTextView.setSingleLine(true);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label449;
      }
      i = 5;
      ((TextView)localObject).setGravity(i);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label455;
      }
      i = 5;
      if (!paramBoolean) {
        break label461;
      }
      f1 = 71.0F;
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, i, 71.0F, 35.0F, f1, 0.0F));
      this.imageView = new BackupImageView(paramContext);
      this.imageView.setAspectFit(true);
      localObject = this.imageView;
      if (!LocaleController.isRTL) {
        break label467;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label473;
      }
      f1 = 0.0F;
      if (!LocaleController.isRTL) {
        break label479;
      }
      f2 = 12.0F;
      label328:
      addView((View)localObject, LayoutHelper.createFrame(48, 48.0F, i | 0x30, f1, 8.0F, f2, 0.0F));
      if (paramBoolean)
      {
        this.checkBox = new Switch(paramContext);
        this.checkBox.setDuplicateParentStateEnabled(false);
        this.checkBox.setFocusable(false);
        this.checkBox.setFocusableInTouchMode(false);
        paramContext = this.checkBox;
        if (!LocaleController.isRTL) {
          break label485;
        }
      }
    }
    label437:
    label443:
    label449:
    label455:
    label461:
    label467:
    label473:
    label479:
    label485:
    for (int i = j;; i = 5)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x10, 14.0F, 0.0F, 14.0F, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label124;
      f1 = 21.0F;
      break label131;
      i = 3;
      break label225;
      i = 3;
      break label247;
      f1 = 21.0F;
      break label254;
      i = 3;
      break label310;
      f1 = 12.0F;
      break label318;
      f2 = 0.0F;
      break label328;
    }
  }
  
  public Switch getCheckBox()
  {
    return this.checkBox;
  }
  
  public TLRPC.StickerSetCovered getStickersSet()
  {
    return this.stickersSet;
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  public TextView getValueTextView()
  {
    return this.valueTextView;
  }
  
  public boolean isChecked()
  {
    return (this.checkBox != null) && (this.checkBox.isChecked());
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(0.0F, getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(64.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if (this.checkBox != null)
    {
      this.checkBox.getHitRect(this.rect);
      if (this.rect.contains((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY()))
      {
        paramMotionEvent.offsetLocation(-this.checkBox.getX(), -this.checkBox.getY());
        return this.checkBox.onTouchEvent(paramMotionEvent);
      }
    }
    return super.onTouchEvent(paramMotionEvent);
  }
  
  public void setChecked(boolean paramBoolean)
  {
    this.checkBox.setOnCheckedChangeListener(null);
    this.checkBox.setChecked(paramBoolean);
    this.checkBox.setOnCheckedChangeListener(this.onCheckedChangeListener);
  }
  
  public void setOnCheckClick(CompoundButton.OnCheckedChangeListener paramOnCheckedChangeListener)
  {
    Switch localSwitch = this.checkBox;
    this.onCheckedChangeListener = paramOnCheckedChangeListener;
    localSwitch.setOnCheckedChangeListener(paramOnCheckedChangeListener);
    this.checkBox.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView) {}
    });
  }
  
  public void setStickersSet(TLRPC.StickerSetCovered paramStickerSetCovered, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    this.stickersSet = paramStickerSetCovered;
    if (!this.needDivider)
    {
      paramBoolean = true;
      setWillNotDraw(paramBoolean);
      this.textView.setText(this.stickersSet.set.title);
      this.valueTextView.setText(LocaleController.formatPluralString("Stickers", paramStickerSetCovered.set.count));
      if ((paramStickerSetCovered.cover == null) || (paramStickerSetCovered.cover.thumb == null) || (paramStickerSetCovered.cover.thumb.location == null)) {
        break label119;
      }
      this.imageView.setImage(paramStickerSetCovered.cover.thumb.location, null, "webp", null);
    }
    label119:
    while (paramStickerSetCovered.covers.isEmpty())
    {
      return;
      paramBoolean = false;
      break;
    }
    this.imageView.setImage(((TLRPC.Document)paramStickerSetCovered.covers.get(0)).thumb.location, null, "webp", null);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ArchivedStickerSetCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */