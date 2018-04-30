package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.Components.LayoutHelper;

public class ThemeCell
  extends FrameLayout
{
  private static byte[] bytes = new byte['Ѐ'];
  private ImageView checkImage;
  private Theme.ThemeInfo currentThemeInfo;
  private boolean isNightTheme;
  private boolean needDivider;
  private ImageView optionsButton;
  private Paint paint;
  private TextView textView;
  
  public ThemeCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    setWillNotDraw(false);
    this.isNightTheme = paramBoolean;
    this.paint = new Paint(1);
    this.textView = new TextView(paramContext);
    this.textView.setTextSize(1, 16.0F);
    this.textView.setLines(1);
    this.textView.setMaxLines(1);
    this.textView.setSingleLine(true);
    this.textView.setPadding(0, 0, 0, AndroidUtilities.dp(1.0F));
    this.textView.setEllipsize(TextUtils.TruncateAt.END);
    Object localObject = this.textView;
    int j;
    label140:
    float f1;
    label149:
    float f2;
    if (LocaleController.isRTL)
    {
      j = 5;
      ((TextView)localObject).setGravity(j | 0x10);
      localObject = this.textView;
      if (!LocaleController.isRTL) {
        break label381;
      }
      j = 5;
      if (!LocaleController.isRTL) {
        break label387;
      }
      f1 = 101.0F;
      if (!LocaleController.isRTL) {
        break label393;
      }
      f2 = 60.0F;
      label159:
      addView((View)localObject, LayoutHelper.createFrame(-1, -1.0F, j | 0x30, f1, 0.0F, f2, 0.0F));
      this.checkImage = new ImageView(paramContext);
      this.checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
      this.checkImage.setImageResource(2131165652);
      if (this.isNightTheme) {
        break label412;
      }
      localObject = this.checkImage;
      if (!LocaleController.isRTL) {
        break label400;
      }
      j = 3;
      label246:
      addView((View)localObject, LayoutHelper.createFrame(19, 14.0F, j | 0x10, 55.0F, 0.0F, 55.0F, 0.0F));
      this.optionsButton = new ImageView(paramContext);
      this.optionsButton.setFocusable(false);
      this.optionsButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor("stickers_menuSelector")));
      this.optionsButton.setImageResource(2131165353);
      this.optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("stickers_menu"), PorterDuff.Mode.MULTIPLY));
      this.optionsButton.setScaleType(ImageView.ScaleType.CENTER);
      paramContext = this.optionsButton;
      if (!LocaleController.isRTL) {
        break label406;
      }
    }
    for (;;)
    {
      addView(paramContext, LayoutHelper.createFrame(48, 48, i | 0x30));
      return;
      j = 3;
      break;
      label381:
      j = 3;
      break label140;
      label387:
      f1 = 60.0F;
      break label149;
      label393:
      f2 = 101.0F;
      break label159;
      label400:
      j = 5;
      break label246;
      label406:
      i = 5;
    }
    label412:
    paramContext = this.checkImage;
    if (LocaleController.isRTL) {}
    for (;;)
    {
      addView(paramContext, LayoutHelper.createFrame(19, 14.0F, i | 0x10, 17.0F, 0.0F, 17.0F, 0.0F));
      return;
      i = 5;
    }
  }
  
  public Theme.ThemeInfo getCurrentThemeInfo()
  {
    return this.currentThemeInfo;
  }
  
  public TextView getTextView()
  {
    return this.textView;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.checkImage.setColorFilter(new PorterDuffColorFilter(Theme.getColor("featuredStickers_addedIcon"), PorterDuff.Mode.MULTIPLY));
    this.textView.setTextColor(Theme.getColor("windowBackgroundWhiteBlackText"));
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.needDivider) {
      paramCanvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
    }
    int j = AndroidUtilities.dp(27.0F);
    int i = j;
    if (LocaleController.isRTL) {
      i = getWidth() - j;
    }
    paramCanvas.drawCircle(i, AndroidUtilities.dp(24.0F), AndroidUtilities.dp(11.0F), this.paint);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    paramInt2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), 1073741824);
    int i = AndroidUtilities.dp(48.0F);
    if (this.needDivider) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      super.onMeasure(paramInt2, View.MeasureSpec.makeMeasureSpec(paramInt1 + i, 1073741824));
      return;
    }
  }
  
  public void setOnOptionsClick(View.OnClickListener paramOnClickListener)
  {
    this.optionsButton.setOnClickListener(paramOnClickListener);
  }
  
  public void setTextColor(int paramInt)
  {
    this.textView.setTextColor(paramInt);
  }
  
  /* Error */
  public void setTheme(Theme.ThemeInfo paramThemeInfo, boolean paramBoolean)
  {
    // Byte code:
    //   0: aload_0
    //   1: aload_1
    //   2: putfield 171	org/telegram/ui/Cells/ThemeCell:currentThemeInfo	Lorg/telegram/ui/ActionBar/Theme$ThemeInfo;
    //   5: aload_1
    //   6: invokevirtual 247	org/telegram/ui/ActionBar/Theme$ThemeInfo:getName	()Ljava/lang/String;
    //   9: astore 15
    //   11: aload 15
    //   13: astore 14
    //   15: aload 15
    //   17: ldc -7
    //   19: invokevirtual 255	java/lang/String:endsWith	(Ljava/lang/String;)Z
    //   22: ifeq +18 -> 40
    //   25: aload 15
    //   27: iconst_0
    //   28: aload 15
    //   30: bipush 46
    //   32: invokevirtual 258	java/lang/String:lastIndexOf	(I)I
    //   35: invokevirtual 262	java/lang/String:substring	(II)Ljava/lang/String;
    //   38: astore 14
    //   40: aload_0
    //   41: getfield 46	org/telegram/ui/Cells/ThemeCell:textView	Landroid/widget/TextView;
    //   44: aload 14
    //   46: invokevirtual 266	android/widget/TextView:setText	(Ljava/lang/CharSequence;)V
    //   49: aload_0
    //   50: iload_2
    //   51: putfield 185	org/telegram/ui/Cells/ThemeCell:needDivider	Z
    //   54: aload_0
    //   55: invokevirtual 269	org/telegram/ui/Cells/ThemeCell:updateCurrentThemeCheck	()V
    //   58: iconst_0
    //   59: istore_3
    //   60: iconst_0
    //   61: istore 5
    //   63: iconst_0
    //   64: istore 4
    //   66: aload_1
    //   67: getfield 273	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
    //   70: ifnonnull +10 -> 80
    //   73: aload_1
    //   74: getfield 276	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   77: ifnull +228 -> 305
    //   80: aconst_null
    //   81: astore 15
    //   83: aconst_null
    //   84: astore 16
    //   86: iconst_0
    //   87: istore 6
    //   89: aload 15
    //   91: astore 14
    //   93: aload_1
    //   94: getfield 276	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   97: ifnull +226 -> 323
    //   100: aload 15
    //   102: astore 14
    //   104: aload_1
    //   105: getfield 276	org/telegram/ui/ActionBar/Theme$ThemeInfo:assetName	Ljava/lang/String;
    //   108: invokestatic 280	org/telegram/ui/ActionBar/Theme:getAssetFile	(Ljava/lang/String;)Ljava/io/File;
    //   111: astore_1
    //   112: aload 15
    //   114: astore 14
    //   116: new 282	java/io/FileInputStream
    //   119: dup
    //   120: aload_1
    //   121: invokespecial 285	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   124: astore_1
    //   125: iconst_0
    //   126: istore 7
    //   128: iload 4
    //   130: istore_3
    //   131: iload 6
    //   133: istore 9
    //   135: iload_3
    //   136: istore 4
    //   138: aload_1
    //   139: getstatic 22	org/telegram/ui/Cells/ThemeCell:bytes	[B
    //   142: invokevirtual 289	java/io/FileInputStream:read	([B)I
    //   145: istore 13
    //   147: iload_3
    //   148: istore 4
    //   150: iload 13
    //   152: iconst_m1
    //   153: if_icmpeq +138 -> 291
    //   156: iconst_0
    //   157: istore 11
    //   159: iconst_0
    //   160: istore 10
    //   162: iload 9
    //   164: istore 5
    //   166: iload 5
    //   168: istore 6
    //   170: iload_3
    //   171: istore 5
    //   173: iload 7
    //   175: istore 8
    //   177: iload 10
    //   179: iload 13
    //   181: if_icmpge +87 -> 268
    //   184: iload_3
    //   185: istore 4
    //   187: iload 6
    //   189: istore 5
    //   191: iload 7
    //   193: istore 8
    //   195: iload 11
    //   197: istore 12
    //   199: getstatic 22	org/telegram/ui/Cells/ThemeCell:bytes	[B
    //   202: iload 10
    //   204: baload
    //   205: bipush 10
    //   207: if_icmpne +421 -> 628
    //   210: iload 7
    //   212: iconst_1
    //   213: iadd
    //   214: istore 8
    //   216: iload 10
    //   218: iload 11
    //   220: isub
    //   221: iconst_1
    //   222: iadd
    //   223: istore 5
    //   225: iload_3
    //   226: istore 4
    //   228: new 251	java/lang/String
    //   231: dup
    //   232: getstatic 22	org/telegram/ui/Cells/ThemeCell:bytes	[B
    //   235: iload 11
    //   237: iload 5
    //   239: iconst_1
    //   240: isub
    //   241: ldc_w 291
    //   244: invokespecial 294	java/lang/String:<init>	([BIILjava/lang/String;)V
    //   247: astore 14
    //   249: iload_3
    //   250: istore 4
    //   252: aload 14
    //   254: ldc_w 296
    //   257: invokevirtual 299	java/lang/String:startsWith	(Ljava/lang/String;)Z
    //   260: istore_2
    //   261: iload_2
    //   262: ifeq +80 -> 342
    //   265: iload_3
    //   266: istore 5
    //   268: iload 5
    //   270: istore 4
    //   272: iload 9
    //   274: iload 6
    //   276: if_icmpeq +15 -> 291
    //   279: iload 8
    //   281: sipush 500
    //   284: if_icmplt +239 -> 523
    //   287: iload 5
    //   289: istore 4
    //   291: iload 4
    //   293: istore_3
    //   294: aload_1
    //   295: ifnull +10 -> 305
    //   298: aload_1
    //   299: invokevirtual 302	java/io/FileInputStream:close	()V
    //   302: iload 4
    //   304: istore_3
    //   305: iload_3
    //   306: ifne +16 -> 322
    //   309: aload_0
    //   310: getfield 41	org/telegram/ui/Cells/ThemeCell:paint	Landroid/graphics/Paint;
    //   313: ldc_w 304
    //   316: invokestatic 307	org/telegram/ui/ActionBar/Theme:getDefaultColor	(Ljava/lang/String;)I
    //   319: invokevirtual 310	android/graphics/Paint:setColor	(I)V
    //   322: return
    //   323: aload 15
    //   325: astore 14
    //   327: new 312	java/io/File
    //   330: dup
    //   331: aload_1
    //   332: getfield 273	org/telegram/ui/ActionBar/Theme$ThemeInfo:pathToFile	Ljava/lang/String;
    //   335: invokespecial 315	java/io/File:<init>	(Ljava/lang/String;)V
    //   338: astore_1
    //   339: goto -227 -> 112
    //   342: iload_3
    //   343: istore 4
    //   345: aload 14
    //   347: bipush 61
    //   349: invokevirtual 318	java/lang/String:indexOf	(I)I
    //   352: istore 7
    //   354: iload 7
    //   356: iconst_m1
    //   357: if_icmpeq +257 -> 614
    //   360: iload_3
    //   361: istore 4
    //   363: aload 14
    //   365: iconst_0
    //   366: iload 7
    //   368: invokevirtual 262	java/lang/String:substring	(II)Ljava/lang/String;
    //   371: ldc_w 304
    //   374: invokevirtual 322	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   377: ifeq +237 -> 614
    //   380: iload_3
    //   381: istore 4
    //   383: aload 14
    //   385: iload 7
    //   387: iconst_1
    //   388: iadd
    //   389: invokevirtual 325	java/lang/String:substring	(I)Ljava/lang/String;
    //   392: astore 14
    //   394: iload_3
    //   395: istore 4
    //   397: aload 14
    //   399: invokevirtual 328	java/lang/String:length	()I
    //   402: ifle +106 -> 508
    //   405: iload_3
    //   406: istore 4
    //   408: aload 14
    //   410: iconst_0
    //   411: invokevirtual 332	java/lang/String:charAt	(I)C
    //   414: istore 5
    //   416: iload 5
    //   418: bipush 35
    //   420: if_icmpne +88 -> 508
    //   423: iload_3
    //   424: istore 4
    //   426: aload 14
    //   428: invokestatic 337	android/graphics/Color:parseColor	(Ljava/lang/String;)I
    //   431: istore 5
    //   433: iload 5
    //   435: istore_3
    //   436: iconst_1
    //   437: istore 4
    //   439: iconst_1
    //   440: istore 5
    //   442: aload_0
    //   443: getfield 41	org/telegram/ui/Cells/ThemeCell:paint	Landroid/graphics/Paint;
    //   446: iload_3
    //   447: invokevirtual 310	android/graphics/Paint:setColor	(I)V
    //   450: goto -182 -> 268
    //   453: astore 15
    //   455: aload_1
    //   456: astore 14
    //   458: aload 15
    //   460: invokestatic 343	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   463: iload 4
    //   465: istore_3
    //   466: aload_1
    //   467: ifnull -162 -> 305
    //   470: aload_1
    //   471: invokevirtual 302	java/io/FileInputStream:close	()V
    //   474: iload 4
    //   476: istore_3
    //   477: goto -172 -> 305
    //   480: astore_1
    //   481: aload_1
    //   482: invokestatic 343	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   485: iload 4
    //   487: istore_3
    //   488: goto -183 -> 305
    //   491: astore 15
    //   493: iload_3
    //   494: istore 4
    //   496: aload 14
    //   498: invokestatic 349	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   501: invokevirtual 354	java/lang/Integer:intValue	()I
    //   504: istore_3
    //   505: goto -69 -> 436
    //   508: iload_3
    //   509: istore 4
    //   511: aload 14
    //   513: invokestatic 349	org/telegram/messenger/Utilities:parseInt	(Ljava/lang/String;)Ljava/lang/Integer;
    //   516: invokevirtual 354	java/lang/Integer:intValue	()I
    //   519: istore_3
    //   520: goto -84 -> 436
    //   523: iload 5
    //   525: istore 4
    //   527: aload_1
    //   528: invokevirtual 358	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   531: iload 6
    //   533: i2l
    //   534: invokevirtual 364	java/nio/channels/FileChannel:position	(J)Ljava/nio/channels/FileChannel;
    //   537: pop
    //   538: iload 5
    //   540: istore_3
    //   541: iload 8
    //   543: istore 7
    //   545: iload 5
    //   547: ifeq -416 -> 131
    //   550: iload 5
    //   552: istore 4
    //   554: goto -263 -> 291
    //   557: astore_1
    //   558: aload_1
    //   559: invokestatic 343	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   562: iload 4
    //   564: istore_3
    //   565: goto -260 -> 305
    //   568: astore_1
    //   569: aload 14
    //   571: ifnull +8 -> 579
    //   574: aload 14
    //   576: invokevirtual 302	java/io/FileInputStream:close	()V
    //   579: aload_1
    //   580: athrow
    //   581: astore 14
    //   583: aload 14
    //   585: invokestatic 343	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   588: goto -9 -> 579
    //   591: astore 15
    //   593: aload_1
    //   594: astore 14
    //   596: aload 15
    //   598: astore_1
    //   599: goto -30 -> 569
    //   602: astore 15
    //   604: iload 5
    //   606: istore 4
    //   608: aload 16
    //   610: astore_1
    //   611: goto -156 -> 455
    //   614: iload 11
    //   616: iload 5
    //   618: iadd
    //   619: istore 12
    //   621: iload 6
    //   623: iload 5
    //   625: iadd
    //   626: istore 5
    //   628: iload 10
    //   630: iconst_1
    //   631: iadd
    //   632: istore 10
    //   634: iload 8
    //   636: istore 7
    //   638: iload 12
    //   640: istore 11
    //   642: goto -476 -> 166
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	645	0	this	ThemeCell
    //   0	645	1	paramThemeInfo	Theme.ThemeInfo
    //   0	645	2	paramBoolean	boolean
    //   59	506	3	i	int
    //   64	543	4	j	int
    //   61	566	5	k	int
    //   87	539	6	m	int
    //   126	511	7	n	int
    //   175	460	8	i1	int
    //   133	144	9	i2	int
    //   160	473	10	i3	int
    //   157	484	11	i4	int
    //   197	442	12	i5	int
    //   145	37	13	i6	int
    //   13	562	14	localObject1	Object
    //   581	3	14	localException1	Exception
    //   594	1	14	localThemeInfo	Theme.ThemeInfo
    //   9	315	15	str	String
    //   453	6	15	localThrowable1	Throwable
    //   491	1	15	localException2	Exception
    //   591	6	15	localObject2	Object
    //   602	1	15	localThrowable2	Throwable
    //   84	525	16	localObject3	Object
    // Exception table:
    //   from	to	target	type
    //   138	147	453	java/lang/Throwable
    //   199	210	453	java/lang/Throwable
    //   228	249	453	java/lang/Throwable
    //   252	261	453	java/lang/Throwable
    //   345	354	453	java/lang/Throwable
    //   363	380	453	java/lang/Throwable
    //   383	394	453	java/lang/Throwable
    //   397	405	453	java/lang/Throwable
    //   408	416	453	java/lang/Throwable
    //   426	433	453	java/lang/Throwable
    //   442	450	453	java/lang/Throwable
    //   496	505	453	java/lang/Throwable
    //   511	520	453	java/lang/Throwable
    //   527	538	453	java/lang/Throwable
    //   470	474	480	java/lang/Exception
    //   426	433	491	java/lang/Exception
    //   298	302	557	java/lang/Exception
    //   93	100	568	finally
    //   104	112	568	finally
    //   116	125	568	finally
    //   327	339	568	finally
    //   458	463	568	finally
    //   574	579	581	java/lang/Exception
    //   138	147	591	finally
    //   199	210	591	finally
    //   228	249	591	finally
    //   252	261	591	finally
    //   345	354	591	finally
    //   363	380	591	finally
    //   383	394	591	finally
    //   397	405	591	finally
    //   408	416	591	finally
    //   426	433	591	finally
    //   442	450	591	finally
    //   496	505	591	finally
    //   511	520	591	finally
    //   527	538	591	finally
    //   93	100	602	java/lang/Throwable
    //   104	112	602	java/lang/Throwable
    //   116	125	602	java/lang/Throwable
    //   327	339	602	java/lang/Throwable
  }
  
  public void updateCurrentThemeCheck()
  {
    Theme.ThemeInfo localThemeInfo;
    if (this.isNightTheme)
    {
      localThemeInfo = Theme.getCurrentNightTheme();
      if (this.currentThemeInfo != localThemeInfo) {
        break label48;
      }
    }
    label48:
    for (int i = 0;; i = 4)
    {
      if (this.checkImage.getVisibility() != i) {
        this.checkImage.setVisibility(i);
      }
      return;
      localThemeInfo = Theme.getCurrentTheme();
      break;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/ThemeCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */