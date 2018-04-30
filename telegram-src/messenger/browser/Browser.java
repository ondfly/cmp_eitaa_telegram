package org.telegram.messenger.browser;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import java.lang.ref.WeakReference;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.CustomTabsCopyReceiver;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.ShareBroadcastReceiver;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.customtabs.CustomTabsCallback;
import org.telegram.messenger.support.customtabs.CustomTabsClient;
import org.telegram.messenger.support.customtabs.CustomTabsIntent;
import org.telegram.messenger.support.customtabs.CustomTabsIntent.Builder;
import org.telegram.messenger.support.customtabs.CustomTabsServiceConnection;
import org.telegram.messenger.support.customtabs.CustomTabsSession;
import org.telegram.messenger.support.customtabsclient.shared.CustomTabsHelper;
import org.telegram.messenger.support.customtabsclient.shared.ServiceConnection;
import org.telegram.messenger.support.customtabsclient.shared.ServiceConnectionCallback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messages_getWebPagePreview;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

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
    if (currentCustomTabsActivity == null)
    {
      if ((localActivity != null) && (localActivity != paramActivity)) {
        unbindCustomTabsService(localActivity);
      }
      if (customTabsClient == null) {
        break label41;
      }
    }
    for (;;)
    {
      return;
      localActivity = (Activity)currentCustomTabsActivity.get();
      break;
      label41:
      currentCustomTabsActivity = new WeakReference(paramActivity);
      try
      {
        if (TextUtils.isEmpty(customTabsPackageToBind))
        {
          customTabsPackageToBind = CustomTabsHelper.getPackageNameToUse(paramActivity);
          if (customTabsPackageToBind == null) {}
        }
        else
        {
          customTabsServiceConnection = new ServiceConnection(new ServiceConnectionCallback()
          {
            public void onServiceConnected(CustomTabsClient paramAnonymousCustomTabsClient)
            {
              Browser.access$102(paramAnonymousCustomTabsClient);
              if ((SharedConfig.customTabs) && (Browser.customTabsClient != null)) {}
              try
              {
                Browser.customTabsClient.warmup(0L);
                return;
              }
              catch (Exception paramAnonymousCustomTabsClient)
              {
                FileLog.e(paramAnonymousCustomTabsClient);
              }
            }
            
            public void onServiceDisconnected()
            {
              Browser.access$102(null);
            }
          });
          if (!CustomTabsClient.bindCustomTabsService(paramActivity, customTabsPackageToBind, customTabsServiceConnection))
          {
            customTabsServiceConnection = null;
            return;
          }
        }
      }
      catch (Exception paramActivity)
      {
        FileLog.e(paramActivity);
      }
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
  
  public static boolean isInternalUri(Uri paramUri, boolean[] paramArrayOfBoolean)
  {
    String str = paramUri.getHost();
    if (str != null)
    {
      str = str.toLowerCase();
      if (!"tg".equals(paramUri.getScheme())) {
        break label34;
      }
    }
    label34:
    label115:
    do
    {
      do
      {
        return true;
        str = "";
        break;
        if (!"telegram.dog".equals(str)) {
          break label115;
        }
        paramUri = paramUri.getPath();
        if ((paramUri == null) || (paramUri.length() <= 1)) {
          break label185;
        }
        paramUri = paramUri.substring(1).toLowerCase();
      } while ((!paramUri.startsWith("blog")) && (!paramUri.equals("iv")) && (!paramUri.startsWith("faq")) && (!paramUri.equals("apps")));
      if (paramArrayOfBoolean != null) {
        paramArrayOfBoolean[0] = true;
      }
      return false;
      if ((!"telegram.me".equals(str)) && (!"t.me".equals(str)) && (!"telesco.pe".equals(str))) {
        break label185;
      }
      paramUri = paramUri.getPath();
      if ((paramUri == null) || (paramUri.length() <= 1)) {
        break label185;
      }
    } while (!paramUri.substring(1).toLowerCase().equals("iv"));
    if (paramArrayOfBoolean != null) {
      paramArrayOfBoolean[0] = true;
    }
    return false;
    label185:
    return false;
  }
  
  public static boolean isInternalUrl(String paramString, boolean[] paramArrayOfBoolean)
  {
    return isInternalUri(Uri.parse(paramString), paramArrayOfBoolean);
  }
  
  public static void openUrl(Context paramContext, Uri paramUri)
  {
    openUrl(paramContext, paramUri, true);
  }
  
  public static void openUrl(Context paramContext, Uri paramUri, boolean paramBoolean)
  {
    openUrl(paramContext, paramUri, paramBoolean, true);
  }
  
  public static void openUrl(final Context paramContext, final Uri paramUri, final boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramContext == null) || (paramUri == null)) {
      return;
    }
    i = UserConfig.selectedAccount;
    boolean[] arrayOfBoolean = new boolean[1];
    arrayOfBoolean[0] = false;
    bool = isInternalUri(paramUri, arrayOfBoolean);
    Object localObject4;
    if (paramBoolean2) {
      try
      {
        if ((paramUri.getHost().toLowerCase().equals("telegra.ph")) || (paramUri.toString().toLowerCase().contains("telegram.org/faq")))
        {
          AlertDialog[] arrayOfAlertDialog = new AlertDialog[1];
          arrayOfAlertDialog[0] = new AlertDialog(paramContext, 1);
          localObject4 = new TLRPC.TL_messages_getWebPagePreview();
          ((TLRPC.TL_messages_getWebPagePreview)localObject4).message = paramUri.toString();
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  try
                  {
                    Browser.2.this.val$progressDialog[0].dismiss();
                    Browser.2.this.val$progressDialog[0] = null;
                    int j = 0;
                    int i = j;
                    if ((paramAnonymousTLObject instanceof TLRPC.TL_messageMediaWebPage))
                    {
                      TLRPC.TL_messageMediaWebPage localTL_messageMediaWebPage = (TLRPC.TL_messageMediaWebPage)paramAnonymousTLObject;
                      i = j;
                      if ((localTL_messageMediaWebPage.webpage instanceof TLRPC.TL_webPage))
                      {
                        i = j;
                        if (localTL_messageMediaWebPage.webpage.cached_page != null)
                        {
                          NotificationCenter.getInstance(Browser.2.this.val$currentAccount).postNotificationName(NotificationCenter.openArticle, new Object[] { localTL_messageMediaWebPage.webpage, Browser.2.this.val$uri.toString() });
                          i = 1;
                        }
                      }
                    }
                    if (i == 0) {
                      Browser.openUrl(Browser.2.this.val$context, Browser.2.this.val$uri, Browser.2.this.val$allowCustom, false);
                    }
                    return;
                  }
                  catch (Throwable localThrowable)
                  {
                    for (;;) {}
                  }
                }
              });
            }
          }
          {
            public void run()
            {
              if (this.val$progressDialog[0] == null) {
                return;
              }
              try
              {
                this.val$progressDialog[0].setMessage(LocaleController.getString("Loading", 2131493762));
                this.val$progressDialog[0].setCanceledOnTouchOutside(false);
                this.val$progressDialog[0].setCancelable(false);
                this.val$progressDialog[0].setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
                {
                  public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                  {
                    ConnectionsManager.getInstance(UserConfig.selectedAccount).cancelRequest(Browser.3.this.val$reqId, true);
                    try
                    {
                      paramAnonymous2DialogInterface.dismiss();
                      return;
                    }
                    catch (Exception paramAnonymous2DialogInterface)
                    {
                      FileLog.e(paramAnonymous2DialogInterface);
                    }
                  }
                });
                this.val$progressDialog[0].show();
                return;
              }
              catch (Exception localException) {}
            }
          }, 1000L);
          return;
        }
      }
      catch (Exception localException1) {}
    }
    for (;;)
    {
      try
      {
        Object localObject1;
        if (paramUri.getScheme() != null)
        {
          localObject1 = paramUri.getScheme().toLowerCase();
          if ((!paramBoolean1) || (!SharedConfig.customTabs) || (bool)) {
            continue;
          }
          paramBoolean1 = ((String)localObject1).equals("tel");
          if (paramBoolean1) {
            continue;
          }
          localIntent = null;
          localObject5 = null;
          localObject4 = localObject5;
        }
        try
        {
          localObject1 = new Intent("android.intent.action.VIEW", Uri.parse("http://www.google.com"));
          localObject4 = localObject5;
          List localList = paramContext.getPackageManager().queryIntentActivities((Intent)localObject1, 0);
          localObject1 = localIntent;
          if (localList != null)
          {
            localObject4 = localObject5;
            localObject1 = localIntent;
            if (!localList.isEmpty())
            {
              localObject4 = localObject5;
              localObject5 = new String[localList.size()];
              i = 0;
              localObject4 = localObject5;
              localObject1 = localObject5;
              if (i < localList.size())
              {
                localObject4 = localObject5;
                localObject5[i] = ((ResolveInfo)localList.get(i)).activityInfo.packageName;
                localObject4 = localObject5;
                if (BuildVars.LOGS_ENABLED)
                {
                  localObject4 = localObject5;
                  FileLog.d("default browser name = " + localObject5[i]);
                }
                i += 1;
                continue;
                localObject1 = "";
              }
            }
          }
        }
        catch (Exception localException2)
        {
          localObject2 = localObject4;
          localObject5 = null;
          localObject4 = localObject5;
        }
      }
      catch (Exception localException4)
      {
        Intent localIntent;
        Object localObject5;
        Object localObject2;
        int j;
        int k;
        Object localObject3;
        FileLog.e(localException4);
        try
        {
          paramUri = new Intent("android.intent.action.VIEW", paramUri);
          if (bool) {
            paramUri.setComponent(new ComponentName(paramContext.getPackageName(), LaunchActivity.class.getName()));
          }
          paramUri.putExtra("create_new_tab", true);
          paramUri.putExtra("com.android.browser.application_id", paramContext.getPackageName());
          paramContext.startActivity(paramUri);
          return;
        }
        catch (Exception paramContext)
        {
          FileLog.e(paramContext);
          return;
        }
        i = k + 1;
        continue;
        j += 1;
        continue;
        i = 0;
        continue;
        i = j + 1;
        continue;
      }
      try
      {
        localIntent = new Intent("android.intent.action.VIEW", paramUri);
        localObject4 = localObject5;
        localObject5 = paramContext.getPackageManager().queryIntentActivities(localIntent, 0);
        if (localObject2 == null) {
          continue;
        }
        i = 0;
        localObject4 = localObject5;
        if (i < ((List)localObject5).size())
        {
          j = 0;
          localObject4 = localObject5;
          k = i;
          if (j >= localObject2.length) {
            continue;
          }
          localObject4 = localObject5;
          if (!localObject2[j].equals(((ResolveInfo)((List)localObject5).get(i)).activityInfo.packageName)) {
            continue;
          }
          localObject4 = localObject5;
          ((List)localObject5).remove(i);
          k = i - 1;
          continue;
          localObject4 = localObject5;
          if (i < ((List)localObject5).size())
          {
            localObject4 = localObject5;
            if (!((ResolveInfo)((List)localObject5).get(i)).activityInfo.packageName.toLowerCase().contains("browser"))
            {
              localObject4 = localObject5;
              j = i;
              if (!((ResolveInfo)((List)localObject5).get(i)).activityInfo.packageName.toLowerCase().contains("chrome")) {
                continue;
              }
            }
            localObject4 = localObject5;
            ((List)localObject5).remove(i);
            j = i - 1;
            continue;
          }
        }
        localObject4 = localObject5;
        localObject2 = localObject5;
        if (BuildVars.LOGS_ENABLED)
        {
          i = 0;
          localObject4 = localObject5;
          localObject2 = localObject5;
          if (i < ((List)localObject5).size())
          {
            localObject4 = localObject5;
            FileLog.d("device has " + ((ResolveInfo)((List)localObject5).get(i)).activityInfo.packageName + " to open " + paramUri.toString());
            i += 1;
            continue;
          }
        }
        if (localObject3 == null) {
          continue;
        }
      }
      catch (Exception localException3)
      {
        localObject3 = localObject4;
        if (arrayOfBoolean[0] != 0) {
          continue;
        }
      }
    }
    if (((List)localObject3).isEmpty())
    {
      localObject3 = new Intent(ApplicationLoader.applicationContext, ShareBroadcastReceiver.class);
      ((Intent)localObject3).setAction("android.intent.action.SEND");
      localObject4 = PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, new Intent(ApplicationLoader.applicationContext, CustomTabsCopyReceiver.class), 134217728);
      localObject5 = new CustomTabsIntent.Builder(getSession());
      ((CustomTabsIntent.Builder)localObject5).addMenuItem(LocaleController.getString("CopyLink", 2131493304), (PendingIntent)localObject4);
      ((CustomTabsIntent.Builder)localObject5).setToolbarColor(Theme.getColor("actionBarDefault"));
      ((CustomTabsIntent.Builder)localObject5).setShowTitle(true);
      ((CustomTabsIntent.Builder)localObject5).setActionButton(BitmapFactory.decodeResource(paramContext.getResources(), 2131165185), LocaleController.getString("ShareFile", 2131494383), PendingIntent.getBroadcast(ApplicationLoader.applicationContext, 0, (Intent)localObject3, 0), false);
      localObject3 = ((CustomTabsIntent.Builder)localObject5).build();
      ((CustomTabsIntent)localObject3).setUseNewTask();
      ((CustomTabsIntent)localObject3).launchUrl(paramContext, paramUri);
      return;
    }
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
    if (customTabsServiceConnection == null) {
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
        for (;;) {}
      }
    }
  }
  
  private static class NavigationCallback
    extends CustomTabsCallback
  {
    public void onNavigationEvent(int paramInt, Bundle paramBundle) {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/browser/Browser.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */