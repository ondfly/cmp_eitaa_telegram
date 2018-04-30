package ir.eitaa.messenger.exoplayer.drm;

import android.annotation.TargetApi;
import java.util.UUID;

@TargetApi(18)
public abstract interface MediaDrmCallback
{
  public abstract byte[] executeKeyRequest(UUID paramUUID, ExoMediaDrm.KeyRequest paramKeyRequest)
    throws Exception;
  
  public abstract byte[] executeProvisionRequest(UUID paramUUID, ExoMediaDrm.ProvisionRequest paramProvisionRequest)
    throws Exception;
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/drm/MediaDrmCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */