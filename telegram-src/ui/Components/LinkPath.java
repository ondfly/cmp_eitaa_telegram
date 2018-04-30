package org.telegram.ui.Components;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.text.StaticLayout;

public class LinkPath
  extends Path
{
  private StaticLayout currentLayout;
  private int currentLine;
  private float heightOffset;
  private float lastTop = -1.0F;
  
  public void addRect(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, Path.Direction paramDirection)
  {
    float f2 = paramFloat2 + this.heightOffset;
    float f3 = paramFloat4 + this.heightOffset;
    if (this.lastTop == -1.0F) {
      this.lastTop = f2;
    }
    float f1;
    for (;;)
    {
      f1 = this.currentLayout.getLineRight(this.currentLine);
      paramFloat4 = this.currentLayout.getLineLeft(this.currentLine);
      if (paramFloat1 < f1) {
        break;
      }
      return;
      if (this.lastTop != f2)
      {
        this.lastTop = f2;
        this.currentLine += 1;
      }
    }
    paramFloat2 = paramFloat3;
    if (paramFloat3 > f1) {
      paramFloat2 = f1;
    }
    paramFloat3 = paramFloat1;
    if (paramFloat1 < paramFloat4) {
      paramFloat3 = paramFloat4;
    }
    if (f3 != this.currentLayout.getHeight()) {}
    for (paramFloat1 = this.currentLayout.getSpacingAdd();; paramFloat1 = 0.0F)
    {
      super.addRect(paramFloat3, f2, paramFloat2, f3 - paramFloat1, paramDirection);
      return;
    }
  }
  
  public void setCurrentLayout(StaticLayout paramStaticLayout, int paramInt, float paramFloat)
  {
    this.currentLayout = paramStaticLayout;
    this.currentLine = paramStaticLayout.getLineForOffset(paramInt);
    this.lastTop = -1.0F;
    this.heightOffset = paramFloat;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/LinkPath.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */