package org.telegram.ui.Components.voip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;

public class DarkTheme
{
  public static int getColor(String paramString)
  {
    int j = -7697782;
    label2112:
    int i;
    switch (paramString.hashCode())
    {
    default: 
      i = -1;
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        FileLog.w("returning color for key " + paramString + " from current theme");
        j = Theme.getColor(paramString);
      case 0: 
      case 63: 
      case 80: 
      case 181: 
      case 228: 
      case 239: 
      case 261: 
        return j;
        if (!paramString.equals("avatar_subtitleInProfilePink")) {
          break label2112;
        }
        i = 0;
        continue;
        if (!paramString.equals("chat_secretTimerBackground")) {
          break label2112;
        }
        i = 1;
        continue;
        if (!paramString.equals("chat_emojiPanelTrendingDescription")) {
          break label2112;
        }
        i = 2;
        continue;
        if (!paramString.equals("chat_inFileBackground")) {
          break label2112;
        }
        i = 3;
        continue;
        if (!paramString.equals("chat_emojiPanelIconSelected")) {
          break label2112;
        }
        i = 4;
        continue;
        if (!paramString.equals("actionBarActionModeDefaultSelector")) {
          break label2112;
        }
        i = 5;
        continue;
        if (!paramString.equals("chats_menuItemIcon")) {
          break label2112;
        }
        i = 6;
        continue;
        if (!paramString.equals("chat_inTimeText")) {
          break label2112;
        }
        i = 7;
        continue;
        if (!paramString.equals("avatar_backgroundGroupCreateSpanBlue")) {
          break label2112;
        }
        i = 8;
        continue;
        if (!paramString.equals("windowBackgroundGray")) {
          break label2112;
        }
        i = 9;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGreenText2")) {
          break label2112;
        }
        i = 10;
        continue;
        if (!paramString.equals("chat_emojiPanelBackspace")) {
          break label2112;
        }
        i = 11;
        continue;
        if (!paramString.equals("chat_outPreviewInstantSelectedText")) {
          break label2112;
        }
        i = 12;
        continue;
        if (!paramString.equals("chat_inBubble")) {
          break label2112;
        }
        i = 13;
        continue;
        if (!paramString.equals("chat_outFileInfoSelectedText")) {
          break label2112;
        }
        i = 14;
        continue;
        if (!paramString.equals("chat_outLoaderSelected")) {
          break label2112;
        }
        i = 15;
        continue;
        if (!paramString.equals("chat_emojiPanelIcon")) {
          break label2112;
        }
        i = 16;
        continue;
        if (!paramString.equals("chat_selectedBackground")) {
          break label2112;
        }
        i = 17;
        continue;
        if (!paramString.equals("chats_pinnedIcon")) {
          break label2112;
        }
        i = 18;
        continue;
        if (!paramString.equals("player_actionBarTitle")) {
          break label2112;
        }
        i = 19;
        continue;
        if (!paramString.equals("chat_muteIcon")) {
          break label2112;
        }
        i = 20;
        continue;
        if (!paramString.equals("chat_mediaMenu")) {
          break label2112;
        }
        i = 21;
        continue;
        if (!paramString.equals("chat_addContact")) {
          break label2112;
        }
        i = 22;
        continue;
        if (!paramString.equals("chat_outMenu")) {
          break label2112;
        }
        i = 23;
        continue;
        if (!paramString.equals("actionBarActionModeDefault")) {
          break label2112;
        }
        i = 24;
        continue;
        if (!paramString.equals("chat_emojiPanelShadowLine")) {
          break label2112;
        }
        i = 25;
        continue;
        if (!paramString.equals("dialogBackground")) {
          break label2112;
        }
        i = 26;
        continue;
        if (!paramString.equals("chat_inPreviewInstantText")) {
          break label2112;
        }
        i = 27;
        continue;
        if (!paramString.equals("chat_outVoiceSeekbarSelected")) {
          break label2112;
        }
        i = 28;
        continue;
        if (!paramString.equals("chat_outForwardedNameText")) {
          break label2112;
        }
        i = 29;
        continue;
        if (!paramString.equals("chat_outFileProgressSelected")) {
          break label2112;
        }
        i = 30;
        continue;
        if (!paramString.equals("player_progressBackground")) {
          break label2112;
        }
        i = 31;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorRed")) {
          break label2112;
        }
        i = 32;
        continue;
        if (!paramString.equals("player_button")) {
          break label2112;
        }
        i = 33;
        continue;
        if (!paramString.equals("chat_inVoiceSeekbar")) {
          break label2112;
        }
        i = 34;
        continue;
        if (!paramString.equals("switchThumb")) {
          break label2112;
        }
        i = 35;
        continue;
        if (!paramString.equals("chats_tabletSelectedOverlay")) {
          break label2112;
        }
        i = 36;
        continue;
        if (!paramString.equals("chats_menuItemText")) {
          break label2112;
        }
        i = 37;
        continue;
        if (!paramString.equals("chat_outFileNameText")) {
          break label2112;
        }
        i = 38;
        continue;
        if (!paramString.equals("divider")) {
          break label2112;
        }
        i = 39;
        continue;
        if (!paramString.equals("chat_outViews")) {
          break label2112;
        }
        i = 40;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorBlue")) {
          break label2112;
        }
        i = 41;
        continue;
        if (!paramString.equals("chats_actionMessage")) {
          break label2112;
        }
        i = 42;
        continue;
        if (!paramString.equals("groupcreate_spanBackground")) {
          break label2112;
        }
        i = 43;
        continue;
        if (!paramString.equals("chat_messageTextIn")) {
          break label2112;
        }
        i = 44;
        continue;
        if (!paramString.equals("chat_outLoaderPhoto")) {
          break label2112;
        }
        i = 45;
        continue;
        if (!paramString.equals("chat_outFileIcon")) {
          break label2112;
        }
        i = 46;
        continue;
        if (!paramString.equals("chat_serviceBackgroundSelected")) {
          break label2112;
        }
        i = 47;
        continue;
        if (!paramString.equals("inappPlayerBackground")) {
          break label2112;
        }
        i = 48;
        continue;
        if (!paramString.equals("chat_topPanelLine")) {
          break label2112;
        }
        i = 49;
        continue;
        if (!paramString.equals("player_actionBar")) {
          break label2112;
        }
        i = 50;
        continue;
        if (!paramString.equals("chat_outFileInfoText")) {
          break label2112;
        }
        i = 51;
        continue;
        if (!paramString.equals("chat_outLoaderPhotoIcon")) {
          break label2112;
        }
        i = 52;
        continue;
        if (!paramString.equals("chat_unreadMessagesStartArrowIcon")) {
          break label2112;
        }
        i = 53;
        continue;
        if (!paramString.equals("chat_outAudioProgress")) {
          break label2112;
        }
        i = 54;
        continue;
        if (!paramString.equals("chat_outBubbleShadow")) {
          break label2112;
        }
        i = 55;
        continue;
        if (!paramString.equals("chat_inMenuSelected")) {
          break label2112;
        }
        i = 56;
        continue;
        if (!paramString.equals("chat_inContactIcon")) {
          break label2112;
        }
        i = 57;
        continue;
        if (!paramString.equals("chat_messageTextOut")) {
          break label2112;
        }
        i = 58;
        continue;
        if (!paramString.equals("chat_outAudioTitleText")) {
          break label2112;
        }
        i = 59;
        continue;
        if (!paramString.equals("chat_inLoaderPhotoSelected")) {
          break label2112;
        }
        i = 60;
        continue;
        if (!paramString.equals("inappPlayerPerformer")) {
          break label2112;
        }
        i = 61;
        continue;
        if (!paramString.equals("actionBarActionModeDefaultTop")) {
          break label2112;
        }
        i = 62;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileCyan")) {
          break label2112;
        }
        i = 63;
        continue;
        if (!paramString.equals("profile_actionBackground")) {
          break label2112;
        }
        i = 64;
        continue;
        if (!paramString.equals("chat_outSentClockSelected")) {
          break label2112;
        }
        i = 65;
        continue;
        if (!paramString.equals("avatar_nameInMessageGreen")) {
          break label2112;
        }
        i = 66;
        continue;
        if (!paramString.equals("chat_outAudioSeekbarFill")) {
          break label2112;
        }
        i = 67;
        continue;
        if (!paramString.equals("player_placeholder")) {
          break label2112;
        }
        i = 68;
        continue;
        if (!paramString.equals("chat_inReplyNameText")) {
          break label2112;
        }
        i = 69;
        continue;
        if (!paramString.equals("chat_messagePanelIcons")) {
          break label2112;
        }
        i = 70;
        continue;
        if (!paramString.equals("graySection")) {
          break label2112;
        }
        i = 71;
        continue;
        if (!paramString.equals("chats_nameIcon")) {
          break label2112;
        }
        i = 72;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarViolet")) {
          break label2112;
        }
        i = 73;
        continue;
        if (!paramString.equals("chat_emojiPanelIconSelector")) {
          break label2112;
        }
        i = 74;
        continue;
        if (!paramString.equals("chat_replyPanelMessage")) {
          break label2112;
        }
        i = 75;
        continue;
        if (!paramString.equals("chat_outPreviewInstantText")) {
          break label2112;
        }
        i = 76;
        continue;
        if (!paramString.equals("chat_emojiPanelTrendingTitle")) {
          break label2112;
        }
        i = 77;
        continue;
        if (!paramString.equals("chat_inPreviewInstantSelectedText")) {
          break label2112;
        }
        i = 78;
        continue;
        if (!paramString.equals("chat_inFileInfoSelectedText")) {
          break label2112;
        }
        i = 79;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileRed")) {
          break label2112;
        }
        i = 80;
        continue;
        if (!paramString.equals("chat_outLocationIcon")) {
          break label2112;
        }
        i = 81;
        continue;
        if (!paramString.equals("chat_inAudioPerfomerText")) {
          break label2112;
        }
        i = 82;
        continue;
        if (!paramString.equals("chats_attachMessage")) {
          break label2112;
        }
        i = 83;
        continue;
        if (!paramString.equals("chat_messageLinkIn")) {
          break label2112;
        }
        i = 84;
        continue;
        if (!paramString.equals("chats_unreadCounter")) {
          break label2112;
        }
        i = 85;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGrayText")) {
          break label2112;
        }
        i = 86;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGrayText3")) {
          break label2112;
        }
        i = 87;
        continue;
        if (!paramString.equals("actionBarDefaultSubmenuBackground")) {
          break label2112;
        }
        i = 88;
        continue;
        if (!paramString.equals("chat_outSentCheckSelected")) {
          break label2112;
        }
        i = 89;
        continue;
        if (!paramString.equals("chat_outTimeSelectedText")) {
          break label2112;
        }
        i = 90;
        continue;
        if (!paramString.equals("chat_outFileSelectedIcon")) {
          break label2112;
        }
        i = 91;
        continue;
        if (!paramString.equals("chats_secretIcon")) {
          break label2112;
        }
        i = 92;
        continue;
        if (!paramString.equals("dialogIcon")) {
          break label2112;
        }
        i = 93;
        continue;
        if (!paramString.equals("chat_outAudioPerfomerText")) {
          break label2112;
        }
        i = 94;
        continue;
        if (!paramString.equals("chats_pinnedOverlay")) {
          break label2112;
        }
        i = 95;
        continue;
        if (!paramString.equals("chat_outContactIcon")) {
          break label2112;
        }
        i = 96;
        continue;
        if (!paramString.equals("windowBackgroundWhiteBlueHeader")) {
          break label2112;
        }
        i = 97;
        continue;
        if (!paramString.equals("actionBarDefaultSelector")) {
          break label2112;
        }
        i = 98;
        continue;
        if (!paramString.equals("chat_emojiPanelEmptyText")) {
          break label2112;
        }
        i = 99;
        continue;
        if (!paramString.equals("chat_inViews")) {
          break label2112;
        }
        i = 100;
        continue;
        if (!paramString.equals("listSelector")) {
          break label2112;
        }
        i = 101;
        continue;
        if (!paramString.equals("chat_messagePanelBackground")) {
          break label2112;
        }
        i = 102;
        continue;
        if (!paramString.equals("chats_secretName")) {
          break label2112;
        }
        i = 103;
        continue;
        if (!paramString.equals("chat_inReplyLine")) {
          break label2112;
        }
        i = 104;
        continue;
        if (!paramString.equals("actionBarDefaultSubtitle")) {
          break label2112;
        }
        i = 105;
        continue;
        if (!paramString.equals("switchThumbChecked")) {
          break label2112;
        }
        i = 106;
        continue;
        if (!paramString.equals("chat_inReplyMessageText")) {
          break label2112;
        }
        i = 107;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorGreen")) {
          break label2112;
        }
        i = 108;
        continue;
        if (!paramString.equals("chat_inFileIcon")) {
          break label2112;
        }
        i = 109;
        continue;
        if (!paramString.equals("chat_inAudioTitleText")) {
          break label2112;
        }
        i = 110;
        continue;
        if (!paramString.equals("chat_inAudioDurationSelectedText")) {
          break label2112;
        }
        i = 111;
        continue;
        if (!paramString.equals("chat_outSentClock")) {
          break label2112;
        }
        i = 112;
        continue;
        if (!paramString.equals("actionBarDefault")) {
          break label2112;
        }
        i = 113;
        continue;
        if (!paramString.equals("chat_goDownButton")) {
          break label2112;
        }
        i = 114;
        continue;
        if (!paramString.equals("chat_inAudioSelectedProgress")) {
          break label2112;
        }
        i = 115;
        continue;
        if (!paramString.equals("profile_actionPressedBackground")) {
          break label2112;
        }
        i = 116;
        continue;
        if (!paramString.equals("chat_outContactPhoneText")) {
          break label2112;
        }
        i = 117;
        continue;
        if (!paramString.equals("chat_inVenueInfoText")) {
          break label2112;
        }
        i = 118;
        continue;
        if (!paramString.equals("chat_outAudioDurationText")) {
          break label2112;
        }
        i = 119;
        continue;
        if (!paramString.equals("windowBackgroundWhiteLinkText")) {
          break label2112;
        }
        i = 120;
        continue;
        if (!paramString.equals("chat_outSiteNameText")) {
          break label2112;
        }
        i = 121;
        continue;
        if (!paramString.equals("chat_inBubbleSelected")) {
          break label2112;
        }
        i = 122;
        continue;
        if (!paramString.equals("chats_date")) {
          break label2112;
        }
        i = 123;
        continue;
        if (!paramString.equals("chat_outFileProgress")) {
          break label2112;
        }
        i = 124;
        continue;
        if (!paramString.equals("chat_outBubbleSelected")) {
          break label2112;
        }
        i = 125;
        continue;
        if (!paramString.equals("progressCircle")) {
          break label2112;
        }
        i = 126;
        continue;
        if (!paramString.equals("chats_unreadCounterMuted")) {
          break label2112;
        }
        i = 127;
        continue;
        if (!paramString.equals("stickers_menu")) {
          break label2112;
        }
        i = 128;
        continue;
        if (!paramString.equals("chat_outAudioSeekbarSelected")) {
          break label2112;
        }
        i = 129;
        continue;
        if (!paramString.equals("chat_inSiteNameText")) {
          break label2112;
        }
        i = 130;
        continue;
        if (!paramString.equals("chat_inFileProgressSelected")) {
          break label2112;
        }
        i = 131;
        continue;
        if (!paramString.equals("chat_topPanelMessage")) {
          break label2112;
        }
        i = 132;
        continue;
        if (!paramString.equals("chat_outVoiceSeekbar")) {
          break label2112;
        }
        i = 133;
        continue;
        if (!paramString.equals("chat_topPanelBackground")) {
          break label2112;
        }
        i = 134;
        continue;
        if (!paramString.equals("chat_outVenueInfoSelectedText")) {
          break label2112;
        }
        i = 135;
        continue;
        if (!paramString.equals("chats_menuTopShadow")) {
          break label2112;
        }
        i = 136;
        continue;
        if (!paramString.equals("dialogTextBlack")) {
          break label2112;
        }
        i = 137;
        continue;
        if (!paramString.equals("player_actionBarItems")) {
          break label2112;
        }
        i = 138;
        continue;
        if (!paramString.equals("files_folderIcon")) {
          break label2112;
        }
        i = 139;
        continue;
        if (!paramString.equals("chat_inReplyMediaMessageSelectedText")) {
          break label2112;
        }
        i = 140;
        continue;
        if (!paramString.equals("chat_inViewsSelected")) {
          break label2112;
        }
        i = 141;
        continue;
        if (!paramString.equals("chat_outAudioDurationSelectedText")) {
          break label2112;
        }
        i = 142;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarGreen")) {
          break label2112;
        }
        i = 143;
        continue;
        if (!paramString.equals("profile_verifiedCheck")) {
          break label2112;
        }
        i = 144;
        continue;
        if (!paramString.equals("chat_outViewsSelected")) {
          break label2112;
        }
        i = 145;
        continue;
        if (!paramString.equals("switchTrackChecked")) {
          break label2112;
        }
        i = 146;
        continue;
        if (!paramString.equals("chat_serviceBackground")) {
          break label2112;
        }
        i = 147;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGrayText2")) {
          break label2112;
        }
        i = 148;
        continue;
        if (!paramString.equals("chat_inFileSelectedIcon")) {
          break label2112;
        }
        i = 149;
        continue;
        if (!paramString.equals("profile_actionIcon")) {
          break label2112;
        }
        i = 150;
        continue;
        if (!paramString.equals("chat_secretChatStatusText")) {
          break label2112;
        }
        i = 151;
        continue;
        if (!paramString.equals("chat_emojiPanelBackground")) {
          break label2112;
        }
        i = 152;
        continue;
        if (!paramString.equals("chat_inPreviewLine")) {
          break label2112;
        }
        i = 153;
        continue;
        if (!paramString.equals("chat_unreadMessagesStartBackground")) {
          break label2112;
        }
        i = 154;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarBlue")) {
          break label2112;
        }
        i = 155;
        continue;
        if (!paramString.equals("chat_inViaBotNameText")) {
          break label2112;
        }
        i = 156;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorCyan")) {
          break label2112;
        }
        i = 157;
        continue;
        if (!paramString.equals("avatar_nameInMessageOrange")) {
          break label2112;
        }
        i = 158;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGrayText4")) {
          break label2112;
        }
        i = 159;
        continue;
        if (!paramString.equals("files_folderIconBackground")) {
          break label2112;
        }
        i = 160;
        continue;
        if (!paramString.equals("profile_verifiedBackground")) {
          break label2112;
        }
        i = 161;
        continue;
        if (!paramString.equals("chat_outFileBackground")) {
          break label2112;
        }
        i = 162;
        continue;
        if (!paramString.equals("chat_inLoaderPhoto")) {
          break label2112;
        }
        i = 163;
        continue;
        if (!paramString.equals("dialogTextLink")) {
          break label2112;
        }
        i = 164;
        continue;
        if (!paramString.equals("chat_inForwardedNameText")) {
          break label2112;
        }
        i = 165;
        continue;
        if (!paramString.equals("chat_inSentClock")) {
          break label2112;
        }
        i = 166;
        continue;
        if (!paramString.equals("chat_inAudioSeekbarSelected")) {
          break label2112;
        }
        i = 167;
        continue;
        if (!paramString.equals("chats_name")) {
          break label2112;
        }
        i = 168;
        continue;
        if (!paramString.equals("chats_nameMessage")) {
          break label2112;
        }
        i = 169;
        continue;
        if (!paramString.equals("key_chats_menuTopShadow")) {
          break label2112;
        }
        i = 170;
        continue;
        if (!paramString.equals("windowBackgroundWhite")) {
          break label2112;
        }
        i = 171;
        continue;
        if (!paramString.equals("chat_outBubble")) {
          break label2112;
        }
        i = 172;
        continue;
        if (!paramString.equals("chats_menuBackground")) {
          break label2112;
        }
        i = 173;
        continue;
        if (!paramString.equals("chat_messagePanelHint")) {
          break label2112;
        }
        i = 174;
        continue;
        if (!paramString.equals("chat_replyPanelLine")) {
          break label2112;
        }
        i = 175;
        continue;
        if (!paramString.equals("chat_inReplyMediaMessageText")) {
          break label2112;
        }
        i = 176;
        continue;
        if (!paramString.equals("chat_outReplyMediaMessageText")) {
          break label2112;
        }
        i = 177;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarPink")) {
          break label2112;
        }
        i = 178;
        continue;
        if (!paramString.equals("chat_outLoader")) {
          break label2112;
        }
        i = 179;
        continue;
        if (!paramString.equals("chat_outReplyNameText")) {
          break label2112;
        }
        i = 180;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileViolet")) {
          break label2112;
        }
        i = 181;
        continue;
        if (!paramString.equals("chat_outAudioSelectedProgress")) {
          break label2112;
        }
        i = 182;
        continue;
        if (!paramString.equals("chat_inSentClockSelected")) {
          break label2112;
        }
        i = 183;
        continue;
        if (!paramString.equals("chat_inBubbleShadow")) {
          break label2112;
        }
        i = 184;
        continue;
        if (!paramString.equals("chat_inFileInfoText")) {
          break label2112;
        }
        i = 185;
        continue;
        if (!paramString.equals("windowBackgroundWhiteGrayIcon")) {
          break label2112;
        }
        i = 186;
        continue;
        if (!paramString.equals("chat_inAudioSeekbar")) {
          break label2112;
        }
        i = 187;
        continue;
        if (!paramString.equals("chat_inContactPhoneText")) {
          break label2112;
        }
        i = 188;
        continue;
        if (!paramString.equals("avatar_backgroundInProfileBlue")) {
          break label2112;
        }
        i = 189;
        continue;
        if (!paramString.equals("chat_outInstantSelected")) {
          break label2112;
        }
        i = 190;
        continue;
        if (!paramString.equals("chat_outLoaderPhotoIconSelected")) {
          break label2112;
        }
        i = 191;
        continue;
        if (!paramString.equals("chat_outAudioSeekbar")) {
          break label2112;
        }
        i = 192;
        continue;
        if (!paramString.equals("chat_inLoaderPhotoIcon")) {
          break label2112;
        }
        i = 193;
        continue;
        if (!paramString.equals("windowBackgroundWhiteRedText5")) {
          break label2112;
        }
        i = 194;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorViolet")) {
          break label2112;
        }
        i = 195;
        continue;
        if (!paramString.equals("chats_menuPhone")) {
          break label2112;
        }
        i = 196;
        continue;
        if (!paramString.equals("chat_outVoiceSeekbarFill")) {
          break label2112;
        }
        i = 197;
        continue;
        if (!paramString.equals("actionBarDefaultSubmenuItem")) {
          break label2112;
        }
        i = 198;
        continue;
        if (!paramString.equals("chat_outPreviewLine")) {
          break label2112;
        }
        i = 199;
        continue;
        if (!paramString.equals("chats_sentCheck")) {
          break label2112;
        }
        i = 200;
        continue;
        if (!paramString.equals("chat_inMenu")) {
          break label2112;
        }
        i = 201;
        continue;
        if (!paramString.equals("player_seekBarBackground")) {
          break label2112;
        }
        i = 202;
        continue;
        if (!paramString.equals("chats_sentClock")) {
          break label2112;
        }
        i = 203;
        continue;
        if (!paramString.equals("chat_messageLinkOut")) {
          break label2112;
        }
        i = 204;
        continue;
        if (!paramString.equals("chat_unreadMessagesStartText")) {
          break label2112;
        }
        i = 205;
        continue;
        if (!paramString.equals("inappPlayerClose")) {
          break label2112;
        }
        i = 206;
        continue;
        if (!paramString.equals("chat_inAudioProgress")) {
          break label2112;
        }
        i = 207;
        continue;
        if (!paramString.equals("chat_outFileBackgroundSelected")) {
          break label2112;
        }
        i = 208;
        continue;
        if (!paramString.equals("chat_outInstant")) {
          break label2112;
        }
        i = 209;
        continue;
        if (!paramString.equals("chat_outReplyMessageText")) {
          break label2112;
        }
        i = 210;
        continue;
        if (!paramString.equals("chat_outContactBackground")) {
          break label2112;
        }
        i = 211;
        continue;
        if (!paramString.equals("chat_inAudioDurationText")) {
          break label2112;
        }
        i = 212;
        continue;
        if (!paramString.equals("listSelectorSDK21")) {
          break label2112;
        }
        i = 213;
        continue;
        if (!paramString.equals("chat_goDownButtonIcon")) {
          break label2112;
        }
        i = 214;
        continue;
        if (!paramString.equals("chats_menuCloudBackgroundCats")) {
          break label2112;
        }
        i = 215;
        continue;
        if (!paramString.equals("chat_inLoaderPhotoIconSelected")) {
          break label2112;
        }
        i = 216;
        continue;
        if (!paramString.equals("windowBackgroundWhiteBlueText4")) {
          break label2112;
        }
        i = 217;
        continue;
        if (!paramString.equals("chat_inContactNameText")) {
          break label2112;
        }
        i = 218;
        continue;
        if (!paramString.equals("chat_topPanelTitle")) {
          break label2112;
        }
        i = 219;
        continue;
        if (!paramString.equals("chat_outLoaderPhotoSelected")) {
          break label2112;
        }
        i = 220;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorPink")) {
          break label2112;
        }
        i = 221;
        continue;
        if (!paramString.equals("chat_outContactNameText")) {
          break label2112;
        }
        i = 222;
        continue;
        if (!paramString.equals("player_actionBarSubtitle")) {
          break label2112;
        }
        i = 223;
        continue;
        if (!paramString.equals("chat_wallpaper")) {
          break label2112;
        }
        i = 224;
        continue;
        if (!paramString.equals("chat_emojiPanelStickerPackSelector")) {
          break label2112;
        }
        i = 225;
        continue;
        if (!paramString.equals("chats_menuPhoneCats")) {
          break label2112;
        }
        i = 226;
        continue;
        if (!paramString.equals("chat_reportSpam")) {
          break label2112;
        }
        i = 227;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileGreen")) {
          break label2112;
        }
        i = 228;
        continue;
        if (!paramString.equals("inappPlayerTitle")) {
          break label2112;
        }
        i = 229;
        continue;
        if (!paramString.equals("chat_outViaBotNameText")) {
          break label2112;
        }
        i = 230;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarRed")) {
          break label2112;
        }
        i = 231;
        continue;
        if (!paramString.equals("windowBackgroundWhiteValueText")) {
          break label2112;
        }
        i = 232;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarOrange")) {
          break label2112;
        }
        i = 233;
        continue;
        if (!paramString.equals("chat_inFileBackgroundSelected")) {
          break label2112;
        }
        i = 234;
        continue;
        if (!paramString.equals("avatar_actionBarSelectorOrange")) {
          break label2112;
        }
        i = 235;
        continue;
        if (!paramString.equals("chat_inVenueInfoSelectedText")) {
          break label2112;
        }
        i = 236;
        continue;
        if (!paramString.equals("actionBarActionModeDefaultIcon")) {
          break label2112;
        }
        i = 237;
        continue;
        if (!paramString.equals("chats_message")) {
          break label2112;
        }
        i = 238;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileBlue")) {
          break label2112;
        }
        i = 239;
        continue;
        if (!paramString.equals("chat_outVenueNameText")) {
          break label2112;
        }
        i = 240;
        continue;
        if (!paramString.equals("emptyListPlaceholder")) {
          break label2112;
        }
        i = 241;
        continue;
        if (!paramString.equals("chat_inFileProgress")) {
          break label2112;
        }
        i = 242;
        continue;
        if (!paramString.equals("chat_outLocationBackground")) {
          break label2112;
        }
        i = 243;
        continue;
        if (!paramString.equals("chats_muteIcon")) {
          break label2112;
        }
        i = 244;
        continue;
        if (!paramString.equals("groupcreate_spanText")) {
          break label2112;
        }
        i = 245;
        continue;
        if (!paramString.equals("windowBackgroundWhiteBlackText")) {
          break label2112;
        }
        i = 246;
        continue;
        if (!paramString.equals("windowBackgroundWhiteBlueText")) {
          break label2112;
        }
        i = 247;
        continue;
        if (!paramString.equals("chat_outReplyMediaMessageSelectedText")) {
          break label2112;
        }
        i = 248;
        continue;
        if (!paramString.equals("avatar_backgroundActionBarCyan")) {
          break label2112;
        }
        i = 249;
        continue;
        if (!paramString.equals("chat_topPanelClose")) {
          break label2112;
        }
        i = 250;
        continue;
        if (!paramString.equals("chat_outSentCheck")) {
          break label2112;
        }
        i = 251;
        continue;
        if (!paramString.equals("chat_outMenuSelected")) {
          break label2112;
        }
        i = 252;
        continue;
        if (!paramString.equals("chat_messagePanelText")) {
          break label2112;
        }
        i = 253;
        continue;
        if (!paramString.equals("chat_outReplyLine")) {
          break label2112;
        }
        i = 254;
        continue;
        if (!paramString.equals("dialogBackgroundGray")) {
          break label2112;
        }
        i = 255;
        continue;
        if (!paramString.equals("dialogButtonSelector")) {
          break label2112;
        }
        i = 256;
        continue;
        if (!paramString.equals("chat_outVenueInfoText")) {
          break label2112;
        }
        i = 257;
        continue;
        if (!paramString.equals("chat_outTimeText")) {
          break label2112;
        }
        i = 258;
        continue;
        if (!paramString.equals("chat_inTimeSelectedText")) {
          break label2112;
        }
        i = 259;
        continue;
        if (!paramString.equals("switchTrack")) {
          break label2112;
        }
        i = 260;
        continue;
        if (!paramString.equals("avatar_subtitleInProfileOrange")) {
          break label2112;
        }
        i = 261;
      }
    }
    return -1239540194;
    return -9342607;
    return -10653824;
    return -11167525;
    return 2047809827;
    return -8224126;
    return -645885536;
    return -13803892;
    return -15921907;
    return -12401818;
    return -9276814;
    return -1;
    return -14339006;
    return -1;
    return -1;
    return -9342607;
    return 1276090861;
    return -8882056;
    return -1579033;
    return -8487298;
    return -1;
    return -11164709;
    return -9594162;
    return -14339006;
    return 251658239;
    return -14605274;
    return -11164965;
    return -1313793;
    return -3019777;
    return -1;
    return -1979711488;
    return -11972268;
    return -7960954;
    return -10653824;
    return -12829636;
    return 268435455;
    return -986896;
    return -2954241;
    return 402653183;
    return -8211748;
    return -11972524;
    return -11234874;
    return -14143949;
    return -328966;
    return -13077852;
    return -13143396;
    return 1615417684;
    return -668259541;
    return -11108183;
    return -14935012;
    return -5582866;
    return -9263664;
    return -10851462;
    return -13077596;
    return -16777216;
    return -2102800402;
    return -14338750;
    return -328966;
    return -3019777;
    return -14925725;
    return -328966;
    return -1543503872;
    return -13091262;
    return -1;
    return -9652901;
    return -3874313;
    return -13948117;
    return -11164965;
    return -9868951;
    return -14540254;
    return -2236963;
    return -14605274;
    return -11167525;
    return -7105645;
    return -3019777;
    return -723724;
    return -11099429;
    return -5648402;
    return -10052929;
    return -8812393;
    return -11234874;
    return -11099173;
    return -14183202;
    return -10132123;
    return -9408400;
    return -81911774;
    return -1;
    return -1;
    return -13925429;
    return -9316522;
    return -8747891;
    return -7028510;
    return 167772159;
    return -5452289;
    return -9851917;
    return -11972268;
    return -10658467;
    return -8812137;
    return 771751936;
    return -14803426;
    return -9316522;
    return -11230501;
    return -7368817;
    return -13600600;
    return -1;
    return -11972268;
    return -14470078;
    return -11099173;
    return -5648402;
    return -8211748;
    return -14407896;
    return -11711155;
    return -14925469;
    return -11972524;
    return -4792321;
    return -10653824;
    return -3019777;
    return -12741934;
    return -3019777;
    return -14925725;
    return -10592674;
    return -9263664;
    return -13859893;
    return -13221820;
    return -12303292;
    return -11710381;
    return -1;
    return -11164965;
    return -5845010;
    return -9803158;
    return -9263664;
    return -98821092;
    return -1;
    return -15724528;
    return -394759;
    return -1;
    return -5855578;
    return -9590561;
    return -5648402;
    return -1;
    return -14605274;
    return -1;
    return -1;
    return -15316366;
    return 1713910333;
    return -8816263;
    return -15056797;
    return -1;
    return -9934744;
    return -14474461;
    return -11230501;
    return -14339006;
    return -14605274;
    return -11164965;
    return -11972268;
    return -2324391;
    return -9539986;
    return -13619152;
    return -11416584;
    return -9263664;
    return -14404542;
    return -13007663;
    return -11164965;
    return -10653824;
    return -5648402;
    return -1644826;
    return -11696202;
    return 789516;
    return -15263719;
    return -13077852;
    return -14868445;
    return -11776948;
    return -14869219;
    return -8812393;
    return -3019777;
    return -14605274;
    return -7421976;
    return -3019777;
    return -14187829;
    return -5648146;
    return -16777216;
    return -8812137;
    return -8224126;
    return -11443856;
    return -8812393;
    return -11232035;
    return -1;
    return -1;
    return -1770871344;
    return -10915968;
    return -45994;
    return -11972268;
    return 1627389951;
    return -3874313;
    return -657931;
    return -3019777;
    return -10574624;
    return 2036100992;
    return 1196577362;
    return -10452291;
    return -4792577;
    return -620756993;
    return -10987432;
    return -14338750;
    return -1;
    return -4792321;
    return -1;
    return -10910270;
    return -8746857;
    return 301989887;
    return -1776412;
    return -11232035;
    return -5648402;
    return -11890739;
    return -11099173;
    return -11164709;
    return -13208924;
    return -11972268;
    return -3019777;
    return -10526881;
    return -15526377;
    return 217775871;
    return -7434610;
    return -1481631;
    return -6513508;
    return -3019777;
    return -14605274;
    return -12214815;
    return -14605274;
    return -1;
    return -11972268;
    return -5648402;
    return -1;
    return -9934744;
    return -3019777;
    return -11447983;
    return -10653824;
    return -6234891;
    return -10790053;
    return -657931;
    return -855310;
    return -12413479;
    return -1;
    return -14605274;
    return -11184811;
    return -6831126;
    return -1;
    return -1118482;
    return -3019777;
    return -11840163;
    return 352321535;
    return -4792321;
    return -693579794;
    return -5582866;
    return -13948117;
  }
  
  public static Drawable getThemedDrawable(Context paramContext, int paramInt, String paramString)
  {
    paramContext = paramContext.getResources().getDrawable(paramInt).mutate();
    paramContext.setColorFilter(new PorterDuffColorFilter(getColor(paramString), PorterDuff.Mode.MULTIPLY));
    return paramContext;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/voip/DarkTheme.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */