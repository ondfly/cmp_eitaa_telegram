package org.telegram.messenger.exoplayer2.metadata.id3;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.util.Util;

public final class ApicFrame
  extends Id3Frame
{
  public static final Parcelable.Creator<ApicFrame> CREATOR = new Parcelable.Creator()
  {
    public ApicFrame createFromParcel(Parcel paramAnonymousParcel)
    {
      return new ApicFrame(paramAnonymousParcel);
    }
    
    public ApicFrame[] newArray(int paramAnonymousInt)
    {
      return new ApicFrame[paramAnonymousInt];
    }
  };
  public static final String ID = "APIC";
  public final String description;
  public final String mimeType;
  public final byte[] pictureData;
  public final int pictureType;
  
  ApicFrame(Parcel paramParcel)
  {
    super("APIC");
    this.mimeType = paramParcel.readString();
    this.description = paramParcel.readString();
    this.pictureType = paramParcel.readInt();
    this.pictureData = paramParcel.createByteArray();
  }
  
  public ApicFrame(String paramString1, String paramString2, int paramInt, byte[] paramArrayOfByte)
  {
    super("APIC");
    this.mimeType = paramString1;
    this.description = paramString2;
    this.pictureType = paramInt;
    this.pictureData = paramArrayOfByte;
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
      paramObject = (ApicFrame)paramObject;
    } while ((this.pictureType == ((ApicFrame)paramObject).pictureType) && (Util.areEqual(this.mimeType, ((ApicFrame)paramObject).mimeType)) && (Util.areEqual(this.description, ((ApicFrame)paramObject).description)) && (Arrays.equals(this.pictureData, ((ApicFrame)paramObject).pictureData)));
    return false;
  }
  
  public int hashCode()
  {
    int j = 0;
    int k = this.pictureType;
    if (this.mimeType != null) {}
    for (int i = this.mimeType.hashCode();; i = 0)
    {
      if (this.description != null) {
        j = this.description.hashCode();
      }
      return (((k + 527) * 31 + i) * 31 + j) * 31 + Arrays.hashCode(this.pictureData);
    }
  }
  
  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeString(this.mimeType);
    paramParcel.writeString(this.description);
    paramParcel.writeInt(this.pictureType);
    paramParcel.writeByteArray(this.pictureData);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/id3/ApicFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */