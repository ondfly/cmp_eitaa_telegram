package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.LongSparseArray;
import android.view.View.MeasureSpec;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;

public class ProfileSearchCell
  extends BaseCell
{
  private AvatarDrawable avatarDrawable;
  private ImageReceiver avatarImage = new ImageReceiver(this);
  private TLRPC.Chat chat;
  private StaticLayout countLayout;
  private int countLeft;
  private int countTop = AndroidUtilities.dp(25.0F);
  private int countWidth;
  private int currentAccount = UserConfig.selectedAccount;
  private CharSequence currentName;
  private long dialog_id;
  private boolean drawCheck;
  private boolean drawCount;
  private boolean drawNameBot;
  private boolean drawNameBroadcast;
  private boolean drawNameGroup;
  private boolean drawNameLock;
  private TLRPC.EncryptedChat encryptedChat;
  private TLRPC.FileLocation lastAvatar;
  private String lastName;
  private int lastStatus;
  private int lastUnreadCount;
  private StaticLayout nameLayout;
  private int nameLeft;
  private int nameLockLeft;
  private int nameLockTop;
  private int nameTop;
  private int nameWidth;
  private StaticLayout onlineLayout;
  private int onlineLeft;
  private int paddingRight;
  private RectF rect = new RectF();
  private boolean savedMessages;
  private CharSequence subLabel;
  public boolean useSeparator;
  private TLRPC.User user;
  
  public ProfileSearchCell(Context paramContext)
  {
    super(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(26.0F));
    this.avatarDrawable = new AvatarDrawable();
  }
  
  public void buildLayout()
  {
    this.drawNameBroadcast = false;
    this.drawNameLock = false;
    this.drawNameGroup = false;
    this.drawCheck = false;
    this.drawNameBot = false;
    label98:
    Object localObject2;
    Object localObject1;
    label188:
    label200:
    int i;
    label229:
    label257:
    int j;
    Object localObject3;
    label453:
    label528:
    label555:
    label576:
    float f;
    label592:
    double d;
    if (this.encryptedChat != null)
    {
      this.drawNameLock = true;
      this.dialog_id = (this.encryptedChat.id << 32);
      if (!LocaleController.isRTL)
      {
        this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        this.nameLeft = (AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4) + Theme.dialogs_lockDrawable.getIntrinsicWidth());
        this.nameLockTop = AndroidUtilities.dp(16.5F);
        if (this.currentName == null) {
          break label1268;
        }
        localObject2 = this.currentName;
        localObject1 = localObject2;
        if (((CharSequence)localObject2).length() == 0)
        {
          if ((this.user == null) || (this.user.phone == null) || (this.user.phone.length() == 0)) {
            break label1322;
          }
          localObject1 = PhoneFormat.getInstance().format("+" + this.user.phone);
        }
        if (this.encryptedChat == null) {
          break label1336;
        }
        localObject2 = Theme.dialogs_nameEncryptedPaint;
        if (LocaleController.isRTL) {
          break label1344;
        }
        i = getMeasuredWidth() - this.nameLeft - AndroidUtilities.dp(14.0F);
        this.nameWidth = i;
        if (!this.drawNameLock) {
          break label1372;
        }
        this.nameWidth -= AndroidUtilities.dp(6.0F) + Theme.dialogs_lockDrawable.getIntrinsicWidth();
        this.nameWidth -= this.paddingRight;
        j = i - this.paddingRight;
        if (!this.drawCount) {
          break label1502;
        }
        localObject3 = (TLRPC.TL_dialog)MessagesController.getInstance(this.currentAccount).dialogs_dict.get(this.dialog_id);
        if ((localObject3 == null) || (((TLRPC.TL_dialog)localObject3).unread_count == 0)) {
          break label1489;
        }
        this.lastUnreadCount = ((TLRPC.TL_dialog)localObject3).unread_count;
        localObject3 = String.format("%d", new Object[] { Integer.valueOf(((TLRPC.TL_dialog)localObject3).unread_count) });
        this.countWidth = Math.max(AndroidUtilities.dp(12.0F), (int)Math.ceil(Theme.dialogs_countTextPaint.measureText((String)localObject3)));
        this.countLayout = new StaticLayout((CharSequence)localObject3, Theme.dialogs_countTextPaint, this.countWidth, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
        i = this.countWidth + AndroidUtilities.dp(18.0F);
        this.nameWidth -= i;
        if (LocaleController.isRTL) {
          break label1465;
        }
        this.countLeft = (getMeasuredWidth() - this.countWidth - AndroidUtilities.dp(19.0F));
        this.nameLayout = new StaticLayout(TextUtils.ellipsize((CharSequence)localObject1, (TextPaint)localObject2, this.nameWidth - AndroidUtilities.dp(12.0F), TextUtils.TruncateAt.END), (TextPaint)localObject2, this.nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        if ((this.chat != null) && (this.subLabel == null)) {
          break label1808;
        }
        if (LocaleController.isRTL) {
          break label1515;
        }
        this.onlineLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        localObject1 = "";
        localObject3 = Theme.dialogs_offlinePaint;
        if (this.subLabel == null) {
          break label1528;
        }
        localObject1 = this.subLabel;
        localObject2 = localObject3;
        if (!this.savedMessages) {
          break label1727;
        }
        this.onlineLayout = null;
        this.nameTop = AndroidUtilities.dp(25.0F);
        if (LocaleController.isRTL) {
          break label1832;
        }
        if (!AndroidUtilities.isTablet()) {
          break label1825;
        }
        f = 13.0F;
        i = AndroidUtilities.dp(f);
        this.avatarImage.setImageCoords(i, AndroidUtilities.dp(10.0F), AndroidUtilities.dp(52.0F), AndroidUtilities.dp(52.0F));
        if (!LocaleController.isRTL) {
          break label1867;
        }
        if ((this.nameLayout.getLineCount() > 0) && (this.nameLayout.getLineLeft(0) == 0.0F))
        {
          d = Math.ceil(this.nameLayout.getLineWidth(0));
          if (d < this.nameWidth) {
            this.nameLeft = ((int)(this.nameLeft + (this.nameWidth - d)));
          }
        }
        if ((this.onlineLayout != null) && (this.onlineLayout.getLineCount() > 0) && (this.onlineLayout.getLineLeft(0) == 0.0F))
        {
          d = Math.ceil(this.onlineLayout.getLineWidth(0));
          if (d < j) {
            this.onlineLeft = ((int)(this.onlineLeft + (j - d)));
          }
        }
      }
    }
    for (;;)
    {
      if (LocaleController.isRTL)
      {
        this.nameLeft += this.paddingRight;
        this.onlineLeft += this.paddingRight;
      }
      return;
      this.nameLockLeft = (getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline + 2) - Theme.dialogs_lockDrawable.getIntrinsicWidth());
      this.nameLeft = AndroidUtilities.dp(11.0F);
      break;
      if (this.chat != null)
      {
        if (this.chat.id < 0)
        {
          this.dialog_id = AndroidUtilities.makeBroadcastId(this.chat.id);
          this.drawNameBroadcast = true;
          this.nameLockTop = AndroidUtilities.dp(28.5F);
          label879:
          this.drawCheck = this.chat.verified;
          if (LocaleController.isRTL) {
            break label1025;
          }
          this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
          j = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4);
          if (!this.drawNameGroup) {
            break label1014;
          }
        }
        label1014:
        for (i = Theme.dialogs_groupDrawable.getIntrinsicWidth();; i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth())
        {
          this.nameLeft = (i + j);
          break;
          this.dialog_id = (-this.chat.id);
          if ((ChatObject.isChannel(this.chat)) && (!this.chat.megagroup))
          {
            this.drawNameBroadcast = true;
            this.nameLockTop = AndroidUtilities.dp(28.5F);
            break label879;
          }
          this.drawNameGroup = true;
          this.nameLockTop = AndroidUtilities.dp(30.0F);
          break label879;
        }
        label1025:
        j = getMeasuredWidth();
        int k = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 2);
        if (this.drawNameGroup) {}
        for (i = Theme.dialogs_groupDrawable.getIntrinsicWidth();; i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth())
        {
          this.nameLockLeft = (j - k - i);
          this.nameLeft = AndroidUtilities.dp(11.0F);
          break;
        }
      }
      if (this.user == null) {
        break label98;
      }
      this.dialog_id = this.user.id;
      if (!LocaleController.isRTL)
      {
        this.nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        label1129:
        if (!this.user.bot) {
          break label1255;
        }
        this.drawNameBot = true;
        if (LocaleController.isRTL) {
          break label1217;
        }
        this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        this.nameLeft = (AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4) + Theme.dialogs_botDrawable.getIntrinsicWidth());
      }
      label1181:
      label1217:
      label1255:
      for (this.nameLockTop = AndroidUtilities.dp(16.5F);; this.nameLockTop = AndroidUtilities.dp(17.0F))
      {
        this.drawCheck = this.user.verified;
        break;
        this.nameLeft = AndroidUtilities.dp(11.0F);
        break label1129;
        this.nameLockLeft = (getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline + 2) - Theme.dialogs_botDrawable.getIntrinsicWidth());
        this.nameLeft = AndroidUtilities.dp(11.0F);
        break label1181;
      }
      label1268:
      localObject1 = "";
      if (this.chat != null) {
        localObject1 = this.chat.title;
      }
      for (;;)
      {
        localObject2 = ((String)localObject1).replace('\n', ' ');
        break;
        if (this.user != null) {
          localObject1 = UserObject.getUserName(this.user);
        }
      }
      label1322:
      localObject1 = LocaleController.getString("HiddenName", 2131493648);
      break label188;
      label1336:
      localObject2 = Theme.dialogs_namePaint;
      break label200;
      label1344:
      i = getMeasuredWidth() - this.nameLeft - AndroidUtilities.dp(AndroidUtilities.leftBaseline);
      this.nameWidth = i;
      break label229;
      label1372:
      if (this.drawNameBroadcast)
      {
        this.nameWidth -= AndroidUtilities.dp(6.0F) + Theme.dialogs_broadcastDrawable.getIntrinsicWidth();
        break label257;
      }
      if (this.drawNameGroup)
      {
        this.nameWidth -= AndroidUtilities.dp(6.0F) + Theme.dialogs_groupDrawable.getIntrinsicWidth();
        break label257;
      }
      if (!this.drawNameBot) {
        break label257;
      }
      this.nameWidth -= AndroidUtilities.dp(6.0F) + Theme.dialogs_botDrawable.getIntrinsicWidth();
      break label257;
      label1465:
      this.countLeft = AndroidUtilities.dp(19.0F);
      this.nameLeft += i;
      break label453;
      label1489:
      this.lastUnreadCount = 0;
      this.countLayout = null;
      break label453;
      label1502:
      this.lastUnreadCount = 0;
      this.countLayout = null;
      break label453;
      label1515:
      this.onlineLeft = AndroidUtilities.dp(11.0F);
      break label528;
      label1528:
      localObject2 = localObject3;
      if (this.user == null) {
        break label555;
      }
      if (this.user.bot)
      {
        localObject1 = LocaleController.getString("Bot", 2131493086);
        localObject2 = localObject3;
        break label555;
      }
      if ((this.user.id == 333000) || (this.user.id == 777000))
      {
        localObject1 = LocaleController.getString("ServiceNotifications", 2131494365);
        localObject2 = localObject3;
        break label555;
      }
      String str = LocaleController.formatUserStatus(this.currentAccount, this.user);
      localObject2 = localObject3;
      localObject1 = str;
      if (this.user == null) {
        break label555;
      }
      if (this.user.id != UserConfig.getInstance(this.currentAccount).getClientUserId())
      {
        localObject2 = localObject3;
        localObject1 = str;
        if (this.user.status == null) {
          break label555;
        }
        localObject2 = localObject3;
        localObject1 = str;
        if (this.user.status.expires <= ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()) {
          break label555;
        }
      }
      localObject2 = Theme.dialogs_onlinePaint;
      localObject1 = LocaleController.getString("Online", 2131494030);
      break label555;
      label1727:
      this.onlineLayout = new StaticLayout(TextUtils.ellipsize((CharSequence)localObject1, (TextPaint)localObject2, j - AndroidUtilities.dp(12.0F), TextUtils.TruncateAt.END), (TextPaint)localObject2, j, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
      this.nameTop = AndroidUtilities.dp(13.0F);
      if ((this.subLabel == null) || (this.chat == null)) {
        break label576;
      }
      this.nameLockTop -= AndroidUtilities.dp(12.0F);
      break label576;
      label1808:
      this.onlineLayout = null;
      this.nameTop = AndroidUtilities.dp(25.0F);
      break label576;
      label1825:
      f = 9.0F;
      break label592;
      label1832:
      i = getMeasuredWidth();
      if (AndroidUtilities.isTablet()) {}
      for (f = 65.0F;; f = 61.0F)
      {
        i -= AndroidUtilities.dp(f);
        break;
      }
      label1867:
      if ((this.nameLayout.getLineCount() > 0) && (this.nameLayout.getLineRight(0) == this.nameWidth))
      {
        d = Math.ceil(this.nameLayout.getLineWidth(0));
        if (d < this.nameWidth) {
          this.nameLeft = ((int)(this.nameLeft - (this.nameWidth - d)));
        }
      }
      if ((this.onlineLayout != null) && (this.onlineLayout.getLineCount() > 0) && (this.onlineLayout.getLineRight(0) == j))
      {
        d = Math.ceil(this.onlineLayout.getLineWidth(0));
        if (d < j) {
          this.onlineLeft = ((int)(this.onlineLeft - (j - d)));
        }
      }
    }
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.avatarImage.onAttachedToWindow();
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.avatarImage.onDetachedFromWindow();
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.user == null) && (this.chat == null) && (this.encryptedChat == null)) {
      return;
    }
    label98:
    int i;
    label181:
    RectF localRectF;
    float f1;
    float f2;
    if (this.useSeparator)
    {
      if (LocaleController.isRTL) {
        paramCanvas.drawLine(0.0F, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, Theme.dividerPaint);
      }
    }
    else
    {
      if (!this.drawNameLock) {
        break label460;
      }
      setDrawableBounds(Theme.dialogs_lockDrawable, this.nameLockLeft, this.nameLockTop);
      Theme.dialogs_lockDrawable.draw(paramCanvas);
      if (this.nameLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.nameLeft, this.nameTop);
        this.nameLayout.draw(paramCanvas);
        paramCanvas.restore();
        if (this.drawCheck)
        {
          if (!LocaleController.isRTL) {
            break label599;
          }
          if (this.nameLayout.getLineLeft(0) != 0.0F) {
            break label553;
          }
          i = this.nameLeft - AndroidUtilities.dp(6.0F) - Theme.dialogs_verifiedDrawable.getIntrinsicWidth();
          setDrawableBounds(Theme.dialogs_verifiedDrawable, i, this.nameLockTop);
          setDrawableBounds(Theme.dialogs_verifiedCheckDrawable, i, this.nameLockTop);
          Theme.dialogs_verifiedDrawable.draw(paramCanvas);
          Theme.dialogs_verifiedCheckDrawable.draw(paramCanvas);
        }
      }
      if (this.onlineLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.onlineLeft, AndroidUtilities.dp(40.0F));
        this.onlineLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
      if (this.countLayout != null)
      {
        i = this.countLeft - AndroidUtilities.dp(5.5F);
        this.rect.set(i, this.countTop, this.countWidth + i + AndroidUtilities.dp(11.0F), this.countTop + AndroidUtilities.dp(23.0F));
        localRectF = this.rect;
        f1 = AndroidUtilities.density;
        f2 = AndroidUtilities.density;
        if (!MessagesController.getInstance(this.currentAccount).isDialogMuted(this.dialog_id)) {
          break label626;
        }
      }
    }
    label460:
    label553:
    label599:
    label626:
    for (Paint localPaint = Theme.dialogs_countGrayPaint;; localPaint = Theme.dialogs_countPaint)
    {
      paramCanvas.drawRoundRect(localRectF, 11.5F * f1, 11.5F * f2, localPaint);
      paramCanvas.save();
      paramCanvas.translate(this.countLeft, this.countTop + AndroidUtilities.dp(4.0F));
      this.countLayout.draw(paramCanvas);
      paramCanvas.restore();
      this.avatarImage.draw(paramCanvas);
      return;
      paramCanvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
      break;
      if (this.drawNameGroup)
      {
        setDrawableBounds(Theme.dialogs_groupDrawable, this.nameLockLeft, this.nameLockTop);
        Theme.dialogs_groupDrawable.draw(paramCanvas);
        break label98;
      }
      if (this.drawNameBroadcast)
      {
        setDrawableBounds(Theme.dialogs_broadcastDrawable, this.nameLockLeft, this.nameLockTop);
        Theme.dialogs_broadcastDrawable.draw(paramCanvas);
        break label98;
      }
      if (!this.drawNameBot) {
        break label98;
      }
      setDrawableBounds(Theme.dialogs_botDrawable, this.nameLockLeft, this.nameLockTop);
      Theme.dialogs_botDrawable.draw(paramCanvas);
      break label98;
      f1 = this.nameLayout.getLineWidth(0);
      i = (int)(this.nameLeft + this.nameWidth - Math.ceil(f1) - AndroidUtilities.dp(6.0F) - Theme.dialogs_verifiedDrawable.getIntrinsicWidth());
      break label181;
      i = (int)(this.nameLeft + this.nameLayout.getLineRight(0) + AndroidUtilities.dp(6.0F));
      break label181;
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((this.user == null) && (this.chat == null) && (this.encryptedChat == null)) {}
    while (!paramBoolean) {
      return;
    }
    buildLayout();
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), AndroidUtilities.dp(72.0F));
  }
  
  public void setData(TLObject paramTLObject, TLRPC.EncryptedChat paramEncryptedChat, CharSequence paramCharSequence1, CharSequence paramCharSequence2, boolean paramBoolean1, boolean paramBoolean2)
  {
    this.currentName = paramCharSequence1;
    if ((paramTLObject instanceof TLRPC.User))
    {
      this.user = ((TLRPC.User)paramTLObject);
      this.chat = null;
    }
    for (;;)
    {
      this.encryptedChat = paramEncryptedChat;
      this.subLabel = paramCharSequence2;
      this.drawCount = paramBoolean1;
      this.savedMessages = paramBoolean2;
      update(0);
      return;
      if ((paramTLObject instanceof TLRPC.Chat))
      {
        this.chat = ((TLRPC.Chat)paramTLObject);
        this.user = null;
      }
    }
  }
  
  public void setPaddingRight(int paramInt)
  {
    this.paddingRight = paramInt;
  }
  
  public void update(int paramInt)
  {
    Object localObject2 = null;
    Object localObject1 = null;
    int j;
    int i;
    if (this.user != null)
    {
      this.avatarDrawable.setInfo(this.user);
      if (this.savedMessages)
      {
        this.avatarDrawable.setSavedMessages(1);
        if (paramInt == 0) {
          break label479;
        }
        j = 0;
        if (((paramInt & 0x2) == 0) || (this.user == null))
        {
          i = j;
          if ((paramInt & 0x8) != 0)
          {
            i = j;
            if (this.chat == null) {}
          }
        }
        else
        {
          if ((this.lastAvatar == null) || (localObject1 != null))
          {
            i = j;
            if (this.lastAvatar != null) {
              break label155;
            }
            i = j;
            if (localObject1 == null) {
              break label155;
            }
            i = j;
            if (this.lastAvatar == null) {
              break label155;
            }
            i = j;
            if (localObject1 == null) {
              break label155;
            }
            if (this.lastAvatar.volume_id == ((TLRPC.FileLocation)localObject1).volume_id)
            {
              i = j;
              if (this.lastAvatar.local_id == ((TLRPC.FileLocation)localObject1).local_id) {
                break label155;
              }
            }
          }
          i = 1;
        }
        label155:
        j = i;
        if (i == 0)
        {
          j = i;
          if ((paramInt & 0x4) != 0)
          {
            j = i;
            if (this.user != null)
            {
              int k = 0;
              if (this.user.status != null) {
                k = this.user.status.expires;
              }
              j = i;
              if (k != this.lastStatus) {
                j = 1;
              }
            }
          }
        }
        if ((j != 0) || ((paramInt & 0x1) == 0) || (this.user == null))
        {
          i = j;
          if ((paramInt & 0x10) != 0)
          {
            i = j;
            if (this.chat == null) {}
          }
        }
        else
        {
          if (this.user == null) {
            break label467;
          }
        }
      }
    }
    label467:
    for (localObject2 = this.user.first_name + this.user.last_name;; localObject2 = this.chat.title)
    {
      i = j;
      if (!((String)localObject2).equals(this.lastName)) {
        i = 1;
      }
      j = i;
      if (i == 0)
      {
        j = i;
        if (this.drawCount)
        {
          j = i;
          if ((paramInt & 0x100) != 0)
          {
            localObject2 = (TLRPC.TL_dialog)MessagesController.getInstance(this.currentAccount).dialogs_dict.get(this.dialog_id);
            j = i;
            if (localObject2 != null)
            {
              j = i;
              if (((TLRPC.TL_dialog)localObject2).unread_count != this.lastUnreadCount) {
                j = 1;
              }
            }
          }
        }
      }
      if (j != 0) {
        break label479;
      }
      return;
      if (this.user.photo == null) {
        break;
      }
      localObject1 = this.user.photo.photo_small;
      break;
      if (this.chat != null)
      {
        localObject1 = localObject2;
        if (this.chat.photo != null) {
          localObject1 = this.chat.photo.photo_small;
        }
        this.avatarDrawable.setInfo(this.chat);
        break;
      }
      this.avatarDrawable.setInfo(0, null, null, false);
      break;
    }
    label479:
    if (this.user != null) {
      if (this.user.status != null)
      {
        this.lastStatus = this.user.status.expires;
        this.lastName = (this.user.first_name + this.user.last_name);
        label544:
        this.lastAvatar = ((TLRPC.FileLocation)localObject1);
        this.avatarImage.setImage((TLObject)localObject1, "50_50", this.avatarDrawable, null, 0);
        if ((getMeasuredWidth() == 0) && (getMeasuredHeight() == 0)) {
          break label620;
        }
        buildLayout();
      }
    }
    for (;;)
    {
      postInvalidate();
      return;
      this.lastStatus = 0;
      break;
      if (this.chat == null) {
        break label544;
      }
      this.lastName = this.chat.title;
      break label544;
      label620:
      requestLayout();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ProfileSearchCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */