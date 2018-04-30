package org.telegram.messenger.exoplayer2.source.dash.manifest;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class RepresentationKey
  implements Parcelable, Comparable<RepresentationKey>
{
  public static final Parcelable.Creator<RepresentationKey> CREATOR = new Parcelable.Creator()
  {
    public RepresentationKey createFromParcel(Parcel paramAnonymousParcel)
    {
      return new RepresentationKey(paramAnonymousParcel.readInt(), paramAnonymousParcel.readInt(), paramAnonymousParcel.readInt());
    }
    
    public RepresentationKey[] newArray(int paramAnonymousInt)
    {
      return new RepresentationKey[paramAnonymousInt];
    }
  };
  public final int adaptationSetIndex;
  public final int periodIndex;
  public final int representationIndex;
  
  public RepresentationKey(int paramInt1, int paramInt2, int paramInt3)
  {
    this.periodIndex = paramInt1;
    this.adaptationSetIndex = paramInt2;
    this.representationIndex = paramInt3;
  }
  
  public int compareTo(RepresentationKey paramRepresentationKey)
  {
    int j = this.periodIndex - paramRepresentationKey.periodIndex;
    int i = j;
    if (j == 0)
    {
      j = this.adaptationSetIndex - paramRepresentationKey.adaptationSetIndex;
      i = j;
      if (j == 0) {
        i = this.representationIndex - paramRepresentationKey.representationIndex;
      }
    }
    return i;
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
      paramObject = (RepresentationKey)paramObject;
    } while ((this.periodIndex == ((RepresentationKey)paramObject).periodIndex) && (this.adaptationSetIndex == ((RepresentationKey)paramObject).adaptationSetIndex) && (this.representationIndex == ((RepresentationKey)paramObject).representationIndex));
    return false;
  }
  
  public int hashCode()
  {
    return (this.periodIndex * 31 + this.adaptationSetIndex) * 31 + this.representationIndex;
  }
  
  public String toString()
  {
    return this.periodIndex + "." + this.adaptationSetIndex + "." + this.representationIndex;
  }
  
  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeInt(this.periodIndex);
    paramParcel.writeInt(this.adaptationSetIndex);
    paramParcel.writeInt(this.representationIndex);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/RepresentationKey.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */