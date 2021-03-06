package org.telegram.messenger.exoplayer2.drm;

import java.util.UUID;

public abstract interface MediaDrmCallback
{
  public abstract byte[] executeKeyRequest(UUID paramUUID, ExoMediaDrm.KeyRequest paramKeyRequest)
    throws Exception;
  
  public abstract byte[] executeProvisionRequest(UUID paramUUID, ExoMediaDrm.ProvisionRequest paramProvisionRequest)
    throws Exception;
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/MediaDrmCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */