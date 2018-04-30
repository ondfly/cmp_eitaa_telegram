package org.telegram.messenger.exoplayer2.source;

public class CompositeSequenceableLoader
  implements SequenceableLoader
{
  protected final SequenceableLoader[] loaders;
  
  public CompositeSequenceableLoader(SequenceableLoader[] paramArrayOfSequenceableLoader)
  {
    this.loaders = paramArrayOfSequenceableLoader;
  }
  
  public boolean continueLoading(long paramLong)
  {
    boolean bool3 = false;
    boolean bool1;
    boolean bool4;
    do
    {
      bool1 = false;
      long l1 = getNextLoadPositionUs();
      if (l1 == Long.MIN_VALUE) {
        return bool3;
      }
      SequenceableLoader[] arrayOfSequenceableLoader = this.loaders;
      int k = arrayOfSequenceableLoader.length;
      int i = 0;
      if (i < k)
      {
        SequenceableLoader localSequenceableLoader = arrayOfSequenceableLoader[i];
        long l2 = localSequenceableLoader.getNextLoadPositionUs();
        if ((l2 != Long.MIN_VALUE) && (l2 <= paramLong)) {}
        for (int j = 1;; j = 0)
        {
          boolean bool2;
          if (l2 != l1)
          {
            bool2 = bool1;
            if (j == 0) {}
          }
          else
          {
            bool2 = bool1 | localSequenceableLoader.continueLoading(paramLong);
          }
          i += 1;
          bool1 = bool2;
          break;
        }
      }
      bool4 = bool3 | bool1;
      bool3 = bool4;
    } while (bool1);
    return bool4;
  }
  
  public final long getBufferedPositionUs()
  {
    long l1 = Long.MAX_VALUE;
    SequenceableLoader[] arrayOfSequenceableLoader = this.loaders;
    int j = arrayOfSequenceableLoader.length;
    int i = 0;
    while (i < j)
    {
      long l3 = arrayOfSequenceableLoader[i].getBufferedPositionUs();
      l2 = l1;
      if (l3 != Long.MIN_VALUE) {
        l2 = Math.min(l1, l3);
      }
      i += 1;
      l1 = l2;
    }
    long l2 = l1;
    if (l1 == Long.MAX_VALUE) {
      l2 = Long.MIN_VALUE;
    }
    return l2;
  }
  
  public final long getNextLoadPositionUs()
  {
    long l1 = Long.MAX_VALUE;
    SequenceableLoader[] arrayOfSequenceableLoader = this.loaders;
    int j = arrayOfSequenceableLoader.length;
    int i = 0;
    while (i < j)
    {
      long l3 = arrayOfSequenceableLoader[i].getNextLoadPositionUs();
      l2 = l1;
      if (l3 != Long.MIN_VALUE) {
        l2 = Math.min(l1, l3);
      }
      i += 1;
      l1 = l2;
    }
    long l2 = l1;
    if (l1 == Long.MAX_VALUE) {
      l2 = Long.MIN_VALUE;
    }
    return l2;
  }
  
  public final void reevaluateBuffer(long paramLong)
  {
    SequenceableLoader[] arrayOfSequenceableLoader = this.loaders;
    int j = arrayOfSequenceableLoader.length;
    int i = 0;
    while (i < j)
    {
      arrayOfSequenceableLoader[i].reevaluateBuffer(paramLong);
      i += 1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/CompositeSequenceableLoader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */