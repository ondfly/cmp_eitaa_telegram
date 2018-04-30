package org.telegram.messenger.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class VoIPMediaButtonReceiver
  extends BroadcastReceiver
{
  public void onReceive(Context paramContext, Intent paramIntent)
  {
    if ((!"android.intent.action.MEDIA_BUTTON".equals(paramIntent.getAction())) || (VoIPService.getSharedInstance() == null)) {
      return;
    }
    paramContext = (KeyEvent)paramIntent.getParcelableExtra("android.intent.extra.KEY_EVENT");
    VoIPService.getSharedInstance().onMediaButtonEvent(paramContext);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/VoIPMediaButtonReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */