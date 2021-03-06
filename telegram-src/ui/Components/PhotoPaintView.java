package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Looper;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.Frame.Builder;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.FaceDetector.Builder;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputDocument;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_maskCoords;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.OnDispatchKeyEventListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Paint.Brush;
import org.telegram.ui.Components.Paint.Brush.Elliptical;
import org.telegram.ui.Components.Paint.Brush.Neon;
import org.telegram.ui.Components.Paint.Brush.Radial;
import org.telegram.ui.Components.Paint.Painting;
import org.telegram.ui.Components.Paint.PhotoFace;
import org.telegram.ui.Components.Paint.RenderView;
import org.telegram.ui.Components.Paint.RenderView.RenderViewDelegate;
import org.telegram.ui.Components.Paint.Swatch;
import org.telegram.ui.Components.Paint.UndoStore;
import org.telegram.ui.Components.Paint.UndoStore.UndoStoreDelegate;
import org.telegram.ui.Components.Paint.Views.ColorPicker;
import org.telegram.ui.Components.Paint.Views.ColorPicker.ColorPickerDelegate;
import org.telegram.ui.Components.Paint.Views.EditTextOutline;
import org.telegram.ui.Components.Paint.Views.EntitiesContainerView;
import org.telegram.ui.Components.Paint.Views.EntitiesContainerView.EntitiesContainerViewDelegate;
import org.telegram.ui.Components.Paint.Views.EntityView;
import org.telegram.ui.Components.Paint.Views.EntityView.EntityViewDelegate;
import org.telegram.ui.Components.Paint.Views.StickerView;
import org.telegram.ui.Components.Paint.Views.TextPaintView;
import org.telegram.ui.PhotoViewer;

@SuppressLint({"NewApi"})
public class PhotoPaintView
  extends FrameLayout
  implements EntityView.EntityViewDelegate
{
  private static final int gallery_menu_done = 1;
  private Bitmap bitmapToEdit;
  private Brush[] brushes = { new Brush.Radial(), new Brush.Elliptical(), new Brush.Neon() };
  private TextView cancelTextView;
  private ColorPicker colorPicker;
  private Animator colorPickerAnimator;
  int currentBrush;
  private EntityView currentEntityView;
  private FrameLayout curtainView;
  private FrameLayout dimView;
  private TextView doneTextView;
  private Point editedTextPosition;
  private float editedTextRotation;
  private float editedTextScale;
  private boolean editingText;
  private EntitiesContainerView entitiesView;
  private ArrayList<PhotoFace> faces;
  private String initialText;
  private int orientation;
  private ImageView paintButton;
  private Size paintingSize;
  private boolean pickingSticker;
  private ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout;
  private Rect popupRect;
  private ActionBarPopupWindow popupWindow;
  private DispatchQueue queue = new DispatchQueue("Paint");
  private RenderView renderView;
  private boolean selectedStroke = true;
  private FrameLayout selectionContainerView;
  private StickerMasksView stickersView;
  private FrameLayout textDimView;
  private FrameLayout toolsView;
  private UndoStore undoStore;
  
  public PhotoPaintView(Context paramContext, Bitmap paramBitmap, int paramInt)
  {
    super(paramContext);
    this.bitmapToEdit = paramBitmap;
    this.orientation = paramInt;
    this.undoStore = new UndoStore();
    this.undoStore.setDelegate(new UndoStore.UndoStoreDelegate()
    {
      public void historyChanged()
      {
        PhotoPaintView.this.colorPicker.setUndoEnabled(PhotoPaintView.this.undoStore.canUndo());
      }
    });
    this.curtainView = new FrameLayout(paramContext);
    this.curtainView.setBackgroundColor(-16777216);
    this.curtainView.setVisibility(4);
    addView(this.curtainView);
    this.renderView = new RenderView(paramContext, new Painting(getPaintingSize()), paramBitmap, this.orientation);
    this.renderView.setDelegate(new RenderView.RenderViewDelegate()
    {
      public void onBeganDrawing()
      {
        if (PhotoPaintView.this.currentEntityView != null) {
          PhotoPaintView.this.selectEntity(null);
        }
      }
      
      public void onFinishedDrawing(boolean paramAnonymousBoolean)
      {
        PhotoPaintView.this.colorPicker.setUndoEnabled(PhotoPaintView.this.undoStore.canUndo());
      }
      
      public boolean shouldDraw()
      {
        if (PhotoPaintView.this.currentEntityView == null) {}
        for (boolean bool = true;; bool = false)
        {
          if (!bool) {
            PhotoPaintView.this.selectEntity(null);
          }
          return bool;
        }
      }
    });
    this.renderView.setUndoStore(this.undoStore);
    this.renderView.setQueue(this.queue);
    this.renderView.setVisibility(4);
    this.renderView.setBrush(this.brushes[0]);
    addView(this.renderView, LayoutHelper.createFrame(-1, -1, 51));
    this.entitiesView = new EntitiesContainerView(paramContext, new EntitiesContainerView.EntitiesContainerViewDelegate()
    {
      public void onEntityDeselect()
      {
        PhotoPaintView.this.selectEntity(null);
      }
      
      public EntityView onSelectedEntityRequest()
      {
        return PhotoPaintView.this.currentEntityView;
      }
      
      public boolean shouldReceiveTouches()
      {
        return PhotoPaintView.this.textDimView.getVisibility() != 0;
      }
    });
    this.entitiesView.setPivotX(0.0F);
    this.entitiesView.setPivotY(0.0F);
    addView(this.entitiesView);
    this.dimView = new FrameLayout(paramContext);
    this.dimView.setAlpha(0.0F);
    this.dimView.setBackgroundColor(1711276032);
    this.dimView.setVisibility(8);
    addView(this.dimView);
    this.textDimView = new FrameLayout(paramContext);
    this.textDimView.setAlpha(0.0F);
    this.textDimView.setBackgroundColor(1711276032);
    this.textDimView.setVisibility(8);
    this.textDimView.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.closeTextEnter(true);
      }
    });
    this.selectionContainerView = new FrameLayout(paramContext)
    {
      public boolean onTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return false;
      }
    };
    addView(this.selectionContainerView);
    this.colorPicker = new ColorPicker(paramContext);
    addView(this.colorPicker);
    this.colorPicker.setDelegate(new ColorPicker.ColorPickerDelegate()
    {
      public void onBeganColorPicking()
      {
        if (!(PhotoPaintView.this.currentEntityView instanceof TextPaintView)) {
          PhotoPaintView.this.setDimVisibility(true);
        }
      }
      
      public void onColorValueChanged()
      {
        PhotoPaintView.this.setCurrentSwatch(PhotoPaintView.this.colorPicker.getSwatch(), false);
      }
      
      public void onFinishedColorPicking()
      {
        PhotoPaintView.this.setCurrentSwatch(PhotoPaintView.this.colorPicker.getSwatch(), false);
        if (!(PhotoPaintView.this.currentEntityView instanceof TextPaintView)) {
          PhotoPaintView.this.setDimVisibility(false);
        }
      }
      
      public void onSettingsPressed()
      {
        if (PhotoPaintView.this.currentEntityView != null)
        {
          if ((PhotoPaintView.this.currentEntityView instanceof StickerView)) {
            PhotoPaintView.this.mirrorSticker();
          }
          while (!(PhotoPaintView.this.currentEntityView instanceof TextPaintView)) {
            return;
          }
          PhotoPaintView.this.showTextSettings();
          return;
        }
        PhotoPaintView.this.showBrushSettings();
      }
      
      public void onUndoPressed()
      {
        PhotoPaintView.this.undoStore.undo();
      }
    });
    this.toolsView = new FrameLayout(paramContext);
    this.toolsView.setBackgroundColor(-16777216);
    addView(this.toolsView, LayoutHelper.createFrame(-1, 48, 83));
    this.cancelTextView = new TextView(paramContext);
    this.cancelTextView.setTextSize(1, 14.0F);
    this.cancelTextView.setTextColor(-1);
    this.cancelTextView.setGravity(17);
    this.cancelTextView.setBackgroundDrawable(Theme.createSelectorDrawable(-12763843, 0));
    this.cancelTextView.setPadding(AndroidUtilities.dp(20.0F), 0, AndroidUtilities.dp(20.0F), 0);
    this.cancelTextView.setText(LocaleController.getString("Cancel", 2131493127).toUpperCase());
    this.cancelTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.toolsView.addView(this.cancelTextView, LayoutHelper.createFrame(-2, -1, 51));
    this.doneTextView = new TextView(paramContext);
    this.doneTextView.setTextSize(1, 14.0F);
    this.doneTextView.setTextColor(-11420173);
    this.doneTextView.setGravity(17);
    this.doneTextView.setBackgroundDrawable(Theme.createSelectorDrawable(-12763843, 0));
    this.doneTextView.setPadding(AndroidUtilities.dp(20.0F), 0, AndroidUtilities.dp(20.0F), 0);
    this.doneTextView.setText(LocaleController.getString("Done", 2131493395).toUpperCase());
    this.doneTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.toolsView.addView(this.doneTextView, LayoutHelper.createFrame(-2, -1, 53));
    this.paintButton = new ImageView(paramContext);
    this.paintButton.setScaleType(ImageView.ScaleType.CENTER);
    this.paintButton.setImageResource(2131165581);
    this.paintButton.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.toolsView.addView(this.paintButton, LayoutHelper.createFrame(54, -1.0F, 17, 0.0F, 0.0F, 56.0F, 0.0F));
    this.paintButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.selectEntity(null);
      }
    });
    paramBitmap = new ImageView(paramContext);
    paramBitmap.setScaleType(ImageView.ScaleType.CENTER);
    paramBitmap.setImageResource(2131165585);
    paramBitmap.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.toolsView.addView(paramBitmap, LayoutHelper.createFrame(54, -1, 17));
    paramBitmap.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.openStickersView();
      }
    });
    paramContext = new ImageView(paramContext);
    paramContext.setScaleType(ImageView.ScaleType.CENTER);
    paramContext.setImageResource(2131165583);
    paramContext.setBackgroundDrawable(Theme.createSelectorDrawable(1090519039));
    this.toolsView.addView(paramContext, LayoutHelper.createFrame(54, -1.0F, 17, 56.0F, 0.0F, 0.0F, 0.0F));
    paramContext.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.createText();
      }
    });
    this.colorPicker.setUndoEnabled(false);
    setCurrentSwatch(this.colorPicker.getSwatch(), false);
    updateSettingsButton();
  }
  
  private int baseFontSize()
  {
    return (int)(getPaintingSize().width / 9.0F);
  }
  
  private Size baseStickerSize()
  {
    float f = (float)Math.floor(getPaintingSize().width * 0.5D);
    return new Size(f, f);
  }
  
  private FrameLayout buttonForBrush(final int paramInt1, int paramInt2, boolean paramBoolean)
  {
    FrameLayout localFrameLayout = new FrameLayout(getContext());
    localFrameLayout.setBackgroundDrawable(Theme.getSelectorDrawable(false));
    localFrameLayout.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.setBrush(paramInt1);
        if ((PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
          PhotoPaintView.this.popupWindow.dismiss(true);
        }
      }
    });
    ImageView localImageView = new ImageView(getContext());
    localImageView.setImageResource(paramInt2);
    localFrameLayout.addView(localImageView, LayoutHelper.createFrame(165, 44.0F, 19, 46.0F, 0.0F, 8.0F, 0.0F));
    if (paramBoolean)
    {
      localImageView = new ImageView(getContext());
      localImageView.setImageResource(2131165349);
      localImageView.setScaleType(ImageView.ScaleType.CENTER);
      localFrameLayout.addView(localImageView, LayoutHelper.createFrame(50, -1.0F));
    }
    return localFrameLayout;
  }
  
  private FrameLayout buttonForText(final boolean paramBoolean1, String paramString, boolean paramBoolean2)
  {
    int j = -16777216;
    FrameLayout local20 = new FrameLayout(getContext())
    {
      public boolean onInterceptTouchEvent(MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    };
    local20.setBackgroundDrawable(Theme.getSelectorDrawable(false));
    local20.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        PhotoPaintView.this.setStroke(paramBoolean1);
        if ((PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
          PhotoPaintView.this.popupWindow.dismiss(true);
        }
      }
    });
    EditTextOutline localEditTextOutline = new EditTextOutline(getContext());
    localEditTextOutline.setBackgroundColor(0);
    localEditTextOutline.setEnabled(false);
    localEditTextOutline.setStrokeWidth(AndroidUtilities.dp(3.0F));
    if (paramBoolean1)
    {
      i = -1;
      localEditTextOutline.setTextColor(i);
      if (!paramBoolean1) {
        break label232;
      }
    }
    label232:
    for (int i = j;; i = 0)
    {
      localEditTextOutline.setStrokeColor(i);
      localEditTextOutline.setPadding(AndroidUtilities.dp(2.0F), 0, AndroidUtilities.dp(2.0F), 0);
      localEditTextOutline.setTextSize(1, 18.0F);
      localEditTextOutline.setTypeface(null, 1);
      localEditTextOutline.setTag(Boolean.valueOf(paramBoolean1));
      localEditTextOutline.setText(paramString);
      local20.addView(localEditTextOutline, LayoutHelper.createFrame(-2, -2.0F, 19, 46.0F, 0.0F, 16.0F, 0.0F));
      if (paramBoolean2)
      {
        paramString = new ImageView(getContext());
        paramString.setImageResource(2131165349);
        paramString.setScaleType(ImageView.ScaleType.CENTER);
        local20.addView(paramString, LayoutHelper.createFrame(50, -1.0F));
      }
      return local20;
      i = -16777216;
      break;
    }
  }
  
  private StickerPosition calculateStickerPosition(TLRPC.Document paramDocument)
  {
    Object localObject2 = null;
    int i = 0;
    Object localObject1 = localObject2;
    if (i < paramDocument.attributes.size())
    {
      localObject1 = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
      if ((localObject1 instanceof TLRPC.TL_documentAttributeSticker)) {
        localObject1 = ((TLRPC.DocumentAttribute)localObject1).mask_coords;
      }
    }
    else
    {
      localObject2 = new StickerPosition(centerPositionForEntity(), 0.75F, 0.0F);
      if ((localObject1 != null) && (this.faces != null) && (this.faces.size() != 0)) {
        break label103;
      }
    }
    label103:
    do
    {
      return (StickerPosition)localObject2;
      i += 1;
      break;
      i = ((TLRPC.TL_maskCoords)localObject1).n;
      paramDocument = getRandomFaceWithVacantAnchor(i, paramDocument.id, (TLRPC.TL_maskCoords)localObject1);
    } while (paramDocument == null);
    localObject2 = paramDocument.getPointForAnchor(i);
    float f6 = paramDocument.getWidthForAnchor(i);
    float f1 = paramDocument.getAngle();
    float f2 = (float)(f6 / baseStickerSize().width * ((TLRPC.TL_maskCoords)localObject1).zoom);
    float f7 = (float)Math.toRadians(f1);
    float f3 = (float)(Math.sin(1.5707963267948966D - f7) * f6 * ((TLRPC.TL_maskCoords)localObject1).x);
    float f4 = (float)(Math.cos(1.5707963267948966D - f7) * f6 * ((TLRPC.TL_maskCoords)localObject1).x);
    float f5 = (float)(Math.cos(1.5707963267948966D + f7) * f6 * ((TLRPC.TL_maskCoords)localObject1).y);
    f6 = (float)(Math.sin(1.5707963267948966D + f7) * f6 * ((TLRPC.TL_maskCoords)localObject1).y);
    return new StickerPosition(new Point(((Point)localObject2).x + f3 + f5, ((Point)localObject2).y + f4 + f6), f2, f1);
  }
  
  private Point centerPositionForEntity()
  {
    Size localSize = getPaintingSize();
    return new Point(localSize.width / 2.0F, localSize.height / 2.0F);
  }
  
  private void closeStickersView()
  {
    if ((this.stickersView == null) || (this.stickersView.getVisibility() != 0)) {
      return;
    }
    this.pickingSticker = false;
    ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(this.stickersView, "alpha", new float[] { 1.0F, 0.0F });
    localObjectAnimator.setDuration(200L);
    localObjectAnimator.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        PhotoPaintView.this.stickersView.setVisibility(8);
      }
    });
    localObjectAnimator.start();
  }
  
  private void createSticker(TLRPC.Document paramDocument)
  {
    StickerPosition localStickerPosition = calculateStickerPosition(paramDocument);
    paramDocument = new StickerView(getContext(), localStickerPosition.position, localStickerPosition.angle, localStickerPosition.scale, baseStickerSize(), paramDocument);
    paramDocument.setDelegate(this);
    this.entitiesView.addView(paramDocument);
    registerRemovalUndo(paramDocument);
    selectEntity(paramDocument);
  }
  
  private void createText()
  {
    Swatch localSwatch = this.colorPicker.getSwatch();
    Object localObject = new Swatch(-1, 1.0F, localSwatch.brushWeight);
    localSwatch = new Swatch(-16777216, 0.85F, localSwatch.brushWeight);
    if (this.selectedStroke) {
      localObject = localSwatch;
    }
    for (;;)
    {
      setCurrentSwatch((Swatch)localObject, true);
      localObject = new TextPaintView(getContext(), startPositionRelativeToEntity(null), baseFontSize(), "", this.colorPicker.getSwatch(), this.selectedStroke);
      ((TextPaintView)localObject).setDelegate(this);
      ((TextPaintView)localObject).setMaxWidth((int)(getPaintingSize().width - 20.0F));
      this.entitiesView.addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F));
      registerRemovalUndo((EntityView)localObject);
      selectEntity((EntityView)localObject);
      editSelectedTextEntity();
      return;
    }
  }
  
  private void detectFaces()
  {
    this.queue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject3 = null;
        Object localObject1 = null;
        try
        {
          localFaceDetector = new FaceDetector.Builder(PhotoPaintView.this.getContext()).setMode(1).setLandmarkType(1).setTrackingEnabled(false).build();
          localObject1 = localFaceDetector;
          localObject3 = localFaceDetector;
          if (!localFaceDetector.isOperational())
          {
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            if (BuildVars.LOGS_ENABLED)
            {
              localObject1 = localFaceDetector;
              localObject3 = localFaceDetector;
              FileLog.e("face detection is not operational");
            }
            if (localFaceDetector != null) {
              localFaceDetector.release();
            }
            return;
          }
        }
        catch (Exception localException)
        {
          FaceDetector localFaceDetector;
          Object localObject4;
          localObject3 = localObject1;
          FileLog.e(localException);
          return;
        }
        finally
        {
          if (localObject3 == null) {
            break label337;
          }
          ((FaceDetector)localObject3).release();
        }
        localObject1 = localFaceDetector;
        localObject3 = localFaceDetector;
        localObject4 = new Frame.Builder().setBitmap(PhotoPaintView.this.bitmapToEdit).setRotation(PhotoPaintView.this.getFrameRotation()).build();
        localObject1 = localFaceDetector;
        localObject3 = localFaceDetector;
        for (;;)
        {
          ArrayList localArrayList;
          int i;
          try
          {
            localObject4 = localFaceDetector.detect((Frame)localObject4);
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            localArrayList = new ArrayList();
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            Size localSize = PhotoPaintView.this.getPaintingSize();
            i = 0;
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            if (i >= ((SparseArray)localObject4).size()) {
              break label283;
            }
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            PhotoFace localPhotoFace = new PhotoFace((Face)((SparseArray)localObject4).get(((SparseArray)localObject4).keyAt(i)), PhotoPaintView.this.bitmapToEdit, localSize, PhotoPaintView.this.isSidewardOrientation());
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            if (!localPhotoFace.isSufficient()) {
              break label339;
            }
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            localArrayList.add(localPhotoFace);
          }
          catch (Throwable localThrowable)
          {
            localObject1 = localFaceDetector;
            localObject3 = localFaceDetector;
            FileLog.e(localThrowable);
          }
          if (localFaceDetector == null) {
            break;
          }
          localFaceDetector.release();
          return;
          label283:
          localObject1 = localFaceDetector;
          localObject3 = localFaceDetector;
          PhotoPaintView.access$3402(PhotoPaintView.this, localArrayList);
          if (localFaceDetector == null) {
            break;
          }
          localFaceDetector.release();
          return;
          label337:
          label339:
          i += 1;
        }
      }
    });
  }
  
  private void duplicateSelectedEntity()
  {
    if (this.currentEntityView == null) {
      return;
    }
    Object localObject = null;
    Point localPoint = startPositionRelativeToEntity(this.currentEntityView);
    if ((this.currentEntityView instanceof StickerView))
    {
      localObject = new StickerView(getContext(), (StickerView)this.currentEntityView, localPoint);
      ((StickerView)localObject).setDelegate(this);
      this.entitiesView.addView((View)localObject);
    }
    for (;;)
    {
      registerRemovalUndo((EntityView)localObject);
      selectEntity((EntityView)localObject);
      updateSettingsButton();
      return;
      if ((this.currentEntityView instanceof TextPaintView))
      {
        localObject = new TextPaintView(getContext(), (TextPaintView)this.currentEntityView, localPoint);
        ((TextPaintView)localObject).setDelegate(this);
        ((TextPaintView)localObject).setMaxWidth((int)(getPaintingSize().width - 20.0F));
        this.entitiesView.addView((View)localObject, LayoutHelper.createFrame(-2, -2.0F));
      }
    }
  }
  
  private void editSelectedTextEntity()
  {
    if ((!(this.currentEntityView instanceof TextPaintView)) || (this.editingText)) {
      return;
    }
    this.curtainView.setVisibility(0);
    TextPaintView localTextPaintView = (TextPaintView)this.currentEntityView;
    this.initialText = localTextPaintView.getText();
    this.editingText = true;
    this.editedTextPosition = localTextPaintView.getPosition();
    this.editedTextRotation = localTextPaintView.getRotation();
    this.editedTextScale = localTextPaintView.getScale();
    localTextPaintView.setPosition(centerPositionForEntity());
    localTextPaintView.setRotation(0.0F);
    localTextPaintView.setScale(1.0F);
    this.toolsView.setVisibility(8);
    setTextDimVisibility(true, localTextPaintView);
    localTextPaintView.beginEditing();
    ((InputMethodManager)ApplicationLoader.applicationContext.getSystemService("input_method")).toggleSoftInputFromWindow(localTextPaintView.getFocusedView().getWindowToken(), 2, 0);
  }
  
  private int getFrameRotation()
  {
    switch (this.orientation)
    {
    default: 
      return 0;
    case 90: 
      return 1;
    case 180: 
      return 2;
    }
    return 3;
  }
  
  private Size getPaintingSize()
  {
    if (this.paintingSize != null) {
      return this.paintingSize;
    }
    float f1;
    if (isSidewardOrientation())
    {
      f1 = this.bitmapToEdit.getHeight();
      if (!isSidewardOrientation()) {
        break label132;
      }
    }
    label132:
    for (float f2 = this.bitmapToEdit.getWidth();; f2 = this.bitmapToEdit.getHeight())
    {
      Size localSize = new Size(f1, f2);
      localSize.width = 1280.0F;
      localSize.height = ((float)Math.floor(localSize.width * f2 / f1));
      if (localSize.height > 1280.0F)
      {
        localSize.height = 1280.0F;
        localSize.width = ((float)Math.floor(localSize.height * f1 / f2));
      }
      this.paintingSize = localSize;
      return localSize;
      f1 = this.bitmapToEdit.getWidth();
      break;
    }
  }
  
  private PhotoFace getRandomFaceWithVacantAnchor(int paramInt, long paramLong, TLRPC.TL_maskCoords paramTL_maskCoords)
  {
    Object localObject;
    if ((paramInt < 0) || (paramInt > 3) || (this.faces.isEmpty()))
    {
      localObject = null;
      return (PhotoFace)localObject;
    }
    int k = this.faces.size();
    int j = Utilities.random.nextInt(k);
    int i = k;
    for (;;)
    {
      if (i <= 0) {
        break label102;
      }
      PhotoFace localPhotoFace = (PhotoFace)this.faces.get(j);
      localObject = localPhotoFace;
      if (!isFaceAnchorOccupied(localPhotoFace, paramInt, paramLong, paramTL_maskCoords)) {
        break;
      }
      j = (j + 1) % k;
      i -= 1;
    }
    label102:
    return null;
  }
  
  private boolean hasChanges()
  {
    return (this.undoStore.canUndo()) || (this.entitiesView.entitiesCount() > 0);
  }
  
  private boolean isFaceAnchorOccupied(PhotoFace paramPhotoFace, int paramInt, long paramLong, TLRPC.TL_maskCoords paramTL_maskCoords)
  {
    paramTL_maskCoords = paramPhotoFace.getPointForAnchor(paramInt);
    if (paramTL_maskCoords == null) {
      return true;
    }
    float f1 = paramPhotoFace.getWidthForAnchor(0);
    int i = 0;
    if (i < this.entitiesView.getChildCount())
    {
      paramPhotoFace = this.entitiesView.getChildAt(i);
      if (!(paramPhotoFace instanceof StickerView)) {}
      float f2;
      do
      {
        do
        {
          i += 1;
          break;
          paramPhotoFace = (StickerView)paramPhotoFace;
        } while (paramPhotoFace.getAnchor() != paramInt);
        Point localPoint = paramPhotoFace.getPosition();
        f2 = (float)Math.hypot(localPoint.x - paramTL_maskCoords.x, localPoint.y - paramTL_maskCoords.y);
      } while (((paramLong != paramPhotoFace.getSticker().id) && (this.faces.size() <= 1)) || (f2 >= f1 * 1.1F));
      return true;
    }
    return false;
  }
  
  private boolean isSidewardOrientation()
  {
    return (this.orientation % 360 == 90) || (this.orientation % 360 == 270);
  }
  
  private void mirrorSticker()
  {
    if ((this.currentEntityView instanceof StickerView)) {
      ((StickerView)this.currentEntityView).mirror();
    }
  }
  
  private void openStickersView()
  {
    if ((this.stickersView != null) && (this.stickersView.getVisibility() == 0)) {
      return;
    }
    this.pickingSticker = true;
    if (this.stickersView == null)
    {
      this.stickersView = new StickerMasksView(getContext());
      this.stickersView.setListener(new StickerMasksView.Listener()
      {
        public void onStickerSelected(TLRPC.Document paramAnonymousDocument)
        {
          PhotoPaintView.this.closeStickersView();
          PhotoPaintView.this.createSticker(paramAnonymousDocument);
        }
        
        public void onTypeChanged() {}
      });
      addView(this.stickersView, LayoutHelper.createFrame(-1, -1, 51));
    }
    this.stickersView.setVisibility(0);
    ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(this.stickersView, "alpha", new float[] { 0.0F, 1.0F });
    localObjectAnimator.setDuration(200L);
    localObjectAnimator.start();
  }
  
  private void registerRemovalUndo(final EntityView paramEntityView)
  {
    this.undoStore.registerUndo(paramEntityView.getUUID(), new Runnable()
    {
      public void run()
      {
        PhotoPaintView.this.removeEntity(paramEntityView);
      }
    });
  }
  
  private void removeEntity(EntityView paramEntityView)
  {
    if (paramEntityView == this.currentEntityView)
    {
      this.currentEntityView.deselect();
      if (this.editingText) {
        closeTextEnter(false);
      }
      this.currentEntityView = null;
      updateSettingsButton();
    }
    this.entitiesView.removeView(paramEntityView);
    this.undoStore.unregisterUndo(paramEntityView.getUUID());
  }
  
  private boolean selectEntity(EntityView paramEntityView)
  {
    boolean bool = false;
    if (this.currentEntityView != null)
    {
      if (this.currentEntityView == paramEntityView)
      {
        if (!this.editingText) {
          showMenuForEntity(this.currentEntityView);
        }
        return true;
      }
      this.currentEntityView.deselect();
      bool = true;
    }
    this.currentEntityView = paramEntityView;
    if (this.currentEntityView != null)
    {
      this.currentEntityView.select(this.selectionContainerView);
      this.entitiesView.bringViewToFront(this.currentEntityView);
      if ((this.currentEntityView instanceof TextPaintView)) {
        setCurrentSwatch(((TextPaintView)this.currentEntityView).getSwatch(), true);
      }
      bool = true;
    }
    updateSettingsButton();
    return bool;
  }
  
  private void setBrush(int paramInt)
  {
    RenderView localRenderView = this.renderView;
    Brush[] arrayOfBrush = this.brushes;
    this.currentBrush = paramInt;
    localRenderView.setBrush(arrayOfBrush[paramInt]);
  }
  
  private void setCurrentSwatch(Swatch paramSwatch, boolean paramBoolean)
  {
    this.renderView.setColor(paramSwatch.color);
    this.renderView.setBrushSize(paramSwatch.brushWeight);
    if (paramBoolean) {
      this.colorPicker.setSwatch(paramSwatch);
    }
    if ((this.currentEntityView instanceof TextPaintView)) {
      ((TextPaintView)this.currentEntityView).setSwatch(paramSwatch);
    }
  }
  
  private void setDimVisibility(final boolean paramBoolean)
  {
    if (paramBoolean) {
      this.dimView.setVisibility(0);
    }
    for (ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(this.dimView, "alpha", new float[] { 0.0F, 1.0F });; localObjectAnimator = ObjectAnimator.ofFloat(this.dimView, "alpha", new float[] { 1.0F, 0.0F }))
    {
      localObjectAnimator.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (!paramBoolean) {
            PhotoPaintView.this.dimView.setVisibility(8);
          }
        }
      });
      localObjectAnimator.setDuration(200L);
      localObjectAnimator.start();
      return;
    }
  }
  
  private void setStroke(boolean paramBoolean)
  {
    this.selectedStroke = paramBoolean;
    Swatch localSwatch;
    if ((this.currentEntityView instanceof TextPaintView))
    {
      localSwatch = this.colorPicker.getSwatch();
      if ((!paramBoolean) || (localSwatch.color != -1)) {
        break label68;
      }
      setCurrentSwatch(new Swatch(-16777216, 0.85F, localSwatch.brushWeight), true);
    }
    for (;;)
    {
      ((TextPaintView)this.currentEntityView).setStroke(paramBoolean);
      return;
      label68:
      if ((!paramBoolean) && (localSwatch.color == -16777216)) {
        setCurrentSwatch(new Swatch(-1, 1.0F, localSwatch.brushWeight), true);
      }
    }
  }
  
  private void setTextDimVisibility(final boolean paramBoolean, EntityView paramEntityView)
  {
    if ((paramBoolean) && (paramEntityView != null))
    {
      ViewGroup localViewGroup = (ViewGroup)paramEntityView.getParent();
      if (this.textDimView.getParent() != null) {
        ((EntitiesContainerView)this.textDimView.getParent()).removeView(this.textDimView);
      }
      localViewGroup.addView(this.textDimView, localViewGroup.indexOfChild(paramEntityView));
    }
    boolean bool;
    if (!paramBoolean)
    {
      bool = true;
      paramEntityView.setSelectionVisibility(bool);
      if (!paramBoolean) {
        break label135;
      }
      this.textDimView.setVisibility(0);
    }
    label135:
    for (paramEntityView = ObjectAnimator.ofFloat(this.textDimView, "alpha", new float[] { 0.0F, 1.0F });; paramEntityView = ObjectAnimator.ofFloat(this.textDimView, "alpha", new float[] { 1.0F, 0.0F }))
    {
      paramEntityView.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if (!paramBoolean)
          {
            PhotoPaintView.this.textDimView.setVisibility(8);
            if (PhotoPaintView.this.textDimView.getParent() != null) {
              ((EntitiesContainerView)PhotoPaintView.this.textDimView.getParent()).removeView(PhotoPaintView.this.textDimView);
            }
          }
        }
      });
      paramEntityView.setDuration(200L);
      paramEntityView.start();
      return;
      bool = false;
      break;
    }
  }
  
  private void showBrushSettings()
  {
    showPopup(new Runnable()
    {
      public void run()
      {
        boolean bool2 = true;
        Object localObject = PhotoPaintView.this;
        LinearLayout.LayoutParams localLayoutParams;
        if (PhotoPaintView.this.currentBrush == 0)
        {
          bool1 = true;
          localObject = ((PhotoPaintView)localObject).buttonForBrush(0, 2131165570, bool1);
          PhotoPaintView.this.popupLayout.addView((View)localObject);
          localLayoutParams = (LinearLayout.LayoutParams)((View)localObject).getLayoutParams();
          localLayoutParams.width = -1;
          localLayoutParams.height = AndroidUtilities.dp(52.0F);
          ((View)localObject).setLayoutParams(localLayoutParams);
          localObject = PhotoPaintView.this;
          if (PhotoPaintView.this.currentBrush != 1) {
            break label214;
          }
          bool1 = true;
          label88:
          localObject = ((PhotoPaintView)localObject).buttonForBrush(1, 2131165566, bool1);
          PhotoPaintView.this.popupLayout.addView((View)localObject);
          localLayoutParams = (LinearLayout.LayoutParams)((View)localObject).getLayoutParams();
          localLayoutParams.width = -1;
          localLayoutParams.height = AndroidUtilities.dp(52.0F);
          ((View)localObject).setLayoutParams(localLayoutParams);
          localObject = PhotoPaintView.this;
          if (PhotoPaintView.this.currentBrush != 2) {
            break label219;
          }
        }
        label214:
        label219:
        for (boolean bool1 = bool2;; bool1 = false)
        {
          localObject = ((PhotoPaintView)localObject).buttonForBrush(2, 2131165568, bool1);
          PhotoPaintView.this.popupLayout.addView((View)localObject);
          localLayoutParams = (LinearLayout.LayoutParams)((View)localObject).getLayoutParams();
          localLayoutParams.width = -1;
          localLayoutParams.height = AndroidUtilities.dp(52.0F);
          ((View)localObject).setLayoutParams(localLayoutParams);
          return;
          bool1 = false;
          break;
          bool1 = false;
          break label88;
        }
      }
    }, this, 85, 0, AndroidUtilities.dp(48.0F));
  }
  
  private void showMenuForEntity(final EntityView paramEntityView)
  {
    int i = (int)((paramEntityView.getPosition().x - this.entitiesView.getWidth() / 2) * this.entitiesView.getScaleX());
    int j = (int)((paramEntityView.getPosition().y - paramEntityView.getHeight() * paramEntityView.getScale() / 2.0F - this.entitiesView.getHeight() / 2) * this.entitiesView.getScaleY());
    int k = AndroidUtilities.dp(32.0F);
    showPopup(new Runnable()
    {
      public void run()
      {
        LinearLayout localLinearLayout = new LinearLayout(PhotoPaintView.this.getContext());
        localLinearLayout.setOrientation(0);
        Object localObject = new TextView(PhotoPaintView.this.getContext());
        ((TextView)localObject).setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
        ((TextView)localObject).setBackgroundDrawable(Theme.getSelectorDrawable(false));
        ((TextView)localObject).setGravity(16);
        ((TextView)localObject).setPadding(AndroidUtilities.dp(16.0F), 0, AndroidUtilities.dp(14.0F), 0);
        ((TextView)localObject).setTextSize(1, 18.0F);
        ((TextView)localObject).setTag(Integer.valueOf(0));
        ((TextView)localObject).setText(LocaleController.getString("PaintDelete", 2131494062));
        ((TextView)localObject).setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymous2View)
          {
            PhotoPaintView.this.removeEntity(PhotoPaintView.17.this.val$entityView);
            if ((PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
              PhotoPaintView.this.popupWindow.dismiss(true);
            }
          }
        });
        localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, 48));
        if ((paramEntityView instanceof TextPaintView))
        {
          localObject = new TextView(PhotoPaintView.this.getContext());
          ((TextView)localObject).setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
          ((TextView)localObject).setBackgroundDrawable(Theme.getSelectorDrawable(false));
          ((TextView)localObject).setGravity(16);
          ((TextView)localObject).setPadding(AndroidUtilities.dp(16.0F), 0, AndroidUtilities.dp(16.0F), 0);
          ((TextView)localObject).setTextSize(1, 18.0F);
          ((TextView)localObject).setTag(Integer.valueOf(1));
          ((TextView)localObject).setText(LocaleController.getString("PaintEdit", 2131494064));
          ((TextView)localObject).setOnClickListener(new View.OnClickListener()
          {
            public void onClick(View paramAnonymous2View)
            {
              PhotoPaintView.this.editSelectedTextEntity();
              if ((PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
                PhotoPaintView.this.popupWindow.dismiss(true);
              }
            }
          });
          localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, 48));
        }
        localObject = new TextView(PhotoPaintView.this.getContext());
        ((TextView)localObject).setTextColor(Theme.getColor("actionBarDefaultSubmenuItem"));
        ((TextView)localObject).setBackgroundDrawable(Theme.getSelectorDrawable(false));
        ((TextView)localObject).setGravity(16);
        ((TextView)localObject).setPadding(AndroidUtilities.dp(14.0F), 0, AndroidUtilities.dp(16.0F), 0);
        ((TextView)localObject).setTextSize(1, 18.0F);
        ((TextView)localObject).setTag(Integer.valueOf(2));
        ((TextView)localObject).setText(LocaleController.getString("PaintDuplicate", 2131494063));
        ((TextView)localObject).setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View paramAnonymous2View)
          {
            PhotoPaintView.this.duplicateSelectedEntity();
            if ((PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
              PhotoPaintView.this.popupWindow.dismiss(true);
            }
          }
        });
        localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, 48));
        PhotoPaintView.this.popupLayout.addView(localLinearLayout);
        localObject = (LinearLayout.LayoutParams)localLinearLayout.getLayoutParams();
        ((LinearLayout.LayoutParams)localObject).width = -2;
        ((LinearLayout.LayoutParams)localObject).height = -2;
        localLinearLayout.setLayoutParams((ViewGroup.LayoutParams)localObject);
      }
    }, paramEntityView, 17, i, j - k);
  }
  
  private void showPopup(Runnable paramRunnable, View paramView, int paramInt1, int paramInt2, int paramInt3)
  {
    if ((this.popupWindow != null) && (this.popupWindow.isShowing()))
    {
      this.popupWindow.dismiss();
      return;
    }
    if (this.popupLayout == null)
    {
      this.popupRect = new Rect();
      this.popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext());
      this.popupLayout.setAnimationEnabled(false);
      this.popupLayout.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          if ((paramAnonymousMotionEvent.getActionMasked() == 0) && (PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing()))
          {
            paramAnonymousView.getHitRect(PhotoPaintView.this.popupRect);
            if (!PhotoPaintView.this.popupRect.contains((int)paramAnonymousMotionEvent.getX(), (int)paramAnonymousMotionEvent.getY())) {
              PhotoPaintView.this.popupWindow.dismiss();
            }
          }
          return false;
        }
      });
      this.popupLayout.setDispatchKeyEventListener(new ActionBarPopupWindow.OnDispatchKeyEventListener()
      {
        public void onDispatchKeyEvent(KeyEvent paramAnonymousKeyEvent)
        {
          if ((paramAnonymousKeyEvent.getKeyCode() == 4) && (paramAnonymousKeyEvent.getRepeatCount() == 0) && (PhotoPaintView.this.popupWindow != null) && (PhotoPaintView.this.popupWindow.isShowing())) {
            PhotoPaintView.this.popupWindow.dismiss();
          }
        }
      });
      this.popupLayout.setShowedFromBotton(true);
    }
    this.popupLayout.removeInnerViews();
    paramRunnable.run();
    if (this.popupWindow == null)
    {
      this.popupWindow = new ActionBarPopupWindow(this.popupLayout, -2, -2);
      this.popupWindow.setAnimationEnabled(false);
      this.popupWindow.setAnimationStyle(2131558406);
      this.popupWindow.setOutsideTouchable(true);
      this.popupWindow.setClippingEnabled(true);
      this.popupWindow.setInputMethodMode(2);
      this.popupWindow.setSoftInputMode(0);
      this.popupWindow.getContentView().setFocusableInTouchMode(true);
      this.popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
      {
        public void onDismiss()
        {
          PhotoPaintView.this.popupLayout.removeInnerViews();
        }
      });
    }
    this.popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0F), Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000.0F), Integer.MIN_VALUE));
    this.popupWindow.setFocusable(true);
    this.popupWindow.showAtLocation(paramView, paramInt1, paramInt2, paramInt3);
    this.popupWindow.startAnimation();
  }
  
  private void showTextSettings()
  {
    showPopup(new Runnable()
    {
      public void run()
      {
        boolean bool = true;
        Object localObject1 = PhotoPaintView.this.buttonForText(true, LocaleController.getString("PaintOutlined", 2131494065), PhotoPaintView.this.selectedStroke);
        PhotoPaintView.this.popupLayout.addView((View)localObject1);
        Object localObject2 = (LinearLayout.LayoutParams)((View)localObject1).getLayoutParams();
        ((LinearLayout.LayoutParams)localObject2).width = -1;
        ((LinearLayout.LayoutParams)localObject2).height = AndroidUtilities.dp(48.0F);
        ((View)localObject1).setLayoutParams((ViewGroup.LayoutParams)localObject2);
        localObject1 = PhotoPaintView.this;
        localObject2 = LocaleController.getString("PaintRegular", 2131494066);
        if (!PhotoPaintView.this.selectedStroke) {}
        for (;;)
        {
          localObject1 = ((PhotoPaintView)localObject1).buttonForText(false, (String)localObject2, bool);
          PhotoPaintView.this.popupLayout.addView((View)localObject1);
          localObject2 = (LinearLayout.LayoutParams)((View)localObject1).getLayoutParams();
          ((LinearLayout.LayoutParams)localObject2).width = -1;
          ((LinearLayout.LayoutParams)localObject2).height = AndroidUtilities.dp(48.0F);
          ((View)localObject1).setLayoutParams((ViewGroup.LayoutParams)localObject2);
          return;
          bool = false;
        }
      }
    }, this, 85, 0, AndroidUtilities.dp(48.0F));
  }
  
  private Point startPositionRelativeToEntity(EntityView paramEntityView)
  {
    Object localObject;
    if (paramEntityView != null)
    {
      paramEntityView = paramEntityView.getPosition();
      localObject = new Point(paramEntityView.x + 200.0F, paramEntityView.y + 200.0F);
      return (Point)localObject;
    }
    for (paramEntityView = centerPositionForEntity();; paramEntityView = new Point(paramEntityView.x + 200.0F, paramEntityView.y + 200.0F))
    {
      int j = 0;
      int i = 0;
      if (i < this.entitiesView.getChildCount())
      {
        localObject = this.entitiesView.getChildAt(i);
        if (!(localObject instanceof EntityView)) {}
        for (;;)
        {
          i += 1;
          break;
          localObject = ((EntityView)localObject).getPosition();
          if ((float)Math.sqrt(Math.pow(((Point)localObject).x - paramEntityView.x, 2.0D) + Math.pow(((Point)localObject).y - paramEntityView.y, 2.0D)) < 100.0F) {
            j = 1;
          }
        }
      }
      localObject = paramEntityView;
      if (j == 0) {
        break;
      }
    }
  }
  
  private void updateSettingsButton()
  {
    int i = 2131165582;
    if (this.currentEntityView != null) {
      if ((this.currentEntityView instanceof StickerView))
      {
        i = 2131165579;
        this.paintButton.setImageResource(2131165581);
        this.paintButton.setColorFilter(null);
      }
    }
    for (;;)
    {
      this.colorPicker.setSettingsButtonImage(i);
      return;
      if (!(this.currentEntityView instanceof TextPaintView)) {
        break;
      }
      i = 2131165580;
      break;
      this.paintButton.setColorFilter(new PorterDuffColorFilter(-11420173, PorterDuff.Mode.MULTIPLY));
      this.paintButton.setImageResource(2131165581);
    }
  }
  
  public boolean allowInteraction(EntityView paramEntityView)
  {
    return !this.editingText;
  }
  
  public void closeTextEnter(boolean paramBoolean)
  {
    if ((!this.editingText) || (!(this.currentEntityView instanceof TextPaintView))) {
      return;
    }
    TextPaintView localTextPaintView = (TextPaintView)this.currentEntityView;
    this.toolsView.setVisibility(0);
    AndroidUtilities.hideKeyboard(localTextPaintView.getFocusedView());
    localTextPaintView.getFocusedView().clearFocus();
    localTextPaintView.endEditing();
    if (!paramBoolean) {
      localTextPaintView.setText(this.initialText);
    }
    if (localTextPaintView.getText().trim().length() == 0)
    {
      this.entitiesView.removeView(localTextPaintView);
      selectEntity(null);
    }
    for (;;)
    {
      setTextDimVisibility(false, localTextPaintView);
      this.editingText = false;
      this.initialText = null;
      this.curtainView.setVisibility(8);
      return;
      localTextPaintView.setPosition(this.editedTextPosition);
      localTextPaintView.setRotation(this.editedTextRotation);
      localTextPaintView.setScale(this.editedTextScale);
      this.editedTextPosition = null;
      this.editedTextRotation = 0.0F;
      this.editedTextScale = 0.0F;
    }
  }
  
  public Bitmap getBitmap()
  {
    Bitmap localBitmap = this.renderView.getResultBitmap();
    if ((localBitmap != null) && (this.entitiesView.entitiesCount() > 0))
    {
      Canvas localCanvas1 = new Canvas(localBitmap);
      int i = 0;
      if (i < this.entitiesView.getChildCount())
      {
        View localView = this.entitiesView.getChildAt(i);
        localCanvas1.save();
        Object localObject;
        Canvas localCanvas2;
        if ((localView instanceof EntityView))
        {
          localObject = (EntityView)localView;
          localCanvas1.translate(((EntityView)localObject).getPosition().x, ((EntityView)localObject).getPosition().y);
          localCanvas1.scale(localView.getScaleX(), localView.getScaleY());
          localCanvas1.rotate(localView.getRotation());
          localCanvas1.translate(-((EntityView)localObject).getWidth() / 2, -((EntityView)localObject).getHeight() / 2);
          if (!(localView instanceof TextPaintView)) {
            break label242;
          }
          localObject = Bitmaps.createBitmap(localView.getWidth(), localView.getHeight(), Bitmap.Config.ARGB_8888);
          localCanvas2 = new Canvas((Bitmap)localObject);
          localView.draw(localCanvas2);
          localCanvas1.drawBitmap((Bitmap)localObject, null, new Rect(0, 0, ((Bitmap)localObject).getWidth(), ((Bitmap)localObject).getHeight()), null);
        }
        for (;;)
        {
          try
          {
            localCanvas2.setBitmap(null);
            ((Bitmap)localObject).recycle();
            localCanvas1.restore();
            i += 1;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
            continue;
          }
          label242:
          localException.draw(localCanvas1);
        }
      }
    }
    return localBitmap;
  }
  
  public TextView getCancelTextView()
  {
    return this.cancelTextView;
  }
  
  public ColorPicker getColorPicker()
  {
    return this.colorPicker;
  }
  
  public TextView getDoneTextView()
  {
    return this.doneTextView;
  }
  
  public ArrayList<TLRPC.InputDocument> getMasks()
  {
    Object localObject1 = null;
    int j = this.entitiesView.getChildCount();
    int i = 0;
    while (i < j)
    {
      Object localObject3 = this.entitiesView.getChildAt(i);
      Object localObject2 = localObject1;
      if ((localObject3 instanceof StickerView))
      {
        localObject3 = ((StickerView)localObject3).getSticker();
        localObject2 = localObject1;
        if (localObject1 == null) {
          localObject2 = new ArrayList();
        }
        localObject1 = new TLRPC.TL_inputDocument();
        ((TLRPC.TL_inputDocument)localObject1).id = ((TLRPC.Document)localObject3).id;
        ((TLRPC.TL_inputDocument)localObject1).access_hash = ((TLRPC.Document)localObject3).access_hash;
        ((ArrayList)localObject2).add(localObject1);
      }
      i += 1;
      localObject1 = localObject2;
    }
    return (ArrayList<TLRPC.InputDocument>)localObject1;
  }
  
  public FrameLayout getToolsView()
  {
    return this.toolsView;
  }
  
  public void init()
  {
    this.renderView.setVisibility(0);
    detectFaces();
  }
  
  public void maybeShowDismissalAlert(PhotoViewer paramPhotoViewer, Activity paramActivity, final Runnable paramRunnable)
  {
    if (this.editingText) {
      closeTextEnter(false);
    }
    do
    {
      return;
      if (this.pickingSticker)
      {
        closeStickersView();
        return;
      }
      if (!hasChanges()) {
        break;
      }
    } while (paramActivity == null);
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setMessage(LocaleController.getString("DiscardChanges", 2131493388));
    paramActivity.setTitle(LocaleController.getString("AppName", 2131492981));
    paramActivity.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        paramRunnable.run();
      }
    });
    paramActivity.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
    paramPhotoViewer.showAlertDialog(paramActivity);
    return;
    paramRunnable.run();
  }
  
  public boolean onEntityLongClicked(EntityView paramEntityView)
  {
    showMenuForEntity(paramEntityView);
    return true;
  }
  
  public boolean onEntitySelected(EntityView paramEntityView)
  {
    return selectEntity(paramEntityView);
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    paramInt3 -= paramInt1;
    paramInt2 = paramInt4 - paramInt2;
    int j;
    int i;
    float f2;
    label77:
    float f1;
    if (Build.VERSION.SDK_INT >= 21)
    {
      paramInt1 = AndroidUtilities.statusBarHeight;
      j = ActionBar.getCurrentActionBarHeight();
      paramInt4 = ActionBar.getCurrentActionBarHeight() + paramInt1;
      i = AndroidUtilities.displaySize.y - j - AndroidUtilities.dp(48.0F);
      if (this.bitmapToEdit == null) {
        break label517;
      }
      if (!isSidewardOrientation()) {
        break label491;
      }
      f2 = this.bitmapToEdit.getHeight();
      if (!isSidewardOrientation()) {
        break label504;
      }
      f1 = this.bitmapToEdit.getWidth();
    }
    for (;;)
    {
      float f4 = paramInt3;
      float f3 = f4;
      if ((float)Math.floor(f4 * f1 / f2) > i) {
        f3 = (float)Math.floor(i * f2 / f1);
      }
      j = (int)Math.ceil((paramInt3 - this.renderView.getMeasuredWidth()) / 2);
      int k = (paramInt2 - paramInt4 - AndroidUtilities.dp(48.0F) - this.renderView.getMeasuredHeight()) / 2 + paramInt4 - ActionBar.getCurrentActionBarHeight() + AndroidUtilities.dp(8.0F);
      this.renderView.layout(j, k, this.renderView.getMeasuredWidth() + j, this.renderView.getMeasuredHeight() + k);
      f1 = f3 / this.paintingSize.width;
      this.entitiesView.setScaleX(f1);
      this.entitiesView.setScaleY(f1);
      this.entitiesView.layout(j, k, this.entitiesView.getMeasuredWidth() + j, this.entitiesView.getMeasuredHeight() + k);
      this.dimView.layout(0, paramInt1, this.dimView.getMeasuredWidth(), this.dimView.getMeasuredHeight() + paramInt1);
      this.selectionContainerView.layout(0, paramInt1, this.selectionContainerView.getMeasuredWidth(), this.selectionContainerView.getMeasuredHeight() + paramInt1);
      this.colorPicker.layout(0, paramInt4, this.colorPicker.getMeasuredWidth(), this.colorPicker.getMeasuredHeight() + paramInt4);
      this.toolsView.layout(0, paramInt2 - this.toolsView.getMeasuredHeight(), this.toolsView.getMeasuredWidth(), paramInt2);
      this.curtainView.layout(0, 0, paramInt3, i);
      if (this.stickersView != null) {
        this.stickersView.layout(0, paramInt1, this.stickersView.getMeasuredWidth(), this.stickersView.getMeasuredHeight() + paramInt1);
      }
      if (this.currentEntityView != null)
      {
        this.currentEntityView.updateSelectionView();
        this.currentEntityView.setOffset(this.entitiesView.getLeft() - this.selectionContainerView.getLeft(), this.entitiesView.getTop() - this.selectionContainerView.getTop());
      }
      return;
      paramInt1 = 0;
      break;
      label491:
      f2 = this.bitmapToEdit.getWidth();
      break label77;
      label504:
      f1 = this.bitmapToEdit.getHeight();
      continue;
      label517:
      f2 = paramInt3;
      f1 = paramInt2 - j - AndroidUtilities.dp(48.0F);
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    int i = View.MeasureSpec.getSize(paramInt1);
    paramInt2 = View.MeasureSpec.getSize(paramInt2);
    setMeasuredDimension(i, paramInt2);
    int j = AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(48.0F);
    float f2;
    float f1;
    if (this.bitmapToEdit != null) {
      if (isSidewardOrientation())
      {
        f2 = this.bitmapToEdit.getHeight();
        if (!isSidewardOrientation()) {
          break label304;
        }
        f1 = this.bitmapToEdit.getWidth();
      }
    }
    for (;;)
    {
      float f4 = i;
      float f5 = (float)Math.floor(f4 * f1 / f2);
      float f3 = f5;
      if (f5 > j)
      {
        f3 = j;
        f4 = (float)Math.floor(f3 * f2 / f1);
      }
      this.renderView.measure(View.MeasureSpec.makeMeasureSpec((int)f4, 1073741824), View.MeasureSpec.makeMeasureSpec((int)f3, 1073741824));
      this.entitiesView.measure(View.MeasureSpec.makeMeasureSpec((int)this.paintingSize.width, 1073741824), View.MeasureSpec.makeMeasureSpec((int)this.paintingSize.height, 1073741824));
      this.dimView.measure(paramInt1, View.MeasureSpec.makeMeasureSpec(j, Integer.MIN_VALUE));
      this.selectionContainerView.measure(paramInt1, View.MeasureSpec.makeMeasureSpec(j, 1073741824));
      this.colorPicker.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
      this.toolsView.measure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48.0F), 1073741824));
      if (this.stickersView != null) {
        this.stickersView.measure(paramInt1, View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, 1073741824));
      }
      return;
      f2 = this.bitmapToEdit.getWidth();
      break;
      label304:
      f1 = this.bitmapToEdit.getHeight();
      continue;
      f2 = i;
      f1 = paramInt2 - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(48.0F);
    }
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if (this.currentEntityView != null)
    {
      if (this.editingText) {
        closeTextEnter(true);
      }
    }
    else {
      return true;
    }
    selectEntity(null);
    return true;
  }
  
  public void shutdown()
  {
    this.renderView.shutdown();
    this.entitiesView.setVisibility(8);
    this.selectionContainerView.setVisibility(8);
    this.queue.postRunnable(new Runnable()
    {
      public void run()
      {
        Looper localLooper = Looper.myLooper();
        if (localLooper != null) {
          localLooper.quit();
        }
      }
    });
  }
  
  private class StickerPosition
  {
    private float angle;
    private Point position;
    private float scale;
    
    StickerPosition(Point paramPoint, float paramFloat1, float paramFloat2)
    {
      this.position = paramPoint;
      this.scale = paramFloat1;
      this.angle = paramFloat2;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/PhotoPaintView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */