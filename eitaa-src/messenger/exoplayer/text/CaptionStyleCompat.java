package ir.eitaa.messenger.exoplayer.text;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import ir.eitaa.messenger.exoplayer.util.Util;

public final class CaptionStyleCompat
{
  public static final CaptionStyleCompat DEFAULT = new CaptionStyleCompat(-1, -16777216, 0, 0, -1, null);
  public static final int EDGE_TYPE_DEPRESSED = 4;
  public static final int EDGE_TYPE_DROP_SHADOW = 2;
  public static final int EDGE_TYPE_NONE = 0;
  public static final int EDGE_TYPE_OUTLINE = 1;
  public static final int EDGE_TYPE_RAISED = 3;
  public static final int USE_TRACK_COLOR_SETTINGS = 1;
  public final int backgroundColor;
  public final int edgeColor;
  public final int edgeType;
  public final int foregroundColor;
  public final Typeface typeface;
  public final int windowColor;
  
  public CaptionStyleCompat(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, Typeface paramTypeface)
  {
    this.foregroundColor = paramInt1;
    this.backgroundColor = paramInt2;
    this.windowColor = paramInt3;
    this.edgeType = paramInt4;
    this.edgeColor = paramInt5;
    this.typeface = paramTypeface;
  }
  
  @TargetApi(19)
  public static CaptionStyleCompat createFromCaptionStyle(CaptioningManager.CaptionStyle paramCaptionStyle)
  {
    if (Util.SDK_INT >= 21) {
      return createFromCaptionStyleV21(paramCaptionStyle);
    }
    return createFromCaptionStyleV19(paramCaptionStyle);
  }
  
  @TargetApi(19)
  private static CaptionStyleCompat createFromCaptionStyleV19(CaptioningManager.CaptionStyle paramCaptionStyle)
  {
    return new CaptionStyleCompat(paramCaptionStyle.foregroundColor, paramCaptionStyle.backgroundColor, 0, paramCaptionStyle.edgeType, paramCaptionStyle.edgeColor, paramCaptionStyle.getTypeface());
  }
  
  @TargetApi(21)
  private static CaptionStyleCompat createFromCaptionStyleV21(CaptioningManager.CaptionStyle paramCaptionStyle)
  {
    int i;
    int j;
    label24:
    int k;
    label36:
    int m;
    if (paramCaptionStyle.hasForegroundColor())
    {
      i = paramCaptionStyle.foregroundColor;
      if (!paramCaptionStyle.hasBackgroundColor()) {
        break label91;
      }
      j = paramCaptionStyle.backgroundColor;
      if (!paramCaptionStyle.hasWindowColor()) {
        break label101;
      }
      k = paramCaptionStyle.windowColor;
      if (!paramCaptionStyle.hasEdgeType()) {
        break label111;
      }
      m = paramCaptionStyle.edgeType;
      label49:
      if (!paramCaptionStyle.hasEdgeColor()) {
        break label122;
      }
    }
    label91:
    label101:
    label111:
    label122:
    for (int n = paramCaptionStyle.edgeColor;; n = DEFAULT.edgeColor)
    {
      return new CaptionStyleCompat(i, j, k, m, n, paramCaptionStyle.getTypeface());
      i = DEFAULT.foregroundColor;
      break;
      j = DEFAULT.backgroundColor;
      break label24;
      k = DEFAULT.windowColor;
      break label36;
      m = DEFAULT.edgeType;
      break label49;
    }
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/exoplayer/text/CaptionStyleCompat.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */