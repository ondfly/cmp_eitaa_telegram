package org.telegram.ui.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.EmojiSuggestion;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.SendMessagesHelper.LocationProvider;
import org.telegram.messenger.SendMessagesHelper.LocationProvider.LocationProviderDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.BotInfo;
import org.telegram.tgnet.TLRPC.BotInlineResult;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.ChatParticipants;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_botCommand;
import org.telegram.tgnet.TLRPC.TL_botInlineMessageMediaAuto;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsSearch;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inlineBotSwitchPM;
import org.telegram.tgnet.TLRPC.TL_inputGeoPoint;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_messages_botResults;
import org.telegram.tgnet.TLRPC.TL_messages_getInlineBotResults;
import org.telegram.tgnet.TLRPC.TL_photo;
import org.telegram.tgnet.TLRPC.TL_topPeer;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.BotSwitchCell;
import org.telegram.ui.Cells.ContextLinkCell;
import org.telegram.ui.Cells.ContextLinkCell.ContextLinkCellDelegate;
import org.telegram.ui.Cells.MentionCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class MentionsAdapter
  extends RecyclerListView.SelectionAdapter
{
  private SparseArray<TLRPC.BotInfo> botInfo;
  private int botsCount;
  private int channelLastReqId;
  private int channelReqId;
  private boolean contextMedia;
  private int contextQueryReqid;
  private Runnable contextQueryRunnable;
  private int contextUsernameReqid;
  private int currentAccount = UserConfig.selectedAccount;
  private MentionsAdapterDelegate delegate;
  private long dialog_id;
  private TLRPC.User foundContextBot;
  private TLRPC.ChatFull info;
  private boolean inlineMediaEnabled = true;
  private boolean isDarkTheme;
  private boolean isSearchingMentions;
  private Location lastKnownLocation;
  private int lastPosition;
  private String lastText;
  private boolean lastUsernameOnly;
  private SendMessagesHelper.LocationProvider locationProvider = new SendMessagesHelper.LocationProvider(new SendMessagesHelper.LocationProvider.LocationProviderDelegate()
  {
    public void onLocationAcquired(Location paramAnonymousLocation)
    {
      if ((MentionsAdapter.this.foundContextBot != null) && (MentionsAdapter.this.foundContextBot.bot_inline_geo))
      {
        MentionsAdapter.access$102(MentionsAdapter.this, paramAnonymousLocation);
        MentionsAdapter.this.searchForContextBotResults(true, MentionsAdapter.this.foundContextBot, MentionsAdapter.this.searchingContextQuery, "");
      }
    }
    
    public void onUnableLocationAcquire()
    {
      MentionsAdapter.this.onLocationUnavailable();
    }
  })
  {
    public void stop()
    {
      super.stop();
      MentionsAdapter.access$102(MentionsAdapter.this, null);
    }
  };
  private Context mContext;
  private ArrayList<MessageObject> messages;
  private boolean needBotContext = true;
  private boolean needUsernames = true;
  private String nextQueryOffset;
  private boolean noUserName;
  private ChatActivity parentFragment;
  private int resultLength;
  private int resultStartPosition;
  private SearchAdapterHelper searchAdapterHelper;
  private Runnable searchGlobalRunnable;
  private ArrayList<TLRPC.BotInlineResult> searchResultBotContext;
  private HashMap<String, TLRPC.BotInlineResult> searchResultBotContextById;
  private TLRPC.TL_inlineBotSwitchPM searchResultBotContextSwitch;
  private ArrayList<String> searchResultCommands;
  private ArrayList<String> searchResultCommandsHelp;
  private ArrayList<TLRPC.User> searchResultCommandsUsers;
  private ArrayList<String> searchResultHashtags;
  private ArrayList<EmojiSuggestion> searchResultSuggestions;
  private ArrayList<TLRPC.User> searchResultUsernames;
  private SparseArray<TLRPC.User> searchResultUsernamesMap;
  private String searchingContextQuery;
  private String searchingContextUsername;
  
  public MentionsAdapter(Context paramContext, boolean paramBoolean, long paramLong, MentionsAdapterDelegate paramMentionsAdapterDelegate)
  {
    this.mContext = paramContext;
    this.delegate = paramMentionsAdapterDelegate;
    this.isDarkTheme = paramBoolean;
    this.dialog_id = paramLong;
    this.searchAdapterHelper = new SearchAdapterHelper(true);
    this.searchAdapterHelper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate()
    {
      public void onDataSetChanged()
      {
        MentionsAdapter.this.notifyDataSetChanged();
      }
      
      public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramAnonymousArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramAnonymousHashMap)
      {
        if (MentionsAdapter.this.lastText != null) {
          MentionsAdapter.this.searchUsernameOrHashtag(MentionsAdapter.this.lastText, MentionsAdapter.this.lastPosition, MentionsAdapter.this.messages, MentionsAdapter.this.lastUsernameOnly);
        }
      }
    });
  }
  
  private void checkLocationPermissionsOrStart()
  {
    if ((this.parentFragment == null) || (this.parentFragment.getParentActivity() == null)) {}
    do
    {
      return;
      if ((Build.VERSION.SDK_INT >= 23) && (this.parentFragment.getParentActivity().checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0))
      {
        this.parentFragment.getParentActivity().requestPermissions(new String[] { "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION" }, 2);
        return;
      }
    } while ((this.foundContextBot == null) || (!this.foundContextBot.bot_inline_geo));
    this.locationProvider.start();
  }
  
  private void onLocationUnavailable()
  {
    if ((this.foundContextBot != null) && (this.foundContextBot.bot_inline_geo))
    {
      this.lastKnownLocation = new Location("network");
      this.lastKnownLocation.setLatitude(-1000.0D);
      this.lastKnownLocation.setLongitude(-1000.0D);
      searchForContextBotResults(true, this.foundContextBot, this.searchingContextQuery, "");
    }
  }
  
  private void processFoundUser(final TLRPC.User paramUser)
  {
    this.contextUsernameReqid = 0;
    this.locationProvider.stop();
    if ((paramUser != null) && (paramUser.bot) && (paramUser.bot_inline_placeholder != null))
    {
      this.foundContextBot = paramUser;
      if (this.parentFragment != null)
      {
        paramUser = this.parentFragment.getCurrentChat();
        if (paramUser != null)
        {
          this.inlineMediaEnabled = ChatObject.canSendStickers(paramUser);
          if (!this.inlineMediaEnabled)
          {
            notifyDataSetChanged();
            this.delegate.needChangePanelVisibility(true);
            return;
          }
        }
      }
      if (this.foundContextBot.bot_inline_geo)
      {
        if ((MessagesController.getNotificationsSettings(this.currentAccount).getBoolean("inlinegeo_" + this.foundContextBot.id, false)) || (this.parentFragment == null) || (this.parentFragment.getParentActivity() == null)) {
          break label286;
        }
        paramUser = this.foundContextBot;
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this.parentFragment.getParentActivity());
        localBuilder.setTitle(LocaleController.getString("ShareYouLocationTitle", 2131494391));
        localBuilder.setMessage(LocaleController.getString("ShareYouLocationInline", 2131494390));
        final boolean[] arrayOfBoolean = new boolean[1];
        localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            arrayOfBoolean[0] = true;
            if (paramUser != null)
            {
              MessagesController.getNotificationsSettings(MentionsAdapter.this.currentAccount).edit().putBoolean("inlinegeo_" + paramUser.id, true).commit();
              MentionsAdapter.this.checkLocationPermissionsOrStart();
            }
          }
        });
        localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            arrayOfBoolean[0] = true;
            MentionsAdapter.this.onLocationUnavailable();
          }
        });
        this.parentFragment.showDialog(localBuilder.create(), new DialogInterface.OnDismissListener()
        {
          public void onDismiss(DialogInterface paramAnonymousDialogInterface)
          {
            if (arrayOfBoolean[0] == 0) {
              MentionsAdapter.this.onLocationUnavailable();
            }
          }
        });
      }
    }
    while (this.foundContextBot == null)
    {
      this.noUserName = true;
      return;
      label286:
      checkLocationPermissionsOrStart();
      continue;
      this.foundContextBot = null;
      this.inlineMediaEnabled = true;
    }
    if (this.delegate != null) {
      this.delegate.onContextSearch(true);
    }
    searchForContextBotResults(true, this.foundContextBot, this.searchingContextQuery, "");
  }
  
  private void searchForContextBot(final String paramString1, final String paramString2)
  {
    if ((this.foundContextBot != null) && (this.foundContextBot.username != null) && (this.foundContextBot.username.equals(paramString1)) && (this.searchingContextQuery != null) && (this.searchingContextQuery.equals(paramString2))) {}
    do
    {
      do
      {
        do
        {
          return;
          this.searchResultBotContext = null;
          this.searchResultBotContextById = null;
          this.searchResultBotContextSwitch = null;
          notifyDataSetChanged();
          if (this.foundContextBot == null) {
            break;
          }
        } while ((!this.inlineMediaEnabled) && (paramString1 != null) && (paramString2 != null));
        this.delegate.needChangePanelVisibility(false);
        if (this.contextQueryRunnable != null)
        {
          AndroidUtilities.cancelRunOnUIThread(this.contextQueryRunnable);
          this.contextQueryRunnable = null;
        }
        if ((!TextUtils.isEmpty(paramString1)) && ((this.searchingContextUsername == null) || (this.searchingContextUsername.equals(paramString1)))) {
          break;
        }
        if (this.contextUsernameReqid != 0)
        {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextUsernameReqid, true);
          this.contextUsernameReqid = 0;
        }
        if (this.contextQueryReqid != 0)
        {
          ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextQueryReqid, true);
          this.contextQueryReqid = 0;
        }
        this.foundContextBot = null;
        this.inlineMediaEnabled = true;
        this.searchingContextUsername = null;
        this.searchingContextQuery = null;
        this.locationProvider.stop();
        this.noUserName = false;
        if (this.delegate != null) {
          this.delegate.onContextSearch(false);
        }
      } while ((paramString1 == null) || (paramString1.length() == 0));
      if (paramString2 != null) {
        break;
      }
      if (this.contextQueryReqid != 0)
      {
        ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextQueryReqid, true);
        this.contextQueryReqid = 0;
      }
      this.searchingContextQuery = null;
    } while (this.delegate == null);
    this.delegate.onContextSearch(false);
    return;
    if (this.delegate != null)
    {
      if (this.foundContextBot == null) {
        break label387;
      }
      this.delegate.onContextSearch(true);
    }
    for (;;)
    {
      final MessagesController localMessagesController = MessagesController.getInstance(this.currentAccount);
      final MessagesStorage localMessagesStorage = MessagesStorage.getInstance(this.currentAccount);
      this.searchingContextQuery = paramString2;
      this.contextQueryRunnable = new Runnable()
      {
        public void run()
        {
          if (MentionsAdapter.this.contextQueryRunnable != this) {}
          do
          {
            return;
            MentionsAdapter.access$1102(MentionsAdapter.this, null);
            if ((MentionsAdapter.this.foundContextBot == null) && (!MentionsAdapter.this.noUserName)) {
              break;
            }
          } while (MentionsAdapter.this.noUserName);
          MentionsAdapter.this.searchForContextBotResults(true, MentionsAdapter.this.foundContextBot, paramString2, "");
          return;
          MentionsAdapter.access$1302(MentionsAdapter.this, paramString1);
          Object localObject = localMessagesController.getUserOrChat(MentionsAdapter.this.searchingContextUsername);
          if ((localObject instanceof TLRPC.User))
          {
            MentionsAdapter.this.processFoundUser((TLRPC.User)localObject);
            return;
          }
          localObject = new TLRPC.TL_contacts_resolveUsername();
          ((TLRPC.TL_contacts_resolveUsername)localObject).username = MentionsAdapter.this.searchingContextUsername;
          MentionsAdapter.access$1502(MentionsAdapter.this, ConnectionsManager.getInstance(MentionsAdapter.this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
            {
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if ((MentionsAdapter.this.searchingContextUsername == null) || (!MentionsAdapter.this.searchingContextUsername.equals(MentionsAdapter.7.this.val$username))) {
                    return;
                  }
                  Object localObject2 = null;
                  Object localObject1 = localObject2;
                  if (paramAnonymous2TL_error == null)
                  {
                    TLRPC.TL_contacts_resolvedPeer localTL_contacts_resolvedPeer = (TLRPC.TL_contacts_resolvedPeer)paramAnonymous2TLObject;
                    localObject1 = localObject2;
                    if (!localTL_contacts_resolvedPeer.users.isEmpty())
                    {
                      localObject1 = (TLRPC.User)localTL_contacts_resolvedPeer.users.get(0);
                      MentionsAdapter.7.this.val$messagesController.putUser((TLRPC.User)localObject1, false);
                      MentionsAdapter.7.this.val$messagesStorage.putUsersAndChats(localTL_contacts_resolvedPeer.users, null, true, true);
                    }
                  }
                  MentionsAdapter.this.processFoundUser((TLRPC.User)localObject1);
                }
              });
            }
          }));
        }
      };
      AndroidUtilities.runOnUIThread(this.contextQueryRunnable, 400L);
      return;
      label387:
      if (paramString1.equals("gif"))
      {
        this.searchingContextUsername = "gif";
        this.delegate.onContextSearch(false);
      }
    }
  }
  
  private void searchForContextBotResults(final boolean paramBoolean, final TLRPC.User paramUser, final String paramString1, final String paramString2)
  {
    if (this.contextQueryReqid != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextQueryReqid, true);
      this.contextQueryReqid = 0;
    }
    if (!this.inlineMediaEnabled) {
      if (this.delegate != null) {
        this.delegate.onContextSearch(false);
      }
    }
    do
    {
      return;
      if ((paramString1 == null) || (paramUser == null))
      {
        this.searchingContextQuery = null;
        return;
      }
    } while ((paramUser.bot_inline_geo) && (this.lastKnownLocation == null));
    final Object localObject2 = new StringBuilder().append(this.dialog_id).append("_").append(paramString1).append("_").append(paramString2).append("_").append(this.dialog_id).append("_").append(paramUser.id).append("_");
    if ((paramUser.bot_inline_geo) && (this.lastKnownLocation != null) && (this.lastKnownLocation.getLatitude() != -1000.0D)) {}
    for (Object localObject1 = Double.valueOf(this.lastKnownLocation.getLatitude() + this.lastKnownLocation.getLongitude());; localObject1 = "")
    {
      localObject2 = localObject1;
      final MessagesStorage localMessagesStorage = MessagesStorage.getInstance(this.currentAccount);
      localObject1 = new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              boolean bool = false;
              if ((MentionsAdapter.this.searchingContextQuery == null) || (!MentionsAdapter.8.this.val$query.equals(MentionsAdapter.this.searchingContextQuery))) {}
              Object localObject1;
              Object localObject2;
              int j;
              for (;;)
              {
                return;
                MentionsAdapter.access$1602(MentionsAdapter.this, 0);
                if ((MentionsAdapter.8.this.val$cache) && (paramAnonymousTLObject == null)) {
                  MentionsAdapter.this.searchForContextBotResults(false, MentionsAdapter.8.this.val$user, MentionsAdapter.8.this.val$query, MentionsAdapter.8.this.val$offset);
                }
                while (paramAnonymousTLObject != null)
                {
                  localObject1 = (TLRPC.TL_messages_botResults)paramAnonymousTLObject;
                  if ((!MentionsAdapter.8.this.val$cache) && (((TLRPC.TL_messages_botResults)localObject1).cache_time != 0)) {
                    MentionsAdapter.8.this.val$messagesStorage.saveBotCache(MentionsAdapter.8.this.val$key, (TLObject)localObject1);
                  }
                  MentionsAdapter.access$1802(MentionsAdapter.this, ((TLRPC.TL_messages_botResults)localObject1).next_offset);
                  if (MentionsAdapter.this.searchResultBotContextById == null)
                  {
                    MentionsAdapter.access$1902(MentionsAdapter.this, new HashMap());
                    MentionsAdapter.access$2002(MentionsAdapter.this, ((TLRPC.TL_messages_botResults)localObject1).switch_pm);
                  }
                  for (i = 0; i < ((TLRPC.TL_messages_botResults)localObject1).results.size(); i = j + 1)
                  {
                    localObject2 = (TLRPC.BotInlineResult)((TLRPC.TL_messages_botResults)localObject1).results.get(i);
                    if (!MentionsAdapter.this.searchResultBotContextById.containsKey(((TLRPC.BotInlineResult)localObject2).id))
                    {
                      j = i;
                      if (!(((TLRPC.BotInlineResult)localObject2).document instanceof TLRPC.TL_document))
                      {
                        j = i;
                        if (!(((TLRPC.BotInlineResult)localObject2).photo instanceof TLRPC.TL_photo))
                        {
                          j = i;
                          if (((TLRPC.BotInlineResult)localObject2).content == null)
                          {
                            j = i;
                            if (!(((TLRPC.BotInlineResult)localObject2).send_message instanceof TLRPC.TL_botInlineMessageMediaAuto)) {}
                          }
                        }
                      }
                    }
                    else
                    {
                      ((TLRPC.TL_messages_botResults)localObject1).results.remove(i);
                      j = i - 1;
                    }
                    ((TLRPC.BotInlineResult)localObject2).query_id = ((TLRPC.TL_messages_botResults)localObject1).query_id;
                    MentionsAdapter.this.searchResultBotContextById.put(((TLRPC.BotInlineResult)localObject2).id, localObject2);
                  }
                  if (MentionsAdapter.this.delegate != null) {
                    MentionsAdapter.this.delegate.onContextSearch(false);
                  }
                }
              }
              int i = 0;
              if ((MentionsAdapter.this.searchResultBotContext == null) || (MentionsAdapter.8.this.val$offset.length() == 0))
              {
                MentionsAdapter.access$2102(MentionsAdapter.this, ((TLRPC.TL_messages_botResults)localObject1).results);
                MentionsAdapter.access$2202(MentionsAdapter.this, ((TLRPC.TL_messages_botResults)localObject1).gallery);
                MentionsAdapter.access$2302(MentionsAdapter.this, null);
                MentionsAdapter.access$2402(MentionsAdapter.this, null);
                MentionsAdapter.access$2502(MentionsAdapter.this, null);
                MentionsAdapter.access$2602(MentionsAdapter.this, null);
                MentionsAdapter.access$2702(MentionsAdapter.this, null);
                MentionsAdapter.access$2802(MentionsAdapter.this, null);
                MentionsAdapter.access$2902(MentionsAdapter.this, null);
                if (i == 0) {
                  break label791;
                }
                if (MentionsAdapter.this.searchResultBotContextSwitch == null) {
                  break label776;
                }
                i = 1;
                label562:
                localObject2 = MentionsAdapter.this;
                int k = MentionsAdapter.this.searchResultBotContext.size();
                int m = ((TLRPC.TL_messages_botResults)localObject1).results.size();
                if (i == 0) {
                  break label781;
                }
                j = 1;
                label601:
                ((MentionsAdapter)localObject2).notifyItemChanged(j + (k - m) - 1);
                localObject2 = MentionsAdapter.this;
                j = MentionsAdapter.this.searchResultBotContext.size();
                k = ((TLRPC.TL_messages_botResults)localObject1).results.size();
                if (i == 0) {
                  break label786;
                }
                i = 1;
                label652:
                ((MentionsAdapter)localObject2).notifyItemRangeInserted(i + (j - k), ((TLRPC.TL_messages_botResults)localObject1).results.size());
              }
              for (;;)
              {
                localObject1 = MentionsAdapter.this.delegate;
                if ((!MentionsAdapter.this.searchResultBotContext.isEmpty()) || (MentionsAdapter.this.searchResultBotContextSwitch != null)) {
                  bool = true;
                }
                ((MentionsAdapter.MentionsAdapterDelegate)localObject1).needChangePanelVisibility(bool);
                return;
                j = 1;
                MentionsAdapter.this.searchResultBotContext.addAll(((TLRPC.TL_messages_botResults)localObject1).results);
                i = j;
                if (!((TLRPC.TL_messages_botResults)localObject1).results.isEmpty()) {
                  break;
                }
                MentionsAdapter.access$1802(MentionsAdapter.this, "");
                i = j;
                break;
                label776:
                i = 0;
                break label562;
                label781:
                j = 0;
                break label601;
                label786:
                i = 0;
                break label652;
                label791:
                MentionsAdapter.this.notifyDataSetChanged();
              }
            }
          });
        }
      };
      if (!paramBoolean) {
        break;
      }
      localMessagesStorage.getBotCache((String)localObject2, (RequestDelegate)localObject1);
      return;
    }
    localObject2 = new TLRPC.TL_messages_getInlineBotResults();
    ((TLRPC.TL_messages_getInlineBotResults)localObject2).bot = MessagesController.getInstance(this.currentAccount).getInputUser(paramUser);
    ((TLRPC.TL_messages_getInlineBotResults)localObject2).query = paramString1;
    ((TLRPC.TL_messages_getInlineBotResults)localObject2).offset = paramString2;
    if ((paramUser.bot_inline_geo) && (this.lastKnownLocation != null) && (this.lastKnownLocation.getLatitude() != -1000.0D))
    {
      ((TLRPC.TL_messages_getInlineBotResults)localObject2).flags |= 0x1;
      ((TLRPC.TL_messages_getInlineBotResults)localObject2).geo_point = new TLRPC.TL_inputGeoPoint();
      ((TLRPC.TL_messages_getInlineBotResults)localObject2).geo_point.lat = this.lastKnownLocation.getLatitude();
      ((TLRPC.TL_messages_getInlineBotResults)localObject2).geo_point._long = this.lastKnownLocation.getLongitude();
    }
    int i = (int)this.dialog_id;
    int j = (int)(this.dialog_id >> 32);
    if (i != 0) {}
    for (((TLRPC.TL_messages_getInlineBotResults)localObject2).peer = MessagesController.getInstance(this.currentAccount).getInputPeer(i);; ((TLRPC.TL_messages_getInlineBotResults)localObject2).peer = new TLRPC.TL_inputPeerEmpty())
    {
      this.contextQueryReqid = ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, (RequestDelegate)localObject1, 2);
      return;
    }
  }
  
  public void addHashtagsFromMessage(CharSequence paramCharSequence)
  {
    this.searchAdapterHelper.addHashtagsFromMessage(paramCharSequence);
  }
  
  public void clearRecentHashtags()
  {
    this.searchAdapterHelper.clearRecentHashtags();
    this.searchResultHashtags.clear();
    notifyDataSetChanged();
    if (this.delegate != null) {
      this.delegate.needChangePanelVisibility(false);
    }
  }
  
  public String getBotCaption()
  {
    if (this.foundContextBot != null) {
      return this.foundContextBot.bot_inline_placeholder;
    }
    if ((this.searchingContextUsername != null) && (this.searchingContextUsername.equals("gif"))) {
      return "Search GIFs";
    }
    return null;
  }
  
  public TLRPC.TL_inlineBotSwitchPM getBotContextSwitch()
  {
    return this.searchResultBotContextSwitch;
  }
  
  public int getContextBotId()
  {
    if (this.foundContextBot != null) {
      return this.foundContextBot.id;
    }
    return 0;
  }
  
  public String getContextBotName()
  {
    if (this.foundContextBot != null) {
      return this.foundContextBot.username;
    }
    return "";
  }
  
  public TLRPC.User getContextBotUser()
  {
    return this.foundContextBot;
  }
  
  public Object getItem(int paramInt)
  {
    Object localObject2 = null;
    int i;
    Object localObject1;
    if (this.searchResultBotContext != null)
    {
      i = paramInt;
      if (this.searchResultBotContextSwitch != null) {
        if (paramInt == 0) {
          localObject1 = this.searchResultBotContextSwitch;
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
            do
            {
              do
              {
                do
                {
                  do
                  {
                    do
                    {
                      do
                      {
                        do
                        {
                          return localObject1;
                          i = paramInt - 1;
                          localObject1 = localObject2;
                        } while (i < 0);
                        localObject1 = localObject2;
                      } while (i >= this.searchResultBotContext.size());
                      return this.searchResultBotContext.get(i);
                      if (this.searchResultUsernames == null) {
                        break;
                      }
                      localObject1 = localObject2;
                    } while (paramInt < 0);
                    localObject1 = localObject2;
                  } while (paramInt >= this.searchResultUsernames.size());
                  return this.searchResultUsernames.get(paramInt);
                  if (this.searchResultHashtags == null) {
                    break;
                  }
                  localObject1 = localObject2;
                } while (paramInt < 0);
                localObject1 = localObject2;
              } while (paramInt >= this.searchResultHashtags.size());
              return this.searchResultHashtags.get(paramInt);
              if (this.searchResultSuggestions == null) {
                break;
              }
              localObject1 = localObject2;
            } while (paramInt < 0);
            localObject1 = localObject2;
          } while (paramInt >= this.searchResultSuggestions.size());
          return this.searchResultSuggestions.get(paramInt);
          localObject1 = localObject2;
        } while (this.searchResultCommands == null);
        localObject1 = localObject2;
      } while (paramInt < 0);
      localObject1 = localObject2;
    } while (paramInt >= this.searchResultCommands.size());
    if ((this.searchResultCommandsUsers != null) && ((this.botsCount != 1) || ((this.info instanceof TLRPC.TL_channelFull))))
    {
      if (this.searchResultCommandsUsers.get(paramInt) != null)
      {
        localObject2 = this.searchResultCommands.get(paramInt);
        if (this.searchResultCommandsUsers.get(paramInt) != null) {}
        for (localObject1 = ((TLRPC.User)this.searchResultCommandsUsers.get(paramInt)).username;; localObject1 = "") {
          return String.format("%s@%s", new Object[] { localObject2, localObject1 });
        }
      }
      return String.format("%s", new Object[] { this.searchResultCommands.get(paramInt) });
    }
    return this.searchResultCommands.get(paramInt);
  }
  
  public int getItemCount()
  {
    int i = 1;
    if ((this.foundContextBot != null) && (!this.inlineMediaEnabled)) {
      return 1;
    }
    if (this.searchResultBotContext != null)
    {
      int j = this.searchResultBotContext.size();
      if (this.searchResultBotContextSwitch != null) {}
      for (;;)
      {
        return i + j;
        i = 0;
      }
    }
    if (this.searchResultUsernames != null) {
      return this.searchResultUsernames.size();
    }
    if (this.searchResultHashtags != null) {
      return this.searchResultHashtags.size();
    }
    if (this.searchResultCommands != null) {
      return this.searchResultCommands.size();
    }
    if (this.searchResultSuggestions != null) {
      return this.searchResultSuggestions.size();
    }
    return 0;
  }
  
  public int getItemPosition(int paramInt)
  {
    int i = paramInt;
    if (this.searchResultBotContext != null)
    {
      i = paramInt;
      if (this.searchResultBotContextSwitch != null) {
        i = paramInt - 1;
      }
    }
    return i;
  }
  
  public int getItemViewType(int paramInt)
  {
    if ((this.foundContextBot != null) && (!this.inlineMediaEnabled)) {
      return 3;
    }
    if (this.searchResultBotContext != null)
    {
      if ((paramInt == 0) && (this.searchResultBotContextSwitch != null)) {
        return 2;
      }
      return 1;
    }
    return 0;
  }
  
  public int getResultLength()
  {
    return this.resultLength;
  }
  
  public int getResultStartPosition()
  {
    return this.resultStartPosition;
  }
  
  public ArrayList<TLRPC.BotInlineResult> getSearchResultBotContext()
  {
    return this.searchResultBotContext;
  }
  
  public boolean isBannedInline()
  {
    return (this.foundContextBot != null) && (!this.inlineMediaEnabled);
  }
  
  public boolean isBotCommands()
  {
    return this.searchResultCommands != null;
  }
  
  public boolean isBotContext()
  {
    return this.searchResultBotContext != null;
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    return (this.foundContextBot == null) || (this.inlineMediaEnabled);
  }
  
  public boolean isLongClickEnabled()
  {
    return (this.searchResultHashtags != null) || (this.searchResultCommands != null);
  }
  
  public boolean isMediaLayout()
  {
    return this.contextMedia;
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    boolean bool2 = true;
    if (paramViewHolder.getItemViewType() == 3)
    {
      paramViewHolder = (TextView)paramViewHolder.itemView;
      localObject = this.parentFragment.getCurrentChat();
      if (localObject != null)
      {
        if (!AndroidUtilities.isBannedForever(((TLRPC.Chat)localObject).banned_rights.until_date)) {
          break label61;
        }
        paramViewHolder.setText(LocaleController.getString("AttachInlineRestrictedForever", 2131493030));
      }
    }
    label61:
    label145:
    label230:
    do
    {
      return;
      paramViewHolder.setText(LocaleController.formatString("AttachInlineRestricted", 2131493029, new Object[] { LocaleController.formatDateForBan(((TLRPC.Chat)localObject).banned_rights.until_date) }));
      return;
      if (this.searchResultBotContext != null)
      {
        if (this.searchResultBotContextSwitch != null) {}
        for (int i = 1;; i = 0)
        {
          if (paramViewHolder.getItemViewType() != 2) {
            break label145;
          }
          if (i == 0) {
            break;
          }
          ((BotSwitchCell)paramViewHolder.itemView).setText(this.searchResultBotContextSwitch.text);
          return;
        }
        int j = paramInt;
        if (i != 0) {
          j = paramInt - 1;
        }
        paramViewHolder = (ContextLinkCell)paramViewHolder.itemView;
        localObject = (TLRPC.BotInlineResult)this.searchResultBotContext.get(j);
        boolean bool3 = this.contextMedia;
        boolean bool1;
        if (j != this.searchResultBotContext.size() - 1)
        {
          bool1 = true;
          if ((i == 0) || (j != 0)) {
            break label230;
          }
        }
        for (;;)
        {
          paramViewHolder.setLink((TLRPC.BotInlineResult)localObject, bool3, bool1, bool2);
          return;
          bool1 = false;
          break;
          bool2 = false;
        }
      }
      if (this.searchResultUsernames != null)
      {
        ((MentionCell)paramViewHolder.itemView).setUser((TLRPC.User)this.searchResultUsernames.get(paramInt));
        return;
      }
      if (this.searchResultHashtags != null)
      {
        ((MentionCell)paramViewHolder.itemView).setText((String)this.searchResultHashtags.get(paramInt));
        return;
      }
      if (this.searchResultSuggestions != null)
      {
        ((MentionCell)paramViewHolder.itemView).setEmojiSuggestion((EmojiSuggestion)this.searchResultSuggestions.get(paramInt));
        return;
      }
    } while (this.searchResultCommands == null);
    Object localObject = (MentionCell)paramViewHolder.itemView;
    String str1 = (String)this.searchResultCommands.get(paramInt);
    String str2 = (String)this.searchResultCommandsHelp.get(paramInt);
    if (this.searchResultCommandsUsers != null) {}
    for (paramViewHolder = (TLRPC.User)this.searchResultCommandsUsers.get(paramInt);; paramViewHolder = null)
    {
      ((MentionCell)localObject).setBotCommand(str1, str2, paramViewHolder);
      return;
    }
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    switch (paramInt)
    {
    default: 
      paramViewGroup = new TextView(this.mContext);
      paramViewGroup.setPadding(AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F), AndroidUtilities.dp(8.0F));
      paramViewGroup.setTextSize(1, 14.0F);
      paramViewGroup.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
    }
    for (;;)
    {
      return new RecyclerListView.Holder(paramViewGroup);
      paramViewGroup = new MentionCell(this.mContext);
      ((MentionCell)paramViewGroup).setIsDarkTheme(this.isDarkTheme);
      continue;
      paramViewGroup = new ContextLinkCell(this.mContext);
      ((ContextLinkCell)paramViewGroup).setDelegate(new ContextLinkCell.ContextLinkCellDelegate()
      {
        public void didPressedImage(ContextLinkCell paramAnonymousContextLinkCell)
        {
          MentionsAdapter.this.delegate.onContextClick(paramAnonymousContextLinkCell.getResult());
        }
      });
      continue;
      paramViewGroup = new BotSwitchCell(this.mContext);
    }
  }
  
  public void onDestroy()
  {
    if (this.locationProvider != null) {
      this.locationProvider.stop();
    }
    if (this.contextQueryRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.contextQueryRunnable);
      this.contextQueryRunnable = null;
    }
    if (this.contextUsernameReqid != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextUsernameReqid, true);
      this.contextUsernameReqid = 0;
    }
    if (this.contextQueryReqid != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.contextQueryReqid, true);
      this.contextQueryReqid = 0;
    }
    this.foundContextBot = null;
    this.inlineMediaEnabled = true;
    this.searchingContextUsername = null;
    this.searchingContextQuery = null;
    this.noUserName = false;
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if ((paramInt == 2) && (this.foundContextBot != null) && (this.foundContextBot.bot_inline_geo))
    {
      if ((paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0)) {
        this.locationProvider.start();
      }
    }
    else {
      return;
    }
    onLocationUnavailable();
  }
  
  public void searchForContextBotForNextOffset()
  {
    if ((this.contextQueryReqid != 0) || (this.nextQueryOffset == null) || (this.nextQueryOffset.length() == 0) || (this.foundContextBot == null) || (this.searchingContextQuery == null)) {
      return;
    }
    searchForContextBotResults(true, this.foundContextBot, this.searchingContextQuery, this.nextQueryOffset);
  }
  
  public void searchUsernameOrHashtag(final String paramString, int paramInt, ArrayList<MessageObject> paramArrayList, boolean paramBoolean)
  {
    if (this.channelReqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.channelReqId, true);
      this.channelReqId = 0;
    }
    if (this.searchGlobalRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.searchGlobalRunnable);
      this.searchGlobalRunnable = null;
    }
    if (TextUtils.isEmpty(paramString))
    {
      searchForContextBot(null, null);
      this.delegate.needChangePanelVisibility(false);
      this.lastText = null;
    }
    int i;
    final Object localObject4;
    int n;
    label190:
    label207:
    label284:
    label292:
    label428:
    label437:
    label452:
    label454:
    label462:
    label818:
    label1063:
    label1117:
    label1210:
    label1262:
    label1378:
    label1626:
    do
    {
      return;
      int j = paramInt;
      i = j;
      if (paramString.length() > 0) {
        i = j - 1;
      }
      this.lastText = null;
      this.lastUsernameOnly = paramBoolean;
      localObject4 = new StringBuilder();
      int i1 = -1;
      int k = 0;
      n = 0;
      int m;
      final Object localObject1;
      final Object localObject2;
      final Object localObject3;
      int i2;
      if ((!paramBoolean) && (this.needBotContext) && (paramString.charAt(0) == '@'))
      {
        j = paramString.indexOf(' ');
        m = paramString.length();
        localObject1 = null;
        localObject2 = null;
        if (j > 0)
        {
          localObject1 = paramString.substring(1, j);
          localObject2 = paramString.substring(j + 1);
          if ((localObject1 == null) || (((String)localObject1).length() < 1)) {
            break label437;
          }
          j = 1;
          localObject3 = localObject1;
          if (j < ((String)localObject1).length())
          {
            m = ((String)localObject1).charAt(j);
            if (((m >= 48) && (m <= 57)) || ((m >= 97) && (m <= 122)) || ((m >= 65) && (m <= 90)) || (m == 95)) {
              break label428;
            }
            localObject3 = "";
          }
          searchForContextBot((String)localObject3, (String)localObject2);
          if (this.foundContextBot != null) {
            break label452;
          }
          localObject1 = MessagesController.getInstance(this.currentAccount);
          i2 = -1;
          if (!paramBoolean) {
            break label454;
          }
          ((StringBuilder)localObject4).append(paramString.substring(1));
          this.resultStartPosition = 0;
          this.resultLength = ((StringBuilder)localObject4).length();
          i = 0;
          m = i2;
        }
      }
      for (;;)
      {
        if (i == -1)
        {
          this.delegate.needChangePanelVisibility(false);
          return;
          if ((paramString.charAt(m - 1) == 't') && (paramString.charAt(m - 2) == 'o') && (paramString.charAt(m - 3) == 'b'))
          {
            localObject1 = paramString.substring(1);
            localObject2 = "";
            break label190;
          }
          searchForContextBot(null, null);
          break label190;
          j += 1;
          break label207;
          localObject3 = "";
          break label284;
          searchForContextBot(null, null);
          break label292;
          break;
          j = k;
          k = i;
          m = i2;
          i = i1;
          n = j;
          if (k >= 0)
          {
            if (k >= paramString.length()) {
              i = j;
            }
            for (;;)
            {
              k -= 1;
              j = i;
              break label462;
              char c = paramString.charAt(k);
              if ((k == 0) || (paramString.charAt(k - 1) == ' ') || (paramString.charAt(k - 1) == '\n'))
              {
                if (c == '@')
                {
                  if ((!this.needUsernames) && ((!this.needBotContext) || (k != 0))) {
                    break label818;
                  }
                  if ((this.info == null) && (k != 0))
                  {
                    this.lastText = paramString;
                    this.lastPosition = paramInt;
                    this.messages = paramArrayList;
                    this.delegate.needChangePanelVisibility(false);
                    return;
                  }
                  m = k;
                  i = 0;
                  this.resultStartPosition = k;
                  this.resultLength = (((StringBuilder)localObject4).length() + 1);
                  n = j;
                  break;
                }
                if (c == '#')
                {
                  if (this.searchAdapterHelper.loadRecentHashtags())
                  {
                    i = 1;
                    this.resultStartPosition = k;
                    this.resultLength = (((StringBuilder)localObject4).length() + 1);
                    ((StringBuilder)localObject4).insert(0, c);
                    m = i2;
                    n = j;
                    break;
                  }
                  this.lastText = paramString;
                  this.lastPosition = paramInt;
                  this.messages = paramArrayList;
                  this.delegate.needChangePanelVisibility(false);
                  return;
                }
                if ((k == 0) && (this.botInfo != null) && (c == '/'))
                {
                  i = 2;
                  this.resultStartPosition = k;
                  this.resultLength = (((StringBuilder)localObject4).length() + 1);
                  m = i2;
                  n = j;
                  break;
                }
                if ((c == ':') && (((StringBuilder)localObject4).length() > 0))
                {
                  i = 3;
                  this.resultStartPosition = k;
                  this.resultLength = (((StringBuilder)localObject4).length() + 1);
                  m = i2;
                  n = j;
                  break;
                }
              }
              if (c >= '0')
              {
                i = j;
                if (c <= '9') {}
              }
              else if (c >= 'a')
              {
                i = j;
                if (c <= 'z') {}
              }
              else if (c >= 'A')
              {
                i = j;
                if (c <= 'Z') {}
              }
              else
              {
                i = j;
                if (c != '_') {
                  i = 1;
                }
              }
              ((StringBuilder)localObject4).insert(0, c);
            }
          }
        }
      }
      Object localObject5;
      if (i == 0)
      {
        localObject2 = new ArrayList();
        paramInt = 0;
        while (paramInt < Math.min(100, paramArrayList.size()))
        {
          i = ((MessageObject)paramArrayList.get(paramInt)).messageOwner.from_id;
          if (!((ArrayList)localObject2).contains(Integer.valueOf(i))) {
            ((ArrayList)localObject2).add(Integer.valueOf(i));
          }
          paramInt += 1;
        }
        localObject4 = ((StringBuilder)localObject4).toString().toLowerCase();
        TLRPC.User localUser;
        if (((String)localObject4).indexOf(' ') >= 0)
        {
          j = 1;
          paramArrayList = new ArrayList();
          localObject3 = new SparseArray();
          localObject5 = new SparseArray();
          paramString = DataQuery.getInstance(this.currentAccount).inlineBots;
          if ((paramBoolean) || (!this.needBotContext) || (m != 0) || (paramString.isEmpty())) {
            break label1210;
          }
          paramInt = 0;
          k = 0;
          if (k >= paramString.size()) {
            break label1210;
          }
          localUser = ((MessagesController)localObject1).getUser(Integer.valueOf(((TLRPC.TL_topPeer)paramString.get(k)).peer.user_id));
          if (localUser != null) {
            break label1117;
          }
        }
        do
        {
          k += 1;
          break label1063;
          j = 0;
          break;
          i = paramInt;
          if (localUser.username != null)
          {
            i = paramInt;
            if (localUser.username.length() > 0) {
              if ((((String)localObject4).length() <= 0) || (!localUser.username.toLowerCase().startsWith((String)localObject4)))
              {
                i = paramInt;
                if (((String)localObject4).length() != 0) {}
              }
              else
              {
                paramArrayList.add(localUser);
                ((SparseArray)localObject3).put(localUser.id, localUser);
                i = paramInt + 1;
              }
            }
          }
          paramInt = i;
        } while (i != 5);
        if (this.parentFragment != null)
        {
          paramString = this.parentFragment.getCurrentChat();
          if ((paramString == null) || (this.info == null) || (this.info.participants == null) || ((ChatObject.isChannel(paramString)) && (!paramString.megagroup))) {
            break label1626;
          }
          paramInt = 0;
          if (paramInt >= this.info.participants.participants.size()) {
            break label1626;
          }
          localUser = ((MessagesController)localObject1).getUser(Integer.valueOf(((TLRPC.ChatParticipant)this.info.participants.participants.get(paramInt)).user_id));
          if ((localUser != null) && ((paramBoolean) || (!UserObject.isUserSelf(localUser))) && (((SparseArray)localObject3).indexOfKey(localUser.id) < 0)) {
            break label1378;
          }
        }
        for (;;)
        {
          paramInt += 1;
          break label1262;
          if (this.info != null)
          {
            paramString = ((MessagesController)localObject1).getChat(Integer.valueOf(this.info.id));
            break;
          }
          paramString = null;
          break;
          if (((String)localObject4).length() == 0)
          {
            if (!localUser.deleted) {
              paramArrayList.add(localUser);
            }
          }
          else if ((localUser.username != null) && (localUser.username.length() > 0) && (localUser.username.toLowerCase().startsWith((String)localObject4)))
          {
            paramArrayList.add(localUser);
            ((SparseArray)localObject5).put(localUser.id, localUser);
          }
          else if ((localUser.first_name != null) && (localUser.first_name.length() > 0) && (localUser.first_name.toLowerCase().startsWith((String)localObject4)))
          {
            paramArrayList.add(localUser);
            ((SparseArray)localObject5).put(localUser.id, localUser);
          }
          else if ((localUser.last_name != null) && (localUser.last_name.length() > 0) && (localUser.last_name.toLowerCase().startsWith((String)localObject4)))
          {
            paramArrayList.add(localUser);
            ((SparseArray)localObject5).put(localUser.id, localUser);
          }
          else if ((j != 0) && (ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase().startsWith((String)localObject4)))
          {
            paramArrayList.add(localUser);
            ((SparseArray)localObject5).put(localUser.id, localUser);
          }
        }
        this.searchResultHashtags = null;
        this.searchResultCommands = null;
        this.searchResultCommandsHelp = null;
        this.searchResultCommandsUsers = null;
        this.searchResultSuggestions = null;
        this.searchResultUsernames = paramArrayList;
        this.searchResultUsernamesMap = ((SparseArray)localObject5);
        if ((paramString != null) && (paramString.megagroup) && (((String)localObject4).length() > 0))
        {
          paramString = new Runnable()
          {
            public void run()
            {
              if (MentionsAdapter.this.searchGlobalRunnable != this) {
                return;
              }
              TLRPC.TL_channels_getParticipants localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
              localTL_channels_getParticipants.channel = MessagesController.getInputChannel(paramString);
              localTL_channels_getParticipants.limit = 20;
              localTL_channels_getParticipants.offset = 0;
              localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsSearch();
              localTL_channels_getParticipants.filter.q = localObject4;
              final int i = MentionsAdapter.access$3104(MentionsAdapter.this);
              MentionsAdapter.access$3202(MentionsAdapter.this, ConnectionsManager.getInstance(MentionsAdapter.this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
              {
                public void run(final TLObject paramAnonymous2TLObject, final TLRPC.TL_error paramAnonymous2TL_error)
                {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      if ((MentionsAdapter.this.channelReqId != 0) && (MentionsAdapter.9.1.this.val$currentReqId == MentionsAdapter.this.channelLastReqId) && (MentionsAdapter.this.searchResultUsernamesMap != null) && (MentionsAdapter.this.searchResultUsernames != null) && (paramAnonymous2TL_error == null))
                      {
                        TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymous2TLObject;
                        MentionsAdapter.9.this.val$messagesController.putUsers(localTL_channels_channelParticipants.users, false);
                        if (!localTL_channels_channelParticipants.participants.isEmpty())
                        {
                          int j = UserConfig.getInstance(MentionsAdapter.this.currentAccount).getClientUserId();
                          int i = 0;
                          if (i < localTL_channels_channelParticipants.participants.size())
                          {
                            Object localObject = (TLRPC.ChannelParticipant)localTL_channels_channelParticipants.participants.get(i);
                            if ((MentionsAdapter.this.searchResultUsernamesMap.indexOfKey(((TLRPC.ChannelParticipant)localObject).user_id) >= 0) || ((!MentionsAdapter.this.isSearchingMentions) && (((TLRPC.ChannelParticipant)localObject).user_id == j))) {}
                            for (;;)
                            {
                              i += 1;
                              break;
                              localObject = MentionsAdapter.9.this.val$messagesController.getUser(Integer.valueOf(((TLRPC.ChannelParticipant)localObject).user_id));
                              if (localObject == null) {
                                return;
                              }
                              MentionsAdapter.this.searchResultUsernames.add(localObject);
                            }
                          }
                          MentionsAdapter.this.notifyDataSetChanged();
                        }
                      }
                      MentionsAdapter.access$3202(MentionsAdapter.this, 0);
                    }
                  });
                }
              }));
            }
          };
          this.searchGlobalRunnable = paramString;
          AndroidUtilities.runOnUIThread(paramString, 200L);
        }
        Collections.sort(this.searchResultUsernames, new Comparator()
        {
          public int compare(TLRPC.User paramAnonymousUser1, TLRPC.User paramAnonymousUser2)
          {
            int j = -1;
            int i;
            if ((localObject3.indexOfKey(paramAnonymousUser1.id) >= 0) && (localObject3.indexOfKey(paramAnonymousUser2.id) >= 0)) {
              i = 0;
            }
            int k;
            int m;
            do
            {
              do
              {
                do
                {
                  return i;
                  i = j;
                } while (localObject3.indexOfKey(paramAnonymousUser1.id) >= 0);
                if (localObject3.indexOfKey(paramAnonymousUser2.id) >= 0) {
                  return 1;
                }
                k = localObject2.indexOf(Integer.valueOf(paramAnonymousUser1.id));
                m = localObject2.indexOf(Integer.valueOf(paramAnonymousUser2.id));
                if ((k == -1) || (m == -1)) {
                  break;
                }
                i = j;
              } while (k < m);
              if (k == m) {
                return 0;
              }
              return 1;
              if (k == -1) {
                break;
              }
              i = j;
            } while (m == -1);
            if ((k == -1) && (m != -1)) {
              return 1;
            }
            return 0;
          }
        });
        notifyDataSetChanged();
        paramString = this.delegate;
        if (!paramArrayList.isEmpty()) {}
        for (paramBoolean = true;; paramBoolean = false)
        {
          paramString.needChangePanelVisibility(paramBoolean);
          return;
        }
      }
      if (i == 1)
      {
        paramString = new ArrayList();
        paramArrayList = ((StringBuilder)localObject4).toString().toLowerCase();
        localObject1 = this.searchAdapterHelper.getHashtags();
        paramInt = 0;
        while (paramInt < ((ArrayList)localObject1).size())
        {
          localObject2 = (SearchAdapterHelper.HashtagObject)((ArrayList)localObject1).get(paramInt);
          if ((localObject2 != null) && (((SearchAdapterHelper.HashtagObject)localObject2).hashtag != null) && (((SearchAdapterHelper.HashtagObject)localObject2).hashtag.startsWith(paramArrayList))) {
            paramString.add(((SearchAdapterHelper.HashtagObject)localObject2).hashtag);
          }
          paramInt += 1;
        }
        this.searchResultHashtags = paramString;
        this.searchResultUsernames = null;
        this.searchResultUsernamesMap = null;
        this.searchResultCommands = null;
        this.searchResultCommandsHelp = null;
        this.searchResultCommandsUsers = null;
        this.searchResultSuggestions = null;
        notifyDataSetChanged();
        paramArrayList = this.delegate;
        if (!paramString.isEmpty()) {}
        for (paramBoolean = true;; paramBoolean = false)
        {
          paramArrayList.needChangePanelVisibility(paramBoolean);
          return;
        }
      }
      if (i == 2)
      {
        paramString = new ArrayList();
        paramArrayList = new ArrayList();
        localObject2 = new ArrayList();
        localObject3 = ((StringBuilder)localObject4).toString().toLowerCase();
        paramInt = 0;
        while (paramInt < this.botInfo.size())
        {
          localObject4 = (TLRPC.BotInfo)this.botInfo.valueAt(paramInt);
          i = 0;
          while (i < ((TLRPC.BotInfo)localObject4).commands.size())
          {
            localObject5 = (TLRPC.TL_botCommand)((TLRPC.BotInfo)localObject4).commands.get(i);
            if ((localObject5 != null) && (((TLRPC.TL_botCommand)localObject5).command != null) && (((TLRPC.TL_botCommand)localObject5).command.startsWith((String)localObject3)))
            {
              paramString.add("/" + ((TLRPC.TL_botCommand)localObject5).command);
              paramArrayList.add(((TLRPC.TL_botCommand)localObject5).description);
              ((ArrayList)localObject2).add(((MessagesController)localObject1).getUser(Integer.valueOf(((TLRPC.BotInfo)localObject4).user_id)));
            }
            i += 1;
          }
          paramInt += 1;
        }
        this.searchResultHashtags = null;
        this.searchResultUsernames = null;
        this.searchResultUsernamesMap = null;
        this.searchResultSuggestions = null;
        this.searchResultCommands = paramString;
        this.searchResultCommandsHelp = paramArrayList;
        this.searchResultCommandsUsers = ((ArrayList)localObject2);
        notifyDataSetChanged();
        paramArrayList = this.delegate;
        if (!paramString.isEmpty()) {}
        for (paramBoolean = true;; paramBoolean = false)
        {
          paramArrayList.needChangePanelVisibility(paramBoolean);
          return;
        }
      }
    } while (i != 3);
    if (n == 0)
    {
      paramString = Emoji.getSuggestion(((StringBuilder)localObject4).toString());
      if (paramString != null)
      {
        this.searchResultSuggestions = new ArrayList();
        paramInt = 0;
        while (paramInt < paramString.length)
        {
          paramArrayList = (EmojiSuggestion)paramString[paramInt];
          paramArrayList.emoji = paramArrayList.emoji.replace("️", "");
          this.searchResultSuggestions.add(paramArrayList);
          paramInt += 1;
        }
        Emoji.loadRecentEmoji();
        Collections.sort(this.searchResultSuggestions, new Comparator()
        {
          public int compare(EmojiSuggestion paramAnonymousEmojiSuggestion1, EmojiSuggestion paramAnonymousEmojiSuggestion2)
          {
            Integer localInteger = (Integer)Emoji.emojiUseHistory.get(paramAnonymousEmojiSuggestion1.emoji);
            paramAnonymousEmojiSuggestion1 = localInteger;
            if (localInteger == null) {
              paramAnonymousEmojiSuggestion1 = Integer.valueOf(0);
            }
            localInteger = (Integer)Emoji.emojiUseHistory.get(paramAnonymousEmojiSuggestion2.emoji);
            paramAnonymousEmojiSuggestion2 = localInteger;
            if (localInteger == null) {
              paramAnonymousEmojiSuggestion2 = Integer.valueOf(0);
            }
            return paramAnonymousEmojiSuggestion2.compareTo(paramAnonymousEmojiSuggestion1);
          }
        });
      }
      this.searchResultHashtags = null;
      this.searchResultUsernames = null;
      this.searchResultUsernamesMap = null;
      this.searchResultCommands = null;
      this.searchResultCommandsHelp = null;
      this.searchResultCommandsUsers = null;
      notifyDataSetChanged();
      paramString = this.delegate;
      if (this.searchResultSuggestions != null) {}
      for (paramBoolean = true;; paramBoolean = false)
      {
        paramString.needChangePanelVisibility(paramBoolean);
        return;
      }
    }
    this.delegate.needChangePanelVisibility(false);
  }
  
  public void setBotInfo(SparseArray<TLRPC.BotInfo> paramSparseArray)
  {
    this.botInfo = paramSparseArray;
  }
  
  public void setBotsCount(int paramInt)
  {
    this.botsCount = paramInt;
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.currentAccount = UserConfig.selectedAccount;
    this.info = paramChatFull;
    if ((!this.inlineMediaEnabled) && (this.foundContextBot != null) && (this.parentFragment != null))
    {
      paramChatFull = this.parentFragment.getCurrentChat();
      if (paramChatFull != null)
      {
        this.inlineMediaEnabled = ChatObject.canSendStickers(paramChatFull);
        if (this.inlineMediaEnabled)
        {
          this.searchResultUsernames = null;
          notifyDataSetChanged();
          this.delegate.needChangePanelVisibility(false);
          processFoundUser(this.foundContextBot);
        }
      }
    }
    if (this.lastText != null) {
      searchUsernameOrHashtag(this.lastText, this.lastPosition, this.messages, this.lastUsernameOnly);
    }
  }
  
  public void setNeedBotContext(boolean paramBoolean)
  {
    this.needBotContext = paramBoolean;
  }
  
  public void setNeedUsernames(boolean paramBoolean)
  {
    this.needUsernames = paramBoolean;
  }
  
  public void setParentFragment(ChatActivity paramChatActivity)
  {
    this.parentFragment = paramChatActivity;
  }
  
  public void setSearchingMentions(boolean paramBoolean)
  {
    this.isSearchingMentions = paramBoolean;
  }
  
  public static abstract interface MentionsAdapterDelegate
  {
    public abstract void needChangePanelVisibility(boolean paramBoolean);
    
    public abstract void onContextClick(TLRPC.BotInlineResult paramBotInlineResult);
    
    public abstract void onContextSearch(boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/MentionsAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */