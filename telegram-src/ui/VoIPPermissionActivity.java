package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.ui.Components.voip.VoIPHelper;

public class VoIPPermissionActivity
  extends Activity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    requestPermissions(new String[] { "android.permission.RECORD_AUDIO" }, 101);
  }
  
  public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 101)
    {
      if ((paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0))
      {
        if (VoIPService.getSharedInstance() != null) {
          VoIPService.getSharedInstance().acceptIncomingCall();
        }
        finish();
        startActivity(new Intent(this, VoIPActivity.class));
      }
    }
    else {
      return;
    }
    if (!shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO"))
    {
      if (VoIPService.getSharedInstance() != null) {
        VoIPService.getSharedInstance().declineIncomingCall();
      }
      VoIPHelper.permissionDenied(this, new Runnable()
      {
        public void run()
        {
          VoIPPermissionActivity.this.finish();
        }
      });
      return;
    }
    finish();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/VoIPPermissionActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */