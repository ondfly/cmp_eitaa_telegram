package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.PhotoViewer;

public class ChatActionCell
  extends BaseCell
{
  private AvatarDrawable avatarDrawable;
  private int currentAccount = UserConfig.selectedAccount;
  private MessageObject currentMessageObject;
  private int customDate;
  private CharSequence customText;
  private ChatActionCellDelegate delegate;
  private boolean hasReplyMessage;
  private boolean imagePressed = false;
  private ImageReceiver imageReceiver = new ImageReceiver(this);
  private URLSpan pressedLink;
  private int previousWidth = 0;
  private int textHeight = 0;
  private StaticLayout textLayout;
  private int textWidth = 0;
  private int textX = 0;
  private int textXLeft = 0;
  private int textY = 0;
  
  public ChatActionCell(Context paramContext)
  {
    super(paramContext);
    this.imageReceiver.setRoundRadius(AndroidUtilities.dp(32.0F));
    this.avatarDrawable = new AvatarDrawable();
  }
  
  private void createLayout(CharSequence paramCharSequence, int paramInt)
  {
    int j = paramInt - AndroidUtilities.dp(30.0F);
    this.textLayout = new StaticLayout(paramCharSequence, Theme.chat_actionTextPaint, j, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
    this.textHeight = 0;
    this.textWidth = 0;
    try
    {
      int k = this.textLayout.getLineCount();
      int i = 0;
      for (;;)
      {
        if (i < k) {
          try
          {
            float f2 = this.textLayout.getLineWidth(i);
            float f1 = f2;
            if (f2 > j) {
              f1 = j;
            }
            this.textHeight = ((int)Math.max(this.textHeight, Math.ceil(this.textLayout.getLineBottom(i))));
            this.textWidth = ((int)Math.max(this.textWidth, Math.ceil(f1)));
            i += 1;
          }
          catch (Exception paramCharSequence)
          {
            FileLog.e(paramCharSequence);
            return;
          }
        }
      }
      return;
    }
    catch (Exception paramCharSequence)
    {
      FileLog.e(paramCharSequence);
      this.textX = ((paramInt - this.textWidth) / 2);
      this.textY = AndroidUtilities.dp(7.0F);
      this.textXLeft = ((paramInt - this.textLayout.getWidth()) / 2);
    }
  }
  
  private int findMaxWidthAroundLine(int paramInt)
  {
    int i = (int)Math.ceil(this.textLayout.getLineWidth(paramInt));
    int k = this.textLayout.getLineCount();
    int j = paramInt + 1;
    while (j < k)
    {
      int m = (int)Math.ceil(this.textLayout.getLineWidth(j));
      if (Math.abs(m - i) >= AndroidUtilities.dp(10.0F)) {
        break;
      }
      i = Math.max(m, i);
      j += 1;
    }
    paramInt -= 1;
    while (paramInt >= 0)
    {
      j = (int)Math.ceil(this.textLayout.getLineWidth(paramInt));
      if (Math.abs(j - i) >= AndroidUtilities.dp(10.0F)) {
        break;
      }
      i = Math.max(j, i);
      paramInt -= 1;
    }
    return i;
  }
  
  private boolean isLineBottom(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    return (paramInt3 == paramInt4 - 1) || ((paramInt3 >= 0) && (paramInt3 <= paramInt4 - 1) && (findMaxWidthAroundLine(paramInt3 + 1) + paramInt5 * 3 < paramInt1));
  }
  
  private boolean isLineTop(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
  {
    return (paramInt3 == 0) || ((paramInt3 >= 0) && (paramInt3 < paramInt4) && (findMaxWidthAroundLine(paramInt3 - 1) + paramInt5 * 3 < paramInt1));
  }
  
  public int getCustomDate()
  {
    return this.customDate;
  }
  
  public MessageObject getMessageObject()
  {
    return this.currentMessageObject;
  }
  
  public ImageReceiver getPhotoImage()
  {
    return this.imageReceiver;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.currentMessageObject != null) && (this.currentMessageObject.type == 11)) {
      this.imageReceiver.draw(paramCanvas);
    }
    if (this.textLayout != null)
    {
      int i11 = this.textLayout.getLineCount();
      int i12 = AndroidUtilities.dp(11.0F);
      int i13 = AndroidUtilities.dp(6.0F);
      int i14 = i12 - i13;
      int i15 = AndroidUtilities.dp(8.0F);
      int k = AndroidUtilities.dp(7.0F);
      int i4 = 0;
      int i3 = 0;
      if (i3 < i11)
      {
        int i = findMaxWidthAroundLine(i3);
        int i16 = (getMeasuredWidth() - i - i14) / 2;
        int i17 = i + i14;
        i = this.textLayout.getLineBottom(i3);
        int i2 = i - i4;
        int n = 0;
        i4 = i;
        int j;
        label151:
        label158:
        int m;
        int i1;
        int i9;
        int i6;
        int i8;
        int i5;
        label286:
        int i7;
        if (i3 == i11 - 1)
        {
          j = 1;
          if (i3 != 0) {
            break label904;
          }
          i = 1;
          m = i2;
          i1 = k;
          if (i != 0)
          {
            i1 = k - AndroidUtilities.dp(3.0F);
            m = i2 + AndroidUtilities.dp(3.0F);
          }
          i2 = m;
          if (j != 0) {
            i2 = m + AndroidUtilities.dp(3.0F);
          }
          m = 0;
          i9 = 0;
          i6 = 0;
          int i10 = 0;
          i8 = i6;
          i5 = j;
          k = m;
          if (j == 0)
          {
            i8 = i6;
            i5 = j;
            k = m;
            if (i3 + 1 < i11)
            {
              i8 = findMaxWidthAroundLine(i3 + 1) + i14;
              if (i14 * 2 + i8 >= i17) {
                break label909;
              }
              k = 1;
              i5 = 1;
            }
          }
          m = i9;
          i6 = i;
          i7 = i10;
          if (i == 0)
          {
            m = i9;
            i6 = i;
            i7 = i10;
            if (i3 > 0)
            {
              i7 = findMaxWidthAroundLine(i3 - 1) + i14;
              if (i14 * 2 + i7 >= i17) {
                break label939;
              }
              m = 1;
              i6 = 1;
            }
          }
          label348:
          if (k != 0)
          {
            if (k != 1) {
              break label1044;
            }
            i = (getMeasuredWidth() - i8) / 2;
            n = AndroidUtilities.dp(3.0F);
            if (!isLineBottom(i8, i17, i3 + 1, i11, i14)) {
              break label969;
            }
            paramCanvas.drawRect(i16 + i13, i1 + i2, i - i14, i1 + i2 + AndroidUtilities.dp(3.0F), Theme.chat_actionBackgroundPaint);
            paramCanvas.drawRect(i + i8 + i14, i1 + i2, i16 + i17 - i13, i1 + i2 + AndroidUtilities.dp(3.0F), Theme.chat_actionBackgroundPaint);
          }
          label473:
          i = i2;
          j = i1;
          if (m != 0)
          {
            if (m != 1) {
              break label1340;
            }
            i8 = (getMeasuredWidth() - i7) / 2;
            j = i1 - AndroidUtilities.dp(3.0F);
            i = i2 + AndroidUtilities.dp(3.0F);
            if (!isLineTop(i7, i17, i3 - 1, i11, i14)) {
              break label1279;
            }
            paramCanvas.drawRect(i16 + i13, j, i8 - i14, AndroidUtilities.dp(3.0F) + j, Theme.chat_actionBackgroundPaint);
            paramCanvas.drawRect(i8 + i7 + i14, j, i16 + i17 - i13, AndroidUtilities.dp(3.0F) + j, Theme.chat_actionBackgroundPaint);
          }
          label602:
          if ((i6 == 0) && (i5 == 0)) {
            break label1607;
          }
          paramCanvas.drawRect(i16 + i13, i1, i16 + i17 - i13, i1 + i2, Theme.chat_actionBackgroundPaint);
          label643:
          i1 = i16 - i14;
          i2 = i16 + i17 - i13;
          if ((i6 == 0) || (i5 != 0) || (k == 2)) {
            break label1635;
          }
          paramCanvas.drawRect(i1, j + i12, i1 + i12, j + i + n - AndroidUtilities.dp(6.0F), Theme.chat_actionBackgroundPaint);
          paramCanvas.drawRect(i2, j + i12, i2 + i12, j + i + n - AndroidUtilities.dp(6.0F), Theme.chat_actionBackgroundPaint);
        }
        for (;;)
        {
          if (i6 != 0)
          {
            Theme.chat_cornerOuter[0].setBounds(i1, j, i1 + i12, j + i12);
            Theme.chat_cornerOuter[0].draw(paramCanvas);
            Theme.chat_cornerOuter[1].setBounds(i2, j, i2 + i12, j + i12);
            Theme.chat_cornerOuter[1].draw(paramCanvas);
          }
          if (i5 != 0)
          {
            k = j + i + n - i12;
            Theme.chat_cornerOuter[2].setBounds(i2, k, i2 + i12, k + i12);
            Theme.chat_cornerOuter[2].draw(paramCanvas);
            Theme.chat_cornerOuter[3].setBounds(i1, k, i1 + i12, k + i12);
            Theme.chat_cornerOuter[3].draw(paramCanvas);
          }
          k = j + i;
          i3 += 1;
          break;
          j = 0;
          break label151;
          label904:
          i = 0;
          break label158;
          label909:
          if (i14 * 2 + i17 < i8)
          {
            k = 2;
            i5 = j;
            break label286;
          }
          k = 3;
          i5 = j;
          break label286;
          label939:
          if (i14 * 2 + i17 < i7)
          {
            m = 2;
            i6 = i;
            break label348;
          }
          m = 3;
          i6 = i;
          break label348;
          label969:
          paramCanvas.drawRect(i16 + i13, i1 + i2, i, i1 + i2 + AndroidUtilities.dp(3.0F), Theme.chat_actionBackgroundPaint);
          paramCanvas.drawRect(i + i8, i1 + i2, i16 + i17 - i13, i1 + i2 + AndroidUtilities.dp(3.0F), Theme.chat_actionBackgroundPaint);
          break label473;
          label1044:
          if (k == 2)
          {
            n = AndroidUtilities.dp(3.0F);
            i8 = i1 + i2 - AndroidUtilities.dp(11.0F);
            j = i16 - i15;
            i = j;
            if (m != 2)
            {
              i = j;
              if (m != 3) {
                i = j - i14;
              }
            }
            if ((i6 != 0) || (i5 != 0)) {
              paramCanvas.drawRect(i + i15, AndroidUtilities.dp(3.0F) + i8, i + i15 + i12, i8 + i12, Theme.chat_actionBackgroundPaint);
            }
            Theme.chat_cornerInner[2].setBounds(i, i8, i + i15, i8 + i15);
            Theme.chat_cornerInner[2].draw(paramCanvas);
            j = i16 + i17;
            i = j;
            if (m != 2)
            {
              i = j;
              if (m != 3) {
                i = j + i14;
              }
            }
            if ((i6 != 0) || (i5 != 0)) {
              paramCanvas.drawRect(i - i12, AndroidUtilities.dp(3.0F) + i8, i, i8 + i12, Theme.chat_actionBackgroundPaint);
            }
            Theme.chat_cornerInner[3].setBounds(i, i8, i + i15, i8 + i15);
            Theme.chat_cornerInner[3].draw(paramCanvas);
            break label473;
          }
          n = AndroidUtilities.dp(6.0F);
          break label473;
          label1279:
          paramCanvas.drawRect(i16 + i13, j, i8, AndroidUtilities.dp(3.0F) + j, Theme.chat_actionBackgroundPaint);
          paramCanvas.drawRect(i8 + i7, j, i16 + i17 - i13, AndroidUtilities.dp(3.0F) + j, Theme.chat_actionBackgroundPaint);
          break label602;
          label1340:
          if (m == 2)
          {
            j = i1 - AndroidUtilities.dp(3.0F);
            i7 = i2 + AndroidUtilities.dp(3.0F);
            i9 = j + AndroidUtilities.dp(6.2F);
            i8 = i16 - i15;
            i = i8;
            if (k != 2)
            {
              i = i8;
              if (k != 3) {
                i = i8 - i14;
              }
            }
            if ((i6 != 0) || (i5 != 0)) {
              paramCanvas.drawRect(i + i15, AndroidUtilities.dp(3.0F) + j, i + i15 + i12, AndroidUtilities.dp(11.0F) + j, Theme.chat_actionBackgroundPaint);
            }
            Theme.chat_cornerInner[0].setBounds(i, i9, i + i15, i9 + i15);
            Theme.chat_cornerInner[0].draw(paramCanvas);
            i8 = i16 + i17;
            i = i8;
            if (k != 2)
            {
              i = i8;
              if (k != 3) {
                i = i8 + i14;
              }
            }
            if ((i6 != 0) || (i5 != 0)) {
              paramCanvas.drawRect(i - i12, AndroidUtilities.dp(3.0F) + j, i, AndroidUtilities.dp(11.0F) + j, Theme.chat_actionBackgroundPaint);
            }
            Theme.chat_cornerInner[1].setBounds(i, i9, i + i15, i9 + i15);
            Theme.chat_cornerInner[1].draw(paramCanvas);
            i = i7;
            break label602;
          }
          j = i1 - AndroidUtilities.dp(6.0F);
          i = i2 + AndroidUtilities.dp(6.0F);
          break label602;
          label1607:
          paramCanvas.drawRect(i16, i1, i16 + i17, i1 + i2, Theme.chat_actionBackgroundPaint);
          break label643;
          label1635:
          if ((i5 != 0) && (i6 == 0) && (m != 2))
          {
            paramCanvas.drawRect(i1, j + i12 - AndroidUtilities.dp(5.0F), i1 + i12, j + i + n - i12, Theme.chat_actionBackgroundPaint);
            paramCanvas.drawRect(i2, j + i12 - AndroidUtilities.dp(5.0F), i2 + i12, j + i + n - i12, Theme.chat_actionBackgroundPaint);
          }
          else if ((i6 != 0) || (i5 != 0))
          {
            paramCanvas.drawRect(i1, j + i12, i1 + i12, j + i + n - i12, Theme.chat_actionBackgroundPaint);
            paramCanvas.drawRect(i2, j + i12, i2 + i12, j + i + n - i12, Theme.chat_actionBackgroundPaint);
          }
        }
      }
      paramCanvas.save();
      paramCanvas.translate(this.textXLeft, this.textY);
      this.textLayout.draw(paramCanvas);
      paramCanvas.restore();
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {}
  
  protected void onLongPress()
  {
    if (this.delegate != null) {
      this.delegate.didLongPressed(this);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    if ((this.currentMessageObject == null) && (this.customText == null))
    {
      setMeasuredDimension(View.MeasureSpec.getSize(paramInt1), this.textHeight + AndroidUtilities.dp(14.0F));
      return;
    }
    paramInt2 = Math.max(AndroidUtilities.dp(30.0F), View.MeasureSpec.getSize(paramInt1));
    Object localObject;
    if (paramInt2 != this.previousWidth)
    {
      if (this.currentMessageObject == null) {
        break label301;
      }
      if ((this.currentMessageObject.messageOwner == null) || (this.currentMessageObject.messageOwner.media == null) || (this.currentMessageObject.messageOwner.media.ttl_seconds == 0)) {
        break label289;
      }
      if (!(this.currentMessageObject.messageOwner.media.photo instanceof TLRPC.TL_photoEmpty)) {
        break label244;
      }
      localObject = LocaleController.getString("AttachPhotoExpired", 2131493038);
      this.previousWidth = paramInt2;
      createLayout((CharSequence)localObject, paramInt2);
      if ((this.currentMessageObject != null) && (this.currentMessageObject.type == 11)) {
        this.imageReceiver.setImageCoords((paramInt2 - AndroidUtilities.dp(64.0F)) / 2, this.textHeight + AndroidUtilities.dp(15.0F), AndroidUtilities.dp(64.0F), AndroidUtilities.dp(64.0F));
      }
    }
    int i = this.textHeight;
    if ((this.currentMessageObject != null) && (this.currentMessageObject.type == 11)) {}
    for (paramInt1 = 70;; paramInt1 = 0)
    {
      setMeasuredDimension(paramInt2, AndroidUtilities.dp(paramInt1 + 14) + i);
      return;
      label244:
      if ((this.currentMessageObject.messageOwner.media.document instanceof TLRPC.TL_documentEmpty))
      {
        localObject = LocaleController.getString("AttachVideoExpired", 2131493044);
        break;
      }
      localObject = this.currentMessageObject.messageText;
      break;
      label289:
      localObject = this.currentMessageObject.messageText;
      break;
      label301:
      localObject = this.customText;
      break;
    }
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    boolean bool2;
    if (this.currentMessageObject == null)
    {
      bool2 = super.onTouchEvent(paramMotionEvent);
      return bool2;
    }
    float f3 = paramMotionEvent.getX();
    float f1 = paramMotionEvent.getY();
    boolean bool1 = false;
    boolean bool3 = false;
    label110:
    Object localObject;
    if (paramMotionEvent.getAction() == 0)
    {
      bool2 = bool1;
      if (this.delegate != null)
      {
        bool1 = bool3;
        if (this.currentMessageObject.type == 11)
        {
          bool1 = bool3;
          if (this.imageReceiver.isInsideImage(f3, f1))
          {
            this.imagePressed = true;
            bool1 = true;
          }
        }
        bool2 = bool1;
        if (bool1)
        {
          startCheckLongPress();
          bool2 = bool1;
        }
      }
      bool1 = bool2;
      if (!bool2) {
        if (paramMotionEvent.getAction() != 0)
        {
          bool1 = bool2;
          if (this.pressedLink != null)
          {
            bool1 = bool2;
            if (paramMotionEvent.getAction() != 1) {}
          }
        }
        else
        {
          if ((f3 < this.textX) || (f1 < this.textY) || (f3 > this.textX + this.textWidth) || (f1 > this.textY + this.textHeight)) {
            break label618;
          }
          float f2 = this.textY;
          f3 -= this.textXLeft;
          int i = this.textLayout.getLineForVertical((int)(f1 - f2));
          int j = this.textLayout.getOffsetForHorizontal(i, f3);
          f1 = this.textLayout.getLineLeft(i);
          if ((f1 > f3) || (this.textLayout.getLineWidth(i) + f1 < f3) || (!(this.currentMessageObject.messageText instanceof Spannable))) {
            break label606;
          }
          localObject = (URLSpan[])((Spannable)this.currentMessageObject.messageText).getSpans(j, j, URLSpan.class);
          if (localObject.length == 0) {
            break label594;
          }
          if (paramMotionEvent.getAction() != 0) {
            break label486;
          }
          this.pressedLink = localObject[0];
          bool1 = true;
        }
      }
    }
    for (;;)
    {
      bool2 = bool1;
      if (bool1) {
        break;
      }
      return super.onTouchEvent(paramMotionEvent);
      if (paramMotionEvent.getAction() != 2) {
        cancelCheckLongPress();
      }
      bool2 = bool1;
      if (!this.imagePressed) {
        break label110;
      }
      if (paramMotionEvent.getAction() == 1)
      {
        this.imagePressed = false;
        bool2 = bool1;
        if (this.delegate == null) {
          break label110;
        }
        this.delegate.didClickedImage(this);
        playSoundEffect(0);
        bool2 = bool1;
        break label110;
      }
      if (paramMotionEvent.getAction() == 3)
      {
        this.imagePressed = false;
        bool2 = bool1;
        break label110;
      }
      bool2 = bool1;
      if (paramMotionEvent.getAction() != 2) {
        break label110;
      }
      bool2 = bool1;
      if (this.imageReceiver.isInsideImage(f3, f1)) {
        break label110;
      }
      this.imagePressed = false;
      bool2 = bool1;
      break label110;
      label486:
      bool1 = bool2;
      if (localObject[0] == this.pressedLink)
      {
        if (this.delegate != null)
        {
          localObject = localObject[0].getURL();
          if (!((String)localObject).startsWith("game")) {
            break label554;
          }
          this.delegate.didPressedReplyMessage(this, this.currentMessageObject.messageOwner.reply_to_msg_id);
        }
        for (;;)
        {
          bool1 = true;
          break;
          label554:
          if (((String)localObject).startsWith("http")) {
            Browser.openUrl(getContext(), (String)localObject);
          } else {
            this.delegate.needOpenUserProfile(Integer.parseInt((String)localObject));
          }
        }
        label594:
        this.pressedLink = null;
        bool1 = bool2;
        continue;
        label606:
        this.pressedLink = null;
        bool1 = bool2;
        continue;
        label618:
        this.pressedLink = null;
        bool1 = bool2;
      }
    }
  }
  
  public void setCustomDate(int paramInt)
  {
    if (this.customDate == paramInt) {}
    String str;
    do
    {
      return;
      str = LocaleController.formatDateChat(paramInt);
    } while ((this.customText != null) && (TextUtils.equals(str, this.customText)));
    this.previousWidth = 0;
    this.customDate = paramInt;
    this.customText = str;
    if (getMeasuredWidth() != 0)
    {
      createLayout(this.customText, getMeasuredWidth());
      invalidate();
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ChatActionCell.this.requestLayout();
      }
    });
  }
  
  public void setDelegate(ChatActionCellDelegate paramChatActionCellDelegate)
  {
    this.delegate = paramChatActionCellDelegate;
  }
  
  public void setMessageObject(MessageObject paramMessageObject)
  {
    boolean bool2 = true;
    if ((this.currentMessageObject == paramMessageObject) && ((this.hasReplyMessage) || (paramMessageObject.replyMessageObject == null))) {
      return;
    }
    this.currentMessageObject = paramMessageObject;
    boolean bool1;
    int i;
    if (paramMessageObject.replyMessageObject != null)
    {
      bool1 = true;
      this.hasReplyMessage = bool1;
      this.previousWidth = 0;
      if (this.currentMessageObject.type != 11) {
        break label325;
      }
      i = 0;
      if (paramMessageObject.messageOwner.to_id != null)
      {
        if (paramMessageObject.messageOwner.to_id.chat_id == 0) {
          break label196;
        }
        i = paramMessageObject.messageOwner.to_id.chat_id;
      }
      label100:
      this.avatarDrawable.setInfo(i, null, null, false);
      if (!(this.currentMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto)) {
        break label261;
      }
      this.imageReceiver.setImage(this.currentMessageObject.messageOwner.action.newUserPhoto.photo_small, "50_50", this.avatarDrawable, null, 0);
      label159:
      paramMessageObject = this.imageReceiver;
      if (PhotoViewer.isShowingImage(this.currentMessageObject)) {
        break label319;
      }
      bool1 = bool2;
      label178:
      paramMessageObject.setVisible(bool1, false);
    }
    for (;;)
    {
      requestLayout();
      return;
      bool1 = false;
      break;
      label196:
      if (paramMessageObject.messageOwner.to_id.channel_id != 0)
      {
        i = paramMessageObject.messageOwner.to_id.channel_id;
        break label100;
      }
      int j = paramMessageObject.messageOwner.to_id.user_id;
      i = j;
      if (j != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
        break label100;
      }
      i = paramMessageObject.messageOwner.from_id;
      break label100;
      label261:
      paramMessageObject = FileLoader.getClosestPhotoSizeWithSize(this.currentMessageObject.photoThumbs, AndroidUtilities.dp(64.0F));
      if (paramMessageObject != null)
      {
        this.imageReceiver.setImage(paramMessageObject.location, "50_50", this.avatarDrawable, null, 0);
        break label159;
      }
      this.imageReceiver.setImageBitmap(this.avatarDrawable);
      break label159;
      label319:
      bool1 = false;
      break label178;
      label325:
      this.imageReceiver.setImageBitmap((Bitmap)null);
    }
  }
  
  public static abstract interface ChatActionCellDelegate
  {
    public abstract void didClickedImage(ChatActionCell paramChatActionCell);
    
    public abstract void didLongPressed(ChatActionCell paramChatActionCell);
    
    public abstract void didPressedBotButton(MessageObject paramMessageObject, TLRPC.KeyboardButton paramKeyboardButton);
    
    public abstract void didPressedReplyMessage(ChatActionCell paramChatActionCell, int paramInt);
    
    public abstract void needOpenUserProfile(int paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ChatActionCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */