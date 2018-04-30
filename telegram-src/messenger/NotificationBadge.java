package org.telegram.messenger;

import android.annotation.TargetApi;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NotificationBadge
{
  private static final List<Class<? extends Badger>> BADGERS = new LinkedList();
  private static Badger badger;
  private static ComponentName componentName;
  private static boolean initied;
  
  static
  {
    BADGERS.add(AdwHomeBadger.class);
    BADGERS.add(ApexHomeBadger.class);
    BADGERS.add(NewHtcHomeBadger.class);
    BADGERS.add(NovaHomeBadger.class);
    BADGERS.add(SonyHomeBadger.class);
    BADGERS.add(XiaomiHomeBadger.class);
    BADGERS.add(AsusHomeBadger.class);
    BADGERS.add(HuaweiHomeBadger.class);
    BADGERS.add(OPPOHomeBader.class);
    BADGERS.add(SamsungHomeBadger.class);
    BADGERS.add(ZukHomeBadger.class);
    BADGERS.add(VivoHomeBadger.class);
  }
  
  public static boolean applyCount(int paramInt)
  {
    try
    {
      if ((badger == null) && (!initied))
      {
        initBadger();
        initied = true;
      }
      if (badger == null) {
        return false;
      }
      badger.executeBadge(paramInt);
      return true;
    }
    catch (Throwable localThrowable) {}
    return false;
  }
  
  private static boolean canResolveBroadcast(Intent paramIntent)
  {
    boolean bool2 = false;
    paramIntent = ApplicationLoader.applicationContext.getPackageManager().queryBroadcastReceivers(paramIntent, 0);
    boolean bool1 = bool2;
    if (paramIntent != null)
    {
      bool1 = bool2;
      if (paramIntent.size() > 0) {
        bool1 = true;
      }
    }
    return bool1;
  }
  
  public static void close(Cursor paramCursor)
  {
    if ((paramCursor != null) && (!paramCursor.isClosed())) {
      paramCursor.close();
    }
  }
  
  public static void closeQuietly(Closeable paramCloseable)
  {
    if (paramCloseable != null) {}
    try
    {
      paramCloseable.close();
      return;
    }
    catch (Throwable paramCloseable) {}
  }
  
  private static boolean initBadger()
  {
    Object localObject3 = ApplicationLoader.applicationContext;
    Object localObject1 = ((Context)localObject3).getPackageManager().getLaunchIntentForPackage(((Context)localObject3).getPackageName());
    if (localObject1 == null) {
      return false;
    }
    componentName = ((Intent)localObject1).getComponent();
    Object localObject4 = new Intent("android.intent.action.MAIN");
    ((Intent)localObject4).addCategory("android.intent.category.HOME");
    localObject1 = ((Context)localObject3).getPackageManager().resolveActivity((Intent)localObject4, 65536);
    Object localObject5;
    Object localObject2;
    if (localObject1 != null)
    {
      localObject5 = ((ResolveInfo)localObject1).activityInfo.packageName;
      Iterator localIterator = BADGERS.iterator();
      while (localIterator.hasNext())
      {
        localObject2 = (Class)localIterator.next();
        localObject1 = null;
        try
        {
          localObject2 = (Badger)((Class)localObject2).newInstance();
          localObject1 = localObject2;
        }
        catch (Exception localException2)
        {
          int i;
          for (;;) {}
        }
        if ((localObject1 != null) && (((Badger)localObject1).getSupportLaunchers().contains(localObject5))) {
          badger = (Badger)localObject1;
        }
      }
      if (badger != null) {
        return true;
      }
    }
    localObject3 = ((Context)localObject3).getPackageManager().queryIntentActivities((Intent)localObject4, 65536);
    if (localObject3 != null)
    {
      i = 0;
      if (i < ((List)localObject3).size())
      {
        localObject4 = ((ResolveInfo)((List)localObject3).get(i)).activityInfo.packageName;
        localObject5 = BADGERS.iterator();
        while (((Iterator)localObject5).hasNext())
        {
          localObject2 = (Class)((Iterator)localObject5).next();
          localObject1 = null;
          try
          {
            localObject2 = (Badger)((Class)localObject2).newInstance();
            localObject1 = localObject2;
          }
          catch (Exception localException1)
          {
            for (;;) {}
          }
          if ((localObject1 != null) && (((Badger)localObject1).getSupportLaunchers().contains(localObject4))) {
            badger = (Badger)localObject1;
          }
        }
        if (badger == null) {
          break label296;
        }
      }
    }
    if (badger == null)
    {
      if (!Build.MANUFACTURER.equalsIgnoreCase("Xiaomi")) {
        break label303;
      }
      badger = new XiaomiHomeBadger();
    }
    for (;;)
    {
      return true;
      label296:
      i += 1;
      break;
      label303:
      if (Build.MANUFACTURER.equalsIgnoreCase("ZUK")) {
        badger = new ZukHomeBadger();
      } else if (Build.MANUFACTURER.equalsIgnoreCase("OPPO")) {
        badger = new OPPOHomeBader();
      } else if (Build.MANUFACTURER.equalsIgnoreCase("VIVO")) {
        badger = new VivoHomeBadger();
      } else {
        badger = new DefaultBadger();
      }
    }
  }
  
  public static class AdwHomeBadger
    implements NotificationBadge.Badger
  {
    public static final String CLASSNAME = "CNAME";
    public static final String COUNT = "COUNT";
    public static final String INTENT_UPDATE_COUNTER = "org.adw.launcher.counter.SEND";
    public static final String PACKAGENAME = "PNAME";
    
    public void executeBadge(int paramInt)
    {
      final Intent localIntent = new Intent("org.adw.launcher.counter.SEND");
      localIntent.putExtra("PNAME", NotificationBadge.componentName.getPackageName());
      localIntent.putExtra("CNAME", NotificationBadge.componentName.getClassName());
      localIntent.putExtra("COUNT", paramInt);
      if (NotificationBadge.canResolveBroadcast(localIntent)) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ApplicationLoader.applicationContext.sendBroadcast(localIntent);
          }
        });
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "org.adw.launcher", "org.adwfreak.launcher" });
    }
  }
  
  public static class ApexHomeBadger
    implements NotificationBadge.Badger
  {
    private static final String CLASS = "class";
    private static final String COUNT = "count";
    private static final String INTENT_UPDATE_COUNTER = "com.anddoes.launcher.COUNTER_CHANGED";
    private static final String PACKAGENAME = "package";
    
    public void executeBadge(int paramInt)
    {
      final Intent localIntent = new Intent("com.anddoes.launcher.COUNTER_CHANGED");
      localIntent.putExtra("package", NotificationBadge.componentName.getPackageName());
      localIntent.putExtra("count", paramInt);
      localIntent.putExtra("class", NotificationBadge.componentName.getClassName());
      if (NotificationBadge.canResolveBroadcast(localIntent)) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ApplicationLoader.applicationContext.sendBroadcast(localIntent);
          }
        });
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.anddoes.launcher" });
    }
  }
  
  public static class AsusHomeBadger
    implements NotificationBadge.Badger
  {
    private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";
    private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    
    public void executeBadge(int paramInt)
    {
      final Intent localIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
      localIntent.putExtra("badge_count", paramInt);
      localIntent.putExtra("badge_count_package_name", NotificationBadge.componentName.getPackageName());
      localIntent.putExtra("badge_count_class_name", NotificationBadge.componentName.getClassName());
      localIntent.putExtra("badge_vip_count", 0);
      if (NotificationBadge.canResolveBroadcast(localIntent)) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ApplicationLoader.applicationContext.sendBroadcast(localIntent);
          }
        });
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.asus.launcher" });
    }
  }
  
  public static abstract interface Badger
  {
    public abstract void executeBadge(int paramInt);
    
    public abstract List<String> getSupportLaunchers();
  }
  
  public static class DefaultBadger
    implements NotificationBadge.Badger
  {
    private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";
    private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    
    public void executeBadge(int paramInt)
    {
      final Intent localIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
      localIntent.putExtra("badge_count", paramInt);
      localIntent.putExtra("badge_count_package_name", NotificationBadge.componentName.getPackageName());
      localIntent.putExtra("badge_count_class_name", NotificationBadge.componentName.getClassName());
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          try
          {
            ApplicationLoader.applicationContext.sendBroadcast(localIntent);
            return;
          }
          catch (Exception localException) {}
        }
      });
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "fr.neamar.kiss", "com.quaap.launchtime", "com.quaap.launchtime_official" });
    }
  }
  
  public static class HuaweiHomeBadger
    implements NotificationBadge.Badger
  {
    public void executeBadge(int paramInt)
    {
      final Bundle localBundle = new Bundle();
      localBundle.putString("package", ApplicationLoader.applicationContext.getPackageName());
      localBundle.putString("class", NotificationBadge.componentName.getClassName());
      localBundle.putInt("badgenumber", paramInt);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          try
          {
            ApplicationLoader.applicationContext.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, localBundle);
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      });
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.huawei.android.launcher" });
    }
  }
  
  public static class NewHtcHomeBadger
    implements NotificationBadge.Badger
  {
    public static final String COUNT = "count";
    public static final String EXTRA_COMPONENT = "com.htc.launcher.extra.COMPONENT";
    public static final String EXTRA_COUNT = "com.htc.launcher.extra.COUNT";
    public static final String INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION";
    public static final String INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT";
    public static final String PACKAGENAME = "packagename";
    
    public void executeBadge(int paramInt)
    {
      final Intent localIntent1 = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
      localIntent1.putExtra("com.htc.launcher.extra.COMPONENT", NotificationBadge.componentName.flattenToShortString());
      localIntent1.putExtra("com.htc.launcher.extra.COUNT", paramInt);
      final Intent localIntent2 = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
      localIntent2.putExtra("packagename", NotificationBadge.componentName.getPackageName());
      localIntent2.putExtra("count", paramInt);
      if ((NotificationBadge.canResolveBroadcast(localIntent1)) || (NotificationBadge.canResolveBroadcast(localIntent2))) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ApplicationLoader.applicationContext.sendBroadcast(localIntent1);
            ApplicationLoader.applicationContext.sendBroadcast(localIntent2);
          }
        });
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.htc.launcher" });
    }
  }
  
  public static class NovaHomeBadger
    implements NotificationBadge.Badger
  {
    private static final String CONTENT_URI = "content://com.teslacoilsw.notifier/unread_count";
    private static final String COUNT = "count";
    private static final String TAG = "tag";
    
    public void executeBadge(int paramInt)
    {
      ContentValues localContentValues = new ContentValues();
      localContentValues.put("tag", NotificationBadge.componentName.getPackageName() + "/" + NotificationBadge.componentName.getClassName());
      localContentValues.put("count", Integer.valueOf(paramInt));
      ApplicationLoader.applicationContext.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"), localContentValues);
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.teslacoilsw.launcher" });
    }
  }
  
  public static class OPPOHomeBader
    implements NotificationBadge.Badger
  {
    private static final String INTENT_ACTION = "com.oppo.unsettledevent";
    private static final String INTENT_EXTRA_BADGEUPGRADE_COUNT = "app_badge_count";
    private static final String INTENT_EXTRA_BADGE_COUNT = "number";
    private static final String INTENT_EXTRA_BADGE_UPGRADENUMBER = "upgradeNumber";
    private static final String INTENT_EXTRA_PACKAGENAME = "pakeageName";
    private static final String PROVIDER_CONTENT_URI = "content://com.android.badge/badge";
    private int mCurrentTotalCount = -1;
    
    @TargetApi(11)
    private void executeBadgeByContentProvider(int paramInt)
    {
      try
      {
        Bundle localBundle = new Bundle();
        localBundle.putInt("app_badge_count", paramInt);
        ApplicationLoader.applicationContext.getContentResolver().call(Uri.parse("content://com.android.badge/badge"), "setAppBadgeCount", null, localBundle);
        return;
      }
      catch (Throwable localThrowable) {}
    }
    
    public void executeBadge(int paramInt)
    {
      if (this.mCurrentTotalCount == paramInt) {
        return;
      }
      this.mCurrentTotalCount = paramInt;
      executeBadgeByContentProvider(paramInt);
    }
    
    public List<String> getSupportLaunchers()
    {
      return Collections.singletonList("com.oppo.launcher");
    }
  }
  
  public static class SamsungHomeBadger
    implements NotificationBadge.Badger
  {
    private static final String[] CONTENT_PROJECTION = { "_id", "class" };
    private static final String CONTENT_URI = "content://com.sec.badge/apps?notify=true";
    private static NotificationBadge.DefaultBadger defaultBadger;
    
    private ContentValues getContentValues(ComponentName paramComponentName, int paramInt, boolean paramBoolean)
    {
      ContentValues localContentValues = new ContentValues();
      if (paramBoolean)
      {
        localContentValues.put("package", paramComponentName.getPackageName());
        localContentValues.put("class", paramComponentName.getClassName());
      }
      localContentValues.put("badgecount", Integer.valueOf(paramInt));
      return localContentValues;
    }
    
    public void executeBadge(int paramInt)
    {
      try
      {
        if (defaultBadger == null) {
          defaultBadger = new NotificationBadge.DefaultBadger();
        }
        defaultBadger.executeBadge(paramInt);
      }
      catch (Exception localException)
      {
        Uri localUri;
        ContentResolver localContentResolver;
        Object localObject1;
        for (;;) {}
      }
      localUri = Uri.parse("content://com.sec.badge/apps?notify=true");
      localContentResolver = ApplicationLoader.applicationContext.getContentResolver();
      localObject1 = null;
      try
      {
        Cursor localCursor = localContentResolver.query(localUri, CONTENT_PROJECTION, "package=?", new String[] { NotificationBadge.componentName.getPackageName() }, null);
        if (localCursor != null)
        {
          localObject1 = localCursor;
          String str = NotificationBadge.componentName.getClassName();
          int i = 0;
          for (;;)
          {
            localObject1 = localCursor;
            if (!localCursor.moveToNext()) {
              break;
            }
            localObject1 = localCursor;
            int j = localCursor.getInt(0);
            localObject1 = localCursor;
            localContentResolver.update(localUri, getContentValues(NotificationBadge.componentName, paramInt, false), "_id=?", new String[] { String.valueOf(j) });
            localObject1 = localCursor;
            if (str.equals(localCursor.getString(localCursor.getColumnIndex("class")))) {
              i = 1;
            }
          }
          if (i == 0)
          {
            localObject1 = localCursor;
            localContentResolver.insert(localUri, getContentValues(NotificationBadge.componentName, paramInt, true));
          }
        }
        NotificationBadge.close(localCursor);
        return;
      }
      finally
      {
        NotificationBadge.close((Cursor)localObject1);
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.sec.android.app.launcher", "com.sec.android.app.twlauncher" });
    }
  }
  
  public static class SonyHomeBadger
    implements NotificationBadge.Badger
  {
    private static final String INTENT_ACTION = "com.sonyericsson.home.action.UPDATE_BADGE";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME";
    private static final String INTENT_EXTRA_MESSAGE = "com.sonyericsson.home.intent.extra.badge.MESSAGE";
    private static final String INTENT_EXTRA_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME";
    private static final String INTENT_EXTRA_SHOW_MESSAGE = "com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE";
    private static final String PROVIDER_COLUMNS_ACTIVITY_NAME = "activity_name";
    private static final String PROVIDER_COLUMNS_BADGE_COUNT = "badge_count";
    private static final String PROVIDER_COLUMNS_PACKAGE_NAME = "package_name";
    private static final String PROVIDER_CONTENT_URI = "content://com.sonymobile.home.resourceprovider/badge";
    private static final String SONY_HOME_PROVIDER_NAME = "com.sonymobile.home.resourceprovider";
    private static AsyncQueryHandler mQueryHandler;
    private final Uri BADGE_CONTENT_URI = Uri.parse("content://com.sonymobile.home.resourceprovider/badge");
    
    private static void executeBadgeByBroadcast(int paramInt)
    {
      Intent localIntent = new Intent("com.sonyericsson.home.action.UPDATE_BADGE");
      localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", NotificationBadge.componentName.getPackageName());
      localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", NotificationBadge.componentName.getClassName());
      localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(paramInt));
      if (paramInt > 0) {}
      for (boolean bool = true;; bool = false)
      {
        localIntent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", bool);
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ApplicationLoader.applicationContext.sendBroadcast(this.val$intent);
          }
        });
        return;
      }
    }
    
    private void executeBadgeByContentProvider(int paramInt)
    {
      if (paramInt < 0) {
        return;
      }
      if (mQueryHandler == null) {
        mQueryHandler = new AsyncQueryHandler(ApplicationLoader.applicationContext.getApplicationContext().getContentResolver())
        {
          public void handleMessage(Message paramAnonymousMessage)
          {
            try
            {
              super.handleMessage(paramAnonymousMessage);
              return;
            }
            catch (Throwable paramAnonymousMessage) {}
          }
        };
      }
      insertBadgeAsync(paramInt, NotificationBadge.componentName.getPackageName(), NotificationBadge.componentName.getClassName());
    }
    
    private void insertBadgeAsync(int paramInt, String paramString1, String paramString2)
    {
      ContentValues localContentValues = new ContentValues();
      localContentValues.put("badge_count", Integer.valueOf(paramInt));
      localContentValues.put("package_name", paramString1);
      localContentValues.put("activity_name", paramString2);
      mQueryHandler.startInsert(0, null, this.BADGE_CONTENT_URI, localContentValues);
    }
    
    private static boolean sonyBadgeContentProviderExists()
    {
      boolean bool = false;
      if (ApplicationLoader.applicationContext.getPackageManager().resolveContentProvider("com.sonymobile.home.resourceprovider", 0) != null) {
        bool = true;
      }
      return bool;
    }
    
    public void executeBadge(int paramInt)
    {
      if (sonyBadgeContentProviderExists())
      {
        executeBadgeByContentProvider(paramInt);
        return;
      }
      executeBadgeByBroadcast(paramInt);
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.sonyericsson.home", "com.sonymobile.home" });
    }
  }
  
  public static class VivoHomeBadger
    implements NotificationBadge.Badger
  {
    public void executeBadge(int paramInt)
    {
      Intent localIntent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
      localIntent.putExtra("packageName", ApplicationLoader.applicationContext.getPackageName());
      localIntent.putExtra("className", NotificationBadge.componentName.getClassName());
      localIntent.putExtra("notificationNum", paramInt);
      ApplicationLoader.applicationContext.sendBroadcast(localIntent);
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.vivo.launcher" });
    }
  }
  
  public static class XiaomiHomeBadger
    implements NotificationBadge.Badger
  {
    public static final String EXTRA_UPDATE_APP_COMPONENT_NAME = "android.intent.extra.update_application_component_name";
    public static final String EXTRA_UPDATE_APP_MSG_TEXT = "android.intent.extra.update_application_message_text";
    public static final String INTENT_ACTION = "android.intent.action.APPLICATION_MESSAGE_UPDATE";
    
    public void executeBadge(int paramInt)
    {
      try
      {
        final Object localObject3 = Class.forName("android.app.MiuiNotification").newInstance();
        Field localField = localObject3.getClass().getDeclaredField("messageCount");
        localField.setAccessible(true);
        if (paramInt == 0) {}
        for (Object localObject1 = "";; localObject1 = Integer.valueOf(paramInt))
        {
          localField.set(localObject3, String.valueOf(localObject1));
          return;
        }
        Object localObject2;
        return;
      }
      catch (Throwable localThrowable)
      {
        localObject3 = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
        ((Intent)localObject3).putExtra("android.intent.extra.update_application_component_name", NotificationBadge.componentName.getPackageName() + "/" + NotificationBadge.componentName.getClassName());
        if (paramInt == 0) {}
        for (localObject2 = "";; localObject2 = Integer.valueOf(paramInt))
        {
          ((Intent)localObject3).putExtra("android.intent.extra.update_application_message_text", String.valueOf(localObject2));
          if (!NotificationBadge.canResolveBroadcast((Intent)localObject3)) {
            break;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              ApplicationLoader.applicationContext.sendBroadcast(localObject3);
            }
          });
          return;
        }
      }
    }
    
    public List<String> getSupportLaunchers()
    {
      return Arrays.asList(new String[] { "com.miui.miuilite", "com.miui.home", "com.miui.miuihome", "com.miui.miuihome2", "com.miui.mihome", "com.miui.mihome2" });
    }
  }
  
  public static class ZukHomeBadger
    implements NotificationBadge.Badger
  {
    private final Uri CONTENT_URI = Uri.parse("content://com.android.badge/badge");
    
    @TargetApi(11)
    public void executeBadge(int paramInt)
    {
      final Bundle localBundle = new Bundle();
      localBundle.putInt("app_badge_count", paramInt);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          try
          {
            ApplicationLoader.applicationContext.getContentResolver().call(NotificationBadge.ZukHomeBadger.this.CONTENT_URI, "setAppBadgeCount", null, localBundle);
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      });
    }
    
    public List<String> getSupportLaunchers()
    {
      return Collections.singletonList("com.zui.launcher");
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/NotificationBadge.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */