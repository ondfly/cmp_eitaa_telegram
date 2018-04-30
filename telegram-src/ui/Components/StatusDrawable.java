package org.telegram.ui.Components;

import android.graphics.drawable.Drawable;

public abstract class StatusDrawable
  extends Drawable
{
  public abstract void setIsChat(boolean paramBoolean);
  
  public abstract void start();
  
  public abstract void stop();
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/StatusDrawable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */