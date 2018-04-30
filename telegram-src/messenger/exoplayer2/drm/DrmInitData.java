package org.telegram.messenger.exoplayer2.drm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.telegram.messenger.exoplayer2.C;
import org.telegram.messenger.exoplayer2.util.Assertions;
import org.telegram.messenger.exoplayer2.util.Util;

public final class DrmInitData
  implements Parcelable, Comparator<SchemeData>
{
  public static final Parcelable.Creator<DrmInitData> CREATOR = new Parcelable.Creator()
  {
    public DrmInitData createFromParcel(Parcel paramAnonymousParcel)
    {
      return new DrmInitData(paramAnonymousParcel);
    }
    
    public DrmInitData[] newArray(int paramAnonymousInt)
    {
      return new DrmInitData[paramAnonymousInt];
    }
  };
  private int hashCode;
  public final int schemeDataCount;
  private final SchemeData[] schemeDatas;
  public final String schemeType;
  
  DrmInitData(Parcel paramParcel)
  {
    this.schemeType = paramParcel.readString();
    this.schemeDatas = ((SchemeData[])paramParcel.createTypedArray(SchemeData.CREATOR));
    this.schemeDataCount = this.schemeDatas.length;
  }
  
  public DrmInitData(String paramString, List<SchemeData> paramList)
  {
    this(paramString, false, (SchemeData[])paramList.toArray(new SchemeData[paramList.size()]));
  }
  
  private DrmInitData(String paramString, boolean paramBoolean, SchemeData... paramVarArgs)
  {
    this.schemeType = paramString;
    paramString = paramVarArgs;
    if (paramBoolean) {
      paramString = (SchemeData[])paramVarArgs.clone();
    }
    Arrays.sort(paramString, this);
    this.schemeDatas = paramString;
    this.schemeDataCount = paramString.length;
  }
  
  public DrmInitData(String paramString, SchemeData... paramVarArgs)
  {
    this(paramString, true, paramVarArgs);
  }
  
  public DrmInitData(List<SchemeData> paramList)
  {
    this(null, false, (SchemeData[])paramList.toArray(new SchemeData[paramList.size()]));
  }
  
  public DrmInitData(SchemeData... paramVarArgs)
  {
    this(null, paramVarArgs);
  }
  
  private static boolean containsSchemeDataWithUuid(ArrayList<SchemeData> paramArrayList, int paramInt, UUID paramUUID)
  {
    int i = 0;
    while (i < paramInt)
    {
      if (((SchemeData)paramArrayList.get(i)).uuid.equals(paramUUID)) {
        return true;
      }
      i += 1;
    }
    return false;
  }
  
  public static DrmInitData createSessionCreationData(DrmInitData paramDrmInitData1, DrmInitData paramDrmInitData2)
  {
    int j = 0;
    ArrayList localArrayList = new ArrayList();
    Object localObject1 = null;
    int k;
    int i;
    if (paramDrmInitData1 != null)
    {
      localObject2 = paramDrmInitData1.schemeType;
      paramDrmInitData1 = paramDrmInitData1.schemeDatas;
      k = paramDrmInitData1.length;
      i = 0;
      for (;;)
      {
        localObject1 = localObject2;
        if (i >= k) {
          break;
        }
        localObject1 = paramDrmInitData1[i];
        if (((SchemeData)localObject1).hasData()) {
          localArrayList.add(localObject1);
        }
        i += 1;
      }
    }
    Object localObject2 = localObject1;
    if (paramDrmInitData2 != null)
    {
      paramDrmInitData1 = (DrmInitData)localObject1;
      if (localObject1 == null) {
        paramDrmInitData1 = paramDrmInitData2.schemeType;
      }
      k = localArrayList.size();
      paramDrmInitData2 = paramDrmInitData2.schemeDatas;
      int m = paramDrmInitData2.length;
      i = j;
      for (;;)
      {
        localObject2 = paramDrmInitData1;
        if (i >= m) {
          break;
        }
        localObject1 = paramDrmInitData2[i];
        if ((((SchemeData)localObject1).hasData()) && (!containsSchemeDataWithUuid(localArrayList, k, ((SchemeData)localObject1).uuid))) {
          localArrayList.add(localObject1);
        }
        i += 1;
      }
    }
    if (localArrayList.isEmpty()) {
      return null;
    }
    return new DrmInitData((String)localObject2, localArrayList);
  }
  
  public int compare(SchemeData paramSchemeData1, SchemeData paramSchemeData2)
  {
    if (C.UUID_NIL.equals(paramSchemeData1.uuid))
    {
      if (C.UUID_NIL.equals(paramSchemeData2.uuid)) {
        return 0;
      }
      return 1;
    }
    return paramSchemeData1.uuid.compareTo(paramSchemeData2.uuid);
  }
  
  public DrmInitData copyWithSchemeType(String paramString)
  {
    if (Util.areEqual(this.schemeType, paramString)) {
      return this;
    }
    return new DrmInitData(paramString, false, this.schemeDatas);
  }
  
  public int describeContents()
  {
    return 0;
  }
  
  public boolean equals(Object paramObject)
  {
    if (this == paramObject) {}
    do
    {
      return true;
      if ((paramObject == null) || (getClass() != paramObject.getClass())) {
        return false;
      }
      paramObject = (DrmInitData)paramObject;
    } while ((Util.areEqual(this.schemeType, ((DrmInitData)paramObject).schemeType)) && (Arrays.equals(this.schemeDatas, ((DrmInitData)paramObject).schemeDatas)));
    return false;
  }
  
  public SchemeData get(int paramInt)
  {
    return this.schemeDatas[paramInt];
  }
  
  @Deprecated
  public SchemeData get(UUID paramUUID)
  {
    SchemeData[] arrayOfSchemeData = this.schemeDatas;
    int j = arrayOfSchemeData.length;
    int i = 0;
    while (i < j)
    {
      SchemeData localSchemeData = arrayOfSchemeData[i];
      if (localSchemeData.matches(paramUUID)) {
        return localSchemeData;
      }
      i += 1;
    }
    return null;
  }
  
  public int hashCode()
  {
    if (this.hashCode == 0) {
      if (this.schemeType != null) {
        break label37;
      }
    }
    label37:
    for (int i = 0;; i = this.schemeType.hashCode())
    {
      this.hashCode = (i * 31 + Arrays.hashCode(this.schemeDatas));
      return this.hashCode;
    }
  }
  
  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeString(this.schemeType);
    paramParcel.writeTypedArray(this.schemeDatas, 0);
  }
  
  public static final class SchemeData
    implements Parcelable
  {
    public static final Parcelable.Creator<SchemeData> CREATOR = new Parcelable.Creator()
    {
      public DrmInitData.SchemeData createFromParcel(Parcel paramAnonymousParcel)
      {
        return new DrmInitData.SchemeData(paramAnonymousParcel);
      }
      
      public DrmInitData.SchemeData[] newArray(int paramAnonymousInt)
      {
        return new DrmInitData.SchemeData[paramAnonymousInt];
      }
    };
    public final byte[] data;
    private int hashCode;
    public final String mimeType;
    public final boolean requiresSecureDecryption;
    private final UUID uuid;
    
    SchemeData(Parcel paramParcel)
    {
      this.uuid = new UUID(paramParcel.readLong(), paramParcel.readLong());
      this.mimeType = paramParcel.readString();
      this.data = paramParcel.createByteArray();
      if (paramParcel.readByte() != 0) {}
      for (boolean bool = true;; bool = false)
      {
        this.requiresSecureDecryption = bool;
        return;
      }
    }
    
    public SchemeData(UUID paramUUID, String paramString, byte[] paramArrayOfByte)
    {
      this(paramUUID, paramString, paramArrayOfByte, false);
    }
    
    public SchemeData(UUID paramUUID, String paramString, byte[] paramArrayOfByte, boolean paramBoolean)
    {
      this.uuid = ((UUID)Assertions.checkNotNull(paramUUID));
      this.mimeType = ((String)Assertions.checkNotNull(paramString));
      this.data = paramArrayOfByte;
      this.requiresSecureDecryption = paramBoolean;
    }
    
    public boolean canReplace(SchemeData paramSchemeData)
    {
      return (hasData()) && (!paramSchemeData.hasData()) && (matches(paramSchemeData.uuid));
    }
    
    public int describeContents()
    {
      return 0;
    }
    
    public boolean equals(Object paramObject)
    {
      boolean bool2 = true;
      boolean bool1;
      if (!(paramObject instanceof SchemeData)) {
        bool1 = false;
      }
      do
      {
        do
        {
          return bool1;
          bool1 = bool2;
        } while (paramObject == this);
        paramObject = (SchemeData)paramObject;
        if ((!this.mimeType.equals(((SchemeData)paramObject).mimeType)) || (!Util.areEqual(this.uuid, ((SchemeData)paramObject).uuid))) {
          break;
        }
        bool1 = bool2;
      } while (Arrays.equals(this.data, ((SchemeData)paramObject).data));
      return false;
    }
    
    public boolean hasData()
    {
      return this.data != null;
    }
    
    public int hashCode()
    {
      if (this.hashCode == 0) {
        this.hashCode = ((this.uuid.hashCode() * 31 + this.mimeType.hashCode()) * 31 + Arrays.hashCode(this.data));
      }
      return this.hashCode;
    }
    
    public boolean matches(UUID paramUUID)
    {
      return (C.UUID_NIL.equals(this.uuid)) || (paramUUID.equals(this.uuid));
    }
    
    public void writeToParcel(Parcel paramParcel, int paramInt)
    {
      paramParcel.writeLong(this.uuid.getMostSignificantBits());
      paramParcel.writeLong(this.uuid.getLeastSignificantBits());
      paramParcel.writeString(this.mimeType);
      paramParcel.writeByteArray(this.data);
      if (this.requiresSecureDecryption) {}
      for (paramInt = 1;; paramInt = 0)
      {
        paramParcel.writeByte((byte)paramInt);
        return;
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/DrmInitData.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */