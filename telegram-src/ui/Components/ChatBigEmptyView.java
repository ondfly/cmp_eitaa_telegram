package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;

public class ChatBigEmptyView
  extends LinearLayout
{
  private ArrayList<ImageView> imageViews = new ArrayList();
  private TextView secretViewStatusTextView;
  private ArrayList<TextView> textViews = new ArrayList();
  
  public ChatBigEmptyView(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    setBackgroundResource(2131165671);
    getBackground().setColorFilter(Theme.colorFilter);
    setPadding(AndroidUtilities.dp(16.0F), AndroidUtilities.dp(12.0F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(12.0F));
    setOrientation(1);
    Object localObject;
    label193:
    int i;
    label235:
    int j;
    label242:
    label267:
    label297:
    ImageView localImageView;
    label355:
    TextView localTextView;
    if (paramBoolean)
    {
      this.secretViewStatusTextView = new TextView(paramContext);
      this.secretViewStatusTextView.setTextSize(1, 15.0F);
      this.secretViewStatusTextView.setTextColor(Theme.getColor("chat_serviceText"));
      this.secretViewStatusTextView.setGravity(1);
      this.secretViewStatusTextView.setMaxWidth(AndroidUtilities.dp(210.0F));
      this.textViews.add(this.secretViewStatusTextView);
      addView(this.secretViewStatusTextView, LayoutHelper.createLinear(-2, -2, 49));
      localObject = new TextView(paramContext);
      if (!paramBoolean) {
        break label562;
      }
      ((TextView)localObject).setText(LocaleController.getString("EncryptedDescriptionTitle", 2131493435));
      ((TextView)localObject).setTextSize(1, 15.0F);
      ((TextView)localObject).setTextColor(Theme.getColor("chat_serviceText"));
      this.textViews.add(localObject);
      ((TextView)localObject).setMaxWidth(AndroidUtilities.dp(260.0F));
      if (!paramBoolean) {
        break label606;
      }
      if (!LocaleController.isRTL) {
        break label601;
      }
      i = 5;
      if (!paramBoolean) {
        break label611;
      }
      j = 0;
      addView((View)localObject, LayoutHelper.createLinear(-2, -2, i | 0x30, 0, 8, 0, j));
      i = 0;
      if (i >= 4) {
        return;
      }
      localObject = new LinearLayout(paramContext);
      ((LinearLayout)localObject).setOrientation(0);
      if (!LocaleController.isRTL) {
        break label618;
      }
      j = 5;
      addView((View)localObject, LayoutHelper.createLinear(-2, -2, j, 0, 8, 0, 0));
      localImageView = new ImageView(paramContext);
      localImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_serviceText"), PorterDuff.Mode.MULTIPLY));
      if (!paramBoolean) {
        break label624;
      }
      j = 2131165386;
      localImageView.setImageResource(j);
      this.imageViews.add(localImageView);
      localTextView = new TextView(paramContext);
      localTextView.setTextSize(1, 15.0F);
      localTextView.setTextColor(Theme.getColor("chat_serviceText"));
      this.textViews.add(localTextView);
      if (!LocaleController.isRTL) {
        break label631;
      }
      j = 5;
      label419:
      localTextView.setGravity(j | 0x10);
      localTextView.setMaxWidth(AndroidUtilities.dp(260.0F));
      switch (i)
      {
      default: 
        label472:
        if (!LocaleController.isRTL) {
          break label796;
        }
        ((LinearLayout)localObject).addView(localTextView, LayoutHelper.createLinear(-2, -2));
        if (paramBoolean) {
          ((LinearLayout)localObject).addView(localImageView, LayoutHelper.createLinear(-2, -2, 8.0F, 3.0F, 0.0F, 0.0F));
        }
        break;
      }
    }
    for (;;)
    {
      i += 1;
      break label267;
      localObject = new ImageView(paramContext);
      ((ImageView)localObject).setImageResource(2131165276);
      addView((View)localObject, LayoutHelper.createLinear(-2, -2, 49, 0, 2, 0, 0));
      break;
      label562:
      ((TextView)localObject).setText(LocaleController.getString("ChatYourSelfTitle", 2131493239));
      ((TextView)localObject).setTextSize(1, 16.0F);
      ((TextView)localObject).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      ((TextView)localObject).setGravity(1);
      break label193;
      label601:
      i = 3;
      break label235;
      label606:
      i = 1;
      break label235;
      label611:
      j = 8;
      break label242;
      label618:
      j = 3;
      break label297;
      label624:
      j = 2131165458;
      break label355;
      label631:
      j = 3;
      break label419;
      if (paramBoolean)
      {
        localTextView.setText(LocaleController.getString("EncryptedDescription1", 2131493431));
        break label472;
      }
      localTextView.setText(LocaleController.getString("ChatYourSelfDescription1", 2131493234));
      break label472;
      if (paramBoolean)
      {
        localTextView.setText(LocaleController.getString("EncryptedDescription2", 2131493432));
        break label472;
      }
      localTextView.setText(LocaleController.getString("ChatYourSelfDescription2", 2131493235));
      break label472;
      if (paramBoolean)
      {
        localTextView.setText(LocaleController.getString("EncryptedDescription3", 2131493433));
        break label472;
      }
      localTextView.setText(LocaleController.getString("ChatYourSelfDescription3", 2131493236));
      break label472;
      if (paramBoolean)
      {
        localTextView.setText(LocaleController.getString("EncryptedDescription4", 2131493434));
        break label472;
      }
      localTextView.setText(LocaleController.getString("ChatYourSelfDescription4", 2131493237));
      break label472;
      ((LinearLayout)localObject).addView(localImageView, LayoutHelper.createLinear(-2, -2, 8.0F, 7.0F, 0.0F, 0.0F));
    }
    label796:
    if (paramBoolean) {
      ((LinearLayout)localObject).addView(localImageView, LayoutHelper.createLinear(-2, -2, 0.0F, 4.0F, 8.0F, 0.0F));
    }
    for (;;)
    {
      ((LinearLayout)localObject).addView(localTextView, LayoutHelper.createLinear(-2, -2));
      break;
      ((LinearLayout)localObject).addView(localImageView, LayoutHelper.createLinear(-2, -2, 0.0F, 8.0F, 8.0F, 0.0F));
    }
  }
  
  public void setSecretText(String paramString)
  {
    this.secretViewStatusTextView.setText(paramString);
  }
  
  public void setTextColor(int paramInt)
  {
    int i = 0;
    while (i < this.textViews.size())
    {
      ((TextView)this.textViews.get(i)).setTextColor(paramInt);
      i += 1;
    }
    paramInt = 0;
    while (paramInt < this.imageViews.size())
    {
      ((ImageView)this.imageViews.get(paramInt)).setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_serviceText"), PorterDuff.Mode.MULTIPLY));
      paramInt += 1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/ChatBigEmptyView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */