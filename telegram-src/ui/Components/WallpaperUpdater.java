package org.telegram.ui.Components;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.security.SecureRandom;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AlertDialog.Builder;

public class WallpaperUpdater
{
  private String currentPicturePath;
  private File currentWallpaperPath;
  private WallpaperUpdaterDelegate delegate;
  private Activity parentActivity;
  private File picturePath = null;
  
  public WallpaperUpdater(Activity paramActivity, WallpaperUpdaterDelegate paramWallpaperUpdaterDelegate)
  {
    this.parentActivity = paramActivity;
    this.delegate = paramWallpaperUpdaterDelegate;
    this.currentWallpaperPath = new File(FileLoader.getDirectory(4), Utilities.random.nextInt() + ".jpg");
  }
  
  public void cleanup()
  {
    this.currentWallpaperPath.delete();
  }
  
  public String getCurrentPicturePath()
  {
    return this.currentPicturePath;
  }
  
  public File getCurrentWallpaperPath()
  {
    return this.currentWallpaperPath;
  }
  
  /* Error */
  public void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
  {
    // Byte code:
    //   0: iload_2
    //   1: iconst_m1
    //   2: if_icmpne +115 -> 117
    //   5: iload_1
    //   6: bipush 10
    //   8: if_icmpne +176 -> 184
    //   11: aload_0
    //   12: getfield 77	org/telegram/ui/Components/WallpaperUpdater:currentPicturePath	Ljava/lang/String;
    //   15: invokestatic 97	org/telegram/messenger/AndroidUtilities:addMediaToGallery	(Ljava/lang/String;)V
    //   18: aconst_null
    //   19: astore 4
    //   21: aconst_null
    //   22: astore 5
    //   24: aload 4
    //   26: astore_3
    //   27: invokestatic 101	org/telegram/messenger/AndroidUtilities:getRealScreenSize	()Landroid/graphics/Point;
    //   30: astore 6
    //   32: aload 4
    //   34: astore_3
    //   35: aload_0
    //   36: getfield 77	org/telegram/ui/Components/WallpaperUpdater:currentPicturePath	Ljava/lang/String;
    //   39: aconst_null
    //   40: aload 6
    //   42: getfield 107	android/graphics/Point:x	I
    //   45: i2f
    //   46: aload 6
    //   48: getfield 110	android/graphics/Point:y	I
    //   51: i2f
    //   52: iconst_1
    //   53: invokestatic 116	org/telegram/messenger/ImageLoader:loadBitmap	(Ljava/lang/String;Landroid/net/Uri;FFZ)Landroid/graphics/Bitmap;
    //   56: astore 6
    //   58: aload 4
    //   60: astore_3
    //   61: new 118	java/io/FileOutputStream
    //   64: dup
    //   65: aload_0
    //   66: getfield 70	org/telegram/ui/Components/WallpaperUpdater:currentWallpaperPath	Ljava/io/File;
    //   69: invokespecial 121	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   72: astore 4
    //   74: aload 6
    //   76: getstatic 127	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
    //   79: bipush 87
    //   81: aload 4
    //   83: invokevirtual 133	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    //   86: pop
    //   87: aload_0
    //   88: getfield 29	org/telegram/ui/Components/WallpaperUpdater:delegate	Lorg/telegram/ui/Components/WallpaperUpdater$WallpaperUpdaterDelegate;
    //   91: aload_0
    //   92: getfield 70	org/telegram/ui/Components/WallpaperUpdater:currentWallpaperPath	Ljava/io/File;
    //   95: aload 6
    //   97: invokeinterface 137 3 0
    //   102: aload 4
    //   104: ifnull +8 -> 112
    //   107: aload 4
    //   109: invokevirtual 140	java/io/FileOutputStream:close	()V
    //   112: aload_0
    //   113: aconst_null
    //   114: putfield 77	org/telegram/ui/Components/WallpaperUpdater:currentPicturePath	Ljava/lang/String;
    //   117: return
    //   118: astore_3
    //   119: aload_3
    //   120: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   123: goto -11 -> 112
    //   126: astore_3
    //   127: aload 5
    //   129: astore 4
    //   131: aload_3
    //   132: astore 5
    //   134: aload 4
    //   136: astore_3
    //   137: aload 5
    //   139: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   142: aload 4
    //   144: ifnull -32 -> 112
    //   147: aload 4
    //   149: invokevirtual 140	java/io/FileOutputStream:close	()V
    //   152: goto -40 -> 112
    //   155: astore_3
    //   156: aload_3
    //   157: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   160: goto -48 -> 112
    //   163: astore 4
    //   165: aload_3
    //   166: ifnull +7 -> 173
    //   169: aload_3
    //   170: invokevirtual 140	java/io/FileOutputStream:close	()V
    //   173: aload 4
    //   175: athrow
    //   176: astore_3
    //   177: aload_3
    //   178: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   181: goto -8 -> 173
    //   184: iload_1
    //   185: bipush 11
    //   187: if_icmpne -70 -> 117
    //   190: aload_3
    //   191: ifnull -74 -> 117
    //   194: aload_3
    //   195: invokevirtual 152	android/content/Intent:getData	()Landroid/net/Uri;
    //   198: ifnull -81 -> 117
    //   201: invokestatic 101	org/telegram/messenger/AndroidUtilities:getRealScreenSize	()Landroid/graphics/Point;
    //   204: astore 4
    //   206: aconst_null
    //   207: aload_3
    //   208: invokevirtual 152	android/content/Intent:getData	()Landroid/net/Uri;
    //   211: aload 4
    //   213: getfield 107	android/graphics/Point:x	I
    //   216: i2f
    //   217: aload 4
    //   219: getfield 110	android/graphics/Point:y	I
    //   222: i2f
    //   223: iconst_1
    //   224: invokestatic 116	org/telegram/messenger/ImageLoader:loadBitmap	(Ljava/lang/String;Landroid/net/Uri;FFZ)Landroid/graphics/Bitmap;
    //   227: astore_3
    //   228: new 118	java/io/FileOutputStream
    //   231: dup
    //   232: aload_0
    //   233: getfield 70	org/telegram/ui/Components/WallpaperUpdater:currentWallpaperPath	Ljava/io/File;
    //   236: invokespecial 121	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   239: astore 4
    //   241: aload_3
    //   242: getstatic 127	android/graphics/Bitmap$CompressFormat:JPEG	Landroid/graphics/Bitmap$CompressFormat;
    //   245: bipush 87
    //   247: aload 4
    //   249: invokevirtual 133	android/graphics/Bitmap:compress	(Landroid/graphics/Bitmap$CompressFormat;ILjava/io/OutputStream;)Z
    //   252: pop
    //   253: aload_0
    //   254: getfield 29	org/telegram/ui/Components/WallpaperUpdater:delegate	Lorg/telegram/ui/Components/WallpaperUpdater$WallpaperUpdaterDelegate;
    //   257: aload_0
    //   258: getfield 70	org/telegram/ui/Components/WallpaperUpdater:currentWallpaperPath	Ljava/io/File;
    //   261: aload_3
    //   262: invokeinterface 137 3 0
    //   267: return
    //   268: astore_3
    //   269: aload_3
    //   270: invokestatic 146	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   273: return
    //   274: astore 5
    //   276: aload 4
    //   278: astore_3
    //   279: aload 5
    //   281: astore 4
    //   283: goto -118 -> 165
    //   286: astore 5
    //   288: goto -154 -> 134
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	291	0	this	WallpaperUpdater
    //   0	291	1	paramInt1	int
    //   0	291	2	paramInt2	int
    //   0	291	3	paramIntent	Intent
    //   19	129	4	localObject1	Object
    //   163	11	4	localObject2	Object
    //   204	78	4	localObject3	Object
    //   22	116	5	localIntent	Intent
    //   274	6	5	localObject4	Object
    //   286	1	5	localException	Exception
    //   30	66	6	localObject5	Object
    // Exception table:
    //   from	to	target	type
    //   107	112	118	java/lang/Exception
    //   27	32	126	java/lang/Exception
    //   35	58	126	java/lang/Exception
    //   61	74	126	java/lang/Exception
    //   147	152	155	java/lang/Exception
    //   27	32	163	finally
    //   35	58	163	finally
    //   61	74	163	finally
    //   137	142	163	finally
    //   169	173	176	java/lang/Exception
    //   201	267	268	java/lang/Exception
    //   74	102	274	finally
    //   74	102	286	java/lang/Exception
  }
  
  public void setCurrentPicturePath(String paramString)
  {
    this.currentPicturePath = paramString;
  }
  
  public void showAlert(final boolean paramBoolean)
  {
    AlertDialog.Builder localBuilder = new AlertDialog.Builder(this.parentActivity);
    CharSequence[] arrayOfCharSequence;
    if (paramBoolean)
    {
      arrayOfCharSequence = new CharSequence[5];
      arrayOfCharSequence[0] = LocaleController.getString("FromCamera", 2131493613);
      arrayOfCharSequence[1] = LocaleController.getString("FromGalley", 2131493621);
      arrayOfCharSequence[2] = LocaleController.getString("SelectColor", 2131494328);
      arrayOfCharSequence[3] = LocaleController.getString("Default", 2131493354);
      arrayOfCharSequence[4] = LocaleController.getString("Cancel", 2131493127);
    }
    for (;;)
    {
      localBuilder.setItems(arrayOfCharSequence, new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
        {
          if (paramAnonymousInt == 0) {
            try
            {
              paramAnonymousDialogInterface = new Intent("android.media.action.IMAGE_CAPTURE");
              File localFile = AndroidUtilities.generatePicturePath();
              if (localFile != null)
              {
                if (Build.VERSION.SDK_INT < 24) {
                  break label88;
                }
                paramAnonymousDialogInterface.putExtra("output", FileProvider.getUriForFile(WallpaperUpdater.this.parentActivity, "org.telegram.messenger.provider", localFile));
                paramAnonymousDialogInterface.addFlags(2);
                paramAnonymousDialogInterface.addFlags(1);
              }
              for (;;)
              {
                WallpaperUpdater.access$102(WallpaperUpdater.this, localFile.getAbsolutePath());
                WallpaperUpdater.this.parentActivity.startActivityForResult(paramAnonymousDialogInterface, 10);
                return;
                label88:
                paramAnonymousDialogInterface.putExtra("output", Uri.fromFile(localFile));
              }
              if (paramAnonymousInt != 1) {
                break label150;
              }
            }
            catch (Exception paramAnonymousDialogInterface)
            {
              try
              {
                FileLog.e(paramAnonymousDialogInterface);
                return;
              }
              catch (Exception paramAnonymousDialogInterface)
              {
                FileLog.e(paramAnonymousDialogInterface);
                return;
              }
            }
          }
          paramAnonymousDialogInterface = new Intent("android.intent.action.PICK");
          paramAnonymousDialogInterface.setType("image/*");
          WallpaperUpdater.this.parentActivity.startActivityForResult(paramAnonymousDialogInterface, 11);
          return;
          label150:
          if (paramBoolean)
          {
            if (paramAnonymousInt == 2)
            {
              WallpaperUpdater.this.delegate.needOpenColorPicker();
              return;
            }
            if (paramAnonymousInt == 3) {
              WallpaperUpdater.this.delegate.didSelectWallpaper(null, null);
            }
          }
        }
      });
      localBuilder.show();
      return;
      arrayOfCharSequence = new CharSequence[3];
      arrayOfCharSequence[0] = LocaleController.getString("FromCamera", 2131493613);
      arrayOfCharSequence[1] = LocaleController.getString("FromGalley", 2131493621);
      arrayOfCharSequence[2] = LocaleController.getString("Cancel", 2131493127);
    }
  }
  
  public static abstract interface WallpaperUpdaterDelegate
  {
    public abstract void didSelectWallpaper(File paramFile, Bitmap paramBitmap);
    
    public abstract void needOpenColorPicker();
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Components/WallpaperUpdater.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */