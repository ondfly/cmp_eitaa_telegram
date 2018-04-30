package org.telegram.messenger;

import android.util.SparseArray;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFileLocation;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileHash;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_inputDocumentFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputWebFileLocation;
import org.telegram.tgnet.TLRPC.TL_upload_cdnFile;
import org.telegram.tgnet.TLRPC.TL_upload_cdnFileReuploadNeeded;
import org.telegram.tgnet.TLRPC.TL_upload_file;
import org.telegram.tgnet.TLRPC.TL_upload_fileCdnRedirect;
import org.telegram.tgnet.TLRPC.TL_upload_getCdnFile;
import org.telegram.tgnet.TLRPC.TL_upload_getCdnFileHashes;
import org.telegram.tgnet.TLRPC.TL_upload_getFile;
import org.telegram.tgnet.TLRPC.TL_upload_getWebFile;
import org.telegram.tgnet.TLRPC.TL_upload_reuploadCdnFile;
import org.telegram.tgnet.TLRPC.TL_upload_webFile;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.Vector;

public class FileLoadOperation
{
  private static final int bigFileSizeFrom = 1048576;
  private static final int cdnChunkCheckSize = 131072;
  private static final int downloadChunkSize = 32768;
  private static final int downloadChunkSizeBig = 131072;
  private static final int maxCdnParts = 12288;
  private static final int maxDownloadRequests = 4;
  private static final int maxDownloadRequestsBig = 4;
  private static final int stateDownloading = 1;
  private static final int stateFailed = 2;
  private static final int stateFinished = 3;
  private static final int stateIdle = 0;
  private boolean allowDisordererFileSave;
  private int bytesCountPadding;
  private File cacheFileFinal;
  private File cacheFileParts;
  private File cacheFileTemp;
  private File cacheIvTemp;
  private byte[] cdnCheckBytes;
  private int cdnDatacenterId;
  private SparseArray<TLRPC.TL_fileHash> cdnHashes;
  private byte[] cdnIv;
  private byte[] cdnKey;
  private byte[] cdnToken;
  private int currentAccount;
  private int currentDownloadChunkSize;
  private int currentMaxDownloadRequests;
  private int currentType;
  private int datacenterId;
  private ArrayList<RequestInfo> delayedRequestInfos;
  private FileLoadOperationDelegate delegate;
  private volatile int downloadedBytes;
  private boolean encryptFile;
  private byte[] encryptIv;
  private byte[] encryptKey;
  private String ext;
  private RandomAccessFile fileOutputStream;
  private RandomAccessFile filePartsStream;
  private RandomAccessFile fileReadStream;
  private RandomAccessFile fiv;
  private int initialDatacenterId;
  private boolean isCdn;
  private boolean isForceRequest;
  private byte[] iv;
  private byte[] key;
  private TLRPC.InputFileLocation location;
  private ArrayList<Range> notCheckedCdnRanges;
  private ArrayList<Range> notLoadedBytesRanges;
  private volatile ArrayList<Range> notLoadedBytesRangesCopy;
  private ArrayList<Range> notRequestedBytesRanges;
  private volatile boolean paused;
  private int renameRetryCount;
  private ArrayList<RequestInfo> requestInfos;
  private int requestedBytesCount;
  private boolean requestingCdnOffsets;
  private int requestsCount;
  private boolean reuploadingCdn;
  private boolean started;
  private volatile int state = 0;
  private File storePath;
  private ArrayList<FileStreamLoadOperation> streamListeners;
  private int streamStartOffset;
  private File tempPath;
  private int totalBytesCount;
  private TLRPC.TL_inputWebFileLocation webLocation;
  
  public FileLoadOperation(TLRPC.Document paramDocument)
  {
    for (;;)
    {
      try
      {
        int j;
        if ((paramDocument instanceof TLRPC.TL_documentEncrypted))
        {
          this.location = new TLRPC.TL_inputEncryptedFileLocation();
          this.location.id = paramDocument.id;
          this.location.access_hash = paramDocument.access_hash;
          j = paramDocument.dc_id;
          this.datacenterId = j;
          this.initialDatacenterId = j;
          this.iv = new byte[32];
          System.arraycopy(paramDocument.iv, 0, this.iv, 0, this.iv.length);
          this.key = paramDocument.key;
          this.totalBytesCount = paramDocument.size;
          if ((this.key != null) && (this.totalBytesCount % 16 != 0))
          {
            this.bytesCountPadding = (16 - this.totalBytesCount % 16);
            this.totalBytesCount += this.bytesCountPadding;
          }
          this.ext = FileLoader.getDocumentFileName(paramDocument);
          if (this.ext != null)
          {
            j = this.ext.lastIndexOf('.');
            if (j != -1) {
              break label342;
            }
          }
          this.ext = "";
          if (!"audio/ogg".equals(paramDocument.mime_type)) {
            break label357;
          }
          this.currentType = 50331648;
          if (this.ext.length() > 1) {
            break;
          }
          if (paramDocument.mime_type == null) {
            break label429;
          }
          paramDocument = paramDocument.mime_type;
        }
        switch (paramDocument.hashCode())
        {
        case 1331848029: 
          this.ext = "";
          return;
          if (!(paramDocument instanceof TLRPC.TL_document)) {
            continue;
          }
          this.location = new TLRPC.TL_inputDocumentFileLocation();
          this.location.id = paramDocument.id;
          this.location.access_hash = paramDocument.access_hash;
          j = paramDocument.dc_id;
          this.datacenterId = j;
          this.initialDatacenterId = j;
          this.allowDisordererFileSave = true;
          continue;
          this.ext = this.ext.substring(j);
        }
      }
      catch (Exception paramDocument)
      {
        FileLog.e(paramDocument);
        onFail(true, 0);
        return;
      }
      label342:
      continue;
      label357:
      if ("video/mp4".equals(paramDocument.mime_type))
      {
        this.currentType = 33554432;
      }
      else
      {
        this.currentType = 67108864;
        continue;
        if (paramDocument.equals("video/mp4"))
        {
          i = 0;
          break label436;
          if (paramDocument.equals("audio/ogg"))
          {
            i = 1;
            break label436;
            this.ext = ".mp4";
            return;
            this.ext = ".ogg";
            return;
            label429:
            this.ext = "";
            return;
          }
        }
        label436:
        switch (i)
        {
        }
      }
    }
  }
  
  public FileLoadOperation(TLRPC.FileLocation paramFileLocation, String paramString, int paramInt)
  {
    int i;
    if ((paramFileLocation instanceof TLRPC.TL_fileEncryptedLocation))
    {
      this.location = new TLRPC.TL_inputEncryptedFileLocation();
      this.location.id = paramFileLocation.volume_id;
      this.location.volume_id = paramFileLocation.volume_id;
      this.location.access_hash = paramFileLocation.secret;
      this.location.local_id = paramFileLocation.local_id;
      this.iv = new byte[32];
      System.arraycopy(paramFileLocation.iv, 0, this.iv, 0, this.iv.length);
      this.key = paramFileLocation.key;
      i = paramFileLocation.dc_id;
      this.datacenterId = i;
      this.initialDatacenterId = i;
      this.currentType = 16777216;
      this.totalBytesCount = paramInt;
      if (paramString == null) {
        break label222;
      }
    }
    for (;;)
    {
      this.ext = paramString;
      return;
      if (!(paramFileLocation instanceof TLRPC.TL_fileLocation)) {
        break;
      }
      this.location = new TLRPC.TL_inputFileLocation();
      this.location.volume_id = paramFileLocation.volume_id;
      this.location.secret = paramFileLocation.secret;
      this.location.local_id = paramFileLocation.local_id;
      i = paramFileLocation.dc_id;
      this.datacenterId = i;
      this.initialDatacenterId = i;
      this.allowDisordererFileSave = true;
      break;
      label222:
      paramString = "jpg";
    }
  }
  
  public FileLoadOperation(TLRPC.TL_webDocument paramTL_webDocument)
  {
    this.webLocation = new TLRPC.TL_inputWebFileLocation();
    this.webLocation.url = paramTL_webDocument.url;
    this.webLocation.access_hash = paramTL_webDocument.access_hash;
    this.totalBytesCount = paramTL_webDocument.size;
    int i = paramTL_webDocument.dc_id;
    this.datacenterId = i;
    this.initialDatacenterId = i;
    String str = FileLoader.getExtensionByMime(paramTL_webDocument.mime_type);
    if (paramTL_webDocument.mime_type.startsWith("image/")) {
      this.currentType = 16777216;
    }
    for (;;)
    {
      this.allowDisordererFileSave = true;
      this.ext = ImageLoader.getHttpUrlExtension(paramTL_webDocument.url, str);
      return;
      if (paramTL_webDocument.mime_type.equals("audio/ogg")) {
        this.currentType = 50331648;
      } else if (paramTL_webDocument.mime_type.startsWith("video/")) {
        this.currentType = 33554432;
      } else {
        this.currentType = 67108864;
      }
    }
  }
  
  private void addPart(ArrayList<Range> paramArrayList, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramInt2 < paramInt1)) {}
    label173:
    label293:
    do
    {
      for (;;)
      {
        return;
        int k = 0;
        int m = paramArrayList.size();
        int j = 0;
        for (;;)
        {
          int i = k;
          Range localRange;
          if (j < m)
          {
            localRange = (Range)paramArrayList.get(j);
            if (paramInt1 > localRange.start) {
              break label173;
            }
            if (paramInt2 >= localRange.end)
            {
              paramArrayList.remove(j);
              i = 1;
            }
          }
          else
          {
            if (!paramBoolean) {
              break;
            }
            if (i == 0) {
              break label293;
            }
          }
          try
          {
            this.filePartsStream.seek(0L);
            paramInt2 = paramArrayList.size();
            this.filePartsStream.writeInt(paramInt2);
            paramInt1 = 0;
            for (;;)
            {
              if (paramInt1 < paramInt2)
              {
                localRange = (Range)paramArrayList.get(paramInt1);
                this.filePartsStream.writeInt(localRange.start);
                this.filePartsStream.writeInt(localRange.end);
                paramInt1 += 1;
                continue;
                if (paramInt2 > localRange.start)
                {
                  Range.access$102(localRange, paramInt2);
                  i = 1;
                  break;
                  if (paramInt2 < localRange.end)
                  {
                    paramArrayList.add(0, new Range(localRange.start, paramInt1, null));
                    i = 1;
                    Range.access$102(localRange, paramInt2);
                    break;
                  }
                  if (paramInt1 < localRange.end)
                  {
                    Range.access$002(localRange, paramInt1);
                    i = 1;
                    break;
                  }
                }
                j += 1;
              }
            }
          }
          catch (Exception paramArrayList)
          {
            FileLog.e(paramArrayList);
          }
        }
        if (this.streamListeners != null)
        {
          paramInt2 = this.streamListeners.size();
          paramInt1 = 0;
          while (paramInt1 < paramInt2)
          {
            ((FileStreamLoadOperation)this.streamListeners.get(paramInt1)).newDataAvailable();
            paramInt1 += 1;
          }
        }
      }
    } while (!BuildVars.LOGS_ENABLED);
    FileLog.e(this.cacheFileFinal + " downloaded duplicate file part " + paramInt1 + " - " + paramInt2);
  }
  
  /* Error */
  private void cleanup()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 512	org/telegram/messenger/FileLoadOperation:fileOutputStream	Ljava/io/RandomAccessFile;
    //   4: astore_2
    //   5: aload_2
    //   6: ifnull +25 -> 31
    //   9: aload_0
    //   10: getfield 512	org/telegram/messenger/FileLoadOperation:fileOutputStream	Ljava/io/RandomAccessFile;
    //   13: invokevirtual 516	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
    //   16: invokevirtual 521	java/nio/channels/FileChannel:close	()V
    //   19: aload_0
    //   20: getfield 512	org/telegram/messenger/FileLoadOperation:fileOutputStream	Ljava/io/RandomAccessFile;
    //   23: invokevirtual 522	java/io/RandomAccessFile:close	()V
    //   26: aload_0
    //   27: aconst_null
    //   28: putfield 512	org/telegram/messenger/FileLoadOperation:fileOutputStream	Ljava/io/RandomAccessFile;
    //   31: aload_0
    //   32: getfield 524	org/telegram/messenger/FileLoadOperation:fileReadStream	Ljava/io/RandomAccessFile;
    //   35: astore_2
    //   36: aload_2
    //   37: ifnull +25 -> 62
    //   40: aload_0
    //   41: getfield 524	org/telegram/messenger/FileLoadOperation:fileReadStream	Ljava/io/RandomAccessFile;
    //   44: invokevirtual 516	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
    //   47: invokevirtual 521	java/nio/channels/FileChannel:close	()V
    //   50: aload_0
    //   51: getfield 524	org/telegram/messenger/FileLoadOperation:fileReadStream	Ljava/io/RandomAccessFile;
    //   54: invokevirtual 522	java/io/RandomAccessFile:close	()V
    //   57: aload_0
    //   58: aconst_null
    //   59: putfield 524	org/telegram/messenger/FileLoadOperation:fileReadStream	Ljava/io/RandomAccessFile;
    //   62: aload_0
    //   63: getfield 449	org/telegram/messenger/FileLoadOperation:filePartsStream	Ljava/io/RandomAccessFile;
    //   66: astore_2
    //   67: aload_2
    //   68: ifnull +25 -> 93
    //   71: aload_0
    //   72: getfield 449	org/telegram/messenger/FileLoadOperation:filePartsStream	Ljava/io/RandomAccessFile;
    //   75: invokevirtual 516	java/io/RandomAccessFile:getChannel	()Ljava/nio/channels/FileChannel;
    //   78: invokevirtual 521	java/nio/channels/FileChannel:close	()V
    //   81: aload_0
    //   82: getfield 449	org/telegram/messenger/FileLoadOperation:filePartsStream	Ljava/io/RandomAccessFile;
    //   85: invokevirtual 522	java/io/RandomAccessFile:close	()V
    //   88: aload_0
    //   89: aconst_null
    //   90: putfield 449	org/telegram/messenger/FileLoadOperation:filePartsStream	Ljava/io/RandomAccessFile;
    //   93: aload_0
    //   94: getfield 526	org/telegram/messenger/FileLoadOperation:fiv	Ljava/io/RandomAccessFile;
    //   97: ifnull +15 -> 112
    //   100: aload_0
    //   101: getfield 526	org/telegram/messenger/FileLoadOperation:fiv	Ljava/io/RandomAccessFile;
    //   104: invokevirtual 522	java/io/RandomAccessFile:close	()V
    //   107: aload_0
    //   108: aconst_null
    //   109: putfield 526	org/telegram/messenger/FileLoadOperation:fiv	Ljava/io/RandomAccessFile;
    //   112: aload_0
    //   113: getfield 357	org/telegram/messenger/FileLoadOperation:delayedRequestInfos	Ljava/util/ArrayList;
    //   116: ifnull +170 -> 286
    //   119: iconst_0
    //   120: istore_1
    //   121: iload_1
    //   122: aload_0
    //   123: getfield 357	org/telegram/messenger/FileLoadOperation:delayedRequestInfos	Ljava/util/ArrayList;
    //   126: invokevirtual 433	java/util/ArrayList:size	()I
    //   129: if_icmpge +150 -> 279
    //   132: aload_0
    //   133: getfield 357	org/telegram/messenger/FileLoadOperation:delayedRequestInfos	Ljava/util/ArrayList;
    //   136: iload_1
    //   137: invokevirtual 437	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   140: checkcast 38	org/telegram/messenger/FileLoadOperation$RequestInfo
    //   143: astore_2
    //   144: aload_2
    //   145: invokestatic 530	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2000	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_file;
    //   148: ifnull +81 -> 229
    //   151: aload_2
    //   152: invokestatic 530	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2000	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_file;
    //   155: iconst_0
    //   156: putfield 535	org/telegram/tgnet/TLRPC$TL_upload_file:disableFree	Z
    //   159: aload_2
    //   160: invokestatic 530	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2000	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_file;
    //   163: invokevirtual 538	org/telegram/tgnet/TLRPC$TL_upload_file:freeResources	()V
    //   166: iload_1
    //   167: iconst_1
    //   168: iadd
    //   169: istore_1
    //   170: goto -49 -> 121
    //   173: astore_2
    //   174: aload_2
    //   175: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   178: goto -159 -> 19
    //   181: astore_2
    //   182: aload_2
    //   183: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   186: goto -155 -> 31
    //   189: astore_2
    //   190: aload_2
    //   191: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   194: goto -144 -> 50
    //   197: astore_2
    //   198: aload_2
    //   199: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   202: goto -140 -> 62
    //   205: astore_2
    //   206: aload_2
    //   207: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   210: goto -129 -> 81
    //   213: astore_2
    //   214: aload_2
    //   215: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   218: goto -125 -> 93
    //   221: astore_2
    //   222: aload_2
    //   223: invokestatic 230	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   226: goto -114 -> 112
    //   229: aload_2
    //   230: invokestatic 542	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2100	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_webFile;
    //   233: ifnull +21 -> 254
    //   236: aload_2
    //   237: invokestatic 542	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2100	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_webFile;
    //   240: iconst_0
    //   241: putfield 545	org/telegram/tgnet/TLRPC$TL_upload_webFile:disableFree	Z
    //   244: aload_2
    //   245: invokestatic 542	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2100	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_webFile;
    //   248: invokevirtual 546	org/telegram/tgnet/TLRPC$TL_upload_webFile:freeResources	()V
    //   251: goto -85 -> 166
    //   254: aload_2
    //   255: invokestatic 550	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2200	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_cdnFile;
    //   258: ifnull -92 -> 166
    //   261: aload_2
    //   262: invokestatic 550	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2200	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_cdnFile;
    //   265: iconst_0
    //   266: putfield 553	org/telegram/tgnet/TLRPC$TL_upload_cdnFile:disableFree	Z
    //   269: aload_2
    //   270: invokestatic 550	org/telegram/messenger/FileLoadOperation$RequestInfo:access$2200	(Lorg/telegram/messenger/FileLoadOperation$RequestInfo;)Lorg/telegram/tgnet/TLRPC$TL_upload_cdnFile;
    //   273: invokevirtual 554	org/telegram/tgnet/TLRPC$TL_upload_cdnFile:freeResources	()V
    //   276: goto -110 -> 166
    //   279: aload_0
    //   280: getfield 357	org/telegram/messenger/FileLoadOperation:delayedRequestInfos	Ljava/util/ArrayList;
    //   283: invokevirtual 557	java/util/ArrayList:clear	()V
    //   286: return
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	287	0	this	FileLoadOperation
    //   120	50	1	i	int
    //   4	156	2	localObject	Object
    //   173	2	2	localException1	Exception
    //   181	2	2	localException2	Exception
    //   189	2	2	localException3	Exception
    //   197	2	2	localException4	Exception
    //   205	2	2	localException5	Exception
    //   213	2	2	localException6	Exception
    //   221	49	2	localException7	Exception
    // Exception table:
    //   from	to	target	type
    //   9	19	173	java/lang/Exception
    //   0	5	181	java/lang/Exception
    //   19	31	181	java/lang/Exception
    //   174	178	181	java/lang/Exception
    //   40	50	189	java/lang/Exception
    //   31	36	197	java/lang/Exception
    //   50	62	197	java/lang/Exception
    //   190	194	197	java/lang/Exception
    //   71	81	205	java/lang/Exception
    //   62	67	213	java/lang/Exception
    //   81	93	213	java/lang/Exception
    //   206	210	213	java/lang/Exception
    //   93	112	221	java/lang/Exception
  }
  
  private void clearOperaion(RequestInfo paramRequestInfo)
  {
    int i = Integer.MAX_VALUE;
    int j = 0;
    if (j < this.requestInfos.size())
    {
      RequestInfo localRequestInfo = (RequestInfo)this.requestInfos.get(j);
      i = Math.min(localRequestInfo.offset, i);
      removePart(this.notRequestedBytesRanges, localRequestInfo.offset, localRequestInfo.offset + this.currentDownloadChunkSize);
      if (paramRequestInfo == localRequestInfo) {}
      for (;;)
      {
        j += 1;
        break;
        if (localRequestInfo.requestToken != 0) {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(localRequestInfo.requestToken, true);
        }
      }
    }
    this.requestInfos.clear();
    int k = 0;
    j = i;
    i = k;
    if (i < this.delayedRequestInfos.size())
    {
      paramRequestInfo = (RequestInfo)this.delayedRequestInfos.get(i);
      removePart(this.notRequestedBytesRanges, paramRequestInfo.offset, paramRequestInfo.offset + this.currentDownloadChunkSize);
      if (paramRequestInfo.response != null)
      {
        paramRequestInfo.response.disableFree = false;
        paramRequestInfo.response.freeResources();
      }
      for (;;)
      {
        j = Math.min(paramRequestInfo.offset, j);
        i += 1;
        break;
        if (paramRequestInfo.responseWeb != null)
        {
          paramRequestInfo.responseWeb.disableFree = false;
          paramRequestInfo.responseWeb.freeResources();
        }
        else if (paramRequestInfo.responseCdn != null)
        {
          paramRequestInfo.responseCdn.disableFree = false;
          paramRequestInfo.responseCdn.freeResources();
        }
      }
    }
    this.delayedRequestInfos.clear();
    this.requestsCount = 0;
    if (this.notLoadedBytesRanges == null)
    {
      this.downloadedBytes = j;
      this.requestedBytesCount = j;
    }
  }
  
  private void copytNotLoadedRanges()
  {
    if (this.notLoadedBytesRanges == null) {
      return;
    }
    this.notLoadedBytesRangesCopy = new ArrayList(this.notLoadedBytesRanges);
  }
  
  private void delayRequestInfo(RequestInfo paramRequestInfo)
  {
    this.delayedRequestInfos.add(paramRequestInfo);
    if (paramRequestInfo.response != null) {
      paramRequestInfo.response.disableFree = true;
    }
    do
    {
      return;
      if (paramRequestInfo.responseWeb != null)
      {
        paramRequestInfo.responseWeb.disableFree = true;
        return;
      }
    } while (paramRequestInfo.responseCdn == null);
    paramRequestInfo.responseCdn.disableFree = true;
  }
  
  private int getDownloadedLengthFromOffsetInternal(ArrayList<Range> paramArrayList, int paramInt1, int paramInt2)
  {
    if ((paramArrayList == null) || (this.state == 3) || (paramArrayList.isEmpty()))
    {
      if (this.downloadedBytes == 0) {
        return paramInt2;
      }
      return Math.min(paramInt2, Math.max(this.downloadedBytes - paramInt1, 0));
    }
    int m = paramArrayList.size();
    Object localObject1 = null;
    int j = paramInt2;
    int i = 0;
    while (i < m)
    {
      Range localRange = (Range)paramArrayList.get(i);
      Object localObject2 = localObject1;
      if (paramInt1 <= localRange.start) {
        if (localObject1 != null)
        {
          localObject2 = localObject1;
          if (localRange.start >= ((Range)localObject1).start) {}
        }
        else
        {
          localObject2 = localRange;
        }
      }
      int k = j;
      if (localRange.start <= paramInt1)
      {
        k = j;
        if (localRange.end > paramInt1) {
          k = 0;
        }
      }
      i += 1;
      j = k;
      localObject1 = localObject2;
    }
    if (j == 0) {
      return 0;
    }
    if (localObject1 != null) {
      return Math.min(paramInt2, ((Range)localObject1).start - paramInt1);
    }
    return Math.min(paramInt2, Math.max(this.totalBytesCount - paramInt1, 0));
  }
  
  private void onFail(boolean paramBoolean, final int paramInt)
  {
    cleanup();
    this.state = 2;
    if (paramBoolean)
    {
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, paramInt);
        }
      });
      return;
    }
    this.delegate.didFailedLoadingFile(this, paramInt);
  }
  
  private void onFinishLoadingFile(final boolean paramBoolean)
    throws Exception
  {
    if (this.state != 1) {}
    do
    {
      do
      {
        return;
        this.state = 3;
        cleanup();
        if (this.cacheIvTemp != null)
        {
          this.cacheIvTemp.delete();
          this.cacheIvTemp = null;
        }
        if (this.cacheFileParts != null)
        {
          this.cacheFileParts.delete();
          this.cacheFileParts = null;
        }
        if ((this.cacheFileTemp != null) && (!this.cacheFileTemp.renameTo(this.cacheFileFinal)))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.e("unable to rename temp = " + this.cacheFileTemp + " to final = " + this.cacheFileFinal + " retry = " + this.renameRetryCount);
          }
          this.renameRetryCount += 1;
          if (this.renameRetryCount < 3)
          {
            this.state = 1;
            Utilities.stageQueue.postRunnable(new Runnable()
            {
              public void run()
              {
                try
                {
                  FileLoadOperation.this.onFinishLoadingFile(paramBoolean);
                  return;
                }
                catch (Exception localException)
                {
                  FileLoadOperation.this.onFail(false, 0);
                }
              }
            }, 200L);
            return;
          }
          this.cacheFileFinal = this.cacheFileTemp;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("finished downloading file to " + this.cacheFileFinal);
        }
        this.delegate.didFinishLoadingFile(this, this.cacheFileFinal);
      } while (!paramBoolean);
      if (this.currentType == 50331648)
      {
        StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(ConnectionsManager.getCurrentNetworkType(), 3, 1);
        return;
      }
      if (this.currentType == 33554432)
      {
        StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(ConnectionsManager.getCurrentNetworkType(), 2, 1);
        return;
      }
      if (this.currentType == 16777216)
      {
        StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(ConnectionsManager.getCurrentNetworkType(), 4, 1);
        return;
      }
    } while (this.currentType != 67108864);
    StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(ConnectionsManager.getCurrentNetworkType(), 5, 1);
  }
  
  private boolean processRequestResult(RequestInfo paramRequestInfo, TLRPC.TL_error paramTL_error)
  {
    if (this.state != 1) {
      return false;
    }
    this.requestInfos.remove(paramRequestInfo);
    int j;
    int i;
    TLRPC.TL_fileHash localTL_fileHash;
    if (paramTL_error == null)
    {
      label161:
      label305:
      int n;
      try
      {
        if ((this.notLoadedBytesRanges == null) && (this.downloadedBytes != paramRequestInfo.offset))
        {
          delayRequestInfo(paramRequestInfo);
          return false;
        }
        if (paramRequestInfo.response != null) {
          paramTL_error = paramRequestInfo.response.bytes;
        }
        while ((paramTL_error == null) || (paramTL_error.limit() == 0))
        {
          onFinishLoadingFile(true);
          return false;
          if (paramRequestInfo.responseWeb != null)
          {
            paramTL_error = paramRequestInfo.responseWeb.bytes;
          }
          else
          {
            if (paramRequestInfo.responseCdn == null) {
              break label1632;
            }
            paramTL_error = paramRequestInfo.responseCdn.bytes;
          }
        }
        j = paramTL_error.limit();
        if (this.isCdn)
        {
          i = paramRequestInfo.offset / 131072 * 131072;
          if (this.cdnHashes == null) {
            break label1637;
          }
          localTL_fileHash = (TLRPC.TL_fileHash)this.cdnHashes.get(i);
          if (localTL_fileHash == null)
          {
            delayRequestInfo(paramRequestInfo);
            requestFileOffsets(i);
            return true;
          }
        }
        if (paramRequestInfo.responseCdn != null)
        {
          i = paramRequestInfo.offset / 16;
          this.cdnIv[15] = ((byte)(i & 0xFF));
          this.cdnIv[14] = ((byte)(i >> 8 & 0xFF));
          this.cdnIv[13] = ((byte)(i >> 16 & 0xFF));
          this.cdnIv[12] = ((byte)(i >> 24 & 0xFF));
          Utilities.aesCtrDecryption(paramTL_error.buffer, this.cdnKey, this.cdnIv, 0, paramTL_error.limit());
        }
        this.downloadedBytes += j;
        if (this.totalBytesCount > 0)
        {
          if (this.downloadedBytes < this.totalBytesCount) {
            break label1643;
          }
          i = 1;
          if (this.key != null)
          {
            Utilities.aesIgeEncryption(paramTL_error.buffer, this.key, this.iv, false, true, 0, paramTL_error.limit());
            if ((i != 0) && (this.bytesCountPadding != 0)) {
              paramTL_error.limit(paramTL_error.limit() - this.bytesCountPadding);
            }
          }
          if (this.encryptFile)
          {
            k = paramRequestInfo.offset / 16;
            this.encryptIv[15] = ((byte)(k & 0xFF));
            this.encryptIv[14] = ((byte)(k >> 8 & 0xFF));
            this.encryptIv[13] = ((byte)(k >> 16 & 0xFF));
            this.encryptIv[12] = ((byte)(k >> 24 & 0xFF));
            Utilities.aesCtrDecryption(paramTL_error.buffer, this.encryptKey, this.encryptIv, 0, paramTL_error.limit());
          }
          if (this.notLoadedBytesRanges != null) {
            this.fileOutputStream.seek(paramRequestInfo.offset);
          }
          this.fileOutputStream.getChannel().write(paramTL_error.buffer);
          addPart(this.notLoadedBytesRanges, paramRequestInfo.offset, paramRequestInfo.offset + j, true);
          if (!this.isCdn) {
            break label1040;
          }
          n = paramRequestInfo.offset / 131072;
          int i1 = this.notCheckedCdnRanges.size();
          int m = 1;
          j = 0;
          label544:
          int k = m;
          if (j < i1)
          {
            paramRequestInfo = (Range)this.notCheckedCdnRanges.get(j);
            if ((paramRequestInfo.start > n) || (n > paramRequestInfo.end)) {
              break label1658;
            }
            k = 0;
          }
          if (k != 0) {
            break label1040;
          }
          j = n * 131072;
          k = getDownloadedLengthFromOffsetInternal(this.notLoadedBytesRanges, j, 131072);
          if ((k == 0) || ((k != 131072) && ((this.totalBytesCount <= 0) || (k != this.totalBytesCount - j)) && ((this.totalBytesCount > 0) || (i == 0)))) {
            break label1040;
          }
          paramRequestInfo = (TLRPC.TL_fileHash)this.cdnHashes.get(j);
          if (this.fileReadStream == null)
          {
            this.cdnCheckBytes = new byte[131072];
            this.fileReadStream = new RandomAccessFile(this.cacheFileTemp, "r");
          }
          this.fileReadStream.seek(j);
          this.fileReadStream.readFully(this.cdnCheckBytes, 0, k);
          if (Arrays.equals(Utilities.computeSHA256(this.cdnCheckBytes, 0, k), paramRequestInfo.hash)) {
            break label1016;
          }
          if (BuildVars.LOGS_ENABLED)
          {
            if (this.location == null) {
              break label935;
            }
            FileLog.e("invalid cdn hash " + this.location + " id = " + this.location.id + " local_id = " + this.location.local_id + " access_hash = " + this.location.access_hash + " volume_id = " + this.location.volume_id + " secret = " + this.location.secret);
          }
        }
        for (;;)
        {
          onFail(false, 0);
          this.cacheFileTemp.delete();
          return false;
          if (j != this.currentDownloadChunkSize) {
            break;
          }
          if ((this.totalBytesCount != this.downloadedBytes) && (this.downloadedBytes % this.currentDownloadChunkSize == 0)) {
            break label1653;
          }
          if (this.totalBytesCount <= 0) {
            break;
          }
          if (this.totalBytesCount > this.downloadedBytes) {
            break label1653;
          }
          break;
          label935:
          if (this.webLocation != null) {
            FileLog.e("invalid cdn hash  " + this.webLocation + " id = " + this.webLocation.url + " access_hash = " + this.webLocation.access_hash);
          }
        }
        return false;
      }
      catch (Exception paramRequestInfo)
      {
        onFail(false, 0);
        FileLog.e(paramRequestInfo);
      }
      label1014:
      label1016:
      this.cdnHashes.remove(j);
      addPart(this.notCheckedCdnRanges, n, n + 1, false);
      label1040:
      if (this.fiv != null)
      {
        this.fiv.seek(0L);
        this.fiv.write(this.iv);
      }
      if ((this.totalBytesCount <= 0) || (this.state != 1)) {
        break label1667;
      }
      copytNotLoadedRanges();
      this.delegate.didChangedLoadProgress(this, Math.min(1.0F, this.downloadedBytes / this.totalBytesCount));
    }
    for (;;)
    {
      if (j < this.delayedRequestInfos.size())
      {
        paramRequestInfo = (RequestInfo)this.delayedRequestInfos.get(j);
        if ((this.notLoadedBytesRanges == null) && (this.downloadedBytes != paramRequestInfo.offset)) {
          break label1673;
        }
        this.delayedRequestInfos.remove(j);
        if (!processRequestResult(paramRequestInfo, null))
        {
          if (paramRequestInfo.response == null) {
            break label1209;
          }
          paramRequestInfo.response.disableFree = false;
          paramRequestInfo.response.freeResources();
        }
      }
      for (;;)
      {
        if (i == 0) {
          break label1259;
        }
        onFinishLoadingFile(true);
        break;
        label1209:
        if (paramRequestInfo.responseWeb != null)
        {
          paramRequestInfo.responseWeb.disableFree = false;
          paramRequestInfo.responseWeb.freeResources();
        }
        else if (paramRequestInfo.responseCdn != null)
        {
          paramRequestInfo.responseCdn.disableFree = false;
          paramRequestInfo.responseCdn.freeResources();
        }
      }
      label1259:
      startDownloadRequest();
      break label1014;
      if (paramTL_error.text.contains("FILE_MIGRATE_"))
      {
        paramRequestInfo = new Scanner(paramTL_error.text.replace("FILE_MIGRATE_", ""));
        paramRequestInfo.useDelimiter("");
        try
        {
          i = paramRequestInfo.nextInt();
          paramRequestInfo = Integer.valueOf(i);
        }
        catch (Exception paramRequestInfo)
        {
          for (;;)
          {
            paramRequestInfo = null;
          }
          this.datacenterId = paramRequestInfo.intValue();
          this.downloadedBytes = 0;
          this.requestedBytesCount = 0;
          startDownloadRequest();
        }
        if (paramRequestInfo == null)
        {
          onFail(false, 0);
          break label1014;
        }
        break label1014;
      }
      if (paramTL_error.text.contains("OFFSET_INVALID"))
      {
        if (this.downloadedBytes % this.currentDownloadChunkSize == 0)
        {
          try
          {
            onFinishLoadingFile(true);
          }
          catch (Exception paramRequestInfo)
          {
            FileLog.e(paramRequestInfo);
            onFail(false, 0);
          }
          break label1014;
        }
        onFail(false, 0);
        break label1014;
      }
      if (paramTL_error.text.contains("RETRY_LIMIT"))
      {
        onFail(false, 2);
        break label1014;
      }
      if (BuildVars.LOGS_ENABLED)
      {
        if (this.location == null) {
          break label1565;
        }
        FileLog.e("" + this.location + " id = " + this.location.id + " local_id = " + this.location.local_id + " access_hash = " + this.location.access_hash + " volume_id = " + this.location.volume_id + " secret = " + this.location.secret);
      }
      for (;;)
      {
        onFail(false, 0);
        break;
        label1565:
        if (this.webLocation != null) {
          FileLog.e("" + this.webLocation + " id = " + this.webLocation.url + " access_hash = " + this.webLocation.access_hash);
        }
      }
      label1632:
      paramTL_error = null;
      break;
      label1637:
      localTL_fileHash = null;
      break label161;
      label1643:
      i = 0;
      break label305;
      label1653:
      for (i = 1;; i = 0) {
        break;
      }
      label1658:
      j += 1;
      break label544;
      label1667:
      j = 0;
      continue;
      label1673:
      j += 1;
    }
  }
  
  private void removePart(ArrayList<Range> paramArrayList, int paramInt1, int paramInt2)
  {
    if ((paramArrayList == null) || (paramInt2 < paramInt1)) {
      return;
    }
    int m = paramArrayList.size();
    int k = 0;
    int j = 0;
    for (;;)
    {
      int i = k;
      Range localRange;
      if (j < m)
      {
        localRange = (Range)paramArrayList.get(j);
        if (paramInt1 != localRange.end) {
          break label84;
        }
        Range.access$002(localRange, paramInt2);
      }
      for (i = 1; i == 0; i = 1)
      {
        paramArrayList.add(new Range(paramInt1, paramInt2, null));
        return;
        label84:
        if (paramInt2 != localRange.start) {
          break label106;
        }
        Range.access$102(localRange, paramInt1);
      }
      break;
      label106:
      j += 1;
    }
  }
  
  private void requestFileOffsets(int paramInt)
  {
    if (this.requestingCdnOffsets) {
      return;
    }
    this.requestingCdnOffsets = true;
    TLRPC.TL_upload_getCdnFileHashes localTL_upload_getCdnFileHashes = new TLRPC.TL_upload_getCdnFileHashes();
    localTL_upload_getCdnFileHashes.file_token = this.cdnToken;
    localTL_upload_getCdnFileHashes.offset = paramInt;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_upload_getCdnFileHashes, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          FileLoadOperation.this.onFail(false, 0);
        }
        label263:
        for (;;)
        {
          return;
          FileLoadOperation.access$2302(FileLoadOperation.this, false);
          paramAnonymousTLObject = (TLRPC.Vector)paramAnonymousTLObject;
          if (!paramAnonymousTLObject.objects.isEmpty())
          {
            if (FileLoadOperation.this.cdnHashes == null) {
              FileLoadOperation.access$2402(FileLoadOperation.this, new SparseArray());
            }
            i = 0;
            while (i < paramAnonymousTLObject.objects.size())
            {
              paramAnonymousTL_error = (TLRPC.TL_fileHash)paramAnonymousTLObject.objects.get(i);
              FileLoadOperation.this.cdnHashes.put(paramAnonymousTL_error.offset, paramAnonymousTL_error);
              i += 1;
            }
          }
          int i = 0;
          for (;;)
          {
            if (i >= FileLoadOperation.this.delayedRequestInfos.size()) {
              break label263;
            }
            paramAnonymousTLObject = (FileLoadOperation.RequestInfo)FileLoadOperation.this.delayedRequestInfos.get(i);
            if ((FileLoadOperation.this.notLoadedBytesRanges != null) || (FileLoadOperation.this.downloadedBytes == FileLoadOperation.RequestInfo.access$2600(paramAnonymousTLObject)))
            {
              FileLoadOperation.this.delayedRequestInfos.remove(i);
              if (FileLoadOperation.this.processRequestResult(paramAnonymousTLObject, null)) {
                break;
              }
              if (FileLoadOperation.RequestInfo.access$2000(paramAnonymousTLObject) != null)
              {
                FileLoadOperation.RequestInfo.access$2000(paramAnonymousTLObject).disableFree = false;
                FileLoadOperation.RequestInfo.access$2000(paramAnonymousTLObject).freeResources();
                return;
              }
              if (FileLoadOperation.RequestInfo.access$2100(paramAnonymousTLObject) != null)
              {
                FileLoadOperation.RequestInfo.access$2100(paramAnonymousTLObject).disableFree = false;
                FileLoadOperation.RequestInfo.access$2100(paramAnonymousTLObject).freeResources();
                return;
              }
              if (FileLoadOperation.RequestInfo.access$2200(paramAnonymousTLObject) == null) {
                break;
              }
              FileLoadOperation.RequestInfo.access$2200(paramAnonymousTLObject).disableFree = false;
              FileLoadOperation.RequestInfo.access$2200(paramAnonymousTLObject).freeResources();
              return;
            }
            i += 1;
          }
        }
      }
    }, null, null, 0, this.datacenterId, 1, true);
  }
  
  private void startDownloadRequest()
  {
    if ((this.paused) || (this.state != 1) || (this.requestInfos.size() + this.delayedRequestInfos.size() >= this.currentMaxDownloadRequests)) {}
    int n;
    int i1;
    label69:
    int j;
    int i2;
    label103:
    int m;
    int k;
    final Object localObject;
    label181:
    do
    {
      return;
      n = 1;
      if (this.totalBytesCount > 0) {
        n = Math.max(0, this.currentMaxDownloadRequests - this.requestInfos.size());
      }
      i1 = 0;
      if (i1 >= n) {
        break;
      }
      if (this.notRequestedBytesRanges == null) {
        break label509;
      }
      int i3 = this.notRequestedBytesRanges.size();
      i = Integer.MAX_VALUE;
      j = Integer.MAX_VALUE;
      i2 = 0;
      m = i;
      k = j;
      if (i2 < i3)
      {
        localObject = (Range)this.notRequestedBytesRanges.get(i2);
        k = j;
        if (this.streamStartOffset == 0) {
          break label474;
        }
        if ((((Range)localObject).start > this.streamStartOffset) || (((Range)localObject).end <= this.streamStartOffset)) {
          break label443;
        }
        k = this.streamStartOffset;
        m = Integer.MAX_VALUE;
      }
      if (k == Integer.MAX_VALUE) {
        break label495;
      }
      i = k;
      if (this.notRequestedBytesRanges != null) {
        addPart(this.notRequestedBytesRanges, i, this.currentDownloadChunkSize + i, false);
      }
    } while ((this.totalBytesCount > 0) && (i >= this.totalBytesCount));
    boolean bool;
    label258:
    label269:
    label279:
    label327:
    final RequestInfo localRequestInfo;
    ConnectionsManager localConnectionsManager;
    RequestDelegate local12;
    if ((this.totalBytesCount <= 0) || (i1 == n - 1) || ((this.totalBytesCount > 0) && (this.currentDownloadChunkSize + i >= this.totalBytesCount)))
    {
      bool = true;
      if (this.requestsCount % 2 != 0) {
        break label523;
      }
      j = 2;
      if (!this.isForceRequest) {
        break label530;
      }
      k = 32;
      k |= 0x2;
      if (!this.isCdn) {
        break label535;
      }
      localObject = new TLRPC.TL_upload_getCdnFile();
      ((TLRPC.TL_upload_getCdnFile)localObject).file_token = this.cdnToken;
      ((TLRPC.TL_upload_getCdnFile)localObject).offset = i;
      ((TLRPC.TL_upload_getCdnFile)localObject).limit = this.currentDownloadChunkSize;
      k |= 0x1;
      this.requestedBytesCount += this.currentDownloadChunkSize;
      localRequestInfo = new RequestInfo(null);
      this.requestInfos.add(localRequestInfo);
      RequestInfo.access$2602(localRequestInfo, i);
      localConnectionsManager = ConnectionsManager.getInstance(this.currentAccount);
      local12 = new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (!FileLoadOperation.this.requestInfos.contains(localRequestInfo)) {}
          do
          {
            return;
            if ((paramAnonymousTL_error != null) && ((localObject instanceof TLRPC.TL_upload_getCdnFile)) && (paramAnonymousTL_error.text.equals("FILE_TOKEN_INVALID")))
            {
              FileLoadOperation.access$3002(FileLoadOperation.this, false);
              FileLoadOperation.this.clearOperaion(localRequestInfo);
              FileLoadOperation.this.startDownloadRequest();
              return;
            }
            if ((paramAnonymousTLObject instanceof TLRPC.TL_upload_fileCdnRedirect))
            {
              paramAnonymousTLObject = (TLRPC.TL_upload_fileCdnRedirect)paramAnonymousTLObject;
              if (!paramAnonymousTLObject.file_hashes.isEmpty())
              {
                if (FileLoadOperation.this.cdnHashes == null) {
                  FileLoadOperation.access$2402(FileLoadOperation.this, new SparseArray());
                }
                int i = 0;
                while (i < paramAnonymousTLObject.file_hashes.size())
                {
                  paramAnonymousTL_error = (TLRPC.TL_fileHash)paramAnonymousTLObject.file_hashes.get(i);
                  FileLoadOperation.this.cdnHashes.put(paramAnonymousTL_error.offset, paramAnonymousTL_error);
                  i += 1;
                }
              }
              if ((paramAnonymousTLObject.encryption_iv == null) || (paramAnonymousTLObject.encryption_key == null) || (paramAnonymousTLObject.encryption_iv.length != 16) || (paramAnonymousTLObject.encryption_key.length != 32))
              {
                paramAnonymousTLObject = new TLRPC.TL_error();
                paramAnonymousTLObject.text = "bad redirect response";
                paramAnonymousTLObject.code = 400;
                FileLoadOperation.this.processRequestResult(localRequestInfo, paramAnonymousTLObject);
                return;
              }
              FileLoadOperation.access$3002(FileLoadOperation.this, true);
              if (FileLoadOperation.this.notCheckedCdnRanges == null)
              {
                FileLoadOperation.access$3202(FileLoadOperation.this, new ArrayList());
                FileLoadOperation.this.notCheckedCdnRanges.add(new FileLoadOperation.Range(0, 12288, null));
              }
              FileLoadOperation.access$3302(FileLoadOperation.this, paramAnonymousTLObject.dc_id);
              FileLoadOperation.access$3402(FileLoadOperation.this, paramAnonymousTLObject.encryption_iv);
              FileLoadOperation.access$3502(FileLoadOperation.this, paramAnonymousTLObject.encryption_key);
              FileLoadOperation.access$3602(FileLoadOperation.this, paramAnonymousTLObject.file_token);
              FileLoadOperation.this.clearOperaion(localRequestInfo);
              FileLoadOperation.this.startDownloadRequest();
              return;
            }
            if (!(paramAnonymousTLObject instanceof TLRPC.TL_upload_cdnFileReuploadNeeded)) {
              break;
            }
          } while (FileLoadOperation.this.reuploadingCdn);
          FileLoadOperation.this.clearOperaion(localRequestInfo);
          FileLoadOperation.access$3702(FileLoadOperation.this, true);
          paramAnonymousTLObject = (TLRPC.TL_upload_cdnFileReuploadNeeded)paramAnonymousTLObject;
          paramAnonymousTL_error = new TLRPC.TL_upload_reuploadCdnFile();
          paramAnonymousTL_error.file_token = FileLoadOperation.this.cdnToken;
          paramAnonymousTL_error.request_token = paramAnonymousTLObject.request_token;
          ConnectionsManager.getInstance(FileLoadOperation.this.currentAccount).sendRequest(paramAnonymousTL_error, new RequestDelegate()
          {
            public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              FileLoadOperation.access$3702(FileLoadOperation.this, false);
              if (paramAnonymous2TL_error == null)
              {
                paramAnonymous2TLObject = (TLRPC.Vector)paramAnonymous2TLObject;
                if (!paramAnonymous2TLObject.objects.isEmpty())
                {
                  if (FileLoadOperation.this.cdnHashes == null) {
                    FileLoadOperation.access$2402(FileLoadOperation.this, new SparseArray());
                  }
                  int i = 0;
                  while (i < paramAnonymous2TLObject.objects.size())
                  {
                    paramAnonymous2TL_error = (TLRPC.TL_fileHash)paramAnonymous2TLObject.objects.get(i);
                    FileLoadOperation.this.cdnHashes.put(paramAnonymous2TL_error.offset, paramAnonymous2TL_error);
                    i += 1;
                  }
                }
                FileLoadOperation.this.startDownloadRequest();
                return;
              }
              if ((paramAnonymous2TL_error.text.equals("FILE_TOKEN_INVALID")) || (paramAnonymous2TL_error.text.equals("REQUEST_TOKEN_INVALID")))
              {
                FileLoadOperation.access$3002(FileLoadOperation.this, false);
                FileLoadOperation.this.clearOperaion(FileLoadOperation.12.this.val$requestInfo);
                FileLoadOperation.this.startDownloadRequest();
                return;
              }
              FileLoadOperation.this.onFail(false, 0);
            }
          }, null, null, 0, FileLoadOperation.this.datacenterId, 1, true);
          return;
          if ((paramAnonymousTLObject instanceof TLRPC.TL_upload_file))
          {
            FileLoadOperation.RequestInfo.access$2002(localRequestInfo, (TLRPC.TL_upload_file)paramAnonymousTLObject);
            if (paramAnonymousTLObject != null)
            {
              if (FileLoadOperation.this.currentType != 50331648) {
                break label616;
              }
              StatsController.getInstance(FileLoadOperation.this.currentAccount).incrementReceivedBytesCount(paramAnonymousTLObject.networkType, 3, paramAnonymousTLObject.getObjectSize() + 4);
            }
          }
          for (;;)
          {
            FileLoadOperation.this.processRequestResult(localRequestInfo, paramAnonymousTL_error);
            return;
            if ((paramAnonymousTLObject instanceof TLRPC.TL_upload_webFile))
            {
              FileLoadOperation.RequestInfo.access$2102(localRequestInfo, (TLRPC.TL_upload_webFile)paramAnonymousTLObject);
              if ((FileLoadOperation.this.totalBytesCount != 0) || (FileLoadOperation.RequestInfo.access$2100(localRequestInfo).size == 0)) {
                break;
              }
              FileLoadOperation.access$1302(FileLoadOperation.this, FileLoadOperation.RequestInfo.access$2100(localRequestInfo).size);
              break;
            }
            FileLoadOperation.RequestInfo.access$2202(localRequestInfo, (TLRPC.TL_upload_cdnFile)paramAnonymousTLObject);
            break;
            label616:
            if (FileLoadOperation.this.currentType == 33554432) {
              StatsController.getInstance(FileLoadOperation.this.currentAccount).incrementReceivedBytesCount(paramAnonymousTLObject.networkType, 2, paramAnonymousTLObject.getObjectSize() + 4);
            } else if (FileLoadOperation.this.currentType == 16777216) {
              StatsController.getInstance(FileLoadOperation.this.currentAccount).incrementReceivedBytesCount(paramAnonymousTLObject.networkType, 4, paramAnonymousTLObject.getObjectSize() + 4);
            } else if (FileLoadOperation.this.currentType == 67108864) {
              StatsController.getInstance(FileLoadOperation.this.currentAccount).incrementReceivedBytesCount(paramAnonymousTLObject.networkType, 5, paramAnonymousTLObject.getObjectSize() + 4);
            }
          }
        }
      };
      if (!this.isCdn) {
        break label614;
      }
    }
    label443:
    label474:
    label495:
    label509:
    label523:
    label530:
    label535:
    label614:
    for (int i = this.cdnDatacenterId;; i = this.datacenterId)
    {
      RequestInfo.access$1802(localRequestInfo, localConnectionsManager.sendRequest((TLObject)localObject, local12, null, null, k, i, j, bool));
      this.requestsCount += 1;
      i1 += 1;
      break label69;
      break;
      k = j;
      if (this.streamStartOffset < ((Range)localObject).start)
      {
        k = j;
        if (((Range)localObject).start < j) {
          k = ((Range)localObject).start;
        }
      }
      i = Math.min(i, ((Range)localObject).start);
      i2 += 1;
      j = k;
      break label103;
      if (m == Integer.MAX_VALUE) {
        break;
      }
      i = m;
      break label181;
      i = this.requestedBytesCount;
      break label181;
      bool = false;
      break label258;
      j = 65538;
      break label269;
      k = 0;
      break label279;
      if (this.webLocation != null)
      {
        localObject = new TLRPC.TL_upload_getWebFile();
        ((TLRPC.TL_upload_getWebFile)localObject).location = this.webLocation;
        ((TLRPC.TL_upload_getWebFile)localObject).offset = i;
        ((TLRPC.TL_upload_getWebFile)localObject).limit = this.currentDownloadChunkSize;
        break label327;
      }
      localObject = new TLRPC.TL_upload_getFile();
      ((TLRPC.TL_upload_getFile)localObject).location = this.location;
      ((TLRPC.TL_upload_getFile)localObject).offset = i;
      ((TLRPC.TL_upload_getFile)localObject).limit = this.currentDownloadChunkSize;
      break label327;
    }
  }
  
  public void cancel()
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if ((FileLoadOperation.this.state == 3) || (FileLoadOperation.this.state == 2)) {
          return;
        }
        if (FileLoadOperation.this.requestInfos != null)
        {
          int i = 0;
          while (i < FileLoadOperation.this.requestInfos.size())
          {
            FileLoadOperation.RequestInfo localRequestInfo = (FileLoadOperation.RequestInfo)FileLoadOperation.this.requestInfos.get(i);
            if (FileLoadOperation.RequestInfo.access$1800(localRequestInfo) != 0) {
              ConnectionsManager.getInstance(FileLoadOperation.this.currentAccount).cancelRequest(FileLoadOperation.RequestInfo.access$1800(localRequestInfo), true);
            }
            i += 1;
          }
        }
        FileLoadOperation.this.onFail(false, 1);
      }
    });
  }
  
  protected File getCurrentFile()
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final File[] arrayOfFile = new File[1];
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (FileLoadOperation.this.state == 3) {
          arrayOfFile[0] = FileLoadOperation.this.cacheFileFinal;
        }
        for (;;)
        {
          localCountDownLatch.countDown();
          return;
          arrayOfFile[0] = FileLoadOperation.this.cacheFileTemp;
        }
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfFile[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public int getCurrentType()
  {
    return this.currentType;
  }
  
  public int getDatacenterId()
  {
    return this.initialDatacenterId;
  }
  
  protected float getDownloadedLengthFromOffset(float paramFloat)
  {
    ArrayList localArrayList = this.notLoadedBytesRangesCopy;
    if ((this.totalBytesCount == 0) || (localArrayList == null)) {
      return 0.0F;
    }
    return getDownloadedLengthFromOffsetInternal(localArrayList, (int)(this.totalBytesCount * paramFloat), this.totalBytesCount) / this.totalBytesCount + paramFloat;
  }
  
  protected int getDownloadedLengthFromOffset(final int paramInt1, final int paramInt2)
  {
    final CountDownLatch localCountDownLatch = new CountDownLatch(1);
    final int[] arrayOfInt = new int[1];
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        arrayOfInt[0] = FileLoadOperation.this.getDownloadedLengthFromOffsetInternal(FileLoadOperation.this.notLoadedBytesRanges, paramInt1, paramInt2);
        localCountDownLatch.countDown();
      }
    });
    try
    {
      localCountDownLatch.await();
      return arrayOfInt[0];
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public String getFileName()
  {
    if (this.location != null) {
      return this.location.volume_id + "_" + this.location.local_id + "." + this.ext;
    }
    return Utilities.MD5(this.webLocation.url) + "." + this.ext;
  }
  
  public boolean isForceRequest()
  {
    return this.isForceRequest;
  }
  
  public boolean isPaused()
  {
    return this.paused;
  }
  
  public void pause()
  {
    if (this.state != 1) {
      return;
    }
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        FileLoadOperation.access$902(FileLoadOperation.this, true);
      }
    });
  }
  
  protected void removeStreamListener(final FileStreamLoadOperation paramFileStreamLoadOperation)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (FileLoadOperation.this.streamListeners == null) {
          return;
        }
        FileLoadOperation.this.streamListeners.remove(paramFileStreamLoadOperation);
      }
    });
  }
  
  public void setDelegate(FileLoadOperationDelegate paramFileLoadOperationDelegate)
  {
    this.delegate = paramFileLoadOperationDelegate;
  }
  
  public void setEncryptFile(boolean paramBoolean)
  {
    this.encryptFile = paramBoolean;
    if (this.encryptFile) {
      this.allowDisordererFileSave = false;
    }
  }
  
  public void setForceRequest(boolean paramBoolean)
  {
    this.isForceRequest = paramBoolean;
  }
  
  public void setPaths(int paramInt, File paramFile1, File paramFile2)
  {
    this.storePath = paramFile1;
    this.tempPath = paramFile2;
    this.currentAccount = paramInt;
  }
  
  public boolean start()
  {
    return start(null, 0);
  }
  
  public boolean start(final FileStreamLoadOperation paramFileStreamLoadOperation, final int paramInt)
  {
    int i;
    label33:
    final boolean bool1;
    label48:
    boolean bool2;
    if (this.currentDownloadChunkSize == 0)
    {
      if (this.totalBytesCount >= 1048576)
      {
        i = 131072;
        this.currentDownloadChunkSize = i;
        if (this.totalBytesCount < 1048576) {
          break label95;
        }
        this.currentMaxDownloadRequests = 4;
      }
    }
    else
    {
      if (this.state == 0) {
        break label98;
      }
      bool1 = true;
      bool2 = this.paused;
      this.paused = false;
      if (paramFileStreamLoadOperation == null) {
        break label104;
      }
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (FileLoadOperation.this.streamListeners == null) {
            FileLoadOperation.access$802(FileLoadOperation.this, new ArrayList());
          }
          FileLoadOperation.access$1002(FileLoadOperation.this, paramInt / FileLoadOperation.this.currentDownloadChunkSize * FileLoadOperation.this.currentDownloadChunkSize);
          FileLoadOperation.this.streamListeners.add(paramFileStreamLoadOperation);
          if (bool1) {
            FileLoadOperation.this.startDownloadRequest();
          }
        }
      });
    }
    for (;;)
    {
      if (!bool1) {
        break label131;
      }
      return bool2;
      i = 32768;
      break;
      label95:
      break label33;
      label98:
      bool1 = false;
      break label48;
      label104:
      if ((bool2) && (bool1)) {
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            FileLoadOperation.this.startDownloadRequest();
          }
        });
      }
    }
    label131:
    if ((this.location == null) && (this.webLocation == null))
    {
      onFail(true, 0);
      return false;
    }
    this.streamStartOffset = (paramInt / this.currentDownloadChunkSize * this.currentDownloadChunkSize);
    if ((this.allowDisordererFileSave) && (this.totalBytesCount > 0) && (this.totalBytesCount > this.currentDownloadChunkSize))
    {
      this.notLoadedBytesRanges = new ArrayList();
      this.notRequestedBytesRanges = new ArrayList();
    }
    Object localObject5 = null;
    Object localObject4 = null;
    Object localObject1 = null;
    paramFileStreamLoadOperation = null;
    String str2;
    String str1;
    Object localObject3;
    Object localObject2;
    if (this.webLocation != null)
    {
      str2 = Utilities.MD5(this.webLocation.url);
      if (this.encryptFile)
      {
        localObject4 = str2 + ".temp.enc";
        str1 = str2 + "." + this.ext + ".enc";
        localObject1 = str1;
        localObject3 = localObject5;
        localObject2 = localObject4;
        if (this.key != null)
        {
          paramFileStreamLoadOperation = str2 + ".iv.enc";
          localObject2 = localObject4;
          localObject3 = localObject5;
          localObject1 = str1;
        }
      }
    }
    int j;
    for (;;)
    {
      this.requestInfos = new ArrayList(this.currentMaxDownloadRequests);
      this.delayedRequestInfos = new ArrayList(this.currentMaxDownloadRequests - 1);
      this.state = 1;
      this.cacheFileFinal = new File(this.storePath, (String)localObject1);
      bool2 = this.cacheFileFinal.exists();
      bool1 = bool2;
      if (bool2)
      {
        bool1 = bool2;
        if (this.totalBytesCount != 0)
        {
          bool1 = bool2;
          if (this.totalBytesCount != this.cacheFileFinal.length())
          {
            this.cacheFileFinal.delete();
            bool1 = false;
          }
        }
      }
      if (!bool1)
      {
        this.cacheFileTemp = new File(this.tempPath, (String)localObject2);
        paramInt = 0;
        int k = 0;
        j = 0;
        if (this.encryptFile)
        {
          localObject2 = new File(FileLoader.getInternalCacheDir(), (String)localObject1 + ".key");
          i = k;
        }
        for (;;)
        {
          try
          {
            localObject1 = new RandomAccessFile((File)localObject2, "rws");
            i = k;
            l = ((File)localObject2).length();
            i = k;
            this.encryptKey = new byte[32];
            i = k;
            this.encryptIv = new byte[16];
            if ((l > 0L) && (l % 48L == 0L))
            {
              i = k;
              ((RandomAccessFile)localObject1).read(this.encryptKey, 0, 32);
              i = k;
              ((RandomAccessFile)localObject1).read(this.encryptIv, 0, 16);
              paramInt = j;
            }
          }
          catch (Exception localException1)
          {
            FileLog.e(localException1);
            paramInt = i;
            continue;
            if (!this.cacheFileTemp.exists()) {
              continue;
            }
            if (paramInt == 0) {
              continue;
            }
            this.cacheFileTemp.delete();
            if (this.notLoadedBytesRanges == null) {
              continue;
            }
            this.downloadedBytes = this.totalBytesCount;
            j = this.notLoadedBytesRanges.size();
            i = 0;
            if (i >= j) {
              continue;
            }
            localRange = (Range)this.notLoadedBytesRanges.get(i);
            this.downloadedBytes -= localRange.end - localRange.start;
            i += 1;
            continue;
            long l = this.cacheFileTemp.length();
            if ((paramFileStreamLoadOperation == null) || (l % this.currentDownloadChunkSize == 0L)) {
              continue;
            }
            this.downloadedBytes = 0;
            this.requestedBytesCount = 0;
            if ((this.notLoadedBytesRanges == null) || (!this.notLoadedBytesRanges.isEmpty())) {
              continue;
            }
            this.notLoadedBytesRanges.add(new Range(this.downloadedBytes, this.totalBytesCount, null));
            this.notRequestedBytesRanges.add(new Range(this.downloadedBytes, this.totalBytesCount, null));
            continue;
            i = (int)this.cacheFileTemp.length() / this.currentDownloadChunkSize * this.currentDownloadChunkSize;
            this.downloadedBytes = i;
            this.requestedBytesCount = i;
            continue;
            continue;
            if ((this.notLoadedBytesRanges == null) || (!this.notLoadedBytesRanges.isEmpty())) {
              continue;
            }
            this.notLoadedBytesRanges.add(new Range(0, this.totalBytesCount, null));
            this.notRequestedBytesRanges.add(new Range(0, this.totalBytesCount, null));
            continue;
            if (!BuildVars.LOGS_ENABLED) {
              continue;
            }
            FileLog.d("start loading file to temp = " + this.cacheFileTemp + " final = " + this.cacheFileFinal);
            if (paramFileStreamLoadOperation == null) {
              continue;
            }
            this.cacheIvTemp = new File(this.tempPath, paramFileStreamLoadOperation);
            try
            {
              this.fiv = new RandomAccessFile(this.cacheIvTemp, "rws");
              if ((this.downloadedBytes != 0) && (paramInt == 0))
              {
                l = this.cacheIvTemp.length();
                if ((l <= 0L) || (l % 32L != 0L)) {
                  break label2425;
                }
                this.fiv.read(this.iv, 0, 32);
              }
            }
            catch (Exception paramFileStreamLoadOperation)
            {
              try
              {
                this.fileOutputStream = new RandomAccessFile(this.cacheFileTemp, "rws");
                if (this.downloadedBytes == 0) {
                  break label2410;
                }
                this.fileOutputStream.seek(this.downloadedBytes);
                if (this.fileOutputStream != null) {
                  break label2464;
                }
                onFail(true, 0);
                return false;
                this.downloadedBytes = 0;
                this.requestedBytesCount = 0;
                continue;
                paramFileStreamLoadOperation = paramFileStreamLoadOperation;
                FileLog.e(paramFileStreamLoadOperation);
                this.downloadedBytes = 0;
                this.requestedBytesCount = 0;
              }
              catch (Exception paramFileStreamLoadOperation)
              {
                for (;;)
                {
                  FileLog.e(paramFileStreamLoadOperation);
                }
                this.started = true;
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    if ((FileLoadOperation.this.totalBytesCount != 0) && (FileLoadOperation.this.downloadedBytes == FileLoadOperation.this.totalBytesCount)) {
                      try
                      {
                        FileLoadOperation.this.onFinishLoadingFile(false);
                        return;
                      }
                      catch (Exception localException)
                      {
                        FileLoadOperation.this.onFail(true, 0);
                        return;
                      }
                    }
                    FileLoadOperation.this.startDownloadRequest();
                  }
                });
              }
            }
            if ((this.downloadedBytes == 0) || (this.totalBytesCount <= 0)) {
              break label2373;
            }
            copytNotLoadedRanges();
            this.delegate.didChangedLoadProgress(this, Math.min(1.0F, this.downloadedBytes / this.totalBytesCount));
          }
          try
          {
            ((RandomAccessFile)localObject1).getChannel().close();
            i = paramInt;
            ((RandomAccessFile)localObject1).close();
            if (localObject3 != null)
            {
              this.cacheFileParts = new File(this.tempPath, (String)localObject3);
              try
              {
                this.filePartsStream = new RandomAccessFile(this.cacheFileParts, "rws");
                l = this.filePartsStream.length();
                if (l % 8L != 4L) {
                  continue;
                }
                j = this.filePartsStream.readInt();
                if (j > (l - 4L) / 2L) {
                  continue;
                }
                i = 0;
                if (i >= j) {
                  continue;
                }
                k = this.filePartsStream.readInt();
                int m = this.filePartsStream.readInt();
                this.notLoadedBytesRanges.add(new Range(k, m, null));
                this.notRequestedBytesRanges.add(new Range(k, m, null));
                i += 1;
                continue;
                localObject4 = str2 + ".temp";
                str1 = str2 + "." + this.ext;
                localObject1 = str1;
                localObject3 = localObject5;
                localObject2 = localObject4;
                if (this.key == null) {
                  break;
                }
                paramFileStreamLoadOperation = str2 + ".iv";
                localObject1 = str1;
                localObject3 = localObject5;
                localObject2 = localObject4;
              }
              catch (Exception localException2)
              {
                FileLog.e(localException2);
              }
              if ((this.location.volume_id != 0L) && (this.location.local_id != 0))
              {
                if ((this.datacenterId == Integer.MIN_VALUE) || (this.location.volume_id == -2147483648L) || (this.datacenterId == 0))
                {
                  onFail(true, 0);
                  return false;
                }
                if (this.encryptFile)
                {
                  localObject4 = this.location.volume_id + "_" + this.location.local_id + ".temp.enc";
                  str1 = this.location.volume_id + "_" + this.location.local_id + "." + this.ext + ".enc";
                  localObject1 = str1;
                  localObject3 = localObject5;
                  localObject2 = localObject4;
                  if (this.key == null) {
                    break;
                  }
                  paramFileStreamLoadOperation = this.location.volume_id + "_" + this.location.local_id + ".iv.enc";
                  localObject1 = str1;
                  localObject3 = localObject5;
                  localObject2 = localObject4;
                  break;
                }
                str1 = this.location.volume_id + "_" + this.location.local_id + ".temp";
                str2 = this.location.volume_id + "_" + this.location.local_id + "." + this.ext;
                if (this.key != null) {
                  localObject4 = this.location.volume_id + "_" + this.location.local_id + ".iv";
                }
                localObject1 = str2;
                paramFileStreamLoadOperation = (FileStreamLoadOperation)localObject4;
                localObject3 = localObject5;
                localObject2 = str1;
                if (this.notLoadedBytesRanges == null) {
                  break;
                }
                localObject3 = this.location.volume_id + "_" + this.location.local_id + ".pt";
                localObject1 = str2;
                paramFileStreamLoadOperation = (FileStreamLoadOperation)localObject4;
                localObject2 = str1;
                break;
              }
              if ((this.datacenterId == 0) || (this.location.id == 0L))
              {
                onFail(true, 0);
                return false;
              }
              if (this.encryptFile)
              {
                localObject4 = this.datacenterId + "_" + this.location.id + ".temp.enc";
                str1 = this.datacenterId + "_" + this.location.id + this.ext + ".enc";
                localObject1 = str1;
                localObject3 = localObject5;
                localObject2 = localObject4;
                if (this.key == null) {
                  break;
                }
                paramFileStreamLoadOperation = this.datacenterId + "_" + this.location.id + ".iv.enc";
                localObject1 = str1;
                localObject3 = localObject5;
                localObject2 = localObject4;
                break;
              }
              str1 = this.datacenterId + "_" + this.location.id + ".temp";
              str2 = this.datacenterId + "_" + this.location.id + this.ext;
              localObject4 = localObject1;
              if (this.key != null) {
                localObject4 = this.datacenterId + "_" + this.location.id + ".iv";
              }
              localObject1 = str2;
              paramFileStreamLoadOperation = (FileStreamLoadOperation)localObject4;
              localObject3 = localObject5;
              localObject2 = str1;
              if (this.notLoadedBytesRanges == null) {
                break;
              }
              localObject3 = this.datacenterId + "_" + this.location.id + ".pt";
              localObject1 = str2;
              paramFileStreamLoadOperation = (FileStreamLoadOperation)localObject4;
              localObject2 = str1;
              break;
              i = k;
              Utilities.random.nextBytes(this.encryptKey);
              i = k;
              Utilities.random.nextBytes(this.encryptIv);
              i = k;
              ((RandomAccessFile)localObject1).write(this.encryptKey);
              i = k;
              ((RandomAccessFile)localObject1).write(this.encryptIv);
              paramInt = 1;
              continue;
            }
          }
          catch (Exception localException3)
          {
            i = paramInt;
            FileLog.e(localException3);
          }
        }
      }
    }
    for (;;)
    {
      Range localRange;
      label2373:
      label2410:
      label2425:
      label2464:
      return true;
      this.started = true;
      try
      {
        onFinishLoadingFile(false);
      }
      catch (Exception paramFileStreamLoadOperation)
      {
        onFail(true, 0);
      }
    }
  }
  
  public boolean wasStarted()
  {
    return this.started;
  }
  
  public static abstract interface FileLoadOperationDelegate
  {
    public abstract void didChangedLoadProgress(FileLoadOperation paramFileLoadOperation, float paramFloat);
    
    public abstract void didFailedLoadingFile(FileLoadOperation paramFileLoadOperation, int paramInt);
    
    public abstract void didFinishLoadingFile(FileLoadOperation paramFileLoadOperation, File paramFile);
  }
  
  public static class Range
  {
    private int end;
    private int start;
    
    private Range(int paramInt1, int paramInt2)
    {
      this.start = paramInt1;
      this.end = paramInt2;
    }
  }
  
  private static class RequestInfo
  {
    private int offset;
    private int requestToken;
    private TLRPC.TL_upload_file response;
    private TLRPC.TL_upload_cdnFile responseCdn;
    private TLRPC.TL_upload_webFile responseWeb;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/FileLoadOperation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */