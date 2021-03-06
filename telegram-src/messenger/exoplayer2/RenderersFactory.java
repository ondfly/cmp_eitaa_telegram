package org.telegram.messenger.exoplayer2;

import android.os.Handler;
import org.telegram.messenger.exoplayer2.audio.AudioRendererEventListener;
import org.telegram.messenger.exoplayer2.metadata.MetadataOutput;
import org.telegram.messenger.exoplayer2.text.TextOutput;
import org.telegram.messenger.exoplayer2.video.VideoRendererEventListener;

public abstract interface RenderersFactory
{
  public abstract Renderer[] createRenderers(Handler paramHandler, VideoRendererEventListener paramVideoRendererEventListener, AudioRendererEventListener paramAudioRendererEventListener, TextOutput paramTextOutput, MetadataOutput paramMetadataOutput);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/RenderersFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */