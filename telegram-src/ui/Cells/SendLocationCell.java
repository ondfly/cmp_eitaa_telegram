package org.telegram.ui.Cells;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;

public class SendLocationCell
  extends FrameLayout
{
  private SimpleTextView accurateTextView;
  private int currentAccount = UserConfig.selectedAccount;
  private long dialogId;
  private ImageView imageView;
  private Runnable invalidateRunnable = new Runnable()
  {
    public void run()
    {
      SendLocationCell.this.checkText();
      SendLocationCell.this.invalidate((int)SendLocationCell.this.rect.left - 5, (int)SendLocationCell.this.rect.top - 5, (int)SendLocationCell.this.rect.right + 5, (int)SendLocationCell.this.rect.bottom + 5);
      AndroidUtilities.runOnUIThread(SendLocationCell.this.invalidateRunnable, 1000L);
    }
  };
  private RectF rect;
  private SimpleTextView titleTextView;
  
  public SendLocationCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.imageView = new ImageView(paramContext);
    int i = AndroidUtilities.dp(40.0F);
    Object localObject;
    label66:
    Drawable localDrawable;
    label178:
    label193:
    float f1;
    if (paramBoolean)
    {
      localObject = "location_sendLiveLocationBackground";
      int j = Theme.getColor((String)localObject);
      if (!paramBoolean) {
        break label479;
      }
      localObject = "location_sendLiveLocationBackground";
      localObject = Theme.createSimpleSelectorCircleDrawable(i, j, Theme.getColor((String)localObject));
      if (!paramBoolean) {
        break label486;
      }
      this.rect = new RectF();
      localDrawable = getResources().getDrawable(2131165467);
      localDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("location_sendLocationIcon"), PorterDuff.Mode.MULTIPLY));
      localObject = new CombinedDrawable((Drawable)localObject, localDrawable);
      ((CombinedDrawable)localObject).setCustomSize(AndroidUtilities.dp(40.0F), AndroidUtilities.dp(40.0F));
      this.imageView.setBackgroundDrawable((Drawable)localObject);
      AndroidUtilities.runOnUIThread(this.invalidateRunnable, 1000L);
      setWillNotDraw(false);
      localObject = this.imageView;
      if (!LocaleController.isRTL) {
        break label572;
      }
      i = 5;
      if (!LocaleController.isRTL) {
        break label578;
      }
      f1 = 0.0F;
      label201:
      if (!LocaleController.isRTL) {
        break label584;
      }
      f2 = 17.0F;
      label211:
      addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, i | 0x30, f1, 13.0F, f2, 0.0F));
      this.titleTextView = new SimpleTextView(paramContext);
      this.titleTextView.setTextSize(16);
      if (!paramBoolean) {
        break label590;
      }
      this.titleTextView.setTextColor(Theme.getColor("windowBackgroundWhiteRedText2"));
      label272:
      localObject = this.titleTextView;
      if (!LocaleController.isRTL) {
        break label605;
      }
      i = 5;
      label287:
      ((SimpleTextView)localObject).setGravity(i);
      this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      localObject = this.titleTextView;
      if (!LocaleController.isRTL) {
        break label611;
      }
      i = 5;
      label321:
      if (!LocaleController.isRTL) {
        break label617;
      }
      f1 = 16.0F;
      label330:
      if (!LocaleController.isRTL) {
        break label623;
      }
      f2 = 73.0F;
      label340:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 12.0F, f2, 0.0F));
      this.accurateTextView = new SimpleTextView(paramContext);
      this.accurateTextView.setTextSize(14);
      this.accurateTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      paramContext = this.accurateTextView;
      if (!LocaleController.isRTL) {
        break label630;
      }
      i = 5;
      label410:
      paramContext.setGravity(i);
      paramContext = this.accurateTextView;
      if (!LocaleController.isRTL) {
        break label636;
      }
      i = 5;
      label430:
      if (!LocaleController.isRTL) {
        break label642;
      }
      f1 = 16.0F;
      label439:
      if (!LocaleController.isRTL) {
        break label648;
      }
    }
    label479:
    label486:
    label572:
    label578:
    label584:
    label590:
    label605:
    label611:
    label617:
    label623:
    label630:
    label636:
    label642:
    label648:
    for (float f2 = 73.0F;; f2 = 16.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 37.0F, f2, 0.0F));
      return;
      localObject = "location_sendLocationBackground";
      break;
      localObject = "location_sendLocationBackground";
      break label66;
      localDrawable = getResources().getDrawable(2131165597);
      localDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("location_sendLocationIcon"), PorterDuff.Mode.MULTIPLY));
      localObject = new CombinedDrawable((Drawable)localObject, localDrawable);
      ((CombinedDrawable)localObject).setCustomSize(AndroidUtilities.dp(40.0F), AndroidUtilities.dp(40.0F));
      ((CombinedDrawable)localObject).setIconSize(AndroidUtilities.dp(24.0F), AndroidUtilities.dp(24.0F));
      this.imageView.setBackgroundDrawable((Drawable)localObject);
      break label178;
      i = 3;
      break label193;
      f1 = 17.0F;
      break label201;
      f2 = 0.0F;
      break label211;
      this.titleTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlueText7"));
      break label272;
      i = 3;
      break label287;
      i = 3;
      break label321;
      f1 = 73.0F;
      break label330;
      f2 = 16.0F;
      break label340;
      i = 3;
      break label410;
      i = 3;
      break label430;
      f1 = 73.0F;
      break label439;
    }
  }
  
  private void checkText()
  {
    LocationController.SharingLocationInfo localSharingLocationInfo = LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId);
    if (localSharingLocationInfo != null)
    {
      String str = LocaleController.getString("StopLiveLocation", 2131494438);
      if (localSharingLocationInfo.messageObject.messageOwner.edit_date != 0) {}
      for (long l = localSharingLocationInfo.messageObject.messageOwner.edit_date;; l = localSharingLocationInfo.messageObject.messageOwner.date)
      {
        setText(str, LocaleController.formatLocationUpdateDate(l));
        return;
      }
    }
    setText(LocaleController.getString("SendLiveLocation", 2131494340), LocaleController.getString("SendLiveLocationInfo", 2131494344));
  }
  
  private ImageView getImageView()
  {
    return this.imageView;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (this.rect != null) {
      AndroidUtilities.runOnUIThread(this.invalidateRunnable, 1000L);
    }
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    AndroidUtilities.cancelRunOnUIThread(this.invalidateRunnable);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    Object localObject = LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId);
    if (localObject == null) {}
    int i;
    do
    {
      return;
      i = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    } while (((LocationController.SharingLocationInfo)localObject).stopTime < i);
    float f = Math.abs(((LocationController.SharingLocationInfo)localObject).stopTime - i) / ((LocationController.SharingLocationInfo)localObject).period;
    if (LocaleController.isRTL) {
      this.rect.set(AndroidUtilities.dp(13.0F), AndroidUtilities.dp(18.0F), AndroidUtilities.dp(43.0F), AndroidUtilities.dp(48.0F));
    }
    for (;;)
    {
      int j = Theme.getColor("location_liveLocationProgress");
      Theme.chat_radialProgress2Paint.setColor(j);
      Theme.chat_livePaint.setColor(j);
      paramCanvas.drawArc(this.rect, -90.0F, -360.0F * f, false, Theme.chat_radialProgress2Paint);
      localObject = LocaleController.formatLocationLeftTime(Math.abs(((LocationController.SharingLocationInfo)localObject).stopTime - i));
      f = Theme.chat_livePaint.measureText((String)localObject);
      paramCanvas.drawText((String)localObject, this.rect.centerX() - f / 2.0F, AndroidUtilities.dp(37.0F), Theme.chat_livePaint);
      return;
      this.rect.set(getMeasuredWidth() - AndroidUtilities.dp(43.0F), AndroidUtilities.dp(18.0F), getMeasuredWidth() - AndroidUtilities.dp(13.0F), AndroidUtilities.dp(48.0F));
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(66.0F), 1073741824));
  }
  
  public void setDialogId(long paramLong)
  {
    this.dialogId = paramLong;
    checkText();
  }
  
  public void setHasLocation(boolean paramBoolean)
  {
    float f2 = 1.0F;
    Object localObject;
    if (LocationController.getInstance(this.currentAccount).getSharingLocationInfo(this.dialogId) == null)
    {
      localObject = this.titleTextView;
      if (!paramBoolean) {
        break label74;
      }
      f1 = 1.0F;
      ((SimpleTextView)localObject).setAlpha(f1);
      localObject = this.accurateTextView;
      if (!paramBoolean) {
        break label81;
      }
      f1 = 1.0F;
      label49:
      ((SimpleTextView)localObject).setAlpha(f1);
      localObject = this.imageView;
      if (!paramBoolean) {
        break label88;
      }
    }
    label74:
    label81:
    label88:
    for (float f1 = f2;; f1 = 0.5F)
    {
      ((ImageView)localObject).setAlpha(f1);
      return;
      f1 = 0.5F;
      break;
      f1 = 0.5F;
      break label49;
    }
  }
  
  public void setText(String paramString1, String paramString2)
  {
    this.titleTextView.setText(paramString1);
    this.accurateTextView.setText(paramString2);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/SendLocationCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */