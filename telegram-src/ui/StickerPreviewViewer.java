package org.telegram.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.StickerCell;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;

public class StickerPreviewViewer
{
  @SuppressLint({"StaticFieldLeak"})
  private static volatile StickerPreviewViewer Instance = null;
  private static TextPaint textPaint;
  private ColorDrawable backgroundDrawable = new ColorDrawable(1895825408);
  private ImageReceiver centerImage = new ImageReceiver();
  private FrameLayoutDrawer containerView;
  private int currentAccount;
  private TLRPC.InputStickerSet currentSet;
  private TLRPC.Document currentSticker;
  private View currentStickerPreviewCell;
  private StickerPreviewViewerDelegate delegate;
  private boolean isVisible = false;
  private int keyboardHeight = AndroidUtilities.dp(200.0F);
  private long lastUpdateTime;
  private Runnable openStickerPreviewRunnable;
  private Activity parentActivity;
  private float showProgress;
  private Runnable showSheetRunnable = new Runnable()
  {
    public void run()
    {
      if ((StickerPreviewViewer.this.parentActivity == null) || (StickerPreviewViewer.this.currentSet == null)) {
        return;
      }
      final boolean bool = DataQuery.getInstance(StickerPreviewViewer.this.currentAccount).isStickerInFavorites(StickerPreviewViewer.this.currentSticker);
      BottomSheet.Builder localBuilder = new BottomSheet.Builder(StickerPreviewViewer.this.parentActivity);
      ArrayList localArrayList1 = new ArrayList();
      final ArrayList localArrayList2 = new ArrayList();
      ArrayList localArrayList3 = new ArrayList();
      if (StickerPreviewViewer.this.delegate != null)
      {
        if (StickerPreviewViewer.this.delegate.needSend())
        {
          localArrayList1.add(LocaleController.getString("SendStickerPreview", 2131494353));
          localArrayList3.add(Integer.valueOf(2131165663));
          localArrayList2.add(Integer.valueOf(0));
        }
        localArrayList1.add(LocaleController.formatString("ViewPackPreview", 2131494580, new Object[0]));
        localArrayList3.add(Integer.valueOf(2131165662));
        localArrayList2.add(Integer.valueOf(1));
      }
      Object localObject;
      if ((!MessageObject.isMaskDocument(StickerPreviewViewer.this.currentSticker)) && ((bool) || (DataQuery.getInstance(StickerPreviewViewer.this.currentAccount).canAddStickerToFavorites())))
      {
        if (!bool) {
          break label310;
        }
        localObject = LocaleController.getString("DeleteFromFavorites", 2131493369);
        label227:
        localArrayList1.add(localObject);
        if (!bool) {
          break label321;
        }
      }
      label310:
      label321:
      for (int i = 2131165664;; i = 2131165660)
      {
        localArrayList3.add(Integer.valueOf(i));
        localArrayList2.add(Integer.valueOf(2));
        if (localArrayList1.isEmpty()) {
          break;
        }
        localObject = new int[localArrayList3.size()];
        i = 0;
        while (i < localArrayList3.size())
        {
          localObject[i] = ((Integer)localArrayList3.get(i)).intValue();
          i += 1;
        }
        localObject = LocaleController.getString("AddToFavorites", 2131492941);
        break label227;
      }
      localBuilder.setItems((CharSequence[])localArrayList1.toArray(new CharSequence[localArrayList1.size()]), (int[])localObject, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
        {
          if (StickerPreviewViewer.this.parentActivity == null) {}
          do
          {
            do
            {
              do
              {
                return;
                if (((Integer)localArrayList2.get(paramAnonymous2Int)).intValue() != 0) {
                  break;
                }
              } while (StickerPreviewViewer.this.delegate == null);
              StickerPreviewViewer.this.delegate.sendSticker(StickerPreviewViewer.this.currentSticker);
              return;
              if (((Integer)localArrayList2.get(paramAnonymous2Int)).intValue() != 1) {
                break;
              }
            } while (StickerPreviewViewer.this.delegate == null);
            StickerPreviewViewer.this.delegate.openSet(StickerPreviewViewer.this.currentSet);
            return;
          } while (((Integer)localArrayList2.get(paramAnonymous2Int)).intValue() != 2);
          DataQuery.getInstance(StickerPreviewViewer.this.currentAccount).addRecentSticker(2, StickerPreviewViewer.this.currentSticker, (int)(System.currentTimeMillis() / 1000L), bool);
        }
      });
      StickerPreviewViewer.access$602(StickerPreviewViewer.this, localBuilder.create());
      StickerPreviewViewer.this.visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
      {
        public void onDismiss(DialogInterface paramAnonymous2DialogInterface)
        {
          StickerPreviewViewer.access$602(StickerPreviewViewer.this, null);
          StickerPreviewViewer.this.close();
        }
      });
      StickerPreviewViewer.this.visibleDialog.show();
      StickerPreviewViewer.this.containerView.performHapticFeedback(0);
    }
  };
  private int startX;
  private int startY;
  private StaticLayout stickerEmojiLayout;
  private Dialog visibleDialog;
  private WindowManager.LayoutParams windowLayoutParams;
  private FrameLayout windowView;
  
  public static StickerPreviewViewer getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          StickerPreviewViewer localStickerPreviewViewer2 = Instance;
          localObject1 = localStickerPreviewViewer2;
          if (localStickerPreviewViewer2 == null) {
            localObject1 = new StickerPreviewViewer();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (StickerPreviewViewer)localObject1;
          return (StickerPreviewViewer)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localStickerPreviewViewer1;
  }
  
  public static boolean hasInstance()
  {
    return Instance != null;
  }
  
  @SuppressLint({"DrawAllocation"})
  private void onDraw(Canvas paramCanvas)
  {
    if ((this.containerView == null) || (this.backgroundDrawable == null)) {}
    for (;;)
    {
      return;
      this.backgroundDrawable.setAlpha((int)(180.0F * this.showProgress));
      this.backgroundDrawable.setBounds(0, 0, this.containerView.getWidth(), this.containerView.getHeight());
      this.backgroundDrawable.draw(paramCanvas);
      paramCanvas.save();
      int j = (int)(Math.min(this.containerView.getWidth(), this.containerView.getHeight()) / 1.8F);
      float f = this.containerView.getWidth() / 2;
      int k = j / 2;
      int m = AndroidUtilities.statusBarHeight;
      if (this.stickerEmojiLayout != null) {}
      long l1;
      long l2;
      for (int i = AndroidUtilities.dp(40.0F);; i = 0)
      {
        paramCanvas.translate(f, Math.max(i + (m + k), (this.containerView.getHeight() - this.keyboardHeight) / 2));
        if (this.centerImage.getBitmap() != null)
        {
          f = 0.8F * this.showProgress / 0.8F;
          i = (int)(j * f);
          this.centerImage.setAlpha(this.showProgress);
          this.centerImage.setImageCoords(-i / 2, -i / 2, i, i);
          this.centerImage.draw(paramCanvas);
        }
        if (this.stickerEmojiLayout != null)
        {
          paramCanvas.translate(-AndroidUtilities.dp(50.0F), -this.centerImage.getImageHeight() / 2 - AndroidUtilities.dp(30.0F));
          this.stickerEmojiLayout.draw(paramCanvas);
        }
        paramCanvas.restore();
        if (!this.isVisible) {
          break label345;
        }
        if (this.showProgress == 1.0F) {
          break;
        }
        l1 = System.currentTimeMillis();
        l2 = this.lastUpdateTime;
        this.lastUpdateTime = l1;
        this.showProgress += (float)(l1 - l2) / 120.0F;
        this.containerView.invalidate();
        if (this.showProgress <= 1.0F) {
          break;
        }
        this.showProgress = 1.0F;
        return;
      }
      label345:
      if (this.showProgress != 0.0F)
      {
        l1 = System.currentTimeMillis();
        l2 = this.lastUpdateTime;
        this.lastUpdateTime = l1;
        this.showProgress -= (float)(l1 - l2) / 120.0F;
        this.containerView.invalidate();
        if (this.showProgress < 0.0F) {
          this.showProgress = 0.0F;
        }
        if (this.showProgress == 0.0F)
        {
          AndroidUtilities.unlockOrientation(this.parentActivity);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              StickerPreviewViewer.this.centerImage.setImageBitmap((Bitmap)null);
            }
          });
          try
          {
            if (this.windowView.getParent() != null)
            {
              ((WindowManager)this.parentActivity.getSystemService("window")).removeView(this.windowView);
              return;
            }
          }
          catch (Exception paramCanvas)
          {
            FileLog.e(paramCanvas);
          }
        }
      }
    }
  }
  
  public void close()
  {
    if ((this.parentActivity == null) || (this.visibleDialog != null)) {
      return;
    }
    AndroidUtilities.cancelRunOnUIThread(this.showSheetRunnable);
    this.showProgress = 1.0F;
    this.lastUpdateTime = System.currentTimeMillis();
    this.containerView.invalidate();
    try
    {
      if (this.visibleDialog != null)
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
      this.currentSticker = null;
      this.currentSet = null;
      this.delegate = null;
      this.isVisible = false;
      return;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public void destroy()
  {
    this.isVisible = false;
    this.delegate = null;
    this.currentSticker = null;
    this.currentSet = null;
    try
    {
      if (this.visibleDialog != null)
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
      if ((this.parentActivity == null) || (this.windowView == null)) {
        return;
      }
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        FileLog.e(localException1);
      }
    }
    try
    {
      if (this.windowView.getParent() != null) {
        ((WindowManager)this.parentActivity.getSystemService("window")).removeViewImmediate(this.windowView);
      }
      this.windowView = null;
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        FileLog.e(localException2);
      }
    }
    Instance = null;
  }
  
  public boolean isVisible()
  {
    return this.isVisible;
  }
  
  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent, final View paramView, final int paramInt, StickerPreviewViewerDelegate paramStickerPreviewViewerDelegate)
  {
    this.delegate = paramStickerPreviewViewerDelegate;
    if (paramMotionEvent.getAction() == 0)
    {
      int k = (int)paramMotionEvent.getX();
      int m = (int)paramMotionEvent.getY();
      int i = 0;
      int j;
      if ((paramView instanceof AbsListView))
      {
        i = ((AbsListView)paramView).getChildCount();
        j = 0;
      }
      for (;;)
      {
        if (j < i)
        {
          paramMotionEvent = null;
          if ((paramView instanceof AbsListView)) {
            paramMotionEvent = ((AbsListView)paramView).getChildAt(j);
          }
          for (;;)
          {
            if (paramMotionEvent != null) {
              break label120;
            }
            return false;
            if (!(paramView instanceof RecyclerListView)) {
              break;
            }
            i = ((RecyclerListView)paramView).getChildCount();
            break;
            if ((paramView instanceof RecyclerListView)) {
              paramMotionEvent = ((RecyclerListView)paramView).getChildAt(j);
            }
          }
          label120:
          int n = paramMotionEvent.getTop();
          int i1 = paramMotionEvent.getBottom();
          int i2 = paramMotionEvent.getLeft();
          int i3 = paramMotionEvent.getRight();
          if ((n > m) || (i1 < m) || (i2 > k) || (i3 < k))
          {
            j += 1;
          }
          else
          {
            boolean bool = false;
            if ((paramMotionEvent instanceof StickerEmojiCell)) {
              bool = ((StickerEmojiCell)paramMotionEvent).showingBitmap();
            }
            while (!bool)
            {
              return false;
              if ((paramMotionEvent instanceof StickerCell))
              {
                bool = ((StickerCell)paramMotionEvent).showingBitmap();
              }
              else if ((paramMotionEvent instanceof ContextLinkCell))
              {
                paramStickerPreviewViewerDelegate = (ContextLinkCell)paramMotionEvent;
                if ((paramStickerPreviewViewerDelegate.isSticker()) && (paramStickerPreviewViewerDelegate.showingBitmap())) {}
                for (bool = true;; bool = false) {
                  break;
                }
              }
            }
            this.startX = k;
            this.startY = m;
            this.currentStickerPreviewCell = paramMotionEvent;
            this.openStickerPreviewRunnable = new Runnable()
            {
              public void run()
              {
                if (StickerPreviewViewer.this.openStickerPreviewRunnable == null) {}
                do
                {
                  return;
                  if ((paramView instanceof AbsListView))
                  {
                    ((AbsListView)paramView).setOnItemClickListener(null);
                    ((AbsListView)paramView).requestDisallowInterceptTouchEvent(true);
                  }
                  for (;;)
                  {
                    StickerPreviewViewer.access$802(StickerPreviewViewer.this, null);
                    StickerPreviewViewer.this.setParentActivity((Activity)paramView.getContext());
                    StickerPreviewViewer.this.setKeyboardHeight(paramInt);
                    if (!(StickerPreviewViewer.this.currentStickerPreviewCell instanceof StickerEmojiCell)) {
                      break;
                    }
                    StickerPreviewViewer.this.open(((StickerEmojiCell)StickerPreviewViewer.this.currentStickerPreviewCell).getSticker(), ((StickerEmojiCell)StickerPreviewViewer.this.currentStickerPreviewCell).isRecent());
                    ((StickerEmojiCell)StickerPreviewViewer.this.currentStickerPreviewCell).setScaled(true);
                    return;
                    if ((paramView instanceof RecyclerListView))
                    {
                      ((RecyclerListView)paramView).setOnItemClickListener((RecyclerListView.OnItemClickListener)null);
                      ((RecyclerListView)paramView).requestDisallowInterceptTouchEvent(true);
                    }
                  }
                  if ((StickerPreviewViewer.this.currentStickerPreviewCell instanceof StickerCell))
                  {
                    StickerPreviewViewer.this.open(((StickerCell)StickerPreviewViewer.this.currentStickerPreviewCell).getSticker(), false);
                    ((StickerCell)StickerPreviewViewer.this.currentStickerPreviewCell).setScaled(true);
                    return;
                  }
                } while (!(StickerPreviewViewer.this.currentStickerPreviewCell instanceof ContextLinkCell));
                StickerPreviewViewer.this.open(((ContextLinkCell)StickerPreviewViewer.this.currentStickerPreviewCell).getDocument(), false);
                ((ContextLinkCell)StickerPreviewViewer.this.currentStickerPreviewCell).setScaled(true);
              }
            };
            AndroidUtilities.runOnUIThread(this.openStickerPreviewRunnable, 200L);
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public boolean onTouch(MotionEvent paramMotionEvent, final View paramView, int paramInt, final Object paramObject, StickerPreviewViewerDelegate paramStickerPreviewViewerDelegate)
  {
    this.delegate = paramStickerPreviewViewerDelegate;
    if ((this.openStickerPreviewRunnable != null) || (isVisible()))
    {
      if ((paramMotionEvent.getAction() != 1) && (paramMotionEvent.getAction() != 3) && (paramMotionEvent.getAction() != 6)) {
        break label178;
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if ((paramView instanceof AbsListView)) {
            ((AbsListView)paramView).setOnItemClickListener((AdapterView.OnItemClickListener)paramObject);
          }
          while (!(paramView instanceof RecyclerListView)) {
            return;
          }
          ((RecyclerListView)paramView).setOnItemClickListener((RecyclerListView.OnItemClickListener)paramObject);
        }
      }, 150L);
      if (this.openStickerPreviewRunnable == null) {
        break label83;
      }
      AndroidUtilities.cancelRunOnUIThread(this.openStickerPreviewRunnable);
      this.openStickerPreviewRunnable = null;
    }
    for (;;)
    {
      return false;
      label83:
      if (isVisible())
      {
        close();
        if (this.currentStickerPreviewCell != null)
        {
          if ((this.currentStickerPreviewCell instanceof StickerEmojiCell)) {
            ((StickerEmojiCell)this.currentStickerPreviewCell).setScaled(false);
          }
          for (;;)
          {
            this.currentStickerPreviewCell = null;
            break;
            if ((this.currentStickerPreviewCell instanceof StickerCell)) {
              ((StickerCell)this.currentStickerPreviewCell).setScaled(false);
            } else if ((this.currentStickerPreviewCell instanceof ContextLinkCell)) {
              ((ContextLinkCell)this.currentStickerPreviewCell).setScaled(false);
            }
          }
          label178:
          if (paramMotionEvent.getAction() != 0)
          {
            if (isVisible())
            {
              label307:
              boolean bool;
              if (paramMotionEvent.getAction() == 2)
              {
                int k = (int)paramMotionEvent.getX();
                int m = (int)paramMotionEvent.getY();
                int i = 0;
                int j;
                if ((paramView instanceof AbsListView))
                {
                  i = ((AbsListView)paramView).getChildCount();
                  j = 0;
                }
                for (;;)
                {
                  if (j < i)
                  {
                    paramMotionEvent = null;
                    if ((paramView instanceof AbsListView)) {
                      paramMotionEvent = ((AbsListView)paramView).getChildAt(j);
                    }
                    for (;;)
                    {
                      if (paramMotionEvent != null) {
                        break label307;
                      }
                      return false;
                      if (!(paramView instanceof RecyclerListView)) {
                        break;
                      }
                      i = ((RecyclerListView)paramView).getChildCount();
                      break;
                      if ((paramView instanceof RecyclerListView)) {
                        paramMotionEvent = ((RecyclerListView)paramView).getChildAt(j);
                      }
                    }
                    int n = paramMotionEvent.getTop();
                    int i1 = paramMotionEvent.getBottom();
                    int i2 = paramMotionEvent.getLeft();
                    int i3 = paramMotionEvent.getRight();
                    if ((n > m) || (i1 < m) || (i2 > k) || (i3 < k))
                    {
                      j += 1;
                    }
                    else
                    {
                      bool = false;
                      if (!(paramMotionEvent instanceof StickerEmojiCell)) {
                        break label396;
                      }
                      bool = true;
                    }
                  }
                }
              }
              while ((!bool) || (paramMotionEvent == this.currentStickerPreviewCell))
              {
                return true;
                label396:
                if ((paramMotionEvent instanceof StickerCell)) {
                  bool = true;
                } else if ((paramMotionEvent instanceof ContextLinkCell)) {
                  bool = ((ContextLinkCell)paramMotionEvent).isSticker();
                }
              }
              if ((this.currentStickerPreviewCell instanceof StickerEmojiCell))
              {
                ((StickerEmojiCell)this.currentStickerPreviewCell).setScaled(false);
                this.currentStickerPreviewCell = paramMotionEvent;
                setKeyboardHeight(paramInt);
                if (!(this.currentStickerPreviewCell instanceof StickerEmojiCell)) {
                  break label554;
                }
                open(((StickerEmojiCell)this.currentStickerPreviewCell).getSticker(), ((StickerEmojiCell)this.currentStickerPreviewCell).isRecent());
                ((StickerEmojiCell)this.currentStickerPreviewCell).setScaled(true);
              }
              for (;;)
              {
                return true;
                if ((this.currentStickerPreviewCell instanceof StickerCell))
                {
                  ((StickerCell)this.currentStickerPreviewCell).setScaled(false);
                  break;
                }
                if (!(this.currentStickerPreviewCell instanceof ContextLinkCell)) {
                  break;
                }
                ((ContextLinkCell)this.currentStickerPreviewCell).setScaled(false);
                break;
                label554:
                if ((this.currentStickerPreviewCell instanceof StickerCell))
                {
                  open(((StickerCell)this.currentStickerPreviewCell).getSticker(), false);
                  ((StickerCell)this.currentStickerPreviewCell).setScaled(true);
                }
                else if ((this.currentStickerPreviewCell instanceof ContextLinkCell))
                {
                  open(((ContextLinkCell)this.currentStickerPreviewCell).getDocument(), false);
                  ((ContextLinkCell)this.currentStickerPreviewCell).setScaled(true);
                }
              }
            }
            if (this.openStickerPreviewRunnable != null) {
              if (paramMotionEvent.getAction() == 2)
              {
                if (Math.hypot(this.startX - paramMotionEvent.getX(), this.startY - paramMotionEvent.getY()) > AndroidUtilities.dp(10.0F))
                {
                  AndroidUtilities.cancelRunOnUIThread(this.openStickerPreviewRunnable);
                  this.openStickerPreviewRunnable = null;
                }
              }
              else
              {
                AndroidUtilities.cancelRunOnUIThread(this.openStickerPreviewRunnable);
                this.openStickerPreviewRunnable = null;
              }
            }
          }
        }
      }
    }
  }
  
  public void open(TLRPC.Document paramDocument, boolean paramBoolean)
  {
    if ((this.parentActivity == null) || (paramDocument == null)) {}
    for (;;)
    {
      return;
      if (textPaint == null)
      {
        textPaint = new TextPaint(1);
        textPaint.setTextSize(AndroidUtilities.dp(24.0F));
      }
      ImageReceiver localImageReceiver = null;
      int i = 0;
      Object localObject = localImageReceiver;
      if (i < paramDocument.attributes.size())
      {
        localObject = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if (((localObject instanceof TLRPC.TL_documentAttributeSticker)) && (((TLRPC.DocumentAttribute)localObject).stickerset != null)) {
          localObject = ((TLRPC.DocumentAttribute)localObject).stickerset;
        }
      }
      else if (localObject == null) {}
      try
      {
        if (this.visibleDialog != null)
        {
          this.visibleDialog.setOnDismissListener(null);
          this.visibleDialog.dismiss();
          this.visibleDialog = null;
        }
        AndroidUtilities.cancelRunOnUIThread(this.showSheetRunnable);
        AndroidUtilities.runOnUIThread(this.showSheetRunnable, 1300L);
        this.currentSet = ((TLRPC.InputStickerSet)localObject);
        localImageReceiver = this.centerImage;
        if ((paramDocument != null) && (paramDocument.thumb != null))
        {
          localObject = paramDocument.thumb.location;
          localImageReceiver.setImage(paramDocument, null, (TLRPC.FileLocation)localObject, null, "webp", 1);
          this.stickerEmojiLayout = null;
          i = 0;
          if (i < paramDocument.attributes.size())
          {
            localObject = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
            if ((!(localObject instanceof TLRPC.TL_documentAttributeSticker)) || (TextUtils.isEmpty(((TLRPC.DocumentAttribute)localObject).alt))) {
              break label415;
            }
            this.stickerEmojiLayout = new StaticLayout(Emoji.replaceEmoji(((TLRPC.DocumentAttribute)localObject).alt, textPaint.getFontMetricsInt(), AndroidUtilities.dp(24.0F), false), textPaint, AndroidUtilities.dp(100.0F), Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, false);
          }
          this.currentSticker = paramDocument;
          this.containerView.invalidate();
          if (this.isVisible) {
            continue;
          }
          AndroidUtilities.lockOrientation(this.parentActivity);
        }
      }
      catch (Exception localException)
      {
        try
        {
          for (;;)
          {
            if (this.windowView.getParent() != null) {
              ((WindowManager)this.parentActivity.getSystemService("window")).removeView(this.windowView);
            }
            ((WindowManager)this.parentActivity.getSystemService("window")).addView(this.windowView, this.windowLayoutParams);
            this.isVisible = true;
            this.showProgress = 0.0F;
            this.lastUpdateTime = System.currentTimeMillis();
            return;
            i += 1;
            break;
            localException = localException;
            FileLog.e(localException);
            continue;
            localObject = null;
          }
          label415:
          i += 1;
        }
        catch (Exception paramDocument)
        {
          for (;;)
          {
            FileLog.e(paramDocument);
          }
        }
      }
    }
  }
  
  public void reset()
  {
    if (this.openStickerPreviewRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.openStickerPreviewRunnable);
      this.openStickerPreviewRunnable = null;
    }
    if (this.currentStickerPreviewCell != null)
    {
      if (!(this.currentStickerPreviewCell instanceof StickerEmojiCell)) {
        break label53;
      }
      ((StickerEmojiCell)this.currentStickerPreviewCell).setScaled(false);
    }
    for (;;)
    {
      this.currentStickerPreviewCell = null;
      return;
      label53:
      if ((this.currentStickerPreviewCell instanceof StickerCell)) {
        ((StickerCell)this.currentStickerPreviewCell).setScaled(false);
      } else if ((this.currentStickerPreviewCell instanceof ContextLinkCell)) {
        ((ContextLinkCell)this.currentStickerPreviewCell).setScaled(false);
      }
    }
  }
  
  public void setDelegate(StickerPreviewViewerDelegate paramStickerPreviewViewerDelegate)
  {
    this.delegate = paramStickerPreviewViewerDelegate;
  }
  
  public void setKeyboardHeight(int paramInt)
  {
    this.keyboardHeight = paramInt;
  }
  
  public void setParentActivity(Activity paramActivity)
  {
    this.currentAccount = UserConfig.selectedAccount;
    this.centerImage.setCurrentAccount(this.currentAccount);
    if (this.parentActivity == paramActivity) {
      return;
    }
    this.parentActivity = paramActivity;
    this.windowView = new FrameLayout(paramActivity);
    this.windowView.setFocusable(true);
    this.windowView.setFocusableInTouchMode(true);
    if (Build.VERSION.SDK_INT >= 23) {
      this.windowView.setFitsSystemWindows(true);
    }
    this.containerView = new FrameLayoutDrawer(paramActivity);
    this.containerView.setFocusable(false);
    this.windowView.addView(this.containerView, LayoutHelper.createFrame(-1, -1, 51));
    this.containerView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        if ((paramAnonymousMotionEvent.getAction() == 1) || (paramAnonymousMotionEvent.getAction() == 6) || (paramAnonymousMotionEvent.getAction() == 3)) {
          StickerPreviewViewer.this.close();
        }
        return true;
      }
    });
    this.windowLayoutParams = new WindowManager.LayoutParams();
    this.windowLayoutParams.height = -1;
    this.windowLayoutParams.format = -3;
    this.windowLayoutParams.width = -1;
    this.windowLayoutParams.gravity = 48;
    this.windowLayoutParams.type = 99;
    if (Build.VERSION.SDK_INT >= 21) {}
    for (this.windowLayoutParams.flags = -2147483640;; this.windowLayoutParams.flags = 8)
    {
      this.centerImage.setAspectFit(true);
      this.centerImage.setInvalidateAll(true);
      this.centerImage.setParentView(this.containerView);
      return;
    }
  }
  
  private class FrameLayoutDrawer
    extends FrameLayout
  {
    public FrameLayoutDrawer(Context paramContext)
    {
      super();
      setWillNotDraw(false);
    }
    
    protected void onDraw(Canvas paramCanvas)
    {
      StickerPreviewViewer.this.onDraw(paramCanvas);
    }
  }
  
  public static abstract interface StickerPreviewViewerDelegate
  {
    public abstract boolean needSend();
    
    public abstract void openSet(TLRPC.InputStickerSet paramInputStickerSet);
    
    public abstract void sendSticker(TLRPC.Document paramDocument);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/StickerPreviewViewer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */