package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;

public class StatsController
{
  private static volatile StatsController[] Instance = new StatsController[3];
  private static final int TYPES_COUNT = 7;
  public static final int TYPE_AUDIOS = 3;
  public static final int TYPE_CALLS = 0;
  public static final int TYPE_FILES = 5;
  public static final int TYPE_MESSAGES = 1;
  public static final int TYPE_MOBILE = 0;
  public static final int TYPE_PHOTOS = 4;
  public static final int TYPE_ROAMING = 2;
  public static final int TYPE_TOTAL = 6;
  public static final int TYPE_VIDEOS = 2;
  public static final int TYPE_WIFI = 1;
  private static final ThreadLocal<Long> lastStatsSaveTime;
  private static DispatchQueue statsSaveQueue = new DispatchQueue("statsSaveQueue");
  private byte[] buffer = new byte[8];
  private int[] callsTotalTime = new int[3];
  private long lastInternalStatsSaveTime;
  private long[][] receivedBytes = (long[][])Array.newInstance(Long.TYPE, new int[] { 3, 7 });
  private int[][] receivedItems = (int[][])Array.newInstance(Integer.TYPE, new int[] { 3, 7 });
  private long[] resetStatsDate = new long[3];
  private Runnable saveRunnable = new Runnable()
  {
    public void run()
    {
      long l = System.currentTimeMillis();
      if (Math.abs(l - StatsController.this.lastInternalStatsSaveTime) < 2000L) {
        return;
      }
      StatsController.access$002(StatsController.this, l);
      for (;;)
      {
        int i;
        int j;
        try
        {
          StatsController.this.statsFile.seek(0L);
          i = 0;
        }
        catch (Exception localException)
        {
          return;
        }
        if (j < 7)
        {
          StatsController.this.statsFile.write(StatsController.this.longToBytes(StatsController.this.sentBytes[i][j]), 0, 8);
          StatsController.this.statsFile.write(StatsController.this.longToBytes(StatsController.this.receivedBytes[i][j]), 0, 8);
          StatsController.this.statsFile.write(StatsController.this.intToBytes(StatsController.this.sentItems[i][j]), 0, 4);
          StatsController.this.statsFile.write(StatsController.this.intToBytes(StatsController.this.receivedItems[i][j]), 0, 4);
          j += 1;
        }
        else
        {
          StatsController.this.statsFile.write(StatsController.this.intToBytes(StatsController.this.callsTotalTime[i]), 0, 4);
          StatsController.this.statsFile.write(StatsController.this.longToBytes(StatsController.this.resetStatsDate[i]), 0, 8);
          i += 1;
          break label265;
          StatsController.this.statsFile.getFD().sync();
          return;
          label265:
          if (i < 3) {
            j = 0;
          }
        }
      }
    }
  };
  private long[][] sentBytes = (long[][])Array.newInstance(Long.TYPE, new int[] { 3, 7 });
  private int[][] sentItems = (int[][])Array.newInstance(Integer.TYPE, new int[] { 3, 7 });
  private RandomAccessFile statsFile;
  
  static
  {
    lastStatsSaveTime = new ThreadLocal()
    {
      protected Long initialValue()
      {
        return Long.valueOf(System.currentTimeMillis() - 1000L);
      }
    };
  }
  
  private StatsController(int paramInt)
  {
    Object localObject = ApplicationLoader.getFilesDirFixed();
    if (paramInt != 0)
    {
      localObject = new File(ApplicationLoader.getFilesDirFixed(), "account" + paramInt + "/");
      ((File)localObject).mkdirs();
    }
    int m = 1;
    int i;
    int k;
    for (;;)
    {
      try
      {
        this.statsFile = new RandomAccessFile(new File((File)localObject, "stats2.dat"), "rw");
        i = m;
        if (this.statsFile.length() <= 0L) {
          continue;
        }
        j = 0;
        i = 0;
      }
      catch (Exception localException)
      {
        int j;
        i = m;
        continue;
      }
      if (k < 7)
      {
        this.statsFile.readFully(this.buffer, 0, 8);
        this.sentBytes[i][k] = bytesToLong(this.buffer);
        this.statsFile.readFully(this.buffer, 0, 8);
        this.receivedBytes[i][k] = bytesToLong(this.buffer);
        this.statsFile.readFully(this.buffer, 0, 4);
        this.sentItems[i][k] = bytesToInt(this.buffer);
        this.statsFile.readFully(this.buffer, 0, 4);
        this.receivedItems[i][k] = bytesToInt(this.buffer);
        k += 1;
      }
      else
      {
        this.statsFile.readFully(this.buffer, 0, 4);
        this.callsTotalTime[i] = bytesToInt(this.buffer);
        this.statsFile.readFully(this.buffer, 0, 8);
        this.resetStatsDate[i] = bytesToLong(this.buffer);
        if (this.resetStatsDate[i] != 0L) {
          break label839;
        }
        j = 1;
        this.resetStatsDate[i] = System.currentTimeMillis();
        break label839;
      }
    }
    label811:
    label839:
    label844:
    for (;;)
    {
      if (j != 0) {
        saveStats();
      }
      i = 0;
      if (i != 0)
      {
        if (paramInt == 0)
        {
          localObject = ApplicationLoader.applicationContext.getSharedPreferences("stats", 0);
          i = 0;
          paramInt = 0;
        }
        for (;;)
        {
          if (paramInt >= 3) {
            break label811;
          }
          this.callsTotalTime[paramInt] = ((SharedPreferences)localObject).getInt("callsTotalTime" + paramInt, 0);
          this.resetStatsDate[paramInt] = ((SharedPreferences)localObject).getLong("resetStatsDate" + paramInt, 0L);
          j = 0;
          for (;;)
          {
            if (j < 7)
            {
              this.sentBytes[paramInt][j] = ((SharedPreferences)localObject).getLong("sentBytes" + paramInt + "_" + j, 0L);
              this.receivedBytes[paramInt][j] = ((SharedPreferences)localObject).getLong("receivedBytes" + paramInt + "_" + j, 0L);
              this.sentItems[paramInt][j] = ((SharedPreferences)localObject).getInt("sentItems" + paramInt + "_" + j, 0);
              this.receivedItems[paramInt][j] = ((SharedPreferences)localObject).getInt("receivedItems" + paramInt + "_" + j, 0);
              j += 1;
              continue;
              localObject = ApplicationLoader.applicationContext.getSharedPreferences("stats" + paramInt, 0);
              break;
            }
          }
          if (this.resetStatsDate[paramInt] == 0L)
          {
            i = 1;
            this.resetStatsDate[paramInt] = System.currentTimeMillis();
          }
          paramInt += 1;
        }
        if (i != 0) {
          saveStats();
        }
      }
      return;
      for (;;)
      {
        if (i >= 3) {
          break label844;
        }
        k = 0;
        break;
        i += 1;
      }
    }
  }
  
  private int bytesToInt(byte[] paramArrayOfByte)
  {
    return paramArrayOfByte[0] << 24 | (paramArrayOfByte[1] & 0xFF) << 16 | (paramArrayOfByte[2] & 0xFF) << 8 | paramArrayOfByte[3] & 0xFF;
  }
  
  private long bytesToLong(byte[] paramArrayOfByte)
  {
    return (paramArrayOfByte[0] & 0xFF) << 56 | (paramArrayOfByte[1] & 0xFF) << 48 | (paramArrayOfByte[2] & 0xFF) << 40 | (paramArrayOfByte[3] & 0xFF) << 32 | (paramArrayOfByte[4] & 0xFF) << 24 | (paramArrayOfByte[5] & 0xFF) << 16 | (paramArrayOfByte[6] & 0xFF) << 8 | paramArrayOfByte[7] & 0xFF;
  }
  
  public static StatsController getInstance(int paramInt)
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
        localObject1 = new StatsController(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (StatsController)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (StatsController)localObject1;
  }
  
  private byte[] intToBytes(int paramInt)
  {
    this.buffer[0] = ((byte)(paramInt >>> 24));
    this.buffer[1] = ((byte)(paramInt >>> 16));
    this.buffer[2] = ((byte)(paramInt >>> 8));
    this.buffer[3] = ((byte)paramInt);
    return this.buffer;
  }
  
  private byte[] longToBytes(long paramLong)
  {
    this.buffer[0] = ((byte)(int)(paramLong >>> 56));
    this.buffer[1] = ((byte)(int)(paramLong >>> 48));
    this.buffer[2] = ((byte)(int)(paramLong >>> 40));
    this.buffer[3] = ((byte)(int)(paramLong >>> 32));
    this.buffer[4] = ((byte)(int)(paramLong >>> 24));
    this.buffer[5] = ((byte)(int)(paramLong >>> 16));
    this.buffer[6] = ((byte)(int)(paramLong >>> 8));
    this.buffer[7] = ((byte)(int)paramLong);
    return this.buffer;
  }
  
  private void saveStats()
  {
    long l = System.currentTimeMillis();
    if (Math.abs(l - ((Long)lastStatsSaveTime.get()).longValue()) >= 2000L)
    {
      lastStatsSaveTime.set(Long.valueOf(l));
      statsSaveQueue.postRunnable(this.saveRunnable);
    }
  }
  
  public int getCallsTotalTime(int paramInt)
  {
    return this.callsTotalTime[paramInt];
  }
  
  public long getReceivedBytesCount(int paramInt1, int paramInt2)
  {
    if (paramInt2 == 1) {
      return this.receivedBytes[paramInt1][6] - this.receivedBytes[paramInt1][5] - this.receivedBytes[paramInt1][3] - this.receivedBytes[paramInt1][2] - this.receivedBytes[paramInt1][4];
    }
    return this.receivedBytes[paramInt1][paramInt2];
  }
  
  public int getRecivedItemsCount(int paramInt1, int paramInt2)
  {
    return this.receivedItems[paramInt1][paramInt2];
  }
  
  public long getResetStatsDate(int paramInt)
  {
    return this.resetStatsDate[paramInt];
  }
  
  public long getSentBytesCount(int paramInt1, int paramInt2)
  {
    if (paramInt2 == 1) {
      return this.sentBytes[paramInt1][6] - this.sentBytes[paramInt1][5] - this.sentBytes[paramInt1][3] - this.sentBytes[paramInt1][2] - this.sentBytes[paramInt1][4];
    }
    return this.sentBytes[paramInt1][paramInt2];
  }
  
  public int getSentItemsCount(int paramInt1, int paramInt2)
  {
    return this.sentItems[paramInt1][paramInt2];
  }
  
  public void incrementReceivedBytesCount(int paramInt1, int paramInt2, long paramLong)
  {
    long[] arrayOfLong = this.receivedBytes[paramInt1];
    arrayOfLong[paramInt2] += paramLong;
    saveStats();
  }
  
  public void incrementReceivedItemsCount(int paramInt1, int paramInt2, int paramInt3)
  {
    int[] arrayOfInt = this.receivedItems[paramInt1];
    arrayOfInt[paramInt2] += paramInt3;
    saveStats();
  }
  
  public void incrementSentBytesCount(int paramInt1, int paramInt2, long paramLong)
  {
    long[] arrayOfLong = this.sentBytes[paramInt1];
    arrayOfLong[paramInt2] += paramLong;
    saveStats();
  }
  
  public void incrementSentItemsCount(int paramInt1, int paramInt2, int paramInt3)
  {
    int[] arrayOfInt = this.sentItems[paramInt1];
    arrayOfInt[paramInt2] += paramInt3;
    saveStats();
  }
  
  public void incrementTotalCallsTime(int paramInt1, int paramInt2)
  {
    int[] arrayOfInt = this.callsTotalTime;
    arrayOfInt[paramInt1] += paramInt2;
    saveStats();
  }
  
  public void resetStats(int paramInt)
  {
    this.resetStatsDate[paramInt] = System.currentTimeMillis();
    int i = 0;
    while (i < 7)
    {
      this.sentBytes[paramInt][i] = 0L;
      this.receivedBytes[paramInt][i] = 0L;
      this.sentItems[paramInt][i] = 0;
      this.receivedItems[paramInt][i] = 0;
      i += 1;
    }
    this.callsTotalTime[paramInt] = 0;
    saveStats();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/StatsController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */