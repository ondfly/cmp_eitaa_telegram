package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.LayoutParams;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Photo;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_help_getSupport;
import org.telegram.tgnet.TLRPC.TL_help_support;
import org.telegram.tgnet.TLRPC.TL_photos_photo;
import org.telegram.tgnet.TLRPC.TL_photos_uploadProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userFull;
import org.telegram.tgnet.TLRPC.TL_userProfilePhoto;
import org.telegram.tgnet.TLRPC.TL_userProfilePhotoEmpty;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet.BottomSheetCell;
import org.telegram.ui.ActionBar.BottomSheet.Builder;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.AvatarUpdater.AvatarUpdaterDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;
import org.telegram.ui.Components.URLSpanNoUnderline;

public class SettingsActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int edit_name = 1;
  private static final int logout = 2;
  private int askQuestionRow;
  private int autoplayGifsRow;
  private AvatarDrawable avatarDrawable;
  private BackupImageView avatarImage;
  private AvatarUpdater avatarUpdater = new AvatarUpdater();
  private int backgroundRow;
  private int bioRow;
  private int clearLogsRow;
  private int contactsReimportRow;
  private int contactsSectionRow;
  private int contactsSortRow;
  private int customTabsRow;
  private int dataRow;
  private int directShareRow;
  private int dumpCallStatsRow;
  private int emojiRow;
  private int emptyRow;
  private int enableAnimationsRow;
  private int extraHeight;
  private View extraHeightView;
  private int forceTcpInCallsRow;
  private int languageRow;
  private LinearLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private int messagesSectionRow;
  private int messagesSectionRow2;
  private TextView nameTextView;
  private int notificationRow;
  private int numberRow;
  private int numberSectionRow;
  private TextView onlineTextView;
  private int overscrollRow;
  private int privacyPolicyRow;
  private int privacyRow;
  private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider()
  {
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      paramAnonymousInt = 0;
      if (paramAnonymousFileLocation == null) {}
      do
      {
        do
        {
          return null;
          paramAnonymousMessageObject = MessagesController.getInstance(SettingsActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId()));
        } while ((paramAnonymousMessageObject == null) || (paramAnonymousMessageObject.photo == null) || (paramAnonymousMessageObject.photo.photo_big == null));
        paramAnonymousMessageObject = paramAnonymousMessageObject.photo.photo_big;
      } while ((paramAnonymousMessageObject.local_id != paramAnonymousFileLocation.local_id) || (paramAnonymousMessageObject.volume_id != paramAnonymousFileLocation.volume_id) || (paramAnonymousMessageObject.dc_id != paramAnonymousFileLocation.dc_id));
      paramAnonymousMessageObject = new int[2];
      SettingsActivity.this.avatarImage.getLocationInWindow(paramAnonymousMessageObject);
      paramAnonymousFileLocation = new PhotoViewer.PlaceProviderObject();
      paramAnonymousFileLocation.viewX = paramAnonymousMessageObject[0];
      int i = paramAnonymousMessageObject[1];
      if (Build.VERSION.SDK_INT >= 21) {}
      for (;;)
      {
        paramAnonymousFileLocation.viewY = (i - paramAnonymousInt);
        paramAnonymousFileLocation.parentView = SettingsActivity.this.avatarImage;
        paramAnonymousFileLocation.imageReceiver = SettingsActivity.this.avatarImage.getImageReceiver();
        paramAnonymousFileLocation.dialogId = UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId();
        paramAnonymousFileLocation.thumb = paramAnonymousFileLocation.imageReceiver.getBitmapSafe();
        paramAnonymousFileLocation.size = -1;
        paramAnonymousFileLocation.radius = SettingsActivity.this.avatarImage.getImageReceiver().getRoundRadius();
        paramAnonymousFileLocation.scale = SettingsActivity.this.avatarImage.getScaleX();
        return paramAnonymousFileLocation;
        paramAnonymousInt = AndroidUtilities.statusBarHeight;
      }
    }
    
    public void willHidePhotoViewer()
    {
      SettingsActivity.this.avatarImage.getImageReceiver().setVisible(true, true);
    }
  };
  private int raiseToSpeakRow;
  private int rowCount;
  private int saveToGalleryRow;
  private int sendByEnterRow;
  private int sendLogsRow;
  private int settingsSectionRow;
  private int settingsSectionRow2;
  private View shadowView;
  private int stickersRow;
  private int supportSectionRow;
  private int supportSectionRow2;
  private int switchBackendButtonRow;
  private int telegramFaqRow;
  private int textSizeRow;
  private int themeRow;
  private int usernameRow;
  private int versionRow;
  private ImageView writeButton;
  private AnimatorSet writeButtonAnimation;
  
  private void fixLayout()
  {
    if (this.fragmentView == null) {
      return;
    }
    this.fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
    {
      public boolean onPreDraw()
      {
        if (SettingsActivity.this.fragmentView != null)
        {
          SettingsActivity.this.needLayout();
          SettingsActivity.this.fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
        }
        return true;
      }
    });
  }
  
  private void needLayout()
  {
    float f1;
    label135:
    final boolean bool1;
    label169:
    boolean bool2;
    if (this.actionBar.getOccupyStatusBar())
    {
      i = AndroidUtilities.statusBarHeight;
      i += ActionBar.getCurrentActionBarHeight();
      Object localObject;
      if (this.listView != null)
      {
        localObject = (FrameLayout.LayoutParams)this.listView.getLayoutParams();
        if (((FrameLayout.LayoutParams)localObject).topMargin != i)
        {
          ((FrameLayout.LayoutParams)localObject).topMargin = i;
          this.listView.setLayoutParams((ViewGroup.LayoutParams)localObject);
          this.extraHeightView.setTranslationY(i);
        }
      }
      if (this.avatarImage != null)
      {
        f1 = this.extraHeight / AndroidUtilities.dp(88.0F);
        this.extraHeightView.setScaleY(f1);
        this.shadowView.setTranslationY(this.extraHeight + i);
        localObject = this.writeButton;
        if (!this.actionBar.getOccupyStatusBar()) {
          break label624;
        }
        i = AndroidUtilities.statusBarHeight;
        ((ImageView)localObject).setTranslationY(i + ActionBar.getCurrentActionBarHeight() + this.extraHeight - AndroidUtilities.dp(29.5F));
        if (f1 <= 0.2F) {
          break label629;
        }
        bool1 = true;
        if (this.writeButton.getTag() != null) {
          break label635;
        }
        bool2 = true;
        label182:
        if (bool1 != bool2)
        {
          if (!bool1) {
            break label641;
          }
          this.writeButton.setTag(null);
          this.writeButton.setVisibility(0);
          label210:
          if (this.writeButtonAnimation != null)
          {
            localObject = this.writeButtonAnimation;
            this.writeButtonAnimation = null;
            ((AnimatorSet)localObject).cancel();
          }
          this.writeButtonAnimation = new AnimatorSet();
          if (!bool1) {
            break label655;
          }
          this.writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
          this.writeButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 1.0F }), ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 1.0F }) });
          label334:
          this.writeButtonAnimation.setDuration(150L);
          this.writeButtonAnimation.addListener(new AnimatorListenerAdapter()
          {
            public void onAnimationEnd(Animator paramAnonymousAnimator)
            {
              if ((SettingsActivity.this.writeButtonAnimation != null) && (SettingsActivity.this.writeButtonAnimation.equals(paramAnonymousAnimator)))
              {
                paramAnonymousAnimator = SettingsActivity.this.writeButton;
                if (!bool1) {
                  break label56;
                }
              }
              label56:
              for (int i = 0;; i = 8)
              {
                paramAnonymousAnimator.setVisibility(i);
                SettingsActivity.access$7802(SettingsActivity.this, null);
                return;
              }
            }
          });
          this.writeButtonAnimation.start();
        }
        this.avatarImage.setScaleX((42.0F + 18.0F * f1) / 42.0F);
        this.avatarImage.setScaleY((42.0F + 18.0F * f1) / 42.0F);
        if (!this.actionBar.getOccupyStatusBar()) {
          break label747;
        }
      }
    }
    label624:
    label629:
    label635:
    label641:
    label655:
    label747:
    for (int i = AndroidUtilities.statusBarHeight;; i = 0)
    {
      float f2 = i + ActionBar.getCurrentActionBarHeight() / 2.0F * (1.0F + f1) - 21.0F * AndroidUtilities.density + 27.0F * AndroidUtilities.density * f1;
      this.avatarImage.setTranslationX(-AndroidUtilities.dp(47.0F) * f1);
      this.avatarImage.setTranslationY((float)Math.ceil(f2));
      this.nameTextView.setTranslationX(-21.0F * AndroidUtilities.density * f1);
      this.nameTextView.setTranslationY((float)Math.floor(f2) - (float)Math.ceil(AndroidUtilities.density) + (float)Math.floor(7.0F * AndroidUtilities.density * f1));
      this.onlineTextView.setTranslationX(-21.0F * AndroidUtilities.density * f1);
      this.onlineTextView.setTranslationY((float)Math.floor(f2) + AndroidUtilities.dp(22.0F) + (float)Math.floor(11.0F * AndroidUtilities.density) * f1);
      this.nameTextView.setScaleX(1.0F + 0.12F * f1);
      this.nameTextView.setScaleY(1.0F + 0.12F * f1);
      return;
      i = 0;
      break;
      i = 0;
      break label135;
      bool1 = false;
      break label169;
      bool2 = false;
      break label182;
      this.writeButton.setTag(Integer.valueOf(0));
      break label210;
      this.writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
      this.writeButtonAnimation.playTogether(new Animator[] { ObjectAnimator.ofFloat(this.writeButton, "scaleX", new float[] { 0.2F }), ObjectAnimator.ofFloat(this.writeButton, "scaleY", new float[] { 0.2F }), ObjectAnimator.ofFloat(this.writeButton, "alpha", new float[] { 0.0F }) });
      break label334;
    }
  }
  
  private void performAskAQuestion()
  {
    final SharedPreferences localSharedPreferences = MessagesController.getMainSettings(this.currentAccount);
    int i = localSharedPreferences.getInt("support_id", 0);
    final Object localObject1 = null;
    Object localObject2;
    Object localObject3;
    if (i != 0)
    {
      localObject2 = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(i));
      localObject1 = localObject2;
      if (localObject2 == null)
      {
        localObject3 = localSharedPreferences.getString("support_user", null);
        localObject1 = localObject2;
        if (localObject3 == null) {}
      }
    }
    try
    {
      localObject3 = Base64.decode((String)localObject3, 0);
      localObject1 = localObject2;
      if (localObject3 != null)
      {
        localObject3 = new SerializedData((byte[])localObject3);
        localObject2 = TLRPC.User.TLdeserialize((AbstractSerializedData)localObject3, ((SerializedData)localObject3).readInt32(false), false);
        localObject1 = localObject2;
        if (localObject2 != null)
        {
          localObject1 = localObject2;
          if (((TLRPC.User)localObject2).id == 333000) {
            localObject1 = null;
          }
        }
        ((SerializedData)localObject3).cleanup();
      }
    }
    catch (Exception localException)
    {
      TLRPC.User localUser;
      for (;;)
      {
        FileLog.e(localException);
        localUser = null;
      }
      MessagesController.getInstance(this.currentAccount).putUser(localUser, true);
      localObject2 = new Bundle();
      ((Bundle)localObject2).putInt("user_id", localUser.id);
      presentFragment(new ChatActivity((Bundle)localObject2));
    }
    if (localObject1 == null)
    {
      localObject1 = new AlertDialog(getParentActivity(), 1);
      ((AlertDialog)localObject1).setMessage(LocaleController.getString("Loading", 2131493762));
      ((AlertDialog)localObject1).setCanceledOnTouchOutside(false);
      ((AlertDialog)localObject1).setCancelable(false);
      ((AlertDialog)localObject1).show();
      localObject2 = new TLRPC.TL_help_getSupport();
      ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject2, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
        {
          if (paramAnonymousTL_error == null)
          {
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                Object localObject = SettingsActivity.12.this.val$preferences.edit();
                ((SharedPreferences.Editor)localObject).putInt("support_id", paramAnonymousTLObject.user.id);
                SerializedData localSerializedData = new SerializedData();
                paramAnonymousTLObject.user.serializeToStream(localSerializedData);
                ((SharedPreferences.Editor)localObject).putString("support_user", Base64.encodeToString(localSerializedData.toByteArray(), 0));
                ((SharedPreferences.Editor)localObject).commit();
                localSerializedData.cleanup();
                try
                {
                  SettingsActivity.12.this.val$progressDialog.dismiss();
                  localObject = new ArrayList();
                  ((ArrayList)localObject).add(paramAnonymousTLObject.user);
                  MessagesStorage.getInstance(SettingsActivity.this.currentAccount).putUsersAndChats((ArrayList)localObject, null, true, true);
                  MessagesController.getInstance(SettingsActivity.this.currentAccount).putUser(paramAnonymousTLObject.user, false);
                  localObject = new Bundle();
                  ((Bundle)localObject).putInt("user_id", paramAnonymousTLObject.user.id);
                  SettingsActivity.this.presentFragment(new ChatActivity((Bundle)localObject));
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
            return;
          }
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              try
              {
                SettingsActivity.12.this.val$progressDialog.dismiss();
                return;
              }
              catch (Exception localException)
              {
                FileLog.e(localException);
              }
            }
          });
        }
      });
      return;
    }
  }
  
  private void sendLogs()
  {
    for (;;)
    {
      Object localObject;
      int i;
      try
      {
        ArrayList localArrayList = new ArrayList();
        localObject = ApplicationLoader.applicationContext.getExternalFilesDir(null);
        localObject = new File(((File)localObject).getAbsolutePath() + "/logs").listFiles();
        int j = localObject.length;
        i = 0;
        if (i < j)
        {
          File localFile = localObject[i];
          if (Build.VERSION.SDK_INT >= 24) {
            localArrayList.add(FileProvider.getUriForFile(getParentActivity(), "org.telegram.messenger.provider", localFile));
          } else {
            localArrayList.add(Uri.fromFile(localFile));
          }
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        return;
      }
      if (!localException.isEmpty())
      {
        localObject = new Intent("android.intent.action.SEND_MULTIPLE");
        if (Build.VERSION.SDK_INT >= 24) {
          ((Intent)localObject).addFlags(1);
        }
        ((Intent)localObject).setType("message/rfc822");
        ((Intent)localObject).putExtra("android.intent.extra.EMAIL", "");
        ((Intent)localObject).putExtra("android.intent.extra.SUBJECT", "last logs");
        ((Intent)localObject).putParcelableArrayListExtra("android.intent.extra.STREAM", localException);
        getParentActivity().startActivityForResult(Intent.createChooser((Intent)localObject, "Select email application."), 500);
        return;
        i += 1;
      }
    }
  }
  
  private void updateUserData()
  {
    boolean bool2 = true;
    TLRPC.User localUser = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(this.currentAccount).getClientUserId()));
    Object localObject = null;
    TLRPC.FileLocation localFileLocation = null;
    if (localUser.photo != null)
    {
      localObject = localUser.photo.photo_small;
      localFileLocation = localUser.photo.photo_big;
    }
    this.avatarDrawable = new AvatarDrawable(localUser, true);
    this.avatarDrawable.setColor(Theme.getColor("avatar_backgroundInProfileBlue"));
    if (this.avatarImage != null)
    {
      this.avatarImage.setImage((TLObject)localObject, "50_50", this.avatarDrawable);
      localObject = this.avatarImage.getImageReceiver();
      if (PhotoViewer.isShowingImage(localFileLocation)) {
        break label188;
      }
      bool1 = true;
      ((ImageReceiver)localObject).setVisible(bool1, false);
      this.nameTextView.setText(UserObject.getUserName(localUser));
      this.onlineTextView.setText(LocaleController.getString("Online", 2131494030));
      localObject = this.avatarImage.getImageReceiver();
      if (PhotoViewer.isShowingImage(localFileLocation)) {
        break label193;
      }
    }
    label188:
    label193:
    for (boolean bool1 = bool2;; bool1 = false)
    {
      ((ImageReceiver)localObject).setVisible(bool1, false);
      return;
      bool1 = false;
      break;
    }
  }
  
  public View createView(Context paramContext)
  {
    this.actionBar.setBackgroundColor(Theme.getColor("avatar_backgroundActionBarBlue"));
    this.actionBar.setItemsBackgroundColor(Theme.getColor("avatar_actionBarSelectorBlue"), false);
    this.actionBar.setItemsColor(Theme.getColor("avatar_actionBarIconBlue"), false);
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAddToContainer(false);
    this.extraHeight = 88;
    if (AndroidUtilities.isTablet()) {
      this.actionBar.setOccupyStatusBar(false);
    }
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          SettingsActivity.this.finishFragment();
        }
        do
        {
          return;
          if (paramAnonymousInt == 1)
          {
            SettingsActivity.this.presentFragment(new ChangeNameActivity());
            return;
          }
        } while ((paramAnonymousInt != 2) || (SettingsActivity.this.getParentActivity() == null));
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
        localBuilder.setMessage(LocaleController.getString("AreYouSureLogout", 2131493009));
        localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
        localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
          {
            MessagesController.getInstance(SettingsActivity.this.currentAccount).performLogout(true);
          }
        });
        localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
        SettingsActivity.this.showDialog(localBuilder.create());
      }
    });
    Object localObject1 = this.actionBar.createMenu().addItem(0, 2131165353);
    ((ActionBarMenuItem)localObject1).addSubItem(1, LocaleController.getString("EditName", 2131493416));
    ((ActionBarMenuItem)localObject1).addSubItem(2, LocaleController.getString("LogOut", 2131493776));
    this.listAdapter = new ListAdapter(paramContext);
    this.fragmentView = new FrameLayout(paramContext)
    {
      protected boolean drawChild(Canvas paramAnonymousCanvas, View paramAnonymousView, long paramAnonymousLong)
      {
        if (paramAnonymousView == SettingsActivity.this.listView)
        {
          boolean bool = super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
          if (SettingsActivity.this.parentLayout != null)
          {
            int k = 0;
            int m = getChildCount();
            int i = 0;
            int j = k;
            if (i < m)
            {
              View localView = getChildAt(i);
              if (localView == paramAnonymousView) {}
              while ((!(localView instanceof ActionBar)) || (localView.getVisibility() != 0))
              {
                i += 1;
                break;
              }
              j = k;
              if (((ActionBar)localView).getCastShadows()) {
                j = localView.getMeasuredHeight();
              }
            }
            SettingsActivity.this.parentLayout.drawHeaderShadow(paramAnonymousCanvas, j);
          }
          return bool;
        }
        return super.drawChild(paramAnonymousCanvas, paramAnonymousView, paramAnonymousLong);
      }
    };
    this.fragmentView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.listView = new RecyclerListView(paramContext);
    this.listView.setVerticalScrollBarEnabled(false);
    localObject1 = this.listView;
    Object localObject2 = new LinearLayoutManager(paramContext, 1, false)
    {
      public boolean supportsPredictiveItemAnimations()
      {
        return false;
      }
    };
    this.layoutManager = ((LinearLayoutManager)localObject2);
    ((RecyclerListView)localObject1).setLayoutManager((RecyclerView.LayoutManager)localObject2);
    this.listView.setGlowColor(Theme.getColor("avatar_backgroundActionBarBlue"));
    localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1, 51));
    this.listView.setAdapter(this.listAdapter);
    this.listView.setItemAnimator(null);
    this.listView.setLayoutAnimation(null);
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, final int paramAnonymousInt)
      {
        if (paramAnonymousInt == SettingsActivity.this.textSizeRow) {
          if (SettingsActivity.this.getParentActivity() != null) {}
        }
        boolean bool2;
        label234:
        label748:
        label1581:
        label1711:
        label1736:
        label1942:
        do
        {
          do
          {
            do
            {
              int j;
              Object localObject4;
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
                                    do
                                    {
                                      do
                                      {
                                        return;
                                        paramAnonymousView = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
                                        paramAnonymousView.setTitle(LocaleController.getString("TextSize", 2131494481));
                                        localObject1 = new NumberPicker(SettingsActivity.this.getParentActivity());
                                        ((NumberPicker)localObject1).setMinValue(12);
                                        ((NumberPicker)localObject1).setMaxValue(30);
                                        ((NumberPicker)localObject1).setValue(SharedConfig.fontSize);
                                        paramAnonymousView.setView((View)localObject1);
                                        paramAnonymousView.setNegativeButton(LocaleController.getString("Done", 2131493395), new DialogInterface.OnClickListener()
                                        {
                                          public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                                          {
                                            paramAnonymous2DialogInterface = MessagesController.getGlobalMainSettings().edit();
                                            paramAnonymous2DialogInterface.putInt("fons_size", localObject1.getValue());
                                            SharedConfig.fontSize = localObject1.getValue();
                                            paramAnonymous2DialogInterface.commit();
                                            if (SettingsActivity.this.listAdapter != null) {
                                              SettingsActivity.this.listAdapter.notifyItemChanged(paramAnonymousInt);
                                            }
                                          }
                                        });
                                        SettingsActivity.this.showDialog(paramAnonymousView.create());
                                        return;
                                        if (paramAnonymousInt != SettingsActivity.this.enableAnimationsRow) {
                                          break label234;
                                        }
                                        localObject1 = MessagesController.getGlobalMainSettings();
                                        bool2 = ((SharedPreferences)localObject1).getBoolean("view_animations", true);
                                        localObject1 = ((SharedPreferences)localObject1).edit();
                                        if (bool2) {
                                          break;
                                        }
                                        bool1 = true;
                                        ((SharedPreferences.Editor)localObject1).putBoolean("view_animations", bool1);
                                        ((SharedPreferences.Editor)localObject1).commit();
                                      } while (!(paramAnonymousView instanceof TextCheckCell));
                                      paramAnonymousView = (TextCheckCell)paramAnonymousView;
                                      if (!bool2) {}
                                      for (bool1 = true;; bool1 = false)
                                      {
                                        paramAnonymousView.setChecked(bool1);
                                        return;
                                        bool1 = false;
                                        break;
                                      }
                                      if (paramAnonymousInt == SettingsActivity.this.notificationRow)
                                      {
                                        SettingsActivity.this.presentFragment(new NotificationsSettingsActivity());
                                        return;
                                      }
                                      if (paramAnonymousInt == SettingsActivity.this.backgroundRow)
                                      {
                                        SettingsActivity.this.presentFragment(new WallpapersActivity());
                                        return;
                                      }
                                      if (paramAnonymousInt != SettingsActivity.this.askQuestionRow) {
                                        break;
                                      }
                                    } while (SettingsActivity.this.getParentActivity() == null);
                                    paramAnonymousView = new TextView(SettingsActivity.this.getParentActivity());
                                    localObject1 = new SpannableString(Html.fromHtml(LocaleController.getString("AskAQuestionInfo", 2131493021).replace("\n", "<br>")));
                                    localObject2 = (URLSpan[])((Spannable)localObject1).getSpans(0, ((Spannable)localObject1).length(), URLSpan.class);
                                    paramAnonymousInt = 0;
                                    while (paramAnonymousInt < localObject2.length)
                                    {
                                      localObject3 = localObject2[paramAnonymousInt];
                                      i = ((Spannable)localObject1).getSpanStart(localObject3);
                                      j = ((Spannable)localObject1).getSpanEnd(localObject3);
                                      ((Spannable)localObject1).removeSpan(localObject3);
                                      ((Spannable)localObject1).setSpan(new URLSpanNoUnderline(((URLSpan)localObject3).getURL())
                                      {
                                        public void onClick(View paramAnonymous2View)
                                        {
                                          SettingsActivity.this.dismissCurrentDialig();
                                          super.onClick(paramAnonymous2View);
                                        }
                                      }, i, j, 0);
                                      paramAnonymousInt += 1;
                                    }
                                    paramAnonymousView.setText((CharSequence)localObject1);
                                    paramAnonymousView.setTextSize(1, 16.0F);
                                    paramAnonymousView.setLinkTextColor(Theme.getColor("dialogTextLink"));
                                    paramAnonymousView.setHighlightColor(Theme.getColor("dialogLinkSelection"));
                                    paramAnonymousView.setPadding(AndroidUtilities.dp(23.0F), 0, AndroidUtilities.dp(23.0F), 0);
                                    paramAnonymousView.setMovementMethod(new SettingsActivity.LinkMovementMethodMy(null));
                                    paramAnonymousView.setTextColor(Theme.getColor("dialogTextBlack"));
                                    localObject1 = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
                                    ((AlertDialog.Builder)localObject1).setView(paramAnonymousView);
                                    ((AlertDialog.Builder)localObject1).setTitle(LocaleController.getString("AskAQuestion", 2131493020));
                                    ((AlertDialog.Builder)localObject1).setPositiveButton(LocaleController.getString("AskButton", 2131493022), new DialogInterface.OnClickListener()
                                    {
                                      public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                                      {
                                        SettingsActivity.this.performAskAQuestion();
                                      }
                                    });
                                    ((AlertDialog.Builder)localObject1).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                                    SettingsActivity.this.showDialog(((AlertDialog.Builder)localObject1).create());
                                    return;
                                    if (paramAnonymousInt == SettingsActivity.this.sendLogsRow)
                                    {
                                      SettingsActivity.this.sendLogs();
                                      return;
                                    }
                                    if (paramAnonymousInt == SettingsActivity.this.clearLogsRow)
                                    {
                                      FileLog.cleanupLogs();
                                      return;
                                    }
                                    if (paramAnonymousInt != SettingsActivity.this.sendByEnterRow) {
                                      break label748;
                                    }
                                    localObject1 = MessagesController.getGlobalMainSettings();
                                    bool2 = ((SharedPreferences)localObject1).getBoolean("send_by_enter", false);
                                    localObject1 = ((SharedPreferences)localObject1).edit();
                                    if (bool2) {
                                      break;
                                    }
                                    bool1 = true;
                                    ((SharedPreferences.Editor)localObject1).putBoolean("send_by_enter", bool1);
                                    ((SharedPreferences.Editor)localObject1).commit();
                                  } while (!(paramAnonymousView instanceof TextCheckCell));
                                  paramAnonymousView = (TextCheckCell)paramAnonymousView;
                                  if (!bool2) {}
                                  for (bool1 = true;; bool1 = false)
                                  {
                                    paramAnonymousView.setChecked(bool1);
                                    return;
                                    bool1 = false;
                                    break;
                                  }
                                  if (paramAnonymousInt != SettingsActivity.this.raiseToSpeakRow) {
                                    break;
                                  }
                                  SharedConfig.toogleRaiseToSpeak();
                                } while (!(paramAnonymousView instanceof TextCheckCell));
                                ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.raiseToSpeak);
                                return;
                                if (paramAnonymousInt != SettingsActivity.this.autoplayGifsRow) {
                                  break;
                                }
                                SharedConfig.toggleAutoplayGifs();
                              } while (!(paramAnonymousView instanceof TextCheckCell));
                              ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.autoplayGifs);
                              return;
                              if (paramAnonymousInt != SettingsActivity.this.saveToGalleryRow) {
                                break;
                              }
                              SharedConfig.toggleSaveToGallery();
                            } while (!(paramAnonymousView instanceof TextCheckCell));
                            ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.saveToGallery);
                            return;
                            if (paramAnonymousInt != SettingsActivity.this.customTabsRow) {
                              break;
                            }
                            SharedConfig.toggleCustomTabs();
                          } while (!(paramAnonymousView instanceof TextCheckCell));
                          ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.customTabs);
                          return;
                          if (paramAnonymousInt != SettingsActivity.this.directShareRow) {
                            break;
                          }
                          SharedConfig.toggleDirectShare();
                        } while (!(paramAnonymousView instanceof TextCheckCell));
                        ((TextCheckCell)paramAnonymousView).setChecked(SharedConfig.directShare);
                        return;
                        if (paramAnonymousInt == SettingsActivity.this.privacyRow)
                        {
                          SettingsActivity.this.presentFragment(new PrivacySettingsActivity());
                          return;
                        }
                        if (paramAnonymousInt == SettingsActivity.this.dataRow)
                        {
                          SettingsActivity.this.presentFragment(new DataSettingsActivity());
                          return;
                        }
                        if (paramAnonymousInt == SettingsActivity.this.languageRow)
                        {
                          SettingsActivity.this.presentFragment(new LanguageSelectActivity());
                          return;
                        }
                        if (paramAnonymousInt == SettingsActivity.this.themeRow)
                        {
                          SettingsActivity.this.presentFragment(new ThemeActivity(0));
                          return;
                        }
                        if (paramAnonymousInt != SettingsActivity.this.switchBackendButtonRow) {
                          break;
                        }
                      } while (SettingsActivity.this.getParentActivity() == null);
                      paramAnonymousView = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
                      paramAnonymousView.setMessage(LocaleController.getString("AreYouSure", 2131492998));
                      paramAnonymousView.setTitle(LocaleController.getString("AppName", 2131492981));
                      paramAnonymousView.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                      {
                        public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                        {
                          SharedConfig.pushAuthKey = null;
                          SharedConfig.pushAuthKeyId = null;
                          SharedConfig.saveConfig();
                          ConnectionsManager.getInstance(SettingsActivity.this.currentAccount).switchBackend();
                        }
                      });
                      paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                      SettingsActivity.this.showDialog(paramAnonymousView.create());
                      return;
                      if (paramAnonymousInt == SettingsActivity.this.telegramFaqRow)
                      {
                        Browser.openUrl(SettingsActivity.this.getParentActivity(), LocaleController.getString("TelegramFaqUrl", 2131494471));
                        return;
                      }
                      if (paramAnonymousInt == SettingsActivity.this.privacyPolicyRow)
                      {
                        Browser.openUrl(SettingsActivity.this.getParentActivity(), LocaleController.getString("PrivacyPolicyUrl", 2131494201));
                        return;
                      }
                    } while (paramAnonymousInt == SettingsActivity.this.contactsReimportRow);
                    if (paramAnonymousInt != SettingsActivity.this.contactsSortRow) {
                      break;
                    }
                  } while (SettingsActivity.this.getParentActivity() == null);
                  paramAnonymousView = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
                  paramAnonymousView.setTitle(LocaleController.getString("SortBy", 2131494416));
                  localObject1 = LocaleController.getString("Default", 2131493354);
                  localObject2 = LocaleController.getString("SortFirstName", 2131494417);
                  localObject3 = LocaleController.getString("SortLastName", 2131494418);
                  localObject4 = new DialogInterface.OnClickListener()
                  {
                    public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                    {
                      paramAnonymous2DialogInterface = MessagesController.getGlobalMainSettings().edit();
                      paramAnonymous2DialogInterface.putInt("sortContactsBy", paramAnonymous2Int);
                      paramAnonymous2DialogInterface.commit();
                      if (SettingsActivity.this.listAdapter != null) {
                        SettingsActivity.this.listAdapter.notifyItemChanged(paramAnonymousInt);
                      }
                    }
                  };
                  paramAnonymousView.setItems(new CharSequence[] { localObject1, localObject2, localObject3 }, (DialogInterface.OnClickListener)localObject4);
                  paramAnonymousView.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                  SettingsActivity.this.showDialog(paramAnonymousView.create());
                  return;
                  if (paramAnonymousInt == SettingsActivity.this.usernameRow)
                  {
                    SettingsActivity.this.presentFragment(new ChangeUsernameActivity());
                    return;
                  }
                  if (paramAnonymousInt != SettingsActivity.this.bioRow) {
                    break;
                  }
                } while (MessagesController.getInstance(SettingsActivity.this.currentAccount).getUserFull(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId()) == null);
                SettingsActivity.this.presentFragment(new ChangeBioActivity());
                return;
                if (paramAnonymousInt == SettingsActivity.this.numberRow)
                {
                  SettingsActivity.this.presentFragment(new ChangePhoneHelpActivity());
                  return;
                }
                if (paramAnonymousInt == SettingsActivity.this.stickersRow)
                {
                  SettingsActivity.this.presentFragment(new StickersActivity(0));
                  return;
                }
                if (paramAnonymousInt != SettingsActivity.this.emojiRow) {
                  break;
                }
              } while (SettingsActivity.this.getParentActivity() == null);
              localObject1 = new boolean[2];
              Object localObject2 = new BottomSheet.Builder(SettingsActivity.this.getParentActivity());
              ((BottomSheet.Builder)localObject2).setApplyTopPadding(false);
              ((BottomSheet.Builder)localObject2).setApplyBottomPadding(false);
              Object localObject3 = new LinearLayout(SettingsActivity.this.getParentActivity());
              ((LinearLayout)localObject3).setOrientation(1);
              int i = 0;
              if (Build.VERSION.SDK_INT >= 19)
              {
                j = 2;
                if (i >= j) {
                  break label1736;
                }
                paramAnonymousView = null;
                if (i != 0) {
                  break label1711;
                }
                localObject1[i] = SharedConfig.allowBigEmoji;
                paramAnonymousView = LocaleController.getString("EmojiBigSize", 2131493420);
              }
              for (;;)
              {
                localObject4 = new CheckBoxCell(SettingsActivity.this.getParentActivity(), 1);
                ((CheckBoxCell)localObject4).setTag(Integer.valueOf(i));
                ((CheckBoxCell)localObject4).setBackgroundDrawable(Theme.getSelectorDrawable(false));
                ((LinearLayout)localObject3).addView((View)localObject4, LayoutHelper.createLinear(-1, 48));
                ((CheckBoxCell)localObject4).setText(paramAnonymousView, "", localObject1[i], true);
                ((CheckBoxCell)localObject4).setTextColor(Theme.getColor("dialogTextBlack"));
                ((CheckBoxCell)localObject4).setOnClickListener(new View.OnClickListener()
                {
                  public void onClick(View paramAnonymous2View)
                  {
                    paramAnonymous2View = (CheckBoxCell)paramAnonymous2View;
                    int i = ((Integer)paramAnonymous2View.getTag()).intValue();
                    boolean[] arrayOfBoolean = localObject1;
                    if (localObject1[i] == 0) {}
                    for (int j = 1;; j = 0)
                    {
                      arrayOfBoolean[i] = j;
                      paramAnonymous2View.setChecked(localObject1[i], true);
                      return;
                    }
                  }
                });
                i += 1;
                break;
                j = 1;
                break label1581;
                if (i == 1)
                {
                  localObject1[i] = SharedConfig.useSystemEmoji;
                  paramAnonymousView = LocaleController.getString("EmojiUseDefault", 2131493421);
                }
              }
              paramAnonymousView = new BottomSheet.BottomSheetCell(SettingsActivity.this.getParentActivity(), 1);
              paramAnonymousView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
              paramAnonymousView.setTextAndIcon(LocaleController.getString("Save", 2131494286).toUpperCase(), 0);
              paramAnonymousView.setTextColor(Theme.getColor("dialogTextBlue2"));
              paramAnonymousView.setOnClickListener(new View.OnClickListener()
              {
                public void onClick(View paramAnonymous2View)
                {
                  try
                  {
                    if (SettingsActivity.this.visibleDialog != null) {
                      SettingsActivity.this.visibleDialog.dismiss();
                    }
                    paramAnonymous2View = MessagesController.getGlobalMainSettings().edit();
                    int i = localObject1[0];
                    SharedConfig.allowBigEmoji = i;
                    paramAnonymous2View.putBoolean("allowBigEmoji", i);
                    int j = localObject1[1];
                    SharedConfig.useSystemEmoji = j;
                    paramAnonymous2View.putBoolean("useSystemEmoji", j);
                    paramAnonymous2View.commit();
                    if (SettingsActivity.this.listAdapter != null) {
                      SettingsActivity.this.listAdapter.notifyItemChanged(paramAnonymousInt);
                    }
                    return;
                  }
                  catch (Exception paramAnonymous2View)
                  {
                    for (;;)
                    {
                      FileLog.e(paramAnonymous2View);
                    }
                  }
                }
              });
              ((LinearLayout)localObject3).addView(paramAnonymousView, LayoutHelper.createLinear(-1, 48));
              ((BottomSheet.Builder)localObject2).setCustomView((View)localObject3);
              SettingsActivity.this.showDialog(((BottomSheet.Builder)localObject2).create());
              return;
              if (paramAnonymousInt != SettingsActivity.this.dumpCallStatsRow) {
                break label1942;
              }
              localObject1 = MessagesController.getGlobalMainSettings();
              bool2 = ((SharedPreferences)localObject1).getBoolean("dbg_dump_call_stats", false);
              localObject1 = ((SharedPreferences)localObject1).edit();
              if (bool2) {
                break;
              }
              bool1 = true;
              ((SharedPreferences.Editor)localObject1).putBoolean("dbg_dump_call_stats", bool1);
              ((SharedPreferences.Editor)localObject1).commit();
            } while (!(paramAnonymousView instanceof TextCheckCell));
            paramAnonymousView = (TextCheckCell)paramAnonymousView;
            if (!bool2) {}
            for (bool1 = true;; bool1 = false)
            {
              paramAnonymousView.setChecked(bool1);
              return;
              bool1 = false;
              break;
            }
          } while (paramAnonymousInt != SettingsActivity.this.forceTcpInCallsRow);
          final Object localObject1 = MessagesController.getGlobalMainSettings();
          bool2 = ((SharedPreferences)localObject1).getBoolean("dbg_force_tcp_in_calls", false);
          localObject1 = ((SharedPreferences)localObject1).edit();
          if (bool2) {
            break;
          }
          bool1 = true;
          ((SharedPreferences.Editor)localObject1).putBoolean("dbg_force_tcp_in_calls", bool1);
          ((SharedPreferences.Editor)localObject1).commit();
        } while (!(paramAnonymousView instanceof TextCheckCell));
        paramAnonymousView = (TextCheckCell)paramAnonymousView;
        if (!bool2) {}
        for (boolean bool1 = true;; bool1 = false)
        {
          paramAnonymousView.setChecked(bool1);
          return;
          bool1 = false;
          break;
        }
      }
    });
    this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
    {
      private int pressCount = 0;
      
      public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        boolean bool = false;
        String str1;
        if (paramAnonymousInt == SettingsActivity.this.versionRow)
        {
          this.pressCount += 1;
          if ((this.pressCount < 2) && (!BuildVars.DEBUG_PRIVATE_VERSION)) {
            break label252;
          }
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
          localBuilder.setTitle(LocaleController.getString("DebugMenu", 2131493343));
          String str2 = LocaleController.getString("DebugMenuImportContacts", 2131493349);
          String str3 = LocaleController.getString("DebugMenuReloadContacts", 2131493350);
          String str4 = LocaleController.getString("DebugMenuResetContacts", 2131493351);
          String str5 = LocaleController.getString("DebugMenuResetDialogs", 2131493352);
          if (!BuildVars.LOGS_ENABLED) {
            break label229;
          }
          paramAnonymousView = LocaleController.getString("DebugMenuDisableLogs", 2131493346);
          if (!SharedConfig.inappCamera) {
            break label240;
          }
          str1 = LocaleController.getString("DebugMenuDisableCamera", 2131493345);
          label131:
          String str6 = LocaleController.getString("DebugMenuClearMediaCache", 2131493344);
          DialogInterface.OnClickListener local1 = new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
            {
              boolean bool = true;
              if (paramAnonymous2Int == 0)
              {
                UserConfig.getInstance(SettingsActivity.this.currentAccount).syncContacts = true;
                UserConfig.getInstance(SettingsActivity.this.currentAccount).saveConfig(false);
                ContactsController.getInstance(SettingsActivity.this.currentAccount).forceImportContacts();
              }
              do
              {
                return;
                if (paramAnonymous2Int == 1)
                {
                  ContactsController.getInstance(SettingsActivity.this.currentAccount).loadContacts(false, 0);
                  return;
                }
                if (paramAnonymous2Int == 2)
                {
                  ContactsController.getInstance(SettingsActivity.this.currentAccount).resetImportedContacts();
                  return;
                }
                if (paramAnonymous2Int == 3)
                {
                  MessagesController.getInstance(SettingsActivity.this.currentAccount).forceResetDialogs();
                  return;
                }
                if (paramAnonymous2Int == 4)
                {
                  if (!BuildVars.LOGS_ENABLED) {}
                  for (;;)
                  {
                    BuildVars.LOGS_ENABLED = bool;
                    ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", 0).edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit();
                    return;
                    bool = false;
                  }
                }
                if (paramAnonymous2Int == 5)
                {
                  SharedConfig.toggleInappCamera();
                  return;
                }
                if (paramAnonymous2Int == 6)
                {
                  MessagesStorage.getInstance(SettingsActivity.this.currentAccount).clearSentMedia();
                  return;
                }
              } while (paramAnonymous2Int != 7);
              SharedConfig.toggleRoundCamera16to9();
            }
          };
          localBuilder.setItems(new CharSequence[] { str2, str3, str4, str5, paramAnonymousView, str1, str6 }, local1);
          localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
          SettingsActivity.this.showDialog(localBuilder.create());
        }
        for (;;)
        {
          bool = true;
          return bool;
          label229:
          paramAnonymousView = LocaleController.getString("DebugMenuEnableLogs", 2131493348);
          break;
          label240:
          str1 = LocaleController.getString("DebugMenuEnableCamera", 2131493347);
          break label131;
          try
          {
            label252:
            Toast.makeText(SettingsActivity.this.getParentActivity(), "¯\\_(ツ)_/¯", 0).show();
          }
          catch (Exception paramAnonymousView)
          {
            FileLog.e(paramAnonymousView);
          }
        }
      }
    });
    localFrameLayout.addView(this.actionBar);
    this.extraHeightView = new View(paramContext);
    this.extraHeightView.setPivotY(0.0F);
    this.extraHeightView.setBackgroundColor(Theme.getColor("avatar_backgroundActionBarBlue"));
    localFrameLayout.addView(this.extraHeightView, LayoutHelper.createFrame(-1, 88.0F));
    this.shadowView = new View(paramContext);
    this.shadowView.setBackgroundResource(2131165342);
    localFrameLayout.addView(this.shadowView, LayoutHelper.createFrame(-1, 3.0F));
    this.avatarImage = new BackupImageView(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(21.0F));
    this.avatarImage.setPivotX(0.0F);
    this.avatarImage.setPivotY(0.0F);
    localFrameLayout.addView(this.avatarImage, LayoutHelper.createFrame(42, 42.0F, 51, 64.0F, 0.0F, 0.0F, 0.0F));
    this.avatarImage.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        paramAnonymousView = MessagesController.getInstance(SettingsActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId()));
        if ((paramAnonymousView != null) && (paramAnonymousView.photo != null) && (paramAnonymousView.photo.photo_big != null))
        {
          PhotoViewer.getInstance().setParentActivity(SettingsActivity.this.getParentActivity());
          PhotoViewer.getInstance().openPhoto(paramAnonymousView.photo.photo_big, SettingsActivity.this.provider);
        }
      }
    });
    this.nameTextView = new TextView(paramContext);
    this.nameTextView.setTextColor(Theme.getColor("profile_title"));
    this.nameTextView.setTextSize(1, 18.0F);
    this.nameTextView.setLines(1);
    this.nameTextView.setMaxLines(1);
    this.nameTextView.setSingleLine(true);
    this.nameTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.nameTextView.setGravity(3);
    this.nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    this.nameTextView.setPivotX(0.0F);
    this.nameTextView.setPivotY(0.0F);
    localFrameLayout.addView(this.nameTextView, LayoutHelper.createFrame(-2, -2.0F, 51, 118.0F, 0.0F, 48.0F, 0.0F));
    this.onlineTextView = new TextView(paramContext);
    this.onlineTextView.setTextColor(Theme.getColor("avatar_subtitleInProfileBlue"));
    this.onlineTextView.setTextSize(1, 14.0F);
    this.onlineTextView.setLines(1);
    this.onlineTextView.setMaxLines(1);
    this.onlineTextView.setSingleLine(true);
    this.onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
    this.onlineTextView.setGravity(3);
    localFrameLayout.addView(this.onlineTextView, LayoutHelper.createFrame(-2, -2.0F, 51, 118.0F, 0.0F, 48.0F, 0.0F));
    this.writeButton = new ImageView(paramContext);
    localObject2 = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56.0F), Theme.getColor("profile_actionBackground"), Theme.getColor("profile_actionPressedBackground"));
    localObject1 = localObject2;
    if (Build.VERSION.SDK_INT < 21)
    {
      paramContext = paramContext.getResources().getDrawable(2131165323).mutate();
      paramContext.setColorFilter(new PorterDuffColorFilter(-16777216, PorterDuff.Mode.MULTIPLY));
      localObject1 = new CombinedDrawable(paramContext, (Drawable)localObject2, 0, 0);
      ((CombinedDrawable)localObject1).setIconSize(AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
    }
    this.writeButton.setBackgroundDrawable((Drawable)localObject1);
    this.writeButton.setImageResource(2131165319);
    this.writeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor("profile_actionIcon"), PorterDuff.Mode.MULTIPLY));
    this.writeButton.setScaleType(ImageView.ScaleType.CENTER);
    if (Build.VERSION.SDK_INT >= 21)
    {
      paramContext = new StateListAnimator();
      localObject1 = ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[] { AndroidUtilities.dp(2.0F), AndroidUtilities.dp(4.0F) }).setDuration(200L);
      paramContext.addState(new int[] { 16842919 }, (Animator)localObject1);
      localObject1 = ObjectAnimator.ofFloat(this.writeButton, "translationZ", new float[] { AndroidUtilities.dp(4.0F), AndroidUtilities.dp(2.0F) }).setDuration(200L);
      paramContext.addState(new int[0], (Animator)localObject1);
      this.writeButton.setStateListAnimator(paramContext);
      this.writeButton.setOutlineProvider(new ViewOutlineProvider()
      {
        @SuppressLint({"NewApi"})
        public void getOutline(View paramAnonymousView, Outline paramAnonymousOutline)
        {
          paramAnonymousOutline.setOval(0, 0, AndroidUtilities.dp(56.0F), AndroidUtilities.dp(56.0F));
        }
      });
    }
    paramContext = this.writeButton;
    int i;
    if (Build.VERSION.SDK_INT >= 21)
    {
      i = 56;
      if (Build.VERSION.SDK_INT < 21) {
        break label1138;
      }
    }
    label1138:
    for (float f = 56.0F;; f = 60.0F)
    {
      localFrameLayout.addView(paramContext, LayoutHelper.createFrame(i, f, 53, 0.0F, 0.0F, 16.0F, 0.0F));
      this.writeButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (SettingsActivity.this.getParentActivity() == null) {}
          AlertDialog.Builder localBuilder;
          do
          {
            return;
            localBuilder = new AlertDialog.Builder(SettingsActivity.this.getParentActivity());
            TLRPC.User localUser = MessagesController.getInstance(SettingsActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId()));
            paramAnonymousView = localUser;
            if (localUser == null) {
              paramAnonymousView = UserConfig.getInstance(SettingsActivity.this.currentAccount).getCurrentUser();
            }
          } while (paramAnonymousView == null);
          int i = 0;
          if ((paramAnonymousView.photo != null) && (paramAnonymousView.photo.photo_big != null) && (!(paramAnonymousView.photo instanceof TLRPC.TL_userProfilePhotoEmpty)))
          {
            paramAnonymousView = new CharSequence[3];
            paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
            paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
            paramAnonymousView[2] = LocaleController.getString("DeletePhoto", 2131493374);
            i = 1;
          }
          for (;;)
          {
            localBuilder.setItems(paramAnonymousView, new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                if (paramAnonymous2Int == 0) {
                  SettingsActivity.this.avatarUpdater.openCamera();
                }
                do
                {
                  return;
                  if (paramAnonymous2Int == 1)
                  {
                    SettingsActivity.this.avatarUpdater.openGallery();
                    return;
                  }
                } while (paramAnonymous2Int != 2);
                MessagesController.getInstance(SettingsActivity.this.currentAccount).deleteUserPhoto(null);
              }
            });
            SettingsActivity.this.showDialog(localBuilder.create());
            return;
            paramAnonymousView = new CharSequence[2];
            paramAnonymousView[0] = LocaleController.getString("FromCamera", 2131493613);
            paramAnonymousView[1] = LocaleController.getString("FromGalley", 2131493621);
          }
        }
      });
      needLayout();
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          paramAnonymousInt2 = 0;
          if (SettingsActivity.this.layoutManager.getItemCount() == 0) {}
          do
          {
            do
            {
              return;
              paramAnonymousInt1 = 0;
              paramAnonymousRecyclerView = paramAnonymousRecyclerView.getChildAt(0);
            } while (paramAnonymousRecyclerView == null);
            if (SettingsActivity.this.layoutManager.findFirstVisibleItemPosition() == 0)
            {
              int i = AndroidUtilities.dp(88.0F);
              paramAnonymousInt1 = paramAnonymousInt2;
              if (paramAnonymousRecyclerView.getTop() < 0) {
                paramAnonymousInt1 = paramAnonymousRecyclerView.getTop();
              }
              paramAnonymousInt1 = i + paramAnonymousInt1;
            }
          } while (SettingsActivity.this.extraHeight == paramAnonymousInt1);
          SettingsActivity.access$7402(SettingsActivity.this, paramAnonymousInt1);
          SettingsActivity.this.needLayout();
        }
      });
      return this.fragmentView;
      i = 60;
      break;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.updateInterfaces)
    {
      paramInt1 = ((Integer)paramVarArgs[0]).intValue();
      if (((paramInt1 & 0x2) != 0) || ((paramInt1 & 0x1) != 0)) {
        updateUserData();
      }
    }
    do
    {
      do
      {
        do
        {
          return;
          if (paramInt1 != NotificationCenter.featuredStickersDidLoaded) {
            break;
          }
        } while (this.listAdapter == null);
        this.listAdapter.notifyItemChanged(this.stickersRow);
        return;
        if (paramInt1 != NotificationCenter.userInfoDidLoaded) {
          break;
        }
      } while ((((Integer)paramVarArgs[0]).intValue() != UserConfig.getInstance(this.currentAccount).getClientUserId()) || (this.listAdapter == null));
      this.listAdapter.notifyItemChanged(this.bioRow);
      return;
    } while ((paramInt1 != NotificationCenter.emojiDidLoaded) || (this.listView == null));
    this.listView.invalidateViews();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextInfoCell.class, TextDetailSettingsCell.class }, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.extraHeightView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "avatar_backgroundActionBarBlue");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "avatar_actionBarIconBlue");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "avatar_actionBarSelectorBlue");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "profile_title");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.onlineTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "avatar_subtitleInProfileBlue");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, "actionBarDefaultSubmenuBackground");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, "actionBarDefaultSubmenuItem");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { ShadowSectionCell.class }, null, null, null, "windowBackgroundGrayShadow"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteValueText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumb"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrack"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchThumbChecked"), new ThemeDescription(this.listView, 0, new Class[] { TextCheckCell.class }, new String[] { "checkBox" }, null, null, null, "switchTrackChecked"), new ThemeDescription(this.listView, 0, new Class[] { HeaderCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlueHeader"), new ThemeDescription(this.listView, 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { TextDetailSettingsCell.class }, new String[] { "valueTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { TextInfoCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText5"), new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable }, null, "avatar_text"), new ThemeDescription(this.avatarImage, 0, null, null, new Drawable[] { this.avatarDrawable }, null, "avatar_backgroundInProfileBlue"), new ThemeDescription(this.writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, "profile_actionIcon"), new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, "profile_actionBackground"), new ThemeDescription(this.writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, "profile_actionPressedBackground") };
  }
  
  public void onActivityResultFragment(int paramInt1, int paramInt2, Intent paramIntent)
  {
    this.avatarUpdater.onActivityResult(paramInt1, paramInt2, paramIntent);
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    fixLayout();
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    this.avatarUpdater.parentFragment = this;
    this.avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate()
    {
      public void didUploadedPhoto(TLRPC.InputFile paramAnonymousInputFile, TLRPC.PhotoSize paramAnonymousPhotoSize1, TLRPC.PhotoSize paramAnonymousPhotoSize2)
      {
        paramAnonymousPhotoSize1 = new TLRPC.TL_photos_uploadProfilePhoto();
        paramAnonymousPhotoSize1.file = paramAnonymousInputFile;
        ConnectionsManager.getInstance(SettingsActivity.this.currentAccount).sendRequest(paramAnonymousPhotoSize1, new RequestDelegate()
        {
          public void run(TLObject paramAnonymous2TLObject, TLRPC.TL_error paramAnonymous2TL_error)
          {
            if (paramAnonymous2TL_error == null)
            {
              paramAnonymous2TL_error = MessagesController.getInstance(SettingsActivity.this.currentAccount).getUser(Integer.valueOf(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId()));
              if (paramAnonymous2TL_error != null) {
                break label240;
              }
              paramAnonymous2TL_error = UserConfig.getInstance(SettingsActivity.this.currentAccount).getCurrentUser();
              if (paramAnonymous2TL_error != null) {}
            }
            else
            {
              return;
            }
            MessagesController.getInstance(SettingsActivity.this.currentAccount).putUser(paramAnonymous2TL_error, false);
            paramAnonymous2TLObject = (TLRPC.TL_photos_photo)paramAnonymous2TLObject;
            Object localObject = paramAnonymous2TLObject.photo.sizes;
            TLRPC.PhotoSize localPhotoSize = FileLoader.getClosestPhotoSizeWithSize((ArrayList)localObject, 100);
            localObject = FileLoader.getClosestPhotoSizeWithSize((ArrayList)localObject, 1000);
            paramAnonymous2TL_error.photo = new TLRPC.TL_userProfilePhoto();
            paramAnonymous2TL_error.photo.photo_id = paramAnonymous2TLObject.photo.id;
            if (localPhotoSize != null) {
              paramAnonymous2TL_error.photo.photo_small = localPhotoSize.location;
            }
            if (localObject != null) {
              paramAnonymous2TL_error.photo.photo_big = ((TLRPC.PhotoSize)localObject).location;
            }
            for (;;)
            {
              MessagesStorage.getInstance(SettingsActivity.this.currentAccount).clearUserPhotos(paramAnonymous2TL_error.id);
              paramAnonymous2TLObject = new ArrayList();
              paramAnonymous2TLObject.add(paramAnonymous2TL_error);
              MessagesStorage.getInstance(SettingsActivity.this.currentAccount).putUsersAndChats(paramAnonymous2TLObject, null, false, true);
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  NotificationCenter.getInstance(SettingsActivity.this.currentAccount).postNotificationName(NotificationCenter.updateInterfaces, new Object[] { Integer.valueOf(1535) });
                  NotificationCenter.getInstance(SettingsActivity.this.currentAccount).postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
                  UserConfig.getInstance(SettingsActivity.this.currentAccount).saveConfig(true);
                }
              });
              return;
              label240:
              UserConfig.getInstance(SettingsActivity.this.currentAccount).setCurrentUser(paramAnonymous2TL_error);
              break;
              if (localPhotoSize != null) {
                paramAnonymous2TL_error.photo.photo_small = localPhotoSize.location;
              }
            }
          }
        });
      }
    };
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.featuredStickersDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.userInfoDidLoaded);
    NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
    this.rowCount = 0;
    int i = this.rowCount;
    this.rowCount = (i + 1);
    this.overscrollRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.emptyRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.numberSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.numberRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.usernameRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.bioRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.settingsSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.settingsSectionRow2 = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.notificationRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.privacyRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.dataRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.backgroundRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.themeRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.languageRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.enableAnimationsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messagesSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.messagesSectionRow2 = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.customTabsRow = i;
    if (Build.VERSION.SDK_INT >= 23)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.directShareRow = i;
    }
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.stickersRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.textSizeRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.raiseToSpeakRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.sendByEnterRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.autoplayGifsRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.saveToGalleryRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.supportSectionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.supportSectionRow2 = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.askQuestionRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.telegramFaqRow = i;
    i = this.rowCount;
    this.rowCount = (i + 1);
    this.privacyPolicyRow = i;
    if (BuildVars.LOGS_ENABLED)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.sendLogsRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.clearLogsRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.dumpCallStatsRow = i;
      if (!BuildVars.DEBUG_VERSION) {
        break label772;
      }
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.forceTcpInCallsRow = i;
      i = this.rowCount;
      this.rowCount = (i + 1);
    }
    label772:
    for (this.switchBackendButtonRow = i;; this.switchBackendButtonRow = -1)
    {
      i = this.rowCount;
      this.rowCount = (i + 1);
      this.versionRow = i;
      DataQuery.getInstance(this.currentAccount).checkFeaturedStickers();
      MessagesController.getInstance(this.currentAccount).loadFullUser(UserConfig.getInstance(this.currentAccount).getCurrentUser(), this.classGuid, true);
      return true;
      this.sendLogsRow = -1;
      this.clearLogsRow = -1;
      this.dumpCallStatsRow = -1;
      break;
    }
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    if (this.avatarImage != null) {
      this.avatarImage.setImageDrawable(null);
    }
    MessagesController.getInstance(this.currentAccount).cancelLoadFullUser(UserConfig.getInstance(this.currentAccount).getClientUserId());
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.featuredStickersDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.userInfoDidLoaded);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    this.avatarUpdater.clear();
  }
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    updateUserData();
    fixLayout();
  }
  
  public void restoreSelfArgs(Bundle paramBundle)
  {
    if (this.avatarUpdater != null) {
      this.avatarUpdater.currentPicturePath = paramBundle.getString("path");
    }
  }
  
  public void saveSelfArgs(Bundle paramBundle)
  {
    if ((this.avatarUpdater != null) && (this.avatarUpdater.currentPicturePath != null)) {
      paramBundle.putString("path", this.avatarUpdater.currentPicturePath);
    }
  }
  
  private static class LinkMovementMethodMy
    extends LinkMovementMethod
  {
    public boolean onTouchEvent(TextView paramTextView, Spannable paramSpannable, MotionEvent paramMotionEvent)
    {
      try
      {
        boolean bool = super.onTouchEvent(paramTextView, paramSpannable, paramMotionEvent);
        return bool;
      }
      catch (Exception paramTextView)
      {
        FileLog.e(paramTextView);
      }
      return false;
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
      return SettingsActivity.this.rowCount;
    }
    
    public int getItemViewType(int paramInt)
    {
      int j = 2;
      int i;
      if ((paramInt == SettingsActivity.this.emptyRow) || (paramInt == SettingsActivity.this.overscrollRow)) {
        i = 0;
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
                                          return i;
                                          if ((paramInt == SettingsActivity.this.settingsSectionRow) || (paramInt == SettingsActivity.this.supportSectionRow) || (paramInt == SettingsActivity.this.messagesSectionRow) || (paramInt == SettingsActivity.this.contactsSectionRow)) {
                                            return 1;
                                          }
                                          if ((paramInt == SettingsActivity.this.enableAnimationsRow) || (paramInt == SettingsActivity.this.sendByEnterRow) || (paramInt == SettingsActivity.this.saveToGalleryRow) || (paramInt == SettingsActivity.this.autoplayGifsRow) || (paramInt == SettingsActivity.this.raiseToSpeakRow) || (paramInt == SettingsActivity.this.customTabsRow) || (paramInt == SettingsActivity.this.directShareRow) || (paramInt == SettingsActivity.this.dumpCallStatsRow) || (paramInt == SettingsActivity.this.forceTcpInCallsRow)) {
                                            return 3;
                                          }
                                          i = j;
                                        } while (paramInt == SettingsActivity.this.notificationRow);
                                        i = j;
                                      } while (paramInt == SettingsActivity.this.themeRow);
                                      i = j;
                                    } while (paramInt == SettingsActivity.this.backgroundRow);
                                    i = j;
                                  } while (paramInt == SettingsActivity.this.askQuestionRow);
                                  i = j;
                                } while (paramInt == SettingsActivity.this.sendLogsRow);
                                i = j;
                              } while (paramInt == SettingsActivity.this.privacyRow);
                              i = j;
                            } while (paramInt == SettingsActivity.this.clearLogsRow);
                            i = j;
                          } while (paramInt == SettingsActivity.this.switchBackendButtonRow);
                          i = j;
                        } while (paramInt == SettingsActivity.this.telegramFaqRow);
                        i = j;
                      } while (paramInt == SettingsActivity.this.contactsReimportRow);
                      i = j;
                    } while (paramInt == SettingsActivity.this.textSizeRow);
                    i = j;
                  } while (paramInt == SettingsActivity.this.languageRow);
                  i = j;
                } while (paramInt == SettingsActivity.this.contactsSortRow);
                i = j;
              } while (paramInt == SettingsActivity.this.stickersRow);
              i = j;
            } while (paramInt == SettingsActivity.this.privacyPolicyRow);
            i = j;
          } while (paramInt == SettingsActivity.this.emojiRow);
          i = j;
        } while (paramInt == SettingsActivity.this.dataRow);
        if (paramInt == SettingsActivity.this.versionRow) {
          return 5;
        }
        if ((paramInt == SettingsActivity.this.numberRow) || (paramInt == SettingsActivity.this.usernameRow) || (paramInt == SettingsActivity.this.bioRow)) {
          return 6;
        }
        if ((paramInt == SettingsActivity.this.settingsSectionRow2) || (paramInt == SettingsActivity.this.messagesSectionRow2) || (paramInt == SettingsActivity.this.supportSectionRow2)) {
          break;
        }
        i = j;
      } while (paramInt != SettingsActivity.this.numberSectionRow);
      return 4;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      int i = paramViewHolder.getAdapterPosition();
      return (i == SettingsActivity.this.textSizeRow) || (i == SettingsActivity.this.enableAnimationsRow) || (i == SettingsActivity.this.notificationRow) || (i == SettingsActivity.this.backgroundRow) || (i == SettingsActivity.this.numberRow) || (i == SettingsActivity.this.askQuestionRow) || (i == SettingsActivity.this.sendLogsRow) || (i == SettingsActivity.this.sendByEnterRow) || (i == SettingsActivity.this.autoplayGifsRow) || (i == SettingsActivity.this.privacyRow) || (i == SettingsActivity.this.clearLogsRow) || (i == SettingsActivity.this.languageRow) || (i == SettingsActivity.this.usernameRow) || (i == SettingsActivity.this.bioRow) || (i == SettingsActivity.this.switchBackendButtonRow) || (i == SettingsActivity.this.telegramFaqRow) || (i == SettingsActivity.this.contactsSortRow) || (i == SettingsActivity.this.contactsReimportRow) || (i == SettingsActivity.this.saveToGalleryRow) || (i == SettingsActivity.this.stickersRow) || (i == SettingsActivity.this.raiseToSpeakRow) || (i == SettingsActivity.this.privacyPolicyRow) || (i == SettingsActivity.this.customTabsRow) || (i == SettingsActivity.this.directShareRow) || (i == SettingsActivity.this.versionRow) || (i == SettingsActivity.this.emojiRow) || (i == SettingsActivity.this.dataRow) || (i == SettingsActivity.this.themeRow) || (i == SettingsActivity.this.dumpCallStatsRow) || (i == SettingsActivity.this.forceTcpInCallsRow);
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      switch (paramViewHolder.getItemViewType())
      {
      }
      Object localObject;
      do
      {
        do
        {
          do
          {
            do
            {
              return;
              if (paramInt == SettingsActivity.this.overscrollRow)
              {
                ((EmptyCell)paramViewHolder.itemView).setHeight(AndroidUtilities.dp(88.0F));
                return;
              }
              ((EmptyCell)paramViewHolder.itemView).setHeight(AndroidUtilities.dp(16.0F));
              return;
              localObject = (TextSettingsCell)paramViewHolder.itemView;
              if (paramInt == SettingsActivity.this.textSizeRow)
              {
                paramViewHolder = MessagesController.getGlobalMainSettings();
                if (AndroidUtilities.isTablet()) {}
                for (paramInt = 18;; paramInt = 16)
                {
                  paramInt = paramViewHolder.getInt("fons_size", paramInt);
                  ((TextSettingsCell)localObject).setTextAndValue(LocaleController.getString("TextSize", 2131494481), String.format("%d", new Object[] { Integer.valueOf(paramInt) }), true);
                  return;
                }
              }
              if (paramInt == SettingsActivity.this.languageRow)
              {
                ((TextSettingsCell)localObject).setTextAndValue(LocaleController.getString("Language", 2131493721), LocaleController.getCurrentLanguageName(), true);
                return;
              }
              if (paramInt == SettingsActivity.this.themeRow)
              {
                ((TextSettingsCell)localObject).setTextAndValue(LocaleController.getString("Theme", 2131494482), Theme.getCurrentThemeName(), true);
                return;
              }
              if (paramInt == SettingsActivity.this.contactsSortRow)
              {
                paramInt = MessagesController.getGlobalMainSettings().getInt("sortContactsBy", 0);
                if (paramInt == 0) {
                  paramViewHolder = LocaleController.getString("Default", 2131493354);
                }
                for (;;)
                {
                  ((TextSettingsCell)localObject).setTextAndValue(LocaleController.getString("SortBy", 2131494416), paramViewHolder, true);
                  return;
                  if (paramInt == 1) {
                    paramViewHolder = LocaleController.getString("FirstName", 2131494417);
                  } else {
                    paramViewHolder = LocaleController.getString("LastName", 2131494418);
                  }
                }
              }
              if (paramInt == SettingsActivity.this.notificationRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("NotificationsAndSounds", 2131494005), true);
                return;
              }
              if (paramInt == SettingsActivity.this.backgroundRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("ChatBackground", 2131493220), true);
                return;
              }
              if (paramInt == SettingsActivity.this.sendLogsRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("DebugSendLogs", 2131493353), true);
                return;
              }
              if (paramInt == SettingsActivity.this.clearLogsRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("DebugClearLogs", 2131493342), true);
                return;
              }
              if (paramInt == SettingsActivity.this.askQuestionRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("AskAQuestion", 2131493020), true);
                return;
              }
              if (paramInt == SettingsActivity.this.privacyRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("PrivacySettings", 2131494202), true);
                return;
              }
              if (paramInt == SettingsActivity.this.dataRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("DataSettings", 2131493328), true);
                return;
              }
              if (paramInt == SettingsActivity.this.switchBackendButtonRow)
              {
                ((TextSettingsCell)localObject).setText("Switch Backend", true);
                return;
              }
              if (paramInt == SettingsActivity.this.telegramFaqRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("TelegramFAQ", 2131494469), true);
                return;
              }
              if (paramInt == SettingsActivity.this.contactsReimportRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("ImportContacts", 2131493666), true);
                return;
              }
              if (paramInt == SettingsActivity.this.stickersRow)
              {
                paramInt = DataQuery.getInstance(SettingsActivity.this.currentAccount).getUnreadStickerSets().size();
                String str = LocaleController.getString("StickersName", 2131494426);
                if (paramInt != 0) {}
                for (paramViewHolder = String.format("%d", new Object[] { Integer.valueOf(paramInt) });; paramViewHolder = "")
                {
                  ((TextSettingsCell)localObject).setTextAndValue(str, paramViewHolder, true);
                  return;
                }
              }
              if (paramInt == SettingsActivity.this.privacyPolicyRow)
              {
                ((TextSettingsCell)localObject).setText(LocaleController.getString("PrivacyPolicy", 2131494200), true);
                return;
              }
            } while (paramInt != SettingsActivity.this.emojiRow);
            ((TextSettingsCell)localObject).setText(LocaleController.getString("Emoji", 2131493419), true);
            return;
            paramViewHolder = (TextCheckCell)paramViewHolder.itemView;
            localObject = MessagesController.getGlobalMainSettings();
            if (paramInt == SettingsActivity.this.enableAnimationsRow)
            {
              paramViewHolder.setTextAndCheck(LocaleController.getString("EnableAnimations", 2131493424), ((SharedPreferences)localObject).getBoolean("view_animations", true), false);
              return;
            }
            if (paramInt == SettingsActivity.this.sendByEnterRow)
            {
              paramViewHolder.setTextAndCheck(LocaleController.getString("SendByEnter", 2131494334), ((SharedPreferences)localObject).getBoolean("send_by_enter", false), true);
              return;
            }
            if (paramInt == SettingsActivity.this.saveToGalleryRow)
            {
              paramViewHolder.setTextAndCheck(LocaleController.getString("SaveToGallerySettings", 2131494291), SharedConfig.saveToGallery, false);
              return;
            }
            if (paramInt == SettingsActivity.this.autoplayGifsRow)
            {
              paramViewHolder.setTextAndCheck(LocaleController.getString("AutoplayGifs", 2131493074), SharedConfig.autoplayGifs, true);
              return;
            }
            if (paramInt == SettingsActivity.this.raiseToSpeakRow)
            {
              paramViewHolder.setTextAndCheck(LocaleController.getString("RaiseToSpeak", 2131494213), SharedConfig.raiseToSpeak, true);
              return;
            }
            if (paramInt == SettingsActivity.this.customTabsRow)
            {
              paramViewHolder.setTextAndValueAndCheck(LocaleController.getString("ChromeCustomTabs", 2131493255), LocaleController.getString("ChromeCustomTabsInfo", 2131493256), SharedConfig.customTabs, false, true);
              return;
            }
            if (paramInt == SettingsActivity.this.directShareRow)
            {
              paramViewHolder.setTextAndValueAndCheck(LocaleController.getString("DirectShare", 2131493383), LocaleController.getString("DirectShareInfo", 2131493384), SharedConfig.directShare, false, true);
              return;
            }
            if (paramInt == SettingsActivity.this.dumpCallStatsRow)
            {
              paramViewHolder.setTextAndCheck("Dump detailed call stats", ((SharedPreferences)localObject).getBoolean("dbg_dump_call_stats", false), true);
              return;
            }
          } while (paramInt != SettingsActivity.this.forceTcpInCallsRow);
          paramViewHolder.setTextAndValueAndCheck("Force TCP in calls", "This disables UDP", ((SharedPreferences)localObject).getBoolean("dbg_force_tcp_in_calls", false), false, true);
          return;
          if (paramInt == SettingsActivity.this.settingsSectionRow2)
          {
            ((HeaderCell)paramViewHolder.itemView).setText(LocaleController.getString("SETTINGS", 2131494283));
            return;
          }
          if (paramInt == SettingsActivity.this.supportSectionRow2)
          {
            ((HeaderCell)paramViewHolder.itemView).setText(LocaleController.getString("Support", 2131494454));
            return;
          }
          if (paramInt == SettingsActivity.this.messagesSectionRow2)
          {
            ((HeaderCell)paramViewHolder.itemView).setText(LocaleController.getString("MessagesSettings", 2131493828));
            return;
          }
        } while (paramInt != SettingsActivity.this.numberSectionRow);
        ((HeaderCell)paramViewHolder.itemView).setText(LocaleController.getString("Info", 2131493675));
        return;
        localObject = (TextDetailSettingsCell)paramViewHolder.itemView;
        if (paramInt == SettingsActivity.this.numberRow)
        {
          paramViewHolder = UserConfig.getInstance(SettingsActivity.this.currentAccount).getCurrentUser();
          if ((paramViewHolder != null) && (paramViewHolder.phone != null) && (paramViewHolder.phone.length() != 0)) {}
          for (paramViewHolder = PhoneFormat.getInstance().format("+" + paramViewHolder.phone);; paramViewHolder = LocaleController.getString("NumberUnknown", 2131494027))
          {
            ((TextDetailSettingsCell)localObject).setTextAndValue(paramViewHolder, LocaleController.getString("Phone", 2131494149), true);
            return;
          }
        }
        if (paramInt == SettingsActivity.this.usernameRow)
        {
          paramViewHolder = UserConfig.getInstance(SettingsActivity.this.currentAccount).getCurrentUser();
          if ((paramViewHolder != null) && (!TextUtils.isEmpty(paramViewHolder.username))) {}
          for (paramViewHolder = "@" + paramViewHolder.username;; paramViewHolder = LocaleController.getString("UsernameEmpty", 2131494559))
          {
            ((TextDetailSettingsCell)localObject).setTextAndValue(paramViewHolder, LocaleController.getString("Username", 2131494556), true);
            return;
          }
        }
      } while (paramInt != SettingsActivity.this.bioRow);
      paramViewHolder = MessagesController.getInstance(SettingsActivity.this.currentAccount).getUserFull(UserConfig.getInstance(SettingsActivity.this.currentAccount).getClientUserId());
      if (paramViewHolder == null) {
        paramViewHolder = LocaleController.getString("Loading", 2131493762);
      }
      for (;;)
      {
        ((TextDetailSettingsCell)localObject).setTextWithEmojiAndValue(paramViewHolder, LocaleController.getString("UserBio", 2131494541), false);
        return;
        if (!TextUtils.isEmpty(paramViewHolder.about)) {
          paramViewHolder = paramViewHolder.about;
        } else {
          paramViewHolder = LocaleController.getString("UserBioEmpty", 2131494542);
        }
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = null;
      TextInfoCell localTextInfoCell;
      switch (paramInt)
      {
      default: 
      case 0: 
      case 1: 
      case 2: 
      case 3: 
      case 4: 
        for (;;)
        {
          paramViewGroup.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
          return new RecyclerListView.Holder(paramViewGroup);
          paramViewGroup = new EmptyCell(this.mContext);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          continue;
          paramViewGroup = new ShadowSectionCell(this.mContext);
          continue;
          paramViewGroup = new TextSettingsCell(this.mContext);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          continue;
          paramViewGroup = new TextCheckCell(this.mContext);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
          continue;
          paramViewGroup = new HeaderCell(this.mContext);
          paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        }
      case 5: 
        localTextInfoCell = new TextInfoCell(this.mContext);
        localTextInfoCell.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      for (;;)
      {
        try
        {
          PackageInfo localPackageInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
          paramInt = localPackageInfo.versionCode / 10;
          paramViewGroup = "";
          switch (localPackageInfo.versionCode % 10)
          {
          case 1: 
            ((TextInfoCell)localTextInfoCell).setText(LocaleController.formatString("TelegramVersion", 2131494472, new Object[] { String.format(Locale.US, "v%s (%d) %s", new Object[] { localPackageInfo.versionName, Integer.valueOf(paramInt), paramViewGroup }) }));
            paramViewGroup = localTextInfoCell;
          }
        }
        catch (Exception paramViewGroup)
        {
          FileLog.e(paramViewGroup);
          paramViewGroup = localTextInfoCell;
        }
        break;
        paramViewGroup = "arm-v7a";
        continue;
        paramViewGroup = "universal " + Build.CPU_ABI + " " + Build.CPU_ABI2;
        continue;
        paramViewGroup = new TextDetailSettingsCell(this.mContext);
        paramViewGroup.setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
        break;
        continue;
        paramViewGroup = "x86";
        continue;
        paramViewGroup = "arm64-v8a";
        continue;
        paramViewGroup = "x86_64";
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/SettingsActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */