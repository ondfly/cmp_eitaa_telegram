package org.telegram.messenger;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.telegram.messenger.exoplayer2.upstream.DataSource;
import org.telegram.messenger.exoplayer2.upstream.TransferListener;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_fileLocationUnavailable;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.tgnet.TLRPC.WebPage;

public class FileLoader
{
  private static volatile FileLoader[] Instance = new FileLoader[3];
  public static final int MEDIA_DIR_AUDIO = 1;
  public static final int MEDIA_DIR_CACHE = 4;
  public static final int MEDIA_DIR_DOCUMENT = 3;
  public static final int MEDIA_DIR_IMAGE = 0;
  public static final int MEDIA_DIR_VIDEO = 2;
  private static volatile DispatchQueue fileLoaderQueue = new DispatchQueue("fileUploadQueue");
  private static SparseArray<File> mediaDirs = null;
  private ArrayList<FileLoadOperation> activeFileLoadOperation = new ArrayList();
  private SparseArray<LinkedList<FileLoadOperation>> audioLoadOperationQueues = new SparseArray();
  private int currentAccount;
  private SparseIntArray currentAudioLoadOperationsCount = new SparseIntArray();
  private SparseIntArray currentLoadOperationsCount = new SparseIntArray();
  private SparseIntArray currentPhotoLoadOperationsCount = new SparseIntArray();
  private int currentUploadOperationsCount = 0;
  private int currentUploadSmallOperationsCount = 0;
  private FileLoaderDelegate delegate = null;
  private ConcurrentHashMap<String, FileLoadOperation> loadOperationPaths = new ConcurrentHashMap();
  private ConcurrentHashMap<String, Boolean> loadOperationPathsUI = new ConcurrentHashMap(10, 1.0F, 2);
  private SparseArray<LinkedList<FileLoadOperation>> loadOperationQueues = new SparseArray();
  private SparseArray<LinkedList<FileLoadOperation>> photoLoadOperationQueues = new SparseArray();
  private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPaths = new ConcurrentHashMap();
  private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPathsEnc = new ConcurrentHashMap();
  private LinkedList<FileUploadOperation> uploadOperationQueue = new LinkedList();
  private HashMap<String, Long> uploadSizes = new HashMap();
  private LinkedList<FileUploadOperation> uploadSmallOperationQueue = new LinkedList();
  
  public FileLoader(int paramInt)
  {
    this.currentAccount = paramInt;
  }
  
  private void cancelLoadFile(final TLRPC.Document paramDocument, final TLRPC.TL_webDocument paramTL_webDocument, final TLRPC.FileLocation paramFileLocation, final String paramString)
  {
    if ((paramFileLocation == null) && (paramDocument == null) && (paramTL_webDocument == null)) {}
    for (;;)
    {
      return;
      if (paramFileLocation != null) {
        paramString = getAttachFileName(paramFileLocation, paramString);
      }
      while (paramString != null)
      {
        this.loadOperationPathsUI.remove(paramString);
        fileLoaderQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            FileLoadOperation localFileLoadOperation = (FileLoadOperation)FileLoader.this.loadOperationPaths.remove(paramString);
            int i;
            if (localFileLoadOperation != null)
            {
              i = localFileLoadOperation.getDatacenterId();
              if ((!MessageObject.isVoiceDocument(paramDocument)) && (!MessageObject.isVoiceWebDocument(paramTL_webDocument))) {
                break label91;
              }
              if (!FileLoader.this.getAudioLoadOperationQueue(i).remove(localFileLoadOperation)) {
                FileLoader.this.currentAudioLoadOperationsCount.put(i, FileLoader.this.currentAudioLoadOperationsCount.get(i) - 1);
              }
            }
            for (;;)
            {
              localFileLoadOperation.cancel();
              return;
              label91:
              if ((paramFileLocation != null) || (MessageObject.isImageWebDocument(paramTL_webDocument)))
              {
                if (!FileLoader.this.getPhotoLoadOperationQueue(i).remove(localFileLoadOperation)) {
                  FileLoader.this.currentPhotoLoadOperationsCount.put(i, FileLoader.this.currentPhotoLoadOperationsCount.get(i) - 1);
                }
              }
              else
              {
                if (!FileLoader.this.getLoadOperationQueue(i).remove(localFileLoadOperation)) {
                  FileLoader.this.currentLoadOperationsCount.put(i, FileLoader.this.currentLoadOperationsCount.get(i) - 1);
                }
                FileLoader.this.activeFileLoadOperation.remove(localFileLoadOperation);
              }
            }
          }
        });
        return;
        if (paramDocument != null) {
          paramString = getAttachFileName(paramDocument);
        } else if (paramTL_webDocument != null) {
          paramString = getAttachFileName(paramTL_webDocument);
        } else {
          paramString = null;
        }
      }
    }
  }
  
  public static File checkDirectory(int paramInt)
  {
    return (File)mediaDirs.get(paramInt);
  }
  
  private void checkDownloadQueue(final int paramInt, final TLRPC.Document paramDocument, final TLRPC.TL_webDocument paramTL_webDocument, final TLRPC.FileLocation paramFileLocation, final String paramString)
  {
    fileLoaderQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        Object localObject1 = FileLoader.this.getAudioLoadOperationQueue(paramInt);
        Object localObject2 = FileLoader.this.getPhotoLoadOperationQueue(paramInt);
        LinkedList localLinkedList = FileLoader.this.getLoadOperationQueue(paramInt);
        FileLoadOperation localFileLoadOperation = (FileLoadOperation)FileLoader.this.loadOperationPaths.remove(paramString);
        int j;
        int i;
        if ((MessageObject.isVoiceDocument(paramDocument)) || (MessageObject.isVoiceWebDocument(paramTL_webDocument)))
        {
          j = FileLoader.this.currentAudioLoadOperationsCount.get(paramInt);
          i = j;
          if (localFileLoadOperation != null)
          {
            if (localFileLoadOperation.wasStarted())
            {
              i = j - 1;
              FileLoader.this.currentAudioLoadOperationsCount.put(paramInt, i);
            }
          }
          else
          {
            if (((LinkedList)localObject1).isEmpty()) {
              return;
            }
            if (!((FileLoadOperation)((LinkedList)localObject1).get(0)).isForceRequest()) {
              break label210;
            }
            j = 3;
          }
        }
        for (;;)
        {
          if (i < j)
          {
            localObject2 = (FileLoadOperation)((LinkedList)localObject1).poll();
            if ((localObject2 == null) || (!((FileLoadOperation)localObject2).start())) {
              break;
            }
            i += 1;
            FileLoader.this.currentAudioLoadOperationsCount.put(paramInt, i);
            break;
            ((LinkedList)localObject1).remove(localFileLoadOperation);
            i = j;
            break;
            label210:
            j = 1;
            continue;
            if ((paramFileLocation != null) || (MessageObject.isImageWebDocument(paramTL_webDocument)))
            {
              j = FileLoader.this.currentPhotoLoadOperationsCount.get(paramInt);
              i = j;
              if (localFileLoadOperation != null)
              {
                if (localFileLoadOperation.wasStarted())
                {
                  i = j - 1;
                  FileLoader.this.currentPhotoLoadOperationsCount.put(paramInt, i);
                }
              }
              else
              {
                if (((LinkedList)localObject2).isEmpty()) {
                  return;
                }
                if (!((FileLoadOperation)((LinkedList)localObject2).get(0)).isForceRequest()) {
                  break label366;
                }
                j = 3;
              }
            }
            for (;;)
            {
              if (i < j)
              {
                localObject1 = (FileLoadOperation)((LinkedList)localObject2).poll();
                if ((localObject1 == null) || (!((FileLoadOperation)localObject1).start())) {
                  break;
                }
                i += 1;
                FileLoader.this.currentPhotoLoadOperationsCount.put(paramInt, i);
                break;
                ((LinkedList)localObject2).remove(localFileLoadOperation);
                i = j;
                break;
                label366:
                j = 1;
                continue;
                j = FileLoader.this.currentLoadOperationsCount.get(paramInt);
                i = j;
                if (localFileLoadOperation != null)
                {
                  if (localFileLoadOperation.wasStarted())
                  {
                    i = j - 1;
                    FileLoader.this.currentLoadOperationsCount.put(paramInt, i);
                    FileLoader.this.activeFileLoadOperation.remove(localFileLoadOperation);
                  }
                }
                else
                {
                  label433:
                  if (localLinkedList.isEmpty()) {
                    return;
                  }
                  if (!((FileLoadOperation)localLinkedList.get(0)).isForceRequest()) {
                    break label548;
                  }
                }
                label548:
                for (j = 3;; j = 1)
                {
                  if (i >= j) {
                    return;
                  }
                  localObject1 = (FileLoadOperation)localLinkedList.poll();
                  if ((localObject1 == null) || (!((FileLoadOperation)localObject1).start())) {
                    break label433;
                  }
                  j = i + 1;
                  FileLoader.this.currentLoadOperationsCount.put(paramInt, j);
                  i = j;
                  if (FileLoader.this.activeFileLoadOperation.contains(localObject1)) {
                    break label433;
                  }
                  FileLoader.this.activeFileLoadOperation.add(localObject1);
                  i = j;
                  break label433;
                  localLinkedList.remove(localFileLoadOperation);
                  i = j;
                  break;
                }
              }
            }
          }
        }
      }
    });
  }
  
  public static String fixFileName(String paramString)
  {
    String str = paramString;
    if (paramString != null) {
      str = paramString.replaceAll("[\001-\037<>:\"/\\\\|?*]+", "").trim();
    }
    return str;
  }
  
  public static String getAttachFileName(TLObject paramTLObject)
  {
    return getAttachFileName(paramTLObject, null);
  }
  
  public static String getAttachFileName(TLObject paramTLObject, String paramString)
  {
    int i = -1;
    if ((paramTLObject instanceof TLRPC.Document))
    {
      TLRPC.Document localDocument = (TLRPC.Document)paramTLObject;
      paramString = null;
      int j;
      if (0 == 0)
      {
        paramTLObject = getDocumentFileName(localDocument);
        if (paramTLObject != null)
        {
          j = paramTLObject.lastIndexOf('.');
          if (j != -1) {}
        }
        else
        {
          paramString = "";
        }
      }
      else
      {
        paramTLObject = paramString;
        if (paramString.length() <= 1)
        {
          if (localDocument.mime_type == null) {
            break label233;
          }
          paramTLObject = localDocument.mime_type;
          switch (paramTLObject.hashCode())
          {
          default: 
            switch (i)
            {
            default: 
              label100:
              paramTLObject = "";
            }
            break;
          }
        }
      }
      for (;;)
      {
        if (localDocument.version == 0)
        {
          if (paramTLObject.length() > 1)
          {
            return localDocument.dc_id + "_" + localDocument.id + paramTLObject;
            paramString = paramTLObject.substring(j);
            break;
            if (!paramTLObject.equals("video/mp4")) {
              break label100;
            }
            i = 0;
            break label100;
            if (!paramTLObject.equals("audio/ogg")) {
              break label100;
            }
            i = 1;
            break label100;
            paramTLObject = ".mp4";
            continue;
            paramTLObject = ".ogg";
            continue;
            label233:
            paramTLObject = "";
            continue;
          }
          return localDocument.dc_id + "_" + localDocument.id;
        }
      }
      if (paramTLObject.length() > 1) {
        return localDocument.dc_id + "_" + localDocument.id + "_" + localDocument.version + paramTLObject;
      }
      return localDocument.dc_id + "_" + localDocument.id + "_" + localDocument.version;
    }
    if ((paramTLObject instanceof TLRPC.TL_webDocument))
    {
      paramTLObject = (TLRPC.TL_webDocument)paramTLObject;
      return Utilities.MD5(paramTLObject.url) + "." + ImageLoader.getHttpUrlExtension(paramTLObject.url, getExtensionByMime(paramTLObject.mime_type));
    }
    if ((paramTLObject instanceof TLRPC.PhotoSize))
    {
      paramTLObject = (TLRPC.PhotoSize)paramTLObject;
      if ((paramTLObject.location == null) || ((paramTLObject.location instanceof TLRPC.TL_fileLocationUnavailable))) {
        return "";
      }
      paramTLObject = new StringBuilder().append(paramTLObject.location.volume_id).append("_").append(paramTLObject.location.local_id).append(".");
      if (paramString != null) {}
      for (;;)
      {
        return paramString;
        paramString = "jpg";
      }
    }
    if ((paramTLObject instanceof TLRPC.FileLocation))
    {
      if ((paramTLObject instanceof TLRPC.TL_fileLocationUnavailable)) {
        return "";
      }
      paramTLObject = (TLRPC.FileLocation)paramTLObject;
      paramTLObject = new StringBuilder().append(paramTLObject.volume_id).append("_").append(paramTLObject.local_id).append(".");
      if (paramString != null) {}
      for (;;)
      {
        return paramString;
        paramString = "jpg";
      }
    }
    return "";
  }
  
  private LinkedList<FileLoadOperation> getAudioLoadOperationQueue(int paramInt)
  {
    LinkedList localLinkedList2 = (LinkedList)this.audioLoadOperationQueues.get(paramInt);
    LinkedList localLinkedList1 = localLinkedList2;
    if (localLinkedList2 == null)
    {
      localLinkedList1 = new LinkedList();
      this.audioLoadOperationQueues.put(paramInt, localLinkedList1);
    }
    return localLinkedList1;
  }
  
  public static TLRPC.PhotoSize getClosestPhotoSizeWithSize(ArrayList<TLRPC.PhotoSize> paramArrayList, int paramInt)
  {
    return getClosestPhotoSizeWithSize(paramArrayList, paramInt, false);
  }
  
  public static TLRPC.PhotoSize getClosestPhotoSizeWithSize(ArrayList<TLRPC.PhotoSize> paramArrayList, int paramInt, boolean paramBoolean)
  {
    Object localObject2;
    if ((paramArrayList == null) || (paramArrayList.isEmpty()))
    {
      localObject2 = null;
      return (TLRPC.PhotoSize)localObject2;
    }
    int m = 0;
    Object localObject1 = null;
    int k = 0;
    TLRPC.PhotoSize localPhotoSize;
    int i;
    for (;;)
    {
      localObject2 = localObject1;
      if (k >= paramArrayList.size()) {
        break;
      }
      localPhotoSize = (TLRPC.PhotoSize)paramArrayList.get(k);
      if (localPhotoSize != null) {
        break label78;
      }
      i = m;
      localObject2 = localObject1;
      k += 1;
      localObject1 = localObject2;
      m = i;
    }
    label78:
    if (paramBoolean)
    {
      if (localPhotoSize.h >= localPhotoSize.w) {}
      for (j = localPhotoSize.w;; j = localPhotoSize.h)
      {
        if ((localObject1 != null) && ((paramInt <= 100) || (((TLRPC.PhotoSize)localObject1).location == null) || (((TLRPC.PhotoSize)localObject1).location.dc_id != Integer.MIN_VALUE)) && (!(localPhotoSize instanceof TLRPC.TL_photoCachedSize)))
        {
          localObject2 = localObject1;
          i = m;
          if (paramInt <= m) {
            break;
          }
          localObject2 = localObject1;
          i = m;
          if (m >= j) {
            break;
          }
        }
        localObject2 = localPhotoSize;
        i = j;
        break;
      }
    }
    if (localPhotoSize.w >= localPhotoSize.h) {}
    for (int j = localPhotoSize.w;; j = localPhotoSize.h)
    {
      if ((localObject1 != null) && ((paramInt <= 100) || (((TLRPC.PhotoSize)localObject1).location == null) || (((TLRPC.PhotoSize)localObject1).location.dc_id != Integer.MIN_VALUE)) && (!(localPhotoSize instanceof TLRPC.TL_photoCachedSize)))
      {
        localObject2 = localObject1;
        i = m;
        if (j > paramInt) {
          break;
        }
        localObject2 = localObject1;
        i = m;
        if (m >= j) {
          break;
        }
      }
      localObject2 = localPhotoSize;
      i = j;
      break;
    }
  }
  
  public static File getDirectory(int paramInt)
  {
    File localFile2 = (File)mediaDirs.get(paramInt);
    File localFile1 = localFile2;
    if (localFile2 == null)
    {
      localFile1 = localFile2;
      if (paramInt != 4) {
        localFile1 = (File)mediaDirs.get(4);
      }
    }
    try
    {
      if (!localFile1.isDirectory()) {
        localFile1.mkdirs();
      }
      return localFile1;
    }
    catch (Exception localException) {}
    return localFile1;
  }
  
  public static String getDocumentExtension(TLRPC.Document paramDocument)
  {
    Object localObject = getDocumentFileName(paramDocument);
    int i = ((String)localObject).lastIndexOf('.');
    String str = null;
    if (i != -1) {
      str = ((String)localObject).substring(i + 1);
    }
    if (str != null)
    {
      localObject = str;
      if (str.length() != 0) {}
    }
    else
    {
      localObject = paramDocument.mime_type;
    }
    paramDocument = (TLRPC.Document)localObject;
    if (localObject == null) {
      paramDocument = "";
    }
    return paramDocument.toUpperCase();
  }
  
  public static String getDocumentFileName(TLRPC.Document paramDocument)
  {
    String str = null;
    Object localObject = null;
    if (paramDocument != null)
    {
      if (paramDocument.file_name != null) {
        localObject = paramDocument.file_name;
      }
    }
    else
    {
      paramDocument = fixFileName((String)localObject);
      if (paramDocument == null) {
        break label77;
      }
      return paramDocument;
    }
    int i = 0;
    for (;;)
    {
      localObject = str;
      if (i >= paramDocument.attributes.size()) {
        break;
      }
      localObject = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
      if ((localObject instanceof TLRPC.TL_documentAttributeFilename)) {
        str = ((TLRPC.DocumentAttribute)localObject).file_name;
      }
      i += 1;
    }
    label77:
    return "";
  }
  
  public static String getExtensionByMime(String paramString)
  {
    int i = paramString.lastIndexOf('/');
    if (i != -1) {
      return paramString.substring(i + 1);
    }
    return "";
  }
  
  public static String getFileExtension(File paramFile)
  {
    paramFile = paramFile.getName();
    try
    {
      paramFile = paramFile.substring(paramFile.lastIndexOf('.') + 1);
      return paramFile;
    }
    catch (Exception paramFile) {}
    return "";
  }
  
  public static FileLoader getInstance(int paramInt)
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
        localObject1 = new FileLoader(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (FileLoader)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (FileLoader)localObject1;
  }
  
  public static File getInternalCacheDir()
  {
    return ApplicationLoader.applicationContext.getCacheDir();
  }
  
  private LinkedList<FileLoadOperation> getLoadOperationQueue(int paramInt)
  {
    LinkedList localLinkedList2 = (LinkedList)this.loadOperationQueues.get(paramInt);
    LinkedList localLinkedList1 = localLinkedList2;
    if (localLinkedList2 == null)
    {
      localLinkedList1 = new LinkedList();
      this.loadOperationQueues.put(paramInt, localLinkedList1);
    }
    return localLinkedList1;
  }
  
  public static String getMessageFileName(TLRPC.Message paramMessage)
  {
    if (paramMessage == null) {
      return "";
    }
    if ((paramMessage instanceof TLRPC.TL_messageService))
    {
      if (paramMessage.action.photo != null)
      {
        paramMessage = paramMessage.action.photo.sizes;
        if (paramMessage.size() > 0)
        {
          paramMessage = getClosestPhotoSizeWithSize(paramMessage, AndroidUtilities.getPhotoSize());
          if (paramMessage != null) {
            return getAttachFileName(paramMessage);
          }
        }
      }
    }
    else
    {
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) {
        return getAttachFileName(paramMessage.media.document);
      }
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto))
      {
        paramMessage = paramMessage.media.photo.sizes;
        if (paramMessage.size() > 0)
        {
          paramMessage = getClosestPhotoSizeWithSize(paramMessage, AndroidUtilities.getPhotoSize());
          if (paramMessage != null) {
            return getAttachFileName(paramMessage);
          }
        }
      }
      else if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage))
      {
        if (paramMessage.media.webpage.document != null) {
          return getAttachFileName(paramMessage.media.webpage.document);
        }
        if (paramMessage.media.webpage.photo != null)
        {
          paramMessage = paramMessage.media.webpage.photo.sizes;
          if (paramMessage.size() > 0)
          {
            paramMessage = getClosestPhotoSizeWithSize(paramMessage, AndroidUtilities.getPhotoSize());
            if (paramMessage != null) {
              return getAttachFileName(paramMessage);
            }
          }
        }
        else if ((paramMessage.media instanceof TLRPC.TL_messageMediaInvoice))
        {
          return getAttachFileName(((TLRPC.TL_messageMediaInvoice)paramMessage.media).photo);
        }
      }
      else if ((paramMessage.media instanceof TLRPC.TL_messageMediaInvoice))
      {
        paramMessage = ((TLRPC.TL_messageMediaInvoice)paramMessage.media).photo;
        if (paramMessage != null) {
          return Utilities.MD5(paramMessage.url) + "." + ImageLoader.getHttpUrlExtension(paramMessage.url, getExtensionByMime(paramMessage.mime_type));
        }
      }
    }
    return "";
  }
  
  public static File getPathToAttach(TLObject paramTLObject)
  {
    return getPathToAttach(paramTLObject, null, false);
  }
  
  public static File getPathToAttach(TLObject paramTLObject, String paramString, boolean paramBoolean)
  {
    Object localObject = null;
    if (paramBoolean) {
      localObject = getDirectory(4);
    }
    while (localObject == null)
    {
      return new File("");
      if ((paramTLObject instanceof TLRPC.Document))
      {
        localObject = (TLRPC.Document)paramTLObject;
        if (((TLRPC.Document)localObject).key != null) {
          localObject = getDirectory(4);
        } else if (MessageObject.isVoiceDocument((TLRPC.Document)localObject)) {
          localObject = getDirectory(1);
        } else if (MessageObject.isVideoDocument((TLRPC.Document)localObject)) {
          localObject = getDirectory(2);
        } else {
          localObject = getDirectory(3);
        }
      }
      else if ((paramTLObject instanceof TLRPC.PhotoSize))
      {
        localObject = (TLRPC.PhotoSize)paramTLObject;
        if ((((TLRPC.PhotoSize)localObject).location == null) || (((TLRPC.PhotoSize)localObject).location.key != null) || ((((TLRPC.PhotoSize)localObject).location.volume_id == -2147483648L) && (((TLRPC.PhotoSize)localObject).location.local_id < 0)) || (((TLRPC.PhotoSize)localObject).size < 0)) {
          localObject = getDirectory(4);
        } else {
          localObject = getDirectory(0);
        }
      }
      else if ((paramTLObject instanceof TLRPC.FileLocation))
      {
        localObject = (TLRPC.FileLocation)paramTLObject;
        if ((((TLRPC.FileLocation)localObject).key != null) || ((((TLRPC.FileLocation)localObject).volume_id == -2147483648L) && (((TLRPC.FileLocation)localObject).local_id < 0))) {
          localObject = getDirectory(4);
        } else {
          localObject = getDirectory(0);
        }
      }
      else if ((paramTLObject instanceof TLRPC.TL_webDocument))
      {
        localObject = (TLRPC.TL_webDocument)paramTLObject;
        if (((TLRPC.TL_webDocument)localObject).mime_type.startsWith("image/")) {
          localObject = getDirectory(0);
        } else if (((TLRPC.TL_webDocument)localObject).mime_type.startsWith("audio/")) {
          localObject = getDirectory(1);
        } else if (((TLRPC.TL_webDocument)localObject).mime_type.startsWith("video/")) {
          localObject = getDirectory(2);
        } else {
          localObject = getDirectory(3);
        }
      }
    }
    return new File((File)localObject, getAttachFileName(paramTLObject, paramString));
  }
  
  public static File getPathToAttach(TLObject paramTLObject, boolean paramBoolean)
  {
    return getPathToAttach(paramTLObject, null, paramBoolean);
  }
  
  public static File getPathToMessage(TLRPC.Message paramMessage)
  {
    boolean bool1 = false;
    boolean bool2 = true;
    if (paramMessage == null) {
      return new File("");
    }
    if ((paramMessage instanceof TLRPC.TL_messageService))
    {
      if (paramMessage.action.photo != null)
      {
        paramMessage = paramMessage.action.photo.sizes;
        if (paramMessage.size() > 0)
        {
          paramMessage = getClosestPhotoSizeWithSize(paramMessage, AndroidUtilities.getPhotoSize());
          if (paramMessage != null) {
            return getPathToAttach(paramMessage);
          }
        }
      }
    }
    else
    {
      Object localObject;
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))
      {
        localObject = paramMessage.media.document;
        if (paramMessage.media.ttl_seconds != 0) {
          bool1 = true;
        }
        return getPathToAttach((TLObject)localObject, bool1);
      }
      if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto))
      {
        localObject = paramMessage.media.photo.sizes;
        if (((ArrayList)localObject).size() > 0)
        {
          localObject = getClosestPhotoSizeWithSize((ArrayList)localObject, AndroidUtilities.getPhotoSize());
          if (localObject != null)
          {
            if (paramMessage.media.ttl_seconds != 0) {}
            for (bool1 = bool2;; bool1 = false) {
              return getPathToAttach((TLObject)localObject, bool1);
            }
          }
        }
      }
      else if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage))
      {
        if (paramMessage.media.webpage.document != null) {
          return getPathToAttach(paramMessage.media.webpage.document);
        }
        if (paramMessage.media.webpage.photo != null)
        {
          paramMessage = paramMessage.media.webpage.photo.sizes;
          if (paramMessage.size() > 0)
          {
            paramMessage = getClosestPhotoSizeWithSize(paramMessage, AndroidUtilities.getPhotoSize());
            if (paramMessage != null) {
              return getPathToAttach(paramMessage);
            }
          }
        }
      }
      else if ((paramMessage.media instanceof TLRPC.TL_messageMediaInvoice))
      {
        return getPathToAttach(((TLRPC.TL_messageMediaInvoice)paramMessage.media).photo, true);
      }
    }
    return new File("");
  }
  
  private LinkedList<FileLoadOperation> getPhotoLoadOperationQueue(int paramInt)
  {
    LinkedList localLinkedList2 = (LinkedList)this.photoLoadOperationQueues.get(paramInt);
    LinkedList localLinkedList1 = localLinkedList2;
    if (localLinkedList2 == null)
    {
      localLinkedList1 = new LinkedList();
      this.photoLoadOperationQueues.put(paramInt, localLinkedList1);
    }
    return localLinkedList1;
  }
  
  public static FileStreamLoadOperation getStreamLoadOperation(TransferListener<? super DataSource> paramTransferListener)
  {
    return new FileStreamLoadOperation(paramTransferListener);
  }
  
  private void loadFile(final TLRPC.Document paramDocument, final TLRPC.TL_webDocument paramTL_webDocument, final TLRPC.FileLocation paramFileLocation, final String paramString, final int paramInt1, final boolean paramBoolean, final int paramInt2)
  {
    String str;
    if (paramFileLocation != null) {
      str = getAttachFileName(paramFileLocation, paramString);
    }
    for (;;)
    {
      if ((!TextUtils.isEmpty(str)) && (!str.contains("-2147483648"))) {
        this.loadOperationPathsUI.put(str, Boolean.valueOf(true));
      }
      fileLoaderQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          FileLoader.this.loadFileInternal(paramDocument, paramTL_webDocument, paramFileLocation, paramString, paramInt1, paramBoolean, null, 0, paramInt2);
        }
      });
      return;
      if (paramDocument != null) {
        str = getAttachFileName(paramDocument);
      } else if (paramTL_webDocument != null) {
        str = getAttachFileName(paramTL_webDocument);
      } else {
        str = null;
      }
    }
  }
  
  private FileLoadOperation loadFileInternal(final TLRPC.Document paramDocument, final TLRPC.TL_webDocument paramTL_webDocument, final TLRPC.FileLocation paramFileLocation, String paramString, final int paramInt1, boolean paramBoolean, FileStreamLoadOperation paramFileStreamLoadOperation, int paramInt2, int paramInt3)
  {
    final Object localObject1 = null;
    if (paramFileLocation != null)
    {
      localObject1 = getAttachFileName(paramFileLocation, paramString);
      if ((localObject1 != null) && (!((String)localObject1).contains("-2147483648"))) {
        break label63;
      }
      localObject1 = null;
    }
    label63:
    label129:
    Object localObject3;
    label284:
    label329:
    label413:
    label423:
    label483:
    label517:
    label529:
    label786:
    label809:
    label906:
    label933:
    do
    {
      do
      {
        Object localObject2;
        int i;
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
                      return (FileLoadOperation)localObject1;
                      if (paramDocument != null)
                      {
                        localObject1 = getAttachFileName(paramDocument);
                        break;
                      }
                      if (paramTL_webDocument == null) {
                        break;
                      }
                      localObject1 = getAttachFileName(paramTL_webDocument);
                      break;
                      if ((!TextUtils.isEmpty((CharSequence)localObject1)) && (!((String)localObject1).contains("-2147483648"))) {
                        this.loadOperationPathsUI.put(localObject1, Boolean.valueOf(true));
                      }
                      localObject2 = (FileLoadOperation)this.loadOperationPaths.get(localObject1);
                      if (localObject2 == null) {
                        break label483;
                      }
                      if (paramInt2 != 0) {
                        break label129;
                      }
                      localObject1 = localObject2;
                    } while (!paramBoolean);
                    paramInt1 = ((FileLoadOperation)localObject2).getDatacenterId();
                    paramString = getAudioLoadOperationQueue(paramInt1);
                    localObject3 = getPhotoLoadOperationQueue(paramInt1);
                    localObject4 = getLoadOperationQueue(paramInt1);
                    ((FileLoadOperation)localObject2).setForceRequest(true);
                    if ((MessageObject.isVoiceDocument(paramDocument)) || (MessageObject.isVoiceWebDocument(paramTL_webDocument))) {
                      paramDocument = paramString;
                    }
                    for (;;)
                    {
                      localObject1 = localObject2;
                      if (paramDocument == null) {
                        break;
                      }
                      paramInt3 = paramDocument.indexOf(localObject2);
                      if (paramInt3 <= 0) {
                        break label423;
                      }
                      paramDocument.remove(paramInt3);
                      if (paramInt2 == 0) {
                        break label413;
                      }
                      if (paramDocument != paramString) {
                        break label284;
                      }
                      localObject1 = localObject2;
                      if (!((FileLoadOperation)localObject2).start(paramFileStreamLoadOperation, paramInt2)) {
                        break;
                      }
                      this.currentAudioLoadOperationsCount.put(paramInt1, this.currentAudioLoadOperationsCount.get(paramInt1) + 1);
                      return (FileLoadOperation)localObject2;
                      if ((paramFileLocation != null) || (MessageObject.isImageWebDocument(paramTL_webDocument))) {
                        paramDocument = (TLRPC.Document)localObject3;
                      } else {
                        paramDocument = (TLRPC.Document)localObject4;
                      }
                    }
                    if (paramDocument != localObject3) {
                      break label329;
                    }
                    localObject1 = localObject2;
                  } while (!((FileLoadOperation)localObject2).start(paramFileStreamLoadOperation, paramInt2));
                  this.currentPhotoLoadOperationsCount.put(paramInt1, this.currentPhotoLoadOperationsCount.get(paramInt1) + 1);
                  return (FileLoadOperation)localObject2;
                  if (((FileLoadOperation)localObject2).start(paramFileStreamLoadOperation, paramInt2)) {
                    this.currentLoadOperationsCount.put(paramInt1, this.currentLoadOperationsCount.get(paramInt1) + 1);
                  }
                  localObject1 = localObject2;
                } while (!((FileLoadOperation)localObject2).wasStarted());
                localObject1 = localObject2;
              } while (this.activeFileLoadOperation.contains(localObject2));
              if (paramFileStreamLoadOperation != null) {
                pauseCurrentFileLoadOperations((FileLoadOperation)localObject2);
              }
              this.activeFileLoadOperation.add(localObject2);
              return (FileLoadOperation)localObject2;
              paramDocument.add(0, localObject2);
              return (FileLoadOperation)localObject2;
              if (paramFileStreamLoadOperation != null) {
                pauseCurrentFileLoadOperations((FileLoadOperation)localObject2);
              }
              ((FileLoadOperation)localObject2).start(paramFileStreamLoadOperation, paramInt2);
              localObject1 = localObject2;
            } while (paramDocument != localObject4);
            localObject1 = localObject2;
          } while (this.activeFileLoadOperation.contains(localObject2));
          this.activeFileLoadOperation.add(localObject2);
          return (FileLoadOperation)localObject2;
          Object localObject4 = getDirectory(4);
          localObject3 = localObject4;
          i = 4;
          if (paramFileLocation != null)
          {
            paramString = new FileLoadOperation(paramFileLocation, paramString, paramInt1);
            paramInt1 = 0;
            if (paramInt3 != 0) {
              break label786;
            }
            localObject2 = getDirectory(paramInt1);
            paramString.setPaths(this.currentAccount, (File)localObject2, (File)localObject4);
            paramString.setDelegate(new FileLoadOperation.FileLoadOperationDelegate()
            {
              public void didChangedLoadProgress(FileLoadOperation paramAnonymousFileLoadOperation, float paramAnonymousFloat)
              {
                if (FileLoader.this.delegate != null) {
                  FileLoader.this.delegate.fileLoadProgressChanged(localObject1, paramAnonymousFloat);
                }
              }
              
              public void didFailedLoadingFile(FileLoadOperation paramAnonymousFileLoadOperation, int paramAnonymousInt)
              {
                FileLoader.this.loadOperationPathsUI.remove(localObject1);
                FileLoader.this.checkDownloadQueue(paramAnonymousFileLoadOperation.getDatacenterId(), paramDocument, paramTL_webDocument, paramFileLocation, localObject1);
                if (FileLoader.this.delegate != null) {
                  FileLoader.this.delegate.fileDidFailedLoad(localObject1, paramAnonymousInt);
                }
              }
              
              public void didFinishLoadingFile(FileLoadOperation paramAnonymousFileLoadOperation, File paramAnonymousFile)
              {
                FileLoader.this.loadOperationPathsUI.remove(localObject1);
                if (FileLoader.this.delegate != null) {
                  FileLoader.this.delegate.fileDidLoaded(localObject1, paramAnonymousFile, paramInt1);
                }
                FileLoader.this.checkDownloadQueue(paramAnonymousFileLoadOperation.getDatacenterId(), paramDocument, paramTL_webDocument, paramFileLocation, localObject1);
              }
            });
            i = paramString.getDatacenterId();
            paramDocument = getAudioLoadOperationQueue(i);
            localObject2 = getPhotoLoadOperationQueue(i);
            localObject3 = getLoadOperationQueue(i);
            this.loadOperationPaths.put(localObject1, paramString);
            if (!paramBoolean) {
              break label809;
            }
            paramInt3 = 3;
          }
          for (;;)
          {
            if (paramInt1 == 1)
            {
              paramInt1 = this.currentAudioLoadOperationsCount.get(i);
              if ((paramInt2 != 0) || (paramInt1 < paramInt3))
              {
                localObject1 = paramString;
                if (!paramString.start(paramFileStreamLoadOperation, paramInt2)) {
                  break;
                }
                this.currentAudioLoadOperationsCount.put(i, paramInt1 + 1);
                return paramString;
                if (paramDocument != null)
                {
                  paramString = new FileLoadOperation(paramDocument);
                  if (MessageObject.isVoiceDocument(paramDocument))
                  {
                    paramInt1 = 1;
                    break label517;
                  }
                  if (MessageObject.isVideoDocument(paramDocument))
                  {
                    paramInt1 = 2;
                    break label517;
                  }
                  paramInt1 = 3;
                  break label517;
                }
                paramString = (String)localObject2;
                paramInt1 = i;
                if (paramTL_webDocument == null) {
                  break label517;
                }
                paramString = new FileLoadOperation(paramTL_webDocument);
                if (MessageObject.isVoiceWebDocument(paramTL_webDocument))
                {
                  paramInt1 = 1;
                  break label517;
                }
                if (MessageObject.isVideoWebDocument(paramTL_webDocument))
                {
                  paramInt1 = 2;
                  break label517;
                }
                if (MessageObject.isImageWebDocument(paramTL_webDocument))
                {
                  paramInt1 = 0;
                  break label517;
                }
                paramInt1 = 3;
                break label517;
                localObject2 = localObject3;
                if (paramInt3 != 2) {
                  break label529;
                }
                paramString.setEncryptFile(true);
                localObject2 = localObject3;
                break label529;
                paramInt3 = 1;
                continue;
              }
              if (paramBoolean)
              {
                paramDocument.add(0, paramString);
                return paramString;
              }
              paramDocument.add(paramString);
              return paramString;
            }
          }
          if ((paramFileLocation == null) && (!MessageObject.isImageWebDocument(paramTL_webDocument))) {
            break label933;
          }
          paramInt1 = this.currentPhotoLoadOperationsCount.get(i);
          if ((paramInt2 == 0) && (paramInt1 >= paramInt3)) {
            break label906;
          }
          localObject1 = paramString;
        } while (!paramString.start(paramFileStreamLoadOperation, paramInt2));
        this.currentPhotoLoadOperationsCount.put(i, paramInt1 + 1);
        return paramString;
        if (paramBoolean)
        {
          ((LinkedList)localObject2).add(0, paramString);
          return paramString;
        }
        ((LinkedList)localObject2).add(paramString);
        return paramString;
        paramInt1 = this.currentLoadOperationsCount.get(i);
        if ((paramInt2 == 0) && (paramInt1 >= paramInt3)) {
          break label1021;
        }
        if (paramString.start(paramFileStreamLoadOperation, paramInt2))
        {
          this.currentLoadOperationsCount.put(i, paramInt1 + 1);
          this.activeFileLoadOperation.add(paramString);
        }
        localObject1 = paramString;
      } while (!paramString.wasStarted());
      localObject1 = paramString;
    } while (paramFileStreamLoadOperation == null);
    pauseCurrentFileLoadOperations(paramString);
    return paramString;
    label1021:
    if (paramBoolean)
    {
      ((LinkedList)localObject3).add(0, paramString);
      return paramString;
    }
    ((LinkedList)localObject3).add(paramString);
    return paramString;
  }
  
  private void pauseCurrentFileLoadOperations(FileLoadOperation paramFileLoadOperation)
  {
    int i = 0;
    if (i < this.activeFileLoadOperation.size())
    {
      FileLoadOperation localFileLoadOperation = (FileLoadOperation)this.activeFileLoadOperation.get(i);
      if (localFileLoadOperation == paramFileLoadOperation) {}
      for (;;)
      {
        i += 1;
        break;
        this.activeFileLoadOperation.remove(localFileLoadOperation);
        int j = i - 1;
        localFileLoadOperation.pause();
        int k = localFileLoadOperation.getDatacenterId();
        getLoadOperationQueue(k).add(0, localFileLoadOperation);
        i = j;
        if (localFileLoadOperation.wasStarted())
        {
          this.currentLoadOperationsCount.put(k, this.currentLoadOperationsCount.get(k) - 1);
          i = j;
        }
      }
    }
  }
  
  public static void setMediaDirs(SparseArray<File> paramSparseArray)
  {
    mediaDirs = paramSparseArray;
  }
  
  public void cancelLoadFile(TLRPC.Document paramDocument)
  {
    cancelLoadFile(paramDocument, null, null, null);
  }
  
  public void cancelLoadFile(TLRPC.FileLocation paramFileLocation, String paramString)
  {
    cancelLoadFile(null, null, paramFileLocation, paramString);
  }
  
  public void cancelLoadFile(TLRPC.PhotoSize paramPhotoSize)
  {
    cancelLoadFile(null, null, paramPhotoSize.location, null);
  }
  
  public void cancelLoadFile(TLRPC.TL_webDocument paramTL_webDocument)
  {
    cancelLoadFile(null, paramTL_webDocument, null, null);
  }
  
  public void cancelUploadFile(final String paramString, final boolean paramBoolean)
  {
    fileLoaderQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (!paramBoolean) {}
        for (FileUploadOperation localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationPaths.get(paramString);; localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationPathsEnc.get(paramString))
        {
          FileLoader.this.uploadSizes.remove(paramString);
          if (localFileUploadOperation != null)
          {
            FileLoader.this.uploadOperationPathsEnc.remove(paramString);
            FileLoader.this.uploadOperationQueue.remove(localFileUploadOperation);
            FileLoader.this.uploadSmallOperationQueue.remove(localFileUploadOperation);
            localFileUploadOperation.cancel();
          }
          return;
        }
      }
    });
  }
  
  public void checkUploadNewDataAvailable(final String paramString, final boolean paramBoolean, final long paramLong1, long paramLong2)
  {
    fileLoaderQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        FileUploadOperation localFileUploadOperation;
        if (paramBoolean)
        {
          localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationPathsEnc.get(paramString);
          if (localFileUploadOperation == null) {
            break label63;
          }
          localFileUploadOperation.checkNewDataAvailable(paramLong1, this.val$finalSize);
        }
        label63:
        while (this.val$finalSize == 0L)
        {
          return;
          localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationPaths.get(paramString);
          break;
        }
        FileLoader.this.uploadSizes.put(paramString, Long.valueOf(this.val$finalSize));
      }
    });
  }
  
  public void deleteFiles(final ArrayList<File> paramArrayList, final int paramInt)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    fileLoaderQueue.postRunnable(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: iconst_0
        //   1: istore_1
        //   2: iload_1
        //   3: aload_0
        //   4: getfield 23	org/telegram/messenger/FileLoader$9:val$files	Ljava/util/ArrayList;
        //   7: invokevirtual 38	java/util/ArrayList:size	()I
        //   10: if_icmpge +219 -> 229
        //   13: aload_0
        //   14: getfield 23	org/telegram/messenger/FileLoader$9:val$files	Ljava/util/ArrayList;
        //   17: iload_1
        //   18: invokevirtual 42	java/util/ArrayList:get	(I)Ljava/lang/Object;
        //   21: checkcast 44	java/io/File
        //   24: astore_2
        //   25: new 44	java/io/File
        //   28: dup
        //   29: new 46	java/lang/StringBuilder
        //   32: dup
        //   33: invokespecial 47	java/lang/StringBuilder:<init>	()V
        //   36: aload_2
        //   37: invokevirtual 51	java/io/File:getAbsolutePath	()Ljava/lang/String;
        //   40: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   43: ldc 57
        //   45: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   48: invokevirtual 60	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   51: invokespecial 63	java/io/File:<init>	(Ljava/lang/String;)V
        //   54: astore_3
        //   55: aload_3
        //   56: invokevirtual 67	java/io/File:exists	()Z
        //   59: ifeq +133 -> 192
        //   62: aload_3
        //   63: invokevirtual 70	java/io/File:delete	()Z
        //   66: ifne +7 -> 73
        //   69: aload_3
        //   70: invokevirtual 73	java/io/File:deleteOnExit	()V
        //   73: new 44	java/io/File
        //   76: dup
        //   77: invokestatic 77	org/telegram/messenger/FileLoader:getInternalCacheDir	()Ljava/io/File;
        //   80: new 46	java/lang/StringBuilder
        //   83: dup
        //   84: invokespecial 47	java/lang/StringBuilder:<init>	()V
        //   87: aload_2
        //   88: invokevirtual 80	java/io/File:getName	()Ljava/lang/String;
        //   91: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   94: ldc 82
        //   96: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   99: invokevirtual 60	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   102: invokespecial 85	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
        //   105: astore_3
        //   106: aload_3
        //   107: invokevirtual 70	java/io/File:delete	()Z
        //   110: ifne +7 -> 117
        //   113: aload_3
        //   114: invokevirtual 73	java/io/File:deleteOnExit	()V
        //   117: new 44	java/io/File
        //   120: dup
        //   121: aload_2
        //   122: invokevirtual 88	java/io/File:getParentFile	()Ljava/io/File;
        //   125: new 46	java/lang/StringBuilder
        //   128: dup
        //   129: invokespecial 47	java/lang/StringBuilder:<init>	()V
        //   132: ldc 90
        //   134: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   137: aload_2
        //   138: invokevirtual 80	java/io/File:getName	()Ljava/lang/String;
        //   141: invokevirtual 55	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
        //   144: invokevirtual 60	java/lang/StringBuilder:toString	()Ljava/lang/String;
        //   147: invokespecial 85	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
        //   150: astore_2
        //   151: aload_2
        //   152: invokevirtual 67	java/io/File:exists	()Z
        //   155: ifeq +14 -> 169
        //   158: aload_2
        //   159: invokevirtual 70	java/io/File:delete	()Z
        //   162: ifne +7 -> 169
        //   165: aload_2
        //   166: invokevirtual 73	java/io/File:deleteOnExit	()V
        //   169: iload_1
        //   170: iconst_1
        //   171: iadd
        //   172: istore_1
        //   173: goto -171 -> 2
        //   176: astore_3
        //   177: aload_3
        //   178: invokestatic 96	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   181: goto -108 -> 73
        //   184: astore_3
        //   185: aload_3
        //   186: invokestatic 96	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   189: goto -72 -> 117
        //   192: aload_2
        //   193: invokevirtual 67	java/io/File:exists	()Z
        //   196: ifeq -79 -> 117
        //   199: aload_2
        //   200: invokevirtual 70	java/io/File:delete	()Z
        //   203: ifne -86 -> 117
        //   206: aload_2
        //   207: invokevirtual 73	java/io/File:deleteOnExit	()V
        //   210: goto -93 -> 117
        //   213: astore_3
        //   214: aload_3
        //   215: invokestatic 96	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   218: goto -101 -> 117
        //   221: astore_2
        //   222: aload_2
        //   223: invokestatic 96	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   226: goto -57 -> 169
        //   229: aload_0
        //   230: getfield 25	org/telegram/messenger/FileLoader$9:val$type	I
        //   233: iconst_2
        //   234: if_icmpne +9 -> 243
        //   237: invokestatic 102	org/telegram/messenger/ImageLoader:getInstance	()Lorg/telegram/messenger/ImageLoader;
        //   240: invokevirtual 105	org/telegram/messenger/ImageLoader:clearMemory	()V
        //   243: return
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	244	0	this	9
        //   1	172	1	i	int
        //   24	183	2	localFile1	File
        //   221	2	2	localException1	Exception
        //   54	60	3	localFile2	File
        //   176	2	3	localException2	Exception
        //   184	2	3	localException3	Exception
        //   213	2	3	localException4	Exception
        // Exception table:
        //   from	to	target	type
        //   62	73	176	java/lang/Exception
        //   73	117	184	java/lang/Exception
        //   199	210	213	java/lang/Exception
        //   117	169	221	java/lang/Exception
      }
    });
  }
  
  public float getBufferedProgressFromPosition(float paramFloat, String paramString)
  {
    if (TextUtils.isEmpty(paramString)) {}
    do
    {
      return 0.0F;
      paramString = (FileLoadOperation)this.loadOperationPaths.get(paramString);
    } while (paramString == null);
    return paramString.getDownloadedLengthFromOffset(paramFloat);
  }
  
  public boolean isLoadingFile(String paramString)
  {
    return this.loadOperationPathsUI.containsKey(paramString);
  }
  
  public void loadFile(TLRPC.Document paramDocument, boolean paramBoolean, int paramInt)
  {
    if (paramDocument == null) {
      return;
    }
    int i = paramInt;
    if (paramInt == 0)
    {
      i = paramInt;
      if (paramDocument != null)
      {
        i = paramInt;
        if (paramDocument.key != null) {
          i = 1;
        }
      }
    }
    loadFile(paramDocument, null, null, null, 0, paramBoolean, i);
  }
  
  public void loadFile(TLRPC.FileLocation paramFileLocation, String paramString, int paramInt1, int paramInt2)
  {
    if (paramFileLocation == null) {
      return;
    }
    int i = paramInt2;
    if (paramInt2 == 0) {
      if (paramInt1 != 0)
      {
        i = paramInt2;
        if (paramFileLocation != null)
        {
          i = paramInt2;
          if (paramFileLocation.key == null) {}
        }
      }
      else
      {
        i = 1;
      }
    }
    loadFile(null, null, paramFileLocation, paramString, paramInt1, true, i);
  }
  
  public void loadFile(TLRPC.PhotoSize paramPhotoSize, String paramString, int paramInt)
  {
    if (paramPhotoSize == null) {
      return;
    }
    int i = paramInt;
    if (paramInt == 0)
    {
      i = paramInt;
      if (paramPhotoSize != null) {
        if (paramPhotoSize.size != 0)
        {
          i = paramInt;
          if (paramPhotoSize.location.key == null) {}
        }
        else
        {
          i = 1;
        }
      }
    }
    loadFile(null, null, paramPhotoSize.location, paramString, paramPhotoSize.size, false, i);
  }
  
  public void loadFile(TLRPC.TL_webDocument paramTL_webDocument, boolean paramBoolean, int paramInt)
  {
    loadFile(null, paramTL_webDocument, null, null, 0, paramBoolean, paramInt);
  }
  
  protected FileLoadOperation loadStreamFile(final FileStreamLoadOperation paramFileStreamLoadOperation, final TLRPC.Document paramDocument, final int paramInt)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final FileLoadOperation[] arrayOfFileLoadOperation = new FileLoadOperation[1];
    fileLoaderQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        arrayOfFileLoadOperation[0] = FileLoader.this.loadFileInternal(paramDocument, null, null, null, 0, true, paramFileStreamLoadOperation, paramInt, 0);
        localCountDownLatch.countDown();
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfFileLoadOperation[0];
    }
    catch (Exception paramFileStreamLoadOperation)
    {
      for (;;)
      {
        FileLog.e(paramFileStreamLoadOperation);
      }
    }
  }
  
  public void setDelegate(FileLoaderDelegate paramFileLoaderDelegate)
  {
    this.delegate = paramFileLoaderDelegate;
  }
  
  public void uploadFile(String paramString, boolean paramBoolean1, boolean paramBoolean2, int paramInt)
  {
    uploadFile(paramString, paramBoolean1, paramBoolean2, 0, paramInt);
  }
  
  public void uploadFile(final String paramString, final boolean paramBoolean1, final boolean paramBoolean2, final int paramInt1, final int paramInt2)
  {
    if (paramString == null) {
      return;
    }
    fileLoaderQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (paramBoolean1)
        {
          if (!FileLoader.this.uploadOperationPathsEnc.containsKey(paramString)) {}
        }
        else {
          while (FileLoader.this.uploadOperationPaths.containsKey(paramString)) {
            return;
          }
        }
        int j = paramInt1;
        int i = j;
        if (j != 0)
        {
          i = j;
          if ((Long)FileLoader.this.uploadSizes.get(paramString) != null)
          {
            i = 0;
            FileLoader.this.uploadSizes.remove(paramString);
          }
        }
        FileUploadOperation localFileUploadOperation = new FileUploadOperation(FileLoader.this.currentAccount, paramString, paramBoolean1, i, paramInt2);
        if (paramBoolean1) {
          FileLoader.this.uploadOperationPathsEnc.put(paramString, localFileUploadOperation);
        }
        for (;;)
        {
          localFileUploadOperation.setDelegate(new FileUploadOperation.FileUploadOperationDelegate()
          {
            public void didChangedUploadProgress(FileUploadOperation paramAnonymous2FileUploadOperation, float paramAnonymous2Float)
            {
              if (FileLoader.this.delegate != null) {
                FileLoader.this.delegate.fileUploadProgressChanged(FileLoader.3.this.val$location, paramAnonymous2Float, FileLoader.3.this.val$encrypted);
              }
            }
            
            public void didFailedUploadingFile(FileUploadOperation paramAnonymous2FileUploadOperation)
            {
              FileLoader.fileLoaderQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  FileUploadOperation localFileUploadOperation;
                  if (FileLoader.3.this.val$encrypted)
                  {
                    FileLoader.this.uploadOperationPathsEnc.remove(FileLoader.3.this.val$location);
                    if (FileLoader.this.delegate != null) {
                      FileLoader.this.delegate.fileDidFailedUpload(FileLoader.3.this.val$location, FileLoader.3.this.val$encrypted);
                    }
                    if (!FileLoader.3.this.val$small) {
                      break label211;
                    }
                    FileLoader.access$610(FileLoader.this);
                    if (FileLoader.this.currentUploadSmallOperationsCount < 1)
                    {
                      localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadSmallOperationQueue.poll();
                      if (localFileUploadOperation != null)
                      {
                        FileLoader.access$608(FileLoader.this);
                        localFileUploadOperation.start();
                      }
                    }
                  }
                  label211:
                  do
                  {
                    do
                    {
                      return;
                      FileLoader.this.uploadOperationPaths.remove(FileLoader.3.this.val$location);
                      break;
                      FileLoader.access$710(FileLoader.this);
                    } while (FileLoader.this.currentUploadOperationsCount >= 1);
                    localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationQueue.poll();
                  } while (localFileUploadOperation == null);
                  FileLoader.access$708(FileLoader.this);
                  localFileUploadOperation.start();
                }
              });
            }
            
            public void didFinishUploadingFile(final FileUploadOperation paramAnonymous2FileUploadOperation, final TLRPC.InputFile paramAnonymous2InputFile, final TLRPC.InputEncryptedFile paramAnonymous2InputEncryptedFile, final byte[] paramAnonymous2ArrayOfByte1, final byte[] paramAnonymous2ArrayOfByte2)
            {
              FileLoader.fileLoaderQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  FileUploadOperation localFileUploadOperation;
                  if (FileLoader.3.this.val$encrypted)
                  {
                    FileLoader.this.uploadOperationPathsEnc.remove(FileLoader.3.this.val$location);
                    if (!FileLoader.3.this.val$small) {
                      break label224;
                    }
                    FileLoader.access$610(FileLoader.this);
                    if (FileLoader.this.currentUploadSmallOperationsCount < 1)
                    {
                      localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadSmallOperationQueue.poll();
                      if (localFileUploadOperation != null)
                      {
                        FileLoader.access$608(FileLoader.this);
                        localFileUploadOperation.start();
                      }
                    }
                  }
                  for (;;)
                  {
                    if (FileLoader.this.delegate != null) {
                      FileLoader.this.delegate.fileDidUploaded(FileLoader.3.this.val$location, paramAnonymous2InputFile, paramAnonymous2InputEncryptedFile, paramAnonymous2ArrayOfByte1, paramAnonymous2ArrayOfByte2, paramAnonymous2FileUploadOperation.getTotalFileSize());
                    }
                    return;
                    FileLoader.this.uploadOperationPaths.remove(FileLoader.3.this.val$location);
                    break;
                    label224:
                    FileLoader.access$710(FileLoader.this);
                    if (FileLoader.this.currentUploadOperationsCount < 1)
                    {
                      localFileUploadOperation = (FileUploadOperation)FileLoader.this.uploadOperationQueue.poll();
                      if (localFileUploadOperation != null)
                      {
                        FileLoader.access$708(FileLoader.this);
                        localFileUploadOperation.start();
                      }
                    }
                  }
                }
              });
            }
          });
          if (!paramBoolean2) {
            break label218;
          }
          if (FileLoader.this.currentUploadSmallOperationsCount >= 1) {
            break;
          }
          FileLoader.access$608(FileLoader.this);
          localFileUploadOperation.start();
          return;
          FileLoader.this.uploadOperationPaths.put(paramString, localFileUploadOperation);
        }
        FileLoader.this.uploadSmallOperationQueue.add(localFileUploadOperation);
        return;
        label218:
        if (FileLoader.this.currentUploadOperationsCount < 1)
        {
          FileLoader.access$708(FileLoader.this);
          localFileUploadOperation.start();
          return;
        }
        FileLoader.this.uploadOperationQueue.add(localFileUploadOperation);
      }
    });
  }
  
  public static abstract interface FileLoaderDelegate
  {
    public abstract void fileDidFailedLoad(String paramString, int paramInt);
    
    public abstract void fileDidFailedUpload(String paramString, boolean paramBoolean);
    
    public abstract void fileDidLoaded(String paramString, File paramFile, int paramInt);
    
    public abstract void fileDidUploaded(String paramString, TLRPC.InputFile paramInputFile, TLRPC.InputEncryptedFile paramInputEncryptedFile, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long paramLong);
    
    public abstract void fileLoadProgressChanged(String paramString, float paramFloat);
    
    public abstract void fileUploadProgressChanged(String paramString, float paramFloat, boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/FileLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */