package org.telegram.messenger.exoplayer2.text.cea;

import android.text.Layout.Alignment;
import org.telegram.messenger.exoplayer2.text.Cue;

final class Cea708Cue
  extends Cue
  implements Comparable<Cea708Cue>
{
  public static final int PRIORITY_UNSET = -1;
  public final int priority;
  
  public Cea708Cue(CharSequence paramCharSequence, Layout.Alignment paramAlignment, float paramFloat1, int paramInt1, int paramInt2, float paramFloat2, int paramInt3, float paramFloat3, boolean paramBoolean, int paramInt4, int paramInt5)
  {
    super(paramCharSequence, paramAlignment, paramFloat1, paramInt1, paramInt2, paramFloat2, paramInt3, paramFloat3, paramBoolean, paramInt4);
    this.priority = paramInt5;
  }
  
  public int compareTo(Cea708Cue paramCea708Cue)
  {
    if (paramCea708Cue.priority < this.priority) {
      return -1;
    }
    if (paramCea708Cue.priority > this.priority) {
      return 1;
    }
    return 0;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/text/cea/Cea708Cue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */