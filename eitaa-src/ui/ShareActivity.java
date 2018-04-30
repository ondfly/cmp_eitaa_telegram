package ir.eitaa.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import ir.eitaa.messenger.AndroidUtilities;
import ir.eitaa.messenger.ApplicationLoader;
import ir.eitaa.messenger.FileLog;
import ir.eitaa.messenger.MessageObject;
import ir.eitaa.messenger.Utilities;
import ir.eitaa.tgnet.AbstractSerializedData;
import ir.eitaa.tgnet.SerializedData;
import ir.eitaa.tgnet.TLRPC.Message;
import ir.eitaa.ui.Components.ShareAlert;

public class ShareActivity
  extends Activity
{
  private Dialog visibleDialog;
  
  protected void onCreate(Bundle paramBundle)
  {
    ApplicationLoader.postInitApplication();
    AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
    requestWindowFeature(1);
    setTheme(2131296266);
    super.onCreate(paramBundle);
    setContentView(new View(this), new ViewGroup.LayoutParams(-1, -1));
    paramBundle = getIntent();
    if ((paramBundle == null) || (!"android.intent.action.VIEW".equals(paramBundle.getAction())) || (paramBundle.getData() == null))
    {
      finish();
      return;
    }
    paramBundle = paramBundle.getData();
    Object localObject1 = paramBundle.getScheme();
    Object localObject2 = paramBundle.toString();
    paramBundle = paramBundle.getQueryParameter("hash");
    if ((!"tgb".equals(localObject1)) || (!((String)localObject2).toLowerCase().startsWith("tgb://share_game_score")) || (TextUtils.isEmpty(paramBundle)))
    {
      finish();
      return;
    }
    localObject1 = ApplicationLoader.applicationContext.getSharedPreferences("botshare", 0);
    localObject2 = ((SharedPreferences)localObject1).getString(paramBundle + "_m", null);
    if (TextUtils.isEmpty((CharSequence)localObject2))
    {
      finish();
      return;
    }
    localObject2 = new SerializedData(Utilities.hexToBytes((String)localObject2));
    localObject2 = TLRPC.Message.TLdeserialize((AbstractSerializedData)localObject2, ((SerializedData)localObject2).readInt32(false), false);
    if (localObject2 == null)
    {
      finish();
      return;
    }
    paramBundle = ((SharedPreferences)localObject1).getString(paramBundle + "_link", null);
    localObject1 = new MessageObject((TLRPC.Message)localObject2, null, false);
    ((MessageObject)localObject1).messageOwner.with_my_score = true;
    try
    {
      this.visibleDialog = new ShareAlert(this, (MessageObject)localObject1, null, false, paramBundle);
      this.visibleDialog.setCanceledOnTouchOutside(true);
      this.visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
      {
        public void onDismiss(DialogInterface paramAnonymousDialogInterface)
        {
          if (!ShareActivity.this.isFinishing()) {
            ShareActivity.this.finish();
          }
          ShareActivity.access$002(ShareActivity.this, null);
        }
      });
      this.visibleDialog.show();
      return;
    }
    catch (Exception paramBundle)
    {
      FileLog.e("TSMS", paramBundle);
      finish();
    }
  }
  
  public void onPause()
  {
    super.onPause();
    try
    {
      if ((this.visibleDialog != null) && (this.visibleDialog.isShowing()))
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
      return;
    }
    catch (Exception localException)
    {
      FileLog.e("TSMS", localException);
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/ShareActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */