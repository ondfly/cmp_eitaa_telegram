package org.telegram.messenger;

import android.graphics.Point;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.SparseArray;
import java.io.File;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.ChannelAdminLogEventAction;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.InputStickerSet;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.KeyboardButton;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Page;
import org.telegram.tgnet.TLRPC.PageBlock;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.ReplyMarkup;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEvent;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangeAbout;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangePhoto;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangeStickerSet;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangeTitle;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionChangeUsername;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionDeleteMessage;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionEditMessage;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionParticipantInvite;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionParticipantJoin;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionParticipantLeave;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionParticipantToggleAdmin;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionParticipantToggleBan;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionToggleInvites;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionTogglePreHistoryHidden;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionToggleSignatures;
import org.telegram.tgnet.TLRPC.TL_channelAdminLogEventActionUpdatePinned;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionScreenshotMessages;
import org.telegram.tgnet.TLRPC.TL_decryptedMessageActionSetMessageTTL;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAnimated;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeImageSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_documentAttributeVideo;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_game;
import org.telegram.tgnet.TLRPC.TL_inputMessageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetEmpty;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonBuy;
import org.telegram.tgnet.TLRPC.TL_keyboardButtonRow;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionBotAllowed;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelMigrateFrom;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeletePhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditPhoto;
import org.telegram.tgnet.TLRPC.TL_messageActionChatEditTitle;
import org.telegram.tgnet.TLRPC.TL_messageActionChatJoinedByLink;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionCreatedBroadcastList;
import org.telegram.tgnet.TLRPC.TL_messageActionCustomAction;
import org.telegram.tgnet.TLRPC.TL_messageActionEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionGameScore;
import org.telegram.tgnet.TLRPC.TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC.TL_messageActionLoginUnknownLocation;
import org.telegram.tgnet.TLRPC.TL_messageActionPaymentSent;
import org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messageActionPinMessage;
import org.telegram.tgnet.TLRPC.TL_messageActionScreenshotTaken;
import org.telegram.tgnet.TLRPC.TL_messageActionTTLChange;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageActionUserUpdatedPhoto;
import org.telegram.tgnet.TLRPC.TL_messageEmpty;
import org.telegram.tgnet.TLRPC.TL_messageEncryptedAction;
import org.telegram.tgnet.TLRPC.TL_messageEntityBold;
import org.telegram.tgnet.TLRPC.TL_messageEntityBotCommand;
import org.telegram.tgnet.TLRPC.TL_messageEntityCashtag;
import org.telegram.tgnet.TLRPC.TL_messageEntityCode;
import org.telegram.tgnet.TLRPC.TL_messageEntityEmail;
import org.telegram.tgnet.TLRPC.TL_messageEntityHashtag;
import org.telegram.tgnet.TLRPC.TL_messageEntityItalic;
import org.telegram.tgnet.TLRPC.TL_messageEntityMention;
import org.telegram.tgnet.TLRPC.TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageEntityPhone;
import org.telegram.tgnet.TLRPC.TL_messageEntityPre;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageForwarded_old2;
import org.telegram.tgnet.TLRPC.TL_messageMediaContact;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument_layer68;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument_layer74;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument_old;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaGame;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeo;
import org.telegram.tgnet.TLRPC.TL_messageMediaGeoLive;
import org.telegram.tgnet.TLRPC.TL_messageMediaInvoice;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto_layer68;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto_layer74;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto_old;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_message_secret;
import org.telegram.tgnet.TLRPC.TL_pageBlockCollage;
import org.telegram.tgnet.TLRPC.TL_pageBlockPhoto;
import org.telegram.tgnet.TLRPC.TL_pageBlockSlideshow;
import org.telegram.tgnet.TLRPC.TL_pageBlockVideo;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_photoSize;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.tgnet.TLRPC.TL_replyInlineMarkup;
import org.telegram.tgnet.TLRPC.TL_webDocument;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebDocument;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanBotCommand;
import org.telegram.ui.Components.URLSpanBrowser;
import org.telegram.ui.Components.URLSpanMono;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.Components.URLSpanNoUnderlineBold;
import org.telegram.ui.Components.URLSpanReplacement;
import org.telegram.ui.Components.URLSpanUserMention;

public class MessageObject
{
  private static final int LINES_PER_BLOCK = 10;
  public static final int MESSAGE_SEND_STATE_SENDING = 1;
  public static final int MESSAGE_SEND_STATE_SEND_ERROR = 2;
  public static final int MESSAGE_SEND_STATE_SENT = 0;
  public static final int POSITION_FLAG_BOTTOM = 8;
  public static final int POSITION_FLAG_LEFT = 1;
  public static final int POSITION_FLAG_RIGHT = 2;
  public static final int POSITION_FLAG_TOP = 4;
  public static Pattern urlPattern;
  public boolean attachPathExists;
  public int audioPlayerDuration;
  public float audioProgress;
  public int audioProgressSec;
  public StringBuilder botButtonsLayout;
  public float bufferedProgress;
  public CharSequence caption;
  public int contentType;
  public int currentAccount;
  public TLRPC.TL_channelAdminLogEvent currentEvent;
  public String customReplyName;
  public String dateKey;
  public boolean deleted;
  public long eventId;
  public boolean forceUpdate;
  private int generatedWithMinSize;
  public float gifState;
  public boolean hasRtl;
  public boolean isDateObject;
  private int isRoundVideoCached;
  public int lastLineWidth;
  private boolean layoutCreated;
  public int linesCount;
  public CharSequence linkDescription;
  public boolean localChannel;
  public long localGroupId;
  public String localName;
  public int localType;
  public String localUserName;
  public boolean mediaExists;
  public TLRPC.Message messageOwner;
  public CharSequence messageText;
  public String monthKey;
  public ArrayList<TLRPC.PhotoSize> photoThumbs;
  public ArrayList<TLRPC.PhotoSize> photoThumbs2;
  public MessageObject replyMessageObject;
  public boolean resendAsIs;
  public int textHeight;
  public ArrayList<TextLayoutBlock> textLayoutBlocks;
  public int textWidth;
  public float textXOffset;
  public int type = 1000;
  public boolean useCustomPhoto;
  public VideoEditedInfo videoEditedInfo;
  public boolean viewsReloaded;
  public int wantedBotKeyboardWidth;
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, SparseArray<TLRPC.User> paramSparseArray, SparseArray<TLRPC.Chat> paramSparseArray1, boolean paramBoolean)
  {
    this(paramInt, paramMessage, null, null, paramSparseArray, paramSparseArray1, paramBoolean, 0L);
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, SparseArray<TLRPC.User> paramSparseArray, boolean paramBoolean)
  {
    this(paramInt, paramMessage, paramSparseArray, null, paramBoolean);
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, String paramString1, String paramString2, String paramString3, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramBoolean1) {}
    for (int i = 2;; i = 1)
    {
      this.localType = i;
      this.currentAccount = paramInt;
      this.localName = paramString2;
      this.localUserName = paramString3;
      this.messageText = paramString1;
      this.messageOwner = paramMessage;
      this.localChannel = paramBoolean2;
      return;
    }
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, AbstractMap<Integer, TLRPC.User> paramAbstractMap, AbstractMap<Integer, TLRPC.Chat> paramAbstractMap1, SparseArray<TLRPC.User> paramSparseArray, SparseArray<TLRPC.Chat> paramSparseArray1, boolean paramBoolean, long paramLong)
  {
    Theme.createChatResources(null, true);
    this.currentAccount = paramInt;
    this.messageOwner = paramMessage;
    this.eventId = paramLong;
    if (paramMessage.replyMessage != null) {
      this.replyMessageObject = new MessageObject(paramInt, paramMessage.replyMessage, paramAbstractMap, paramAbstractMap1, paramSparseArray, paramSparseArray1, false, paramLong);
    }
    Object localObject1 = null;
    Object localObject2 = null;
    if (paramMessage.from_id > 0)
    {
      if (paramAbstractMap == null) {
        break label576;
      }
      localObject2 = (TLRPC.User)paramAbstractMap.get(Integer.valueOf(paramMessage.from_id));
      localObject1 = localObject2;
      if (localObject2 == null) {
        localObject1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramMessage.from_id));
      }
    }
    label166:
    int i;
    int j;
    if ((paramMessage instanceof TLRPC.TL_messageService))
    {
      localObject2 = localObject1;
      if (paramMessage.action != null)
      {
        if ((paramMessage.action instanceof TLRPC.TL_messageActionCustomAction))
        {
          this.messageText = paramMessage.action.message;
          localObject2 = localObject1;
        }
      }
      else
      {
        if (this.messageText == null) {
          this.messageText = "";
        }
        setType();
        measureInlineBotButtons();
        paramMessage = new GregorianCalendar();
        paramMessage.setTimeInMillis(this.messageOwner.date * 1000L);
        paramInt = paramMessage.get(6);
        i = paramMessage.get(1);
        j = paramMessage.get(2);
        this.dateKey = String.format("%d_%02d_%02d", new Object[] { Integer.valueOf(i), Integer.valueOf(j), Integer.valueOf(paramInt) });
        this.monthKey = String.format("%d_%02d", new Object[] { Integer.valueOf(i), Integer.valueOf(j) });
        if ((this.messageOwner.message != null) && (this.messageOwner.id < 0) && (this.messageOwner.params != null))
        {
          paramMessage = (String)this.messageOwner.params.get("ve");
          if ((paramMessage != null) && ((isVideo()) || (isNewGif()) || (isRoundVideo())))
          {
            this.videoEditedInfo = new VideoEditedInfo();
            if (this.videoEditedInfo.parseString(paramMessage)) {
              break label4243;
            }
            this.videoEditedInfo = null;
          }
        }
        label395:
        generateCaption();
        if (!paramBoolean) {
          break label4303;
        }
        if (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) {
          break label4257;
        }
        paramMessage = Theme.chat_msgGameTextPaint;
        label421:
        if (!SharedConfig.allowBigEmoji) {
          break label4264;
        }
        paramAbstractMap = new int[1];
        label431:
        this.messageText = Emoji.replaceEmoji(this.messageText, paramMessage.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false, paramAbstractMap);
        if ((paramAbstractMap == null) || (paramAbstractMap[0] < 1) || (paramAbstractMap[0] > 3)) {
          break label4297;
        }
        switch (paramAbstractMap[0])
        {
        default: 
          paramMessage = Theme.chat_msgTextPaintThreeEmoji;
          paramInt = AndroidUtilities.dp(24.0F);
        }
      }
    }
    for (;;)
    {
      paramAbstractMap = (Emoji.EmojiSpan[])((Spannable)this.messageText).getSpans(0, this.messageText.length(), Emoji.EmojiSpan.class);
      if ((paramAbstractMap == null) || (paramAbstractMap.length <= 0)) {
        break label4297;
      }
      i = 0;
      while (i < paramAbstractMap.length)
      {
        paramAbstractMap[i].replaceFontMetrics(paramMessage.getFontMetricsInt(), paramInt);
        i += 1;
      }
      label576:
      if (paramSparseArray == null) {
        break;
      }
      localObject2 = (TLRPC.User)paramSparseArray.get(paramMessage.from_id);
      break;
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatCreate))
      {
        if (isOut())
        {
          this.messageText = LocaleController.getString("ActionYouCreateGroup", 2131492914);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionCreateGroup", 2131492883), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatDeleteUser))
      {
        if (paramMessage.action.user_id == paramMessage.from_id)
        {
          if (isOut())
          {
            this.messageText = LocaleController.getString("ActionYouLeftUser", 2131492916);
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = replaceWithLink(LocaleController.getString("ActionLeftUser", 2131492889), "un1", (TLObject)localObject1);
          localObject2 = localObject1;
          break label166;
        }
        paramAbstractMap1 = null;
        if (paramAbstractMap != null) {
          paramAbstractMap = (TLRPC.User)paramAbstractMap.get(Integer.valueOf(paramMessage.action.user_id));
        }
        for (;;)
        {
          paramAbstractMap1 = paramAbstractMap;
          if (paramAbstractMap == null) {
            paramAbstractMap1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramMessage.action.user_id));
          }
          if (!isOut()) {
            break label858;
          }
          this.messageText = replaceWithLink(LocaleController.getString("ActionYouKickUser", 2131492915), "un2", paramAbstractMap1);
          localObject2 = localObject1;
          break;
          paramAbstractMap = paramAbstractMap1;
          if (paramSparseArray != null) {
            paramAbstractMap = (TLRPC.User)paramSparseArray.get(paramMessage.action.user_id);
          }
        }
        label858:
        if (paramMessage.action.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId())
        {
          this.messageText = replaceWithLink(LocaleController.getString("ActionKickUserYou", 2131492888), "un1", (TLObject)localObject1);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionKickUser", 2131492887), "un2", paramAbstractMap1);
        this.messageText = replaceWithLink(this.messageText, "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatAddUser))
      {
        j = this.messageOwner.action.user_id;
        i = j;
        if (j == 0)
        {
          i = j;
          if (this.messageOwner.action.users.size() == 1) {
            i = ((Integer)this.messageOwner.action.users.get(0)).intValue();
          }
        }
        if (i != 0)
        {
          paramAbstractMap1 = null;
          if (paramAbstractMap != null) {
            paramAbstractMap = (TLRPC.User)paramAbstractMap.get(Integer.valueOf(i));
          }
          for (;;)
          {
            paramAbstractMap1 = paramAbstractMap;
            if (paramAbstractMap == null) {
              paramAbstractMap1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(i));
            }
            if (i != paramMessage.from_id) {
              break label1278;
            }
            if ((paramMessage.to_id.channel_id == 0) || (isMegagroup())) {
              break label1141;
            }
            this.messageText = LocaleController.getString("ChannelJoined", 2131493172);
            localObject2 = localObject1;
            break;
            paramAbstractMap = paramAbstractMap1;
            if (paramSparseArray != null) {
              paramAbstractMap = (TLRPC.User)paramSparseArray.get(i);
            }
          }
          label1141:
          if ((paramMessage.to_id.channel_id != 0) && (isMegagroup()))
          {
            if (i == UserConfig.getInstance(this.currentAccount).getClientUserId())
            {
              this.messageText = LocaleController.getString("ChannelMegaJoined", 2131493176);
              localObject2 = localObject1;
              break label166;
            }
            this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserSelfMega", 2131492873), "un1", (TLObject)localObject1);
            localObject2 = localObject1;
            break label166;
          }
          if (isOut())
          {
            this.messageText = LocaleController.getString("ActionAddUserSelfYou", 2131492874);
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserSelf", 2131492872), "un1", (TLObject)localObject1);
          localObject2 = localObject1;
          break label166;
          label1278:
          if (isOut())
          {
            this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", 2131492911), "un2", paramAbstractMap1);
            localObject2 = localObject1;
            break label166;
          }
          if (i == UserConfig.getInstance(this.currentAccount).getClientUserId())
          {
            if (paramMessage.to_id.channel_id != 0)
            {
              if (isMegagroup())
              {
                this.messageText = replaceWithLink(LocaleController.getString("MegaAddedBy", 2131493798), "un1", (TLObject)localObject1);
                localObject2 = localObject1;
                break label166;
              }
              this.messageText = replaceWithLink(LocaleController.getString("ChannelAddedBy", 2131493147), "un1", (TLObject)localObject1);
              localObject2 = localObject1;
              break label166;
            }
            this.messageText = replaceWithLink(LocaleController.getString("ActionAddUserYou", 2131492875), "un1", (TLObject)localObject1);
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", 2131492871), "un2", paramAbstractMap1);
          this.messageText = replaceWithLink(this.messageText, "un1", (TLObject)localObject1);
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = replaceWithLink(LocaleController.getString("ActionYouAddUser", 2131492911), "un2", paramMessage.action.users, paramAbstractMap, paramSparseArray);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionAddUser", 2131492871), "un2", paramMessage.action.users, paramAbstractMap, paramSparseArray);
        this.messageText = replaceWithLink(this.messageText, "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatJoinedByLink))
      {
        if (isOut())
        {
          this.messageText = LocaleController.getString("ActionInviteYou", 2131492886);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionInviteUser", 2131492885), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatEditPhoto))
      {
        if ((paramMessage.to_id.channel_id != 0) && (!isMegagroup()))
        {
          this.messageText = LocaleController.getString("ActionChannelChangedPhoto", 2131492879);
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = LocaleController.getString("ActionYouChangedPhoto", 2131492912);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedPhoto", 2131492877), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatEditTitle))
      {
        if ((paramMessage.to_id.channel_id != 0) && (!isMegagroup()))
        {
          this.messageText = LocaleController.getString("ActionChannelChangedTitle", 2131492880).replace("un2", paramMessage.action.title);
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = LocaleController.getString("ActionYouChangedTitle", 2131492913).replace("un2", paramMessage.action.title);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionChangedTitle", 2131492878).replace("un2", paramMessage.action.title), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatDeletePhoto))
      {
        if ((paramMessage.to_id.channel_id != 0) && (!isMegagroup()))
        {
          this.messageText = LocaleController.getString("ActionChannelRemovedPhoto", 2131492881);
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = LocaleController.getString("ActionYouRemovedPhoto", 2131492917);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionRemovedPhoto", 2131492906), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionTTLChange))
      {
        if (paramMessage.action.ttl != 0)
        {
          if (isOut())
          {
            this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", 2131493820, new Object[] { LocaleController.formatTTLString(paramMessage.action.ttl) });
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = LocaleController.formatString("MessageLifetimeChanged", 2131493819, new Object[] { UserObject.getFirstName((TLRPC.User)localObject1), LocaleController.formatTTLString(paramMessage.action.ttl) });
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", 2131493824);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = LocaleController.formatString("MessageLifetimeRemoved", 2131493822, new Object[] { UserObject.getFirstName((TLRPC.User)localObject1) });
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionLoginUnknownLocation))
      {
        paramLong = paramMessage.date * 1000L;
        if ((LocaleController.getInstance().formatterDay != null) && (LocaleController.getInstance().formatterYear != null))
        {
          paramSparseArray1 = LocaleController.formatString("formatDateAtTime", 2131494696, new Object[] { LocaleController.getInstance().formatterYear.format(paramLong), LocaleController.getInstance().formatterDay.format(paramLong) });
          label2241:
          localObject2 = UserConfig.getInstance(this.currentAccount).getCurrentUser();
          paramAbstractMap1 = (AbstractMap<Integer, TLRPC.Chat>)localObject2;
          if (localObject2 == null)
          {
            if (paramAbstractMap == null) {
              break label2407;
            }
            paramAbstractMap = (TLRPC.User)paramAbstractMap.get(Integer.valueOf(this.messageOwner.to_id.user_id));
            label2287:
            paramAbstractMap1 = paramAbstractMap;
            if (paramAbstractMap == null) {
              paramAbstractMap1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(this.messageOwner.to_id.user_id));
            }
          }
          if (paramAbstractMap1 == null) {
            break label2437;
          }
        }
        label2407:
        label2437:
        for (paramAbstractMap = UserObject.getFirstName(paramAbstractMap1);; paramAbstractMap = "")
        {
          this.messageText = LocaleController.formatString("NotificationUnrecognizedDevice", 2131494003, new Object[] { paramAbstractMap, paramSparseArray1, paramMessage.action.title, paramMessage.action.address });
          localObject2 = localObject1;
          break;
          paramSparseArray1 = "" + paramMessage.date;
          break label2241;
          paramAbstractMap = (AbstractMap<Integer, TLRPC.User>)localObject2;
          if (paramSparseArray == null) {
            break label2287;
          }
          paramAbstractMap = (TLRPC.User)paramSparseArray.get(this.messageOwner.to_id.user_id);
          break label2287;
        }
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionUserJoined))
      {
        this.messageText = LocaleController.formatString("NotificationContactJoined", 2131493952, new Object[] { UserObject.getUserName((TLRPC.User)localObject1) });
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto))
      {
        this.messageText = LocaleController.formatString("NotificationContactNewPhoto", 2131493953, new Object[] { UserObject.getUserName((TLRPC.User)localObject1) });
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageEncryptedAction))
      {
        if ((paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages))
        {
          if (isOut())
          {
            this.messageText = LocaleController.formatString("ActionTakeScreenshootYou", 2131492908, new Object[0]);
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = replaceWithLink(LocaleController.getString("ActionTakeScreenshoot", 2131492907), "un1", (TLObject)localObject1);
          localObject2 = localObject1;
          break label166;
        }
        localObject2 = localObject1;
        if (!(paramMessage.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)) {
          break label166;
        }
        paramMessage = (TLRPC.TL_decryptedMessageActionSetMessageTTL)paramMessage.action.encryptedAction;
        if (paramMessage.ttl_seconds != 0)
        {
          if (isOut())
          {
            this.messageText = LocaleController.formatString("MessageLifetimeChangedOutgoing", 2131493820, new Object[] { LocaleController.formatTTLString(paramMessage.ttl_seconds) });
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = LocaleController.formatString("MessageLifetimeChanged", 2131493819, new Object[] { UserObject.getFirstName((TLRPC.User)localObject1), LocaleController.formatTTLString(paramMessage.ttl_seconds) });
          localObject2 = localObject1;
          break label166;
        }
        if (isOut())
        {
          this.messageText = LocaleController.getString("MessageLifetimeYouRemoved", 2131493824);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = LocaleController.formatString("MessageLifetimeRemoved", 2131493822, new Object[] { UserObject.getFirstName((TLRPC.User)localObject1) });
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionScreenshotTaken))
      {
        if (isOut())
        {
          this.messageText = LocaleController.formatString("ActionTakeScreenshootYou", 2131492908, new Object[0]);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = replaceWithLink(LocaleController.getString("ActionTakeScreenshoot", 2131492907), "un1", (TLObject)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionCreatedBroadcastList))
      {
        this.messageText = LocaleController.formatString("YouCreatedBroadcastList", 2131494656, new Object[0]);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChannelCreate))
      {
        if (isMegagroup())
        {
          this.messageText = LocaleController.getString("ActionCreateMega", 2131492884);
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = LocaleController.getString("ActionCreateChannel", 2131492882);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChatMigrateTo))
      {
        this.messageText = LocaleController.getString("ActionMigrateFromGroup", 2131492890);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionChannelMigrateFrom))
      {
        this.messageText = LocaleController.getString("ActionMigrateFromGroup", 2131492890);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionPinMessage))
      {
        if (localObject1 == null) {
          if (paramAbstractMap1 != null) {
            paramMessage = (TLRPC.Chat)paramAbstractMap1.get(Integer.valueOf(paramMessage.to_id.channel_id));
          }
        }
        for (;;)
        {
          generatePinMessageText((TLRPC.User)localObject1, paramMessage);
          localObject2 = localObject1;
          break;
          if (paramSparseArray1 != null)
          {
            paramMessage = (TLRPC.Chat)paramSparseArray1.get(paramMessage.to_id.channel_id);
          }
          else
          {
            paramMessage = null;
            continue;
            paramMessage = null;
          }
        }
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionHistoryClear))
      {
        this.messageText = LocaleController.getString("HistoryCleared", 2131493650);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionGameScore))
      {
        generateGameMessageText((TLRPC.User)localObject1);
        localObject2 = localObject1;
        break label166;
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionPhoneCall))
      {
        paramMessage = (TLRPC.TL_messageActionPhoneCall)this.messageOwner.action;
        boolean bool = paramMessage.reason instanceof TLRPC.TL_phoneCallDiscardReasonMissed;
        if (this.messageOwner.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
          if (bool) {
            this.messageText = LocaleController.getString("CallMessageOutgoingMissed", 2131493113);
          }
        }
        for (;;)
        {
          localObject2 = localObject1;
          if (paramMessage.duration <= 0) {
            break;
          }
          paramMessage = LocaleController.formatCallDuration(paramMessage.duration);
          this.messageText = LocaleController.formatString("CallMessageWithDuration", 2131493115, new Object[] { this.messageText, paramMessage });
          paramAbstractMap = this.messageText.toString();
          i = paramAbstractMap.indexOf(paramMessage);
          localObject2 = localObject1;
          if (i == -1) {
            break;
          }
          paramAbstractMap1 = new SpannableString(this.messageText);
          j = i + paramMessage.length();
          paramInt = i;
          if (i > 0)
          {
            paramInt = i;
            if (paramAbstractMap.charAt(i - 1) == '(') {
              paramInt = i - 1;
            }
          }
          i = j;
          if (j < paramAbstractMap.length())
          {
            i = j;
            if (paramAbstractMap.charAt(j) == ')') {
              i = j + 1;
            }
          }
          paramAbstractMap1.setSpan(new TypefaceSpan(Typeface.DEFAULT), paramInt, i, 0);
          this.messageText = paramAbstractMap1;
          localObject2 = localObject1;
          break;
          this.messageText = LocaleController.getString("CallMessageOutgoing", 2131493112);
          continue;
          if (bool) {
            this.messageText = LocaleController.getString("CallMessageIncomingMissed", 2131493111);
          } else if ((paramMessage.reason instanceof TLRPC.TL_phoneCallDiscardReasonBusy)) {
            this.messageText = LocaleController.getString("CallMessageIncomingDeclined", 2131493110);
          } else {
            this.messageText = LocaleController.getString("CallMessageIncoming", 2131493109);
          }
        }
      }
      if ((paramMessage.action instanceof TLRPC.TL_messageActionPaymentSent))
      {
        i = (int)getDialogId();
        if (paramAbstractMap != null) {
          localObject1 = (TLRPC.User)paramAbstractMap.get(Integer.valueOf(i));
        }
        for (;;)
        {
          paramMessage = (TLRPC.Message)localObject1;
          if (localObject1 == null) {
            paramMessage = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(i));
          }
          generatePaymentSentMessageText(null);
          localObject2 = paramMessage;
          break;
          if (paramSparseArray != null) {
            localObject1 = (TLRPC.User)paramSparseArray.get(i);
          }
        }
      }
      localObject2 = localObject1;
      if (!(paramMessage.action instanceof TLRPC.TL_messageActionBotAllowed)) {
        break label166;
      }
      paramMessage = ((TLRPC.TL_messageActionBotAllowed)paramMessage.action).domain;
      paramAbstractMap = LocaleController.getString("ActionBotAllowed", 2131492876);
      paramInt = paramAbstractMap.indexOf("%1$s");
      paramAbstractMap = new SpannableString(String.format(paramAbstractMap, new Object[] { paramMessage }));
      if (paramInt >= 0) {
        paramAbstractMap.setSpan(new URLSpanNoUnderlineBold("http://" + paramMessage), paramInt, paramMessage.length() + paramInt, 33);
      }
      this.messageText = paramAbstractMap;
      localObject2 = localObject1;
      break label166;
      if (!isMediaEmpty())
      {
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto))
        {
          this.messageText = LocaleController.getString("AttachPhoto", 2131493037);
          localObject2 = localObject1;
          break label166;
        }
        if ((isVideo()) || (((paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) && ((paramMessage.media.document instanceof TLRPC.TL_documentEmpty)) && (paramMessage.media.ttl_seconds != 0)))
        {
          this.messageText = LocaleController.getString("AttachVideo", 2131493043);
          localObject2 = localObject1;
          break label166;
        }
        if (isVoice())
        {
          this.messageText = LocaleController.getString("AttachAudio", 2131493023);
          localObject2 = localObject1;
          break label166;
        }
        if (isRoundVideo())
        {
          this.messageText = LocaleController.getString("AttachRound", 2131493039);
          localObject2 = localObject1;
          break label166;
        }
        if (((paramMessage.media instanceof TLRPC.TL_messageMediaGeo)) || ((paramMessage.media instanceof TLRPC.TL_messageMediaVenue)))
        {
          this.messageText = LocaleController.getString("AttachLocation", 2131493033);
          localObject2 = localObject1;
          break label166;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaGeoLive))
        {
          this.messageText = LocaleController.getString("AttachLiveLocation", 2131493031);
          localObject2 = localObject1;
          break label166;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaContact))
        {
          this.messageText = LocaleController.getString("AttachContact", 2131493025);
          localObject2 = localObject1;
          break label166;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaGame))
        {
          this.messageText = paramMessage.message;
          localObject2 = localObject1;
          break label166;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaInvoice))
        {
          this.messageText = paramMessage.media.description;
          localObject2 = localObject1;
          break label166;
        }
        if ((paramMessage.media instanceof TLRPC.TL_messageMediaUnsupported))
        {
          this.messageText = LocaleController.getString("UnsupportedMedia", 2131494518);
          localObject2 = localObject1;
          break label166;
        }
        localObject2 = localObject1;
        if (!(paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) {
          break label166;
        }
        if (isSticker())
        {
          paramMessage = getStrickerChar();
          if ((paramMessage != null) && (paramMessage.length() > 0))
          {
            this.messageText = String.format("%s %s", new Object[] { paramMessage, LocaleController.getString("AttachSticker", 2131493040) });
            localObject2 = localObject1;
            break label166;
          }
          this.messageText = LocaleController.getString("AttachSticker", 2131493040);
          localObject2 = localObject1;
          break label166;
        }
        if (isMusic())
        {
          this.messageText = LocaleController.getString("AttachMusic", 2131493036);
          localObject2 = localObject1;
          break label166;
        }
        if (isGif())
        {
          this.messageText = LocaleController.getString("AttachGif", 2131493028);
          localObject2 = localObject1;
          break label166;
        }
        paramMessage = FileLoader.getDocumentFileName(paramMessage.media.document);
        if ((paramMessage != null) && (paramMessage.length() > 0))
        {
          this.messageText = paramMessage;
          localObject2 = localObject1;
          break label166;
        }
        this.messageText = LocaleController.getString("AttachDocument", 2131493026);
        localObject2 = localObject1;
        break label166;
      }
      this.messageText = paramMessage.message;
      localObject2 = localObject1;
      break label166;
      label4243:
      this.videoEditedInfo.roundVideo = isRoundVideo();
      break label395;
      label4257:
      paramMessage = Theme.chat_msgTextPaint;
      break label421;
      label4264:
      paramAbstractMap = null;
      break label431;
      paramMessage = Theme.chat_msgTextPaintOneEmoji;
      paramInt = AndroidUtilities.dp(32.0F);
      continue;
      paramMessage = Theme.chat_msgTextPaintTwoEmoji;
      paramInt = AndroidUtilities.dp(28.0F);
    }
    label4297:
    generateLayout((TLRPC.User)localObject2);
    label4303:
    this.layoutCreated = paramBoolean;
    generateThumbs(false);
    checkMediaExistance();
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, AbstractMap<Integer, TLRPC.User> paramAbstractMap, AbstractMap<Integer, TLRPC.Chat> paramAbstractMap1, boolean paramBoolean)
  {
    this(paramInt, paramMessage, paramAbstractMap, paramAbstractMap1, paramBoolean, 0L);
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, AbstractMap<Integer, TLRPC.User> paramAbstractMap, AbstractMap<Integer, TLRPC.Chat> paramAbstractMap1, boolean paramBoolean, long paramLong)
  {
    this(paramInt, paramMessage, paramAbstractMap, paramAbstractMap1, null, null, paramBoolean, paramLong);
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, AbstractMap<Integer, TLRPC.User> paramAbstractMap, boolean paramBoolean)
  {
    this(paramInt, paramMessage, paramAbstractMap, null, paramBoolean);
  }
  
  public MessageObject(int paramInt, TLRPC.Message paramMessage, boolean paramBoolean)
  {
    this(paramInt, paramMessage, null, null, null, null, paramBoolean, 0L);
  }
  
  public MessageObject(int paramInt, TLRPC.TL_channelAdminLogEvent paramTL_channelAdminLogEvent, ArrayList<MessageObject> paramArrayList, HashMap<String, ArrayList<MessageObject>> paramHashMap, TLRPC.Chat paramChat, int[] paramArrayOfInt)
  {
    Object localObject1 = null;
    Object localObject2 = localObject1;
    if (paramTL_channelAdminLogEvent.user_id > 0)
    {
      localObject2 = localObject1;
      if (0 == 0) {
        localObject2 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramTL_channelAdminLogEvent.user_id));
      }
    }
    this.currentEvent = paramTL_channelAdminLogEvent;
    localObject1 = new GregorianCalendar();
    ((Calendar)localObject1).setTimeInMillis(paramTL_channelAdminLogEvent.date * 1000L);
    int i = ((Calendar)localObject1).get(6);
    int j = ((Calendar)localObject1).get(1);
    int k = ((Calendar)localObject1).get(2);
    this.dateKey = String.format("%d_%02d_%02d", new Object[] { Integer.valueOf(j), Integer.valueOf(k), Integer.valueOf(i) });
    this.monthKey = String.format("%d_%02d", new Object[] { Integer.valueOf(j), Integer.valueOf(k) });
    Object localObject3 = new TLRPC.TL_peerChannel();
    ((TLRPC.Peer)localObject3).channel_id = paramChat.id;
    Object localObject5 = null;
    Object localObject4;
    if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeTitle))
    {
      localObject1 = ((TLRPC.TL_channelAdminLogEventActionChangeTitle)paramTL_channelAdminLogEvent.action).new_value;
      if (paramChat.megagroup)
      {
        this.messageText = replaceWithLink(LocaleController.formatString("EventLogEditedGroupTitle", 2131493473, new Object[] { localObject1 }), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        if (this.messageOwner == null) {
          this.messageOwner = new TLRPC.TL_messageService();
        }
        this.messageOwner.message = this.messageText.toString();
        this.messageOwner.from_id = paramTL_channelAdminLogEvent.user_id;
        this.messageOwner.date = paramTL_channelAdminLogEvent.date;
        localObject3 = this.messageOwner;
        i = paramArrayOfInt[0];
        paramArrayOfInt[0] = (i + 1);
        ((TLRPC.Message)localObject3).id = i;
        this.eventId = paramTL_channelAdminLogEvent.id;
        this.messageOwner.out = false;
        this.messageOwner.to_id = new TLRPC.TL_peerChannel();
        this.messageOwner.to_id.channel_id = paramChat.id;
        this.messageOwner.unread = false;
        if (paramChat.megagroup)
        {
          localObject3 = this.messageOwner;
          ((TLRPC.Message)localObject3).flags |= 0x80000000;
        }
        localObject4 = MediaController.getInstance();
        localObject3 = localObject1;
        if (paramTL_channelAdminLogEvent.action.message != null)
        {
          localObject3 = localObject1;
          if (!(paramTL_channelAdminLogEvent.action.message instanceof TLRPC.TL_messageEmpty)) {
            localObject3 = paramTL_channelAdminLogEvent.action.message;
          }
        }
        if (localObject3 != null)
        {
          ((TLRPC.Message)localObject3).out = false;
          i = paramArrayOfInt[0];
          paramArrayOfInt[0] = (i + 1);
          ((TLRPC.Message)localObject3).id = i;
          ((TLRPC.Message)localObject3).reply_to_msg_id = 0;
          ((TLRPC.Message)localObject3).flags &= 0xFFFF7FFF;
          if (paramChat.megagroup) {
            ((TLRPC.Message)localObject3).flags |= 0x80000000;
          }
          paramChat = new MessageObject(paramInt, (TLRPC.Message)localObject3, null, null, true, this.eventId);
          if (paramChat.contentType < 0) {
            break label5208;
          }
          if (((MediaController)localObject4).isPlayingMessage(paramChat))
          {
            paramArrayOfInt = ((MediaController)localObject4).getPlayingMessageObject();
            paramChat.audioProgress = paramArrayOfInt.audioProgress;
            paramChat.audioProgressSec = paramArrayOfInt.audioProgressSec;
          }
          createDateArray(paramInt, paramTL_channelAdminLogEvent, paramArrayList, paramHashMap);
          paramArrayList.add(paramArrayList.size() - 1, paramChat);
        }
        label623:
        if (this.contentType < 0) {
          return;
        }
        createDateArray(paramInt, paramTL_channelAdminLogEvent, paramArrayList, paramHashMap);
        paramArrayList.add(paramArrayList.size() - 1, this);
        if (this.messageText == null) {
          this.messageText = "";
        }
        setType();
        measureInlineBotButtons();
        if ((this.messageOwner.message != null) && (this.messageOwner.id < 0) && (this.messageOwner.message.length() > 6) && ((isVideo()) || (isNewGif()) || (isRoundVideo())))
        {
          this.videoEditedInfo = new VideoEditedInfo();
          if (this.videoEditedInfo.parseString(this.messageOwner.message)) {
            break label5216;
          }
          this.videoEditedInfo = null;
        }
        label760:
        generateCaption();
        if (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) {
          break label5230;
        }
        paramTL_channelAdminLogEvent = Theme.chat_msgGameTextPaint;
        label781:
        if (!SharedConfig.allowBigEmoji) {
          break label5237;
        }
        paramArrayList = new int[1];
        label791:
        this.messageText = Emoji.replaceEmoji(this.messageText, paramTL_channelAdminLogEvent.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false, paramArrayList);
        if ((paramArrayList == null) || (paramArrayList[0] < 1) || (paramArrayList[0] > 3)) {
          break label5270;
        }
        switch (paramArrayList[0])
        {
        default: 
          paramTL_channelAdminLogEvent = Theme.chat_msgTextPaintThreeEmoji;
          paramInt = AndroidUtilities.dp(24.0F);
        }
      }
    }
    for (;;)
    {
      paramArrayList = (Emoji.EmojiSpan[])((Spannable)this.messageText).getSpans(0, this.messageText.length(), Emoji.EmojiSpan.class);
      if ((paramArrayList == null) || (paramArrayList.length <= 0)) {
        break label5270;
      }
      i = 0;
      while (i < paramArrayList.length)
      {
        paramArrayList[i].replaceFontMetrics(paramTL_channelAdminLogEvent.getFontMetricsInt(), paramInt);
        i += 1;
      }
      this.messageText = replaceWithLink(LocaleController.formatString("EventLogEditedChannelTitle", 2131493470, new Object[] { localObject1 }), "un1", (TLObject)localObject2);
      localObject1 = localObject5;
      break;
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangePhoto))
      {
        this.messageOwner = new TLRPC.TL_messageService();
        if ((paramTL_channelAdminLogEvent.action.new_photo instanceof TLRPC.TL_chatPhotoEmpty))
        {
          this.messageOwner.action = new TLRPC.TL_messageActionChatDeletePhoto();
          if (paramChat.megagroup)
          {
            this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedWGroupPhoto", 2131493515), "un1", (TLObject)localObject2);
            localObject1 = localObject5;
            break;
          }
          this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedChannelPhoto", 2131493512), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageOwner.action = new TLRPC.TL_messageActionChatEditPhoto();
        this.messageOwner.action.photo = new TLRPC.TL_photo();
        localObject1 = new TLRPC.TL_photoSize();
        ((TLRPC.TL_photoSize)localObject1).location = paramTL_channelAdminLogEvent.action.new_photo.photo_small;
        ((TLRPC.TL_photoSize)localObject1).type = "s";
        ((TLRPC.TL_photoSize)localObject1).h = 80;
        ((TLRPC.TL_photoSize)localObject1).w = 80;
        this.messageOwner.action.photo.sizes.add(localObject1);
        localObject1 = new TLRPC.TL_photoSize();
        ((TLRPC.TL_photoSize)localObject1).location = paramTL_channelAdminLogEvent.action.new_photo.photo_big;
        ((TLRPC.TL_photoSize)localObject1).type = "m";
        ((TLRPC.TL_photoSize)localObject1).h = 640;
        ((TLRPC.TL_photoSize)localObject1).w = 640;
        this.messageOwner.action.photo.sizes.add(localObject1);
        if (paramChat.megagroup)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedGroupPhoto", 2131493472), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedChannelPhoto", 2131493469), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionParticipantJoin))
      {
        if (paramChat.megagroup)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogGroupJoined", 2131493489), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogChannelJoined", 2131493463), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionParticipantLeave))
      {
        this.messageOwner = new TLRPC.TL_messageService();
        this.messageOwner.action = new TLRPC.TL_messageActionChatDeleteUser();
        this.messageOwner.action.user_id = paramTL_channelAdminLogEvent.user_id;
        if (paramChat.megagroup)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogLeftGroup", 2131493494), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogLeftChannel", 2131493493), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionParticipantInvite))
      {
        this.messageOwner = new TLRPC.TL_messageService();
        this.messageOwner.action = new TLRPC.TL_messageActionChatAddUser();
        localObject1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramTL_channelAdminLogEvent.action.participant.user_id));
        if (paramTL_channelAdminLogEvent.action.participant.user_id == this.messageOwner.from_id)
        {
          if (paramChat.megagroup)
          {
            this.messageText = replaceWithLink(LocaleController.getString("EventLogGroupJoined", 2131493489), "un1", (TLObject)localObject2);
            localObject1 = localObject5;
            break;
          }
          this.messageText = replaceWithLink(LocaleController.getString("EventLogChannelJoined", 2131493463), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogAdded", 2131493457), "un2", (TLObject)localObject1);
        this.messageText = replaceWithLink(this.messageText, "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      Object localObject6;
      char c;
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionParticipantToggleAdmin))
      {
        this.messageOwner = new TLRPC.TL_message();
        localObject1 = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramTL_channelAdminLogEvent.action.prev_participant.user_id));
        localObject3 = LocaleController.getString("EventLogPromoted", 2131493501);
        i = ((String)localObject3).indexOf("%1$s");
        localObject6 = new StringBuilder(String.format((String)localObject3, new Object[] { getUserName((TLRPC.User)localObject1, this.messageOwner.entities, i) }));
        ((StringBuilder)localObject6).append("\n");
        localObject3 = paramTL_channelAdminLogEvent.action.prev_participant.admin_rights;
        localObject4 = paramTL_channelAdminLogEvent.action.new_participant.admin_rights;
        localObject1 = localObject3;
        if (localObject3 == null) {
          localObject1 = new TLRPC.TL_channelAdminRights();
        }
        localObject3 = localObject4;
        if (localObject4 == null) {
          localObject3 = new TLRPC.TL_channelAdminRights();
        }
        if (((TLRPC.TL_channelAdminRights)localObject1).change_info != ((TLRPC.TL_channelAdminRights)localObject3).change_info)
        {
          localObject4 = ((StringBuilder)localObject6).append('\n');
          if (((TLRPC.TL_channelAdminRights)localObject3).change_info)
          {
            c = '+';
            label1900:
            ((StringBuilder)localObject4).append(c).append(' ');
            if (!paramChat.megagroup) {
              break label2421;
            }
            localObject4 = LocaleController.getString("EventLogPromotedChangeGroupInfo", 2131493506);
            label1932:
            ((StringBuilder)localObject6).append((String)localObject4);
          }
        }
        else
        {
          if (!paramChat.megagroup)
          {
            if (((TLRPC.TL_channelAdminRights)localObject1).post_messages != ((TLRPC.TL_channelAdminRights)localObject3).post_messages)
            {
              localObject4 = ((StringBuilder)localObject6).append('\n');
              if (!((TLRPC.TL_channelAdminRights)localObject3).post_messages) {
                break label2435;
              }
              c = '+';
              label1982:
              ((StringBuilder)localObject4).append(c).append(' ');
              ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedPostMessages", 2131493510));
            }
            if (((TLRPC.TL_channelAdminRights)localObject1).edit_messages != ((TLRPC.TL_channelAdminRights)localObject3).edit_messages)
            {
              localObject4 = ((StringBuilder)localObject6).append('\n');
              if (!((TLRPC.TL_channelAdminRights)localObject3).edit_messages) {
                break label2442;
              }
              c = '+';
              label2044:
              ((StringBuilder)localObject4).append(c).append(' ');
              ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedEditMessages", 2131493508));
            }
          }
          if (((TLRPC.TL_channelAdminRights)localObject1).delete_messages != ((TLRPC.TL_channelAdminRights)localObject3).delete_messages)
          {
            localObject4 = ((StringBuilder)localObject6).append('\n');
            if (!((TLRPC.TL_channelAdminRights)localObject3).delete_messages) {
              break label2449;
            }
            c = '+';
            label2106:
            ((StringBuilder)localObject4).append(c).append(' ');
            ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedDeleteMessages", 2131493507));
          }
          if (((TLRPC.TL_channelAdminRights)localObject1).add_admins != ((TLRPC.TL_channelAdminRights)localObject3).add_admins)
          {
            localObject4 = ((StringBuilder)localObject6).append('\n');
            if (!((TLRPC.TL_channelAdminRights)localObject3).add_admins) {
              break label2456;
            }
            c = '+';
            label2168:
            ((StringBuilder)localObject4).append(c).append(' ');
            ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedAddAdmins", 2131493502));
          }
          if ((paramChat.megagroup) && (((TLRPC.TL_channelAdminRights)localObject1).ban_users != ((TLRPC.TL_channelAdminRights)localObject3).ban_users))
          {
            localObject4 = ((StringBuilder)localObject6).append('\n');
            if (!((TLRPC.TL_channelAdminRights)localObject3).ban_users) {
              break label2463;
            }
            c = '+';
            label2238:
            ((StringBuilder)localObject4).append(c).append(' ');
            ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedBanUsers", 2131493504));
          }
          if (((TLRPC.TL_channelAdminRights)localObject1).invite_users != ((TLRPC.TL_channelAdminRights)localObject3).invite_users)
          {
            localObject4 = ((StringBuilder)localObject6).append('\n');
            if (!((TLRPC.TL_channelAdminRights)localObject3).invite_users) {
              break label2470;
            }
            c = '+';
            label2300:
            ((StringBuilder)localObject4).append(c).append(' ');
            ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedAddUsers", 2131493503));
          }
          if ((paramChat.megagroup) && (((TLRPC.TL_channelAdminRights)localObject1).pin_messages != ((TLRPC.TL_channelAdminRights)localObject3).pin_messages))
          {
            localObject1 = ((StringBuilder)localObject6).append('\n');
            if (!((TLRPC.TL_channelAdminRights)localObject3).pin_messages) {
              break label2477;
            }
          }
        }
        label2421:
        label2435:
        label2442:
        label2449:
        label2456:
        label2463:
        label2470:
        label2477:
        for (c = '+';; c = '-')
        {
          ((StringBuilder)localObject1).append(c).append(' ');
          ((StringBuilder)localObject6).append(LocaleController.getString("EventLogPromotedPinMessages", 2131493509));
          this.messageText = ((StringBuilder)localObject6).toString();
          localObject1 = localObject5;
          break;
          c = '-';
          break label1900;
          localObject4 = LocaleController.getString("EventLogPromotedChangeChannelInfo", 2131493505);
          break label1932;
          c = '-';
          break label1982;
          c = '-';
          break label2044;
          c = '-';
          break label2106;
          c = '-';
          break label2168;
          c = '-';
          break label2238;
          c = '-';
          break label2300;
        }
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionParticipantToggleBan))
      {
        this.messageOwner = new TLRPC.TL_message();
        TLRPC.User localUser = MessagesController.getInstance(paramInt).getUser(Integer.valueOf(paramTL_channelAdminLogEvent.action.prev_participant.user_id));
        localObject3 = paramTL_channelAdminLogEvent.action.prev_participant.banned_rights;
        localObject4 = paramTL_channelAdminLogEvent.action.new_participant.banned_rights;
        if ((paramChat.megagroup) && ((localObject4 == null) || (!((TLRPC.TL_channelBannedRights)localObject4).view_messages) || ((localObject4 != null) && (localObject3 != null) && (((TLRPC.TL_channelBannedRights)localObject4).until_date != ((TLRPC.TL_channelBannedRights)localObject3).until_date))))
        {
          int n;
          int i1;
          if ((localObject4 != null) && (!AndroidUtilities.isBannedForever(((TLRPC.TL_channelBannedRights)localObject4).until_date)))
          {
            localObject6 = new StringBuilder();
            i = ((TLRPC.TL_channelBannedRights)localObject4).until_date - paramTL_channelAdminLogEvent.date;
            int m = i / 60 / 60 / 24;
            i -= m * 60 * 60 * 24;
            n = i / 60 / 60;
            i1 = (i - n * 60 * 60) / 60;
            k = 0;
            j = 0;
            label2693:
            localObject1 = localObject6;
            if (j < 3)
            {
              localObject1 = null;
              if (j == 0)
              {
                i = k;
                if (m != 0)
                {
                  localObject1 = LocaleController.formatPluralString("Days", m);
                  i = k + 1;
                }
                label2736:
                if (localObject1 != null)
                {
                  if (((StringBuilder)localObject6).length() > 0) {
                    ((StringBuilder)localObject6).append(", ");
                  }
                  ((StringBuilder)localObject6).append((String)localObject1);
                }
                if (i != 2) {
                  break label3414;
                }
                localObject1 = localObject6;
              }
            }
            else
            {
              label2776:
              localObject6 = LocaleController.getString("EventLogRestrictedUntil", 2131493521);
              i = ((String)localObject6).indexOf("%1$s");
              localObject6 = new StringBuilder(String.format((String)localObject6, new Object[] { getUserName(localUser, this.messageOwner.entities, i), ((StringBuilder)localObject1).toString() }));
              i = 0;
              j = 0;
              localObject1 = localObject3;
              if (localObject3 == null) {
                localObject1 = new TLRPC.TL_channelBannedRights();
              }
              localObject3 = localObject4;
              if (localObject4 == null) {
                localObject3 = new TLRPC.TL_channelBannedRights();
              }
              if (((TLRPC.TL_channelBannedRights)localObject1).view_messages != ((TLRPC.TL_channelBannedRights)localObject3).view_messages)
              {
                i = j;
                if (0 == 0)
                {
                  ((StringBuilder)localObject6).append('\n');
                  i = 1;
                }
                localObject4 = ((StringBuilder)localObject6).append('\n');
                if (((TLRPC.TL_channelBannedRights)localObject3).view_messages) {
                  break label3448;
                }
                c = '+';
                label2936:
                ((StringBuilder)localObject4).append(c).append(' ');
                ((StringBuilder)localObject6).append(LocaleController.getString("EventLogRestrictedReadMessages", 2131493516));
              }
              j = i;
              if (((TLRPC.TL_channelBannedRights)localObject1).send_messages != ((TLRPC.TL_channelBannedRights)localObject3).send_messages)
              {
                j = i;
                if (i == 0)
                {
                  ((StringBuilder)localObject6).append('\n');
                  j = 1;
                }
                localObject4 = ((StringBuilder)localObject6).append('\n');
                if (((TLRPC.TL_channelBannedRights)localObject3).send_messages) {
                  break label3455;
                }
                c = '+';
                label3022:
                ((StringBuilder)localObject4).append(c).append(' ');
                ((StringBuilder)localObject6).append(LocaleController.getString("EventLogRestrictedSendMessages", 2131493519));
              }
              if ((((TLRPC.TL_channelBannedRights)localObject1).send_stickers == ((TLRPC.TL_channelBannedRights)localObject3).send_stickers) && (((TLRPC.TL_channelBannedRights)localObject1).send_inline == ((TLRPC.TL_channelBannedRights)localObject3).send_inline) && (((TLRPC.TL_channelBannedRights)localObject1).send_gifs == ((TLRPC.TL_channelBannedRights)localObject3).send_gifs))
              {
                i = j;
                if (((TLRPC.TL_channelBannedRights)localObject1).send_games == ((TLRPC.TL_channelBannedRights)localObject3).send_games) {}
              }
              else
              {
                i = j;
                if (j == 0)
                {
                  ((StringBuilder)localObject6).append('\n');
                  i = 1;
                }
                localObject4 = ((StringBuilder)localObject6).append('\n');
                if (((TLRPC.TL_channelBannedRights)localObject3).send_stickers) {
                  break label3462;
                }
                c = '+';
                label3147:
                ((StringBuilder)localObject4).append(c).append(' ');
                ((StringBuilder)localObject6).append(LocaleController.getString("EventLogRestrictedSendStickers", 2131493520));
              }
              j = i;
              if (((TLRPC.TL_channelBannedRights)localObject1).send_media != ((TLRPC.TL_channelBannedRights)localObject3).send_media)
              {
                j = i;
                if (i == 0)
                {
                  ((StringBuilder)localObject6).append('\n');
                  j = 1;
                }
                localObject4 = ((StringBuilder)localObject6).append('\n');
                if (((TLRPC.TL_channelBannedRights)localObject3).send_media) {
                  break label3469;
                }
                c = '+';
                label3233:
                ((StringBuilder)localObject4).append(c).append(' ');
                ((StringBuilder)localObject6).append(LocaleController.getString("EventLogRestrictedSendMedia", 2131493518));
              }
              if (((TLRPC.TL_channelBannedRights)localObject1).embed_links != ((TLRPC.TL_channelBannedRights)localObject3).embed_links)
              {
                if (j == 0) {
                  ((StringBuilder)localObject6).append('\n');
                }
                localObject1 = ((StringBuilder)localObject6).append('\n');
                if (((TLRPC.TL_channelBannedRights)localObject3).embed_links) {
                  break label3476;
                }
              }
            }
          }
          label3414:
          label3448:
          label3455:
          label3462:
          label3469:
          label3476:
          for (c = '+';; c = '-')
          {
            ((StringBuilder)localObject1).append(c).append(' ');
            ((StringBuilder)localObject6).append(LocaleController.getString("EventLogRestrictedSendEmbed", 2131493517));
            this.messageText = ((StringBuilder)localObject6).toString();
            localObject1 = localObject5;
            break;
            if (j == 1)
            {
              i = k;
              if (n == 0) {
                break label2736;
              }
              localObject1 = LocaleController.formatPluralString("Hours", n);
              i = k + 1;
              break label2736;
            }
            i = k;
            if (i1 == 0) {
              break label2736;
            }
            localObject1 = LocaleController.formatPluralString("Minutes", i1);
            i = k + 1;
            break label2736;
            j += 1;
            k = i;
            break label2693;
            localObject1 = new StringBuilder(LocaleController.getString("UserRestrictionsUntilForever", 2131494555));
            break label2776;
            c = '-';
            break label2936;
            c = '-';
            break label3022;
            c = '-';
            break label3147;
            c = '-';
            break label3233;
          }
        }
        if ((localObject4 != null) && ((localObject3 == null) || (((TLRPC.TL_channelBannedRights)localObject4).view_messages))) {}
        for (localObject1 = LocaleController.getString("EventLogChannelRestricted", 2131493464);; localObject1 = LocaleController.getString("EventLogChannelUnrestricted", 2131493465))
        {
          i = ((String)localObject1).indexOf("%1$s");
          this.messageText = String.format((String)localObject1, new Object[] { getUserName(localUser, this.messageOwner.entities, i) });
          localObject1 = localObject5;
          break;
        }
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionUpdatePinned))
      {
        if ((paramTL_channelAdminLogEvent.action.message instanceof TLRPC.TL_messageEmpty))
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogUnpinnedMessages", 2131493529), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogPinnedMessages", 2131493498), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionToggleSignatures))
      {
        if (((TLRPC.TL_channelAdminLogEventActionToggleSignatures)paramTL_channelAdminLogEvent.action).new_value)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledSignaturesOn", 2131493528), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledSignaturesOff", 2131493527), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionToggleInvites))
      {
        if (((TLRPC.TL_channelAdminLogEventActionToggleInvites)paramTL_channelAdminLogEvent.action).new_value)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesOn", 2131493526), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesOff", 2131493525), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionDeleteMessage))
      {
        this.messageText = replaceWithLink(LocaleController.getString("EventLogDeletedMessages", 2131493466), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionTogglePreHistoryHidden))
      {
        if (((TLRPC.TL_channelAdminLogEventActionTogglePreHistoryHidden)paramTL_channelAdminLogEvent.action).new_value)
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesHistoryOff", 2131493523), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogToggledInvitesHistoryOn", 2131493524), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeAbout))
      {
        if (paramChat.megagroup) {}
        for (localObject1 = LocaleController.getString("EventLogEditedGroupDescription", 2131493471);; localObject1 = LocaleController.getString("EventLogEditedChannelDescription", 2131493468))
        {
          this.messageText = replaceWithLink((CharSequence)localObject1, "un1", (TLObject)localObject2);
          localObject1 = new TLRPC.TL_message();
          ((TLRPC.Message)localObject1).out = false;
          ((TLRPC.Message)localObject1).unread = false;
          ((TLRPC.Message)localObject1).from_id = paramTL_channelAdminLogEvent.user_id;
          ((TLRPC.Message)localObject1).to_id = ((TLRPC.Peer)localObject3);
          ((TLRPC.Message)localObject1).date = paramTL_channelAdminLogEvent.date;
          ((TLRPC.Message)localObject1).message = ((TLRPC.TL_channelAdminLogEventActionChangeAbout)paramTL_channelAdminLogEvent.action).new_value;
          if (TextUtils.isEmpty(((TLRPC.TL_channelAdminLogEventActionChangeAbout)paramTL_channelAdminLogEvent.action).prev_value)) {
            break label4182;
          }
          ((TLRPC.Message)localObject1).media = new TLRPC.TL_messageMediaWebPage();
          ((TLRPC.Message)localObject1).media.webpage = new TLRPC.TL_webPage();
          ((TLRPC.Message)localObject1).media.webpage.flags = 10;
          ((TLRPC.Message)localObject1).media.webpage.display_url = "";
          ((TLRPC.Message)localObject1).media.webpage.url = "";
          ((TLRPC.Message)localObject1).media.webpage.site_name = LocaleController.getString("EventLogPreviousGroupDescription", 2131493499);
          ((TLRPC.Message)localObject1).media.webpage.description = ((TLRPC.TL_channelAdminLogEventActionChangeAbout)paramTL_channelAdminLogEvent.action).prev_value;
          break;
        }
        label4182:
        ((TLRPC.Message)localObject1).media = new TLRPC.TL_messageMediaEmpty();
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeUsername))
      {
        localObject4 = ((TLRPC.TL_channelAdminLogEventActionChangeUsername)paramTL_channelAdminLogEvent.action).new_value;
        if (!TextUtils.isEmpty((CharSequence)localObject4)) {
          if (paramChat.megagroup)
          {
            localObject1 = LocaleController.getString("EventLogChangedGroupLink", 2131493461);
            label4246:
            this.messageText = replaceWithLink((CharSequence)localObject1, "un1", (TLObject)localObject2);
            localObject1 = new TLRPC.TL_message();
            ((TLRPC.Message)localObject1).out = false;
            ((TLRPC.Message)localObject1).unread = false;
            ((TLRPC.Message)localObject1).from_id = paramTL_channelAdminLogEvent.user_id;
            ((TLRPC.Message)localObject1).to_id = ((TLRPC.Peer)localObject3);
            ((TLRPC.Message)localObject1).date = paramTL_channelAdminLogEvent.date;
            if (TextUtils.isEmpty((CharSequence)localObject4)) {
              break label4622;
            }
          }
        }
        label4622:
        for (((TLRPC.Message)localObject1).message = ("https://" + MessagesController.getInstance(paramInt).linkPrefix + "/" + (String)localObject4);; ((TLRPC.Message)localObject1).message = "")
        {
          localObject3 = new TLRPC.TL_messageEntityUrl();
          ((TLRPC.TL_messageEntityUrl)localObject3).offset = 0;
          ((TLRPC.TL_messageEntityUrl)localObject3).length = ((TLRPC.Message)localObject1).message.length();
          ((TLRPC.Message)localObject1).entities.add(localObject3);
          if (TextUtils.isEmpty(((TLRPC.TL_channelAdminLogEventActionChangeUsername)paramTL_channelAdminLogEvent.action).prev_value)) {
            break label4632;
          }
          ((TLRPC.Message)localObject1).media = new TLRPC.TL_messageMediaWebPage();
          ((TLRPC.Message)localObject1).media.webpage = new TLRPC.TL_webPage();
          ((TLRPC.Message)localObject1).media.webpage.flags = 10;
          ((TLRPC.Message)localObject1).media.webpage.display_url = "";
          ((TLRPC.Message)localObject1).media.webpage.url = "";
          ((TLRPC.Message)localObject1).media.webpage.site_name = LocaleController.getString("EventLogPreviousLink", 2131493500);
          ((TLRPC.Message)localObject1).media.webpage.description = ("https://" + MessagesController.getInstance(paramInt).linkPrefix + "/" + ((TLRPC.TL_channelAdminLogEventActionChangeUsername)paramTL_channelAdminLogEvent.action).prev_value);
          break;
          localObject1 = LocaleController.getString("EventLogChangedChannelLink", 2131493460);
          break label4246;
          if (paramChat.megagroup) {}
          for (localObject1 = LocaleController.getString("EventLogRemovedGroupLink", 2131493513);; localObject1 = LocaleController.getString("EventLogRemovedChannelLink", 2131493511))
          {
            this.messageText = replaceWithLink((CharSequence)localObject1, "un1", (TLObject)localObject2);
            break;
          }
        }
        label4632:
        ((TLRPC.Message)localObject1).media = new TLRPC.TL_messageMediaEmpty();
        break;
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionEditMessage))
      {
        localObject1 = new TLRPC.TL_message();
        ((TLRPC.Message)localObject1).out = false;
        ((TLRPC.Message)localObject1).unread = false;
        ((TLRPC.Message)localObject1).from_id = paramTL_channelAdminLogEvent.user_id;
        ((TLRPC.Message)localObject1).to_id = ((TLRPC.Peer)localObject3);
        ((TLRPC.Message)localObject1).date = paramTL_channelAdminLogEvent.date;
        localObject3 = ((TLRPC.TL_channelAdminLogEventActionEditMessage)paramTL_channelAdminLogEvent.action).new_message;
        localObject4 = ((TLRPC.TL_channelAdminLogEventActionEditMessage)paramTL_channelAdminLogEvent.action).prev_message;
        if ((((TLRPC.Message)localObject3).media != null) && (!(((TLRPC.Message)localObject3).media instanceof TLRPC.TL_messageMediaEmpty)) && (!(((TLRPC.Message)localObject3).media instanceof TLRPC.TL_messageMediaWebPage)) && (TextUtils.isEmpty(((TLRPC.Message)localObject3).message)))
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedCaption", 2131493467), "un1", (TLObject)localObject2);
          ((TLRPC.Message)localObject1).media = ((TLRPC.Message)localObject3).media;
          ((TLRPC.Message)localObject1).media.webpage = new TLRPC.TL_webPage();
          ((TLRPC.Message)localObject1).media.webpage.site_name = LocaleController.getString("EventLogOriginalCaption", 2131493495);
          if (TextUtils.isEmpty(((TLRPC.Message)localObject4).message)) {
            ((TLRPC.Message)localObject1).media.webpage.description = LocaleController.getString("EventLogOriginalCaptionEmpty", 2131493496);
          }
        }
        for (;;)
        {
          ((TLRPC.Message)localObject1).reply_markup = ((TLRPC.Message)localObject3).reply_markup;
          ((TLRPC.Message)localObject1).media.webpage.flags = 10;
          ((TLRPC.Message)localObject1).media.webpage.display_url = "";
          ((TLRPC.Message)localObject1).media.webpage.url = "";
          break;
          ((TLRPC.Message)localObject1).media.webpage.description = ((TLRPC.Message)localObject4).message;
          continue;
          this.messageText = replaceWithLink(LocaleController.getString("EventLogEditedMessages", 2131493474), "un1", (TLObject)localObject2);
          ((TLRPC.Message)localObject1).message = ((TLRPC.Message)localObject3).message;
          ((TLRPC.Message)localObject1).media = new TLRPC.TL_messageMediaWebPage();
          ((TLRPC.Message)localObject1).media.webpage = new TLRPC.TL_webPage();
          ((TLRPC.Message)localObject1).media.webpage.site_name = LocaleController.getString("EventLogOriginalMessages", 2131493497);
          if (TextUtils.isEmpty(((TLRPC.Message)localObject4).message)) {
            ((TLRPC.Message)localObject1).media.webpage.description = LocaleController.getString("EventLogOriginalCaptionEmpty", 2131493496);
          } else {
            ((TLRPC.Message)localObject1).media.webpage.description = ((TLRPC.Message)localObject4).message;
          }
        }
      }
      if ((paramTL_channelAdminLogEvent.action instanceof TLRPC.TL_channelAdminLogEventActionChangeStickerSet))
      {
        localObject1 = ((TLRPC.TL_channelAdminLogEventActionChangeStickerSet)paramTL_channelAdminLogEvent.action).new_stickerset;
        localObject3 = ((TLRPC.TL_channelAdminLogEventActionChangeStickerSet)paramTL_channelAdminLogEvent.action).new_stickerset;
        if ((localObject1 == null) || ((localObject1 instanceof TLRPC.TL_inputStickerSetEmpty)))
        {
          this.messageText = replaceWithLink(LocaleController.getString("EventLogRemovedStickersSet", 2131493514), "un1", (TLObject)localObject2);
          localObject1 = localObject5;
          break;
        }
        this.messageText = replaceWithLink(LocaleController.getString("EventLogChangedStickersSet", 2131493462), "un1", (TLObject)localObject2);
        localObject1 = localObject5;
        break;
      }
      this.messageText = ("unsupported " + paramTL_channelAdminLogEvent.action);
      localObject1 = localObject5;
      break;
      label5208:
      this.contentType = -1;
      break label623;
      label5216:
      this.videoEditedInfo.roundVideo = isRoundVideo();
      break label760;
      label5230:
      paramTL_channelAdminLogEvent = Theme.chat_msgTextPaint;
      break label781;
      label5237:
      paramArrayList = null;
      break label791;
      paramTL_channelAdminLogEvent = Theme.chat_msgTextPaintOneEmoji;
      paramInt = AndroidUtilities.dp(32.0F);
      continue;
      paramTL_channelAdminLogEvent = Theme.chat_msgTextPaintTwoEmoji;
      paramInt = AndroidUtilities.dp(28.0F);
    }
    label5270:
    if (((MediaController)localObject4).isPlayingMessage(this))
    {
      paramTL_channelAdminLogEvent = ((MediaController)localObject4).getPlayingMessageObject();
      this.audioProgress = paramTL_channelAdminLogEvent.audioProgress;
      this.audioProgressSec = paramTL_channelAdminLogEvent.audioProgressSec;
    }
    generateLayout((TLRPC.User)localObject2);
    this.layoutCreated = true;
    generateThumbs(false);
    checkMediaExistance();
  }
  
  private boolean addEntitiesToText(CharSequence paramCharSequence, boolean paramBoolean)
  {
    return addEntitiesToText(paramCharSequence, false, paramBoolean);
  }
  
  public static void addLinks(boolean paramBoolean, CharSequence paramCharSequence)
  {
    addLinks(paramBoolean, paramCharSequence, true);
  }
  
  public static void addLinks(boolean paramBoolean1, CharSequence paramCharSequence, boolean paramBoolean2)
  {
    if ((!(paramCharSequence instanceof Spannable)) || (!containsUrls(paramCharSequence)) || (paramCharSequence.length() < 1000)) {}
    for (;;)
    {
      try
      {
        Linkify.addLinks((Spannable)paramCharSequence, 5);
        addUsernamesAndHashtags(paramBoolean1, paramCharSequence, paramBoolean2);
        return;
      }
      catch (Exception localException1)
      {
        FileLog.e(localException1);
        continue;
      }
      try
      {
        Linkify.addLinks((Spannable)paramCharSequence, 1);
      }
      catch (Exception localException2)
      {
        FileLog.e(localException2);
      }
    }
  }
  
  private static void addUsernamesAndHashtags(boolean paramBoolean1, CharSequence paramCharSequence, boolean paramBoolean2)
  {
    for (;;)
    {
      int k;
      int i;
      try
      {
        if (urlPattern == null) {
          urlPattern = Pattern.compile("(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s)@[a-zA-Z\\d_]{1,32}|(^|\\s)#[\\w\\.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)");
        }
        Matcher localMatcher = urlPattern.matcher(paramCharSequence);
        if (localMatcher.find())
        {
          j = localMatcher.start();
          k = localMatcher.end();
          int m = paramCharSequence.charAt(j);
          i = j;
          if (m != 64)
          {
            i = j;
            if (m != 35)
            {
              i = j;
              if (m != 47)
              {
                i = j;
                if (m != 36) {
                  i = j + 1;
                }
              }
            }
          }
          localObject = null;
          if (paramCharSequence.charAt(i) != '/') {
            break label191;
          }
          if (paramBoolean2)
          {
            localObject = paramCharSequence.subSequence(i, k).toString();
            if (paramBoolean1)
            {
              j = 1;
              localObject = new URLSpanBotCommand((String)localObject, j);
            }
          }
          else
          {
            if (localObject == null) {
              continue;
            }
            ((Spannable)paramCharSequence).setSpan(localObject, i, k, 0);
          }
        }
        else
        {
          return;
        }
      }
      catch (Exception paramCharSequence)
      {
        FileLog.e(paramCharSequence);
      }
      int j = 0;
      continue;
      label191:
      Object localObject = new URLSpanNoUnderline(paramCharSequence.subSequence(i, k).toString());
    }
  }
  
  public static boolean canDeleteMessage(int paramInt, TLRPC.Message paramMessage, TLRPC.Chat paramChat)
  {
    boolean bool = false;
    if (paramMessage.id < 0) {}
    TLRPC.Chat localChat;
    do
    {
      return true;
      localChat = paramChat;
      if (paramChat == null)
      {
        localChat = paramChat;
        if (paramMessage.to_id.channel_id != 0) {
          localChat = MessagesController.getInstance(paramInt).getChat(Integer.valueOf(paramMessage.to_id.channel_id));
        }
      }
      if (!ChatObject.isChannel(localChat)) {
        break;
      }
    } while ((paramMessage.id != 1) && ((localChat.creator) || ((localChat.admin_rights != null) && ((localChat.admin_rights.delete_messages) || (paramMessage.out))) || ((localChat.megagroup) && (paramMessage.out) && (paramMessage.from_id > 0))));
    return false;
    if ((isOut(paramMessage)) || (!ChatObject.isChannel(localChat))) {
      bool = true;
    }
    return bool;
  }
  
  public static boolean canEditMessage(int paramInt, TLRPC.Message paramMessage, TLRPC.Chat paramChat)
  {
    boolean bool2 = true;
    if ((paramMessage == null) || (paramMessage.to_id == null) || ((paramMessage.media != null) && ((isRoundVideoDocument(paramMessage.media.document)) || (isStickerDocument(paramMessage.media.document)))) || ((paramMessage.action != null) && (!(paramMessage.action instanceof TLRPC.TL_messageActionEmpty))) || (isForwardedMessage(paramMessage)) || (paramMessage.via_bot_id != 0) || (paramMessage.id < 0)) {}
    TLRPC.Chat localChat;
    label401:
    do
    {
      do
      {
        do
        {
          do
          {
            return false;
            if ((paramMessage.from_id == paramMessage.to_id.user_id) && (paramMessage.from_id == UserConfig.getInstance(paramInt).getClientUserId()) && (!isLiveLocationMessage(paramMessage)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaContact))) {
              return true;
            }
            localChat = paramChat;
            if (paramChat != null) {
              break;
            }
            localChat = paramChat;
            if (paramMessage.to_id.channel_id == 0) {
              break;
            }
            localChat = MessagesController.getInstance(paramInt).getChat(Integer.valueOf(paramMessage.to_id.channel_id));
          } while (localChat == null);
        } while ((paramMessage.media != null) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaEmpty)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)));
        if ((paramMessage.out) && (localChat != null) && (localChat.megagroup) && ((localChat.creator) || ((localChat.admin_rights != null) && (localChat.admin_rights.pin_messages)))) {
          return true;
        }
      } while (Math.abs(paramMessage.date - ConnectionsManager.getInstance(paramInt).getCurrentTime()) > MessagesController.getInstance(paramInt).maxEditTime);
      if (paramMessage.to_id.channel_id == 0)
      {
        if ((paramMessage.out) || (paramMessage.from_id == UserConfig.getInstance(paramInt).getClientUserId()))
        {
          bool1 = bool2;
          if (!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) {
            if ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))
            {
              bool1 = bool2;
              if (!isStickerMessage(paramMessage)) {}
            }
            else
            {
              bool1 = bool2;
              if (!(paramMessage.media instanceof TLRPC.TL_messageMediaEmpty))
              {
                bool1 = bool2;
                if (!(paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
                  if (paramMessage.media != null) {
                    break label401;
                  }
                }
              }
            }
          }
        }
        for (boolean bool1 = bool2;; bool1 = false) {
          return bool1;
        }
      }
    } while (((!localChat.megagroup) || (!paramMessage.out)) && ((localChat.megagroup) || ((!localChat.creator) && ((localChat.admin_rights == null) || ((!localChat.admin_rights.edit_messages) && (!paramMessage.out)))) || (!paramMessage.post) || ((!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && ((!(paramMessage.media instanceof TLRPC.TL_messageMediaDocument)) || (isStickerMessage(paramMessage))) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaEmpty)) && (!(paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) && (paramMessage.media != null))));
    return true;
  }
  
  public static boolean canEditMessageAnytime(int paramInt, TLRPC.Message paramMessage, TLRPC.Chat paramChat)
  {
    boolean bool2 = true;
    boolean bool1;
    if ((paramMessage == null) || (paramMessage.to_id == null) || ((paramMessage.media != null) && ((isRoundVideoDocument(paramMessage.media.document)) || (isStickerDocument(paramMessage.media.document)))) || ((paramMessage.action != null) && (!(paramMessage.action instanceof TLRPC.TL_messageActionEmpty))) || (isForwardedMessage(paramMessage)) || (paramMessage.via_bot_id != 0) || (paramMessage.id < 0)) {
      bool1 = false;
    }
    TLRPC.Chat localChat;
    do
    {
      do
      {
        do
        {
          return bool1;
          if ((paramMessage.from_id != paramMessage.to_id.user_id) || (paramMessage.from_id != UserConfig.getInstance(paramInt).getClientUserId())) {
            break;
          }
          bool1 = bool2;
        } while (!isLiveLocationMessage(paramMessage));
        localChat = paramChat;
        if (paramChat == null)
        {
          localChat = paramChat;
          if (paramMessage.to_id.channel_id != 0)
          {
            paramChat = MessagesController.getInstance(UserConfig.selectedAccount).getChat(Integer.valueOf(paramMessage.to_id.channel_id));
            localChat = paramChat;
            if (paramChat == null) {
              return false;
            }
          }
        }
        if ((!paramMessage.out) || (localChat == null) || (!localChat.megagroup)) {
          break;
        }
        bool1 = bool2;
      } while (localChat.creator);
      if (localChat.admin_rights == null) {
        break;
      }
      bool1 = bool2;
    } while (localChat.admin_rights.pin_messages);
    return false;
  }
  
  private static boolean containsUrls(CharSequence paramCharSequence)
  {
    boolean bool2 = true;
    boolean bool1;
    if ((paramCharSequence == null) || (paramCharSequence.length() < 2) || (paramCharSequence.length() > 20480)) {
      bool1 = false;
    }
    int i3;
    int n;
    int m;
    int i2;
    int i1;
    label58:
    int i4;
    int k;
    int i;
    int j;
    label108:
    do
    {
      do
      {
        do
        {
          do
          {
            return bool1;
            int i5 = paramCharSequence.length();
            i3 = 0;
            n = 0;
            m = 0;
            i2 = 0;
            i1 = 0;
            if (i1 >= i5) {
              break label351;
            }
            i4 = paramCharSequence.charAt(i1);
            if ((i4 < 48) || (i4 > 57)) {
              break;
            }
            k = i3 + 1;
            bool1 = bool2;
          } while (k >= 6);
          i = 0;
          j = 0;
          if ((i4 != 64) && (i4 != 35) && (i4 != 47) && (i4 != 36)) {
            break;
          }
          bool1 = bool2;
        } while (i1 == 0);
        if (i1 == 0) {
          break;
        }
        bool1 = bool2;
      } while (paramCharSequence.charAt(i1 - 1) == ' ');
      bool1 = bool2;
    } while (paramCharSequence.charAt(i1 - 1) == '\n');
    if (i4 == 58) {
      if (i == 0) {
        i = 1;
      }
    }
    for (;;)
    {
      i2 = i4;
      i1 += 1;
      i3 = k;
      m = j;
      n = i;
      break label58;
      if (i4 != 32)
      {
        k = i3;
        j = m;
        i = n;
        if (i3 > 0) {
          break label108;
        }
      }
      k = 0;
      j = m;
      i = n;
      break label108;
      i = 0;
      continue;
      if (i4 == 47)
      {
        bool1 = bool2;
        if (i == 2) {
          break;
        }
        if (i == 1)
        {
          i += 1;
          continue;
        }
        i = 0;
        continue;
      }
      if (i4 == 46)
      {
        if ((j == 0) && (i2 != 32)) {
          j += 1;
        } else {
          j = 0;
        }
      }
      else
      {
        if ((i4 != 32) && (i2 == 46))
        {
          bool1 = bool2;
          if (j == 1) {
            break;
          }
        }
        j = 0;
      }
    }
    label351:
    return false;
  }
  
  private void createDateArray(int paramInt, TLRPC.TL_channelAdminLogEvent paramTL_channelAdminLogEvent, ArrayList<MessageObject> paramArrayList, HashMap<String, ArrayList<MessageObject>> paramHashMap)
  {
    if ((ArrayList)paramHashMap.get(this.dateKey) == null)
    {
      ArrayList localArrayList = new ArrayList();
      paramHashMap.put(this.dateKey, localArrayList);
      paramHashMap = new TLRPC.TL_message();
      paramHashMap.message = LocaleController.formatDateChat(paramTL_channelAdminLogEvent.date);
      paramHashMap.id = 0;
      paramHashMap.date = paramTL_channelAdminLogEvent.date;
      paramTL_channelAdminLogEvent = new MessageObject(paramInt, paramHashMap, false);
      paramTL_channelAdminLogEvent.type = 10;
      paramTL_channelAdminLogEvent.contentType = 1;
      paramTL_channelAdminLogEvent.isDateObject = true;
      paramArrayList.add(paramTL_channelAdminLogEvent);
    }
  }
  
  public static long getDialogId(TLRPC.Message paramMessage)
  {
    if ((paramMessage.dialog_id == 0L) && (paramMessage.to_id != null))
    {
      if (paramMessage.to_id.chat_id == 0) {
        break label71;
      }
      if (paramMessage.to_id.chat_id >= 0) {
        break label55;
      }
      paramMessage.dialog_id = AndroidUtilities.makeBroadcastId(paramMessage.to_id.chat_id);
    }
    for (;;)
    {
      return paramMessage.dialog_id;
      label55:
      paramMessage.dialog_id = (-paramMessage.to_id.chat_id);
      continue;
      label71:
      if (paramMessage.to_id.channel_id != 0) {
        paramMessage.dialog_id = (-paramMessage.to_id.channel_id);
      } else if (isOut(paramMessage)) {
        paramMessage.dialog_id = paramMessage.to_id.user_id;
      } else {
        paramMessage.dialog_id = paramMessage.from_id;
      }
    }
  }
  
  private TLRPC.Document getDocumentWithId(TLRPC.WebPage paramWebPage, long paramLong)
  {
    Object localObject;
    if ((paramWebPage == null) || (paramWebPage.cached_page == null))
    {
      localObject = null;
      return (TLRPC.Document)localObject;
    }
    if ((paramWebPage.document != null) && (paramWebPage.document.id == paramLong)) {
      return paramWebPage.document;
    }
    int i = 0;
    for (;;)
    {
      if (i >= paramWebPage.cached_page.documents.size()) {
        break label99;
      }
      TLRPC.Document localDocument = (TLRPC.Document)paramWebPage.cached_page.documents.get(i);
      localObject = localDocument;
      if (localDocument.id == paramLong) {
        break;
      }
      i += 1;
    }
    label99:
    return null;
  }
  
  public static int getInlineResultDuration(TLRPC.BotInlineResult paramBotInlineResult)
  {
    int j = getWebDocumentDuration(paramBotInlineResult.content);
    int i = j;
    if (j == 0) {
      i = getWebDocumentDuration(paramBotInlineResult.thumb);
    }
    return i;
  }
  
  public static int[] getInlineResultWidthAndHeight(TLRPC.BotInlineResult paramBotInlineResult)
  {
    int[] arrayOfInt = getWebDocumentWidthAndHeight(paramBotInlineResult.content);
    Object localObject = arrayOfInt;
    if (arrayOfInt == null)
    {
      paramBotInlineResult = getWebDocumentWidthAndHeight(paramBotInlineResult.thumb);
      localObject = paramBotInlineResult;
      if (paramBotInlineResult == null)
      {
        localObject = new int[2];
        Object tmp33_32 = localObject;
        tmp33_32[0] = 0;
        Object tmp37_33 = tmp33_32;
        tmp37_33[1] = 0;
        tmp37_33;
      }
    }
    return (int[])localObject;
  }
  
  public static TLRPC.InputStickerSet getInputStickerSet(TLRPC.Message paramMessage)
  {
    TLRPC.DocumentAttribute localDocumentAttribute;
    if ((paramMessage.media != null) && (paramMessage.media.document != null))
    {
      paramMessage = paramMessage.media.document.attributes.iterator();
      while (paramMessage.hasNext())
      {
        localDocumentAttribute = (TLRPC.DocumentAttribute)paramMessage.next();
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker)) {
          if (!(localDocumentAttribute.stickerset instanceof TLRPC.TL_inputStickerSetEmpty)) {
            break label69;
          }
        }
      }
    }
    return null;
    label69:
    return localDocumentAttribute.stickerset;
  }
  
  private MessageObject getMessageObjectForBlock(TLRPC.WebPage paramWebPage, TLRPC.PageBlock paramPageBlock)
  {
    TLRPC.TL_message localTL_message = null;
    if ((paramPageBlock instanceof TLRPC.TL_pageBlockPhoto))
    {
      paramPageBlock = getPhotoWithId(paramWebPage, paramPageBlock.photo_id);
      if (paramPageBlock == paramWebPage.photo) {
        return this;
      }
      localTL_message = new TLRPC.TL_message();
      localTL_message.media = new TLRPC.TL_messageMediaPhoto();
      localTL_message.media.photo = paramPageBlock;
    }
    for (;;)
    {
      localTL_message.message = "";
      localTL_message.id = Utilities.random.nextInt();
      localTL_message.date = this.messageOwner.date;
      localTL_message.to_id = this.messageOwner.to_id;
      localTL_message.out = this.messageOwner.out;
      localTL_message.from_id = this.messageOwner.from_id;
      return new MessageObject(this.currentAccount, localTL_message, false);
      if ((paramPageBlock instanceof TLRPC.TL_pageBlockVideo))
      {
        if (getDocumentWithId(paramWebPage, paramPageBlock.video_id) == paramWebPage.document) {
          break;
        }
        localTL_message = new TLRPC.TL_message();
        localTL_message.media = new TLRPC.TL_messageMediaDocument();
        localTL_message.media.document = getDocumentWithId(paramWebPage, paramPageBlock.video_id);
      }
    }
  }
  
  public static int getMessageSize(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media != null) && (paramMessage.media.document != null)) {
      return paramMessage.media.document.size;
    }
    return 0;
  }
  
  private TLRPC.Photo getPhotoWithId(TLRPC.WebPage paramWebPage, long paramLong)
  {
    Object localObject;
    if ((paramWebPage == null) || (paramWebPage.cached_page == null))
    {
      localObject = null;
      return (TLRPC.Photo)localObject;
    }
    if ((paramWebPage.photo != null) && (paramWebPage.photo.id == paramLong)) {
      return paramWebPage.photo;
    }
    int i = 0;
    for (;;)
    {
      if (i >= paramWebPage.cached_page.photos.size()) {
        break label99;
      }
      TLRPC.Photo localPhoto = (TLRPC.Photo)paramWebPage.cached_page.photos.get(i);
      localObject = localPhoto;
      if (localPhoto.id == paramLong) {
        break;
      }
      i += 1;
    }
    label99:
    return null;
  }
  
  public static long getStickerSetId(TLRPC.Document paramDocument)
  {
    if (paramDocument == null) {}
    label63:
    for (;;)
    {
      return -1L;
      int i = 0;
      for (;;)
      {
        if (i >= paramDocument.attributes.size()) {
          break label63;
        }
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker))
        {
          if ((localDocumentAttribute.stickerset instanceof TLRPC.TL_inputStickerSetEmpty)) {
            break;
          }
          return localDocumentAttribute.stickerset.id;
        }
        i += 1;
      }
    }
  }
  
  public static int getUnreadFlags(TLRPC.Message paramMessage)
  {
    int i = 0;
    if (!paramMessage.unread) {
      i = 0x0 | 0x1;
    }
    int j = i;
    if (!paramMessage.media_unread) {
      j = i | 0x2;
    }
    return j;
  }
  
  private String getUserName(TLRPC.User paramUser, ArrayList<TLRPC.MessageEntity> paramArrayList, int paramInt)
  {
    if (paramUser == null) {}
    for (String str = "";; str = ContactsController.formatName(paramUser.first_name, paramUser.last_name))
    {
      if (paramInt >= 0)
      {
        localObject = new TLRPC.TL_messageEntityMentionName();
        ((TLRPC.TL_messageEntityMentionName)localObject).user_id = paramUser.id;
        ((TLRPC.TL_messageEntityMentionName)localObject).offset = paramInt;
        ((TLRPC.TL_messageEntityMentionName)localObject).length = str.length();
        paramArrayList.add(localObject);
      }
      Object localObject = str;
      if (!TextUtils.isEmpty(paramUser.username))
      {
        if (paramInt >= 0)
        {
          localObject = new TLRPC.TL_messageEntityMentionName();
          ((TLRPC.TL_messageEntityMentionName)localObject).user_id = paramUser.id;
          ((TLRPC.TL_messageEntityMentionName)localObject).offset = (str.length() + paramInt + 2);
          ((TLRPC.TL_messageEntityMentionName)localObject).length = (paramUser.username.length() + 1);
          paramArrayList.add(localObject);
        }
        localObject = String.format("%1$s (@%2$s)", new Object[] { str, paramUser.username });
      }
      return (String)localObject;
    }
  }
  
  public static int getWebDocumentDuration(TLRPC.WebDocument paramWebDocument)
  {
    if (paramWebDocument == null) {}
    for (;;)
    {
      return 0;
      int i = 0;
      int j = paramWebDocument.attributes.size();
      while (i < j)
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramWebDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
          return localDocumentAttribute.duration;
        }
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio)) {
          return localDocumentAttribute.duration;
        }
        i += 1;
      }
    }
  }
  
  public static int[] getWebDocumentWidthAndHeight(TLRPC.WebDocument paramWebDocument)
  {
    if (paramWebDocument == null) {}
    for (;;)
    {
      return null;
      int i = 0;
      int j = paramWebDocument.attributes.size();
      while (i < j)
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramWebDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeImageSize)) {
          return new int[] { localDocumentAttribute.w, localDocumentAttribute.h };
        }
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
          return new int[] { localDocumentAttribute.w, localDocumentAttribute.h };
        }
        i += 1;
      }
    }
  }
  
  public static boolean isContentUnread(TLRPC.Message paramMessage)
  {
    return paramMessage.media_unread;
  }
  
  public static boolean isForwardedMessage(TLRPC.Message paramMessage)
  {
    return ((paramMessage.flags & 0x4) != 0) && (paramMessage.fwd_from != null);
  }
  
  public static boolean isGameMessage(TLRPC.Message paramMessage)
  {
    return paramMessage.media instanceof TLRPC.TL_messageMediaGame;
  }
  
  public static boolean isGifDocument(TLRPC.Document paramDocument)
  {
    return (paramDocument != null) && (paramDocument.thumb != null) && (paramDocument.mime_type != null) && ((paramDocument.mime_type.equals("image/gif")) || (isNewGifDocument(paramDocument)));
  }
  
  public static boolean isGifDocument(TLRPC.TL_webDocument paramTL_webDocument)
  {
    return (paramTL_webDocument != null) && ((paramTL_webDocument.mime_type.equals("image/gif")) || (isNewGifDocument(paramTL_webDocument)));
  }
  
  public static boolean isGifMessage(TLRPC.Message paramMessage)
  {
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isGifDocument(paramMessage.media.document));
  }
  
  public static boolean isImageWebDocument(TLRPC.TL_webDocument paramTL_webDocument)
  {
    return (paramTL_webDocument != null) && (!isGifDocument(paramTL_webDocument)) && (paramTL_webDocument.mime_type.startsWith("image/"));
  }
  
  public static boolean isInvoiceMessage(TLRPC.Message paramMessage)
  {
    return paramMessage.media instanceof TLRPC.TL_messageMediaInvoice;
  }
  
  public static boolean isLiveLocationMessage(TLRPC.Message paramMessage)
  {
    return paramMessage.media instanceof TLRPC.TL_messageMediaGeoLive;
  }
  
  public static boolean isMaskDocument(TLRPC.Document paramDocument)
  {
    if (paramDocument != null)
    {
      int i = 0;
      while (i < paramDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if (((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker)) && (localDocumentAttribute.mask)) {
          return true;
        }
        i += 1;
      }
    }
    return false;
  }
  
  public static boolean isMaskMessage(TLRPC.Message paramMessage)
  {
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isMaskDocument(paramMessage.media.document));
  }
  
  public static boolean isMediaEmpty(TLRPC.Message paramMessage)
  {
    return (paramMessage == null) || (paramMessage.media == null) || ((paramMessage.media instanceof TLRPC.TL_messageMediaEmpty)) || ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage));
  }
  
  public static boolean isMegagroup(TLRPC.Message paramMessage)
  {
    return (paramMessage.flags & 0x80000000) != 0;
  }
  
  public static boolean isMusicDocument(TLRPC.Document paramDocument)
  {
    if (paramDocument != null)
    {
      int i = 0;
      Object localObject;
      if (i < paramDocument.attributes.size())
      {
        localObject = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if ((localObject instanceof TLRPC.TL_documentAttributeAudio)) {
          if (((TLRPC.DocumentAttribute)localObject).voice) {}
        }
      }
      do
      {
        return true;
        return false;
        i += 1;
        break;
        if (TextUtils.isEmpty(paramDocument.mime_type)) {
          break label135;
        }
        localObject = paramDocument.mime_type.toLowerCase();
      } while ((((String)localObject).equals("audio/flac")) || (((String)localObject).equals("audio/ogg")) || (((String)localObject).equals("audio/opus")) || (((String)localObject).equals("audio/x-opus+ogg")) || ((((String)localObject).equals("application/octet-stream")) && (FileLoader.getDocumentFileName(paramDocument).endsWith(".opus"))));
    }
    label135:
    return false;
  }
  
  public static boolean isMusicMessage(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return isMusicDocument(paramMessage.media.webpage.document);
    }
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isMusicDocument(paramMessage.media.document));
  }
  
  public static boolean isNewGifDocument(TLRPC.Document paramDocument)
  {
    if ((paramDocument != null) && (paramDocument.mime_type != null) && (paramDocument.mime_type.equals("video/mp4")))
    {
      int k = 0;
      int n = 0;
      int j = 0;
      int i = 0;
      if (i < paramDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        int m;
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAnimated)) {
          m = 1;
        }
        for (;;)
        {
          i += 1;
          j = m;
          break;
          m = j;
          if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo))
          {
            k = localDocumentAttribute.w;
            n = localDocumentAttribute.w;
            m = j;
          }
        }
      }
      if ((j != 0) && (k <= 1280) && (n <= 1280)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isNewGifDocument(TLRPC.TL_webDocument paramTL_webDocument)
  {
    if ((paramTL_webDocument != null) && (paramTL_webDocument.mime_type != null) && (paramTL_webDocument.mime_type.equals("video/mp4")))
    {
      int j = 0;
      int k = 0;
      int i = 0;
      if (i < paramTL_webDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramTL_webDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAnimated)) {}
        for (;;)
        {
          i += 1;
          break;
          if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo))
          {
            j = localDocumentAttribute.w;
            k = localDocumentAttribute.w;
          }
        }
      }
      if ((j <= 1280) && (k <= 1280)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isNewGifMessage(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return isNewGifDocument(paramMessage.media.webpage.document);
    }
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isNewGifDocument(paramMessage.media.document));
  }
  
  public static boolean isOut(TLRPC.Message paramMessage)
  {
    return paramMessage.out;
  }
  
  public static boolean isPhoto(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return paramMessage.media.webpage.photo instanceof TLRPC.TL_photo;
    }
    return paramMessage.media instanceof TLRPC.TL_messageMediaPhoto;
  }
  
  public static boolean isRoundVideoDocument(TLRPC.Document paramDocument)
  {
    if ((paramDocument != null) && (paramDocument.mime_type != null) && (paramDocument.mime_type.equals("video/mp4")))
    {
      int j = 0;
      int k = 0;
      boolean bool = false;
      int i = 0;
      while (i < paramDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo))
        {
          j = localDocumentAttribute.w;
          k = localDocumentAttribute.w;
          bool = localDocumentAttribute.round_message;
        }
        i += 1;
      }
      if ((bool) && (j <= 1280) && (k <= 1280)) {
        return true;
      }
    }
    return false;
  }
  
  public static boolean isRoundVideoMessage(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return isRoundVideoDocument(paramMessage.media.webpage.document);
    }
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isRoundVideoDocument(paramMessage.media.document));
  }
  
  public static boolean isSecretPhotoOrVideo(TLRPC.Message paramMessage)
  {
    if ((paramMessage instanceof TLRPC.TL_message_secret)) {
      if (((!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (!isRoundVideoMessage(paramMessage)) && (!isVideoMessage(paramMessage))) || (paramMessage.ttl <= 0) || (paramMessage.ttl > 60)) {}
    }
    do
    {
      return true;
      return false;
      if (!(paramMessage instanceof TLRPC.TL_message)) {
        break;
      }
    } while ((((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) || ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))) && (paramMessage.media.ttl_seconds != 0));
    return false;
    return false;
  }
  
  public static boolean isStickerDocument(TLRPC.Document paramDocument)
  {
    if (paramDocument != null)
    {
      int i = 0;
      while (i < paramDocument.attributes.size())
      {
        if (((TLRPC.DocumentAttribute)paramDocument.attributes.get(i) instanceof TLRPC.TL_documentAttributeSticker)) {
          return true;
        }
        i += 1;
      }
    }
    return false;
  }
  
  public static boolean isStickerMessage(TLRPC.Message paramMessage)
  {
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isStickerDocument(paramMessage.media.document));
  }
  
  public static boolean isUnread(TLRPC.Message paramMessage)
  {
    return paramMessage.unread;
  }
  
  public static boolean isVideoDocument(TLRPC.Document paramDocument)
  {
    int i;
    int j;
    int k;
    int n;
    int m;
    TLRPC.DocumentAttribute localDocumentAttribute;
    if (paramDocument != null)
    {
      i = 0;
      j = 0;
      k = 0;
      n = 0;
      m = 0;
      if (m >= paramDocument.attributes.size()) {
        break label129;
      }
      localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(m);
      if (!(localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
        break label96;
      }
      if (!localDocumentAttribute.round_message) {
        break label60;
      }
    }
    label60:
    label96:
    label129:
    do
    {
      return false;
      int i3 = 1;
      int i1 = localDocumentAttribute.w;
      int i2 = localDocumentAttribute.h;
      for (;;)
      {
        m += 1;
        n = i2;
        j = i3;
        k = i1;
        break;
        i2 = n;
        i3 = j;
        i1 = k;
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAnimated))
        {
          i = 1;
          i2 = n;
          i3 = j;
          i1 = k;
        }
      }
      m = i;
      if (i != 0) {
        if (k <= 1280)
        {
          m = i;
          if (n <= 1280) {}
        }
        else
        {
          m = 0;
        }
      }
    } while ((j == 0) || (m != 0));
    return true;
  }
  
  public static boolean isVideoMessage(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return isVideoDocument(paramMessage.media.webpage.document);
    }
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isVideoDocument(paramMessage.media.document));
  }
  
  public static boolean isVideoWebDocument(TLRPC.TL_webDocument paramTL_webDocument)
  {
    return (paramTL_webDocument != null) && (paramTL_webDocument.mime_type.startsWith("video/"));
  }
  
  public static boolean isVoiceDocument(TLRPC.Document paramDocument)
  {
    if (paramDocument != null)
    {
      int i = 0;
      while (i < paramDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio)) {
          return localDocumentAttribute.voice;
        }
        i += 1;
      }
    }
    return false;
  }
  
  public static boolean isVoiceMessage(TLRPC.Message paramMessage)
  {
    if ((paramMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return isVoiceDocument(paramMessage.media.webpage.document);
    }
    return (paramMessage.media != null) && (paramMessage.media.document != null) && (isVoiceDocument(paramMessage.media.document));
  }
  
  public static boolean isVoiceWebDocument(TLRPC.TL_webDocument paramTL_webDocument)
  {
    return (paramTL_webDocument != null) && (paramTL_webDocument.mime_type.equals("audio/ogg"));
  }
  
  public static void setUnreadFlags(TLRPC.Message paramMessage, int paramInt)
  {
    boolean bool2 = true;
    if ((paramInt & 0x1) == 0)
    {
      bool1 = true;
      paramMessage.unread = bool1;
      if ((paramInt & 0x2) != 0) {
        break label34;
      }
    }
    label34:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      paramMessage.media_unread = bool1;
      return;
      bool1 = false;
      break;
    }
  }
  
  public static boolean shouldEncryptPhotoOrVideo(TLRPC.Message paramMessage)
  {
    if ((paramMessage instanceof TLRPC.TL_message_secret)) {
      if (((!(paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) && (!isVideoMessage(paramMessage))) || (paramMessage.ttl <= 0) || (paramMessage.ttl > 60)) {}
    }
    while ((((paramMessage.media instanceof TLRPC.TL_messageMediaPhoto)) || ((paramMessage.media instanceof TLRPC.TL_messageMediaDocument))) && (paramMessage.media.ttl_seconds != 0))
    {
      return true;
      return false;
    }
    return false;
  }
  
  public boolean addEntitiesToText(CharSequence paramCharSequence, boolean paramBoolean1, boolean paramBoolean2)
  {
    boolean bool2 = false;
    if (!(paramCharSequence instanceof Spannable)) {
      return false;
    }
    Spannable localSpannable = (Spannable)paramCharSequence;
    int k = this.messageOwner.entities.size();
    URLSpan[] arrayOfURLSpan = (URLSpan[])localSpannable.getSpans(0, paramCharSequence.length(), URLSpan.class);
    boolean bool1 = bool2;
    if (arrayOfURLSpan != null)
    {
      bool1 = bool2;
      if (arrayOfURLSpan.length > 0) {
        bool1 = true;
      }
    }
    int i = 0;
    if (i < k)
    {
      TLRPC.MessageEntity localMessageEntity = (TLRPC.MessageEntity)this.messageOwner.entities.get(i);
      if ((localMessageEntity.length > 0) && (localMessageEntity.offset >= 0)) {
        if (localMessageEntity.offset < paramCharSequence.length()) {}
      }
      for (;;)
      {
        i += 1;
        break;
        if (localMessageEntity.offset + localMessageEntity.length > paramCharSequence.length()) {
          localMessageEntity.length = (paramCharSequence.length() - localMessageEntity.offset);
        }
        if (((!paramBoolean2) || ((localMessageEntity instanceof TLRPC.TL_messageEntityBold)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityItalic)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityCode)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityPre)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityMentionName)) || ((localMessageEntity instanceof TLRPC.TL_inputMessageEntityMentionName))) && (arrayOfURLSpan != null) && (arrayOfURLSpan.length > 0))
        {
          int j = 0;
          if (j < arrayOfURLSpan.length)
          {
            if (arrayOfURLSpan[j] == null) {}
            for (;;)
            {
              j += 1;
              break;
              int m = localSpannable.getSpanStart(arrayOfURLSpan[j]);
              int n = localSpannable.getSpanEnd(arrayOfURLSpan[j]);
              if (((localMessageEntity.offset <= m) && (localMessageEntity.offset + localMessageEntity.length >= m)) || ((localMessageEntity.offset <= n) && (localMessageEntity.offset + localMessageEntity.length >= n)))
              {
                localSpannable.removeSpan(arrayOfURLSpan[j]);
                arrayOfURLSpan[j] = null;
              }
            }
          }
        }
        if ((localMessageEntity instanceof TLRPC.TL_messageEntityBold))
        {
          localSpannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
        }
        else if ((localMessageEntity instanceof TLRPC.TL_messageEntityItalic))
        {
          localSpannable.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/ritalic.ttf")), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
        }
        else
        {
          if (((localMessageEntity instanceof TLRPC.TL_messageEntityCode)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityPre)))
          {
            byte b;
            if (paramBoolean1) {
              b = 2;
            }
            for (;;)
            {
              localSpannable.setSpan(new URLSpanMono(localSpannable, localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, b), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
              break;
              if (isOutOwner()) {
                b = 1;
              } else {
                b = 0;
              }
            }
          }
          if ((localMessageEntity instanceof TLRPC.TL_messageEntityMentionName))
          {
            localSpannable.setSpan(new URLSpanUserMention("" + ((TLRPC.TL_messageEntityMentionName)localMessageEntity).user_id, this.type), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
          }
          else if ((localMessageEntity instanceof TLRPC.TL_inputMessageEntityMentionName))
          {
            localSpannable.setSpan(new URLSpanUserMention("" + ((TLRPC.TL_inputMessageEntityMentionName)localMessageEntity).user_id.user_id, this.type), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
          }
          else if (!paramBoolean2)
          {
            String str3 = TextUtils.substring(paramCharSequence, localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length);
            if ((localMessageEntity instanceof TLRPC.TL_messageEntityBotCommand))
            {
              localSpannable.setSpan(new URLSpanBotCommand(str3, this.type), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
            }
            else if (((localMessageEntity instanceof TLRPC.TL_messageEntityHashtag)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityMention)) || ((localMessageEntity instanceof TLRPC.TL_messageEntityCashtag)))
            {
              localSpannable.setSpan(new URLSpanNoUnderline(str3), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
            }
            else if ((localMessageEntity instanceof TLRPC.TL_messageEntityEmail))
            {
              localSpannable.setSpan(new URLSpanReplacement("mailto:" + str3), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
            }
            else if ((localMessageEntity instanceof TLRPC.TL_messageEntityUrl))
            {
              bool1 = true;
              if ((!str3.toLowerCase().startsWith("http")) && (!str3.toLowerCase().startsWith("tg://"))) {
                localSpannable.setSpan(new URLSpanBrowser("http://" + str3), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
              } else {
                localSpannable.setSpan(new URLSpanBrowser(str3), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
              }
            }
            else if ((localMessageEntity instanceof TLRPC.TL_messageEntityPhone))
            {
              bool1 = true;
              String str2 = PhoneFormat.stripExceptNumbers(str3);
              String str1 = str2;
              if (str3.startsWith("+")) {
                str1 = "+" + str2;
              }
              localSpannable.setSpan(new URLSpanBrowser("tel://" + str1), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
            }
            else if ((localMessageEntity instanceof TLRPC.TL_messageEntityTextUrl))
            {
              localSpannable.setSpan(new URLSpanReplacement(localMessageEntity.url), localMessageEntity.offset, localMessageEntity.offset + localMessageEntity.length, 33);
            }
          }
        }
      }
    }
    return bool1;
  }
  
  public void applyNewText()
  {
    if (TextUtils.isEmpty(this.messageOwner.message)) {
      return;
    }
    TLRPC.User localUser = null;
    if (isFromUser()) {
      localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
    }
    this.messageText = this.messageOwner.message;
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) {}
    for (TextPaint localTextPaint = Theme.chat_msgGameTextPaint;; localTextPaint = Theme.chat_msgTextPaint)
    {
      this.messageText = Emoji.replaceEmoji(this.messageText, localTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
      generateLayout(localUser);
      return;
    }
  }
  
  public boolean canDeleteMessage(TLRPC.Chat paramChat)
  {
    return (this.eventId == 0L) && (canDeleteMessage(this.currentAccount, this.messageOwner, paramChat));
  }
  
  public boolean canEditMessage(TLRPC.Chat paramChat)
  {
    return canEditMessage(this.currentAccount, this.messageOwner, paramChat);
  }
  
  public boolean canEditMessageAnytime(TLRPC.Chat paramChat)
  {
    return canEditMessageAnytime(this.currentAccount, this.messageOwner, paramChat);
  }
  
  public boolean canStreamVideo()
  {
    TLRPC.Document localDocument = getDocument();
    if (localDocument == null) {}
    for (;;)
    {
      return false;
      if (SharedConfig.streamAllVideo) {
        return true;
      }
      int i = 0;
      while (i < localDocument.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)localDocument.attributes.get(i);
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
          return localDocumentAttribute.supports_streaming;
        }
        i += 1;
      }
    }
  }
  
  public boolean checkLayout()
  {
    if ((this.type != 0) || (this.messageOwner.to_id == null) || (this.messageText == null) || (this.messageText.length() == 0)) {
      return false;
    }
    int i;
    if (this.layoutCreated)
    {
      if (!AndroidUtilities.isTablet()) {
        break label166;
      }
      i = AndroidUtilities.getMinTabletSide();
      if (Math.abs(this.generatedWithMinSize - i) > AndroidUtilities.dp(52.0F)) {
        this.layoutCreated = false;
      }
    }
    if (!this.layoutCreated)
    {
      this.layoutCreated = true;
      TLRPC.User localUser = null;
      if (isFromUser()) {
        localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
      }
      if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) {}
      for (TextPaint localTextPaint = Theme.chat_msgGameTextPaint;; localTextPaint = Theme.chat_msgTextPaint)
      {
        this.messageText = Emoji.replaceEmoji(this.messageText, localTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
        generateLayout(localUser);
        return true;
        label166:
        i = AndroidUtilities.displaySize.x;
        break;
      }
    }
    return false;
  }
  
  public void checkMediaExistance()
  {
    this.attachPathExists = false;
    this.mediaExists = false;
    Object localObject;
    if (this.type == 1) {
      if (FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize()) != null)
      {
        localObject = FileLoader.getPathToMessage(this.messageOwner);
        if (needDrawBluredPreview()) {
          this.mediaExists = new File(((File)localObject).getAbsolutePath() + ".enc").exists();
        }
        if (!this.mediaExists) {
          this.mediaExists = ((File)localObject).exists();
        }
      }
    }
    do
    {
      do
      {
        do
        {
          do
          {
            return;
            if ((this.type != 8) && (this.type != 3) && (this.type != 9) && (this.type != 2) && (this.type != 14) && (this.type != 5)) {
              break;
            }
            if ((this.messageOwner.attachPath != null) && (this.messageOwner.attachPath.length() > 0)) {
              this.attachPathExists = new File(this.messageOwner.attachPath).exists();
            }
          } while (this.attachPathExists);
          localObject = FileLoader.getPathToMessage(this.messageOwner);
          if ((this.type == 3) && (needDrawBluredPreview())) {
            this.mediaExists = new File(((File)localObject).getAbsolutePath() + ".enc").exists();
          }
        } while (this.mediaExists);
        this.mediaExists = ((File)localObject).exists();
        return;
        localObject = getDocument();
        if (localObject != null)
        {
          this.mediaExists = FileLoader.getPathToAttach((TLObject)localObject).exists();
          return;
        }
      } while (this.type != 0);
      localObject = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
    } while ((localObject == null) || (localObject == null));
    this.mediaExists = FileLoader.getPathToAttach((TLObject)localObject, true).exists();
  }
  
  public void generateCaption()
  {
    if ((this.caption != null) || (isRoundVideo())) {}
    while ((isMediaEmpty()) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) || (TextUtils.isEmpty(this.messageOwner.message))) {
      return;
    }
    this.caption = Emoji.replaceEmoji(this.messageOwner.message, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
    int j;
    int i;
    label124:
    boolean bool;
    if (this.messageOwner.send_state != 0)
    {
      int k = 0;
      j = 0;
      i = k;
      if (j < this.messageOwner.entities.size())
      {
        if (!(this.messageOwner.entities.get(j) instanceof TLRPC.TL_inputMessageEntityMentionName)) {
          i = 1;
        }
      }
      else
      {
        if ((i != 0) || ((this.eventId == 0L) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto_old)) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto_layer68)) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto_layer74)) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument_old)) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument_layer68)) && (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument_layer74)) && ((!isOut()) || (this.messageOwner.send_state == 0)) && (this.messageOwner.id >= 0))) {
          break label326;
        }
        bool = true;
        if (!bool) {
          break label342;
        }
        if (!containsUrls(this.caption)) {}
      }
    }
    for (;;)
    {
      try
      {
        Linkify.addLinks((Spannable)this.caption, 5);
        addUsernamesAndHashtags(isOutOwner(), this.caption, true);
        addEntitiesToText(this.caption, bool);
        return;
        j += 1;
        break;
        if (!this.messageOwner.entities.isEmpty())
        {
          i = 1;
          break label124;
        }
        i = 0;
        continue;
        label326:
        bool = false;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        continue;
      }
      try
      {
        label342:
        Linkify.addLinks((Spannable)this.caption, 4);
      }
      catch (Throwable localThrowable)
      {
        FileLog.e(localThrowable);
      }
    }
  }
  
  public void generateGameMessageText(TLRPC.User paramUser)
  {
    TLRPC.User localUser = paramUser;
    if (paramUser == null)
    {
      localUser = paramUser;
      if (this.messageOwner.from_id > 0) {
        localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
      }
    }
    Object localObject = null;
    paramUser = (TLRPC.User)localObject;
    if (this.replyMessageObject != null)
    {
      paramUser = (TLRPC.User)localObject;
      if (this.replyMessageObject.messageOwner.media != null)
      {
        paramUser = (TLRPC.User)localObject;
        if (this.replyMessageObject.messageOwner.media.game != null) {
          paramUser = this.replyMessageObject.messageOwner.media.game;
        }
      }
    }
    if (paramUser == null)
    {
      if ((localUser != null) && (localUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId()))
      {
        this.messageText = LocaleController.formatString("ActionYouScored", 2131492918, new Object[] { LocaleController.formatPluralString("Points", this.messageOwner.action.score) });
        return;
      }
      this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScored", 2131492909, new Object[] { LocaleController.formatPluralString("Points", this.messageOwner.action.score) }), "un1", localUser);
      return;
    }
    if ((localUser != null) && (localUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId())) {}
    for (this.messageText = LocaleController.formatString("ActionYouScoredInGame", 2131492919, new Object[] { LocaleController.formatPluralString("Points", this.messageOwner.action.score) });; this.messageText = replaceWithLink(LocaleController.formatString("ActionUserScoredInGame", 2131492910, new Object[] { LocaleController.formatPluralString("Points", this.messageOwner.action.score) }), "un1", localUser))
    {
      this.messageText = replaceWithLink(this.messageText, "un2", paramUser);
      return;
    }
  }
  
  /* Error */
  public void generateLayout(TLRPC.User paramUser)
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 110	org/telegram/messenger/MessageObject:type	I
    //   4: ifne +23 -> 27
    //   7: aload_0
    //   8: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   11: getfield 387	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   14: ifnull +13 -> 27
    //   17: aload_0
    //   18: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   21: invokestatic 1234	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   24: ifeq +4 -> 28
    //   27: return
    //   28: aload_0
    //   29: invokevirtual 1994	org/telegram/messenger/MessageObject:generateLinkDescription	()V
    //   32: aload_0
    //   33: new 376	java/util/ArrayList
    //   36: dup
    //   37: invokespecial 1480	java/util/ArrayList:<init>	()V
    //   40: putfield 1996	org/telegram/messenger/MessageObject:textLayoutBlocks	Ljava/util/ArrayList;
    //   43: aload_0
    //   44: iconst_0
    //   45: putfield 1998	org/telegram/messenger/MessageObject:textWidth	I
    //   48: aload_0
    //   49: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   52: getfield 1954	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   55: ifeq +997 -> 1052
    //   58: iconst_0
    //   59: istore 9
    //   61: iconst_0
    //   62: istore 8
    //   64: iload 9
    //   66: istore 7
    //   68: iload 8
    //   70: aload_0
    //   71: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   74: getfield 1029	org/telegram/tgnet/TLRPC$Message:entities	Ljava/util/ArrayList;
    //   77: invokevirtual 379	java/util/ArrayList:size	()I
    //   80: if_icmpge +24 -> 104
    //   83: aload_0
    //   84: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   87: getfield 1029	org/telegram/tgnet/TLRPC$Message:entities	Ljava/util/ArrayList;
    //   90: iload 8
    //   92: invokevirtual 380	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   95: instanceof 1786
    //   98: ifne +945 -> 1043
    //   101: iconst_1
    //   102: istore 7
    //   104: iload 7
    //   106: ifne +971 -> 1077
    //   109: aload_0
    //   110: getfield 132	org/telegram/messenger/MessageObject:eventId	J
    //   113: lconst_0
    //   114: lcmp
    //   115: ifne +126 -> 241
    //   118: aload_0
    //   119: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   122: instanceof 2000
    //   125: ifne +116 -> 241
    //   128: aload_0
    //   129: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   132: instanceof 2002
    //   135: ifne +106 -> 241
    //   138: aload_0
    //   139: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   142: instanceof 2004
    //   145: ifne +96 -> 241
    //   148: aload_0
    //   149: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   152: instanceof 2006
    //   155: ifne +86 -> 241
    //   158: aload_0
    //   159: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   162: instanceof 2008
    //   165: ifne +76 -> 241
    //   168: aload_0
    //   169: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   172: instanceof 2010
    //   175: ifne +66 -> 241
    //   178: aload_0
    //   179: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   182: instanceof 1747
    //   185: ifne +56 -> 241
    //   188: aload_0
    //   189: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   192: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   195: instanceof 765
    //   198: ifne +43 -> 241
    //   201: aload_0
    //   202: invokevirtual 321	org/telegram/messenger/MessageObject:isOut	()Z
    //   205: ifeq +13 -> 218
    //   208: aload_0
    //   209: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   212: getfield 1954	org/telegram/tgnet/TLRPC$Message:send_state	I
    //   215: ifne +26 -> 241
    //   218: aload_0
    //   219: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   222: getfield 222	org/telegram/tgnet/TLRPC$Message:id	I
    //   225: iflt +16 -> 241
    //   228: aload_0
    //   229: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   232: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   235: instanceof 770
    //   238: ifeq +839 -> 1077
    //   241: iconst_1
    //   242: istore 19
    //   244: iload 19
    //   246: ifeq +837 -> 1083
    //   249: aload_0
    //   250: invokevirtual 1813	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   253: aload_0
    //   254: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   257: invokestatic 2012	org/telegram/messenger/MessageObject:addLinks	(ZLjava/lang/CharSequence;)V
    //   260: aload_0
    //   261: aload_0
    //   262: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   265: iload 19
    //   267: invokespecial 1968	org/telegram/messenger/MessageObject:addEntitiesToText	(Ljava/lang/CharSequence;Z)Z
    //   270: istore 20
    //   272: aload_0
    //   273: getfield 132	org/telegram/messenger/MessageObject:eventId	J
    //   276: lconst_0
    //   277: lcmp
    //   278: ifne +855 -> 1133
    //   281: aload_0
    //   282: invokevirtual 1813	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   285: ifne +848 -> 1133
    //   288: aload_0
    //   289: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   292: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   295: ifnull +42 -> 337
    //   298: aload_0
    //   299: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   302: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   305: getfield 2017	org/telegram/tgnet/TLRPC$MessageFwdHeader:saved_from_peer	Lorg/telegram/tgnet/TLRPC$Peer;
    //   308: ifnonnull +91 -> 399
    //   311: aload_0
    //   312: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   315: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   318: getfield 2018	org/telegram/tgnet/TLRPC$MessageFwdHeader:from_id	I
    //   321: ifne +78 -> 399
    //   324: aload_0
    //   325: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   328: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   331: getfield 2019	org/telegram/tgnet/TLRPC$MessageFwdHeader:channel_id	I
    //   334: ifne +65 -> 399
    //   337: aload_0
    //   338: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   341: getfield 142	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   344: ifle +789 -> 1133
    //   347: aload_0
    //   348: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   351: getfield 387	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   354: getfield 392	org/telegram/tgnet/TLRPC$Peer:channel_id	I
    //   357: ifne +42 -> 399
    //   360: aload_0
    //   361: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   364: getfield 387	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   367: getfield 1502	org/telegram/tgnet/TLRPC$Peer:chat_id	I
    //   370: ifne +29 -> 399
    //   373: aload_0
    //   374: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   377: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   380: instanceof 259
    //   383: ifne +16 -> 399
    //   386: aload_0
    //   387: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   390: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   393: instanceof 765
    //   396: ifeq +737 -> 1133
    //   399: iconst_1
    //   400: istore 7
    //   402: invokestatic 1891	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   405: ifeq +734 -> 1139
    //   408: invokestatic 1894	org/telegram/messenger/AndroidUtilities:getMinTabletSide	()I
    //   411: istore 8
    //   413: aload_0
    //   414: iload 8
    //   416: putfield 1896	org/telegram/messenger/MessageObject:generatedWithMinSize	I
    //   419: aload_0
    //   420: getfield 1896	org/telegram/messenger/MessageObject:generatedWithMinSize	I
    //   423: istore 8
    //   425: iload 7
    //   427: ifne +12 -> 439
    //   430: aload_0
    //   431: getfield 132	org/telegram/messenger/MessageObject:eventId	J
    //   434: lconst_0
    //   435: lcmp
    //   436: ifeq +714 -> 1150
    //   439: ldc_w 2020
    //   442: fstore_2
    //   443: iload 8
    //   445: fload_2
    //   446: invokestatic 281	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   449: isub
    //   450: istore 8
    //   452: aload_1
    //   453: ifnull +10 -> 463
    //   456: aload_1
    //   457: getfield 2023	org/telegram/tgnet/TLRPC$User:bot	Z
    //   460: ifne +52 -> 512
    //   463: aload_0
    //   464: invokevirtual 395	org/telegram/messenger/MessageObject:isMegagroup	()Z
    //   467: ifne +34 -> 501
    //   470: iload 8
    //   472: istore 7
    //   474: aload_0
    //   475: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   478: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   481: ifnull +42 -> 523
    //   484: iload 8
    //   486: istore 7
    //   488: aload_0
    //   489: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   492: getfield 1667	org/telegram/tgnet/TLRPC$Message:fwd_from	Lorg/telegram/tgnet/TLRPC$MessageFwdHeader;
    //   495: getfield 2019	org/telegram/tgnet/TLRPC$MessageFwdHeader:channel_id	I
    //   498: ifeq +25 -> 523
    //   501: iload 8
    //   503: istore 7
    //   505: aload_0
    //   506: invokevirtual 321	org/telegram/messenger/MessageObject:isOut	()Z
    //   509: ifne +14 -> 523
    //   512: iload 8
    //   514: ldc_w 275
    //   517: invokestatic 281	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   520: isub
    //   521: istore 7
    //   523: iload 7
    //   525: istore 9
    //   527: aload_0
    //   528: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   531: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   534: instanceof 259
    //   537: ifeq +14 -> 551
    //   540: iload 7
    //   542: ldc_w 2024
    //   545: invokestatic 281	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   548: isub
    //   549: istore 9
    //   551: aload_0
    //   552: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   555: getfield 257	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   558: instanceof 259
    //   561: ifeq +596 -> 1157
    //   564: getstatic 263	org/telegram/ui/ActionBar/Theme:chat_msgGameTextPaint	Landroid/text/TextPaint;
    //   567: astore_1
    //   568: getstatic 2029	android/os/Build$VERSION:SDK_INT	I
    //   571: bipush 24
    //   573: if_icmplt +591 -> 1164
    //   576: aload_0
    //   577: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   580: iconst_0
    //   581: aload_0
    //   582: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   585: invokeinterface 299 1 0
    //   590: aload_1
    //   591: iload 9
    //   593: invokestatic 2035	android/text/StaticLayout$Builder:obtain	(Ljava/lang/CharSequence;IILandroid/text/TextPaint;I)Landroid/text/StaticLayout$Builder;
    //   596: iconst_1
    //   597: invokevirtual 2039	android/text/StaticLayout$Builder:setBreakStrategy	(I)Landroid/text/StaticLayout$Builder;
    //   600: iconst_0
    //   601: invokevirtual 2042	android/text/StaticLayout$Builder:setHyphenationFrequency	(I)Landroid/text/StaticLayout$Builder;
    //   604: getstatic 2048	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   607: invokevirtual 2052	android/text/StaticLayout$Builder:setAlignment	(Landroid/text/Layout$Alignment;)Landroid/text/StaticLayout$Builder;
    //   610: invokevirtual 2056	android/text/StaticLayout$Builder:build	()Landroid/text/StaticLayout;
    //   613: astore 21
    //   615: aload_0
    //   616: aload 21
    //   618: invokevirtual 2061	android/text/StaticLayout:getHeight	()I
    //   621: putfield 2063	org/telegram/messenger/MessageObject:textHeight	I
    //   624: aload_0
    //   625: aload 21
    //   627: invokevirtual 2066	android/text/StaticLayout:getLineCount	()I
    //   630: putfield 2068	org/telegram/messenger/MessageObject:linesCount	I
    //   633: getstatic 2029	android/os/Build$VERSION:SDK_INT	I
    //   636: bipush 24
    //   638: if_icmplt +557 -> 1195
    //   641: iconst_1
    //   642: istore 10
    //   644: iconst_0
    //   645: istore 12
    //   647: fconst_0
    //   648: fstore_2
    //   649: iconst_0
    //   650: istore 11
    //   652: iload 11
    //   654: iload 10
    //   656: if_icmpge -629 -> 27
    //   659: getstatic 2029	android/os/Build$VERSION:SDK_INT	I
    //   662: bipush 24
    //   664: if_icmplt +550 -> 1214
    //   667: aload_0
    //   668: getfield 2068	org/telegram/messenger/MessageObject:linesCount	I
    //   671: istore 7
    //   673: new 15	org/telegram/messenger/MessageObject$TextLayoutBlock
    //   676: dup
    //   677: invokespecial 2069	org/telegram/messenger/MessageObject$TextLayoutBlock:<init>	()V
    //   680: astore 22
    //   682: iload 10
    //   684: iconst_1
    //   685: if_icmpne +546 -> 1231
    //   688: aload 22
    //   690: aload 21
    //   692: putfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   695: aload 22
    //   697: fconst_0
    //   698: putfield 2076	org/telegram/messenger/MessageObject$TextLayoutBlock:textYOffset	F
    //   701: aload 22
    //   703: iconst_0
    //   704: putfield 2079	org/telegram/messenger/MessageObject$TextLayoutBlock:charactersOffset	I
    //   707: aload 22
    //   709: aload_0
    //   710: getfield 2063	org/telegram/messenger/MessageObject:textHeight	I
    //   713: putfield 2082	org/telegram/messenger/MessageObject$TextLayoutBlock:height	I
    //   716: iload 7
    //   718: istore 13
    //   720: aload_0
    //   721: getfield 1996	org/telegram/messenger/MessageObject:textLayoutBlocks	Ljava/util/ArrayList;
    //   724: aload 22
    //   726: invokevirtual 974	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   729: pop
    //   730: aload 22
    //   732: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   735: iload 13
    //   737: iconst_1
    //   738: isub
    //   739: invokevirtual 2086	android/text/StaticLayout:getLineLeft	(I)F
    //   742: fstore 4
    //   744: fload 4
    //   746: fstore_3
    //   747: iload 11
    //   749: ifne +22 -> 771
    //   752: fload 4
    //   754: fstore_3
    //   755: fload 4
    //   757: fconst_0
    //   758: fcmpl
    //   759: iflt +12 -> 771
    //   762: aload_0
    //   763: fload 4
    //   765: putfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   768: fload 4
    //   770: fstore_3
    //   771: aload 22
    //   773: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   776: iload 13
    //   778: iconst_1
    //   779: isub
    //   780: invokevirtual 2091	android/text/StaticLayout:getLineWidth	(I)F
    //   783: fstore 4
    //   785: fload 4
    //   787: f2d
    //   788: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   791: d2i
    //   792: istore 7
    //   794: iload 11
    //   796: iload 10
    //   798: iconst_1
    //   799: isub
    //   800: if_icmpne +9 -> 809
    //   803: aload_0
    //   804: iload 7
    //   806: putfield 2097	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   809: fload 4
    //   811: fload_3
    //   812: fadd
    //   813: f2d
    //   814: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   817: d2i
    //   818: istore 17
    //   820: iload 17
    //   822: istore 14
    //   824: iload 13
    //   826: iconst_1
    //   827: if_icmple +861 -> 1688
    //   830: iconst_0
    //   831: istore 16
    //   833: fconst_0
    //   834: fstore_3
    //   835: fconst_0
    //   836: fstore 4
    //   838: iconst_0
    //   839: istore 8
    //   841: iload 7
    //   843: istore 15
    //   845: iload 8
    //   847: iload 13
    //   849: if_icmpge +758 -> 1607
    //   852: aload 22
    //   854: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   857: iload 8
    //   859: invokevirtual 2091	android/text/StaticLayout:getLineWidth	(I)F
    //   862: fstore 5
    //   864: fload 5
    //   866: fstore 6
    //   868: fload 5
    //   870: iload 9
    //   872: bipush 20
    //   874: iadd
    //   875: i2f
    //   876: fcmpl
    //   877: ifle +8 -> 885
    //   880: iload 9
    //   882: i2f
    //   883: fstore 6
    //   885: aload 22
    //   887: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   890: iload 8
    //   892: invokevirtual 2086	android/text/StaticLayout:getLineLeft	(I)F
    //   895: fstore 5
    //   897: fload 5
    //   899: fconst_0
    //   900: fcmpl
    //   901: ifle +682 -> 1583
    //   904: aload_0
    //   905: aload_0
    //   906: getfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   909: fload 5
    //   911: invokestatic 2101	java/lang/Math:min	(FF)F
    //   914: putfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   917: aload 22
    //   919: aload 22
    //   921: getfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   924: iconst_1
    //   925: ior
    //   926: i2b
    //   927: putfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   930: aload_0
    //   931: iconst_1
    //   932: putfield 2107	org/telegram/messenger/MessageObject:hasRtl	Z
    //   935: iload 16
    //   937: istore 7
    //   939: iload 16
    //   941: ifne +39 -> 980
    //   944: iload 16
    //   946: istore 7
    //   948: fload 5
    //   950: fconst_0
    //   951: fcmpl
    //   952: ifne +28 -> 980
    //   955: aload 22
    //   957: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   960: iload 8
    //   962: invokevirtual 2110	android/text/StaticLayout:getParagraphDirection	(I)I
    //   965: istore 18
    //   967: iload 16
    //   969: istore 7
    //   971: iload 18
    //   973: iconst_1
    //   974: if_icmpne +6 -> 980
    //   977: iconst_1
    //   978: istore 7
    //   980: fload_3
    //   981: fload 6
    //   983: invokestatic 2113	java/lang/Math:max	(FF)F
    //   986: fstore_3
    //   987: fload 4
    //   989: fload 6
    //   991: fload 5
    //   993: fadd
    //   994: invokestatic 2113	java/lang/Math:max	(FF)F
    //   997: fstore 4
    //   999: iload 15
    //   1001: fload 6
    //   1003: f2d
    //   1004: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   1007: d2i
    //   1008: invokestatic 2116	java/lang/Math:max	(II)I
    //   1011: istore 15
    //   1013: iload 14
    //   1015: fload 6
    //   1017: fload 5
    //   1019: fadd
    //   1020: f2d
    //   1021: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   1024: d2i
    //   1025: invokestatic 2116	java/lang/Math:max	(II)I
    //   1028: istore 14
    //   1030: iload 8
    //   1032: iconst_1
    //   1033: iadd
    //   1034: istore 8
    //   1036: iload 7
    //   1038: istore 16
    //   1040: goto -195 -> 845
    //   1043: iload 8
    //   1045: iconst_1
    //   1046: iadd
    //   1047: istore 8
    //   1049: goto -985 -> 64
    //   1052: aload_0
    //   1053: getfield 122	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1056: getfield 1029	org/telegram/tgnet/TLRPC$Message:entities	Ljava/util/ArrayList;
    //   1059: invokevirtual 1970	java/util/ArrayList:isEmpty	()Z
    //   1062: ifne +9 -> 1071
    //   1065: iconst_1
    //   1066: istore 7
    //   1068: goto -964 -> 104
    //   1071: iconst_0
    //   1072: istore 7
    //   1074: goto -6 -> 1068
    //   1077: iconst_0
    //   1078: istore 19
    //   1080: goto -836 -> 244
    //   1083: aload_0
    //   1084: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1087: instanceof 293
    //   1090: ifeq -830 -> 260
    //   1093: aload_0
    //   1094: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1097: invokeinterface 299 1 0
    //   1102: sipush 1000
    //   1105: if_icmpge -845 -> 260
    //   1108: aload_0
    //   1109: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1112: checkcast 293	android/text/Spannable
    //   1115: iconst_4
    //   1116: invokestatic 1367	android/text/util/Linkify:addLinks	(Landroid/text/Spannable;I)Z
    //   1119: pop
    //   1120: goto -860 -> 260
    //   1123: astore 21
    //   1125: aload 21
    //   1127: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1130: goto -870 -> 260
    //   1133: iconst_0
    //   1134: istore 7
    //   1136: goto -734 -> 402
    //   1139: getstatic 1901	org/telegram/messenger/AndroidUtilities:displaySize	Landroid/graphics/Point;
    //   1142: getfield 1906	android/graphics/Point:x	I
    //   1145: istore 8
    //   1147: goto -734 -> 413
    //   1150: ldc_w 2117
    //   1153: fstore_2
    //   1154: goto -711 -> 443
    //   1157: getstatic 811	org/telegram/ui/ActionBar/Theme:chat_msgTextPaint	Landroid/text/TextPaint;
    //   1160: astore_1
    //   1161: goto -593 -> 568
    //   1164: new 2058	android/text/StaticLayout
    //   1167: dup
    //   1168: aload_0
    //   1169: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1172: aload_1
    //   1173: iload 9
    //   1175: getstatic 2048	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1178: fconst_1
    //   1179: fconst_0
    //   1180: iconst_0
    //   1181: invokespecial 2120	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1184: astore 21
    //   1186: goto -571 -> 615
    //   1189: astore_1
    //   1190: aload_1
    //   1191: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1194: return
    //   1195: aload_0
    //   1196: getfield 2068	org/telegram/messenger/MessageObject:linesCount	I
    //   1199: i2f
    //   1200: ldc_w 2024
    //   1203: fdiv
    //   1204: f2d
    //   1205: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   1208: d2i
    //   1209: istore 10
    //   1211: goto -567 -> 644
    //   1214: bipush 10
    //   1216: aload_0
    //   1217: getfield 2068	org/telegram/messenger/MessageObject:linesCount	I
    //   1220: iload 12
    //   1222: isub
    //   1223: invokestatic 2122	java/lang/Math:min	(II)I
    //   1226: istore 7
    //   1228: goto -555 -> 673
    //   1231: aload 21
    //   1233: iload 12
    //   1235: invokevirtual 2125	android/text/StaticLayout:getLineStart	(I)I
    //   1238: istore 8
    //   1240: aload 21
    //   1242: iload 12
    //   1244: iload 7
    //   1246: iadd
    //   1247: iconst_1
    //   1248: isub
    //   1249: invokevirtual 2128	android/text/StaticLayout:getLineEnd	(I)I
    //   1252: istore 13
    //   1254: iload 13
    //   1256: iload 8
    //   1258: if_icmpge +12 -> 1270
    //   1261: iload 11
    //   1263: iconst_1
    //   1264: iadd
    //   1265: istore 11
    //   1267: goto -615 -> 652
    //   1270: aload 22
    //   1272: iload 8
    //   1274: putfield 2079	org/telegram/messenger/MessageObject$TextLayoutBlock:charactersOffset	I
    //   1277: aload 22
    //   1279: iload 13
    //   1281: putfield 2131	org/telegram/messenger/MessageObject$TextLayoutBlock:charactersEnd	I
    //   1284: iload 20
    //   1286: ifeq +194 -> 1480
    //   1289: getstatic 2029	android/os/Build$VERSION:SDK_INT	I
    //   1292: bipush 24
    //   1294: if_icmplt +186 -> 1480
    //   1297: aload 22
    //   1299: aload_0
    //   1300: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1303: iload 8
    //   1305: iload 13
    //   1307: aload_1
    //   1308: fconst_2
    //   1309: invokestatic 281	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1312: iload 9
    //   1314: iadd
    //   1315: invokestatic 2035	android/text/StaticLayout$Builder:obtain	(Ljava/lang/CharSequence;IILandroid/text/TextPaint;I)Landroid/text/StaticLayout$Builder;
    //   1318: iconst_1
    //   1319: invokevirtual 2039	android/text/StaticLayout$Builder:setBreakStrategy	(I)Landroid/text/StaticLayout$Builder;
    //   1322: iconst_0
    //   1323: invokevirtual 2042	android/text/StaticLayout$Builder:setHyphenationFrequency	(I)Landroid/text/StaticLayout$Builder;
    //   1326: getstatic 2048	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1329: invokevirtual 2052	android/text/StaticLayout$Builder:setAlignment	(Landroid/text/Layout$Alignment;)Landroid/text/StaticLayout$Builder;
    //   1332: invokevirtual 2056	android/text/StaticLayout$Builder:build	()Landroid/text/StaticLayout;
    //   1335: putfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1338: aload 22
    //   1340: aload 21
    //   1342: iload 12
    //   1344: invokevirtual 2134	android/text/StaticLayout:getLineTop	(I)I
    //   1347: i2f
    //   1348: putfield 2076	org/telegram/messenger/MessageObject$TextLayoutBlock:textYOffset	F
    //   1351: iload 11
    //   1353: ifeq +16 -> 1369
    //   1356: aload 22
    //   1358: aload 22
    //   1360: getfield 2076	org/telegram/messenger/MessageObject$TextLayoutBlock:textYOffset	F
    //   1363: fload_2
    //   1364: fsub
    //   1365: f2i
    //   1366: putfield 2082	org/telegram/messenger/MessageObject$TextLayoutBlock:height	I
    //   1369: aload 22
    //   1371: aload 22
    //   1373: getfield 2082	org/telegram/messenger/MessageObject$TextLayoutBlock:height	I
    //   1376: aload 22
    //   1378: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1381: aload 22
    //   1383: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1386: invokevirtual 2066	android/text/StaticLayout:getLineCount	()I
    //   1389: iconst_1
    //   1390: isub
    //   1391: invokevirtual 2137	android/text/StaticLayout:getLineBottom	(I)I
    //   1394: invokestatic 2116	java/lang/Math:max	(II)I
    //   1397: putfield 2082	org/telegram/messenger/MessageObject$TextLayoutBlock:height	I
    //   1400: aload 22
    //   1402: getfield 2076	org/telegram/messenger/MessageObject$TextLayoutBlock:textYOffset	F
    //   1405: fstore_3
    //   1406: iload 7
    //   1408: istore 13
    //   1410: fload_3
    //   1411: fstore_2
    //   1412: iload 11
    //   1414: iload 10
    //   1416: iconst_1
    //   1417: isub
    //   1418: if_icmpne -698 -> 720
    //   1421: iload 7
    //   1423: aload 22
    //   1425: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1428: invokevirtual 2066	android/text/StaticLayout:getLineCount	()I
    //   1431: invokestatic 2116	java/lang/Math:max	(II)I
    //   1434: istore 13
    //   1436: aload_0
    //   1437: aload_0
    //   1438: getfield 2063	org/telegram/messenger/MessageObject:textHeight	I
    //   1441: aload 22
    //   1443: getfield 2076	org/telegram/messenger/MessageObject$TextLayoutBlock:textYOffset	F
    //   1446: aload 22
    //   1448: getfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1451: invokevirtual 2061	android/text/StaticLayout:getHeight	()I
    //   1454: i2f
    //   1455: fadd
    //   1456: f2i
    //   1457: invokestatic 2116	java/lang/Math:max	(II)I
    //   1460: putfield 2063	org/telegram/messenger/MessageObject:textHeight	I
    //   1463: fload_3
    //   1464: fstore_2
    //   1465: goto -745 -> 720
    //   1468: astore 23
    //   1470: aload 23
    //   1472: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1475: fload_3
    //   1476: fstore_2
    //   1477: goto -757 -> 720
    //   1480: aload 22
    //   1482: new 2058	android/text/StaticLayout
    //   1485: dup
    //   1486: aload_0
    //   1487: getfield 120	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   1490: iload 8
    //   1492: iload 13
    //   1494: aload_1
    //   1495: iload 9
    //   1497: getstatic 2048	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1500: fconst_1
    //   1501: fconst_0
    //   1502: iconst_0
    //   1503: invokespecial 2140	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;IILandroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1506: putfield 2073	org/telegram/messenger/MessageObject$TextLayoutBlock:textLayout	Landroid/text/StaticLayout;
    //   1509: goto -171 -> 1338
    //   1512: astore 22
    //   1514: aload 22
    //   1516: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1519: goto -258 -> 1261
    //   1522: astore 23
    //   1524: fconst_0
    //   1525: fstore_3
    //   1526: iload 11
    //   1528: ifne +8 -> 1536
    //   1531: aload_0
    //   1532: fconst_0
    //   1533: putfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   1536: aload 23
    //   1538: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1541: goto -770 -> 771
    //   1544: astore 23
    //   1546: fconst_0
    //   1547: fstore 4
    //   1549: aload 23
    //   1551: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1554: goto -769 -> 785
    //   1557: astore 23
    //   1559: aload 23
    //   1561: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1564: fconst_0
    //   1565: fstore 5
    //   1567: goto -703 -> 864
    //   1570: astore 23
    //   1572: aload 23
    //   1574: invokestatic 1376	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   1577: fconst_0
    //   1578: fstore 5
    //   1580: goto -683 -> 897
    //   1583: aload 22
    //   1585: aload 22
    //   1587: getfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1590: iconst_2
    //   1591: ior
    //   1592: i2b
    //   1593: putfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1596: goto -661 -> 935
    //   1599: astore 23
    //   1601: iconst_1
    //   1602: istore 7
    //   1604: goto -624 -> 980
    //   1607: iload 16
    //   1609: ifeq +55 -> 1664
    //   1612: fload 4
    //   1614: fstore_3
    //   1615: fload_3
    //   1616: fstore 4
    //   1618: iload 11
    //   1620: iload 10
    //   1622: iconst_1
    //   1623: isub
    //   1624: if_icmpne +12 -> 1636
    //   1627: aload_0
    //   1628: iload 17
    //   1630: putfield 2097	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   1633: fload_3
    //   1634: fstore 4
    //   1636: aload_0
    //   1637: aload_0
    //   1638: getfield 1998	org/telegram/messenger/MessageObject:textWidth	I
    //   1641: fload 4
    //   1643: f2d
    //   1644: invokestatic 2095	java/lang/Math:ceil	(D)D
    //   1647: d2i
    //   1648: invokestatic 2116	java/lang/Math:max	(II)I
    //   1651: putfield 1998	org/telegram/messenger/MessageObject:textWidth	I
    //   1654: iload 12
    //   1656: iload 13
    //   1658: iadd
    //   1659: istore 12
    //   1661: goto -400 -> 1261
    //   1664: fload_3
    //   1665: fstore 4
    //   1667: iload 11
    //   1669: iload 10
    //   1671: iconst_1
    //   1672: isub
    //   1673: if_icmpne -37 -> 1636
    //   1676: aload_0
    //   1677: iload 15
    //   1679: putfield 2097	org/telegram/messenger/MessageObject:lastLineWidth	I
    //   1682: fload_3
    //   1683: fstore 4
    //   1685: goto -49 -> 1636
    //   1688: fload_3
    //   1689: fconst_0
    //   1690: fcmpl
    //   1691: ifle +95 -> 1786
    //   1694: aload_0
    //   1695: aload_0
    //   1696: getfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   1699: fload_3
    //   1700: invokestatic 2101	java/lang/Math:min	(FF)F
    //   1703: putfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   1706: iload 7
    //   1708: istore 8
    //   1710: aload_0
    //   1711: getfield 2088	org/telegram/messenger/MessageObject:textXOffset	F
    //   1714: fconst_0
    //   1715: fcmpl
    //   1716: ifne +11 -> 1727
    //   1719: iload 7
    //   1721: i2f
    //   1722: fload_3
    //   1723: fadd
    //   1724: f2i
    //   1725: istore 8
    //   1727: iload 10
    //   1729: iconst_1
    //   1730: if_icmpeq +50 -> 1780
    //   1733: iconst_1
    //   1734: istore 19
    //   1736: aload_0
    //   1737: iload 19
    //   1739: putfield 2107	org/telegram/messenger/MessageObject:hasRtl	Z
    //   1742: aload 22
    //   1744: aload 22
    //   1746: getfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1749: iconst_1
    //   1750: ior
    //   1751: i2b
    //   1752: putfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1755: iload 8
    //   1757: istore 7
    //   1759: aload_0
    //   1760: aload_0
    //   1761: getfield 1998	org/telegram/messenger/MessageObject:textWidth	I
    //   1764: iload 9
    //   1766: iload 7
    //   1768: invokestatic 2122	java/lang/Math:min	(II)I
    //   1771: invokestatic 2116	java/lang/Math:max	(II)I
    //   1774: putfield 1998	org/telegram/messenger/MessageObject:textWidth	I
    //   1777: goto -123 -> 1654
    //   1780: iconst_0
    //   1781: istore 19
    //   1783: goto -47 -> 1736
    //   1786: aload 22
    //   1788: aload 22
    //   1790: getfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1793: iconst_2
    //   1794: ior
    //   1795: i2b
    //   1796: putfield 2105	org/telegram/messenger/MessageObject$TextLayoutBlock:directionFlags	B
    //   1799: goto -40 -> 1759
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	1802	0	this	MessageObject
    //   0	1802	1	paramUser	TLRPC.User
    //   442	1035	2	f1	float
    //   746	977	3	f2	float
    //   742	942	4	f3	float
    //   862	717	5	f4	float
    //   866	150	6	f5	float
    //   66	1701	7	i	int
    //   62	1694	8	j	int
    //   59	1706	9	k	int
    //   642	1089	10	m	int
    //   650	1024	11	n	int
    //   645	1015	12	i1	int
    //   718	941	13	i2	int
    //   822	207	14	i3	int
    //   843	835	15	i4	int
    //   831	777	16	i5	int
    //   818	811	17	i6	int
    //   965	10	18	i7	int
    //   242	1540	19	bool1	boolean
    //   270	1015	20	bool2	boolean
    //   613	78	21	localStaticLayout1	StaticLayout
    //   1123	3	21	localThrowable	Throwable
    //   1184	157	21	localStaticLayout2	StaticLayout
    //   680	801	22	localTextLayoutBlock	TextLayoutBlock
    //   1512	277	22	localException1	Exception
    //   1468	3	23	localException2	Exception
    //   1522	15	23	localException3	Exception
    //   1544	6	23	localException4	Exception
    //   1557	3	23	localException5	Exception
    //   1570	3	23	localException6	Exception
    //   1599	1	23	localException7	Exception
    // Exception table:
    //   from	to	target	type
    //   1108	1120	1123	java/lang/Throwable
    //   568	615	1189	java/lang/Exception
    //   1164	1186	1189	java/lang/Exception
    //   1436	1463	1468	java/lang/Exception
    //   1289	1338	1512	java/lang/Exception
    //   1338	1351	1512	java/lang/Exception
    //   1356	1369	1512	java/lang/Exception
    //   1369	1406	1512	java/lang/Exception
    //   1480	1509	1512	java/lang/Exception
    //   730	744	1522	java/lang/Exception
    //   762	768	1522	java/lang/Exception
    //   771	785	1544	java/lang/Exception
    //   852	864	1557	java/lang/Exception
    //   885	897	1570	java/lang/Exception
    //   955	967	1599	java/lang/Exception
  }
  
  public void generateLinkDescription()
  {
    if (this.linkDescription != null) {
      return;
    }
    if (((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && ((this.messageOwner.media.webpage instanceof TLRPC.TL_webPage)) && (this.messageOwner.media.webpage.description != null)) {
      this.linkDescription = Spannable.Factory.getInstance().newSpannable(this.messageOwner.media.webpage.description);
    }
    while (this.linkDescription != null)
    {
      if (containsUrls(this.linkDescription)) {}
      try
      {
        Linkify.addLinks((Spannable)this.linkDescription, 1);
        this.linkDescription = Emoji.replaceEmoji(this.linkDescription, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
        return;
        if (((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame)) && (this.messageOwner.media.game.description != null))
        {
          this.linkDescription = Spannable.Factory.getInstance().newSpannable(this.messageOwner.media.game.description);
          continue;
        }
        if ((!(this.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)) || (this.messageOwner.media.description == null)) {
          continue;
        }
        this.linkDescription = Spannable.Factory.getInstance().newSpannable(this.messageOwner.media.description);
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
      }
    }
  }
  
  public void generatePaymentSentMessageText(TLRPC.User paramUser)
  {
    TLRPC.User localUser = paramUser;
    if (paramUser == null) {
      localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf((int)getDialogId()));
    }
    if (localUser != null) {}
    for (paramUser = UserObject.getFirstName(localUser); (this.replyMessageObject != null) && ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)); paramUser = "")
    {
      this.messageText = LocaleController.formatString("PaymentSuccessfullyPaid", 2131494131, new Object[] { LocaleController.getInstance().formatCurrencyString(this.messageOwner.action.total_amount, this.messageOwner.action.currency), paramUser, this.replyMessageObject.messageOwner.media.title });
      return;
    }
    this.messageText = LocaleController.formatString("PaymentSuccessfullyPaidNoItem", 2131494132, new Object[] { LocaleController.getInstance().formatCurrencyString(this.messageOwner.action.total_amount, this.messageOwner.action.currency), paramUser });
  }
  
  public void generatePinMessageText(TLRPC.User paramUser, TLRPC.Chat paramChat)
  {
    Object localObject = paramUser;
    TLRPC.Chat localChat = paramChat;
    if (paramUser == null)
    {
      localObject = paramUser;
      localChat = paramChat;
      if (paramChat == null)
      {
        if (this.messageOwner.from_id > 0) {
          paramUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
        }
        localObject = paramUser;
        localChat = paramChat;
        if (paramUser == null)
        {
          localChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.to_id.channel_id));
          localObject = paramUser;
        }
      }
    }
    if ((this.replyMessageObject == null) || ((this.replyMessageObject.messageOwner instanceof TLRPC.TL_messageEmpty)) || ((this.replyMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionHistoryClear)))
    {
      paramUser = LocaleController.getString("ActionPinnedNoText", 2131492899);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isMusic())
    {
      paramUser = LocaleController.getString("ActionPinnedMusic", 2131492898);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isVideo())
    {
      paramUser = LocaleController.getString("ActionPinnedVideo", 2131492904);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isGif())
    {
      paramUser = LocaleController.getString("ActionPinnedGif", 2131492897);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isVoice())
    {
      paramUser = LocaleController.getString("ActionPinnedVoice", 2131492905);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isRoundVideo())
    {
      paramUser = LocaleController.getString("ActionPinnedRound", 2131492901);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if (this.replyMessageObject.isSticker())
    {
      paramUser = LocaleController.getString("ActionPinnedSticker", 2131492902);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
    {
      paramUser = LocaleController.getString("ActionPinnedFile", 2131492893);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo))
    {
      paramUser = LocaleController.getString("ActionPinnedGeo", 2131492895);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive))
    {
      paramUser = LocaleController.getString("ActionPinnedGeoLive", 2131492896);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
    {
      paramUser = LocaleController.getString("ActionPinnedContact", 2131492892);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
    {
      paramUser = LocaleController.getString("ActionPinnedPhoto", 2131492900);
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
    {
      paramUser = LocaleController.formatString("ActionPinnedGame", 2131492894, new Object[] { "🎮 " + this.replyMessageObject.messageOwner.media.game.title });
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        this.messageText = Emoji.replaceEmoji(this.messageText, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false);
        return;
        localObject = localChat;
      }
    }
    if ((this.replyMessageObject.messageText != null) && (this.replyMessageObject.messageText.length() > 0))
    {
      paramChat = this.replyMessageObject.messageText;
      paramUser = paramChat;
      if (paramChat.length() > 20) {
        paramUser = paramChat.subSequence(0, 20) + "...";
      }
      paramUser = LocaleController.formatString("ActionPinnedText", 2131492903, new Object[] { Emoji.replaceEmoji(paramUser, Theme.chat_msgTextPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0F), false) });
      if (localObject != null) {}
      for (;;)
      {
        this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
        return;
        localObject = localChat;
      }
    }
    paramUser = LocaleController.getString("ActionPinnedNoText", 2131492899);
    if (localObject != null) {}
    for (;;)
    {
      this.messageText = replaceWithLink(paramUser, "un1", (TLObject)localObject);
      return;
      localObject = localChat;
    }
  }
  
  public void generateThumbs(boolean paramBoolean)
  {
    if ((this.messageOwner instanceof TLRPC.TL_messageService)) {
      if ((this.messageOwner.action instanceof TLRPC.TL_messageActionChatEditPhoto))
      {
        if (paramBoolean) {
          break label52;
        }
        this.photoThumbs = new ArrayList(this.messageOwner.action.photo.sizes);
      }
    }
    label51:
    label52:
    label689:
    label843:
    label976:
    label1179:
    do
    {
      do
      {
        break label51;
        break label51;
        return;
        break label51;
        break label51;
        break label51;
        break label51;
        for (;;)
        {
          if ((this.photoThumbs != null) && (!this.photoThumbs.isEmpty()))
          {
            int i = 0;
            TLRPC.PhotoSize localPhotoSize1;
            int j;
            TLRPC.PhotoSize localPhotoSize2;
            while (i < this.photoThumbs.size())
            {
              localPhotoSize1 = (TLRPC.PhotoSize)this.photoThumbs.get(i);
              j = 0;
              if (j < this.messageOwner.action.photo.sizes.size())
              {
                localPhotoSize2 = (TLRPC.PhotoSize)this.messageOwner.action.photo.sizes.get(j);
                if ((localPhotoSize2 instanceof TLRPC.TL_photoSizeEmpty)) {}
                while (!localPhotoSize2.type.equals(localPhotoSize1.type))
                {
                  j += 1;
                  break;
                }
                localPhotoSize1.location = localPhotoSize2.location;
              }
              i += 1;
            }
            continue;
            if ((this.messageOwner.media == null) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty))) {
              break;
            }
            if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
            {
              if ((!paramBoolean) || ((this.photoThumbs != null) && (this.photoThumbs.size() != this.messageOwner.media.photo.sizes.size())))
              {
                this.photoThumbs = new ArrayList(this.messageOwner.media.photo.sizes);
                return;
              }
              if ((this.photoThumbs == null) || (this.photoThumbs.isEmpty())) {
                break;
              }
              i = 0;
              while (i < this.photoThumbs.size())
              {
                localPhotoSize1 = (TLRPC.PhotoSize)this.photoThumbs.get(i);
                j = 0;
                if (j < this.messageOwner.media.photo.sizes.size())
                {
                  localPhotoSize2 = (TLRPC.PhotoSize)this.messageOwner.media.photo.sizes.get(j);
                  if ((localPhotoSize2 instanceof TLRPC.TL_photoSizeEmpty)) {}
                  while (!localPhotoSize2.type.equals(localPhotoSize1.type))
                  {
                    j += 1;
                    break;
                  }
                  localPhotoSize1.location = localPhotoSize2.location;
                }
                i += 1;
              }
              continue;
            }
            if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
            {
              if ((this.messageOwner.media.document.thumb instanceof TLRPC.TL_photoSizeEmpty)) {
                break;
              }
              if (!paramBoolean)
              {
                this.photoThumbs = new ArrayList();
                this.photoThumbs.add(this.messageOwner.media.document.thumb);
                return;
              }
              if ((this.photoThumbs == null) || (this.photoThumbs.isEmpty()) || (this.messageOwner.media.document.thumb == null)) {
                break;
              }
              localPhotoSize1 = (TLRPC.PhotoSize)this.photoThumbs.get(0);
              localPhotoSize1.location = this.messageOwner.media.document.thumb.location;
              localPhotoSize1.w = this.messageOwner.media.document.thumb.w;
              localPhotoSize1.h = this.messageOwner.media.document.thumb.h;
              return;
            }
            if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
            {
              if ((this.messageOwner.media.game.document != null) && (!(this.messageOwner.media.game.document.thumb instanceof TLRPC.TL_photoSizeEmpty)))
              {
                if (!paramBoolean)
                {
                  this.photoThumbs = new ArrayList();
                  this.photoThumbs.add(this.messageOwner.media.game.document.thumb);
                }
              }
              else if (this.messageOwner.media.game.photo != null)
              {
                if ((paramBoolean) && (this.photoThumbs2 != null)) {
                  break label843;
                }
                this.photoThumbs2 = new ArrayList(this.messageOwner.media.game.photo.sizes);
              }
              for (;;)
              {
                if ((this.photoThumbs != null) || (this.photoThumbs2 == null)) {
                  break label976;
                }
                this.photoThumbs = this.photoThumbs2;
                this.photoThumbs2 = null;
                return;
                if ((this.photoThumbs == null) || (this.photoThumbs.isEmpty()) || (this.messageOwner.media.game.document.thumb == null)) {
                  break label689;
                }
                ((TLRPC.PhotoSize)this.photoThumbs.get(0)).location = this.messageOwner.media.game.document.thumb.location;
                break label689;
                if (this.photoThumbs2.isEmpty()) {
                  break;
                }
                i = 0;
                while (i < this.photoThumbs2.size())
                {
                  localPhotoSize1 = (TLRPC.PhotoSize)this.photoThumbs2.get(i);
                  j = 0;
                  if (j < this.messageOwner.media.game.photo.sizes.size())
                  {
                    localPhotoSize2 = (TLRPC.PhotoSize)this.messageOwner.media.game.photo.sizes.get(j);
                    if ((localPhotoSize2 instanceof TLRPC.TL_photoSizeEmpty)) {}
                    while (!localPhotoSize2.type.equals(localPhotoSize1.type))
                    {
                      j += 1;
                      break;
                    }
                    localPhotoSize1.location = localPhotoSize2.location;
                  }
                  i += 1;
                }
              }
              break;
            }
            if (!(this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
              break;
            }
            if (this.messageOwner.media.webpage.photo == null) {
              break label1179;
            }
            if ((!paramBoolean) || (this.photoThumbs == null))
            {
              this.photoThumbs = new ArrayList(this.messageOwner.media.webpage.photo.sizes);
              return;
            }
            if (this.photoThumbs.isEmpty()) {
              break;
            }
            i = 0;
            while (i < this.photoThumbs.size())
            {
              localPhotoSize1 = (TLRPC.PhotoSize)this.photoThumbs.get(i);
              j = 0;
              if (j < this.messageOwner.media.webpage.photo.sizes.size())
              {
                localPhotoSize2 = (TLRPC.PhotoSize)this.messageOwner.media.webpage.photo.sizes.get(j);
                if ((localPhotoSize2 instanceof TLRPC.TL_photoSizeEmpty)) {}
                while (!localPhotoSize2.type.equals(localPhotoSize1.type))
                {
                  j += 1;
                  break;
                }
                localPhotoSize1.location = localPhotoSize2.location;
              }
              i += 1;
            }
          }
        }
      } while ((this.messageOwner.media.webpage.document == null) || ((this.messageOwner.media.webpage.document.thumb instanceof TLRPC.TL_photoSizeEmpty)));
      if (!paramBoolean)
      {
        this.photoThumbs = new ArrayList();
        this.photoThumbs.add(this.messageOwner.media.webpage.document.thumb);
        return;
      }
    } while ((this.photoThumbs == null) || (this.photoThumbs.isEmpty()) || (this.messageOwner.media.webpage.document.thumb == null));
    ((TLRPC.PhotoSize)this.photoThumbs.get(0)).location = this.messageOwner.media.webpage.document.thumb.location;
  }
  
  public int getApproximateHeight()
  {
    int j;
    if (this.type == 0)
    {
      j = this.textHeight;
      if (((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && ((this.messageOwner.media.webpage instanceof TLRPC.TL_webPage))) {}
      for (i = AndroidUtilities.dp(100.0F);; i = 0)
      {
        j += i;
        i = j;
        if (isReply()) {
          i = j + AndroidUtilities.dp(42.0F);
        }
        return i;
      }
    }
    if (this.type == 2) {
      return AndroidUtilities.dp(72.0F);
    }
    if (this.type == 12) {
      return AndroidUtilities.dp(71.0F);
    }
    if (this.type == 9) {
      return AndroidUtilities.dp(100.0F);
    }
    if (this.type == 4) {
      return AndroidUtilities.dp(114.0F);
    }
    if (this.type == 14) {
      return AndroidUtilities.dp(82.0F);
    }
    if (this.type == 10) {
      return AndroidUtilities.dp(30.0F);
    }
    if (this.type == 11) {
      return AndroidUtilities.dp(50.0F);
    }
    if (this.type == 5) {
      return AndroidUtilities.roundMessageSize;
    }
    float f1;
    Object localObject;
    int k;
    if (this.type == 13)
    {
      float f2 = AndroidUtilities.displaySize.y * 0.4F;
      if (AndroidUtilities.isTablet()) {}
      for (f1 = AndroidUtilities.getMinTabletSide() * 0.5F;; f1 = AndroidUtilities.displaySize.x * 0.5F)
      {
        j = 0;
        int m = 0;
        localObject = this.messageOwner.media.document.attributes.iterator();
        TLRPC.DocumentAttribute localDocumentAttribute;
        do
        {
          k = j;
          i = m;
          if (!((Iterator)localObject).hasNext()) {
            break;
          }
          localDocumentAttribute = (TLRPC.DocumentAttribute)((Iterator)localObject).next();
        } while (!(localDocumentAttribute instanceof TLRPC.TL_documentAttributeImageSize));
        i = localDocumentAttribute.w;
        k = localDocumentAttribute.h;
        j = i;
        if (i == 0)
        {
          k = (int)f2;
          j = k + AndroidUtilities.dp(100.0F);
        }
        i = k;
        m = j;
        if (k > f2)
        {
          m = (int)(j * (f2 / k));
          i = (int)f2;
        }
        j = i;
        if (m > f1) {
          j = (int)(i * (f1 / m));
        }
        return j + AndroidUtilities.dp(14.0F);
      }
    }
    if (AndroidUtilities.isTablet())
    {
      i = (int)(AndroidUtilities.getMinTabletSide() * 0.7F);
      k = i + AndroidUtilities.dp(100.0F);
      j = i;
      if (i > AndroidUtilities.getPhotoSize()) {
        j = AndroidUtilities.getPhotoSize();
      }
      i = k;
      if (k > AndroidUtilities.getPhotoSize()) {
        i = AndroidUtilities.getPhotoSize();
      }
      localObject = FileLoader.getClosestPhotoSizeWithSize(this.photoThumbs, AndroidUtilities.getPhotoSize());
      k = i;
      if (localObject != null)
      {
        f1 = ((TLRPC.PhotoSize)localObject).w / j;
        k = (int)(((TLRPC.PhotoSize)localObject).h / f1);
        j = k;
        if (k == 0) {
          j = AndroidUtilities.dp(100.0F);
        }
        if (j <= i) {
          break label595;
        }
        label534:
        if (needDrawBluredPreview()) {
          if (!AndroidUtilities.isTablet()) {
            break label619;
          }
        }
      }
    }
    label595:
    label619:
    for (int i = (int)(AndroidUtilities.getMinTabletSide() * 0.5F);; i = (int)(Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.5F))
    {
      k = i;
      return k + AndroidUtilities.dp(14.0F);
      i = (int)(Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.7F);
      break;
      i = j;
      if (j >= AndroidUtilities.dp(120.0F)) {
        break label534;
      }
      i = AndroidUtilities.dp(120.0F);
      break label534;
    }
  }
  
  public int getChannelId()
  {
    if (this.messageOwner.to_id != null) {
      return this.messageOwner.to_id.channel_id;
    }
    return 0;
  }
  
  public long getDialogId()
  {
    return getDialogId(this.messageOwner);
  }
  
  public TLRPC.Document getDocument()
  {
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return this.messageOwner.media.webpage.document;
    }
    if (this.messageOwner.media != null) {
      return this.messageOwner.media.document;
    }
    return null;
  }
  
  public String getDocumentName()
  {
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
      return FileLoader.getDocumentFileName(this.messageOwner.media.document);
    }
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
      return FileLoader.getDocumentFileName(this.messageOwner.media.webpage.document);
    }
    return "";
  }
  
  public int getDuration()
  {
    TLRPC.Document localDocument;
    int i;
    if (this.type == 0)
    {
      localDocument = this.messageOwner.media.webpage.document;
      i = 0;
    }
    for (;;)
    {
      if (i >= localDocument.attributes.size()) {
        break label91;
      }
      TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)localDocument.attributes.get(i);
      if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio))
      {
        return localDocumentAttribute.duration;
        localDocument = this.messageOwner.media.document;
        break;
      }
      if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeVideo)) {
        return localDocumentAttribute.duration;
      }
      i += 1;
    }
    label91:
    return this.audioPlayerDuration;
  }
  
  public String getExtension()
  {
    Object localObject2 = getFileName();
    int i = ((String)localObject2).lastIndexOf('.');
    Object localObject1 = null;
    if (i != -1) {
      localObject1 = ((String)localObject2).substring(i + 1);
    }
    if (localObject1 != null)
    {
      localObject2 = localObject1;
      if (((String)localObject1).length() != 0) {}
    }
    else
    {
      localObject2 = this.messageOwner.media.document.mime_type;
    }
    localObject1 = localObject2;
    if (localObject2 == null) {
      localObject1 = "";
    }
    return ((String)localObject1).toUpperCase();
  }
  
  public String getFileName()
  {
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
      return FileLoader.getAttachFileName(this.messageOwner.media.document);
    }
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
    {
      Object localObject = this.messageOwner.media.photo.sizes;
      if (((ArrayList)localObject).size() > 0)
      {
        localObject = FileLoader.getClosestPhotoSizeWithSize((ArrayList)localObject, AndroidUtilities.getPhotoSize());
        if (localObject != null) {
          return FileLoader.getAttachFileName((TLObject)localObject);
        }
      }
    }
    else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage))
    {
      return FileLoader.getAttachFileName(this.messageOwner.media.webpage.document);
    }
    return "";
  }
  
  public int getFileType()
  {
    if (isVideo()) {
      return 2;
    }
    if (isVoice()) {
      return 1;
    }
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
      return 3;
    }
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) {
      return 0;
    }
    return 4;
  }
  
  public String getForwardedName()
  {
    if (this.messageOwner.fwd_from != null)
    {
      Object localObject;
      if (this.messageOwner.fwd_from.channel_id != 0)
      {
        localObject = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.channel_id));
        if (localObject != null) {
          return ((TLRPC.Chat)localObject).title;
        }
      }
      else if (this.messageOwner.fwd_from.from_id != 0)
      {
        localObject = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.fwd_from.from_id));
        if (localObject != null) {
          return UserObject.getUserName((TLRPC.User)localObject);
        }
      }
    }
    return null;
  }
  
  public int getFromId()
  {
    if ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.saved_from_peer != null))
    {
      if (this.messageOwner.fwd_from.saved_from_peer.user_id != 0)
      {
        if (this.messageOwner.fwd_from.from_id != 0) {
          return this.messageOwner.fwd_from.from_id;
        }
        return this.messageOwner.fwd_from.saved_from_peer.user_id;
      }
      if (this.messageOwner.fwd_from.saved_from_peer.channel_id != 0)
      {
        if ((isSavedFromMegagroup()) && (this.messageOwner.fwd_from.from_id != 0)) {
          return this.messageOwner.fwd_from.from_id;
        }
        if (this.messageOwner.fwd_from.channel_id != 0) {
          return -this.messageOwner.fwd_from.channel_id;
        }
        return -this.messageOwner.fwd_from.saved_from_peer.channel_id;
      }
      if (this.messageOwner.fwd_from.saved_from_peer.chat_id != 0)
      {
        if (this.messageOwner.fwd_from.from_id != 0) {
          return this.messageOwner.fwd_from.from_id;
        }
        if (this.messageOwner.fwd_from.channel_id != 0) {
          return -this.messageOwner.fwd_from.channel_id;
        }
        return -this.messageOwner.fwd_from.saved_from_peer.chat_id;
      }
    }
    else
    {
      if (this.messageOwner.from_id != 0) {
        return this.messageOwner.from_id;
      }
      if (this.messageOwner.post) {
        return this.messageOwner.to_id.channel_id;
      }
    }
    return 0;
  }
  
  public long getGroupId()
  {
    if (this.localGroupId != 0L) {
      return this.localGroupId;
    }
    return this.messageOwner.grouped_id;
  }
  
  public int getId()
  {
    return this.messageOwner.id;
  }
  
  public long getIdWithChannel()
  {
    long l2 = this.messageOwner.id;
    long l1 = l2;
    if (this.messageOwner.to_id != null)
    {
      l1 = l2;
      if (this.messageOwner.to_id.channel_id != 0) {
        l1 = l2 | this.messageOwner.to_id.channel_id << 32;
      }
    }
    return l1;
  }
  
  public TLRPC.InputStickerSet getInputStickerSet()
  {
    return getInputStickerSet(this.messageOwner);
  }
  
  public String getMimeType()
  {
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument)) {
      return this.messageOwner.media.document.mime_type;
    }
    if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice))
    {
      TLRPC.WebDocument localWebDocument = ((TLRPC.TL_messageMediaInvoice)this.messageOwner.media).photo;
      if (localWebDocument != null) {
        return localWebDocument.mime_type;
      }
    }
    else
    {
      if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) {
        return "image/jpeg";
      }
      if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage))
      {
        if (this.messageOwner.media.webpage.document != null) {
          return this.messageOwner.media.document.mime_type;
        }
        if (this.messageOwner.media.webpage.photo != null) {
          return "image/jpeg";
        }
      }
    }
    return "";
  }
  
  public String getMusicAuthor()
  {
    return getMusicAuthor(true);
  }
  
  public String getMusicAuthor(boolean paramBoolean)
  {
    TLRPC.Document localDocument;
    int k;
    int j;
    if (this.type == 0)
    {
      localDocument = this.messageOwner.media.webpage.document;
      k = 0;
      j = 0;
    }
    for (;;)
    {
      if (j >= localDocument.attributes.size()) {
        break label412;
      }
      Object localObject1 = (TLRPC.DocumentAttribute)localDocument.attributes.get(j);
      int i;
      if ((localObject1 instanceof TLRPC.TL_documentAttributeAudio)) {
        if (((TLRPC.DocumentAttribute)localObject1).voice) {
          i = 1;
        }
      }
      for (;;)
      {
        if (i != 0)
        {
          Object localObject2;
          if (!paramBoolean)
          {
            localObject1 = null;
            do
            {
              do
              {
                return (String)localObject1;
                localDocument = this.messageOwner.media.document;
                break;
                localObject2 = ((TLRPC.DocumentAttribute)localObject1).performer;
                localObject1 = localObject2;
              } while (!TextUtils.isEmpty((CharSequence)localObject2));
              localObject1 = localObject2;
            } while (!paramBoolean);
            return LocaleController.getString("AudioUnknownArtist", 2131493047);
            i = k;
            if ((localObject1 instanceof TLRPC.TL_documentAttributeVideo))
            {
              i = k;
              if (((TLRPC.DocumentAttribute)localObject1).round_message) {
                i = 1;
              }
            }
          }
          else
          {
            if ((isOutOwner()) || ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId()))) {
              return LocaleController.getString("FromYou", 2131493622);
            }
            localObject2 = null;
            localObject1 = null;
            if ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.channel_id != 0)) {
              localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.channel_id));
            }
            while (localObject2 != null)
            {
              return UserObject.getUserName((TLRPC.User)localObject2);
              if ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.from_id != 0)) {
                localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.fwd_from.from_id));
              } else if (this.messageOwner.from_id < 0) {
                localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-this.messageOwner.from_id));
              } else {
                localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.messageOwner.from_id));
              }
            }
            if (localObject1 != null) {
              return ((TLRPC.Chat)localObject1).title;
            }
          }
        }
      }
      j += 1;
      k = i;
    }
    label412:
    return LocaleController.getString("AudioUnknownArtist", 2131493047);
  }
  
  public String getMusicTitle()
  {
    return getMusicTitle(true);
  }
  
  public String getMusicTitle(boolean paramBoolean)
  {
    int i;
    if (this.type == 0)
    {
      localObject2 = this.messageOwner.media.webpage.document;
      i = 0;
    }
    for (;;)
    {
      if (i >= ((TLRPC.Document)localObject2).attributes.size()) {
        break label188;
      }
      localObject1 = (TLRPC.DocumentAttribute)((TLRPC.Document)localObject2).attributes.get(i);
      if ((localObject1 instanceof TLRPC.TL_documentAttributeAudio))
      {
        if (((TLRPC.DocumentAttribute)localObject1).voice) {
          if (!paramBoolean) {
            localObject1 = null;
          }
        }
        label120:
        do
        {
          do
          {
            String str;
            do
            {
              return (String)localObject1;
              localObject2 = this.messageOwner.media.document;
              break;
              return LocaleController.formatDateAudio(this.messageOwner.date);
              str = ((TLRPC.DocumentAttribute)localObject1).title;
              if (str == null) {
                break label120;
              }
              localObject1 = str;
            } while (str.length() != 0);
            localObject2 = FileLoader.getDocumentFileName((TLRPC.Document)localObject2);
            localObject1 = localObject2;
          } while (!TextUtils.isEmpty((CharSequence)localObject2));
          localObject1 = localObject2;
        } while (!paramBoolean);
        return LocaleController.getString("AudioUnknownTitle", 2131493048);
      }
      if (((localObject1 instanceof TLRPC.TL_documentAttributeVideo)) && (((TLRPC.DocumentAttribute)localObject1).round_message)) {
        return LocaleController.formatDateAudio(this.messageOwner.date);
      }
      i += 1;
    }
    label188:
    Object localObject2 = FileLoader.getDocumentFileName((TLRPC.Document)localObject2);
    Object localObject1 = localObject2;
    if (TextUtils.isEmpty((CharSequence)localObject2)) {
      localObject1 = LocaleController.getString("AudioUnknownTitle", 2131493048);
    }
    return (String)localObject1;
  }
  
  public int getSecretTimeLeft()
  {
    int i = this.messageOwner.ttl;
    if (this.messageOwner.destroyTime != 0) {
      i = Math.max(0, this.messageOwner.destroyTime - ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
    }
    return i;
  }
  
  public String getSecretTimeString()
  {
    if (!isSecretMedia()) {
      return null;
    }
    int i = getSecretTimeLeft();
    if (i < 60) {
      return i + "s";
    }
    return i / 60 + "m";
  }
  
  public int getSize()
  {
    return getMessageSize(this.messageOwner);
  }
  
  public String getStickerEmoji()
  {
    Object localObject2 = null;
    int i = 0;
    for (;;)
    {
      Object localObject1 = localObject2;
      if (i < this.messageOwner.media.document.attributes.size())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)this.messageOwner.media.document.attributes.get(i);
        if (!(localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker)) {
          break label87;
        }
        localObject1 = localObject2;
        if (localDocumentAttribute.alt != null)
        {
          localObject1 = localObject2;
          if (localDocumentAttribute.alt.length() > 0) {
            localObject1 = localDocumentAttribute.alt;
          }
        }
      }
      return (String)localObject1;
      label87:
      i += 1;
    }
  }
  
  public String getStrickerChar()
  {
    if ((this.messageOwner.media != null) && (this.messageOwner.media.document != null))
    {
      Iterator localIterator = this.messageOwner.media.document.attributes.iterator();
      while (localIterator.hasNext())
      {
        TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)localIterator.next();
        if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker)) {
          return localDocumentAttribute.alt;
        }
      }
    }
    return null;
  }
  
  public int getUnradFlags()
  {
    return getUnreadFlags(this.messageOwner);
  }
  
  public ArrayList<MessageObject> getWebPagePhotos(ArrayList<MessageObject> paramArrayList, ArrayList<TLRPC.PageBlock> paramArrayList1)
  {
    TLRPC.WebPage localWebPage = this.messageOwner.media.webpage;
    if (paramArrayList == null)
    {
      paramArrayList = new ArrayList();
      if (localWebPage.cached_page != null) {
        break label37;
      }
    }
    label37:
    label206:
    for (;;)
    {
      return paramArrayList;
      break;
      int i;
      if (paramArrayList1 == null)
      {
        paramArrayList1 = localWebPage.cached_page.blocks;
        i = 0;
      }
      for (;;)
      {
        if (i >= paramArrayList1.size()) {
          break label206;
        }
        Object localObject = (TLRPC.PageBlock)paramArrayList1.get(i);
        int j;
        if ((localObject instanceof TLRPC.TL_pageBlockSlideshow))
        {
          localObject = (TLRPC.TL_pageBlockSlideshow)localObject;
          j = 0;
          while (j < ((TLRPC.TL_pageBlockSlideshow)localObject).items.size())
          {
            paramArrayList.add(getMessageObjectForBlock(localWebPage, (TLRPC.PageBlock)((TLRPC.TL_pageBlockSlideshow)localObject).items.get(j)));
            j += 1;
          }
          break;
        }
        if ((localObject instanceof TLRPC.TL_pageBlockCollage))
        {
          localObject = (TLRPC.TL_pageBlockCollage)localObject;
          j = 0;
          while (j < ((TLRPC.TL_pageBlockCollage)localObject).items.size())
          {
            paramArrayList.add(getMessageObjectForBlock(localWebPage, (TLRPC.PageBlock)((TLRPC.TL_pageBlockCollage)localObject).items.get(j)));
            j += 1;
          }
        }
        i += 1;
      }
    }
  }
  
  public boolean hasPhotoStickers()
  {
    return (this.messageOwner.media != null) && (this.messageOwner.media.photo != null) && (this.messageOwner.media.photo.has_stickers);
  }
  
  public boolean hasValidGroupId()
  {
    return (getGroupId() != 0L) && (this.photoThumbs != null) && (!this.photoThumbs.isEmpty());
  }
  
  public boolean hasValidReplyMessageObject()
  {
    return (this.replyMessageObject != null) && (!(this.replyMessageObject.messageOwner instanceof TLRPC.TL_messageEmpty)) && (!(this.replyMessageObject.messageOwner.action instanceof TLRPC.TL_messageActionHistoryClear));
  }
  
  public boolean isContentUnread()
  {
    return this.messageOwner.media_unread;
  }
  
  public boolean isFcmMessage()
  {
    return this.localType != 0;
  }
  
  public boolean isForwarded()
  {
    return isForwardedMessage(this.messageOwner);
  }
  
  public boolean isFromUser()
  {
    return (this.messageOwner.from_id > 0) && (!this.messageOwner.post);
  }
  
  public boolean isGame()
  {
    return isGameMessage(this.messageOwner);
  }
  
  public boolean isGif()
  {
    return isGifMessage(this.messageOwner);
  }
  
  public boolean isInvoice()
  {
    return isInvoiceMessage(this.messageOwner);
  }
  
  public boolean isLiveLocation()
  {
    return isLiveLocationMessage(this.messageOwner);
  }
  
  public boolean isMask()
  {
    return isMaskMessage(this.messageOwner);
  }
  
  public boolean isMediaEmpty()
  {
    return isMediaEmpty(this.messageOwner);
  }
  
  public boolean isMegagroup()
  {
    return isMegagroup(this.messageOwner);
  }
  
  public boolean isMusic()
  {
    return isMusicMessage(this.messageOwner);
  }
  
  public boolean isNewGif()
  {
    return (this.messageOwner.media != null) && (isNewGifDocument(this.messageOwner.media.document));
  }
  
  public boolean isOut()
  {
    return this.messageOwner.out;
  }
  
  public boolean isOutOwner()
  {
    if ((!this.messageOwner.out) || (this.messageOwner.from_id <= 0) || (this.messageOwner.post)) {}
    int i;
    do
    {
      do
      {
        return false;
        if (this.messageOwner.fwd_from == null) {
          return true;
        }
        i = UserConfig.getInstance(this.currentAccount).getClientUserId();
        if (getDialogId() != i) {
          break;
        }
      } while ((this.messageOwner.fwd_from.from_id != i) && ((this.messageOwner.fwd_from.saved_from_peer == null) || (this.messageOwner.fwd_from.saved_from_peer.user_id != i)));
      return true;
    } while ((this.messageOwner.fwd_from.saved_from_peer != null) && (this.messageOwner.fwd_from.saved_from_peer.user_id != i));
    return true;
  }
  
  public boolean isReply()
  {
    return ((this.replyMessageObject == null) || (!(this.replyMessageObject.messageOwner instanceof TLRPC.TL_messageEmpty))) && ((this.messageOwner.reply_to_msg_id != 0) || (this.messageOwner.reply_to_random_id != 0L)) && ((this.messageOwner.flags & 0x8) != 0);
  }
  
  public boolean isRoundVideo()
  {
    if (this.isRoundVideoCached == 0) {
      if ((this.type != 5) && (!isRoundVideoMessage(this.messageOwner))) {
        break label42;
      }
    }
    label42:
    for (int i = 1;; i = 2)
    {
      this.isRoundVideoCached = i;
      if (this.isRoundVideoCached != 1) {
        break;
      }
      return true;
    }
    return false;
  }
  
  public boolean isSavedFromMegagroup()
  {
    if ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.saved_from_peer != null) && (this.messageOwner.fwd_from.saved_from_peer.channel_id != 0)) {
      return ChatObject.isMegagroup(MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.messageOwner.fwd_from.saved_from_peer.channel_id)));
    }
    return false;
  }
  
  public boolean isSecretMedia()
  {
    boolean bool2 = true;
    boolean bool1 = false;
    if ((this.messageOwner instanceof TLRPC.TL_message_secret)) {
      if (((!(this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) && (!isGif())) || (((this.messageOwner.ttl > 0) && (this.messageOwner.ttl <= 60)) || (isVoice()) || (isRoundVideo()) || (isVideo()))) {
        bool1 = true;
      }
    }
    while (!(this.messageOwner instanceof TLRPC.TL_message)) {
      return bool1;
    }
    if ((((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))) && (this.messageOwner.media.ttl_seconds != 0)) {}
    for (bool1 = bool2;; bool1 = false) {
      return bool1;
    }
  }
  
  public boolean isSendError()
  {
    return (this.messageOwner.send_state == 2) && (this.messageOwner.id < 0);
  }
  
  public boolean isSending()
  {
    return (this.messageOwner.send_state == 1) && (this.messageOwner.id < 0);
  }
  
  public boolean isSent()
  {
    return (this.messageOwner.send_state == 0) || (this.messageOwner.id > 0);
  }
  
  public boolean isSticker()
  {
    if (this.type != 1000) {
      return this.type == 13;
    }
    return isStickerMessage(this.messageOwner);
  }
  
  public boolean isUnread()
  {
    return this.messageOwner.unread;
  }
  
  public boolean isVideo()
  {
    return isVideoMessage(this.messageOwner);
  }
  
  public boolean isVoice()
  {
    return isVoiceMessage(this.messageOwner);
  }
  
  public boolean isWebpage()
  {
    return this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage;
  }
  
  public boolean isWebpageDocument()
  {
    return ((this.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) && (this.messageOwner.media.webpage.document != null) && (!isGifDocument(this.messageOwner.media.webpage.document));
  }
  
  public void measureInlineBotButtons()
  {
    this.wantedBotKeyboardWidth = 0;
    if (!(this.messageOwner.reply_markup instanceof TLRPC.TL_replyInlineMarkup)) {}
    label99:
    label346:
    for (;;)
    {
      return;
      Theme.createChatResources(null, true);
      int i;
      if (this.botButtonsLayout == null)
      {
        this.botButtonsLayout = new StringBuilder();
        i = 0;
      }
      for (;;)
      {
        if (i >= this.messageOwner.reply_markup.rows.size()) {
          break label346;
        }
        TLRPC.TL_keyboardButtonRow localTL_keyboardButtonRow = (TLRPC.TL_keyboardButtonRow)this.messageOwner.reply_markup.rows.get(i);
        int k = 0;
        int n = localTL_keyboardButtonRow.buttons.size();
        int j = 0;
        if (j < n)
        {
          Object localObject = (TLRPC.KeyboardButton)localTL_keyboardButtonRow.buttons.get(j);
          this.botButtonsLayout.append(i).append(j);
          if (((localObject instanceof TLRPC.TL_keyboardButtonBuy)) && ((this.messageOwner.media.flags & 0x4) != 0)) {}
          for (localObject = LocaleController.getString("PaymentReceipt", 2131494115);; localObject = Emoji.replaceEmoji(((TLRPC.KeyboardButton)localObject).text, Theme.chat_msgBotButtonPaint.getFontMetricsInt(), AndroidUtilities.dp(15.0F), false))
          {
            localObject = new StaticLayout((CharSequence)localObject, Theme.chat_msgBotButtonPaint, AndroidUtilities.dp(2000.0F), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
            int m = k;
            if (((StaticLayout)localObject).getLineCount() > 0)
            {
              float f2 = ((StaticLayout)localObject).getLineWidth(0);
              float f3 = ((StaticLayout)localObject).getLineLeft(0);
              float f1 = f2;
              if (f3 < f2) {
                f1 = f2 - f3;
              }
              m = Math.max(k, (int)Math.ceil(f1) + AndroidUtilities.dp(4.0F));
            }
            j += 1;
            k = m;
            break label99;
            this.botButtonsLayout.setLength(0);
            break;
          }
        }
        this.wantedBotKeyboardWidth = Math.max(this.wantedBotKeyboardWidth, (AndroidUtilities.dp(12.0F) + k) * n + AndroidUtilities.dp(5.0F) * (n - 1));
        i += 1;
      }
    }
  }
  
  public boolean needDrawAvatar()
  {
    return (isFromUser()) || (this.eventId != 0L) || ((this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.saved_from_peer != null));
  }
  
  public boolean needDrawBluredPreview()
  {
    if ((this.messageOwner instanceof TLRPC.TL_message_secret))
    {
      int i = Math.max(this.messageOwner.ttl, this.messageOwner.media.ttl_seconds);
      if ((i <= 0) || ((((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) || (isVideo()) || (isGif())) && ((i > 60) && (!isRoundVideo())))) {}
    }
    do
    {
      return true;
      return false;
      if (!(this.messageOwner instanceof TLRPC.TL_message)) {
        break;
      }
    } while ((((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto)) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))) && (this.messageOwner.media.ttl_seconds != 0));
    return false;
    return false;
  }
  
  public boolean needDrawForwarded()
  {
    return ((this.messageOwner.flags & 0x4) != 0) && (this.messageOwner.fwd_from != null) && (this.messageOwner.fwd_from.saved_from_peer == null) && (UserConfig.getInstance(this.currentAccount).getClientUserId() != getDialogId());
  }
  
  public CharSequence replaceWithLink(CharSequence paramCharSequence, String paramString, ArrayList<Integer> paramArrayList, AbstractMap<Integer, TLRPC.User> paramAbstractMap, SparseArray<TLRPC.User> paramSparseArray)
  {
    Object localObject1 = paramCharSequence;
    if (TextUtils.indexOf(paramCharSequence, paramString) >= 0)
    {
      SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder("");
      int i = 0;
      if (i < paramArrayList.size())
      {
        localObject1 = null;
        if (paramAbstractMap != null) {
          localObject1 = (TLRPC.User)paramAbstractMap.get(paramArrayList.get(i));
        }
        for (;;)
        {
          Object localObject2 = localObject1;
          if (localObject1 == null) {
            localObject2 = MessagesController.getInstance(this.currentAccount).getUser((Integer)paramArrayList.get(i));
          }
          if (localObject2 != null)
          {
            localObject1 = UserObject.getUserName((TLRPC.User)localObject2);
            int j = localSpannableStringBuilder.length();
            if (localSpannableStringBuilder.length() != 0) {
              localSpannableStringBuilder.append(", ");
            }
            localSpannableStringBuilder.append((CharSequence)localObject1);
            localSpannableStringBuilder.setSpan(new URLSpanNoUnderlineBold("" + ((TLRPC.User)localObject2).id), j, ((String)localObject1).length() + j, 33);
          }
          i += 1;
          break;
          if (paramSparseArray != null) {
            localObject1 = (TLRPC.User)paramSparseArray.get(((Integer)paramArrayList.get(i)).intValue());
          }
        }
      }
      localObject1 = TextUtils.replace(paramCharSequence, new String[] { paramString }, new CharSequence[] { localSpannableStringBuilder });
    }
    return (CharSequence)localObject1;
  }
  
  public CharSequence replaceWithLink(CharSequence paramCharSequence, String paramString, TLObject paramTLObject)
  {
    int i = TextUtils.indexOf(paramCharSequence, paramString);
    if (i >= 0)
    {
      String str;
      if ((paramTLObject instanceof TLRPC.User))
      {
        str = UserObject.getUserName((TLRPC.User)paramTLObject);
        paramTLObject = "" + ((TLRPC.User)paramTLObject).id;
      }
      for (;;)
      {
        str = str.replace('\n', ' ');
        paramCharSequence = new SpannableStringBuilder(TextUtils.replace(paramCharSequence, new String[] { paramString }, new String[] { str }));
        paramCharSequence.setSpan(new URLSpanNoUnderlineBold("" + paramTLObject), i, str.length() + i, 33);
        return paramCharSequence;
        if ((paramTLObject instanceof TLRPC.Chat))
        {
          str = ((TLRPC.Chat)paramTLObject).title;
          paramTLObject = "" + -((TLRPC.Chat)paramTLObject).id;
        }
        else if ((paramTLObject instanceof TLRPC.TL_game))
        {
          str = ((TLRPC.TL_game)paramTLObject).title;
          paramTLObject = "game";
        }
        else
        {
          str = "";
          paramTLObject = "0";
        }
      }
    }
    return paramCharSequence;
  }
  
  public void resetPlayingProgress()
  {
    this.audioProgress = 0.0F;
    this.audioProgressSec = 0;
    this.bufferedProgress = 0.0F;
  }
  
  public void setContentIsRead()
  {
    this.messageOwner.media_unread = false;
  }
  
  public void setIsRead()
  {
    this.messageOwner.unread = false;
  }
  
  public void setType()
  {
    int i = this.type;
    this.isRoundVideoCached = 0;
    if (((this.messageOwner instanceof TLRPC.TL_message)) || ((this.messageOwner instanceof TLRPC.TL_messageForwarded_old2))) {
      if (isMediaEmpty())
      {
        this.type = 0;
        if ((TextUtils.isEmpty(this.messageText)) && (this.eventId == 0L)) {
          this.messageText = "Empty message";
        }
      }
    }
    for (;;)
    {
      if ((i != 1000) && (i != this.type)) {
        generateThumbs(false);
      }
      return;
      if ((this.messageOwner.media.ttl_seconds != 0) && (((this.messageOwner.media.photo instanceof TLRPC.TL_photoEmpty)) || ((this.messageOwner.media.document instanceof TLRPC.TL_documentEmpty))))
      {
        this.contentType = 1;
        this.type = 10;
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaPhoto))
      {
        this.type = 1;
      }
      else if (((this.messageOwner.media instanceof TLRPC.TL_messageMediaGeo)) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaVenue)) || ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGeoLive)))
      {
        this.type = 4;
      }
      else if (isRoundVideo())
      {
        this.type = 5;
      }
      else if (isVideo())
      {
        this.type = 3;
      }
      else if (isVoice())
      {
        this.type = 2;
      }
      else if (isMusic())
      {
        this.type = 14;
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaContact))
      {
        this.type = 12;
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaUnsupported))
      {
        this.type = 0;
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaDocument))
      {
        if ((this.messageOwner.media.document != null) && (this.messageOwner.media.document.mime_type != null))
        {
          if (isGifDocument(this.messageOwner.media.document)) {
            this.type = 8;
          } else if ((this.messageOwner.media.document.mime_type.equals("image/webp")) && (isSticker())) {
            this.type = 13;
          } else {
            this.type = 9;
          }
        }
        else {
          this.type = 9;
        }
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaGame))
      {
        this.type = 0;
      }
      else if ((this.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice))
      {
        this.type = 0;
        continue;
        if ((this.messageOwner instanceof TLRPC.TL_messageService)) {
          if ((this.messageOwner.action instanceof TLRPC.TL_messageActionLoginUnknownLocation))
          {
            this.type = 0;
          }
          else if (((this.messageOwner.action instanceof TLRPC.TL_messageActionChatEditPhoto)) || ((this.messageOwner.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto)))
          {
            this.contentType = 1;
            this.type = 11;
          }
          else if ((this.messageOwner.action instanceof TLRPC.TL_messageEncryptedAction))
          {
            if (((this.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionScreenshotMessages)) || ((this.messageOwner.action.encryptedAction instanceof TLRPC.TL_decryptedMessageActionSetMessageTTL)))
            {
              this.contentType = 1;
              this.type = 10;
            }
            else
            {
              this.contentType = -1;
              this.type = -1;
            }
          }
          else if ((this.messageOwner.action instanceof TLRPC.TL_messageActionHistoryClear))
          {
            this.contentType = -1;
            this.type = -1;
          }
          else if ((this.messageOwner.action instanceof TLRPC.TL_messageActionPhoneCall))
          {
            this.type = 16;
          }
          else
          {
            this.contentType = 1;
            this.type = 10;
          }
        }
      }
    }
  }
  
  public boolean shouldEncryptPhotoOrVideo()
  {
    return shouldEncryptPhotoOrVideo(this.messageOwner);
  }
  
  public static class GroupedMessagePosition
  {
    public float aspectRatio;
    public boolean edge;
    public int flags;
    public boolean last;
    public int leftSpanOffset;
    public byte maxX;
    public byte maxY;
    public byte minX;
    public byte minY;
    public float ph;
    public int pw;
    public float[] siblingHeights;
    public int spanSize;
    
    public void set(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, float paramFloat, int paramInt6)
    {
      this.minX = ((byte)paramInt1);
      this.maxX = ((byte)paramInt2);
      this.minY = ((byte)paramInt3);
      this.maxY = ((byte)paramInt4);
      this.pw = paramInt5;
      this.spanSize = paramInt5;
      this.ph = paramFloat;
      this.flags = ((byte)paramInt6);
    }
  }
  
  public static class GroupedMessages
  {
    private int firstSpanAdditionalSize = 200;
    public long groupId;
    public boolean hasSibling;
    private int maxSizeWidth = 800;
    public ArrayList<MessageObject> messages = new ArrayList();
    public ArrayList<MessageObject.GroupedMessagePosition> posArray = new ArrayList();
    public HashMap<MessageObject, MessageObject.GroupedMessagePosition> positions = new HashMap();
    
    private float multiHeight(float[] paramArrayOfFloat, int paramInt1, int paramInt2)
    {
      float f = 0.0F;
      while (paramInt1 < paramInt2)
      {
        f += paramArrayOfFloat[paramInt1];
        paramInt1 += 1;
      }
      return this.maxSizeWidth / f;
    }
    
    public void calculate()
    {
      this.posArray.clear();
      this.positions.clear();
      int i3 = this.messages.size();
      if (i3 <= 1) {
        return;
      }
      Object localObject1 = new StringBuilder();
      float f1 = 1.0F;
      boolean bool1 = false;
      int m = 0;
      int i1 = 0;
      int j = 0;
      int i = 0;
      this.hasSibling = false;
      int k = 0;
      Object localObject2;
      label197:
      Object localObject3;
      MessageObject.GroupedMessagePosition localGroupedMessagePosition;
      if (k < i3)
      {
        localObject2 = (MessageObject)this.messages.get(k);
        boolean bool2;
        if (k == 0)
        {
          bool1 = ((MessageObject)localObject2).isOutOwner();
          if ((!bool1) && (((((MessageObject)localObject2).messageOwner.fwd_from != null) && (((MessageObject)localObject2).messageOwner.fwd_from.saved_from_peer != null)) || ((((MessageObject)localObject2).messageOwner.from_id > 0) && ((((MessageObject)localObject2).messageOwner.to_id.channel_id != 0) || (((MessageObject)localObject2).messageOwner.to_id.chat_id != 0) || ((((MessageObject)localObject2).messageOwner.media instanceof TLRPC.TL_messageMediaGame)) || ((((MessageObject)localObject2).messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)))))) {
            i = 1;
          }
        }
        else
        {
          localObject3 = FileLoader.getClosestPhotoSizeWithSize(((MessageObject)localObject2).photoThumbs, AndroidUtilities.getPhotoSize());
          localGroupedMessagePosition = new MessageObject.GroupedMessagePosition();
          if (k != i3 - 1) {
            break label328;
          }
          bool2 = true;
          label231:
          localGroupedMessagePosition.last = bool2;
          if (localObject3 != null) {
            break label334;
          }
          f2 = 1.0F;
          label245:
          localGroupedMessagePosition.aspectRatio = f2;
          if (localGroupedMessagePosition.aspectRatio <= 1.2F) {
            break label351;
          }
          ((StringBuilder)localObject1).append("w");
        }
        for (;;)
        {
          f1 += localGroupedMessagePosition.aspectRatio;
          if (localGroupedMessagePosition.aspectRatio > 2.0F) {
            j = 1;
          }
          this.positions.put(localObject2, localGroupedMessagePosition);
          this.posArray.add(localGroupedMessagePosition);
          k += 1;
          break;
          i = 0;
          break label197;
          label328:
          bool2 = false;
          break label231;
          label334:
          f2 = ((TLRPC.PhotoSize)localObject3).w / ((TLRPC.PhotoSize)localObject3).h;
          break label245;
          label351:
          if (localGroupedMessagePosition.aspectRatio < 0.8F) {
            ((StringBuilder)localObject1).append("n");
          } else {
            ((StringBuilder)localObject1).append("q");
          }
        }
      }
      if (i != 0)
      {
        this.maxSizeWidth -= 50;
        this.firstSpanAdditionalSize += 50;
      }
      int i2 = AndroidUtilities.dp(120.0F);
      int n = (int)(AndroidUtilities.dp(120.0F) / (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / this.maxSizeWidth));
      k = (int)(AndroidUtilities.dp(40.0F) / (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) / this.maxSizeWidth));
      float f2 = this.maxSizeWidth / 814.0F;
      f1 /= i3;
      if ((j == 0) && ((i3 == 2) || (i3 == 3) || (i3 == 4))) {
        if (i3 == 2)
        {
          localObject2 = (MessageObject.GroupedMessagePosition)this.posArray.get(0);
          localObject3 = (MessageObject.GroupedMessagePosition)this.posArray.get(1);
          localObject1 = ((StringBuilder)localObject1).toString();
          if ((((String)localObject1).equals("ww")) && (f1 > 1.4D * f2) && (((MessageObject.GroupedMessagePosition)localObject2).aspectRatio - ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio < 0.2D))
          {
            f1 = Math.round(Math.min(this.maxSizeWidth / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio, Math.min(this.maxSizeWidth / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio, 814.0F / 2.0F))) / 814.0F;
            ((MessageObject.GroupedMessagePosition)localObject2).set(0, 0, 0, 0, this.maxSizeWidth, f1, 7);
            ((MessageObject.GroupedMessagePosition)localObject3).set(0, 0, 1, 1, this.maxSizeWidth, f1, 11);
            i = i1;
            label676:
            j = 0;
            label679:
            if (j < i3)
            {
              localObject1 = (MessageObject.GroupedMessagePosition)this.posArray.get(j);
              if (!bool1) {
                break label3162;
              }
              if (((MessageObject.GroupedMessagePosition)localObject1).minX == 0) {
                ((MessageObject.GroupedMessagePosition)localObject1).spanSize += this.firstSpanAdditionalSize;
              }
              if ((((MessageObject.GroupedMessagePosition)localObject1).flags & 0x2) != 0) {
                ((MessageObject.GroupedMessagePosition)localObject1).edge = true;
              }
              label744:
              localObject2 = (MessageObject)this.messages.get(j);
              if ((!bool1) && (((MessageObject)localObject2).needDrawAvatar()))
              {
                if (!((MessageObject.GroupedMessagePosition)localObject1).edge) {
                  break label3216;
                }
                if (((MessageObject.GroupedMessagePosition)localObject1).spanSize != 1000) {
                  ((MessageObject.GroupedMessagePosition)localObject1).spanSize += 108;
                }
                ((MessageObject.GroupedMessagePosition)localObject1).pw += 108;
              }
            }
          }
        }
      }
      for (;;)
      {
        j += 1;
        break label679;
        break;
        if ((((String)localObject1).equals("ww")) || (((String)localObject1).equals("qq")))
        {
          i = this.maxSizeWidth / 2;
          f1 = Math.round(Math.min(i / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio, Math.min(i / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio, 814.0F))) / 814.0F;
          ((MessageObject.GroupedMessagePosition)localObject2).set(0, 0, 0, 0, i, f1, 13);
          ((MessageObject.GroupedMessagePosition)localObject3).set(1, 1, 0, 0, i, f1, 14);
          i = 1;
          break label676;
        }
        k = (int)Math.max(0.4F * this.maxSizeWidth, Math.round(this.maxSizeWidth / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio / (1.0F / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio + 1.0F / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio)));
        m = this.maxSizeWidth - k;
        i = m;
        j = k;
        if (m < n)
        {
          i = n;
          j = k - (n - m);
        }
        f1 = Math.min(814.0F, Math.round(Math.min(i / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio, j / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio))) / 814.0F;
        ((MessageObject.GroupedMessagePosition)localObject2).set(0, 0, 0, 0, i, f1, 13);
        ((MessageObject.GroupedMessagePosition)localObject3).set(1, 1, 0, 0, j, f1, 14);
        i = 1;
        break label676;
        if (i3 == 3)
        {
          localObject2 = (MessageObject.GroupedMessagePosition)this.posArray.get(0);
          localObject3 = (MessageObject.GroupedMessagePosition)this.posArray.get(1);
          localGroupedMessagePosition = (MessageObject.GroupedMessagePosition)this.posArray.get(2);
          if (((StringBuilder)localObject1).charAt(0) == 'n')
          {
            f1 = Math.min(0.5F * 814.0F, Math.round(((MessageObject.GroupedMessagePosition)localObject3).aspectRatio * this.maxSizeWidth / (localGroupedMessagePosition.aspectRatio + ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio)));
            f2 = 814.0F - f1;
            i = (int)Math.max(n, Math.min(this.maxSizeWidth * 0.5F, Math.round(Math.min(localGroupedMessagePosition.aspectRatio * f1, ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio * f2))));
            j = Math.round(Math.min(((MessageObject.GroupedMessagePosition)localObject2).aspectRatio * 814.0F + k, this.maxSizeWidth - i));
            ((MessageObject.GroupedMessagePosition)localObject2).set(0, 0, 0, 1, j, 1.0F, 13);
            ((MessageObject.GroupedMessagePosition)localObject3).set(1, 1, 0, 0, i, f2 / 814.0F, 6);
            localGroupedMessagePosition.set(0, 1, 1, 1, i, f1 / 814.0F, 10);
            localGroupedMessagePosition.spanSize = this.maxSizeWidth;
            ((MessageObject.GroupedMessagePosition)localObject2).siblingHeights = new float[] { f1 / 814.0F, f2 / 814.0F };
            if (bool1) {
              ((MessageObject.GroupedMessagePosition)localObject2).spanSize = (this.maxSizeWidth - i);
            }
            for (;;)
            {
              this.hasSibling = true;
              i = 1;
              break;
              ((MessageObject.GroupedMessagePosition)localObject3).spanSize = (this.maxSizeWidth - j);
              localGroupedMessagePosition.leftSpanOffset = j;
            }
          }
          f1 = Math.round(Math.min(this.maxSizeWidth / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio, 0.66F * 814.0F)) / 814.0F;
          ((MessageObject.GroupedMessagePosition)localObject2).set(0, 1, 0, 0, this.maxSizeWidth, f1, 7);
          i = this.maxSizeWidth / 2;
          f1 = Math.min(814.0F - f1, Math.round(Math.min(i / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio, i / localGroupedMessagePosition.aspectRatio))) / 814.0F;
          ((MessageObject.GroupedMessagePosition)localObject3).set(0, 0, 1, 1, i, f1, 9);
          localGroupedMessagePosition.set(1, 1, 1, 1, i, f1, 10);
          i = 1;
          break label676;
        }
        i = i1;
        if (i3 != 4) {
          break label676;
        }
        localObject2 = (MessageObject.GroupedMessagePosition)this.posArray.get(0);
        localObject3 = (MessageObject.GroupedMessagePosition)this.posArray.get(1);
        localGroupedMessagePosition = (MessageObject.GroupedMessagePosition)this.posArray.get(2);
        Object localObject4 = (MessageObject.GroupedMessagePosition)this.posArray.get(3);
        if (((StringBuilder)localObject1).charAt(0) == 'w')
        {
          f1 = Math.round(Math.min(this.maxSizeWidth / ((MessageObject.GroupedMessagePosition)localObject2).aspectRatio, 0.66F * 814.0F)) / 814.0F;
          ((MessageObject.GroupedMessagePosition)localObject2).set(0, 2, 0, 0, this.maxSizeWidth, f1, 7);
          f2 = Math.round(this.maxSizeWidth / (((MessageObject.GroupedMessagePosition)localObject3).aspectRatio + localGroupedMessagePosition.aspectRatio + ((MessageObject.GroupedMessagePosition)localObject4).aspectRatio));
          i = (int)Math.max(n, Math.min(this.maxSizeWidth * 0.4F, ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio * f2));
          j = (int)Math.max(Math.max(n, this.maxSizeWidth * 0.33F), ((MessageObject.GroupedMessagePosition)localObject4).aspectRatio * f2);
          k = this.maxSizeWidth;
          f1 = Math.min(814.0F - f1, f2) / 814.0F;
          ((MessageObject.GroupedMessagePosition)localObject3).set(0, 0, 1, 1, i, f1, 9);
          localGroupedMessagePosition.set(1, 1, 1, 1, k - i - j, f1, 8);
          ((MessageObject.GroupedMessagePosition)localObject4).set(2, 2, 1, 1, j, f1, 10);
          i = 2;
          break label676;
        }
        f1 = 1.0F / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio;
        f2 = 1.0F / localGroupedMessagePosition.aspectRatio;
        i = Math.max(n, Math.round(814.0F / (1.0F / ((MessageObject.GroupedMessagePosition)this.posArray.get(3)).aspectRatio + (f2 + f1))));
        f1 = Math.min(0.33F, Math.max(i2, i / ((MessageObject.GroupedMessagePosition)localObject3).aspectRatio) / 814.0F);
        f2 = Math.min(0.33F, Math.max(i2, i / localGroupedMessagePosition.aspectRatio) / 814.0F);
        float f3 = 1.0F - f1 - f2;
        j = Math.round(Math.min(((MessageObject.GroupedMessagePosition)localObject2).aspectRatio * 814.0F + k, this.maxSizeWidth - i));
        ((MessageObject.GroupedMessagePosition)localObject2).set(0, 0, 0, 2, j, f1 + f2 + f3, 13);
        ((MessageObject.GroupedMessagePosition)localObject3).set(1, 1, 0, 0, i, f1, 6);
        localGroupedMessagePosition.set(0, 1, 1, 1, i, f2, 2);
        localGroupedMessagePosition.spanSize = this.maxSizeWidth;
        ((MessageObject.GroupedMessagePosition)localObject4).set(0, 1, 2, 2, i, f3, 10);
        ((MessageObject.GroupedMessagePosition)localObject4).spanSize = this.maxSizeWidth;
        if (bool1) {
          ((MessageObject.GroupedMessagePosition)localObject2).spanSize = (this.maxSizeWidth - i);
        }
        for (;;)
        {
          ((MessageObject.GroupedMessagePosition)localObject2).siblingHeights = new float[] { f1, f2, f3 };
          this.hasSibling = true;
          i = 1;
          break;
          ((MessageObject.GroupedMessagePosition)localObject3).spanSize = (this.maxSizeWidth - j);
          localGroupedMessagePosition.leftSpanOffset = j;
          ((MessageObject.GroupedMessagePosition)localObject4).leftSpanOffset = j;
        }
        localObject4 = new float[this.posArray.size()];
        i = 0;
        if (i < i3)
        {
          if (f1 > 1.1F) {
            localObject4[i] = Math.max(1.0F, ((MessageObject.GroupedMessagePosition)this.posArray.get(i)).aspectRatio);
          }
          for (;;)
          {
            localObject4[i] = Math.max(0.66667F, Math.min(1.7F, localObject4[i]));
            i += 1;
            break;
            localObject4[i] = Math.min(1.0F, ((MessageObject.GroupedMessagePosition)this.posArray.get(i)).aspectRatio);
          }
        }
        localObject2 = new ArrayList();
        i = 1;
        if (i < localObject4.length)
        {
          j = localObject4.length - i;
          if ((i > 3) || (j > 3)) {}
          for (;;)
          {
            i += 1;
            break;
            ((ArrayList)localObject2).add(new MessageGroupedLayoutAttempt(i, j, multiHeight((float[])localObject4, 0, i), multiHeight((float[])localObject4, i, localObject4.length)));
          }
        }
        i = 1;
        while (i < localObject4.length - 1)
        {
          j = 1;
          if (j < localObject4.length - i)
          {
            i1 = localObject4.length - i - j;
            if (i <= 3)
            {
              if (f1 >= 0.85F) {
                break label2318;
              }
              k = 4;
              label2296:
              if ((j <= k) && (i1 <= 3)) {
                break label2324;
              }
            }
            for (;;)
            {
              j += 1;
              break;
              label2318:
              k = 3;
              break label2296;
              label2324:
              ((ArrayList)localObject2).add(new MessageGroupedLayoutAttempt(i, j, i1, multiHeight((float[])localObject4, 0, i), multiHeight((float[])localObject4, i, i + j), multiHeight((float[])localObject4, i + j, localObject4.length)));
            }
          }
          i += 1;
        }
        i = 1;
        while (i < localObject4.length - 2)
        {
          j = 1;
          while (j < localObject4.length - i)
          {
            k = 1;
            if (k < localObject4.length - i - j)
            {
              i1 = localObject4.length - i - j - k;
              if ((i > 3) || (j > 3) || (k > 3) || (i1 > 3)) {}
              for (;;)
              {
                k += 1;
                break;
                ((ArrayList)localObject2).add(new MessageGroupedLayoutAttempt(i, j, k, i1, multiHeight((float[])localObject4, 0, i), multiHeight((float[])localObject4, i, i + j), multiHeight((float[])localObject4, i + j, i + j + k), multiHeight((float[])localObject4, i + j + k, localObject4.length)));
              }
            }
            j += 1;
          }
          i += 1;
        }
        localObject3 = null;
        f3 = 0.0F;
        float f5 = this.maxSizeWidth / 3 * 4;
        i = 0;
        while (i < ((ArrayList)localObject2).size())
        {
          localObject1 = (MessageGroupedLayoutAttempt)((ArrayList)localObject2).get(i);
          f2 = 0.0F;
          float f4 = Float.MAX_VALUE;
          j = 0;
          while (j < ((MessageGroupedLayoutAttempt)localObject1).heights.length)
          {
            f2 += localObject1.heights[j];
            f1 = f4;
            if (localObject1.heights[j] < f4) {
              f1 = localObject1.heights[j];
            }
            j += 1;
            f4 = f1;
          }
          f2 = Math.abs(f2 - f5);
          f1 = f2;
          if (((MessageGroupedLayoutAttempt)localObject1).lineCounts.length > 1) {
            if ((localObject1.lineCounts[0] <= localObject1.lineCounts[1]) && ((((MessageGroupedLayoutAttempt)localObject1).lineCounts.length <= 2) || (localObject1.lineCounts[1] <= localObject1.lineCounts[2])))
            {
              f1 = f2;
              if (((MessageGroupedLayoutAttempt)localObject1).lineCounts.length > 3)
              {
                f1 = f2;
                if (localObject1.lineCounts[2] <= localObject1.lineCounts[3]) {}
              }
            }
            else
            {
              f1 = f2 * 1.2F;
            }
          }
          f2 = f1;
          if (f4 < n) {
            f2 = f1 * 1.5F;
          }
          if (localObject3 != null)
          {
            f1 = f3;
            if (f2 >= f3) {}
          }
          else
          {
            localObject3 = localObject1;
            f1 = f2;
          }
          i += 1;
          f3 = f1;
        }
        if (localObject3 == null) {
          break;
        }
        i = 0;
        f1 = 0.0F;
        k = 0;
        j = m;
        m = i;
        for (;;)
        {
          i = j;
          if (k >= ((MessageGroupedLayoutAttempt)localObject3).lineCounts.length) {
            break;
          }
          int i4 = localObject3.lineCounts[k];
          f2 = localObject3.heights[k];
          i = this.maxSizeWidth;
          localObject2 = null;
          i1 = Math.max(j, i4 - 1);
          n = 0;
          while (n < i4)
          {
            int i5 = (int)(localObject4[m] * f2);
            i2 = i - i5;
            localGroupedMessagePosition = (MessageObject.GroupedMessagePosition)this.posArray.get(m);
            i = 0;
            if (k == 0) {
              i = 0x0 | 0x4;
            }
            j = i;
            if (k == ((MessageGroupedLayoutAttempt)localObject3).lineCounts.length - 1) {
              j = i | 0x8;
            }
            i = j;
            localObject1 = localObject2;
            if (n == 0)
            {
              j |= 0x1;
              i = j;
              localObject1 = localObject2;
              if (bool1)
              {
                localObject1 = localGroupedMessagePosition;
                i = j;
              }
            }
            j = i;
            localObject2 = localObject1;
            if (n == i4 - 1)
            {
              i |= 0x2;
              j = i;
              localObject2 = localObject1;
              if (!bool1)
              {
                localObject2 = localGroupedMessagePosition;
                j = i;
              }
            }
            localGroupedMessagePosition.set(n, n, k, k, i5, f2 / 814.0F, j);
            m += 1;
            n += 1;
            i = i2;
          }
          ((MessageObject.GroupedMessagePosition)localObject2).pw += i;
          ((MessageObject.GroupedMessagePosition)localObject2).spanSize += i;
          f1 += f2;
          k += 1;
          j = i1;
        }
        label3162:
        if ((((MessageObject.GroupedMessagePosition)localObject1).maxX == i) || ((((MessageObject.GroupedMessagePosition)localObject1).flags & 0x2) != 0)) {
          ((MessageObject.GroupedMessagePosition)localObject1).spanSize += this.firstSpanAdditionalSize;
        }
        if ((((MessageObject.GroupedMessagePosition)localObject1).flags & 0x1) == 0) {
          break label744;
        }
        ((MessageObject.GroupedMessagePosition)localObject1).edge = true;
        break label744;
        label3216:
        if ((((MessageObject.GroupedMessagePosition)localObject1).flags & 0x2) != 0) {
          if (((MessageObject.GroupedMessagePosition)localObject1).spanSize != 1000) {
            ((MessageObject.GroupedMessagePosition)localObject1).spanSize -= 108;
          } else if (((MessageObject.GroupedMessagePosition)localObject1).leftSpanOffset != 0) {
            ((MessageObject.GroupedMessagePosition)localObject1).leftSpanOffset += 108;
          }
        }
      }
    }
    
    private class MessageGroupedLayoutAttempt
    {
      public float[] heights;
      public int[] lineCounts;
      
      public MessageGroupedLayoutAttempt(int paramInt1, int paramInt2, float paramFloat1, float paramFloat2)
      {
        this.lineCounts = new int[] { paramInt1, paramInt2 };
        this.heights = new float[] { paramFloat1, paramFloat2 };
      }
      
      public MessageGroupedLayoutAttempt(int paramInt1, int paramInt2, int paramInt3, float paramFloat1, float paramFloat2, float paramFloat3)
      {
        this.lineCounts = new int[] { paramInt1, paramInt2, paramInt3 };
        this.heights = new float[] { paramFloat1, paramFloat2, paramFloat3 };
      }
      
      public MessageGroupedLayoutAttempt(int paramInt1, int paramInt2, int paramInt3, int paramInt4, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
      {
        this.lineCounts = new int[] { paramInt1, paramInt2, paramInt3, paramInt4 };
        this.heights = new float[] { paramFloat1, paramFloat2, paramFloat3, paramFloat4 };
      }
    }
  }
  
  public static class TextLayoutBlock
  {
    public int charactersEnd;
    public int charactersOffset;
    public byte directionFlags;
    public int height;
    public int heightByOffset;
    public StaticLayout textLayout;
    public float textYOffset;
    
    public boolean isRtl()
    {
      return ((this.directionFlags & 0x1) != 0) && ((this.directionFlags & 0x2) == 0);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/MessageObject.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */