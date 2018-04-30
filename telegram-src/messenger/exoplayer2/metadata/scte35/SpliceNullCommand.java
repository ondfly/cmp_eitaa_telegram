package org.telegram.messenger.exoplayer2.metadata.scte35;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public final class SpliceNullCommand
  extends SpliceCommand
{
  public static final Parcelable.Creator<SpliceNullCommand> CREATOR = new Parcelable.Creator()
  {
    public SpliceNullCommand createFromParcel(Parcel paramAnonymousParcel)
    {
      return new SpliceNullCommand();
    }
    
    public SpliceNullCommand[] newArray(int paramAnonymousInt)
    {
      return new SpliceNullCommand[paramAnonymousInt];
    }
  };
  
  public void writeToParcel(Parcel paramParcel, int paramInt) {}
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/metadata/scte35/SpliceNullCommand.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */