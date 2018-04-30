package org.telegram.ui.ActionBar;

import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.text.SpannedString;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.support.widget.RecyclerView.RecycledViewPool;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatBigEmptyView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.GroupCreateCheckBox;
import org.telegram.ui.Components.GroupCreateSpan;
import org.telegram.ui.Components.LetterDrawable;
import org.telegram.ui.Components.LineProgressView;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.TypefaceSpan;

public class ThemeDescription
{
  public static int FLAG_AB_AM_BACKGROUND;
  public static int FLAG_AB_AM_ITEMSCOLOR;
  public static int FLAG_AB_AM_SELECTORCOLOR;
  public static int FLAG_AB_AM_TOPBACKGROUND;
  public static int FLAG_AB_ITEMSCOLOR;
  public static int FLAG_AB_SEARCH;
  public static int FLAG_AB_SEARCHPLACEHOLDER;
  public static int FLAG_AB_SELECTORCOLOR;
  public static int FLAG_AB_SUBMENUBACKGROUND = Integer.MIN_VALUE;
  public static int FLAG_AB_SUBMENUITEM;
  public static int FLAG_AB_SUBTITLECOLOR;
  public static int FLAG_AB_TITLECOLOR;
  public static int FLAG_BACKGROUND = 1;
  public static int FLAG_BACKGROUNDFILTER;
  public static int FLAG_CELLBACKGROUNDCOLOR;
  public static int FLAG_CHECKBOX;
  public static int FLAG_CHECKBOXCHECK;
  public static int FLAG_CHECKTAG;
  public static int FLAG_CURSORCOLOR;
  public static int FLAG_DRAWABLESELECTEDSTATE;
  public static int FLAG_FASTSCROLL;
  public static int FLAG_HINTTEXTCOLOR;
  public static int FLAG_IMAGECOLOR;
  public static int FLAG_LINKCOLOR = 2;
  public static int FLAG_LISTGLOWCOLOR;
  public static int FLAG_PROGRESSBAR;
  public static int FLAG_SECTIONS;
  public static int FLAG_SELECTOR;
  public static int FLAG_SELECTORWHITE;
  public static int FLAG_SERVICEBACKGROUND;
  public static int FLAG_TEXTCOLOR = 4;
  public static int FLAG_USEBACKGROUNDDRAWABLE;
  private HashMap<String, Field> cachedFields;
  private int changeFlags;
  private int currentColor;
  private String currentKey;
  private int defaultColor;
  private ThemeDescriptionDelegate delegate;
  private Drawable[] drawablesToUpdate;
  private Class[] listClasses;
  private String[] listClassesFieldName;
  private HashMap<String, Boolean> notFoundCachedFields;
  private Paint[] paintToUpdate;
  private int previousColor;
  private boolean[] previousIsDefault = new boolean[1];
  private View viewToInvalidate;
  
  static
  {
    FLAG_IMAGECOLOR = 8;
    FLAG_CELLBACKGROUNDCOLOR = 16;
    FLAG_BACKGROUNDFILTER = 32;
    FLAG_AB_ITEMSCOLOR = 64;
    FLAG_AB_TITLECOLOR = 128;
    FLAG_AB_SELECTORCOLOR = 256;
    FLAG_AB_AM_ITEMSCOLOR = 512;
    FLAG_AB_SUBTITLECOLOR = 1024;
    FLAG_PROGRESSBAR = 2048;
    FLAG_SELECTOR = 4096;
    FLAG_CHECKBOX = 8192;
    FLAG_CHECKBOXCHECK = 16384;
    FLAG_LISTGLOWCOLOR = 32768;
    FLAG_DRAWABLESELECTEDSTATE = 65536;
    FLAG_USEBACKGROUNDDRAWABLE = 131072;
    FLAG_CHECKTAG = 262144;
    FLAG_SECTIONS = 524288;
    FLAG_AB_AM_BACKGROUND = 1048576;
    FLAG_AB_AM_TOPBACKGROUND = 2097152;
    FLAG_AB_AM_SELECTORCOLOR = 4194304;
    FLAG_HINTTEXTCOLOR = 8388608;
    FLAG_CURSORCOLOR = 16777216;
    FLAG_FASTSCROLL = 33554432;
    FLAG_AB_SEARCHPLACEHOLDER = 67108864;
    FLAG_AB_SEARCH = 134217728;
    FLAG_SELECTORWHITE = 268435456;
    FLAG_SERVICEBACKGROUND = 536870912;
    FLAG_AB_SUBMENUITEM = 1073741824;
  }
  
  public ThemeDescription(View paramView, int paramInt, Class[] paramArrayOfClass, Paint paramPaint, Drawable[] paramArrayOfDrawable, ThemeDescriptionDelegate paramThemeDescriptionDelegate, String paramString)
  {
    this.currentKey = paramString;
    if (paramPaint != null) {
      this.paintToUpdate = new Paint[] { paramPaint };
    }
    this.drawablesToUpdate = paramArrayOfDrawable;
    this.viewToInvalidate = paramView;
    this.changeFlags = paramInt;
    this.listClasses = paramArrayOfClass;
    this.delegate = paramThemeDescriptionDelegate;
  }
  
  public ThemeDescription(View paramView, int paramInt, Class[] paramArrayOfClass, Paint[] paramArrayOfPaint, Drawable[] paramArrayOfDrawable, ThemeDescriptionDelegate paramThemeDescriptionDelegate, String paramString, Object paramObject)
  {
    this.currentKey = paramString;
    this.paintToUpdate = paramArrayOfPaint;
    this.drawablesToUpdate = paramArrayOfDrawable;
    this.viewToInvalidate = paramView;
    this.changeFlags = paramInt;
    this.listClasses = paramArrayOfClass;
    this.delegate = paramThemeDescriptionDelegate;
  }
  
  public ThemeDescription(View paramView, int paramInt, Class[] paramArrayOfClass, String[] paramArrayOfString, Paint[] paramArrayOfPaint, Drawable[] paramArrayOfDrawable, ThemeDescriptionDelegate paramThemeDescriptionDelegate, String paramString)
  {
    this.currentKey = paramString;
    this.paintToUpdate = paramArrayOfPaint;
    this.drawablesToUpdate = paramArrayOfDrawable;
    this.viewToInvalidate = paramView;
    this.changeFlags = paramInt;
    this.listClasses = paramArrayOfClass;
    this.listClassesFieldName = paramArrayOfString;
    this.delegate = paramThemeDescriptionDelegate;
    this.cachedFields = new HashMap();
    this.notFoundCachedFields = new HashMap();
  }
  
  private void processViewColor(View paramView, int paramInt)
  {
    int i = 0;
    Object localObject3;
    int j;
    label155:
    label223:
    label268:
    label373:
    label379:
    label607:
    Object localObject2;
    if (i < this.listClasses.length)
    {
      int k;
      Object localObject1;
      String str;
      if (this.listClasses[i].isInstance(paramView))
      {
        paramView.invalidate();
        if (((this.changeFlags & FLAG_CHECKTAG) != 0) && (((this.changeFlags & FLAG_CHECKTAG) == 0) || (!this.currentKey.equals(paramView.getTag())))) {
          break label373;
        }
        k = 1;
        paramView.invalidate();
        if ((this.changeFlags & FLAG_BACKGROUNDFILTER) == 0) {
          break label268;
        }
        localObject3 = paramView.getBackground();
        j = k;
        if (localObject3 != null)
        {
          if ((this.changeFlags & FLAG_CELLBACKGROUNDCOLOR) == 0) {
            break label223;
          }
          j = k;
          if ((localObject3 instanceof CombinedDrawable))
          {
            localObject1 = ((CombinedDrawable)localObject3).getBackground();
            j = k;
            if ((localObject1 instanceof ColorDrawable))
            {
              ((ColorDrawable)localObject1).setColor(paramInt);
              j = k;
            }
          }
        }
        if (this.listClassesFieldName == null) {
          break label1582;
        }
        str = this.listClasses[i] + "_" + this.listClassesFieldName[i];
        if ((this.notFoundCachedFields == null) || (!this.notFoundCachedFields.containsKey(str))) {
          break label379;
        }
      }
      for (;;)
      {
        i += 1;
        break;
        localObject1 = localObject3;
        if ((localObject3 instanceof CombinedDrawable)) {
          localObject1 = ((CombinedDrawable)localObject3).getIcon();
        }
        ((Drawable)localObject1).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
        j = k;
        break label155;
        if ((this.changeFlags & FLAG_CELLBACKGROUNDCOLOR) != 0)
        {
          paramView.setBackgroundColor(paramInt);
          j = k;
          break label155;
        }
        if ((this.changeFlags & FLAG_TEXTCOLOR) != 0)
        {
          j = k;
          if (!(paramView instanceof TextView)) {
            break label155;
          }
          ((TextView)paramView).setTextColor(paramInt);
          j = k;
          break label155;
        }
        j = k;
        if ((this.changeFlags & FLAG_SERVICEBACKGROUND) == 0) {
          break label155;
        }
        localObject1 = paramView.getBackground();
        j = k;
        if (localObject1 == null) {
          break label155;
        }
        ((Drawable)localObject1).setColorFilter(Theme.colorFilter);
        j = k;
        break label155;
        j = 0;
        break label155;
        try
        {
          localObject3 = (Field)this.cachedFields.get(str);
          localObject1 = localObject3;
          if (localObject3 == null)
          {
            localObject3 = this.listClasses[i].getDeclaredField(this.listClassesFieldName[i]);
            localObject1 = localObject3;
            if (localObject3 != null)
            {
              ((Field)localObject3).setAccessible(true);
              this.cachedFields.put(str, localObject3);
              localObject1 = localObject3;
            }
          }
          if (localObject1 == null) {
            continue;
          }
          Object localObject4 = ((Field)localObject1).get(paramView);
          if ((localObject4 == null) || ((j == 0) && ((localObject4 instanceof View)) && (!this.currentKey.equals(((View)localObject4).getTag())))) {
            continue;
          }
          if ((localObject4 instanceof View)) {
            ((View)localObject4).invalidate();
          }
          localObject3 = localObject4;
          if ((this.changeFlags & FLAG_USEBACKGROUNDDRAWABLE) != 0)
          {
            localObject3 = localObject4;
            if ((localObject4 instanceof View)) {
              localObject3 = ((View)localObject4).getBackground();
            }
          }
          if (((this.changeFlags & FLAG_BACKGROUND) == 0) || (!(localObject3 instanceof View))) {
            break label607;
          }
          ((View)localObject3).setBackgroundColor(paramInt);
        }
        catch (Throwable localThrowable)
        {
          FileLog.e(localThrowable);
          this.notFoundCachedFields.put(str, Boolean.valueOf(true));
        }
        continue;
        if ((localObject3 instanceof Switch))
        {
          ((Switch)localObject3).checkColorFilters();
        }
        else if ((localObject3 instanceof EditTextCaption))
        {
          if ((this.changeFlags & FLAG_HINTTEXTCOLOR) != 0)
          {
            ((EditTextCaption)localObject3).setHintColor(paramInt);
            ((EditTextCaption)localObject3).setHintTextColor(paramInt);
          }
          else
          {
            ((EditTextCaption)localObject3).setTextColor(paramInt);
          }
        }
        else if ((localObject3 instanceof SimpleTextView))
        {
          if ((this.changeFlags & FLAG_LINKCOLOR) != 0) {
            ((SimpleTextView)localObject3).setLinkTextColor(paramInt);
          } else {
            ((SimpleTextView)localObject3).setTextColor(paramInt);
          }
        }
        else if ((localObject3 instanceof TextView))
        {
          localObject2 = (TextView)localObject3;
          if ((this.changeFlags & FLAG_IMAGECOLOR) != 0)
          {
            localObject2 = ((TextView)localObject2).getCompoundDrawables();
            if (localObject2 != null)
            {
              j = 0;
              label762:
              if (j >= localObject2.length) {
                break label1607;
              }
              if (localObject2[j] == null) {
                break label1600;
              }
              localObject2[j].setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
              break label1600;
            }
          }
          else if ((this.changeFlags & FLAG_LINKCOLOR) != 0)
          {
            ((TextView)localObject2).getPaint().linkColor = paramInt;
            ((TextView)localObject2).invalidate();
          }
          else if ((this.changeFlags & FLAG_FASTSCROLL) != 0)
          {
            localObject2 = ((TextView)localObject2).getText();
            if ((localObject2 instanceof SpannedString))
            {
              localObject2 = (TypefaceSpan[])((SpannedString)localObject2).getSpans(0, ((CharSequence)localObject2).length(), TypefaceSpan.class);
              if ((localObject2 != null) && (localObject2.length > 0))
              {
                j = 0;
                while (j < localObject2.length)
                {
                  localObject2[j].setColor(paramInt);
                  j += 1;
                }
              }
            }
          }
          else
          {
            ((TextView)localObject2).setTextColor(paramInt);
          }
        }
        else if ((localObject3 instanceof ImageView))
        {
          ((ImageView)localObject3).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
        }
        else if ((localObject3 instanceof BackupImageView))
        {
          localObject2 = ((BackupImageView)localObject3).getImageReceiver().getStaticThumb();
          if ((localObject2 instanceof CombinedDrawable))
          {
            if ((this.changeFlags & FLAG_BACKGROUNDFILTER) != 0) {
              ((CombinedDrawable)localObject2).getBackground().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            } else {
              ((CombinedDrawable)localObject2).getIcon().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            }
          }
          else if (localObject2 != null) {
            ((Drawable)localObject2).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
          }
        }
        else
        {
          if (!(localObject3 instanceof Drawable)) {
            break label1269;
          }
          if ((localObject3 instanceof LetterDrawable))
          {
            if ((this.changeFlags & FLAG_BACKGROUNDFILTER) != 0) {
              ((LetterDrawable)localObject3).setBackgroundColor(paramInt);
            } else {
              ((LetterDrawable)localObject3).setColor(paramInt);
            }
          }
          else
          {
            if (!(localObject3 instanceof CombinedDrawable)) {
              break label1191;
            }
            if ((this.changeFlags & FLAG_BACKGROUNDFILTER) != 0) {
              ((CombinedDrawable)localObject3).getBackground().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            } else {
              ((CombinedDrawable)localObject3).getIcon().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            }
          }
        }
      }
      label1191:
      if (((localObject3 instanceof StateListDrawable)) || ((Build.VERSION.SDK_INT >= 21) && ((localObject3 instanceof RippleDrawable))))
      {
        localObject2 = (Drawable)localObject3;
        if ((this.changeFlags & FLAG_DRAWABLESELECTEDSTATE) == 0) {
          break label1609;
        }
      }
    }
    label1269:
    label1582:
    label1600:
    label1607:
    label1609:
    for (boolean bool = true;; bool = false)
    {
      Theme.setSelectorDrawableColor((Drawable)localObject2, paramInt, bool);
      break;
      ((Drawable)localObject3).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      break;
      if ((localObject3 instanceof CheckBox))
      {
        if ((this.changeFlags & FLAG_CHECKBOX) != 0)
        {
          ((CheckBox)localObject3).setBackgroundColor(paramInt);
          break;
        }
        if ((this.changeFlags & FLAG_CHECKBOXCHECK) == 0) {
          break;
        }
        ((CheckBox)localObject3).setCheckColor(paramInt);
        break;
      }
      if ((localObject3 instanceof GroupCreateCheckBox))
      {
        ((GroupCreateCheckBox)localObject3).updateColors();
        break;
      }
      if ((localObject3 instanceof Integer))
      {
        ((Field)localObject2).set(paramView, Integer.valueOf(paramInt));
        break;
      }
      if ((localObject3 instanceof RadioButton))
      {
        if ((this.changeFlags & FLAG_CHECKBOX) != 0)
        {
          ((RadioButton)localObject3).setBackgroundColor(paramInt);
          ((RadioButton)localObject3).invalidate();
          break;
        }
        if ((this.changeFlags & FLAG_CHECKBOXCHECK) == 0) {
          break;
        }
        ((RadioButton)localObject3).setCheckedColor(paramInt);
        ((RadioButton)localObject3).invalidate();
        break;
      }
      if ((localObject3 instanceof TextPaint))
      {
        if ((this.changeFlags & FLAG_LINKCOLOR) != 0)
        {
          ((TextPaint)localObject3).linkColor = paramInt;
          break;
        }
        ((TextPaint)localObject3).setColor(paramInt);
        break;
      }
      if ((localObject3 instanceof LineProgressView))
      {
        if ((this.changeFlags & FLAG_PROGRESSBAR) != 0)
        {
          ((LineProgressView)localObject3).setProgressColor(paramInt);
          break;
        }
        ((LineProgressView)localObject3).setBackColor(paramInt);
        break;
      }
      if ((localObject3 instanceof Paint))
      {
        ((Paint)localObject3).setColor(paramInt);
        break;
      }
      if (!(localObject3 instanceof SeekBarView)) {
        break;
      }
      if ((this.changeFlags & FLAG_PROGRESSBAR) != 0)
      {
        ((SeekBarView)localObject3).setOuterColor(paramInt);
        break;
      }
      ((SeekBarView)localObject3).setInnerColor(paramInt);
      break;
      if (!(paramView instanceof GroupCreateSpan)) {
        break;
      }
      ((GroupCreateSpan)paramView).updateColors();
      break;
      return;
      j += 1;
      break label762;
      break;
    }
  }
  
  public int getCurrentColor()
  {
    return this.currentColor;
  }
  
  public String getCurrentKey()
  {
    return this.currentKey;
  }
  
  public int getSetColor()
  {
    return Theme.getColor(this.currentKey);
  }
  
  public String getTitle()
  {
    return this.currentKey;
  }
  
  public void setColor(int paramInt, boolean paramBoolean)
  {
    setColor(paramInt, paramBoolean, true);
  }
  
  public void setColor(int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean2) {
      Theme.setColor(this.currentKey, paramInt, paramBoolean1);
    }
    int i;
    if (this.paintToUpdate != null)
    {
      i = 0;
      if (i < this.paintToUpdate.length)
      {
        if (((this.changeFlags & FLAG_LINKCOLOR) != 0) && ((this.paintToUpdate[i] instanceof TextPaint))) {
          ((TextPaint)this.paintToUpdate[i]).linkColor = paramInt;
        }
        for (;;)
        {
          i += 1;
          break;
          this.paintToUpdate[i].setColor(paramInt);
        }
      }
    }
    if (this.drawablesToUpdate != null)
    {
      i = 0;
      if (i < this.drawablesToUpdate.length)
      {
        if (this.drawablesToUpdate[i] == null) {}
        for (;;)
        {
          i += 1;
          break;
          if ((this.drawablesToUpdate[i] instanceof CombinedDrawable))
          {
            if ((this.changeFlags & FLAG_BACKGROUNDFILTER) != 0) {
              ((CombinedDrawable)this.drawablesToUpdate[i]).getBackground().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            } else {
              ((CombinedDrawable)this.drawablesToUpdate[i]).getIcon().setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
            }
          }
          else if ((this.drawablesToUpdate[i] instanceof AvatarDrawable)) {
            ((AvatarDrawable)this.drawablesToUpdate[i]).setColor(paramInt);
          } else {
            this.drawablesToUpdate[i].setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
          }
        }
      }
    }
    if ((this.viewToInvalidate != null) && (this.listClasses == null) && (this.listClassesFieldName == null) && (((this.changeFlags & FLAG_CHECKTAG) == 0) || (((this.changeFlags & FLAG_CHECKTAG) != 0) && (this.currentKey.equals(this.viewToInvalidate.getTag())))))
    {
      if ((this.changeFlags & FLAG_BACKGROUND) != 0) {
        this.viewToInvalidate.setBackgroundColor(paramInt);
      }
      if ((this.changeFlags & FLAG_BACKGROUNDFILTER) != 0)
      {
        localObject2 = this.viewToInvalidate.getBackground();
        localObject1 = localObject2;
        if ((localObject2 instanceof CombinedDrawable))
        {
          if ((this.changeFlags & FLAG_DRAWABLESELECTEDSTATE) == 0) {
            break label1268;
          }
          localObject1 = ((CombinedDrawable)localObject2).getBackground();
        }
        if (localObject1 != null)
        {
          if ((!(localObject1 instanceof StateListDrawable)) && ((Build.VERSION.SDK_INT < 21) || (!(localObject1 instanceof RippleDrawable)))) {
            break label1286;
          }
          if ((this.changeFlags & FLAG_DRAWABLESELECTEDSTATE) == 0) {
            break label1281;
          }
          paramBoolean1 = true;
          label445:
          Theme.setSelectorDrawableColor((Drawable)localObject1, paramInt, paramBoolean1);
        }
      }
    }
    label452:
    if ((this.viewToInvalidate instanceof ActionBar))
    {
      if ((this.changeFlags & FLAG_AB_ITEMSCOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setItemsColor(paramInt, false);
      }
      if ((this.changeFlags & FLAG_AB_TITLECOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setTitleColor(paramInt);
      }
      if ((this.changeFlags & FLAG_AB_SELECTORCOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setItemsBackgroundColor(paramInt, false);
      }
      if ((this.changeFlags & FLAG_AB_AM_SELECTORCOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setItemsBackgroundColor(paramInt, true);
      }
      if ((this.changeFlags & FLAG_AB_AM_ITEMSCOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setItemsColor(paramInt, true);
      }
      if ((this.changeFlags & FLAG_AB_SUBTITLECOLOR) != 0) {
        ((ActionBar)this.viewToInvalidate).setSubtitleColor(paramInt);
      }
      if ((this.changeFlags & FLAG_AB_AM_BACKGROUND) != 0) {
        ((ActionBar)this.viewToInvalidate).setActionModeColor(paramInt);
      }
      if ((this.changeFlags & FLAG_AB_AM_TOPBACKGROUND) != 0) {
        ((ActionBar)this.viewToInvalidate).setActionModeTopColor(paramInt);
      }
      if ((this.changeFlags & FLAG_AB_SEARCHPLACEHOLDER) != 0) {
        ((ActionBar)this.viewToInvalidate).setSearchTextColor(paramInt, true);
      }
      if ((this.changeFlags & FLAG_AB_SEARCH) != 0) {
        ((ActionBar)this.viewToInvalidate).setSearchTextColor(paramInt, false);
      }
      if ((this.changeFlags & FLAG_AB_SUBMENUITEM) != 0) {
        ((ActionBar)this.viewToInvalidate).setPopupItemsColor(paramInt);
      }
      if ((this.changeFlags & FLAG_AB_SUBMENUBACKGROUND) != 0) {
        ((ActionBar)this.viewToInvalidate).setPopupBackgroundColor(paramInt);
      }
    }
    if ((this.viewToInvalidate instanceof EmptyTextProgressView))
    {
      if ((this.changeFlags & FLAG_TEXTCOLOR) != 0) {
        ((EmptyTextProgressView)this.viewToInvalidate).setTextColor(paramInt);
      }
    }
    else
    {
      label764:
      if (!(this.viewToInvalidate instanceof RadialProgressView)) {
        break label1353;
      }
      ((RadialProgressView)this.viewToInvalidate).setProgressColor(paramInt);
      label785:
      if (((this.changeFlags & FLAG_TEXTCOLOR) != 0) && (((this.changeFlags & FLAG_CHECKTAG) == 0) || ((this.viewToInvalidate != null) && ((this.changeFlags & FLAG_CHECKTAG) != 0) && (this.currentKey.equals(this.viewToInvalidate.getTag())))))
      {
        if (!(this.viewToInvalidate instanceof TextView)) {
          break label1425;
        }
        ((TextView)this.viewToInvalidate).setTextColor(paramInt);
      }
      label863:
      if (((this.changeFlags & FLAG_CURSORCOLOR) != 0) && ((this.viewToInvalidate instanceof EditTextBoldCursor))) {
        ((EditTextBoldCursor)this.viewToInvalidate).setCursorColor(paramInt);
      }
      if ((this.changeFlags & FLAG_HINTTEXTCOLOR) != 0)
      {
        if (!(this.viewToInvalidate instanceof EditTextBoldCursor)) {
          break label1497;
        }
        ((EditTextBoldCursor)this.viewToInvalidate).setHintColor(paramInt);
      }
      label927:
      if ((this.viewToInvalidate != null) && ((this.changeFlags & FLAG_SERVICEBACKGROUND) != 0))
      {
        localObject1 = this.viewToInvalidate.getBackground();
        if (localObject1 != null) {
          ((Drawable)localObject1).setColorFilter(Theme.colorFilter);
        }
      }
      if (((this.changeFlags & FLAG_IMAGECOLOR) != 0) && (((this.changeFlags & FLAG_CHECKTAG) == 0) || (((this.changeFlags & FLAG_CHECKTAG) != 0) && (this.currentKey.equals(this.viewToInvalidate.getTag())))))
      {
        if (!(this.viewToInvalidate instanceof ImageView)) {
          break label1550;
        }
        if ((this.changeFlags & FLAG_USEBACKGROUNDDRAWABLE) == 0) {
          break label1526;
        }
        localObject1 = ((ImageView)this.viewToInvalidate).getDrawable();
        if (((localObject1 instanceof StateListDrawable)) || ((Build.VERSION.SDK_INT >= 21) && ((localObject1 instanceof RippleDrawable))))
        {
          if ((this.changeFlags & FLAG_DRAWABLESELECTEDSTATE) == 0) {
            break label1521;
          }
          paramBoolean1 = true;
          label1087:
          Theme.setSelectorDrawableColor((Drawable)localObject1, paramInt, paramBoolean1);
        }
      }
    }
    for (;;)
    {
      if (((this.viewToInvalidate instanceof ScrollView)) && ((this.changeFlags & FLAG_LISTGLOWCOLOR) != 0)) {
        AndroidUtilities.setScrollViewEdgeEffectColor((ScrollView)this.viewToInvalidate, paramInt);
      }
      if (!(this.viewToInvalidate instanceof RecyclerListView)) {
        break label1703;
      }
      localObject1 = (RecyclerListView)this.viewToInvalidate;
      if (((this.changeFlags & FLAG_SELECTOR) != 0) && (this.currentKey.equals("listSelectorSDK21"))) {
        ((RecyclerListView)localObject1).setListSelectorColor(paramInt);
      }
      if ((this.changeFlags & FLAG_FASTSCROLL) != 0) {
        ((RecyclerListView)localObject1).updateFastScrollColors();
      }
      if ((this.changeFlags & FLAG_LISTGLOWCOLOR) != 0) {
        ((RecyclerListView)localObject1).setGlowColor(paramInt);
      }
      if ((this.changeFlags & FLAG_SECTIONS) == 0) {
        break label1631;
      }
      localObject2 = ((RecyclerListView)localObject1).getHeaders();
      if (localObject2 == null) {
        break label1563;
      }
      i = 0;
      while (i < ((ArrayList)localObject2).size())
      {
        processViewColor((View)((ArrayList)localObject2).get(i), paramInt);
        i += 1;
      }
      label1268:
      localObject1 = ((CombinedDrawable)localObject2).getIcon();
      break;
      label1281:
      paramBoolean1 = false;
      break label445;
      label1286:
      if ((localObject1 instanceof ShapeDrawable))
      {
        ((ShapeDrawable)localObject1).getPaint().setColor(paramInt);
        break label452;
      }
      ((Drawable)localObject1).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      break label452;
      if ((this.changeFlags & FLAG_PROGRESSBAR) == 0) {
        break label764;
      }
      ((EmptyTextProgressView)this.viewToInvalidate).setProgressBarColor(paramInt);
      break label764;
      label1353:
      if ((this.viewToInvalidate instanceof LineProgressView))
      {
        if ((this.changeFlags & FLAG_PROGRESSBAR) != 0)
        {
          ((LineProgressView)this.viewToInvalidate).setProgressColor(paramInt);
          break label785;
        }
        ((LineProgressView)this.viewToInvalidate).setBackColor(paramInt);
        break label785;
      }
      if (!(this.viewToInvalidate instanceof ContextProgressView)) {
        break label785;
      }
      ((ContextProgressView)this.viewToInvalidate).updateColors();
      break label785;
      label1425:
      if ((this.viewToInvalidate instanceof NumberTextView))
      {
        ((NumberTextView)this.viewToInvalidate).setTextColor(paramInt);
        break label863;
      }
      if ((this.viewToInvalidate instanceof SimpleTextView))
      {
        ((SimpleTextView)this.viewToInvalidate).setTextColor(paramInt);
        break label863;
      }
      if (!(this.viewToInvalidate instanceof ChatBigEmptyView)) {
        break label863;
      }
      ((ChatBigEmptyView)this.viewToInvalidate).setTextColor(paramInt);
      break label863;
      label1497:
      if (!(this.viewToInvalidate instanceof EditText)) {
        break label927;
      }
      ((EditText)this.viewToInvalidate).setHintTextColor(paramInt);
      break label927;
      label1521:
      paramBoolean1 = false;
      break label1087;
      label1526:
      ((ImageView)this.viewToInvalidate).setColorFilter(new PorterDuffColorFilter(paramInt, PorterDuff.Mode.MULTIPLY));
      continue;
      label1550:
      if (!(this.viewToInvalidate instanceof BackupImageView)) {}
    }
    label1563:
    Object localObject2 = ((RecyclerListView)localObject1).getHeadersCache();
    if (localObject2 != null)
    {
      i = 0;
      while (i < ((ArrayList)localObject2).size())
      {
        processViewColor((View)((ArrayList)localObject2).get(i), paramInt);
        i += 1;
      }
    }
    Object localObject1 = ((RecyclerListView)localObject1).getPinnedHeader();
    if (localObject1 != null) {
      processViewColor((View)localObject1, paramInt);
    }
    label1631:
    while (this.listClasses != null)
    {
      int j;
      if ((this.viewToInvalidate instanceof RecyclerListView))
      {
        localObject1 = (RecyclerListView)this.viewToInvalidate;
        ((RecyclerListView)localObject1).getRecycledViewPool().clear();
        j = ((RecyclerListView)localObject1).getHiddenChildCount();
        i = 0;
        for (;;)
        {
          if (i < j)
          {
            processViewColor(((RecyclerListView)localObject1).getHiddenChildAt(i), paramInt);
            i += 1;
            continue;
            label1703:
            if (this.viewToInvalidate == null) {
              break;
            }
            if ((this.changeFlags & FLAG_SELECTOR) != 0)
            {
              this.viewToInvalidate.setBackgroundDrawable(Theme.getSelectorDrawable(false));
              break;
            }
            if ((this.changeFlags & FLAG_SELECTORWHITE) == 0) {
              break;
            }
            this.viewToInvalidate.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            break;
          }
        }
        j = ((RecyclerListView)localObject1).getCachedChildCount();
        i = 0;
        while (i < j)
        {
          processViewColor(((RecyclerListView)localObject1).getCachedChildAt(i), paramInt);
          i += 1;
        }
        j = ((RecyclerListView)localObject1).getAttachedScrapChildCount();
        i = 0;
        while (i < j)
        {
          processViewColor(((RecyclerListView)localObject1).getAttachedScrapChildAt(i), paramInt);
          i += 1;
        }
      }
      if ((this.viewToInvalidate instanceof ViewGroup))
      {
        localObject1 = (ViewGroup)this.viewToInvalidate;
        j = ((ViewGroup)localObject1).getChildCount();
        i = 0;
        while (i < j)
        {
          processViewColor(((ViewGroup)localObject1).getChildAt(i), paramInt);
          i += 1;
        }
      }
      processViewColor(this.viewToInvalidate, paramInt);
    }
    this.currentColor = paramInt;
    if (this.delegate != null) {
      this.delegate.didSetColor();
    }
    if (this.viewToInvalidate != null) {
      this.viewToInvalidate.invalidate();
    }
  }
  
  public void setDefaultColor()
  {
    setColor(Theme.getDefaultColor(this.currentKey), true);
  }
  
  public ThemeDescriptionDelegate setDelegateDisabled()
  {
    ThemeDescriptionDelegate localThemeDescriptionDelegate = this.delegate;
    this.delegate = null;
    return localThemeDescriptionDelegate;
  }
  
  public void setPreviousColor()
  {
    setColor(this.previousColor, this.previousIsDefault[0]);
  }
  
  public void startEditing()
  {
    int i = Theme.getColor(this.currentKey, this.previousIsDefault);
    this.previousColor = i;
    this.currentColor = i;
  }
  
  public static abstract interface ThemeDescriptionDelegate
  {
    public abstract void didSetColor();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/ThemeDescription.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */