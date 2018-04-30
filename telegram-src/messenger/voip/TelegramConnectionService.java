package org.telegram.messenger.voip;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

@TargetApi(26)
public class TelegramConnectionService
  extends ConnectionService
{
  public void onCreate()
  {
    super.onCreate();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.w("ConnectionService created");
    }
  }
  
  public Connection onCreateIncomingConnection(PhoneAccountHandle paramPhoneAccountHandle, ConnectionRequest paramConnectionRequest)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("onCreateIncomingConnection ");
    }
    paramPhoneAccountHandle = paramConnectionRequest.getExtras();
    if (paramPhoneAccountHandle.getInt("call_type") == 1)
    {
      paramPhoneAccountHandle = VoIPService.getSharedInstance();
      if (paramPhoneAccountHandle != null) {}
    }
    while (paramPhoneAccountHandle.getInt("call_type") != 2)
    {
      do
      {
        return null;
      } while (paramPhoneAccountHandle.isOutgoing());
      return paramPhoneAccountHandle.getConnectionAndStartCall();
    }
    return null;
  }
  
  public void onCreateIncomingConnectionFailed(PhoneAccountHandle paramPhoneAccountHandle, ConnectionRequest paramConnectionRequest)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.e("onCreateIncomingConnectionFailed ");
    }
    if (VoIPBaseService.getSharedInstance() != null) {
      VoIPBaseService.getSharedInstance().callFailedFromConnectionService();
    }
  }
  
  public Connection onCreateOutgoingConnection(PhoneAccountHandle paramPhoneAccountHandle, ConnectionRequest paramConnectionRequest)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("onCreateOutgoingConnection ");
    }
    paramPhoneAccountHandle = paramConnectionRequest.getExtras();
    if (paramPhoneAccountHandle.getInt("call_type") == 1)
    {
      paramPhoneAccountHandle = VoIPService.getSharedInstance();
      if (paramPhoneAccountHandle != null) {}
    }
    while (paramPhoneAccountHandle.getInt("call_type") != 2)
    {
      return null;
      return paramPhoneAccountHandle.getConnectionAndStartCall();
    }
    return null;
  }
  
  public void onCreateOutgoingConnectionFailed(PhoneAccountHandle paramPhoneAccountHandle, ConnectionRequest paramConnectionRequest)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.e("onCreateOutgoingConnectionFailed ");
    }
    if (VoIPBaseService.getSharedInstance() != null) {
      VoIPBaseService.getSharedInstance().callFailedFromConnectionService();
    }
  }
  
  public void onDestroy()
  {
    super.onDestroy();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.w("ConnectionService destroyed");
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/voip/TelegramConnectionService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */