package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.Theme;

public class AvatarDrawable
  extends Drawable
{
  private int color;
  private boolean drawBrodcast;
  private boolean drawPhoto;
  private boolean isProfile;
  private TextPaint namePaint = new TextPaint(1);
  private int savedMessages;
  private StringBuilder stringBuilder = new StringBuilder(5);
  private float textHeight;
  private StaticLayout textLayout;
  private float textLeft;
  private float textWidth;
  
  public AvatarDrawable()
  {
    this.namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.namePaint.setTextSize(AndroidUtilities.dp(18.0F));
  }
  
  public AvatarDrawable(TLRPC.Chat paramChat)
  {
    this(paramChat, false);
  }
  
  public AvatarDrawable(TLRPC.Chat paramChat, boolean paramBoolean)
  {
    this();
    this.isProfile = paramBoolean;
    int i;
    String str;
    if (paramChat != null)
    {
      i = paramChat.id;
      str = paramChat.title;
      if (paramChat.id >= 0) {
        break label44;
      }
    }
    label44:
    for (paramBoolean = true;; paramBoolean = false)
    {
      setInfo(i, str, null, paramBoolean, null);
      return;
    }
  }
  
  public AvatarDrawable(TLRPC.User paramUser)
  {
    this(paramUser, false);
  }
  
  public AvatarDrawable(TLRPC.User paramUser, boolean paramBoolean)
  {
    this();
    this.isProfile = paramBoolean;
    if (paramUser != null) {
      setInfo(paramUser.id, paramUser.first_name, paramUser.last_name, false, null);
    }
  }
  
  public static int getButtonColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_actionBarSelector[getColorIndex(paramInt)]);
  }
  
  public static int getColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_background[getColorIndex(paramInt)]);
  }
  
  public static int getColorIndex(int paramInt)
  {
    if ((paramInt >= 0) && (paramInt < 7)) {
      return paramInt;
    }
    return Math.abs(paramInt % Theme.keys_avatar_background.length);
  }
  
  public static int getIconColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_actionBarIcon[getColorIndex(paramInt)]);
  }
  
  public static int getNameColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_nameInMessage[getColorIndex(paramInt)]);
  }
  
  public static int getProfileBackColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_backgroundActionBar[getColorIndex(paramInt)]);
  }
  
  public static int getProfileColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_backgroundInProfile[getColorIndex(paramInt)]);
  }
  
  public static int getProfileTextColorForId(int paramInt)
  {
    return Theme.getColor(Theme.keys_avatar_subtitleInProfile[getColorIndex(paramInt)]);
  }
  
  public void draw(Canvas paramCanvas)
  {
    Rect localRect = getBounds();
    if (localRect == null) {
      return;
    }
    int n = localRect.width();
    this.namePaint.setColor(Theme.getColor("avatar_text"));
    Theme.avatar_backgroundPaint.setColor(this.color);
    paramCanvas.save();
    paramCanvas.translate(localRect.left, localRect.top);
    paramCanvas.drawCircle(n / 2.0F, n / 2.0F, n / 2.0F, Theme.avatar_backgroundPaint);
    int j;
    int i;
    if ((this.savedMessages != 0) && (Theme.avatar_savedDrawable != null))
    {
      int m = Theme.avatar_savedDrawable.getIntrinsicWidth();
      int k = Theme.avatar_savedDrawable.getIntrinsicHeight();
      j = k;
      i = m;
      if (this.savedMessages == 2)
      {
        i = (int)(m * 0.8F);
        j = (int)(k * 0.8F);
      }
      k = (n - i) / 2;
      m = (n - j) / 2;
      Theme.avatar_savedDrawable.setBounds(k, m, k + i, m + j);
      Theme.avatar_savedDrawable.draw(paramCanvas);
    }
    for (;;)
    {
      paramCanvas.restore();
      return;
      if ((this.drawBrodcast) && (Theme.avatar_broadcastDrawable != null))
      {
        i = (n - Theme.avatar_broadcastDrawable.getIntrinsicWidth()) / 2;
        j = (n - Theme.avatar_broadcastDrawable.getIntrinsicHeight()) / 2;
        Theme.avatar_broadcastDrawable.setBounds(i, j, Theme.avatar_broadcastDrawable.getIntrinsicWidth() + i, Theme.avatar_broadcastDrawable.getIntrinsicHeight() + j);
        Theme.avatar_broadcastDrawable.draw(paramCanvas);
      }
      else if (this.textLayout != null)
      {
        paramCanvas.translate((n - this.textWidth) / 2.0F - this.textLeft, (n - this.textHeight) / 2.0F);
        this.textLayout.draw(paramCanvas);
      }
      else if ((this.drawPhoto) && (Theme.avatar_photoDrawable != null))
      {
        i = (n - Theme.avatar_photoDrawable.getIntrinsicWidth()) / 2;
        j = (n - Theme.avatar_photoDrawable.getIntrinsicHeight()) / 2;
        Theme.avatar_photoDrawable.setBounds(i, j, Theme.avatar_photoDrawable.getIntrinsicWidth() + i, Theme.avatar_photoDrawable.getIntrinsicHeight() + j);
        Theme.avatar_photoDrawable.draw(paramCanvas);
      }
    }
  }
  
  public int getColor()
  {
    return this.color;
  }
  
  public int getIntrinsicHeight()
  {
    return 0;
  }
  
  public int getIntrinsicWidth()
  {
    return 0;
  }
  
  public int getOpacity()
  {
    return -2;
  }
  
  public void setAlpha(int paramInt) {}
  
  public void setColor(int paramInt)
  {
    this.color = paramInt;
  }
  
  public void setColorFilter(ColorFilter paramColorFilter) {}
  
  public void setDrawPhoto(boolean paramBoolean)
  {
    this.drawPhoto = paramBoolean;
  }
  
  public void setInfo(int paramInt, String paramString1, String paramString2, boolean paramBoolean)
  {
    setInfo(paramInt, paramString1, paramString2, paramBoolean, null);
  }
  
  public void setInfo(int paramInt, String paramString1, String paramString2, boolean paramBoolean, String paramString3)
  {
    String str2;
    String str1;
    if (this.isProfile)
    {
      this.color = getProfileColorForId(paramInt);
      this.drawBrodcast = paramBoolean;
      this.savedMessages = 0;
      if (paramString1 != null)
      {
        str2 = paramString1;
        str1 = paramString2;
        if (paramString1.length() != 0) {}
      }
      else
      {
        str1 = null;
        str2 = paramString2;
      }
      this.stringBuilder.setLength(0);
      if (paramString3 == null) {
        break label180;
      }
      this.stringBuilder.append(paramString3);
    }
    label180:
    label403:
    for (;;)
    {
      if (this.stringBuilder.length() <= 0) {
        break label411;
      }
      paramString1 = this.stringBuilder.toString().toUpperCase();
      try
      {
        this.textLayout = new StaticLayout(paramString1, this.namePaint, AndroidUtilities.dp(100.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        if (this.textLayout.getLineCount() > 0)
        {
          this.textLeft = this.textLayout.getLineLeft(0);
          this.textWidth = this.textLayout.getLineWidth(0);
          this.textHeight = this.textLayout.getLineBottom(0);
        }
        return;
      }
      catch (Exception paramString1)
      {
        FileLog.e(paramString1);
        return;
      }
      this.color = getColorForId(paramInt);
      break;
      if ((str2 != null) && (str2.length() > 0)) {
        this.stringBuilder.appendCodePoint(str2.codePointAt(0));
      }
      if ((str1 != null) && (str1.length() > 0))
      {
        paramString1 = null;
        paramInt = str1.length() - 1;
        for (;;)
        {
          if ((paramInt < 0) || ((paramString1 != null) && (str1.charAt(paramInt) == ' ')))
          {
            if (Build.VERSION.SDK_INT >= 17) {
              this.stringBuilder.append("‌");
            }
            this.stringBuilder.appendCodePoint(paramString1.intValue());
            break;
          }
          paramString1 = Integer.valueOf(str1.codePointAt(paramInt));
          paramInt -= 1;
        }
      }
      if ((str2 != null) && (str2.length() > 0))
      {
        paramInt = str2.length() - 1;
        for (;;)
        {
          if (paramInt < 0) {
            break label403;
          }
          if ((str2.charAt(paramInt) == ' ') && (paramInt != str2.length() - 1) && (str2.charAt(paramInt + 1) != ' '))
          {
            if (Build.VERSION.SDK_INT >= 17) {
              this.stringBuilder.append("‌");
            }
            this.stringBuilder.appendCodePoint(str2.codePointAt(paramInt + 1));
            break;
          }
          paramInt -= 1;
        }
      }
    }
    label411:
    this.textLayout = null;
  }
  
  public void setInfo(TLRPC.Chat paramChat)
  {
    int i;
    String str;
    if (paramChat != null)
    {
      i = paramChat.id;
      str = paramChat.title;
      if (paramChat.id >= 0) {
        break label35;
      }
    }
    label35:
    for (boolean bool = true;; bool = false)
    {
      setInfo(i, str, null, bool, null);
      return;
    }
  }
  
  public void setInfo(TLRPC.User paramUser)
  {
    if (paramUser != null) {
      setInfo(paramUser.id, paramUser.first_name, paramUser.last_name, false, null);
    }
  }
  
  public void setProfile(boolean paramBoolean)
  {
    this.isProfile = paramBoolean;
  }
  
  public void setSavedMessages(int paramInt)
  {
    this.savedMessages = paramInt;
    this.color = Theme.getColor("avatar_backgroundSaved");
  }
  
  public void setTextSize(int paramInt)
  {
    this.namePaint.setTextSize(paramInt);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/AvatarDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */