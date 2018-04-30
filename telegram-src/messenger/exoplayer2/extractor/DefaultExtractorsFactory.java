package org.telegram.messenger.exoplayer2.extractor;

import java.lang.reflect.Constructor;

public final class DefaultExtractorsFactory
  implements ExtractorsFactory
{
  private static final Constructor<? extends Extractor> FLAC_EXTRACTOR_CONSTRUCTOR;
  private int fragmentedMp4Flags;
  private int matroskaFlags;
  private int mp3Flags;
  private int mp4Flags;
  private int tsFlags;
  private int tsMode = 1;
  
  static
  {
    Object localObject = null;
    try
    {
      Constructor localConstructor = Class.forName("org.telegram.messenger.exoplayer2.ext.flac.FlacExtractor").asSubclass(Extractor.class).getConstructor(new Class[0]);
      localObject = localConstructor;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      for (;;) {}
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      for (;;) {}
    }
    FLAC_EXTRACTOR_CONSTRUCTOR = (Constructor)localObject;
  }
  
  /* Error */
  public Extractor[] createExtractors()
  {
    // Byte code:
    //   0: bipush 11
    //   2: istore_1
    //   3: aload_0
    //   4: monitorenter
    //   5: getstatic 42	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:FLAC_EXTRACTOR_CONSTRUCTOR	Ljava/lang/reflect/Constructor;
    //   8: ifnonnull +176 -> 184
    //   11: iload_1
    //   12: anewarray 32	org/telegram/messenger/exoplayer2/extractor/Extractor
    //   15: astore_2
    //   16: aload_2
    //   17: iconst_0
    //   18: new 54	org/telegram/messenger/exoplayer2/extractor/mkv/MatroskaExtractor
    //   21: dup
    //   22: aload_0
    //   23: getfield 56	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:matroskaFlags	I
    //   26: invokespecial 59	org/telegram/messenger/exoplayer2/extractor/mkv/MatroskaExtractor:<init>	(I)V
    //   29: aastore
    //   30: aload_2
    //   31: iconst_1
    //   32: new 61	org/telegram/messenger/exoplayer2/extractor/mp4/FragmentedMp4Extractor
    //   35: dup
    //   36: aload_0
    //   37: getfield 63	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:fragmentedMp4Flags	I
    //   40: invokespecial 64	org/telegram/messenger/exoplayer2/extractor/mp4/FragmentedMp4Extractor:<init>	(I)V
    //   43: aastore
    //   44: aload_2
    //   45: iconst_2
    //   46: new 66	org/telegram/messenger/exoplayer2/extractor/mp4/Mp4Extractor
    //   49: dup
    //   50: aload_0
    //   51: getfield 68	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:mp4Flags	I
    //   54: invokespecial 69	org/telegram/messenger/exoplayer2/extractor/mp4/Mp4Extractor:<init>	(I)V
    //   57: aastore
    //   58: aload_2
    //   59: iconst_3
    //   60: new 71	org/telegram/messenger/exoplayer2/extractor/mp3/Mp3Extractor
    //   63: dup
    //   64: aload_0
    //   65: getfield 73	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:mp3Flags	I
    //   68: invokespecial 74	org/telegram/messenger/exoplayer2/extractor/mp3/Mp3Extractor:<init>	(I)V
    //   71: aastore
    //   72: aload_2
    //   73: iconst_4
    //   74: new 76	org/telegram/messenger/exoplayer2/extractor/ts/AdtsExtractor
    //   77: dup
    //   78: invokespecial 77	org/telegram/messenger/exoplayer2/extractor/ts/AdtsExtractor:<init>	()V
    //   81: aastore
    //   82: aload_2
    //   83: iconst_5
    //   84: new 79	org/telegram/messenger/exoplayer2/extractor/ts/Ac3Extractor
    //   87: dup
    //   88: invokespecial 80	org/telegram/messenger/exoplayer2/extractor/ts/Ac3Extractor:<init>	()V
    //   91: aastore
    //   92: aload_2
    //   93: bipush 6
    //   95: new 82	org/telegram/messenger/exoplayer2/extractor/ts/TsExtractor
    //   98: dup
    //   99: aload_0
    //   100: getfield 48	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:tsMode	I
    //   103: aload_0
    //   104: getfield 84	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:tsFlags	I
    //   107: invokespecial 87	org/telegram/messenger/exoplayer2/extractor/ts/TsExtractor:<init>	(II)V
    //   110: aastore
    //   111: aload_2
    //   112: bipush 7
    //   114: new 89	org/telegram/messenger/exoplayer2/extractor/flv/FlvExtractor
    //   117: dup
    //   118: invokespecial 90	org/telegram/messenger/exoplayer2/extractor/flv/FlvExtractor:<init>	()V
    //   121: aastore
    //   122: aload_2
    //   123: bipush 8
    //   125: new 92	org/telegram/messenger/exoplayer2/extractor/ogg/OggExtractor
    //   128: dup
    //   129: invokespecial 93	org/telegram/messenger/exoplayer2/extractor/ogg/OggExtractor:<init>	()V
    //   132: aastore
    //   133: aload_2
    //   134: bipush 9
    //   136: new 95	org/telegram/messenger/exoplayer2/extractor/ts/PsExtractor
    //   139: dup
    //   140: invokespecial 96	org/telegram/messenger/exoplayer2/extractor/ts/PsExtractor:<init>	()V
    //   143: aastore
    //   144: aload_2
    //   145: bipush 10
    //   147: new 98	org/telegram/messenger/exoplayer2/extractor/wav/WavExtractor
    //   150: dup
    //   151: invokespecial 99	org/telegram/messenger/exoplayer2/extractor/wav/WavExtractor:<init>	()V
    //   154: aastore
    //   155: getstatic 42	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:FLAC_EXTRACTOR_CONSTRUCTOR	Ljava/lang/reflect/Constructor;
    //   158: astore_3
    //   159: aload_3
    //   160: ifnull +20 -> 180
    //   163: aload_2
    //   164: bipush 11
    //   166: getstatic 42	org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory:FLAC_EXTRACTOR_CONSTRUCTOR	Ljava/lang/reflect/Constructor;
    //   169: iconst_0
    //   170: anewarray 4	java/lang/Object
    //   173: invokevirtual 105	java/lang/reflect/Constructor:newInstance	([Ljava/lang/Object;)Ljava/lang/Object;
    //   176: checkcast 32	org/telegram/messenger/exoplayer2/extractor/Extractor
    //   179: aastore
    //   180: aload_0
    //   181: monitorexit
    //   182: aload_2
    //   183: areturn
    //   184: bipush 12
    //   186: istore_1
    //   187: goto -176 -> 11
    //   190: astore_2
    //   191: new 107	java/lang/IllegalStateException
    //   194: dup
    //   195: ldc 109
    //   197: aload_2
    //   198: invokespecial 112	java/lang/IllegalStateException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
    //   201: athrow
    //   202: astore_2
    //   203: aload_0
    //   204: monitorexit
    //   205: aload_2
    //   206: athrow
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	207	0	this	DefaultExtractorsFactory
    //   2	185	1	i	int
    //   15	168	2	arrayOfExtractor	Extractor[]
    //   190	8	2	localException	Exception
    //   202	4	2	localObject	Object
    //   158	2	3	localConstructor	Constructor
    // Exception table:
    //   from	to	target	type
    //   163	180	190	java/lang/Exception
    //   5	11	202	finally
    //   11	159	202	finally
    //   163	180	202	finally
    //   191	202	202	finally
  }
  
  public DefaultExtractorsFactory setFragmentedMp4ExtractorFlags(int paramInt)
  {
    try
    {
      this.fragmentedMp4Flags = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public DefaultExtractorsFactory setMatroskaExtractorFlags(int paramInt)
  {
    try
    {
      this.matroskaFlags = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public DefaultExtractorsFactory setMp3ExtractorFlags(int paramInt)
  {
    try
    {
      this.mp3Flags = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public DefaultExtractorsFactory setMp4ExtractorFlags(int paramInt)
  {
    try
    {
      this.mp4Flags = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public DefaultExtractorsFactory setTsExtractorFlags(int paramInt)
  {
    try
    {
      this.tsFlags = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
  
  public DefaultExtractorsFactory setTsExtractorMode(int paramInt)
  {
    try
    {
      this.tsMode = paramInt;
      return this;
    }
    finally
    {
      localObject = finally;
      throw ((Throwable)localObject);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/extractor/DefaultExtractorsFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */