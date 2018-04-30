package ir.eitaa.messenger.exoplayer.dash.mpd;

import ir.eitaa.messenger.exoplayer.drm.DrmInitData.SchemeInitData;
import ir.eitaa.messenger.exoplayer.util.Assertions;
import ir.eitaa.messenger.exoplayer.util.Util;
import java.util.UUID;

public class ContentProtection
{
  public final DrmInitData.SchemeInitData data;
  public final String schemeUriId;
  public final UUID uuid;
  
  public ContentProtection(String paramString, UUID paramUUID, DrmInitData.SchemeInitData paramSchemeInitData)
  {
    this.schemeUriId = ((String)Assertions.checkNotNull(paramString));
    this.uuid = paramUUID;
    this.data = paramSchemeInitData;
  }
  
  public boolean equals(Object paramObject)
  {
    boolean bool2 = true;
    boolean bool1;
    if (!(paramObject instanceof ContentProtection)) {
      bool1 = false;
    }
    do
    {
      do
      {
        return bool1;
        bool1 = bool2;
      } while (paramObject == this);
      paramObject = (ContentProtection)paramObject;
      if ((!this.schemeUriId.equals(((ContentProtection)paramObject).schemeUriId)) || (!Util.areEqual(this.uuid, ((ContentProtection)paramObject).uuid))) {
        break;
      }
      bool1 = bool2;
    } while (Util.areEqual(this.data, ((ContentProtection)paramObject).data));
    return false;
  }
  
  public int hashCode()
  {
    int j = 0;
    int k = this.schemeUriId.hashCode();
    if (this.uuid != null) {}
    for (int i = this.uuid.hashCode();; i = 0)
    {
      if (this.data != null) {
        j = this.data.hashCode();
      }
      return (k * 37 + i) * 37 + j;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/dash/mpd/ContentProtection.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */