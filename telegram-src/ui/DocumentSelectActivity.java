package org.telegram.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.os.StatFs;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.messenger.support.widget.RecyclerView.OnScrollListener;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog.Builder;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.SharedDocumentCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.RecyclerListView.Holder;
import org.telegram.ui.Components.RecyclerListView.OnItemClickListener;
import org.telegram.ui.Components.RecyclerListView.OnItemLongClickListener;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public class DocumentSelectActivity
  extends BaseFragment
{
  private static final int done = 3;
  private ArrayList<View> actionModeViews = new ArrayList();
  private File currentDir;
  private DocumentSelectActivityDelegate delegate;
  private EmptyTextProgressView emptyView;
  private ArrayList<HistoryEntry> history = new ArrayList();
  private ArrayList<ListItem> items = new ArrayList();
  private LinearLayoutManager layoutManager;
  private ListAdapter listAdapter;
  private RecyclerListView listView;
  private BroadcastReceiver receiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
      paramAnonymousContext = new Runnable()
      {
        public void run()
        {
          try
          {
            if (DocumentSelectActivity.this.currentDir == null)
            {
              DocumentSelectActivity.this.listRoots();
              return;
            }
            DocumentSelectActivity.this.listFiles(DocumentSelectActivity.this.currentDir);
            return;
          }
          catch (Exception localException)
          {
            FileLog.e(localException);
          }
        }
      };
      if ("android.intent.action.MEDIA_UNMOUNTED".equals(paramAnonymousIntent.getAction()))
      {
        DocumentSelectActivity.this.listView.postDelayed(paramAnonymousContext, 1000L);
        return;
      }
      paramAnonymousContext.run();
    }
  };
  private boolean receiverRegistered = false;
  private ArrayList<ListItem> recentItems = new ArrayList();
  private boolean scrolling;
  private HashMap<String, ListItem> selectedFiles = new HashMap();
  private NumberTextView selectedMessagesCountTextView;
  private long sizeLimit = 1610612736L;
  
  private void fixLayoutInternal()
  {
    if (this.selectedMessagesCountTextView == null) {
      return;
    }
    if ((!AndroidUtilities.isTablet()) && (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == 2))
    {
      this.selectedMessagesCountTextView.setTextSize(18);
      return;
    }
    this.selectedMessagesCountTextView.setTextSize(20);
  }
  
  private String getRootSubtitle(String paramString)
  {
    try
    {
      Object localObject = new StatFs(paramString);
      long l1 = ((StatFs)localObject).getBlockCount() * ((StatFs)localObject).getBlockSize();
      long l2 = ((StatFs)localObject).getAvailableBlocks();
      long l3 = ((StatFs)localObject).getBlockSize();
      if (l1 == 0L) {
        return "";
      }
      localObject = LocaleController.formatString("FreeOfTotal", 2131493611, new Object[] { AndroidUtilities.formatFileSize(l2 * l3), AndroidUtilities.formatFileSize(l1) });
      return (String)localObject;
    }
    catch (Exception localException)
    {
      FileLog.e(localException);
    }
    return paramString;
  }
  
  private boolean listFiles(File paramFile)
  {
    if (!paramFile.canRead())
    {
      if (((paramFile.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().toString())) || (paramFile.getAbsolutePath().startsWith("/sdcard")) || (paramFile.getAbsolutePath().startsWith("/mnt/sdcard"))) && (!Environment.getExternalStorageState().equals("mounted")) && (!Environment.getExternalStorageState().equals("mounted_ro")))
      {
        this.currentDir = paramFile;
        this.items.clear();
        if ("shared".equals(Environment.getExternalStorageState())) {
          this.emptyView.setText(LocaleController.getString("UsbActive", 2131494529));
        }
        for (;;)
        {
          AndroidUtilities.clearDrawableAnimation(this.listView);
          this.scrolling = true;
          this.listAdapter.notifyDataSetChanged();
          return true;
          this.emptyView.setText(LocaleController.getString("NotMounted", 2131493919));
        }
      }
      showErrorBox(LocaleController.getString("AccessError", 2131492866));
      return false;
    }
    Object localObject;
    try
    {
      localObject = paramFile.listFiles();
      if (localObject == null)
      {
        showErrorBox(LocaleController.getString("UnknownError", 2131494509));
        return false;
      }
    }
    catch (Exception paramFile)
    {
      showErrorBox(paramFile.getLocalizedMessage());
      return false;
    }
    this.currentDir = paramFile;
    this.items.clear();
    Arrays.sort((Object[])localObject, new Comparator()
    {
      public int compare(File paramAnonymousFile1, File paramAnonymousFile2)
      {
        if (paramAnonymousFile1.isDirectory() != paramAnonymousFile2.isDirectory())
        {
          if (paramAnonymousFile1.isDirectory()) {
            return -1;
          }
          return 1;
        }
        return paramAnonymousFile1.getName().compareToIgnoreCase(paramAnonymousFile2.getName());
      }
    });
    int i = 0;
    if (i < localObject.length)
    {
      File localFile = localObject[i];
      if (localFile.getName().indexOf('.') == 0) {}
      ListItem localListItem;
      for (;;)
      {
        i += 1;
        break;
        localListItem = new ListItem(null);
        localListItem.title = localFile.getName();
        localListItem.file = localFile;
        if (!localFile.isDirectory()) {
          break label331;
        }
        localListItem.icon = 2131165375;
        localListItem.subtitle = LocaleController.getString("Folder", 2131493545);
        this.items.add(localListItem);
      }
      label331:
      String str = localFile.getName();
      paramFile = str.split("\\.");
      if (paramFile.length > 1) {}
      for (paramFile = paramFile[(paramFile.length - 1)];; paramFile = "?")
      {
        localListItem.ext = paramFile;
        localListItem.subtitle = AndroidUtilities.formatFileSize(localFile.length());
        paramFile = str.toLowerCase();
        if ((!paramFile.endsWith(".jpg")) && (!paramFile.endsWith(".png")) && (!paramFile.endsWith(".gif")) && (!paramFile.endsWith(".jpeg"))) {
          break;
        }
        localListItem.thumb = localFile.getAbsolutePath();
        break;
      }
    }
    paramFile = new ListItem(null);
    paramFile.title = "..";
    if (this.history.size() > 0)
    {
      localObject = (HistoryEntry)this.history.get(this.history.size() - 1);
      if (((HistoryEntry)localObject).dir == null) {
        paramFile.subtitle = LocaleController.getString("Folder", 2131493545);
      }
    }
    for (;;)
    {
      paramFile.icon = 2131165375;
      paramFile.file = null;
      this.items.add(0, paramFile);
      AndroidUtilities.clearDrawableAnimation(this.listView);
      this.scrolling = true;
      this.listAdapter.notifyDataSetChanged();
      return true;
      paramFile.subtitle = ((HistoryEntry)localObject).dir.toString();
      continue;
      paramFile.subtitle = LocaleController.getString("Folder", 2131493545);
    }
  }
  
  /* Error */
  @android.annotation.SuppressLint({"NewApi"})
  private void listRoots()
  {
    // Byte code:
    //   0: aload_0
    //   1: aconst_null
    //   2: putfield 107	org/telegram/ui/DocumentSelectActivity:currentDir	Ljava/io/File;
    //   5: aload_0
    //   6: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   9: invokevirtual 295	java/util/ArrayList:clear	()V
    //   12: new 425	java/util/HashSet
    //   15: dup
    //   16: invokespecial 426	java/util/HashSet:<init>	()V
    //   19: astore 6
    //   21: invokestatic 268	android/os/Environment:getExternalStorageDirectory	()Ljava/io/File;
    //   24: invokevirtual 429	java/io/File:getPath	()Ljava/lang/String;
    //   27: astore_2
    //   28: invokestatic 432	android/os/Environment:isExternalStorageRemovable	()Z
    //   31: pop
    //   32: invokestatic 284	android/os/Environment:getExternalStorageState	()Ljava/lang/String;
    //   35: astore_3
    //   36: aload_3
    //   37: ldc_w 286
    //   40: invokevirtual 290	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   43: ifne +13 -> 56
    //   46: aload_3
    //   47: ldc_w 292
    //   50: invokevirtual 290	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   53: ifeq +71 -> 124
    //   56: new 35	org/telegram/ui/DocumentSelectActivity$ListItem
    //   59: dup
    //   60: aload_0
    //   61: aconst_null
    //   62: invokespecial 350	org/telegram/ui/DocumentSelectActivity$ListItem:<init>	(Lorg/telegram/ui/DocumentSelectActivity;Lorg/telegram/ui/DocumentSelectActivity$1;)V
    //   65: astore_3
    //   66: invokestatic 432	android/os/Environment:isExternalStorageRemovable	()Z
    //   69: ifeq +615 -> 684
    //   72: aload_3
    //   73: ldc_w 434
    //   76: ldc_w 435
    //   79: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   82: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   85: aload_3
    //   86: ldc_w 436
    //   89: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   92: aload_3
    //   93: aload_0
    //   94: aload_2
    //   95: invokespecial 438	org/telegram/ui/DocumentSelectActivity:getRootSubtitle	(Ljava/lang/String;)Ljava/lang/String;
    //   98: putfield 370	org/telegram/ui/DocumentSelectActivity$ListItem:subtitle	Ljava/lang/String;
    //   101: aload_3
    //   102: invokestatic 268	android/os/Environment:getExternalStorageDirectory	()Ljava/io/File;
    //   105: putfield 357	org/telegram/ui/DocumentSelectActivity$ListItem:file	Ljava/io/File;
    //   108: aload_0
    //   109: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   112: aload_3
    //   113: invokevirtual 373	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   116: pop
    //   117: aload 6
    //   119: aload_2
    //   120: invokevirtual 439	java/util/HashSet:add	(Ljava/lang/Object;)Z
    //   123: pop
    //   124: aconst_null
    //   125: astore_2
    //   126: aconst_null
    //   127: astore 4
    //   129: new 441	java/io/BufferedReader
    //   132: dup
    //   133: new 443	java/io/FileReader
    //   136: dup
    //   137: ldc_w 445
    //   140: invokespecial 446	java/io/FileReader:<init>	(Ljava/lang/String;)V
    //   143: invokespecial 449	java/io/BufferedReader:<init>	(Ljava/io/Reader;)V
    //   146: astore_3
    //   147: aload_3
    //   148: invokevirtual 452	java/io/BufferedReader:readLine	()Ljava/lang/String;
    //   151: astore_2
    //   152: aload_2
    //   153: ifnull +582 -> 735
    //   156: aload_2
    //   157: ldc_w 454
    //   160: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   163: ifne +13 -> 176
    //   166: aload_2
    //   167: ldc_w 460
    //   170: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   173: ifeq -26 -> 147
    //   176: getstatic 465	org/telegram/messenger/BuildVars:LOGS_ENABLED	Z
    //   179: ifeq +7 -> 186
    //   182: aload_2
    //   183: invokestatic 468	org/telegram/messenger/FileLog:d	(Ljava/lang/String;)V
    //   186: new 470	java/util/StringTokenizer
    //   189: dup
    //   190: aload_2
    //   191: ldc_w 472
    //   194: invokespecial 475	java/util/StringTokenizer:<init>	(Ljava/lang/String;Ljava/lang/String;)V
    //   197: astore 4
    //   199: aload 4
    //   201: invokevirtual 478	java/util/StringTokenizer:nextToken	()Ljava/lang/String;
    //   204: pop
    //   205: aload 4
    //   207: invokevirtual 478	java/util/StringTokenizer:nextToken	()Ljava/lang/String;
    //   210: astore 4
    //   212: aload 6
    //   214: aload 4
    //   216: invokevirtual 480	java/util/HashSet:contains	(Ljava/lang/Object;)Z
    //   219: ifne -72 -> 147
    //   222: aload_2
    //   223: ldc_w 482
    //   226: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   229: ifeq -82 -> 147
    //   232: aload_2
    //   233: ldc_w 484
    //   236: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   239: ifne -92 -> 147
    //   242: aload_2
    //   243: ldc_w 486
    //   246: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   249: ifne -102 -> 147
    //   252: aload_2
    //   253: ldc_w 488
    //   256: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   259: ifne -112 -> 147
    //   262: aload_2
    //   263: ldc_w 490
    //   266: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   269: ifne -122 -> 147
    //   272: aload_2
    //   273: ldc_w 492
    //   276: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   279: ifne -132 -> 147
    //   282: aload 4
    //   284: astore_2
    //   285: new 255	java/io/File
    //   288: dup
    //   289: aload 4
    //   291: invokespecial 493	java/io/File:<init>	(Ljava/lang/String;)V
    //   294: invokevirtual 360	java/io/File:isDirectory	()Z
    //   297: ifne +69 -> 366
    //   300: aload 4
    //   302: bipush 47
    //   304: invokevirtual 496	java/lang/String:lastIndexOf	(I)I
    //   307: istore_1
    //   308: aload 4
    //   310: astore_2
    //   311: iload_1
    //   312: iconst_m1
    //   313: if_icmpeq +53 -> 366
    //   316: new 498	java/lang/StringBuilder
    //   319: dup
    //   320: invokespecial 499	java/lang/StringBuilder:<init>	()V
    //   323: ldc_w 501
    //   326: invokevirtual 505	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   329: aload 4
    //   331: iload_1
    //   332: iconst_1
    //   333: iadd
    //   334: invokevirtual 509	java/lang/String:substring	(I)Ljava/lang/String;
    //   337: invokevirtual 505	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   340: invokevirtual 510	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   343: astore 5
    //   345: aload 4
    //   347: astore_2
    //   348: new 255	java/io/File
    //   351: dup
    //   352: aload 5
    //   354: invokespecial 493	java/io/File:<init>	(Ljava/lang/String;)V
    //   357: invokevirtual 360	java/io/File:isDirectory	()Z
    //   360: ifeq +6 -> 366
    //   363: aload 5
    //   365: astore_2
    //   366: aload 6
    //   368: aload_2
    //   369: invokevirtual 439	java/util/HashSet:add	(Ljava/lang/Object;)Z
    //   372: pop
    //   373: new 35	org/telegram/ui/DocumentSelectActivity$ListItem
    //   376: dup
    //   377: aload_0
    //   378: aconst_null
    //   379: invokespecial 350	org/telegram/ui/DocumentSelectActivity$ListItem:<init>	(Lorg/telegram/ui/DocumentSelectActivity;Lorg/telegram/ui/DocumentSelectActivity$1;)V
    //   382: astore 4
    //   384: aload_2
    //   385: invokevirtual 389	java/lang/String:toLowerCase	()Ljava/lang/String;
    //   388: ldc_w 512
    //   391: invokevirtual 458	java/lang/String:contains	(Ljava/lang/CharSequence;)Z
    //   394: ifeq +313 -> 707
    //   397: aload 4
    //   399: ldc_w 434
    //   402: ldc_w 435
    //   405: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   408: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   411: aload 4
    //   413: ldc_w 436
    //   416: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   419: aload 4
    //   421: aload_0
    //   422: aload_2
    //   423: invokespecial 438	org/telegram/ui/DocumentSelectActivity:getRootSubtitle	(Ljava/lang/String;)Ljava/lang/String;
    //   426: putfield 370	org/telegram/ui/DocumentSelectActivity$ListItem:subtitle	Ljava/lang/String;
    //   429: aload 4
    //   431: new 255	java/io/File
    //   434: dup
    //   435: aload_2
    //   436: invokespecial 493	java/io/File:<init>	(Ljava/lang/String;)V
    //   439: putfield 357	org/telegram/ui/DocumentSelectActivity$ListItem:file	Ljava/io/File;
    //   442: aload_0
    //   443: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   446: aload 4
    //   448: invokevirtual 373	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   451: pop
    //   452: goto -305 -> 147
    //   455: astore_2
    //   456: aload_2
    //   457: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   460: goto -313 -> 147
    //   463: astore 4
    //   465: aload_3
    //   466: astore_2
    //   467: aload 4
    //   469: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   472: aload_3
    //   473: ifnull +7 -> 480
    //   476: aload_3
    //   477: invokevirtual 515	java/io/BufferedReader:close	()V
    //   480: new 35	org/telegram/ui/DocumentSelectActivity$ListItem
    //   483: dup
    //   484: aload_0
    //   485: aconst_null
    //   486: invokespecial 350	org/telegram/ui/DocumentSelectActivity$ListItem:<init>	(Lorg/telegram/ui/DocumentSelectActivity;Lorg/telegram/ui/DocumentSelectActivity$1;)V
    //   489: astore_2
    //   490: aload_2
    //   491: ldc_w 517
    //   494: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   497: aload_2
    //   498: ldc_w 519
    //   501: ldc_w 520
    //   504: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   507: putfield 370	org/telegram/ui/DocumentSelectActivity$ListItem:subtitle	Ljava/lang/String;
    //   510: aload_2
    //   511: ldc_w 361
    //   514: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   517: aload_2
    //   518: new 255	java/io/File
    //   521: dup
    //   522: ldc_w 517
    //   525: invokespecial 493	java/io/File:<init>	(Ljava/lang/String;)V
    //   528: putfield 357	org/telegram/ui/DocumentSelectActivity$ListItem:file	Ljava/io/File;
    //   531: aload_0
    //   532: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   535: aload_2
    //   536: invokevirtual 373	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   539: pop
    //   540: new 255	java/io/File
    //   543: dup
    //   544: invokestatic 268	android/os/Environment:getExternalStorageDirectory	()Ljava/io/File;
    //   547: ldc_w 522
    //   550: invokespecial 525	java/io/File:<init>	(Ljava/io/File;Ljava/lang/String;)V
    //   553: astore_2
    //   554: aload_2
    //   555: invokevirtual 528	java/io/File:exists	()Z
    //   558: ifeq +49 -> 607
    //   561: new 35	org/telegram/ui/DocumentSelectActivity$ListItem
    //   564: dup
    //   565: aload_0
    //   566: aconst_null
    //   567: invokespecial 350	org/telegram/ui/DocumentSelectActivity$ListItem:<init>	(Lorg/telegram/ui/DocumentSelectActivity;Lorg/telegram/ui/DocumentSelectActivity$1;)V
    //   570: astore_3
    //   571: aload_3
    //   572: ldc_w 522
    //   575: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   578: aload_3
    //   579: aload_2
    //   580: invokevirtual 271	java/io/File:toString	()Ljava/lang/String;
    //   583: putfield 370	org/telegram/ui/DocumentSelectActivity$ListItem:subtitle	Ljava/lang/String;
    //   586: aload_3
    //   587: ldc_w 361
    //   590: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   593: aload_3
    //   594: aload_2
    //   595: putfield 357	org/telegram/ui/DocumentSelectActivity$ListItem:file	Ljava/io/File;
    //   598: aload_0
    //   599: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   602: aload_3
    //   603: invokevirtual 373	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   606: pop
    //   607: new 35	org/telegram/ui/DocumentSelectActivity$ListItem
    //   610: dup
    //   611: aload_0
    //   612: aconst_null
    //   613: invokespecial 350	org/telegram/ui/DocumentSelectActivity$ListItem:<init>	(Lorg/telegram/ui/DocumentSelectActivity;Lorg/telegram/ui/DocumentSelectActivity$1;)V
    //   616: astore_2
    //   617: aload_2
    //   618: ldc_w 530
    //   621: ldc_w 531
    //   624: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   627: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   630: aload_2
    //   631: ldc_w 533
    //   634: ldc_w 534
    //   637: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   640: putfield 370	org/telegram/ui/DocumentSelectActivity$ListItem:subtitle	Ljava/lang/String;
    //   643: aload_2
    //   644: ldc_w 535
    //   647: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   650: aload_2
    //   651: aconst_null
    //   652: putfield 357	org/telegram/ui/DocumentSelectActivity$ListItem:file	Ljava/io/File;
    //   655: aload_0
    //   656: getfield 80	org/telegram/ui/DocumentSelectActivity:items	Ljava/util/ArrayList;
    //   659: aload_2
    //   660: invokevirtual 373	java/util/ArrayList:add	(Ljava/lang/Object;)Z
    //   663: pop
    //   664: aload_0
    //   665: getfield 162	org/telegram/ui/DocumentSelectActivity:listView	Lorg/telegram/ui/Components/RecyclerListView;
    //   668: invokestatic 315	org/telegram/messenger/AndroidUtilities:clearDrawableAnimation	(Landroid/view/View;)V
    //   671: aload_0
    //   672: iconst_1
    //   673: putfield 174	org/telegram/ui/DocumentSelectActivity:scrolling	Z
    //   676: aload_0
    //   677: getfield 115	org/telegram/ui/DocumentSelectActivity:listAdapter	Lorg/telegram/ui/DocumentSelectActivity$ListAdapter;
    //   680: invokevirtual 318	org/telegram/ui/DocumentSelectActivity$ListAdapter:notifyDataSetChanged	()V
    //   683: return
    //   684: aload_3
    //   685: ldc_w 537
    //   688: ldc_w 538
    //   691: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   694: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   697: aload_3
    //   698: ldc_w 539
    //   701: putfield 364	org/telegram/ui/DocumentSelectActivity$ListItem:icon	I
    //   704: goto -612 -> 92
    //   707: aload 4
    //   709: ldc_w 541
    //   712: ldc_w 542
    //   715: invokestatic 306	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   718: putfield 354	org/telegram/ui/DocumentSelectActivity$ListItem:title	Ljava/lang/String;
    //   721: goto -310 -> 411
    //   724: astore_2
    //   725: aload_3
    //   726: ifnull +7 -> 733
    //   729: aload_3
    //   730: invokevirtual 515	java/io/BufferedReader:close	()V
    //   733: aload_2
    //   734: athrow
    //   735: aload_3
    //   736: ifnull +66 -> 802
    //   739: aload_3
    //   740: invokevirtual 515	java/io/BufferedReader:close	()V
    //   743: goto -263 -> 480
    //   746: astore_2
    //   747: aload_2
    //   748: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   751: goto -271 -> 480
    //   754: astore_2
    //   755: aload_2
    //   756: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   759: goto -279 -> 480
    //   762: astore_3
    //   763: aload_3
    //   764: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   767: goto -34 -> 733
    //   770: astore_2
    //   771: aload_2
    //   772: invokestatic 253	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   775: goto -168 -> 607
    //   778: astore_2
    //   779: goto -8 -> 771
    //   782: astore 4
    //   784: aload_2
    //   785: astore_3
    //   786: aload 4
    //   788: astore_2
    //   789: goto -64 -> 725
    //   792: astore_2
    //   793: aload 4
    //   795: astore_3
    //   796: aload_2
    //   797: astore 4
    //   799: goto -334 -> 465
    //   802: goto -322 -> 480
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	805	0	this	DocumentSelectActivity
    //   307	27	1	i	int
    //   27	409	2	localObject1	Object
    //   455	2	2	localException1	Exception
    //   466	194	2	localObject2	Object
    //   724	10	2	localObject3	Object
    //   746	2	2	localException2	Exception
    //   754	2	2	localException3	Exception
    //   770	2	2	localException4	Exception
    //   778	7	2	localException5	Exception
    //   788	1	2	localObject4	Object
    //   792	5	2	localException6	Exception
    //   35	705	3	localObject5	Object
    //   762	2	3	localException7	Exception
    //   785	11	3	localObject6	Object
    //   127	320	4	localObject7	Object
    //   463	245	4	localException8	Exception
    //   782	12	4	localObject8	Object
    //   797	1	4	localException9	Exception
    //   343	21	5	str	String
    //   19	348	6	localHashSet	java.util.HashSet
    // Exception table:
    //   from	to	target	type
    //   373	411	455	java/lang/Exception
    //   411	452	455	java/lang/Exception
    //   707	721	455	java/lang/Exception
    //   147	152	463	java/lang/Exception
    //   156	176	463	java/lang/Exception
    //   176	186	463	java/lang/Exception
    //   186	282	463	java/lang/Exception
    //   285	308	463	java/lang/Exception
    //   316	345	463	java/lang/Exception
    //   348	363	463	java/lang/Exception
    //   366	373	463	java/lang/Exception
    //   456	460	463	java/lang/Exception
    //   147	152	724	finally
    //   156	176	724	finally
    //   176	186	724	finally
    //   186	282	724	finally
    //   285	308	724	finally
    //   316	345	724	finally
    //   348	363	724	finally
    //   366	373	724	finally
    //   373	411	724	finally
    //   411	452	724	finally
    //   456	460	724	finally
    //   707	721	724	finally
    //   739	743	746	java/lang/Exception
    //   476	480	754	java/lang/Exception
    //   729	733	762	java/lang/Exception
    //   540	571	770	java/lang/Exception
    //   571	607	778	java/lang/Exception
    //   129	147	782	finally
    //   467	472	782	finally
    //   129	147	792	java/lang/Exception
  }
  
  private void showErrorBox(String paramString)
  {
    if (getParentActivity() == null) {
      return;
    }
    new AlertDialog.Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", 2131492981)).setMessage(paramString).setPositiveButton(LocaleController.getString("OK", 2131494028), null).show();
  }
  
  public View createView(Context paramContext)
  {
    if (!this.receiverRegistered)
    {
      this.receiverRegistered = true;
      localObject = new IntentFilter();
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_BAD_REMOVAL");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_CHECKING");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_EJECT");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_MOUNTED");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_NOFS");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_REMOVED");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_SHARED");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_UNMOUNTABLE");
      ((IntentFilter)localObject).addAction("android.intent.action.MEDIA_UNMOUNTED");
      ((IntentFilter)localObject).addDataScheme("file");
      ApplicationLoader.applicationContext.registerReceiver(this.receiver, (IntentFilter)localObject);
    }
    this.actionBar.setBackButtonDrawable(new BackDrawable(false));
    this.actionBar.setAllowOverlayTitle(true);
    this.actionBar.setTitle(LocaleController.getString("SelectFile", 2131494330));
    this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
    {
      public void onItemClick(int paramAnonymousInt)
      {
        Object localObject;
        if (paramAnonymousInt == -1)
        {
          if (DocumentSelectActivity.this.actionBar.isActionModeShowed())
          {
            DocumentSelectActivity.this.selectedFiles.clear();
            DocumentSelectActivity.this.actionBar.hideActionMode();
            int i = DocumentSelectActivity.this.listView.getChildCount();
            paramAnonymousInt = 0;
            while (paramAnonymousInt < i)
            {
              localObject = DocumentSelectActivity.this.listView.getChildAt(paramAnonymousInt);
              if ((localObject instanceof SharedDocumentCell)) {
                ((SharedDocumentCell)localObject).setChecked(false, true);
              }
              paramAnonymousInt += 1;
            }
          }
          DocumentSelectActivity.this.finishFragment();
        }
        for (;;)
        {
          return;
          if ((paramAnonymousInt == 3) && (DocumentSelectActivity.this.delegate != null))
          {
            localObject = new ArrayList();
            ((ArrayList)localObject).addAll(DocumentSelectActivity.this.selectedFiles.keySet());
            DocumentSelectActivity.this.delegate.didSelectFiles(DocumentSelectActivity.this, (ArrayList)localObject);
            localObject = DocumentSelectActivity.this.selectedFiles.values().iterator();
            while (((Iterator)localObject).hasNext()) {
              ((DocumentSelectActivity.ListItem)((Iterator)localObject).next()).date = System.currentTimeMillis();
            }
          }
        }
      }
    });
    this.selectedFiles.clear();
    this.actionModeViews.clear();
    Object localObject = this.actionBar.createActionMode();
    this.selectedMessagesCountTextView = new NumberTextView(((ActionBarMenu)localObject).getContext());
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
    ((ActionBarMenu)localObject).addView(this.selectedMessagesCountTextView, LayoutHelper.createLinear(0, -1, 1.0F, 65, 0, 0, 0));
    this.actionModeViews.add(((ActionBarMenu)localObject).addItemWithWidth(3, 2131165349, AndroidUtilities.dp(54.0F)));
    this.fragmentView = new FrameLayout(paramContext);
    localObject = (FrameLayout)this.fragmentView;
    this.emptyView = new EmptyTextProgressView(paramContext);
    this.emptyView.showTextView();
    ((FrameLayout)localObject).addView(this.emptyView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView = new RecyclerListView(paramContext);
    this.listView.setVerticalScrollBarEnabled(false);
    RecyclerListView localRecyclerListView = this.listView;
    LinearLayoutManager localLinearLayoutManager = new LinearLayoutManager(paramContext, 1, false);
    this.layoutManager = localLinearLayoutManager;
    localRecyclerListView.setLayoutManager(localLinearLayoutManager);
    this.listView.setEmptyView(this.emptyView);
    localRecyclerListView = this.listView;
    paramContext = new ListAdapter(paramContext);
    this.listAdapter = paramContext;
    localRecyclerListView.setAdapter(paramContext);
    ((FrameLayout)localObject).addView(this.listView, LayoutHelper.createFrame(-1, -1.0F));
    this.listView.setOnScrollListener(new RecyclerView.OnScrollListener()
    {
      public void onScrollStateChanged(RecyclerView paramAnonymousRecyclerView, int paramAnonymousInt)
      {
        paramAnonymousRecyclerView = DocumentSelectActivity.this;
        if (paramAnonymousInt != 0) {}
        for (boolean bool = true;; bool = false)
        {
          DocumentSelectActivity.access$802(paramAnonymousRecyclerView, bool);
          return;
        }
      }
    });
    this.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener()
    {
      public boolean onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        if (DocumentSelectActivity.this.actionBar.isActionModeShowed()) {
          return false;
        }
        Object localObject1 = DocumentSelectActivity.this.listAdapter.getItem(paramAnonymousInt);
        if (localObject1 == null) {
          return false;
        }
        Object localObject2 = ((DocumentSelectActivity.ListItem)localObject1).file;
        if ((localObject2 != null) && (!((File)localObject2).isDirectory()))
        {
          if (!((File)localObject2).canRead())
          {
            DocumentSelectActivity.this.showErrorBox(LocaleController.getString("AccessError", 2131492866));
            return false;
          }
          if ((DocumentSelectActivity.this.sizeLimit != 0L) && (((File)localObject2).length() > DocumentSelectActivity.this.sizeLimit))
          {
            DocumentSelectActivity.this.showErrorBox(LocaleController.formatString("FileUploadLimit", 2131493537, new Object[] { AndroidUtilities.formatFileSize(DocumentSelectActivity.this.sizeLimit) }));
            return false;
          }
          if (((File)localObject2).length() == 0L) {
            return false;
          }
          DocumentSelectActivity.this.selectedFiles.put(((File)localObject2).toString(), localObject1);
          DocumentSelectActivity.this.selectedMessagesCountTextView.setNumber(1, false);
          localObject1 = new AnimatorSet();
          localObject2 = new ArrayList();
          paramAnonymousInt = 0;
          while (paramAnonymousInt < DocumentSelectActivity.this.actionModeViews.size())
          {
            View localView = (View)DocumentSelectActivity.this.actionModeViews.get(paramAnonymousInt);
            AndroidUtilities.clearDrawableAnimation(localView);
            ((ArrayList)localObject2).add(ObjectAnimator.ofFloat(localView, "scaleY", new float[] { 0.1F, 1.0F }));
            paramAnonymousInt += 1;
          }
          ((AnimatorSet)localObject1).playTogether((Collection)localObject2);
          ((AnimatorSet)localObject1).setDuration(250L);
          ((AnimatorSet)localObject1).start();
          DocumentSelectActivity.access$802(DocumentSelectActivity.this, false);
          if ((paramAnonymousView instanceof SharedDocumentCell)) {
            ((SharedDocumentCell)paramAnonymousView).setChecked(true, true);
          }
          DocumentSelectActivity.this.actionBar.showActionMode();
        }
        return true;
      }
    });
    this.listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener()
    {
      public void onItemClick(View paramAnonymousView, int paramAnonymousInt)
      {
        DocumentSelectActivity.ListItem localListItem = DocumentSelectActivity.this.listAdapter.getItem(paramAnonymousInt);
        if (localListItem == null) {}
        Object localObject;
        label448:
        label534:
        do
        {
          do
          {
            return;
            File localFile = localListItem.file;
            if (localFile == null)
            {
              if (localListItem.icon == 2131165424)
              {
                if (DocumentSelectActivity.this.delegate != null) {
                  DocumentSelectActivity.this.delegate.startDocumentSelectActivity();
                }
                DocumentSelectActivity.this.finishFragment(false);
                return;
              }
              paramAnonymousView = (DocumentSelectActivity.HistoryEntry)DocumentSelectActivity.this.history.remove(DocumentSelectActivity.this.history.size() - 1);
              DocumentSelectActivity.this.actionBar.setTitle(paramAnonymousView.title);
              if (paramAnonymousView.dir != null) {
                DocumentSelectActivity.this.listFiles(paramAnonymousView.dir);
              }
              for (;;)
              {
                DocumentSelectActivity.this.layoutManager.scrollToPositionWithOffset(paramAnonymousView.scrollItem, paramAnonymousView.scrollOffset);
                return;
                DocumentSelectActivity.this.listRoots();
              }
            }
            if (localFile.isDirectory())
            {
              paramAnonymousView = new DocumentSelectActivity.HistoryEntry(DocumentSelectActivity.this, null);
              paramAnonymousView.scrollItem = DocumentSelectActivity.this.layoutManager.findLastVisibleItemPosition();
              localObject = DocumentSelectActivity.this.layoutManager.findViewByPosition(paramAnonymousView.scrollItem);
              if (localObject != null) {
                paramAnonymousView.scrollOffset = ((View)localObject).getTop();
              }
              paramAnonymousView.dir = DocumentSelectActivity.this.currentDir;
              paramAnonymousView.title = DocumentSelectActivity.this.actionBar.getTitle();
              DocumentSelectActivity.this.history.add(paramAnonymousView);
              if (!DocumentSelectActivity.this.listFiles(localFile))
              {
                DocumentSelectActivity.this.history.remove(paramAnonymousView);
                return;
              }
              DocumentSelectActivity.this.actionBar.setTitle(localListItem.title);
              return;
            }
            localObject = localFile;
            if (!localFile.canRead())
            {
              DocumentSelectActivity.this.showErrorBox(LocaleController.getString("AccessError", 2131492866));
              localObject = new File("/mnt/sdcard");
            }
            if ((DocumentSelectActivity.this.sizeLimit != 0L) && (((File)localObject).length() > DocumentSelectActivity.this.sizeLimit))
            {
              DocumentSelectActivity.this.showErrorBox(LocaleController.formatString("FileUploadLimit", 2131493537, new Object[] { AndroidUtilities.formatFileSize(DocumentSelectActivity.this.sizeLimit) }));
              return;
            }
          } while (((File)localObject).length() == 0L);
          if (DocumentSelectActivity.this.actionBar.isActionModeShowed())
          {
            if (DocumentSelectActivity.this.selectedFiles.containsKey(((File)localObject).toString()))
            {
              DocumentSelectActivity.this.selectedFiles.remove(((File)localObject).toString());
              if (!DocumentSelectActivity.this.selectedFiles.isEmpty()) {
                break label534;
              }
              DocumentSelectActivity.this.actionBar.hideActionMode();
            }
            for (;;)
            {
              DocumentSelectActivity.access$802(DocumentSelectActivity.this, false);
              if (!(paramAnonymousView instanceof SharedDocumentCell)) {
                break;
              }
              ((SharedDocumentCell)paramAnonymousView).setChecked(DocumentSelectActivity.this.selectedFiles.containsKey(localListItem.file.toString()), true);
              return;
              DocumentSelectActivity.this.selectedFiles.put(((File)localObject).toString(), localListItem);
              break label448;
              DocumentSelectActivity.this.selectedMessagesCountTextView.setNumber(DocumentSelectActivity.this.selectedFiles.size(), true);
            }
          }
        } while (DocumentSelectActivity.this.delegate == null);
        paramAnonymousView = new ArrayList();
        paramAnonymousView.add(((File)localObject).getAbsolutePath());
        DocumentSelectActivity.this.delegate.didSelectFiles(DocumentSelectActivity.this, paramAnonymousView);
      }
    });
    listRoots();
    return this.fragmentView;
  }
  
  public ThemeDescription[] getThemeDescriptions()
  {
    return new ThemeDescription[] { new ThemeDescription(this.fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "windowBackgroundWhite"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, "actionBarDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, "actionBarDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, "actionBarDefaultTitle"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, "actionBarDefaultSelector"), new ThemeDescription(this.listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, "listSelectorSDK21"), new ThemeDescription(this.emptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "emptyListPlaceholder"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, null, null, null, null, "actionBarActionModeDefault"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, null, null, null, null, "actionBarActionModeDefaultTop"), new ThemeDescription(this.actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, null, null, null, null, "actionBarActionModeDefaultSelector"), new ThemeDescription(this.selectedMessagesCountTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, "actionBarActionModeDefaultIcon"), new ThemeDescription(this.listView, 0, new Class[] { GraySectionCell.class }, new String[] { "textView" }, null, null, null, "windowBackgroundWhiteGrayText2"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[] { GraySectionCell.class }, null, null, null, "graySection"), new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "nameTextView" }, null, null, null, "windowBackgroundWhiteBlackText"), new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "dateTextView" }, null, null, null, "windowBackgroundWhiteGrayText3"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOX, new Class[] { SharedDocumentCell.class }, new String[] { "checkBox" }, null, null, null, "checkbox"), new ThemeDescription(this.listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[] { SharedDocumentCell.class }, new String[] { "checkBox" }, null, null, null, "checkboxCheck"), new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "thumbImageView" }, null, null, null, "files_folderIcon"), new ThemeDescription(this.listView, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[] { SharedDocumentCell.class }, new String[] { "thumbImageView" }, null, null, null, "files_folderIconBackground"), new ThemeDescription(this.listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[] { SharedDocumentCell.class }, new String[] { "extTextView" }, null, null, null, "files_iconText") };
  }
  
  public void loadRecentFiles()
  {
    for (;;)
    {
      int i;
      try
      {
        File[] arrayOfFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
        i = 0;
        if (i >= arrayOfFile.length) {
          break label192;
        }
        File localFile = arrayOfFile[i];
        if (localFile.isDirectory()) {
          break label208;
        }
        ListItem localListItem = new ListItem(null);
        localListItem.title = localFile.getName();
        localListItem.file = localFile;
        String str2 = localFile.getName();
        Object localObject = str2.split("\\.");
        if (localObject.length > 1)
        {
          localObject = localObject[(localObject.length - 1)];
          localListItem.ext = ((String)localObject);
          localListItem.subtitle = AndroidUtilities.formatFileSize(localFile.length());
          localObject = str2.toLowerCase();
          if ((((String)localObject).endsWith(".jpg")) || (((String)localObject).endsWith(".png")) || (((String)localObject).endsWith(".gif")) || (((String)localObject).endsWith(".jpeg"))) {
            localListItem.thumb = localFile.getAbsolutePath();
          }
          this.recentItems.add(localListItem);
        }
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        return;
      }
      String str1 = "?";
      continue;
      label192:
      Collections.sort(this.recentItems, new Comparator()
      {
        public int compare(DocumentSelectActivity.ListItem paramAnonymousListItem1, DocumentSelectActivity.ListItem paramAnonymousListItem2)
        {
          long l1 = paramAnonymousListItem1.file.lastModified();
          long l2 = paramAnonymousListItem2.file.lastModified();
          if (l1 == l2) {
            return 0;
          }
          if (l1 > l2) {
            return -1;
          }
          return 1;
        }
      });
      return;
      label208:
      i += 1;
    }
  }
  
  public boolean onBackPressed()
  {
    if (this.history.size() > 0)
    {
      HistoryEntry localHistoryEntry = (HistoryEntry)this.history.remove(this.history.size() - 1);
      this.actionBar.setTitle(localHistoryEntry.title);
      if (localHistoryEntry.dir != null) {
        listFiles(localHistoryEntry.dir);
      }
      for (;;)
      {
        this.layoutManager.scrollToPositionWithOffset(localHistoryEntry.scrollItem, localHistoryEntry.scrollOffset);
        return false;
        listRoots();
      }
    }
    return super.onBackPressed();
  }
  
  public void onConfigurationChanged(Configuration paramConfiguration)
  {
    super.onConfigurationChanged(paramConfiguration);
    if (this.listView != null) {
      this.listView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
      {
        public boolean onPreDraw()
        {
          DocumentSelectActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
          DocumentSelectActivity.this.fixLayoutInternal();
          return true;
        }
      });
    }
  }
  
  public boolean onFragmentCreate()
  {
    loadRecentFiles();
    return super.onFragmentCreate();
  }
  
  public void onFragmentDestroy()
  {
    try
    {
      if (this.receiverRegistered) {
        ApplicationLoader.applicationContext.unregisterReceiver(this.receiver);
      }
      super.onFragmentDestroy();
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
  
  public void onResume()
  {
    super.onResume();
    if (this.listAdapter != null) {
      this.listAdapter.notifyDataSetChanged();
    }
    fixLayoutInternal();
  }
  
  public void setDelegate(DocumentSelectActivityDelegate paramDocumentSelectActivityDelegate)
  {
    this.delegate = paramDocumentSelectActivityDelegate;
  }
  
  public static abstract interface DocumentSelectActivityDelegate
  {
    public abstract void didSelectFiles(DocumentSelectActivity paramDocumentSelectActivity, ArrayList<String> paramArrayList);
    
    public abstract void startDocumentSelectActivity();
  }
  
  private class HistoryEntry
  {
    File dir;
    int scrollItem;
    int scrollOffset;
    String title;
    
    private HistoryEntry() {}
  }
  
  private class ListAdapter
    extends RecyclerListView.SelectionAdapter
  {
    private Context mContext;
    
    public ListAdapter(Context paramContext)
    {
      this.mContext = paramContext;
    }
    
    public DocumentSelectActivity.ListItem getItem(int paramInt)
    {
      if (paramInt < DocumentSelectActivity.this.items.size()) {
        return (DocumentSelectActivity.ListItem)DocumentSelectActivity.this.items.get(paramInt);
      }
      if ((DocumentSelectActivity.this.history.isEmpty()) && (!DocumentSelectActivity.this.recentItems.isEmpty()) && (paramInt != DocumentSelectActivity.this.items.size()))
      {
        paramInt -= DocumentSelectActivity.this.items.size() + 1;
        if (paramInt < DocumentSelectActivity.this.recentItems.size()) {
          return (DocumentSelectActivity.ListItem)DocumentSelectActivity.this.recentItems.get(paramInt);
        }
      }
      return null;
    }
    
    public int getItemCount()
    {
      int j = DocumentSelectActivity.this.items.size();
      int i = j;
      if (DocumentSelectActivity.this.history.isEmpty())
      {
        i = j;
        if (!DocumentSelectActivity.this.recentItems.isEmpty()) {
          i = j + (DocumentSelectActivity.this.recentItems.size() + 1);
        }
      }
      return i;
    }
    
    public int getItemViewType(int paramInt)
    {
      if (getItem(paramInt) != null) {
        return 1;
      }
      return 0;
    }
    
    public boolean isEnabled(RecyclerView.ViewHolder paramViewHolder)
    {
      return paramViewHolder.getItemViewType() != 0;
    }
    
    public void onBindViewHolder(RecyclerView.ViewHolder paramViewHolder, int paramInt)
    {
      boolean bool1 = true;
      DocumentSelectActivity.ListItem localListItem;
      boolean bool2;
      if (paramViewHolder.getItemViewType() == 1)
      {
        localListItem = getItem(paramInt);
        paramViewHolder = (SharedDocumentCell)paramViewHolder.itemView;
        if (localListItem.icon == 0) {
          break label115;
        }
        paramViewHolder.setTextAndValueAndTypeAndThumb(localListItem.title, localListItem.subtitle, null, null, localListItem.icon);
        if ((localListItem.file == null) || (!DocumentSelectActivity.this.actionBar.isActionModeShowed())) {
          break label171;
        }
        bool2 = DocumentSelectActivity.this.selectedFiles.containsKey(localListItem.file.toString());
        if (DocumentSelectActivity.this.scrolling) {
          break label166;
        }
      }
      label115:
      label166:
      for (bool1 = true;; bool1 = false)
      {
        paramViewHolder.setChecked(bool2, bool1);
        return;
        String str = localListItem.ext.toUpperCase().substring(0, Math.min(localListItem.ext.length(), 4));
        paramViewHolder.setTextAndValueAndTypeAndThumb(localListItem.title, localListItem.subtitle, str, localListItem.thumb, 0);
        break;
      }
      label171:
      if (!DocumentSelectActivity.this.scrolling) {}
      for (;;)
      {
        paramViewHolder.setChecked(false, bool1);
        return;
        bool1 = false;
      }
    }
    
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
    {
      switch (paramInt)
      {
      default: 
        paramViewGroup = new SharedDocumentCell(this.mContext);
      }
      for (;;)
      {
        return new RecyclerListView.Holder(paramViewGroup);
        paramViewGroup = new GraySectionCell(this.mContext);
        ((GraySectionCell)paramViewGroup).setText(LocaleController.getString("Recent", 2131494215).toUpperCase());
      }
    }
  }
  
  private class ListItem
  {
    long date;
    String ext = "";
    File file;
    int icon;
    String subtitle = "";
    String thumb;
    String title;
    
    private ListItem() {}
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/DocumentSelectActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */