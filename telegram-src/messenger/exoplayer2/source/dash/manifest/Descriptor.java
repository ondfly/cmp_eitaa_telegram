package org.telegram.messenger.exoplayer2.source.dash.manifest;

import org.telegram.messenger.exoplayer2.util.Util;

public final class Descriptor
{
  public final String id;
  public final String schemeIdUri;
  public final String value;
  
  public Descriptor(String paramString1, String paramString2, String paramString3)
  {
    this.schemeIdUri = paramString1;
    this.value = paramString2;
    this.id = paramString3;
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
      paramObject = (Descriptor)paramObject;
    } while ((Util.areEqual(this.schemeIdUri, ((Descriptor)paramObject).schemeIdUri)) && (Util.areEqual(this.value, ((Descriptor)paramObject).value)) && (Util.areEqual(this.id, ((Descriptor)paramObject).id)));
    return false;
  }
  
  public int hashCode()
  {
    int k = 0;
    int i;
    if (this.schemeIdUri != null)
    {
      i = this.schemeIdUri.hashCode();
      if (this.value == null) {
        break label64;
      }
    }
    label64:
    for (int j = this.value.hashCode();; j = 0)
    {
      if (this.id != null) {
        k = this.id.hashCode();
      }
      return (i * 31 + j) * 31 + k;
      i = 0;
      break;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/dash/manifest/Descriptor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */