package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import java.io.File;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC.TL_account_tmpPassword;
import org.telegram.tgnet.TLRPC.User;

public class UserConfig
{
  private static volatile UserConfig[] Instance = new UserConfig[3];
  public static final int MAX_ACCOUNT_COUNT = 3;
  public static int selectedAccount;
  public boolean blockedUsersLoaded;
  public int botRatingLoadTime;
  public int clientUserId;
  private boolean configLoaded;
  public boolean contactsReimported;
  public int contactsSavedCount;
  private int currentAccount;
  private TLRPC.User currentUser;
  public long dialogsLoadOffsetAccess = 0L;
  public int dialogsLoadOffsetChannelId = 0;
  public int dialogsLoadOffsetChatId = 0;
  public int dialogsLoadOffsetDate = 0;
  public int dialogsLoadOffsetId = 0;
  public int dialogsLoadOffsetUserId = 0;
  public boolean draftsLoaded;
  public int lastBroadcastId = -1;
  public int lastContactsSyncTime;
  public int lastHintsSyncTime;
  public int lastSendMessageId = -210000;
  public int loginTime;
  public long migrateOffsetAccess = -1L;
  public int migrateOffsetChannelId = -1;
  public int migrateOffsetChatId = -1;
  public int migrateOffsetDate = -1;
  public int migrateOffsetId = -1;
  public int migrateOffsetUserId = -1;
  public boolean pinnedDialogsLoaded = true;
  public int ratingLoadTime;
  public boolean registeredForPush;
  private final Object sync = new Object();
  public boolean syncContacts = true;
  public TLRPC.TL_account_tmpPassword tmpPassword;
  public int totalDialogsLoadCount = 0;
  
  public UserConfig(int paramInt)
  {
    this.currentAccount = paramInt;
  }
  
  public static int getActivatedAccountsCount()
  {
    int j = 0;
    int i = 0;
    while (i < 3)
    {
      int k = j;
      if (getInstance(i).isClientActivated()) {
        k = j + 1;
      }
      i += 1;
      j = k;
    }
    return j;
  }
  
  public static UserConfig getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new UserConfig(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (UserConfig)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (UserConfig)localObject1;
  }
  
  public void clearConfig()
  {
    this.currentUser = null;
    this.clientUserId = 0;
    this.registeredForPush = false;
    this.contactsSavedCount = 0;
    this.lastSendMessageId = -210000;
    this.lastBroadcastId = -1;
    this.blockedUsersLoaded = false;
    this.migrateOffsetId = -1;
    this.migrateOffsetDate = -1;
    this.migrateOffsetUserId = -1;
    this.migrateOffsetChatId = -1;
    this.migrateOffsetChannelId = -1;
    this.migrateOffsetAccess = -1L;
    this.dialogsLoadOffsetId = 0;
    this.totalDialogsLoadCount = 0;
    this.dialogsLoadOffsetDate = 0;
    this.dialogsLoadOffsetUserId = 0;
    this.dialogsLoadOffsetChatId = 0;
    this.dialogsLoadOffsetChannelId = 0;
    this.dialogsLoadOffsetAccess = 0L;
    this.ratingLoadTime = 0;
    this.botRatingLoadTime = 0;
    this.draftsLoaded = true;
    this.contactsReimported = true;
    this.syncContacts = true;
    this.pinnedDialogsLoaded = false;
    this.loginTime = ((int)(System.currentTimeMillis() / 1000L));
    this.lastContactsSyncTime = ((int)(System.currentTimeMillis() / 1000L) - 82800);
    this.lastHintsSyncTime = ((int)(System.currentTimeMillis() / 1000L) - 90000);
    int k = 0;
    int i = 0;
    for (;;)
    {
      int j = k;
      if (i < 3)
      {
        if (getInstance(i).isClientActivated()) {
          j = 1;
        }
      }
      else
      {
        if (j == 0) {
          SharedConfig.clearConfig();
        }
        saveConfig(true);
        return;
      }
      i += 1;
    }
  }
  
  public int getClientUserId()
  {
    for (;;)
    {
      synchronized (this.sync)
      {
        if (this.currentUser != null)
        {
          i = this.currentUser.id;
          return i;
        }
      }
      int i = 0;
    }
  }
  
  public TLRPC.User getCurrentUser()
  {
    synchronized (this.sync)
    {
      TLRPC.User localUser = this.currentUser;
      return localUser;
    }
  }
  
  public int getNewMessageId()
  {
    synchronized (this.sync)
    {
      int i = this.lastSendMessageId;
      this.lastSendMessageId -= 1;
      return i;
    }
  }
  
  public boolean isClientActivated()
  {
    for (;;)
    {
      synchronized (this.sync)
      {
        if (this.currentUser != null)
        {
          bool = true;
          return bool;
        }
      }
      boolean bool = false;
    }
  }
  
  public void loadConfig()
  {
    for (;;)
    {
      synchronized (this.sync)
      {
        if (this.configLoaded) {
          return;
        }
        if (this.currentAccount == 0)
        {
          Object localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
          selectedAccount = ((SharedPreferences)localObject1).getInt("selectedAccount", 0);
          this.registeredForPush = ((SharedPreferences)localObject1).getBoolean("registeredForPush", false);
          this.lastSendMessageId = ((SharedPreferences)localObject1).getInt("lastSendMessageId", -210000);
          this.contactsSavedCount = ((SharedPreferences)localObject1).getInt("contactsSavedCount", 0);
          this.lastBroadcastId = ((SharedPreferences)localObject1).getInt("lastBroadcastId", -1);
          this.blockedUsersLoaded = ((SharedPreferences)localObject1).getBoolean("blockedUsersLoaded", false);
          this.lastContactsSyncTime = ((SharedPreferences)localObject1).getInt("lastContactsSyncTime", (int)(System.currentTimeMillis() / 1000L) - 82800);
          this.lastHintsSyncTime = ((SharedPreferences)localObject1).getInt("lastHintsSyncTime", (int)(System.currentTimeMillis() / 1000L) - 90000);
          this.draftsLoaded = ((SharedPreferences)localObject1).getBoolean("draftsLoaded", false);
          this.pinnedDialogsLoaded = ((SharedPreferences)localObject1).getBoolean("pinnedDialogsLoaded", false);
          this.contactsReimported = ((SharedPreferences)localObject1).getBoolean("contactsReimported", false);
          this.ratingLoadTime = ((SharedPreferences)localObject1).getInt("ratingLoadTime", 0);
          this.botRatingLoadTime = ((SharedPreferences)localObject1).getInt("botRatingLoadTime", 0);
          this.loginTime = ((SharedPreferences)localObject1).getInt("loginTime", this.currentAccount);
          this.syncContacts = ((SharedPreferences)localObject1).getBoolean("syncContacts", this.syncContacts);
          this.migrateOffsetId = ((SharedPreferences)localObject1).getInt("3migrateOffsetId", 0);
          if (this.migrateOffsetId != -1)
          {
            this.migrateOffsetDate = ((SharedPreferences)localObject1).getInt("3migrateOffsetDate", 0);
            this.migrateOffsetUserId = ((SharedPreferences)localObject1).getInt("3migrateOffsetUserId", 0);
            this.migrateOffsetChatId = ((SharedPreferences)localObject1).getInt("3migrateOffsetChatId", 0);
            this.migrateOffsetChannelId = ((SharedPreferences)localObject1).getInt("3migrateOffsetChannelId", 0);
            this.migrateOffsetAccess = ((SharedPreferences)localObject1).getLong("3migrateOffsetAccess", 0L);
          }
          this.dialogsLoadOffsetId = ((SharedPreferences)localObject1).getInt("2dialogsLoadOffsetId", -1);
          this.totalDialogsLoadCount = ((SharedPreferences)localObject1).getInt("2totalDialogsLoadCount", 0);
          this.dialogsLoadOffsetDate = ((SharedPreferences)localObject1).getInt("2dialogsLoadOffsetDate", -1);
          this.dialogsLoadOffsetUserId = ((SharedPreferences)localObject1).getInt("2dialogsLoadOffsetUserId", -1);
          this.dialogsLoadOffsetChatId = ((SharedPreferences)localObject1).getInt("2dialogsLoadOffsetChatId", -1);
          this.dialogsLoadOffsetChannelId = ((SharedPreferences)localObject1).getInt("2dialogsLoadOffsetChannelId", -1);
          this.dialogsLoadOffsetAccess = ((SharedPreferences)localObject1).getLong("2dialogsLoadOffsetAccess", -1L);
          Object localObject4 = ((SharedPreferences)localObject1).getString("tmpPassword", null);
          if (localObject4 != null)
          {
            localObject4 = Base64.decode((String)localObject4, 0);
            if (localObject4 != null)
            {
              localObject4 = new SerializedData((byte[])localObject4);
              this.tmpPassword = TLRPC.TL_account_tmpPassword.TLdeserialize((AbstractSerializedData)localObject4, ((SerializedData)localObject4).readInt32(false), false);
              ((SerializedData)localObject4).cleanup();
            }
          }
          localObject1 = ((SharedPreferences)localObject1).getString("user", null);
          if (localObject1 != null)
          {
            localObject1 = Base64.decode((String)localObject1, 0);
            if (localObject1 != null)
            {
              localObject1 = new SerializedData((byte[])localObject1);
              this.currentUser = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject1, ((SerializedData)localObject1).readInt32(false), false);
              ((SerializedData)localObject1).cleanup();
            }
          }
          if (this.currentUser != null) {
            this.clientUserId = this.currentUser.id;
          }
          this.configLoaded = true;
          return;
        }
      }
      SharedPreferences localSharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfig" + this.currentAccount, 0);
    }
  }
  
  public void saveConfig(boolean paramBoolean)
  {
    saveConfig(paramBoolean, null);
  }
  
  public void saveConfig(boolean paramBoolean, File paramFile)
  {
    for (;;)
    {
      Object localObject1;
      synchronized (this.sync)
      {
        try
        {
          if (this.currentAccount != 0) {
            continue;
          }
          localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", 0);
          localObject1 = ((SharedPreferences)localObject1).edit();
          if (this.currentAccount == 0) {
            ((SharedPreferences.Editor)localObject1).putInt("selectedAccount", selectedAccount);
          }
          ((SharedPreferences.Editor)localObject1).putBoolean("registeredForPush", this.registeredForPush);
          ((SharedPreferences.Editor)localObject1).putInt("lastSendMessageId", this.lastSendMessageId);
          ((SharedPreferences.Editor)localObject1).putInt("contactsSavedCount", this.contactsSavedCount);
          ((SharedPreferences.Editor)localObject1).putInt("lastBroadcastId", this.lastBroadcastId);
          ((SharedPreferences.Editor)localObject1).putBoolean("blockedUsersLoaded", this.blockedUsersLoaded);
          ((SharedPreferences.Editor)localObject1).putInt("lastContactsSyncTime", this.lastContactsSyncTime);
          ((SharedPreferences.Editor)localObject1).putInt("lastHintsSyncTime", this.lastHintsSyncTime);
          ((SharedPreferences.Editor)localObject1).putBoolean("draftsLoaded", this.draftsLoaded);
          ((SharedPreferences.Editor)localObject1).putBoolean("pinnedDialogsLoaded", this.pinnedDialogsLoaded);
          ((SharedPreferences.Editor)localObject1).putInt("ratingLoadTime", this.ratingLoadTime);
          ((SharedPreferences.Editor)localObject1).putInt("botRatingLoadTime", this.botRatingLoadTime);
          ((SharedPreferences.Editor)localObject1).putBoolean("contactsReimported", this.contactsReimported);
          ((SharedPreferences.Editor)localObject1).putInt("loginTime", this.loginTime);
          ((SharedPreferences.Editor)localObject1).putBoolean("syncContacts", this.syncContacts);
          ((SharedPreferences.Editor)localObject1).putInt("3migrateOffsetId", this.migrateOffsetId);
          if (this.migrateOffsetId != -1)
          {
            ((SharedPreferences.Editor)localObject1).putInt("3migrateOffsetDate", this.migrateOffsetDate);
            ((SharedPreferences.Editor)localObject1).putInt("3migrateOffsetUserId", this.migrateOffsetUserId);
            ((SharedPreferences.Editor)localObject1).putInt("3migrateOffsetChatId", this.migrateOffsetChatId);
            ((SharedPreferences.Editor)localObject1).putInt("3migrateOffsetChannelId", this.migrateOffsetChannelId);
            ((SharedPreferences.Editor)localObject1).putLong("3migrateOffsetAccess", this.migrateOffsetAccess);
          }
          ((SharedPreferences.Editor)localObject1).putInt("2totalDialogsLoadCount", this.totalDialogsLoadCount);
          ((SharedPreferences.Editor)localObject1).putInt("2dialogsLoadOffsetId", this.dialogsLoadOffsetId);
          ((SharedPreferences.Editor)localObject1).putInt("2dialogsLoadOffsetDate", this.dialogsLoadOffsetDate);
          ((SharedPreferences.Editor)localObject1).putInt("2dialogsLoadOffsetUserId", this.dialogsLoadOffsetUserId);
          ((SharedPreferences.Editor)localObject1).putInt("2dialogsLoadOffsetChatId", this.dialogsLoadOffsetChatId);
          ((SharedPreferences.Editor)localObject1).putInt("2dialogsLoadOffsetChannelId", this.dialogsLoadOffsetChannelId);
          ((SharedPreferences.Editor)localObject1).putLong("2dialogsLoadOffsetAccess", this.dialogsLoadOffsetAccess);
          SharedConfig.saveConfig();
          if (this.tmpPassword == null) {
            continue;
          }
          SerializedData localSerializedData = new SerializedData();
          this.tmpPassword.serializeToStream(localSerializedData);
          ((SharedPreferences.Editor)localObject1).putString("tmpPassword", Base64.encodeToString(localSerializedData.toByteArray(), 0));
          localSerializedData.cleanup();
          if (this.currentUser == null) {
            break label595;
          }
          if (paramBoolean)
          {
            localSerializedData = new SerializedData();
            this.currentUser.serializeToStream(localSerializedData);
            ((SharedPreferences.Editor)localObject1).putString("user", Base64.encodeToString(localSerializedData.toByteArray(), 0));
            localSerializedData.cleanup();
          }
          ((SharedPreferences.Editor)localObject1).commit();
          if (paramFile != null) {
            paramFile.delete();
          }
        }
        catch (Exception paramFile)
        {
          FileLog.e(paramFile);
          continue;
        }
        return;
        localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("userconfig" + this.currentAccount, 0);
        continue;
        ((SharedPreferences.Editor)localObject1).remove("tmpPassword");
      }
      label595:
      ((SharedPreferences.Editor)localObject1).remove("user");
    }
  }
  
  public void setCurrentUser(TLRPC.User paramUser)
  {
    synchronized (this.sync)
    {
      this.currentUser = paramUser;
      this.clientUserId = paramUser.id;
      return;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/UserConfig.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */