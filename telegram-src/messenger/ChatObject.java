package org.telegram.messenger;

import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.TL_channel;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelForbidden;
import org.telegram.tgnet.TLRPC.TL_chatEmpty;
import org.telegram.tgnet.TLRPC.TL_chatForbidden;

public class ChatObject
{
  public static final int CHAT_TYPE_BROADCAST = 1;
  public static final int CHAT_TYPE_CHANNEL = 2;
  public static final int CHAT_TYPE_CHAT = 0;
  public static final int CHAT_TYPE_MEGAGROUP = 4;
  public static final int CHAT_TYPE_USER = 3;
  
  public static boolean canAddAdmins(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.add_admins)));
  }
  
  public static boolean canAddUsers(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.invite_users)));
  }
  
  public static boolean canAddViaLink(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.invite_link)));
  }
  
  public static boolean canBlockUsers(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.ban_users)));
  }
  
  public static boolean canChangeChatInfo(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.change_info)));
  }
  
  public static boolean canEditInfo(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.change_info)));
  }
  
  public static boolean canPost(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.post_messages)));
  }
  
  public static boolean canSendEmbed(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat != null) && ((paramChat.banned_rights == null) || ((!paramChat.banned_rights.send_media) && (!paramChat.banned_rights.embed_links))));
  }
  
  public static boolean canSendMessages(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat != null) && ((paramChat.banned_rights == null) || (!paramChat.banned_rights.send_messages)));
  }
  
  public static boolean canSendStickers(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat != null) && ((paramChat.banned_rights == null) || ((!paramChat.banned_rights.send_media) && (!paramChat.banned_rights.send_stickers))));
  }
  
  public static boolean canWriteToChat(TLRPC.Chat paramChat)
  {
    return (!isChannel(paramChat)) || (paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.post_messages)) || (!paramChat.broadcast);
  }
  
  public static TLRPC.Chat getChatByDialog(long paramLong, int paramInt)
  {
    int i = (int)paramLong;
    int j = (int)(paramLong >> 32);
    if (i < 0) {
      return MessagesController.getInstance(paramInt).getChat(Integer.valueOf(-i));
    }
    return null;
  }
  
  public static boolean hasAdminRights(TLRPC.Chat paramChat)
  {
    return (paramChat != null) && ((paramChat.creator) || ((paramChat.admin_rights != null) && (paramChat.admin_rights.flags != 0)));
  }
  
  public static boolean isCanWriteToChannel(int paramInt1, int paramInt2)
  {
    TLRPC.Chat localChat = MessagesController.getInstance(paramInt2).getChat(Integer.valueOf(paramInt1));
    return (localChat != null) && ((localChat.creator) || ((localChat.admin_rights != null) && (localChat.admin_rights.post_messages)) || (localChat.megagroup));
  }
  
  public static boolean isChannel(int paramInt1, int paramInt2)
  {
    TLRPC.Chat localChat = MessagesController.getInstance(paramInt2).getChat(Integer.valueOf(paramInt1));
    return ((localChat instanceof TLRPC.TL_channel)) || ((localChat instanceof TLRPC.TL_channelForbidden));
  }
  
  public static boolean isChannel(TLRPC.Chat paramChat)
  {
    return ((paramChat instanceof TLRPC.TL_channel)) || ((paramChat instanceof TLRPC.TL_channelForbidden));
  }
  
  public static boolean isKickedFromChat(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat instanceof TLRPC.TL_chatEmpty)) || ((paramChat instanceof TLRPC.TL_chatForbidden)) || ((paramChat instanceof TLRPC.TL_channelForbidden)) || (paramChat.kicked) || (paramChat.deactivated) || ((paramChat.banned_rights != null) && (paramChat.banned_rights.view_messages));
  }
  
  public static boolean isLeftFromChat(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat instanceof TLRPC.TL_chatEmpty)) || ((paramChat instanceof TLRPC.TL_chatForbidden)) || ((paramChat instanceof TLRPC.TL_channelForbidden)) || (paramChat.left) || (paramChat.deactivated);
  }
  
  public static boolean isMegagroup(TLRPC.Chat paramChat)
  {
    return (((paramChat instanceof TLRPC.TL_channel)) || ((paramChat instanceof TLRPC.TL_channelForbidden))) && (paramChat.megagroup);
  }
  
  public static boolean isNotInChat(TLRPC.Chat paramChat)
  {
    return (paramChat == null) || ((paramChat instanceof TLRPC.TL_chatEmpty)) || ((paramChat instanceof TLRPC.TL_chatForbidden)) || ((paramChat instanceof TLRPC.TL_channelForbidden)) || (paramChat.left) || (paramChat.kicked) || (paramChat.deactivated);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/ChatObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */