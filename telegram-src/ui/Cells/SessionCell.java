package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_authorization;
import org.telegram.tgnet.TLRPC.TL_webAuthorization;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class SessionCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable;
  private int currentAccount = UserConfig.selectedAccount;
  private TextView detailExTextView;
  private TextView detailTextView;
  private BackupImageView imageView;
  private TextView nameTextView;
  private boolean needDivider;
  private TextView onlineTextView;
  
  public SessionCell(Context paramContext, int paramInt)
  {
    super(paramContext);
    Object localObject1 = new LinearLayout(paramContext);
    ((LinearLayout)localObject1).setOrientation(0);
    ((LinearLayout)localObject1).setWeightSum(1.0F);
    int i;
    label57:
    float f;
    if (paramInt == 1) {
      if (LocaleController.isRTL)
      {
        paramInt = 5;
        if (!LocaleController.isRTL) {
          break label657;
        }
        i = 11;
        f = i;
        if (!LocaleController.isRTL) {
          break label664;
        }
        i = 45;
        label71:
        addView((View)localObject1, LayoutHelper.createFrame(-1, 30.0F, paramInt | 0x30, f, 11.0F, i, 0.0F));
        this.avatarDrawable = new AvatarDrawable();
        this.avatarDrawable.setTextSize(AndroidUtilities.dp(10.0F));
        this.imageView = new BackupImageView(paramContext);
        this.imageView.setRoundRadius(AndroidUtilities.dp(10.0F));
        Object localObject2 = this.imageView;
        if (!LocaleController.isRTL) {
          break label671;
        }
        paramInt = 5;
        label155:
        if (!LocaleController.isRTL) {
          break label676;
        }
        i = 0;
        label164:
        f = i;
        if (!LocaleController.isRTL) {
          break label683;
        }
        i = 17;
        label178:
        addView((View)localObject2, LayoutHelper.createFrame(20, 20.0F, paramInt | 0x30, f, 13.0F, i, 0.0F));
        this.nameTextView = new TextView(paramContext);
        this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.nameTextView.setTextSize(1, 16.0F);
        this.nameTextView.setLines(1);
        this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.nameTextView.setMaxLines(1);
        this.nameTextView.setSingleLine(true);
        this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        localObject2 = this.nameTextView;
        if (!LocaleController.isRTL) {
          break label766;
        }
        paramInt = 5;
        label296:
        ((TextView)localObject2).setGravity(paramInt | 0x30);
        this.onlineTextView = new TextView(paramContext);
        this.onlineTextView.setTextSize(1, 14.0F);
        localObject2 = this.onlineTextView;
        if (!LocaleController.isRTL) {
          break label771;
        }
        paramInt = 3;
        label341:
        ((TextView)localObject2).setGravity(paramInt | 0x30);
        if (!LocaleController.isRTL) {
          break label776;
        }
        ((LinearLayout)localObject1).addView(this.onlineTextView, LayoutHelper.createLinear(-2, -1, 51, 0, 2, 0, 0));
        ((LinearLayout)localObject1).addView(this.nameTextView, LayoutHelper.createLinear(0, -1, 1.0F, 53, 10, 0, 0, 0));
        label399:
        this.detailTextView = new TextView(paramContext);
        this.detailTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
        this.detailTextView.setTextSize(1, 14.0F);
        this.detailTextView.setLines(1);
        this.detailTextView.setMaxLines(1);
        this.detailTextView.setSingleLine(true);
        this.detailTextView.setEllipsize(TextUtils.TruncateAt.END);
        localObject1 = this.detailTextView;
        if (!LocaleController.isRTL) {
          break label822;
        }
        paramInt = 5;
        label481:
        ((TextView)localObject1).setGravity(paramInt | 0x30);
        localObject1 = this.detailTextView;
        if (!LocaleController.isRTL) {
          break label827;
        }
        paramInt = 5;
        label504:
        addView((View)localObject1, LayoutHelper.createFrame(-1, -2.0F, paramInt | 0x30, 17.0F, 36.0F, 17.0F, 0.0F));
        this.detailExTextView = new TextView(paramContext);
        this.detailExTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
        this.detailExTextView.setTextSize(1, 14.0F);
        this.detailExTextView.setLines(1);
        this.detailExTextView.setMaxLines(1);
        this.detailExTextView.setSingleLine(true);
        this.detailExTextView.setEllipsize(TextUtils.TruncateAt.END);
        paramContext = this.detailExTextView;
        if (!LocaleController.isRTL) {
          break label832;
        }
        paramInt = 5;
        label608:
        paramContext.setGravity(paramInt | 0x30);
        paramContext = this.detailExTextView;
        if (!LocaleController.isRTL) {
          break label837;
        }
      }
    }
    label657:
    label664:
    label671:
    label676:
    label683:
    label697:
    label707:
    label752:
    label759:
    label766:
    label771:
    label776:
    label822:
    label827:
    label832:
    label837:
    for (paramInt = 5;; paramInt = 3)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, -2.0F, paramInt | 0x30, 17.0F, 59.0F, 17.0F, 0.0F));
      return;
      paramInt = 3;
      break;
      i = 45;
      break label57;
      i = 11;
      break label71;
      paramInt = 3;
      break label155;
      i = 17;
      break label164;
      i = 0;
      break label178;
      if (LocaleController.isRTL)
      {
        paramInt = 5;
        if (!LocaleController.isRTL) {
          break label752;
        }
        i = 11;
        f = i;
        if (!LocaleController.isRTL) {
          break label759;
        }
      }
      for (i = 17;; i = 11)
      {
        addView((View)localObject1, LayoutHelper.createFrame(-1, 30.0F, paramInt | 0x30, f, 11.0F, i, 0.0F));
        break;
        paramInt = 3;
        break label697;
        i = 17;
        break label707;
      }
      paramInt = 3;
      break label296;
      paramInt = 5;
      break label341;
      ((LinearLayout)localObject1).addView(this.nameTextView, LayoutHelper.createLinear(0, -1, 1.0F, 51, 0, 0, 10, 0));
      ((LinearLayout)localObject1).addView(this.onlineTextView, LayoutHelper.createLinear(-2, -1, 53, 0, 2, 0, 0));
      break label399;
      paramInt = 3;
      break label481;
      paramInt = 3;
      break label504;
      paramInt = 3;
      break label608;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(90.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void setSession(TLObject paramTLObject, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    Object localObject;
    if ((paramTLObject instanceof TLRPC.TL_authorization))
    {
      paramTLObject = (TLRPC.TL_authorization)paramTLObject;
      this.nameTextView.setText(String.format(Locale.US, "%s %s", new Object[] { paramTLObject.app_name, paramTLObject.app_version }));
      if ((paramTLObject.flags & 0x1) != 0)
      {
        setTag("windowBackgroundWhiteValueText");
        this.onlineTextView.setText(LocaleController.getString("Online", 2131494030));
        this.onlineTextView.setTextColor(Theme.getColor("windowBackgroundWhiteValueText"));
        localObject = new StringBuilder();
        if (paramTLObject.ip.length() != 0) {
          ((StringBuilder)localObject).append(paramTLObject.ip);
        }
        if (paramTLObject.country.length() != 0)
        {
          if (((StringBuilder)localObject).length() != 0) {
            ((StringBuilder)localObject).append(" ");
          }
          ((StringBuilder)localObject).append("— ");
          ((StringBuilder)localObject).append(paramTLObject.country);
        }
        this.detailExTextView.setText((CharSequence)localObject);
        localObject = new StringBuilder();
        if (paramTLObject.device_model.length() != 0) {
          ((StringBuilder)localObject).append(paramTLObject.device_model);
        }
        if ((paramTLObject.system_version.length() != 0) || (paramTLObject.platform.length() != 0))
        {
          if (((StringBuilder)localObject).length() != 0) {
            ((StringBuilder)localObject).append(", ");
          }
          if (paramTLObject.platform.length() != 0) {
            ((StringBuilder)localObject).append(paramTLObject.platform);
          }
          if (paramTLObject.system_version.length() != 0)
          {
            if (paramTLObject.platform.length() != 0) {
              ((StringBuilder)localObject).append(" ");
            }
            ((StringBuilder)localObject).append(paramTLObject.system_version);
          }
        }
        if ((paramTLObject.flags & 0x2) == 0)
        {
          if (((StringBuilder)localObject).length() != 0) {
            ((StringBuilder)localObject).append(", ");
          }
          ((StringBuilder)localObject).append(LocaleController.getString("UnofficialApp", 2131494512));
          ((StringBuilder)localObject).append(" (ID: ");
          ((StringBuilder)localObject).append(paramTLObject.api_id);
          ((StringBuilder)localObject).append(")");
        }
        this.detailTextView.setText((CharSequence)localObject);
      }
    }
    while (!(paramTLObject instanceof TLRPC.TL_webAuthorization)) {
      for (;;)
      {
        return;
        setTag("windowBackgroundWhiteGrayText3");
        this.onlineTextView.setText(LocaleController.stringForMessageListDate(paramTLObject.date_active));
        this.onlineTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      }
    }
    TLRPC.TL_webAuthorization localTL_webAuthorization = (TLRPC.TL_webAuthorization)paramTLObject;
    paramTLObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(localTL_webAuthorization.bot_id));
    this.nameTextView.setText(localTL_webAuthorization.domain);
    if (paramTLObject != null)
    {
      this.avatarDrawable.setInfo(paramTLObject);
      localObject = UserObject.getFirstName(paramTLObject);
      if (paramTLObject.photo != null)
      {
        paramTLObject = paramTLObject.photo.photo_small;
        this.imageView.setImage(paramTLObject, "50_50", this.avatarDrawable);
      }
    }
    for (paramTLObject = (TLObject)localObject;; paramTLObject = "")
    {
      setTag("windowBackgroundWhiteGrayText3");
      this.onlineTextView.setText(LocaleController.stringForMessageListDate(localTL_webAuthorization.date_active));
      this.onlineTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      localObject = new StringBuilder();
      if (localTL_webAuthorization.ip.length() != 0) {
        ((StringBuilder)localObject).append(localTL_webAuthorization.ip);
      }
      if (localTL_webAuthorization.region.length() != 0)
      {
        if (((StringBuilder)localObject).length() != 0) {
          ((StringBuilder)localObject).append(" ");
        }
        ((StringBuilder)localObject).append("— ");
        ((StringBuilder)localObject).append(localTL_webAuthorization.region);
      }
      this.detailExTextView.setText((CharSequence)localObject);
      localObject = new StringBuilder();
      if (!TextUtils.isEmpty(paramTLObject)) {
        ((StringBuilder)localObject).append(paramTLObject);
      }
      if (localTL_webAuthorization.browser.length() != 0)
      {
        if (((StringBuilder)localObject).length() != 0) {
          ((StringBuilder)localObject).append(", ");
        }
        ((StringBuilder)localObject).append(localTL_webAuthorization.browser);
      }
      if (localTL_webAuthorization.platform.length() != 0)
      {
        if (((StringBuilder)localObject).length() != 0) {
          ((StringBuilder)localObject).append(", ");
        }
        ((StringBuilder)localObject).append(localTL_webAuthorization.platform);
      }
      this.detailTextView.setText((CharSequence)localObject);
      return;
      paramTLObject = null;
      break;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/SessionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */