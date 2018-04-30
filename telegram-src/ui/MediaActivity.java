package org.telegram.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.Adapter;
import org.telegram.messenger.support.widget.RecyclerView.LayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterDocument;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterMusic;
import org.telegram.tgnet.TLRPC.TL_inputMessagesFilterUrl;
import org.telegram.tgnet.TLRPC.TL_messages_search;
import org.telegram.tgnet.TLRPC.TL_webPageEmpty;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.tgnet.TLRPC.messages_Messages;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.ActionBarPopupWindow.ActionBarPopupWindowLayout;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.ActionBar.ThemeDescription.ThemeDescriptionDelegate;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Cells.SharedLinkCell;
import org.telegram.ui.Cells.SharedLinkCell.SharedLinkCellDelegate;
import org.telegram.ui.Cells.SharedMediaSectionCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell.SharedPhotoVideoCellDelegate;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmbedBottomSheet;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SectionsAdapter;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class MediaActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private static final int delete = 4;
  private static final int files_item = 2;
  private static final int forward = 3;
  private static final int gotochat = 7;
  private static final int links_item = 5;
  private static final int music_item = 6;
  private static final int shared_media_item = 1;
  private ArrayList<View> actionModeViews = new ArrayList();
  private SharedDocumentsAdapter audioAdapter;
  private MediaSearchAdapter audioSearchAdapter;
  private int cantDeleteMessagesCount;
  private ArrayList<SharedPhotoVideoCell> cellCache = new ArrayList(6);
  private int columnsCount = 4;
  private long dialog_id;
  private SharedDocumentsAdapter documentsAdapter;
  private MediaSearchAdapter documentsSearchAdapter;
  private TextView dropDown;
  private ActionBarMenuItem dropDownContainer;
  private Drawable dropDownDrawable;
  private ImageView emptyImageView;
  private TextView emptyTextView;
  private LinearLayout emptyView;
  private FragmentContextView fragmentContextView;
  private ActionBarMenuItem gotoItem;
  protected TLRPC.ChatFull info = null;
  private LinearLayoutManager layoutManager;
  private SharedLinksAdapter linksAdapter;
  private MediaSearchAdapter linksSearchAdapter;
  private RecyclerListView listView;
  private long mergeDialogId;
  private SharedPhotoVideoAdapter photoVideoAdapter;
  private ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout;
  private RadialProgressView progressBar;
  private LinearLayout progressView;
  private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider()
  {
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject paramAnonymousMessageObject, TLRPC.FileLocation paramAnonymousFileLocation, int paramAnonymousInt)
    {
      if ((paramAnonymousMessageObject == null) || (MediaActivity.this.listView == null) || (MediaActivity.this.selectedMode != 0)) {
        return null;
      }
      int j = MediaActivity.this.listView.getChildCount();
      paramAnonymousInt = 0;
      if (paramAnonymousInt < j)
      {
        paramAnonymousFileLocation = MediaActivity.this.listView.getChildAt(paramAnonymousInt);
        Object localObject;
        int i;
        if ((paramAnonymousFileLocation instanceof SharedPhotoVideoCell))
        {
          localObject = (SharedPhotoVideoCell)paramAnonymousFileLocation;
          i = 0;
        }
        for (;;)
        {
          MessageObject localMessageObject;
          if (i < 6)
          {
            localMessageObject = ((SharedPhotoVideoCell)localObject).getMessageObject(i);
            if (localMessageObject != null) {}
          }
          else
          {
            paramAnonymousInt += 1;
            break;
          }
          paramAnonymousFileLocation = ((SharedPhotoVideoCell)localObject).getImageView(i);
          if (localMessageObject.getId() == paramAnonymousMessageObject.getId())
          {
            paramAnonymousMessageObject = new int[2];
            paramAnonymousFileLocation.getLocationInWindow(paramAnonymousMessageObject);
            localObject = new PhotoViewer.PlaceProviderObject();
            ((PhotoViewer.PlaceProviderObject)localObject).viewX = paramAnonymousMessageObject[0];
            i = paramAnonymousMessageObject[1];
            if (Build.VERSION.SDK_INT >= 21) {}
            for (paramAnonymousInt = 0;; paramAnonymousInt = AndroidUtilities.statusBarHeight)
            {
              ((PhotoViewer.PlaceProviderObject)localObject).viewY = (i - paramAnonymousInt);
              ((PhotoViewer.PlaceProviderObject)localObject).parentView = MediaActivity.this.listView;
              ((PhotoViewer.PlaceProviderObject)localObject).imageReceiver = paramAnonymousFileLocation.getImageReceiver();
              ((PhotoViewer.PlaceProviderObject)localObject).thumb = ((PhotoViewer.PlaceProviderObject)localObject).imageReceiver.getBitmapSafe();
              ((PhotoViewer.PlaceProviderObject)localObject).parentView.getLocationInWindow(paramAnonymousMessageObject);
              ((PhotoViewer.PlaceProviderObject)localObject).clipTopAddition = AndroidUtilities.dp(40.0F);
              return (PhotoViewer.PlaceProviderObject)localObject;
            }
          }
          i += 1;
        }
      }
      return null;
    }
  };
  private boolean scrolling;
  private ActionBarMenuItem searchItem;
  private boolean searchWas;
  private boolean searching;
  private SparseArray<MessageObject>[] selectedFiles = { new SparseArray(), new SparseArray() };
  private NumberTextView selectedMessagesCountTextView;
  private int selectedMode;
  private SharedMediaData[] sharedMediaData = new SharedMediaData[5];
  
  public MediaActivity(Bundle paramBundle)
  {
    super(paramBundle);
  }
  
  private void fixLayoutInternal()
  {
    int i = 0;
    if (this.listView == null) {
      return;
    }
    int j = ((WindowManager)ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
    if ((!AndroidUtilities.isTablet()) && (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == 2))
    {
      this.selectedMessagesCountTextView.setTextSize(18);
      label62:
      if (!AndroidUtilities.isTablet()) {
        break label200;
      }
      this.columnsCount = 4;
      this.emptyTextView.setPadding(AndroidUtilities.dp(40.0F), 0, AndroidUtilities.dp(40.0F), AndroidUtilities.dp(128.0F));
    }
    for (;;)
    {
      this.photoVideoAdapter.notifyDataSetChanged();
      if (this.dropDownContainer == null) {
        break;
      }
      if (!AndroidUtilities.isTablet())
      {
        FrameLayout.LayoutParams localLayoutParams = (FrameLayout.LayoutParams)this.dropDownContainer.getLayoutParams();
        if (Build.VERSION.SDK_INT >= 21) {
          i = AndroidUtilities.statusBarHeight;
        }
        localLayoutParams.topMargin = i;
        this.dropDownContainer.setLayoutParams(localLayoutParams);
      }
      if ((AndroidUtilities.isTablet()) || (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != 2)) {
        break label274;
      }
      this.dropDown.setTextSize(18.0F);
      return;
      this.selectedMessagesCountTextView.setTextSize(20);
      break label62;
      label200:
      if ((j == 3) || (j == 1))
      {
        this.columnsCount = 6;
        this.emptyTextView.setPadding(AndroidUtilities.dp(40.0F), 0, AndroidUtilities.dp(40.0F), 0);
      }
      else
      {
        this.columnsCount = 4;
        this.emptyTextView.setPadding(AndroidUtilities.dp(40.0F), 0, AndroidUtilities.dp(40.0F), AndroidUtilities.dp(128.0F));
      }
    }
    label274:
    this.dropDown.setTextSize(20.0F);
  }
  
  private void onItemClick(int paramInt1, View paramView, MessageObject paramMessageObject, int paramInt2)
  {
    if (paramMessageObject == null) {}
    label29:
    label76:
    label107:
    label136:
    label166:
    label222:
    label281:
    label311:
    label318:
    label325:
    label331:
    do
    {
      do
      {
        do
        {
          return;
          if (!this.actionBar.isActionModeShowed()) {
            break;
          }
          int i;
          if (paramMessageObject.getDialogId() == this.dialog_id)
          {
            paramInt1 = 0;
            if (this.selectedFiles[paramInt1].indexOfKey(paramMessageObject.getId()) < 0) {
              break label222;
            }
            this.selectedFiles[paramInt1].remove(paramMessageObject.getId());
            if (!paramMessageObject.canDeleteMessage(null)) {
              this.cantDeleteMessagesCount -= 1;
            }
            if ((this.selectedFiles[0].size() != 0) || (this.selectedFiles[1].size() != 0)) {
              break label281;
            }
            this.actionBar.hideActionMode();
            if (this.gotoItem != null)
            {
              localObject1 = this.gotoItem;
              if (this.selectedFiles[0].size() != 1) {
                break label311;
              }
              i = 0;
              ((ActionBarMenuItem)localObject1).setVisibility(i);
            }
            localObject1 = this.actionBar.createActionMode().getItem(4);
            if (this.cantDeleteMessagesCount != 0) {
              break label318;
            }
            i = 0;
            ((ActionBarMenuItem)localObject1).setVisibility(i);
            this.scrolling = false;
            if (!(paramView instanceof SharedDocumentCell)) {
              break label331;
            }
            paramView = (SharedDocumentCell)paramView;
            if (this.selectedFiles[paramInt1].indexOfKey(paramMessageObject.getId()) < 0) {
              break label325;
            }
          }
          for (bool = true;; bool = false)
          {
            paramView.setChecked(bool, true);
            return;
            paramInt1 = 1;
            break label29;
            if (this.selectedFiles[0].size() + this.selectedFiles[1].size() >= 100) {
              break;
            }
            this.selectedFiles[paramInt1].put(paramMessageObject.getId(), paramMessageObject);
            if (paramMessageObject.canDeleteMessage(null)) {
              break label76;
            }
            this.cantDeleteMessagesCount += 1;
            break label76;
            this.selectedMessagesCountTextView.setNumber(this.selectedFiles[0].size() + this.selectedFiles[1].size(), true);
            break label107;
            i = 8;
            break label136;
            i = 8;
            break label166;
          }
          if ((paramView instanceof SharedPhotoVideoCell))
          {
            paramView = (SharedPhotoVideoCell)paramView;
            if (this.selectedFiles[paramInt1].indexOfKey(paramMessageObject.getId()) >= 0) {}
            for (bool = true;; bool = false)
            {
              paramView.setChecked(paramInt2, bool, true);
              return;
            }
          }
        } while (!(paramView instanceof SharedLinkCell));
        paramView = (SharedLinkCell)paramView;
        if (this.selectedFiles[paramInt1].indexOfKey(paramMessageObject.getId()) >= 0) {}
        for (boolean bool = true;; bool = false)
        {
          paramView.setChecked(bool, true);
          return;
        }
        if (this.selectedMode == 0)
        {
          PhotoViewer.getInstance().setParentActivity(getParentActivity());
          PhotoViewer.getInstance().openPhoto(this.sharedMediaData[this.selectedMode].messages, paramInt1, this.dialog_id, this.mergeDialogId, this.provider);
          return;
        }
        if ((this.selectedMode != 1) && (this.selectedMode != 4)) {
          break;
        }
      } while (!(paramView instanceof SharedDocumentCell));
      paramView = (SharedDocumentCell)paramView;
      if (!paramView.isLoaded()) {
        break;
      }
    } while ((paramMessageObject.isMusic()) && (MediaController.getInstance().setPlaylist(this.sharedMediaData[this.selectedMode].messages, paramMessageObject)));
    Object localObject1 = null;
    if (paramMessageObject.messageOwner.media != null) {}
    for (Object localObject2 = FileLoader.getAttachFileName(paramMessageObject.getDocument());; localObject2 = "")
    {
      paramView = (View)localObject1;
      if (paramMessageObject.messageOwner.attachPath != null)
      {
        paramView = (View)localObject1;
        if (paramMessageObject.messageOwner.attachPath.length() != 0) {
          paramView = new File(paramMessageObject.messageOwner.attachPath);
        }
      }
      if (paramView != null)
      {
        localObject1 = paramView;
        if (paramView != null)
        {
          localObject1 = paramView;
          if (paramView.exists()) {}
        }
      }
      else
      {
        localObject1 = FileLoader.getPathToMessage(paramMessageObject.messageOwner);
      }
      if ((localObject1 == null) || (!((File)localObject1).exists())) {
        break;
      }
      if (!((File)localObject1).getName().toLowerCase().endsWith("attheme")) {
        break label767;
      }
      paramView = Theme.applyThemeFile((File)localObject1, paramMessageObject.getDocumentName(), true);
      if (paramView == null) {
        break label702;
      }
      presentFragment(new ThemePreviewActivity((File)localObject1, paramView));
      return;
    }
    label702:
    paramView = new AlertDialog.Builder(getParentActivity());
    paramView.setTitle(LocaleController.getString("AppName", 2131492981));
    paramView.setMessage(LocaleController.getString("IncorrectTheme", 2131493674));
    paramView.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
    showDialog(paramView.create());
    return;
    label767:
    paramView = null;
    Intent localIntent;
    for (;;)
    {
      try
      {
        localIntent = new Intent("android.intent.action.VIEW");
        localIntent.setFlags(1);
        localObject3 = MimeTypeMap.getSingleton();
        paramInt1 = ((String)localObject2).lastIndexOf('.');
        if (paramInt1 != -1)
        {
          localObject2 = ((MimeTypeMap)localObject3).getMimeTypeFromExtension(((String)localObject2).substring(paramInt1 + 1).toLowerCase());
          paramView = (View)localObject2;
          if (localObject2 == null)
          {
            localObject2 = paramMessageObject.getDocument().mime_type;
            if (localObject2 == null) {
              break label1276;
            }
            paramView = (View)localObject2;
            if (((String)localObject2).length() == 0) {
              break label1276;
            }
          }
        }
        label860:
        if (Build.VERSION.SDK_INT < 24) {
          break label1054;
        }
        localObject3 = FileProvider.getUriForFile(getParentActivity(), "org.telegram.messenger.provider", (File)localObject1);
        if (paramView == null) {
          break label1046;
        }
        localObject2 = paramView;
        localIntent.setDataAndType((Uri)localObject3, (String)localObject2);
        label899:
        if (paramView == null) {
          break label1098;
        }
        try
        {
          getParentActivity().startActivityForResult(localIntent, 500);
          return;
        }
        catch (Exception paramView)
        {
          if (Build.VERSION.SDK_INT < 24) {
            break label1081;
          }
        }
        localIntent.setDataAndType(FileProvider.getUriForFile(getParentActivity(), "org.telegram.messenger.provider", (File)localObject1), "text/plain");
        label946:
        getParentActivity().startActivityForResult(localIntent, 500);
        return;
      }
      catch (Exception paramView) {}
      if (getParentActivity() == null) {
        break;
      }
      paramView = new AlertDialog.Builder(getParentActivity());
      paramView.setTitle(LocaleController.getString("AppName", 2131492981));
      paramView.setPositiveButton(LocaleController.getString("OK", 2131494028), null);
      paramView.setMessage(LocaleController.formatString("NoHandleAppInstalled", 2131493889, new Object[] { paramMessageObject.getDocument().mime_type }));
      showDialog(paramView.create());
      return;
      label1046:
      localObject2 = "text/plain";
    }
    label1054:
    Object localObject3 = Uri.fromFile((File)localObject1);
    if (paramView != null) {}
    for (localObject2 = paramView;; localObject2 = "text/plain")
    {
      localIntent.setDataAndType((Uri)localObject3, (String)localObject2);
      break label899;
      label1081:
      localIntent.setDataAndType(Uri.fromFile((File)localObject1), "text/plain");
      break label946;
      label1098:
      getParentActivity().startActivityForResult(localIntent, 500);
      return;
      if (!paramView.isLoading())
      {
        FileLoader.getInstance(this.currentAccount).loadFile(paramView.getMessage().getDocument(), false, 0);
        paramView.updateFileExistIcon();
        return;
      }
      FileLoader.getInstance(this.currentAccount).cancelLoadFile(paramView.getMessage().getDocument());
      paramView.updateFileExistIcon();
      return;
      if (this.selectedMode != 3) {
        break;
      }
      try
      {
        localObject2 = paramMessageObject.messageOwner.media.webpage;
        localObject1 = null;
        paramMessageObject = (MessageObject)localObject1;
        if (localObject2 == null) {
          break label1244;
        }
        paramMessageObject = (MessageObject)localObject1;
        if ((localObject2 instanceof TLRPC.TL_webPageEmpty)) {
          break label1244;
        }
        if ((((TLRPC.WebPage)localObject2).embed_url != null) && (((TLRPC.WebPage)localObject2).embed_url.length() != 0))
        {
          openWebView((TLRPC.WebPage)localObject2);
          return;
        }
      }
      catch (Exception paramView)
      {
        FileLog.e(paramView);
        return;
      }
      paramMessageObject = ((TLRPC.WebPage)localObject2).url;
      label1244:
      localObject1 = paramMessageObject;
      if (paramMessageObject == null) {
        localObject1 = ((SharedLinkCell)paramView).getLink(0);
      }
      if (localObject1 == null) {
        break;
      }
      Browser.openUrl(getParentActivity(), (String)localObject1);
      return;
      label1276:
      paramView = null;
      break label860;
    }
  }
  
  private boolean onItemLongClick(MessageObject paramMessageObject, View paramView, int paramInt)
  {
    if (this.actionBar.isActionModeShowed()) {
      return false;
    }
    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
    Object localObject = this.selectedFiles;
    if (paramMessageObject.getDialogId() == this.dialog_id)
    {
      i = 0;
      localObject[i].put(paramMessageObject.getId(), paramMessageObject);
      if (!paramMessageObject.canDeleteMessage(null)) {
        this.cantDeleteMessagesCount += 1;
      }
      paramMessageObject = this.actionBar.createActionMode().getItem(4);
      if (this.cantDeleteMessagesCount != 0) {
        break label219;
      }
    }
    label219:
    for (int i = 0;; i = 8)
    {
      paramMessageObject.setVisibility(i);
      if (this.gotoItem != null) {
        this.gotoItem.setVisibility(0);
      }
      this.selectedMessagesCountTextView.setNumber(1, false);
      paramMessageObject = new AnimatorSet();
      localObject = new ArrayList();
      i = 0;
      while (i < this.actionModeViews.size())
      {
        View localView = (View)this.actionModeViews.get(i);
        AndroidUtilities.clearDrawableAnimation(localView);
        ((ArrayList)localObject).add(ObjectAnimator.ofFloat(localView, "scaleY", new float[] { 0.1F, 1.0F }));
        i += 1;
      }
      i = 1;
      break;
    }
    paramMessageObject.playTogether((Collection)localObject);
    paramMessageObject.setDuration(250L);
    paramMessageObject.start();
    this.scrolling = false;
    if ((paramView instanceof SharedDocumentCell)) {
      ((SharedDocumentCell)paramView).setChecked(true, true);
    }
    for (;;)
    {
      this.actionBar.showActionMode();
      return true;
      if ((paramView instanceof SharedPhotoVideoCell)) {
        ((SharedPhotoVideoCell)paramView).setChecked(paramInt, true, true);
      } else if ((paramView instanceof SharedLinkCell)) {
        ((SharedLinkCell)paramView).setChecked(true, true);
      }
    }
  }
  
  private void openWebView(TLRPC.WebPage paramWebPage)
  {
    EmbedBottomSheet.show(getParentActivity(), paramWebPage.site_name, paramWebPage.description, paramWebPage.url, paramWebPage.embed_url, paramWebPage.embed_width, paramWebPage.embed_height);
  }
  
  private void switchToCurrentSelectedMode()
  {
    if ((this.searching) && (this.searchWas)) {
      if (this.listView != null)
      {
        if (this.selectedMode == 1)
        {
          this.listView.setAdapter(this.documentsSearchAdapter);
          this.documentsSearchAdapter.notifyDataSetChanged();
        }
      }
      else if (this.emptyTextView != null)
      {
        this.emptyTextView.setText(LocaleController.getString("NoResult", 2131493906));
        this.emptyTextView.setTextSize(1, 20.0F);
        this.emptyImageView.setVisibility(8);
      }
    }
    label347:
    Object localObject;
    int i;
    label481:
    label575:
    label783:
    label789:
    label794:
    do
    {
      return;
      if (this.selectedMode == 3)
      {
        this.listView.setAdapter(this.linksSearchAdapter);
        this.linksSearchAdapter.notifyDataSetChanged();
        break;
      }
      if (this.selectedMode != 4) {
        break;
      }
      this.listView.setAdapter(this.audioSearchAdapter);
      this.audioSearchAdapter.notifyDataSetChanged();
      break;
      this.emptyTextView.setTextSize(1, 17.0F);
      this.emptyImageView.setVisibility(0);
      if (this.selectedMode == 0)
      {
        this.listView.setAdapter(this.photoVideoAdapter);
        this.dropDown.setText(LocaleController.getString("SharedMediaTitle", 2131494396));
        this.emptyImageView.setImageResource(2131165675);
        if ((int)this.dialog_id == 0)
        {
          this.emptyTextView.setText(LocaleController.getString("NoMediaSecret", 2131493894));
          this.searchItem.setVisibility(8);
          if ((!this.sharedMediaData[this.selectedMode].loading) || (!this.sharedMediaData[this.selectedMode].messages.isEmpty())) {
            break label347;
          }
          this.progressView.setVisibility(0);
          this.listView.setEmptyView(null);
          this.emptyView.setVisibility(8);
        }
        for (;;)
        {
          this.listView.setVisibility(0);
          this.listView.setPadding(0, 0, 0, AndroidUtilities.dp(4.0F));
          return;
          this.emptyTextView.setText(LocaleController.getString("NoMedia", 2131493892));
          break;
          this.progressView.setVisibility(8);
          this.listView.setEmptyView(this.emptyView);
        }
      }
      if ((this.selectedMode == 1) || (this.selectedMode == 4))
      {
        if (this.selectedMode == 1)
        {
          this.listView.setAdapter(this.documentsAdapter);
          this.dropDown.setText(LocaleController.getString("DocumentsTitle", 2131493394));
          this.emptyImageView.setImageResource(2131165676);
          if ((int)this.dialog_id == 0)
          {
            this.emptyTextView.setText(LocaleController.getString("NoSharedFilesSecret", 2131493910));
            localObject = this.searchItem;
            if (this.sharedMediaData[this.selectedMode].messages.isEmpty()) {
              break label783;
            }
            i = 0;
            ((ActionBarMenuItem)localObject).setVisibility(i);
            if ((!this.sharedMediaData[this.selectedMode].loading) && (this.sharedMediaData[this.selectedMode].endReached[0] == 0) && (this.sharedMediaData[this.selectedMode].messages.isEmpty()))
            {
              SharedMediaData.access$602(this.sharedMediaData[this.selectedMode], true);
              localObject = DataQuery.getInstance(this.currentAccount);
              long l = this.dialog_id;
              if (this.selectedMode != 1) {
                break label789;
              }
              i = 1;
              ((DataQuery)localObject).loadMedia(l, 50, 0, i, true, this.classGuid);
            }
            this.listView.setVisibility(0);
            if ((!this.sharedMediaData[this.selectedMode].loading) || (!this.sharedMediaData[this.selectedMode].messages.isEmpty())) {
              break label794;
            }
            this.progressView.setVisibility(0);
            this.listView.setEmptyView(null);
            this.emptyView.setVisibility(8);
          }
        }
        for (;;)
        {
          this.listView.setPadding(0, 0, 0, AndroidUtilities.dp(4.0F));
          return;
          this.emptyTextView.setText(LocaleController.getString("NoSharedFiles", 2131493909));
          break;
          if (this.selectedMode != 4) {
            break;
          }
          this.listView.setAdapter(this.audioAdapter);
          this.dropDown.setText(LocaleController.getString("AudioTitle", 2131493046));
          this.emptyImageView.setImageResource(2131165678);
          if ((int)this.dialog_id == 0)
          {
            this.emptyTextView.setText(LocaleController.getString("NoSharedAudioSecret", 2131493908));
            break;
          }
          this.emptyTextView.setText(LocaleController.getString("NoSharedAudio", 2131493907));
          break;
          i = 8;
          break label481;
          i = 4;
          break label575;
          this.progressView.setVisibility(8);
          this.listView.setEmptyView(this.emptyView);
        }
      }
    } while (this.selectedMode != 3);
    this.listView.setAdapter(this.linksAdapter);
    this.dropDown.setText(LocaleController.getString("LinksTitle", 2131493758));
    this.emptyImageView.setImageResource(2131165677);
    if ((int)this.dialog_id == 0)
    {
      this.emptyTextView.setText(LocaleController.getString("NoSharedLinksSecret", 2131493912));
      localObject = this.searchItem;
      if (this.sharedMediaData[3].messages.isEmpty()) {
        break label1104;
      }
      i = 0;
      label909:
      ((ActionBarMenuItem)localObject).setVisibility(i);
      if ((!this.sharedMediaData[this.selectedMode].loading) && (this.sharedMediaData[this.selectedMode].endReached[0] == 0) && (this.sharedMediaData[this.selectedMode].messages.isEmpty()))
      {
        SharedMediaData.access$602(this.sharedMediaData[this.selectedMode], true);
        DataQuery.getInstance(this.currentAccount).loadMedia(this.dialog_id, 50, 0, 3, true, this.classGuid);
      }
      this.listView.setVisibility(0);
      if ((!this.sharedMediaData[this.selectedMode].loading) || (!this.sharedMediaData[this.selectedMode].messages.isEmpty())) {
        break label1110;
      }
      this.progressView.setVisibility(0);
      this.listView.setEmptyView(null);
      this.emptyView.setVisibility(8);
    }
    for (;;)
    {
      this.listView.setPadding(0, 0, 0, AndroidUtilities.dp(4.0F));
      return;
      this.emptyTextView.setText(LocaleController.getString("NoSharedLinks", 2131493911));
      break;
      label1104:
      i = 8;
      break label909;
      label1110:
      this.progressView.setVisibility(8);
      this.listView.setEmptyView(this.emptyView);
    }
  }
  
  public View createView(Context paramContext)
  {
    this.searching = false;
    this.searchWas = false;
    this.actionBar.setBackButtonDrawable(new BackDrawable(false));
    this.actionBar.setTitle("");
    this.actionBar.setAllowOverlayTitle(false);
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        int j;
        if (paramAnonymousInt == -1) {
          if (MediaActivity.this.actionBar.isActionModeShowed())
          {
            paramAnonymousInt = 1;
            while (paramAnonymousInt >= 0)
            {
              MediaActivity.this.selectedFiles[paramAnonymousInt].clear();
              paramAnonymousInt -= 1;
            }
            MediaActivity.access$902(MediaActivity.this, 0);
            MediaActivity.this.actionBar.hideActionMode();
            j = MediaActivity.this.listView.getChildCount();
            paramAnonymousInt = 0;
            if (paramAnonymousInt < j)
            {
              localObject1 = MediaActivity.this.listView.getChildAt(paramAnonymousInt);
              if ((localObject1 instanceof SharedDocumentCell)) {
                ((SharedDocumentCell)localObject1).setChecked(false, true);
              }
              for (;;)
              {
                paramAnonymousInt += 1;
                break;
                if ((localObject1 instanceof SharedPhotoVideoCell))
                {
                  i = 0;
                  while (i < 6)
                  {
                    ((SharedPhotoVideoCell)localObject1).setChecked(i, false, true);
                    i += 1;
                  }
                }
                else if ((localObject1 instanceof SharedLinkCell))
                {
                  ((SharedLinkCell)localObject1).setChecked(false, true);
                }
              }
            }
          }
          else
          {
            MediaActivity.this.finishFragment();
          }
        }
        Object localObject2;
        label525:
        label609:
        label638:
        label640:
        label646:
        label713:
        label726:
        label791:
        label846:
        label853:
        label886:
        label896:
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
                    if (paramAnonymousInt != 1) {
                      break;
                    }
                  } while (MediaActivity.this.selectedMode == 0);
                  MediaActivity.access$102(MediaActivity.this, 0);
                  MediaActivity.this.switchToCurrentSelectedMode();
                  return;
                  if (paramAnonymousInt != 2) {
                    break;
                  }
                } while (MediaActivity.this.selectedMode == 1);
                MediaActivity.access$102(MediaActivity.this, 1);
                MediaActivity.this.switchToCurrentSelectedMode();
                return;
                if (paramAnonymousInt != 5) {
                  break;
                }
              } while (MediaActivity.this.selectedMode == 3);
              MediaActivity.access$102(MediaActivity.this, 3);
              MediaActivity.this.switchToCurrentSelectedMode();
              return;
              if (paramAnonymousInt != 6) {
                break;
              }
            } while (MediaActivity.this.selectedMode == 4);
            MediaActivity.access$102(MediaActivity.this, 4);
            MediaActivity.this.switchToCurrentSelectedMode();
            return;
            if (paramAnonymousInt != 4) {
              break;
            }
          } while (MediaActivity.this.getParentActivity() == null);
          AlertDialog.Builder localBuilder = new AlertDialog.Builder(MediaActivity.this.getParentActivity());
          localBuilder.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", 2131493005, new Object[] { LocaleController.formatPluralString("items", MediaActivity.this.selectedFiles[0].size() + MediaActivity.this.selectedFiles[1].size()) }));
          localBuilder.setTitle(LocaleController.getString("AppName", 2131492981));
          final boolean[] arrayOfBoolean = new boolean[1];
          paramAnonymousInt = (int)MediaActivity.this.dialog_id;
          Object localObject3;
          CheckBoxCell localCheckBoxCell;
          if (paramAnonymousInt != 0)
          {
            int m;
            int k;
            if (paramAnonymousInt > 0)
            {
              localObject2 = MessagesController.getInstance(MediaActivity.this.currentAccount).getUser(Integer.valueOf(paramAnonymousInt));
              localObject1 = null;
              if ((localObject2 == null) && (ChatObject.isChannel((TLRPC.Chat)localObject1))) {
                break label791;
              }
              m = ConnectionsManager.getInstance(MediaActivity.this.currentAccount).getCurrentTime();
              if (((localObject2 == null) || (((TLRPC.User)localObject2).id == UserConfig.getInstance(MediaActivity.this.currentAccount).getClientUserId())) && (localObject1 == null)) {
                break label791;
              }
              paramAnonymousInt = 0;
              j = 1;
              i = paramAnonymousInt;
              if (j < 0) {
                break label646;
              }
              k = 0;
              i = paramAnonymousInt;
              paramAnonymousInt = i;
              if (k >= MediaActivity.this.selectedFiles[j].size()) {
                break label640;
              }
              localObject3 = (MessageObject)MediaActivity.this.selectedFiles[j].valueAt(k);
              if (((MessageObject)localObject3).messageOwner.action == null) {
                break label609;
              }
            }
            for (;;)
            {
              k += 1;
              break label525;
              localObject2 = null;
              localObject1 = MessagesController.getInstance(MediaActivity.this.currentAccount).getChat(Integer.valueOf(-paramAnonymousInt));
              break;
              if (!((MessageObject)localObject3).isOut()) {
                break label638;
              }
              if (m - ((MessageObject)localObject3).messageOwner.date <= 172800) {
                i = 1;
              }
            }
            paramAnonymousInt = 0;
            if (paramAnonymousInt == 0) {
              break label846;
            }
            i = paramAnonymousInt;
            if (i != 0)
            {
              localObject3 = new FrameLayout(MediaActivity.this.getParentActivity());
              localCheckBoxCell = new CheckBoxCell(MediaActivity.this.getParentActivity(), 1);
              localCheckBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
              if (localObject1 == null) {
                break label853;
              }
              localCheckBoxCell.setText(LocaleController.getString("DeleteForAll", 2131493367), "", false, false);
              if (!LocaleController.isRTL) {
                break label886;
              }
              paramAnonymousInt = AndroidUtilities.dp(16.0F);
              if (!LocaleController.isRTL) {
                break label896;
              }
            }
          }
          for (i = AndroidUtilities.dp(8.0F);; i = AndroidUtilities.dp(16.0F))
          {
            localCheckBoxCell.setPadding(paramAnonymousInt, 0, i, 0);
            ((FrameLayout)localObject3).addView(localCheckBoxCell, LayoutHelper.createFrame(-1, 48.0F, 51, 0.0F, 0.0F, 0.0F, 0.0F));
            localCheckBoxCell.setOnClickListener(new View.OnClickListener()
            {
              public void onClick(View paramAnonymous2View)
              {
                paramAnonymous2View = (CheckBoxCell)paramAnonymous2View;
                boolean[] arrayOfBoolean = arrayOfBoolean;
                if (arrayOfBoolean[0] == 0) {}
                for (int i = 1;; i = 0)
                {
                  arrayOfBoolean[0] = i;
                  paramAnonymous2View.setChecked(arrayOfBoolean[0], true);
                  return;
                }
              }
            });
            localBuilder.setView((View)localObject3);
            localBuilder.setPositiveButton(LocaleController.getString("OK", 2131494028), new DialogInterface.OnClickListener()
            {
              public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
              {
                paramAnonymous2Int = 1;
                while (paramAnonymous2Int >= 0)
                {
                  ArrayList localArrayList = new ArrayList();
                  int i = 0;
                  while (i < MediaActivity.this.selectedFiles[paramAnonymous2Int].size())
                  {
                    localArrayList.add(Integer.valueOf(MediaActivity.this.selectedFiles[paramAnonymous2Int].keyAt(i)));
                    i += 1;
                  }
                  Object localObject1 = null;
                  paramAnonymous2DialogInterface = null;
                  int j = 0;
                  i = j;
                  Object localObject2;
                  if (!localArrayList.isEmpty())
                  {
                    localObject2 = (MessageObject)MediaActivity.this.selectedFiles[paramAnonymous2Int].get(((Integer)localArrayList.get(0)).intValue());
                    i = j;
                    if (0 == 0)
                    {
                      i = j;
                      if (((MessageObject)localObject2).messageOwner.to_id.channel_id != 0) {
                        i = ((MessageObject)localObject2).messageOwner.to_id.channel_id;
                      }
                    }
                  }
                  if ((int)MediaActivity.this.dialog_id == 0) {
                    paramAnonymous2DialogInterface = MessagesController.getInstance(MediaActivity.this.currentAccount).getEncryptedChat(Integer.valueOf((int)(MediaActivity.this.dialog_id >> 32)));
                  }
                  if (paramAnonymous2DialogInterface != null)
                  {
                    localObject2 = new ArrayList();
                    j = 0;
                    for (;;)
                    {
                      localObject1 = localObject2;
                      if (j >= MediaActivity.this.selectedFiles[paramAnonymous2Int].size()) {
                        break;
                      }
                      localObject1 = (MessageObject)MediaActivity.this.selectedFiles[paramAnonymous2Int].valueAt(j);
                      if ((((MessageObject)localObject1).messageOwner.random_id != 0L) && (((MessageObject)localObject1).type != 10)) {
                        ((ArrayList)localObject2).add(Long.valueOf(((MessageObject)localObject1).messageOwner.random_id));
                      }
                      j += 1;
                    }
                  }
                  MessagesController.getInstance(MediaActivity.this.currentAccount).deleteMessages(localArrayList, (ArrayList)localObject1, paramAnonymous2DialogInterface, i, arrayOfBoolean[0]);
                  MediaActivity.this.selectedFiles[paramAnonymous2Int].clear();
                  paramAnonymous2Int -= 1;
                }
                MediaActivity.this.actionBar.hideActionMode();
                MediaActivity.this.actionBar.closeSearchField();
                MediaActivity.access$902(MediaActivity.this, 0);
              }
            });
            localBuilder.setNegativeButton(LocaleController.getString("Cancel", 2131493127), null);
            MediaActivity.this.showDialog(localBuilder.create());
            return;
            j -= 1;
            break;
            localCheckBoxCell.setText(LocaleController.formatString("DeleteForUser", 2131493368, new Object[] { UserObject.getFirstName((TLRPC.User)localObject2) }), "", false, false);
            break label713;
            paramAnonymousInt = AndroidUtilities.dp(8.0F);
            break label726;
          }
          if (paramAnonymousInt == 3)
          {
            localObject1 = new Bundle();
            ((Bundle)localObject1).putBoolean("onlySelect", true);
            ((Bundle)localObject1).putInt("dialogsType", 3);
            localObject1 = new DialogsActivity((Bundle)localObject1);
            ((DialogsActivity)localObject1).setDelegate(new DialogsActivity.DialogsActivityDelegate()
            {
              public void didSelectDialogs(DialogsActivity paramAnonymous2DialogsActivity, ArrayList<Long> paramAnonymous2ArrayList, CharSequence paramAnonymous2CharSequence, boolean paramAnonymous2Boolean)
              {
                ArrayList localArrayList = new ArrayList();
                int i = 1;
                int j;
                while (i >= 0)
                {
                  Object localObject = new ArrayList();
                  j = 0;
                  while (j < MediaActivity.this.selectedFiles[i].size())
                  {
                    ((ArrayList)localObject).add(Integer.valueOf(MediaActivity.this.selectedFiles[i].keyAt(j)));
                    j += 1;
                  }
                  Collections.sort((List)localObject);
                  localObject = ((ArrayList)localObject).iterator();
                  while (((Iterator)localObject).hasNext())
                  {
                    Integer localInteger = (Integer)((Iterator)localObject).next();
                    if (localInteger.intValue() > 0) {
                      localArrayList.add(MediaActivity.this.selectedFiles[i].get(localInteger.intValue()));
                    }
                  }
                  MediaActivity.this.selectedFiles[i].clear();
                  i -= 1;
                }
                MediaActivity.access$902(MediaActivity.this, 0);
                MediaActivity.this.actionBar.hideActionMode();
                long l;
                if ((paramAnonymous2ArrayList.size() > 1) || (((Long)paramAnonymous2ArrayList.get(0)).longValue() == UserConfig.getInstance(MediaActivity.this.currentAccount).getClientUserId()) || (paramAnonymous2CharSequence != null))
                {
                  i = 0;
                  while (i < paramAnonymous2ArrayList.size())
                  {
                    l = ((Long)paramAnonymous2ArrayList.get(i)).longValue();
                    if (paramAnonymous2CharSequence != null) {
                      SendMessagesHelper.getInstance(MediaActivity.this.currentAccount).sendMessage(paramAnonymous2CharSequence.toString(), l, null, null, true, null, null, null);
                    }
                    SendMessagesHelper.getInstance(MediaActivity.this.currentAccount).sendMessage(localArrayList, l);
                    i += 1;
                  }
                  paramAnonymous2DialogsActivity.finishFragment();
                }
                label530:
                for (;;)
                {
                  return;
                  l = ((Long)paramAnonymous2ArrayList.get(0)).longValue();
                  i = (int)l;
                  j = (int)(l >> 32);
                  paramAnonymous2ArrayList = new Bundle();
                  paramAnonymous2ArrayList.putBoolean("scrollToTopOnResume", true);
                  if (i != 0) {
                    if (i > 0) {
                      paramAnonymous2ArrayList.putInt("user_id", i);
                    }
                  }
                  for (;;)
                  {
                    if ((i != 0) && (!MessagesController.getInstance(MediaActivity.this.currentAccount).checkCanOpenChat(paramAnonymous2ArrayList, paramAnonymous2DialogsActivity))) {
                      break label530;
                    }
                    NotificationCenter.getInstance(MediaActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
                    paramAnonymous2DialogsActivity = new ChatActivity(paramAnonymous2ArrayList);
                    MediaActivity.this.presentFragment(paramAnonymous2DialogsActivity, true);
                    paramAnonymous2DialogsActivity.showReplyPanel(true, null, localArrayList, null, false);
                    if (AndroidUtilities.isTablet()) {
                      break;
                    }
                    MediaActivity.this.removeSelfFromStack();
                    return;
                    if (i < 0)
                    {
                      paramAnonymous2ArrayList.putInt("chat_id", -i);
                      continue;
                      paramAnonymous2ArrayList.putInt("enc_id", j);
                    }
                  }
                }
              }
            });
            MediaActivity.this.presentFragment((BaseFragment)localObject1);
            return;
          }
        } while ((paramAnonymousInt != 7) || (MediaActivity.this.selectedFiles[0].size() != 1));
        Object localObject1 = new Bundle();
        int i = (int)MediaActivity.this.dialog_id;
        paramAnonymousInt = (int)(MediaActivity.this.dialog_id >> 32);
        if (i != 0) {
          if (paramAnonymousInt == 1) {
            ((Bundle)localObject1).putInt("chat_id", i);
          }
        }
        for (;;)
        {
          ((Bundle)localObject1).putInt("message_id", MediaActivity.this.selectedFiles[0].keyAt(0));
          NotificationCenter.getInstance(MediaActivity.this.currentAccount).postNotificationName(NotificationCenter.closeChats, new Object[0]);
          MediaActivity.this.presentFragment(new ChatActivity((Bundle)localObject1), true);
          return;
          if (i > 0)
          {
            ((Bundle)localObject1).putInt("user_id", i);
          }
          else if (i < 0)
          {
            localObject2 = MessagesController.getInstance(MediaActivity.this.currentAccount).getChat(Integer.valueOf(-i));
            paramAnonymousInt = i;
            if (localObject2 != null)
            {
              paramAnonymousInt = i;
              if (((TLRPC.Chat)localObject2).migrated_to != null)
              {
                ((Bundle)localObject1).putInt("migrated_to", i);
                paramAnonymousInt = -((TLRPC.Chat)localObject2).migrated_to.channel_id;
              }
            }
            ((Bundle)localObject1).putInt("chat_id", -paramAnonymousInt);
            continue;
            ((Bundle)localObject1).putInt("enc_id", paramAnonymousInt);
          }
        }
      }
    });
    int i = 1;
    while (i >= 0)
    {
      this.selectedFiles[i].clear();
      i -= 1;
    }
    this.cantDeleteMessagesCount = 0;
    this.actionModeViews.clear();
    Object localObject1 = this.actionBar.createMenu();
    this.searchItem = ((ActionBarMenu)localObject1).addItem(0, 2131165356).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()
    {
      public void onSearchCollapse()
      {
        MediaActivity.this.dropDownContainer.setVisibility(0);
        if (MediaActivity.this.selectedMode == 1) {
          MediaActivity.this.documentsSearchAdapter.search(null);
        }
        for (;;)
        {
          MediaActivity.access$2902(MediaActivity.this, false);
          MediaActivity.access$3302(MediaActivity.this, false);
          MediaActivity.this.switchToCurrentSelectedMode();
          return;
          if (MediaActivity.this.selectedMode == 3) {
            MediaActivity.this.linksSearchAdapter.search(null);
          } else if (MediaActivity.this.selectedMode == 4) {
            MediaActivity.this.audioSearchAdapter.search(null);
          }
        }
      }
      
      public void onSearchExpand()
      {
        MediaActivity.this.dropDownContainer.setVisibility(8);
        MediaActivity.access$2902(MediaActivity.this, true);
      }
      
      public void onTextChanged(EditText paramAnonymousEditText)
      {
        paramAnonymousEditText = paramAnonymousEditText.getText().toString();
        if (paramAnonymousEditText.length() != 0)
        {
          MediaActivity.access$3302(MediaActivity.this, true);
          MediaActivity.this.switchToCurrentSelectedMode();
        }
        if (MediaActivity.this.selectedMode == 1) {
          if (MediaActivity.this.documentsSearchAdapter != null) {}
        }
        do
        {
          do
          {
            return;
            MediaActivity.this.documentsSearchAdapter.search(paramAnonymousEditText);
            return;
            if (MediaActivity.this.selectedMode != 3) {
              break;
            }
          } while (MediaActivity.this.linksSearchAdapter == null);
          MediaActivity.this.linksSearchAdapter.search(paramAnonymousEditText);
          return;
        } while ((MediaActivity.this.selectedMode != 4) || (MediaActivity.this.audioSearchAdapter == null));
        MediaActivity.this.audioSearchAdapter.search(paramAnonymousEditText);
      }
    });
    this.searchItem.getSearchField().setHint(LocaleController.getString("Search", 2131494298));
    this.searchItem.setVisibility(8);
    this.dropDownContainer = new ActionBarMenuItem(paramContext, (ActionBarMenu)localObject1, 0, 0);
    this.dropDownContainer.setSubMenuOpenSide(1);
    this.dropDownContainer.addSubItem(1, LocaleController.getString("SharedMediaTitle", 2131494396));
    this.dropDownContainer.addSubItem(2, LocaleController.getString("DocumentsTitle", 2131493394));
    Object localObject2;
    float f;
    label284:
    Object localObject3;
    int k;
    int j;
    if ((int)this.dialog_id != 0)
    {
      this.dropDownContainer.addSubItem(5, LocaleController.getString("LinksTitle", 2131493758));
      this.dropDownContainer.addSubItem(6, LocaleController.getString("AudioTitle", 2131493046));
      localObject1 = this.actionBar;
      localObject2 = this.dropDownContainer;
      if (!AndroidUtilities.isTablet()) {
        break label1106;
      }
      f = 64.0F;
      ((ActionBar)localObject1).addView((View)localObject2, 0, LayoutHelper.createFrame(-2, -1.0F, 51, f, 0.0F, 40.0F, 0.0F));
      this.dropDownContainer.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          MediaActivity.this.dropDownContainer.toggleSubMenu();
        }
      });
      this.dropDown = new TextView(paramContext);
      this.dropDown.setGravity(3);
      this.dropDown.setSingleLine(true);
      this.dropDown.setLines(1);
      this.dropDown.setMaxLines(1);
      this.dropDown.setEllipsize(TextUtils.TruncateAt.END);
      this.dropDown.setTextColor(Theme.getColor("actionBarDefaultTitle"));
      this.dropDown.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.dropDownDrawable = paramContext.getResources().getDrawable(2131165364).mutate();
      this.dropDownDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor("actionBarDefaultTitle"), PorterDuff.Mode.MULTIPLY));
      this.dropDown.setCompoundDrawablesWithIntrinsicBounds(null, null, this.dropDownDrawable, null);
      this.dropDown.setCompoundDrawablePadding(AndroidUtilities.dp(4.0F));
      this.dropDown.setPadding(0, 0, AndroidUtilities.dp(10.0F), 0);
      this.dropDownContainer.addView(this.dropDown, LayoutHelper.createFrame(-2, -2.0F, 16, 16.0F, 0.0F, 0.0F, 0.0F));
      localObject1 = this.actionBar.createActionMode();
      this.selectedMessagesCountTextView = new NumberTextView(((ActionBarMenu)localObject1).getContext());
      this.selectedMessagesCountTextView.setTextSize(18);
      this.selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
      this.selectedMessagesCountTextView.setTextColor(Theme.getColor("actionBarActionModeDefaultIcon"));
      this.selectedMessagesCountTextView.setOnTouchListener(new View.OnTouchListener()
      {
        public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
        {
          return true;
        }
      });
      ((ActionBarMenu)localObject1).addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0F, 65, 0, 0, 0));
      if ((int)this.dialog_id != 0)
      {
        localObject2 = this.actionModeViews;
        localObject3 = ((ActionBarMenu)localObject1).addItemWithWidth(7, 2131165326, AndroidUtilities.dp(54.0F));
        this.gotoItem = ((ActionBarMenuItem)localObject3);
        ((ArrayList)localObject2).add(localObject3);
        this.actionModeViews.add(((ActionBarMenu)localObject1).addItemWithWidth(3, 2131165351, AndroidUtilities.dp(54.0F)));
      }
      this.actionModeViews.add(((ActionBarMenu)localObject1).addItemWithWidth(4, 2131165348, AndroidUtilities.dp(54.0F)));
      this.photoVideoAdapter = new SharedPhotoVideoAdapter(paramContext);
      this.documentsAdapter = new SharedDocumentsAdapter(paramContext, 1);
      this.audioAdapter = new SharedDocumentsAdapter(paramContext, 4);
      this.documentsSearchAdapter = new MediaSearchAdapter(paramContext, 1);
      this.audioSearchAdapter = new MediaSearchAdapter(paramContext, 4);
      this.linksSearchAdapter = new MediaSearchAdapter(paramContext, 3);
      this.linksAdapter = new SharedLinksAdapter(paramContext);
      localObject1 = new FrameLayout(paramContext);
      this.fragmentView = ((View)localObject1);
      i = -1;
      k = 0;
      j = k;
      if (this.layoutManager != null)
      {
        i = this.layoutManager.findFirstVisibleItemPosition();
        if (i == this.layoutManager.getItemCount() - 1) {
          break label1122;
        }
        localObject2 = (RecyclerListView.Holder)this.listView.findViewHolderForAdapterPosition(i);
        if (localObject2 == null) {
          break label1113;
        }
        j = ((RecyclerListView.Holder)localObject2).itemView.getTop();
      }
    }
    for (;;)
    {
      this.listView = new RecyclerListView(paramContext);
      this.listView.setClipToPadding(false);
      this.listView.setSectionsType(2);
      localObject2 = this.listView;
      localObject3 = new LinearLayoutManager(paramContext, 1, false);
      this.layoutManager = ((LinearLayoutManager)localObject3);
      ((RecyclerListView)localObject2).setLayoutManager((RecyclerView.LayoutManager)localObject3);
      ((FrameLayout)localObject1).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          if (((MediaActivity.this.selectedMode == 1) || (MediaActivity.this.selectedMode == 4)) && ((paramAnonymousView instanceof SharedDocumentCell))) {
            MediaActivity.this.onItemClick(paramAnonymousInt, paramAnonymousView, ((SharedDocumentCell)paramAnonymousView).getMessage(), 0);
          }
          while ((MediaActivity.this.selectedMode != 3) || (!(paramAnonymousView instanceof SharedLinkCell))) {
            return;
          }
          MediaActivity.this.onItemClick(paramAnonymousInt, paramAnonymousView, ((SharedLinkCell)paramAnonymousView).getMessage(), 0);
        }
      });
      this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
      {
        public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
        {
          boolean bool = true;
          if ((paramAnonymousInt == 1) && (MediaActivity.this.searching) && (MediaActivity.this.searchWas)) {
            AndroidUtilities.hideKeyboard(MediaActivity.this.getParentActivity().getCurrentFocus());
          }
          paramAnonymousRecyclerView = MediaActivity.this;
          if (paramAnonymousInt != 0) {}
          for (;;)
          {
            MediaActivity.access$3502(paramAnonymousRecyclerView, bool);
            return;
            bool = false;
          }
        }
        
        public void onScrolled(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt1, int paramAnonymousInt2)
        {
          if ((MediaActivity.this.searching) && (MediaActivity.this.searchWas)) {}
          label264:
          do
          {
            int i;
            do
            {
              return;
              paramAnonymousInt2 = MediaActivity.this.layoutManager.findFirstVisibleItemPosition();
              if (paramAnonymousInt2 != -1) {
                break;
              }
              paramAnonymousInt1 = 0;
              i = paramAnonymousRecyclerView.getAdapter().getItemCount();
            } while ((paramAnonymousInt1 == 0) || (paramAnonymousInt2 + paramAnonymousInt1 <= i - 2) || (MediaActivity.SharedMediaData.access$600(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode])));
            if (MediaActivity.this.selectedMode == 0) {
              paramAnonymousInt1 = 0;
            }
            for (;;)
            {
              if (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode])[0] != 0) {
                break label264;
              }
              MediaActivity.SharedMediaData.access$602(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode], true);
              DataQuery.getInstance(MediaActivity.this.currentAccount).loadMedia(MediaActivity.this.dialog_id, 50, MediaActivity.SharedMediaData.access$400(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode])[0], paramAnonymousInt1, true, MediaActivity.this.classGuid);
              return;
              paramAnonymousInt1 = Math.abs(MediaActivity.this.layoutManager.findLastVisibleItemPosition() - paramAnonymousInt2) + 1;
              break;
              if (MediaActivity.this.selectedMode == 1) {
                paramAnonymousInt1 = 1;
              } else if (MediaActivity.this.selectedMode == 2) {
                paramAnonymousInt1 = 2;
              } else if (MediaActivity.this.selectedMode == 4) {
                paramAnonymousInt1 = 4;
              } else {
                paramAnonymousInt1 = 3;
              }
            }
          } while ((MediaActivity.this.mergeDialogId == 0L) || (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode])[1] != 0));
          MediaActivity.SharedMediaData.access$602(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode], true);
          DataQuery.getInstance(MediaActivity.this.currentAccount).loadMedia(MediaActivity.this.mergeDialogId, 50, MediaActivity.SharedMediaData.access$400(MediaActivity.this.sharedMediaData[MediaActivity.this.selectedMode])[1], paramAnonymousInt1, true, MediaActivity.this.classGuid);
        }
      });
      this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
      {
        public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          boolean bool2 = false;
          boolean bool1;
          if (((MediaActivity.this.selectedMode == 1) || (MediaActivity.this.selectedMode == 4)) && ((paramAnonymousView instanceof SharedDocumentCell)))
          {
            localMessageObject = ((SharedDocumentCell)paramAnonymousView).getMessage();
            bool1 = MediaActivity.this.onItemLongClick(localMessageObject, paramAnonymousView, 0);
          }
          do
          {
            do
            {
              return bool1;
              bool1 = bool2;
            } while (MediaActivity.this.selectedMode != 3);
            bool1 = bool2;
          } while (!(paramAnonymousView instanceof SharedLinkCell));
          MessageObject localMessageObject = ((SharedLinkCell)paramAnonymousView).getMessage();
          return MediaActivity.this.onItemLongClick(localMessageObject, paramAnonymousView, 0);
        }
      });
      if (i != -1) {
        this.layoutManager.scrollToPositionWithOffset(i, j);
      }
      i = 0;
      while (i < 6)
      {
        this.cellCache.add(new SharedPhotoVideoCell(paramContext));
        i += 1;
      }
      localObject1 = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf((int)(this.dialog_id >> 32)));
      if ((localObject1 == null) || (AndroidUtilities.getPeerLayerVersion(((TLRPC.EncryptedChat)localObject1).layer) < 46)) {
        break;
      }
      this.dropDownContainer.addSubItem(6, LocaleController.getString("AudioTitle", 2131493046));
      break;
      label1106:
      f = 56.0F;
      break label284;
      label1113:
      i = -1;
      j = k;
      continue;
      label1122:
      i = -1;
      j = k;
    }
    this.emptyView = new LinearLayout(paramContext);
    this.emptyView.setOrientation(1);
    this.emptyView.setGravity(17);
    this.emptyView.setVisibility(8);
    this.emptyView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    ((FrameLayout)localObject1).addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.emptyView.setOnTouchListener(new View.OnTouchListener()
    {
      public boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent)
      {
        return true;
      }
    });
    this.emptyImageView = new ImageView(paramContext);
    this.emptyView.addView(this.emptyImageView, LayoutHelper.createLinear(-2, -2));
    this.emptyTextView = new TextView(paramContext);
    this.emptyTextView.setTextColor(Theme.getColor("windowBackgroundWhiteGrayText2"));
    this.emptyTextView.setGravity(17);
    this.emptyTextView.setTextSize(1, 17.0F);
    this.emptyTextView.setPadding(AndroidUtilities.dp(40.0F), 0, AndroidUtilities.dp(40.0F), AndroidUtilities.dp(128.0F));
    this.emptyView.addView(this.emptyTextView, LayoutHelper.createLinear(-2, -2, 17, 0, 24, 0, 0));
    this.progressView = new LinearLayout(paramContext);
    this.progressView.setGravity(17);
    this.progressView.setOrientation(1);
    this.progressView.setVisibility(8);
    this.progressView.setBackgroundColor(Theme.getColor("windowBackgroundGray"));
    ((FrameLayout)localObject1).addView(this.progressView, LayoutHelper.createFrame(-1, -1.0F));
    this.progressBar = new RadialProgressView(paramContext);
    this.progressView.addView(this.progressBar, LayoutHelper.createLinear(-2, -2));
    switchToCurrentSelectedMode();
    if (!AndroidUtilities.isTablet())
    {
      paramContext = new FragmentContextView(paramContext, this, false);
      this.fragmentContextView = paramContext;
      ((FrameLayout)localObject1).addView(paramContext, LayoutHelper.createFrame(-1, 39.0F, 51, 0.0F, -36.0F, 0.0F, 0.0F));
    }
    return this.fragmentView;
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    int i;
    Object localObject1;
    boolean bool;
    Object localObject2;
    if (paramInt1 == NotificationCenter.mediaDidLoaded)
    {
      long l = ((Long)paramVarArgs[0]).longValue();
      if (((Integer)paramVarArgs[3]).intValue() == this.classGuid)
      {
        i = ((Integer)paramVarArgs[4]).intValue();
        SharedMediaData.access$602(this.sharedMediaData[i], false);
        SharedMediaData.access$4402(this.sharedMediaData[i], ((Integer)paramVarArgs[1]).intValue());
        localObject1 = (ArrayList)paramVarArgs[2];
        if ((int)this.dialog_id == 0)
        {
          bool = true;
          if (l != this.dialog_id) {
            break label159;
          }
        }
        label159:
        for (paramInt1 = 0;; paramInt1 = 1)
        {
          paramInt2 = 0;
          while (paramInt2 < ((ArrayList)localObject1).size())
          {
            localObject2 = (MessageObject)((ArrayList)localObject1).get(paramInt2);
            this.sharedMediaData[i].addMessage((MessageObject)localObject2, false, bool);
            paramInt2 += 1;
          }
          bool = false;
          break;
        }
        this.sharedMediaData[i].endReached[paramInt1] = ((Boolean)paramVarArgs[5]).booleanValue();
        if ((paramInt1 == 0) && (this.sharedMediaData[i].endReached[paramInt1] != 0) && (this.mergeDialogId != 0L))
        {
          SharedMediaData.access$602(this.sharedMediaData[i], true);
          DataQuery.getInstance(this.currentAccount).loadMedia(this.mergeDialogId, 50, this.sharedMediaData[i].max_id[1], i, true, this.classGuid);
        }
        if (!this.sharedMediaData[i].loading)
        {
          if (this.progressView != null) {
            this.progressView.setVisibility(8);
          }
          if ((this.selectedMode == i) && (this.listView != null) && (this.listView.getEmptyView() == null)) {
            this.listView.setEmptyView(this.emptyView);
          }
        }
        this.scrolling = true;
        if ((this.selectedMode != 0) || (i != 0)) {
          break label417;
        }
        if (this.photoVideoAdapter != null) {
          this.photoVideoAdapter.notifyDataSetChanged();
        }
        if ((this.selectedMode == 1) || (this.selectedMode == 3) || (this.selectedMode == 4))
        {
          paramVarArgs = this.searchItem;
          if ((this.sharedMediaData[i].messages.isEmpty()) || (this.searching)) {
            break label510;
          }
          paramInt1 = 0;
          label411:
          paramVarArgs.setVisibility(paramInt1);
        }
      }
    }
    for (;;)
    {
      return;
      label417:
      if ((this.selectedMode == 1) && (i == 1))
      {
        if (this.documentsAdapter == null) {
          break;
        }
        this.documentsAdapter.notifyDataSetChanged();
        break;
      }
      if ((this.selectedMode == 3) && (i == 3))
      {
        if (this.linksAdapter == null) {
          break;
        }
        this.linksAdapter.notifyDataSetChanged();
        break;
      }
      if ((this.selectedMode != 4) || (i != 4) || (this.audioAdapter == null)) {
        break;
      }
      this.audioAdapter.notifyDataSetChanged();
      break;
      label510:
      paramInt1 = 8;
      break label411;
      int j;
      if (paramInt1 == NotificationCenter.messagesDeleted)
      {
        localObject1 = null;
        if ((int)this.dialog_id < 0) {
          localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-(int)this.dialog_id));
        }
        paramInt2 = ((Integer)paramVarArgs[1]).intValue();
        paramInt1 = 0;
        if (ChatObject.isChannel((TLRPC.Chat)localObject1)) {
          if ((paramInt2 == 0) && (this.mergeDialogId != 0L)) {
            paramInt1 = 1;
          }
        }
        while (paramInt2 == 0) {
          for (;;)
          {
            paramVarArgs = (ArrayList)paramVarArgs[0];
            i = 0;
            paramVarArgs = paramVarArgs.iterator();
            if (!paramVarArgs.hasNext()) {
              break label697;
            }
            localObject1 = (Integer)paramVarArgs.next();
            localObject2 = this.sharedMediaData;
            int k = localObject2.length;
            paramInt2 = 0;
            j = i;
            for (;;)
            {
              i = j;
              if (paramInt2 >= k) {
                break;
              }
              if (localObject2[paramInt2].deleteMessage(((Integer)localObject1).intValue(), paramInt1)) {
                j = 1;
              }
              paramInt2 += 1;
            }
            if (paramInt2 != ((TLRPC.Chat)localObject1).id) {
              break;
            }
            paramInt1 = 0;
          }
        }
        return;
        label697:
        if (i != 0)
        {
          this.scrolling = true;
          if (this.photoVideoAdapter != null) {
            this.photoVideoAdapter.notifyDataSetChanged();
          }
          if (this.documentsAdapter != null) {
            this.documentsAdapter.notifyDataSetChanged();
          }
          if (this.linksAdapter != null) {
            this.linksAdapter.notifyDataSetChanged();
          }
          if (this.audioAdapter != null) {
            this.audioAdapter.notifyDataSetChanged();
          }
          if ((this.selectedMode == 1) || (this.selectedMode == 3) || (this.selectedMode == 4))
          {
            paramVarArgs = this.searchItem;
            if ((!this.sharedMediaData[this.selectedMode].messages.isEmpty()) && (!this.searching)) {}
            for (paramInt1 = 0;; paramInt1 = 8)
            {
              paramVarArgs.setVisibility(paramInt1);
              return;
            }
          }
        }
      }
      else if (paramInt1 == NotificationCenter.didReceivedNewMessages)
      {
        if (((Long)paramVarArgs[0]).longValue() == this.dialog_id)
        {
          paramVarArgs = (ArrayList)paramVarArgs[1];
          if ((int)this.dialog_id == 0)
          {
            bool = true;
            label873:
            paramInt2 = 0;
            paramInt1 = 0;
            label877:
            if (paramInt1 >= paramVarArgs.size()) {
              break label979;
            }
            localObject1 = (MessageObject)paramVarArgs.get(paramInt1);
            i = paramInt2;
            if (((MessageObject)localObject1).messageOwner.media != null)
            {
              if (!((MessageObject)localObject1).needDrawBluredPreview()) {
                break label936;
              }
              i = paramInt2;
            }
          }
          for (;;)
          {
            paramInt1 += 1;
            paramInt2 = i;
            break label877;
            bool = false;
            break label873;
            label936:
            j = DataQuery.getMediaType(((MessageObject)localObject1).messageOwner);
            if (j == -1) {
              break;
            }
            i = paramInt2;
            if (this.sharedMediaData[j].addMessage((MessageObject)localObject1, true, bool)) {
              i = 1;
            }
          }
          label979:
          if (paramInt2 != 0)
          {
            this.scrolling = true;
            if (this.photoVideoAdapter != null) {
              this.photoVideoAdapter.notifyDataSetChanged();
            }
            if (this.documentsAdapter != null) {
              this.documentsAdapter.notifyDataSetChanged();
            }
            if (this.linksAdapter != null) {
              this.linksAdapter.notifyDataSetChanged();
            }
            if (this.audioAdapter != null) {
              this.audioAdapter.notifyDataSetChanged();
            }
            if ((this.selectedMode == 1) || (this.selectedMode == 3) || (this.selectedMode == 4))
            {
              paramVarArgs = this.searchItem;
              if ((!this.sharedMediaData[this.selectedMode].messages.isEmpty()) && (!this.searching)) {}
              for (paramInt1 = 0;; paramInt1 = 8)
              {
                paramVarArgs.setVisibility(paramInt1);
                return;
              }
            }
          }
        }
      }
      else if (paramInt1 == NotificationCenter.messageReceivedByServer)
      {
        localObject1 = (Integer)paramVarArgs[0];
        paramVarArgs = (Integer)paramVarArgs[1];
        localObject2 = this.sharedMediaData;
        paramInt2 = localObject2.length;
        paramInt1 = 0;
        while (paramInt1 < paramInt2)
        {
          localObject2[paramInt1].replaceMid(((Integer)localObject1).intValue(), paramVarArgs.intValue());
          paramInt1 += 1;
        }
      }
    }
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription.ThemeDescriptionDelegate local11 = new ThemeDescription.ThemeDescriptionDelegate()
    {
      public void didSetColor()
      {
        if (MediaActivity.this.listView != null)
        {
          int j = MediaActivity.this.listView.getChildCount();
          int i = 0;
          while (i < j)
          {
            View localView = MediaActivity.this.listView.getChildAt(i);
            if ((localView instanceof SharedPhotoVideoCell)) {
              ((SharedPhotoVideoCell)localView).updateCheckboxColor();
            }
            i += 1;
          }
        }
      }
    };
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.progressView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundGray");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription8 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription9 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, "actionBarDefaultSubmenuBackground");
    ThemeDescription localThemeDescription10 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, "actionBarDefaultSubmenuItem");
    ThemeDescription localThemeDescription11 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    ThemeDescription localThemeDescription12 = new ThemeDescription(this.dropDown, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription13 = new ThemeDescription(this.dropDown, 0, null, null, new Drawable[] { this.dropDownDrawable }, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription14 = new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder");
    ThemeDescription localThemeDescription15 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon");
    ThemeDescription localThemeDescription16 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, null, null, null, null, "actionBarActionModeDefault");
    ThemeDescription localThemeDescription17 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, null, null, null, null, "actionBarActionModeDefaultTop");
    ThemeDescription localThemeDescription18 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, null, null, null, null, "actionBarActionModeDefaultSelector");
    ThemeDescription localThemeDescription19 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, "actionBarDefaultSearch");
    ThemeDescription localThemeDescription20 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, "actionBarDefaultSearchPlaceholder");
    ThemeDescription localThemeDescription21 = new ThemeDescription(this.selectedMessagesCountTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon");
    ThemeDescription localThemeDescription22 = new ThemeDescription(this.progressBar, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription23 = new ThemeDescription(this.emptyTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "windowBackgroundWhiteGrayText2");
    ThemeDescription localThemeDescription24 = new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2");
    ThemeDescription localThemeDescription25 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection");
    ThemeDescription localThemeDescription26 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription27 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "dateTextView" }, null, null, null, "windowBackgroundWhiteGrayText3");
    ThemeDescription localThemeDescription28 = new ThemeDescription(this.listView, ThemeDescription.FLAG_PROGRESSBAR, new Class[] { SharedDocumentCell.class }, new String[] { "progressView" }, null, null, null, "sharedMedia_startStopLoadIcon");
    ThemeDescription localThemeDescription29 = new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "statusImageView" }, null, null, null, "sharedMedia_startStopLoadIcon");
    ThemeDescription localThemeDescription30 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { SharedDocumentCell.class }, new String[] { "checkBox" }, null, null, null, "checkbox");
    ThemeDescription localThemeDescription31 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { SharedDocumentCell.class }, new String[] { "checkBox" }, null, null, null, "checkboxCheck");
    ThemeDescription localThemeDescription32 = new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "thumbImageView" }, null, null, null, "files_folderIcon");
    ThemeDescription localThemeDescription33 = new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "extTextView" }, null, null, null, "files_iconText");
    ThemeDescription localThemeDescription34 = new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2");
    ThemeDescription localThemeDescription35 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection");
    ThemeDescription localThemeDescription36 = new ThemeDescription(this.listView, 0, new Class[] { LoadingCell.class }, new String[] { "progressBar" }, null, null, null, "progressCircle");
    ThemeDescription localThemeDescription37 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { SharedLinkCell.class }, new String[] { "checkBox" }, null, null, null, "checkbox");
    ThemeDescription localThemeDescription38 = new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { SharedLinkCell.class }, new String[] { "checkBox" }, null, null, null, "checkboxCheck");
    ThemeDescription localThemeDescription39 = new ThemeDescription(this.listView, 0, new Class[] { SharedLinkCell.class }, new String[] { "titleTextPaint" }, null, null, null, "windowBackgroundWhiteBlackText");
    ThemeDescription localThemeDescription40 = new ThemeDescription(this.listView, 0, new Class[] { SharedLinkCell.class }, null, null, null, "windowBackgroundWhiteLinkText");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.linkSelectionPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, localThemeDescription8, localThemeDescription9, localThemeDescription10, localThemeDescription11, localThemeDescription12, localThemeDescription13, localThemeDescription14, localThemeDescription15, localThemeDescription16, localThemeDescription17, localThemeDescription18, localThemeDescription19, localThemeDescription20, localThemeDescription21, localThemeDescription22, localThemeDescription23, localThemeDescription24, localThemeDescription25, localThemeDescription26, localThemeDescription27, localThemeDescription28, localThemeDescription29, localThemeDescription30, localThemeDescription31, localThemeDescription32, localThemeDescription33, localThemeDescription34, localThemeDescription35, localThemeDescription36, localThemeDescription37, localThemeDescription38, localThemeDescription39, localThemeDescription40, new ThemeDescription(localRecyclerListView, 0, new Class[] { SharedLinkCell.class }, localPaint, null, null, "windowBackgroundWhiteLinkSelection"), new ThemeDescription(this.listView, 0, new Class[] { SharedLinkCell.class }, new String[] { "letterDrawable" }, null, null, null, "sharedMedia_linkPlaceholderText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { SharedLinkCell.class }, new String[] { "letterDrawable" }, null, null, null, "sharedMedia_linkPlaceholder"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { SharedMediaSectionCell.class }, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SECTIONS, new Class[] { SharedMediaSectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { SharedMediaSectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { SharedPhotoVideoCell.class }, null, null, local11, "checkbox"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { SharedPhotoVideoCell.class }, null, null, local11, "checkboxCheck"), new ThemeDescription(this.fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { FragmentContextView.class }, new String[] { "frameLayout" }, null, null, null, "inappPlayerBackground"), new ThemeDescription(this.fragmentContextView, 0, new Class[] { FragmentContextView.class }, new String[] { "playButton" }, null, null, null, "inappPlayerPlayPause"), new ThemeDescription(this.fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { FragmentContextView.class }, new String[] { "titleTextView" }, null, null, null, "inappPlayerTitle"), new ThemeDescription(this.fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { FragmentContextView.class }, new String[] { "frameLayout" }, null, null, null, "inappPlayerPerformer"), new ThemeDescription(this.fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { FragmentContextView.class }, new String[] { "closeButton" }, null, null, null, "inappPlayerClose") };
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if (this.listView != null) {
      this.listView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          MediaActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
          MediaActivity.this.fixLayoutInternal();
          return true;
        }
      });
    }
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.mediaDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagesDeleted);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.didReceivedNewMessages);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messageReceivedByServer);
    this.dialog_id = getArguments().getLong("dialog_id", 0L);
    int i = 0;
    if (i < this.sharedMediaData.length)
    {
      this.sharedMediaData[i] = new SharedMediaData(null);
      int[] arrayOfInt = this.sharedMediaData[i].max_id;
      if ((int)this.dialog_id == 0) {}
      for (int j = Integer.MIN_VALUE;; j = Integer.MAX_VALUE)
      {
        arrayOfInt[0] = j;
        if ((this.mergeDialogId != 0L) && (this.info != null))
        {
          this.sharedMediaData[i].max_id[1] = this.info.migrated_from_max_id;
          this.sharedMediaData[i].endReached[1] = 0;
        }
        i += 1;
        break;
      }
    }
    SharedMediaData.access$602(this.sharedMediaData[0], true);
    DataQuery.getInstance(this.currentAccount).loadMedia(this.dialog_id, 50, 0, 0, true, this.classGuid);
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.mediaDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.didReceivedNewMessages);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagesDeleted);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messageReceivedByServer);
  }
  
  public void onPause()
  {
    super.onPause();
    if (this.dropDownContainer != null) {
      this.dropDownContainer.closeSubMenu();
    }
  }
  
  public void onResume()
  {
    super.onResume();
    this.scrolling = true;
    if (this.photoVideoAdapter != null) {
      this.photoVideoAdapter.notifyDataSetChanged();
    }
    if (this.documentsAdapter != null) {
      this.documentsAdapter.notifyDataSetChanged();
    }
    if (this.linksAdapter != null) {
      this.linksAdapter.notifyDataSetChanged();
    }
    fixLayoutInternal();
  }
  
  public void setChatInfo(TLRPC.ChatFull paramChatFull)
  {
    this.info = paramChatFull;
    if ((this.info != null) && (this.info.migrated_from_chat_id != 0)) {
      this.mergeDialogId = (-this.info.migrated_from_chat_id);
    }
  }
  
  public void setMergeDialogId(long paramLong)
  {
    this.mergeDialogId = paramLong;
  }
  
  public class MediaSearchAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private int currentType;
    protected ArrayList<MessageObject> globalSearch = new ArrayList();
    private int lastReqId;
    private Context mContext;
    private int reqId = 0;
    private ArrayList<MessageObject> searchResult = new ArrayList();
    private Timer searchTimer;
    
    public MediaSearchAdapter(Context paramContext, int paramInt)
    {
      this.mContext = paramContext;
      this.currentType = paramInt;
    }
    
    private void processSearch(final String paramString)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          final Object localObject;
          if (!MediaActivity.SharedMediaData.access$4500(MediaActivity.this.sharedMediaData[MediaActivity.MediaSearchAdapter.this.currentType]).isEmpty())
          {
            if ((MediaActivity.MediaSearchAdapter.this.currentType != 1) && (MediaActivity.MediaSearchAdapter.this.currentType != 4)) {
              break label194;
            }
            localObject = (MessageObject)MediaActivity.SharedMediaData.access$4500(MediaActivity.this.sharedMediaData[MediaActivity.MediaSearchAdapter.this.currentType]).get(MediaActivity.SharedMediaData.access$4500(MediaActivity.this.sharedMediaData[MediaActivity.MediaSearchAdapter.this.currentType]).size() - 1);
            MediaActivity.MediaSearchAdapter.this.queryServerSearch(paramString, ((MessageObject)localObject).getId(), ((MessageObject)localObject).getDialogId());
          }
          for (;;)
          {
            if ((MediaActivity.MediaSearchAdapter.this.currentType == 1) || (MediaActivity.MediaSearchAdapter.this.currentType == 4))
            {
              localObject = new ArrayList();
              ((ArrayList)localObject).addAll(MediaActivity.SharedMediaData.access$4500(MediaActivity.this.sharedMediaData[MediaActivity.MediaSearchAdapter.this.currentType]));
              Utilities.searchQueue.postRunnable(new Runnable()
              {
                public void run()
                {
                  Object localObject3 = MediaActivity.MediaSearchAdapter.3.this.val$query.trim().toLowerCase();
                  if (((String)localObject3).length() == 0)
                  {
                    MediaActivity.MediaSearchAdapter.this.updateSearchResults(new ArrayList());
                    return;
                  }
                  Object localObject2 = LocaleController.getInstance().getTranslitString((String)localObject3);
                  Object localObject1;
                  if (!((String)localObject3).equals(localObject2))
                  {
                    localObject1 = localObject2;
                    if (((String)localObject2).length() != 0) {}
                  }
                  else
                  {
                    localObject1 = null;
                  }
                  int i;
                  label119:
                  MessageObject localMessageObject;
                  int j;
                  label145:
                  CharSequence localCharSequence;
                  if (localObject1 != null)
                  {
                    i = 1;
                    localObject2 = new String[i + 1];
                    localObject2[0] = localObject3;
                    if (localObject1 != null) {
                      localObject2[1] = localObject1;
                    }
                    localObject3 = new ArrayList();
                    i = 0;
                    if (i >= localObject.size()) {
                      break label400;
                    }
                    localMessageObject = (MessageObject)localObject.get(i);
                    j = 0;
                    if (j >= localObject2.length) {
                      break label211;
                    }
                    localCharSequence = localObject2[j];
                    localObject1 = localMessageObject.getDocumentName();
                    if ((localObject1 != null) && (((String)localObject1).length() != 0)) {
                      break label190;
                    }
                  }
                  label190:
                  label211:
                  do
                  {
                    j += 1;
                    break label145;
                    i = 0;
                    break;
                    if (((String)localObject1).toLowerCase().contains(localCharSequence))
                    {
                      ((ArrayList)localObject3).add(localMessageObject);
                      i += 1;
                      break label119;
                    }
                  } while (MediaActivity.MediaSearchAdapter.this.currentType != 4);
                  label256:
                  boolean bool3;
                  boolean bool2;
                  int k;
                  if (localMessageObject.type == 0)
                  {
                    localObject1 = localMessageObject.messageOwner.media.webpage.document;
                    bool3 = false;
                    bool2 = false;
                    k = 0;
                  }
                  for (;;)
                  {
                    boolean bool1 = bool3;
                    if (k < ((TLRPC.Document)localObject1).attributes.size())
                    {
                      TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)((TLRPC.Document)localObject1).attributes.get(k);
                      if (!(localDocumentAttribute instanceof TLRPC.TL_documentAttributeAudio)) {
                        break label393;
                      }
                      if (localDocumentAttribute.performer != null) {
                        bool2 = localDocumentAttribute.performer.toLowerCase().contains(localCharSequence);
                      }
                      bool1 = bool2;
                      if (!bool2)
                      {
                        bool1 = bool2;
                        if (localDocumentAttribute.title != null) {
                          bool1 = localDocumentAttribute.title.toLowerCase().contains(localCharSequence);
                        }
                      }
                    }
                    if (!bool1) {
                      break;
                    }
                    ((ArrayList)localObject3).add(localMessageObject);
                    break label211;
                    localObject1 = localMessageObject.messageOwner.media.document;
                    break label256;
                    label393:
                    k += 1;
                  }
                  label400:
                  MediaActivity.MediaSearchAdapter.this.updateSearchResults((ArrayList)localObject3);
                }
              });
            }
            return;
            label194:
            if (MediaActivity.MediaSearchAdapter.this.currentType == 3) {
              MediaActivity.MediaSearchAdapter.this.queryServerSearch(paramString, 0, MediaActivity.this.dialog_id);
            }
          }
        }
      });
    }
    
    private void updateSearchResults(final ArrayList<MessageObject> paramArrayList)
    {
      AndroidUtilities.runOnUIThread(new Runnable()
      {
        public void run()
        {
          MediaActivity.MediaSearchAdapter.access$6802(MediaActivity.MediaSearchAdapter.this, paramArrayList);
          MediaActivity.MediaSearchAdapter.this.notifyDataSetChanged();
        }
      });
    }
    
    public MessageObject getItem(int paramInt)
    {
      if (paramInt < this.searchResult.size()) {
        return (MessageObject)this.searchResult.get(paramInt);
      }
      return (MessageObject)this.globalSearch.get(paramInt - this.searchResult.size());
    }
    
    public int getItemCount()
    {
      int j = this.searchResult.size();
      int k = this.globalSearch.size();
      int i = j;
      if (k != 0) {
        i = j + k;
      }
      return i;
    }
    
    public int getItemViewType(int paramInt)
    {
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() != this.searchResult.size() + this.globalSearch.size();
    }
    
    public boolean isGlobalSearch(int paramInt)
    {
      int i = this.searchResult.size();
      int j = this.globalSearch.size();
      if ((paramInt >= 0) && (paramInt < i)) {}
      while ((paramInt <= i) || (paramInt > j + i)) {
        return false;
      }
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool4 = true;
      boolean bool3 = true;
      boolean bool5 = true;
      boolean bool2 = true;
      SparseArray[] arrayOfSparseArray;
      if ((this.currentType == 1) || (this.currentType == 4))
      {
        paramViewHolder = (SharedDocumentCell)paramViewHolder.itemView;
        localMessageObject = getItem(paramInt);
        if (paramInt != getItemCount() - 1)
        {
          bool1 = true;
          paramViewHolder.setDocument(localMessageObject, bool1);
          if (!MediaActivity.this.actionBar.isActionModeShowed()) {
            break label158;
          }
          arrayOfSparseArray = MediaActivity.this.selectedFiles;
          if (localMessageObject.getDialogId() != MediaActivity.this.dialog_id) {
            break label142;
          }
          paramInt = 0;
          if (arrayOfSparseArray[paramInt].indexOfKey(localMessageObject.getId()) < 0) {
            break label147;
          }
          bool1 = true;
          if (MediaActivity.this.scrolling) {
            break label152;
          }
          paramViewHolder.setChecked(bool1, bool2);
        }
      }
      label142:
      label147:
      label152:
      label158:
      while (this.currentType != 3)
      {
        for (;;)
        {
          return;
          bool1 = false;
          continue;
          paramInt = 1;
          continue;
          bool1 = false;
          continue;
          bool2 = false;
        }
        if (!MediaActivity.this.scrolling) {}
        for (bool1 = bool4;; bool1 = false)
        {
          paramViewHolder.setChecked(false, bool1);
          return;
        }
      }
      paramViewHolder = (SharedLinkCell)paramViewHolder.itemView;
      MessageObject localMessageObject = getItem(paramInt);
      if (paramInt != getItemCount() - 1)
      {
        bool1 = true;
        paramViewHolder.setLink(localMessageObject, bool1);
        if (!MediaActivity.this.actionBar.isActionModeShowed()) {
          break label325;
        }
        arrayOfSparseArray = MediaActivity.this.selectedFiles;
        if (localMessageObject.getDialogId() != MediaActivity.this.dialog_id) {
          break label309;
        }
        paramInt = 0;
        label265:
        if (arrayOfSparseArray[paramInt].indexOfKey(localMessageObject.getId()) < 0) {
          break label314;
        }
        bool1 = true;
        label282:
        if (MediaActivity.this.scrolling) {
          break label319;
        }
      }
      label309:
      label314:
      label319:
      for (bool2 = bool3;; bool2 = false)
      {
        paramViewHolder.setChecked(bool1, bool2);
        return;
        bool1 = false;
        break;
        paramInt = 1;
        break label265;
        bool1 = false;
        break label282;
      }
      label325:
      if (!MediaActivity.this.scrolling) {}
      for (boolean bool1 = bool5;; bool1 = false)
      {
        paramViewHolder.setChecked(false, bool1);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      if ((this.currentType == 1) || (this.currentType == 4)) {
        paramViewGroup = new SharedDocumentCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new SharedLinkCell(this.mContext);
        ((SharedLinkCell)paramViewGroup).setDelegate(new SharedLinkCell.SharedLinkCellDelegate()
        {
          public boolean canPerformActions()
          {
            return !MediaActivity.this.actionBar.isActionModeShowed();
          }
          
          public void needOpenWebView(TLRPC.WebPage paramAnonymousWebPage)
          {
            MediaActivity.this.openWebView(paramAnonymousWebPage);
          }
        });
      }
    }
    
    public void queryServerSearch(String paramString, final int paramInt, long paramLong)
    {
      final int i = (int)paramLong;
      if (i == 0) {
        return;
      }
      if (this.reqId != 0)
      {
        ConnectionsManager.getInstance(MediaActivity.this.currentAccount).cancelRequest(this.reqId, true);
        this.reqId = 0;
      }
      if ((paramString == null) || (paramString.length() == 0))
      {
        this.globalSearch.clear();
        this.lastReqId = 0;
        notifyDataSetChanged();
        return;
      }
      TLRPC.TL_messages_search localTL_messages_search = new TLRPC.TL_messages_search();
      localTL_messages_search.limit = 50;
      localTL_messages_search.offset_id = paramInt;
      if (this.currentType == 1) {
        localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterDocument();
      }
      for (;;)
      {
        localTL_messages_search.q = paramString;
        localTL_messages_search.peer = MessagesController.getInstance(MediaActivity.this.currentAccount).getInputPeer(i);
        if (localTL_messages_search.peer == null) {
          break;
        }
        i = this.lastReqId + 1;
        this.lastReqId = i;
        this.reqId = ConnectionsManager.getInstance(MediaActivity.this.currentAccount).sendRequest(localTL_messages_search, new RequestDelegate()
        {
          public void run(TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
          {
            final ArrayList localArrayList = new ArrayList();
            if (paramAnonymousTL_error == null)
            {
              paramAnonymousTLObject = (TLRPC.messages_Messages)paramAnonymousTLObject;
              int i = 0;
              if (i < paramAnonymousTLObject.messages.size())
              {
                paramAnonymousTL_error = (TLRPC.Message)paramAnonymousTLObject.messages.get(i);
                if ((paramInt != 0) && (paramAnonymousTL_error.id > paramInt)) {}
                for (;;)
                {
                  i += 1;
                  break;
                  localArrayList.add(new MessageObject(MediaActivity.this.currentAccount, paramAnonymousTL_error, false));
                }
              }
            }
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                if (MediaActivity.MediaSearchAdapter.1.this.val$currentReqId == MediaActivity.MediaSearchAdapter.this.lastReqId)
                {
                  MediaActivity.MediaSearchAdapter.this.globalSearch = localArrayList;
                  MediaActivity.MediaSearchAdapter.this.notifyDataSetChanged();
                }
                MediaActivity.MediaSearchAdapter.access$6002(MediaActivity.MediaSearchAdapter.this, 0);
              }
            });
          }
        }, 2);
        ConnectionsManager.getInstance(MediaActivity.this.currentAccount).bindRequestToGuid(this.reqId, MediaActivity.this.classGuid);
        return;
        if (this.currentType == 3) {
          localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterUrl();
        } else if (this.currentType == 4) {
          localTL_messages_search.filter = new TLRPC.TL_inputMessagesFilterMusic();
        }
      }
    }
    
    public void search(final String paramString)
    {
      try
      {
        if (this.searchTimer != null) {
          this.searchTimer.cancel();
        }
        if (paramString == null)
        {
          this.searchResult.clear();
          notifyDataSetChanged();
          return;
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
        }
        this.searchTimer = new Timer();
        this.searchTimer.schedule(new TimerTask()
        {
          public void run()
          {
            try
            {
              MediaActivity.MediaSearchAdapter.this.searchTimer.cancel();
              MediaActivity.MediaSearchAdapter.access$6402(MediaActivity.MediaSearchAdapter.this, null);
              MediaActivity.MediaSearchAdapter.this.processSearch(paramString);
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
  
  private class SharedDocumentsAdapter
    extends RecyclerListView.SectionsAdapter
  {
    private int currentType;
    private Context mContext;
    
    public SharedDocumentsAdapter(Context paramContext, int paramInt)
    {
      this.mContext = paramContext;
      this.currentType = paramInt;
    }
    
    public int getCountForSection(int paramInt)
    {
      if (paramInt < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).size()) {
        return ((ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[this.currentType]).get(MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).get(paramInt))).size() + 1;
      }
      return 1;
    }
    
    public Object getItem(int paramInt1, int paramInt2)
    {
      return null;
    }
    
    public int getItemViewType(int paramInt1, int paramInt2)
    {
      if (paramInt1 < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).size())
      {
        if (paramInt2 == 0) {
          return 0;
        }
        return 1;
      }
      return 2;
    }
    
    public String getLetter(int paramInt)
    {
      return null;
    }
    
    public int getPositionForScrollProgress(float paramFloat)
    {
      return 0;
    }
    
    public int getSectionCount()
    {
      int j = 1;
      int k = MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).size();
      int i;
      if (!MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).isEmpty())
      {
        i = j;
        if (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[this.currentType])[0] != 0)
        {
          i = j;
          if (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[this.currentType])[1] == 0) {}
        }
      }
      else
      {
        i = 0;
      }
      return i + k;
    }
    
    public View getSectionHeaderView(int paramInt, View paramView)
    {
      Object localObject = paramView;
      if (paramView == null) {
        localObject = new GraySectionCell(this.mContext);
      }
      if (paramInt < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).size())
      {
        paramView = (String)MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).get(paramInt);
        paramView = (MessageObject)((ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[this.currentType]).get(paramView)).get(0);
        ((GraySectionCell)localObject).setText(LocaleController.getInstance().formatterMonthYear.format(paramView.messageOwner.date * 1000L).toUpperCase());
      }
      return (View)localObject;
    }
    
    public boolean isEnabled(int paramInt1, int paramInt2)
    {
      return paramInt2 != 0;
    }
    
    public void onBindViewHolder(int paramInt1, int paramInt2, RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool3 = true;
      boolean bool2 = true;
      Object localObject;
      if (paramViewHolder.getItemViewType() != 2)
      {
        localObject = (String)MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).get(paramInt1);
        localObject = (ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[this.currentType]).get(localObject);
      }
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      case 0: 
        localObject = (MessageObject)((ArrayList)localObject).get(0);
        ((GraySectionCell)paramViewHolder.itemView).setText(LocaleController.getInstance().formatterMonthYear.format(((MessageObject)localObject).messageOwner.date * 1000L).toUpperCase());
        return;
      }
      paramViewHolder = (SharedDocumentCell)paramViewHolder.itemView;
      MessageObject localMessageObject = (MessageObject)((ArrayList)localObject).get(paramInt2 - 1);
      if ((paramInt2 != ((ArrayList)localObject).size()) || ((paramInt1 == MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[this.currentType]).size() - 1) && (MediaActivity.SharedMediaData.access$600(MediaActivity.this.sharedMediaData[this.currentType]))))
      {
        bool1 = true;
        paramViewHolder.setDocument(localMessageObject, bool1);
        if (!MediaActivity.this.actionBar.isActionModeShowed()) {
          break label319;
        }
        localObject = MediaActivity.this.selectedFiles;
        if (localMessageObject.getDialogId() != MediaActivity.this.dialog_id) {
          break label302;
        }
        paramInt1 = 0;
        label259:
        if (localObject[paramInt1].indexOfKey(localMessageObject.getId()) < 0) {
          break label307;
        }
        bool1 = true;
        label277:
        if (MediaActivity.this.scrolling) {
          break label313;
        }
      }
      for (;;)
      {
        paramViewHolder.setChecked(bool1, bool2);
        return;
        bool1 = false;
        break;
        label302:
        paramInt1 = 1;
        break label259;
        label307:
        bool1 = false;
        break label277;
        label313:
        bool2 = false;
      }
      label319:
      if (!MediaActivity.this.scrolling) {}
      for (boolean bool1 = bool3;; bool1 = false)
      {
        paramViewHolder.setChecked(false, bool1);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new LoadingCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new GraySectionCell(this.mContext);
        continue;
        paramViewGroup = new SharedDocumentCell(this.mContext);
      }
    }
  }
  
  private class SharedLinksAdapter
    extends RecyclerListView.SectionsAdapter
  {
    private Context mContext;
    
    public SharedLinksAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getCountForSection(int paramInt)
    {
      if (paramInt < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).size()) {
        return ((ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[3]).get(MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).get(paramInt))).size() + 1;
      }
      return 1;
    }
    
    public Object getItem(int paramInt1, int paramInt2)
    {
      return null;
    }
    
    public int getItemViewType(int paramInt1, int paramInt2)
    {
      if (paramInt1 < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).size())
      {
        if (paramInt2 == 0) {
          return 0;
        }
        return 1;
      }
      return 2;
    }
    
    public String getLetter(int paramInt)
    {
      return null;
    }
    
    public int getPositionForScrollProgress(float paramFloat)
    {
      return 0;
    }
    
    public int getSectionCount()
    {
      int j = 1;
      int k = MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).size();
      int i;
      if (!MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).isEmpty())
      {
        i = j;
        if (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[3])[0] != 0)
        {
          i = j;
          if (MediaActivity.SharedMediaData.access$500(MediaActivity.this.sharedMediaData[3])[1] == 0) {}
        }
      }
      else
      {
        i = 0;
      }
      return i + k;
    }
    
    public View getSectionHeaderView(int paramInt, View paramView)
    {
      Object localObject = paramView;
      if (paramView == null) {
        localObject = new GraySectionCell(this.mContext);
      }
      if (paramInt < MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).size())
      {
        paramView = (String)MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).get(paramInt);
        paramView = (MessageObject)((ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[3]).get(paramView)).get(0);
        ((GraySectionCell)localObject).setText(LocaleController.getInstance().formatterMonthYear.format(paramView.messageOwner.date * 1000L).toUpperCase());
      }
      return (View)localObject;
    }
    
    public boolean isEnabled(int paramInt1, int paramInt2)
    {
      return paramInt2 != 0;
    }
    
    public void onBindViewHolder(int paramInt1, int paramInt2, RecyclerView.ViewHolder paramViewHolder)
    {
      boolean bool3 = true;
      boolean bool2 = true;
      Object localObject;
      if (paramViewHolder.getItemViewType() != 2)
      {
        localObject = (String)MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).get(paramInt1);
        localObject = (ArrayList)MediaActivity.SharedMediaData.access$4800(MediaActivity.this.sharedMediaData[3]).get(localObject);
      }
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      case 0: 
        localObject = (MessageObject)((ArrayList)localObject).get(0);
        ((GraySectionCell)paramViewHolder.itemView).setText(LocaleController.getInstance().formatterMonthYear.format(((MessageObject)localObject).messageOwner.date * 1000L).toUpperCase());
        return;
      }
      paramViewHolder = (SharedLinkCell)paramViewHolder.itemView;
      MessageObject localMessageObject = (MessageObject)((ArrayList)localObject).get(paramInt2 - 1);
      if ((paramInt2 != ((ArrayList)localObject).size()) || ((paramInt1 == MediaActivity.SharedMediaData.access$4700(MediaActivity.this.sharedMediaData[3]).size() - 1) && (MediaActivity.SharedMediaData.access$600(MediaActivity.this.sharedMediaData[3]))))
      {
        bool1 = true;
        paramViewHolder.setLink(localMessageObject, bool1);
        if (!MediaActivity.this.actionBar.isActionModeShowed()) {
          break label309;
        }
        localObject = MediaActivity.this.selectedFiles;
        if (localMessageObject.getDialogId() != MediaActivity.this.dialog_id) {
          break label292;
        }
        paramInt1 = 0;
        label249:
        if (localObject[paramInt1].indexOfKey(localMessageObject.getId()) < 0) {
          break label297;
        }
        bool1 = true;
        label267:
        if (MediaActivity.this.scrolling) {
          break label303;
        }
      }
      for (;;)
      {
        paramViewHolder.setChecked(bool1, bool2);
        return;
        bool1 = false;
        break;
        label292:
        paramInt1 = 1;
        break label249;
        label297:
        bool1 = false;
        break label267;
        label303:
        bool2 = false;
      }
      label309:
      if (!MediaActivity.this.scrolling) {}
      for (boolean bool1 = bool3;; bool1 = false)
      {
        paramViewHolder.setChecked(false, bool1);
        return;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new LoadingCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new GraySectionCell(this.mContext);
        continue;
        paramViewGroup = new SharedLinkCell(this.mContext);
        ((SharedLinkCell)paramViewGroup).setDelegate(new SharedLinkCell.SharedLinkCellDelegate()
        {
          public boolean canPerformActions()
          {
            return !MediaActivity.this.actionBar.isActionModeShowed();
          }
          
          public void needOpenWebView(TLRPC.WebPage paramAnonymousWebPage)
          {
            MediaActivity.this.openWebView(paramAnonymousWebPage);
          }
        });
      }
    }
  }
  
  private class SharedMediaData
  {
    private boolean[] endReached = { 0, 1 };
    private boolean loading;
    private int[] max_id = { 0, 0 };
    private ArrayList<MessageObject> messages = new ArrayList();
    private SparseArray<MessageObject>[] messagesDict = { new SparseArray(), new SparseArray() };
    private HashMap<String, ArrayList<MessageObject>> sectionArrays = new HashMap();
    private ArrayList<String> sections = new ArrayList();
    private int totalCount;
    
    private SharedMediaData() {}
    
    public boolean addMessage(MessageObject paramMessageObject, boolean paramBoolean1, boolean paramBoolean2)
    {
      if (paramMessageObject.getDialogId() == MediaActivity.this.dialog_id) {}
      for (int i = 0; this.messagesDict[i].indexOfKey(paramMessageObject.getId()) >= 0; i = 1) {
        return false;
      }
      ArrayList localArrayList2 = (ArrayList)this.sectionArrays.get(paramMessageObject.monthKey);
      ArrayList localArrayList1 = localArrayList2;
      if (localArrayList2 == null)
      {
        localArrayList1 = new ArrayList();
        this.sectionArrays.put(paramMessageObject.monthKey, localArrayList1);
        if (paramBoolean1) {
          this.sections.add(0, paramMessageObject.monthKey);
        }
      }
      else
      {
        if (!paramBoolean1) {
          break label191;
        }
        localArrayList1.add(0, paramMessageObject);
        this.messages.add(0, paramMessageObject);
        label127:
        this.messagesDict[i].put(paramMessageObject.getId(), paramMessageObject);
        if (paramBoolean2) {
          break label210;
        }
        if (paramMessageObject.getId() > 0) {
          this.max_id[i] = Math.min(paramMessageObject.getId(), this.max_id[i]);
        }
      }
      for (;;)
      {
        return true;
        this.sections.add(paramMessageObject.monthKey);
        break;
        label191:
        localArrayList1.add(paramMessageObject);
        this.messages.add(paramMessageObject);
        break label127;
        label210:
        this.max_id[i] = Math.max(paramMessageObject.getId(), this.max_id[i]);
      }
    }
    
    public boolean deleteMessage(int paramInt1, int paramInt2)
    {
      MessageObject localMessageObject = (MessageObject)this.messagesDict[paramInt2].get(paramInt1);
      if (localMessageObject == null) {}
      ArrayList localArrayList;
      do
      {
        return false;
        localArrayList = (ArrayList)this.sectionArrays.get(localMessageObject.monthKey);
      } while (localArrayList == null);
      localArrayList.remove(localMessageObject);
      this.messages.remove(localMessageObject);
      this.messagesDict[paramInt2].remove(localMessageObject.getId());
      if (localArrayList.isEmpty())
      {
        this.sectionArrays.remove(localMessageObject.monthKey);
        this.sections.remove(localMessageObject.monthKey);
      }
      this.totalCount -= 1;
      return true;
    }
    
    public void replaceMid(int paramInt1, int paramInt2)
    {
      MessageObject localMessageObject = (MessageObject)this.messagesDict[0].get(paramInt1);
      if (localMessageObject != null)
      {
        this.messagesDict[0].remove(paramInt1);
        this.messagesDict[0].put(paramInt2, localMessageObject);
        localMessageObject.messageOwner.id = paramInt2;
      }
    }
  }
  
  private class SharedPhotoVideoAdapter
    extends RecyclerListView.SectionsAdapter
  {
    private Context mContext;
    
    public SharedPhotoVideoAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public int getCountForSection(int paramInt)
    {
      if (paramInt < MediaActivity.access$3700(MediaActivity.this)[0].sections.size()) {
        return (int)Math.ceil(((ArrayList)MediaActivity.access$3700(MediaActivity.this)[0].sectionArrays.get(MediaActivity.access$3700(MediaActivity.this)[0].sections.get(paramInt))).size() / MediaActivity.this.columnsCount) + 1;
      }
      return 1;
    }
    
    public Object getItem(int paramInt1, int paramInt2)
    {
      return null;
    }
    
    public int getItemViewType(int paramInt1, int paramInt2)
    {
      if (paramInt1 < MediaActivity.access$3700(MediaActivity.this)[0].sections.size())
      {
        if (paramInt2 == 0) {
          return 0;
        }
        return 1;
      }
      return 2;
    }
    
    public String getLetter(int paramInt)
    {
      return null;
    }
    
    public int getPositionForScrollProgress(float paramFloat)
    {
      return 0;
    }
    
    public int getSectionCount()
    {
      int j = 1;
      int k = MediaActivity.access$3700(MediaActivity.this)[0].sections.size();
      int i;
      if (!MediaActivity.access$3700(MediaActivity.this)[0].sections.isEmpty())
      {
        i = j;
        if (MediaActivity.access$3700(MediaActivity.this)[0].endReached[0] != 0)
        {
          i = j;
          if (MediaActivity.access$3700(MediaActivity.this)[0].endReached[1] == 0) {}
        }
      }
      else
      {
        i = 0;
      }
      return i + k;
    }
    
    public View getSectionHeaderView(int paramInt, View paramView)
    {
      Object localObject = paramView;
      if (paramView == null)
      {
        localObject = new SharedMediaSectionCell(this.mContext);
        ((View)localObject).setBackgroundColor(Theme.getColor("windowBackgroundWhite"));
      }
      if (paramInt < MediaActivity.access$3700(MediaActivity.this)[0].sections.size())
      {
        paramView = (String)MediaActivity.access$3700(MediaActivity.this)[0].sections.get(paramInt);
        paramView = (MessageObject)((ArrayList)MediaActivity.access$3700(MediaActivity.this)[0].sectionArrays.get(paramView)).get(0);
        ((SharedMediaSectionCell)localObject).setText(LocaleController.getInstance().formatterMonthYear.format(paramView.messageOwner.date * 1000L).toUpperCase());
      }
      return (View)localObject;
    }
    
    public boolean isEnabled(int paramInt1, int paramInt2)
    {
      return false;
    }
    
    public void onBindViewHolder(int paramInt1, int paramInt2, RecyclerView.ViewHolder paramViewHolder)
    {
      Object localObject;
      if (paramViewHolder.getItemViewType() != 2)
      {
        localObject = (String)MediaActivity.access$3700(MediaActivity.this)[0].sections.get(paramInt1);
        localObject = (ArrayList)MediaActivity.access$3700(MediaActivity.this)[0].sectionArrays.get(localObject);
      }
      switch (paramViewHolder.getItemViewType())
      {
      default: 
        return;
      case 0: 
        localObject = (MessageObject)((ArrayList)localObject).get(0);
        ((SharedMediaSectionCell)paramViewHolder.itemView).setText(LocaleController.getInstance().formatterMonthYear.format(((MessageObject)localObject).messageOwner.date * 1000L).toUpperCase());
        return;
      }
      paramViewHolder = (SharedPhotoVideoCell)paramViewHolder.itemView;
      paramViewHolder.setItemsCount(MediaActivity.this.columnsCount);
      paramInt1 = 0;
      if (paramInt1 < MediaActivity.this.columnsCount)
      {
        int i = (paramInt2 - 1) * MediaActivity.this.columnsCount + paramInt1;
        boolean bool1;
        label201:
        label272:
        label291:
        boolean bool2;
        if (i < ((ArrayList)localObject).size())
        {
          MessageObject localMessageObject = (MessageObject)((ArrayList)localObject).get(i);
          if (paramInt2 == 1)
          {
            bool1 = true;
            paramViewHolder.setIsFirst(bool1);
            paramViewHolder.setItem(paramInt1, MediaActivity.access$3700(MediaActivity.this)[0].messages.indexOf(localMessageObject), localMessageObject);
            if (!MediaActivity.this.actionBar.isActionModeShowed()) {
              break label344;
            }
            SparseArray[] arrayOfSparseArray = MediaActivity.this.selectedFiles;
            if (localMessageObject.getDialogId() != MediaActivity.this.dialog_id) {
              break label326;
            }
            i = 0;
            if (arrayOfSparseArray[i].indexOfKey(localMessageObject.getId()) < 0) {
              break label332;
            }
            bool1 = true;
            if (MediaActivity.this.scrolling) {
              break label338;
            }
            bool2 = true;
            label304:
            paramViewHolder.setChecked(paramInt1, bool1, bool2);
          }
        }
        for (;;)
        {
          paramInt1 += 1;
          break;
          bool1 = false;
          break label201;
          label326:
          i = 1;
          break label272;
          label332:
          bool1 = false;
          break label291;
          label338:
          bool2 = false;
          break label304;
          label344:
          if (!MediaActivity.this.scrolling) {}
          for (bool1 = true;; bool1 = false)
          {
            paramViewHolder.setChecked(paramInt1, false, bool1);
            break;
          }
          paramViewHolder.setItem(paramInt1, i, null);
        }
      }
      paramViewHolder.requestLayout();
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
      case 0: 
        for (paramViewGroup = new LoadingCell(this.mContext);; paramViewGroup = new SharedMediaSectionCell(this.mContext)) {
          return new RecyclerListView.Holder(paramViewGroup);
        }
      }
      if (!MediaActivity.this.cellCache.isEmpty())
      {
        paramViewGroup = (View)MediaActivity.this.cellCache.get(0);
        MediaActivity.this.cellCache.remove(0);
      }
      for (;;)
      {
        ((SharedPhotoVideoCell)paramViewGroup).setDelegate(new SharedPhotoVideoCell.SharedPhotoVideoCellDelegate()
        {
          public void didClickItem(SharedPhotoVideoCell paramAnonymousSharedPhotoVideoCell, int paramAnonymousInt1, MessageObject paramAnonymousMessageObject, int paramAnonymousInt2)
          {
            MediaActivity.this.onItemClick(paramAnonymousInt1, paramAnonymousSharedPhotoVideoCell, paramAnonymousMessageObject, paramAnonymousInt2);
          }
          
          public boolean didLongClickItem(SharedPhotoVideoCell paramAnonymousSharedPhotoVideoCell, int paramAnonymousInt1, MessageObject paramAnonymousMessageObject, int paramAnonymousInt2)
          {
            return MediaActivity.this.onItemLongClick(paramAnonymousMessageObject, paramAnonymousSharedPhotoVideoCell, paramAnonymousInt2);
          }
        });
        break;
        paramViewGroup = new SharedPhotoVideoCell(this.mContext);
      }
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/MediaActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */