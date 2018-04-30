package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.DownloadController.FileDownloadProgressListener;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.ImageReceiver.ImageReceiverDelegate;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LineProgressView;

public class SharedDocumentCell
  extends FrameLayout
  implements DownloadController.FileDownloadProgressListener
{
  private int TAG = DownloadController.getInstance(this.currentAccount).generateObserverTag();
  private CheckBox checkBox;
  private int currentAccount = UserConfig.selectedAccount;
  private TextView dateTextView;
  private TextView extTextView;
  private int[] icons = { 2131165483, 2131165484, 2131165487, 2131165488 };
  private boolean loaded;
  private boolean loading;
  private MessageObject message;
  private TextView nameTextView;
  private boolean needDivider;
  private ImageView placeholderImageView;
  private LineProgressView progressView;
  private ImageView statusImageView;
  private BackupImageView thumbImageView;
  
  public SharedDocumentCell(Context paramContext)
  {
    super(paramContext);
    this.placeholderImageView = new ImageView(paramContext);
    Object localObject = this.placeholderImageView;
    int i;
    float f1;
    if (LocaleController.isRTL)
    {
      i = 5;
      if (!LocaleController.isRTL) {
        break label931;
      }
      f1 = 0.0F;
      label88:
      if (!LocaleController.isRTL) {
        break label937;
      }
      f2 = 12.0F;
      label97:
      addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, i | 0x30, f1, 8.0F, f2, 0.0F));
      this.extTextView = new TextView(paramContext);
      this.extTextView.setTextColor(Theme.getColor("files_iconText"));
      this.extTextView.setTextSize(1, 14.0F);
      this.extTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.extTextView.setLines(1);
      this.extTextView.setMaxLines(1);
      this.extTextView.setSingleLine(true);
      this.extTextView.setGravity(17);
      this.extTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.extTextView;
      if (!LocaleController.isRTL) {
        break label942;
      }
      i = 5;
      label224:
      if (!LocaleController.isRTL) {
        break label948;
      }
      f1 = 0.0F;
      label232:
      if (!LocaleController.isRTL) {
        break label954;
      }
      f2 = 16.0F;
      label241:
      addView((View)localObject, LayoutHelper.createFrame(32, -2.0F, i | 0x30, f1, 22.0F, f2, 0.0F));
      this.thumbImageView = new BackupImageView(paramContext);
      localObject = this.thumbImageView;
      if (!LocaleController.isRTL) {
        break label959;
      }
      i = 5;
      label291:
      if (!LocaleController.isRTL) {
        break label965;
      }
      f1 = 0.0F;
      label299:
      if (!LocaleController.isRTL) {
        break label971;
      }
      f2 = 12.0F;
      label308:
      addView((View)localObject, LayoutHelper.createFrame(40, 40.0F, i | 0x30, f1, 8.0F, f2, 0.0F));
      this.thumbImageView.getImageReceiver().setDelegate(new ImageReceiver.ImageReceiverDelegate()
      {
        public void didSetImage(ImageReceiver paramAnonymousImageReceiver, boolean paramAnonymousBoolean1, boolean paramAnonymousBoolean2)
        {
          int j = 4;
          paramAnonymousImageReceiver = SharedDocumentCell.this.extTextView;
          if (paramAnonymousBoolean1)
          {
            i = 4;
            paramAnonymousImageReceiver.setVisibility(i);
            paramAnonymousImageReceiver = SharedDocumentCell.this.placeholderImageView;
            if (!paramAnonymousBoolean1) {
              break label53;
            }
          }
          label53:
          for (int i = j;; i = 0)
          {
            paramAnonymousImageReceiver.setVisibility(i);
            return;
            i = 0;
            break;
          }
        }
      });
      this.nameTextView = new TextView(paramContext);
      this.nameTextView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
      this.nameTextView.setTextSize(1, 16.0F);
      this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.nameTextView.setLines(1);
      this.nameTextView.setMaxLines(1);
      this.nameTextView.setSingleLine(true);
      this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label976;
      }
      i = 5;
      label444:
      ((TextView)localObject).setGravity(i | 0x10);
      localObject = this.nameTextView;
      if (!LocaleController.isRTL) {
        break label982;
      }
      i = 5;
      label469:
      if (!LocaleController.isRTL) {
        break label988;
      }
      f1 = 8.0F;
      label478:
      if (!LocaleController.isRTL) {
        break label994;
      }
      f2 = 72.0F;
      label487:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 5.0F, f2, 0.0F));
      this.statusImageView = new ImageView(paramContext);
      this.statusImageView.setVisibility(4);
      this.statusImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("sharedMedia_startStopLoadIcon"), PorterDuff.Mode.MULTIPLY));
      localObject = this.statusImageView;
      if (!LocaleController.isRTL) {
        break label1000;
      }
      i = 5;
      label566:
      if (!LocaleController.isRTL) {
        break label1006;
      }
      f1 = 8.0F;
      label575:
      if (!LocaleController.isRTL) {
        break label1012;
      }
      f2 = 72.0F;
      label584:
      addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F, i | 0x30, f1, 35.0F, f2, 0.0F));
      this.dateTextView = new TextView(paramContext);
      this.dateTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText3"));
      this.dateTextView.setTextSize(1, 14.0F);
      this.dateTextView.setLines(1);
      this.dateTextView.setMaxLines(1);
      this.dateTextView.setSingleLine(true);
      this.dateTextView.setEllipsize(TextUtils.TruncateAt.END);
      localObject = this.dateTextView;
      if (!LocaleController.isRTL) {
        break label1018;
      }
      i = 5;
      label690:
      ((TextView)localObject).setGravity(i | 0x10);
      localObject = this.dateTextView;
      if (!LocaleController.isRTL) {
        break label1024;
      }
      i = 5;
      label715:
      if (!LocaleController.isRTL) {
        break label1030;
      }
      f1 = 8.0F;
      label724:
      if (!LocaleController.isRTL) {
        break label1036;
      }
      f2 = 72.0F;
      label733:
      addView((View)localObject, LayoutHelper.createFrame(-1, -2.0F, i | 0x30, f1, 30.0F, f2, 0.0F));
      this.progressView = new LineProgressView(paramContext);
      this.progressView.setProgressColor(Theme.getColor("sharedMedia_startStopLoadIcon"));
      localObject = this.progressView;
      if (!LocaleController.isRTL) {
        break label1042;
      }
      i = 5;
      label794:
      if (!LocaleController.isRTL) {
        break label1048;
      }
      f1 = 0.0F;
      label802:
      if (!LocaleController.isRTL) {
        break label1054;
      }
      f2 = 72.0F;
      label811:
      addView((View)localObject, LayoutHelper.createFrame(-1, 2.0F, i | 0x30, f1, 54.0F, f2, 0.0F));
      this.checkBox = new CheckBox(paramContext, 2131165623);
      this.checkBox.setVisibility(4);
      this.checkBox.setColor(Theme.getColor("checkbox"), Theme.getColor("checkboxCheck"));
      paramContext = this.checkBox;
      if (!LocaleController.isRTL) {
        break label1059;
      }
      i = 5;
      label885:
      if (!LocaleController.isRTL) {
        break label1065;
      }
      f1 = 0.0F;
      label893:
      if (!LocaleController.isRTL) {
        break label1071;
      }
    }
    label931:
    label937:
    label942:
    label948:
    label954:
    label959:
    label965:
    label971:
    label976:
    label982:
    label988:
    label994:
    label1000:
    label1006:
    label1012:
    label1018:
    label1024:
    label1030:
    label1036:
    label1042:
    label1048:
    label1054:
    label1059:
    label1065:
    label1071:
    for (float f2 = 34.0F;; f2 = 0.0F)
    {
      addView(paramContext, LayoutHelper.createFrame(22, 22.0F, i | 0x30, f1, 30.0F, f2, 0.0F));
      return;
      i = 3;
      break;
      f1 = 12.0F;
      break label88;
      f2 = 0.0F;
      break label97;
      i = 3;
      break label224;
      f1 = 16.0F;
      break label232;
      f2 = 0.0F;
      break label241;
      i = 3;
      break label291;
      f1 = 12.0F;
      break label299;
      f2 = 0.0F;
      break label308;
      i = 3;
      break label444;
      i = 3;
      break label469;
      f1 = 72.0F;
      break label478;
      f2 = 8.0F;
      break label487;
      i = 3;
      break label566;
      f1 = 72.0F;
      break label575;
      f2 = 8.0F;
      break label584;
      i = 3;
      break label690;
      i = 3;
      break label715;
      f1 = 72.0F;
      break label724;
      f2 = 8.0F;
      break label733;
      i = 3;
      break label794;
      f1 = 72.0F;
      break label802;
      f2 = 0.0F;
      break label811;
      i = 3;
      break label885;
      f1 = 34.0F;
      break label893;
    }
  }
  
  private int getThumbForNameOrMime(String paramString1, String paramString2)
  {
    if ((paramString1 != null) && (paramString1.length() != 0))
    {
      int i = -1;
      if ((paramString1.contains(".doc")) || (paramString1.contains(".txt")) || (paramString1.contains(".psd")))
      {
        i = 0;
        j = i;
        if (i == -1)
        {
          i = paramString1.lastIndexOf('.');
          if (i != -1) {
            break label218;
          }
          paramString2 = "";
          label65:
          if (paramString2.length() == 0) {
            break label229;
          }
        }
      }
      label218:
      label229:
      for (int j = paramString2.charAt(0) % this.icons.length;; j = paramString1.charAt(0) % this.icons.length)
      {
        return this.icons[j];
        if ((paramString1.contains(".xls")) || (paramString1.contains(".csv")))
        {
          i = 1;
          break;
        }
        if ((paramString1.contains(".pdf")) || (paramString1.contains(".ppt")) || (paramString1.contains(".key")))
        {
          i = 2;
          break;
        }
        if ((!paramString1.contains(".zip")) && (!paramString1.contains(".rar")) && (!paramString1.contains(".ai")) && (!paramString1.contains(".mp3")) && (!paramString1.contains(".mov")) && (!paramString1.contains(".avi"))) {
          break;
        }
        i = 3;
        break;
        paramString2 = paramString1.substring(i + 1);
        break label65;
      }
    }
    return this.icons[0];
  }
  
  public MessageObject getMessage()
  {
    return this.message;
  }
  
  public int getObserverTag()
  {
    return this.TAG;
  }
  
  public boolean isLoaded()
  {
    return this.loaded;
  }
  
  public boolean isLoading()
  {
    return this.loading;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (this.progressView.getVisibility() == 0) {
      updateFileExistIcon();
    }
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(AndroidUtilities.dp(72.0F), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
  }
  
  public void onFailedDownload(String paramString)
  {
    updateFileExistIcon();
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(56.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void onProgressDownload(String paramString, float paramFloat)
  {
    if (this.progressView.getVisibility() != 0) {
      updateFileExistIcon();
    }
    this.progressView.setProgress(paramFloat, true);
  }
  
  public void onProgressUpload(String paramString, float paramFloat, boolean paramBoolean) {}
  
  public void onSuccessDownload(String paramString)
  {
    this.progressView.setProgress(1.0F, true);
    updateFileExistIcon();
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.checkBox.getVisibility() != 0) {
      this.checkBox.setVisibility(0);
    }
    this.checkBox.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setDocument(MessageObject paramMessageObject, boolean paramBoolean)
  {
    this.needDivider = paramBoolean;
    this.message = paramMessageObject;
    this.loaded = false;
    this.loading = false;
    Object localObject1;
    Object localObject3;
    int i;
    if ((paramMessageObject != null) && (paramMessageObject.getDocument() != null))
    {
      localObject1 = null;
      Object localObject2 = null;
      if (paramMessageObject.isMusic())
      {
        if (paramMessageObject.type == 0) {}
        for (localObject3 = paramMessageObject.messageOwner.media.webpage.document;; localObject3 = paramMessageObject.messageOwner.media.document)
        {
          i = 0;
          for (;;)
          {
            localObject1 = localObject2;
            if (i >= ((TLRPC.Document)localObject3).attributes.size()) {
              break;
            }
            TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)((TLRPC.Document)localObject3).attributes.get(i);
            localObject1 = localObject2;
            if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio)) {
              if ((localDocumentAttribute.performer == null) || (localDocumentAttribute.performer.length() == 0))
              {
                localObject1 = localObject2;
                if (localDocumentAttribute.title != null)
                {
                  localObject1 = localObject2;
                  if (localDocumentAttribute.title.length() == 0) {}
                }
              }
              else
              {
                localObject1 = paramMessageObject.getMusicAuthor() + " - " + paramMessageObject.getMusicTitle();
              }
            }
            i += 1;
            localObject2 = localObject1;
          }
        }
      }
      localObject3 = FileLoader.getDocumentFileName(paramMessageObject.getDocument());
      localObject2 = localObject1;
      if (localObject1 == null) {
        localObject2 = localObject3;
      }
      this.nameTextView.setText((CharSequence)localObject2);
      this.placeholderImageView.setVisibility(0);
      this.extTextView.setVisibility(0);
      this.placeholderImageView.setImageResource(getThumbForNameOrMime((String)localObject3, paramMessageObject.getDocument().mime_type));
      localObject2 = this.extTextView;
      i = ((String)localObject3).lastIndexOf('.');
      if (i == -1)
      {
        localObject1 = "";
        ((TextView)localObject2).setText((CharSequence)localObject1);
        if ((!(paramMessageObject.getDocument().thumb instanceof TLRPC.TL_photoSizeEmpty)) && (paramMessageObject.getDocument().thumb != null)) {
          break label497;
        }
        this.thumbImageView.setVisibility(4);
        this.thumbImageView.setImageBitmap(null);
        label350:
        long l = paramMessageObject.messageOwner.date * 1000L;
        this.dateTextView.setText(String.format("%s, %s", new Object[] { AndroidUtilities.formatFileSize(paramMessageObject.getDocument().size), LocaleController.formatString("formatDateAtTime", 2131494696, new Object[] { LocaleController.getInstance().formatterYear.format(new Date(l)), LocaleController.getInstance().formatterDay.format(new Date(l)) }) }));
        label453:
        if (this.needDivider) {
          break label594;
        }
      }
    }
    label497:
    label594:
    for (paramBoolean = true;; paramBoolean = false)
    {
      setWillNotDraw(paramBoolean);
      this.progressView.setProgress(0.0F, false);
      updateFileExistIcon();
      return;
      localObject1 = ((String)localObject3).substring(i + 1).toLowerCase();
      break;
      this.thumbImageView.setVisibility(0);
      this.thumbImageView.setImage(paramMessageObject.getDocument().thumb.location, "40_40", (Drawable)null);
      break label350;
      this.nameTextView.setText("");
      this.extTextView.setText("");
      this.dateTextView.setText("");
      this.placeholderImageView.setVisibility(0);
      this.extTextView.setVisibility(0);
      this.thumbImageView.setVisibility(4);
      this.thumbImageView.setImageBitmap(null);
      break label453;
    }
  }
  
  public void setTextAndValueAndTypeAndThumb(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
  {
    this.nameTextView.setText(paramString1);
    this.dateTextView.setText(paramString2);
    if (paramString3 != null)
    {
      this.extTextView.setVisibility(0);
      this.extTextView.setText(paramString3);
      if (paramInt != 0) {
        break label110;
      }
      this.placeholderImageView.setImageResource(getThumbForNameOrMime(paramString1, paramString3));
      this.placeholderImageView.setVisibility(0);
      label62:
      if ((paramString4 == null) && (paramInt == 0)) {
        break label165;
      }
      if (paramString4 == null) {
        break label121;
      }
      this.thumbImageView.setImage(paramString4, "40_40", null);
    }
    for (;;)
    {
      this.thumbImageView.setVisibility(0);
      return;
      this.extTextView.setVisibility(4);
      break;
      label110:
      this.placeholderImageView.setVisibility(4);
      break label62;
      label121:
      paramString1 = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(40.0F), paramInt);
      Theme.setCombinedDrawableColor(paramString1, Theme.getColor("files_folderIconBackground"), false);
      Theme.setCombinedDrawableColor(paramString1, Theme.getColor("files_folderIcon"), true);
      this.thumbImageView.setImageDrawable(paramString1);
    }
    label165:
    this.thumbImageView.setImageBitmap(null);
    this.thumbImageView.setVisibility(4);
  }
  
  public void updateFileExistIcon()
  {
    if ((this.message != null) && (this.message.messageOwner.media != null))
    {
      Object localObject2 = null;
      Object localObject1;
      if ((this.message.messageOwner.attachPath != null) && (this.message.messageOwner.attachPath.length() != 0))
      {
        localObject1 = localObject2;
        if (new File(this.message.messageOwner.attachPath).exists()) {}
      }
      else
      {
        localObject1 = localObject2;
        if (!FileLoader.getPathToMessage(this.message.messageOwner).exists()) {
          localObject1 = FileLoader.getAttachFileName(this.message.getDocument());
        }
      }
      this.loaded = false;
      if (localObject1 == null)
      {
        this.statusImageView.setVisibility(4);
        this.progressView.setVisibility(4);
        this.dateTextView.setPadding(0, 0, 0, 0);
        this.loading = false;
        this.loaded = true;
        DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
        return;
      }
      DownloadController.getInstance(this.currentAccount).addLoadingFileObserver((String)localObject1, this);
      this.loading = FileLoader.getInstance(this.currentAccount).isLoadingFile((String)localObject1);
      this.statusImageView.setVisibility(0);
      localObject2 = this.statusImageView;
      int i;
      if (this.loading)
      {
        i = 2131165486;
        ((ImageView)localObject2).setImageResource(i);
        localObject2 = this.dateTextView;
        if (!LocaleController.isRTL) {
          break label316;
        }
        i = 0;
        label238:
        if (!LocaleController.isRTL) {
          break label325;
        }
      }
      label316:
      label325:
      for (int j = AndroidUtilities.dp(14.0F);; j = 0)
      {
        ((TextView)localObject2).setPadding(i, 0, j, 0);
        if (!this.loading) {
          break label330;
        }
        this.progressView.setVisibility(0);
        localObject2 = ImageLoader.getInstance().getFileProgress((String)localObject1);
        localObject1 = localObject2;
        if (localObject2 == null) {
          localObject1 = Float.valueOf(0.0F);
        }
        this.progressView.setProgress(((Float)localObject1).floatValue(), false);
        return;
        i = 2131165485;
        break;
        i = AndroidUtilities.dp(14.0F);
        break label238;
      }
      label330:
      this.progressView.setVisibility(4);
      return;
    }
    this.loading = false;
    this.loaded = true;
    this.progressView.setVisibility(4);
    this.progressView.setProgress(0.0F, false);
    this.statusImageView.setVisibility(4);
    this.dateTextView.setPadding(0, 0, 0, 0);
    DownloadController.getInstance(this.currentAccount).removeLoadingFileObserver(this);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/SharedDocumentCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */