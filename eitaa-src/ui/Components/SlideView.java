package ir.eitaa.ui.Components;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

public class SlideView
  extends LinearLayout
{
  public SlideView(Context paramContext)
  {
    super(paramContext);
  }
  
  public String getHeaderName()
  {
    return "";
  }
  
  public boolean needBackButton()
  {
    return false;
  }
  
  public void onBackPressed() {}
  
  public void onDestroyActivity() {}
  
  public void onNextPressed() {}
  
  public void onShow() {}
  
  public void restoreStateParams(Bundle paramBundle) {}
  
  public void saveStateParams(Bundle paramBundle) {}
  
  public void setParams(Bundle paramBundle) {}
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/SlideView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */