package org.telegram.messenger;

import java.util.Locale;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;

public class VideoEditedInfo
{
  public int bitrate;
  public TLRPC.InputEncryptedFile encryptedFile;
  public long endTime;
  public long estimatedDuration;
  public long estimatedSize;
  public TLRPC.InputFile file;
  public int framerate = 24;
  public byte[] iv;
  public byte[] key;
  public boolean muted;
  public int originalHeight;
  public String originalPath;
  public int originalWidth;
  public int resultHeight;
  public int resultWidth;
  public int rotationValue;
  public boolean roundVideo;
  public long startTime;
  
  public String getString()
  {
    return String.format(Locale.US, "-1_%d_%d_%d_%d_%d_%d_%d_%d_%d_%s", new Object[] { Long.valueOf(this.startTime), Long.valueOf(this.endTime), Integer.valueOf(this.rotationValue), Integer.valueOf(this.originalWidth), Integer.valueOf(this.originalHeight), Integer.valueOf(this.bitrate), Integer.valueOf(this.resultWidth), Integer.valueOf(this.resultHeight), Integer.valueOf(this.framerate), this.originalPath });
  }
  
  public boolean needConvert()
  {
    return (!this.roundVideo) || ((this.roundVideo) && ((this.startTime > 0L) || ((this.endTime != -1L) && (this.endTime != this.estimatedDuration))));
  }
  
  /* Error */
  public boolean parseString(String paramString)
  {
    // Byte code:
    //   0: aload_1
    //   1: invokevirtual 97	java/lang/String:length	()I
    //   4: bipush 6
    //   6: if_icmpge +5 -> 11
    //   9: iconst_0
    //   10: ireturn
    //   11: aload_1
    //   12: ldc 99
    //   14: invokevirtual 103	java/lang/String:split	(Ljava/lang/String;)[Ljava/lang/String;
    //   17: astore_1
    //   18: aload_1
    //   19: arraylength
    //   20: bipush 10
    //   22: if_icmplt +200 -> 222
    //   25: aload_0
    //   26: aload_1
    //   27: iconst_1
    //   28: aaload
    //   29: invokestatic 107	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   32: putfield 48	org/telegram/messenger/VideoEditedInfo:startTime	J
    //   35: aload_0
    //   36: aload_1
    //   37: iconst_2
    //   38: aaload
    //   39: invokestatic 107	java/lang/Long:parseLong	(Ljava/lang/String;)J
    //   42: putfield 56	org/telegram/messenger/VideoEditedInfo:endTime	J
    //   45: aload_0
    //   46: aload_1
    //   47: iconst_3
    //   48: aaload
    //   49: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   52: putfield 58	org/telegram/messenger/VideoEditedInfo:rotationValue	I
    //   55: aload_0
    //   56: aload_1
    //   57: iconst_4
    //   58: aaload
    //   59: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   62: putfield 65	org/telegram/messenger/VideoEditedInfo:originalWidth	I
    //   65: aload_0
    //   66: aload_1
    //   67: iconst_5
    //   68: aaload
    //   69: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   72: putfield 67	org/telegram/messenger/VideoEditedInfo:originalHeight	I
    //   75: aload_0
    //   76: aload_1
    //   77: bipush 6
    //   79: aaload
    //   80: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   83: putfield 69	org/telegram/messenger/VideoEditedInfo:bitrate	I
    //   86: aload_0
    //   87: aload_1
    //   88: bipush 7
    //   90: aaload
    //   91: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   94: putfield 71	org/telegram/messenger/VideoEditedInfo:resultWidth	I
    //   97: aload_0
    //   98: aload_1
    //   99: bipush 8
    //   101: aaload
    //   102: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   105: putfield 73	org/telegram/messenger/VideoEditedInfo:resultHeight	I
    //   108: aload_1
    //   109: arraylength
    //   110: istore_2
    //   111: iload_2
    //   112: bipush 11
    //   114: if_icmplt +14 -> 128
    //   117: aload_0
    //   118: aload_1
    //   119: bipush 9
    //   121: aaload
    //   122: invokestatic 111	java/lang/Integer:parseInt	(Ljava/lang/String;)I
    //   125: putfield 35	org/telegram/messenger/VideoEditedInfo:framerate	I
    //   128: aload_0
    //   129: getfield 35	org/telegram/messenger/VideoEditedInfo:framerate	I
    //   132: ifle +13 -> 145
    //   135: aload_0
    //   136: getfield 35	org/telegram/messenger/VideoEditedInfo:framerate	I
    //   139: sipush 400
    //   142: if_icmple +96 -> 238
    //   145: bipush 9
    //   147: istore_2
    //   148: aload_0
    //   149: bipush 25
    //   151: putfield 35	org/telegram/messenger/VideoEditedInfo:framerate	I
    //   154: goto +74 -> 228
    //   157: iload_2
    //   158: aload_1
    //   159: arraylength
    //   160: if_icmpge +62 -> 222
    //   163: aload_0
    //   164: getfield 75	org/telegram/messenger/VideoEditedInfo:originalPath	Ljava/lang/String;
    //   167: ifnonnull +13 -> 180
    //   170: aload_0
    //   171: aload_1
    //   172: iload_2
    //   173: aaload
    //   174: putfield 75	org/telegram/messenger/VideoEditedInfo:originalPath	Ljava/lang/String;
    //   177: goto +54 -> 231
    //   180: aload_0
    //   181: new 113	java/lang/StringBuilder
    //   184: dup
    //   185: invokespecial 114	java/lang/StringBuilder:<init>	()V
    //   188: aload_0
    //   189: getfield 75	org/telegram/messenger/VideoEditedInfo:originalPath	Ljava/lang/String;
    //   192: invokevirtual 118	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   195: ldc 99
    //   197: invokevirtual 118	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   200: aload_1
    //   201: iload_2
    //   202: aaload
    //   203: invokevirtual 118	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   206: invokevirtual 121	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   209: putfield 75	org/telegram/messenger/VideoEditedInfo:originalPath	Ljava/lang/String;
    //   212: goto +19 -> 231
    //   215: astore_1
    //   216: aload_1
    //   217: invokestatic 127	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   220: iconst_0
    //   221: ireturn
    //   222: iconst_1
    //   223: ireturn
    //   224: astore_3
    //   225: goto -97 -> 128
    //   228: goto -71 -> 157
    //   231: iload_2
    //   232: iconst_1
    //   233: iadd
    //   234: istore_2
    //   235: goto -78 -> 157
    //   238: bipush 10
    //   240: istore_2
    //   241: goto -13 -> 228
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	244	0	this	VideoEditedInfo
    //   0	244	1	paramString	String
    //   110	131	2	i	int
    //   224	1	3	localException	Exception
    // Exception table:
    //   from	to	target	type
    //   11	111	215	java/lang/Exception
    //   128	145	215	java/lang/Exception
    //   148	154	215	java/lang/Exception
    //   157	177	215	java/lang/Exception
    //   180	212	215	java/lang/Exception
    //   117	128	224	java/lang/Exception
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/VideoEditedInfo.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */