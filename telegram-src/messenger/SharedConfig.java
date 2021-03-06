package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import java.io.File;
import java.security.SecureRandom;

public class SharedConfig
{
  public static boolean allowBigEmoji;
  public static boolean allowScreenCapture;
  public static boolean appLocked;
  public static int autoLockIn;
  public static boolean autoplayGifs;
  private static boolean configLoaded;
  public static boolean customTabs;
  public static boolean directShare;
  public static int fontSize;
  public static boolean groupPhotosEnabled;
  public static boolean inappCamera;
  public static boolean isWaitingForPasscodeEnter;
  public static long lastAppPauseTime;
  private static int lastLocalId;
  public static int lastPauseTime;
  public static String lastUpdateVersion;
  private static final Object localIdSync;
  public static String passcodeHash;
  public static byte[] passcodeSalt;
  public static int passcodeType;
  public static boolean playOrderReversed;
  public static byte[] pushAuthKey;
  public static byte[] pushAuthKeyId;
  public static String pushString = "";
  public static boolean raiseToSpeak;
  public static int repeatMode;
  public static boolean roundCamera16to9;
  public static boolean saveIncomingPhotos;
  public static boolean saveStreamMedia;
  public static boolean saveToGallery;
  public static boolean shuffleMusic;
  public static boolean streamAllVideo;
  public static boolean streamMedia;
  public static int suggestStickers;
  private static final Object sync;
  public static boolean useFingerprint;
  public static boolean useSystemEmoji;
  
  static
  {
    passcodeHash = "";
    passcodeSalt = new byte[0];
    autoLockIn = 3600;
    useFingerprint = true;
    lastLocalId = -210000;
    sync = new Object();
    localIdSync = new Object();
    autoplayGifs = true;
    raiseToSpeak = true;
    customTabs = true;
    directShare = true;
    inappCamera = true;
    roundCamera16to9 = true;
    groupPhotosEnabled = true;
    streamMedia = true;
    streamAllVideo = false;
    saveStreamMedia = true;
    fontSize = AndroidUtilities.dp(16.0F);
    loadConfig();
  }
  
  public static boolean checkPasscode(String paramString)
  {
    boolean bool;
    byte[] arrayOfByte;
    if (passcodeSalt.length == 0)
    {
      bool = Utilities.MD5(paramString).equals(passcodeHash);
      if (bool) {}
      try
      {
        passcodeSalt = new byte[16];
        Utilities.random.nextBytes(passcodeSalt);
        paramString = paramString.getBytes("UTF-8");
        arrayOfByte = new byte[paramString.length + 32];
        System.arraycopy(passcodeSalt, 0, arrayOfByte, 0, 16);
        System.arraycopy(paramString, 0, arrayOfByte, 16, paramString.length);
        System.arraycopy(passcodeSalt, 0, arrayOfByte, paramString.length + 16, 16);
        passcodeHash = Utilities.bytesToHex(Utilities.computeSHA256(arrayOfByte, 0, arrayOfByte.length));
        saveConfig();
        return bool;
      }
      catch (Exception paramString)
      {
        FileLog.e(paramString);
        return bool;
      }
    }
    try
    {
      paramString = paramString.getBytes("UTF-8");
      arrayOfByte = new byte[paramString.length + 32];
      System.arraycopy(passcodeSalt, 0, arrayOfByte, 0, 16);
      System.arraycopy(paramString, 0, arrayOfByte, 16, paramString.length);
      System.arraycopy(passcodeSalt, 0, arrayOfByte, paramString.length + 16, 16);
      paramString = Utilities.bytesToHex(Utilities.computeSHA256(arrayOfByte, 0, arrayOfByte.length));
      bool = passcodeHash.equals(paramString);
      return bool;
    }
    catch (Exception paramString)
    {
      FileLog.e(paramString);
    }
    return false;
  }
  
  public static void checkSaveToGalleryFiles()
  {
    try
    {
      File localFile2 = new File(Environment.getExternalStorageDirectory(), "Telegram");
      File localFile1 = new File(localFile2, "Telegram Images");
      localFile1.mkdir();
      localFile2 = new File(localFile2, "Telegram Video");
      localFile2.mkdir();
      if (saveToGallery)
      {
        if (localFile1.isDirectory()) {
          new File(localFile1, ".nomedia").delete();
        }
        if (localFile2.isDirectory()) {
          new File(localFile2, ".nomedia").delete();
        }
      }
      else
      {
        if (localFile1.isDirectory()) {
          new File(localFile1, ".nomedia").createNewFile();
        }
        if (localFile2.isDirectory())
        {
          new File(localFile2, ".nomedia").createNewFile();
          return;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  public static void clearConfig()
  {
    saveIncomingPhotos = false;
    appLocked = false;
    passcodeType = 0;
    passcodeHash = "";
    passcodeSalt = new byte[0];
    autoLockIn = 3600;
    lastPauseTime = 0;
    useFingerprint = true;
    isWaitingForPasscodeEnter = false;
    allowScreenCapture = false;
    lastUpdateVersion = BuildVars.BUILD_VERSION_STRING;
    saveConfig();
  }
  
  public static int getLastLocalId()
  {
    synchronized (localIdSync)
    {
      int i = lastLocalId;
      lastLocalId = i - 1;
      return i;
    }
  }
  
  public static void loadConfig()
  {
    for (;;)
    {
      synchronized (sync)
      {
        if (configLoaded) {
          return;
        }
        Object localObject2 = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
        saveIncomingPhotos = ((SharedPreferences)localObject2).getBoolean("saveIncomingPhotos", false);
        passcodeHash = ((SharedPreferences)localObject2).getString("passcodeHash1", "");
        appLocked = ((SharedPreferences)localObject2).getBoolean("appLocked", false);
        passcodeType = ((SharedPreferences)localObject2).getInt("passcodeType", 0);
        autoLockIn = ((SharedPreferences)localObject2).getInt("autoLockIn", 3600);
        lastPauseTime = ((SharedPreferences)localObject2).getInt("lastPauseTime", 0);
        lastAppPauseTime = ((SharedPreferences)localObject2).getLong("lastAppPauseTime", 0L);
        useFingerprint = ((SharedPreferences)localObject2).getBoolean("useFingerprint", true);
        lastUpdateVersion = ((SharedPreferences)localObject2).getString("lastUpdateVersion2", "3.5");
        allowScreenCapture = ((SharedPreferences)localObject2).getBoolean("allowScreenCapture", false);
        lastLocalId = ((SharedPreferences)localObject2).getInt("lastLocalId", -210000);
        pushString = ((SharedPreferences)localObject2).getString("pushString2", "");
        String str = ((SharedPreferences)localObject2).getString("pushAuthKey", null);
        if (!TextUtils.isEmpty(str)) {
          pushAuthKey = Base64.decode(str, 0);
        }
        if ((passcodeHash.length() > 0) && (lastPauseTime == 0)) {
          lastPauseTime = (int)(System.currentTimeMillis() / 1000L - 600L);
        }
        localObject2 = ((SharedPreferences)localObject2).getString("passcodeSalt", "");
        if (((String)localObject2).length() > 0)
        {
          passcodeSalt = Base64.decode((String)localObject2, 0);
          localObject2 = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
          saveToGallery = ((SharedPreferences)localObject2).getBoolean("save_gallery", false);
          autoplayGifs = ((SharedPreferences)localObject2).getBoolean("autoplay_gif", true);
          raiseToSpeak = ((SharedPreferences)localObject2).getBoolean("raise_to_speak", true);
          customTabs = ((SharedPreferences)localObject2).getBoolean("custom_tabs", true);
          directShare = ((SharedPreferences)localObject2).getBoolean("direct_share", true);
          shuffleMusic = ((SharedPreferences)localObject2).getBoolean("shuffleMusic", false);
          playOrderReversed = ((SharedPreferences)localObject2).getBoolean("playOrderReversed", false);
          inappCamera = ((SharedPreferences)localObject2).getBoolean("inappCamera", true);
          roundCamera16to9 = true;
          groupPhotosEnabled = ((SharedPreferences)localObject2).getBoolean("groupPhotosEnabled", true);
          repeatMode = ((SharedPreferences)localObject2).getInt("repeatMode", 0);
          if (!AndroidUtilities.isTablet()) {
            break label532;
          }
          i = 18;
          fontSize = ((SharedPreferences)localObject2).getInt("fons_size", i);
          allowBigEmoji = ((SharedPreferences)localObject2).getBoolean("allowBigEmoji", false);
          useSystemEmoji = ((SharedPreferences)localObject2).getBoolean("useSystemEmoji", false);
          streamMedia = ((SharedPreferences)localObject2).getBoolean("streamMedia", true);
          saveStreamMedia = ((SharedPreferences)localObject2).getBoolean("saveStreamMedia", true);
          streamAllVideo = ((SharedPreferences)localObject2).getBoolean("streamAllVideo", BuildVars.DEBUG_VERSION);
          suggestStickers = ((SharedPreferences)localObject2).getInt("suggestStickers", 0);
          configLoaded = true;
          return;
        }
      }
      passcodeSalt = new byte[0];
      continue;
      label532:
      int i = 16;
    }
  }
  
  public static void saveConfig()
  {
    synchronized (sync)
    {
      for (;;)
      {
        try
        {
          SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0).edit();
          localEditor.putBoolean("saveIncomingPhotos", saveIncomingPhotos);
          localEditor.putString("passcodeHash1", passcodeHash);
          if (passcodeSalt.length <= 0) {
            continue;
          }
          str = Base64.encodeToString(passcodeSalt, 0);
          localEditor.putString("passcodeSalt", str);
          localEditor.putBoolean("appLocked", appLocked);
          localEditor.putInt("passcodeType", passcodeType);
          localEditor.putInt("autoLockIn", autoLockIn);
          localEditor.putInt("lastPauseTime", lastPauseTime);
          localEditor.putLong("lastAppPauseTime", lastAppPauseTime);
          localEditor.putString("lastUpdateVersion2", lastUpdateVersion);
          localEditor.putBoolean("useFingerprint", useFingerprint);
          localEditor.putBoolean("allowScreenCapture", allowScreenCapture);
          localEditor.putString("pushString2", pushString);
          if (pushAuthKey == null) {
            continue;
          }
          str = Base64.encodeToString(pushAuthKey, 0);
          localEditor.putString("pushAuthKey", str);
          localEditor.putInt("lastLocalId", lastLocalId);
          localEditor.commit();
        }
        catch (Exception localException)
        {
          String str;
          FileLog.e(localException);
          continue;
        }
        return;
        str = "";
      }
      str = "";
    }
  }
  
  public static void setSuggestStickers(int paramInt)
  {
    suggestStickers = paramInt;
    SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
    localEditor.putInt("suggestStickers", suggestStickers);
    localEditor.commit();
  }
  
  public static void toggleAutoplayGifs()
  {
    if (!autoplayGifs) {}
    for (boolean bool = true;; bool = false)
    {
      autoplayGifs = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("autoplay_gif", autoplayGifs);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleCustomTabs()
  {
    if (!customTabs) {}
    for (boolean bool = true;; bool = false)
    {
      customTabs = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("custom_tabs", customTabs);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleDirectShare()
  {
    if (!directShare) {}
    for (boolean bool = true;; bool = false)
    {
      directShare = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("direct_share", directShare);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleGroupPhotosEnabled()
  {
    if (!groupPhotosEnabled) {}
    for (boolean bool = true;; bool = false)
    {
      groupPhotosEnabled = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("groupPhotosEnabled", groupPhotosEnabled);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleInappCamera()
  {
    if (!inappCamera) {}
    for (boolean bool = true;; bool = false)
    {
      inappCamera = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("direct_share", inappCamera);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleRepeatMode()
  {
    repeatMode += 1;
    if (repeatMode > 2) {
      repeatMode = 0;
    }
    SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
    localEditor.putInt("repeatMode", repeatMode);
    localEditor.commit();
  }
  
  public static void toggleRoundCamera16to9()
  {
    if (!roundCamera16to9) {}
    for (boolean bool = true;; bool = false)
    {
      roundCamera16to9 = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("roundCamera16to9", roundCamera16to9);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleSaveStreamMedia()
  {
    if (!saveStreamMedia) {}
    for (boolean bool = true;; bool = false)
    {
      saveStreamMedia = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("saveStreamMedia", saveStreamMedia);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleSaveToGallery()
  {
    if (!saveToGallery) {}
    for (boolean bool = true;; bool = false)
    {
      saveToGallery = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("save_gallery", saveToGallery);
      localEditor.commit();
      checkSaveToGalleryFiles();
      return;
    }
  }
  
  public static void toggleShuffleMusic(int paramInt)
  {
    boolean bool2 = true;
    boolean bool1 = true;
    if (paramInt == 2)
    {
      if (!shuffleMusic) {}
      for (;;)
      {
        shuffleMusic = bool1;
        MediaController.getInstance().checkIsNextMediaFileDownloaded();
        SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
        localEditor.putBoolean("shuffleMusic", shuffleMusic);
        localEditor.putBoolean("playOrderReversed", playOrderReversed);
        localEditor.commit();
        return;
        bool1 = false;
      }
    }
    if (!playOrderReversed) {}
    for (bool1 = bool2;; bool1 = false)
    {
      playOrderReversed = bool1;
      break;
    }
  }
  
  public static void toggleStreamAllVideo()
  {
    if (!streamAllVideo) {}
    for (boolean bool = true;; bool = false)
    {
      streamAllVideo = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("streamAllVideo", streamAllVideo);
      localEditor.commit();
      return;
    }
  }
  
  public static void toggleStreamMedia()
  {
    if (!streamMedia) {}
    for (boolean bool = true;; bool = false)
    {
      streamMedia = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("streamMedia", streamMedia);
      localEditor.commit();
      return;
    }
  }
  
  public static void toogleRaiseToSpeak()
  {
    if (!raiseToSpeak) {}
    for (boolean bool = true;; bool = false)
    {
      raiseToSpeak = bool;
      SharedPreferences.Editor localEditor = MessagesController.getGlobalMainSettings().edit();
      localEditor.putBoolean("raise_to_speak", raiseToSpeak);
      localEditor.commit();
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/SharedConfig.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */