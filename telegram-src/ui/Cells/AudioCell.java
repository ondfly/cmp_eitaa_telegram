package org.telegram.ui.Cells;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.AudioEntry;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;

public class AudioCell
  extends FrameLayout
{
  private MediaController.AudioEntry audioEntry;
  private TextView authorTextView;
  private CheckBox checkBox;
  private int currentAccount = UserConfig.selectedAccount;
  private AudioCellDelegate delegate;
  private TextView genreTextView;
  private boolean needDivider;
  private ImageView playButton;
  private TextView timeTextView;
  private TextView titleTextView;
  
  public AudioCell(Context paramContext)
  {
    super(paramContext);
    this.playButton = new ImageView(paramContext);
    Object localObject = this.playButton;
    int i;
    float f1;
    if (LocaleController.isRTL)
    {
      i = 5;
      if (!LocaleController.isRTL) {
        break label801;
      }
      f1 = 0.0F;
      label50:
      if (!LocaleController.isRTL) {
        break label807;
      }
      f2 = 13.0F;
      label59:
      addView((View)localObject, LayoutHelper.createFrame(46, 46.0F, i | 0x30, f1, 13.0F, f2, 0.0F));
      this.playButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (AudioCell.this.audioEntry != null)
          {
            if ((!MediaController.getInstance().isPlayingMessage(AudioCell.this.audioEntry.messageObject)) || (MediaController.getInstance().isMessagePaused())) {
              break label64;
            }
            MediaController.getInstance().pauseMessage(AudioCell.this.audioEntry.messageObject);
            AudioCell.this.setPlayDrawable(false);
          }
          label64:
          do
          {
            do
            {
              return;
              paramAnonymousView = new ArrayList();
              paramAnonymousView.add(AudioCell.this.audioEntry.messageObject);
            } while (!MediaController.getInstance().setPlaylist(paramAnonymousView, AudioCell.this.audioEntry.messageObject));
            AudioCell.this.setPlayDrawable(true);
          } while (AudioCell.this.delegate == null);
          AudioCell.this.delegate.startedPlayingAudio(AudioCell.this.audioEntry.messageObject);
        }
      });
      this.titleTextView = new TextView(paramContext);
      this.titleTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.titleTextView.setTextSize(1, 16.0F);
      this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.titleTextView.setLines(1);
      this.titleTextView.setMaxLines(1);
      this.titleTextView.setSingleLine(true);
      this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.titleTextView;
      if (!LocaleController.isRTL) {
        break label812;
      }
      i = 5;
      label192:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.titleTextView;
      if (!LocaleController.isRTL) {
        break label818;
      }
      i = 5;
      label217:
      if (!LocaleController.isRTL) {
        break label824;
      }
      f1 = 50.0F;
      label226:
      if (!LocaleController.isRTL) {
        break label830;
      }
      f2 = 72.0F;
      label235:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 7.0F, f2, 0.0F));
      this.genreTextView = new TextView(paramContext);
      this.genreTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.genreTextView.setTextSize(1, 14.0F);
      this.genreTextView.setLines(1);
      this.genreTextView.setMaxLines(1);
      this.genreTextView.setSingleLine(true);
      this.genreTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.genreTextView;
      if (!LocaleController.isRTL) {
        break label836;
      }
      i = 5;
      label340:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.genreTextView;
      if (!LocaleController.isRTL) {
        break label842;
      }
      i = 5;
      label365:
      if (!LocaleController.isRTL) {
        break label848;
      }
      f1 = 50.0F;
      label374:
      if (!LocaleController.isRTL) {
        break label854;
      }
      f2 = 72.0F;
      label383:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 28.0F, f2, 0.0F));
      this.authorTextView = new TextView(paramContext);
      this.authorTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
      this.authorTextView.setTextSize(1, 14.0F);
      this.authorTextView.setLines(1);
      this.authorTextView.setMaxLines(1);
      this.authorTextView.setSingleLine(true);
      this.authorTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.authorTextView;
      if (!LocaleController.isRTL) {
        break label860;
      }
      i = 5;
      label488:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.authorTextView;
      if (!LocaleController.isRTL) {
        break label866;
      }
      i = 5;
      label513:
      if (!LocaleController.isRTL) {
        break label872;
      }
      f1 = 50.0F;
      label522:
      if (!LocaleController.isRTL) {
        break label878;
      }
      f2 = 72.0F;
      label531:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 44.0F, f2, 0.0F));
      this.timeTextView = new TextView(paramContext);
      this.timeTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      this.timeTextView.setTextSize(1, 13.0F);
      this.timeTextView.setLines(1);
      this.timeTextView.setMaxLines(1);
      this.timeTextView.setSingleLine(true);
      this.timeTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.timeTextView;
      if (!LocaleController.isRTL) {
        break label884;
      }
      i = 3;
      label636:
      ((TextView)localObject).setGravity(i | 0x30);
      localObject = this.timeTextView;
      if (!LocaleController.isRTL) {
        break label890;
      }
      i = 3;
      label661:
      if (!LocaleController.isRTL) {
        break label896;
      }
      f1 = 18.0F;
      label670:
      if (!LocaleController.isRTL) {
        break label901;
      }
      f2 = 0.0F;
      label678:
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 11.0F, f2, 0.0F));
      this.checkBox = new CheckBox(paramContext, 2131165623);
      this.checkBox.setVisibility(0);
      this.checkBox.setColor(Theme.getColor("musicPicker_checkbox"), Theme.getColor("musicPicker_checkboxCheck"));
      paramContext = this.checkBox;
      if (!LocaleController.isRTL) {
        break label907;
      }
      i = j;
      label755:
      if (!LocaleController.isRTL) {
        break label913;
      }
      f1 = 18.0F;
      label764:
      if (!LocaleController.isRTL) {
        break label918;
      }
    }
    label801:
    label807:
    label812:
    label818:
    label824:
    label830:
    label836:
    label842:
    label848:
    label854:
    label860:
    label866:
    label872:
    label878:
    label884:
    label890:
    label896:
    label901:
    label907:
    label913:
    label918:
    for (float f2 = 0.0F;; f2 = 18.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(22, 22.0F, i | 0x30, f1, 39.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      f1 = 13.0F;
      break label50;
      f2 = 0.0F;
      break label59;
      i = 3;
      break label192;
      i = 3;
      break label217;
      f1 = 72.0F;
      break label226;
      f2 = 50.0F;
      break label235;
      i = 3;
      break label340;
      i = 3;
      break label365;
      f1 = 72.0F;
      break label374;
      f2 = 50.0F;
      break label383;
      i = 3;
      break label488;
      i = 3;
      break label513;
      f1 = 72.0F;
      break label522;
      f2 = 50.0F;
      break label531;
      i = 5;
      break label636;
      i = 5;
      break label661;
      f1 = 0.0F;
      break label670;
      f2 = 18.0F;
      break label678;
      i = 5;
      break label755;
      f1 = 0.0F;
      break label764;
    }
  }
  
  private void setPlayDrawable(boolean paramBoolean)
  {
    Object localObject1 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(46.0F), Theme.getColor("musicPicker_buttonBackground"), Theme.getColor("musicPicker_buttonBackground"));
    Object localObject2 = getResources();
    if (paramBoolean) {}
    for (int i = 2131165224;; i = 2131165225)
    {
      localObject2 = ((Resources)localObject2).getDrawable(i);
      ((Drawable)localObject2).setColorFilter(new PorterDuffColorFilter(Theme.getColor("musicPicker_buttonIcon"), PorterDuff.Mode.MULTIPLY));
      localObject1 = new CombinedDrawable((Drawable)localObject1, (Drawable)localObject2);
      ((CombinedDrawable)localObject1).setCustomSize(AndroidUtilities.dp(46.0F), AndroidUtilities.dp(46.0F));
      this.playButton.setBackgroundDrawable((Drawable)localObject1);
      return;
    }
  }
  
  public MediaController.AudioEntry getAudioEntry()
  {
    return this.audioEntry;
  }
  
  public TextView getAuthorTextView()
  {
    return this.authorTextView;
  }
  
  public CheckBox getCheckBox()
  {
    return this.checkBox;
  }
  
  public TextView getGenreTextView()
  {
    return this.genreTextView;
  }
  
  public ImageView getPlayButton()
  {
    return this.playButton;
  }
  
  public TextView getTimeTextView()
  {
    return this.timeTextView;
  }
  
  public TextView getTitleTextView()
  {
    return this.titleTextView;
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(AndroidUtilities.dp(72.0F), getHeight() - 1, getWidth(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(72.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void setAudio(MediaController.AudioEntry paramAudioEntry, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool2 = true;
    this.audioEntry = paramAudioEntry;
    this.titleTextView.setText(this.audioEntry.title);
    this.genreTextView.setText(this.audioEntry.genre);
    this.authorTextView.setText(this.audioEntry.author);
    this.timeTextView.setText(String.format("%d:%02d", new Object[] { Integer.valueOf(this.audioEntry.duration / 60), Integer.valueOf(this.audioEntry.duration % 60) }));
    boolean bool1;
    if ((MediaController.getInstance().isPlayingMessage(this.audioEntry.messageObject)) && (!MediaController.getInstance().isMessagePaused()))
    {
      bool1 = true;
      setPlayDrawable(bool1);
      this.needDivider = paramBoolean1;
      if (paramBoolean1) {
        break label166;
      }
    }
    label166:
    for (paramBoolean1 = bool2;; paramBoolean1 = false)
    {
      setWillNotDraw(paramBoolean1);
      this.checkBox.setChecked(paramBoolean2, false);
      return;
      bool1 = false;
      break;
    }
  }
  
  public void setChecked(boolean paramBoolean)
  {
    this.checkBox.setChecked(paramBoolean, true);
  }
  
  public void setDelegate(AudioCellDelegate paramAudioCellDelegate)
  {
    this.delegate = paramAudioCellDelegate;
  }
  
  public static abstract interface AudioCellDelegate
  {
    public abstract void startedPlayingAudio(MessageObject paramMessageObject);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/AudioCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */