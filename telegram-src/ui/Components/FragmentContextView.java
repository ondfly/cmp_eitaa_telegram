package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.TruncateAt;
import android.util.LongSparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.voip.VoIPService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.LocationActivity.LocationActivityDelegate;
import org.telegram.ui.VoIPActivity;

public class FragmentContextView
  extends FrameLayout
  implements NotificationCenter.NotificationCenterDelegate
{
  private FragmentContextView additionalContextView;
  private AnimatorSet animatorSet;
  private Runnable checkLocationRunnable = new Runnable()
  {
    public void run()
    {
      FragmentContextView.this.checkLocationString();
      AndroidUtilities.runOnUIThread(FragmentContextView.this.checkLocationRunnable, 1000L);
    }
  };
  private ImageView closeButton;
  private int currentStyle = -1;
  private boolean firstLocationsLoaded;
  private BaseFragment fragment;
  private FrameLayout frameLayout;
  private boolean isLocation;
  private int lastLocationSharingCount = -1;
  private MessageObject lastMessageObject;
  private String lastString;
  private boolean loadingSharingCount;
  private ImageView playButton;
  private TextView titleTextView;
  private float topPadding;
  private boolean visible;
  private float yPosition;
  
  public FragmentContextView(Context paramContext, BaseFragment paramBaseFragment, boolean paramBoolean)
  {
    super(paramContext);
    this.fragment = paramBaseFragment;
    this.visible = true;
    this.isLocation = paramBoolean;
    ((ViewGroup)this.fragment.getFragmentView()).setClipToPadding(false);
    setTag(Integer.valueOf(1));
    this.frameLayout = new FrameLayout(paramContext);
    this.frameLayout.setWillNotDraw(false);
    addView(this.frameLayout, LayoutHelper.createFrame(-1, 36.0F, 51, 0.0F, 0.0F, 0.0F, 0.0F));
    paramBaseFragment = new View(paramContext);
    paramBaseFragment.setBackgroundResource(2131165342);
    addView(paramBaseFragment, LayoutHelper.createFrame(-1, 3.0F, 51, 0.0F, 36.0F, 0.0F, 0.0F));
    this.playButton = new ImageView(paramContext);
    this.playButton.setScaleType(ImageView.ScaleType.CENTER);
    this.playButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("inappPlayerPlayPause"), PorterDuff.Mode.MULTIPLY));
    addView(this.playButton, LayoutHelper.createFrame(36, 36.0F, 51, 0.0F, 0.0F, 0.0F, 0.0F));
    this.playButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (FragmentContextView.this.currentStyle == 0)
        {
          if (MediaController.getInstance().isMessagePaused()) {
            MediaController.getInstance().playMessage(MediaController.getInstance().getPlayingMessageObject());
          }
        }
        else {
          return;
        }
        MediaController.getInstance().pauseMessage(MediaController.getInstance().getPlayingMessageObject());
      }
    });
    this.titleTextView = new TextView(paramContext);
    this.titleTextView.setMaxLines(1);
    this.titleTextView.setLines(1);
    this.titleTextView.setSingleLine(true);
    this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.titleTextView.setTextSize(1, 15.0F);
    this.titleTextView.setGravity(19);
    addView(this.titleTextView, LayoutHelper.createFrame(-1, 36.0F, 51, 35.0F, 0.0F, 36.0F, 0.0F));
    this.closeButton = new ImageView(paramContext);
    this.closeButton.setImageResource(2131165504);
    this.closeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("inappPlayerClose"), PorterDuff.Mode.MULTIPLY));
    this.closeButton.setScaleType(ImageView.ScaleType.CENTER);
    addView(this.closeButton, LayoutHelper.createFrame(36, 36, 53));
    this.closeButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (FragmentContextView.this.currentStyle == 2)
        {
          paramAnonymousView = new AlertDialog.Builder(FragmentContextView.this.fragment.getParentActivity());
          paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
          if ((FragmentContextView.this.fragment instanceof DialogsActivity)) {
            paramAnonymousView.setMessage(LocaleController.getString("StopLiveLocationAlertAll", 2131494439));
          }
          for (;;)
          {
            paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                if ((FragmentContextView.this.fragment instanceof DialogsActivity))
                {
                  paramAnonymous2Int = 0;
                  while (paramAnonymous2Int < 3)
                  {
                    LocationController.getInstance(paramAnonymous2Int).removeAllLocationSharings();
                    paramAnonymous2Int += 1;
                  }
                }
                LocationController.getInstance(FragmentContextView.this.fragment.getCurrentAccount()).removeSharingLocation(((ChatActivity)FragmentContextView.this.fragment).getDialogId());
              }
            });
            paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            paramAnonymousView.show();
            return;
            Object localObject = (ChatActivity)FragmentContextView.this.fragment;
            TLRPC.Chat localChat = ((ChatActivity)localObject).getCurrentChat();
            localObject = ((ChatActivity)localObject).getCurrentUser();
            if (localChat != null) {
              paramAnonymousView.setMessage(LocaleController.formatString("StopLiveLocationAlertToGroup", 2131494440, new Object[] { localChat.title }));
            } else if (localObject != null) {
              paramAnonymousView.setMessage(LocaleController.formatString("StopLiveLocationAlertToUser", 2131494441, new Object[] { UserObject.getFirstName((TLRPC.User)localObject) }));
            } else {
              paramAnonymousView.setMessage(LocaleController.getString("AreYouSure", 2131492998));
            }
          }
        }
        MediaController.getInstance().cleanupPlayer(true, true);
      }
    });
    setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        if (FragmentContextView.this.currentStyle == 0)
        {
          paramAnonymousView = MediaController.getInstance().getPlayingMessageObject();
          if ((FragmentContextView.this.fragment != null) && (paramAnonymousView != null))
          {
            if (!paramAnonymousView.isMusic()) {
              break label64;
            }
            FragmentContextView.this.fragment.showDialog(new AudioPlayerAlert(FragmentContextView.this.getContext()));
          }
        }
        label64:
        long l1;
        int i;
        int j;
        do
        {
          return;
          l1 = 0L;
          if ((FragmentContextView.this.fragment instanceof ChatActivity)) {
            l1 = ((ChatActivity)FragmentContextView.this.fragment).getDialogId();
          }
          if (paramAnonymousView.getDialogId() == l1)
          {
            ((ChatActivity)FragmentContextView.this.fragment).scrollToMessageId(paramAnonymousView.getId(), 0, false, 0, true);
            return;
          }
          l1 = paramAnonymousView.getDialogId();
          Bundle localBundle = new Bundle();
          i = (int)l1;
          j = (int)(l1 >> 32);
          if (i != 0) {
            if (j == 1) {
              localBundle.putInt("chat_id", i);
            }
          }
          for (;;)
          {
            localBundle.putInt("message_id", paramAnonymousView.getId());
            FragmentContextView.this.fragment.presentFragment(new ChatActivity(localBundle), FragmentContextView.this.fragment instanceof ChatActivity);
            return;
            if (i > 0)
            {
              localBundle.putInt("user_id", i);
            }
            else if (i < 0)
            {
              localBundle.putInt("chat_id", -i);
              continue;
              localBundle.putInt("enc_id", j);
            }
          }
          if (FragmentContextView.this.currentStyle == 1)
          {
            paramAnonymousView = new Intent(FragmentContextView.this.getContext(), VoIPActivity.class);
            paramAnonymousView.addFlags(805306368);
            FragmentContextView.this.getContext().startActivity(paramAnonymousView);
            return;
          }
        } while (FragmentContextView.this.currentStyle != 2);
        long l2 = 0L;
        int k = UserConfig.selectedAccount;
        if ((FragmentContextView.this.fragment instanceof ChatActivity))
        {
          l1 = ((ChatActivity)FragmentContextView.this.fragment).getDialogId();
          i = FragmentContextView.this.fragment.getCurrentAccount();
        }
        while (l1 != 0L)
        {
          FragmentContextView.this.openSharingLocation(LocationController.getInstance(i).getSharingLocationInfo(l1));
          return;
          if (LocationController.getLocationsCount() == 1)
          {
            j = 0;
            for (;;)
            {
              i = k;
              l1 = l2;
              if (j >= 3) {
                break;
              }
              if (!LocationController.getInstance(j).sharingLocationsUI.isEmpty())
              {
                paramAnonymousView = (LocationController.SharingLocationInfo)LocationController.getInstance(j).sharingLocationsUI.get(0);
                l1 = paramAnonymousView.did;
                i = paramAnonymousView.messageObject.currentAccount;
                break;
              }
              j += 1;
            }
          }
          l1 = 0L;
          i = k;
        }
        FragmentContextView.this.fragment.showDialog(new SharingLocationsAlert(FragmentContextView.this.getContext(), new SharingLocationsAlert.SharingLocationsAlertDelegate()
        {
          public void didSelectLocation(LocationController.SharingLocationInfo paramAnonymous2SharingLocationInfo)
          {
            FragmentContextView.this.openSharingLocation(paramAnonymous2SharingLocationInfo);
          }
        }));
      }
    });
  }
  
  private void checkCall(boolean paramBoolean)
  {
    View localView = this.fragment.getFragmentView();
    boolean bool = paramBoolean;
    if (!paramBoolean)
    {
      bool = paramBoolean;
      if (localView != null) {
        if (localView.getParent() != null)
        {
          bool = paramBoolean;
          if (((View)localView.getParent()).getVisibility() == 0) {}
        }
        else
        {
          bool = true;
        }
      }
    }
    int i;
    if ((VoIPService.getSharedInstance() != null) && (VoIPService.getSharedInstance().getCallState() != 15))
    {
      i = 1;
      if (i != 0) {
        break label228;
      }
      if (this.visible)
      {
        this.visible = false;
        if (!bool) {
          break label113;
        }
        if (getVisibility() != 8) {
          setVisibility(8);
        }
        setTopPadding(0.0F);
      }
    }
    label113:
    label228:
    do
    {
      return;
      i = 0;
      break;
      if (this.animatorSet != null)
      {
        this.animatorSet.cancel();
        this.animatorSet = null;
      }
      this.animatorSet = new AnimatorSet();
      this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F) }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { 0.0F }) });
      this.animatorSet.setDuration(200L);
      this.animatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator)))
          {
            FragmentContextView.this.setVisibility(8);
            FragmentContextView.access$502(FragmentContextView.this, null);
          }
        }
      });
      this.animatorSet.start();
      return;
      updateStyle(1);
      if ((bool) && (this.topPadding == 0.0F))
      {
        setTopPadding(AndroidUtilities.dp2(36.0F));
        if ((this.additionalContextView == null) || (this.additionalContextView.getVisibility() != 0)) {
          break label479;
        }
        ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(72.0F));
        setTranslationY(0.0F);
        this.yPosition = 0.0F;
      }
    } while (this.visible);
    if (!bool)
    {
      if (this.animatorSet != null)
      {
        this.animatorSet.cancel();
        this.animatorSet = null;
      }
      this.animatorSet = new AnimatorSet();
      if ((this.additionalContextView == null) || (this.additionalContextView.getVisibility() != 0)) {
        break label498;
      }
    }
    label479:
    label498:
    for (((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(72.0F));; ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0F)))
    {
      this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F), 0.0F }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { AndroidUtilities.dp2(36.0F) }) });
      this.animatorSet.setDuration(200L);
      this.animatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator))) {
            FragmentContextView.access$502(FragmentContextView.this, null);
          }
        }
      });
      this.animatorSet.start();
      this.visible = true;
      setVisibility(0);
      return;
      ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0F));
      break;
    }
  }
  
  private void checkLiveLocation(boolean paramBoolean)
  {
    Object localObject = this.fragment.getFragmentView();
    boolean bool = paramBoolean;
    if (!paramBoolean)
    {
      bool = paramBoolean;
      if (localObject != null) {
        if (((View)localObject).getParent() != null)
        {
          bool = paramBoolean;
          if (((View)((View)localObject).getParent()).getVisibility() == 0) {}
        }
        else
        {
          bool = true;
        }
      }
    }
    if ((this.fragment instanceof DialogsActivity)) {
      if (LocationController.getLocationsCount() != 0) {
        paramBoolean = true;
      }
    }
    while (!paramBoolean)
    {
      this.lastLocationSharingCount = -1;
      AndroidUtilities.cancelRunOnUIThread(this.checkLocationRunnable);
      if (this.visible)
      {
        this.visible = false;
        if (bool)
        {
          if (getVisibility() != 8) {
            setVisibility(8);
          }
          setTopPadding(0.0F);
        }
      }
      else
      {
        return;
        paramBoolean = false;
        continue;
        paramBoolean = LocationController.getInstance(this.fragment.getCurrentAccount()).isSharingLocation(((ChatActivity)this.fragment).getDialogId());
        continue;
      }
      if (this.animatorSet != null)
      {
        this.animatorSet.cancel();
        this.animatorSet = null;
      }
      this.animatorSet = new AnimatorSet();
      this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F) }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { 0.0F }) });
      this.animatorSet.setDuration(200L);
      this.animatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator)))
          {
            FragmentContextView.this.setVisibility(8);
            FragmentContextView.access$502(FragmentContextView.this, null);
          }
        }
      });
      this.animatorSet.start();
      return;
    }
    updateStyle(2);
    this.playButton.setImageDrawable(new ShareLocationDrawable(getContext(), true));
    if ((bool) && (this.topPadding == 0.0F))
    {
      setTopPadding(AndroidUtilities.dp2(36.0F));
      setTranslationY(0.0F);
      this.yPosition = 0.0F;
    }
    if (!this.visible)
    {
      if (!bool)
      {
        if (this.animatorSet != null)
        {
          this.animatorSet.cancel();
          this.animatorSet = null;
        }
        this.animatorSet = new AnimatorSet();
        this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F), 0.0F }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { AndroidUtilities.dp2(36.0F) }) });
        this.animatorSet.setDuration(200L);
        this.animatorSet.addListener(new AnimatorListenerAdapter()
        {
          public void onAnimationEnd(Animator paramAnonymousAnimator)
          {
            if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator))) {
              FragmentContextView.access$502(FragmentContextView.this, null);
            }
          }
        });
        this.animatorSet.start();
      }
      this.visible = true;
      setVisibility(0);
    }
    if ((this.fragment instanceof DialogsActivity))
    {
      String str = LocaleController.getString("AttachLiveLocation", 2131493031);
      localObject = new ArrayList();
      int i = 0;
      while (i < 3)
      {
        ((ArrayList)localObject).addAll(LocationController.getInstance(i).sharingLocationsUI);
        i += 1;
      }
      if (((ArrayList)localObject).size() == 1)
      {
        localObject = (LocationController.SharingLocationInfo)((ArrayList)localObject).get(0);
        i = (int)((LocationController.SharingLocationInfo)localObject).messageObject.getDialogId();
        if (i > 0) {
          localObject = UserObject.getFirstName(MessagesController.getInstance(((LocationController.SharingLocationInfo)localObject).messageObject.currentAccount).getUser(Integer.valueOf(i)));
        }
      }
      for (;;)
      {
        localObject = String.format(LocaleController.getString("AttachLiveLocationIsSharing", 2131493032), new Object[] { str, localObject });
        i = ((String)localObject).indexOf(str);
        localObject = new SpannableStringBuilder((CharSequence)localObject);
        this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        ((SpannableStringBuilder)localObject).setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor("inappPlayerPerformer")), i, str.length() + i, 18);
        this.titleTextView.setText((CharSequence)localObject);
        return;
        localObject = MessagesController.getInstance(((LocationController.SharingLocationInfo)localObject).messageObject.currentAccount).getChat(Integer.valueOf(-i));
        if (localObject != null)
        {
          localObject = ((TLRPC.Chat)localObject).title;
        }
        else
        {
          localObject = "";
          continue;
          localObject = LocaleController.formatPluralString("Chats", ((ArrayList)localObject).size());
        }
      }
    }
    this.checkLocationRunnable.run();
    checkLocationString();
  }
  
  private void checkLocationString()
  {
    if ((!(this.fragment instanceof ChatActivity)) || (this.titleTextView == null)) {}
    for (;;)
    {
      return;
      Object localObject1 = (ChatActivity)this.fragment;
      long l = ((ChatActivity)localObject1).getDialogId();
      int m = ((ChatActivity)localObject1).getCurrentAccount();
      Object localObject3 = (ArrayList)LocationController.getInstance(m).locationsCache.get(l);
      if (!this.firstLocationsLoaded)
      {
        LocationController.getInstance(m).loadLiveLocations(l);
        this.firstLocationsLoaded = true;
      }
      int k = 0;
      int i = 0;
      Object localObject2 = null;
      localObject1 = null;
      if (localObject3 != null)
      {
        int n = UserConfig.getInstance(m).getClientUserId();
        int i1 = ConnectionsManager.getInstance(m).getCurrentTime();
        int j = 0;
        k = i;
        localObject2 = localObject1;
        if (j < ((ArrayList)localObject3).size())
        {
          TLRPC.Message localMessage = (TLRPC.Message)((ArrayList)localObject3).get(j);
          if (localMessage.media == null)
          {
            localObject2 = localObject1;
            k = i;
          }
          for (;;)
          {
            j += 1;
            i = k;
            localObject1 = localObject2;
            break;
            k = i;
            localObject2 = localObject1;
            if (localMessage.date + localMessage.media.period > i1)
            {
              localObject2 = localObject1;
              if (localObject1 == null)
              {
                localObject2 = localObject1;
                if (localMessage.from_id != n) {
                  localObject2 = MessagesController.getInstance(m).getUser(Integer.valueOf(localMessage.from_id));
                }
              }
              k = i + 1;
            }
          }
        }
      }
      if (this.lastLocationSharingCount != k)
      {
        this.lastLocationSharingCount = k;
        localObject3 = LocaleController.getString("AttachLiveLocation", 2131493031);
        if (k == 0) {
          localObject1 = localObject3;
        }
        while ((this.lastString == null) || (!((String)localObject1).equals(this.lastString)))
        {
          this.lastString = ((String)localObject1);
          i = ((String)localObject1).indexOf((String)localObject3);
          localObject1 = new SpannableStringBuilder((CharSequence)localObject1);
          this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
          if (i >= 0) {
            ((SpannableStringBuilder)localObject1).setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor("inappPlayerPerformer")), i, ((String)localObject3).length() + i, 18);
          }
          this.titleTextView.setText((CharSequence)localObject1);
          return;
          i = k - 1;
          if (LocationController.getInstance(m).isSharingLocation(l))
          {
            if (i != 0)
            {
              if ((i == 1) && (localObject2 != null)) {
                localObject1 = String.format("%1$s - %2$s", new Object[] { localObject3, LocaleController.formatString("SharingYouAndOtherName", 2131494399, new Object[] { UserObject.getFirstName((TLRPC.User)localObject2) }) });
              } else {
                localObject1 = String.format("%1$s - %2$s %3$s", new Object[] { localObject3, LocaleController.getString("ChatYourSelfName", 2131493238), LocaleController.formatPluralString("AndOther", i) });
              }
            }
            else {
              localObject1 = String.format("%1$s - %2$s", new Object[] { localObject3, LocaleController.getString("ChatYourSelfName", 2131493238) });
            }
          }
          else if (i != 0) {
            localObject1 = String.format("%1$s - %2$s %3$s", new Object[] { localObject3, UserObject.getFirstName((TLRPC.User)localObject2), LocaleController.formatPluralString("AndOther", i) });
          } else {
            localObject1 = String.format("%1$s - %2$s", new Object[] { localObject3, UserObject.getFirstName((TLRPC.User)localObject2) });
          }
        }
      }
    }
  }
  
  private void checkPlayer(boolean paramBoolean)
  {
    MessageObject localMessageObject = MediaController.getInstance().getPlayingMessageObject();
    Object localObject = this.fragment.getFragmentView();
    boolean bool = paramBoolean;
    if (!paramBoolean)
    {
      bool = paramBoolean;
      if (localObject != null) {
        if (((View)localObject).getParent() != null)
        {
          bool = paramBoolean;
          if (((View)((View)localObject).getParent()).getVisibility() == 0) {}
        }
        else
        {
          bool = true;
        }
      }
    }
    if ((localMessageObject == null) || (localMessageObject.getId() == 0))
    {
      this.lastMessageObject = null;
      if ((VoIPService.getSharedInstance() != null) && (VoIPService.getSharedInstance().getCallState() != 15))
      {
        i = 1;
        if (i == 0) {
          break label108;
        }
        checkCall(false);
      }
      label108:
      while (!this.visible)
      {
        return;
        i = 0;
        break;
      }
      this.visible = false;
      if (bool)
      {
        if (getVisibility() != 8) {
          setVisibility(8);
        }
        setTopPadding(0.0F);
        return;
      }
      if (this.animatorSet != null)
      {
        this.animatorSet.cancel();
        this.animatorSet = null;
      }
      this.animatorSet = new AnimatorSet();
      this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F) }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { 0.0F }) });
      this.animatorSet.setDuration(200L);
      this.animatorSet.addListener(new AnimatorListenerAdapter()
      {
        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator)))
          {
            FragmentContextView.this.setVisibility(8);
            FragmentContextView.access$502(FragmentContextView.this, null);
          }
        }
      });
      this.animatorSet.start();
      return;
    }
    int i = this.currentStyle;
    updateStyle(0);
    if ((bool) && (this.topPadding == 0.0F))
    {
      setTopPadding(AndroidUtilities.dp2(36.0F));
      if ((this.additionalContextView != null) && (this.additionalContextView.getVisibility() == 0))
      {
        ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(72.0F));
        label327:
        setTranslationY(0.0F);
        this.yPosition = 0.0F;
      }
    }
    else
    {
      if (!this.visible)
      {
        if (!bool)
        {
          if (this.animatorSet != null)
          {
            this.animatorSet.cancel();
            this.animatorSet = null;
          }
          this.animatorSet = new AnimatorSet();
          if ((this.additionalContextView == null) || (this.additionalContextView.getVisibility() != 0)) {
            break label683;
          }
          ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(72.0F));
          label412:
          this.animatorSet.playTogether(new Animator[] { ObjectAnimator.ofFloat(this, "translationY", new float[] { -AndroidUtilities.dp2(36.0F), 0.0F }), ObjectAnimator.ofFloat(this, "topPadding", new float[] { AndroidUtilities.dp2(36.0F) }) });
          this.animatorSet.setDuration(200L);
          this.animatorSet.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if ((FragmentContextView.this.animatorSet != null) && (FragmentContextView.this.animatorSet.equals(paramAnonymousAnimator))) {
                FragmentContextView.access$502(FragmentContextView.this, null);
              }
            }
          });
          this.animatorSet.start();
        }
        this.visible = true;
        setVisibility(0);
      }
      if (!MediaController.getInstance().isMessagePaused()) {
        break label702;
      }
      this.playButton.setImageResource(2131165506);
      label534:
      if ((this.lastMessageObject == localMessageObject) && (i == 0)) {
        break label713;
      }
      this.lastMessageObject = localMessageObject;
      if ((!this.lastMessageObject.isVoice()) && (!this.lastMessageObject.isRoundVideo())) {
        break label715;
      }
      localObject = new SpannableStringBuilder(String.format("%s %s", new Object[] { localMessageObject.getMusicAuthor(), localMessageObject.getMusicTitle() }));
      this.titleTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
    }
    for (;;)
    {
      ((SpannableStringBuilder)localObject).setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf"), 0, Theme.getColor("inappPlayerPerformer")), 0, localMessageObject.getMusicAuthor().length(), 18);
      this.titleTextView.setText((CharSequence)localObject);
      return;
      ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0F));
      break label327;
      label683:
      ((FrameLayout.LayoutParams)getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0F));
      break label412;
      label702:
      this.playButton.setImageResource(2131165505);
      break label534;
      label713:
      break;
      label715:
      localObject = new SpannableStringBuilder(String.format("%s - %s", new Object[] { localMessageObject.getMusicAuthor(), localMessageObject.getMusicTitle() }));
      this.titleTextView.setEllipsize(TextUtils.TruncateAt.END);
    }
  }
  
  private void checkVisibility()
  {
    int i = 0;
    boolean bool2 = false;
    boolean bool1;
    if (this.isLocation) {
      if ((this.fragment instanceof DialogsActivity)) {
        if (LocationController.getLocationsCount() != 0)
        {
          bool1 = true;
          if (!bool1) {
            break label123;
          }
        }
      }
    }
    for (;;)
    {
      setVisibility(i);
      return;
      bool1 = false;
      break;
      bool1 = LocationController.getInstance(this.fragment.getCurrentAccount()).isSharingLocation(((ChatActivity)this.fragment).getDialogId());
      break;
      if ((VoIPService.getSharedInstance() != null) && (VoIPService.getSharedInstance().getCallState() != 15))
      {
        bool1 = true;
        break;
      }
      MessageObject localMessageObject = MediaController.getInstance().getPlayingMessageObject();
      bool1 = bool2;
      if (localMessageObject == null) {
        break;
      }
      bool1 = bool2;
      if (localMessageObject.getId() == 0) {
        break;
      }
      bool1 = true;
      break;
      label123:
      i = 8;
    }
  }
  
  private void openSharingLocation(final LocationController.SharingLocationInfo paramSharingLocationInfo)
  {
    if ((paramSharingLocationInfo == null) || (this.fragment.getParentActivity() == null)) {
      return;
    }
    LaunchActivity localLaunchActivity = (LaunchActivity)this.fragment.getParentActivity();
    localLaunchActivity.switchToAccount(paramSharingLocationInfo.messageObject.currentAccount, true);
    LocationActivity localLocationActivity = new LocationActivity(2);
    localLocationActivity.setMessageObject(paramSharingLocationInfo.messageObject);
    localLocationActivity.setDelegate(new LocationActivity.LocationActivityDelegate()
    {
      public void didSelectLocation(TLRPC.MessageMedia paramAnonymousMessageMedia, int paramAnonymousInt)
      {
        SendMessagesHelper.getInstance(paramSharingLocationInfo.messageObject.currentAccount).sendMessage(paramAnonymousMessageMedia, this.val$dialog_id, null, null, null);
      }
    });
    localLaunchActivity.presentFragment(localLocationActivity);
  }
  
  private void updateStyle(int paramInt)
  {
    if (this.currentStyle == paramInt) {}
    do
    {
      do
      {
        return;
        this.currentStyle = paramInt;
        if ((paramInt != 0) && (paramInt != 2)) {
          break;
        }
        this.frameLayout.setBackgroundColor(Theme.getColor("inappPlayerBackground"));
        this.frameLayout.setTag("inappPlayerBackground");
        this.titleTextView.setTextColor(Theme.getColor("inappPlayerTitle"));
        this.titleTextView.setTag("inappPlayerTitle");
        this.closeButton.setVisibility(0);
        this.playButton.setVisibility(0);
        this.titleTextView.setTypeface(Typeface.DEFAULT);
        this.titleTextView.setTextSize(1, 15.0F);
        this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0F, 51, 35.0F, 0.0F, 36.0F, 0.0F));
        if (paramInt == 0)
        {
          this.playButton.setLayoutParams(LayoutHelper.createFrame(36, 36.0F, 51, 0.0F, 0.0F, 0.0F, 0.0F));
          this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0F, 51, 35.0F, 0.0F, 36.0F, 0.0F));
          return;
        }
      } while (paramInt != 2);
      this.playButton.setLayoutParams(LayoutHelper.createFrame(36, 36.0F, 51, 8.0F, 0.0F, 0.0F, 0.0F));
      this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-1, 36.0F, 51, 51.0F, 0.0F, 36.0F, 0.0F));
      return;
    } while (paramInt != 1);
    this.titleTextView.setText(LocaleController.getString("ReturnToCall", 2131494274));
    this.frameLayout.setBackgroundColor(Theme.getColor("returnToCallBackground"));
    this.frameLayout.setTag("returnToCallBackground");
    this.titleTextView.setTextColor(Theme.getColor("returnToCallText"));
    this.titleTextView.setTag("returnToCallText");
    this.closeButton.setVisibility(8);
    this.playButton.setVisibility(8);
    this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.titleTextView.setTextSize(1, 14.0F);
    this.titleTextView.setLayoutParams(LayoutHelper.createFrame(-2, -2.0F, 17, 0.0F, 0.0F, 0.0F, 2.0F));
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.liveLocationsChanged) {
      checkLiveLocation(false);
    }
    long l;
    do
    {
      do
      {
        return;
        if (paramInt1 != NotificationCenter.liveLocationsCacheChanged) {
          break;
        }
      } while (!(this.fragment instanceof ChatActivity));
      l = ((Long)paramVarArgs[0]).longValue();
    } while (((ChatActivity)this.fragment).getDialogId() != l);
    checkLocationString();
    return;
    if ((paramInt1 == NotificationCenter.messagePlayingDidStarted) || (paramInt1 == NotificationCenter.messagePlayingPlayStateChanged) || (paramInt1 == NotificationCenter.messagePlayingDidReset) || (paramInt1 == NotificationCenter.didEndedCall))
    {
      checkPlayer(false);
      return;
    }
    if (paramInt1 == NotificationCenter.didStartedCall)
    {
      checkCall(false);
      return;
    }
    checkPlayer(false);
  }
  
  protected boolean drawChild(Canvas paramCanvas, View paramView, long paramLong)
  {
    int i = paramCanvas.save();
    if (this.yPosition < 0.0F) {
      paramCanvas.clipRect(0, (int)-this.yPosition, paramView.getMeasuredWidth(), AndroidUtilities.dp2(39.0F));
    }
    boolean bool = super.drawChild(paramCanvas, paramView, paramLong);
    paramCanvas.restoreToCount(i);
    return bool;
  }
  
  public float getTopPadding()
  {
    return this.topPadding;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (this.isLocation)
    {
      NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsChanged);
      NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.liveLocationsCacheChanged);
      if (this.additionalContextView != null) {
        this.additionalContextView.checkVisibility();
      }
      checkLiveLocation(true);
      return;
    }
    int i = 0;
    while (i < 3)
    {
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
      NotificationCenter.getInstance(i).addObserver(this, NotificationCenter.messagePlayingDidStarted);
      i += 1;
    }
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didStartedCall);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didEndedCall);
    if (this.additionalContextView != null) {
      this.additionalContextView.checkVisibility();
    }
    if ((VoIPService.getSharedInstance() != null) && (VoIPService.getSharedInstance().getCallState() != 15))
    {
      checkCall(true);
      return;
    }
    checkPlayer(true);
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.topPadding = 0.0F;
    if (this.isLocation)
    {
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
      NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsCacheChanged);
      return;
    }
    int i = 0;
    while (i < 3)
    {
      NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingDidReset);
      NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
      NotificationCenter.getInstance(i).removeObserver(this, NotificationCenter.messagePlayingDidStarted);
      i += 1;
    }
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didStartedCall);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didEndedCall);
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, AndroidUtilities.dp2(39.0F));
  }
  
  public void setAdditionalContextView(FragmentContextView paramFragmentContextView)
  {
    this.additionalContextView = paramFragmentContextView;
  }
  
  @Keep
  public void setTopPadding(float paramFloat)
  {
    this.topPadding = paramFloat;
    if (this.fragment != null)
    {
      View localView = this.fragment.getFragmentView();
      int j = 0;
      int i = j;
      if (this.additionalContextView != null)
      {
        i = j;
        if (this.additionalContextView.getVisibility() == 0) {
          i = AndroidUtilities.dp(36.0F);
        }
      }
      if (localView != null) {
        localView.setPadding(0, (int)this.topPadding + i, 0, 0);
      }
      if ((this.isLocation) && (this.additionalContextView != null)) {
        ((FrameLayout.LayoutParams)this.additionalContextView.getLayoutParams()).topMargin = (-AndroidUtilities.dp(36.0F) - (int)this.topPadding);
      }
    }
  }
  
  @Keep
  public void setTranslationY(float paramFloat)
  {
    super.setTranslationY(paramFloat);
    this.yPosition = paramFloat;
    invalidate();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/FragmentContextView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */