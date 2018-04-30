package org.telegram.messenger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.widget.Toast;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.telegram.SQLite.SQLiteCursor;
import org.telegram.SQLite.SQLiteDatabase;
import org.telegram.messenger.support.SparseLongArray;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.DialogPeer;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DraftMessage;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.EncryptedMessage;
import org.telegram.tgnet.TLRPC.ExportedChatInvite;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.InputPeer;
import org.telegram.tgnet.TLRPC.InputPhoto;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.MessageFwdHeader;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.NotifyPeer;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.PeerNotifySettings;
import org.telegram.tgnet.TLRPC.PhoneCall;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.RecentMeUrl;
import org.telegram.tgnet.TLRPC.SendMessageAction;
import org.telegram.tgnet.TLRPC.TL_account_registerDevice;
import org.telegram.tgnet.TLRPC.TL_account_unregisterDevice;
import org.telegram.tgnet.TLRPC.TL_account_updateStatus;
import org.telegram.tgnet.TLRPC.TL_auth_logOut;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_botInfo;
import org.telegram.tgnet.TLRPC.TL_channel;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelForbidden;
import org.telegram.tgnet.TLRPC.TL_channelMessagesFilterEmpty;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_createChannel;
import org.telegram.tgnet.TLRPC.TL_channels_deleteChannel;
import org.telegram.tgnet.TLRPC.TL_channels_deleteHistory;
import org.telegram.tgnet.TLRPC.TL_channels_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_channels_deleteUserHistory;
import org.telegram.tgnet.TLRPC.TL_channels_editAbout;
import org.telegram.tgnet.TLRPC.TL_channels_editAdmin;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_editPhoto;
import org.telegram.tgnet.TLRPC.TL_channels_editTitle;
import org.telegram.tgnet.TLRPC.TL_channels_getFullChannel;
import org.telegram.tgnet.TLRPC.TL_channels_getMessages;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipant;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_inviteToChannel;
import org.telegram.tgnet.TLRPC.TL_channels_joinChannel;
import org.telegram.tgnet.TLRPC.TL_channels_leaveChannel;
import org.telegram.tgnet.TLRPC.TL_channels_readHistory;
import org.telegram.tgnet.TLRPC.TL_channels_readMessageContents;
import org.telegram.tgnet.TLRPC.TL_channels_toggleInvites;
import org.telegram.tgnet.TLRPC.TL_channels_togglePreHistoryHidden;
import org.telegram.tgnet.TLRPC.TL_channels_toggleSignatures;
import org.telegram.tgnet.TLRPC.TL_channels_updatePinnedMessage;
import org.telegram.tgnet.TLRPC.TL_channels_updateUsername;
import org.telegram.tgnet.TLRPC.TL_chat;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.TL_chatInviteEmpty;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipants;
import org.telegram.tgnet.TLRPC.TL_chatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_config;
import org.telegram.tgnet.TLRPC.TL_contactBlocked;
import org.telegram.tgnet.TLRPC.TL_contactLinkContact;
import org.telegram.tgnet.TLRPC.TL_contacts_block;
import org.telegram.tgnet.TLRPC.TL_contacts_getBlocked;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_contacts_unblock;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.TL_dialogPeer;
import org.telegram.tgnet.TLRPC.TL_documentEmpty;
import org.telegram.tgnet.TLRPC.TL_draftMessage;
import org.telegram.tgnet.TLRPC.TL_encryptedChat;
import org.telegram.tgnet.TLRPC.TL_encryptedChatRequested;
import org.telegram.tgnet.TLRPC.TL_encryptedChatWaiting;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getAppChangelog;
import org.telegram.tgnet.TLRPC.TL_help_getRecentMeUrls;
import org.telegram.tgnet.TLRPC.TL_help_recentMeUrls;
import org.telegram.tgnet.TLRPC.TL_inputChannel;
import org.telegram.tgnet.TLRPC.TL_inputChannelEmpty;
import org.telegram.tgnet.TLRPC.TL_inputChatPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_inputChatUploadedPhoto;
import org.telegram.tgnet.TLRPC.TL_inputDialogPeer;
import org.telegram.tgnet.TLRPC.TL_inputDocument;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedChat;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterChatPhotos;
import org.telegram.tgnet.TLRPC.TL_inputPeerChannel;
import org.telegram.tgnet.TLRPC.TL_inputPeerChat;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_inputPeerUser;
import org.telegram.tgnet.TLRPC.TL_inputPhoneCall;
import org.telegram.tgnet.TLRPC.TL_inputPhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_inputUser;
import org.telegram.tgnet.TLRPC.TL_inputUserEmpty;
import org.telegram.tgnet.TLRPC.TL_inputUserSelf;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageActionChannelCreate;
import org.telegram.tgnet.TLRPC.TL_messageActionChatAddUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatDeleteUser;
import org.telegram.tgnet.TLRPC.TL_messageActionChatMigrateTo;
import org.telegram.tgnet.TLRPC.TL_messageActionCreatedBroadcastList;
import org.telegram.tgnet.TLRPC.TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC.TL_messageActionUserJoined;
import org.telegram.tgnet.TLRPC.TL_messageEntityMentionName;
import org.telegram.tgnet.TLRPC.TL_messageMediaEmpty;
import org.telegram.tgnet.TLRPC.TL_messageMediaUnsupported;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_messages_addChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_affectedHistory;
import org.telegram.tgnet.TLRPC.TL_messages_affectedMessages;
import org.telegram.tgnet.TLRPC.TL_messages_channelMessages;
import org.telegram.tgnet.TLRPC.TL_messages_chatFull;
import org.telegram.tgnet.TLRPC.TL_messages_createChat;
import org.telegram.tgnet.TLRPC.TL_messages_deleteChatUser;
import org.telegram.tgnet.TLRPC.TL_messages_deleteHistory;
import org.telegram.tgnet.TLRPC.TL_messages_deleteMessages;
import org.telegram.tgnet.TLRPC.TL_messages_dialogs;
import org.telegram.tgnet.TLRPC.TL_messages_editChatAdmin;
import org.telegram.tgnet.TLRPC.TL_messages_editChatPhoto;
import org.telegram.tgnet.TLRPC.TL_messages_editChatTitle;
import org.telegram.tgnet.TLRPC.TL_messages_getDialogs;
import org.telegram.tgnet.TLRPC.TL_messages_getFullChat;
import org.telegram.tgnet.TLRPC.TL_messages_getHistory;
import org.telegram.tgnet.TLRPC.TL_messages_getMessages;
import org.telegram.tgnet.TLRPC.TL_messages_getMessagesViews;
import org.telegram.tgnet.TLRPC.TL_messages_getPeerDialogs;
import org.telegram.tgnet.TLRPC.TL_messages_getPeerSettings;
import org.telegram.tgnet.TLRPC.TL_messages_getPinnedDialogs;
import org.telegram.tgnet.TLRPC.TL_messages_getUnreadMentions;
import org.telegram.tgnet.TLRPC.TL_messages_getWebPagePreview;
import org.telegram.tgnet.TLRPC.TL_messages_hideReportSpam;
import org.telegram.tgnet.TLRPC.TL_messages_messages;
import org.telegram.tgnet.TLRPC.TL_messages_migrateChat;
import org.telegram.tgnet.TLRPC.TL_messages_peerDialogs;
import org.telegram.tgnet.TLRPC.TL_messages_readEncryptedHistory;
import org.telegram.tgnet.TLRPC.TL_messages_readHistory;
import org.telegram.tgnet.TLRPC.TL_messages_readMessageContents;
import org.telegram.tgnet.TLRPC.TL_messages_receivedQueue;
import org.telegram.tgnet.TLRPC.TL_messages_reportEncryptedSpam;
import org.telegram.tgnet.TLRPC.TL_messages_reportSpam;
import org.telegram.tgnet.TLRPC.TL_messages_saveGif;
import org.telegram.tgnet.TLRPC.TL_messages_saveRecentSticker;
import org.telegram.tgnet.TLRPC.TL_messages_search;
import org.telegram.tgnet.TLRPC.TL_messages_setEncryptedTyping;
import org.telegram.tgnet.TLRPC.TL_messages_setTyping;
import org.telegram.tgnet.TLRPC.TL_messages_startBot;
import org.telegram.tgnet.TLRPC.TL_messages_toggleChatAdmins;
import org.telegram.tgnet.TLRPC.TL_notifyPeer;
import org.telegram.tgnet.TLRPC.TL_peerChannel;
import org.telegram.tgnet.TLRPC.TL_peerChat;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettings;
import org.telegram.tgnet.TLRPC.TL_peerNotifySettingsEmpty;
import org.telegram.tgnet.TLRPC.TL_peerSettings;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallRequested;
import org.telegram.tgnet.TLRPC.TL_phone_discardCall;
import org.telegram.tgnet.TLRPC.TL_photoEmpty;
import org.telegram.tgnet.TLRPC.TL_photos_deletePhotos;
import org.telegram.tgnet.TLRPC.TL_photos_getUserPhotos;
import org.telegram.tgnet.TLRPC.TL_photos_photo;
import org.telegram.tgnet.TLRPC.TL_photos_photos;
import org.telegram.tgnet.TLRPC.TL_photos_updateProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_photos_uploadProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_privacyKeyChatInvite;
import org.telegram.tgnet.TLRPC.TL_privacyKeyPhoneCall;
import org.telegram.tgnet.TLRPC.TL_privacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_replyKeyboardHide;
import org.telegram.tgnet.TLRPC.TL_sendMessageCancelAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageGamePlayAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageRecordAudioAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageRecordRoundAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageRecordVideoAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageTypingAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadAudioAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadDocumentAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadPhotoAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadRoundAction;
import org.telegram.tgnet.TLRPC.TL_sendMessageUploadVideoAction;
import org.telegram.tgnet.TLRPC.TL_updateChannel;
import org.telegram.tgnet.TLRPC.TL_updateChannelAvailableMessages;
import org.telegram.tgnet.TLRPC.TL_updateChannelMessageViews;
import org.telegram.tgnet.TLRPC.TL_updateChannelPinnedMessage;
import org.telegram.tgnet.TLRPC.TL_updateChannelReadMessagesContents;
import org.telegram.tgnet.TLRPC.TL_updateChannelTooLong;
import org.telegram.tgnet.TLRPC.TL_updateChannelWebPage;
import org.telegram.tgnet.TLRPC.TL_updateChatAdmins;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantAdd;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipantDelete;
import org.telegram.tgnet.TLRPC.TL_updateChatParticipants;
import org.telegram.tgnet.TLRPC.TL_updateChatUserTyping;
import org.telegram.tgnet.TLRPC.TL_updateConfig;
import org.telegram.tgnet.TLRPC.TL_updateContactLink;
import org.telegram.tgnet.TLRPC.TL_updateContactRegistered;
import org.telegram.tgnet.TLRPC.TL_updateContactsReset;
import org.telegram.tgnet.TLRPC.TL_updateDcOptions;
import org.telegram.tgnet.TLRPC.TL_updateDeleteChannelMessages;
import org.telegram.tgnet.TLRPC.TL_updateDeleteMessages;
import org.telegram.tgnet.TLRPC.TL_updateDialogPinned;
import org.telegram.tgnet.TLRPC.TL_updateDraftMessage;
import org.telegram.tgnet.TLRPC.TL_updateEditChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateEditMessage;
import org.telegram.tgnet.TLRPC.TL_updateEncryptedChatTyping;
import org.telegram.tgnet.TLRPC.TL_updateEncryptedMessagesRead;
import org.telegram.tgnet.TLRPC.TL_updateEncryption;
import org.telegram.tgnet.TLRPC.TL_updateFavedStickers;
import org.telegram.tgnet.TLRPC.TL_updateGroupCall;
import org.telegram.tgnet.TLRPC.TL_updateGroupCallParticipant;
import org.telegram.tgnet.TLRPC.TL_updateLangPack;
import org.telegram.tgnet.TLRPC.TL_updateLangPackTooLong;
import org.telegram.tgnet.TLRPC.TL_updateMessageID;
import org.telegram.tgnet.TLRPC.TL_updateNewChannelMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewEncryptedMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewMessage;
import org.telegram.tgnet.TLRPC.TL_updateNewStickerSet;
import org.telegram.tgnet.TLRPC.TL_updateNotifySettings;
import org.telegram.tgnet.TLRPC.TL_updatePhoneCall;
import org.telegram.tgnet.TLRPC.TL_updatePinnedDialogs;
import org.telegram.tgnet.TLRPC.TL_updatePrivacy;
import org.telegram.tgnet.TLRPC.TL_updateReadChannelInbox;
import org.telegram.tgnet.TLRPC.TL_updateReadChannelOutbox;
import org.telegram.tgnet.TLRPC.TL_updateReadFeaturedStickers;
import org.telegram.tgnet.TLRPC.TL_updateReadHistoryInbox;
import org.telegram.tgnet.TLRPC.TL_updateReadHistoryOutbox;
import org.telegram.tgnet.TLRPC.TL_updateReadMessagesContents;
import org.telegram.tgnet.TLRPC.TL_updateRecentStickers;
import org.telegram.tgnet.TLRPC.TL_updateSavedGifs;
import org.telegram.tgnet.TLRPC.TL_updateServiceNotification;
import org.telegram.tgnet.TLRPC.TL_updateShort;
import org.telegram.tgnet.TLRPC.TL_updateShortChatMessage;
import org.telegram.tgnet.TLRPC.TL_updateShortMessage;
import org.telegram.tgnet.TLRPC.TL_updateStickerSets;
import org.telegram.tgnet.TLRPC.TL_updateStickerSetsOrder;
import org.telegram.tgnet.TLRPC.TL_updateUserBlocked;
import org.telegram.tgnet.TLRPC.TL_updateUserName;
import org.telegram.tgnet.TLRPC.TL_updateUserPhone;
import org.telegram.tgnet.TLRPC.TL_updateUserPhoto;
import org.telegram.tgnet.TLRPC.TL_updateUserStatus;
import org.telegram.tgnet.TLRPC.TL_updateUserTyping;
import org.telegram.tgnet.TLRPC.TL_updateWebPage;
import org.telegram.tgnet.TLRPC.TL_updates;
import org.telegram.tgnet.TLRPC.TL_updatesCombined;
import org.telegram.tgnet.TLRPC.TL_updatesTooLong;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifference;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceEmpty;
import org.telegram.tgnet.TLRPC.TL_updates_channelDifferenceTooLong;
import org.telegram.tgnet.TLRPC.TL_updates_difference;
import org.telegram.tgnet.TLRPC.TL_updates_differenceEmpty;
import org.telegram.tgnet.TLRPC.TL_updates_differenceSlice;
import org.telegram.tgnet.TLRPC.TL_updates_differenceTooLong;
import org.telegram.tgnet.TLRPC.TL_updates_getChannelDifference;
import org.telegram.tgnet.TLRPC.TL_updates_getDifference;
import org.telegram.tgnet.TLRPC.TL_updates_getState;
import org.telegram.tgnet.TLRPC.TL_updates_state;
import org.telegram.tgnet.TLRPC.TL_user;
import org.telegram.tgnet.TLRPC.TL_userForeign_old2;
import org.telegram.tgnet.TLRPC.TL_userFull;
import org.telegram.tgnet.TLRPC.TL_userProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userProfilePhotoEmpty;
import org.telegram.tgnet.TLRPC.TL_userStatusLastMonth;
import org.telegram.tgnet.TLRPC.TL_userStatusLastWeek;
import org.telegram.tgnet.TLRPC.TL_userStatusRecently;
import org.telegram.tgnet.TLRPC.TL_users_getFullUser;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.TL_webPageEmpty;
import org.telegram.tgnet.TLRPC.TL_webPagePending;
import org.telegram.tgnet.TLRPC.TL_webPageUrlPending;
import org.telegram.tgnet.TLRPC.Update;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.contacts_Blocked;
import org.telegram.tgnet.TLRPC.messages_Dialogs;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.tgnet.TLRPC.photos_Photos;
import org.telegram.tgnet.TLRPC.updates_ChannelDifference;
import org.telegram.tgnet.TLRPC.updates_Difference;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.ProfileActivity;

public class MessagesController
  implements NotificationCenter.NotificationCenterDelegate
{
  private static volatile MessagesController[] Instance = new MessagesController[3];
  public static final int UPDATE_MASK_ALL = 1535;
  public static final int UPDATE_MASK_AVATAR = 2;
  public static final int UPDATE_MASK_CHANNEL = 8192;
  public static final int UPDATE_MASK_CHAT_ADMINS = 16384;
  public static final int UPDATE_MASK_CHAT_AVATAR = 8;
  public static final int UPDATE_MASK_CHAT_MEMBERS = 32;
  public static final int UPDATE_MASK_CHAT_NAME = 16;
  public static final int UPDATE_MASK_MESSAGE_TEXT = 32768;
  public static final int UPDATE_MASK_NAME = 1;
  public static final int UPDATE_MASK_NEW_MESSAGE = 2048;
  public static final int UPDATE_MASK_PHONE = 1024;
  public static final int UPDATE_MASK_READ_DIALOG_MESSAGE = 256;
  public static final int UPDATE_MASK_SELECT_DIALOG = 512;
  public static final int UPDATE_MASK_SEND_STATE = 4096;
  public static final int UPDATE_MASK_STATUS = 4;
  public static final int UPDATE_MASK_USER_PHONE = 128;
  public static final int UPDATE_MASK_USER_PRINT = 64;
  private static volatile long lastThemeCheckTime;
  public ArrayList<Integer> blockedUsers = new ArrayList();
  public int callConnectTimeout = 30000;
  public int callPacketTimeout = 10000;
  public int callReceiveTimeout = 20000;
  public int callRingTimeout = 90000;
  public boolean canRevokePmInbox;
  private SparseArray<ArrayList<Integer>> channelAdmins = new SparseArray();
  private SparseArray<ArrayList<Integer>> channelViewsToSend = new SparseArray();
  private SparseIntArray channelsPts = new SparseIntArray();
  private ConcurrentHashMap<Integer, TLRPC.Chat> chats = new ConcurrentHashMap(100, 1.0F, 2);
  private SparseBooleanArray checkingLastMessagesDialogs = new SparseBooleanArray();
  private ArrayList<Long> createdDialogIds = new ArrayList();
  private ArrayList<Long> createdDialogMainThreadIds = new ArrayList();
  private int currentAccount;
  private Runnable currentDeleteTaskRunnable;
  private int currentDeletingTaskChannelId;
  private ArrayList<Integer> currentDeletingTaskMids;
  private int currentDeletingTaskTime;
  public boolean defaultP2pContacts = false;
  private final Comparator<TLRPC.TL_dialog> dialogComparator = new Comparator()
  {
    public int compare(TLRPC.TL_dialog paramAnonymousTL_dialog1, TLRPC.TL_dialog paramAnonymousTL_dialog2)
    {
      if ((!paramAnonymousTL_dialog1.pinned) && (paramAnonymousTL_dialog2.pinned)) {}
      label175:
      label182:
      for (;;)
      {
        return 1;
        if ((paramAnonymousTL_dialog1.pinned) && (!paramAnonymousTL_dialog2.pinned)) {
          return -1;
        }
        if ((paramAnonymousTL_dialog1.pinned) && (paramAnonymousTL_dialog2.pinned))
        {
          if (paramAnonymousTL_dialog1.pinnedNum >= paramAnonymousTL_dialog2.pinnedNum)
          {
            if (paramAnonymousTL_dialog1.pinnedNum > paramAnonymousTL_dialog2.pinnedNum) {
              return -1;
            }
            return 0;
          }
        }
        else
        {
          TLRPC.DraftMessage localDraftMessage = DataQuery.getInstance(MessagesController.this.currentAccount).getDraft(paramAnonymousTL_dialog1.id);
          int i;
          if ((localDraftMessage != null) && (localDraftMessage.date >= paramAnonymousTL_dialog1.last_message_date))
          {
            i = localDraftMessage.date;
            paramAnonymousTL_dialog1 = DataQuery.getInstance(MessagesController.this.currentAccount).getDraft(paramAnonymousTL_dialog2.id);
            if ((paramAnonymousTL_dialog1 == null) || (paramAnonymousTL_dialog1.date < paramAnonymousTL_dialog2.last_message_date)) {
              break label175;
            }
          }
          for (int j = paramAnonymousTL_dialog1.date;; j = paramAnonymousTL_dialog2.last_message_date)
          {
            if (i < j) {
              break label182;
            }
            if (i <= j) {
              break label184;
            }
            return -1;
            i = paramAnonymousTL_dialog1.last_message_date;
            break;
          }
        }
      }
      label184:
      return 0;
    }
  };
  public LongSparseArray<MessageObject> dialogMessage = new LongSparseArray();
  public SparseArray<MessageObject> dialogMessagesByIds = new SparseArray();
  public LongSparseArray<MessageObject> dialogMessagesByRandomIds = new LongSparseArray();
  public ArrayList<TLRPC.TL_dialog> dialogs = new ArrayList();
  public boolean dialogsEndReached;
  public ArrayList<TLRPC.TL_dialog> dialogsForward = new ArrayList();
  public ArrayList<TLRPC.TL_dialog> dialogsGroupsOnly = new ArrayList();
  public ArrayList<TLRPC.TL_dialog> dialogsServerOnly = new ArrayList();
  public LongSparseArray<TLRPC.TL_dialog> dialogs_dict = new LongSparseArray();
  public ConcurrentHashMap<Long, Integer> dialogs_read_inbox_max = new ConcurrentHashMap(100, 1.0F, 2);
  public ConcurrentHashMap<Long, Integer> dialogs_read_outbox_max = new ConcurrentHashMap(100, 1.0F, 2);
  private SharedPreferences emojiPreferences;
  public boolean enableJoined = true;
  private ConcurrentHashMap<Integer, TLRPC.EncryptedChat> encryptedChats = new ConcurrentHashMap(10, 1.0F, 2);
  private SparseArray<TLRPC.ExportedChatInvite> exportedChats = new SparseArray();
  public boolean firstGettingTask;
  private SparseArray<TLRPC.TL_userFull> fullUsers = new SparseArray();
  private boolean getDifferenceFirstSync = true;
  public boolean gettingDifference;
  private SparseBooleanArray gettingDifferenceChannels = new SparseBooleanArray();
  private boolean gettingNewDeleteTask;
  private SparseBooleanArray gettingUnknownChannels = new SparseBooleanArray();
  public ArrayList<TLRPC.RecentMeUrl> hintDialogs = new ArrayList();
  private String installReferer;
  private ArrayList<Integer> joiningToChannels = new ArrayList();
  private int lastPrintingStringCount;
  private long lastPushRegisterSendTime;
  private long lastStatusUpdateTime;
  private long lastViewsCheckTime;
  public String linkPrefix = "t.me";
  private ArrayList<Integer> loadedFullChats = new ArrayList();
  private ArrayList<Integer> loadedFullParticipants = new ArrayList();
  private ArrayList<Integer> loadedFullUsers = new ArrayList();
  public boolean loadingBlockedUsers = false;
  private SparseIntArray loadingChannelAdmins = new SparseIntArray();
  public boolean loadingDialogs;
  private ArrayList<Integer> loadingFullChats = new ArrayList();
  private ArrayList<Integer> loadingFullParticipants = new ArrayList();
  private ArrayList<Integer> loadingFullUsers = new ArrayList();
  private LongSparseArray<Boolean> loadingPeerSettings = new LongSparseArray();
  private SharedPreferences mainPreferences;
  public int maxBroadcastCount = 100;
  public int maxEditTime = 172800;
  public int maxFaveStickersCount = 5;
  public int maxGroupCount = 200;
  public int maxMegagroupCount = 10000;
  public int maxPinnedDialogsCount = 5;
  public int maxRecentGifsCount = 200;
  public int maxRecentStickersCount = 30;
  private boolean migratingDialogs;
  public int minGroupConvertSize = 200;
  private SparseIntArray needShortPollChannels = new SparseIntArray();
  public int nextDialogsCacheOffset;
  private SharedPreferences notificationsPreferences;
  private ConcurrentHashMap<String, TLObject> objectsByUsernames = new ConcurrentHashMap(100, 1.0F, 2);
  private boolean offlineSent;
  public ConcurrentHashMap<Integer, Integer> onlinePrivacy = new ConcurrentHashMap(20, 1.0F, 2);
  public boolean preloadFeaturedStickers;
  public LongSparseArray<CharSequence> printingStrings = new LongSparseArray();
  public LongSparseArray<Integer> printingStringsTypes = new LongSparseArray();
  public ConcurrentHashMap<Long, ArrayList<PrintingUser>> printingUsers = new ConcurrentHashMap(20, 1.0F, 2);
  public int ratingDecay;
  private ArrayList<ReadTask> readTasks = new ArrayList();
  private LongSparseArray<ReadTask> readTasksMap = new LongSparseArray();
  public boolean registeringForPush;
  private LongSparseArray<ArrayList<Integer>> reloadingMessages = new LongSparseArray();
  private HashMap<String, ArrayList<MessageObject>> reloadingWebpages = new HashMap();
  private LongSparseArray<ArrayList<MessageObject>> reloadingWebpagesPending = new LongSparseArray();
  private TLRPC.messages_Dialogs resetDialogsAll;
  private TLRPC.TL_messages_peerDialogs resetDialogsPinned;
  private boolean resetingDialogs;
  public int revokeTimeLimit = 172800;
  public int revokeTimePmLimit = 172800;
  public int secretWebpagePreview = 2;
  public SparseArray<LongSparseArray<Boolean>> sendingTypings = new SparseArray();
  public boolean serverDialogsEndReached;
  private SparseIntArray shortPollChannels = new SparseIntArray();
  private int statusRequest;
  private int statusSettingState;
  private Runnable themeCheckRunnable = new Runnable()
  {
    public void run() {}
  };
  private final Comparator<TLRPC.Update> updatesComparator = new Comparator()
  {
    public int compare(TLRPC.Update paramAnonymousUpdate1, TLRPC.Update paramAnonymousUpdate2)
    {
      int i = MessagesController.this.getUpdateType(paramAnonymousUpdate1);
      int j = MessagesController.this.getUpdateType(paramAnonymousUpdate2);
      if (i != j) {
        return AndroidUtilities.compare(i, j);
      }
      if (i == 0) {
        return AndroidUtilities.compare(MessagesController.getUpdatePts(paramAnonymousUpdate1), MessagesController.getUpdatePts(paramAnonymousUpdate2));
      }
      if (i == 1) {
        return AndroidUtilities.compare(MessagesController.getUpdateQts(paramAnonymousUpdate1), MessagesController.getUpdateQts(paramAnonymousUpdate2));
      }
      if (i == 2)
      {
        i = MessagesController.getUpdateChannelId(paramAnonymousUpdate1);
        j = MessagesController.getUpdateChannelId(paramAnonymousUpdate2);
        if (i == j) {
          return AndroidUtilities.compare(MessagesController.getUpdatePts(paramAnonymousUpdate1), MessagesController.getUpdatePts(paramAnonymousUpdate2));
        }
        return AndroidUtilities.compare(i, j);
      }
      return 0;
    }
  };
  private SparseArray<ArrayList<TLRPC.Updates>> updatesQueueChannels = new SparseArray();
  private ArrayList<TLRPC.Updates> updatesQueuePts = new ArrayList();
  private ArrayList<TLRPC.Updates> updatesQueueQts = new ArrayList();
  private ArrayList<TLRPC.Updates> updatesQueueSeq = new ArrayList();
  private SparseLongArray updatesStartWaitTimeChannels = new SparseLongArray();
  private long updatesStartWaitTimePts;
  private long updatesStartWaitTimeQts;
  private long updatesStartWaitTimeSeq;
  public boolean updatingState;
  private String uploadingAvatar;
  private ConcurrentHashMap<Integer, TLRPC.User> users = new ConcurrentHashMap(100, 1.0F, 2);
  
  public MessagesController(int paramInt)
  {
    this.currentAccount = paramInt;
    ImageLoader.getInstance();
    MessagesStorage.getInstance(this.currentAccount);
    LocationController.getInstance(this.currentAccount);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController localMessagesController = MessagesController.getInstance(MessagesController.this.currentAccount);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.messageReceivedByServer);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).addObserver(localMessagesController, NotificationCenter.updateMessageMedia);
      }
    });
    addSupportUser();
    if (this.currentAccount == 0)
    {
      this.notificationsPreferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", 0);
      this.mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
    }
    for (this.emojiPreferences = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0);; this.emojiPreferences = ApplicationLoader.applicationContext.getSharedPreferences("emoji" + this.currentAccount, 0))
    {
      this.enableJoined = this.notificationsPreferences.getBoolean("EnableContactJoined", true);
      this.secretWebpagePreview = this.mainPreferences.getInt("secretWebpage2", 2);
      this.maxGroupCount = this.mainPreferences.getInt("maxGroupCount", 200);
      this.maxMegagroupCount = this.mainPreferences.getInt("maxMegagroupCount", 10000);
      this.maxRecentGifsCount = this.mainPreferences.getInt("maxRecentGifsCount", 200);
      this.maxRecentStickersCount = this.mainPreferences.getInt("maxRecentStickersCount", 30);
      this.maxFaveStickersCount = this.mainPreferences.getInt("maxFaveStickersCount", 5);
      this.maxEditTime = this.mainPreferences.getInt("maxEditTime", 3600);
      this.ratingDecay = this.mainPreferences.getInt("ratingDecay", 2419200);
      this.linkPrefix = this.mainPreferences.getString("linkPrefix", "t.me");
      this.callReceiveTimeout = this.mainPreferences.getInt("callReceiveTimeout", 20000);
      this.callRingTimeout = this.mainPreferences.getInt("callRingTimeout", 90000);
      this.callConnectTimeout = this.mainPreferences.getInt("callConnectTimeout", 30000);
      this.callPacketTimeout = this.mainPreferences.getInt("callPacketTimeout", 10000);
      this.maxPinnedDialogsCount = this.mainPreferences.getInt("maxPinnedDialogsCount", 5);
      this.installReferer = this.mainPreferences.getString("installReferer", null);
      this.defaultP2pContacts = this.mainPreferences.getBoolean("defaultP2pContacts", false);
      this.revokeTimeLimit = this.mainPreferences.getInt("revokeTimeLimit", this.revokeTimeLimit);
      this.revokeTimePmLimit = this.mainPreferences.getInt("revokeTimePmLimit", this.revokeTimePmLimit);
      this.canRevokePmInbox = this.mainPreferences.getBoolean("canRevokePmInbox", this.canRevokePmInbox);
      this.preloadFeaturedStickers = this.mainPreferences.getBoolean("preloadFeaturedStickers", false);
      return;
      this.notificationsPreferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications" + this.currentAccount, 0);
      this.mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig" + this.currentAccount, 0);
    }
  }
  
  private void applyDialogNotificationsSettings(long paramLong, TLRPC.PeerNotifySettings paramPeerNotifySettings)
  {
    int m = this.notificationsPreferences.getInt("notify2_" + paramLong, 0);
    int n = this.notificationsPreferences.getInt("notifyuntil_" + paramLong, 0);
    SharedPreferences.Editor localEditor = this.notificationsPreferences.edit();
    int j = 0;
    int k = 0;
    int i = 0;
    TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)this.dialogs_dict.get(paramLong);
    if (localTL_dialog != null) {
      localTL_dialog.notify_settings = paramPeerNotifySettings;
    }
    localEditor.putBoolean("silent_" + paramLong, paramPeerNotifySettings.silent);
    if (paramPeerNotifySettings.mute_until > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime())
    {
      k = 0;
      if (paramPeerNotifySettings.mute_until > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 31536000)
      {
        j = k;
        if (m != 2)
        {
          m = 1;
          localEditor.putInt("notify2_" + paramLong, 2);
          j = k;
          i = m;
          if (localTL_dialog != null)
          {
            localTL_dialog.notify_settings.mute_until = Integer.MAX_VALUE;
            i = m;
            j = k;
          }
        }
        MessagesStorage.getInstance(this.currentAccount).setDialogFlags(paramLong, j << 32 | 1L);
        NotificationsController.getInstance(this.currentAccount).removeNotificationsForDialog(paramLong);
      }
    }
    for (;;)
    {
      localEditor.commit();
      if (i != 0) {
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
      }
      return;
      if (m == 3)
      {
        i = j;
        if (n == paramPeerNotifySettings.mute_until) {}
      }
      else
      {
        j = 1;
        localEditor.putInt("notify2_" + paramLong, 3);
        localEditor.putInt("notifyuntil_" + paramLong, paramPeerNotifySettings.mute_until);
        i = j;
        if (localTL_dialog != null)
        {
          localTL_dialog.notify_settings.mute_until = 0;
          i = j;
        }
      }
      j = paramPeerNotifySettings.mute_until;
      break;
      i = k;
      if (m != 0)
      {
        i = k;
        if (m != 1)
        {
          i = 1;
          if (localTL_dialog != null) {
            localTL_dialog.notify_settings.mute_until = 0;
          }
          localEditor.remove("notify2_" + paramLong);
        }
      }
      MessagesStorage.getInstance(this.currentAccount).setDialogFlags(paramLong, 0L);
    }
  }
  
  private void applyDialogsNotificationsSettings(ArrayList<TLRPC.TL_dialog> paramArrayList)
  {
    Object localObject1 = null;
    int j = 0;
    if (j < paramArrayList.size())
    {
      TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)paramArrayList.get(j);
      Object localObject2 = localObject1;
      int i;
      if (localTL_dialog.peer != null)
      {
        localObject2 = localObject1;
        if ((localTL_dialog.notify_settings instanceof TLRPC.TL_peerNotifySettings))
        {
          localObject2 = localObject1;
          if (localObject1 == null) {
            localObject2 = this.notificationsPreferences.edit();
          }
          if (localTL_dialog.peer.user_id == 0) {
            break label213;
          }
          i = localTL_dialog.peer.user_id;
          label90:
          ((SharedPreferences.Editor)localObject2).putBoolean("silent_" + i, localTL_dialog.notify_settings.silent);
          if (localTL_dialog.notify_settings.mute_until == 0) {
            break label318;
          }
          if (localTL_dialog.notify_settings.mute_until <= ConnectionsManager.getInstance(this.currentAccount).getCurrentTime() + 31536000) {
            break label250;
          }
          ((SharedPreferences.Editor)localObject2).putInt("notify2_" + i, 2);
          localTL_dialog.notify_settings.mute_until = Integer.MAX_VALUE;
        }
      }
      for (;;)
      {
        j += 1;
        localObject1 = localObject2;
        break;
        label213:
        if (localTL_dialog.peer.chat_id != 0)
        {
          i = -localTL_dialog.peer.chat_id;
          break label90;
        }
        i = -localTL_dialog.peer.channel_id;
        break label90;
        label250:
        ((SharedPreferences.Editor)localObject2).putInt("notify2_" + i, 3);
        ((SharedPreferences.Editor)localObject2).putInt("notifyuntil_" + i, localTL_dialog.notify_settings.mute_until);
        continue;
        label318:
        ((SharedPreferences.Editor)localObject2).remove("notify2_" + i);
      }
    }
    if (localObject1 != null) {
      ((SharedPreferences.Editor)localObject1).commit();
    }
  }
  
  private void checkChannelError(String paramString, int paramInt)
  {
    int i = -1;
    switch (paramString.hashCode())
    {
    }
    for (;;)
    {
      switch (i)
      {
      default: 
        return;
        if (paramString.equals("CHANNEL_PRIVATE"))
        {
          i = 0;
          continue;
          if (paramString.equals("CHANNEL_PUBLIC_GROUP_NA"))
          {
            i = 1;
            continue;
            if (paramString.equals("USER_BANNED_IN_CHANNEL")) {
              i = 2;
            }
          }
        }
        break;
      }
    }
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatInfoCantLoad, new Object[] { Integer.valueOf(paramInt), Integer.valueOf(0) });
    return;
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatInfoCantLoad, new Object[] { Integer.valueOf(paramInt), Integer.valueOf(1) });
    return;
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatInfoCantLoad, new Object[] { Integer.valueOf(paramInt), Integer.valueOf(2) });
  }
  
  private boolean checkDeletingTask(boolean paramBoolean)
  {
    boolean bool2 = false;
    int i = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    boolean bool1 = bool2;
    if (this.currentDeletingTaskMids != null) {
      if (!paramBoolean)
      {
        bool1 = bool2;
        if (this.currentDeletingTaskTime != 0)
        {
          bool1 = bool2;
          if (this.currentDeletingTaskTime > i) {}
        }
      }
      else
      {
        this.currentDeletingTaskTime = 0;
        if ((this.currentDeleteTaskRunnable != null) && (!paramBoolean)) {
          Utilities.stageQueue.cancelRunnable(this.currentDeleteTaskRunnable);
        }
        this.currentDeleteTaskRunnable = null;
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if ((!this.val$mids.isEmpty()) && (((Integer)this.val$mids.get(0)).intValue() > 0)) {
              MessagesStorage.getInstance(MessagesController.this.currentAccount).emptyMessagesMedia(this.val$mids);
            }
            for (;;)
            {
              Utilities.stageQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.getNewDeleteTask(MessagesController.26.this.val$mids, MessagesController.this.currentDeletingTaskChannelId);
                  MessagesController.access$4102(MessagesController.this, 0);
                  MessagesController.access$3902(MessagesController.this, null);
                }
              });
              return;
              MessagesController.this.deleteMessages(this.val$mids, null, null, 0, false);
            }
          }
        });
        bool1 = true;
      }
    }
    return bool1;
  }
  
  private void checkReadTasks()
  {
    long l = SystemClock.uptimeMillis();
    int i = 0;
    int j = this.readTasks.size();
    if (i < j)
    {
      ReadTask localReadTask = (ReadTask)this.readTasks.get(i);
      if (localReadTask.sendRequestTime > l) {}
      for (;;)
      {
        i += 1;
        break;
        completeReadTask(localReadTask);
        this.readTasks.remove(i);
        this.readTasksMap.remove(localReadTask.dialogId);
        i -= 1;
        j -= 1;
      }
    }
  }
  
  private void completeReadTask(ReadTask paramReadTask)
  {
    int i = (int)paramReadTask.dialogId;
    int j = (int)(paramReadTask.dialogId >> 32);
    Object localObject1;
    if (i != 0)
    {
      localObject2 = getInputPeer(i);
      if ((localObject2 instanceof TLRPC.TL_inputPeerChannel))
      {
        localObject1 = new TLRPC.TL_channels_readHistory();
        ((TLRPC.TL_channels_readHistory)localObject1).channel = getInputChannel(-i);
        ((TLRPC.TL_channels_readHistory)localObject1).max_id = paramReadTask.maxId;
        paramReadTask = (ReadTask)localObject1;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramReadTask, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if ((paramAnonymousTL_error == null) && ((paramAnonymousTLObject instanceof TLRPC.TL_messages_affectedMessages)))
            {
              paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
              MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
            }
          }
        });
      }
    }
    do
    {
      return;
      localObject1 = new TLRPC.TL_messages_readHistory();
      ((TLRPC.TL_messages_readHistory)localObject1).peer = ((TLRPC.InputPeer)localObject2);
      ((TLRPC.TL_messages_readHistory)localObject1).max_id = paramReadTask.maxId;
      paramReadTask = (ReadTask)localObject1;
      break;
      localObject1 = getEncryptedChat(Integer.valueOf(j));
    } while ((((TLRPC.EncryptedChat)localObject1).auth_key == null) || (((TLRPC.EncryptedChat)localObject1).auth_key.length <= 1) || (!(localObject1 instanceof TLRPC.TL_encryptedChat)));
    Object localObject2 = new TLRPC.TL_messages_readEncryptedHistory();
    ((TLRPC.TL_messages_readEncryptedHistory)localObject2).peer = new TLRPC.TL_inputEncryptedChat();
    ((TLRPC.TL_messages_readEncryptedHistory)localObject2).peer.chat_id = ((TLRPC.EncryptedChat)localObject1).id;
    ((TLRPC.TL_messages_readEncryptedHistory)localObject2).peer.access_hash = ((TLRPC.EncryptedChat)localObject1).access_hash;
    ((TLRPC.TL_messages_readEncryptedHistory)localObject2).max_date = paramReadTask.maxDate;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  private void deleteDialog(final long paramLong, boolean paramBoolean, int paramInt1, final int paramInt2)
  {
    int j = (int)paramLong;
    int k = (int)(paramLong >> 32);
    int i = paramInt2;
    if (paramInt1 == 2) {
      MessagesStorage.getInstance(this.currentAccount).deleteDialog(paramLong, paramInt1);
    }
    Object localObject1;
    label281:
    label367:
    label451:
    label538:
    do
    {
      do
      {
        do
        {
          return;
          if ((paramInt1 == 0) || (paramInt1 == 3)) {
            DataQuery.getInstance(this.currentAccount).uninstallShortcut(paramLong);
          }
          paramInt2 = i;
          if (paramBoolean)
          {
            MessagesStorage.getInstance(this.currentAccount).deleteDialog(paramLong, paramInt1);
            localObject2 = (TLRPC.TL_dialog)this.dialogs_dict.get(paramLong);
            paramInt2 = i;
            if (localObject2 != null)
            {
              paramInt2 = i;
              if (i == 0) {
                paramInt2 = Math.max(0, ((TLRPC.TL_dialog)localObject2).top_message);
              }
              if ((paramInt1 != 0) && (paramInt1 != 3)) {
                break;
              }
              this.dialogs.remove(localObject2);
              if ((this.dialogsServerOnly.remove(localObject2)) && (DialogObject.isChannel((TLRPC.TL_dialog)localObject2))) {
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.this.channelsPts.delete(-(int)paramLong);
                    MessagesController.this.shortPollChannels.delete(-(int)paramLong);
                    MessagesController.this.needShortPollChannels.delete(-(int)paramLong);
                  }
                });
              }
              this.dialogsGroupsOnly.remove(localObject2);
              this.dialogs_dict.remove(paramLong);
              this.dialogs_read_inbox_max.remove(Long.valueOf(paramLong));
              this.dialogs_read_outbox_max.remove(Long.valueOf(paramLong));
              this.nextDialogsCacheOffset -= 1;
              localObject1 = (MessageObject)this.dialogMessage.get(((TLRPC.TL_dialog)localObject2).id);
              this.dialogMessage.remove(((TLRPC.TL_dialog)localObject2).id);
              if (localObject1 == null) {
                break label743;
              }
              i = ((MessageObject)localObject1).getId();
              this.dialogMessagesByIds.remove(((MessageObject)localObject1).getId());
              if ((localObject1 != null) && (((MessageObject)localObject1).messageOwner.random_id != 0L)) {
                this.dialogMessagesByRandomIds.remove(((MessageObject)localObject1).messageOwner.random_id);
              }
              if ((paramInt1 != 1) || (j == 0) || (i <= 0)) {
                break label855;
              }
              localObject1 = new TLRPC.TL_messageService();
              ((TLRPC.TL_messageService)localObject1).id = ((TLRPC.TL_dialog)localObject2).top_message;
              if (UserConfig.getInstance(this.currentAccount).getClientUserId() != paramLong) {
                break label782;
              }
              paramBoolean = true;
              ((TLRPC.TL_messageService)localObject1).out = paramBoolean;
              ((TLRPC.TL_messageService)localObject1).from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
              ((TLRPC.TL_messageService)localObject1).flags |= 0x100;
              ((TLRPC.TL_messageService)localObject1).action = new TLRPC.TL_messageActionHistoryClear();
              ((TLRPC.TL_messageService)localObject1).date = ((TLRPC.TL_dialog)localObject2).last_message_date;
              if (j <= 0) {
                break label787;
              }
              ((TLRPC.TL_messageService)localObject1).to_id = new TLRPC.TL_peerUser();
              ((TLRPC.TL_messageService)localObject1).to_id.user_id = j;
              Object localObject3 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject1, this.createdDialogIds.contains(Long.valueOf(((TLRPC.TL_messageService)localObject1).dialog_id)));
              localObject2 = new ArrayList();
              ((ArrayList)localObject2).add(localObject3);
              localObject3 = new ArrayList();
              ((ArrayList)localObject3).add(localObject1);
              updateInterfaceWithMessages(paramLong, (ArrayList)localObject2);
              MessagesStorage.getInstance(this.currentAccount).putMessages((ArrayList)localObject3, false, true, false, 0);
            }
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.removeAllMessagesFromDialog, new Object[] { Long.valueOf(paramLong), Boolean.valueOf(false) });
            MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
            {
              public void run()
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationsController.getInstance(MessagesController.this.currentAccount).removeNotificationsForDialog(MessagesController.44.this.val$did);
                  }
                });
              }
            });
          }
        } while ((k == 1) || (paramInt1 == 3));
        if (j == 0) {
          break label959;
        }
        localObject1 = getInputPeer(j);
      } while (localObject1 == null);
      if (!(localObject1 instanceof TLRPC.TL_inputPeerChannel)) {
        break label872;
      }
    } while (paramInt1 == 0);
    Object localObject2 = new TLRPC.TL_channels_deleteHistory();
    ((TLRPC.TL_channels_deleteHistory)localObject2).channel = new TLRPC.TL_inputChannel();
    ((TLRPC.TL_channels_deleteHistory)localObject2).channel.channel_id = ((TLRPC.InputPeer)localObject1).channel_id;
    ((TLRPC.TL_channels_deleteHistory)localObject2).channel.access_hash = ((TLRPC.InputPeer)localObject1).access_hash;
    if (paramInt2 > 0) {}
    for (;;)
    {
      ((TLRPC.TL_channels_deleteHistory)localObject2).max_id = paramInt2;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      }, 64);
      return;
      ((TLRPC.TL_dialog)localObject2).unread_count = 0;
      break;
      label743:
      i = ((TLRPC.TL_dialog)localObject2).top_message;
      localObject1 = (MessageObject)this.dialogMessagesByIds.get(((TLRPC.TL_dialog)localObject2).top_message);
      this.dialogMessagesByIds.remove(((TLRPC.TL_dialog)localObject2).top_message);
      break label281;
      label782:
      paramBoolean = false;
      break label367;
      label787:
      if (ChatObject.isChannel(getChat(Integer.valueOf(-j))))
      {
        ((TLRPC.TL_messageService)localObject1).to_id = new TLRPC.TL_peerChannel();
        ((TLRPC.TL_messageService)localObject1).to_id.channel_id = (-j);
        break label451;
      }
      ((TLRPC.TL_messageService)localObject1).to_id = new TLRPC.TL_peerChat();
      ((TLRPC.TL_messageService)localObject1).to_id.chat_id = (-j);
      break label451;
      label855:
      ((TLRPC.TL_dialog)localObject2).top_message = 0;
      break label538;
      paramInt2 = Integer.MAX_VALUE;
    }
    label872:
    localObject2 = new TLRPC.TL_messages_deleteHistory();
    ((TLRPC.TL_messages_deleteHistory)localObject2).peer = ((TLRPC.InputPeer)localObject1);
    if (paramInt1 == 0)
    {
      i = Integer.MAX_VALUE;
      ((TLRPC.TL_messages_deleteHistory)localObject2).max_id = i;
      if (paramInt1 == 0) {
        break label954;
      }
    }
    label954:
    for (paramBoolean = true;; paramBoolean = false)
    {
      ((TLRPC.TL_messages_deleteHistory)localObject2).just_clear = paramBoolean;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.TL_messages_affectedHistory)paramAnonymousTLObject;
            if (paramAnonymousTLObject.offset > 0) {
              MessagesController.this.deleteDialog(paramLong, false, paramInt2, this.val$max_id_delete_final);
            }
            MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
          }
        }
      }, 64);
      return;
      i = paramInt2;
      break;
    }
    label959:
    if (paramInt1 == 1)
    {
      SecretChatHelper.getInstance(this.currentAccount).sendClearHistoryMessage(getEncryptedChat(Integer.valueOf(k)), null);
      return;
    }
    SecretChatHelper.getInstance(this.currentAccount).declineSecretChat(k);
  }
  
  private void getChannelDifference(int paramInt)
  {
    getChannelDifference(paramInt, 0, 0L, null);
  }
  
  public static SharedPreferences getEmojiSettings(int paramInt)
  {
    return getInstance(paramInt).emojiPreferences;
  }
  
  public static SharedPreferences getGlobalEmojiSettings()
  {
    return getInstance(0).emojiPreferences;
  }
  
  public static SharedPreferences getGlobalMainSettings()
  {
    return getInstance(0).mainPreferences;
  }
  
  public static SharedPreferences getGlobalNotificationsSettings()
  {
    return getInstance(0).notificationsPreferences;
  }
  
  public static TLRPC.InputChannel getInputChannel(TLRPC.Chat paramChat)
  {
    if (((paramChat instanceof TLRPC.TL_channel)) || ((paramChat instanceof TLRPC.TL_channelForbidden)))
    {
      TLRPC.TL_inputChannel localTL_inputChannel = new TLRPC.TL_inputChannel();
      localTL_inputChannel.channel_id = paramChat.id;
      localTL_inputChannel.access_hash = paramChat.access_hash;
      return localTL_inputChannel;
    }
    return new TLRPC.TL_inputChannelEmpty();
  }
  
  public static MessagesController getInstance(int paramInt)
  {
    Object localObject1 = Instance[paramInt];
    if (localObject1 == null) {}
    try
    {
      Object localObject3 = Instance[paramInt];
      localObject1 = localObject3;
      if (localObject3 == null)
      {
        localObject3 = Instance;
        localObject1 = new MessagesController(paramInt);
        localObject3[paramInt] = localObject1;
      }
      return (MessagesController)localObject1;
    }
    finally
    {
      for (;;) {}
    }
    throw ((Throwable)localObject1);
    return (MessagesController)localObject1;
  }
  
  public static SharedPreferences getMainSettings(int paramInt)
  {
    return getInstance(paramInt).mainPreferences;
  }
  
  public static SharedPreferences getNotificationsSettings(int paramInt)
  {
    return getInstance(paramInt).notificationsPreferences;
  }
  
  private static String getRestrictionReason(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0)) {}
    int i;
    String str;
    do
    {
      do
      {
        return null;
        i = paramString.indexOf(": ");
      } while (i <= 0);
      str = paramString.substring(0, i);
    } while ((!str.contains("-all")) && (!str.contains("-android")));
    return paramString.substring(i + 2);
  }
  
  private static int getUpdateChannelId(TLRPC.Update paramUpdate)
  {
    if ((paramUpdate instanceof TLRPC.TL_updateNewChannelMessage)) {
      return ((TLRPC.TL_updateNewChannelMessage)paramUpdate).message.to_id.channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateEditChannelMessage)) {
      return ((TLRPC.TL_updateEditChannelMessage)paramUpdate).message.to_id.channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadChannelOutbox)) {
      return ((TLRPC.TL_updateReadChannelOutbox)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelMessageViews)) {
      return ((TLRPC.TL_updateChannelMessageViews)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelTooLong)) {
      return ((TLRPC.TL_updateChannelTooLong)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelPinnedMessage)) {
      return ((TLRPC.TL_updateChannelPinnedMessage)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelReadMessagesContents)) {
      return ((TLRPC.TL_updateChannelReadMessagesContents)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelAvailableMessages)) {
      return ((TLRPC.TL_updateChannelAvailableMessages)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannel)) {
      return ((TLRPC.TL_updateChannel)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelWebPage)) {
      return ((TLRPC.TL_updateChannelWebPage)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateDeleteChannelMessages)) {
      return ((TLRPC.TL_updateDeleteChannelMessages)paramUpdate).channel_id;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadChannelInbox)) {
      return ((TLRPC.TL_updateReadChannelInbox)paramUpdate).channel_id;
    }
    if (BuildVars.LOGS_ENABLED) {
      FileLog.e("trying to get unknown update channel_id for " + paramUpdate);
    }
    return 0;
  }
  
  private static int getUpdatePts(TLRPC.Update paramUpdate)
  {
    if ((paramUpdate instanceof TLRPC.TL_updateDeleteMessages)) {
      return ((TLRPC.TL_updateDeleteMessages)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateNewChannelMessage)) {
      return ((TLRPC.TL_updateNewChannelMessage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadHistoryOutbox)) {
      return ((TLRPC.TL_updateReadHistoryOutbox)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateNewMessage)) {
      return ((TLRPC.TL_updateNewMessage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateEditMessage)) {
      return ((TLRPC.TL_updateEditMessage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateWebPage)) {
      return ((TLRPC.TL_updateWebPage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadHistoryInbox)) {
      return ((TLRPC.TL_updateReadHistoryInbox)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelWebPage)) {
      return ((TLRPC.TL_updateChannelWebPage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateDeleteChannelMessages)) {
      return ((TLRPC.TL_updateDeleteChannelMessages)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateEditChannelMessage)) {
      return ((TLRPC.TL_updateEditChannelMessage)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadMessagesContents)) {
      return ((TLRPC.TL_updateReadMessagesContents)paramUpdate).pts;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelTooLong)) {
      return ((TLRPC.TL_updateChannelTooLong)paramUpdate).pts;
    }
    return 0;
  }
  
  private static int getUpdatePtsCount(TLRPC.Update paramUpdate)
  {
    if ((paramUpdate instanceof TLRPC.TL_updateDeleteMessages)) {
      return ((TLRPC.TL_updateDeleteMessages)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateNewChannelMessage)) {
      return ((TLRPC.TL_updateNewChannelMessage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadHistoryOutbox)) {
      return ((TLRPC.TL_updateReadHistoryOutbox)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateNewMessage)) {
      return ((TLRPC.TL_updateNewMessage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateEditMessage)) {
      return ((TLRPC.TL_updateEditMessage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateWebPage)) {
      return ((TLRPC.TL_updateWebPage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadHistoryInbox)) {
      return ((TLRPC.TL_updateReadHistoryInbox)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateChannelWebPage)) {
      return ((TLRPC.TL_updateChannelWebPage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateDeleteChannelMessages)) {
      return ((TLRPC.TL_updateDeleteChannelMessages)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateEditChannelMessage)) {
      return ((TLRPC.TL_updateEditChannelMessage)paramUpdate).pts_count;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateReadMessagesContents)) {
      return ((TLRPC.TL_updateReadMessagesContents)paramUpdate).pts_count;
    }
    return 0;
  }
  
  private static int getUpdateQts(TLRPC.Update paramUpdate)
  {
    if ((paramUpdate instanceof TLRPC.TL_updateNewEncryptedMessage)) {
      return ((TLRPC.TL_updateNewEncryptedMessage)paramUpdate).qts;
    }
    return 0;
  }
  
  private int getUpdateSeq(TLRPC.Updates paramUpdates)
  {
    if ((paramUpdates instanceof TLRPC.TL_updatesCombined)) {
      return paramUpdates.seq_start;
    }
    return paramUpdates.seq;
  }
  
  private int getUpdateType(TLRPC.Update paramUpdate)
  {
    if (((paramUpdate instanceof TLRPC.TL_updateNewMessage)) || ((paramUpdate instanceof TLRPC.TL_updateReadMessagesContents)) || ((paramUpdate instanceof TLRPC.TL_updateReadHistoryInbox)) || ((paramUpdate instanceof TLRPC.TL_updateReadHistoryOutbox)) || ((paramUpdate instanceof TLRPC.TL_updateDeleteMessages)) || ((paramUpdate instanceof TLRPC.TL_updateWebPage)) || ((paramUpdate instanceof TLRPC.TL_updateEditMessage))) {
      return 0;
    }
    if ((paramUpdate instanceof TLRPC.TL_updateNewEncryptedMessage)) {
      return 1;
    }
    if (((paramUpdate instanceof TLRPC.TL_updateNewChannelMessage)) || ((paramUpdate instanceof TLRPC.TL_updateDeleteChannelMessages)) || ((paramUpdate instanceof TLRPC.TL_updateEditChannelMessage)) || ((paramUpdate instanceof TLRPC.TL_updateChannelWebPage))) {
      return 2;
    }
    return 3;
  }
  
  private String getUserNameForTyping(TLRPC.User paramUser)
  {
    if (paramUser == null) {
      return "";
    }
    if ((paramUser.first_name != null) && (paramUser.first_name.length() > 0)) {
      return paramUser.first_name;
    }
    if ((paramUser.last_name != null) && (paramUser.last_name.length() > 0)) {
      return paramUser.last_name;
    }
    return "";
  }
  
  private boolean isNotifySettingsMuted(TLRPC.PeerNotifySettings paramPeerNotifySettings)
  {
    return ((paramPeerNotifySettings instanceof TLRPC.TL_peerNotifySettings)) && (paramPeerNotifySettings.mute_until > ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
  }
  
  public static boolean isSupportId(int paramInt)
  {
    return (paramInt / 1000 == 777) || (paramInt == 333000) || (paramInt == 4240000) || (paramInt == 4240000) || (paramInt == 4244000) || (paramInt == 4245000) || (paramInt == 4246000) || (paramInt == 410000) || (paramInt == 420000) || (paramInt == 431000) || (paramInt == 431415000) || (paramInt == 434000) || (paramInt == 4243000) || (paramInt == 439000) || (paramInt == 449000) || (paramInt == 450000) || (paramInt == 452000) || (paramInt == 454000) || (paramInt == 4254000) || (paramInt == 455000) || (paramInt == 460000) || (paramInt == 470000) || (paramInt == 479000) || (paramInt == 796000) || (paramInt == 482000) || (paramInt == 490000) || (paramInt == 496000) || (paramInt == 497000) || (paramInt == 498000) || (paramInt == 4298000);
  }
  
  private int isValidUpdate(TLRPC.Updates paramUpdates, int paramInt)
  {
    int i = 1;
    int j;
    if (paramInt == 0)
    {
      j = getUpdateSeq(paramUpdates);
      if ((MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + 1 == j) || (MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() == j)) {
        paramInt = 0;
      }
    }
    do
    {
      do
      {
        do
        {
          return paramInt;
          paramInt = i;
        } while (MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() < j);
        return 2;
        if (paramInt != 1) {
          break;
        }
        if (paramUpdates.pts <= MessagesStorage.getInstance(this.currentAccount).getLastPtsValue()) {
          return 2;
        }
        paramInt = i;
      } while (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + paramUpdates.pts_count != paramUpdates.pts);
      return 0;
      if (paramInt != 2) {
        break;
      }
      if (paramUpdates.pts <= MessagesStorage.getInstance(this.currentAccount).getLastQtsValue()) {
        return 2;
      }
      paramInt = i;
    } while (MessagesStorage.getInstance(this.currentAccount).getLastQtsValue() + paramUpdates.updates.size() != paramUpdates.pts);
    return 0;
    return 0;
  }
  
  private void loadMessagesInternal(final long paramLong, final int paramInt1, final int paramInt2, final int paramInt3, boolean paramBoolean1, final int paramInt4, final int paramInt5, final int paramInt6, final int paramInt7, final boolean paramBoolean2, final int paramInt8, final int paramInt9, final int paramInt10, final int paramInt11, final boolean paramBoolean3, final int paramInt12, boolean paramBoolean4)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("load messages in chat " + paramLong + " count " + paramInt1 + " max_id " + paramInt2 + " cache " + paramBoolean1 + " mindate = " + paramInt4 + " guid " + paramInt5 + " load_type " + paramInt6 + " last_message_id " + paramInt7 + " index " + paramInt8 + " firstUnread " + paramInt9 + " unread_count " + paramInt10 + " last_date " + paramInt11 + " queryFromServer " + paramBoolean3);
    }
    int i = (int)paramLong;
    if ((paramBoolean1) || (i == 0))
    {
      MessagesStorage.getInstance(this.currentAccount).getMessages(paramLong, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramBoolean2, paramInt8);
      return;
    }
    if ((paramBoolean4) && ((paramInt6 == 3) || (paramInt6 == 2)) && (paramInt7 == 0))
    {
      localObject = new TLRPC.TL_messages_getPeerDialogs();
      TLRPC.InputPeer localInputPeer = getInputPeer((int)paramLong);
      TLRPC.TL_inputDialogPeer localTL_inputDialogPeer = new TLRPC.TL_inputDialogPeer();
      localTL_inputDialogPeer.peer = localInputPeer;
      ((TLRPC.TL_messages_getPeerDialogs)localObject).peers.add(localTL_inputDialogPeer);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null)
          {
            paramAnonymousTLObject = (TLRPC.TL_messages_peerDialogs)paramAnonymousTLObject;
            if (!paramAnonymousTLObject.dialogs.isEmpty())
            {
              paramAnonymousTL_error = (TLRPC.TL_dialog)paramAnonymousTLObject.dialogs.get(0);
              if (paramAnonymousTL_error.top_message != 0)
              {
                TLRPC.TL_messages_dialogs localTL_messages_dialogs = new TLRPC.TL_messages_dialogs();
                localTL_messages_dialogs.chats = paramAnonymousTLObject.chats;
                localTL_messages_dialogs.users = paramAnonymousTLObject.users;
                localTL_messages_dialogs.dialogs = paramAnonymousTLObject.dialogs;
                localTL_messages_dialogs.messages = paramAnonymousTLObject.messages;
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putDialogs(localTL_messages_dialogs, false);
              }
              MessagesController.this.loadMessagesInternal(paramLong, paramInt2, paramInt3, paramInt4, false, paramInt5, paramInt6, paramBoolean2, paramAnonymousTL_error.top_message, paramInt8, paramInt9, paramInt11, paramAnonymousTL_error.unread_count, paramBoolean3, this.val$queryFromServer, paramAnonymousTL_error.unread_mentions_count, false);
            }
          }
        }
      });
      return;
    }
    Object localObject = new TLRPC.TL_messages_getHistory();
    ((TLRPC.TL_messages_getHistory)localObject).peer = getInputPeer(i);
    if (paramInt6 == 4) {
      ((TLRPC.TL_messages_getHistory)localObject).add_offset = (-paramInt1 + 5);
    }
    for (;;)
    {
      ((TLRPC.TL_messages_getHistory)localObject).limit = paramInt1;
      ((TLRPC.TL_messages_getHistory)localObject).offset_id = paramInt2;
      ((TLRPC.TL_messages_getHistory)localObject).offset_date = paramInt3;
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          int j;
          int i;
          int k;
          if (paramAnonymousTLObject != null)
          {
            paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
            if (paramAnonymousTLObject.messages.size() > paramInt1) {
              paramAnonymousTLObject.messages.remove(0);
            }
            j = paramInt2;
            i = j;
            if (paramInt3 != 0)
            {
              i = j;
              if (!paramAnonymousTLObject.messages.isEmpty())
              {
                k = ((TLRPC.Message)paramAnonymousTLObject.messages.get(paramAnonymousTLObject.messages.size() - 1)).id;
                j = paramAnonymousTLObject.messages.size() - 1;
              }
            }
          }
          for (;;)
          {
            i = k;
            if (j >= 0)
            {
              paramAnonymousTL_error = (TLRPC.Message)paramAnonymousTLObject.messages.get(j);
              if (paramAnonymousTL_error.date > paramInt3) {
                i = paramAnonymousTL_error.id;
              }
            }
            else
            {
              MessagesController.this.processLoadedMessages(paramAnonymousTLObject, paramLong, paramInt1, i, paramInt3, false, paramInt9, paramInt7, paramInt10, paramInt11, paramInt6, paramBoolean2, paramInt8, false, paramBoolean3, paramInt12, this.val$mentionsCount);
              return;
            }
            j -= 1;
          }
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt5);
      return;
      if (paramInt6 == 3)
      {
        ((TLRPC.TL_messages_getHistory)localObject).add_offset = (-paramInt1 / 2);
      }
      else if (paramInt6 == 1)
      {
        ((TLRPC.TL_messages_getHistory)localObject).add_offset = (-paramInt1 - 1);
      }
      else if ((paramInt6 == 2) && (paramInt2 != 0))
      {
        ((TLRPC.TL_messages_getHistory)localObject).add_offset = (-paramInt1 + 6);
      }
      else if ((i < 0) && (paramInt2 != 0) && (ChatObject.isChannel(getChat(Integer.valueOf(-i)))))
      {
        ((TLRPC.TL_messages_getHistory)localObject).add_offset = -1;
        ((TLRPC.TL_messages_getHistory)localObject).limit += 1;
      }
    }
  }
  
  private void migrateDialogs(final int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, long paramLong)
  {
    if ((this.migratingDialogs) || (paramInt1 == -1)) {
      return;
    }
    this.migratingDialogs = true;
    TLRPC.TL_messages_getDialogs localTL_messages_getDialogs = new TLRPC.TL_messages_getDialogs();
    localTL_messages_getDialogs.exclude_pinned = true;
    localTL_messages_getDialogs.limit = 100;
    localTL_messages_getDialogs.offset_id = paramInt1;
    localTL_messages_getDialogs.offset_date = paramInt2;
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("start migrate with id " + paramInt1 + " date " + LocaleController.getInstance().formatterStats.format(paramInt2 * 1000L));
    }
    if (paramInt1 == 0)
    {
      localTL_messages_getDialogs.offset_peer = new TLRPC.TL_inputPeerEmpty();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getDialogs, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.messages_Dialogs)paramAnonymousTLObject;
            MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
            {
              public void run()
              {
                int i;
                Object localObject2;
                Object localObject4;
                for (;;)
                {
                  try
                  {
                    UserConfig localUserConfig = UserConfig.getInstance(MessagesController.this.currentAccount);
                    localUserConfig.totalDialogsLoadCount += paramAnonymousTLObject.dialogs.size();
                    localUserConfig = null;
                    i = 0;
                    if (i < paramAnonymousTLObject.messages.size())
                    {
                      localObject3 = (TLRPC.Message)paramAnonymousTLObject.messages.get(i);
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("search migrate id " + ((TLRPC.Message)localObject3).id + " date " + LocaleController.getInstance().formatterStats.format(((TLRPC.Message)localObject3).date * 1000L));
                      }
                      if (localUserConfig == null) {
                        break label2052;
                      }
                      localObject2 = localUserConfig;
                      if (((TLRPC.Message)localObject3).date >= localUserConfig.date) {
                        break label2056;
                      }
                      break label2052;
                    }
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d("migrate step with id " + localUserConfig.id + " date " + LocaleController.getInstance().formatterStats.format(localUserConfig.date * 1000L));
                    }
                    if (paramAnonymousTLObject.dialogs.size() >= 100)
                    {
                      i = localUserConfig.id;
                      localObject3 = new StringBuilder(paramAnonymousTLObject.dialogs.size() * 12);
                      localObject2 = new LongSparseArray();
                      j = 0;
                      if (j >= paramAnonymousTLObject.dialogs.size()) {
                        break;
                      }
                      localObject4 = (TLRPC.TL_dialog)paramAnonymousTLObject.dialogs.get(j);
                      if (((TLRPC.TL_dialog)localObject4).peer.channel_id != 0)
                      {
                        ((TLRPC.TL_dialog)localObject4).id = (-((TLRPC.TL_dialog)localObject4).peer.channel_id);
                        if (((StringBuilder)localObject3).length() > 0) {
                          ((StringBuilder)localObject3).append(",");
                        }
                        ((StringBuilder)localObject3).append(((TLRPC.TL_dialog)localObject4).id);
                        ((LongSparseArray)localObject2).put(((TLRPC.TL_dialog)localObject4).id, localObject4);
                        j += 1;
                        continue;
                      }
                    }
                    else
                    {
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("migrate stop due to not 100 dialogs");
                      }
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId = Integer.MAX_VALUE;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetDate = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetDate;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess;
                      i = -1;
                      continue;
                    }
                    if (((TLRPC.TL_dialog)localObject4).peer.chat_id != 0) {
                      ((TLRPC.TL_dialog)localObject4).id = (-((TLRPC.TL_dialog)localObject4).peer.chat_id);
                    } else {
                      ((TLRPC.TL_dialog)localObject4).id = ((TLRPC.TL_dialog)localObject4).peer.user_id;
                    }
                  }
                  catch (Exception localException)
                  {
                    FileLog.e(localException);
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        MessagesController.access$6002(MessagesController.this, false);
                      }
                    });
                    return;
                  }
                }
                Object localObject3 = MessagesStorage.getInstance(MessagesController.this.currentAccount).getDatabase().queryFinalized(String.format(Locale.US, "SELECT did FROM dialogs WHERE did IN (%s)", new Object[] { ((StringBuilder)localObject3).toString() }), new Object[0]);
                label674:
                long l;
                while (((SQLiteCursor)localObject3).next())
                {
                  l = ((SQLiteCursor)localObject3).longValue(0);
                  localObject4 = (TLRPC.TL_dialog)((LongSparseArray)localObject2).get(l);
                  ((LongSparseArray)localObject2).remove(l);
                  if (localObject4 != null)
                  {
                    paramAnonymousTLObject.dialogs.remove(localObject4);
                    j = 0;
                    label729:
                    if (j >= paramAnonymousTLObject.messages.size()) {
                      break label2072;
                    }
                    TLRPC.Message localMessage = (TLRPC.Message)paramAnonymousTLObject.messages.get(j);
                    if (MessageObject.getDialogId(localMessage) != l) {
                      break label2067;
                    }
                    paramAnonymousTLObject.messages.remove(j);
                    j -= 1;
                    if (localMessage.id != ((TLRPC.TL_dialog)localObject4).top_message) {
                      break label2067;
                    }
                    ((TLRPC.TL_dialog)localObject4).top_message = 0;
                  }
                }
                ((SQLiteCursor)localObject3).dispose();
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d("migrate found missing dialogs " + paramAnonymousTLObject.dialogs.size());
                }
                localObject3 = MessagesStorage.getInstance(MessagesController.this.currentAccount).getDatabase().queryFinalized("SELECT min(date) FROM dialogs WHERE date != 0 AND did >> 32 IN (0, -1)", new Object[0]);
                int j = i;
                label908:
                int k;
                int m;
                if (((SQLiteCursor)localObject3).next())
                {
                  int n = Math.max(1441062000, ((SQLiteCursor)localObject3).intValue(0));
                  j = 0;
                  if (j < paramAnonymousTLObject.messages.size())
                  {
                    localObject4 = (TLRPC.Message)paramAnonymousTLObject.messages.get(j);
                    k = i;
                    m = j;
                    if (((TLRPC.Message)localObject4).date >= n) {
                      break label2074;
                    }
                    if (MessagesController.68.this.val$offset != -1)
                    {
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetDate = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetDate;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId;
                      UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess;
                      k = -1;
                      i = k;
                      if (BuildVars.LOGS_ENABLED)
                      {
                        FileLog.d("migrate stop due to reached loaded dialogs " + LocaleController.getInstance().formatterStats.format(n * 1000L));
                        i = k;
                      }
                    }
                    paramAnonymousTLObject.messages.remove(j);
                    j -= 1;
                    l = MessageObject.getDialogId((TLRPC.Message)localObject4);
                    localObject4 = (TLRPC.TL_dialog)((LongSparseArray)localObject2).get(l);
                    ((LongSparseArray)localObject2).remove(l);
                    k = i;
                    m = j;
                    if (localObject4 == null) {
                      break label2074;
                    }
                    paramAnonymousTLObject.dialogs.remove(localObject4);
                    k = i;
                    m = j;
                    break label2074;
                  }
                  j = i;
                  if (localException != null)
                  {
                    j = i;
                    if (localException.date < n)
                    {
                      j = i;
                      if (MessagesController.68.this.val$offset != -1)
                      {
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetId;
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetDate = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetDate;
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId;
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId;
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId;
                        UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess;
                        i = -1;
                        j = i;
                        if (BuildVars.LOGS_ENABLED)
                        {
                          FileLog.d("migrate stop due to reached loaded dialogs " + LocaleController.getInstance().formatterStats.format(n * 1000L));
                          j = i;
                        }
                      }
                    }
                  }
                }
                ((SQLiteCursor)localObject3).dispose();
                UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetDate = localException.date;
                label1650:
                Object localObject1;
                if (localException.to_id.channel_id != 0)
                {
                  UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId = localException.to_id.channel_id;
                  UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId = 0;
                  UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId = 0;
                  i = 0;
                  if (i < paramAnonymousTLObject.chats.size())
                  {
                    localObject1 = (TLRPC.Chat)paramAnonymousTLObject.chats.get(i);
                    if (((TLRPC.Chat)localObject1).id != UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId) {
                      break label2084;
                    }
                    UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
                  }
                }
                label1725:
                label2050:
                label2052:
                label2056:
                label2067:
                label2072:
                label2074:
                label2084:
                label2096:
                for (;;)
                {
                  MessagesController.this.processLoadedDialogs(paramAnonymousTLObject, null, j, 0, 0, false, true, false);
                  return;
                  if (((TLRPC.Message)localObject1).to_id.chat_id != 0)
                  {
                    UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId = ((TLRPC.Message)localObject1).to_id.chat_id;
                    UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId = 0;
                    UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId = 0;
                    i = 0;
                  }
                  for (;;)
                  {
                    if (i >= paramAnonymousTLObject.chats.size()) {
                      break label2096;
                    }
                    localObject1 = (TLRPC.Chat)paramAnonymousTLObject.chats.get(i);
                    if (((TLRPC.Chat)localObject1).id == UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId)
                    {
                      UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
                      break label1725;
                      if (((TLRPC.Message)localObject1).to_id.user_id == 0) {
                        break label1725;
                      }
                      UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId = ((TLRPC.Message)localObject1).to_id.user_id;
                      UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId = 0;
                      UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId = 0;
                      i = 0;
                      for (;;)
                      {
                        if (i >= paramAnonymousTLObject.users.size()) {
                          break label2050;
                        }
                        localObject1 = (TLRPC.User)paramAnonymousTLObject.users.get(i);
                        if (((TLRPC.User)localObject1).id == UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId)
                        {
                          UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess = ((TLRPC.User)localObject1).access_hash;
                          break;
                        }
                        i += 1;
                      }
                      break label1725;
                      localObject2 = localObject3;
                      i += 1;
                      localObject1 = localObject2;
                      break;
                      j += 1;
                      break label729;
                      break label674;
                      j = m + 1;
                      i = k;
                      break label908;
                      i += 1;
                      break label1650;
                    }
                    i += 1;
                  }
                }
              }
            });
            return;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.access$6002(MessagesController.this, false);
            }
          });
        }
      });
      return;
    }
    if (paramInt5 != 0)
    {
      localTL_messages_getDialogs.offset_peer = new TLRPC.TL_inputPeerChannel();
      localTL_messages_getDialogs.offset_peer.channel_id = paramInt5;
    }
    for (;;)
    {
      localTL_messages_getDialogs.offset_peer.access_hash = paramLong;
      break;
      if (paramInt3 != 0)
      {
        localTL_messages_getDialogs.offset_peer = new TLRPC.TL_inputPeerUser();
        localTL_messages_getDialogs.offset_peer.user_id = paramInt3;
      }
      else
      {
        localTL_messages_getDialogs.offset_peer = new TLRPC.TL_inputPeerChat();
        localTL_messages_getDialogs.offset_peer.chat_id = paramInt4;
      }
    }
  }
  
  public static void openChatOrProfileWith(TLRPC.User paramUser, TLRPC.Chat paramChat, BaseFragment paramBaseFragment, int paramInt, boolean paramBoolean)
  {
    if (((paramUser == null) && (paramChat == null)) || (paramBaseFragment == null)) {
      return;
    }
    Object localObject = null;
    boolean bool;
    int i;
    if (paramChat != null)
    {
      localObject = getRestrictionReason(paramChat.restriction_reason);
      bool = paramBoolean;
      i = paramInt;
    }
    while (localObject != null)
    {
      showCantOpenAlert(paramBaseFragment, (String)localObject);
      return;
      i = paramInt;
      bool = paramBoolean;
      if (paramUser != null)
      {
        String str = getRestrictionReason(paramUser.restriction_reason);
        localObject = str;
        i = paramInt;
        bool = paramBoolean;
        if (paramUser.bot)
        {
          i = 1;
          bool = true;
          localObject = str;
        }
      }
    }
    localObject = new Bundle();
    if (paramChat != null) {
      ((Bundle)localObject).putInt("chat_id", paramChat.id);
    }
    while (i == 0)
    {
      paramBaseFragment.presentFragment(new ProfileActivity((Bundle)localObject));
      return;
      ((Bundle)localObject).putInt("user_id", paramUser.id);
    }
    if (i == 2)
    {
      paramBaseFragment.presentFragment(new ChatActivity((Bundle)localObject), true, true);
      return;
    }
    paramBaseFragment.presentFragment(new ChatActivity((Bundle)localObject), bool);
  }
  
  private void processChannelsUpdatesQueue(int paramInt1, int paramInt2)
  {
    ArrayList localArrayList = (ArrayList)this.updatesQueueChannels.get(paramInt1);
    if (localArrayList == null) {}
    label94:
    label126:
    label186:
    do
    {
      return;
      int k = this.channelsPts.get(paramInt1);
      if ((localArrayList.isEmpty()) || (k == 0))
      {
        this.updatesQueueChannels.remove(paramInt1);
        return;
      }
      Collections.sort(localArrayList, new Comparator()
      {
        public int compare(TLRPC.Updates paramAnonymousUpdates1, TLRPC.Updates paramAnonymousUpdates2)
        {
          return AndroidUtilities.compare(paramAnonymousUpdates1.pts, paramAnonymousUpdates2.pts);
        }
      });
      int i = 0;
      if (paramInt2 == 2) {
        this.channelsPts.put(paramInt1, ((TLRPC.Updates)localArrayList.get(0)).pts);
      }
      int j = 0;
      if (localArrayList.size() > 0)
      {
        TLRPC.Updates localUpdates = (TLRPC.Updates)localArrayList.get(j);
        if (localUpdates.pts <= k)
        {
          paramInt2 = 2;
          if (paramInt2 != 0) {
            break label186;
          }
          processUpdates(localUpdates, true);
          i = 1;
          localArrayList.remove(j);
        }
        for (paramInt2 = j - 1;; paramInt2 = j - 1)
        {
          j = paramInt2 + 1;
          break label94;
          if (localUpdates.pts_count + k == localUpdates.pts)
          {
            paramInt2 = 0;
            break label126;
          }
          paramInt2 = 1;
          break label126;
          if (paramInt2 == 1)
          {
            long l = this.updatesStartWaitTimeChannels.get(paramInt1);
            if ((l != 0L) && ((i != 0) || (Math.abs(System.currentTimeMillis() - l) <= 1500L)))
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("HOLE IN CHANNEL " + paramInt1 + " UPDATES QUEUE - will wait more time");
              }
              if (i == 0) {
                break;
              }
              this.updatesStartWaitTimeChannels.put(paramInt1, System.currentTimeMillis());
              return;
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("HOLE IN CHANNEL " + paramInt1 + " UPDATES QUEUE - getChannelDifference ");
            }
            this.updatesStartWaitTimeChannels.delete(paramInt1);
            this.updatesQueueChannels.remove(paramInt1);
            getChannelDifference(paramInt1);
            return;
          }
          localArrayList.remove(j);
        }
      }
      this.updatesQueueChannels.remove(paramInt1);
      this.updatesStartWaitTimeChannels.delete(paramInt1);
    } while (!BuildVars.LOGS_ENABLED);
    FileLog.d("UPDATES CHANNEL " + paramInt1 + " QUEUE PROCEED - OK");
  }
  
  private void processUpdatesQueue(int paramInt1, int paramInt2)
  {
    ArrayList localArrayList = null;
    int i;
    TLRPC.Updates localUpdates;
    label77:
    int j;
    if (paramInt1 == 0)
    {
      localArrayList = this.updatesQueueSeq;
      Collections.sort(localArrayList, new Comparator()
      {
        public int compare(TLRPC.Updates paramAnonymousUpdates1, TLRPC.Updates paramAnonymousUpdates2)
        {
          return AndroidUtilities.compare(MessagesController.this.getUpdateSeq(paramAnonymousUpdates1), MessagesController.this.getUpdateSeq(paramAnonymousUpdates2));
        }
      });
      if ((localArrayList == null) || (localArrayList.isEmpty())) {
        break label363;
      }
      i = 0;
      if (paramInt2 == 2)
      {
        localUpdates = (TLRPC.Updates)localArrayList.get(0);
        if (paramInt1 != 0) {
          break label199;
        }
        MessagesStorage.getInstance(this.currentAccount).setLastSeqValue(getUpdateSeq(localUpdates));
      }
      j = 0;
      paramInt2 = i;
      i = j;
      label85:
      if (localArrayList.size() <= 0) {
        break label346;
      }
      localUpdates = (TLRPC.Updates)localArrayList.get(i);
      j = isValidUpdate(localUpdates, paramInt1);
      if (j != 0) {
        break label240;
      }
      processUpdates(localUpdates, true);
      paramInt2 = 1;
      localArrayList.remove(i);
      i -= 1;
    }
    for (;;)
    {
      i += 1;
      break label85;
      if (paramInt1 == 1)
      {
        localArrayList = this.updatesQueuePts;
        Collections.sort(localArrayList, new Comparator()
        {
          public int compare(TLRPC.Updates paramAnonymousUpdates1, TLRPC.Updates paramAnonymousUpdates2)
          {
            return AndroidUtilities.compare(paramAnonymousUpdates1.pts, paramAnonymousUpdates2.pts);
          }
        });
        break;
      }
      if (paramInt1 != 2) {
        break;
      }
      localArrayList = this.updatesQueueQts;
      Collections.sort(localArrayList, new Comparator()
      {
        public int compare(TLRPC.Updates paramAnonymousUpdates1, TLRPC.Updates paramAnonymousUpdates2)
        {
          return AndroidUtilities.compare(paramAnonymousUpdates1.pts, paramAnonymousUpdates2.pts);
        }
      });
      break;
      label199:
      if (paramInt1 == 1)
      {
        MessagesStorage.getInstance(this.currentAccount).setLastPtsValue(localUpdates.pts);
        break label77;
      }
      MessagesStorage.getInstance(this.currentAccount).setLastQtsValue(localUpdates.pts);
      break label77;
      label240:
      if (j == 1)
      {
        if ((getUpdatesStartTime(paramInt1) != 0L) && ((paramInt2 != 0) || (Math.abs(System.currentTimeMillis() - getUpdatesStartTime(paramInt1)) <= 1500L)))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("HOLE IN UPDATES QUEUE - will wait more time");
          }
          if (paramInt2 != 0) {
            setUpdatesStartTime(paramInt1, System.currentTimeMillis());
          }
          return;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("HOLE IN UPDATES QUEUE - getDifference");
        }
        setUpdatesStartTime(paramInt1, 0L);
        localArrayList.clear();
        getDifference();
        return;
      }
      localArrayList.remove(i);
      i -= 1;
    }
    label346:
    localArrayList.clear();
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("UPDATES QUEUE PROCEED - OK");
    }
    label363:
    setUpdatesStartTime(paramInt1, 0L);
  }
  
  private void reloadDialogsReadValue(ArrayList<TLRPC.TL_dialog> paramArrayList, long paramLong)
  {
    if ((paramLong == 0L) && ((paramArrayList == null) || (paramArrayList.isEmpty()))) {}
    TLRPC.TL_messages_getPeerDialogs localTL_messages_getPeerDialogs;
    do
    {
      do
      {
        return;
        localTL_messages_getPeerDialogs = new TLRPC.TL_messages_getPeerDialogs();
        if (paramArrayList != null)
        {
          int i = 0;
          if (i >= paramArrayList.size()) {
            break;
          }
          localObject = getInputPeer((int)((TLRPC.TL_dialog)paramArrayList.get(i)).id);
          if (((localObject instanceof TLRPC.TL_inputPeerChannel)) && (((TLRPC.InputPeer)localObject).access_hash == 0L)) {}
          for (;;)
          {
            i += 1;
            break;
            TLRPC.TL_inputDialogPeer localTL_inputDialogPeer = new TLRPC.TL_inputDialogPeer();
            localTL_inputDialogPeer.peer = ((TLRPC.InputPeer)localObject);
            localTL_messages_getPeerDialogs.peers.add(localTL_inputDialogPeer);
          }
        }
        paramArrayList = getInputPeer((int)paramLong);
      } while (((paramArrayList instanceof TLRPC.TL_inputPeerChannel)) && (paramArrayList.access_hash == 0L));
      Object localObject = new TLRPC.TL_inputDialogPeer();
      ((TLRPC.TL_inputDialogPeer)localObject).peer = paramArrayList;
      localTL_messages_getPeerDialogs.peers.add(localObject);
    } while (localTL_messages_getPeerDialogs.peers.isEmpty());
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getPeerDialogs, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null)
        {
          TLRPC.TL_messages_peerDialogs localTL_messages_peerDialogs = (TLRPC.TL_messages_peerDialogs)paramAnonymousTLObject;
          ArrayList localArrayList = new ArrayList();
          int i = 0;
          if (i < localTL_messages_peerDialogs.dialogs.size())
          {
            TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)localTL_messages_peerDialogs.dialogs.get(i);
            if (localTL_dialog.read_inbox_max_id == 0) {
              localTL_dialog.read_inbox_max_id = 1;
            }
            if (localTL_dialog.read_outbox_max_id == 0) {
              localTL_dialog.read_outbox_max_id = 1;
            }
            if ((localTL_dialog.id == 0L) && (localTL_dialog.peer != null))
            {
              if (localTL_dialog.peer.user_id != 0) {
                localTL_dialog.id = localTL_dialog.peer.user_id;
              }
            }
            else
            {
              label118:
              paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(localTL_dialog.id));
              paramAnonymousTLObject = paramAnonymousTL_error;
              if (paramAnonymousTL_error == null) {
                paramAnonymousTLObject = Integer.valueOf(0);
              }
              MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(localTL_dialog.id), Integer.valueOf(Math.max(localTL_dialog.read_inbox_max_id, paramAnonymousTLObject.intValue())));
              if (paramAnonymousTLObject.intValue() == 0)
              {
                if (localTL_dialog.peer.channel_id == 0) {
                  break label425;
                }
                paramAnonymousTLObject = new TLRPC.TL_updateReadChannelInbox();
                paramAnonymousTLObject.channel_id = localTL_dialog.peer.channel_id;
                paramAnonymousTLObject.max_id = localTL_dialog.read_inbox_max_id;
                localArrayList.add(paramAnonymousTLObject);
              }
              label239:
              paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(localTL_dialog.id));
              paramAnonymousTLObject = paramAnonymousTL_error;
              if (paramAnonymousTL_error == null) {
                paramAnonymousTLObject = Integer.valueOf(0);
              }
              MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(localTL_dialog.id), Integer.valueOf(Math.max(localTL_dialog.read_outbox_max_id, paramAnonymousTLObject.intValue())));
              if (paramAnonymousTLObject.intValue() == 0)
              {
                if (localTL_dialog.peer.channel_id == 0) {
                  break label461;
                }
                paramAnonymousTLObject = new TLRPC.TL_updateReadChannelOutbox();
                paramAnonymousTLObject.channel_id = localTL_dialog.peer.channel_id;
                paramAnonymousTLObject.max_id = localTL_dialog.read_outbox_max_id;
                localArrayList.add(paramAnonymousTLObject);
              }
            }
            for (;;)
            {
              i += 1;
              break;
              if (localTL_dialog.peer.chat_id != 0)
              {
                localTL_dialog.id = (-localTL_dialog.peer.chat_id);
                break label118;
              }
              if (localTL_dialog.peer.channel_id == 0) {
                break label118;
              }
              localTL_dialog.id = (-localTL_dialog.peer.channel_id);
              break label118;
              label425:
              paramAnonymousTLObject = new TLRPC.TL_updateReadHistoryInbox();
              paramAnonymousTLObject.peer = localTL_dialog.peer;
              paramAnonymousTLObject.max_id = localTL_dialog.read_inbox_max_id;
              localArrayList.add(paramAnonymousTLObject);
              break label239;
              label461:
              paramAnonymousTLObject = new TLRPC.TL_updateReadHistoryOutbox();
              paramAnonymousTLObject.peer = localTL_dialog.peer;
              paramAnonymousTLObject.max_id = localTL_dialog.read_outbox_max_id;
              localArrayList.add(paramAnonymousTLObject);
            }
          }
          if (!localArrayList.isEmpty()) {
            MessagesController.this.processUpdateArray(localArrayList, null, null, false);
          }
        }
      }
    });
  }
  
  private void reloadMessages(ArrayList<Integer> paramArrayList, final long paramLong)
  {
    if (paramArrayList.isEmpty()) {}
    final ArrayList localArrayList2;
    TLRPC.Chat localChat;
    Object localObject;
    ArrayList localArrayList1;
    label77:
    label140:
    do
    {
      return;
      localArrayList2 = new ArrayList();
      localChat = ChatObject.getChatByDialog(paramLong, this.currentAccount);
      int i;
      Integer localInteger;
      if (ChatObject.isChannel(localChat))
      {
        localObject = new TLRPC.TL_channels_getMessages();
        ((TLRPC.TL_channels_getMessages)localObject).channel = getInputChannel(localChat);
        ((TLRPC.TL_channels_getMessages)localObject).id = localArrayList2;
        localArrayList1 = (ArrayList)this.reloadingMessages.get(paramLong);
        i = 0;
        if (i >= paramArrayList.size()) {
          continue;
        }
        localInteger = (Integer)paramArrayList.get(i);
        if ((localArrayList1 == null) || (!localArrayList1.contains(localInteger))) {
          break label140;
        }
      }
      for (;;)
      {
        i += 1;
        break label77;
        localObject = new TLRPC.TL_messages_getMessages();
        ((TLRPC.TL_messages_getMessages)localObject).id = localArrayList2;
        break;
        localArrayList2.add(localInteger);
      }
    } while (localArrayList2.isEmpty());
    paramArrayList = localArrayList1;
    if (localArrayList1 == null)
    {
      paramArrayList = new ArrayList();
      this.reloadingMessages.put(paramLong, paramArrayList);
    }
    paramArrayList.addAll(localArrayList2);
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
          SparseArray localSparseArray1 = new SparseArray();
          int i = 0;
          while (i < localmessages_Messages.users.size())
          {
            paramAnonymousTLObject = (TLRPC.User)localmessages_Messages.users.get(i);
            localSparseArray1.put(paramAnonymousTLObject.id, paramAnonymousTLObject);
            i += 1;
          }
          SparseArray localSparseArray2 = new SparseArray();
          i = 0;
          while (i < localmessages_Messages.chats.size())
          {
            paramAnonymousTLObject = (TLRPC.Chat)localmessages_Messages.chats.get(i);
            localSparseArray2.put(paramAnonymousTLObject.id, paramAnonymousTLObject);
            i += 1;
          }
          paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(paramLong));
          paramAnonymousTLObject = paramAnonymousTL_error;
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(false, paramLong));
            MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(paramLong), paramAnonymousTLObject);
          }
          Object localObject = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(paramLong));
          paramAnonymousTL_error = (TLRPC.TL_error)localObject;
          if (localObject == null)
          {
            paramAnonymousTL_error = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(true, paramLong));
            MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(paramLong), paramAnonymousTL_error);
          }
          final ArrayList localArrayList = new ArrayList();
          i = 0;
          if (i < localmessages_Messages.messages.size())
          {
            TLRPC.Message localMessage = (TLRPC.Message)localmessages_Messages.messages.get(i);
            if ((localArrayList2 != null) && (localArrayList2.megagroup)) {
              localMessage.flags |= 0x80000000;
            }
            localMessage.dialog_id = paramLong;
            if (localMessage.out)
            {
              localObject = paramAnonymousTL_error;
              label342:
              if (((Integer)localObject).intValue() >= localMessage.id) {
                break label405;
              }
            }
            label405:
            for (boolean bool = true;; bool = false)
            {
              localMessage.unread = bool;
              localArrayList.add(new MessageObject(MessagesController.this.currentAccount, localMessage, localSparseArray1, localSparseArray2, true));
              i += 1;
              break;
              localObject = paramAnonymousTLObject;
              break label342;
            }
          }
          ImageLoader.saveMessagesThumbs(localmessages_Messages.messages);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(localmessages_Messages, paramLong, -1, 0, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              Object localObject = (ArrayList)MessagesController.this.reloadingMessages.get(MessagesController.17.this.val$dialog_id);
              if (localObject != null)
              {
                ((ArrayList)localObject).removeAll(MessagesController.17.this.val$result);
                if (((ArrayList)localObject).isEmpty()) {
                  MessagesController.this.reloadingMessages.remove(MessagesController.17.this.val$dialog_id);
                }
              }
              MessageObject localMessageObject = (MessageObject)MessagesController.this.dialogMessage.get(MessagesController.17.this.val$dialog_id);
              int i;
              if (localMessageObject != null) {
                i = 0;
              }
              for (;;)
              {
                if (i < localArrayList.size())
                {
                  localObject = (MessageObject)localArrayList.get(i);
                  if ((localMessageObject != null) && (localMessageObject.getId() == ((MessageObject)localObject).getId()))
                  {
                    MessagesController.this.dialogMessage.put(MessagesController.17.this.val$dialog_id, localObject);
                    if (((MessageObject)localObject).messageOwner.to_id.channel_id == 0)
                    {
                      localMessageObject = (MessageObject)MessagesController.this.dialogMessagesByIds.get(((MessageObject)localObject).getId());
                      MessagesController.this.dialogMessagesByIds.remove(((MessageObject)localObject).getId());
                      if (localMessageObject != null) {
                        MessagesController.this.dialogMessagesByIds.put(localMessageObject.getId(), localMessageObject);
                      }
                    }
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                  }
                }
                else
                {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, new Object[] { Long.valueOf(MessagesController.17.this.val$dialog_id), localArrayList });
                  return;
                }
                i += 1;
              }
            }
          });
        }
      }
    });
  }
  
  private void resetDialogs(boolean paramBoolean, final int paramInt1, final int paramInt2, final int paramInt3, final int paramInt4)
  {
    if (paramBoolean) {
      if (!this.resetingDialogs) {}
    }
    while ((this.resetDialogsPinned == null) || (this.resetDialogsAll == null))
    {
      return;
      this.resetingDialogs = true;
      localObject1 = new TLRPC.TL_messages_getPinnedDialogs();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTLObject != null)
          {
            MessagesController.access$1602(MessagesController.this, (TLRPC.TL_messages_peerDialogs)paramAnonymousTLObject);
            MessagesController.this.resetDialogs(false, paramInt1, paramInt2, paramInt3, paramInt4);
          }
        }
      });
      localObject1 = new TLRPC.TL_messages_getDialogs();
      ((TLRPC.TL_messages_getDialogs)localObject1).limit = 100;
      ((TLRPC.TL_messages_getDialogs)localObject1).exclude_pinned = true;
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerEmpty();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            MessagesController.access$1702(MessagesController.this, (TLRPC.messages_Dialogs)paramAnonymousTLObject);
            MessagesController.this.resetDialogs(false, paramInt1, paramInt2, paramInt3, paramInt4);
          }
        }
      });
      return;
    }
    int j = this.resetDialogsAll.messages.size();
    int k = this.resetDialogsAll.dialogs.size();
    this.resetDialogsAll.dialogs.addAll(this.resetDialogsPinned.dialogs);
    this.resetDialogsAll.messages.addAll(this.resetDialogsPinned.messages);
    this.resetDialogsAll.users.addAll(this.resetDialogsPinned.users);
    this.resetDialogsAll.chats.addAll(this.resetDialogsPinned.chats);
    LongSparseArray localLongSparseArray1 = new LongSparseArray();
    LongSparseArray localLongSparseArray2 = new LongSparseArray();
    SparseArray localSparseArray = new SparseArray();
    Object localObject4 = new SparseArray();
    int i = 0;
    while (i < this.resetDialogsAll.users.size())
    {
      localObject1 = (TLRPC.User)this.resetDialogsAll.users.get(i);
      localSparseArray.put(((TLRPC.User)localObject1).id, localObject1);
      i += 1;
    }
    i = 0;
    while (i < this.resetDialogsAll.chats.size())
    {
      localObject1 = (TLRPC.Chat)this.resetDialogsAll.chats.get(i);
      ((SparseArray)localObject4).put(((TLRPC.Chat)localObject1).id, localObject1);
      i += 1;
    }
    Object localObject1 = null;
    i = 0;
    Object localObject3;
    Object localObject2;
    if (i < this.resetDialogsAll.messages.size())
    {
      localObject3 = (TLRPC.Message)this.resetDialogsAll.messages.get(i);
      localObject2 = localObject1;
      if (i < j) {
        if (localObject1 != null)
        {
          localObject2 = localObject1;
          if (((TLRPC.Message)localObject3).date >= ((TLRPC.Message)localObject1).date) {}
        }
        else
        {
          localObject2 = localObject3;
        }
      }
      if (((TLRPC.Message)localObject3).to_id.channel_id != 0)
      {
        localObject1 = (TLRPC.Chat)((SparseArray)localObject4).get(((TLRPC.Message)localObject3).to_id.channel_id);
        if ((localObject1 == null) || (!((TLRPC.Chat)localObject1).left)) {}
      }
      for (;;)
      {
        i += 1;
        localObject1 = localObject2;
        break;
        if ((localObject1 != null) && (((TLRPC.Chat)localObject1).megagroup)) {
          ((TLRPC.Message)localObject3).flags |= 0x80000000;
        }
        do
        {
          do
          {
            localObject1 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject3, localSparseArray, (SparseArray)localObject4, false);
            localLongSparseArray2.put(((MessageObject)localObject1).getDialogId(), localObject1);
            break;
          } while (((TLRPC.Message)localObject3).to_id.chat_id == 0);
          localObject1 = (TLRPC.Chat)((SparseArray)localObject4).get(((TLRPC.Message)localObject3).to_id.chat_id);
        } while ((localObject1 == null) || (((TLRPC.Chat)localObject1).migrated_to == null));
      }
    }
    i = 0;
    Object localObject5;
    if (i < this.resetDialogsAll.dialogs.size())
    {
      localObject5 = (TLRPC.TL_dialog)this.resetDialogsAll.dialogs.get(i);
      if ((((TLRPC.TL_dialog)localObject5).id == 0L) && (((TLRPC.TL_dialog)localObject5).peer != null))
      {
        if (((TLRPC.TL_dialog)localObject5).peer.user_id != 0) {
          ((TLRPC.TL_dialog)localObject5).id = ((TLRPC.TL_dialog)localObject5).peer.user_id;
        }
      }
      else {
        label688:
        if (((TLRPC.TL_dialog)localObject5).id != 0L) {
          break label765;
        }
      }
      for (;;)
      {
        i += 1;
        break;
        if (((TLRPC.TL_dialog)localObject5).peer.chat_id != 0)
        {
          ((TLRPC.TL_dialog)localObject5).id = (-((TLRPC.TL_dialog)localObject5).peer.chat_id);
          break label688;
        }
        if (((TLRPC.TL_dialog)localObject5).peer.channel_id == 0) {
          break label688;
        }
        ((TLRPC.TL_dialog)localObject5).id = (-((TLRPC.TL_dialog)localObject5).peer.channel_id);
        break label688;
        label765:
        if (((TLRPC.TL_dialog)localObject5).last_message_date == 0)
        {
          localObject2 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject5).id);
          if (localObject2 != null) {
            ((TLRPC.TL_dialog)localObject5).last_message_date = ((MessageObject)localObject2).messageOwner.date;
          }
        }
        if (DialogObject.isChannel((TLRPC.TL_dialog)localObject5))
        {
          localObject2 = (TLRPC.Chat)((SparseArray)localObject4).get(-(int)((TLRPC.TL_dialog)localObject5).id);
          if ((localObject2 == null) || (!((TLRPC.Chat)localObject2).left)) {
            this.channelsPts.put(-(int)((TLRPC.TL_dialog)localObject5).id, ((TLRPC.TL_dialog)localObject5).pts);
          }
        }
        else
        {
          do
          {
            do
            {
              localLongSparseArray1.put(((TLRPC.TL_dialog)localObject5).id, localObject5);
              localObject3 = (Integer)this.dialogs_read_inbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject5).id));
              localObject2 = localObject3;
              if (localObject3 == null) {
                localObject2 = Integer.valueOf(0);
              }
              this.dialogs_read_inbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject5).id), Integer.valueOf(Math.max(((Integer)localObject2).intValue(), ((TLRPC.TL_dialog)localObject5).read_inbox_max_id)));
              localObject3 = (Integer)this.dialogs_read_outbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject5).id));
              localObject2 = localObject3;
              if (localObject3 == null) {
                localObject2 = Integer.valueOf(0);
              }
              this.dialogs_read_outbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject5).id), Integer.valueOf(Math.max(((Integer)localObject2).intValue(), ((TLRPC.TL_dialog)localObject5).read_outbox_max_id)));
              break;
            } while ((int)((TLRPC.TL_dialog)localObject5).id >= 0);
            localObject2 = (TLRPC.Chat)((SparseArray)localObject4).get(-(int)((TLRPC.TL_dialog)localObject5).id);
          } while ((localObject2 == null) || (((TLRPC.Chat)localObject2).migrated_to == null));
        }
      }
    }
    ImageLoader.saveMessagesThumbs(this.resetDialogsAll.messages);
    i = 0;
    while (i < this.resetDialogsAll.messages.size())
    {
      localObject5 = (TLRPC.Message)this.resetDialogsAll.messages.get(i);
      if ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChatDeleteUser))
      {
        localObject2 = (TLRPC.User)localSparseArray.get(((TLRPC.Message)localObject5).action.user_id);
        if ((localObject2 != null) && (((TLRPC.User)localObject2).bot))
        {
          ((TLRPC.Message)localObject5).reply_markup = new TLRPC.TL_replyKeyboardHide();
          ((TLRPC.Message)localObject5).flags |= 0x40;
        }
      }
      if (((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChatMigrateTo)) || ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChannelCreate)))
      {
        ((TLRPC.Message)localObject5).unread = false;
        ((TLRPC.Message)localObject5).media_unread = false;
        i += 1;
      }
      else
      {
        if (((TLRPC.Message)localObject5).out)
        {
          localObject2 = this.dialogs_read_outbox_max;
          label1223:
          localObject4 = (Integer)((ConcurrentHashMap)localObject2).get(Long.valueOf(((TLRPC.Message)localObject5).dialog_id));
          localObject3 = localObject4;
          if (localObject4 == null)
          {
            localObject3 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(((TLRPC.Message)localObject5).out, ((TLRPC.Message)localObject5).dialog_id));
            ((ConcurrentHashMap)localObject2).put(Long.valueOf(((TLRPC.Message)localObject5).dialog_id), localObject3);
          }
          if (((Integer)localObject3).intValue() >= ((TLRPC.Message)localObject5).id) {
            break label1324;
          }
        }
        label1324:
        for (paramBoolean = true;; paramBoolean = false)
        {
          ((TLRPC.Message)localObject5).unread = paramBoolean;
          break;
          localObject2 = this.dialogs_read_inbox_max;
          break label1223;
        }
      }
    }
    MessagesStorage.getInstance(this.currentAccount).resetDialogs(this.resetDialogsAll, j, paramInt1, paramInt2, paramInt3, paramInt4, localLongSparseArray1, localLongSparseArray2, (TLRPC.Message)localObject1, k);
    this.resetDialogsPinned = null;
    this.resetDialogsAll = null;
  }
  
  private void setUpdatesStartTime(int paramInt, long paramLong)
  {
    if (paramInt == 0) {
      this.updatesStartWaitTimeSeq = paramLong;
    }
    do
    {
      return;
      if (paramInt == 1)
      {
        this.updatesStartWaitTimePts = paramLong;
        return;
      }
    } while (paramInt != 2);
    this.updatesStartWaitTimeQts = paramLong;
  }
  
  private static void showCantOpenAlert(BaseFragment paramBaseFragment, String paramString)
  {
    if ((paramBaseFragment == null) || (paramBaseFragment.getParentActivity() == null)) {
      return;
    }
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramBaseFragment.getParentActivity());
    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    localBuilder.setMessage(paramString);
    paramBaseFragment.showDialog(localBuilder.create());
  }
  
  private void updatePrintingStrings()
  {
    final LongSparseArray localLongSparseArray1 = new LongSparseArray();
    final LongSparseArray localLongSparseArray2 = new LongSparseArray();
    new ArrayList(this.printingUsers.keySet());
    Iterator localIterator1 = this.printingUsers.entrySet().iterator();
    while (localIterator1.hasNext())
    {
      Object localObject1 = (Map.Entry)localIterator1.next();
      long l = ((Long)((Map.Entry)localObject1).getKey()).longValue();
      localObject1 = (ArrayList)((Map.Entry)localObject1).getValue();
      int i = (int)l;
      Object localObject2;
      if ((i > 0) || (i == 0) || (((ArrayList)localObject1).size() == 1))
      {
        localObject1 = (PrintingUser)((ArrayList)localObject1).get(0);
        localObject2 = getUser(Integer.valueOf(((PrintingUser)localObject1).userId));
        if (localObject2 != null)
        {
          if ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageRecordAudioAction))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsRecordingAudio", 2131493700, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(1));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("RecordingAudio", 2131494225));
            }
          }
          if (((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageRecordRoundAction)) || ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageUploadRoundAction)))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsRecordingRound", 2131493701, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(4));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("RecordingRound", 2131494226));
            }
          }
          if ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageUploadAudioAction))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsSendingAudio", 2131493702, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(2));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("SendingAudio", 2131494354));
            }
          }
          if (((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageUploadVideoAction)) || ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageRecordVideoAction)))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsSendingVideo", 2131493706, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(2));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("SendingVideoStatus", 2131494360));
            }
          }
          if ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageUploadDocumentAction))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsSendingFile", 2131493703, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(2));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("SendingFile", 2131494355));
            }
          }
          if ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageUploadPhotoAction))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsSendingPhoto", 2131493705, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(2));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("SendingPhoto", 2131494358));
            }
          }
          if ((((PrintingUser)localObject1).action instanceof TLRPC.TL_sendMessageGamePlayAction))
          {
            if (i < 0) {
              localLongSparseArray1.put(l, LocaleController.formatString("IsSendingGame", 2131493704, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
            }
            for (;;)
            {
              localLongSparseArray2.put(l, Integer.valueOf(3));
              break;
              localLongSparseArray1.put(l, LocaleController.getString("SendingGame", 2131494356));
            }
          }
          if (i < 0) {
            localLongSparseArray1.put(l, LocaleController.formatString("IsTypingGroup", 2131493708, new Object[] { getUserNameForTyping((TLRPC.User)localObject2) }));
          }
          for (;;)
          {
            localLongSparseArray2.put(l, Integer.valueOf(0));
            break;
            localLongSparseArray1.put(l, LocaleController.getString("Typing", 2131494503));
          }
        }
      }
      else
      {
        i = 0;
        localObject2 = new StringBuilder();
        Iterator localIterator2 = ((ArrayList)localObject1).iterator();
        int j;
        do
        {
          j = i;
          if (!localIterator2.hasNext()) {
            break;
          }
          TLRPC.User localUser = getUser(Integer.valueOf(((PrintingUser)localIterator2.next()).userId));
          j = i;
          if (localUser != null)
          {
            if (((StringBuilder)localObject2).length() != 0) {
              ((StringBuilder)localObject2).append(", ");
            }
            ((StringBuilder)localObject2).append(getUserNameForTyping(localUser));
            j = i + 1;
          }
          i = j;
        } while (j != 2);
        if (((StringBuilder)localObject2).length() != 0)
        {
          if (j == 1) {
            localLongSparseArray1.put(l, LocaleController.formatString("IsTypingGroup", 2131493708, new Object[] { ((StringBuilder)localObject2).toString() }));
          }
          for (;;)
          {
            localLongSparseArray2.put(l, Integer.valueOf(0));
            break;
            if (((ArrayList)localObject1).size() > 2) {
              localLongSparseArray1.put(l, String.format(LocaleController.getPluralString("AndMoreTypingGroup", ((ArrayList)localObject1).size() - 2), new Object[] { ((StringBuilder)localObject2).toString(), Integer.valueOf(((ArrayList)localObject1).size() - 2) }));
            } else {
              localLongSparseArray1.put(l, LocaleController.formatString("AreTypingGroup", 2131492997, new Object[] { ((StringBuilder)localObject2).toString() }));
            }
          }
        }
      }
    }
    this.lastPrintingStringCount = localLongSparseArray1.size();
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.this.printingStrings = localLongSparseArray1;
        MessagesController.this.printingStringsTypes = localLongSparseArray2;
      }
    });
  }
  
  private boolean updatePrintingUsersWithNewMessages(long paramLong, ArrayList<MessageObject> paramArrayList)
  {
    if (paramLong > 0L)
    {
      if ((ArrayList)this.printingUsers.get(Long.valueOf(paramLong)) != null) {
        this.printingUsers.remove(Long.valueOf(paramLong));
      }
    }
    else
    {
      int k;
      do
      {
        return true;
        if (paramLong >= 0L) {
          break;
        }
        ArrayList localArrayList = new ArrayList();
        paramArrayList = paramArrayList.iterator();
        while (paramArrayList.hasNext())
        {
          MessageObject localMessageObject = (MessageObject)paramArrayList.next();
          if (!localArrayList.contains(Integer.valueOf(localMessageObject.messageOwner.from_id))) {
            localArrayList.add(Integer.valueOf(localMessageObject.messageOwner.from_id));
          }
        }
        paramArrayList = (ArrayList)this.printingUsers.get(Long.valueOf(paramLong));
        k = 0;
        int j = 0;
        if (paramArrayList != null) {
          for (int i = 0;; i = k + 1)
          {
            k = j;
            if (i >= paramArrayList.size()) {
              break;
            }
            k = i;
            if (localArrayList.contains(Integer.valueOf(((PrintingUser)paramArrayList.get(i)).userId)))
            {
              paramArrayList.remove(i);
              k = i - 1;
              if (paramArrayList.isEmpty()) {
                this.printingUsers.remove(Long.valueOf(paramLong));
              }
              j = 1;
            }
          }
        }
      } while (k != 0);
    }
    return false;
  }
  
  public void addSupportUser()
  {
    TLRPC.TL_userForeign_old2 localTL_userForeign_old2 = new TLRPC.TL_userForeign_old2();
    localTL_userForeign_old2.phone = "333";
    localTL_userForeign_old2.id = 333000;
    localTL_userForeign_old2.first_name = "Telegram";
    localTL_userForeign_old2.last_name = "";
    localTL_userForeign_old2.status = null;
    localTL_userForeign_old2.photo = new TLRPC.TL_userProfilePhotoEmpty();
    putUser(localTL_userForeign_old2, true);
    localTL_userForeign_old2 = new TLRPC.TL_userForeign_old2();
    localTL_userForeign_old2.phone = "42777";
    localTL_userForeign_old2.id = 777000;
    localTL_userForeign_old2.first_name = "Telegram";
    localTL_userForeign_old2.last_name = "Notifications";
    localTL_userForeign_old2.status = null;
    localTL_userForeign_old2.photo = new TLRPC.TL_userProfilePhotoEmpty();
    putUser(localTL_userForeign_old2, true);
  }
  
  public void addToViewsQueue(final TLRPC.Message paramMessage)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        int i;
        if (paramMessage.to_id.channel_id != 0) {
          i = -paramMessage.to_id.channel_id;
        }
        for (;;)
        {
          ArrayList localArrayList2 = (ArrayList)MessagesController.this.channelViewsToSend.get(i);
          ArrayList localArrayList1 = localArrayList2;
          if (localArrayList2 == null)
          {
            localArrayList1 = new ArrayList();
            MessagesController.this.channelViewsToSend.put(i, localArrayList1);
          }
          if (!localArrayList1.contains(Integer.valueOf(paramMessage.id))) {
            localArrayList1.add(Integer.valueOf(paramMessage.id));
          }
          return;
          if (paramMessage.to_id.chat_id != 0) {
            i = -paramMessage.to_id.chat_id;
          } else {
            i = paramMessage.to_id.user_id;
          }
        }
      }
    });
  }
  
  public void addUserToChat(final int paramInt1, final TLRPC.User paramUser, final TLRPC.ChatFull paramChatFull, int paramInt2, String paramString, final BaseFragment paramBaseFragment)
  {
    if (paramUser == null) {}
    label153:
    label182:
    label209:
    label278:
    do
    {
      final boolean bool2;
      final boolean bool1;
      do
      {
        return;
        if (paramInt1 <= 0) {
          break label278;
        }
        bool2 = ChatObject.isChannel(paramInt1, this.currentAccount);
        if ((!bool2) || (!getChat(Integer.valueOf(paramInt1)).megagroup)) {
          break;
        }
        bool1 = true;
        paramChatFull = getInputUser(paramUser);
        if ((paramString != null) && ((!bool2) || (bool1))) {
          break label209;
        }
        if (!bool2) {
          break label182;
        }
        if (!(paramChatFull instanceof TLRPC.TL_inputUserSelf)) {
          break label153;
        }
      } while (this.joiningToChannels.contains(Integer.valueOf(paramInt1)));
      paramUser = new TLRPC.TL_channels_joinChannel();
      paramUser.channel = getInputChannel(paramInt1);
      this.joiningToChannels.add(Integer.valueOf(paramInt1));
      for (;;)
      {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramUser, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            if ((bool2) && ((paramChatFull instanceof TLRPC.TL_inputUserSelf))) {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.joiningToChannels.remove(Integer.valueOf(MessagesController.100.this.val$chat_id));
                }
              });
            }
            if (paramAnonymousTL_error != null)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  boolean bool = true;
                  int i = MessagesController.this.currentAccount;
                  TLRPC.TL_error localTL_error = paramAnonymousTL_error;
                  BaseFragment localBaseFragment = MessagesController.100.this.val$fragment;
                  TLObject localTLObject = MessagesController.100.this.val$request;
                  if ((MessagesController.100.this.val$isChannel) && (!MessagesController.100.this.val$isMegagroup)) {}
                  for (;;)
                  {
                    AlertsCreator.processError(i, localTL_error, localBaseFragment, localTLObject, new Object[] { Boolean.valueOf(bool) });
                    return;
                    bool = false;
                  }
                }
              });
              return;
            }
            int k = 0;
            paramAnonymousTLObject = (TLRPC.Updates)paramAnonymousTLObject;
            int i = 0;
            for (;;)
            {
              int j = k;
              if (i < paramAnonymousTLObject.updates.size())
              {
                paramAnonymousTL_error = (TLRPC.Update)paramAnonymousTLObject.updates.get(i);
                if (((paramAnonymousTL_error instanceof TLRPC.TL_updateNewChannelMessage)) && ((((TLRPC.TL_updateNewChannelMessage)paramAnonymousTL_error).message.action instanceof TLRPC.TL_messageActionChatAddUser))) {
                  j = 1;
                }
              }
              else
              {
                MessagesController.this.processUpdates(paramAnonymousTLObject, false);
                if (bool2)
                {
                  if ((j == 0) && ((paramChatFull instanceof TLRPC.TL_inputUserSelf))) {
                    MessagesController.this.generateJoinMessage(paramInt1, true);
                  }
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      MessagesController.this.loadFullChat(MessagesController.100.this.val$chat_id, 0, true);
                    }
                  }, 1000L);
                }
                if ((!bool2) || (!(paramChatFull instanceof TLRPC.TL_inputUserSelf))) {
                  break;
                }
                MessagesStorage.getInstance(MessagesController.this.currentAccount).updateDialogsWithDeletedMessages(new ArrayList(), null, true, paramInt1);
                return;
              }
              i += 1;
            }
          }
        });
        return;
        bool1 = false;
        break;
        paramUser = new TLRPC.TL_channels_inviteToChannel();
        paramUser.channel = getInputChannel(paramInt1);
        paramUser.users.add(paramChatFull);
        continue;
        paramUser = new TLRPC.TL_messages_addChatUser();
        paramUser.chat_id = paramInt1;
        paramUser.fwd_limit = paramInt2;
        paramUser.user_id = paramChatFull;
      }
      paramUser = new TLRPC.TL_messages_startBot();
      paramUser.bot = paramChatFull;
      if (bool2) {
        paramUser.peer = getInputPeer(-paramInt1);
      }
      for (;;)
      {
        paramUser.start_param = paramString;
        paramUser.random_id = Utilities.random.nextLong();
        break;
        paramUser.peer = new TLRPC.TL_inputPeerChat();
        paramUser.peer.chat_id = paramInt1;
      }
    } while (!(paramChatFull instanceof TLRPC.TL_chatFull));
    paramInt2 = 0;
    for (;;)
    {
      if (paramInt2 >= paramChatFull.participants.participants.size()) {
        break label337;
      }
      if (((TLRPC.ChatParticipant)paramChatFull.participants.participants.get(paramInt2)).user_id == paramUser.id) {
        break;
      }
      paramInt2 += 1;
    }
    label337:
    paramString = getChat(Integer.valueOf(paramInt1));
    paramString.participants_count += 1;
    paramBaseFragment = new ArrayList();
    paramBaseFragment.add(paramString);
    MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(null, paramBaseFragment, true, true);
    paramString = new TLRPC.TL_chatParticipant();
    paramString.user_id = paramUser.id;
    paramString.inviter_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
    paramString.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    paramChatFull.participants.participants.add(0, paramString);
    MessagesStorage.getInstance(this.currentAccount).updateChatInfo(paramChatFull, true);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { paramChatFull, Integer.valueOf(0), Boolean.valueOf(false), null });
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(32) });
  }
  
  public void addUsersToChannel(int paramInt, ArrayList<TLRPC.InputUser> paramArrayList, final BaseFragment paramBaseFragment)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    final TLRPC.TL_channels_inviteToChannel localTL_channels_inviteToChannel = new TLRPC.TL_channels_inviteToChannel();
    localTL_channels_inviteToChannel.channel = getInputChannel(paramInt);
    localTL_channels_inviteToChannel.users = paramArrayList;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_inviteToChannel, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              AlertsCreator.processError(MessagesController.this.currentAccount, paramAnonymousTL_error, MessagesController.91.this.val$fragment, MessagesController.91.this.val$req, new Object[] { Boolean.valueOf(true) });
            }
          });
          return;
        }
        MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
      }
    });
  }
  
  public void blockUser(int paramInt)
  {
    final TLRPC.User localUser = getUser(Integer.valueOf(paramInt));
    if ((localUser == null) || (this.blockedUsers.contains(Integer.valueOf(paramInt)))) {
      return;
    }
    this.blockedUsers.add(Integer.valueOf(paramInt));
    if (localUser.bot) {
      DataQuery.getInstance(this.currentAccount).removeInline(paramInt);
    }
    for (;;)
    {
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
      TLRPC.TL_contacts_block localTL_contacts_block = new TLRPC.TL_contacts_block();
      localTL_contacts_block.id = getInputUser(localUser);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_block, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = new ArrayList();
            paramAnonymousTLObject.add(Integer.valueOf(localUser.id));
            MessagesStorage.getInstance(MessagesController.this.currentAccount).putBlockedUsers(paramAnonymousTLObject, false);
          }
        }
      });
      return;
      DataQuery.getInstance(this.currentAccount).removePeer(paramInt);
    }
  }
  
  public boolean canPinDialog(boolean paramBoolean)
  {
    int j = 0;
    int i = 0;
    if (i < this.dialogs.size())
    {
      TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)this.dialogs.get(i);
      int m = (int)localTL_dialog.id;
      int k;
      if (paramBoolean)
      {
        k = j;
        if (m != 0) {}
      }
      else
      {
        if ((paramBoolean) || (m != 0)) {
          break label70;
        }
        k = j;
      }
      for (;;)
      {
        i += 1;
        j = k;
        break;
        label70:
        k = j;
        if (localTL_dialog.pinned) {
          k = j + 1;
        }
      }
    }
    return j < this.maxPinnedDialogsCount;
  }
  
  public void cancelLoadFullChat(int paramInt)
  {
    this.loadingFullChats.remove(Integer.valueOf(paramInt));
  }
  
  public void cancelLoadFullUser(int paramInt)
  {
    this.loadingFullUsers.remove(Integer.valueOf(paramInt));
  }
  
  public void cancelTyping(int paramInt, long paramLong)
  {
    LongSparseArray localLongSparseArray = (LongSparseArray)this.sendingTypings.get(paramInt);
    if (localLongSparseArray != null) {
      localLongSparseArray.remove(paramLong);
    }
  }
  
  public void changeChatAvatar(int paramInt, TLRPC.InputFile paramInputFile)
  {
    if (ChatObject.isChannel(paramInt, this.currentAccount))
    {
      localObject = new TLRPC.TL_channels_editPhoto();
      ((TLRPC.TL_channels_editPhoto)localObject).channel = getInputChannel(paramInt);
      if (paramInputFile != null)
      {
        ((TLRPC.TL_channels_editPhoto)localObject).photo = new TLRPC.TL_inputChatUploadedPhoto();
        ((TLRPC.TL_channels_editPhoto)localObject).photo.file = paramInputFile;
      }
      for (;;)
      {
        paramInputFile = (TLRPC.InputFile)localObject;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramInputFile, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error != null) {
              return;
            }
            MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          }
        }, 64);
        return;
        ((TLRPC.TL_channels_editPhoto)localObject).photo = new TLRPC.TL_inputChatPhotoEmpty();
      }
    }
    Object localObject = new TLRPC.TL_messages_editChatPhoto();
    ((TLRPC.TL_messages_editChatPhoto)localObject).chat_id = paramInt;
    if (paramInputFile != null)
    {
      ((TLRPC.TL_messages_editChatPhoto)localObject).photo = new TLRPC.TL_inputChatUploadedPhoto();
      ((TLRPC.TL_messages_editChatPhoto)localObject).photo.file = paramInputFile;
    }
    for (;;)
    {
      paramInputFile = (TLRPC.InputFile)localObject;
      break;
      ((TLRPC.TL_messages_editChatPhoto)localObject).photo = new TLRPC.TL_inputChatPhotoEmpty();
    }
  }
  
  public void changeChatTitle(int paramInt, String paramString)
  {
    if (paramInt > 0)
    {
      if (ChatObject.isChannel(paramInt, this.currentAccount))
      {
        localObject = new TLRPC.TL_channels_editTitle();
        ((TLRPC.TL_channels_editTitle)localObject).channel = getInputChannel(paramInt);
        ((TLRPC.TL_channels_editTitle)localObject).title = paramString;
      }
      for (paramString = (String)localObject;; paramString = (String)localObject)
      {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramString, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error != null) {
              return;
            }
            MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          }
        }, 64);
        return;
        localObject = new TLRPC.TL_messages_editChatTitle();
        ((TLRPC.TL_messages_editChatTitle)localObject).chat_id = paramInt;
        ((TLRPC.TL_messages_editChatTitle)localObject).title = paramString;
      }
    }
    Object localObject = getChat(Integer.valueOf(paramInt));
    ((TLRPC.Chat)localObject).title = paramString;
    paramString = new ArrayList();
    paramString.add(localObject);
    MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(null, paramString, true, true);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(16) });
  }
  
  public boolean checkCanOpenChat(Bundle paramBundle, BaseFragment paramBaseFragment)
  {
    return checkCanOpenChat(paramBundle, paramBaseFragment, null);
  }
  
  public boolean checkCanOpenChat(final Bundle paramBundle, final BaseFragment paramBaseFragment, MessageObject paramMessageObject)
  {
    if ((paramBundle == null) || (paramBaseFragment == null)) {
      return true;
    }
    final Object localObject2 = null;
    Object localObject1 = null;
    final int i = paramBundle.getInt("user_id", 0);
    int j = paramBundle.getInt("chat_id", 0);
    int k = paramBundle.getInt("message_id", 0);
    Object localObject3;
    if (i != 0) {
      localObject3 = getUser(Integer.valueOf(i));
    }
    while ((localObject3 == null) && (localObject1 == null))
    {
      return true;
      localObject3 = localObject2;
      if (j != 0)
      {
        localObject1 = getChat(Integer.valueOf(j));
        localObject3 = localObject2;
      }
    }
    localObject2 = null;
    if (localObject1 != null) {
      localObject2 = getRestrictionReason(((TLRPC.Chat)localObject1).restriction_reason);
    }
    while (localObject2 != null)
    {
      showCantOpenAlert(paramBaseFragment, (String)localObject2);
      return false;
      if (localObject3 != null) {
        localObject2 = getRestrictionReason(((TLRPC.User)localObject3).restriction_reason);
      }
    }
    if ((k != 0) && (paramMessageObject != null) && (localObject1 != null) && (((TLRPC.Chat)localObject1).access_hash == 0L))
    {
      i = (int)paramMessageObject.getDialogId();
      if (i != 0)
      {
        localObject2 = new AlertDialog(paramBaseFragment.getParentActivity(), 1);
        ((AlertDialog)localObject2).setMessage(LocaleController.getString("Loading", 2131493762));
        ((AlertDialog)localObject2).setCanceledOnTouchOutside(false);
        ((AlertDialog)localObject2).setCancelable(false);
        if (i < 0) {
          localObject1 = getChat(Integer.valueOf(-i));
        }
        if ((i > 0) || (!ChatObject.isChannel((TLRPC.Chat)localObject1)))
        {
          localObject1 = new TLRPC.TL_messages_getMessages();
          ((TLRPC.TL_messages_getMessages)localObject1).id.add(Integer.valueOf(paramMessageObject.getId()));
        }
        for (paramMessageObject = (MessageObject)localObject1;; paramMessageObject = (MessageObject)localObject1)
        {
          i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramMessageObject, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTLObject != null) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    try
                    {
                      MessagesController.136.this.val$progressDialog.dismiss();
                      TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
                      MessagesController.this.putUsers(localmessages_Messages.users, false);
                      MessagesController.this.putChats(localmessages_Messages.chats, false);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localmessages_Messages.users, localmessages_Messages.chats, true, true);
                      MessagesController.136.this.val$fragment.presentFragment(new ChatActivity(MessagesController.136.this.val$bundle), true);
                      return;
                    }
                    catch (Exception localException)
                    {
                      for (;;)
                      {
                        FileLog.e(localException);
                      }
                    }
                  }
                });
              }
            }
          });
          ((AlertDialog)localObject2).setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              ConnectionsManager.getInstance(MessagesController.this.currentAccount).cancelRequest(i, true);
              try
              {
                paramAnonymousDialogInterface.dismiss();
                if (paramBaseFragment != null) {
                  paramBaseFragment.setVisibleDialog(null);
                }
                return;
              }
              catch (Exception paramAnonymousDialogInterface)
              {
                for (;;)
                {
                  FileLog.e(paramAnonymousDialogInterface);
                }
              }
            }
          });
          paramBaseFragment.setVisibleDialog((Dialog)localObject2);
          ((AlertDialog)localObject2).show();
          return false;
          localObject3 = getChat(Integer.valueOf(-i));
          localObject1 = new TLRPC.TL_channels_getMessages();
          ((TLRPC.TL_channels_getMessages)localObject1).channel = getInputChannel((TLRPC.Chat)localObject3);
          ((TLRPC.TL_channels_getMessages)localObject1).id.add(Integer.valueOf(paramMessageObject.getId()));
        }
      }
    }
    return true;
  }
  
  public void checkChannelInviter(final int paramInt)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        final TLRPC.Chat localChat = MessagesController.this.getChat(Integer.valueOf(paramInt));
        if ((localChat == null) || (!ChatObject.isChannel(paramInt, MessagesController.this.currentAccount)) || (localChat.creator)) {
          return;
        }
        TLRPC.TL_channels_getParticipant localTL_channels_getParticipant = new TLRPC.TL_channels_getParticipant();
        localTL_channels_getParticipant.channel = MessagesController.this.getInputChannel(paramInt);
        localTL_channels_getParticipant.user_id = new TLRPC.TL_inputUserSelf();
        ConnectionsManager.getInstance(MessagesController.this.currentAccount).sendRequest(localTL_channels_getParticipant, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
          {
            paramAnonymous2TLObject = (TLRPC.TL_channels_channelParticipant)paramAnonymous2TLObject;
            if ((paramAnonymous2TLObject == null) || (!(paramAnonymous2TLObject.participant instanceof TLRPC.TL_channelParticipantSelf)) || (paramAnonymous2TLObject.participant.inviter_id == UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()) || ((localChat.megagroup) && (MessagesStorage.getInstance(MessagesController.this.currentAccount).isMigratedChat(localChat.id)))) {
              return;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.putUsers(paramAnonymous2TLObject.users, false);
              }
            });
            MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(paramAnonymous2TLObject.users, null, true, true);
            paramAnonymous2TL_error = new TLRPC.TL_messageService();
            paramAnonymous2TL_error.media_unread = true;
            paramAnonymous2TL_error.unread = true;
            paramAnonymous2TL_error.flags = 256;
            paramAnonymous2TL_error.post = true;
            if (localChat.megagroup) {
              paramAnonymous2TL_error.flags |= 0x80000000;
            }
            int i = UserConfig.getInstance(MessagesController.this.currentAccount).getNewMessageId();
            paramAnonymous2TL_error.id = i;
            paramAnonymous2TL_error.local_id = i;
            paramAnonymous2TL_error.date = paramAnonymous2TLObject.participant.date;
            paramAnonymous2TL_error.action = new TLRPC.TL_messageActionChatAddUser();
            paramAnonymous2TL_error.from_id = paramAnonymous2TLObject.participant.inviter_id;
            paramAnonymous2TL_error.action.users.add(Integer.valueOf(UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()));
            paramAnonymous2TL_error.to_id = new TLRPC.TL_peerChannel();
            paramAnonymous2TL_error.to_id.channel_id = MessagesController.121.this.val$chat_id;
            paramAnonymous2TL_error.dialog_id = (-MessagesController.121.this.val$chat_id);
            UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
            final ArrayList localArrayList1 = new ArrayList();
            ArrayList localArrayList2 = new ArrayList();
            ConcurrentHashMap localConcurrentHashMap = new ConcurrentHashMap();
            i = 0;
            while (i < paramAnonymous2TLObject.users.size())
            {
              TLRPC.User localUser = (TLRPC.User)paramAnonymous2TLObject.users.get(i);
              localConcurrentHashMap.put(Integer.valueOf(localUser.id), localUser);
              i += 1;
            }
            localArrayList2.add(paramAnonymous2TL_error);
            localArrayList1.add(new MessageObject(MessagesController.this.currentAccount, paramAnonymous2TL_error, localConcurrentHashMap, true));
            MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
            {
              public void run()
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationsController.getInstance(MessagesController.this.currentAccount).processNewMessages(MessagesController.121.1.2.this.val$pushMessages, true, false);
                  }
                });
              }
            });
            MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(localArrayList2, true, true, false, 0);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.updateInterfaceWithMessages(-MessagesController.121.this.val$chat_id, localArrayList1);
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
              }
            });
          }
        });
      }
    });
  }
  
  protected void checkLastDialogMessage(final TLRPC.TL_dialog paramTL_dialog, TLRPC.InputPeer paramInputPeer, final long paramLong)
  {
    int i = (int)paramTL_dialog.id;
    if ((i == 0) || (this.checkingLastMessagesDialogs.indexOfKey(i) >= 0)) {}
    TLRPC.TL_messages_getHistory localTL_messages_getHistory;
    Object localObject1;
    do
    {
      return;
      localTL_messages_getHistory = new TLRPC.TL_messages_getHistory();
      if (paramInputPeer != null) {
        break;
      }
      localObject1 = getInputPeer(i);
      localTL_messages_getHistory.peer = ((TLRPC.InputPeer)localObject1);
    } while ((localTL_messages_getHistory.peer == null) || ((localTL_messages_getHistory.peer instanceof TLRPC.TL_inputPeerChannel)));
    localTL_messages_getHistory.limit = 1;
    this.checkingLastMessagesDialogs.put(i, true);
    Object localObject3;
    if (paramLong == 0L) {
      localObject3 = null;
    }
    for (;;)
    {
      try
      {
        localObject1 = new NativeByteBuffer(localTL_messages_getHistory.peer.getObjectSize() + 48);
      }
      catch (Exception localException1)
      {
        paramInputPeer = (TLRPC.InputPeer)localObject3;
      }
      try
      {
        ((NativeByteBuffer)localObject1).writeInt32(8);
        ((NativeByteBuffer)localObject1).writeInt64(paramTL_dialog.id);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.top_message);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.read_inbox_max_id);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.read_outbox_max_id);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.unread_count);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.last_message_date);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.pts);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.flags);
        ((NativeByteBuffer)localObject1).writeBool(paramTL_dialog.pinned);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.pinnedNum);
        ((NativeByteBuffer)localObject1).writeInt32(paramTL_dialog.unread_mentions_count);
        paramInputPeer.serializeToStream((AbstractSerializedData)localObject1);
        paramInputPeer = (TLRPC.InputPeer)localObject1;
        paramLong = MessagesStorage.getInstance(this.currentAccount).createPendingTask(paramInputPeer);
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getHistory, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTLObject != null)
            {
              paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
              if (paramAnonymousTLObject.messages.isEmpty()) {
                break label329;
              }
              paramAnonymousTL_error = new TLRPC.TL_messages_dialogs();
              TLRPC.Message localMessage = (TLRPC.Message)paramAnonymousTLObject.messages.get(0);
              TLRPC.TL_dialog localTL_dialog = new TLRPC.TL_dialog();
              localTL_dialog.flags = paramTL_dialog.flags;
              localTL_dialog.top_message = localMessage.id;
              localTL_dialog.last_message_date = localMessage.date;
              localTL_dialog.notify_settings = paramTL_dialog.notify_settings;
              localTL_dialog.pts = paramTL_dialog.pts;
              localTL_dialog.unread_count = paramTL_dialog.unread_count;
              localTL_dialog.unread_mentions_count = paramTL_dialog.unread_mentions_count;
              localTL_dialog.read_inbox_max_id = paramTL_dialog.read_inbox_max_id;
              localTL_dialog.read_outbox_max_id = paramTL_dialog.read_outbox_max_id;
              localTL_dialog.pinned = paramTL_dialog.pinned;
              localTL_dialog.pinnedNum = paramTL_dialog.pinnedNum;
              long l = paramTL_dialog.id;
              localTL_dialog.id = l;
              localMessage.dialog_id = l;
              paramAnonymousTL_error.users.addAll(paramAnonymousTLObject.users);
              paramAnonymousTL_error.chats.addAll(paramAnonymousTLObject.chats);
              paramAnonymousTL_error.dialogs.add(localTL_dialog);
              paramAnonymousTL_error.messages.addAll(paramAnonymousTLObject.messages);
              paramAnonymousTL_error.count = 1;
              MessagesController.this.processDialogsUpdate(paramAnonymousTL_error, null);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(paramAnonymousTLObject.messages, true, true, false, DownloadController.getInstance(MessagesController.this.currentAccount).getAutodownloadMask(), true);
            }
            for (;;)
            {
              if (paramLong != 0L) {
                MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(paramLong);
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.checkingLastMessagesDialogs.delete(MessagesController.72.this.val$lower_id);
                }
              });
              return;
              label329:
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(MessagesController.72.this.val$dialog.id);
                  if ((localTL_dialog != null) && (localTL_dialog.top_message == 0)) {
                    MessagesController.this.deleteDialog(MessagesController.72.this.val$dialog.id, 3);
                  }
                }
              });
            }
          }
        });
        return;
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          paramInputPeer = localException1;
          Object localObject2 = localException2;
        }
      }
      localObject1 = paramInputPeer;
      break;
      FileLog.e(localException1);
    }
  }
  
  public void cleanup()
  {
    ContactsController.getInstance(this.currentAccount).cleanup();
    MediaController.getInstance().cleanup();
    NotificationsController.getInstance(this.currentAccount).cleanup();
    SendMessagesHelper.getInstance(this.currentAccount).cleanup();
    SecretChatHelper.getInstance(this.currentAccount).cleanup();
    LocationController.getInstance(this.currentAccount).cleanup();
    DataQuery.getInstance(this.currentAccount).cleanup();
    org.telegram.ui.DialogsActivity.dialogsLoaded[this.currentAccount] = false;
    this.reloadingWebpages.clear();
    this.reloadingWebpagesPending.clear();
    this.dialogs_dict.clear();
    this.dialogs_read_inbox_max.clear();
    this.dialogs_read_outbox_max.clear();
    this.exportedChats.clear();
    this.fullUsers.clear();
    this.dialogs.clear();
    this.joiningToChannels.clear();
    this.channelViewsToSend.clear();
    this.dialogsServerOnly.clear();
    this.dialogsForward.clear();
    this.dialogsGroupsOnly.clear();
    this.dialogMessagesByIds.clear();
    this.dialogMessagesByRandomIds.clear();
    this.channelAdmins.clear();
    this.loadingChannelAdmins.clear();
    this.users.clear();
    this.objectsByUsernames.clear();
    this.chats.clear();
    this.dialogMessage.clear();
    this.printingUsers.clear();
    this.printingStrings.clear();
    this.printingStringsTypes.clear();
    this.onlinePrivacy.clear();
    this.loadingPeerSettings.clear();
    this.lastPrintingStringCount = 0;
    this.nextDialogsCacheOffset = 0;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.this.readTasks.clear();
        MessagesController.this.readTasksMap.clear();
        MessagesController.this.updatesQueueSeq.clear();
        MessagesController.this.updatesQueuePts.clear();
        MessagesController.this.updatesQueueQts.clear();
        MessagesController.this.gettingUnknownChannels.clear();
        MessagesController.access$1202(MessagesController.this, 0L);
        MessagesController.access$1302(MessagesController.this, 0L);
        MessagesController.access$1402(MessagesController.this, 0L);
        MessagesController.this.createdDialogIds.clear();
        MessagesController.this.gettingDifference = false;
        MessagesController.access$1602(MessagesController.this, null);
        MessagesController.access$1702(MessagesController.this, null);
      }
    });
    this.createdDialogMainThreadIds.clear();
    this.blockedUsers.clear();
    this.sendingTypings.clear();
    this.loadingFullUsers.clear();
    this.loadedFullUsers.clear();
    this.reloadingMessages.clear();
    this.loadingFullChats.clear();
    this.loadingFullParticipants.clear();
    this.loadedFullParticipants.clear();
    this.loadedFullChats.clear();
    this.currentDeletingTaskTime = 0;
    this.currentDeletingTaskMids = null;
    this.currentDeletingTaskChannelId = 0;
    this.gettingNewDeleteTask = false;
    this.loadingDialogs = false;
    this.dialogsEndReached = false;
    this.serverDialogsEndReached = false;
    this.loadingBlockedUsers = false;
    this.firstGettingTask = false;
    this.updatingState = false;
    this.resetingDialogs = false;
    this.lastStatusUpdateTime = 0L;
    this.offlineSent = false;
    this.registeringForPush = false;
    this.getDifferenceFirstSync = true;
    this.uploadingAvatar = null;
    this.statusRequest = 0;
    this.statusSettingState = 0;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ConnectionsManager.getInstance(MessagesController.this.currentAccount).setIsUpdating(false);
        MessagesController.this.updatesQueueChannels.clear();
        MessagesController.this.updatesStartWaitTimeChannels.clear();
        MessagesController.this.gettingDifferenceChannels.clear();
        MessagesController.this.channelsPts.clear();
        MessagesController.this.shortPollChannels.clear();
        MessagesController.this.needShortPollChannels.clear();
      }
    });
    if (this.currentDeleteTaskRunnable != null)
    {
      Utilities.stageQueue.cancelRunnable(this.currentDeleteTaskRunnable);
      this.currentDeleteTaskRunnable = null;
    }
    addSupportUser();
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
  }
  
  protected void clearFullUsers()
  {
    this.loadedFullUsers.clear();
    this.loadedFullChats.clear();
  }
  
  protected void completeDialogsReset(final TLRPC.messages_Dialogs parammessages_Dialogs, int paramInt1, int paramInt2, final int paramInt3, final int paramInt4, final int paramInt5, final LongSparseArray<TLRPC.TL_dialog> paramLongSparseArray, final LongSparseArray<MessageObject> paramLongSparseArray1, TLRPC.Message paramMessage)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.this.gettingDifference = false;
        MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastPtsValue(paramInt3);
        MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastDateValue(paramInt4);
        MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastQtsValue(paramInt5);
        MessagesController.this.getDifference();
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.access$5802(MessagesController.this, false);
            MessagesController.this.applyDialogsNotificationsSettings(MessagesController.67.this.val$dialogsRes.dialogs);
            if (!UserConfig.getInstance(MessagesController.this.currentAccount).draftsLoaded) {
              DataQuery.getInstance(MessagesController.this.currentAccount).loadDrafts();
            }
            MessagesController.this.putUsers(MessagesController.67.this.val$dialogsRes.users, false);
            MessagesController.this.putChats(MessagesController.67.this.val$dialogsRes.chats, false);
            int i = 0;
            Object localObject;
            while (i < MessagesController.this.dialogs.size())
            {
              localObject = (TLRPC.TL_dialog)MessagesController.this.dialogs.get(i);
              if ((int)((TLRPC.TL_dialog)localObject).id != 0)
              {
                MessagesController.this.dialogs_dict.remove(((TLRPC.TL_dialog)localObject).id);
                MessageObject localMessageObject = (MessageObject)MessagesController.this.dialogMessage.get(((TLRPC.TL_dialog)localObject).id);
                MessagesController.this.dialogMessage.remove(((TLRPC.TL_dialog)localObject).id);
                if (localMessageObject != null)
                {
                  MessagesController.this.dialogMessagesByIds.remove(localMessageObject.getId());
                  if (localMessageObject.messageOwner.random_id != 0L) {
                    MessagesController.this.dialogMessagesByRandomIds.remove(localMessageObject.messageOwner.random_id);
                  }
                }
              }
              i += 1;
            }
            i = 0;
            while (i < MessagesController.67.this.val$new_dialogs_dict.size())
            {
              long l = MessagesController.67.this.val$new_dialogs_dict.keyAt(i);
              localObject = (TLRPC.TL_dialog)MessagesController.67.this.val$new_dialogs_dict.valueAt(i);
              if ((((TLRPC.TL_dialog)localObject).draft instanceof TLRPC.TL_draftMessage)) {
                DataQuery.getInstance(MessagesController.this.currentAccount).saveDraft(((TLRPC.TL_dialog)localObject).id, ((TLRPC.TL_dialog)localObject).draft, null, false);
              }
              MessagesController.this.dialogs_dict.put(l, localObject);
              localObject = (MessageObject)MessagesController.67.this.val$new_dialogMessage.get(((TLRPC.TL_dialog)localObject).id);
              MessagesController.this.dialogMessage.put(l, localObject);
              if ((localObject != null) && (((MessageObject)localObject).messageOwner.to_id.channel_id == 0))
              {
                MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject).getId(), localObject);
                if (((MessageObject)localObject).messageOwner.random_id != 0L) {
                  MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject).messageOwner.random_id, localObject);
                }
              }
              i += 1;
            }
            MessagesController.this.dialogs.clear();
            i = 0;
            int j = MessagesController.this.dialogs_dict.size();
            while (i < j)
            {
              MessagesController.this.dialogs.add(MessagesController.this.dialogs_dict.valueAt(i));
              i += 1;
            }
            MessagesController.this.sortDialogs(null);
            MessagesController.this.dialogsEndReached = true;
            MessagesController.this.serverDialogsEndReached = false;
            if ((UserConfig.getInstance(MessagesController.this.currentAccount).totalDialogsLoadCount < 400) && (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId != -1) && (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId != Integer.MAX_VALUE)) {
              MessagesController.this.loadDialogs(0, 100, false);
            }
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
          }
        });
      }
    });
  }
  
  public void convertToMegaGroup(final Context paramContext, final int paramInt)
  {
    TLRPC.TL_messages_migrateChat localTL_messages_migrateChat = new TLRPC.TL_messages_migrateChat();
    localTL_messages_migrateChat.chat_id = paramInt;
    final AlertDialog localAlertDialog = new AlertDialog(paramContext, 1);
    localAlertDialog.setMessage(LocaleController.getString("Loading", 2131493762));
    localAlertDialog.setCanceledOnTouchOutside(false);
    localAlertDialog.setCancelable(false);
    paramInt = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_migrateChat, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (!((Activity)MessagesController.89.this.val$context).isFinishing()) {}
              try
              {
                MessagesController.89.this.val$progressDialog.dismiss();
                return;
              }
              catch (Exception localException)
              {
                FileLog.e(localException);
              }
            }
          });
          paramAnonymousTL_error = (TLRPC.Updates)paramAnonymousTLObject;
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (!((Activity)MessagesController.89.this.val$context).isFinishing()) {}
            try
            {
              MessagesController.89.this.val$progressDialog.dismiss();
              AlertDialog.Builder localBuilder = new AlertDialog.Builder(MessagesController.89.this.val$context);
              localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
              localBuilder.setMessage(LocaleController.getString("ErrorOccurred", 2131493453));
              localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
              localBuilder.show().setCanceledOnTouchOutside(true);
              return;
            }
            catch (Exception localException)
            {
              for (;;)
              {
                FileLog.e(localException);
              }
            }
          }
        });
      }
    });
    localAlertDialog.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        ConnectionsManager.getInstance(MessagesController.this.currentAccount).cancelRequest(paramInt, true);
        try
        {
          paramAnonymousDialogInterface.dismiss();
          return;
        }
        catch (Exception paramAnonymousDialogInterface)
        {
          FileLog.e(paramAnonymousDialogInterface);
        }
      }
    });
    try
    {
      localAlertDialog.show();
      return;
    }
    catch (Exception paramContext) {}
  }
  
  public int createChat(String paramString1, final ArrayList<Integer> paramArrayList, final String paramString2, int paramInt, final BaseFragment paramBaseFragment)
  {
    if (paramInt == 1)
    {
      paramString2 = new TLRPC.TL_chat();
      paramString2.id = UserConfig.getInstance(this.currentAccount).lastBroadcastId;
      paramString2.title = paramString1;
      paramString2.photo = new TLRPC.TL_chatPhotoEmpty();
      paramString2.participants_count = paramArrayList.size();
      paramString2.date = ((int)(System.currentTimeMillis() / 1000L));
      paramString2.version = 1;
      paramString1 = UserConfig.getInstance(this.currentAccount);
      paramString1.lastBroadcastId -= 1;
      putChat(paramString2, false);
      paramString1 = new ArrayList();
      paramString1.add(paramString2);
      MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(null, paramString1, true, true);
      paramString1 = new TLRPC.TL_chatFull();
      paramString1.id = paramString2.id;
      paramString1.chat_photo = new TLRPC.TL_photoEmpty();
      paramString1.notify_settings = new TLRPC.TL_peerNotifySettingsEmpty();
      paramString1.exported_invite = new TLRPC.TL_chatInviteEmpty();
      paramString1.participants = new TLRPC.TL_chatParticipants();
      paramString1.participants.chat_id = paramString2.id;
      paramString1.participants.admin_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
      paramString1.participants.version = 1;
      paramInt = 0;
      while (paramInt < paramArrayList.size())
      {
        paramBaseFragment = new TLRPC.TL_chatParticipant();
        paramBaseFragment.user_id = ((Integer)paramArrayList.get(paramInt)).intValue();
        paramBaseFragment.inviter_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
        paramBaseFragment.date = ((int)(System.currentTimeMillis() / 1000L));
        paramString1.participants.participants.add(paramBaseFragment);
        paramInt += 1;
      }
      MessagesStorage.getInstance(this.currentAccount).updateChatInfo(paramString1, false);
      paramString1 = new TLRPC.TL_messageService();
      paramString1.action = new TLRPC.TL_messageActionCreatedBroadcastList();
      paramInt = UserConfig.getInstance(this.currentAccount).getNewMessageId();
      paramString1.id = paramInt;
      paramString1.local_id = paramInt;
      paramString1.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
      paramString1.dialog_id = AndroidUtilities.makeBroadcastId(paramString2.id);
      paramString1.to_id = new TLRPC.TL_peerChat();
      paramString1.to_id.chat_id = paramString2.id;
      paramString1.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      paramString1.random_id = 0L;
      paramString1.flags |= 0x100;
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
      paramBaseFragment = new MessageObject(this.currentAccount, paramString1, this.users, true);
      paramBaseFragment.messageOwner.send_state = 0;
      paramArrayList = new ArrayList();
      paramArrayList.add(paramBaseFragment);
      paramBaseFragment = new ArrayList();
      paramBaseFragment.add(paramString1);
      MessagesStorage.getInstance(this.currentAccount).putMessages(paramBaseFragment, false, true, false, 0);
      updateInterfaceWithMessages(paramString1.dialog_id, paramArrayList);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatDidCreated, new Object[] { Integer.valueOf(paramString2.id) });
      return 0;
    }
    if (paramInt == 0)
    {
      paramString2 = new TLRPC.TL_messages_createChat();
      paramString2.title = paramString1;
      paramInt = 0;
      if (paramInt < paramArrayList.size())
      {
        paramString1 = getUser((Integer)paramArrayList.get(paramInt));
        if (paramString1 == null) {}
        for (;;)
        {
          paramInt += 1;
          break;
          paramString2.users.add(getInputUser(paramString1));
        }
      }
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramString2, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error != null)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                AlertsCreator.processError(MessagesController.this.currentAccount, paramAnonymousTL_error, MessagesController.87.this.val$fragment, MessagesController.87.this.val$req, new Object[0]);
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
              }
            });
            return;
          }
          paramAnonymousTLObject = (TLRPC.Updates)paramAnonymousTLObject;
          MessagesController.this.processUpdates(paramAnonymousTLObject, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.putUsers(paramAnonymousTLObject.users, false);
              MessagesController.this.putChats(paramAnonymousTLObject.chats, false);
              if ((paramAnonymousTLObject.chats != null) && (!paramAnonymousTLObject.chats.isEmpty()))
              {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidCreated, new Object[] { Integer.valueOf(((TLRPC.Chat)paramAnonymousTLObject.chats.get(0)).id) });
                return;
              }
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
            }
          });
        }
      }, 2);
    }
    if ((paramInt == 2) || (paramInt == 4))
    {
      paramArrayList = new TLRPC.TL_channels_createChannel();
      paramArrayList.title = paramString1;
      paramArrayList.about = paramString2;
      if (paramInt == 4) {
        paramArrayList.megagroup = true;
      }
      for (;;)
      {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramArrayList, new RequestDelegate()
        {
          public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error != null)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  AlertsCreator.processError(MessagesController.this.currentAccount, paramAnonymousTL_error, MessagesController.88.this.val$fragment, MessagesController.88.this.val$req, new Object[0]);
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
                }
              });
              return;
            }
            paramAnonymousTLObject = (TLRPC.Updates)paramAnonymousTLObject;
            MessagesController.this.processUpdates(paramAnonymousTLObject, false);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.putUsers(paramAnonymousTLObject.users, false);
                MessagesController.this.putChats(paramAnonymousTLObject.chats, false);
                if ((paramAnonymousTLObject.chats != null) && (!paramAnonymousTLObject.chats.isEmpty()))
                {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidCreated, new Object[] { Integer.valueOf(((TLRPC.Chat)paramAnonymousTLObject.chats.get(0)).id) });
                  return;
                }
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatDidFailCreate, new Object[0]);
              }
            });
          }
        }, 2);
        paramArrayList.broadcast = true;
      }
    }
    return 0;
  }
  
  public void deleteDialog(long paramLong, int paramInt)
  {
    deleteDialog(paramLong, true, paramInt, 0);
  }
  
  public void deleteMessages(ArrayList<Integer> paramArrayList, ArrayList<Long> paramArrayList1, TLRPC.EncryptedChat paramEncryptedChat, int paramInt, boolean paramBoolean)
  {
    deleteMessages(paramArrayList, paramArrayList1, paramEncryptedChat, paramInt, paramBoolean, 0L, null);
  }
  
  public void deleteMessages(ArrayList<Integer> paramArrayList, ArrayList<Long> paramArrayList1, TLRPC.EncryptedChat paramEncryptedChat, final int paramInt, boolean paramBoolean, final long paramLong, TLObject paramTLObject)
  {
    if (((paramArrayList == null) || (paramArrayList.isEmpty())) && (paramTLObject == null)) {
      return;
    }
    Object localObject = null;
    if (paramLong == 0L)
    {
      if (paramInt == 0)
      {
        i = 0;
        while (i < paramArrayList.size())
        {
          localObject = (Integer)paramArrayList.get(i);
          localObject = (MessageObject)this.dialogMessagesByIds.get(((Integer)localObject).intValue());
          if (localObject != null) {
            ((MessageObject)localObject).deleted = true;
          }
          i += 1;
        }
      }
      markChannelDialogMessageAsDeleted(paramArrayList, paramInt);
      localObject = new ArrayList();
      int i = 0;
      while (i < paramArrayList.size())
      {
        Integer localInteger = (Integer)paramArrayList.get(i);
        if (localInteger.intValue() > 0) {
          ((ArrayList)localObject).add(localInteger);
        }
        i += 1;
      }
      MessagesStorage.getInstance(this.currentAccount).markMessagesAsDeleted(paramArrayList, true, paramInt);
      MessagesStorage.getInstance(this.currentAccount).updateDialogsWithDeletedMessages(paramArrayList, null, true, paramInt);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.messagesDeleted, new Object[] { paramArrayList, Integer.valueOf(paramInt) });
    }
    if (paramInt != 0)
    {
      if (paramTLObject != null)
      {
        paramArrayList = (TLRPC.TL_channels_deleteMessages)paramTLObject;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramArrayList, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error == null)
            {
              paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
              MessagesController.this.processNewChannelDifferenceParams(paramAnonymousTLObject.pts, paramAnonymousTLObject.pts_count, paramInt);
            }
            if (paramLong != 0L) {
              MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(paramLong);
            }
          }
        });
        return;
      }
      paramEncryptedChat = new TLRPC.TL_channels_deleteMessages();
      paramEncryptedChat.id = ((ArrayList)localObject);
      paramEncryptedChat.channel = getInputChannel(paramInt);
      paramTLObject = null;
      for (;;)
      {
        try
        {
          paramArrayList = new NativeByteBuffer(paramEncryptedChat.getObjectSize() + 8);
        }
        catch (Exception paramArrayList1)
        {
          try
          {
            paramArrayList.writeInt32(7);
            paramArrayList.writeInt32(paramInt);
            paramEncryptedChat.serializeToStream(paramArrayList);
            paramLong = MessagesStorage.getInstance(this.currentAccount).createPendingTask(paramArrayList);
            paramArrayList = paramEncryptedChat;
          }
          catch (Exception paramArrayList1)
          {
            for (;;) {}
          }
          paramArrayList1 = paramArrayList1;
          paramArrayList = paramTLObject;
        }
        FileLog.e(paramArrayList1);
      }
    }
    if ((paramArrayList1 != null) && (paramEncryptedChat != null) && (!paramArrayList1.isEmpty())) {
      SecretChatHelper.getInstance(this.currentAccount).sendMessagesDeleteMessage(paramEncryptedChat, paramArrayList1, null);
    }
    if (paramTLObject != null) {
      paramArrayList = (TLRPC.TL_messages_deleteMessages)paramTLObject;
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramArrayList, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
            MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
          }
          if (paramLong != 0L) {
            MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(paramLong);
          }
        }
      });
      return;
      paramEncryptedChat = new TLRPC.TL_messages_deleteMessages();
      paramEncryptedChat.id = ((ArrayList)localObject);
      paramEncryptedChat.revoke = paramBoolean;
      paramTLObject = null;
      try
      {
        paramArrayList = new NativeByteBuffer(paramEncryptedChat.getObjectSize() + 8);
      }
      catch (Exception paramArrayList1)
      {
        for (;;)
        {
          try
          {
            paramArrayList.writeInt32(7);
            paramArrayList.writeInt32(paramInt);
            paramEncryptedChat.serializeToStream(paramArrayList);
            paramLong = MessagesStorage.getInstance(this.currentAccount).createPendingTask(paramArrayList);
            paramArrayList = paramEncryptedChat;
          }
          catch (Exception paramArrayList1)
          {
            continue;
          }
          paramArrayList1 = paramArrayList1;
          paramArrayList = paramTLObject;
          FileLog.e(paramArrayList1);
        }
      }
    }
  }
  
  public void deleteUserChannelHistory(final TLRPC.Chat paramChat, final TLRPC.User paramUser, int paramInt)
  {
    if (paramInt == 0) {
      MessagesStorage.getInstance(this.currentAccount).deleteUserChannelHistory(paramChat.id, paramUser.id);
    }
    TLRPC.TL_channels_deleteUserHistory localTL_channels_deleteUserHistory = new TLRPC.TL_channels_deleteUserHistory();
    localTL_channels_deleteUserHistory.channel = getInputChannel(paramChat);
    localTL_channels_deleteUserHistory.user_id = getInputUser(paramUser);
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_deleteUserHistory, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_messages_affectedHistory)paramAnonymousTLObject;
          if (paramAnonymousTLObject.offset > 0) {
            MessagesController.this.deleteUserChannelHistory(paramChat, paramUser, paramAnonymousTLObject.offset);
          }
          MessagesController.this.processNewChannelDifferenceParams(paramAnonymousTLObject.pts, paramAnonymousTLObject.pts_count, paramChat.id);
        }
      }
    });
  }
  
  public void deleteUserFromChat(int paramInt, TLRPC.User paramUser, TLRPC.ChatFull paramChatFull)
  {
    deleteUserFromChat(paramInt, paramUser, paramChatFull, false);
  }
  
  public void deleteUserFromChat(final int paramInt, final TLRPC.User paramUser, TLRPC.ChatFull paramChatFull, boolean paramBoolean)
  {
    if (paramUser == null) {}
    do
    {
      return;
      if (paramInt > 0)
      {
        localObject1 = getInputUser(paramUser);
        localObject2 = getChat(Integer.valueOf(paramInt));
        final boolean bool = ChatObject.isChannel((TLRPC.Chat)localObject2);
        if (bool) {
          if ((localObject1 instanceof TLRPC.TL_inputUserSelf)) {
            if ((((TLRPC.Chat)localObject2).creator) && (paramBoolean))
            {
              paramChatFull = new TLRPC.TL_channels_deleteChannel();
              paramChatFull.channel = getInputChannel((TLRPC.Chat)localObject2);
            }
          }
        }
        for (;;)
        {
          ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramChatFull, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramUser.id == UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.this.deleteDialog(-MessagesController.101.this.val$chat_id, 0);
                  }
                });
              }
              if (paramAnonymousTL_error != null) {}
              do
              {
                return;
                paramAnonymousTLObject = (TLRPC.Updates)paramAnonymousTLObject;
                MessagesController.this.processUpdates(paramAnonymousTLObject, false);
              } while ((!bool) || ((localObject1 instanceof TLRPC.TL_inputUserSelf)));
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.loadFullChat(MessagesController.101.this.val$chat_id, 0, true);
                }
              }, 1000L);
            }
          }, 64);
          return;
          paramChatFull = new TLRPC.TL_channels_leaveChannel();
          paramChatFull.channel = getInputChannel((TLRPC.Chat)localObject2);
          continue;
          paramChatFull = new TLRPC.TL_channels_editBanned();
          paramChatFull.channel = getInputChannel((TLRPC.Chat)localObject2);
          paramChatFull.user_id = ((TLRPC.InputUser)localObject1);
          paramChatFull.banned_rights = new TLRPC.TL_channelBannedRights();
          paramChatFull.banned_rights.view_messages = true;
          paramChatFull.banned_rights.send_media = true;
          paramChatFull.banned_rights.send_messages = true;
          paramChatFull.banned_rights.send_stickers = true;
          paramChatFull.banned_rights.send_gifs = true;
          paramChatFull.banned_rights.send_games = true;
          paramChatFull.banned_rights.send_inline = true;
          paramChatFull.banned_rights.embed_links = true;
          continue;
          paramChatFull = new TLRPC.TL_messages_deleteChatUser();
          paramChatFull.chat_id = paramInt;
          paramChatFull.user_id = getInputUser(paramUser);
        }
      }
    } while (!(paramChatFull instanceof TLRPC.TL_chatFull));
    final Object localObject1 = getChat(Integer.valueOf(paramInt));
    ((TLRPC.Chat)localObject1).participants_count -= 1;
    Object localObject2 = new ArrayList();
    ((ArrayList)localObject2).add(localObject1);
    MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(null, (ArrayList)localObject2, true, true);
    int j = 0;
    paramInt = 0;
    for (;;)
    {
      int i = j;
      if (paramInt < paramChatFull.participants.participants.size())
      {
        if (((TLRPC.ChatParticipant)paramChatFull.participants.participants.get(paramInt)).user_id == paramUser.id)
        {
          paramChatFull.participants.participants.remove(paramInt);
          i = 1;
        }
      }
      else
      {
        if (i != 0)
        {
          MessagesStorage.getInstance(this.currentAccount).updateChatInfo(paramChatFull, true);
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { paramChatFull, Integer.valueOf(0), Boolean.valueOf(false), null });
        }
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(32) });
        return;
      }
      paramInt += 1;
    }
  }
  
  public void deleteUserPhoto(TLRPC.InputPhoto paramInputPhoto)
  {
    if (paramInputPhoto == null)
    {
      TLRPC.TL_photos_updateProfilePhoto localTL_photos_updateProfilePhoto = new TLRPC.TL_photos_updateProfilePhoto();
      localTL_photos_updateProfilePhoto.id = new TLRPC.TL_inputPhotoEmpty();
      UserConfig.getInstance(this.currentAccount).getCurrentUser().photo = new TLRPC.TL_userProfilePhotoEmpty();
      localObject = getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
      paramInputPhoto = (TLRPC.InputPhoto)localObject;
      if (localObject == null) {
        paramInputPhoto = UserConfig.getInstance(this.currentAccount).getCurrentUser();
      }
      if (paramInputPhoto == null) {
        return;
      }
      paramInputPhoto.photo = UserConfig.getInstance(this.currentAccount).getCurrentUser().photo;
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1535) });
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_photos_updateProfilePhoto, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTL_error = MessagesController.this.getUser(Integer.valueOf(UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()));
            if (paramAnonymousTL_error != null) {
              break label61;
            }
            paramAnonymousTL_error = UserConfig.getInstance(MessagesController.this.currentAccount).getCurrentUser();
            MessagesController.this.putUser(paramAnonymousTL_error, false);
          }
          while (paramAnonymousTL_error == null)
          {
            return;
            label61:
            UserConfig.getInstance(MessagesController.this.currentAccount).setCurrentUser(paramAnonymousTL_error);
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).clearUserPhotos(paramAnonymousTL_error.id);
          ArrayList localArrayList = new ArrayList();
          localArrayList.add(paramAnonymousTL_error);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localArrayList, null, false, true);
          paramAnonymousTL_error.photo = ((TLRPC.UserProfilePhoto)paramAnonymousTLObject);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1535) });
              UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(true);
            }
          });
        }
      });
      return;
    }
    Object localObject = new TLRPC.TL_photos_deletePhotos();
    ((TLRPC.TL_photos_deletePhotos)localObject).id.add(paramInputPhoto);
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void didAddedNewTask(final int paramInt, final SparseArray<ArrayList<Long>> paramSparseArray)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (((MessagesController.this.currentDeletingTaskMids == null) && (!MessagesController.this.gettingNewDeleteTask)) || ((MessagesController.this.currentDeletingTaskTime != 0) && (paramInt < MessagesController.this.currentDeletingTaskTime))) {
          MessagesController.this.getNewDeleteTask(null, 0);
        }
      }
    });
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.didCreatedNewDeleteTask, new Object[] { paramSparseArray });
      }
    });
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    Object localObject;
    if (paramInt1 == NotificationCenter.FileDidUpload)
    {
      localObject = (String)paramVarArgs[0];
      paramVarArgs = (TLRPC.InputFile)paramVarArgs[1];
      if ((this.uploadingAvatar != null) && (this.uploadingAvatar.equals(localObject)))
      {
        localObject = new TLRPC.TL_photos_uploadProfilePhoto();
        ((TLRPC.TL_photos_uploadProfilePhoto)localObject).file = paramVarArgs;
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error == null)
            {
              paramAnonymousTL_error = MessagesController.this.getUser(Integer.valueOf(UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()));
              if (paramAnonymousTL_error != null) {
                break label61;
              }
              paramAnonymousTL_error = UserConfig.getInstance(MessagesController.this.currentAccount).getCurrentUser();
              MessagesController.this.putUser(paramAnonymousTL_error, true);
            }
            while (paramAnonymousTL_error == null)
            {
              return;
              label61:
              UserConfig.getInstance(MessagesController.this.currentAccount).setCurrentUser(paramAnonymousTL_error);
            }
            paramAnonymousTLObject = (TLRPC.TL_photos_photo)paramAnonymousTLObject;
            Object localObject = paramAnonymousTLObject.photo.sizes;
            TLRPC.PhotoSize localPhotoSize = FileLoader.getClosestPhotoSizeWithSize((ArrayList)localObject, 100);
            localObject = FileLoader.getClosestPhotoSizeWithSize((ArrayList)localObject, 1000);
            paramAnonymousTL_error.photo = new TLRPC.TL_userProfilePhoto();
            paramAnonymousTL_error.photo.photo_id = paramAnonymousTLObject.photo.id;
            if (localPhotoSize != null) {
              paramAnonymousTL_error.photo.photo_small = localPhotoSize.location;
            }
            if (localObject != null) {
              paramAnonymousTL_error.photo.photo_big = ((TLRPC.PhotoSize)localObject).location;
            }
            for (;;)
            {
              MessagesStorage.getInstance(MessagesController.this.currentAccount).clearUserPhotos(paramAnonymousTL_error.id);
              paramAnonymousTLObject = new ArrayList();
              paramAnonymousTLObject.add(paramAnonymousTL_error);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(paramAnonymousTLObject, null, false, true);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(2) });
                  UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(true);
                }
              });
              return;
              if (localPhotoSize != null) {
                paramAnonymousTL_error.photo.photo_small = localPhotoSize.location;
              }
            }
          }
        });
      }
    }
    do
    {
      do
      {
        do
        {
          Integer localInteger;
          do
          {
            do
            {
              return;
              if (paramInt1 != NotificationCenter.FileDidFailUpload) {
                break;
              }
              paramVarArgs = (String)paramVarArgs[0];
            } while ((this.uploadingAvatar == null) || (!this.uploadingAvatar.equals(paramVarArgs)));
            this.uploadingAvatar = null;
            return;
            if (paramInt1 != NotificationCenter.messageReceivedByServer) {
              break;
            }
            localObject = (Integer)paramVarArgs[0];
            localInteger = (Integer)paramVarArgs[1];
            paramVarArgs = (Long)paramVarArgs[3];
            MessageObject localMessageObject = (MessageObject)this.dialogMessage.get(paramVarArgs.longValue());
            if ((localMessageObject != null) && ((localMessageObject.getId() == ((Integer)localObject).intValue()) || (localMessageObject.messageOwner.local_id == ((Integer)localObject).intValue())))
            {
              localMessageObject.messageOwner.id = localInteger.intValue();
              localMessageObject.messageOwner.send_state = 0;
            }
            paramVarArgs = (TLRPC.TL_dialog)this.dialogs_dict.get(paramVarArgs.longValue());
            if ((paramVarArgs != null) && (paramVarArgs.top_message == ((Integer)localObject).intValue()))
            {
              paramVarArgs.top_message = localInteger.intValue();
              NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
            paramVarArgs = (MessageObject)this.dialogMessagesByIds.get(((Integer)localObject).intValue());
            this.dialogMessagesByIds.remove(((Integer)localObject).intValue());
          } while (paramVarArgs == null);
          this.dialogMessagesByIds.put(localInteger.intValue(), paramVarArgs);
          return;
        } while (paramInt1 != NotificationCenter.updateMessageMedia);
        paramVarArgs = (TLRPC.Message)paramVarArgs[0];
        localObject = (MessageObject)this.dialogMessagesByIds.get(paramVarArgs.id);
      } while (localObject == null);
      ((MessageObject)localObject).messageOwner.media = paramVarArgs.media;
    } while ((paramVarArgs.media.ttl_seconds == 0) || ((!(paramVarArgs.media.photo instanceof TLRPC.TL_photoEmpty)) && (!(paramVarArgs.media.document instanceof TLRPC.TL_documentEmpty))));
    ((MessageObject)localObject).setType();
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
  }
  
  public void forceResetDialogs()
  {
    resetDialogs(true, MessagesStorage.getInstance(this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(this.currentAccount).getLastQtsValue());
  }
  
  public void generateJoinMessage(final int paramInt, boolean paramBoolean)
  {
    final Object localObject = getChat(Integer.valueOf(paramInt));
    if ((localObject == null) || (!ChatObject.isChannel(paramInt, this.currentAccount)) || (((((TLRPC.Chat)localObject).left) || (((TLRPC.Chat)localObject).kicked)) && (!paramBoolean))) {
      return;
    }
    TLRPC.TL_messageService localTL_messageService = new TLRPC.TL_messageService();
    localTL_messageService.flags = 256;
    int i = UserConfig.getInstance(this.currentAccount).getNewMessageId();
    localTL_messageService.id = i;
    localTL_messageService.local_id = i;
    localTL_messageService.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    localTL_messageService.from_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
    localTL_messageService.to_id = new TLRPC.TL_peerChannel();
    localTL_messageService.to_id.channel_id = paramInt;
    localTL_messageService.dialog_id = (-paramInt);
    localTL_messageService.post = true;
    localTL_messageService.action = new TLRPC.TL_messageActionChatAddUser();
    localTL_messageService.action.users.add(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
    if (((TLRPC.Chat)localObject).megagroup) {
      localTL_messageService.flags |= 0x80000000;
    }
    UserConfig.getInstance(this.currentAccount).saveConfig(false);
    localObject = new ArrayList();
    ArrayList localArrayList = new ArrayList();
    localArrayList.add(localTL_messageService);
    ((ArrayList)localObject).add(new MessageObject(this.currentAccount, localTL_messageService, true));
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationsController.getInstance(MessagesController.this.currentAccount).processNewMessages(MessagesController.119.this.val$pushMessages, true, false);
          }
        });
      }
    });
    MessagesStorage.getInstance(this.currentAccount).putMessages(localArrayList, true, true, false, 0);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.this.updateInterfaceWithMessages(-paramInt, localObject);
        NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
      }
    });
  }
  
  public void generateUpdateMessage()
  {
    if ((BuildVars.DEBUG_VERSION) || (SharedConfig.lastUpdateVersion == null) || (SharedConfig.lastUpdateVersion.equals(BuildVars.BUILD_VERSION_STRING))) {
      return;
    }
    TLRPC.TL_help_getAppChangelog localTL_help_getAppChangelog = new TLRPC.TL_help_getAppChangelog();
    localTL_help_getAppChangelog.prev_app_version = SharedConfig.lastUpdateVersion;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_help_getAppChangelog, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          SharedConfig.lastUpdateVersion = BuildVars.BUILD_VERSION_STRING;
          SharedConfig.saveConfig();
        }
        if ((paramAnonymousTLObject instanceof TLRPC.Updates)) {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
        }
      }
    });
  }
  
  public void getBlockedUsers(boolean paramBoolean)
  {
    if ((!UserConfig.getInstance(this.currentAccount).isClientActivated()) || (this.loadingBlockedUsers)) {
      return;
    }
    this.loadingBlockedUsers = true;
    if (paramBoolean)
    {
      MessagesStorage.getInstance(this.currentAccount).getBlockedUsers();
      return;
    }
    TLRPC.TL_contacts_getBlocked localTL_contacts_getBlocked = new TLRPC.TL_contacts_getBlocked();
    localTL_contacts_getBlocked.offset = 0;
    localTL_contacts_getBlocked.limit = 200;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_getBlocked, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        ArrayList localArrayList2 = new ArrayList();
        ArrayList localArrayList1 = null;
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.contacts_Blocked)paramAnonymousTLObject;
          paramAnonymousTL_error = paramAnonymousTLObject.blocked.iterator();
          while (paramAnonymousTL_error.hasNext()) {
            localArrayList2.add(Integer.valueOf(((TLRPC.TL_contactBlocked)paramAnonymousTL_error.next()).user_id));
          }
          localArrayList1 = paramAnonymousTLObject.users;
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(paramAnonymousTLObject.users, null, true, true);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putBlockedUsers(localArrayList2, true);
        }
        MessagesController.this.processLoadedBlockedUsers(localArrayList2, localArrayList1, false);
      }
    });
  }
  
  protected void getChannelDifference(final int paramInt1, final int paramInt2, final long paramLong, TLRPC.InputChannel paramInputChannel)
  {
    if (this.gettingDifferenceChannels.get(paramInt1)) {}
    do
    {
      return;
      k = 100;
      if (paramInt2 != 1) {
        break;
      }
    } while (this.channelsPts.get(paramInt1) != 0);
    int j = 1;
    int k = 1;
    Object localObject1;
    int i;
    do
    {
      localObject1 = paramInputChannel;
      if (paramInputChannel == null)
      {
        localObject1 = getChat(Integer.valueOf(paramInt1));
        paramInputChannel = (TLRPC.InputChannel)localObject1;
        if (localObject1 == null)
        {
          localObject1 = MessagesStorage.getInstance(this.currentAccount).getChatSync(paramInt1);
          paramInputChannel = (TLRPC.InputChannel)localObject1;
          if (localObject1 != null)
          {
            putChat((TLRPC.Chat)localObject1, true);
            paramInputChannel = (TLRPC.InputChannel)localObject1;
          }
        }
        localObject1 = getInputChannel(paramInputChannel);
      }
      if ((localObject1 != null) && (((TLRPC.InputChannel)localObject1).access_hash != 0L)) {
        break label219;
      }
      if (paramLong == 0L) {
        break;
      }
      MessagesStorage.getInstance(this.currentAccount).removePendingTask(paramLong);
      return;
      j = this.channelsPts.get(paramInt1);
      i = j;
      if (j == 0)
      {
        j = MessagesStorage.getInstance(this.currentAccount).getChannelPtsSync(paramInt1);
        if (j != 0) {
          this.channelsPts.put(paramInt1, j);
        }
        i = j;
        if (j == 0)
        {
          if ((paramInt2 == 2) || (paramInt2 == 3)) {
            break;
          }
          i = j;
        }
      }
      j = i;
    } while (i != 0);
    return;
    label219:
    Object localObject2;
    if (paramLong == 0L) {
      localObject2 = null;
    }
    for (;;)
    {
      try
      {
        paramInputChannel = new NativeByteBuffer(((TLRPC.InputChannel)localObject1).getObjectSize() + 12);
        FileLog.e(localException1);
      }
      catch (Exception localException1)
      {
        try
        {
          paramInputChannel.writeInt32(6);
          paramInputChannel.writeInt32(paramInt1);
          paramInputChannel.writeInt32(paramInt2);
          ((TLRPC.InputChannel)localObject1).serializeToStream(paramInputChannel);
          paramLong = MessagesStorage.getInstance(this.currentAccount).createPendingTask(paramInputChannel);
          this.gettingDifferenceChannels.put(paramInt1, true);
          paramInputChannel = new TLRPC.TL_updates_getChannelDifference();
          paramInputChannel.channel = ((TLRPC.InputChannel)localObject1);
          paramInputChannel.filter = new TLRPC.TL_channelMessagesFilterEmpty();
          paramInputChannel.pts = j;
          paramInputChannel.limit = k;
          if (paramInt2 == 3) {
            break label432;
          }
          bool = true;
          paramInputChannel.force = bool;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("start getChannelDifference with pts = " + j + " channelId = " + paramInt1);
          }
          ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramInputChannel, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTL_error == null)
              {
                final TLRPC.updates_ChannelDifference localupdates_ChannelDifference = (TLRPC.updates_ChannelDifference)paramAnonymousTLObject;
                final SparseArray localSparseArray = new SparseArray();
                int i = 0;
                while (i < localupdates_ChannelDifference.users.size())
                {
                  paramAnonymousTLObject = (TLRPC.User)localupdates_ChannelDifference.users.get(i);
                  localSparseArray.put(paramAnonymousTLObject.id, paramAnonymousTLObject);
                  i += 1;
                }
                paramAnonymousTL_error = null;
                i = 0;
                for (;;)
                {
                  paramAnonymousTLObject = paramAnonymousTL_error;
                  if (i < localupdates_ChannelDifference.chats.size())
                  {
                    paramAnonymousTLObject = (TLRPC.Chat)localupdates_ChannelDifference.chats.get(i);
                    if (paramAnonymousTLObject.id != paramInt1) {}
                  }
                  else
                  {
                    paramAnonymousTL_error = new ArrayList();
                    if (localupdates_ChannelDifference.other_updates.isEmpty()) {
                      break;
                    }
                    int j;
                    for (i = 0; i < localupdates_ChannelDifference.other_updates.size(); i = j + 1)
                    {
                      TLRPC.Update localUpdate = (TLRPC.Update)localupdates_ChannelDifference.other_updates.get(i);
                      j = i;
                      if ((localUpdate instanceof TLRPC.TL_updateMessageID))
                      {
                        paramAnonymousTL_error.add((TLRPC.TL_updateMessageID)localUpdate);
                        localupdates_ChannelDifference.other_updates.remove(i);
                        j = i - 1;
                      }
                    }
                  }
                  i += 1;
                }
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localupdates_ChannelDifference.users, localupdates_ChannelDifference.chats, true, true);
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.this.putUsers(localupdates_ChannelDifference.users, false);
                    MessagesController.this.putChats(localupdates_ChannelDifference.chats, false);
                  }
                });
                MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                {
                  public void run()
                  {
                    if (!paramAnonymousTL_error.isEmpty())
                    {
                      final SparseArray localSparseArray = new SparseArray();
                      Iterator localIterator = paramAnonymousTL_error.iterator();
                      while (localIterator.hasNext())
                      {
                        TLRPC.TL_updateMessageID localTL_updateMessageID = (TLRPC.TL_updateMessageID)localIterator.next();
                        long[] arrayOfLong = MessagesStorage.getInstance(MessagesController.this.currentAccount).updateMessageStateAndId(localTL_updateMessageID.random_id, null, localTL_updateMessageID.id, 0, false, MessagesController.115.this.val$channelId);
                        if (arrayOfLong != null) {
                          localSparseArray.put(localTL_updateMessageID.id, arrayOfLong);
                        }
                      }
                      if (localSparseArray.size() != 0) {
                        AndroidUtilities.runOnUIThread(new Runnable()
                        {
                          public void run()
                          {
                            int i = 0;
                            while (i < localSparseArray.size())
                            {
                              int j = localSparseArray.keyAt(i);
                              long[] arrayOfLong = (long[])localSparseArray.valueAt(i);
                              int k = (int)arrayOfLong[1];
                              SendMessagesHelper.getInstance(MessagesController.this.currentAccount).processSentMessage(k);
                              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(k), Integer.valueOf(j), null, Long.valueOf(arrayOfLong[0]) });
                              i += 1;
                            }
                          }
                        });
                      }
                    }
                    Utilities.stageQueue.postRunnable(new Runnable()
                    {
                      public void run()
                      {
                        long l1;
                        Object localObject2;
                        Object localObject1;
                        Object localObject3;
                        int i;
                        Object localObject4;
                        label325:
                        boolean bool;
                        if (((MessagesController.115.2.this.val$res instanceof TLRPC.TL_updates_channelDifference)) || ((MessagesController.115.2.this.val$res instanceof TLRPC.TL_updates_channelDifferenceEmpty)))
                        {
                          if (!MessagesController.115.2.this.val$res.new_messages.isEmpty())
                          {
                            final LongSparseArray localLongSparseArray = new LongSparseArray();
                            ImageLoader.saveMessagesThumbs(MessagesController.115.2.this.val$res.new_messages);
                            final ArrayList localArrayList = new ArrayList();
                            l1 = -MessagesController.115.this.val$channelId;
                            localObject2 = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(l1));
                            localObject1 = localObject2;
                            if (localObject2 == null)
                            {
                              localObject1 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(false, l1));
                              MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(l1), localObject1);
                            }
                            localObject3 = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(l1));
                            localObject2 = localObject3;
                            if (localObject3 == null)
                            {
                              localObject2 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(true, l1));
                              MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(l1), localObject2);
                            }
                            i = 0;
                            if (i < MessagesController.115.2.this.val$res.new_messages.size())
                            {
                              localObject4 = (TLRPC.Message)MessagesController.115.2.this.val$res.new_messages.get(i);
                              if ((MessagesController.115.2.this.val$channelFinal == null) || (!MessagesController.115.2.this.val$channelFinal.left)) {
                                if (((TLRPC.Message)localObject4).out)
                                {
                                  localObject3 = localObject2;
                                  if ((((Integer)localObject3).intValue() >= ((TLRPC.Message)localObject4).id) || ((((TLRPC.Message)localObject4).action instanceof TLRPC.TL_messageActionChannelCreate))) {
                                    break label545;
                                  }
                                }
                              }
                              label545:
                              for (bool = true;; bool = false)
                              {
                                ((TLRPC.Message)localObject4).unread = bool;
                                if ((MessagesController.115.2.this.val$channelFinal != null) && (MessagesController.115.2.this.val$channelFinal.megagroup)) {
                                  ((TLRPC.Message)localObject4).flags |= 0x80000000;
                                }
                                MessageObject localMessageObject = new MessageObject(MessagesController.this.currentAccount, (TLRPC.Message)localObject4, MessagesController.115.2.this.val$usersDict, MessagesController.this.createdDialogIds.contains(Long.valueOf(l1)));
                                if ((!localMessageObject.isOut()) && (localMessageObject.isUnread())) {
                                  localArrayList.add(localMessageObject);
                                }
                                long l2 = -MessagesController.115.this.val$channelId;
                                localObject4 = (ArrayList)localLongSparseArray.get(l2);
                                localObject3 = localObject4;
                                if (localObject4 == null)
                                {
                                  localObject3 = new ArrayList();
                                  localLongSparseArray.put(l2, localObject3);
                                }
                                ((ArrayList)localObject3).add(localMessageObject);
                                i += 1;
                                break;
                                localObject3 = localObject1;
                                break label325;
                              }
                            }
                            AndroidUtilities.runOnUIThread(new Runnable()
                            {
                              public void run()
                              {
                                int i = 0;
                                while (i < localLongSparseArray.size())
                                {
                                  long l = localLongSparseArray.keyAt(i);
                                  ArrayList localArrayList = (ArrayList)localLongSparseArray.valueAt(i);
                                  MessagesController.this.updateInterfaceWithMessages(l, localArrayList);
                                  i += 1;
                                }
                                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                              }
                            });
                            MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                            {
                              public void run()
                              {
                                if (!localArrayList.isEmpty()) {
                                  AndroidUtilities.runOnUIThread(new Runnable()
                                  {
                                    public void run()
                                    {
                                      NotificationsController.getInstance(MessagesController.this.currentAccount).processNewMessages(MessagesController.115.2.2.2.this.val$pushMessages, true, false);
                                    }
                                  });
                                }
                                MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(MessagesController.115.2.this.val$res.new_messages, true, false, false, DownloadController.getInstance(MessagesController.this.currentAccount).getAutodownloadMask());
                              }
                            });
                          }
                          if (!MessagesController.115.2.this.val$res.other_updates.isEmpty()) {
                            MessagesController.this.processUpdateArray(MessagesController.115.2.this.val$res.other_updates, MessagesController.115.2.this.val$res.users, MessagesController.115.2.this.val$res.chats, true);
                          }
                          MessagesController.this.processChannelsUpdatesQueue(MessagesController.115.this.val$channelId, 1);
                          MessagesStorage.getInstance(MessagesController.this.currentAccount).saveChannelPts(MessagesController.115.this.val$channelId, MessagesController.115.2.this.val$res.pts);
                        }
                        for (;;)
                        {
                          MessagesController.this.gettingDifferenceChannels.delete(MessagesController.115.this.val$channelId);
                          MessagesController.this.channelsPts.put(MessagesController.115.this.val$channelId, MessagesController.115.2.this.val$res.pts);
                          if ((MessagesController.115.2.this.val$res.flags & 0x2) != 0) {
                            MessagesController.this.shortPollChannels.put(MessagesController.115.this.val$channelId, (int)(System.currentTimeMillis() / 1000L) + MessagesController.115.2.this.val$res.timeout);
                          }
                          if (!MessagesController.115.2.this.val$res.isFinal) {
                            MessagesController.this.getChannelDifference(MessagesController.115.this.val$channelId);
                          }
                          if (BuildVars.LOGS_ENABLED)
                          {
                            FileLog.d("received channel difference with pts = " + MessagesController.115.2.this.val$res.pts + " channelId = " + MessagesController.115.this.val$channelId);
                            FileLog.d("new_messages = " + MessagesController.115.2.this.val$res.new_messages.size() + " messages = " + MessagesController.115.2.this.val$res.messages.size() + " users = " + MessagesController.115.2.this.val$res.users.size() + " chats = " + MessagesController.115.2.this.val$res.chats.size() + " other updates = " + MessagesController.115.2.this.val$res.other_updates.size());
                          }
                          if (MessagesController.115.this.val$newTaskId != 0L) {
                            MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(MessagesController.115.this.val$newTaskId);
                          }
                          return;
                          if ((MessagesController.115.2.this.val$res instanceof TLRPC.TL_updates_channelDifferenceTooLong))
                          {
                            l1 = -MessagesController.115.this.val$channelId;
                            localObject2 = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(l1));
                            localObject1 = localObject2;
                            if (localObject2 == null)
                            {
                              localObject1 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(false, l1));
                              MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(l1), localObject1);
                            }
                            localObject3 = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(l1));
                            localObject2 = localObject3;
                            if (localObject3 == null)
                            {
                              localObject2 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(true, l1));
                              MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(l1), localObject2);
                            }
                            i = 0;
                            if (i < MessagesController.115.2.this.val$res.messages.size())
                            {
                              localObject4 = (TLRPC.Message)MessagesController.115.2.this.val$res.messages.get(i);
                              ((TLRPC.Message)localObject4).dialog_id = (-MessagesController.115.this.val$channelId);
                              if ((!(((TLRPC.Message)localObject4).action instanceof TLRPC.TL_messageActionChannelCreate)) && ((MessagesController.115.2.this.val$channelFinal == null) || (!MessagesController.115.2.this.val$channelFinal.left))) {
                                if (((TLRPC.Message)localObject4).out)
                                {
                                  localObject3 = localObject2;
                                  label1396:
                                  if (((Integer)localObject3).intValue() >= ((TLRPC.Message)localObject4).id) {
                                    break label1469;
                                  }
                                }
                              }
                              label1469:
                              for (bool = true;; bool = false)
                              {
                                ((TLRPC.Message)localObject4).unread = bool;
                                if ((MessagesController.115.2.this.val$channelFinal != null) && (MessagesController.115.2.this.val$channelFinal.megagroup)) {
                                  ((TLRPC.Message)localObject4).flags |= 0x80000000;
                                }
                                i += 1;
                                break;
                                localObject3 = localObject1;
                                break label1396;
                              }
                            }
                            MessagesStorage.getInstance(MessagesController.this.currentAccount).overwriteChannel(MessagesController.115.this.val$channelId, (TLRPC.TL_updates_channelDifferenceTooLong)MessagesController.115.2.this.val$res, MessagesController.115.this.val$newDialogType);
                          }
                        }
                      }
                    });
                  }
                });
              }
              do
              {
                return;
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    MessagesController.this.checkChannelError(paramAnonymousTL_error.text, MessagesController.115.this.val$channelId);
                  }
                });
                MessagesController.this.gettingDifferenceChannels.delete(paramInt1);
              } while (paramLong == 0L);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(paramLong);
            }
          });
          return;
        }
        catch (Exception localException2)
        {
          boolean bool;
          for (;;) {}
        }
        localException1 = localException1;
        paramInputChannel = (TLRPC.InputChannel)localObject2;
      }
      continue;
      continue;
      label432:
      bool = false;
    }
  }
  
  public TLRPC.Chat getChat(Integer paramInteger)
  {
    return (TLRPC.Chat)this.chats.get(paramInteger);
  }
  
  public void getDifference()
  {
    getDifference(MessagesStorage.getInstance(this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(this.currentAccount).getLastQtsValue(), false);
  }
  
  public void getDifference(int paramInt1, final int paramInt2, final int paramInt3, boolean paramBoolean)
  {
    registerForPush(SharedConfig.pushString);
    if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() == 0) {
      loadCurrentState();
    }
    while ((!paramBoolean) && (this.gettingDifference)) {
      return;
    }
    this.gettingDifference = true;
    TLRPC.TL_updates_getDifference localTL_updates_getDifference = new TLRPC.TL_updates_getDifference();
    localTL_updates_getDifference.pts = paramInt1;
    localTL_updates_getDifference.date = paramInt2;
    localTL_updates_getDifference.qts = paramInt3;
    if (this.getDifferenceFirstSync)
    {
      localTL_updates_getDifference.flags |= 0x1;
      if (!ConnectionsManager.isConnectedOrConnectingToWiFi()) {
        break label214;
      }
    }
    label214:
    for (localTL_updates_getDifference.pts_total_limit = 5000;; localTL_updates_getDifference.pts_total_limit = 1000)
    {
      this.getDifferenceFirstSync = false;
      if (localTL_updates_getDifference.date == 0) {
        localTL_updates_getDifference.date = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("start getDifference with date = " + paramInt2 + " pts = " + paramInt1 + " qts = " + paramInt3);
      }
      ConnectionsManager.getInstance(this.currentAccount).setIsUpdating(true);
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_updates_getDifference, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.updates_Difference)paramAnonymousTLObject;
            if ((paramAnonymousTLObject instanceof TLRPC.TL_updates_differenceTooLong))
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.loadedFullUsers.clear();
                  MessagesController.this.loadedFullChats.clear();
                  MessagesController.this.resetDialogs(true, MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastSeqValue(), paramAnonymousTLObject.pts, MessagesController.116.this.val$date, MessagesController.116.this.val$qts);
                }
              });
              return;
            }
            if ((paramAnonymousTLObject instanceof TLRPC.TL_updates_differenceSlice)) {
              MessagesController.this.getDifference(paramAnonymousTLObject.intermediate_state.pts, paramAnonymousTLObject.intermediate_state.date, paramAnonymousTLObject.intermediate_state.qts, true);
            }
            paramAnonymousTL_error = new SparseArray();
            final SparseArray localSparseArray = new SparseArray();
            int i = 0;
            while (i < paramAnonymousTLObject.users.size())
            {
              localObject = (TLRPC.User)paramAnonymousTLObject.users.get(i);
              paramAnonymousTL_error.put(((TLRPC.User)localObject).id, localObject);
              i += 1;
            }
            i = 0;
            while (i < paramAnonymousTLObject.chats.size())
            {
              localObject = (TLRPC.Chat)paramAnonymousTLObject.chats.get(i);
              localSparseArray.put(((TLRPC.Chat)localObject).id, localObject);
              i += 1;
            }
            final Object localObject = new ArrayList();
            if (!paramAnonymousTLObject.other_updates.isEmpty())
            {
              i = 0;
              if (i < paramAnonymousTLObject.other_updates.size())
              {
                TLRPC.Update localUpdate = (TLRPC.Update)paramAnonymousTLObject.other_updates.get(i);
                int j;
                if ((localUpdate instanceof TLRPC.TL_updateMessageID))
                {
                  ((ArrayList)localObject).add((TLRPC.TL_updateMessageID)localUpdate);
                  paramAnonymousTLObject.other_updates.remove(i);
                  j = i - 1;
                }
                for (;;)
                {
                  i = j + 1;
                  break;
                  j = i;
                  if (MessagesController.this.getUpdateType(localUpdate) == 2)
                  {
                    int m = MessagesController.getUpdateChannelId(localUpdate);
                    j = MessagesController.this.channelsPts.get(m);
                    int k = j;
                    if (j == 0)
                    {
                      j = MessagesStorage.getInstance(MessagesController.this.currentAccount).getChannelPtsSync(m);
                      k = j;
                      if (j != 0)
                      {
                        MessagesController.this.channelsPts.put(m, j);
                        k = j;
                      }
                    }
                    j = i;
                    if (k != 0)
                    {
                      j = i;
                      if (MessagesController.getUpdatePts(localUpdate) <= k)
                      {
                        paramAnonymousTLObject.other_updates.remove(i);
                        j = i - 1;
                      }
                    }
                  }
                }
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.loadedFullUsers.clear();
                MessagesController.this.loadedFullChats.clear();
                MessagesController.this.putUsers(paramAnonymousTLObject.users, false);
                MessagesController.this.putChats(paramAnonymousTLObject.chats, false);
              }
            });
            MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
            {
              public void run()
              {
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(paramAnonymousTLObject.users, paramAnonymousTLObject.chats, true, false);
                if (!localObject.isEmpty())
                {
                  final SparseArray localSparseArray = new SparseArray();
                  int i = 0;
                  while (i < localObject.size())
                  {
                    TLRPC.TL_updateMessageID localTL_updateMessageID = (TLRPC.TL_updateMessageID)localObject.get(i);
                    long[] arrayOfLong = MessagesStorage.getInstance(MessagesController.this.currentAccount).updateMessageStateAndId(localTL_updateMessageID.random_id, null, localTL_updateMessageID.id, 0, false, 0);
                    if (arrayOfLong != null) {
                      localSparseArray.put(localTL_updateMessageID.id, arrayOfLong);
                    }
                    i += 1;
                  }
                  if (localSparseArray.size() != 0) {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        int i = 0;
                        while (i < localSparseArray.size())
                        {
                          int j = localSparseArray.keyAt(i);
                          long[] arrayOfLong = (long[])localSparseArray.valueAt(i);
                          int k = (int)arrayOfLong[1];
                          SendMessagesHelper.getInstance(MessagesController.this.currentAccount).processSentMessage(k);
                          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messageReceivedByServer, new Object[] { Integer.valueOf(k), Integer.valueOf(j), null, Long.valueOf(arrayOfLong[0]) });
                          i += 1;
                        }
                      }
                    });
                  }
                }
                Utilities.stageQueue.postRunnable(new Runnable()
                {
                  public void run()
                  {
                    int i;
                    if ((!MessagesController.116.3.this.val$res.new_messages.isEmpty()) || (!MessagesController.116.3.this.val$res.new_encrypted_messages.isEmpty()))
                    {
                      final LongSparseArray localLongSparseArray = new LongSparseArray();
                      i = 0;
                      Object localObject1;
                      while (i < MessagesController.116.3.this.val$res.new_encrypted_messages.size())
                      {
                        localObject1 = (TLRPC.EncryptedMessage)MessagesController.116.3.this.val$res.new_encrypted_messages.get(i);
                        localObject1 = SecretChatHelper.getInstance(MessagesController.this.currentAccount).decryptMessage((TLRPC.EncryptedMessage)localObject1);
                        if ((localObject1 != null) && (!((ArrayList)localObject1).isEmpty())) {
                          MessagesController.116.3.this.val$res.new_messages.addAll((Collection)localObject1);
                        }
                        i += 1;
                      }
                      ImageLoader.saveMessagesThumbs(MessagesController.116.3.this.val$res.new_messages);
                      final ArrayList localArrayList = new ArrayList();
                      int j = UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId();
                      i = 0;
                      if (i < MessagesController.116.3.this.val$res.new_messages.size())
                      {
                        TLRPC.Message localMessage = (TLRPC.Message)MessagesController.116.3.this.val$res.new_messages.get(i);
                        if (localMessage.dialog_id == 0L) {
                          if (localMessage.to_id.chat_id == 0) {
                            break label544;
                          }
                        }
                        Object localObject3;
                        Object localObject2;
                        for (localMessage.dialog_id = (-localMessage.to_id.chat_id);; localMessage.dialog_id = localMessage.to_id.user_id)
                        {
                          if ((int)localMessage.dialog_id != 0)
                          {
                            if ((localMessage.action instanceof TLRPC.TL_messageActionChatDeleteUser))
                            {
                              localObject1 = (TLRPC.User)MessagesController.116.3.this.val$usersDict.get(localMessage.action.user_id);
                              if ((localObject1 != null) && (((TLRPC.User)localObject1).bot))
                              {
                                localMessage.reply_markup = new TLRPC.TL_replyKeyboardHide();
                                localMessage.flags |= 0x40;
                              }
                            }
                            if ((!(localMessage.action instanceof TLRPC.TL_messageActionChatMigrateTo)) && (!(localMessage.action instanceof TLRPC.TL_messageActionChannelCreate))) {
                              break label604;
                            }
                            localMessage.unread = false;
                            localMessage.media_unread = false;
                          }
                          if (localMessage.dialog_id == j)
                          {
                            localMessage.unread = false;
                            localMessage.media_unread = false;
                            localMessage.out = true;
                          }
                          localObject3 = new MessageObject(MessagesController.this.currentAccount, localMessage, MessagesController.116.3.this.val$usersDict, MessagesController.116.3.this.val$chatsDict, MessagesController.this.createdDialogIds.contains(Long.valueOf(localMessage.dialog_id)));
                          if ((!((MessageObject)localObject3).isOut()) && (((MessageObject)localObject3).isUnread())) {
                            localArrayList.add(localObject3);
                          }
                          localObject2 = (ArrayList)localLongSparseArray.get(localMessage.dialog_id);
                          localObject1 = localObject2;
                          if (localObject2 == null)
                          {
                            localObject1 = new ArrayList();
                            localLongSparseArray.put(localMessage.dialog_id, localObject1);
                          }
                          ((ArrayList)localObject1).add(localObject3);
                          i += 1;
                          break;
                          label544:
                          if (localMessage.to_id.user_id == UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()) {
                            localMessage.to_id.user_id = localMessage.from_id;
                          }
                        }
                        label604:
                        if (localMessage.out)
                        {
                          localObject1 = MessagesController.this.dialogs_read_outbox_max;
                          label627:
                          localObject3 = (Integer)((ConcurrentHashMap)localObject1).get(Long.valueOf(localMessage.dialog_id));
                          localObject2 = localObject3;
                          if (localObject3 == null)
                          {
                            localObject2 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(localMessage.out, localMessage.dialog_id));
                            ((ConcurrentHashMap)localObject1).put(Long.valueOf(localMessage.dialog_id), localObject2);
                          }
                          if (((Integer)localObject2).intValue() >= localMessage.id) {
                            break label746;
                          }
                        }
                        label746:
                        for (boolean bool = true;; bool = false)
                        {
                          localMessage.unread = bool;
                          break;
                          localObject1 = MessagesController.this.dialogs_read_inbox_max;
                          break label627;
                        }
                      }
                      AndroidUtilities.runOnUIThread(new Runnable()
                      {
                        public void run()
                        {
                          int i = 0;
                          while (i < localLongSparseArray.size())
                          {
                            long l = localLongSparseArray.keyAt(i);
                            ArrayList localArrayList = (ArrayList)localLongSparseArray.valueAt(i);
                            MessagesController.this.updateInterfaceWithMessages(l, localArrayList);
                            i += 1;
                          }
                          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                        }
                      });
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                      {
                        public void run()
                        {
                          if (!localArrayList.isEmpty()) {
                            AndroidUtilities.runOnUIThread(new Runnable()
                            {
                              public void run()
                              {
                                NotificationsController localNotificationsController = NotificationsController.getInstance(MessagesController.this.currentAccount);
                                ArrayList localArrayList = MessagesController.116.3.2.2.this.val$pushMessages;
                                if (!(MessagesController.116.3.this.val$res instanceof TLRPC.TL_updates_differenceSlice)) {}
                                for (boolean bool = true;; bool = false)
                                {
                                  localNotificationsController.processNewMessages(localArrayList, bool, false);
                                  return;
                                }
                              }
                            });
                          }
                          MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(MessagesController.116.3.this.val$res.new_messages, true, false, false, DownloadController.getInstance(MessagesController.this.currentAccount).getAutodownloadMask());
                        }
                      });
                      SecretChatHelper.getInstance(MessagesController.this.currentAccount).processPendingEncMessages();
                    }
                    if (!MessagesController.116.3.this.val$res.other_updates.isEmpty()) {
                      MessagesController.this.processUpdateArray(MessagesController.116.3.this.val$res.other_updates, MessagesController.116.3.this.val$res.users, MessagesController.116.3.this.val$res.chats, true);
                    }
                    if ((MessagesController.116.3.this.val$res instanceof TLRPC.TL_updates_difference))
                    {
                      MessagesController.this.gettingDifference = false;
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastSeqValue(MessagesController.116.3.this.val$res.state.seq);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastDateValue(MessagesController.116.3.this.val$res.state.date);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastPtsValue(MessagesController.116.3.this.val$res.state.pts);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastQtsValue(MessagesController.116.3.this.val$res.state.qts);
                      ConnectionsManager.getInstance(MessagesController.this.currentAccount).setIsUpdating(false);
                      i = 0;
                      while (i < 3)
                      {
                        MessagesController.this.processUpdatesQueue(i, 1);
                        i += 1;
                      }
                    }
                    if ((MessagesController.116.3.this.val$res instanceof TLRPC.TL_updates_differenceSlice))
                    {
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastDateValue(MessagesController.116.3.this.val$res.intermediate_state.date);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastPtsValue(MessagesController.116.3.this.val$res.intermediate_state.pts);
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastQtsValue(MessagesController.116.3.this.val$res.intermediate_state.qts);
                    }
                    for (;;)
                    {
                      MessagesStorage.getInstance(MessagesController.this.currentAccount).saveDiffParams(MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastQtsValue());
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("received difference with date = " + MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastDateValue() + " pts = " + MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastPtsValue() + " seq = " + MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastSeqValue() + " messages = " + MessagesController.116.3.this.val$res.new_messages.size() + " users = " + MessagesController.116.3.this.val$res.users.size() + " chats = " + MessagesController.116.3.this.val$res.chats.size() + " other updates = " + MessagesController.116.3.this.val$res.other_updates.size());
                      }
                      return;
                      if ((MessagesController.116.3.this.val$res instanceof TLRPC.TL_updates_differenceEmpty))
                      {
                        MessagesController.this.gettingDifference = false;
                        MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastSeqValue(MessagesController.116.3.this.val$res.seq);
                        MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastDateValue(MessagesController.116.3.this.val$res.date);
                        ConnectionsManager.getInstance(MessagesController.this.currentAccount).setIsUpdating(false);
                        i = 0;
                        while (i < 3)
                        {
                          MessagesController.this.processUpdatesQueue(i, 1);
                          i += 1;
                        }
                      }
                    }
                  }
                });
              }
            });
            return;
          }
          MessagesController.this.gettingDifference = false;
          ConnectionsManager.getInstance(MessagesController.this.currentAccount).setIsUpdating(false);
        }
      });
      return;
    }
  }
  
  public TLRPC.EncryptedChat getEncryptedChat(Integer paramInteger)
  {
    return (TLRPC.EncryptedChat)this.encryptedChats.get(paramInteger);
  }
  
  public TLRPC.EncryptedChat getEncryptedChatDB(int paramInt, boolean paramBoolean)
  {
    Object localObject2 = (TLRPC.EncryptedChat)this.encryptedChats.get(Integer.valueOf(paramInt));
    if (localObject2 != null)
    {
      localObject1 = localObject2;
      if (!paramBoolean) {
        break label130;
      }
      if (!(localObject2 instanceof TLRPC.TL_encryptedChatWaiting))
      {
        localObject1 = localObject2;
        if (!(localObject2 instanceof TLRPC.TL_encryptedChatRequested)) {
          break label130;
        }
      }
    }
    Object localObject1 = new CountDownLatch(1);
    ArrayList localArrayList = new ArrayList();
    MessagesStorage.getInstance(this.currentAccount).getEncryptedChat(paramInt, (CountDownLatch)localObject1, localArrayList);
    try
    {
      ((CountDownLatch)localObject1).await();
      localObject1 = localObject2;
      if (localArrayList.size() == 2)
      {
        localObject1 = (TLRPC.EncryptedChat)localArrayList.get(0);
        localObject2 = (TLRPC.User)localArrayList.get(1);
        putEncryptedChat((TLRPC.EncryptedChat)localObject1, false);
        putUser((TLRPC.User)localObject2, true);
      }
      label130:
      return (TLRPC.EncryptedChat)localObject1;
    }
    catch (Exception localException)
    {
      for (;;)
      {
        FileLog.e(localException);
      }
    }
  }
  
  public TLRPC.ExportedChatInvite getExportedInvite(int paramInt)
  {
    return (TLRPC.ExportedChatInvite)this.exportedChats.get(paramInt);
  }
  
  public TLRPC.InputChannel getInputChannel(int paramInt)
  {
    return getInputChannel(getChat(Integer.valueOf(paramInt)));
  }
  
  public TLRPC.InputPeer getInputPeer(int paramInt)
  {
    Object localObject2;
    Object localObject1;
    if (paramInt < 0)
    {
      localObject2 = getChat(Integer.valueOf(-paramInt));
      if (ChatObject.isChannel((TLRPC.Chat)localObject2))
      {
        localObject1 = new TLRPC.TL_inputPeerChannel();
        ((TLRPC.InputPeer)localObject1).channel_id = (-paramInt);
        ((TLRPC.InputPeer)localObject1).access_hash = ((TLRPC.Chat)localObject2).access_hash;
      }
    }
    TLRPC.User localUser;
    do
    {
      return (TLRPC.InputPeer)localObject1;
      localObject1 = new TLRPC.TL_inputPeerChat();
      ((TLRPC.InputPeer)localObject1).chat_id = (-paramInt);
      return (TLRPC.InputPeer)localObject1;
      localUser = getUser(Integer.valueOf(paramInt));
      localObject2 = new TLRPC.TL_inputPeerUser();
      ((TLRPC.InputPeer)localObject2).user_id = paramInt;
      localObject1 = localObject2;
    } while (localUser == null);
    ((TLRPC.InputPeer)localObject2).access_hash = localUser.access_hash;
    return (TLRPC.InputPeer)localObject2;
  }
  
  public TLRPC.InputUser getInputUser(int paramInt)
  {
    return getInputUser(getInstance(UserConfig.selectedAccount).getUser(Integer.valueOf(paramInt)));
  }
  
  public TLRPC.InputUser getInputUser(TLRPC.User paramUser)
  {
    if (paramUser == null) {
      return new TLRPC.TL_inputUserEmpty();
    }
    if (paramUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
      return new TLRPC.TL_inputUserSelf();
    }
    TLRPC.TL_inputUser localTL_inputUser = new TLRPC.TL_inputUser();
    localTL_inputUser.user_id = paramUser.id;
    localTL_inputUser.access_hash = paramUser.access_hash;
    return localTL_inputUser;
  }
  
  public void getNewDeleteTask(final ArrayList<Integer> paramArrayList, final int paramInt)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.access$4002(MessagesController.this, true);
        MessagesStorage.getInstance(MessagesController.this.currentAccount).getNewTask(paramArrayList, paramInt);
      }
    });
  }
  
  public TLRPC.Peer getPeer(int paramInt)
  {
    if (paramInt < 0)
    {
      localObject = getChat(Integer.valueOf(-paramInt));
      if (((localObject instanceof TLRPC.TL_channel)) || ((localObject instanceof TLRPC.TL_channelForbidden)))
      {
        localObject = new TLRPC.TL_peerChannel();
        ((TLRPC.Peer)localObject).channel_id = (-paramInt);
        return (TLRPC.Peer)localObject;
      }
      localObject = new TLRPC.TL_peerChat();
      ((TLRPC.Peer)localObject).chat_id = (-paramInt);
      return (TLRPC.Peer)localObject;
    }
    getUser(Integer.valueOf(paramInt));
    Object localObject = new TLRPC.TL_peerUser();
    ((TLRPC.Peer)localObject).user_id = paramInt;
    return (TLRPC.Peer)localObject;
  }
  
  public long getUpdatesStartTime(int paramInt)
  {
    if (paramInt == 0) {
      return this.updatesStartWaitTimeSeq;
    }
    if (paramInt == 1) {
      return this.updatesStartWaitTimePts;
    }
    if (paramInt == 2) {
      return this.updatesStartWaitTimeQts;
    }
    return 0L;
  }
  
  public TLRPC.User getUser(Integer paramInteger)
  {
    return (TLRPC.User)this.users.get(paramInteger);
  }
  
  public TLRPC.TL_userFull getUserFull(int paramInt)
  {
    return (TLRPC.TL_userFull)this.fullUsers.get(paramInt);
  }
  
  public TLObject getUserOrChat(String paramString)
  {
    if ((paramString == null) || (paramString.length() == 0)) {
      return null;
    }
    return (TLObject)this.objectsByUsernames.get(paramString.toLowerCase());
  }
  
  public ConcurrentHashMap<Integer, TLRPC.User> getUsers()
  {
    return this.users;
  }
  
  public void hideReportSpam(long paramLong, TLRPC.User paramUser, TLRPC.Chat paramChat)
  {
    if ((paramUser == null) && (paramChat == null)) {}
    do
    {
      return;
      localObject = this.notificationsPreferences.edit();
      ((SharedPreferences.Editor)localObject).putInt("spam3_" + paramLong, 1);
      ((SharedPreferences.Editor)localObject).commit();
    } while ((int)paramLong == 0);
    Object localObject = new TLRPC.TL_messages_hideReportSpam();
    if (paramUser != null) {
      ((TLRPC.TL_messages_hideReportSpam)localObject).peer = getInputPeer(paramUser.id);
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      });
      return;
      if (paramChat != null) {
        ((TLRPC.TL_messages_hideReportSpam)localObject).peer = getInputPeer(-paramChat.id);
      }
    }
  }
  
  public boolean isChannelAdmin(int paramInt1, int paramInt2)
  {
    ArrayList localArrayList = (ArrayList)this.channelAdmins.get(paramInt1);
    return (localArrayList != null) && (localArrayList.indexOf(Integer.valueOf(paramInt2)) >= 0);
  }
  
  public boolean isDialogCreated(long paramLong)
  {
    return this.createdDialogMainThreadIds.contains(Long.valueOf(paramLong));
  }
  
  public boolean isDialogMuted(long paramLong)
  {
    int i = this.notificationsPreferences.getInt("notify2_" + paramLong, 0);
    if (i == 2) {}
    while ((i == 3) && (this.notificationsPreferences.getInt("notifyuntil_" + paramLong, 0) >= ConnectionsManager.getInstance(this.currentAccount).getCurrentTime())) {
      return true;
    }
    return false;
  }
  
  public void loadChannelAdmins(final int paramInt, boolean paramBoolean)
  {
    if (this.loadingChannelAdmins.indexOfKey(paramInt) >= 0) {
      return;
    }
    this.loadingChannelAdmins.put(paramInt, 0);
    if (paramBoolean)
    {
      MessagesStorage.getInstance(this.currentAccount).loadChannelAdmins(paramInt);
      return;
    }
    TLRPC.TL_channels_getParticipants localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
    ArrayList localArrayList = (ArrayList)this.channelAdmins.get(paramInt);
    if (localArrayList != null)
    {
      long l = 0L;
      int i = 0;
      while (i < localArrayList.size())
      {
        l = (20261L * l + 2147483648L + ((Integer)localArrayList.get(i)).intValue()) % 2147483648L;
        i += 1;
      }
      localTL_channels_getParticipants.hash = ((int)l);
    }
    localTL_channels_getParticipants.channel = getInputChannel(paramInt);
    localTL_channels_getParticipants.limit = 100;
    localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsAdmins();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if ((paramAnonymousTLObject instanceof TLRPC.TL_channels_channelParticipants))
        {
          paramAnonymousTLObject = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
          paramAnonymousTL_error = new ArrayList(paramAnonymousTLObject.participants.size());
          int i = 0;
          while (i < paramAnonymousTLObject.participants.size())
          {
            paramAnonymousTL_error.add(Integer.valueOf(((TLRPC.ChannelParticipant)paramAnonymousTLObject.participants.get(i)).user_id));
            i += 1;
          }
          MessagesController.this.processLoadedChannelAdmins(paramAnonymousTL_error, paramInt, false);
        }
      }
    });
  }
  
  public void loadChannelParticipants(final Integer paramInteger)
  {
    if ((this.loadingFullParticipants.contains(paramInteger)) || (this.loadedFullParticipants.contains(paramInteger))) {
      return;
    }
    this.loadingFullParticipants.add(paramInteger);
    TLRPC.TL_channels_getParticipants localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
    localTL_channels_getParticipants.channel = getInputChannel(paramInteger.intValue());
    localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsRecent();
    localTL_channels_getParticipants.offset = 0;
    localTL_channels_getParticipants.limit = 32;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (paramAnonymousTL_error == null)
            {
              TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
              MessagesController.this.putUsers(localTL_channels_channelParticipants.users, false);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localTL_channels_channelParticipants.users, null, true, true);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).updateChannelUsers(MessagesController.49.this.val$chat_id.intValue(), localTL_channels_channelParticipants.participants);
              MessagesController.this.loadedFullParticipants.add(MessagesController.49.this.val$chat_id);
            }
            MessagesController.this.loadingFullParticipants.remove(MessagesController.49.this.val$chat_id);
          }
        });
      }
    });
  }
  
  public void loadChatInfo(int paramInt, CountDownLatch paramCountDownLatch, boolean paramBoolean)
  {
    MessagesStorage.getInstance(this.currentAccount).loadChatInfo(paramInt, paramCountDownLatch, paramBoolean, false);
  }
  
  public void loadCurrentState()
  {
    if (this.updatingState) {
      return;
    }
    this.updatingState = true;
    TLRPC.TL_updates_getState localTL_updates_getState = new TLRPC.TL_updates_getState();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_updates_getState, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        MessagesController.this.updatingState = false;
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_updates_state)paramAnonymousTLObject;
          MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastDateValue(paramAnonymousTLObject.date);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastPtsValue(paramAnonymousTLObject.pts);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastSeqValue(paramAnonymousTLObject.seq);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).setLastQtsValue(paramAnonymousTLObject.qts);
          i = 0;
          while (i < 3)
          {
            MessagesController.this.processUpdatesQueue(i, 2);
            i += 1;
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).saveDiffParams(MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(MessagesController.this.currentAccount).getLastQtsValue());
        }
        while (paramAnonymousTL_error.code == 401)
        {
          int i;
          return;
        }
        MessagesController.this.loadCurrentState();
      }
    });
  }
  
  public void loadDialogPhotos(final int paramInt1, final int paramInt2, final long paramLong, boolean paramBoolean, int paramInt3)
  {
    if (paramBoolean) {
      MessagesStorage.getInstance(this.currentAccount).getDialogPhotos(paramInt1, paramInt2, paramLong, paramInt3);
    }
    do
    {
      do
      {
        return;
        if (paramInt1 <= 0) {
          break;
        }
        localObject = getUser(Integer.valueOf(paramInt1));
      } while (localObject == null);
      TLRPC.TL_photos_getUserPhotos localTL_photos_getUserPhotos = new TLRPC.TL_photos_getUserPhotos();
      localTL_photos_getUserPhotos.limit = paramInt2;
      localTL_photos_getUserPhotos.offset = 0;
      localTL_photos_getUserPhotos.max_id = ((int)paramLong);
      localTL_photos_getUserPhotos.user_id = getInputUser((TLRPC.User)localObject);
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_photos_getUserPhotos, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.photos_Photos)paramAnonymousTLObject;
            MessagesController.this.processLoadedUserPhotos(paramAnonymousTLObject, paramInt1, paramInt2, paramLong, false, this.val$classGuid);
          }
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt3);
      return;
    } while (paramInt1 >= 0);
    Object localObject = new TLRPC.TL_messages_search();
    ((TLRPC.TL_messages_search)localObject).filter = new TLRPC.TL_inputMessagesFilterChatPhotos();
    ((TLRPC.TL_messages_search)localObject).limit = paramInt2;
    ((TLRPC.TL_messages_search)localObject).offset_id = ((int)paramLong);
    ((TLRPC.TL_messages_search)localObject).q = "";
    ((TLRPC.TL_messages_search)localObject).peer = getInputPeer(paramInt1);
    paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
          paramAnonymousTL_error = new TLRPC.TL_photos_photos();
          paramAnonymousTL_error.count = paramAnonymousTLObject.count;
          paramAnonymousTL_error.users.addAll(paramAnonymousTLObject.users);
          int i = 0;
          if (i < paramAnonymousTLObject.messages.size())
          {
            TLRPC.Message localMessage = (TLRPC.Message)paramAnonymousTLObject.messages.get(i);
            if ((localMessage.action == null) || (localMessage.action.photo == null)) {}
            for (;;)
            {
              i += 1;
              break;
              paramAnonymousTL_error.photos.add(localMessage.action.photo);
            }
          }
          MessagesController.this.processLoadedUserPhotos(paramAnonymousTL_error, paramInt1, paramInt2, paramLong, false, this.val$classGuid);
        }
      }
    });
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt3);
  }
  
  public void loadDialogs(int paramInt1, final int paramInt2, boolean paramBoolean)
  {
    if ((this.loadingDialogs) || (this.resetingDialogs)) {
      return;
    }
    this.loadingDialogs = true;
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("load cacheOffset = " + paramInt1 + " count = " + paramInt2 + " cache = " + paramBoolean);
    }
    if (paramBoolean)
    {
      localObject1 = MessagesStorage.getInstance(this.currentAccount);
      if (paramInt1 == 0) {}
      for (paramInt1 = 0;; paramInt1 = this.nextDialogsCacheOffset)
      {
        ((MessagesStorage)localObject1).getDialogs(paramInt1, paramInt2);
        return;
      }
    }
    Object localObject1 = new TLRPC.TL_messages_getDialogs();
    ((TLRPC.TL_messages_getDialogs)localObject1).limit = paramInt2;
    ((TLRPC.TL_messages_getDialogs)localObject1).exclude_pinned = true;
    if (UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetId != -1)
    {
      if (UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetId == Integer.MAX_VALUE)
      {
        this.dialogsEndReached = true;
        this.serverDialogsEndReached = true;
        this.loadingDialogs = false;
        NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        return;
      }
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_id = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetId;
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_date = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetDate;
      if (((TLRPC.TL_messages_getDialogs)localObject1).offset_id == 0)
      {
        ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerEmpty();
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            if (paramAnonymousTL_error == null)
            {
              paramAnonymousTLObject = (TLRPC.messages_Dialogs)paramAnonymousTLObject;
              MessagesController.this.processLoadedDialogs(paramAnonymousTLObject, null, 0, paramInt2, 0, false, false, false);
            }
          }
        });
        return;
      }
      if (UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetChannelId != 0)
      {
        ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerChannel();
        ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer.channel_id = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetChannelId;
      }
      for (;;)
      {
        ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer.access_hash = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetAccess;
        break;
        if (UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetUserId != 0)
        {
          ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerUser();
          ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer.user_id = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetUserId;
        }
        else
        {
          ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerChat();
          ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer.chat_id = UserConfig.getInstance(this.currentAccount).dialogsLoadOffsetChatId;
        }
      }
    }
    int j = 0;
    paramInt1 = this.dialogs.size() - 1;
    int i = j;
    Object localObject2;
    if (paramInt1 >= 0)
    {
      localObject2 = (TLRPC.TL_dialog)this.dialogs.get(paramInt1);
      if (((TLRPC.TL_dialog)localObject2).pinned) {}
      do
      {
        int k;
        do
        {
          paramInt1 -= 1;
          break;
          i = (int)((TLRPC.TL_dialog)localObject2).id;
          k = (int)(((TLRPC.TL_dialog)localObject2).id >> 32);
        } while ((i == 0) || (k == 1) || (((TLRPC.TL_dialog)localObject2).top_message <= 0));
        localObject2 = (MessageObject)this.dialogMessage.get(((TLRPC.TL_dialog)localObject2).id);
      } while ((localObject2 == null) || (((MessageObject)localObject2).getId() <= 0));
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_date = ((MessageObject)localObject2).messageOwner.date;
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_id = ((MessageObject)localObject2).messageOwner.id;
      if (((MessageObject)localObject2).messageOwner.to_id.channel_id == 0) {
        break label624;
      }
      paramInt1 = -((MessageObject)localObject2).messageOwner.to_id.channel_id;
    }
    for (;;)
    {
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = getInputPeer(paramInt1);
      i = 1;
      if (i != 0) {
        break;
      }
      ((TLRPC.TL_messages_getDialogs)localObject1).offset_peer = new TLRPC.TL_inputPeerEmpty();
      break;
      label624:
      if (((MessageObject)localObject2).messageOwner.to_id.chat_id != 0) {
        paramInt1 = -((MessageObject)localObject2).messageOwner.to_id.chat_id;
      } else {
        paramInt1 = ((MessageObject)localObject2).messageOwner.to_id.user_id;
      }
    }
  }
  
  public void loadFullChat(int paramInt1, final int paramInt2, boolean paramBoolean)
  {
    boolean bool = this.loadedFullChats.contains(Integer.valueOf(paramInt1));
    if ((this.loadingFullChats.contains(Integer.valueOf(paramInt1))) || ((!paramBoolean) && (bool))) {
      return;
    }
    this.loadingFullChats.add(Integer.valueOf(paramInt1));
    final long l = -paramInt1;
    final TLRPC.Chat localChat = getChat(Integer.valueOf(paramInt1));
    Object localObject2;
    Object localObject1;
    if (ChatObject.isChannel(localChat))
    {
      localObject2 = new TLRPC.TL_channels_getFullChannel();
      ((TLRPC.TL_channels_getFullChannel)localObject2).channel = getInputChannel(localChat);
      localObject1 = localObject2;
      if (localChat.megagroup)
      {
        if (bool) {
          break label164;
        }
        paramBoolean = true;
        label110:
        loadChannelAdmins(paramInt1, paramBoolean);
        localObject1 = localObject2;
      }
    }
    for (;;)
    {
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            final TLRPC.TL_messages_chatFull localTL_messages_chatFull = (TLRPC.TL_messages_chatFull)paramAnonymousTLObject;
            MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localTL_messages_chatFull.users, localTL_messages_chatFull.chats, true, true);
            MessagesStorage.getInstance(MessagesController.this.currentAccount).updateChatInfo(localTL_messages_chatFull.full_chat, false);
            if (ChatObject.isChannel(localChat))
            {
              paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(l));
              paramAnonymousTLObject = paramAnonymousTL_error;
              if (paramAnonymousTL_error == null) {
                paramAnonymousTLObject = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(false, l));
              }
              MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(l), Integer.valueOf(Math.max(localTL_messages_chatFull.full_chat.read_inbox_max_id, paramAnonymousTLObject.intValue())));
              if (paramAnonymousTLObject.intValue() == 0)
              {
                paramAnonymousTLObject = new ArrayList();
                paramAnonymousTL_error = new TLRPC.TL_updateReadChannelInbox();
                paramAnonymousTL_error.channel_id = paramInt2;
                paramAnonymousTL_error.max_id = localTL_messages_chatFull.full_chat.read_inbox_max_id;
                paramAnonymousTLObject.add(paramAnonymousTL_error);
                MessagesController.this.processUpdateArray(paramAnonymousTLObject, null, null, false);
              }
              paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(l));
              paramAnonymousTLObject = paramAnonymousTL_error;
              if (paramAnonymousTL_error == null) {
                paramAnonymousTLObject = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(true, l));
              }
              MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(l), Integer.valueOf(Math.max(localTL_messages_chatFull.full_chat.read_outbox_max_id, paramAnonymousTLObject.intValue())));
              if (paramAnonymousTLObject.intValue() == 0)
              {
                paramAnonymousTLObject = new ArrayList();
                paramAnonymousTL_error = new TLRPC.TL_updateReadChannelOutbox();
                paramAnonymousTL_error.channel_id = paramInt2;
                paramAnonymousTL_error.max_id = localTL_messages_chatFull.full_chat.read_outbox_max_id;
                paramAnonymousTLObject.add(paramAnonymousTL_error);
                MessagesController.this.processUpdateArray(paramAnonymousTLObject, null, null, false);
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.applyDialogNotificationsSettings(-MessagesController.15.this.val$chat_id, localTL_messages_chatFull.full_chat.notify_settings);
                int i = 0;
                while (i < localTL_messages_chatFull.full_chat.bot_info.size())
                {
                  TLRPC.BotInfo localBotInfo = (TLRPC.BotInfo)localTL_messages_chatFull.full_chat.bot_info.get(i);
                  DataQuery.getInstance(MessagesController.this.currentAccount).putBotInfo(localBotInfo);
                  i += 1;
                }
                MessagesController.this.exportedChats.put(MessagesController.15.this.val$chat_id, localTL_messages_chatFull.full_chat.exported_invite);
                MessagesController.this.loadingFullChats.remove(Integer.valueOf(MessagesController.15.this.val$chat_id));
                MessagesController.this.loadedFullChats.add(Integer.valueOf(MessagesController.15.this.val$chat_id));
                MessagesController.this.putUsers(localTL_messages_chatFull.users, false);
                MessagesController.this.putChats(localTL_messages_chatFull.chats, false);
                if (localTL_messages_chatFull.full_chat.stickerset != null) {
                  DataQuery.getInstance(MessagesController.this.currentAccount).getGroupStickerSetById(localTL_messages_chatFull.full_chat.stickerset);
                }
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { localTL_messages_chatFull.full_chat, Integer.valueOf(MessagesController.15.this.val$classGuid), Boolean.valueOf(false), null });
              }
            });
            return;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.checkChannelError(paramAnonymousTL_error.text, MessagesController.15.this.val$chat_id);
              MessagesController.this.loadingFullChats.remove(Integer.valueOf(MessagesController.15.this.val$chat_id));
            }
          });
        }
      });
      if (paramInt2 == 0) {
        break;
      }
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt2);
      return;
      label164:
      paramBoolean = false;
      break label110;
      localObject2 = new TLRPC.TL_messages_getFullChat();
      ((TLRPC.TL_messages_getFullChat)localObject2).chat_id = paramInt1;
      if (this.dialogs_read_inbox_max.get(Long.valueOf(l)) != null)
      {
        localObject1 = localObject2;
        if (this.dialogs_read_outbox_max.get(Long.valueOf(l)) != null) {}
      }
      else
      {
        reloadDialogsReadValue(null, l);
        localObject1 = localObject2;
      }
    }
  }
  
  public void loadFullUser(final TLRPC.User paramUser, final int paramInt, boolean paramBoolean)
  {
    if ((paramUser == null) || (this.loadingFullUsers.contains(Integer.valueOf(paramUser.id))) || ((!paramBoolean) && (this.loadedFullUsers.contains(Integer.valueOf(paramUser.id))))) {
      return;
    }
    this.loadingFullUsers.add(Integer.valueOf(paramUser.id));
    TLRPC.TL_users_getFullUser localTL_users_getFullUser = new TLRPC.TL_users_getFullUser();
    localTL_users_getFullUser.id = getInputUser(paramUser);
    long l = paramUser.id;
    if ((this.dialogs_read_inbox_max.get(Long.valueOf(l)) == null) || (this.dialogs_read_outbox_max.get(Long.valueOf(l)) == null)) {
      reloadDialogsReadValue(null, l);
    }
    int i = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_users_getFullUser, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.TL_userFull localTL_userFull = (TLRPC.TL_userFull)paramAnonymousTLObject;
              MessagesController.this.applyDialogNotificationsSettings(MessagesController.16.this.val$user.id, localTL_userFull.notify_settings);
              if ((localTL_userFull.bot_info instanceof TLRPC.TL_botInfo)) {
                DataQuery.getInstance(MessagesController.this.currentAccount).putBotInfo(localTL_userFull.bot_info);
              }
              MessagesController.this.fullUsers.put(MessagesController.16.this.val$user.id, localTL_userFull);
              MessagesController.this.loadingFullUsers.remove(Integer.valueOf(MessagesController.16.this.val$user.id));
              MessagesController.this.loadedFullUsers.add(Integer.valueOf(MessagesController.16.this.val$user.id));
              String str = MessagesController.16.this.val$user.first_name + MessagesController.16.this.val$user.last_name + MessagesController.16.this.val$user.username;
              ArrayList localArrayList = new ArrayList();
              localArrayList.add(localTL_userFull.user);
              MessagesController.this.putUsers(localArrayList, false);
              MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localArrayList, null, false, true);
              if ((str != null) && (!str.equals(localTL_userFull.user.first_name + localTL_userFull.user.last_name + localTL_userFull.user.username))) {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1) });
              }
              if ((localTL_userFull.bot_info instanceof TLRPC.TL_botInfo)) {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.botInfoDidLoaded, new Object[] { localTL_userFull.bot_info, Integer.valueOf(MessagesController.16.this.val$classGuid) });
              }
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.userInfoDidLoaded, new Object[] { Integer.valueOf(MessagesController.16.this.val$user.id), localTL_userFull });
            }
          });
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.this.loadingFullUsers.remove(Integer.valueOf(MessagesController.16.this.val$user.id));
          }
        });
      }
    });
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(i, paramInt);
  }
  
  public void loadHintDialogs()
  {
    if ((!this.hintDialogs.isEmpty()) || (TextUtils.isEmpty(this.installReferer))) {
      return;
    }
    TLRPC.TL_help_getRecentMeUrls localTL_help_getRecentMeUrls = new TLRPC.TL_help_getRecentMeUrls();
    localTL_help_getRecentMeUrls.referer = this.installReferer;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_help_getRecentMeUrls, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.TL_help_recentMeUrls localTL_help_recentMeUrls = (TLRPC.TL_help_recentMeUrls)paramAnonymousTLObject;
              MessagesController.this.putUsers(localTL_help_recentMeUrls.users, false);
              MessagesController.this.putChats(localTL_help_recentMeUrls.chats, false);
              MessagesController.this.hintDialogs.clear();
              MessagesController.this.hintDialogs.addAll(localTL_help_recentMeUrls.urls);
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            }
          });
        }
      }
    });
  }
  
  public void loadMessages(long paramLong, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean1, int paramInt4, int paramInt5, int paramInt6, int paramInt7, boolean paramBoolean2, int paramInt8)
  {
    loadMessages(paramLong, paramInt1, paramInt2, paramInt3, paramBoolean1, paramInt4, paramInt5, paramInt6, paramInt7, paramBoolean2, paramInt8, 0, 0, 0, false, 0);
  }
  
  public void loadMessages(long paramLong, int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean1, int paramInt4, int paramInt5, int paramInt6, int paramInt7, boolean paramBoolean2, int paramInt8, int paramInt9, int paramInt10, int paramInt11, boolean paramBoolean3, int paramInt12)
  {
    loadMessagesInternal(paramLong, paramInt1, paramInt2, paramInt3, paramBoolean1, paramInt4, paramInt5, paramInt6, paramInt7, paramBoolean2, paramInt8, paramInt9, paramInt10, paramInt11, paramBoolean3, paramInt12, true);
  }
  
  public void loadPeerSettings(TLRPC.User paramUser, TLRPC.Chat paramChat)
  {
    if ((paramUser == null) && (paramChat == null)) {}
    final long l;
    label143:
    for (;;)
    {
      return;
      if (paramUser != null) {}
      for (l = paramUser.id;; l = -paramChat.id)
      {
        if (this.loadingPeerSettings.indexOfKey(l) >= 0) {
          break label143;
        }
        this.loadingPeerSettings.put(l, Boolean.valueOf(true));
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("request spam button for " + l);
        }
        if (this.notificationsPreferences.getInt("spam3_" + l, 0) != 1) {
          break label145;
        }
        if (!BuildVars.LOGS_ENABLED) {
          break;
        }
        FileLog.d("spam button already hidden for " + l);
        return;
      }
    }
    label145:
    if (this.notificationsPreferences.getBoolean("spam_" + l, false))
    {
      localObject = new TLRPC.TL_messages_hideReportSpam();
      if (paramUser != null) {
        ((TLRPC.TL_messages_hideReportSpam)localObject).peer = getInputPeer(paramUser.id);
      }
      for (;;)
      {
        ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController.this.loadingPeerSettings.remove(MessagesController.21.this.val$dialogId);
                SharedPreferences.Editor localEditor = MessagesController.this.notificationsPreferences.edit();
                localEditor.remove("spam_" + MessagesController.21.this.val$dialogId);
                localEditor.putInt("spam3_" + MessagesController.21.this.val$dialogId, 1);
                localEditor.commit();
              }
            });
          }
        });
        return;
        if (paramChat != null) {
          ((TLRPC.TL_messages_hideReportSpam)localObject).peer = getInputPeer(-paramChat.id);
        }
      }
    }
    Object localObject = new TLRPC.TL_messages_getPeerSettings();
    if (paramUser != null) {
      ((TLRPC.TL_messages_getPeerSettings)localObject).peer = getInputPeer(paramUser.id);
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.loadingPeerSettings.remove(MessagesController.22.this.val$dialogId);
              SharedPreferences.Editor localEditor;
              if (paramAnonymousTLObject != null)
              {
                TLRPC.TL_peerSettings localTL_peerSettings = (TLRPC.TL_peerSettings)paramAnonymousTLObject;
                localEditor = MessagesController.this.notificationsPreferences.edit();
                if (!localTL_peerSettings.report_spam)
                {
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("don't show spam button for " + MessagesController.22.this.val$dialogId);
                  }
                  localEditor.putInt("spam3_" + MessagesController.22.this.val$dialogId, 1);
                  localEditor.commit();
                }
              }
              else
              {
                return;
              }
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("show spam button for " + MessagesController.22.this.val$dialogId);
              }
              localEditor.putInt("spam3_" + MessagesController.22.this.val$dialogId, 2);
              localEditor.commit();
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.peerSettingsDidLoaded, new Object[] { Long.valueOf(MessagesController.22.this.val$dialogId) });
            }
          });
        }
      });
      return;
      if (paramChat != null) {
        ((TLRPC.TL_messages_getPeerSettings)localObject).peer = getInputPeer(-paramChat.id);
      }
    }
  }
  
  public void loadPinnedDialogs(final long paramLong, final ArrayList<Long> paramArrayList)
  {
    if (UserConfig.getInstance(this.currentAccount).pinnedDialogsLoaded) {
      return;
    }
    TLRPC.TL_messages_getPinnedDialogs localTL_messages_getPinnedDialogs = new TLRPC.TL_messages_getPinnedDialogs();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getPinnedDialogs, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null)
        {
          final TLRPC.TL_messages_peerDialogs localTL_messages_peerDialogs = (TLRPC.TL_messages_peerDialogs)paramAnonymousTLObject;
          final TLRPC.TL_messages_dialogs localTL_messages_dialogs = new TLRPC.TL_messages_dialogs();
          localTL_messages_dialogs.users.addAll(localTL_messages_peerDialogs.users);
          localTL_messages_dialogs.chats.addAll(localTL_messages_peerDialogs.chats);
          localTL_messages_dialogs.dialogs.addAll(localTL_messages_peerDialogs.dialogs);
          localTL_messages_dialogs.messages.addAll(localTL_messages_peerDialogs.messages);
          final LongSparseArray localLongSparseArray = new LongSparseArray();
          paramAnonymousTLObject = new SparseArray();
          SparseArray localSparseArray = new SparseArray();
          final ArrayList localArrayList = new ArrayList();
          int i = 0;
          while (i < localTL_messages_peerDialogs.users.size())
          {
            paramAnonymousTL_error = (TLRPC.User)localTL_messages_peerDialogs.users.get(i);
            paramAnonymousTLObject.put(paramAnonymousTL_error.id, paramAnonymousTL_error);
            i += 1;
          }
          i = 0;
          while (i < localTL_messages_peerDialogs.chats.size())
          {
            paramAnonymousTL_error = (TLRPC.Chat)localTL_messages_peerDialogs.chats.get(i);
            localSparseArray.put(paramAnonymousTL_error.id, paramAnonymousTL_error);
            i += 1;
          }
          i = 0;
          Object localObject;
          if (i < localTL_messages_peerDialogs.messages.size())
          {
            paramAnonymousTL_error = (TLRPC.Message)localTL_messages_peerDialogs.messages.get(i);
            if (paramAnonymousTL_error.to_id.channel_id != 0)
            {
              localObject = (TLRPC.Chat)localSparseArray.get(paramAnonymousTL_error.to_id.channel_id);
              if ((localObject == null) || (!((TLRPC.Chat)localObject).left)) {
                break label311;
              }
            }
            for (;;)
            {
              i += 1;
              break;
              if (paramAnonymousTL_error.to_id.chat_id != 0)
              {
                localObject = (TLRPC.Chat)localSparseArray.get(paramAnonymousTL_error.to_id.chat_id);
                if ((localObject != null) && (((TLRPC.Chat)localObject).migrated_to != null)) {}
              }
              else
              {
                label311:
                paramAnonymousTL_error = new MessageObject(MessagesController.this.currentAccount, paramAnonymousTL_error, paramAnonymousTLObject, localSparseArray, false);
                localLongSparseArray.put(paramAnonymousTL_error.getDialogId(), paramAnonymousTL_error);
              }
            }
          }
          i = 0;
          if (i < localTL_messages_peerDialogs.dialogs.size())
          {
            localObject = (TLRPC.TL_dialog)localTL_messages_peerDialogs.dialogs.get(i);
            if (((TLRPC.TL_dialog)localObject).id == 0L)
            {
              if (((TLRPC.TL_dialog)localObject).peer.user_id != 0) {
                ((TLRPC.TL_dialog)localObject).id = ((TLRPC.TL_dialog)localObject).peer.user_id;
              }
            }
            else
            {
              label407:
              localArrayList.add(Long.valueOf(((TLRPC.TL_dialog)localObject).id));
              if (!DialogObject.isChannel((TLRPC.TL_dialog)localObject)) {
                break label521;
              }
              paramAnonymousTLObject = (TLRPC.Chat)localSparseArray.get(-(int)((TLRPC.TL_dialog)localObject).id);
              if ((paramAnonymousTLObject == null) || (!paramAnonymousTLObject.left)) {
                break label557;
              }
            }
            for (;;)
            {
              i += 1;
              break;
              if (((TLRPC.TL_dialog)localObject).peer.chat_id != 0)
              {
                ((TLRPC.TL_dialog)localObject).id = (-((TLRPC.TL_dialog)localObject).peer.chat_id);
                break label407;
              }
              if (((TLRPC.TL_dialog)localObject).peer.channel_id == 0) {
                break label407;
              }
              ((TLRPC.TL_dialog)localObject).id = (-((TLRPC.TL_dialog)localObject).peer.channel_id);
              break label407;
              label521:
              if ((int)((TLRPC.TL_dialog)localObject).id < 0)
              {
                paramAnonymousTLObject = (TLRPC.Chat)localSparseArray.get(-(int)((TLRPC.TL_dialog)localObject).id);
                if ((paramAnonymousTLObject != null) && (paramAnonymousTLObject.migrated_to != null)) {}
              }
              else
              {
                label557:
                if (((TLRPC.TL_dialog)localObject).last_message_date == 0)
                {
                  paramAnonymousTLObject = (MessageObject)localLongSparseArray.get(((TLRPC.TL_dialog)localObject).id);
                  if (paramAnonymousTLObject != null) {
                    ((TLRPC.TL_dialog)localObject).last_message_date = paramAnonymousTLObject.messageOwner.date;
                  }
                }
                paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject).id));
                paramAnonymousTLObject = paramAnonymousTL_error;
                if (paramAnonymousTL_error == null) {
                  paramAnonymousTLObject = Integer.valueOf(0);
                }
                MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject).id), Integer.valueOf(Math.max(paramAnonymousTLObject.intValue(), ((TLRPC.TL_dialog)localObject).read_inbox_max_id)));
                paramAnonymousTL_error = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject).id));
                paramAnonymousTLObject = paramAnonymousTL_error;
                if (paramAnonymousTL_error == null) {
                  paramAnonymousTLObject = Integer.valueOf(0);
                }
                MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject).id), Integer.valueOf(Math.max(paramAnonymousTLObject.intValue(), ((TLRPC.TL_dialog)localObject).read_outbox_max_id)));
              }
            }
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).getStorageQueue().postRunnable(new Runnable()
          {
            public void run()
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  MessagesController.this.applyDialogsNotificationsSettings(MessagesController.118.1.this.val$res.dialogs);
                  int i = 0;
                  int n = 0;
                  int m = 0;
                  int k = 0;
                  LongSparseArray localLongSparseArray = new LongSparseArray();
                  ArrayList localArrayList1 = new ArrayList();
                  int j = 0;
                  while (j < MessagesController.this.dialogs.size())
                  {
                    localObject1 = (TLRPC.TL_dialog)MessagesController.this.dialogs.get(j);
                    if ((int)((TLRPC.TL_dialog)localObject1).id == 0) {
                      j += 1;
                    } else {
                      if (((TLRPC.TL_dialog)localObject1).pinned) {
                        break label202;
                      }
                    }
                  }
                  ArrayList localArrayList2 = new ArrayList();
                  if (MessagesController.118.this.val$order != null) {}
                  for (Object localObject1 = MessagesController.118.this.val$order;; localObject1 = MessagesController.118.1.this.val$newPinnedOrder)
                  {
                    if (((ArrayList)localObject1).size() < localArrayList1.size()) {
                      ((ArrayList)localObject1).add(Long.valueOf(0L));
                    }
                    while (localArrayList1.size() < ((ArrayList)localObject1).size()) {
                      localArrayList1.add(0, Long.valueOf(0L));
                    }
                    label202:
                    k = Math.max(((TLRPC.TL_dialog)localObject1).pinnedNum, k);
                    localLongSparseArray.put(((TLRPC.TL_dialog)localObject1).id, Integer.valueOf(((TLRPC.TL_dialog)localObject1).pinnedNum));
                    localArrayList1.add(Long.valueOf(((TLRPC.TL_dialog)localObject1).id));
                    ((TLRPC.TL_dialog)localObject1).pinned = false;
                    ((TLRPC.TL_dialog)localObject1).pinnedNum = 0;
                    i = 1;
                    break;
                  }
                  j = i;
                  if (!MessagesController.118.1.this.val$res.dialogs.isEmpty())
                  {
                    MessagesController.this.putUsers(MessagesController.118.1.this.val$res.users, false);
                    MessagesController.this.putChats(MessagesController.118.1.this.val$res.chats, false);
                    n = 0;
                    j = i;
                    i = m;
                    m = n;
                    n = i;
                    if (m < MessagesController.118.1.this.val$res.dialogs.size())
                    {
                      TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)MessagesController.118.1.this.val$res.dialogs.get(m);
                      Object localObject2;
                      if (MessagesController.118.this.val$newDialogId != 0L)
                      {
                        localObject2 = (Integer)localLongSparseArray.get(localTL_dialog.id);
                        if (localObject2 != null) {
                          localTL_dialog.pinnedNum = ((Integer)localObject2).intValue();
                        }
                        label437:
                        if (localTL_dialog.pinnedNum == 0) {
                          localTL_dialog.pinnedNum = (MessagesController.118.1.this.val$res.dialogs.size() - m + k);
                        }
                        localArrayList2.add(Long.valueOf(localTL_dialog.id));
                        localObject2 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(localTL_dialog.id);
                        if (localObject2 == null) {
                          break label689;
                        }
                        ((TLRPC.TL_dialog)localObject2).pinned = true;
                        ((TLRPC.TL_dialog)localObject2).pinnedNum = localTL_dialog.pinnedNum;
                        MessagesStorage.getInstance(MessagesController.this.currentAccount).setDialogPinned(localTL_dialog.id, localTL_dialog.pinnedNum);
                      }
                      for (;;)
                      {
                        j = 1;
                        m += 1;
                        break;
                        j = localArrayList1.indexOf(Long.valueOf(localTL_dialog.id));
                        n = ((ArrayList)localObject1).indexOf(Long.valueOf(localTL_dialog.id));
                        if ((j == -1) || (n == -1)) {
                          break label437;
                        }
                        if (j == n)
                        {
                          localObject2 = (Integer)localLongSparseArray.get(localTL_dialog.id);
                          if (localObject2 == null) {
                            break label437;
                          }
                          localTL_dialog.pinnedNum = ((Integer)localObject2).intValue();
                          break label437;
                        }
                        localObject2 = (Integer)localLongSparseArray.get(((Long)localArrayList1.get(n)).longValue());
                        if (localObject2 == null) {
                          break label437;
                        }
                        localTL_dialog.pinnedNum = ((Integer)localObject2).intValue();
                        break label437;
                        label689:
                        j = 1;
                        MessagesController.this.dialogs_dict.put(localTL_dialog.id, localTL_dialog);
                        localObject2 = (MessageObject)MessagesController.118.1.this.val$new_dialogMessage.get(localTL_dialog.id);
                        MessagesController.this.dialogMessage.put(localTL_dialog.id, localObject2);
                        i = j;
                        if (localObject2 != null)
                        {
                          i = j;
                          if (((MessageObject)localObject2).messageOwner.to_id.channel_id == 0)
                          {
                            MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject2).getId(), localObject2);
                            i = j;
                            if (((MessageObject)localObject2).messageOwner.random_id != 0L)
                            {
                              MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject2).messageOwner.random_id, localObject2);
                              i = j;
                            }
                          }
                        }
                      }
                    }
                  }
                  if (j != 0)
                  {
                    if (n != 0)
                    {
                      MessagesController.this.dialogs.clear();
                      i = 0;
                      j = MessagesController.this.dialogs_dict.size();
                      while (i < j)
                      {
                        MessagesController.this.dialogs.add(MessagesController.this.dialogs_dict.valueAt(i));
                        i += 1;
                      }
                    }
                    MessagesController.this.sortDialogs(null);
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                  }
                  MessagesStorage.getInstance(MessagesController.this.currentAccount).unpinAllDialogsExceptNew(localArrayList2);
                  MessagesStorage.getInstance(MessagesController.this.currentAccount).putDialogs(MessagesController.118.1.this.val$toCache, true);
                  UserConfig.getInstance(MessagesController.this.currentAccount).pinnedDialogsLoaded = true;
                  UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
                }
              });
            }
          });
        }
      }
    });
  }
  
  protected void loadUnknownChannel(TLRPC.Chat paramChat, final long paramLong)
  {
    if ((!(paramChat instanceof TLRPC.TL_channel)) || (this.gettingUnknownChannels.indexOfKey(paramChat.id) >= 0)) {}
    do
    {
      return;
      if (paramChat.access_hash != 0L) {
        break;
      }
    } while (paramLong == 0L);
    MessagesStorage.getInstance(this.currentAccount).removePendingTask(paramLong);
    return;
    Object localObject1 = new TLRPC.TL_inputPeerChannel();
    ((TLRPC.TL_inputPeerChannel)localObject1).channel_id = paramChat.id;
    ((TLRPC.TL_inputPeerChannel)localObject1).access_hash = paramChat.access_hash;
    this.gettingUnknownChannels.put(paramChat.id, true);
    TLRPC.TL_messages_getPeerDialogs localTL_messages_getPeerDialogs = new TLRPC.TL_messages_getPeerDialogs();
    TLRPC.TL_inputDialogPeer localTL_inputDialogPeer = new TLRPC.TL_inputDialogPeer();
    localTL_inputDialogPeer.peer = ((TLRPC.InputPeer)localObject1);
    localTL_messages_getPeerDialogs.peers.add(localTL_inputDialogPeer);
    Object localObject2;
    if (paramLong == 0L) {
      localObject2 = null;
    }
    for (;;)
    {
      try
      {
        localObject1 = new NativeByteBuffer(paramChat.getObjectSize() + 4);
        FileLog.e(localException1);
      }
      catch (Exception localException1)
      {
        try
        {
          ((NativeByteBuffer)localObject1).writeInt32(0);
          paramChat.serializeToStream((AbstractSerializedData)localObject1);
          paramLong = MessagesStorage.getInstance(this.currentAccount).createPendingTask((NativeByteBuffer)localObject1);
          ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getPeerDialogs, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTLObject != null)
              {
                paramAnonymousTLObject = (TLRPC.TL_messages_peerDialogs)paramAnonymousTLObject;
                if ((!paramAnonymousTLObject.dialogs.isEmpty()) && (!paramAnonymousTLObject.chats.isEmpty()))
                {
                  paramAnonymousTL_error = new TLRPC.TL_messages_dialogs();
                  paramAnonymousTL_error.dialogs.addAll(paramAnonymousTLObject.dialogs);
                  paramAnonymousTL_error.messages.addAll(paramAnonymousTLObject.messages);
                  paramAnonymousTL_error.users.addAll(paramAnonymousTLObject.users);
                  paramAnonymousTL_error.chats.addAll(paramAnonymousTLObject.chats);
                  MessagesController.this.processLoadedDialogs(paramAnonymousTL_error, null, 0, 1, 2, false, false, false);
                }
              }
              if (paramLong != 0L) {
                MessagesStorage.getInstance(MessagesController.this.currentAccount).removePendingTask(paramLong);
              }
              MessagesController.this.gettingUnknownChannels.delete(this.val$channel.id);
            }
          });
          return;
        }
        catch (Exception localException2)
        {
          for (;;) {}
        }
        localException1 = localException1;
        localObject1 = localObject2;
      }
    }
  }
  
  public void markChannelDialogMessageAsDeleted(ArrayList<Integer> paramArrayList, int paramInt)
  {
    MessageObject localMessageObject = (MessageObject)this.dialogMessage.get(-paramInt);
    if (localMessageObject != null) {
      paramInt = 0;
    }
    for (;;)
    {
      if (paramInt < paramArrayList.size())
      {
        Integer localInteger = (Integer)paramArrayList.get(paramInt);
        if (localMessageObject.getId() == localInteger.intValue()) {
          localMessageObject.deleted = true;
        }
      }
      else
      {
        return;
      }
      paramInt += 1;
    }
  }
  
  public void markDialogAsRead(final long paramLong, final int paramInt1, final int paramInt2, final int paramInt3, final boolean paramBoolean1, final int paramInt4, boolean paramBoolean2)
  {
    int i = (int)paramLong;
    int j = (int)(paramLong >> 32);
    if (i != 0) {
      if ((paramInt1 != 0) && (j != 1)) {}
    }
    label377:
    for (;;)
    {
      return;
      long l4 = paramInt1;
      long l3 = paramInt2;
      boolean bool2 = false;
      long l2 = l4;
      long l1 = l3;
      boolean bool1 = bool2;
      if (i < 0)
      {
        l2 = l4;
        l1 = l3;
        bool1 = bool2;
        if (ChatObject.isChannel(getChat(Integer.valueOf(-i))))
        {
          l2 = l4 | -i << 32;
          l1 = l3 | -i << 32;
          bool1 = true;
        }
      }
      Integer localInteger = (Integer)this.dialogs_read_inbox_max.get(Long.valueOf(paramLong));
      Object localObject = localInteger;
      if (localInteger == null) {
        localObject = Integer.valueOf(0);
      }
      this.dialogs_read_inbox_max.put(Long.valueOf(paramLong), Integer.valueOf(Math.max(((Integer)localObject).intValue(), paramInt1)));
      MessagesStorage.getInstance(this.currentAccount).processPendingRead(paramLong, l2, l1, paramInt3, bool1);
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              Object localObject = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(MessagesController.84.this.val$dialogId);
              if (localObject != null) {
                if ((MessagesController.84.this.val$countDiff != 0) && (MessagesController.84.this.val$maxPositiveId < ((TLRPC.TL_dialog)localObject).top_message)) {
                  break label174;
                }
              }
              for (((TLRPC.TL_dialog)localObject).unread_count = 0;; ((TLRPC.TL_dialog)localObject).unread_count = (((TLRPC.TL_dialog)localObject).top_message - MessagesController.84.this.val$maxPositiveId)) {
                label174:
                do
                {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(256) });
                  if (MessagesController.84.this.val$popup) {
                    break;
                  }
                  NotificationsController.getInstance(MessagesController.this.currentAccount).processReadMessages(null, MessagesController.84.this.val$dialogId, 0, MessagesController.84.this.val$maxPositiveId, false);
                  localObject = new LongSparseArray(1);
                  ((LongSparseArray)localObject).put(MessagesController.84.this.val$dialogId, Integer.valueOf(0));
                  NotificationsController.getInstance(MessagesController.this.currentAccount).processDialogsUpdateRead((LongSparseArray)localObject);
                  return;
                  ((TLRPC.TL_dialog)localObject).unread_count = Math.max(((TLRPC.TL_dialog)localObject).unread_count - MessagesController.84.this.val$countDiff, 0);
                } while ((MessagesController.84.this.val$maxPositiveId == Integer.MIN_VALUE) || (((TLRPC.TL_dialog)localObject).unread_count <= ((TLRPC.TL_dialog)localObject).top_message - MessagesController.84.this.val$maxPositiveId));
              }
              NotificationsController.getInstance(MessagesController.this.currentAccount).processReadMessages(null, MessagesController.84.this.val$dialogId, 0, MessagesController.84.this.val$maxPositiveId, true);
              localObject = new LongSparseArray(1);
              ((LongSparseArray)localObject).put(MessagesController.84.this.val$dialogId, Integer.valueOf(-1));
              NotificationsController.getInstance(MessagesController.this.currentAccount).processDialogsUpdateRead((LongSparseArray)localObject);
            }
          });
        }
      });
      if (paramInt1 != Integer.MAX_VALUE) {
        paramInt2 = 1;
      }
      for (;;)
      {
        if (paramInt2 == 0) {
          break label377;
        }
        Utilities.stageQueue.postRunnable(new Runnable()
        {
          public void run()
          {
            MessagesController.ReadTask localReadTask2 = (MessagesController.ReadTask)MessagesController.this.readTasksMap.get(paramLong);
            MessagesController.ReadTask localReadTask1 = localReadTask2;
            if (localReadTask2 == null)
            {
              localReadTask2 = new MessagesController.ReadTask(MessagesController.this, null);
              localReadTask2.dialogId = paramLong;
              localReadTask2.sendRequestTime = (SystemClock.uptimeMillis() + 5000L);
              localReadTask1 = localReadTask2;
              if (!paramInt3)
              {
                MessagesController.this.readTasksMap.put(paramLong, localReadTask2);
                MessagesController.this.readTasks.add(localReadTask2);
                localReadTask1 = localReadTask2;
              }
            }
            localReadTask1.maxDate = paramInt1;
            localReadTask1.maxId = this.val$maxPositiveId;
            if (paramInt3) {
              MessagesController.this.completeReadTask(localReadTask1);
            }
          }
        });
        return;
        paramInt2 = 0;
        continue;
        if (paramInt3 == 0) {
          break;
        }
        i = 1;
        localObject = getEncryptedChat(Integer.valueOf(j));
        MessagesStorage.getInstance(this.currentAccount).processPendingRead(paramLong, paramInt1, paramInt2, paramInt3, false);
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
        {
          public void run()
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationsController.getInstance(MessagesController.this.currentAccount).processReadMessages(null, MessagesController.85.this.val$dialogId, MessagesController.85.this.val$maxDate, 0, MessagesController.85.this.val$popup);
                Object localObject = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(MessagesController.85.this.val$dialogId);
                if (localObject != null) {
                  if ((MessagesController.85.this.val$countDiff != 0) && (MessagesController.85.this.val$maxNegativeId > ((TLRPC.TL_dialog)localObject).top_message)) {
                    break label170;
                  }
                }
                for (((TLRPC.TL_dialog)localObject).unread_count = 0;; ((TLRPC.TL_dialog)localObject).unread_count = (MessagesController.85.this.val$maxNegativeId - ((TLRPC.TL_dialog)localObject).top_message)) {
                  label170:
                  do
                  {
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(256) });
                    localObject = new LongSparseArray(1);
                    ((LongSparseArray)localObject).put(MessagesController.85.this.val$dialogId, Integer.valueOf(0));
                    NotificationsController.getInstance(MessagesController.this.currentAccount).processDialogsUpdateRead((LongSparseArray)localObject);
                    return;
                    ((TLRPC.TL_dialog)localObject).unread_count = Math.max(((TLRPC.TL_dialog)localObject).unread_count - MessagesController.85.this.val$countDiff, 0);
                  } while ((MessagesController.85.this.val$maxNegativeId == Integer.MAX_VALUE) || (((TLRPC.TL_dialog)localObject).unread_count <= MessagesController.85.this.val$maxNegativeId - ((TLRPC.TL_dialog)localObject).top_message));
                }
              }
            });
          }
        });
        paramInt2 = i;
        if (((TLRPC.EncryptedChat)localObject).ttl > 0)
        {
          paramInt2 = Math.max(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime(), paramInt3);
          MessagesStorage.getInstance(this.currentAccount).createTaskForSecretChat(((TLRPC.EncryptedChat)localObject).id, paramInt2, paramInt2, 0, null);
          paramInt2 = i;
        }
      }
    }
  }
  
  public void markDialogAsReadNow(final long paramLong)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.ReadTask localReadTask = (MessagesController.ReadTask)MessagesController.this.readTasksMap.get(paramLong);
        if (localReadTask == null) {
          return;
        }
        MessagesController.this.completeReadTask(localReadTask);
        MessagesController.this.readTasks.remove(localReadTask);
        MessagesController.this.readTasksMap.remove(paramLong);
      }
    });
  }
  
  public void markMentionMessageAsRead(int paramInt1, int paramInt2, long paramLong)
  {
    MessagesStorage.getInstance(this.currentAccount).markMentionMessageAsRead(paramInt1, paramInt2, paramLong);
    if (paramInt2 != 0)
    {
      localObject = new TLRPC.TL_channels_readMessageContents();
      ((TLRPC.TL_channels_readMessageContents)localObject).channel = getInputChannel(paramInt2);
      if (((TLRPC.TL_channels_readMessageContents)localObject).channel == null) {
        return;
      }
      ((TLRPC.TL_channels_readMessageContents)localObject).id.add(Integer.valueOf(paramInt1));
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      });
      return;
    }
    Object localObject = new TLRPC.TL_messages_readMessageContents();
    ((TLRPC.TL_messages_readMessageContents)localObject).id.add(Integer.valueOf(paramInt1));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
          MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
        }
      }
    });
  }
  
  public void markMessageAsRead(int paramInt1, int paramInt2, int paramInt3)
  {
    if ((paramInt1 == 0) || (paramInt3 <= 0)) {
      return;
    }
    int i = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    MessagesStorage.getInstance(this.currentAccount).createTaskForMid(paramInt1, paramInt2, i, i, paramInt3, false);
    if (paramInt2 != 0)
    {
      localObject = new TLRPC.TL_channels_readMessageContents();
      ((TLRPC.TL_channels_readMessageContents)localObject).channel = getInputChannel(paramInt2);
      ((TLRPC.TL_channels_readMessageContents)localObject).id.add(Integer.valueOf(paramInt1));
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      });
      return;
    }
    Object localObject = new TLRPC.TL_messages_readMessageContents();
    ((TLRPC.TL_messages_readMessageContents)localObject).id.add(Integer.valueOf(paramInt1));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
          MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
        }
      }
    });
  }
  
  public void markMessageAsRead(long paramLong1, long paramLong2, int paramInt)
  {
    if ((paramLong2 == 0L) || (paramLong1 == 0L) || ((paramInt <= 0) && (paramInt != Integer.MIN_VALUE))) {}
    TLRPC.EncryptedChat localEncryptedChat;
    ArrayList localArrayList;
    do
    {
      do
      {
        int i;
        int j;
        do
        {
          return;
          i = (int)paramLong1;
          j = (int)(paramLong1 >> 32);
        } while (i != 0);
        localEncryptedChat = getEncryptedChat(Integer.valueOf(j));
      } while (localEncryptedChat == null);
      localArrayList = new ArrayList();
      localArrayList.add(Long.valueOf(paramLong2));
      SecretChatHelper.getInstance(this.currentAccount).sendMessagesReadMessage(localEncryptedChat, localArrayList, null);
    } while (paramInt <= 0);
    paramInt = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
    MessagesStorage.getInstance(this.currentAccount).createTaskForSecretChat(localEncryptedChat.id, paramInt, paramInt, 0, localArrayList);
  }
  
  public void markMessageContentAsRead(MessageObject paramMessageObject)
  {
    Object localObject = new ArrayList();
    long l2 = paramMessageObject.getId();
    long l1 = l2;
    if (paramMessageObject.messageOwner.to_id.channel_id != 0) {
      l1 = l2 | paramMessageObject.messageOwner.to_id.channel_id << 32;
    }
    if (paramMessageObject.messageOwner.mentioned) {
      MessagesStorage.getInstance(this.currentAccount).markMentionMessageAsRead(paramMessageObject.getId(), paramMessageObject.messageOwner.to_id.channel_id, paramMessageObject.getDialogId());
    }
    ((ArrayList)localObject).add(Long.valueOf(l1));
    MessagesStorage.getInstance(this.currentAccount).markMessagesContentAsRead((ArrayList)localObject, 0);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.messagesReadContent, new Object[] { localObject });
    if (paramMessageObject.getId() < 0) {
      markMessageAsRead(paramMessageObject.getDialogId(), paramMessageObject.messageOwner.random_id, Integer.MIN_VALUE);
    }
    do
    {
      return;
      if (paramMessageObject.messageOwner.to_id.channel_id == 0) {
        break;
      }
      localObject = new TLRPC.TL_channels_readMessageContents();
      ((TLRPC.TL_channels_readMessageContents)localObject).channel = getInputChannel(paramMessageObject.messageOwner.to_id.channel_id);
    } while (((TLRPC.TL_channels_readMessageContents)localObject).channel == null);
    ((TLRPC.TL_channels_readMessageContents)localObject).id.add(Integer.valueOf(paramMessageObject.getId()));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
    return;
    localObject = new TLRPC.TL_messages_readMessageContents();
    ((TLRPC.TL_messages_readMessageContents)localObject).id.add(Integer.valueOf(paramMessageObject.getId()));
    ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          paramAnonymousTLObject = (TLRPC.TL_messages_affectedMessages)paramAnonymousTLObject;
          MessagesController.this.processNewDifferenceParams(-1, paramAnonymousTLObject.pts, -1, paramAnonymousTLObject.pts_count);
        }
      }
    });
  }
  
  public void openByUserName(String paramString, final BaseFragment paramBaseFragment, final int paramInt)
  {
    if ((paramString == null) || (paramBaseFragment == null)) {}
    do
    {
      return;
      TLObject localTLObject = getUserOrChat(paramString);
      TLRPC.User localUser = null;
      TLRPC.Chat localChat = null;
      if ((localTLObject instanceof TLRPC.User))
      {
        localUser = (TLRPC.User)localTLObject;
        localObject1 = localChat;
        localObject2 = localUser;
        if (localUser.min)
        {
          localObject2 = null;
          localObject1 = localChat;
        }
      }
      while (localObject2 != null)
      {
        openChatOrProfileWith((TLRPC.User)localObject2, null, paramBaseFragment, paramInt, false);
        return;
        localObject1 = localChat;
        localObject2 = localUser;
        if ((localTLObject instanceof TLRPC.Chat))
        {
          localChat = (TLRPC.Chat)localTLObject;
          localObject1 = localChat;
          localObject2 = localUser;
          if (localChat.min)
          {
            localObject1 = null;
            localObject2 = localUser;
          }
        }
      }
      if (localObject1 != null)
      {
        openChatOrProfileWith(null, (TLRPC.Chat)localObject1, paramBaseFragment, 1, false);
        return;
      }
    } while (paramBaseFragment.getParentActivity() == null);
    final Object localObject1 = new AlertDialog[1];
    localObject1[0] = new AlertDialog(paramBaseFragment.getParentActivity(), 1);
    Object localObject2 = new TLRPC.TL_contacts_resolveUsername();
    ((TLRPC.TL_contacts_resolveUsername)localObject2).username = paramString;
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            try
            {
              MessagesController.138.this.val$progressDialog[0].dismiss();
              MessagesController.138.this.val$progressDialog[0] = null;
              MessagesController.138.this.val$fragment.setVisibleDialog(null);
              if (paramAnonymousTL_error == null)
              {
                localTL_contacts_resolvedPeer = (TLRPC.TL_contacts_resolvedPeer)paramAnonymousTLObject;
                MessagesController.this.putUsers(localTL_contacts_resolvedPeer.users, false);
                MessagesController.this.putChats(localTL_contacts_resolvedPeer.chats, false);
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(localTL_contacts_resolvedPeer.users, localTL_contacts_resolvedPeer.chats, false, true);
                if (!localTL_contacts_resolvedPeer.chats.isEmpty()) {
                  MessagesController.openChatOrProfileWith(null, (TLRPC.Chat)localTL_contacts_resolvedPeer.chats.get(0), MessagesController.138.this.val$fragment, 1, false);
                }
              }
              while ((MessagesController.138.this.val$fragment == null) || (MessagesController.138.this.val$fragment.getParentActivity() == null))
              {
                TLRPC.TL_contacts_resolvedPeer localTL_contacts_resolvedPeer;
                do
                {
                  return;
                } while (localTL_contacts_resolvedPeer.users.isEmpty());
                MessagesController.openChatOrProfileWith((TLRPC.User)localTL_contacts_resolvedPeer.users.get(0), null, MessagesController.138.this.val$fragment, MessagesController.138.this.val$type, false);
                return;
              }
              try
              {
                Toast.makeText(MessagesController.138.this.val$fragment.getParentActivity(), LocaleController.getString("NoUsernameFound", 2131493916), 0).show();
                return;
              }
              catch (Exception localException1)
              {
                FileLog.e(localException1);
                return;
              }
            }
            catch (Exception localException2)
            {
              for (;;) {}
            }
          }
        });
      }
    }
    {
      public void run()
      {
        if (localObject1[0] == null) {
          return;
        }
        localObject1[0].setMessage(LocaleController.getString("Loading", 2131493762));
        localObject1[0].setCanceledOnTouchOutside(false);
        localObject1[0].setCancelable(false);
        localObject1[0].setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            ConnectionsManager.getInstance(MessagesController.this.currentAccount).cancelRequest(MessagesController.139.this.val$reqId, true);
            try
            {
              paramAnonymous2DialogInterface.dismiss();
              return;
            }
            catch (Exception paramAnonymous2DialogInterface)
            {
              FileLog.e(paramAnonymous2DialogInterface);
            }
          }
        });
        paramBaseFragment.showDialog(localObject1[0]);
      }
    }, 500L);
  }
  
  public void performLogout(boolean paramBoolean)
  {
    this.notificationsPreferences.edit().clear().commit();
    this.emojiPreferences.edit().putLong("lastGifLoadTime", 0L).putLong("lastStickersLoadTime", 0L).putLong("lastStickersLoadTimeMask", 0L).putLong("lastStickersLoadTimeFavs", 0L).commit();
    this.mainPreferences.edit().remove("gifhint").commit();
    if (paramBoolean)
    {
      unregistedPush();
      TLRPC.TL_auth_logOut localTL_auth_logOut = new TLRPC.TL_auth_logOut();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_auth_logOut, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          ConnectionsManager.getInstance(MessagesController.this.currentAccount).cleanup();
        }
      });
    }
    for (;;)
    {
      UserConfig.getInstance(this.currentAccount).clearConfig();
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.appDidLogout, new Object[0]);
      MessagesStorage.getInstance(this.currentAccount).cleanup(false);
      cleanup();
      ContactsController.getInstance(this.currentAccount).deleteUnknownAppAccounts();
      return;
      ConnectionsManager.getInstance(this.currentAccount).cleanup();
    }
  }
  
  public void pinChannelMessage(TLRPC.Chat paramChat, int paramInt, boolean paramBoolean)
  {
    TLRPC.TL_channels_updatePinnedMessage localTL_channels_updatePinnedMessage = new TLRPC.TL_channels_updatePinnedMessage();
    localTL_channels_updatePinnedMessage.channel = getInputChannel(paramChat);
    localTL_channels_updatePinnedMessage.id = paramInt;
    if (!paramBoolean) {}
    for (paramBoolean = true;; paramBoolean = false)
    {
      localTL_channels_updatePinnedMessage.silent = paramBoolean;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_updatePinnedMessage, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            paramAnonymousTLObject = (TLRPC.Updates)paramAnonymousTLObject;
            MessagesController.this.processUpdates(paramAnonymousTLObject, false);
          }
        }
      });
      return;
    }
  }
  
  /* Error */
  public boolean pinDialog(long paramLong1, boolean paramBoolean, TLRPC.InputPeer paramInputPeer, final long paramLong2)
  {
    // Byte code:
    //   0: lload_1
    //   1: l2i
    //   2: istore 9
    //   4: aload_0
    //   5: getfield 719	org/telegram/messenger/MessagesController:dialogs_dict	Landroid/util/LongSparseArray;
    //   8: lload_1
    //   9: invokevirtual 1202	android/util/LongSparseArray:get	(J)Ljava/lang/Object;
    //   12: checkcast 1204	org/telegram/tgnet/TLRPC$TL_dialog
    //   15: astore 12
    //   17: aload 12
    //   19: ifnull +12 -> 31
    //   22: aload 12
    //   24: getfield 2553	org/telegram/tgnet/TLRPC$TL_dialog:pinned	Z
    //   27: iload_3
    //   28: if_icmpne +12 -> 40
    //   31: aload 12
    //   33: ifnull +5 -> 38
    //   36: iconst_1
    //   37: ireturn
    //   38: iconst_0
    //   39: ireturn
    //   40: aload 12
    //   42: iload_3
    //   43: putfield 2553	org/telegram/tgnet/TLRPC$TL_dialog:pinned	Z
    //   46: iload_3
    //   47: ifeq +193 -> 240
    //   50: iconst_0
    //   51: istore 8
    //   53: iconst_0
    //   54: istore 7
    //   56: iload 7
    //   58: aload_0
    //   59: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   62: invokevirtual 1275	java/util/ArrayList:size	()I
    //   65: if_icmpge +25 -> 90
    //   68: aload_0
    //   69: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   72: iload 7
    //   74: invokevirtual 1278	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   77: checkcast 1204	org/telegram/tgnet/TLRPC$TL_dialog
    //   80: astore 10
    //   82: aload 10
    //   84: getfield 2553	org/telegram/tgnet/TLRPC$TL_dialog:pinned	Z
    //   87: ifne +132 -> 219
    //   90: aload 12
    //   92: iload 8
    //   94: iconst_1
    //   95: iadd
    //   96: putfield 2676	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
    //   99: aload_0
    //   100: aconst_null
    //   101: invokevirtual 3581	org/telegram/messenger/MessagesController:sortDialogs	(Landroid/util/SparseArray;)V
    //   104: iload_3
    //   105: ifne +41 -> 146
    //   108: aload_0
    //   109: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   112: aload_0
    //   113: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   116: invokevirtual 1275	java/util/ArrayList:size	()I
    //   119: iconst_1
    //   120: isub
    //   121: invokevirtual 1278	java/util/ArrayList:get	(I)Ljava/lang/Object;
    //   124: aload 12
    //   126: if_acmpne +20 -> 146
    //   129: aload_0
    //   130: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   133: aload_0
    //   134: getfield 704	org/telegram/messenger/MessagesController:dialogs	Ljava/util/ArrayList;
    //   137: invokevirtual 1275	java/util/ArrayList:size	()I
    //   140: iconst_1
    //   141: isub
    //   142: invokevirtual 1349	java/util/ArrayList:remove	(I)Ljava/lang/Object;
    //   145: pop
    //   146: aload_0
    //   147: getfield 866	org/telegram/messenger/MessagesController:currentAccount	I
    //   150: invokestatic 1261	org/telegram/messenger/NotificationCenter:getInstance	(I)Lorg/telegram/messenger/NotificationCenter;
    //   153: getstatic 1550	org/telegram/messenger/NotificationCenter:dialogsNeedReload	I
    //   156: iconst_0
    //   157: anewarray 4	java/lang/Object
    //   160: invokevirtual 1268	org/telegram/messenger/NotificationCenter:postNotificationName	(I[Ljava/lang/Object;)V
    //   163: iload 9
    //   165: ifeq +196 -> 361
    //   168: lload 5
    //   170: ldc2_w 3582
    //   173: lcmp
    //   174: ifeq +187 -> 361
    //   177: new 3585	org/telegram/tgnet/TLRPC$TL_messages_toggleDialogPin
    //   180: dup
    //   181: invokespecial 3586	org/telegram/tgnet/TLRPC$TL_messages_toggleDialogPin:<init>	()V
    //   184: astore 13
    //   186: aload 13
    //   188: iload_3
    //   189: putfield 3587	org/telegram/tgnet/TLRPC$TL_messages_toggleDialogPin:pinned	Z
    //   192: aload 4
    //   194: astore 10
    //   196: aload 4
    //   198: ifnonnull +11 -> 209
    //   201: aload_0
    //   202: iload 9
    //   204: invokevirtual 1358	org/telegram/messenger/MessagesController:getInputPeer	(I)Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   207: astore 10
    //   209: aload 10
    //   211: instanceof 1953
    //   214: ifeq +35 -> 249
    //   217: iconst_0
    //   218: ireturn
    //   219: aload 10
    //   221: getfield 2676	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
    //   224: iload 8
    //   226: invokestatic 1445	java/lang/Math:max	(II)I
    //   229: istore 8
    //   231: iload 7
    //   233: iconst_1
    //   234: iadd
    //   235: istore 7
    //   237: goto -181 -> 56
    //   240: aload 12
    //   242: iconst_0
    //   243: putfield 2676	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
    //   246: goto -147 -> 99
    //   249: new 1891	org/telegram/tgnet/TLRPC$TL_inputDialogPeer
    //   252: dup
    //   253: invokespecial 1892	org/telegram/tgnet/TLRPC$TL_inputDialogPeer:<init>	()V
    //   256: astore 4
    //   258: aload 4
    //   260: aload 10
    //   262: putfield 1893	org/telegram/tgnet/TLRPC$TL_inputDialogPeer:peer	Lorg/telegram/tgnet/TLRPC$InputPeer;
    //   265: aload 13
    //   267: aload 4
    //   269: putfield 3590	org/telegram/tgnet/TLRPC$TL_messages_toggleDialogPin:peer	Lorg/telegram/tgnet/TLRPC$InputDialogPeer;
    //   272: lload 5
    //   274: lconst_0
    //   275: lcmp
    //   276: ifne +117 -> 393
    //   279: aconst_null
    //   280: astore 11
    //   282: new 2659	org/telegram/tgnet/NativeByteBuffer
    //   285: dup
    //   286: aload 10
    //   288: invokevirtual 2662	org/telegram/tgnet/TLRPC$InputPeer:getObjectSize	()I
    //   291: bipush 16
    //   293: iadd
    //   294: invokespecial 2663	org/telegram/tgnet/NativeByteBuffer:<init>	(I)V
    //   297: astore 4
    //   299: aload 4
    //   301: iconst_1
    //   302: invokevirtual 2666	org/telegram/tgnet/NativeByteBuffer:writeInt32	(I)V
    //   305: aload 4
    //   307: lload_1
    //   308: invokevirtual 2669	org/telegram/tgnet/NativeByteBuffer:writeInt64	(J)V
    //   311: aload 4
    //   313: iload_3
    //   314: invokevirtual 2673	org/telegram/tgnet/NativeByteBuffer:writeBool	(Z)V
    //   317: aload 10
    //   319: aload 4
    //   321: invokevirtual 2683	org/telegram/tgnet/TLRPC$InputPeer:serializeToStream	(Lorg/telegram/tgnet/AbstractSerializedData;)V
    //   324: aload_0
    //   325: getfield 866	org/telegram/messenger/MessagesController:currentAccount	I
    //   328: invokestatic 877	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   331: aload 4
    //   333: invokevirtual 2687	org/telegram/messenger/MessagesStorage:createPendingTask	(Lorg/telegram/tgnet/NativeByteBuffer;)J
    //   336: lstore 5
    //   338: aload_0
    //   339: getfield 866	org/telegram/messenger/MessagesController:currentAccount	I
    //   342: invokestatic 1229	org/telegram/tgnet/ConnectionsManager:getInstance	(I)Lorg/telegram/tgnet/ConnectionsManager;
    //   345: aload 13
    //   347: new 92	org/telegram/messenger/MessagesController$117
    //   350: dup
    //   351: aload_0
    //   352: lload 5
    //   354: invokespecial 3591	org/telegram/messenger/MessagesController$117:<init>	(Lorg/telegram/messenger/MessagesController;J)V
    //   357: invokevirtual 1382	org/telegram/tgnet/ConnectionsManager:sendRequest	(Lorg/telegram/tgnet/TLObject;Lorg/telegram/tgnet/RequestDelegate;)I
    //   360: pop
    //   361: aload_0
    //   362: getfield 866	org/telegram/messenger/MessagesController:currentAccount	I
    //   365: invokestatic 877	org/telegram/messenger/MessagesStorage:getInstance	(I)Lorg/telegram/messenger/MessagesStorage;
    //   368: lload_1
    //   369: aload 12
    //   371: getfield 2676	org/telegram/tgnet/TLRPC$TL_dialog:pinnedNum	I
    //   374: invokevirtual 3594	org/telegram/messenger/MessagesStorage:setDialogPinned	(JI)V
    //   377: iconst_1
    //   378: ireturn
    //   379: astore 10
    //   381: aload 11
    //   383: astore 4
    //   385: aload 10
    //   387: invokestatic 2693	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   390: goto -66 -> 324
    //   393: goto -55 -> 338
    //   396: astore 10
    //   398: goto -13 -> 385
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	401	0	this	MessagesController
    //   0	401	1	paramLong1	long
    //   0	401	3	paramBoolean	boolean
    //   0	401	4	paramInputPeer	TLRPC.InputPeer
    //   0	401	5	paramLong2	long
    //   54	182	7	i	int
    //   51	179	8	j	int
    //   2	201	9	k	int
    //   80	238	10	localObject1	Object
    //   379	7	10	localException1	Exception
    //   396	1	10	localException2	Exception
    //   280	102	11	localObject2	Object
    //   15	355	12	localTL_dialog	TLRPC.TL_dialog
    //   184	162	13	localTL_messages_toggleDialogPin	org.telegram.tgnet.TLRPC.TL_messages_toggleDialogPin
    // Exception table:
    //   from	to	target	type
    //   282	299	379	java/lang/Exception
    //   299	324	396	java/lang/Exception
  }
  
  public void processChatInfo(int paramInt, final TLRPC.ChatFull paramChatFull, final ArrayList<TLRPC.User> paramArrayList, final boolean paramBoolean1, boolean paramBoolean2, final boolean paramBoolean3, final MessageObject paramMessageObject)
  {
    if ((paramBoolean1) && (paramInt > 0) && (!paramBoolean3)) {
      loadFullChat(paramInt, 0, paramBoolean2);
    }
    if (paramChatFull != null) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MessagesController.this.putUsers(paramArrayList, paramBoolean1);
          if (paramChatFull.stickerset != null) {
            DataQuery.getInstance(MessagesController.this.currentAccount).getGroupStickerSetById(paramChatFull.stickerset);
          }
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { paramChatFull, Integer.valueOf(0), Boolean.valueOf(paramBoolean3), paramMessageObject });
        }
      });
    }
  }
  
  public void processDialogsUpdate(final TLRPC.messages_Dialogs parammessages_Dialogs, ArrayList<TLRPC.EncryptedChat> paramArrayList)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final LongSparseArray localLongSparseArray1 = new LongSparseArray();
        final LongSparseArray localLongSparseArray2 = new LongSparseArray();
        Object localObject1 = new SparseArray(parammessages_Dialogs.users.size());
        SparseArray localSparseArray = new SparseArray(parammessages_Dialogs.chats.size());
        final LongSparseArray localLongSparseArray3 = new LongSparseArray();
        int i = 0;
        Object localObject2;
        while (i < parammessages_Dialogs.users.size())
        {
          localObject2 = (TLRPC.User)parammessages_Dialogs.users.get(i);
          ((SparseArray)localObject1).put(((TLRPC.User)localObject2).id, localObject2);
          i += 1;
        }
        i = 0;
        while (i < parammessages_Dialogs.chats.size())
        {
          localObject2 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
          localSparseArray.put(((TLRPC.Chat)localObject2).id, localObject2);
          i += 1;
        }
        i = 0;
        Object localObject3;
        if (i < parammessages_Dialogs.messages.size())
        {
          localObject2 = (TLRPC.Message)parammessages_Dialogs.messages.get(i);
          if (((TLRPC.Message)localObject2).to_id.channel_id != 0)
          {
            localObject3 = (TLRPC.Chat)localSparseArray.get(((TLRPC.Message)localObject2).to_id.channel_id);
            if ((localObject3 == null) || (!((TLRPC.Chat)localObject3).left)) {
              break label277;
            }
          }
          for (;;)
          {
            i += 1;
            break;
            if (((TLRPC.Message)localObject2).to_id.chat_id != 0)
            {
              localObject3 = (TLRPC.Chat)localSparseArray.get(((TLRPC.Message)localObject2).to_id.chat_id);
              if ((localObject3 != null) && (((TLRPC.Chat)localObject3).migrated_to != null)) {}
            }
            else
            {
              label277:
              localObject2 = new MessageObject(MessagesController.this.currentAccount, (TLRPC.Message)localObject2, (SparseArray)localObject1, localSparseArray, false);
              localLongSparseArray2.put(((MessageObject)localObject2).getDialogId(), localObject2);
            }
          }
        }
        i = 0;
        if (i < parammessages_Dialogs.dialogs.size())
        {
          localObject3 = (TLRPC.TL_dialog)parammessages_Dialogs.dialogs.get(i);
          if (((TLRPC.TL_dialog)localObject3).id == 0L)
          {
            if (((TLRPC.TL_dialog)localObject3).peer.user_id != 0) {
              ((TLRPC.TL_dialog)localObject3).id = ((TLRPC.TL_dialog)localObject3).peer.user_id;
            }
          }
          else
          {
            label377:
            if (!DialogObject.isChannel((TLRPC.TL_dialog)localObject3)) {
              break label477;
            }
            localObject1 = (TLRPC.Chat)localSparseArray.get(-(int)((TLRPC.TL_dialog)localObject3).id);
            if ((localObject1 == null) || (!((TLRPC.Chat)localObject1).left)) {
              break label513;
            }
          }
          for (;;)
          {
            i += 1;
            break;
            if (((TLRPC.TL_dialog)localObject3).peer.chat_id != 0)
            {
              ((TLRPC.TL_dialog)localObject3).id = (-((TLRPC.TL_dialog)localObject3).peer.chat_id);
              break label377;
            }
            if (((TLRPC.TL_dialog)localObject3).peer.channel_id == 0) {
              break label377;
            }
            ((TLRPC.TL_dialog)localObject3).id = (-((TLRPC.TL_dialog)localObject3).peer.channel_id);
            break label377;
            label477:
            if ((int)((TLRPC.TL_dialog)localObject3).id < 0)
            {
              localObject1 = (TLRPC.Chat)localSparseArray.get(-(int)((TLRPC.TL_dialog)localObject3).id);
              if ((localObject1 != null) && (((TLRPC.Chat)localObject1).migrated_to != null)) {}
            }
            else
            {
              label513:
              if (((TLRPC.TL_dialog)localObject3).last_message_date == 0)
              {
                localObject1 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject3).id);
                if (localObject1 != null) {
                  ((TLRPC.TL_dialog)localObject3).last_message_date = ((MessageObject)localObject1).messageOwner.date;
                }
              }
              localLongSparseArray1.put(((TLRPC.TL_dialog)localObject3).id, localObject3);
              localLongSparseArray3.put(((TLRPC.TL_dialog)localObject3).id, Integer.valueOf(((TLRPC.TL_dialog)localObject3).unread_count));
              localObject2 = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject3).id));
              localObject1 = localObject2;
              if (localObject2 == null) {
                localObject1 = Integer.valueOf(0);
              }
              MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject3).id), Integer.valueOf(Math.max(((Integer)localObject1).intValue(), ((TLRPC.TL_dialog)localObject3).read_inbox_max_id)));
              localObject2 = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject3).id));
              localObject1 = localObject2;
              if (localObject2 == null) {
                localObject1 = Integer.valueOf(0);
              }
              MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject3).id), Integer.valueOf(Math.max(((Integer)localObject1).intValue(), ((TLRPC.TL_dialog)localObject3).read_outbox_max_id)));
            }
          }
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.this.putUsers(MessagesController.73.this.val$dialogsRes.users, true);
            MessagesController.this.putChats(MessagesController.73.this.val$dialogsRes.chats, true);
            int i = 0;
            if (i < localLongSparseArray1.size())
            {
              long l = localLongSparseArray1.keyAt(i);
              Object localObject1 = (TLRPC.TL_dialog)localLongSparseArray1.valueAt(i);
              Object localObject3 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l);
              Object localObject2;
              if (localObject3 == null)
              {
                localObject2 = MessagesController.this;
                ((MessagesController)localObject2).nextDialogsCacheOffset += 1;
                MessagesController.this.dialogs_dict.put(l, localObject1);
                localObject1 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject1).id);
                MessagesController.this.dialogMessage.put(l, localObject1);
                if ((localObject1 != null) && (((MessageObject)localObject1).messageOwner.to_id.channel_id == 0))
                {
                  MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject1).getId(), localObject1);
                  if (((MessageObject)localObject1).messageOwner.random_id != 0L) {
                    MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject1).messageOwner.random_id, localObject1);
                  }
                }
              }
              for (;;)
              {
                i += 1;
                break;
                ((TLRPC.TL_dialog)localObject3).unread_count = ((TLRPC.TL_dialog)localObject1).unread_count;
                if (((TLRPC.TL_dialog)localObject3).unread_mentions_count != ((TLRPC.TL_dialog)localObject1).unread_mentions_count)
                {
                  ((TLRPC.TL_dialog)localObject3).unread_mentions_count = ((TLRPC.TL_dialog)localObject1).unread_mentions_count;
                  if (MessagesController.this.createdDialogMainThreadIds.contains(Long.valueOf(((TLRPC.TL_dialog)localObject3).id))) {
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateMentionsCount, new Object[] { Long.valueOf(((TLRPC.TL_dialog)localObject3).id), Integer.valueOf(((TLRPC.TL_dialog)localObject3).unread_mentions_count) });
                  }
                }
                localObject2 = (MessageObject)MessagesController.this.dialogMessage.get(l);
                if ((localObject2 == null) || (((TLRPC.TL_dialog)localObject3).top_message > 0))
                {
                  if (((localObject2 != null) && (((MessageObject)localObject2).deleted)) || (((TLRPC.TL_dialog)localObject1).top_message > ((TLRPC.TL_dialog)localObject3).top_message))
                  {
                    MessagesController.this.dialogs_dict.put(l, localObject1);
                    localObject3 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject1).id);
                    MessagesController.this.dialogMessage.put(l, localObject3);
                    if ((localObject3 != null) && (((MessageObject)localObject3).messageOwner.to_id.channel_id == 0))
                    {
                      MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject3).getId(), localObject3);
                      if (((MessageObject)localObject3).messageOwner.random_id != 0L) {
                        MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject3).messageOwner.random_id, localObject3);
                      }
                    }
                    if (localObject2 != null)
                    {
                      MessagesController.this.dialogMessagesByIds.remove(((MessageObject)localObject2).getId());
                      if (((MessageObject)localObject2).messageOwner.random_id != 0L) {
                        MessagesController.this.dialogMessagesByRandomIds.remove(((MessageObject)localObject2).messageOwner.random_id);
                      }
                    }
                    if (localObject3 == null) {
                      MessagesController.this.checkLastDialogMessage((TLRPC.TL_dialog)localObject1, null, 0L);
                    }
                  }
                }
                else
                {
                  localObject3 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject1).id);
                  if ((((MessageObject)localObject2).deleted) || (localObject3 == null) || (((MessageObject)localObject3).messageOwner.date > ((MessageObject)localObject2).messageOwner.date))
                  {
                    MessagesController.this.dialogs_dict.put(l, localObject1);
                    MessagesController.this.dialogMessage.put(l, localObject3);
                    if ((localObject3 != null) && (((MessageObject)localObject3).messageOwner.to_id.channel_id == 0))
                    {
                      MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject3).getId(), localObject3);
                      if (((MessageObject)localObject3).messageOwner.random_id != 0L) {
                        MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject3).messageOwner.random_id, localObject3);
                      }
                    }
                    MessagesController.this.dialogMessagesByIds.remove(((MessageObject)localObject2).getId());
                    if (((MessageObject)localObject2).messageOwner.random_id != 0L) {
                      MessagesController.this.dialogMessagesByRandomIds.remove(((MessageObject)localObject2).messageOwner.random_id);
                    }
                  }
                }
              }
            }
            MessagesController.this.dialogs.clear();
            i = 0;
            int j = MessagesController.this.dialogs_dict.size();
            while (i < j)
            {
              MessagesController.this.dialogs.add(MessagesController.this.dialogs_dict.valueAt(i));
              i += 1;
            }
            MessagesController.this.sortDialogs(null);
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
            NotificationsController.getInstance(MessagesController.this.currentAccount).processDialogsUpdateRead(localLongSparseArray3);
          }
        });
      }
    });
  }
  
  public void processDialogsUpdateRead(final LongSparseArray<Integer> paramLongSparseArray1, final LongSparseArray<Integer> paramLongSparseArray2)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i;
        long l;
        TLRPC.TL_dialog localTL_dialog;
        if (paramLongSparseArray1 != null)
        {
          i = 0;
          while (i < paramLongSparseArray1.size())
          {
            l = paramLongSparseArray1.keyAt(i);
            localTL_dialog = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l);
            if (localTL_dialog != null) {
              localTL_dialog.unread_count = ((Integer)paramLongSparseArray1.valueAt(i)).intValue();
            }
            i += 1;
          }
        }
        if (paramLongSparseArray2 != null)
        {
          i = 0;
          while (i < paramLongSparseArray2.size())
          {
            l = paramLongSparseArray2.keyAt(i);
            localTL_dialog = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l);
            if (localTL_dialog != null)
            {
              localTL_dialog.unread_mentions_count = ((Integer)paramLongSparseArray2.valueAt(i)).intValue();
              if (MessagesController.this.createdDialogMainThreadIds.contains(Long.valueOf(localTL_dialog.id))) {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateMentionsCount, new Object[] { Long.valueOf(localTL_dialog.id), Integer.valueOf(localTL_dialog.unread_mentions_count) });
              }
            }
            i += 1;
          }
        }
        NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(256) });
        if (paramLongSparseArray1 != null) {
          NotificationsController.getInstance(MessagesController.this.currentAccount).processDialogsUpdateRead(paramLongSparseArray1);
        }
      }
    });
  }
  
  public void processLoadedBlockedUsers(final ArrayList<Integer> paramArrayList, final ArrayList<TLRPC.User> paramArrayList1, final boolean paramBoolean)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        if (paramArrayList1 != null) {
          MessagesController.this.putUsers(paramArrayList1, paramBoolean);
        }
        MessagesController.this.loadingBlockedUsers = false;
        if ((paramArrayList.isEmpty()) && (paramBoolean) && (!UserConfig.getInstance(MessagesController.this.currentAccount).blockedUsersLoaded))
        {
          MessagesController.this.getBlockedUsers(false);
          return;
        }
        if (!paramBoolean)
        {
          UserConfig.getInstance(MessagesController.this.currentAccount).blockedUsersLoaded = true;
          UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
        }
        MessagesController.this.blockedUsers = paramArrayList;
        NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
      }
    });
  }
  
  public void processLoadedChannelAdmins(final ArrayList<Integer> paramArrayList, final int paramInt, final boolean paramBoolean)
  {
    Collections.sort(paramArrayList);
    if (!paramBoolean) {
      MessagesStorage.getInstance(this.currentAccount).putChannelAdmins(paramInt, paramArrayList);
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        MessagesController.this.loadingChannelAdmins.delete(paramInt);
        MessagesController.this.channelAdmins.put(paramInt, paramArrayList);
        if (paramBoolean) {
          MessagesController.this.loadChannelAdmins(paramInt, false);
        }
      }
    });
  }
  
  public void processLoadedDeleteTask(final int paramInt1, final ArrayList<Integer> paramArrayList, int paramInt2)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        MessagesController.access$4002(MessagesController.this, false);
        if (paramArrayList != null)
        {
          MessagesController.access$4102(MessagesController.this, paramInt1);
          MessagesController.access$3902(MessagesController.this, paramArrayList);
          if (MessagesController.this.currentDeleteTaskRunnable != null)
          {
            Utilities.stageQueue.cancelRunnable(MessagesController.this.currentDeleteTaskRunnable);
            MessagesController.access$4302(MessagesController.this, null);
          }
          if (!MessagesController.this.checkDeletingTask(false))
          {
            MessagesController.access$4302(MessagesController.this, new Runnable()
            {
              public void run()
              {
                MessagesController.this.checkDeletingTask(true);
              }
            });
            int i = ConnectionsManager.getInstance(MessagesController.this.currentAccount).getCurrentTime();
            Utilities.stageQueue.postRunnable(MessagesController.this.currentDeleteTaskRunnable, Math.abs(i - MessagesController.this.currentDeletingTaskTime) * 1000L);
          }
          return;
        }
        MessagesController.access$4102(MessagesController.this, 0);
        MessagesController.access$3902(MessagesController.this, null);
      }
    });
  }
  
  public void processLoadedDialogs(final TLRPC.messages_Dialogs parammessages_Dialogs, final ArrayList<TLRPC.EncryptedChat> paramArrayList, final int paramInt1, final int paramInt2, final int paramInt3, final boolean paramBoolean1, final boolean paramBoolean2, final boolean paramBoolean3)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (!MessagesController.this.firstGettingTask)
        {
          MessagesController.this.getNewDeleteTask(null, 0);
          MessagesController.this.firstGettingTask = true;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("loaded loadType " + paramInt3 + " count " + parammessages_Dialogs.dialogs.size());
        }
        if ((paramInt3 == 1) && (parammessages_Dialogs.dialogs.size() == 0))
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.putUsers(MessagesController.69.this.val$dialogsRes.users, true);
              MessagesController.this.loadingDialogs = false;
              if (MessagesController.69.this.val$resetEnd)
              {
                MessagesController.this.dialogsEndReached = false;
                MessagesController.this.serverDialogsEndReached = false;
              }
              for (;;)
              {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                return;
                if (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId == Integer.MAX_VALUE)
                {
                  MessagesController.this.dialogsEndReached = true;
                  MessagesController.this.serverDialogsEndReached = true;
                }
                else
                {
                  MessagesController.this.loadDialogs(0, MessagesController.69.this.val$count, false);
                }
              }
            }
          });
          return;
        }
        final LongSparseArray localLongSparseArray1 = new LongSparseArray();
        final LongSparseArray localLongSparseArray2 = new LongSparseArray();
        SparseArray localSparseArray2 = new SparseArray();
        final SparseArray localSparseArray1 = new SparseArray();
        int i = 0;
        Object localObject1;
        while (i < parammessages_Dialogs.users.size())
        {
          localObject1 = (TLRPC.User)parammessages_Dialogs.users.get(i);
          localSparseArray2.put(((TLRPC.User)localObject1).id, localObject1);
          i += 1;
        }
        i = 0;
        while (i < parammessages_Dialogs.chats.size())
        {
          localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
          localSparseArray1.put(((TLRPC.Chat)localObject1).id, localObject1);
          i += 1;
        }
        if (paramInt3 == 1) {
          MessagesController.this.nextDialogsCacheOffset = (paramInt1 + paramInt2);
        }
        Object localObject2 = null;
        i = 0;
        Object localObject3;
        if (i < parammessages_Dialogs.messages.size())
        {
          localObject3 = (TLRPC.Message)parammessages_Dialogs.messages.get(i);
          if (localObject2 != null)
          {
            localObject1 = localObject2;
            if (((TLRPC.Message)localObject3).date >= ((TLRPC.Message)localObject2).date) {}
          }
          else
          {
            localObject1 = localObject3;
          }
          if (((TLRPC.Message)localObject3).to_id.channel_id != 0)
          {
            localObject2 = (TLRPC.Chat)localSparseArray1.get(((TLRPC.Message)localObject3).to_id.channel_id);
            if ((localObject2 == null) || (!((TLRPC.Chat)localObject2).left)) {}
          }
          for (;;)
          {
            i += 1;
            localObject2 = localObject1;
            break;
            if ((localObject2 != null) && (((TLRPC.Chat)localObject2).megagroup)) {
              ((TLRPC.Message)localObject3).flags |= 0x80000000;
            }
            do
            {
              do
              {
                localObject2 = new MessageObject(MessagesController.this.currentAccount, (TLRPC.Message)localObject3, localSparseArray2, localSparseArray1, false);
                localLongSparseArray2.put(((MessageObject)localObject2).getDialogId(), localObject2);
                break;
              } while (((TLRPC.Message)localObject3).to_id.chat_id == 0);
              localObject2 = (TLRPC.Chat)localSparseArray1.get(((TLRPC.Message)localObject3).to_id.chat_id);
            } while ((localObject2 == null) || (((TLRPC.Chat)localObject2).migrated_to == null));
          }
        }
        label758:
        final ArrayList localArrayList;
        int j;
        if ((!paramBoolean3) && (!paramBoolean2) && (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId != -1) && (paramInt3 == 0))
        {
          if ((localObject2 == null) || (((TLRPC.Message)localObject2).id == UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId)) {
            break label1162;
          }
          localObject1 = UserConfig.getInstance(MessagesController.this.currentAccount);
          ((UserConfig)localObject1).totalDialogsLoadCount += parammessages_Dialogs.dialogs.size();
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId = ((TLRPC.Message)localObject2).id;
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetDate = ((TLRPC.Message)localObject2).date;
          if (((TLRPC.Message)localObject2).to_id.channel_id == 0) {
            break label880;
          }
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = ((TLRPC.Message)localObject2).to_id.channel_id;
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = 0;
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = 0;
          i = 0;
          if (i < parammessages_Dialogs.chats.size())
          {
            localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
            if (((TLRPC.Chat)localObject1).id == UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId) {
              UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
            }
          }
          else
          {
            UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
          }
        }
        else
        {
          localArrayList = new ArrayList();
          j = 0;
          label783:
          if (j >= parammessages_Dialogs.dialogs.size()) {
            break label1614;
          }
          localObject3 = (TLRPC.TL_dialog)parammessages_Dialogs.dialogs.get(j);
          if ((((TLRPC.TL_dialog)localObject3).id == 0L) && (((TLRPC.TL_dialog)localObject3).peer != null))
          {
            if (((TLRPC.TL_dialog)localObject3).peer.user_id == 0) {
              break label1180;
            }
            ((TLRPC.TL_dialog)localObject3).id = ((TLRPC.TL_dialog)localObject3).peer.user_id;
          }
          label856:
          if (((TLRPC.TL_dialog)localObject3).id != 0L) {
            break label1238;
          }
        }
        for (;;)
        {
          j += 1;
          break label783;
          i += 1;
          break;
          label880:
          if (((TLRPC.Message)localObject2).to_id.chat_id != 0)
          {
            UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = ((TLRPC.Message)localObject2).to_id.chat_id;
            UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = 0;
            UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = 0;
            i = 0;
            for (;;)
            {
              if (i >= parammessages_Dialogs.chats.size()) {
                break label1019;
              }
              localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(i);
              if (((TLRPC.Chat)localObject1).id == UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId)
              {
                UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.Chat)localObject1).access_hash;
                break;
              }
              i += 1;
            }
            label1019:
            break label758;
          }
          if (((TLRPC.Message)localObject2).to_id.user_id == 0) {
            break label758;
          }
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId = ((TLRPC.Message)localObject2).to_id.user_id;
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChatId = 0;
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetChannelId = 0;
          i = 0;
          for (;;)
          {
            if (i >= parammessages_Dialogs.users.size()) {
              break label1160;
            }
            localObject1 = (TLRPC.User)parammessages_Dialogs.users.get(i);
            if (((TLRPC.User)localObject1).id == UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetUserId)
            {
              UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetAccess = ((TLRPC.User)localObject1).access_hash;
              break;
            }
            i += 1;
          }
          label1160:
          break label758;
          label1162:
          UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId = Integer.MAX_VALUE;
          break label758;
          label1180:
          if (((TLRPC.TL_dialog)localObject3).peer.chat_id != 0)
          {
            ((TLRPC.TL_dialog)localObject3).id = (-((TLRPC.TL_dialog)localObject3).peer.chat_id);
            break label856;
          }
          if (((TLRPC.TL_dialog)localObject3).peer.channel_id == 0) {
            break label856;
          }
          ((TLRPC.TL_dialog)localObject3).id = (-((TLRPC.TL_dialog)localObject3).peer.channel_id);
          break label856;
          label1238:
          if (((TLRPC.TL_dialog)localObject3).last_message_date == 0)
          {
            localObject1 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject3).id);
            if (localObject1 != null) {
              ((TLRPC.TL_dialog)localObject3).last_message_date = ((MessageObject)localObject1).messageOwner.date;
            }
          }
          i = 1;
          int m = 1;
          int k = 1;
          if (DialogObject.isChannel((TLRPC.TL_dialog)localObject3))
          {
            localObject1 = (TLRPC.Chat)localSparseArray1.get(-(int)((TLRPC.TL_dialog)localObject3).id);
            if (localObject1 != null)
            {
              i = k;
              if (!((TLRPC.Chat)localObject1).megagroup) {
                i = 0;
              }
              if (((TLRPC.Chat)localObject1).left) {}
            }
            else
            {
              MessagesController.this.channelsPts.put(-(int)((TLRPC.TL_dialog)localObject3).id, ((TLRPC.TL_dialog)localObject3).pts);
            }
          }
          else
          {
            do
            {
              do
              {
                do
                {
                  localLongSparseArray1.put(((TLRPC.TL_dialog)localObject3).id, localObject3);
                  if ((i != 0) && (paramInt3 == 1) && ((((TLRPC.TL_dialog)localObject3).read_outbox_max_id == 0) || (((TLRPC.TL_dialog)localObject3).read_inbox_max_id == 0)) && (((TLRPC.TL_dialog)localObject3).top_message != 0)) {
                    localArrayList.add(localObject3);
                  }
                  localObject2 = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject3).id));
                  localObject1 = localObject2;
                  if (localObject2 == null) {
                    localObject1 = Integer.valueOf(0);
                  }
                  MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject3).id), Integer.valueOf(Math.max(((Integer)localObject1).intValue(), ((TLRPC.TL_dialog)localObject3).read_inbox_max_id)));
                  localObject2 = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(((TLRPC.TL_dialog)localObject3).id));
                  localObject1 = localObject2;
                  if (localObject2 == null) {
                    localObject1 = Integer.valueOf(0);
                  }
                  MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(((TLRPC.TL_dialog)localObject3).id), Integer.valueOf(Math.max(((Integer)localObject1).intValue(), ((TLRPC.TL_dialog)localObject3).read_outbox_max_id)));
                  break;
                  i = m;
                } while ((int)((TLRPC.TL_dialog)localObject3).id >= 0);
                localObject1 = (TLRPC.Chat)localSparseArray1.get(-(int)((TLRPC.TL_dialog)localObject3).id);
                i = m;
              } while (localObject1 == null);
              i = m;
            } while (((TLRPC.Chat)localObject1).migrated_to == null);
          }
        }
        label1614:
        if (paramInt3 != 1)
        {
          ImageLoader.saveMessagesThumbs(parammessages_Dialogs.messages);
          i = 0;
          while (i < parammessages_Dialogs.messages.size())
          {
            TLRPC.Message localMessage = (TLRPC.Message)parammessages_Dialogs.messages.get(i);
            if ((localMessage.action instanceof TLRPC.TL_messageActionChatDeleteUser))
            {
              localObject1 = (TLRPC.User)localSparseArray2.get(localMessage.action.user_id);
              if ((localObject1 != null) && (((TLRPC.User)localObject1).bot))
              {
                localMessage.reply_markup = new TLRPC.TL_replyKeyboardHide();
                localMessage.flags |= 0x40;
              }
            }
            if (((localMessage.action instanceof TLRPC.TL_messageActionChatMigrateTo)) || ((localMessage.action instanceof TLRPC.TL_messageActionChannelCreate)))
            {
              localMessage.unread = false;
              localMessage.media_unread = false;
              i += 1;
            }
            else
            {
              if (localMessage.out)
              {
                localObject1 = MessagesController.this.dialogs_read_outbox_max;
                label1789:
                localObject3 = (Integer)((ConcurrentHashMap)localObject1).get(Long.valueOf(localMessage.dialog_id));
                localObject2 = localObject3;
                if (localObject3 == null)
                {
                  localObject2 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(localMessage.out, localMessage.dialog_id));
                  ((ConcurrentHashMap)localObject1).put(Long.valueOf(localMessage.dialog_id), localObject2);
                }
                if (((Integer)localObject2).intValue() >= localMessage.id) {
                  break label1898;
                }
              }
              label1898:
              for (boolean bool = true;; bool = false)
              {
                localMessage.unread = bool;
                break;
                localObject1 = MessagesController.this.dialogs_read_inbox_max;
                break label1789;
              }
            }
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putDialogs(parammessages_Dialogs, false);
        }
        if (paramInt3 == 2)
        {
          localObject1 = (TLRPC.Chat)parammessages_Dialogs.chats.get(0);
          MessagesController.this.getChannelDifference(((TLRPC.Chat)localObject1).id);
          MessagesController.this.checkChannelInviter(((TLRPC.Chat)localObject1).id);
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (MessagesController.69.this.val$loadType != 1)
            {
              MessagesController.this.applyDialogsNotificationsSettings(MessagesController.69.this.val$dialogsRes.dialogs);
              if (!UserConfig.getInstance(MessagesController.this.currentAccount).draftsLoaded) {
                DataQuery.getInstance(MessagesController.this.currentAccount).loadDrafts();
              }
            }
            Object localObject1 = MessagesController.this;
            Object localObject2 = MessagesController.69.this.val$dialogsRes.users;
            if (MessagesController.69.this.val$loadType == 1)
            {
              bool = true;
              ((MessagesController)localObject1).putUsers((ArrayList)localObject2, bool);
              localObject1 = MessagesController.this;
              localObject2 = MessagesController.69.this.val$dialogsRes.chats;
              if (MessagesController.69.this.val$loadType != 1) {
                break label262;
              }
            }
            label262:
            for (boolean bool = true;; bool = false)
            {
              ((MessagesController)localObject1).putChats((ArrayList)localObject2, bool);
              if (MessagesController.69.this.val$encChats == null) {
                break label268;
              }
              i = 0;
              while (i < MessagesController.69.this.val$encChats.size())
              {
                localObject1 = (TLRPC.EncryptedChat)MessagesController.69.this.val$encChats.get(i);
                if (((localObject1 instanceof TLRPC.TL_encryptedChat)) && (AndroidUtilities.getMyLayerVersion(((TLRPC.EncryptedChat)localObject1).layer) < 73)) {
                  SecretChatHelper.getInstance(MessagesController.this.currentAccount).sendNotifyLayerMessage((TLRPC.EncryptedChat)localObject1, null);
                }
                MessagesController.this.putEncryptedChat((TLRPC.EncryptedChat)localObject1, true);
                i += 1;
              }
              bool = false;
              break;
            }
            label268:
            if (!MessagesController.69.this.val$migrate) {
              MessagesController.this.loadingDialogs = false;
            }
            int i = 0;
            int m;
            label355:
            long l;
            if ((MessagesController.69.this.val$migrate) && (!MessagesController.this.dialogs.isEmpty()))
            {
              j = ((TLRPC.TL_dialog)MessagesController.this.dialogs.get(MessagesController.this.dialogs.size() - 1)).last_message_date;
              m = 0;
              if (m >= localLongSparseArray1.size()) {
                break label1170;
              }
              l = localLongSparseArray1.keyAt(m);
              localObject2 = (TLRPC.TL_dialog)localLongSparseArray1.valueAt(m);
              if ((!MessagesController.69.this.val$migrate) || (j == 0) || (((TLRPC.TL_dialog)localObject2).last_message_date >= j)) {
                break label433;
              }
              k = i;
            }
            for (;;)
            {
              m += 1;
              i = k;
              break label355;
              j = 0;
              break;
              label433:
              Object localObject3 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l);
              if ((MessagesController.69.this.val$loadType != 1) && ((((TLRPC.TL_dialog)localObject2).draft instanceof TLRPC.TL_draftMessage))) {
                DataQuery.getInstance(MessagesController.this.currentAccount).saveDraft(((TLRPC.TL_dialog)localObject2).id, ((TLRPC.TL_dialog)localObject2).draft, null, false);
              }
              if (localObject3 == null)
              {
                i = 1;
                MessagesController.this.dialogs_dict.put(l, localObject2);
                localObject1 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject2).id);
                MessagesController.this.dialogMessage.put(l, localObject1);
                k = i;
                if (localObject1 != null)
                {
                  k = i;
                  if (((MessageObject)localObject1).messageOwner.to_id.channel_id == 0)
                  {
                    MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject1).getId(), localObject1);
                    k = i;
                    if (((MessageObject)localObject1).messageOwner.random_id != 0L)
                    {
                      MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject1).messageOwner.random_id, localObject1);
                      k = i;
                    }
                  }
                }
              }
              else
              {
                if (MessagesController.69.this.val$loadType != 1) {
                  ((TLRPC.TL_dialog)localObject3).notify_settings = ((TLRPC.TL_dialog)localObject2).notify_settings;
                }
                ((TLRPC.TL_dialog)localObject3).pinned = ((TLRPC.TL_dialog)localObject2).pinned;
                ((TLRPC.TL_dialog)localObject3).pinnedNum = ((TLRPC.TL_dialog)localObject2).pinnedNum;
                localObject1 = (MessageObject)MessagesController.this.dialogMessage.get(l);
                if (((localObject1 != null) && (((MessageObject)localObject1).deleted)) || (localObject1 == null) || (((TLRPC.TL_dialog)localObject3).top_message > 0))
                {
                  k = i;
                  if (((TLRPC.TL_dialog)localObject2).top_message >= ((TLRPC.TL_dialog)localObject3).top_message)
                  {
                    MessagesController.this.dialogs_dict.put(l, localObject2);
                    localObject2 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject2).id);
                    MessagesController.this.dialogMessage.put(l, localObject2);
                    if ((localObject2 != null) && (((MessageObject)localObject2).messageOwner.to_id.channel_id == 0))
                    {
                      MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject2).getId(), localObject2);
                      if ((localObject2 != null) && (((MessageObject)localObject2).messageOwner.random_id != 0L)) {
                        MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject2).messageOwner.random_id, localObject2);
                      }
                    }
                    k = i;
                    if (localObject1 != null)
                    {
                      MessagesController.this.dialogMessagesByIds.remove(((MessageObject)localObject1).getId());
                      k = i;
                      if (((MessageObject)localObject1).messageOwner.random_id != 0L)
                      {
                        MessagesController.this.dialogMessagesByRandomIds.remove(((MessageObject)localObject1).messageOwner.random_id);
                        k = i;
                      }
                    }
                  }
                }
                else
                {
                  localObject3 = (MessageObject)localLongSparseArray2.get(((TLRPC.TL_dialog)localObject2).id);
                  if ((!((MessageObject)localObject1).deleted) && (localObject3 != null))
                  {
                    k = i;
                    if (((MessageObject)localObject3).messageOwner.date <= ((MessageObject)localObject1).messageOwner.date) {}
                  }
                  else
                  {
                    MessagesController.this.dialogs_dict.put(l, localObject2);
                    MessagesController.this.dialogMessage.put(l, localObject3);
                    if ((localObject3 != null) && (((MessageObject)localObject3).messageOwner.to_id.channel_id == 0))
                    {
                      MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject3).getId(), localObject3);
                      if ((localObject3 != null) && (((MessageObject)localObject3).messageOwner.random_id != 0L)) {
                        MessagesController.this.dialogMessagesByRandomIds.put(((MessageObject)localObject3).messageOwner.random_id, localObject3);
                      }
                    }
                    MessagesController.this.dialogMessagesByIds.remove(((MessageObject)localObject1).getId());
                    k = i;
                    if (((MessageObject)localObject1).messageOwner.random_id != 0L)
                    {
                      MessagesController.this.dialogMessagesByRandomIds.remove(((MessageObject)localObject1).messageOwner.random_id);
                      k = i;
                    }
                  }
                }
              }
            }
            label1170:
            MessagesController.this.dialogs.clear();
            int j = 0;
            int k = MessagesController.this.dialogs_dict.size();
            while (j < k)
            {
              MessagesController.this.dialogs.add(MessagesController.this.dialogs_dict.valueAt(j));
              j += 1;
            }
            localObject2 = MessagesController.this;
            if (MessagesController.69.this.val$migrate)
            {
              localObject1 = localSparseArray1;
              ((MessagesController)localObject2).sortDialogs((SparseArray)localObject1);
              if ((MessagesController.69.this.val$loadType != 2) && (!MessagesController.69.this.val$migrate))
              {
                localObject1 = MessagesController.this;
                if (((MessagesController.69.this.val$dialogsRes.dialogs.size() != 0) && (MessagesController.69.this.val$dialogsRes.dialogs.size() == MessagesController.69.this.val$count)) || (MessagesController.69.this.val$loadType != 0)) {
                  break label1782;
                }
                bool = true;
                label1353:
                ((MessagesController)localObject1).dialogsEndReached = bool;
                if (!MessagesController.69.this.val$fromCache)
                {
                  localObject1 = MessagesController.this;
                  if (((MessagesController.69.this.val$dialogsRes.dialogs.size() != 0) && (MessagesController.69.this.val$dialogsRes.dialogs.size() == MessagesController.69.this.val$count)) || (MessagesController.69.this.val$loadType != 0)) {
                    break label1788;
                  }
                  bool = true;
                  label1431:
                  ((MessagesController)localObject1).serverDialogsEndReached = bool;
                }
              }
              if ((!MessagesController.69.this.val$fromCache) && (!MessagesController.69.this.val$migrate) && (UserConfig.getInstance(MessagesController.this.currentAccount).totalDialogsLoadCount < 400) && (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId != -1) && (UserConfig.getInstance(MessagesController.this.currentAccount).dialogsLoadOffsetId != Integer.MAX_VALUE)) {
                MessagesController.this.loadDialogs(0, 100, false);
              }
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
              if (!MessagesController.69.this.val$migrate) {
                break label1794;
              }
              UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetId = MessagesController.69.this.val$offset;
              UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
              MessagesController.access$6002(MessagesController.this, false);
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
            }
            for (;;)
            {
              MessagesController.this.migrateDialogs(UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetId, UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetDate, UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetUserId, UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChatId, UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetChannelId, UserConfig.getInstance(MessagesController.this.currentAccount).migrateOffsetAccess);
              if (!localArrayList.isEmpty()) {
                MessagesController.this.reloadDialogsReadValue(localArrayList, 0L);
              }
              return;
              localObject1 = null;
              break;
              label1782:
              bool = false;
              break label1353;
              label1788:
              bool = false;
              break label1431;
              label1794:
              MessagesController.this.generateUpdateMessage();
              if ((i == 0) && (MessagesController.69.this.val$loadType == 1)) {
                MessagesController.this.loadDialogs(0, MessagesController.69.this.val$count, false);
              }
            }
          }
        });
      }
    });
  }
  
  public void processLoadedMessages(final TLRPC.messages_Messages parammessages_Messages, final long paramLong, final int paramInt1, final int paramInt2, final int paramInt3, boolean paramBoolean1, final int paramInt4, final int paramInt5, final int paramInt6, final int paramInt7, final int paramInt8, final int paramInt9, final boolean paramBoolean2, final boolean paramBoolean3, final int paramInt10, final boolean paramBoolean4, final int paramInt11)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("processLoadedMessages size " + parammessages_Messages.messages.size() + " in chat " + paramLong + " count " + paramInt1 + " max_id " + paramInt2 + " cache " + paramBoolean1 + " guid " + paramInt4 + " load_type " + paramInt9 + " last_message_id " + paramInt6 + " isChannel " + paramBoolean2 + " index " + paramInt10 + " firstUnread " + paramInt5 + " unread_count " + paramInt7 + " last_date " + paramInt8 + " queryFromServer " + paramBoolean4);
    }
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        boolean bool2 = false;
        boolean bool5 = false;
        boolean bool4 = false;
        boolean bool3 = bool4;
        boolean bool1;
        if ((parammessages_Messages instanceof TLRPC.TL_messages_channelMessages))
        {
          j = -(int)paramLong;
          bool1 = bool5;
          if (MessagesController.this.channelsPts.get(j) == 0)
          {
            bool1 = bool5;
            if (MessagesStorage.getInstance(MessagesController.this.currentAccount).getChannelPtsSync(j) == 0)
            {
              MessagesController.this.channelsPts.put(j, parammessages_Messages.pts);
              bool1 = true;
              if ((MessagesController.this.needShortPollChannels.indexOfKey(j) < 0) || (MessagesController.this.shortPollChannels.indexOfKey(j) >= 0)) {
                break label257;
              }
              MessagesController.this.getChannelDifference(j, 2, 0L, null);
            }
          }
          i = 0;
        }
        Object localObject1;
        for (;;)
        {
          bool2 = bool1;
          bool3 = bool4;
          if (i < parammessages_Messages.chats.size())
          {
            localObject1 = (TLRPC.Chat)parammessages_Messages.chats.get(i);
            if (((TLRPC.Chat)localObject1).id == j)
            {
              bool3 = ((TLRPC.Chat)localObject1).megagroup;
              bool2 = bool1;
            }
          }
          else
          {
            i = (int)paramLong;
            j = (int)(paramLong >> 32);
            if (!paramInt1) {
              ImageLoader.saveMessagesThumbs(parammessages_Messages.messages);
            }
            if ((j == 1) || (i == 0) || (!paramInt1) || (parammessages_Messages.messages.size() != 0)) {
              break label275;
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                MessagesController localMessagesController = MessagesController.this;
                long l = MessagesController.62.this.val$dialog_id;
                int j = MessagesController.62.this.val$count;
                if ((MessagesController.62.this.val$load_type == 2) && (MessagesController.62.this.val$queryFromServer)) {}
                for (int i = MessagesController.62.this.val$first_unread;; i = MessagesController.62.this.val$max_id)
                {
                  localMessagesController.loadMessages(l, j, i, MessagesController.62.this.val$offset_date, false, 0, MessagesController.62.this.val$classGuid, MessagesController.62.this.val$load_type, MessagesController.62.this.val$last_message_id, MessagesController.62.this.val$isChannel, MessagesController.62.this.val$loadIndex, MessagesController.62.this.val$first_unread, MessagesController.62.this.val$unread_count, MessagesController.62.this.val$last_date, MessagesController.62.this.val$queryFromServer, MessagesController.62.this.val$mentionsCount);
                  return;
                }
              }
            });
            return;
            label257:
            MessagesController.this.getChannelDifference(j);
            break;
          }
          i += 1;
        }
        label275:
        SparseArray localSparseArray1 = new SparseArray();
        SparseArray localSparseArray2 = new SparseArray();
        int i = 0;
        while (i < parammessages_Messages.users.size())
        {
          localObject1 = (TLRPC.User)parammessages_Messages.users.get(i);
          localSparseArray1.put(((TLRPC.User)localObject1).id, localObject1);
          i += 1;
        }
        i = 0;
        while (i < parammessages_Messages.chats.size())
        {
          localObject1 = (TLRPC.Chat)parammessages_Messages.chats.get(i);
          localSparseArray2.put(((TLRPC.Chat)localObject1).id, localObject1);
          i += 1;
        }
        int j = parammessages_Messages.messages.size();
        Object localObject2;
        if (!paramInt1)
        {
          localObject2 = (Integer)MessagesController.this.dialogs_read_inbox_max.get(Long.valueOf(paramLong));
          localObject1 = localObject2;
          if (localObject2 == null)
          {
            localObject1 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(false, paramLong));
            MessagesController.this.dialogs_read_inbox_max.put(Long.valueOf(paramLong), localObject1);
          }
          localObject3 = (Integer)MessagesController.this.dialogs_read_outbox_max.get(Long.valueOf(paramLong));
          localObject2 = localObject3;
          if (localObject3 == null)
          {
            localObject2 = Integer.valueOf(MessagesStorage.getInstance(MessagesController.this.currentAccount).getDialogReadMax(true, paramLong));
            MessagesController.this.dialogs_read_outbox_max.put(Long.valueOf(paramLong), localObject2);
          }
          i = 0;
          while (i < j)
          {
            localObject4 = (TLRPC.Message)parammessages_Messages.messages.get(i);
            if (bool3) {
              ((TLRPC.Message)localObject4).flags |= 0x80000000;
            }
            if ((((TLRPC.Message)localObject4).action instanceof TLRPC.TL_messageActionChatDeleteUser))
            {
              localObject3 = (TLRPC.User)localSparseArray1.get(((TLRPC.Message)localObject4).action.user_id);
              if ((localObject3 != null) && (((TLRPC.User)localObject3).bot))
              {
                ((TLRPC.Message)localObject4).reply_markup = new TLRPC.TL_replyKeyboardHide();
                ((TLRPC.Message)localObject4).flags |= 0x40;
              }
            }
            if (((((TLRPC.Message)localObject4).action instanceof TLRPC.TL_messageActionChatMigrateTo)) || ((((TLRPC.Message)localObject4).action instanceof TLRPC.TL_messageActionChannelCreate)))
            {
              ((TLRPC.Message)localObject4).unread = false;
              ((TLRPC.Message)localObject4).media_unread = false;
              i += 1;
            }
            else
            {
              if (((TLRPC.Message)localObject4).out)
              {
                localObject3 = localObject2;
                label722:
                if (((Integer)localObject3).intValue() >= ((TLRPC.Message)localObject4).id) {
                  break label753;
                }
              }
              label753:
              for (bool1 = true;; bool1 = false)
              {
                ((TLRPC.Message)localObject4).unread = bool1;
                break;
                localObject3 = localObject1;
                break label722;
              }
            }
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(parammessages_Messages, paramLong, paramBoolean4, paramInt3, bool2);
        }
        final Object localObject3 = new ArrayList();
        final Object localObject4 = new ArrayList();
        final HashMap localHashMap = new HashMap();
        i = 0;
        if (i < j)
        {
          TLRPC.Message localMessage = (TLRPC.Message)parammessages_Messages.messages.get(i);
          localMessage.dialog_id = paramLong;
          MessageObject localMessageObject = new MessageObject(MessagesController.this.currentAccount, localMessage, localSparseArray1, localSparseArray2, true);
          ((ArrayList)localObject3).add(localMessageObject);
          if (paramInt1)
          {
            if (!(localMessage.media instanceof TLRPC.TL_messageMediaUnsupported)) {
              break label969;
            }
            if ((localMessage.media.bytes != null) && ((localMessage.media.bytes.length == 0) || ((localMessage.media.bytes.length == 1) && (localMessage.media.bytes[0] < 76)))) {
              ((ArrayList)localObject4).add(Integer.valueOf(localMessage.id));
            }
          }
          for (;;)
          {
            i += 1;
            break;
            label969:
            if ((localMessage.media instanceof TLRPC.TL_messageMediaWebPage)) {
              if (((localMessage.media.webpage instanceof TLRPC.TL_webPagePending)) && (localMessage.media.webpage.date <= ConnectionsManager.getInstance(MessagesController.this.currentAccount).getCurrentTime()))
              {
                ((ArrayList)localObject4).add(Integer.valueOf(localMessage.id));
              }
              else if ((localMessage.media.webpage instanceof TLRPC.TL_webPageUrlPending))
              {
                localObject2 = (ArrayList)localHashMap.get(localMessage.media.webpage.url);
                localObject1 = localObject2;
                if (localObject2 == null)
                {
                  localObject1 = new ArrayList();
                  localHashMap.put(localMessage.media.webpage.url, localObject1);
                }
                ((ArrayList)localObject1).add(localMessageObject);
              }
            }
          }
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.this.putUsers(MessagesController.62.this.val$messagesRes.users, MessagesController.62.this.val$isCache);
            MessagesController.this.putChats(MessagesController.62.this.val$messagesRes.chats, MessagesController.62.this.val$isCache);
            int i = Integer.MAX_VALUE;
            int j = i;
            if (MessagesController.62.this.val$queryFromServer)
            {
              j = i;
              if (MessagesController.62.this.val$load_type == 2)
              {
                int k = 0;
                for (;;)
                {
                  j = i;
                  if (k >= MessagesController.62.this.val$messagesRes.messages.size()) {
                    break;
                  }
                  TLRPC.Message localMessage = (TLRPC.Message)MessagesController.62.this.val$messagesRes.messages.get(k);
                  j = i;
                  if (!localMessage.out)
                  {
                    j = i;
                    if (localMessage.id > MessagesController.62.this.val$first_unread)
                    {
                      j = i;
                      if (localMessage.id < i) {
                        j = localMessage.id;
                      }
                    }
                  }
                  k += 1;
                  i = j;
                }
              }
            }
            i = j;
            if (j == Integer.MAX_VALUE) {
              i = MessagesController.62.this.val$first_unread;
            }
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messagesDidLoaded, new Object[] { Long.valueOf(MessagesController.62.this.val$dialog_id), Integer.valueOf(MessagesController.62.this.val$count), localObject3, Boolean.valueOf(MessagesController.62.this.val$isCache), Integer.valueOf(i), Integer.valueOf(MessagesController.62.this.val$last_message_id), Integer.valueOf(MessagesController.62.this.val$unread_count), Integer.valueOf(MessagesController.62.this.val$last_date), Integer.valueOf(MessagesController.62.this.val$load_type), Boolean.valueOf(MessagesController.62.this.val$isEnd), Integer.valueOf(MessagesController.62.this.val$classGuid), Integer.valueOf(MessagesController.62.this.val$loadIndex), Integer.valueOf(MessagesController.62.this.val$max_id), Integer.valueOf(MessagesController.62.this.val$mentionsCount) });
            if (!localObject4.isEmpty()) {
              MessagesController.this.reloadMessages(localObject4, MessagesController.62.this.val$dialog_id);
            }
            if (!localHashMap.isEmpty()) {
              MessagesController.this.reloadWebPages(MessagesController.62.this.val$dialog_id, localHashMap);
            }
          }
        });
      }
    });
  }
  
  public void processLoadedUserPhotos(final TLRPC.photos_Photos paramphotos_Photos, final int paramInt1, final int paramInt2, long paramLong, final boolean paramBoolean, final int paramInt3)
  {
    if (!paramBoolean)
    {
      MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(paramphotos_Photos.users, null, true, true);
      MessagesStorage.getInstance(this.currentAccount).putDialogPhotos(paramInt1, paramphotos_Photos);
    }
    while ((paramphotos_Photos != null) && (!paramphotos_Photos.photos.isEmpty()))
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MessagesController.this.putUsers(paramphotos_Photos.users, paramBoolean);
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogPhotosLoaded, new Object[] { Integer.valueOf(paramInt1), Integer.valueOf(paramInt2), Boolean.valueOf(paramBoolean), Integer.valueOf(paramInt3), paramphotos_Photos.photos });
        }
      });
      return;
    }
    loadDialogPhotos(paramInt1, paramInt2, paramLong, false, paramInt3);
  }
  
  protected void processNewChannelDifferenceParams(int paramInt1, int paramInt2, int paramInt3)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("processNewChannelDifferenceParams pts = " + paramInt1 + " pts_count = " + paramInt2 + " channeldId = " + paramInt3);
    }
    int j = this.channelsPts.get(paramInt3);
    int i = j;
    if (j == 0)
    {
      j = MessagesStorage.getInstance(this.currentAccount).getChannelPtsSync(paramInt3);
      i = j;
      if (j == 0) {
        i = 1;
      }
      this.channelsPts.put(paramInt3, i);
    }
    if (i + paramInt2 == paramInt1)
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("APPLY CHANNEL PTS");
      }
      this.channelsPts.put(paramInt3, paramInt1);
      MessagesStorage.getInstance(this.currentAccount).saveChannelPts(paramInt3, paramInt1);
    }
    while (i == paramInt1) {
      return;
    }
    long l = this.updatesStartWaitTimeChannels.get(paramInt3);
    if ((this.gettingDifferenceChannels.get(paramInt3)) || (l == 0L) || (Math.abs(System.currentTimeMillis() - l) <= 1500L))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ADD CHANNEL UPDATE TO QUEUE pts = " + paramInt1 + " pts_count = " + paramInt2);
      }
      if (l == 0L) {
        this.updatesStartWaitTimeChannels.put(paramInt3, System.currentTimeMillis());
      }
      UserActionUpdatesPts localUserActionUpdatesPts = new UserActionUpdatesPts(null);
      localUserActionUpdatesPts.pts = paramInt1;
      localUserActionUpdatesPts.pts_count = paramInt2;
      localUserActionUpdatesPts.chat_id = paramInt3;
      ArrayList localArrayList2 = (ArrayList)this.updatesQueueChannels.get(paramInt3);
      ArrayList localArrayList1 = localArrayList2;
      if (localArrayList2 == null)
      {
        localArrayList1 = new ArrayList();
        this.updatesQueueChannels.put(paramInt3, localArrayList1);
      }
      localArrayList1.add(localUserActionUpdatesPts);
      return;
    }
    getChannelDifference(paramInt3);
  }
  
  protected void processNewDifferenceParams(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (BuildVars.LOGS_ENABLED) {
      FileLog.d("processNewDifferenceParams seq = " + paramInt1 + " pts = " + paramInt2 + " date = " + paramInt3 + " pts_count = " + paramInt4);
    }
    if (paramInt2 != -1)
    {
      if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + paramInt4 != paramInt2) {
        break label266;
      }
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("APPLY PTS");
      }
      MessagesStorage.getInstance(this.currentAccount).setLastPtsValue(paramInt2);
      MessagesStorage.getInstance(this.currentAccount).saveDiffParams(MessagesStorage.getInstance(this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(this.currentAccount).getLastQtsValue());
    }
    label266:
    Object localObject;
    do
    {
      for (;;)
      {
        if (paramInt1 != -1)
        {
          if (MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + 1 != paramInt1) {
            break;
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("APPLY SEQ");
          }
          MessagesStorage.getInstance(this.currentAccount).setLastSeqValue(paramInt1);
          if (paramInt3 != -1) {
            MessagesStorage.getInstance(this.currentAccount).setLastDateValue(paramInt3);
          }
          MessagesStorage.getInstance(this.currentAccount).saveDiffParams(MessagesStorage.getInstance(this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(this.currentAccount).getLastQtsValue());
        }
        return;
        if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() != paramInt2) {
          if ((this.gettingDifference) || (this.updatesStartWaitTimePts == 0L) || (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500L))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("ADD UPDATE TO QUEUE pts = " + paramInt2 + " pts_count = " + paramInt4);
            }
            if (this.updatesStartWaitTimePts == 0L) {
              this.updatesStartWaitTimePts = System.currentTimeMillis();
            }
            localObject = new UserActionUpdatesPts(null);
            ((UserActionUpdatesPts)localObject).pts = paramInt2;
            ((UserActionUpdatesPts)localObject).pts_count = paramInt4;
            this.updatesQueuePts.add(localObject);
          }
          else
          {
            getDifference();
          }
        }
      }
    } while (MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() == paramInt1);
    if ((this.gettingDifference) || (this.updatesStartWaitTimeSeq == 0L) || (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeSeq) <= 1500L))
    {
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("ADD UPDATE TO QUEUE seq = " + paramInt1);
      }
      if (this.updatesStartWaitTimeSeq == 0L) {
        this.updatesStartWaitTimeSeq = System.currentTimeMillis();
      }
      localObject = new UserActionUpdatesSeq(null);
      ((UserActionUpdatesSeq)localObject).seq = paramInt1;
      this.updatesQueueSeq.add(localObject);
      return;
    }
    getDifference();
  }
  
  public boolean processUpdateArray(final ArrayList<TLRPC.Update> paramArrayList, final ArrayList<TLRPC.User> paramArrayList1, final ArrayList<TLRPC.Chat> paramArrayList2, final boolean paramBoolean)
  {
    if (paramArrayList.isEmpty())
    {
      if ((paramArrayList1 != null) || (paramArrayList2 != null)) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.this.putUsers(paramArrayList1, false);
            MessagesController.this.putChats(paramArrayList2, false);
          }
        });
      }
      return true;
    }
    long l3 = System.currentTimeMillis();
    boolean bool1 = false;
    final Object localObject1 = null;
    final Object localObject6 = null;
    final Object localObject3 = null;
    Object localObject2 = null;
    final Object localObject16 = null;
    final Object localObject18 = null;
    final Object localObject9 = null;
    final Object localObject8 = null;
    final Object localObject7 = null;
    final Object localObject13 = null;
    final Object localObject10 = null;
    final Object localObject14 = null;
    final Object localObject17 = null;
    Object localObject4 = null;
    Object localObject12 = null;
    final Object localObject15 = null;
    final int k = 1;
    Object localObject5;
    final int m;
    Object localObject11;
    if (paramArrayList1 != null)
    {
      localObject5 = new ConcurrentHashMap();
      j = 0;
      m = paramArrayList1.size();
      for (;;)
      {
        localObject25 = localObject5;
        i = k;
        if (j >= m) {
          break;
        }
        localObject11 = (TLRPC.User)paramArrayList1.get(j);
        ((ConcurrentHashMap)localObject5).put(Integer.valueOf(((TLRPC.User)localObject11).id), localObject11);
        j += 1;
      }
    }
    int i = 0;
    Object localObject25 = this.users;
    if (paramArrayList2 != null)
    {
      localObject5 = new ConcurrentHashMap();
      k = 0;
      m = paramArrayList2.size();
      for (;;)
      {
        localObject26 = localObject5;
        j = i;
        if (k >= m) {
          break;
        }
        localObject11 = (TLRPC.Chat)paramArrayList2.get(k);
        ((ConcurrentHashMap)localObject5).put(Integer.valueOf(((TLRPC.Chat)localObject11).id), localObject11);
        k += 1;
      }
    }
    final int j = 0;
    Object localObject26 = this.chats;
    int n = j;
    if (paramBoolean) {
      n = 0;
    }
    if ((paramArrayList1 != null) || (paramArrayList2 != null)) {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MessagesController.this.putUsers(paramArrayList1, false);
          MessagesController.this.putChats(paramArrayList2, false);
        }
      });
    }
    j = 0;
    int i1 = 0;
    int i4 = paramArrayList.size();
    paramArrayList1 = (ArrayList<TLRPC.User>)localObject4;
    if (i1 < i4)
    {
      final Object localObject24 = (TLRPC.Update)paramArrayList.get(i1);
      if (BuildVars.LOGS_ENABLED) {
        FileLog.d("process update " + localObject24);
      }
      label616:
      label672:
      int i2;
      label941:
      label1112:
      label1207:
      label1276:
      label1290:
      boolean bool2;
      label1407:
      Object localObject34;
      label1495:
      Object localObject27;
      Object localObject28;
      Object localObject29;
      Object localObject30;
      Object localObject19;
      Object localObject31;
      Object localObject32;
      Object localObject20;
      Object localObject21;
      Object localObject22;
      Object localObject33;
      Object localObject23;
      if (((localObject24 instanceof TLRPC.TL_updateNewMessage)) || ((localObject24 instanceof TLRPC.TL_updateNewChannelMessage)))
      {
        if ((localObject24 instanceof TLRPC.TL_updateNewMessage))
        {
          localObject5 = ((TLRPC.TL_updateNewMessage)localObject24).message;
          paramArrayList2 = null;
          i = 0;
          k = 0;
          if (((TLRPC.Message)localObject5).to_id.channel_id == 0) {
            break label616;
          }
          m = ((TLRPC.Message)localObject5).to_id.channel_id;
        }
        for (;;)
        {
          if (m != 0)
          {
            paramArrayList2 = (TLRPC.Chat)((ConcurrentHashMap)localObject26).get(Integer.valueOf(m));
            localObject4 = paramArrayList2;
            if (paramArrayList2 == null) {
              localObject4 = getChat(Integer.valueOf(m));
            }
            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
            if (localObject4 == null)
            {
              paramArrayList2 = MessagesStorage.getInstance(this.currentAccount).getChatSync(m);
              putChat(paramArrayList2, true);
            }
          }
          i = j;
          if (n == 0) {
            break label1112;
          }
          if ((m == 0) || (paramArrayList2 != null)) {
            break label672;
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("not found chat " + m);
          }
          return false;
          paramArrayList2 = ((TLRPC.TL_updateNewChannelMessage)localObject24).message;
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d(localObject24 + " channelId = " + paramArrayList2.to_id.channel_id);
          }
          localObject5 = paramArrayList2;
          if (paramArrayList2.out) {
            break;
          }
          localObject5 = paramArrayList2;
          if (paramArrayList2.from_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            break;
          }
          paramArrayList2.out = true;
          localObject5 = paramArrayList2;
          break;
          if (((TLRPC.Message)localObject5).to_id.chat_id != 0)
          {
            m = ((TLRPC.Message)localObject5).to_id.chat_id;
          }
          else
          {
            m = i;
            if (((TLRPC.Message)localObject5).to_id.user_id != 0)
            {
              k = ((TLRPC.Message)localObject5).to_id.user_id;
              m = i;
            }
          }
        }
        int i5 = ((TLRPC.Message)localObject5).entities.size();
        i = 0;
        i2 = k;
        k = i;
        for (;;)
        {
          i = j;
          if (k >= i5 + 3) {
            break;
          }
          int i3 = 0;
          m = i3;
          i = i2;
          if (k != 0)
          {
            if (k != 1) {
              break label941;
            }
            i2 = ((TLRPC.Message)localObject5).from_id;
            m = i3;
            i = i2;
            if (((TLRPC.Message)localObject5).post)
            {
              m = 1;
              i = i2;
            }
          }
          i2 = j;
          if (i > 0)
          {
            localObject11 = (TLRPC.User)((ConcurrentHashMap)localObject25).get(Integer.valueOf(i));
            if (localObject11 != null)
            {
              localObject4 = localObject11;
              if (m == 0)
              {
                localObject4 = localObject11;
                if (!((TLRPC.User)localObject11).min) {}
              }
            }
            else
            {
              localObject4 = getUser(Integer.valueOf(i));
            }
            if (localObject4 != null)
            {
              localObject11 = localObject4;
              if (m == 0)
              {
                localObject11 = localObject4;
                if (!((TLRPC.User)localObject4).min) {}
              }
            }
            else
            {
              localObject11 = MessagesStorage.getInstance(this.currentAccount).getUserSync(i);
              localObject4 = localObject11;
              if (localObject11 != null)
              {
                localObject4 = localObject11;
                if (m == 0)
                {
                  localObject4 = localObject11;
                  if (((TLRPC.User)localObject11).min) {
                    localObject4 = null;
                  }
                }
              }
              putUser((TLRPC.User)localObject4, true);
              localObject11 = localObject4;
            }
            if (localObject11 == null)
            {
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("not found user " + i);
              }
              return false;
              if (k == 2)
              {
                if (((TLRPC.Message)localObject5).fwd_from != null) {}
                for (i = ((TLRPC.Message)localObject5).fwd_from.from_id;; i = 0)
                {
                  m = i3;
                  break;
                }
              }
              localObject4 = (TLRPC.MessageEntity)((TLRPC.Message)localObject5).entities.get(k - 3);
              if ((localObject4 instanceof TLRPC.TL_messageEntityMentionName)) {}
              for (i = ((TLRPC.TL_messageEntityMentionName)localObject4).user_id;; i = 0)
              {
                m = i3;
                break;
              }
            }
            i2 = j;
            if (k == 1)
            {
              i2 = j;
              if (((TLRPC.User)localObject11).status != null)
              {
                i2 = j;
                if (((TLRPC.User)localObject11).status.expires <= 0)
                {
                  this.onlinePrivacy.put(Integer.valueOf(i), Integer.valueOf(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()));
                  i2 = j | 0x4;
                }
              }
            }
          }
          k += 1;
          j = i2;
          i2 = i;
        }
        if ((paramArrayList2 != null) && (paramArrayList2.megagroup)) {
          ((TLRPC.Message)localObject5).flags |= 0x80000000;
        }
        if ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChatDeleteUser))
        {
          localObject4 = (TLRPC.User)((ConcurrentHashMap)localObject25).get(Integer.valueOf(((TLRPC.Message)localObject5).action.user_id));
          if ((localObject4 != null) && (((TLRPC.User)localObject4).bot))
          {
            ((TLRPC.Message)localObject5).reply_markup = new TLRPC.TL_replyKeyboardHide();
            ((TLRPC.Message)localObject5).flags |= 0x40;
          }
        }
        else
        {
          localObject24 = localObject2;
          if (localObject2 == null) {
            localObject24 = new ArrayList();
          }
          ((ArrayList)localObject24).add(localObject5);
          ImageLoader.saveMessageThumbs((TLRPC.Message)localObject5);
          j = UserConfig.getInstance(this.currentAccount).getClientUserId();
          if (((TLRPC.Message)localObject5).to_id.chat_id == 0) {
            break label1997;
          }
          ((TLRPC.Message)localObject5).dialog_id = (-((TLRPC.Message)localObject5).to_id.chat_id);
          if (!((TLRPC.Message)localObject5).out) {
            break label2069;
          }
          localObject2 = this.dialogs_read_outbox_max;
          localObject11 = (Integer)((ConcurrentHashMap)localObject2).get(Long.valueOf(((TLRPC.Message)localObject5).dialog_id));
          localObject4 = localObject11;
          if (localObject11 == null)
          {
            localObject4 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(((TLRPC.Message)localObject5).out, ((TLRPC.Message)localObject5).dialog_id));
            ((ConcurrentHashMap)localObject2).put(Long.valueOf(((TLRPC.Message)localObject5).dialog_id), localObject4);
          }
          if ((((Integer)localObject4).intValue() >= ((TLRPC.Message)localObject5).id) || ((paramArrayList2 != null) && (ChatObject.isNotInChat(paramArrayList2))) || ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChatMigrateTo)) || ((((TLRPC.Message)localObject5).action instanceof TLRPC.TL_messageActionChannelCreate))) {
            break label2078;
          }
          bool2 = true;
          ((TLRPC.Message)localObject5).unread = bool2;
          if (((TLRPC.Message)localObject5).dialog_id == j)
          {
            ((TLRPC.Message)localObject5).unread = false;
            ((TLRPC.Message)localObject5).media_unread = false;
            ((TLRPC.Message)localObject5).out = true;
          }
          localObject34 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject5, (AbstractMap)localObject25, (AbstractMap)localObject26, this.createdDialogIds.contains(Long.valueOf(((TLRPC.Message)localObject5).dialog_id)));
          if (((MessageObject)localObject34).type != 11) {
            break label2084;
          }
          j = i | 0x8;
          localObject2 = localObject1;
          if (localObject1 == null) {
            localObject2 = new LongSparseArray();
          }
          localObject1 = (ArrayList)((LongSparseArray)localObject2).get(((TLRPC.Message)localObject5).dialog_id);
          paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject1;
          if (localObject1 == null)
          {
            paramArrayList2 = new ArrayList();
            ((LongSparseArray)localObject2).put(((TLRPC.Message)localObject5).dialog_id, paramArrayList2);
          }
          paramArrayList2.add(localObject34);
          localObject27 = localObject18;
          localObject28 = localObject17;
          localObject29 = localObject14;
          localObject30 = localObject15;
          localObject19 = localObject10;
          localObject31 = localObject16;
          i = j;
          localObject32 = localObject13;
          localObject20 = localObject7;
          localObject21 = localObject9;
          localObject22 = localObject8;
          localObject4 = localObject2;
          localObject5 = localObject24;
          bool2 = bool1;
          localObject11 = localObject3;
          localObject33 = localObject12;
          paramArrayList2 = paramArrayList1;
          localObject23 = localObject6;
          if (!((MessageObject)localObject34).isOut())
          {
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject2;
            localObject5 = localObject24;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (((MessageObject)localObject34).isUnread())
            {
              localObject11 = localObject3;
              if (localObject3 == null) {
                localObject11 = new ArrayList();
              }
              ((ArrayList)localObject11).add(localObject34);
              localObject23 = localObject6;
              paramArrayList2 = paramArrayList1;
              localObject33 = localObject12;
              bool2 = bool1;
              localObject5 = localObject24;
              localObject4 = localObject2;
              localObject22 = localObject8;
              localObject21 = localObject9;
              localObject20 = localObject7;
              localObject32 = localObject13;
              i = j;
              localObject31 = localObject16;
              localObject19 = localObject10;
              localObject30 = localObject15;
              localObject29 = localObject14;
              localObject28 = localObject17;
              localObject27 = localObject18;
            }
          }
        }
      }
      for (;;)
      {
        i1 += 1;
        localObject18 = localObject27;
        localObject17 = localObject28;
        localObject14 = localObject29;
        localObject15 = localObject30;
        localObject10 = localObject19;
        localObject16 = localObject31;
        j = i;
        localObject13 = localObject32;
        localObject7 = localObject20;
        localObject9 = localObject21;
        localObject8 = localObject22;
        localObject1 = localObject4;
        localObject2 = localObject5;
        bool1 = bool2;
        localObject3 = localObject11;
        localObject12 = localObject33;
        paramArrayList1 = paramArrayList2;
        localObject6 = localObject23;
        break;
        if ((((TLRPC.Message)localObject5).from_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) || (((TLRPC.Message)localObject5).action.user_id != UserConfig.getInstance(this.currentAccount).getClientUserId())) {
          break label1207;
        }
        localObject27 = localObject18;
        localObject28 = localObject17;
        localObject29 = localObject14;
        localObject30 = localObject15;
        localObject19 = localObject10;
        localObject31 = localObject16;
        localObject32 = localObject13;
        localObject20 = localObject7;
        localObject21 = localObject9;
        localObject22 = localObject8;
        localObject4 = localObject1;
        localObject5 = localObject2;
        bool2 = bool1;
        localObject11 = localObject3;
        localObject33 = localObject12;
        paramArrayList2 = paramArrayList1;
        localObject23 = localObject6;
        continue;
        label1997:
        if (((TLRPC.Message)localObject5).to_id.channel_id != 0)
        {
          ((TLRPC.Message)localObject5).dialog_id = (-((TLRPC.Message)localObject5).to_id.channel_id);
          break label1276;
        }
        if (((TLRPC.Message)localObject5).to_id.user_id == j) {
          ((TLRPC.Message)localObject5).to_id.user_id = ((TLRPC.Message)localObject5).from_id;
        }
        ((TLRPC.Message)localObject5).dialog_id = ((TLRPC.Message)localObject5).to_id.user_id;
        break label1276;
        label2069:
        localObject2 = this.dialogs_read_inbox_max;
        break label1290;
        label2078:
        bool2 = false;
        break label1407;
        label2084:
        j = i;
        if (((MessageObject)localObject34).type != 10) {
          break label1495;
        }
        j = i | 0x10;
        break label1495;
        if ((localObject24 instanceof TLRPC.TL_updateReadMessagesContents))
        {
          localObject34 = (TLRPC.TL_updateReadMessagesContents)localObject24;
          localObject24 = localObject7;
          if (localObject7 == null) {
            localObject24 = new ArrayList();
          }
          k = 0;
          m = ((TLRPC.TL_updateReadMessagesContents)localObject34).messages.size();
          for (;;)
          {
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject24;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (k >= m) {
              break;
            }
            ((ArrayList)localObject24).add(Long.valueOf(((Integer)((TLRPC.TL_updateReadMessagesContents)localObject34).messages.get(k)).intValue()));
            k += 1;
          }
        }
        if ((localObject24 instanceof TLRPC.TL_updateChannelReadMessagesContents))
        {
          localObject34 = (TLRPC.TL_updateChannelReadMessagesContents)localObject24;
          localObject24 = localObject7;
          if (localObject7 == null) {
            localObject24 = new ArrayList();
          }
          k = 0;
          m = ((TLRPC.TL_updateChannelReadMessagesContents)localObject34).messages.size();
          for (;;)
          {
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject24;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (k >= m) {
              break;
            }
            ((ArrayList)localObject24).add(Long.valueOf(((Integer)((TLRPC.TL_updateChannelReadMessagesContents)localObject34).messages.get(k)).intValue() | ((TLRPC.TL_updateChannelReadMessagesContents)localObject34).channel_id << 32));
            k += 1;
          }
        }
        long l1;
        if ((localObject24 instanceof TLRPC.TL_updateReadHistoryInbox))
        {
          localObject5 = (TLRPC.TL_updateReadHistoryInbox)localObject24;
          localObject21 = localObject9;
          if (localObject9 == null) {
            localObject21 = new SparseLongArray();
          }
          if (((TLRPC.TL_updateReadHistoryInbox)localObject5).peer.chat_id != 0) {
            ((SparseLongArray)localObject21).put(-((TLRPC.TL_updateReadHistoryInbox)localObject5).peer.chat_id, ((TLRPC.TL_updateReadHistoryInbox)localObject5).max_id);
          }
          for (l1 = -((TLRPC.TL_updateReadHistoryInbox)localObject5).peer.chat_id;; l1 = ((TLRPC.TL_updateReadHistoryInbox)localObject5).peer.user_id)
          {
            localObject4 = (Integer)this.dialogs_read_inbox_max.get(Long.valueOf(l1));
            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
            if (localObject4 == null) {
              paramArrayList2 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(false, l1));
            }
            this.dialogs_read_inbox_max.put(Long.valueOf(l1), Integer.valueOf(Math.max(paramArrayList2.intValue(), ((TLRPC.TL_updateReadHistoryInbox)localObject5).max_id)));
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            break;
            ((SparseLongArray)localObject21).put(((TLRPC.TL_updateReadHistoryInbox)localObject5).peer.user_id, ((TLRPC.TL_updateReadHistoryInbox)localObject5).max_id);
          }
        }
        if ((localObject24 instanceof TLRPC.TL_updateReadHistoryOutbox))
        {
          localObject5 = (TLRPC.TL_updateReadHistoryOutbox)localObject24;
          localObject22 = localObject8;
          if (localObject8 == null) {
            localObject22 = new SparseLongArray();
          }
          if (((TLRPC.TL_updateReadHistoryOutbox)localObject5).peer.chat_id != 0) {
            ((SparseLongArray)localObject22).put(-((TLRPC.TL_updateReadHistoryOutbox)localObject5).peer.chat_id, ((TLRPC.TL_updateReadHistoryOutbox)localObject5).max_id);
          }
          for (l1 = -((TLRPC.TL_updateReadHistoryOutbox)localObject5).peer.chat_id;; l1 = ((TLRPC.TL_updateReadHistoryOutbox)localObject5).peer.user_id)
          {
            localObject4 = (Integer)this.dialogs_read_outbox_max.get(Long.valueOf(l1));
            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
            if (localObject4 == null) {
              paramArrayList2 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(true, l1));
            }
            this.dialogs_read_outbox_max.put(Long.valueOf(l1), Integer.valueOf(Math.max(paramArrayList2.intValue(), ((TLRPC.TL_updateReadHistoryOutbox)localObject5).max_id)));
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            break;
            ((SparseLongArray)localObject22).put(((TLRPC.TL_updateReadHistoryOutbox)localObject5).peer.user_id, ((TLRPC.TL_updateReadHistoryOutbox)localObject5).max_id);
          }
        }
        if ((localObject24 instanceof TLRPC.TL_updateDeleteMessages))
        {
          localObject5 = (TLRPC.TL_updateDeleteMessages)localObject24;
          localObject19 = localObject10;
          if (localObject10 == null) {
            localObject19 = new SparseArray();
          }
          localObject4 = (ArrayList)((SparseArray)localObject19).get(0);
          paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
          if (localObject4 == null)
          {
            paramArrayList2 = new ArrayList();
            ((SparseArray)localObject19).put(0, paramArrayList2);
          }
          paramArrayList2.addAll(((TLRPC.TL_updateDeleteMessages)localObject5).messages);
          localObject27 = localObject18;
          localObject28 = localObject17;
          localObject29 = localObject14;
          localObject30 = localObject15;
          localObject31 = localObject16;
          i = j;
          localObject32 = localObject13;
          localObject20 = localObject7;
          localObject21 = localObject9;
          localObject22 = localObject8;
          localObject4 = localObject1;
          localObject5 = localObject2;
          bool2 = bool1;
          localObject11 = localObject3;
          localObject33 = localObject12;
          paramArrayList2 = paramArrayList1;
          localObject23 = localObject6;
        }
        else
        {
          label3121:
          long l2;
          if (((localObject24 instanceof TLRPC.TL_updateUserTyping)) || ((localObject24 instanceof TLRPC.TL_updateChatUserTyping)))
          {
            if ((localObject24 instanceof TLRPC.TL_updateUserTyping))
            {
              paramArrayList2 = (TLRPC.TL_updateUserTyping)localObject24;
              k = paramArrayList2.user_id;
              localObject24 = paramArrayList2.action;
              m = 0;
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if (k == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                continue;
              }
              l2 = -m;
              l1 = l2;
              if (l2 == 0L) {
                l1 = k;
              }
              localObject4 = (ArrayList)this.printingUsers.get(Long.valueOf(l1));
              if (!(localObject24 instanceof TLRPC.TL_sendMessageCancelAction)) {
                break label3472;
              }
              bool2 = bool1;
              if (localObject4 != null)
              {
                i = 0;
                m = ((ArrayList)localObject4).size();
                label3272:
                boolean bool3 = bool1;
                if (i < m)
                {
                  if (((PrintingUser)((ArrayList)localObject4).get(i)).userId != k) {
                    break label3463;
                  }
                  ((ArrayList)localObject4).remove(i);
                  bool3 = true;
                }
                bool2 = bool3;
                if (((ArrayList)localObject4).isEmpty())
                {
                  this.printingUsers.remove(Long.valueOf(l1));
                  bool2 = bool3;
                }
              }
            }
            for (;;)
            {
              this.onlinePrivacy.put(Integer.valueOf(k), Integer.valueOf(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()));
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              break;
              paramArrayList2 = (TLRPC.TL_updateChatUserTyping)localObject24;
              m = paramArrayList2.chat_id;
              k = paramArrayList2.user_id;
              localObject24 = paramArrayList2.action;
              break label3121;
              label3463:
              i += 1;
              break label3272;
              label3472:
              paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
              if (localObject4 == null)
              {
                paramArrayList2 = new ArrayList();
                this.printingUsers.put(Long.valueOf(l1), paramArrayList2);
              }
              m = 0;
              localObject4 = paramArrayList2.iterator();
              do
              {
                i = m;
                bool2 = bool1;
                if (!((Iterator)localObject4).hasNext()) {
                  break;
                }
                localObject5 = (PrintingUser)((Iterator)localObject4).next();
              } while (((PrintingUser)localObject5).userId != k);
              i = 1;
              ((PrintingUser)localObject5).lastTime = l3;
              if (((PrintingUser)localObject5).action.getClass() != localObject24.getClass()) {
                bool1 = true;
              }
              ((PrintingUser)localObject5).action = ((TLRPC.SendMessageAction)localObject24);
              bool2 = bool1;
              if (i == 0)
              {
                localObject4 = new PrintingUser();
                ((PrintingUser)localObject4).userId = k;
                ((PrintingUser)localObject4).lastTime = l3;
                ((PrintingUser)localObject4).action = ((TLRPC.SendMessageAction)localObject24);
                paramArrayList2.add(localObject4);
                bool2 = true;
              }
            }
          }
          else if ((localObject24 instanceof TLRPC.TL_updateChatParticipants))
          {
            localObject4 = (TLRPC.TL_updateChatParticipants)localObject24;
            i = j | 0x20;
            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject17;
            if (localObject17 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add(((TLRPC.TL_updateChatParticipants)localObject4).participants);
            localObject27 = localObject18;
            localObject28 = paramArrayList2;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateUserStatus))
          {
            i = j | 0x4;
            paramArrayList2 = paramArrayList1;
            if (paramArrayList1 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add(localObject24);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateUserName))
          {
            i = j | 0x1;
            paramArrayList2 = paramArrayList1;
            if (paramArrayList1 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add(localObject24);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateUserPhoto))
          {
            paramArrayList2 = (TLRPC.TL_updateUserPhoto)localObject24;
            i = j | 0x2;
            MessagesStorage.getInstance(this.currentAccount).clearUserPhotos(paramArrayList2.user_id);
            paramArrayList2 = paramArrayList1;
            if (paramArrayList1 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add(localObject24);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateUserPhone))
          {
            i = j | 0x400;
            paramArrayList2 = paramArrayList1;
            if (paramArrayList1 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add(localObject24);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateContactRegistered))
          {
            localObject24 = (TLRPC.TL_updateContactRegistered)localObject24;
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (this.enableJoined)
            {
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if (((ConcurrentHashMap)localObject25).containsKey(Integer.valueOf(((TLRPC.TL_updateContactRegistered)localObject24).user_id)))
              {
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
                if (!MessagesStorage.getInstance(this.currentAccount).isDialogHasMessages(((TLRPC.TL_updateContactRegistered)localObject24).user_id))
                {
                  localObject11 = new TLRPC.TL_messageService();
                  ((TLRPC.TL_messageService)localObject11).action = new TLRPC.TL_messageActionUserJoined();
                  i = UserConfig.getInstance(this.currentAccount).getNewMessageId();
                  ((TLRPC.TL_messageService)localObject11).id = i;
                  ((TLRPC.TL_messageService)localObject11).local_id = i;
                  UserConfig.getInstance(this.currentAccount).saveConfig(false);
                  ((TLRPC.TL_messageService)localObject11).unread = false;
                  ((TLRPC.TL_messageService)localObject11).flags = 256;
                  ((TLRPC.TL_messageService)localObject11).date = ((TLRPC.TL_updateContactRegistered)localObject24).date;
                  ((TLRPC.TL_messageService)localObject11).from_id = ((TLRPC.TL_updateContactRegistered)localObject24).user_id;
                  ((TLRPC.TL_messageService)localObject11).to_id = new TLRPC.TL_peerUser();
                  ((TLRPC.TL_messageService)localObject11).to_id.user_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                  ((TLRPC.TL_messageService)localObject11).dialog_id = ((TLRPC.TL_updateContactRegistered)localObject24).user_id;
                  localObject5 = localObject2;
                  if (localObject2 == null) {
                    localObject5 = new ArrayList();
                  }
                  ((ArrayList)localObject5).add(localObject11);
                  localObject2 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject11, (AbstractMap)localObject25, (AbstractMap)localObject26, this.createdDialogIds.contains(Long.valueOf(((TLRPC.TL_messageService)localObject11).dialog_id)));
                  localObject4 = localObject1;
                  if (localObject1 == null) {
                    localObject4 = new LongSparseArray();
                  }
                  localObject1 = (ArrayList)((LongSparseArray)localObject4).get(((TLRPC.TL_messageService)localObject11).dialog_id);
                  paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject1;
                  if (localObject1 == null)
                  {
                    paramArrayList2 = new ArrayList();
                    ((LongSparseArray)localObject4).put(((TLRPC.TL_messageService)localObject11).dialog_id, paramArrayList2);
                  }
                  paramArrayList2.add(localObject2);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  paramArrayList2 = paramArrayList1;
                  localObject23 = localObject6;
                }
              }
            }
          }
          else if ((localObject24 instanceof TLRPC.TL_updateContactLink))
          {
            localObject34 = (TLRPC.TL_updateContactLink)localObject24;
            localObject24 = localObject15;
            if (localObject15 == null) {
              localObject24 = new ArrayList();
            }
            if ((((TLRPC.TL_updateContactLink)localObject34).my_link instanceof TLRPC.TL_contactLinkContact))
            {
              i = ((ArrayList)localObject24).indexOf(Integer.valueOf(-((TLRPC.TL_updateContactLink)localObject34).user_id));
              if (i != -1) {
                ((ArrayList)localObject24).remove(i);
              }
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject24;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if (!((ArrayList)localObject24).contains(Integer.valueOf(((TLRPC.TL_updateContactLink)localObject34).user_id)))
              {
                ((ArrayList)localObject24).add(Integer.valueOf(((TLRPC.TL_updateContactLink)localObject34).user_id));
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject24;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
            }
            else
            {
              i = ((ArrayList)localObject24).indexOf(Integer.valueOf(((TLRPC.TL_updateContactLink)localObject34).user_id));
              if (i != -1) {
                ((ArrayList)localObject24).remove(i);
              }
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject24;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if (!((ArrayList)localObject24).contains(Integer.valueOf(((TLRPC.TL_updateContactLink)localObject34).user_id)))
              {
                ((ArrayList)localObject24).add(Integer.valueOf(-((TLRPC.TL_updateContactLink)localObject34).user_id));
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject24;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
            }
          }
          else if ((localObject24 instanceof TLRPC.TL_updateNewEncryptedMessage))
          {
            localObject34 = SecretChatHelper.getInstance(this.currentAccount).decryptMessage(((TLRPC.TL_updateNewEncryptedMessage)localObject24).message);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (localObject34 != null)
            {
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if (!((ArrayList)localObject34).isEmpty())
              {
                l1 = ((TLRPC.TL_updateNewEncryptedMessage)localObject24).message.chat_id << 32;
                localObject24 = localObject1;
                if (localObject1 == null) {
                  localObject24 = new LongSparseArray();
                }
                paramArrayList2 = (ArrayList)((LongSparseArray)localObject24).get(l1);
                localObject1 = paramArrayList2;
                if (paramArrayList2 == null)
                {
                  localObject1 = new ArrayList();
                  ((LongSparseArray)localObject24).put(l1, localObject1);
                }
                k = 0;
                m = ((ArrayList)localObject34).size();
                for (;;)
                {
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject24;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  paramArrayList2 = paramArrayList1;
                  localObject23 = localObject6;
                  if (k >= m) {
                    break;
                  }
                  localObject4 = (TLRPC.Message)((ArrayList)localObject34).get(k);
                  ImageLoader.saveMessageThumbs((TLRPC.Message)localObject4);
                  paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject2;
                  if (localObject2 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject4);
                  localObject2 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject4, (AbstractMap)localObject25, (AbstractMap)localObject26, this.createdDialogIds.contains(Long.valueOf(l1)));
                  ((ArrayList)localObject1).add(localObject2);
                  localObject4 = localObject3;
                  if (localObject3 == null) {
                    localObject4 = new ArrayList();
                  }
                  ((ArrayList)localObject4).add(localObject2);
                  k += 1;
                  localObject2 = paramArrayList2;
                  localObject3 = localObject4;
                }
              }
            }
          }
          else if ((localObject24 instanceof TLRPC.TL_updateEncryptedChatTyping))
          {
            localObject34 = (TLRPC.TL_updateEncryptedChatTyping)localObject24;
            localObject24 = getEncryptedChatDB(((TLRPC.TL_updateEncryptedChatTyping)localObject34).chat_id, true);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
            if (localObject24 != null)
            {
              l1 = ((TLRPC.TL_updateEncryptedChatTyping)localObject34).chat_id << 32;
              localObject4 = (ArrayList)this.printingUsers.get(Long.valueOf(l1));
              paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
              if (localObject4 == null)
              {
                paramArrayList2 = new ArrayList();
                this.printingUsers.put(Long.valueOf(l1), paramArrayList2);
              }
              m = 0;
              i = 0;
              i2 = paramArrayList2.size();
              for (;;)
              {
                k = m;
                if (i < i2)
                {
                  localObject4 = (PrintingUser)paramArrayList2.get(i);
                  if (((PrintingUser)localObject4).userId == ((TLRPC.EncryptedChat)localObject24).user_id)
                  {
                    k = 1;
                    ((PrintingUser)localObject4).lastTime = l3;
                    ((PrintingUser)localObject4).action = new TLRPC.TL_sendMessageTypingAction();
                  }
                }
                else
                {
                  if (k == 0)
                  {
                    localObject4 = new PrintingUser();
                    ((PrintingUser)localObject4).userId = ((TLRPC.EncryptedChat)localObject24).user_id;
                    ((PrintingUser)localObject4).lastTime = l3;
                    ((PrintingUser)localObject4).action = new TLRPC.TL_sendMessageTypingAction();
                    paramArrayList2.add(localObject4);
                    bool1 = true;
                  }
                  this.onlinePrivacy.put(Integer.valueOf(((TLRPC.EncryptedChat)localObject24).user_id), Integer.valueOf(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()));
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  paramArrayList2 = paramArrayList1;
                  localObject23 = localObject6;
                  break;
                }
                i += 1;
              }
            }
          }
          else if ((localObject24 instanceof TLRPC.TL_updateEncryptedMessagesRead))
          {
            paramArrayList2 = (TLRPC.TL_updateEncryptedMessagesRead)localObject24;
            localObject4 = localObject13;
            if (localObject13 == null) {
              localObject4 = new SparseIntArray();
            }
            ((SparseIntArray)localObject4).put(paramArrayList2.chat_id, Math.max(paramArrayList2.max_date, paramArrayList2.date));
            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject12;
            if (localObject12 == null) {
              paramArrayList2 = new ArrayList();
            }
            paramArrayList2.add((TLRPC.TL_updateEncryptedMessagesRead)localObject24);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject4;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = paramArrayList2;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateChatParticipantAdd))
          {
            paramArrayList2 = (TLRPC.TL_updateChatParticipantAdd)localObject24;
            MessagesStorage.getInstance(this.currentAccount).updateChatInfo(paramArrayList2.chat_id, paramArrayList2.user_id, 0, paramArrayList2.inviter_id, paramArrayList2.version);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateChatParticipantDelete))
          {
            paramArrayList2 = (TLRPC.TL_updateChatParticipantDelete)localObject24;
            MessagesStorage.getInstance(this.currentAccount).updateChatInfo(paramArrayList2.chat_id, paramArrayList2.user_id, 1, 0, paramArrayList2.version);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else if (((localObject24 instanceof TLRPC.TL_updateDcOptions)) || ((localObject24 instanceof TLRPC.TL_updateConfig)))
          {
            ConnectionsManager.getInstance(this.currentAccount).updateDcSettings();
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else if ((localObject24 instanceof TLRPC.TL_updateEncryption))
          {
            SecretChatHelper.getInstance(this.currentAccount).processUpdateEncryption((TLRPC.TL_updateEncryption)localObject24, (ConcurrentHashMap)localObject25);
            localObject27 = localObject18;
            localObject28 = localObject17;
            localObject29 = localObject14;
            localObject30 = localObject15;
            localObject19 = localObject10;
            localObject31 = localObject16;
            i = j;
            localObject32 = localObject13;
            localObject20 = localObject7;
            localObject21 = localObject9;
            localObject22 = localObject8;
            localObject4 = localObject1;
            localObject5 = localObject2;
            bool2 = bool1;
            localObject11 = localObject3;
            localObject33 = localObject12;
            paramArrayList2 = paramArrayList1;
            localObject23 = localObject6;
          }
          else
          {
            if ((localObject24 instanceof TLRPC.TL_updateUserBlocked))
            {
              paramArrayList2 = (TLRPC.TL_updateUserBlocked)localObject24;
              if (paramArrayList2.blocked)
              {
                localObject4 = new ArrayList();
                ((ArrayList)localObject4).add(Integer.valueOf(paramArrayList2.user_id));
                MessagesStorage.getInstance(this.currentAccount).putBlockedUsers((ArrayList)localObject4, false);
              }
              for (;;)
              {
                MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
                {
                  public void run()
                  {
                    AndroidUtilities.runOnUIThread(new Runnable()
                    {
                      public void run()
                      {
                        if (MessagesController.129.this.val$finalUpdate.blocked) {
                          if (!MessagesController.this.blockedUsers.contains(Integer.valueOf(MessagesController.129.this.val$finalUpdate.user_id))) {
                            MessagesController.this.blockedUsers.add(Integer.valueOf(MessagesController.129.this.val$finalUpdate.user_id));
                          }
                        }
                        for (;;)
                        {
                          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
                          return;
                          MessagesController.this.blockedUsers.remove(Integer.valueOf(MessagesController.129.this.val$finalUpdate.user_id));
                        }
                      }
                    });
                  }
                });
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
                break;
                MessagesStorage.getInstance(this.currentAccount).deleteBlockedUser(paramArrayList2.user_id);
              }
            }
            if ((localObject24 instanceof TLRPC.TL_updateNotifySettings))
            {
              paramArrayList2 = paramArrayList1;
              if (paramArrayList1 == null) {
                paramArrayList2 = new ArrayList();
              }
              paramArrayList2.add(localObject24);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              localObject23 = localObject6;
            }
            else if ((localObject24 instanceof TLRPC.TL_updateServiceNotification))
            {
              localObject24 = (TLRPC.TL_updateServiceNotification)localObject24;
              if ((((TLRPC.TL_updateServiceNotification)localObject24).popup) && (((TLRPC.TL_updateServiceNotification)localObject24).message != null) && (((TLRPC.TL_updateServiceNotification)localObject24).message.length() > 0)) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.needShowAlert, new Object[] { Integer.valueOf(2), localObject24.message });
                  }
                });
              }
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
              if ((((TLRPC.TL_updateServiceNotification)localObject24).flags & 0x2) != 0)
              {
                localObject11 = new TLRPC.TL_message();
                i = UserConfig.getInstance(this.currentAccount).getNewMessageId();
                ((TLRPC.TL_message)localObject11).id = i;
                ((TLRPC.TL_message)localObject11).local_id = i;
                UserConfig.getInstance(this.currentAccount).saveConfig(false);
                ((TLRPC.TL_message)localObject11).unread = true;
                ((TLRPC.TL_message)localObject11).flags = 256;
                if (((TLRPC.TL_updateServiceNotification)localObject24).inbox_date != 0) {}
                for (((TLRPC.TL_message)localObject11).date = ((TLRPC.TL_updateServiceNotification)localObject24).inbox_date;; ((TLRPC.TL_message)localObject11).date = ((int)(System.currentTimeMillis() / 1000L)))
                {
                  ((TLRPC.TL_message)localObject11).from_id = 777000;
                  ((TLRPC.TL_message)localObject11).to_id = new TLRPC.TL_peerUser();
                  ((TLRPC.TL_message)localObject11).to_id.user_id = UserConfig.getInstance(this.currentAccount).getClientUserId();
                  ((TLRPC.TL_message)localObject11).dialog_id = 777000L;
                  if (((TLRPC.TL_updateServiceNotification)localObject24).media != null)
                  {
                    ((TLRPC.TL_message)localObject11).media = ((TLRPC.TL_updateServiceNotification)localObject24).media;
                    ((TLRPC.TL_message)localObject11).flags |= 0x200;
                  }
                  ((TLRPC.TL_message)localObject11).message = ((TLRPC.TL_updateServiceNotification)localObject24).message;
                  if (((TLRPC.TL_updateServiceNotification)localObject24).entities != null) {
                    ((TLRPC.TL_message)localObject11).entities = ((TLRPC.TL_updateServiceNotification)localObject24).entities;
                  }
                  localObject5 = localObject2;
                  if (localObject2 == null) {
                    localObject5 = new ArrayList();
                  }
                  ((ArrayList)localObject5).add(localObject11);
                  localObject2 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject11, (AbstractMap)localObject25, (AbstractMap)localObject26, this.createdDialogIds.contains(Long.valueOf(((TLRPC.TL_message)localObject11).dialog_id)));
                  localObject4 = localObject1;
                  if (localObject1 == null) {
                    localObject4 = new LongSparseArray();
                  }
                  localObject1 = (ArrayList)((LongSparseArray)localObject4).get(((TLRPC.TL_message)localObject11).dialog_id);
                  paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject1;
                  if (localObject1 == null)
                  {
                    paramArrayList2 = new ArrayList();
                    ((LongSparseArray)localObject4).put(((TLRPC.TL_message)localObject11).dialog_id, paramArrayList2);
                  }
                  paramArrayList2.add(localObject2);
                  localObject11 = localObject3;
                  if (localObject3 == null) {
                    localObject11 = new ArrayList();
                  }
                  ((ArrayList)localObject11).add(localObject2);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  bool2 = bool1;
                  localObject33 = localObject12;
                  paramArrayList2 = paramArrayList1;
                  localObject23 = localObject6;
                  break;
                }
              }
            }
            else if ((localObject24 instanceof TLRPC.TL_updateDialogPinned))
            {
              paramArrayList2 = paramArrayList1;
              if (paramArrayList1 == null) {
                paramArrayList2 = new ArrayList();
              }
              paramArrayList2.add(localObject24);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              localObject23 = localObject6;
            }
            else if ((localObject24 instanceof TLRPC.TL_updatePinnedDialogs))
            {
              paramArrayList2 = paramArrayList1;
              if (paramArrayList1 == null) {
                paramArrayList2 = new ArrayList();
              }
              paramArrayList2.add(localObject24);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              localObject23 = localObject6;
            }
            else if ((localObject24 instanceof TLRPC.TL_updatePrivacy))
            {
              paramArrayList2 = paramArrayList1;
              if (paramArrayList1 == null) {
                paramArrayList2 = new ArrayList();
              }
              paramArrayList2.add(localObject24);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              localObject23 = localObject6;
            }
            else if ((localObject24 instanceof TLRPC.TL_updateWebPage))
            {
              paramArrayList2 = (TLRPC.TL_updateWebPage)localObject24;
              localObject23 = localObject6;
              if (localObject6 == null) {
                localObject23 = new LongSparseArray();
              }
              ((LongSparseArray)localObject23).put(paramArrayList2.webpage.id, paramArrayList2.webpage);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
            }
            else if ((localObject24 instanceof TLRPC.TL_updateChannelWebPage))
            {
              paramArrayList2 = (TLRPC.TL_updateChannelWebPage)localObject24;
              localObject23 = localObject6;
              if (localObject6 == null) {
                localObject23 = new LongSparseArray();
              }
              ((LongSparseArray)localObject23).put(paramArrayList2.webpage.id, paramArrayList2.webpage);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
            }
            else if ((localObject24 instanceof TLRPC.TL_updateChannelTooLong))
            {
              localObject34 = (TLRPC.TL_updateChannelTooLong)localObject24;
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d(localObject24 + " channelId = " + ((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
              }
              i = this.channelsPts.get(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
              k = i;
              if (i == 0)
              {
                i = MessagesStorage.getInstance(this.currentAccount).getChannelPtsSync(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
                if (i != 0) {
                  break label8465;
                }
                localObject4 = (TLRPC.Chat)((ConcurrentHashMap)localObject26).get(Integer.valueOf(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id));
                if (localObject4 != null)
                {
                  paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
                  if (!((TLRPC.Chat)localObject4).min) {}
                }
                else
                {
                  paramArrayList2 = getChat(Integer.valueOf(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id));
                }
                if (paramArrayList2 != null)
                {
                  localObject4 = paramArrayList2;
                  if (!paramArrayList2.min) {}
                }
                else
                {
                  localObject4 = MessagesStorage.getInstance(this.currentAccount).getChatSync(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
                  putChat((TLRPC.Chat)localObject4, true);
                }
                k = i;
                if (localObject4 != null)
                {
                  k = i;
                  if (!((TLRPC.Chat)localObject4).min) {
                    loadUnknownChannel((TLRPC.Chat)localObject4, 0L);
                  }
                }
              }
              for (k = i;; k = i)
              {
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
                if (k == 0) {
                  break;
                }
                if ((((TLRPC.TL_updateChannelTooLong)localObject34).flags & 0x1) == 0) {
                  break label8486;
                }
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
                if (((TLRPC.TL_updateChannelTooLong)localObject34).pts <= k) {
                  break;
                }
                getChannelDifference(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
                break;
                label8465:
                this.channelsPts.put(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id, i);
              }
              label8486:
              getChannelDifference(((TLRPC.TL_updateChannelTooLong)localObject34).channel_id);
              localObject27 = localObject18;
              localObject28 = localObject17;
              localObject29 = localObject14;
              localObject30 = localObject15;
              localObject19 = localObject10;
              localObject31 = localObject16;
              i = j;
              localObject32 = localObject13;
              localObject20 = localObject7;
              localObject21 = localObject9;
              localObject22 = localObject8;
              localObject4 = localObject1;
              localObject5 = localObject2;
              bool2 = bool1;
              localObject11 = localObject3;
              localObject33 = localObject12;
              paramArrayList2 = paramArrayList1;
              localObject23 = localObject6;
            }
            else
            {
              long l4;
              if ((localObject24 instanceof TLRPC.TL_updateReadChannelInbox))
              {
                localObject5 = (TLRPC.TL_updateReadChannelInbox)localObject24;
                l1 = ((TLRPC.TL_updateReadChannelInbox)localObject5).max_id;
                l2 = ((TLRPC.TL_updateReadChannelInbox)localObject5).channel_id;
                l4 = -((TLRPC.TL_updateReadChannelInbox)localObject5).channel_id;
                localObject21 = localObject9;
                if (localObject9 == null) {
                  localObject21 = new SparseLongArray();
                }
                ((SparseLongArray)localObject21).put(-((TLRPC.TL_updateReadChannelInbox)localObject5).channel_id, l1 | l2 << 32);
                localObject4 = (Integer)this.dialogs_read_inbox_max.get(Long.valueOf(l4));
                paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
                if (localObject4 == null) {
                  paramArrayList2 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(false, l4));
                }
                this.dialogs_read_inbox_max.put(Long.valueOf(l4), Integer.valueOf(Math.max(paramArrayList2.intValue(), ((TLRPC.TL_updateReadChannelInbox)localObject5).max_id)));
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
              else if ((localObject24 instanceof TLRPC.TL_updateReadChannelOutbox))
              {
                localObject5 = (TLRPC.TL_updateReadChannelOutbox)localObject24;
                l1 = ((TLRPC.TL_updateReadChannelOutbox)localObject5).max_id;
                l2 = ((TLRPC.TL_updateReadChannelOutbox)localObject5).channel_id;
                l4 = -((TLRPC.TL_updateReadChannelOutbox)localObject5).channel_id;
                localObject22 = localObject8;
                if (localObject8 == null) {
                  localObject22 = new SparseLongArray();
                }
                ((SparseLongArray)localObject22).put(-((TLRPC.TL_updateReadChannelOutbox)localObject5).channel_id, l1 | l2 << 32);
                localObject4 = (Integer)this.dialogs_read_outbox_max.get(Long.valueOf(l4));
                paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
                if (localObject4 == null) {
                  paramArrayList2 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(true, l4));
                }
                this.dialogs_read_outbox_max.put(Long.valueOf(l4), Integer.valueOf(Math.max(paramArrayList2.intValue(), ((TLRPC.TL_updateReadChannelOutbox)localObject5).max_id)));
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
              else if ((localObject24 instanceof TLRPC.TL_updateDeleteChannelMessages))
              {
                localObject5 = (TLRPC.TL_updateDeleteChannelMessages)localObject24;
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d(localObject24 + " channelId = " + ((TLRPC.TL_updateDeleteChannelMessages)localObject5).channel_id);
                }
                localObject19 = localObject10;
                if (localObject10 == null) {
                  localObject19 = new SparseArray();
                }
                localObject4 = (ArrayList)((SparseArray)localObject19).get(((TLRPC.TL_updateDeleteChannelMessages)localObject5).channel_id);
                paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
                if (localObject4 == null)
                {
                  paramArrayList2 = new ArrayList();
                  ((SparseArray)localObject19).put(((TLRPC.TL_updateDeleteChannelMessages)localObject5).channel_id, paramArrayList2);
                }
                paramArrayList2.addAll(((TLRPC.TL_updateDeleteChannelMessages)localObject5).messages);
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
              else if ((localObject24 instanceof TLRPC.TL_updateChannel))
              {
                if (BuildVars.LOGS_ENABLED)
                {
                  paramArrayList2 = (TLRPC.TL_updateChannel)localObject24;
                  FileLog.d(localObject24 + " channelId = " + paramArrayList2.channel_id);
                }
                paramArrayList2 = paramArrayList1;
                if (paramArrayList1 == null) {
                  paramArrayList2 = new ArrayList();
                }
                paramArrayList2.add(localObject24);
                localObject27 = localObject18;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                localObject23 = localObject6;
              }
              else if ((localObject24 instanceof TLRPC.TL_updateChannelMessageViews))
              {
                localObject11 = (TLRPC.TL_updateChannelMessageViews)localObject24;
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d(localObject24 + " channelId = " + ((TLRPC.TL_updateChannelMessageViews)localObject11).channel_id);
                }
                paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject18;
                if (localObject18 == null) {
                  paramArrayList2 = new SparseArray();
                }
                localObject5 = (SparseIntArray)paramArrayList2.get(((TLRPC.TL_updateChannelMessageViews)localObject11).channel_id);
                localObject4 = localObject5;
                if (localObject5 == null)
                {
                  localObject4 = new SparseIntArray();
                  paramArrayList2.put(((TLRPC.TL_updateChannelMessageViews)localObject11).channel_id, localObject4);
                }
                ((SparseIntArray)localObject4).put(((TLRPC.TL_updateChannelMessageViews)localObject11).id, ((TLRPC.TL_updateChannelMessageViews)localObject11).views);
                localObject27 = paramArrayList2;
                localObject28 = localObject17;
                localObject29 = localObject14;
                localObject30 = localObject15;
                localObject19 = localObject10;
                localObject31 = localObject16;
                i = j;
                localObject32 = localObject13;
                localObject20 = localObject7;
                localObject21 = localObject9;
                localObject22 = localObject8;
                localObject4 = localObject1;
                localObject5 = localObject2;
                bool2 = bool1;
                localObject11 = localObject3;
                localObject33 = localObject12;
                paramArrayList2 = paramArrayList1;
                localObject23 = localObject6;
              }
              else
              {
                if ((localObject24 instanceof TLRPC.TL_updateChatParticipantAdmin))
                {
                  paramArrayList2 = (TLRPC.TL_updateChatParticipantAdmin)localObject24;
                  localObject4 = MessagesStorage.getInstance(this.currentAccount);
                  k = paramArrayList2.chat_id;
                  m = paramArrayList2.user_id;
                  if (paramArrayList2.is_admin) {}
                  for (i = 1;; i = 0)
                  {
                    ((MessagesStorage)localObject4).updateChatInfo(k, m, 2, i, paramArrayList2.version);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    paramArrayList2 = paramArrayList1;
                    localObject23 = localObject6;
                    break;
                  }
                }
                if ((localObject24 instanceof TLRPC.TL_updateChatAdmins))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else if ((localObject24 instanceof TLRPC.TL_updateStickerSets))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else if ((localObject24 instanceof TLRPC.TL_updateStickerSetsOrder))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else if ((localObject24 instanceof TLRPC.TL_updateNewStickerSet))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else if ((localObject24 instanceof TLRPC.TL_updateDraftMessage))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else if ((localObject24 instanceof TLRPC.TL_updateSavedGifs))
                {
                  paramArrayList2 = paramArrayList1;
                  if (paramArrayList1 == null) {
                    paramArrayList2 = new ArrayList();
                  }
                  paramArrayList2.add(localObject24);
                  localObject27 = localObject18;
                  localObject28 = localObject17;
                  localObject29 = localObject14;
                  localObject30 = localObject15;
                  localObject19 = localObject10;
                  localObject31 = localObject16;
                  i = j;
                  localObject32 = localObject13;
                  localObject20 = localObject7;
                  localObject21 = localObject9;
                  localObject22 = localObject8;
                  localObject4 = localObject1;
                  localObject5 = localObject2;
                  bool2 = bool1;
                  localObject11 = localObject3;
                  localObject33 = localObject12;
                  localObject23 = localObject6;
                }
                else
                {
                  if (((localObject24 instanceof TLRPC.TL_updateEditChannelMessage)) || ((localObject24 instanceof TLRPC.TL_updateEditMessage)))
                  {
                    k = UserConfig.getInstance(this.currentAccount).getClientUserId();
                    if ((localObject24 instanceof TLRPC.TL_updateEditChannelMessage))
                    {
                      localObject11 = ((TLRPC.TL_updateEditChannelMessage)localObject24).message;
                      localObject4 = (TLRPC.Chat)((ConcurrentHashMap)localObject26).get(Integer.valueOf(((TLRPC.Message)localObject11).to_id.channel_id));
                      paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject4;
                      if (localObject4 == null) {
                        paramArrayList2 = getChat(Integer.valueOf(((TLRPC.Message)localObject11).to_id.channel_id));
                      }
                      localObject5 = paramArrayList2;
                      if (paramArrayList2 == null)
                      {
                        localObject5 = MessagesStorage.getInstance(this.currentAccount).getChatSync(((TLRPC.Message)localObject11).to_id.channel_id);
                        putChat((TLRPC.Chat)localObject5, true);
                      }
                      localObject4 = localObject11;
                      if (localObject5 != null)
                      {
                        localObject4 = localObject11;
                        if (((TLRPC.Chat)localObject5).megagroup)
                        {
                          ((TLRPC.Message)localObject11).flags |= 0x80000000;
                          localObject4 = localObject11;
                        }
                      }
                      if ((!((TLRPC.Message)localObject4).out) && (((TLRPC.Message)localObject4).from_id == UserConfig.getInstance(this.currentAccount).getClientUserId())) {
                        ((TLRPC.Message)localObject4).out = true;
                      }
                      if (!paramBoolean)
                      {
                        i = 0;
                        m = ((TLRPC.Message)localObject4).entities.size();
                      }
                    }
                    else
                    {
                      for (;;)
                      {
                        if (i >= m) {
                          break label10685;
                        }
                        paramArrayList2 = (TLRPC.MessageEntity)((TLRPC.Message)localObject4).entities.get(i);
                        if ((paramArrayList2 instanceof TLRPC.TL_messageEntityMentionName))
                        {
                          i2 = ((TLRPC.TL_messageEntityMentionName)paramArrayList2).user_id;
                          localObject5 = (TLRPC.User)((ConcurrentHashMap)localObject25).get(Integer.valueOf(i2));
                          if (localObject5 != null)
                          {
                            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject5;
                            if (!((TLRPC.User)localObject5).min) {}
                          }
                          else
                          {
                            paramArrayList2 = getUser(Integer.valueOf(i2));
                          }
                          if (paramArrayList2 != null)
                          {
                            localObject5 = paramArrayList2;
                            if (!paramArrayList2.min) {}
                          }
                          else
                          {
                            localObject5 = MessagesStorage.getInstance(this.currentAccount).getUserSync(i2);
                            paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject5;
                            if (localObject5 != null)
                            {
                              paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject5;
                              if (((TLRPC.User)localObject5).min) {
                                paramArrayList2 = null;
                              }
                            }
                            putUser(paramArrayList2, true);
                            localObject5 = paramArrayList2;
                          }
                          if (localObject5 == null)
                          {
                            return false;
                            paramArrayList2 = ((TLRPC.TL_updateEditMessage)localObject24).message;
                            localObject4 = paramArrayList2;
                            if (paramArrayList2.dialog_id != k) {
                              break;
                            }
                            paramArrayList2.unread = false;
                            paramArrayList2.media_unread = false;
                            paramArrayList2.out = true;
                            localObject4 = paramArrayList2;
                            break;
                          }
                        }
                        i += 1;
                      }
                    }
                    label10685:
                    if (((TLRPC.Message)localObject4).to_id.chat_id != 0)
                    {
                      ((TLRPC.Message)localObject4).dialog_id = (-((TLRPC.Message)localObject4).to_id.chat_id);
                      label10711:
                      if (!((TLRPC.Message)localObject4).out) {
                        break label11133;
                      }
                      paramArrayList2 = this.dialogs_read_outbox_max;
                      label10724:
                      localObject11 = (Integer)paramArrayList2.get(Long.valueOf(((TLRPC.Message)localObject4).dialog_id));
                      localObject5 = localObject11;
                      if (localObject11 == null)
                      {
                        localObject5 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(((TLRPC.Message)localObject4).out, ((TLRPC.Message)localObject4).dialog_id));
                        paramArrayList2.put(Long.valueOf(((TLRPC.Message)localObject4).dialog_id), localObject5);
                      }
                      if (((Integer)localObject5).intValue() >= ((TLRPC.Message)localObject4).id) {
                        break label11141;
                      }
                    }
                    label11133:
                    label11141:
                    for (bool2 = true;; bool2 = false)
                    {
                      ((TLRPC.Message)localObject4).unread = bool2;
                      if (((TLRPC.Message)localObject4).dialog_id == k)
                      {
                        ((TLRPC.Message)localObject4).out = true;
                        ((TLRPC.Message)localObject4).unread = false;
                        ((TLRPC.Message)localObject4).media_unread = false;
                      }
                      if ((((TLRPC.Message)localObject4).out) && (((TLRPC.Message)localObject4).message == null))
                      {
                        ((TLRPC.Message)localObject4).message = "";
                        ((TLRPC.Message)localObject4).attachPath = "";
                      }
                      ImageLoader.saveMessageThumbs((TLRPC.Message)localObject4);
                      localObject19 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject4, (AbstractMap)localObject25, (AbstractMap)localObject26, this.createdDialogIds.contains(Long.valueOf(((TLRPC.Message)localObject4).dialog_id)));
                      paramArrayList2 = (ArrayList<TLRPC.Chat>)localObject16;
                      if (localObject16 == null) {
                        paramArrayList2 = new LongSparseArray();
                      }
                      localObject11 = (ArrayList)paramArrayList2.get(((TLRPC.Message)localObject4).dialog_id);
                      localObject5 = localObject11;
                      if (localObject11 == null)
                      {
                        localObject5 = new ArrayList();
                        paramArrayList2.put(((TLRPC.Message)localObject4).dialog_id, localObject5);
                      }
                      ((ArrayList)localObject5).add(localObject19);
                      localObject27 = localObject18;
                      localObject28 = localObject17;
                      localObject29 = localObject14;
                      localObject30 = localObject15;
                      localObject19 = localObject10;
                      localObject31 = paramArrayList2;
                      i = j;
                      localObject32 = localObject13;
                      localObject20 = localObject7;
                      localObject21 = localObject9;
                      localObject22 = localObject8;
                      localObject4 = localObject1;
                      localObject5 = localObject2;
                      bool2 = bool1;
                      localObject11 = localObject3;
                      localObject33 = localObject12;
                      paramArrayList2 = paramArrayList1;
                      localObject23 = localObject6;
                      break;
                      if (((TLRPC.Message)localObject4).to_id.channel_id != 0)
                      {
                        ((TLRPC.Message)localObject4).dialog_id = (-((TLRPC.Message)localObject4).to_id.channel_id);
                        break label10711;
                      }
                      if (((TLRPC.Message)localObject4).to_id.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
                        ((TLRPC.Message)localObject4).to_id.user_id = ((TLRPC.Message)localObject4).from_id;
                      }
                      ((TLRPC.Message)localObject4).dialog_id = ((TLRPC.Message)localObject4).to_id.user_id;
                      break label10711;
                      paramArrayList2 = this.dialogs_read_inbox_max;
                      break label10724;
                    }
                  }
                  if ((localObject24 instanceof TLRPC.TL_updateChannelPinnedMessage))
                  {
                    paramArrayList2 = (TLRPC.TL_updateChannelPinnedMessage)localObject24;
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d(localObject24 + " channelId = " + paramArrayList2.channel_id);
                    }
                    MessagesStorage.getInstance(this.currentAccount).updateChannelPinnedMessage(paramArrayList2.channel_id, paramArrayList2.id);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    paramArrayList2 = paramArrayList1;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updateReadFeaturedStickers))
                  {
                    paramArrayList2 = paramArrayList1;
                    if (paramArrayList1 == null) {
                      paramArrayList2 = new ArrayList();
                    }
                    paramArrayList2.add(localObject24);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updatePhoneCall))
                  {
                    paramArrayList2 = paramArrayList1;
                    if (paramArrayList1 == null) {
                      paramArrayList2 = new ArrayList();
                    }
                    paramArrayList2.add(localObject24);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updateLangPack))
                  {
                    paramArrayList2 = (TLRPC.TL_updateLangPack)localObject24;
                    LocaleController.getInstance().saveRemoteLocaleStrings(paramArrayList2.difference, this.currentAccount);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    paramArrayList2 = paramArrayList1;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updateLangPackTooLong))
                  {
                    LocaleController.getInstance().reloadCurrentRemoteLocale(this.currentAccount);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    paramArrayList2 = paramArrayList1;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updateFavedStickers))
                  {
                    paramArrayList2 = paramArrayList1;
                    if (paramArrayList1 == null) {
                      paramArrayList2 = new ArrayList();
                    }
                    paramArrayList2.add(localObject24);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    localObject23 = localObject6;
                  }
                  else if ((localObject24 instanceof TLRPC.TL_updateContactsReset))
                  {
                    paramArrayList2 = paramArrayList1;
                    if (paramArrayList1 == null) {
                      paramArrayList2 = new ArrayList();
                    }
                    paramArrayList2.add(localObject24);
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    localObject23 = localObject6;
                  }
                  else
                  {
                    localObject27 = localObject18;
                    localObject28 = localObject17;
                    localObject29 = localObject14;
                    localObject30 = localObject15;
                    localObject19 = localObject10;
                    localObject31 = localObject16;
                    i = j;
                    localObject32 = localObject13;
                    localObject20 = localObject7;
                    localObject21 = localObject9;
                    localObject22 = localObject8;
                    localObject4 = localObject1;
                    localObject5 = localObject2;
                    bool2 = bool1;
                    localObject11 = localObject3;
                    localObject33 = localObject12;
                    paramArrayList2 = paramArrayList1;
                    localObject23 = localObject6;
                    if ((localObject24 instanceof TLRPC.TL_updateChannelAvailableMessages))
                    {
                      localObject34 = (TLRPC.TL_updateChannelAvailableMessages)localObject24;
                      localObject24 = localObject14;
                      if (localObject14 == null) {
                        localObject24 = new SparseIntArray();
                      }
                      k = ((SparseIntArray)localObject24).get(((TLRPC.TL_updateChannelAvailableMessages)localObject34).channel_id);
                      if (k != 0)
                      {
                        localObject27 = localObject18;
                        localObject28 = localObject17;
                        localObject29 = localObject24;
                        localObject30 = localObject15;
                        localObject19 = localObject10;
                        localObject31 = localObject16;
                        i = j;
                        localObject32 = localObject13;
                        localObject20 = localObject7;
                        localObject21 = localObject9;
                        localObject22 = localObject8;
                        localObject4 = localObject1;
                        localObject5 = localObject2;
                        bool2 = bool1;
                        localObject11 = localObject3;
                        localObject33 = localObject12;
                        paramArrayList2 = paramArrayList1;
                        localObject23 = localObject6;
                        if (k >= ((TLRPC.TL_updateChannelAvailableMessages)localObject34).available_min_id) {}
                      }
                      else
                      {
                        ((SparseIntArray)localObject24).put(((TLRPC.TL_updateChannelAvailableMessages)localObject34).channel_id, ((TLRPC.TL_updateChannelAvailableMessages)localObject34).available_min_id);
                        localObject27 = localObject18;
                        localObject28 = localObject17;
                        localObject29 = localObject24;
                        localObject30 = localObject15;
                        localObject19 = localObject10;
                        localObject31 = localObject16;
                        i = j;
                        localObject32 = localObject13;
                        localObject20 = localObject7;
                        localObject21 = localObject9;
                        localObject22 = localObject8;
                        localObject4 = localObject1;
                        localObject5 = localObject2;
                        bool2 = bool1;
                        localObject11 = localObject3;
                        localObject33 = localObject12;
                        paramArrayList2 = paramArrayList1;
                        localObject23 = localObject6;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    paramBoolean = bool1;
    if (localObject1 != null)
    {
      i = 0;
      k = ((LongSparseArray)localObject1).size();
      for (;;)
      {
        paramBoolean = bool1;
        if (i >= k) {
          break;
        }
        if (updatePrintingUsersWithNewMessages(((LongSparseArray)localObject1).keyAt(i), (ArrayList)((LongSparseArray)localObject1).valueAt(i))) {
          bool1 = true;
        }
        i += 1;
      }
    }
    if (paramBoolean) {
      updatePrintingStrings();
    }
    if (localObject15 != null) {
      ContactsController.getInstance(this.currentAccount).processContactsUpdates((ArrayList)localObject15, (ConcurrentHashMap)localObject25);
    }
    if (localObject3 != null) {
      MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
      {
        public void run()
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationsController.getInstance(MessagesController.this.currentAccount).processNewMessages(MessagesController.131.this.val$pushMessagesFinal, true, false);
            }
          });
        }
      });
    }
    if (localObject2 != null)
    {
      StatsController.getInstance(this.currentAccount).incrementReceivedItemsCount(ConnectionsManager.getCurrentNetworkType(), 1, ((ArrayList)localObject2).size());
      MessagesStorage.getInstance(this.currentAccount).putMessages((ArrayList)localObject2, true, true, false, DownloadController.getInstance(this.currentAccount).getAutodownloadMask());
    }
    if (localObject16 != null)
    {
      i = 0;
      m = ((LongSparseArray)localObject16).size();
      while (i < m)
      {
        paramArrayList = new TLRPC.TL_messages_messages();
        paramArrayList2 = (ArrayList)((LongSparseArray)localObject16).valueAt(i);
        k = 0;
        n = paramArrayList2.size();
        while (k < n)
        {
          paramArrayList.messages.add(((MessageObject)paramArrayList2.get(k)).messageOwner);
          k += 1;
        }
        MessagesStorage.getInstance(this.currentAccount).putMessages(paramArrayList, ((LongSparseArray)localObject16).keyAt(i), -2, 0, false);
        i += 1;
      }
    }
    if (localObject18 != null) {
      MessagesStorage.getInstance(this.currentAccount).putChannelViews((SparseArray)localObject18, true);
    }
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = j;
        int k = 0;
        int m = 0;
        int j = i;
        Object localObject1;
        int i1;
        final Object localObject4;
        int n;
        long l1;
        label1375:
        label1636:
        label1737:
        label1930:
        Object localObject3;
        if (paramArrayList1 != null)
        {
          ArrayList localArrayList1 = new ArrayList();
          ArrayList localArrayList2 = new ArrayList();
          localObject1 = null;
          k = 0;
          i1 = paramArrayList1.size();
          j = i;
          i = m;
          m = k;
          if (m < i1)
          {
            Object localObject5 = (TLRPC.Update)paramArrayList1.get(m);
            final Object localObject2;
            if ((localObject5 instanceof TLRPC.TL_updatePrivacy))
            {
              localObject4 = (TLRPC.TL_updatePrivacy)localObject5;
              if ((((TLRPC.TL_updatePrivacy)localObject4).key instanceof TLRPC.TL_privacyKeyStatusTimestamp))
              {
                ContactsController.getInstance(MessagesController.this.currentAccount).setPrivacyRules(((TLRPC.TL_updatePrivacy)localObject4).rules, 0);
                k = j;
                n = i;
                localObject2 = localObject1;
              }
            }
            for (;;)
            {
              m += 1;
              localObject1 = localObject2;
              i = n;
              j = k;
              break;
              if ((((TLRPC.TL_updatePrivacy)localObject4).key instanceof TLRPC.TL_privacyKeyChatInvite))
              {
                ContactsController.getInstance(MessagesController.this.currentAccount).setPrivacyRules(((TLRPC.TL_updatePrivacy)localObject4).rules, 1);
                localObject2 = localObject1;
                n = i;
                k = j;
              }
              else
              {
                localObject2 = localObject1;
                n = i;
                k = j;
                if ((((TLRPC.TL_updatePrivacy)localObject4).key instanceof TLRPC.TL_privacyKeyPhoneCall))
                {
                  ContactsController.getInstance(MessagesController.this.currentAccount).setPrivacyRules(((TLRPC.TL_updatePrivacy)localObject4).rules, 2);
                  localObject2 = localObject1;
                  n = i;
                  k = j;
                  continue;
                  if ((localObject5 instanceof TLRPC.TL_updateUserStatus))
                  {
                    localObject4 = (TLRPC.TL_updateUserStatus)localObject5;
                    localObject2 = MessagesController.this.getUser(Integer.valueOf(((TLRPC.TL_updateUserStatus)localObject4).user_id));
                    if ((((TLRPC.TL_updateUserStatus)localObject4).status instanceof TLRPC.TL_userStatusRecently)) {
                      ((TLRPC.TL_updateUserStatus)localObject4).status.expires = -100;
                    }
                    for (;;)
                    {
                      if (localObject2 != null)
                      {
                        ((TLRPC.User)localObject2).id = ((TLRPC.TL_updateUserStatus)localObject4).user_id;
                        ((TLRPC.User)localObject2).status = ((TLRPC.TL_updateUserStatus)localObject4).status;
                      }
                      localObject2 = new TLRPC.TL_user();
                      ((TLRPC.User)localObject2).id = ((TLRPC.TL_updateUserStatus)localObject4).user_id;
                      ((TLRPC.User)localObject2).status = ((TLRPC.TL_updateUserStatus)localObject4).status;
                      localArrayList2.add(localObject2);
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                      if (((TLRPC.TL_updateUserStatus)localObject4).user_id != UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()) {
                        break;
                      }
                      NotificationsController.getInstance(MessagesController.this.currentAccount).setLastOnlineFromOtherDevice(((TLRPC.TL_updateUserStatus)localObject4).status.expires);
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                      break;
                      if ((((TLRPC.TL_updateUserStatus)localObject4).status instanceof TLRPC.TL_userStatusLastWeek)) {
                        ((TLRPC.TL_updateUserStatus)localObject4).status.expires = -101;
                      } else if ((((TLRPC.TL_updateUserStatus)localObject4).status instanceof TLRPC.TL_userStatusLastMonth)) {
                        ((TLRPC.TL_updateUserStatus)localObject4).status.expires = -102;
                      }
                    }
                  }
                  if ((localObject5 instanceof TLRPC.TL_updateUserName))
                  {
                    localObject2 = (TLRPC.TL_updateUserName)localObject5;
                    localObject4 = MessagesController.this.getUser(Integer.valueOf(((TLRPC.TL_updateUserName)localObject2).user_id));
                    if (localObject4 != null)
                    {
                      if (!UserObject.isContact((TLRPC.User)localObject4))
                      {
                        ((TLRPC.User)localObject4).first_name = ((TLRPC.TL_updateUserName)localObject2).first_name;
                        ((TLRPC.User)localObject4).last_name = ((TLRPC.TL_updateUserName)localObject2).last_name;
                      }
                      if (!TextUtils.isEmpty(((TLRPC.User)localObject4).username)) {
                        MessagesController.this.objectsByUsernames.remove(((TLRPC.User)localObject4).username);
                      }
                      if (TextUtils.isEmpty(((TLRPC.TL_updateUserName)localObject2).username)) {
                        MessagesController.this.objectsByUsernames.put(((TLRPC.TL_updateUserName)localObject2).username, localObject4);
                      }
                      ((TLRPC.User)localObject4).username = ((TLRPC.TL_updateUserName)localObject2).username;
                    }
                    localObject4 = new TLRPC.TL_user();
                    ((TLRPC.User)localObject4).id = ((TLRPC.TL_updateUserName)localObject2).user_id;
                    ((TLRPC.User)localObject4).first_name = ((TLRPC.TL_updateUserName)localObject2).first_name;
                    ((TLRPC.User)localObject4).last_name = ((TLRPC.TL_updateUserName)localObject2).last_name;
                    ((TLRPC.User)localObject4).username = ((TLRPC.TL_updateUserName)localObject2).username;
                    localArrayList1.add(localObject4);
                    localObject2 = localObject1;
                    n = i;
                    k = j;
                  }
                  else
                  {
                    if ((localObject5 instanceof TLRPC.TL_updateDialogPinned))
                    {
                      localObject4 = (TLRPC.TL_updateDialogPinned)localObject5;
                      if ((((TLRPC.TL_updateDialogPinned)localObject4).peer instanceof TLRPC.TL_dialogPeer))
                      {
                        localObject2 = ((TLRPC.TL_dialogPeer)((TLRPC.TL_updateDialogPinned)localObject4).peer).peer;
                        if ((localObject2 instanceof TLRPC.TL_peerUser)) {
                          l1 = ((TLRPC.Peer)localObject2).user_id;
                        }
                      }
                      for (;;)
                      {
                        localObject2 = localObject1;
                        n = i;
                        k = j;
                        if (MessagesController.this.pinDialog(l1, ((TLRPC.TL_updateDialogPinned)localObject4).pinned, null, -1L)) {
                          break;
                        }
                        UserConfig.getInstance(MessagesController.this.currentAccount).pinnedDialogsLoaded = false;
                        UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
                        MessagesController.this.loadPinnedDialogs(l1, null);
                        localObject2 = localObject1;
                        n = i;
                        k = j;
                        break;
                        if ((localObject2 instanceof TLRPC.TL_peerChat))
                        {
                          l1 = -((TLRPC.Peer)localObject2).chat_id;
                        }
                        else
                        {
                          l1 = -((TLRPC.Peer)localObject2).channel_id;
                          continue;
                          l1 = 0L;
                        }
                      }
                    }
                    if ((localObject5 instanceof TLRPC.TL_updatePinnedDialogs))
                    {
                      localObject2 = (TLRPC.TL_updatePinnedDialogs)localObject5;
                      UserConfig.getInstance(MessagesController.this.currentAccount).pinnedDialogsLoaded = false;
                      UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
                      if ((((TLRPC.TL_updatePinnedDialogs)localObject2).flags & 0x1) != 0)
                      {
                        localObject4 = new ArrayList();
                        localObject5 = ((TLRPC.TL_updatePinnedDialogs)localObject5).order;
                        k = 0;
                        n = ((ArrayList)localObject5).size();
                        localObject2 = localObject4;
                        if (k < n)
                        {
                          localObject2 = (TLRPC.DialogPeer)((ArrayList)localObject5).get(k);
                          if ((localObject2 instanceof TLRPC.TL_dialogPeer))
                          {
                            localObject2 = ((TLRPC.TL_dialogPeer)localObject2).peer;
                            if (((TLRPC.Peer)localObject2).user_id != 0) {
                              l1 = ((TLRPC.Peer)localObject2).user_id;
                            }
                          }
                          for (;;)
                          {
                            ((ArrayList)localObject4).add(Long.valueOf(l1));
                            k += 1;
                            break;
                            if (((TLRPC.Peer)localObject2).chat_id != 0)
                            {
                              l1 = -((TLRPC.Peer)localObject2).chat_id;
                            }
                            else
                            {
                              l1 = -((TLRPC.Peer)localObject2).channel_id;
                              continue;
                              l1 = 0L;
                            }
                          }
                        }
                      }
                      else
                      {
                        localObject2 = null;
                      }
                      MessagesController.this.loadPinnedDialogs(0L, (ArrayList)localObject2);
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                    }
                    else if ((localObject5 instanceof TLRPC.TL_updateUserPhoto))
                    {
                      localObject2 = (TLRPC.TL_updateUserPhoto)localObject5;
                      localObject4 = MessagesController.this.getUser(Integer.valueOf(((TLRPC.TL_updateUserPhoto)localObject2).user_id));
                      if (localObject4 != null) {
                        ((TLRPC.User)localObject4).photo = ((TLRPC.TL_updateUserPhoto)localObject2).photo;
                      }
                      localObject4 = new TLRPC.TL_user();
                      ((TLRPC.User)localObject4).id = ((TLRPC.TL_updateUserPhoto)localObject2).user_id;
                      ((TLRPC.User)localObject4).photo = ((TLRPC.TL_updateUserPhoto)localObject2).photo;
                      localArrayList1.add(localObject4);
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                    }
                    else if ((localObject5 instanceof TLRPC.TL_updateUserPhone))
                    {
                      localObject2 = (TLRPC.TL_updateUserPhone)localObject5;
                      localObject4 = MessagesController.this.getUser(Integer.valueOf(((TLRPC.TL_updateUserPhone)localObject2).user_id));
                      if (localObject4 != null)
                      {
                        ((TLRPC.User)localObject4).phone = ((TLRPC.TL_updateUserPhone)localObject2).phone;
                        Utilities.phoneBookQueue.postRunnable(new Runnable()
                        {
                          public void run()
                          {
                            ContactsController.getInstance(MessagesController.this.currentAccount).addContactToPhoneBook(localObject4, true);
                          }
                        });
                      }
                      localObject4 = new TLRPC.TL_user();
                      ((TLRPC.User)localObject4).id = ((TLRPC.TL_updateUserPhone)localObject2).user_id;
                      ((TLRPC.User)localObject4).phone = ((TLRPC.TL_updateUserPhone)localObject2).phone;
                      localArrayList1.add(localObject4);
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                    }
                    else if ((localObject5 instanceof TLRPC.TL_updateNotifySettings))
                    {
                      localObject4 = (TLRPC.TL_updateNotifySettings)localObject5;
                      localObject2 = localObject1;
                      n = i;
                      k = j;
                      if ((((TLRPC.TL_updateNotifySettings)localObject4).notify_settings instanceof TLRPC.TL_peerNotifySettings))
                      {
                        localObject2 = localObject1;
                        n = i;
                        k = j;
                        if ((((TLRPC.TL_updateNotifySettings)localObject4).peer instanceof TLRPC.TL_notifyPeer))
                        {
                          localObject2 = localObject1;
                          if (localObject1 == null) {
                            localObject2 = MessagesController.this.notificationsPreferences.edit();
                          }
                          if (((TLRPC.TL_updateNotifySettings)localObject4).peer.peer.user_id != 0)
                          {
                            l1 = ((TLRPC.TL_updateNotifySettings)localObject4).peer.peer.user_id;
                            localObject1 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l1);
                            if (localObject1 != null) {
                              ((TLRPC.TL_dialog)localObject1).notify_settings = ((TLRPC.TL_updateNotifySettings)localObject4).notify_settings;
                            }
                            ((SharedPreferences.Editor)localObject2).putBoolean("silent_" + l1, ((TLRPC.TL_updateNotifySettings)localObject4).notify_settings.silent);
                            k = ConnectionsManager.getInstance(MessagesController.this.currentAccount).getCurrentTime();
                            if (((TLRPC.TL_updateNotifySettings)localObject4).notify_settings.mute_until <= k) {
                              break label1737;
                            }
                            n = 0;
                            if (((TLRPC.TL_updateNotifySettings)localObject4).notify_settings.mute_until <= 31536000 + k) {
                              break label1636;
                            }
                            ((SharedPreferences.Editor)localObject2).putInt("notify2_" + l1, 2);
                            k = n;
                            if (localObject1 != null)
                            {
                              ((TLRPC.TL_dialog)localObject1).notify_settings.mute_until = Integer.MAX_VALUE;
                              k = n;
                            }
                          }
                          for (;;)
                          {
                            MessagesStorage.getInstance(MessagesController.this.currentAccount).setDialogFlags(l1, k << 32 | 1L);
                            NotificationsController.getInstance(MessagesController.this.currentAccount).removeNotificationsForDialog(l1);
                            n = i;
                            k = j;
                            break;
                            if (((TLRPC.TL_updateNotifySettings)localObject4).peer.peer.chat_id != 0)
                            {
                              l1 = -((TLRPC.TL_updateNotifySettings)localObject4).peer.peer.chat_id;
                              break label1375;
                            }
                            l1 = -((TLRPC.TL_updateNotifySettings)localObject4).peer.peer.channel_id;
                            break label1375;
                            n = ((TLRPC.TL_updateNotifySettings)localObject4).notify_settings.mute_until;
                            ((SharedPreferences.Editor)localObject2).putInt("notify2_" + l1, 3);
                            ((SharedPreferences.Editor)localObject2).putInt("notifyuntil_" + l1, ((TLRPC.TL_updateNotifySettings)localObject4).notify_settings.mute_until);
                            k = n;
                            if (localObject1 != null)
                            {
                              ((TLRPC.TL_dialog)localObject1).notify_settings.mute_until = n;
                              k = n;
                            }
                          }
                          if (localObject1 != null) {
                            ((TLRPC.TL_dialog)localObject1).notify_settings.mute_until = 0;
                          }
                          ((SharedPreferences.Editor)localObject2).remove("notify2_" + l1);
                          MessagesStorage.getInstance(MessagesController.this.currentAccount).setDialogFlags(l1, 0L);
                          n = i;
                          k = j;
                        }
                      }
                    }
                    else
                    {
                      if ((localObject5 instanceof TLRPC.TL_updateChannel))
                      {
                        localObject2 = (TLRPC.TL_updateChannel)localObject5;
                        localObject4 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(-((TLRPC.TL_updateChannel)localObject2).channel_id);
                        localObject5 = MessagesController.this.getChat(Integer.valueOf(((TLRPC.TL_updateChannel)localObject2).channel_id));
                        if (localObject5 != null)
                        {
                          if ((localObject4 != null) || (!(localObject5 instanceof TLRPC.TL_channel)) || (((TLRPC.Chat)localObject5).left)) {
                            break label1930;
                          }
                          Utilities.stageQueue.postRunnable(new Runnable()
                          {
                            public void run()
                            {
                              MessagesController.this.getChannelDifference(localObject2.channel_id, 1, 0L, null);
                            }
                          });
                        }
                        for (;;)
                        {
                          k = j | 0x2000;
                          MessagesController.this.loadFullChat(((TLRPC.TL_updateChannel)localObject2).channel_id, 0, true);
                          localObject2 = localObject1;
                          n = i;
                          break;
                          if ((((TLRPC.Chat)localObject5).left) && (localObject4 != null)) {
                            MessagesController.this.deleteDialog(((TLRPC.TL_dialog)localObject4).id, 0);
                          }
                        }
                      }
                      if ((localObject5 instanceof TLRPC.TL_updateChatAdmins))
                      {
                        k = j | 0x4000;
                        localObject2 = localObject1;
                        n = i;
                      }
                      else if ((localObject5 instanceof TLRPC.TL_updateStickerSets))
                      {
                        localObject2 = (TLRPC.TL_updateStickerSets)localObject5;
                        DataQuery.getInstance(MessagesController.this.currentAccount).loadStickers(0, false, true);
                        localObject2 = localObject1;
                        n = i;
                        k = j;
                      }
                      else
                      {
                        if ((localObject5 instanceof TLRPC.TL_updateStickerSetsOrder))
                        {
                          localObject2 = (TLRPC.TL_updateStickerSetsOrder)localObject5;
                          localObject4 = DataQuery.getInstance(MessagesController.this.currentAccount);
                          if (((TLRPC.TL_updateStickerSetsOrder)localObject2).masks) {}
                          for (k = 1;; k = 0)
                          {
                            ((DataQuery)localObject4).reorderStickers(k, ((TLRPC.TL_updateStickerSetsOrder)localObject5).order);
                            localObject2 = localObject1;
                            n = i;
                            k = j;
                            break;
                          }
                        }
                        if ((localObject5 instanceof TLRPC.TL_updateFavedStickers))
                        {
                          DataQuery.getInstance(MessagesController.this.currentAccount).loadRecents(2, false, false, true);
                          localObject2 = localObject1;
                          n = i;
                          k = j;
                        }
                        else if ((localObject5 instanceof TLRPC.TL_updateContactsReset))
                        {
                          ContactsController.getInstance(MessagesController.this.currentAccount).forceImportContacts();
                          localObject2 = localObject1;
                          n = i;
                          k = j;
                        }
                        else if ((localObject5 instanceof TLRPC.TL_updateNewStickerSet))
                        {
                          localObject2 = (TLRPC.TL_updateNewStickerSet)localObject5;
                          DataQuery.getInstance(MessagesController.this.currentAccount).addNewStickerSet(((TLRPC.TL_updateNewStickerSet)localObject2).stickerset);
                          localObject2 = localObject1;
                          n = i;
                          k = j;
                        }
                        else if ((localObject5 instanceof TLRPC.TL_updateSavedGifs))
                        {
                          MessagesController.this.emojiPreferences.edit().putLong("lastGifLoadTime", 0L).commit();
                          localObject2 = localObject1;
                          n = i;
                          k = j;
                        }
                        else if ((localObject5 instanceof TLRPC.TL_updateRecentStickers))
                        {
                          MessagesController.this.emojiPreferences.edit().putLong("lastStickersLoadTime", 0L).commit();
                          localObject2 = localObject1;
                          n = i;
                          k = j;
                        }
                        else
                        {
                          if ((localObject5 instanceof TLRPC.TL_updateDraftMessage))
                          {
                            localObject2 = (TLRPC.TL_updateDraftMessage)localObject5;
                            n = 1;
                            localObject4 = ((TLRPC.TL_updateDraftMessage)localObject5).peer;
                            if (((TLRPC.Peer)localObject4).user_id != 0) {
                              l1 = ((TLRPC.Peer)localObject4).user_id;
                            }
                            for (;;)
                            {
                              DataQuery.getInstance(MessagesController.this.currentAccount).saveDraft(l1, ((TLRPC.TL_updateDraftMessage)localObject2).draft, null, true);
                              localObject2 = localObject1;
                              k = j;
                              break;
                              if (((TLRPC.Peer)localObject4).channel_id != 0) {
                                l1 = -((TLRPC.Peer)localObject4).channel_id;
                              } else {
                                l1 = -((TLRPC.Peer)localObject4).chat_id;
                              }
                            }
                          }
                          if ((localObject5 instanceof TLRPC.TL_updateReadFeaturedStickers))
                          {
                            DataQuery.getInstance(MessagesController.this.currentAccount).markFaturedStickersAsRead(false);
                            localObject2 = localObject1;
                            n = i;
                            k = j;
                          }
                          else if ((localObject5 instanceof TLRPC.TL_updatePhoneCall))
                          {
                            localObject4 = ((TLRPC.TL_updatePhoneCall)localObject5).phone_call;
                            localObject2 = VoIPService.getSharedInstance();
                            if (BuildVars.LOGS_ENABLED)
                            {
                              FileLog.d("Received call in update: " + localObject4);
                              FileLog.d("call id " + ((TLRPC.PhoneCall)localObject4).id);
                            }
                            if ((localObject4 instanceof TLRPC.TL_phoneCallRequested))
                            {
                              if (((TLRPC.PhoneCall)localObject4).date + MessagesController.this.callRingTimeout / 1000 < ConnectionsManager.getInstance(MessagesController.this.currentAccount).getCurrentTime())
                              {
                                localObject2 = localObject1;
                                n = i;
                                k = j;
                                if (BuildVars.LOGS_ENABLED)
                                {
                                  FileLog.d("ignoring too old call");
                                  localObject2 = localObject1;
                                  n = i;
                                  k = j;
                                }
                              }
                              else
                              {
                                localObject5 = (TelephonyManager)ApplicationLoader.applicationContext.getSystemService("phone");
                                if ((localObject2 != null) || (VoIPService.callIShouldHavePutIntoIntent != null) || (((TelephonyManager)localObject5).getCallState() != 0))
                                {
                                  if (BuildVars.LOGS_ENABLED) {
                                    FileLog.d("Auto-declining call " + ((TLRPC.PhoneCall)localObject4).id + " because there's already active one");
                                  }
                                  localObject2 = new TLRPC.TL_phone_discardCall();
                                  ((TLRPC.TL_phone_discardCall)localObject2).peer = new TLRPC.TL_inputPhoneCall();
                                  ((TLRPC.TL_phone_discardCall)localObject2).peer.access_hash = ((TLRPC.PhoneCall)localObject4).access_hash;
                                  ((TLRPC.TL_phone_discardCall)localObject2).peer.id = ((TLRPC.PhoneCall)localObject4).id;
                                  ((TLRPC.TL_phone_discardCall)localObject2).reason = new TLRPC.TL_phoneCallDiscardReasonBusy();
                                  ConnectionsManager.getInstance(MessagesController.this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
                                  {
                                    public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
                                    {
                                      if (paramAnonymous2TLObject != null)
                                      {
                                        paramAnonymous2TLObject = (TLRPC.Updates)paramAnonymous2TLObject;
                                        MessagesController.this.processUpdates(paramAnonymous2TLObject, false);
                                      }
                                    }
                                  });
                                  localObject2 = localObject1;
                                  n = i;
                                  k = j;
                                }
                                else
                                {
                                  if (BuildVars.LOGS_ENABLED) {
                                    FileLog.d("Starting service for call " + ((TLRPC.PhoneCall)localObject4).id);
                                  }
                                  VoIPService.callIShouldHavePutIntoIntent = (TLRPC.PhoneCall)localObject4;
                                  localObject2 = new Intent(ApplicationLoader.applicationContext, VoIPService.class);
                                  ((Intent)localObject2).putExtra("is_outgoing", false);
                                  if (((TLRPC.PhoneCall)localObject4).participant_id == UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId()) {}
                                  for (k = ((TLRPC.PhoneCall)localObject4).admin_id;; k = ((TLRPC.PhoneCall)localObject4).participant_id)
                                  {
                                    ((Intent)localObject2).putExtra("user_id", k);
                                    ((Intent)localObject2).putExtra("account", MessagesController.this.currentAccount);
                                    try
                                    {
                                      if (Build.VERSION.SDK_INT < 26) {
                                        break label2938;
                                      }
                                      ApplicationLoader.applicationContext.startForegroundService((Intent)localObject2);
                                      localObject2 = localObject1;
                                      n = i;
                                      k = j;
                                    }
                                    catch (Throwable localThrowable)
                                    {
                                      FileLog.e(localThrowable);
                                      localObject3 = localObject1;
                                      n = i;
                                      k = j;
                                    }
                                    break;
                                  }
                                  label2938:
                                  ApplicationLoader.applicationContext.startService((Intent)localObject3);
                                  localObject3 = localObject1;
                                  n = i;
                                  k = j;
                                }
                              }
                            }
                            else if ((localObject3 != null) && (localObject4 != null))
                            {
                              ((VoIPService)localObject3).onCallUpdated((TLRPC.PhoneCall)localObject4);
                              localObject3 = localObject1;
                              n = i;
                              k = j;
                            }
                            else
                            {
                              localObject3 = localObject1;
                              n = i;
                              k = j;
                              if (VoIPService.callIShouldHavePutIntoIntent != null)
                              {
                                if (BuildVars.LOGS_ENABLED) {
                                  FileLog.d("Updated the call while the service is starting");
                                }
                                localObject3 = localObject1;
                                n = i;
                                k = j;
                                if (((TLRPC.PhoneCall)localObject4).id == VoIPService.callIShouldHavePutIntoIntent.id)
                                {
                                  VoIPService.callIShouldHavePutIntoIntent = (TLRPC.PhoneCall)localObject4;
                                  localObject3 = localObject1;
                                  n = i;
                                  k = j;
                                }
                              }
                            }
                          }
                          else
                          {
                            localObject3 = localObject1;
                            n = i;
                            k = j;
                            if (!(localObject5 instanceof TLRPC.TL_updateGroupCall))
                            {
                              localObject3 = localObject1;
                              n = i;
                              k = j;
                              if ((localObject5 instanceof TLRPC.TL_updateGroupCallParticipant))
                              {
                                localObject3 = localObject1;
                                n = i;
                                k = j;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          if (localObject1 != null)
          {
            ((SharedPreferences.Editor)localObject1).commit();
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.notificationsSettingsUpdated, new Object[0]);
          }
          MessagesStorage.getInstance(MessagesController.this.currentAccount).updateUsers(localArrayList2, true, true, true);
          MessagesStorage.getInstance(MessagesController.this.currentAccount).updateUsers(localArrayList1, false, true, true);
          k = i;
        }
        if (localObject6 != null)
        {
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.didReceivedWebpagesInUpdates, new Object[] { localObject6 });
          i = 0;
          n = localObject6.size();
          while (i < n)
          {
            l1 = localObject6.keyAt(i);
            localObject1 = (ArrayList)MessagesController.this.reloadingWebpagesPending.get(l1);
            MessagesController.this.reloadingWebpagesPending.remove(l1);
            if (localObject1 != null)
            {
              localObject3 = (TLRPC.WebPage)localObject6.valueAt(i);
              localObject4 = new ArrayList();
              l1 = 0L;
              if (((localObject3 instanceof TLRPC.TL_webPage)) || ((localObject3 instanceof TLRPC.TL_webPageEmpty)))
              {
                m = 0;
                i1 = ((ArrayList)localObject1).size();
                for (;;)
                {
                  l2 = l1;
                  if (m >= i1) {
                    break;
                  }
                  ((MessageObject)((ArrayList)localObject1).get(m)).messageOwner.media.webpage = ((TLRPC.WebPage)localObject3);
                  if (m == 0)
                  {
                    l1 = ((MessageObject)((ArrayList)localObject1).get(m)).getDialogId();
                    ImageLoader.saveMessageThumbs(((MessageObject)((ArrayList)localObject1).get(m)).messageOwner);
                  }
                  ((ArrayList)localObject4).add(((MessageObject)((ArrayList)localObject1).get(m)).messageOwner);
                  m += 1;
                }
              }
              MessagesController.this.reloadingWebpagesPending.put(((TLRPC.WebPage)localObject3).id, localObject1);
              long l2 = l1;
              if (!((ArrayList)localObject4).isEmpty())
              {
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages((ArrayList)localObject4, true, true, false, DownloadController.getInstance(MessagesController.this.currentAccount).getAutodownloadMask());
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, new Object[] { Long.valueOf(l2), localObject1 });
              }
            }
            i += 1;
          }
        }
        i = 0;
        label3603:
        int i2;
        if (localObject1 != null)
        {
          i = 0;
          k = localObject1.size();
          while (i < k)
          {
            l1 = localObject1.keyAt(i);
            localObject1 = (ArrayList)localObject1.valueAt(i);
            MessagesController.this.updateInterfaceWithMessages(l1, (ArrayList)localObject1);
            i += 1;
          }
          i = 1;
          m = i;
          if (localObject16 == null) {
            break label3850;
          }
          k = 0;
          i1 = localObject16.size();
          m = i;
          if (k >= i1) {
            break label3850;
          }
          l1 = localObject16.keyAt(k);
          localObject1 = (ArrayList)localObject16.valueAt(k);
          localObject3 = (MessageObject)MessagesController.this.dialogMessage.get(l1);
          m = i;
          if (localObject3 != null)
          {
            n = 0;
            i2 = ((ArrayList)localObject1).size();
          }
        }
        for (;;)
        {
          m = i;
          if (n < i2)
          {
            localObject4 = (MessageObject)((ArrayList)localObject1).get(n);
            if (((MessageObject)localObject3).getId() == ((MessageObject)localObject4).getId())
            {
              MessagesController.this.dialogMessage.put(l1, localObject4);
              if ((((MessageObject)localObject4).messageOwner.to_id != null) && (((MessageObject)localObject4).messageOwner.to_id.channel_id == 0)) {
                MessagesController.this.dialogMessagesByIds.put(((MessageObject)localObject4).getId(), localObject4);
              }
              m = 1;
            }
          }
          else
          {
            DataQuery.getInstance(MessagesController.this.currentAccount).loadReplyMessagesForMessages((ArrayList)localObject1, l1);
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, new Object[] { Long.valueOf(l1), localObject1 });
            k += 1;
            i = m;
            break label3603;
            if (k == 0) {
              break;
            }
            MessagesController.this.sortDialogs(null);
            i = 1;
            break;
          }
          n += 1;
        }
        label3850:
        if (m != 0) {
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
        }
        i = j;
        if (paramBoolean) {
          i = j | 0x40;
        }
        j = i;
        if (localObject15 != null) {
          j = i | 0x1 | 0x80;
        }
        if (localObject17 != null)
        {
          i = 0;
          k = localObject17.size();
          while (i < k)
          {
            localObject1 = (TLRPC.ChatParticipants)localObject17.get(i);
            MessagesStorage.getInstance(MessagesController.this.currentAccount).updateChatParticipants((TLRPC.ChatParticipants)localObject1);
            i += 1;
          }
        }
        if (localObject18 != null) {
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.didUpdatedMessagesViews, new Object[] { localObject18 });
        }
        if (j != 0) {
          NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(j) });
        }
      }
    });
    MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
    {
      public void run()
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            int j = 0;
            int i = 0;
            int k = 0;
            Object localObject1;
            int m;
            int n;
            Object localObject2;
            if ((MessagesController.133.this.val$markAsReadMessagesInboxFinal != null) || (MessagesController.133.this.val$markAsReadMessagesOutboxFinal != null))
            {
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messagesRead, new Object[] { MessagesController.133.this.val$markAsReadMessagesInboxFinal, MessagesController.133.this.val$markAsReadMessagesOutboxFinal });
              if (MessagesController.133.this.val$markAsReadMessagesInboxFinal != null)
              {
                NotificationsController.getInstance(MessagesController.this.currentAccount).processReadMessages(MessagesController.133.this.val$markAsReadMessagesInboxFinal, 0L, 0, 0, false);
                localObject1 = MessagesController.this.notificationsPreferences.edit();
                i = 0;
                m = MessagesController.133.this.val$markAsReadMessagesInboxFinal.size();
                for (j = k; i < m; j = k)
                {
                  n = MessagesController.133.this.val$markAsReadMessagesInboxFinal.keyAt(i);
                  int i1 = (int)MessagesController.133.this.val$markAsReadMessagesInboxFinal.valueAt(i);
                  localObject2 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(n);
                  k = j;
                  if (localObject2 != null)
                  {
                    k = j;
                    if (((TLRPC.TL_dialog)localObject2).top_message > 0)
                    {
                      k = j;
                      if (((TLRPC.TL_dialog)localObject2).top_message <= i1)
                      {
                        localObject2 = (MessageObject)MessagesController.this.dialogMessage.get(((TLRPC.TL_dialog)localObject2).id);
                        k = j;
                        if (localObject2 != null)
                        {
                          k = j;
                          if (!((MessageObject)localObject2).isOut())
                          {
                            ((MessageObject)localObject2).setIsRead();
                            k = j | 0x100;
                          }
                        }
                      }
                    }
                  }
                  if (n != UserConfig.getInstance(MessagesController.this.currentAccount).getClientUserId())
                  {
                    ((SharedPreferences.Editor)localObject1).remove("diditem" + n);
                    ((SharedPreferences.Editor)localObject1).remove("diditemo" + n);
                  }
                  i += 1;
                }
                ((SharedPreferences.Editor)localObject1).commit();
              }
              i = j;
              if (MessagesController.133.this.val$markAsReadMessagesOutboxFinal != null)
              {
                k = 0;
                m = MessagesController.133.this.val$markAsReadMessagesOutboxFinal.size();
                for (;;)
                {
                  i = j;
                  if (k >= m) {
                    break;
                  }
                  i = MessagesController.133.this.val$markAsReadMessagesOutboxFinal.keyAt(k);
                  n = (int)MessagesController.133.this.val$markAsReadMessagesOutboxFinal.valueAt(k);
                  localObject1 = (TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(i);
                  i = j;
                  if (localObject1 != null)
                  {
                    i = j;
                    if (((TLRPC.TL_dialog)localObject1).top_message > 0)
                    {
                      i = j;
                      if (((TLRPC.TL_dialog)localObject1).top_message <= n)
                      {
                        localObject1 = (MessageObject)MessagesController.this.dialogMessage.get(((TLRPC.TL_dialog)localObject1).id);
                        i = j;
                        if (localObject1 != null)
                        {
                          i = j;
                          if (((MessageObject)localObject1).isOut())
                          {
                            ((MessageObject)localObject1).setIsRead();
                            i = j | 0x100;
                          }
                        }
                      }
                    }
                  }
                  k += 1;
                  j = i;
                }
              }
            }
            j = i;
            long l;
            if (MessagesController.133.this.val$markAsReadEncryptedFinal != null)
            {
              k = 0;
              m = MessagesController.133.this.val$markAsReadEncryptedFinal.size();
              for (;;)
              {
                j = i;
                if (k >= m) {
                  break;
                }
                j = MessagesController.133.this.val$markAsReadEncryptedFinal.keyAt(k);
                n = MessagesController.133.this.val$markAsReadEncryptedFinal.valueAt(k);
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messagesReadEncrypted, new Object[] { Integer.valueOf(j), Integer.valueOf(n) });
                l = j << 32;
                j = i;
                if ((TLRPC.TL_dialog)MessagesController.this.dialogs_dict.get(l) != null)
                {
                  localObject1 = (MessageObject)MessagesController.this.dialogMessage.get(l);
                  j = i;
                  if (localObject1 != null)
                  {
                    j = i;
                    if (((MessageObject)localObject1).messageOwner.date <= n)
                    {
                      ((MessageObject)localObject1).setIsRead();
                      j = i | 0x100;
                    }
                  }
                }
                k += 1;
                i = j;
              }
            }
            if (MessagesController.133.this.val$markAsReadMessagesFinal != null) {
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messagesReadContent, new Object[] { MessagesController.133.this.val$markAsReadMessagesFinal });
            }
            if (MessagesController.133.this.val$deletedMessagesFinal != null)
            {
              i = 0;
              m = MessagesController.133.this.val$deletedMessagesFinal.size();
              if (i < m)
              {
                k = MessagesController.133.this.val$deletedMessagesFinal.keyAt(i);
                localObject1 = (ArrayList)MessagesController.133.this.val$deletedMessagesFinal.valueAt(i);
                if (localObject1 == null) {}
                label1019:
                for (;;)
                {
                  i += 1;
                  break;
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.messagesDeleted, new Object[] { localObject1, Integer.valueOf(k) });
                  if (k == 0)
                  {
                    k = 0;
                    n = ((ArrayList)localObject1).size();
                    while (k < n)
                    {
                      localObject2 = (Integer)((ArrayList)localObject1).get(k);
                      localObject2 = (MessageObject)MessagesController.this.dialogMessagesByIds.get(((Integer)localObject2).intValue());
                      if (localObject2 != null) {
                        ((MessageObject)localObject2).deleted = true;
                      }
                      k += 1;
                    }
                  }
                  else
                  {
                    localObject2 = (MessageObject)MessagesController.this.dialogMessage.get(-k);
                    if (localObject2 != null)
                    {
                      k = 0;
                      n = ((ArrayList)localObject1).size();
                      for (;;)
                      {
                        if (k >= n) {
                          break label1019;
                        }
                        if (((MessageObject)localObject2).getId() == ((Integer)((ArrayList)localObject1).get(k)).intValue())
                        {
                          ((MessageObject)localObject2).deleted = true;
                          break;
                        }
                        k += 1;
                      }
                    }
                  }
                }
              }
              NotificationsController.getInstance(MessagesController.this.currentAccount).removeDeletedMessagesFromNotifications(MessagesController.133.this.val$deletedMessagesFinal);
            }
            if (MessagesController.133.this.val$clearHistoryMessagesFinal != null)
            {
              i = 0;
              k = MessagesController.133.this.val$clearHistoryMessagesFinal.size();
            }
            for (;;)
            {
              if (i < k)
              {
                m = MessagesController.133.this.val$clearHistoryMessagesFinal.keyAt(i);
                n = MessagesController.133.this.val$clearHistoryMessagesFinal.valueAt(i);
                l = -m;
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.historyCleared, new Object[] { Long.valueOf(l), Integer.valueOf(n) });
                localObject1 = (MessageObject)MessagesController.this.dialogMessage.get(l);
                if ((localObject1 != null) && (((MessageObject)localObject1).getId() <= n)) {
                  ((MessageObject)localObject1).deleted = true;
                }
              }
              else
              {
                NotificationsController.getInstance(MessagesController.this.currentAccount).removeDeletedHisoryFromNotifications(MessagesController.133.this.val$clearHistoryMessagesFinal);
                if (j != 0) {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(j) });
                }
                return;
              }
              i += 1;
            }
          }
        });
      }
    });
    if (localObject6 != null) {
      MessagesStorage.getInstance(this.currentAccount).putWebPages((LongSparseArray)localObject6);
    }
    if ((localObject9 != null) || (localObject8 != null) || (localObject13 != null) || (localObject7 != null))
    {
      if ((localObject9 != null) || (localObject7 != null)) {
        MessagesStorage.getInstance(this.currentAccount).updateDialogsWithReadMessages((SparseLongArray)localObject9, (SparseLongArray)localObject8, (ArrayList)localObject7, true);
      }
      MessagesStorage.getInstance(this.currentAccount).markMessagesAsRead((SparseLongArray)localObject9, (SparseLongArray)localObject8, (SparseIntArray)localObject13, true);
    }
    if (localObject7 != null) {
      MessagesStorage.getInstance(this.currentAccount).markMessagesContentAsRead((ArrayList)localObject7, ConnectionsManager.getInstance(this.currentAccount).getCurrentTime());
    }
    if (localObject10 != null)
    {
      i = 0;
      j = ((SparseArray)localObject10).size();
      while (i < j)
      {
        k = ((SparseArray)localObject10).keyAt(i);
        paramArrayList = (ArrayList)((SparseArray)localObject10).valueAt(i);
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
        {
          public void run()
          {
            ArrayList localArrayList = MessagesStorage.getInstance(MessagesController.this.currentAccount).markMessagesAsDeleted(paramArrayList, false, k);
            MessagesStorage.getInstance(MessagesController.this.currentAccount).updateDialogsWithDeletedMessages(paramArrayList, localArrayList, false, k);
          }
        });
        i += 1;
      }
    }
    if (localObject14 != null)
    {
      i = 0;
      j = ((SparseIntArray)localObject14).size();
      while (i < j)
      {
        k = ((SparseIntArray)localObject14).keyAt(i);
        m = ((SparseIntArray)localObject14).valueAt(i);
        MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
        {
          public void run()
          {
            ArrayList localArrayList = MessagesStorage.getInstance(MessagesController.this.currentAccount).markMessagesAsDeleted(k, m, false);
            MessagesStorage.getInstance(MessagesController.this.currentAccount).updateDialogsWithDeletedMessages(new ArrayList(), localArrayList, false, k);
          }
        });
        i += 1;
      }
    }
    if (localObject12 != null)
    {
      i = 0;
      j = ((ArrayList)localObject12).size();
      while (i < j)
      {
        paramArrayList = (TLRPC.TL_updateEncryptedMessagesRead)((ArrayList)localObject12).get(i);
        MessagesStorage.getInstance(this.currentAccount).createTaskForSecretChat(paramArrayList.chat_id, paramArrayList.max_date, paramArrayList.date, 1, null);
        i += 1;
      }
    }
    return true;
  }
  
  public void processUpdates(final TLRPC.Updates paramUpdates, boolean paramBoolean)
  {
    Object localObject4 = null;
    Object localObject6 = null;
    int m = 0;
    int i3 = 0;
    final int k = 0;
    int i1 = 0;
    int n = 0;
    int i2 = 0;
    Object localObject1;
    int i;
    final Object localObject2;
    int j;
    if ((paramUpdates instanceof TLRPC.TL_updateShort))
    {
      localObject1 = new ArrayList();
      ((ArrayList)localObject1).add(paramUpdates.update);
      processUpdateArray((ArrayList)localObject1, null, null, false);
      n = i2;
      m = i1;
      i = i3;
      localObject2 = localObject6;
      SecretChatHelper.getInstance(this.currentAccount).processPendingEncMessages();
      if (paramBoolean) {
        break label4572;
      }
      j = 0;
      label92:
      if (j >= this.updatesQueueChannels.size()) {
        break label4564;
      }
      k = this.updatesQueueChannels.keyAt(j);
      if ((localObject2 == null) || (!((ArrayList)localObject2).contains(Integer.valueOf(k)))) {
        break label4554;
      }
      getChannelDifference(k);
    }
    for (;;)
    {
      j += 1;
      break label92;
      label175:
      Object localObject5;
      Object localObject3;
      if (((paramUpdates instanceof TLRPC.TL_updateShortChatMessage)) || ((paramUpdates instanceof TLRPC.TL_updateShortMessage)))
      {
        TLRPC.Chat localChat;
        if ((paramUpdates instanceof TLRPC.TL_updateShortChatMessage))
        {
          k = paramUpdates.from_id;
          localObject1 = getUser(Integer.valueOf(k));
          localObject4 = null;
          Object localObject7 = null;
          localObject5 = null;
          localChat = null;
          if (localObject1 != null)
          {
            localObject3 = localObject1;
            if (!((TLRPC.User)localObject1).min) {}
          }
          else
          {
            localObject2 = MessagesStorage.getInstance(this.currentAccount).getUserSync(k);
            localObject1 = localObject2;
            if (localObject2 != null)
            {
              localObject1 = localObject2;
              if (((TLRPC.User)localObject2).min) {
                localObject1 = null;
              }
            }
            putUser((TLRPC.User)localObject1, true);
            localObject3 = localObject1;
          }
          i = 0;
          j = 0;
          localObject2 = localChat;
          if (paramUpdates.fwd_from != null)
          {
            i = j;
            localObject1 = localObject7;
            if (paramUpdates.fwd_from.from_id != 0)
            {
              localObject2 = getUser(Integer.valueOf(paramUpdates.fwd_from.from_id));
              localObject1 = localObject2;
              if (localObject2 == null)
              {
                localObject1 = MessagesStorage.getInstance(this.currentAccount).getUserSync(paramUpdates.fwd_from.from_id);
                putUser((TLRPC.User)localObject1, true);
              }
              i = 1;
            }
            localObject2 = localChat;
            localObject4 = localObject1;
            if (paramUpdates.fwd_from.channel_id != 0)
            {
              localObject4 = getChat(Integer.valueOf(paramUpdates.fwd_from.channel_id));
              localObject2 = localObject4;
              if (localObject4 == null)
              {
                localObject2 = MessagesStorage.getInstance(this.currentAccount).getChatSync(paramUpdates.fwd_from.channel_id);
                putChat((TLRPC.Chat)localObject2, true);
              }
              i = 1;
              localObject4 = localObject1;
            }
          }
          j = 0;
          localObject1 = localObject5;
          if (paramUpdates.via_bot_id != 0)
          {
            localObject5 = getUser(Integer.valueOf(paramUpdates.via_bot_id));
            localObject1 = localObject5;
            if (localObject5 == null)
            {
              localObject1 = MessagesStorage.getInstance(this.currentAccount).getUserSync(paramUpdates.via_bot_id);
              putUser((TLRPC.User)localObject1, true);
            }
            j = 1;
          }
          if (!(paramUpdates instanceof TLRPC.TL_updateShortMessage)) {
            break label772;
          }
          if ((localObject3 != null) && ((i == 0) || (localObject4 != null) || (localObject2 != null)) && ((j == 0) || (localObject1 != null))) {
            break label767;
          }
          i = 1;
          label528:
          m = i;
          if (i == 0)
          {
            m = i;
            if (!paramUpdates.entities.isEmpty()) {
              j = 0;
            }
          }
        }
        for (;;)
        {
          m = i;
          if (j < paramUpdates.entities.size())
          {
            localObject1 = (TLRPC.MessageEntity)paramUpdates.entities.get(j);
            if (!(localObject1 instanceof TLRPC.TL_messageEntityMentionName)) {
              break label869;
            }
            m = ((TLRPC.TL_messageEntityMentionName)localObject1).user_id;
            localObject1 = getUser(Integer.valueOf(m));
            if ((localObject1 != null) && (!((TLRPC.User)localObject1).min)) {
              break label869;
            }
            localObject2 = MessagesStorage.getInstance(this.currentAccount).getUserSync(m);
            localObject1 = localObject2;
            if (localObject2 != null)
            {
              localObject1 = localObject2;
              if (((TLRPC.User)localObject2).min) {
                localObject1 = null;
              }
            }
            if (localObject1 == null) {
              m = 1;
            }
          }
          else
          {
            j = n;
            if (localObject3 != null)
            {
              j = n;
              if (((TLRPC.User)localObject3).status != null)
              {
                j = n;
                if (((TLRPC.User)localObject3).status.expires <= 0)
                {
                  this.onlinePrivacy.put(Integer.valueOf(((TLRPC.User)localObject3).id), Integer.valueOf(ConnectionsManager.getInstance(this.currentAccount).getCurrentTime()));
                  j = 1;
                }
              }
            }
            if (m == 0) {
              break label878;
            }
            i = 1;
            localObject2 = localObject6;
            m = i1;
            n = j;
            break;
            k = paramUpdates.user_id;
            break label175;
            label767:
            i = 0;
            break label528;
            label772:
            localChat = getChat(Integer.valueOf(paramUpdates.chat_id));
            localObject5 = localChat;
            if (localChat == null)
            {
              localObject5 = MessagesStorage.getInstance(this.currentAccount).getChatSync(paramUpdates.chat_id);
              putChat((TLRPC.Chat)localObject5, true);
            }
            if ((localObject5 == null) || (localObject3 == null) || ((i != 0) && (localObject4 == null) && (localObject2 == null)) || ((j != 0) && (localObject1 == null))) {}
            for (i = 1;; i = 0) {
              break;
            }
          }
          putUser((TLRPC.User)localObject3, true);
          label869:
          j += 1;
        }
        label878:
        if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + paramUpdates.pts_count == paramUpdates.pts)
        {
          localObject4 = new TLRPC.TL_message();
          ((TLRPC.TL_message)localObject4).id = paramUpdates.id;
          i = UserConfig.getInstance(this.currentAccount).getClientUserId();
          label949:
          label979:
          label1108:
          final boolean bool;
          if ((paramUpdates instanceof TLRPC.TL_updateShortMessage)) {
            if (paramUpdates.out)
            {
              ((TLRPC.TL_message)localObject4).from_id = i;
              ((TLRPC.TL_message)localObject4).to_id = new TLRPC.TL_peerUser();
              ((TLRPC.TL_message)localObject4).to_id.user_id = k;
              ((TLRPC.TL_message)localObject4).dialog_id = k;
              ((TLRPC.TL_message)localObject4).fwd_from = paramUpdates.fwd_from;
              ((TLRPC.TL_message)localObject4).silent = paramUpdates.silent;
              ((TLRPC.TL_message)localObject4).out = paramUpdates.out;
              ((TLRPC.TL_message)localObject4).mentioned = paramUpdates.mentioned;
              ((TLRPC.TL_message)localObject4).media_unread = paramUpdates.media_unread;
              ((TLRPC.TL_message)localObject4).entities = paramUpdates.entities;
              ((TLRPC.TL_message)localObject4).message = paramUpdates.message;
              ((TLRPC.TL_message)localObject4).date = paramUpdates.date;
              ((TLRPC.TL_message)localObject4).via_bot_id = paramUpdates.via_bot_id;
              ((TLRPC.TL_message)localObject4).flags = (paramUpdates.flags | 0x100);
              ((TLRPC.TL_message)localObject4).reply_to_msg_id = paramUpdates.reply_to_msg_id;
              ((TLRPC.TL_message)localObject4).media = new TLRPC.TL_messageMediaEmpty();
              if (!((TLRPC.TL_message)localObject4).out) {
                break label1483;
              }
              localObject1 = this.dialogs_read_outbox_max;
              localObject3 = (Integer)((ConcurrentHashMap)localObject1).get(Long.valueOf(((TLRPC.TL_message)localObject4).dialog_id));
              localObject2 = localObject3;
              if (localObject3 == null)
              {
                localObject2 = Integer.valueOf(MessagesStorage.getInstance(this.currentAccount).getDialogReadMax(((TLRPC.TL_message)localObject4).out, ((TLRPC.TL_message)localObject4).dialog_id));
                ((ConcurrentHashMap)localObject1).put(Long.valueOf(((TLRPC.TL_message)localObject4).dialog_id), localObject2);
              }
              if (((Integer)localObject2).intValue() >= ((TLRPC.TL_message)localObject4).id) {
                break label1492;
              }
              bool = true;
              label1192:
              ((TLRPC.TL_message)localObject4).unread = bool;
              if (((TLRPC.TL_message)localObject4).dialog_id == i)
              {
                ((TLRPC.TL_message)localObject4).unread = false;
                ((TLRPC.TL_message)localObject4).media_unread = false;
                ((TLRPC.TL_message)localObject4).out = true;
              }
              MessagesStorage.getInstance(this.currentAccount).setLastPtsValue(paramUpdates.pts);
              localObject1 = new MessageObject(this.currentAccount, (TLRPC.Message)localObject4, this.createdDialogIds.contains(Long.valueOf(((TLRPC.TL_message)localObject4).dialog_id)));
              localObject2 = new ArrayList();
              ((ArrayList)localObject2).add(localObject1);
              localObject3 = new ArrayList();
              ((ArrayList)localObject3).add(localObject4);
              if (!(paramUpdates instanceof TLRPC.TL_updateShortMessage)) {
                break label1504;
              }
              if ((paramUpdates.out) || (!updatePrintingUsersWithNewMessages(paramUpdates.user_id, (ArrayList)localObject2))) {
                break label1498;
              }
              bool = true;
              label1337:
              if (bool) {
                updatePrintingStrings();
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (bool) {
                    NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(64) });
                  }
                  MessagesController.this.updateInterfaceWithMessages(k, localObject2);
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
                }
              });
            }
          }
          for (;;)
          {
            if (!((MessageObject)localObject1).isOut()) {
              MessagesStorage.getInstance(this.currentAccount).getStorageQueue().postRunnable(new Runnable()
              {
                public void run()
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationsController.getInstance(MessagesController.this.currentAccount).processNewMessages(MessagesController.124.this.val$objArr, true, false);
                    }
                  });
                }
              });
            }
            MessagesStorage.getInstance(this.currentAccount).putMessages((ArrayList)localObject3, false, true, false, 0);
            localObject2 = localObject6;
            i = i3;
            m = i1;
            n = j;
            break;
            ((TLRPC.TL_message)localObject4).from_id = k;
            break label949;
            ((TLRPC.TL_message)localObject4).from_id = k;
            ((TLRPC.TL_message)localObject4).to_id = new TLRPC.TL_peerChat();
            ((TLRPC.TL_message)localObject4).to_id.chat_id = paramUpdates.chat_id;
            ((TLRPC.TL_message)localObject4).dialog_id = (-paramUpdates.chat_id);
            break label979;
            label1483:
            localObject1 = this.dialogs_read_inbox_max;
            break label1108;
            label1492:
            bool = false;
            break label1192;
            label1498:
            bool = false;
            break label1337;
            label1504:
            bool = updatePrintingUsersWithNewMessages(-paramUpdates.chat_id, (ArrayList)localObject2);
            if (bool) {
              updatePrintingStrings();
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (bool) {
                  NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(64) });
                }
                MessagesController.this.updateInterfaceWithMessages(-paramUpdates.chat_id, localObject2);
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload, new Object[0]);
              }
            });
          }
        }
        localObject2 = localObject6;
        i = i3;
        m = i1;
        n = j;
        if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() == paramUpdates.pts) {
          break;
        }
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("need get diff short message, pts: " + MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + " " + paramUpdates.pts + " count = " + paramUpdates.pts_count);
        }
        if ((this.gettingDifference) || (this.updatesStartWaitTimePts == 0L) || (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500L))
        {
          if (this.updatesStartWaitTimePts == 0L) {
            this.updatesStartWaitTimePts = System.currentTimeMillis();
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("add to queue");
          }
          this.updatesQueuePts.add(paramUpdates);
          localObject2 = localObject6;
          i = i3;
          m = i1;
          n = j;
          break;
        }
        i = 1;
        localObject2 = localObject6;
        m = i1;
        n = j;
        break;
      }
      if (((paramUpdates instanceof TLRPC.TL_updatesCombined)) || ((paramUpdates instanceof TLRPC.TL_updates)))
      {
        localObject1 = null;
        i = 0;
        while (i < paramUpdates.chats.size())
        {
          localObject5 = (TLRPC.Chat)paramUpdates.chats.get(i);
          localObject3 = localObject1;
          if ((localObject5 instanceof TLRPC.TL_channel))
          {
            localObject3 = localObject1;
            if (((TLRPC.Chat)localObject5).min)
            {
              localObject3 = getChat(Integer.valueOf(((TLRPC.Chat)localObject5).id));
              if (localObject3 != null)
              {
                localObject2 = localObject3;
                if (!((TLRPC.Chat)localObject3).min) {}
              }
              else
              {
                localObject2 = MessagesStorage.getInstance(this.currentAccount).getChatSync(paramUpdates.chat_id);
                putChat((TLRPC.Chat)localObject2, true);
              }
              if (localObject2 != null)
              {
                localObject3 = localObject1;
                if (!((TLRPC.Chat)localObject2).min) {}
              }
              else
              {
                localObject2 = localObject1;
                if (localObject1 == null) {
                  localObject2 = new SparseArray();
                }
                ((SparseArray)localObject2).put(((TLRPC.Chat)localObject5).id, localObject5);
                localObject3 = localObject2;
              }
            }
          }
          i += 1;
          localObject1 = localObject3;
        }
        j = m;
        if (localObject1 != null) {
          i = 0;
        }
        for (;;)
        {
          j = m;
          if (i < paramUpdates.updates.size())
          {
            localObject2 = (TLRPC.Update)paramUpdates.updates.get(i);
            if ((localObject2 instanceof TLRPC.TL_updateNewChannelMessage))
            {
              j = ((TLRPC.TL_updateNewChannelMessage)localObject2).message.to_id.channel_id;
              if (((SparseArray)localObject1).indexOfKey(j) >= 0)
              {
                if (BuildVars.LOGS_ENABLED) {
                  FileLog.d("need get diff because of min channel " + j);
                }
                j = 1;
              }
            }
          }
          else
          {
            localObject2 = localObject6;
            i = j;
            m = i1;
            n = i2;
            if (j != 0) {
              break;
            }
            MessagesStorage.getInstance(this.currentAccount).putUsersAndChats(paramUpdates.users, paramUpdates.chats, true, true);
            Collections.sort(paramUpdates.updates, this.updatesComparator);
            m = 0;
            localObject1 = localObject4;
            if (paramUpdates.updates.size() <= 0) {
              break label3955;
            }
            localObject3 = (TLRPC.Update)paramUpdates.updates.get(m);
            if (getUpdateType((TLRPC.Update)localObject3) != 0) {
              break label2664;
            }
            localObject4 = new TLRPC.TL_updates();
            ((TLRPC.TL_updates)localObject4).updates.add(localObject3);
            ((TLRPC.TL_updates)localObject4).pts = getUpdatePts((TLRPC.Update)localObject3);
            ((TLRPC.TL_updates)localObject4).pts_count = getUpdatePtsCount((TLRPC.Update)localObject3);
            for (i = m + 1; i < paramUpdates.updates.size(); i = i - 1 + 1)
            {
              localObject2 = (TLRPC.Update)paramUpdates.updates.get(i);
              n = getUpdatePts((TLRPC.Update)localObject2);
              i1 = getUpdatePtsCount((TLRPC.Update)localObject2);
              if ((getUpdateType((TLRPC.Update)localObject2) != 0) || (((TLRPC.TL_updates)localObject4).pts + i1 != n)) {
                break;
              }
              ((TLRPC.TL_updates)localObject4).updates.add(localObject2);
              ((TLRPC.TL_updates)localObject4).pts = n;
              ((TLRPC.TL_updates)localObject4).pts_count += i1;
              paramUpdates.updates.remove(i);
            }
          }
          i += 1;
        }
        if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + ((TLRPC.TL_updates)localObject4).pts_count == ((TLRPC.TL_updates)localObject4).pts) {
          if (!processUpdateArray(((TLRPC.TL_updates)localObject4).updates, paramUpdates.users, paramUpdates.chats, false))
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("need get diff inner TL_updates, pts: " + MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + " " + paramUpdates.seq);
            }
            i = 1;
            i1 = k;
            localObject2 = localObject1;
          }
        }
        for (;;)
        {
          paramUpdates.updates.remove(m);
          m = m - 1 + 1;
          localObject1 = localObject2;
          j = i;
          k = i1;
          break;
          MessagesStorage.getInstance(this.currentAccount).setLastPtsValue(((TLRPC.TL_updates)localObject4).pts);
          localObject2 = localObject1;
          i = j;
          i1 = k;
          continue;
          localObject2 = localObject1;
          i = j;
          i1 = k;
          if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() != ((TLRPC.TL_updates)localObject4).pts)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d(localObject3 + " need get diff, pts: " + MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() + " " + ((TLRPC.TL_updates)localObject4).pts + " count = " + ((TLRPC.TL_updates)localObject4).pts_count);
            }
            if ((this.gettingDifference) || (this.updatesStartWaitTimePts == 0L) || ((this.updatesStartWaitTimePts != 0L) && (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimePts) <= 1500L)))
            {
              if (this.updatesStartWaitTimePts == 0L) {
                this.updatesStartWaitTimePts = System.currentTimeMillis();
              }
              if (BuildVars.LOGS_ENABLED) {
                FileLog.d("add to queue");
              }
              this.updatesQueuePts.add(localObject4);
              localObject2 = localObject1;
              i = j;
              i1 = k;
            }
            else
            {
              i = 1;
              localObject2 = localObject1;
              i1 = k;
              continue;
              label2664:
              if (getUpdateType((TLRPC.Update)localObject3) == 1)
              {
                localObject4 = new TLRPC.TL_updates();
                ((TLRPC.TL_updates)localObject4).updates.add(localObject3);
                ((TLRPC.TL_updates)localObject4).pts = getUpdateQts((TLRPC.Update)localObject3);
                for (i = m + 1; i < paramUpdates.updates.size(); i = i - 1 + 1)
                {
                  localObject2 = (TLRPC.Update)paramUpdates.updates.get(i);
                  n = getUpdateQts((TLRPC.Update)localObject2);
                  if ((getUpdateType((TLRPC.Update)localObject2) != 1) || (((TLRPC.TL_updates)localObject4).pts + 1 != n)) {
                    break;
                  }
                  ((TLRPC.TL_updates)localObject4).updates.add(localObject2);
                  ((TLRPC.TL_updates)localObject4).pts = n;
                  paramUpdates.updates.remove(i);
                }
                if ((MessagesStorage.getInstance(this.currentAccount).getLastQtsValue() == 0) || (MessagesStorage.getInstance(this.currentAccount).getLastQtsValue() + ((TLRPC.TL_updates)localObject4).updates.size() == ((TLRPC.TL_updates)localObject4).pts))
                {
                  processUpdateArray(((TLRPC.TL_updates)localObject4).updates, paramUpdates.users, paramUpdates.chats, false);
                  MessagesStorage.getInstance(this.currentAccount).setLastQtsValue(((TLRPC.TL_updates)localObject4).pts);
                  i1 = 1;
                  localObject2 = localObject1;
                  i = j;
                }
                else
                {
                  localObject2 = localObject1;
                  i = j;
                  i1 = k;
                  if (MessagesStorage.getInstance(this.currentAccount).getLastPtsValue() != ((TLRPC.TL_updates)localObject4).pts)
                  {
                    if (BuildVars.LOGS_ENABLED) {
                      FileLog.d(localObject3 + " need get diff, qts: " + MessagesStorage.getInstance(this.currentAccount).getLastQtsValue() + " " + ((TLRPC.TL_updates)localObject4).pts);
                    }
                    if ((this.gettingDifference) || (this.updatesStartWaitTimeQts == 0L) || ((this.updatesStartWaitTimeQts != 0L) && (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeQts) <= 1500L)))
                    {
                      if (this.updatesStartWaitTimeQts == 0L) {
                        this.updatesStartWaitTimeQts = System.currentTimeMillis();
                      }
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("add to queue");
                      }
                      this.updatesQueueQts.add(localObject4);
                      localObject2 = localObject1;
                      i = j;
                      i1 = k;
                    }
                    else
                    {
                      i = 1;
                      localObject2 = localObject1;
                      i1 = k;
                    }
                  }
                }
              }
              else
              {
                if (getUpdateType((TLRPC.Update)localObject3) != 2) {
                  break label3955;
                }
                int i5 = getUpdateChannelId((TLRPC.Update)localObject3);
                i3 = 0;
                i1 = this.channelsPts.get(i5);
                n = i1;
                i = i3;
                int i4;
                if (i1 == 0)
                {
                  i4 = MessagesStorage.getInstance(this.currentAccount).getChannelPtsSync(i5);
                  if (i4 != 0) {
                    break label3380;
                  }
                  i1 = 0;
                  n = i4;
                  i = i3;
                  if (i1 < paramUpdates.chats.size())
                  {
                    localObject2 = (TLRPC.Chat)paramUpdates.chats.get(i1);
                    if (((TLRPC.Chat)localObject2).id != i5) {
                      break label3371;
                    }
                    loadUnknownChannel((TLRPC.Chat)localObject2, 0L);
                    i = 1;
                    n = i4;
                  }
                }
                for (;;)
                {
                  localObject4 = new TLRPC.TL_updates();
                  ((TLRPC.TL_updates)localObject4).updates.add(localObject3);
                  ((TLRPC.TL_updates)localObject4).pts = getUpdatePts((TLRPC.Update)localObject3);
                  ((TLRPC.TL_updates)localObject4).pts_count = getUpdatePtsCount((TLRPC.Update)localObject3);
                  for (i1 = m + 1; i1 < paramUpdates.updates.size(); i1 = i1 - 1 + 1)
                  {
                    localObject2 = (TLRPC.Update)paramUpdates.updates.get(i1);
                    i3 = getUpdatePts((TLRPC.Update)localObject2);
                    i4 = getUpdatePtsCount((TLRPC.Update)localObject2);
                    if ((getUpdateType((TLRPC.Update)localObject2) != 2) || (i5 != getUpdateChannelId((TLRPC.Update)localObject2)) || (((TLRPC.TL_updates)localObject4).pts + i4 != i3)) {
                      break;
                    }
                    ((TLRPC.TL_updates)localObject4).updates.add(localObject2);
                    ((TLRPC.TL_updates)localObject4).pts = i3;
                    ((TLRPC.TL_updates)localObject4).pts_count += i4;
                    paramUpdates.updates.remove(i1);
                  }
                  label3371:
                  i1 += 1;
                  break;
                  label3380:
                  this.channelsPts.put(i5, i4);
                  n = i4;
                  i = i3;
                }
                if (i == 0)
                {
                  if (((TLRPC.TL_updates)localObject4).pts_count + n == ((TLRPC.TL_updates)localObject4).pts)
                  {
                    if (!processUpdateArray(((TLRPC.TL_updates)localObject4).updates, paramUpdates.users, paramUpdates.chats, false))
                    {
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d("need get channel diff inner TL_updates, channel_id = " + i5);
                      }
                      if (localObject1 == null)
                      {
                        localObject2 = new ArrayList();
                        i = j;
                        i1 = k;
                      }
                      else
                      {
                        localObject2 = localObject1;
                        i = j;
                        i1 = k;
                        if (!((ArrayList)localObject1).contains(Integer.valueOf(i5)))
                        {
                          ((ArrayList)localObject1).add(Integer.valueOf(i5));
                          localObject2 = localObject1;
                          i = j;
                          i1 = k;
                        }
                      }
                    }
                    else
                    {
                      this.channelsPts.put(i5, ((TLRPC.TL_updates)localObject4).pts);
                      MessagesStorage.getInstance(this.currentAccount).saveChannelPts(i5, ((TLRPC.TL_updates)localObject4).pts);
                      localObject2 = localObject1;
                      i = j;
                      i1 = k;
                    }
                  }
                  else
                  {
                    localObject2 = localObject1;
                    i = j;
                    i1 = k;
                    if (n != ((TLRPC.TL_updates)localObject4).pts)
                    {
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d(localObject3 + " need get channel diff, pts: " + n + " " + ((TLRPC.TL_updates)localObject4).pts + " count = " + ((TLRPC.TL_updates)localObject4).pts_count + " channelId = " + i5);
                      }
                      long l = this.updatesStartWaitTimeChannels.get(i5);
                      if ((this.gettingDifferenceChannels.get(i5)) || (l == 0L) || (Math.abs(System.currentTimeMillis() - l) <= 1500L))
                      {
                        if (l == 0L) {
                          this.updatesStartWaitTimeChannels.put(i5, System.currentTimeMillis());
                        }
                        if (BuildVars.LOGS_ENABLED) {
                          FileLog.d("add to queue");
                        }
                        localObject3 = (ArrayList)this.updatesQueueChannels.get(i5);
                        localObject2 = localObject3;
                        if (localObject3 == null)
                        {
                          localObject2 = new ArrayList();
                          this.updatesQueueChannels.put(i5, localObject2);
                        }
                        ((ArrayList)localObject2).add(localObject4);
                        localObject2 = localObject1;
                        i = j;
                        i1 = k;
                      }
                      else if (localObject1 == null)
                      {
                        localObject2 = new ArrayList();
                        i = j;
                        i1 = k;
                      }
                      else
                      {
                        localObject2 = localObject1;
                        i = j;
                        i1 = k;
                        if (!((ArrayList)localObject1).contains(Integer.valueOf(i5)))
                        {
                          ((ArrayList)localObject1).add(Integer.valueOf(i5));
                          localObject2 = localObject1;
                          i = j;
                          i1 = k;
                        }
                      }
                    }
                  }
                }
                else
                {
                  localObject2 = localObject1;
                  i = j;
                  i1 = k;
                  if (BuildVars.LOGS_ENABLED)
                  {
                    FileLog.d("need load unknown channel = " + i5);
                    localObject2 = localObject1;
                    i = j;
                    i1 = k;
                  }
                }
              }
            }
          }
        }
        label3955:
        if ((paramUpdates instanceof TLRPC.TL_updatesCombined))
        {
          if ((MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + 1 == paramUpdates.seq_start) || (MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() == paramUpdates.seq_start)) {}
          for (i = 1;; i = 0)
          {
            if (i == 0) {
              break label4155;
            }
            processUpdateArray(paramUpdates.updates, paramUpdates.users, paramUpdates.chats, false);
            localObject2 = localObject1;
            i = j;
            m = k;
            n = i2;
            if (paramUpdates.seq == 0) {
              break;
            }
            if (paramUpdates.date != 0) {
              MessagesStorage.getInstance(this.currentAccount).setLastDateValue(paramUpdates.date);
            }
            MessagesStorage.getInstance(this.currentAccount).setLastSeqValue(paramUpdates.seq);
            localObject2 = localObject1;
            i = j;
            m = k;
            n = i2;
            break;
          }
        }
        if ((MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + 1 == paramUpdates.seq) || (paramUpdates.seq == 0) || (paramUpdates.seq == MessagesStorage.getInstance(this.currentAccount).getLastSeqValue())) {}
        for (i = 1;; i = 0) {
          break;
        }
        label4155:
        if (BuildVars.LOGS_ENABLED)
        {
          if (!(paramUpdates instanceof TLRPC.TL_updatesCombined)) {
            break label4302;
          }
          FileLog.d("need get diff TL_updatesCombined, seq: " + MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + " " + paramUpdates.seq_start);
        }
        for (;;)
        {
          if ((!this.gettingDifference) && (this.updatesStartWaitTimeSeq != 0L) && (Math.abs(System.currentTimeMillis() - this.updatesStartWaitTimeSeq) > 1500L)) {
            break label4350;
          }
          if (this.updatesStartWaitTimeSeq == 0L) {
            this.updatesStartWaitTimeSeq = System.currentTimeMillis();
          }
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("add TL_updates/Combined to queue");
          }
          this.updatesQueueSeq.add(paramUpdates);
          localObject2 = localObject1;
          i = j;
          m = k;
          n = i2;
          break;
          label4302:
          FileLog.d("need get diff TL_updates, seq: " + MessagesStorage.getInstance(this.currentAccount).getLastSeqValue() + " " + paramUpdates.seq);
        }
        label4350:
        i = 1;
        localObject2 = localObject1;
        m = k;
        n = i2;
        break;
      }
      if ((paramUpdates instanceof TLRPC.TL_updatesTooLong))
      {
        if (BuildVars.LOGS_ENABLED) {
          FileLog.d("need get diff TL_updatesTooLong");
        }
        i = 1;
        localObject2 = localObject6;
        m = i1;
        n = i2;
        break;
      }
      if ((paramUpdates instanceof UserActionUpdatesSeq))
      {
        MessagesStorage.getInstance(this.currentAccount).setLastSeqValue(paramUpdates.seq);
        localObject2 = localObject6;
        i = i3;
        m = i1;
        n = i2;
        break;
      }
      localObject2 = localObject6;
      i = i3;
      m = i1;
      n = i2;
      if (!(paramUpdates instanceof UserActionUpdatesPts)) {
        break;
      }
      if (paramUpdates.chat_id != 0)
      {
        this.channelsPts.put(paramUpdates.chat_id, paramUpdates.pts);
        MessagesStorage.getInstance(this.currentAccount).saveChannelPts(paramUpdates.chat_id, paramUpdates.pts);
        localObject2 = localObject6;
        i = i3;
        m = i1;
        n = i2;
        break;
      }
      MessagesStorage.getInstance(this.currentAccount).setLastPtsValue(paramUpdates.pts);
      localObject2 = localObject6;
      i = i3;
      m = i1;
      n = i2;
      break;
      label4554:
      processChannelsUpdatesQueue(k, 0);
    }
    label4564:
    if (i != 0) {
      getDifference();
    }
    for (;;)
    {
      label4572:
      if (m != 0)
      {
        paramUpdates = new TLRPC.TL_messages_receivedQueue();
        paramUpdates.max_qts = MessagesStorage.getInstance(this.currentAccount).getLastQtsValue();
        ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramUpdates, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
        });
      }
      if (n != 0) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(4) });
          }
        });
      }
      MessagesStorage.getInstance(this.currentAccount).saveDiffParams(MessagesStorage.getInstance(this.currentAccount).getLastSeqValue(), MessagesStorage.getInstance(this.currentAccount).getLastPtsValue(), MessagesStorage.getInstance(this.currentAccount).getLastDateValue(), MessagesStorage.getInstance(this.currentAccount).getLastQtsValue());
      return;
      i = 0;
      while (i < 3)
      {
        processUpdatesQueue(i, 0);
        i += 1;
      }
    }
  }
  
  public void putChat(final TLRPC.Chat paramChat, boolean paramBoolean)
  {
    if (paramChat == null) {}
    TLRPC.Chat localChat;
    label243:
    label383:
    label388:
    do
    {
      for (;;)
      {
        return;
        localChat = (TLRPC.Chat)this.chats.get(Integer.valueOf(paramChat.id));
        if (localChat != paramChat)
        {
          if ((localChat != null) && (!TextUtils.isEmpty(localChat.username))) {
            this.objectsByUsernames.remove(localChat.username.toLowerCase());
          }
          if (!TextUtils.isEmpty(paramChat.username)) {
            this.objectsByUsernames.put(paramChat.username.toLowerCase(), paramChat);
          }
          if (!paramChat.min) {
            break label243;
          }
          if (localChat == null) {
            break;
          }
          if (!paramBoolean)
          {
            localChat.title = paramChat.title;
            localChat.photo = paramChat.photo;
            localChat.broadcast = paramChat.broadcast;
            localChat.verified = paramChat.verified;
            localChat.megagroup = paramChat.megagroup;
            localChat.democracy = paramChat.democracy;
            if (paramChat.username != null)
            {
              localChat.username = paramChat.username;
              localChat.flags |= 0x40;
            }
            while (paramChat.participants_count != 0)
            {
              localChat.participants_count = paramChat.participants_count;
              return;
              localChat.flags &= 0xFFFFFFBF;
              localChat.username = null;
            }
          }
        }
      }
      this.chats.put(Integer.valueOf(paramChat.id), paramChat);
      return;
      if (!paramBoolean)
      {
        int i;
        if (localChat != null)
        {
          if (paramChat.version != localChat.version) {
            this.loadedFullChats.remove(Integer.valueOf(paramChat.id));
          }
          if ((localChat.participants_count != 0) && (paramChat.participants_count == 0))
          {
            paramChat.participants_count = localChat.participants_count;
            paramChat.flags |= 0x20000;
          }
          if (localChat.banned_rights == null) {
            break label383;
          }
          i = localChat.banned_rights.flags;
          if (paramChat.banned_rights == null) {
            break label388;
          }
        }
        for (int j = paramChat.banned_rights.flags;; j = 0)
        {
          if (i != j) {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.channelRightsUpdated, new Object[] { paramChat });
              }
            });
          }
          this.chats.put(Integer.valueOf(paramChat.id), paramChat);
          return;
          i = 0;
          break;
        }
      }
      if (localChat == null)
      {
        this.chats.put(Integer.valueOf(paramChat.id), paramChat);
        return;
      }
    } while (!localChat.min);
    paramChat.min = false;
    paramChat.title = localChat.title;
    paramChat.photo = localChat.photo;
    paramChat.broadcast = localChat.broadcast;
    paramChat.verified = localChat.verified;
    paramChat.megagroup = localChat.megagroup;
    paramChat.democracy = localChat.democracy;
    if (localChat.username != null)
    {
      paramChat.username = localChat.username;
      paramChat.flags |= 0x40;
    }
    for (;;)
    {
      if ((localChat.participants_count != 0) && (paramChat.participants_count == 0))
      {
        paramChat.participants_count = localChat.participants_count;
        paramChat.flags |= 0x20000;
      }
      this.chats.put(Integer.valueOf(paramChat.id), paramChat);
      return;
      paramChat.flags &= 0xFFFFFFBF;
      paramChat.username = null;
    }
  }
  
  public void putChats(ArrayList<TLRPC.Chat> paramArrayList, boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    for (;;)
    {
      return;
      int j = paramArrayList.size();
      int i = 0;
      while (i < j)
      {
        putChat((TLRPC.Chat)paramArrayList.get(i), paramBoolean);
        i += 1;
      }
    }
  }
  
  public void putEncryptedChat(TLRPC.EncryptedChat paramEncryptedChat, boolean paramBoolean)
  {
    if (paramEncryptedChat == null) {
      return;
    }
    if (paramBoolean)
    {
      this.encryptedChats.putIfAbsent(Integer.valueOf(paramEncryptedChat.id), paramEncryptedChat);
      return;
    }
    this.encryptedChats.put(Integer.valueOf(paramEncryptedChat.id), paramEncryptedChat);
  }
  
  public void putEncryptedChats(ArrayList<TLRPC.EncryptedChat> paramArrayList, boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    for (;;)
    {
      return;
      int j = paramArrayList.size();
      int i = 0;
      while (i < j)
      {
        putEncryptedChat((TLRPC.EncryptedChat)paramArrayList.get(i), paramBoolean);
        i += 1;
      }
    }
  }
  
  public boolean putUser(TLRPC.User paramUser, boolean paramBoolean)
  {
    if (paramUser == null) {}
    TLRPC.User localUser;
    label207:
    label229:
    label250:
    label268:
    do
    {
      do
      {
        int i;
        do
        {
          do
          {
            return false;
            if ((!paramBoolean) || (paramUser.id / 1000 == 333) || (paramUser.id == 777000)) {
              break;
            }
            i = 1;
            localUser = (TLRPC.User)this.users.get(Integer.valueOf(paramUser.id));
          } while (localUser == paramUser);
          if ((localUser != null) && (!TextUtils.isEmpty(localUser.username))) {
            this.objectsByUsernames.remove(localUser.username.toLowerCase());
          }
          if (!TextUtils.isEmpty(paramUser.username)) {
            this.objectsByUsernames.put(paramUser.username.toLowerCase(), paramUser);
          }
          if (!paramUser.min) {
            break label268;
          }
          if (localUser == null) {
            break label250;
          }
        } while (i != 0);
        if (paramUser.bot)
        {
          if (paramUser.username == null) {
            break label207;
          }
          localUser.username = paramUser.username;
          localUser.flags |= 0x8;
        }
        for (;;)
        {
          if (paramUser.photo == null) {
            break label229;
          }
          localUser.photo = paramUser.photo;
          localUser.flags |= 0x20;
          return false;
          i = 0;
          break;
          localUser.flags &= 0xFFFFFFF7;
          localUser.username = null;
        }
        localUser.flags &= 0xFFFFFFDF;
        localUser.photo = null;
        return false;
        this.users.put(Integer.valueOf(paramUser.id), paramUser);
        return false;
        if (i != 0) {
          break;
        }
        this.users.put(Integer.valueOf(paramUser.id), paramUser);
        if (paramUser.id == UserConfig.getInstance(this.currentAccount).getClientUserId())
        {
          UserConfig.getInstance(this.currentAccount).setCurrentUser(paramUser);
          UserConfig.getInstance(this.currentAccount).saveConfig(true);
        }
      } while ((localUser == null) || (paramUser.status == null) || (localUser.status == null) || (paramUser.status.expires == localUser.status.expires));
      return true;
      if (localUser == null)
      {
        this.users.put(Integer.valueOf(paramUser.id), paramUser);
        return false;
      }
    } while (!localUser.min);
    paramUser.min = false;
    if (localUser.bot)
    {
      if (localUser.username != null)
      {
        paramUser.username = localUser.username;
        paramUser.flags |= 0x8;
      }
    }
    else
    {
      if (localUser.photo == null) {
        break label504;
      }
      paramUser.photo = localUser.photo;
      paramUser.flags |= 0x20;
    }
    for (;;)
    {
      this.users.put(Integer.valueOf(paramUser.id), paramUser);
      return false;
      paramUser.flags &= 0xFFFFFFF7;
      paramUser.username = null;
      break;
      label504:
      paramUser.flags &= 0xFFFFFFDF;
      paramUser.photo = null;
    }
  }
  
  public void putUsers(ArrayList<TLRPC.User> paramArrayList, boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    int j;
    do
    {
      return;
      j = 0;
      int k = paramArrayList.size();
      int i = 0;
      while (i < k)
      {
        if (putUser((TLRPC.User)paramArrayList.get(i), paramBoolean)) {
          j = 1;
        }
        i += 1;
      }
    } while (j == 0);
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(4) });
      }
    });
  }
  
  public void registerForPush(final String paramString)
  {
    if ((TextUtils.isEmpty(paramString)) || (this.registeringForPush) || (UserConfig.getInstance(this.currentAccount).getClientUserId() == 0)) {}
    while ((UserConfig.getInstance(this.currentAccount).registeredForPush) && (paramString.equals(SharedConfig.pushString))) {
      return;
    }
    this.registeringForPush = true;
    this.lastPushRegisterSendTime = SystemClock.uptimeMillis();
    if (SharedConfig.pushAuthKey == null)
    {
      SharedConfig.pushAuthKey = new byte['Ā'];
      Utilities.random.nextBytes(SharedConfig.pushAuthKey);
      SharedConfig.saveConfig();
    }
    TLRPC.TL_account_registerDevice localTL_account_registerDevice = new TLRPC.TL_account_registerDevice();
    localTL_account_registerDevice.token_type = 2;
    localTL_account_registerDevice.token = paramString;
    localTL_account_registerDevice.secret = SharedConfig.pushAuthKey;
    int i = 0;
    while (i < 3)
    {
      UserConfig localUserConfig = UserConfig.getInstance(i);
      if ((i != this.currentAccount) && (localUserConfig.isClientActivated())) {
        localTL_account_registerDevice.other_uids.add(Integer.valueOf(localUserConfig.getClientUserId()));
      }
      i += 1;
    }
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_registerDevice, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if ((paramAnonymousTLObject instanceof TLRPC.TL_boolTrue))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("account " + MessagesController.this.currentAccount + " registered for push");
          }
          UserConfig.getInstance(MessagesController.this.currentAccount).registeredForPush = true;
          SharedConfig.pushString = paramString;
          UserConfig.getInstance(MessagesController.this.currentAccount).saveConfig(false);
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            MessagesController.this.registeringForPush = false;
          }
        });
      }
    });
  }
  
  public void reloadMentionsCountForChannels(final ArrayList<Integer> paramArrayList)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        int i = 0;
        while (i < paramArrayList.size())
        {
          final long l = -((Integer)paramArrayList.get(i)).intValue();
          TLRPC.TL_messages_getUnreadMentions localTL_messages_getUnreadMentions = new TLRPC.TL_messages_getUnreadMentions();
          localTL_messages_getUnreadMentions.peer = MessagesController.this.getInputPeer((int)l);
          localTL_messages_getUnreadMentions.limit = 1;
          ConnectionsManager.getInstance(MessagesController.this.currentAccount).sendRequest(localTL_messages_getUnreadMentions, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymous2TLObject;
                  if (localmessages_Messages != null) {
                    if (localmessages_Messages.count == 0) {
                      break label52;
                    }
                  }
                  label52:
                  for (int i = localmessages_Messages.count;; i = localmessages_Messages.messages.size())
                  {
                    MessagesStorage.getInstance(MessagesController.this.currentAccount).resetMentionsCount(MessagesController.70.1.this.val$dialog_id, i);
                    return;
                  }
                }
              });
            }
          });
          i += 1;
        }
      }
    });
  }
  
  public void reloadWebPages(final long paramLong, HashMap<String, ArrayList<MessageObject>> paramHashMap)
  {
    Iterator localIterator = paramHashMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      paramHashMap = (Map.Entry)localIterator.next();
      final String str = (String)paramHashMap.getKey();
      ArrayList localArrayList2 = (ArrayList)paramHashMap.getValue();
      ArrayList localArrayList1 = (ArrayList)this.reloadingWebpages.get(str);
      paramHashMap = localArrayList1;
      if (localArrayList1 == null)
      {
        paramHashMap = new ArrayList();
        this.reloadingWebpages.put(str, paramHashMap);
      }
      paramHashMap.addAll(localArrayList2);
      paramHashMap = new TLRPC.TL_messages_getWebPagePreview();
      paramHashMap.message = str;
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramHashMap, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              ArrayList localArrayList = (ArrayList)MessagesController.this.reloadingWebpages.remove(MessagesController.61.this.val$url);
              if (localArrayList == null) {}
              TLRPC.TL_messages_messages localTL_messages_messages;
              do
              {
                return;
                localTL_messages_messages = new TLRPC.TL_messages_messages();
                int i;
                if (!(paramAnonymousTLObject instanceof TLRPC.TL_messageMediaWebPage))
                {
                  i = 0;
                  while (i < localArrayList.size())
                  {
                    ((MessageObject)localArrayList.get(i)).messageOwner.media.webpage = new TLRPC.TL_webPageEmpty();
                    localTL_messages_messages.messages.add(((MessageObject)localArrayList.get(i)).messageOwner);
                    i += 1;
                  }
                }
                TLRPC.TL_messageMediaWebPage localTL_messageMediaWebPage = (TLRPC.TL_messageMediaWebPage)paramAnonymousTLObject;
                if (((localTL_messageMediaWebPage.webpage instanceof TLRPC.TL_webPage)) || ((localTL_messageMediaWebPage.webpage instanceof TLRPC.TL_webPageEmpty))) {
                  i = 0;
                }
                while (i < localArrayList.size())
                {
                  ((MessageObject)localArrayList.get(i)).messageOwner.media.webpage = localTL_messageMediaWebPage.webpage;
                  if (i == 0) {
                    ImageLoader.saveMessageThumbs(((MessageObject)localArrayList.get(i)).messageOwner);
                  }
                  localTL_messages_messages.messages.add(((MessageObject)localArrayList.get(i)).messageOwner);
                  i += 1;
                  continue;
                  MessagesController.this.reloadingWebpagesPending.put(localTL_messageMediaWebPage.webpage.id, localArrayList);
                }
              } while (localTL_messages_messages.messages.isEmpty());
              MessagesStorage.getInstance(MessagesController.this.currentAccount).putMessages(localTL_messages_messages, MessagesController.61.this.val$dialog_id, -2, 0, false);
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.replaceMessagesObjects, new Object[] { Long.valueOf(MessagesController.61.this.val$dialog_id), localArrayList });
            }
          });
        }
      });
    }
  }
  
  public void reportSpam(long paramLong, TLRPC.User paramUser, TLRPC.Chat paramChat, TLRPC.EncryptedChat paramEncryptedChat)
  {
    if ((paramUser == null) && (paramChat == null) && (paramEncryptedChat == null)) {}
    do
    {
      return;
      SharedPreferences.Editor localEditor = this.notificationsPreferences.edit();
      localEditor.putInt("spam3_" + paramLong, 1);
      localEditor.commit();
      if ((int)paramLong != 0) {
        break;
      }
    } while ((paramEncryptedChat == null) || (paramEncryptedChat.access_hash == 0L));
    paramUser = new TLRPC.TL_messages_reportEncryptedSpam();
    paramUser.peer = new TLRPC.TL_inputEncryptedChat();
    paramUser.peer.chat_id = paramEncryptedChat.id;
    paramUser.peer.access_hash = paramEncryptedChat.access_hash;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramUser, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    }, 2);
    return;
    paramEncryptedChat = new TLRPC.TL_messages_reportSpam();
    if (paramChat != null) {
      paramEncryptedChat.peer = getInputPeer(-paramChat.id);
    }
    for (;;)
    {
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(paramEncryptedChat, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      }, 2);
      return;
      if (paramUser != null) {
        paramEncryptedChat.peer = getInputPeer(paramUser.id);
      }
    }
  }
  
  public void saveGif(TLRPC.Document paramDocument)
  {
    TLRPC.TL_messages_saveGif localTL_messages_saveGif = new TLRPC.TL_messages_saveGif();
    localTL_messages_saveGif.id = new TLRPC.TL_inputDocument();
    localTL_messages_saveGif.id.id = paramDocument.id;
    localTL_messages_saveGif.id.access_hash = paramDocument.access_hash;
    localTL_messages_saveGif.unsave = false;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_saveGif, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void saveRecentSticker(TLRPC.Document paramDocument, boolean paramBoolean)
  {
    TLRPC.TL_messages_saveRecentSticker localTL_messages_saveRecentSticker = new TLRPC.TL_messages_saveRecentSticker();
    localTL_messages_saveRecentSticker.id = new TLRPC.TL_inputDocument();
    localTL_messages_saveRecentSticker.id.id = paramDocument.id;
    localTL_messages_saveRecentSticker.id.access_hash = paramDocument.access_hash;
    localTL_messages_saveRecentSticker.unsave = false;
    localTL_messages_saveRecentSticker.attached = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_saveRecentSticker, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void sendBotStart(TLRPC.User paramUser, String paramString)
  {
    if (paramUser == null) {
      return;
    }
    TLRPC.TL_messages_startBot localTL_messages_startBot = new TLRPC.TL_messages_startBot();
    localTL_messages_startBot.bot = getInputUser(paramUser);
    localTL_messages_startBot.peer = getInputPeer(paramUser.id);
    localTL_messages_startBot.start_param = paramString;
    localTL_messages_startBot.random_id = Utilities.random.nextLong();
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_startBot, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error != null) {
          return;
        }
        MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
      }
    });
  }
  
  public void sendTyping(final long paramLong, final int paramInt1, int paramInt2)
  {
    if (paramLong == 0L) {}
    do
    {
      Object localObject2;
      Object localObject1;
      do
      {
        int j;
        do
        {
          do
          {
            do
            {
              int i;
              do
              {
                do
                {
                  return;
                  localObject2 = (LongSparseArray)this.sendingTypings.get(paramInt1);
                } while ((localObject2 != null) && (((LongSparseArray)localObject2).get(paramLong) != null));
                localObject1 = localObject2;
                if (localObject2 == null)
                {
                  localObject1 = new LongSparseArray();
                  this.sendingTypings.put(paramInt1, localObject1);
                }
                i = (int)paramLong;
                j = (int)(paramLong >> 32);
                if (i == 0) {
                  break;
                }
              } while (j == 1);
              localObject2 = new TLRPC.TL_messages_setTyping();
              ((TLRPC.TL_messages_setTyping)localObject2).peer = getInputPeer(i);
              if (!(((TLRPC.TL_messages_setTyping)localObject2).peer instanceof TLRPC.TL_inputPeerChannel)) {
                break;
              }
              localObject3 = getChat(Integer.valueOf(((TLRPC.TL_messages_setTyping)localObject2).peer.channel_id));
            } while ((localObject3 == null) || (!((TLRPC.Chat)localObject3).megagroup));
          } while (((TLRPC.TL_messages_setTyping)localObject2).peer == null);
          if (paramInt1 == 0) {
            ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageTypingAction();
          }
          for (;;)
          {
            ((LongSparseArray)localObject1).put(paramLong, Boolean.valueOf(true));
            paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
            {
              public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    LongSparseArray localLongSparseArray = (LongSparseArray)MessagesController.this.sendingTypings.get(MessagesController.57.this.val$action);
                    if (localLongSparseArray != null) {
                      localLongSparseArray.remove(MessagesController.57.this.val$dialog_id);
                    }
                  }
                });
              }
            }, 2);
            if (paramInt2 == 0) {
              break;
            }
            ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt2);
            return;
            if (paramInt1 == 1) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageRecordAudioAction();
            } else if (paramInt1 == 2) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageCancelAction();
            } else if (paramInt1 == 3) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageUploadDocumentAction();
            } else if (paramInt1 == 4) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageUploadPhotoAction();
            } else if (paramInt1 == 5) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageUploadVideoAction();
            } else if (paramInt1 == 6) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageGamePlayAction();
            } else if (paramInt1 == 7) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageRecordRoundAction();
            } else if (paramInt1 == 8) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageUploadRoundAction();
            } else if (paramInt1 == 9) {
              ((TLRPC.TL_messages_setTyping)localObject2).action = new TLRPC.TL_sendMessageUploadAudioAction();
            }
          }
        } while (paramInt1 != 0);
        localObject2 = getEncryptedChat(Integer.valueOf(j));
      } while ((((TLRPC.EncryptedChat)localObject2).auth_key == null) || (((TLRPC.EncryptedChat)localObject2).auth_key.length <= 1) || (!(localObject2 instanceof TLRPC.TL_encryptedChat)));
      Object localObject3 = new TLRPC.TL_messages_setEncryptedTyping();
      ((TLRPC.TL_messages_setEncryptedTyping)localObject3).peer = new TLRPC.TL_inputEncryptedChat();
      ((TLRPC.TL_messages_setEncryptedTyping)localObject3).peer.chat_id = ((TLRPC.EncryptedChat)localObject2).id;
      ((TLRPC.TL_messages_setEncryptedTyping)localObject3).peer.access_hash = ((TLRPC.EncryptedChat)localObject2).access_hash;
      ((TLRPC.TL_messages_setEncryptedTyping)localObject3).typing = true;
      ((LongSparseArray)localObject1).put(paramLong, Boolean.valueOf(true));
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject3, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              LongSparseArray localLongSparseArray = (LongSparseArray)MessagesController.this.sendingTypings.get(MessagesController.58.this.val$action);
              if (localLongSparseArray != null) {
                localLongSparseArray.remove(MessagesController.58.this.val$dialog_id);
              }
            }
          });
        }
      }, 2);
    } while (paramInt2 == 0);
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, paramInt2);
  }
  
  public void setLastCreatedDialogId(final long paramLong, final boolean paramBoolean)
  {
    if (paramBoolean) {
      this.createdDialogMainThreadIds.add(Long.valueOf(paramLong));
    }
    for (;;)
    {
      Utilities.stageQueue.postRunnable(new Runnable()
      {
        public void run()
        {
          if (paramBoolean)
          {
            MessagesController.this.createdDialogIds.add(Long.valueOf(paramLong));
            return;
          }
          MessagesController.this.createdDialogIds.remove(Long.valueOf(paramLong));
        }
      });
      return;
      this.createdDialogMainThreadIds.remove(Long.valueOf(paramLong));
    }
  }
  
  public void setReferer(String paramString)
  {
    if (paramString == null) {
      return;
    }
    this.installReferer = paramString;
    this.mainPreferences.edit().putString("installReferer", paramString).commit();
  }
  
  public void setUserAdminRole(final int paramInt, TLRPC.User paramUser, TLRPC.TL_channelAdminRights paramTL_channelAdminRights, final boolean paramBoolean, final BaseFragment paramBaseFragment)
  {
    if ((paramUser == null) || (paramTL_channelAdminRights == null)) {
      return;
    }
    final TLRPC.TL_channels_editAdmin localTL_channels_editAdmin = new TLRPC.TL_channels_editAdmin();
    localTL_channels_editAdmin.channel = getInputChannel(paramInt);
    localTL_channels_editAdmin.user_id = getInputUser(paramUser);
    localTL_channels_editAdmin.admin_rights = paramTL_channelAdminRights;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_editAdmin, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.loadFullChat(MessagesController.32.this.val$chatId, 0, true);
            }
          }, 1000L);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            boolean bool = true;
            int i = MessagesController.this.currentAccount;
            TLRPC.TL_error localTL_error = paramAnonymousTL_error;
            BaseFragment localBaseFragment = MessagesController.32.this.val$parentFragment;
            TLRPC.TL_channels_editAdmin localTL_channels_editAdmin = MessagesController.32.this.val$req;
            if (!MessagesController.32.this.val$isMegagroup) {}
            for (;;)
            {
              AlertsCreator.processError(i, localTL_error, localBaseFragment, localTL_channels_editAdmin, new Object[] { Boolean.valueOf(bool) });
              return;
              bool = false;
            }
          }
        });
      }
    });
  }
  
  public void setUserBannedRole(final int paramInt, TLRPC.User paramUser, TLRPC.TL_channelBannedRights paramTL_channelBannedRights, final boolean paramBoolean, final BaseFragment paramBaseFragment)
  {
    if ((paramUser == null) || (paramTL_channelBannedRights == null)) {
      return;
    }
    final TLRPC.TL_channels_editBanned localTL_channels_editBanned = new TLRPC.TL_channels_editBanned();
    localTL_channels_editBanned.channel = getInputChannel(paramInt);
    localTL_channels_editBanned.user_id = getInputUser(paramUser);
    localTL_channels_editBanned.banned_rights = paramTL_channelBannedRights;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_editBanned, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.this.loadFullChat(MessagesController.31.this.val$chatId, 0, true);
            }
          }, 1000L);
          return;
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            boolean bool = true;
            int i = MessagesController.this.currentAccount;
            TLRPC.TL_error localTL_error = paramAnonymousTL_error;
            BaseFragment localBaseFragment = MessagesController.31.this.val$parentFragment;
            TLRPC.TL_channels_editBanned localTL_channels_editBanned = MessagesController.31.this.val$req;
            if (!MessagesController.31.this.val$isMegagroup) {}
            for (;;)
            {
              AlertsCreator.processError(i, localTL_error, localBaseFragment, localTL_channels_editBanned, new Object[] { Boolean.valueOf(bool) });
              return;
              bool = false;
            }
          }
        });
      }
    });
  }
  
  public void sortDialogs(SparseArray<TLRPC.Chat> paramSparseArray)
  {
    this.dialogsServerOnly.clear();
    this.dialogsGroupsOnly.clear();
    this.dialogsForward.clear();
    int j = 0;
    int m = UserConfig.getInstance(this.currentAccount).getClientUserId();
    Collections.sort(this.dialogs, this.dialogComparator);
    int i = 0;
    TLRPC.TL_dialog localTL_dialog;
    if (i < this.dialogs.size())
    {
      localTL_dialog = (TLRPC.TL_dialog)this.dialogs.get(i);
      int n = (int)(localTL_dialog.id >> 32);
      int i1 = (int)localTL_dialog.id;
      label110:
      int k;
      TLRPC.Chat localChat;
      if (i1 == m)
      {
        this.dialogsForward.add(0, localTL_dialog);
        j = 1;
        k = i;
        if (i1 != 0)
        {
          k = i;
          if (n != 1)
          {
            this.dialogsServerOnly.add(localTL_dialog);
            if (!DialogObject.isChannel(localTL_dialog)) {
              break label248;
            }
            localChat = getChat(Integer.valueOf(-i1));
            k = i;
            if (localChat != null) {
              if ((!localChat.megagroup) || (localChat.admin_rights == null) || ((!localChat.admin_rights.post_messages) && (!localChat.admin_rights.add_admins)))
              {
                k = i;
                if (!localChat.creator) {}
              }
              else
              {
                this.dialogsGroupsOnly.add(localTL_dialog);
                k = i;
              }
            }
          }
        }
      }
      for (;;)
      {
        i = k + 1;
        break;
        this.dialogsForward.add(localTL_dialog);
        break label110;
        label248:
        k = i;
        if (i1 < 0)
        {
          if (paramSparseArray != null)
          {
            localChat = (TLRPC.Chat)paramSparseArray.get(-i1);
            if ((localChat != null) && (localChat.migrated_to != null))
            {
              this.dialogs.remove(i);
              k = i - 1;
              continue;
            }
          }
          this.dialogsGroupsOnly.add(localTL_dialog);
          k = i;
        }
      }
    }
    if (j == 0)
    {
      paramSparseArray = UserConfig.getInstance(this.currentAccount).getCurrentUser();
      if (paramSparseArray != null)
      {
        localTL_dialog = new TLRPC.TL_dialog();
        localTL_dialog.id = paramSparseArray.id;
        localTL_dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
        localTL_dialog.peer = new TLRPC.TL_peerUser();
        localTL_dialog.peer.user_id = paramSparseArray.id;
        this.dialogsForward.add(0, localTL_dialog);
      }
    }
  }
  
  public void startShortPoll(final int paramInt, final boolean paramBoolean)
  {
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (paramBoolean) {
          MessagesController.this.needShortPollChannels.delete(paramInt);
        }
        do
        {
          return;
          MessagesController.this.needShortPollChannels.put(paramInt, 0);
        } while (MessagesController.this.shortPollChannels.indexOfKey(paramInt) >= 0);
        MessagesController.this.getChannelDifference(paramInt, 3, 0L, null);
      }
    });
  }
  
  public void toggleAdminMode(final int paramInt, boolean paramBoolean)
  {
    TLRPC.TL_messages_toggleChatAdmins localTL_messages_toggleChatAdmins = new TLRPC.TL_messages_toggleChatAdmins();
    localTL_messages_toggleChatAdmins.chat_id = paramInt;
    localTL_messages_toggleChatAdmins.enabled = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_toggleChatAdmins, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTL_error == null)
        {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          MessagesController.this.loadFullChat(paramInt, 0, true);
        }
      }
    });
  }
  
  public void toggleUserAdmin(int paramInt1, int paramInt2, boolean paramBoolean)
  {
    TLRPC.TL_messages_editChatAdmin localTL_messages_editChatAdmin = new TLRPC.TL_messages_editChatAdmin();
    localTL_messages_editChatAdmin.chat_id = paramInt1;
    localTL_messages_editChatAdmin.user_id = getInputUser(paramInt2);
    localTL_messages_editChatAdmin.is_admin = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_editChatAdmin, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
    });
  }
  
  public void toogleChannelInvites(int paramInt, boolean paramBoolean)
  {
    TLRPC.TL_channels_toggleInvites localTL_channels_toggleInvites = new TLRPC.TL_channels_toggleInvites();
    localTL_channels_toggleInvites.channel = getInputChannel(paramInt);
    localTL_channels_toggleInvites.enabled = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_toggleInvites, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null) {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
        }
      }
    }, 64);
  }
  
  public void toogleChannelInvitesHistory(int paramInt, boolean paramBoolean)
  {
    TLRPC.TL_channels_togglePreHistoryHidden localTL_channels_togglePreHistoryHidden = new TLRPC.TL_channels_togglePreHistoryHidden();
    localTL_channels_togglePreHistoryHidden.channel = getInputChannel(paramInt);
    localTL_channels_togglePreHistoryHidden.enabled = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_togglePreHistoryHidden, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null)
        {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(8192) });
            }
          });
        }
      }
    }, 64);
  }
  
  public void toogleChannelSignatures(int paramInt, boolean paramBoolean)
  {
    TLRPC.TL_channels_toggleSignatures localTL_channels_toggleSignatures = new TLRPC.TL_channels_toggleSignatures();
    localTL_channels_toggleSignatures.channel = getInputChannel(paramInt);
    localTL_channels_toggleSignatures.enabled = paramBoolean;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_toggleSignatures, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if (paramAnonymousTLObject != null)
        {
          MessagesController.this.processUpdates((TLRPC.Updates)paramAnonymousTLObject, false);
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(8192) });
            }
          });
        }
      }
    }, 64);
  }
  
  public void unblockUser(int paramInt)
  {
    TLRPC.TL_contacts_unblock localTL_contacts_unblock = new TLRPC.TL_contacts_unblock();
    final TLRPC.User localUser = getUser(Integer.valueOf(paramInt));
    if (localUser == null) {
      return;
    }
    this.blockedUsers.remove(Integer.valueOf(localUser.id));
    localTL_contacts_unblock.id = getInputUser(localUser);
    NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.blockedUsersDidLoaded, new Object[0]);
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_contacts_unblock, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        MessagesStorage.getInstance(MessagesController.this.currentAccount).deleteBlockedUser(localUser.id);
      }
    });
  }
  
  public void unregistedPush()
  {
    if ((UserConfig.getInstance(this.currentAccount).registeredForPush) && (SharedConfig.pushString.length() == 0))
    {
      TLRPC.TL_account_unregisterDevice localTL_account_unregisterDevice = new TLRPC.TL_account_unregisterDevice();
      localTL_account_unregisterDevice.token = SharedConfig.pushString;
      localTL_account_unregisterDevice.token_type = 2;
      int i = 0;
      while (i < 3)
      {
        UserConfig localUserConfig = UserConfig.getInstance(i);
        if ((i != this.currentAccount) && (localUserConfig.isClientActivated())) {
          localTL_account_unregisterDevice.other_uids.add(Integer.valueOf(localUserConfig.getClientUserId()));
        }
        i += 1;
      }
      ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_account_unregisterDevice, new RequestDelegate()
      {
        public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error) {}
      });
    }
  }
  
  public void updateChannelAbout(int paramInt, final String paramString, final TLRPC.ChatFull paramChatFull)
  {
    if (paramChatFull == null) {
      return;
    }
    TLRPC.TL_channels_editAbout localTL_channels_editAbout = new TLRPC.TL_channels_editAbout();
    localTL_channels_editAbout.channel = getInputChannel(paramInt);
    localTL_channels_editAbout.about = paramString;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_editAbout, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if ((paramAnonymousTLObject instanceof TLRPC.TL_boolTrue)) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              MessagesController.95.this.val$info.about = MessagesController.95.this.val$about;
              MessagesStorage.getInstance(MessagesController.this.currentAccount).updateChatInfo(MessagesController.95.this.val$info, false);
              NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.chatInfoDidLoaded, new Object[] { MessagesController.95.this.val$info, Integer.valueOf(0), Boolean.valueOf(false), null });
            }
          });
        }
      }
    }, 64);
  }
  
  public void updateChannelUserName(final int paramInt, final String paramString)
  {
    TLRPC.TL_channels_updateUsername localTL_channels_updateUsername = new TLRPC.TL_channels_updateUsername();
    localTL_channels_updateUsername.channel = getInputChannel(paramInt);
    localTL_channels_updateUsername.username = paramString;
    ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_updateUsername, new RequestDelegate()
    {
      public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        if ((paramAnonymousTLObject instanceof TLRPC.TL_boolTrue)) {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              TLRPC.Chat localChat = MessagesController.this.getChat(Integer.valueOf(MessagesController.96.this.val$chat_id));
              if (MessagesController.96.this.val$userName.length() != 0) {}
              for (localChat.flags |= 0x40;; localChat.flags &= 0xFFFFFFBF)
              {
                localChat.username = MessagesController.96.this.val$userName;
                ArrayList localArrayList = new ArrayList();
                localArrayList.add(localChat);
                MessagesStorage.getInstance(MessagesController.this.currentAccount).putUsersAndChats(null, localArrayList, true, true);
                NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(8192) });
                return;
              }
            }
          });
        }
      }
    }, 64);
  }
  
  public void updateConfig(final TLRPC.TL_config paramTL_config)
  {
    AndroidUtilities.runOnUIThread(new Runnable()
    {
      public void run()
      {
        LocaleController.getInstance().loadRemoteLanguages(MessagesController.this.currentAccount);
        MessagesController.this.maxMegagroupCount = paramTL_config.megagroup_size_max;
        MessagesController.this.maxGroupCount = paramTL_config.chat_size_max;
        MessagesController.this.maxEditTime = paramTL_config.edit_time_limit;
        MessagesController.this.ratingDecay = paramTL_config.rating_e_decay;
        MessagesController.this.maxRecentGifsCount = paramTL_config.saved_gifs_limit;
        MessagesController.this.maxRecentStickersCount = paramTL_config.stickers_recent_limit;
        MessagesController.this.maxFaveStickersCount = paramTL_config.stickers_faved_limit;
        MessagesController.this.revokeTimeLimit = paramTL_config.revoke_time_limit;
        MessagesController.this.revokeTimePmLimit = paramTL_config.revoke_pm_time_limit;
        MessagesController.this.canRevokePmInbox = paramTL_config.revoke_pm_inbox;
        MessagesController.this.linkPrefix = paramTL_config.me_url_prefix;
        if (MessagesController.this.linkPrefix.endsWith("/")) {
          MessagesController.this.linkPrefix = MessagesController.this.linkPrefix.substring(0, MessagesController.this.linkPrefix.length() - 1);
        }
        if (MessagesController.this.linkPrefix.startsWith("https://")) {
          MessagesController.this.linkPrefix = MessagesController.this.linkPrefix.substring(8);
        }
        for (;;)
        {
          MessagesController.this.callReceiveTimeout = paramTL_config.call_receive_timeout_ms;
          MessagesController.this.callRingTimeout = paramTL_config.call_ring_timeout_ms;
          MessagesController.this.callConnectTimeout = paramTL_config.call_connect_timeout_ms;
          MessagesController.this.callPacketTimeout = paramTL_config.call_packet_timeout_ms;
          MessagesController.this.maxPinnedDialogsCount = paramTL_config.pinned_dialogs_count_max;
          MessagesController.this.defaultP2pContacts = paramTL_config.default_p2p_contacts;
          MessagesController.this.preloadFeaturedStickers = paramTL_config.preload_featured_stickers;
          SharedPreferences.Editor localEditor = MessagesController.this.mainPreferences.edit();
          localEditor.putInt("maxGroupCount", MessagesController.this.maxGroupCount);
          localEditor.putInt("maxMegagroupCount", MessagesController.this.maxMegagroupCount);
          localEditor.putInt("maxEditTime", MessagesController.this.maxEditTime);
          localEditor.putInt("ratingDecay", MessagesController.this.ratingDecay);
          localEditor.putInt("maxRecentGifsCount", MessagesController.this.maxRecentGifsCount);
          localEditor.putInt("maxRecentStickersCount", MessagesController.this.maxRecentStickersCount);
          localEditor.putInt("maxFaveStickersCount", MessagesController.this.maxFaveStickersCount);
          localEditor.putInt("callReceiveTimeout", MessagesController.this.callReceiveTimeout);
          localEditor.putInt("callRingTimeout", MessagesController.this.callRingTimeout);
          localEditor.putInt("callConnectTimeout", MessagesController.this.callConnectTimeout);
          localEditor.putInt("callPacketTimeout", MessagesController.this.callPacketTimeout);
          localEditor.putString("linkPrefix", MessagesController.this.linkPrefix);
          localEditor.putInt("maxPinnedDialogsCount", MessagesController.this.maxPinnedDialogsCount);
          localEditor.putBoolean("defaultP2pContacts", MessagesController.this.defaultP2pContacts);
          localEditor.putBoolean("preloadFeaturedStickers", MessagesController.this.preloadFeaturedStickers);
          localEditor.putInt("revokeTimeLimit", MessagesController.this.revokeTimeLimit);
          localEditor.putInt("revokeTimePmLimit", MessagesController.this.revokeTimePmLimit);
          localEditor.putBoolean("canRevokePmInbox", MessagesController.this.canRevokePmInbox);
          localEditor.commit();
          LocaleController.getInstance().checkUpdateForCurrentRemoteLocale(MessagesController.this.currentAccount, paramTL_config.lang_pack_version);
          return;
          if (MessagesController.this.linkPrefix.startsWith("http://")) {
            MessagesController.this.linkPrefix = MessagesController.this.linkPrefix.substring(7);
          }
        }
      }
    });
  }
  
  protected void updateInterfaceWithMessages(long paramLong, ArrayList<MessageObject> paramArrayList)
  {
    updateInterfaceWithMessages(paramLong, paramArrayList, false);
  }
  
  protected void updateInterfaceWithMessages(long paramLong, ArrayList<MessageObject> paramArrayList, boolean paramBoolean)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {}
    int i;
    Object localObject1;
    int j;
    int k;
    int m;
    label32:
    label294:
    label336:
    do
    {
      do
      {
        do
        {
          return;
          MessageObject localMessageObject;
          int n;
          if ((int)paramLong == 0)
          {
            i = 1;
            localObject1 = null;
            j = 0;
            k = 0;
            m = 0;
            if (m >= paramArrayList.size()) {
              break label336;
            }
            localMessageObject = (MessageObject)paramArrayList.get(m);
            if ((localObject1 != null) && ((i != 0) || (localMessageObject.getId() <= ((MessageObject)localObject1).getId())) && (((i == 0) && ((localMessageObject.getId() >= 0) || (((MessageObject)localObject1).getId() >= 0))) || (localMessageObject.getId() >= ((MessageObject)localObject1).getId())))
            {
              n = j;
              localObject2 = localObject1;
              if (localMessageObject.messageOwner.date <= ((MessageObject)localObject1).messageOwner.date) {}
            }
            else
            {
              localObject1 = localMessageObject;
              n = j;
              localObject2 = localObject1;
              if (localMessageObject.messageOwner.to_id.channel_id != 0)
              {
                n = localMessageObject.messageOwner.to_id.channel_id;
                localObject2 = localObject1;
              }
            }
            if ((localMessageObject.isOut()) && (!localMessageObject.isSending()) && (!localMessageObject.isForwarded()))
            {
              if (!localMessageObject.isNewGif()) {
                break label294;
              }
              DataQuery.getInstance(this.currentAccount).addRecentGif(localMessageObject.messageOwner.media.document, localMessageObject.messageOwner.date);
            }
          }
          for (;;)
          {
            int i1 = k;
            if (localMessageObject.isOut())
            {
              i1 = k;
              if (localMessageObject.isSent()) {
                i1 = 1;
              }
            }
            m += 1;
            j = n;
            localObject1 = localObject2;
            k = i1;
            break label32;
            i = 0;
            break;
            if (localMessageObject.isSticker()) {
              DataQuery.getInstance(this.currentAccount).addRecentSticker(0, localMessageObject.messageOwner.media.document, localMessageObject.messageOwner.date, false);
            }
          }
          DataQuery.getInstance(this.currentAccount).loadReplyMessagesForMessages(paramArrayList, paramLong);
          NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.didReceivedNewMessages, new Object[] { Long.valueOf(paramLong), paramArrayList });
        } while (localObject1 == null);
        paramArrayList = (TLRPC.TL_dialog)this.dialogs_dict.get(paramLong);
        if (!(((MessageObject)localObject1).messageOwner.action instanceof TLRPC.TL_messageActionChatMigrateTo)) {
          break;
        }
      } while (paramArrayList == null);
      this.dialogs.remove(paramArrayList);
      this.dialogsServerOnly.remove(paramArrayList);
      this.dialogsGroupsOnly.remove(paramArrayList);
      this.dialogs_dict.remove(paramArrayList.id);
      this.dialogs_read_inbox_max.remove(Long.valueOf(paramArrayList.id));
      this.dialogs_read_outbox_max.remove(Long.valueOf(paramArrayList.id));
      this.nextDialogsCacheOffset -= 1;
      this.dialogMessage.remove(paramArrayList.id);
      localObject1 = (MessageObject)this.dialogMessagesByIds.get(paramArrayList.top_message);
      this.dialogMessagesByIds.remove(paramArrayList.top_message);
      if ((localObject1 != null) && (((MessageObject)localObject1).messageOwner.random_id != 0L)) {
        this.dialogMessagesByRandomIds.remove(((MessageObject)localObject1).messageOwner.random_id);
      }
      paramArrayList.top_message = 0;
      NotificationsController.getInstance(this.currentAccount).removeNotificationsForDialog(paramArrayList.id);
      NotificationCenter.getInstance(this.currentAccount).postNotificationName(NotificationCenter.needReloadRecentDialogsSearch, new Object[0]);
      return;
      m = 0;
      i = 0;
      if (paramArrayList != null) {
        break label837;
      }
      if (paramBoolean) {
        break;
      }
      paramArrayList = getChat(Integer.valueOf(j));
    } while (((j != 0) && (paramArrayList == null)) || ((paramArrayList != null) && (paramArrayList.left)));
    Object localObject2 = new TLRPC.TL_dialog();
    ((TLRPC.TL_dialog)localObject2).id = paramLong;
    ((TLRPC.TL_dialog)localObject2).unread_count = 0;
    ((TLRPC.TL_dialog)localObject2).top_message = ((MessageObject)localObject1).getId();
    ((TLRPC.TL_dialog)localObject2).last_message_date = ((MessageObject)localObject1).messageOwner.date;
    if (ChatObject.isChannel(paramArrayList))
    {
      i = 1;
      label696:
      ((TLRPC.TL_dialog)localObject2).flags = i;
      this.dialogs_dict.put(paramLong, localObject2);
      this.dialogs.add(localObject2);
      this.dialogMessage.put(paramLong, localObject1);
      if (((MessageObject)localObject1).messageOwner.to_id.channel_id == 0)
      {
        this.dialogMessagesByIds.put(((MessageObject)localObject1).getId(), localObject1);
        if (((MessageObject)localObject1).messageOwner.random_id != 0L) {
          this.dialogMessagesByRandomIds.put(((MessageObject)localObject1).messageOwner.random_id, localObject1);
        }
      }
      this.nextDialogsCacheOffset += 1;
      i = 1;
    }
    for (;;)
    {
      if (i != 0) {
        sortDialogs(null);
      }
      if (k == 0) {
        break;
      }
      DataQuery.getInstance(this.currentAccount).increasePeerRaiting(paramLong);
      return;
      i = 0;
      break label696;
      label837:
      if (((paramArrayList.top_message > 0) && (((MessageObject)localObject1).getId() > 0) && (((MessageObject)localObject1).getId() > paramArrayList.top_message)) || ((paramArrayList.top_message < 0) && (((MessageObject)localObject1).getId() < 0) && (((MessageObject)localObject1).getId() < paramArrayList.top_message)) || (this.dialogMessage.indexOfKey(paramLong) < 0) || (paramArrayList.top_message < 0) || (paramArrayList.last_message_date <= ((MessageObject)localObject1).messageOwner.date))
      {
        localObject2 = (MessageObject)this.dialogMessagesByIds.get(paramArrayList.top_message);
        this.dialogMessagesByIds.remove(paramArrayList.top_message);
        if ((localObject2 != null) && (((MessageObject)localObject2).messageOwner.random_id != 0L)) {
          this.dialogMessagesByRandomIds.remove(((MessageObject)localObject2).messageOwner.random_id);
        }
        paramArrayList.top_message = ((MessageObject)localObject1).getId();
        j = m;
        if (!paramBoolean)
        {
          paramArrayList.last_message_date = ((MessageObject)localObject1).messageOwner.date;
          j = 1;
        }
        this.dialogMessage.put(paramLong, localObject1);
        i = j;
        if (((MessageObject)localObject1).messageOwner.to_id.channel_id == 0)
        {
          this.dialogMessagesByIds.put(((MessageObject)localObject1).getId(), localObject1);
          i = j;
          if (((MessageObject)localObject1).messageOwner.random_id != 0L)
          {
            this.dialogMessagesByRandomIds.put(((MessageObject)localObject1).messageOwner.random_id, localObject1);
            i = j;
          }
        }
      }
    }
  }
  
  public void updateTimerProc()
  {
    long l1 = System.currentTimeMillis();
    checkDeletingTask(false);
    checkReadTasks();
    final Object localObject1;
    label188:
    int i;
    final int j;
    if (UserConfig.getInstance(this.currentAccount).isClientActivated())
    {
      if ((ConnectionsManager.getInstance(this.currentAccount).getPauseTime() == 0L) && (ApplicationLoader.isScreenOn) && (!ApplicationLoader.mainInterfacePausedStageQueue)) {
        if ((ApplicationLoader.mainInterfacePausedStageQueueTime != 0L) && (Math.abs(ApplicationLoader.mainInterfacePausedStageQueueTime - System.currentTimeMillis()) > 1000L) && (this.statusSettingState != 1) && ((this.lastStatusUpdateTime == 0L) || (Math.abs(System.currentTimeMillis() - this.lastStatusUpdateTime) >= 55000L) || (this.offlineSent)))
        {
          this.statusSettingState = 1;
          if (this.statusRequest != 0) {
            ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.statusRequest, true);
          }
          localObject1 = new TLRPC.TL_account_updateStatus();
          ((TLRPC.TL_account_updateStatus)localObject1).offline = false;
        }
      }
      for (this.statusRequest = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTL_error == null)
              {
                MessagesController.access$4802(MessagesController.this, System.currentTimeMillis());
                MessagesController.access$4902(MessagesController.this, false);
                MessagesController.access$5002(MessagesController.this, 0);
              }
              for (;;)
              {
                MessagesController.access$5102(MessagesController.this, 0);
                return;
                if (MessagesController.this.lastStatusUpdateTime != 0L) {
                  MessagesController.access$4802(MessagesController.this, MessagesController.this.lastStatusUpdateTime + 5000L);
                }
              }
            }
          }); this.updatesQueueChannels.size() != 0; this.statusRequest = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTL_error == null) {
                MessagesController.access$4902(MessagesController.this, true);
              }
              for (;;)
              {
                MessagesController.access$5102(MessagesController.this, 0);
                return;
                if (MessagesController.this.lastStatusUpdateTime != 0L) {
                  MessagesController.access$4802(MessagesController.this, MessagesController.this.lastStatusUpdateTime + 5000L);
                }
              }
            }
          }))
      {
        i = 0;
        while (i < this.updatesQueueChannels.size())
        {
          j = this.updatesQueueChannels.keyAt(i);
          if (1500L + this.updatesStartWaitTimeChannels.valueAt(i) < l1)
          {
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("QUEUE CHANNEL " + j + " UPDATES WAIT TIMEOUT - CHECK QUEUE");
            }
            processChannelsUpdatesQueue(j, 0);
          }
          i += 1;
        }
        if ((this.statusSettingState == 2) || (this.offlineSent) || (Math.abs(System.currentTimeMillis() - ConnectionsManager.getInstance(this.currentAccount).getPauseTime()) < 2000L)) {
          break label188;
        }
        this.statusSettingState = 2;
        if (this.statusRequest != 0) {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.statusRequest, true);
        }
        localObject1 = new TLRPC.TL_account_updateStatus();
        ((TLRPC.TL_account_updateStatus)localObject1).offline = true;
      }
      i = 0;
      while (i < 3)
      {
        if ((getUpdatesStartTime(i) != 0L) && (getUpdatesStartTime(i) + 1500L < l1))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d(i + " QUEUE UPDATES WAIT TIMEOUT - CHECK QUEUE");
          }
          processUpdatesQueue(i, 0);
        }
        i += 1;
      }
    }
    if ((this.channelViewsToSend.size() != 0) && (Math.abs(System.currentTimeMillis() - this.lastViewsCheckTime) >= 5000L))
    {
      this.lastViewsCheckTime = System.currentTimeMillis();
      i = 0;
      if (i < this.channelViewsToSend.size())
      {
        j = this.channelViewsToSend.keyAt(i);
        localObject1 = new TLRPC.TL_messages_getMessagesViews();
        ((TLRPC.TL_messages_getMessagesViews)localObject1).peer = getInputPeer(j);
        ((TLRPC.TL_messages_getMessagesViews)localObject1).id = ((ArrayList)this.channelViewsToSend.valueAt(i));
        if (i == 0) {}
        for (boolean bool = true;; bool = false)
        {
          ((TLRPC.TL_messages_getMessagesViews)localObject1).increment = bool;
          ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject1, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
            {
              TLRPC.Vector localVector;
              final SparseArray localSparseArray;
              int i;
              if (paramAnonymousTL_error == null)
              {
                localVector = (TLRPC.Vector)paramAnonymousTLObject;
                localSparseArray = new SparseArray();
                paramAnonymousTL_error = (SparseIntArray)localSparseArray.get(j);
                paramAnonymousTLObject = paramAnonymousTL_error;
                if (paramAnonymousTL_error == null)
                {
                  paramAnonymousTLObject = new SparseIntArray();
                  localSparseArray.put(j, paramAnonymousTLObject);
                }
                i = 0;
              }
              for (;;)
              {
                if ((i >= localObject1.id.size()) || (i >= localVector.objects.size()))
                {
                  MessagesStorage.getInstance(MessagesController.this.currentAccount).putChannelViews(localSparseArray, localObject1.peer instanceof TLRPC.TL_inputPeerChannel);
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.didUpdatedMessagesViews, new Object[] { localSparseArray });
                    }
                  });
                  return;
                }
                paramAnonymousTLObject.put(((Integer)localObject1.id.get(i)).intValue(), ((Integer)localVector.objects.get(i)).intValue());
                i += 1;
              }
            }
          });
          i += 1;
          break;
        }
      }
      this.channelViewsToSend.clear();
    }
    Object localObject3;
    Object localObject2;
    if (!this.onlinePrivacy.isEmpty())
    {
      localObject1 = null;
      i = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      localObject3 = this.onlinePrivacy.entrySet().iterator();
      while (((Iterator)localObject3).hasNext())
      {
        Map.Entry localEntry = (Map.Entry)((Iterator)localObject3).next();
        if (((Integer)localEntry.getValue()).intValue() < i - 30)
        {
          localObject2 = localObject1;
          if (localObject1 == null) {
            localObject2 = new ArrayList();
          }
          ((ArrayList)localObject2).add(localEntry.getKey());
          localObject1 = localObject2;
        }
      }
      if (localObject1 != null)
      {
        localObject1 = ((ArrayList)localObject1).iterator();
        while (((Iterator)localObject1).hasNext())
        {
          localObject2 = (Integer)((Iterator)localObject1).next();
          this.onlinePrivacy.remove(localObject2);
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(4) });
          }
        });
      }
    }
    if (this.shortPollChannels.size() != 0)
    {
      i = 0;
      while (i < this.shortPollChannels.size())
      {
        j = this.shortPollChannels.keyAt(i);
        if (this.shortPollChannels.valueAt(i) < System.currentTimeMillis() / 1000L)
        {
          this.shortPollChannels.delete(j);
          if (this.needShortPollChannels.indexOfKey(j) >= 0) {
            getChannelDifference(j);
          }
        }
        i += 1;
      }
    }
    if ((!this.printingUsers.isEmpty()) || (this.lastPrintingStringCount != this.printingUsers.size()))
    {
      int k = 0;
      localObject1 = new ArrayList(this.printingUsers.keySet());
      i = 0;
      while (i < ((ArrayList)localObject1).size())
      {
        long l2 = ((Long)((ArrayList)localObject1).get(i)).longValue();
        localObject2 = (ArrayList)this.printingUsers.get(Long.valueOf(l2));
        int m = k;
        if (localObject2 != null)
        {
          j = 0;
          m = k;
          if (j < ((ArrayList)localObject2).size())
          {
            localObject3 = (PrintingUser)((ArrayList)localObject2).get(j);
            if ((((PrintingUser)localObject3).action instanceof TLRPC.TL_sendMessageGamePlayAction)) {}
            for (m = 30000;; m = 5900)
            {
              int n = j;
              if (((PrintingUser)localObject3).lastTime + m < l1)
              {
                k = 1;
                ((ArrayList)localObject2).remove(localObject3);
                n = j - 1;
              }
              j = n + 1;
              break;
            }
          }
        }
        if (localObject2 != null)
        {
          j = i;
          if (!((ArrayList)localObject2).isEmpty()) {}
        }
        else
        {
          this.printingUsers.remove(Long.valueOf(l2));
          ((ArrayList)localObject1).remove(i);
          j = i - 1;
        }
        i = j + 1;
        k = m;
      }
      updatePrintingStrings();
      if (k != 0) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            NotificationCenter.getInstance(MessagesController.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(64) });
          }
        });
      }
    }
    if ((Theme.selectedAutoNightType == 1) && (Math.abs(l1 - lastThemeCheckTime) >= 60L))
    {
      AndroidUtilities.runOnUIThread(this.themeCheckRunnable);
      lastThemeCheckTime = l1;
    }
    if ((this.lastPushRegisterSendTime != 0L) && (Math.abs(SystemClock.uptimeMillis() - this.lastPushRegisterSendTime) >= 10800000L)) {
      GcmInstanceIDListenerService.sendRegistrationToServer(SharedConfig.pushString);
    }
    LocationController.getInstance(this.currentAccount).update();
  }
  
  public void uploadAndApplyUserAvatar(TLRPC.PhotoSize paramPhotoSize)
  {
    if (paramPhotoSize != null)
    {
      this.uploadingAvatar = (FileLoader.getDirectory(4) + "/" + paramPhotoSize.location.volume_id + "_" + paramPhotoSize.location.local_id + ".jpg");
      FileLoader.getInstance(this.currentAccount).uploadFile(this.uploadingAvatar, false, true, 16777216);
    }
  }
  
  public static class PrintingUser
  {
    public TLRPC.SendMessageAction action;
    public long lastTime;
    public int userId;
  }
  
  private class ReadTask
  {
    public long dialogId;
    public int maxDate;
    public int maxId;
    public long sendRequestTime;
    
    private ReadTask() {}
  }
  
  private class UserActionUpdatesPts
    extends TLRPC.Updates
  {
    private UserActionUpdatesPts() {}
  }
  
  private class UserActionUpdatesSeq
    extends TLRPC.Updates
  {
    private UserActionUpdatesSeq() {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/messenger/MessagesController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */