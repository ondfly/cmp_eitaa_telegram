package ir.eitaa.messenger;

import ir.eitaa.tgnet.TLRPC.TL_dialog;

public class DialogObject
{
  public static boolean isChannel(TLRPC.TL_dialog paramTL_dialog)
  {
    return (paramTL_dialog != null) && ((paramTL_dialog.flags & 0x1) != 0);
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/messenger/DialogObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */