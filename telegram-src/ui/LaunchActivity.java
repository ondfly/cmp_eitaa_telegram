package org.telegram.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocaleController.LocaleInfo;
import org.telegram.messenger.LocationController.SharingLocationInfo;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SendMessagesHelper.SendingMediaInfo;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.support.widget.DefaultItemAnimator;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatInvite;
import org.telegram.tgnet.TLRPC.LangPackString;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.TL_contacts_resolveUsername;
import org.telegram.tgnet.TLRPC.TL_contacts_resolvedPeer;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputGameShortName;
import org.telegram.tgnet.TLRPC.TL_inputMediaGame;
import org.telegram.tgnet.TLRPC.TL_inputStickerSetShortName;
import org.telegram.tgnet.TLRPC.TL_langpack_getStrings;
import org.telegram.tgnet.TLRPC.TL_messages_checkChatInvite;
import org.telegram.tgnet.TLRPC.TL_messages_importChatInvite;
import org.telegram.tgnet.TLRPC.TL_userContact_old2;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.Updates;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.Vector;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarLayout.ActionBarLayoutDelegate;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.Theme.ThemeInfo;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.LanguageCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.PasscodeView.PasscodeViewDelegate;
import org.telegram.ui.Components.PipRoundVideoView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.SharingLocationsAlert;
import org.telegram.ui.Components.SharingLocationsAlert.SharingLocationsAlertDelegate;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.ThemeEditorView;

public class LaunchActivity
  extends Activity
  implements NotificationCenter.NotificationCenterDelegate, ActionBarLayout.ActionBarLayoutDelegate, DialogsActivity.DialogsActivityDelegate
{
  private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList();
  private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList();
  private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList();
  private ActionBarLayout actionBarLayout;
  private View backgroundTablet;
  private ArrayList<TLRPC.User> contactsToSend;
  private int currentAccount;
  private int currentConnectionState;
  private String documentsMimeType;
  private ArrayList<String> documentsOriginalPathsArray;
  private ArrayList<String> documentsPathsArray;
  private ArrayList<Uri> documentsUrisArray;
  private DrawerLayoutAdapter drawerLayoutAdapter;
  protected DrawerLayoutContainer drawerLayoutContainer;
  private HashMap<String, String> englishLocaleStrings;
  private boolean finished;
  private ActionBarLayout layersActionBarLayout;
  private boolean loadingLocaleDialog;
  private AlertDialog localeDialog;
  private Runnable lockRunnable;
  private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
  private Intent passcodeSaveIntent;
  private boolean passcodeSaveIntentIsNew;
  private boolean passcodeSaveIntentIsRestore;
  private PasscodeView passcodeView;
  private ArrayList<SendMessagesHelper.SendingMediaInfo> photoPathsArray;
  private ActionBarLayout rightActionBarLayout;
  private String sendingText;
  private FrameLayout shadowTablet;
  private FrameLayout shadowTabletSide;
  private RecyclerListView sideMenu;
  private HashMap<String, String> systemLocaleStrings;
  private boolean tabletFullSize;
  private String videoPath;
  private AlertDialog visibleDialog;
  
  private void checkCurrentAccount()
  {
    if (this.currentAccount != UserConfig.selectedAccount)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
    }
    this.currentAccount = UserConfig.selectedAccount;
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.appDidLogout);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mainUserInfoChanged);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didUpdatedConnectionState);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.needShowAlert);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.openArticle);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.hasNewContactsToImport);
    updateCurrentConnectionState(this.currentAccount);
  }
  
  private void checkFreeDiscSpace()
  {
    if (Build.VERSION.SDK_INT >= 26) {
      return;
    }
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        if (!UserConfig.getInstance(LaunchActivity.this.currentAccount).isClientActivated()) {}
        for (;;)
        {
          return;
          try
          {
            SharedPreferences localSharedPreferences = MessagesController.getGlobalMainSettings();
            if (Math.abs(localSharedPreferences.getLong("last_space_check", 0L) - System.currentTimeMillis()) < 259200000L) {
              continue;
            }
            Object localObject = FileLoader.getDirectory(4);
            if (localObject == null) {
              continue;
            }
            localObject = new StatFs(((File)localObject).getAbsolutePath());
            if (Build.VERSION.SDK_INT < 18) {}
            long l2;
            for (long l1 = Math.abs(((StatFs)localObject).getAvailableBlocks() * ((StatFs)localObject).getBlockSize());; l1 *= l2)
            {
              localSharedPreferences.edit().putLong("last_space_check", System.currentTimeMillis()).commit();
              if (l1 >= 104857600L) {
                break;
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  try
                  {
                    AlertsCreator.createFreeSpaceDialog(LaunchActivity.this).show();
                    return;
                  }
                  catch (Throwable localThrowable) {}
                }
              });
              return;
              l1 = ((StatFs)localObject).getAvailableBlocksLong();
              l2 = ((StatFs)localObject).getBlockSizeLong();
            }
            return;
          }
          catch (Throwable localThrowable) {}
        }
      }
    }, 2000L);
  }
  
  private void checkLayout()
  {
    int j = 0;
    int k = 8;
    if ((!AndroidUtilities.isTablet()) || (this.rightActionBarLayout == null)) {
      return;
    }
    if ((!AndroidUtilities.isInMultiwindow) && ((!AndroidUtilities.isSmallTablet()) || (getResources().getConfiguration().orientation == 2)))
    {
      this.tabletFullSize = false;
      if (this.actionBarLayout.fragmentsStack.size() >= 2)
      {
        for (i = 1; i < this.actionBarLayout.fragmentsStack.size(); i = i - 1 + 1)
        {
          localObject = (BaseFragment)this.actionBarLayout.fragmentsStack.get(i);
          if ((localObject instanceof ChatActivity)) {
            ((ChatActivity)localObject).setIgnoreAttachOnPause(true);
          }
          ((BaseFragment)localObject).onPause();
          this.actionBarLayout.fragmentsStack.remove(i);
          this.rightActionBarLayout.fragmentsStack.add(localObject);
        }
        if (this.passcodeView.getVisibility() != 0)
        {
          this.actionBarLayout.showLastFragment();
          this.rightActionBarLayout.showLastFragment();
        }
      }
      localObject = this.rightActionBarLayout;
      if (this.rightActionBarLayout.fragmentsStack.isEmpty())
      {
        i = 8;
        ((ActionBarLayout)localObject).setVisibility(i);
        localObject = this.backgroundTablet;
        if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
          break label264;
        }
        i = 0;
        label225:
        ((View)localObject).setVisibility(i);
        localObject = this.shadowTabletSide;
        if (this.actionBarLayout.fragmentsStack.isEmpty()) {
          break label270;
        }
      }
      label264:
      label270:
      for (i = j;; i = 8)
      {
        ((FrameLayout)localObject).setVisibility(i);
        return;
        i = 0;
        break;
        i = 8;
        break label225;
      }
    }
    this.tabletFullSize = true;
    if (!this.rightActionBarLayout.fragmentsStack.isEmpty())
    {
      for (i = 0; this.rightActionBarLayout.fragmentsStack.size() > 0; i = i - 1 + 1)
      {
        localObject = (BaseFragment)this.rightActionBarLayout.fragmentsStack.get(i);
        if ((localObject instanceof ChatActivity)) {
          ((ChatActivity)localObject).setIgnoreAttachOnPause(true);
        }
        ((BaseFragment)localObject).onPause();
        this.rightActionBarLayout.fragmentsStack.remove(i);
        this.actionBarLayout.fragmentsStack.add(localObject);
      }
      if (this.passcodeView.getVisibility() != 0) {
        this.actionBarLayout.showLastFragment();
      }
    }
    this.shadowTabletSide.setVisibility(8);
    this.rightActionBarLayout.setVisibility(8);
    Object localObject = this.backgroundTablet;
    if (!this.actionBarLayout.fragmentsStack.isEmpty()) {}
    for (int i = k;; i = 0)
    {
      ((View)localObject).setVisibility(i);
      return;
    }
  }
  
  private String getStringForLanguageAlert(HashMap<String, String> paramHashMap, String paramString, int paramInt)
  {
    String str = (String)paramHashMap.get(paramString);
    paramHashMap = str;
    if (str == null) {
      paramHashMap = LocaleController.getString(paramString, paramInt);
    }
    return paramHashMap;
  }
  
  private boolean handleIntent(Intent paramIntent, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3)
  {
    if (AndroidUtilities.handleProxyIntent(this, paramIntent)) {
      return true;
    }
    if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible()) && ((paramIntent == null) || (!"android.intent.action.MAIN".equals(paramIntent.getAction())))) {
      PhotoViewer.getInstance().closePhoto(false, true);
    }
    int i = paramIntent.getFlags();
    final int[] arrayOfInt = new int[1];
    arrayOfInt[0] = paramIntent.getIntExtra("currentAccount", UserConfig.selectedAccount);
    switchToAccount(arrayOfInt[0], true);
    if ((!paramBoolean3) && ((AndroidUtilities.needShowPasscode(true)) || (SharedConfig.isWaitingForPasscodeEnter)))
    {
      showPasscodeActivity();
      this.passcodeSaveIntent = paramIntent;
      this.passcodeSaveIntentIsNew = paramBoolean1;
      this.passcodeSaveIntentIsRestore = paramBoolean2;
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
      return false;
    }
    boolean bool1 = false;
    Object localObject10 = Integer.valueOf(0);
    Integer localInteger1 = Integer.valueOf(0);
    Integer localInteger3 = Integer.valueOf(0);
    Integer localInteger2 = Integer.valueOf(0);
    Integer localInteger4 = Integer.valueOf(0);
    Integer localInteger5 = Integer.valueOf(0);
    long l = 0L;
    int n;
    int i1;
    int i2;
    Object localObject12;
    Object localObject15;
    Object localObject8;
    Object localObject16;
    Object localObject14;
    final Object localObject7;
    int j;
    int k;
    int m;
    Object localObject11;
    Object localObject9;
    Object localObject13;
    label734:
    label808:
    label860:
    label893:
    label1114:
    label1249:
    label1314:
    label1380:
    label1437:
    label1516:
    label1628:
    Object localObject3;
    if (SharedConfig.directShare)
    {
      if ((paramIntent != null) && (paramIntent.getExtras() != null)) {
        l = paramIntent.getExtras().getLong("dialogId", 0L);
      }
    }
    else
    {
      n = 0;
      i1 = 0;
      i2 = 0;
      this.photoPathsArray = null;
      this.videoPath = null;
      this.sendingText = null;
      this.documentsPathsArray = null;
      this.documentsOriginalPathsArray = null;
      this.documentsMimeType = null;
      this.documentsUrisArray = null;
      this.contactsToSend = null;
      localObject12 = localInteger5;
      localObject15 = localInteger4;
      localObject8 = localInteger1;
      localObject16 = localInteger3;
      localObject14 = localInteger2;
      localObject7 = localObject10;
      j = n;
      k = i2;
      m = i1;
      if (UserConfig.getInstance(this.currentAccount).isClientActivated())
      {
        localObject12 = localInteger5;
        localObject15 = localInteger4;
        localObject8 = localInteger1;
        localObject16 = localInteger3;
        localObject14 = localInteger2;
        localObject7 = localObject10;
        j = n;
        k = i2;
        m = i1;
        if ((0x100000 & i) == 0)
        {
          localObject12 = localInteger5;
          localObject15 = localInteger4;
          localObject8 = localInteger1;
          localObject16 = localInteger3;
          localObject14 = localInteger2;
          localObject7 = localObject10;
          j = n;
          k = i2;
          m = i1;
          if (paramIntent != null)
          {
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localInteger1;
            localObject16 = localInteger3;
            localObject14 = localInteger2;
            localObject7 = localObject10;
            j = n;
            k = i2;
            m = i1;
            if (paramIntent.getAction() != null)
            {
              localObject12 = localInteger5;
              localObject15 = localInteger4;
              localObject8 = localInteger1;
              localObject16 = localInteger3;
              localObject14 = localInteger2;
              localObject7 = localObject10;
              j = n;
              k = i2;
              m = i1;
              if (!paramBoolean2)
              {
                if (!"android.intent.action.SEND".equals(paramIntent.getAction())) {
                  break label2149;
                }
                i = 0;
                k = 0;
                localObject8 = paramIntent.getType();
                if ((localObject8 == null) || (!((String)localObject8).equals("text/x-vcard"))) {
                  break label1628;
                }
                try
                {
                  Object localObject1 = (Uri)paramIntent.getExtras().get("android.intent.extra.STREAM");
                  if (localObject1 != null)
                  {
                    localObject12 = getContentResolver().openInputStream((Uri)localObject1);
                    localObject11 = new ArrayList();
                    localObject9 = null;
                    localObject13 = new BufferedReader(new InputStreamReader((InputStream)localObject12, "UTF-8"));
                    for (;;)
                    {
                      localObject1 = ((BufferedReader)localObject13).readLine();
                      if (localObject1 == null) {
                        break label1437;
                      }
                      if (BuildVars.LOGS_ENABLED) {
                        FileLog.d((String)localObject1);
                      }
                      localObject14 = ((String)localObject1).split(":");
                      if (localObject14.length == 2)
                      {
                        if ((localObject14[0].equals("BEGIN")) && (localObject14[1].equals("VCARD")))
                        {
                          localObject1 = new VcardData(null);
                          ((ArrayList)localObject11).add(localObject1);
                        }
                        for (;;)
                        {
                          localObject9 = localObject1;
                          if (localObject1 == null) {
                            break;
                          }
                          if ((!localObject14[0].startsWith("FN")) && ((!localObject14[0].startsWith("ORG")) || (!TextUtils.isEmpty(((VcardData)localObject1).name)))) {
                            break label1380;
                          }
                          localObject8 = null;
                          localObject7 = null;
                          localObject15 = localObject14[0].split(";");
                          j = localObject15.length;
                          i = 0;
                          if (i >= j) {
                            break label860;
                          }
                          localObject16 = localObject15[i].split("=");
                          if (localObject16.length == 2) {
                            break label808;
                          }
                          localObject9 = localObject7;
                          break label7559;
                          localObject1 = localObject9;
                          if (localObject14[0].equals("END"))
                          {
                            localObject1 = localObject9;
                            if (localObject14[1].equals("VCARD")) {
                              localObject1 = null;
                            }
                          }
                        }
                        if (localObject16[0].equals("CHARSET"))
                        {
                          localObject9 = localObject16[1];
                          break label7559;
                        }
                        localObject9 = localObject7;
                        if (!localObject16[0].equals("ENCODING")) {
                          break label7559;
                        }
                        localObject8 = localObject16[1];
                        localObject9 = localObject7;
                        break label7559;
                        ((VcardData)localObject1).name = localObject14[1];
                        localObject9 = localObject1;
                        if (localObject8 != null)
                        {
                          localObject9 = localObject1;
                          if (((String)localObject8).equalsIgnoreCase("QUOTED-PRINTABLE"))
                          {
                            if ((((VcardData)localObject1).name.endsWith("=")) && (localObject8 != null))
                            {
                              ((VcardData)localObject1).name = ((VcardData)localObject1).name.substring(0, ((VcardData)localObject1).name.length() - 1);
                              localObject9 = ((BufferedReader)localObject13).readLine();
                              if (localObject9 != null) {
                                break;
                              }
                            }
                            localObject8 = AndroidUtilities.decodeQuotedPrintable(((VcardData)localObject1).name.getBytes());
                            localObject9 = localObject1;
                            if (localObject8 != null)
                            {
                              localObject9 = localObject1;
                              if (localObject8.length != 0)
                              {
                                localObject7 = new String((byte[])localObject8, (String)localObject7);
                                localObject9 = localObject1;
                                if (localObject7 != null)
                                {
                                  ((VcardData)localObject1).name = ((String)localObject7);
                                  localObject9 = localObject1;
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                    localObject12 = localInteger5;
                  }
                }
                catch (Exception localException1)
                {
                  FileLog.e(localException1);
                  i = 1;
                }
              }
            }
          }
        }
      }
      for (;;)
      {
        localObject15 = localInteger4;
        localObject8 = localInteger1;
        localObject16 = localInteger3;
        localObject14 = localInteger2;
        localObject7 = localObject10;
        j = n;
        k = i2;
        m = i1;
        if (i != 0)
        {
          Toast.makeText(this, "Unsupported content", 0).show();
          m = i1;
          k = i2;
          j = n;
          localObject7 = localObject10;
          localObject14 = localInteger2;
          localObject16 = localInteger3;
          localObject8 = localInteger1;
          localObject15 = localInteger4;
          localObject12 = localInteger5;
        }
        if (((Integer)localObject7).intValue() == 0) {
          break label6240;
        }
        Object localObject2 = new Bundle();
        ((Bundle)localObject2).putInt("user_id", ((Integer)localObject7).intValue());
        if (((Integer)localObject14).intValue() != 0) {
          ((Bundle)localObject2).putInt("message_id", ((Integer)localObject14).intValue());
        }
        if (!mainFragmentsStack.isEmpty())
        {
          paramBoolean2 = bool1;
          paramBoolean3 = paramBoolean1;
          if (!MessagesController.getInstance(arrayOfInt[0]).checkCanOpenChat((Bundle)localObject2, (BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {}
        }
        else
        {
          localObject2 = new ChatActivity((Bundle)localObject2);
          paramBoolean2 = bool1;
          paramBoolean3 = paramBoolean1;
          if (this.actionBarLayout.presentFragment((BaseFragment)localObject2, false, true, true))
          {
            paramBoolean2 = true;
            paramBoolean3 = paramBoolean1;
          }
        }
        if ((!paramBoolean2) && (!paramBoolean3))
        {
          if (!AndroidUtilities.isTablet()) {
            break label7379;
          }
          if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
            break label7325;
          }
          if (this.layersActionBarLayout.fragmentsStack.isEmpty())
          {
            this.layersActionBarLayout.addFragmentToStack(new LoginActivity());
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
          }
          this.actionBarLayout.showLastFragment();
          if (AndroidUtilities.isTablet())
          {
            this.layersActionBarLayout.showLastFragment();
            this.rightActionBarLayout.showLastFragment();
          }
        }
        paramIntent.setAction(null);
        return paramBoolean2;
        ((VcardData)localObject2).name += (String)localObject9;
        break label893;
        localObject9 = localObject2;
        if (!localObject14[0].startsWith("TEL")) {
          break;
        }
        localObject7 = PhoneFormat.stripExceptNumbers(localObject14[1], true);
        localObject9 = localObject2;
        if (((String)localObject7).length() <= 0) {
          break;
        }
        ((VcardData)localObject2).phones.add(localObject7);
        localObject9 = localObject2;
        break;
        try
        {
          ((BufferedReader)localObject13).close();
          ((InputStream)localObject12).close();
          j = 0;
          i = k;
          if (j >= ((ArrayList)localObject11).size()) {
            continue;
          }
          localObject8 = (VcardData)((ArrayList)localObject11).get(j);
          if ((((VcardData)localObject8).name != null) && (!((VcardData)localObject8).phones.isEmpty()))
          {
            if (this.contactsToSend != null) {
              break label7578;
            }
            this.contactsToSend = new ArrayList();
            break label7578;
            while (i < ((VcardData)localObject8).phones.size())
            {
              localObject7 = (String)((VcardData)localObject8).phones.get(i);
              localObject2 = new TLRPC.TL_userContact_old2();
              ((TLRPC.User)localObject2).phone = ((String)localObject7);
              ((TLRPC.User)localObject2).first_name = ((VcardData)localObject8).name;
              ((TLRPC.User)localObject2).last_name = "";
              ((TLRPC.User)localObject2).id = 0;
              this.contactsToSend.add(localObject2);
              i += 1;
            }
          }
        }
        catch (Exception localException2)
        {
          for (;;)
          {
            FileLog.e(localException2);
            continue;
            j += 1;
          }
        }
        i = 1;
        continue;
        localObject7 = paramIntent.getStringExtra("android.intent.extra.TEXT");
        localObject3 = localObject7;
        if (localObject7 == null)
        {
          localObject9 = paramIntent.getCharSequenceExtra("android.intent.extra.TEXT");
          localObject3 = localObject7;
          if (localObject9 != null) {
            localObject3 = ((CharSequence)localObject9).toString();
          }
        }
        localObject9 = paramIntent.getStringExtra("android.intent.extra.SUBJECT");
        if ((localObject3 != null) && (((String)localObject3).length() != 0)) {
          if (!((String)localObject3).startsWith("http://"))
          {
            localObject7 = localObject3;
            if (!((String)localObject3).startsWith("https://")) {}
          }
          else
          {
            localObject7 = localObject3;
            if (localObject9 != null)
            {
              localObject7 = localObject3;
              if (((String)localObject9).length() != 0) {
                localObject7 = (String)localObject9 + "\n" + (String)localObject3;
              }
            }
          }
        }
        for (this.sendingText = ((String)localObject7);; this.sendingText = ((String)localObject9)) {
          do
          {
            localObject7 = paramIntent.getParcelableExtra("android.intent.extra.STREAM");
            if (localObject7 == null) {
              break label2132;
            }
            localObject3 = localObject7;
            if (!(localObject7 instanceof Uri)) {
              localObject3 = Uri.parse(localObject7.toString());
            }
            localObject9 = (Uri)localObject3;
            j = i;
            if (localObject9 != null)
            {
              j = i;
              if (AndroidUtilities.isInternalUri((Uri)localObject9)) {
                j = 1;
              }
            }
            i = j;
            if (j != 0) {
              break;
            }
            if ((localObject9 == null) || (((localObject8 == null) || (!((String)localObject8).startsWith("image/"))) && (!((Uri)localObject9).toString().toLowerCase().endsWith(".jpg")))) {
              break label1963;
            }
            if (this.photoPathsArray == null) {
              this.photoPathsArray = new ArrayList();
            }
            localObject3 = new SendMessagesHelper.SendingMediaInfo();
            ((SendMessagesHelper.SendingMediaInfo)localObject3).uri = ((Uri)localObject9);
            this.photoPathsArray.add(localObject3);
            i = j;
            break;
          } while ((localObject9 == null) || (((String)localObject9).length() <= 0));
        }
        label1963:
        localObject7 = AndroidUtilities.getPath((Uri)localObject9);
        if (localObject7 != null)
        {
          localObject3 = localObject7;
          if (((String)localObject7).startsWith("file:")) {
            localObject3 = ((String)localObject7).replace("file://", "");
          }
          if ((localObject8 != null) && (((String)localObject8).startsWith("video/")))
          {
            this.videoPath = ((String)localObject3);
            i = j;
          }
          else
          {
            if (this.documentsPathsArray == null)
            {
              this.documentsPathsArray = new ArrayList();
              this.documentsOriginalPathsArray = new ArrayList();
            }
            this.documentsPathsArray.add(localObject3);
            this.documentsOriginalPathsArray.add(((Uri)localObject9).toString());
            i = j;
          }
        }
        else
        {
          if (this.documentsUrisArray == null) {
            this.documentsUrisArray = new ArrayList();
          }
          this.documentsUrisArray.add(localObject9);
          this.documentsMimeType = ((String)localObject8);
          i = j;
          continue;
          label2132:
          i = k;
          if (this.sendingText == null) {
            i = 1;
          }
        }
      }
      label2149:
      if (paramIntent.getAction().equals("android.intent.action.SEND_MULTIPLE")) {
        k = 0;
      }
    }
    for (;;)
    {
      try
      {
        localObject7 = paramIntent.getParcelableArrayListExtra("android.intent.extra.STREAM");
        localObject11 = paramIntent.getType();
        localObject3 = localObject7;
        if (localObject7 != null)
        {
          i = 0;
          if (i < ((ArrayList)localObject7).size())
          {
            localObject8 = (Parcelable)((ArrayList)localObject7).get(i);
            localObject3 = localObject8;
            if (!(localObject8 instanceof Uri)) {
              localObject3 = Uri.parse(localObject8.toString());
            }
            localObject3 = (Uri)localObject3;
            j = i;
            if (localObject3 == null) {
              break label7584;
            }
            j = i;
            if (!AndroidUtilities.isInternalUri((Uri)localObject3)) {
              break label7584;
            }
            ((ArrayList)localObject7).remove(i);
            j = i - 1;
            break label7584;
          }
          localObject3 = localObject7;
          if (((ArrayList)localObject7).isEmpty()) {
            localObject3 = null;
          }
        }
        if (localObject3 != null)
        {
          if ((localObject11 == null) || (!((String)localObject11).startsWith("image/"))) {
            break label7593;
          }
          j = 0;
          i = k;
          if (j < ((ArrayList)localObject3).size())
          {
            localObject8 = (Parcelable)((ArrayList)localObject3).get(j);
            localObject7 = localObject8;
            if (!(localObject8 instanceof Uri)) {
              localObject7 = Uri.parse(localObject8.toString());
            }
            localObject8 = (Uri)localObject7;
            if (this.photoPathsArray == null) {
              this.photoPathsArray = new ArrayList();
            }
            localObject7 = new SendMessagesHelper.SendingMediaInfo();
            ((SendMessagesHelper.SendingMediaInfo)localObject7).uri = ((Uri)localObject8);
            this.photoPathsArray.add(localObject7);
            j += 1;
            continue;
            i = k;
            if (j < ((ArrayList)localObject3).size())
            {
              localObject7 = (Parcelable)((ArrayList)localObject3).get(j);
              localObject8 = localObject7;
              if (!(localObject7 instanceof Uri)) {
                localObject8 = Uri.parse(localObject7.toString());
              }
              localObject12 = (Uri)localObject8;
              localObject7 = AndroidUtilities.getPath((Uri)localObject12);
              localObject9 = localObject8.toString();
              localObject8 = localObject9;
              if (localObject9 == null) {
                localObject8 = localObject7;
              }
              if (localObject7 != null)
              {
                localObject9 = localObject7;
                if (((String)localObject7).startsWith("file:")) {
                  localObject9 = ((String)localObject7).replace("file://", "");
                }
                if (this.documentsPathsArray == null)
                {
                  this.documentsPathsArray = new ArrayList();
                  this.documentsOriginalPathsArray = new ArrayList();
                }
                this.documentsPathsArray.add(localObject9);
                this.documentsOriginalPathsArray.add(localObject8);
              }
              else
              {
                if (this.documentsUrisArray == null) {
                  this.documentsUrisArray = new ArrayList();
                }
                this.documentsUrisArray.add(localObject12);
                this.documentsMimeType = ((String)localObject11);
              }
            }
          }
        }
      }
      catch (Exception localException3)
      {
        FileLog.e(localException3);
        i = 1;
      }
      for (;;)
      {
        localObject12 = localInteger5;
        localObject15 = localInteger4;
        localObject8 = localInteger1;
        localObject16 = localInteger3;
        localObject14 = localInteger2;
        localObject7 = localObject10;
        j = n;
        k = i2;
        m = i1;
        if (i == 0) {
          break;
        }
        Toast.makeText(this, "Unsupported content", 0).show();
        localObject12 = localInteger5;
        localObject15 = localInteger4;
        localObject8 = localInteger1;
        localObject16 = localInteger3;
        localObject14 = localInteger2;
        localObject7 = localObject10;
        j = n;
        k = i2;
        m = i1;
        break;
        i = 1;
      }
      Object localObject29;
      Object localObject21;
      String str2;
      Object localObject22;
      Object localObject23;
      String str1;
      Object localObject26;
      Object localObject28;
      Object localObject24;
      Integer localInteger6;
      Object localObject25;
      Object localObject27;
      boolean bool5;
      Object localObject17;
      Object localObject18;
      Object localObject19;
      Object localObject20;
      Object localObject4;
      if ("android.intent.action.VIEW".equals(paramIntent.getAction()))
      {
        localObject29 = paramIntent.getData();
        localObject12 = localInteger5;
        localObject15 = localInteger4;
        localObject8 = localInteger1;
        localObject16 = localInteger3;
        localObject14 = localInteger2;
        localObject7 = localObject10;
        j = n;
        k = i2;
        m = i1;
        if (localObject29 == null) {
          break label1114;
        }
        localObject21 = null;
        str2 = null;
        localObject22 = null;
        localObject23 = null;
        str1 = null;
        localObject26 = null;
        localObject28 = null;
        localObject24 = null;
        localInteger6 = null;
        localObject25 = null;
        localObject9 = null;
        localObject27 = null;
        boolean bool2 = false;
        paramBoolean3 = false;
        boolean bool3 = false;
        boolean bool4 = false;
        bool5 = false;
        String str3 = ((Uri)localObject29).getScheme();
        localObject12 = str2;
        localObject14 = localObject22;
        localObject15 = localObject23;
        localObject16 = str1;
        localObject17 = localObject26;
        localObject7 = localObject28;
        paramBoolean2 = bool5;
        localObject8 = localObject27;
        localObject18 = localInteger6;
        localObject19 = localObject24;
        localObject20 = localObject25;
        localObject11 = localInteger1;
        localObject13 = localInteger2;
        localObject4 = localObject10;
        if (str3 != null)
        {
          if ((!str3.equals("http")) && (!str3.equals("https"))) {
            break label4065;
          }
          str3 = ((Uri)localObject29).getHost().toLowerCase();
          if ((!str3.equals("telegram.me")) && (!str3.equals("t.me")) && (!str3.equals("telegram.dog")))
          {
            localObject12 = str2;
            localObject14 = localObject22;
            localObject15 = localObject23;
            localObject16 = str1;
            localObject17 = localObject26;
            localObject7 = localObject28;
            paramBoolean2 = bool5;
            localObject8 = localObject27;
            localObject18 = localInteger6;
            localObject19 = localObject24;
            localObject20 = localObject25;
            localObject11 = localInteger1;
            localObject13 = localInteger2;
            localObject4 = localObject10;
            if (!str3.equals("telesco.pe")) {}
          }
          else
          {
            str3 = ((Uri)localObject29).getPath();
            localObject12 = str2;
            localObject14 = localObject22;
            localObject15 = localObject23;
            localObject16 = str1;
            localObject17 = localObject26;
            localObject7 = localObject28;
            paramBoolean2 = bool5;
            localObject8 = localObject27;
            localObject18 = localInteger6;
            localObject19 = localObject24;
            localObject20 = localObject25;
            localObject11 = localInteger1;
            localObject13 = localInteger2;
            localObject4 = localObject10;
            if (str3 != null)
            {
              localObject12 = str2;
              localObject14 = localObject22;
              localObject15 = localObject23;
              localObject16 = str1;
              localObject17 = localObject26;
              localObject7 = localObject28;
              paramBoolean2 = bool5;
              localObject8 = localObject27;
              localObject18 = localInteger6;
              localObject19 = localObject24;
              localObject20 = localObject25;
              localObject11 = localInteger1;
              localObject13 = localInteger2;
              localObject4 = localObject10;
              if (str3.length() > 1)
              {
                str3 = str3.substring(1);
                if (!str3.startsWith("joinchat/")) {
                  break label3400;
                }
                localObject14 = str3.replace("joinchat/", "");
                localObject4 = localObject10;
                localObject13 = localInteger2;
                localObject11 = localInteger1;
                localObject20 = localObject25;
                localObject19 = localObject24;
                localObject18 = localInteger6;
                localObject8 = localObject27;
                paramBoolean2 = bool5;
                localObject7 = localObject28;
                localObject17 = localObject26;
                localObject16 = str1;
                localObject15 = localObject23;
                localObject12 = str2;
              }
            }
          }
        }
        label3262:
        label3400:
        label4065:
        label4999:
        do
        {
          for (;;)
          {
            localObject9 = localObject7;
            if (localObject7 != null)
            {
              localObject9 = localObject7;
              if (((String)localObject7).startsWith("@")) {
                localObject9 = " " + (String)localObject7;
              }
            }
            if ((localObject19 == null) && (localObject20 == null)) {
              break label5316;
            }
            localObject7 = new Bundle();
            ((Bundle)localObject7).putString("phone", (String)localObject19);
            ((Bundle)localObject7).putString("hash", (String)localObject20);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                LaunchActivity.this.presentFragment(new CancelAccountDeletionActivity(localObject7));
              }
            });
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localObject11;
            localObject16 = localInteger3;
            localObject14 = localObject13;
            localObject7 = localObject4;
            j = n;
            k = i2;
            m = i1;
            break;
            if (str3.startsWith("addstickers/"))
            {
              localObject15 = str3.replace("addstickers/", "");
              localObject12 = str2;
              localObject14 = localObject22;
              localObject16 = str1;
              localObject17 = localObject26;
              localObject7 = localObject28;
              paramBoolean2 = bool5;
              localObject8 = localObject27;
              localObject18 = localInteger6;
              localObject19 = localObject24;
              localObject20 = localObject25;
              localObject11 = localInteger1;
              localObject13 = localInteger2;
              localObject4 = localObject10;
            }
            else
            {
              if (str3.startsWith("iv/"))
              {
                ((Uri)localObject29).getQueryParameter("url");
                ((Uri)localObject29).getQueryParameter("rhash");
                throw new NullPointerException();
              }
              if ((str3.startsWith("msg/")) || (str3.startsWith("share/")))
              {
                localObject7 = ((Uri)localObject29).getQueryParameter("url");
                localObject4 = localObject7;
                if (localObject7 == null) {
                  localObject4 = "";
                }
                localObject7 = localObject4;
                if (((Uri)localObject29).getQueryParameter("text") != null)
                {
                  localObject7 = localObject4;
                  paramBoolean3 = bool2;
                  if (((String)localObject4).length() > 0)
                  {
                    paramBoolean3 = true;
                    localObject7 = (String)localObject4 + "\n";
                  }
                  localObject7 = (String)localObject7 + ((Uri)localObject29).getQueryParameter("text");
                }
                localObject9 = localObject7;
                if (((String)localObject7).length() > 16384) {}
                for (localObject9 = ((String)localObject7).substring(0, 16384);; localObject9 = ((String)localObject9).substring(0, ((String)localObject9).length() - 1))
                {
                  localObject12 = str2;
                  localObject14 = localObject22;
                  localObject15 = localObject23;
                  localObject16 = str1;
                  localObject17 = localObject26;
                  localObject7 = localObject9;
                  paramBoolean2 = paramBoolean3;
                  localObject8 = localObject27;
                  localObject18 = localInteger6;
                  localObject19 = localObject24;
                  localObject20 = localObject25;
                  localObject11 = localInteger1;
                  localObject13 = localInteger2;
                  localObject4 = localObject10;
                  if (!((String)localObject9).endsWith("\n")) {
                    break;
                  }
                }
              }
              if (str3.startsWith("confirmphone"))
              {
                localObject19 = ((Uri)localObject29).getQueryParameter("phone");
                localObject20 = ((Uri)localObject29).getQueryParameter("hash");
                localObject12 = str2;
                localObject14 = localObject22;
                localObject15 = localObject23;
                localObject16 = str1;
                localObject17 = localObject26;
                localObject7 = localObject28;
                paramBoolean2 = bool5;
                localObject8 = localObject27;
                localObject18 = localInteger6;
                localObject11 = localInteger1;
                localObject13 = localInteger2;
                localObject4 = localObject10;
              }
              else
              {
                localObject12 = str2;
                localObject14 = localObject22;
                localObject15 = localObject23;
                localObject16 = str1;
                localObject17 = localObject26;
                localObject7 = localObject28;
                paramBoolean2 = bool5;
                localObject8 = localObject27;
                localObject18 = localInteger6;
                localObject19 = localObject24;
                localObject20 = localObject25;
                localObject11 = localInteger1;
                localObject13 = localInteger2;
                localObject4 = localObject10;
                if (str3.length() >= 1)
                {
                  localObject7 = ((Uri)localObject29).getPathSegments();
                  localObject12 = localObject21;
                  localObject8 = localObject9;
                  if (((List)localObject7).size() > 0)
                  {
                    localObject4 = (String)((List)localObject7).get(0);
                    localObject12 = localObject4;
                    localObject8 = localObject9;
                    if (((List)localObject7).size() > 1)
                    {
                      localObject7 = Utilities.parseInt((String)((List)localObject7).get(1));
                      localObject12 = localObject4;
                      localObject8 = localObject7;
                      if (((Integer)localObject7).intValue() == 0)
                      {
                        localObject8 = null;
                        localObject12 = localObject4;
                      }
                    }
                  }
                  localObject16 = ((Uri)localObject29).getQueryParameter("start");
                  localObject17 = ((Uri)localObject29).getQueryParameter("startgroup");
                  localObject18 = ((Uri)localObject29).getQueryParameter("game");
                  localObject14 = localObject22;
                  localObject15 = localObject23;
                  localObject7 = localObject28;
                  paramBoolean2 = bool5;
                  localObject19 = localObject24;
                  localObject20 = localObject25;
                  localObject11 = localInteger1;
                  localObject13 = localInteger2;
                  localObject4 = localObject10;
                  continue;
                  localObject12 = str2;
                  localObject14 = localObject22;
                  localObject15 = localObject23;
                  localObject16 = str1;
                  localObject17 = localObject26;
                  localObject7 = localObject28;
                  paramBoolean2 = bool5;
                  localObject8 = localObject27;
                  localObject18 = localInteger6;
                  localObject19 = localObject24;
                  localObject20 = localObject25;
                  localObject11 = localInteger1;
                  localObject13 = localInteger2;
                  localObject4 = localObject10;
                  if (str3.equals("tg"))
                  {
                    localObject9 = ((Uri)localObject29).toString();
                    if ((((String)localObject9).startsWith("tg:resolve")) || (((String)localObject9).startsWith("tg://resolve")))
                    {
                      localObject4 = Uri.parse(((String)localObject9).replace("tg:resolve", "tg://telegram.org").replace("tg://resolve", "tg://telegram.org"));
                      localObject21 = ((Uri)localObject4).getQueryParameter("domain");
                      str1 = ((Uri)localObject4).getQueryParameter("start");
                      localObject9 = ((Uri)localObject4).getQueryParameter("startgroup");
                      str2 = ((Uri)localObject4).getQueryParameter("game");
                      localInteger6 = Utilities.parseInt(((Uri)localObject4).getQueryParameter("post"));
                      localObject12 = localObject21;
                      localObject14 = localObject22;
                      localObject15 = localObject23;
                      localObject16 = str1;
                      localObject17 = localObject9;
                      localObject7 = localObject28;
                      paramBoolean2 = bool5;
                      localObject8 = localInteger6;
                      localObject18 = str2;
                      localObject19 = localObject24;
                      localObject20 = localObject25;
                      localObject11 = localInteger1;
                      localObject13 = localInteger2;
                      localObject4 = localObject10;
                      if (localInteger6.intValue() == 0)
                      {
                        localObject8 = null;
                        localObject12 = localObject21;
                        localObject14 = localObject22;
                        localObject15 = localObject23;
                        localObject16 = str1;
                        localObject17 = localObject9;
                        localObject7 = localObject28;
                        paramBoolean2 = bool5;
                        localObject18 = str2;
                        localObject19 = localObject24;
                        localObject20 = localObject25;
                        localObject11 = localInteger1;
                        localObject13 = localInteger2;
                        localObject4 = localObject10;
                      }
                    }
                    else if ((((String)localObject9).startsWith("tg:join")) || (((String)localObject9).startsWith("tg://join")))
                    {
                      localObject14 = Uri.parse(((String)localObject9).replace("tg:join", "tg://telegram.org").replace("tg://join", "tg://telegram.org")).getQueryParameter("invite");
                      localObject12 = str2;
                      localObject15 = localObject23;
                      localObject16 = str1;
                      localObject17 = localObject26;
                      localObject7 = localObject28;
                      paramBoolean2 = bool5;
                      localObject8 = localObject27;
                      localObject18 = localInteger6;
                      localObject19 = localObject24;
                      localObject20 = localObject25;
                      localObject11 = localInteger1;
                      localObject13 = localInteger2;
                      localObject4 = localObject10;
                    }
                    else if ((((String)localObject9).startsWith("tg:addstickers")) || (((String)localObject9).startsWith("tg://addstickers")))
                    {
                      localObject15 = Uri.parse(((String)localObject9).replace("tg:addstickers", "tg://telegram.org").replace("tg://addstickers", "tg://telegram.org")).getQueryParameter("set");
                      localObject12 = str2;
                      localObject14 = localObject22;
                      localObject16 = str1;
                      localObject17 = localObject26;
                      localObject7 = localObject28;
                      paramBoolean2 = bool5;
                      localObject8 = localObject27;
                      localObject18 = localInteger6;
                      localObject19 = localObject24;
                      localObject20 = localObject25;
                      localObject11 = localInteger1;
                      localObject13 = localInteger2;
                      localObject4 = localObject10;
                    }
                    else
                    {
                      if ((((String)localObject9).startsWith("tg:msg")) || (((String)localObject9).startsWith("tg://msg")) || (((String)localObject9).startsWith("tg://share")) || (((String)localObject9).startsWith("tg:share")))
                      {
                        localObject8 = Uri.parse(((String)localObject9).replace("tg:msg", "tg://telegram.org").replace("tg://msg", "tg://telegram.org").replace("tg://share", "tg://telegram.org").replace("tg:share", "tg://telegram.org"));
                        localObject7 = ((Uri)localObject8).getQueryParameter("url");
                        localObject4 = localObject7;
                        if (localObject7 == null) {
                          localObject4 = "";
                        }
                        localObject7 = localObject4;
                        paramBoolean3 = bool4;
                        if (((Uri)localObject8).getQueryParameter("text") != null)
                        {
                          localObject7 = localObject4;
                          paramBoolean3 = bool3;
                          if (((String)localObject4).length() > 0)
                          {
                            paramBoolean3 = true;
                            localObject7 = (String)localObject4 + "\n";
                          }
                          localObject7 = (String)localObject7 + ((Uri)localObject8).getQueryParameter("text");
                        }
                        localObject9 = localObject7;
                        if (((String)localObject7).length() > 16384) {}
                        for (localObject9 = ((String)localObject7).substring(0, 16384);; localObject9 = ((String)localObject9).substring(0, ((String)localObject9).length() - 1))
                        {
                          localObject12 = str2;
                          localObject14 = localObject22;
                          localObject15 = localObject23;
                          localObject16 = str1;
                          localObject17 = localObject26;
                          localObject7 = localObject9;
                          paramBoolean2 = paramBoolean3;
                          localObject8 = localObject27;
                          localObject18 = localInteger6;
                          localObject19 = localObject24;
                          localObject20 = localObject25;
                          localObject11 = localInteger1;
                          localObject13 = localInteger2;
                          localObject4 = localObject10;
                          if (!((String)localObject9).endsWith("\n")) {
                            break;
                          }
                        }
                      }
                      if ((!((String)localObject9).startsWith("tg:confirmphone")) && (!((String)localObject9).startsWith("tg://confirmphone"))) {
                        break label4999;
                      }
                      localObject4 = Uri.parse(((String)localObject9).replace("tg:confirmphone", "tg://telegram.org").replace("tg://confirmphone", "tg://telegram.org"));
                      localObject19 = ((Uri)localObject4).getQueryParameter("phone");
                      localObject20 = ((Uri)localObject4).getQueryParameter("hash");
                      localObject12 = str2;
                      localObject14 = localObject22;
                      localObject15 = localObject23;
                      localObject16 = str1;
                      localObject17 = localObject26;
                      localObject7 = localObject28;
                      paramBoolean2 = bool5;
                      localObject8 = localObject27;
                      localObject18 = localInteger6;
                      localObject11 = localInteger1;
                      localObject13 = localInteger2;
                      localObject4 = localObject10;
                    }
                  }
                }
              }
            }
          }
          if (((String)localObject9).startsWith("tg:openmessage")) {
            break label5076;
          }
          localObject12 = str2;
          localObject14 = localObject22;
          localObject15 = localObject23;
          localObject16 = str1;
          localObject17 = localObject26;
          localObject7 = localObject28;
          paramBoolean2 = bool5;
          localObject8 = localObject27;
          localObject18 = localInteger6;
          localObject19 = localObject24;
          localObject20 = localObject25;
          localObject11 = localInteger1;
          localObject13 = localInteger2;
          localObject4 = localObject10;
        } while (!((String)localObject9).startsWith("tg://openmessage"));
        label5076:
        localObject8 = Uri.parse(((String)localObject9).replace("tg:openmessage", "tg://telegram.org").replace("tg://openmessage", "tg://telegram.org"));
        localObject7 = ((Uri)localObject8).getQueryParameter("user_id");
        localObject4 = ((Uri)localObject8).getQueryParameter("chat_id");
        localObject29 = ((Uri)localObject8).getQueryParameter("message_id");
        if (localObject7 == null) {}
      }
      for (;;)
      {
        try
        {
          i = Integer.parseInt((String)localObject7);
          localObject9 = Integer.valueOf(i);
          localObject21 = localInteger1;
        }
        catch (NumberFormatException localNumberFormatException3)
        {
          label5316:
          label6240:
          label7325:
          label7379:
          localObject21 = localInteger1;
          localObject9 = localObject10;
          continue;
        }
        localObject12 = str2;
        localObject14 = localObject22;
        localObject15 = localObject23;
        localObject16 = str1;
        localObject17 = localObject26;
        localObject7 = localObject28;
        paramBoolean2 = bool5;
        localObject8 = localObject27;
        localObject18 = localInteger6;
        localObject19 = localObject24;
        localObject20 = localObject25;
        localObject11 = localObject21;
        localObject13 = localInteger2;
        localObject4 = localObject9;
        if (localObject29 == null) {
          break label3262;
        }
        try
        {
          i = Integer.parseInt((String)localObject29);
          localObject13 = Integer.valueOf(i);
          localObject12 = str2;
          localObject14 = localObject22;
          localObject15 = localObject23;
          localObject16 = str1;
          localObject17 = localObject26;
          localObject7 = localObject28;
          paramBoolean2 = bool5;
          localObject8 = localObject27;
          localObject18 = localInteger6;
          localObject19 = localObject24;
          localObject20 = localObject25;
          localObject11 = localObject21;
          localObject4 = localObject9;
        }
        catch (NumberFormatException localNumberFormatException1)
        {
          localObject12 = str2;
          localObject14 = localObject22;
          localObject15 = localObject23;
          localObject16 = str1;
          localObject17 = localObject26;
          localObject7 = localObject28;
          paramBoolean2 = bool5;
          localObject8 = localObject27;
          localObject18 = localInteger6;
          localObject19 = localObject24;
          localObject20 = localObject25;
          localObject11 = localObject21;
          localObject13 = localInteger2;
          localObject6 = localObject9;
        }
        localObject21 = localInteger1;
        localObject9 = localObject10;
        if (localObject4 != null)
        {
          try
          {
            i = Integer.parseInt((String)localObject4);
            localObject21 = Integer.valueOf(i);
            localObject9 = localObject10;
          }
          catch (NumberFormatException localNumberFormatException2)
          {
            Object localObject5;
            Object localObject6;
            localObject21 = localInteger1;
            localObject9 = localObject10;
          }
          if ((localObject12 != null) || (localObject14 != null) || (localObject15 != null) || (localObject9 != null) || (localObject18 != null) || (0 != 0))
          {
            runLinkRequest(arrayOfInt[0], (String)localObject12, (String)localObject14, (String)localObject15, (String)localObject16, (String)localObject17, (String)localObject9, paramBoolean2, (Integer)localObject8, (String)localObject18, null, 0);
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localObject11;
            localObject16 = localInteger3;
            localObject14 = localObject13;
            localObject7 = localObject4;
            j = n;
            k = i2;
            m = i1;
            break;
          }
          localObject9 = localObject4;
          try
          {
            localObject10 = getContentResolver().query(paramIntent.getData(), null, null, null, null);
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localObject11;
            localObject16 = localInteger3;
            localObject14 = localObject13;
            localObject7 = localObject4;
            j = n;
            k = i2;
            m = i1;
            if (localObject10 == null) {
              break;
            }
            localObject7 = localObject4;
            localObject9 = localObject4;
            if (((Cursor)localObject10).moveToFirst())
            {
              localObject9 = localObject4;
              j = Utilities.parseInt(((Cursor)localObject10).getString(((Cursor)localObject10).getColumnIndex("account_name"))).intValue();
              i = 0;
              if (i < 3)
              {
                localObject9 = localObject4;
                if (UserConfig.getInstance(i).getClientUserId() != j) {
                  continue;
                }
                arrayOfInt[0] = i;
                localObject9 = localObject4;
                switchToAccount(arrayOfInt[0], true);
              }
              localObject9 = localObject4;
              i = ((Cursor)localObject10).getInt(((Cursor)localObject10).getColumnIndex("DATA4"));
              localObject9 = localObject4;
              NotificationCenter.getInstance(arrayOfInt[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              localObject9 = localObject4;
              localObject7 = Integer.valueOf(i);
            }
            localObject9 = localObject7;
            ((Cursor)localObject10).close();
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localObject11;
            localObject16 = localInteger3;
            localObject14 = localObject13;
            j = n;
            k = i2;
            m = i1;
          }
          catch (Exception localException4)
          {
            FileLog.e(localException4);
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localObject11;
            localObject16 = localInteger3;
            localObject14 = localObject13;
            localObject7 = localObject9;
            j = n;
            k = i2;
            m = i1;
          }
          break;
          i += 1;
          continue;
          if (paramIntent.getAction().equals("org.telegram.messenger.OPEN_ACCOUNT"))
          {
            localObject15 = Integer.valueOf(1);
            localObject12 = localInteger5;
            localObject8 = localInteger1;
            localObject16 = localInteger3;
            localObject14 = localInteger2;
            localObject7 = localObject10;
            j = n;
            k = i2;
            m = i1;
            break;
          }
          if (paramIntent.getAction().equals("new_dialog"))
          {
            localObject12 = Integer.valueOf(1);
            localObject15 = localInteger4;
            localObject8 = localInteger1;
            localObject16 = localInteger3;
            localObject14 = localInteger2;
            localObject7 = localObject10;
            j = n;
            k = i2;
            m = i1;
            break;
          }
          if (paramIntent.getAction().startsWith("com.tmessages.openchat"))
          {
            k = paramIntent.getIntExtra("chatId", 0);
            j = paramIntent.getIntExtra("userId", 0);
            i = paramIntent.getIntExtra("encId", 0);
            if (k != 0)
            {
              NotificationCenter.getInstance(arrayOfInt[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              localObject8 = Integer.valueOf(k);
              localObject12 = localInteger5;
              localObject15 = localInteger4;
              localObject16 = localInteger3;
              localObject14 = localInteger2;
              localObject7 = localObject10;
              j = n;
              k = i2;
              m = i1;
              break;
            }
            if (j != 0)
            {
              NotificationCenter.getInstance(arrayOfInt[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              localObject7 = Integer.valueOf(j);
              localObject12 = localInteger5;
              localObject15 = localInteger4;
              localObject8 = localInteger1;
              localObject16 = localInteger3;
              localObject14 = localInteger2;
              j = n;
              k = i2;
              m = i1;
              break;
            }
            if (i != 0)
            {
              NotificationCenter.getInstance(arrayOfInt[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              localObject16 = Integer.valueOf(i);
              localObject12 = localInteger5;
              localObject15 = localInteger4;
              localObject8 = localInteger1;
              localObject14 = localInteger2;
              localObject7 = localObject10;
              j = n;
              k = i2;
              m = i1;
              break;
            }
            j = 1;
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localInteger1;
            localObject16 = localInteger3;
            localObject14 = localInteger2;
            localObject7 = localObject10;
            k = i2;
            m = i1;
            break;
          }
          if (paramIntent.getAction().equals("com.tmessages.openplayer"))
          {
            m = 1;
            localObject12 = localInteger5;
            localObject15 = localInteger4;
            localObject8 = localInteger1;
            localObject16 = localInteger3;
            localObject14 = localInteger2;
            localObject7 = localObject10;
            j = n;
            k = i2;
            break;
          }
          localObject12 = localInteger5;
          localObject15 = localInteger4;
          localObject8 = localInteger1;
          localObject16 = localInteger3;
          localObject14 = localInteger2;
          localObject7 = localObject10;
          j = n;
          k = i2;
          m = i1;
          if (!paramIntent.getAction().equals("org.tmessages.openlocations")) {
            break;
          }
          k = 1;
          localObject12 = localInteger5;
          localObject15 = localInteger4;
          localObject8 = localInteger1;
          localObject16 = localInteger3;
          localObject14 = localInteger2;
          localObject7 = localObject10;
          j = n;
          m = i1;
          break;
          if (((Integer)localObject8).intValue() != 0)
          {
            localObject5 = new Bundle();
            ((Bundle)localObject5).putInt("chat_id", ((Integer)localObject8).intValue());
            if (((Integer)localObject14).intValue() != 0) {
              ((Bundle)localObject5).putInt("message_id", ((Integer)localObject14).intValue());
            }
            if (!mainFragmentsStack.isEmpty())
            {
              paramBoolean2 = bool1;
              paramBoolean3 = paramBoolean1;
              if (!MessagesController.getInstance(arrayOfInt[0]).checkCanOpenChat((Bundle)localObject5, (BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                break label1249;
              }
            }
            localObject5 = new ChatActivity((Bundle)localObject5);
            paramBoolean2 = bool1;
            paramBoolean3 = paramBoolean1;
            if (!this.actionBarLayout.presentFragment((BaseFragment)localObject5, false, true, true)) {
              break label1249;
            }
            paramBoolean2 = true;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          if (((Integer)localObject16).intValue() != 0)
          {
            localObject5 = new Bundle();
            ((Bundle)localObject5).putInt("enc_id", ((Integer)localObject16).intValue());
            localObject5 = new ChatActivity((Bundle)localObject5);
            paramBoolean2 = bool1;
            paramBoolean3 = paramBoolean1;
            if (!this.actionBarLayout.presentFragment((BaseFragment)localObject5, false, true, true)) {
              break label1249;
            }
            paramBoolean2 = true;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          if (j != 0)
          {
            if (!AndroidUtilities.isTablet())
            {
              this.actionBarLayout.removeAllFragments();
              paramBoolean2 = false;
              paramBoolean3 = false;
              break label1249;
            }
            if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
              continue;
            }
            i = 0;
            if (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0)
            {
              this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
              i = i - 1 + 1;
              continue;
            }
            this.layersActionBarLayout.closeLastFragment(false);
            continue;
          }
          if (m != 0)
          {
            if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
              ((BaseFragment)this.actionBarLayout.fragmentsStack.get(0)).showDialog(new AudioPlayerAlert(this));
            }
            paramBoolean2 = false;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          if (k != 0)
          {
            if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
              ((BaseFragment)this.actionBarLayout.fragmentsStack.get(0)).showDialog(new SharingLocationsAlert(this, new SharingLocationsAlert.SharingLocationsAlertDelegate()
              {
                public void didSelectLocation(LocationController.SharingLocationInfo paramAnonymousSharingLocationInfo)
                {
                  arrayOfInt[0] = paramAnonymousSharingLocationInfo.messageObject.currentAccount;
                  LaunchActivity.this.switchToAccount(arrayOfInt[0], true);
                  LocationActivity localLocationActivity = new LocationActivity(2);
                  localLocationActivity.setMessageObject(paramAnonymousSharingLocationInfo.messageObject);
                  localLocationActivity.setDelegate(new LocationActivity.LocationActivityDelegate()
                  {
                    public void didSelectLocation(TLRPC.MessageMedia paramAnonymous2MessageMedia, int paramAnonymous2Int)
                    {
                      SendMessagesHelper.getInstance(LaunchActivity.8.this.val$intentAccount[0]).sendMessage(paramAnonymous2MessageMedia, this.val$dialog_id, null, null, null);
                    }
                  });
                  LaunchActivity.this.presentFragment(localLocationActivity);
                }
              }));
            }
            paramBoolean2 = false;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          if ((this.videoPath != null) || (this.photoPathsArray != null) || (this.sendingText != null) || (this.documentsPathsArray != null) || (this.contactsToSend != null) || (this.documentsUrisArray != null))
          {
            if (!AndroidUtilities.isTablet()) {
              NotificationCenter.getInstance(arrayOfInt[0]).postNotificationName(NotificationCenter.closeChats, new Object[0]);
            }
            if (l == 0L)
            {
              localObject5 = new Bundle();
              ((Bundle)localObject5).putBoolean("onlySelect", true);
              ((Bundle)localObject5).putInt("dialogsType", 3);
              ((Bundle)localObject5).putBoolean("allowSwitchAccount", true);
              if (this.contactsToSend != null)
              {
                ((Bundle)localObject5).putString("selectAlertString", LocaleController.getString("SendContactTo", 2131494349));
                ((Bundle)localObject5).putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", 2131494336));
                localObject5 = new DialogsActivity((Bundle)localObject5);
                ((DialogsActivity)localObject5).setDelegate(this);
                if (!AndroidUtilities.isTablet()) {
                  continue;
                }
                if ((this.layersActionBarLayout.fragmentsStack.size() <= 0) || (!(this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity))) {
                  continue;
                }
                paramBoolean2 = true;
                this.actionBarLayout.presentFragment((BaseFragment)localObject5, paramBoolean2, true, true);
                paramBoolean2 = true;
                if ((!SecretMediaViewer.hasInstance()) || (!SecretMediaViewer.getInstance().isVisible())) {
                  continue;
                }
                SecretMediaViewer.getInstance().closePhoto(false, false);
                this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                if (!AndroidUtilities.isTablet()) {
                  continue;
                }
                this.actionBarLayout.showLastFragment();
                this.rightActionBarLayout.showLastFragment();
                paramBoolean3 = paramBoolean1;
                break label1249;
              }
              ((Bundle)localObject5).putString("selectAlertString", LocaleController.getString("SendMessagesTo", 2131494349));
              ((Bundle)localObject5).putString("selectAlertStringGroup", LocaleController.getString("SendMessagesToGroup", 2131494350));
              continue;
              paramBoolean2 = false;
              continue;
              if ((this.actionBarLayout.fragmentsStack.size() > 1) && ((this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity)))
              {
                paramBoolean2 = true;
                continue;
              }
              paramBoolean2 = false;
              continue;
              if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible()))
              {
                PhotoViewer.getInstance().closePhoto(false, true);
                continue;
              }
              if ((!ArticleViewer.hasInstance()) || (!ArticleViewer.getInstance().isVisible())) {
                continue;
              }
              ArticleViewer.getInstance().close(false, true);
              continue;
              this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
              paramBoolean3 = paramBoolean1;
              break label1249;
            }
            localObject5 = new ArrayList();
            ((ArrayList)localObject5).add(Long.valueOf(l));
            didSelectDialogs(null, (ArrayList)localObject5, null, false);
            paramBoolean2 = bool1;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          if (((Integer)localObject15).intValue() != 0)
          {
            this.actionBarLayout.presentFragment(new SettingsActivity(), false, true, true);
            if (AndroidUtilities.isTablet())
            {
              this.actionBarLayout.showLastFragment();
              this.rightActionBarLayout.showLastFragment();
              this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
              paramBoolean2 = true;
              paramBoolean3 = paramBoolean1;
              break label1249;
            }
            this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            continue;
          }
          paramBoolean2 = bool1;
          paramBoolean3 = paramBoolean1;
          if (((Integer)localObject12).intValue() == 0) {
            break label1249;
          }
          localObject5 = new Bundle();
          ((Bundle)localObject5).putBoolean("destroyAfterSelect", true);
          this.actionBarLayout.presentFragment(new ContactsActivity((Bundle)localObject5), false, true, true);
          if (AndroidUtilities.isTablet())
          {
            this.actionBarLayout.showLastFragment();
            this.rightActionBarLayout.showLastFragment();
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            paramBoolean2 = true;
            paramBoolean3 = paramBoolean1;
            break label1249;
          }
          this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
          continue;
          if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
            break label1314;
          }
          localObject5 = new DialogsActivity(null);
          ((DialogsActivity)localObject5).setSideMenu(this.sideMenu);
          this.actionBarLayout.addFragmentToStack((BaseFragment)localObject5);
          this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
          break label1314;
          if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
            break label1314;
          }
          if (!UserConfig.getInstance(this.currentAccount).isClientActivated())
          {
            this.actionBarLayout.addFragmentToStack(new LoginActivity());
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            break label1314;
          }
          localObject5 = new DialogsActivity(null);
          ((DialogsActivity)localObject5).setSideMenu(this.sideMenu);
          this.actionBarLayout.addFragmentToStack((BaseFragment)localObject5);
          this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
          break label1314;
          break label3262;
        }
      }
      label7559:
      i += 1;
      localObject7 = localObject9;
      break label734;
      l = 0L;
      break;
      label7578:
      i = 0;
      break label1516;
      label7584:
      i = j + 1;
      continue;
      label7593:
      j = 0;
      continue;
      j += 1;
    }
  }
  
  private void onFinish()
  {
    if (this.finished) {
      return;
    }
    this.finished = true;
    if (this.lockRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
      this.lockRunnable = null;
    }
    if (this.currentAccount != -1)
    {
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mainUserInfoChanged);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.needShowAlert);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.openArticle);
      NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.hasNewContactsToImport);
    }
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewWallpapper);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.reloadInterface);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetNewTheme);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.notificationsCountUpdated);
  }
  
  private void onPasscodePause()
  {
    if (this.lockRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
      this.lockRunnable = null;
    }
    if (SharedConfig.passcodeHash.length() != 0)
    {
      SharedConfig.lastPauseTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
      this.lockRunnable = new Runnable()
      {
        public void run()
        {
          if (LaunchActivity.this.lockRunnable == this)
          {
            if (!AndroidUtilities.needShowPasscode(true)) {
              break label46;
            }
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("lock app");
            }
            LaunchActivity.this.showPasscodeActivity();
          }
          for (;;)
          {
            LaunchActivity.access$2202(LaunchActivity.this, null);
            return;
            label46:
            if (BuildVars.LOGS_ENABLED) {
              FileLog.d("didn't pass lock check");
            }
          }
        }
      };
      if (SharedConfig.appLocked) {
        AndroidUtilities.runOnUIThread(this.lockRunnable, 1000L);
      }
    }
    for (;;)
    {
      SharedConfig.saveConfig();
      return;
      if (SharedConfig.autoLockIn != 0)
      {
        AndroidUtilities.runOnUIThread(this.lockRunnable, SharedConfig.autoLockIn * 1000L + 1000L);
        continue;
        SharedConfig.lastPauseTime = 0;
      }
    }
  }
  
  private void onPasscodeResume()
  {
    if (this.lockRunnable != null)
    {
      AndroidUtilities.cancelRunOnUIThread(this.lockRunnable);
      this.lockRunnable = null;
    }
    if (AndroidUtilities.needShowPasscode(true)) {
      showPasscodeActivity();
    }
    if (SharedConfig.lastPauseTime != 0)
    {
      SharedConfig.lastPauseTime = 0;
      SharedConfig.saveConfig();
    }
  }
  
  private void runLinkRequest(final int paramInt1, final String paramString1, final String paramString2, final String paramString3, final String paramString4, final String paramString5, final String paramString6, final boolean paramBoolean, final Integer paramInteger, final String paramString7, final String[] paramArrayOfString, int paramInt2)
  {
    final AlertDialog localAlertDialog = new AlertDialog(this, 1);
    localAlertDialog.setMessage(LocaleController.getString("Loading", 2131493762));
    localAlertDialog.setCanceledOnTouchOutside(false);
    localAlertDialog.setCancelable(false);
    int j = 0;
    final int i;
    if (paramString1 != null)
    {
      paramString2 = new TLRPC.TL_contacts_resolveUsername();
      paramString2.username = paramString1;
      i = ConnectionsManager.getInstance(paramInt1).sendRequest(paramString2, new RequestDelegate()
      {
        public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
        {
          AndroidUtilities.runOnUIThread(new Runnable()
          {
            public void run()
            {
              if (!LaunchActivity.this.isFinishing()) {}
              Object localObject2;
              label1042:
              label1046:
              for (;;)
              {
                try
                {
                  LaunchActivity.9.this.val$progressDialog.dismiss();
                  final TLRPC.TL_contacts_resolvedPeer localTL_contacts_resolvedPeer = (TLRPC.TL_contacts_resolvedPeer)paramAnonymousTLObject;
                  if ((paramAnonymousTL_error != null) || (LaunchActivity.this.actionBarLayout == null) || ((LaunchActivity.9.this.val$game != null) && ((LaunchActivity.9.this.val$game == null) || (localTL_contacts_resolvedPeer.users.isEmpty())))) {
                    break label1099;
                  }
                  MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).putUsers(localTL_contacts_resolvedPeer.users, false);
                  MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).putChats(localTL_contacts_resolvedPeer.chats, false);
                  MessagesStorage.getInstance(LaunchActivity.9.this.val$intentAccount).putUsersAndChats(localTL_contacts_resolvedPeer.users, localTL_contacts_resolvedPeer.chats, false, true);
                  if (LaunchActivity.9.this.val$game != null)
                  {
                    localObject2 = new Bundle();
                    ((Bundle)localObject2).putBoolean("onlySelect", true);
                    ((Bundle)localObject2).putBoolean("cantSendToChannels", true);
                    ((Bundle)localObject2).putInt("dialogsType", 1);
                    ((Bundle)localObject2).putString("selectAlertString", LocaleController.getString("SendGameTo", 2131494337));
                    ((Bundle)localObject2).putString("selectAlertStringGroup", LocaleController.getString("SendGameToGroup", 2131494338));
                    localObject2 = new DialogsActivity((Bundle)localObject2);
                    ((DialogsActivity)localObject2).setDelegate(new DialogsActivity.DialogsActivityDelegate()
                    {
                      public void didSelectDialogs(DialogsActivity paramAnonymous3DialogsActivity, ArrayList<Long> paramAnonymous3ArrayList, CharSequence paramAnonymous3CharSequence, boolean paramAnonymous3Boolean)
                      {
                        long l = ((Long)paramAnonymous3ArrayList.get(0)).longValue();
                        paramAnonymous3ArrayList = new TLRPC.TL_inputMediaGame();
                        paramAnonymous3ArrayList.id = new TLRPC.TL_inputGameShortName();
                        paramAnonymous3ArrayList.id.short_name = LaunchActivity.9.this.val$game;
                        paramAnonymous3ArrayList.id.bot_id = MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).getInputUser((TLRPC.User)localTL_contacts_resolvedPeer.users.get(0));
                        SendMessagesHelper.getInstance(LaunchActivity.9.this.val$intentAccount).sendGame(MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).getInputPeer((int)l), paramAnonymous3ArrayList, 0L, 0L);
                        paramAnonymous3ArrayList = new Bundle();
                        paramAnonymous3ArrayList.putBoolean("scrollToTopOnResume", true);
                        int i = (int)l;
                        int j = (int)(l >> 32);
                        if (i != 0) {
                          if (j == 1) {
                            paramAnonymous3ArrayList.putInt("chat_id", i);
                          }
                        }
                        for (;;)
                        {
                          if (MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).checkCanOpenChat(paramAnonymous3ArrayList, paramAnonymous3DialogsActivity))
                          {
                            NotificationCenter.getInstance(LaunchActivity.9.this.val$intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                            LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(paramAnonymous3ArrayList), true, false, true);
                          }
                          return;
                          if (i > 0)
                          {
                            paramAnonymous3ArrayList.putInt("user_id", i);
                          }
                          else if (i < 0)
                          {
                            paramAnonymous3ArrayList.putInt("chat_id", -i);
                            continue;
                            paramAnonymous3ArrayList.putInt("enc_id", j);
                          }
                        }
                      }
                    });
                    if (AndroidUtilities.isTablet()) {
                      if ((LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() > 0) && ((LaunchActivity.this.layersActionBarLayout.fragmentsStack.get(LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity)))
                      {
                        bool = true;
                        LaunchActivity.this.actionBarLayout.presentFragment((BaseFragment)localObject2, bool, true, true);
                        if ((!SecretMediaViewer.hasInstance()) || (!SecretMediaViewer.getInstance().isVisible())) {
                          continue;
                        }
                        SecretMediaViewer.getInstance().closePhoto(false, false);
                        LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
                        if (!AndroidUtilities.isTablet()) {
                          continue;
                        }
                        LaunchActivity.this.actionBarLayout.showLastFragment();
                        LaunchActivity.this.rightActionBarLayout.showLastFragment();
                        return;
                      }
                    }
                  }
                }
                catch (Exception localException1)
                {
                  FileLog.e(localException1);
                  continue;
                  boolean bool = false;
                  continue;
                  if ((LaunchActivity.this.actionBarLayout.fragmentsStack.size() > 1) && ((LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity)))
                  {
                    bool = true;
                    continue;
                  }
                  bool = false;
                  continue;
                  if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible()))
                  {
                    PhotoViewer.getInstance().closePhoto(false, true);
                    continue;
                  }
                  if ((!ArticleViewer.hasInstance()) || (!ArticleViewer.getInstance().isVisible())) {
                    continue;
                  }
                  ArticleViewer.getInstance().close(false, true);
                  continue;
                  LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
                  return;
                }
                if (LaunchActivity.9.this.val$botChat != null)
                {
                  if (!localException1.users.isEmpty()) {}
                  for (TLRPC.User localUser = (TLRPC.User)localException1.users.get(0); (localUser == null) || ((localUser.bot) && (localUser.bot_nochats)); localObject1 = null) {
                    try
                    {
                      Toast.makeText(LaunchActivity.this, LocaleController.getString("BotCantJoinGroups", 2131493087), 0).show();
                      return;
                    }
                    catch (Exception localException2)
                    {
                      FileLog.e(localException2);
                      return;
                    }
                  }
                  localObject2 = new Bundle();
                  ((Bundle)localObject2).putBoolean("onlySelect", true);
                  ((Bundle)localObject2).putInt("dialogsType", 2);
                  ((Bundle)localObject2).putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", 2131492946, new Object[] { UserObject.getUserName((TLRPC.User)localObject1), "%1$s" }));
                  localObject2 = new DialogsActivity((Bundle)localObject2);
                  ((DialogsActivity)localObject2).setDelegate(new DialogsActivity.DialogsActivityDelegate()
                  {
                    public void didSelectDialogs(DialogsActivity paramAnonymous3DialogsActivity, ArrayList<Long> paramAnonymous3ArrayList, CharSequence paramAnonymous3CharSequence, boolean paramAnonymous3Boolean)
                    {
                      long l = ((Long)paramAnonymous3ArrayList.get(0)).longValue();
                      paramAnonymous3DialogsActivity = new Bundle();
                      paramAnonymous3DialogsActivity.putBoolean("scrollToTopOnResume", true);
                      paramAnonymous3DialogsActivity.putInt("chat_id", -(int)l);
                      if ((LaunchActivity.mainFragmentsStack.isEmpty()) || (MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).checkCanOpenChat(paramAnonymous3DialogsActivity, (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))))
                      {
                        NotificationCenter.getInstance(LaunchActivity.9.this.val$intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                        MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).addUserToChat(-(int)l, localObject1, null, 0, LaunchActivity.9.this.val$botChat, null);
                        LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(paramAnonymous3DialogsActivity), true, false, true);
                      }
                    }
                  });
                  LaunchActivity.this.presentFragment((BaseFragment)localObject2);
                  return;
                }
                int j = 0;
                localObject2 = new Bundle();
                long l;
                int i;
                if (!((TLRPC.TL_contacts_resolvedPeer)localObject1).chats.isEmpty())
                {
                  ((Bundle)localObject2).putInt("chat_id", ((TLRPC.Chat)((TLRPC.TL_contacts_resolvedPeer)localObject1).chats.get(0)).id);
                  l = -((TLRPC.Chat)((TLRPC.TL_contacts_resolvedPeer)localObject1).chats.get(0)).id;
                  i = j;
                  if (LaunchActivity.9.this.val$botUser != null)
                  {
                    i = j;
                    if (((TLRPC.TL_contacts_resolvedPeer)localObject1).users.size() > 0)
                    {
                      i = j;
                      if (((TLRPC.User)((TLRPC.TL_contacts_resolvedPeer)localObject1).users.get(0)).bot)
                      {
                        ((Bundle)localObject2).putString("botUser", LaunchActivity.9.this.val$botUser);
                        i = 1;
                      }
                    }
                  }
                  if (LaunchActivity.9.this.val$messageId != null) {
                    ((Bundle)localObject2).putInt("message_id", LaunchActivity.9.this.val$messageId.intValue());
                  }
                  if (LaunchActivity.mainFragmentsStack.isEmpty()) {
                    break label1042;
                  }
                }
                for (localObject1 = (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1);; localObject1 = null)
                {
                  if ((localObject1 != null) && (!MessagesController.getInstance(LaunchActivity.9.this.val$intentAccount).checkCanOpenChat((Bundle)localObject2, (BaseFragment)localObject1))) {
                    break label1046;
                  }
                  if ((i == 0) || (localObject1 == null) || (!(localObject1 instanceof ChatActivity)) || (((ChatActivity)localObject1).getDialogId() != l)) {
                    break label1048;
                  }
                  ((ChatActivity)localObject1).setBotUser(LaunchActivity.9.this.val$botUser);
                  return;
                  ((Bundle)localObject2).putInt("user_id", ((TLRPC.User)((TLRPC.TL_contacts_resolvedPeer)localObject1).users.get(0)).id);
                  l = ((TLRPC.User)((TLRPC.TL_contacts_resolvedPeer)localObject1).users.get(0)).id;
                  break;
                }
              }
              label1048:
              final Object localObject1 = new ChatActivity((Bundle)localObject2);
              NotificationCenter.getInstance(LaunchActivity.9.this.val$intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
              LaunchActivity.this.actionBarLayout.presentFragment((BaseFragment)localObject1, false, true, true);
              return;
              try
              {
                label1099:
                Toast.makeText(LaunchActivity.this, LocaleController.getString("NoUsernameFound", 2131493916), 0).show();
                return;
              }
              catch (Exception localException3)
              {
                FileLog.e(localException3);
              }
            }
          });
        }
      });
    }
    for (;;)
    {
      if (i != 0) {
        localAlertDialog.setButton(-2, LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
          {
            ConnectionsManager.getInstance(paramInt1).cancelRequest(i, true);
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
      }
      do
      {
        try
        {
          localAlertDialog.show();
          return;
        }
        catch (Exception paramString1) {}
        if (paramString2 != null)
        {
          if (paramInt2 == 0)
          {
            TLRPC.TL_messages_checkChatInvite localTL_messages_checkChatInvite = new TLRPC.TL_messages_checkChatInvite();
            localTL_messages_checkChatInvite.hash = paramString2;
            i = ConnectionsManager.getInstance(paramInt1).sendRequest(localTL_messages_checkChatInvite, new RequestDelegate()
            {
              public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
              {
                AndroidUtilities.runOnUIThread(new Runnable()
                {
                  public void run()
                  {
                    if (!LaunchActivity.this.isFinishing()) {}
                    Object localObject2;
                    try
                    {
                      LaunchActivity.10.this.val$progressDialog.dismiss();
                      if ((paramAnonymousTL_error == null) && (LaunchActivity.this.actionBarLayout != null))
                      {
                        Object localObject1 = (TLRPC.ChatInvite)paramAnonymousTLObject;
                        if ((((TLRPC.ChatInvite)localObject1).chat != null) && (!ChatObject.isLeftFromChat(((TLRPC.ChatInvite)localObject1).chat)))
                        {
                          MessagesController.getInstance(LaunchActivity.10.this.val$intentAccount).putChat(((TLRPC.ChatInvite)localObject1).chat, false);
                          localObject3 = new ArrayList();
                          ((ArrayList)localObject3).add(((TLRPC.ChatInvite)localObject1).chat);
                          MessagesStorage.getInstance(LaunchActivity.10.this.val$intentAccount).putUsersAndChats(null, (ArrayList)localObject3, false, true);
                          localObject3 = new Bundle();
                          ((Bundle)localObject3).putInt("chat_id", ((TLRPC.ChatInvite)localObject1).chat.id);
                          if ((LaunchActivity.mainFragmentsStack.isEmpty()) || (MessagesController.getInstance(LaunchActivity.10.this.val$intentAccount).checkCanOpenChat((Bundle)localObject3, (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))))
                          {
                            localObject1 = new ChatActivity((Bundle)localObject3);
                            NotificationCenter.getInstance(LaunchActivity.10.this.val$intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                            LaunchActivity.this.actionBarLayout.presentFragment((BaseFragment)localObject1, false, true, true);
                          }
                          return;
                        }
                      }
                    }
                    catch (Exception localException)
                    {
                      for (;;)
                      {
                        FileLog.e(localException);
                      }
                      if (((localException.chat == null) && ((!localException.channel) || (localException.megagroup))) || ((localException.chat != null) && ((!ChatObject.isChannel(localException.chat)) || (localException.chat.megagroup)) && (!LaunchActivity.mainFragmentsStack.isEmpty())))
                      {
                        localObject3 = (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1);
                        ((BaseFragment)localObject3).showDialog(new JoinGroupAlert(LaunchActivity.this, localException, LaunchActivity.10.this.val$group, (BaseFragment)localObject3));
                        return;
                      }
                      Object localObject3 = new AlertDialog.Builder(LaunchActivity.this);
                      ((AlertDialog.Builder)localObject3).setTitle(LocaleController.getString("AppName", 2131492981));
                      if (localException.chat != null) {}
                      for (localObject2 = localException.chat.title;; localObject2 = ((TLRPC.ChatInvite)localObject2).title)
                      {
                        ((AlertDialog.Builder)localObject3).setMessage(LocaleController.formatString("ChannelJoinTo", 2131493171, new Object[] { localObject2 }));
                        ((AlertDialog.Builder)localObject3).setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
                        {
                          public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                          {
                            LaunchActivity.this.runLinkRequest(LaunchActivity.10.this.val$intentAccount, LaunchActivity.10.this.val$username, LaunchActivity.10.this.val$group, LaunchActivity.10.this.val$sticker, LaunchActivity.10.this.val$botUser, LaunchActivity.10.this.val$botChat, LaunchActivity.10.this.val$message, LaunchActivity.10.this.val$hasUrl, LaunchActivity.10.this.val$messageId, LaunchActivity.10.this.val$game, LaunchActivity.10.this.val$instantView, 1);
                          }
                        });
                        ((AlertDialog.Builder)localObject3).setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
                        LaunchActivity.this.showAlertDialog((AlertDialog.Builder)localObject3);
                        return;
                      }
                      localObject2 = new AlertDialog.Builder(LaunchActivity.this);
                      ((AlertDialog.Builder)localObject2).setTitle(LocaleController.getString("AppName", 2131492981));
                      if (!paramAnonymousTL_error.text.startsWith("FLOOD_WAIT")) {
                        break label540;
                      }
                    }
                    ((AlertDialog.Builder)localObject2).setMessage(LocaleController.getString("FloodWait", 2131493543));
                    for (;;)
                    {
                      ((AlertDialog.Builder)localObject2).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                      LaunchActivity.this.showAlertDialog((AlertDialog.Builder)localObject2);
                      return;
                      label540:
                      ((AlertDialog.Builder)localObject2).setMessage(LocaleController.getString("JoinToGroupErrorNotExist", 2131493712));
                    }
                  }
                });
              }
            }, 2);
            break;
          }
          i = j;
          if (paramInt2 != 1) {
            break;
          }
          paramString1 = new TLRPC.TL_messages_importChatInvite();
          paramString1.hash = paramString2;
          ConnectionsManager.getInstance(paramInt1).sendRequest(paramString1, new RequestDelegate()
          {
            public void run(final TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
            {
              if (paramAnonymousTL_error == null)
              {
                TLRPC.Updates localUpdates = (TLRPC.Updates)paramAnonymousTLObject;
                MessagesController.getInstance(paramInt1).processUpdates(localUpdates, false);
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  if (!LaunchActivity.this.isFinishing()) {}
                  AlertDialog.Builder localBuilder;
                  try
                  {
                    LaunchActivity.11.this.val$progressDialog.dismiss();
                    if (paramAnonymousTL_error == null)
                    {
                      if (LaunchActivity.this.actionBarLayout != null)
                      {
                        Object localObject2 = (TLRPC.Updates)paramAnonymousTLObject;
                        if (!((TLRPC.Updates)localObject2).chats.isEmpty())
                        {
                          Object localObject1 = (TLRPC.Chat)((TLRPC.Updates)localObject2).chats.get(0);
                          ((TLRPC.Chat)localObject1).left = false;
                          ((TLRPC.Chat)localObject1).kicked = false;
                          MessagesController.getInstance(LaunchActivity.11.this.val$intentAccount).putUsers(((TLRPC.Updates)localObject2).users, false);
                          MessagesController.getInstance(LaunchActivity.11.this.val$intentAccount).putChats(((TLRPC.Updates)localObject2).chats, false);
                          localObject2 = new Bundle();
                          ((Bundle)localObject2).putInt("chat_id", ((TLRPC.Chat)localObject1).id);
                          if ((LaunchActivity.mainFragmentsStack.isEmpty()) || (MessagesController.getInstance(LaunchActivity.11.this.val$intentAccount).checkCanOpenChat((Bundle)localObject2, (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))))
                          {
                            localObject1 = new ChatActivity((Bundle)localObject2);
                            NotificationCenter.getInstance(LaunchActivity.11.this.val$intentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                            LaunchActivity.this.actionBarLayout.presentFragment((BaseFragment)localObject1, false, true, true);
                          }
                        }
                      }
                      return;
                    }
                  }
                  catch (Exception localException)
                  {
                    for (;;)
                    {
                      FileLog.e(localException);
                    }
                    localBuilder = new AlertDialog.Builder(LaunchActivity.this);
                    localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
                    if (!paramAnonymousTL_error.text.startsWith("FLOOD_WAIT")) {
                      break label316;
                    }
                  }
                  localBuilder.setMessage(LocaleController.getString("FloodWait", 2131493543));
                  for (;;)
                  {
                    localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
                    LaunchActivity.this.showAlertDialog(localBuilder);
                    return;
                    label316:
                    if (paramAnonymousTL_error.text.equals("USERS_TOO_MUCH")) {
                      localBuilder.setMessage(LocaleController.getString("JoinToGroupErrorFull", 2131493711));
                    } else {
                      localBuilder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", 2131493712));
                    }
                  }
                }
              });
            }
          }, 2);
          i = j;
          break;
        }
        if (paramString3 == null) {
          break label307;
        }
      } while (mainFragmentsStack.isEmpty());
      paramString1 = new TLRPC.TL_inputStickerSetShortName();
      paramString1.short_name = paramString3;
      paramString2 = (BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1);
      paramString2.showDialog(new StickersAlert(this, paramString2, paramString1, null, null));
      return;
      label307:
      if (paramString6 != null)
      {
        paramString1 = new Bundle();
        paramString1.putBoolean("onlySelect", true);
        paramString1 = new DialogsActivity(paramString1);
        paramString1.setDelegate(new DialogsActivity.DialogsActivityDelegate()
        {
          public void didSelectDialogs(DialogsActivity paramAnonymousDialogsActivity, ArrayList<Long> paramAnonymousArrayList, CharSequence paramAnonymousCharSequence, boolean paramAnonymousBoolean)
          {
            long l = ((Long)paramAnonymousArrayList.get(0)).longValue();
            paramAnonymousArrayList = new Bundle();
            paramAnonymousArrayList.putBoolean("scrollToTopOnResume", true);
            paramAnonymousArrayList.putBoolean("hasUrl", paramBoolean);
            int i = (int)l;
            int j = (int)(l >> 32);
            if (i != 0) {
              if (j == 1) {
                paramAnonymousArrayList.putInt("chat_id", i);
              }
            }
            for (;;)
            {
              if (MessagesController.getInstance(paramInt1).checkCanOpenChat(paramAnonymousArrayList, paramAnonymousDialogsActivity))
              {
                NotificationCenter.getInstance(paramInt1).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                DataQuery.getInstance(paramInt1).saveDraft(l, paramString6, null, null, false);
                LaunchActivity.this.actionBarLayout.presentFragment(new ChatActivity(paramAnonymousArrayList), true, false, true);
              }
              return;
              if (i > 0)
              {
                paramAnonymousArrayList.putInt("user_id", i);
              }
              else if (i < 0)
              {
                paramAnonymousArrayList.putInt("chat_id", -i);
                continue;
                paramAnonymousArrayList.putInt("enc_id", j);
              }
            }
          }
        });
        presentFragment(paramString1, false, true);
        i = j;
      }
      else
      {
        i = j;
        if (paramArrayOfString != null) {
          i = j;
        }
      }
    }
  }
  
  private void showLanguageAlert(boolean paramBoolean)
  {
    final String str3;
    try
    {
      if (this.loadingLocaleDialog) {
        return;
      }
      String str1 = MessagesController.getGlobalMainSettings().getString("language_showed2", "");
      str3 = LocaleController.getSystemLocaleStringIso639().toLowerCase();
      if ((!paramBoolean) && (str1.equals(str3)))
      {
        if (!BuildVars.LOGS_ENABLED) {
          break label565;
        }
        FileLog.d("alert already showed for " + str1);
        return;
      }
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
      return;
    }
    final LocaleController.LocaleInfo[] arrayOfLocaleInfo = new LocaleController.LocaleInfo[2];
    if (str3.contains("-")) {}
    for (String str2 = str3.split("-")[0];; str2 = str3)
    {
      Object localObject;
      int i;
      LocaleController.LocaleInfo localLocaleInfo;
      if ("in".equals(str2))
      {
        localObject = "id";
        break label566;
        if (i < LocaleController.getInstance().languages.size())
        {
          localLocaleInfo = (LocaleController.LocaleInfo)LocaleController.getInstance().languages.get(i);
          if (localLocaleInfo.shortName.equals("en")) {
            arrayOfLocaleInfo[0] = localLocaleInfo;
          }
          if ((localLocaleInfo.shortName.replace("_", "-").equals(str3)) || (localLocaleInfo.shortName.equals(str2))) {
            break label571;
          }
          if ((localObject == null) || (!localLocaleInfo.shortName.equals(localObject))) {
            break label577;
          }
          break label571;
        }
      }
      for (;;)
      {
        if ((arrayOfLocaleInfo[0] != null) && (arrayOfLocaleInfo[1] != null) && (arrayOfLocaleInfo[0] != arrayOfLocaleInfo[1]))
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("show lang alert for " + arrayOfLocaleInfo[0].getKey() + " and " + arrayOfLocaleInfo[1].getKey());
          }
          this.systemLocaleStrings = null;
          this.englishLocaleStrings = null;
          this.loadingLocaleDialog = true;
          localObject = new TLRPC.TL_langpack_getStrings();
          ((TLRPC.TL_langpack_getStrings)localObject).lang_code = arrayOfLocaleInfo[1].shortName.replace("_", "-");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("English");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChooseYourLanguage");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChooseYourLanguageOther");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChangeLanguageLater");
          ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
            {
              paramAnonymousTL_error = new HashMap();
              if (paramAnonymousTLObject != null)
              {
                paramAnonymousTLObject = (TLRPC.Vector)paramAnonymousTLObject;
                int i = 0;
                while (i < paramAnonymousTLObject.objects.size())
                {
                  TLRPC.LangPackString localLangPackString = (TLRPC.LangPackString)paramAnonymousTLObject.objects.get(i);
                  paramAnonymousTL_error.put(localLangPackString.key, localLangPackString.value);
                  i += 1;
                }
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  LaunchActivity.access$1902(LaunchActivity.this, paramAnonymousTL_error);
                  if ((LaunchActivity.this.englishLocaleStrings != null) && (LaunchActivity.this.systemLocaleStrings != null)) {
                    LaunchActivity.this.showLanguageAlertInternal(LaunchActivity.27.this.val$infos[1], LaunchActivity.27.this.val$infos[0], LaunchActivity.27.this.val$systemLang);
                  }
                }
              });
            }
          }, 8);
          localObject = new TLRPC.TL_langpack_getStrings();
          ((TLRPC.TL_langpack_getStrings)localObject).lang_code = arrayOfLocaleInfo[0].shortName.replace("_", "-");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("English");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChooseYourLanguage");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChooseYourLanguageOther");
          ((TLRPC.TL_langpack_getStrings)localObject).keys.add("ChangeLanguageLater");
          ConnectionsManager.getInstance(this.currentAccount).sendRequest((TLObject)localObject, new RequestDelegate()
          {
            public void run(TLObject paramAnonymousTLObject, final TLRPC.TL_error paramAnonymousTL_error)
            {
              paramAnonymousTL_error = new HashMap();
              if (paramAnonymousTLObject != null)
              {
                paramAnonymousTLObject = (TLRPC.Vector)paramAnonymousTLObject;
                int i = 0;
                while (i < paramAnonymousTLObject.objects.size())
                {
                  TLRPC.LangPackString localLangPackString = (TLRPC.LangPackString)paramAnonymousTLObject.objects.get(i);
                  paramAnonymousTL_error.put(localLangPackString.key, localLangPackString.value);
                  i += 1;
                }
              }
              AndroidUtilities.runOnUIThread(new Runnable()
              {
                public void run()
                {
                  LaunchActivity.access$1802(LaunchActivity.this, paramAnonymousTL_error);
                  if ((LaunchActivity.this.englishLocaleStrings != null) && (LaunchActivity.this.systemLocaleStrings != null)) {
                    LaunchActivity.this.showLanguageAlertInternal(LaunchActivity.28.this.val$infos[1], LaunchActivity.28.this.val$infos[0], LaunchActivity.28.this.val$systemLang);
                  }
                }
              });
            }
          }, 8);
          return;
          if ("iw".equals(str2)) {
            localObject = "he";
          } else if ("jw".equals(str2)) {
            localObject = "jv";
          } else {
            localObject = null;
          }
        }
        label565:
        label566:
        label571:
        label577:
        do
        {
          i += 1;
          break;
          return;
          i = 0;
          break;
          arrayOfLocaleInfo[1] = localLocaleInfo;
        } while ((arrayOfLocaleInfo[0] == null) || (arrayOfLocaleInfo[1] == null));
      }
    }
  }
  
  private void showLanguageAlertInternal(LocaleController.LocaleInfo paramLocaleInfo1, LocaleController.LocaleInfo paramLocaleInfo2, String paramString)
  {
    int i;
    try
    {
      this.loadingLocaleDialog = false;
      if (paramLocaleInfo1.builtIn) {
        break label425;
      }
      if (!LocaleController.getInstance().isCurrentLocalLocale()) {
        break label467;
      }
    }
    catch (Exception paramLocaleInfo1)
    {
      AlertDialog.Builder localBuilder;
      LinearLayout localLinearLayout;
      final LanguageCell[] arrayOfLanguageCell;
      String str;
      LocaleController.LocaleInfo localLocaleInfo;
      FileLog.e(paramLocaleInfo1);
      return;
    }
    localBuilder = new AlertDialog.Builder(this);
    localBuilder.setTitle(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguage", 2131493253));
    localBuilder.setSubtitle(getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguage", 2131493253));
    localLinearLayout = new LinearLayout(this);
    localLinearLayout.setOrientation(1);
    arrayOfLanguageCell = new LanguageCell[2];
    final LocaleController.LocaleInfo[] arrayOfLocaleInfo1 = new LocaleController.LocaleInfo[1];
    LocaleController.LocaleInfo[] arrayOfLocaleInfo2 = new LocaleController.LocaleInfo[2];
    str = getStringForLanguageAlert(this.systemLocaleStrings, "English", 2131493443);
    Object localObject;
    if (i != 0)
    {
      localObject = paramLocaleInfo1;
      break label431;
      label135:
      if (i < 2)
      {
        arrayOfLanguageCell[i] = new LanguageCell(this, true);
        localObject = arrayOfLanguageCell[i];
        localLocaleInfo = arrayOfLocaleInfo2[i];
        if (arrayOfLocaleInfo2[i] != paramLocaleInfo2) {
          break label490;
        }
        paramLocaleInfo1 = str;
        label181:
        ((LanguageCell)localObject).setLanguage(localLocaleInfo, paramLocaleInfo1, true);
        arrayOfLanguageCell[i].setTag(Integer.valueOf(i));
        arrayOfLanguageCell[i].setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor("dialogButtonSelector"), 2));
        paramLocaleInfo1 = arrayOfLanguageCell[i];
        if (i != 0) {
          break label495;
        }
      }
    }
    label425:
    label431:
    label445:
    label467:
    label485:
    label490:
    label495:
    for (boolean bool = true;; bool = false)
    {
      paramLocaleInfo1.setLanguageSelected(bool);
      localLinearLayout.addView(arrayOfLanguageCell[i], LayoutHelper.createLinear(-1, 48));
      arrayOfLanguageCell[i].setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          Integer localInteger = (Integer)paramAnonymousView.getTag();
          arrayOfLocaleInfo1[0] = ((LanguageCell)paramAnonymousView).getCurrentLocale();
          int i = 0;
          if (i < arrayOfLanguageCell.length)
          {
            paramAnonymousView = arrayOfLanguageCell[i];
            if (i == localInteger.intValue()) {}
            for (boolean bool = true;; bool = false)
            {
              paramAnonymousView.setLanguageSelected(bool);
              i += 1;
              break;
            }
          }
        }
      });
      i += 1;
      break label135;
      paramLocaleInfo1 = new LanguageCell(this, true);
      paramLocaleInfo1.setValue(getStringForLanguageAlert(this.systemLocaleStrings, "ChooseYourLanguageOther", 2131493254), getStringForLanguageAlert(this.englishLocaleStrings, "ChooseYourLanguageOther", 2131493254));
      paramLocaleInfo1.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          LaunchActivity.access$1702(LaunchActivity.this, null);
          LaunchActivity.this.drawerLayoutContainer.closeDrawer(true);
          LaunchActivity.this.presentFragment(new LanguageSelectActivity());
          if (LaunchActivity.this.visibleDialog != null)
          {
            LaunchActivity.this.visibleDialog.dismiss();
            LaunchActivity.access$1602(LaunchActivity.this, null);
          }
        }
      });
      localLinearLayout.addView(paramLocaleInfo1, LayoutHelper.createLinear(-1, 48));
      localBuilder.setView(localLinearLayout);
      localBuilder.setNegativeButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          LocaleController.getInstance().applyLanguage(arrayOfLocaleInfo1[0], true, false, LaunchActivity.this.currentAccount);
          LaunchActivity.this.rebuildAllFragments(true);
        }
      });
      this.localeDialog = showAlertDialog(localBuilder);
      MessagesController.getGlobalMainSettings().edit().putString("language_showed2", paramString).commit();
      return;
      i = 1;
      break;
      arrayOfLocaleInfo2[0] = localObject;
      if (i != 0)
      {
        localObject = paramLocaleInfo2;
        arrayOfLocaleInfo2[1] = localObject;
        if (i == 0) {
          break label485;
        }
      }
      for (;;)
      {
        arrayOfLocaleInfo1[0] = paramLocaleInfo1;
        i = 0;
        break label135;
        i = 0;
        break;
        localObject = paramLocaleInfo2;
        break label431;
        localObject = paramLocaleInfo1;
        break label445;
        paramLocaleInfo1 = paramLocaleInfo2;
      }
      paramLocaleInfo1 = null;
      break label181;
    }
  }
  
  private void showPasscodeActivity()
  {
    if (this.passcodeView == null) {
      return;
    }
    SharedConfig.appLocked = true;
    if ((SecretMediaViewer.hasInstance()) && (SecretMediaViewer.getInstance().isVisible())) {
      SecretMediaViewer.getInstance().closePhoto(false, false);
    }
    for (;;)
    {
      this.passcodeView.onShow();
      SharedConfig.isWaitingForPasscodeEnter = true;
      this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
      this.passcodeView.setDelegate(new PasscodeView.PasscodeViewDelegate()
      {
        public void didAcceptedPassword()
        {
          SharedConfig.isWaitingForPasscodeEnter = false;
          if (LaunchActivity.this.passcodeSaveIntent != null)
          {
            LaunchActivity.this.handleIntent(LaunchActivity.this.passcodeSaveIntent, LaunchActivity.this.passcodeSaveIntentIsNew, LaunchActivity.this.passcodeSaveIntentIsRestore, true);
            LaunchActivity.access$902(LaunchActivity.this, null);
          }
          LaunchActivity.this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
          LaunchActivity.this.actionBarLayout.showLastFragment();
          if (AndroidUtilities.isTablet())
          {
            LaunchActivity.this.layersActionBarLayout.showLastFragment();
            LaunchActivity.this.rightActionBarLayout.showLastFragment();
          }
        }
      });
      return;
      if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible())) {
        PhotoViewer.getInstance().closePhoto(false, true);
      } else if ((ArticleViewer.hasInstance()) && (ArticleViewer.getInstance().isVisible())) {
        ArticleViewer.getInstance().close(false, true);
      }
    }
  }
  
  private void switchToAvailableAccountOrLogout()
  {
    int k = -1;
    int i = 0;
    for (;;)
    {
      int j = k;
      if (i < 3)
      {
        if (UserConfig.getInstance(i).isClientActivated()) {
          j = i;
        }
      }
      else
      {
        if (j == -1) {
          break;
        }
        switchToAccount(j, true);
        return;
      }
      i += 1;
    }
    if (this.drawerLayoutAdapter != null) {
      this.drawerLayoutAdapter.notifyDataSetChanged();
    }
    Iterator localIterator = this.actionBarLayout.fragmentsStack.iterator();
    while (localIterator.hasNext()) {
      ((BaseFragment)localIterator.next()).onFragmentDestroy();
    }
    this.actionBarLayout.fragmentsStack.clear();
    if (AndroidUtilities.isTablet())
    {
      localIterator = this.layersActionBarLayout.fragmentsStack.iterator();
      while (localIterator.hasNext()) {
        ((BaseFragment)localIterator.next()).onFragmentDestroy();
      }
      this.layersActionBarLayout.fragmentsStack.clear();
      localIterator = this.rightActionBarLayout.fragmentsStack.iterator();
      while (localIterator.hasNext()) {
        ((BaseFragment)localIterator.next()).onFragmentDestroy();
      }
      this.rightActionBarLayout.fragmentsStack.clear();
    }
    startActivity(new Intent(this, IntroActivity.class));
    onFinish();
    finish();
  }
  
  private void updateCurrentConnectionState(int paramInt)
  {
    if (this.actionBarLayout == null) {
      return;
    }
    String str1 = null;
    String str2 = null;
    Object localObject = null;
    if (this.currentConnectionState == 2) {
      str1 = LocaleController.getString("WaitingForNetwork", 2131494621);
    }
    for (;;)
    {
      this.actionBarLayout.setTitleOverlayText(str1, str2, (Runnable)localObject);
      return;
      if (this.currentConnectionState == 1)
      {
        str1 = LocaleController.getString("Connecting", 2131493282);
        localObject = new Runnable()
        {
          public void run()
          {
            if (AndroidUtilities.isTablet())
            {
              if ((LaunchActivity.layerFragmentsStack.isEmpty()) || (!(LaunchActivity.layerFragmentsStack.get(LaunchActivity.layerFragmentsStack.size() - 1) instanceof ProxySettingsActivity))) {}
            }
            else {
              while ((!LaunchActivity.mainFragmentsStack.isEmpty()) && ((LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1) instanceof ProxySettingsActivity))) {
                return;
              }
            }
            LaunchActivity.this.presentFragment(new ProxySettingsActivity());
          }
        };
      }
      else if (this.currentConnectionState == 5)
      {
        str1 = LocaleController.getString("Updating", 2131494527);
      }
      else if (this.currentConnectionState == 4)
      {
        str1 = LocaleController.getString("ConnectingToProxy", 2131493283);
        str2 = LocaleController.getString("ConnectingToProxyTapToDisable", 2131493287);
        localObject = new Runnable()
        {
          public void run()
          {
            if ((LaunchActivity.this.actionBarLayout == null) || (LaunchActivity.this.actionBarLayout.fragmentsStack.isEmpty())) {
              return;
            }
            SharedPreferences localSharedPreferences = MessagesController.getGlobalMainSettings();
            BaseFragment localBaseFragment = (BaseFragment)LaunchActivity.this.actionBarLayout.fragmentsStack.get(LaunchActivity.this.actionBarLayout.fragmentsStack.size() - 1);
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(LaunchActivity.this);
            localBuilder.setTitle(LocaleController.getString("Proxy", 2131494206));
            localBuilder.setMessage(LocaleController.formatString("ConnectingToProxyDisableAlert", 2131493285, new Object[] { localSharedPreferences.getString("proxy_ip", "") }));
            localBuilder.setPositiveButton(LocaleController.getString("ConnectingToProxyDisable", 2131493284), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                paramAnonymous2DialogInterface = MessagesController.getGlobalMainSettings().edit();
                paramAnonymous2DialogInterface.putBoolean("proxy_enabled", false);
                paramAnonymous2DialogInterface.commit();
                paramAnonymous2Int = 0;
                while (paramAnonymous2Int < 3)
                {
                  ConnectionsManager.native_setProxySettings(paramAnonymous2Int, "", 0, "", "");
                  paramAnonymous2Int += 1;
                }
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged, new Object[0]);
              }
            });
            localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            localBaseFragment.showDialog(localBuilder.create());
          }
        };
      }
    }
  }
  
  public void didReceivedNotification(int paramInt1, final int paramInt2, final Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.appDidLogout) {
      switchToAvailableAccountOrLogout();
    }
    for (;;)
    {
      return;
      if (paramInt1 == NotificationCenter.closeOtherAppActivities)
      {
        if (paramVarArgs[0] != this)
        {
          onFinish();
          finish();
        }
      }
      else if (paramInt1 == NotificationCenter.didUpdatedConnectionState)
      {
        paramInt1 = ConnectionsManager.getInstance(paramInt2).getConnectionState();
        if (this.currentConnectionState != paramInt1)
        {
          if (BuildVars.LOGS_ENABLED) {
            FileLog.d("switch to state " + paramInt1);
          }
          this.currentConnectionState = paramInt1;
          updateCurrentConnectionState(paramInt2);
        }
      }
      else
      {
        if (paramInt1 == NotificationCenter.mainUserInfoChanged)
        {
          this.drawerLayoutAdapter.notifyDataSetChanged();
          return;
        }
        final Object localObject;
        AlertDialog.Builder localBuilder;
        if (paramInt1 == NotificationCenter.needShowAlert)
        {
          localObject = (Integer)paramVarArgs[0];
          localBuilder = new AlertDialog.Builder(this);
          localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
          localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
          if (((Integer)localObject).intValue() != 2) {
            localBuilder.setNegativeButton(LocaleController.getString("MoreInfo", 2131493855), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
              {
                if (!LaunchActivity.mainFragmentsStack.isEmpty()) {
                  MessagesController.getInstance(paramInt2).openByUserName("spambot", (BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1), 1);
                }
              }
            });
          }
          if (((Integer)localObject).intValue() == 0) {
            localBuilder.setMessage(LocaleController.getString("NobodyLikesSpam1", 2131493917));
          }
          while (!mainFragmentsStack.isEmpty())
          {
            ((BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(localBuilder.create());
            return;
            if (((Integer)localObject).intValue() == 1) {
              localBuilder.setMessage(LocaleController.getString("NobodyLikesSpam2", 2131493918));
            } else if (((Integer)localObject).intValue() == 2) {
              localBuilder.setMessage((String)paramVarArgs[1]);
            }
          }
        }
        else if (paramInt1 == NotificationCenter.wasUnableToFindCurrentLocation)
        {
          paramVarArgs = (HashMap)paramVarArgs[0];
          localObject = new AlertDialog.Builder(this);
          ((AlertDialog.Builder)localObject).setTitle(LocaleController.getString("AppName", 2131492981));
          ((AlertDialog.Builder)localObject).setPositiveButton(LocaleController.getString("OK", 2131494028), null);
          ((AlertDialog.Builder)localObject).setNegativeButton(LocaleController.getString("ShareYouLocationUnableManually", 2131494393), new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
            {
              if (LaunchActivity.mainFragmentsStack.isEmpty()) {}
              while (!AndroidUtilities.isGoogleMapsInstalled((BaseFragment)LaunchActivity.mainFragmentsStack.get(LaunchActivity.mainFragmentsStack.size() - 1))) {
                return;
              }
              paramAnonymousDialogInterface = new LocationActivity(0);
              paramAnonymousDialogInterface.setDelegate(new LocationActivity.LocationActivityDelegate()
              {
                public void didSelectLocation(TLRPC.MessageMedia paramAnonymous2MessageMedia, int paramAnonymous2Int)
                {
                  Iterator localIterator = LaunchActivity.19.this.val$waitingForLocation.entrySet().iterator();
                  while (localIterator.hasNext())
                  {
                    MessageObject localMessageObject = (MessageObject)((Map.Entry)localIterator.next()).getValue();
                    SendMessagesHelper.getInstance(LaunchActivity.19.this.val$account).sendMessage(paramAnonymous2MessageMedia, localMessageObject.getDialogId(), localMessageObject, null, null);
                  }
                }
              });
              LaunchActivity.this.presentFragment(paramAnonymousDialogInterface);
            }
          });
          ((AlertDialog.Builder)localObject).setMessage(LocaleController.getString("ShareYouLocationUnable", 2131494392));
          if (!mainFragmentsStack.isEmpty()) {
            ((BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1)).showDialog(((AlertDialog.Builder)localObject).create());
          }
        }
        else if (paramInt1 == NotificationCenter.didSetNewWallpapper)
        {
          if (this.sideMenu != null)
          {
            paramVarArgs = this.sideMenu.getChildAt(0);
            if (paramVarArgs != null) {
              paramVarArgs.invalidate();
            }
          }
        }
        else
        {
          if (paramInt1 == NotificationCenter.didSetPasscode)
          {
            if ((SharedConfig.passcodeHash.length() > 0) && (!SharedConfig.allowScreenCapture)) {
              try
              {
                getWindow().setFlags(8192, 8192);
                return;
              }
              catch (Exception paramVarArgs)
              {
                FileLog.e(paramVarArgs);
                return;
              }
            }
            try
            {
              getWindow().clearFlags(8192);
              return;
            }
            catch (Exception paramVarArgs)
            {
              FileLog.e(paramVarArgs);
              return;
            }
          }
          if (paramInt1 == NotificationCenter.reloadInterface)
          {
            rebuildAllFragments(false);
            return;
          }
          if (paramInt1 == NotificationCenter.suggestedLangpack)
          {
            showLanguageAlert(false);
            return;
          }
          if (paramInt1 == NotificationCenter.openArticle)
          {
            if (!mainFragmentsStack.isEmpty())
            {
              ArticleViewer.getInstance().setParentActivity(this, (BaseFragment)mainFragmentsStack.get(mainFragmentsStack.size() - 1));
              ArticleViewer.getInstance().open((TLRPC.TL_webPage)paramVarArgs[0], (String)paramVarArgs[1]);
            }
          }
          else if (paramInt1 == NotificationCenter.hasNewContactsToImport)
          {
            if ((this.actionBarLayout != null) && (!this.actionBarLayout.fragmentsStack.isEmpty()))
            {
              ((Integer)paramVarArgs[0]).intValue();
              localObject = (HashMap)paramVarArgs[1];
              final boolean bool1 = ((Boolean)paramVarArgs[2]).booleanValue();
              final boolean bool2 = ((Boolean)paramVarArgs[3]).booleanValue();
              paramVarArgs = (BaseFragment)this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
              localBuilder = new AlertDialog.Builder(this);
              localBuilder.setTitle(LocaleController.getString("UpdateContactsTitle", 2131494520));
              localBuilder.setMessage(LocaleController.getString("UpdateContactsMessage", 2131494519));
              localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                  ContactsController.getInstance(paramInt2).syncPhoneBookByAlert(localObject, bool1, bool2, false);
                }
              });
              localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                  ContactsController.getInstance(paramInt2).syncPhoneBookByAlert(localObject, bool1, bool2, true);
                }
              });
              localBuilder.setOnBackButtonListener(new DialogInterface.OnClickListener()
              {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                  ContactsController.getInstance(paramInt2).syncPhoneBookByAlert(localObject, bool1, bool2, true);
                }
              });
              localObject = localBuilder.create();
              paramVarArgs.showDialog((Dialog)localObject);
              ((AlertDialog)localObject).setCanceledOnTouchOutside(false);
            }
          }
          else
          {
            if (paramInt1 == NotificationCenter.didSetNewTheme)
            {
              if (((Boolean)paramVarArgs[0]).booleanValue()) {
                continue;
              }
              if (this.sideMenu != null)
              {
                this.sideMenu.setBackgroundColor(Theme.getColor("chats_menuBackground"));
                this.sideMenu.setGlowColor(Theme.getColor("chats_menuBackground"));
                this.sideMenu.getAdapter().notifyDataSetChanged();
              }
              if (Build.VERSION.SDK_INT < 21) {
                continue;
              }
              try
              {
                setTaskDescription(new ActivityManager.TaskDescription(null, null, Theme.getColor("actionBarDefault") | 0xFF000000));
                return;
              }
              catch (Exception paramVarArgs)
              {
                return;
              }
            }
            if (paramInt1 == NotificationCenter.needSetDayNightTheme)
            {
              paramVarArgs = (Theme.ThemeInfo)paramVarArgs[0];
              this.actionBarLayout.animateThemedValues(paramVarArgs);
              if (AndroidUtilities.isTablet())
              {
                this.layersActionBarLayout.animateThemedValues(paramVarArgs);
                this.rightActionBarLayout.animateThemedValues(paramVarArgs);
              }
            }
            else if ((paramInt1 == NotificationCenter.notificationsCountUpdated) && (this.sideMenu != null))
            {
              paramVarArgs = (Integer)paramVarArgs[0];
              paramInt2 = this.sideMenu.getChildCount();
              paramInt1 = 0;
              while (paramInt1 < paramInt2)
              {
                localObject = this.sideMenu.getChildAt(paramInt1);
                if (((localObject instanceof DrawerUserCell)) && (((DrawerUserCell)localObject).getAccountNumber() == paramVarArgs.intValue()))
                {
                  ((View)localObject).invalidate();
                  return;
                }
                paramInt1 += 1;
              }
            }
          }
        }
      }
    }
  }
  
  public void didSelectDialogs(DialogsActivity paramDialogsActivity, ArrayList<Long> paramArrayList, CharSequence paramCharSequence, boolean paramBoolean)
  {
    long l = ((Long)paramArrayList.get(0)).longValue();
    int j = (int)l;
    int k = (int)(l >> 32);
    paramArrayList = new Bundle();
    int i;
    if (paramDialogsActivity != null)
    {
      i = paramDialogsActivity.getCurrentAccount();
      paramArrayList.putBoolean("scrollToTopOnResume", true);
      if (!AndroidUtilities.isTablet()) {
        NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.closeChats, new Object[0]);
      }
      if (j == 0) {
        break label151;
      }
      if (k != 1) {
        break label116;
      }
      paramArrayList.putInt("chat_id", j);
    }
    for (;;)
    {
      if (MessagesController.getInstance(i).checkCanOpenChat(paramArrayList, paramDialogsActivity)) {
        break label163;
      }
      return;
      i = this.currentAccount;
      break;
      label116:
      if (j > 0)
      {
        paramArrayList.putInt("user_id", j);
      }
      else if (j < 0)
      {
        paramArrayList.putInt("chat_id", -j);
        continue;
        label151:
        paramArrayList.putInt("enc_id", k);
      }
    }
    label163:
    paramArrayList = new ChatActivity(paramArrayList);
    paramCharSequence = this.actionBarLayout;
    if (paramDialogsActivity != null)
    {
      paramBoolean = true;
      if (paramDialogsActivity != null) {
        break label420;
      }
    }
    label420:
    for (boolean bool = true;; bool = false)
    {
      paramCharSequence.presentFragment(paramArrayList, paramBoolean, bool, true);
      if (this.videoPath != null)
      {
        paramArrayList.openVideoEditor(this.videoPath, this.sendingText);
        this.sendingText = null;
      }
      if (this.photoPathsArray != null)
      {
        if ((this.sendingText != null) && (this.sendingText.length() <= 200) && (this.photoPathsArray.size() == 1))
        {
          ((SendMessagesHelper.SendingMediaInfo)this.photoPathsArray.get(0)).caption = this.sendingText;
          this.sendingText = null;
        }
        SendMessagesHelper.prepareSendingMedia(this.photoPathsArray, l, null, null, false, false);
      }
      if (this.sendingText != null) {
        SendMessagesHelper.prepareSendingText(this.sendingText, l);
      }
      if ((this.documentsPathsArray != null) || (this.documentsUrisArray != null)) {
        SendMessagesHelper.prepareSendingDocuments(this.documentsPathsArray, this.documentsOriginalPathsArray, this.documentsUrisArray, this.documentsMimeType, l, null, null);
      }
      if ((this.contactsToSend == null) || (this.contactsToSend.isEmpty())) {
        break label426;
      }
      paramDialogsActivity = this.contactsToSend.iterator();
      while (paramDialogsActivity.hasNext())
      {
        paramArrayList = (TLRPC.User)paramDialogsActivity.next();
        SendMessagesHelper.getInstance(i).sendMessage(paramArrayList, l, null, null, null);
      }
      paramBoolean = false;
      break;
    }
    label426:
    this.photoPathsArray = null;
    this.videoPath = null;
    this.sendingText = null;
    this.documentsPathsArray = null;
    this.documentsOriginalPathsArray = null;
    this.contactsToSend = null;
  }
  
  public ActionBarLayout getActionBarLayout()
  {
    return this.actionBarLayout;
  }
  
  public ActionBarLayout getLayersActionBarLayout()
  {
    return this.layersActionBarLayout;
  }
  
  public int getMainFragmentsCount()
  {
    return mainFragmentsStack.size();
  }
  
  public ActionBarLayout getRightActionBarLayout()
  {
    return this.rightActionBarLayout;
  }
  
  public boolean needAddFragmentToStack(BaseFragment paramBaseFragment, ActionBarLayout paramActionBarLayout)
  {
    boolean bool1;
    if (AndroidUtilities.isTablet())
    {
      DrawerLayoutContainer localDrawerLayoutContainer = this.drawerLayoutContainer;
      if ((!(paramBaseFragment instanceof LoginActivity)) && (!(paramBaseFragment instanceof CountrySelectActivity)) && (this.layersActionBarLayout.getVisibility() != 0))
      {
        bool1 = true;
        localDrawerLayoutContainer.setAllowOpenDrawer(bool1, true);
        if (!(paramBaseFragment instanceof DialogsActivity)) {
          break label157;
        }
        if ((!((DialogsActivity)paramBaseFragment).isMainDialogList()) || (paramActionBarLayout == this.actionBarLayout)) {
          break label457;
        }
        this.actionBarLayout.removeAllFragments();
        this.actionBarLayout.addFragmentToStack(paramBaseFragment);
        this.layersActionBarLayout.removeAllFragments();
        this.layersActionBarLayout.setVisibility(8);
        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        if (!this.tabletFullSize)
        {
          this.shadowTabletSide.setVisibility(0);
          if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
            this.backgroundTablet.setVisibility(0);
          }
        }
      }
      label157:
      label282:
      do
      {
        do
        {
          return false;
          bool1 = false;
          break;
          if (!(paramBaseFragment instanceof ChatActivity)) {
            break label376;
          }
          if ((this.tabletFullSize) || (paramActionBarLayout == this.rightActionBarLayout)) {
            break label282;
          }
          this.rightActionBarLayout.setVisibility(0);
          this.backgroundTablet.setVisibility(8);
          this.rightActionBarLayout.removeAllFragments();
          this.rightActionBarLayout.addFragmentToStack(paramBaseFragment);
        } while (this.layersActionBarLayout.fragmentsStack.isEmpty());
        for (i = 0; this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
          this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
        }
        this.layersActionBarLayout.closeLastFragment(true);
        return false;
        if ((!this.tabletFullSize) || (paramActionBarLayout == this.actionBarLayout)) {
          break label457;
        }
        this.actionBarLayout.addFragmentToStack(paramBaseFragment);
      } while (this.layersActionBarLayout.fragmentsStack.isEmpty());
      for (int i = 0; this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
      }
      this.layersActionBarLayout.closeLastFragment(true);
      return false;
      label376:
      if (paramActionBarLayout != this.layersActionBarLayout)
      {
        this.layersActionBarLayout.setVisibility(0);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
        if ((paramBaseFragment instanceof LoginActivity))
        {
          this.backgroundTablet.setVisibility(0);
          this.shadowTabletSide.setVisibility(8);
          this.shadowTablet.setBackgroundColor(0);
        }
        for (;;)
        {
          this.layersActionBarLayout.addFragmentToStack(paramBaseFragment);
          return false;
          this.shadowTablet.setBackgroundColor(2130706432);
        }
      }
      label457:
      return true;
    }
    boolean bool2 = true;
    if ((paramBaseFragment instanceof LoginActivity))
    {
      bool1 = bool2;
      if (mainFragmentsStack.size() == 0) {
        bool1 = false;
      }
    }
    for (;;)
    {
      this.drawerLayoutContainer.setAllowOpenDrawer(bool1, false);
      return true;
      bool1 = bool2;
      if ((paramBaseFragment instanceof CountrySelectActivity))
      {
        bool1 = bool2;
        if (mainFragmentsStack.size() == 1) {
          bool1 = false;
        }
      }
    }
  }
  
  public boolean needCloseLastFragment(ActionBarLayout paramActionBarLayout)
  {
    if (AndroidUtilities.isTablet())
    {
      if ((paramActionBarLayout == this.actionBarLayout) && (paramActionBarLayout.fragmentsStack.size() <= 1))
      {
        onFinish();
        finish();
        return false;
      }
      if (paramActionBarLayout == this.rightActionBarLayout) {
        if (!this.tabletFullSize) {
          this.backgroundTablet.setVisibility(0);
        }
      }
    }
    for (;;)
    {
      return true;
      if ((paramActionBarLayout == this.layersActionBarLayout) && (this.actionBarLayout.fragmentsStack.isEmpty()) && (this.layersActionBarLayout.fragmentsStack.size() == 1))
      {
        onFinish();
        finish();
        return false;
        if (paramActionBarLayout.fragmentsStack.size() <= 1)
        {
          onFinish();
          finish();
          return false;
        }
        if ((paramActionBarLayout.fragmentsStack.size() >= 2) && (!(paramActionBarLayout.fragmentsStack.get(0) instanceof LoginActivity))) {
          this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
      }
    }
  }
  
  public boolean needPresentFragment(BaseFragment paramBaseFragment, boolean paramBoolean1, boolean paramBoolean2, ActionBarLayout paramActionBarLayout)
  {
    boolean bool5 = true;
    boolean bool3 = true;
    boolean bool4 = true;
    boolean bool2 = true;
    if ((ArticleViewer.hasInstance()) && (ArticleViewer.getInstance().isVisible())) {
      ArticleViewer.getInstance().close(false, true);
    }
    if (AndroidUtilities.isTablet())
    {
      DrawerLayoutContainer localDrawerLayoutContainer = this.drawerLayoutContainer;
      boolean bool1;
      if ((!(paramBaseFragment instanceof LoginActivity)) && (!(paramBaseFragment instanceof CountrySelectActivity)) && (this.layersActionBarLayout.getVisibility() != 0))
      {
        bool1 = true;
        localDrawerLayoutContainer.setAllowOpenDrawer(bool1, true);
        if ((!(paramBaseFragment instanceof DialogsActivity)) || (!((DialogsActivity)paramBaseFragment).isMainDialogList()) || (paramActionBarLayout == this.actionBarLayout)) {
          break label196;
        }
        this.actionBarLayout.removeAllFragments();
        this.actionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, paramBoolean2, false);
        this.layersActionBarLayout.removeAllFragments();
        this.layersActionBarLayout.setVisibility(8);
        this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        if (!this.tabletFullSize)
        {
          this.shadowTabletSide.setVisibility(0);
          if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
            this.backgroundTablet.setVisibility(0);
          }
        }
      }
      label196:
      label374:
      do
      {
        return false;
        bool1 = false;
        break;
        if (!(paramBaseFragment instanceof ChatActivity)) {
          break label785;
        }
        if (((!this.tabletFullSize) && (paramActionBarLayout == this.rightActionBarLayout)) || ((this.tabletFullSize) && (paramActionBarLayout == this.actionBarLayout)))
        {
          if ((!this.tabletFullSize) || (paramActionBarLayout != this.actionBarLayout) || (this.actionBarLayout.fragmentsStack.size() != 1)) {
            paramBoolean1 = true;
          }
          while (!this.layersActionBarLayout.fragmentsStack.isEmpty())
          {
            i = 0;
            for (;;)
            {
              if (this.layersActionBarLayout.fragmentsStack.size() - 1 > 0)
              {
                this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
                i = i - 1 + 1;
                continue;
                paramBoolean1 = false;
                break;
              }
            }
            paramActionBarLayout = this.layersActionBarLayout;
            if (paramBoolean2) {
              break label374;
            }
          }
          for (bool1 = bool2;; bool1 = false)
          {
            paramActionBarLayout.closeLastFragment(bool1);
            if (!paramBoolean1) {
              this.actionBarLayout.presentFragment(paramBaseFragment, false, paramBoolean2, false);
            }
            return paramBoolean1;
          }
        }
        if ((this.tabletFullSize) || (paramActionBarLayout == this.rightActionBarLayout)) {
          break label519;
        }
        this.rightActionBarLayout.setVisibility(0);
        this.backgroundTablet.setVisibility(8);
        this.rightActionBarLayout.removeAllFragments();
        this.rightActionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, true, false);
      } while (this.layersActionBarLayout.fragmentsStack.isEmpty());
      for (int i = 0; this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
        this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
      }
      paramBaseFragment = this.layersActionBarLayout;
      if (!paramBoolean2) {}
      for (paramBoolean1 = bool5;; paramBoolean1 = false)
      {
        paramBaseFragment.closeLastFragment(paramBoolean1);
        return false;
      }
      label519:
      if ((this.tabletFullSize) && (paramActionBarLayout != this.actionBarLayout))
      {
        paramActionBarLayout = this.actionBarLayout;
        if (this.actionBarLayout.fragmentsStack.size() > 1) {}
        for (paramBoolean1 = true;; paramBoolean1 = false)
        {
          paramActionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, paramBoolean2, false);
          if (this.layersActionBarLayout.fragmentsStack.isEmpty()) {
            break;
          }
          for (i = 0; this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
            this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
          }
        }
        paramBaseFragment = this.layersActionBarLayout;
        if (!paramBoolean2) {}
        for (paramBoolean1 = bool3;; paramBoolean1 = false)
        {
          paramBaseFragment.closeLastFragment(paramBoolean1);
          return false;
        }
      }
      if (!this.layersActionBarLayout.fragmentsStack.isEmpty())
      {
        for (i = 0; this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
          this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(0));
        }
        paramActionBarLayout = this.layersActionBarLayout;
        if (!paramBoolean2)
        {
          paramBoolean1 = true;
          paramActionBarLayout.closeLastFragment(paramBoolean1);
        }
      }
      else
      {
        paramActionBarLayout = this.actionBarLayout;
        if (this.actionBarLayout.fragmentsStack.size() <= 1) {
          break label780;
        }
      }
      label780:
      for (paramBoolean1 = bool4;; paramBoolean1 = false)
      {
        paramActionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, paramBoolean2, false);
        return false;
        paramBoolean1 = false;
        break;
      }
      label785:
      if (paramActionBarLayout != this.layersActionBarLayout)
      {
        this.layersActionBarLayout.setVisibility(0);
        this.drawerLayoutContainer.setAllowOpenDrawer(false, true);
        if ((paramBaseFragment instanceof LoginActivity))
        {
          this.backgroundTablet.setVisibility(0);
          this.shadowTabletSide.setVisibility(8);
          this.shadowTablet.setBackgroundColor(0);
        }
        for (;;)
        {
          this.layersActionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, paramBoolean2, false);
          return false;
          this.shadowTablet.setBackgroundColor(2130706432);
        }
      }
      return true;
    }
    paramBoolean2 = true;
    if ((paramBaseFragment instanceof LoginActivity))
    {
      paramBoolean1 = paramBoolean2;
      if (mainFragmentsStack.size() == 0) {
        paramBoolean1 = false;
      }
    }
    for (;;)
    {
      this.drawerLayoutContainer.setAllowOpenDrawer(paramBoolean1, false);
      return true;
      paramBoolean1 = paramBoolean2;
      if ((paramBaseFragment instanceof CountrySelectActivity))
      {
        paramBoolean1 = paramBoolean2;
        if (mainFragmentsStack.size() == 1) {
          paramBoolean1 = false;
        }
      }
    }
  }
  
  public void onActionModeFinished(ActionMode paramActionMode)
  {
    super.onActionModeFinished(paramActionMode);
    if ((Build.VERSION.SDK_INT >= 23) && (paramActionMode.getType() == 1)) {}
    do
    {
      return;
      this.actionBarLayout.onActionModeFinished(paramActionMode);
    } while (!AndroidUtilities.isTablet());
    this.rightActionBarLayout.onActionModeFinished(paramActionMode);
    this.layersActionBarLayout.onActionModeFinished(paramActionMode);
  }
  
  public void onActionModeStarted(ActionMode paramActionMode)
  {
    super.onActionModeStarted(paramActionMode);
    try
    {
      Menu localMenu = paramActionMode.getMenu();
      if ((localMenu != null) && (!this.actionBarLayout.extendActionMode(localMenu)) && (AndroidUtilities.isTablet()) && (!this.rightActionBarLayout.extendActionMode(localMenu))) {
        this.layersActionBarLayout.extendActionMode(localMenu);
      }
    }
    catch (Exception localException)
    {
      do
      {
        for (;;)
        {
          FileLog.e(localException);
        }
        this.actionBarLayout.onActionModeStarted(paramActionMode);
      } while (!AndroidUtilities.isTablet());
      this.rightActionBarLayout.onActionModeStarted(paramActionMode);
      this.layersActionBarLayout.onActionModeStarted(paramActionMode);
    }
    if ((Build.VERSION.SDK_INT >= 23) && (paramActionMode.getType() == 1)) {
      return;
    }
  }
  
  protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    if ((SharedConfig.passcodeHash.length() != 0) && (SharedConfig.lastPauseTime != 0))
    {
      SharedConfig.lastPauseTime = 0;
      UserConfig.getInstance(this.currentAccount).saveConfig(false);
    }
    super.onActivityResult(paramInt1, paramInt2, paramIntent);
    ThemeEditorView localThemeEditorView = ThemeEditorView.getInstance();
    if (localThemeEditorView != null) {
      localThemeEditorView.onActivityResult(paramInt1, paramInt2, paramIntent);
    }
    if (this.actionBarLayout.fragmentsStack.size() != 0) {
      ((BaseFragment)this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(paramInt1, paramInt2, paramIntent);
    }
    if (AndroidUtilities.isTablet())
    {
      if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
        ((BaseFragment)this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(paramInt1, paramInt2, paramIntent);
      }
      if (this.layersActionBarLayout.fragmentsStack.size() != 0) {
        ((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onActivityResultFragment(paramInt1, paramInt2, paramIntent);
      }
    }
  }
  
  public void onBackPressed()
  {
    if (this.passcodeView.getVisibility() == 0) {
      finish();
    }
    for (;;)
    {
      return;
      if ((SecretMediaViewer.hasInstance()) && (SecretMediaViewer.getInstance().isVisible()))
      {
        SecretMediaViewer.getInstance().closePhoto(true, false);
        return;
      }
      if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible()))
      {
        PhotoViewer.getInstance().closePhoto(true, false);
        return;
      }
      if ((ArticleViewer.hasInstance()) && (ArticleViewer.getInstance().isVisible()))
      {
        ArticleViewer.getInstance().close(true, false);
        return;
      }
      if (this.drawerLayoutContainer.isDrawerOpened())
      {
        this.drawerLayoutContainer.closeDrawer(false);
        return;
      }
      if (!AndroidUtilities.isTablet()) {
        break;
      }
      if (this.layersActionBarLayout.getVisibility() == 0)
      {
        this.layersActionBarLayout.onBackPressed();
        return;
      }
      int j = 0;
      int i = j;
      if (this.rightActionBarLayout.getVisibility() == 0)
      {
        i = j;
        if (!this.rightActionBarLayout.fragmentsStack.isEmpty()) {
          if (((BaseFragment)this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onBackPressed()) {
            break label204;
          }
        }
      }
      label204:
      for (i = 1; i == 0; i = 0)
      {
        this.actionBarLayout.onBackPressed();
        return;
      }
    }
    this.actionBarLayout.onBackPressed();
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    AndroidUtilities.checkDisplaySize(this, paramConfiguration);
    super.onConfigurationChanged(paramConfiguration);
    checkLayout();
    Object localObject = PipRoundVideoView.getInstance();
    if (localObject != null) {
      ((PipRoundVideoView)localObject).onConfigurationChanged();
    }
    localObject = EmbedBottomSheet.getInstance();
    if (localObject != null) {
      ((EmbedBottomSheet)localObject).onConfigurationChanged(paramConfiguration);
    }
    localObject = PhotoViewer.getPipInstance();
    if (localObject != null) {
      ((PhotoViewer)localObject).onConfigurationChanged(paramConfiguration);
    }
    paramConfiguration = ThemeEditorView.getInstance();
    if (paramConfiguration != null) {
      paramConfiguration.onConfigurationChanged();
    }
  }
  
  protected void onCreate(Bundle paramBundle)
  {
    ApplicationLoader.postInitApplication();
    AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
    this.currentAccount = UserConfig.selectedAccount;
    final Object localObject1;
    Object localObject4;
    boolean bool1;
    if (!UserConfig.getInstance(this.currentAccount).isClientActivated())
    {
      localObject1 = getIntent();
      if ((localObject1 != null) && (((Intent)localObject1).getAction() != null) && (("android.intent.action.SEND".equals(((Intent)localObject1).getAction())) || (((Intent)localObject1).getAction().equals("android.intent.action.SEND_MULTIPLE"))))
      {
        super.onCreate(paramBundle);
        finish();
        return;
      }
      localObject4 = MessagesController.getGlobalMainSettings();
      long l = ((SharedPreferences)localObject4).getLong("intro_crashed_time", 0L);
      bool1 = ((Intent)localObject1).getBooleanExtra("fromIntro", false);
      if (bool1) {
        ((SharedPreferences)localObject4).edit().putLong("intro_crashed_time", 0L).commit();
      }
      if ((Math.abs(l - System.currentTimeMillis()) >= 120000L) && (localObject1 != null) && (!bool1) && (ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", 0).getAll().isEmpty()))
      {
        localObject4 = new Intent(this, IntroActivity.class);
        ((Intent)localObject4).setData(((Intent)localObject1).getData());
        startActivity((Intent)localObject4);
        super.onCreate(paramBundle);
        finish();
        return;
      }
    }
    requestWindowFeature(1);
    setTheme(2131558412);
    if (Build.VERSION.SDK_INT >= 21) {}
    try
    {
      setTaskDescription(new ActivityManager.TaskDescription(null, null, Theme.getColor("actionBarDefault") | 0xFF000000));
      getWindow().setBackgroundDrawableResource(2131165683);
      if ((SharedConfig.passcodeHash.length() > 0) && (!SharedConfig.allowScreenCapture)) {}
      try
      {
        getWindow().setFlags(8192, 8192);
        super.onCreate(paramBundle);
        if (Build.VERSION.SDK_INT >= 24) {
          AndroidUtilities.isInMultiwindow = isInMultiWindowMode();
        }
        Theme.createChatResources(this, false);
        if ((SharedConfig.passcodeHash.length() != 0) && (SharedConfig.appLocked)) {
          SharedConfig.lastPauseTime = ConnectionsManager.getInstance(this.currentAccount).getCurrentTime();
        }
        i = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (i > 0) {
          AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(i);
        }
        this.actionBarLayout = new ActionBarLayout(this);
        this.drawerLayoutContainer = new DrawerLayoutContainer(this);
        setContentView(this.drawerLayoutContainer, new ViewGroup.LayoutParams(-1, -1));
        if (AndroidUtilities.isTablet())
        {
          getWindow().setSoftInputMode(16);
          localObject1 = new RelativeLayout(this)
          {
            private boolean inLayout;
            
            protected void onLayout(boolean paramAnonymousBoolean, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3, int paramAnonymousInt4)
            {
              int i = paramAnonymousInt3 - paramAnonymousInt1;
              if ((!AndroidUtilities.isInMultiwindow) && ((!AndroidUtilities.isSmallTablet()) || (getResources().getConfiguration().orientation == 2)))
              {
                paramAnonymousInt3 = i / 100 * 35;
                paramAnonymousInt1 = paramAnonymousInt3;
                if (paramAnonymousInt3 < AndroidUtilities.dp(320.0F)) {
                  paramAnonymousInt1 = AndroidUtilities.dp(320.0F);
                }
                LaunchActivity.this.shadowTabletSide.layout(paramAnonymousInt1, 0, LaunchActivity.this.shadowTabletSide.getMeasuredWidth() + paramAnonymousInt1, LaunchActivity.this.shadowTabletSide.getMeasuredHeight());
                LaunchActivity.this.actionBarLayout.layout(0, 0, LaunchActivity.this.actionBarLayout.getMeasuredWidth(), LaunchActivity.this.actionBarLayout.getMeasuredHeight());
                LaunchActivity.this.rightActionBarLayout.layout(paramAnonymousInt1, 0, LaunchActivity.this.rightActionBarLayout.getMeasuredWidth() + paramAnonymousInt1, LaunchActivity.this.rightActionBarLayout.getMeasuredHeight());
              }
              for (;;)
              {
                paramAnonymousInt1 = (i - LaunchActivity.this.layersActionBarLayout.getMeasuredWidth()) / 2;
                paramAnonymousInt2 = (paramAnonymousInt4 - paramAnonymousInt2 - LaunchActivity.this.layersActionBarLayout.getMeasuredHeight()) / 2;
                LaunchActivity.this.layersActionBarLayout.layout(paramAnonymousInt1, paramAnonymousInt2, LaunchActivity.this.layersActionBarLayout.getMeasuredWidth() + paramAnonymousInt1, LaunchActivity.this.layersActionBarLayout.getMeasuredHeight() + paramAnonymousInt2);
                LaunchActivity.this.backgroundTablet.layout(0, 0, LaunchActivity.this.backgroundTablet.getMeasuredWidth(), LaunchActivity.this.backgroundTablet.getMeasuredHeight());
                LaunchActivity.this.shadowTablet.layout(0, 0, LaunchActivity.this.shadowTablet.getMeasuredWidth(), LaunchActivity.this.shadowTablet.getMeasuredHeight());
                return;
                LaunchActivity.this.actionBarLayout.layout(0, 0, LaunchActivity.this.actionBarLayout.getMeasuredWidth(), LaunchActivity.this.actionBarLayout.getMeasuredHeight());
              }
            }
            
            protected void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2)
            {
              this.inLayout = true;
              int i = View.MeasureSpec.getSize(paramAnonymousInt1);
              int j = View.MeasureSpec.getSize(paramAnonymousInt2);
              setMeasuredDimension(i, j);
              if ((!AndroidUtilities.isInMultiwindow) && ((!AndroidUtilities.isSmallTablet()) || (getResources().getConfiguration().orientation == 2)))
              {
                LaunchActivity.access$002(LaunchActivity.this, false);
                paramAnonymousInt2 = i / 100 * 35;
                paramAnonymousInt1 = paramAnonymousInt2;
                if (paramAnonymousInt2 < AndroidUtilities.dp(320.0F)) {
                  paramAnonymousInt1 = AndroidUtilities.dp(320.0F);
                }
                LaunchActivity.this.actionBarLayout.measure(View.MeasureSpec.makeMeasureSpec(paramAnonymousInt1, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
                LaunchActivity.this.shadowTabletSide.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
                LaunchActivity.this.rightActionBarLayout.measure(View.MeasureSpec.makeMeasureSpec(i - paramAnonymousInt1, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
              }
              for (;;)
              {
                LaunchActivity.this.backgroundTablet.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
                LaunchActivity.this.shadowTablet.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
                LaunchActivity.this.layersActionBarLayout.measure(View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(530.0F), i), 1073741824), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(528.0F), j), 1073741824));
                this.inLayout = false;
                return;
                LaunchActivity.access$002(LaunchActivity.this, true);
                LaunchActivity.this.actionBarLayout.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(j, 1073741824));
              }
            }
            
            public void requestLayout()
            {
              if (this.inLayout) {
                return;
              }
              super.requestLayout();
            }
          };
          this.drawerLayoutContainer.addView((View)localObject1, LayoutHelper.createFrame(-1, -1.0F));
          this.backgroundTablet = new View(this);
          localObject4 = (BitmapDrawable)getResources().getDrawable(2131165266);
          ((BitmapDrawable)localObject4).setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
          this.backgroundTablet.setBackgroundDrawable((Drawable)localObject4);
          ((RelativeLayout)localObject1).addView(this.backgroundTablet, LayoutHelper.createRelative(-1, -1));
          ((RelativeLayout)localObject1).addView(this.actionBarLayout);
          this.rightActionBarLayout = new ActionBarLayout(this);
          this.rightActionBarLayout.init(rightFragmentsStack);
          this.rightActionBarLayout.setDelegate(this);
          ((RelativeLayout)localObject1).addView(this.rightActionBarLayout);
          this.shadowTabletSide = new FrameLayout(this);
          this.shadowTabletSide.setBackgroundColor(1076449908);
          ((RelativeLayout)localObject1).addView(this.shadowTabletSide);
          this.shadowTablet = new FrameLayout(this);
          localObject4 = this.shadowTablet;
          if (layerFragmentsStack.isEmpty())
          {
            i = 8;
            ((FrameLayout)localObject4).setVisibility(i);
            this.shadowTablet.setBackgroundColor(2130706432);
            ((RelativeLayout)localObject1).addView(this.shadowTablet);
            this.shadowTablet.setOnTouchListener(new View.OnTouchListener()
            {
              public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
              {
                if ((!LaunchActivity.this.actionBarLayout.fragmentsStack.isEmpty()) && (paramAnonymousMotionEvent.getAction() == 1))
                {
                  float f1 = paramAnonymousMotionEvent.getX();
                  float f2 = paramAnonymousMotionEvent.getY();
                  paramAnonymousView = new int[2];
                  LaunchActivity.this.layersActionBarLayout.getLocationOnScreen(paramAnonymousView);
                  int i = paramAnonymousView[0];
                  int j = paramAnonymousView[1];
                  if ((LaunchActivity.this.layersActionBarLayout.checkTransitionAnimation()) || ((f1 > i) && (f1 < LaunchActivity.this.layersActionBarLayout.getWidth() + i) && (f2 > j) && (f2 < LaunchActivity.this.layersActionBarLayout.getHeight() + j))) {
                    return false;
                  }
                  if (!LaunchActivity.this.layersActionBarLayout.fragmentsStack.isEmpty())
                  {
                    for (i = 0; LaunchActivity.this.layersActionBarLayout.fragmentsStack.size() - 1 > 0; i = i - 1 + 1) {
                      LaunchActivity.this.layersActionBarLayout.removeFragmentFromStack((BaseFragment)LaunchActivity.this.layersActionBarLayout.fragmentsStack.get(0));
                    }
                    LaunchActivity.this.layersActionBarLayout.closeLastFragment(true);
                  }
                  return true;
                }
                return false;
              }
            });
            this.shadowTablet.setOnClickListener(new View.OnClickListener()
            {
              public void onClick(View paramAnonymousView) {}
            });
            this.layersActionBarLayout = new ActionBarLayout(this);
            this.layersActionBarLayout.setRemoveActionBarExtraHeight(true);
            this.layersActionBarLayout.setBackgroundView(this.shadowTablet);
            this.layersActionBarLayout.setUseAlphaAnimations(true);
            this.layersActionBarLayout.setBackgroundResource(2131165251);
            this.layersActionBarLayout.init(layerFragmentsStack);
            this.layersActionBarLayout.setDelegate(this);
            this.layersActionBarLayout.setDrawerLayoutContainer(this.drawerLayoutContainer);
            localObject4 = this.layersActionBarLayout;
            if (!layerFragmentsStack.isEmpty()) {
              break label1516;
            }
            i = 8;
            ((ActionBarLayout)localObject4).setVisibility(i);
            ((RelativeLayout)localObject1).addView(this.layersActionBarLayout);
            this.sideMenu = new RecyclerListView(this);
            ((DefaultItemAnimator)this.sideMenu.getItemAnimator()).setDelayAnimations(false);
            this.sideMenu.setBackgroundColor(Theme.getColor("chats_menuBackground"));
            this.sideMenu.setLayoutManager(new LinearLayoutManager(this, 1, false));
            localObject1 = this.sideMenu;
            localObject4 = new DrawerLayoutAdapter(this);
            this.drawerLayoutAdapter = ((DrawerLayoutAdapter)localObject4);
            ((RecyclerListView)localObject1).setAdapter((RecyclerView.Adapter)localObject4);
            this.drawerLayoutContainer.setDrawerLayout(this.sideMenu);
            localObject1 = (FrameLayout.LayoutParams)this.sideMenu.getLayoutParams();
            localObject4 = AndroidUtilities.getRealScreenSize();
            if (!AndroidUtilities.isTablet()) {
              break label1544;
            }
            i = AndroidUtilities.dp(320.0F);
            ((FrameLayout.LayoutParams)localObject1).width = i;
            ((FrameLayout.LayoutParams)localObject1).height = -1;
            this.sideMenu.setLayoutParams((ViewGroup.LayoutParams)localObject1);
            this.sideMenu.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
            {
              public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
              {
                boolean bool = false;
                if (paramAnonymousInt == 0)
                {
                  paramAnonymousView = LaunchActivity.this.drawerLayoutAdapter;
                  if (!LaunchActivity.this.drawerLayoutAdapter.isAccountsShowed()) {
                    bool = true;
                  }
                  paramAnonymousView.setAccountsShowed(bool, true);
                }
                do
                {
                  return;
                  if ((paramAnonymousView instanceof DrawerUserCell))
                  {
                    LaunchActivity.this.switchToAccount(((DrawerUserCell)paramAnonymousView).getAccountNumber(), true);
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if ((paramAnonymousView instanceof DrawerAddCell))
                  {
                    int j = -1;
                    paramAnonymousInt = 0;
                    for (;;)
                    {
                      int i = j;
                      if (paramAnonymousInt < 3)
                      {
                        if (!UserConfig.getInstance(paramAnonymousInt).isClientActivated()) {
                          i = paramAnonymousInt;
                        }
                      }
                      else
                      {
                        if (i >= 0) {
                          LaunchActivity.this.presentFragment(new LoginActivity(i));
                        }
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                        return;
                      }
                      paramAnonymousInt += 1;
                    }
                  }
                  paramAnonymousInt = LaunchActivity.this.drawerLayoutAdapter.getId(paramAnonymousInt);
                  if (paramAnonymousInt == 2)
                  {
                    LaunchActivity.this.presentFragment(new GroupCreateActivity());
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 3)
                  {
                    paramAnonymousView = new Bundle();
                    paramAnonymousView.putBoolean("onlyUsers", true);
                    paramAnonymousView.putBoolean("destroyAfterSelect", true);
                    paramAnonymousView.putBoolean("createSecretChat", true);
                    paramAnonymousView.putBoolean("allowBots", false);
                    LaunchActivity.this.presentFragment(new ContactsActivity(paramAnonymousView));
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 4)
                  {
                    paramAnonymousView = MessagesController.getGlobalMainSettings();
                    if ((!BuildVars.DEBUG_VERSION) && (paramAnonymousView.getBoolean("channel_intro", false)))
                    {
                      paramAnonymousView = new Bundle();
                      paramAnonymousView.putInt("step", 0);
                      LaunchActivity.this.presentFragment(new ChannelCreateActivity(paramAnonymousView));
                    }
                    for (;;)
                    {
                      LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                      return;
                      LaunchActivity.this.presentFragment(new ChannelIntroActivity());
                      paramAnonymousView.edit().putBoolean("channel_intro", true).commit();
                    }
                  }
                  if (paramAnonymousInt == 6)
                  {
                    LaunchActivity.this.presentFragment(new ContactsActivity(null));
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 7)
                  {
                    LaunchActivity.this.presentFragment(new InviteContactsActivity());
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 8)
                  {
                    LaunchActivity.this.presentFragment(new SettingsActivity());
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 9)
                  {
                    Browser.openUrl(LaunchActivity.this, LocaleController.getString("TelegramFaqUrl", 2131494471));
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                  if (paramAnonymousInt == 10)
                  {
                    LaunchActivity.this.presentFragment(new CallLogActivity());
                    LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    return;
                  }
                } while (paramAnonymousInt != 11);
                paramAnonymousView = new Bundle();
                paramAnonymousView.putInt("user_id", UserConfig.getInstance(LaunchActivity.this.currentAccount).getClientUserId());
                LaunchActivity.this.presentFragment(new ChatActivity(paramAnonymousView));
                LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
              }
            });
            this.drawerLayoutContainer.setParentActionBarLayout(this.actionBarLayout);
            this.actionBarLayout.setDrawerLayoutContainer(this.drawerLayoutContainer);
            this.actionBarLayout.init(mainFragmentsStack);
            this.actionBarLayout.setDelegate(this);
            Theme.loadWallpaper();
            this.passcodeView = new PasscodeView(this);
            this.drawerLayoutContainer.addView(this.passcodeView, LayoutHelper.createFrame(-1, -1.0F));
            checkCurrentAccount();
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, new Object[] { this });
            this.currentConnectionState = ConnectionsManager.getInstance(this.currentAccount).getConnectionState();
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.reloadInterface);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewTheme);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.needSetDayNightTheme);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeOtherAppActivities);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetNewWallpapper);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.notificationsCountUpdated);
            if (!this.actionBarLayout.fragmentsStack.isEmpty()) {
              break label1982;
            }
            if (UserConfig.getInstance(this.currentAccount).isClientActivated()) {
              break label1577;
            }
            this.actionBarLayout.addFragmentToStack(new LoginActivity());
            this.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            if (paramBundle == null) {}
          }
        }
        try
        {
          localObject1 = paramBundle.getString("fragment");
          if (localObject1 != null)
          {
            localObject4 = paramBundle.getBundle("args");
            i = -1;
            int j = ((String)localObject1).hashCode();
            switch (j)
            {
            default: 
              switch (i)
              {
              }
              break;
            }
          }
        }
        catch (Exception localException2)
        {
          for (;;)
          {
            FileLog.e(localException2);
            continue;
            localObject3 = new SettingsActivity();
            this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3);
            ((SettingsActivity)localObject3).restoreSelfArgs(paramBundle);
            continue;
            if (localObject4 != null)
            {
              localObject3 = new GroupCreateFinalActivity((Bundle)localObject4);
              if (this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3))
              {
                ((GroupCreateFinalActivity)localObject3).restoreSelfArgs(paramBundle);
                continue;
                if (localObject4 != null)
                {
                  localObject3 = new ChannelCreateActivity((Bundle)localObject4);
                  if (this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3))
                  {
                    ((ChannelCreateActivity)localObject3).restoreSelfArgs(paramBundle);
                    continue;
                    if (localObject4 != null)
                    {
                      localObject3 = new ChannelEditActivity((Bundle)localObject4);
                      if (this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3))
                      {
                        ((ChannelEditActivity)localObject3).restoreSelfArgs(paramBundle);
                        continue;
                        if (localObject4 != null)
                        {
                          localObject3 = new ProfileActivity((Bundle)localObject4);
                          if (this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3))
                          {
                            ((ProfileActivity)localObject3).restoreSelfArgs(paramBundle);
                            continue;
                            localObject3 = new WallpapersActivity();
                            this.actionBarLayout.addFragmentToStack((BaseFragment)localObject3);
                            ((WallpapersActivity)localObject3).restoreSelfArgs(paramBundle);
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
        checkLayout();
        localObject1 = getIntent();
        if (paramBundle != null)
        {
          bool1 = true;
          handleIntent((Intent)localObject1, false, bool1, false);
        }
        try
        {
          paramBundle = Build.DISPLAY;
          localObject1 = Build.USER;
          if (paramBundle == null) {
            break label2166;
          }
          paramBundle = paramBundle.toLowerCase();
          if (localObject1 == null) {
            break label2173;
          }
          localObject1 = paramBundle.toLowerCase();
          if ((paramBundle.contains("flyme")) || (((String)localObject1).contains("flyme")))
          {
            AndroidUtilities.incorrectDisplaySizeFix = true;
            localObject1 = getWindow().getDecorView().getRootView();
            paramBundle = ((View)localObject1).getViewTreeObserver();
            localObject1 = new ViewTreeObserver.OnGlobalLayoutListener()
            {
              public void onGlobalLayout()
              {
                int j = localObject1.getMeasuredHeight();
                int i = j;
                if (Build.VERSION.SDK_INT >= 21) {
                  i = j - AndroidUtilities.statusBarHeight;
                }
                if ((i > AndroidUtilities.dp(100.0F)) && (i < AndroidUtilities.displaySize.y) && (AndroidUtilities.dp(100.0F) + i > AndroidUtilities.displaySize.y))
                {
                  AndroidUtilities.displaySize.y = i;
                  if (BuildVars.LOGS_ENABLED) {
                    FileLog.d("fix display size y to " + AndroidUtilities.displaySize.y);
                  }
                }
              }
            };
            this.onGlobalLayoutListener = ((ViewTreeObserver.OnGlobalLayoutListener)localObject1);
            paramBundle.addOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener)localObject1);
          }
        }
        catch (Exception paramBundle)
        {
          for (;;)
          {
            Object localObject2;
            Object localObject3;
            boolean bool2;
            FileLog.e(paramBundle);
          }
        }
        MediaController.getInstance().setBaseActivity(this, true);
        return;
      }
      catch (Exception localException1)
      {
        for (;;)
        {
          FileLog.e(localException1);
          continue;
          int i = 0;
          continue;
          label1516:
          i = 0;
          continue;
          this.drawerLayoutContainer.addView(this.actionBarLayout, new ViewGroup.LayoutParams(-1, -1));
          continue;
          label1544:
          i = Math.min(AndroidUtilities.dp(320.0F), Math.min(((Point)localObject4).x, ((Point)localObject4).y) - AndroidUtilities.dp(56.0F));
          continue;
          label1577:
          localObject2 = new DialogsActivity(null);
          ((DialogsActivity)localObject2).setSideMenu(this.sideMenu);
          this.actionBarLayout.addFragmentToStack((BaseFragment)localObject2);
          this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
          continue;
          if (((String)localObject2).equals("chat"))
          {
            i = 0;
            continue;
            if (((String)localObject2).equals("settings"))
            {
              i = 1;
              continue;
              if (((String)localObject2).equals("group"))
              {
                i = 2;
                continue;
                if (((String)localObject2).equals("channel"))
                {
                  i = 3;
                  continue;
                  if (((String)localObject2).equals("edit"))
                  {
                    i = 4;
                    continue;
                    if (((String)localObject2).equals("chat_profile"))
                    {
                      i = 5;
                      continue;
                      if (((String)localObject2).equals("wallpapers"))
                      {
                        i = 6;
                        continue;
                        if (localObject4 != null)
                        {
                          localObject2 = new ChatActivity((Bundle)localObject4);
                          if (this.actionBarLayout.addFragmentToStack((BaseFragment)localObject2))
                          {
                            ((ChatActivity)localObject2).restoreSelfArgs(paramBundle);
                            continue;
                            label1982:
                            localObject3 = (BaseFragment)this.actionBarLayout.fragmentsStack.get(0);
                            if ((localObject3 instanceof DialogsActivity)) {
                              ((DialogsActivity)localObject3).setSideMenu(this.sideMenu);
                            }
                            bool1 = true;
                            if (AndroidUtilities.isTablet()) {
                              if ((this.actionBarLayout.fragmentsStack.size() > 1) || (!this.layersActionBarLayout.fragmentsStack.isEmpty())) {
                                break label2154;
                              }
                            }
                            label2154:
                            for (bool2 = true;; bool2 = false)
                            {
                              bool1 = bool2;
                              if (this.layersActionBarLayout.fragmentsStack.size() == 1)
                              {
                                bool1 = bool2;
                                if ((this.layersActionBarLayout.fragmentsStack.get(0) instanceof LoginActivity)) {
                                  bool1 = false;
                                }
                              }
                              bool2 = bool1;
                              if (this.actionBarLayout.fragmentsStack.size() == 1)
                              {
                                bool2 = bool1;
                                if ((this.actionBarLayout.fragmentsStack.get(0) instanceof LoginActivity)) {
                                  bool2 = false;
                                }
                              }
                              this.drawerLayoutContainer.setAllowOpenDrawer(bool2, false);
                              break;
                            }
                            bool1 = false;
                            continue;
                            label2166:
                            paramBundle = "";
                            continue;
                            label2173:
                            localObject3 = "";
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
    }
    catch (Exception localException3)
    {
      for (;;) {}
    }
  }
  
  protected void onDestroy()
  {
    if (PhotoViewer.getPipInstance() != null) {
      PhotoViewer.getPipInstance().destroyPhotoViewer();
    }
    if (PhotoViewer.hasInstance()) {
      PhotoViewer.getInstance().destroyPhotoViewer();
    }
    if (SecretMediaViewer.hasInstance()) {
      SecretMediaViewer.getInstance().destroyPhotoViewer();
    }
    if (ArticleViewer.hasInstance()) {
      ArticleViewer.getInstance().destroyArticleViewer();
    }
    if (StickerPreviewViewer.hasInstance()) {
      StickerPreviewViewer.getInstance().destroy();
    }
    Object localObject = PipRoundVideoView.getInstance();
    MediaController.getInstance().setBaseActivity(this, false);
    MediaController.getInstance().setFeedbackView(this.actionBarLayout, false);
    if (localObject != null) {
      ((PipRoundVideoView)localObject).close(false);
    }
    Theme.destroyResources();
    localObject = EmbedBottomSheet.getInstance();
    if (localObject != null) {
      ((EmbedBottomSheet)localObject).destroy();
    }
    localObject = ThemeEditorView.getInstance();
    if (localObject != null) {
      ((ThemeEditorView)localObject).destroy();
    }
    try
    {
      if (this.visibleDialog != null)
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
    }
    catch (Exception localException1)
    {
      try
      {
        for (;;)
        {
          if (this.onGlobalLayoutListener != null) {
            getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
          }
          super.onDestroy();
          onFinish();
          return;
          localException1 = localException1;
          FileLog.e(localException1);
        }
      }
      catch (Exception localException2)
      {
        for (;;)
        {
          FileLog.e(localException2);
        }
      }
    }
  }
  
  public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent)
  {
    if ((paramInt == 82) && (!SharedConfig.isWaitingForPasscodeEnter))
    {
      if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible())) {
        return super.onKeyUp(paramInt, paramKeyEvent);
      }
      if ((ArticleViewer.hasInstance()) && (ArticleViewer.getInstance().isVisible())) {
        return super.onKeyUp(paramInt, paramKeyEvent);
      }
      if (!AndroidUtilities.isTablet()) {
        break label151;
      }
      if ((this.layersActionBarLayout.getVisibility() != 0) || (this.layersActionBarLayout.fragmentsStack.isEmpty())) {
        break label102;
      }
      this.layersActionBarLayout.onKeyUp(paramInt, paramKeyEvent);
    }
    for (;;)
    {
      return super.onKeyUp(paramInt, paramKeyEvent);
      label102:
      if ((this.rightActionBarLayout.getVisibility() == 0) && (!this.rightActionBarLayout.fragmentsStack.isEmpty()))
      {
        this.rightActionBarLayout.onKeyUp(paramInt, paramKeyEvent);
      }
      else
      {
        this.actionBarLayout.onKeyUp(paramInt, paramKeyEvent);
        continue;
        label151:
        if (this.actionBarLayout.fragmentsStack.size() == 1)
        {
          if (!this.drawerLayoutContainer.isDrawerOpened())
          {
            if (getCurrentFocus() != null) {
              AndroidUtilities.hideKeyboard(getCurrentFocus());
            }
            this.drawerLayoutContainer.openDrawer(false);
          }
          else
          {
            this.drawerLayoutContainer.closeDrawer(false);
          }
        }
        else {
          this.actionBarLayout.onKeyUp(paramInt, paramKeyEvent);
        }
      }
    }
  }
  
  public void onLowMemory()
  {
    super.onLowMemory();
    this.actionBarLayout.onLowMemory();
    if (AndroidUtilities.isTablet())
    {
      this.rightActionBarLayout.onLowMemory();
      this.layersActionBarLayout.onLowMemory();
    }
  }
  
  public void onMultiWindowModeChanged(boolean paramBoolean)
  {
    AndroidUtilities.isInMultiwindow = paramBoolean;
    checkLayout();
  }
  
  protected void onNewIntent(Intent paramIntent)
  {
    super.onNewIntent(paramIntent);
    handleIntent(paramIntent, true, false, false);
  }
  
  protected void onPause()
  {
    super.onPause();
    SharedConfig.lastAppPauseTime = System.currentTimeMillis();
    ApplicationLoader.mainInterfacePaused = true;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ApplicationLoader.mainInterfacePausedStageQueue = true;
        ApplicationLoader.mainInterfacePausedStageQueueTime = 0L;
      }
    });
    onPasscodePause();
    this.actionBarLayout.onPause();
    if (AndroidUtilities.isTablet())
    {
      this.rightActionBarLayout.onPause();
      this.layersActionBarLayout.onPause();
    }
    if (this.passcodeView != null) {
      this.passcodeView.onPause();
    }
    ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
    AndroidUtilities.unregisterUpdates();
    if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible())) {
      PhotoViewer.getInstance().onPause();
    }
  }
  
  public boolean onPreIme()
  {
    if ((SecretMediaViewer.hasInstance()) && (SecretMediaViewer.getInstance().isVisible()))
    {
      SecretMediaViewer.getInstance().closePhoto(true, false);
      return true;
    }
    if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible()))
    {
      PhotoViewer.getInstance().closePhoto(true, false);
      return true;
    }
    if ((ArticleViewer.hasInstance()) && (ArticleViewer.getInstance().isVisible()))
    {
      ArticleViewer.getInstance().close(true, false);
      return true;
    }
    return false;
  }
  
  public void onRebuildAllFragments(ActionBarLayout paramActionBarLayout, boolean paramBoolean)
  {
    if ((AndroidUtilities.isTablet()) && (paramActionBarLayout == this.layersActionBarLayout))
    {
      this.rightActionBarLayout.rebuildAllFragmentViews(paramBoolean, paramBoolean);
      this.actionBarLayout.rebuildAllFragmentViews(paramBoolean, paramBoolean);
    }
    this.drawerLayoutAdapter.notifyDataSetChanged();
  }
  
  public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    super.onRequestPermissionsResult(paramInt, paramArrayOfString, paramArrayOfInt);
    int j;
    int i;
    if ((paramInt == 3) || (paramInt == 4) || (paramInt == 5) || (paramInt == 19) || (paramInt == 20))
    {
      j = 1;
      i = j;
      if (paramArrayOfInt.length > 0)
      {
        i = j;
        if (paramArrayOfInt[0] == 0) {
          if (paramInt == 4) {
            ImageLoader.getInstance().checkMediaPaths();
          }
        }
      }
    }
    do
    {
      do
      {
        do
        {
          return;
          if (paramInt == 5)
          {
            ContactsController.getInstance(this.currentAccount).forceImportContacts();
            return;
          }
          if (paramInt != 3) {
            break;
          }
        } while (!SharedConfig.inappCamera);
        CameraController.getInstance().initCamera();
        return;
        if (paramInt != 19)
        {
          i = j;
          if (paramInt != 20) {}
        }
        else
        {
          i = 0;
        }
        if (i != 0)
        {
          paramArrayOfString = new AlertDialog.Builder(this);
          paramArrayOfString.setTitle(LocaleController.getString("AppName", 2131492981));
          if (paramInt == 3) {
            paramArrayOfString.setMessage(LocaleController.getString("PermissionNoAudio", 2131494142));
          }
          for (;;)
          {
            paramArrayOfString.setNegativeButton(LocaleController.getString("PermissionOpenSettings", 2131494147), new DialogInterface.OnClickListener()
            {
              @TargetApi(9)
              public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
              {
                try
                {
                  paramAnonymousDialogInterface = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                  paramAnonymousDialogInterface.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                  LaunchActivity.this.startActivity(paramAnonymousDialogInterface);
                  return;
                }
                catch (Exception paramAnonymousDialogInterface)
                {
                  FileLog.e(paramAnonymousDialogInterface);
                }
              }
            });
            paramArrayOfString.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
            paramArrayOfString.show();
            return;
            if (paramInt == 4) {
              paramArrayOfString.setMessage(LocaleController.getString("PermissionStorage", 2131494148));
            } else if (paramInt == 5) {
              paramArrayOfString.setMessage(LocaleController.getString("PermissionContacts", 2131494140));
            } else if ((paramInt == 19) || (paramInt == 20)) {
              paramArrayOfString.setMessage(LocaleController.getString("PermissionNoCamera", 2131494144));
            }
          }
          if ((paramInt == 2) && (paramArrayOfInt.length > 0) && (paramArrayOfInt[0] == 0)) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.locationPermissionGranted, new Object[0]);
          }
        }
        if (this.actionBarLayout.fragmentsStack.size() != 0) {
          ((BaseFragment)this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(paramInt, paramArrayOfString, paramArrayOfInt);
        }
      } while (!AndroidUtilities.isTablet());
      if (this.rightActionBarLayout.fragmentsStack.size() != 0) {
        ((BaseFragment)this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(paramInt, paramArrayOfString, paramArrayOfInt);
      }
    } while (this.layersActionBarLayout.fragmentsStack.size() == 0);
    ((BaseFragment)this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1)).onRequestPermissionsResultFragment(paramInt, paramArrayOfString, paramArrayOfInt);
  }
  
  protected void onResume()
  {
    super.onResume();
    MediaController.getInstance().setFeedbackView(this.actionBarLayout, true);
    showLanguageAlert(false);
    ApplicationLoader.mainInterfacePaused = false;
    org.telegram.messenger.NotificationsController.lastNoDataNotificationTime = 0L;
    Utilities.stageQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        ApplicationLoader.mainInterfacePausedStageQueue = false;
        ApplicationLoader.mainInterfacePausedStageQueueTime = System.currentTimeMillis();
      }
    });
    checkFreeDiscSpace();
    MediaController.checkGallery();
    onPasscodeResume();
    if (this.passcodeView.getVisibility() != 0)
    {
      this.actionBarLayout.onResume();
      if (AndroidUtilities.isTablet())
      {
        this.rightActionBarLayout.onResume();
        this.layersActionBarLayout.onResume();
      }
    }
    for (;;)
    {
      AndroidUtilities.checkForCrashes(this);
      AndroidUtilities.checkForUpdates(this);
      ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
      updateCurrentConnectionState(this.currentAccount);
      if ((PhotoViewer.hasInstance()) && (PhotoViewer.getInstance().isVisible())) {
        PhotoViewer.getInstance().onResume();
      }
      if ((PipRoundVideoView.getInstance() != null) && (MediaController.getInstance().isMessagePaused()))
      {
        MessageObject localMessageObject = MediaController.getInstance().getPlayingMessageObject();
        if (localMessageObject != null) {
          MediaController.getInstance().seekToProgress(localMessageObject, localMessageObject.audioProgress);
        }
      }
      return;
      this.actionBarLayout.dismissDialogs();
      if (AndroidUtilities.isTablet())
      {
        this.rightActionBarLayout.dismissDialogs();
        this.layersActionBarLayout.dismissDialogs();
      }
      this.passcodeView.onResume();
    }
  }
  
  protected void onSaveInstanceState(Bundle paramBundle)
  {
    for (;;)
    {
      BaseFragment localBaseFragment;
      Bundle localBundle;
      try
      {
        super.onSaveInstanceState(paramBundle);
        localBaseFragment = null;
        if (AndroidUtilities.isTablet())
        {
          if (!this.layersActionBarLayout.fragmentsStack.isEmpty())
          {
            localBaseFragment = (BaseFragment)this.layersActionBarLayout.fragmentsStack.get(this.layersActionBarLayout.fragmentsStack.size() - 1);
            if (localBaseFragment == null) {
              break;
            }
            localBundle = localBaseFragment.getArguments();
            if (((localBaseFragment instanceof ChatActivity)) && (localBundle != null))
            {
              paramBundle.putBundle("args", localBundle);
              paramBundle.putString("fragment", "chat");
              localBaseFragment.saveSelfArgs(paramBundle);
            }
          }
          else
          {
            if (!this.rightActionBarLayout.fragmentsStack.isEmpty())
            {
              localBaseFragment = (BaseFragment)this.rightActionBarLayout.fragmentsStack.get(this.rightActionBarLayout.fragmentsStack.size() - 1);
              continue;
            }
            if (this.actionBarLayout.fragmentsStack.isEmpty()) {
              continue;
            }
            localBaseFragment = (BaseFragment)this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
            continue;
          }
        }
        else
        {
          if (this.actionBarLayout.fragmentsStack.isEmpty()) {
            continue;
          }
          localBaseFragment = (BaseFragment)this.actionBarLayout.fragmentsStack.get(this.actionBarLayout.fragmentsStack.size() - 1);
          continue;
        }
        if ((localBaseFragment instanceof SettingsActivity))
        {
          paramBundle.putString("fragment", "settings");
          continue;
        }
        if (!(localBaseFragment instanceof GroupCreateFinalActivity)) {
          break label280;
        }
      }
      catch (Exception paramBundle)
      {
        FileLog.e(paramBundle);
        return;
      }
      if (localBundle != null)
      {
        paramBundle.putBundle("args", localBundle);
        paramBundle.putString("fragment", "group");
      }
      else
      {
        label280:
        if ((localBaseFragment instanceof WallpapersActivity))
        {
          paramBundle.putString("fragment", "wallpapers");
        }
        else if (((localBaseFragment instanceof ProfileActivity)) && (((ProfileActivity)localBaseFragment).isChat()) && (localBundle != null))
        {
          paramBundle.putBundle("args", localBundle);
          paramBundle.putString("fragment", "chat_profile");
        }
        else if (((localBaseFragment instanceof ChannelCreateActivity)) && (localBundle != null) && (localBundle.getInt("step") == 0))
        {
          paramBundle.putBundle("args", localBundle);
          paramBundle.putString("fragment", "channel");
        }
        else if (((localBaseFragment instanceof ChannelEditActivity)) && (localBundle != null))
        {
          paramBundle.putBundle("args", localBundle);
          paramBundle.putString("fragment", "edit");
        }
      }
    }
  }
  
  protected void onStart()
  {
    super.onStart();
    Browser.bindCustomTabsService(this);
  }
  
  protected void onStop()
  {
    super.onStop();
    Browser.unbindCustomTabsService(this);
  }
  
  public void presentFragment(BaseFragment paramBaseFragment)
  {
    this.actionBarLayout.presentFragment(paramBaseFragment);
  }
  
  public boolean presentFragment(BaseFragment paramBaseFragment, boolean paramBoolean1, boolean paramBoolean2)
  {
    return this.actionBarLayout.presentFragment(paramBaseFragment, paramBoolean1, paramBoolean2, true);
  }
  
  public void rebuildAllFragments(boolean paramBoolean)
  {
    if (this.layersActionBarLayout != null)
    {
      this.layersActionBarLayout.rebuildAllFragmentViews(paramBoolean, paramBoolean);
      return;
    }
    this.actionBarLayout.rebuildAllFragmentViews(paramBoolean, paramBoolean);
  }
  
  public AlertDialog showAlertDialog(AlertDialog.Builder paramBuilder)
  {
    try
    {
      if (this.visibleDialog != null)
      {
        this.visibleDialog.dismiss();
        this.visibleDialog = null;
      }
    }
    catch (Exception localException)
    {
      for (;;)
      {
        try
        {
          this.visibleDialog = paramBuilder.show();
          this.visibleDialog.setCanceledOnTouchOutside(true);
          this.visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
          {
            public void onDismiss(DialogInterface paramAnonymousDialogInterface)
            {
              if ((LaunchActivity.this.visibleDialog != null) && (LaunchActivity.this.visibleDialog == LaunchActivity.this.localeDialog)) {}
              for (;;)
              {
                try
                {
                  paramAnonymousDialogInterface = LocaleController.getInstance().getCurrentLocaleInfo().shortName;
                  LaunchActivity localLaunchActivity1 = LaunchActivity.this;
                  LaunchActivity localLaunchActivity2 = LaunchActivity.this;
                  if (!paramAnonymousDialogInterface.equals("en")) {
                    continue;
                  }
                  paramAnonymousDialogInterface = LaunchActivity.this.englishLocaleStrings;
                  Toast.makeText(localLaunchActivity1, localLaunchActivity2.getStringForLanguageAlert(paramAnonymousDialogInterface, "ChangeLanguageLater", 2131493135), 1).show();
                }
                catch (Exception paramAnonymousDialogInterface)
                {
                  FileLog.e(paramAnonymousDialogInterface);
                  continue;
                }
                LaunchActivity.access$1702(LaunchActivity.this, null);
                LaunchActivity.access$1602(LaunchActivity.this, null);
                return;
                paramAnonymousDialogInterface = LaunchActivity.this.systemLocaleStrings;
              }
            }
          });
          paramBuilder = this.visibleDialog;
          return paramBuilder;
        }
        catch (Exception paramBuilder)
        {
          FileLog.e(paramBuilder);
        }
        localException = localException;
        FileLog.e(localException);
      }
    }
    return null;
  }
  
  public void switchToAccount(int paramInt, boolean paramBoolean)
  {
    if (paramInt == UserConfig.selectedAccount) {
      return;
    }
    ConnectionsManager.getInstance(this.currentAccount).setAppPaused(true, false);
    UserConfig.selectedAccount = paramInt;
    UserConfig.getInstance(0).saveConfig(false);
    checkCurrentAccount();
    if (AndroidUtilities.isTablet())
    {
      this.layersActionBarLayout.removeAllFragments();
      this.rightActionBarLayout.removeAllFragments();
      if (!this.tabletFullSize)
      {
        this.shadowTabletSide.setVisibility(0);
        if (this.rightActionBarLayout.fragmentsStack.isEmpty()) {
          this.backgroundTablet.setVisibility(0);
        }
      }
    }
    if (paramBoolean) {
      this.actionBarLayout.removeAllFragments();
    }
    for (;;)
    {
      DialogsActivity localDialogsActivity = new DialogsActivity(null);
      localDialogsActivity.setSideMenu(this.sideMenu);
      this.actionBarLayout.addFragmentToStack(localDialogsActivity, 0);
      this.drawerLayoutContainer.setAllowOpenDrawer(true, false);
      this.actionBarLayout.showLastFragment();
      if (AndroidUtilities.isTablet())
      {
        this.layersActionBarLayout.showLastFragment();
        this.rightActionBarLayout.showLastFragment();
      }
      if (ApplicationLoader.mainInterfacePaused) {
        break;
      }
      ConnectionsManager.getInstance(this.currentAccount).setAppPaused(false, false);
      return;
      this.actionBarLayout.removeFragmentFromStack(0);
    }
  }
  
  private class VcardData
  {
    String name;
    ArrayList<String> phones = new ArrayList();
    
    private VcardData() {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/LaunchActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */