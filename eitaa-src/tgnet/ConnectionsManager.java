package ir.eitaa.tgnet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import ir.eitaa.helper.http.HelperHttp;
import ir.eitaa.helper.schedule.ScheduleController;
import ir.eitaa.helper.schedule.ScheduleGetDifference;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.BuildVars;
import ir.eitaa.messenger.ContactsController;
import ir.eitaa.messenger.DispatchQueue;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.LocaleController;
import ir.eitaa.messenger.MessagesController;
import ir.eitaa.messenger.NotificationCenter;
import ir.eitaa.messenger.UserConfig;
import ir.eitaa.messenger.Utilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionsManager
{
  public static final int AllConnectionTypes = 7;
  public static final int ConnectionStateConnected = 3;
  public static final int ConnectionStateConnecting = 1;
  public static final int ConnectionStateUpdating = 4;
  public static final int ConnectionStateWaitingForNetwork = 2;
  public static final int ConnectionTypeDownload = 2;
  public static final int ConnectionTypeDownload2 = 65538;
  public static final int ConnectionTypeGeneric = 1;
  public static final int ConnectionTypePush = 8;
  public static final int ConnectionTypeUpload = 4;
  private static final int DC_UPDATE_TIME = 3600;
  private static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
  private static volatile ConnectionsManager Instance = null;
  public static final int RequestFlagCanCompress = 4;
  public static final int RequestFlagEnableUnauthorized = 1;
  public static final int RequestFlagFailOnServerErrors = 2;
  public static final int RequestFlagForceDownload = 32;
  public static final int RequestFlagInvokeAfter = 64;
  public static final int RequestFlagNeedQuickAck = 128;
  public static final int RequestFlagTryDifferentDc = 16;
  public static final int RequestFlagWithoutLogin = 8;
  private static final String WEB_ADD = "/eitaa/index.php";
  private static final String WEB_ADD2 = "/eitaa/index.php";
  private ArrayList<Action> actionQueue = new ArrayList();
  public ArrayList<String> addressesIpv4 = new ArrayList();
  public ArrayList<String> addressesIpv4Download = new ArrayList();
  private boolean appPaused = true;
  private int connectionState = getConnectionState();
  private int connectionToken = 1;
  private volatile int currentAddressNumIpv4 = 0;
  private volatile int currentAddressNumIpv4Download = 0;
  protected int currentDatacenterId;
  private volatile int currentPortNumIpv4 = 0;
  private volatile int currentPortNumIpv4Download = 0;
  private HashMap<Integer, TLRPC.TL_dcOption> datacenters = new HashMap();
  public int[] defaultPorts = { 443 };
  public int[] defaultPorts8888 = { 443 };
  private boolean disableSchedule = false;
  private int failedConnectionCount;
  private int isTestBackend = 0;
  private boolean isUpdating = false;
  private int lastClassGuid = 1;
  private int lastDcUpdateTime = 0;
  private long lastGetDifferenceTime = getCurrentTimeMillis();
  private int lastInitVersion = 0;
  private long lastOutgoingMessageId = 0L;
  public long lastPauseTime = getCurrentTimeMillis();
  private long lastPingTime = getCurrentTimeMillis();
  private AtomicInteger lastRequestToken = new AtomicInteger(1);
  private long nextPingId = 0L;
  private int nextSleepTimeout = 30000;
  public int overridePort = -1;
  private boolean paused = false;
  public boolean pingOK = true;
  public HashMap<String, Integer> ports = new HashMap();
  private HashMap<Integer, ArrayList<Long>> quickAckIdToRequestIds = new HashMap();
  private boolean refreshingToken = false;
  private long refreshingTokenTime;
  private ArrayList<Integer> requestInvalid = new ArrayList();
  private ConcurrentLinkedQueue<RPCRequest> requestQueue = new ConcurrentLinkedQueue();
  private ConcurrentHashMap<Long, Integer> requestsByClass = new ConcurrentHashMap(100, 1.0F, 2);
  private ConcurrentHashMap<Integer, ArrayList<Long>> requestsByGuids = new ConcurrentHashMap(100, 1.0F, 2);
  private ConcurrentLinkedQueue<RPCRequest> runningRequests = new ConcurrentLinkedQueue();
  private Runnable stageRunnable = new Runnable()
  {
    public void run()
    {
      Utilities.stageQueue.handler.removeCallbacks(ConnectionsManager.this.stageRunnable);
      if (ConnectionsManager.this.disableSchedule) {
        return;
      }
      long l = ConnectionsManager.this.getCurrentTimeMillis();
      int i;
      if ((ConnectionsManager.this.lastPauseTime != 0L) && (ConnectionsManager.this.lastPauseTime < l - ConnectionsManager.this.nextSleepTimeout))
      {
        j = 0;
        i = j;
        Iterator localIterator;
        RPCRequest localRPCRequest;
        if (0 == 0)
        {
          localIterator = ConnectionsManager.this.runningRequests.iterator();
          do
          {
            i = j;
            if (!localIterator.hasNext()) {
              break;
            }
            localRPCRequest = (RPCRequest)localIterator.next();
          } while ((localRPCRequest.retryCount >= 10) || (localRPCRequest.startTime + 60 <= (int)(l / 1000L)) || (((localRPCRequest.connectionType & 0x2) == 0) && ((localRPCRequest.connectionType & 0x4) == 0)));
          i = 1;
        }
        j = i;
        if (i == 0)
        {
          localIterator = ConnectionsManager.this.requestQueue.iterator();
          do
          {
            j = i;
            if (!localIterator.hasNext()) {
              break;
            }
            localRPCRequest = (RPCRequest)localIterator.next();
          } while (((localRPCRequest.connectionType & 0x2) == 0) && ((localRPCRequest.connectionType & 0x4) == 0));
          j = 1;
        }
        if (j != 0) {
          break label487;
        }
        try
        {
          ConnectionsManager.access$502(ConnectionsManager.this, true);
          Utilities.stageQueue.postRunnable(ConnectionsManager.this.stageRunnable, 1000L);
          return;
        }
        catch (Exception localException)
        {
          FileLog.e("TSMS", localException);
        }
      }
      if (ConnectionsManager.this.paused)
      {
        ConnectionsManager.access$502(ConnectionsManager.this, false);
        FileLog.e("TSMS", "resume network and timers");
      }
      if (ConnectionsManager.isNetworkOnline())
      {
        if (ConnectionsManager.this.lastPingTime < System.currentTimeMillis() - 19000L)
        {
          ConnectionsManager.access$602(ConnectionsManager.this, ConnectionsManager.this.getCurrentTimeMillis());
          ConnectionsManager.this.generatePing();
        }
        if (ConnectionsManager.this.getPingStatus())
        {
          if (ConnectionsManager.this.appPaused) {
            break label517;
          }
          i = 1;
          label358:
          if (ConnectionsManager.this.lastGetDifferenceTime >= System.currentTimeMillis() - MessagesController.getInstance().schedule_period_forground_ms) {
            break label522;
          }
        }
      }
      label487:
      label517:
      label522:
      for (int j = 1;; j = 0)
      {
        if (((i & j) != 0) && (!ScheduleGetDifference.isDeveloopMode))
        {
          MessagesController.getInstance().getDifference();
          ConnectionsManager.access$902(ConnectionsManager.this, ConnectionsManager.this.getCurrentTimeMillis());
        }
        if ((!ConnectionsManager.this.updatingDcSettings) && (ConnectionsManager.this.lastDcUpdateTime < (int)(System.currentTimeMillis() / 1000L) - 3600)) {
          ConnectionsManager.this.updateDcSettings(0);
        }
        MessagesController.getInstance().updateTimerProc();
        ConnectionsManager.this.processRequestQueue(0, 0);
        Utilities.stageQueue.postRunnable(ConnectionsManager.this.stageRunnable, 1000L);
        return;
        ConnectionsManager localConnectionsManager = ConnectionsManager.this;
        localConnectionsManager.lastPauseTime += 30000L;
        FileLog.e("TSMS", "don't sleep 30 seconds because of salt, upload or download request");
        break;
        i = 0;
        break label358;
      }
    }
  };
  private int timeDifference = 0;
  private boolean updatingDcSettings = false;
  private int updatingDcStartTime = 0;
  private PowerManager.WakeLock wakeLock = null;
  
  public ConnectionsManager()
  {
    if (!isNetworkOnline()) {
      this.connectionState = 1;
    }
    loadSession();
    Utilities.stageQueue.postRunnable(this.stageRunnable, 1000L);
    try
    {
      this.wakeLock = ((PowerManager)ApplicationLoader.applicationContext.getSystemService("power")).newWakeLock(1, "lock");
      this.wakeLock.setReferenceCounted(false);
      return;
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
  }
  
  private void cancelRpc(final long paramLong, boolean paramBoolean)
  {
    if (paramLong == 0L) {
      return;
    }
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int j = 0;
        Iterator localIterator = ConnectionsManager.this.requestQueue.iterator();
        RPCRequest localRPCRequest;
        do
        {
          i = j;
          if (!localIterator.hasNext()) {
            break;
          }
          localRPCRequest = (RPCRequest)localIterator.next();
        } while (localRPCRequest.token != paramLong);
        int i = 1;
        localRPCRequest.cancelled = true;
        FileLog.d("TSMS", "===== Cancelled queued rpc request " + localRPCRequest.rawRequest);
        ConnectionsManager.this.requestQueue.remove(localRPCRequest);
        if (!this.val$ifNotSent)
        {
          localIterator = ConnectionsManager.this.runningRequests.iterator();
          do
          {
            j = i;
            if (!localIterator.hasNext()) {
              break;
            }
            localRPCRequest = (RPCRequest)localIterator.next();
          } while (localRPCRequest.token != paramLong);
          j = 1;
          FileLog.d("TSMS", "===== Cancelled running rpc request " + localRPCRequest.rawRequest);
          localRPCRequest.cancelled = true;
          localRPCRequest.rawRequest.freeResources();
          localRPCRequest.rpcRequest.freeResources();
          ConnectionsManager.this.runningRequests.remove(localRPCRequest);
          if (j == 0) {
            FileLog.d("TSMS", "***** Warning: cancelling unknown request");
          }
        }
      }
    });
  }
  
  private void checkConnection()
  {
    if (isNetworkOnline())
    {
      this.disableSchedule = false;
      Utilities.stageQueue.postRunnable(this.stageRunnable, 1000L);
    }
    for (;;)
    {
      ScheduleController.scheduleGetDifference(Boolean.valueOf(this.appPaused), Boolean.valueOf(this.disableSchedule));
      return;
      this.disableSchedule = true;
      onConnectionStateChanged(2);
    }
  }
  
  private void fillDatacenters()
  {
    if (this.isTestBackend == 0)
    {
      localTL_dcOption = new TLRPC.TL_dcOption();
      localTL_dcOption.id = 1;
      localTL_dcOption.ip_address = "alzheimer.eitaa.com";
      localTL_dcOption.port = 80;
      localTL_dcOption.flags = 2;
      addAddressAndPort(localTL_dcOption.ip_address, localTL_dcOption.port, localTL_dcOption.flags);
      this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
      localTL_dcOption = new TLRPC.TL_dcOption();
      localTL_dcOption.id = 2;
      localTL_dcOption.ip_address = "majid.eitaa.com";
      localTL_dcOption.port = 80;
      localTL_dcOption.flags = 0;
      addAddressAndPort(localTL_dcOption.ip_address, localTL_dcOption.port, localTL_dcOption.flags);
      this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
      localTL_dcOption = new TLRPC.TL_dcOption();
      localTL_dcOption.id = 3;
      localTL_dcOption.ip_address = "armita.eitaa.com";
      localTL_dcOption.port = 80;
      localTL_dcOption.flags = 0;
      addAddressAndPort(localTL_dcOption.ip_address, localTL_dcOption.port, localTL_dcOption.flags);
      this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
      localTL_dcOption = new TLRPC.TL_dcOption();
      localTL_dcOption.id = 4;
      localTL_dcOption.ip_address = "mostafa.eitaa.com";
      localTL_dcOption.port = 80;
      localTL_dcOption.flags = 0;
      addAddressAndPort(localTL_dcOption.ip_address, localTL_dcOption.port, localTL_dcOption.flags);
      this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
      return;
    }
    TLRPC.TL_dcOption localTL_dcOption = new TLRPC.TL_dcOption();
    localTL_dcOption.id = 1;
    localTL_dcOption.ip_address = "192.168.2.135";
    localTL_dcOption.port = 80;
    localTL_dcOption.flags = 0;
    addAddressAndPort(localTL_dcOption.ip_address, localTL_dcOption.port, localTL_dcOption.flags);
    this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
  }
  
  private void generatePing()
  {
    TLRPC.TL_ping localTL_ping = new TLRPC.TL_ping();
    long l = this.nextPingId;
    this.nextPingId = (1L + l);
    localTL_ping.ping_id = l;
    RPCRequest localRPCRequest = new RPCRequest();
    localRPCRequest.token = this.lastRequestToken.getAndIncrement();
    localRPCRequest.flags = 8;
    localRPCRequest.connectionType = 1;
    localRPCRequest.rawRequest = localTL_ping;
    localRPCRequest.rpcRequest = wrapInLayer(localTL_ping, localRPCRequest);
    this.lastPingTime = getCurrentTimeMillis();
    sendMessagesToTransport(localRPCRequest);
  }
  
  private int getConnectionToken()
  {
    return this.connectionToken;
  }
  
  public static ConnectionsManager getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          ConnectionsManager localConnectionsManager2 = Instance;
          localObject1 = localConnectionsManager2;
          if (localConnectionsManager2 == null) {
            localObject1 = new ConnectionsManager();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (ConnectionsManager)localObject1;
          return (ConnectionsManager)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localConnectionsManager1;
  }
  
  private void handleConnectionError(int paramInt)
  {
    this.failedConnectionCount += 1;
    if ((isNetworkOnline()) && (this.failedConnectionCount > 1))
    {
      nextAddressOrPort(paramInt);
      this.failedConnectionCount = 0;
    }
  }
  
  public static boolean isConnectedToData()
  {
    boolean bool2 = false;
    try
    {
      Object localObject = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getNetworkInfo(0);
      boolean bool1 = bool2;
      if (localObject != null)
      {
        localObject = ((NetworkInfo)localObject).getState();
        NetworkInfo.State localState = NetworkInfo.State.CONNECTED;
        bool1 = bool2;
        if (localObject == localState) {
          bool1 = true;
        }
      }
      return bool1;
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
    return false;
  }
  
  public static boolean isConnectedToWiFi()
  {
    try
    {
      Object localObject = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getNetworkInfo(1);
      if (localObject != null)
      {
        localObject = ((NetworkInfo)localObject).getState();
        NetworkInfo.State localState = NetworkInfo.State.CONNECTED;
        if (localObject == localState) {
          return true;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
    return false;
  }
  
  public static boolean isNetworkOnline()
  {
    try
    {
      Object localObject = (ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity");
      NetworkInfo localNetworkInfo = ((ConnectivityManager)localObject).getActiveNetworkInfo();
      if (localNetworkInfo != null)
      {
        if (localNetworkInfo.isConnectedOrConnecting()) {
          break label84;
        }
        if (localNetworkInfo.isAvailable()) {
          return true;
        }
      }
      localNetworkInfo = ((ConnectivityManager)localObject).getNetworkInfo(0);
      if ((localNetworkInfo == null) || (!localNetworkInfo.isConnectedOrConnecting()))
      {
        localObject = ((ConnectivityManager)localObject).getNetworkInfo(1);
        if (localObject != null)
        {
          boolean bool = ((NetworkInfo)localObject).isConnectedOrConnecting();
          if (bool) {}
        }
        else
        {
          return false;
        }
      }
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
    label84:
    return true;
  }
  
  public static boolean isRoaming()
  {
    try
    {
      NetworkInfo localNetworkInfo = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getActiveNetworkInfo();
      if (localNetworkInfo != null)
      {
        boolean bool = localNetworkInfo.isRoaming();
        return bool;
      }
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
    return false;
  }
  
  private void loadSession()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", 0);
        ConnectionsManager.access$2502(ConnectionsManager.this, ((SharedPreferences)localObject).getInt("datacenterSetId", 0));
        ConnectionsManager.this.currentDatacenterId = ((SharedPreferences)localObject).getInt("currentDatacenterId", 0);
        ConnectionsManager.access$2602(ConnectionsManager.this, ((SharedPreferences)localObject).getInt("timeDifference", 0));
        ConnectionsManager.access$1102(ConnectionsManager.this, ((SharedPreferences)localObject).getInt("lastDcUpdateTime", 0));
        try
        {
          localObject = ((SharedPreferences)localObject).getString("datacenters", null);
          if (localObject != null)
          {
            localObject = Base64.decode((String)localObject, 0);
            if (localObject != null)
            {
              localObject = new SerializedData((byte[])localObject);
              int j = ((SerializedData)localObject).readInt32(false);
              int i = 0;
              while (i < j)
              {
                TLRPC.TL_dcOption localTL_dcOption = new TLRPC.TL_dcOption();
                localTL_dcOption.id = ((SerializedData)localObject).readInt32(false);
                localTL_dcOption.ip_address = ((SerializedData)localObject).readString(false);
                localTL_dcOption.port = ((SerializedData)localObject).readInt32(false);
                localTL_dcOption.flags = ((SerializedData)localObject).readInt32(false);
                ConnectionsManager.this.datacenters.put(Integer.valueOf(localTL_dcOption.id), localTL_dcOption);
                i += 1;
              }
              ((SerializedData)localObject).cleanup();
            }
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            FileLog.e("tmessages", localException);
          }
        }
        if ((ConnectionsManager.this.currentDatacenterId != 0) && (UserConfig.isClientActivated()) && (ConnectionsManager.this.datacenterWithId(ConnectionsManager.this.currentDatacenterId) == null))
        {
          ConnectionsManager.this.currentDatacenterId = 0;
          ConnectionsManager.this.datacenters.clear();
          UserConfig.clearConfig();
        }
        ConnectionsManager.this.fillDatacenters();
        if ((ConnectionsManager.this.datacenters.size() != 0) && (ConnectionsManager.this.currentDatacenterId == 0))
        {
          if (ConnectionsManager.this.currentDatacenterId == 0) {
            ConnectionsManager.this.currentDatacenterId = 1;
          }
          ConnectionsManager.this.saveSession();
        }
      }
    });
  }
  
  public static native void native_setJava(boolean paramBoolean);
  
  private void onConnectionDataReceived(RPCRequest paramRPCRequest, NativeByteBuffer paramNativeByteBuffer)
  {
    if (((paramRPCRequest.connectionType & 0x1) != 0) && ((this.connectionState == 1) || (this.connectionState == 2))) {
      onConnectionStateChanged(3);
    }
    paramNativeByteBuffer = deserialize(paramRPCRequest.rawRequest, paramNativeByteBuffer, true);
    if (((paramNativeByteBuffer instanceof TLRPC.TL_error)) && (((TLRPC.TL_error)paramNativeByteBuffer).text.contains("INVALID_CONSTRUCTOR")))
    {
      if (!this.requestInvalid.contains(Integer.valueOf(paramRPCRequest.rawRequest.getClass().hashCode()))) {
        this.requestInvalid.add(Integer.valueOf(paramRPCRequest.rawRequest.getClass().hashCode()));
      }
      rpcCompleted(paramRPCRequest.messageId);
    }
    if ((paramNativeByteBuffer instanceof TLRPC.TL_updates_ExpireToken))
    {
      refreshToken();
      reSendRequest(paramRPCRequest);
    }
    do
    {
      return;
      if ((paramNativeByteBuffer instanceof TLRPC.TL_tokenUpdateing))
      {
        reSendRequest(paramRPCRequest);
        return;
      }
      if ((paramNativeByteBuffer instanceof TLRPC.TL_updates_token))
      {
        UserConfig.token = ((TLRPC.TL_updates_token)paramNativeByteBuffer).token;
        UserConfig.saveConfig(false);
        this.refreshingToken = false;
        this.refreshingTokenTime = 0L;
        return;
      }
      if (paramNativeByteBuffer != null)
      {
        this.pingOK = true;
        this.lastPingTime = getCurrentTimeMillis();
        if ((BuildVars.DEBUG_LOG) && (BuildVars.DEVELOP_VERSION) && (!(paramRPCRequest.rawRequest instanceof TLRPC.TL_ping))) {
          AndroidUtilities.sendJson(AndroidUtilities.toJakson(paramRPCRequest.rawRequest), AndroidUtilities.toJakson(paramNativeByteBuffer));
        }
        processMessage(paramNativeByteBuffer, paramRPCRequest);
        return;
      }
      if (paramRPCRequest.connectionType == 1)
      {
        processMessage(null, paramRPCRequest);
        rpcCompleted(paramRPCRequest.messageId);
      }
    } while (((paramRPCRequest.rawRequest instanceof TLRPC.TL_upload_saveFilePart)) || ((paramRPCRequest.rawRequest instanceof TLRPC.TL_upload_saveBigFilePart)) || (!BuildVars.DEBUG_LOG) || (!BuildVars.DEVELOP_VERSION) || ((paramRPCRequest.rawRequest instanceof TLRPC.TL_ping)));
    AndroidUtilities.sendJson(AndroidUtilities.toJakson(paramRPCRequest.rawRequest), null);
  }
  
  private static void onConnectionStateChanged(int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ConnectionsManager.access$2802(ConnectionsManager.getInstance(), this.val$state);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
      }
    });
  }
  
  public static void onInternalPushReceived()
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        try
        {
          if (!ConnectionsManager.getInstance().wakeLock.isHeld())
          {
            ConnectionsManager.getInstance().wakeLock.acquire(10000L);
            FileLog.d("TSMS", "acquire wakelock");
          }
          return;
        }
        catch (Exception localException)
        {
          FileLog.e("TSMS", localException);
        }
      }
    });
  }
  
  private static void onLogout()
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (UserConfig.getClientUserId() != 0)
        {
          UserConfig.clearConfig();
          MessagesController.getInstance().performLogout(false);
        }
      }
    });
  }
  
  public static void onSessionCreated()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance().getDifference();
      }
    });
  }
  
  public static void onUnparsedMessageReceived(int paramInt) {}
  
  public static void onUpdate()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.getInstance().updateTimerProc();
      }
    });
  }
  
  public static void onUpdateConfig(int paramInt)
  {
    try
    {
      Object localObject = NativeByteBuffer.wrap(paramInt);
      ((NativeByteBuffer)localObject).reused = true;
      localObject = TLRPC.TL_config.TLdeserialize((AbstractSerializedData)localObject, ((NativeByteBuffer)localObject).readInt32(true), true);
      if (localObject != null) {
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            MessagesController.getInstance().updateConfig(this.val$message);
          }
        });
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
  }
  
  private void processMessage(TLObject paramTLObject, RPCRequest paramRPCRequest)
  {
    if (paramTLObject == null)
    {
      FileLog.e("TSMS", "message is null. Request: " + paramRPCRequest.toString());
      if (paramRPCRequest.completionBlock != null)
      {
        paramTLObject = new TLRPC.TL_error();
        paramTLObject.code = 500;
        paramTLObject.text = "Response message is NULL";
        paramRPCRequest.completionBlock.run(null, paramTLObject);
      }
      return;
    }
    int i = 0;
    int k = 0;
    int i2 = 0;
    int i1 = 1;
    if ((paramTLObject instanceof TLRPC.TL_pong))
    {
      this.pingOK = true;
      this.lastPingTime = getCurrentTimeMillis();
      i = 1;
    }
    int m = i1;
    int n = i;
    Object localObject1;
    Object localObject3;
    int j;
    if (paramRPCRequest.completionBlock != null)
    {
      localObject1 = null;
      if ((paramTLObject instanceof TLRPC.TL_gzip_packed)) {
        localObject3 = (TLRPC.TL_gzip_packed)paramTLObject;
      }
      j = i;
      if ((paramTLObject instanceof TLRPC.TL_error))
      {
        localObject1 = ((TLRPC.TL_error)paramTLObject).text;
        FileLog.e("TSMS", String.format(Locale.US, "***** RPC error %d: %s", new Object[] { Integer.valueOf(((TLRPC.TL_error)paramTLObject).code), localObject1 }));
        k = ((TLRPC.TL_error)paramTLObject).code;
        if ((k != 500) && (k >= 0)) {
          break label555;
        }
        if ((paramRPCRequest.flags & 0x2) == 0)
        {
          i = 1;
          j = Math.min(1, paramRPCRequest.serverFailureCount * 2);
          paramRPCRequest.minStartTime = (paramRPCRequest.startTime + j);
          paramRPCRequest.confirmed = false;
        }
        paramRPCRequest.serverFailureCount += 1;
        j = i;
        label273:
        localObject1 = (TLRPC.TL_error)paramTLObject;
      }
      i = i2;
      if (j == 0)
      {
        if ((localObject1 == null) && (!(paramTLObject instanceof TLRPC.TL_error))) {
          break label726;
        }
        i = 1;
        RequestDelegate localRequestDelegate = paramRPCRequest.completionBlock;
        if (localObject1 == null) {
          break label717;
        }
        localObject3 = localObject1;
        label318:
        localRequestDelegate.run(null, (TLRPC.TL_error)localObject3);
      }
      label328:
      m = i1;
      n = j;
      k = i;
      if (localObject1 != null)
      {
        m = i1;
        n = j;
        k = i;
        if (((TLRPC.TL_error)localObject1).code == 401)
        {
          i = 0;
          i1 = 1;
          m = i;
          n = j;
          k = i1;
          if ((paramRPCRequest.connectionType & 0x1) != 0)
          {
            m = i;
            n = j;
            k = i1;
            if (UserConfig.isClientActivated())
            {
              onLogout();
              k = i1;
              n = j;
              m = i;
            }
          }
        }
      }
    }
    if (n == 0) {
      if ((m != 0) && (paramRPCRequest.initRequest) && (k == 0))
      {
        if (((paramRPCRequest.flags & 0x8) != 0) && (this.lastInitVersion != BuildVars.BUILD_VERSION))
        {
          this.lastInitVersion = BuildVars.BUILD_VERSION;
          saveSession();
          FileLog.e("TSMS", "init connection completed");
        }
      }
      else
      {
        label490:
        paramRPCRequest.completed = true;
        rpcCompleted(paramRPCRequest.messageId);
        label503:
        paramTLObject.freeResources();
        if (-1 < 0) {
          break label808;
        }
        processRequestQueue(0, -1);
      }
    }
    for (;;)
    {
      if (!(paramTLObject instanceof TLRPC.TL_gzip_packed)) {
        break label817;
      }
      paramTLObject = Utilities.decompress(((TLRPC.TL_gzip_packed)paramTLObject).packed_data, getRequestWithMessageId(paramRPCRequest.messageId), true);
      if (paramTLObject == null) {
        break;
      }
      processMessage(paramTLObject, paramRPCRequest);
      return;
      label555:
      j = i;
      if (k != 420) {
        break label273;
      }
      j = i;
      if ((paramRPCRequest.flags & 0x2) != 0) {
        break label273;
      }
      double d2 = 2.0D;
      double d1 = d2;
      if (((String)localObject1).contains("FLOOD_WAIT_"))
      {
        localObject1 = ((String)localObject1).replace("FLOOD_WAIT_", "");
        localObject3 = Pattern.compile("[0-9]+").matcher((CharSequence)localObject1);
        if (((Matcher)localObject3).find()) {
          localObject1 = ((Matcher)localObject3).group(0);
        }
      }
      try
      {
        i = Integer.parseInt((String)localObject1);
        localObject1 = Integer.valueOf(i);
      }
      catch (Exception localException)
      {
        for (;;)
        {
          Object localObject2 = null;
        }
      }
      d1 = d2;
      if (localObject1 != null) {
        d1 = ((Integer)localObject1).intValue();
      }
      d1 = Math.min(30.0D, d1);
      j = 1;
      paramRPCRequest.wait = true;
      paramRPCRequest.minStartTime = ((int)(System.currentTimeMillis() / 1000L + d1));
      paramRPCRequest.confirmed = false;
      break label273;
      label717:
      localObject3 = (TLRPC.TL_error)paramTLObject;
      break label318;
      label726:
      i = i2;
      if ((paramTLObject instanceof TLRPC.TL_ping)) {
        break label328;
      }
      i = i2;
      if (paramRPCRequest.completionBlock == null) {
        break label328;
      }
      paramRPCRequest.completionBlock.run(paramTLObject, null);
      i = i2;
      if (!(paramTLObject instanceof TLRPC.updates_Difference)) {
        break label328;
      }
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (ConnectionsManager.this.wakeLock.isHeld())
          {
            FileLog.e("TSMS", "release wakelock");
            ConnectionsManager.this.wakeLock.release();
          }
        }
      });
      i = i2;
      break label328;
      FileLog.e("TSMS", "rpc is init, but init connection already completed");
      break label490;
      paramRPCRequest.messageId = 0L;
      break label503;
      label808:
      processRequestQueue(0, 0);
    }
    label817:
    if ((paramTLObject instanceof TLRPC.TL_pong))
    {
      this.pingOK = true;
      this.lastPingTime = getCurrentTimeMillis();
      return;
    }
    if ((paramTLObject instanceof TLRPC.TL_auth_authorization))
    {
      UserConfig.token = ((TLRPC.TL_auth_authorization)paramTLObject).token;
      UserConfig.saveConfig(false);
      return;
    }
    if ((paramTLObject instanceof TLRPC.Updates))
    {
      MessagesController.getInstance().processUpdates((TLRPC.Updates)paramTLObject, false);
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          if (ConnectionsManager.this.wakeLock.isHeld())
          {
            FileLog.e("TSMS", "release wakelock");
            ConnectionsManager.this.wakeLock.release();
          }
        }
      });
      return;
    }
    FileLog.e("TSMS", "***** Error: unknown message class " + paramTLObject);
  }
  
  private void processRequestQueue(int paramInt1, int paramInt2)
  {
    int i = 0;
    int j = 0;
    paramInt2 = 0;
    int i2 = (int)(System.currentTimeMillis() / 1000L);
    Iterator localIterator = this.runningRequests.iterator();
    RPCRequest localRPCRequest;
    int n;
    label235:
    label249:
    label461:
    label467:
    label530:
    Object localObject;
    while (localIterator.hasNext())
    {
      localRPCRequest = (RPCRequest)localIterator.next();
      if ((localRPCRequest.connectionType & 0x1) != 0)
      {
        m = i + 1;
        n = j;
        k = paramInt2;
      }
      for (;;)
      {
        if ((localRPCRequest.flags & 0x10) == 0) {
          break label235;
        }
        paramInt2 = localRPCRequest.startTime;
        if ((paramInt2 == 0) || (paramInt2 >= i2 - 30)) {
          break label235;
        }
        FileLog.e("TSMS", "move " + localRPCRequest.rawRequest + " to requestQueue");
        this.requestQueue.add(localRPCRequest);
        this.runningRequests.remove(localRPCRequest);
        paramInt2 = k;
        i = m;
        j = n;
        break;
        if ((localRPCRequest.connectionType & 0x4) != 0)
        {
          n = j + 1;
          k = paramInt2;
          m = i;
        }
        else
        {
          k = paramInt2;
          m = i;
          n = j;
          if ((localRPCRequest.connectionType & 0x2) != 0)
          {
            k = paramInt2 + 1;
            m = i;
            n = j;
          }
        }
      }
      float f;
      if ((localRPCRequest.connectionType & 0x1) != 0)
      {
        f = 8.0F;
        if ((localRPCRequest.flags & paramInt1) == 0) {
          break label461;
        }
      }
      for (int i1 = 1;; i1 = 0)
      {
        if (i1 == 0)
        {
          paramInt2 = k;
          i = m;
          j = n;
          if (Math.abs(i2 - localRPCRequest.startTime) <= f) {
            break;
          }
          if ((i2 < localRPCRequest.minStartTime) && ((localRPCRequest.failedByFloodWait == 0) || (localRPCRequest.minStartTime - i2 <= localRPCRequest.failedByFloodWait)))
          {
            paramInt2 = k;
            i = m;
            j = n;
            if (localRPCRequest.failedByFloodWait != 0) {
              break;
            }
            paramInt2 = k;
            i = m;
            j = n;
            if (Math.abs(i2 - localRPCRequest.minStartTime) < 60) {
              break;
            }
          }
        }
        if ((i1 != 0) || (localRPCRequest.connectionToken <= 0)) {
          break label530;
        }
        if (((localRPCRequest.connectionType & 0x1) == 0) || (localRPCRequest.connectionToken != getConnectionToken())) {
          break label467;
        }
        FileLog.d("TSMS", "Request token is valid, not retrying " + localRPCRequest.rawRequest);
        paramInt2 = k;
        i = m;
        j = n;
        break;
        f = 30.0F;
        break label249;
      }
      if ((getConnectionToken() != 0) && (localRPCRequest.connectionToken == getConnectionToken()))
      {
        FileLog.d("TSMS", "Request download token is valid, not retrying " + localRPCRequest.rawRequest);
        paramInt2 = k;
        i = m;
        j = n;
      }
      else
      {
        localRPCRequest.retryCount += 1;
        if ((!localRPCRequest.salt) && ((localRPCRequest.connectionType & 0x2) != 0))
        {
          paramInt2 = 10;
          if ((localRPCRequest.flags & 0x20) == 0) {
            if (!localRPCRequest.wait) {
              break label692;
            }
          }
          label692:
          for (paramInt2 = 1;; paramInt2 = 6)
          {
            if (localRPCRequest.retryCount < paramInt2) {
              break label698;
            }
            FileLog.e("TSMS", "timed out " + localRPCRequest.rawRequest);
            localObject = new TLRPC.TL_error();
            ((TLRPC.TL_error)localObject).code = -123;
            ((TLRPC.TL_error)localObject).text = "RETRY_LIMIT";
            if (localRPCRequest.completionBlock != null) {
              localRPCRequest.completionBlock.run(null, (TLRPC.TL_error)localObject);
            }
            this.runningRequests.remove(localRPCRequest);
            paramInt2 = k;
            i = m;
            j = n;
            break;
          }
        }
        label698:
        localRPCRequest.startTime = i2;
        sendMessagesToTransport(localRPCRequest);
        paramInt2 = k;
        i = m;
        j = n;
      }
    }
    localIterator = this.requestQueue.iterator();
    int k = i;
    int m = paramInt2;
    while (localIterator.hasNext())
    {
      localRPCRequest = (RPCRequest)localIterator.next();
      if ((!this.refreshingToken) || (this.refreshingTokenTime == 0L) || (this.refreshingTokenTime <= System.currentTimeMillis() - 60000L)) {
        if (localRPCRequest.cancelled)
        {
          this.requestQueue.remove(localRPCRequest);
        }
        else
        {
          if ((localRPCRequest.connectionType & 0x1) != 0)
          {
            if (k >= 60) {
              continue;
            }
            paramInt2 = k + 1;
            i = j;
            paramInt1 = m;
            label844:
            if ((localRPCRequest.flags & 0x4) == 0) {
              break label1082;
            }
            j = 1;
            label857:
            if (j != 0) {
              break label1088;
            }
          }
          label1082:
          label1088:
          for (boolean bool = true;; bool = false)
          {
            localObject = new SerializedData(bool);
            localRPCRequest.rpcRequest.serializeToStream((AbstractSerializedData)localObject);
            n = ((SerializedData)localObject).length();
            if (n == 0) {
              break label1094;
            }
            if (j != 0) {}
            m = paramInt1;
            k = paramInt2;
            j = i;
            if (localRPCRequest.connectionToken == getConnectionToken()) {
              break;
            }
            localRPCRequest.messageId = generateMessageId();
            localRPCRequest.serializedLength = n;
            localRPCRequest.startTime = ((int)(System.currentTimeMillis() / 1000L));
            localRPCRequest.connectionToken = getConnectionToken();
            this.requestQueue.remove(localRPCRequest);
            this.runningRequests.add(localRPCRequest);
            sendMessagesToTransport(localRPCRequest);
            m = paramInt1;
            k = paramInt2;
            j = i;
            break;
            if ((localRPCRequest.flags & 0x4) != 0)
            {
              if ((1 == 0) || (j >= 5)) {
                break;
              }
              i = j + 1;
              paramInt1 = m;
              paramInt2 = k;
              break label844;
            }
            paramInt1 = m;
            paramInt2 = k;
            i = j;
            if ((localRPCRequest.connectionType & 0x2) == 0) {
              break label844;
            }
            if ((1 == 0) || (m >= 5)) {
              break;
            }
            paramInt1 = m + 1;
            paramInt2 = k;
            i = j;
            break label844;
            j = 0;
            break label857;
          }
          label1094:
          FileLog.e("TSMS", "***** Couldn't serialize " + localRPCRequest.rawRequest);
          m = paramInt1;
          k = paramInt2;
          j = i;
        }
      }
    }
  }
  
  private void reSendRequest(RPCRequest paramRPCRequest)
  {
    Iterator localIterator = this.runningRequests.iterator();
    while (localIterator.hasNext())
    {
      RPCRequest localRPCRequest = (RPCRequest)localIterator.next();
      if (paramRPCRequest.token == localRPCRequest.token) {
        this.runningRequests.remove(localRPCRequest);
      }
    }
    paramRPCRequest.token = this.lastRequestToken.getAndIncrement();
    paramRPCRequest.connectionToken = 0;
    paramRPCRequest.retryCount = 0;
    paramRPCRequest.startTime = 0;
    this.requestQueue.add(paramRPCRequest);
  }
  
  private void refreshToken()
  {
    if ((this.refreshingToken) && (this.refreshingTokenTime != 0L) && (this.refreshingTokenTime > System.currentTimeMillis() - 60000L)) {
      return;
    }
    this.refreshingToken = true;
    this.refreshingTokenTime = System.currentTimeMillis();
    TLRPC.TL_refreshToken localTL_refreshToken = new TLRPC.TL_refreshToken();
    RPCRequest localRPCRequest = new RPCRequest();
    localRPCRequest.messageId = generateMessageId();
    localRPCRequest.startTime = ((int)(System.currentTimeMillis() / 1000L));
    localRPCRequest.connectionToken = getConnectionToken();
    localRPCRequest.token = this.lastRequestToken.getAndIncrement();
    localRPCRequest.connectionType = 1;
    localRPCRequest.rawRequest = localTL_refreshToken;
    localRPCRequest.rpcRequest = wrapInLayer(localTL_refreshToken, localRPCRequest);
    sendMessagesToTransport(localRPCRequest);
  }
  
  private void removeRequestInClass(final Long paramLong)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        Object localObject = (Integer)ConnectionsManager.this.requestsByClass.get(paramLong);
        if (localObject != null)
        {
          localObject = (ArrayList)ConnectionsManager.this.requestsByGuids.get(localObject);
          if (localObject != null) {
            ((ArrayList)localObject).remove(paramLong);
          }
        }
      }
    });
  }
  
  private void resumeNetworkInternal(boolean paramBoolean)
  {
    if (paramBoolean)
    {
      if (this.paused)
      {
        this.lastPauseTime = getCurrentTimeMillis();
        this.nextSleepTimeout = 30000;
        this.paused = false;
        FileLog.e("TSMS", "wakeup network in background");
      }
      while (this.lastPauseTime == 0L) {
        return;
      }
      this.lastPauseTime = getCurrentTimeMillis();
      this.paused = false;
      FileLog.e("TSMS", "reset sleep timeout");
      return;
    }
    this.lastPauseTime = 0L;
    this.paused = false;
  }
  
  private void rpcCompleted(final long paramLong)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Iterator localIterator = ConnectionsManager.this.runningRequests.iterator();
        while (localIterator.hasNext())
        {
          RPCRequest localRPCRequest = (RPCRequest)localIterator.next();
          ConnectionsManager.this.removeRequestInClass(Long.valueOf(localRPCRequest.token));
          if (localRPCRequest.respondsToMessageId(paramLong))
          {
            localRPCRequest.rawRequest.freeResources();
            localRPCRequest.rpcRequest.freeResources();
            ConnectionsManager.this.runningRequests.remove(localRPCRequest);
          }
        }
      }
    });
  }
  
  private void saveSession()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        SerializedData localSerializedData;
        try
        {
          SharedPreferences.Editor localEditor = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", 0).edit();
          localEditor.putInt("datacenterSetId", ConnectionsManager.this.isTestBackend);
          if (ConnectionsManager.this.datacenterWithId(ConnectionsManager.this.currentDatacenterId) == null) {
            break label255;
          }
          localEditor.putInt("currentDatacenterId", ConnectionsManager.this.currentDatacenterId);
          localEditor.putInt("timeDifference", ConnectionsManager.this.timeDifference);
          localEditor.putInt("lastDcUpdateTime", ConnectionsManager.this.lastDcUpdateTime);
          if (ConnectionsManager.this.datacenters.isEmpty()) {
            break label243;
          }
          localSerializedData = new SerializedData();
          localSerializedData.writeInt32(ConnectionsManager.this.datacenters.size());
          Iterator localIterator = ConnectionsManager.this.datacenters.values().iterator();
          while (localIterator.hasNext())
          {
            TLRPC.TL_dcOption localTL_dcOption = (TLRPC.TL_dcOption)localIterator.next();
            localSerializedData.writeInt32(localTL_dcOption.id);
            localSerializedData.writeString(localTL_dcOption.ip_address);
            localSerializedData.writeInt32(localTL_dcOption.port);
            localSerializedData.writeInt32(localTL_dcOption.flags);
          }
          localException.putString("datacenters", Base64.encodeToString(localSerializedData.toByteArray(), 0));
        }
        catch (Exception localException)
        {
          FileLog.e("TSMS", localException);
          return;
        }
        localSerializedData.cleanup();
        for (;;)
        {
          localException.commit();
          return;
          label243:
          localException.remove("datacenters");
          continue;
          label255:
          localException.remove("datacenters");
          localException.remove("sessionsToDestroy");
          localException.remove("currentDatacenterId");
          localException.remove("timeDifference");
        }
      }
    });
  }
  
  private void sendMessagesToTransport(final RPCRequest paramRPCRequest)
  {
    Log.i("TSMS", "Request Class: " + paramRPCRequest.rawRequest.getClass().toString());
    new Thread(new Runnable()
    {
      public void run()
      {
        Object localObject3;
        if (BuildVars.DEVELOP_VERSION)
        {
          localObject3 = new TLRPC.TL_clientDebugRequest();
          ((TLRPC.TL_clientRequest)localObject3).json = AndroidUtilities.toJakson(paramRPCRequest.rawRequest);
        }
        for (;;)
        {
          ((TLRPC.TL_clientRequest)localObject3).imei = UserConfig.imei;
          ((TLRPC.TL_clientRequest)localObject3).token = UserConfig.token;
          Object localObject1 = new SerializedData(false);
          if (((paramRPCRequest.flags & 0x8) == 0) && (ConnectionsManager.this.lastInitVersion != BuildVars.BUILD_VERSION)) {
            paramRPCRequest.rpcRequest.serializeToStream((AbstractSerializedData)localObject1);
          }
          try
          {
            for (;;)
            {
              ((TLRPC.TL_clientRequest)localObject3).packed_data = ((SerializedData)localObject1).toByteArray();
              if ((paramRPCRequest.flags & 0x8) == 0) {
                break label414;
              }
              if (!BuildVars.DEBUG_VERSION) {
                break label333;
              }
              if (!(paramRPCRequest.rawRequest instanceof TLRPC.TL_ping)) {
                break label299;
              }
              localObject1 = new HelperHttp(ConnectionsManager.this.getCurrentAddress(0), ConnectionsManager.this.getCurrentPort(0), "/eitaa/index.php");
              localSerializedData = new SerializedData();
              ((TLRPC.TL_clientRequest)localObject3).serializeToStream(localSerializedData);
              localObject1 = ((HelperHttp)localObject1).send(localSerializedData.toByteArray());
              if (localObject1 != null) {
                break label398;
              }
              ConnectionsManager.this.nextAddressOrPort(0);
              ConnectionsManager.this.tcpConnectionClosed();
              if (!(paramRPCRequest.rawRequest instanceof TLRPC.TL_ping)) {
                break label367;
              }
              ConnectionsManager.access$602(ConnectionsManager.this, ConnectionsManager.this.getCurrentTimeMillis());
              ConnectionsManager.this.pingOK = false;
              ConnectionsManager.onConnectionStateChanged(1);
              return;
              localObject3 = new TLRPC.TL_clientRequestAPK();
              ((TLRPC.TL_clientRequest)localObject3).isWifi = ConnectionsManager.isConnectedToWiFi();
              ((TLRPC.TL_clientRequest)localObject3).isData = ConnectionsManager.isConnectedToData();
              ((TLRPC.TL_clientRequest)localObject3).appPause = ConnectionsManager.this.getAppPaused();
              break;
              paramRPCRequest.rawRequest.serializeToStream((AbstractSerializedData)localObject1);
            }
          }
          catch (Exception localException)
          {
            label299:
            label333:
            label367:
            do
            {
              for (;;)
              {
                FileLog.e("TSMS", localException);
                continue;
                localObject2 = new HelperHttp(ConnectionsManager.this.getCurrentAddress(0), ConnectionsManager.this.getCurrentPort(0), ConnectionsManager.this.getServerURL());
                continue;
                localObject2 = new HelperHttp(ConnectionsManager.this.getCurrentAddress(0), ConnectionsManager.this.getCurrentPort(0), ConnectionsManager.this.getServerURL());
              }
              ConnectionsManager.this.generatePing();
            } while ((paramRPCRequest.connectionType & 0x1) == 0);
            ConnectionsManager.this.reSendRequest(paramRPCRequest);
            return;
          }
        }
        label398:
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ConnectionsManager.this.onConnectionDataReceived(ConnectionsManager.3.this.val$request, localObject2);
            localObject2.reuse();
          }
        });
        return;
        label414:
        int i = 0;
        if (((paramRPCRequest.connectionType & 0x2) != 0) || ((paramRPCRequest.connectionType & 0x10002) != 0) || ((paramRPCRequest.connectionType & 0x4) != 0)) {
          i = 2;
        }
        if (((paramRPCRequest.rawRequest instanceof TLRPC.TL_messages_sendMedia)) || ((paramRPCRequest.rawRequest instanceof TLRPC.TL_channels_editPhoto)) || ((paramRPCRequest.rawRequest instanceof TLRPC.TL_messages_editChatPhoto)) || ((paramRPCRequest.rawRequest instanceof TLRPC.TL_photos_uploadProfilePhoto))) {
          i = 2;
        }
        final Object localObject2 = new HelperHttp(ConnectionsManager.this.getCurrentAddress(i), ConnectionsManager.this.getCurrentPort(i), ConnectionsManager.this.getServerURL());
        SerializedData localSerializedData = new SerializedData();
        ((TLRPC.TL_clientRequest)localObject3).serializeToStream(localSerializedData);
        localObject2 = ((HelperHttp)localObject2).send(localSerializedData.toByteArray());
        if (localObject2 == null)
        {
          ConnectionsManager.this.nextAddressOrPort(i);
          paramRPCRequest.connectionToken = 0;
          ConnectionsManager.this.tcpConnectionClosed();
          ConnectionsManager.this.generatePing();
          ConnectionsManager.this.reSendRequest(paramRPCRequest);
          return;
        }
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            ConnectionsManager.this.onConnectionDataReceived(ConnectionsManager.3.this.val$request, localObject2);
            localObject2.reuse();
          }
        });
      }
    }).start();
  }
  
  protected static boolean useIpv6Address()
  {
    return false;
  }
  
  private TLObject wrapInLayer(TLObject paramTLObject, RPCRequest paramRPCRequest)
  {
    Object localObject = paramTLObject;
    if (this.lastInitVersion != BuildVars.BUILD_VERSION)
    {
      paramRPCRequest.initRequest = true;
      paramRPCRequest = new TLRPC.initConnection();
      paramRPCRequest.query = paramTLObject;
      paramRPCRequest.api_id = BuildVars.APP_ID;
    }
    try
    {
      paramRPCRequest.lang_code = LocaleController.getInstance().getLocaleString(LocaleController.getInstance().getSystemDefaultLocale());
      if (paramRPCRequest.lang_code.length() == 0) {
        paramRPCRequest.lang_code = "en";
      }
      paramRPCRequest.device_model = (Build.MANUFACTURER + Build.MODEL);
      localObject = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
      paramRPCRequest.app_version = (((PackageInfo)localObject).versionName + " (" + ((PackageInfo)localObject).versionCode + ")");
      paramRPCRequest.system_version = ("SDK " + Build.VERSION.SDK_INT);
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e("TSMS", localException);
        paramRPCRequest.lang_code = "en";
        paramRPCRequest.device_model = "Android unknown";
        paramRPCRequest.app_version = "App version unknown";
        paramRPCRequest.system_version = ("SDK " + Build.VERSION.SDK_INT);
      }
    }
    if ((paramRPCRequest.lang_code == null) || (paramRPCRequest.lang_code.length() == 0)) {
      paramRPCRequest.lang_code = "en";
    }
    if ((paramRPCRequest.device_model == null) || (paramRPCRequest.device_model.length() == 0)) {
      paramRPCRequest.device_model = "Android unknown";
    }
    if ((paramRPCRequest.app_version == null) || (paramRPCRequest.app_version.length() == 0)) {
      paramRPCRequest.app_version = "App version unknown";
    }
    if ((paramRPCRequest.system_version == null) || (paramRPCRequest.system_version.length() == 0)) {
      paramRPCRequest.system_version = "SDK Unknown";
    }
    localObject = new TLRPC.invokeWithLayer();
    ((TLRPC.invokeWithLayer)localObject).query = paramRPCRequest;
    FileLog.d("wrap in layer", "" + paramTLObject);
    return (TLObject)localObject;
  }
  
  public void addAddressAndPort(String paramString, int paramInt1, int paramInt2)
  {
    if ((paramInt2 & 0x2) != 0) {}
    for (ArrayList localArrayList = this.addressesIpv4Download; localArrayList.contains(paramString); localArrayList = this.addressesIpv4) {
      return;
    }
    localArrayList.add(paramString);
    this.ports.put(paramString, Integer.valueOf(paramInt1));
  }
  
  public void applyCountryPortNumber(String paramString) {}
  
  public void applyDatacenterAddress(int paramInt1, String paramString, int paramInt2) {}
  
  public void bindRequestToGuid(final int paramInt1, final int paramInt2)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        ArrayList localArrayList = (ArrayList)ConnectionsManager.this.requestsByGuids.get(Integer.valueOf(paramInt2));
        if (localArrayList != null)
        {
          localArrayList.add(Long.valueOf(paramInt1));
          ConnectionsManager.this.requestsByClass.put(Long.valueOf(paramInt1), Integer.valueOf(paramInt2));
        }
      }
    });
  }
  
  public void cancelRequest(long paramLong, boolean paramBoolean)
  {
    cancelRpc(paramLong, false);
  }
  
  public void cancelRequestsForGuid(int paramInt)
  {
    ArrayList localArrayList = (ArrayList)this.requestsByGuids.get(Integer.valueOf(paramInt));
    if (localArrayList != null)
    {
      int i = 0;
      while (i < localArrayList.size())
      {
        cancelRequest(((Long)localArrayList.get(i)).longValue(), true);
        i += 1;
      }
      this.requestsByGuids.remove(Integer.valueOf(paramInt));
    }
  }
  
  public void cleanup()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Iterator localIterator = ConnectionsManager.this.requestQueue.iterator();
        RPCRequest localRPCRequest;
        TLRPC.TL_error localTL_error;
        while (localIterator.hasNext())
        {
          localRPCRequest = (RPCRequest)localIterator.next();
          if ((localRPCRequest.flags & 0x8) == 0)
          {
            ConnectionsManager.this.requestQueue.remove(localRPCRequest);
            if (localRPCRequest.completionBlock != null)
            {
              localTL_error = new TLRPC.TL_error();
              localTL_error.code = 64536;
              localTL_error.text = "";
              localRPCRequest.completionBlock.run(null, localTL_error);
            }
          }
        }
        localIterator = ConnectionsManager.this.runningRequests.iterator();
        while (localIterator.hasNext())
        {
          localRPCRequest = (RPCRequest)localIterator.next();
          if ((localRPCRequest.flags & 0x8) == 0)
          {
            ConnectionsManager.this.runningRequests.remove(localRPCRequest);
            if (localRPCRequest.completionBlock != null)
            {
              localTL_error = new TLRPC.TL_error();
              localTL_error.code = 64536;
              localTL_error.text = "";
              localRPCRequest.completionBlock.run(null, localTL_error);
            }
          }
        }
        ConnectionsManager.this.quickAckIdToRequestIds.clear();
        ConnectionsManager.this.saveSession();
      }
    });
  }
  
  public TLRPC.TL_dcOption datacenterWithId(int paramInt)
  {
    if (paramInt == Integer.MAX_VALUE) {
      return (TLRPC.TL_dcOption)this.datacenters.get(Integer.valueOf(this.currentDatacenterId));
    }
    return (TLRPC.TL_dcOption)this.datacenters.get(Integer.valueOf(paramInt));
  }
  
  public TLObject deserialize(TLObject paramTLObject, AbstractSerializedData paramAbstractSerializedData, boolean paramBoolean)
  {
    i = 0;
    try
    {
      int j = paramAbstractSerializedData.readInt32(paramBoolean);
      i = j;
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        Object localObject1;
        FileLog.e("TSMS", localException1);
      }
      return localException1;
    }
    localObject1 = null;
    try
    {
      localObject2 = TLClassStore.Instance().TLdeserialize(paramAbstractSerializedData, i, paramBoolean);
      localObject1 = localObject2;
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        Object localObject2;
        FileLog.e("TSMS", localException2);
      }
      FileLog.d("TSMS", String.format(Locale.US, "***** Not found request to parse message: %x", new Object[] { Integer.valueOf(i) }));
    }
    localObject2 = localObject1;
    if ((localObject1 != null) || (paramTLObject != null))
    {
      try
      {
        paramTLObject = paramTLObject.deserializeResponse(paramAbstractSerializedData, i, paramBoolean);
        localObject1 = paramTLObject;
      }
      catch (Exception paramTLObject)
      {
        for (;;)
        {
          FileLog.e("TSMS", paramTLObject);
        }
      }
      localObject2 = localObject1;
      if (localObject1 == null)
      {
        FileLog.e("TSMS", String.format(Locale.US, "***** Error parsing message: %x", new Object[] { Integer.valueOf(i) }));
        localObject2 = localObject1;
      }
      return (TLObject)localObject2;
    }
  }
  
  protected TLObject deserialize(TLObject paramTLObject, NativeByteBuffer paramNativeByteBuffer, boolean paramBoolean)
  {
    i = 0;
    try
    {
      int j = paramNativeByteBuffer.readInt32(paramBoolean);
      i = j;
    }
    catch (Exception localException1)
    {
      for (;;)
      {
        Object localObject1;
        FileLog.e("TSMS", localException1);
      }
      return localException1;
    }
    localObject1 = null;
    try
    {
      localObject2 = TLClassStore.Instance().TLdeserialize(paramNativeByteBuffer, i, paramBoolean);
      localObject1 = localObject2;
    }
    catch (Exception localException2)
    {
      for (;;)
      {
        Object localObject2;
        FileLog.e("TSMS", localException2);
      }
      FileLog.d("TSMS", String.format(Locale.US, "***** Not found request to parse message: %x", new Object[] { Integer.valueOf(i) }));
    }
    localObject2 = localObject1;
    if ((localObject1 != null) || (paramTLObject != null))
    {
      try
      {
        paramTLObject = paramTLObject.deserializeResponse(paramNativeByteBuffer, i, paramBoolean);
        localObject1 = paramTLObject;
      }
      catch (Exception paramTLObject)
      {
        for (;;)
        {
          FileLog.e("TSMS", paramTLObject);
        }
      }
      localObject2 = localObject1;
      if (localObject1 == null)
      {
        FileLog.e("TSMS", String.format(Locale.US, "***** Error parsing message: %x", new Object[] { Integer.valueOf(i) }));
        localObject2 = localObject1;
      }
      return (TLObject)localObject2;
    }
  }
  
  protected TLObject deserialize(TLObject paramTLObject, String paramString, boolean paramBoolean)
  {
    int i = 0;
    SerializedJson localSerializedJson = new SerializedJson(paramString);
    try
    {
      long l = Long.parseLong(localSerializedJson.readString("constructor", false), 16);
      i = (int)l;
    }
    catch (NumberFormatException paramString)
    {
      for (;;)
      {
        FileLog.e("TSMS", "read long error");
      }
    }
    paramString = null;
    try
    {
      localObject = TLClassStore.Instance().TLdeserialize(localSerializedJson, i, paramBoolean);
      paramString = (String)localObject;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        Object localObject;
        FileLog.e("TSMS", localException);
      }
      FileLog.d("TSMS", String.format(Locale.US, "***** Not found request to parse message: %x", new Object[] { Integer.valueOf(i) }));
    }
    localObject = paramString;
    if ((paramString != null) || (paramTLObject != null))
    {
      try
      {
        paramTLObject = paramTLObject.deserializeResponse(localSerializedJson, paramBoolean);
        paramString = paramTLObject;
      }
      catch (Exception paramTLObject)
      {
        for (;;)
        {
          FileLog.e("TSMS", paramTLObject);
        }
      }
      localObject = paramString;
      if (paramString == null)
      {
        FileLog.e("TSMS", String.format(Locale.US, "***** Error parsing message: %x", new Object[] { Integer.valueOf(i) }));
        localObject = paramString;
      }
      return (TLObject)localObject;
    }
    return paramString;
  }
  
  public int generateClassGuid()
  {
    int i = this.lastClassGuid;
    this.lastClassGuid = (i + 1);
    return i;
  }
  
  public long generateMessageId()
  {
    long l2 = ((System.currentTimeMillis() + this.timeDifference * 1000.0D) * 4.294967296E9D / 1000.0D);
    long l1 = l2;
    if (l2 <= this.lastOutgoingMessageId) {}
    for (l1 = this.lastOutgoingMessageId + 1L; l1 % 4L != 0L; l1 += 1L) {}
    this.lastOutgoingMessageId = l1;
    return l1;
  }
  
  public boolean getAppPaused()
  {
    return this.appPaused;
  }
  
  public int getConnectionState()
  {
    if ((this.connectionState == 3) && (this.isUpdating)) {
      return 4;
    }
    return this.connectionState;
  }
  
  public String getCurrentAddress(int paramInt)
  {
    int i;
    if ((paramInt & 0x2) != 0) {
      i = this.currentAddressNumIpv4Download;
    }
    for (ArrayList localArrayList = this.addressesIpv4Download; localArrayList.isEmpty(); localArrayList = this.addressesIpv4)
    {
      return null;
      i = this.currentAddressNumIpv4;
    }
    int j = i;
    if (i >= localArrayList.size())
    {
      j = 0;
      if ((paramInt & 0x2) == 0) {
        break label75;
      }
      this.currentAddressNumIpv4Download = 0;
    }
    for (;;)
    {
      return (String)localArrayList.get(j);
      label75:
      this.currentAddressNumIpv4 = 0;
    }
  }
  
  public int getCurrentPort(int paramInt)
  {
    if (this.ports.isEmpty())
    {
      if (this.overridePort == -1) {
        return 443;
      }
      return this.overridePort;
    }
    Object localObject = this.defaultPorts;
    if (this.overridePort == 8888) {
      localObject = this.defaultPorts8888;
    }
    int i;
    int j;
    if ((paramInt & 0x2) != 0)
    {
      i = this.currentPortNumIpv4Download;
      j = i;
      if (i >= this.defaultPorts.length)
      {
        j = 0;
        if ((paramInt & 0x2) == 0) {
          break label115;
        }
        this.currentPortNumIpv4Download = 0;
      }
    }
    for (;;)
    {
      i = localObject[j];
      if (i != -1) {
        return i;
      }
      if (this.overridePort == -1) {
        break label123;
      }
      return this.overridePort;
      i = this.currentPortNumIpv4;
      break;
      label115:
      this.currentPortNumIpv4 = 0;
    }
    label123:
    localObject = getCurrentAddress(paramInt);
    return ((Integer)this.ports.get(localObject)).intValue();
    return i;
  }
  
  public int getCurrentTime()
  {
    return (int)(System.currentTimeMillis() / 1000L) + this.timeDifference;
  }
  
  public long getCurrentTimeMillis()
  {
    return System.currentTimeMillis();
  }
  
  public CharSequence getHostName()
  {
    return datacenterWithId(this.currentDatacenterId).ip_address;
  }
  
  public long getPauseTime()
  {
    return this.lastPauseTime;
  }
  
  public boolean getPingStatus()
  {
    if (this.lastPingTime < System.currentTimeMillis() - 19000L)
    {
      this.lastPingTime = getCurrentTimeMillis();
      generatePing();
    }
    return this.pingOK;
  }
  
  public TLObject getRequestWithMessageId(long paramLong)
  {
    Iterator localIterator = this.runningRequests.iterator();
    while (localIterator.hasNext())
    {
      RPCRequest localRPCRequest = (RPCRequest)localIterator.next();
      if (paramLong == localRPCRequest.messageId) {
        return localRPCRequest.rawRequest;
      }
    }
    return null;
  }
  
  public String getServerURL()
  {
    if (BuildVars.DEBUG_VERSION) {
      return "/eitaa/index.php";
    }
    return "/eitaa/index.php";
  }
  
  public int getTimeDifference()
  {
    return this.timeDifference;
  }
  
  long getTimeFromMsgId(long paramLong)
  {
    return (paramLong / 4.294967296E9D * 1000.0D);
  }
  
  public void init(int paramInt1, int paramInt2, int paramInt3, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, int paramInt4, boolean paramBoolean)
  {
    checkConnection();
    paramString1 = new BroadcastReceiver()
    {
      public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
      {
        ConnectionsManager.this.checkConnection();
      }
    };
    paramString2 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    ApplicationLoader.applicationContext.registerReceiver(paramString1, paramString2);
  }
  
  public void nextAddressOrPort(int paramInt)
  {
    int j;
    int i;
    if ((paramInt & 0x2) != 0)
    {
      j = this.currentPortNumIpv4Download;
      i = this.currentAddressNumIpv4Download;
    }
    for (ArrayList localArrayList = this.addressesIpv4Download; j + 1 < this.defaultPorts.length; localArrayList = this.addressesIpv4)
    {
      j += 1;
      if ((paramInt & 0x2) == 0) {
        break label98;
      }
      this.currentPortNumIpv4Download = j;
      this.currentAddressNumIpv4Download = i;
      return;
      j = this.currentPortNumIpv4;
      i = this.currentAddressNumIpv4;
    }
    if (i + 1 < localArrayList.size()) {
      i += 1;
    }
    for (;;)
    {
      j = 0;
      break;
      i = 0;
    }
    label98:
    this.currentPortNumIpv4 = j;
    this.currentAddressNumIpv4 = i;
  }
  
  public void pauseNetwork()
  {
    if (this.lastPauseTime != 0L) {
      return;
    }
    this.lastPauseTime = getCurrentTimeMillis();
  }
  
  public void replaceAddressesAndPorts(ArrayList<String> paramArrayList, HashMap<String, Integer> paramHashMap, int paramInt)
  {
    if ((paramInt & 0x2) != 0) {}
    for (Object localObject = this.addressesIpv4Download;; localObject = this.addressesIpv4)
    {
      localObject = ((ArrayList)localObject).iterator();
      while (((Iterator)localObject).hasNext())
      {
        String str = (String)((Iterator)localObject).next();
        this.ports.remove(str);
      }
    }
    if ((paramInt & 0x2) != 0) {
      this.addressesIpv4Download = paramArrayList;
    }
    for (;;)
    {
      this.ports.putAll(paramHashMap);
      return;
      this.addressesIpv4 = paramArrayList;
    }
  }
  
  public void resumeNetworkMaybe()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ConnectionsManager.this.resumeNetworkInternal(true);
      }
    });
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, 0);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, int paramInt)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, paramInt, Integer.MAX_VALUE, 1, true);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, int paramInt1, int paramInt2)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, null, paramInt1, Integer.MAX_VALUE, paramInt2, true);
  }
  
  public int sendRequest(TLObject paramTLObject, RequestDelegate paramRequestDelegate, QuickAckDelegate paramQuickAckDelegate, int paramInt)
  {
    return sendRequest(paramTLObject, paramRequestDelegate, paramQuickAckDelegate, paramInt, Integer.MAX_VALUE, 1, true);
  }
  
  public int sendRequest(final TLObject paramTLObject, final RequestDelegate paramRequestDelegate, final QuickAckDelegate paramQuickAckDelegate, final int paramInt1, final int paramInt2, final int paramInt3, final boolean paramBoolean)
  {
    final int i = this.lastRequestToken.getAndIncrement();
    if ((paramTLObject == null) || ((!UserConfig.isClientActivated()) && ((paramInt1 & 0x8) == 0)))
    {
      FileLog.e("TSMS", "can't do request without login " + paramTLObject);
      return 0;
    }
    if (this.requestInvalid.contains(Integer.valueOf(paramTLObject.getClass().hashCode())))
    {
      FileLog.w("TSMS", "Request is in INVALID_CONSTRUCTOR." + paramTLObject);
      return 0;
    }
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        RPCRequest localRPCRequest = new RPCRequest();
        localRPCRequest.token = i;
        localRPCRequest.flags = paramInt1;
        localRPCRequest.connectionType = paramInt3;
        localRPCRequest.runningDatacenterId = paramInt2;
        localRPCRequest.rawRequest = paramTLObject;
        localRPCRequest.rpcRequest = ConnectionsManager.this.wrapInLayer(paramTLObject, localRPCRequest);
        localRPCRequest.completionBlock = paramRequestDelegate;
        localRPCRequest.quickAckBlock = paramQuickAckDelegate;
        ConnectionsManager.this.requestQueue.add(localRPCRequest);
        if (paramBoolean) {
          ConnectionsManager.this.processRequestQueue(0, 0);
        }
      }
    });
    return i;
  }
  
  public void setAppPaused(boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool;
    if (paramBoolean1 != this.appPaused)
    {
      if (!isNetworkOnline())
      {
        bool = true;
        ScheduleController.scheduleGetDifference(Boolean.valueOf(paramBoolean1), Boolean.valueOf(bool));
      }
    }
    else
    {
      if (!paramBoolean2)
      {
        this.appPaused = paramBoolean1;
        FileLog.d("TSMS", "app paused = " + paramBoolean1);
      }
      if (!paramBoolean1) {
        break label100;
      }
      if (this.lastPauseTime == 0L) {
        this.lastPauseTime = getCurrentTimeMillis();
      }
      pauseNetwork();
      ScheduleController.periodBackgroundSchedule(Boolean.valueOf(true));
    }
    label100:
    while (this.appPaused)
    {
      return;
      bool = false;
      break;
    }
    FileLog.e("TSMS", "reset app pause time");
    if ((ScheduleController.appPausedbackgroundSchedule) && (this.lastPauseTime != 0L) && (System.currentTimeMillis() - this.lastPauseTime > 5000L)) {
      ContactsController.getInstance().checkContacts();
    }
    this.lastPauseTime = 0L;
    ScheduleController.lastPauseTime = System.currentTimeMillis();
    ScheduleController.periodBackgroundSchedule(Boolean.valueOf(false));
    resumeNetworkInternal(false);
  }
  
  public void setIsUpdating(final boolean paramBoolean)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (ConnectionsManager.this.isUpdating == paramBoolean) {}
        do
        {
          return;
          ConnectionsManager.access$3002(ConnectionsManager.this, paramBoolean);
        } while (ConnectionsManager.this.connectionState != 3);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, new Object[0]);
      }
    });
  }
  
  public void setPushConnectionEnabled(boolean paramBoolean) {}
  
  void setTimeDifference(int paramInt)
  {
    if (Math.abs(paramInt - this.timeDifference) > 25) {}
    for (;;)
    {
      this.timeDifference = paramInt;
      return;
    }
  }
  
  public void setUserId(int paramInt) {}
  
  public void switchBackend()
  {
    if (this.isTestBackend == 0)
    {
      this.isTestBackend = 1;
      this.datacenters.clear();
      this.ports.clear();
      this.addressesIpv4.clear();
      this.addressesIpv4Download.clear();
      fillDatacenters();
      if (UserConfig.switchBackEnd.booleanValue()) {
        break label152;
      }
    }
    label152:
    for (boolean bool = true;; bool = false)
    {
      UserConfig.switchBackEnd = Boolean.valueOf(bool);
      BuildVars.DEBUG_LOG = UserConfig.switchBackEnd.booleanValue();
      UserConfig.saveConfig(true);
      Toast.makeText(ApplicationLoader.applicationContext, getHostName() + " : " + UserConfig.switchBackEnd, 0).show();
      FileLog.e("TSMS", "switchBackend to: " + getHostName());
      return;
      this.isTestBackend = 0;
      break;
    }
  }
  
  public void tcpConnectionClosed()
  {
    if (isNetworkOnline()) {
      onConnectionStateChanged(3);
    }
    for (;;)
    {
      if (BuildVars.DEBUG_VERSION) {}
      try
      {
        NetworkInfo[] arrayOfNetworkInfo = ((ConnectivityManager)ApplicationLoader.applicationContext.getSystemService("connectivity")).getAllNetworkInfo();
        int i = 0;
        for (;;)
        {
          if ((i >= 2) || (i >= arrayOfNetworkInfo.length))
          {
            if (arrayOfNetworkInfo.length == 0) {
              FileLog.e("tmessages", "no network available");
            }
            return;
            onConnectionStateChanged(1);
            break;
          }
          NetworkInfo localNetworkInfo = arrayOfNetworkInfo[i];
          FileLog.e("tmessages", "Network: " + localNetworkInfo.getTypeName() + " status: " + localNetworkInfo.getState() + " info: " + localNetworkInfo.getExtraInfo() + " object: " + localNetworkInfo.getDetailedState() + " other: " + localNetworkInfo);
          i += 1;
        }
        return;
      }
      catch (Exception localException)
      {
        FileLog.e("tmessages", "NETWORK STATE GET ERROR", localException);
      }
    }
  }
  
  public void updateDcSettings(int paramInt)
  {
    if (this.updatingDcSettings) {
      return;
    }
    this.updatingDcStartTime = ((int)(System.currentTimeMillis() / 1000L));
    this.updatingDcSettings = true;
    TLRPC.TL_help_getConfig localTL_help_getConfig = new TLRPC.TL_help_getConfig();
    getInstance().sendRequest(localTL_help_getConfig, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (!ConnectionsManager.this.updatingDcSettings) {
          return;
        }
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_config)paramAnonymousTLObject;
          int j = paramAnonymousTLObject.expires - ConnectionsManager.this.getCurrentTime();
          int i = j;
          if (j <= 0) {
            i = 120;
          }
          ConnectionsManager.access$1102(ConnectionsManager.this, (int)(System.currentTimeMillis() / 1000L) - 3600 + i);
          paramAnonymousTL_error = new ArrayList();
          Object localObject = new HashMap();
          HashMap localHashMap1 = new HashMap();
          HashMap localHashMap2 = new HashMap();
          i = 0;
          if (i < paramAnonymousTLObject.dc_options.size())
          {
            TLRPC.TL_dcOption localTL_dcOption1 = (TLRPC.TL_dcOption)paramAnonymousTLObject.dc_options.get(i);
            if ((TLRPC.TL_dcOption)((HashMap)localObject).get(Integer.valueOf(localTL_dcOption1.id)) == null)
            {
              TLRPC.TL_dcOption localTL_dcOption2 = new TLRPC.TL_dcOption();
              localTL_dcOption2.id = localTL_dcOption1.id;
              paramAnonymousTL_error.add(localTL_dcOption2);
              ((HashMap)localObject).put(Integer.valueOf(localTL_dcOption2.id), localTL_dcOption2);
            }
            if ((localTL_dcOption1.flags & 0x2) != 0) {
              localHashMap2.put(localTL_dcOption1.ip_address, Integer.valueOf(localTL_dcOption1.port));
            }
            for (;;)
            {
              ConnectionsManager.this.addAddressAndPort(localTL_dcOption1.ip_address, localTL_dcOption1.port, localTL_dcOption1.flags);
              i += 1;
              break;
              localHashMap1.put(localTL_dcOption1.ip_address, Integer.valueOf(localTL_dcOption1.port));
            }
          }
          if (!paramAnonymousTL_error.isEmpty())
          {
            i = 0;
            while (i < paramAnonymousTL_error.size())
            {
              localObject = (TLRPC.TL_dcOption)paramAnonymousTL_error.get(i);
              if (ConnectionsManager.this.datacenterWithId(((TLRPC.TL_dcOption)localObject).id) == null) {
                ConnectionsManager.this.datacenters.put(Integer.valueOf(((TLRPC.TL_dcOption)localObject).id), localObject);
              }
              i += 1;
            }
            ConnectionsManager.this.replaceAddressesAndPorts(ConnectionsManager.this.addressesIpv4, localHashMap1, 0);
            ConnectionsManager.this.replaceAddressesAndPorts(ConnectionsManager.this.addressesIpv4Download, localHashMap2, 2);
            ConnectionsManager.this.saveSession();
            ConnectionsManager.this.processRequestQueue(7, 0);
          }
          MessagesController.getInstance().updateConfig(paramAnonymousTLObject);
        }
        ConnectionsManager.access$1002(ConnectionsManager.this, false);
      }
    }, 25, 17);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/tgnet/ConnectionsManager.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */