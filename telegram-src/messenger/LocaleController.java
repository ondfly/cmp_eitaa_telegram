package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.LangPackString;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_langPackDifference;
import org.telegram.tgnet.TLRPC.TL_langPackLanguage;
import org.telegram.tgnet.TLRPC.TL_langPackString;
import org.telegram.tgnet.TLRPC.TL_langPackStringDeleted;
import org.telegram.tgnet.TLRPC.TL_langPackStringPluralized;
import org.telegram.tgnet.TLRPC.TL_langpack_getDifference;
import org.telegram.tgnet.TLRPC.TL_langpack_getLangPack;
import org.telegram.tgnet.TLRPC.TL_langpack_getLanguages;
import org.telegram.tgnet.TLRPC.TL_userEmpty;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.tgnet.TLRPC.Vector;

public class LocaleController
{
  private static volatile LocaleController Instance = null;
  static final int QUANTITY_FEW = 8;
  static final int QUANTITY_MANY = 16;
  static final int QUANTITY_ONE = 2;
  static final int QUANTITY_OTHER = 0;
  static final int QUANTITY_TWO = 4;
  static final int QUANTITY_ZERO = 1;
  public static boolean is24HourFormat;
  public static boolean isRTL = false;
  public static int nameDisplayOrder = 1;
  private HashMap<String, PluralRules> allRules = new HashMap();
  private boolean changingConfiguration = false;
  public FastDateFormat chatDate;
  public FastDateFormat chatFullDate;
  private HashMap<String, String> currencyValues;
  private Locale currentLocale;
  private LocaleInfo currentLocaleInfo;
  private PluralRules currentPluralRules;
  public FastDateFormat formatterBannedUntil;
  public FastDateFormat formatterBannedUntilThisYear;
  public FastDateFormat formatterDay;
  public FastDateFormat formatterMonth;
  public FastDateFormat formatterMonthYear;
  public FastDateFormat formatterStats;
  public FastDateFormat formatterWeek;
  public FastDateFormat formatterYear;
  public FastDateFormat formatterYearMax;
  private String languageOverride;
  public ArrayList<LocaleInfo> languages = new ArrayList();
  public HashMap<String, LocaleInfo> languagesDict = new HashMap();
  private boolean loadingRemoteLanguages;
  private HashMap<String, String> localeValues = new HashMap();
  private ArrayList<LocaleInfo> otherLanguages = new ArrayList();
  private boolean reloadLastFile;
  public ArrayList<LocaleInfo> remoteLanguages = new ArrayList();
  private Locale systemDefaultLocale;
  private HashMap<String, String> translitChars;
  
  static
  {
    is24HourFormat = false;
  }
  
  public LocaleController()
  {
    Object localObject1 = new PluralRules_One();
    addRules(new String[] { "bem", "brx", "da", "de", "el", "en", "eo", "es", "et", "fi", "fo", "gl", "he", "iw", "it", "nb", "nl", "nn", "no", "sv", "af", "bg", "bn", "ca", "eu", "fur", "fy", "gu", "ha", "is", "ku", "lb", "ml", "mr", "nah", "ne", "om", "or", "pa", "pap", "ps", "so", "sq", "sw", "ta", "te", "tk", "ur", "zu", "mn", "gsw", "chr", "rm", "pt", "an", "ast" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Czech();
    addRules(new String[] { "cs", "sk" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_French();
    addRules(new String[] { "ff", "fr", "kab" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Balkan();
    addRules(new String[] { "hr", "ru", "sr", "uk", "be", "bs", "sh" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Latvian();
    addRules(new String[] { "lv" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Lithuanian();
    addRules(new String[] { "lt" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Polish();
    addRules(new String[] { "pl" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Romanian();
    addRules(new String[] { "ro", "mo" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Slovenian();
    addRules(new String[] { "sl" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Arabic();
    addRules(new String[] { "ar" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Macedonian();
    addRules(new String[] { "mk" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Welsh();
    addRules(new String[] { "cy" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Breton();
    addRules(new String[] { "br" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Langi();
    addRules(new String[] { "lag" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Tachelhit();
    addRules(new String[] { "shi" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Maltese();
    addRules(new String[] { "mt" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Two();
    addRules(new String[] { "ga", "se", "sma", "smi", "smj", "smn", "sms" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_Zero();
    addRules(new String[] { "ak", "am", "bh", "fil", "tl", "guw", "hi", "ln", "mg", "nso", "ti", "wa" }, (PluralRules)localObject1);
    localObject1 = new PluralRules_None();
    addRules(new String[] { "az", "bm", "fa", "ig", "hu", "ja", "kde", "kea", "ko", "my", "ses", "sg", "to", "tr", "vi", "wo", "yo", "zh", "bo", "dz", "id", "jv", "jw", "ka", "km", "kn", "ms", "th", "in" }, (PluralRules)localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "English";
    ((LocaleInfo)localObject1).nameEnglish = "English";
    ((LocaleInfo)localObject1).shortName = "en";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "Italiano";
    ((LocaleInfo)localObject1).nameEnglish = "Italian";
    ((LocaleInfo)localObject1).shortName = "it";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "Español";
    ((LocaleInfo)localObject1).nameEnglish = "Spanish";
    ((LocaleInfo)localObject1).shortName = "es";
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "Deutsch";
    ((LocaleInfo)localObject1).nameEnglish = "German";
    ((LocaleInfo)localObject1).shortName = "de";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "Nederlands";
    ((LocaleInfo)localObject1).nameEnglish = "Dutch";
    ((LocaleInfo)localObject1).shortName = "nl";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "العربية";
    ((LocaleInfo)localObject1).nameEnglish = "Arabic";
    ((LocaleInfo)localObject1).shortName = "ar";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "Português (Brasil)";
    ((LocaleInfo)localObject1).nameEnglish = "Portuguese (Brazil)";
    ((LocaleInfo)localObject1).shortName = "pt_br";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    localObject1 = new LocaleInfo();
    ((LocaleInfo)localObject1).name = "한국어";
    ((LocaleInfo)localObject1).nameEnglish = "Korean";
    ((LocaleInfo)localObject1).shortName = "ko";
    ((LocaleInfo)localObject1).pathToFile = null;
    ((LocaleInfo)localObject1).builtIn = true;
    this.languages.add(localObject1);
    this.languagesDict.put(((LocaleInfo)localObject1).shortName, localObject1);
    loadOtherLanguages();
    if (this.remoteLanguages.isEmpty()) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          LocaleController.this.loadRemoteLanguages(UserConfig.selectedAccount);
        }
      });
    }
    int i = 0;
    while (i < this.otherLanguages.size())
    {
      localObject1 = (LocaleInfo)this.otherLanguages.get(i);
      this.languages.add(localObject1);
      this.languagesDict.put(((LocaleInfo)localObject1).getKey(), localObject1);
      i += 1;
    }
    i = 0;
    Object localObject2;
    if (i < this.remoteLanguages.size())
    {
      localObject1 = (LocaleInfo)this.remoteLanguages.get(i);
      localObject2 = getLanguageFromDict(((LocaleInfo)localObject1).getKey());
      if (localObject2 != null)
      {
        ((LocaleInfo)localObject2).pathToFile = ((LocaleInfo)localObject1).pathToFile;
        ((LocaleInfo)localObject2).version = ((LocaleInfo)localObject1).version;
        this.remoteLanguages.set(i, localObject2);
      }
      for (;;)
      {
        i += 1;
        break;
        this.languages.add(localObject1);
        this.languagesDict.put(((LocaleInfo)localObject1).getKey(), localObject1);
      }
    }
    this.systemDefaultLocale = Locale.getDefault();
    is24HourFormat = DateFormat.is24HourFormat(ApplicationLoader.applicationContext);
    localObject1 = null;
    boolean bool2 = false;
    try
    {
      localObject2 = MessagesController.getGlobalMainSettings().getString("language", null);
      boolean bool1 = bool2;
      if (localObject2 != null)
      {
        localObject2 = getLanguageFromDict((String)localObject2);
        localObject1 = localObject2;
        bool1 = bool2;
        if (localObject2 != null)
        {
          bool1 = true;
          localObject1 = localObject2;
        }
      }
      localObject2 = localObject1;
      if (localObject1 == null)
      {
        localObject2 = localObject1;
        if (this.systemDefaultLocale.getLanguage() != null) {
          localObject2 = getLanguageFromDict(this.systemDefaultLocale.getLanguage());
        }
      }
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        localObject2 = getLanguageFromDict(getLocaleString(this.systemDefaultLocale));
        localObject1 = localObject2;
        if (localObject2 == null) {
          localObject1 = getLanguageFromDict("en");
        }
      }
      applyLanguage((LocaleInfo)localObject1, bool1, true, UserConfig.selectedAccount);
      return;
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        try
        {
          localObject1 = new IntentFilter("android.intent.action.TIMEZONE_CHANGED");
          ApplicationLoader.applicationContext.registerReceiver(new TimeZoneChangedReceiver(null), (IntentFilter)localObject1);
          return;
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
        localException1 = localException1;
        FileLog.e(localException1);
      }
    }
  }
  
  public static String addNbsp(String paramString)
  {
    return paramString.replace(' ', ' ');
  }
  
  private void addRules(String[] paramArrayOfString, PluralRules paramPluralRules)
  {
    int j = paramArrayOfString.length;
    int i = 0;
    while (i < j)
    {
      String str = paramArrayOfString[i];
      this.allRules.put(str, paramPluralRules);
      i += 1;
    }
  }
  
  private void applyRemoteLanguage(LocaleInfo paramLocaleInfo, boolean paramBoolean, final int paramInt)
  {
    if ((paramLocaleInfo == null) || ((paramLocaleInfo != null) && (!paramLocaleInfo.isRemote()))) {
      return;
    }
    if ((paramLocaleInfo.version != 0) && (!paramBoolean))
    {
      localObject = new TLRPC.TL_langpack_getDifference();
      ((TLRPC.TL_langpack_getDifference)localObject).from_version = paramLocaleInfo.version;
      ConnectionsManager.getInstance(paramInt).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                LocaleController.this.saveRemoteLocaleStrings((TLRPC.TL_langPackDifference)paramAnonymousTLObject, LocaleController.6.this.val$currentAccount);
              }
            });
          }
        }
      }, 8);
      return;
    }
    int i = 0;
    while (i < 3)
    {
      ConnectionsManager.setLangCode(paramLocaleInfo.shortName);
      i += 1;
    }
    Object localObject = new TLRPC.TL_langpack_getLangPack();
    ((TLRPC.TL_langpack_getLangPack)localObject).lang_code = paramLocaleInfo.shortName.replace("_", "-");
    ConnectionsManager.getInstance(paramInt).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LocaleController.this.saveRemoteLocaleStrings((TLRPC.TL_langPackDifference)paramAnonymousTLObject, LocaleController.7.this.val$currentAccount);
            }
          });
        }
      }
    }, 8);
  }
  
  private FastDateFormat createFormatter(Locale paramLocale, String paramString1, String paramString2)
  {
    String str;
    if (paramString1 != null)
    {
      str = paramString1;
      if (paramString1.length() != 0) {}
    }
    else
    {
      str = paramString2;
    }
    try
    {
      paramString1 = FastDateFormat.getInstance(str, paramLocale);
      return paramString1;
    }
    catch (Exception paramString1) {}
    return FastDateFormat.getInstance(paramString2, paramLocale);
  }
  
  private String escapeString(String paramString)
  {
    if (paramString.contains("[CDATA")) {
      return paramString;
    }
    return paramString.replace("<", "&lt;").replace(">", "&gt;").replace("& ", "&amp; ");
  }
  
  public static String formatCallDuration(int paramInt)
  {
    if (paramInt > 3600)
    {
      String str2 = formatPluralString("Hours", paramInt / 3600);
      paramInt = paramInt % 3600 / 60;
      String str1 = str2;
      if (paramInt > 0) {
        str1 = str2 + ", " + formatPluralString("Minutes", paramInt);
      }
      return str1;
    }
    if (paramInt > 60) {
      return formatPluralString("Minutes", paramInt / 60);
    }
    return formatPluralString("Seconds", paramInt);
  }
  
  public static String formatDate(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      int j = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int k = ((Calendar)localObject).get(6);
      int m = ((Calendar)localObject).get(1);
      if ((k == i) && (j == m)) {
        return getInstance().formatterDay.format(new Date(paramLong));
      }
      if ((k + 1 == i) && (j == m)) {
        return getString("Yesterday", 2131494652);
      }
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return getInstance().formatterMonth.format(new Date(paramLong));
      }
      localObject = getInstance().formatterYear.format(new Date(paramLong));
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR: formatDate";
  }
  
  public static String formatDateAudio(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      int j = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int k = ((Calendar)localObject).get(6);
      int m = ((Calendar)localObject).get(1);
      if ((k == i) && (j == m)) {
        return formatString("TodayAtFormatted", 2131494497, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) });
      }
      if ((k + 1 == i) && (j == m)) {
        return formatString("YesterdayAtFormatted", 2131494654, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) });
      }
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterMonth.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) });
      }
      localObject = formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterYear.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) });
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  public static String formatDateCallLog(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      int j = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int k = ((Calendar)localObject).get(6);
      int m = ((Calendar)localObject).get(1);
      if ((k == i) && (j == m)) {
        return getInstance().formatterDay.format(new Date(paramLong));
      }
      if ((k + 1 == i) && (j == m)) {
        return formatString("YesterdayAtFormatted", 2131494654, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) });
      }
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().chatDate.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) });
      }
      localObject = formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().chatFullDate.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) });
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  public static String formatDateChat(long paramLong)
  {
    try
    {
      Object localObject = Calendar.getInstance();
      paramLong *= 1000L;
      ((Calendar)localObject).setTimeInMillis(paramLong);
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return getInstance().chatDate.format(paramLong);
      }
      localObject = getInstance().chatFullDate.format(paramLong);
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR: formatDateChat";
  }
  
  public static String formatDateForBan(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      if (i == ((Calendar)localObject).get(1)) {
        return getInstance().formatterBannedUntilThisYear.format(new Date(paramLong));
      }
      localObject = getInstance().formatterBannedUntil.format(new Date(paramLong));
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  public static String formatDateOnline(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      int j = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int k = ((Calendar)localObject).get(6);
      int m = ((Calendar)localObject).get(1);
      if ((k == i) && (j == m)) {
        return formatString("LastSeenFormatted", 2131493736, new Object[] { formatString("TodayAtFormatted", 2131494497, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      if ((k + 1 == i) && (j == m)) {
        return formatString("LastSeenFormatted", 2131493736, new Object[] { formatString("YesterdayAtFormatted", 2131494654, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return formatString("LastSeenDateFormatted", 2131493733, new Object[] { formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterMonth.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      localObject = formatString("LastSeenDateFormatted", 2131493733, new Object[] { formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterYear.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) }) });
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  public static String formatLocationLeftTime(int paramInt)
  {
    int j = 1;
    int i = 1;
    int k = paramInt / 60 / 60;
    paramInt -= k * 60 * 60;
    int m = paramInt / 60;
    paramInt -= m * 60;
    if (k != 0)
    {
      if (m > 30) {}
      for (paramInt = i;; paramInt = 0) {
        return String.format("%dh", new Object[] { Integer.valueOf(paramInt + k) });
      }
    }
    if (m != 0)
    {
      if (paramInt > 30) {}
      for (paramInt = j;; paramInt = 0) {
        return String.format("%d", new Object[] { Integer.valueOf(paramInt + m) });
      }
    }
    return String.format("%d", new Object[] { Integer.valueOf(paramInt) });
  }
  
  public static String formatLocationUpdateDate(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      int j = ((Calendar)localObject).get(1);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int k = ((Calendar)localObject).get(6);
      int m = ((Calendar)localObject).get(1);
      if ((k == i) && (j == m))
      {
        i = (int)(ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime() - paramLong / 1000L) / 60;
        if (i < 1) {
          return getString("LocationUpdatedJustNow", 2131493775);
        }
        if (i < 60) {
          return formatPluralString("UpdatedMinutes", i);
        }
        return formatString("LocationUpdatedFormatted", 2131493774, new Object[] { formatString("TodayAtFormatted", 2131494497, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      if ((k + 1 == i) && (j == m)) {
        return formatString("LocationUpdatedFormatted", 2131493774, new Object[] { formatString("YesterdayAtFormatted", 2131494654, new Object[] { getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      if (Math.abs(System.currentTimeMillis() - paramLong) < 31536000000L) {
        return formatString("LocationUpdatedFormatted", 2131493774, new Object[] { formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterMonth.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) }) });
      }
      localObject = formatString("LocationUpdatedFormatted", 2131493774, new Object[] { formatString("formatDateAtTime", 2131494696, new Object[] { getInstance().formatterYear.format(new Date(paramLong)), getInstance().formatterDay.format(new Date(paramLong)) }) });
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  public static String formatPluralString(String paramString, int paramInt)
  {
    if ((paramString == null) || (paramString.length() == 0) || (getInstance().currentPluralRules == null)) {
      return "LOC_ERR:" + paramString;
    }
    String str = getInstance().stringForQuantity(getInstance().currentPluralRules.quantityForNumber(paramInt));
    paramString = paramString + "_" + str;
    return formatString(paramString, ApplicationLoader.applicationContext.getResources().getIdentifier(paramString, "string", ApplicationLoader.applicationContext.getPackageName()), new Object[] { Integer.valueOf(paramInt) });
  }
  
  public static String formatShortNumber(int paramInt, int[] paramArrayOfInt)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    int i = 0;
    while (paramInt / 1000 > 0)
    {
      localStringBuilder.append("K");
      i = paramInt % 1000 / 100;
      paramInt /= 1000;
    }
    if (paramArrayOfInt != null)
    {
      double d = paramInt + i / 10.0D;
      int j = 0;
      while (j < localStringBuilder.length())
      {
        d *= 1000.0D;
        j += 1;
      }
      paramArrayOfInt[0] = ((int)d);
    }
    if ((i != 0) && (localStringBuilder.length() > 0))
    {
      if (localStringBuilder.length() == 2) {
        return String.format(Locale.US, "%d.%dM", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i) });
      }
      return String.format(Locale.US, "%d.%d%s", new Object[] { Integer.valueOf(paramInt), Integer.valueOf(i), localStringBuilder.toString() });
    }
    if (localStringBuilder.length() == 2) {
      return String.format(Locale.US, "%dM", new Object[] { Integer.valueOf(paramInt) });
    }
    return String.format(Locale.US, "%d%s", new Object[] { Integer.valueOf(paramInt), localStringBuilder.toString() });
  }
  
  public static String formatString(String paramString, int paramInt, Object... paramVarArgs)
  {
    try
    {
      String str2 = (String)getInstance().localeValues.get(paramString);
      String str1 = str2;
      if (str2 == null) {
        str1 = ApplicationLoader.applicationContext.getString(paramInt);
      }
      if (getInstance().currentLocale != null) {
        return String.format(getInstance().currentLocale, str1, paramVarArgs);
      }
      paramVarArgs = String.format(str1, paramVarArgs);
      return paramVarArgs;
    }
    catch (Exception paramVarArgs)
    {
      FileLog.e(paramVarArgs);
    }
    return "LOC_ERR: " + paramString;
  }
  
  public static String formatStringSimple(String paramString, Object... paramVarArgs)
  {
    try
    {
      if (getInstance().currentLocale != null) {
        return String.format(getInstance().currentLocale, paramString, paramVarArgs);
      }
      paramVarArgs = String.format(paramString, paramVarArgs);
      return paramVarArgs;
    }
    catch (Exception paramVarArgs)
    {
      FileLog.e(paramVarArgs);
    }
    return "LOC_ERR: " + paramString;
  }
  
  public static String formatTTLString(int paramInt)
  {
    if (paramInt < 60) {
      return formatPluralString("Seconds", paramInt);
    }
    if (paramInt < 3600) {
      return formatPluralString("Minutes", paramInt / 60);
    }
    if (paramInt < 86400) {
      return formatPluralString("Hours", paramInt / 60 / 60);
    }
    if (paramInt < 604800) {
      return formatPluralString("Days", paramInt / 60 / 60 / 24);
    }
    int i = paramInt / 60 / 60 / 24;
    if (paramInt % 7 == 0) {
      return formatPluralString("Weeks", i / 7);
    }
    return String.format("%s %s", new Object[] { formatPluralString("Weeks", i / 7), formatPluralString("Days", i % 7) });
  }
  
  public static String formatUserStatus(int paramInt, TLRPC.User paramUser)
  {
    if ((paramUser != null) && (paramUser.status != null) && (paramUser.status.expires == 0))
    {
      if (!(paramUser.status instanceof TLRPC.TL_userStatusRecently)) {
        break label91;
      }
      paramUser.status.expires = -100;
    }
    while ((paramUser != null) && (paramUser.status != null) && (paramUser.status.expires <= 0) && (MessagesController.getInstance(paramInt).onlinePrivacy.containsKey(Integer.valueOf(paramUser.id))))
    {
      return getString("Online", 2131494030);
      label91:
      if ((paramUser.status instanceof TLRPC.TL_userStatusLastWeek)) {
        paramUser.status.expires = -101;
      } else if ((paramUser.status instanceof TLRPC.TL_userStatusLastMonth)) {
        paramUser.status.expires = -102;
      }
    }
    if ((paramUser == null) || (paramUser.status == null) || (paramUser.status.expires == 0) || (UserObject.isDeleted(paramUser)) || ((paramUser instanceof TLRPC.TL_userEmpty))) {
      return getString("ALongTimeAgo", 2131492864);
    }
    paramInt = ConnectionsManager.getInstance(paramInt).getCurrentTime();
    if (paramUser.status.expires > paramInt) {
      return getString("Online", 2131494030);
    }
    if (paramUser.status.expires == -1) {
      return getString("Invisible", 2131493683);
    }
    if (paramUser.status.expires == -100) {
      return getString("Lately", 2131493740);
    }
    if (paramUser.status.expires == -101) {
      return getString("WithinAWeek", 2131494643);
    }
    if (paramUser.status.expires == -102) {
      return getString("WithinAMonth", 2131494642);
    }
    return formatDateOnline(paramUser.status.expires);
  }
  
  public static String getCurrentLanguageName()
  {
    return getString("LanguageName", 2131493724);
  }
  
  public static LocaleController getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          LocaleController localLocaleController2 = Instance;
          localObject1 = localLocaleController2;
          if (localLocaleController2 == null) {
            localObject1 = new LocaleController();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (LocaleController)localObject1;
          return (LocaleController)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localLocaleController1;
  }
  
  private LocaleInfo getLanguageFromDict(String paramString)
  {
    if (paramString == null) {
      return null;
    }
    return (LocaleInfo)this.languagesDict.get(paramString.toLowerCase().replace("-", "_"));
  }
  
  public static String getLocaleAlias(String paramString)
  {
    if (paramString == null) {
      return null;
    }
    int i = -1;
    switch (paramString.hashCode())
    {
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return null;
      case 0: 
        return "id";
        if (paramString.equals("in"))
        {
          i = 0;
          continue;
          if (paramString.equals("iw"))
          {
            i = 1;
            continue;
            if (paramString.equals("jw"))
            {
              i = 2;
              continue;
              if (paramString.equals("no"))
              {
                i = 3;
                continue;
                if (paramString.equals("tl"))
                {
                  i = 4;
                  continue;
                  if (paramString.equals("ji"))
                  {
                    i = 5;
                    continue;
                    if (paramString.equals("id"))
                    {
                      i = 6;
                      continue;
                      if (paramString.equals("he"))
                      {
                        i = 7;
                        continue;
                        if (paramString.equals("jv"))
                        {
                          i = 8;
                          continue;
                          if (paramString.equals("nb"))
                          {
                            i = 9;
                            continue;
                            if (paramString.equals("fil"))
                            {
                              i = 10;
                              continue;
                              if (paramString.equals("yi")) {
                                i = 11;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        break;
      }
    }
    return "he";
    return "jv";
    return "nb";
    return "fil";
    return "yi";
    return "in";
    return "iw";
    return "jw";
    return "no";
    return "tl";
    return "ji";
  }
  
  private HashMap<String, String> getLocaleFileStrings(File paramFile)
  {
    return getLocaleFileStrings(paramFile, false);
  }
  
  /* Error */
  private HashMap<String, String> getLocaleFileStrings(File paramFile, boolean paramBoolean)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 5
    //   3: aconst_null
    //   4: astore 6
    //   6: aload_0
    //   7: iconst_0
    //   8: putfield 1048	org/telegram/messenger/LocaleController:reloadLastFile	Z
    //   11: aload 5
    //   13: astore 4
    //   15: aload_1
    //   16: invokevirtual 1053	java/io/File:exists	()Z
    //   19: ifne +38 -> 57
    //   22: aload 5
    //   24: astore 4
    //   26: new 167	java/util/HashMap
    //   29: dup
    //   30: invokespecial 168	java/util/HashMap:<init>	()V
    //   33: astore_1
    //   34: iconst_0
    //   35: ifeq +11 -> 46
    //   38: new 1055	java/lang/NullPointerException
    //   41: dup
    //   42: invokespecial 1056	java/lang/NullPointerException:<init>	()V
    //   45: athrow
    //   46: aload_1
    //   47: areturn
    //   48: astore 4
    //   50: aload 4
    //   52: invokestatic 634	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   55: aload_1
    //   56: areturn
    //   57: aload 5
    //   59: astore 4
    //   61: new 167	java/util/HashMap
    //   64: dup
    //   65: invokespecial 168	java/util/HashMap:<init>	()V
    //   68: astore 10
    //   70: aload 5
    //   72: astore 4
    //   74: invokestatic 1062	android/util/Xml:newPullParser	()Lorg/xmlpull/v1/XmlPullParser;
    //   77: astore 11
    //   79: aload 5
    //   81: astore 4
    //   83: new 1064	java/io/FileInputStream
    //   86: dup
    //   87: aload_1
    //   88: invokespecial 1067	java/io/FileInputStream:<init>	(Ljava/io/File;)V
    //   91: astore 9
    //   93: aload 11
    //   95: aload 9
    //   97: ldc_w 1069
    //   100: invokeinterface 1075 3 0
    //   105: aload 11
    //   107: invokeinterface 1078 1 0
    //   112: istore_3
    //   113: aconst_null
    //   114: astore 7
    //   116: aconst_null
    //   117: astore 8
    //   119: aconst_null
    //   120: astore 6
    //   122: iload_3
    //   123: iconst_1
    //   124: if_icmpeq +451 -> 575
    //   127: iload_3
    //   128: iconst_2
    //   129: if_icmpne +185 -> 314
    //   132: aload 11
    //   134: invokeinterface 1081 1 0
    //   139: astore 7
    //   141: aload 6
    //   143: astore 5
    //   145: aload 7
    //   147: astore 4
    //   149: aload 8
    //   151: astore_1
    //   152: aload 11
    //   154: invokeinterface 1084 1 0
    //   159: ifle +20 -> 179
    //   162: aload 11
    //   164: iconst_0
    //   165: invokeinterface 1087 2 0
    //   170: astore 5
    //   172: aload 8
    //   174: astore_1
    //   175: aload 7
    //   177: astore 4
    //   179: aload 5
    //   181: astore 6
    //   183: aload 4
    //   185: astore 7
    //   187: aload_1
    //   188: astore 8
    //   190: aload 4
    //   192: ifnull +111 -> 303
    //   195: aload 5
    //   197: astore 6
    //   199: aload 4
    //   201: astore 7
    //   203: aload_1
    //   204: astore 8
    //   206: aload 4
    //   208: ldc_w 910
    //   211: invokevirtual 1035	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   214: ifeq +89 -> 303
    //   217: aload 5
    //   219: astore 6
    //   221: aload 4
    //   223: astore 7
    //   225: aload_1
    //   226: astore 8
    //   228: aload_1
    //   229: ifnull +74 -> 303
    //   232: aload 5
    //   234: astore 6
    //   236: aload 4
    //   238: astore 7
    //   240: aload_1
    //   241: astore 8
    //   243: aload 5
    //   245: ifnull +58 -> 303
    //   248: aload 5
    //   250: astore 6
    //   252: aload 4
    //   254: astore 7
    //   256: aload_1
    //   257: astore 8
    //   259: aload_1
    //   260: invokevirtual 730	java/lang/String:length	()I
    //   263: ifeq +40 -> 303
    //   266: aload 5
    //   268: astore 6
    //   270: aload 4
    //   272: astore 7
    //   274: aload_1
    //   275: astore 8
    //   277: aload 5
    //   279: invokevirtual 730	java/lang/String:length	()I
    //   282: ifeq +21 -> 303
    //   285: aload 10
    //   287: aload 5
    //   289: aload_1
    //   290: invokevirtual 494	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   293: pop
    //   294: aconst_null
    //   295: astore 7
    //   297: aconst_null
    //   298: astore 8
    //   300: aconst_null
    //   301: astore 6
    //   303: aload 11
    //   305: invokeinterface 1090 1 0
    //   310: istore_3
    //   311: goto -189 -> 122
    //   314: iload_3
    //   315: iconst_4
    //   316: if_icmpne +232 -> 548
    //   319: aload 6
    //   321: astore 5
    //   323: aload 7
    //   325: astore 4
    //   327: aload 8
    //   329: astore_1
    //   330: aload 6
    //   332: ifnull -153 -> 179
    //   335: aload 11
    //   337: invokeinterface 1093 1 0
    //   342: astore 8
    //   344: aload 6
    //   346: astore 5
    //   348: aload 7
    //   350: astore 4
    //   352: aload 8
    //   354: astore_1
    //   355: aload 8
    //   357: ifnull -178 -> 179
    //   360: aload 8
    //   362: invokevirtual 1096	java/lang/String:trim	()Ljava/lang/String;
    //   365: astore_1
    //   366: iload_2
    //   367: ifeq +52 -> 419
    //   370: aload_1
    //   371: ldc_w 744
    //   374: ldc_w 746
    //   377: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   380: ldc_w 748
    //   383: ldc_w 750
    //   386: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   389: ldc_w 1098
    //   392: ldc_w 1100
    //   395: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   398: ldc_w 752
    //   401: ldc_w 754
    //   404: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   407: astore_1
    //   408: aload 6
    //   410: astore 5
    //   412: aload 7
    //   414: astore 4
    //   416: goto -237 -> 179
    //   419: aload_1
    //   420: ldc_w 1102
    //   423: ldc_w 1104
    //   426: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   429: ldc_w 1106
    //   432: ldc_w 1108
    //   435: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   438: astore 12
    //   440: aload 12
    //   442: ldc_w 746
    //   445: ldc_w 744
    //   448: invokevirtual 721	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   451: astore 8
    //   453: aload 6
    //   455: astore 5
    //   457: aload 7
    //   459: astore 4
    //   461: aload 8
    //   463: astore_1
    //   464: aload_0
    //   465: getfield 1048	org/telegram/messenger/LocaleController:reloadLastFile	Z
    //   468: ifne -289 -> 179
    //   471: aload 6
    //   473: astore 5
    //   475: aload 7
    //   477: astore 4
    //   479: aload 8
    //   481: astore_1
    //   482: aload 8
    //   484: aload 12
    //   486: invokevirtual 1035	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   489: ifne -310 -> 179
    //   492: aload_0
    //   493: iconst_1
    //   494: putfield 1048	org/telegram/messenger/LocaleController:reloadLastFile	Z
    //   497: aload 6
    //   499: astore 5
    //   501: aload 7
    //   503: astore 4
    //   505: aload 8
    //   507: astore_1
    //   508: goto -329 -> 179
    //   511: astore 5
    //   513: aload 9
    //   515: astore_1
    //   516: aload_1
    //   517: astore 4
    //   519: aload 5
    //   521: invokestatic 634	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   524: aload_1
    //   525: astore 4
    //   527: aload_0
    //   528: iconst_1
    //   529: putfield 1048	org/telegram/messenger/LocaleController:reloadLastFile	Z
    //   532: aload_1
    //   533: ifnull +7 -> 540
    //   536: aload_1
    //   537: invokevirtual 1111	java/io/FileInputStream:close	()V
    //   540: new 167	java/util/HashMap
    //   543: dup
    //   544: invokespecial 168	java/util/HashMap:<init>	()V
    //   547: areturn
    //   548: aload 6
    //   550: astore 5
    //   552: aload 7
    //   554: astore 4
    //   556: aload 8
    //   558: astore_1
    //   559: iload_3
    //   560: iconst_3
    //   561: if_icmpne -382 -> 179
    //   564: aconst_null
    //   565: astore_1
    //   566: aconst_null
    //   567: astore 5
    //   569: aconst_null
    //   570: astore 4
    //   572: goto -393 -> 179
    //   575: aload 9
    //   577: ifnull +8 -> 585
    //   580: aload 9
    //   582: invokevirtual 1111	java/io/FileInputStream:close	()V
    //   585: aload 10
    //   587: areturn
    //   588: astore_1
    //   589: aload_1
    //   590: invokestatic 634	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   593: goto -8 -> 585
    //   596: astore_1
    //   597: aload_1
    //   598: invokestatic 634	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   601: goto -61 -> 540
    //   604: astore_1
    //   605: aload 4
    //   607: ifnull +8 -> 615
    //   610: aload 4
    //   612: invokevirtual 1111	java/io/FileInputStream:close	()V
    //   615: aload_1
    //   616: athrow
    //   617: astore 4
    //   619: aload 4
    //   621: invokestatic 634	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   624: goto -9 -> 615
    //   627: astore_1
    //   628: aload 9
    //   630: astore 4
    //   632: goto -27 -> 605
    //   635: astore 5
    //   637: aload 6
    //   639: astore_1
    //   640: goto -124 -> 516
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	643	0	this	LocaleController
    //   0	643	1	paramFile	File
    //   0	643	2	paramBoolean	boolean
    //   112	450	3	i	int
    //   13	12	4	localObject1	Object
    //   48	3	4	localException1	Exception
    //   59	552	4	localObject2	Object
    //   617	3	4	localException2	Exception
    //   630	1	4	localObject3	Object
    //   1	499	5	localObject4	Object
    //   511	9	5	localException3	Exception
    //   550	18	5	localObject5	Object
    //   635	1	5	localException4	Exception
    //   4	634	6	localObject6	Object
    //   114	439	7	localObject7	Object
    //   117	440	8	localObject8	Object
    //   91	538	9	localFileInputStream	java.io.FileInputStream
    //   68	518	10	localHashMap	HashMap
    //   77	259	11	localXmlPullParser	org.xmlpull.v1.XmlPullParser
    //   438	47	12	str	String
    // Exception table:
    //   from	to	target	type
    //   38	46	48	java/lang/Exception
    //   93	113	511	java/lang/Exception
    //   132	141	511	java/lang/Exception
    //   152	172	511	java/lang/Exception
    //   206	217	511	java/lang/Exception
    //   259	266	511	java/lang/Exception
    //   277	294	511	java/lang/Exception
    //   303	311	511	java/lang/Exception
    //   335	344	511	java/lang/Exception
    //   360	366	511	java/lang/Exception
    //   370	408	511	java/lang/Exception
    //   419	453	511	java/lang/Exception
    //   464	471	511	java/lang/Exception
    //   482	497	511	java/lang/Exception
    //   580	585	588	java/lang/Exception
    //   536	540	596	java/lang/Exception
    //   15	22	604	finally
    //   26	34	604	finally
    //   61	70	604	finally
    //   74	79	604	finally
    //   83	93	604	finally
    //   519	524	604	finally
    //   527	532	604	finally
    //   610	615	617	java/lang/Exception
    //   93	113	627	finally
    //   132	141	627	finally
    //   152	172	627	finally
    //   206	217	627	finally
    //   259	266	627	finally
    //   277	294	627	finally
    //   303	311	627	finally
    //   335	344	627	finally
    //   360	366	627	finally
    //   370	408	627	finally
    //   419	453	627	finally
    //   464	471	627	finally
    //   482	497	627	finally
    //   15	22	635	java/lang/Exception
    //   26	34	635	java/lang/Exception
    //   61	70	635	java/lang/Exception
    //   74	79	635	java/lang/Exception
    //   83	93	635	java/lang/Exception
  }
  
  private String getLocaleString(Locale paramLocale)
  {
    if (paramLocale == null) {
      return "en";
    }
    String str1 = paramLocale.getLanguage();
    String str2 = paramLocale.getCountry();
    paramLocale = paramLocale.getVariant();
    if ((str1.length() == 0) && (str2.length() == 0)) {
      return "en";
    }
    StringBuilder localStringBuilder = new StringBuilder(11);
    localStringBuilder.append(str1);
    if ((str2.length() > 0) || (paramLocale.length() > 0)) {
      localStringBuilder.append('_');
    }
    localStringBuilder.append(str2);
    if (paramLocale.length() > 0) {
      localStringBuilder.append('_');
    }
    localStringBuilder.append(paramLocale);
    return localStringBuilder.toString();
  }
  
  public static String getLocaleStringIso639()
  {
    Object localObject = getInstance().currentLocale;
    if (localObject == null) {
      return "en";
    }
    String str1 = ((Locale)localObject).getLanguage();
    String str2 = ((Locale)localObject).getCountry();
    localObject = ((Locale)localObject).getVariant();
    if ((str1.length() == 0) && (str2.length() == 0)) {
      return "en";
    }
    StringBuilder localStringBuilder = new StringBuilder(11);
    localStringBuilder.append(str1);
    if ((str2.length() > 0) || (((String)localObject).length() > 0)) {
      localStringBuilder.append('-');
    }
    localStringBuilder.append(str2);
    if (((String)localObject).length() > 0) {
      localStringBuilder.append('_');
    }
    localStringBuilder.append((String)localObject);
    return localStringBuilder.toString();
  }
  
  public static String getPluralString(String paramString, int paramInt)
  {
    if ((paramString == null) || (paramString.length() == 0) || (getInstance().currentPluralRules == null)) {
      return "LOC_ERR:" + paramString;
    }
    String str = getInstance().stringForQuantity(getInstance().currentPluralRules.quantityForNumber(paramInt));
    paramString = paramString + "_" + str;
    return getString(paramString, ApplicationLoader.applicationContext.getResources().getIdentifier(paramString, "string", ApplicationLoader.applicationContext.getPackageName()));
  }
  
  public static String getString(String paramString, int paramInt)
  {
    return getInstance().getStringInternal(paramString, paramInt);
  }
  
  private String getStringInternal(String paramString, int paramInt)
  {
    Object localObject3 = (String)this.localeValues.get(paramString);
    Object localObject1 = localObject3;
    if (localObject3 == null) {}
    try
    {
      localObject1 = ApplicationLoader.applicationContext.getString(paramInt);
      localObject3 = localObject1;
      if (localObject1 == null) {
        localObject3 = "LOC_ERR:" + paramString;
      }
      return (String)localObject3;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
        Object localObject2 = localObject3;
      }
    }
  }
  
  public static String getSystemLocaleStringIso639()
  {
    Object localObject = getInstance().getSystemDefaultLocale();
    if (localObject == null) {
      return "en";
    }
    String str1 = ((Locale)localObject).getLanguage();
    String str2 = ((Locale)localObject).getCountry();
    localObject = ((Locale)localObject).getVariant();
    if ((str1.length() == 0) && (str2.length() == 0)) {
      return "en";
    }
    StringBuilder localStringBuilder = new StringBuilder(11);
    localStringBuilder.append(str1);
    if ((str2.length() > 0) || (((String)localObject).length() > 0)) {
      localStringBuilder.append('-');
    }
    localStringBuilder.append(str2);
    if (((String)localObject).length() > 0) {
      localStringBuilder.append('_');
    }
    localStringBuilder.append((String)localObject);
    return localStringBuilder.toString();
  }
  
  public static boolean isRTLCharacter(char paramChar)
  {
    return (Character.getDirectionality(paramChar) == 1) || (Character.getDirectionality(paramChar) == 2) || (Character.getDirectionality(paramChar) == 16) || (Character.getDirectionality(paramChar) == 17);
  }
  
  private void loadOtherLanguages()
  {
    int j = 0;
    Object localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("langconfig", 0);
    Object localObject2 = ((SharedPreferences)localObject1).getString("locales", null);
    int k;
    int i;
    if (!TextUtils.isEmpty((CharSequence)localObject2))
    {
      localObject2 = ((String)localObject2).split("&");
      k = localObject2.length;
      i = 0;
      while (i < k)
      {
        LocaleInfo localLocaleInfo = LocaleInfo.createWithString(localObject2[i]);
        if (localLocaleInfo != null) {
          this.otherLanguages.add(localLocaleInfo);
        }
        i += 1;
      }
    }
    localObject1 = ((SharedPreferences)localObject1).getString("remote", null);
    if (!TextUtils.isEmpty((CharSequence)localObject1))
    {
      localObject1 = ((String)localObject1).split("&");
      k = localObject1.length;
      i = j;
      while (i < k)
      {
        localObject2 = LocaleInfo.createWithString(localObject1[i]);
        ((LocaleInfo)localObject2).shortName = ((LocaleInfo)localObject2).shortName.replace("-", "_");
        if (localObject2 != null) {
          this.remoteLanguages.add(localObject2);
        }
        i += 1;
      }
    }
  }
  
  private void saveOtherLanguages()
  {
    SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("langconfig", 0).edit();
    StringBuilder localStringBuilder = new StringBuilder();
    int i = 0;
    String str;
    while (i < this.otherLanguages.size())
    {
      str = ((LocaleInfo)this.otherLanguages.get(i)).getSaveString();
      if (str != null)
      {
        if (localStringBuilder.length() != 0) {
          localStringBuilder.append("&");
        }
        localStringBuilder.append(str);
      }
      i += 1;
    }
    localEditor.putString("locales", localStringBuilder.toString());
    localStringBuilder.setLength(0);
    i = 0;
    while (i < this.remoteLanguages.size())
    {
      str = ((LocaleInfo)this.remoteLanguages.get(i)).getSaveString();
      if (str != null)
      {
        if (localStringBuilder.length() != 0) {
          localStringBuilder.append("&");
        }
        localStringBuilder.append(str);
      }
      i += 1;
    }
    localEditor.putString("remote", localStringBuilder.toString());
    localEditor.commit();
  }
  
  public static String stringForMessageListDate(long paramLong)
  {
    paramLong *= 1000L;
    try
    {
      Object localObject = Calendar.getInstance();
      int i = ((Calendar)localObject).get(6);
      ((Calendar)localObject).setTimeInMillis(paramLong);
      int j = ((Calendar)localObject).get(6);
      if (Math.abs(System.currentTimeMillis() - paramLong) >= 31536000000L) {
        return getInstance().formatterYear.format(new Date(paramLong));
      }
      i = j - i;
      if ((i == 0) || ((i == -1) && (System.currentTimeMillis() - paramLong < 28800000L))) {
        return getInstance().formatterDay.format(new Date(paramLong));
      }
      if ((i > -7) && (i <= -1)) {
        return getInstance().formatterWeek.format(new Date(paramLong));
      }
      localObject = getInstance().formatterMonth.format(new Date(paramLong));
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return "LOC_ERR";
  }
  
  private String stringForQuantity(int paramInt)
  {
    switch (paramInt)
    {
    default: 
      return "other";
    case 1: 
      return "zero";
    case 2: 
      return "one";
    case 4: 
      return "two";
    case 8: 
      return "few";
    }
    return "many";
  }
  
  public void applyLanguage(LocaleInfo paramLocaleInfo, boolean paramBoolean1, boolean paramBoolean2, int paramInt)
  {
    applyLanguage(paramLocaleInfo, paramBoolean1, paramBoolean2, false, false, paramInt);
  }
  
  public void applyLanguage(final LocaleInfo paramLocaleInfo, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, boolean paramBoolean4, final int paramInt)
  {
    if (paramLocaleInfo == null) {
      return;
    }
    File localFile = paramLocaleInfo.getPathToFile();
    Object localObject = paramLocaleInfo.shortName;
    if (!paramBoolean2) {
      ConnectionsManager.setLangCode(((String)localObject).replace("_", "-"));
    }
    if ((paramLocaleInfo.isRemote()) && ((paramBoolean4) || (!localFile.exists())))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("reload locale because file doesn't exist " + localFile);
      }
      if (!paramBoolean2) {
        break label353;
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          LocaleController.this.applyRemoteLanguage(paramLocaleInfo, true, paramInt);
        }
      });
    }
    for (;;)
    {
      try
      {
        arrayOfString = paramLocaleInfo.shortName.split("_");
        if (arrayOfString.length != 1) {
          continue;
        }
        localObject = new Locale(paramLocaleInfo.shortName);
        if (paramBoolean1)
        {
          this.languageOverride = paramLocaleInfo.shortName;
          SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
          localEditor.putString("language", paramLocaleInfo.getKey());
          localEditor.commit();
        }
        if (localFile != null) {
          continue;
        }
        this.localeValues.clear();
        this.currentLocale = ((Locale)localObject);
        this.currentLocaleInfo = paramLocaleInfo;
        this.currentPluralRules = ((PluralRules)this.allRules.get(arrayOfString[0]));
        if (this.currentPluralRules == null) {
          this.currentPluralRules = ((PluralRules)this.allRules.get(this.currentLocale.getLanguage()));
        }
        if (this.currentPluralRules == null) {
          this.currentPluralRules = new PluralRules_None();
        }
        this.changingConfiguration = true;
        Locale.setDefault(this.currentLocale);
        paramLocaleInfo = new Configuration();
        paramLocaleInfo.locale = this.currentLocale;
        ApplicationLoader.applicationContext.getResources().updateConfiguration(paramLocaleInfo, ApplicationLoader.applicationContext.getResources().getDisplayMetrics());
        this.changingConfiguration = false;
        if (!this.reloadLastFile) {
          continue;
        }
        if (!paramBoolean2) {
          continue;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            LocaleController.this.reloadCurrentRemoteLocale(paramInt);
          }
        });
      }
      catch (Exception paramLocaleInfo)
      {
        String[] arrayOfString;
        label353:
        FileLog.e(paramLocaleInfo);
        this.changingConfiguration = false;
        continue;
        reloadCurrentRemoteLocale(paramInt);
        continue;
      }
      this.reloadLastFile = false;
      recreateFormatters();
      return;
      applyRemoteLanguage(paramLocaleInfo, true, paramInt);
      continue;
      localObject = new Locale(arrayOfString[0], arrayOfString[1]);
      continue;
      if (!paramBoolean3) {
        this.localeValues = getLocaleFileStrings(localFile);
      }
    }
  }
  
  public boolean applyLanguageFile(File paramFile, int paramInt)
  {
    try
    {
      HashMap localHashMap = getLocaleFileStrings(paramFile);
      String str1 = (String)localHashMap.get("LanguageName");
      String str2 = (String)localHashMap.get("LanguageNameInEnglish");
      String str3 = (String)localHashMap.get("LanguageCode");
      if ((str1 != null) && (str1.length() > 0) && (str2 != null) && (str2.length() > 0) && (str3 != null) && (str3.length() > 0))
      {
        if ((str1.contains("&")) || (str1.contains("|"))) {
          break label359;
        }
        if ((str2.contains("&")) || (str2.contains("|"))) {
          break label361;
        }
        if ((str3.contains("&")) || (str3.contains("|")) || (str3.contains("/")) || (str3.contains("\\"))) {
          break label363;
        }
        File localFile = new File(ApplicationLoader.getFilesDirFixed(), str3 + ".xml");
        if (!AndroidUtilities.copyFile(paramFile, localFile)) {
          return false;
        }
        LocaleInfo localLocaleInfo = getLanguageFromDict("local_" + str3.toLowerCase());
        paramFile = localLocaleInfo;
        if (localLocaleInfo == null)
        {
          paramFile = new LocaleInfo();
          paramFile.name = str1;
          paramFile.nameEnglish = str2;
          paramFile.shortName = str3.toLowerCase();
          paramFile.pathToFile = localFile.getAbsolutePath();
          this.languages.add(paramFile);
          this.languagesDict.put(paramFile.getKey(), paramFile);
          this.otherLanguages.add(paramFile);
          saveOtherLanguages();
        }
        this.localeValues = localHashMap;
        applyLanguage(paramFile, true, false, true, false, paramInt);
        return true;
      }
    }
    catch (Exception paramFile)
    {
      FileLog.e(paramFile);
    }
    return false;
    label359:
    return false;
    label361:
    return false;
    label363:
    return false;
  }
  
  public void checkUpdateForCurrentRemoteLocale(int paramInt1, int paramInt2)
  {
    if ((this.currentLocaleInfo == null) || ((this.currentLocaleInfo != null) && (!this.currentLocaleInfo.isRemote()))) {}
    while (this.currentLocaleInfo.version >= paramInt2) {
      return;
    }
    applyRemoteLanguage(this.currentLocaleInfo, false, paramInt1);
  }
  
  public boolean deleteLanguage(LocaleInfo paramLocaleInfo, int paramInt)
  {
    if ((paramLocaleInfo.pathToFile == null) || (paramLocaleInfo.isRemote())) {
      return false;
    }
    if (this.currentLocaleInfo == paramLocaleInfo)
    {
      Object localObject2 = null;
      if (this.systemDefaultLocale.getLanguage() != null) {
        localObject2 = getLanguageFromDict(this.systemDefaultLocale.getLanguage());
      }
      Object localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = getLanguageFromDict(getLocaleString(this.systemDefaultLocale));
      }
      localObject2 = localObject1;
      if (localObject1 == null) {
        localObject2 = getLanguageFromDict("en");
      }
      applyLanguage((LocaleInfo)localObject2, true, false, paramInt);
    }
    this.otherLanguages.remove(paramLocaleInfo);
    this.languages.remove(paramLocaleInfo);
    this.languagesDict.remove(paramLocaleInfo.shortName);
    new File(paramLocaleInfo.pathToFile).delete();
    saveOtherLanguages();
    return true;
  }
  
  public String formatCurrencyDecimalString(long paramLong, String paramString, boolean paramBoolean)
  {
    String str = paramString.toUpperCase();
    paramLong = Math.abs(paramLong);
    int i = -1;
    double d;
    label416:
    Locale localLocale;
    switch (str.hashCode())
    {
    default: 
      switch (i)
      {
      default: 
        paramString = " %.2f";
        d = paramLong / 100.0D;
        localLocale = Locale.US;
        if (!paramBoolean) {}
        break;
      }
      break;
    }
    for (paramString = str;; paramString = "" + paramString)
    {
      return String.format(localLocale, paramString, new Object[] { Double.valueOf(d) }).trim();
      if (!str.equals("CLF")) {
        break;
      }
      i = 0;
      break;
      if (!str.equals("IRR")) {
        break;
      }
      i = 1;
      break;
      if (!str.equals("BHD")) {
        break;
      }
      i = 2;
      break;
      if (!str.equals("IQD")) {
        break;
      }
      i = 3;
      break;
      if (!str.equals("JOD")) {
        break;
      }
      i = 4;
      break;
      if (!str.equals("KWD")) {
        break;
      }
      i = 5;
      break;
      if (!str.equals("LYD")) {
        break;
      }
      i = 6;
      break;
      if (!str.equals("OMR")) {
        break;
      }
      i = 7;
      break;
      if (!str.equals("TND")) {
        break;
      }
      i = 8;
      break;
      if (!str.equals("BIF")) {
        break;
      }
      i = 9;
      break;
      if (!str.equals("BYR")) {
        break;
      }
      i = 10;
      break;
      if (!str.equals("CLP")) {
        break;
      }
      i = 11;
      break;
      if (!str.equals("CVE")) {
        break;
      }
      i = 12;
      break;
      if (!str.equals("DJF")) {
        break;
      }
      i = 13;
      break;
      if (!str.equals("GNF")) {
        break;
      }
      i = 14;
      break;
      if (!str.equals("ISK")) {
        break;
      }
      i = 15;
      break;
      if (!str.equals("JPY")) {
        break;
      }
      i = 16;
      break;
      if (!str.equals("KMF")) {
        break;
      }
      i = 17;
      break;
      if (!str.equals("KRW")) {
        break;
      }
      i = 18;
      break;
      if (!str.equals("MGA")) {
        break;
      }
      i = 19;
      break;
      if (!str.equals("PYG")) {
        break;
      }
      i = 20;
      break;
      if (!str.equals("RWF")) {
        break;
      }
      i = 21;
      break;
      if (!str.equals("UGX")) {
        break;
      }
      i = 22;
      break;
      if (!str.equals("UYI")) {
        break;
      }
      i = 23;
      break;
      if (!str.equals("VND")) {
        break;
      }
      i = 24;
      break;
      if (!str.equals("VUV")) {
        break;
      }
      i = 25;
      break;
      if (!str.equals("XAF")) {
        break;
      }
      i = 26;
      break;
      if (!str.equals("XOF")) {
        break;
      }
      i = 27;
      break;
      if (!str.equals("XPF")) {
        break;
      }
      i = 28;
      break;
      if (!str.equals("MRO")) {
        break;
      }
      i = 29;
      break;
      paramString = " %.4f";
      d = paramLong / 10000.0D;
      break label416;
      d = (float)paramLong / 100.0F;
      if (paramLong % 100L == 0L)
      {
        paramString = " %.0f";
        break label416;
      }
      paramString = " %.2f";
      break label416;
      paramString = " %.3f";
      d = paramLong / 1000.0D;
      break label416;
      paramString = " %.0f";
      d = paramLong;
      break label416;
      paramString = " %.1f";
      d = paramLong / 10.0D;
      break label416;
    }
  }
  
  public String formatCurrencyString(long paramLong, String paramString)
  {
    String str = paramString.toUpperCase();
    int j;
    int i;
    label284:
    double d;
    if (paramLong < 0L)
    {
      j = 1;
      paramLong = Math.abs(paramLong);
      localObject1 = Currency.getInstance(str);
      i = -1;
      switch (str.hashCode())
      {
      default: 
        switch (i)
        {
        default: 
          paramString = " %.2f";
          d = paramLong / 100.0D;
          label432:
          if (localObject1 == null) {
            break label1161;
          }
          if (this.currentLocale != null)
          {
            paramString = this.currentLocale;
            label449:
            localObject2 = NumberFormat.getCurrencyInstance(paramString);
            ((NumberFormat)localObject2).setCurrency((Currency)localObject1);
            if (str.equals("IRR")) {
              ((NumberFormat)localObject2).setMaximumFractionDigits(0);
            }
            localObject1 = new StringBuilder();
            if (j == 0) {
              break label1154;
            }
          }
          break;
        }
        break;
      }
    }
    label1154:
    for (paramString = "-";; paramString = "")
    {
      return paramString + ((NumberFormat)localObject2).format(d);
      j = 0;
      break;
      if (!str.equals("CLF")) {
        break label284;
      }
      i = 0;
      break label284;
      if (!str.equals("IRR")) {
        break label284;
      }
      i = 1;
      break label284;
      if (!str.equals("BHD")) {
        break label284;
      }
      i = 2;
      break label284;
      if (!str.equals("IQD")) {
        break label284;
      }
      i = 3;
      break label284;
      if (!str.equals("JOD")) {
        break label284;
      }
      i = 4;
      break label284;
      if (!str.equals("KWD")) {
        break label284;
      }
      i = 5;
      break label284;
      if (!str.equals("LYD")) {
        break label284;
      }
      i = 6;
      break label284;
      if (!str.equals("OMR")) {
        break label284;
      }
      i = 7;
      break label284;
      if (!str.equals("TND")) {
        break label284;
      }
      i = 8;
      break label284;
      if (!str.equals("BIF")) {
        break label284;
      }
      i = 9;
      break label284;
      if (!str.equals("BYR")) {
        break label284;
      }
      i = 10;
      break label284;
      if (!str.equals("CLP")) {
        break label284;
      }
      i = 11;
      break label284;
      if (!str.equals("CVE")) {
        break label284;
      }
      i = 12;
      break label284;
      if (!str.equals("DJF")) {
        break label284;
      }
      i = 13;
      break label284;
      if (!str.equals("GNF")) {
        break label284;
      }
      i = 14;
      break label284;
      if (!str.equals("ISK")) {
        break label284;
      }
      i = 15;
      break label284;
      if (!str.equals("JPY")) {
        break label284;
      }
      i = 16;
      break label284;
      if (!str.equals("KMF")) {
        break label284;
      }
      i = 17;
      break label284;
      if (!str.equals("KRW")) {
        break label284;
      }
      i = 18;
      break label284;
      if (!str.equals("MGA")) {
        break label284;
      }
      i = 19;
      break label284;
      if (!str.equals("PYG")) {
        break label284;
      }
      i = 20;
      break label284;
      if (!str.equals("RWF")) {
        break label284;
      }
      i = 21;
      break label284;
      if (!str.equals("UGX")) {
        break label284;
      }
      i = 22;
      break label284;
      if (!str.equals("UYI")) {
        break label284;
      }
      i = 23;
      break label284;
      if (!str.equals("VND")) {
        break label284;
      }
      i = 24;
      break label284;
      if (!str.equals("VUV")) {
        break label284;
      }
      i = 25;
      break label284;
      if (!str.equals("XAF")) {
        break label284;
      }
      i = 26;
      break label284;
      if (!str.equals("XOF")) {
        break label284;
      }
      i = 27;
      break label284;
      if (!str.equals("XPF")) {
        break label284;
      }
      i = 28;
      break label284;
      if (!str.equals("MRO")) {
        break label284;
      }
      i = 29;
      break label284;
      paramString = " %.4f";
      d = paramLong / 10000.0D;
      break label432;
      d = (float)paramLong / 100.0F;
      if (paramLong % 100L == 0L)
      {
        paramString = " %.0f";
        break label432;
      }
      paramString = " %.2f";
      break label432;
      paramString = " %.3f";
      d = paramLong / 1000.0D;
      break label432;
      paramString = " %.0f";
      d = paramLong;
      break label432;
      paramString = " %.1f";
      d = paramLong / 10.0D;
      break label432;
      paramString = this.systemDefaultLocale;
      break label449;
    }
    label1161:
    Object localObject2 = new StringBuilder();
    if (j != 0) {}
    for (Object localObject1 = "-";; localObject1 = "") {
      return (String)localObject1 + String.format(Locale.US, new StringBuilder().append(str).append(paramString).toString(), new Object[] { Double.valueOf(d) });
    }
  }
  
  public LocaleInfo getCurrentLocaleInfo()
  {
    return this.currentLocaleInfo;
  }
  
  public Locale getSystemDefaultLocale()
  {
    return this.systemDefaultLocale;
  }
  
  public String getTranslitString(String paramString)
  {
    if (this.translitChars == null)
    {
      this.translitChars = new HashMap(520);
      this.translitChars.put("ȼ", "c");
      this.translitChars.put("ᶇ", "n");
      this.translitChars.put("ɖ", "d");
      this.translitChars.put("ỿ", "y");
      this.translitChars.put("ᴓ", "o");
      this.translitChars.put("ø", "o");
      this.translitChars.put("ḁ", "a");
      this.translitChars.put("ʯ", "h");
      this.translitChars.put("ŷ", "y");
      this.translitChars.put("ʞ", "k");
      this.translitChars.put("ừ", "u");
      this.translitChars.put("ꜳ", "aa");
      this.translitChars.put("ĳ", "ij");
      this.translitChars.put("ḽ", "l");
      this.translitChars.put("ɪ", "i");
      this.translitChars.put("ḇ", "b");
      this.translitChars.put("ʀ", "r");
      this.translitChars.put("ě", "e");
      this.translitChars.put("ﬃ", "ffi");
      this.translitChars.put("ơ", "o");
      this.translitChars.put("ⱹ", "r");
      this.translitChars.put("ồ", "o");
      this.translitChars.put("ǐ", "i");
      this.translitChars.put("ꝕ", "p");
      this.translitChars.put("ý", "y");
      this.translitChars.put("ḝ", "e");
      this.translitChars.put("ₒ", "o");
      this.translitChars.put("ⱥ", "a");
      this.translitChars.put("ʙ", "b");
      this.translitChars.put("ḛ", "e");
      this.translitChars.put("ƈ", "c");
      this.translitChars.put("ɦ", "h");
      this.translitChars.put("ᵬ", "b");
      this.translitChars.put("ṣ", "s");
      this.translitChars.put("đ", "d");
      this.translitChars.put("ỗ", "o");
      this.translitChars.put("ɟ", "j");
      this.translitChars.put("ẚ", "a");
      this.translitChars.put("ɏ", "y");
      this.translitChars.put("л", "l");
      this.translitChars.put("ʌ", "v");
      this.translitChars.put("ꝓ", "p");
      this.translitChars.put("ﬁ", "fi");
      this.translitChars.put("ᶄ", "k");
      this.translitChars.put("ḏ", "d");
      this.translitChars.put("ᴌ", "l");
      this.translitChars.put("ė", "e");
      this.translitChars.put("ё", "yo");
      this.translitChars.put("ᴋ", "k");
      this.translitChars.put("ċ", "c");
      this.translitChars.put("ʁ", "r");
      this.translitChars.put("ƕ", "hv");
      this.translitChars.put("ƀ", "b");
      this.translitChars.put("ṍ", "o");
      this.translitChars.put("ȣ", "ou");
      this.translitChars.put("ǰ", "j");
      this.translitChars.put("ᶃ", "g");
      this.translitChars.put("ṋ", "n");
      this.translitChars.put("ɉ", "j");
      this.translitChars.put("ǧ", "g");
      this.translitChars.put("ǳ", "dz");
      this.translitChars.put("ź", "z");
      this.translitChars.put("ꜷ", "au");
      this.translitChars.put("ǖ", "u");
      this.translitChars.put("ᵹ", "g");
      this.translitChars.put("ȯ", "o");
      this.translitChars.put("ɐ", "a");
      this.translitChars.put("ą", "a");
      this.translitChars.put("õ", "o");
      this.translitChars.put("ɻ", "r");
      this.translitChars.put("ꝍ", "o");
      this.translitChars.put("ǟ", "a");
      this.translitChars.put("ȴ", "l");
      this.translitChars.put("ʂ", "s");
      this.translitChars.put("ﬂ", "fl");
      this.translitChars.put("ȉ", "i");
      this.translitChars.put("ⱻ", "e");
      this.translitChars.put("ṉ", "n");
      this.translitChars.put("ï", "i");
      this.translitChars.put("ñ", "n");
      this.translitChars.put("ᴉ", "i");
      this.translitChars.put("ʇ", "t");
      this.translitChars.put("ẓ", "z");
      this.translitChars.put("ỷ", "y");
      this.translitChars.put("ȳ", "y");
      this.translitChars.put("ṩ", "s");
      this.translitChars.put("ɽ", "r");
      this.translitChars.put("ĝ", "g");
      this.translitChars.put("в", "v");
      this.translitChars.put("ᴝ", "u");
      this.translitChars.put("ḳ", "k");
      this.translitChars.put("ꝫ", "et");
      this.translitChars.put("ī", "i");
      this.translitChars.put("ť", "t");
      this.translitChars.put("ꜿ", "c");
      this.translitChars.put("ʟ", "l");
      this.translitChars.put("ꜹ", "av");
      this.translitChars.put("û", "u");
      this.translitChars.put("æ", "ae");
      this.translitChars.put("и", "i");
      this.translitChars.put("ă", "a");
      this.translitChars.put("ǘ", "u");
      this.translitChars.put("ꞅ", "s");
      this.translitChars.put("ᵣ", "r");
      this.translitChars.put("ᴀ", "a");
      this.translitChars.put("ƃ", "b");
      this.translitChars.put("ḩ", "h");
      this.translitChars.put("ṧ", "s");
      this.translitChars.put("ₑ", "e");
      this.translitChars.put("ʜ", "h");
      this.translitChars.put("ẋ", "x");
      this.translitChars.put("ꝅ", "k");
      this.translitChars.put("ḋ", "d");
      this.translitChars.put("ƣ", "oi");
      this.translitChars.put("ꝑ", "p");
      this.translitChars.put("ħ", "h");
      this.translitChars.put("ⱴ", "v");
      this.translitChars.put("ẇ", "w");
      this.translitChars.put("ǹ", "n");
      this.translitChars.put("ɯ", "m");
      this.translitChars.put("ɡ", "g");
      this.translitChars.put("ɴ", "n");
      this.translitChars.put("ᴘ", "p");
      this.translitChars.put("ᵥ", "v");
      this.translitChars.put("ū", "u");
      this.translitChars.put("ḃ", "b");
      this.translitChars.put("ṗ", "p");
      this.translitChars.put("ь", "");
      this.translitChars.put("å", "a");
      this.translitChars.put("ɕ", "c");
      this.translitChars.put("ọ", "o");
      this.translitChars.put("ắ", "a");
      this.translitChars.put("ƒ", "f");
      this.translitChars.put("ǣ", "ae");
      this.translitChars.put("ꝡ", "vy");
      this.translitChars.put("ﬀ", "ff");
      this.translitChars.put("ᶉ", "r");
      this.translitChars.put("ô", "o");
      this.translitChars.put("ǿ", "o");
      this.translitChars.put("ṳ", "u");
      this.translitChars.put("ȥ", "z");
      this.translitChars.put("ḟ", "f");
      this.translitChars.put("ḓ", "d");
      this.translitChars.put("ȇ", "e");
      this.translitChars.put("ȕ", "u");
      this.translitChars.put("п", "p");
      this.translitChars.put("ȵ", "n");
      this.translitChars.put("ʠ", "q");
      this.translitChars.put("ấ", "a");
      this.translitChars.put("ǩ", "k");
      this.translitChars.put("ĩ", "i");
      this.translitChars.put("ṵ", "u");
      this.translitChars.put("ŧ", "t");
      this.translitChars.put("ɾ", "r");
      this.translitChars.put("ƙ", "k");
      this.translitChars.put("ṫ", "t");
      this.translitChars.put("ꝗ", "q");
      this.translitChars.put("ậ", "a");
      this.translitChars.put("н", "n");
      this.translitChars.put("ʄ", "j");
      this.translitChars.put("ƚ", "l");
      this.translitChars.put("ᶂ", "f");
      this.translitChars.put("д", "d");
      this.translitChars.put("ᵴ", "s");
      this.translitChars.put("ꞃ", "r");
      this.translitChars.put("ᶌ", "v");
      this.translitChars.put("ɵ", "o");
      this.translitChars.put("ḉ", "c");
      this.translitChars.put("ᵤ", "u");
      this.translitChars.put("ẑ", "z");
      this.translitChars.put("ṹ", "u");
      this.translitChars.put("ň", "n");
      this.translitChars.put("ʍ", "w");
      this.translitChars.put("ầ", "a");
      this.translitChars.put("ǉ", "lj");
      this.translitChars.put("ɓ", "b");
      this.translitChars.put("ɼ", "r");
      this.translitChars.put("ò", "o");
      this.translitChars.put("ẘ", "w");
      this.translitChars.put("ɗ", "d");
      this.translitChars.put("ꜽ", "ay");
      this.translitChars.put("ư", "u");
      this.translitChars.put("ᶀ", "b");
      this.translitChars.put("ǜ", "u");
      this.translitChars.put("ẹ", "e");
      this.translitChars.put("ǡ", "a");
      this.translitChars.put("ɥ", "h");
      this.translitChars.put("ṏ", "o");
      this.translitChars.put("ǔ", "u");
      this.translitChars.put("ʎ", "y");
      this.translitChars.put("ȱ", "o");
      this.translitChars.put("ệ", "e");
      this.translitChars.put("ế", "e");
      this.translitChars.put("ĭ", "i");
      this.translitChars.put("ⱸ", "e");
      this.translitChars.put("ṯ", "t");
      this.translitChars.put("ᶑ", "d");
      this.translitChars.put("ḧ", "h");
      this.translitChars.put("ṥ", "s");
      this.translitChars.put("ë", "e");
      this.translitChars.put("ᴍ", "m");
      this.translitChars.put("ö", "o");
      this.translitChars.put("é", "e");
      this.translitChars.put("ı", "i");
      this.translitChars.put("ď", "d");
      this.translitChars.put("ᵯ", "m");
      this.translitChars.put("ỵ", "y");
      this.translitChars.put("я", "ya");
      this.translitChars.put("ŵ", "w");
      this.translitChars.put("ề", "e");
      this.translitChars.put("ứ", "u");
      this.translitChars.put("ƶ", "z");
      this.translitChars.put("ĵ", "j");
      this.translitChars.put("ḍ", "d");
      this.translitChars.put("ŭ", "u");
      this.translitChars.put("ʝ", "j");
      this.translitChars.put("ж", "zh");
      this.translitChars.put("ê", "e");
      this.translitChars.put("ǚ", "u");
      this.translitChars.put("ġ", "g");
      this.translitChars.put("ṙ", "r");
      this.translitChars.put("ƞ", "n");
      this.translitChars.put("ъ", "");
      this.translitChars.put("ḗ", "e");
      this.translitChars.put("ẝ", "s");
      this.translitChars.put("ᶁ", "d");
      this.translitChars.put("ķ", "k");
      this.translitChars.put("ᴂ", "ae");
      this.translitChars.put("ɘ", "e");
      this.translitChars.put("ợ", "o");
      this.translitChars.put("ḿ", "m");
      this.translitChars.put("ꜰ", "f");
      this.translitChars.put("а", "a");
      this.translitChars.put("ẵ", "a");
      this.translitChars.put("ꝏ", "oo");
      this.translitChars.put("ᶆ", "m");
      this.translitChars.put("ᵽ", "p");
      this.translitChars.put("ц", "ts");
      this.translitChars.put("ữ", "u");
      this.translitChars.put("ⱪ", "k");
      this.translitChars.put("ḥ", "h");
      this.translitChars.put("ţ", "t");
      this.translitChars.put("ᵱ", "p");
      this.translitChars.put("ṁ", "m");
      this.translitChars.put("á", "a");
      this.translitChars.put("ᴎ", "n");
      this.translitChars.put("ꝟ", "v");
      this.translitChars.put("è", "e");
      this.translitChars.put("ᶎ", "z");
      this.translitChars.put("ꝺ", "d");
      this.translitChars.put("ᶈ", "p");
      this.translitChars.put("м", "m");
      this.translitChars.put("ɫ", "l");
      this.translitChars.put("ᴢ", "z");
      this.translitChars.put("ɱ", "m");
      this.translitChars.put("ṝ", "r");
      this.translitChars.put("ṽ", "v");
      this.translitChars.put("ũ", "u");
      this.translitChars.put("ß", "ss");
      this.translitChars.put("т", "t");
      this.translitChars.put("ĥ", "h");
      this.translitChars.put("ᵵ", "t");
      this.translitChars.put("ʐ", "z");
      this.translitChars.put("ṟ", "r");
      this.translitChars.put("ɲ", "n");
      this.translitChars.put("à", "a");
      this.translitChars.put("ẙ", "y");
      this.translitChars.put("ỳ", "y");
      this.translitChars.put("ᴔ", "oe");
      this.translitChars.put("ы", "i");
      this.translitChars.put("ₓ", "x");
      this.translitChars.put("ȗ", "u");
      this.translitChars.put("ⱼ", "j");
      this.translitChars.put("ẫ", "a");
      this.translitChars.put("ʑ", "z");
      this.translitChars.put("ẛ", "s");
      this.translitChars.put("ḭ", "i");
      this.translitChars.put("ꜵ", "ao");
      this.translitChars.put("ɀ", "z");
      this.translitChars.put("ÿ", "y");
      this.translitChars.put("ǝ", "e");
      this.translitChars.put("ǭ", "o");
      this.translitChars.put("ᴅ", "d");
      this.translitChars.put("ᶅ", "l");
      this.translitChars.put("ù", "u");
      this.translitChars.put("ạ", "a");
      this.translitChars.put("ḅ", "b");
      this.translitChars.put("ụ", "u");
      this.translitChars.put("к", "k");
      this.translitChars.put("ằ", "a");
      this.translitChars.put("ᴛ", "t");
      this.translitChars.put("ƴ", "y");
      this.translitChars.put("ⱦ", "t");
      this.translitChars.put("з", "z");
      this.translitChars.put("ⱡ", "l");
      this.translitChars.put("ȷ", "j");
      this.translitChars.put("ᵶ", "z");
      this.translitChars.put("ḫ", "h");
      this.translitChars.put("ⱳ", "w");
      this.translitChars.put("ḵ", "k");
      this.translitChars.put("ờ", "o");
      this.translitChars.put("î", "i");
      this.translitChars.put("ģ", "g");
      this.translitChars.put("ȅ", "e");
      this.translitChars.put("ȧ", "a");
      this.translitChars.put("ẳ", "a");
      this.translitChars.put("щ", "sch");
      this.translitChars.put("ɋ", "q");
      this.translitChars.put("ṭ", "t");
      this.translitChars.put("ꝸ", "um");
      this.translitChars.put("ᴄ", "c");
      this.translitChars.put("ẍ", "x");
      this.translitChars.put("ủ", "u");
      this.translitChars.put("ỉ", "i");
      this.translitChars.put("ᴚ", "r");
      this.translitChars.put("ś", "s");
      this.translitChars.put("ꝋ", "o");
      this.translitChars.put("ỹ", "y");
      this.translitChars.put("ṡ", "s");
      this.translitChars.put("ǌ", "nj");
      this.translitChars.put("ȁ", "a");
      this.translitChars.put("ẗ", "t");
      this.translitChars.put("ĺ", "l");
      this.translitChars.put("ž", "z");
      this.translitChars.put("ᵺ", "th");
      this.translitChars.put("ƌ", "d");
      this.translitChars.put("ș", "s");
      this.translitChars.put("š", "s");
      this.translitChars.put("ᶙ", "u");
      this.translitChars.put("ẽ", "e");
      this.translitChars.put("ẜ", "s");
      this.translitChars.put("ɇ", "e");
      this.translitChars.put("ṷ", "u");
      this.translitChars.put("ố", "o");
      this.translitChars.put("ȿ", "s");
      this.translitChars.put("ᴠ", "v");
      this.translitChars.put("ꝭ", "is");
      this.translitChars.put("ᴏ", "o");
      this.translitChars.put("ɛ", "e");
      this.translitChars.put("ǻ", "a");
      this.translitChars.put("ﬄ", "ffl");
      this.translitChars.put("ⱺ", "o");
      this.translitChars.put("ȋ", "i");
      this.translitChars.put("ᵫ", "ue");
      this.translitChars.put("ȡ", "d");
      this.translitChars.put("ⱬ", "z");
      this.translitChars.put("ẁ", "w");
      this.translitChars.put("ᶏ", "a");
      this.translitChars.put("ꞇ", "t");
      this.translitChars.put("ğ", "g");
      this.translitChars.put("ɳ", "n");
      this.translitChars.put("ʛ", "g");
      this.translitChars.put("ᴜ", "u");
      this.translitChars.put("ф", "f");
      this.translitChars.put("ẩ", "a");
      this.translitChars.put("ṅ", "n");
      this.translitChars.put("ɨ", "i");
      this.translitChars.put("ᴙ", "r");
      this.translitChars.put("ǎ", "a");
      this.translitChars.put("ſ", "s");
      this.translitChars.put("у", "u");
      this.translitChars.put("ȫ", "o");
      this.translitChars.put("ɿ", "r");
      this.translitChars.put("ƭ", "t");
      this.translitChars.put("ḯ", "i");
      this.translitChars.put("ǽ", "ae");
      this.translitChars.put("ⱱ", "v");
      this.translitChars.put("ɶ", "oe");
      this.translitChars.put("ṃ", "m");
      this.translitChars.put("ż", "z");
      this.translitChars.put("ĕ", "e");
      this.translitChars.put("ꜻ", "av");
      this.translitChars.put("ở", "o");
      this.translitChars.put("ễ", "e");
      this.translitChars.put("ɬ", "l");
      this.translitChars.put("ị", "i");
      this.translitChars.put("ᵭ", "d");
      this.translitChars.put("ﬆ", "st");
      this.translitChars.put("ḷ", "l");
      this.translitChars.put("ŕ", "r");
      this.translitChars.put("ᴕ", "ou");
      this.translitChars.put("ʈ", "t");
      this.translitChars.put("ā", "a");
      this.translitChars.put("э", "e");
      this.translitChars.put("ḙ", "e");
      this.translitChars.put("ᴑ", "o");
      this.translitChars.put("ç", "c");
      this.translitChars.put("ᶊ", "s");
      this.translitChars.put("ặ", "a");
      this.translitChars.put("ų", "u");
      this.translitChars.put("ả", "a");
      this.translitChars.put("ǥ", "g");
      this.translitChars.put("р", "r");
      this.translitChars.put("ꝁ", "k");
      this.translitChars.put("ẕ", "z");
      this.translitChars.put("ŝ", "s");
      this.translitChars.put("ḕ", "e");
      this.translitChars.put("ɠ", "g");
      this.translitChars.put("ꝉ", "l");
      this.translitChars.put("ꝼ", "f");
      this.translitChars.put("ᶍ", "x");
      this.translitChars.put("х", "h");
      this.translitChars.put("ǒ", "o");
      this.translitChars.put("ę", "e");
      this.translitChars.put("ổ", "o");
      this.translitChars.put("ƫ", "t");
      this.translitChars.put("ǫ", "o");
      this.translitChars.put("i̇", "i");
      this.translitChars.put("ṇ", "n");
      this.translitChars.put("ć", "c");
      this.translitChars.put("ᵷ", "g");
      this.translitChars.put("ẅ", "w");
      this.translitChars.put("ḑ", "d");
      this.translitChars.put("ḹ", "l");
      this.translitChars.put("ч", "ch");
      this.translitChars.put("œ", "oe");
      this.translitChars.put("ᵳ", "r");
      this.translitChars.put("ļ", "l");
      this.translitChars.put("ȑ", "r");
      this.translitChars.put("ȭ", "o");
      this.translitChars.put("ᵰ", "n");
      this.translitChars.put("ᴁ", "ae");
      this.translitChars.put("ŀ", "l");
      this.translitChars.put("ä", "a");
      this.translitChars.put("ƥ", "p");
      this.translitChars.put("ỏ", "o");
      this.translitChars.put("į", "i");
      this.translitChars.put("ȓ", "r");
      this.translitChars.put("ǆ", "dz");
      this.translitChars.put("ḡ", "g");
      this.translitChars.put("ṻ", "u");
      this.translitChars.put("ō", "o");
      this.translitChars.put("ľ", "l");
      this.translitChars.put("ẃ", "w");
      this.translitChars.put("ț", "t");
      this.translitChars.put("ń", "n");
      this.translitChars.put("ɍ", "r");
      this.translitChars.put("ȃ", "a");
      this.translitChars.put("ü", "u");
      this.translitChars.put("ꞁ", "l");
      this.translitChars.put("ᴐ", "o");
      this.translitChars.put("ớ", "o");
      this.translitChars.put("ᴃ", "b");
      this.translitChars.put("ɹ", "r");
      this.translitChars.put("ᵲ", "r");
      this.translitChars.put("ʏ", "y");
      this.translitChars.put("ᵮ", "f");
      this.translitChars.put("ⱨ", "h");
      this.translitChars.put("ŏ", "o");
      this.translitChars.put("ú", "u");
      this.translitChars.put("ṛ", "r");
      this.translitChars.put("ʮ", "h");
      this.translitChars.put("ó", "o");
      this.translitChars.put("ů", "u");
      this.translitChars.put("ỡ", "o");
      this.translitChars.put("ṕ", "p");
      this.translitChars.put("ᶖ", "i");
      this.translitChars.put("ự", "u");
      this.translitChars.put("ã", "a");
      this.translitChars.put("ᵢ", "i");
      this.translitChars.put("ṱ", "t");
      this.translitChars.put("ể", "e");
      this.translitChars.put("ử", "u");
      this.translitChars.put("í", "i");
      this.translitChars.put("ɔ", "o");
      this.translitChars.put("с", "s");
      this.translitChars.put("й", "i");
      this.translitChars.put("ɺ", "r");
      this.translitChars.put("ɢ", "g");
      this.translitChars.put("ř", "r");
      this.translitChars.put("ẖ", "h");
      this.translitChars.put("ű", "u");
      this.translitChars.put("ȍ", "o");
      this.translitChars.put("ш", "sh");
      this.translitChars.put("ḻ", "l");
      this.translitChars.put("ḣ", "h");
      this.translitChars.put("ȶ", "t");
      this.translitChars.put("ņ", "n");
      this.translitChars.put("ᶒ", "e");
      this.translitChars.put("ì", "i");
      this.translitChars.put("ẉ", "w");
      this.translitChars.put("б", "b");
      this.translitChars.put("ē", "e");
      this.translitChars.put("ᴇ", "e");
      this.translitChars.put("ł", "l");
      this.translitChars.put("ộ", "o");
      this.translitChars.put("ɭ", "l");
      this.translitChars.put("ẏ", "y");
      this.translitChars.put("ᴊ", "j");
      this.translitChars.put("ḱ", "k");
      this.translitChars.put("ṿ", "v");
      this.translitChars.put("ȩ", "e");
      this.translitChars.put("â", "a");
      this.translitChars.put("ş", "s");
      this.translitChars.put("ŗ", "r");
      this.translitChars.put("ʋ", "v");
      this.translitChars.put("ₐ", "a");
      this.translitChars.put("ↄ", "c");
      this.translitChars.put("ᶓ", "e");
      this.translitChars.put("ɰ", "m");
      this.translitChars.put("е", "e");
      this.translitChars.put("ᴡ", "w");
      this.translitChars.put("ȏ", "o");
      this.translitChars.put("č", "c");
      this.translitChars.put("ǵ", "g");
      this.translitChars.put("ĉ", "c");
      this.translitChars.put("ю", "yu");
      this.translitChars.put("ᶗ", "o");
      this.translitChars.put("ꝃ", "k");
      this.translitChars.put("ꝙ", "q");
      this.translitChars.put("г", "g");
      this.translitChars.put("ṑ", "o");
      this.translitChars.put("ꜱ", "s");
      this.translitChars.put("ṓ", "o");
      this.translitChars.put("ȟ", "h");
      this.translitChars.put("ő", "o");
      this.translitChars.put("ꜩ", "tz");
      this.translitChars.put("ẻ", "e");
      this.translitChars.put("о", "o");
    }
    StringBuilder localStringBuilder = new StringBuilder(paramString.length());
    int j = paramString.length();
    int i = 0;
    if (i < j)
    {
      String str1 = paramString.substring(i, i + 1);
      String str2 = (String)this.translitChars.get(str1);
      if (str2 != null) {
        localStringBuilder.append(str2);
      }
      for (;;)
      {
        i += 1;
        break;
        localStringBuilder.append(str1);
      }
    }
    return localStringBuilder.toString();
  }
  
  public boolean isCurrentLocalLocale()
  {
    return this.currentLocaleInfo.isLocal();
  }
  
  public void loadRemoteLanguages(final int paramInt)
  {
    if (this.loadingRemoteLanguages) {
      return;
    }
    this.loadingRemoteLanguages = true;
    TLRPC.TL_langpack_getLanguages localTL_langpack_getLanguages = new TLRPC.TL_langpack_getLanguages();
    ConnectionsManager.getInstance(paramInt).sendRequest(localTL_langpack_getLanguages, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LocaleController.access$1102(LocaleController.this, false);
              TLRPC.Vector localVector = (TLRPC.Vector)paramAnonymousTLObject;
              HashMap localHashMap = new HashMap();
              LocaleController.this.remoteLanguages.clear();
              int i = 0;
              Object localObject2;
              Object localObject1;
              if (i < localVector.objects.size())
              {
                localObject2 = (TLRPC.TL_langPackLanguage)localVector.objects.get(i);
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d("loaded lang " + ((TLRPC.TL_langPackLanguage)localObject2).name);
                }
                localObject1 = new LocaleController.LocaleInfo();
                ((LocaleController.LocaleInfo)localObject1).nameEnglish = ((TLRPC.TL_langPackLanguage)localObject2).name;
                ((LocaleController.LocaleInfo)localObject1).name = ((TLRPC.TL_langPackLanguage)localObject2).native_name;
                ((LocaleController.LocaleInfo)localObject1).shortName = ((TLRPC.TL_langPackLanguage)localObject2).lang_code.replace('-', '_').toLowerCase();
                ((LocaleController.LocaleInfo)localObject1).pathToFile = "remote";
                localObject2 = LocaleController.this.getLanguageFromDict(((LocaleController.LocaleInfo)localObject1).getKey());
                if (localObject2 == null)
                {
                  LocaleController.this.languages.add(localObject1);
                  LocaleController.this.languagesDict.put(((LocaleController.LocaleInfo)localObject1).getKey(), localObject1);
                  localObject2 = localObject1;
                }
                for (;;)
                {
                  LocaleController.this.remoteLanguages.add(localObject1);
                  localHashMap.put(((LocaleController.LocaleInfo)localObject1).getKey(), localObject2);
                  i += 1;
                  break;
                  ((LocaleController.LocaleInfo)localObject2).nameEnglish = ((LocaleController.LocaleInfo)localObject1).nameEnglish;
                  ((LocaleController.LocaleInfo)localObject2).name = ((LocaleController.LocaleInfo)localObject1).name;
                  ((LocaleController.LocaleInfo)localObject2).pathToFile = ((LocaleController.LocaleInfo)localObject1).pathToFile;
                  localObject1 = localObject2;
                }
              }
              i = 0;
              if (i < LocaleController.this.languages.size())
              {
                localObject2 = (LocaleController.LocaleInfo)LocaleController.this.languages.get(i);
                int j = i;
                if (!((LocaleController.LocaleInfo)localObject2).isBuiltIn())
                {
                  if (((LocaleController.LocaleInfo)localObject2).isRemote()) {
                    break label344;
                  }
                  j = i;
                }
                for (;;)
                {
                  i = j + 1;
                  break;
                  label344:
                  j = i;
                  if ((LocaleController.LocaleInfo)localHashMap.get(((LocaleController.LocaleInfo)localObject2).getKey()) == null)
                  {
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d("remove lang " + ((LocaleController.LocaleInfo)localObject2).getKey());
                    }
                    LocaleController.this.languages.remove(i);
                    LocaleController.this.languagesDict.remove(((LocaleController.LocaleInfo)localObject2).getKey());
                    i -= 1;
                    j = i;
                    if (localObject2 == LocaleController.this.currentLocaleInfo)
                    {
                      if (LocaleController.this.systemDefaultLocale.getLanguage() != null) {
                        localObject2 = LocaleController.this.getLanguageFromDict(LocaleController.this.systemDefaultLocale.getLanguage());
                      }
                      localObject1 = localObject2;
                      if (localObject2 == null) {
                        localObject1 = LocaleController.this.getLanguageFromDict(LocaleController.access$1300(LocaleController.this, LocaleController.this.systemDefaultLocale));
                      }
                      localObject2 = localObject1;
                      if (localObject1 == null) {
                        localObject2 = LocaleController.this.getLanguageFromDict("en");
                      }
                      LocaleController.this.applyLanguage((LocaleController.LocaleInfo)localObject2, true, false, LocaleController.5.this.val$currentAccount);
                      NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface, new Object[0]);
                      j = i;
                    }
                  }
                }
              }
              LocaleController.this.saveOtherLanguages();
              NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.suggestedLangpack, new Object[0]);
              LocaleController.this.applyLanguage(LocaleController.this.currentLocaleInfo, true, false, LocaleController.5.this.val$currentAccount);
            }
          });
        }
      }
    }, 8);
  }
  
  public void onDeviceConfigurationChange(Configuration paramConfiguration)
  {
    if (this.changingConfiguration) {}
    do
    {
      do
      {
        return;
        is24HourFormat = DateFormat.is24HourFormat(ApplicationLoader.applicationContext);
        this.systemDefaultLocale = paramConfiguration.locale;
        if (this.languageOverride != null)
        {
          paramConfiguration = this.currentLocaleInfo;
          this.currentLocaleInfo = null;
          applyLanguage(paramConfiguration, false, false, UserConfig.selectedAccount);
          return;
        }
        paramConfiguration = paramConfiguration.locale;
      } while (paramConfiguration == null);
      String str1 = paramConfiguration.getDisplayName();
      String str2 = this.currentLocale.getDisplayName();
      if ((str1 != null) && (str2 != null) && (!str1.equals(str2))) {
        recreateFormatters();
      }
      this.currentLocale = paramConfiguration;
      this.currentPluralRules = ((PluralRules)this.allRules.get(this.currentLocale.getLanguage()));
    } while (this.currentPluralRules != null);
    this.currentPluralRules = ((PluralRules)this.allRules.get("en"));
  }
  
  public void recreateFormatters()
  {
    int i = 1;
    Object localObject2 = this.currentLocale;
    Object localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = Locale.getDefault();
    }
    String str1 = ((Locale)localObject1).getLanguage();
    localObject2 = str1;
    if (str1 == null) {
      localObject2 = "en";
    }
    localObject2 = ((String)localObject2).toLowerCase();
    boolean bool;
    label302:
    label320:
    String str2;
    if ((((String)localObject2).startsWith("ar")) || (((String)localObject2).startsWith("fa")) || ((BuildVars.DEBUG_VERSION) && ((((String)localObject2).startsWith("he")) || (((String)localObject2).startsWith("iw")))))
    {
      bool = true;
      isRTL = bool;
      if (((String)localObject2).equals("ko")) {
        i = 2;
      }
      nameDisplayOrder = i;
      this.formatterMonth = createFormatter((Locale)localObject1, getStringInternal("formatterMonth", 2131494703), "dd MMM");
      this.formatterYear = createFormatter((Locale)localObject1, getStringInternal("formatterYear", 2131494708), "dd.MM.yy");
      this.formatterYearMax = createFormatter((Locale)localObject1, getStringInternal("formatterYearMax", 2131494709), "dd.MM.yyyy");
      this.chatDate = createFormatter((Locale)localObject1, getStringInternal("chatDate", 2131494673), "d MMMM");
      this.chatFullDate = createFormatter((Locale)localObject1, getStringInternal("chatFullDate", 2131494674), "d MMMM yyyy");
      this.formatterWeek = createFormatter((Locale)localObject1, getStringInternal("formatterWeek", 2131494707), "EEE");
      this.formatterMonthYear = createFormatter((Locale)localObject1, getStringInternal("formatterMonthYear", 2131494704), "MMMM yyyy");
      if ((!((String)localObject2).toLowerCase().equals("ar")) && (!((String)localObject2).toLowerCase().equals("ko"))) {
        break label477;
      }
      localObject2 = localObject1;
      if (!is24HourFormat) {
        break label485;
      }
      str1 = getStringInternal("formatterDay24H", 2131494702);
      if (!is24HourFormat) {
        break label500;
      }
      str2 = "HH:mm";
      label331:
      this.formatterDay = createFormatter((Locale)localObject2, str1, str2);
      if (!is24HourFormat) {
        break label508;
      }
      localObject2 = getStringInternal("formatterStats24H", 2131494706);
      label363:
      if (!is24HourFormat) {
        break label523;
      }
      str1 = "MMM dd yyyy, HH:mm";
      label374:
      this.formatterStats = createFormatter((Locale)localObject1, (String)localObject2, str1);
      if (!is24HourFormat) {
        break label531;
      }
      localObject2 = getStringInternal("formatterBannedUntil24H", 2131494698);
      label405:
      if (!is24HourFormat) {
        break label546;
      }
      str1 = "MMM dd yyyy, HH:mm";
      label416:
      this.formatterBannedUntil = createFormatter((Locale)localObject1, (String)localObject2, str1);
      if (!is24HourFormat) {
        break label554;
      }
      localObject2 = getStringInternal("formatterBannedUntilThisYear24H", 2131494700);
      label447:
      if (!is24HourFormat) {
        break label569;
      }
    }
    label477:
    label485:
    label500:
    label508:
    label523:
    label531:
    label546:
    label554:
    label569:
    for (str1 = "MMM dd, HH:mm";; str1 = "MMM dd, h:mm a")
    {
      this.formatterBannedUntilThisYear = createFormatter((Locale)localObject1, (String)localObject2, str1);
      return;
      bool = false;
      break;
      localObject2 = Locale.US;
      break label302;
      str1 = getStringInternal("formatterDay12H", 2131494701);
      break label320;
      str2 = "h:mm a";
      break label331;
      localObject2 = getStringInternal("formatterStats12H", 2131494705);
      break label363;
      str1 = "MMM dd yyyy, h:mm a";
      break label374;
      localObject2 = getStringInternal("formatterBannedUntil12H", 2131494697);
      break label405;
      str1 = "MMM dd yyyy, h:mm a";
      break label416;
      localObject2 = getStringInternal("formatterBannedUntilThisYear12H", 2131494699);
      break label447;
    }
  }
  
  public void reloadCurrentRemoteLocale(int paramInt)
  {
    applyRemoteLanguage(this.currentLocaleInfo, true, paramInt);
  }
  
  public void saveRemoteLocaleStrings(final TLRPC.TL_langPackDifference paramTL_langPackDifference, int paramInt)
  {
    if ((paramTL_langPackDifference == null) || (paramTL_langPackDifference.strings.isEmpty())) {}
    final String str1;
    do
    {
      return;
      str1 = paramTL_langPackDifference.lang_code.replace('-', '_').toLowerCase();
    } while (!str1.equals(this.currentLocaleInfo.shortName));
    File localFile = new File(ApplicationLoader.getFilesDirFixed(), "remote_" + str1 + ".xml");
    for (;;)
    {
      try
      {
        Object localObject1;
        Object localObject3;
        if (paramTL_langPackDifference.from_version == 0)
        {
          localObject1 = new HashMap();
          continue;
          if (paramInt >= paramTL_langPackDifference.strings.size()) {
            continue;
          }
          localObject3 = (TLRPC.LangPackString)paramTL_langPackDifference.strings.get(paramInt);
          if ((localObject3 instanceof TLRPC.TL_langPackString))
          {
            ((HashMap)localObject1).put(((TLRPC.LangPackString)localObject3).key, escapeString(((TLRPC.LangPackString)localObject3).value));
            continue;
          }
        }
        else
        {
          localObject1 = getLocaleFileStrings(localFile, true);
          continue;
        }
        if ((localObject3 instanceof TLRPC.TL_langPackStringPluralized))
        {
          String str2 = ((TLRPC.LangPackString)localObject3).key + "_zero";
          if (((TLRPC.LangPackString)localObject3).zero_value == null) {
            break label704;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).zero_value);
          ((HashMap)localObject1).put(str2, localObject2);
          str2 = ((TLRPC.LangPackString)localObject3).key + "_one";
          if (((TLRPC.LangPackString)localObject3).one_value == null) {
            break label712;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).one_value);
          ((HashMap)localObject1).put(str2, localObject2);
          str2 = ((TLRPC.LangPackString)localObject3).key + "_two";
          if (((TLRPC.LangPackString)localObject3).two_value == null) {
            break label720;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).two_value);
          ((HashMap)localObject1).put(str2, localObject2);
          str2 = ((TLRPC.LangPackString)localObject3).key + "_few";
          if (((TLRPC.LangPackString)localObject3).few_value == null) {
            break label728;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).few_value);
          ((HashMap)localObject1).put(str2, localObject2);
          str2 = ((TLRPC.LangPackString)localObject3).key + "_many";
          if (((TLRPC.LangPackString)localObject3).many_value == null) {
            break label736;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).many_value);
          ((HashMap)localObject1).put(str2, localObject2);
          str2 = ((TLRPC.LangPackString)localObject3).key + "_other";
          if (((TLRPC.LangPackString)localObject3).other_value == null) {
            break label744;
          }
          localObject2 = escapeString(((TLRPC.LangPackString)localObject3).other_value);
          ((HashMap)localObject1).put(str2, localObject2);
        }
        else if ((localObject3 instanceof TLRPC.TL_langPackStringDeleted))
        {
          ((HashMap)localObject1).remove(((TLRPC.LangPackString)localObject3).key);
          continue;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("save locale file to " + localFile);
          }
          localObject2 = new BufferedWriter(new FileWriter(localFile));
          ((BufferedWriter)localObject2).write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
          ((BufferedWriter)localObject2).write("<resources>\n");
          localObject1 = ((HashMap)localObject1).entrySet().iterator();
          if (((Iterator)localObject1).hasNext())
          {
            localObject3 = (Map.Entry)((Iterator)localObject1).next();
            ((BufferedWriter)localObject2).write(String.format("<string name=\"%1$s\">%2$s</string>\n", new Object[] { ((Map.Entry)localObject3).getKey(), ((Map.Entry)localObject3).getValue() }));
            continue;
          }
          ((BufferedWriter)localObject2).write("</resources>");
          ((BufferedWriter)localObject2).close();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LocaleController.LocaleInfo localLocaleInfo = LocaleController.this.getLanguageFromDict(str1);
              if (localLocaleInfo != null) {
                localLocaleInfo.version = paramTL_langPackDifference.version;
              }
              LocaleController.this.saveOtherLanguages();
              if ((LocaleController.this.currentLocaleInfo != null) && (LocaleController.this.currentLocaleInfo.isLocal())) {
                return;
              }
              for (;;)
              {
                try
                {
                  localObject = localLocaleInfo.shortName.split("_");
                  if (localObject.length != 1) {
                    continue;
                  }
                  localObject = new Locale(localLocaleInfo.shortName);
                  if (localObject != null)
                  {
                    LocaleController.access$502(LocaleController.this, localLocaleInfo.shortName);
                    SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
                    localEditor.putString("language", localLocaleInfo.getKey());
                    localEditor.commit();
                  }
                  if (localObject != null)
                  {
                    LocaleController.access$602(LocaleController.this, this.val$valuesToSet);
                    LocaleController.access$702(LocaleController.this, (Locale)localObject);
                    LocaleController.access$402(LocaleController.this, localLocaleInfo);
                    LocaleController.access$802(LocaleController.this, (LocaleController.PluralRules)LocaleController.this.allRules.get(LocaleController.this.currentLocale.getLanguage()));
                    if (LocaleController.this.currentPluralRules == null) {
                      LocaleController.access$802(LocaleController.this, (LocaleController.PluralRules)LocaleController.this.allRules.get("en"));
                    }
                    LocaleController.access$1002(LocaleController.this, true);
                    Locale.setDefault(LocaleController.this.currentLocale);
                    localObject = new Configuration();
                    ((Configuration)localObject).locale = LocaleController.this.currentLocale;
                    ApplicationLoader.applicationContext.getResources().updateConfiguration((Configuration)localObject, ApplicationLoader.applicationContext.getResources().getDisplayMetrics());
                    LocaleController.access$1002(LocaleController.this, false);
                  }
                }
                catch (Exception localException)
                {
                  Object localObject;
                  FileLog.e(localException);
                  LocaleController.access$1002(LocaleController.this, false);
                  continue;
                }
                LocaleController.this.recreateFormatters();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface, new Object[0]);
                return;
                localObject = new Locale(localObject[0], localObject[1]);
              }
            }
          });
          return;
          paramInt = 0;
          continue;
        }
        paramInt += 1;
        continue;
        localObject2 = "";
      }
      catch (Exception paramTL_langPackDifference)
      {
        return;
      }
      label704:
      continue;
      label712:
      Object localObject2 = "";
      continue;
      label720:
      localObject2 = "";
      continue;
      label728:
      localObject2 = "";
      continue;
      label736:
      localObject2 = "";
      continue;
      label744:
      localObject2 = "";
    }
  }
  
  public static class LocaleInfo
  {
    public boolean builtIn;
    public String name;
    public String nameEnglish;
    public String pathToFile;
    public String shortName;
    public int version;
    
    public static LocaleInfo createWithString(String paramString)
    {
      if ((paramString == null) || (paramString.length() == 0)) {
        paramString = null;
      }
      String[] arrayOfString;
      LocaleInfo localLocaleInfo;
      do
      {
        do
        {
          return paramString;
          arrayOfString = paramString.split("\\|");
          paramString = null;
        } while (arrayOfString.length < 4);
        localLocaleInfo = new LocaleInfo();
        localLocaleInfo.name = arrayOfString[0];
        localLocaleInfo.nameEnglish = arrayOfString[1];
        localLocaleInfo.shortName = arrayOfString[2].toLowerCase();
        localLocaleInfo.pathToFile = arrayOfString[3];
        paramString = localLocaleInfo;
      } while (arrayOfString.length < 5);
      localLocaleInfo.version = Utilities.parseInt(arrayOfString[4]).intValue();
      return localLocaleInfo;
    }
    
    public String getKey()
    {
      if ((this.pathToFile != null) && (!"remote".equals(this.pathToFile))) {
        return "local_" + this.shortName;
      }
      return this.shortName;
    }
    
    public File getPathToFile()
    {
      if (isRemote()) {
        return new File(ApplicationLoader.getFilesDirFixed(), "remote_" + this.shortName + ".xml");
      }
      if (!TextUtils.isEmpty(this.pathToFile)) {
        return new File(this.pathToFile);
      }
      return null;
    }
    
    public String getSaveString()
    {
      return this.name + "|" + this.nameEnglish + "|" + this.shortName + "|" + this.pathToFile + "|" + this.version;
    }
    
    public boolean isBuiltIn()
    {
      return this.builtIn;
    }
    
    public boolean isLocal()
    {
      return (!TextUtils.isEmpty(this.pathToFile)) && (!isRemote());
    }
    
    public boolean isRemote()
    {
      return "remote".equals(this.pathToFile);
    }
  }
  
  public static abstract class PluralRules
  {
    abstract int quantityForNumber(int paramInt);
  }
  
  public static class PluralRules_Arabic
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      if (paramInt == 0) {
        return 1;
      }
      if (paramInt == 1) {
        return 2;
      }
      if (paramInt == 2) {
        return 4;
      }
      if ((i >= 3) && (i <= 10)) {
        return 8;
      }
      if ((i >= 11) && (i <= 99)) {
        return 16;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Balkan
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      paramInt %= 10;
      if ((paramInt == 1) && (i != 11)) {
        return 2;
      }
      if ((paramInt >= 2) && (paramInt <= 4) && ((i < 12) || (i > 14))) {
        return 8;
      }
      if ((paramInt == 0) || ((paramInt >= 5) && (paramInt <= 9)) || ((i >= 11) && (i <= 14))) {
        return 16;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Breton
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 0) {
        return 1;
      }
      if (paramInt == 1) {
        return 2;
      }
      if (paramInt == 2) {
        return 4;
      }
      if (paramInt == 3) {
        return 8;
      }
      if (paramInt == 6) {
        return 16;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Czech
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 1) {
        return 2;
      }
      if ((paramInt >= 2) && (paramInt <= 4)) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_French
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if ((paramInt >= 0) && (paramInt < 2)) {
        return 2;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Langi
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = 2;
      if (paramInt == 0) {
        i = 1;
      }
      while ((paramInt > 0) && (paramInt < 2)) {
        return i;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Latvian
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 0) {
        return 1;
      }
      if ((paramInt % 10 == 1) && (paramInt % 100 != 11)) {
        return 2;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Lithuanian
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      paramInt %= 10;
      if ((paramInt == 1) && ((i < 11) || (i > 19))) {
        return 2;
      }
      if ((paramInt >= 2) && (paramInt <= 9) && ((i < 11) || (i > 19))) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Macedonian
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if ((paramInt % 10 == 1) && (paramInt != 11)) {
        return 2;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Maltese
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      if (paramInt == 1) {
        return 2;
      }
      if ((paramInt == 0) || ((i >= 2) && (i <= 10))) {
        return 8;
      }
      if ((i >= 11) && (i <= 19)) {
        return 16;
      }
      return 0;
    }
  }
  
  public static class PluralRules_None
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      return 0;
    }
  }
  
  public static class PluralRules_One
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 1) {
        return 2;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Polish
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      int j = paramInt % 10;
      if (paramInt == 1) {
        return 2;
      }
      if ((j >= 2) && (j <= 4) && ((i < 12) || (i > 14)) && ((i < 22) || (i > 24))) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Romanian
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      int i = paramInt % 100;
      if (paramInt == 1) {
        return 2;
      }
      if ((paramInt == 0) || ((i >= 1) && (i <= 19))) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Slovenian
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      paramInt %= 100;
      if (paramInt == 1) {
        return 2;
      }
      if (paramInt == 2) {
        return 4;
      }
      if ((paramInt >= 3) && (paramInt <= 4)) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Tachelhit
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if ((paramInt >= 0) && (paramInt <= 1)) {
        return 2;
      }
      if ((paramInt >= 2) && (paramInt <= 10)) {
        return 8;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Two
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 1) {
        return 2;
      }
      if (paramInt == 2) {
        return 4;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Welsh
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if (paramInt == 0) {
        return 1;
      }
      if (paramInt == 1) {
        return 2;
      }
      if (paramInt == 2) {
        return 4;
      }
      if (paramInt == 3) {
        return 8;
      }
      if (paramInt == 6) {
        return 16;
      }
      return 0;
    }
  }
  
  public static class PluralRules_Zero
    extends LocaleController.PluralRules
  {
    public int quantityForNumber(int paramInt)
    {
      if ((paramInt == 0) || (paramInt == 1)) {
        return 2;
      }
      return 0;
    }
  }
  
  private class TimeZoneChangedReceiver
    extends BroadcastReceiver
  {
    private TimeZoneChangedReceiver() {}
    
    public void onReceive(Context paramContext, Intent paramIntent)
    {
      ApplicationLoader.applicationHandler.post(new Runnable()
      {
        public void run()
        {
          if (!LocaleController.this.formatterMonth.getTimeZone().equals(TimeZone.getDefault())) {
            LocaleController.getInstance().recreateFormatters();
          }
        }
      });
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/LocaleController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */