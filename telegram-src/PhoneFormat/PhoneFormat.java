package org.telegram.PhoneFormat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.telegram.messenger.FileLog;

public class PhoneFormat
{
  private static volatile PhoneFormat Instance = null;
  public ByteBuffer buffer;
  public HashMap<String, ArrayList<String>> callingCodeCountries;
  public HashMap<String, CallingCodeInfo> callingCodeData;
  public HashMap<String, Integer> callingCodeOffsets;
  public HashMap<String, String> countryCallingCode;
  public byte[] data;
  public String defaultCallingCode;
  public String defaultCountry;
  private boolean initialzed = false;
  
  public PhoneFormat()
  {
    init(null);
  }
  
  public PhoneFormat(String paramString)
  {
    init(paramString);
  }
  
  public static PhoneFormat getInstance()
  {
    Object localObject1 = Instance;
    if (localObject1 == null)
    {
      for (;;)
      {
        try
        {
          PhoneFormat localPhoneFormat2 = Instance;
          localObject1 = localPhoneFormat2;
          if (localPhoneFormat2 == null) {
            localObject1 = new PhoneFormat();
          }
        }
        finally
        {
          continue;
        }
        try
        {
          Instance = (PhoneFormat)localObject1;
          return (PhoneFormat)localObject1;
        }
        finally {}
      }
      throw ((Throwable)localObject1);
    }
    return localPhoneFormat1;
  }
  
  public static String strip(String paramString)
  {
    paramString = new StringBuilder(paramString);
    int i = paramString.length() - 1;
    while (i >= 0)
    {
      if (!"0123456789+*#".contains(paramString.substring(i, i + 1))) {
        paramString.deleteCharAt(i);
      }
      i -= 1;
    }
    return paramString.toString();
  }
  
  public static String stripExceptNumbers(String paramString)
  {
    return stripExceptNumbers(paramString, false);
  }
  
  public static String stripExceptNumbers(String paramString, boolean paramBoolean)
  {
    if (paramString == null) {
      return null;
    }
    StringBuilder localStringBuilder = new StringBuilder(paramString);
    paramString = "0123456789";
    if (paramBoolean) {
      paramString = "0123456789" + "+";
    }
    int i = localStringBuilder.length() - 1;
    while (i >= 0)
    {
      if (!paramString.contains(localStringBuilder.substring(i, i + 1))) {
        localStringBuilder.deleteCharAt(i);
      }
      i -= 1;
    }
    return localStringBuilder.toString();
  }
  
  public String callingCodeForCountryCode(String paramString)
  {
    return (String)this.countryCallingCode.get(paramString.toLowerCase());
  }
  
  public CallingCodeInfo callingCodeInfo(String paramString)
  {
    Object localObject2 = (CallingCodeInfo)this.callingCodeData.get(paramString);
    Object localObject1 = localObject2;
    if (localObject2 == null)
    {
      Object localObject3 = (Integer)this.callingCodeOffsets.get(paramString);
      localObject1 = localObject2;
      if (localObject3 != null)
      {
        localObject2 = this.data;
        int m = ((Integer)localObject3).intValue();
        localObject1 = new CallingCodeInfo();
        ((CallingCodeInfo)localObject1).callingCode = paramString;
        ((CallingCodeInfo)localObject1).countries = ((ArrayList)this.callingCodeCountries.get(paramString));
        this.callingCodeData.put(paramString, localObject1);
        int n = value16(m);
        int i = m + 2 + 2;
        int i1 = value16(i);
        i = i + 2 + 2;
        int i2 = value16(i);
        i = i + 2 + 2;
        paramString = new ArrayList(5);
        for (;;)
        {
          localObject3 = valueString(i);
          if (((String)localObject3).length() == 0) {
            break;
          }
          paramString.add(localObject3);
          i += ((String)localObject3).length() + 1;
        }
        ((CallingCodeInfo)localObject1).trunkPrefixes = paramString;
        i += 1;
        paramString = new ArrayList(5);
        for (;;)
        {
          localObject3 = valueString(i);
          if (((String)localObject3).length() == 0) {
            break;
          }
          paramString.add(localObject3);
          i += ((String)localObject3).length() + 1;
        }
        ((CallingCodeInfo)localObject1).intlPrefixes = paramString;
        paramString = new ArrayList(i2);
        i = m + n;
        int j = 0;
        while (j < i2)
        {
          localObject3 = new RuleSet();
          ((RuleSet)localObject3).matchLen = value16(i);
          i += 2;
          int i3 = value16(i);
          i += 2;
          ArrayList localArrayList = new ArrayList(i3);
          int k = 0;
          while (k < i3)
          {
            PhoneRule localPhoneRule = new PhoneRule();
            localPhoneRule.minVal = value32(i);
            i += 4;
            localPhoneRule.maxVal = value32(i);
            int i4 = i + 4;
            i = i4 + 1;
            localPhoneRule.byte8 = localObject2[i4];
            i4 = i + 1;
            localPhoneRule.maxLen = localObject2[i];
            i = i4 + 1;
            localPhoneRule.otherFlag = localObject2[i4];
            i4 = i + 1;
            localPhoneRule.prefixLen = localObject2[i];
            i = i4 + 1;
            localPhoneRule.flag12 = localObject2[i4];
            i4 = i + 1;
            localPhoneRule.flag13 = localObject2[i];
            int i5 = value16(i4);
            i = i4 + 2;
            localPhoneRule.format = valueString(m + n + i1 + i5);
            i4 = localPhoneRule.format.indexOf("[[");
            if (i4 != -1)
            {
              i5 = localPhoneRule.format.indexOf("]]");
              localPhoneRule.format = String.format("%s%s", new Object[] { localPhoneRule.format.substring(0, i4), localPhoneRule.format.substring(i5 + 2) });
            }
            localArrayList.add(localPhoneRule);
            if (localPhoneRule.hasIntlPrefix) {
              ((RuleSet)localObject3).hasRuleWithIntlPrefix = true;
            }
            if (localPhoneRule.hasTrunkPrefix) {
              ((RuleSet)localObject3).hasRuleWithTrunkPrefix = true;
            }
            k += 1;
          }
          ((RuleSet)localObject3).rules = localArrayList;
          paramString.add(localObject3);
          j += 1;
        }
        ((CallingCodeInfo)localObject1).ruleSets = paramString;
      }
    }
    return (CallingCodeInfo)localObject1;
  }
  
  public ArrayList countriesForCallingCode(String paramString)
  {
    String str = paramString;
    if (paramString.startsWith("+")) {
      str = paramString.substring(1);
    }
    return (ArrayList)this.callingCodeCountries.get(str);
  }
  
  public String defaultCallingCode()
  {
    return callingCodeForCountryCode(this.defaultCountry);
  }
  
  public CallingCodeInfo findCallingCodeInfo(String paramString)
  {
    CallingCodeInfo localCallingCodeInfo1 = null;
    int i = 0;
    for (;;)
    {
      CallingCodeInfo localCallingCodeInfo2 = localCallingCodeInfo1;
      if (i < 3)
      {
        localCallingCodeInfo2 = localCallingCodeInfo1;
        if (i < paramString.length())
        {
          localCallingCodeInfo1 = callingCodeInfo(paramString.substring(0, i + 1));
          if (localCallingCodeInfo1 == null) {
            break label46;
          }
          localCallingCodeInfo2 = localCallingCodeInfo1;
        }
      }
      return localCallingCodeInfo2;
      label46:
      i += 1;
    }
  }
  
  public String format(String paramString)
  {
    if (!this.initialzed) {}
    for (;;)
    {
      return paramString;
      try
      {
        Object localObject1 = strip(paramString);
        Object localObject2;
        if (((String)localObject1).startsWith("+"))
        {
          localObject1 = ((String)localObject1).substring(1);
          localObject2 = findCallingCodeInfo((String)localObject1);
          if (localObject2 != null)
          {
            localObject1 = ((CallingCodeInfo)localObject2).format((String)localObject1);
            return "+" + (String)localObject1;
          }
        }
        else
        {
          localObject2 = callingCodeInfo(this.defaultCallingCode);
          if (localObject2 != null)
          {
            String str = ((CallingCodeInfo)localObject2).matchingAccessCode((String)localObject1);
            if (str != null)
            {
              localObject2 = ((String)localObject1).substring(str.length());
              localObject1 = localObject2;
              CallingCodeInfo localCallingCodeInfo = findCallingCodeInfo((String)localObject2);
              if (localCallingCodeInfo != null) {
                localObject1 = localCallingCodeInfo.format((String)localObject2);
              }
              if (((String)localObject1).length() == 0) {
                return str;
              }
              return String.format("%s %s", new Object[] { str, localObject1 });
            }
            localObject1 = ((CallingCodeInfo)localObject2).format((String)localObject1);
            return (String)localObject1;
          }
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
      }
    }
    return paramString;
  }
  
  /* Error */
  public void init(String paramString)
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore 6
    //   3: aconst_null
    //   4: astore 4
    //   6: aconst_null
    //   7: astore 7
    //   9: aconst_null
    //   10: astore 8
    //   12: aload 7
    //   14: astore 5
    //   16: getstatic 264	org/telegram/messenger/ApplicationLoader:applicationContext	Landroid/content/Context;
    //   19: invokevirtual 270	android/content/Context:getAssets	()Landroid/content/res/AssetManager;
    //   22: ldc_w 272
    //   25: invokevirtual 278	android/content/res/AssetManager:open	(Ljava/lang/String;)Ljava/io/InputStream;
    //   28: astore_3
    //   29: aload 7
    //   31: astore 5
    //   33: aload_3
    //   34: astore 4
    //   36: aload_3
    //   37: astore 6
    //   39: new 280	java/io/ByteArrayOutputStream
    //   42: dup
    //   43: invokespecial 281	java/io/ByteArrayOutputStream:<init>	()V
    //   46: astore 7
    //   48: sipush 1024
    //   51: newarray <illegal type>
    //   53: astore 4
    //   55: aload_3
    //   56: aload 4
    //   58: iconst_0
    //   59: sipush 1024
    //   62: invokevirtual 287	java/io/InputStream:read	([BII)I
    //   65: istore_2
    //   66: iload_2
    //   67: iconst_m1
    //   68: if_icmpeq +52 -> 120
    //   71: aload 7
    //   73: aload 4
    //   75: iconst_0
    //   76: iload_2
    //   77: invokevirtual 291	java/io/ByteArrayOutputStream:write	([BII)V
    //   80: goto -25 -> 55
    //   83: astore 4
    //   85: aload 7
    //   87: astore_1
    //   88: aload 4
    //   90: astore 7
    //   92: aload_1
    //   93: astore 5
    //   95: aload_3
    //   96: astore 4
    //   98: aload 7
    //   100: invokevirtual 294	java/lang/Exception:printStackTrace	()V
    //   103: aload_1
    //   104: ifnull +7 -> 111
    //   107: aload_1
    //   108: invokevirtual 297	java/io/ByteArrayOutputStream:close	()V
    //   111: aload_3
    //   112: ifnull +7 -> 119
    //   115: aload_3
    //   116: invokevirtual 298	java/io/InputStream:close	()V
    //   119: return
    //   120: aload_0
    //   121: aload 7
    //   123: invokevirtual 302	java/io/ByteArrayOutputStream:toByteArray	()[B
    //   126: putfield 108	org/telegram/PhoneFormat/PhoneFormat:data	[B
    //   129: aload_0
    //   130: aload_0
    //   131: getfield 108	org/telegram/PhoneFormat/PhoneFormat:data	[B
    //   134: invokestatic 308	java/nio/ByteBuffer:wrap	([B)Ljava/nio/ByteBuffer;
    //   137: putfield 310	org/telegram/PhoneFormat/PhoneFormat:buffer	Ljava/nio/ByteBuffer;
    //   140: aload_0
    //   141: getfield 310	org/telegram/PhoneFormat/PhoneFormat:buffer	Ljava/nio/ByteBuffer;
    //   144: getstatic 316	java/nio/ByteOrder:LITTLE_ENDIAN	Ljava/nio/ByteOrder;
    //   147: invokevirtual 320	java/nio/ByteBuffer:order	(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;
    //   150: pop
    //   151: aload 7
    //   153: ifnull +8 -> 161
    //   156: aload 7
    //   158: invokevirtual 297	java/io/ByteArrayOutputStream:close	()V
    //   161: aload_3
    //   162: ifnull +7 -> 169
    //   165: aload_3
    //   166: invokevirtual 298	java/io/InputStream:close	()V
    //   169: aload_1
    //   170: ifnull +154 -> 324
    //   173: aload_1
    //   174: invokevirtual 139	java/lang/String:length	()I
    //   177: ifeq +147 -> 324
    //   180: aload_0
    //   181: aload_1
    //   182: putfield 232	org/telegram/PhoneFormat/PhoneFormat:defaultCountry	Ljava/lang/String;
    //   185: aload_0
    //   186: new 92	java/util/HashMap
    //   189: dup
    //   190: sipush 255
    //   193: invokespecial 321	java/util/HashMap:<init>	(I)V
    //   196: putfield 104	org/telegram/PhoneFormat/PhoneFormat:callingCodeOffsets	Ljava/util/HashMap;
    //   199: aload_0
    //   200: new 92	java/util/HashMap
    //   203: dup
    //   204: sipush 255
    //   207: invokespecial 321	java/util/HashMap:<init>	(I)V
    //   210: putfield 117	org/telegram/PhoneFormat/PhoneFormat:callingCodeCountries	Ljava/util/HashMap;
    //   213: aload_0
    //   214: new 92	java/util/HashMap
    //   217: dup
    //   218: bipush 10
    //   220: invokespecial 321	java/util/HashMap:<init>	(I)V
    //   223: putfield 100	org/telegram/PhoneFormat/PhoneFormat:callingCodeData	Ljava/util/HashMap;
    //   226: aload_0
    //   227: new 92	java/util/HashMap
    //   230: dup
    //   231: sipush 255
    //   234: invokespecial 321	java/util/HashMap:<init>	(I)V
    //   237: putfield 87	org/telegram/PhoneFormat/PhoneFormat:countryCallingCode	Ljava/util/HashMap;
    //   240: aload_0
    //   241: invokevirtual 324	org/telegram/PhoneFormat/PhoneFormat:parseDataHeader	()V
    //   244: aload_0
    //   245: iconst_1
    //   246: putfield 34	org/telegram/PhoneFormat/PhoneFormat:initialzed	Z
    //   249: return
    //   250: astore 4
    //   252: aload 4
    //   254: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   257: goto -96 -> 161
    //   260: astore_3
    //   261: aload_3
    //   262: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   265: goto -96 -> 169
    //   268: astore_1
    //   269: aload_1
    //   270: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   273: goto -162 -> 111
    //   276: astore_1
    //   277: aload_1
    //   278: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   281: return
    //   282: astore_1
    //   283: aload 4
    //   285: astore_3
    //   286: aload 5
    //   288: ifnull +8 -> 296
    //   291: aload 5
    //   293: invokevirtual 297	java/io/ByteArrayOutputStream:close	()V
    //   296: aload_3
    //   297: ifnull +7 -> 304
    //   300: aload_3
    //   301: invokevirtual 298	java/io/InputStream:close	()V
    //   304: aload_1
    //   305: athrow
    //   306: astore 4
    //   308: aload 4
    //   310: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   313: goto -17 -> 296
    //   316: astore_3
    //   317: aload_3
    //   318: invokestatic 258	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   321: goto -17 -> 304
    //   324: aload_0
    //   325: invokestatic 330	java/util/Locale:getDefault	()Ljava/util/Locale;
    //   328: invokevirtual 333	java/util/Locale:getCountry	()Ljava/lang/String;
    //   331: invokevirtual 90	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   334: putfield 232	org/telegram/PhoneFormat/PhoneFormat:defaultCountry	Ljava/lang/String;
    //   337: goto -152 -> 185
    //   340: astore_1
    //   341: aload 7
    //   343: astore 5
    //   345: goto -59 -> 286
    //   348: astore 7
    //   350: aload 8
    //   352: astore_1
    //   353: aload 6
    //   355: astore_3
    //   356: goto -264 -> 92
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	359	0	this	PhoneFormat
    //   0	359	1	paramString	String
    //   65	12	2	i	int
    //   28	138	3	localInputStream	java.io.InputStream
    //   260	2	3	localException1	Exception
    //   285	16	3	localException2	Exception
    //   316	2	3	localException3	Exception
    //   355	1	3	localObject1	Object
    //   4	70	4	localObject2	Object
    //   83	6	4	localException4	Exception
    //   96	1	4	localObject3	Object
    //   250	34	4	localException5	Exception
    //   306	3	4	localException6	Exception
    //   14	330	5	localObject4	Object
    //   1	353	6	localObject5	Object
    //   7	335	7	localObject6	Object
    //   348	1	7	localException7	Exception
    //   10	341	8	localObject7	Object
    // Exception table:
    //   from	to	target	type
    //   48	55	83	java/lang/Exception
    //   55	66	83	java/lang/Exception
    //   71	80	83	java/lang/Exception
    //   120	151	83	java/lang/Exception
    //   156	161	250	java/lang/Exception
    //   165	169	260	java/lang/Exception
    //   107	111	268	java/lang/Exception
    //   115	119	276	java/lang/Exception
    //   16	29	282	finally
    //   39	48	282	finally
    //   98	103	282	finally
    //   291	296	306	java/lang/Exception
    //   300	304	316	java/lang/Exception
    //   48	55	340	finally
    //   55	66	340	finally
    //   71	80	340	finally
    //   120	151	340	finally
    //   16	29	348	java/lang/Exception
    //   39	48	348	java/lang/Exception
  }
  
  public boolean isPhoneNumberValid(String paramString)
  {
    if (!this.initialzed) {}
    CallingCodeInfo localCallingCodeInfo;
    do
    {
      do
      {
        return true;
        paramString = strip(paramString);
        if (!paramString.startsWith("+")) {
          break;
        }
        paramString = paramString.substring(1);
        localCallingCodeInfo = findCallingCodeInfo(paramString);
      } while ((localCallingCodeInfo != null) && (localCallingCodeInfo.isValidPhoneNumber(paramString)));
      return false;
      localCallingCodeInfo = callingCodeInfo(this.defaultCallingCode);
      if (localCallingCodeInfo == null) {
        return false;
      }
      String str = localCallingCodeInfo.matchingAccessCode(paramString);
      if (str == null) {
        break label112;
      }
      paramString = paramString.substring(str.length());
      if (paramString.length() == 0) {
        break;
      }
      localCallingCodeInfo = findCallingCodeInfo(paramString);
    } while ((localCallingCodeInfo != null) && (localCallingCodeInfo.isValidPhoneNumber(paramString)));
    return false;
    return false;
    label112:
    return localCallingCodeInfo.isValidPhoneNumber(paramString);
  }
  
  public void parseDataHeader()
  {
    int k = value32(0);
    int j = 4;
    int i = 0;
    while (i < k)
    {
      String str1 = valueString(j);
      j += 4;
      String str2 = valueString(j);
      j += 4;
      int m = value32(j);
      j += 4;
      if (str2.equals(this.defaultCountry)) {
        this.defaultCallingCode = str1;
      }
      this.countryCallingCode.put(str2, str1);
      this.callingCodeOffsets.put(str1, Integer.valueOf(m + (k * 12 + 4)));
      ArrayList localArrayList2 = (ArrayList)this.callingCodeCountries.get(str1);
      ArrayList localArrayList1 = localArrayList2;
      if (localArrayList2 == null)
      {
        localArrayList1 = new ArrayList();
        this.callingCodeCountries.put(str1, localArrayList1);
      }
      localArrayList1.add(str2);
      i += 1;
    }
    if (this.defaultCallingCode != null) {
      callingCodeInfo(this.defaultCallingCode);
    }
  }
  
  short value16(int paramInt)
  {
    if (paramInt + 2 <= this.data.length)
    {
      this.buffer.position(paramInt);
      return this.buffer.getShort();
    }
    return 0;
  }
  
  int value32(int paramInt)
  {
    if (paramInt + 4 <= this.data.length)
    {
      this.buffer.position(paramInt);
      return this.buffer.getInt();
    }
    return 0;
  }
  
  public String valueString(int paramInt)
  {
    int i = paramInt;
    for (;;)
    {
      try
      {
        if (i >= this.data.length) {
          break;
        }
        if (this.data[i] == 0)
        {
          if (paramInt == i - paramInt) {
            return "";
          }
          String str = new String(this.data, paramInt, i - paramInt);
          return str;
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        return "";
      }
      i += 1;
    }
    return "";
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/PhoneFormat/PhoneFormat.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */