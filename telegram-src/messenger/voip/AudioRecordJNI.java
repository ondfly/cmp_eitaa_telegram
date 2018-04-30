package org.telegram.messenger.voip;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;
import java.nio.ByteBuffer;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

public class AudioRecordJNI
{
  private AcousticEchoCanceler aec;
  private AutomaticGainControl agc;
  private AudioRecord audioRecord;
  private ByteBuffer buffer;
  private int bufferSize;
  private long nativeInst;
  private boolean needResampling = false;
  private NoiseSuppressor ns;
  private boolean running;
  private Thread thread;
  
  public AudioRecordJNI(long paramLong)
  {
    this.nativeInst = paramLong;
  }
  
  private int getBufferSize(int paramInt1, int paramInt2)
  {
    return Math.max(AudioRecord.getMinBufferSize(paramInt2, 16, 2), paramInt1);
  }
  
  private native void nativeCallback(ByteBuffer paramByteBuffer);
  
  private void startThread()
  {
    if (this.thread != null) {
      throw new IllegalStateException("thread already started");
    }
    this.running = true;
    if (this.needResampling) {}
    for (final ByteBuffer localByteBuffer = ByteBuffer.allocateDirect(1764);; localByteBuffer = null)
    {
      this.thread = new Thread(new Runnable()
      {
        public void run()
        {
          for (;;)
          {
            if (AudioRecordJNI.this.running) {}
            try
            {
              if (!AudioRecordJNI.this.needResampling) {
                AudioRecordJNI.this.audioRecord.read(AudioRecordJNI.this.buffer, 1920);
              }
              while (!AudioRecordJNI.this.running)
              {
                AudioRecordJNI.this.audioRecord.stop();
                Log.i("tg-voip", "audiotrack thread exits");
                return;
                AudioRecordJNI.this.audioRecord.read(localByteBuffer, 1764);
                Resampler.convert44to48(localByteBuffer, AudioRecordJNI.this.buffer);
              }
            }
            catch (Exception localException)
            {
              FileLog.e(localException);
            }
            AudioRecordJNI.this.nativeCallback(AudioRecordJNI.this.buffer);
          }
        }
      });
      this.thread.start();
      return;
    }
  }
  
  private boolean tryInit(int paramInt1, int paramInt2)
  {
    if (this.audioRecord != null) {}
    try
    {
      this.audioRecord.release();
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("Trying to initialize AudioRecord with source=" + paramInt1 + " and sample rate=" + paramInt2);
      }
      int i = getBufferSize(this.bufferSize, 48000);
      try
      {
        this.audioRecord = new AudioRecord(paramInt1, paramInt2, 16, 2, i);
        if (paramInt2 != 48000)
        {
          bool = true;
          this.needResampling = bool;
          if ((this.audioRecord == null) || (this.audioRecord.getState() != 1)) {
            break label132;
          }
          return true;
        }
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          FileLog.e("AudioRecord init failed!", localException1);
          continue;
          boolean bool = false;
        }
      }
      label132:
      return false;
    }
    catch (Exception localException2)
    {
      for (;;) {}
    }
  }
  
  public void init(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.audioRecord != null) {
      throw new IllegalStateException("already inited");
    }
    this.bufferSize = paramInt4;
    boolean bool = tryInit(7, 48000);
    if (!bool) {
      tryInit(1, 48000);
    }
    if (!bool) {
      tryInit(7, 44100);
    }
    if (!bool) {
      tryInit(1, 44100);
    }
    this.buffer = ByteBuffer.allocateDirect(paramInt4);
  }
  
  public void release()
  {
    this.running = false;
    if (this.thread != null) {}
    try
    {
      this.thread.join();
      this.thread = null;
      if (this.audioRecord != null)
      {
        this.audioRecord.release();
        this.audioRecord = null;
      }
      if (this.agc != null)
      {
        this.agc.release();
        this.agc = null;
      }
      if (this.ns != null)
      {
        this.ns.release();
        this.ns = null;
      }
      if (this.aec != null)
      {
        this.aec.release();
        this.aec = null;
      }
      return;
    }
    catch (InterruptedException localInterruptedException)
    {
      for (;;)
      {
        FileLog.e(localInterruptedException);
      }
    }
  }
  
  /* Error */
  public boolean start()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 70	org/telegram/messenger/voip/AudioRecordJNI:thread	Ljava/lang/Thread;
    //   4: ifnonnull +258 -> 262
    //   7: aload_0
    //   8: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   11: ifnonnull +5 -> 16
    //   14: iconst_0
    //   15: ireturn
    //   16: aload_0
    //   17: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   20: invokevirtual 182	android/media/AudioRecord:startRecording	()V
    //   23: getstatic 187	android/os/Build$VERSION:SDK_INT	I
    //   26: istore_1
    //   27: iload_1
    //   28: bipush 16
    //   30: if_icmplt +121 -> 151
    //   33: invokestatic 190	android/media/audiofx/AutomaticGainControl:isAvailable	()Z
    //   36: ifeq +121 -> 157
    //   39: aload_0
    //   40: aload_0
    //   41: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   44: invokevirtual 193	android/media/AudioRecord:getAudioSessionId	()I
    //   47: invokestatic 197	android/media/audiofx/AutomaticGainControl:create	(I)Landroid/media/audiofx/AutomaticGainControl;
    //   50: putfield 160	org/telegram/messenger/voip/AudioRecordJNI:agc	Landroid/media/audiofx/AutomaticGainControl;
    //   53: aload_0
    //   54: getfield 160	org/telegram/messenger/voip/AudioRecordJNI:agc	Landroid/media/audiofx/AutomaticGainControl;
    //   57: ifnull +12 -> 69
    //   60: aload_0
    //   61: getfield 160	org/telegram/messenger/voip/AudioRecordJNI:agc	Landroid/media/audiofx/AutomaticGainControl;
    //   64: iconst_0
    //   65: invokevirtual 201	android/media/audiofx/AutomaticGainControl:setEnabled	(Z)I
    //   68: pop
    //   69: invokestatic 202	android/media/audiofx/NoiseSuppressor:isAvailable	()Z
    //   72: ifeq +130 -> 202
    //   75: aload_0
    //   76: aload_0
    //   77: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   80: invokevirtual 193	android/media/AudioRecord:getAudioSessionId	()I
    //   83: invokestatic 205	android/media/audiofx/NoiseSuppressor:create	(I)Landroid/media/audiofx/NoiseSuppressor;
    //   86: putfield 165	org/telegram/messenger/voip/AudioRecordJNI:ns	Landroid/media/audiofx/NoiseSuppressor;
    //   89: aload_0
    //   90: getfield 165	org/telegram/messenger/voip/AudioRecordJNI:ns	Landroid/media/audiofx/NoiseSuppressor;
    //   93: ifnull +17 -> 110
    //   96: aload_0
    //   97: getfield 165	org/telegram/messenger/voip/AudioRecordJNI:ns	Landroid/media/audiofx/NoiseSuppressor;
    //   100: ldc -49
    //   102: iconst_1
    //   103: invokestatic 213	org/telegram/messenger/voip/VoIPServerConfig:getBoolean	(Ljava/lang/String;Z)Z
    //   106: invokevirtual 214	android/media/audiofx/NoiseSuppressor:setEnabled	(Z)I
    //   109: pop
    //   110: invokestatic 215	android/media/audiofx/AcousticEchoCanceler:isAvailable	()Z
    //   113: ifeq +119 -> 232
    //   116: aload_0
    //   117: aload_0
    //   118: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   121: invokevirtual 193	android/media/AudioRecord:getAudioSessionId	()I
    //   124: invokestatic 218	android/media/audiofx/AcousticEchoCanceler:create	(I)Landroid/media/audiofx/AcousticEchoCanceler;
    //   127: putfield 170	org/telegram/messenger/voip/AudioRecordJNI:aec	Landroid/media/audiofx/AcousticEchoCanceler;
    //   130: aload_0
    //   131: getfield 170	org/telegram/messenger/voip/AudioRecordJNI:aec	Landroid/media/audiofx/AcousticEchoCanceler;
    //   134: ifnull +17 -> 151
    //   137: aload_0
    //   138: getfield 170	org/telegram/messenger/voip/AudioRecordJNI:aec	Landroid/media/audiofx/AcousticEchoCanceler;
    //   141: ldc -36
    //   143: iconst_1
    //   144: invokestatic 213	org/telegram/messenger/voip/VoIPServerConfig:getBoolean	(Ljava/lang/String;Z)Z
    //   147: invokevirtual 221	android/media/audiofx/AcousticEchoCanceler:setEnabled	(Z)I
    //   150: pop
    //   151: aload_0
    //   152: invokespecial 223	org/telegram/messenger/voip/AudioRecordJNI:startThread	()V
    //   155: iconst_1
    //   156: ireturn
    //   157: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   160: ifeq -91 -> 69
    //   163: ldc -31
    //   165: invokestatic 228	org/telegram/messenger/FileLog:w	(Ljava/lang/String;)V
    //   168: goto -99 -> 69
    //   171: astore_2
    //   172: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   175: ifeq -106 -> 69
    //   178: ldc -26
    //   180: aload_2
    //   181: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   184: goto -115 -> 69
    //   187: astore_2
    //   188: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   191: ifeq +81 -> 272
    //   194: ldc -24
    //   196: aload_2
    //   197: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   200: iconst_0
    //   201: ireturn
    //   202: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   205: ifeq -95 -> 110
    //   208: ldc -22
    //   210: invokestatic 228	org/telegram/messenger/FileLog:w	(Ljava/lang/String;)V
    //   213: goto -103 -> 110
    //   216: astore_2
    //   217: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   220: ifeq -110 -> 110
    //   223: ldc -20
    //   225: aload_2
    //   226: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   229: goto -119 -> 110
    //   232: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   235: ifeq -84 -> 151
    //   238: ldc -18
    //   240: invokestatic 228	org/telegram/messenger/FileLog:w	(Ljava/lang/String;)V
    //   243: goto -92 -> 151
    //   246: astore_2
    //   247: getstatic 105	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   250: ifeq -99 -> 151
    //   253: ldc -16
    //   255: aload_2
    //   256: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   259: goto -108 -> 151
    //   262: aload_0
    //   263: getfield 48	org/telegram/messenger/voip/AudioRecordJNI:audioRecord	Landroid/media/AudioRecord;
    //   266: invokevirtual 182	android/media/AudioRecord:startRecording	()V
    //   269: goto -114 -> 155
    //   272: iconst_0
    //   273: ireturn
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	274	0	this	AudioRecordJNI
    //   26	5	1	i	int
    //   171	10	2	localThrowable1	Throwable
    //   187	10	2	localException	Exception
    //   216	10	2	localThrowable2	Throwable
    //   246	10	2	localThrowable3	Throwable
    // Exception table:
    //   from	to	target	type
    //   33	69	171	java/lang/Throwable
    //   157	168	171	java/lang/Throwable
    //   0	14	187	java/lang/Exception
    //   16	27	187	java/lang/Exception
    //   33	69	187	java/lang/Exception
    //   69	110	187	java/lang/Exception
    //   110	151	187	java/lang/Exception
    //   151	155	187	java/lang/Exception
    //   157	168	187	java/lang/Exception
    //   172	184	187	java/lang/Exception
    //   202	213	187	java/lang/Exception
    //   217	229	187	java/lang/Exception
    //   232	243	187	java/lang/Exception
    //   247	259	187	java/lang/Exception
    //   262	269	187	java/lang/Exception
    //   69	110	216	java/lang/Throwable
    //   202	213	216	java/lang/Throwable
    //   110	151	246	java/lang/Throwable
    //   232	243	246	java/lang/Throwable
  }
  
  public void stop()
  {
    if (this.audioRecord != null) {
      this.audioRecord.stop();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/AudioRecordJNI.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */