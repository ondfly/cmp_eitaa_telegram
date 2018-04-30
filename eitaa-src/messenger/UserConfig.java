package ir.eitaa.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import ir.eitaa.tgnet.AbstractSerializedData;
import ir.eitaa.tgnet.SerializedData;
import ir.eitaa.tgnet.TLRPC.User;
import java.io.File;
import java.security.SecureRandom;
import java.util.UUID;

public class UserConfig
{
  public static boolean appLocked;
  public static int autoLockIn;
  public static boolean blockedUsersLoaded;
  public static String contactsHash;
  private static TLRPC.User currentUser;
  public static boolean draftsLoaded;
  public static String imei;
  public static boolean isWaitingForPasscodeEnter;
  public static int lastBroadcastId;
  public static int lastContactsSyncTime;
  public static int lastHintsSyncTime;
  public static int lastLocalId;
  public static int lastPauseTime;
  public static int lastSendMessageId;
  public static String lastUpdateVersion;
  public static long migrateOffsetAccess = -1L;
  public static int migrateOffsetChannelId;
  public static int migrateOffsetChatId;
  public static int migrateOffsetDate;
  public static int migrateOffsetId;
  public static int migrateOffsetUserId;
  public static String passcodeHash;
  public static byte[] passcodeSalt;
  public static int passcodeType;
  public static String pushString = "";
  public static boolean registeredForPush;
  public static boolean saveIncomingPhotos;
  public static Boolean switchBackEnd = Boolean.valueOf(false);
  private static final Object sync;
  public static String token;
  public static boolean useFingerprint;
  
  static
  {
    lastSendMessageId = -210000;
    lastLocalId = -210000;
    lastBroadcastId = -1;
    contactsHash = "";
    sync = new Object();
    passcodeHash = "";
    passcodeSalt = new byte[0];
    autoLockIn = 3600;
    useFingerprint = true;
    migrateOffsetId = -1;
    migrateOffsetDate = -1;
    migrateOffsetUserId = -1;
    migrateOffsetChatId = -1;
    migrateOffsetChannelId = -1;
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
        saveConfig(false);
        return bool;
      }
      catch (Exception paramString)
      {
        FileLog.e("TSMS", paramString);
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
      FileLog.e("TSMS", paramString);
    }
    return false;
  }
  
  public static void clearConfig()
  {
    token = "";
    imei = UUID.randomUUID().toString();
    switchBackEnd = Boolean.valueOf(false);
    currentUser = null;
    registeredForPush = false;
    contactsHash = "";
    lastSendMessageId = -210000;
    lastBroadcastId = -1;
    saveIncomingPhotos = false;
    blockedUsersLoaded = false;
    migrateOffsetId = -1;
    migrateOffsetDate = -1;
    migrateOffsetUserId = -1;
    migrateOffsetChatId = -1;
    migrateOffsetChannelId = -1;
    migrateOffsetAccess = -1L;
    appLocked = false;
    passcodeType = 0;
    passcodeHash = "";
    passcodeSalt = new byte[0];
    autoLockIn = 3600;
    lastPauseTime = 0;
    useFingerprint = true;
    draftsLoaded = true;
    isWaitingForPasscodeEnter = false;
    lastUpdateVersion = BuildVars.BUILD_VERSION_STRING;
    lastContactsSyncTime = (int)(System.currentTimeMillis() / 1000L) - 82800;
    lastHintsSyncTime = (int)(System.currentTimeMillis() / 1000L) - 90000;
    saveConfig(true);
  }
  
  public static int getClientUserId()
  {
    for (;;)
    {
      synchronized (sync)
      {
        if (currentUser != null)
        {
          i = currentUser.id;
          return i;
        }
      }
      int i = 0;
    }
  }
  
  public static TLRPC.User getCurrentUser()
  {
    synchronized (sync)
    {
      TLRPC.User localUser = currentUser;
      return localUser;
    }
  }
  
  public static int getNewMessageId()
  {
    synchronized (sync)
    {
      int i = lastSendMessageId;
      lastSendMessageId -= 1;
      return i;
    }
  }
  
  public static boolean isClientActivated()
  {
    for (;;)
    {
      synchronized (sync)
      {
        if (currentUser != null)
        {
          bool = true;
          return bool;
        }
      }
      boolean bool = false;
    }
  }
  
  public static void loadConfig()
  {
    boolean bool = false;
    synchronized (sync)
    {
      Object localObject2 = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
      token = ((SharedPreferences)localObject2).getString("token", "");
      imei = ((SharedPreferences)localObject2).getString("imei", UUID.randomUUID().toString());
      switchBackEnd = Boolean.valueOf(((SharedPreferences)localObject2).getBoolean("switchBackEnd", false));
      if (BuildVars.DEBUG_VERSION) {
        bool = switchBackEnd.booleanValue();
      }
      switchBackEnd = Boolean.valueOf(bool);
      registeredForPush = ((SharedPreferences)localObject2).getBoolean("registeredForPush", false);
      pushString = ((SharedPreferences)localObject2).getString("pushString2", "");
      lastSendMessageId = ((SharedPreferences)localObject2).getInt("lastSendMessageId", -210000);
      lastLocalId = ((SharedPreferences)localObject2).getInt("lastLocalId", -210000);
      contactsHash = ((SharedPreferences)localObject2).getString("contactsHash", "");
      saveIncomingPhotos = ((SharedPreferences)localObject2).getBoolean("saveIncomingPhotos", false);
      lastBroadcastId = ((SharedPreferences)localObject2).getInt("lastBroadcastId", -1);
      blockedUsersLoaded = ((SharedPreferences)localObject2).getBoolean("blockedUsersLoaded", false);
      passcodeHash = ((SharedPreferences)localObject2).getString("passcodeHash1", "");
      appLocked = ((SharedPreferences)localObject2).getBoolean("appLocked", false);
      passcodeType = ((SharedPreferences)localObject2).getInt("passcodeType", 0);
      autoLockIn = ((SharedPreferences)localObject2).getInt("autoLockIn", 3600);
      lastPauseTime = ((SharedPreferences)localObject2).getInt("lastPauseTime", 0);
      useFingerprint = ((SharedPreferences)localObject2).getBoolean("useFingerprint", true);
      lastUpdateVersion = ((SharedPreferences)localObject2).getString("lastUpdateVersion2", "3.5");
      lastContactsSyncTime = ((SharedPreferences)localObject2).getInt("lastContactsSyncTime", (int)(System.currentTimeMillis() / 1000L) - 82800);
      lastHintsSyncTime = ((SharedPreferences)localObject2).getInt("lastHintsSyncTime", (int)(System.currentTimeMillis() / 1000L) - 90000);
      draftsLoaded = ((SharedPreferences)localObject2).getBoolean("draftsLoaded", false);
      migrateOffsetId = ((SharedPreferences)localObject2).getInt("migrateOffsetId", 0);
      if (migrateOffsetId != -1)
      {
        migrateOffsetDate = ((SharedPreferences)localObject2).getInt("migrateOffsetDate", 0);
        migrateOffsetUserId = ((SharedPreferences)localObject2).getInt("migrateOffsetUserId", 0);
        migrateOffsetChatId = ((SharedPreferences)localObject2).getInt("migrateOffsetChatId", 0);
        migrateOffsetChannelId = ((SharedPreferences)localObject2).getInt("migrateOffsetChannelId", 0);
        migrateOffsetAccess = ((SharedPreferences)localObject2).getLong("migrateOffsetAccess", 0L);
      }
      Object localObject4 = ((SharedPreferences)localObject2).getString("user", null);
      if (localObject4 != null)
      {
        localObject4 = Base64.decode((String)localObject4, 0);
        if (localObject4 != null)
        {
          localObject4 = new SerializedData((byte[])localObject4);
          currentUser = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject4, ((SerializedData)localObject4).readInt32(false), false);
          ((SerializedData)localObject4).cleanup();
        }
      }
      localObject2 = ((SharedPreferences)localObject2).getString("passcodeSalt", "");
      if (((String)localObject2).length() > 0)
      {
        passcodeSalt = Base64.decode((String)localObject2, 0);
        return;
      }
      passcodeSalt = new byte[0];
    }
  }
  
  public static void saveConfig(boolean paramBoolean)
  {
    saveConfig(paramBoolean, null);
  }
  
  public static void saveConfig(boolean paramBoolean, File paramFile)
  {
    synchronized (sync)
    {
      for (;;)
      {
        try
        {
          localEditor = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0).edit();
          localEditor.putString("token", token);
          localEditor.putString("imei", imei);
          localEditor.putBoolean("switchBackEnd", switchBackEnd.booleanValue());
          localEditor.putBoolean("registeredForPush", registeredForPush);
          localEditor.putString("pushString2", pushString);
          localEditor.putInt("lastSendMessageId", lastSendMessageId);
          localEditor.putInt("lastLocalId", lastLocalId);
          localEditor.putString("contactsHash", contactsHash);
          localEditor.putBoolean("saveIncomingPhotos", saveIncomingPhotos);
          localEditor.putInt("lastBroadcastId", lastBroadcastId);
          localEditor.putBoolean("blockedUsersLoaded", blockedUsersLoaded);
          localEditor.putString("passcodeHash1", passcodeHash);
          if (passcodeSalt.length <= 0) {
            continue;
          }
          localObject1 = Base64.encodeToString(passcodeSalt, 0);
          localEditor.putString("passcodeSalt", (String)localObject1);
          localEditor.putBoolean("appLocked", appLocked);
          localEditor.putInt("passcodeType", passcodeType);
          localEditor.putInt("autoLockIn", autoLockIn);
          localEditor.putInt("lastPauseTime", lastPauseTime);
          localEditor.putString("lastUpdateVersion2", lastUpdateVersion);
          localEditor.putInt("lastContactsSyncTime", lastContactsSyncTime);
          localEditor.putBoolean("useFingerprint", useFingerprint);
          localEditor.putInt("lastHintsSyncTime", lastHintsSyncTime);
          localEditor.putBoolean("draftsLoaded", draftsLoaded);
          localEditor.putInt("migrateOffsetId", migrateOffsetId);
          if (migrateOffsetId != -1)
          {
            localEditor.putInt("migrateOffsetDate", migrateOffsetDate);
            localEditor.putInt("migrateOffsetUserId", migrateOffsetUserId);
            localEditor.putInt("migrateOffsetChatId", migrateOffsetChatId);
            localEditor.putInt("migrateOffsetChannelId", migrateOffsetChannelId);
            localEditor.putLong("migrateOffsetAccess", migrateOffsetAccess);
          }
          if (currentUser == null) {
            continue;
          }
          if (paramBoolean)
          {
            localObject1 = new SerializedData();
            currentUser.serializeToStream((AbstractSerializedData)localObject1);
            localEditor.putString("user", Base64.encodeToString(((SerializedData)localObject1).toByteArray(), 0));
            ((SerializedData)localObject1).cleanup();
          }
          localEditor.commit();
          if (paramFile != null) {
            paramFile.delete();
          }
        }
        catch (Exception paramFile)
        {
          SharedPreferences.Editor localEditor;
          Object localObject1;
          FileLog.e("TSMS", paramFile);
          continue;
        }
        return;
        localObject1 = "";
      }
      localEditor.remove("user");
    }
  }
  
  public static void setCurrentUser(TLRPC.User paramUser)
  {
    synchronized (sync)
    {
      currentUser = paramUser;
      return;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/UserConfig.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */