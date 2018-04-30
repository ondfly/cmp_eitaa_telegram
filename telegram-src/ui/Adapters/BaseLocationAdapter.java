package org.telegram.ui.Adapters;

import android.location.Location;
import android.os.AsyncTask;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC.TL_messageMediaVenue;
import org.telegram.ui.Components.RecyclerListView.SelectionAdapter;

public abstract class BaseLocationAdapter
  extends RecyclerListView.SelectionAdapter
{
  private AsyncTask<Void, Void, JSONObject> currentTask;
  private BaseLocationAdapterDelegate delegate;
  protected ArrayList<String> iconUrls = new ArrayList();
  private Location lastSearchLocation;
  protected ArrayList<TLRPC.TL_messageMediaVenue> places = new ArrayList();
  private Timer searchTimer;
  protected boolean searching;
  
  public void destroy()
  {
    if (this.currentTask != null)
    {
      this.currentTask.cancel(true);
      this.currentTask = null;
    }
  }
  
  public void searchDelayed(final String paramString, final Location paramLocation)
  {
    if ((paramString == null) || (paramString.length() == 0))
    {
      this.places.clear();
      notifyDataSetChanged();
      return;
    }
    try
    {
      if (this.searchTimer != null) {
        this.searchTimer.cancel();
      }
      this.searchTimer = new Timer();
      this.searchTimer.schedule(new TimerTask()
      {
        public void run()
        {
          try
          {
            BaseLocationAdapter.this.searchTimer.cancel();
            BaseLocationAdapter.access$002(BaseLocationAdapter.this, null);
            AndroidUtilities.runOnUIThread(new Runnable()
            {
              public void run()
              {
                BaseLocationAdapter.access$102(BaseLocationAdapter.this, null);
                BaseLocationAdapter.this.searchGooglePlacesWithQuery(BaseLocationAdapter.1.this.val$query, BaseLocationAdapter.1.this.val$coordinate);
              }
            });
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
      }, 200L, 500L);
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
  
  public void searchGooglePlacesWithQuery(String paramString, Location paramLocation)
  {
    if ((this.lastSearchLocation != null) && (paramLocation.distanceTo(this.lastSearchLocation) < 200.0F)) {
      return;
    }
    this.lastSearchLocation = paramLocation;
    if (this.searching)
    {
      this.searching = false;
      if (this.currentTask != null)
      {
        this.currentTask.cancel(true);
        this.currentTask = null;
      }
    }
    for (;;)
    {
      try
      {
        this.searching = true;
        Locale localLocale = Locale.US;
        String str1 = BuildVars.FOURSQUARE_API_VERSION;
        String str2 = BuildVars.FOURSQUARE_API_ID;
        String str3 = BuildVars.FOURSQUARE_API_KEY;
        paramLocation = String.format(Locale.US, "%f,%f", new Object[] { Double.valueOf(paramLocation.getLatitude()), Double.valueOf(paramLocation.getLongitude()) });
        if ((paramString == null) || (paramString.length() <= 0)) {
          continue;
        }
        paramString = "&query=" + URLEncoder.encode(paramString, "UTF-8");
        this.currentTask = new AsyncTask()
        {
          private boolean canRetry = true;
          
          private String downloadUrlContent(String paramAnonymousString)
          {
            int m = 1;
            int i = 1;
            Object localObject5 = null;
            int j = 0;
            int k = 0;
            Object localObject4 = null;
            byte[] arrayOfByte = null;
            Object localObject3 = null;
            Object localObject1 = null;
            Object localObject2;
            try
            {
              localObject2 = new URL(paramAnonymousString).openConnection();
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept-Language", "en-us,en;q=0.5");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
              localObject1 = localObject2;
              ((URLConnection)localObject2).addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
              localObject1 = localObject2;
              ((URLConnection)localObject2).setConnectTimeout(5000);
              localObject1 = localObject2;
              ((URLConnection)localObject2).setReadTimeout(5000);
              paramAnonymousString = (String)localObject2;
              localObject1 = localObject2;
              if ((localObject2 instanceof HttpURLConnection))
              {
                localObject1 = localObject2;
                Object localObject6 = (HttpURLConnection)localObject2;
                localObject1 = localObject2;
                ((HttpURLConnection)localObject6).setInstanceFollowRedirects(true);
                localObject1 = localObject2;
                int n = ((HttpURLConnection)localObject6).getResponseCode();
                if ((n != 302) && (n != 301))
                {
                  paramAnonymousString = (String)localObject2;
                  if (n != 303) {}
                }
                else
                {
                  localObject1 = localObject2;
                  paramAnonymousString = ((HttpURLConnection)localObject6).getHeaderField("Location");
                  localObject1 = localObject2;
                  localObject6 = ((HttpURLConnection)localObject6).getHeaderField("Set-Cookie");
                  localObject1 = localObject2;
                  paramAnonymousString = new URL(paramAnonymousString).openConnection();
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.setRequestProperty("Cookie", (String)localObject6);
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:10.0) Gecko/20150101 Firefox/47.0 (Chrome)");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                  localObject1 = paramAnonymousString;
                  paramAnonymousString.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
                }
              }
              localObject1 = paramAnonymousString;
              paramAnonymousString.connect();
              localObject1 = paramAnonymousString;
              localObject2 = paramAnonymousString.getInputStream();
            }
            catch (Throwable paramAnonymousString)
            {
              if (!(paramAnonymousString instanceof SocketTimeoutException)) {
                break label448;
              }
              i = m;
              if (!ConnectionsManager.isNetworkOnline()) {
                break label434;
              }
              i = 0;
              for (;;)
              {
                FileLog.e(paramAnonymousString);
                paramAnonymousString = (String)localObject1;
                localObject2 = localObject5;
                break;
                if ((paramAnonymousString instanceof UnknownHostException))
                {
                  i = 0;
                }
                else if ((paramAnonymousString instanceof SocketException))
                {
                  i = m;
                  if (paramAnonymousString.getMessage() != null)
                  {
                    i = m;
                    if (paramAnonymousString.getMessage().contains("ECONNRESET")) {
                      i = 0;
                    }
                  }
                }
                else
                {
                  i = m;
                  if ((paramAnonymousString instanceof FileNotFoundException)) {
                    i = 0;
                  }
                }
              }
            }
            localObject1 = localObject4;
            if ((i == 0) || (paramAnonymousString != null)) {}
            try
            {
              if ((paramAnonymousString instanceof HttpURLConnection))
              {
                i = ((HttpURLConnection)paramAnonymousString).getResponseCode();
                if ((i == 200) || (i == 202) || (i == 304)) {}
              }
            }
            catch (Exception paramAnonymousString)
            {
              for (;;)
              {
                FileLog.e(paramAnonymousString);
              }
            }
            i = k;
            paramAnonymousString = (String)localObject3;
            if (localObject2 != null) {
              localObject1 = arrayOfByte;
            }
            for (;;)
            {
              try
              {
                arrayOfByte = new byte[32768];
                paramAnonymousString = null;
              }
              catch (Throwable localThrowable3)
              {
                boolean bool;
                label434:
                label448:
                label548:
                paramAnonymousString = (String)localObject1;
                localObject1 = localThrowable3;
                FileLog.e((Throwable)localObject1);
                i = k;
                continue;
              }
              for (;;)
              {
                try
                {
                  bool = isCancelled();
                  if (bool)
                  {
                    i = k;
                    j = i;
                    localObject1 = paramAnonymousString;
                    if (localObject2 == null) {
                      break;
                    }
                  }
                }
                catch (Throwable localThrowable2)
                {
                  String str;
                  break label613;
                  break label548;
                }
                try
                {
                  ((InputStream)localObject2).close();
                  localObject1 = paramAnonymousString;
                  j = i;
                }
                catch (Throwable localThrowable1)
                {
                  FileLog.e(localThrowable1);
                  j = i;
                  str = paramAnonymousString;
                  break;
                }
              }
              if (j == 0) {
                break label639;
              }
              return ((StringBuilder)localObject1).toString();
              try
              {
                i = ((InputStream)localObject2).read(arrayOfByte);
                if (i > 0)
                {
                  if (paramAnonymousString != null) {
                    break label651;
                  }
                  localObject1 = new StringBuilder();
                  paramAnonymousString = (String)localObject1;
                  localObject1 = paramAnonymousString;
                }
              }
              catch (Exception localException1) {}
              try
              {
                paramAnonymousString.append(new String(arrayOfByte, 0, i, "UTF-8"));
              }
              catch (Exception localException2)
              {
                continue;
              }
              if (i == -1)
              {
                i = 1;
              }
              else
              {
                i = k;
                continue;
                localObject1 = paramAnonymousString;
                FileLog.e(localException1);
                i = k;
              }
            }
            label613:
            label639:
            return null;
          }
          
          protected JSONObject doInBackground(Void... paramAnonymousVarArgs)
          {
            paramAnonymousVarArgs = downloadUrlContent(this.val$url);
            if (isCancelled()) {
              return null;
            }
            try
            {
              paramAnonymousVarArgs = new JSONObject(paramAnonymousVarArgs);
              return paramAnonymousVarArgs;
            }
            catch (Exception paramAnonymousVarArgs)
            {
              FileLog.e(paramAnonymousVarArgs);
            }
            return null;
          }
          
          /* Error */
          protected void onPostExecute(JSONObject paramAnonymousJSONObject)
          {
            // Byte code:
            //   0: aload_1
            //   1: ifnull +497 -> 498
            //   4: aload_0
            //   5: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   8: getfield 182	org/telegram/ui/Adapters/BaseLocationAdapter:places	Ljava/util/ArrayList;
            //   11: invokevirtual 187	java/util/ArrayList:clear	()V
            //   14: aload_0
            //   15: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   18: getfield 190	org/telegram/ui/Adapters/BaseLocationAdapter:iconUrls	Ljava/util/ArrayList;
            //   21: invokevirtual 187	java/util/ArrayList:clear	()V
            //   24: aload_1
            //   25: ldc -64
            //   27: invokevirtual 196	org/json/JSONObject:getJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
            //   30: ldc -58
            //   32: invokevirtual 202	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
            //   35: astore 5
            //   37: iconst_0
            //   38: istore_2
            //   39: aload 5
            //   41: invokevirtual 207	org/json/JSONArray:length	()I
            //   44: istore_3
            //   45: iload_2
            //   46: iload_3
            //   47: if_icmpge +307 -> 354
            //   50: aload 5
            //   52: iload_2
            //   53: invokevirtual 210	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
            //   56: astore 6
            //   58: aconst_null
            //   59: astore 4
            //   61: aload 4
            //   63: astore_1
            //   64: aload 6
            //   66: ldc -44
            //   68: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   71: ifeq +83 -> 154
            //   74: aload 6
            //   76: ldc -44
            //   78: invokevirtual 202	org/json/JSONObject:getJSONArray	(Ljava/lang/String;)Lorg/json/JSONArray;
            //   81: astore 7
            //   83: aload 4
            //   85: astore_1
            //   86: aload 7
            //   88: invokevirtual 207	org/json/JSONArray:length	()I
            //   91: ifle +63 -> 154
            //   94: aload 7
            //   96: iconst_0
            //   97: invokevirtual 210	org/json/JSONArray:getJSONObject	(I)Lorg/json/JSONObject;
            //   100: astore 7
            //   102: aload 4
            //   104: astore_1
            //   105: aload 7
            //   107: ldc -38
            //   109: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   112: ifeq +42 -> 154
            //   115: aload 7
            //   117: ldc -38
            //   119: invokevirtual 196	org/json/JSONObject:getJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
            //   122: astore_1
            //   123: getstatic 224	java/util/Locale:US	Ljava/util/Locale;
            //   126: ldc -30
            //   128: iconst_2
            //   129: anewarray 228	java/lang/Object
            //   132: dup
            //   133: iconst_0
            //   134: aload_1
            //   135: ldc -26
            //   137: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   140: aastore
            //   141: dup
            //   142: iconst_1
            //   143: aload_1
            //   144: ldc -21
            //   146: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   149: aastore
            //   150: invokestatic 239	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
            //   153: astore_1
            //   154: aload_0
            //   155: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   158: getfield 190	org/telegram/ui/Adapters/BaseLocationAdapter:iconUrls	Ljava/util/ArrayList;
            //   161: aload_1
            //   162: invokevirtual 243	java/util/ArrayList:add	(Ljava/lang/Object;)Z
            //   165: pop
            //   166: aload 6
            //   168: ldc -11
            //   170: invokevirtual 196	org/json/JSONObject:getJSONObject	(Ljava/lang/String;)Lorg/json/JSONObject;
            //   173: astore_1
            //   174: new 247	org/telegram/tgnet/TLRPC$TL_messageMediaVenue
            //   177: dup
            //   178: invokespecial 248	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:<init>	()V
            //   181: astore 4
            //   183: aload 4
            //   185: new 250	org/telegram/tgnet/TLRPC$TL_geoPoint
            //   188: dup
            //   189: invokespecial 251	org/telegram/tgnet/TLRPC$TL_geoPoint:<init>	()V
            //   192: putfield 255	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
            //   195: aload 4
            //   197: getfield 255	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
            //   200: aload_1
            //   201: ldc_w 257
            //   204: invokevirtual 261	org/json/JSONObject:getDouble	(Ljava/lang/String;)D
            //   207: putfield 266	org/telegram/tgnet/TLRPC$GeoPoint:lat	D
            //   210: aload 4
            //   212: getfield 255	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
            //   215: aload_1
            //   216: ldc_w 268
            //   219: invokevirtual 261	org/json/JSONObject:getDouble	(Ljava/lang/String;)D
            //   222: putfield 271	org/telegram/tgnet/TLRPC$GeoPoint:_long	D
            //   225: aload_1
            //   226: ldc_w 273
            //   229: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   232: ifeq +84 -> 316
            //   235: aload 4
            //   237: aload_1
            //   238: ldc_w 273
            //   241: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   244: putfield 275	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:address	Ljava/lang/String;
            //   247: aload 6
            //   249: ldc_w 277
            //   252: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   255: ifeq +16 -> 271
            //   258: aload 4
            //   260: aload 6
            //   262: ldc_w 277
            //   265: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   268: putfield 280	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:title	Ljava/lang/String;
            //   271: aload 4
            //   273: ldc_w 282
            //   276: putfield 285	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:venue_type	Ljava/lang/String;
            //   279: aload 4
            //   281: aload 6
            //   283: ldc_w 287
            //   286: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   289: putfield 290	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:venue_id	Ljava/lang/String;
            //   292: aload 4
            //   294: ldc_w 292
            //   297: putfield 295	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:provider	Ljava/lang/String;
            //   300: aload_0
            //   301: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   304: getfield 182	org/telegram/ui/Adapters/BaseLocationAdapter:places	Ljava/util/ArrayList;
            //   307: aload 4
            //   309: invokevirtual 243	java/util/ArrayList:add	(Ljava/lang/Object;)Z
            //   312: pop
            //   313: goto +230 -> 543
            //   316: aload_1
            //   317: ldc_w 297
            //   320: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   323: ifeq +76 -> 399
            //   326: aload 4
            //   328: aload_1
            //   329: ldc_w 297
            //   332: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   335: putfield 275	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:address	Ljava/lang/String;
            //   338: goto -91 -> 247
            //   341: astore_1
            //   342: aload_1
            //   343: invokestatic 130	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
            //   346: goto +197 -> 543
            //   349: astore_1
            //   350: aload_1
            //   351: invokestatic 130	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
            //   354: aload_0
            //   355: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   358: iconst_0
            //   359: putfield 300	org/telegram/ui/Adapters/BaseLocationAdapter:searching	Z
            //   362: aload_0
            //   363: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   366: invokevirtual 303	org/telegram/ui/Adapters/BaseLocationAdapter:notifyDataSetChanged	()V
            //   369: aload_0
            //   370: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   373: invokestatic 307	org/telegram/ui/Adapters/BaseLocationAdapter:access$200	(Lorg/telegram/ui/Adapters/BaseLocationAdapter;)Lorg/telegram/ui/Adapters/BaseLocationAdapter$BaseLocationAdapterDelegate;
            //   376: ifnull +22 -> 398
            //   379: aload_0
            //   380: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   383: invokestatic 307	org/telegram/ui/Adapters/BaseLocationAdapter:access$200	(Lorg/telegram/ui/Adapters/BaseLocationAdapter;)Lorg/telegram/ui/Adapters/BaseLocationAdapter$BaseLocationAdapterDelegate;
            //   386: aload_0
            //   387: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   390: getfield 182	org/telegram/ui/Adapters/BaseLocationAdapter:places	Ljava/util/ArrayList;
            //   393: invokeinterface 313 2 0
            //   398: return
            //   399: aload_1
            //   400: ldc_w 315
            //   403: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   406: ifeq +18 -> 424
            //   409: aload 4
            //   411: aload_1
            //   412: ldc_w 315
            //   415: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   418: putfield 275	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:address	Ljava/lang/String;
            //   421: goto -174 -> 247
            //   424: aload_1
            //   425: ldc_w 317
            //   428: invokevirtual 216	org/json/JSONObject:has	(Ljava/lang/String;)Z
            //   431: ifeq +18 -> 449
            //   434: aload 4
            //   436: aload_1
            //   437: ldc_w 317
            //   440: invokevirtual 233	org/json/JSONObject:getString	(Ljava/lang/String;)Ljava/lang/String;
            //   443: putfield 275	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:address	Ljava/lang/String;
            //   446: goto -199 -> 247
            //   449: aload 4
            //   451: getstatic 224	java/util/Locale:US	Ljava/util/Locale;
            //   454: ldc_w 319
            //   457: iconst_2
            //   458: anewarray 228	java/lang/Object
            //   461: dup
            //   462: iconst_0
            //   463: aload 4
            //   465: getfield 255	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
            //   468: getfield 266	org/telegram/tgnet/TLRPC$GeoPoint:lat	D
            //   471: invokestatic 325	java/lang/Double:valueOf	(D)Ljava/lang/Double;
            //   474: aastore
            //   475: dup
            //   476: iconst_1
            //   477: aload 4
            //   479: getfield 255	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:geo	Lorg/telegram/tgnet/TLRPC$GeoPoint;
            //   482: getfield 271	org/telegram/tgnet/TLRPC$GeoPoint:_long	D
            //   485: invokestatic 325	java/lang/Double:valueOf	(D)Ljava/lang/Double;
            //   488: aastore
            //   489: invokestatic 239	java/lang/String:format	(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
            //   492: putfield 275	org/telegram/tgnet/TLRPC$TL_messageMediaVenue:address	Ljava/lang/String;
            //   495: goto -248 -> 247
            //   498: aload_0
            //   499: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   502: iconst_0
            //   503: putfield 300	org/telegram/ui/Adapters/BaseLocationAdapter:searching	Z
            //   506: aload_0
            //   507: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   510: invokevirtual 303	org/telegram/ui/Adapters/BaseLocationAdapter:notifyDataSetChanged	()V
            //   513: aload_0
            //   514: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   517: invokestatic 307	org/telegram/ui/Adapters/BaseLocationAdapter:access$200	(Lorg/telegram/ui/Adapters/BaseLocationAdapter;)Lorg/telegram/ui/Adapters/BaseLocationAdapter$BaseLocationAdapterDelegate;
            //   520: ifnull -122 -> 398
            //   523: aload_0
            //   524: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   527: invokestatic 307	org/telegram/ui/Adapters/BaseLocationAdapter:access$200	(Lorg/telegram/ui/Adapters/BaseLocationAdapter;)Lorg/telegram/ui/Adapters/BaseLocationAdapter$BaseLocationAdapterDelegate;
            //   530: aload_0
            //   531: getfield 20	org/telegram/ui/Adapters/BaseLocationAdapter$2:this$0	Lorg/telegram/ui/Adapters/BaseLocationAdapter;
            //   534: getfield 182	org/telegram/ui/Adapters/BaseLocationAdapter:places	Ljava/util/ArrayList;
            //   537: invokeinterface 313 2 0
            //   542: return
            //   543: iload_2
            //   544: iconst_1
            //   545: iadd
            //   546: istore_2
            //   547: goto -508 -> 39
            // Local variable table:
            //   start	length	slot	name	signature
            //   0	550	0	this	2
            //   0	550	1	paramAnonymousJSONObject	JSONObject
            //   38	509	2	i	int
            //   44	4	3	j	int
            //   59	419	4	localTL_messageMediaVenue	TLRPC.TL_messageMediaVenue
            //   35	16	5	localJSONArray	org.json.JSONArray
            //   56	226	6	localJSONObject	JSONObject
            //   81	35	7	localObject	Object
            // Exception table:
            //   from	to	target	type
            //   50	58	341	java/lang/Exception
            //   64	83	341	java/lang/Exception
            //   86	102	341	java/lang/Exception
            //   105	154	341	java/lang/Exception
            //   154	247	341	java/lang/Exception
            //   247	271	341	java/lang/Exception
            //   271	313	341	java/lang/Exception
            //   316	338	341	java/lang/Exception
            //   399	421	341	java/lang/Exception
            //   424	446	341	java/lang/Exception
            //   449	495	341	java/lang/Exception
            //   4	37	349	java/lang/Exception
            //   39	45	349	java/lang/Exception
            //   342	346	349	java/lang/Exception
          }
        };
        this.currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] { null, null, null });
      }
      catch (Exception paramString)
      {
        FileLog.e(paramString);
        this.searching = false;
        if (this.delegate == null) {
          continue;
        }
        this.delegate.didLoadedSearchResult(this.places);
        continue;
      }
      notifyDataSetChanged();
      return;
      paramString = "";
    }
  }
  
  public void setDelegate(BaseLocationAdapterDelegate paramBaseLocationAdapterDelegate)
  {
    this.delegate = paramBaseLocationAdapterDelegate;
  }
  
  public static abstract interface BaseLocationAdapterDelegate
  {
    public abstract void didLoadedSearchResult(ArrayList<TLRPC.TL_messageMediaVenue> paramArrayList);
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Adapters/BaseLocationAdapter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */