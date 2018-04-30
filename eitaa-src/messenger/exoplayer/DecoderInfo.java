package ir.eitaa.messenger.exoplayer;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo.CodecCapabilities;
import ir.eitaa.messenger.exoplayer.util.Util;

@TargetApi(16)
public final class DecoderInfo
{
  public final boolean adaptive;
  public final MediaCodecInfo.CodecCapabilities capabilities;
  public final String name;
  
  DecoderInfo(String paramString, MediaCodecInfo.CodecCapabilities paramCodecCapabilities)
  {
    this.name = paramString;
    this.capabilities = paramCodecCapabilities;
    this.adaptive = isAdaptive(paramCodecCapabilities);
  }
  
  private static boolean isAdaptive(MediaCodecInfo.CodecCapabilities paramCodecCapabilities)
  {
    return (paramCodecCapabilities != null) && (Util.SDK_INT >= 19) && (isAdaptiveV19(paramCodecCapabilities));
  }
  
  @TargetApi(19)
  private static boolean isAdaptiveV19(MediaCodecInfo.CodecCapabilities paramCodecCapabilities)
  {
    return paramCodecCapabilities.isFeatureSupported("adaptive-playback");
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/DecoderInfo.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */