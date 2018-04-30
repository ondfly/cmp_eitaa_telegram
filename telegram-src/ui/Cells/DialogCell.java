package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.util.LongSparseArray;
import android.view.View.MeasureSpec;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatPhoto;
import org.telegram.tgnet.TLRPC.DraftMessage;
import org.telegram.tgnet.TLRPC.EncryptedChat;
import org.telegram.tgnet.TLRPC.InputChannel;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.TL_dialog;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.tgnet.TLRPC.UserProfilePhoto;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.GroupCreateCheckBox;

public class DialogCell
  extends BaseCell
{
  private AvatarDrawable avatarDrawable = new AvatarDrawable();
  private ImageReceiver avatarImage = new ImageReceiver(this);
  private int avatarTop = AndroidUtilities.dp(10.0F);
  private TLRPC.Chat chat = null;
  private GroupCreateCheckBox checkBox;
  private int checkDrawLeft;
  private int checkDrawTop = AndroidUtilities.dp(18.0F);
  private StaticLayout countLayout;
  private int countLeft;
  private int countTop = AndroidUtilities.dp(39.0F);
  private int countWidth;
  private int currentAccount = UserConfig.selectedAccount;
  private long currentDialogId;
  private int currentEditDate;
  private CustomDialog customDialog;
  private boolean dialogMuted;
  private int dialogsType;
  private TLRPC.DraftMessage draftMessage;
  private boolean drawCheck1;
  private boolean drawCheck2;
  private boolean drawClock;
  private boolean drawCount;
  private boolean drawError;
  private boolean drawMention;
  private boolean drawNameBot;
  private boolean drawNameBroadcast;
  private boolean drawNameGroup;
  private boolean drawNameLock;
  private boolean drawPin;
  private boolean drawPinBackground;
  private boolean drawVerified;
  private TLRPC.EncryptedChat encryptedChat = null;
  private int errorLeft;
  private int errorTop = AndroidUtilities.dp(39.0F);
  private int halfCheckDrawLeft;
  private int index;
  private boolean isDialogCell;
  private boolean isSelected;
  private int lastMessageDate;
  private CharSequence lastMessageString;
  private CharSequence lastPrintString = null;
  private int lastSendState;
  private boolean lastUnreadState;
  private int mentionCount;
  private int mentionLeft;
  private int mentionWidth;
  private MessageObject message;
  private StaticLayout messageLayout;
  private int messageLeft;
  private int messageTop = AndroidUtilities.dp(40.0F);
  private StaticLayout nameLayout;
  private int nameLeft;
  private int nameLockLeft;
  private int nameLockTop;
  private int nameMuteLeft;
  private int pinLeft;
  private int pinTop = AndroidUtilities.dp(39.0F);
  private RectF rect = new RectF();
  private StaticLayout timeLayout;
  private int timeLeft;
  private int timeTop = AndroidUtilities.dp(17.0F);
  private int unreadCount;
  public boolean useSeparator = false;
  private TLRPC.User user = null;
  
  public DialogCell(Context paramContext, boolean paramBoolean)
  {
    super(paramContext);
    Theme.createDialogsResources(paramContext);
    this.avatarImage.setRoundRadius(AndroidUtilities.dp(26.0F));
    if (paramBoolean)
    {
      this.checkBox = new GroupCreateCheckBox(paramContext);
      this.checkBox.setVisibility(0);
      addView(this.checkBox);
    }
  }
  
  private ArrayList<TLRPC.TL_dialog> getDialogsArray()
  {
    if (this.dialogsType == 0) {
      return MessagesController.getInstance(this.currentAccount).dialogs;
    }
    if (this.dialogsType == 1) {
      return MessagesController.getInstance(this.currentAccount).dialogsServerOnly;
    }
    if (this.dialogsType == 2) {
      return MessagesController.getInstance(this.currentAccount).dialogsGroupsOnly;
    }
    if (this.dialogsType == 3) {
      return MessagesController.getInstance(this.currentAccount).dialogsForward;
    }
    return null;
  }
  
  /* Error */
  public void buildLayout()
  {
    // Byte code:
    //   0: ldc -52
    //   2: astore 20
    //   4: ldc -52
    //   6: astore 22
    //   8: aconst_null
    //   9: astore 18
    //   11: aconst_null
    //   12: astore 17
    //   14: aconst_null
    //   15: astore 23
    //   17: aconst_null
    //   18: astore 21
    //   20: aconst_null
    //   21: astore 16
    //   23: aconst_null
    //   24: astore 26
    //   26: ldc -52
    //   28: astore 24
    //   30: aconst_null
    //   31: astore 12
    //   33: aload_0
    //   34: getfield 206	org/telegram/ui/Cells/DialogCell:isDialogCell	Z
    //   37: ifeq +25 -> 62
    //   40: aload_0
    //   41: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   44: invokestatic 184	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   47: getfield 210	org/telegram/messenger/MessagesController:printingStrings	Landroid/util/LongSparseArray;
    //   50: aload_0
    //   51: getfield 212	org/telegram/ui/Cells/DialogCell:currentDialogId	J
    //   54: invokevirtual 218	android/util/LongSparseArray:get	(J)Ljava/lang/Object;
    //   57: checkcast 220	java/lang/CharSequence
    //   60: astore 12
    //   62: getstatic 224	org/telegram/ui/ActionBar/Theme:dialogs_namePaint	Landroid/text/TextPaint;
    //   65: astore 19
    //   67: getstatic 227	org/telegram/ui/ActionBar/Theme:dialogs_messagePaint	Landroid/text/TextPaint;
    //   70: astore 14
    //   72: iconst_1
    //   73: istore 8
    //   75: iconst_1
    //   76: istore 6
    //   78: aload_0
    //   79: iconst_0
    //   80: putfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   83: aload_0
    //   84: iconst_0
    //   85: putfield 231	org/telegram/ui/Cells/DialogCell:drawNameBroadcast	Z
    //   88: aload_0
    //   89: iconst_0
    //   90: putfield 233	org/telegram/ui/Cells/DialogCell:drawNameLock	Z
    //   93: aload_0
    //   94: iconst_0
    //   95: putfield 235	org/telegram/ui/Cells/DialogCell:drawNameBot	Z
    //   98: aload_0
    //   99: iconst_0
    //   100: putfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   103: aload_0
    //   104: iconst_0
    //   105: putfield 239	org/telegram/ui/Cells/DialogCell:drawPinBackground	Z
    //   108: aload_0
    //   109: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   112: invokestatic 245	org/telegram/messenger/UserObject:isUserSelf	(Lorg/telegram/tgnet/TLRPC$User;)Z
    //   115: ifne +1181 -> 1296
    //   118: iconst_1
    //   119: istore 4
    //   121: iconst_1
    //   122: istore 9
    //   124: iconst_1
    //   125: istore 5
    //   127: getstatic 250	android/os/Build$VERSION:SDK_INT	I
    //   130: bipush 18
    //   132: if_icmplt +1170 -> 1302
    //   135: ldc -4
    //   137: astore 15
    //   139: aload_0
    //   140: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   143: ifnull +1167 -> 1310
    //   146: aload_0
    //   147: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   150: getfield 259	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   153: astore 13
    //   155: aload_0
    //   156: aload 13
    //   158: putfield 261	org/telegram/ui/Cells/DialogCell:lastMessageString	Ljava/lang/CharSequence;
    //   161: aload_0
    //   162: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   165: ifnull +1544 -> 1709
    //   168: aload_0
    //   169: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   172: getfield 266	org/telegram/ui/Cells/DialogCell$CustomDialog:type	I
    //   175: iconst_2
    //   176: if_icmpne +1176 -> 1352
    //   179: aload_0
    //   180: iconst_1
    //   181: putfield 233	org/telegram/ui/Cells/DialogCell:drawNameLock	Z
    //   184: aload_0
    //   185: ldc_w 267
    //   188: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   191: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   194: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   197: ifne +1119 -> 1316
    //   200: aload_0
    //   201: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   204: i2f
    //   205: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   208: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   211: aload_0
    //   212: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   215: iconst_4
    //   216: iadd
    //   217: i2f
    //   218: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   221: getstatic 283	org/telegram/ui/ActionBar/Theme:dialogs_lockDrawable	Landroid/graphics/drawable/Drawable;
    //   224: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   227: iadd
    //   228: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   231: aload_0
    //   232: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   235: getfield 266	org/telegram/ui/Cells/DialogCell$CustomDialog:type	I
    //   238: iconst_1
    //   239: if_icmpne +1388 -> 1627
    //   242: ldc_w 293
    //   245: ldc_w 294
    //   248: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   251: astore 16
    //   253: iconst_0
    //   254: istore 4
    //   256: aload_0
    //   257: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   260: getfield 301	org/telegram/ui/Cells/DialogCell$CustomDialog:isMedia	Z
    //   263: ifeq +1291 -> 1554
    //   266: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   269: astore 12
    //   271: aload 15
    //   273: iconst_2
    //   274: anewarray 306	java/lang/Object
    //   277: dup
    //   278: iconst_0
    //   279: aload 16
    //   281: aastore
    //   282: dup
    //   283: iconst_1
    //   284: aload_0
    //   285: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   288: getfield 259	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   291: aastore
    //   292: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   295: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   298: astore 13
    //   300: aload 13
    //   302: new 320	android/text/style/ForegroundColorSpan
    //   305: dup
    //   306: ldc_w 322
    //   309: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   312: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   315: aload 16
    //   317: invokevirtual 331	java/lang/String:length	()I
    //   320: iconst_2
    //   321: iadd
    //   322: aload 13
    //   324: invokevirtual 332	android/text/SpannableStringBuilder:length	()I
    //   327: bipush 33
    //   329: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   332: aload 13
    //   334: invokevirtual 332	android/text/SpannableStringBuilder:length	()I
    //   337: ifle +31 -> 368
    //   340: aload 13
    //   342: new 320	android/text/style/ForegroundColorSpan
    //   345: dup
    //   346: ldc_w 338
    //   349: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   352: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   355: iconst_0
    //   356: aload 16
    //   358: invokevirtual 331	java/lang/String:length	()I
    //   361: iconst_1
    //   362: iadd
    //   363: bipush 33
    //   365: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   368: aload 13
    //   370: getstatic 227	org/telegram/ui/ActionBar/Theme:dialogs_messagePaint	Landroid/text/TextPaint;
    //   373: invokevirtual 344	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   376: ldc_w 345
    //   379: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   382: iconst_0
    //   383: invokestatic 351	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   386: astore 13
    //   388: aload_0
    //   389: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   392: getfield 354	org/telegram/ui/Cells/DialogCell$CustomDialog:date	I
    //   395: i2l
    //   396: invokestatic 358	org/telegram/messenger/LocaleController:stringForMessageListDate	(J)Ljava/lang/String;
    //   399: astore 16
    //   401: aload_0
    //   402: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   405: getfield 361	org/telegram/ui/Cells/DialogCell$CustomDialog:unread_count	I
    //   408: ifeq +1266 -> 1674
    //   411: aload_0
    //   412: iconst_1
    //   413: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   416: ldc_w 365
    //   419: iconst_1
    //   420: anewarray 306	java/lang/Object
    //   423: dup
    //   424: iconst_0
    //   425: aload_0
    //   426: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   429: getfield 361	org/telegram/ui/Cells/DialogCell$CustomDialog:unread_count	I
    //   432: invokestatic 370	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   435: aastore
    //   436: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   439: astore 15
    //   441: aload_0
    //   442: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   445: getfield 373	org/telegram/ui/Cells/DialogCell$CustomDialog:sent	Z
    //   448: ifeq +1238 -> 1686
    //   451: aload_0
    //   452: iconst_1
    //   453: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   456: aload_0
    //   457: iconst_1
    //   458: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   461: aload_0
    //   462: iconst_0
    //   463: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   466: aload_0
    //   467: iconst_0
    //   468: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   471: aload_0
    //   472: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   475: getfield 385	org/telegram/ui/Cells/DialogCell$CustomDialog:name	Ljava/lang/String;
    //   478: astore 17
    //   480: aload 16
    //   482: astore 25
    //   484: iload 4
    //   486: istore 7
    //   488: aload 15
    //   490: astore 23
    //   492: aload 12
    //   494: astore 22
    //   496: iload 5
    //   498: istore 8
    //   500: aload 26
    //   502: astore 24
    //   504: aload 13
    //   506: astore 14
    //   508: aload 17
    //   510: astore 20
    //   512: aload_0
    //   513: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   516: getfield 266	org/telegram/ui/Cells/DialogCell$CustomDialog:type	I
    //   519: iconst_2
    //   520: if_icmpne +40 -> 560
    //   523: getstatic 388	org/telegram/ui/ActionBar/Theme:dialogs_nameEncryptedPaint	Landroid/text/TextPaint;
    //   526: astore 19
    //   528: aload 17
    //   530: astore 20
    //   532: aload 13
    //   534: astore 14
    //   536: aload 26
    //   538: astore 24
    //   540: iload 5
    //   542: istore 8
    //   544: aload 12
    //   546: astore 22
    //   548: aload 15
    //   550: astore 23
    //   552: iload 4
    //   554: istore 7
    //   556: aload 16
    //   558: astore 25
    //   560: iload 8
    //   562: ifeq +4587 -> 5149
    //   565: getstatic 391	org/telegram/ui/ActionBar/Theme:dialogs_timePaint	Landroid/text/TextPaint;
    //   568: aload 25
    //   570: invokevirtual 395	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   573: f2d
    //   574: invokestatic 401	java/lang/Math:ceil	(D)D
    //   577: d2i
    //   578: istore 6
    //   580: aload_0
    //   581: new 403	android/text/StaticLayout
    //   584: dup
    //   585: aload 25
    //   587: getstatic 391	org/telegram/ui/ActionBar/Theme:dialogs_timePaint	Landroid/text/TextPaint;
    //   590: iload 6
    //   592: getstatic 409	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   595: fconst_1
    //   596: fconst_0
    //   597: iconst_0
    //   598: invokespecial 412	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   601: putfield 414	org/telegram/ui/Cells/DialogCell:timeLayout	Landroid/text/StaticLayout;
    //   604: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   607: ifne +4529 -> 5136
    //   610: aload_0
    //   611: aload_0
    //   612: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   615: ldc_w 418
    //   618: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   621: isub
    //   622: iload 6
    //   624: isub
    //   625: putfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   628: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   631: ifne +4534 -> 5165
    //   634: aload_0
    //   635: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   638: aload_0
    //   639: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   642: isub
    //   643: ldc_w 421
    //   646: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   649: isub
    //   650: iload 6
    //   652: isub
    //   653: istore 5
    //   655: aload_0
    //   656: getfield 233	org/telegram/ui/Cells/DialogCell:drawNameLock	Z
    //   659: ifeq +4542 -> 5201
    //   662: iload 5
    //   664: ldc_w 422
    //   667: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   670: getstatic 283	org/telegram/ui/ActionBar/Theme:dialogs_lockDrawable	Landroid/graphics/drawable/Drawable;
    //   673: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   676: iadd
    //   677: isub
    //   678: istore 4
    //   680: aload_0
    //   681: getfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   684: ifeq +4637 -> 5321
    //   687: getstatic 425	org/telegram/ui/ActionBar/Theme:dialogs_clockDrawable	Landroid/graphics/drawable/Drawable;
    //   690: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   693: ldc_w 426
    //   696: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   699: iadd
    //   700: istore 8
    //   702: iload 4
    //   704: iload 8
    //   706: isub
    //   707: istore 5
    //   709: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   712: ifne +4577 -> 5289
    //   715: aload_0
    //   716: aload_0
    //   717: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   720: iload 8
    //   722: isub
    //   723: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   726: aload_0
    //   727: getfield 430	org/telegram/ui/Cells/DialogCell:dialogMuted	Z
    //   730: ifeq +4797 -> 5527
    //   733: aload_0
    //   734: getfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   737: ifne +4790 -> 5527
    //   740: ldc_w 431
    //   743: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   746: getstatic 434	org/telegram/ui/ActionBar/Theme:dialogs_muteDrawable	Landroid/graphics/drawable/Drawable;
    //   749: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   752: iadd
    //   753: istore 6
    //   755: iload 5
    //   757: iload 6
    //   759: isub
    //   760: istore 5
    //   762: iload 5
    //   764: istore 4
    //   766: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   769: ifeq +18 -> 787
    //   772: aload_0
    //   773: aload_0
    //   774: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   777: iload 6
    //   779: iadd
    //   780: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   783: iload 5
    //   785: istore 4
    //   787: ldc_w 435
    //   790: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   793: iload 4
    //   795: invokestatic 439	java/lang/Math:max	(II)I
    //   798: istore 6
    //   800: aload_0
    //   801: new 403	android/text/StaticLayout
    //   804: dup
    //   805: aload 20
    //   807: bipush 10
    //   809: bipush 32
    //   811: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   814: aload 19
    //   816: iload 6
    //   818: ldc_w 435
    //   821: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   824: isub
    //   825: i2f
    //   826: getstatic 449	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   829: invokestatic 455	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   832: aload 19
    //   834: iload 6
    //   836: getstatic 409	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   839: fconst_1
    //   840: fconst_0
    //   841: iconst_0
    //   842: invokespecial 412	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   845: putfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   848: aload_0
    //   849: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   852: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   855: bipush 16
    //   857: iadd
    //   858: i2f
    //   859: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   862: isub
    //   863: istore 4
    //   865: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   868: ifne +4737 -> 5605
    //   871: aload_0
    //   872: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   875: i2f
    //   876: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   879: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   882: invokestatic 463	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   885: ifeq +4713 -> 5598
    //   888: ldc_w 464
    //   891: fstore_3
    //   892: fload_3
    //   893: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   896: istore 5
    //   898: aload_0
    //   899: getfield 105	org/telegram/ui/Cells/DialogCell:avatarImage	Lorg/telegram/messenger/ImageReceiver;
    //   902: iload 5
    //   904: aload_0
    //   905: getfield 147	org/telegram/ui/Cells/DialogCell:avatarTop	I
    //   908: ldc_w 465
    //   911: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   914: ldc_w 465
    //   917: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   920: invokevirtual 469	org/telegram/messenger/ImageReceiver:setImageCoords	(IIII)V
    //   923: aload_0
    //   924: getfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   927: ifeq +4747 -> 5674
    //   930: ldc_w 470
    //   933: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   936: istore 5
    //   938: iload 4
    //   940: iload 5
    //   942: isub
    //   943: istore 4
    //   945: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   948: ifne +4702 -> 5650
    //   951: aload_0
    //   952: aload_0
    //   953: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   956: ldc_w 471
    //   959: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   962: isub
    //   963: putfield 473	org/telegram/ui/Cells/DialogCell:errorLeft	I
    //   966: aload 14
    //   968: astore 12
    //   970: iload 7
    //   972: ifeq +77 -> 1049
    //   975: aload 14
    //   977: astore 12
    //   979: aload 14
    //   981: ifnonnull +7 -> 988
    //   984: ldc -52
    //   986: astore 12
    //   988: aload 12
    //   990: invokeinterface 477 1 0
    //   995: astore 13
    //   997: aload 13
    //   999: astore 12
    //   1001: aload 13
    //   1003: invokevirtual 331	java/lang/String:length	()I
    //   1006: sipush 150
    //   1009: if_icmple +14 -> 1023
    //   1012: aload 13
    //   1014: iconst_0
    //   1015: sipush 150
    //   1018: invokevirtual 481	java/lang/String:substring	(II)Ljava/lang/String;
    //   1021: astore 12
    //   1023: aload 12
    //   1025: bipush 10
    //   1027: bipush 32
    //   1029: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   1032: getstatic 227	org/telegram/ui/ActionBar/Theme:dialogs_messagePaint	Landroid/text/TextPaint;
    //   1035: invokevirtual 344	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   1038: ldc 123
    //   1040: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1043: iconst_0
    //   1044: invokestatic 351	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   1047: astore 12
    //   1049: ldc_w 435
    //   1052: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1055: iload 4
    //   1057: invokestatic 439	java/lang/Math:max	(II)I
    //   1060: istore 4
    //   1062: aload 12
    //   1064: aload 22
    //   1066: iload 4
    //   1068: ldc_w 435
    //   1071: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1074: isub
    //   1075: i2f
    //   1076: getstatic 449	android/text/TextUtils$TruncateAt:END	Landroid/text/TextUtils$TruncateAt;
    //   1079: invokestatic 455	android/text/TextUtils:ellipsize	(Ljava/lang/CharSequence;Landroid/text/TextPaint;FLandroid/text/TextUtils$TruncateAt;)Ljava/lang/CharSequence;
    //   1082: astore 12
    //   1084: aload_0
    //   1085: new 403	android/text/StaticLayout
    //   1088: dup
    //   1089: aload 12
    //   1091: aload 22
    //   1093: iload 4
    //   1095: getstatic 409	android/text/Layout$Alignment:ALIGN_NORMAL	Landroid/text/Layout$Alignment;
    //   1098: fconst_1
    //   1099: fconst_0
    //   1100: iconst_0
    //   1101: invokespecial 412	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   1104: putfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   1107: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   1110: ifeq +5035 -> 6145
    //   1113: aload_0
    //   1114: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   1117: ifnull +111 -> 1228
    //   1120: aload_0
    //   1121: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   1124: invokevirtual 486	android/text/StaticLayout:getLineCount	()I
    //   1127: ifle +101 -> 1228
    //   1130: aload_0
    //   1131: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   1134: iconst_0
    //   1135: invokevirtual 490	android/text/StaticLayout:getLineLeft	(I)F
    //   1138: fstore_3
    //   1139: aload_0
    //   1140: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   1143: iconst_0
    //   1144: invokevirtual 493	android/text/StaticLayout:getLineWidth	(I)F
    //   1147: f2d
    //   1148: invokestatic 401	java/lang/Math:ceil	(D)D
    //   1151: dstore_1
    //   1152: aload_0
    //   1153: getfield 430	org/telegram/ui/Cells/DialogCell:dialogMuted	Z
    //   1156: ifeq +4947 -> 6103
    //   1159: aload_0
    //   1160: getfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   1163: ifne +4940 -> 6103
    //   1166: aload_0
    //   1167: aload_0
    //   1168: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1171: i2d
    //   1172: iload 6
    //   1174: i2d
    //   1175: dload_1
    //   1176: dsub
    //   1177: dadd
    //   1178: ldc_w 431
    //   1181: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1184: i2d
    //   1185: dsub
    //   1186: getstatic 434	org/telegram/ui/ActionBar/Theme:dialogs_muteDrawable	Landroid/graphics/drawable/Drawable;
    //   1189: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1192: i2d
    //   1193: dsub
    //   1194: d2i
    //   1195: putfield 495	org/telegram/ui/Cells/DialogCell:nameMuteLeft	I
    //   1198: fload_3
    //   1199: fconst_0
    //   1200: fcmpl
    //   1201: ifne +27 -> 1228
    //   1204: dload_1
    //   1205: iload 6
    //   1207: i2d
    //   1208: dcmpg
    //   1209: ifge +19 -> 1228
    //   1212: aload_0
    //   1213: aload_0
    //   1214: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1217: i2d
    //   1218: iload 6
    //   1220: i2d
    //   1221: dload_1
    //   1222: dsub
    //   1223: dadd
    //   1224: d2i
    //   1225: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1228: aload_0
    //   1229: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   1232: ifnull +63 -> 1295
    //   1235: aload_0
    //   1236: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   1239: invokevirtual 486	android/text/StaticLayout:getLineCount	()I
    //   1242: ifle +53 -> 1295
    //   1245: aload_0
    //   1246: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   1249: iconst_0
    //   1250: invokevirtual 490	android/text/StaticLayout:getLineLeft	(I)F
    //   1253: fconst_0
    //   1254: fcmpl
    //   1255: ifne +40 -> 1295
    //   1258: aload_0
    //   1259: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   1262: iconst_0
    //   1263: invokevirtual 493	android/text/StaticLayout:getLineWidth	(I)F
    //   1266: f2d
    //   1267: invokestatic 401	java/lang/Math:ceil	(D)D
    //   1270: dstore_1
    //   1271: dload_1
    //   1272: iload 4
    //   1274: i2d
    //   1275: dcmpg
    //   1276: ifge +19 -> 1295
    //   1279: aload_0
    //   1280: aload_0
    //   1281: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   1284: i2d
    //   1285: iload 4
    //   1287: i2d
    //   1288: dload_1
    //   1289: dsub
    //   1290: dadd
    //   1291: d2i
    //   1292: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   1295: return
    //   1296: iconst_0
    //   1297: istore 4
    //   1299: goto -1178 -> 121
    //   1302: ldc_w 497
    //   1305: astore 15
    //   1307: goto -1168 -> 139
    //   1310: aconst_null
    //   1311: astore 13
    //   1313: goto -1158 -> 155
    //   1316: aload_0
    //   1317: aload_0
    //   1318: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   1321: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1324: i2f
    //   1325: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1328: isub
    //   1329: getstatic 283	org/telegram/ui/ActionBar/Theme:dialogs_lockDrawable	Landroid/graphics/drawable/Drawable;
    //   1332: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1335: isub
    //   1336: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   1339: aload_0
    //   1340: ldc_w 421
    //   1343: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1346: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1349: goto -1118 -> 231
    //   1352: aload_0
    //   1353: aload_0
    //   1354: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   1357: getfield 500	org/telegram/ui/Cells/DialogCell$CustomDialog:verified	Z
    //   1360: putfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   1363: aload_0
    //   1364: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   1367: getfield 266	org/telegram/ui/Cells/DialogCell$CustomDialog:type	I
    //   1370: iconst_1
    //   1371: if_icmpne +150 -> 1521
    //   1374: aload_0
    //   1375: iconst_1
    //   1376: putfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   1379: aload_0
    //   1380: ldc_w 501
    //   1383: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1386: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   1389: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   1392: ifne +63 -> 1455
    //   1395: aload_0
    //   1396: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1399: i2f
    //   1400: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1403: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   1406: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1409: iconst_4
    //   1410: iadd
    //   1411: i2f
    //   1412: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1415: istore 7
    //   1417: aload_0
    //   1418: getfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   1421: ifeq +23 -> 1444
    //   1424: getstatic 504	org/telegram/ui/ActionBar/Theme:dialogs_groupDrawable	Landroid/graphics/drawable/Drawable;
    //   1427: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1430: istore 4
    //   1432: aload_0
    //   1433: iload 4
    //   1435: iload 7
    //   1437: iadd
    //   1438: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1441: goto -1210 -> 231
    //   1444: getstatic 507	org/telegram/ui/ActionBar/Theme:dialogs_broadcastDrawable	Landroid/graphics/drawable/Drawable;
    //   1447: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1450: istore 4
    //   1452: goto -20 -> 1432
    //   1455: aload_0
    //   1456: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   1459: istore 7
    //   1461: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1464: i2f
    //   1465: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1468: istore 8
    //   1470: aload_0
    //   1471: getfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   1474: ifeq +36 -> 1510
    //   1477: getstatic 504	org/telegram/ui/ActionBar/Theme:dialogs_groupDrawable	Landroid/graphics/drawable/Drawable;
    //   1480: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1483: istore 4
    //   1485: aload_0
    //   1486: iload 7
    //   1488: iload 8
    //   1490: isub
    //   1491: iload 4
    //   1493: isub
    //   1494: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   1497: aload_0
    //   1498: ldc_w 421
    //   1501: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1504: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1507: goto -1276 -> 231
    //   1510: getstatic 507	org/telegram/ui/ActionBar/Theme:dialogs_broadcastDrawable	Landroid/graphics/drawable/Drawable;
    //   1513: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1516: istore 4
    //   1518: goto -33 -> 1485
    //   1521: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   1524: ifne +17 -> 1541
    //   1527: aload_0
    //   1528: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1531: i2f
    //   1532: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1535: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1538: goto -1307 -> 231
    //   1541: aload_0
    //   1542: ldc_w 421
    //   1545: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1548: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1551: goto -1320 -> 231
    //   1554: aload_0
    //   1555: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   1558: getfield 509	org/telegram/ui/Cells/DialogCell$CustomDialog:message	Ljava/lang/String;
    //   1561: astore 13
    //   1563: aload 13
    //   1565: astore 12
    //   1567: aload 13
    //   1569: invokevirtual 331	java/lang/String:length	()I
    //   1572: sipush 150
    //   1575: if_icmple +14 -> 1589
    //   1578: aload 13
    //   1580: iconst_0
    //   1581: sipush 150
    //   1584: invokevirtual 481	java/lang/String:substring	(II)Ljava/lang/String;
    //   1587: astore 12
    //   1589: aload 15
    //   1591: iconst_2
    //   1592: anewarray 306	java/lang/Object
    //   1595: dup
    //   1596: iconst_0
    //   1597: aload 16
    //   1599: aastore
    //   1600: dup
    //   1601: iconst_1
    //   1602: aload 12
    //   1604: bipush 10
    //   1606: bipush 32
    //   1608: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   1611: aastore
    //   1612: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   1615: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   1618: astore 13
    //   1620: aload 14
    //   1622: astore 12
    //   1624: goto -1292 -> 332
    //   1627: aload_0
    //   1628: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   1631: getfield 509	org/telegram/ui/Cells/DialogCell$CustomDialog:message	Ljava/lang/String;
    //   1634: astore 15
    //   1636: iload 6
    //   1638: istore 4
    //   1640: aload 14
    //   1642: astore 12
    //   1644: aload 15
    //   1646: astore 13
    //   1648: aload_0
    //   1649: getfield 263	org/telegram/ui/Cells/DialogCell:customDialog	Lorg/telegram/ui/Cells/DialogCell$CustomDialog;
    //   1652: getfield 301	org/telegram/ui/Cells/DialogCell$CustomDialog:isMedia	Z
    //   1655: ifeq -1267 -> 388
    //   1658: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   1661: astore 12
    //   1663: iload 6
    //   1665: istore 4
    //   1667: aload 15
    //   1669: astore 13
    //   1671: goto -1283 -> 388
    //   1674: aload_0
    //   1675: iconst_0
    //   1676: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   1679: aload 23
    //   1681: astore 15
    //   1683: goto -1242 -> 441
    //   1686: aload_0
    //   1687: iconst_0
    //   1688: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   1691: aload_0
    //   1692: iconst_0
    //   1693: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   1696: aload_0
    //   1697: iconst_0
    //   1698: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   1701: aload_0
    //   1702: iconst_0
    //   1703: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   1706: goto -1235 -> 471
    //   1709: aload_0
    //   1710: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   1713: ifnull +501 -> 2214
    //   1716: aload_0
    //   1717: iconst_1
    //   1718: putfield 233	org/telegram/ui/Cells/DialogCell:drawNameLock	Z
    //   1721: aload_0
    //   1722: ldc_w 267
    //   1725: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1728: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   1731: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   1734: ifne +444 -> 2178
    //   1737: aload_0
    //   1738: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1741: i2f
    //   1742: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1745: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   1748: aload_0
    //   1749: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   1752: iconst_4
    //   1753: iadd
    //   1754: i2f
    //   1755: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   1758: getstatic 283	org/telegram/ui/ActionBar/Theme:dialogs_lockDrawable	Landroid/graphics/drawable/Drawable;
    //   1761: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   1764: iadd
    //   1765: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   1768: aload_0
    //   1769: getfield 511	org/telegram/ui/Cells/DialogCell:lastMessageDate	I
    //   1772: istore 6
    //   1774: iload 6
    //   1776: istore 5
    //   1778: aload_0
    //   1779: getfield 511	org/telegram/ui/Cells/DialogCell:lastMessageDate	I
    //   1782: ifne +26 -> 1808
    //   1785: iload 6
    //   1787: istore 5
    //   1789: aload_0
    //   1790: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   1793: ifnull +15 -> 1808
    //   1796: aload_0
    //   1797: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   1800: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   1803: getfield 518	org/telegram/tgnet/TLRPC$Message:date	I
    //   1806: istore 5
    //   1808: aload_0
    //   1809: getfield 206	org/telegram/ui/Cells/DialogCell:isDialogCell	Z
    //   1812: ifeq +764 -> 2576
    //   1815: aload_0
    //   1816: aload_0
    //   1817: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   1820: invokestatic 523	org/telegram/messenger/DataQuery:getInstance	(I)Lorg/telegram/messenger/DataQuery;
    //   1823: aload_0
    //   1824: getfield 212	org/telegram/ui/Cells/DialogCell:currentDialogId	J
    //   1827: invokevirtual 527	org/telegram/messenger/DataQuery:getDraft	(J)Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1830: putfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1833: aload_0
    //   1834: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1837: ifnull +45 -> 1882
    //   1840: aload_0
    //   1841: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1844: getfield 532	org/telegram/tgnet/TLRPC$DraftMessage:message	Ljava/lang/String;
    //   1847: invokestatic 536	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   1850: ifeq +13 -> 1863
    //   1853: aload_0
    //   1854: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1857: getfield 539	org/telegram/tgnet/TLRPC$DraftMessage:reply_to_msg_id	I
    //   1860: ifeq +102 -> 1962
    //   1863: iload 5
    //   1865: aload_0
    //   1866: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1869: getfield 540	org/telegram/tgnet/TLRPC$DraftMessage:date	I
    //   1872: if_icmple +10 -> 1882
    //   1875: aload_0
    //   1876: getfield 542	org/telegram/ui/Cells/DialogCell:unreadCount	I
    //   1879: ifne +83 -> 1962
    //   1882: aload_0
    //   1883: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1886: invokestatic 548	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   1889: ifeq +46 -> 1935
    //   1892: aload_0
    //   1893: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1896: getfield 553	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   1899: ifne +36 -> 1935
    //   1902: aload_0
    //   1903: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1906: getfield 556	org/telegram/tgnet/TLRPC$Chat:creator	Z
    //   1909: ifne +26 -> 1935
    //   1912: aload_0
    //   1913: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1916: getfield 560	org/telegram/tgnet/TLRPC$Chat:admin_rights	Lorg/telegram/tgnet/TLRPC$TL_channelAdminRights;
    //   1919: ifnull +43 -> 1962
    //   1922: aload_0
    //   1923: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1926: getfield 560	org/telegram/tgnet/TLRPC$Chat:admin_rights	Lorg/telegram/tgnet/TLRPC$TL_channelAdminRights;
    //   1929: getfield 565	org/telegram/tgnet/TLRPC$TL_channelAdminRights:post_messages	Z
    //   1932: ifeq +30 -> 1962
    //   1935: aload_0
    //   1936: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1939: ifnull +28 -> 1967
    //   1942: aload_0
    //   1943: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1946: getfield 568	org/telegram/tgnet/TLRPC$Chat:left	Z
    //   1949: ifne +13 -> 1962
    //   1952: aload_0
    //   1953: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   1956: getfield 571	org/telegram/tgnet/TLRPC$Chat:kicked	Z
    //   1959: ifeq +8 -> 1967
    //   1962: aload_0
    //   1963: aconst_null
    //   1964: putfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   1967: aload 12
    //   1969: ifnull +615 -> 2584
    //   1972: aload 12
    //   1974: astore 13
    //   1976: aload_0
    //   1977: aload 12
    //   1979: putfield 120	org/telegram/ui/Cells/DialogCell:lastPrintString	Ljava/lang/CharSequence;
    //   1982: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   1985: astore 14
    //   1987: iload 4
    //   1989: istore 7
    //   1991: aload 13
    //   1993: astore 12
    //   1995: iload 9
    //   1997: istore 6
    //   1999: aload 14
    //   2001: astore 13
    //   2003: iload 8
    //   2005: istore 5
    //   2007: aload_0
    //   2008: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2011: ifnull +2457 -> 4468
    //   2014: aload_0
    //   2015: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2018: getfield 540	org/telegram/tgnet/TLRPC$DraftMessage:date	I
    //   2021: i2l
    //   2022: invokestatic 358	org/telegram/messenger/LocaleController:stringForMessageListDate	(J)Ljava/lang/String;
    //   2025: astore 15
    //   2027: aload_0
    //   2028: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   2031: ifnonnull +2487 -> 4518
    //   2034: aload_0
    //   2035: iconst_0
    //   2036: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   2039: aload_0
    //   2040: iconst_0
    //   2041: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   2044: aload_0
    //   2045: iconst_0
    //   2046: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   2049: aload_0
    //   2050: iconst_0
    //   2051: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   2054: aload_0
    //   2055: iconst_0
    //   2056: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   2059: aload_0
    //   2060: iconst_0
    //   2061: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   2064: aload 21
    //   2066: astore 17
    //   2068: aload_0
    //   2069: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2072: ifnull +2790 -> 4862
    //   2075: aload_0
    //   2076: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2079: getfield 576	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   2082: astore 16
    //   2084: aload 19
    //   2086: astore 21
    //   2088: aload 15
    //   2090: astore 25
    //   2092: iload 5
    //   2094: istore 7
    //   2096: aload 18
    //   2098: astore 23
    //   2100: aload 13
    //   2102: astore 22
    //   2104: aload 21
    //   2106: astore 19
    //   2108: iload 6
    //   2110: istore 8
    //   2112: aload 17
    //   2114: astore 24
    //   2116: aload 12
    //   2118: astore 14
    //   2120: aload 16
    //   2122: astore 20
    //   2124: aload 16
    //   2126: invokevirtual 331	java/lang/String:length	()I
    //   2129: ifne -1569 -> 560
    //   2132: ldc_w 578
    //   2135: ldc_w 579
    //   2138: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2141: astore 20
    //   2143: aload 15
    //   2145: astore 25
    //   2147: iload 5
    //   2149: istore 7
    //   2151: aload 18
    //   2153: astore 23
    //   2155: aload 13
    //   2157: astore 22
    //   2159: aload 21
    //   2161: astore 19
    //   2163: iload 6
    //   2165: istore 8
    //   2167: aload 17
    //   2169: astore 24
    //   2171: aload 12
    //   2173: astore 14
    //   2175: goto -1615 -> 560
    //   2178: aload_0
    //   2179: aload_0
    //   2180: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   2183: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2186: i2f
    //   2187: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2190: isub
    //   2191: getstatic 283	org/telegram/ui/ActionBar/Theme:dialogs_lockDrawable	Landroid/graphics/drawable/Drawable;
    //   2194: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2197: isub
    //   2198: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   2201: aload_0
    //   2202: ldc_w 421
    //   2205: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2208: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2211: goto -443 -> 1768
    //   2214: aload_0
    //   2215: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2218: ifnull +209 -> 2427
    //   2221: aload_0
    //   2222: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2225: getfield 582	org/telegram/tgnet/TLRPC$Chat:id	I
    //   2228: iflt +23 -> 2251
    //   2231: aload_0
    //   2232: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2235: invokestatic 548	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   2238: ifeq +94 -> 2332
    //   2241: aload_0
    //   2242: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2245: getfield 553	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   2248: ifne +84 -> 2332
    //   2251: aload_0
    //   2252: iconst_1
    //   2253: putfield 231	org/telegram/ui/Cells/DialogCell:drawNameBroadcast	Z
    //   2256: aload_0
    //   2257: ldc_w 267
    //   2260: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2263: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   2266: aload_0
    //   2267: aload_0
    //   2268: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   2271: getfield 583	org/telegram/tgnet/TLRPC$Chat:verified	Z
    //   2274: putfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   2277: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   2280: ifne +81 -> 2361
    //   2283: aload_0
    //   2284: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2287: i2f
    //   2288: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2291: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   2294: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2297: iconst_4
    //   2298: iadd
    //   2299: i2f
    //   2300: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2303: istore 6
    //   2305: aload_0
    //   2306: getfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   2309: ifeq +41 -> 2350
    //   2312: getstatic 504	org/telegram/ui/ActionBar/Theme:dialogs_groupDrawable	Landroid/graphics/drawable/Drawable;
    //   2315: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2318: istore 5
    //   2320: aload_0
    //   2321: iload 5
    //   2323: iload 6
    //   2325: iadd
    //   2326: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2329: goto -561 -> 1768
    //   2332: aload_0
    //   2333: iconst_1
    //   2334: putfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   2337: aload_0
    //   2338: ldc_w 501
    //   2341: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2344: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   2347: goto -81 -> 2266
    //   2350: getstatic 507	org/telegram/ui/ActionBar/Theme:dialogs_broadcastDrawable	Landroid/graphics/drawable/Drawable;
    //   2353: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2356: istore 5
    //   2358: goto -38 -> 2320
    //   2361: aload_0
    //   2362: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   2365: istore 6
    //   2367: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2370: i2f
    //   2371: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2374: istore 7
    //   2376: aload_0
    //   2377: getfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   2380: ifeq +36 -> 2416
    //   2383: getstatic 504	org/telegram/ui/ActionBar/Theme:dialogs_groupDrawable	Landroid/graphics/drawable/Drawable;
    //   2386: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2389: istore 5
    //   2391: aload_0
    //   2392: iload 6
    //   2394: iload 7
    //   2396: isub
    //   2397: iload 5
    //   2399: isub
    //   2400: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   2403: aload_0
    //   2404: ldc_w 421
    //   2407: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2410: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2413: goto -645 -> 1768
    //   2416: getstatic 507	org/telegram/ui/ActionBar/Theme:dialogs_broadcastDrawable	Landroid/graphics/drawable/Drawable;
    //   2419: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2422: istore 5
    //   2424: goto -33 -> 2391
    //   2427: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   2430: ifne +97 -> 2527
    //   2433: aload_0
    //   2434: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2437: i2f
    //   2438: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2441: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2444: aload_0
    //   2445: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2448: ifnull -680 -> 1768
    //   2451: aload_0
    //   2452: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2455: getfield 588	org/telegram/tgnet/TLRPC$User:bot	Z
    //   2458: ifeq +55 -> 2513
    //   2461: aload_0
    //   2462: iconst_1
    //   2463: putfield 235	org/telegram/ui/Cells/DialogCell:drawNameBot	Z
    //   2466: aload_0
    //   2467: ldc_w 267
    //   2470: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2473: putfield 269	org/telegram/ui/Cells/DialogCell:nameLockTop	I
    //   2476: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   2479: ifne +61 -> 2540
    //   2482: aload_0
    //   2483: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2486: i2f
    //   2487: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2490: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   2493: aload_0
    //   2494: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2497: iconst_4
    //   2498: iadd
    //   2499: i2f
    //   2500: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2503: getstatic 591	org/telegram/ui/ActionBar/Theme:dialogs_botDrawable	Landroid/graphics/drawable/Drawable;
    //   2506: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2509: iadd
    //   2510: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2513: aload_0
    //   2514: aload_0
    //   2515: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2518: getfield 592	org/telegram/tgnet/TLRPC$User:verified	Z
    //   2521: putfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   2524: goto -756 -> 1768
    //   2527: aload_0
    //   2528: ldc_w 421
    //   2531: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2534: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2537: goto -93 -> 2444
    //   2540: aload_0
    //   2541: aload_0
    //   2542: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   2545: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   2548: i2f
    //   2549: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2552: isub
    //   2553: getstatic 591	org/telegram/ui/ActionBar/Theme:dialogs_botDrawable	Landroid/graphics/drawable/Drawable;
    //   2556: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   2559: isub
    //   2560: putfield 279	org/telegram/ui/Cells/DialogCell:nameLockLeft	I
    //   2563: aload_0
    //   2564: ldc_w 421
    //   2567: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2570: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   2573: goto -60 -> 2513
    //   2576: aload_0
    //   2577: aconst_null
    //   2578: putfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2581: goto -614 -> 1967
    //   2584: aload_0
    //   2585: aconst_null
    //   2586: putfield 120	org/telegram/ui/Cells/DialogCell:lastPrintString	Ljava/lang/CharSequence;
    //   2589: aload_0
    //   2590: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2593: ifnull +218 -> 2811
    //   2596: iconst_0
    //   2597: istore 5
    //   2599: aload_0
    //   2600: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2603: getfield 532	org/telegram/tgnet/TLRPC$DraftMessage:message	Ljava/lang/String;
    //   2606: invokestatic 536	android/text/TextUtils:isEmpty	(Ljava/lang/CharSequence;)Z
    //   2609: ifeq +62 -> 2671
    //   2612: ldc_w 594
    //   2615: ldc_w 595
    //   2618: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2621: astore 13
    //   2623: aload 13
    //   2625: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   2628: astore 12
    //   2630: aload 12
    //   2632: new 320	android/text/style/ForegroundColorSpan
    //   2635: dup
    //   2636: ldc_w 597
    //   2639: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   2642: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   2645: iconst_0
    //   2646: aload 13
    //   2648: invokevirtual 331	java/lang/String:length	()I
    //   2651: bipush 33
    //   2653: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   2656: aload 14
    //   2658: astore 13
    //   2660: iload 9
    //   2662: istore 6
    //   2664: iload 4
    //   2666: istore 7
    //   2668: goto -661 -> 2007
    //   2671: aload_0
    //   2672: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   2675: getfield 532	org/telegram/tgnet/TLRPC$DraftMessage:message	Ljava/lang/String;
    //   2678: astore 13
    //   2680: aload 13
    //   2682: astore 12
    //   2684: aload 13
    //   2686: invokevirtual 331	java/lang/String:length	()I
    //   2689: sipush 150
    //   2692: if_icmple +14 -> 2706
    //   2695: aload 13
    //   2697: iconst_0
    //   2698: sipush 150
    //   2701: invokevirtual 481	java/lang/String:substring	(II)Ljava/lang/String;
    //   2704: astore 12
    //   2706: ldc_w 594
    //   2709: ldc_w 595
    //   2712: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2715: astore 13
    //   2717: aload 15
    //   2719: iconst_2
    //   2720: anewarray 306	java/lang/Object
    //   2723: dup
    //   2724: iconst_0
    //   2725: aload 13
    //   2727: aastore
    //   2728: dup
    //   2729: iconst_1
    //   2730: aload 12
    //   2732: bipush 10
    //   2734: bipush 32
    //   2736: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   2739: aastore
    //   2740: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   2743: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   2746: astore 12
    //   2748: aload 12
    //   2750: new 320	android/text/style/ForegroundColorSpan
    //   2753: dup
    //   2754: ldc_w 597
    //   2757: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   2760: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   2763: iconst_0
    //   2764: aload 13
    //   2766: invokevirtual 331	java/lang/String:length	()I
    //   2769: iconst_1
    //   2770: iadd
    //   2771: bipush 33
    //   2773: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   2776: aload 12
    //   2778: getstatic 227	org/telegram/ui/ActionBar/Theme:dialogs_messagePaint	Landroid/text/TextPaint;
    //   2781: invokevirtual 344	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   2784: ldc_w 345
    //   2787: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   2790: iconst_0
    //   2791: invokestatic 351	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   2794: astore 12
    //   2796: aload 14
    //   2798: astore 13
    //   2800: iload 9
    //   2802: istore 6
    //   2804: iload 4
    //   2806: istore 7
    //   2808: goto -801 -> 2007
    //   2811: aload_0
    //   2812: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   2815: ifnonnull +405 -> 3220
    //   2818: iload 8
    //   2820: istore 5
    //   2822: aload 14
    //   2824: astore 13
    //   2826: iload 9
    //   2828: istore 6
    //   2830: aload 24
    //   2832: astore 12
    //   2834: iload 4
    //   2836: istore 7
    //   2838: aload_0
    //   2839: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   2842: ifnull -835 -> 2007
    //   2845: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   2848: astore 14
    //   2850: aload_0
    //   2851: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   2854: instanceof 599
    //   2857: ifeq +33 -> 2890
    //   2860: ldc_w 601
    //   2863: ldc_w 602
    //   2866: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   2869: astore 12
    //   2871: iload 8
    //   2873: istore 5
    //   2875: aload 14
    //   2877: astore 13
    //   2879: iload 9
    //   2881: istore 6
    //   2883: iload 4
    //   2885: istore 7
    //   2887: goto -880 -> 2007
    //   2890: aload_0
    //   2891: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   2894: instanceof 604
    //   2897: ifeq +103 -> 3000
    //   2900: aload_0
    //   2901: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2904: ifnull +57 -> 2961
    //   2907: aload_0
    //   2908: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2911: getfield 607	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   2914: ifnull +47 -> 2961
    //   2917: ldc_w 609
    //   2920: ldc_w 610
    //   2923: iconst_1
    //   2924: anewarray 306	java/lang/Object
    //   2927: dup
    //   2928: iconst_0
    //   2929: aload_0
    //   2930: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   2933: getfield 607	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   2936: aastore
    //   2937: invokestatic 614	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   2940: astore 12
    //   2942: iload 8
    //   2944: istore 5
    //   2946: aload 14
    //   2948: astore 13
    //   2950: iload 9
    //   2952: istore 6
    //   2954: iload 4
    //   2956: istore 7
    //   2958: goto -951 -> 2007
    //   2961: ldc_w 609
    //   2964: ldc_w 610
    //   2967: iconst_1
    //   2968: anewarray 306	java/lang/Object
    //   2971: dup
    //   2972: iconst_0
    //   2973: ldc -52
    //   2975: aastore
    //   2976: invokestatic 614	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   2979: astore 12
    //   2981: iload 8
    //   2983: istore 5
    //   2985: aload 14
    //   2987: astore 13
    //   2989: iload 9
    //   2991: istore 6
    //   2993: iload 4
    //   2995: istore 7
    //   2997: goto -990 -> 2007
    //   3000: aload_0
    //   3001: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   3004: instanceof 616
    //   3007: ifeq +33 -> 3040
    //   3010: ldc_w 618
    //   3013: ldc_w 619
    //   3016: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   3019: astore 12
    //   3021: iload 8
    //   3023: istore 5
    //   3025: aload 14
    //   3027: astore 13
    //   3029: iload 9
    //   3031: istore 6
    //   3033: iload 4
    //   3035: istore 7
    //   3037: goto -1030 -> 2007
    //   3040: iload 8
    //   3042: istore 5
    //   3044: aload 14
    //   3046: astore 13
    //   3048: iload 9
    //   3050: istore 6
    //   3052: aload 24
    //   3054: astore 12
    //   3056: iload 4
    //   3058: istore 7
    //   3060: aload_0
    //   3061: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   3064: instanceof 621
    //   3067: ifeq -1060 -> 2007
    //   3070: aload_0
    //   3071: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   3074: getfield 626	org/telegram/tgnet/TLRPC$EncryptedChat:admin_id	I
    //   3077: aload_0
    //   3078: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   3081: invokestatic 629	org/telegram/messenger/UserConfig:getInstance	(I)Lorg/telegram/messenger/UserConfig;
    //   3084: invokevirtual 632	org/telegram/messenger/UserConfig:getClientUserId	()I
    //   3087: if_icmpne +103 -> 3190
    //   3090: aload_0
    //   3091: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   3094: ifnull +57 -> 3151
    //   3097: aload_0
    //   3098: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   3101: getfield 607	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   3104: ifnull +47 -> 3151
    //   3107: ldc_w 634
    //   3110: ldc_w 635
    //   3113: iconst_1
    //   3114: anewarray 306	java/lang/Object
    //   3117: dup
    //   3118: iconst_0
    //   3119: aload_0
    //   3120: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   3123: getfield 607	org/telegram/tgnet/TLRPC$User:first_name	Ljava/lang/String;
    //   3126: aastore
    //   3127: invokestatic 614	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   3130: astore 12
    //   3132: iload 8
    //   3134: istore 5
    //   3136: aload 14
    //   3138: astore 13
    //   3140: iload 9
    //   3142: istore 6
    //   3144: iload 4
    //   3146: istore 7
    //   3148: goto -1141 -> 2007
    //   3151: ldc_w 634
    //   3154: ldc_w 635
    //   3157: iconst_1
    //   3158: anewarray 306	java/lang/Object
    //   3161: dup
    //   3162: iconst_0
    //   3163: ldc -52
    //   3165: aastore
    //   3166: invokestatic 614	org/telegram/messenger/LocaleController:formatString	(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;
    //   3169: astore 12
    //   3171: iload 8
    //   3173: istore 5
    //   3175: aload 14
    //   3177: astore 13
    //   3179: iload 9
    //   3181: istore 6
    //   3183: iload 4
    //   3185: istore 7
    //   3187: goto -1180 -> 2007
    //   3190: ldc_w 637
    //   3193: ldc_w 638
    //   3196: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   3199: astore 12
    //   3201: iload 8
    //   3203: istore 5
    //   3205: aload 14
    //   3207: astore 13
    //   3209: iload 9
    //   3211: istore 6
    //   3213: iload 4
    //   3215: istore 7
    //   3217: goto -1210 -> 2007
    //   3220: aconst_null
    //   3221: astore 13
    //   3223: aconst_null
    //   3224: astore 12
    //   3226: aload_0
    //   3227: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3230: invokevirtual 641	org/telegram/messenger/MessageObject:isFromUser	()Z
    //   3233: ifeq +74 -> 3307
    //   3236: aload_0
    //   3237: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   3240: invokestatic 184	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3243: aload_0
    //   3244: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3247: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3250: getfield 644	org/telegram/tgnet/TLRPC$Message:from_id	I
    //   3253: invokestatic 370	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3256: invokevirtual 648	org/telegram/messenger/MessagesController:getUser	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$User;
    //   3259: astore 13
    //   3261: aload_0
    //   3262: getfield 178	org/telegram/ui/Cells/DialogCell:dialogsType	I
    //   3265: iconst_3
    //   3266: if_icmpne +72 -> 3338
    //   3269: aload_0
    //   3270: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   3273: invokestatic 245	org/telegram/messenger/UserObject:isUserSelf	(Lorg/telegram/tgnet/TLRPC$User;)Z
    //   3276: ifeq +62 -> 3338
    //   3279: ldc_w 650
    //   3282: ldc_w 651
    //   3285: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   3288: astore 12
    //   3290: iconst_0
    //   3291: istore 7
    //   3293: iconst_0
    //   3294: istore 6
    //   3296: iload 8
    //   3298: istore 5
    //   3300: aload 14
    //   3302: astore 13
    //   3304: goto -1297 -> 2007
    //   3307: aload_0
    //   3308: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   3311: invokestatic 184	org/telegram/messenger/MessagesController:getInstance	(I)Lorg/telegram/messenger/MessagesController;
    //   3314: aload_0
    //   3315: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3318: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3321: getfield 655	org/telegram/tgnet/TLRPC$Message:to_id	Lorg/telegram/tgnet/TLRPC$Peer;
    //   3324: getfield 660	org/telegram/tgnet/TLRPC$Peer:channel_id	I
    //   3327: invokestatic 370	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   3330: invokevirtual 664	org/telegram/messenger/MessagesController:getChat	(Ljava/lang/Integer;)Lorg/telegram/tgnet/TLRPC$Chat;
    //   3333: astore 12
    //   3335: goto -74 -> 3261
    //   3338: aload_0
    //   3339: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3342: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3345: instanceof 666
    //   3348: ifeq +68 -> 3416
    //   3351: aload_0
    //   3352: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   3355: invokestatic 548	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   3358: ifeq +46 -> 3404
    //   3361: aload_0
    //   3362: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3365: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3368: getfield 670	org/telegram/tgnet/TLRPC$Message:action	Lorg/telegram/tgnet/TLRPC$MessageAction;
    //   3371: instanceof 672
    //   3374: ifeq +30 -> 3404
    //   3377: ldc -52
    //   3379: astore 12
    //   3381: iconst_0
    //   3382: istore 4
    //   3384: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   3387: astore 13
    //   3389: iload 8
    //   3391: istore 5
    //   3393: iload 9
    //   3395: istore 6
    //   3397: iload 4
    //   3399: istore 7
    //   3401: goto -1394 -> 2007
    //   3404: aload_0
    //   3405: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3408: getfield 259	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   3411: astore 12
    //   3413: goto -29 -> 3384
    //   3416: aload_0
    //   3417: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   3420: ifnull +649 -> 4069
    //   3423: aload_0
    //   3424: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   3427: getfield 582	org/telegram/tgnet/TLRPC$Chat:id	I
    //   3430: ifle +639 -> 4069
    //   3433: aload 12
    //   3435: ifnonnull +634 -> 4069
    //   3438: aload_0
    //   3439: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3442: invokevirtual 675	org/telegram/messenger/MessageObject:isOutOwner	()Z
    //   3445: ifeq +169 -> 3614
    //   3448: ldc_w 293
    //   3451: ldc_w 294
    //   3454: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   3457: astore 13
    //   3459: iconst_0
    //   3460: istore 5
    //   3462: aload_0
    //   3463: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3466: getfield 678	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   3469: ifnull +199 -> 3668
    //   3472: aload_0
    //   3473: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3476: getfield 678	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   3479: invokeinterface 477 1 0
    //   3484: astore 23
    //   3486: aload 23
    //   3488: astore 12
    //   3490: aload 23
    //   3492: invokevirtual 331	java/lang/String:length	()I
    //   3495: sipush 150
    //   3498: if_icmple +14 -> 3512
    //   3501: aload 23
    //   3503: iconst_0
    //   3504: sipush 150
    //   3507: invokevirtual 481	java/lang/String:substring	(II)Ljava/lang/String;
    //   3510: astore 12
    //   3512: aload 15
    //   3514: iconst_2
    //   3515: anewarray 306	java/lang/Object
    //   3518: dup
    //   3519: iconst_0
    //   3520: aload 13
    //   3522: aastore
    //   3523: dup
    //   3524: iconst_1
    //   3525: aload 12
    //   3527: bipush 10
    //   3529: bipush 32
    //   3531: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   3534: aastore
    //   3535: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3538: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3541: astore 12
    //   3543: aload 12
    //   3545: invokevirtual 332	android/text/SpannableStringBuilder:length	()I
    //   3548: ifle +31 -> 3579
    //   3551: aload 12
    //   3553: new 320	android/text/style/ForegroundColorSpan
    //   3556: dup
    //   3557: ldc_w 338
    //   3560: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   3563: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   3566: iconst_0
    //   3567: aload 13
    //   3569: invokevirtual 331	java/lang/String:length	()I
    //   3572: iconst_1
    //   3573: iadd
    //   3574: bipush 33
    //   3576: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   3579: aload 12
    //   3581: getstatic 227	org/telegram/ui/ActionBar/Theme:dialogs_messagePaint	Landroid/text/TextPaint;
    //   3584: invokevirtual 344	android/text/TextPaint:getFontMetricsInt	()Landroid/graphics/Paint$FontMetricsInt;
    //   3587: ldc_w 345
    //   3590: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   3593: iconst_0
    //   3594: invokestatic 351	org/telegram/messenger/Emoji:replaceEmoji	(Ljava/lang/CharSequence;Landroid/graphics/Paint$FontMetricsInt;IZ)Ljava/lang/CharSequence;
    //   3597: astore 12
    //   3599: aload 14
    //   3601: astore 13
    //   3603: iload 9
    //   3605: istore 6
    //   3607: iload 4
    //   3609: istore 7
    //   3611: goto -1604 -> 2007
    //   3614: aload 13
    //   3616: ifnull +21 -> 3637
    //   3619: aload 13
    //   3621: invokestatic 682	org/telegram/messenger/UserObject:getFirstName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   3624: ldc_w 684
    //   3627: ldc -52
    //   3629: invokevirtual 687	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   3632: astore 13
    //   3634: goto -175 -> 3459
    //   3637: aload 12
    //   3639: ifnull +21 -> 3660
    //   3642: aload 12
    //   3644: getfield 576	org/telegram/tgnet/TLRPC$Chat:title	Ljava/lang/String;
    //   3647: ldc_w 684
    //   3650: ldc -52
    //   3652: invokevirtual 687	java/lang/String:replace	(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   3655: astore 13
    //   3657: goto -198 -> 3459
    //   3660: ldc_w 689
    //   3663: astore 13
    //   3665: goto -206 -> 3459
    //   3668: aload_0
    //   3669: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3672: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3675: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3678: ifnull +296 -> 3974
    //   3681: aload_0
    //   3682: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3685: invokevirtual 696	org/telegram/messenger/MessageObject:isMediaEmpty	()Z
    //   3688: ifne +286 -> 3974
    //   3691: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   3694: astore 14
    //   3696: aload_0
    //   3697: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3700: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3703: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3706: instanceof 698
    //   3709: ifeq +127 -> 3836
    //   3712: getstatic 250	android/os/Build$VERSION:SDK_INT	I
    //   3715: bipush 18
    //   3717: if_icmplt +77 -> 3794
    //   3720: ldc_w 700
    //   3723: iconst_2
    //   3724: anewarray 306	java/lang/Object
    //   3727: dup
    //   3728: iconst_0
    //   3729: aload 13
    //   3731: aastore
    //   3732: dup
    //   3733: iconst_1
    //   3734: aload_0
    //   3735: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3738: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3741: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3744: getfield 706	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   3747: getfield 709	org/telegram/tgnet/TLRPC$TL_game:title	Ljava/lang/String;
    //   3750: aastore
    //   3751: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3754: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3757: astore 12
    //   3759: aload 12
    //   3761: new 320	android/text/style/ForegroundColorSpan
    //   3764: dup
    //   3765: ldc_w 322
    //   3768: invokestatic 326	org/telegram/ui/ActionBar/Theme:getColor	(Ljava/lang/String;)I
    //   3771: invokespecial 328	android/text/style/ForegroundColorSpan:<init>	(I)V
    //   3774: aload 13
    //   3776: invokevirtual 331	java/lang/String:length	()I
    //   3779: iconst_2
    //   3780: iadd
    //   3781: aload 12
    //   3783: invokevirtual 332	android/text/SpannableStringBuilder:length	()I
    //   3786: bipush 33
    //   3788: invokevirtual 336	android/text/SpannableStringBuilder:setSpan	(Ljava/lang/Object;III)V
    //   3791: goto -248 -> 3543
    //   3794: ldc_w 711
    //   3797: iconst_2
    //   3798: anewarray 306	java/lang/Object
    //   3801: dup
    //   3802: iconst_0
    //   3803: aload 13
    //   3805: aastore
    //   3806: dup
    //   3807: iconst_1
    //   3808: aload_0
    //   3809: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3812: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3815: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   3818: getfield 706	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   3821: getfield 709	org/telegram/tgnet/TLRPC$TL_game:title	Ljava/lang/String;
    //   3824: aastore
    //   3825: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3828: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3831: astore 12
    //   3833: goto -74 -> 3759
    //   3836: aload_0
    //   3837: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3840: getfield 712	org/telegram/messenger/MessageObject:type	I
    //   3843: bipush 14
    //   3845: if_icmpne +97 -> 3942
    //   3848: getstatic 250	android/os/Build$VERSION:SDK_INT	I
    //   3851: bipush 18
    //   3853: if_icmplt +46 -> 3899
    //   3856: ldc_w 714
    //   3859: iconst_3
    //   3860: anewarray 306	java/lang/Object
    //   3863: dup
    //   3864: iconst_0
    //   3865: aload 13
    //   3867: aastore
    //   3868: dup
    //   3869: iconst_1
    //   3870: aload_0
    //   3871: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3874: invokevirtual 717	org/telegram/messenger/MessageObject:getMusicAuthor	()Ljava/lang/String;
    //   3877: aastore
    //   3878: dup
    //   3879: iconst_2
    //   3880: aload_0
    //   3881: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3884: invokevirtual 720	org/telegram/messenger/MessageObject:getMusicTitle	()Ljava/lang/String;
    //   3887: aastore
    //   3888: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3891: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3894: astore 12
    //   3896: goto -137 -> 3759
    //   3899: ldc_w 722
    //   3902: iconst_3
    //   3903: anewarray 306	java/lang/Object
    //   3906: dup
    //   3907: iconst_0
    //   3908: aload 13
    //   3910: aastore
    //   3911: dup
    //   3912: iconst_1
    //   3913: aload_0
    //   3914: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3917: invokevirtual 717	org/telegram/messenger/MessageObject:getMusicAuthor	()Ljava/lang/String;
    //   3920: aastore
    //   3921: dup
    //   3922: iconst_2
    //   3923: aload_0
    //   3924: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3927: invokevirtual 720	org/telegram/messenger/MessageObject:getMusicTitle	()Ljava/lang/String;
    //   3930: aastore
    //   3931: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3934: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3937: astore 12
    //   3939: goto -180 -> 3759
    //   3942: aload 15
    //   3944: iconst_2
    //   3945: anewarray 306	java/lang/Object
    //   3948: dup
    //   3949: iconst_0
    //   3950: aload 13
    //   3952: aastore
    //   3953: dup
    //   3954: iconst_1
    //   3955: aload_0
    //   3956: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3959: getfield 259	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   3962: aastore
    //   3963: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   3966: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   3969: astore 12
    //   3971: goto -212 -> 3759
    //   3974: aload_0
    //   3975: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3978: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3981: getfield 723	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   3984: ifnull +75 -> 4059
    //   3987: aload_0
    //   3988: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   3991: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   3994: getfield 723	org/telegram/tgnet/TLRPC$Message:message	Ljava/lang/String;
    //   3997: astore 23
    //   3999: aload 23
    //   4001: astore 12
    //   4003: aload 23
    //   4005: invokevirtual 331	java/lang/String:length	()I
    //   4008: sipush 150
    //   4011: if_icmple +14 -> 4025
    //   4014: aload 23
    //   4016: iconst_0
    //   4017: sipush 150
    //   4020: invokevirtual 481	java/lang/String:substring	(II)Ljava/lang/String;
    //   4023: astore 12
    //   4025: aload 15
    //   4027: iconst_2
    //   4028: anewarray 306	java/lang/Object
    //   4031: dup
    //   4032: iconst_0
    //   4033: aload 13
    //   4035: aastore
    //   4036: dup
    //   4037: iconst_1
    //   4038: aload 12
    //   4040: bipush 10
    //   4042: bipush 32
    //   4044: invokevirtual 443	java/lang/String:replace	(CC)Ljava/lang/String;
    //   4047: aastore
    //   4048: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   4051: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   4054: astore 12
    //   4056: goto -513 -> 3543
    //   4059: ldc -52
    //   4061: invokestatic 318	android/text/SpannableStringBuilder:valueOf	(Ljava/lang/CharSequence;)Landroid/text/SpannableStringBuilder;
    //   4064: astore 12
    //   4066: goto -523 -> 3543
    //   4069: aload_0
    //   4070: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4073: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4076: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4079: instanceof 725
    //   4082: ifeq +68 -> 4150
    //   4085: aload_0
    //   4086: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4089: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4092: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4095: getfield 729	org/telegram/tgnet/TLRPC$MessageMedia:photo	Lorg/telegram/tgnet/TLRPC$Photo;
    //   4098: instanceof 731
    //   4101: ifeq +49 -> 4150
    //   4104: aload_0
    //   4105: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4108: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4111: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4114: getfield 734	org/telegram/tgnet/TLRPC$MessageMedia:ttl_seconds	I
    //   4117: ifeq +33 -> 4150
    //   4120: ldc_w 736
    //   4123: ldc_w 737
    //   4126: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   4129: astore 12
    //   4131: iload 8
    //   4133: istore 5
    //   4135: aload 14
    //   4137: astore 13
    //   4139: iload 9
    //   4141: istore 6
    //   4143: iload 4
    //   4145: istore 7
    //   4147: goto -2140 -> 2007
    //   4150: aload_0
    //   4151: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4154: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4157: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4160: instanceof 739
    //   4163: ifeq +68 -> 4231
    //   4166: aload_0
    //   4167: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4170: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4173: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4176: getfield 743	org/telegram/tgnet/TLRPC$MessageMedia:document	Lorg/telegram/tgnet/TLRPC$Document;
    //   4179: instanceof 745
    //   4182: ifeq +49 -> 4231
    //   4185: aload_0
    //   4186: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4189: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4192: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4195: getfield 734	org/telegram/tgnet/TLRPC$MessageMedia:ttl_seconds	I
    //   4198: ifeq +33 -> 4231
    //   4201: ldc_w 747
    //   4204: ldc_w 748
    //   4207: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   4210: astore 12
    //   4212: iload 8
    //   4214: istore 5
    //   4216: aload 14
    //   4218: astore 13
    //   4220: iload 9
    //   4222: istore 6
    //   4224: iload 4
    //   4226: istore 7
    //   4228: goto -2221 -> 2007
    //   4231: aload_0
    //   4232: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4235: getfield 678	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   4238: ifnull +31 -> 4269
    //   4241: aload_0
    //   4242: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4245: getfield 678	org/telegram/messenger/MessageObject:caption	Ljava/lang/CharSequence;
    //   4248: astore 12
    //   4250: iload 8
    //   4252: istore 5
    //   4254: aload 14
    //   4256: astore 13
    //   4258: iload 9
    //   4260: istore 6
    //   4262: iload 4
    //   4264: istore 7
    //   4266: goto -2259 -> 2007
    //   4269: aload_0
    //   4270: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4273: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4276: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4279: instanceof 698
    //   4282: ifeq +127 -> 4409
    //   4285: new 750	java/lang/StringBuilder
    //   4288: dup
    //   4289: invokespecial 751	java/lang/StringBuilder:<init>	()V
    //   4292: ldc_w 753
    //   4295: invokevirtual 757	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4298: aload_0
    //   4299: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4302: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4305: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4308: getfield 706	org/telegram/tgnet/TLRPC$MessageMedia:game	Lorg/telegram/tgnet/TLRPC$TL_game;
    //   4311: getfield 709	org/telegram/tgnet/TLRPC$TL_game:title	Ljava/lang/String;
    //   4314: invokevirtual 757	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   4317: invokevirtual 758	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   4320: astore 15
    //   4322: iload 8
    //   4324: istore 5
    //   4326: aload 14
    //   4328: astore 13
    //   4330: iload 9
    //   4332: istore 6
    //   4334: aload 15
    //   4336: astore 12
    //   4338: iload 4
    //   4340: istore 7
    //   4342: aload_0
    //   4343: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4346: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4349: getfield 693	org/telegram/tgnet/TLRPC$Message:media	Lorg/telegram/tgnet/TLRPC$MessageMedia;
    //   4352: ifnull -2345 -> 2007
    //   4355: iload 8
    //   4357: istore 5
    //   4359: aload 14
    //   4361: astore 13
    //   4363: iload 9
    //   4365: istore 6
    //   4367: aload 15
    //   4369: astore 12
    //   4371: iload 4
    //   4373: istore 7
    //   4375: aload_0
    //   4376: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4379: invokevirtual 696	org/telegram/messenger/MessageObject:isMediaEmpty	()Z
    //   4382: ifne -2375 -> 2007
    //   4385: getstatic 304	org/telegram/ui/ActionBar/Theme:dialogs_messagePrintingPaint	Landroid/text/TextPaint;
    //   4388: astore 13
    //   4390: iload 8
    //   4392: istore 5
    //   4394: iload 9
    //   4396: istore 6
    //   4398: aload 15
    //   4400: astore 12
    //   4402: iload 4
    //   4404: istore 7
    //   4406: goto -2399 -> 2007
    //   4409: aload_0
    //   4410: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4413: getfield 712	org/telegram/messenger/MessageObject:type	I
    //   4416: bipush 14
    //   4418: if_icmpne +38 -> 4456
    //   4421: ldc_w 760
    //   4424: iconst_2
    //   4425: anewarray 306	java/lang/Object
    //   4428: dup
    //   4429: iconst_0
    //   4430: aload_0
    //   4431: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4434: invokevirtual 717	org/telegram/messenger/MessageObject:getMusicAuthor	()Ljava/lang/String;
    //   4437: aastore
    //   4438: dup
    //   4439: iconst_1
    //   4440: aload_0
    //   4441: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4444: invokevirtual 720	org/telegram/messenger/MessageObject:getMusicTitle	()Ljava/lang/String;
    //   4447: aastore
    //   4448: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   4451: astore 15
    //   4453: goto -131 -> 4322
    //   4456: aload_0
    //   4457: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4460: getfield 259	org/telegram/messenger/MessageObject:messageText	Ljava/lang/CharSequence;
    //   4463: astore 15
    //   4465: goto -143 -> 4322
    //   4468: aload_0
    //   4469: getfield 511	org/telegram/ui/Cells/DialogCell:lastMessageDate	I
    //   4472: ifeq +16 -> 4488
    //   4475: aload_0
    //   4476: getfield 511	org/telegram/ui/Cells/DialogCell:lastMessageDate	I
    //   4479: i2l
    //   4480: invokestatic 358	org/telegram/messenger/LocaleController:stringForMessageListDate	(J)Ljava/lang/String;
    //   4483: astore 15
    //   4485: goto -2458 -> 2027
    //   4488: aload 22
    //   4490: astore 15
    //   4492: aload_0
    //   4493: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4496: ifnull -2469 -> 2027
    //   4499: aload_0
    //   4500: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4503: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4506: getfield 518	org/telegram/tgnet/TLRPC$Message:date	I
    //   4509: i2l
    //   4510: invokestatic 358	org/telegram/messenger/LocaleController:stringForMessageListDate	(J)Ljava/lang/String;
    //   4513: astore 15
    //   4515: goto -2488 -> 2027
    //   4518: aload_0
    //   4519: getfield 542	org/telegram/ui/Cells/DialogCell:unreadCount	I
    //   4522: ifeq +149 -> 4671
    //   4525: aload_0
    //   4526: getfield 542	org/telegram/ui/Cells/DialogCell:unreadCount	I
    //   4529: iconst_1
    //   4530: if_icmpne +34 -> 4564
    //   4533: aload_0
    //   4534: getfield 542	org/telegram/ui/Cells/DialogCell:unreadCount	I
    //   4537: aload_0
    //   4538: getfield 762	org/telegram/ui/Cells/DialogCell:mentionCount	I
    //   4541: if_icmpne +23 -> 4564
    //   4544: aload_0
    //   4545: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4548: ifnull +16 -> 4564
    //   4551: aload_0
    //   4552: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4555: getfield 515	org/telegram/messenger/MessageObject:messageOwner	Lorg/telegram/tgnet/TLRPC$Message;
    //   4558: getfield 765	org/telegram/tgnet/TLRPC$Message:mentioned	Z
    //   4561: ifne +110 -> 4671
    //   4564: aload_0
    //   4565: iconst_1
    //   4566: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   4569: ldc_w 365
    //   4572: iconst_1
    //   4573: anewarray 306	java/lang/Object
    //   4576: dup
    //   4577: iconst_0
    //   4578: aload_0
    //   4579: getfield 542	org/telegram/ui/Cells/DialogCell:unreadCount	I
    //   4582: invokestatic 370	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   4585: aastore
    //   4586: invokestatic 312	java/lang/String:format	(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    //   4589: astore 14
    //   4591: aload_0
    //   4592: getfield 762	org/telegram/ui/Cells/DialogCell:mentionCount	I
    //   4595: ifeq +88 -> 4683
    //   4598: aload_0
    //   4599: iconst_1
    //   4600: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   4603: ldc_w 767
    //   4606: astore 16
    //   4608: aload_0
    //   4609: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4612: invokevirtual 770	org/telegram/messenger/MessageObject:isOut	()Z
    //   4615: ifeq +216 -> 4831
    //   4618: aload_0
    //   4619: getfield 529	org/telegram/ui/Cells/DialogCell:draftMessage	Lorg/telegram/tgnet/TLRPC$DraftMessage;
    //   4622: ifnonnull +209 -> 4831
    //   4625: iload 7
    //   4627: ifeq +204 -> 4831
    //   4630: aload_0
    //   4631: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4634: invokevirtual 773	org/telegram/messenger/MessageObject:isSending	()Z
    //   4637: ifeq +54 -> 4691
    //   4640: aload_0
    //   4641: iconst_0
    //   4642: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   4645: aload_0
    //   4646: iconst_0
    //   4647: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   4650: aload_0
    //   4651: iconst_1
    //   4652: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   4655: aload_0
    //   4656: iconst_0
    //   4657: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   4660: aload 14
    //   4662: astore 18
    //   4664: aload 16
    //   4666: astore 17
    //   4668: goto -2600 -> 2068
    //   4671: aload_0
    //   4672: iconst_0
    //   4673: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   4676: aload 17
    //   4678: astore 14
    //   4680: goto -89 -> 4591
    //   4683: aload_0
    //   4684: iconst_0
    //   4685: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   4688: goto -80 -> 4608
    //   4691: aload_0
    //   4692: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4695: invokevirtual 776	org/telegram/messenger/MessageObject:isSendError	()Z
    //   4698: ifeq +44 -> 4742
    //   4701: aload_0
    //   4702: iconst_0
    //   4703: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   4706: aload_0
    //   4707: iconst_0
    //   4708: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   4711: aload_0
    //   4712: iconst_0
    //   4713: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   4716: aload_0
    //   4717: iconst_1
    //   4718: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   4721: aload_0
    //   4722: iconst_0
    //   4723: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   4726: aload_0
    //   4727: iconst_0
    //   4728: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   4731: aload 14
    //   4733: astore 18
    //   4735: aload 16
    //   4737: astore 17
    //   4739: goto -2671 -> 2068
    //   4742: aload 14
    //   4744: astore 18
    //   4746: aload 16
    //   4748: astore 17
    //   4750: aload_0
    //   4751: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4754: invokevirtual 779	org/telegram/messenger/MessageObject:isSent	()Z
    //   4757: ifeq -2689 -> 2068
    //   4760: aload_0
    //   4761: getfield 254	org/telegram/ui/Cells/DialogCell:message	Lorg/telegram/messenger/MessageObject;
    //   4764: invokevirtual 782	org/telegram/messenger/MessageObject:isUnread	()Z
    //   4767: ifeq +23 -> 4790
    //   4770: aload_0
    //   4771: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   4774: invokestatic 548	org/telegram/messenger/ChatObject:isChannel	(Lorg/telegram/tgnet/TLRPC$Chat;)Z
    //   4777: ifeq +48 -> 4825
    //   4780: aload_0
    //   4781: getfield 116	org/telegram/ui/Cells/DialogCell:chat	Lorg/telegram/tgnet/TLRPC$Chat;
    //   4784: getfield 553	org/telegram/tgnet/TLRPC$Chat:megagroup	Z
    //   4787: ifne +38 -> 4825
    //   4790: iconst_1
    //   4791: istore 11
    //   4793: aload_0
    //   4794: iload 11
    //   4796: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   4799: aload_0
    //   4800: iconst_1
    //   4801: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   4804: aload_0
    //   4805: iconst_0
    //   4806: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   4809: aload_0
    //   4810: iconst_0
    //   4811: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   4814: aload 14
    //   4816: astore 18
    //   4818: aload 16
    //   4820: astore 17
    //   4822: goto -2754 -> 2068
    //   4825: iconst_0
    //   4826: istore 11
    //   4828: goto -35 -> 4793
    //   4831: aload_0
    //   4832: iconst_0
    //   4833: putfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   4836: aload_0
    //   4837: iconst_0
    //   4838: putfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   4841: aload_0
    //   4842: iconst_0
    //   4843: putfield 379	org/telegram/ui/Cells/DialogCell:drawClock	Z
    //   4846: aload_0
    //   4847: iconst_0
    //   4848: putfield 381	org/telegram/ui/Cells/DialogCell:drawError	Z
    //   4851: aload 14
    //   4853: astore 18
    //   4855: aload 16
    //   4857: astore 17
    //   4859: goto -2791 -> 2068
    //   4862: aload 19
    //   4864: astore 21
    //   4866: aload 20
    //   4868: astore 16
    //   4870: aload_0
    //   4871: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   4874: ifnull -2786 -> 2088
    //   4877: aload_0
    //   4878: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   4881: invokestatic 245	org/telegram/messenger/UserObject:isUserSelf	(Lorg/telegram/tgnet/TLRPC$User;)Z
    //   4884: ifeq +54 -> 4938
    //   4887: aload_0
    //   4888: getfield 178	org/telegram/ui/Cells/DialogCell:dialogsType	I
    //   4891: iconst_3
    //   4892: if_icmpne +8 -> 4900
    //   4895: aload_0
    //   4896: iconst_1
    //   4897: putfield 239	org/telegram/ui/Cells/DialogCell:drawPinBackground	Z
    //   4900: ldc_w 784
    //   4903: ldc_w 785
    //   4906: invokestatic 298	org/telegram/messenger/LocaleController:getString	(Ljava/lang/String;I)Ljava/lang/String;
    //   4909: astore 14
    //   4911: aload 19
    //   4913: astore 21
    //   4915: aload 14
    //   4917: astore 16
    //   4919: aload_0
    //   4920: getfield 118	org/telegram/ui/Cells/DialogCell:encryptedChat	Lorg/telegram/tgnet/TLRPC$EncryptedChat;
    //   4923: ifnull -2835 -> 2088
    //   4926: getstatic 388	org/telegram/ui/ActionBar/Theme:dialogs_nameEncryptedPaint	Landroid/text/TextPaint;
    //   4929: astore 21
    //   4931: aload 14
    //   4933: astore 16
    //   4935: goto -2847 -> 2088
    //   4938: aload_0
    //   4939: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   4942: getfield 786	org/telegram/tgnet/TLRPC$User:id	I
    //   4945: sipush 1000
    //   4948: idiv
    //   4949: sipush 777
    //   4952: if_icmpeq +172 -> 5124
    //   4955: aload_0
    //   4956: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   4959: getfield 786	org/telegram/tgnet/TLRPC$User:id	I
    //   4962: sipush 1000
    //   4965: idiv
    //   4966: sipush 333
    //   4969: if_icmpeq +155 -> 5124
    //   4972: aload_0
    //   4973: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   4976: invokestatic 791	org/telegram/messenger/ContactsController:getInstance	(I)Lorg/telegram/messenger/ContactsController;
    //   4979: getfield 795	org/telegram/messenger/ContactsController:contactsDict	Ljava/util/concurrent/ConcurrentHashMap;
    //   4982: aload_0
    //   4983: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   4986: getfield 786	org/telegram/tgnet/TLRPC$User:id	I
    //   4989: invokestatic 370	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   4992: invokevirtual 800	java/util/concurrent/ConcurrentHashMap:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   4995: ifnonnull +129 -> 5124
    //   4998: aload_0
    //   4999: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   5002: invokestatic 791	org/telegram/messenger/ContactsController:getInstance	(I)Lorg/telegram/messenger/ContactsController;
    //   5005: getfield 795	org/telegram/messenger/ContactsController:contactsDict	Ljava/util/concurrent/ConcurrentHashMap;
    //   5008: invokevirtual 803	java/util/concurrent/ConcurrentHashMap:size	()I
    //   5011: ifne +41 -> 5052
    //   5014: aload_0
    //   5015: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   5018: invokestatic 791	org/telegram/messenger/ContactsController:getInstance	(I)Lorg/telegram/messenger/ContactsController;
    //   5021: getfield 806	org/telegram/messenger/ContactsController:contactsLoaded	Z
    //   5024: ifeq +16 -> 5040
    //   5027: aload_0
    //   5028: getfield 98	org/telegram/ui/Cells/DialogCell:currentAccount	I
    //   5031: invokestatic 791	org/telegram/messenger/ContactsController:getInstance	(I)Lorg/telegram/messenger/ContactsController;
    //   5034: invokevirtual 809	org/telegram/messenger/ContactsController:isLoadingContacts	()Z
    //   5037: ifeq +15 -> 5052
    //   5040: aload_0
    //   5041: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5044: invokestatic 812	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   5047: astore 14
    //   5049: goto -138 -> 4911
    //   5052: aload_0
    //   5053: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5056: getfield 815	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   5059: ifnull +53 -> 5112
    //   5062: aload_0
    //   5063: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5066: getfield 815	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   5069: invokevirtual 331	java/lang/String:length	()I
    //   5072: ifeq +40 -> 5112
    //   5075: invokestatic 820	org/telegram/PhoneFormat/PhoneFormat:getInstance	()Lorg/telegram/PhoneFormat/PhoneFormat;
    //   5078: new 750	java/lang/StringBuilder
    //   5081: dup
    //   5082: invokespecial 751	java/lang/StringBuilder:<init>	()V
    //   5085: ldc_w 822
    //   5088: invokevirtual 757	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5091: aload_0
    //   5092: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5095: getfield 815	org/telegram/tgnet/TLRPC$User:phone	Ljava/lang/String;
    //   5098: invokevirtual 757	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   5101: invokevirtual 758	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   5104: invokevirtual 825	org/telegram/PhoneFormat/PhoneFormat:format	(Ljava/lang/String;)Ljava/lang/String;
    //   5107: astore 14
    //   5109: goto -198 -> 4911
    //   5112: aload_0
    //   5113: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5116: invokestatic 812	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   5119: astore 14
    //   5121: goto -210 -> 4911
    //   5124: aload_0
    //   5125: getfield 114	org/telegram/ui/Cells/DialogCell:user	Lorg/telegram/tgnet/TLRPC$User;
    //   5128: invokestatic 812	org/telegram/messenger/UserObject:getUserName	(Lorg/telegram/tgnet/TLRPC$User;)Ljava/lang/String;
    //   5131: astore 14
    //   5133: goto -222 -> 4911
    //   5136: aload_0
    //   5137: ldc_w 418
    //   5140: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5143: putfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5146: goto -4518 -> 628
    //   5149: iconst_0
    //   5150: istore 6
    //   5152: aload_0
    //   5153: aconst_null
    //   5154: putfield 414	org/telegram/ui/Cells/DialogCell:timeLayout	Landroid/text/StaticLayout;
    //   5157: aload_0
    //   5158: iconst_0
    //   5159: putfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5162: goto -4534 -> 628
    //   5165: aload_0
    //   5166: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   5169: aload_0
    //   5170: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5173: isub
    //   5174: getstatic 277	org/telegram/messenger/AndroidUtilities:leftBaseline	I
    //   5177: i2f
    //   5178: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5181: isub
    //   5182: iload 6
    //   5184: isub
    //   5185: istore 5
    //   5187: aload_0
    //   5188: aload_0
    //   5189: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5192: iload 6
    //   5194: iadd
    //   5195: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5198: goto -4543 -> 655
    //   5201: aload_0
    //   5202: getfield 229	org/telegram/ui/Cells/DialogCell:drawNameGroup	Z
    //   5205: ifeq +24 -> 5229
    //   5208: iload 5
    //   5210: ldc_w 422
    //   5213: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5216: getstatic 504	org/telegram/ui/ActionBar/Theme:dialogs_groupDrawable	Landroid/graphics/drawable/Drawable;
    //   5219: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5222: iadd
    //   5223: isub
    //   5224: istore 4
    //   5226: goto -4546 -> 680
    //   5229: aload_0
    //   5230: getfield 231	org/telegram/ui/Cells/DialogCell:drawNameBroadcast	Z
    //   5233: ifeq +24 -> 5257
    //   5236: iload 5
    //   5238: ldc_w 422
    //   5241: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5244: getstatic 507	org/telegram/ui/ActionBar/Theme:dialogs_broadcastDrawable	Landroid/graphics/drawable/Drawable;
    //   5247: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5250: iadd
    //   5251: isub
    //   5252: istore 4
    //   5254: goto -4574 -> 680
    //   5257: iload 5
    //   5259: istore 4
    //   5261: aload_0
    //   5262: getfield 235	org/telegram/ui/Cells/DialogCell:drawNameBot	Z
    //   5265: ifeq -4585 -> 680
    //   5268: iload 5
    //   5270: ldc_w 422
    //   5273: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5276: getstatic 591	org/telegram/ui/ActionBar/Theme:dialogs_botDrawable	Landroid/graphics/drawable/Drawable;
    //   5279: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5282: iadd
    //   5283: isub
    //   5284: istore 4
    //   5286: goto -4606 -> 680
    //   5289: aload_0
    //   5290: aload_0
    //   5291: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5294: iload 6
    //   5296: iadd
    //   5297: ldc_w 426
    //   5300: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5303: iadd
    //   5304: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5307: aload_0
    //   5308: aload_0
    //   5309: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5312: iload 8
    //   5314: iadd
    //   5315: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5318: goto -4592 -> 726
    //   5321: iload 4
    //   5323: istore 5
    //   5325: aload_0
    //   5326: getfield 377	org/telegram/ui/Cells/DialogCell:drawCheck2	Z
    //   5329: ifeq -4603 -> 726
    //   5332: getstatic 828	org/telegram/ui/ActionBar/Theme:dialogs_checkDrawable	Landroid/graphics/drawable/Drawable;
    //   5335: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5338: ldc_w 426
    //   5341: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5344: iadd
    //   5345: istore 8
    //   5347: iload 4
    //   5349: iload 8
    //   5351: isub
    //   5352: istore 5
    //   5354: aload_0
    //   5355: getfield 375	org/telegram/ui/Cells/DialogCell:drawCheck1	Z
    //   5358: ifeq +117 -> 5475
    //   5361: iload 5
    //   5363: getstatic 831	org/telegram/ui/ActionBar/Theme:dialogs_halfCheckDrawable	Landroid/graphics/drawable/Drawable;
    //   5366: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5369: ldc_w 832
    //   5372: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5375: isub
    //   5376: isub
    //   5377: istore 5
    //   5379: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   5382: ifne +32 -> 5414
    //   5385: aload_0
    //   5386: aload_0
    //   5387: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5390: iload 8
    //   5392: isub
    //   5393: putfield 834	org/telegram/ui/Cells/DialogCell:halfCheckDrawLeft	I
    //   5396: aload_0
    //   5397: aload_0
    //   5398: getfield 834	org/telegram/ui/Cells/DialogCell:halfCheckDrawLeft	I
    //   5401: ldc_w 835
    //   5404: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5407: isub
    //   5408: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5411: goto -4685 -> 726
    //   5414: aload_0
    //   5415: aload_0
    //   5416: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5419: iload 6
    //   5421: iadd
    //   5422: ldc_w 426
    //   5425: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5428: iadd
    //   5429: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5432: aload_0
    //   5433: aload_0
    //   5434: getfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5437: ldc_w 835
    //   5440: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5443: iadd
    //   5444: putfield 834	org/telegram/ui/Cells/DialogCell:halfCheckDrawLeft	I
    //   5447: aload_0
    //   5448: aload_0
    //   5449: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5452: getstatic 831	org/telegram/ui/ActionBar/Theme:dialogs_halfCheckDrawable	Landroid/graphics/drawable/Drawable;
    //   5455: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5458: iload 8
    //   5460: iadd
    //   5461: ldc_w 832
    //   5464: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5467: isub
    //   5468: iadd
    //   5469: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5472: goto -4746 -> 726
    //   5475: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   5478: ifne +17 -> 5495
    //   5481: aload_0
    //   5482: aload_0
    //   5483: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5486: iload 8
    //   5488: isub
    //   5489: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5492: goto -4766 -> 726
    //   5495: aload_0
    //   5496: aload_0
    //   5497: getfield 420	org/telegram/ui/Cells/DialogCell:timeLeft	I
    //   5500: iload 6
    //   5502: iadd
    //   5503: ldc_w 426
    //   5506: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5509: iadd
    //   5510: putfield 428	org/telegram/ui/Cells/DialogCell:checkDrawLeft	I
    //   5513: aload_0
    //   5514: aload_0
    //   5515: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5518: iload 8
    //   5520: iadd
    //   5521: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5524: goto -4798 -> 726
    //   5527: iload 5
    //   5529: istore 4
    //   5531: aload_0
    //   5532: getfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   5535: ifeq -4748 -> 787
    //   5538: ldc_w 431
    //   5541: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5544: getstatic 838	org/telegram/ui/ActionBar/Theme:dialogs_verifiedDrawable	Landroid/graphics/drawable/Drawable;
    //   5547: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   5550: iadd
    //   5551: istore 6
    //   5553: iload 5
    //   5555: iload 6
    //   5557: isub
    //   5558: istore 5
    //   5560: iload 5
    //   5562: istore 4
    //   5564: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   5567: ifeq -4780 -> 787
    //   5570: aload_0
    //   5571: aload_0
    //   5572: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5575: iload 6
    //   5577: iadd
    //   5578: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   5581: iload 5
    //   5583: istore 4
    //   5585: goto -4798 -> 787
    //   5588: astore 12
    //   5590: aload 12
    //   5592: invokestatic 844	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   5595: goto -4747 -> 848
    //   5598: ldc_w 845
    //   5601: fstore_3
    //   5602: goto -4710 -> 892
    //   5605: aload_0
    //   5606: ldc_w 846
    //   5609: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5612: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5615: aload_0
    //   5616: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   5619: istore 5
    //   5621: invokestatic 463	org/telegram/messenger/AndroidUtilities:isTablet	()Z
    //   5624: ifeq +19 -> 5643
    //   5627: ldc_w 847
    //   5630: fstore_3
    //   5631: iload 5
    //   5633: fload_3
    //   5634: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5637: isub
    //   5638: istore 5
    //   5640: goto -4742 -> 898
    //   5643: ldc_w 848
    //   5646: fstore_3
    //   5647: goto -16 -> 5631
    //   5650: aload_0
    //   5651: ldc_w 849
    //   5654: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5657: putfield 473	org/telegram/ui/Cells/DialogCell:errorLeft	I
    //   5660: aload_0
    //   5661: aload_0
    //   5662: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5665: iload 5
    //   5667: iadd
    //   5668: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5671: goto -4705 -> 966
    //   5674: aload 23
    //   5676: ifnonnull +8 -> 5684
    //   5679: aload 24
    //   5681: ifnull +318 -> 5999
    //   5684: aload 23
    //   5686: ifnull +239 -> 5925
    //   5689: aload_0
    //   5690: ldc_w 435
    //   5693: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5696: getstatic 852	org/telegram/ui/ActionBar/Theme:dialogs_countTextPaint	Landroid/text/TextPaint;
    //   5699: aload 23
    //   5701: invokevirtual 395	android/text/TextPaint:measureText	(Ljava/lang/String;)F
    //   5704: f2d
    //   5705: invokestatic 401	java/lang/Math:ceil	(D)D
    //   5708: d2i
    //   5709: invokestatic 439	java/lang/Math:max	(II)I
    //   5712: putfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5715: aload_0
    //   5716: new 403	android/text/StaticLayout
    //   5719: dup
    //   5720: aload 23
    //   5722: getstatic 852	org/telegram/ui/ActionBar/Theme:dialogs_countTextPaint	Landroid/text/TextPaint;
    //   5725: aload_0
    //   5726: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5729: getstatic 857	android/text/Layout$Alignment:ALIGN_CENTER	Landroid/text/Layout$Alignment;
    //   5732: fconst_1
    //   5733: fconst_0
    //   5734: iconst_0
    //   5735: invokespecial 412	android/text/StaticLayout:<init>	(Ljava/lang/CharSequence;Landroid/text/TextPaint;ILandroid/text/Layout$Alignment;FFZ)V
    //   5738: putfield 859	org/telegram/ui/Cells/DialogCell:countLayout	Landroid/text/StaticLayout;
    //   5741: aload_0
    //   5742: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5745: ldc -124
    //   5747: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5750: iadd
    //   5751: istore 8
    //   5753: iload 4
    //   5755: iload 8
    //   5757: isub
    //   5758: istore 5
    //   5760: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   5763: ifne +138 -> 5901
    //   5766: aload_0
    //   5767: aload_0
    //   5768: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   5771: aload_0
    //   5772: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5775: isub
    //   5776: ldc_w 860
    //   5779: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5782: isub
    //   5783: putfield 862	org/telegram/ui/Cells/DialogCell:countLeft	I
    //   5786: aload_0
    //   5787: iconst_1
    //   5788: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   5791: iload 5
    //   5793: istore 4
    //   5795: aload 24
    //   5797: ifnull -4831 -> 966
    //   5800: aload_0
    //   5801: ldc_w 435
    //   5804: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5807: putfield 864	org/telegram/ui/Cells/DialogCell:mentionWidth	I
    //   5810: aload_0
    //   5811: getfield 864	org/telegram/ui/Cells/DialogCell:mentionWidth	I
    //   5814: ldc -124
    //   5816: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5819: iadd
    //   5820: istore 8
    //   5822: iload 5
    //   5824: iload 8
    //   5826: isub
    //   5827: istore 5
    //   5829: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   5832: ifne +111 -> 5943
    //   5835: aload_0
    //   5836: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   5839: istore 8
    //   5841: aload_0
    //   5842: getfield 864	org/telegram/ui/Cells/DialogCell:mentionWidth	I
    //   5845: istore 9
    //   5847: ldc_w 860
    //   5850: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5853: istore 10
    //   5855: aload_0
    //   5856: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5859: ifeq +78 -> 5937
    //   5862: aload_0
    //   5863: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5866: ldc -124
    //   5868: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5871: iadd
    //   5872: istore 4
    //   5874: aload_0
    //   5875: iload 8
    //   5877: iload 9
    //   5879: isub
    //   5880: iload 10
    //   5882: isub
    //   5883: iload 4
    //   5885: isub
    //   5886: putfield 866	org/telegram/ui/Cells/DialogCell:mentionLeft	I
    //   5889: aload_0
    //   5890: iconst_1
    //   5891: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   5894: iload 5
    //   5896: istore 4
    //   5898: goto -4932 -> 966
    //   5901: aload_0
    //   5902: ldc_w 860
    //   5905: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5908: putfield 862	org/telegram/ui/Cells/DialogCell:countLeft	I
    //   5911: aload_0
    //   5912: aload_0
    //   5913: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5916: iload 8
    //   5918: iadd
    //   5919: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5922: goto -136 -> 5786
    //   5925: aload_0
    //   5926: iconst_0
    //   5927: putfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5930: iload 4
    //   5932: istore 5
    //   5934: goto -143 -> 5791
    //   5937: iconst_0
    //   5938: istore 4
    //   5940: goto -66 -> 5874
    //   5943: ldc_w 860
    //   5946: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5949: istore 9
    //   5951: aload_0
    //   5952: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5955: ifeq +38 -> 5993
    //   5958: aload_0
    //   5959: getfield 854	org/telegram/ui/Cells/DialogCell:countWidth	I
    //   5962: ldc -124
    //   5964: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   5967: iadd
    //   5968: istore 4
    //   5970: aload_0
    //   5971: iload 4
    //   5973: iload 9
    //   5975: iadd
    //   5976: putfield 866	org/telegram/ui/Cells/DialogCell:mentionLeft	I
    //   5979: aload_0
    //   5980: aload_0
    //   5981: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5984: iload 8
    //   5986: iadd
    //   5987: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   5990: goto -101 -> 5889
    //   5993: iconst_0
    //   5994: istore 4
    //   5996: goto -26 -> 5970
    //   5999: aload_0
    //   6000: getfield 868	org/telegram/ui/Cells/DialogCell:drawPin	Z
    //   6003: ifeq +317 -> 6320
    //   6006: getstatic 871	org/telegram/ui/ActionBar/Theme:dialogs_pinnedDrawable	Landroid/graphics/drawable/Drawable;
    //   6009: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   6012: ldc_w 832
    //   6015: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6018: iadd
    //   6019: istore 5
    //   6021: iload 4
    //   6023: iload 5
    //   6025: isub
    //   6026: istore 4
    //   6028: getstatic 274	org/telegram/messenger/LocaleController:isRTL	Z
    //   6031: ifne +38 -> 6069
    //   6034: aload_0
    //   6035: aload_0
    //   6036: invokevirtual 417	org/telegram/ui/Cells/DialogCell:getMeasuredWidth	()I
    //   6039: getstatic 871	org/telegram/ui/ActionBar/Theme:dialogs_pinnedDrawable	Landroid/graphics/drawable/Drawable;
    //   6042: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   6045: isub
    //   6046: ldc_w 421
    //   6049: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6052: isub
    //   6053: putfield 873	org/telegram/ui/Cells/DialogCell:pinLeft	I
    //   6056: aload_0
    //   6057: iconst_0
    //   6058: putfield 363	org/telegram/ui/Cells/DialogCell:drawCount	Z
    //   6061: aload_0
    //   6062: iconst_0
    //   6063: putfield 573	org/telegram/ui/Cells/DialogCell:drawMention	Z
    //   6066: goto -5100 -> 966
    //   6069: aload_0
    //   6070: ldc_w 421
    //   6073: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6076: putfield 873	org/telegram/ui/Cells/DialogCell:pinLeft	I
    //   6079: aload_0
    //   6080: aload_0
    //   6081: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   6084: iload 5
    //   6086: iadd
    //   6087: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   6090: goto -34 -> 6056
    //   6093: astore 12
    //   6095: aload 12
    //   6097: invokestatic 844	org/telegram/messenger/FileLog:e	(Ljava/lang/Throwable;)V
    //   6100: goto -4993 -> 1107
    //   6103: aload_0
    //   6104: getfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   6107: ifeq -4909 -> 1198
    //   6110: aload_0
    //   6111: aload_0
    //   6112: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   6115: i2d
    //   6116: iload 6
    //   6118: i2d
    //   6119: dload_1
    //   6120: dsub
    //   6121: dadd
    //   6122: ldc_w 431
    //   6125: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6128: i2d
    //   6129: dsub
    //   6130: getstatic 838	org/telegram/ui/ActionBar/Theme:dialogs_verifiedDrawable	Landroid/graphics/drawable/Drawable;
    //   6133: invokevirtual 289	android/graphics/drawable/Drawable:getIntrinsicWidth	()I
    //   6136: i2d
    //   6137: dsub
    //   6138: d2i
    //   6139: putfield 495	org/telegram/ui/Cells/DialogCell:nameMuteLeft	I
    //   6142: goto -4944 -> 1198
    //   6145: aload_0
    //   6146: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   6149: ifnull +101 -> 6250
    //   6152: aload_0
    //   6153: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   6156: invokevirtual 486	android/text/StaticLayout:getLineCount	()I
    //   6159: ifle +91 -> 6250
    //   6162: aload_0
    //   6163: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   6166: iconst_0
    //   6167: invokevirtual 876	android/text/StaticLayout:getLineRight	(I)F
    //   6170: fstore_3
    //   6171: fload_3
    //   6172: iload 6
    //   6174: i2f
    //   6175: fcmpl
    //   6176: ifne +40 -> 6216
    //   6179: aload_0
    //   6180: getfield 457	org/telegram/ui/Cells/DialogCell:nameLayout	Landroid/text/StaticLayout;
    //   6183: iconst_0
    //   6184: invokevirtual 493	android/text/StaticLayout:getLineWidth	(I)F
    //   6187: f2d
    //   6188: invokestatic 401	java/lang/Math:ceil	(D)D
    //   6191: dstore_1
    //   6192: dload_1
    //   6193: iload 6
    //   6195: i2d
    //   6196: dcmpg
    //   6197: ifge +19 -> 6216
    //   6200: aload_0
    //   6201: aload_0
    //   6202: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   6205: i2d
    //   6206: iload 6
    //   6208: i2d
    //   6209: dload_1
    //   6210: dsub
    //   6211: dsub
    //   6212: d2i
    //   6213: putfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   6216: aload_0
    //   6217: getfield 430	org/telegram/ui/Cells/DialogCell:dialogMuted	Z
    //   6220: ifne +10 -> 6230
    //   6223: aload_0
    //   6224: getfield 237	org/telegram/ui/Cells/DialogCell:drawVerified	Z
    //   6227: ifeq +23 -> 6250
    //   6230: aload_0
    //   6231: aload_0
    //   6232: getfield 291	org/telegram/ui/Cells/DialogCell:nameLeft	I
    //   6235: i2f
    //   6236: fload_3
    //   6237: fadd
    //   6238: ldc_w 431
    //   6241: invokestatic 129	org/telegram/messenger/AndroidUtilities:dp	(F)I
    //   6244: i2f
    //   6245: fadd
    //   6246: f2i
    //   6247: putfield 495	org/telegram/ui/Cells/DialogCell:nameMuteLeft	I
    //   6250: aload_0
    //   6251: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   6254: ifnull -4959 -> 1295
    //   6257: aload_0
    //   6258: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   6261: invokevirtual 486	android/text/StaticLayout:getLineCount	()I
    //   6264: ifle -4969 -> 1295
    //   6267: aload_0
    //   6268: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   6271: iconst_0
    //   6272: invokevirtual 876	android/text/StaticLayout:getLineRight	(I)F
    //   6275: iload 4
    //   6277: i2f
    //   6278: fcmpl
    //   6279: ifne -4984 -> 1295
    //   6282: aload_0
    //   6283: getfield 483	org/telegram/ui/Cells/DialogCell:messageLayout	Landroid/text/StaticLayout;
    //   6286: iconst_0
    //   6287: invokevirtual 493	android/text/StaticLayout:getLineWidth	(I)F
    //   6290: f2d
    //   6291: invokestatic 401	java/lang/Math:ceil	(D)D
    //   6294: dstore_1
    //   6295: dload_1
    //   6296: iload 4
    //   6298: i2d
    //   6299: dcmpg
    //   6300: ifge -5005 -> 1295
    //   6303: aload_0
    //   6304: aload_0
    //   6305: getfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   6308: i2d
    //   6309: iload 4
    //   6311: i2d
    //   6312: dload_1
    //   6313: dsub
    //   6314: dsub
    //   6315: d2i
    //   6316: putfield 459	org/telegram/ui/Cells/DialogCell:messageLeft	I
    //   6319: return
    //   6320: goto -264 -> 6056
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	6323	0	this	DialogCell
    //   1151	5162	1	d	double
    //   891	5346	3	f	float
    //   119	6191	4	i	int
    //   125	5962	5	j	int
    //   76	6131	6	k	int
    //   486	4140	7	m	int
    //   73	5914	8	n	int
    //   122	5854	9	i1	int
    //   5853	30	10	i2	int
    //   4791	36	11	bool	boolean
    //   31	4370	12	localObject1	Object
    //   5588	3	12	localException1	Exception
    //   6093	3	12	localException2	Exception
    //   153	4236	13	localObject2	Object
    //   70	5062	14	localObject3	Object
    //   137	4377	15	localObject4	Object
    //   21	4913	16	localObject5	Object
    //   12	4846	17	localObject6	Object
    //   9	4845	18	localObject7	Object
    //   65	4847	19	localObject8	Object
    //   2	4865	20	localObject9	Object
    //   18	4912	21	localObject10	Object
    //   6	4483	22	localObject11	Object
    //   15	5706	23	localObject12	Object
    //   28	5768	24	localObject13	Object
    //   482	1664	25	localObject14	Object
    //   24	513	26	localObject15	Object
    // Exception table:
    //   from	to	target	type
    //   800	848	5588	java/lang/Exception
    //   1084	1107	6093	java/lang/Exception
  }
  
  public void checkCurrentDialogIndex()
  {
    if (this.index < getDialogsArray().size())
    {
      TLRPC.TL_dialog localTL_dialog = (TLRPC.TL_dialog)getDialogsArray().get(this.index);
      TLRPC.DraftMessage localDraftMessage = DataQuery.getInstance(this.currentAccount).getDraft(this.currentDialogId);
      MessageObject localMessageObject = (MessageObject)MessagesController.getInstance(this.currentAccount).dialogMessage.get(localTL_dialog.id);
      if ((this.currentDialogId != localTL_dialog.id) || ((this.message != null) && (this.message.getId() != localTL_dialog.top_message)) || ((localMessageObject != null) && (localMessageObject.messageOwner.edit_date != this.currentEditDate)) || (this.unreadCount != localTL_dialog.unread_count) || (this.mentionCount != localTL_dialog.unread_mentions_count) || (this.message != localMessageObject) || ((this.message == null) && (localMessageObject != null)) || (localDraftMessage != this.draftMessage) || (this.drawPin != localTL_dialog.pinned))
      {
        this.currentDialogId = localTL_dialog.id;
        update(0);
      }
    }
  }
  
  public long getDialogId()
  {
    return this.currentDialogId;
  }
  
  public boolean hasOverlappingRendering()
  {
    return false;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.avatarImage.onAttachedToWindow();
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.avatarImage.onDetachedFromWindow();
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.currentDialogId == 0L) && (this.customDialog == null)) {
      return;
    }
    if (this.isSelected) {
      paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight(), Theme.dialogs_tabletSeletedPaint);
    }
    if ((this.drawPin) || (this.drawPinBackground)) {
      paramCanvas.drawRect(0.0F, 0.0F, getMeasuredWidth(), getMeasuredHeight(), Theme.dialogs_pinnedPaint);
    }
    if (this.drawNameLock)
    {
      setDrawableBounds(Theme.dialogs_lockDrawable, this.nameLockLeft, this.nameLockTop);
      Theme.dialogs_lockDrawable.draw(paramCanvas);
    }
    for (;;)
    {
      if (this.nameLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.nameLeft, AndroidUtilities.dp(13.0F));
        this.nameLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
      if (this.timeLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.timeLeft, this.timeTop);
        this.timeLayout.draw(paramCanvas);
        paramCanvas.restore();
      }
      if (this.messageLayout != null)
      {
        paramCanvas.save();
        paramCanvas.translate(this.messageLeft, this.messageTop);
      }
      try
      {
        this.messageLayout.draw(paramCanvas);
        paramCanvas.restore();
        if (this.drawClock)
        {
          setDrawableBounds(Theme.dialogs_clockDrawable, this.checkDrawLeft, this.checkDrawTop);
          Theme.dialogs_clockDrawable.draw(paramCanvas);
          if ((!this.dialogMuted) || (this.drawVerified)) {
            break label637;
          }
          setDrawableBounds(Theme.dialogs_muteDrawable, this.nameMuteLeft, AndroidUtilities.dp(16.5F));
          Theme.dialogs_muteDrawable.draw(paramCanvas);
          if (!this.drawError) {
            break label693;
          }
          this.rect.set(this.errorLeft, this.errorTop, this.errorLeft + AndroidUtilities.dp(23.0F), this.errorTop + AndroidUtilities.dp(23.0F));
          paramCanvas.drawRoundRect(this.rect, AndroidUtilities.density * 11.5F, AndroidUtilities.density * 11.5F, Theme.dialogs_errorPaint);
          setDrawableBounds(Theme.dialogs_errorDrawable, this.errorLeft + AndroidUtilities.dp(5.5F), this.errorTop + AndroidUtilities.dp(5.0F));
          Theme.dialogs_errorDrawable.draw(paramCanvas);
          if (this.useSeparator)
          {
            if (!LocaleController.isRTL) {
              break label1032;
            }
            paramCanvas.drawLine(0.0F, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, Theme.dividerPaint);
          }
          this.avatarImage.draw(paramCanvas);
          return;
          if (this.drawNameGroup)
          {
            setDrawableBounds(Theme.dialogs_groupDrawable, this.nameLockLeft, this.nameLockTop);
            Theme.dialogs_groupDrawable.draw(paramCanvas);
            continue;
          }
          if (this.drawNameBroadcast)
          {
            setDrawableBounds(Theme.dialogs_broadcastDrawable, this.nameLockLeft, this.nameLockTop);
            Theme.dialogs_broadcastDrawable.draw(paramCanvas);
            continue;
          }
          if (!this.drawNameBot) {
            continue;
          }
          setDrawableBounds(Theme.dialogs_botDrawable, this.nameLockLeft, this.nameLockTop);
          Theme.dialogs_botDrawable.draw(paramCanvas);
        }
      }
      catch (Exception localException)
      {
        for (;;)
        {
          FileLog.e(localException);
          continue;
          if (this.drawCheck2) {
            if (this.drawCheck1)
            {
              setDrawableBounds(Theme.dialogs_halfCheckDrawable, this.halfCheckDrawLeft, this.checkDrawTop);
              Theme.dialogs_halfCheckDrawable.draw(paramCanvas);
              setDrawableBounds(Theme.dialogs_checkDrawable, this.checkDrawLeft, this.checkDrawTop);
              Theme.dialogs_checkDrawable.draw(paramCanvas);
            }
            else
            {
              setDrawableBounds(Theme.dialogs_checkDrawable, this.checkDrawLeft, this.checkDrawTop);
              Theme.dialogs_checkDrawable.draw(paramCanvas);
              continue;
              label637:
              if (this.drawVerified)
              {
                setDrawableBounds(Theme.dialogs_verifiedDrawable, this.nameMuteLeft, AndroidUtilities.dp(16.5F));
                setDrawableBounds(Theme.dialogs_verifiedCheckDrawable, this.nameMuteLeft, AndroidUtilities.dp(16.5F));
                Theme.dialogs_verifiedDrawable.draw(paramCanvas);
                Theme.dialogs_verifiedCheckDrawable.draw(paramCanvas);
                continue;
                label693:
                if ((this.drawCount) || (this.drawMention))
                {
                  int i;
                  RectF localRectF;
                  float f1;
                  float f2;
                  if (this.drawCount)
                  {
                    i = this.countLeft - AndroidUtilities.dp(5.5F);
                    this.rect.set(i, this.countTop, this.countWidth + i + AndroidUtilities.dp(11.0F), this.countTop + AndroidUtilities.dp(23.0F));
                    localRectF = this.rect;
                    f1 = AndroidUtilities.density;
                    f2 = AndroidUtilities.density;
                    if (!this.dialogMuted) {
                      break label993;
                    }
                  }
                  label993:
                  for (Paint localPaint = Theme.dialogs_countGrayPaint;; localPaint = Theme.dialogs_countPaint)
                  {
                    paramCanvas.drawRoundRect(localRectF, 11.5F * f1, 11.5F * f2, localPaint);
                    paramCanvas.save();
                    paramCanvas.translate(this.countLeft, this.countTop + AndroidUtilities.dp(4.0F));
                    if (this.countLayout != null) {
                      this.countLayout.draw(paramCanvas);
                    }
                    paramCanvas.restore();
                    if (!this.drawMention) {
                      break;
                    }
                    i = this.mentionLeft - AndroidUtilities.dp(5.5F);
                    this.rect.set(i, this.countTop, this.mentionWidth + i + AndroidUtilities.dp(11.0F), this.countTop + AndroidUtilities.dp(23.0F));
                    paramCanvas.drawRoundRect(this.rect, AndroidUtilities.density * 11.5F, AndroidUtilities.density * 11.5F, Theme.dialogs_countPaint);
                    setDrawableBounds(Theme.dialogs_mentionDrawable, this.mentionLeft - AndroidUtilities.dp(2.0F), this.countTop + AndroidUtilities.dp(3.2F), AndroidUtilities.dp(16.0F), AndroidUtilities.dp(16.0F));
                    Theme.dialogs_mentionDrawable.draw(paramCanvas);
                    break;
                  }
                }
                if (this.drawPin)
                {
                  setDrawableBounds(Theme.dialogs_pinnedDrawable, this.pinLeft, this.pinTop);
                  Theme.dialogs_pinnedDrawable.draw(paramCanvas);
                  continue;
                  label1032:
                  paramCanvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
              }
            }
          }
        }
      }
    }
  }
  
  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((this.currentDialogId == 0L) && (this.customDialog == null)) {
      return;
    }
    if (this.checkBox != null) {
      if (!LocaleController.isRTL) {
        break label93;
      }
    }
    label93:
    for (paramInt1 = paramInt3 - paramInt1 - AndroidUtilities.dp(42.0F);; paramInt1 = AndroidUtilities.dp(42.0F))
    {
      paramInt2 = AndroidUtilities.dp(43.0F);
      this.checkBox.layout(paramInt1, paramInt2, this.checkBox.getMeasuredWidth() + paramInt1, this.checkBox.getMeasuredHeight() + paramInt2);
      if (!paramBoolean) {
        break;
      }
      try
      {
        buildLayout();
        return;
      }
      catch (Exception localException)
      {
        FileLog.e(localException);
        return;
      }
    }
  }
  
  protected void onMeasure(int paramInt1, int paramInt2)
  {
    if (this.checkBox != null) {
      this.checkBox.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0F), 1073741824), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0F), 1073741824));
    }
    paramInt2 = View.MeasureSpec.getSize(paramInt1);
    int i = AndroidUtilities.dp(72.0F);
    if (this.useSeparator) {}
    for (paramInt1 = 1;; paramInt1 = 0)
    {
      setMeasuredDimension(paramInt2, paramInt1 + i);
      return;
    }
  }
  
  public void setChecked(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.checkBox == null) {
      return;
    }
    this.checkBox.setChecked(paramBoolean1, paramBoolean2);
  }
  
  public void setDialog(long paramLong, MessageObject paramMessageObject, int paramInt)
  {
    this.currentDialogId = paramLong;
    this.message = paramMessageObject;
    this.isDialogCell = false;
    this.lastMessageDate = paramInt;
    if (paramMessageObject != null)
    {
      paramInt = paramMessageObject.messageOwner.edit_date;
      this.currentEditDate = paramInt;
      this.unreadCount = 0;
      this.mentionCount = 0;
      if ((paramMessageObject == null) || (!paramMessageObject.isUnread())) {
        break label103;
      }
    }
    label103:
    for (boolean bool = true;; bool = false)
    {
      this.lastUnreadState = bool;
      if (this.message != null) {
        this.lastSendState = this.message.messageOwner.send_state;
      }
      update(0);
      return;
      paramInt = 0;
      break;
    }
  }
  
  public void setDialog(TLRPC.TL_dialog paramTL_dialog, int paramInt1, int paramInt2)
  {
    this.currentDialogId = paramTL_dialog.id;
    this.isDialogCell = true;
    this.index = paramInt1;
    this.dialogsType = paramInt2;
    update(0);
  }
  
  public void setDialog(CustomDialog paramCustomDialog)
  {
    this.customDialog = paramCustomDialog;
    update(0);
  }
  
  public void setDialogSelected(boolean paramBoolean)
  {
    if (this.isSelected != paramBoolean) {
      invalidate();
    }
    this.isSelected = paramBoolean;
  }
  
  public void update(int paramInt)
  {
    boolean bool;
    if (this.customDialog != null)
    {
      this.lastMessageDate = this.customDialog.date;
      if (this.customDialog.unread_count != 0)
      {
        bool = true;
        this.lastUnreadState = bool;
        this.unreadCount = this.customDialog.unread_count;
        this.drawPin = this.customDialog.pinned;
        this.dialogMuted = this.customDialog.muted;
        this.avatarDrawable.setInfo(this.customDialog.id, this.customDialog.name, null, false);
        this.avatarImage.setImage((TLObject)null, "50_50", this.avatarDrawable, null, 0);
        if ((getMeasuredWidth() == 0) && (getMeasuredHeight() == 0)) {
          break label1168;
        }
        buildLayout();
      }
    }
    for (;;)
    {
      invalidate();
      Object localObject1;
      label225:
      int i;
      label267:
      label311:
      int j;
      label608:
      do
      {
        return;
        bool = false;
        break;
        if (!this.isDialogCell) {
          break label836;
        }
        localObject1 = (TLRPC.TL_dialog)MessagesController.getInstance(this.currentAccount).dialogs_dict.get(this.currentDialogId);
        if ((localObject1 != null) && (paramInt == 0))
        {
          this.message = ((MessageObject)MessagesController.getInstance(this.currentAccount).dialogMessage.get(((TLRPC.TL_dialog)localObject1).id));
          if ((this.message == null) || (!this.message.isUnread())) {
            break label825;
          }
          bool = true;
          this.lastUnreadState = bool;
          this.unreadCount = ((TLRPC.TL_dialog)localObject1).unread_count;
          this.mentionCount = ((TLRPC.TL_dialog)localObject1).unread_mentions_count;
          if (this.message == null) {
            break label831;
          }
          i = this.message.messageOwner.edit_date;
          this.currentEditDate = i;
          this.lastMessageDate = ((TLRPC.TL_dialog)localObject1).last_message_date;
          this.drawPin = ((TLRPC.TL_dialog)localObject1).pinned;
          if (this.message != null) {
            this.lastSendState = this.message.messageOwner.send_state;
          }
        }
        if (paramInt == 0) {
          break label672;
        }
        j = 0;
        i = j;
        if (this.isDialogCell)
        {
          i = j;
          if ((paramInt & 0x40) != 0)
          {
            localObject1 = (CharSequence)MessagesController.getInstance(this.currentAccount).printingStrings.get(this.currentDialogId);
            if (((this.lastPrintString == null) || (localObject1 != null)) && ((this.lastPrintString != null) || (localObject1 == null)))
            {
              i = j;
              if (this.lastPrintString != null)
              {
                i = j;
                if (localObject1 != null)
                {
                  i = j;
                  if (this.lastPrintString.equals(localObject1)) {}
                }
              }
            }
            else
            {
              i = 1;
            }
          }
        }
        j = i;
        if (i == 0)
        {
          j = i;
          if ((0x8000 & paramInt) != 0)
          {
            j = i;
            if (this.message != null)
            {
              j = i;
              if (this.message.messageText != this.lastMessageString) {
                j = 1;
              }
            }
          }
        }
        i = j;
        if (j == 0)
        {
          i = j;
          if ((paramInt & 0x2) != 0)
          {
            i = j;
            if (this.chat == null) {
              i = 1;
            }
          }
        }
        j = i;
        if (i == 0)
        {
          j = i;
          if ((paramInt & 0x1) != 0)
          {
            j = i;
            if (this.chat == null) {
              j = 1;
            }
          }
        }
        i = j;
        if (j == 0)
        {
          i = j;
          if ((paramInt & 0x8) != 0)
          {
            i = j;
            if (this.user == null) {
              i = 1;
            }
          }
        }
        j = i;
        if (i == 0)
        {
          j = i;
          if ((paramInt & 0x10) != 0)
          {
            j = i;
            if (this.user == null) {
              j = 1;
            }
          }
        }
        i = j;
        if (j == 0)
        {
          i = j;
          if ((paramInt & 0x100) != 0)
          {
            if ((this.message == null) || (this.lastUnreadState == this.message.isUnread())) {
              break label844;
            }
            this.lastUnreadState = this.message.isUnread();
            i = 1;
          }
        }
        j = i;
        if (i == 0)
        {
          j = i;
          if ((paramInt & 0x1000) != 0)
          {
            j = i;
            if (this.message != null)
            {
              j = i;
              if (this.lastSendState != this.message.messageOwner.send_state)
              {
                this.lastSendState = this.message.messageOwner.send_state;
                j = 1;
              }
            }
          }
        }
      } while (j == 0);
      label672:
      label699:
      label762:
      Object localObject2;
      if ((this.isDialogCell) && (MessagesController.getInstance(this.currentAccount).isDialogMuted(this.currentDialogId)))
      {
        bool = true;
        this.dialogMuted = bool;
        this.user = null;
        this.chat = null;
        this.encryptedChat = null;
        paramInt = (int)this.currentDialogId;
        i = (int)(this.currentDialogId >> 32);
        if (paramInt == 0) {
          break label1044;
        }
        if (i != 1) {
          break label937;
        }
        this.chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(paramInt));
        localObject2 = null;
        localObject1 = null;
        if (this.user == null) {
          break label1121;
        }
        this.avatarDrawable.setInfo(this.user);
        if (!UserObject.isUserSelf(this.user)) {
          break label1096;
        }
        this.avatarDrawable.setSavedMessages(1);
      }
      for (;;)
      {
        this.avatarImage.setImage((TLObject)localObject1, "50_50", this.avatarDrawable, null, 0);
        break;
        label825:
        bool = false;
        break label225;
        label831:
        i = 0;
        break label267;
        label836:
        this.drawPin = false;
        break label311;
        label844:
        i = j;
        if (!this.isDialogCell) {
          break label608;
        }
        localObject1 = (TLRPC.TL_dialog)MessagesController.getInstance(this.currentAccount).dialogs_dict.get(this.currentDialogId);
        i = j;
        if (localObject1 == null) {
          break label608;
        }
        if (this.unreadCount == ((TLRPC.TL_dialog)localObject1).unread_count)
        {
          i = j;
          if (this.mentionCount == ((TLRPC.TL_dialog)localObject1).unread_mentions_count) {
            break label608;
          }
        }
        this.unreadCount = ((TLRPC.TL_dialog)localObject1).unread_count;
        this.mentionCount = ((TLRPC.TL_dialog)localObject1).unread_mentions_count;
        i = 1;
        break label608;
        bool = false;
        break label699;
        label937:
        if (paramInt < 0)
        {
          this.chat = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(-paramInt));
          if ((this.isDialogCell) || (this.chat == null) || (this.chat.migrated_to == null)) {
            break label762;
          }
          localObject1 = MessagesController.getInstance(this.currentAccount).getChat(Integer.valueOf(this.chat.migrated_to.channel_id));
          if (localObject1 == null) {
            break label762;
          }
          this.chat = ((TLRPC.Chat)localObject1);
          break label762;
        }
        this.user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(paramInt));
        break label762;
        label1044:
        this.encryptedChat = MessagesController.getInstance(this.currentAccount).getEncryptedChat(Integer.valueOf(i));
        if (this.encryptedChat == null) {
          break label762;
        }
        this.user = MessagesController.getInstance(this.currentAccount).getUser(Integer.valueOf(this.encryptedChat.user_id));
        break label762;
        label1096:
        if (this.user.photo != null)
        {
          localObject1 = this.user.photo.photo_small;
          continue;
          label1121:
          if (this.chat != null)
          {
            localObject1 = localObject2;
            if (this.chat.photo != null) {
              localObject1 = this.chat.photo.photo_small;
            }
            this.avatarDrawable.setInfo(this.chat);
          }
        }
      }
      label1168:
      requestLayout();
    }
  }
  
  public static class CustomDialog
  {
    public int date;
    public int id;
    public boolean isMedia;
    public String message;
    public boolean muted;
    public String name;
    public boolean pinned;
    public boolean sent;
    public int type;
    public int unread_count;
    public boolean verified;
  }
}


/* Location:              /test_eitaa/dex2jar/telegram-dex2jar.jar!/org/telegram/ui/Cells/DialogCell.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */