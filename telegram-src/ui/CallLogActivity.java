package org.telegram.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageAction;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterPhoneCalls;
import org.telegram.tgnet.TLRPC.TL_inputPeerEmpty;
import org.telegram.tgnet.TLRPC.TL_messageActionHistoryClear;
import org.telegram.tgnet.TLRPC.TL_messageActionPhoneCall;
import org.telegram.tgnet.TLRPC.TL_messages_search;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonBusy;
import org.telegram.tgnet.TLRPC.TL_phoneCallDiscardReasonMissed;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.voip.VoIPHelper;

public class CallLogActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int TYPE_IN = 1;
  private static final int TYPE_MISSED = 2;
  private static final int TYPE_OUT = 0;
  private View.OnClickListener callBtnClickListener = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      paramAnonymousView = (CallLogActivity.CallLogRow)paramAnonymousView.getTag();
      VoIPHelper.startCall(CallLogActivity.access$102(CallLogActivity.this, paramAnonymousView.user), CallLogActivity.this.getParentActivity(), null);
    }
  };
  private ArrayList<CallLogRow> calls = new ArrayList();
  private EmptyTextProgressView emptyView;
  private boolean endReached;
  private boolean firstLoaded;
  private ImageView floatingButton;
  private boolean floatingHidden;
  private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
  private Drawable greenDrawable;
  private Drawable greenDrawable2;
  private ImageSpan iconIn;
  private ImageSpan iconMissed;
  private ImageSpan iconOut;
  private TLRPC.User lastCallUser;
  private LinearLayoutManager layoutManager;
  private RecyclerListView listView;
  private ListAdapter listViewAdapter;
  private boolean loading;
  private int prevPosition;
  private int prevTop;
  private Drawable redDrawable;
  private boolean scrollUpdated;
  
  private void confirmAndDelete(final CallLogRow paramCallLogRow)
  {
    if (getParentActivity() == null) {
      return;
    }
    new AlertDialog.Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", 2131492981)).setMessage(LocaleController.getString("ConfirmDeleteCallLog", 2131493281)).setPositiveButton(LocaleController.getString("Delete", 2131493356), new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
      {
        paramAnonymousDialogInterface = new ArrayList();
        Iterator localIterator = paramCallLogRow.calls.iterator();
        while (localIterator.hasNext()) {
          paramAnonymousDialogInterface.add(Integer.valueOf(((TLRPC.Message)localIterator.next()).id));
        }
        MessagesController.getInstance(CallLogActivity.this.currentAccount).deleteMessages(paramAnonymousDialogInterface, null, null, 0, false);
      }
    }).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null).show().setCanceledOnTouchOutside(true);
  }
  
  private void getCalls(int paramInt1, int paramInt2)
  {
    if (this.loading) {
      return;
    }
    this.loading = true;
    if ((this.emptyView != null) && (!this.firstLoaded)) {
      this.emptyView.showProgress();
    }
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
    TLRPC.TL_messages_search localTL_messages_search = new TLRPC.TL_messages_search();
    localTL_messages_search.limit = paramInt2;
    localTL_messages_search.peer = new TLRPC.TL_inputPeerEmpty();
    localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterPhoneCalls();
    localTL_messages_search.q = "";
    localTL_messages_search.offset_id = paramInt1;
    paramInt1 = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_search, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            if (paramAnonymousTL_error == null)
            {
              SparseArray localSparseArray = new SparseArray();
              TLRPC.messages_Messages localmessages_Messages = (TLRPC.messages_Messages)paramAnonymousTLObject;
              CallLogActivity.access$702(CallLogActivity.this, localmessages_Messages.messages.isEmpty());
              int i = 0;
              while (i < localmessages_Messages.users.size())
              {
                localObject1 = (TLRPC.User)localmessages_Messages.users.get(i);
                localSparseArray.put(((TLRPC.User)localObject1).id, localObject1);
                i += 1;
              }
              if (CallLogActivity.this.calls.size() > 0) {}
              TLRPC.Message localMessage;
              Object localObject2;
              for (Object localObject1 = (CallLogActivity.CallLogRow)CallLogActivity.this.calls.get(CallLogActivity.this.calls.size() - 1);; localObject1 = null)
              {
                int j = 0;
                for (;;)
                {
                  if (j >= localmessages_Messages.messages.size()) {
                    break label456;
                  }
                  localMessage = (TLRPC.Message)localmessages_Messages.messages.get(j);
                  localObject2 = localObject1;
                  if (localMessage.action != null)
                  {
                    if (!(localMessage.action instanceof TLRPC.TL_messageActionHistoryClear)) {
                      break;
                    }
                    localObject2 = localObject1;
                  }
                  j += 1;
                  localObject1 = localObject2;
                }
              }
              label238:
              int k;
              if (localMessage.from_id == UserConfig.getInstance(CallLogActivity.this.currentAccount).getClientUserId())
              {
                i = 0;
                localObject2 = localMessage.action.reason;
                k = i;
                if (i == 1) {
                  if (!(localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonMissed))
                  {
                    k = i;
                    if (!(localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonBusy)) {}
                  }
                  else
                  {
                    k = 2;
                  }
                }
                if (localMessage.from_id != UserConfig.getInstance(CallLogActivity.this.currentAccount).getClientUserId()) {
                  break label447;
                }
              }
              label447:
              for (i = localMessage.to_id.user_id;; i = localMessage.from_id)
              {
                if ((localObject1 != null) && (((CallLogActivity.CallLogRow)localObject1).user.id == i))
                {
                  localObject2 = localObject1;
                  if (((CallLogActivity.CallLogRow)localObject1).type == k) {}
                }
                else
                {
                  if ((localObject1 != null) && (!CallLogActivity.this.calls.contains(localObject1))) {
                    CallLogActivity.this.calls.add(localObject1);
                  }
                  localObject2 = new CallLogActivity.CallLogRow(CallLogActivity.this, null);
                  ((CallLogActivity.CallLogRow)localObject2).calls = new ArrayList();
                  ((CallLogActivity.CallLogRow)localObject2).user = ((TLRPC.User)localSparseArray.get(i));
                  ((CallLogActivity.CallLogRow)localObject2).type = k;
                }
                ((CallLogActivity.CallLogRow)localObject2).calls.add(localMessage);
                break;
                i = 1;
                break label238;
              }
              label456:
              if ((localObject1 != null) && (((CallLogActivity.CallLogRow)localObject1).calls.size() > 0) && (!CallLogActivity.this.calls.contains(localObject1))) {
                CallLogActivity.this.calls.add(localObject1);
              }
            }
            for (;;)
            {
              CallLogActivity.access$802(CallLogActivity.this, false);
              CallLogActivity.access$1702(CallLogActivity.this, true);
              if (CallLogActivity.this.emptyView != null) {
                CallLogActivity.this.emptyView.showTextView();
              }
              if (CallLogActivity.this.listViewAdapter != null) {
                CallLogActivity.this.listViewAdapter.notifyDataSetChanged();
              }
              return;
              CallLogActivity.access$702(CallLogActivity.this, true);
            }
          }
        });
      }
    }, 2);
    ConnectionsManager.getInstance(this.currentAccount).bindRequestToGuid(paramInt1, this.classGuid);
  }
  
  private void hideFloatingButton(boolean paramBoolean)
  {
    if (this.floatingHidden == paramBoolean) {
      return;
    }
    this.floatingHidden = paramBoolean;
    Object localObject = this.floatingButton;
    float f;
    ImageView localImageView;
    if (this.floatingHidden)
    {
      f = AndroidUtilities.dp(100.0F);
      localObject = ObjectAnimator.ofFloat(localObject, "translationY", new float[] { f }).setDuration(300L);
      ((ObjectAnimator)localObject).setInterpolator(this.floatingInterpolator);
      localImageView = this.floatingButton;
      if (paramBoolean) {
        break label91;
      }
    }
    label91:
    for (paramBoolean = true;; paramBoolean = false)
    {
      localImageView.setClickable(paramBoolean);
      ((ObjectAnimator)localObject).start();
      return;
      f = 0.0F;
      break;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.greenDrawable = getParentActivity().getResources().getDrawable(2131165368).mutate();
    this.greenDrawable.setBounds(0, 0, this.greenDrawable.getIntrinsicWidth(), this.greenDrawable.getIntrinsicHeight());
    this.greenDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("calls_callReceivedGreenIcon"), PorterDuff.Mode.MULTIPLY));
    this.iconOut = new ImageSpan(this.greenDrawable, 0);
    this.greenDrawable2 = getParentActivity().getResources().getDrawable(2131165371).mutate();
    this.greenDrawable2.setBounds(0, 0, this.greenDrawable2.getIntrinsicWidth(), this.greenDrawable2.getIntrinsicHeight());
    this.greenDrawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor("calls_callReceivedGreenIcon"), PorterDuff.Mode.MULTIPLY));
    this.iconIn = new ImageSpan(this.greenDrawable2, 0);
    this.redDrawable = getParentActivity().getResources().getDrawable(2131165371).mutate();
    this.redDrawable.setBounds(0, 0, this.redDrawable.getIntrinsicWidth(), this.redDrawable.getIntrinsicHeight());
    this.redDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("calls_callReceivedRedIcon"), PorterDuff.Mode.MULTIPLY));
    this.iconMissed = new ImageSpan(this.redDrawable, 0);
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("Calls", 2131493124));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          CallLogActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.setText(LocaleController.getString("NoCallLog", 2131493884));
    localFrameLayout.addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setEmptyView(this.emptyView);
    Object localObject1 = this.listView;
    Object localObject2 = new LinearLayoutManager(paramContext, 1, false);
    this.layoutManager = ((LinearLayoutManager)localObject2);
    ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
    localObject1 = this.listView;
    localObject2 = new ListAdapter(paramContext);
    this.listViewAdapter = ((ListAdapter)localObject2);
    ((RecyclerListView)localObject1).setAdapter((RecyclerView.Adapter)localObject2);
    localObject1 = this.listView;
    int i;
    label554:
    label881:
    float f1;
    label893:
    int j;
    label902:
    float f2;
    if (LocaleController.isRTL)
    {
      i = 1;
      ((RecyclerListView)localObject1).setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= CallLogActivity.this.calls.size())) {
            return;
          }
          paramAnonymousView = (CallLogActivity.CallLogRow)CallLogActivity.this.calls.get(paramAnonymousInt);
          Bundle localBundle = new Bundle();
          localBundle.putInt("user_id", paramAnonymousView.user.id);
          localBundle.putInt("message_id", ((TLRPC.Message)paramAnonymousView.calls.get(0)).id);
          NotificationCenter.getInstance(CallLogActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
          CallLogActivity.this.presentFragment(new ChatActivity(localBundle), true);
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(final View paramAnonymousView, int paramAnonymousInt)
        {
          if ((paramAnonymousInt < 0) || (paramAnonymousInt >= CallLogActivity.this.calls.size())) {
            return false;
          }
          paramAnonymousView = (CallLogActivity.CallLogRow)CallLogActivity.this.calls.get(paramAnonymousInt);
          ArrayList localArrayList = new ArrayList();
          localArrayList.add(LocaleController.getString("Delete", 2131493356));
          if (VoIPHelper.canRateCall((TLRPC.TL_messageActionPhoneCall)((TLRPC.Message)paramAnonymousView.calls.get(0)).action)) {
            localArrayList.add(LocaleController.getString("CallMessageReportProblem", 2131493114));
          }
          new AlertDialog.Builder(CallLogActivity.this.getParentActivity()).setTitle(LocaleController.getString("Calls", 2131493124)).setItems((CharSequence[])localArrayList.toArray(new String[localArrayList.size()]), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              switch (paramAnonymous2Int)
              {
              default: 
                return;
              case 0: 
                CallLogActivity.this.confirmAndDelete(paramAnonymousView);
                return;
              }
              VoIPHelper.showRateAlert(CallLogActivity.this.getParentActivity(), (TLRPC.TL_messageActionPhoneCall)((TLRPC.Message)paramAnonymousView.calls.get(0)).action);
            }
          }).show();
          return true;
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          int i = CallLogActivity.this.layoutManager.findFirstVisibleItemPosition();
          if (i == -1)
          {
            paramAnonymousInt1 = 0;
            if (paramAnonymousInt1 > 0)
            {
              paramAnonymousInt2 = CallLogActivity.this.listViewAdapter.getItemCount();
              if ((!CallLogActivity.this.endReached) && (!CallLogActivity.this.loading) && (!CallLogActivity.this.calls.isEmpty()) && (i + paramAnonymousInt1 >= paramAnonymousInt2 - 5)) {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    CallLogActivity.this.getCalls(((TLRPC.Message)this.val$row.calls.get(this.val$row.calls.size() - 1)).id, 100);
                  }
                });
              }
            }
            if (CallLogActivity.this.floatingButton.getVisibility() != 8)
            {
              paramAnonymousRecyclerView = paramAnonymousRecyclerView.getChildAt(0);
              paramAnonymousInt2 = 0;
              if (paramAnonymousRecyclerView != null) {
                paramAnonymousInt2 = paramAnonymousRecyclerView.getTop();
              }
              paramAnonymousInt1 = 1;
              if (CallLogActivity.this.prevPosition != i) {
                break label279;
              }
              paramAnonymousInt1 = CallLogActivity.this.prevTop;
              if (paramAnonymousInt2 >= CallLogActivity.this.prevTop) {
                break label268;
              }
              bool = true;
              label182:
              if (Math.abs(paramAnonymousInt1 - paramAnonymousInt2) <= 1) {
                break label274;
              }
            }
          }
          label268:
          label274:
          for (paramAnonymousInt1 = 1;; paramAnonymousInt1 = 0)
          {
            if ((paramAnonymousInt1 != 0) && (CallLogActivity.this.scrollUpdated)) {
              CallLogActivity.this.hideFloatingButton(bool);
            }
            CallLogActivity.access$1102(CallLogActivity.this, i);
            CallLogActivity.access$1202(CallLogActivity.this, paramAnonymousInt2);
            CallLogActivity.access$1302(CallLogActivity.this, true);
            return;
            paramAnonymousInt1 = Math.abs(CallLogActivity.this.layoutManager.findLastVisibleItemPosition() - i) + 1;
            break;
            bool = false;
            break label182;
          }
          label279:
          if (i > CallLogActivity.this.prevPosition) {}
          for (boolean bool = true;; bool = false) {
            break;
          }
        }
      });
      if (!this.loading) {
        break label971;
      }
      this.emptyView.showProgress();
      this.floatingButton = new ImageView(paramContext);
      this.floatingButton.setVisibility(0);
      this.floatingButton.setScaleType(ImageView.ScaleType.CENTER);
      localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("chats_actionBackground"), Theme.getColor("chats_actionPressedBackground"));
      localObject1 = localObject2;
      if (Build.VERSION.SDK_INT < 21)
      {
        paramContext = paramContext.getResources().getDrawable(2131165322).mutate();
        paramContext.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
        localObject1 = new CombinedDrawable(paramContext, (Drawable)localObject2, 0, 0);
        ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
      }
      this.floatingButton.setBackgroundDrawable((Drawable)localObject1);
      this.floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("chats_actionIcon"), PorterDuff.Mode.MULTIPLY));
      this.floatingButton.setImageResource(2131165372);
      if (Build.VERSION.SDK_INT >= 21)
      {
        paramContext = new StateListAnimator();
        localObject1 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
        paramContext.addState(new int[] { 16842919 }, (Animator)localObject1);
        localObject1 = ObjectAnimator.ofFloat(this.floatingButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
        paramContext.addState(new int[0], (Animator)localObject1);
        this.floatingButton.setStateListAnimator(paramContext);
        this.floatingButton.setOutlineProvider(new ViewOutlineProvider()
        {
          @SuppressLint({"NewApi"})
          public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
          {
            paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
          }
        });
      }
      paramContext = this.floatingButton;
      if (Build.VERSION.SDK_INT < 21) {
        break label981;
      }
      i = 56;
      if (Build.VERSION.SDK_INT < 21) {
        break label988;
      }
      f1 = 56.0F;
      if (!LocaleController.isRTL) {
        break label995;
      }
      j = 3;
      if (!LocaleController.isRTL) {
        break label1001;
      }
      f2 = 14.0F;
      label912:
      if (!LocaleController.isRTL) {
        break label1006;
      }
    }
    label971:
    label981:
    label988:
    label995:
    label1001:
    label1006:
    for (float f3 = 0.0F;; f3 = 14.0F)
    {
      localFrameLayout.addView(paramContext, LayoutHelper.createFrame(i, f1, j | 0x50, f2, 0.0F, f3, 14.0F));
      this.floatingButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          paramAnonymousView = new Bundle();
          paramAnonymousView.putBoolean("destroyAfterSelect", true);
          paramAnonymousView.putBoolean("returnAsResult", true);
          paramAnonymousView.putBoolean("onlyUsers", true);
          paramAnonymousView = new ContactsActivity(paramAnonymousView);
          paramAnonymousView.setDelegate(new ContactsActivity.ContactsActivityDelegate()
          {
            public void didSelectContact(TLRPC.User paramAnonymous2User, String paramAnonymous2String, ContactsActivity paramAnonymous2ContactsActivity)
            {
              VoIPHelper.startCall(paramAnonymous2User, CallLogActivity.this.getParentActivity(), null);
            }
          });
          CallLogActivity.this.presentFragment(paramAnonymousView);
        }
      });
      return this.fragmentView;
      i = 2;
      break;
      this.emptyView.showTextView();
      break label554;
      i = 60;
      break label881;
      f1 = 60.0F;
      break label893;
      j = 5;
      break label902;
      f2 = 0.0F;
      break label912;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if ((paramInt1 == NotificationCenter.didReceivedNewMessages) && (this.firstLoaded)) {
      paramVarArgs = ((ArrayList)paramVarArgs[1]).iterator();
    }
    while (paramVarArgs.hasNext())
    {
      Object localObject1 = (MessageObject)paramVarArgs.next();
      if ((((MessageObject)localObject1).messageOwner.action != null) && ((((MessageObject)localObject1).messageOwner.action instanceof TLRPC.TL_messageActionPhoneCall)))
      {
        if (((MessageObject)localObject1).messageOwner.from_id == UserConfig.getInstance(this.currentAccount).getClientUserId())
        {
          paramInt2 = ((MessageObject)localObject1).messageOwner.to_id.user_id;
          label102:
          if (((MessageObject)localObject1).messageOwner.from_id != UserConfig.getInstance(this.currentAccount).getClientUserId()) {
            break label252;
          }
        }
        int i;
        label252:
        for (paramInt1 = 0;; paramInt1 = 1)
        {
          localObject2 = ((MessageObject)localObject1).messageOwner.action.reason;
          i = paramInt1;
          if (paramInt1 == 1) {
            if (!(localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonMissed))
            {
              i = paramInt1;
              if (!(localObject2 instanceof TLRPC.TL_phoneCallDiscardReasonBusy)) {}
            }
            else
            {
              i = 2;
            }
          }
          if (this.calls.size() <= 0) {
            break label257;
          }
          localObject2 = (CallLogRow)this.calls.get(0);
          if ((((CallLogRow)localObject2).user.id != paramInt2) || (((CallLogRow)localObject2).type != i)) {
            break label257;
          }
          ((CallLogRow)localObject2).calls.add(0, ((MessageObject)localObject1).messageOwner);
          this.listViewAdapter.notifyItemChanged(0);
          break;
          paramInt2 = ((MessageObject)localObject1).messageOwner.from_id;
          break label102;
        }
        label257:
        Object localObject2 = new CallLogRow(null);
        ((CallLogRow)localObject2).calls = new ArrayList();
        ((CallLogRow)localObject2).calls.add(((MessageObject)localObject1).messageOwner);
        ((CallLogRow)localObject2).user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramInt2));
        ((CallLogRow)localObject2).type = i;
        this.calls.add(0, localObject2);
        this.listViewAdapter.notifyItemInserted(0);
        continue;
        if ((paramInt1 == NotificationCenter.messagesDeleted) && (this.firstLoaded))
        {
          paramInt1 = 0;
          paramVarArgs = (ArrayList)paramVarArgs[0];
          localObject1 = this.calls.iterator();
          while (((Iterator)localObject1).hasNext())
          {
            localObject2 = (CallLogRow)((Iterator)localObject1).next();
            Iterator localIterator = ((CallLogRow)localObject2).calls.iterator();
            paramInt2 = paramInt1;
            while (localIterator.hasNext()) {
              if (paramVarArgs.contains(Integer.valueOf(((TLRPC.Message)localIterator.next()).id)))
              {
                paramInt2 = 1;
                localIterator.remove();
              }
            }
            paramInt1 = paramInt2;
            if (((CallLogRow)localObject2).calls.size() == 0)
            {
              ((Iterator)localObject1).remove();
              paramInt1 = paramInt2;
            }
          }
          if ((paramInt1 != 0) && (this.listViewAdapter != null)) {
            this.listViewAdapter.notifyDataSetChanged();
          }
        }
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    Object localObject7 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (CallLogActivity.this.listView != null)
        {
          int j = CallLogActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = CallLogActivity.this.listView.getChildAt(i);
            if ((localView instanceof ProfileSearchCell)) {
              ((ProfileSearchCell)localView).update(0);
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { LocationCell.class, CustomCell.class }, null, null, null, "windowBackgroundWhite");
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
    localObject2 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.listView, 0, new Class[] { LoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { TextInfoPrivacyCell.class }, null, null, null, "windowBackgroundGrayShadow");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.listView, 0, new Class[] { TextInfoPrivacyCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText4");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "chats_actionIcon");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "chats_actionBackground");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "chats_actionPressedBackground");
    Object localObject3 = this.listView;
    Object localObject4 = Theme.dialogs_verifiedCheckDrawable;
    localObject3 = new ThemeDescription((View)localObject3, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject4 }, null, "chats_verifiedCheck");
    localObject4 = this.listView;
    Object localObject5 = Theme.dialogs_verifiedDrawable;
    localObject4 = new ThemeDescription((View)localObject4, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject5 }, null, "chats_verifiedBackground");
    localObject5 = this.listView;
    Object localObject6 = Theme.dialogs_offlinePaint;
    localObject5 = new ThemeDescription((View)localObject5, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject6, null, null, "windowBackgroundWhiteGrayText3");
    localObject6 = this.listView;
    Object localObject8 = Theme.dialogs_onlinePaint;
    localObject6 = new ThemeDescription((View)localObject6, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject8, null, null, "windowBackgroundWhiteBlueText3");
    localObject8 = this.listView;
    Object localObject9 = Theme.dialogs_namePaint;
    localObject8 = new ThemeDescription((View)localObject8, 0, new Class[] { ProfileSearchCell.class }, (Paint)localObject9, null, null, "chats_name");
    localObject9 = this.listView;
    Object localObject10 = Theme.avatar_photoDrawable;
    Object localObject11 = Theme.avatar_broadcastDrawable;
    Object localObject12 = Theme.avatar_savedDrawable;
    localObject9 = new ThemeDescription((View)localObject9, 0, new Class[] { ProfileSearchCell.class }, null, new Drawable[] { localObject10, localObject11, localObject12 }, null, "avatar_text");
    localObject10 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundRed");
    localObject11 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundOrange");
    localObject12 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundViolet");
    ThemeDescription localThemeDescription16 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundGreen");
    ThemeDescription localThemeDescription17 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundCyan");
    ThemeDescription localThemeDescription18 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundBlue");
    localObject7 = new ThemeDescription(null, 0, null, null, null, (ThemeDescription.ThemeDescriptionDelegate)localObject7, "avatar_backgroundPink");
    Object localObject13 = this.listView;
    Object localObject14 = this.greenDrawable;
    Drawable localDrawable1 = this.greenDrawable2;
    Drawable localDrawable2 = Theme.chat_msgCallUpRedDrawable;
    Drawable localDrawable3 = Theme.chat_msgCallDownRedDrawable;
    localObject13 = new ThemeDescription((View)localObject13, 0, new Class[] { View.class }, null, new Drawable[] { localObject14, localDrawable1, localDrawable2, localDrawable3 }, null, "calls_callReceivedGreenIcon");
    localObject14 = this.listView;
    localDrawable1 = this.redDrawable;
    localDrawable2 = Theme.chat_msgCallUpGreenDrawable;
    localDrawable3 = Theme.chat_msgCallDownGreenDrawable;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localObject1, localObject2, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localObject3, localObject4, localObject5, localObject6, localObject8, localObject9, localObject10, localObject11, localObject12, localThemeDescription16, localThemeDescription17, localThemeDescription18, localObject7, localObject13, new ThemeDescription((View)localObject14, 0, new Class[] { View.class }, null, new Drawable[] { localDrawable1, localDrawable2, localDrawable3 }, null, "calls_callReceivedRedIcon") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    getCalls(0, 50);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didReceivedNewMessages);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagesDeleted);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didReceivedNewMessages);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagesDeleted);
  }
  
  public void onRequestPermissionsResultFragment(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    if (paramInt == 101)
    {
      if ((paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0)) {
        VoIPHelper.startCall(this.lastCallUser, getParentActivity(), null);
      }
    }
    else {
      return;
    }
    VoIPHelper.permissionDenied(getParentActivity(), null);
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listViewAdapter != null) {
      this.listViewAdapter.notifyDataSetChanged();
    }
  }
  
  private class CallLogRow
  {
    public List<TLRPC.Message> calls;
    public int type;
    public TLRPC.User user;
    
    private CallLogRow() {}
  }
  
  private class CustomCell
    extends FrameLayout
  {
    public CustomCell(Context paramContext)
    {
      super();
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
    
    public int getItemCount()
    {
      int j = CallLogActivity.this.calls.size();
      int i = j;
      if (!CallLogActivity.this.calls.isEmpty())
      {
        i = j;
        if (!CallLogActivity.this.endReached) {
          i = j + 1;
        }
      }
      return i;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (paramInt < CallLogActivity.this.calls.size()) {
        return 0;
      }
      if ((!CallLogActivity.this.endReached) && (paramInt == CallLogActivity.this.calls.size())) {
        return 1;
      }
      return 2;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getAdapterPosition() != CallLogActivity.this.calls.size();
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      CallLogActivity.ViewItem localViewItem;
      ProfileSearchCell localProfileSearchCell;
      CallLogActivity.CallLogRow localCallLogRow;
      Object localObject;
      if (paramViewHolder.getItemViewType() == 0)
      {
        localViewItem = (CallLogActivity.ViewItem)paramViewHolder.itemView.getTag();
        localProfileSearchCell = localViewItem.cell;
        localCallLogRow = (CallLogActivity.CallLogRow)CallLogActivity.this.calls.get(paramInt);
        localObject = (TLRPC.Message)localCallLogRow.calls.get(0);
        if (!LocaleController.isRTL) {
          break label213;
        }
        paramViewHolder = "‫";
        if (localCallLogRow.calls.size() != 1) {
          break label219;
        }
        localObject = new SpannableString(paramViewHolder + "  " + LocaleController.formatDateCallLog(((TLRPC.Message)localObject).date));
        label121:
        switch (localCallLogRow.type)
        {
        default: 
          label152:
          localProfileSearchCell.setData(localCallLogRow.user, null, null, (CharSequence)localObject, false, false);
          if ((paramInt == CallLogActivity.this.calls.size() - 1) && (CallLogActivity.this.endReached)) {
            break;
          }
        }
      }
      for (boolean bool = true;; bool = false)
      {
        localProfileSearchCell.useSeparator = bool;
        localViewItem.button.setTag(localCallLogRow);
        return;
        label213:
        paramViewHolder = "";
        break;
        label219:
        localObject = new SpannableString(String.format(paramViewHolder + "  (%d) %s", new Object[] { Integer.valueOf(localCallLogRow.calls.size()), LocaleController.formatDateCallLog(((TLRPC.Message)localObject).date) }));
        break label121;
        ((SpannableString)localObject).setSpan(CallLogActivity.this.iconOut, paramViewHolder.length(), paramViewHolder.length() + 1, 0);
        break label152;
        ((SpannableString)localObject).setSpan(CallLogActivity.this.iconIn, paramViewHolder.length(), paramViewHolder.length() + 1, 0);
        break label152;
        ((SpannableString)localObject).setSpan(CallLogActivity.this.iconMissed, paramViewHolder.length(), paramViewHolder.length() + 1, 0);
        break label152;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new TextInfoPrivacyCell(this.mContext);
        paramViewGroup.setBackgroundDrawable(Theme.getThemedDrawable(this.mContext, 2131165332, "windowBackgroundGrayShadow"));
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new CallLogActivity.CustomCell(CallLogActivity.this, this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        ProfileSearchCell localProfileSearchCell = new ProfileSearchCell(this.mContext);
        localProfileSearchCell.setPaddingRight(AndroidUtilities.dp(32.0F));
        paramViewGroup.addView(localProfileSearchCell);
        ImageView localImageView = new ImageView(this.mContext);
        localImageView.setImageResource(2131165614);
        localImageView.setAlpha(214);
        localImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor("windowBackgroundWhiteGrayIcon"), PorterDuff.Mode.MULTIPLY));
        localImageView.setBackgroundDrawable(Theme.createSelectorDrawable(788529152, 0));
        localImageView.setScaleType(ImageView.ScaleType.CENTER);
        localImageView.setOnClickListener(CallLogActivity.this.callBtnClickListener);
        if (LocaleController.isRTL) {}
        for (paramInt = 3;; paramInt = 5)
        {
          paramViewGroup.addView(localImageView, LayoutHelper.createFrame(48, 48.0F, paramInt | 0x10, 8.0F, 0.0F, 8.0F, 0.0F));
          paramViewGroup.setTag(new CallLogActivity.ViewItem(CallLogActivity.this, localImageView, localProfileSearchCell));
          break;
        }
        paramViewGroup = new LoadingCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
    }
  }
  
  private class ViewItem
  {
    public ImageView button;
    public ProfileSearchCell cell;
    
    public ViewItem(ImageView paramImageView, ProfileSearchCell paramProfileSearchCell)
    {
      this.button = paramImageView;
      this.cell = paramProfileSearchCell;
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/CallLogActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */