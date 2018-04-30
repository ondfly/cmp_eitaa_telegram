package org.telegram.ui.Cells;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.GeoPoint;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LocationActivity.LiveLocation;

public class SharingLiveLocationCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable;
  private BackupImageView avatarImageView;
  private int currentAccount;
  private LocationController.SharingLocationInfo currentInfo;
  private SimpleTextView distanceTextView;
  private Runnable invalidateRunnable = new Runnable()
  {
    public void run()
    {
      SharingLiveLocationCell.this.invalidate((int)SharingLiveLocationCell.this.rect.left - 5, (int)SharingLiveLocationCell.this.rect.top - 5, (int)SharingLiveLocationCell.this.rect.right + 5, (int)SharingLiveLocationCell.this.rect.bottom + 5);
      AndroidUtilities.runOnUIThread(SharingLiveLocationCell.this.invalidateRunnable, 1000L);
    }
  };
  private LocationActivity.LiveLocation liveLocation;
  private Location location = new Location("network");
  private SimpleTextView nameTextView;
  private RectF rect = new RectF();
  
  public SharingLiveLocationCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    this.avatarImageView = new BackupImageView(paramContext);
    this.avatarImageView.setRoundRadius(AndroidUtilities.dp(20.0F));
    this.avatarDrawable = new AvatarDrawable();
    this.nameTextView = new SimpleTextView(paramContext);
    this.nameTextView.setTextSize(16);
    this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
    this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    Object localObject = this.nameTextView;
    int j;
    label165:
    float f1;
    if (LocaleController.isRTL)
    {
      j = 5;
      ((SimpleTextView)localObject).setGravity(j);
      if (!paramBoolean) {
        break label443;
      }
      localObject = this.avatarImageView;
      if (!LocaleController.isRTL) {
        break label381;
      }
      j = 5;
      if (!LocaleController.isRTL) {
        break label387;
      }
      f1 = 0.0F;
      label173:
      if (!LocaleController.isRTL) {
        break label393;
      }
      f2 = 17.0F;
      label183:
      addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, j | 0x30, f1, 13.0F, f2, 0.0F));
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label399;
      }
      j = 5;
      label222:
      if (!LocaleController.isRTL) {
        break label405;
      }
      f1 = 54.0F;
      label231:
      if (!LocaleController.isRTL) {
        break label411;
      }
      f2 = 73.0F;
      label241:
      addView((View)localObject, LayoutHelper.createFrame(-1, 20.0F, j | 0x30, f1, 12.0F, f2, 0.0F));
      this.distanceTextView = new SimpleTextView(paramContext);
      this.distanceTextView.setTextSize(14);
      this.distanceTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      paramContext = this.distanceTextView;
      if (!LocaleController.isRTL) {
        break label418;
      }
      j = 5;
      label311:
      paramContext.setGravity(j);
      paramContext = this.distanceTextView;
      if (!LocaleController.isRTL) {
        break label424;
      }
      label328:
      if (!LocaleController.isRTL) {
        break label430;
      }
      f1 = 54.0F;
      label337:
      if (!LocaleController.isRTL) {
        break label436;
      }
    }
    label381:
    label387:
    label393:
    label399:
    label405:
    label411:
    label418:
    label424:
    label430:
    label436:
    for (float f2 = 73.0F;; f2 = 54.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(-1, 20.0F, i | 0x30, f1, 37.0F, f2, 0.0F));
      setWillNotDraw(false);
      return;
      j = 3;
      break;
      j = 3;
      break label165;
      f1 = 17.0F;
      break label173;
      f2 = 0.0F;
      break label183;
      j = 3;
      break label222;
      f1 = 73.0F;
      break label231;
      f2 = 54.0F;
      break label241;
      j = 3;
      break label311;
      i = 3;
      break label328;
      f1 = 73.0F;
      break label337;
    }
    label443:
    paramContext = this.avatarImageView;
    if (LocaleController.isRTL)
    {
      j = 5;
      label457:
      if (!LocaleController.isRTL) {
        break label560;
      }
      f1 = 0.0F;
      label465:
      if (!LocaleController.isRTL) {
        break label566;
      }
      f2 = 17.0F;
      label475:
      addView(paramContext, LayoutHelper.createFrame(40, 40.0F, j | 0x30, f1, 7.0F, f2, 0.0F));
      paramContext = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label572;
      }
      label509:
      if (!LocaleController.isRTL) {
        break label578;
      }
      f1 = 54.0F;
      label518:
      if (!LocaleController.isRTL) {
        break label584;
      }
    }
    label560:
    label566:
    label572:
    label578:
    label584:
    for (f2 = 74.0F;; f2 = 54.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 17.0F, f2, 0.0F));
      break;
      j = 3;
      break label457;
      f1 = 17.0F;
      break label465;
      f2 = 0.0F;
      break label475;
      i = 3;
      break label509;
      f1 = 74.0F;
      break label518;
    }
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    AndroidUtilities.runOnUIThread(this.invalidateRunnable);
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    AndroidUtilities.cancelRunOnUIThread(this.invalidateRunnable);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.currentInfo == null) && (this.liveLocation == null)) {}
    int i;
    int j;
    int k;
    do
    {
      return;
      if (this.currentInfo == null) {
        break;
      }
      i = this.currentInfo.stopTime;
      j = this.currentInfo.period;
      k = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    } while (i < k);
    float f2 = Math.abs(i - k) / j;
    Object localObject;
    float f3;
    label103:
    float f4;
    float f5;
    if (LocaleController.isRTL)
    {
      localObject = this.rect;
      f3 = AndroidUtilities.dp(13.0F);
      if (this.distanceTextView != null)
      {
        f1 = 18.0F;
        f4 = AndroidUtilities.dp(f1);
        f5 = AndroidUtilities.dp(43.0F);
        if (this.distanceTextView == null) {
          break label303;
        }
        f1 = 48.0F;
        label128:
        ((RectF)localObject).set(f3, f4, f5, AndroidUtilities.dp(f1));
        if (this.distanceTextView != null) {
          break label401;
        }
        j = Theme.getColor("location_liveLocationProgress");
        label158:
        Theme.chat_radialProgress2Paint.setColor(j);
        Theme.chat_livePaint.setColor(j);
        paramCanvas.drawArc(this.rect, -90.0F, -360.0F * f2, false, Theme.chat_radialProgress2Paint);
        localObject = LocaleController.formatLocationLeftTime(i - k);
        f1 = Theme.chat_livePaint.measureText((String)localObject);
        f2 = this.rect.centerX();
        f3 = f1 / 2.0F;
        if (this.distanceTextView == null) {
          break label411;
        }
      }
    }
    label303:
    label339:
    label394:
    label401:
    label411:
    for (float f1 = 37.0F;; f1 = 31.0F)
    {
      paramCanvas.drawText((String)localObject, f2 - f3, AndroidUtilities.dp(f1), Theme.chat_livePaint);
      return;
      i = this.liveLocation.object.date + this.liveLocation.object.media.period;
      j = this.liveLocation.object.media.period;
      break;
      f1 = 12.0F;
      break label103;
      f1 = 42.0F;
      break label128;
      localObject = this.rect;
      f3 = getMeasuredWidth() - AndroidUtilities.dp(43.0F);
      if (this.distanceTextView != null)
      {
        f1 = 18.0F;
        f4 = AndroidUtilities.dp(f1);
        f5 = getMeasuredWidth() - AndroidUtilities.dp(13.0F);
        if (this.distanceTextView == null) {
          break label394;
        }
      }
      for (f1 = 48.0F;; f1 = 42.0F)
      {
        ((RectF)localObject).set(f3, f4, f5, AndroidUtilities.dp(f1));
        break;
        f1 = 12.0F;
        break label339;
      }
      j = Theme.getColor("location_liveLocationProgress");
      break label158;
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt1 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    if (this.distanceTextView != null) {}
    for (float f = 66.0F;; f = 54.0F)
    {
      super.onMeasure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(f), 1073741824));
      return;
    }
  }
  
  public void setDialog(LocationController.SharingLocationInfo paramSharingLocationInfo)
  {
    this.currentInfo = paramSharingLocationInfo;
    int i = (int)paramSharingLocationInfo.did;
    Object localObject1 = null;
    Object localObject2;
    if (i > 0)
    {
      localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
      paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
      if (localObject2 != null)
      {
        this.avatarDrawable.setInfo((TLRPC.User)localObject2);
        this.nameTextView.setText(ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name));
        paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
        if (((TLRPC.User)localObject2).photo != null)
        {
          paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
          if (((TLRPC.User)localObject2).photo.photo_small != null) {
            paramSharingLocationInfo = ((TLRPC.User)localObject2).photo.photo_small;
          }
        }
      }
    }
    for (;;)
    {
      this.avatarImageView.setImage(paramSharingLocationInfo, null, this.avatarDrawable);
      return;
      localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
      if (localObject2 != null)
      {
        this.avatarDrawable.setInfo((TLRPC.Chat)localObject2);
        this.nameTextView.setText(((TLRPC.Chat)localObject2).title);
        paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
        if (((TLRPC.Chat)localObject2).photo != null)
        {
          paramSharingLocationInfo = (LocationController.SharingLocationInfo)localObject1;
          if (((TLRPC.Chat)localObject2).photo.photo_small != null) {
            paramSharingLocationInfo = ((TLRPC.Chat)localObject2).photo.photo_small;
          }
        }
      }
    }
  }
  
  public void setDialog(MessageObject paramMessageObject, Location paramLocation)
  {
    int i = paramMessageObject.messageOwner.from_id;
    if (paramMessageObject.isForwarded()) {
      if (paramMessageObject.messageOwner.fwd_from.channel_id == 0) {
        break label341;
      }
    }
    String str2;
    Object localObject;
    TLRPC.User localUser;
    TLRPC.Chat localChat;
    float f;
    label341:
    for (i = -paramMessageObject.messageOwner.fwd_from.channel_id;; i = paramMessageObject.messageOwner.fwd_from.from_id)
    {
      this.currentAccount = paramMessageObject.currentAccount;
      str2 = null;
      localObject = null;
      localUser = null;
      localChat = null;
      if (!TextUtils.isEmpty(paramMessageObject.messageOwner.media.address)) {
        str2 = paramMessageObject.messageOwner.media.address;
      }
      if (TextUtils.isEmpty(paramMessageObject.messageOwner.media.title)) {
        break;
      }
      str1 = paramMessageObject.messageOwner.media.title;
      localObject = getResources().getDrawable(2131165597);
      ((Drawable)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("location_sendLocationIcon"), PorterDuff.Mode.MULTIPLY));
      i = Theme.getColor("location_placeLocationBackground");
      localObject = new CombinedDrawable(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(40.0F), i, i), (Drawable)localObject);
      ((CombinedDrawable)localObject).setCustomSize(AndroidUtilities.dp(40.0F), AndroidUtilities.dp(40.0F));
      ((CombinedDrawable)localObject).setIconSize(AndroidUtilities.dp(24.0F), AndroidUtilities.dp(24.0F));
      this.avatarImageView.setImageDrawable((Drawable)localObject);
      this.nameTextView.setText(str1);
      this.location.setLatitude(paramMessageObject.messageOwner.media.geo.lat);
      this.location.setLongitude(paramMessageObject.messageOwner.media.geo._long);
      if (paramLocation == null) {
        break label653;
      }
      f = this.location.distanceTo(paramLocation);
      if (str2 == null) {
        break label566;
      }
      if (f >= 1000.0F) {
        break label520;
      }
      this.distanceTextView.setText(String.format("%s - %d %s", new Object[] { str2, Integer.valueOf((int)f), LocaleController.getString("MetersAway", 2131493829) }));
      return;
    }
    String str1 = "";
    this.avatarDrawable = null;
    if (i > 0)
    {
      localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
      if (localUser != null)
      {
        localObject = localChat;
        if (localUser.photo != null) {
          localObject = localUser.photo.photo_small;
        }
        this.avatarDrawable = new AvatarDrawable(localUser);
        str1 = UserObject.getUserName(localUser);
      }
    }
    for (;;)
    {
      this.avatarImageView.setImage((TLObject)localObject, null, this.avatarDrawable);
      break;
      localChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      if (localChat != null)
      {
        localObject = localUser;
        if (localChat.photo != null) {
          localObject = localChat.photo.photo_small;
        }
        this.avatarDrawable = new AvatarDrawable(localChat);
        str1 = localChat.title;
      }
    }
    label520:
    this.distanceTextView.setText(String.format("%s - %.2f %s", new Object[] { str2, Float.valueOf(f / 1000.0F), LocaleController.getString("KMetersAway", 2131493714) }));
    return;
    label566:
    if (f < 1000.0F)
    {
      this.distanceTextView.setText(String.format("%d %s", new Object[] { Integer.valueOf((int)f), LocaleController.getString("MetersAway", 2131493829) }));
      return;
    }
    this.distanceTextView.setText(String.format("%.2f %s", new Object[] { Float.valueOf(f / 1000.0F), LocaleController.getString("KMetersAway", 2131493714) }));
    return;
    label653:
    if (str2 != null)
    {
      this.distanceTextView.setText(str2);
      return;
    }
    this.distanceTextView.setText(LocaleController.getString("Loading", 2131493762));
  }
  
  public void setDialog(LocationActivity.LiveLocation paramLiveLocation, Location paramLocation)
  {
    this.liveLocation = paramLiveLocation;
    int i = paramLiveLocation.id;
    LatLng localLatLng = null;
    Object localObject2;
    Object localObject1;
    long l;
    label164:
    float f;
    if (i > 0)
    {
      localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
      this.avatarDrawable.setInfo((TLRPC.User)localObject2);
      localObject1 = localLatLng;
      if (localObject2 != null)
      {
        this.nameTextView.setText(ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name));
        localObject1 = localLatLng;
        if (((TLRPC.User)localObject2).photo != null)
        {
          localObject1 = localLatLng;
          if (((TLRPC.User)localObject2).photo.photo_small != null) {
            localObject1 = ((TLRPC.User)localObject2).photo.photo_small;
          }
        }
      }
      localLatLng = paramLiveLocation.marker.getPosition();
      this.location.setLatitude(localLatLng.latitude);
      this.location.setLongitude(localLatLng.longitude);
      if (paramLiveLocation.object.edit_date == 0) {
        break label335;
      }
      l = paramLiveLocation.object.edit_date;
      paramLiveLocation = LocaleController.formatLocationUpdateDate(l);
      if (paramLocation == null) {
        break label395;
      }
      f = this.location.distanceTo(paramLocation);
      if (f >= 1000.0F) {
        break label348;
      }
      this.distanceTextView.setText(String.format("%s - %d %s", new Object[] { paramLiveLocation, Integer.valueOf((int)f), LocaleController.getString("MetersAway", 2131493829) }));
    }
    for (;;)
    {
      this.avatarImageView.setImage((TLObject)localObject1, null, this.avatarDrawable);
      return;
      localObject2 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-i));
      localObject1 = localLatLng;
      if (localObject2 == null) {
        break;
      }
      this.avatarDrawable.setInfo((TLRPC.Chat)localObject2);
      this.nameTextView.setText(((TLRPC.Chat)localObject2).title);
      localObject1 = localLatLng;
      if (((TLRPC.Chat)localObject2).photo == null) {
        break;
      }
      localObject1 = localLatLng;
      if (((TLRPC.Chat)localObject2).photo.photo_small == null) {
        break;
      }
      localObject1 = ((TLRPC.Chat)localObject2).photo.photo_small;
      break;
      label335:
      l = paramLiveLocation.object.date;
      break label164;
      label348:
      this.distanceTextView.setText(String.format("%s - %.2f %s", new Object[] { paramLiveLocation, Float.valueOf(f / 1000.0F), LocaleController.getString("KMetersAway", 2131493714) }));
      continue;
      label395:
      this.distanceTextView.setText(paramLiveLocation);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/SharingLiveLocationCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */