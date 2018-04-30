package org.telegram.messenger;

import android.text.TextUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.Channel.GetInputStreamResult;
import com.google.android.gms.wearable.Channel.GetOutputStreamResult;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;

public class WearDataLayerListenerService
  extends WearableListenerService
{
  private static boolean watchConnected;
  private int currentAccount = UserConfig.selectedAccount;
  
  public static boolean isWatchConnected()
  {
    return watchConnected;
  }
  
  public static void sendMessageToWatch(String paramString1, final byte[] paramArrayOfByte, String paramString2)
  {
    Wearable.getCapabilityClient(ApplicationLoader.applicationContext).getCapability(paramString2, 1).addOnCompleteListener(new OnCompleteListener()
    {
      public void onComplete(Task<CapabilityInfo> paramAnonymousTask)
      {
        Object localObject = (CapabilityInfo)paramAnonymousTask.getResult();
        if (localObject != null)
        {
          paramAnonymousTask = Wearable.getMessageClient(ApplicationLoader.applicationContext);
          localObject = ((CapabilityInfo)localObject).getNodes().iterator();
          while (((Iterator)localObject).hasNext()) {
            paramAnonymousTask.sendMessage(((Node)((Iterator)localObject).next()).getId(), this.val$path, paramArrayOfByte);
          }
        }
      }
    });
  }
  
  public static void updateWatchConnectionState()
  {
    Wearable.getCapabilityClient(ApplicationLoader.applicationContext).getCapability("remote_notifications", 1).addOnCompleteListener(new OnCompleteListener()
    {
      public void onComplete(Task<CapabilityInfo> paramAnonymousTask)
      {
        WearDataLayerListenerService.access$102(false);
        try
        {
          paramAnonymousTask = (CapabilityInfo)paramAnonymousTask.getResult();
          if (paramAnonymousTask == null) {
            return;
          }
          paramAnonymousTask = paramAnonymousTask.getNodes().iterator();
          while (paramAnonymousTask.hasNext()) {
            if (((Node)paramAnonymousTask.next()).isNearby()) {
              WearDataLayerListenerService.access$102(true);
            }
          }
          return;
        }
        catch (Exception paramAnonymousTask) {}
      }
    });
  }
  
  public void onCapabilityChanged(CapabilityInfo paramCapabilityInfo)
  {
    if ("remote_notifications".equals(paramCapabilityInfo.getName()))
    {
      watchConnected = false;
      paramCapabilityInfo = paramCapabilityInfo.getNodes().iterator();
      while (paramCapabilityInfo.hasNext()) {
        if (((Node)paramCapabilityInfo.next()).isNearby()) {
          watchConnected = true;
        }
      }
    }
  }
  
  public void onChannelOpened(Channel paramChannel)
  {
    GoogleApiClient localGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
    if (!localGoogleApiClient.blockingConnect().isSuccess()) {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.e("failed to connect google api client");
      }
    }
    for (;;)
    {
      return;
      Object localObject1 = paramChannel.getPath();
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("wear channel path: " + (String)localObject1);
      }
      try
      {
        if ("/getCurrentUser".equals(localObject1))
        {
          localObject1 = new DataOutputStream(new BufferedOutputStream(((Channel.GetOutputStreamResult)paramChannel.getOutputStream(localGoogleApiClient).await()).getOutputStream()));
          if (UserConfig.getInstance(this.currentAccount).isClientActivated())
          {
            localObject7 = UserConfig.getInstance(this.currentAccount).getCurrentUser();
            ((DataOutputStream)localObject1).writeInt(((TLRPC.User)localObject7).id);
            ((DataOutputStream)localObject1).writeUTF(((TLRPC.User)localObject7).first_name);
            ((DataOutputStream)localObject1).writeUTF(((TLRPC.User)localObject7).last_name);
            ((DataOutputStream)localObject1).writeUTF(((TLRPC.User)localObject7).phone);
            if (((TLRPC.User)localObject7).photo != null)
            {
              localObject5 = FileLoader.getPathToAttach(((TLRPC.User)localObject7).photo.photo_small, true);
              localObject8 = new CyclicBarrier(2);
              if (!((File)localObject5).exists())
              {
                localObject6 = new NotificationCenter.NotificationCenterDelegate()
                {
                  public void didReceivedNotification(int paramAnonymousInt1, int paramAnonymousInt2, Object... paramAnonymousVarArgs)
                  {
                    if (paramAnonymousInt1 == NotificationCenter.FileDidLoaded)
                    {
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("file loaded: " + paramAnonymousVarArgs[0] + " " + paramAnonymousVarArgs[0].getClass().getName());
                      }
                      if (paramAnonymousVarArgs[0].equals(localObject5.getName())) {
                        if (BuildVars.LOGS_ENABLED) {
                          FileLog.e("LOADED USER PHOTO");
                        }
                      }
                    }
                    try
                    {
                      localObject8.await(10L, TimeUnit.MILLISECONDS);
                      return;
                    }
                    catch (Exception paramAnonymousVarArgs) {}
                  }
                };
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationCenter.getInstance(WearDataLayerListenerService.this.currentAccount).addObserver(localObject6, NotificationCenter.FileDidLoaded);
                    FileLoader.getInstance(WearDataLayerListenerService.this.currentAccount).loadFile(localObject7.photo.photo_small, null, 0, 1);
                  }
                });
              }
            }
          }
        }
      }
      catch (Exception localException1)
      {
        try
        {
          final Object localObject7;
          final Object localObject5;
          final Object localObject8;
          final Object localObject6;
          ((CyclicBarrier)localObject8).await(10L, TimeUnit.SECONDS);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(WearDataLayerListenerService.this.currentAccount).removeObserver(localObject6, NotificationCenter.FileDidLoaded);
            }
          });
          if ((((File)localObject5).exists()) && (((File)localObject5).length() <= 52428800L))
          {
            localObject6 = new byte[(int)((File)localObject5).length()];
            localObject5 = new FileInputStream((File)localObject5);
            new DataInputStream((InputStream)localObject5).readFully((byte[])localObject6);
            ((FileInputStream)localObject5).close();
            ((DataOutputStream)localObject1).writeInt(localObject6.length);
            ((DataOutputStream)localObject1).write((byte[])localObject6);
            label358:
            ((DataOutputStream)localObject1).flush();
            ((DataOutputStream)localObject1).close();
          }
          for (;;)
          {
            paramChannel.close(localGoogleApiClient).await();
            localGoogleApiClient.disconnect();
            if (!BuildVars.LOGS_ENABLED) {
              break;
            }
            FileLog.d("WearableDataLayer channel thread exiting");
            return;
            ((DataOutputStream)localObject1).writeInt(0);
            break label358;
            localException1 = localException1;
            if (!BuildVars.LOGS_ENABLED) {
              continue;
            }
            FileLog.e("error processing wear request", localException1);
            continue;
            localException1.writeInt(0);
            break label358;
            localException1.writeInt(0);
            break label358;
            final Object localObject2;
            if ("/waitForAuthCode".equals(localException1))
            {
              ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
              localObject2 = new String[1];
              localObject2[0] = null;
              localObject6 = new CyclicBarrier(2);
              localObject5 = new NotificationCenter.NotificationCenterDelegate()
              {
                public void didReceivedNotification(int paramAnonymousInt1, int paramAnonymousInt2, Object... paramAnonymousVarArgs)
                {
                  if ((paramAnonymousInt1 == NotificationCenter.didReceivedNewMessages) && (((Long)paramAnonymousVarArgs[0]).longValue() == 777000L))
                  {
                    paramAnonymousVarArgs = (ArrayList)paramAnonymousVarArgs[1];
                    if (paramAnonymousVarArgs.size() > 0)
                    {
                      paramAnonymousVarArgs = (MessageObject)paramAnonymousVarArgs.get(0);
                      if (!TextUtils.isEmpty(paramAnonymousVarArgs.messageText))
                      {
                        paramAnonymousVarArgs = Pattern.compile("[0-9]+").matcher(paramAnonymousVarArgs.messageText);
                        if (paramAnonymousVarArgs.find()) {
                          localObject2[0] = paramAnonymousVarArgs.group();
                        }
                      }
                    }
                  }
                  try
                  {
                    localObject6.await(10L, TimeUnit.MILLISECONDS);
                    return;
                  }
                  catch (Exception paramAnonymousVarArgs) {}
                }
              };
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(WearDataLayerListenerService.this.currentAccount).addObserver(localObject5, NotificationCenter.didReceivedNewMessages);
                }
              });
            }
            try
            {
              ((CyclicBarrier)localObject6).await(15L, TimeUnit.SECONDS);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(WearDataLayerListenerService.this.currentAccount).removeObserver(localObject5, NotificationCenter.didReceivedNewMessages);
                }
              });
              localObject5 = new DataOutputStream(((Channel.GetOutputStreamResult)paramChannel.getOutputStream(localGoogleApiClient).await()).getOutputStream());
              if (localObject2[0] != null) {
                ((DataOutputStream)localObject5).writeUTF(localObject2[0]);
              }
              for (;;)
              {
                ((DataOutputStream)localObject5).flush();
                ((DataOutputStream)localObject5).close();
                ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
                break;
                ((DataOutputStream)localObject5).writeUTF("");
              }
              if (!"/getChatPhoto".equals(localObject2)) {
                continue;
              }
              localObject6 = new DataInputStream(((Channel.GetInputStreamResult)paramChannel.getInputStream(localGoogleApiClient).await()).getInputStream());
              localObject7 = new DataOutputStream(((Channel.GetOutputStreamResult)paramChannel.getOutputStream(localGoogleApiClient).await()).getOutputStream());
              for (;;)
              {
                try
                {
                  localObject2 = new JSONObject(((DataInputStream)localObject6).readUTF());
                  m = ((JSONObject)localObject2).getInt("chat_id");
                  int n = ((JSONObject)localObject2).getInt("account_id");
                  int k = -1;
                  i = 0;
                  j = k;
                  if (i < UserConfig.getActivatedAccountsCount())
                  {
                    if (UserConfig.getInstance(i).getClientUserId() == n) {
                      j = i;
                    }
                  }
                  else
                  {
                    if (j == -1) {
                      break label1021;
                    }
                    localObject5 = null;
                    if (m <= 0) {
                      continue;
                    }
                    localObject8 = MessagesController.getInstance(j).getUser(Integer.valueOf(m));
                    localObject2 = localObject5;
                    if (localObject8 != null)
                    {
                      localObject2 = localObject5;
                      if (((TLRPC.User)localObject8).photo != null) {
                        localObject2 = ((TLRPC.User)localObject8).photo.photo_small;
                      }
                    }
                    if (localObject2 == null) {
                      break label1012;
                    }
                    localObject2 = FileLoader.getPathToAttach((TLObject)localObject2, true);
                    if ((!((File)localObject2).exists()) || (((File)localObject2).length() >= 102400L)) {
                      continue;
                    }
                    ((DataOutputStream)localObject7).writeInt((int)((File)localObject2).length());
                    localObject2 = new FileInputStream((File)localObject2);
                    localObject5 = new byte['⠀'];
                    i = ((FileInputStream)localObject2).read((byte[])localObject5);
                    if (i <= 0) {
                      continue;
                    }
                    ((DataOutputStream)localObject7).write((byte[])localObject5, 0, i);
                    continue;
                  }
                }
                catch (Exception localException2)
                {
                  int m;
                  int i;
                  int j;
                  ((DataInputStream)localObject6).close();
                  ((DataOutputStream)localObject7).close();
                  break;
                  i += 1;
                  continue;
                  localObject8 = MessagesController.getInstance(j).getChat(Integer.valueOf(-m));
                  Object localObject3 = localObject5;
                  if (localObject8 == null) {
                    continue;
                  }
                  localObject3 = localObject5;
                  if (((TLRPC.Chat)localObject8).photo == null) {
                    continue;
                  }
                  localObject3 = ((TLRPC.Chat)localObject8).photo.photo_small;
                  continue;
                  ((FileInputStream)localObject3).close();
                  ((DataOutputStream)localObject7).flush();
                  ((DataInputStream)localObject6).close();
                  ((DataOutputStream)localObject7).close();
                  break;
                  ((DataOutputStream)localObject7).writeInt(0);
                  continue;
                }
                finally
                {
                  ((DataInputStream)localObject6).close();
                  ((DataOutputStream)localObject7).close();
                }
                label1012:
                ((DataOutputStream)localObject7).writeInt(0);
                continue;
                label1021:
                ((DataOutputStream)localObject7).writeInt(0);
              }
            }
            catch (Exception localException3)
            {
              for (;;) {}
            }
          }
        }
        catch (Exception localException4)
        {
          for (;;) {}
        }
      }
    }
  }
  
  public void onCreate()
  {
    super.onCreate();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("WearableDataLayer service created");
    }
  }
  
  public void onDestroy()
  {
    super.onDestroy();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("WearableDataLayer service destroyed");
    }
  }
  
  public void onMessageReceived(final MessageEvent paramMessageEvent)
  {
    if ("/reply".equals(paramMessageEvent.getPath())) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          try
          {
            ApplicationLoader.postInitApplication();
            JSONObject localJSONObject = new JSONObject(new String(paramMessageEvent.getData(), "UTF-8"));
            str = localJSONObject.getString("text");
            if (str == null) {
              return;
            }
            if (str.length() == 0) {
              return;
            }
            l = localJSONObject.getLong("chat_id");
            m = localJSONObject.getInt("max_id");
            k = -1;
            n = localJSONObject.getInt("account_id");
            i = 0;
          }
          catch (Exception localException)
          {
            String str;
            long l;
            int m;
            int k;
            int n;
            int i;
            int j;
            while (BuildVars.LOGS_ENABLED)
            {
              FileLog.e(localException);
              return;
              i += 1;
            }
          }
          j = k;
          if (i < UserConfig.getActivatedAccountsCount())
          {
            if (UserConfig.getInstance(i).getClientUserId() == n) {
              j = i;
            }
          }
          else
          {
            if ((l == 0L) || (m == 0) || (j == -1)) {
              return;
            }
            SendMessagesHelper.getInstance(j).sendMessage(str.toString(), l, null, null, true, null, null, null);
            MessagesController.getInstance(j).markDialogAsRead(l, m, m, 0, false, 0, true);
            return;
          }
        }
      });
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/WearDataLayerListenerService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */