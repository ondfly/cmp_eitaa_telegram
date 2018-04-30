package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;

public class EditTextBoldCursor
  extends EditText
{
  private static Method getVerticalOffsetMethod;
  private static Field mCursorDrawableField;
  private static Field mCursorDrawableResField;
  private static Field mEditor;
  private static Field mScrollYField;
  private static Field mShowCursorField;
  private boolean allowDrawCursor = true;
  private int cursorSize;
  private float cursorWidth = 2.0F;
  private Object editor;
  private GradientDrawable gradientDrawable;
  private float hintAlpha = 1.0F;
  private int hintColor;
  private StaticLayout hintLayout;
  private boolean hintVisible = true;
  private int ignoreBottomCount;
  private int ignoreTopCount;
  private long lastUpdateTime;
  private float lineSpacingExtra;
  private Drawable[] mCursorDrawable;
  private Rect rect = new Rect();
  private int scrollY;
  
  public EditTextBoldCursor(Context paramContext)
  {
    super(paramContext);
    if (mCursorDrawableField == null) {}
    try
    {
      mScrollYField = View.class.getDeclaredField("mScrollY");
      mScrollYField.setAccessible(true);
      mCursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
      mCursorDrawableResField.setAccessible(true);
      mEditor = TextView.class.getDeclaredField("mEditor");
      mEditor.setAccessible(true);
      paramContext = Class.forName("android.widget.Editor");
      mShowCursorField = paramContext.getDeclaredField("mShowCursor");
      mShowCursorField.setAccessible(true);
      mCursorDrawableField = paramContext.getDeclaredField("mCursorDrawable");
      mCursorDrawableField.setAccessible(true);
      getVerticalOffsetMethod = TextView.class.getDeclaredMethod("getVerticalOffset", new Class[] { Boolean.TYPE });
      getVerticalOffsetMethod.setAccessible(true);
      try
      {
        this.gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { -11230757, -11230757 });
        this.editor = mEditor.get(this);
        this.mCursorDrawable = ((Drawable[])mCursorDrawableField.get(this.editor));
        mCursorDrawableResField.set(this, Integer.valueOf(2131165314));
        this.cursorSize = AndroidUtilities.dp(24.0F);
        return;
      }
      catch (Exception paramContext)
      {
        for (;;)
        {
          FileLog.e(paramContext);
        }
      }
    }
    catch (Throwable paramContext)
    {
      for (;;) {}
    }
  }
  
  public int getExtendedPaddingBottom()
  {
    if (this.ignoreBottomCount != 0)
    {
      this.ignoreBottomCount -= 1;
      if (this.scrollY != Integer.MAX_VALUE) {
        return -this.scrollY;
      }
      return 0;
    }
    return super.getExtendedPaddingBottom();
  }
  
  public int getExtendedPaddingTop()
  {
    if (this.ignoreTopCount != 0)
    {
      this.ignoreTopCount -= 1;
      return 0;
    }
    return super.getExtendedPaddingTop();
  }
  
  /* Error */
  protected void onDraw(android.graphics.Canvas paramCanvas)
  {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual 184	org/telegram/ui/Components/EditTextBoldCursor:getExtendedPaddingTop	()I
    //   4: istore_3
    //   5: aload_0
    //   6: ldc -82
    //   8: putfield 173	org/telegram/ui/Components/EditTextBoldCursor:scrollY	I
    //   11: aload_0
    //   12: getstatic 74	org/telegram/ui/Components/EditTextBoldCursor:mScrollYField	Ljava/lang/reflect/Field;
    //   15: aload_0
    //   16: invokevirtual 188	java/lang/reflect/Field:getInt	(Ljava/lang/Object;)I
    //   19: putfield 173	org/telegram/ui/Components/EditTextBoldCursor:scrollY	I
    //   22: getstatic 74	org/telegram/ui/Components/EditTextBoldCursor:mScrollYField	Ljava/lang/reflect/Field;
    //   25: aload_0
    //   26: iconst_0
    //   27: invokestatic 147	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   30: invokevirtual 151	java/lang/reflect/Field:set	(Ljava/lang/Object;Ljava/lang/Object;)V
    //   33: aload_0
    //   34: iconst_1
    //   35: putfield 179	org/telegram/ui/Components/EditTextBoldCursor:ignoreTopCount	I
    //   38: aload_0
    //   39: iconst_1
    //   40: putfield 171	org/telegram/ui/Components/EditTextBoldCursor:ignoreBottomCount	I
    //   43: aload_1
    //   44: invokevirtual 193	android/graphics/Canvas:save	()I
    //   47: pop
    //   48: aload_1
    //   49: fconst_0
    //   50: iload_3
    //   51: i2f
    //   52: invokevirtual 197	android/graphics/Canvas:translate	(FF)V
    //   55: aload_0
    //   56: aload_1
    //   57: invokespecial 199	android/widget/EditText:onDraw	(Landroid/graphics/Canvas;)V
    //   60: aload_0
    //   61: getfield 173	org/telegram/ui/Components/EditTextBoldCursor:scrollY	I
    //   64: ldc -82
    //   66: if_icmpeq +17 -> 83
    //   69: getstatic 74	org/telegram/ui/Components/EditTextBoldCursor:mScrollYField	Ljava/lang/reflect/Field;
    //   72: aload_0
    //   73: aload_0
    //   74: getfield 173	org/telegram/ui/Components/EditTextBoldCursor:scrollY	I
    //   77: invokestatic 147	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   80: invokevirtual 151	java/lang/reflect/Field:set	(Ljava/lang/Object;Ljava/lang/Object;)V
    //   83: aload_1
    //   84: invokevirtual 202	android/graphics/Canvas:restore	()V
    //   87: aload_0
    //   88: invokevirtual 205	org/telegram/ui/Components/EditTextBoldCursor:length	()I
    //   91: ifne +248 -> 339
    //   94: aload_0
    //   95: getfield 207	org/telegram/ui/Components/EditTextBoldCursor:hintLayout	Landroid/text/StaticLayout;
    //   98: ifnull +241 -> 339
    //   101: aload_0
    //   102: getfield 54	org/telegram/ui/Components/EditTextBoldCursor:hintVisible	Z
    //   105: ifne +12 -> 117
    //   108: aload_0
    //   109: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   112: fconst_0
    //   113: fcmpl
    //   114: ifeq +225 -> 339
    //   117: aload_0
    //   118: getfield 54	org/telegram/ui/Components/EditTextBoldCursor:hintVisible	Z
    //   121: ifeq +12 -> 133
    //   124: aload_0
    //   125: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   128: fconst_1
    //   129: fcmpl
    //   130: ifne +19 -> 149
    //   133: aload_0
    //   134: getfield 54	org/telegram/ui/Components/EditTextBoldCursor:hintVisible	Z
    //   137: ifne +97 -> 234
    //   140: aload_0
    //   141: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   144: fconst_0
    //   145: fcmpl
    //   146: ifeq +88 -> 234
    //   149: invokestatic 213	java/lang/System:currentTimeMillis	()J
    //   152: lstore 9
    //   154: lload 9
    //   156: aload_0
    //   157: getfield 215	org/telegram/ui/Components/EditTextBoldCursor:lastUpdateTime	J
    //   160: lsub
    //   161: lstore 7
    //   163: lload 7
    //   165: lconst_0
    //   166: lcmp
    //   167: iflt +16 -> 183
    //   170: lload 7
    //   172: lstore 5
    //   174: lload 7
    //   176: ldc2_w 216
    //   179: lcmp
    //   180: ifle +8 -> 188
    //   183: ldc2_w 216
    //   186: lstore 5
    //   188: aload_0
    //   189: lload 9
    //   191: putfield 215	org/telegram/ui/Components/EditTextBoldCursor:lastUpdateTime	J
    //   194: aload_0
    //   195: getfield 54	org/telegram/ui/Components/EditTextBoldCursor:hintVisible	Z
    //   198: ifeq +466 -> 664
    //   201: aload_0
    //   202: aload_0
    //   203: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   206: lload 5
    //   208: l2f
    //   209: ldc -38
    //   211: fdiv
    //   212: fadd
    //   213: putfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   216: aload_0
    //   217: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   220: fconst_1
    //   221: fcmpl
    //   222: ifle +8 -> 230
    //   225: aload_0
    //   226: fconst_1
    //   227: putfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   230: aload_0
    //   231: invokevirtual 221	org/telegram/ui/Components/EditTextBoldCursor:invalidate	()V
    //   234: aload_0
    //   235: invokevirtual 225	org/telegram/ui/Components/EditTextBoldCursor:getPaint	()Landroid/text/TextPaint;
    //   238: invokevirtual 230	android/text/TextPaint:getColor	()I
    //   241: istore 4
    //   243: aload_0
    //   244: invokevirtual 225	org/telegram/ui/Components/EditTextBoldCursor:getPaint	()Landroid/text/TextPaint;
    //   247: aload_0
    //   248: getfield 232	org/telegram/ui/Components/EditTextBoldCursor:hintColor	I
    //   251: invokevirtual 236	android/text/TextPaint:setColor	(I)V
    //   254: aload_0
    //   255: invokevirtual 225	org/telegram/ui/Components/EditTextBoldCursor:getPaint	()Landroid/text/TextPaint;
    //   258: ldc -19
    //   260: aload_0
    //   261: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   264: fmul
    //   265: f2i
    //   266: invokevirtual 240	android/text/TextPaint:setAlpha	(I)V
    //   269: aload_1
    //   270: invokevirtual 193	android/graphics/Canvas:save	()I
    //   273: pop
    //   274: iconst_0
    //   275: istore_3
    //   276: aload_0
    //   277: getfield 207	org/telegram/ui/Components/EditTextBoldCursor:hintLayout	Landroid/text/StaticLayout;
    //   280: iconst_0
    //   281: invokevirtual 246	android/text/StaticLayout:getLineLeft	(I)F
    //   284: fstore_2
    //   285: fload_2
    //   286: fconst_0
    //   287: fcmpl
    //   288: ifeq +9 -> 297
    //   291: iconst_0
    //   292: i2f
    //   293: fload_2
    //   294: fsub
    //   295: f2i
    //   296: istore_3
    //   297: aload_1
    //   298: iload_3
    //   299: i2f
    //   300: aload_0
    //   301: invokevirtual 249	org/telegram/ui/Components/EditTextBoldCursor:getMeasuredHeight	()I
    //   304: aload_0
    //   305: getfield 207	org/telegram/ui/Components/EditTextBoldCursor:hintLayout	Landroid/text/StaticLayout;
    //   308: invokevirtual 252	android/text/StaticLayout:getHeight	()I
    //   311: isub
    //   312: i2f
    //   313: fconst_2
    //   314: fdiv
    //   315: invokevirtual 197	android/graphics/Canvas:translate	(FF)V
    //   318: aload_0
    //   319: getfield 207	org/telegram/ui/Components/EditTextBoldCursor:hintLayout	Landroid/text/StaticLayout;
    //   322: aload_1
    //   323: invokevirtual 255	android/text/StaticLayout:draw	(Landroid/graphics/Canvas;)V
    //   326: aload_0
    //   327: invokevirtual 225	org/telegram/ui/Components/EditTextBoldCursor:getPaint	()Landroid/text/TextPaint;
    //   330: iload 4
    //   332: invokevirtual 236	android/text/TextPaint:setColor	(I)V
    //   335: aload_1
    //   336: invokevirtual 202	android/graphics/Canvas:restore	()V
    //   339: aload_0
    //   340: getfield 58	org/telegram/ui/Components/EditTextBoldCursor:allowDrawCursor	Z
    //   343: ifeq +320 -> 663
    //   346: getstatic 99	org/telegram/ui/Components/EditTextBoldCursor:mShowCursorField	Ljava/lang/reflect/Field;
    //   349: ifnull +314 -> 663
    //   352: aload_0
    //   353: getfield 140	org/telegram/ui/Components/EditTextBoldCursor:mCursorDrawable	[Landroid/graphics/drawable/Drawable;
    //   356: ifnull +307 -> 663
    //   359: aload_0
    //   360: getfield 140	org/telegram/ui/Components/EditTextBoldCursor:mCursorDrawable	[Landroid/graphics/drawable/Drawable;
    //   363: iconst_0
    //   364: aaload
    //   365: ifnull +298 -> 663
    //   368: getstatic 99	org/telegram/ui/Components/EditTextBoldCursor:mShowCursorField	Ljava/lang/reflect/Field;
    //   371: aload_0
    //   372: getfield 137	org/telegram/ui/Components/EditTextBoldCursor:editor	Ljava/lang/Object;
    //   375: invokevirtual 259	java/lang/reflect/Field:getLong	(Ljava/lang/Object;)J
    //   378: lstore 5
    //   380: invokestatic 264	android/os/SystemClock:uptimeMillis	()J
    //   383: lload 5
    //   385: lsub
    //   386: ldc2_w 265
    //   389: lrem
    //   390: ldc2_w 267
    //   393: lcmp
    //   394: ifge +302 -> 696
    //   397: aload_0
    //   398: invokevirtual 272	org/telegram/ui/Components/EditTextBoldCursor:isFocused	()Z
    //   401: ifeq +295 -> 696
    //   404: iconst_1
    //   405: istore_3
    //   406: iload_3
    //   407: ifeq +256 -> 663
    //   410: aload_1
    //   411: invokevirtual 193	android/graphics/Canvas:save	()I
    //   414: pop
    //   415: iconst_0
    //   416: istore_3
    //   417: aload_0
    //   418: invokevirtual 275	org/telegram/ui/Components/EditTextBoldCursor:getGravity	()I
    //   421: bipush 112
    //   423: iand
    //   424: bipush 48
    //   426: if_icmpeq +28 -> 454
    //   429: getstatic 114	org/telegram/ui/Components/EditTextBoldCursor:getVerticalOffsetMethod	Ljava/lang/reflect/Method;
    //   432: aload_0
    //   433: iconst_1
    //   434: anewarray 277	java/lang/Object
    //   437: dup
    //   438: iconst_0
    //   439: iconst_1
    //   440: invokestatic 280	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
    //   443: aastore
    //   444: invokevirtual 284	java/lang/reflect/Method:invoke	(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    //   447: checkcast 143	java/lang/Integer
    //   450: invokevirtual 287	java/lang/Integer:intValue	()I
    //   453: istore_3
    //   454: aload_1
    //   455: aload_0
    //   456: invokevirtual 290	org/telegram/ui/Components/EditTextBoldCursor:getPaddingLeft	()I
    //   459: i2f
    //   460: aload_0
    //   461: invokevirtual 184	org/telegram/ui/Components/EditTextBoldCursor:getExtendedPaddingTop	()I
    //   464: iload_3
    //   465: iadd
    //   466: i2f
    //   467: invokevirtual 197	android/graphics/Canvas:translate	(FF)V
    //   470: aload_0
    //   471: invokevirtual 294	org/telegram/ui/Components/EditTextBoldCursor:getLayout	()Landroid/text/Layout;
    //   474: astore 11
    //   476: aload 11
    //   478: aload_0
    //   479: invokevirtual 297	org/telegram/ui/Components/EditTextBoldCursor:getSelectionStart	()I
    //   482: invokevirtual 303	android/text/Layout:getLineForOffset	(I)I
    //   485: istore_3
    //   486: aload 11
    //   488: invokevirtual 306	android/text/Layout:getLineCount	()I
    //   491: istore 4
    //   493: aload_0
    //   494: getfield 140	org/telegram/ui/Components/EditTextBoldCursor:mCursorDrawable	[Landroid/graphics/drawable/Drawable;
    //   497: iconst_0
    //   498: aaload
    //   499: invokevirtual 312	android/graphics/drawable/Drawable:getBounds	()Landroid/graphics/Rect;
    //   502: astore 11
    //   504: aload_0
    //   505: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   508: aload 11
    //   510: getfield 315	android/graphics/Rect:left	I
    //   513: putfield 315	android/graphics/Rect:left	I
    //   516: aload_0
    //   517: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   520: aload 11
    //   522: getfield 315	android/graphics/Rect:left	I
    //   525: aload_0
    //   526: getfield 60	org/telegram/ui/Components/EditTextBoldCursor:cursorWidth	F
    //   529: invokestatic 158	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   532: iadd
    //   533: putfield 318	android/graphics/Rect:right	I
    //   536: aload_0
    //   537: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   540: aload 11
    //   542: getfield 321	android/graphics/Rect:bottom	I
    //   545: putfield 321	android/graphics/Rect:bottom	I
    //   548: aload_0
    //   549: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   552: aload 11
    //   554: getfield 324	android/graphics/Rect:top	I
    //   557: putfield 324	android/graphics/Rect:top	I
    //   560: aload_0
    //   561: getfield 326	org/telegram/ui/Components/EditTextBoldCursor:lineSpacingExtra	F
    //   564: fconst_0
    //   565: fcmpl
    //   566: ifeq +34 -> 600
    //   569: iload_3
    //   570: iload 4
    //   572: iconst_1
    //   573: isub
    //   574: if_icmpge +26 -> 600
    //   577: aload_0
    //   578: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   581: astore 11
    //   583: aload 11
    //   585: aload 11
    //   587: getfield 321	android/graphics/Rect:bottom	I
    //   590: i2f
    //   591: aload_0
    //   592: getfield 326	org/telegram/ui/Components/EditTextBoldCursor:lineSpacingExtra	F
    //   595: fsub
    //   596: f2i
    //   597: putfield 321	android/graphics/Rect:bottom	I
    //   600: aload_0
    //   601: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   604: aload_0
    //   605: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   608: invokevirtual 329	android/graphics/Rect:centerY	()I
    //   611: aload_0
    //   612: getfield 160	org/telegram/ui/Components/EditTextBoldCursor:cursorSize	I
    //   615: iconst_2
    //   616: idiv
    //   617: isub
    //   618: putfield 324	android/graphics/Rect:top	I
    //   621: aload_0
    //   622: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   625: aload_0
    //   626: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   629: getfield 324	android/graphics/Rect:top	I
    //   632: aload_0
    //   633: getfield 160	org/telegram/ui/Components/EditTextBoldCursor:cursorSize	I
    //   636: iadd
    //   637: putfield 321	android/graphics/Rect:bottom	I
    //   640: aload_0
    //   641: getfield 131	org/telegram/ui/Components/EditTextBoldCursor:gradientDrawable	Landroid/graphics/drawable/GradientDrawable;
    //   644: aload_0
    //   645: getfield 52	org/telegram/ui/Components/EditTextBoldCursor:rect	Landroid/graphics/Rect;
    //   648: invokevirtual 333	android/graphics/drawable/GradientDrawable:setBounds	(Landroid/graphics/Rect;)V
    //   651: aload_0
    //   652: getfield 131	org/telegram/ui/Components/EditTextBoldCursor:gradientDrawable	Landroid/graphics/drawable/GradientDrawable;
    //   655: aload_1
    //   656: invokevirtual 334	android/graphics/drawable/GradientDrawable:draw	(Landroid/graphics/Canvas;)V
    //   659: aload_1
    //   660: invokevirtual 202	android/graphics/Canvas:restore	()V
    //   663: return
    //   664: aload_0
    //   665: aload_0
    //   666: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   669: lload 5
    //   671: l2f
    //   672: ldc -38
    //   674: fdiv
    //   675: fsub
    //   676: putfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   679: aload_0
    //   680: getfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   683: fconst_0
    //   684: fcmpg
    //   685: ifge -455 -> 230
    //   688: aload_0
    //   689: fconst_0
    //   690: putfield 56	org/telegram/ui/Components/EditTextBoldCursor:hintAlpha	F
    //   693: goto -463 -> 230
    //   696: iconst_0
    //   697: istore_3
    //   698: goto -292 -> 406
    //   701: astore 11
    //   703: goto -643 -> 60
    //   706: astore_1
    //   707: return
    //   708: astore 11
    //   710: goto -627 -> 83
    //   713: astore 11
    //   715: goto -682 -> 33
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	718	0	this	EditTextBoldCursor
    //   0	718	1	paramCanvas	android.graphics.Canvas
    //   284	10	2	f	float
    //   4	694	3	i	int
    //   241	333	4	j	int
    //   172	498	5	l1	long
    //   161	14	7	l2	long
    //   152	38	9	l3	long
    //   474	112	11	localObject	Object
    //   701	1	11	localException1	Exception
    //   708	1	11	localException2	Exception
    //   713	1	11	localException3	Exception
    // Exception table:
    //   from	to	target	type
    //   55	60	701	java/lang/Exception
    //   339	404	706	java/lang/Throwable
    //   410	415	706	java/lang/Throwable
    //   417	454	706	java/lang/Throwable
    //   454	569	706	java/lang/Throwable
    //   577	600	706	java/lang/Throwable
    //   600	663	706	java/lang/Throwable
    //   69	83	708	java/lang/Exception
    //   11	33	713	java/lang/Exception
  }
  
  public void setAllowDrawCursor(boolean paramBoolean)
  {
    this.allowDrawCursor = paramBoolean;
  }
  
  public void setCursorColor(int paramInt)
  {
    this.gradientDrawable.setColor(paramInt);
    invalidate();
  }
  
  public void setCursorSize(int paramInt)
  {
    this.cursorSize = paramInt;
  }
  
  public void setCursorWidth(float paramFloat)
  {
    this.cursorWidth = paramFloat;
  }
  
  public void setHintColor(int paramInt)
  {
    this.hintColor = paramInt;
    invalidate();
  }
  
  public void setHintText(String paramString)
  {
    this.hintLayout = new StaticLayout(paramString, getPaint(), AndroidUtilities.dp(1000.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
  }
  
  public void setHintVisible(boolean paramBoolean)
  {
    if (this.hintVisible == paramBoolean) {
      return;
    }
    this.lastUpdateTime = System.currentTimeMillis();
    this.hintVisible = paramBoolean;
    invalidate();
  }
  
  public void setLineSpacing(float paramFloat1, float paramFloat2)
  {
    super.setLineSpacing(paramFloat1, paramFloat2);
    this.lineSpacingExtra = paramFloat1;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/EditTextBoldCursor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */