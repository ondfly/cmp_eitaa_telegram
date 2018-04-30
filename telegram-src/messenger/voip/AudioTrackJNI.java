package org.telegram.messenger.voip;

import android.media.AudioTrack;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

public class AudioTrackJNI
{
  private AudioTrack audioTrack;
  private byte[] buffer = new byte['ހ'];
  private int bufferSize;
  private long nativeInst;
  private boolean needResampling;
  private boolean running;
  private Thread thread;
  
  public AudioTrackJNI(long paramLong)
  {
    this.nativeInst = paramLong;
  }
  
  private int getBufferSize(int paramInt1, int paramInt2)
  {
    return Math.max(AudioTrack.getMinBufferSize(paramInt2, 4, 2), paramInt1);
  }
  
  private native void nativeCallback(byte[] paramArrayOfByte);
  
  private void startThread()
  {
    if (this.thread != null) {
      throw new IllegalStateException("thread already started");
    }
    this.running = true;
    this.thread = new Thread(new Runnable()
    {
      /* Error */
      public void run()
      {
        // Byte code:
        //   0: aload_0
        //   1: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   4: invokestatic 27	org/telegram/messenger/voip/AudioTrackJNI:access$000	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Landroid/media/AudioTrack;
        //   7: invokevirtual 32	android/media/AudioTrack:play	()V
        //   10: aload_0
        //   11: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   14: invokestatic 36	org/telegram/messenger/voip/AudioTrackJNI:access$100	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Z
        //   17: ifeq +170 -> 187
        //   20: sipush 1920
        //   23: invokestatic 42	java/nio/ByteBuffer:allocateDirect	(I)Ljava/nio/ByteBuffer;
        //   26: astore_1
        //   27: aload_0
        //   28: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   31: invokestatic 36	org/telegram/messenger/voip/AudioTrackJNI:access$100	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Z
        //   34: ifeq +158 -> 192
        //   37: sipush 1764
        //   40: invokestatic 42	java/nio/ByteBuffer:allocateDirect	(I)Ljava/nio/ByteBuffer;
        //   43: astore_2
        //   44: aload_0
        //   45: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   48: invokestatic 45	org/telegram/messenger/voip/AudioTrackJNI:access$200	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Z
        //   51: ifeq +113 -> 164
        //   54: aload_0
        //   55: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   58: invokestatic 36	org/telegram/messenger/voip/AudioTrackJNI:access$100	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Z
        //   61: ifeq +136 -> 197
        //   64: aload_0
        //   65: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   68: aload_0
        //   69: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   72: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   75: invokestatic 53	org/telegram/messenger/voip/AudioTrackJNI:access$400	(Lorg/telegram/messenger/voip/AudioTrackJNI;[B)V
        //   78: aload_1
        //   79: invokevirtual 57	java/nio/ByteBuffer:rewind	()Ljava/nio/Buffer;
        //   82: pop
        //   83: aload_1
        //   84: aload_0
        //   85: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   88: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   91: invokevirtual 61	java/nio/ByteBuffer:put	([B)Ljava/nio/ByteBuffer;
        //   94: pop
        //   95: aload_1
        //   96: aload_2
        //   97: invokestatic 67	org/telegram/messenger/voip/Resampler:convert48to44	(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
        //   100: pop
        //   101: aload_2
        //   102: invokevirtual 57	java/nio/ByteBuffer:rewind	()Ljava/nio/Buffer;
        //   105: pop
        //   106: aload_2
        //   107: aload_0
        //   108: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   111: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   114: iconst_0
        //   115: sipush 1764
        //   118: invokevirtual 71	java/nio/ByteBuffer:get	([BII)Ljava/nio/ByteBuffer;
        //   121: pop
        //   122: aload_0
        //   123: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   126: invokestatic 27	org/telegram/messenger/voip/AudioTrackJNI:access$000	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Landroid/media/AudioTrack;
        //   129: aload_0
        //   130: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   133: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   136: iconst_0
        //   137: sipush 1764
        //   140: invokevirtual 75	android/media/AudioTrack:write	([BII)I
        //   143: pop
        //   144: aload_0
        //   145: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   148: invokestatic 45	org/telegram/messenger/voip/AudioTrackJNI:access$200	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Z
        //   151: ifne -107 -> 44
        //   154: aload_0
        //   155: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   158: invokestatic 27	org/telegram/messenger/voip/AudioTrackJNI:access$000	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Landroid/media/AudioTrack;
        //   161: invokevirtual 78	android/media/AudioTrack:stop	()V
        //   164: ldc 80
        //   166: ldc 82
        //   168: invokestatic 88	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
        //   171: pop
        //   172: return
        //   173: astore_1
        //   174: getstatic 94	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
        //   177: ifeq -5 -> 172
        //   180: ldc 96
        //   182: aload_1
        //   183: invokestatic 102	org/telegram/messenger/FileLog:e	(Ljava/lang/String;Ljava/lang/Throwable;)V
        //   186: return
        //   187: aconst_null
        //   188: astore_1
        //   189: goto -162 -> 27
        //   192: aconst_null
        //   193: astore_2
        //   194: goto -150 -> 44
        //   197: aload_0
        //   198: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   201: aload_0
        //   202: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   205: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   208: invokestatic 53	org/telegram/messenger/voip/AudioTrackJNI:access$400	(Lorg/telegram/messenger/voip/AudioTrackJNI;[B)V
        //   211: aload_0
        //   212: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   215: invokestatic 27	org/telegram/messenger/voip/AudioTrackJNI:access$000	(Lorg/telegram/messenger/voip/AudioTrackJNI;)Landroid/media/AudioTrack;
        //   218: aload_0
        //   219: getfield 17	org/telegram/messenger/voip/AudioTrackJNI$1:this$0	Lorg/telegram/messenger/voip/AudioTrackJNI;
        //   222: invokestatic 49	org/telegram/messenger/voip/AudioTrackJNI:access$300	(Lorg/telegram/messenger/voip/AudioTrackJNI;)[B
        //   225: iconst_0
        //   226: sipush 1920
        //   229: invokevirtual 75	android/media/AudioTrack:write	([BII)I
        //   232: pop
        //   233: goto -89 -> 144
        //   236: astore_3
        //   237: aload_3
        //   238: invokestatic 105	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
        //   241: goto -197 -> 44
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	244	0	this	1
        //   26	70	1	localByteBuffer1	java.nio.ByteBuffer
        //   173	10	1	localException1	Exception
        //   188	1	1	localObject	Object
        //   43	151	2	localByteBuffer2	java.nio.ByteBuffer
        //   236	2	3	localException2	Exception
        // Exception table:
        //   from	to	target	type
        //   0	10	173	java/lang/Exception
        //   54	144	236	java/lang/Exception
        //   144	164	236	java/lang/Exception
        //   197	233	236	java/lang/Exception
      }
    });
    this.thread.start();
  }
  
  public void init(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (this.audioTrack != null) {
      throw new IllegalStateException("already inited");
    }
    paramInt2 = getBufferSize(paramInt4, 48000);
    this.bufferSize = paramInt4;
    if (paramInt3 == 1) {
      paramInt1 = 4;
    }
    for (;;)
    {
      this.audioTrack = new AudioTrack(0, 48000, paramInt1, 2, paramInt2, 1);
      if (this.audioTrack.getState() != 1) {}
      try
      {
        this.audioTrack.release();
        paramInt2 = getBufferSize(paramInt4 * 6, 44100);
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("buffer size: " + paramInt2);
        }
        if (paramInt3 == 1) {}
        for (paramInt1 = 4;; paramInt1 = 12)
        {
          this.audioTrack = new AudioTrack(0, 44100, paramInt1, 2, paramInt2, 1);
          this.needResampling = true;
          return;
          paramInt1 = 12;
          break;
        }
      }
      catch (Throwable localThrowable)
      {
        for (;;) {}
      }
    }
  }
  
  public void release()
  {
    this.running = false;
    if (this.thread != null) {}
    try
    {
      this.thread.join();
      this.thread = null;
      if (this.audioTrack != null)
      {
        this.audioTrack.release();
        this.audioTrack = null;
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
  
  public void start()
  {
    if (this.thread == null)
    {
      startThread();
      return;
    }
    this.audioTrack.play();
  }
  
  public void stop()
  {
    if (this.audioTrack != null) {}
    try
    {
      this.audioTrack.stop();
      return;
    }
    catch (Exception localException) {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/AudioTrackJNI.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */