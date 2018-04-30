package ir.eitaa.messenger.browser;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MediaController;
import ir.eitaa.messenger.ShareBroadcastReceiver;
import ir.eitaa.messenger.support.customtabs.CustomTabsCallback;
import ir.eitaa.messenger.support.customtabs.CustomTabsClient;
import ir.eitaa.messenger.support.customtabs.CustomTabsIntent;
import ir.eitaa.messenger.support.customtabs.CustomTabsIntent.Builder;
import ir.eitaa.messenger.support.customtabs.CustomTabsServiceConnection;
import ir.eitaa.messenger.support.customtabs.CustomTabsSession;
import ir.eitaa.messenger.support.customtabsclient.shared.CustomTabsHelper;
import ir.eitaa.messenger.support.customtabsclient.shared.ServiceConnection;
import ir.eitaa.messenger.support.customtabsclient.shared.ServiceConnectionCallback;
import ir.eitaa.ui.LaunchActivity;
import java.lang.ref.WeakReference;

public class Browser
{
  private static WeakReference<Activity> currentCustomTabsActivity;
  private static CustomTabsClient customTabsClient;
  private static WeakReference<CustomTabsSession> customTabsCurrentSession;
  private static String customTabsPackageToBind;
  private static CustomTabsServiceConnection customTabsServiceConnection;
  private static CustomTabsSession customTabsSession;
  
  public static void bindCustomTabsService(Activity paramActivity)
  {
    Activity localActivity = null;
    if (Build.VERSION.SDK_INT < 15) {
      return;
    }
    if (currentCustomTabsActivity == null) {}
    for (;;)
    {
      if ((localActivity != null) && (localActivity != paramActivity)) {
        unbindCustomTabsService(localActivity);
      }
      if (customTabsClient != null) {
        break;
      }
      currentCustomTabsActivity = new WeakReference(paramActivity);
      try
      {
        if (TextUtils.isEmpty(customTabsPackageToBind))
        {
          customTabsPackageToBind = CustomTabsHelper.getPackageNameToUse(paramActivity);
          if (customTabsPackageToBind == null) {
            break;
          }
        }
        customTabsServiceConnection = new ServiceConnection(new ServiceConnectionCallback()
        {
          public void onServiceConnected(CustomTabsClient paramAnonymousCustomTabsClient)
          {
            Browser.access$102(paramAnonymousCustomTabsClient);
            if ((MediaController.getInstance().canCustomTabs()) && (Browser.customTabsClient != null)) {}
            try
            {
              Browser.customTabsClient.warmup(0L);
              return;
            }
            catch (Exception paramAnonymousCustomTabsClient)
            {
              FileLog.e("TSMS", paramAnonymousCustomTabsClient);
            }
          }
          
          public void onServiceDisconnected()
          {
            Browser.access$102(null);
          }
        });
        if (CustomTabsClient.bindCustomTabsService(paramActivity, customTabsPackageToBind, customTabsServiceConnection)) {
          break;
        }
        customTabsServiceConnection = null;
        return;
      }
      catch (Exception paramActivity)
      {
        FileLog.e("TSMS", paramActivity);
        return;
      }
      localActivity = (Activity)currentCustomTabsActivity.get();
    }
  }
  
  private static CustomTabsSession getCurrentSession()
  {
    if (customTabsCurrentSession == null) {
      return null;
    }
    return (CustomTabsSession)customTabsCurrentSession.get();
  }
  
  private static CustomTabsSession getSession()
  {
    if (customTabsClient == null) {
      customTabsSession = null;
    }
    for (;;)
    {
      return customTabsSession;
      if (customTabsSession == null)
      {
        customTabsSession = customTabsClient.newSession(new NavigationCallback(null));
        setCurrentSession(customTabsSession);
      }
    }
  }
  
  public static boolean isInternalUri(Uri paramUri)
  {
    String str = paramUri.getHost();
    if (str != null) {}
    for (str = str.toLowerCase(); ("tg".equals(paramUri.getScheme())) || ("eitaa.com".equals(str)) || ("eitaa.net".equals(str)) || ("eitaa.info".equals(str)) || ("eitaa.ir".equals(str)); str = "") {
      return true;
    }
    return false;
  }
  
  public static boolean isInternalUrl(String paramString)
  {
    return isInternalUri(Uri.parse(paramString));
  }
  
  public static void openUrl(Context paramContext, Uri paramUri)
  {
    openUrl(paramContext, paramUri, true);
  }
  
  public static void openUrl(Context paramContext, Uri paramUri, boolean paramBoolean)
  {
    if ((paramContext == null) || (paramUri == null)) {
      return;
    }
    boolean bool;
    for (;;)
    {
      try
      {
        if (paramUri.getScheme() != null)
        {
          localObject = paramUri.getScheme().toLowerCase();
          bool = isInternalUri(paramUri);
          if ((Build.VERSION.SDK_INT < 15) || (!paramBoolean) || (!MediaController.getInstance().canCustomTabs()) || (bool) || (((String)localObject).equals("tel"))) {
            break;
          }
          localObject = new Intent(ApplicationLoader.applicationContext, ShareBroadcastReceiver.class);
          ((Intent)localObject).setAction("android.intent.action.SEND");
          CustomTabsIntent.Builder localBuilder = new CustomTabsIntent.Builder(getSession());
          localBuilder.setToolbarColor(-1674199);
          localBuilder.setShowTitle(true);
          localBuilder.setActionButton(BitmapFactory.decodeResource(paramContext.getResources(), 2130837505), LocaleController.getString("ShareFile", 2131166277), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, (Intent)localObject, 0), false);
          localBuilder.build().launchUrl((Activity)paramContext, paramUri);
          return;
        }
      }
      catch (Exception paramContext)
      {
        FileLog.e("TSMS", paramContext);
        return;
      }
      Object localObject = "";
    }
    paramUri = new Intent("android.intent.action.VIEW", paramUri);
    if (bool) {
      paramUri.setComponent(new ComponentName(paramContext.getPackageName(), LaunchActivity.class.getName()));
    }
    paramUri.putExtra("com.android.browser.application_id", paramContext.getPackageName());
    paramContext.startActivity(paramUri);
  }
  
  public static void openUrl(Context paramContext, String paramString)
  {
    if (paramString == null) {
      return;
    }
    openUrl(paramContext, Uri.parse(paramString), true);
  }
  
  public static void openUrl(Context paramContext, String paramString, boolean paramBoolean)
  {
    if ((paramContext == null) || (paramString == null)) {
      return;
    }
    openUrl(paramContext, Uri.parse(paramString), paramBoolean);
  }
  
  private static void setCurrentSession(CustomTabsSession paramCustomTabsSession)
  {
    customTabsCurrentSession = new WeakReference(paramCustomTabsSession);
  }
  
  public static void unbindCustomTabsService(Activity paramActivity)
  {
    if ((Build.VERSION.SDK_INT < 15) || (customTabsServiceConnection == null)) {
      return;
    }
    Activity localActivity;
    if (currentCustomTabsActivity == null) {
      localActivity = null;
    }
    for (;;)
    {
      if (localActivity == paramActivity) {
        currentCustomTabsActivity.clear();
      }
      try
      {
        paramActivity.unbindService(customTabsServiceConnection);
        customTabsClient = null;
        customTabsSession = null;
        return;
        localActivity = (Activity)currentCustomTabsActivity.get();
      }
      catch (Exception paramActivity)
      {
        for (;;)
        {
          FileLog.e("TSMS", paramActivity);
        }
      }
    }
  }
  
  private static class NavigationCallback
    extends CustomTabsCallback
  {
    public void onNavigationEvent(int paramInt, Bundle paramBundle)
    {
      FileLog.e("TSMS", "code = " + paramInt + " extras " + paramBundle);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/browser/Browser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */