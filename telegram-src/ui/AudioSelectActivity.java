package org.telegram.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.provider.MediaStore.Audio.Media;
import android.util.LongSparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.AudioEntry;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.MessageMedia;
import org.telegram.tgnet.TLRPC.Peer;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentAttributeAudio;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_message;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_peerUser;
import org.telegram.tgnet.TLRPC.TL_photoSizeEmpty;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.AudioCell;
import org.telegram.ui.Cells.AudioCell.AudioCellDelegate;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PickerBottomLayout;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class AudioSelectActivity
  extends BaseFragment
  implements NotificationCenter.NotificationCenterDelegate
{
  private ArrayList<MediaController.AudioEntry> audioEntries = new ArrayList();
  private PickerBottomLayout bottomLayout;
  private AudioSelectActivityDelegate delegate;
  private RecyclerListView listView;
  private ListAdapter listViewAdapter;
  private boolean loadingAudio;
  private MessageObject playingAudio;
  private EmptyTextProgressView progressView;
  private LongSparseArray<MediaController.AudioEntry> selectedAudios = new LongSparseArray();
  private View shadow;
  
  private void loadAudio()
  {
    this.loadingAudio = true;
    if (this.progressView != null) {
      this.progressView.showProgress();
    }
    Utilities.globalQueue.postRunnable(new Runnable()
    {
      public void run()
      {
        final ArrayList localArrayList = new ArrayList();
        localObject3 = null;
        localObject1 = null;
        try
        {
          Cursor localCursor = ApplicationLoader.applicationContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { "_id", "artist", "title", "_data", "duration", "album" }, "is_music != 0", null, "title");
          int i = -2000000000;
          localObject1 = localCursor;
          localObject3 = localCursor;
          if (localCursor.moveToNext())
          {
            localObject1 = localCursor;
            localObject3 = localCursor;
            MediaController.AudioEntry localAudioEntry = new MediaController.AudioEntry();
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.id = localCursor.getInt(0);
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.author = localCursor.getString(1);
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.title = localCursor.getString(2);
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.path = localCursor.getString(3);
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.duration = ((int)(localCursor.getLong(4) / 1000L));
            localObject1 = localCursor;
            localObject3 = localCursor;
            localAudioEntry.genre = localCursor.getString(5);
            localObject1 = localCursor;
            localObject3 = localCursor;
            File localFile = new File(localAudioEntry.path);
            localObject1 = localCursor;
            localObject3 = localCursor;
            TLRPC.TL_message localTL_message = new TLRPC.TL_message();
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.out = true;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.id = i;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.to_id = new TLRPC.TL_peerUser();
            localObject1 = localCursor;
            localObject3 = localCursor;
            Object localObject4 = localTL_message.to_id;
            localObject1 = localCursor;
            localObject3 = localCursor;
            int j = UserConfig.getInstance(AudioSelectActivity.this.currentAccount).getClientUserId();
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.from_id = j;
            localObject1 = localCursor;
            localObject3 = localCursor;
            ((TLRPC.Peer)localObject4).user_id = j;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.date = ((int)(System.currentTimeMillis() / 1000L));
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.message = "";
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.attachPath = localAudioEntry.path;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.media = new TLRPC.TL_messageMediaDocument();
            localObject1 = localCursor;
            localObject3 = localCursor;
            localObject4 = localTL_message.media;
            localObject1 = localCursor;
            localObject3 = localCursor;
            ((TLRPC.MessageMedia)localObject4).flags |= 0x3;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.media.document = new TLRPC.TL_document();
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.flags |= 0x300;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localObject4 = FileLoader.getFileExtension(localFile);
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.media.document.id = 0L;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.media.document.access_hash = 0L;
            localObject1 = localCursor;
            localObject3 = localCursor;
            localTL_message.media.document.date = localTL_message.date;
            localObject1 = localCursor;
            localObject3 = localCursor;
            TLRPC.Document localDocument = localTL_message.media.document;
            localObject1 = localCursor;
            localObject3 = localCursor;
            StringBuilder localStringBuilder = new StringBuilder().append("audio/");
            localObject1 = localCursor;
            localObject3 = localCursor;
            if (((String)localObject4).length() > 0) {}
            for (;;)
            {
              localObject1 = localCursor;
              localObject3 = localCursor;
              localDocument.mime_type = ((String)localObject4);
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.size = ((int)localFile.length());
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.thumb = new TLRPC.TL_photoSizeEmpty();
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.thumb.type = "s";
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.dc_id = 0;
              localObject1 = localCursor;
              localObject3 = localCursor;
              localObject4 = new TLRPC.TL_documentAttributeAudio();
              localObject1 = localCursor;
              localObject3 = localCursor;
              ((TLRPC.TL_documentAttributeAudio)localObject4).duration = localAudioEntry.duration;
              localObject1 = localCursor;
              localObject3 = localCursor;
              ((TLRPC.TL_documentAttributeAudio)localObject4).title = localAudioEntry.title;
              localObject1 = localCursor;
              localObject3 = localCursor;
              ((TLRPC.TL_documentAttributeAudio)localObject4).performer = localAudioEntry.author;
              localObject1 = localCursor;
              localObject3 = localCursor;
              ((TLRPC.TL_documentAttributeAudio)localObject4).flags |= 0x3;
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.attributes.add(localObject4);
              localObject1 = localCursor;
              localObject3 = localCursor;
              localObject4 = new TLRPC.TL_documentAttributeFilename();
              localObject1 = localCursor;
              localObject3 = localCursor;
              ((TLRPC.TL_documentAttributeFilename)localObject4).file_name = localFile.getName();
              localObject1 = localCursor;
              localObject3 = localCursor;
              localTL_message.media.document.attributes.add(localObject4);
              localObject1 = localCursor;
              localObject3 = localCursor;
              localAudioEntry.messageObject = new MessageObject(AudioSelectActivity.this.currentAccount, localTL_message, false);
              localObject1 = localCursor;
              localObject3 = localCursor;
              localArrayList.add(localAudioEntry);
              i -= 1;
              break;
              localObject4 = "mp3";
            }
          }
          if (localCursor != null) {
            localCursor.close();
          }
        }
        catch (Exception localException)
        {
          for (;;)
          {
            localObject3 = localObject1;
            FileLog.e(localException);
            if (localObject1 != null) {
              ((Cursor)localObject1).close();
            }
          }
        }
        finally
        {
          if (localObject3 == null) {
            break label1048;
          }
          ((Cursor)localObject3).close();
        }
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            AudioSelectActivity.access$502(AudioSelectActivity.this, localArrayList);
            AudioSelectActivity.this.progressView.showTextView();
            AudioSelectActivity.this.listViewAdapter.notifyDataSetChanged();
          }
        });
      }
    });
  }
  
  private void updateBottomLayoutCount()
  {
    this.bottomLayout.updateSelectedCount(this.selectedAudios.size(), true);
  }
  
  public View createView(Context paramContext)
  {
    int i = 1;
    this.actionBar.setBackButtonImage(2131165346);
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("AttachMusic", 2131493036));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        if (paramAnonymousInt == -1) {
          AudioSelectActivity.this.finishFragment();
        }
      }
    });
    this.fragmentView = new FrameLayout(paramContext);
    FrameLayout localFrameLayout = (FrameLayout)this.fragmentView;
    this.progressView = new EmptyTextProgressView(paramContext);
    this.progressView.setText(LocaleController.getString("NoAudio", 2131493879));
    localFrameLayout.addView(this.progressView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setEmptyView(this.progressView);
    this.listView.setVerticalScrollBarEnabled(false);
    this.listView.setLayoutManager(new LinearLayoutManager(paramContext, 1, false));
    RecyclerListView localRecyclerListView = this.listView;
    ListAdapter localListAdapter = new ListAdapter(paramContext);
    this.listViewAdapter = localListAdapter;
    localRecyclerListView.setAdapter(localListAdapter);
    localRecyclerListView = this.listView;
    if (LocaleController.isRTL)
    {
      localRecyclerListView.setVerticalScrollbarPosition(i);
      localFrameLayout.addView(this.listView, LayoutHelper.createFrame(-1, -1.0F, 51, 0.0F, 0.0F, 0.0F, 48.0F));
      this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
      {
        public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
        {
          paramAnonymousView = (AudioCell)paramAnonymousView;
          MediaController.AudioEntry localAudioEntry = paramAnonymousView.getAudioEntry();
          if (AudioSelectActivity.this.selectedAudios.indexOfKey(localAudioEntry.id) >= 0)
          {
            AudioSelectActivity.this.selectedAudios.remove(localAudioEntry.id);
            paramAnonymousView.setChecked(false);
          }
          for (;;)
          {
            AudioSelectActivity.this.updateBottomLayoutCount();
            return;
            AudioSelectActivity.this.selectedAudios.put(localAudioEntry.id, localAudioEntry);
            paramAnonymousView.setChecked(true);
          }
        }
      });
      this.bottomLayout = new PickerBottomLayout(paramContext, false);
      localFrameLayout.addView(this.bottomLayout, LayoutHelper.createFrame(-1, 48, 80));
      this.bottomLayout.cancelButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          AudioSelectActivity.this.finishFragment();
        }
      });
      this.bottomLayout.doneButton.setOnClickListener(new View.OnClickListener()
      {
        public void onClick(View paramAnonymousView)
        {
          if (AudioSelectActivity.this.delegate != null)
          {
            paramAnonymousView = new ArrayList();
            int i = 0;
            while (i < AudioSelectActivity.this.selectedAudios.size())
            {
              paramAnonymousView.add(((MediaController.AudioEntry)AudioSelectActivity.this.selectedAudios.valueAt(i)).messageObject);
              i += 1;
            }
            AudioSelectActivity.this.delegate.didSelectAudio(paramAnonymousView);
          }
          AudioSelectActivity.this.finishFragment();
        }
      });
      paramContext = new View(paramContext);
      paramContext.setBackgroundResource(2131165343);
      localFrameLayout.addView(paramContext, LayoutHelper.createFrame(-1, 3.0F, 83, 0.0F, 0.0F, 0.0F, 48.0F));
      if (!this.loadingAudio) {
        break label368;
      }
      this.progressView.showProgress();
    }
    for (;;)
    {
      updateBottomLayoutCount();
      return this.fragmentView;
      i = 2;
      break;
      label368:
      this.progressView.showTextView();
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    if (paramInt1 == NotificationCenter.closeChats) {
      removeSelfFromStack();
    }
    while ((paramInt1 != NotificationCenter.messagePlayingDidReset) || (this.listViewAdapter == null)) {
      return;
    }
    this.listViewAdapter.notifyDataSetChanged();
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    ThemeDescription localThemeDescription1 = new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite");
    ThemeDescription localThemeDescription2 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription3 = new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault");
    ThemeDescription localThemeDescription4 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon");
    ThemeDescription localThemeDescription5 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle");
    ThemeDescription localThemeDescription6 = new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector");
    ThemeDescription localThemeDescription7 = new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21");
    RecyclerListView localRecyclerListView = this.listView;
    Paint localPaint = Theme.dividerPaint;
    return new ThemeDescription[] { localThemeDescription1, localThemeDescription2, localThemeDescription3, localThemeDescription4, localThemeDescription5, localThemeDescription6, localThemeDescription7, new ThemeDescription(localRecyclerListView, 0, new Class[] { View.class }, localPaint, null, null, "divider"), new ThemeDescription(this.progressView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder"), new ThemeDescription(this.progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, "progressCircle"), new ThemeDescription(this.listView, 0, new Class[] { AudioCell.class }, new String[] { "titleTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, 0, new Class[] { AudioCell.class }, new String[] { "genreTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { AudioCell.class }, new String[] { "authorTextView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, 0, new Class[] { AudioCell.class }, new String[] { "timeTextView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { AudioCell.class }, new String[] { "checkBox" }, null, null, null, "musicPicker_checkbox"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { AudioCell.class }, new String[] { "checkBox" }, null, null, null, "musicPicker_checkboxCheck"), new ThemeDescription(this.listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { AudioCell.class }, new String[] { "playButton" }, null, null, null, "musicPicker_buttonIcon"), new ThemeDescription(this.listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { AudioCell.class }, new String[] { "playButton" }, null, null, null, "musicPicker_buttonBackground"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { PickerBottomLayout.class }, new String[] { "cancelButton" }, null, null, null, "picker_enabledButton"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { PickerBottomLayout.class }, new String[] { "doneButtonTextView" }, null, null, null, "picker_enabledButton"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[] { PickerBottomLayout.class }, new String[] { "doneButtonTextView" }, null, null, null, "picker_disabledButton"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { PickerBottomLayout.class }, new String[] { "doneButtonBadgeTextView" }, null, null, null, "picker_badgeText"), new ThemeDescription(this.bottomLayout, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[] { PickerBottomLayout.class }, new String[] { "doneButtonBadgeTextView" }, null, null, null, "picker_badge") };
  }
  
  public boolean onFragmentCreate()
  {
    super.onFragmentCreate();
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
    loadAudio();
    return true;
  }
  
  public void onFragmentDestroy()
  {
    super.onFragmentDestroy();
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.closeChats);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
    if ((this.playingAudio != null) && (MediaController.getInstance().isPlayingMessage(this.playingAudio))) {
      MediaController.getInstance().cleanupPlayer(true, true);
    }
  }
  
  public void setDelegate(AudioSelectActivityDelegate paramAudioSelectActivityDelegate)
  {
    this.delegate = paramAudioSelectActivityDelegate;
  }
  
  public static abstract interface AudioSelectActivityDelegate
  {
    public abstract void didSelectAudio(ArrayList<MessageObject> paramArrayList);
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public Object getItem(int paramInt)
    {
      return AudioSelectActivity.this.audioEntries.get(paramInt);
    }
    
    public int getItemCount()
    {
      return AudioSelectActivity.this.audioEntries.size();
    }
    
    public long getItemId(int paramInt)
    {
      return paramInt;
    }
    
    public int getItemViewType(int paramInt)
    {
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return true;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool2 = true;
      MediaController.AudioEntry localAudioEntry1 = (MediaController.AudioEntry)AudioSelectActivity.this.audioEntries.get(paramInt);
      paramViewHolder = (AudioCell)paramViewHolder.itemView;
      MediaController.AudioEntry localAudioEntry2 = (MediaController.AudioEntry)AudioSelectActivity.this.audioEntries.get(paramInt);
      boolean bool1;
      if (paramInt != AudioSelectActivity.this.audioEntries.size() - 1)
      {
        bool1 = true;
        if (AudioSelectActivity.this.selectedAudios.indexOfKey(localAudioEntry1.id) < 0) {
          break label94;
        }
      }
      for (;;)
      {
        paramViewHolder.setAudio(localAudioEntry2, bool1, bool2);
        return;
        bool1 = false;
        break;
        label94:
        bool2 = false;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      paramViewGroup = new AudioCell(this.mContext);
      paramViewGroup.setDelegate(new AudioCell.AudioCellDelegate()
      {
        public void startedPlayingAudio(MessageObject paramAnonymousMessageObject)
        {
          AudioSelectActivity.access$802(AudioSelectActivity.this, paramAnonymousMessageObject);
        }
      });
      return new RecyclerListView.Holder(paramViewGroup);
    }
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/AudioSelectActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */