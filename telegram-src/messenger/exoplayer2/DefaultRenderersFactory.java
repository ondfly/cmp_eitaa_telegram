package org.telegram.messenger.exoplayer2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import org.telegram.messenger.exoplayer2.audio.AudioProcessor;
import org.telegram.messenger.exoplayer2.audio.AudioRendererEventListener;
import org.telegram.messenger.exoplayer2.drm.DrmSessionManager;
import org.telegram.messenger.exoplayer2.drm.FrameworkMediaCrypto;
import org.telegram.messenger.exoplayer2.metadata.MetadataOutput;
import org.telegram.messenger.exoplayer2.metadata.MetadataRenderer;
import org.telegram.messenger.exoplayer2.text.TextOutput;
import org.telegram.messenger.exoplayer2.text.TextRenderer;
import org.telegram.messenger.exoplayer2.video.VideoRendererEventListener;

public class DefaultRenderersFactory
  implements RenderersFactory
{
  public static final long DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS = 5000L;
  public static final int EXTENSION_RENDERER_MODE_OFF = 0;
  public static final int EXTENSION_RENDERER_MODE_ON = 1;
  public static final int EXTENSION_RENDERER_MODE_PREFER = 2;
  protected static final int MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY = 50;
  private static final String TAG = "DefaultRenderersFactory";
  private final long allowedVideoJoiningTimeMs;
  private final Context context;
  private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
  private final int extensionRendererMode;
  
  public DefaultRenderersFactory(Context paramContext)
  {
    this(paramContext, null);
  }
  
  public DefaultRenderersFactory(Context paramContext, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager)
  {
    this(paramContext, paramDrmSessionManager, 0);
  }
  
  public DefaultRenderersFactory(Context paramContext, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, int paramInt)
  {
    this(paramContext, paramDrmSessionManager, paramInt, 5000L);
  }
  
  public DefaultRenderersFactory(Context paramContext, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, int paramInt, long paramLong)
  {
    this.context = paramContext;
    this.drmSessionManager = paramDrmSessionManager;
    this.extensionRendererMode = paramInt;
    this.allowedVideoJoiningTimeMs = paramLong;
  }
  
  protected AudioProcessor[] buildAudioProcessors()
  {
    return new AudioProcessor[0];
  }
  
  /* Error */
  protected void buildAudioRenderers(Context paramContext, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, AudioProcessor[] paramArrayOfAudioProcessor, Handler paramHandler, AudioRendererEventListener paramAudioRendererEventListener, int paramInt, ArrayList<Renderer> paramArrayList)
  {
    // Byte code:
    //   0: aload 7
    //   2: new 72	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer
    //   5: dup
    //   6: getstatic 78	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector:DEFAULT	Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector;
    //   9: aload_2
    //   10: iconst_1
    //   11: aload 4
    //   13: aload 5
    //   15: aload_1
    //   16: invokestatic 84	org/telegram/messenger/exoplayer2/audio/AudioCapabilities:getCapabilities	(Landroid/content/Context;)Lorg/telegram/messenger/exoplayer2/audio/AudioCapabilities;
    //   19: aload_3
    //   20: invokespecial 87	org/telegram/messenger/exoplayer2/audio/MediaCodecAudioRenderer:<init>	(Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector;Lorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;ZLandroid/os/Handler;Lorg/telegram/messenger/exoplayer2/audio/AudioRendererEventListener;Lorg/telegram/messenger/exoplayer2/audio/AudioCapabilities;[Lorg/telegram/messenger/exoplayer2/audio/AudioProcessor;)V
    //   23: invokevirtual 93	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   26: pop
    //   27: iload 6
    //   29: ifne +4 -> 33
    //   32: return
    //   33: aload 7
    //   35: invokevirtual 97	java/util/ArrayList:size	()I
    //   38: istore 8
    //   40: iload 6
    //   42: iconst_2
    //   43: if_icmpne +304 -> 347
    //   46: iload 8
    //   48: iconst_1
    //   49: isub
    //   50: istore 6
    //   52: ldc 99
    //   54: invokestatic 105	java/lang/Class:forName	(Ljava/lang/String;)Ljava/lang/Class;
    //   57: iconst_3
    //   58: anewarray 101	java/lang/Class
    //   61: dup
    //   62: iconst_0
    //   63: ldc 107
    //   65: aastore
    //   66: dup
    //   67: iconst_1
    //   68: ldc 109
    //   70: aastore
    //   71: dup
    //   72: iconst_2
    //   73: ldc 111
    //   75: aastore
    //   76: invokevirtual 115	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   79: iconst_3
    //   80: anewarray 4	java/lang/Object
    //   83: dup
    //   84: iconst_0
    //   85: aload 4
    //   87: aastore
    //   88: dup
    //   89: iconst_1
    //   90: aload 5
    //   92: aastore
    //   93: dup
    //   94: iconst_2
    //   95: aload_3
    //   96: aastore
    //   97: invokevirtual 121	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   100: checkcast 123	org/telegram/messenger/exoplayer2/Renderer
    //   103: astore_1
    //   104: iload 6
    //   106: iconst_1
    //   107: iadd
    //   108: istore 8
    //   110: aload 7
    //   112: iload 6
    //   114: aload_1
    //   115: invokevirtual 126	java/util/ArrayList:add	(ILjava/lang/Object;)V
    //   118: ldc 26
    //   120: ldc -128
    //   122: invokestatic 134	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   125: pop
    //   126: iload 8
    //   128: istore 6
    //   130: ldc -120
    //   132: invokestatic 105	java/lang/Class:forName	(Ljava/lang/String;)Ljava/lang/Class;
    //   135: iconst_3
    //   136: anewarray 101	java/lang/Class
    //   139: dup
    //   140: iconst_0
    //   141: ldc 107
    //   143: aastore
    //   144: dup
    //   145: iconst_1
    //   146: ldc 109
    //   148: aastore
    //   149: dup
    //   150: iconst_2
    //   151: ldc 111
    //   153: aastore
    //   154: invokevirtual 115	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   157: iconst_3
    //   158: anewarray 4	java/lang/Object
    //   161: dup
    //   162: iconst_0
    //   163: aload 4
    //   165: aastore
    //   166: dup
    //   167: iconst_1
    //   168: aload 5
    //   170: aastore
    //   171: dup
    //   172: iconst_2
    //   173: aload_3
    //   174: aastore
    //   175: invokevirtual 121	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   178: checkcast 123	org/telegram/messenger/exoplayer2/Renderer
    //   181: astore_1
    //   182: iload 6
    //   184: iconst_1
    //   185: iadd
    //   186: istore 8
    //   188: aload 7
    //   190: iload 6
    //   192: aload_1
    //   193: invokevirtual 126	java/util/ArrayList:add	(ILjava/lang/Object;)V
    //   196: ldc 26
    //   198: ldc -118
    //   200: invokestatic 134	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   203: pop
    //   204: ldc -116
    //   206: invokestatic 105	java/lang/Class:forName	(Ljava/lang/String;)Ljava/lang/Class;
    //   209: iconst_3
    //   210: anewarray 101	java/lang/Class
    //   213: dup
    //   214: iconst_0
    //   215: ldc 107
    //   217: aastore
    //   218: dup
    //   219: iconst_1
    //   220: ldc 109
    //   222: aastore
    //   223: dup
    //   224: iconst_2
    //   225: ldc 111
    //   227: aastore
    //   228: invokevirtual 115	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   231: iconst_3
    //   232: anewarray 4	java/lang/Object
    //   235: dup
    //   236: iconst_0
    //   237: aload 4
    //   239: aastore
    //   240: dup
    //   241: iconst_1
    //   242: aload 5
    //   244: aastore
    //   245: dup
    //   246: iconst_2
    //   247: aload_3
    //   248: aastore
    //   249: invokevirtual 121	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   252: checkcast 123	org/telegram/messenger/exoplayer2/Renderer
    //   255: astore_1
    //   256: aload 7
    //   258: iload 8
    //   260: aload_1
    //   261: invokevirtual 126	java/util/ArrayList:add	(ILjava/lang/Object;)V
    //   264: ldc 26
    //   266: ldc -114
    //   268: invokestatic 134	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   271: pop
    //   272: return
    //   273: astore_1
    //   274: return
    //   275: astore_1
    //   276: goto -146 -> 130
    //   279: astore_1
    //   280: new 144	java/lang/RuntimeException
    //   283: dup
    //   284: aload_1
    //   285: invokespecial 147	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   288: athrow
    //   289: astore_1
    //   290: iload 6
    //   292: istore 8
    //   294: goto -90 -> 204
    //   297: astore_1
    //   298: new 144	java/lang/RuntimeException
    //   301: dup
    //   302: aload_1
    //   303: invokespecial 147	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   306: athrow
    //   307: astore_1
    //   308: new 144	java/lang/RuntimeException
    //   311: dup
    //   312: aload_1
    //   313: invokespecial 147	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   316: athrow
    //   317: astore_1
    //   318: goto -10 -> 308
    //   321: astore_1
    //   322: return
    //   323: astore_1
    //   324: goto -26 -> 298
    //   327: astore_1
    //   328: iload 8
    //   330: istore 6
    //   332: goto -42 -> 290
    //   335: astore_1
    //   336: goto -56 -> 280
    //   339: astore_1
    //   340: iload 8
    //   342: istore 6
    //   344: goto -68 -> 276
    //   347: iload 8
    //   349: istore 6
    //   351: goto -299 -> 52
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	354	0	this	DefaultRenderersFactory
    //   0	354	1	paramContext	Context
    //   0	354	2	paramDrmSessionManager	DrmSessionManager<FrameworkMediaCrypto>
    //   0	354	3	paramArrayOfAudioProcessor	AudioProcessor[]
    //   0	354	4	paramHandler	Handler
    //   0	354	5	paramAudioRendererEventListener	AudioRendererEventListener
    //   0	354	6	paramInt	int
    //   0	354	7	paramArrayList	ArrayList<Renderer>
    //   38	310	8	i	int
    // Exception table:
    //   from	to	target	type
    //   256	272	273	java/lang/ClassNotFoundException
    //   52	104	275	java/lang/ClassNotFoundException
    //   52	104	279	java/lang/Exception
    //   130	182	289	java/lang/ClassNotFoundException
    //   130	182	297	java/lang/Exception
    //   204	256	307	java/lang/Exception
    //   256	272	317	java/lang/Exception
    //   204	256	321	java/lang/ClassNotFoundException
    //   188	204	323	java/lang/Exception
    //   188	204	327	java/lang/ClassNotFoundException
    //   110	126	335	java/lang/Exception
    //   110	126	339	java/lang/ClassNotFoundException
  }
  
  protected void buildMetadataRenderers(Context paramContext, MetadataOutput paramMetadataOutput, Looper paramLooper, int paramInt, ArrayList<Renderer> paramArrayList)
  {
    paramArrayList.add(new MetadataRenderer(paramMetadataOutput, paramLooper));
  }
  
  protected void buildMiscellaneousRenderers(Context paramContext, Handler paramHandler, int paramInt, ArrayList<Renderer> paramArrayList) {}
  
  protected void buildTextRenderers(Context paramContext, TextOutput paramTextOutput, Looper paramLooper, int paramInt, ArrayList<Renderer> paramArrayList)
  {
    paramArrayList.add(new TextRenderer(paramTextOutput, paramLooper));
  }
  
  /* Error */
  protected void buildVideoRenderers(Context paramContext, DrmSessionManager<FrameworkMediaCrypto> paramDrmSessionManager, long paramLong, Handler paramHandler, VideoRendererEventListener paramVideoRendererEventListener, int paramInt, ArrayList<Renderer> paramArrayList)
  {
    // Byte code:
    //   0: aload 8
    //   2: new 171	org/telegram/messenger/exoplayer2/video/MediaCodecVideoRenderer
    //   5: dup
    //   6: aload_1
    //   7: getstatic 78	org/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector:DEFAULT	Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector;
    //   10: lload_3
    //   11: aload_2
    //   12: iconst_0
    //   13: aload 5
    //   15: aload 6
    //   17: bipush 50
    //   19: invokespecial 174	org/telegram/messenger/exoplayer2/video/MediaCodecVideoRenderer:<init>	(Landroid/content/Context;Lorg/telegram/messenger/exoplayer2/mediacodec/MediaCodecSelector;JLorg/telegram/messenger/exoplayer2/drm/DrmSessionManager;ZLandroid/os/Handler;Lorg/telegram/messenger/exoplayer2/video/VideoRendererEventListener;I)V
    //   22: invokevirtual 93	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   25: pop
    //   26: iload 7
    //   28: ifne +4 -> 32
    //   31: return
    //   32: aload 8
    //   34: invokevirtual 97	java/util/ArrayList:size	()I
    //   37: istore 9
    //   39: iload 7
    //   41: iconst_2
    //   42: if_icmpne +127 -> 169
    //   45: iload 9
    //   47: iconst_1
    //   48: isub
    //   49: istore 7
    //   51: ldc -80
    //   53: invokestatic 105	java/lang/Class:forName	(Ljava/lang/String;)Ljava/lang/Class;
    //   56: iconst_5
    //   57: anewarray 101	java/lang/Class
    //   60: dup
    //   61: iconst_0
    //   62: getstatic 182	java/lang/Boolean:TYPE	Ljava/lang/Class;
    //   65: aastore
    //   66: dup
    //   67: iconst_1
    //   68: getstatic 185	java/lang/Long:TYPE	Ljava/lang/Class;
    //   71: aastore
    //   72: dup
    //   73: iconst_2
    //   74: ldc 107
    //   76: aastore
    //   77: dup
    //   78: iconst_3
    //   79: ldc -69
    //   81: aastore
    //   82: dup
    //   83: iconst_4
    //   84: getstatic 190	java/lang/Integer:TYPE	Ljava/lang/Class;
    //   87: aastore
    //   88: invokevirtual 115	java/lang/Class:getConstructor	([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
    //   91: iconst_5
    //   92: anewarray 4	java/lang/Object
    //   95: dup
    //   96: iconst_0
    //   97: iconst_1
    //   98: invokestatic 194	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
    //   101: aastore
    //   102: dup
    //   103: iconst_1
    //   104: lload_3
    //   105: invokestatic 197	java/lang/Long:valueOf	(J)Ljava/lang/Long;
    //   108: aastore
    //   109: dup
    //   110: iconst_2
    //   111: aload 5
    //   113: aastore
    //   114: dup
    //   115: iconst_3
    //   116: aload 6
    //   118: aastore
    //   119: dup
    //   120: iconst_4
    //   121: bipush 50
    //   123: invokestatic 200	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   126: aastore
    //   127: invokevirtual 121	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   130: checkcast 123	org/telegram/messenger/exoplayer2/Renderer
    //   133: astore_1
    //   134: aload 8
    //   136: iload 7
    //   138: aload_1
    //   139: invokevirtual 126	java/util/ArrayList:add	(ILjava/lang/Object;)V
    //   142: ldc 26
    //   144: ldc -54
    //   146: invokestatic 134	android/util/Log:i	(Ljava/lang/String;Ljava/lang/String;)I
    //   149: pop
    //   150: return
    //   151: astore_1
    //   152: return
    //   153: astore_1
    //   154: new 144	java/lang/RuntimeException
    //   157: dup
    //   158: aload_1
    //   159: invokespecial 147	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   162: athrow
    //   163: astore_1
    //   164: goto -10 -> 154
    //   167: astore_1
    //   168: return
    //   169: iload 9
    //   171: istore 7
    //   173: goto -122 -> 51
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	176	0	this	DefaultRenderersFactory
    //   0	176	1	paramContext	Context
    //   0	176	2	paramDrmSessionManager	DrmSessionManager<FrameworkMediaCrypto>
    //   0	176	3	paramLong	long
    //   0	176	5	paramHandler	Handler
    //   0	176	6	paramVideoRendererEventListener	VideoRendererEventListener
    //   0	176	7	paramInt	int
    //   0	176	8	paramArrayList	ArrayList<Renderer>
    //   37	133	9	i	int
    // Exception table:
    //   from	to	target	type
    //   134	150	151	java/lang/ClassNotFoundException
    //   51	134	153	java/lang/Exception
    //   134	150	163	java/lang/Exception
    //   51	134	167	java/lang/ClassNotFoundException
  }
  
  public Renderer[] createRenderers(Handler paramHandler, VideoRendererEventListener paramVideoRendererEventListener, AudioRendererEventListener paramAudioRendererEventListener, TextOutput paramTextOutput, MetadataOutput paramMetadataOutput)
  {
    ArrayList localArrayList = new ArrayList();
    buildVideoRenderers(this.context, this.drmSessionManager, this.allowedVideoJoiningTimeMs, paramHandler, paramVideoRendererEventListener, this.extensionRendererMode, localArrayList);
    buildAudioRenderers(this.context, this.drmSessionManager, buildAudioProcessors(), paramHandler, paramAudioRendererEventListener, this.extensionRendererMode, localArrayList);
    buildTextRenderers(this.context, paramTextOutput, paramHandler.getLooper(), this.extensionRendererMode, localArrayList);
    buildMetadataRenderers(this.context, paramMetadataOutput, paramHandler.getLooper(), this.extensionRendererMode, localArrayList);
    buildMiscellaneousRenderers(this.context, paramHandler, this.extensionRendererMode, localArrayList);
    return (Renderer[])localArrayList.toArray(new Renderer[localArrayList.size()]);
  }
  
  @Retention(RetentionPolicy.SOURCE)
  public static @interface ExtensionRendererMode {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/DefaultRenderersFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */