package ir.eitaa.ui.Cells;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ContactsController;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.tgnet.TLObject;
import ir.eitaa.tgnet.TLRPC.FileLocation;
import ir.eitaa.tgnet.TLRPC.User;
import ir.eitaa.tgnet.TLRPC.UserProfilePhoto;
import ir.eitaa.ui.Components.AvatarDrawable;
import ir.eitaa.ui.Components.BackupImageView;
import ir.eitaa.ui.Components.LayoutHelper;

public class JoinSheetUserCell
  extends FrameLayout
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private BackupImageView imageView;
  private TextView nameTextView;
  private int[] result = new int[1];
  
  public JoinSheetUserCell(Context paramContext)
  {
    super(paramContext);
    this.imageView = new BackupImageView(paramContext);
    this.imageView.setRoundRadius(AndroidUtilities.dp(27.0F));
    addView(this.imageView, LayoutHelper.createFrame(54, 54.0F, 49, 0.0F, 7.0F, 0.0F, 0.0F));
    this.nameTextView = new TextView(paramContext);
    this.nameTextView.setTextColor(-14606047);
    this.nameTextView.setTextSize(1, 12.0F);
    this.nameTextView.setMaxLines(1);
    this.nameTextView.setGravity(49);
    this.nameTextView.setLines(1);
    this.nameTextView.setSingleLine(true);
    this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
    addView(this.nameTextView, LayoutHelper.createFrame(-1, -2.0F, 51, 6.0F, 64.0F, 6.0F, 0.0F));
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(90.0F), 1073741824));
  }
  
  public void setCount(int paramInt)
  {
    this.nameTextView.setText("");
    this.avatarDrawable.setInfo(0, null, null, false, "+" + LocaleController.formatShortNumber(paramInt, this.result));
    this.imageView.setImage((TLRPC.FileLocation)null, "50_50", this.avatarDrawable);
  }
  
  public void setUser(TLRPC.User paramUser)
  {
    this.nameTextView.setText(ContactsController.formatName(paramUser.first_name, paramUser.last_name));
    this.avatarDrawable.setInfo(paramUser);
    Object localObject2 = null;
    Object localObject1 = localObject2;
    if (paramUser != null)
    {
      localObject1 = localObject2;
      if (paramUser.photo != null) {
        localObject1 = paramUser.photo.photo_small;
      }
    }
    this.imageView.setImage((TLObject)localObject1, "50_50", this.avatarDrawable);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Cells/JoinSheetUserCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */