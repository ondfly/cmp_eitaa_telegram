package org.telegram.messenger.exoplayer2.source;

public final class DefaultCompositeSequenceableLoaderFactory
  implements CompositeSequenceableLoaderFactory
{
  public SequenceableLoader createCompositeSequenceableLoader(SequenceableLoader... paramVarArgs)
  {
    return new CompositeSequenceableLoader(paramVarArgs);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/exoplayer2/source/DefaultCompositeSequenceableLoaderFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */