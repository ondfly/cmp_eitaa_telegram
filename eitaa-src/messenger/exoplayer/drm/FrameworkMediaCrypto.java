package ir.eitaa.messenger.exoplayer.drm;

import android.annotation.TargetApi;
import android.media.MediaCrypto;
import ir.eitaa.messenger.exoplayer.util.Assertions;

@TargetApi(16)
public final class FrameworkMediaCrypto
  implements ExoMediaCrypto
{
  private final MediaCrypto mediaCrypto;
  
  FrameworkMediaCrypto(MediaCrypto paramMediaCrypto)
  {
    this.mediaCrypto = ((MediaCrypto)Assertions.checkNotNull(paramMediaCrypto));
  }
  
  public MediaCrypto getWrappedMediaCrypto()
  {
    return this.mediaCrypto;
  }
  
  public boolean requiresSecureDecoderComponent(String paramString)
  {
    return this.mediaCrypto.requiresSecureDecoderComponent(paramString);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/drm/FrameworkMediaCrypto.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */