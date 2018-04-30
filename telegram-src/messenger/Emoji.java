package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

public class Emoji
{
  private static int bigImgSize = 0;
  private static final int[][] cols;
  private static int drawImgSize = 0;
  private static Bitmap[][] emojiBmp;
  public static HashMap<String, String> emojiColor;
  public static HashMap<String, Integer> emojiUseHistory;
  private static boolean inited = false;
  private static boolean[][] loadingEmoji;
  private static Paint placeholderPaint;
  public static ArrayList<String> recentEmoji;
  private static boolean recentEmojiLoaded = false;
  private static HashMap<CharSequence, DrawableInfo> rects = new HashMap();
  private static final int splitCount = 4;
  
  static
  {
    inited = false;
    emojiBmp = (Bitmap[][])Array.newInstance(Bitmap.class, new int[] { 5, 4 });
    loadingEmoji = (boolean[][])Array.newInstance(Boolean.TYPE, new int[] { 5, 4 });
    emojiUseHistory = new HashMap();
    recentEmoji = new ArrayList();
    emojiColor = new HashMap();
    cols = new int[][] { { 16, 16, 16, 16 }, { 6, 6, 6, 6 }, { 9, 9, 9, 9 }, { 9, 9, 9, 9 }, { 10, 10, 10, 10 } };
    int j = 2;
    int i;
    float f;
    label258:
    int k;
    if (AndroidUtilities.density <= 1.0F)
    {
      i = 32;
      j = 1;
      drawImgSize = AndroidUtilities.dp(20.0F);
      if (!AndroidUtilities.isTablet()) {
        break label473;
      }
      f = 40.0F;
      bigImgSize = AndroidUtilities.dp(f);
      k = 0;
    }
    for (;;)
    {
      if (k >= EmojiData.data.length) {
        break label486;
      }
      int n = (int)Math.ceil(EmojiData.data[k].length / 4.0F);
      int m = 0;
      for (;;)
      {
        if (m < EmojiData.data[k].length)
        {
          int i1 = m / n;
          int i3 = m - i1 * n;
          int i2 = i3 % cols[k][i1];
          i3 /= cols[k][i1];
          Rect localRect = new Rect(i2 * i + i2 * j, i3 * i + i3 * j, (i2 + 1) * i + i2 * j, (i3 + 1) * i + i3 * j);
          rects.put(EmojiData.data[k][m], new DrawableInfo(localRect, (byte)k, (byte)i1, m));
          m += 1;
          continue;
          if (AndroidUtilities.density <= 1.5F)
          {
            i = 64;
            break;
          }
          if (AndroidUtilities.density <= 2.0F)
          {
            i = 64;
            break;
          }
          i = 64;
          break;
          label473:
          f = 32.0F;
          break label258;
        }
      }
      k += 1;
    }
    label486:
    placeholderPaint = new Paint();
    placeholderPaint.setColor(0);
  }
  
  public static void addRecentEmoji(String paramString)
  {
    Object localObject2 = (Integer)emojiUseHistory.get(paramString);
    Object localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = Integer.valueOf(0);
    }
    int i;
    if ((((Integer)localObject1).intValue() == 0) && (emojiUseHistory.size() > 50)) {
      i = recentEmoji.size() - 1;
    }
    for (;;)
    {
      if (i >= 0)
      {
        localObject2 = (String)recentEmoji.get(i);
        emojiUseHistory.remove(localObject2);
        recentEmoji.remove(i);
        if (emojiUseHistory.size() > 50) {}
      }
      else
      {
        emojiUseHistory.put(paramString, Integer.valueOf(((Integer)localObject1).intValue() + 1));
        return;
      }
      i -= 1;
    }
  }
  
  public static void clearRecentEmoji()
  {
    MessagesController.getGlobalEmojiSettings().edit().putBoolean("filled_default", true).commit();
    emojiUseHistory.clear();
    recentEmoji.clear();
    saveRecentEmoji();
  }
  
  public static String fixEmoji(String paramString)
  {
    int n = paramString.length();
    int k = 0;
    String str = paramString;
    int i;
    int j;
    int m;
    if (k < n)
    {
      i = str.charAt(k);
      if ((i >= 55356) && (i <= 55358)) {
        if ((i == 55356) && (k < n - 1))
        {
          j = str.charAt(k + 1);
          if ((j == 56879) || (j == 56324) || (j == 56858) || (j == 56703))
          {
            paramString = str.substring(0, k + 2) + "️" + str.substring(k + 2);
            m = n + 1;
            j = k + 2;
          }
        }
      }
    }
    for (;;)
    {
      k = j + 1;
      n = m;
      str = paramString;
      break;
      j = k + 1;
      m = n;
      paramString = str;
      continue;
      j = k + 1;
      m = n;
      paramString = str;
      continue;
      if (i == 8419) {
        return str;
      }
      j = k;
      m = n;
      paramString = str;
      if (i >= 8252)
      {
        j = k;
        m = n;
        paramString = str;
        if (i <= 12953)
        {
          j = k;
          m = n;
          paramString = str;
          if (EmojiData.emojiToFE0FMap.containsKey(Character.valueOf(i)))
          {
            paramString = str.substring(0, k + 1) + "️" + str.substring(k + 1);
            m = n + 1;
            j = k + 1;
          }
        }
      }
    }
  }
  
  public static Drawable getEmojiBigDrawable(String paramString)
  {
    EmojiDrawable localEmojiDrawable2 = getEmojiDrawable(paramString);
    EmojiDrawable localEmojiDrawable1 = localEmojiDrawable2;
    if (localEmojiDrawable2 == null)
    {
      paramString = (CharSequence)EmojiData.emojiAliasMap.get(paramString);
      localEmojiDrawable1 = localEmojiDrawable2;
      if (paramString != null) {
        localEmojiDrawable1 = getEmojiDrawable(paramString);
      }
    }
    if (localEmojiDrawable1 == null) {
      return null;
    }
    localEmojiDrawable1.setBounds(0, 0, bigImgSize, bigImgSize);
    EmojiDrawable.access$102(localEmojiDrawable1, true);
    return localEmojiDrawable1;
  }
  
  public static EmojiDrawable getEmojiDrawable(CharSequence paramCharSequence)
  {
    DrawableInfo localDrawableInfo2 = (DrawableInfo)rects.get(paramCharSequence);
    DrawableInfo localDrawableInfo1 = localDrawableInfo2;
    if (localDrawableInfo2 == null)
    {
      CharSequence localCharSequence = (CharSequence)EmojiData.emojiAliasMap.get(paramCharSequence);
      localDrawableInfo1 = localDrawableInfo2;
      if (localCharSequence != null) {
        localDrawableInfo1 = (DrawableInfo)rects.get(localCharSequence);
      }
    }
    if (localDrawableInfo1 == null)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("No drawable for emoji " + paramCharSequence);
      }
      return null;
    }
    paramCharSequence = new EmojiDrawable(localDrawableInfo1);
    paramCharSequence.setBounds(0, 0, drawImgSize, drawImgSize);
    return paramCharSequence;
  }
  
  public static native Object[] getSuggestion(String paramString);
  
  private static boolean inArray(char paramChar, char[] paramArrayOfChar)
  {
    boolean bool2 = false;
    int j = paramArrayOfChar.length;
    int i = 0;
    for (;;)
    {
      boolean bool1 = bool2;
      if (i < j)
      {
        if (paramArrayOfChar[i] == paramChar) {
          bool1 = true;
        }
      }
      else {
        return bool1;
      }
      i += 1;
    }
  }
  
  public static void invalidateAll(View paramView)
  {
    if ((paramView instanceof ViewGroup))
    {
      paramView = (ViewGroup)paramView;
      int i = 0;
      while (i < paramView.getChildCount())
      {
        invalidateAll(paramView.getChildAt(i));
        i += 1;
      }
    }
    if ((paramView instanceof TextView)) {
      paramView.invalidate();
    }
  }
  
  public static boolean isValidEmoji(String paramString)
  {
    DrawableInfo localDrawableInfo2 = (DrawableInfo)rects.get(paramString);
    DrawableInfo localDrawableInfo1 = localDrawableInfo2;
    if (localDrawableInfo2 == null)
    {
      paramString = (CharSequence)EmojiData.emojiAliasMap.get(paramString);
      localDrawableInfo1 = localDrawableInfo2;
      if (paramString != null) {
        localDrawableInfo1 = (DrawableInfo)rects.get(paramString);
      }
    }
    return localDrawableInfo1 != null;
  }
  
  private static void loadEmoji(int paramInt1, final int paramInt2)
  {
    int i = 1;
    try
    {
      float f = AndroidUtilities.density;
      if (f <= 1.0F) {
        i = 2;
      }
      for (;;)
      {
        j = 4;
        for (;;)
        {
          if (j >= 7) {
            break label183;
          }
          try
          {
            localObject1 = String.format(Locale.US, "v%d_emoji%.01fx_%d.jpg", new Object[] { Integer.valueOf(j), Float.valueOf(2.0F), Integer.valueOf(paramInt1) });
            localObject1 = ApplicationLoader.applicationContext.getFileStreamPath((String)localObject1);
            if (((File)localObject1).exists()) {
              ((File)localObject1).delete();
            }
            localObject1 = String.format(Locale.US, "v%d_emoji%.01fx_a_%d.jpg", new Object[] { Integer.valueOf(j), Float.valueOf(2.0F), Integer.valueOf(paramInt1) });
            localObject1 = ApplicationLoader.applicationContext.getFileStreamPath((String)localObject1);
            if (((File)localObject1).exists()) {
              ((File)localObject1).delete();
            }
            j += 1;
          }
          catch (Exception localException)
          {
            Object localObject1;
            FileLog.e(localException);
            Bitmap localBitmap = null;
            final Object localObject2 = localBitmap;
            try
            {
              InputStream localInputStream = ApplicationLoader.applicationContext.getAssets().open("emoji/" + String.format(Locale.US, "v12_emoji%.01fx_%d_%d.png", new Object[] { Float.valueOf(2.0F), Integer.valueOf(paramInt1), Integer.valueOf(paramInt2) }));
              localObject2 = localBitmap;
              BitmapFactory.Options localOptions = new BitmapFactory.Options();
              localObject2 = localBitmap;
              localOptions.inJustDecodeBounds = false;
              localObject2 = localBitmap;
              localOptions.inSampleSize = i;
              localObject2 = localBitmap;
              localBitmap = BitmapFactory.decodeStream(localInputStream, null, localOptions);
              localObject2 = localBitmap;
              localInputStream.close();
              localObject2 = localBitmap;
            }
            catch (Throwable localThrowable2)
            {
              for (;;)
              {
                FileLog.e(localThrowable2);
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                Emoji.emojiBmp[this.val$page][paramInt2] = localObject2;
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.emojiDidLoaded, new Object[0]);
              }
            });
            return;
          }
        }
        if (AndroidUtilities.density > 1.5F)
        {
          f = AndroidUtilities.density;
          if (f <= 2.0F) {}
        }
      }
      label183:
      int j = 8;
      while (j < 12)
      {
        localObject1 = String.format(Locale.US, "v%d_emoji%.01fx_%d.png", new Object[] { Integer.valueOf(j), Float.valueOf(2.0F), Integer.valueOf(paramInt1) });
        localObject1 = ApplicationLoader.applicationContext.getFileStreamPath((String)localObject1);
        if (((File)localObject1).exists()) {
          ((File)localObject1).delete();
        }
        j += 1;
      }
      return;
    }
    catch (Throwable localThrowable1)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e("Error loading emoji", localThrowable1);
      }
    }
  }
  
  public static void loadRecentEmoji()
  {
    if (recentEmojiLoaded) {
      return;
    }
    recentEmojiLoaded = true;
    Object localObject1 = MessagesController.getGlobalEmojiSettings();
    for (;;)
    {
      int j;
      try
      {
        emojiUseHistory.clear();
        String[] arrayOfString;
        if (((SharedPreferences)localObject1).contains("emojis"))
        {
          localObject2 = ((SharedPreferences)localObject1).getString("emojis", "");
          if ((localObject2 != null) && (((String)localObject2).length() > 0))
          {
            localObject2 = ((String)localObject2).split(",");
            int k = localObject2.length;
            i = 0;
            if (i < k)
            {
              arrayOfString = localObject2[i].split("=");
              long l = Utilities.parseLong(arrayOfString[0]).longValue();
              StringBuilder localStringBuilder = new StringBuilder();
              j = 0;
              if (j < 4)
              {
                localStringBuilder.insert(0, String.valueOf((char)(int)l));
                l >>= 16;
                if (l != 0L) {
                  break label750;
                }
              }
              if (localStringBuilder.length() <= 0) {
                break label743;
              }
              emojiUseHistory.put(localStringBuilder.toString(), Utilities.parseInt(arrayOfString[1]));
              break label743;
            }
          }
          ((SharedPreferences)localObject1).edit().remove("emojis").commit();
          saveRecentEmoji();
          if ((!emojiUseHistory.isEmpty()) || (((SharedPreferences)localObject1).getBoolean("filled_default", false))) {
            continue;
          }
          localObject2 = new String[34];
          localObject2[0] = "😂";
          localObject2[1] = "😘";
          localObject2[2] = "❤";
          localObject2[3] = "😍";
          localObject2[4] = "😊";
          localObject2[5] = "😁";
          localObject2[6] = "👍";
          localObject2[7] = "☺";
          localObject2[8] = "😔";
          localObject2[9] = "😄";
          localObject2[10] = "😭";
          localObject2[11] = "💋";
          localObject2[12] = "😒";
          localObject2[13] = "😳";
          localObject2[14] = "😜";
          localObject2[15] = "🙈";
          localObject2[16] = "😉";
          localObject2[17] = "😃";
          localObject2[18] = "😢";
          localObject2[19] = "😝";
          localObject2[20] = "😱";
          localObject2[21] = "😡";
          localObject2[22] = "😏";
          localObject2[23] = "😞";
          localObject2[24] = "😅";
          localObject2[25] = "😚";
          localObject2[26] = "🙊";
          localObject2[27] = "😌";
          localObject2[28] = "😀";
          localObject2[29] = "😋";
          localObject2[30] = "😆";
          localObject2[31] = "👌";
          localObject2[32] = "😐";
          localObject2[33] = "😕";
          i = 0;
          if (i < localObject2.length)
          {
            emojiUseHistory.put(localObject2[i], Integer.valueOf(localObject2.length - i));
            i += 1;
            continue;
          }
        }
        else
        {
          localObject2 = ((SharedPreferences)localObject1).getString("emojis2", "");
          if ((localObject2 == null) || (((String)localObject2).length() <= 0)) {
            continue;
          }
          localObject2 = ((String)localObject2).split(",");
          j = localObject2.length;
          i = 0;
          if (i < j)
          {
            arrayOfString = localObject2[i].split("=");
            emojiUseHistory.put(arrayOfString[0], Utilities.parseInt(arrayOfString[1]));
            i += 1;
            continue;
          }
          continue;
        }
        ((SharedPreferences)localObject1).edit().putBoolean("filled_default", true).commit();
        saveRecentEmoji();
        sortEmoji();
      }
      catch (Exception localException2)
      {
        try
        {
          localObject1 = ((SharedPreferences)localObject1).getString("color", "");
          if ((localObject1 == null) || (((String)localObject1).length() <= 0)) {
            break;
          }
          localObject1 = ((String)localObject1).split(",");
          int i = 0;
          if (i >= localObject1.length) {
            break;
          }
          Object localObject2 = localObject1[i].split("=");
          emojiColor.put(localObject2[0], localObject2[1]);
          i += 1;
          continue;
          localException2 = localException2;
          FileLog.e(localException2);
          continue;
          i += 1;
        }
        catch (Exception localException1)
        {
          FileLog.e(localException1);
          return;
        }
      }
      label743:
      continue;
      label750:
      j += 1;
    }
  }
  
  public static CharSequence replaceEmoji(CharSequence paramCharSequence, Paint.FontMetricsInt paramFontMetricsInt, int paramInt, boolean paramBoolean)
  {
    return replaceEmoji(paramCharSequence, paramFontMetricsInt, paramInt, paramBoolean, null);
  }
  
  public static CharSequence replaceEmoji(CharSequence paramCharSequence, Paint.FontMetricsInt paramFontMetricsInt, int paramInt, boolean paramBoolean, int[] paramArrayOfInt)
  {
    if ((SharedConfig.useSystemEmoji) || (paramCharSequence == null) || (paramCharSequence.length() == 0)) {
      return paramCharSequence;
    }
    Spannable localSpannable;
    long l2;
    int i4;
    int i6;
    StringBuilder localStringBuilder;
    int i8;
    int i5;
    int i1;
    Object localObject;
    if ((!paramBoolean) && ((paramCharSequence instanceof Spannable)))
    {
      localSpannable = (Spannable)paramCharSequence;
      l2 = 0L;
      i4 = 0;
      i2 = -1;
      i3 = 0;
      i6 = 0;
      localStringBuilder = new StringBuilder(16);
      new StringBuilder(2);
      i8 = paramCharSequence.length();
      i5 = 0;
      i1 = 0;
      localObject = paramArrayOfInt;
      if (i1 >= i8) {
        break label1109;
      }
    }
    try
    {
      i = paramCharSequence.charAt(i1);
      if (i < 55356) {
        break label1144;
      }
      if (i <= 55358) {
        break label1191;
      }
    }
    catch (Exception paramFontMetricsInt)
    {
      int i;
      label125:
      FileLog.e(paramFontMetricsInt);
      return paramCharSequence;
    }
    localStringBuilder.append(i);
    int k = i3 + 1;
    long l1 = l2 << 16 | i;
    paramArrayOfInt = (int[])localObject;
    int m = i5;
    label286:
    int i7;
    int j;
    label394:
    label432:
    int n;
    char c;
    for (;;)
    {
      i2 = i1;
      i3 = k;
      if (m == 0) {
        break label1208;
      }
      i2 = i1;
      i3 = k;
      if (i1 + 2 >= i8) {
        break label1208;
      }
      i5 = paramCharSequence.charAt(i1 + 1);
      if (i5 != 55356) {
        break label736;
      }
      i5 = paramCharSequence.charAt(i1 + 2);
      i2 = i1;
      i3 = k;
      if (i5 < 57339) {
        break label1208;
      }
      i2 = i1;
      i3 = k;
      if (i5 > 57343) {
        break label1208;
      }
      localStringBuilder.append(paramCharSequence.subSequence(i1 + 1, i1 + 3));
      i3 = k + 2;
      i2 = i1 + 2;
      break label1208;
      while (i5 < 3)
      {
        i7 = m;
        k = i2;
        i1 = i3;
        if (i2 + 1 < i8)
        {
          j = paramCharSequence.charAt(i2 + 1);
          if (i5 != 1) {
            break label1379;
          }
          i7 = m;
          k = i2;
          i1 = i3;
          if (j == 8205)
          {
            i7 = m;
            k = i2;
            i1 = i3;
            if (localStringBuilder.length() > 0)
            {
              localStringBuilder.append(j);
              k = i2 + 1;
              i1 = i3 + 1;
              i7 = 0;
            }
          }
        }
        i5 += 1;
        m = i7;
        i2 = k;
        i3 = i1;
      }
      localSpannable = Spannable.Factory.getInstance().newSpannable(paramCharSequence.toString());
      break;
      if ((localStringBuilder.length() > 0) && ((j == 9792) || (j == 9794) || (j == 9877)))
      {
        localStringBuilder.append(j);
        k = i3 + 1;
        l1 = 0L;
        m = 1;
        n = i2;
        paramArrayOfInt = (int[])localObject;
      }
      else if ((l2 > 0L) && ((0xF000 & j) == 53248))
      {
        localStringBuilder.append(j);
        k = i3 + 1;
        l1 = 0L;
        m = 1;
        n = i2;
        paramArrayOfInt = (int[])localObject;
      }
      else
      {
        if (j != 8419) {
          break label1255;
        }
        l1 = l2;
        m = i5;
        n = i2;
        k = i3;
        paramArrayOfInt = (int[])localObject;
        if (i1 > 0)
        {
          c = paramCharSequence.charAt(i6);
          if ((c < '0') || (c > '9')) {
            break label1218;
          }
          label605:
          n = i6;
          k = i1 - i6 + 1;
          localStringBuilder.append(c);
          localStringBuilder.append(j);
          m = 1;
          l1 = l2;
          paramArrayOfInt = (int[])localObject;
          continue;
          label648:
          if (EmojiData.dataCharsMap.containsKey(Character.valueOf(j)))
          {
            n = i2;
            if (i2 == -1) {
              n = i1;
            }
            k = i3 + 1;
            localStringBuilder.append(j);
            m = 1;
            l1 = l2;
            paramArrayOfInt = (int[])localObject;
          }
          else
          {
            label704:
            if (i2 == -1) {
              break label1290;
            }
            localStringBuilder.setLength(0);
            n = -1;
            k = 0;
            m = 0;
            l1 = l2;
            paramArrayOfInt = (int[])localObject;
          }
        }
      }
    }
    label736:
    int i2 = i1;
    int i3 = k;
    if (localStringBuilder.length() >= 2)
    {
      i2 = i1;
      i3 = k;
      if (localStringBuilder.charAt(0) == 55356)
      {
        i2 = i1;
        i3 = k;
        if (localStringBuilder.charAt(1) == 57332)
        {
          i2 = i1;
          i3 = k;
          if (i5 == 56128)
          {
            i1 += 1;
            do
            {
              localStringBuilder.append(paramCharSequence.subSequence(i1, i1 + 2));
              i3 = k + 2;
              i2 = i1 + 2;
              if (i2 >= paramCharSequence.length()) {
                break;
              }
              i1 = i2;
              k = i3;
            } while (paramCharSequence.charAt(i2) == 56128);
            break label1370;
            i7 = i2;
            i1 = i3;
            if (m == 0) {
              break label1438;
            }
            i7 = i2;
            i1 = i3;
            if (i2 + 2 >= i8) {
              break label1438;
            }
            i7 = i2;
            i1 = i3;
            if (paramCharSequence.charAt(i2 + 1) != 55356) {
              break label1438;
            }
            k = paramCharSequence.charAt(i2 + 2);
            i7 = i2;
            i1 = i3;
            if (k < 57339) {
              break label1438;
            }
            i7 = i2;
            i1 = i3;
            if (k > 57343) {
              break label1438;
            }
            localStringBuilder.append(paramCharSequence.subSequence(i2 + 1, i2 + 3));
            i1 = i3 + 2;
            i7 = i2 + 2;
            break label1438;
          }
        }
      }
    }
    for (;;)
    {
      localObject = getEmojiDrawable(localStringBuilder.subSequence(0, localStringBuilder.length()));
      m = i4;
      if (localObject != null)
      {
        localSpannable.setSpan(new EmojiSpan((EmojiDrawable)localObject, 0, paramInt, paramFontMetricsInt), n, n + i1, 33);
        m = i4 + 1;
      }
      k = 0;
      i2 = -1;
      localStringBuilder.setLength(0);
      i5 = 0;
      i3 = m;
      label1109:
      label1144:
      label1191:
      label1208:
      label1218:
      label1255:
      label1290:
      label1370:
      label1379:
      label1438:
      do
      {
        m = Build.VERSION.SDK_INT;
        if ((m < 23) && (i3 >= 50)) {
          return localSpannable;
        }
        i1 = i7 + 1;
        l2 = l1;
        i4 = i3;
        i3 = k;
        localObject = paramArrayOfInt;
        break;
        if ((l2 == 0L) || ((0xFFFFFFFF00000000 & l2) != 0L) || ((0xFFFF & l2) != 55356L) || (j < 56806) || (j > 56831)) {
          break label432;
        }
        n = i2;
        if (i2 != -1) {
          break label125;
        }
        n = i1;
        break label125;
        for (;;)
        {
          i6 = i2;
          i5 = 0;
          break label286;
          if (c == '#') {
            break label605;
          }
          l1 = l2;
          m = i5;
          n = i2;
          k = i3;
          paramArrayOfInt = (int[])localObject;
          if (c != '*') {
            break;
          }
          break label605;
          if ((j == 169) || (j == 174)) {
            break label648;
          }
          if ((j < 8252) || (j > 12953)) {
            break label704;
          }
          break label648;
          l1 = l2;
          m = i5;
          n = i2;
          k = i3;
          paramArrayOfInt = (int[])localObject;
          if (j == 65039) {
            break;
          }
          l1 = l2;
          m = i5;
          n = i2;
          k = i3;
          paramArrayOfInt = (int[])localObject;
          if (localObject == null) {
            break;
          }
          localObject[0] = 0;
          paramArrayOfInt = null;
          l1 = l2;
          m = i5;
          n = i2;
          k = i3;
          break;
          i2 -= 1;
        }
        i7 = m;
        k = i2;
        i1 = i3;
        if (j < 65024) {
          break label394;
        }
        i7 = m;
        k = i2;
        i1 = i3;
        if (j > 65039) {
          break label394;
        }
        k = i2 + 1;
        i1 = i3 + 1;
        i7 = m;
        break label394;
        i5 = m;
        i3 = i4;
        i2 = n;
        k = i1;
      } while (m == 0);
      if (paramArrayOfInt != null) {
        paramArrayOfInt[0] += 1;
      }
    }
  }
  
  public static void saveEmojiColors()
  {
    SharedPreferences localSharedPreferences = MessagesController.getGlobalEmojiSettings();
    StringBuilder localStringBuilder = new StringBuilder();
    Iterator localIterator = emojiColor.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (localStringBuilder.length() != 0) {
        localStringBuilder.append(",");
      }
      localStringBuilder.append((String)localEntry.getKey());
      localStringBuilder.append("=");
      localStringBuilder.append((String)localEntry.getValue());
    }
    localSharedPreferences.edit().putString("color", localStringBuilder.toString()).commit();
  }
  
  public static void saveRecentEmoji()
  {
    SharedPreferences localSharedPreferences = MessagesController.getGlobalEmojiSettings();
    StringBuilder localStringBuilder = new StringBuilder();
    Iterator localIterator = emojiUseHistory.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (localStringBuilder.length() != 0) {
        localStringBuilder.append(",");
      }
      localStringBuilder.append((String)localEntry.getKey());
      localStringBuilder.append("=");
      localStringBuilder.append(localEntry.getValue());
    }
    localSharedPreferences.edit().putString("emojis2", localStringBuilder.toString()).commit();
  }
  
  public static void sortEmoji()
  {
    recentEmoji.clear();
    Iterator localIterator = emojiUseHistory.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      recentEmoji.add(localEntry.getKey());
    }
    Collections.sort(recentEmoji, new Comparator()
    {
      public int compare(String paramAnonymousString1, String paramAnonymousString2)
      {
        int i = 0;
        Integer localInteger2 = (Integer)Emoji.emojiUseHistory.get(paramAnonymousString1);
        Integer localInteger1 = (Integer)Emoji.emojiUseHistory.get(paramAnonymousString2);
        paramAnonymousString1 = localInteger2;
        if (localInteger2 == null) {
          paramAnonymousString1 = Integer.valueOf(0);
        }
        paramAnonymousString2 = localInteger1;
        if (localInteger1 == null) {
          paramAnonymousString2 = Integer.valueOf(0);
        }
        if (paramAnonymousString1.intValue() > paramAnonymousString2.intValue()) {
          i = -1;
        }
        while (paramAnonymousString1.intValue() >= paramAnonymousString2.intValue()) {
          return i;
        }
        return 1;
      }
    });
    while (recentEmoji.size() > 50) {
      recentEmoji.remove(recentEmoji.size() - 1);
    }
  }
  
  private static class DrawableInfo
  {
    public int emojiIndex;
    public byte page;
    public byte page2;
    public Rect rect;
    
    public DrawableInfo(Rect paramRect, byte paramByte1, byte paramByte2, int paramInt)
    {
      this.rect = paramRect;
      this.page = paramByte1;
      this.page2 = paramByte2;
      this.emojiIndex = paramInt;
    }
  }
  
  public static class EmojiDrawable
    extends Drawable
  {
    private static Paint paint = new Paint(2);
    private static Rect rect = new Rect();
    private static TextPaint textPaint = new TextPaint(1);
    private boolean fullSize = false;
    private Emoji.DrawableInfo info;
    
    public EmojiDrawable(Emoji.DrawableInfo paramDrawableInfo)
    {
      this.info = paramDrawableInfo;
    }
    
    public void draw(Canvas paramCanvas)
    {
      if (Emoji.emojiBmp[this.info.page][this.info.page2] == null)
      {
        if (Emoji.loadingEmoji[this.info.page][this.info.page2] != 0) {
          return;
        }
        Emoji.loadingEmoji[this.info.page][this.info.page2] = 1;
        Utilities.globalQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            Emoji.loadEmoji(Emoji.EmojiDrawable.this.info.page, Emoji.EmojiDrawable.this.info.page2);
            Emoji.loadingEmoji[Emoji.EmojiDrawable.this.info.page][Emoji.EmojiDrawable.this.info.page2] = 0;
          }
        });
        paramCanvas.drawRect(getBounds(), Emoji.placeholderPaint);
        return;
      }
      if (this.fullSize) {}
      for (Rect localRect = getDrawRect();; localRect = getBounds())
      {
        paramCanvas.drawBitmap(Emoji.emojiBmp[this.info.page][this.info.page2], this.info.rect, localRect, paint);
        return;
      }
    }
    
    public Rect getDrawRect()
    {
      Rect localRect = getBounds();
      int k = localRect.centerX();
      int j = localRect.centerY();
      localRect = rect;
      if (this.fullSize)
      {
        i = Emoji.bigImgSize;
        localRect.left = (k - i / 2);
        localRect = rect;
        if (!this.fullSize) {
          break label133;
        }
        i = Emoji.bigImgSize;
        label60:
        localRect.right = (i / 2 + k);
        localRect = rect;
        if (!this.fullSize) {
          break label140;
        }
        i = Emoji.bigImgSize;
        label86:
        localRect.top = (j - i / 2);
        localRect = rect;
        if (!this.fullSize) {
          break label147;
        }
      }
      label133:
      label140:
      label147:
      for (int i = Emoji.bigImgSize;; i = Emoji.drawImgSize)
      {
        localRect.bottom = (i / 2 + j);
        return rect;
        i = Emoji.drawImgSize;
        break;
        i = Emoji.drawImgSize;
        break label60;
        i = Emoji.drawImgSize;
        break label86;
      }
    }
    
    public Emoji.DrawableInfo getDrawableInfo()
    {
      return this.info;
    }
    
    public int getOpacity()
    {
      return -2;
    }
    
    public void setAlpha(int paramInt) {}
    
    public void setColorFilter(ColorFilter paramColorFilter) {}
  }
  
  public static class EmojiSpan
    extends ImageSpan
  {
    private Paint.FontMetricsInt fontMetrics = null;
    private int size = AndroidUtilities.dp(20.0F);
    
    public EmojiSpan(Emoji.EmojiDrawable paramEmojiDrawable, int paramInt1, int paramInt2, Paint.FontMetricsInt paramFontMetricsInt)
    {
      super(paramInt1);
      this.fontMetrics = paramFontMetricsInt;
      if (paramFontMetricsInt != null)
      {
        this.size = (Math.abs(this.fontMetrics.descent) + Math.abs(this.fontMetrics.ascent));
        if (this.size == 0) {
          this.size = AndroidUtilities.dp(20.0F);
        }
      }
    }
    
    public int getSize(Paint paramPaint, CharSequence paramCharSequence, int paramInt1, int paramInt2, Paint.FontMetricsInt paramFontMetricsInt)
    {
      Paint.FontMetricsInt localFontMetricsInt = paramFontMetricsInt;
      if (paramFontMetricsInt == null) {
        localFontMetricsInt = new Paint.FontMetricsInt();
      }
      if (this.fontMetrics == null)
      {
        paramInt1 = super.getSize(paramPaint, paramCharSequence, paramInt1, paramInt2, localFontMetricsInt);
        paramInt2 = AndroidUtilities.dp(8.0F);
        int i = AndroidUtilities.dp(10.0F);
        localFontMetricsInt.top = (-i - paramInt2);
        localFontMetricsInt.bottom = (i - paramInt2);
        localFontMetricsInt.ascent = (-i - paramInt2);
        localFontMetricsInt.leading = 0;
        localFontMetricsInt.descent = (i - paramInt2);
        return paramInt1;
      }
      if (localFontMetricsInt != null)
      {
        localFontMetricsInt.ascent = this.fontMetrics.ascent;
        localFontMetricsInt.descent = this.fontMetrics.descent;
        localFontMetricsInt.top = this.fontMetrics.top;
        localFontMetricsInt.bottom = this.fontMetrics.bottom;
      }
      if (getDrawable() != null) {
        getDrawable().setBounds(0, 0, this.size, this.size);
      }
      return this.size;
    }
    
    public void replaceFontMetrics(Paint.FontMetricsInt paramFontMetricsInt, int paramInt)
    {
      this.fontMetrics = paramFontMetricsInt;
      this.size = paramInt;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/Emoji.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */