package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColorSpanUnderline;
import org.telegram.ui.Components.LayoutHelper;

public class StickerSetNameCell
  extends FrameLayout
{
  private ImageView buttonView;
  private boolean empty;
  private TextView textView;
  private TextView urlTextView;
  
  public StickerSetNameCell(Context paramContext)
  {
    super(paramContext);
    this.textView = new TextView(paramContext);
    this.textView.setTextColor(Theme.getColor("chat_emojiPanelStickerSetName"));
    this.textView.setTextSize(1, 14.0F);
    this.textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    this.textView.setSingleLine(true);
    addView(this.textView, LayoutHelper.createFrame(-2, -2.0F, 51, 17.0F, 4.0F, 57.0F, 0.0F));
    this.urlTextView = new TextView(paramContext);
    this.urlTextView.setTextColor(Theme.getColor("chat_emojiPanelStickerSetName"));
    this.urlTextView.setTextSize(1, 12.0F);
    this.urlTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.urlTextView.setSingleLine(true);
    this.urlTextView.setVisibility(4);
    addView(this.urlTextView, LayoutHelper.createFrame(-2, -2.0F, 53, 17.0F, 6.0F, 17.0F, 0.0F));
    this.buttonView = new ImageView(paramContext);
    this.buttonView.setScaleType(ImageView.ScaleType.CENTER);
    this.buttonView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chat_emojiPanelStickerSetNameIcon"), PorterDuff.Mode.MULTIPLY));
    addView(this.buttonView, LayoutHelper.createFrame(24, 24.0F, 53, 0.0F, 0.0F, 16.0F, 0.0F));
  }
  
  public void invalidate()
  {
    this.textView.invalidate();
    super.invalidate();
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    if (this.empty)
    {
      super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(1, 1073741824));
      return;
    }
    super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0F), 1073741824));
  }
  
  public void setOnIconClickListener(View.OnClickListener paramOnClickListener)
  {
    this.buttonView.setOnClickListener(paramOnClickListener);
  }
  
  public void setText(CharSequence paramCharSequence, int paramInt)
  {
    setText(paramCharSequence, paramInt, 0, 0);
  }
  
  public void setText(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramCharSequence == null)
    {
      this.empty = true;
      this.textView.setText("");
      this.buttonView.setVisibility(4);
      return;
    }
    if (paramInt3 != 0) {
      paramCharSequence = new SpannableStringBuilder(paramCharSequence);
    }
    try
    {
      paramCharSequence.setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt2, paramInt2 + paramInt3, 33);
      this.textView.setText(paramCharSequence);
      while (paramInt1 != 0)
      {
        this.buttonView.setImageResource(paramInt1);
        this.buttonView.setVisibility(0);
        return;
        this.textView.setText(Emoji.replaceEmoji(paramCharSequence, this.textView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14.0F), false));
      }
      this.buttonView.setVisibility(4);
      return;
    }
    catch (Exception localException)
    {
      for (;;) {}
    }
  }
  
  public void setUrl(CharSequence paramCharSequence, int paramInt)
  {
    SpannableStringBuilder localSpannableStringBuilder;
    if (paramCharSequence != null) {
      localSpannableStringBuilder = new SpannableStringBuilder(paramCharSequence);
    }
    try
    {
      localSpannableStringBuilder.setSpan(new ColorSpanUnderline(Theme.getColor("windowBackgroundWhiteBlueText4")), 0, paramInt, 33);
      localSpannableStringBuilder.setSpan(new ColorSpanUnderline(Theme.getColor("chat_emojiPanelStickerSetName")), paramInt, paramCharSequence.length(), 33);
      this.urlTextView.setText(localSpannableStringBuilder);
      this.urlTextView.setVisibility(0);
      return;
      this.urlTextView.setVisibility(8);
      return;
    }
    catch (Exception paramCharSequence)
    {
      for (;;) {}
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/StickerSetNameCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */