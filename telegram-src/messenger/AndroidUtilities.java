package org.telegram.messenger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video.Media;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.EdgeEffectCompat;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EdgeEffect;
import android.widget.ListView;
import android.widget.ScrollView;
import com.android.internal.telephony.ITelephony;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Pattern;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.Components.TypefaceSpan;

public class AndroidUtilities
{
  public static final int FLAG_TAG_ALL = 3;
  public static final int FLAG_TAG_BOLD = 2;
  public static final int FLAG_TAG_BR = 1;
  public static final int FLAG_TAG_COLOR = 4;
  public static Pattern WEB_URL = null;
  public static AccelerateInterpolator accelerateInterpolator;
  private static int adjustOwnerClassGuid;
  private static RectF bitmapRect;
  private static final Object callLock;
  private static ContentObserver callLogContentObserver;
  public static DecelerateInterpolator decelerateInterpolator;
  public static float density;
  public static DisplayMetrics displayMetrics;
  public static Point displaySize;
  private static boolean hasCallPermissions;
  public static boolean incorrectDisplaySizeFix;
  public static boolean isInMultiwindow;
  private static Boolean isTablet;
  public static int leftBaseline;
  private static Field mAttachInfoField;
  private static Field mStableInsetsField;
  public static OvershootInterpolator overshootInterpolator;
  public static Integer photoSize;
  private static int prevOrientation;
  public static int roundMessageSize;
  private static Paint roundPaint;
  private static final Object smsLock;
  public static int statusBarHeight;
  private static final Hashtable<String, Typeface> typefaceCache = new Hashtable();
  private static Runnable unregisterRunnable;
  public static boolean usingHardwareInput;
  private static boolean waitingForCall;
  private static boolean waitingForSms;
  
  static
  {
    prevOrientation = -10;
    waitingForSms = false;
    waitingForCall = false;
    smsLock = new Object();
    callLock = new Object();
    statusBarHeight = 0;
    density = 1.0F;
    displaySize = new Point();
    photoSize = null;
    displayMetrics = new DisplayMetrics();
    decelerateInterpolator = new DecelerateInterpolator();
    accelerateInterpolator = new AccelerateInterpolator();
    overshootInterpolator = new OvershootInterpolator();
    isTablet = null;
    adjustOwnerClassGuid = 0;
    try
    {
      Pattern localPattern = Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))");
      localPattern = Pattern.compile("(([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]([a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\-]{0,61}[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯]){0,1}\\.)+[a-zA-Z -퟿豈-﷏ﷰ-￯]{2,63}|" + localPattern + ")");
      WEB_URL = Pattern.compile("((?:(http|https|Http|Https):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?(?:" + localPattern + ")(?:\\:\\d{1,5})?)(\\/(?:(?:[" + "a-zA-Z0-9 -퟿豈-﷏ﷰ-￯" + "\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)");
      if (isTablet())
      {
        i = 80;
        leftBaseline = i;
        checkDisplaySize(ApplicationLoader.applicationContext, null);
        if (Build.VERSION.SDK_INT < 23) {
          break label240;
        }
        bool = true;
        hasCallPermissions = bool;
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
        continue;
        int i = 72;
        continue;
        label240:
        boolean bool = false;
      }
    }
  }
  
  public static void addMediaToGallery(Uri paramUri)
  {
    if (paramUri == null) {
      return;
    }
    try
    {
      Intent localIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
      localIntent.setData(paramUri);
      ApplicationLoader.applicationContext.sendBroadcast(localIntent);
      return;
    }
    catch (Exception paramUri)
    {
      FileLog.e(paramUri);
    }
  }
  
  public static void addMediaToGallery(String paramString)
  {
    if (paramString == null) {
      return;
    }
    addMediaToGallery(Uri.fromFile(new File(paramString)));
  }
  
  public static void addToClipboard(CharSequence paramCharSequence)
  {
    try
    {
      ((ClipboardManager)ApplicationLoader.applicationContext.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", paramCharSequence));
      return;
    }
    catch (Exception paramCharSequence)
    {
      FileLog.e(paramCharSequence);
    }
  }
  
  public static byte[] calcAuthKeyHash(byte[] paramArrayOfByte)
  {
    paramArrayOfByte = Utilities.computeSHA1(paramArrayOfByte);
    byte[] arrayOfByte = new byte[16];
    System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, 16);
    return arrayOfByte;
  }
  
  public static int[] calcDrawableColor(Drawable paramDrawable)
  {
    int k = -16777216;
    j = k;
    for (;;)
    {
      try
      {
        if (!(paramDrawable instanceof BitmapDrawable)) {
          continue;
        }
        j = k;
        paramDrawable = ((BitmapDrawable)paramDrawable).getBitmap();
        i = k;
        if (paramDrawable != null)
        {
          j = k;
          Bitmap localBitmap = Bitmaps.createScaledBitmap(paramDrawable, 1, 1, true);
          i = k;
          if (localBitmap != null)
          {
            j = k;
            k = localBitmap.getPixel(0, 0);
            i = k;
            if (paramDrawable != localBitmap)
            {
              j = k;
              localBitmap.recycle();
              i = k;
            }
          }
        }
      }
      catch (Exception paramDrawable)
      {
        FileLog.e(paramDrawable);
        int i = j;
        continue;
      }
      paramDrawable = rgbToHsv(i >> 16 & 0xFF, i >> 8 & 0xFF, i & 0xFF);
      paramDrawable[1] = Math.min(1.0D, paramDrawable[1] + 0.05D + 0.1D * (1.0D - paramDrawable[1]));
      paramDrawable[2] = Math.max(0.0D, paramDrawable[2] * 0.65D);
      paramDrawable = hsvToRgb(paramDrawable[0], paramDrawable[1], paramDrawable[2]);
      return new int[] { Color.argb(102, paramDrawable[0], paramDrawable[1], paramDrawable[2]), Color.argb(136, paramDrawable[0], paramDrawable[1], paramDrawable[2]) };
      i = k;
      j = k;
      if ((paramDrawable instanceof ColorDrawable))
      {
        j = k;
        i = ((ColorDrawable)paramDrawable).getColor();
      }
    }
  }
  
  public static void cancelRunOnUIThread(Runnable paramRunnable)
  {
    ApplicationLoader.applicationHandler.removeCallbacks(paramRunnable);
  }
  
  public static void checkDisplaySize(Context paramContext, Configuration paramConfiguration)
  {
    for (boolean bool = true;; bool = false) {
      try
      {
        density = paramContext.getResources().getDisplayMetrics().density;
        Configuration localConfiguration = paramConfiguration;
        paramConfiguration = localConfiguration;
        if (localConfiguration == null) {
          paramConfiguration = paramContext.getResources().getConfiguration();
        }
        if ((paramConfiguration.keyboard != 1) && (paramConfiguration.hardKeyboardHidden == 1))
        {
          usingHardwareInput = bool;
          paramContext = (WindowManager)paramContext.getSystemService("window");
          if (paramContext != null)
          {
            paramContext = paramContext.getDefaultDisplay();
            if (paramContext != null)
            {
              paramContext.getMetrics(displayMetrics);
              paramContext.getSize(displaySize);
            }
          }
          int i;
          if (paramConfiguration.screenWidthDp != 0)
          {
            i = (int)Math.ceil(paramConfiguration.screenWidthDp * density);
            if (Math.abs(displaySize.x - i) > 3) {
              displaySize.x = i;
            }
          }
          if (paramConfiguration.screenHeightDp != 0)
          {
            i = (int)Math.ceil(paramConfiguration.screenHeightDp * density);
            if (Math.abs(displaySize.y - i) > 3) {
              displaySize.y = i;
            }
          }
          if (roundMessageSize == 0) {
            if (!isTablet()) {
              break label286;
            }
          }
          label286:
          for (roundMessageSize = (int)(getMinTabletSide() * 0.6F); BuildVars.LOGS_ENABLED; roundMessageSize = (int)(Math.min(displaySize.x, displaySize.y) * 0.6F))
          {
            FileLog.e("display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
            return;
          }
          return;
        }
      }
      catch (Exception paramContext)
      {
        FileLog.e(paramContext);
      }
    }
  }
  
  public static void checkForCrashes(Activity paramActivity)
  {
    if (BuildVars.DEBUG_VERSION) {}
    for (String str = BuildVars.HOCKEY_APP_HASH_DEBUG;; str = BuildVars.HOCKEY_APP_HASH)
    {
      CrashManager.register(paramActivity, str, new CrashManagerListener()
      {
        public boolean includeDeviceData()
        {
          return true;
        }
      });
      return;
    }
  }
  
  public static void checkForUpdates(Activity paramActivity)
  {
    if (BuildVars.DEBUG_VERSION) {
      if (!BuildVars.DEBUG_VERSION) {
        break label22;
      }
    }
    label22:
    for (String str = BuildVars.HOCKEY_APP_HASH_DEBUG;; str = BuildVars.HOCKEY_APP_HASH)
    {
      UpdateManager.register(paramActivity, str);
      return;
    }
  }
  
  public static boolean checkPhonePattern(String paramString1, String paramString2)
  {
    if ((TextUtils.isEmpty(paramString1)) || (paramString1.equals("*"))) {}
    for (;;)
    {
      return true;
      paramString1 = paramString1.split("\\*");
      paramString2 = PhoneFormat.stripExceptNumbers(paramString2);
      int j = 0;
      int i = 0;
      while (i < paramString1.length)
      {
        CharSequence localCharSequence = paramString1[i];
        int k = j;
        if (!TextUtils.isEmpty(localCharSequence))
        {
          j = paramString2.indexOf(localCharSequence, j);
          if (j == -1) {
            return false;
          }
          k = j + localCharSequence.length();
        }
        i += 1;
        j = k;
      }
    }
  }
  
  @SuppressLint({"NewApi"})
  public static void clearDrawableAnimation(View paramView)
  {
    if ((Build.VERSION.SDK_INT < 21) || (paramView == null)) {}
    do
    {
      do
      {
        return;
        if (!(paramView instanceof ListView)) {
          break;
        }
        paramView = ((ListView)paramView).getSelector();
      } while (paramView == null);
      paramView.setState(StateSet.NOTHING);
      return;
      paramView = paramView.getBackground();
    } while (paramView == null);
    paramView.setState(StateSet.NOTHING);
    paramView.jumpToCurrentState();
  }
  
  public static int compare(int paramInt1, int paramInt2)
  {
    if (paramInt1 == paramInt2) {
      return 0;
    }
    if (paramInt1 > paramInt2) {
      return 1;
    }
    return -1;
  }
  
  /* Error */
  public static boolean copyFile(File paramFile1, File paramFile2)
    throws IOException
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 535	java/io/File:exists	()Z
    //   4: ifne +8 -> 12
    //   7: aload_1
    //   8: invokevirtual 538	java/io/File:createNewFile	()Z
    //   11: pop
    //   12: aconst_null
    //   13: astore 4
    //   15: aconst_null
    //   16: astore 7
    //   18: aconst_null
    //   19: astore_3
    //   20: aconst_null
    //   21: astore 5
    //   23: aconst_null
    //   24: astore 6
    //   26: new 540	java/io/FileInputStream
    //   29: dup
    //   30: aload_0
    //   31: invokespecial 543	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   34: astore_0
    //   35: new 545	java/io/FileOutputStream
    //   38: dup
    //   39: aload_1
    //   40: invokespecial 546	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   43: astore_1
    //   44: aload_1
    //   45: invokevirtual 550	java/io/FileOutputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   48: aload_0
    //   49: invokevirtual 551	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   52: lconst_0
    //   53: aload_0
    //   54: invokevirtual 551	java/io/FileInputStream:getChannel	()Ljava/nio/channels/FileChannel;
    //   57: invokevirtual 557	java/nio/channels/FileChannel:size	()J
    //   60: invokevirtual 561	java/nio/channels/FileChannel:transferFrom	(Ljava/nio/channels/ReadableByteChannel;JJ)J
    //   63: pop2
    //   64: aload_0
    //   65: ifnull +7 -> 72
    //   68: aload_0
    //   69: invokevirtual 564	java/io/FileInputStream:close	()V
    //   72: aload_1
    //   73: ifnull +7 -> 80
    //   76: aload_1
    //   77: invokevirtual 565	java/io/FileOutputStream:close	()V
    //   80: iconst_1
    //   81: istore_2
    //   82: iload_2
    //   83: ireturn
    //   84: astore 5
    //   86: aload 7
    //   88: astore_0
    //   89: aload 6
    //   91: astore_1
    //   92: aload_1
    //   93: astore_3
    //   94: aload_0
    //   95: astore 4
    //   97: aload 5
    //   99: invokestatic 195	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   102: iconst_0
    //   103: istore_2
    //   104: aload_0
    //   105: ifnull +7 -> 112
    //   108: aload_0
    //   109: invokevirtual 564	java/io/FileInputStream:close	()V
    //   112: aload_1
    //   113: ifnull -31 -> 82
    //   116: aload_1
    //   117: invokevirtual 565	java/io/FileOutputStream:close	()V
    //   120: iconst_0
    //   121: ireturn
    //   122: astore_0
    //   123: aload 4
    //   125: ifnull +8 -> 133
    //   128: aload 4
    //   130: invokevirtual 564	java/io/FileInputStream:close	()V
    //   133: aload_3
    //   134: ifnull +7 -> 141
    //   137: aload_3
    //   138: invokevirtual 565	java/io/FileOutputStream:close	()V
    //   141: aload_0
    //   142: athrow
    //   143: astore_1
    //   144: aload_0
    //   145: astore 4
    //   147: aload_1
    //   148: astore_0
    //   149: aload 5
    //   151: astore_3
    //   152: goto -29 -> 123
    //   155: astore_3
    //   156: aload_0
    //   157: astore 4
    //   159: aload_3
    //   160: astore_0
    //   161: aload_1
    //   162: astore_3
    //   163: goto -40 -> 123
    //   166: astore 5
    //   168: aload 6
    //   170: astore_1
    //   171: goto -79 -> 92
    //   174: astore 5
    //   176: goto -84 -> 92
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	179	0	paramFile1	File
    //   0	179	1	paramFile2	File
    //   81	23	2	bool	boolean
    //   19	133	3	localObject1	Object
    //   155	5	3	localObject2	Object
    //   162	1	3	localFile1	File
    //   13	145	4	localFile2	File
    //   21	1	5	localObject3	Object
    //   84	66	5	localException1	Exception
    //   166	1	5	localException2	Exception
    //   174	1	5	localException3	Exception
    //   24	145	6	localObject4	Object
    //   16	71	7	localObject5	Object
    // Exception table:
    //   from	to	target	type
    //   26	35	84	java/lang/Exception
    //   26	35	122	finally
    //   97	102	122	finally
    //   35	44	143	finally
    //   44	64	155	finally
    //   35	44	166	java/lang/Exception
    //   44	64	174	java/lang/Exception
  }
  
  public static boolean copyFile(InputStream paramInputStream, File paramFile)
    throws IOException
  {
    paramFile = new FileOutputStream(paramFile);
    byte[] arrayOfByte = new byte['က'];
    for (;;)
    {
      int i = paramInputStream.read(arrayOfByte);
      if (i <= 0) {
        break;
      }
      Thread.yield();
      paramFile.write(arrayOfByte, 0, i);
    }
    paramFile.close();
    return true;
  }
  
  public static byte[] decodeQuotedPrintable(byte[] paramArrayOfByte)
  {
    if (paramArrayOfByte == null) {
      return null;
    }
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    int i = 0;
    if (i < paramArrayOfByte.length)
    {
      int j = paramArrayOfByte[i];
      if (j == 61) {
        i += 1;
      }
      for (;;)
      {
        try
        {
          j = Character.digit((char)paramArrayOfByte[i], 16);
          i += 1;
          localByteArrayOutputStream.write((char)((j << 4) + Character.digit((char)paramArrayOfByte[i], 16)));
          i += 1;
        }
        catch (Exception paramArrayOfByte)
        {
          FileLog.e(paramArrayOfByte);
          return null;
        }
        localByteArrayOutputStream.write(j);
      }
    }
    paramArrayOfByte = localByteArrayOutputStream.toByteArray();
    try
    {
      localByteArrayOutputStream.close();
      return paramArrayOfByte;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return paramArrayOfByte;
  }
  
  public static int dp(float paramFloat)
  {
    if (paramFloat == 0.0F) {
      return 0;
    }
    return (int)Math.ceil(density * paramFloat);
  }
  
  public static int dp2(float paramFloat)
  {
    if (paramFloat == 0.0F) {
      return 0;
    }
    return (int)Math.floor(density * paramFloat);
  }
  
  public static float dpf2(float paramFloat)
  {
    if (paramFloat == 0.0F) {
      return 0.0F;
    }
    return density * paramFloat;
  }
  
  public static void endIncomingCall()
  {
    if (!hasCallPermissions) {
      return;
    }
    try
    {
      Object localObject = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
      Method localMethod = Class.forName(localObject.getClass().getName()).getDeclaredMethod("getITelephony", new Class[0]);
      localMethod.setAccessible(true);
      ITelephony localITelephony = (ITelephony)localMethod.invoke(localObject, new Object[0]);
      localObject = (ITelephony)localMethod.invoke(localObject, new Object[0]);
      ((ITelephony)localObject).silenceRinger();
      ((ITelephony)localObject).endCall();
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static String formatFileSize(long paramLong)
  {
    if (paramLong < 1024L) {
      return String.format("%d B", new Object[] { Long.valueOf(paramLong) });
    }
    if (paramLong < 1048576L) {
      return String.format("%.1f KB", new Object[] { Float.valueOf((float)paramLong / 1024.0F) });
    }
    if (paramLong < 1073741824L) {
      return String.format("%.1f MB", new Object[] { Float.valueOf((float)paramLong / 1024.0F / 1024.0F) });
    }
    return String.format("%.1f GB", new Object[] { Float.valueOf((float)paramLong / 1024.0F / 1024.0F / 1024.0F) });
  }
  
  public static File generatePicturePath()
  {
    try
    {
      File localFile = getAlbumDir();
      Object localObject = new Date();
      ((Date)localObject).setTime(System.currentTimeMillis() + Utilities.random.nextInt(1000) + 1L);
      localObject = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format((Date)localObject);
      localFile = new File(localFile, "IMG_" + (String)localObject + ".jpg");
      return localFile;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return null;
  }
  
  public static CharSequence generateSearchName(String paramString1, String paramString2, String paramString3)
  {
    if ((paramString1 == null) && (paramString2 == null)) {
      paramString1 = "";
    }
    SpannableStringBuilder localSpannableStringBuilder;
    int i;
    label72:
    label113:
    label134:
    label307:
    label313:
    label319:
    label344:
    do
    {
      do
      {
        return paramString1;
        localSpannableStringBuilder = new SpannableStringBuilder();
        String str = paramString1;
        int j;
        int k;
        if ((str == null) || (str.length() == 0))
        {
          paramString1 = paramString2;
          paramString2 = paramString1.trim();
          paramString1 = " " + paramString2.toLowerCase();
          i = 0;
          int m = paramString1.indexOf(" " + paramString3, i);
          if (m == -1) {
            break label344;
          }
          if (m != 0) {
            break label307;
          }
          j = 0;
          k = m - j;
          int n = paramString3.length();
          if (m != 0) {
            break label313;
          }
          j = 0;
          j = j + n + k;
          if ((i == 0) || (i == k + 1)) {
            break label319;
          }
          localSpannableStringBuilder.append(paramString2.substring(i, k));
        }
        for (;;)
        {
          str = paramString2.substring(k, Math.min(paramString2.length(), j));
          if (str.startsWith(" ")) {
            localSpannableStringBuilder.append(" ");
          }
          str = str.trim();
          i = localSpannableStringBuilder.length();
          localSpannableStringBuilder.append(str);
          localSpannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), i, str.length() + i, 33);
          i = j;
          break label72;
          paramString1 = str;
          if (paramString2 == null) {
            break;
          }
          paramString1 = str;
          if (paramString2.length() == 0) {
            break;
          }
          paramString1 = str + " " + paramString2;
          break;
          j = 1;
          break label113;
          j = 1;
          break label134;
          if ((i == 0) && (k != 0)) {
            localSpannableStringBuilder.append(paramString2.substring(0, k));
          }
        }
        paramString1 = localSpannableStringBuilder;
      } while (i == -1);
      paramString1 = localSpannableStringBuilder;
    } while (i >= paramString2.length());
    localSpannableStringBuilder.append(paramString2.substring(i, paramString2.length()));
    return localSpannableStringBuilder;
  }
  
  public static File generateVideoPath()
  {
    try
    {
      File localFile = getAlbumDir();
      Object localObject = new Date();
      ((Date)localObject).setTime(System.currentTimeMillis() + Utilities.random.nextInt(1000) + 1L);
      localObject = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format((Date)localObject);
      localFile = new File(localFile, "VID_" + (String)localObject + ".mp4");
      return localFile;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return null;
  }
  
  private static File getAlbumDir()
  {
    Object localObject;
    if ((Build.VERSION.SDK_INT >= 23) && (ApplicationLoader.applicationContext.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0)) {
      localObject = FileLoader.getDirectory(4);
    }
    do
    {
      File localFile;
      do
      {
        do
        {
          return (File)localObject;
          localObject = null;
          if (!"mounted".equals(Environment.getExternalStorageState())) {
            break;
          }
          localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Telegram");
          localObject = localFile;
        } while (localFile.mkdirs());
        localObject = localFile;
      } while (localFile.exists());
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("failed to create directory");
      }
      return null;
    } while (!BuildVars.LOGS_ENABLED);
    FileLog.d("External storage is not mounted READ/WRITE.");
    return null;
  }
  
  public static File getCacheDir()
  {
    Object localObject1 = null;
    try
    {
      String str = Environment.getExternalStorageState();
      localObject1 = str;
    }
    catch (Exception localException3)
    {
      for (;;)
      {
        try
        {
          localObject1 = ApplicationLoader.applicationContext.getExternalCacheDir();
          if (localObject1 == null) {
            break;
          }
          return (File)localObject1;
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
        }
        localException3 = localException3;
        FileLog.e(localException3);
      }
    }
    if ((localObject1 == null) || (((String)localObject1).startsWith("mounted"))) {}
    try
    {
      File localFile;
      do
      {
        localFile = ApplicationLoader.applicationContext.getCacheDir();
        Object localObject2 = localFile;
      } while (localFile != null);
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        FileLog.e(localException2);
      }
    }
    return new File("");
  }
  
  public static String getDataColumn(Context paramContext, Uri paramUri, String paramString, String[] paramArrayOfString)
  {
    localContext2 = null;
    localContext1 = null;
    try
    {
      paramContext = paramContext.getContentResolver().query(paramUri, new String[] { "_data" }, paramString, paramArrayOfString, null);
      if (paramContext != null)
      {
        localContext1 = paramContext;
        localContext2 = paramContext;
        if (paramContext.moveToFirst())
        {
          localContext1 = paramContext;
          localContext2 = paramContext;
          paramString = paramContext.getString(paramContext.getColumnIndexOrThrow("_data"));
          localContext1 = paramContext;
          localContext2 = paramContext;
          if (!paramString.startsWith("content://"))
          {
            localContext1 = paramContext;
            localContext2 = paramContext;
            if (!paramString.startsWith("/"))
            {
              localContext1 = paramContext;
              localContext2 = paramContext;
              boolean bool = paramString.startsWith("file://");
              if (bool) {}
            }
          }
          else
          {
            if (paramContext != null) {
              paramContext.close();
            }
            paramUri = null;
            return paramUri;
          }
          paramUri = paramString;
          return paramString;
        }
      }
    }
    catch (Exception paramContext)
    {
      for (;;)
      {
        if (localContext1 != null) {
          localContext1.close();
        }
      }
    }
    finally
    {
      if (localContext2 == null) {
        break label190;
      }
      localContext2.close();
    }
    return null;
  }
  
  public static int getMinTabletSide()
  {
    if (!isSmallTablet())
    {
      k = Math.min(displaySize.x, displaySize.y);
      j = k * 35 / 100;
      i = j;
      if (j < dp(320.0F)) {
        i = dp(320.0F);
      }
      return k - i;
    }
    int k = Math.min(displaySize.x, displaySize.y);
    int m = Math.max(displaySize.x, displaySize.y);
    int j = m * 35 / 100;
    int i = j;
    if (j < dp(320.0F)) {
      i = dp(320.0F);
    }
    return Math.min(k, m - i);
  }
  
  public static int getMyLayerVersion(int paramInt)
  {
    return 0xFFFF & paramInt;
  }
  
  @SuppressLint({"NewApi"})
  public static String getPath(Uri paramUri)
  {
    int j = 0;
    for (;;)
    {
      try
      {
        if (Build.VERSION.SDK_INT < 19) {
          break label330;
        }
        i = 1;
        Object localObject1;
        Object localObject2;
        if ((i != 0) && (DocumentsContract.isDocumentUri(ApplicationLoader.applicationContext, paramUri)))
        {
          if (isExternalStorageDocument(paramUri))
          {
            paramUri = DocumentsContract.getDocumentId(paramUri).split(":");
            if ("primary".equalsIgnoreCase(paramUri[0])) {
              return Environment.getExternalStorageDirectory() + "/" + paramUri[1];
            }
          }
          else
          {
            if (isDownloadsDocument(paramUri))
            {
              paramUri = DocumentsContract.getDocumentId(paramUri);
              paramUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(paramUri).longValue());
              return getDataColumn(ApplicationLoader.applicationContext, paramUri, null, null);
            }
            if (isMediaDocument(paramUri))
            {
              localObject1 = DocumentsContract.getDocumentId(paramUri).split(":");
              localObject2 = localObject1[0];
              paramUri = null;
            }
          }
        }
        else {
          switch (((String)localObject2).hashCode())
          {
          case 100313435: 
            localObject1 = localObject1[1];
            return getDataColumn(ApplicationLoader.applicationContext, paramUri, "_id=?", new String[] { localObject1 });
            if (!((String)localObject2).equals("image")) {
              break;
            }
            i = j;
            break;
          case 112202875: 
            if (!((String)localObject2).equals("video")) {
              break;
            }
            i = 1;
            break;
          case 93166550: 
            if (!((String)localObject2).equals("audio")) {
              break;
            }
            i = 2;
            break label337;
            paramUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            continue;
            paramUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            continue;
            paramUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            continue;
            if ("content".equalsIgnoreCase(paramUri.getScheme())) {
              return getDataColumn(ApplicationLoader.applicationContext, paramUri, null, null);
            }
            if ("file".equalsIgnoreCase(paramUri.getScheme()))
            {
              paramUri = paramUri.getPath();
              return paramUri;
            }
            break;
          }
        }
      }
      catch (Exception paramUri)
      {
        FileLog.e(paramUri);
      }
      return null;
      label330:
      int i = 0;
      continue;
      i = -1;
      label337:
      switch (i)
      {
      }
    }
  }
  
  public static int getPeerLayerVersion(int paramInt)
  {
    return paramInt >> 16 & 0xFFFF;
  }
  
  public static int getPhotoSize()
  {
    if (photoSize == null) {
      photoSize = Integer.valueOf(1280);
    }
    return photoSize.intValue();
  }
  
  public static float getPixelsInCM(float paramFloat, boolean paramBoolean)
  {
    float f = paramFloat / 2.54F;
    if (paramBoolean) {}
    for (paramFloat = displayMetrics.xdpi;; paramFloat = displayMetrics.ydpi) {
      return paramFloat * f;
    }
  }
  
  public static Point getRealScreenSize()
  {
    localPoint = new Point();
    try
    {
      WindowManager localWindowManager = (WindowManager)ApplicationLoader.applicationContext.getSystemService("window");
      if (Build.VERSION.SDK_INT >= 17)
      {
        localWindowManager.getDefaultDisplay().getRealSize(localPoint);
        return localPoint;
      }
      try
      {
        Method localMethod1 = Display.class.getMethod("getRawWidth", new Class[0]);
        Method localMethod2 = Display.class.getMethod("getRawHeight", new Class[0]);
        localPoint.set(((Integer)localMethod1.invoke(localWindowManager.getDefaultDisplay(), new Object[0])).intValue(), ((Integer)localMethod2.invoke(localWindowManager.getDefaultDisplay(), new Object[0])).intValue());
        return localPoint;
      }
      catch (Exception localException2)
      {
        localPoint.set(localWindowManager.getDefaultDisplay().getWidth(), localWindowManager.getDefaultDisplay().getHeight());
        FileLog.e(localException2);
        return localPoint;
      }
      return localPoint;
    }
    catch (Exception localException1)
    {
      FileLog.e(localException1);
    }
  }
  
  public static CharSequence getTrimmedString(CharSequence paramCharSequence)
  {
    CharSequence localCharSequence = paramCharSequence;
    if (paramCharSequence != null)
    {
      localCharSequence = paramCharSequence;
      if (paramCharSequence.length() == 0) {
        localCharSequence = paramCharSequence;
      }
    }
    else
    {
      return localCharSequence;
    }
    for (;;)
    {
      paramCharSequence = localCharSequence;
      if (localCharSequence.length() <= 0) {
        break;
      }
      if (localCharSequence.charAt(0) != '\n')
      {
        paramCharSequence = localCharSequence;
        if (localCharSequence.charAt(0) != ' ') {
          break;
        }
      }
      localCharSequence = localCharSequence.subSequence(1, localCharSequence.length());
    }
    for (;;)
    {
      localCharSequence = paramCharSequence;
      if (paramCharSequence.length() <= 0) {
        break;
      }
      if (paramCharSequence.charAt(paramCharSequence.length() - 1) != '\n')
      {
        localCharSequence = paramCharSequence;
        if (paramCharSequence.charAt(paramCharSequence.length() - 1) != ' ') {
          break;
        }
      }
      paramCharSequence = paramCharSequence.subSequence(0, paramCharSequence.length() - 1);
    }
  }
  
  public static Typeface getTypeface(String paramString)
  {
    synchronized (typefaceCache)
    {
      boolean bool = typefaceCache.containsKey(paramString);
      if (!bool) {}
      try
      {
        Typeface localTypeface = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), paramString);
        typefaceCache.put(paramString, localTypeface);
        paramString = (Typeface)typefaceCache.get(paramString);
        return paramString;
      }
      catch (Exception localException)
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.e("Could not get typeface '" + paramString + "' because " + localException.getMessage());
        }
        return null;
      }
    }
  }
  
  public static int getViewInset(View paramView)
  {
    if ((paramView == null) || (Build.VERSION.SDK_INT < 21) || (paramView.getHeight() == displaySize.y) || (paramView.getHeight() == displaySize.y - statusBarHeight)) {}
    for (;;)
    {
      return 0;
      try
      {
        if (mAttachInfoField == null)
        {
          mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
          mAttachInfoField.setAccessible(true);
        }
        paramView = mAttachInfoField.get(paramView);
        if (paramView != null)
        {
          if (mStableInsetsField == null)
          {
            mStableInsetsField = paramView.getClass().getDeclaredField("mStableInsets");
            mStableInsetsField.setAccessible(true);
          }
          int i = ((Rect)mStableInsetsField.get(paramView)).bottom;
          return i;
        }
      }
      catch (Exception paramView)
      {
        FileLog.e(paramView);
      }
    }
    return 0;
  }
  
  public static boolean handleProxyIntent(Activity paramActivity, Intent paramIntent)
  {
    if (paramIntent == null) {
      return false;
    }
    for (;;)
    {
      Object localObject1;
      try
      {
        if ((paramIntent.getFlags() & 0x100000) != 0) {
          break;
        }
        Object localObject8 = paramIntent.getData();
        if (localObject8 == null) {
          break;
        }
        Object localObject4 = null;
        Object localObject5 = null;
        Object localObject6 = null;
        Object localObject7 = null;
        String str = ((Uri)localObject8).getScheme();
        Object localObject2 = localObject7;
        paramIntent = (Intent)localObject5;
        Object localObject3 = localObject6;
        localObject1 = localObject4;
        if (str != null)
        {
          if ((!str.equals("http")) && (!str.equals("https"))) {
            continue;
          }
          str = ((Uri)localObject8).getHost().toLowerCase();
          if ((!str.equals("telegram.me")) && (!str.equals("t.me")) && (!str.equals("telegram.dog")))
          {
            localObject2 = localObject7;
            paramIntent = (Intent)localObject5;
            localObject3 = localObject6;
            localObject1 = localObject4;
            if (!str.equals("telesco.pe")) {}
          }
          else
          {
            str = ((Uri)localObject8).getPath();
            localObject2 = localObject7;
            paramIntent = (Intent)localObject5;
            localObject3 = localObject6;
            localObject1 = localObject4;
            if (str != null)
            {
              localObject2 = localObject7;
              paramIntent = (Intent)localObject5;
              localObject3 = localObject6;
              localObject1 = localObject4;
              if (str.startsWith("/socks"))
              {
                localObject2 = ((Uri)localObject8).getQueryParameter("server");
                localObject3 = ((Uri)localObject8).getQueryParameter("port");
                localObject1 = ((Uri)localObject8).getQueryParameter("user");
                paramIntent = ((Uri)localObject8).getQueryParameter("pass");
              }
            }
          }
        }
        if ((TextUtils.isEmpty((CharSequence)localObject2)) || (TextUtils.isEmpty((CharSequence)localObject3))) {
          break;
        }
        localObject4 = localObject1;
        if (localObject1 == null)
        {
          localObject4 = "";
          break label411;
          showProxyAlert(paramActivity, (String)localObject2, (String)localObject3, (String)localObject4, (String)localObject1);
          return true;
          localObject2 = localObject7;
          paramIntent = (Intent)localObject5;
          localObject3 = localObject6;
          localObject1 = localObject4;
          if (!str.equals("tg")) {
            continue;
          }
          localObject8 = ((Uri)localObject8).toString();
          if (!((String)localObject8).startsWith("tg:socks"))
          {
            localObject2 = localObject7;
            paramIntent = (Intent)localObject5;
            localObject3 = localObject6;
            localObject1 = localObject4;
            if (!((String)localObject8).startsWith("tg://socks")) {
              continue;
            }
          }
          paramIntent = Uri.parse(((String)localObject8).replace("tg:proxy", "tg://telegram.org").replace("tg://proxy", "tg://telegram.org"));
          localObject2 = paramIntent.getQueryParameter("server");
          localObject3 = paramIntent.getQueryParameter("port");
          localObject1 = paramIntent.getQueryParameter("user");
          paramIntent = paramIntent.getQueryParameter("pass");
          continue;
        }
        localObject1 = paramIntent;
      }
      catch (Exception paramActivity)
      {
        return false;
      }
      label411:
      if (paramIntent == null) {
        localObject1 = "";
      }
    }
  }
  
  public static void hideKeyboard(View paramView)
  {
    if (paramView == null) {}
    for (;;)
    {
      return;
      try
      {
        InputMethodManager localInputMethodManager = (InputMethodManager)paramView.getContext().getSystemService("input_method");
        if (localInputMethodManager.isActive())
        {
          localInputMethodManager.hideSoftInputFromWindow(paramView.getWindowToken(), 0);
          return;
        }
      }
      catch (Exception paramView)
      {
        FileLog.e(paramView);
      }
    }
  }
  
  private static int[] hsvToRgb(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    double d4 = 0.0D;
    double d3 = 0.0D;
    double d2 = 0.0D;
    double d5 = (int)Math.floor(6.0D * paramDouble1);
    double d6 = 6.0D * paramDouble1 - d5;
    paramDouble1 = paramDouble3 * (1.0D - paramDouble2);
    double d1 = paramDouble3 * (1.0D - d6 * paramDouble2);
    paramDouble2 = paramDouble3 * (1.0D - (1.0D - d6) * paramDouble2);
    switch ((int)d5 % 6)
    {
    default: 
      paramDouble3 = d4;
      paramDouble2 = d3;
      paramDouble1 = d2;
    }
    for (;;)
    {
      return new int[] { (int)(255.0D * paramDouble3), (int)(255.0D * paramDouble2), (int)(255.0D * paramDouble1) };
      continue;
      paramDouble2 = paramDouble3;
      paramDouble3 = d1;
      continue;
      d1 = paramDouble1;
      paramDouble1 = paramDouble2;
      paramDouble2 = paramDouble3;
      paramDouble3 = d1;
      continue;
      d2 = paramDouble1;
      paramDouble2 = d1;
      paramDouble1 = paramDouble3;
      paramDouble3 = d2;
      continue;
      d1 = paramDouble2;
      paramDouble2 = paramDouble1;
      paramDouble1 = paramDouble3;
      paramDouble3 = d1;
      continue;
      paramDouble2 = paramDouble1;
      paramDouble1 = d1;
    }
  }
  
  public static boolean isBannedForever(int paramInt)
  {
    return Math.abs(paramInt - System.currentTimeMillis() / 1000L) > 157680000L;
  }
  
  public static boolean isDownloadsDocument(Uri paramUri)
  {
    return "com.android.providers.downloads.documents".equals(paramUri.getAuthority());
  }
  
  public static boolean isExternalStorageDocument(Uri paramUri)
  {
    return "com.android.externalstorage.documents".equals(paramUri.getAuthority());
  }
  
  public static boolean isGoogleMapsInstalled(BaseFragment paramBaseFragment)
  {
    boolean bool = false;
    try
    {
      ApplicationLoader.applicationContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
      bool = true;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      while (paramBaseFragment.getParentActivity() == null) {}
      AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
      localBuilder.setMessage("Install Google Maps?");
      localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          try
          {
            paramAnonymousDialogInterface = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.google.android.apps.maps"));
            this.val$fragment.getParentActivity().startActivityForResult(paramAnonymousDialogInterface, 500);
            return;
          }
          catch (Exception paramAnonymousDialogInterface)
          {
            FileLog.e(paramAnonymousDialogInterface);
          }
        }
      });
      localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
      paramBaseFragment.showDialog(localBuilder.create());
    }
    return bool;
    return false;
  }
  
  public static boolean isInternalUri(Uri paramUri)
  {
    Object localObject = paramUri.getPath();
    paramUri = (Uri)localObject;
    if (localObject == null) {}
    do
    {
      return false;
      do
      {
        paramUri = (Uri)localObject;
        localObject = Utilities.readlink(paramUri);
      } while ((localObject != null) && (!((String)localObject).equals(paramUri)));
      localObject = paramUri;
      if (paramUri != null) {}
      try
      {
        String str = new File(paramUri).getCanonicalPath();
        localObject = paramUri;
        if (str != null) {
          localObject = str;
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          paramUri.replace("/./", "/");
          Uri localUri = paramUri;
        }
      }
    } while ((localObject == null) || (!((String)localObject).toLowerCase().contains("/data/data/" + ApplicationLoader.applicationContext.getPackageName() + "/files")));
    return true;
  }
  
  public static boolean isKeyboardShowed(View paramView)
  {
    if (paramView == null) {
      return false;
    }
    try
    {
      boolean bool = ((InputMethodManager)paramView.getContext().getSystemService("input_method")).isActive(paramView);
      return bool;
    }
    catch (Exception paramView)
    {
      FileLog.e(paramView);
    }
    return false;
  }
  
  public static boolean isMediaDocument(Uri paramUri)
  {
    return "com.android.providers.media.documents".equals(paramUri.getAuthority());
  }
  
  public static boolean isSmallTablet()
  {
    return Math.min(displaySize.x, displaySize.y) / density <= 700.0F;
  }
  
  public static boolean isTablet()
  {
    if (isTablet == null) {
      isTablet = Boolean.valueOf(ApplicationLoader.applicationContext.getResources().getBoolean(2130968577));
    }
    return isTablet.booleanValue();
  }
  
  public static boolean isWaitingForCall()
  {
    synchronized (callLock)
    {
      boolean bool = waitingForCall;
      return bool;
    }
  }
  
  public static boolean isWaitingForSms()
  {
    synchronized (smsLock)
    {
      boolean bool = waitingForSms;
      return bool;
    }
  }
  
  public static void lockOrientation(Activity paramActivity)
  {
    if ((paramActivity == null) || (prevOrientation != -10)) {}
    int i;
    int j;
    for (;;)
    {
      return;
      try
      {
        prevOrientation = paramActivity.getRequestedOrientation();
        WindowManager localWindowManager = (WindowManager)paramActivity.getSystemService("window");
        if ((localWindowManager != null) && (localWindowManager.getDefaultDisplay() != null))
        {
          i = localWindowManager.getDefaultDisplay().getRotation();
          j = paramActivity.getResources().getConfiguration().orientation;
          if (i != 3) {
            break label94;
          }
          if (j == 1)
          {
            paramActivity.setRequestedOrientation(1);
            return;
          }
        }
      }
      catch (Exception paramActivity)
      {
        FileLog.e(paramActivity);
        return;
      }
    }
    paramActivity.setRequestedOrientation(8);
    return;
    label94:
    if (i == 1)
    {
      if (j == 1)
      {
        paramActivity.setRequestedOrientation(9);
        return;
      }
      paramActivity.setRequestedOrientation(0);
      return;
    }
    if (i == 0)
    {
      if (j == 2)
      {
        paramActivity.setRequestedOrientation(0);
        return;
      }
      paramActivity.setRequestedOrientation(1);
      return;
    }
    if (j == 2)
    {
      paramActivity.setRequestedOrientation(8);
      return;
    }
    paramActivity.setRequestedOrientation(9);
  }
  
  public static long makeBroadcastId(int paramInt)
  {
    return 0x100000000 | paramInt & 0xFFFFFFFF;
  }
  
  public static boolean needShowPasscode(boolean paramBoolean)
  {
    boolean bool = ForegroundDetector.getInstance().isWasInBackground(paramBoolean);
    if (paramBoolean) {
      ForegroundDetector.getInstance().resetBackgroundVar();
    }
    return (SharedConfig.passcodeHash.length() > 0) && (bool) && ((SharedConfig.appLocked) || ((SharedConfig.autoLockIn != 0) && (SharedConfig.lastPauseTime != 0) && (!SharedConfig.appLocked) && (SharedConfig.lastPauseTime + SharedConfig.autoLockIn <= ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime())) || (ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime() + 5 < SharedConfig.lastPauseTime));
  }
  
  public static String obtainLoginPhoneCall(String paramString)
  {
    if (!hasCallPermissions) {
      paramString = null;
    }
    for (;;)
    {
      return paramString;
      Object localObject2 = null;
      Object localObject1 = null;
      try
      {
        Cursor localCursor = ApplicationLoader.applicationContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] { "number", "date" }, "type IN (3,1,5)", null, "date DESC LIMIT 5");
        String str;
        boolean bool;
        do
        {
          long l;
          do
          {
            localObject1 = localCursor;
            localObject2 = localCursor;
            if (!localCursor.moveToNext()) {
              break;
            }
            localObject1 = localCursor;
            localObject2 = localCursor;
            str = localCursor.getString(0);
            localObject1 = localCursor;
            localObject2 = localCursor;
            l = localCursor.getLong(1);
            localObject1 = localCursor;
            localObject2 = localCursor;
            if (BuildVars.LOGS_ENABLED)
            {
              localObject1 = localCursor;
              localObject2 = localCursor;
              FileLog.e("number = " + str);
            }
            localObject1 = localCursor;
            localObject2 = localCursor;
          } while (Math.abs(System.currentTimeMillis() - l) >= 3600000L);
          localObject1 = localCursor;
          localObject2 = localCursor;
          bool = checkPhonePattern(paramString, str);
        } while (!bool);
        paramString = str;
        if (localCursor != null)
        {
          localCursor.close();
          return str;
          if (localCursor != null) {
            localCursor.close();
          }
        }
      }
      catch (Exception paramString)
      {
        for (;;)
        {
          localObject2 = localObject1;
          FileLog.e(paramString);
          if (localObject1 != null) {
            ((Cursor)localObject1).close();
          }
        }
      }
      finally
      {
        if (localObject2 == null) {
          break label263;
        }
        ((Cursor)localObject2).close();
      }
    }
    return null;
  }
  
  public static void openForView(MessageObject paramMessageObject, Activity paramActivity)
    throws Exception
  {
    Object localObject2 = null;
    String str = paramMessageObject.getFileName();
    Object localObject1 = localObject2;
    if (paramMessageObject.messageOwner.attachPath != null)
    {
      localObject1 = localObject2;
      if (paramMessageObject.messageOwner.attachPath.length() != 0) {
        localObject1 = new File(paramMessageObject.messageOwner.attachPath);
      }
    }
    Object localObject3;
    if (localObject1 != null)
    {
      localObject3 = localObject1;
      if (((File)localObject1).exists()) {}
    }
    else
    {
      localObject3 = FileLoader.getPathToMessage(paramMessageObject.messageOwner);
    }
    Intent localIntent;
    if ((localObject3 != null) && (((File)localObject3).exists()))
    {
      localObject1 = null;
      localIntent = new Intent("android.intent.action.VIEW");
      localIntent.setFlags(1);
      localObject2 = MimeTypeMap.getSingleton();
      int i = str.lastIndexOf('.');
      if (i != -1)
      {
        localObject2 = ((MimeTypeMap)localObject2).getMimeTypeFromExtension(str.substring(i + 1).toLowerCase());
        localObject1 = localObject2;
        if (localObject2 == null)
        {
          if ((paramMessageObject.type == 9) || (paramMessageObject.type == 0)) {
            localObject2 = paramMessageObject.getDocument().mime_type;
          }
          if (localObject2 != null)
          {
            localObject1 = localObject2;
            if (((String)localObject2).length() != 0) {}
          }
          else
          {
            localObject1 = null;
          }
        }
      }
      if ((Build.VERSION.SDK_INT >= 26) && (localObject1 != null) && (((String)localObject1).equals("application/vnd.android.package-archive")) && (!ApplicationLoader.applicationContext.getPackageManager().canRequestPackageInstalls()))
      {
        paramMessageObject = new AlertDialog.Builder(paramActivity);
        paramMessageObject.setTitle(LocaleController.getString("AppName", 2131492981));
        paramMessageObject.setMessage(LocaleController.getString("ApkRestricted", 2131492980));
        paramMessageObject.setPositiveButton(LocaleController.getString("PermissionOpenSettings", 2131494147), new DialogInterface.OnClickListener()
        {
          @TargetApi(26)
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            try
            {
              this.val$activity.startActivity(new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", Uri.parse("package:" + this.val$activity.getPackageName())));
              return;
            }
            catch (Exception paramAnonymousDialogInterface)
            {
              FileLog.e(paramAnonymousDialogInterface);
            }
          }
        });
        paramMessageObject.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        paramMessageObject.show();
      }
    }
    else
    {
      return;
    }
    if (Build.VERSION.SDK_INT >= 24)
    {
      localObject2 = FileProvider.getUriForFile(paramActivity, "org.telegram.messenger.provider", (File)localObject3);
      if (localObject1 != null)
      {
        paramMessageObject = (MessageObject)localObject1;
        localIntent.setDataAndType((Uri)localObject2, paramMessageObject);
        if (localObject1 == null) {
          break label452;
        }
        try
        {
          paramActivity.startActivityForResult(localIntent, 500);
          return;
        }
        catch (Exception paramMessageObject)
        {
          if (Build.VERSION.SDK_INT < 24) {
            break label435;
          }
        }
        localIntent.setDataAndType(FileProvider.getUriForFile(paramActivity, "org.telegram.messenger.provider", (File)localObject3), "text/plain");
      }
    }
    for (;;)
    {
      paramActivity.startActivityForResult(localIntent, 500);
      return;
      paramMessageObject = "text/plain";
      break;
      localObject2 = Uri.fromFile((File)localObject3);
      if (localObject1 != null) {}
      for (paramMessageObject = (MessageObject)localObject1;; paramMessageObject = "text/plain")
      {
        localIntent.setDataAndType((Uri)localObject2, paramMessageObject);
        break;
      }
      label435:
      localIntent.setDataAndType(Uri.fromFile((File)localObject3), "text/plain");
    }
    label452:
    paramActivity.startActivityForResult(localIntent, 500);
  }
  
  public static void openForView(TLObject paramTLObject, Activity paramActivity)
    throws Exception
  {
    if ((paramTLObject == null) || (paramActivity == null)) {}
    Object localObject2;
    File localFile;
    do
    {
      return;
      localObject2 = FileLoader.getAttachFileName(paramTLObject);
      localFile = FileLoader.getPathToAttach(paramTLObject, true);
    } while ((localFile == null) || (!localFile.exists()));
    Object localObject1 = null;
    Intent localIntent = new Intent("android.intent.action.VIEW");
    localIntent.setFlags(1);
    MimeTypeMap localMimeTypeMap = MimeTypeMap.getSingleton();
    int i = ((String)localObject2).lastIndexOf('.');
    if (i != -1)
    {
      localObject2 = localMimeTypeMap.getMimeTypeFromExtension(((String)localObject2).substring(i + 1).toLowerCase());
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        if ((paramTLObject instanceof TLRPC.TL_document)) {
          localObject2 = ((TLRPC.TL_document)paramTLObject).mime_type;
        }
        if (localObject2 != null)
        {
          localObject1 = localObject2;
          if (((String)localObject2).length() != 0) {}
        }
        else
        {
          localObject1 = null;
        }
      }
    }
    if (Build.VERSION.SDK_INT >= 24)
    {
      localObject2 = FileProvider.getUriForFile(paramActivity, "org.telegram.messenger.provider", localFile);
      if (localObject1 != null)
      {
        paramTLObject = (TLObject)localObject1;
        localIntent.setDataAndType((Uri)localObject2, paramTLObject);
        if (localObject1 == null) {
          break label275;
        }
        try
        {
          paramActivity.startActivityForResult(localIntent, 500);
          return;
        }
        catch (Exception paramTLObject)
        {
          if (Build.VERSION.SDK_INT < 24) {
            break label258;
          }
        }
        localIntent.setDataAndType(FileProvider.getUriForFile(paramActivity, "org.telegram.messenger.provider", localFile), "text/plain");
      }
    }
    for (;;)
    {
      paramActivity.startActivityForResult(localIntent, 500);
      return;
      paramTLObject = "text/plain";
      break;
      localObject2 = Uri.fromFile(localFile);
      if (localObject1 != null) {}
      for (paramTLObject = (TLObject)localObject1;; paramTLObject = "text/plain")
      {
        localIntent.setDataAndType((Uri)localObject2, paramTLObject);
        break;
      }
      label258:
      localIntent.setDataAndType(Uri.fromFile(localFile), "text/plain");
    }
    label275:
    paramActivity.startActivityForResult(localIntent, 500);
  }
  
  private static void registerLoginContentObserver(boolean paramBoolean, final String paramString)
  {
    if (paramBoolean) {
      if (callLogContentObserver == null) {}
    }
    while (callLogContentObserver == null)
    {
      return;
      ContentResolver localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
      Uri localUri = CallLog.Calls.CONTENT_URI;
      ContentObserver local2 = new ContentObserver(new Handler())
      {
        public boolean deliverSelfNotifications()
        {
          return true;
        }
        
        public void onChange(boolean paramAnonymousBoolean)
        {
          AndroidUtilities.registerLoginContentObserver(false, paramString);
          AndroidUtilities.removeLoginPhoneCall(paramString, false);
        }
      };
      callLogContentObserver = local2;
      localContentResolver.registerContentObserver(localUri, true, local2);
      paramString = new Runnable()
      {
        public void run()
        {
          AndroidUtilities.access$102(null);
          AndroidUtilities.registerLoginContentObserver(false, this.val$number);
        }
      };
      unregisterRunnable = paramString;
      runOnUIThread(paramString, 10000L);
      return;
    }
    if (unregisterRunnable != null)
    {
      cancelRunOnUIThread(unregisterRunnable);
      unregisterRunnable = null;
    }
    try
    {
      ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(callLogContentObserver);
      callLogContentObserver = null;
      return;
    }
    catch (Exception paramString)
    {
      paramString = paramString;
      callLogContentObserver = null;
      return;
    }
    finally
    {
      paramString = finally;
      callLogContentObserver = null;
      throw paramString;
    }
  }
  
  public static void removeAdjustResize(Activity paramActivity, int paramInt)
  {
    if ((paramActivity == null) || (isTablet())) {}
    while (adjustOwnerClassGuid != paramInt) {
      return;
    }
    paramActivity.getWindow().setSoftInputMode(32);
  }
  
  public static void removeLoginPhoneCall(String paramString, boolean paramBoolean)
  {
    if (!hasCallPermissions) {}
    for (;;)
    {
      return;
      Object localObject2 = null;
      Object localObject1 = null;
      try
      {
        Cursor localCursor = ApplicationLoader.applicationContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[] { "_id", "number" }, "type IN (3,1,5)", null, "date DESC LIMIT 5");
        int j = 0;
        String str;
        do
        {
          i = j;
          localObject1 = localCursor;
          localObject2 = localCursor;
          if (!localCursor.moveToNext()) {
            break label168;
          }
          localObject1 = localCursor;
          localObject2 = localCursor;
          str = localCursor.getString(1);
          localObject1 = localCursor;
          localObject2 = localCursor;
          if (str.contains(paramString)) {
            break;
          }
          localObject1 = localCursor;
          localObject2 = localCursor;
        } while (!paramString.contains(str));
        int i = 1;
        localObject1 = localCursor;
        localObject2 = localCursor;
        ApplicationLoader.applicationContext.getContentResolver().delete(CallLog.Calls.CONTENT_URI, "_id = ? ", new String[] { String.valueOf(localCursor.getInt(0)) });
        label168:
        if ((i == 0) && (paramBoolean))
        {
          localObject1 = localCursor;
          localObject2 = localCursor;
          registerLoginContentObserver(true, paramString);
        }
        if (localCursor == null) {
          continue;
        }
        localCursor.close();
        return;
      }
      catch (Exception paramString)
      {
        localObject2 = localObject1;
        FileLog.e(paramString);
        return;
      }
      finally
      {
        if (localObject2 != null) {
          ((Cursor)localObject2).close();
        }
      }
    }
  }
  
  public static SpannableStringBuilder replaceTags(String paramString)
  {
    return replaceTags(paramString, 3);
  }
  
  public static SpannableStringBuilder replaceTags(String paramString, int paramInt)
  {
    int i;
    Object localObject;
    try
    {
      StringBuilder localStringBuilder = new StringBuilder(paramString);
      if ((paramInt & 0x1) == 0) {
        break label96;
      }
      for (;;)
      {
        i = localStringBuilder.indexOf("<br>");
        if (i == -1) {
          break;
        }
        localStringBuilder.replace(i, i + 4, "\n");
      }
      return (SpannableStringBuilder)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
      localObject = new SpannableStringBuilder(paramString);
    }
    for (;;)
    {
      i = ((StringBuilder)localObject).indexOf("<br/>");
      if (i == -1) {
        break;
      }
      ((StringBuilder)localObject).replace(i, i + 5, "\n");
    }
    label96:
    ArrayList localArrayList = new ArrayList();
    if ((paramInt & 0x2) != 0)
    {
      for (;;)
      {
        int j = ((StringBuilder)localObject).indexOf("<b>");
        if (j == -1) {
          break;
        }
        ((StringBuilder)localObject).replace(j, j + 3, "");
        i = ((StringBuilder)localObject).indexOf("</b>");
        paramInt = i;
        if (i == -1) {
          paramInt = ((StringBuilder)localObject).indexOf("<b>");
        }
        ((StringBuilder)localObject).replace(paramInt, paramInt + 4, "");
        localArrayList.add(Integer.valueOf(j));
        localArrayList.add(Integer.valueOf(paramInt));
      }
      for (;;)
      {
        paramInt = ((StringBuilder)localObject).indexOf("**");
        if (paramInt == -1) {
          break;
        }
        ((StringBuilder)localObject).replace(paramInt, paramInt + 2, "");
        i = ((StringBuilder)localObject).indexOf("**");
        if (i >= 0)
        {
          ((StringBuilder)localObject).replace(i, i + 2, "");
          localArrayList.add(Integer.valueOf(paramInt));
          localArrayList.add(Integer.valueOf(i));
        }
      }
    }
    SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder((CharSequence)localObject);
    paramInt = 0;
    for (;;)
    {
      localObject = localSpannableStringBuilder;
      if (paramInt >= localArrayList.size() / 2) {
        break;
      }
      localSpannableStringBuilder.setSpan(new TypefaceSpan(getTypeface("fonts/rmedium.ttf")), ((Integer)localArrayList.get(paramInt * 2)).intValue(), ((Integer)localArrayList.get(paramInt * 2 + 1)).intValue(), 33);
      paramInt += 1;
    }
  }
  
  public static void requestAdjustResize(Activity paramActivity, int paramInt)
  {
    if ((paramActivity == null) || (isTablet())) {
      return;
    }
    paramActivity.getWindow().setSoftInputMode(16);
    adjustOwnerClassGuid = paramInt;
  }
  
  private static double[] rgbToHsv(int paramInt1, int paramInt2, int paramInt3)
  {
    double d5 = paramInt1 / 255.0D;
    double d3 = paramInt2 / 255.0D;
    double d4 = paramInt3 / 255.0D;
    double d1;
    double d2;
    label63:
    double d7;
    if ((d5 > d3) && (d5 > d4))
    {
      d1 = d5;
      if ((d5 >= d3) || (d5 >= d4)) {
        break label126;
      }
      d2 = d5;
      d7 = d1 - d2;
      if (d1 != 0.0D) {
        break label148;
      }
    }
    label126:
    label148:
    for (double d6 = 0.0D;; d6 = d7 / d1)
    {
      if (d1 != d2) {
        break label157;
      }
      d2 = 0.0D;
      return new double[] { d2, d6, d1 };
      if (d3 > d4)
      {
        d1 = d3;
        break;
      }
      d1 = d4;
      break;
      if (d3 < d4)
      {
        d2 = d3;
        break label63;
      }
      d2 = d4;
      break label63;
    }
    label157:
    if ((d5 > d3) && (d5 > d4))
    {
      d2 = (d3 - d4) / d7;
      if (d3 < d4)
      {
        paramInt1 = 6;
        label194:
        d2 += paramInt1;
      }
    }
    for (;;)
    {
      d2 /= 6.0D;
      break;
      paramInt1 = 0;
      break label194;
      if (d3 > d4) {
        d2 = (d4 - d5) / d7 + 2.0D;
      } else {
        d2 = (d5 - d3) / d7 + 4.0D;
      }
    }
  }
  
  public static void runOnUIThread(Runnable paramRunnable)
  {
    runOnUIThread(paramRunnable, 0L);
  }
  
  public static void runOnUIThread(Runnable paramRunnable, long paramLong)
  {
    if (paramLong == 0L)
    {
      ApplicationLoader.applicationHandler.post(paramRunnable);
      return;
    }
    ApplicationLoader.applicationHandler.postDelayed(paramRunnable, paramLong);
  }
  
  public static void setEnabled(View paramView, boolean paramBoolean)
  {
    if (paramView == null) {}
    for (;;)
    {
      return;
      paramView.setEnabled(paramBoolean);
      if ((paramView instanceof ViewGroup))
      {
        paramView = (ViewGroup)paramView;
        int i = 0;
        while (i < paramView.getChildCount())
        {
          setEnabled(paramView.getChildAt(i), paramBoolean);
          i += 1;
        }
      }
    }
  }
  
  public static int setMyLayerVersion(int paramInt1, int paramInt2)
  {
    return 0xFFFF0000 & paramInt1 | paramInt2;
  }
  
  public static int setPeerLayerVersion(int paramInt1, int paramInt2)
  {
    return 0xFFFF & paramInt1 | paramInt2 << 16;
  }
  
  public static void setRectToRect(Matrix paramMatrix, RectF paramRectF1, RectF paramRectF2, int paramInt, Matrix.ScaleToFit paramScaleToFit)
  {
    float f1;
    float f2;
    float f3;
    float f4;
    if ((paramInt == 90) || (paramInt == 270))
    {
      f1 = paramRectF2.height() / paramRectF1.width();
      f2 = paramRectF2.width() / paramRectF1.height();
      f3 = f1;
      f4 = f2;
      if (paramScaleToFit != Matrix.ScaleToFit.FILL)
      {
        if (f1 <= f2) {
          break label168;
        }
        f3 = f2;
        f4 = f2;
      }
      label67:
      f1 = -paramRectF1.left;
      f2 = -paramRectF1.top;
      paramMatrix.setTranslate(paramRectF2.left, paramRectF2.top);
      if (paramInt != 90) {
        break label179;
      }
      paramMatrix.preRotate(90.0F);
      paramMatrix.preTranslate(0.0F, -paramRectF2.width());
    }
    for (;;)
    {
      paramMatrix.preScale(f3, f4);
      paramMatrix.preTranslate(f1 * f3, f2 * f4);
      return;
      f1 = paramRectF2.width() / paramRectF1.width();
      f2 = paramRectF2.height() / paramRectF1.height();
      break;
      label168:
      f4 = f1;
      f3 = f1;
      break label67;
      label179:
      if (paramInt == 180)
      {
        paramMatrix.preRotate(180.0F);
        paramMatrix.preTranslate(-paramRectF2.width(), -paramRectF2.height());
      }
      else if (paramInt == 270)
      {
        paramMatrix.preRotate(270.0F);
        paramMatrix.preTranslate(-paramRectF2.height(), 0.0F);
      }
    }
  }
  
  public static void setScrollViewEdgeEffectColor(ScrollView paramScrollView, int paramInt)
  {
    if (Build.VERSION.SDK_INT >= 21) {}
    try
    {
      Object localObject = ScrollView.class.getDeclaredField("mEdgeGlowTop");
      ((Field)localObject).setAccessible(true);
      localObject = (EdgeEffect)((Field)localObject).get(paramScrollView);
      if (localObject != null) {
        ((EdgeEffect)localObject).setColor(paramInt);
      }
      localObject = ScrollView.class.getDeclaredField("mEdgeGlowBottom");
      ((Field)localObject).setAccessible(true);
      paramScrollView = (EdgeEffect)((Field)localObject).get(paramScrollView);
      if (paramScrollView != null) {
        paramScrollView.setColor(paramInt);
      }
      return;
    }
    catch (Exception paramScrollView)
    {
      FileLog.e(paramScrollView);
    }
  }
  
  public static void setViewPagerEdgeEffectColor(ViewPager paramViewPager, int paramInt)
  {
    if (Build.VERSION.SDK_INT >= 21) {}
    try
    {
      Object localObject = ViewPager.class.getDeclaredField("mLeftEdge");
      ((Field)localObject).setAccessible(true);
      localObject = (EdgeEffectCompat)((Field)localObject).get(paramViewPager);
      if (localObject != null)
      {
        Field localField = EdgeEffectCompat.class.getDeclaredField("mEdgeEffect");
        localField.setAccessible(true);
        localObject = (EdgeEffect)localField.get(localObject);
        if (localObject != null) {
          ((EdgeEffect)localObject).setColor(paramInt);
        }
      }
      localObject = ViewPager.class.getDeclaredField("mRightEdge");
      ((Field)localObject).setAccessible(true);
      paramViewPager = (EdgeEffectCompat)((Field)localObject).get(paramViewPager);
      if (paramViewPager != null)
      {
        localObject = EdgeEffectCompat.class.getDeclaredField("mEdgeEffect");
        ((Field)localObject).setAccessible(true);
        paramViewPager = (EdgeEffect)((Field)localObject).get(paramViewPager);
        if (paramViewPager != null) {
          paramViewPager.setColor(paramInt);
        }
      }
      return;
    }
    catch (Exception paramViewPager)
    {
      FileLog.e(paramViewPager);
    }
  }
  
  public static void setWaitingForCall(boolean paramBoolean)
  {
    synchronized (callLock)
    {
      waitingForCall = paramBoolean;
      return;
    }
  }
  
  public static void setWaitingForSms(boolean paramBoolean)
  {
    synchronized (smsLock)
    {
      waitingForSms = paramBoolean;
      return;
    }
  }
  
  public static void shakeView(View paramView, final float paramFloat, final int paramInt)
  {
    if (paramInt == 6)
    {
      paramView.setTranslationX(0.0F);
      return;
    }
    AnimatorSet localAnimatorSet = new AnimatorSet();
    localAnimatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(paramView, "translationX", new float[] { dp(paramFloat) }) });
    localAnimatorSet.setDuration(50L);
    localAnimatorSet.addListener(new AnimatorListenerAdapter()
    {
      public void onAnimationEnd(Animator paramAnonymousAnimator)
      {
        paramAnonymousAnimator = this.val$view;
        if (paramInt == 5) {}
        for (float f = 0.0F;; f = -paramFloat)
        {
          AndroidUtilities.shakeView(paramAnonymousAnimator, f, paramInt + 1);
          return;
        }
      }
    });
    localAnimatorSet.start();
  }
  
  public static void showKeyboard(View paramView)
  {
    if (paramView == null) {
      return;
    }
    try
    {
      ((InputMethodManager)paramView.getContext().getSystemService("input_method")).showSoftInput(paramView, 1);
      return;
    }
    catch (Exception paramView)
    {
      FileLog.e(paramView);
    }
  }
  
  public static void showProxyAlert(Activity paramActivity, String paramString1, final String paramString2, final String paramString3, final String paramString4)
  {
    paramActivity = new AlertDialog.Builder(paramActivity);
    paramActivity.setTitle(LocaleController.getString("Proxy", 2131494206));
    StringBuilder localStringBuilder = new StringBuilder(LocaleController.getString("EnableProxyAlert", 2131493425));
    localStringBuilder.append("\n\n");
    localStringBuilder.append(LocaleController.getString("UseProxyAddress", 2131494533)).append(": ").append(paramString1).append("\n");
    localStringBuilder.append(LocaleController.getString("UseProxyPort", 2131494538)).append(": ").append(paramString2).append("\n");
    if (!TextUtils.isEmpty(paramString3)) {
      localStringBuilder.append(LocaleController.getString("UseProxyUsername", 2131494540)).append(": ").append(paramString3).append("\n");
    }
    if (!TextUtils.isEmpty(paramString4)) {
      localStringBuilder.append(LocaleController.getString("UseProxyPassword", 2131494537)).append(": ").append(paramString4).append("\n");
    }
    localStringBuilder.append("\n").append(LocaleController.getString("EnableProxyAlert2", 2131493426));
    paramActivity.setMessage(localStringBuilder.toString());
    paramActivity.setPositiveButton(LocaleController.getString("ConnectingToProxyEnable", 2131493286), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        paramAnonymousDialogInterface = MessagesController.getGlobalMainSettings().edit();
        paramAnonymousDialogInterface.putBoolean("proxy_enabled", true);
        paramAnonymousDialogInterface.putString("proxy_ip", this.val$address);
        int i = Utilities.parseInt(paramString2).intValue();
        paramAnonymousDialogInterface.putInt("proxy_port", i);
        if (TextUtils.isEmpty(paramString4))
        {
          paramAnonymousDialogInterface.remove("proxy_pass");
          if (!TextUtils.isEmpty(paramString3)) {
            break label145;
          }
          paramAnonymousDialogInterface.remove("proxy_user");
        }
        for (;;)
        {
          paramAnonymousDialogInterface.commit();
          paramAnonymousInt = 0;
          while (paramAnonymousInt < 3)
          {
            ConnectionsManager.native_setProxySettings(paramAnonymousInt, this.val$address, i, paramString3, paramString4);
            paramAnonymousInt += 1;
          }
          paramAnonymousDialogInterface.putString("proxy_pass", paramString4);
          break;
          label145:
          paramAnonymousDialogInterface.putString("proxy_user", paramString3);
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged, new Object[0]);
      }
    });
    paramActivity.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
    paramActivity.show().setCanceledOnTouchOutside(true);
  }
  
  public static void unlockOrientation(Activity paramActivity)
  {
    if (paramActivity == null) {}
    for (;;)
    {
      return;
      try
      {
        if (prevOrientation != -10)
        {
          paramActivity.setRequestedOrientation(prevOrientation);
          prevOrientation = -10;
          return;
        }
      }
      catch (Exception paramActivity)
      {
        FileLog.e(paramActivity);
      }
    }
  }
  
  public static void unregisterUpdates()
  {
    if (BuildVars.DEBUG_VERSION) {
      UpdateManager.unregister();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/AndroidUtilities.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */