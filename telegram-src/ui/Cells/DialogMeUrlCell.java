package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.RecentMeUrl;
import org.telegram.tgnet.TLRPC.StickerSet;
import org.telegram.tgnet.TLRPC.StickerSetCovered;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlChat;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlChatInvite;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlStickerSet;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlUnknown;
import org.telegram.tgnet.TLRPC.TL_recentMeUrlUser;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;

public class DialogMeUrlCell
  extends BaseCell
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private ImageReceiver avatarImage = new ImageReceiver(this);
  private int avatarTop = AndroidUtilities.dp(10.0F);
  private int currentAccount = UserConfig.selectedAccount;
  private boolean drawNameBot;
  private boolean drawNameBroadcast;
  private boolean drawNameGroup;
  private boolean drawNameLock;
  private boolean drawVerified;
  private boolean isSelected;
  private StaticLayout messageLayout;
  private int messageLeft;
  private int messageTop = AndroidUtilities.dp(40.0F);
  private StaticLayout nameLayout;
  private int nameLeft;
  private int nameLockLeft;
  private int nameLockTop;
  private int nameMuteLeft;
  private TLRPC.RecentMeUrl recentMeUrl;
  public boolean useSeparator;
  
  public DialogMeUrlCell(Context paramContext)
  {
    super(paramContext);
    Theme.createDialogsResources(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(26.0F));
  }
  
  public void buildLayout()
  {
    Object localObject1 = "";
    TextPaint localTextPaint2 = Theme.dialogs_namePaint;
    TextPaint localTextPaint1 = Theme.dialogs_messagePaint;
    this.drawNameGroup = false;
    this.drawNameBroadcast = false;
    this.drawNameLock = false;
    this.drawNameBot = false;
    this.drawVerified = false;
    Object localObject3;
    if ((this.recentMeUrl instanceof TLRPC.TL_recentMeUrlChat))
    {
      localObject3 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.recentMeUrl.chat_id));
      if ((((TLRPC.Chat)localObject3).id < 0) || ((ChatObject.isChannel((TLRPC.Chat)localObject3)) && (!((TLRPC.Chat)localObject3).megagroup)))
      {
        this.drawNameBroadcast = true;
        this.nameLockTop = AndroidUtilities.dp(16.5F);
      }
    }
    for (;;)
    {
      this.drawVerified = ((TLRPC.Chat)localObject3).verified;
      int j;
      int i;
      label161:
      Object localObject2;
      label195:
      label204:
      label305:
      label329:
      int k;
      if (!LocaleController.isRTL)
      {
        this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        j = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4);
        if (this.drawNameGroup)
        {
          i = Theme.dialogs_groupDrawable.getIntrinsicWidth();
          this.nameLeft = (i + j);
          localObject1 = ((TLRPC.Chat)localObject3).title;
          if (((TLRPC.Chat)localObject3).photo == null) {
            break label855;
          }
          localObject2 = ((TLRPC.Chat)localObject3).photo.photo_small;
          this.avatarDrawable.setInfo((TLRPC.Chat)localObject3);
          localObject3 = MessagesController.getInstance(this.currentAccount).linkPrefix + "/" + this.recentMeUrl.url;
          this.avatarImage.setImage((TLObject)localObject2, "50_50", this.avatarDrawable, null, 0);
          localObject2 = localObject1;
          if (TextUtils.isEmpty((CharSequence)localObject1)) {
            localObject2 = LocaleController.getString("HiddenName", 2131493648);
          }
          if (LocaleController.isRTL) {
            break label1681;
          }
          j = getMeasuredWidth() - this.nameLeft - AndroidUtilities.dp(14.0F);
          if (!this.drawNameLock) {
            break label1703;
          }
          i = j - (AndroidUtilities.dp(4.0F) + Theme.dialogs_lockDrawable.getIntrinsicWidth());
          j = i;
          if (this.drawVerified)
          {
            k = AndroidUtilities.dp(6.0F) + Theme.dialogs_verifiedDrawable.getIntrinsicWidth();
            i -= k;
            j = i;
            if (LocaleController.isRTL)
            {
              this.nameLeft += k;
              j = i;
            }
          }
          j = Math.max(AndroidUtilities.dp(12.0F), j);
        }
      }
      try
      {
        this.nameLayout = new StaticLayout(TextUtils.ellipsize(((String)localObject2).replace('\n', ' '), localTextPaint2, j - AndroidUtilities.dp(12.0F), TextUtils.TruncateAt.END), localTextPaint2, j, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        k = getMeasuredWidth();
        int m = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 16);
        if (!LocaleController.isRTL)
        {
          this.messageLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
          if (AndroidUtilities.isTablet())
          {
            f = 13.0F;
            i = AndroidUtilities.dp(f);
            this.avatarImage.setImageCoords(i, this.avatarTop, AndroidUtilities.dp(52.0F), AndroidUtilities.dp(52.0F));
            i = Math.max(AndroidUtilities.dp(12.0F), k - m);
            localObject1 = TextUtils.ellipsize((CharSequence)localObject3, localTextPaint1, i - AndroidUtilities.dp(12.0F), TextUtils.TruncateAt.END);
          }
        }
      }
      catch (Exception localException1)
      {
        try
        {
          for (;;)
          {
            this.messageLayout = new StaticLayout((CharSequence)localObject1, localTextPaint1, i, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            if (!LocaleController.isRTL) {
              break label1860;
            }
            if ((this.nameLayout != null) && (this.nameLayout.getLineCount() > 0))
            {
              f = this.nameLayout.getLineLeft(0);
              d = Math.ceil(this.nameLayout.getLineWidth(0));
              if (this.drawVerified) {
                this.nameMuteLeft = ((int)(this.nameLeft + (j - d) - AndroidUtilities.dp(6.0F) - Theme.dialogs_verifiedDrawable.getIntrinsicWidth()));
              }
              if ((f == 0.0F) && (d < j)) {
                this.nameLeft = ((int)(this.nameLeft + (j - d)));
              }
            }
            if ((this.messageLayout != null) && (this.messageLayout.getLineCount() > 0) && (this.messageLayout.getLineLeft(0) == 0.0F))
            {
              d = Math.ceil(this.messageLayout.getLineWidth(0));
              if (d < i) {
                this.messageLeft = ((int)(this.messageLeft + (i - d)));
              }
            }
            return;
            this.drawNameGroup = true;
            this.nameLockTop = AndroidUtilities.dp(17.5F);
            break;
            i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth();
            break label161;
            j = getMeasuredWidth();
            k = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
            if (this.drawNameGroup) {}
            for (i = Theme.dialogs_groupDrawable.getIntrinsicWidth();; i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth())
            {
              this.nameLockLeft = (j - k - i);
              this.nameLeft = AndroidUtilities.dp(14.0F);
              break;
            }
            label855:
            localObject2 = null;
            break label195;
            if ((this.recentMeUrl instanceof TLRPC.TL_recentMeUrlUser))
            {
              localObject3 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.recentMeUrl.user_id));
              if (!LocaleController.isRTL)
              {
                this.nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
                label910:
                if (localObject3 != null)
                {
                  if (((TLRPC.User)localObject3).bot)
                  {
                    this.drawNameBot = true;
                    this.nameLockTop = AndroidUtilities.dp(16.5F);
                    if (LocaleController.isRTL) {
                      break label1032;
                    }
                    this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
                    this.nameLeft = (AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4) + Theme.dialogs_botDrawable.getIntrinsicWidth());
                  }
                  label974:
                  this.drawVerified = ((TLRPC.User)localObject3).verified;
                }
                localObject1 = UserObject.getUserName((TLRPC.User)localObject3);
                if (((TLRPC.User)localObject3).photo == null) {
                  break label1067;
                }
              }
              label1032:
              label1067:
              for (localObject2 = ((TLRPC.User)localObject3).photo.photo_small;; localObject2 = null)
              {
                this.avatarDrawable.setInfo((TLRPC.User)localObject3);
                break;
                this.nameLeft = AndroidUtilities.dp(14.0F);
                break label910;
                this.nameLockLeft = (getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline) - Theme.dialogs_botDrawable.getIntrinsicWidth());
                this.nameLeft = AndroidUtilities.dp(14.0F);
                break label974;
              }
            }
            if ((this.recentMeUrl instanceof TLRPC.TL_recentMeUrlStickerSet))
            {
              if (!LocaleController.isRTL) {}
              for (this.nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);; this.nameLeft = AndroidUtilities.dp(14.0F))
              {
                localObject1 = this.recentMeUrl.set.set.title;
                localObject2 = this.recentMeUrl.set.cover;
                this.avatarDrawable.setInfo(5, this.recentMeUrl.set.set.title, null, false);
                break;
              }
            }
            if ((this.recentMeUrl instanceof TLRPC.TL_recentMeUrlChatInvite))
            {
              if (!LocaleController.isRTL)
              {
                this.nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
                label1192:
                if (this.recentMeUrl.chat_invite.chat == null) {
                  break label1441;
                }
                this.avatarDrawable.setInfo(this.recentMeUrl.chat_invite.chat);
                localObject1 = this.recentMeUrl.chat_invite.chat.title;
                if (this.recentMeUrl.chat_invite.chat.photo == null) {
                  break label1417;
                }
                localObject2 = this.recentMeUrl.chat_invite.chat.photo.photo_small;
                label1271:
                if ((this.recentMeUrl.chat_invite.chat.id >= 0) && ((!ChatObject.isChannel(this.recentMeUrl.chat_invite.chat)) || (this.recentMeUrl.chat_invite.chat.megagroup))) {
                  break label1423;
                }
                this.drawNameBroadcast = true;
                this.nameLockTop = AndroidUtilities.dp(16.5F);
                label1333:
                this.drawVerified = this.recentMeUrl.chat_invite.chat.verified;
                label1350:
                if (LocaleController.isRTL) {
                  break label1560;
                }
                this.nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
                j = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4);
                if (!this.drawNameGroup) {
                  break label1549;
                }
              }
              label1417:
              label1423:
              label1441:
              label1549:
              for (i = Theme.dialogs_groupDrawable.getIntrinsicWidth();; i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth())
              {
                this.nameLeft = (i + j);
                break;
                this.nameLeft = AndroidUtilities.dp(14.0F);
                break label1192;
                localObject2 = null;
                break label1271;
                this.drawNameGroup = true;
                this.nameLockTop = AndroidUtilities.dp(17.5F);
                break label1333;
                localObject1 = this.recentMeUrl.chat_invite.title;
                localObject2 = this.recentMeUrl.chat_invite.photo.photo_small;
                this.avatarDrawable.setInfo(5, this.recentMeUrl.chat_invite.title, null, false);
                if ((this.recentMeUrl.chat_invite.broadcast) || (this.recentMeUrl.chat_invite.channel))
                {
                  this.drawNameBroadcast = true;
                  this.nameLockTop = AndroidUtilities.dp(16.5F);
                  break label1350;
                }
                this.drawNameGroup = true;
                this.nameLockTop = AndroidUtilities.dp(17.5F);
                break label1350;
              }
              label1560:
              j = getMeasuredWidth();
              k = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
              if (this.drawNameGroup) {}
              for (i = Theme.dialogs_groupDrawable.getIntrinsicWidth();; i = Theme.dialogs_broadcastDrawable.getIntrinsicWidth())
              {
                this.nameLockLeft = (j - k - i);
                this.nameLeft = AndroidUtilities.dp(14.0F);
                break;
              }
            }
            if ((this.recentMeUrl instanceof TLRPC.TL_recentMeUrlUnknown))
            {
              if (!LocaleController.isRTL) {}
              for (this.nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);; this.nameLeft = AndroidUtilities.dp(14.0F))
              {
                localObject1 = "Url";
                localObject2 = null;
                break;
              }
            }
            localObject2 = null;
            break label204;
            label1681:
            j = getMeasuredWidth() - this.nameLeft - AndroidUtilities.dp(AndroidUtilities.leftBaseline);
            break label305;
            label1703:
            if (this.drawNameGroup)
            {
              i = j - (AndroidUtilities.dp(4.0F) + Theme.dialogs_groupDrawable.getIntrinsicWidth());
              break label329;
            }
            if (this.drawNameBroadcast)
            {
              i = j - (AndroidUtilities.dp(4.0F) + Theme.dialogs_broadcastDrawable.getIntrinsicWidth());
              break label329;
            }
            i = j;
            if (!this.drawNameBot) {
              break label329;
            }
            i = j - (AndroidUtilities.dp(4.0F) + Theme.dialogs_botDrawable.getIntrinsicWidth());
            break label329;
            localException1 = localException1;
            FileLog.e(localException1);
            continue;
            f = 9.0F;
          }
          this.messageLeft = AndroidUtilities.dp(16.0F);
          i = getMeasuredWidth();
          if (AndroidUtilities.isTablet()) {}
          for (f = 65.0F;; f = 61.0F)
          {
            i -= AndroidUtilities.dp(f);
            break;
          }
        }
        catch (Exception localException2)
        {
          double d;
          label1860:
          do
          {
            do
            {
              float f;
              for (;;)
              {
                FileLog.e(localException2);
              }
              if ((this.nameLayout != null) && (this.nameLayout.getLineCount() > 0))
              {
                f = this.nameLayout.getLineRight(0);
                if (f == j)
                {
                  d = Math.ceil(this.nameLayout.getLineWidth(0));
                  if (d < j) {
                    this.nameLeft = ((int)(this.nameLeft - (j - d)));
                  }
                }
                if (this.drawVerified) {
                  this.nameMuteLeft = ((int)(this.nameLeft + f + AndroidUtilities.dp(6.0F)));
                }
              }
            } while ((this.messageLayout == null) || (this.messageLayout.getLineCount() <= 0) || (this.messageLayout.getLineRight(0) != i));
            d = Math.ceil(this.messageLayout.getLineWidth(0));
          } while (d >= i);
          this.messageLeft = ((int)(this.messageLeft - (i - d)));
        }
      }
    }
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
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
    if (this.isSelected) {
      paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight(), Theme.dialogs_tabletSeletedPaint);
    }
    if (this.drawNameLock)
    {
      setDrawableBounds(Theme.dialogs_lockDrawable, this.nameLockLeft, this.nameLockTop);
      Theme.dialogs_lockDrawable.draw(paramCanvas);
    }
    for (;;)
    {
      if (this.nameLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.nameLeft, AndroidUtilities.dp(13.0F));
        this.nameLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
      if (this.messageLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.messageLeft, this.messageTop);
      }
      try
      {
        this.messageLayout.draw(paramCanvas);
        paramCanvas.restore();
        if (this.drawVerified)
        {
          setDrawableBounds(Theme.dialogs_verifiedDrawable, this.nameMuteLeft, AndroidUtilities.dp(16.5F));
          setDrawableBounds(Theme.dialogs_verifiedCheckDrawable, this.nameMuteLeft, AndroidUtilities.dp(16.5F));
          Theme.dialogs_verifiedDrawable.draw(paramCanvas);
          Theme.dialogs_verifiedCheckDrawable.draw(paramCanvas);
        }
        if (this.useSeparator)
        {
          if (LocaleController.isRTL) {
            paramCanvas.drawLine(0.0F, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, Theme.dividerPaint);
          }
        }
        else
        {
          this.avatarImage.draw(paramCanvas);
          return;
          if (this.drawNameGroup)
          {
            setDrawableBounds(Theme.dialogs_groupDrawable, this.nameLockLeft, this.nameLockTop);
            Theme.dialogs_groupDrawable.draw(paramCanvas);
            continue;
          }
          if (this.drawNameBroadcast)
          {
            setDrawableBounds(Theme.dialogs_broadcastDrawable, this.nameLockLeft, this.nameLockTop);
            Theme.dialogs_broadcastDrawable.draw(paramCanvas);
            continue;
          }
          if (!this.drawNameBot) {
            continue;
          }
          setDrawableBounds(Theme.dialogs_botDrawable, this.nameLockLeft, this.nameLockTop);
          Theme.dialogs_botDrawable.draw(paramCanvas);
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          paramCanvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
      }
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (paramBoolean) {
      buildLayout();
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.getSize(paramInt1);
    int i = AndroidUtilities.dp(72.0F);
    if (this.useSeparator) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      setMeasuredDimension(paramInt2, paramInt1 + i);
      return;
    }
  }
  
  public void setDialogSelected(boolean paramBoolean)
  {
    if (this.isSelected != paramBoolean) {
      invalidate();
    }
    this.isSelected = paramBoolean;
  }
  
  public void setRecentMeUrl(TLRPC.RecentMeUrl paramRecentMeUrl)
  {
    this.recentMeUrl = paramRecentMeUrl;
    requestLayout();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/DialogMeUrlCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */