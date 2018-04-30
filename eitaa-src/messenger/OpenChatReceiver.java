package ir.eitaa.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import ir.eitaa.ui.LaunchActivity;

public class OpenChatReceiver
  extends Activity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    paramBundle = getIntent();
    if (paramBundle == null) {
      finish();
    }
    if ((paramBundle.getAction() == null) || (!paramBundle.getAction().startsWith("com.tmessages.openchat")))
    {
      finish();
      return;
    }
    Intent localIntent = new Intent(this, LaunchActivity.class);
    localIntent.setAction(paramBundle.getAction());
    localIntent.putExtras(paramBundle);
    startActivity(localIntent);
    finish();
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/OpenChatReceiver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */