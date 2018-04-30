package org.telegram.messenger.exoplayer2.drm;

import android.annotation.TargetApi;
import android.os.Looper;

@TargetApi(16)
public abstract interface DrmSessionManager<T extends ExoMediaCrypto>
{
  public abstract DrmSession<T> acquireSession(Looper paramLooper, DrmInitData paramDrmInitData);
  
  public abstract boolean canAcquireSession(DrmInitData paramDrmInitData);
  
  public abstract void releaseSession(DrmSession<T> paramDrmSession);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/drm/DrmSessionManager.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */