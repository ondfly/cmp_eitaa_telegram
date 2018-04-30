package org.telegram.ui.ActionBar;

import android.content.Context;

public class DarkAlertDialog
  extends AlertDialog
{
  public DarkAlertDialog(Context paramContext, int paramInt)
  {
    super(paramContext, paramInt);
  }
  
  protected int getThemeColor(String paramString)
  {
    int j = -1;
    label48:
    int i;
    switch (paramString.hashCode())
    {
    default: 
      i = -1;
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        j = super.getThemeColor(paramString);
      case 1: 
      case 2: 
      case 3: 
        return j;
        if (!paramString.equals("dialogBackground")) {
          break label48;
        }
        i = 0;
        continue;
        if (!paramString.equals("dialogTextBlack")) {
          break label48;
        }
        i = 1;
        continue;
        if (!paramString.equals("dialogButton")) {
          break label48;
        }
        i = 2;
        continue;
        if (!paramString.equals("dialogScrollGlow")) {
          break label48;
        }
        i = 3;
      }
    }
    return -14277082;
  }
  
  public static class Builder
    extends AlertDialog.Builder
  {
    public Builder(Context paramContext)
    {
      super();
    }
    
    public Builder(Context paramContext, int paramInt)
    {
      super();
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ActionBar/DarkAlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */