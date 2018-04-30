package org.telegram.ui.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeSticker;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_messages_getStickers;
import org.telegram.tgnet.TLRPC.TL_messages_stickers;
import org.telegram.ui.Cells.StickerCell;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class StickersAdapter
  extends RecyclerListView.SelectionAdapter
  implements NotificationCenter.NotificationCenterDelegate
{
  private int currentAccount = UserConfig.selectedAccount;
  private boolean delayLocalResults;
  private StickersAdapterDelegate delegate;
  private int lastReqId;
  private String lastSticker;
  private Context mContext;
  private ArrayList<TLRPC.Document> stickers;
  private HashMap<String, TLRPC.Document> stickersMap;
  private ArrayList<String> stickersToLoad = new ArrayList();
  private boolean visible;
  
  public StickersAdapter(Context paramContext, StickersAdapterDelegate paramStickersAdapterDelegate)
  {
    this.mContext = paramContext;
    this.delegate = paramStickersAdapterDelegate;
    DataQuery.getInstance(this.currentAccount).checkStickers(0);
    DataQuery.getInstance(this.currentAccount).checkStickers(1);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).addObserver(this, NotificationCenter.FileDidFailedLoad);
  }
  
  private void addStickerToResult(TLRPC.Document paramDocument)
  {
    if (paramDocument == null) {}
    String str;
    do
    {
      return;
      str = paramDocument.dc_id + "_" + paramDocument.id;
    } while ((this.stickersMap != null) && (this.stickersMap.containsKey(str)));
    if (this.stickers == null)
    {
      this.stickers = new ArrayList();
      this.stickersMap = new HashMap();
    }
    this.stickers.add(paramDocument);
    this.stickersMap.put(str, paramDocument);
  }
  
  private void addStickersToResult(ArrayList<TLRPC.Document> paramArrayList)
  {
    if ((paramArrayList == null) || (paramArrayList.isEmpty())) {
      return;
    }
    int i = 0;
    int j = paramArrayList.size();
    label19:
    TLRPC.Document localDocument;
    String str;
    if (i < j)
    {
      localDocument = (TLRPC.Document)paramArrayList.get(i);
      str = localDocument.dc_id + "_" + localDocument.id;
      if ((this.stickersMap == null) || (!this.stickersMap.containsKey(str))) {
        break label93;
      }
    }
    for (;;)
    {
      i += 1;
      break label19;
      break;
      label93:
      if (this.stickers == null)
      {
        this.stickers = new ArrayList();
        this.stickersMap = new HashMap();
      }
      this.stickers.add(localDocument);
      this.stickersMap.put(str, localDocument);
    }
  }
  
  private boolean checkStickerFilesExistAndDownload()
  {
    if (this.stickers == null) {
      return false;
    }
    this.stickersToLoad.clear();
    int j = Math.min(10, this.stickers.size());
    int i = 0;
    while (i < j)
    {
      TLRPC.Document localDocument = (TLRPC.Document)this.stickers.get(i);
      if (!FileLoader.getPathToAttach(localDocument.thumb, "webp", true).exists())
      {
        this.stickersToLoad.add(FileLoader.getAttachFileName(localDocument.thumb, "webp"));
        FileLoader.getInstance(this.currentAccount).loadFile(localDocument.thumb.location, "webp", 0, 1);
      }
      i += 1;
    }
    return this.stickersToLoad.isEmpty();
  }
  
  private boolean isValidSticker(TLRPC.Document paramDocument, String paramString)
  {
    int i = 0;
    int j = paramDocument.attributes.size();
    while (i < j)
    {
      TLRPC.DocumentAttribute localDocumentAttribute = (TLRPC.DocumentAttribute)paramDocument.attributes.get(i);
      if ((localDocumentAttribute instanceof TLRPC.TL_documentAttributeSticker))
      {
        if ((localDocumentAttribute.alt == null) || (!localDocumentAttribute.alt.contains(paramString))) {
          break;
        }
        return true;
      }
      i += 1;
    }
    return false;
  }
  
  private void searchServerStickers(final String paramString)
  {
    if (this.lastReqId != 0) {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.lastReqId, true);
    }
    TLRPC.TL_messages_getStickers localTL_messages_getStickers = new TLRPC.TL_messages_getStickers();
    localTL_messages_getStickers.emoticon = paramString;
    localTL_messages_getStickers.hash = "";
    localTL_messages_getStickers.exclude_featured = false;
    this.lastReqId = ConnectionsManager.getInstance(this.currentAccount).sendRequest(localTL_messages_getStickers, new RequestDelegate()
    {
      public void run(final TLObject paramAnonymousTLObject, TLRPC.TL_error paramAnonymousTL_error)
      {
        AndroidUtilities.runOnUIThread(new Runnable()
        {
          public void run()
          {
            boolean bool2 = false;
            StickersAdapter.access$002(StickersAdapter.this, 0);
            if ((!StickersAdapter.2.this.val$emoji.equals(StickersAdapter.this.lastSticker)) || (!(paramAnonymousTLObject instanceof TLRPC.TL_messages_stickers))) {
              return;
            }
            StickersAdapter.access$202(StickersAdapter.this, false);
            Object localObject = (TLRPC.TL_messages_stickers)paramAnonymousTLObject;
            int i;
            if (StickersAdapter.this.stickers != null)
            {
              i = StickersAdapter.this.stickers.size();
              label97:
              StickersAdapter.this.addStickersToResult(((TLRPC.TL_messages_stickers)localObject).stickers);
              if (StickersAdapter.this.stickers == null) {
                break label301;
              }
            }
            label301:
            for (int j = StickersAdapter.this.stickers.size();; j = 0)
            {
              if ((!StickersAdapter.this.visible) && (StickersAdapter.this.stickers != null) && (!StickersAdapter.this.stickers.isEmpty()))
              {
                StickersAdapter.this.checkStickerFilesExistAndDownload();
                localObject = StickersAdapter.this.delegate;
                boolean bool1 = bool2;
                if (StickersAdapter.this.stickers != null)
                {
                  bool1 = bool2;
                  if (!StickersAdapter.this.stickers.isEmpty())
                  {
                    bool1 = bool2;
                    if (StickersAdapter.this.stickersToLoad.isEmpty()) {
                      bool1 = true;
                    }
                  }
                }
                ((StickersAdapter.StickersAdapterDelegate)localObject).needChangePanelVisibility(bool1);
                StickersAdapter.access$502(StickersAdapter.this, true);
              }
              if (i == j) {
                break;
              }
              StickersAdapter.this.notifyDataSetChanged();
              return;
              i = 0;
              break label97;
            }
          }
        });
      }
    });
  }
  
  public void clearStickers()
  {
    this.lastSticker = null;
    this.stickers = null;
    this.stickersMap = null;
    this.stickersToLoad.clear();
    notifyDataSetChanged();
    if (this.lastReqId != 0)
    {
      ConnectionsManager.getInstance(this.currentAccount).cancelRequest(this.lastReqId, true);
      this.lastReqId = 0;
    }
  }
  
  public void didReceivedNotification(int paramInt1, int paramInt2, Object... paramVarArgs)
  {
    boolean bool2 = false;
    if (((paramInt1 == NotificationCenter.FileDidLoaded) || (paramInt1 == NotificationCenter.FileDidFailedLoad)) && (this.stickers != null) && (!this.stickers.isEmpty()) && (!this.stickersToLoad.isEmpty()) && (this.visible))
    {
      paramVarArgs = (String)paramVarArgs[0];
      this.stickersToLoad.remove(paramVarArgs);
      if (this.stickersToLoad.isEmpty())
      {
        paramVarArgs = this.delegate;
        boolean bool1 = bool2;
        if (this.stickers != null)
        {
          bool1 = bool2;
          if (!this.stickers.isEmpty())
          {
            bool1 = bool2;
            if (this.stickersToLoad.isEmpty()) {
              bool1 = true;
            }
          }
        }
        paramVarArgs.needChangePanelVisibility(bool1);
      }
    }
  }
  
  public TLRPC.Document getItem(int paramInt)
  {
    if ((this.stickers != null) && (paramInt >= 0) && (paramInt < this.stickers.size())) {
      return (TLRPC.Document)this.stickers.get(paramInt);
    }
    return null;
  }
  
  public int getItemCount()
  {
    if ((!this.delayLocalResults) && (this.stickers != null)) {
      return this.stickers.size();
    }
    return 0;
  }
  
  public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
  {
    return true;
  }
  
  public void loadStikersForEmoji(CharSequence paramCharSequence)
  {
    if (SharedConfig.suggestStickers == 2) {}
    label51:
    label226:
    label302:
    label730:
    label753:
    do
    {
      do
      {
        int i;
        do
        {
          return;
          if ((paramCharSequence != null) && (paramCharSequence.length() > 0) && (paramCharSequence.length() <= 14))
          {
            i = 1;
            if (i == 0) {
              break label753;
            }
            m = paramCharSequence.length();
            i = 0;
            localObject = paramCharSequence;
            if (i >= m) {
              break label302;
            }
            if ((i >= m - 1) || (((((CharSequence)localObject).charAt(i) != 55356) || (((CharSequence)localObject).charAt(i + 1) < 57339) || (((CharSequence)localObject).charAt(i + 1) > 57343)) && ((((CharSequence)localObject).charAt(i) != '‍') || ((((CharSequence)localObject).charAt(i + 1) != '♀') && (((CharSequence)localObject).charAt(i + 1) != '♂'))))) {
              break label226;
            }
            paramCharSequence = TextUtils.concat(new CharSequence[] { ((CharSequence)localObject).subSequence(0, i), ((CharSequence)localObject).subSequence(i + 2, ((CharSequence)localObject).length()) });
            j = m - 2;
            k = i - 1;
          }
          for (;;)
          {
            i = k + 1;
            m = j;
            localObject = paramCharSequence;
            break label51;
            i = 0;
            break;
            k = i;
            j = m;
            paramCharSequence = (CharSequence)localObject;
            if (((CharSequence)localObject).charAt(i) == 65039)
            {
              paramCharSequence = TextUtils.concat(new CharSequence[] { ((CharSequence)localObject).subSequence(0, i), ((CharSequence)localObject).subSequence(i + 1, ((CharSequence)localObject).length()) });
              j = m - 1;
              k = i - 1;
            }
          }
          this.lastSticker = ((CharSequence)localObject).toString().trim();
          if (Emoji.isValidEmoji(this.lastSticker)) {
            break;
          }
        } while (!this.visible);
        this.visible = false;
        this.delegate.needChangePanelVisibility(false);
        notifyDataSetChanged();
        return;
        this.stickers = null;
        this.stickersMap = null;
        this.delayLocalResults = false;
        final Object localObject = DataQuery.getInstance(this.currentAccount).getRecentStickersNoCopy(0);
        final ArrayList localArrayList = DataQuery.getInstance(this.currentAccount).getRecentStickersNoCopy(2);
        int k = 0;
        int j = 0;
        int m = ((ArrayList)localObject).size();
        for (;;)
        {
          if (j < m)
          {
            paramCharSequence = (TLRPC.Document)((ArrayList)localObject).get(j);
            i = k;
            if (isValidSticker(paramCharSequence, this.lastSticker))
            {
              addStickerToResult(paramCharSequence);
              k += 1;
              i = k;
              if (k < 5) {}
            }
          }
          else
          {
            i = 0;
            j = localArrayList.size();
            while (i < j)
            {
              paramCharSequence = (TLRPC.Document)localArrayList.get(i);
              if (isValidSticker(paramCharSequence, this.lastSticker)) {
                addStickerToResult(paramCharSequence);
              }
              i += 1;
            }
          }
          j += 1;
          k = i;
        }
        paramCharSequence = DataQuery.getInstance(this.currentAccount).getAllStickers();
        if (paramCharSequence != null) {}
        for (paramCharSequence = (ArrayList)paramCharSequence.get(this.lastSticker);; paramCharSequence = null)
        {
          if ((paramCharSequence != null) && (!paramCharSequence.isEmpty()))
          {
            paramCharSequence = new ArrayList(paramCharSequence);
            if (!((ArrayList)localObject).isEmpty()) {
              Collections.sort(paramCharSequence, new Comparator()
              {
                private int getIndex(long paramAnonymousLong)
                {
                  int i = 0;
                  while (i < localArrayList.size())
                  {
                    if (((TLRPC.Document)localArrayList.get(i)).id == paramAnonymousLong) {
                      return i + 1000;
                    }
                    i += 1;
                  }
                  i = 0;
                  while (i < localObject.size())
                  {
                    if (((TLRPC.Document)localObject.get(i)).id == paramAnonymousLong) {
                      return i;
                    }
                    i += 1;
                  }
                  return -1;
                }
                
                public int compare(TLRPC.Document paramAnonymousDocument1, TLRPC.Document paramAnonymousDocument2)
                {
                  int i = getIndex(paramAnonymousDocument1.id);
                  int j = getIndex(paramAnonymousDocument2.id);
                  if (i > j) {
                    return -1;
                  }
                  if (i < j) {
                    return 1;
                  }
                  return 0;
                }
              });
            }
            addStickersToResult(paramCharSequence);
          }
          if (SharedConfig.suggestStickers == 0) {
            searchServerStickers(this.lastSticker);
          }
          if ((this.stickers == null) || (this.stickers.isEmpty())) {
            break label730;
          }
          if ((SharedConfig.suggestStickers != 0) || (this.stickers.size() >= 5)) {
            break;
          }
          this.delayLocalResults = true;
          this.delegate.needChangePanelVisibility(false);
          this.visible = false;
          notifyDataSetChanged();
          return;
        }
        checkStickerFilesExistAndDownload();
        paramCharSequence = this.delegate;
        if ((this.stickers != null) && (!this.stickers.isEmpty()) && (this.stickersToLoad.isEmpty())) {}
        for (boolean bool = true;; bool = false)
        {
          paramCharSequence.needChangePanelVisibility(bool);
          this.visible = true;
          break;
        }
      } while (!this.visible);
      this.delegate.needChangePanelVisibility(false);
      this.visible = false;
      return;
      this.lastSticker = "";
    } while ((!this.visible) || (this.stickers == null));
    this.visible = false;
    this.delegate.needChangePanelVisibility(false);
  }
  
  public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
  {
    int i = 0;
    if (paramInt == 0) {
      if (this.stickers.size() == 1) {
        i = 2;
      }
    }
    for (;;)
    {
      ((StickerCell)paramViewHolder.itemView).setSticker((TLRPC.Document)this.stickers.get(paramInt), i);
      return;
      i = -1;
      continue;
      if (paramInt == this.stickers.size() - 1) {
        i = 1;
      }
    }
  }
  
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    return new RecyclerListView.Holder(new StickerCell(this.mContext));
  }
  
  public void onDestroy()
  {
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidLoaded);
    NotificationCenter.getInstance(this.currentAccount).removeObserver(this, NotificationCenter.FileDidFailedLoad);
  }
  
  public static abstract interface StickersAdapterDelegate
  {
    public abstract void needChangePanelVisibility(boolean paramBoolean);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/StickersAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */