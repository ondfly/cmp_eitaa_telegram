package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.TL_messages_stickerSet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

public class StickerSetCell
  extends FrameLayout
{
  private BackupImageView imageView;
  private boolean needDivider;
  private ImageView optionsButton;
  private RadialProgressView progressView;
  private Rect rect = new Rect();
  private TLRPC.TL_messages_stickerSet stickersSet;
  private TextView textView;
  private TextView valueTextView;
  
  public StickerSetCell(Context paramContext, int paramInt)
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
    int j;
    label127:
    float f1;
    label136:
    float f2;
    if (LocaleController.isRTL)
    {
      j = 5;
      ((TextView)localObject).setGravity(j);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label484;
      }
      j = 5;
      if (!LocaleController.isRTL) {
        break label490;
      }
      f1 = 40.0F;
      if (!LocaleController.isRTL) {
        break label496;
      }
      f2 = 71.0F;
      label146:
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, j, f1, 10.0F, f2, 0.0F));
      this.valueTextView = new TextView(paramContext);
      this.valueTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.valueTextView.setTextSize(1, 13.0F);
      this.valueTextView.setLines(1);
      this.valueTextView.setMaxLines(1);
      this.valueTextView.setSingleLine(true);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label503;
      }
      j = 5;
      label240:
      ((TextView)localObject).setGravity(j);
      localObject = this.valueTextView;
      if (!LocaleController.isRTL) {
        break label509;
      }
      j = 5;
      label262:
      if (!LocaleController.isRTL) {
        break label515;
      }
      f1 = 40.0F;
      label271:
      if (!LocaleController.isRTL) {
        break label521;
      }
      f2 = 71.0F;
      label281:
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, j, f1, 35.0F, f2, 0.0F));
      this.imageView = new BackupImageView(paramContext);
      this.imageView.setAspectFit(true);
      localObject = this.imageView;
      if (!LocaleController.isRTL) {
        break label528;
      }
      j = 5;
      label337:
      if (!LocaleController.isRTL) {
        break label534;
      }
      f1 = 0.0F;
      label345:
      if (!LocaleController.isRTL) {
        break label540;
      }
      f2 = 12.0F;
      label355:
      addView((View)localObject, LayoutHelper.createFrame(48, 48.0F, j | 0x30, f1, 8.0F, f2, 0.0F));
      if (paramInt != 2) {
        break label563;
      }
      this.progressView = new RadialProgressView(getContext());
      this.progressView.setProgressColor(Theme.getColor("dialogProgressCircle"));
      this.progressView.setSize(AndroidUtilities.dp(30.0F));
      paramContext = this.progressView;
      if (!LocaleController.isRTL) {
        break label546;
      }
      paramInt = k;
      label437:
      if (!LocaleController.isRTL) {
        break label551;
      }
      f1 = 0.0F;
      label445:
      if (!LocaleController.isRTL) {
        break label557;
      }
      f2 = 12.0F;
      label455:
      addView(paramContext, LayoutHelper.createFrame(48, 48.0F, paramInt | 0x30, f1, 8.0F, f2, 0.0F));
    }
    label484:
    label490:
    label496:
    label503:
    label509:
    label515:
    label521:
    label528:
    label534:
    label540:
    label546:
    label551:
    label557:
    label563:
    do
    {
      do
      {
        return;
        j = 3;
        break;
        j = 3;
        break label127;
        f1 = 71.0F;
        break label136;
        f2 = 40.0F;
        break label146;
        j = 3;
        break label240;
        j = 3;
        break label262;
        f1 = 71.0F;
        break label271;
        f2 = 40.0F;
        break label281;
        j = 3;
        break label337;
        f1 = 12.0F;
        break label345;
        f2 = 0.0F;
        break label355;
        paramInt = 3;
        break label437;
        f1 = 12.0F;
        break label445;
        f2 = 0.0F;
        break label455;
      } while (paramInt == 0);
      this.optionsButton = new ImageView(paramContext);
      this.optionsButton.setFocusable(false);
      this.optionsButton.setScaleType(ImageView.ScaleType.CENTER);
      this.optionsButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor("stickers_menuSelector")));
      if (paramInt == 1)
      {
        this.optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("stickers_menu"), PorterDuff.Mode.MULTIPLY));
        this.optionsButton.setImageResource(2131165508);
        paramContext = this.optionsButton;
        if (LocaleController.isRTL) {}
        for (;;)
        {
          addView(paramContext, LayoutHelper.createFrame(40, 40, i | 0x30));
          return;
          i = 5;
        }
      }
    } while (paramInt != 3);
    this.optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
    this.optionsButton.setImageResource(2131165652);
    paramContext = this.optionsButton;
    if (LocaleController.isRTL)
    {
      if (!LocaleController.isRTL) {
        break label780;
      }
      paramInt = 10;
      label739:
      f1 = paramInt;
      if (!LocaleController.isRTL) {
        break label785;
      }
    }
    label780:
    label785:
    for (paramInt = 0;; paramInt = 10)
    {
      addView(paramContext, LayoutHelper.createFrame(40, 40.0F, i | 0x30, f1, 12.0F, paramInt, 0.0F));
      return;
      i = 5;
      break;
      paramInt = 0;
      break label739;
    }
  }
  
  public TLRPC.TL_messages_stickerSet getStickersSet()
  {
    return this.stickersSet;
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
    if ((Build.VERSION.SDK_INT >= 21) && (getBackground() != null) && (this.optionsButton != null))
    {
      this.optionsButton.getHitRect(this.rect);
      if (this.rect.contains((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY())) {
        return true;
      }
    }
    return super.onTouchEvent(paramMotionEvent);
  }
  
  public void setChecked(boolean paramBoolean)
  {
    if (this.optionsButton == null) {
      return;
    }
    ImageView localImageView = this.optionsButton;
    if (paramBoolean) {}
    for (int i = 0;; i = 4)
    {
      localImageView.setVisibility(i);
      return;
    }
  }
  
  public void setOnOptionsClick(View.OnClickListener paramOnClickListener)
  {
    if (this.optionsButton == null) {
      return;
    }
    this.optionsButton.setOnClickListener(paramOnClickListener);
  }
  
  public void setStickersSet(TLRPC.TL_messages_stickerSet paramTL_messages_stickerSet, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    this.stickersSet = paramTL_messages_stickerSet;
    this.imageView.setVisibility(0);
    if (this.progressView != null) {
      this.progressView.setVisibility(4);
    }
    this.textView.setTranslationY(0.0F);
    this.textView.setText(this.stickersSet.set.title);
    if (this.stickersSet.set.archived)
    {
      this.textView.setAlpha(0.5F);
      this.valueTextView.setAlpha(0.5F);
      this.imageView.setAlpha(0.5F);
    }
    for (;;)
    {
      paramTL_messages_stickerSet = paramTL_messages_stickerSet.documents;
      if ((paramTL_messages_stickerSet == null) || (paramTL_messages_stickerSet.isEmpty())) {
        break;
      }
      this.valueTextView.setText(LocaleController.formatPluralString("Stickers", paramTL_messages_stickerSet.size()));
      paramTL_messages_stickerSet = (TLRPC.Document)paramTL_messages_stickerSet.get(0);
      if ((paramTL_messages_stickerSet.thumb != null) && (paramTL_messages_stickerSet.thumb.location != null)) {
        this.imageView.setImage(paramTL_messages_stickerSet.thumb.location, null, "webp", null);
      }
      return;
      this.textView.setAlpha(1.0F);
      this.valueTextView.setAlpha(1.0F);
      this.imageView.setAlpha(1.0F);
    }
    this.valueTextView.setText(LocaleController.formatPluralString("Stickers", 0));
  }
  
  public void setText(String paramString1, String paramString2, int paramInt, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    this.stickersSet = null;
    this.textView.setText(paramString1);
    this.valueTextView.setText(paramString2);
    if (TextUtils.isEmpty(paramString2))
    {
      this.textView.setTranslationY(AndroidUtilities.dp(10.0F));
      if (paramInt == 0) {
        break label100;
      }
      this.imageView.setImageResource(paramInt, Theme.getColor("windowBackgroundWhiteGrayIcon"));
      this.imageView.setVisibility(0);
      if (this.progressView != null) {
        this.progressView.setVisibility(4);
      }
    }
    label100:
    do
    {
      return;
      this.textView.setTranslationY(0.0F);
      break;
      this.imageView.setVisibility(4);
    } while (this.progressView == null);
    this.progressView.setVisibility(0);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/StickerSetCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */