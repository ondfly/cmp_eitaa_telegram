package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class PaymentInfoCell
  extends FrameLayout
{
  private TextView detailExTextView;
  private TextView detailTextView;
  private BackupImageView imageView;
  private TextView nameTextView;
  
  public PaymentInfoCell(Context paramContext)
  {
    super(paramContext);
    this.imageView = new BackupImageView(paramContext);
    Object localObject = this.imageView;
    int i;
    label149:
    label174:
    float f1;
    if (LocaleController.isRTL)
    {
      i = 5;
      addView((View)localObject, LayoutHelper.createFrame(100, 100.0F, i, 10.0F, 10.0F, 10.0F, 0.0F));
      this.nameTextView = new TextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTextSize(1, 16.0F);
      this.nameTextView.setLines(1);
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.nameTextView.setMaxLines(1);
      this.nameTextView.setSingleLine(true);
      this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label497;
      }
      i = 5;
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label503;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label509;
      }
      f1 = 10.0F;
      label183:
      if (!LocaleController.isRTL) {
        break label515;
      }
      f2 = 123.0F;
      label192:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 9.0F, f2, 0.0F));
      this.detailTextView = new TextView(paramContext);
      this.detailTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.detailTextView.setTextSize(1, 14.0F);
      this.detailTextView.setMaxLines(3);
      this.detailTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.detailTextView;
      if (!LocaleController.isRTL) {
        break label521;
      }
      i = 5;
      label281:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.detailTextView;
      if (!LocaleController.isRTL) {
        break label527;
      }
      i = 5;
      label306:
      if (!LocaleController.isRTL) {
        break label533;
      }
      f1 = 10.0F;
      label315:
      if (!LocaleController.isRTL) {
        break label539;
      }
      f2 = 123.0F;
      label324:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 33.0F, f2, 0.0F));
      this.detailExTextView = new TextView(paramContext);
      this.detailExTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.detailExTextView.setTextSize(1, 13.0F);
      this.detailExTextView.setLines(1);
      this.detailExTextView.setMaxLines(1);
      this.detailExTextView.setSingleLine(true);
      this.detailExTextView.setEllipsize(TextUtils.TruncateAt.END);
      paramContext = this.detailExTextView;
      if (!LocaleController.isRTL) {
        break label545;
      }
      i = 5;
      label428:
      paramContext.setGravity(i | 0x30);
      paramContext = this.detailExTextView;
      if (!LocaleController.isRTL) {
        break label551;
      }
      i = 5;
      label451:
      if (!LocaleController.isRTL) {
        break label557;
      }
      f1 = 10.0F;
      label460:
      if (!LocaleController.isRTL) {
        break label563;
      }
    }
    label497:
    label503:
    label509:
    label515:
    label521:
    label527:
    label533:
    label539:
    label545:
    label551:
    label557:
    label563:
    for (float f2 = 123.0F;; f2 = 10.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 90.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      i = 3;
      break label149;
      i = 3;
      break label174;
      f1 = 123.0F;
      break label183;
      f2 = 10.0F;
      break label192;
      i = 3;
      break label281;
      i = 3;
      break label306;
      f1 = 123.0F;
      break label315;
      f2 = 10.0F;
      break label324;
      i = 3;
      break label428;
      i = 3;
      break label451;
      f1 = 123.0F;
      break label460;
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
    paramInt1 = this.detailTextView.getBottom() + AndroidUtilities.dp(3.0F);
    this.detailExTextView.layout(this.detailExTextView.getLeft(), paramInt1, this.detailExTextView.getRight(), this.detailExTextView.getMeasuredHeight() + paramInt1);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(120.0F), 1073741824));
  }
  
  public void setInvoice(TLRPC.TL_messageMediaInvoice paramTL_messageMediaInvoice, String paramString)
  {
    this.nameTextView.setText(paramTL_messageMediaInvoice.title);
    this.detailTextView.setText(paramTL_messageMediaInvoice.description);
    this.detailExTextView.setText(paramString);
    float f1;
    int j;
    int k;
    if (AndroidUtilities.isTablet())
    {
      i = (int)(AndroidUtilities.getMinTabletSide() * 0.7F);
      f1 = 'ʀ' / (i - AndroidUtilities.dp(2.0F));
      j = (int)('ʀ' / f1);
      k = (int)('Ũ' / f1);
      if ((paramTL_messageMediaInvoice.photo == null) || (!paramTL_messageMediaInvoice.photo.mime_type.startsWith("image/"))) {
        break label402;
      }
      paramString = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label345;
      }
      i = 5;
      label114:
      if (!LocaleController.isRTL) {
        break label351;
      }
      f1 = 10.0F;
      label123:
      if (!LocaleController.isRTL) {
        break label357;
      }
      f2 = 123.0F;
      label133:
      paramString.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 9.0F, f2, 0.0F));
      paramString = this.detailTextView;
      if (!LocaleController.isRTL) {
        break label364;
      }
      i = 5;
      label168:
      if (!LocaleController.isRTL) {
        break label370;
      }
      f1 = 10.0F;
      label177:
      if (!LocaleController.isRTL) {
        break label376;
      }
      f2 = 123.0F;
      label187:
      paramString.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 33.0F, f2, 0.0F));
      paramString = this.detailExTextView;
      if (!LocaleController.isRTL) {
        break label383;
      }
      i = 5;
      label222:
      if (!LocaleController.isRTL) {
        break label389;
      }
      f1 = 10.0F;
      label231:
      if (!LocaleController.isRTL) {
        break label395;
      }
    }
    label345:
    label351:
    label357:
    label364:
    label370:
    label376:
    label383:
    label389:
    label395:
    for (float f2 = 123.0F;; f2 = 10.0F)
    {
      paramString.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 90.0F, f2, 0.0F));
      this.imageView.setVisibility(0);
      this.imageView.getImageReceiver().setImage(paramTL_messageMediaInvoice.photo, null, String.format(Locale.US, "%d_%d", new Object[] { Integer.valueOf(j), Integer.valueOf(k) }), null, null, null, -1, null, 1);
      return;
      i = (int)(Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.7F);
      break;
      i = 3;
      break label114;
      f1 = 123.0F;
      break label123;
      f2 = 10.0F;
      break label133;
      i = 3;
      break label168;
      f1 = 123.0F;
      break label177;
      f2 = 10.0F;
      break label187;
      i = 3;
      break label222;
      f1 = 123.0F;
      break label231;
    }
    label402:
    paramTL_messageMediaInvoice = this.nameTextView;
    if (LocaleController.isRTL)
    {
      i = 5;
      paramTL_messageMediaInvoice.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, 17.0F, 9.0F, 17.0F, 0.0F));
      paramTL_messageMediaInvoice = this.detailTextView;
      if (!LocaleController.isRTL) {
        break label526;
      }
      i = 5;
      label452:
      paramTL_messageMediaInvoice.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, 17.0F, 33.0F, 17.0F, 0.0F));
      paramTL_messageMediaInvoice = this.detailExTextView;
      if (!LocaleController.isRTL) {
        break label532;
      }
    }
    label526:
    label532:
    for (int i = 5;; i = 3)
    {
      paramTL_messageMediaInvoice.setLayoutParams(LayoutHelper.createFrame(-1, -2.0F, i | 0x30, 17.0F, 90.0F, 17.0F, 0.0F));
      this.imageView.setVisibility(8);
      return;
      i = 3;
      break;
      i = 3;
      break label452;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/PaymentInfoCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */