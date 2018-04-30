package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.LongSparseArray;
import android.util.SparseArray;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PhotoSize;

public class DownloadController
  implements NotificationCenter.NotificationCenterDelegate
{
  public static final int AUTODOWNLOAD_MASK_AUDIO = 2;
  public static final int AUTODOWNLOAD_MASK_DOCUMENT = 8;
  public static final int AUTODOWNLOAD_MASK_GIF = 32;
  public static final int AUTODOWNLOAD_MASK_MUSIC = 16;
  public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
  public static final int AUTODOWNLOAD_MASK_VIDEO = 4;
  public static final int AUTODOWNLOAD_MASK_VIDEOMESSAGE = 64;
  private static volatile DownloadController[] Instance = new DownloadController[3];
  private HashMap<String, FileDownloadProgressListener> addLaterArray = new HashMap();
  private ArrayList<DownloadObject> audioDownloadQueue = new ArrayList();
  private int currentAccount;
  private ArrayList<FileDownloadProgressListener> deleteLaterArray = new ArrayList();
  private ArrayList<DownloadObject> documentDownloadQueue = new ArrayList();
  private HashMap<String, DownloadObject> downloadQueueKeys = new HashMap();
  private ArrayList<DownloadObject> gifDownloadQueue = new ArrayList();
  public boolean globalAutodownloadEnabled;
  private int lastCheckMask = 0;
  private int lastTag = 0;
  private boolean listenerInProgress = false;
  private HashMap<String, ArrayList<MessageObject>> loadingFileMessagesObservers = new HashMap();
  private HashMap<String, ArrayList<WeakReference<FileDownloadProgressListener>>> loadingFileObservers = new HashMap();
  public int[] mobileDataDownloadMask = new int[4];
  public int[] mobileMaxFileSize = new int[7];
  private ArrayList<DownloadObject> musicDownloadQueue = new ArrayList();
  private SparseArray<String> observersByTag = new SparseArray();
  private ArrayList<DownloadObject> photoDownloadQueue = new ArrayList();
  public int[] roamingDownloadMask = new int[4];
  public int[] roamingMaxFileSize = new int[7];
  private LongSparseArray<Long> typingTimes = new LongSparseArray();
  private ArrayList<DownloadObject> videoDownloadQueue = new ArrayList();
  private ArrayList<DownloadObject> videoMessageDownloadQueue = new ArrayList();
  public int[] wifiDownloadMask = new int[4];
  public int[] wifiMaxFileSize = new int[7];
  
  public DownloadController(int paramInt)
  {
    this.currentAccount = paramInt;
    Object localObject2 = MessagesController.getMainSettings(this.currentAccount);
    paramInt = 0;
    if (paramInt < 4)
    {
      Object localObject3 = new StringBuilder().append("mobileDataDownloadMask");
      if (paramInt == 0)
      {
        localObject1 = "";
        label260:
        localObject1 = localObject1;
        if ((paramInt != 0) && (!((SharedPreferences)localObject2).contains((String)localObject1))) {
          break label429;
        }
        this.mobileDataDownloadMask[paramInt] = ((SharedPreferences)localObject2).getInt((String)localObject1, 115);
        localObject3 = this.wifiDownloadMask;
        StringBuilder localStringBuilder = new StringBuilder().append("wifiDownloadMask");
        if (paramInt != 0) {
          break label413;
        }
        localObject1 = "";
        label328:
        localObject3[paramInt] = ((SharedPreferences)localObject2).getInt(localObject1, 115);
        localObject3 = this.roamingDownloadMask;
        localStringBuilder = new StringBuilder().append("roamingDownloadMask");
        if (paramInt != 0) {
          break label421;
        }
        localObject1 = "";
        label377:
        localObject3[paramInt] = ((SharedPreferences)localObject2).getInt(localObject1, 0);
      }
      for (;;)
      {
        paramInt += 1;
        break;
        localObject1 = Integer.valueOf(paramInt);
        break label260;
        label413:
        localObject1 = Integer.valueOf(paramInt);
        break label328;
        label421:
        localObject1 = Integer.valueOf(paramInt);
        break label377;
        label429:
        this.mobileDataDownloadMask[paramInt] = this.mobileDataDownloadMask[0];
        this.wifiDownloadMask[paramInt] = this.wifiDownloadMask[0];
        this.roamingDownloadMask[paramInt] = this.roamingDownloadMask[0];
      }
    }
    int i = 0;
    if (i < 7)
    {
      if (i == 1) {
        paramInt = 2097152;
      }
      for (;;)
      {
        this.mobileMaxFileSize[i] = ((SharedPreferences)localObject2).getInt("mobileMaxDownloadSize" + i, paramInt);
        this.wifiMaxFileSize[i] = ((SharedPreferences)localObject2).getInt("wifiMaxDownloadSize" + i, paramInt);
        this.roamingMaxFileSize[i] = ((SharedPreferences)localObject2).getInt("roamingMaxDownloadSize" + i, paramInt);
        i += 1;
        break;
        if (i == 6) {
          paramInt = 5242880;
        } else {
          paramInt = 10485760;
        }
      }
    }
    this.globalAutodownloadEnabled = ((SharedPreferences)localObject2).getBoolean("globalAutodownloadEnabled", true);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.FileUploadProgressChanged);
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.httpFileDidLoaded);
        NotificationCenter.getInstance(DownloadController.this.currentAccount).addObserver(DownloadController.this, NotificationCenter.httpFileDidFailedLoad);
      }
    });
    Object localObject1 = new BroadcastReceiver()
    {
      public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
      {
        DownloadController.this.checkAutodownloadSettings();
      }
    };
    localObject2 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    ApplicationLoader.applicationContext.registerReceiver((BroadcastReceiver)localObject1, (IntentFilter)localObject2);
    if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
      checkAutodownloadSettings();
    }
  }
  
  private void checkDownloadFinished(String paramString, int paramInt)
  {
    DownloadObject localDownloadObject = (DownloadObject)this.downloadQueueKeys.get(paramString);
    if (localDownloadObject != null)
    {
      this.downloadQueueKeys.remove(paramString);
      if ((paramInt == 0) || (paramInt == 2)) {
        MessagesStorage.getInstance(this.currentAccount).removeFromDownloadQueue(localDownloadObject.id, localDownloadObject.type, false);
      }
      if (localDownloadObject.type != 1) {
        break label86;
      }
      this.photoDownloadQueue.remove(localDownloadObject);
      if (this.photoDownloadQueue.isEmpty()) {
        newDownloadObjectsAvailable(1);
      }
    }
    label86:
    do
    {
      do
      {
        do
        {
          do
          {
            do
            {
              do
              {
                do
                {
                  return;
                  if (localDownloadObject.type != 2) {
                    break;
                  }
                  this.audioDownloadQueue.remove(localDownloadObject);
                } while (!this.audioDownloadQueue.isEmpty());
                newDownloadObjectsAvailable(2);
                return;
                if (localDownloadObject.type != 64) {
                  break;
                }
                this.videoMessageDownloadQueue.remove(localDownloadObject);
              } while (!this.videoMessageDownloadQueue.isEmpty());
              newDownloadObjectsAvailable(64);
              return;
              if (localDownloadObject.type != 4) {
                break;
              }
              this.videoDownloadQueue.remove(localDownloadObject);
            } while (!this.videoDownloadQueue.isEmpty());
            newDownloadObjectsAvailable(4);
            return;
            if (localDownloadObject.type != 8) {
              break;
            }
            this.documentDownloadQueue.remove(localDownloadObject);
          } while (!this.documentDownloadQueue.isEmpty());
          newDownloadObjectsAvailable(8);
          return;
          if (localDownloadObject.type != 16) {
            break;
          }
          this.musicDownloadQueue.remove(localDownloadObject);
        } while (!this.musicDownloadQueue.isEmpty());
        newDownloadObjectsAvailable(16);
        return;
      } while (localDownloadObject.type != 32);
      this.gifDownloadQueue.remove(localDownloadObject);
    } while (!this.gifDownloadQueue.isEmpty());
    newDownloadObjectsAvailable(32);
  }
  
  public static DownloadController getInstance(int paramInt)
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
        localObject1 = new DownloadController(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (DownloadController)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (DownloadController)localObject1;
  }
  
  public static int maskToIndex(int paramInt)
  {
    if (paramInt == 1) {}
    do
    {
      return 0;
      if (paramInt == 2) {
        return 1;
      }
      if (paramInt == 4) {
        return 2;
      }
      if (paramInt == 8) {
        return 3;
      }
      if (paramInt == 16) {
        return 4;
      }
      if (paramInt == 32) {
        return 5;
      }
    } while (paramInt != 64);
    return 6;
  }
  
  private void processLaterArrays()
  {
    Iterator localIterator = this.addLaterArray.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      addLoadingFileObserver((String)localEntry.getKey(), (FileDownloadProgressListener)localEntry.getValue());
    }
    this.addLaterArray.clear();
    localIterator = this.deleteLaterArray.iterator();
    while (localIterator.hasNext()) {
      removeLoadingFileObserver((FileDownloadProgressListener)localIterator.next());
    }
    this.deleteLaterArray.clear();
  }
  
  public void addLoadingFileObserver(String paramString, FileDownloadProgressListener paramFileDownloadProgressListener)
  {
    addLoadingFileObserver(paramString, null, paramFileDownloadProgressListener);
  }
  
  public void addLoadingFileObserver(String paramString, MessageObject paramMessageObject, FileDownloadProgressListener paramFileDownloadProgressListener)
  {
    if (this.listenerInProgress)
    {
      this.addLaterArray.put(paramString, paramFileDownloadProgressListener);
      return;
    }
    removeLoadingFileObserver(paramFileDownloadProgressListener);
    ArrayList localArrayList2 = (ArrayList)this.loadingFileObservers.get(paramString);
    ArrayList localArrayList1 = localArrayList2;
    if (localArrayList2 == null)
    {
      localArrayList1 = new ArrayList();
      this.loadingFileObservers.put(paramString, localArrayList1);
    }
    localArrayList1.add(new WeakReference(paramFileDownloadProgressListener));
    if (paramMessageObject != null)
    {
      localArrayList2 = (ArrayList)this.loadingFileMessagesObservers.get(paramString);
      localArrayList1 = localArrayList2;
      if (localArrayList2 == null)
      {
        localArrayList1 = new ArrayList();
        this.loadingFileMessagesObservers.put(paramString, localArrayList1);
      }
      localArrayList1.add(paramMessageObject);
    }
    this.observersByTag.put(paramFileDownloadProgressListener.getObserverTag(), paramString);
  }
  
  public boolean canDownloadMedia(MessageObject paramMessageObject)
  {
    return canDownloadMedia(paramMessageObject.messageOwner);
  }
  
  public boolean canDownloadMedia(TLRPC.Message paramMessage)
  {
    if (!this.globalAutodownloadEnabled) {}
    label63:
    label178:
    label183:
    label213:
    label218:
    label264:
    for (;;)
    {
      return false;
      int i;
      TLRPC.Peer localPeer;
      int j;
      int k;
      if (MessageObject.isPhoto(paramMessage))
      {
        i = 1;
        localPeer = paramMessage.to_id;
        if (localPeer == null) {
          break label213;
        }
        if (localPeer.user_id == 0) {
          break label183;
        }
        if (!ContactsController.getInstance(this.currentAccount).contactsDict.containsKey(Integer.valueOf(localPeer.user_id))) {
          break label178;
        }
        j = 0;
        if (!ConnectionsManager.isConnectedToWiFi()) {
          break label218;
        }
        j = this.wifiDownloadMask[j];
        k = this.wifiMaxFileSize[maskToIndex(i)];
      }
      for (;;)
      {
        if (((i != 1) && (MessageObject.getMessageSize(paramMessage) > k)) || ((j & i) == 0)) {
          break label264;
        }
        return true;
        if (MessageObject.isVoiceMessage(paramMessage))
        {
          i = 2;
          break;
        }
        if (MessageObject.isRoundVideoMessage(paramMessage))
        {
          i = 64;
          break;
        }
        if (MessageObject.isVideoMessage(paramMessage))
        {
          i = 4;
          break;
        }
        if (MessageObject.isMusicMessage(paramMessage))
        {
          i = 16;
          break;
        }
        if (MessageObject.isGifMessage(paramMessage))
        {
          i = 32;
          break;
        }
        i = 8;
        break;
        j = 1;
        break label63;
        if (localPeer.chat_id != 0)
        {
          j = 2;
          break label63;
        }
        if (MessageObject.isMegagroup(paramMessage))
        {
          j = 2;
          break label63;
        }
        j = 3;
        break label63;
        j = 1;
        break label63;
        if (ConnectionsManager.isRoaming())
        {
          j = this.roamingDownloadMask[j];
          k = this.roamingMaxFileSize[maskToIndex(i)];
        }
        else
        {
          j = this.mobileDataDownloadMask[j];
          k = this.mobileMaxFileSize[maskToIndex(i)];
        }
      }
    }
  }
  
  public void checkAutodownloadSettings()
  {
    int j = getCurrentDownloadMask();
    if (j == this.lastCheckMask) {}
    label61:
    label84:
    label107:
    label128:
    label151:
    int i;
    label254:
    label313:
    label372:
    label431:
    label490:
    label549:
    label608:
    do
    {
      return;
      this.lastCheckMask = j;
      if ((j & 0x1) != 0)
      {
        if (this.photoDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(1);
        }
        if ((j & 0x2) == 0) {
          break label254;
        }
        if (this.audioDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(2);
        }
        if ((j & 0x40) == 0) {
          break label313;
        }
        if (this.videoMessageDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(64);
        }
        if ((j & 0x8) == 0) {
          break label372;
        }
        if (this.documentDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(8);
        }
        if ((j & 0x4) == 0) {
          break label431;
        }
        if (this.videoDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(4);
        }
        if ((j & 0x10) == 0) {
          break label490;
        }
        if (this.musicDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(16);
        }
        if ((j & 0x20) == 0) {
          break label549;
        }
        if (this.gifDownloadQueue.isEmpty()) {
          newDownloadObjectsAvailable(32);
        }
      }
      for (;;)
      {
        i = getAutodownloadMaskAll();
        if (i != 0) {
          break label608;
        }
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(0);
        return;
        i = 0;
        Object localObject;
        while (i < this.photoDownloadQueue.size())
        {
          localObject = (DownloadObject)this.photoDownloadQueue.get(i);
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.PhotoSize)((DownloadObject)localObject).object);
          i += 1;
        }
        this.photoDownloadQueue.clear();
        break;
        i = 0;
        while (i < this.audioDownloadQueue.size())
        {
          localObject = (DownloadObject)this.audioDownloadQueue.get(i);
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)((DownloadObject)localObject).object);
          i += 1;
        }
        this.audioDownloadQueue.clear();
        break label61;
        i = 0;
        while (i < this.videoMessageDownloadQueue.size())
        {
          localObject = (DownloadObject)this.videoMessageDownloadQueue.get(i);
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)((DownloadObject)localObject).object);
          i += 1;
        }
        this.videoMessageDownloadQueue.clear();
        break label84;
        i = 0;
        while (i < this.documentDownloadQueue.size())
        {
          localObject = (TLRPC.Document)((DownloadObject)this.documentDownloadQueue.get(i)).object;
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)localObject);
          i += 1;
        }
        this.documentDownloadQueue.clear();
        break label107;
        i = 0;
        while (i < this.videoDownloadQueue.size())
        {
          localObject = (DownloadObject)this.videoDownloadQueue.get(i);
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)((DownloadObject)localObject).object);
          i += 1;
        }
        this.videoDownloadQueue.clear();
        break label128;
        i = 0;
        while (i < this.musicDownloadQueue.size())
        {
          localObject = (TLRPC.Document)((DownloadObject)this.musicDownloadQueue.get(i)).object;
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)localObject);
          i += 1;
        }
        this.musicDownloadQueue.clear();
        break label151;
        i = 0;
        while (i < this.gifDownloadQueue.size())
        {
          localObject = (TLRPC.Document)((DownloadObject)this.gifDownloadQueue.get(i)).object;
          FileLoader.getInstance(this.currentAccount).cancelLoadFile((TLRPC.Document)localObject);
          i += 1;
        }
        this.gifDownloadQueue.clear();
      }
      if ((i & 0x1) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(1);
      }
      if ((i & 0x2) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(2);
      }
      if ((i & 0x40) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(64);
      }
      if ((i & 0x4) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(4);
      }
      if ((i & 0x8) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(8);
      }
      if ((i & 0x10) == 0) {
        MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(16);
      }
    } while ((i & 0x20) != 0);
    MessagesStorage.getInstance(this.currentAccount).clearDownloadQueue(32);
  }
  
  public void cleanup()
  {
    this.photoDownloadQueue.clear();
    this.audioDownloadQueue.clear();
    this.videoMessageDownloadQueue.clear();
    this.documentDownloadQueue.clear();
    this.videoDownloadQueue.clear();
    this.musicDownloadQueue.clear();
    this.gifDownloadQueue.clear();
    this.downloadQueueKeys.clear();
    this.typingTimes.clear();
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    Object localObject3;
    if ((paramInt1 == NotificationCenter.FileDidFailedLoad) || (paramInt1 == NotificationCenter.httpFileDidFailedLoad))
    {
      this.listenerInProgress = true;
      localObject1 = (String)paramVarArgs[0];
      localObject2 = (ArrayList)this.loadingFileObservers.get(localObject1);
      if (localObject2 != null)
      {
        paramInt1 = 0;
        paramInt2 = ((ArrayList)localObject2).size();
        while (paramInt1 < paramInt2)
        {
          localObject3 = (WeakReference)((ArrayList)localObject2).get(paramInt1);
          if (((WeakReference)localObject3).get() != null)
          {
            ((FileDownloadProgressListener)((WeakReference)localObject3).get()).onFailedDownload((String)localObject1);
            this.observersByTag.remove(((FileDownloadProgressListener)((WeakReference)localObject3).get()).getObserverTag());
          }
          paramInt1 += 1;
        }
        this.loadingFileObservers.remove(localObject1);
      }
      this.listenerInProgress = false;
      processLaterArrays();
      checkDownloadFinished((String)localObject1, ((Integer)paramVarArgs[1]).intValue());
    }
    do
    {
      return;
      if ((paramInt1 == NotificationCenter.FileDidLoaded) || (paramInt1 == NotificationCenter.httpFileDidLoaded))
      {
        this.listenerInProgress = true;
        paramVarArgs = (String)paramVarArgs[0];
        localObject1 = (ArrayList)this.loadingFileMessagesObservers.get(paramVarArgs);
        if (localObject1 != null)
        {
          paramInt1 = 0;
          paramInt2 = ((ArrayList)localObject1).size();
          while (paramInt1 < paramInt2)
          {
            ((MessageObject)((ArrayList)localObject1).get(paramInt1)).mediaExists = true;
            paramInt1 += 1;
          }
          this.loadingFileMessagesObservers.remove(paramVarArgs);
        }
        localObject1 = (ArrayList)this.loadingFileObservers.get(paramVarArgs);
        if (localObject1 != null)
        {
          paramInt1 = 0;
          paramInt2 = ((ArrayList)localObject1).size();
          while (paramInt1 < paramInt2)
          {
            localObject2 = (WeakReference)((ArrayList)localObject1).get(paramInt1);
            if (((WeakReference)localObject2).get() != null)
            {
              ((FileDownloadProgressListener)((WeakReference)localObject2).get()).onSuccessDownload(paramVarArgs);
              this.observersByTag.remove(((FileDownloadProgressListener)((WeakReference)localObject2).get()).getObserverTag());
            }
            paramInt1 += 1;
          }
          this.loadingFileObservers.remove(paramVarArgs);
        }
        this.listenerInProgress = false;
        processLaterArrays();
        checkDownloadFinished(paramVarArgs, 0);
        return;
      }
      if (paramInt1 == NotificationCenter.FileLoadProgressChanged)
      {
        this.listenerInProgress = true;
        localObject1 = (String)paramVarArgs[0];
        localObject2 = (ArrayList)this.loadingFileObservers.get(localObject1);
        if (localObject2 != null)
        {
          paramVarArgs = (Float)paramVarArgs[1];
          paramInt1 = 0;
          paramInt2 = ((ArrayList)localObject2).size();
          while (paramInt1 < paramInt2)
          {
            localObject3 = (WeakReference)((ArrayList)localObject2).get(paramInt1);
            if (((WeakReference)localObject3).get() != null) {
              ((FileDownloadProgressListener)((WeakReference)localObject3).get()).onProgressDownload((String)localObject1, paramVarArgs.floatValue());
            }
            paramInt1 += 1;
          }
        }
        this.listenerInProgress = false;
        processLaterArrays();
        return;
      }
    } while (paramInt1 != NotificationCenter.FileUploadProgressChanged);
    this.listenerInProgress = true;
    Object localObject1 = (String)paramVarArgs[0];
    Object localObject2 = (ArrayList)this.loadingFileObservers.get(localObject1);
    if (localObject2 != null)
    {
      localObject3 = (Float)paramVarArgs[1];
      paramVarArgs = (Boolean)paramVarArgs[2];
      paramInt1 = 0;
      paramInt2 = ((ArrayList)localObject2).size();
      while (paramInt1 < paramInt2)
      {
        WeakReference localWeakReference = (WeakReference)((ArrayList)localObject2).get(paramInt1);
        if (localWeakReference.get() != null) {
          ((FileDownloadProgressListener)localWeakReference.get()).onProgressUpload((String)localObject1, ((Float)localObject3).floatValue(), paramVarArgs.booleanValue());
        }
        paramInt1 += 1;
      }
    }
    this.listenerInProgress = false;
    processLaterArrays();
    for (;;)
    {
      long l;
      try
      {
        paramVarArgs = SendMessagesHelper.getInstance(this.currentAccount).getDelayedMessages((String)localObject1);
        if (paramVarArgs == null) {
          break;
        }
        paramInt1 = 0;
        if (paramInt1 >= paramVarArgs.size()) {
          break;
        }
        localObject2 = (SendMessagesHelper.DelayedMessage)paramVarArgs.get(paramInt1);
        if (((SendMessagesHelper.DelayedMessage)localObject2).encryptedChat != null) {
          break label993;
        }
        l = ((SendMessagesHelper.DelayedMessage)localObject2).peer;
        if (((SendMessagesHelper.DelayedMessage)localObject2).type == 4)
        {
          localObject3 = (Long)this.typingTimes.get(l);
          if ((localObject3 != null) && (((Long)localObject3).longValue() + 4000L >= System.currentTimeMillis())) {
            break label993;
          }
          localObject2 = (MessageObject)((SendMessagesHelper.DelayedMessage)localObject2).extraHashMap.get((String)localObject1 + "_i");
          if ((localObject2 != null) && (((MessageObject)localObject2).isVideo()))
          {
            MessagesController.getInstance(this.currentAccount).sendTyping(l, 5, 0);
            this.typingTimes.put(l, Long.valueOf(System.currentTimeMillis()));
            break label993;
          }
          MessagesController.getInstance(this.currentAccount).sendTyping(l, 4, 0);
          continue;
        }
        localObject3 = (Long)this.typingTimes.get(l);
      }
      catch (Exception paramVarArgs)
      {
        FileLog.e(paramVarArgs);
        return;
      }
      ((SendMessagesHelper.DelayedMessage)localObject2).obj.getDocument();
      if ((localObject3 == null) || (((Long)localObject3).longValue() + 4000L < System.currentTimeMillis()))
      {
        if (((SendMessagesHelper.DelayedMessage)localObject2).obj.isRoundVideo()) {
          MessagesController.getInstance(this.currentAccount).sendTyping(l, 8, 0);
        }
        for (;;)
        {
          this.typingTimes.put(l, Long.valueOf(System.currentTimeMillis()));
          break;
          if (((SendMessagesHelper.DelayedMessage)localObject2).obj.isVideo()) {
            MessagesController.getInstance(this.currentAccount).sendTyping(l, 5, 0);
          } else if (((SendMessagesHelper.DelayedMessage)localObject2).obj.isVoice()) {
            MessagesController.getInstance(this.currentAccount).sendTyping(l, 9, 0);
          } else if (((SendMessagesHelper.DelayedMessage)localObject2).obj.getDocument() != null) {
            MessagesController.getInstance(this.currentAccount).sendTyping(l, 3, 0);
          } else if (((SendMessagesHelper.DelayedMessage)localObject2).location != null) {
            MessagesController.getInstance(this.currentAccount).sendTyping(l, 4, 0);
          }
        }
      }
      label993:
      paramInt1 += 1;
    }
  }
  
  public int generateObserverTag()
  {
    int i = this.lastTag;
    this.lastTag = (i + 1);
    return i;
  }
  
  protected int getAutodownloadMask()
  {
    int j;
    if (!this.globalAutodownloadEnabled)
    {
      j = 0;
      return j;
    }
    int i = 0;
    int[] arrayOfInt;
    if (ConnectionsManager.isConnectedToWiFi()) {
      arrayOfInt = this.wifiDownloadMask;
    }
    for (;;)
    {
      int m = 0;
      for (;;)
      {
        j = i;
        if (m >= 4) {
          break;
        }
        int k = 0;
        if ((arrayOfInt[m] & 0x1) != 0) {
          k = 0x0 | 0x1;
        }
        j = k;
        if ((arrayOfInt[m] & 0x2) != 0) {
          j = k | 0x2;
        }
        k = j;
        if ((arrayOfInt[m] & 0x40) != 0) {
          k = j | 0x40;
        }
        j = k;
        if ((arrayOfInt[m] & 0x4) != 0) {
          j = k | 0x4;
        }
        k = j;
        if ((arrayOfInt[m] & 0x8) != 0) {
          k = j | 0x8;
        }
        j = k;
        if ((arrayOfInt[m] & 0x10) != 0) {
          j = k | 0x10;
        }
        k = j;
        if ((arrayOfInt[m] & 0x20) != 0) {
          k = j | 0x20;
        }
        i |= k << m * 8;
        m += 1;
      }
      if (ConnectionsManager.isRoaming()) {
        arrayOfInt = this.roamingDownloadMask;
      } else {
        arrayOfInt = this.mobileDataDownloadMask;
      }
    }
  }
  
  protected int getAutodownloadMaskAll()
  {
    int j;
    if (!this.globalAutodownloadEnabled)
    {
      j = 0;
      return j;
    }
    int i = 0;
    int k = 0;
    for (;;)
    {
      j = i;
      if (k >= 4) {
        break;
      }
      if (((this.mobileDataDownloadMask[k] & 0x1) == 0) && ((this.wifiDownloadMask[k] & 0x1) == 0))
      {
        j = i;
        if ((this.roamingDownloadMask[k] & 0x1) == 0) {}
      }
      else
      {
        j = i | 0x1;
      }
      if (((this.mobileDataDownloadMask[k] & 0x2) == 0) && ((this.wifiDownloadMask[k] & 0x2) == 0))
      {
        i = j;
        if ((this.roamingDownloadMask[k] & 0x2) == 0) {}
      }
      else
      {
        i = j | 0x2;
      }
      if (((this.mobileDataDownloadMask[k] & 0x40) == 0) && ((this.wifiDownloadMask[k] & 0x40) == 0))
      {
        j = i;
        if ((this.roamingDownloadMask[k] & 0x40) == 0) {}
      }
      else
      {
        j = i | 0x40;
      }
      if (((this.mobileDataDownloadMask[k] & 0x4) == 0) && ((this.wifiDownloadMask[k] & 0x4) == 0))
      {
        i = j;
        if ((this.roamingDownloadMask[k] & 0x4) == 0) {}
      }
      else
      {
        i = j | 0x4;
      }
      if (((this.mobileDataDownloadMask[k] & 0x8) == 0) && ((this.wifiDownloadMask[k] & 0x8) == 0))
      {
        j = i;
        if ((this.roamingDownloadMask[k] & 0x8) == 0) {}
      }
      else
      {
        j = i | 0x8;
      }
      if (((this.mobileDataDownloadMask[k] & 0x10) == 0) && ((this.wifiDownloadMask[k] & 0x10) == 0))
      {
        i = j;
        if ((this.roamingDownloadMask[k] & 0x10) == 0) {}
      }
      else
      {
        i = j | 0x10;
      }
      if (((this.mobileDataDownloadMask[k] & 0x20) == 0) && ((this.wifiDownloadMask[k] & 0x20) == 0))
      {
        j = i;
        if ((this.roamingDownloadMask[k] & 0x20) == 0) {}
      }
      else
      {
        j = i | 0x20;
      }
      k += 1;
      i = j;
    }
  }
  
  protected int getCurrentDownloadMask()
  {
    int i;
    if (!this.globalAutodownloadEnabled)
    {
      i = 0;
      return i;
    }
    if (ConnectionsManager.isConnectedToWiFi())
    {
      j = 0;
      k = 0;
      for (;;)
      {
        i = j;
        if (k >= 4) {
          break;
        }
        j |= this.wifiDownloadMask[k];
        k += 1;
      }
    }
    if (ConnectionsManager.isRoaming())
    {
      j = 0;
      k = 0;
      for (;;)
      {
        i = j;
        if (k >= 4) {
          break;
        }
        j |= this.roamingDownloadMask[k];
        k += 1;
      }
    }
    int j = 0;
    int k = 0;
    for (;;)
    {
      i = j;
      if (k >= 4) {
        break;
      }
      j |= this.mobileDataDownloadMask[k];
      k += 1;
    }
  }
  
  protected void newDownloadObjectsAvailable(int paramInt)
  {
    int i = getCurrentDownloadMask();
    if (((i & 0x1) != 0) && ((paramInt & 0x1) != 0) && (this.photoDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(1);
    }
    if (((i & 0x2) != 0) && ((paramInt & 0x2) != 0) && (this.audioDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(2);
    }
    if (((i & 0x40) != 0) && ((paramInt & 0x40) != 0) && (this.videoMessageDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(64);
    }
    if (((i & 0x4) != 0) && ((paramInt & 0x4) != 0) && (this.videoDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(4);
    }
    if (((i & 0x8) != 0) && ((paramInt & 0x8) != 0) && (this.documentDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(8);
    }
    if (((i & 0x10) != 0) && ((paramInt & 0x10) != 0) && (this.musicDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(16);
    }
    if (((i & 0x20) != 0) && ((paramInt & 0x20) != 0) && (this.gifDownloadQueue.isEmpty())) {
      MessagesStorage.getInstance(this.currentAccount).getDownloadQueue(32);
    }
  }
  
  protected void processDownloadObjects(int paramInt, ArrayList<DownloadObject> paramArrayList)
  {
    if (paramArrayList.isEmpty()) {
      return;
    }
    ArrayList localArrayList = null;
    label22:
    label24:
    DownloadObject localDownloadObject;
    String str;
    if (paramInt == 1)
    {
      localArrayList = this.photoDownloadQueue;
      paramInt = 0;
      if (paramInt < paramArrayList.size())
      {
        localDownloadObject = (DownloadObject)paramArrayList.get(paramInt);
        if (!(localDownloadObject.object instanceof TLRPC.Document)) {
          break label173;
        }
        str = FileLoader.getAttachFileName((TLRPC.Document)localDownloadObject.object);
        label66:
        if (!this.downloadQueueKeys.containsKey(str)) {
          break label186;
        }
      }
    }
    label173:
    label186:
    label229:
    label336:
    for (;;)
    {
      paramInt += 1;
      break label24;
      break;
      if (paramInt == 2)
      {
        localArrayList = this.audioDownloadQueue;
        break label22;
      }
      if (paramInt == 64)
      {
        localArrayList = this.videoMessageDownloadQueue;
        break label22;
      }
      if (paramInt == 4)
      {
        localArrayList = this.videoDownloadQueue;
        break label22;
      }
      if (paramInt == 8)
      {
        localArrayList = this.documentDownloadQueue;
        break label22;
      }
      if (paramInt == 16)
      {
        localArrayList = this.musicDownloadQueue;
        break label22;
      }
      if (paramInt != 32) {
        break label22;
      }
      localArrayList = this.gifDownloadQueue;
      break label22;
      str = FileLoader.getAttachFileName(localDownloadObject.object);
      break label66;
      int j = 1;
      Object localObject1;
      Object localObject2;
      int i;
      if ((localDownloadObject.object instanceof TLRPC.PhotoSize))
      {
        localObject1 = FileLoader.getInstance(this.currentAccount);
        localObject2 = (TLRPC.PhotoSize)localDownloadObject.object;
        if (localDownloadObject.secret)
        {
          i = 2;
          ((FileLoader)localObject1).loadFile((TLRPC.PhotoSize)localObject2, null, i);
          i = j;
        }
      }
      for (;;)
      {
        if (i == 0) {
          break label336;
        }
        localArrayList.add(localDownloadObject);
        this.downloadQueueKeys.put(str, localDownloadObject);
        break;
        i = 0;
        break label229;
        if ((localDownloadObject.object instanceof TLRPC.Document))
        {
          localObject1 = (TLRPC.Document)localDownloadObject.object;
          localObject2 = FileLoader.getInstance(this.currentAccount);
          if (localDownloadObject.secret) {}
          for (i = 2;; i = 0)
          {
            ((FileLoader)localObject2).loadFile((TLRPC.Document)localObject1, false, i);
            i = j;
            break;
          }
        }
        i = 0;
      }
    }
  }
  
  public void removeLoadingFileObserver(FileDownloadProgressListener paramFileDownloadProgressListener)
  {
    if (this.listenerInProgress) {
      this.deleteLaterArray.add(paramFileDownloadProgressListener);
    }
    String str;
    do
    {
      return;
      str = (String)this.observersByTag.get(paramFileDownloadProgressListener.getObserverTag());
    } while (str == null);
    ArrayList localArrayList = (ArrayList)this.loadingFileObservers.get(str);
    if (localArrayList != null)
    {
      int j;
      for (int i = 0; i < localArrayList.size(); i = j + 1)
      {
        WeakReference localWeakReference = (WeakReference)localArrayList.get(i);
        if (localWeakReference.get() != null)
        {
          j = i;
          if (localWeakReference.get() != paramFileDownloadProgressListener) {}
        }
        else
        {
          localArrayList.remove(i);
          j = i - 1;
        }
      }
      if (localArrayList.isEmpty()) {
        this.loadingFileObservers.remove(str);
      }
    }
    this.observersByTag.remove(paramFileDownloadProgressListener.getObserverTag());
  }
  
  public static abstract interface FileDownloadProgressListener
  {
    public abstract int getObserverTag();
    
    public abstract void onFailedDownload(String paramString);
    
    public abstract void onProgressDownload(String paramString, float paramFloat);
    
    public abstract void onProgressUpload(String paramString, float paramFloat, boolean paramBoolean);
    
    public abstract void onSuccessDownload(String paramString);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/DownloadController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */