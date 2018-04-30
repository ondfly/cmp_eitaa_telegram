package org.telegram.messenger.exoplayer2.metadata.id3;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import org.telegram.messenger.exoplayer2.util.Util;

public final class GeobFrame
  extends Id3Frame
{
  public static final Parcelable.Creator<GeobFrame> CREATOR = new Parcelable.Creator()
  {
    public GeobFrame createFromParcel(Parcel paramAnonymousParcel)
    {
      return new GeobFrame(paramAnonymousParcel);
    }
    
    public GeobFrame[] newArray(int paramAnonymousInt)
    {
      return new GeobFrame[paramAnonymousInt];
    }
  };
  public static final String ID = "GEOB";
  public final byte[] data;
  public final String description;
  public final String filename;
  public final String mimeType;
  
  GeobFrame(Parcel paramParcel)
  {
    super("GEOB");
    this.mimeType = paramParcel.readString();
    this.filename = paramParcel.readString();
    this.description = paramParcel.readString();
    this.data = paramParcel.createByteArray();
  }
  
  public GeobFrame(String paramString1, String paramString2, String paramString3, byte[] paramArrayOfByte)
  {
    super("GEOB");
    this.mimeType = paramString1;
    this.filename = paramString2;
    this.description = paramString3;
    this.data = paramArrayOfByte;
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
      paramObject = (GeobFrame)paramObject;
    } while ((Util.areEqual(this.mimeType, ((GeobFrame)paramObject).mimeType)) && (Util.areEqual(this.filename, ((GeobFrame)paramObject).filename)) && (Util.areEqual(this.description, ((GeobFrame)paramObject).description)) && (Arrays.equals(this.data, ((GeobFrame)paramObject).data)));
    return false;
  }
  
  public int hashCode()
  {
    int k = 0;
    int i;
    if (this.mimeType != null)
    {
      i = this.mimeType.hashCode();
      if (this.filename == null) {
        break label79;
      }
    }
    label79:
    for (int j = this.filename.hashCode();; j = 0)
    {
      if (this.description != null) {
        k = this.description.hashCode();
      }
      return (((i + 527) * 31 + j) * 31 + k) * 31 + Arrays.hashCode(this.data);
      i = 0;
      break;
    }
  }
  
  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeString(this.mimeType);
    paramParcel.writeString(this.filename);
    paramParcel.writeString(this.description);
    paramParcel.writeByteArray(this.data);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/id3/GeobFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */