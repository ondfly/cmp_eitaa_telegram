package org.telegram.messenger.exoplayer2.upstream.cache;

import android.util.SparseArray;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.AtomicFile;
import org.telegram.messenger.exoplayer2.util.ReusableBufferedOutputStream;
import org.telegram.messenger.exoplayer2.util.Util;

class CachedContentIndex
{
  public static final String FILE_NAME = "cached_content_index.exi";
  private static final int FLAG_ENCRYPTED_INDEX = 1;
  private static final String TAG = "CachedContentIndex";
  private static final int VERSION = 1;
  private final AtomicFile atomicFile;
  private ReusableBufferedOutputStream bufferedOutputStream;
  private boolean changed;
  private final Cipher cipher;
  private final boolean encrypt;
  private final SparseArray<String> idToKey;
  private final HashMap<String, CachedContent> keyToContent;
  private final SecretKeySpec secretKeySpec;
  
  public CachedContentIndex(File paramFile)
  {
    this(paramFile, null);
  }
  
  public CachedContentIndex(File paramFile, byte[] paramArrayOfByte) {}
  
  public CachedContentIndex(File paramFile, byte[] paramArrayOfByte, boolean paramBoolean)
  {
    this.encrypt = paramBoolean;
    if (paramArrayOfByte != null) {
      if (paramArrayOfByte.length == 16) {
        paramBoolean = bool1;
      }
    }
    for (;;)
    {
      Assertions.checkArgument(paramBoolean);
      try
      {
        this.cipher = getCipher();
        this.secretKeySpec = new SecretKeySpec(paramArrayOfByte, "AES");
        this.keyToContent = new HashMap();
        this.idToKey = new SparseArray();
        this.atomicFile = new AtomicFile(new File(paramFile, "cached_content_index.exi"));
        return;
        paramBoolean = false;
      }
      catch (NoSuchAlgorithmException paramFile)
      {
        throw new IllegalStateException(paramFile);
        if (!paramBoolean) {}
        for (paramBoolean = bool2;; paramBoolean = false)
        {
          Assertions.checkState(paramBoolean);
          this.cipher = null;
          this.secretKeySpec = null;
          break;
        }
      }
      catch (NoSuchPaddingException paramFile)
      {
        for (;;) {}
      }
    }
  }
  
  private void add(CachedContent paramCachedContent)
  {
    this.keyToContent.put(paramCachedContent.key, paramCachedContent);
    this.idToKey.put(paramCachedContent.id, paramCachedContent.key);
  }
  
  private CachedContent addNew(String paramString, long paramLong)
  {
    paramString = new CachedContent(getNewId(this.idToKey), paramString, paramLong);
    addNew(paramString);
    return paramString;
  }
  
  private static Cipher getCipher()
    throws NoSuchPaddingException, NoSuchAlgorithmException
  {
    if (Util.SDK_INT == 18) {
      try
      {
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "BC");
        return localCipher;
      }
      catch (Throwable localThrowable) {}
    }
    return Cipher.getInstance("AES/CBC/PKCS5PADDING");
  }
  
  public static int getNewId(SparseArray<String> paramSparseArray)
  {
    int k = paramSparseArray.size();
    int i;
    int j;
    if (k == 0)
    {
      i = 0;
      j = i;
      if (i < 0) {
        i = 0;
      }
    }
    for (;;)
    {
      j = i;
      if (i < k)
      {
        if (i != paramSparseArray.keyAt(i)) {
          j = i;
        }
      }
      else
      {
        return j;
        i = paramSparseArray.keyAt(k - 1) + 1;
        break;
      }
      i += 1;
    }
  }
  
  /* Error */
  private boolean readFile()
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 9
    //   3: aconst_null
    //   4: astore 10
    //   6: aconst_null
    //   7: astore 5
    //   9: aload 5
    //   11: astore 7
    //   13: aload 9
    //   15: astore 6
    //   17: aload 10
    //   19: astore 8
    //   21: new 171	java/io/BufferedInputStream
    //   24: dup
    //   25: aload_0
    //   26: getfield 92	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:atomicFile	Lorg/telegram/messenger/exoplayer2/util/AtomicFile;
    //   29: invokevirtual 175	org/telegram/messenger/exoplayer2/util/AtomicFile:openRead	()Ljava/io/InputStream;
    //   32: invokespecial 178	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
    //   35: astore 11
    //   37: aload 5
    //   39: astore 7
    //   41: aload 9
    //   43: astore 6
    //   45: aload 10
    //   47: astore 8
    //   49: new 180	java/io/DataInputStream
    //   52: dup
    //   53: aload 11
    //   55: invokespecial 181	java/io/DataInputStream:<init>	(Ljava/io/InputStream;)V
    //   58: astore 5
    //   60: aload 5
    //   62: invokevirtual 184	java/io/DataInputStream:readInt	()I
    //   65: istore_1
    //   66: iload_1
    //   67: iconst_1
    //   68: if_icmpeq +15 -> 83
    //   71: aload 5
    //   73: ifnull +8 -> 81
    //   76: aload 5
    //   78: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   81: iconst_0
    //   82: ireturn
    //   83: aload 5
    //   85: invokevirtual 184	java/io/DataInputStream:readInt	()I
    //   88: iconst_1
    //   89: iand
    //   90: ifeq +215 -> 305
    //   93: aload_0
    //   94: getfield 62	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:cipher	Ljavax/crypto/Cipher;
    //   97: astore 6
    //   99: aload 6
    //   101: ifnonnull +15 -> 116
    //   104: aload 5
    //   106: ifnull +8 -> 114
    //   109: aload 5
    //   111: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   114: iconst_0
    //   115: ireturn
    //   116: bipush 16
    //   118: newarray <illegal type>
    //   120: astore 6
    //   122: aload 5
    //   124: aload 6
    //   126: invokevirtual 192	java/io/DataInputStream:readFully	([B)V
    //   129: new 194	javax/crypto/spec/IvParameterSpec
    //   132: dup
    //   133: aload 6
    //   135: invokespecial 196	javax/crypto/spec/IvParameterSpec:<init>	([B)V
    //   138: astore 6
    //   140: aload_0
    //   141: getfield 62	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:cipher	Ljavax/crypto/Cipher;
    //   144: iconst_2
    //   145: aload_0
    //   146: getfield 71	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:secretKeySpec	Ljavax/crypto/spec/SecretKeySpec;
    //   149: aload 6
    //   151: invokevirtual 200	javax/crypto/Cipher:init	(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V
    //   154: new 180	java/io/DataInputStream
    //   157: dup
    //   158: new 202	javax/crypto/CipherInputStream
    //   161: dup
    //   162: aload 11
    //   164: aload_0
    //   165: getfield 62	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:cipher	Ljavax/crypto/Cipher;
    //   168: invokespecial 205	javax/crypto/CipherInputStream:<init>	(Ljava/io/InputStream;Ljavax/crypto/Cipher;)V
    //   171: invokespecial 181	java/io/DataInputStream:<init>	(Ljava/io/InputStream;)V
    //   174: astore 6
    //   176: aload 6
    //   178: astore 5
    //   180: aload 5
    //   182: astore 7
    //   184: aload 5
    //   186: astore 6
    //   188: aload 5
    //   190: astore 8
    //   192: aload 5
    //   194: invokevirtual 184	java/io/DataInputStream:readInt	()I
    //   197: istore_3
    //   198: iconst_0
    //   199: istore_1
    //   200: iconst_0
    //   201: istore_2
    //   202: iload_2
    //   203: iload_3
    //   204: if_icmpge +116 -> 320
    //   207: aload 5
    //   209: astore 7
    //   211: aload 5
    //   213: astore 6
    //   215: aload 5
    //   217: astore 8
    //   219: new 104	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent
    //   222: dup
    //   223: aload 5
    //   225: invokespecial 208	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent:<init>	(Ljava/io/DataInputStream;)V
    //   228: astore 9
    //   230: aload 5
    //   232: astore 7
    //   234: aload 5
    //   236: astore 6
    //   238: aload 5
    //   240: astore 8
    //   242: aload_0
    //   243: aload 9
    //   245: invokespecial 210	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:add	(Lorg/telegram/messenger/exoplayer2/upstream/cache/CachedContent;)V
    //   248: aload 5
    //   250: astore 7
    //   252: aload 5
    //   254: astore 6
    //   256: aload 5
    //   258: astore 8
    //   260: aload 9
    //   262: invokevirtual 213	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent:headerHashCode	()I
    //   265: istore 4
    //   267: iload_1
    //   268: iload 4
    //   270: iadd
    //   271: istore_1
    //   272: iload_2
    //   273: iconst_1
    //   274: iadd
    //   275: istore_2
    //   276: goto -74 -> 202
    //   279: astore 6
    //   281: new 94	java/lang/IllegalStateException
    //   284: dup
    //   285: aload 6
    //   287: invokespecial 97	java/lang/IllegalStateException:<init>	(Ljava/lang/Throwable;)V
    //   290: athrow
    //   291: astore 6
    //   293: aload 5
    //   295: ifnull -214 -> 81
    //   298: aload 5
    //   300: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   303: iconst_0
    //   304: ireturn
    //   305: aload_0
    //   306: getfield 50	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:encrypt	Z
    //   309: ifeq +8 -> 317
    //   312: aload_0
    //   313: iconst_1
    //   314: putfield 215	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:changed	Z
    //   317: goto -137 -> 180
    //   320: aload 5
    //   322: astore 7
    //   324: aload 5
    //   326: astore 6
    //   328: aload 5
    //   330: astore 8
    //   332: aload 5
    //   334: invokevirtual 184	java/io/DataInputStream:readInt	()I
    //   337: istore_2
    //   338: iload_2
    //   339: iload_1
    //   340: if_icmpeq +15 -> 355
    //   343: aload 5
    //   345: ifnull -264 -> 81
    //   348: aload 5
    //   350: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   353: iconst_0
    //   354: ireturn
    //   355: aload 5
    //   357: ifnull +8 -> 365
    //   360: aload 5
    //   362: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   365: iconst_1
    //   366: ireturn
    //   367: astore 5
    //   369: aload 7
    //   371: astore 6
    //   373: ldc 14
    //   375: ldc -39
    //   377: aload 5
    //   379: invokestatic 223	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   382: pop
    //   383: aload 7
    //   385: ifnull -304 -> 81
    //   388: aload 7
    //   390: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   393: iconst_0
    //   394: ireturn
    //   395: astore 5
    //   397: aload 6
    //   399: ifnull +8 -> 407
    //   402: aload 6
    //   404: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   407: aload 5
    //   409: athrow
    //   410: astore 6
    //   412: goto -131 -> 281
    //   415: astore 7
    //   417: aload 5
    //   419: astore 6
    //   421: aload 7
    //   423: astore 5
    //   425: goto -28 -> 397
    //   428: astore 6
    //   430: aload 5
    //   432: astore 7
    //   434: aload 6
    //   436: astore 5
    //   438: goto -69 -> 369
    //   441: astore 5
    //   443: aload 8
    //   445: astore 5
    //   447: goto -154 -> 293
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	450	0	this	CachedContentIndex
    //   65	276	1	i	int
    //   201	140	2	j	int
    //   197	8	3	k	int
    //   265	6	4	m	int
    //   7	354	5	localObject1	Object
    //   367	11	5	localIOException1	java.io.IOException
    //   395	23	5	localObject2	Object
    //   423	14	5	localObject3	Object
    //   441	1	5	localFileNotFoundException1	java.io.FileNotFoundException
    //   445	1	5	localObject4	Object
    //   15	240	6	localObject5	Object
    //   279	7	6	localInvalidKeyException	java.security.InvalidKeyException
    //   291	1	6	localFileNotFoundException2	java.io.FileNotFoundException
    //   326	77	6	localObject6	Object
    //   410	1	6	localInvalidAlgorithmParameterException	java.security.InvalidAlgorithmParameterException
    //   419	1	6	localObject7	Object
    //   428	7	6	localIOException2	java.io.IOException
    //   11	378	7	localObject8	Object
    //   415	7	7	localObject9	Object
    //   432	1	7	localObject10	Object
    //   19	425	8	localObject11	Object
    //   1	260	9	localCachedContent	CachedContent
    //   4	42	10	localObject12	Object
    //   35	128	11	localBufferedInputStream	java.io.BufferedInputStream
    // Exception table:
    //   from	to	target	type
    //   140	154	279	java/security/InvalidKeyException
    //   60	66	291	java/io/FileNotFoundException
    //   83	99	291	java/io/FileNotFoundException
    //   116	140	291	java/io/FileNotFoundException
    //   140	154	291	java/io/FileNotFoundException
    //   154	176	291	java/io/FileNotFoundException
    //   281	291	291	java/io/FileNotFoundException
    //   305	317	291	java/io/FileNotFoundException
    //   21	37	367	java/io/IOException
    //   49	60	367	java/io/IOException
    //   192	198	367	java/io/IOException
    //   219	230	367	java/io/IOException
    //   242	248	367	java/io/IOException
    //   260	267	367	java/io/IOException
    //   332	338	367	java/io/IOException
    //   21	37	395	finally
    //   49	60	395	finally
    //   192	198	395	finally
    //   219	230	395	finally
    //   242	248	395	finally
    //   260	267	395	finally
    //   332	338	395	finally
    //   373	383	395	finally
    //   140	154	410	java/security/InvalidAlgorithmParameterException
    //   60	66	415	finally
    //   83	99	415	finally
    //   116	140	415	finally
    //   140	154	415	finally
    //   154	176	415	finally
    //   281	291	415	finally
    //   305	317	415	finally
    //   60	66	428	java/io/IOException
    //   83	99	428	java/io/IOException
    //   116	140	428	java/io/IOException
    //   140	154	428	java/io/IOException
    //   154	176	428	java/io/IOException
    //   281	291	428	java/io/IOException
    //   305	317	428	java/io/IOException
    //   21	37	441	java/io/FileNotFoundException
    //   49	60	441	java/io/FileNotFoundException
    //   192	198	441	java/io/FileNotFoundException
    //   219	230	441	java/io/FileNotFoundException
    //   242	248	441	java/io/FileNotFoundException
    //   260	267	441	java/io/FileNotFoundException
    //   332	338	441	java/io/FileNotFoundException
  }
  
  /* Error */
  private void writeFile()
    throws Cache.CacheException
  {
    // Byte code:
    //   0: iconst_1
    //   1: istore_1
    //   2: aconst_null
    //   3: astore 5
    //   5: aconst_null
    //   6: astore_3
    //   7: aload_3
    //   8: astore 4
    //   10: aload 5
    //   12: astore_2
    //   13: aload_0
    //   14: getfield 92	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:atomicFile	Lorg/telegram/messenger/exoplayer2/util/AtomicFile;
    //   17: invokevirtual 230	org/telegram/messenger/exoplayer2/util/AtomicFile:startWrite	()Ljava/io/OutputStream;
    //   20: astore 6
    //   22: aload_3
    //   23: astore 4
    //   25: aload 5
    //   27: astore_2
    //   28: aload_0
    //   29: getfield 232	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:bufferedOutputStream	Lorg/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream;
    //   32: ifnonnull +232 -> 264
    //   35: aload_3
    //   36: astore 4
    //   38: aload 5
    //   40: astore_2
    //   41: aload_0
    //   42: new 234	org/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream
    //   45: dup
    //   46: aload 6
    //   48: invokespecial 237	org/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream:<init>	(Ljava/io/OutputStream;)V
    //   51: putfield 232	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:bufferedOutputStream	Lorg/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream;
    //   54: aload_3
    //   55: astore 4
    //   57: aload 5
    //   59: astore_2
    //   60: new 239	java/io/DataOutputStream
    //   63: dup
    //   64: aload_0
    //   65: getfield 232	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:bufferedOutputStream	Lorg/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream;
    //   68: invokespecial 240	java/io/DataOutputStream:<init>	(Ljava/io/OutputStream;)V
    //   71: astore_3
    //   72: aload_3
    //   73: iconst_1
    //   74: invokevirtual 244	java/io/DataOutputStream:writeInt	(I)V
    //   77: aload_0
    //   78: getfield 50	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:encrypt	Z
    //   81: ifeq +221 -> 302
    //   84: aload_3
    //   85: iload_1
    //   86: invokevirtual 244	java/io/DataOutputStream:writeInt	(I)V
    //   89: aload_0
    //   90: getfield 50	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:encrypt	Z
    //   93: ifeq +276 -> 369
    //   96: bipush 16
    //   98: newarray <illegal type>
    //   100: astore_2
    //   101: new 246	java/util/Random
    //   104: dup
    //   105: invokespecial 247	java/util/Random:<init>	()V
    //   108: aload_2
    //   109: invokevirtual 250	java/util/Random:nextBytes	([B)V
    //   112: aload_3
    //   113: aload_2
    //   114: invokevirtual 253	java/io/DataOutputStream:write	([B)V
    //   117: new 194	javax/crypto/spec/IvParameterSpec
    //   120: dup
    //   121: aload_2
    //   122: invokespecial 196	javax/crypto/spec/IvParameterSpec:<init>	([B)V
    //   125: astore_2
    //   126: aload_0
    //   127: getfield 62	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:cipher	Ljavax/crypto/Cipher;
    //   130: iconst_1
    //   131: aload_0
    //   132: getfield 71	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:secretKeySpec	Ljavax/crypto/spec/SecretKeySpec;
    //   135: aload_2
    //   136: invokevirtual 200	javax/crypto/Cipher:init	(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V
    //   139: aload_3
    //   140: invokevirtual 256	java/io/DataOutputStream:flush	()V
    //   143: new 239	java/io/DataOutputStream
    //   146: dup
    //   147: new 258	javax/crypto/CipherOutputStream
    //   150: dup
    //   151: aload_0
    //   152: getfield 232	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:bufferedOutputStream	Lorg/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream;
    //   155: aload_0
    //   156: getfield 62	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:cipher	Ljavax/crypto/Cipher;
    //   159: invokespecial 261	javax/crypto/CipherOutputStream:<init>	(Ljava/io/OutputStream;Ljavax/crypto/Cipher;)V
    //   162: invokespecial 240	java/io/DataOutputStream:<init>	(Ljava/io/OutputStream;)V
    //   165: astore_2
    //   166: aload_2
    //   167: astore_3
    //   168: aload_3
    //   169: astore 4
    //   171: aload_3
    //   172: astore_2
    //   173: aload_3
    //   174: aload_0
    //   175: getfield 76	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:keyToContent	Ljava/util/HashMap;
    //   178: invokevirtual 262	java/util/HashMap:size	()I
    //   181: invokevirtual 244	java/io/DataOutputStream:writeInt	(I)V
    //   184: iconst_0
    //   185: istore_1
    //   186: aload_3
    //   187: astore 4
    //   189: aload_3
    //   190: astore_2
    //   191: aload_0
    //   192: getfield 76	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:keyToContent	Ljava/util/HashMap;
    //   195: invokevirtual 266	java/util/HashMap:values	()Ljava/util/Collection;
    //   198: invokeinterface 272 1 0
    //   203: astore 5
    //   205: aload_3
    //   206: astore 4
    //   208: aload_3
    //   209: astore_2
    //   210: aload 5
    //   212: invokeinterface 277 1 0
    //   217: ifeq +110 -> 327
    //   220: aload_3
    //   221: astore 4
    //   223: aload_3
    //   224: astore_2
    //   225: aload 5
    //   227: invokeinterface 281 1 0
    //   232: checkcast 104	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent
    //   235: astore 6
    //   237: aload_3
    //   238: astore 4
    //   240: aload_3
    //   241: astore_2
    //   242: aload 6
    //   244: aload_3
    //   245: invokevirtual 285	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent:writeToStream	(Ljava/io/DataOutputStream;)V
    //   248: aload_3
    //   249: astore 4
    //   251: aload_3
    //   252: astore_2
    //   253: iload_1
    //   254: aload 6
    //   256: invokevirtual 213	org/telegram/messenger/exoplayer2/upstream/cache/CachedContent:headerHashCode	()I
    //   259: iadd
    //   260: istore_1
    //   261: goto -56 -> 205
    //   264: aload_3
    //   265: astore 4
    //   267: aload 5
    //   269: astore_2
    //   270: aload_0
    //   271: getfield 232	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:bufferedOutputStream	Lorg/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream;
    //   274: aload 6
    //   276: invokevirtual 288	org/telegram/messenger/exoplayer2/util/ReusableBufferedOutputStream:reset	(Ljava/io/OutputStream;)V
    //   279: goto -225 -> 54
    //   282: astore_3
    //   283: aload 4
    //   285: astore_2
    //   286: new 226	org/telegram/messenger/exoplayer2/upstream/cache/Cache$CacheException
    //   289: dup
    //   290: aload_3
    //   291: invokespecial 289	org/telegram/messenger/exoplayer2/upstream/cache/Cache$CacheException:<init>	(Ljava/lang/Throwable;)V
    //   294: athrow
    //   295: astore_3
    //   296: aload_2
    //   297: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   300: aload_3
    //   301: athrow
    //   302: iconst_0
    //   303: istore_1
    //   304: goto -220 -> 84
    //   307: astore_2
    //   308: new 94	java/lang/IllegalStateException
    //   311: dup
    //   312: aload_2
    //   313: invokespecial 97	java/lang/IllegalStateException:<init>	(Ljava/lang/Throwable;)V
    //   316: athrow
    //   317: astore 4
    //   319: aload_3
    //   320: astore_2
    //   321: aload 4
    //   323: astore_3
    //   324: goto -38 -> 286
    //   327: aload_3
    //   328: astore 4
    //   330: aload_3
    //   331: astore_2
    //   332: aload_3
    //   333: iload_1
    //   334: invokevirtual 244	java/io/DataOutputStream:writeInt	(I)V
    //   337: aload_3
    //   338: astore 4
    //   340: aload_3
    //   341: astore_2
    //   342: aload_0
    //   343: getfield 92	org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex:atomicFile	Lorg/telegram/messenger/exoplayer2/util/AtomicFile;
    //   346: aload_3
    //   347: invokevirtual 292	org/telegram/messenger/exoplayer2/util/AtomicFile:endWrite	(Ljava/io/OutputStream;)V
    //   350: aconst_null
    //   351: invokestatic 188	org/telegram/messenger/exoplayer2/util/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   354: return
    //   355: astore_2
    //   356: goto -48 -> 308
    //   359: astore 4
    //   361: aload_3
    //   362: astore_2
    //   363: aload 4
    //   365: astore_3
    //   366: goto -70 -> 296
    //   369: goto -201 -> 168
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	372	0	this	CachedContentIndex
    //   1	333	1	i	int
    //   12	285	2	localObject1	Object
    //   307	6	2	localInvalidKeyException	java.security.InvalidKeyException
    //   320	22	2	localObject2	Object
    //   355	1	2	localInvalidAlgorithmParameterException	java.security.InvalidAlgorithmParameterException
    //   362	1	2	localObject3	Object
    //   6	259	3	localObject4	Object
    //   282	9	3	localIOException1	java.io.IOException
    //   295	25	3	localObject5	Object
    //   323	43	3	localObject6	Object
    //   8	276	4	localObject7	Object
    //   317	5	4	localIOException2	java.io.IOException
    //   328	11	4	localObject8	Object
    //   359	5	4	localObject9	Object
    //   3	265	5	localIterator	java.util.Iterator
    //   20	255	6	localObject10	Object
    // Exception table:
    //   from	to	target	type
    //   13	22	282	java/io/IOException
    //   28	35	282	java/io/IOException
    //   41	54	282	java/io/IOException
    //   60	72	282	java/io/IOException
    //   173	184	282	java/io/IOException
    //   191	205	282	java/io/IOException
    //   210	220	282	java/io/IOException
    //   225	237	282	java/io/IOException
    //   242	248	282	java/io/IOException
    //   253	261	282	java/io/IOException
    //   270	279	282	java/io/IOException
    //   332	337	282	java/io/IOException
    //   342	350	282	java/io/IOException
    //   13	22	295	finally
    //   28	35	295	finally
    //   41	54	295	finally
    //   60	72	295	finally
    //   173	184	295	finally
    //   191	205	295	finally
    //   210	220	295	finally
    //   225	237	295	finally
    //   242	248	295	finally
    //   253	261	295	finally
    //   270	279	295	finally
    //   286	295	295	finally
    //   332	337	295	finally
    //   342	350	295	finally
    //   126	139	307	java/security/InvalidKeyException
    //   72	84	317	java/io/IOException
    //   84	126	317	java/io/IOException
    //   126	139	317	java/io/IOException
    //   139	166	317	java/io/IOException
    //   308	317	317	java/io/IOException
    //   126	139	355	java/security/InvalidAlgorithmParameterException
    //   72	84	359	finally
    //   84	126	359	finally
    //   126	139	359	finally
    //   139	166	359	finally
    //   308	317	359	finally
  }
  
  void addNew(CachedContent paramCachedContent)
  {
    add(paramCachedContent);
    this.changed = true;
  }
  
  public int assignIdForKey(String paramString)
  {
    return getOrAdd(paramString).id;
  }
  
  public CachedContent get(String paramString)
  {
    return (CachedContent)this.keyToContent.get(paramString);
  }
  
  public Collection<CachedContent> getAll()
  {
    return this.keyToContent.values();
  }
  
  public long getContentLength(String paramString)
  {
    paramString = get(paramString);
    if (paramString == null) {
      return -1L;
    }
    return paramString.getLength();
  }
  
  public String getKeyForId(int paramInt)
  {
    return (String)this.idToKey.get(paramInt);
  }
  
  public Set<String> getKeys()
  {
    return this.keyToContent.keySet();
  }
  
  public CachedContent getOrAdd(String paramString)
  {
    CachedContent localCachedContent2 = (CachedContent)this.keyToContent.get(paramString);
    CachedContent localCachedContent1 = localCachedContent2;
    if (localCachedContent2 == null) {
      localCachedContent1 = addNew(paramString, -1L);
    }
    return localCachedContent1;
  }
  
  public void load()
  {
    if (!this.changed) {}
    for (boolean bool = true;; bool = false)
    {
      Assertions.checkState(bool);
      if (!readFile())
      {
        this.atomicFile.delete();
        this.keyToContent.clear();
        this.idToKey.clear();
      }
      return;
    }
  }
  
  public void maybeRemove(String paramString)
  {
    CachedContent localCachedContent = (CachedContent)this.keyToContent.get(paramString);
    if ((localCachedContent != null) && (localCachedContent.isEmpty()) && (!localCachedContent.isLocked()))
    {
      this.keyToContent.remove(paramString);
      this.idToKey.remove(localCachedContent.id);
      this.changed = true;
    }
  }
  
  public void removeEmpty()
  {
    String[] arrayOfString = new String[this.keyToContent.size()];
    this.keyToContent.keySet().toArray(arrayOfString);
    int j = arrayOfString.length;
    int i = 0;
    while (i < j)
    {
      maybeRemove(arrayOfString[i]);
      i += 1;
    }
  }
  
  public void setContentLength(String paramString, long paramLong)
  {
    CachedContent localCachedContent = get(paramString);
    if (localCachedContent != null)
    {
      if (localCachedContent.getLength() != paramLong)
      {
        localCachedContent.setLength(paramLong);
        this.changed = true;
      }
      return;
    }
    addNew(paramString, paramLong);
  }
  
  public void store()
    throws Cache.CacheException
  {
    if (!this.changed) {
      return;
    }
    writeFile();
    this.changed = false;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/upstream/cache/CachedContentIndex.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */