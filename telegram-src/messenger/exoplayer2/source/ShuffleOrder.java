package org.telegram.messenger.exoplayer2.source;

import java.util.Arrays;
import java.util.Random;

public abstract interface ShuffleOrder
{
  public abstract ShuffleOrder cloneAndInsert(int paramInt1, int paramInt2);
  
  public abstract ShuffleOrder cloneAndRemove(int paramInt);
  
  public abstract int getFirstIndex();
  
  public abstract int getLastIndex();
  
  public abstract int getLength();
  
  public abstract int getNextIndex(int paramInt);
  
  public abstract int getPreviousIndex(int paramInt);
  
  public static class DefaultShuffleOrder
    implements ShuffleOrder
  {
    private final int[] indexInShuffled;
    private final Random random;
    private final int[] shuffled;
    
    public DefaultShuffleOrder(int paramInt)
    {
      this(paramInt, new Random());
    }
    
    public DefaultShuffleOrder(int paramInt, long paramLong)
    {
      this(paramInt, new Random(paramLong));
    }
    
    private DefaultShuffleOrder(int paramInt, Random paramRandom)
    {
      this(createShuffledList(paramInt, paramRandom), paramRandom);
    }
    
    private DefaultShuffleOrder(int[] paramArrayOfInt, Random paramRandom)
    {
      this.shuffled = paramArrayOfInt;
      this.random = paramRandom;
      this.indexInShuffled = new int[paramArrayOfInt.length];
      int i = 0;
      while (i < paramArrayOfInt.length)
      {
        this.indexInShuffled[paramArrayOfInt[i]] = i;
        i += 1;
      }
    }
    
    private static int[] createShuffledList(int paramInt, Random paramRandom)
    {
      int[] arrayOfInt = new int[paramInt];
      int i = 0;
      while (i < paramInt)
      {
        int j = paramRandom.nextInt(i + 1);
        arrayOfInt[i] = arrayOfInt[j];
        arrayOfInt[j] = i;
        i += 1;
      }
      return arrayOfInt;
    }
    
    public ShuffleOrder cloneAndInsert(int paramInt1, int paramInt2)
    {
      int[] arrayOfInt1 = new int[paramInt2];
      int[] arrayOfInt2 = new int[paramInt2];
      int i = 0;
      while (i < paramInt2)
      {
        arrayOfInt1[i] = this.random.nextInt(this.shuffled.length + 1);
        j = this.random.nextInt(i + 1);
        arrayOfInt2[i] = arrayOfInt2[j];
        arrayOfInt2[j] = (i + paramInt1);
        i += 1;
      }
      Arrays.sort(arrayOfInt1);
      int[] arrayOfInt3 = new int[this.shuffled.length + paramInt2];
      int k = 0;
      int j = 0;
      i = 0;
      if (i < this.shuffled.length + paramInt2)
      {
        if ((j < paramInt2) && (k == arrayOfInt1[j]))
        {
          arrayOfInt3[i] = arrayOfInt2[j];
          j += 1;
        }
        for (;;)
        {
          i += 1;
          break;
          arrayOfInt3[i] = this.shuffled[k];
          if (arrayOfInt3[i] >= paramInt1) {
            arrayOfInt3[i] += paramInt2;
          }
          k += 1;
        }
      }
      return new DefaultShuffleOrder(arrayOfInt3, new Random(this.random.nextLong()));
    }
    
    public ShuffleOrder cloneAndRemove(int paramInt)
    {
      int[] arrayOfInt = new int[this.shuffled.length - 1];
      int j = 0;
      int i = 0;
      while (i < this.shuffled.length) {
        if (this.shuffled[i] == paramInt)
        {
          j = 1;
          i += 1;
        }
        else
        {
          int k;
          if (j != 0)
          {
            k = i - 1;
            label52:
            if (this.shuffled[i] <= paramInt) {
              break label88;
            }
          }
          label88:
          for (int m = this.shuffled[i] - 1;; m = this.shuffled[i])
          {
            arrayOfInt[k] = m;
            break;
            k = i;
            break label52;
          }
        }
      }
      return new DefaultShuffleOrder(arrayOfInt, new Random(this.random.nextLong()));
    }
    
    public int getFirstIndex()
    {
      if (this.shuffled.length > 0) {
        return this.shuffled[0];
      }
      return -1;
    }
    
    public int getLastIndex()
    {
      if (this.shuffled.length > 0) {
        return this.shuffled[(this.shuffled.length - 1)];
      }
      return -1;
    }
    
    public int getLength()
    {
      return this.shuffled.length;
    }
    
    public int getNextIndex(int paramInt)
    {
      paramInt = this.indexInShuffled[paramInt] + 1;
      if (paramInt < this.shuffled.length) {
        return this.shuffled[paramInt];
      }
      return -1;
    }
    
    public int getPreviousIndex(int paramInt)
    {
      paramInt = this.indexInShuffled[paramInt] - 1;
      if (paramInt >= 0) {
        return this.shuffled[paramInt];
      }
      return -1;
    }
  }
  
  public static final class UnshuffledShuffleOrder
    implements ShuffleOrder
  {
    private final int length;
    
    public UnshuffledShuffleOrder(int paramInt)
    {
      this.length = paramInt;
    }
    
    public ShuffleOrder cloneAndInsert(int paramInt1, int paramInt2)
    {
      return new UnshuffledShuffleOrder(this.length + paramInt2);
    }
    
    public ShuffleOrder cloneAndRemove(int paramInt)
    {
      return new UnshuffledShuffleOrder(this.length - 1);
    }
    
    public int getFirstIndex()
    {
      if (this.length > 0) {
        return 0;
      }
      return -1;
    }
    
    public int getLastIndex()
    {
      if (this.length > 0) {
        return this.length - 1;
      }
      return -1;
    }
    
    public int getLength()
    {
      return this.length;
    }
    
    public int getNextIndex(int paramInt)
    {
      paramInt += 1;
      if (paramInt < this.length) {
        return paramInt;
      }
      return -1;
    }
    
    public int getPreviousIndex(int paramInt)
    {
      paramInt -= 1;
      if (paramInt >= 0) {
        return paramInt;
      }
      return -1;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/ShuffleOrder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */