package org.telegram.messenger.exoplayer2.util;

import android.util.Pair;

public abstract interface ErrorMessageProvider<T extends Exception>
{
  public abstract Pair<Integer, String> getErrorMessage(T paramT);
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/util/ErrorMessageProvider.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */