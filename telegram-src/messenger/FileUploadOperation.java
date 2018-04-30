package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.SparseArray;
import android.util.SparseIntArray;
import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileBigUploaded;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileUploaded;
import org.telegram.tgnet.TLRPC.TL_inputFile;
import org.telegram.tgnet.TLRPC.TL_inputFileBig;
import org.telegram.tgnet.TLRPC.TL_upload_saveBigFilePart;
import org.telegram.tgnet.TLRPC.TL_upload_saveFilePart;
import org.telegram.tgnet.WriteToSocketDelegate;

public class FileUploadOperation
{
  private static final int initialRequestsCount = 8;
  private static final int maxUploadingKBytes = 2048;
  private static final int minUploadChunkSize = 128;
  private long availableSize;
  private SparseArray<UploadCachedResult> cachedResults = new SparseArray();
  private int currentAccount;
  private long currentFileId;
  private int currentPartNum;
  private int currentType;
  private int currentUploadRequetsCount;
  private int currentUploadingBytes;
  private FileUploadOperationDelegate delegate;
  private int estimatedSize;
  private String fileKey;
  private int fingerprint;
  private ArrayList<byte[]> freeRequestIvs;
  private boolean isBigFile;
  private boolean isEncrypted;
  private boolean isLastPart;
  private byte[] iv;
  private byte[] ivChange;
  private byte[] key;
  private int lastSavedPartNum;
  private int maxRequestsCount;
  private boolean nextPartFirst;
  private SharedPreferences preferences;
  private byte[] readBuffer;
  private long readBytesCount;
  private int requestNum;
  private SparseIntArray requestTokens = new SparseIntArray();
  private int saveInfoTimes;
  private boolean started;
  private int state;
  private RandomAccessFile stream;
  private long totalFileSize;
  private int totalPartsCount;
  private int uploadChunkSize = 65536;
  private boolean uploadFirstPartLater;
  private int uploadStartTime;
  private long uploadedBytesCount;
  private String uploadingFilePath;
  
  public FileUploadOperation(int paramInt1, String paramString, boolean paramBoolean, int paramInt2, int paramInt3)
  {
    this.currentAccount = paramInt1;
    this.uploadingFilePath = paramString;
    this.isEncrypted = paramBoolean;
    this.estimatedSize = paramInt2;
    this.currentType = paramInt3;
    if ((paramInt2 != 0) && (!this.isEncrypted)) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      this.uploadFirstPartLater = paramBoolean;
      return;
    }
  }
  
  private void calcTotalPartsCount()
  {
    if (this.uploadFirstPartLater)
    {
      if (this.isBigFile)
      {
        this.totalPartsCount = ((int)(this.totalFileSize - this.uploadChunkSize + this.uploadChunkSize - 1L) / this.uploadChunkSize + 1);
        return;
      }
      this.totalPartsCount = ((int)(this.totalFileSize - 1024L + this.uploadChunkSize - 1L) / this.uploadChunkSize + 1);
      return;
    }
    this.totalPartsCount = ((int)(this.totalFileSize + this.uploadChunkSize - 1L) / this.uploadChunkSize);
  }
  
  private void cleanup()
  {
    if (this.preferences == null) {
      this.preferences = ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0);
    }
    this.preferences.edit().remove(this.fileKey + "_time").remove(this.fileKey + "_size").remove(this.fileKey + "_uploaded").remove(this.fileKey + "_id").remove(this.fileKey + "_iv").remove(this.fileKey + "_key").remove(this.fileKey + "_ivc").commit();
    try
    {
      if (this.stream != null)
      {
        this.stream.close();
        this.stream = null;
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
  }
  
  private void startUploadRequest()
  {
    if (this.state != 1) {
      return;
    }
    try
    {
      this.started = true;
      if (this.stream != null) {
        break label1358;
      }
      File localFile = new File(this.uploadingFilePath);
      if (AndroidUtilities.isInternalUri(Uri.fromFile(localFile))) {
        throw new Exception("trying to upload internal file");
      }
    }
    catch (Exception localException1)
    {
      FileLog.e(localException1);
      this.state = 4;
      this.delegate.didFailedUploadingFile(this);
      cleanup();
      return;
    }
    this.stream = new RandomAccessFile(localException1, "r");
    if (this.estimatedSize != 0) {}
    final int i;
    for (this.totalFileSize = this.estimatedSize;; this.totalFileSize = localException1.length())
    {
      if (this.totalFileSize > 10485760L) {
        this.isBigFile = true;
      }
      this.uploadChunkSize = ((int)Math.max(128L, (this.totalFileSize + 3072000L - 1L) / 3072000L));
      if (1024 % this.uploadChunkSize == 0) {
        break label202;
      }
      i = 64;
      while (this.uploadChunkSize > i) {
        i *= 2;
      }
    }
    this.uploadChunkSize = i;
    label202:
    this.maxRequestsCount = (2048 / this.uploadChunkSize);
    if (this.isEncrypted)
    {
      this.freeRequestIvs = new ArrayList(this.maxRequestsCount);
      i = 0;
      while (i < this.maxRequestsCount)
      {
        this.freeRequestIvs.add(new byte[32]);
        i += 1;
      }
    }
    this.uploadChunkSize *= 1024;
    calcTotalPartsCount();
    this.readBuffer = new byte[this.uploadChunkSize];
    Object localObject3 = new StringBuilder().append(this.uploadingFilePath);
    Object localObject1;
    label319:
    final long l;
    final int k;
    final int j;
    label688:
    int m;
    if (this.isEncrypted)
    {
      localObject1 = "enc";
      this.fileKey = Utilities.MD5((String)localObject1);
      l = this.preferences.getLong(this.fileKey + "_size", 0L);
      this.uploadStartTime = ((int)(System.currentTimeMillis() / 1000L));
      k = 0;
      if ((this.uploadFirstPartLater) || (this.nextPartFirst) || (this.estimatedSize != 0) || (l != this.totalFileSize)) {
        break label1981;
      }
      this.currentFileId = this.preferences.getLong(this.fileKey + "_id", 0L);
      i = this.preferences.getInt(this.fileKey + "_time", 0);
      l = this.preferences.getLong(this.fileKey + "_uploaded", 0L);
      j = k;
      if (this.isEncrypted)
      {
        localObject1 = this.preferences.getString(this.fileKey + "_iv", null);
        localObject3 = this.preferences.getString(this.fileKey + "_key", null);
        if ((localObject1 == null) || (localObject3 == null)) {
          break label1966;
        }
        this.key = Utilities.hexToBytes((String)localObject3);
        this.iv = Utilities.hexToBytes((String)localObject1);
        if ((this.key == null) || (this.iv == null) || (this.key.length != 32) || (this.iv.length != 32)) {
          break label1961;
        }
        this.ivChange = new byte[32];
        System.arraycopy(this.iv, 0, this.ivChange, 0, 32);
        j = k;
      }
      if ((j != 0) || (i == 0)) {
        break label1976;
      }
      if ((this.isBigFile) && (i < this.uploadStartTime - 86400))
      {
        k = 0;
        i = j;
        if (k == 0) {
          break label1071;
        }
        if (l <= 0L) {
          break label1971;
        }
        this.readBytesCount = l;
        this.currentPartNum = ((int)(l / this.uploadChunkSize));
        if (!this.isBigFile) {
          k = 0;
        }
      }
      else
      {
        for (;;)
        {
          i = j;
          if (k >= this.readBytesCount / this.uploadChunkSize) {
            break label1071;
          }
          int n = this.stream.read(this.readBuffer);
          m = 0;
          i = m;
          if (this.isEncrypted)
          {
            i = m;
            if (n % 16 != 0) {
              i = 0 + (16 - n % 16);
            }
          }
          localObject1 = new NativeByteBuffer(n + i);
          if ((n != this.uploadChunkSize) || (this.totalPartsCount == this.currentPartNum + 1)) {
            this.isLastPart = true;
          }
          ((NativeByteBuffer)localObject1).writeBytes(this.readBuffer, 0, n);
          if (this.isEncrypted)
          {
            m = 0;
            for (;;)
            {
              if (m < i)
              {
                ((NativeByteBuffer)localObject1).writeByte(0);
                m += 1;
                continue;
                k = i;
                if (this.isBigFile) {
                  break;
                }
                k = i;
                if (i >= this.uploadStartTime - 5400.0F) {
                  break;
                }
                k = 0;
                break;
              }
            }
            Utilities.aesIgeEncryption(((NativeByteBuffer)localObject1).buffer, this.key, this.ivChange, true, true, 0, n + i);
          }
          ((NativeByteBuffer)localObject1).reuse();
          k += 1;
        }
      }
      this.stream.seek(l);
      i = j;
      if (this.isEncrypted)
      {
        localObject1 = this.preferences.getString(this.fileKey + "_ivc", null);
        if (localObject1 == null) {
          break label1280;
        }
        this.ivChange = Utilities.hexToBytes((String)localObject1);
        if (this.ivChange != null)
        {
          i = j;
          if (this.ivChange.length == 32) {}
        }
        else
        {
          i = 1;
          this.readBytesCount = 0L;
          this.currentPartNum = 0;
        }
      }
    }
    for (;;)
    {
      label1071:
      if (i != 0)
      {
        if (this.isEncrypted)
        {
          this.iv = new byte[32];
          this.key = new byte[32];
          this.ivChange = new byte[32];
          Utilities.random.nextBytes(this.iv);
          Utilities.random.nextBytes(this.key);
          System.arraycopy(this.iv, 0, this.ivChange, 0, 32);
        }
        this.currentFileId = Utilities.random.nextLong();
        if ((!this.nextPartFirst) && (!this.uploadFirstPartLater) && (this.estimatedSize == 0)) {
          storeFileUploadInfo();
        }
      }
      boolean bool = this.isEncrypted;
      if (bool)
      {
        try
        {
          localObject1 = MessageDigest.getInstance("MD5");
          localObject3 = new byte[64];
          System.arraycopy(this.key, 0, localObject3, 0, 32);
          System.arraycopy(this.iv, 0, localObject3, 32, 32);
          localObject1 = ((MessageDigest)localObject1).digest((byte[])localObject3);
          i = 0;
          while (i < 4)
          {
            this.fingerprint |= ((localObject1[i] ^ localObject1[(i + 4)]) & 0xFF) << i * 8;
            i += 1;
            continue;
            label1280:
            i = 1;
            this.readBytesCount = 0L;
            this.currentPartNum = 0;
          }
        }
        catch (Exception localException2)
        {
          FileLog.e(localException2);
        }
      }
      else
      {
        this.uploadedBytesCount = this.readBytesCount;
        this.lastSavedPartNum = this.currentPartNum;
        label1353:
        label1358:
        label1417:
        label1422:
        NativeByteBuffer localNativeByteBuffer;
        if (this.uploadFirstPartLater)
        {
          if (this.isBigFile)
          {
            this.stream.seek(this.uploadChunkSize);
            this.readBytesCount = this.uploadChunkSize;
            this.currentPartNum = 1;
          }
        }
        else
        {
          if ((this.estimatedSize != 0) && (this.readBytesCount + this.uploadChunkSize > this.availableSize)) {
            break;
          }
          if (!this.nextPartFirst) {
            break label1598;
          }
          this.stream.seek(0L);
          if (!this.isBigFile) {
            break label1579;
          }
          i = this.stream.read(this.readBuffer);
          this.currentPartNum = 0;
          if (i == -1) {
            break label1611;
          }
          k = 0;
          j = k;
          if (this.isEncrypted)
          {
            j = k;
            if (i % 16 != 0) {
              j = 0 + (16 - i % 16);
            }
          }
          localNativeByteBuffer = new NativeByteBuffer(i + j);
          if ((this.nextPartFirst) || (i != this.uploadChunkSize) || ((this.estimatedSize == 0) && (this.totalPartsCount == this.currentPartNum + 1)))
          {
            if (!this.uploadFirstPartLater) {
              break label1613;
            }
            this.nextPartFirst = true;
            this.uploadFirstPartLater = false;
          }
        }
        for (;;)
        {
          localNativeByteBuffer.writeBytes(this.readBuffer, 0, i);
          if (!this.isEncrypted) {
            break label1896;
          }
          k = 0;
          while (k < j)
          {
            localNativeByteBuffer.writeByte(0);
            k += 1;
          }
          this.stream.seek(1024L);
          this.readBytesCount = 1024L;
          break label1353;
          label1579:
          i = this.stream.read(this.readBuffer, 0, 1024);
          break label1417;
          label1598:
          i = this.stream.read(this.readBuffer);
          break label1422;
          label1611:
          break;
          label1613:
          this.isLastPart = true;
        }
        Utilities.aesIgeEncryption(localNativeByteBuffer.buffer, this.key, this.ivChange, true, true, 0, i + j);
        final Object localObject2 = (byte[])this.freeRequestIvs.get(0);
        System.arraycopy(this.ivChange, 0, localObject2, 0, 32);
        this.freeRequestIvs.remove(0);
        if (this.isBigFile)
        {
          localObject3 = new TLRPC.TL_upload_saveBigFilePart();
          j = this.currentPartNum;
          ((TLRPC.TL_upload_saveBigFilePart)localObject3).file_part = j;
          ((TLRPC.TL_upload_saveBigFilePart)localObject3).file_id = this.currentFileId;
          if (this.estimatedSize != 0)
          {
            ((TLRPC.TL_upload_saveBigFilePart)localObject3).file_total_parts = -1;
            label1727:
            ((TLRPC.TL_upload_saveBigFilePart)localObject3).bytes = localNativeByteBuffer;
          }
        }
        for (;;)
        {
          if ((this.isLastPart) && (this.nextPartFirst))
          {
            this.nextPartFirst = false;
            this.currentPartNum = (this.totalPartsCount - 1);
            this.stream.seek(this.totalFileSize);
          }
          this.readBytesCount += i;
          this.currentPartNum += 1;
          this.currentUploadRequetsCount += 1;
          k = this.requestNum;
          this.requestNum = (k + 1);
          l = j + i;
          m = ((TLObject)localObject3).getObjectSize();
          i = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject3, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              int i;
              if (paramAnonymousTLObject != null)
              {
                i = paramAnonymousTLObject.networkType;
                if (FileUploadOperation.this.currentType != 50331648) {
                  break label103;
                }
                StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentBytesCount(i, 3, this.val$requestSize);
                label41:
                if (localObject2 != null) {
                  FileUploadOperation.this.freeRequestIvs.add(localObject2);
                }
                FileUploadOperation.this.requestTokens.delete(k);
                if (!(paramAnonymousTLObject instanceof TLRPC.TL_boolTrue)) {
                  break label1097;
                }
                if (FileUploadOperation.this.state == 1) {
                  break label208;
                }
              }
              label103:
              label208:
              long l;
              label483:
              label500:
              label610:
              label703:
              do
              {
                do
                {
                  return;
                  i = ConnectionsManager.getCurrentNetworkType();
                  break;
                  if (FileUploadOperation.this.currentType == 33554432)
                  {
                    StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentBytesCount(i, 2, this.val$requestSize);
                    break label41;
                  }
                  if (FileUploadOperation.this.currentType == 16777216)
                  {
                    StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentBytesCount(i, 4, this.val$requestSize);
                    break label41;
                  }
                  if (FileUploadOperation.this.currentType != 67108864) {
                    break label41;
                  }
                  StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentBytesCount(i, 5, this.val$requestSize);
                  break label41;
                  FileUploadOperation.access$1602(FileUploadOperation.this, FileUploadOperation.this.uploadedBytesCount + i);
                  if (FileUploadOperation.this.estimatedSize != 0)
                  {
                    l = Math.max(FileUploadOperation.this.availableSize, FileUploadOperation.this.estimatedSize);
                    FileUploadOperation.this.delegate.didChangedUploadProgress(FileUploadOperation.this, (float)FileUploadOperation.this.uploadedBytesCount / (float)l);
                    FileUploadOperation.access$1110(FileUploadOperation.this);
                    if ((!FileUploadOperation.this.isLastPart) || (FileUploadOperation.this.currentUploadRequetsCount != 0) || (FileUploadOperation.this.state != 1)) {
                      break label703;
                    }
                    FileUploadOperation.access$1502(FileUploadOperation.this, 3);
                    if (FileUploadOperation.this.key != null) {
                      break label500;
                    }
                    if (!FileUploadOperation.this.isBigFile) {
                      break label483;
                    }
                    paramAnonymousTLObject = new TLRPC.TL_inputFileBig();
                  }
                  for (;;)
                  {
                    paramAnonymousTLObject.parts = FileUploadOperation.this.currentPartNum;
                    paramAnonymousTLObject.id = FileUploadOperation.this.currentFileId;
                    paramAnonymousTLObject.name = FileUploadOperation.this.uploadingFilePath.substring(FileUploadOperation.this.uploadingFilePath.lastIndexOf("/") + 1);
                    FileUploadOperation.this.delegate.didFinishUploadingFile(FileUploadOperation.this, paramAnonymousTLObject, null, null, null);
                    FileUploadOperation.this.cleanup();
                    if (FileUploadOperation.this.currentType != 50331648) {
                      break label610;
                    }
                    StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 3, 1);
                    return;
                    l = FileUploadOperation.this.totalFileSize;
                    break;
                    paramAnonymousTLObject = new TLRPC.TL_inputFile();
                    paramAnonymousTLObject.md5_checksum = "";
                  }
                  if (FileUploadOperation.this.isBigFile) {
                    paramAnonymousTLObject = new TLRPC.TL_inputEncryptedFileBigUploaded();
                  }
                  for (;;)
                  {
                    paramAnonymousTLObject.parts = FileUploadOperation.this.currentPartNum;
                    paramAnonymousTLObject.id = FileUploadOperation.this.currentFileId;
                    paramAnonymousTLObject.key_fingerprint = FileUploadOperation.this.fingerprint;
                    FileUploadOperation.this.delegate.didFinishUploadingFile(FileUploadOperation.this, null, paramAnonymousTLObject, FileUploadOperation.this.key, FileUploadOperation.this.iv);
                    FileUploadOperation.this.cleanup();
                    break;
                    paramAnonymousTLObject = new TLRPC.TL_inputEncryptedFileUploaded();
                    paramAnonymousTLObject.md5_checksum = "";
                  }
                  if (FileUploadOperation.this.currentType == 33554432)
                  {
                    StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 2, 1);
                    return;
                  }
                  if (FileUploadOperation.this.currentType == 16777216)
                  {
                    StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 4, 1);
                    return;
                  }
                } while (FileUploadOperation.this.currentType != 67108864);
                StatsController.getInstance(FileUploadOperation.this.currentAccount).incrementSentItemsCount(ConnectionsManager.getCurrentNetworkType(), 5, 1);
                return;
              } while (FileUploadOperation.this.currentUploadRequetsCount >= FileUploadOperation.this.maxRequestsCount);
              if ((FileUploadOperation.this.estimatedSize == 0) && (!FileUploadOperation.this.uploadFirstPartLater) && (!FileUploadOperation.this.nextPartFirst))
              {
                if (FileUploadOperation.this.saveInfoTimes >= 4) {
                  FileUploadOperation.access$2802(FileUploadOperation.this, 0);
                }
                if (j != FileUploadOperation.this.lastSavedPartNum) {
                  break label1026;
                }
                FileUploadOperation.access$2908(FileUploadOperation.this);
                l = l;
                paramAnonymousTLObject = localObject2;
                for (;;)
                {
                  paramAnonymousTL_error = (FileUploadOperation.UploadCachedResult)FileUploadOperation.this.cachedResults.get(FileUploadOperation.this.lastSavedPartNum);
                  if (paramAnonymousTL_error == null) {
                    break;
                  }
                  l = FileUploadOperation.UploadCachedResult.access$3100(paramAnonymousTL_error);
                  paramAnonymousTLObject = FileUploadOperation.UploadCachedResult.access$3200(paramAnonymousTL_error);
                  FileUploadOperation.this.cachedResults.remove(FileUploadOperation.this.lastSavedPartNum);
                  FileUploadOperation.access$2908(FileUploadOperation.this);
                }
                if (((FileUploadOperation.this.isBigFile) && (l % 1048576L == 0L)) || ((!FileUploadOperation.this.isBigFile) && (FileUploadOperation.this.saveInfoTimes == 0)))
                {
                  paramAnonymousTL_error = FileUploadOperation.this.preferences.edit();
                  paramAnonymousTL_error.putLong(FileUploadOperation.this.fileKey + "_uploaded", l);
                  if (FileUploadOperation.this.isEncrypted) {
                    paramAnonymousTL_error.putString(FileUploadOperation.this.fileKey + "_ivc", Utilities.bytesToHex(paramAnonymousTLObject));
                  }
                  paramAnonymousTL_error.commit();
                }
              }
              for (;;)
              {
                FileUploadOperation.access$2808(FileUploadOperation.this);
                FileUploadOperation.this.startUploadRequest();
                return;
                label1026:
                paramAnonymousTLObject = new FileUploadOperation.UploadCachedResult(FileUploadOperation.this, null);
                FileUploadOperation.UploadCachedResult.access$3102(paramAnonymousTLObject, l);
                if (localObject2 != null)
                {
                  FileUploadOperation.UploadCachedResult.access$3202(paramAnonymousTLObject, new byte[32]);
                  System.arraycopy(localObject2, 0, FileUploadOperation.UploadCachedResult.access$3200(paramAnonymousTLObject), 0, 32);
                }
                FileUploadOperation.this.cachedResults.put(j, paramAnonymousTLObject);
              }
              label1097:
              if (this.val$finalRequest != null) {
                FileLog.e("23123");
              }
              FileUploadOperation.access$1502(FileUploadOperation.this, 4);
              FileUploadOperation.this.delegate.didFailedUploadingFile(FileUploadOperation.this);
              FileUploadOperation.this.cleanup();
            }
          }, null, new WriteToSocketDelegate()
          {
            public void run()
            {
              Utilities.stageQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  if (FileUploadOperation.this.currentUploadRequetsCount < FileUploadOperation.this.maxRequestsCount) {
                    FileUploadOperation.this.startUploadRequest();
                  }
                }
              });
            }
          }, 0, Integer.MAX_VALUE, k % 4 << 16 | 0x4, true);
          this.requestTokens.put(k, i);
          return;
          label1896:
          localObject2 = null;
          break;
          ((TLRPC.TL_upload_saveBigFilePart)localObject3).file_total_parts = this.totalPartsCount;
          break label1727;
          localObject3 = new TLRPC.TL_upload_saveFilePart();
          j = this.currentPartNum;
          ((TLRPC.TL_upload_saveFilePart)localObject3).file_part = j;
          ((TLRPC.TL_upload_saveFilePart)localObject3).file_id = this.currentFileId;
          ((TLRPC.TL_upload_saveFilePart)localObject3).bytes = localNativeByteBuffer;
        }
        localObject2 = "";
        break label319;
        label1961:
        j = 1;
        break label688;
        label1966:
        j = 1;
        break label688;
        label1971:
        i = 1;
        continue;
        label1976:
        i = 1;
        continue;
        label1981:
        i = 1;
      }
    }
  }
  
  private void storeFileUploadInfo()
  {
    SharedPreferences.Editor localEditor = this.preferences.edit();
    localEditor.putInt(this.fileKey + "_time", this.uploadStartTime);
    localEditor.putLong(this.fileKey + "_size", this.totalFileSize);
    localEditor.putLong(this.fileKey + "_id", this.currentFileId);
    localEditor.remove(this.fileKey + "_uploaded");
    if (this.isEncrypted)
    {
      localEditor.putString(this.fileKey + "_iv", Utilities.bytesToHex(this.iv));
      localEditor.putString(this.fileKey + "_ivc", Utilities.bytesToHex(this.ivChange));
      localEditor.putString(this.fileKey + "_key", Utilities.bytesToHex(this.key));
    }
    localEditor.commit();
  }
  
  public void cancel()
  {
    if (this.state == 3) {
      return;
    }
    this.state = 2;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i = 0;
        while (i < FileUploadOperation.this.requestTokens.size())
        {
          ConnectionsManager.getInstance(FileUploadOperation.this.currentAccount).cancelRequest(FileUploadOperation.this.requestTokens.valueAt(i), true);
          i += 1;
        }
      }
    });
    this.delegate.didFailedUploadingFile(this);
    cleanup();
  }
  
  protected void checkNewDataAvailable(long paramLong1, final long paramLong2)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if ((FileUploadOperation.this.estimatedSize != 0) && (paramLong2 != 0L))
        {
          FileUploadOperation.access$402(FileUploadOperation.this, 0);
          FileUploadOperation.access$502(FileUploadOperation.this, paramLong2);
          FileUploadOperation.this.calcTotalPartsCount();
          if ((!FileUploadOperation.this.uploadFirstPartLater) && (FileUploadOperation.this.started)) {
            FileUploadOperation.this.storeFileUploadInfo();
          }
        }
        FileUploadOperation.access$1002(FileUploadOperation.this, this.val$newAvailableSize);
        if (FileUploadOperation.this.currentUploadRequetsCount < FileUploadOperation.this.maxRequestsCount) {
          FileUploadOperation.this.startUploadRequest();
        }
      }
    });
  }
  
  public long getTotalFileSize()
  {
    return this.totalFileSize;
  }
  
  public void setDelegate(FileUploadOperationDelegate paramFileUploadOperationDelegate)
  {
    this.delegate = paramFileUploadOperationDelegate;
  }
  
  public void start()
  {
    if (this.state != 0) {
      return;
    }
    this.state = 1;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        FileUploadOperation.access$002(FileUploadOperation.this, ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0));
        int i = 0;
        while (i < 8)
        {
          FileUploadOperation.this.startUploadRequest();
          i += 1;
        }
      }
    });
  }
  
  public static abstract interface FileUploadOperationDelegate
  {
    public abstract void didChangedUploadProgress(FileUploadOperation paramFileUploadOperation, float paramFloat);
    
    public abstract void didFailedUploadingFile(FileUploadOperation paramFileUploadOperation);
    
    public abstract void didFinishUploadingFile(FileUploadOperation paramFileUploadOperation, TLRPC.InputFile paramInputFile, TLRPC.InputEncryptedFile paramInputEncryptedFile, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);
  }
  
  private class UploadCachedResult
  {
    private long bytesOffset;
    private byte[] iv;
    
    private UploadCachedResult() {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/FileUploadOperation.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */