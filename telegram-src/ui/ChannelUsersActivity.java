package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.ChannelParticipant;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.TL_channelAdminRights;
import org.telegram.tgnet.TLRPC.TL_channelBannedRights;
import org.telegram.tgnet.TLRPC.TL_channelParticipant;
import org.telegram.tgnet.TLRPC.TL_channelParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_channelParticipantBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantCreator;
import org.telegram.tgnet.TLRPC.TL_channelParticipantSelf;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsAdmins;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsBanned;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsKicked;
import org.telegram.tgnet.TLRPC.TL_channelParticipantsRecent;
import org.telegram.tgnet.TLRPC.TL_channels_channelParticipants;
import org.telegram.tgnet.TLRPC.TL_channels_editBanned;
import org.telegram.tgnet.TLRPC.TL_channels_getParticipants;
import org.telegram.tgnet.TLRPC.TL_contact;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserStatus;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Adapters.SearchAdapterHelper;
import org.telegram.ui.Adapters.SearchAdapterHelper.HashtagObject;
import org.telegram.ui.Adapters.SearchAdapterHelper.SearchAdapterHelperDelegate;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ManageChatUserCell.ManageChatUserCellDelegate;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class ChannelUsersActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int search_button = 0;
  private int addNew2Row;
  private int addNewRow;
  private int addNewSectionRow;
  private int blockedEmptyRow;
  private int changeAddHeaderRow;
  private int changeAddRadio1Row;
  private int changeAddRadio2Row;
  private int changeAddSectionRow;
  private int chatId = this.arguments.getInt("chat_id");
  private TLRPC.Chat currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
  private EmptyTextProgressView emptyView;
  private boolean firstEndReached;
  private boolean firstLoaded;
  private RecyclerListView listView;
  private ListAdapter listViewAdapter;
  private boolean loadingUsers;
  private boolean needOpenSearch = this.arguments.getBoolean("open_search");
  private ArrayList<TLRPC.ChannelParticipant> participants = new ArrayList();
  private ArrayList<TLRPC.ChannelParticipant> participants2 = new ArrayList();
  private int participants2EndRow;
  private int participants2StartRow;
  private int participantsDividerRow;
  private int participantsEndRow;
  private int participantsInfoRow;
  private SparseArray<TLRPC.ChannelParticipant> participantsMap = new SparseArray();
  private int participantsStartRow;
  private int restricted1SectionRow;
  private int restricted2SectionRow;
  private int rowCount;
  private ActionBarMenuItem searchItem;
  private SearchAdapter searchListViewAdapter;
  private boolean searchWas;
  private boolean searching;
  private int selectType = this.arguments.getInt("selectType");
  private int type = this.arguments.getInt("type");
  
  public ChannelUsersActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private boolean createMenuForParticipant(final TLRPC.ChannelParticipant paramChannelParticipant, boolean paramBoolean)
  {
    boolean bool2 = true;
    boolean bool1;
    if ((paramChannelParticipant == null) || (this.selectType != 0)) {
      bool1 = false;
    }
    label106:
    label128:
    label273:
    label278:
    label284:
    label321:
    label381:
    do
    {
      final TLRPC.User localUser;
      int i;
      int j;
      do
      {
        do
        {
          return bool1;
          if (paramChannelParticipant.user_id == UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            return false;
          }
          if (this.type != 2) {
            break label381;
          }
          localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramChannelParticipant.user_id));
          if ((!(paramChannelParticipant instanceof TLRPC.TL_channelParticipant)) && (!(paramChannelParticipant instanceof TLRPC.TL_channelParticipantBanned))) {
            break label273;
          }
          i = 1;
          if ((((paramChannelParticipant instanceof TLRPC.TL_channelParticipantAdmin)) || ((paramChannelParticipant instanceof TLRPC.TL_channelParticipantCreator))) && (!paramChannelParticipant.can_edit)) {
            break label278;
          }
          j = 1;
          if (paramBoolean) {
            break label284;
          }
          localObject2 = new ArrayList();
          localObject1 = new ArrayList();
          if ((i == 0) || (!ChatObject.canAddAdmins(this.currentChat))) {
            break;
          }
          bool1 = bool2;
        } while (paramBoolean);
        ((ArrayList)localObject2).add(LocaleController.getString("SetAsAdmin", 2131494376));
        ((ArrayList)localObject1).add(Integer.valueOf(0));
        if ((!ChatObject.canBlockUsers(this.currentChat)) || (j == 0)) {
          break;
        }
        bool1 = bool2;
      } while (paramBoolean);
      if (this.currentChat.megagroup)
      {
        ((ArrayList)localObject2).add(LocaleController.getString("KickFromSupergroup", 2131493720));
        ((ArrayList)localObject1).add(Integer.valueOf(1));
        ((ArrayList)localObject2).add(LocaleController.getString("KickFromGroup", 2131493719));
        ((ArrayList)localObject1).add(Integer.valueOf(2));
      }
      for (;;)
      {
        if ((localObject1 != null) && (!((ArrayList)localObject1).isEmpty())) {
          break label321;
        }
        return false;
        i = 0;
        break;
        j = 0;
        break label106;
        localObject2 = null;
        localObject1 = null;
        break label128;
        ((ArrayList)localObject2).add(LocaleController.getString("ChannelRemoveUser", 2131493203));
        ((ArrayList)localObject1).add(Integer.valueOf(2));
      }
      AlertDialog.Builder localBuilder = new AlertDialog.Builder(getParentActivity());
      localBuilder.setItems((CharSequence[])((ArrayList)localObject2).toArray(new CharSequence[((ArrayList)localObject1).size()]), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, final int paramAnonymousInt)
        {
          if (((Integer)localObject1.get(paramAnonymousInt)).intValue() == 2)
          {
            MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).deleteUserFromChat(ChannelUsersActivity.this.chatId, localUser, null);
            paramAnonymousInt = 0;
            for (;;)
            {
              if (paramAnonymousInt < ChannelUsersActivity.this.participants.size())
              {
                if (((TLRPC.ChannelParticipant)ChannelUsersActivity.this.participants.get(paramAnonymousInt)).user_id == paramChannelParticipant.user_id)
                {
                  ChannelUsersActivity.this.participants.remove(paramAnonymousInt);
                  ChannelUsersActivity.this.updateRows();
                  ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                }
              }
              else {
                return;
              }
              paramAnonymousInt += 1;
            }
          }
          paramAnonymousDialogInterface = new ChannelRightsEditActivity(localUser.id, ChannelUsersActivity.this.chatId, paramChannelParticipant.admin_rights, paramChannelParticipant.banned_rights, ((Integer)localObject1.get(paramAnonymousInt)).intValue(), true);
          paramAnonymousDialogInterface.setDelegate(new ChannelRightsEditActivity.ChannelRightsEditActivityDelegate()
          {
            public void didSetRights(int paramAnonymous2Int, TLRPC.TL_channelAdminRights paramAnonymous2TL_channelAdminRights, TLRPC.TL_channelBannedRights paramAnonymous2TL_channelBannedRights)
            {
              int i;
              Object localObject;
              if (((Integer)ChannelUsersActivity.6.this.val$actions.get(paramAnonymousInt)).intValue() == 0)
              {
                i = 0;
                if (i < ChannelUsersActivity.this.participants.size())
                {
                  if (((TLRPC.ChannelParticipant)ChannelUsersActivity.this.participants.get(i)).user_id != ChannelUsersActivity.6.this.val$participant.user_id) {
                    break label186;
                  }
                  if (paramAnonymous2Int != 1) {
                    break label174;
                  }
                  localObject = new TLRPC.TL_channelParticipantAdmin();
                  label92:
                  ((TLRPC.ChannelParticipant)localObject).admin_rights = paramAnonymous2TL_channelAdminRights;
                  ((TLRPC.ChannelParticipant)localObject).banned_rights = paramAnonymous2TL_channelBannedRights;
                  ((TLRPC.ChannelParticipant)localObject).inviter_id = UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId();
                  ((TLRPC.ChannelParticipant)localObject).user_id = ChannelUsersActivity.6.this.val$participant.user_id;
                  ((TLRPC.ChannelParticipant)localObject).date = ChannelUsersActivity.6.this.val$participant.date;
                  ChannelUsersActivity.this.participants.set(i, localObject);
                }
              }
              for (;;)
              {
                return;
                label174:
                localObject = new TLRPC.TL_channelParticipant();
                break label92;
                label186:
                i += 1;
                break;
                if ((((Integer)ChannelUsersActivity.6.this.val$actions.get(paramAnonymousInt)).intValue() == 1) && (paramAnonymous2Int == 0))
                {
                  paramAnonymous2Int = 0;
                  while (paramAnonymous2Int < ChannelUsersActivity.this.participants.size())
                  {
                    if (((TLRPC.ChannelParticipant)ChannelUsersActivity.this.participants.get(paramAnonymous2Int)).user_id == ChannelUsersActivity.6.this.val$participant.user_id)
                    {
                      ChannelUsersActivity.this.participants.remove(paramAnonymous2Int);
                      ChannelUsersActivity.this.updateRows();
                      ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                      return;
                    }
                    paramAnonymous2Int += 1;
                  }
                }
              }
            }
          });
          ChannelUsersActivity.this.presentFragment(paramAnonymousDialogInterface);
        }
      });
      showDialog(localBuilder.create());
      return true;
      localObject2 = null;
      if ((this.type != 0) || (!ChatObject.canBlockUsers(this.currentChat))) {
        break;
      }
      bool1 = bool2;
    } while (paramBoolean);
    final Object localObject1 = new CharSequence[1];
    localObject1[0] = LocaleController.getString("Unban", 2131494504);
    for (;;)
    {
      if (localObject1 != null) {
        break label502;
      }
      return false;
      localObject1 = localObject2;
      if (this.type == 1)
      {
        localObject1 = localObject2;
        if (ChatObject.canAddAdmins(this.currentChat))
        {
          localObject1 = localObject2;
          if (paramChannelParticipant.can_edit)
          {
            bool1 = bool2;
            if (paramBoolean) {
              break;
            }
            localObject1 = new CharSequence[1];
            localObject1[0] = LocaleController.getString("ChannelRemoveUserAdmin", 2131493204);
          }
        }
      }
    }
    label502:
    Object localObject2 = new AlertDialog.Builder(getParentActivity());
    ((AlertDialog.Builder)localObject2).setItems((CharSequence[])localObject1, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        if (paramAnonymousInt == 0)
        {
          if (ChannelUsersActivity.this.type != 0) {
            break label137;
          }
          ChannelUsersActivity.this.participants.remove(paramChannelParticipant);
          ChannelUsersActivity.this.updateRows();
          ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
          paramAnonymousDialogInterface = new TLRPC.TL_channels_editBanned();
          paramAnonymousDialogInterface.user_id = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getInputUser(paramChannelParticipant.user_id);
          paramAnonymousDialogInterface.channel = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getInputChannel(ChannelUsersActivity.this.chatId);
          paramAnonymousDialogInterface.banned_rights = new TLRPC.TL_channelBannedRights();
          ConnectionsManager.getInstance(ChannelUsersActivity.this.currentAccount).sendRequest(paramAnonymousDialogInterface, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
            {
              if (paramAnonymous2TLObject != null)
              {
                paramAnonymous2TLObject = (TLRPC.Updates)paramAnonymous2TLObject;
                MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).processUpdates(paramAnonymous2TLObject, false);
                if (!paramAnonymous2TLObject.chats.isEmpty()) {
                  AndroidUtilities.runOnUIThread(new Runnable()
                  {
                    public void run()
                    {
                      TLRPC.Chat localChat = (TLRPC.Chat)paramAnonymous2TLObject.chats.get(0);
                      MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).loadFullChat(localChat.id, 0, true);
                    }
                  }, 1000L);
                }
              }
            }
          });
        }
        label137:
        do
        {
          return;
          if (ChannelUsersActivity.this.type == 1)
          {
            MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).setUserAdminRole(ChannelUsersActivity.this.chatId, MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramChannelParticipant.user_id)), new TLRPC.TL_channelAdminRights(), ChannelUsersActivity.this.currentChat.megagroup, ChannelUsersActivity.this);
            return;
          }
        } while (ChannelUsersActivity.this.type != 2);
        MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).deleteUserFromChat(ChannelUsersActivity.this.chatId, MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramChannelParticipant.user_id)), null);
      }
    });
    showDialog(((AlertDialog.Builder)localObject2).create());
    return true;
  }
  
  private int getChannelAdminParticipantType(TLRPC.ChannelParticipant paramChannelParticipant)
  {
    if (((paramChannelParticipant instanceof TLRPC.TL_channelParticipantCreator)) || ((paramChannelParticipant instanceof TLRPC.TL_channelParticipantSelf))) {
      return 0;
    }
    if ((paramChannelParticipant instanceof TLRPC.TL_channelParticipantAdmin)) {
      return 1;
    }
    return 2;
  }
  
  private void getChannelParticipants(int paramInt1, int paramInt2)
  {
    if (this.loadingUsers) {
      return;
    }
    this.loadingUsers = true;
    if ((this.emptyView != null) && (!this.firstLoaded)) {
      this.emptyView.showProgress();
    }
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
    TLRPC.TL_channels_getParticipants localTL_channels_getParticipants = new TLRPC.TL_channels_getParticipants();
    localTL_channels_getParticipants.channel = MessagesController.getInstance(this.currentAccount).getInputChannel(this.chatId);
    final boolean bool = this.firstEndReached;
    if (this.type == 0) {
      if (bool) {
        localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsKicked();
      }
    }
    for (;;)
    {
      localTL_channels_getParticipants.filter.q = "";
      localTL_channels_getParticipants.offset = paramInt1;
      localTL_channels_getParticipants.limit = paramInt2;
      paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_channels_getParticipants, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              int i;
              TLRPC.TL_channels_channelParticipants localTL_channels_channelParticipants;
              int j;
              if (!ChannelUsersActivity.this.firstLoaded)
              {
                i = 1;
                ChannelUsersActivity.access$3902(ChannelUsersActivity.this, false);
                ChannelUsersActivity.access$3802(ChannelUsersActivity.this, true);
                if (ChannelUsersActivity.this.emptyView != null) {
                  ChannelUsersActivity.this.emptyView.showTextView();
                }
                if (paramAnonymousTL_error != null) {
                  break label449;
                }
                localTL_channels_channelParticipants = (TLRPC.TL_channels_channelParticipants)paramAnonymousTLObject;
                MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).putUsers(localTL_channels_channelParticipants.users, false);
                int k = UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId();
                if (ChannelUsersActivity.this.selectType != 0)
                {
                  j = 0;
                  label135:
                  if (j < localTL_channels_channelParticipants.participants.size())
                  {
                    if (((TLRPC.ChannelParticipant)localTL_channels_channelParticipants.participants.get(j)).user_id != k) {
                      break label275;
                    }
                    localTL_channels_channelParticipants.participants.remove(j);
                  }
                }
                if (ChannelUsersActivity.this.type != 0) {
                  break label374;
                }
                if (!ChannelUsersActivity.9.this.val$byEndReached) {
                  break label282;
                }
                ChannelUsersActivity.access$4202(ChannelUsersActivity.this, localTL_channels_channelParticipants.participants);
              }
              for (;;)
              {
                i = 0;
                while (i < localTL_channels_channelParticipants.participants.size())
                {
                  TLRPC.ChannelParticipant localChannelParticipant = (TLRPC.ChannelParticipant)localTL_channels_channelParticipants.participants.get(i);
                  ChannelUsersActivity.this.participantsMap.put(localChannelParticipant.user_id, localChannelParticipant);
                  i += 1;
                }
                i = 0;
                break;
                label275:
                j += 1;
                break label135;
                label282:
                ChannelUsersActivity.access$4202(ChannelUsersActivity.this, new ArrayList());
                ChannelUsersActivity.this.participantsMap.clear();
                ChannelUsersActivity.access$2402(ChannelUsersActivity.this, localTL_channels_channelParticipants.participants);
                if (i != 0) {
                  ChannelUsersActivity.access$3802(ChannelUsersActivity.this, false);
                }
                ChannelUsersActivity.access$3602(ChannelUsersActivity.this, true);
                ChannelUsersActivity.this.getChannelParticipants(0, 200);
                continue;
                label374:
                ChannelUsersActivity.this.participantsMap.clear();
                ChannelUsersActivity.access$2402(ChannelUsersActivity.this, localTL_channels_channelParticipants.participants);
              }
              for (;;)
              {
                try
                {
                  if ((ChannelUsersActivity.this.type != 0) && (ChannelUsersActivity.this.type != 2)) {
                    continue;
                  }
                  Collections.sort(localTL_channels_channelParticipants.participants, new Comparator()
                  {
                    public int compare(TLRPC.ChannelParticipant paramAnonymous3ChannelParticipant1, TLRPC.ChannelParticipant paramAnonymous3ChannelParticipant2)
                    {
                      paramAnonymous3ChannelParticipant2 = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymous3ChannelParticipant2.user_id));
                      paramAnonymous3ChannelParticipant1 = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymous3ChannelParticipant1.user_id));
                      int j = 0;
                      int k = 0;
                      int i = j;
                      if (paramAnonymous3ChannelParticipant2 != null)
                      {
                        i = j;
                        if (paramAnonymous3ChannelParticipant2.status != null)
                        {
                          if (paramAnonymous3ChannelParticipant2.id != UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId()) {
                            break label212;
                          }
                          i = ConnectionsManager.getInstance(ChannelUsersActivity.this.currentAccount).getCurrentTime() + 50000;
                        }
                      }
                      j = k;
                      if (paramAnonymous3ChannelParticipant1 != null)
                      {
                        j = k;
                        if (paramAnonymous3ChannelParticipant1.status != null)
                        {
                          if (paramAnonymous3ChannelParticipant1.id != UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId()) {
                            break label223;
                          }
                          j = ConnectionsManager.getInstance(ChannelUsersActivity.this.currentAccount).getCurrentTime() + 50000;
                        }
                      }
                      label195:
                      if ((i > 0) && (j > 0)) {
                        if (i <= j) {}
                      }
                      label212:
                      label223:
                      label270:
                      do
                      {
                        do
                        {
                          return 1;
                          i = paramAnonymous3ChannelParticipant2.status.expires;
                          break;
                          j = paramAnonymous3ChannelParticipant1.status.expires;
                          break label195;
                          if (i < j) {
                            return -1;
                          }
                          return 0;
                          if ((i >= 0) || (j >= 0)) {
                            break label270;
                          }
                        } while (i > j);
                        if (i < j) {
                          return -1;
                        }
                        return 0;
                        if (((i < 0) && (j > 0)) || ((i == 0) && (j != 0))) {
                          return -1;
                        }
                      } while (((j < 0) && (i > 0)) || ((j == 0) && (i != 0)));
                      return 0;
                    }
                  });
                }
                catch (Exception localException)
                {
                  label449:
                  FileLog.e(localException);
                  continue;
                }
                ChannelUsersActivity.this.updateRows();
                if (ChannelUsersActivity.this.listViewAdapter != null) {
                  ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
                }
                return;
                if (ChannelUsersActivity.this.type == 1) {
                  Collections.sort(localTL_channels_channelParticipants.participants, new Comparator()
                  {
                    public int compare(TLRPC.ChannelParticipant paramAnonymous3ChannelParticipant1, TLRPC.ChannelParticipant paramAnonymous3ChannelParticipant2)
                    {
                      int i = ChannelUsersActivity.this.getChannelAdminParticipantType(paramAnonymous3ChannelParticipant1);
                      int j = ChannelUsersActivity.this.getChannelAdminParticipantType(paramAnonymous3ChannelParticipant2);
                      if (i > j) {
                        return 1;
                      }
                      if (i < j) {
                        return -1;
                      }
                      return 0;
                    }
                  });
                }
              }
            }
          });
        }
      });
      ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, this.classGuid);
      return;
      localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsBanned();
      continue;
      if (this.type == 1) {
        localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsAdmins();
      } else if (this.type == 2) {
        localTL_channels_getParticipants.filter = new TLRPC.TL_channelParticipantsRecent();
      }
    }
  }
  
  private void updateRows()
  {
    this.currentChat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chatId));
    if (this.currentChat == null) {}
    for (;;)
    {
      return;
      this.changeAddHeaderRow = -1;
      this.changeAddRadio1Row = -1;
      this.changeAddRadio2Row = -1;
      this.changeAddSectionRow = -1;
      this.addNewRow = -1;
      this.addNew2Row = -1;
      this.addNewSectionRow = -1;
      this.restricted1SectionRow = -1;
      this.participantsStartRow = -1;
      this.participantsDividerRow = -1;
      this.participantsEndRow = -1;
      this.restricted2SectionRow = -1;
      this.participants2StartRow = -1;
      this.participants2EndRow = -1;
      this.participantsInfoRow = -1;
      this.blockedEmptyRow = -1;
      this.rowCount = 0;
      int i;
      if (this.type == 0)
      {
        if (ChatObject.canBlockUsers(this.currentChat))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.addNewRow = i;
          if ((!this.participants.isEmpty()) || (!this.participants2.isEmpty()))
          {
            i = this.rowCount;
            this.rowCount = (i + 1);
          }
        }
        for (this.addNewSectionRow = i;; this.addNewSectionRow = -1)
        {
          if (!this.participants.isEmpty())
          {
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.restricted1SectionRow = i;
            this.participantsStartRow = this.rowCount;
            this.rowCount += this.participants.size();
            this.participantsEndRow = this.rowCount;
          }
          if (!this.participants2.isEmpty())
          {
            if (this.restricted1SectionRow != -1)
            {
              i = this.rowCount;
              this.rowCount = (i + 1);
              this.participantsDividerRow = i;
            }
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.restricted2SectionRow = i;
            this.participants2StartRow = this.rowCount;
            this.rowCount += this.participants2.size();
            this.participants2EndRow = this.rowCount;
          }
          if ((this.participantsStartRow == -1) && (this.participants2StartRow == -1)) {
            break;
          }
          if (this.searchItem != null) {
            this.searchItem.setVisibility(0);
          }
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.participantsInfoRow = i;
          return;
          this.addNewRow = -1;
        }
        if (this.searchItem != null) {
          this.searchItem.setVisibility(4);
        }
        i = this.rowCount;
        this.rowCount = (i + 1);
        this.blockedEmptyRow = i;
        return;
      }
      if (this.type == 1)
      {
        if ((this.currentChat.creator) && (this.currentChat.megagroup))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.changeAddHeaderRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.changeAddRadio1Row = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.changeAddRadio2Row = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.changeAddSectionRow = i;
        }
        if (ChatObject.canAddAdmins(this.currentChat))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.addNewRow = i;
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.addNewSectionRow = i;
          if (this.participants.isEmpty()) {
            break label636;
          }
          this.participantsStartRow = this.rowCount;
          this.rowCount += this.participants.size();
        }
        for (this.participantsEndRow = this.rowCount;; this.participantsEndRow = -1)
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.participantsInfoRow = i;
          return;
          this.addNewRow = -1;
          this.addNewSectionRow = -1;
          break;
          label636:
          this.participantsStartRow = -1;
        }
      }
      if (this.type == 2)
      {
        if ((this.selectType == 0) && (!this.currentChat.megagroup) && (ChatObject.canAddUsers(this.currentChat)))
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.addNewRow = i;
          if (((this.currentChat.flags & 0x40) == 0) && (ChatObject.canAddViaLink(this.currentChat)))
          {
            i = this.rowCount;
            this.rowCount = (i + 1);
            this.addNew2Row = i;
          }
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.addNewSectionRow = i;
        }
        if (!this.participants.isEmpty())
        {
          this.participantsStartRow = this.rowCount;
          this.rowCount += this.participants.size();
        }
        for (this.participantsEndRow = this.rowCount; this.rowCount != 0; this.participantsEndRow = -1)
        {
          i = this.rowCount;
          this.rowCount = (i + 1);
          this.participantsInfoRow = i;
          return;
          this.participantsStartRow = -1;
        }
      }
    }
  }
  
  public View createView(Context paramContext)
  {
    int i = 1;
    this.searching = false;
    this.searchWas = false;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    if (this.type == 0)
    {
      this.actionBar.setTitle(LocaleController.getString("ChannelBlacklist", 2131493154));
      this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
      {
        public void onItemClick(int paramAnonymousInt)
        {
          if (paramAnonymousInt == -1) {
            ChannelUsersActivity.this.finishFragment();
          }
        }
      });
      if ((this.selectType != 0) || (this.type == 2) || (this.type == 0))
      {
        this.searchListViewAdapter = new SearchAdapter(paramContext);
        this.searchItem = this.actionBar.createMenu().addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
        {
          public void onSearchCollapse()
          {
            ChannelUsersActivity.this.searchListViewAdapter.searchDialogs(null);
            ChannelUsersActivity.access$002(ChannelUsersActivity.this, false);
            ChannelUsersActivity.access$302(ChannelUsersActivity.this, false);
            ChannelUsersActivity.this.listView.setAdapter(ChannelUsersActivity.this.listViewAdapter);
            ChannelUsersActivity.this.listViewAdapter.notifyDataSetChanged();
            ChannelUsersActivity.this.listView.setFastScrollVisible(true);
            ChannelUsersActivity.this.listView.setVerticalScrollBarEnabled(false);
            ChannelUsersActivity.this.emptyView.setShowAtCenter(false);
          }
          
          public void onSearchExpand()
          {
            ChannelUsersActivity.access$002(ChannelUsersActivity.this, true);
            ChannelUsersActivity.this.emptyView.setShowAtCenter(true);
          }
          
          public void onTextChanged(EditText paramAnonymousEditText)
          {
            if (ChannelUsersActivity.this.searchListViewAdapter == null) {
              return;
            }
            paramAnonymousEditText = paramAnonymousEditText.getText().toString();
            if (paramAnonymousEditText.length() != 0)
            {
              ChannelUsersActivity.access$302(ChannelUsersActivity.this, true);
              if (ChannelUsersActivity.this.listView != null)
              {
                ChannelUsersActivity.this.listView.setAdapter(ChannelUsersActivity.this.searchListViewAdapter);
                ChannelUsersActivity.this.searchListViewAdapter.notifyDataSetChanged();
                ChannelUsersActivity.this.listView.setFastScrollVisible(false);
                ChannelUsersActivity.this.listView.setVerticalScrollBarEnabled(true);
              }
            }
            ChannelUsersActivity.this.searchListViewAdapter.searchDialogs(paramAnonymousEditText);
          }
        });
        this.searchItem.getSearchField().setHint(LocaleController.getString("Search", 2131494298));
      }
      this.fragmentView = new FrameLayout(paramContext);
      this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
      FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
      this.emptyView = new EmptyTextProgressView(paramContext);
      if ((this.type == 0) || (this.type == 2)) {
        this.emptyView.setText(LocaleController.getString("NoResult", 2131493906));
      }
      localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView = new RecyclerListView(paramContext);
      this.listView.setEmptyView(this.emptyView);
      this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
      RecyclerListView localRecyclerListView = this.listView;
      paramContext = new ListAdapter(paramContext);
      this.listViewAdapter = paramContext;
      localRecyclerListView.setAdapter(paramContext);
      paramContext = this.listView;
      if (!LocaleController.isRTL) {
        break label573;
      }
      label324:
      paramContext.setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (paramAnonymousInt == ChannelUsersActivity.this.addNewRow) {
            if (ChannelUsersActivity.this.type == 0)
            {
              paramAnonymousView = new Bundle();
              paramAnonymousView.putInt("chat_id", ChannelUsersActivity.this.chatId);
              paramAnonymousView.putInt("type", 2);
              paramAnonymousView.putInt("selectType", 2);
              ChannelUsersActivity.this.presentFragment(new ChannelUsersActivity(paramAnonymousView));
            }
          }
          do
          {
            do
            {
              return;
              if (ChannelUsersActivity.this.type == 1)
              {
                paramAnonymousView = new Bundle();
                paramAnonymousView.putInt("chat_id", ChannelUsersActivity.this.chatId);
                paramAnonymousView.putInt("type", 2);
                paramAnonymousView.putInt("selectType", 1);
                ChannelUsersActivity.this.presentFragment(new ChannelUsersActivity(paramAnonymousView));
                return;
              }
            } while (ChannelUsersActivity.this.type != 2);
            paramAnonymousView = new Bundle();
            paramAnonymousView.putBoolean("onlyUsers", true);
            paramAnonymousView.putBoolean("destroyAfterSelect", true);
            paramAnonymousView.putBoolean("returnAsResult", true);
            paramAnonymousView.putBoolean("needForwardCount", false);
            paramAnonymousView.putString("selectAlertString", LocaleController.getString("ChannelAddTo", 2131493146));
            paramAnonymousView = new ContactsActivity(paramAnonymousView);
            paramAnonymousView.setDelegate(new ContactsActivity.ContactsActivityDelegate()
            {
              public void didSelectContact(TLRPC.User paramAnonymous2User, String paramAnonymous2String, ContactsActivity paramAnonymous2ContactsActivity)
              {
                paramAnonymous2ContactsActivity = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount);
                int j = ChannelUsersActivity.this.chatId;
                if (paramAnonymous2String != null) {}
                for (int i = Utilities.parseInt(paramAnonymous2String).intValue();; i = 0)
                {
                  paramAnonymous2ContactsActivity.addUserToChat(j, paramAnonymous2User, null, i, null, ChannelUsersActivity.this);
                  return;
                }
              }
            });
            ChannelUsersActivity.this.presentFragment(paramAnonymousView);
            return;
            if (paramAnonymousInt == ChannelUsersActivity.this.addNew2Row)
            {
              ChannelUsersActivity.this.presentFragment(new GroupInviteActivity(ChannelUsersActivity.this.chatId));
              return;
            }
            if ((paramAnonymousInt != ChannelUsersActivity.this.changeAddRadio1Row) && (paramAnonymousInt != ChannelUsersActivity.this.changeAddRadio2Row)) {
              break;
            }
            paramAnonymousView = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getChat(Integer.valueOf(ChannelUsersActivity.this.chatId));
          } while (paramAnonymousView == null);
          int j = 0;
          if ((paramAnonymousInt == 1) && (!paramAnonymousView.democracy))
          {
            paramAnonymousView.democracy = true;
            i = 1;
            label333:
            if (i == 0) {
              break label489;
            }
            MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).toogleChannelInvites(ChannelUsersActivity.this.chatId, paramAnonymousView.democracy);
            i = ChannelUsersActivity.this.listView.getChildCount();
            paramAnonymousInt = 0;
            label374:
            if (paramAnonymousInt < i)
            {
              localObject1 = ChannelUsersActivity.this.listView.getChildAt(paramAnonymousInt);
              if ((localObject1 instanceof RadioCell))
              {
                j = ((Integer)((View)localObject1).getTag()).intValue();
                localObject1 = (RadioCell)localObject1;
                if (((j != 0) || (!paramAnonymousView.democracy)) && ((j != 1) || (paramAnonymousView.democracy))) {
                  break label491;
                }
              }
            }
          }
          label489:
          label491:
          for (boolean bool1 = true;; bool1 = false)
          {
            ((RadioCell)localObject1).setChecked(bool1, true);
            paramAnonymousInt += 1;
            break label374;
            break;
            i = j;
            if (paramAnonymousInt != 2) {
              break label333;
            }
            i = j;
            if (!paramAnonymousView.democracy) {
              break label333;
            }
            paramAnonymousView.democracy = false;
            i = 1;
            break label333;
            break;
          }
          Object localObject1 = null;
          paramAnonymousView = null;
          j = 0;
          int i = 0;
          bool1 = false;
          final Object localObject2;
          if (ChannelUsersActivity.this.listView.getAdapter() == ChannelUsersActivity.this.listViewAdapter)
          {
            localObject3 = ChannelUsersActivity.this.listViewAdapter.getItem(paramAnonymousInt);
            paramAnonymousInt = i;
            localObject2 = localObject3;
            if (localObject3 != null)
            {
              i = ((TLRPC.ChannelParticipant)localObject3).user_id;
              TLRPC.TL_channelBannedRights localTL_channelBannedRights = ((TLRPC.ChannelParticipant)localObject3).banned_rights;
              paramAnonymousView = ((TLRPC.ChannelParticipant)localObject3).admin_rights;
              if ((((localObject3 instanceof TLRPC.TL_channelParticipantAdmin)) || ((localObject3 instanceof TLRPC.TL_channelParticipantCreator))) && (!((TLRPC.ChannelParticipant)localObject3).can_edit)) {
                break label790;
              }
              bool2 = true;
              label600:
              paramAnonymousInt = i;
              localObject1 = localTL_channelBannedRights;
              bool1 = bool2;
              localObject2 = localObject3;
              if ((localObject3 instanceof TLRPC.TL_channelParticipantCreator))
              {
                paramAnonymousView = new TLRPC.TL_channelAdminRights();
                paramAnonymousView.add_admins = true;
                paramAnonymousView.pin_messages = true;
                paramAnonymousView.invite_link = true;
                paramAnonymousView.invite_users = true;
                paramAnonymousView.ban_users = true;
                paramAnonymousView.delete_messages = true;
                paramAnonymousView.edit_messages = true;
                paramAnonymousView.post_messages = true;
                paramAnonymousView.change_info = true;
                localObject2 = localObject3;
                bool1 = bool2;
                localObject1 = localTL_channelBannedRights;
                paramAnonymousInt = i;
              }
            }
            label689:
            if (paramAnonymousInt == 0) {
              break label961;
            }
            if (ChannelUsersActivity.this.selectType == 0) {
              break label1016;
            }
            if ((!ChannelUsersActivity.this.currentChat.megagroup) && (ChannelUsersActivity.this.selectType != 1)) {
              break label968;
            }
            j = ChannelUsersActivity.this.chatId;
            if (ChannelUsersActivity.this.selectType != 1) {
              break label963;
            }
          }
          label790:
          label867:
          label951:
          label957:
          label961:
          label963:
          for (i = 0;; i = 1)
          {
            paramAnonymousView = new ChannelRightsEditActivity(paramAnonymousInt, j, paramAnonymousView, (TLRPC.TL_channelBannedRights)localObject1, i, bool1);
            paramAnonymousView.setDelegate(new ChannelRightsEditActivity.ChannelRightsEditActivityDelegate()
            {
              public void didSetRights(int paramAnonymous2Int, TLRPC.TL_channelAdminRights paramAnonymous2TL_channelAdminRights, TLRPC.TL_channelBannedRights paramAnonymous2TL_channelBannedRights)
              {
                if (localObject2 != null)
                {
                  localObject2.admin_rights = paramAnonymous2TL_channelAdminRights;
                  localObject2.banned_rights = paramAnonymous2TL_channelBannedRights;
                  TLRPC.ChannelParticipant localChannelParticipant = (TLRPC.ChannelParticipant)ChannelUsersActivity.this.participantsMap.get(localObject2.user_id);
                  if (localChannelParticipant != null)
                  {
                    localChannelParticipant.admin_rights = paramAnonymous2TL_channelAdminRights;
                    localChannelParticipant.banned_rights = paramAnonymous2TL_channelBannedRights;
                  }
                }
                ChannelUsersActivity.this.removeSelfFromStack();
              }
            });
            ChannelUsersActivity.this.presentFragment(paramAnonymousView);
            return;
            bool2 = false;
            break label600;
            localObject2 = ChannelUsersActivity.this.searchListViewAdapter.getItem(paramAnonymousInt);
            if ((localObject2 instanceof TLRPC.User))
            {
              localObject2 = (TLRPC.User)localObject2;
              MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).putUser((TLRPC.User)localObject2, false);
              localObject3 = ChannelUsersActivity.this.participantsMap;
              paramAnonymousInt = ((TLRPC.User)localObject2).id;
              localObject2 = (TLRPC.ChannelParticipant)((SparseArray)localObject3).get(paramAnonymousInt);
              if (localObject2 == null) {
                break label957;
              }
              paramAnonymousInt = ((TLRPC.ChannelParticipant)localObject2).user_id;
              if ((((localObject2 instanceof TLRPC.TL_channelParticipantAdmin)) || ((localObject2 instanceof TLRPC.TL_channelParticipantCreator))) && (!((TLRPC.ChannelParticipant)localObject2).can_edit)) {
                break label951;
              }
            }
            for (bool1 = true;; bool1 = false)
            {
              localObject1 = ((TLRPC.ChannelParticipant)localObject2).banned_rights;
              paramAnonymousView = ((TLRPC.ChannelParticipant)localObject2).admin_rights;
              break;
              if ((localObject2 instanceof TLRPC.ChannelParticipant))
              {
                localObject2 = (TLRPC.ChannelParticipant)localObject2;
                paramAnonymousInt = j;
                break label867;
              }
              localObject2 = null;
              paramAnonymousInt = j;
              break label867;
            }
            bool1 = true;
            break label689;
            break;
          }
          label968:
          paramAnonymousView = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousInt));
          MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).deleteUserFromChat(ChannelUsersActivity.this.chatId, paramAnonymousView, null);
          ChannelUsersActivity.this.finishFragment();
          return;
          label1016:
          boolean bool2 = false;
          if (ChannelUsersActivity.this.type == 1) {
            if ((paramAnonymousInt != UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId()) && ((ChannelUsersActivity.this.currentChat.creator) || (bool1))) {
              bool1 = true;
            }
          }
          while (((ChannelUsersActivity.this.type != 1) && (!ChannelUsersActivity.this.currentChat.megagroup)) || ((ChannelUsersActivity.this.type == 2) && (ChannelUsersActivity.this.selectType == 0)))
          {
            paramAnonymousView = new Bundle();
            paramAnonymousView.putInt("user_id", paramAnonymousInt);
            ChannelUsersActivity.this.presentFragment(new ProfileActivity(paramAnonymousView));
            return;
            bool1 = false;
            continue;
            bool1 = bool2;
            if (ChannelUsersActivity.this.type == 0) {
              bool1 = ChatObject.canBlockUsers(ChannelUsersActivity.this.currentChat);
            }
          }
          Object localObject3 = localObject1;
          if (localObject1 == null)
          {
            localObject3 = new TLRPC.TL_channelBannedRights();
            ((TLRPC.TL_channelBannedRights)localObject3).view_messages = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_stickers = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_media = true;
            ((TLRPC.TL_channelBannedRights)localObject3).embed_links = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_messages = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_games = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_inline = true;
            ((TLRPC.TL_channelBannedRights)localObject3).send_gifs = true;
          }
          j = ChannelUsersActivity.this.chatId;
          if (ChannelUsersActivity.this.type == 1) {}
          for (i = 0;; i = 1)
          {
            paramAnonymousView = new ChannelRightsEditActivity(paramAnonymousInt, j, paramAnonymousView, (TLRPC.TL_channelBannedRights)localObject3, i, bool1);
            paramAnonymousView.setDelegate(new ChannelRightsEditActivity.ChannelRightsEditActivityDelegate()
            {
              public void didSetRights(int paramAnonymous2Int, TLRPC.TL_channelAdminRights paramAnonymous2TL_channelAdminRights, TLRPC.TL_channelBannedRights paramAnonymous2TL_channelBannedRights)
              {
                if (localObject2 != null)
                {
                  localObject2.admin_rights = paramAnonymous2TL_channelAdminRights;
                  localObject2.banned_rights = paramAnonymous2TL_channelBannedRights;
                  TLRPC.ChannelParticipant localChannelParticipant = (TLRPC.ChannelParticipant)ChannelUsersActivity.this.participantsMap.get(localObject2.user_id);
                  if (localChannelParticipant != null)
                  {
                    localChannelParticipant.admin_rights = paramAnonymous2TL_channelAdminRights;
                    localChannelParticipant.banned_rights = paramAnonymous2TL_channelBannedRights;
                  }
                }
              }
            });
            ChannelUsersActivity.this.presentFragment(paramAnonymousView);
            return;
          }
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool2 = false;
          boolean bool1 = bool2;
          if (ChannelUsersActivity.this.getParentActivity() != null)
          {
            bool1 = bool2;
            if (ChannelUsersActivity.this.listView.getAdapter() == ChannelUsersActivity.this.listViewAdapter)
            {
              bool1 = bool2;
              if (ChannelUsersActivity.this.createMenuForParticipant(ChannelUsersActivity.this.listViewAdapter.getItem(paramAnonymousInt), false)) {
                bool1 = true;
              }
            }
          }
          return bool1;
        }
      });
      if (this.searchItem != null) {
        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
        {
          public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
          {
            if ((paramAnonymousInt == 1) && (ChannelUsersActivity.this.searching) && (ChannelUsersActivity.this.searchWas)) {
              AndroidUtilities.hideKeyboard(ChannelUsersActivity.this.getParentActivity().getCurrentFocus());
            }
          }
          
          public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2) {}
        });
      }
      if (!this.loadingUsers) {
        break label578;
      }
      this.emptyView.showProgress();
    }
    for (;;)
    {
      updateRows();
      return this.fragmentView;
      if (this.type == 1)
      {
        this.actionBar.setTitle(LocaleController.getString("ChannelAdministrators", 2131493149));
        break;
      }
      if (this.type != 2) {
        break;
      }
      if (this.selectType == 0)
      {
        if ((ChatObject.isChannel(this.currentChat)) && (!this.currentChat.megagroup))
        {
          this.actionBar.setTitle(LocaleController.getString("ChannelSubscribers", 2131493210));
          break;
        }
        this.actionBar.setTitle(LocaleController.getString("ChannelMembers", 2131493177));
        break;
      }
      if (this.selectType == 1)
      {
        this.actionBar.setTitle(LocaleController.getString("ChannelAddAdmin", 2131493144));
        break;
      }
      if (this.selectType != 2) {
        break;
      }
      this.actionBar.setTitle(LocaleController.getString("ChannelBlockUser", 2131493155));
      break;
      label573:
      i = 2;
      break label324;
      label578:
      this.emptyView.showTextView();
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.chatInfoDidLoaded)
    {
      TLRPC.ChatFull localChatFull = (TLRPC.ChatFull)paramVarArgs[0];
      boolean bool = ((Boolean)paramVarArgs[2]).booleanValue();
      if ((localChatFull.id == this.chatId) && (!bool)) {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            ChannelUsersActivity.access$3602(ChannelUsersActivity.this, false);
            ChannelUsersActivity.this.getChannelParticipants(0, 200);
          }
        });
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local10 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (ChannelUsersActivity.this.listView != null)
        {
          int j = ChannelUsersActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = ChannelUsersActivity.this.listView.getChildAt(i);
            if ((localView instanceof ManageChatUserCell)) {
              ((ManageChatUserCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { ManageChatUserCell.class, TextSettingsCell.class, ManageChatTextCell.class, RadioCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    Object localObject1 = this.listView;
    Object localObject2 = Theme.dividerPaint;
    localObject1 = new ThemeDescription((View)localObject1, 0, new Class[] { View.class }, (Paint)localObject2, null, null, "divider");
    localObject2 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueImageView" }, null, null, null, "windowBackgroundWhiteGrayIcon");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.listView, 0, new Class[] { RadioCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackground");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { RadioCell.class }, new String[] { "radioButton" }, null, null, null, "radioBackgroundChecked");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "statusColor" }, null, null, local10, "windowBackgroundWhiteGrayText");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.listView, 0, new Class[] { ManageChatUserCell.class }, new String[] { "statusOnlineColor" }, null, null, local10, "windowBackgroundWhiteBlueText");
    RecyclerListView localRecyclerListView = this.listView;
    Drawable localDrawable1 = Theme.avatar_photoDrawable;
    Drawable localDrawable2 = Theme.avatar_broadcastDrawable;
    Drawable localDrawable3 = Theme.avatar_savedDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localObject1, localObject2, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, new ThemeDescription(localRecyclerListView, 0, new Class[] { ManageChatUserCell.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "avatar_text"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundRed"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundOrange"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundViolet"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundGreen"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundCyan"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundBlue"), new ThemeDescription(null, 0, null, null, null, local10, "avatar_backgroundPink"), new ThemeDescription(this.listView, 0, new Class[] { ManageChatTextCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { ManageChatTextCell.class }, new String[] { "imageView" }, null, null, null, "windowBackgroundWhiteGrayIcon") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.chatInfoDidLoaded);
    getChannelParticipants(0, 200);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.chatInfoDidLoaded);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
  }
  
  protected void onTransitionAnimationEnd(boolean paramBoolean1, boolean paramBoolean2)
  {
    if ((paramBoolean1) && (!paramBoolean2) && (this.needOpenSearch)) {
      this.searchItem.openSearch(true);
    }
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public TLRPC.ChannelParticipant getItem(int paramInt)
    {
      if ((ChannelUsersActivity.this.participantsStartRow != -1) && (paramInt >= ChannelUsersActivity.this.participantsStartRow) && (paramInt < ChannelUsersActivity.this.participantsEndRow)) {
        return (TLRPC.ChannelParticipant)ChannelUsersActivity.this.participants.get(paramInt - ChannelUsersActivity.this.participantsStartRow);
      }
      if ((ChannelUsersActivity.this.participants2StartRow != -1) && (paramInt >= ChannelUsersActivity.this.participants2StartRow) && (paramInt < ChannelUsersActivity.this.participants2EndRow)) {
        return (TLRPC.ChannelParticipant)ChannelUsersActivity.this.participants2.get(paramInt - ChannelUsersActivity.this.participants2StartRow);
      }
      return null;
    }
    
    public int getItemCount()
    {
      if ((ChannelUsersActivity.this.loadingUsers) && (!ChannelUsersActivity.this.firstLoaded)) {
        return 0;
      }
      return ChannelUsersActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      int j = 0;
      int i;
      if ((paramInt == ChannelUsersActivity.this.addNewRow) || (paramInt == ChannelUsersActivity.this.addNew2Row)) {
        i = 2;
      }
      do
      {
        do
        {
          do
          {
            return i;
            if (paramInt < ChannelUsersActivity.this.participantsStartRow) {
              break;
            }
            i = j;
          } while (paramInt < ChannelUsersActivity.this.participantsEndRow);
          if (paramInt < ChannelUsersActivity.this.participants2StartRow) {
            break;
          }
          i = j;
        } while (paramInt < ChannelUsersActivity.this.participants2EndRow);
        if ((paramInt == ChannelUsersActivity.this.addNewSectionRow) || (paramInt == ChannelUsersActivity.this.changeAddSectionRow) || (paramInt == ChannelUsersActivity.this.participantsDividerRow)) {
          return 3;
        }
        if (paramInt == ChannelUsersActivity.this.participantsInfoRow) {
          return 1;
        }
        if ((paramInt == ChannelUsersActivity.this.changeAddHeaderRow) || (paramInt == ChannelUsersActivity.this.restricted1SectionRow) || (paramInt == ChannelUsersActivity.this.restricted2SectionRow)) {
          return 5;
        }
        if ((paramInt == ChannelUsersActivity.this.changeAddRadio1Row) || (paramInt == ChannelUsersActivity.this.changeAddRadio2Row)) {
          return 6;
        }
        i = j;
      } while (paramInt != ChannelUsersActivity.this.blockedEmptyRow);
      return 4;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getItemViewType();
      return (i == 0) || (i == 2) || (i == 6);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      TLRPC.Chat localChat;
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
                  TLRPC.User localUser;
                  do
                  {
                    Object localObject2;
                    do
                    {
                      return;
                      localObject1 = (ManageChatUserCell)paramViewHolder.itemView;
                      ((ManageChatUserCell)localObject1).setTag(Integer.valueOf(paramInt));
                      localObject2 = getItem(paramInt);
                      localUser = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChannelParticipant)localObject2).user_id));
                    } while (localUser == null);
                    if (ChannelUsersActivity.this.type == 0)
                    {
                      localChat = null;
                      paramViewHolder = localChat;
                      if ((localObject2 instanceof TLRPC.TL_channelParticipantBanned))
                      {
                        localObject2 = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChannelParticipant)localObject2).kicked_by));
                        paramViewHolder = localChat;
                        if (localObject2 != null) {
                          paramViewHolder = LocaleController.formatString("UserRestrictionsBy", 2131494547, new Object[] { ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name) });
                        }
                      }
                      ((ManageChatUserCell)localObject1).setData(localUser, null, paramViewHolder);
                      return;
                    }
                    if (ChannelUsersActivity.this.type == 1)
                    {
                      localChat = null;
                      if (((localObject2 instanceof TLRPC.TL_channelParticipantCreator)) || ((localObject2 instanceof TLRPC.TL_channelParticipantSelf))) {
                        paramViewHolder = LocaleController.getString("ChannelCreator", 2131493162);
                      }
                      for (;;)
                      {
                        ((ManageChatUserCell)localObject1).setData(localUser, null, paramViewHolder);
                        return;
                        paramViewHolder = localChat;
                        if ((localObject2 instanceof TLRPC.TL_channelParticipantAdmin))
                        {
                          localObject2 = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChannelParticipant)localObject2).promoted_by));
                          paramViewHolder = localChat;
                          if (localObject2 != null) {
                            paramViewHolder = LocaleController.formatString("EditAdminPromotedBy", 2131493411, new Object[] { ContactsController.formatName(((TLRPC.User)localObject2).first_name, ((TLRPC.User)localObject2).last_name) });
                          }
                        }
                      }
                    }
                  } while (ChannelUsersActivity.this.type != 2);
                  ((ManageChatUserCell)localObject1).setData(localUser, null, null);
                  return;
                  paramViewHolder = (TextInfoPrivacyCell)paramViewHolder.itemView;
                } while (paramInt != ChannelUsersActivity.this.participantsInfoRow);
                if (ChannelUsersActivity.this.type == 0)
                {
                  if (ChatObject.canBlockUsers(ChannelUsersActivity.this.currentChat)) {
                    if (ChannelUsersActivity.this.currentChat.megagroup) {
                      paramViewHolder.setText(String.format("%1$s\n\n%2$s", new Object[] { LocaleController.getString("NoBlockedGroup", 2131493882), LocaleController.getString("UnbanText", 2131494505) }));
                    }
                  }
                  for (;;)
                  {
                    paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
                    return;
                    paramViewHolder.setText(String.format("%1$s\n\n%2$s", new Object[] { LocaleController.getString("NoBlockedChannel", 2131493881), LocaleController.getString("UnbanText", 2131494505) }));
                    continue;
                    if (ChannelUsersActivity.this.currentChat.megagroup) {
                      paramViewHolder.setText(LocaleController.getString("NoBlockedGroup", 2131493882));
                    } else {
                      paramViewHolder.setText(LocaleController.getString("NoBlockedChannel", 2131493881));
                    }
                  }
                }
                if (ChannelUsersActivity.this.type == 1)
                {
                  if (ChannelUsersActivity.this.addNewRow != -1)
                  {
                    if (ChannelUsersActivity.this.currentChat.megagroup) {
                      paramViewHolder.setText(LocaleController.getString("MegaAdminsInfo", 2131493799));
                    }
                    for (;;)
                    {
                      paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
                      return;
                      paramViewHolder.setText(LocaleController.getString("ChannelAdminsInfo", 2131493150));
                    }
                  }
                  paramViewHolder.setText("");
                  paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
                  return;
                }
              } while (ChannelUsersActivity.this.type != 2);
              if ((ChannelUsersActivity.this.currentChat.megagroup) || (ChannelUsersActivity.this.selectType != 0)) {
                paramViewHolder.setText("");
              }
              for (;;)
              {
                paramViewHolder.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
                return;
                paramViewHolder.setText(LocaleController.getString("ChannelMembersInfo", 2131493178));
              }
              paramViewHolder = (ManageChatTextCell)paramViewHolder.itemView;
              if (paramInt != ChannelUsersActivity.this.addNewRow) {
                break;
              }
              if (ChannelUsersActivity.this.type == 0)
              {
                paramViewHolder.setText(LocaleController.getString("ChannelBlockUser", 2131493155), null, 2131165337, false);
                return;
              }
              if (ChannelUsersActivity.this.type == 1)
              {
                paramViewHolder.setText(LocaleController.getString("ChannelAddAdmin", 2131493144), null, 2131165335, false);
                return;
              }
            } while (ChannelUsersActivity.this.type != 2);
            if ((ChatObject.isChannel(ChannelUsersActivity.this.currentChat)) && (!ChannelUsersActivity.this.currentChat.megagroup))
            {
              paramViewHolder.setText(LocaleController.getString("AddSubscriber", 2131492939), null, 2131165496, true);
              return;
            }
            paramViewHolder.setText(LocaleController.getString("AddMember", 2131492932), null, 2131165496, true);
            return;
          } while (paramInt != ChannelUsersActivity.this.addNew2Row);
          paramViewHolder.setText(LocaleController.getString("ChannelInviteViaLink", 2131493169), null, 2131165521, false);
          return;
          paramViewHolder = (HeaderCell)paramViewHolder.itemView;
          if (paramInt == ChannelUsersActivity.this.restricted1SectionRow)
          {
            paramViewHolder.setText(LocaleController.getString("ChannelRestrictedUsers", 2131493205));
            return;
          }
          if (paramInt == ChannelUsersActivity.this.restricted2SectionRow)
          {
            paramViewHolder.setText(LocaleController.getString("ChannelBlockedUsers", 2131493156));
            return;
          }
        } while (paramInt != ChannelUsersActivity.this.changeAddHeaderRow);
        paramViewHolder.setText(LocaleController.getString("WhoCanAddMembers", 2131494635));
        return;
        paramViewHolder = (RadioCell)paramViewHolder.itemView;
        localChat = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getChat(Integer.valueOf(ChannelUsersActivity.this.chatId));
        if (paramInt == ChannelUsersActivity.this.changeAddRadio1Row)
        {
          paramViewHolder.setTag(Integer.valueOf(0));
          localObject1 = LocaleController.getString("WhoCanAddMembersAllMembers", 2131494637);
          if ((localChat != null) && (localChat.democracy)) {}
          for (bool = true;; bool = false)
          {
            paramViewHolder.setText((String)localObject1, bool, true);
            return;
          }
        }
      } while (paramInt != ChannelUsersActivity.this.changeAddRadio2Row);
      paramViewHolder.setTag(Integer.valueOf(1));
      Object localObject1 = LocaleController.getString("WhoCanAddMembersAdmins", 2131494636);
      if ((localChat != null) && (!localChat.democracy)) {}
      for (boolean bool = true;; bool = false)
      {
        paramViewHolder.setText((String)localObject1, bool, false);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new RadioCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = this.mContext;
        if (ChannelUsersActivity.this.type == 0)
        {
          paramInt = 8;
          label89:
          if (ChannelUsersActivity.this.selectType != 0) {
            break label145;
          }
        }
        label145:
        for (boolean bool = true;; bool = false)
        {
          paramViewGroup = new ManageChatUserCell(paramViewGroup, paramInt, bool);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          ((ManageChatUserCell)paramViewGroup).setDelegate(new ManageChatUserCell.ManageChatUserCellDelegate()
          {
            public boolean onOptionsButtonCheck(ManageChatUserCell paramAnonymousManageChatUserCell, boolean paramAnonymousBoolean)
            {
              paramAnonymousManageChatUserCell = ChannelUsersActivity.this.listViewAdapter.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue());
              ChannelUsersActivity localChannelUsersActivity = ChannelUsersActivity.this;
              if (!paramAnonymousBoolean) {}
              for (paramAnonymousBoolean = true;; paramAnonymousBoolean = false) {
                return localChannelUsersActivity.createMenuForParticipant(paramAnonymousManageChatUserCell, paramAnonymousBoolean);
              }
            }
          });
          break;
          paramInt = 1;
          break label89;
        }
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        continue;
        paramViewGroup = new ManageChatTextCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        continue;
        paramViewGroup = new ShadowSectionCell(this.mContext);
        continue;
        paramViewGroup = new FrameLayout(this.mContext)
        {
          protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
          {
            super.onMeasure(paramAnonymousInt1, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramAnonymousInt2) - AndroidUtilities.dp(56.0F), 1073741824));
          }
        };
        Object localObject = (FrameLayout)paramViewGroup;
        ((FrameLayout)localObject).setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
        LinearLayout localLinearLayout = new LinearLayout(this.mContext);
        localLinearLayout.setOrientation(1);
        ((FrameLayout)localObject).addView(localLinearLayout, LayoutHelper.createFrame(-2, -2.0F, 17, 20.0F, 0.0F, 20.0F, 0.0F));
        localObject = new ImageView(this.mContext);
        ((ImageView)localObject).setImageResource(2131165336);
        ((ImageView)localObject).setScaleType(ImageView.ScaleType.CENTER);
        ((ImageView)localObject).setColorFilter(new PorterDuffColorFilter(Theme.getColor("emptyListPlaceholder"), PorterDuff.Mode.MULTIPLY));
        localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, -2, 1));
        localObject = new TextView(this.mContext);
        ((TextView)localObject).setText(LocaleController.getString("NoBlockedUsers", 2131493883));
        ((TextView)localObject).setTextColor(Theme.getColor("emptyListPlaceholder"));
        ((TextView)localObject).setTextSize(1, 16.0F);
        ((TextView)localObject).setGravity(1);
        ((TextView)localObject).setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
        localObject = new TextView(this.mContext);
        if (ChannelUsersActivity.this.currentChat.megagroup) {
          ((TextView)localObject).setText(LocaleController.getString("NoBlockedGroup", 2131493882));
        }
        for (;;)
        {
          ((TextView)localObject).setTextColor(Theme.getColor("emptyListPlaceholder"));
          ((TextView)localObject).setTextSize(1, 15.0F);
          ((TextView)localObject).setGravity(1);
          localLinearLayout.addView((View)localObject, LayoutHelper.createLinear(-2, -2, 1, 0, 10, 0, 0));
          paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -1));
          break;
          ((TextView)localObject).setText(LocaleController.getString("NoBlockedChannel", 2131493881));
        }
        paramViewGroup = new HeaderCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof ManageChatUserCell)) {
        ((ManageChatUserCell)paramViewHolder.itemView).recycle();
      }
    }
  }
  
  private class SearchAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private int contactsStartRow;
    private int globalStartRow;
    private int group2StartRow;
    private int groupStartRow;
    private Context mContext;
    private SearchAdapterHelper searchAdapterHelper;
    private ArrayList<TLRPC.User> searchResult = new ArrayList();
    private ArrayList<CharSequence> searchResultNames = new ArrayList();
    private Timer searchTimer;
    private int totalCount;
    
    public SearchAdapter(Context paramContext)
    {
      this.mContext = paramContext;
      this.searchAdapterHelper = new SearchAdapterHelper(true);
      this.searchAdapterHelper.setDelegate(new SearchAdapterHelper.SearchAdapterHelperDelegate()
      {
        public void onDataSetChanged()
        {
          ChannelUsersActivity.SearchAdapter.this.notifyDataSetChanged();
        }
        
        public void onSetHashtags(ArrayList<SearchAdapterHelper.HashtagObject> paramAnonymousArrayList, HashMap<String, SearchAdapterHelper.HashtagObject> paramAnonymousHashMap) {}
      });
    }
    
    private void processSearch(final String paramString)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          final Object localObject = ChannelUsersActivity.SearchAdapter.this.searchAdapterHelper;
          String str = paramString;
          boolean bool1;
          int i;
          if (ChannelUsersActivity.this.selectType != 0)
          {
            bool1 = true;
            i = ChannelUsersActivity.this.chatId;
            if (ChannelUsersActivity.this.type != 0) {
              break label136;
            }
          }
          label136:
          for (boolean bool2 = true;; bool2 = false)
          {
            ((SearchAdapterHelper)localObject).queryServerSearch(str, bool1, false, true, true, i, bool2);
            if (ChannelUsersActivity.this.selectType == 1)
            {
              localObject = new ArrayList();
              ((ArrayList)localObject).addAll(ContactsController.getInstance(ChannelUsersActivity.this.currentAccount).contacts);
              Utilities.searchQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  Object localObject2 = ChannelUsersActivity.SearchAdapter.3.this.val$query.trim().toLowerCase();
                  if (((String)localObject2).length() == 0)
                  {
                    ChannelUsersActivity.SearchAdapter.this.updateSearchResults(new ArrayList(), new ArrayList());
                    return;
                  }
                  String str1 = LocaleController.getInstance().getTranslitString((String)localObject2);
                  Object localObject1;
                  if (!((String)localObject2).equals(str1))
                  {
                    localObject1 = str1;
                    if (str1.length() != 0) {}
                  }
                  else
                  {
                    localObject1 = null;
                  }
                  int i;
                  String[] arrayOfString;
                  ArrayList localArrayList;
                  int j;
                  label135:
                  TLRPC.User localUser;
                  if (localObject1 != null)
                  {
                    i = 1;
                    arrayOfString = new String[i + 1];
                    arrayOfString[0] = localObject2;
                    if (localObject1 != null) {
                      arrayOfString[1] = localObject1;
                    }
                    localObject2 = new ArrayList();
                    localArrayList = new ArrayList();
                    j = 0;
                    if (j >= localObject.size()) {
                      break label516;
                    }
                    localObject1 = (TLRPC.TL_contact)localObject.get(j);
                    localUser = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.TL_contact)localObject1).user_id));
                    if (localUser.id != UserConfig.getInstance(ChannelUsersActivity.this.currentAccount).getClientUserId()) {
                      break label227;
                    }
                  }
                  label227:
                  label377:
                  label450:
                  label506:
                  label514:
                  for (;;)
                  {
                    j += 1;
                    break label135;
                    i = 0;
                    break;
                    String str2 = ContactsController.formatName(localUser.first_name, localUser.last_name).toLowerCase();
                    str1 = LocaleController.getInstance().getTranslitString(str2);
                    localObject1 = str1;
                    if (str2.equals(str1)) {
                      localObject1 = null;
                    }
                    int m = 0;
                    int n = arrayOfString.length;
                    int k = 0;
                    for (;;)
                    {
                      if (k >= n) {
                        break label514;
                      }
                      str1 = arrayOfString[k];
                      if ((str2.startsWith(str1)) || (str2.contains(" " + str1)) || ((localObject1 != null) && ((((String)localObject1).startsWith(str1)) || (((String)localObject1).contains(" " + str1)))))
                      {
                        i = 1;
                        if (i == 0) {
                          break label506;
                        }
                        if (i != 1) {
                          break label450;
                        }
                        localArrayList.add(AndroidUtilities.generateSearchName(localUser.first_name, localUser.last_name, str1));
                      }
                      for (;;)
                      {
                        ((ArrayList)localObject2).add(localUser);
                        break;
                        i = m;
                        if (localUser.username == null) {
                          break label377;
                        }
                        i = m;
                        if (!localUser.username.startsWith(str1)) {
                          break label377;
                        }
                        i = 2;
                        break label377;
                        localArrayList.add(AndroidUtilities.generateSearchName("@" + localUser.username, null, "@" + str1));
                      }
                      k += 1;
                      m = i;
                    }
                  }
                  label516:
                  ChannelUsersActivity.SearchAdapter.this.updateSearchResults((ArrayList)localObject2, localArrayList);
                }
              });
            }
            return;
            bool1 = false;
            break;
          }
        }
      });
    }
    
    private void updateSearchResults(final ArrayList<TLRPC.User> paramArrayList, final ArrayList<CharSequence> paramArrayList1)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          ChannelUsersActivity.SearchAdapter.access$5702(ChannelUsersActivity.SearchAdapter.this, paramArrayList);
          ChannelUsersActivity.SearchAdapter.access$5802(ChannelUsersActivity.SearchAdapter.this, paramArrayList1);
          ChannelUsersActivity.SearchAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public TLObject getItem(int paramInt)
    {
      int j = this.searchAdapterHelper.getGroupSearch().size();
      int i = paramInt;
      if (j != 0) {
        if (j + 1 > paramInt) {
          if (paramInt != 0) {}
        }
      }
      label104:
      label149:
      do
      {
        do
        {
          do
          {
            return null;
            return (TLObject)this.searchAdapterHelper.getGroupSearch().get(paramInt - 1);
            i = paramInt - (j + 1);
            j = this.searchAdapterHelper.getGroupSearch2().size();
            paramInt = i;
            if (j == 0) {
              break label104;
            }
            if (j + 1 <= i) {
              break;
            }
          } while (i == 0);
          return (TLObject)this.searchAdapterHelper.getGroupSearch2().get(i - 1);
          paramInt = i - (j + 1);
          j = this.searchResult.size();
          i = paramInt;
          if (j == 0) {
            break label149;
          }
          if (j + 1 <= paramInt) {
            break;
          }
        } while (paramInt == 0);
        return (TLObject)this.searchResult.get(paramInt - 1);
        i = paramInt - (j + 1);
        paramInt = this.searchAdapterHelper.getGlobalSearch().size();
      } while ((paramInt == 0) || (paramInt + 1 <= i) || (i == 0));
      return (TLObject)this.searchAdapterHelper.getGlobalSearch().get(i - 1);
    }
    
    public int getItemCount()
    {
      int j = this.searchResult.size();
      int n = this.searchAdapterHelper.getGlobalSearch().size();
      int m = this.searchAdapterHelper.getGroupSearch().size();
      int k = this.searchAdapterHelper.getGroupSearch2().size();
      int i = 0;
      if (j != 0) {
        i = 0 + (j + 1);
      }
      j = i;
      if (n != 0) {
        j = i + (n + 1);
      }
      i = j;
      if (m != 0) {
        i = j + (m + 1);
      }
      j = i;
      if (k != 0) {
        j = i + (k + 1);
      }
      return j;
    }
    
    public int getItemViewType(int paramInt)
    {
      if ((paramInt == this.globalStartRow) || (paramInt == this.groupStartRow) || (paramInt == this.contactsStartRow) || (paramInt == this.group2StartRow)) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() != 1;
    }
    
    public void notifyDataSetChanged()
    {
      this.totalCount = 0;
      int i = this.searchAdapterHelper.getGroupSearch().size();
      if (i != 0)
      {
        this.groupStartRow = 0;
        this.totalCount += i + 1;
        i = this.searchAdapterHelper.getGroupSearch2().size();
        if (i == 0) {
          break label152;
        }
        this.group2StartRow = this.totalCount;
        this.totalCount += i + 1;
        label72:
        i = this.searchResult.size();
        if (i == 0) {
          break label160;
        }
        this.contactsStartRow = this.totalCount;
        this.totalCount += i + 1;
        label104:
        i = this.searchAdapterHelper.getGlobalSearch().size();
        if (i == 0) {
          break label168;
        }
        this.globalStartRow = this.totalCount;
        this.totalCount += i + 1;
      }
      for (;;)
      {
        super.notifyDataSetChanged();
        return;
        this.groupStartRow = -1;
        break;
        label152:
        this.group2StartRow = -1;
        break label72;
        label160:
        this.contactsStartRow = -1;
        break label104;
        label168:
        this.globalStartRow = -1;
      }
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      label119:
      label175:
      label366:
      label649:
      label667:
      label682:
      do
      {
        return;
        Object localObject1 = getItem(paramInt);
        TLRPC.User localUser;
        String str;
        Object localObject5;
        Object localObject3;
        int m;
        int k;
        Object localObject2;
        int i;
        int j;
        Object localObject4;
        if ((localObject1 instanceof TLRPC.User))
        {
          localUser = (TLRPC.User)localObject1;
          str = localUser.username;
          localObject5 = null;
          localObject3 = null;
          m = this.searchAdapterHelper.getGroupSearch().size();
          k = 0;
          localObject2 = null;
          localObject1 = localObject2;
          i = k;
          j = paramInt;
          if (m != 0)
          {
            if (m + 1 <= paramInt) {
              break label649;
            }
            localObject1 = this.searchAdapterHelper.getLastFoundChannel();
            i = 1;
            j = paramInt;
          }
          localObject4 = localObject1;
          paramInt = j;
          if (i == 0)
          {
            k = this.searchAdapterHelper.getGroupSearch2().size();
            localObject4 = localObject1;
            paramInt = j;
            if (k != 0)
            {
              if (k + 1 <= j) {
                break label667;
              }
              localObject4 = this.searchAdapterHelper.getLastFoundChannel2();
              paramInt = j;
            }
          }
          localObject1 = localObject3;
          k = i;
          localObject2 = localObject5;
          j = paramInt;
          if (i == 0)
          {
            m = this.searchResult.size();
            localObject1 = localObject3;
            k = i;
            localObject2 = localObject5;
            j = paramInt;
            if (m != 0)
            {
              if (m + 1 <= paramInt) {
                break label682;
              }
              i = 1;
              localObject3 = (CharSequence)this.searchResultNames.get(paramInt - 1);
              localObject1 = localObject3;
              k = i;
              localObject2 = localObject5;
              j = paramInt;
              if (localObject3 != null)
              {
                localObject1 = localObject3;
                k = i;
                localObject2 = localObject5;
                j = paramInt;
                if (str != null)
                {
                  localObject1 = localObject3;
                  k = i;
                  localObject2 = localObject5;
                  j = paramInt;
                  if (str.length() > 0)
                  {
                    localObject1 = localObject3;
                    k = i;
                    localObject2 = localObject5;
                    j = paramInt;
                    if (((CharSequence)localObject3).toString().startsWith("@" + str))
                    {
                      localObject2 = localObject3;
                      localObject1 = null;
                      j = paramInt;
                      k = i;
                    }
                  }
                }
              }
            }
          }
          localObject3 = localObject2;
          if (k == 0)
          {
            paramInt = this.searchAdapterHelper.getGlobalSearch().size();
            localObject3 = localObject2;
            if (paramInt != 0)
            {
              localObject3 = localObject2;
              if (paramInt + 1 > j)
              {
                localObject3 = this.searchAdapterHelper.getLastFoundUsername();
                localObject2 = localObject3;
                if (((String)localObject3).startsWith("@")) {
                  localObject2 = ((String)localObject3).substring(1);
                }
              }
            }
          }
        }
        for (;;)
        {
          try
          {
            localObject3 = new SpannableStringBuilder();
            ((SpannableStringBuilder)localObject3).append("@");
            ((SpannableStringBuilder)localObject3).append(str);
            paramInt = str.toLowerCase().indexOf((String)localObject2);
            if (paramInt != -1)
            {
              i = ((String)localObject2).length();
              if (paramInt != 0) {
                continue;
              }
              i += 1;
              ((SpannableStringBuilder)localObject3).setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt, paramInt + i, 33);
            }
          }
          catch (Exception localException)
          {
            localObject3 = str;
            FileLog.e(localException);
            continue;
          }
          if (localObject4 != null)
          {
            localObject1 = UserObject.getUserName(localUser);
            localObject2 = new SpannableStringBuilder((CharSequence)localObject1);
            paramInt = ((String)localObject1).toLowerCase().indexOf((String)localObject4);
            localObject1 = localObject2;
            if (paramInt != -1)
            {
              ((SpannableStringBuilder)localObject2).setSpan(new ForegroundColorSpan(Theme.getColor("windowBackgroundWhiteBlueText4")), paramInt, ((String)localObject4).length() + paramInt, 33);
              localObject1 = localObject2;
            }
          }
          paramViewHolder = (ManageChatUserCell)paramViewHolder.itemView;
          paramViewHolder.setTag(Integer.valueOf(j));
          paramViewHolder.setData(localUser, (CharSequence)localObject1, (CharSequence)localObject3);
          return;
          localUser = MessagesController.getInstance(ChannelUsersActivity.this.currentAccount).getUser(Integer.valueOf(((TLRPC.ChannelParticipant)localObject1).user_id));
          break;
          j = paramInt - (m + 1);
          localObject1 = localObject2;
          i = k;
          break label119;
          paramInt = j - (k + 1);
          localObject4 = localObject1;
          break label175;
          j = paramInt - (m + 1);
          localObject1 = localObject3;
          k = i;
          localObject2 = localObject5;
          break label366;
          paramInt += 1;
        }
        paramViewHolder = (GraySectionCell)paramViewHolder.itemView;
        if (paramInt == this.groupStartRow)
        {
          if (ChannelUsersActivity.this.type == 0)
          {
            paramViewHolder.setText(LocaleController.getString("ChannelRestrictedUsers", 2131493205).toUpperCase());
            return;
          }
          if ((ChatObject.isChannel(ChannelUsersActivity.this.currentChat)) && (!ChannelUsersActivity.this.currentChat.megagroup))
          {
            ChannelUsersActivity.this.actionBar.setTitle(LocaleController.getString("ChannelSubscribers", 2131493210));
            return;
          }
          paramViewHolder.setText(LocaleController.getString("ChannelMembers", 2131493177).toUpperCase());
          return;
        }
        if (paramInt == this.group2StartRow)
        {
          paramViewHolder.setText(LocaleController.getString("ChannelBlockedUsers", 2131493156).toUpperCase());
          return;
        }
        if (paramInt == this.globalStartRow)
        {
          paramViewHolder.setText(LocaleController.getString("GlobalSearch", 2131493628).toUpperCase());
          return;
        }
      } while (paramInt != this.contactsStartRow);
      paramViewHolder.setText(LocaleController.getString("Contacts", 2131493290).toUpperCase());
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new GraySectionCell(this.mContext);
        return new RecyclerListView.Holder(paramViewGroup);
      }
      paramViewGroup = this.mContext;
      if (ChannelUsersActivity.this.selectType == 0) {}
      for (boolean bool = true;; bool = false)
      {
        paramViewGroup = new ManageChatUserCell(paramViewGroup, 2, bool);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        ((ManageChatUserCell)paramViewGroup).setDelegate(new ManageChatUserCell.ManageChatUserCellDelegate()
        {
          public boolean onOptionsButtonCheck(ManageChatUserCell paramAnonymousManageChatUserCell, boolean paramAnonymousBoolean)
          {
            boolean bool = false;
            ChannelUsersActivity localChannelUsersActivity;
            if ((ChannelUsersActivity.SearchAdapter.this.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue()) instanceof TLRPC.ChannelParticipant))
            {
              paramAnonymousManageChatUserCell = (TLRPC.ChannelParticipant)ChannelUsersActivity.SearchAdapter.this.getItem(((Integer)paramAnonymousManageChatUserCell.getTag()).intValue());
              localChannelUsersActivity = ChannelUsersActivity.this;
              if (paramAnonymousBoolean) {
                break label71;
              }
            }
            label71:
            for (paramAnonymousBoolean = true;; paramAnonymousBoolean = false)
            {
              bool = localChannelUsersActivity.createMenuForParticipant(paramAnonymousManageChatUserCell, paramAnonymousBoolean);
              return bool;
            }
          }
        });
        break;
      }
    }
    
    public void onViewRecycled(RecyclerView.ViewHolder paramViewHolder)
    {
      if ((paramViewHolder.itemView instanceof ManageChatUserCell)) {
        ((ManageChatUserCell)paramViewHolder.itemView).recycle();
      }
    }
    
    public void searchDialogs(final String paramString)
    {
      try
      {
        if (this.searchTimer != null) {
          this.searchTimer.cancel();
        }
        if (paramString == null)
        {
          this.searchResult.clear();
          this.searchResultNames.clear();
          paramString = this.searchAdapterHelper;
          if (ChannelUsersActivity.this.type != 0)
          {
            bool1 = true;
            int i = ChannelUsersActivity.this.chatId;
            if (ChannelUsersActivity.this.type != 0) {
              break label102;
            }
            bool2 = true;
            paramString.queryServerSearch(null, bool1, false, true, true, i, bool2);
            notifyDataSetChanged();
          }
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          boolean bool1 = false;
          continue;
          label102:
          boolean bool2 = false;
        }
        this.searchTimer = new Timer();
        this.searchTimer.schedule(new TimerTask()
        {
          public void run()
          {
            try
            {
              ChannelUsersActivity.SearchAdapter.this.searchTimer.cancel();
              ChannelUsersActivity.SearchAdapter.access$5002(ChannelUsersActivity.SearchAdapter.this, null);
              ChannelUsersActivity.SearchAdapter.this.processSearch(paramString);
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
        }, 200L, 300L);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/ChannelUsersActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */